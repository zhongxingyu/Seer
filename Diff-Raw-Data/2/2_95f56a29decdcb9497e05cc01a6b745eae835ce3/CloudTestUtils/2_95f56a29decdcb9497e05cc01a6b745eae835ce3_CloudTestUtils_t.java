 package org.cloudifysource.quality.iTests.test.cli.cloudify;
 
 import java.io.File;
 import java.io.IOException;
 
 import iTests.framework.tools.SGTestHelper;
 import iTests.framework.utils.WebUtils;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.j_spaces.kernel.PlatformVersion;
 
 import iTests.framework.utils.DeploymentUtils;
 import iTests.framework.utils.IOUtils;
 import iTests.framework.utils.LogUtils;
 import iTests.framework.utils.DumpUtils;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.cloudifysource.esc.driver.provisioning.byon.ByonProvisioningDriver;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.CloudService;
 import org.cloudifysource.restclient.GSRestClient;
 
 public class CloudTestUtils {
 
 
 	public static final String WEBUI_URL_REGEX= "Webui service is available at: (http[s]*://(.*):[0-9]*)";
 	public static final String REST_URL_REGEX= "Rest service is available at: (http[s]*://(.*):[0-9]*)";
 
     // unbelievably retarded. but works. regex is evil.
     public static final String WEBUI_PRIVATE_URL_REGEX = " - Webui service is available at: (http[s]*://(.*):[0-9]*)";
     public static final String REST_PRIVATE_URL_REGEX = " - Rest service is available at: (http[s]*://(.*):[0-9]*)";
 
 	public final static long OPERATION_TIMEOUT = 5 * 60 * 1000;	
 
     private static File zipFile;
 
     public static int getNumberOfMachines(URL machinesRestAdminUrl) throws Exception {
 		String json = WebUtils.getURLContent(machinesRestAdminUrl);
 		LogUtils.log("-- getNumberOfMachines received json text: " + json);
 		return parseNumberOfMachines(json);
 	}
 
 	public static int getNumberOfMachines(URL machinesRestAdminUrl, String username, String password) throws Exception {
 		String json = WebUtils.getURLContent(machinesRestAdminUrl, username, password);
 		LogUtils.log("-- getNumberOfMachines received json text: " + json);
 		return parseNumberOfMachines(json);
 	}
 	
 	private static int parseNumberOfMachines(String json) {
 		Matcher matcher = Pattern.compile("\"Size\":\"([0-9]+)\"").matcher(json);
 		if (matcher.find()) {
 			String rawSize = matcher.group(1);
 			int size = Integer.parseInt(rawSize);
 			return size;
 		} else {
 			return 0;
 		}
 	}
 
     public static URL[] extractPublicRestUrls(final String cliOutput,
                                               final int numberOfManagementMachines) throws MalformedURLException {
         List<URL> privateUrls = asList(extractPrivateRestUrls(cliOutput, 1)); // only one private ip is printed
         List<URL> allUrls = asList(extractAllMatchingUrls(cliOutput, numberOfManagementMachines, CloudTestUtils.REST_URL_REGEX));
         for (URL url : privateUrls) {
             allUrls.remove(url);
         }
         URL[] publicUrls = new URL[numberOfManagementMachines];
         return allUrls.toArray(publicUrls);
     }
 
     public static URL[] extractPublicWebuiUrls(final String cliOutput,
                                                final int numberOfManagementMachines) throws MalformedURLException {
         List<URL> privateUrls = asList(extractPrivateWebuiUrls(cliOutput, 1)); // only one private ip is printed
         List<URL> allUrls = asList(extractAllMatchingUrls(cliOutput, numberOfManagementMachines, CloudTestUtils.WEBUI_URL_REGEX));
         for (URL url : privateUrls) {
             allUrls.remove(url);
         }
         URL[] publicUrls = new URL[numberOfManagementMachines];
         return allUrls.toArray(publicUrls);
     }
 
     private static URL[] extractUrls(final String cliOutput, int numberOfManagementMachines, String regex)
             throws MalformedURLException {
         URL[] webuiUrls = new URL[numberOfManagementMachines];
 
         Pattern webUIPattern = Pattern.compile(regex);
         Matcher webUIMatcher = webUIPattern.matcher(cliOutput);
 
         // This is sort of hack.. currently we are outputting this over ssh and locally with different results
         for (int i = 0; i < numberOfManagementMachines; i++) {
             if (webUIMatcher.find()) {
                 String rawWebUIUrl = webUIMatcher.group(1);
                 webuiUrls[i] = new URL(rawWebUIUrl);
             } else {
                 LogUtils.log("Could not find Pattern machine regex : " + regex);
             }
         }
 
         return webuiUrls;
 
     }
 
     private static URL[] extractAllMatchingUrls(final String cliOutput, int numberOfManagementMachines, String regex) throws MalformedURLException {
 
         URL[] urls = new URL[numberOfManagementMachines + 1];
 
         Pattern pattern = Pattern.compile(regex);
         Matcher matcher = pattern.matcher(cliOutput);
 
         // This is sort of hack.. currently we are outputting this over ssh and locally with different results
         for (int i = 0; i < (numberOfManagementMachines + 1); i++) {
             if (matcher.find()) {
                 String rawWebUIUrl = matcher.group(1);
                 urls[i] = new URL(rawWebUIUrl);
             } else {
                 LogUtils.log("Could not find Pattern machine regex : " + regex);
             }
         }
 
         return urls;
     }
 
     private static URL[] extractPrivateWebuiUrls(final String cliOutput, int numberOfManagementMachines)
             throws MalformedURLException {
         return extractUrls(cliOutput, numberOfManagementMachines, CloudTestUtils.WEBUI_PRIVATE_URL_REGEX);
     }
 
     private static URL[] extractPrivateRestUrls(final String cliOutput, int numberOfManagementMachines)
             throws MalformedURLException {
         return extractUrls(cliOutput, numberOfManagementMachines, CloudTestUtils.REST_PRIVATE_URL_REGEX);
     }
 
 
     private static List<URL> asList(URL[] array) {
 
         List<URL> list = new ArrayList<URL>();
         for (URL url : array) {
             list.add(url);
         }
         return list;
     }
 
     public static void dumpMachines(String restUrl) throws Exception {
         dumpMachines(restUrl, "", "");
     }
 
     public static void dumpMachines(String restUrl, String username, String password) throws Exception {
         LogUtils.log("Downloading machines dump");
         String machinesDumpUri = "/service/dump/machines/";
         if (StringUtils.isBlank(username)) {
             username = "";
         }
         if (StringUtils.isBlank(password)) {
             password = "";
         }
 
         GSRestClient rc = new GSRestClient(username, password, new URL(restUrl), PlatformVersion.getVersionNumber());
         DateFormat date1 = new SimpleDateFormat("dd-MM-yyyy");
         DateFormat hour = new SimpleDateFormat("HH-mm-ss-SSS");
         Map<String, Object> resultsMap = (Map) rc.get(machinesDumpUri);
         LogUtils.log("Machines dump downloaded successfully");
 
         for (String key : resultsMap.keySet()) {
             Date date = new Date();
             byte[] result = Base64.decodeBase64(resultsMap.get(key).toString());
             zipFile = new File(DumpUtils.getTestFolder().getAbsolutePath() + "/" + date1.format(date) + "_" + hour.format(date) + "_ip" + key.toString() + "_dump.zip");
             FileUtils.writeByteArrayToFile(zipFile, result);
             LogUtils.log("> Logs: " + zipFile.getAbsolutePath() + "\n");
         }
     }
     
     public static void replaceCloudDriverImplementation(CloudService service, String oldClassName, String newClassName, String newJarName, String newJarVersion) throws IOException {
     	String s = System.getProperty("file.separator");
         String repoQualityItests = DeploymentUtils.getQualityItestsPath(s);
         // copy custom location aware driver to cloudify-overrides
        File locationAwareDriver = new File (repoQualityItests +s+newJarName+s+newJarVersion+s+newJarName+"-"+newJarVersion+".jar");
         File uploadOverrides =
                 new File(service.getPathToCloudFolder() + "/upload/cloudify-overrides/");
         if (!uploadOverrides.exists()) {
             uploadOverrides.mkdir();
         }
 
         File uploadEsmDir = new File(uploadOverrides.getAbsoluteFile() + "/lib/platform/esm");
         File localEsmFolder = new File(SGTestHelper.getBuildDir() + "/lib/platform/esm");
 
         FileUtils.copyFileToDirectory(locationAwareDriver, uploadEsmDir, true);
         FileUtils.copyFileToDirectory(locationAwareDriver, localEsmFolder, false);
 
         final Map<String, String> propsToReplace = new HashMap<String, String>();
 
         propsToReplace.put(appendClassNamePrefix(oldClassName),appendClassNamePrefix(newClassName));
         IOUtils.replaceTextInFile(service.getPathToCloudGroovy(), propsToReplace);
     }
     
     private static String appendClassNamePrefix(String className) {
         return "className \""+className+"\"";
     }
 
 }
