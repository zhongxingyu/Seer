 package test.cli.cloudify.cloud.byon.failover;
 
 import java.io.IOException;
 import java.util.concurrent.TimeUnit;
 
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import org.openspaces.admin.gsm.GridServiceManager;
 import org.openspaces.admin.machine.Machine;
 import org.openspaces.admin.pu.ProcessingUnit;
 
 import test.cli.cloudify.cloud.byon.AbstractByonCloudTest;
 import test.cli.cloudify.cloud.services.byon.ByonCloudService;
 import framework.utils.AssertUtils;
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 import framework.utils.IRepetitiveRunnable;
 import framework.utils.LogUtils;
 import framework.utils.ProcessingUnitUtils;
 import framework.utils.SSHUtils;
 import framework.utils.ScriptUtils;
 import framework.utils.WebUtils;
 
 public abstract class AbstractKillManagementTest extends AbstractByonCloudTest {
 
 	private int numOManagementMachines = 2;
 
 	private GridServiceManager[] managers;
 	private ProcessingUnit tomcat;
 	
 	private static final long TEN_SECONDS = 10 * 1000;
 	
 	protected abstract Machine getMachineToKill();
 
 	public void installApplication() throws IOException, InterruptedException {
 		LogUtils.log("installing application petclinic on byon");
 		installApplicationAndWait(ScriptUtils.getBuildPath() + "/recipes/apps/petclinic-simple", "petclinic");
 		admin.getGridServiceManagers().waitFor(2, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
 		managers = admin.getGridServiceManagers().getManagers();
 		tomcat = admin.getProcessingUnits().waitFor("petclinic.tomcat", OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
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
 		ProcessingUnitUtils.waitForManaged(tomcat, otherManager);
 		LogUtils.log("managing gsm of tomcat pu is now " + otherManager);
 		LogUtils.log("after restart, checking for liveness of petclinic application");
 		repetitiveAssertPetclinicUrlIsAvailable();		
 		LogUtils.log("starting management services on machine " + machineAddress);
 		startManagement(machineAddress);
 		
 		AssertUtils.assertTrue("could not find " + numOManagementMachines + " gsm's after failover", 
 				admin.getGridServiceManagers().waitFor(numOManagementMachines, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 		AssertUtils.assertTrue("could not find " + numOManagementMachines + " gsm's after failover", 
 				admin.getLookupServices().waitFor(numOManagementMachines, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 		
 		AssertUtils.assertTrue("could not find " + numOManagementMachines + " webui instances after failover", admin.getProcessingUnits().getProcessingUnit("webui").waitFor(numOManagementMachines, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 		AssertUtils.assertTrue("could not find " + numOManagementMachines + " rest after failover", admin.getProcessingUnits().getProcessingUnit("rest").waitFor(numOManagementMachines, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 		AssertUtils.assertTrue("could not find " + numOManagementMachines + " space after failover", admin.getProcessingUnits().getProcessingUnit("cloudifyManagementSpace").waitFor(numOManagementMachines, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 		
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
 				LogUtils.log(SSHUtils.runCommand(machine1, DEFAULT_TEST_TIMEOUT,  ByonCloudService.BYON_HOME_FOLDER + "/gigaspaces/tools/cli/cloudify.sh start-management --verbose", ByonCloudService.BYON_CLOUD_USER, ByonCloudService.BYON_CLOUD_PASSWORD));
 				return;
 			} catch (Throwable t) {
 				LogUtils.log("Failed to start management on machine " + machine1 + " restarting machine before attempting again. attempt number " + (i + 1), t);
 				restartMachineAndWait(machine1);
 			}
 		}
 
 	}
 
 	protected void restartMachineAndWait(final String machine) throws Exception {
 		restartMachine(machine);
 		Thread.sleep(TEN_SECONDS);
 		AssertUtils.assertTrue(WebUtils.waitForHost(machine, (int)OPERATION_TIMEOUT));
 		AssertUtils.repetitive(new IRepetitiveRunnable() {
 			@Override
 			public void run() throws Exception {
 				SSHUtils.validateSSHUp(machine, ByonCloudService.BYON_CLOUD_USER, ByonCloudService.BYON_CLOUD_PASSWORD);
 			}
 		}, (int)OPERATION_TIMEOUT);
 		Thread.sleep(TEN_SECONDS * 3);
 	}
 
 	private void restartMachine(String toKill) {
 		LogUtils.log(SSHUtils.runCommand(toKill, TimeUnit.SECONDS.toMillis(30),
 				"sudo shutdown now -r", ByonCloudService.BYON_CLOUD_USER, ByonCloudService.BYON_CLOUD_PASSWORD));
 	}
 }
