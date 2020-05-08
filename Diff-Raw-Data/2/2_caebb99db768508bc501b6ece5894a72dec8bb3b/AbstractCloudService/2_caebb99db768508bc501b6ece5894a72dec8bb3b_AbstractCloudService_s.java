 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services;
 
 import iTests.framework.tools.SGTestHelper;
 import iTests.framework.utils.AssertUtils;
 import iTests.framework.utils.AssertUtils.RepetitiveConditionProvider;
 import iTests.framework.utils.IOUtils;
 import iTests.framework.utils.LogUtils;
 import iTests.framework.utils.SSHUtils;
 import iTests.framework.utils.ScriptUtils;
 import iTests.framework.utils.WebUtils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.cloudifysource.domain.cloud.Cloud;
 import org.cloudifysource.dsl.internal.ServiceReader;
 import org.cloudifysource.quality.iTests.framework.utils.CloudBootstrapper;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CloudTestUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils;
 import org.openspaces.admin.Admin;
 import org.testng.Assert;
 
 public abstract class AbstractCloudService implements CloudService {
 
     private static final int MAX_HOSTNAME_LENGTH = 35;
     private static final int MAX_VOLUME_NAME_LENGTH = 45;
     protected static final String RELATIVE_ESC_PATH = "/clouds/";
     protected static final String UPLOAD_FOLDER = "upload";
     protected static final String CREDENTIALS_FOLDER = System.getProperty("iTests.credentialsFolder",
             SGTestHelper.getSGTestRootDir() + "/src/main/resources/credentials");
     protected static final boolean enableLogstash = Boolean.parseBoolean(System.getProperty("iTests.enableLogstash", "false"));
     private static File propsFile = new File(SGTestHelper.getSGTestRootDir() + "/src/main/resources/logstash/logstash.properties");
 
     private static final int TEN_SECONDS_IN_MILLIS = 10000;
 
     private static final int MAX_SCAN_RETRY = 3;
 
     private int numberOfManagementMachines = 1;
     private URL[] webUIUrls;
     private String machinePrefix;
     private String volumePrefix;
     private Map<String, String> additionalPropsToReplace = new HashMap<String, String>();
     private Cloud cloud;
     private Map<String, Object> properties = new HashMap<String, Object>();
     private String cloudName;
     private String cloudFolderName;
     private String cloudUniqueName = this.getClass().getSimpleName();
     private CloudBootstrapper bootstrapper = new CloudBootstrapper();
 	private File customCloudGroovy;
 
     public AbstractCloudService(String cloudName) {
         this.cloudName = cloudName;
     }
 
     public CloudBootstrapper getBootstrapper() {
         return bootstrapper;
     }
 
     public void setBootstrapper(CloudBootstrapper bootstrapper) {
         bootstrapper.provider(this.cloudFolderName);
         this.bootstrapper = bootstrapper;
 
     }
 
     @Override
     public void beforeBootstrap() throws Exception {
     }
 
 
     public Map<String, Object> getProperties() {
         return properties;
     }
 
     public Cloud getCloud() {
         return cloud;
     }
 
 
     public int getNumberOfManagementMachines() {
         return numberOfManagementMachines;
     }
 
     public void setNumberOfManagementMachines(int numberOfManagementMachines) {
         this.numberOfManagementMachines = numberOfManagementMachines;
     }
 
     public Map<String, String> getAdditionalPropsToReplace() {
         return additionalPropsToReplace;
     }
 
     public String getMachinePrefix() {
         return machinePrefix;
     }
 
     public void setMachinePrefix(String machinePrefix) {
 
         if (machinePrefix.length() > MAX_HOSTNAME_LENGTH) {
             String substring = machinePrefix.substring(0, MAX_HOSTNAME_LENGTH - 1);
             LogUtils.log("machinePrefix " + machinePrefix + " is too long. using " + substring + " as actual machine prefix");
             this.machinePrefix = substring;
         } else {
             this.machinePrefix = machinePrefix;
         }
     }
 
     public String getVolumePrefix() {
         return volumePrefix;
     }
 
     public void setVolumePrefix(String volumePrefix) {
 
         if (volumePrefix.length() > MAX_VOLUME_NAME_LENGTH) {
             String substring = volumePrefix.substring(0, MAX_VOLUME_NAME_LENGTH - 1);
             LogUtils.log("volumePrefix " + volumePrefix + " is too long. using " + substring + " as actual volume prefix");
             this.volumePrefix = substring;
         } else {
             this.volumePrefix = volumePrefix;
         }
     }
 
     public void setCloudName(String cloudName) {
         this.cloudName = cloudName;
     }
 
     public abstract void injectCloudAuthenticationDetails() throws IOException;
 
     public abstract String getUser();
 
     public abstract String getApiKey();
 
     @Override
     public void init(final String uniqueName) throws Exception {
 
         this.cloudUniqueName = uniqueName.toLowerCase();
         this.cloudFolderName = cloudName + "_" + cloudUniqueName;
         bootstrapper.provider(this.cloudFolderName);
         bootstrapper.setCloudService(this);
         deleteServiceFolders();
         createCloudFolder();
 
         if(enableLogstash){
             prepareLogstash(uniqueName);
         }
     }
 
     private void prepareLogstash(String tagClassName) throws Exception {
 
         String pathToLogstash = SGTestHelper.getSGTestRootDir() + "/src/main/resources/logstash";
         String preBootstrapScriptPath = getPathToCloudFolder() + "/upload/pre-bootstrap.sh";
 
         if(cloudName.equalsIgnoreCase("byon")){
             IOUtils.copyFile(pathToLogstash + "/byon/pre-bootstrap.sh", preBootstrapScriptPath);
         }
         else{
             IOUtils.copyFile(pathToLogstash + "/pre-bootstrap.sh", preBootstrapScriptPath);
         }
 
         String confFilePath = pathToLogstash + "/logstash-shipper.conf";
         String backupFilePath = IOUtils.backupFile(confFilePath);
 
         //TODO fix path to build
         String remoteBuildPath;
         if(cloudName.equalsIgnoreCase("byon")){
             remoteBuildPath = "/tmp/byon/gigaspaces";
         }
         else{
             remoteBuildPath = "/home/ec2-user/gigaspaces";
         }
 
         IOUtils.replaceTextInFile(confFilePath, "<path_to_build>", remoteBuildPath);
         IOUtils.replaceTextInFile(confFilePath, "<test_name>", tagClassName);
         IOUtils.replaceTextInFile(confFilePath, "<build_number>", System.getProperty("iTests.buildNumber"));
         IOUtils.replaceTextInFile(confFilePath, "<version>", System.getProperty("cloudifyVersion"));
         IOUtils.replaceTextInFile(confFilePath, "<suite_name>", System.getProperty("iTests.suiteName"));
 
         String logstashConfInBuildPath = getPathToCloudFolder() + "/upload/cloudify-overrides/config/logstash";
         FileUtils.forceMkdir(new File(logstashConfInBuildPath));
         IOUtils.copyFile(confFilePath, logstashConfInBuildPath + "/logstash-shipper.conf");
 
         IOUtils.replaceFileWithMove(new File(confFilePath), new File(backupFilePath));
 
     }
 
     @Override
     public boolean scanLeakedAgentNodes() {
         return true;
     }
 
     @Override
     public String[] getWebuiUrls() {
         if (webUIUrls == null) {
             return null;
         }
         String[] result = new String[webUIUrls.length];
         for (int i = 0; i < webUIUrls.length; i++) {
             result[i] = webUIUrls[i].toString();
         }
         return result;
     }
 
     @Override
     public String[] getRestUrls() {
 
         URL[] restAdminUrls = getBootstrapper().getRestAdminUrls();
         if (restAdminUrls == null) {
             return null;
         }
         String[] result = new String[restAdminUrls.length];
         for (int i = 0; i < restAdminUrls.length; i++) {
             result[i] = restAdminUrls[i].toString();
         }
         return result;
     }
 
     @Override
     public boolean scanLeakedAgentAndManagementNodes() {
         return true;
     }
     
     @Override
     public boolean scanLeakedManagementNodes() {
         return true;
     }
 
     @Override
     public String getCloudName() {
         return cloudName;
     }
 
     public String getPathToCloudGroovy() {
         return getPathToCloudFolder() + "/" + getCloudName() + "-cloud.groovy";
     }
 
     public void setCloudGroovy(File cloudFile) throws IOException {
     	this.customCloudGroovy = cloudFile;
     }
 
     public String getPathToCloudFolder() {
         return ScriptUtils.getBuildPath() + RELATIVE_ESC_PATH + cloudFolderName;
     }
 
     @Override
     public void teardownCloud() throws IOException, InterruptedException {
 
         String[] restUrls = getRestUrls();
 
         try {
             String url = null;
             if (restUrls != null) {
 
                 try {
                     url = restUrls[0] + "/service/dump/machines/?fileSizeLimit=50000000";
                 } catch (Exception e) {
                     LogUtils.log("Failed to create dump for this url - " + url, e);
                 }
 
                 if (!bootstrapper.isForce()) {
                     // this is to connect to the cloud before tearing down.
                     bootstrapper.setRestUrl(restUrls[0]);
                 }
                 bootstrapper.teardown();
             }
         } finally {
             if (!bootstrapper.isTeardownExpectedToFail()) {
                 scanForLeakedAgentAndManagementNodes();
             } else {
                 // machines were not supposed to be terminated.
             }
         }
 
         if(enableLogstash){
 
             LogUtils.log("cloud name: " + cloudName);
             if(cloudName.equalsIgnoreCase("byon")){
                 for(String restUrl : this.getRestUrls()){
                     LogUtils.log("mng url: " + restUrl);
                     int startIndex;
                     int endIndex;
 
                     if(System.getProperty("iTests.suiteName").contains("ipv6")){
                         LogUtils.log("using ipv6 address");
                         startIndex = restUrl.indexOf("[") + 1;
                         endIndex  = restUrl.lastIndexOf("]");
                     }
                     else{
                         startIndex = restUrl.indexOf(":") + 3;
                         endIndex = restUrl.lastIndexOf(":");
                     }
 
                     if(startIndex > 0 && endIndex > -1){
 
                         String hostIp = restUrl.substring(startIndex, endIndex);
 
                         LogUtils.log("destroying logstash agent on " + hostIp);
                         SSHUtils.runCommand(hostIp, SSHUtils.DEFAULT_TIMEOUT, "pkill -9 -f logstash", "tgrid", "tgrid");
                     }
                     else{
                         throw new IllegalStateException("could not resolve host address from " + restUrl);
                     }
                 }
             }
 
             else{
                 // killing any remaining logstash agent connections
                 Properties props;
                 try {
                     props = IOUtils.readPropertiesFromFile(propsFile);
                 } catch (final Exception e) {
                     throw new IllegalStateException("Failed reading properties file : " + e.getMessage());
                 }
                 String logstashHost = props.getProperty("logstash_server_host");
                File pemFile = new File(SGTestHelper.getSGTestRootDir() + "/src/main/resources/credentials/cloud/ec2/ec2-sgtest-us-east-logstash.pem");
                 String redisSrcDir = "/home/ec2-user/redis-2.6.14/src";
                 String user = "ec2-user";
                 long timeoutMilli = 20 * 1000;
 
                 String output = SSHUtils.runCommand(logstashHost, timeoutMilli, "cd " + redisSrcDir + "; ./redis-cli client list", user, pemFile);
                 int ipAndPortStartIndex;
                 int ipAndPortEndIndex;
                 int currentIndex = 0;
                 String ipAndPort;
 
                 while(true){
 
                     ipAndPortStartIndex = output.indexOf("addr=", currentIndex) + 5;
 
                     if(ipAndPortStartIndex == 4){
                         break;
                     }
 
                     ipAndPortEndIndex = output.indexOf(" ", ipAndPortStartIndex);
                     ipAndPort = output.substring(ipAndPortStartIndex, ipAndPortEndIndex);
                     currentIndex = ipAndPortEndIndex;
 
                     if(restUrls != null){
                         for(String restUrl : restUrls){
 
                             int ipStartIndex = restUrl.indexOf(":") + 3;
                             String ip = restUrl.substring(ipStartIndex, restUrl.indexOf(":", ipStartIndex));
                             if(ipAndPort.contains(ip)){
                                 LogUtils.log("shutting down redis client on " + ipAndPort);
                                 SSHUtils.runCommand(logstashHost, timeoutMilli, "cd " + redisSrcDir + "; ./redis-cli client kill " + ipAndPort, user, pemFile);
                             }
                         }
                     }
                 }
             }
         }
     }
 
     @Override
     public void teardownCloud(Admin admin) throws IOException, InterruptedException {
         try {
 
             CommandTestUtils.runCommandAndWait("teardown-cloud -force --verbose " + this.cloudName + "_" + this.cloudUniqueName);
         } finally {
             scanForLeakedAgentAndManagementNodes();
         }
     }
 
     @Override
     public String bootstrapCloud() throws Exception {
 
     	String output = "";
         overrideLogsFile();
         if (customCloudGroovy != null) {
         	// use a custom grooyv file if defined
         	File originalCloudGroovy = new File(getPathToCloudGroovy());
         	IOUtils.replaceFile(originalCloudGroovy, customCloudGroovy);
         }
         injectCloudAuthenticationDetails();
         replaceProps();
 
         writePropertiesToCloudFolder(getProperties());
         // Load updated configuration file into POJO
         this.cloud = ServiceReader.readCloud(new File(getPathToCloudGroovy()));
 
         if(bootstrapper.isScanForLeakedNodes()){
             scanForLeakedAgentAndManagementNodes();
         }
 
         beforeBootstrap();
 
         printCloudConfigFile();
 
         printPropertiesFile();
 
         bootstrapper.setNumberOfManagementMachines(numberOfManagementMachines);
         if (bootstrapper.isNoWebServices()) {
             output = bootstrapper.bootstrap().getOutput();
         } else {
             output = bootstrapper.bootstrap().getOutput();
             if (bootstrapper.isBootstrapExpectedToFail()) {
                 return output;
             }
             this.webUIUrls = CloudTestUtils.extractPublicWebuiUrls(output, numberOfManagementMachines);
             LogUtils.log("webuiUrls = " + StringUtils.join(webUIUrls, ","));
             assertBootstrapServicesAreAvailable();
 
             URL machinesURL;
 
             for (int i = 0; i < numberOfManagementMachines; i++) {
                 machinesURL = getMachinesUrl(getBootstrapper().getRestAdminUrls()[i].toString());
                 LogUtils.log("Expecting " + numberOfManagementMachines + " machines");
                 if (bootstrapper.isSecured()) {
                     LogUtils.log("Found " + CloudTestUtils.getNumberOfMachines(machinesURL, bootstrapper.getUser(), bootstrapper.getPassword()) + " machines");
                 } else {
                     LogUtils.log("Found " + CloudTestUtils.getNumberOfMachines(machinesURL) + " machines");
                 }
             }
         }
         
         return output;
     }
 
     private void printPropertiesFile() throws IOException {
         LogUtils.log(FileUtils.readFileToString(new File(getPathToCloudFolder(), getCloudName() + "-cloud.properties")));
     }
 
     private void scanForLeakedAgentAndManagementNodes() {
 
         if (cloud == null) {
             return;
         }
         // We will give a short timeout to give the ESM
         // time to recognize that he needs to shutdown the machine.
 
         try {
             Thread.sleep(TEN_SECONDS_IN_MILLIS);
         } catch (InterruptedException e) {
         }
 
         Throwable first = null;
         for (int i = 0; i < MAX_SCAN_RETRY; i++) {
             try {
                 boolean leakedAgentAndManagementNodesScanResult = scanLeakedAgentAndManagementNodes();
                 if (leakedAgentAndManagementNodesScanResult == true) {
                     return;
                 } else {
                     Assert.fail("Leaked nodes were found!");
                 }
                 break;
             } catch (final Exception t) {
                 first = t;
                 LogUtils.log("Failed scaning for leaked nodes. attempt number " + (i + 1), t);
             }
         }
         if (first != null) {
             Assert.fail("Failed scanning for leaked nodes after " + MAX_SCAN_RETRY + " attempts. First exception was --> " + first.getMessage(), first);
         }
     }
 
     private void writePropertiesToCloudFolder(Map<String, Object> properties) throws IOException {
         // add a properties file to the cloud driver
         IOUtils.writePropertiesToFile(properties, new File(getPathToCloudFolder() + "/" + getCloudName() + "-cloud.properties"));
     }
 
     private File createCloudFolder() throws IOException {
         File originalCloudFolder = new File(ScriptUtils.getBuildPath() + RELATIVE_ESC_PATH + getCloudName());
         File serviceCloudFolder = new File(originalCloudFolder.getParent(), cloudFolderName);
 
         try {
             if (serviceCloudFolder.isDirectory()) {
                 FileUtils.deleteDirectory(serviceCloudFolder);
             }
 
             // create a new folder for the test to work on (if it's not created already) with the content of the
             // original folder
             LogUtils.log("copying " + originalCloudFolder + " to " + serviceCloudFolder);
             FileUtils.copyDirectory(originalCloudFolder, serviceCloudFolder);
         } catch (IOException e) {
             LogUtils.log("caught an exception while creating service folder " + serviceCloudFolder.getAbsolutePath(), e);
             throw e;
         }
 
         return serviceCloudFolder;
     }
 
     private void deleteServiceFolders()
             throws IOException {
         File serviceCloudFolder = new File(getPathToCloudFolder());
         try {
             if (serviceCloudFolder.exists()) {
                 FileUtils.deleteDirectory(serviceCloudFolder);
             }
         } catch (IOException e) {
             LogUtils.log("caught an exception while deleting service folder " + serviceCloudFolder.getAbsolutePath(), e);
             throw e;
         }
     }
 
     private void replaceProps() throws IOException {
         if (additionalPropsToReplace != null) {
             IOUtils.replaceTextInFile(getPathToCloudGroovy(), additionalPropsToReplace);
         }
     }
 
 
     private void printCloudConfigFile() throws IOException {
         String pathToCloudGroovy = getPathToCloudGroovy();
         File cloudConfigFile = new File(pathToCloudGroovy);
         if (!cloudConfigFile.exists()) {
             LogUtils.log("Failed to print the cloud configuration file content");
             return;
         }
         String cloudConfigFileAsString = FileUtils.readFileToString(cloudConfigFile);
         LogUtils.log("Cloud configuration file: " + cloudConfigFile.getAbsolutePath());
         LogUtils.log(cloudConfigFileAsString);
     }
 
     private URL getMachinesUrl(String url)
             throws Exception {
         return new URL(stripSlash(url) + "/admin/machines");
     }
 
     private void assertBootstrapServicesAreAvailable()
             throws MalformedURLException {
 
         LogUtils.log("Waiting for bootstrap web services to be available");
         URL[] restAdminUrls = getBootstrapper().getRestAdminUrls();
         for (int i = 0; i < restAdminUrls.length; i++) {
             // The rest home page is a JSP page, which will fail to compile if there is no JDK installed. So use
             // testrest instead
             assertWebServiceAvailable(new URL(restAdminUrls[i].toString() + "/service/testrest"));
             assertWebServiceAvailable(webUIUrls[i]);
         }
 
     }
 
     private static void assertWebServiceAvailable(final URL url) {
         AssertUtils.repetitiveAssertTrue(url + " is not up", new RepetitiveConditionProvider() {
 
             public boolean getCondition() {
                 try {
                     return WebUtils.isURLAvailable(url);
                 } catch (final Exception e) {
                     LogUtils.log(url + " is not available yet");
                     return false;
                 }
             }
         }, CloudTestUtils.OPERATION_TIMEOUT);
     }
 
     private void overrideLogsFile()
             throws IOException {
         File logging = new File(SGTestHelper.getSGTestRootDir() + "/src/main/config/gs_logging.properties");
         File uploadOverrides =
                 new File(getPathToCloudFolder() + "/upload/cloudify-overrides/");
         uploadOverrides.mkdir();
         File uploadLoggsDir = new File(uploadOverrides.getAbsoluteFile() + "/config/");
         uploadLoggsDir.mkdir();
         FileUtils.copyFileToDirectory(logging, uploadLoggsDir);
 
         File originalGsLogging = new File(SGTestHelper.getBuildDir() + "/config/gs_logging.properties");
         if (originalGsLogging.exists()) {
             originalGsLogging.delete();
         }
         FileUtils.copyFile(logging, originalGsLogging);
     }
 
     private static String stripSlash(String str) {
         if (str == null || !str.endsWith("/")) {
             return str;
         }
         return str.substring(0, str.length() - 1);
     }
 
     protected Properties getCloudProperties(String propertiesFileName) {
         Properties properties = new Properties();
         try {
             properties.load(new FileInputStream(propertiesFileName));
         } catch (IOException e) {
             throw new RuntimeException("failed to read " + propertiesFileName + " file - " + e, e);
         }
 
 
         return properties;
     }
 }
