 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon.agentrestart;
 
 import iTests.framework.utils.LogUtils;
 
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 
 import org.openspaces.admin.gsa.GridServiceAgent;
 import org.openspaces.admin.gsa.events.GridServiceAgentAddedEventListener;
 import org.openspaces.admin.gsa.events.GridServiceAgentLifecycleEventListener;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 /**
  * 
  * @author adaml
  *
  */
 public class AgentMaintenanceTimeoutExceededTest extends AbstractAgentMaintenanceModeTest {
 
 	private static final long FIVE_SECONDS_MILLIS = 5000;
 
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap() throws Exception {
 		super.bootstrap();
 	}
 	
 	/**
 	 * set
 	 * @throws Exception
 	 */
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = true)
     public void testAgentRestartWithMaintenanceTimeoutExceeded() throws Exception {
     	installServiceAndWait(getServicePath(SERVICE_NAME), SERVICE_NAME);
     	
     	//set maintenance mode for 1 second using the service context.
     	LogUtils.log("Setting maintenance mode for a very short period");
     	startMaintenanceMode(1);
     	LogUtils.log("Waiting for maintenance mode to expire");
     	Thread.sleep(FIVE_SECONDS_MILLIS);
     	LogUtils.log("maintenance mode expired");
     	
 		final CountDownLatch removed = new CountDownLatch(1);
 		final CountDownLatch added = new CountDownLatch(2);
 		
         final GridServiceAgentAddedEventListener agentListener = new GridServiceAgentLifecycleEventListener() {
 			
 			@Override
 			public void gridServiceAgentRemoved(GridServiceAgent gridServiceAgent) {
 				LogUtils.log("agent removed event has been fired");
 				removed.countDown();
 			}
 			
 			@Override
 			public void gridServiceAgentAdded(GridServiceAgent gridServiceAgent) {
 				LogUtils.log("agent added event has been fired");
 				added.countDown();
 			}
 		};
 				
 		admin.addEventListener(agentListener);
     	
 		// Shutdown agent. This machine should not start again.
		gracefullyShutdownAgent(absolutePuName);
 		
     	LogUtils.log("Waiting for esm to recover from agent shutdown.");
     	// The recovery should be as normal meaning a new machine should be allocated.
 		assertTrue("agent machine did not stop as expected.", removed.await(DEFAULT_WAIT_MINUTES, TimeUnit.MINUTES));
 		assertTrue("agent machine was not added as expected. esm did not start a machine",added.await(DEFAULT_WAIT_MINUTES, TimeUnit.MINUTES));
 		
 		LogUtils.log("ESM has recovered successfully. uninstalling service " + SERVICE_NAME);
 		uninstallServiceAndWait(SERVICE_NAME);
     }
     
 	@Override
 	@AfterClass(alwaysRun = true)
 	protected void teardown() throws Exception {
 		uninstallServiceIfFound(SERVICE_NAME);
 		super.teardown();
 	}
 }
