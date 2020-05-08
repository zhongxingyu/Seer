 package framework.utils;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.cloudifysource.restclient.GSRestClient;
 import org.openspaces.admin.Admin;
 import org.openspaces.admin.dump.DumpResult;
 
 import com.gigaspaces.internal.dump.heap.HeapDumpProcessor;
 import com.gigaspaces.internal.dump.log.LogDumpProcessor;
 import com.gigaspaces.internal.dump.pu.ProcessingUnitsDumpProcessor;
 import com.gigaspaces.internal.dump.summary.SummaryDumpProcessor;
 import com.gigaspaces.internal.dump.thread.ThreadDumpProcessor;
 import com.j_spaces.kernel.PlatformVersion;
 
 import framework.tools.SGTestHelper;
 
 
 public class DumpUtils {
 
     private static File testFolder;
     private static File zipFile;
    private static File buildFolder;
 
     public static void dumpALL(Admin admin) {
         dump(admin, null, getAllDumpOptions());
     }
 
     public static void dumpLogs(Admin admin) {
         dump(admin, null, LogDumpProcessor.NAME);
     }
 
     public static void dumpThreads(Admin admin) {
         dump(admin, null, ThreadDumpProcessor.NAME);
     }
 
     public static void dumpHeap(Admin admin) {
         dump(admin, null, HeapDumpProcessor.NAME);
     }
 
     public static void dumpProcessingUnit(Admin admin) {
         dump(admin, null, ProcessingUnitsDumpProcessor.NAME);
     }
 
     public static void dump(Admin admin, String cause, String... dumpOptions) {
         try {
             DumpResult result = admin.generateDump(cause, null, dumpOptions);
             Date date = new Date();
             DateFormat date1 = new SimpleDateFormat("dd-MM-yyyy");
             DateFormat hour = new SimpleDateFormat("HH-mm-ss-SSS");
             zipFile = new File(getTestFolder().getAbsolutePath() + "/" + date1.format(date) + "_" + hour.format(date) + "_dump.zip");
             result.download(zipFile, null);
             LogUtils.log("> Logs: " + zipFile.getAbsolutePath() + "\n");
         } catch (Exception e) {
             LogUtils.log("Dump Failed", e);
         }
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
     		zipFile = new File(getTestFolder().getAbsolutePath() + "/" + date1.format(date) + "_" + hour.format(date) + "_ip" + key.toString() + "_dump.zip");
     		FileUtils.writeByteArrayToFile(zipFile, result);
     		LogUtils.log("> Logs: " + zipFile.getAbsolutePath() + "\n");
     	}
     }
 
     private static String[] getAllDumpOptions() {
         List<String> dumpOptionsList = new ArrayList<String>(5);
         dumpOptionsList.add(SummaryDumpProcessor.NAME);
         dumpOptionsList.add(ThreadDumpProcessor.NAME);
         dumpOptionsList.add(LogDumpProcessor.NAME);
         dumpOptionsList.add(ProcessingUnitsDumpProcessor.NAME);
         dumpOptionsList.add(HeapDumpProcessor.NAME);
         String[] dumpOptionsArray = new String[dumpOptionsList.size()];
         dumpOptionsList.toArray(dumpOptionsArray);
         return dumpOptionsArray;
     }
 
     public static File createTestFolder(String testName, String suiteName) {
         String buildNumber = PlatformVersion.getBuildNumber();
         if (buildNumber == null) {
             return null;
         }
 
        buildFolder = new File(SGTestHelper.getSGTestRootDir() + "/../");
 
         if (!buildFolder.exists())
             buildFolder.mkdir();
         testFolder = new File(buildFolder.getAbsolutePath() + "/" + suiteName + "/" + testName);
         if (!testFolder.exists())
             testFolder.mkdirs();
 
         return testFolder;
     }
 
     public static File getTestFolder() {
         if (testFolder == null) {
             File buildFolder = new File(SGTestHelper.getSGTestRootDir() + "/deploy/local-builds/build_" +
                     PlatformVersion.getBuildNumber());
             testFolder = new File(buildFolder.getAbsolutePath());
             testFolder.mkdirs();
         }
         return testFolder;
     }
 
     public static File getDumpFile() {
         return zipFile;
     }
 
     public static void unzipDump() {
         ZipUtils.unzipArchive(zipFile, testFolder);
     }
 
     public static void copyBeforeConfigurationsLogToTestDir(String testName, String suiteName) {
         File beforeConfigurationsLogDir = new File(buildFolder.getAbsolutePath() + "/" + suiteName + "/" + testName);
         if (beforeConfigurationsLogDir.exists()) {
             try {
            	LogUtils.log("Copying files from source dir:" + beforeConfigurationsLogDir.getAbsolutePath() + " to target dir:" + testFolder.getAbsolutePath());
                 FileUtils.copyDirectory(beforeConfigurationsLogDir, testFolder);
                LogUtils.log("Deleting directory:" + beforeConfigurationsLogDir.getAbsolutePath());
                 FileUtils.deleteDirectory(beforeConfigurationsLogDir);
             } catch (IOException e) {
                 LogUtils.log("Failed to copy before configurations to test dir : " + testFolder.getAbsolutePath(), e);
             }
         }
     }
 
 //    public static void main(String[] args) throws IOException {
 //    	try {
 //			dumpMachines("http://15.185.169.193:8100");
 //			System.out.println("sdf");
 //		} catch (Exception e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 //        DateFormat date1 = new SimpleDateFormat("dd-MM-yyyy");
 //        DateFormat hour = new SimpleDateFormat("HH-mm-ss-SSS");
 //        Date date = new Date();
 //        zipFile = new File(getTestFolder().getAbsolutePath() + "/" + date1.format(date) + "_" + hour.format(date) + "_dump.zip");
 //        zipFile.createNewFile();
 //
 //    }
 }
