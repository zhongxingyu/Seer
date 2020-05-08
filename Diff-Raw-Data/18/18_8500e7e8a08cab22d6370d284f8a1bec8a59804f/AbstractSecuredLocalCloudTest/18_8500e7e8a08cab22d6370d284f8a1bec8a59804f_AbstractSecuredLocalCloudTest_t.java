 package test.cli.cloudify;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.openspaces.admin.machine.Machine;
 import org.testng.Assert;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 
 import test.cli.cloudify.CommandTestUtils.ProcessResult;
 import test.cli.cloudify.security.SecurityConstants;
 import framework.tools.SGTestHelper;
 import framework.utils.ApplicationInstaller;
 import framework.utils.LocalCloudBootstrapper;
 import framework.utils.LogUtils;
 import framework.utils.ServiceInstaller;
 
 public class AbstractSecuredLocalCloudTest extends AbstractLocalCloudTest{
 	
 	private static final String BUILD_SECURITY_FILE_PATH = SGTestHelper.getBuildDir().replace('\\', '/') + "/config/security/spring-security.xml";
 	private static final String BUILD_SECURITY_BACKUP_FILE_PATH = SGTestHelper.getBuildDir().replace('\\', '/') + "/config/security/spring-security.xml.backup";
 	private static final String DEFAULT_SECURITY_FILE_PATH = SGTestHelper.getSGTestRootDir().replace('\\', '/') + "/src/main/config/security/spring-security.xml";
 	private static final String DEFAULT_KEYSTORE_FILE_PATH = SGTestHelper.getSGTestRootDir().replace('\\', '/') + "/src/main/config/security/keystore";
 	private static final String DEFAULT_KEYSTORE_PASSWORD = "sgtest";
 	private static final int BOOTSTRAP_RETRIES_BEFOREMETHOD = 1; //TODO remove this
 
 	@BeforeClass
 	public void beforeClass() {
 		LocalCloudBootstrapper bootstrapper = new LocalCloudBootstrapper();
 		bootstrapper.secured(true).securityFilePath(DEFAULT_SECURITY_FILE_PATH);
 		bootstrapper.keystoreFilePath(DEFAULT_KEYSTORE_FILE_PATH).keystorePassword(DEFAULT_KEYSTORE_PASSWORD);
 		
 		// GS-1286 creating a backup for the security xml file. Shouldn't be here.
 		File originalFile = new File(BUILD_SECURITY_FILE_PATH);
 		File backupFile = new File(BUILD_SECURITY_BACKUP_FILE_PATH);
 		
 		try {
 			FileUtils.copyFile(originalFile, backupFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		beforeTest(bootstrapper);		
 	}
 	
 	@Override
 	@BeforeMethod
 	public void beforeTest() {
 		//do nothing
 	}
 	
 	@AfterClass
 	public void afterClass() {
 		
 		File originalFile = new File(BUILD_SECURITY_FILE_PATH);
 		File backupFile = new File(BUILD_SECURITY_BACKUP_FILE_PATH);
 		
 		try {
 			FileUtils.deleteQuietly(originalFile);
 			FileUtils.moveFile(backupFile, originalFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 
 	public static String getDefaultSecurityFilePath() {
 		return DEFAULT_SECURITY_FILE_PATH;
 	}
 
 	public static String getBuildSecurityFilePath() {
 		return BUILD_SECURITY_FILE_PATH;
 	}
 
 	public void beforeTest(LocalCloudBootstrapper bootstrapper) {
 		
 		isSecured = true;
 		
 		//pre-bootstrap actions will be made with the super-user
 		setUserAndPassword(SecurityConstants.ALL_ROLES_USER_PWD, SecurityConstants.ALL_ROLES_USER_PWD);
 
 		LogUtils.log("Test Configuration Started: " + this.getClass());
 
 		if (admin != null) {
 			LogUtils.log("Admin has not been closed properly in the previous test. Closing old admin");
 			admin.close();
 			admin = null;
 		}
 
 		securedRestUrl = "https://" + getLocalHostIpAddress() + ":" + securedRestPort;
 
 		if (checkIsDevEnv()) {
 			LogUtils.log("Local cloud test running in dev mode, will use existing localcloud");
 		} else {
 			for (int i = 0; i < BOOTSTRAP_RETRIES_BEFOREMETHOD; i++) {
 
 				try {
 					
 					//TODO do not always bootstrap
 //					if (!isRequiresBootstrap()) {
 //						break;
 //					}
 
 					cleanUpCloudifyLocalDir();
 
 					LogUtils.log("Tearing-down existing localclouds");
 					final ProcessResult teardownResult = bootstrapper.teardown();
 					if (teardownResult.getExitcode() != 0) {
 						final String output = teardownResult.getOutput();
 						if (!checkOutputForExceptions(output)) {
 							// we assume that if teardown failed but no
 							// exceptions were found in the output
 							// then the reason was because no cloud was found.
 							LogUtils.log("teardown failed because no cloud was found. proceeding with bootstrap.");
 						} else {
 							Assert.fail("Failed to teardown local cloud. output = "
 									+ output);
 						}
 					}
 
 					final ProcessResult bootstrapResult;
 					
 					//switching from the super-user to the entered credentials
 					if(StringUtils.isNotBlank(bootstrapper.getUser()) && StringUtils.isNotBlank(bootstrapper.getPassword())){	
 						setUserAndPassword(bootstrapper.getUser(), bootstrapper.getPassword());
 					}
 					
 					bootstrapResult = bootstrapper.bootstrap();
 
 					LogUtils.log(bootstrapResult.getOutput());
 					Assert.assertEquals(bootstrapResult.getExitcode(), 0,
 							"Bootstrap failed");
 				} catch (final Throwable t) {
 					LogUtils.log("Failed to bootstrap localcloud. iteration="
 							+ i, t);
 
 					if (i >= BOOTSTRAP_RETRIES_BEFOREMETHOD - 1) {
 						Assert.fail("Failed to bootstrap localcloud after "
 								+ BOOTSTRAP_RETRIES_BEFOREMETHOD + " retries.",
 								t);
 					}
 				}
 
 			}
 		}
 
 		Assert.assertFalse(isRequiresBootstrap(),
 				"Cannot establish connection with localcloud");
 
 		this.admin = getAdminWithLocators();
 		final boolean foundLookupService = admin.getLookupServices().waitFor(1,
 				WAIT_FOR_TIMEOUT_SECONDS, TimeUnit.SECONDS);
 		Assert.assertTrue(foundLookupService,
 				"Failed to discover lookup service after "
 						+ WAIT_FOR_TIMEOUT_SECONDS + " seconds");
 
 		final boolean foundMachine = admin.getMachines().waitFor(1,
 				WAIT_FOR_TIMEOUT_SECONDS, TimeUnit.SECONDS);
 		Assert.assertTrue(foundMachine, "Failed to discover machine after "
 				+ WAIT_FOR_TIMEOUT_SECONDS + " seconds");
 		final Machine[] machines = admin.getMachines().getMachines();
 		Assert.assertTrue(machines.length >= 1, "Expected at least one machine");
 		final Machine machine = machines[0];
 		System.out.println("Machine ["
 				+ machine.getHostName()
 				+ "], "
 				+ "TotalPhysicalMem ["
 				+ machine.getOperatingSystem().getDetails()
 				.getTotalPhysicalMemorySizeInGB()
 				+ "GB], "
 				+ "FreePhysicalMem ["
 				+ machine.getOperatingSystem().getStatistics()
 				.getFreePhysicalMemorySizeInGB() + "GB]]");
 
 	}
 
 //	protected Application installApplication(final String applicationName, String user, String password) {
 //
 //		setUserAndPassword(user, password);
 //		return installApplication(applicationName);
 //	}
 //
 //	protected void uninstallApplication(final String applicationName, String user, String password) {
 //
 //		setUserAndPassword(user, password);
 //		uninstallApplication(applicationName);
 //	}
 //
 //	protected void installService(final String serviceName, String user, String password) {
 //
 //		setUserAndPassword(user, password);
 //		installService(serviceName);
 //	}
 //
 //	protected void uninstallService(final String serviceName, String user, String password) {
 //
 //		setUserAndPassword(user, password);
 //		uninstallService(serviceName);
 //	}
 
 	protected String installApplicationAndWait(String applicationPath, String applicationName, int timeout, final String cloudifyUsername,
 			final String cloudifyPassword, boolean isExpectedToFail, final String authGroups) throws IOException, InterruptedException {
 
 		ApplicationInstaller applicationInstaller = new ApplicationInstaller(securedRestUrl, applicationName);
 		applicationInstaller.recipePath(applicationPath);
 		applicationInstaller.waitForFinish(true);
 		applicationInstaller.cloudifyUsername(cloudifyUsername);
 		applicationInstaller.cloudifyPassword(cloudifyPassword);
 		applicationInstaller.expectToFail(isExpectedToFail);
 		if (StringUtils.isNotBlank(authGroups)) {
 			applicationInstaller.authGroups(authGroups);
 		}
 
 		return applicationInstaller.install();
 	}
 
 	protected String uninstallApplicationAndWait(String applicationPath, String applicationName, int timeout, final String cloudifyUsername,
 			final String cloudifyPassword, boolean isExpectedToFail, final String authGroups) throws IOException, InterruptedException {
 
 		ApplicationInstaller applicationInstaller = new ApplicationInstaller(securedRestUrl, applicationName);
 		applicationInstaller.recipePath(applicationPath);
 		applicationInstaller.waitForFinish(true);
 		applicationInstaller.cloudifyUsername(cloudifyUsername);
 		applicationInstaller.cloudifyPassword(cloudifyPassword);
 		applicationInstaller.expectToFail(isExpectedToFail);
 		if (StringUtils.isNotBlank(authGroups)) {
 			applicationInstaller.authGroups(authGroups);
 		}
 
 		return applicationInstaller.uninstall();
 	}
 	
	protected void uninstallApplicationIfFound(String applicationName, final String cloudifyUsername, final String cloudifyPassword) throws IOException, InterruptedException {
		ApplicationInstaller applicationInstaller = new ApplicationInstaller(securedRestUrl, applicationName);
		applicationInstaller.waitForFinish(true);
		applicationInstaller.cloudifyUsername(cloudifyUsername);
		applicationInstaller.cloudifyPassword(cloudifyPassword);
		applicationInstaller.uninstallIfFound();
	}
	
 	protected String installServiceAndWait(String servicePath, String serviceName, int timeout, final String cloudifyUsername,
 			final String cloudifyPassword, boolean isExpectedToFail, final String authGroups) throws IOException, InterruptedException {
 		
 		ServiceInstaller serviceInstaller = new ServiceInstaller(securedRestUrl, serviceName);
 		serviceInstaller.recipePath(servicePath);
 		serviceInstaller.waitForFinish(true);
 		serviceInstaller.cloudifyUsername(cloudifyUsername);
 		serviceInstaller.cloudifyPassword(cloudifyPassword);
 		serviceInstaller.expectToFail(isExpectedToFail);
 		if (StringUtils.isNotBlank(authGroups)) {
 			serviceInstaller.authGroups(authGroups);
 		}
 		
 		return serviceInstaller.install();
 	}
 	
 	protected String uninstallServiceAndWait(String servicePath, String serviceName, int timeout, final String cloudifyUsername,
 			final String cloudifyPassword, boolean isExpectedToFail, final String authGroups) throws IOException, InterruptedException {
 		
 		ServiceInstaller serviceInstaller = new ServiceInstaller(securedRestUrl, serviceName);
 		serviceInstaller.recipePath(servicePath);
 		serviceInstaller.waitForFinish(true);
 		serviceInstaller.cloudifyUsername(cloudifyUsername);
 		serviceInstaller.cloudifyPassword(cloudifyPassword);
 		serviceInstaller.expectToFail(isExpectedToFail);
 		if (StringUtils.isNotBlank(authGroups)) {
 			serviceInstaller.authGroups(authGroups);
 		}
 		
 		return serviceInstaller.uninstall();
 	}
 	
 	protected String listApplications(String user, String password){
 
 		setUserAndPassword(user, password);
 		return listApplications();
 	}
 	
 	protected String listInstances(String user, String password, String serviceName){
 		
 		setUserAndPassword(user, password);
 		return listInstances(serviceName);
 	}
 	
 	protected String listServices(String user, String password){
 		
 		setUserAndPassword(user, password);
 		return listServices();
 	}
 
 	protected String connect(String user, String password){
 
 		setUserAndPassword(user, password);
 		return connect();
 	}
 
 	protected String login(String user, String password, boolean failCommand){
 
 		setUserAndPassword(SecurityConstants.ALL_ROLES_USER_PWD, SecurityConstants.ALL_ROLES_USER_PWD);
 
 		String output = "no output";
 
 		try {
 			output = CommandTestUtils.runCommand(connectCommand() + ";" + loginCommand(user, password), true, failCommand);
 		} catch (IOException e) {
 			Assert.fail("Failed to connect and login");
 		} catch (InterruptedException e) {
 			Assert.fail("Failed to connect and login");
 		}
 
 		return output;
 	}
 
 	protected String loginCommand(String user, String password){		
 		return ("login " + user + " " + password);
 	}
 
 	protected void setUserAndPassword(String user, String password) {
 		this.user = user;
 		this.password = password;
 	}
 
 	public static String getDefaultKeystoreFilePath() {
 		return DEFAULT_KEYSTORE_FILE_PATH;
 	}
 
 	public static String getDefaultKeystorePassword() {
 		return DEFAULT_KEYSTORE_PASSWORD;
 	}
 
 }
