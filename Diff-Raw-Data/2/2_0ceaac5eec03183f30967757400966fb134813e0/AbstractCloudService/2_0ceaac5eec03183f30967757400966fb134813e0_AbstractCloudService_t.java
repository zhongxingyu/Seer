 package test.cli.cloudify.cloud.services;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.FileUtils;
 import org.cloudifysource.dsl.cloud.Cloud;
 import org.cloudifysource.dsl.internal.ServiceReader;
 import org.openspaces.admin.Admin;
 
 import test.cli.cloudify.CloudTestUtils;
 import test.cli.cloudify.CommandTestUtils;
 import framework.tools.SGTestHelper;
 import framework.utils.AssertUtils;
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 import framework.utils.DumpUtils;
 import framework.utils.IOUtils;
 import framework.utils.LogUtils;
 import framework.utils.ScriptUtils;
 import framework.utils.WebUtils;
 
 public abstract class AbstractCloudService implements CloudService {
 
 	protected static final String RELATIVE_ESC_PATH = "/tools/cli/plugins/esc/";
 	protected static final String UPLOAD_FOLDER = "upload";
 	
 	private static final int TEN_SECONDS_IN_MILLIS = 10000;
 	
 	private static final int MAX_SCAN_RETRY = 3;
 
 	protected boolean noWebServices; 
 	
 	private int numberOfManagementMachines = 1;
 	private URL[] restAdminUrls;
 	private URL[] webUIUrls;
	private String machinePrefix;
 	private Map<String, String> additionalPropsToReplace = new HashMap<String,String>();
 	private Cloud cloud;
 	private Map<String,Object> properties = new HashMap<String,Object>();	
 	private String cloudName;
 	private String cloudFolderName;
 	
 	private String cloudUniqueName = this.getClass().getSimpleName();
 
 	public boolean isNoWebServices(){
 		return noWebServices;
 	}
 
 	public void setNoWebServices(boolean noWebServices){
 		this.noWebServices = noWebServices;
 	}
 	
 	public Map<String,Object> getProperties() {
 		return properties;
 	}
 	
 	public Cloud getCloud() {
 		return cloud;
 	}
 
 	public AbstractCloudService(String cloudName) {
 		this.cloudName = cloudName;
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
 		this.machinePrefix = machinePrefix;
 	}
 
 
 	public void setCloudName(String cloudName) {
 		this.cloudName = cloudName;
 	}
 
 	public abstract void injectCloudAuthenticationDetails() throws IOException;
 	
 	public abstract String getUser();
 
 	public abstract String getApiKey();
 
 	@Override
 	public void init(final String uniqueName) throws IOException {
 		this.cloudUniqueName = uniqueName;
 		this.cloudFolderName = cloudName + "_" + cloudUniqueName;
 		createCloudFolder();
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
 	public String getCloudName() {
 		return cloudName;
 	}
 	
 	public String getPathToCloudGroovy() {
 		return getPathToCloudFolder() + "/" + getCloudName() + "-cloud.groovy";
 	}
 
 	public String getPathToCloudFolder() {
 		return ScriptUtils.getBuildPath() + RELATIVE_ESC_PATH + cloudFolderName;
 	}
 	
 	@Override
 	public void teardownCloud() throws IOException, InterruptedException {
 
 		try {
 			String[] restUrls = getRestUrls();
 			String url = null;
 			if (restUrls != null) {
 				try {
 					url = restUrls[0] + "/service/dump/machines/?fileSizeLimit=50000000";
 					DumpUtils.dumpMachines(restUrls[0]);
 				} catch (Exception e) {
 					LogUtils.log("Failed to create dump for this url - " + url, e);
 				}
 				String connect = "connect " + restUrls[0];
 				CommandTestUtils.runCommandAndWait(connect + ";" + "teardown-cloud --verbose " + this.cloudName + "_" + this.cloudUniqueName);
 			}
 		} 
 		finally {
 			deleteServiceFolders();
 			scanForLeakedAgentAndManagementNodes();
 		}
 	}
 	
 	@Override
 	public void teardownCloud(Admin admin) throws IOException ,InterruptedException {
 		try {
 			DumpUtils.dumpLogs(admin);
 			CommandTestUtils.runCommandAndWait("teardown-cloud -force --verbose " + this.cloudName + "_" + this.cloudUniqueName);			
 		}
 		finally {			
 			deleteServiceFolders();
 			scanForLeakedAgentAndManagementNodes();
 		}
 	}
 	
 	@Override
 	public void bootstrapCloud() throws Exception {
 		
 		overrideLogsFile();
 		injectCloudAuthenticationDetails();
 		replaceProps();
 		
 		writePropertiesToCloudFolder(getProperties());
 		// Load updated configuration file into POJO
 		this.cloud = ServiceReader.readCloud(new File(getPathToCloudGroovy()));
 
 		scanForLeakedAgentAndManagementNodes();
 		
 		printCloudConfigFile();
 		
 		if(!noWebServices){
 			String output = CommandTestUtils.runCommandAndWait("bootstrap-cloud --verbose " + this.cloudName + "_" + this.cloudUniqueName);			
 			restAdminUrls = extractRestAdminUrls(output, numberOfManagementMachines);
 			this.webUIUrls = extractWebuiUrls(output, numberOfManagementMachines);
 			assertBootstrapServicesAreAvailable();
 		} else {
 			CommandTestUtils.runCommandAndWait("bootstrap-cloud -no-web-services --verbose " + this.cloudName + "_" + this.cloudUniqueName);
 		}
 
 		URL machinesURL;
 
 		for (int i = 0; i < numberOfManagementMachines; i++) {
 			machinesURL = getMachinesUrl(restAdminUrls[i].toString());
 			LogUtils.log("Expecting " + numberOfManagementMachines + " machines");
 			LogUtils.log("Found " + CloudTestUtils.getNumberOfMachines(machinesURL) + " machines");
 		}
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
 		
 		boolean leakedAgentAndManagementNodesScanResult = false;
 		for (int i = 0 ; i < MAX_SCAN_RETRY ; i++) {
 			try {
 				leakedAgentAndManagementNodesScanResult = scanLeakedAgentAndManagementNodes();
 				break;
 			} catch (final Throwable t) {
 				LogUtils.log("Failed scaning for leaked nodes. attempt number " + (i + 1) , t);
 			}
 		}
 		AssertUtils.assertTrue("Leaked nodes were found!", leakedAgentAndManagementNodesScanResult);
 	} 
 	
 	private void writePropertiesToCloudFolder(Map<String, Object> properties) throws IOException {
 		
 		Properties props = new Properties();
 		for (Map.Entry<String, Object> entry : properties.entrySet()) {
 			Object value = entry.getValue();
 			String key = entry.getKey();
 			String actualValue = null;
 			if (value instanceof String) {
 				actualValue = '"' + value.toString() + '"';
 			} else {
 				actualValue = value.toString();
 			}
 			props.setProperty(key, actualValue);
 		}
 		// add a properties file to the cloud driver
 		IOUtils.writePropertiesToFile(props, new File(getPathToCloudFolder() + "/" + getCloudName() + "-cloud.properties"));
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
 		} 
 		catch (IOException e) {
 			LogUtils.log("caught an exception while creating service folder " + serviceCloudFolder.getAbsolutePath(), e);
 			throw e;
 		}
 
 		return serviceCloudFolder;
 	}
 
 	private void deleteServiceFolders()
 			throws IOException {
 		File serviceCloudFolder = new File(getPathToCloudFolder());
 		try {
 			FileUtils.deleteDirectory(serviceCloudFolder);
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
 
 	private URL[] extractRestAdminUrls(String output, int numberOfManagementMachines)
 			throws MalformedURLException {
 
 		URL[] restAdminUrls = new URL[numberOfManagementMachines];
 
 		Pattern restPattern = Pattern.compile(CloudTestUtils.REST_URL_REGEX);
 		Matcher restMatcher = restPattern.matcher(output);
 
 		// This is sort of hack.. currently we are outputting this over ssh and locally with different results
 		for (int i = 0; i < numberOfManagementMachines; i++) {
 			AssertUtils.assertTrue("Could not find actual rest url", restMatcher.find());
 			String rawRestAdminUrl = restMatcher.group(1);
 			restAdminUrls[i] = new URL(rawRestAdminUrl);
 		}
 
 		return restAdminUrls;
 
 	}
 
 	private URL[] extractWebuiUrls(String cliOutput, int numberOfManagementMachines)
 			throws MalformedURLException {
 
 		URL[] webuiUrls = new URL[numberOfManagementMachines];
 
 		Pattern webUIPattern = Pattern.compile(CloudTestUtils.WEBUI_URL_REGEX);
 		Matcher webUIMatcher = webUIPattern.matcher(cliOutput);
 
 		// This is sort of hack.. currently we are outputting this over ssh and locally with different results
 		for (int i = 0; i < numberOfManagementMachines; i++) {
 			AssertUtils.assertTrue("Could not find actual webui url", webUIMatcher.find());
 			String rawWebUIUrl = webUIMatcher.group(1);
 			webuiUrls[i] = new URL(rawWebUIUrl);
 		}
 
 		return webuiUrls;
 	}
 
 	private void assertBootstrapServicesAreAvailable()
 			throws MalformedURLException {
 
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
 				} catch (Exception e) {
 					return false;
 				}
 			}
 		}, CloudTestUtils.OPERATION_TIMEOUT);
 	}
 
 	private void overrideLogsFile()
 			throws IOException {
 		File logging = new File(SGTestHelper.getSGTestRootDir() + "/config/gs_logging.properties");
 		File uploadOverrides =
 				new File(getPathToCloudFolder() + "/upload/cloudify-overrides/");
 		uploadOverrides.mkdir();
 		File uploadLoggsDir = new File(uploadOverrides.getAbsoluteFile() + "/config/");
 		uploadLoggsDir.mkdir();
 		FileUtils.copyFileToDirectory(logging, uploadLoggsDir);
 	}
 
 	private static String stripSlash(String str) {
 		if (str == null || !str.endsWith("/")) {
 			return str;
 		}
 		return str.substring(0, str.length() - 1);
 	}
 }
