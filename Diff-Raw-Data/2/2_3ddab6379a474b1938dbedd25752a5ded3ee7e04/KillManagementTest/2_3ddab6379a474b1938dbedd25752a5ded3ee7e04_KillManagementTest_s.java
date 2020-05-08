 package test.cli.cloudify.cloud;
 
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.Assert;
 import org.openspaces.admin.AdminFactory;
 import org.openspaces.admin.gsm.GridServiceManager;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.cloud.byon.ByonCloudService;
 import framework.tools.SGTestHelper;
 import framework.utils.AssertUtils;
 import framework.utils.IOUtils;
 import framework.utils.IRepetitiveRunnable;
 import framework.utils.LogUtils;
 import framework.utils.ProcessingUnitUtils;
 import framework.utils.SSHUtils;
 import framework.utils.ScriptUtils;
 import framework.utils.WebUtils;
 
 public class KillManagementTest extends AbstractCloudTest{
 	
 	private URL petClinicUrl;
 	private ByonCloudService service;
 	private String cloudName = "byon";
 	private int numOManagementMachines = 2;
 	final private static String USERNAME = "tgrid";
 	final private static String PASSWORD = "tgrid";
 	private static final String LOOKUPGROUP = "byon_group";
 	private volatile boolean run = true;
 	private ExecutorService threadPool;
 	private String restUrl;
 	private File byonUploadDir = new File(ScriptUtils.getBuildPath() , "tools/cli/plugins/esc/byon/upload");
 	private File backupStartManagementFile = new File(byonUploadDir, "bootstrap-management.backup");
 	private File originialBootstrapManagement = new File(ScriptUtils.getBuildPath() + "/tools/cli/plugins/esc/byon/upload/bootstrap-management.sh");
 	
 	@BeforeMethod(enabled = true)
 	public void before() throws IOException, InterruptedException {
 		
 		// get the default service
 		service = (ByonCloudService) getDefaultService(cloudName);
 		if (service.isBootstrapped()) {
 			service.teardownCloud(); // tear down the existing byon cloud since we need a new bootstrap			
 		}
 		
 		service = new ByonCloudService();
 		service.setNumberOfManagementMachines(numOManagementMachines);
 		service.setMachinePrefix(this.getClass().getName());
 		
 		// replace the default bootstap-management.sh with a multicast version one
 		// first make a backup of the original file
 		FileUtils.copyFile(originialBootstrapManagement, backupStartManagementFile);
 	
 		// copy multicast bootstrap to upload dir as bootstrap-management.sh
 		File bootstrapManagementWithMulticast = new File(SGTestHelper.getSGTestRootDir() + "/apps/cloudify/cloud/byon/bootstrap-management-with-multicast.sh");
 		
 		FileUtils.deleteQuietly(originialBootstrapManagement);
 		FileUtils.copyFile(bootstrapManagementWithMulticast, new File(byonUploadDir, "bootstrap-management.sh"));
 		
 		replaceByonLookupGroup(LOOKUPGROUP);
 		
 		service.bootstrapCloud();
 		
 		if (service.getRestUrls() == null) {
 			Assert.fail("Test failed becuase the cloud was not bootstrapped properly");
 		}
 
 		setService(service);
 		
 		LogUtils.log("creating admin");
 		AdminFactory factory = new AdminFactory();
 		factory.addGroup(LOOKUPGROUP);
 		admin = factory.createAdmin();
 		
 		restUrl = service.getRestUrls()[0];
 		String hostIp = restUrl.substring(0, restUrl.lastIndexOf(':'));
 		petClinicUrl = new URL(hostIp + ":8080/petclinic-mongo/");
 		threadPool = Executors.newFixedThreadPool(1);
 
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void testPetclinic() throws Exception {
 		LogUtils.log("installing application petclinic on " + cloudName);
		installApplicationAndWait(ScriptUtils.getBuildPath() + "/examples/petclinic", "petclinic");
 
 		Future<Void> ping = threadPool.submit(new Callable<Void>(){
 			@Override
 			public Void call() throws Exception {
 				while(run){
 					Assert.assertTrue(WebUtils.isURLAvailable(petClinicUrl));
 					TimeUnit.SECONDS.sleep(10);
 				}
 				return null;
 			}
 		});
 
 		Assert.assertTrue(admin.getGridServiceManagers().waitFor(numOManagementMachines, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 		GridServiceManager[] gsms = admin.getGridServiceManagers().getManagers();
 		ProcessingUnit pu = admin.getProcessingUnits().getProcessingUnit("petclinic.mongod");
 		GridServiceManager gsm1 = pu.waitForManaged();
 		GridServiceManager gsm2 = gsms[0].equals(gsm1) ? gsms[1] : gsms[0];
 		final String machine1 = gsm1.getMachine().getHostAddress();
 
 		restartMachineAndWait(machine1);
 		ProcessingUnitUtils.waitForManaged(pu, gsm2);
 		startManagement(machine1);
 		Assert.assertTrue(admin.getGridServiceManagers().waitFor(numOManagementMachines, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 		gsms = admin.getGridServiceManagers().getManagers();
 		gsm1 = gsms[0].equals(gsm2) ? gsms[1] : gsms[0];
 		ProcessingUnitUtils.waitForBackupGsm(pu, gsm1);
 
 		final String machine2 = gsm2.getMachine().getHostAddress();
 		restartMachineAndWait(machine2);
 
 		ProcessingUnitUtils.waitForManaged(pu, gsm1);
 		startManagement(machine2);
 		Assert.assertTrue(admin.getGridServiceManagers().waitFor(numOManagementMachines, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 		gsms = admin.getGridServiceManagers().getManagers();
 		gsm2 = gsms[0].equals(gsm1) ? gsms[1] : gsms[0];
 		ProcessingUnitUtils.waitForBackupGsm(pu, gsm2);
 
 
 		run = false;
 		ping.get();
 	}
 
 	//TODO: add support for windows machines (BYON doesn't support windows right now)
 	//TODO: get the remote folder from the groovy file
 	private void startManagement(String machine1) {
 		SSHUtils.runCommand(machine1, DEFAULT_TEST_TIMEOUT, "/tmp/gs-files/gigaspaces/tools/cli/cloudify.sh start-management", USERNAME, PASSWORD);
 		
 	}
 
 	private void restartMachineAndWait(final String machine) throws Exception {
 		restartMachine(machine);
 		AssertUtils.assertTrue(WebUtils.waitForHost(machine, (int)OPERATION_TIMEOUT));
 		AssertUtils.repetitive(new IRepetitiveRunnable() {
 			@Override
 			public void run() throws Exception {
 				SSHUtils.validateSSHUp(machine, USERNAME, PASSWORD);
 			}
 		}, (int)OPERATION_TIMEOUT);
 	}
 
 	private void restartMachine(String toKill) {
 		SSHUtils.runCommand(toKill, TimeUnit.SECONDS.toMillis(30),
 				"sudo shutdown now -r", USERNAME, PASSWORD);
 	}
 
 	@AfterMethod(alwaysRun = true)
 	public void teardown() throws IOException {
 		threadPool.shutdownNow();
 		try {
 			service.teardownCloud();
 		}
 		catch (Throwable e) {
 			LogUtils.log("caught an exception while tearing down " + service.getCloudName(), e);
 			sendTeardownCloudFailedMail(cloudName, e);
 		}
 		putService(new ByonCloudService());
 		restoreOriginalBootstrapManagementFile();
 
 
 	}
 
 	private void replaceByonLookupGroup(String group) throws IOException {
 		File originalStartManagementFile = new File(byonUploadDir, "bootstrap-management.sh");
 		
 		String toReplace = "setenv.sh\"\\s\\s\\s";
 		String toAdd = "sed -i \"1i export LOOKUPGROUPS="+ group +"\" setenv.sh || error_exit \\$? \"Failed updating setenv.sh\"\n";
 		IOUtils.replaceTextInFile(originalStartManagementFile.getAbsolutePath(), toReplace, "setenv.sh\"" + "\r\n" + toAdd + "\r\n");
 		
 	}
 
 	
 	
 	private void restoreOriginalBootstrapManagementFile() throws IOException {
 		originialBootstrapManagement.delete();
 		FileUtils.copyFile(backupStartManagementFile, originialBootstrapManagement);
 		
 	}
 
 }
 
 
