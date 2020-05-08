 package test.cli.cloudify.cloud.services;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Date;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import framework.utils.*;
 import org.apache.commons.io.FileUtils;
 
 import test.cli.cloudify.CloudTestUtils;
 import test.cli.cloudify.CommandTestUtils;
 import framework.tools.SGTestHelper;
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 
 public abstract class AbstractCloudService implements CloudService {
 	
 	protected int numberOfManagementMachines = 1;
 	protected URL[] restAdminUrls;
 	protected URL[] webUIUrls;
 	protected String machinePrefix = CloudTestUtils.SGTEST_MACHINE_PREFIX + System.getProperty("user.name") + "_";
     protected Map<String,String> additionalPropsToReplace;
     protected boolean bootstrapped = false;
     
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
     
 	public String getMachinePrefix() {
 		return machinePrefix;
 	}
 	
 	public void setMachinePrefix(String machinePrefix) {
 		this.machinePrefix = machinePrefix;
 	}
 	
     
 	public abstract String getCloudName();
 	
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
 
 	public abstract void injectAuthenticationDetails() throws IOException;
 	
     public URL getMachinesUrl(String url) throws Exception {
         return new URL(stripSlash(url) + "/admin/machines");
     }
     
 	@Override
 	public void bootstrapCloud() throws IOException, InterruptedException {
 		
 		try {
 			overrideLogsFile();
 			injectAuthenticationDetails();
 			if (additionalPropsToReplace != null) {
 				String pathToCloudGroovy = ScriptUtils.getBuildPath() + "/tools/cli/plugins/esc/" + getCloudName() + "/" + getCloudName() + "-cloud.groovy";
 				IOUtils.replaceTextInFile(pathToCloudGroovy, additionalPropsToReplace);
 			}
 			String output = CommandTestUtils.runCommandAndWait("bootstrap-cloud --verbose " + getCloudName());
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
 					AssertUtils.assertEquals("Expecting " + numberOfManagementMachines + " machines", 
 							numberOfManagementMachines, CloudTestUtils.getNumberOfMachines(machinesURL));
 				}
 			} catch (Exception e) {
 				LogUtils.log("caught exception while geting number of management machines", e);
 			}
 		}
 		finally {
 			// restore to original state to allow for other tests to execute different bootstrap
 			deleteCloudFiles(getCloudName());
 		}
 	}
 
 	@Override
 	public void teardownCloud() throws IOException, InterruptedException {
 		
 		boolean teardownSuccesfull = false;
 		
 		try {
 			injectAuthenticationDetails();
 			String[] restUrls = getRestUrls();
             String url = null;
 			if (restUrls != null) {
                 try {
                    url = restUrls[0] +"/service/dump/?fileSizeLimit=50000000";
                     DumpUtils.dump(new URL(url));
                 } catch (Exception e) {
                     LogUtils.log("Failed to create dump for this url - " + url, e);
                 }
 				String connect = "connect " + restUrls[0];
 				String teardownOutput = CommandTestUtils.runCommandAndWait(connect + ";"  + "teardown-cloud --verbose " + getCloudName());
 				if (teardownOutput.toLowerCase().contains("success")) {
 					teardownSuccesfull = true;				
 				}
 			}
 		}
 		finally {
 			try {
 				if (!teardownSuccesfull) {
 					CommandTestUtils.runCommandAndWait("teardown-cloud --verbose -force " + getCloudName());				
 				}
 			}
 			finally {				
 				setBootstrapped(false);
 				deleteCloudFiles(getCloudName());
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
 		
 		File cloudPluginDir = new File(ScriptUtils.getBuildPath() , "tools/cli/plugins/esc/" + cloudName + "/");
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
 		FileUtils.deleteQuietly(backupCloudDslFile);
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
 }
