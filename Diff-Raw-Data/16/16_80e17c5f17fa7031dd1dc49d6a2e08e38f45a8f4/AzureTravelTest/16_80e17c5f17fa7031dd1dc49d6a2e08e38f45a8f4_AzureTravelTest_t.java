 package test.cli.cloudify.cloud.azure;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.io.FileUtils;
 import org.testng.ITestContext;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.cloud.NewAbstractCloudTest;
 import test.cli.cloudify.cloud.services.azure.MicrosoftAzureCloudService;
 import framework.tools.SGTestHelper;
 
 public class AzureTravelTest extends NewAbstractCloudTest {
 
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap(final ITestContext testContext) {
 		MicrosoftAzureCloudService azureCloudService = new MicrosoftAzureCloudService(this.getClass().getName());
 		super.bootstrap(testContext, azureCloudService);
 	}
 	
 	@BeforeMethod
 	public void copyTravelAzureApplicationToBuildDir() throws IOException {
 		
 		File appsDir = new File(SGTestHelper.getBuildDir() + "/recipes/apps");
 		File originalAzureApplication = new File(SGTestHelper.getBuildDir() + "/recipes/apps/travel-azure");
 		FileUtils.deleteDirectory(originalAzureApplication);
 		File newAzureApplication = new File(SGTestHelper.getSGTestRootDir() + "/apps/cloudify/recipes/travel-azure");
 		FileUtils.copyDirectoryToDirectory(newAzureApplication, appsDir);
 		
 	}
 	
 	@AfterClass(alwaysRun = true)
 	protected void teardown() {
 		super.teardown();
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 4)
 	public void testTravel() throws IOException, InterruptedException {
 		doSanityTest("travel-azure", "travel");		
 	}
 	
 	@AfterMethod
 	public void cleanUp() {
 		super.scanAgentNodesLeak();
 	}
 	
 	@Override
 	protected void customizeCloud() throws Exception {
 		
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
