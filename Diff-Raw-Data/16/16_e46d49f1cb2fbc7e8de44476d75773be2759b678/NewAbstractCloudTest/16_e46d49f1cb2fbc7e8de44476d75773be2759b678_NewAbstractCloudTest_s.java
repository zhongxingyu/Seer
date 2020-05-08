 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud;
 
 import iTests.framework.utils.AssertUtils;
 import iTests.framework.utils.LogUtils;
 import iTests.framework.utils.ScriptUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.cloudifysource.domain.cloud.Cloud;
 import org.cloudifysource.domain.cloud.compute.ComputeTemplate;
 import org.cloudifysource.domain.cloud.network.CloudNetwork;
 import org.cloudifysource.domain.cloud.network.ManagementNetwork;
 import org.cloudifysource.domain.cloud.network.NetworkConfiguration;
 import org.cloudifysource.domain.cloud.network.Subnet;
 import org.cloudifysource.dsl.internal.CloudifyConstants;
 import org.cloudifysource.quality.iTests.framework.utils.ApplicationInstaller;
 import org.cloudifysource.quality.iTests.framework.utils.CloudBootstrapper;
 import org.cloudifysource.quality.iTests.framework.utils.ServiceInstaller;
 import org.cloudifysource.quality.iTests.framework.utils.compute.ComputeApiHelper;
 import org.cloudifysource.quality.iTests.framework.utils.compute.JcloudsComputeApiHelper;
 import org.cloudifysource.quality.iTests.framework.utils.compute.OpenstackComputeApiHelper;
 import org.cloudifysource.quality.iTests.framework.utils.network.NetworkApiHelper;
 import org.cloudifysource.quality.iTests.framework.utils.network.OpenstackNetworkApiHelper;
 import org.cloudifysource.quality.iTests.framework.utils.storage.Ec2StorageApiHelper;
 import org.cloudifysource.quality.iTests.framework.utils.storage.OpenstackStorageApiHelper;
 import org.cloudifysource.quality.iTests.framework.utils.storage.StorageApiHelper;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.CloudService;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.CloudServiceManager;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.ec2.Ec2CloudService;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.hpgrizzly.HpGrizzlyCloudService;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.security.SecurityConstants;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.util.CloudTestUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.util.exceptions.FailedToCreateDumpException;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.util.exceptions.WrongMessageException;
 import org.cloudifysource.restclient.exceptions.RestClientException;
 import org.openspaces.admin.Admin;
 import org.testng.Assert;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 
 public abstract class NewAbstractCloudTest extends AbstractTestSupport {
 
     public static final int SERVICE_INSTALLATION_TIMEOUT_IN_MINUTES = 10;
 
     private static final int TEN_SECONDS_IN_MILLIS = 10000;
     private static final int MAX_SCAN_RETRY = 3;
 	private static final String CIDR_SEPARATOR = "/";
 	private static final String GATEWAY_OPTION_KEY = "gateway";
 	private static final String NULL_GATEWAY = "null";
 
     protected CloudService cloudService;
     protected ComputeApiHelper computeApiHelper;
     protected StorageApiHelper storageApiHelper;
     protected NetworkApiHelper networkApiHelper;
 
     protected void customizeCloud() throws Exception {
     	
     	if (this.networkApiHelper != null) {
     		
         	// set unused subnet ranges to avoid overlapping existing subnets
             Map<String, String> propsToReplace = this.cloudService.getAdditionalPropsToReplace();
             CloudNetwork cloudNetwork = this.cloudService.getCloud().getCloudNetwork();
             
             if (cloudNetwork != null) {
             	// set valid IP ranges for the management network
             	ManagementNetwork managementNetwork = cloudNetwork.getManagement();
             	if (managementNetwork != null) {
             		// replace subnets is they are already used by this tenant (e.g. by another test)
             		setValidNetworkSettings(propsToReplace, managementNetwork.getNetworkConfiguration());
             	}
             	
                 // do the same for the application networks
             	Map<String, NetworkConfiguration> networkTemplates = cloudNetwork.getTemplates();
             	if (networkTemplates != null) {
             		for (NetworkConfiguration networkConfig : networkTemplates.values()) {
             			setValidNetworkSettings(propsToReplace, networkConfig);
             		}
             	}
             }
         }
     }
 
     protected void beforeBootstrap() throws Exception {}
     
     protected void parseBootstrapOutput(String bootstrapOutput) throws Exception {}
 
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
         String cloudifySourceDir = getCloudifySourceDir() + "cloudify";
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
 
     //this is here to notify logstash to kill its test agent
     @AfterMethod
     public void aferMethod(){
         LogUtils.log("after method");
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
 
         this.cloudService.init(this.getClass().getSimpleName());
 
         LogUtils.log("Customizing cloud");
 
         if (cloudService.supportsComputeApi()) {
         	computeApiHelper = initComputeHelper(cloudService);
         }
         
         if (cloudService.supportsStorageApi()) {
         	storageApiHelper = initStorageHelper(cloudService);
         }
         
         if (cloudService.supportNetworkApi()) {
         	networkApiHelper = initNetworkHelper(cloudService);	
         }
 
         customizeCloud();
 
         beforeBootstrap();
 
         String bootstrapOutput = this.cloudService.bootstrapCloud();
         parseBootstrapOutput(bootstrapOutput);
 
         if(!cloudService.getBootstrapper().isBootstrapExpectedToFail()) {
             afterBootstrap();
         }
     }
 
     
     private ComputeApiHelper initComputeHelper(final CloudService cloudService) {
     	
     	final Cloud cloud = cloudService.getCloud();
     	final String cloudName = cloudService.getCloudName();
     	    	
        if (cloudName.equals("ec2") || cloudName.equals("hp-folsom") || cloudName.equals("rackspace")) {
             return new JcloudsComputeApiHelper(cloud, cloudService.getRegion());
         } else if (cloudName.equals("hp-grizzly")) {
         	final String managementTemplateName = cloud.getConfiguration().getManagementMachineTemplate();
         	return new OpenstackComputeApiHelper(cloud, managementTemplateName);
         }
         
         throw new UnsupportedOperationException("Cannot init compute helper for non jclouds or Openstack providers!");
     }
     
 
     private StorageApiHelper initStorageHelper(final CloudService cloudService) {
     	
     	final Cloud cloud = cloudService.getCloud();
     	final String cloudName = cloudService.getCloudName();
     	
         if (cloudName.equals("ec2")) {
             return new Ec2StorageApiHelper(cloud, "SMALL_LINUX", ((Ec2CloudService) cloudService).getRegion(),
                     ((Ec2CloudService) cloudService).getComputeServiceContext());
         } else if (cloudName.equals("hp-grizzly")) {
             return new OpenstackStorageApiHelper(cloud, cloud.getConfiguration().getManagementMachineTemplate(),
             		((HpGrizzlyCloudService) cloudService).getComputeServiceContext());
         }
         
         throw new UnsupportedOperationException("Cannot init storage helper for clouds that are not ec2 or Openstack");
     }
     
     
     private NetworkApiHelper initNetworkHelper(final CloudService cloudService) {
     	
     	final Cloud cloud = cloudService.getCloud();
     	final String cloudName = cloudService.getCloudName();
     	
     	if (cloudName.equals("hp-grizzly")) {
             return new OpenstackNetworkApiHelper(cloud, "SMALL_LINUX");
     	} else {
     		return null;
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
 
     protected void doSanityTest(String applicationFolderName, String applicationName) throws Exception {
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
 
     protected String uninstallApplicationAndWait(String applicationName) throws Exception {
         return uninstallApplicationAndWait(applicationName, false);
     }
     
     protected String uninstallApplicationAndWait(String applicationName, boolean isExpectedFail) throws Exception {
     	return uninstallApplicationAndWait(applicationName, isExpectedFail, 5);
     }
     
     protected String uninstallApplicationAndWait(String applicationName, boolean isExpectedFail,
                                                  int timeout) throws Exception {
         ApplicationInstaller applicationInstaller = new ApplicationInstaller(getRestUrl(), applicationName);
         applicationInstaller.waitForFinish(true);
         applicationInstaller.timeoutInMinutes(timeout);
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
 
 
     protected String uninstallServiceAndWait(String serviceName, boolean isExpectedFail) throws Exception {
         ServiceInstaller serviceInstaller = new ServiceInstaller(getRestUrl(), serviceName);
         serviceInstaller.waitForFinish(true);
         serviceInstaller.expectToFail(isExpectedFail);
         String output = serviceInstaller.uninstall();
         return output;
     }
     
     protected String uninstallServiceAndWait(String serviceName) throws Exception {
     	return uninstallServiceAndWait(serviceName, false);
     }
 
     protected void uninstallServiceIfFound(String serviceName) throws IOException, InterruptedException {
         ServiceInstaller serviceInstaller = new ServiceInstaller(getRestUrl(), serviceName);
         serviceInstaller.waitForFinish(true);
         serviceInstaller.uninstallIfFound();
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
 
     protected String installServiceAndWait(String servicePath, String serviceName, int timeout , boolean expectToFail, boolean disableSelfHealing, int numOfInstances) throws IOException, InterruptedException {
         ServiceInstaller serviceInstaller = new ServiceInstaller(getRestUrl(), serviceName);
         serviceInstaller.recipePath(servicePath);
         serviceInstaller.waitForFinish(true);
         serviceInstaller.expectToFail(expectToFail);
         serviceInstaller.timeoutInMinutes(timeout);
         serviceInstaller.setDisableSelfHealing(disableSelfHealing);
 
         String output = serviceInstaller.install();
 
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
             Assert.fail("Failed to find a working rest URL. tried : " + StringUtils.join(restUrls, ","));
         }
         return finalUrl;
     }
 
     protected String getWebuiUrl() {
         return cloudService.getWebuiUrls()[0];
     }
 
     protected void dumpMachines()
             throws RestClientException, WrongMessageException, FailedToCreateDumpException, IOException {
 
         final boolean enableLogstash = Boolean.parseBoolean(System.getProperty("iTests.enableLogstash", "false"));
         if(enableLogstash){
             return;
         }
 
         final String restUrl = getRestUrl();
         String cloudifyUser = null;
         String cloudifyPassword = null;
 
         if (cloudService.getBootstrapper().isSecured()) {
         	cloudifyUser = SecurityConstants.USER_PWD_ALL_ROLES;
         	cloudifyPassword = SecurityConstants.USER_PWD_ALL_ROLES;
         }
         CloudTestUtils.dumpMachinesNewRestAPI(restUrl, cloudifyUser, cloudifyPassword);
 
         
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
 
     public static String getCloudifySourceDir() {
         String referencePath = ScriptUtils.getClassLocation(CloudifyConstants.class);
         String ans = "";
         String[] split = referencePath.split("/");
         String s = File.separator;
         for (int i = 0; i < split.length - 8; i++) {
             ans += split[i] + s;
         }
         int startIndex = 1;
         if(ScriptUtils.isLinuxMachine())
             startIndex = 0;
         return ans.substring(startIndex, ans.length());
     }
     
     
     /**
      * Validates the set subnet ranges are not already being used. If they are - sets valid (unused) ranges instead.
      * Also, sets the gateway IP according to the subnet range.
      * 
      * @param propsToReplace a map of properties to replace
      * @param networkConfiguration the network configuration to validate
      * @throws Exception Indicates a valid subnet range could not be found
      */
 	private void setValidNetworkSettings(Map<String, String> propsToReplace, NetworkConfiguration networkConfiguration)
 			throws Exception {
 		
 		List<Subnet> subnets = networkConfiguration.getSubnets();
 		for (Subnet subnet : subnets) {
 			String origSubnetRange = StringUtils.substringBefore(subnet.getRange(), CIDR_SEPARATOR);
 			String validSubnetRange = networkApiHelper.getValidSubnetRange(subnet.getRange());
 			if (!origSubnetRange.equalsIgnoreCase(validSubnetRange)) {
 				propsToReplace.put(origSubnetRange, validSubnetRange);	
 			}
 			
 			// replace the gateway accordingly
 			Map<String, String> options = subnet.getOptions();
 			if (options != null && options.size() > 0) {
 				String origGateway = options.get(GATEWAY_OPTION_KEY);
 				if (StringUtils.isNotBlank(origGateway) && !origGateway.equalsIgnoreCase(NULL_GATEWAY)) {
 					String validGateway = networkApiHelper.getGatewayForSubnet(validSubnetRange);
 					if (!origGateway.equalsIgnoreCase(validGateway)) {
 						propsToReplace.put(origGateway, validGateway);						
 					}
 				}
 			}
 		}
 	}
 	
 }
