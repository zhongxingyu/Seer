 package test.cli.cloudify.cloud.azure;
 
 import org.cloudifysource.dsl.internal.CloudifyErrorMessages;
 import org.testng.annotations.Test;
 
 import framework.utils.AssertUtils;
 
 import test.cli.cloudify.cloud.NewAbstractCloudTest;
 import test.cli.cloudify.cloud.services.CloudServiceManager;
 import test.cli.cloudify.cloud.services.azure.MicrosoftAzureCloudService;
 
 /**
  * CLOUDIFY-1397
  * @author elip
  *
  */
 public class FaultyAzureConfigurationTest extends NewAbstractCloudTest {
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 5, enabled = true)
 	public void testInvalidPassword() throws Exception {
 		MicrosoftAzureCloudService azureCloudService = (MicrosoftAzureCloudService) CloudServiceManager.getInstance().getCloudService(getCloudName());
 		
 		// this password is invalid due to azure password restrictions.
 		azureCloudService.setPassword("1234");
 		azureCloudService.getBootstrapper().verbose(false).setBootstrapExpectedToFail(true);
 		super.bootstrap(azureCloudService);
 		
 		String bootstrapOutput = azureCloudService.getBootstrapper().getLastActionOutput();
 		AssertUtils.assertTrue("Bootstrap failed but did not contain the necessary output code : " +  CloudifyErrorMessages.CLOUD_API_ERROR.getName(), bootstrapOutput.contains(CloudifyErrorMessages.CLOUD_API_ERROR.getName()));
 	}
 	
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 5, enabled = true)
 	public void testInvalidAddressSpace() throws Exception {
 		
 		MicrosoftAzureCloudService azureCloudService = (MicrosoftAzureCloudService) CloudServiceManager.getInstance().getCloudService(getCloudName());
 		
 		// this addressSpace is invalid, obviously
 		azureCloudService.setAddressSpace("invalidAddressSpace");
 		azureCloudService.getBootstrapper().setBootstrapExpectedToFail(true);
 		super.bootstrap(azureCloudService);
 		
 		String bootstrapOutput = azureCloudService.getBootstrapper().getLastActionOutput();
 		AssertUtils.assertTrue("Bootstrap failed but did not contain the necessary output code : " +  CloudifyErrorMessages.CLOUD_API_ERROR.getName(), bootstrapOutput.contains(CloudifyErrorMessages.CLOUD_API_ERROR.getName()));		
 	}		
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 5, enabled = true)
 	public void testInvalidImageId() throws Exception {
 		
 		MicrosoftAzureCloudService azureCloudService = (MicrosoftAzureCloudService) CloudServiceManager.getInstance().getCloudService(getCloudName());
 		
 		// this image id is invalid, obviously
		azureCloudService.getAdditionalPropsToReplace().put(MicrosoftAzureCloudService.DEFAULT_IMAGE_ID, "invalidImageId");
 		azureCloudService.getBootstrapper().setBootstrapExpectedToFail(true);
 		super.bootstrap(azureCloudService);
 		
 		String bootstrapOutput = azureCloudService.getBootstrapper().getLastActionOutput();
 		AssertUtils.assertTrue("Bootstrap failed but did not contain the necessary output code : " +  CloudifyErrorMessages.CLOUD_API_ERROR.getName(), bootstrapOutput.contains(CloudifyErrorMessages.CLOUD_API_ERROR.getName()));		
 	}
 
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 5, enabled = true)
 	public void testInvalidAffinityLocation() throws Exception {
 		
 		MicrosoftAzureCloudService azureCloudService = (MicrosoftAzureCloudService) CloudServiceManager.getInstance().getCloudService(getCloudName());
 		
 		// this affinity location is invalid, obviously
 		azureCloudService.setAffinityLocation("invalidLocation");
 		azureCloudService.getBootstrapper().setBootstrapExpectedToFail(true);
 		super.bootstrap(azureCloudService);
 		
 		String bootstrapOutput = azureCloudService.getBootstrapper().getLastActionOutput();
 		AssertUtils.assertTrue("Bootstrap failed but did not contain the necessary output code : " +  CloudifyErrorMessages.CLOUD_API_ERROR.getName(), bootstrapOutput.contains(CloudifyErrorMessages.CLOUD_API_ERROR.getName()));		
 
 	}
 	
 	@Override
 	protected String getCloudName() {
 		return "azure";
 	}
 
 	@Override
 	protected boolean isReusableCloud() {
 		return false;
 	}
 	
 	
 
 }
