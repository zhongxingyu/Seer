 package test.cli.cloudify.cloud;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.CloudTestUtils;
 import test.cli.cloudify.CommandTestUtils;
 import test.cli.cloudify.cloud.services.ec2.Ec2CloudService;
 import framework.utils.LogUtils;
 import framework.utils.ScriptUtils;
 
 public class PrivateImageEc2Test extends AbstractCloudTest {
 	private Ec2CloudService service;
 	private static final String CLOUD_SERVICE_UNIQUE_NAME = "PrivateImageEc2Test";
 
 	@BeforeMethod
 	public void bootstrap() throws IOException, InterruptedException {	
		service = new Ec2CloudService(CLOUD_SERVICE_UNIQUE_NAME);
 		service.setAdditionalPropsToReplace(new HashMap<String, String>());
 		service.getAdditionalPropsToReplace().put("imageId \"us-east-1/ami-76f0061f\"", "imageId \"us-east-1/ami-93b068fa\"");
 		service.getAdditionalPropsToReplace().put("connectToPrivateIp true", "connectToPrivateIp true\n\t\tremoteUsername \"ec2-user\"\n");
 		service.setMachinePrefix(this.getClass().getName() + CloudTestUtils.SGTEST_MACHINE_PREFIX);
 		service.bootstrapCloud();
 		//super.setService(service);
 		super.getRestUrl();
 	}
 	
 	
 	@AfterMethod(alwaysRun = true)
 	public void teardown() throws IOException {
 		try {
 			service.teardownCloud();
 		}
 		catch (Throwable e) {
 			LogUtils.log("caught an exception while tearing down ec2", e);
 			sendTeardownCloudFailedMail("ec2", e);
 		}
 		LogUtils.log("restoring original bootstrap-management file");
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, groups = "1", enabled = true)
 	public void installTest() throws Exception {
 		doTest(EC2, "petclinic", "petclinic");
 	}
 
 	protected void doTest(String cloudName, String applicationFolderName, String applicationName) throws IOException, InterruptedException {
 		LogUtils.log("installing application " + applicationName + " on " + cloudName);
 		String applicationPath = ScriptUtils.getBuildPath() + "/recipes/apps/" + applicationFolderName;
 		try {
 			installApplicationAndWait(applicationPath, applicationName);
 		}
 		finally {
 			if ((getService() != null) && (getService().getRestUrls() != null)) {
 				String command = "connect " + getRestUrl() + ";list-applications";
 				String output = CommandTestUtils.runCommandAndWait(command);
 				if (output.contains(applicationName)) {
 					uninstallApplicationAndWait(applicationName);			
 				}
 			}
 		}
 	}
 	
 	
 }
