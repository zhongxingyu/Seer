 package test.cli.cloudify.pu.xen;
 
 import java.io.IOException;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.cloudifysource.dsl.internal.CloudifyConstants;
 import org.openspaces.admin.esm.ElasticServiceManager;
 import org.openspaces.admin.esm.ElasticServiceManagers;
 import org.openspaces.admin.esm.events.ElasticServiceManagerRemovedEventListener;
 import org.openspaces.admin.internal.admin.DefaultAdmin;
 import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.cloud.xenserver.XenServerMachineProvisioningConfig;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.CommandTestUtils;
 import test.cli.cloudify.xen.AbstractApplicationFailOverXenTest;
 import framework.tools.SGTestHelper;
 import framework.utils.LogUtils;
 import framework.utils.xen.GsmTestUtils;
 
 
 public class Stockdemo2ManagementsEsmFailOverTest extends AbstractApplicationFailOverXenTest {
 	
 	private final String stockdemoAppDirPath = SGTestHelper.getSGTestRootDir().replace("\\", "/") + "/apps/USM/usm/applications/stockdemo";
 	private XenServerMachineProvisioningConfig xenConfigOfEsmMachine;
 	private Machine esmMachine;
 	
 	@BeforeMethod
 	public void beforeTest() {
 		setEdition("CLOUDIFY_XAP");
 		Logger.getLogger(DefaultAdmin.class.getName()).setLevel(Level.ALL);
 		super.beforeTest();
 		
 		assignCassandraPorts(stockdemoAppDirPath);
 		
 		xenConfigOfEsmMachine = getMachineProvisioningConfig();
 		ElasticServiceManager esm = admin.getElasticServiceManagers().waitForAtLeastOne();
 		esmMachine = esm.getMachine();
 		
 		startAgent(0 ,"stockAnalytics" ,"stockAnalyticsMirror" ,"StockDemo" ,"cassandra");
 	    startAgent(0 ,"stockAnalyticsProcessor" ,"stockAnalyticsSpace" , "stockAnalyticsFeeder");
 	    startAgent(0 ,"stockAnalyticsProcessor" ,"stockAnalyticsSpace");
 	    
 	    boolean agents = admin.getGridServiceAgents().waitFor(5, DEFAULT_RECOVERY_TIME, TimeUnit.MICROSECONDS);
 	    assertTrue(agents);
 	    
 	    repetitiveAssertNumberOfGSAsRemoved(0, OPERATION_TIMEOUT);
 	    cassandraHostIp = admin.getZones().getByName("cassandra").getGridServiceAgents().getAgents()[0].getMachine().getHostAddress();
 	    
 	    try {
 			installApp(cassandraPort1 ,cassandraHostIp, cassandraPort2 ,cassandraHostIp , stockdemoAppDirPath);
 			assertStockdemoAppInstalled(cassandraPort1 ,cassandraHostIp, cassandraPort2 ,cassandraHostIp);
 		} catch (Exception e) {
 		    AssertFail("Failed installing stockdemo application", e);
 		}
 	}
 
 	private void uninstallApplication() {
 		try {
 			String host = admin.getElasticServiceManagers().getManagers()[0].getMachine().getHostAddress();
 			restUrl = host + ":8100";
 			CommandTestUtils.runCommandAndWait("connect " + restUrl + " ;uninstall-application stockdemo");
 		} catch (IOException e) {
 			AssertFail("Failed to uninstall application stockdemo", e);
 		} catch (InterruptedException e) {
 			AssertFail("Failed to uninstall application stockdemo", e);
 		}
 		
 		assertAppUninstalled("stockdemo");
 	}
 	
 	/** 
 	 * TODO: shutting down the machine running the esm closes all others
 	 * 
 	 * @throws Exception
 	 */
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true, invocationCount = 1)
 	public void testEsmMachineShutdownFailover() throws Exception {
 		
 		final ElasticServiceManager esm = admin.getElasticServiceManagers().waitForAtLeastOne(10, TimeUnit.SECONDS);
         final CountDownLatch latch = new CountDownLatch(1);
         
         ElasticServiceManagerRemovedEventListener removedEventListener = new ElasticServiceManagerRemovedEventListener() {
 
             public void elasticServiceManagerRemoved(
                     ElasticServiceManager elasticServiceManager) {
                 if (elasticServiceManager.equals(esm)) {
                     latch.countDown();
                 }
             }
         };
         ElasticServiceManagers managers = esm.getAdmin().getElasticServiceManagers();
         managers.getElasticServiceManagerRemoved().add(removedEventListener);
         
 		LogUtils.log("Shuting down esm's mahcine gracefuly");
 		GsmTestUtils.shutdownMachine(esmMachine, xenConfigOfEsmMachine, DEFAULT_TEST_TIMEOUT);
 		latch.await();
 	
 		assertDesiredLogic();
 		uninstallApplication();
 	}
 	
 	/**
 	 * GS-8637
 	 */
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
 	public void testEsmMachineHardShutdownFailover() throws Exception {
 		
 		//TODO : how do I know that I got the right esm?
 		final ElasticServiceManager esm = admin.getElasticServiceManagers().waitForAtLeastOne(10, TimeUnit.SECONDS);
         final CountDownLatch latch = new CountDownLatch(1);
         
         ElasticServiceManagerRemovedEventListener removedEventListener = new ElasticServiceManagerRemovedEventListener() {
 
             public void elasticServiceManagerRemoved(
                     ElasticServiceManager elasticServiceManager) {
                 if (elasticServiceManager.equals(esm)) {
                     latch.countDown();
                 }
             }
         };
         ElasticServiceManagers managers = esm.getAdmin().getElasticServiceManagers();
         managers.getElasticServiceManagerRemoved().add(removedEventListener);
         
         LogUtils.log("Shuting down esm's mahcine");
 		GsmTestUtils.hardShutdownMachine(esmMachine, xenConfigOfEsmMachine, DEFAULT_TEST_TIMEOUT);
 		latch.await();
 	
 		assertDesiredLogic();
 		uninstallApplication();
 	}
 	
 //////////////////////////////////////////////////////////////////////////////////////////////////////////////	
 	
 	 private void assertEnvSanity() {
 		boolean esmRestarted = admin.getElasticServiceManagers().waitFor(1, DEFAULT_RECOVERY_TIME, TimeUnit.MILLISECONDS);
 		boolean gsmUp = admin.getGridServiceManagers().waitFor(1, DEFAULT_RECOVERY_TIME, TimeUnit.MILLISECONDS);
 		boolean lusUp = admin.getLookupServices().waitFor(1, DEFAULT_RECOVERY_TIME, TimeUnit.MILLISECONDS);
 		boolean gsaUp = admin.getGridServiceAgents().waitFor(4, DEFAULT_RECOVERY_TIME, TimeUnit.MILLISECONDS);
		ProcessingUnit rest = admin.getProcessingUnits().waitFor("rest", DEFAULT_RECOVERY_TIME, TimeUnit.MILLISECONDS);
		assertNotNull(rest);
		boolean restUp = rest.waitFor(1, DEFAULT_TEST_TIMEOUT/5, TimeUnit.MILLISECONDS);
		ProcessingUnit webui = admin.getProcessingUnits().waitFor("webui", DEFAULT_RECOVERY_TIME, TimeUnit.MILLISECONDS);
		assertNotNull(webui);
		boolean webuiUp = webui.waitFor(1, DEFAULT_TEST_TIMEOUT/5, TimeUnit.MILLISECONDS);
		ProcessingUnit managementSpace = admin.getProcessingUnits().waitFor(CloudifyConstants.MANAGEMENT_SPACE_NAME, DEFAULT_RECOVERY_TIME, TimeUnit.MILLISECONDS);
		assertNotNull(managementSpace);
		boolean managementSpaceUp = managementSpace.waitFor(1, DEFAULT_TEST_TIMEOUT/5, TimeUnit.MILLISECONDS);
 		
 		assertTrue("esm did not restart" , esmRestarted);
 		assertTrue("gsm did not restart" , gsmUp);
 		assertTrue("lus did not restart" , lusUp);
 		assertTrue("gsa did not restart" , gsaUp);
 		assertTrue("rest did not restart" , restUp);
 		assertTrue("webui did not restart" , webuiUp);
 		assertTrue("management space could not be found", managementSpaceUp);
 	}
 	 
 	private void assertDesiredLogic() throws InterruptedException {
 		LogUtils.log("asserting enviroment reconstracted");
 		assertEnvSanity();
 		
 		LogUtils.log("asserting stockdemo application is still installed");
 		assertStockdemoAppInstalled(cassandraPort1 ,cassandraHostIp, cassandraPort2 ,cassandraHostIp);
 		
 		//TODO: Should I subscribe to the esm added event?
 		ElasticServiceManager esm = admin.getElasticServiceManagers().waitForAtLeastOne(10, TimeUnit.MINUTES);
 		LogUtils.log("asserting esm is managing the application"); 
 		assertEsmIsManagingEnvBySearchingLogs(esm);
 	}
 	
 }
