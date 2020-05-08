 package test.cli.cloudify.cloud.ec2;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import org.cloudifysource.restclient.GSRestClient;
 import org.cloudifysource.restclient.RestException;
 
 import test.cli.cloudify.CommandTestUtils;
 import test.cli.cloudify.cloud.NewAbstractCloudTest;
 import test.cli.cloudify.cloud.services.ec2.SecuredEc2CloudService;
 import test.cli.cloudify.security.SecuredCloudService;
 import test.cli.cloudify.security.SecurityConstants;
 
 import com.j_spaces.kernel.PlatformVersion;
 
 import framework.tools.SGTestHelper;
 import framework.utils.ApplicationInstaller;
 import framework.utils.AssertUtils;
 
 public class Ec2SecurityTest extends NewAbstractCloudTest {
 
 	private static final String FILE_SEP = System.getProperty("file.separator");
 	private static final String APP_NAME = "simple";
 	private static final String SERVICE_NAME = "simple";
 	String pathToSecurityFile = SGTestHelper.getSGTestRootDir() + FILE_SEP + "config" + FILE_SEP + "security"
 			+ FILE_SEP + "spring-security.xml";
 	
 	SecuredCloudService cloudService;
 	
 	//@BeforeClass(alwaysRun = true)
 	protected void bootstrap() throws Exception {
 		cloudService = new SecuredEc2CloudService(SecurityConstants.APP_MANAGER_USER_PWD,
 				SecurityConstants.APP_MANAGER_USER_PWD, pathToSecurityFile);
 		super.bootstrap(cloudService);
 	}
 	
 	//@AfterClass(alwaysRun = true)
 	protected void teardown() throws Exception {
 		super.teardown();
 	}
 
 	//@Test(timeOut = DEFAULT_TEST_TIMEOUT * 20, enabled = true)
 	public void testRoles() throws IOException, InterruptedException, RestException {
 		doTest(APP_NAME, SERVICE_NAME);
 	}
 	
 	private void doTest(final String appName, final String serviceName) throws IOException, InterruptedException, RestException {
 	
 		// test each Rest server found
 		for (String restUrl : cloudService.getRestUrls()) {
 
 			installApplication(restUrl, APP_NAME, SecurityConstants.CLOUD_ADMIN_USER_PWD,
 					SecurityConstants.CLOUD_ADMIN_USER_PWD, null/*authGroups*/);
 			String output = listApplications(restUrl, SecurityConstants.CLOUD_ADMIN_USER_PWD,
 					SecurityConstants.CLOUD_ADMIN_USER_PWD);
 			assertTrue("list-applications did not display application " + APP_NAME + " for user: "
 					+ SecurityConstants.VIEWER_USER_PWD, output.contains(APP_NAME));
 			uninstallApplication(restUrl, APP_NAME, SecurityConstants.CLOUD_ADMIN_USER_PWD,
 					SecurityConstants.CLOUD_ADMIN_USER_PWD, null/*authGroups*/);
 
 			/*output = listApplications(restUrl, SecurityConstants.APP_MANAGER_USER_PWD, SecurityConstants.APP_MANAGER_USER_PWD);
 			assertTrue("list-applications did not display application " + APP_NAME + " for user: "
 					+ SecurityConstants.APP_MANAGER_USER_PWD, output.contains("Recipe test completed"));
 
 			output = listApplications(restUrl, SecurityConstants.VIEWER_USER_PWD, SecurityConstants.VIEWER_USER_PWD);
 			assertTrue("list-applications did not display application " + APP_NAME + " for user: "
 					+ SecurityConstants.VIEWER_USER_PWD, output.contains("Recipe test completed"));
 
 			installApplication(restUrl, APP_NAME, SecurityConstants.CLOUD_ADMIN_USER_PWD,
 					SecurityConstants.CLOUD_ADMIN_USER_PWD, "ROLE_APP_MANAGERS");
 			output = listApplications(restUrl, SecurityConstants.CLOUD_ADMIN_USER_PWD, SecurityConstants.CLOUD_ADMIN_USER_PWD);
 			assertTrue("list-applications did not display application " + APP_NAME + " for user: "
 					+ SecurityConstants.CLOUD_ADMIN_USER_PWD, output.contains("Recipe test completed"));
 
 			output = listApplications(restUrl, SecurityConstants.APP_MANAGER_USER_PWD, SecurityConstants.APP_MANAGER_USER_PWD);
 			assertTrue("list-applications did not display application " + APP_NAME + " for user: "
 					+ SecurityConstants.APP_MANAGER_USER_PWD, output.contains("Recipe test completed"));
 
 			output = listApplications(restUrl, SecurityConstants.VIEWER_USER_PWD, SecurityConstants.VIEWER_USER_PWD);
 			assertTrue("list-applications display application " + APP_NAME + " for unauthorized user: "
 					+ SecurityConstants.VIEWER_USER_PWD, output.contains("Recipe test completed"));*/
 		}
 	}
 
 	private void installApplication(final String restUrl, final String applicationName, final String cloudifyUsername,
 			final String cloudifyPassword, final String authGroups) throws IOException, InterruptedException, 
 			RestException {
 
 		ApplicationInstaller applicationInstaller = new ApplicationInstaller(restUrl, applicationName);
		applicationInstaller.setRecipePath(CommandTestUtils.getPath("src/main/resources/apps/USM/usm/applications/" + applicationName));
		applicationInstaller.setWaitForFinish(true);
		applicationInstaller.setCloudifyUsername(cloudifyUsername);
		applicationInstaller.setCloudifyPassword(cloudifyPassword);
 		if (StringUtils.isNotBlank(authGroups)) {
			applicationInstaller.setAuthGroups(authGroups);
 		}
 		
 		applicationInstaller.install();
 		
 		checkPuStatus(restUrl, cloudifyUsername, cloudifyPassword);
 		
 		/*String absolutePUName = ServiceUtils.getAbsolutePUName("simple", "simple");
 		final ProcessingUnit processingUnit = admin.getProcessingUnits().waitFor(absolutePUName, 30, TimeUnit.SECONDS);
 		if (processingUnit == null) {
 			Assert.fail("Processing unit '" + absolutePUName + "' was not found");
 		}
 		Assert.assertTrue(processingUnit.waitFor(1, 30, TimeUnit.SECONDS), "Instance of '" + absolutePUName
 				+ "' service was not found");
 		assertTrue(USMTestUtils.waitForPuRunningState(absolutePUName, 60, TimeUnit.SECONDS, admin));*/
 	}
 
 	private void uninstallApplication(final String restUrl, final String applicationName, final String cloudifyUsername,
 			final String cloudifyPassword, final String authGroups)
 			throws IOException, InterruptedException, RestException {
 		
 		ApplicationInstaller applicationInstaller = new ApplicationInstaller(restUrl, applicationName);
		applicationInstaller.setRecipePath(CommandTestUtils.getPath("src/main/resources/apps/USM/usm/applications/" + applicationName));
		applicationInstaller.setWaitForFinish(true);
 		if (StringUtils.isNotBlank(authGroups)) {
			applicationInstaller.setAuthGroups(authGroups);
 		}
 		
 		applicationInstaller.uninstall();
 		
 		checkPuStatus(restUrl, cloudifyUsername, cloudifyPassword);
 	}
 
 	private String listApplications(final String restUrl, final String user, final String password) throws IOException,
 			InterruptedException {
 		return CommandTestUtils.runCommandAndWait("connect -user " + user + " -pwd " + password + " " + restUrl 
 				+ ";list-applications");
 	}
 	
 	private void checkPuStatus(final String restUrl, final String cloudifyUsername, final String cloudifyPassword) throws IOException, InterruptedException, RestException {
 		String absolutePUName = ServiceUtils.getAbsolutePUName(APP_NAME, SERVICE_NAME);
 		GSRestClient client = new GSRestClient(cloudifyUsername, cloudifyPassword, new URL(restUrl),
 				PlatformVersion.getVersionNumber());
 		Map<String, Object> entriesJsonMap  = client.getAdminData("ProcessingUnits/Names/" + absolutePUName + "/Status");
 		String serviceStatus = (String)entriesJsonMap.get("STATUS_PROPERTY");
 		
 		AssertUtils.assertTrue("service is not intact", serviceStatus.equalsIgnoreCase("INTACT"));	
 	}
 	
 	@Override
 	protected String getCloudName() {
 		return "ec2";
 	}
 
 	@Override
 	protected boolean isReusableCloud() {
 		return false;
 	}
 }
