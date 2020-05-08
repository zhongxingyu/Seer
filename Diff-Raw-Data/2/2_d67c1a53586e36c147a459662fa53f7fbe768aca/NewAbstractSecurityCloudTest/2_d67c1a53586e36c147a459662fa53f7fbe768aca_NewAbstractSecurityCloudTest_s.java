 package test.cli.cloudify.cloud;
 
 import java.io.IOException;
 
 import org.apache.commons.lang.StringUtils;
 
 import test.cli.cloudify.cloud.services.CloudService;
 import test.cli.cloudify.cloud.services.CloudServiceManager;
 import test.cli.cloudify.security.SecurityConstants;
 import framework.utils.ApplicationInstaller;
 import framework.utils.CloudBootstrapper;
 import framework.utils.ServiceInstaller;
 
 public abstract class NewAbstractSecurityCloudTest extends NewAbstractCloudTest {
 	
 	@Override
 	protected void bootstrap() throws Exception {
 		
 		CloudService service = CloudServiceManager.getInstance().getCloudService(getCloudName());
 		CloudBootstrapper securedBootstrapper = new CloudBootstrapper();
 		securedBootstrapper.secured(true).securityFilePath(SecurityConstants.BUILD_SECURITY_FILE_PATH)
 			.user(SecurityConstants.ALL_ROLES_USER_PWD).password(SecurityConstants.ALL_ROLES_USER_PWD);
 		securedBootstrapper.keystoreFilePath(SecurityConstants.DEFAULT_KEYSTORE_FILE_PATH).keystorePassword(SecurityConstants.DEFAULT_KEYSTORE_PASSWORD);
 		service.setBootstrapper(securedBootstrapper);
 		
 		super.bootstrap(service);
 	}
 	
 	protected void bootstrap(CloudService service, CloudBootstrapper securedBootstrapper) throws Exception {
 		
 		service.setBootstrapper(securedBootstrapper);		
 		super.bootstrap(service);
 	}
 	
 	@Override
 	protected void teardown() throws Exception {
 		
 		super.teardown();
 	}
 	
 	protected String installApplicationAndWait(String applicationPath, String applicationName, int timeout, final String cloudifyUsername,
 			final String cloudifyPassword, boolean isExpectedToFail, final String authGroups) throws IOException, InterruptedException {
 
 		ApplicationInstaller applicationInstaller = new ApplicationInstaller(getRestUrl(), applicationName);
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
 
 		ApplicationInstaller applicationInstaller = new ApplicationInstaller(getRestUrl(), applicationName);
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
 		ApplicationInstaller applicationInstaller = new ApplicationInstaller(getRestUrl(), applicationName);
 		applicationInstaller.waitForFinish(true);
 		applicationInstaller.cloudifyUsername(cloudifyUsername);
 		applicationInstaller.cloudifyPassword(cloudifyPassword);
 		applicationInstaller.uninstallIfFound();
 	}
 	
 	protected String installServiceAndWait(String servicePath, String serviceName, int timeout, final String cloudifyUsername,
 			final String cloudifyPassword, boolean isExpectedToFail, final String authGroups) throws IOException, InterruptedException {
 
 		ServiceInstaller serviceInstaller = new ServiceInstaller(getRestUrl(), serviceName);
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
 
 		ServiceInstaller serviceInstaller = new ServiceInstaller(getRestUrl(), serviceName);
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
 
 	protected String login(String user, String password, boolean failCommand) throws IOException, InterruptedException{
 		return getService().getBootstrapper().user(user).password(password).login(failCommand);
 	}
 	
 	protected String connect(String user, String password, boolean isExpectedToFail) throws IOException, InterruptedException{	
		return getService().getBootstrapper().user(user).password(password).login(isExpectedToFail);
 	}
 
 	protected String listApplications(String user, String password, boolean expectedFail) throws IOException, InterruptedException{
 		return getService().getBootstrapper().user(user).password(password).listApplications(expectedFail);
 	}
 	
 	protected String listServices(String user, String password, String applicationName, boolean expectedFail) throws IOException, InterruptedException{
 		return getService().getBootstrapper().user(user).password(password).listServices(applicationName, expectedFail);
 	}
 	
 	protected String listInstances(String user, String password, String applicationName, String serviceName, boolean expectedFail) throws IOException, InterruptedException{
 		return getService().getBootstrapper().user(user).password(password).listInstances(applicationName, serviceName, expectedFail);
 	}
 }
 
