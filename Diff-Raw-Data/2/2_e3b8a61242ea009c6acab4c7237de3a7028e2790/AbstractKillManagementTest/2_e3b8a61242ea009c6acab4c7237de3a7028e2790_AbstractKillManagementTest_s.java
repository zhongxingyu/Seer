 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon.failover;
 
 import java.io.IOException;
 import java.util.concurrent.TimeUnit;
 
 import iTests.framework.utils.*;
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import org.cloudifysource.quality.iTests.framework.utils.*;
 import iTests.framework.utils.AssertUtils.RepetitiveConditionProvider;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon.AbstractByonCloudTest;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.CloudService;
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
 		
 		LogUtils.log("Before restart, checking for liveness of petclinic application");
 
 		repetitiveAssertPetclinicUrlIsAvailable();
 		
 		Machine machine = getMachineToKill();
 		
 		String machineAddress = machine.getHostAddress();
 		
 		GridServiceManager otherManager = getManagerInOtherHostThen(machineAddress);
 		
 		LogUtils.log("Restarting machine with ip " + machine.getHostAddress());
 		restartMachineAndWait(machineAddress, getService());
		LogUtils.log("Restart was successfull");
 		LogUtils.log("waiting for backup GSM to manage the tomcat processing unit");
 		AssertUtils.assertEquals("Wrong managing gsm for tomcat pu", otherManager,
                 ProcessingUnitUtils.waitForManaged(tomcat, otherManager));
 		AssertUtils.assertEquals("Wrong managing gsm for mongod pu", otherManager,
                 ProcessingUnitUtils.waitForManaged(mongod, otherManager));
 		LogUtils.log("Managing gsm of tomcat pu is now " + otherManager);
 		LogUtils.log("After restart, checking for liveness of petclinic application");
 		repetitiveAssertPetclinicUrlIsAvailable();
         LogUtils.log("Petclinic application is alive.");
 
         AssertUtils.assertTrue("Could not find " + numOManagementMachines + " lus's after failover",
                 admin.getLookupServices().waitFor(numOManagementMachines, AbstractTestSupport.OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
         AssertUtils.assertTrue("Could not find " + numOManagementMachines + " gsm's after failover",
 				admin.getGridServiceManagers().waitFor(numOManagementMachines, AbstractTestSupport.OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 
 		AssertUtils.assertTrue("Could not find " + numOManagementMachines + " webui instances after failover",
                 admin.getProcessingUnits().getProcessingUnit("webui").waitFor(numOManagementMachines, AbstractTestSupport.OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 		AssertUtils.assertTrue("Could not find " + numOManagementMachines + " rest after failover",
                 admin.getProcessingUnits().getProcessingUnit("rest").waitFor(numOManagementMachines, AbstractTestSupport.OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 		AssertUtils.assertTrue("Could not find " + numOManagementMachines + " space after failover",
                 admin.getProcessingUnits().getProcessingUnit("cloudifyManagementSpace").waitFor(numOManagementMachines, AbstractTestSupport.OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 		
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
                     return ServiceUtils.isHttpURLAvailable(spec);
 				} catch (final Exception e) {
 					throw new RuntimeException("Error polling to URL : " + spec + " . Reason --> " + e.getMessage());
 				} 
 			}
 		};
 		AssertUtils.repetitiveAssertTrue("petclinic url is not available! waited for 5 minutes", condition,
                 OPERATION_TIMEOUT);
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
 	                LogUtils.log("Expected at least one "+ GSM_BACKUP_RECOVERED_PU + " log entries. Actual :" +
                             count);
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
 				SSHUtils.runCommand(machine1, AbstractTestSupport.DEFAULT_TEST_TIMEOUT, ByonCloudService.BYON_HOME_FOLDER + "/gigaspaces/tools/cli/cloudify.sh start-management --verbose -timeout 10 -cloud-file " + cloudFile, getService().getUser(), getService().getApiKey());
 				return;
 			} catch (Throwable t) {
 				LogUtils.log("Failed to start management on machine " + machine1 + " restarting machine before attempting again. attempt number " + (i + 1), t);
 				restartMachineAndWait(machine1, getService());
 			}
 		}
         AssertUtils.assertFail("Failed starting management on host " + machine1);
 
 	}
 
 	public void restartMachineAndWait(final String machine, final CloudService service) throws Exception {
         DisconnectionUtils.restartMachineAndWait(machine, service);
 	}
 
 }
