 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon.failover;
 
 import java.io.IOException;
 import java.util.concurrent.TimeUnit;
 
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import org.cloudifysource.quality.iTests.framework.utils.*;
 import org.cloudifysource.quality.iTests.framework.utils.AssertUtils.RepetitiveConditionProvider;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon.AbstractByonCloudTest;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.byon.ByonCloudService;
 import org.openspaces.admin.gsm.GridServiceManager;
 import org.openspaces.admin.machine.Machine;
 import org.openspaces.admin.pu.ProcessingUnit;
 
 import com.gigaspaces.log.LogEntries;
 import com.gigaspaces.log.LogEntryMatcher;
 import com.gigaspaces.log.LogEntryMatchers;
 
 public abstract class AbstractKillManagementTest extends AbstractByonCloudTest {
 	
 	private static final String GSM_BACKUP_RECOVERED_PU = "Completed recovery of processing units from GSM";
 
 	private int numOManagementMachines = 2;
 
 	private GridServiceManager[] managers;
 	private ProcessingUnit tomcat;
 	private ProcessingUnit mongod;
 	
 	private static final long TEN_SECONDS = 10 * 1000;
 	
 	protected abstract Machine getMachineToKill();
 
 	public void installApplication() throws IOException, InterruptedException {
 		LogUtils.log("installing application petclinic on byon");
 		installApplicationAndWait(ScriptUtils.getBuildPath() + "/recipes/apps/petclinic-simple", "petclinic");
 		admin.getGridServiceManagers().waitFor(2, AbstractTestSupport.OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
 		managers = admin.getGridServiceManagers().getManagers();
 		tomcat = admin.getProcessingUnits().waitFor("petclinic.tomcat", AbstractTestSupport.OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
 		mongod = admin.getProcessingUnits().waitFor("petclinic.tomcat", AbstractTestSupport.OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
 	}
 
 	
 	public void testKillMachine() throws Exception {
 		
 		LogUtils.log("before restart, checking for liveness of petclinic application");
 		repetitiveAssertPetclinicUrlIsAvailable();
 		
 		
 		Machine machine = getMachineToKill();
 		
 		String machineAddress = machine.getHostAddress();
 		
 		GridServiceManager otherManager = getManagerInOtherHostThen(machineAddress);
 		
 		LogUtils.log("restarting machine with ip " + machine.getHostAddress());
 		restartMachineAndWait(machineAddress);
 		LogUtils.log("restart was susccefull");
 		LogUtils.log("waiting for backup GSM to manage the tomcat processing unit");
 		AssertUtils.assertEquals("Wrong managing gsm for tomcat pu", otherManager, ProcessingUnitUtils.waitForManaged(tomcat, otherManager));
 		AssertUtils.assertEquals("Wrong managing gsm for tomcat pu", otherManager, ProcessingUnitUtils.waitForManaged(mongod, otherManager));
 		LogUtils.log("managing gsm of tomcat pu is now " + otherManager);
 		LogUtils.log("after restart, checking for liveness of petclinic application");
 		repetitiveAssertPetclinicUrlIsAvailable();		
 		LogUtils.log("starting management services on machine " + machineAddress);
 		startManagement(machineAddress);
 		
 		AssertUtils.assertTrue("could not find " + numOManagementMachines + " gsm's after failover", 
 				admin.getGridServiceManagers().waitFor(numOManagementMachines, AbstractTestSupport.OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 		AssertUtils.assertTrue("could not find " + numOManagementMachines + " gsm's after failover", 
 				admin.getLookupServices().waitFor(numOManagementMachines, AbstractTestSupport.OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 		
 		AssertUtils.assertTrue("could not find " + numOManagementMachines + " webui instances after failover", admin.getProcessingUnits().getProcessingUnit("webui").waitFor(numOManagementMachines, AbstractTestSupport.OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 		AssertUtils.assertTrue("could not find " + numOManagementMachines + " rest after failover", admin.getProcessingUnits().getProcessingUnit("rest").waitFor(numOManagementMachines, AbstractTestSupport.OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 		AssertUtils.assertTrue("could not find " + numOManagementMachines + " space after failover", admin.getProcessingUnits().getProcessingUnit("cloudifyManagementSpace").waitFor(numOManagementMachines, AbstractTestSupport.OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 		
 		repetitiveAssertBackupGsmRecoveredPu(tomcat); // see CLOUDIFY-1585
 		repetitiveAssertBackupGsmRecoveredPu(mongod); // see CLOUDIFY-1585
 		
 		uninstallApplicationAndWait("petclinic");
 	}
 
 	protected void repetitiveAssertPetclinicUrlIsAvailable() {
 		RepetitiveConditionProvider condition = new RepetitiveConditionProvider() {
 			
 			@Override
 			public boolean getCondition() {
 				String spec = null;
 				try {
 					String hostAddress = tomcat.getInstances()[0].getGridServiceContainer().getMachine().getHostAddress();
 					spec = "http://" + hostAddress + ":8080/petclinic/";
 					LogUtils.log("Checking that url : " + spec + " is available");
 					boolean httpURLAvailable = ServiceUtils.isHttpURLAvailable(spec);
 					LogUtils.log(spec + " available = " + httpURLAvailable);
 					return httpURLAvailable;
 				} catch (final Exception e) {
 					throw new RuntimeException("Error polling to URL : " + spec + " . Reason --> " + e.getMessage());
 				} 
 			}
 		};
 		AssertUtils.repetitiveAssertTrue("petclinic url is not available! waited for 10 seconds", condition, TEN_SECONDS);
 	}
 	
 	 private void repetitiveAssertBackupGsmRecoveredPu(final ProcessingUnit pu) {
 	        final GridServiceManager[] backupGridServiceManagers = pu.getBackupGridServiceManagers();
 	        assertEquals(1,backupGridServiceManagers.length);
 	        LogUtils.log("Waiting for 1 "+ GSM_BACKUP_RECOVERED_PU + " log entry before undeploying PU");
 	        final LogEntryMatcher logMatcher = LogEntryMatchers.containsString(GSM_BACKUP_RECOVERED_PU);
 	        repetitiveAssertTrue("Expected " + GSM_BACKUP_RECOVERED_PU +" log", new RepetitiveConditionProvider() {
 	            
 	            @Override
 	            public boolean getCondition() {
 
 	                final LogEntries logEntries = backupGridServiceManagers[0].logEntries(logMatcher);
 	                final int count = logEntries.logEntries().size();
 	                LogUtils.log("Exepcted at least one "+ GSM_BACKUP_RECOVERED_PU + " log entries. Actual :" + count);
 	                return count > 0;
 	            }
 	        } , OPERATION_TIMEOUT);
 	    }
 
 	/**
 	 * this method accepts a host address and return a GridServiceManager that is not located at the given address.
 	 * @param esmMachineAddress
 	 * @return
 	 */
 	private GridServiceManager getManagerInOtherHostThen(
 			String esmMachineAddress) {
 		
 		GridServiceManager result = null;
 		for (GridServiceManager manager : managers) {
 			if (!manager.getMachine().getHostAddress().equals(esmMachineAddress)) {
 				result = manager;
 				break;
 			}
 		}
 		return result;
 	}
 	
 	protected Machine[] getGridServiceManagerMachines() {
 		GridServiceManager[] griServiceManagers = admin.getGridServiceManagers().getManagers();
 		Machine[] gsmMachines = new Machine[griServiceManagers.length];
 		for (int i = 0 ; i < griServiceManagers.length ; i++) {
 			gsmMachines[i] = griServiceManagers[i].getMachine();
 		}
 		return gsmMachines;
 	}
 
 	//TODO: add support for windows machines (BYON doesn't support windows right now)
 	protected void startManagement(String machine1) throws Exception {
 		
 		for (int i = 0 ; i < 3 ; i++) {
 			try {
 				String managementMachineTemplate = getService().getCloud().getConfiguration().getManagementMachineTemplate();
 				String cloudFile = getService().getCloud().getCloudCompute().getTemplates().get(managementMachineTemplate).getRemoteDirectory() + "/byon-cloud.groovy";
 				LogUtils.log(SSHUtils.runCommand(machine1, AbstractTestSupport.DEFAULT_TEST_TIMEOUT,  ByonCloudService.BYON_HOME_FOLDER + "/gigaspaces/tools/cli/cloudify.sh start-management --verbose -cloud-file "  + cloudFile, ByonCloudService.BYON_CLOUD_USER, ByonCloudService.BYON_CLOUD_PASSWORD));
 				return;
 			} catch (Throwable t) {
 				LogUtils.log("Failed to start management on machine " + machine1 + " restarting machine before attempting again. attempt number " + (i + 1), t);
 				restartMachineAndWait(machine1);
 			}
 		}
 
 	}
 
 	public static void restartMachineAndWait(final String machine) throws Exception {
         DisconnectionUtils.restartMachineAndWait(machine);
 	}
 
 	private static void restartMachine(String toKill) {
         DisconnectionUtils.restartMachine(toKill);
 	}
 }
