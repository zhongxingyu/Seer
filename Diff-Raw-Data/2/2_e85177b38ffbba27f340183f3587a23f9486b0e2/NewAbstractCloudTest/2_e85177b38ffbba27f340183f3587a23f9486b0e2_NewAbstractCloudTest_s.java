 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.cloudifysource.dsl.cloud.compute.ComputeTemplate;
 import org.cloudifysource.quality.iTests.framework.utils.ApplicationInstaller;
 import org.cloudifysource.quality.iTests.framework.utils.AssertUtils;
 import org.cloudifysource.quality.iTests.framework.utils.CloudBootstrapper;
 import org.cloudifysource.quality.iTests.framework.utils.DumpUtils;
 import org.cloudifysource.quality.iTests.framework.utils.LogUtils;
 import org.cloudifysource.quality.iTests.framework.utils.ScriptUtils;
 import org.cloudifysource.quality.iTests.framework.utils.ServiceInstaller;
 import org.cloudifysource.quality.iTests.framework.utils.StorageUtils;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.CloudService;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.CloudServiceManager;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.security.SecurityConstants;
 import org.openspaces.admin.Admin;
 import org.testng.Assert;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 
 import com.gigaspaces.internal.utils.StringUtils;
 
 public abstract class NewAbstractCloudTest extends AbstractTestSupport {
 
     private static final int TEN_SECONDS_IN_MILLIS = 10000;
 
     public static final int SERVICE_INSTALLATION_TIMEOUT_IN_MINUTES = 10;
 
     private static final int MAX_SCAN_RETRY = 3;
 
     protected CloudService cloudService;
 
     protected void customizeCloud() throws Exception {}
 
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
 
     @BeforeClass(alwaysRun = true)
     public void overrideCloudifyJars() throws Exception {
         if(System.getProperty("cloudify.override") != null) {
             buildAndCopyCloudifyJars(getCloudName());
         }
     }
 
     private void buildAndCopyCloudifyJars(String cloudName) throws Exception{
         String cloudifySourceDir = ScriptUtils.getCloudifySourceDir() + "cloudify";
         String prefix = "";
         String extension = "";
         if(ScriptUtils.isWindows()) {
             prefix = getPrefix(System.getenv("M2_HOME"));
             extension = ".bat";
         }
         ScriptUtils.startLocalProcess(cloudifySourceDir, (prefix + "mvn" + extension + " clean package -DskipTests").split(" "));
         if(ScriptUtils.isWindows()) {
             prefix = getPrefix(System.getenv("ANT_HOME"));
         }
         ScriptUtils.startLocalProcess(cloudifySourceDir, (prefix + "ant" + extension + " -f copy_jars_local.xml \"Copy Cloudify jars to install dir\"").split(" "));
         ScriptUtils.startLocalProcess(cloudifySourceDir, (prefix + "ant" + extension + " -f copy_jars_local.xml \"Copy Cloudify jars to " + getCloudName() + " upload dir\"").split(" "));
     }
 
     private String getPrefix(String homeVar) {
         String s = File.separator;
         return "cmd /c call " + homeVar + s + "bin" + s;
     }
 
 
 
     @BeforeMethod
     public void beforeTest() {
         LogUtils.log("Creating test folder");
     }
 
     public CloudService getService() {
         return cloudService;
     }
 
     protected void bootstrap() throws Exception {
         bootstrap(null, null);
     }
 
     protected void bootstrap(CloudBootstrapper bootstrapper) throws Exception {
         bootstrap(null, bootstrapper);
     }
 
     protected void bootstrap(CloudService service) throws Exception {
         bootstrap(service, null);
     }
 
     protected void bootstrap(CloudService service, CloudBootstrapper bootstrapper) throws Exception {
         if (this.isReusableCloud()) {
             throw new UnsupportedOperationException(this.getClass().getName() + "Requires reusable clouds, which are not supported yet");
         }
 
         if (service == null) { // use the default cloud service if non is specified
             this.cloudService = CloudServiceManager.getInstance().getCloudService(this.getCloudName());
         }
         else {
             this.cloudService = service; // use the custom service to execute bootstrap and teardown commands
         }
         if (bootstrapper != null) { // use the custom bootstrapper
             this.cloudService.setBootstrapper(bootstrapper);
         }
 
         this.cloudService.init(this.getClass().getSimpleName().toLowerCase());
 
         LogUtils.log("Customizing cloud");
         customizeCloud();
 
         beforeBootstrap();
 
        final String prefix = System.getProperty("branch.name", "") + "-" + System.getProperty("user.name") + "-" + this.getClass().getSimpleName().toLowerCase() + "-";
 
         this.cloudService.setMachinePrefix(prefix);
         this.cloudService.setVolumePrefix(prefix);
         this.cloudService.bootstrapCloud();
 
         if(!cloudService.getBootstrapper().isBootstrapExpectedToFail()) {
             afterBootstrap();
         }
     }
 
     protected void teardown() throws Exception {
 
         beforeTeardown();
 
         if (this.cloudService == null) {
             LogUtils.log("No teardown was executed as the cloud instance for this class was not created");
             return;
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
         LogUtils.log("installing application " + applicationName + " on " + cloudService.getCloudName());
         String applicationPath = ScriptUtils.getBuildPath() + "/recipes/apps/" + applicationFolderName;
         installApplicationAndWait(applicationPath, applicationName);
         uninstallApplicationAndWait(applicationName);
         scanForLeakedAgentNodes();
     }
 
     protected void doSanityTest(String applicationFolderName, String applicationName, final int timeout) throws IOException, InterruptedException {
         LogUtils.log("installing application " + applicationName + " on " + cloudService.getCloudName());
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
 
         Throwable first = null;
         for (int i = 0 ; i < MAX_SCAN_RETRY ; i++) {
             try {
                 boolean leakedAgentNodesScanResult = this.cloudService.scanLeakedAgentNodes();
                 if (leakedAgentNodesScanResult == true) {
                     return;
                 } else {
                     Assert.fail("Leaked nodes were found!");
                 }
                 break;
             } catch (final Exception t) {
                 first = t;
                 LogUtils.log("Failed scaning for leaked nodes. attempt number " + (i + 1) , t);
             }
         }
         if (first != null) {
             Assert.fail("Failed scanning for leaked nodes after " + MAX_SCAN_RETRY + " attempts. First exception was --> " + first.getMessage(), first);
         }
     }
 
     protected String installApplicationAndWait(String applicationName) throws IOException, InterruptedException {
         ApplicationInstaller applicationInstaller = new ApplicationInstaller(getRestUrl(), applicationName);
         applicationInstaller.waitForFinish(true);
         return applicationInstaller.install();
     }
 
     protected String installApplicationAndWait(String applicationPath, String applicationName) throws IOException, InterruptedException {
         ApplicationInstaller applicationInstaller = new ApplicationInstaller(getRestUrl(), applicationName);
         applicationInstaller.recipePath(applicationPath);
         applicationInstaller.waitForFinish(true);
         return applicationInstaller.install();
     }
 
     protected String uninstallApplicationAndWait(String applicationName) throws IOException, InterruptedException {
         return uninstallApplicationAndWait(applicationName, false);
     }
     
     protected String uninstallApplicationAndWait(String applicationName, boolean isExpectedFail) throws IOException, InterruptedException {
         ApplicationInstaller applicationInstaller = new ApplicationInstaller(getRestUrl(), applicationName);
         applicationInstaller.waitForFinish(true);
         applicationInstaller.expectToFail(isExpectedFail);
         return applicationInstaller.uninstall();
     }
 
     protected void uninstallApplicationIfFound(String applicationName) throws IOException, InterruptedException {
         ApplicationInstaller applicationInstaller = new ApplicationInstaller(getRestUrl(), applicationName);
         applicationInstaller.waitForFinish(true);
         applicationInstaller.uninstallIfFound();
     }
 
     protected String installApplicationAndWait(String applicationPath, String applicationName, int timeout) throws IOException, InterruptedException {
         ApplicationInstaller applicationInstaller = new ApplicationInstaller(getRestUrl(), applicationName);
         applicationInstaller.recipePath(applicationPath);
         applicationInstaller.waitForFinish(true);
         applicationInstaller.timeoutInMinutes(timeout);
         return applicationInstaller.install();
     }
 
     protected String installApplicationAndWait(String applicationPath, String applicationName, int timeout , boolean expectToFail) throws IOException, InterruptedException {
         ApplicationInstaller applicationInstaller = new ApplicationInstaller(getRestUrl(), applicationName);
         applicationInstaller.recipePath(applicationPath);
         applicationInstaller.waitForFinish(true);
         applicationInstaller.expectToFail(expectToFail);
         applicationInstaller.timeoutInMinutes(timeout);
         return applicationInstaller.install();
     }
 
 
     protected String uninstallServiceAndWait(String serviceName, boolean isExpectedFail) throws IOException, InterruptedException {
         ServiceInstaller serviceInstaller = new ServiceInstaller(getRestUrl(), serviceName);
         serviceInstaller.waitForFinish(true);
         serviceInstaller.expectToFail(isExpectedFail);
         String output = serviceInstaller.uninstall();
 
         if(StorageUtils.isInitialized()){
             StorageUtils.afterServiceUninstallation(serviceName);
         }
         return output;
     }
     
     protected String uninstallServiceAndWait(String serviceName) throws IOException, InterruptedException {
     	return uninstallServiceAndWait(serviceName, false);
     }
 
     protected void uninstallServiceIfFound(String serviceName) throws IOException, InterruptedException {
         ServiceInstaller serviceInstaller = new ServiceInstaller(getRestUrl(), serviceName);
         serviceInstaller.waitForFinish(true);
         serviceInstaller.uninstallIfFound();
 
         if(StorageUtils.isInitialized()){
             StorageUtils.afterServiceUninstallation(serviceName);
         }
     }
 
     protected String invokeCommand(String serviceName, String commandName)
             throws IOException, InterruptedException {
     	ServiceInstaller installer = new ServiceInstaller(getRestUrl(), serviceName);
     	return installer.invoke(commandName);    	
     }
 
     protected String installServiceAndWait(String servicePath, String serviceName) throws IOException, InterruptedException {
         return installServiceAndWait(servicePath, serviceName, SERVICE_INSTALLATION_TIMEOUT_IN_MINUTES, false);
     }
 
     protected String installServiceAndWait(String servicePath, String serviceName, int timeout, int numOfInstances) throws IOException, InterruptedException {
         return installServiceAndWait(servicePath, serviceName, timeout, false, false, numOfInstances);
     }
 
     protected String installServiceAndWait(String servicePath, String serviceName, int timeout) throws IOException, InterruptedException {
         return installServiceAndWait(servicePath, serviceName, timeout, false);
     }
 
     protected String installServiceAndWait(String servicePath, String serviceName, int timeout , boolean expectToFail) throws IOException, InterruptedException {
         return installServiceAndWait(servicePath, serviceName, timeout, expectToFail, false, 0);
     }
 
     protected String installServiceAndWait(String servicePath, String serviceName, int timeout , boolean expectToFail, boolean disableSelfHealing, int numOfInstances) throws IOException, InterruptedException {
         ServiceInstaller serviceInstaller = new ServiceInstaller(getRestUrl(), serviceName);
         serviceInstaller.recipePath(servicePath);
         serviceInstaller.waitForFinish(true);
         serviceInstaller.expectToFail(expectToFail);
         serviceInstaller.timeoutInMinutes(timeout);
         serviceInstaller.setDisableSelfHealing(disableSelfHealing);
 
         if(StorageUtils.isInitialized()){
             StorageUtils.beforeServiceInstallation();
         }
 
         String output = serviceInstaller.install();
 
         if(StorageUtils.isInitialized()){
             StorageUtils.afterServiceInstallation(serviceName);
         }
 
         if(numOfInstances > 0){
             serviceInstaller.setInstances(numOfInstances);
         }
 
         return output;
     }
 
     protected String installServiceAndWait(String servicePath, String serviceName, boolean expectToFail) throws IOException, InterruptedException {
         return installServiceAndWait(servicePath, serviceName, SERVICE_INSTALLATION_TIMEOUT_IN_MINUTES, expectToFail);
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
             String cloudifyUser = null;
             String cloudifyPassword = null;
 
             url = restUrl + "/service/dump/machines/?fileSizeLimit=50000000";
             if (cloudService.getBootstrapper().isSecured()) {
                 cloudifyUser = SecurityConstants.USER_PWD_ALL_ROLES;
                 cloudifyPassword = SecurityConstants.USER_PWD_ALL_ROLES;
             }
             DumpUtils.dumpMachines(restUrl, cloudifyUser, cloudifyPassword);
 
         } catch (final Exception e) {
             LogUtils.log("Failed to create dump for this url - " + url, e);
         }
     }
 
     protected File getPemFile() {
         String cloudFolderPath = getService().getPathToCloudFolder();
         ComputeTemplate managementTemplate = getService().getCloud().getCloudCompute().getTemplates().get(getService().getCloud().getConfiguration().getManagementMachineTemplate());
         String keyFileName = managementTemplate.getKeyFile();
         String localDirectory = managementTemplate.getLocalDirectory();
         String keyFilePath = cloudFolderPath + "/" + localDirectory + "/" + keyFileName;
         final File keyFile = new File(keyFilePath);
         if(!keyFile.exists()) {
             throw new IllegalStateException("Could not find key file at expected location: " + keyFilePath);
         }
 
         if(!keyFile.isFile()) {
             throw new IllegalStateException("Expected key file: " + keyFile + " is not a file");
         }
         return keyFile;
 
     }
 }
