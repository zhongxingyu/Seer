 package test.cli.cloudify.cloud.ec2;
 
 import java.io.File;
import java.util.HashMap;
import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.testng.ITestContext;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.cloud.NewAbstractCloudTest;
 import test.cli.cloudify.cloud.services.ec2.Ec2CloudService;
import framework.tools.SGTestHelper;
 import framework.utils.LogUtils;
 import framework.utils.ScriptUtils;
 
 public class Ec2PropertiesFileTest extends NewAbstractCloudTest{
 	private static final String MANAGEMENT_MACHINE_TEMPLATE_MY_HARDWARE_ID = "managementMachineTemplate myHardwareId";
 	final String serviceName = "tomcat";
 	final String tomcatServicePath = ScriptUtils.getBuildPath() + "/recipes/services/" + serviceName;
 
 	@Override
 	protected String getCloudName() {
 		return "ec2";
 	}
 	
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap(final ITestContext testContext) {
 		super.bootstrap(testContext);
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = true)
 	public void testInstallationWithPropsFile() throws Exception {
 		String pathToCloudFolder = ((Ec2CloudService)cloud).getPathToCloudFolder();
 		File cloudConfig = new File(pathToCloudFolder, "ec2-cloud.groovy");
 		String configFileAsString = FileUtils.readFileToString(cloudConfig);
 		LogUtils.log("asserting cloud was bootstrapped with the correct properties (properties were overridden)");
 		assertTrue("Management machine did not start using the properties defined in the properties file",
 				configFileAsString.contains(MANAGEMENT_MACHINE_TEMPLATE_MY_HARDWARE_ID));
 
 		LogUtils.log("Testing service installation");
 		installServiceAndWait(tomcatServicePath, serviceName);
 		uninstallServiceAndWait(serviceName);
 	}
 
 	@Override
 	protected void customizeCloud() throws Exception {
		//Copy the props file into the cloud folder path
		Map<File, File> filesToReplace = new HashMap<File, File>();
		String pathToCloudFolder = ((Ec2CloudService)cloud).getPathToCloudFolder();
		String sgTestRootDir = SGTestHelper.getSGTestRootDir() ;
		File propertiesFile = new File(sgTestRootDir + "/apps/cloudify/cloud/ec2/", "ec2-cloud.properties");
		File mockFile = new File(pathToCloudFolder, "ec2-cloud.properties");
		filesToReplace.put(mockFile, propertiesFile);
		((Ec2CloudService)cloud).addFilesToReplace(filesToReplace);
 		
 		//Set the management machine template option to be taken from the cloud props file.
 		((Ec2CloudService)cloud).getAdditionalPropsToReplace().put("managementMachineTemplate \"SMALL_LINUX\"",
 				MANAGEMENT_MACHINE_TEMPLATE_MY_HARDWARE_ID);
 		
 	}
 	
 	@AfterClass(alwaysRun = true)
 	protected void teardown() {
 		super.teardown();
 	}
 
 	@Override
 	protected boolean isReusableCloud() {
 		return false;
 	}
 
 }
