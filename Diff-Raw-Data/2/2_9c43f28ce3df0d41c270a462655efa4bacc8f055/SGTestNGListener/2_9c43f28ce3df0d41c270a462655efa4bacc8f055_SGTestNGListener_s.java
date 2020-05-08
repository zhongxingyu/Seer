 package framework.testng;
 
 import framework.report.MailReporterProperties;
 import framework.tools.SimpleMail;
 import framework.utils.DumpUtils;
 import framework.utils.LogUtils;
 import framework.utils.ZipUtils;
 
 import org.testng.ITestContext;
 import org.testng.ITestResult;
 import org.testng.Reporter;
 import org.testng.TestListenerAdapter;
 
 import java.io.*;
 import java.util.List;
 import java.util.Properties;
 
 
 public class SGTestNGListener extends TestListenerAdapter {
 
     protected File testFolder;
 
     @Override
     public void onTestStart(ITestResult iTestResult) {
         super.onTestStart(iTestResult);
 
         String folderName = iTestResult.getMethod().toString();
         // remove any method input parameters between parenthesis
         // we need to maintain the folder naming convention not to brake the report htmls.
         folderName = folderName.split("\\(|\\)")[0] + "()";
         testFolder = DumpUtils.createTestFolder(folderName);
         LogUtils.log("Test Started: " + iTestResult.getTestClass().getName() + "." + iTestResult.getName());
     }
 
     @Override
     public void onConfigurationFailure(ITestResult iTestResult) {
         super.onConfigurationFailure(iTestResult);
         testFolder = DumpUtils.createTestFolder(iTestResult.getMethod().toString());
         LogUtils.log("Test Configuration Failed: " + iTestResult.getMethod().toString(),
                 iTestResult.getThrowable());
         write2LogFile(iTestResult);
     }
 
     @Override
     public void onTestFailure(ITestResult iTestResult) {
         super.onTestFailure(iTestResult);
         ZipUtils.unzipArchive(iTestResult.getMethod().toString());
         LogUtils.log("Test Failed: " + iTestResult.getMethod().toString(), iTestResult.getThrowable());
         write2LogFile(iTestResult);
     }
 
     @Override
     public void onTestSuccess(ITestResult iTestResult) {
         super.onTestSuccess(iTestResult);
         ZipUtils.unzipArchive(iTestResult.getMethod().toString());
         LogUtils.log("Test Passed: " + iTestResult.getMethod().toString());
         write2LogFile(iTestResult);
     }
 
     @Override
     public void onFinish(ITestContext testContext) {
         super.onFinish(testContext);
         sendHtmlMailReport(testContext);
     }
 
     private void deleteFile(ITestResult iTestResult) {
         File testLogFile = new File(testFolder.getAbsolutePath() + "/" + iTestResult.getName() + ".log");
         testLogFile.delete();
     }
 
     private void write2LogFile(ITestResult iTestResult) {
         try {
             File testLogFile = new File(testFolder.getAbsolutePath() + "/" + iTestResult.getName() + ".log");
             if (!testLogFile.createNewFile()) {
                 new RuntimeException("Failed to create log file [" + testLogFile + "];\n log output: " + Reporter.getOutput());
             }
             FileWriter fstream = new FileWriter(testLogFile);
             BufferedWriter out = new BufferedWriter(fstream);
             String output = SGTestNGReporter.getOutput();
             out.write(output);
             out.close();
         } catch (Exception e) {
             new RuntimeException(e);
         } finally {
             SGTestNGReporter.reset();
         }
     }
 
     void sendHtmlMailReport(ITestContext testContext) {
         String buildNumber = System.getProperty("sgtest.buildNumber");
         String suiteName = System.getProperty("sgtest.suiteName");
         List<String> mailRecipients = null;
         if(buildNumber == null)
             return;
         Properties props = new Properties();
         InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("framework/report/mailreporter.properties");
         try {
             props.load(in);
             in.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
         MailReporterProperties mailProperties = new MailReporterProperties(props);
         String link = null;
         if (suiteName.contains("webui")) {
         	link = "<a href=http://192.168.9.121:8087/sgtest.webui/"  + "build_" + buildNumber + "/" + suiteName + "/html>"+buildNumber+" </a>";
         }
         else {
        	link = "<a href=http://192.168.9.121:8087/sgtest/" + buildNumber + "/" + suiteName + "/html>"+buildNumber+" </a>";
         }
         StringBuilder sb = new StringBuilder();
         sb.append("<html>").append("\n");
         sb.append("<h1>SGTtest Results </h1></br></br></br>").append("\n");
         sb.append("<h2>Suite Name:  " + testContext.getSuite().getName() + " </h2></br>").append("\n");
         sb.append("<h4>Start Date:  " + testContext.getStartDate() + " </h4></br>").append("\n");
         sb.append("<h4>End Date:  " + testContext.getEndDate() + " </h4></br>").append("\n");
         sb.append("<h4 style=\"color:blue\">Skipped Configurations:  " + testContext.getSkippedConfigurations().size() + " </h4></br>").append("\n");
         sb.append("<h4 style=\"color:orange\">Skipped Tests:  " + testContext.getSkippedTests().size() + " </h4></br>").append("\n");
         sb.append("<h4 style=\"color:red\">Failed Tests:  " + testContext.getFailedTests().size() + " </h4></br>").append("\n");
         sb.append("<h4 style=\"color:green\">Passed Tests:  " + testContext.getPassedTests().size() + " </h4></br>").append("\n");
         sb.append("<h4>Full Suite Report:  " + link + " </h4></br>").append("\n");
         sb.append("</html>");
         try {
         	mailRecipients = mailProperties.getRecipients();
         	if (suiteName.contains("webui")) mailRecipients = mailProperties.getWebUIRecipients();
         	if (suiteName.equals("ServiceGrid")) mailRecipients = mailProperties.getSGRecipients();
         	if (suiteName.equals("WAN")) mailRecipients = mailProperties.getWanRecipients();
         	if (suiteName.equals("SECURITY")) mailRecipients = mailProperties.getSecurityRecipients();
         	if (suiteName.equals("CLOUDIFY")) mailRecipients = mailProperties.getCloudifyRecipients();
         	if (suiteName.equals("ESM")) mailRecipients = mailProperties.getESMRecipients();
             SimpleMail.send(mailProperties.getMailHost(), mailProperties.getUsername(), mailProperties.getPassword(),
                     "SGTest Suite " + testContext.getSuite().getName() + " results", sb.toString(), mailRecipients);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
