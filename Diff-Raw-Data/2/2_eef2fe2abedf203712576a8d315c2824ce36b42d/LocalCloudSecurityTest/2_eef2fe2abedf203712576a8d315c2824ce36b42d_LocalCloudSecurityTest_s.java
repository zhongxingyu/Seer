 package org.cloudifysource.quality.iTests.test.cli.cloudify.security;
 
 import org.cloudifysource.quality.iTests.framework.utils.LocalCloudBootstrapper;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.AbstractSecuredLocalCloudTest;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import java.io.IOException;
 
 public class LocalCloudSecurityTest extends AbstractSecuredLocalCloudTest {
 	
 	LocalCloudBootstrapper bootstrapper;
 
 	@BeforeClass
 	public void bootsrap() throws Exception {
 		super.bootstrap();
 	}
 	
 	
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void installAndUninstalApplWithDifferentUsersTest() throws Exception {
 		
 		// installing the "simple" application as Don, which has roles "appmanager" and "viewer". The auth-groups will be Don's group: "Cellcom".
 		installApplicationAndWait(SIMPLE_APP_PATH, SIMPLE_APP_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, false, null);
 
 		// attempting to uninstall the application as John, which has only the "viewer" role. Should fail!
 		String output = uninstallApplicationAndWait(SIMPLE_APP_PATH, SIMPLE_APP_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.USER_PWD_VIEWER, SecurityConstants.USER_PWD_VIEWER, true, null);
 		AbstractTestSupport.assertTrue("uninstall access granted to " + SecurityConstants.VIEWER_DESCRIPTIN, output.contains(SecurityConstants.ACCESS_DENIED_MESSAGE));
 
 		// attempting to uninstall the application as Dan, which has the "appmanager" role, but is not in group "Cellcom". Should fail!
 		output = uninstallApplicationAndWait(SIMPLE_APP_PATH, SIMPLE_APP_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.USER_PWD_APP_MANAGER, SecurityConstants.USER_PWD_APP_MANAGER, true, null);
 		AbstractTestSupport.assertTrue("Application \"" + SIMPLE_APP_NAME + "\" was wrongfully visible to and uninstalled by " + SecurityConstants.APP_MANAGER_DESCRIPTIN, output.contains(SecurityConstants.RESOURCE_NOT_FOUND));
 		
		// attempting to uninstall the service as Amanda, which has the "cloudadmin" role, and is in group "Cellcom". Should succeed.
 		uninstallApplicationAndWait(SIMPLE_APP_PATH, SIMPLE_APP_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.USER_PWD_CLOUD_ADMIN, SecurityConstants.USER_PWD_CLOUD_ADMIN, false, null);
 	}
 	
 	
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void installAndUninstalServicelWithDifferentUsersTest() throws Exception {
 		
 		// installing the "simple" service as Don, which has roles "appmanager" and "viewer". The auth-groups will be Don's group: "Cellcom".
 		installServiceAndWait(SIMPLE_SERVICE_PATH, SIMPLE_SERVICE_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, false, null);
 		
 		// attempting to uninstall the service as John, which has only the "viewer" role. Should fail!
 		String output = uninstallServiceAndWait(SIMPLE_SERVICE_PATH, SIMPLE_SERVICE_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.USER_PWD_VIEWER, SecurityConstants.USER_PWD_VIEWER, true, null);
 		AbstractTestSupport.assertTrue("uninstall access granted to " + SecurityConstants.VIEWER_DESCRIPTIN, output.contains(SecurityConstants.ACCESS_DENIED_MESSAGE));
 		
 		// attempting to uninstall the service as Dan, which has the "appmanager" role, but is not in group "Cellcom". Should fail!
 		output = uninstallServiceAndWait(SIMPLE_SERVICE_PATH, SIMPLE_SERVICE_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.USER_PWD_APP_MANAGER, SecurityConstants.USER_PWD_APP_MANAGER, true, null);
 		AbstractTestSupport.assertTrue("Service \"" + SIMPLE_APP_NAME + "\" was wrongfully visible to and uninstalled by " + SecurityConstants.APP_MANAGER_DESCRIPTIN, output.contains(SecurityConstants.MISSING_RESOURCE));
 
 		// attempting to uninstall the service as Amanda, which has the "cloudadmin" role, and is in group "Cellcom". Should succeed.
 		uninstallServiceAndWait(SIMPLE_SERVICE_PATH, SIMPLE_SERVICE_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.USER_PWD_CLOUD_ADMIN, SecurityConstants.USER_PWD_CLOUD_ADMIN, false, null);
 	}
 	
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void installingAndViewingAppTest() throws IOException, InterruptedException{
 
 		// installing the "simple" application as Dan, which has the role "appmanager". The auth-groups will be Dan's group: "GE".
 		installApplicationAndWait(SIMPLE_APP_PATH, SIMPLE_APP_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.USER_PWD_APP_MANAGER, SecurityConstants.USER_PWD_APP_MANAGER, false, null);
 		// installing the "groovy" application as Don, which has roles "appmanager" and "viewer". The auth-groups will be Don's group: "Cellcom".
 		installApplicationAndWait(GROOVY_APP_PATH, GROOVY_APP_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, false, null);
 
 		// checks if app "simple" can be viewed by Amanda, which has role "cloudadmin" and groups "Bezeq, GE, Cellcom". Expected - true.
 		verifyApplicationVisibleLists(SecurityConstants.APP_MANAGER_DESCRIPTIN, SecurityConstants.USER_PWD_CLOUD_ADMIN, SecurityConstants.USER_PWD_CLOUD_ADMIN, SecurityConstants.CLOUD_ADMIN_DESCRIPTIN, SIMPLE_APP_NAME, true);
 		// same check for app "groovy". Expected - true.
 		verifyApplicationVisibleLists(SecurityConstants.APP_MANAGER_AND_VIEWER_DESCRIPTIN, SecurityConstants.USER_PWD_CLOUD_ADMIN, SecurityConstants.USER_PWD_CLOUD_ADMIN, SecurityConstants.CLOUD_ADMIN_DESCRIPTIN, GROOVY_APP_NAME, true);
 		
 		// checks if app "simple" can be viewed by Dana, which has roles "cloudadmin" and "appmanager", and group "Bezeq". Expected - false, because Dana is not a member of GE.
 		verifyApplicationVisibleLists(SecurityConstants.APP_MANAGER_DESCRIPTIN, SecurityConstants.USER_PWD_CLOUD_ADMIN_AND_APP_MANAGER, SecurityConstants.USER_PWD_CLOUD_ADMIN_AND_APP_MANAGER, SecurityConstants.CLOUD_ADMIN_AND_APP_MANAGER_DESCRIPTION, SIMPLE_APP_NAME, false);
 		// same check for app "groovy". Expected - false, because Dana is not a member of Cellcom.
 		verifyApplicationVisibleLists(SecurityConstants.APP_MANAGER_AND_VIEWER_DESCRIPTIN, SecurityConstants.USER_PWD_CLOUD_ADMIN_AND_APP_MANAGER, SecurityConstants.USER_PWD_CLOUD_ADMIN_AND_APP_MANAGER, SecurityConstants.CLOUD_ADMIN_AND_APP_MANAGER_DESCRIPTION, GROOVY_APP_NAME, false);
 				
 		// checks if app "simple" can be viewed by Dan, which has the role "appmanager", and group "GE". Expected - true.
 		verifyApplicationVisibleLists(SecurityConstants.APP_MANAGER_DESCRIPTIN, SecurityConstants.USER_PWD_APP_MANAGER, SecurityConstants.USER_PWD_APP_MANAGER, SecurityConstants.APP_MANAGER_DESCRIPTIN, SIMPLE_APP_NAME, true);
 		// same check for app "groovy". Expected - false, because Dan is not a member of Cellcom.
 		verifyApplicationVisibleLists(SecurityConstants.APP_MANAGER_AND_VIEWER_DESCRIPTIN, SecurityConstants.USER_PWD_APP_MANAGER, SecurityConstants.USER_PWD_APP_MANAGER, SecurityConstants.APP_MANAGER_DESCRIPTIN, GROOVY_APP_NAME, false);
 		
 		// checks if app "simple" can be viewed by Don, which has roles "appmanager" and "viewer", and group "Cellcom". Expected - false, because Don is not a member of GE.
 		verifyApplicationVisibleLists(SecurityConstants.APP_MANAGER_DESCRIPTIN, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, SecurityConstants.APP_MANAGER_AND_VIEWER_DESCRIPTIN, SIMPLE_APP_NAME, false);
 		// same check for app "groovy". Expected - true.
 		verifyApplicationVisibleLists(SecurityConstants.APP_MANAGER_AND_VIEWER_DESCRIPTIN, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, SecurityConstants.APP_MANAGER_AND_VIEWER_DESCRIPTIN, GROOVY_APP_NAME, true);
 		
 		// checks if app "simple" can be viewed by John, which has the role "viewer", and groups "GE, Cellcom". Expected - true.
 		verifyApplicationVisibleLists(SecurityConstants.APP_MANAGER_DESCRIPTIN, SecurityConstants.USER_PWD_VIEWER, SecurityConstants.USER_PWD_VIEWER, SecurityConstants.VIEWER_DESCRIPTIN, SIMPLE_APP_NAME, true);
 		// same check for app "groovy". Expected - true.
 		verifyApplicationVisibleLists(SecurityConstants.APP_MANAGER_AND_VIEWER_DESCRIPTIN, SecurityConstants.USER_PWD_VIEWER, SecurityConstants.USER_PWD_VIEWER, SecurityConstants.VIEWER_DESCRIPTIN, GROOVY_APP_NAME, true);
 		
 		// checks if app "simple" can be viewed by Jane, which has no roles, and groups "Bezeq". Expected - false, because Jane does not have any role.
 		verifyApplicationVisibleLists(SecurityConstants.APP_MANAGER_DESCRIPTIN, SecurityConstants.USER_PWD_NO_ROLE, SecurityConstants.USER_PWD_NO_ROLE, SecurityConstants.NO_ROLE_DESCRIPTIN, SIMPLE_APP_NAME, false);
 		// same check for app "groovy". Expected - false, because Jane does not have any role.
 		verifyApplicationVisibleLists(SecurityConstants.APP_MANAGER_AND_VIEWER_DESCRIPTIN, SecurityConstants.USER_PWD_NO_ROLE, SecurityConstants.USER_PWD_NO_ROLE, SecurityConstants.NO_ROLE_DESCRIPTIN, GROOVY_APP_NAME, false);
 
 	}
 	
 	
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void installingAndViewingServiceTest() throws Exception {
 		
 		// installing the "simple" service as Dan, which has the role "appmanager". The auth-groups will be Dan's group: "GE".
 		installServiceAndWait(SIMPLE_SERVICE_PATH, SIMPLE_SERVICE_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.USER_PWD_APP_MANAGER, SecurityConstants.USER_PWD_APP_MANAGER, false, null);
 		
 		// checks if service "simple" can be viewed by Amanda, which has role "cloudadmin" and groups "Bezeq, GE, Cellcom". Expected - true.
 		verifyServiceVisibleLists(SIMPLE_SERVICE_NAME, SecurityConstants.APP_MANAGER_DESCRIPTIN, SecurityConstants.USER_PWD_CLOUD_ADMIN, SecurityConstants.USER_PWD_CLOUD_ADMIN, SecurityConstants.CLOUD_ADMIN_DESCRIPTIN, true);
 		// checks if service "simple" can be viewed by Dana, which has roles "cloudadmin" and "appmanager", and group "Bezeq". Expected - false, because Dana is not a member of GE.
 		verifyServiceVisibleLists(SIMPLE_SERVICE_NAME, SecurityConstants.APP_MANAGER_DESCRIPTIN, SecurityConstants.USER_PWD_CLOUD_ADMIN_AND_APP_MANAGER, SecurityConstants.USER_PWD_CLOUD_ADMIN_AND_APP_MANAGER, SecurityConstants.CLOUD_ADMIN_AND_APP_MANAGER_DESCRIPTION, false);
 		// checks if service "simple" can be viewed by Dan, which has the role "appmanager", and group "GE". Expected - true.
 		verifyServiceVisibleLists(SIMPLE_SERVICE_NAME, SecurityConstants.APP_MANAGER_DESCRIPTIN, SecurityConstants.USER_PWD_APP_MANAGER, SecurityConstants.USER_PWD_APP_MANAGER, SecurityConstants.APP_MANAGER_DESCRIPTIN, true);
 		// checks if service "simple" can be viewed by Don, which has roles "appmanager" and "viewer", and group "Cellcom". Expected - false, because Don is not a member of GE.
 		verifyServiceVisibleLists(SIMPLE_SERVICE_NAME, SecurityConstants.APP_MANAGER_DESCRIPTIN, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, SecurityConstants.APP_MANAGER_AND_VIEWER_DESCRIPTIN, false);
 		// checks if service "simple" can be viewed by John, which has the role "viewer", and groups "GE, Cellcom". Expected - true.
 		verifyServiceVisibleLists(SIMPLE_SERVICE_NAME, SecurityConstants.APP_MANAGER_DESCRIPTIN, SecurityConstants.USER_PWD_VIEWER, SecurityConstants.USER_PWD_VIEWER, SecurityConstants.VIEWER_DESCRIPTIN, true);
 		// checks if service "simple" can be viewed by Jane, which has no roles, and groups "Bezeq". Expected - false, because Jane does not have any role.
 		verifyServiceVisibleLists(SIMPLE_SERVICE_NAME, SecurityConstants.APP_MANAGER_DESCRIPTIN, SecurityConstants.USER_PWD_NO_ROLE, SecurityConstants.USER_PWD_NO_ROLE, SecurityConstants.NO_ROLE_DESCRIPTIN, false);
 		
 		// uninstall service "simple" by Dan
 		uninstallServiceAndWait(SIMPLE_SERVICE_PATH, SIMPLE_SERVICE_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.USER_PWD_APP_MANAGER, SecurityConstants.USER_PWD_APP_MANAGER, false, null);
 		
 		// installing the "groovy" service as Don, which has roles "appmanager" and "viewer". The auth-groups will be Don's group: "Cellcom".
 		installServiceAndWait(GROOVY_SERVICE_PATH, GROOVY_SERVICE_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, false, null);
 		
 		// checks if service "groovy" can be viewed by Amanda, which has role "cloudadmin" and groups "Bezeq, GE, Cellcom". Expected - true.
 		verifyServiceVisibleLists(GROOVY_SERVICE_NAME, SecurityConstants.APP_MANAGER_AND_VIEWER_DESCRIPTIN, SecurityConstants.USER_PWD_CLOUD_ADMIN, SecurityConstants.USER_PWD_CLOUD_ADMIN, SecurityConstants.CLOUD_ADMIN_DESCRIPTIN, true);
 		// checks if service "groovy" can be viewed by Dana, which has roles "cloudadmin" and "appmanager", and group "Bezeq". Expected - false, because Dana is not a member of Cellcom.
 		verifyServiceVisibleLists(GROOVY_SERVICE_NAME, SecurityConstants.APP_MANAGER_AND_VIEWER_DESCRIPTIN, SecurityConstants.USER_PWD_CLOUD_ADMIN_AND_APP_MANAGER, SecurityConstants.USER_PWD_CLOUD_ADMIN_AND_APP_MANAGER, SecurityConstants.CLOUD_ADMIN_AND_APP_MANAGER_DESCRIPTION, false);
 		// checks if service "groovy" can be viewed by Dan, which has the role "appmanager", and group "GE". Expected - false, because Dan is not a member of Cellcom.
 		verifyServiceVisibleLists(GROOVY_SERVICE_NAME, SecurityConstants.APP_MANAGER_AND_VIEWER_DESCRIPTIN, SecurityConstants.USER_PWD_APP_MANAGER, SecurityConstants.USER_PWD_APP_MANAGER, SecurityConstants.APP_MANAGER_DESCRIPTIN, false);
 		// checks if service "groovy" can be viewed by Don, which has roles "appmanager" and "viewer", and group "Cellcom". Expected - true.
 		verifyServiceVisibleLists(GROOVY_SERVICE_NAME, SecurityConstants.APP_MANAGER_AND_VIEWER_DESCRIPTIN, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, SecurityConstants.APP_MANAGER_AND_VIEWER_DESCRIPTIN, true);
 		// checks if service "groovy" can be viewed by John, which has the role "viewer", and groups "GE, Cellcom". Expected - true.
 		verifyServiceVisibleLists(GROOVY_SERVICE_NAME, SecurityConstants.APP_MANAGER_AND_VIEWER_DESCRIPTIN, SecurityConstants.USER_PWD_VIEWER, SecurityConstants.USER_PWD_VIEWER, SecurityConstants.VIEWER_DESCRIPTIN, true);
 		// checks if service "groovy" can be viewed by Jane, which has no roles, and groups "Bezeq". Expected - false, because Jane does not have any role.
 		verifyServiceVisibleLists(GROOVY_SERVICE_NAME, SecurityConstants.APP_MANAGER_AND_VIEWER_DESCRIPTIN, SecurityConstants.USER_PWD_NO_ROLE, SecurityConstants.USER_PWD_NO_ROLE, SecurityConstants.NO_ROLE_DESCRIPTIN, false);
 		
 		// uninstall service "groovy" by Don
 		uninstallServiceAndWait(GROOVY_SERVICE_PATH, GROOVY_SERVICE_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, false, null);
 		
 		// Amanda (cloud admin) installs service simple with specific auth-group: "Bezeq"
 		installServiceAndWait(SIMPLE_SERVICE_PATH, SIMPLE_SERVICE_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.USER_PWD_CLOUD_ADMIN, SecurityConstants.USER_PWD_CLOUD_ADMIN, false, SecurityConstants.BEZEQ_GROUP);
 		
 		// checks if service "simple" can be viewed by Amanda, which has role "cloudadmin" and groups "Bezeq, GE, Cellcom". Expected - true.
 		verifyServiceVisibleLists(SIMPLE_SERVICE_NAME, SecurityConstants.APP_MANAGER_DESCRIPTIN, SecurityConstants.USER_PWD_CLOUD_ADMIN, SecurityConstants.USER_PWD_CLOUD_ADMIN, SecurityConstants.CLOUD_ADMIN_DESCRIPTIN, true);
 		// checks if service "simple" can be viewed by Dana, which has roles "cloudadmin" and "appmanager", and group "Bezeq". Expected - true.
 		verifyServiceVisibleLists(SIMPLE_SERVICE_NAME, SecurityConstants.APP_MANAGER_DESCRIPTIN, SecurityConstants.USER_PWD_CLOUD_ADMIN_AND_APP_MANAGER, SecurityConstants.USER_PWD_CLOUD_ADMIN_AND_APP_MANAGER, SecurityConstants.CLOUD_ADMIN_AND_APP_MANAGER_DESCRIPTION, true);
 		// checks if service "simple" can be viewed by Dan, which has the role "appmanager", and group "GE". Expected - false, because Dan is not a member of Bezeq.
 		verifyServiceVisibleLists(SIMPLE_SERVICE_NAME, SecurityConstants.APP_MANAGER_DESCRIPTIN, SecurityConstants.USER_PWD_APP_MANAGER, SecurityConstants.USER_PWD_APP_MANAGER, SecurityConstants.APP_MANAGER_DESCRIPTIN, false);
 		// checks if service "simple" can be viewed by Don, which has roles "appmanager" and "viewer", and group "Cellcom". Expected - false, because Don is not a member of Bezeq.
 		verifyServiceVisibleLists(SIMPLE_SERVICE_NAME, SecurityConstants.APP_MANAGER_DESCRIPTIN, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, SecurityConstants.APP_MANAGER_AND_VIEWER_DESCRIPTIN, false);
 		// checks if service "simple" can be viewed by John, which has the role "viewer", and groups "GE, Cellcom". Expected - false, because Don is not a member of Bezeq.
 		verifyServiceVisibleLists(SIMPLE_SERVICE_NAME, SecurityConstants.APP_MANAGER_DESCRIPTIN, SecurityConstants.USER_PWD_VIEWER, SecurityConstants.USER_PWD_VIEWER, SecurityConstants.VIEWER_DESCRIPTIN, false);
 		// checks if service "simple" can be viewed by Jane, which has no roles, and groups "Bezeq". Expected - false, because Jane does not have any role.
 		verifyServiceVisibleLists(SIMPLE_SERVICE_NAME, SecurityConstants.APP_MANAGER_DESCRIPTIN, SecurityConstants.USER_PWD_NO_ROLE, SecurityConstants.USER_PWD_NO_ROLE, SecurityConstants.NO_ROLE_DESCRIPTIN, false);
 		
 		uninstallServiceAndWait(SIMPLE_SERVICE_PATH, SIMPLE_SERVICE_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.USER_PWD_CLOUD_ADMIN_AND_APP_MANAGER, SecurityConstants.USER_PWD_CLOUD_ADMIN_AND_APP_MANAGER, false, null);
 	}
 	
 	
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void installAppWithWrongGroup() throws IOException, InterruptedException {
 		
 		String output = "no output";
 		
 		output = installApplicationAndWait(SIMPLE_APP_PATH, SIMPLE_APP_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, true, SecurityConstants.CLOUDADMINS_GROUP);
 		
 		AbstractTestSupport.assertTrue("install-application succeeded with authGroup " + SecurityConstants.CLOUDADMINS_GROUP + " for: " + SecurityConstants.APP_MANAGER_AND_VIEWER_DESCRIPTIN, output.contains(SecurityConstants.ACCESS_DENIED_MESSAGE));
 	}
 	
 	
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void installServiceWithWrongGroup() throws IOException, InterruptedException {
 		
 		String output = "no output";
 		
 		output = installServiceAndWait(SIMPLE_SERVICE_PATH, SIMPLE_SERVICE_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, true, SecurityConstants.CLOUDADMINS_GROUP);
 		
 		AbstractTestSupport.assertTrue("install-service succeeded with authGroup " + SecurityConstants.CLOUDADMINS_GROUP + " for: " + SecurityConstants.APP_MANAGER_AND_VIEWER_DESCRIPTIN, output.contains(SecurityConstants.ACCESS_DENIED_MESSAGE));
 	}
 	
 	
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void installAndUninstallAppWithDifferentGroup() throws Exception {
 		String output = "no output";
 		
 		// Dana (cloudadmin and appmanager) installs application simple with specific auth-group: "Bezeq"
 		installApplicationAndWait(SIMPLE_APP_PATH, SIMPLE_APP_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.USER_PWD_CLOUD_ADMIN_AND_APP_MANAGER, SecurityConstants.USER_PWD_CLOUD_ADMIN_AND_APP_MANAGER, false, SecurityConstants.BEZEQ_GROUP);
 		
 		// Don (appmanger and viewer) uninstalls the service. Expected to fail because Don is not a member of group "Bezeq"
 		output = uninstallApplicationAndWait(SIMPLE_APP_PATH, SIMPLE_APP_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, true, null);
 		AbstractTestSupport.assertTrue("unseen application uninstall succeeded", output.contains(SecurityConstants.RESOURCE_NOT_FOUND));
 	}
 	
 	
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void installAndUninstallServiceWithDifferentGroup() throws Exception {
 		String output = "no output";
 		
 		// Dana (cloudadmin and appmanager) installs service simple with specific auth-group: "Bezeq"
 		installServiceAndWait(SIMPLE_SERVICE_PATH, SIMPLE_SERVICE_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.USER_PWD_CLOUD_ADMIN_AND_APP_MANAGER, SecurityConstants.USER_PWD_CLOUD_ADMIN_AND_APP_MANAGER, false, SecurityConstants.BEZEQ_GROUP);
 		
 		// Don (appmanger and viewer) uninstalls the application. Expected to fail because Don is not a member of group "Bezeq"
 		output = uninstallServiceAndWait(SIMPLE_SERVICE_PATH, SIMPLE_SERVICE_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, SecurityConstants.USER_PWD_APP_MANAGER_AND_VIEWER, true, null);
 		AbstractTestSupport.assertTrue("unseen service uninstall succeeded", output.contains(SecurityConstants.MISSING_RESOURCE));
 	}
 	
 	
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void testInstallAndUninstall() throws Exception {
 		super.installAndUninstallTest();
 	}
 	
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void testInstallWithoutCredentials() throws IOException, InterruptedException{
 		super.installWithoutCredentialsTest();
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void loginTest() throws IOException, InterruptedException {
 		super.testLogin();			
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void connectWithNonexistentUserTest() throws IOException, InterruptedException {
 		super.testConnectWithNonExistingUser();			
 	}
 	
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void connectWithNoPasswordTest() throws IOException, InterruptedException {
 		super.testConnectWithNoPassword();			
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void loginWithNonexistentUserTest() throws IOException, InterruptedException {
 		super.testLoginWithNonexistingUser();			
 	}
 
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void connectWithWrongPassword() throws IOException, InterruptedException {
 		super.testConnectWithWrongPassword();
 
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void loginWithWrongPassword() throws IOException, InterruptedException {
 		super.testLoginWithWrongPassword();
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void securedUseApplicationTest() throws IOException, InterruptedException {
 		super.testSecuredUseApplication();
 	}
 
 	
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void tamperWithSecurityFileTest() throws Exception{
 		super.testTamperWithSecurityFile();			
 	}
 	
 	@AfterMethod(alwaysRun = true)
 	protected void uninstall() throws Exception {
 		uninstallApplicationIfFound(SIMPLE_APP_NAME, SecurityConstants.USER_PWD_ALL_ROLES, SecurityConstants.USER_PWD_ALL_ROLES);
 		uninstallApplicationIfFound(GROOVY_APP_NAME, SecurityConstants.USER_PWD_ALL_ROLES, SecurityConstants.USER_PWD_ALL_ROLES);
 
 		uninstallServiceIfFound(SIMPLE_SERVICE_NAME, SecurityConstants.USER_PWD_ALL_ROLES, SecurityConstants.USER_PWD_ALL_ROLES);
 		uninstallServiceIfFound(GROOVY_SERVICE_NAME, SecurityConstants.USER_PWD_ALL_ROLES, SecurityConstants.USER_PWD_ALL_ROLES);
 		uninstallServiceIfFound(GROOVY2_SERVICE_NAME, SecurityConstants.USER_PWD_ALL_ROLES, SecurityConstants.USER_PWD_ALL_ROLES);
 	}
 	
 	@AfterClass(alwaysRun = true)
 	public void teardown() throws IOException, InterruptedException {
 		super.teardown();
 	}
 		
 }
