 package org.cloudifysource.quality.iTests.framework.utils;
 
 import iTests.framework.utils.AssertUtils;
 import iTests.framework.utils.IOUtils;
 import iTests.framework.utils.LogUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.lang.StringUtils;
 import org.cloudifysource.domain.Application;
 import org.cloudifysource.dsl.internal.CloudifyConstants;
 import org.cloudifysource.dsl.internal.DSLReader;
 import org.cloudifysource.dsl.internal.packaging.Packager;
 import org.cloudifysource.dsl.rest.request.InstallApplicationRequest;
 import org.cloudifysource.dsl.rest.request.InstallServiceRequest;
 import org.cloudifysource.dsl.rest.response.ApplicationDescription;
 import org.cloudifysource.dsl.rest.response.InstallApplicationResponse;
 import org.cloudifysource.dsl.rest.response.InstallServiceResponse;
 import org.cloudifysource.dsl.rest.response.ServiceDescription;
 import org.cloudifysource.dsl.rest.response.UploadResponse;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CloudTestUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.security.SecurityConstants;
 import org.cloudifysource.restclient.GSRestClient;
 import org.cloudifysource.restclient.RestClient;
 import org.cloudifysource.restclient.RestException;
 import org.cloudifysource.restclient.exceptions.RestClientException;
 import org.cloudifysource.shell.exceptions.CLIException;
 
 import com.j_spaces.kernel.PlatformVersion;
 
 public abstract class RecipeInstaller {
 
     protected String applicationName;
     private String restUrl;
     private String recipePath;
     private boolean disableSelfHealing = false;
     private Map<String, Object> overrideProperties;
     private Map<String, Object> cloudOverrideProperties;
     private String cloudConfiguration;
     private long timeoutInMinutes;
     private boolean waitForFinish = true;
     private boolean expectToFail = false;
     private String cloudifyUsername;
     private String cloudifyPassword;
     private String authGroups;
     private String debugMode;
     private String debugEvent;
     private boolean justBuildCommand = false;
     private boolean debugAll = false;
 
     public RecipeInstaller(final String restUrl, int timeout, String applicationName) {
         this.restUrl = restUrl;
         this.timeoutInMinutes = timeout;
         this.applicationName = applicationName;
     }
 
     public RecipeInstaller(final String restUrl, int timeout) {
         this.restUrl = restUrl;
         this.timeoutInMinutes = timeout;
         this.applicationName = CloudifyConstants.DEFAULT_APPLICATION_NAME;
     }
 
     public boolean isDisableSelfHealing() {
         return disableSelfHealing;
     }
 
     public RecipeInstaller setDisableSelfHealing(boolean disableSelfHealing) {
         this.disableSelfHealing = disableSelfHealing;
         return this;
     }
 
     public String getRestUrl() {
         return restUrl;
     }
 
     public RecipeInstaller restUrl(String restUrl) {
         this.restUrl = restUrl;
         return this;
     }
 
     public String getRecipePath() {
         return recipePath;
     }
 
     public RecipeInstaller recipePath(String recipePath) {
         this.recipePath = recipePath;
         return this;
     }
 
     public Map<String, Object> getOverrides() {
         return overrideProperties;
     }
 
     public RecipeInstaller overrides(Map<String, Object> overridesFilePath) {
         this.overrideProperties = overridesFilePath;
         return this;
     }
 
     public Map<String, Object> getCloudOverrides() {
         return cloudOverrideProperties;
     }
 
     public RecipeInstaller cloudOverrides(Map<String, Object> cloudOverridesFilePath) {
         this.cloudOverrideProperties = cloudOverridesFilePath;
         return this;
     }
 
     public String getCloudConfiguration() {
         return cloudConfiguration;
     }
 
     public RecipeInstaller cloudConfiguration(String cloudConfiguration) {
         this.cloudConfiguration = cloudConfiguration;
         return this;
     }
 
     public long getTimeoutInMinutes() {
         return timeoutInMinutes;
     }
 
     public RecipeInstaller timeoutInMinutes(long timeoutInMinutes) {
         this.timeoutInMinutes = timeoutInMinutes;
         return this;
     }
 
     public boolean isWaitForFinish() {
         return waitForFinish;
     }
 
     public RecipeInstaller waitForFinish(boolean waitForFinish) {
         this.waitForFinish = waitForFinish;
         return this;
     }
 
     public boolean isExpectToFail() {
         return expectToFail;
     }
 
     public RecipeInstaller expectToFail(boolean expectToFail) {
         this.expectToFail = expectToFail;
         return this;
     }
 
     public RecipeInstaller cloudifyUsername(String cloudifyUsername) {
         this.cloudifyUsername = cloudifyUsername;
         return this;
     }
 
     public RecipeInstaller cloudifyPassword(String cloudifyPassword) {
         this.cloudifyPassword = cloudifyPassword;
         return this;
     }
 
     public String getCloudifyUsername() {
         return cloudifyUsername;
     }
 
     public String getCloudifyPassword() {
         return cloudifyPassword;
     }
 
     public String getAuthGroups() {
         return authGroups;
     }
 
     public RecipeInstaller authGroups(String authGroups) {
         this.authGroups = authGroups;
         return this;
     }
 
     public RecipeInstaller debugMode(String debugMode) {
         this.debugMode = debugMode;
         return this;
     }
 
     public RecipeInstaller debugEvent(String debugEvent) {
         this.debugEvent = debugEvent;
         return this;
     }
 
     public RecipeInstaller debugAll(boolean debugAll) {
         this.debugAll = debugAll;
         return this;
     }
 
     public RecipeInstaller buildCommand(boolean justBuildCommand) {
         this.justBuildCommand = justBuildCommand;
         return this;
     }
 
     public String install() throws IOException, InterruptedException {
 
         String installCommand = null;
         String excpectedResult = null;
         String recipeName = null;
         if (this instanceof ServiceInstaller) {
             installCommand = "install-service";
             recipeName = ((ServiceInstaller) this).getServiceName();
             excpectedResult = "Service \"" + recipeName + "\" successfully installed";
         } else if (this instanceof ApplicationInstaller) {
             installCommand = "install-application";
             recipeName = ((ApplicationInstaller) this).getApplicationName();
             excpectedResult = "Application " + recipeName + " installed successfully";
         }
 
         if (recipePath == null) {
             throw new IllegalStateException("recipe path cannot be null. please use setRecipePath before calling install");
         }
 
         StringBuilder commandBuilder = new StringBuilder()
                 .append(installCommand).append(" ")
                 .append("--verbose").append(" ")
                 .append("-timeout").append(" ")
                 .append(timeoutInMinutes).append(" ");
 
         if (disableSelfHealing) {
             commandBuilder.append("-disableSelfHealing").append(" ");
         }
 
         if (cloudOverrideProperties != null && !cloudOverrideProperties.isEmpty()) {
             File cloudOverridesFile = IOUtils.createTempOverridesFile(cloudOverrideProperties);
             commandBuilder.append("-cloud-overrides").append(" ").append(cloudOverridesFile.getAbsolutePath().replace("\\", "/")).append(" ");
         }
         if (overrideProperties != null && !overrideProperties.isEmpty()) {
             File serviceOverridesFile = IOUtils.createTempOverridesFile(overrideProperties);
             commandBuilder.append("-overrides").append(" ").append(serviceOverridesFile.getAbsolutePath().replace("\\", "/")).append(" ");
         }
         if (cloudConfiguration != null && !cloudConfiguration.isEmpty()) {
             commandBuilder.append("-cloudConfiguration").append(" ").append(cloudConfiguration.replace("\\", "/")).append(" ");
         }
         if (authGroups != null && !authGroups.isEmpty()) {
             commandBuilder.append("-authGroups").append(" ").append(authGroups).append(" ");
         }
         if (debugEvent!= null && !debugEvent.isEmpty()) {
             commandBuilder.append("-debug-events").append(" ").append(debugEvent).append(" ");
         }
         if (debugAll) {
             commandBuilder.append("-debug-all").append(" ");
         }
         if (debugMode != null && !debugMode.isEmpty()) {
             commandBuilder.append("-debug-mode").append(" ").append(debugMode).append(" ");
         }
 
        if (recipeName != null && !recipeName.isEmpty()) {
             commandBuilder.append("-name").append(" ").append(recipeName).append(" ");
         }
 
         commandBuilder.append(recipePath.replace('\\', '/'));
         final String installationCommand = commandBuilder.toString();
         final String connectCommand = connectCommand();
 
         if(justBuildCommand){
             return connectCommand + ";" + installationCommand;
         }
 
         if (expectToFail) {
             return CommandTestUtils.runCommandExpectedFail(connectCommand + ";" + installationCommand);
         }
         if (waitForFinish) {
             String output = CommandTestUtils.runCommandAndWait(connectCommand + ";" + installationCommand);
             AssertUtils.assertTrue("Installation of " + recipeName + " was expected to succeed, but it failed",
                     output.toLowerCase().contains(excpectedResult.toLowerCase()));
             return output;
         } else {
             return CommandTestUtils.runCommand(connectCommand + ";" + installationCommand);
         }
     }
 
 
 
     public String uninstall() throws IOException, InterruptedException {
 
         final boolean enableLogstash = Boolean.parseBoolean(System.getProperty("iTests.enableLogstash", "false"));
         String uninstallCommand = null;
         String excpectedResult = null;
         String recipeName = null;
         if (this instanceof ServiceInstaller) {
             uninstallCommand = "uninstall-service";
             recipeName = ((ServiceInstaller) this).getServiceName();
             excpectedResult = "Successfully undeployed " + recipeName;
         } else if (this instanceof ApplicationInstaller) {
             uninstallCommand = "uninstall-application";
             recipeName = ((ApplicationInstaller) this).getApplicationName();
             excpectedResult = "Application " + recipeName + " uninstalled successfully";
         }
 
         if(!enableLogstash){
             String url = null;
             try {
                 url = restUrl + "/service/dump/machines/?fileSizeLimit=50000000";
                 CloudTestUtils.dumpMachines(restUrl, SecurityConstants.USER_PWD_ALL_ROLES, SecurityConstants.USER_PWD_ALL_ROLES);
             } catch (final Exception e) {
                 LogUtils.log("Failed to create dump for this url - " + url, e);
             }
         }
 
         final String uninstallationCommand = new StringBuilder()
                 .append(uninstallCommand).append(" ")
                 .append("--verbose").append(" ")
                 .append("-timeout").append(" ")
                 .append(timeoutInMinutes).append(" ")
                 .append(recipeName)
                 .toString();
 
         final String connectCommand = connectCommand();
 
         if (expectToFail) {
             String output = CommandTestUtils.runCommandExpectedFail(connectCommand + ";" + uninstallationCommand);
             return output;
         }
         if (waitForFinish) {
             String output = CommandTestUtils.runCommandAndWait(connectCommand + ";" + uninstallationCommand);
             return output;
         } else {
             return CommandTestUtils.runCommand(connectCommand + ";" + uninstallationCommand);
         }
 
     }
 
     protected String connectCommand() {
 
         if (restUrl == null || restUrl.isEmpty()) {
             throw new IllegalStateException("Rest URL cannot be null or empty when trying to connect");
         }
         if (StringUtils.isNotBlank(cloudifyUsername) && StringUtils.isNotBlank(cloudifyPassword)) {
             return "connect -user " + cloudifyUsername + " -password " + cloudifyPassword + " " + getRestUrl();
         }
 
         return "connect " + getRestUrl();
     }
 
     public void restUninstall() throws Exception {
         final String version = PlatformVersion.getVersion();
         final URL url = new URL(this.getRestUrl());
         final RestClient client = new RestClient(url, this.getCloudifyUsername(), this.getCloudifyPassword(), version);
         client.connect();
         String recipeName = null;
         if (this instanceof ServiceInstaller) {
             recipeName = ((ServiceInstaller) this).getServiceName();
             client.uninstallService(applicationName, recipeName, (int) this.getTimeoutInMinutes());
         } else if (this instanceof ApplicationInstaller) {
             recipeName = ((ApplicationInstaller) this).getApplicationName();
             client.uninstallApplication(recipeName, (int) this.getTimeoutInMinutes());
         }
 
         if (this.isWaitForFinish()) {
         	waitForUninstall(url, client);
         }
 
     }
 
     public Object[] restInstall() throws Exception {
         InstallApplicationResponse installApplicationResponse = null;
         InstallServiceResponse installServiceResponse = null;
 
         String recipeName = null;
         if (this instanceof ServiceInstaller) {
             recipeName = ((ServiceInstaller) this).getServiceName();
         } else if (this instanceof ApplicationInstaller) {
             recipeName = ((ApplicationInstaller) this).getApplicationName();
         }
 
         final String version = PlatformVersion.getVersion();
         final URL url = new URL(this.getRestUrl());
         final RestClient client = new RestClient(url, this.getCloudifyUsername(), this.getCloudifyPassword(), version);
         client.connect();
         File packedFile = null;
         if (this instanceof ServiceInstaller) {
             packedFile = Packager.pack(new File(this.getRecipePath()));
         } else if (this instanceof ApplicationInstaller) {
             final File appFolder = new File(this.getRecipePath());
             final DSLReader dslReader = createDslReader(appFolder);
             final Application application = dslReader.readDslEntity(Application.class);
             packedFile = Packager.packApplication(application, appFolder);
         }
         final UploadResponse uploadResponse = client.upload(packedFile.getName(), packedFile);
         final String uploadKey = uploadResponse.getUploadKey();
 
         if (this instanceof ServiceInstaller) {
 
             InstallServiceRequest request = new InstallServiceRequest();
             request.setServiceFolderUploadKey(uploadKey);
 
             // Test will run in unsecured mode.
             request.setAuthGroups(this.getAuthGroups());
             // no debugging.
             request.setDebugAll(this.isDebugAll());
             request.setSelfHealing(this.isDisableSelfHealing());
 
             // set timeout
             request.setTimeoutInMillis(AbstractTestSupport.EXTENDED_TEST_TIMEOUT);
             // make install service API call
             installServiceResponse = client.installService("default", recipeName, request);
         } else if (this instanceof ApplicationInstaller) {
             InstallApplicationRequest request = new InstallApplicationRequest();
             request.setApplcationFileUploadKey(uploadKey);
 
             // Test will run in unsecured mode.
             request.setAuthGroups("");
             // no debugging.
             request.setDebugAll(this.isDebugAll());
             request.setSelfHealing(this.isDisableSelfHealing());
             request.setApplicationName(recipeName);
             // set timeout
             request.setTimeoutInMillis(TimeUnit.MINUTES.toMillis(this.getTimeoutInMinutes()));
 
             // make install service API call
             installApplicationResponse = client.installApplication(recipeName, request);
         }
         if (isWaitForFinish()) {
             // wait for the application to reach STARTED state
             waitForInstall(url, client);
         }
         return new Object[] { installApplicationResponse, installServiceResponse };
     }
 
     private DSLReader createDslReader(final File recipeFile) {
         final DSLReader dslReader = new DSLReader();
         final File dslFile = DSLReader.findDefaultDSLFile(org.cloudifysource.dsl.internal.DSLUtils.APPLICATION_DSL_FILE_NAME_SUFFIX, recipeFile);
         dslReader.setDslFile(dslFile);
         dslReader.setCreateServiceContext(false);
         dslReader.addProperty(org.cloudifysource.dsl.internal.DSLUtils.APPLICATION_DIR, dslFile.getParentFile().getAbsolutePath());
         return dslReader;
     }
 
     public void waitForInstall(final URL url, final RestClient restClient)
             throws CLIException, RestClientException {
         LogUtils.log("Waiting for deployment state to be " + CloudifyConstants.DeploymentState.STARTED);
         if (this instanceof ApplicationInstaller) {
             AssertUtils.repetitiveAssertTrue(applicationName + " application failed to deploy", new AssertUtils.RepetitiveConditionProvider() {
                 @Override
                 public boolean getCondition() {
                     try {
                         final List<ApplicationDescription> applicationDescriptionsList = restClient.getApplicationDescriptionsList();
                         for (ApplicationDescription applicationDescription : applicationDescriptionsList) {
                             if (applicationDescription.getApplicationName().equals(applicationName)) {
                                 if (applicationDescription.getApplicationState().equals(CloudifyConstants.DeploymentState.STARTED)) {
                                     return true;
                                 }
                             }
                         }
                     } catch (RestClientException e) {
                     	LogUtils.log("Failed getting application description");
 					}
                     return false;
                 }
             }, TimeUnit.MINUTES.toMillis(this.getTimeoutInMinutes()));
         }
         else if (this instanceof ServiceInstaller) {
             final String serviceName = ((ServiceInstaller) this).getServiceName();
             AssertUtils.repetitiveAssertTrue(serviceName + " service failed to deploy", new AssertUtils.RepetitiveConditionProvider() {
                 @Override
                 public boolean getCondition() {
                     try {
                     	List<ServiceDescription> servicesDescriptionList = restClient.getServicesDescriptionList(CloudifyConstants.DEFAULT_APPLICATION_NAME);
                         for (ServiceDescription description : servicesDescriptionList) {
                             if (description.getServiceName().equals(serviceName)) {
                                 if (description.getServiceState().equals(CloudifyConstants.DeploymentState.STARTED)) {
                                     return true;
                                 }
                             }
                         }
                     } catch (RestClientException e) {
                     	LogUtils.log("Failed getting service description");
 					}
                     return false;
                 }
             }, TimeUnit.MINUTES.toMillis(this.getTimeoutInMinutes()));
         }
     }
 
     public void waitForUninstall(final URL url, final RestClient restClient) throws Exception {
         LogUtils.log("Waiting for USM_State to be " + CloudifyConstants.USMState.RUNNING);
         if (this instanceof ApplicationInstaller) {
             AssertUtils.repetitiveAssertTrue("uninstall failed for application " + applicationName, new AssertUtils.RepetitiveConditionProvider() {
                 @Override
                 public boolean getCondition() {
                     try {
                         final List<ApplicationDescription> applicationDescriptionsList = restClient.getApplicationDescriptionsList();
                         for (ApplicationDescription applicationDescription : applicationDescriptionsList) {
                             if (applicationDescription.getApplicationName().equals(applicationName)) {
                                 return false;
                             }
                         }
                         return true;
                     } catch (final RestClientException e) {
                         LogUtils.log("Failed getting application list.");
                     }
                     return false;
                 }
             }, TimeUnit.MINUTES.toMillis(this.getTimeoutInMinutes()));
         }
         else if (this instanceof ServiceInstaller) {
             final GSRestClient client = new GSRestClient("", "", new URL(this.getRestUrl()), PlatformVersion.getVersionNumber());
             final String serviceName = ((ServiceInstaller) this).getServiceName();
             AssertUtils.repetitiveAssertTrue("uninstall service failed " + serviceName,
                     new AssertUtils.RepetitiveConditionProvider() {
                         @Override
                         public boolean getCondition() {
                             try {
                                 Map<String, Object> adminData = client.getAdminData("zones/Names");
                                 List<String> names = (List<String>) adminData.get("Names-Elements");
                                 for (String string : names) {
                                     if (string.contains(serviceName)) {
                                         return false;
                                     }
                                 }
                                 return true;
                             } catch (RestException e) {
                                 // TODO Auto-generated catch block
                                 LogUtils.log("Failed getting zones list.");
                                 e.printStackTrace();
                             }
                             return false;
                         }
                     }, TimeUnit.MINUTES.toMillis(this.getTimeoutInMinutes()));
         }
     }
 
     public boolean isDebugAll() {
         return debugAll;
     }
 
     public void setDebugAll(boolean debugAll) {
         this.debugAll = debugAll;
     }
 }
