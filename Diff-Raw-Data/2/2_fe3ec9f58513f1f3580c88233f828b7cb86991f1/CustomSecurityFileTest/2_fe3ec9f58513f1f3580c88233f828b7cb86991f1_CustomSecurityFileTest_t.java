 package test.cli.cloudify.security;
 
 import java.io.IOException;
 
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.AbstractSecuredLocalCloudTest;
 import framework.tools.SGTestHelper;
 import framework.utils.ApplicationInstaller;
 import framework.utils.LocalCloudBootstrapper;
 
 public class CustomSecurityFileTest extends AbstractSecuredLocalCloudTest{
 
 	private static final String SGTEST_ROOT_DIR = SGTestHelper.getSGTestRootDir().replace('\\', '/');
 	private static final String CLOUD_ADMIN_USER_AND_PASSWORD = "John"; 
 	private static final String VIEWER_USER_AND_PASSWORD = "Amanda"; 
 	private static final String APP_NAME = "simple";
 	private static final String CUSTUM_SECURITY_FILE_PATH = SGTEST_ROOT_DIR + "/src/main/config/security/custom-spring-security.xml";
 	private static final String APP_PATH = SGTEST_ROOT_DIR + "/src/main/resources/apps/USM/usm/applications/" + APP_NAME;
 	
 	private LocalCloudBootstrapper bootstrapper;
 	
 	@BeforeClass
 	public void bootstrap() throws Exception {
 		bootstrapper = new LocalCloudBootstrapper();
 		bootstrapper.secured(true).securityFilePath(CUSTUM_SECURITY_FILE_PATH);
		bootstrapper.keystoreFilePath(SecurityConstants.DEFAULT_KEYSTORE_FILE_PATH).keystorePassword(SecurityConstants.DEFAULT_KEYSTORE_PASSWORD);
 		super.bootstrap(bootstrapper);
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void installWithCustomCloudAdminTest() throws IOException, InterruptedException {
 		
 		ApplicationInstaller appInstaller = new ApplicationInstaller(getRestUrl(), APP_NAME);
 		String output = appInstaller.cloudifyUsername(CLOUD_ADMIN_USER_AND_PASSWORD).cloudifyPassword(CLOUD_ADMIN_USER_AND_PASSWORD).recipePath(APP_PATH).install();
 				
 		appInstaller.assertInstall(output);
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void installWithCustomViewerTest() throws IOException, InterruptedException{
 		
 		ApplicationInstaller appInstaller = new ApplicationInstaller(getRestUrl(), APP_NAME);
 		String output = appInstaller.cloudifyUsername(VIEWER_USER_AND_PASSWORD).cloudifyPassword(VIEWER_USER_AND_PASSWORD).recipePath(APP_PATH).expectToFail(true).install();
 
 		assertTrue("install access granted to a viewer", output.contains("Access is denied") || output.contains("no_permission_access_is_denied"));
 		appInstaller.assertInstall(output);
 	}
 	
 	@AfterMethod(alwaysRun = true)
 	protected void uninstall() throws Exception {
 		uninstallApplicationIfFound(APP_NAME, SecurityConstants.ALL_ROLES_USER_PWD, SecurityConstants.ALL_ROLES_USER_PWD);
 	}
 	
 	@AfterClass(alwaysRun = true)
 	public void teardown() throws IOException, InterruptedException {
 		if (bootstrapper != null) {
 			super.teardown(bootstrapper);
 		}
 	}
 }
