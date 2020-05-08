 package test.cli.cloudify;
 
 import org.openspaces.admin.gsc.GridServiceContainer;
 import org.openspaces.admin.pu.DeploymentStatus;
 import org.testng.annotations.Test;
 
 import com.gigaspaces.cloudify.dsl.utils.ServiceUtils;
 
 import framework.tools.SGTestHelper;
 import framework.utils.LogUtils;
 
 public class StockDemoApplicationTest extends AbstractLocalCloudTest {
 	
 	private String STOCK_DEMO_APPLICATION_NAME = "stockdemo";
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void testStockdemoApplication() throws Exception {
 		String applicationDir = SGTestHelper.getSGTestRootDir() + "/apps/USM/usm/applications/stockdemo";
 		String command = "connect " + restUrl + ";" + "install-application " + "--verbose -timeout 25 " + applicationDir;
 		CommandTestUtils.runCommandAndWait(command);
 		
 		assertTrue(STOCK_DEMO_APPLICATION_NAME + " is not installed" , admin.getApplications().getApplication("stockdemo") != null);
 		
 		int currentNumberOfInstances;
 		currentNumberOfInstances = getProcessingUnitInstanceCount(ServiceUtils.getAbsolutePUName(STOCK_DEMO_APPLICATION_NAME, "cassandra"));
 		assertTrue("Expected 2 PU instances. Actual number of instances is " + currentNumberOfInstances, currentNumberOfInstances == 1);
		assertPuStatusInstact("cassandra");
 		
 		currentNumberOfInstances = getProcessingUnitInstanceCount(ServiceUtils.getAbsolutePUName(STOCK_DEMO_APPLICATION_NAME, "stockAnalyticsMirror"));
 		assertTrue("Expected 1 PU instances. Actual number of instances is " + currentNumberOfInstances, currentNumberOfInstances == 1);
		assertPuStatusInstact("stockAnalyticsMirror");
 		
 		currentNumberOfInstances = getProcessingUnitInstanceCount(ServiceUtils.getAbsolutePUName(STOCK_DEMO_APPLICATION_NAME, "stockAnalyticsSpace"));
 		assertTrue("Expected 1 PU instances. Actual number of instances is " + currentNumberOfInstances, currentNumberOfInstances == 2);
		assertPuStatusInstact("stockAnalyticsSpace");
 		
 		currentNumberOfInstances = getProcessingUnitInstanceCount(ServiceUtils.getAbsolutePUName(STOCK_DEMO_APPLICATION_NAME, "stockAnalyticsProcessor"));
 		assertTrue("Expected 1 PU instances. Actual number of instances is " + currentNumberOfInstances, currentNumberOfInstances == 4);
		assertPuStatusInstact("stockAnalyticsProcessor");
 		
 		currentNumberOfInstances = getProcessingUnitInstanceCount(ServiceUtils.getAbsolutePUName(STOCK_DEMO_APPLICATION_NAME, "StockDemo"));
 		assertTrue("Expected 1 PU instances. Actual number of instances is " + currentNumberOfInstances, currentNumberOfInstances == 1);
		assertPuStatusInstact("StockDemo");
 		
 		currentNumberOfInstances = getProcessingUnitInstanceCount(ServiceUtils.getAbsolutePUName(STOCK_DEMO_APPLICATION_NAME, "stockAnalytics"));
 		assertTrue("Expected 1 PU instances. Actual number of instances is " + currentNumberOfInstances, currentNumberOfInstances == 1);
		assertPuStatusInstact("stockAnalytics");
 		
 		for (GridServiceContainer gsc : admin.getGridServiceContainers().getContainers()) {
 			LogUtils.scanContainerLogsFor(gsc, "[SEVERE]");
 		}
 	}
 	
 	private void assertPuStatusInstact(String puName) {
 		assertTrue(admin.getProcessingUnits().getProcessingUnit(puName).getStatus().equals(DeploymentStatus.INTACT));
 	}
 
 }
