 package test.cli.cloudify.cloud.azure;
 
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.cloud.NewAbstractCloudTest;
 import test.cli.cloudify.cloud.services.CloudServiceManager;
 import test.cli.cloudify.cloud.services.azure.MicrosoftAzureCloudService;
 import framework.utils.AssertUtils;
 
 /**
  * CLOUDIFY-1397
  * @author elip
  *
  */
 public class FaultyAzureConfigurationTest extends NewAbstractCloudTest {
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 5, enabled = true)
 	public void testInvalidPassword() throws Exception {
 		
 		String expectedOutput = "The supplied password must be 6-72 characters long and meet password complexity requirements";
 		
 		MicrosoftAzureCloudService azureCloudService = (MicrosoftAzureCloudService) CloudServiceManager.getInstance().getCloudService(getCloudName());
 		
 		// this password is invalid due to azure password restrictions.
 		azureCloudService.setPassword("1234");
 		azureCloudService.getBootstrapper().verbose(false).setBootstrapExpectedToFail(true);
 		super.bootstrap(azureCloudService);
 		
 		String bootstrapOutput = azureCloudService.getBootstrapper().getLastActionOutput();
 		AssertUtils.assertTrue("Bootstrap failed but did not contain the necessary output " + expectedOutput, bootstrapOutput.toLowerCase().contains(expectedOutput.toLowerCase()));
 	}
 	
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 5, enabled = true)
 	public void testInvalidAddressSpace() throws Exception {
 		
 		String expectedOutput = "IP address is not valid '582.0.0.1/16'";
 		
 		MicrosoftAzureCloudService azureCloudService = (MicrosoftAzureCloudService) CloudServiceManager.getInstance().getCloudService(getCloudName());
 		
 		// this addressSpace is invalid, obviously
 		azureCloudService.setAddressSpace("582.0.0.1/16");
 		String className = this.getClass().getSimpleName().toLowerCase();
 		azureCloudService.setVirtualNetworkSiteName(className + "network");
		azureCloudService.setAffinityGroup(className + "affinity");
 		azureCloudService.getBootstrapper().verbose(false).setBootstrapExpectedToFail(true);
 		super.bootstrap(azureCloudService);
 		
 		String bootstrapOutput = azureCloudService.getBootstrapper().getLastActionOutput();
 		AssertUtils.assertTrue("Bootstrap failed but did not contain the necessary output " + expectedOutput, bootstrapOutput.toLowerCase().contains(expectedOutput.toLowerCase()));		
 	}		
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 5, enabled = true)
 	public void testInvalidImageId() throws Exception {
 		
 		String expectedOutput = "The image invalidImageId does not exist";
 		
 		MicrosoftAzureCloudService azureCloudService = (MicrosoftAzureCloudService) CloudServiceManager.getInstance().getCloudService(getCloudName());
 		
 		// this image id is invalid, obviously
 		azureCloudService.getAdditionalPropsToReplace().put(MicrosoftAzureCloudService.DEFAULT_IMAGE_ID, "invalidImageId");
 		azureCloudService.getBootstrapper().verbose(false).setBootstrapExpectedToFail(true);
 		super.bootstrap(azureCloudService);
 		
 		String bootstrapOutput = azureCloudService.getBootstrapper().getLastActionOutput();
 		AssertUtils.assertTrue("Bootstrap failed but did not contain the necessary output " + expectedOutput, bootstrapOutput.toLowerCase().contains(expectedOutput.toLowerCase()));		
 	}
 
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 5, enabled = true)
 	public void testInvalidAffinityLocation() throws Exception {
 		
 		String expectedOutput = "The location constraint is not valid";
 		
 		MicrosoftAzureCloudService azureCloudService = (MicrosoftAzureCloudService) CloudServiceManager.getInstance().getCloudService(getCloudName());
 		
 		// this affinity location is invalid, obviously
 		azureCloudService.setAffinityLocation("invalidLocation");
 		String className = this.getClass().getSimpleName().toLowerCase();
 		azureCloudService.setAffinityGroup(className + "affinity");
 		azureCloudService.getBootstrapper().verbose(false).setBootstrapExpectedToFail(true);
 		super.bootstrap(azureCloudService);
 		
 		String bootstrapOutput = azureCloudService.getBootstrapper().getLastActionOutput();
 		AssertUtils.assertTrue("Bootstrap failed but did not contain the necessary output " + expectedOutput, bootstrapOutput.toLowerCase().contains(expectedOutput.toLowerCase()));		
 
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
