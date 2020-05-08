 package iTests.framework.testng.report;
 
 import iTests.framework.testng.report.xml.TestLog;
 import iTests.framework.tools.S3DeployUtil;
 import iTests.framework.tools.SGTestHelper;
 import iTests.framework.utils.IOUtils;
 import iTests.framework.utils.LogUtils;
 import iTests.framework.utils.TestNGUtils;
 import org.apache.commons.io.FileUtils;
 import org.elasticsearch.action.search.SearchResponse;
 import org.elasticsearch.client.Client;
 import org.elasticsearch.client.transport.TransportClient;
 import org.elasticsearch.common.transport.InetSocketTransportAddress;
 import org.elasticsearch.index.query.QueryBuilders;
 import org.elasticsearch.search.SearchHit;
 import org.elasticsearch.search.sort.SortOrder;
 import org.testng.ITestResult;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.*;
 
 import static org.elasticsearch.search.sort.SortBuilders.fieldSort;
 
 /**
  * @author moran, nirb
  */
 public class LogFetcher {
     private static final String BUILD_FOLDER_KEY = "sgtest.buildFolder";
     private static final String REPORT_URL_KEY = "iTests.url";
     private static final String GIGASPACES_QUALITY_S3 = "http://gigaspaces-quality.s3.amazonaws.com/";
     boolean isCloudEnabled = Boolean.parseBoolean(System.getProperty("iTests.cloud.enabled", "false"));
     private static final boolean enableLogstash = Boolean.parseBoolean(System.getProperty("iTests.enableLogstash", "false"));
     protected static final String CREDENTIALS_FOLDER = System.getProperty("iTests.credentialsFolder",SGTestHelper.getSGTestRootDir() + "/src/main/resources/credentials");
     private static File propsFile = new File(CREDENTIALS_FOLDER + "/logstash/logstash.properties");
     private String suiteName;
 
     public LogFetcher() {
     }
 
     public List<TestLog> getLogs(ITestResult result) {
         return getLogs(null, result);
     }
 
     public List<TestLog> getLogs(TestStatus testStatus, ITestResult result) {
         List<TestLog> logs = new ArrayList<TestLog>();
         suiteName = System.getProperty("iTests.suiteName");
         String buildNumber = System.getProperty("iTests.buildNumber");
         String testName = TestNGUtils.constructTestMethodName(result);
         File testDir = new File(getBuildFolder() + "/" + suiteName + "/" + testName);
 
         String className = result.getTestClass().getRealClass().getSimpleName();
         File testFolder = getTestFolder(testName);
 
         if(enableLogstash && testStatus != null && (testStatus.equals(TestStatus.PASSED_TEST) ||
                                                     testStatus.equals(TestStatus.FAILED_TEST) ||
                                                     testStatus.equals(TestStatus.SKIPPED_TEST))){
             try {
                 getTestLogstashLogs(buildNumber, className, testName);
             } catch (IOException e) {
                 e.printStackTrace();
             }
 
             LogUtils.log("uploading to s3 from " + testFolder);
             S3DeployUtil.uploadLogFile(testFolder, buildNumber, suiteName, testName);
         }
 
         if(isCloudEnabled && !enableLogstash){
             S3DeployUtil.uploadLogFile(testDir, buildNumber, suiteName, testName);
         }
 
         if(enableLogstash){
             List<TestLog> testLogs = fetchLogs(testFolder, logs);
             FileUtils.deleteQuietly(testFolder);
             return testLogs;
         }
         return fetchLogs(testDir, logs);
     }
 
     private String getBuildFolder() {
         return System.getProperty(BUILD_FOLDER_KEY);
     }
 
     private List<TestLog> fetchLogs(File dir, List<TestLog> list) {
         LogUtils.log("Fetching logs from the parent directory: " + dir.getAbsolutePath());
         try {
             File[] children = dir.listFiles();
             if (children == null)
                 return list;
             for (int n = 0; n < children.length; n++) {
                 File file = children[n];
                 if (file.getName().endsWith((".log")) || file.getName().endsWith(".png")) {
                     TestLog testLog = new TestLog(file.getName(),
                             getFileUrl(file.getAbsolutePath()));
                     list.add(testLog);
                     continue;
                 } else if (file.isDirectory()
                         && file.getName().equalsIgnoreCase("logs")) {
                     File[] logs = file.listFiles();
                     for (int i = 0; i < logs.length; i++) {
                         TestLog testLog = new TestLog(logs[i].getName(),
                                 getFileUrl(logs[i].getAbsolutePath()));
                         list.add(testLog);
                     }
                     break;
                 } else if (file.isDirectory()
                         && !file.getName().contains(".zip")) {
                     fetchLogs(file, list);
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return list;
     }
 
     private String getFileUrl(String path) {
         LogUtils.log("file path is: " + path);
         int index = path.indexOf("build_");
         String ans;
         if(index == -1){
             String[] split = path.split("cloudify-itests");
             ans = getUrl() + System.getProperty("iTests.buildNumber") + split[1];
             LogUtils.log("index of build_ is: " + index + " ,and current url is " + ans);
             index = ans.indexOf(System.getProperty("iTests.buildNumber"));
             LogUtils.log("the index within ans of the build number is: " + index);
         }
         else{
             ans = getUrl() + path.substring(index);
         }
 
         if(enableLogstash){
             ans = ans.replace("../", "");
             LogUtils.log("current url (after removing ../) is " + ans);
             StringBuilder finalUrl = new StringBuilder(ans);
             int suiteStartIndex = ans.indexOf("/", index) + 1;
 
             LogUtils.log("inserting suiteName at: " + suiteStartIndex);
             finalUrl.insert(suiteStartIndex, suiteName + "/");
             LogUtils.log("final url is " + finalUrl);
 
             ans = finalUrl.toString();
         }
 
         return ans;
     }
 
     private String getUrl() {
         if(isCloudEnabled || enableLogstash){
             return GIGASPACES_QUALITY_S3;
         }else{
             return System.getProperty(REPORT_URL_KEY);
         }
     }
 
     public static void getTestLogstashLogs(String buildNumber, String className, String testName) throws IOException {
 
         try {
             File testFolder = getTestFolder(testName);
 
             if(testFolder.exists()){
                 FileUtils.deleteQuietly(testFolder);
             }
 
             testFolder.mkdir();
             LogUtils.log("(fetch logs) created test folder in " + testFolder.getAbsolutePath());
 
             LogUtils.log("querying and creating logs the class " + className);
             createLogs(className, buildNumber, testFolder);
 
             LogUtils.log("querying and creating logs the test " + testName);
             createLogs(testName, buildNumber, testFolder);
 
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private static File getTestFolder(String testName){
         File testFolder = new File(SGTestHelper.getSGTestRootDir() + "/../" + testName);
         return testFolder;
     }
 
     private static void createLogs(String tagToSearch, String buildNumber, File testFolder) throws IOException {
 
         Set<String> fileNames = new HashSet<String>();
 
         Properties props;
         try {
             LogUtils.log("SGTEST ROOT DIR IS - " + SGTestHelper.getSGTestRootDir());
 
             props = IOUtils.readPropertiesFromFile(propsFile);
         } catch (final Exception e) {
             throw new IllegalStateException("Failed reading properties file : " + e.getMessage());
         }
 
         String logstashServerHost = props.getProperty("logstash_server_host");
         LogUtils.log("logstash server host is: " + logstashServerHost);
 
         int logstashServerPort = Integer.parseInt(props.getProperty("logstash_server_port", "9300"));
         LogUtils.log("logstash server port is: " + logstashServerPort);
 
         Client client = new TransportClient().addTransportAddress(new InetSocketTransportAddress(logstashServerHost, logstashServerPort));
 
         int hitsPerSearch = 200;
         int currentOffset = 0;
         long startTimeMillis = System.currentTimeMillis();
 
         while (true) {
 
             String query = "tags:\"" + tagToSearch + "\" AND tags:\"" + buildNumber + "\"";
             LogUtils.log("query: " + query);
 
             try {
                 SearchResponse response = client.prepareSearch()
                         .setQuery(QueryBuilders.queryString(query))
                         .setFrom(currentOffset).setSize(hitsPerSearch).execute().actionGet();
 
                 for (SearchHit hit : response.getHits()) {
                     Object sourcePathObject = hit.getSource().get("path");
 
                     if(sourcePathObject == null){
                         continue;
                     }
 
                     String sourcePath = sourcePathObject.toString();
 
                     int fileNameIndex = sourcePath.lastIndexOf("/") + 1;
                     String fileName = sourcePath.substring(fileNameIndex);
                     fileNames.add(fileName);
                 }
                 //Break condition: No hits are returned
                 if (response.getHits().hits().length == 0) {
                     break;
                 }
 
             } catch(Exception e){
 
                 LogUtils.log("a problem occurred while querying " + query, e);
                 break;
             }
 
             currentOffset += hitsPerSearch;
 
         }
 
         for(String fileName : fileNames){
             LogUtils.log("file name found: " + fileName);
         }
 
         //found file names. Now performing query for each one.
         for(String fileName : fileNames){
 
             File methodFile = new File(testFolder.getAbsolutePath() + "/" + fileName);
             LogUtils.log("creating file " + methodFile.getAbsolutePath());
             methodFile.createNewFile();
 
             FileWriter fw = new FileWriter(methodFile.getAbsoluteFile());
             BufferedWriter bw = new BufferedWriter(fw);
 
             currentOffset = 0;
             int index = 0;
 
             while (true) {
 
                 LogUtils.log("retrieving " + currentOffset + " to " + (currentOffset+hitsPerSearch));
 
 
                 try{
 
                     SearchResponse resp = client.prepareSearch()
                             .setQuery(QueryBuilders.queryString("tags:\"" + tagToSearch + "\" AND tags:\"" + buildNumber + "\" AND path:\"" + fileName + "\""))
                            .setFrom(currentOffset).setSize(hitsPerSearch).addSort(fieldSort("timestamp").order(SortOrder.ASC)).execute().actionGet();
 
                     LogUtils.log("size: " + resp.getHits().getTotalHits());
                     LogUtils.log("iteration size: " + resp.getHits().hits().length);
 
                     String message;
 
                     for (SearchHit hit : resp.getHits()) {
 
                         message = hit.getSource().get("message").toString();
 
                         bw.write(message + "\n");
                         index++;
                     }
 
                     //Break condition: No hits are returned
                     if (resp.getHits().hits().length == 0) {
                         break;
                     }
                 } catch(Exception e){
                     LogUtils.log("a problem occurred while querying", e);
                     break;
                 }
 
                 currentOffset += hitsPerSearch;
             }
 
             bw.flush();
             bw.close();
             fw.close();
         }
 
         long endTimeMillis = System.currentTimeMillis();
         LogUtils.log("all queries took " + (endTimeMillis - startTimeMillis)/1000 + " seconds");
 
     }
 
 }
