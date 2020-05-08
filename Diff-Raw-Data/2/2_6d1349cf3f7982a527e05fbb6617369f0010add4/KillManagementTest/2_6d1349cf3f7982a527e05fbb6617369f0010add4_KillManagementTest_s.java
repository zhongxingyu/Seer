 package test.cli.cloudify.cloud.byon;
 
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import org.cloudifysource.dsl.cloud.Cloud;
 import org.cloudifysource.dsl.internal.DSLException;
 import org.cloudifysource.dsl.internal.ServiceReader;
 import org.junit.Assert;
 import org.openspaces.admin.Admin;
 import org.openspaces.admin.AdminFactory;
 import org.openspaces.admin.gsm.GridServiceManager;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.testng.ITestContext;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.cloud.NewAbstractCloudTest;
 import test.cli.cloudify.cloud.services.byon.ByonCloudService;
 import framework.tools.SGTestHelper;
 import framework.utils.AssertUtils;
 import framework.utils.IRepetitiveRunnable;
 import framework.utils.LogUtils;
 import framework.utils.ProcessingUnitUtils;
 import framework.utils.SSHUtils;
 import framework.utils.ScriptUtils;
 import framework.utils.WebUtils;
 
 public class KillManagementTest extends NewAbstractCloudTest{
 	
 	private static final String MANAGEMENT_PORT = "4170";
 	private URL petClinicUrl;
 	//private ByonCloudService service;
 	private int numOManagementMachines = 2;
 	final private static String USERNAME = "tgrid";
 	final private static String PASSWORD = "tgrid";
 	private static final String TEST_UNIQUE_NAME = "KillManagementTest";
 	private static final String UPLOAD_FOLDER = "upload";
 	private static final String CLOUD_NAME = "byon";
 	private Admin admin;
 	
 	private volatile boolean run = true;
 	private ExecutorService threadPool;
 	private String restUrl;
 	
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap(final ITestContext testContext) {
 		super.bootstrap(testContext);
 	}
 	
 	@AfterClass(alwaysRun = true)
 	protected void teardown() {
 		super.teardown();
 	}
 	
 	@AfterMethod
 	public void cleanUp() {
 		super.scanAgentNodesLeak();
 	}
 	
 	@Override
 	protected void customizeCloud() throws Exception {
 
 		ByonCloudService byonService = (ByonCloudService) cloud;
 		
 		byonService.setNumberOfManagementMachines(numOManagementMachines);
 		byonService.setMachinePrefix(this.getClass().getName());
 
 		// replace the default bootstap-management.sh with a multicast version one
 		File standardBootstrapManagement = new File(byonService.getPathToCloudFolder() + "/" + UPLOAD_FOLDER, "bootstrap-management.sh");
 		File customBootstrapManagement = new File(SGTestHelper.getSGTestRootDir() + "/apps/cloudify/cloud/byon/bootstrap-management-" + byonService.getServiceFolder() + ".sh");
 		Map<File, File> filesToReplace = new HashMap<File, File>();
 		filesToReplace.put(standardBootstrapManagement, customBootstrapManagement);
 		byonService.addFilesToReplace(filesToReplace);
 	}
 
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = false)
 	public void testPetclinic() throws Exception {
 
 		try {
 			
 			restUrl = cloud.getRestUrls()[0];
 			String hostIp = restUrl.substring(0, restUrl.lastIndexOf(':'));
 			
 			//create admin object with a unique group
 			LogUtils.log("creating admin");
 			AdminFactory factory = new AdminFactory();
 			String ipNoHttp = hostIp.substring(7);
 			factory.addLocators(ipNoHttp + ":" + MANAGEMENT_PORT);
 			admin = factory.createAdmin();
 			
 			petClinicUrl = new URL(hostIp + ":8080/petclinic/");
 			threadPool = Executors.newFixedThreadPool(1);
 			
 			LogUtils.log("installing application petclinic on " + CLOUD_NAME);
 			//installApplicationAndWait(ScriptUtils.getBuildPath() + "/recipes/apps/petclinic", "petclinic");
 			installApplicationAndWait(ScriptUtils.getBuildPath() + "/recipes/apps/petclinic-simple", "petclinic");
 
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
 		} finally {
 			//clean
 			if (threadPool != null) {
 				threadPool.shutdownNow();
 			}
 			
 			if (admin != null) {
 				admin.close();
 				admin = null;
 			}
 		}
 		
 	}
 
 	//TODO: add support for windows machines (BYON doesn't support windows right now)
 	private void startManagement(String machine1) throws IOException, DSLException {
 		Cloud readCloud = ServiceReader.readCloud(new File(ScriptUtils.getBuildPath() + "/tools/cli/plugins/esc/byon/byon-cloud.groovy"));
 		SSHUtils.runCommand(machine1, DEFAULT_TEST_TIMEOUT, 
 				readCloud.getTemplates().get(
 						readCloud.getConfiguration().getManagementMachineTemplate()
 						).getRemoteDirectory()
				+ "/gigaspaces/tools/cli/cloudify.sh start-management", USERNAME, PASSWORD);
 		
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
 	
 	@Override
 	protected String getCloudName() {
 		return "byon";
 	}
 	
 	@Override
 	protected boolean isReusableCloud() {
 		return false;
 	}
 
 }
