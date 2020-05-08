 package test.cli.cloudify.cloud.byon.sharedprovisioning;
 
 import java.io.IOException;
 import java.util.Set;
 
import org.cloudifysource.dsl.internal.CloudifyConstants;
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import org.openspaces.admin.machine.Machine;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import framework.utils.AssertUtils;
 import framework.utils.ProcessingUnitUtils;
 
 public class AppSharedProvisioningTest extends AbstractSharedProvisioningByonCloudTest {
 		
 	private static final String APPLICATION_ONE = "application1";
 	private static final String APPLICATION_TWO = "application2";
 	
 	private static final String SERVICE_ONE = "service1";
 	private static final String SERVICE_TWO = "service2";
 	
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap() throws Exception {
 		super.bootstrap();
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void testTwoApplications() throws IOException, InterruptedException {
 		super.installManualAppSharedProvisioningApplicationAndWait(APPLICATION_ONE);
 		super.installManualAppSharedProvisioningApplicationAndWait(APPLICATION_TWO);
 		
 		Set<Machine> applicationOneMachines = ProcessingUnitUtils.getMachinesOfApplication(admin, APPLICATION_ONE);
 		Set<Machine> applicationTwoMachines = ProcessingUnitUtils.getMachinesOfApplication(admin, APPLICATION_TWO);
 		
 		// make sure they dont overlap
 		AssertUtils.assertTrue("applications have ovelapping machines even though the isolation is app based", 
 				!applicationOneMachines.removeAll(applicationTwoMachines));
 		
 		super.uninstallApplicationAndWait(APPLICATION_ONE);
 		super.uninstallApplicationAndWait(APPLICATION_TWO);
 		
 		
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void testTwoServicesOnOneApplication() throws IOException, InterruptedException {
 		super.installManualAppSharedProvisioningServiceAndWait(SERVICE_ONE);
 		super.installManualAppSharedProvisioningServiceAndWait(SERVICE_TWO);
 		
		Set<Machine> serviceOneMachines = ProcessingUnitUtils.getMachinesOfService(admin, ServiceUtils.getAbsolutePUName(CloudifyConstants.DEFAULT_APPLICATION_NAME, SERVICE_ONE));
		Set<Machine> serviceTwoMachines = ProcessingUnitUtils.getMachinesOfService(admin, ServiceUtils.getAbsolutePUName(CloudifyConstants.DEFAULT_APPLICATION_NAME, SERVICE_TWO));
 		
 		AssertUtils.assertTrue("services should be deployed on the same machine since they belong to the same application", 
 				serviceOneMachines.equals(serviceTwoMachines));
 		
 		super.uninstallServiceAndWait(SERVICE_ONE);
 		super.uninstallServiceAndWait(SERVICE_TWO);
 	}
 	
 	@AfterMethod
 	public void cleanup() throws IOException, InterruptedException {
 		super.uninstallApplicationIfFound(APPLICATION_ONE);
 		super.uninstallApplicationIfFound(APPLICATION_TWO);
 	}
 	
 	@AfterClass(alwaysRun = true)
 	protected void teardown() throws Exception {
 		super.teardown();
 	}
 }
