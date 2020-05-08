 package iTests.framework.testng.report.mail;
 
 import com.gigaspaces.dashboard.DashboardDBReporter;
 import iTests.framework.testng.report.wiki.WikiUtils;
 import iTests.framework.testng.report.xml.SummaryReport;
 import iTests.framework.tools.SGTestHelper;
 import iTests.framework.tools.SimpleMail;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 public class HtmlMailReporter {
 
     protected static final String CREDENTIALS_FOLDER = System.getProperty("iTests.credentialsFolder",
             SGTestHelper.getSGTestRootDir() + "/src/main/resources/credentials");
 
     private static final String MAIL_REPORTER_PROPERTIES = CREDENTIALS_FOLDER + "/mailreporter.properties";
 
     public HtmlMailReporter() {
     }
 
     public void sendHtmlMailReport(SummaryReport summaryReport, String wikiPageUrl, Properties extProperties) {
         String buildNumber = extProperties.getProperty("buildVersion");
         String majorVersion = extProperties.getProperty("majorVersion");
         String minorVersion = extProperties.getProperty("minorVersion");
         String buildLogUrl = extProperties.getProperty("buildLogUrl");
         String suiteName = summaryReport.getSuiteName();
 
         List<String> mailRecipients = null;
         if(buildNumber == null)
             return;
 
         Properties props = new Properties();
         try {
             props.load(new FileInputStream(MAIL_REPORTER_PROPERTIES));
         } catch (IOException e) {
             throw new RuntimeException("failed to read " + MAIL_REPORTER_PROPERTIES + " file - " + e, e);
         }
         System.out.println("mailreporter.properties: " + props);
 
         MailReporterProperties mailProperties = new MailReporterProperties(props);
 
         String link = "<a href=" + wikiPageUrl + ">"
                 + buildNumber + " " + majorVersion + " " + minorVersion + " </a>";
 
         StringBuilder sb = new StringBuilder();
         sb.append("<html>").append("\n");
 
         String type;
 
         System.out.println("project name: " + extProperties.getProperty("suiteType"));
         if(extProperties.getProperty("suiteType").contains("XAP")){
             sb.append("<h1>SGTest XAP Results </h1></br></br></br>").append("\n");
             type = "iTests-XAP";
         }
         else{
             sb.append("<h1>Cloudify-iTests Results </h1></br></br></br>").append("\n");
             type = "iTests-Cloudify";
         }
 
         sb.append("<h2>Suite Name:  " + summaryReport.getSuiteName() + " </h2></br>").append("\n");
         sb.append("<h4>Duration:  " + WikiUtils.formatDuration(summaryReport.getDuration()) + " </h4></br>").append("\n");
         sb.append("<h4>Full Suite Report:  " + link + " </h4></br>").append("\n");
        if(buildLogUrl != null||  !buildLogUrl.equals(""))
             sb.append("<h4>Full build log:  <a href=" + getFullBuildLog(buildLogUrl) + ">" + getFullBuildLog(buildLogUrl) + "</a> </h4></br>").append("\n");
         sb.append("<h4 style=\"color:blue\">Total run:  " + summaryReport.getTotalTestsRun() + " </h4></br>").append("\n");
         sb.append("<h4 style=\"color:red\">Failed Tests:  " + summaryReport.getFailed() + " </h4></br>").append("\n");
         sb.append("<h4 style=\"color:green\">Passed Tests:  " + summaryReport.getSuccess() + " </h4></br>").append("\n");
         sb.append("<h4 style=\"color:orange\">Skipped:  " + summaryReport.getSkipped() + " </h4></br>").append("\n");
         sb.append("<h4 style=\"color:coral\">Suspected:  " + summaryReport.getSuspected() + " </h4></br>").append("\n");
         sb.append("</html>");
 
         try {
             mailRecipients = mailProperties.getRecipients();
             if (suiteName.contains("webui")) mailRecipients = mailProperties.getWebUIRecipients();
             if (suiteName.equals("ServiceGrid")) mailRecipients = mailProperties.getSGRecipients();
             if (suiteName.equals("WAN")) mailRecipients = mailProperties.getWanRecipients();
             if (suiteName.equals("SECURITY")) mailRecipients = mailProperties.getSecurityRecipients();
             if (suiteName.equals("CLOUDIFY")) mailRecipients = mailProperties.getCloudifyRecipients();
             if (suiteName.equals("ESM")) mailRecipients = mailProperties.getESMRecipients();
             if (suiteName.equals("DISCONNECT")) mailRecipients = mailProperties.getDisconnectRecipients();
             if (suiteName.equals("CPP_Linux-amd64")) mailRecipients = mailProperties.getCPP_Linux_amd64Recipients();
             if (suiteName.equals("CPP_Linux32")) mailRecipients = mailProperties.getCPP_Linux32();
             if (suiteName.contains("CLOUDIFY")) mailRecipients = mailProperties.getCloudifyRecipients();
 
             System.out.println("sending mail to recipients: " + mailRecipients);
 
             SimpleMail.send(mailProperties.getMailHost(), mailProperties.getUsername(), mailProperties.getPassword(),
                     "SGTest Suite " + summaryReport.getSuiteName() + " results " + buildNumber + " " + majorVersion
                             + " " + minorVersion, sb.toString(), mailRecipients);
 
         } catch (Exception e) {
             throw new RuntimeException("failed to send mail - " + e, e);
         }
 
         String[] buildeNumberSplit = buildNumber.split("_");
         String buildNumberForDB;
         if(buildeNumberSplit.length >= 2)
             buildNumberForDB = buildeNumberSplit[1];
         else
             buildNumberForDB = buildNumber;
 
         DashboardDBReporter.writeToDB(summaryReport.getSuiteName(), buildNumberForDB, majorVersion, minorVersion,
 				summaryReport.getDuration(), buildLogUrl, summaryReport.getTotalTestsRun(), summaryReport.getFailed(),
 				summaryReport.getSuccess(), summaryReport.getSkipped(), summaryReport.getSuspected(), 0/*orphans*/, wikiPageUrl, "", type);
     }
 
     static String getFullBuildLog(String buildLog) {
         StringTokenizer tokenizer = new StringTokenizer(buildLog, "/");
         List<String> tokens = new ArrayList<String>();
         while (tokenizer.hasMoreTokens()) {
             tokens.add(tokenizer.nextToken());
         }
         return tokens.get(0) + "//" + tokens.get(1) + "/download/" + tokens.get(3) + "/" + tokens.get(2);
     }
 
 //	/*
 //	 * Test this!
 //	 */
 //	public static void main(String[] args) {
 //
 //        System.setProperty("iTests.buildNumber", "1234-123");
 //        System.setProperty("iTests.suiteName", "ServiceGrid");
 //        System.setProperty("sgtest.majorVersion", "9.0.0");
 //        System.setProperty("sgtest.minorVersion", "m1");
 //
 //		HtmlMailReporter mailReporter = new HtmlMailReporter();
 //		TestsReport testsReport = TestsReport.newEmptyReport();
 //		testsReport.setSuiteName("ServiceGrid");
 //		TestReport report = new TestReport("test");
 //		report.setDuration(10L);
 //		testsReport.getReports().add(report);
 //		SummaryReport summaryReport = new SummaryReport(testsReport);
 //		mailReporter.sendHtmlMailReport(summaryReport, "some-url");
 //	}
 }
