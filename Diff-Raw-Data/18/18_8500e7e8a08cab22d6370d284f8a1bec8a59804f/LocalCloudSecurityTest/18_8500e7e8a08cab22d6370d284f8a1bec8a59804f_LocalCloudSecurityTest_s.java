 package test.cli.cloudify;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.io.FileUtils;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.security.SecurityConstants;
 import framework.tools.SGTestHelper;
 
 public class LocalCloudSecurityTest extends AbstractSecuredLocalCloudTest{
 
 	private static final String SIMPLE_APP_NAME = "simple";
 	private static final String SIMPLE_APP_PATH = "\\src\\main\\resources\\apps\\USM\\usm\\applications\\" + SIMPLE_APP_NAME;
 	private static final String SIMPLE_SERVICE_NAME = "simple";
 	private static final String SIMPLE_SERVICE_PATH = "\\src\\main\\resources\\apps\\USM\\usm\\services\\" + SIMPLE_SERVICE_NAME;
 
 	
 	private static final String TRAVEL_APP_NAME = "travelExtended";
 	private static final String TRAVEL_APP_PATH = "\\src\\main\\resources\\apps\\USM\\usm\\applications\\" + TRAVEL_APP_NAME;
 	private static final String TOMCAT_SERVICE_NAME = "tomcat-extend";
 	private static final String CASSANDRA_SERVICE_NAME = "cassandra-extend";
 	
 	private static final String INSTANCE_VERIFICATION_STRING = "instance #1";
 	private static final int TIMEOUT_IN_MINUTES = 30;
 
 	@AfterMethod(alwaysRun = true)
 	protected void uninstall() throws Exception {
 
		setUserAndPassword(SecurityConstants.ALL_ROLES_USER_PWD, SecurityConstants.ALL_ROLES_USER_PWD);
		uninstallApplicationIfFound(SIMPLE_APP_NAME);
		uninstallApplicationIfFound(TRAVEL_APP_NAME);	
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void installAndUninstallTest() throws IOException, InterruptedException {
 
 		installAndUninstall(SecurityConstants.APP_MANAGER_AND_VIEWER_USER_PWD, SecurityConstants.APP_MANAGER_AND_VIEWER_USER_PWD, false);
 		installAndUninstall(SecurityConstants.APP_MANAGER_USER_PWD, SecurityConstants.APP_MANAGER_USER_PWD, false);
 		installAndUninstall(SecurityConstants.CLOUD_ADMIN_AND_APP_MANAGER_USER_PWD, SecurityConstants.CLOUD_ADMIN_AND_APP_MANAGER_USER_PWD, false);
 		installAndUninstall(SecurityConstants.CLOUD_ADMIN_USER_PWD, SecurityConstants.CLOUD_ADMIN_USER_PWD, false);
 		installAndUninstall(SecurityConstants.VIEWER_USER_PWD, SecurityConstants.VIEWER_USER_PWD, true);
 		installAndUninstall(SecurityConstants.NO_ROLE_USER_PWD, SecurityConstants.NO_ROLE_USER_PWD, true);
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void installAndUninstallWithDifferentUsersTest() throws IOException, InterruptedException {
 		
 		installApplicationAndWait(SIMPLE_APP_PATH, SIMPLE_APP_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.APP_MANAGER_AND_VIEWER_USER_PWD, SecurityConstants.APP_MANAGER_AND_VIEWER_USER_PWD, false, null);
 
 		String output = uninstallApplicationAndWait(SIMPLE_APP_PATH, SIMPLE_APP_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.VIEWER_USER_PWD, SecurityConstants.VIEWER_USER_PWD, true, null);
 		assertTrue("install access granted to " + SecurityConstants.VIEWER_USER_PWD, output.contains("Access is denied"));
 
 		uninstallApplicationAndWait(SIMPLE_APP_PATH, SIMPLE_APP_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.APP_MANAGER_USER_PWD, SecurityConstants.APP_MANAGER_USER_PWD, false, null);
 	
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void installWithoutCredentialsTest() throws IOException, InterruptedException{
 
 		String output = installApplicationAndWait(SIMPLE_APP_PATH, SIMPLE_APP_NAME, TIMEOUT_IN_MINUTES, null, null, true, null);
 
 		assertTrue("install access granted to an Anonymous user" , output.contains("bad_credentials"));
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void installingAndViewingTest() throws IOException, InterruptedException{
 
 		installApplicationAndWait(SIMPLE_APP_PATH, SIMPLE_APP_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.APP_MANAGER_USER_PWD, SecurityConstants.APP_MANAGER_USER_PWD, false, null);
 		installApplicationAndWait(TRAVEL_APP_PATH, TRAVEL_APP_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.APP_MANAGER_AND_VIEWER_USER_PWD, SecurityConstants.APP_MANAGER_AND_VIEWER_USER_PWD, false, null);
 
 		verifyVisibleLists(SecurityConstants.APP_MANAGER_USER_PWD, SecurityConstants.CLOUD_ADMIN_USER_PWD, SecurityConstants.CLOUD_ADMIN_USER_PWD, SIMPLE_APP_NAME, true);
 		verifyVisibleLists(SecurityConstants.APP_MANAGER_AND_VIEWER_USER_PWD, SecurityConstants.CLOUD_ADMIN_USER_PWD, SecurityConstants.CLOUD_ADMIN_USER_PWD, TRAVEL_APP_NAME, true);
 		
 		verifyVisibleLists(SecurityConstants.APP_MANAGER_USER_PWD, SecurityConstants.CLOUD_ADMIN_AND_APP_MANAGER_USER_PWD, SecurityConstants.CLOUD_ADMIN_AND_APP_MANAGER_USER_PWD, SIMPLE_APP_NAME, false);
 		verifyVisibleLists(SecurityConstants.APP_MANAGER_AND_VIEWER_USER_PWD, SecurityConstants.CLOUD_ADMIN_AND_APP_MANAGER_USER_PWD, SecurityConstants.CLOUD_ADMIN_AND_APP_MANAGER_USER_PWD, TRAVEL_APP_NAME, false);
 		
 		verifyVisibleLists(SecurityConstants.APP_MANAGER_USER_PWD, SecurityConstants.APP_MANAGER_USER_PWD, SecurityConstants.APP_MANAGER_USER_PWD, SIMPLE_APP_NAME, true);
 		verifyVisibleLists(SecurityConstants.APP_MANAGER_AND_VIEWER_USER_PWD, SecurityConstants.APP_MANAGER_USER_PWD, SecurityConstants.APP_MANAGER_USER_PWD, TRAVEL_APP_NAME, false);
 		
 		verifyVisibleLists(SecurityConstants.APP_MANAGER_USER_PWD, SecurityConstants.APP_MANAGER_AND_VIEWER_USER_PWD, SecurityConstants.APP_MANAGER_AND_VIEWER_USER_PWD, SIMPLE_APP_NAME, false);
 		verifyVisibleLists(SecurityConstants.APP_MANAGER_AND_VIEWER_USER_PWD, SecurityConstants.APP_MANAGER_AND_VIEWER_USER_PWD, SecurityConstants.APP_MANAGER_AND_VIEWER_USER_PWD, TRAVEL_APP_NAME, true);
 		
 		verifyVisibleLists(SecurityConstants.APP_MANAGER_USER_PWD, SecurityConstants.VIEWER_USER_PWD, SecurityConstants.VIEWER_USER_PWD, SIMPLE_APP_NAME, true);
 		verifyVisibleLists(SecurityConstants.APP_MANAGER_AND_VIEWER_USER_PWD, SecurityConstants.VIEWER_USER_PWD, SecurityConstants.VIEWER_USER_PWD, TRAVEL_APP_NAME, true);
 		
 		verifyVisibleLists(SecurityConstants.APP_MANAGER_USER_PWD, SecurityConstants.NO_ROLE_USER_PWD, SecurityConstants.NO_ROLE_USER_PWD, SIMPLE_APP_NAME, false);
 		verifyVisibleLists(SecurityConstants.APP_MANAGER_AND_VIEWER_USER_PWD, SecurityConstants.NO_ROLE_USER_PWD, SecurityConstants.NO_ROLE_USER_PWD, TRAVEL_APP_NAME, false);
 
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void loginTest() throws IOException, InterruptedException{
 
 		String output = "no output";
 		
 		output = login(SecurityConstants.VIEWER_USER_PWD, SecurityConstants.VIEWER_USER_PWD, false);		
 		assertTrue("login failed for user: " + SecurityConstants.VIEWER_USER_PWD, output.contains("Logged in successfully"));			
 
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void connectWithNonexistentUserTest() throws IOException, InterruptedException{
 
 		String output = connect(SecurityConstants.CLOUD_ADMIN_USER_PWD + "bad", SecurityConstants.CLOUD_ADMIN_USER_PWD);		
 		assertTrue("connect succeeded for user: " + SecurityConstants.CLOUD_ADMIN_USER_PWD + "bad", output.contains("bad credentials"));			
 
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void connectWithNoPasswordTest() throws IOException, InterruptedException{
 		
 		String output = connect(SecurityConstants.CLOUD_ADMIN_USER_PWD, null);		
 		assertTrue("connect succeeded for user: " + SecurityConstants.CLOUD_ADMIN_USER_PWD + " without providing a password", output.contains("password is required"));			
 		
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void loginWithNonexistentUserTest() throws IOException, InterruptedException{
 
 		String output = "no output";
 		
 		output = login(SecurityConstants.CLOUD_ADMIN_USER_PWD + "bad", SecurityConstants.CLOUD_ADMIN_USER_PWD, true);					
 
 		assertTrue("login succeeded for user: " + SecurityConstants.CLOUD_ADMIN_USER_PWD + "bad", output.contains("Bad credentials"));			
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void connectWithWrongPassword() {
 
 		String output = connect(SecurityConstants.CLOUD_ADMIN_USER_PWD, SecurityConstants.CLOUD_ADMIN_USER_PWD + "bad");		
 		assertTrue("connect succeeded for password: " + SecurityConstants.CLOUD_ADMIN_USER_PWD + "bad", output.contains("Access denied"));			
 
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void loginWithWrongPassword() throws IOException, InterruptedException {
 
 		String output = "no output";
 		
 		output = login(SecurityConstants.CLOUD_ADMIN_USER_PWD, SecurityConstants.CLOUD_ADMIN_USER_PWD + "bad", true);
 		
 		assertTrue("login succeeded for password: " + SecurityConstants.CLOUD_ADMIN_USER_PWD + "bad", output.contains("Bad credentials"));
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void TamperWithSecurityFileTest() throws IOException, InterruptedException {
 
 		String fakeCloudAdminUserAndPassword = "John";
 
 		String originalFilePath = getBuildSecurityFilePath();
 		String backupFilePath = originalFilePath + ".backup";
 		String fakeFilePath = SGTestHelper.getSGTestRootDir() + "\\src\\main\\config\\security\\fake-spring-security.xml";
 		File originalFile = new File(originalFilePath);
 		File backupFile = new File(backupFilePath);
 		File fakeFile = new File(fakeFilePath);
 
 		FileUtils.moveFile(originalFile, backupFile);
 		FileUtils.copyFile(fakeFile, originalFile);
 
 		String output = installApplicationAndWait(SIMPLE_APP_PATH, SIMPLE_APP_NAME, TIMEOUT_IN_MINUTES, fakeCloudAdminUserAndPassword, fakeCloudAdminUserAndPassword, true, null);
 
 		FileUtils.deleteQuietly(originalFile);
 		FileUtils.moveFile(backupFile, originalFile);
 		
 		assertTrue("install access granted to " + fakeCloudAdminUserAndPassword, output.contains("Access is denied"));
 			
 	}
 
 	private void verifyVisibleLists(String installer, String viewerName, String viewerPassword, String appName, boolean isVisible) {
 		
 		String output = "no output";
 		
 		output = listApplications(viewerName, viewerPassword);
 		
 		if(isVisible){
 			assertTrue(viewerName + " doesn't see the application of " + installer, output.contains(appName));
 		}
 		else{			
 			assertTrue(viewerName + " sees the application of " + installer, !output.contains(appName));
 		}
 		
 		output = listServices(viewerName, viewerPassword);
 		
 		if(isVisible){
 			if(appName.equalsIgnoreCase(SIMPLE_APP_NAME)){					
 				assertTrue(viewerName + " doesn't see the services of " + installer, output.contains(SIMPLE_SERVICE_NAME));
 			}
 			else{
 				assertTrue(viewerName + " doesn't see the services of " + installer, output.contains(TOMCAT_SERVICE_NAME) && output.contains(CASSANDRA_SERVICE_NAME));
 				
 			}
 				
 		}
 		else{	
 			if(appName.equalsIgnoreCase(SIMPLE_APP_NAME)){	
 				assertTrue(viewerName + " sees the services of " + installer, !output.contains(SIMPLE_SERVICE_NAME));			
 			}
 			else{
 				assertTrue(viewerName + " sees the services of " + installer, !(output.contains(TOMCAT_SERVICE_NAME) || output.contains(CASSANDRA_SERVICE_NAME)));							
 			}
 		}
 		
 		
 		if(appName.equalsIgnoreCase(SIMPLE_APP_NAME)){
 			
 			output = listInstances(viewerName, viewerPassword, SIMPLE_APP_NAME + "." + SIMPLE_SERVICE_NAME);
 			
 			if(isVisible){	
 				
 				assertTrue(viewerName + " doesn't see the instances of " + installer, output.contains(INSTANCE_VERIFICATION_STRING));
 			}
 			else{
 				assertTrue(viewerName + " sees the services of " + installer, !output.contains(INSTANCE_VERIFICATION_STRING));
 				
 			}
 			
 		}
 		else{	
 			
 			output = listInstances(viewerName, viewerPassword, TRAVEL_APP_NAME + "." + TOMCAT_SERVICE_NAME);
 
 			if(isVisible){	
 				assertTrue(viewerName + " doesn't see the instances of " + installer, output.contains(INSTANCE_VERIFICATION_STRING));			
 			}
 			else{
 				assertTrue(viewerName + " sees the services of " + installer, !output.contains(INSTANCE_VERIFICATION_STRING));							
 			}
 		}
 	}
 	
 	public void installAndUninstall(String user, String password, boolean isInstallExpectedToFail) throws IOException, InterruptedException{
 		
 		String output = installApplicationAndWait(SIMPLE_APP_PATH, SIMPLE_APP_NAME, TIMEOUT_IN_MINUTES, user, password, isInstallExpectedToFail, null);
 		
 		if(isInstallExpectedToFail){
 			assertTrue("application installation access granted to " + user, output.contains("Access is denied"));
 		}
 		
 		if(output.contains("Application " + SIMPLE_APP_NAME + " installed successfully")){			
 			uninstallApplicationAndWait(SIMPLE_APP_PATH, SIMPLE_APP_NAME, TIMEOUT_IN_MINUTES, user, password, isInstallExpectedToFail, null);
 		}
 		
 		listApplications(user, password);
 		
 		output = installServiceAndWait(SIMPLE_SERVICE_PATH, SIMPLE_SERVICE_NAME, TIMEOUT_IN_MINUTES, user, password, isInstallExpectedToFail, null);
 		
 		if(isInstallExpectedToFail){
 			assertTrue("service installation access granted to " + user, output.contains("Access is denied"));
 		}
 		
 		if(output.contains("Service \"" + SIMPLE_SERVICE_NAME + "\" successfully installed")){			
 			uninstallServiceAndWait(SIMPLE_APP_PATH, SIMPLE_APP_NAME, TIMEOUT_IN_MINUTES, user, password, isInstallExpectedToFail, null);
 		}
 
 	}
 
 }
