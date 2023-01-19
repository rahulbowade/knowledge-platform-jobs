package org.sunbird.job.videostream.helpers

import java.io.ByteArrayInputStream
import java.nio.charset.Charset
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.cloud.video.transcoder.v1.{TranscoderServiceClient, TranscoderServiceSettings}
import com.google.auth.oauth2.GoogleCredentials
import com.google.common.collect.Lists
import org.slf4j.LoggerFactory
import org.sunbird.job.videostream.exception.MediaServiceException
import org.sunbird.job.videostream.task.VideoStreamGeneratorConfig

object GCPAuthUtil {

  private var transcoderServiceClient: TranscoderServiceClient = null

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  private def getCredentials()(implicit config: VideoStreamGeneratorConfig): GoogleCredentials = {
    val serviceAccCred: String = config.getConfig("gcp.service_account_cred")
    val scope = Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform")
    val credentialsStream = new ByteArrayInputStream(serviceAccCred.getBytes(Charset.forName("UTF-8")))
    val credential = GoogleCredentials.fromStream(credentialsStream).createScoped(scope)
    logger.info("credential in getCredentials : " + credential)
    credential
  }

  def getTranscoderServiceClient()(implicit config: VideoStreamGeneratorConfig): TranscoderServiceClient = {
    if (null != transcoderServiceClient) return transcoderServiceClient
    try {
      val transcoderServiceSettings: TranscoderServiceSettings = TranscoderServiceSettings.newBuilder().
        setCredentialsProvider(FixedCredentialsProvider.create(getCredentials())).build();
      transcoderServiceClient = TranscoderServiceClient.create(transcoderServiceSettings)
      logger.info("transcoderServiceClient in getTranscoderServiceClient : " + transcoderServiceClient)
      transcoderServiceClient
    } catch {
      case ex: Exception => throw new MediaServiceException("ERR_CREATE_GCP_CLIENT", "Unable to create Google Transcoder Service Client")
    }
  }
}