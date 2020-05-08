 package test.cli.cloudify.cloud;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.CloudTestUtils;
 import test.cli.cloudify.cloud.ec2.Ec2CloudService;
 import framework.utils.LogUtils;
 
 public class PrivateImageEc2Test extends ExamplesTest {
 	private Ec2CloudService service;
	private Ec2CloudService oldService;
 
 	@BeforeMethod
 	public void bootstrap() throws IOException, InterruptedException {	
		oldService = (Ec2CloudService) getDefaultService(EC2);
 		service = new Ec2CloudService();
 		service.setAdditionalPropsToReplace(new HashMap<String, String>());
 		service.getAdditionalPropsToReplace().put("imageId \"us-east-1/ami-76f0061f\"", "imageId \"us-east-1/ami-93b068fa\"");
 		service.setMachinePrefix(this.getClass().getName() + CloudTestUtils.SGTEST_MACHINE_PREFIX);
 		service.bootstrapCloud();
 		super.setService(service);
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
		setService(oldService);
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, groups = "1", enabled = true)
 	public void installTest() throws Exception {
 		doTest(EC2, "petclinic", "petclinic");
 	}
 
 	
 	
 }
