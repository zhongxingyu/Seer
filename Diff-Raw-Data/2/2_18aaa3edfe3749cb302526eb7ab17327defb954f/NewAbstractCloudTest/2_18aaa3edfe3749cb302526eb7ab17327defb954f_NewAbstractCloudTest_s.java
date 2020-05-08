 package test.cli.cloudify.cloud;
 
 import java.io.IOException;
 
 import org.openspaces.admin.Admin;
 import org.testng.Assert;
 import org.testng.annotations.BeforeMethod;
 
 import test.AbstractTestSupport;
 import test.cli.cloudify.CommandTestUtils;
 import test.cli.cloudify.cloud.services.CloudService;
 import test.cli.cloudify.cloud.services.CloudServiceManager;
 
 import com.gigaspaces.internal.utils.StringUtils;
 
 import framework.utils.ApplicationInstaller;
 import framework.utils.AssertUtils;
 import framework.utils.DumpUtils;
 import framework.utils.LogUtils;
 import framework.utils.ScriptUtils;
 import framework.utils.ServiceInstaller;
 
 public abstract class NewAbstractCloudTest extends AbstractTestSupport {
 
 	private static final int TEN_SECONDS_IN_MILLIS = 10000;
 
 	private static final int MAX_SCAN_RETRY = 3;
 
 	// initialized in bootstrap
 	private String cloudName;
 
 	protected CloudService cloudService;
 
 	protected void customizeCloud() throws Exception {};
 
 	protected void beforeBootstrap() throws Exception {}
 
 	protected void afterBootstrap() throws Exception {} 
 
 	protected void beforeTeardown() throws Exception {}
 
 	protected void afterTeardown() throws Exception {}
 
 
 	/******
 	 * Returns the name of the cloud, as used in the bootstrap-cloud command.
 	 * 
 	 * @return
 	 */
 	protected abstract String getCloudName();
 
 	/********
 	 * Indicates if the cloud used in this test is reusable - which means it may have already been bootstrapped and can
 	 * be reused. Non reusable clouds are bootstrapped at the beginning of the class, and torn down at its end. Reusable
 	 * clouds are torn down when the suite ends.
 	 * 
 	 * @return
 	 */
 	protected abstract boolean isReusableCloud();
 
 	@BeforeMethod
 	public void beforeTest() {	
 		LogUtils.log("Creating test folder");
 	}
 
 	public CloudService getService() {
 
 		return cloudService;
 	}	
 
 	protected void bootstrap() throws Exception {
 		bootstrap(null);
 	}
 
 	protected void bootstrap(CloudService service) throws Exception {
 		if (this.isReusableCloud()) {
 			throw new UnsupportedOperationException(this.getClass().getName() + "Requires reusable clouds, which are not supported yet");
 		}
 
 		if (service == null) { // use the default cloud service if non is specified
 			this.cloudService = CloudServiceManager.getInstance().getCloudService(this.getCloudName());
 		}
 		else {
 			this.cloudService = service; // use the custom service to execute bootstrap and teardown commands
 		}
 
 		this.cloudService.init(this.getClass().getSimpleName().toLowerCase());
 
 		LogUtils.log("Customizing cloud");
 		customizeCloud();
 
 		beforeBootstrap();
		this.cloudService.setMachinePrefix(this.cloudService.getMachinePrefix() + this.getClass().getSimpleName().toLowerCase() + "-");
 		this.cloudService.bootstrapCloud();
 
 		afterBootstrap();
 	}
 
 	protected void teardown() throws Exception {
 
 		beforeTeardown();
 
 		if (this.cloudService == null) {
 			LogUtils.log("No teardown was executed as the cloud instance for this class was not created");
 		} 
 		this.cloudService.teardownCloud();
 		afterTeardown();
 	}
 	
 	protected void teardown(Admin admin) throws Exception {
 
 		beforeTeardown();
 
 		if (this.cloudService == null) {
 			LogUtils.log("No teardown was executed as the cloud instance for this class was not created");
 		} 
 		this.cloudService.teardownCloud(admin);
 		afterTeardown();
 	}
 
 
 	protected void doSanityTest(String applicationFolderName, String applicationName) throws IOException, InterruptedException {
 		LogUtils.log("installing application " + applicationName + " on " + this.cloudName);
 		String applicationPath = ScriptUtils.getBuildPath() + "/recipes/apps/" + applicationFolderName;
 		installApplicationAndWait(applicationPath, applicationName);
 		uninstallApplicationAndWait(applicationName);
 		scanForLeakedAgentNodes();
 	}
 
 	protected void doSanityTest(String applicationFolderName, String applicationName, final int timeout) throws IOException, InterruptedException {
 		LogUtils.log("installing application " + applicationName + " on " + this.cloudName);
 		String applicationPath = ScriptUtils.getBuildPath() + "/recipes/apps/" + applicationFolderName;
 		installApplicationAndWait(applicationPath, applicationName, timeout);
 		uninstallApplicationIfFound(applicationName);
 		scanForLeakedAgentNodes();
 	}
 
 	protected void scanForLeakedAgentNodes() {
 
 		if (cloudService == null) {
 			return;
 		}
 
 		// We will give a short timeout to give the ESM 
 		// time to recognize that he needs to shutdown the machine.
 		try {
 			Thread.sleep(TEN_SECONDS_IN_MILLIS);
 		} catch (InterruptedException e) {
 		}
 
 		boolean leakedAgentScanResult = false;
 		for (int i = 0 ; i < MAX_SCAN_RETRY ; i++) {
 			try {
 				leakedAgentScanResult = this.cloudService.scanLeakedAgentNodes();
 				break;
 			} catch (final Throwable t) {
 				LogUtils.log("Failed scaning for leaked nodes. attempt number " + (i + 1) , t);
 			}
 		}
 		AssertUtils.assertTrue("Leaked nodes were found!", leakedAgentScanResult);
 	}
 
 	protected void installApplicationAndWait(String applicationPath, String applicationName) throws IOException, InterruptedException {
 		ApplicationInstaller applicationInstaller = new ApplicationInstaller(getRestUrl(), applicationName);
 		applicationInstaller.setRecipePath(applicationPath);
 		applicationInstaller.setWaitForFinish(true);
 		applicationInstaller.install();
 	}
 
 	protected void uninstallApplicationAndWait(String applicationName) throws IOException, InterruptedException {
 		ApplicationInstaller applicationInstaller = new ApplicationInstaller(getRestUrl(), applicationName);
 		applicationInstaller.setWaitForFinish(true);
 		applicationInstaller.uninstall();
 	}
 
 	protected void uninstallApplicationIfFound(String applicationName) throws IOException, InterruptedException {
 		ApplicationInstaller applicationInstaller = new ApplicationInstaller(getRestUrl(), applicationName);
 		applicationInstaller.setWaitForFinish(true);
 		applicationInstaller.uninstallIfFound();
 	}
 
 	protected void installApplicationAndWait(String applicationPath, String applicationName, int timeout) throws IOException, InterruptedException {
 		ApplicationInstaller applicationInstaller = new ApplicationInstaller(getRestUrl(), applicationName);
 		applicationInstaller.setRecipePath(applicationPath);
 		applicationInstaller.setWaitForFinish(true);
 		applicationInstaller.setTimeoutInMinutes(timeout);
 		applicationInstaller.install();
 	}
 
 	protected void installApplicationAndWait(String applicationPath, String applicationName, int timeout , boolean expectToFail) throws IOException, InterruptedException {
 		ApplicationInstaller applicationInstaller = new ApplicationInstaller(getRestUrl(), applicationName);
 		applicationInstaller.setRecipePath(applicationPath);
 		applicationInstaller.setWaitForFinish(true);
 		applicationInstaller.setExpectToFail(expectToFail);
 		applicationInstaller.setTimeoutInMinutes(timeout);
 		applicationInstaller.install();		
 	}
 
 
 	protected void installServiceAndWait(String servicePath, String serviceName) throws IOException, InterruptedException {
 		ServiceInstaller serviceInstaller = new ServiceInstaller(getRestUrl(), serviceName);
 		serviceInstaller.setRecipePath(servicePath);
 		serviceInstaller.setWaitForFinish(true);
 		serviceInstaller.install();
 	}
 
 	protected void uninstallServiceAndWait(String serviceName) throws IOException, InterruptedException {
 		ServiceInstaller serviceInstaller = new ServiceInstaller(getRestUrl(), serviceName);
 		serviceInstaller.setWaitForFinish(true);
 		serviceInstaller.uninstall();
 	}
 
 	protected void uninstallServiceIfFound(String serviceName) throws IOException, InterruptedException {
 		ServiceInstaller serviceInstaller = new ServiceInstaller(getRestUrl(), serviceName);
 		serviceInstaller.setWaitForFinish(true);
 		serviceInstaller.uninstallIfFound();
 	}
 
 	protected void installServiceAndWait(String servicePath, String serviceName, int timeout) throws IOException, InterruptedException {
 		ServiceInstaller serviceInstaller = new ServiceInstaller(getRestUrl(), serviceName);
 		serviceInstaller.setRecipePath(servicePath);
 		serviceInstaller.setWaitForFinish(true);
 		serviceInstaller.setTimeoutInMinutes(timeout);
 		serviceInstaller.install();
 	}
 
 	protected void installServiceAndWait(String servicePath, String serviceName, int timeout , boolean expectToFail) throws IOException, InterruptedException {
 		ServiceInstaller serviceInstaller = new ServiceInstaller(getRestUrl(), serviceName);
 		serviceInstaller.setRecipePath(servicePath);
 		serviceInstaller.setWaitForFinish(true);
 		serviceInstaller.setExpectToFail(expectToFail);
 		serviceInstaller.setTimeoutInMinutes(timeout);
 		serviceInstaller.install();
 	}
 
 	protected String getRestUrl() {
 
 		String finalUrl = null;
 
 		String[] restUrls = cloudService.getRestUrls();
 
 		AssertUtils.assertNotNull("No rest URL's found. there was probably a problem with bootstrap", restUrls);
 
 		if (restUrls.length == 1) {
 			finalUrl = restUrls[0];
 		} else {
 			for (String url : restUrls) {
 				String command = "connect " + url;
 				try {
 					LogUtils.log("trying to connect to rest with url " + url);
 					CommandTestUtils.runCommandAndWait(command);
 					finalUrl = url;
 					break;
 				} catch (Throwable e) {
 					LogUtils.log("caught an exception while trying to connect to rest server with url " + url, e);
 				}
 			}			
 		}
 		if (finalUrl == null) {
 			Assert.fail("Failed to find a working rest URL. tried : " + StringUtils.arrayToCommaDelimitedString(restUrls));
 		}
 		return finalUrl;
 	}
 
 	protected String getWebuiUrl() {
 		return cloudService.getWebuiUrls()[0];		
 	}
 
 	protected void dumpMachines() {
 		final String restUrl = getRestUrl();
 		String url = null;
 		try {
 			url = restUrl + "/service/dump/machines/?fileSizeLimit=50000000";
 			DumpUtils.dumpMachines(restUrl);
 		} catch (final Exception e) {
 			LogUtils.log("Failed to create dump for this url - " + url, e);
 		}
 	}	
 }
