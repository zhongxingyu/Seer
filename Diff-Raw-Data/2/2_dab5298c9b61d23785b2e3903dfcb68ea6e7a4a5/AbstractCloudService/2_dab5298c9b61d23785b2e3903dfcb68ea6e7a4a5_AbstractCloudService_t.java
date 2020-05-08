 package test.cli.cloudify.cloud.services;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.FileUtils;
 
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
 	
 	private String cloudName;
 	protected int numberOfManagementMachines = 1;
 	protected URL[] restAdminUrls;
 	protected URL[] webUIUrls;
 	protected String serviceUniqueName;
 	protected String machinePrefix = CloudTestUtils.SGTEST_MACHINE_PREFIX + System.getProperty("user.name") + "_";
     protected Map<String,String> additionalPropsToReplace;
     protected Map<File, File> filesToReplace;
     protected boolean bootstrapped = false;
 	private String serviceFolder;
     
     public AbstractCloudService(String serviceUniqueName, String cloudName) {
     	this.serviceUniqueName = serviceUniqueName;
     	this.cloudName = cloudName;
     	serviceFolder = cloudName + "_" + serviceUniqueName;
     	filesToReplace = new HashMap<File, File>();
     }
     
 	public String getServiceFolder() {
 		return serviceFolder;
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
 
 	public void setAdditionalPropsToReplace(
 			Map<String, String> additionalPropsToReplace) {
 		this.additionalPropsToReplace = additionalPropsToReplace;
 	}
 	
 	public void addFilesToReplace(
 			Map<File, File> moreFilesToReplace) {
 		this.filesToReplace.putAll(moreFilesToReplace);
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
     
 	@Override
 	public String getCloudName() {
 		return cloudName;
 	}
 	
 	/**
 	 * @return the bootstrapped
 	 */
 	public boolean isBootstrapped() {
 		return bootstrapped;
 	}
 
 	/**
 	 * @param bootstrapped the bootstrapped to set
 	 */
 	public void setBootstrapped(boolean bootstrapped) {
 		this.bootstrapped = bootstrapped;
 	}
 
 	public abstract void injectServiceAuthenticationDetails() throws IOException;
 	
 	public void injectAuthenticationDetails() throws IOException {
 		createServiceFolders();
 		injectServiceAuthenticationDetails();
 		//update localDirectory
 		Map<String, String> propsToReplace = new HashMap<String,String>();
 		propsToReplace.put("localDirectory \"tools/cli/plugins/esc/" + cloudName + "/upload\"", "localDirectory \"" + RELATIVE_ESC_PATH + getServiceFolder() + "/upload\"");
 		
 		IOUtils.replaceTextInFile(getPathToCloudGroovy(), propsToReplace);
 	}
 	
 	/**
 	 * Create a new folder for the configuration files of the current service.
 	 * @param cloudName The name of the cloud (e.g. ec2, byon)
 	 * @param testUniqueName The unique name of the test, used to the folder naming
 	 * @return The created folder
 	 * @throws IOException Indicates the folder could not be created
 	 */
 	protected File createServiceFolders() throws IOException {
 		File originalCloudFolder = new File(ScriptUtils.getBuildPath() + RELATIVE_ESC_PATH + getCloudName());
 		File serviceCloudFolder = new File(originalCloudFolder.getParent(), getCloudName() + "_" + getUniqueName());
 		
 		try {
 			if (serviceCloudFolder.isDirectory()) {
 				FileUtils.deleteDirectory(serviceCloudFolder);
 			}
 			
 			//create a new folder for the test to work on (if it's not created already) with the content of the original folder
 			FileUtils.copyDirectory(originalCloudFolder, serviceCloudFolder);
 		} catch (IOException e) {
 			LogUtils.log("caught an exception while creating service folder " + serviceCloudFolder.getAbsolutePath(), e);
 			throw e;
 		}
 		
 		return serviceCloudFolder;
 	}
 	
 	/**
 	 * Deletes the temporary folder created earlier for the current service.
 	 * @throws IOException Indicates the folder could not be deleted
 	 */
 	protected void deleteServiceFolders() throws IOException {
 		File serviceCloudFolder = new File(ScriptUtils.getBuildPath() + RELATIVE_ESC_PATH + getServiceFolder());
 		try{
 			FileUtils.deleteDirectory(serviceCloudFolder);
 		} catch (IOException e) {
 			LogUtils.log("caught an exception while deleting service folder " + serviceCloudFolder.getAbsolutePath(), e);
 			throw e;
 		}
 	}
 	
     public URL getMachinesUrl(String url) throws Exception {
         return new URL(stripSlash(url) + "/admin/machines");
     }
     
     public String getPathToCloudGroovy() {
     	return getPathToCloudFolder() + "/" + getCloudName() + "-cloud.groovy";
     }
     
     public String getPathToCloudFolder() {
     	return ScriptUtils.getBuildPath() + "/tools/cli/plugins/esc/" + getServiceFolder();
     }
     
 	@Override
 	public void bootstrapCloud() throws IOException, InterruptedException {
 		
 		try {
 			overrideLogsFile();
 			injectAuthenticationDetails();
 			if (additionalPropsToReplace != null) {
 				IOUtils.replaceTextInFile(getPathToCloudGroovy(), additionalPropsToReplace);
 			}
 			if (filesToReplace != null) {
 				//replace files
 				for (Entry<File, File> fileToReplace : filesToReplace.entrySet()) {
 					//delete the old file
 					if (fileToReplace.getKey().exists()){
 						(fileToReplace.getKey()).delete();	
 					}
 					//copy the new file and use the name of the old file
 					FileUtils.copyFile(fileToReplace.getValue(), fileToReplace.getKey());
 				}
 			}
 			printCloudConfigFile();
 			String output = CommandTestUtils.runCommandAndWait("bootstrap-cloud --verbose " + getCloudName() + "_" + getUniqueName());
 			LogUtils.log("Extracting rest url's from cli output");
 			restAdminUrls = extractRestAdminUrls(output, numberOfManagementMachines);
 			LogUtils.log("Extracting webui url's from cli output");
 			webUIUrls = extractWebuiUrls(output, numberOfManagementMachines);
 			assertBootstrapServicesAreAvailable();
 			setBootstrapped(true);
 			
 			
 			URL machinesURL;
 			try {
 				for (int i = 0 ; i < numberOfManagementMachines ; i++) {
 					machinesURL = getMachinesUrl(restAdminUrls[i].toString());
 					LogUtils.log("Expecting " + numberOfManagementMachines + " machines");
 					LogUtils.log("Found " + CloudTestUtils.getNumberOfMachines(machinesURL) + " machines");
 					
 					//AssertUtils.assertEquals("Expecting " + numberOfManagementMachines + " machines", 
 					//		numberOfManagementMachines, CloudTestUtils.getNumberOfMachines(machinesURL));
 				}
 			} catch (Exception e) {
 				LogUtils.log("caught exception while geting number of management machines", e);
 				
 			}
 		} catch (Exception e) {
 			deleteCloudFiles(getCloudName());
 		}
 	}
 
 	private void printCloudConfigFile() throws IOException {
 		String pathToCloudGroovy = getPathToCloudGroovy();
 		File cloudConfigFile = new File(pathToCloudGroovy);
		if (!cloudConfigFile.exists()){
 			LogUtils.log("Failed to print the clou configuration file content");
 			return;
 		}
 		String cloudConfigFileAsString = FileUtils.readFileToString(cloudConfigFile);
 		LogUtils.log(cloudConfigFileAsString);
 	}
 
 	@Override
 	public void teardownCloud() throws IOException, InterruptedException {
 		
 		boolean teardownSuccesfull = false;
 		
 		try {
 			//injectAuthenticationDetails();
 			String[] restUrls = getRestUrls();
             String url = null;
 			if (restUrls != null) {
                 try {
                     url = restUrls[0] +"/service/dump/machines/?fileSizeLimit=50000000";
                     DumpUtils.dumpMachines(restUrls[0]);
                 } catch (Exception e) {
                     LogUtils.log("Failed to create dump for this url - " + url, e);
                 }
 				String connect = "connect " + restUrls[0];
 				String teardownOutput = CommandTestUtils.runCommandAndWait(connect + ";"  + "teardown-cloud --verbose " + getCloudName() + "_" + getUniqueName());
 				if (teardownOutput.toLowerCase().contains("success")) {
 					teardownSuccesfull = true;				
 				}
 			}
 		}
 		finally {
 			try {
 				if (!teardownSuccesfull) {
 					CommandTestUtils.runCommandAndWait("teardown-cloud --verbose -force " + getCloudName() + "_" + getUniqueName());				
 				}
 			}
 			finally {				
 				setBootstrapped(false);
 				try{
 					deleteServiceFolders();
 				} catch (IOException e) {
 					LogUtils.log("Failed to delete the service's custom folder", e);
 				}
 			}
 		}	
 	}
 	
 	@Override 
 	public String[] getWebuiUrls() {
 		if (webUIUrls == null) {
 			return null;
 		}
 		String[] result = new String[webUIUrls.length];
 		for (int i = 0 ; i < webUIUrls.length ; i++) {
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
 		for (int i = 0 ; i < restAdminUrls.length ; i++) {
 			result[i] = restAdminUrls[i].toString();
 		}
 		return result;
 	}
 
 	private URL[] extractRestAdminUrls(String output, int numberOfManagementMachines) throws MalformedURLException {
 		
 		URL[] restAdminUrls = new URL[numberOfManagementMachines];
 		
 		Pattern restPattern = Pattern.compile(CloudTestUtils.REST_URL_REGEX);
 		Matcher restMatcher = restPattern.matcher(output);
 		
 		// This is sort of hack.. currently we are outputting this over ssh and locally with different results
 		for (int i = 0; i < numberOfManagementMachines ; i++) {
 			AssertUtils.assertTrue("Could not find actual rest url", restMatcher.find());
 			String rawRestAdminUrl = restMatcher.group(1);
 			restAdminUrls[i] = new URL(rawRestAdminUrl);
 		}
 
 		return restAdminUrls;
 
 	}
 
 	private URL[] extractWebuiUrls(String cliOutput, int numberOfManagementMachines) throws MalformedURLException {
 		
 		URL[] webuiUrls = new URL[numberOfManagementMachines];
 		
 		Pattern webUIPattern = Pattern.compile(CloudTestUtils.WEBUI_URL_REGEX);
 		Matcher webUIMatcher = webUIPattern.matcher(cliOutput);
 		
 		// This is sort of hack.. currently we are outputting this over ssh and locally with different results
 		for (int i = 0; i < numberOfManagementMachines ; i++) {
 			AssertUtils.assertTrue("Could not find actual webui url", webUIMatcher.find());
 			String rawWebUIUrl = webUIMatcher.group(1);
 			webuiUrls[i] = new URL(rawWebUIUrl);
 		}
 		
 		return webuiUrls;
 	}
 	
 	private void deleteCloudFiles(String cloudName) throws IOException {
 		
 		/*File cloudPluginDir = new File(ScriptUtils.getBuildPath() , "tools/cli/plugins/esc/" + cloudName + "/");
 		File originalCloudDslFile = new File(cloudPluginDir, cloudName + "-cloud.groovy");
 		File backupCloudDslFile = new File(cloudPluginDir, cloudName + "-cloud.backup");
 		File targetPemFolder = new File(ScriptUtils.getBuildPath(), "tools/cli/plugins/esc/" + cloudName + "/upload/");
 		File cloudifyOverrides = new File(cloudPluginDir.getAbsolutePath() + "/upload/cloudify-overrides");
 		File tempDslFolder = new File(cloudPluginDir.getAbsolutePath() + "/tmp");
 		
 		// delete pem files from upload dir
 		for (File file : targetPemFolder.listFiles()) {
 			if (file.getName().contains(".pem")) {
 				FileUtils.deleteQuietly(file);
 				break;
 			}
 		}
 		
 		// delete cloudify-overrides if exists
 		if (cloudifyOverrides.exists()) {
 			FileUtils.deleteDirectory(cloudifyOverrides);
 		}
 		
 		// make backup file the only file
 		String currentDate = new Date().toString().replace(" ", "_").replace(":", "_");
 		if (!tempDslFolder.exists()) {
 			tempDslFolder.mkdir();
 		}
 		FileUtils.moveFile(originalCloudDslFile, new File(tempDslFolder.getAbsolutePath() + File.separator + cloudName +  currentDate + "-cloud.groovy"));
 		FileUtils.copyFile(backupCloudDslFile, originalCloudDslFile);
 		FileUtils.deleteQuietly(backupCloudDslFile);*/
 	}
 
 	
 	private void assertBootstrapServicesAreAvailable() throws MalformedURLException {
 		
 		for (int i = 0; i < restAdminUrls.length; i++) {
 			// The rest home page is a JSP page, which will fail to compile if there is no JDK installed. So use testrest instead
 			assertWebServiceAvailable(new URL( restAdminUrls[i].toString() + "/service/testrest"));
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
 
     
     private void overrideLogsFile() throws IOException {
     	File logging = new File(SGTestHelper.getSGTestRootDir() + "/config/gs_logging.properties");
     	File uploadOverrides = new File(ScriptUtils.getBuildPath() + "/tools/cli/plugins/esc/" + getCloudName() + "/upload/cloudify-overrides/");
     	uploadOverrides.mkdir();
     	File uploadLoggsDir = new File(uploadOverrides.getAbsoluteFile() + "/config/");
     	uploadLoggsDir.mkdir();
     	FileUtils.copyFileToDirectory(logging, uploadLoggsDir);
     }
 	
     private static String stripSlash(String str) {
         if (str == null || !str.endsWith("/")) {
             return str;
         }
         return str.substring(0, str.length()-1);
     }
     
 	public abstract String getUser();
 	
 	public abstract String getApiKey();
 	
 	public String getUniqueName() {
 		return serviceUniqueName;
 	}
 }
