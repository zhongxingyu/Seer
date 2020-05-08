 package framework.testng.report.mail;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import com.gigaspaces.dashboard.DashboardDBReporter;
 
 import framework.report.MailReporterProperties;
 import framework.testng.report.wiki.WikiUtils;
 import framework.testng.report.xml.SummaryReport;
 import framework.tools.SimpleMail;
 
 public class HtmlMailReporter {
 
     public HtmlMailReporter() {
     }
 
     public void sendHtmlMailReport(SummaryReport summaryReport, String wikiPageUrl, Properties extProperties) {
         String buildNumber = extProperties.getProperty("buildVersion");
         String majorVersion = extProperties.getProperty("majorVersion");
         String minorVersion = extProperties.getProperty("minorVersion");
         String buildLogUrl = extProperties.getProperty("buildLogUrl");
         String suiteName = summaryReport.getSuiteName();
 
         List<String> mailRecipients = null;
         if (buildNumber == null)
             return;
         Properties props = new Properties();
        InputStream in = HtmlMailReporter.class.getClassLoader().getResourceAsStream("mailreporter.properties");
         try {
             props.load(in);
             in.close();
             System.out.println("mailreporter.properties: " + props);
         } catch (IOException e) {
             throw new RuntimeException("failed to read mailreporter.properties file - " + e, e);
         }
 
         MailReporterProperties mailProperties = new MailReporterProperties(props);
 
         String link = "<a href=" + wikiPageUrl + ">"
                 + buildNumber + " " + majorVersion + " " + minorVersion + " </a>";
 
         StringBuilder sb = new StringBuilder();
         sb.append("<html>").append("\n");
         sb.append("<h1>SGTest Cloudify Results </h1></br></br></br>").append("\n");
         sb.append("<h2>Suite Name:  " + summaryReport.getSuiteName() + " </h2></br>").append("\n");
         sb.append("<h4>Duration:  " + WikiUtils.formatDuration(summaryReport.getDuration()) + " </h4></br>").append("\n");
         sb.append("<h4>Full Suite Report:  " + link + " </h4></br>").append("\n");
         sb.append("<h4>Full build log:  " + getFullBuildLog(buildLogUrl) + " </h4></br>").append("\n");
         sb.append("<h4 style=\"color:blue\">Total run:  " + summaryReport.getTotalTestsRun() + " </h4></br>").append("\n");
         sb.append("<h4 style=\"color:red\">Failed Tests:  " + summaryReport.getFailed() + " </h4></br>").append("\n");
         sb.append("<h4 style=\"color:green\">Passed Tests:  " + summaryReport.getSuccess() + " </h4></br>").append("\n");
         sb.append("<h4 style=\"color:orange\">Skipped:  " + summaryReport.getSkipped() + " </h4></br>").append("\n");
         sb.append("<h4 style=\"color:coral\">Suspected:  " + summaryReport.getSuspected() + " </h4></br>").append("\n");
         
         sb.append("</html>");
         try {
             mailRecipients = mailProperties.getRecipients();
             if (suiteName.contains("webui")) mailRecipients = mailProperties.getWebUIRecipients();
             if (suiteName.contains("CLOUDIFY")) mailRecipients = mailProperties.getCloudifyRecipients();
 
             System.out.println("sending mail to recipients: " + mailRecipients);
 
             SimpleMail.send(mailProperties.getMailHost(), mailProperties.getUsername(), mailProperties.getPassword(),
                     "SGTest Suite " + summaryReport.getSuiteName() + " results " + buildNumber + " " + majorVersion
                             + " " + minorVersion, sb.toString(), mailRecipients);
 
         } catch (Exception e) {
             throw new RuntimeException("failed to send mail - " + e, e);
         }
         
         DashboardDBReporter.writeToDB(summaryReport.getSuiteName(), buildNumber.split("_")[1], majorVersion, minorVersion, 
 				summaryReport.getDuration(), buildLogUrl, summaryReport.getTotalTestsRun(), summaryReport.getFailed(),
 				summaryReport.getSuccess(), summaryReport.getSkipped(), summaryReport.getSuspected(), 0/*orphans*/, wikiPageUrl, null);
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
 //        System.setProperty("sgtest.buildNumber", "1234-123");
 //        System.setProperty("sgtest.suiteName", "ServiceGrid");
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
