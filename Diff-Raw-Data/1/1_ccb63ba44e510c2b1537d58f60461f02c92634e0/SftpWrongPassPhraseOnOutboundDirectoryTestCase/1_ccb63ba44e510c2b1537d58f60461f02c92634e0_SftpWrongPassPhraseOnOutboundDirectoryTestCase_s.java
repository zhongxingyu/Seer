 package org.mule.transport.sftp.dataintegrity;
 
 import java.io.IOException;
 
 import org.mule.api.endpoint.ImmutableEndpoint;
 import org.mule.module.client.MuleClient;
 import org.mule.transport.sftp.SftpClient;
 
 /**
  * Verify that the original file is not lost if the password for the outbound endpoint is wrong
  */
 public class SftpWrongPassPhraseOnOutboundDirectoryTestCase extends AbstractSftpDataIntegrityTestCase
 {
 
 	private static String INBOUND_ENDPOINT_NAME = "inboundEndpoint";
 
 	protected String getConfigResources()
 	{
 		return "dataintegrity/sftp-wrong-passphrase-config.xml";
 	}
 
     @Override
     protected void doSetUp() throws Exception {
         super.doSetUp();
 
         // Delete the in & outbound directories
 		initEndpointDirectory(INBOUND_ENDPOINT_NAME);
     }
 
 	/**
 	 * The outbound directory doesn't exist.
 	 * The source file should still exist
 	 * @throws Exception
 	 */
 	public void testWrongPassPhraseOnOutboundDirectory() throws Exception
 	{
 		MuleClient muleClient = new MuleClient();
 
 		// Send an file to the SFTP server, which the inbound-outboundEndpoint then can pick up
     	Exception exception = dispatchAndWaitForException(new DispatchParameters(INBOUND_ENDPOINT_NAME, null), "sftp");
         assertNotNull(exception);
         assertTrue(exception instanceof IOException);
         assertTrue(exception.getMessage().startsWith("Error during login to"));
        assertTrue(exception.getMessage().endsWith("Auth fail"));
 
 		SftpClient sftpClient = getSftpClient(muleClient, INBOUND_ENDPOINT_NAME);
         try {
             ImmutableEndpoint endpoint = (ImmutableEndpoint) muleClient.getProperty(INBOUND_ENDPOINT_NAME);
             assertTrue("The inbound file should still exist", super.verifyFileExists(sftpClient, endpoint.getEndpointURI(), FILE_NAME));
         } finally {
             sftpClient.disconnect();
         }
     }
 
 }
