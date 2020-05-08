 package test.cli.cloudify.cloud.byon.publicprovisioning;
 
 import java.io.IOException;
 import java.util.concurrent.TimeUnit;
 
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.ProcessingUnitInstance;
 import org.testng.ITestContext;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.CommandTestUtils;
 import framework.utils.LogUtils;
 
 public class AutomaticPublicProvisioningTest extends AbstractPublicProvisioningByonCloudTest {
 	
 	private static final String SERVICE_NAME = "customServiceMonitor";
 	
 	@Override
 	protected String getCloudName() {
 		return "byon";
 	}
 	
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap(final ITestContext testContext) {
 		super.bootstrap(testContext);
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void testAutoScalingPublicProvisioning() throws IOException, InterruptedException {
 		
 		installAutomaticPublicProvisioningServiceAndWait(SERVICE_NAME, 1, 128, 0, DEFAULT_TEMPLATE_NAME);
 		
 		ProcessingUnit pu = admin.getProcessingUnits().waitFor(ServiceUtils.getAbsolutePUName("default", SERVICE_NAME), OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
 		
 		// the threshold is set to 1 averaged on all instances.
 		// we set the value to 10 causing the average to be 10, this breaches the threshold and an instance should be added.
 		// one this is done no scale in will take place since the average will always be > 1
 		setStatistics(pu, 1, 10);
 		
 		// wait for 2 instances
 		repetitiveAssertNumberOfInstances(pu, 2);
 		
 		// check that they are both on the same machine
 		ProcessingUnitInstance[] instances = pu.getInstances();
 		assertTrue("pu instances were installed on 2 different machines", instances[0].getGridServiceContainer().getMachine().getHostAddress().equals(instances[1].getGridServiceContainer().getMachine().getHostAddress()));
 		
 		uninstallServiceAndWait(SERVICE_NAME);
 
 		
 	}
 		
 	@AfterClass(alwaysRun = true)
 	protected void teardown() {
 		super.teardown();
 	}
 	
 	@Override
	public void beforeTeardown() throws IOException, InterruptedException {
 		super.uninstallServicefFound(SERVICE_NAME);
 	}
 	
 	private void setStatistics(final ProcessingUnit pu, final int expectedNumberOfInstances, long value) throws IOException, InterruptedException {
 		
 		ProcessingUnitInstance[] instances = repetitiveAssertNumberOfInstances(pu, expectedNumberOfInstances);
 		
 		for (ProcessingUnitInstance instance : instances) {
 			String command = "connect " + getService().getRestUrls()[0] + ";invoke -instanceid " + instance.getInstanceId() + " --verbose " + SERVICE_NAME + " set " + value;
 			String output = CommandTestUtils.runCommandAndWait(command);
 			LogUtils.log(output);
 		}
 	}
 
 
 }
