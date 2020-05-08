 package framework.testng.report;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.testng.ITestResult;
 
 import framework.testng.report.xml.TestLog;
 import framework.utils.TestNGUtils;
 
 /**
  * @author moran
  */
 public class LogFetcher {
 	private static final String BUILD_FOLDER_KEY = "sgtest.buildFolder";
 	private static final String REPORT_URL_KEY = "sgtest.url";
 
 	public LogFetcher() {
 	}
 	
 	public List<TestLog> getLogs(ITestResult result) {
 		List<TestLog> logs = new ArrayList<TestLog>();
		String suiteName = result.getTestClass().getXmlTest().getSuite().getName().split("[0-9]+")[0];
 		String parameters = TestNGUtils.extractParameters(result);
 		String testName = result.getMethod().toString().split("\\(|\\)")[0]
 				+ "(" + parameters + ")";
 		File testDir = new File(getBuildFolder() + "/" + suiteName + "/" + testName);
 		return fetchLogs(testDir, logs);
 	}
 
 	private String getBuildFolder() {
 		return System.getProperty(BUILD_FOLDER_KEY);
 	}
 
 	private List<TestLog> fetchLogs(File dir, List<TestLog> list) {
 		try {
 			File[] children = dir.listFiles();
 			if (children == null)
 				return list;
 			for (int n = 0; n < children.length; n++) {
 				File file = children[n];
 				if (file.getName().endsWith((".log"))) {
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
 		int index = path.indexOf("build_");
 		return getUrl() + path.substring(index);
 	}
 
 	private String getUrl() {
 		return System.getProperty(REPORT_URL_KEY);
 	}
 }
