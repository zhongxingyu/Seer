 package test.cli.cloudify;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
import java.net.InetAddress;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
import org.openspaces.admin.AdminFactory;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.ProcessingUnitInstance;
 import org.openspaces.pu.service.ServiceDetails;
 import org.openspaces.pu.service.ServiceMonitors;
 import org.testng.annotations.Test;
 
 import com.gigaspaces.cloudify.dsl.internal.CloudifyConstants;
 import com.gigaspaces.cloudify.dsl.internal.ServiceReader;
 import com.gigaspaces.cloudify.dsl.internal.packaging.PackagingException;
 import com.gigaspaces.cloudify.dsl.utils.ServiceUtils;
 
 
public class LifecycleDetailsMonitorMapClosureTest extends AbstractCommandTest{
 
 	final private String RECIPE_DIR_PATH = CommandTestUtils
 	.getPath("apps/USM/usm/simple");
 	private static final String EXPECTED_DETAILS_FIELDS[] = { "1",
 		"2"};
 	private static final String EXPECTED_MONITORS_FIELDS[] = {"3",
 	"4" };
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void testDetailsAndMonitorsMap() throws FileNotFoundException, PackagingException, IOException, InterruptedException{
 		installService();
 		ProcessingUnitInstance puInstance = getPUInstance();
 		checkDetails(puInstance);
 		checkMonitors(puInstance);
 	}
 	
 	private ProcessingUnitInstance getPUInstance() {
 		ProcessingUnit pu = this.admin.getProcessingUnits().waitFor(
 				ServiceUtils.getAbsolutePUName(DEFAULT_APPLICTION_NAME, "simple"), 30, TimeUnit.SECONDS);
 		assertNotNull("Could not find processing unit for installed service",
 				pu);
 		boolean found = pu.waitFor(1, 30, TimeUnit.SECONDS);
 		assertTrue("Could not find instance of deployed service", found);
 
 		ProcessingUnitInstance pui = pu.getInstances()[0];
 		return pui;
 		
 	}
 	
 	private void installService() throws FileNotFoundException,
 	PackagingException, IOException, InterruptedException {
 		File serviceDir = new File(RECIPE_DIR_PATH);
 		ServiceReader.getServiceFromDirectory(serviceDir, CloudifyConstants.DEFAULT_APPLICATION_NAME).getService();
 
		runCommand("bootstrap-localcloud;install-service --verbose " + RECIPE_DIR_PATH);
		AdminFactory factory = new AdminFactory();
		factory.addLocator(InetAddress.getLocalHost().getHostAddress() + ":4168");
		this.admin = factory.create();
 		assertTrue("Could not find LUS of local cloud", admin.getLookupServices().waitFor(1, 10, TimeUnit.SECONDS));
 
		this.restUrl = "http://" + InetAddress.getLocalHost().getHostAddress() + ":8100";
 	}
 	
 	private void checkDetails(ProcessingUnitInstance pui) {
 		Collection<ServiceDetails> allServiceDetails = pui
 				.getServiceDetailsByServiceId().values();
 		Map<String, Object> allDetails = new HashMap<String, Object>();
 		for (ServiceDetails serviceDetails : allServiceDetails) {
 			allDetails.putAll(serviceDetails.getAttributes());
 		}
 		for (String detailKey : EXPECTED_DETAILS_FIELDS) {
 			assertTrue("Missing details entry: " + detailKey,
 					allDetails.containsKey(detailKey));
 		}
 	}
 	
 	private void checkMonitors(ProcessingUnitInstance pui) {
 		// verify monitors
 		Collection<ServiceMonitors> allSserviceMonitors = pui.getStatistics()
 				.getMonitors().values();
 		Map<String, Object> allMonitors = new HashMap<String, Object>();
 		for (ServiceMonitors serviceMonitors : allSserviceMonitors) {
 			allMonitors.putAll(serviceMonitors.getMonitors());
 		}
 
 		for (String monitorKey : EXPECTED_MONITORS_FIELDS) {
 			assertTrue("Missing Monitor Key: " + monitorKey,
 					allMonitors.containsKey(monitorKey));
 		}
 	}
 }
