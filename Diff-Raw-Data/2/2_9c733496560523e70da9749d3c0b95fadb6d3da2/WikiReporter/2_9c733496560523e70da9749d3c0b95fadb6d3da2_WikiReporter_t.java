 /*
  * @(#)WikiReporter.java Feb 27, 2007 Copyright 2007 GigaSpaces Technologies
  * Inc.
  */
 package org.cloudifysource.quality.iTests.framework.testng.report.wiki;
 
 import org.cloudifysource.quality.iTests.framework.testng.report.TestsReportFileStream;
 import org.cloudifysource.quality.iTests.framework.testng.report.mail.HtmlMailReporter;
 import org.cloudifysource.quality.iTests.framework.testng.report.xml.SummaryReport;
 import org.cloudifysource.quality.iTests.framework.testng.report.xml.TestLog;
 import org.cloudifysource.quality.iTests.framework.testng.report.xml.TestReport;
 import org.cloudifysource.quality.iTests.framework.testng.report.xml.TestsReport;
 import org.cloudifysource.quality.iTests.framework.tools.SGTestHelper;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.text.NumberFormat;
 import java.util.*;
 import java.util.Map.Entry;
 
 
 /**
  * This class provides Wiki reporter with JAX-RPC capabilities.
  * WikiReporter transforms a TestsReport to wiki-markup syntax and upload to the wiki server.
  * NOTE: Wiki configuration located in wikireporter.properties file under the same package of WikiReporter class.
  */
 public class WikiReporter {
     protected static final String CREDENTIALS_FOLDER = System.getProperty("com.quality.sgtest.credentialsFolder",
             SGTestHelper.getSGTestRootDir() + "/src/main/resources/credentials");
 
     private static final String WIKI_REPORTER_PROPERTIES = CREDENTIALS_FOLDER + "/wikireporter.properties";
 
     private WikiClient wikiClient;
     private List<WikiPage> wikiPages;
 
     private WikiReporterProperties wikiProperties;
 
     private final TestsReport testsReport;
     private final SummaryReport summaryReport;
     private Properties extProperties;
 
     public WikiReporter(Properties extProperties, TestsReport testsReport) {
         this.extProperties = extProperties;
         this.testsReport = testsReport;
         this.summaryReport = new SummaryReport(testsReport);
 
         wikiProperties = new WikiReporterProperties(getWikiProperties());
         wikiClient = WikiClient.login(wikiProperties.getWikiServerUrl(),
                 wikiProperties.getUsername(), wikiProperties.getPassword());
         wikiPages = new ArrayList<WikiPage>();
     }
 
     private Properties getWikiProperties() {
         Properties properties = new Properties();
         try {
             properties.load(new FileInputStream(WIKI_REPORTER_PROPERTIES));
         } catch (IOException e) {
             throw new RuntimeException("failed to read " + WIKI_REPORTER_PROPERTIES + " file - " + e, e);
         }
         return properties;
     }
 
     /**
      * Should be called from ant post-run.xml
      *
      * args[0] - input directory
      * args[1] - suiteType (e.g. Regression, Sanity, etc.)
      * args[2] - build version
      * args[3] - majorVersion
      * args[4] - minorVersion
      */
     public static void main(String[] args) {
 
         String fileName = "sgtest-results.xml";
 
         for(String s : args){
             System.out.println("%%%%%%%%%%%%%%%%%%% "+ s);
         }
 
         Properties extProperties = new Properties();
 
         //args
         String inputDirectory = args[0];
         String suiteType = args[1];
         String buildVersion = args[2];
         String majorVersion = args[3];
         String minorVersion = args[4];
 
         String isCloudMode = System.getProperty("iTests.cloud.enabled");
         if(isCloudMode != null && !Boolean.valueOf(isCloudMode)) {
             String buildLogUrl = args[5];
             extProperties.put("buildLogUrl", buildLogUrl);
         }
 
         extProperties.put("fileName", fileName);
         extProperties.put("inputDirectory", inputDirectory);
         extProperties.put("suiteType", suiteType);
         extProperties.put("buildVersion", buildVersion);
         extProperties.put("majorVersion", majorVersion);
         extProperties.put("minorVersion", minorVersion);
 
 
         TestsReportFileStream fileStream = new TestsReportFileStream();
         TestsReport testsReport = fileStream.readFromFile(inputDirectory, fileName);
         WikiReporter wikiReporter = new WikiReporter(extProperties, testsReport);
         wikiReporter.generateReport();
     }
 
     private void generateReport() {
         try {
         	
             /* init titles */
             String summaryPageTitle = testsReport.getSuiteName() + " " + extProperties.getProperty("majorVersion") + " " + extProperties.getProperty("minorVersion") + " - " + extProperties.getProperty("suiteType") + " report";
             String buildPageTitle =  testsReport.getSuiteName() + " " + extProperties.getProperty("buildVersion") + " - " + extProperties.getProperty("suiteType") + " report";
 
             /*
              * main summary page lists all regression for this suite and links to each report page
              */
             String historyFileName = summaryPageTitle.replace(' ', '-').toLowerCase();
             String inputDirectory = extProperties.getProperty("inputDirectory");
             String summaryHistoryFile = inputDirectory + "/../../wiki-summary/" + historyFileName + ".wiki";
             int reportIndex = 0;
             if(new File(summaryHistoryFile).exists()){
                 String wikiSummaryPage = createMainSummaryPage(summaryPageTitle, buildPageTitle);
                 wikiPages.add(0, new WikiPage(wikiProperties.getWikiSpace(), null /* parent page */, summaryPageTitle, wikiSummaryPage));
                 reportIndex++;
             }
             /* main report page */
             String wikiReportPage = createReportPage();
 
             /* upload to wiki-server, NOTE: the order of upload should be unmodified */
             wikiPages.add(reportIndex, new WikiPage(wikiProperties.getWikiSpace(), summaryPageTitle, buildPageTitle, wikiReportPage));
 
             backupWikiPagesToFile();
             uploadWikiPages();
 
 
            String wikiPageUrl = createReportUrl(wikiPages.get(reportIndex));
             HtmlMailReporter mailReporter = new HtmlMailReporter();
             mailReporter.sendHtmlMailReport(summaryReport, wikiPageUrl, extProperties);
 
         } catch (Exception e) {
             throw new RuntimeException("Failed to generate report - " + e, e);
         }
     }
 
     /**
      * Create a summary page which contains all main data of all regressions.
      * 1. This method creates a history file which contains all summary for all regressions.
      * 2. The name of the history page is the name of summary-page title for i.e:
      * Nightly-Regression-Spring 6.0.wiki the location of this file under TGridROOT/summary-report directory.
      * @param buildPageTitle
      */
     private String createMainSummaryPage(String historyFileName, String buildPageTitle)
             throws IOException {
         /* replace spaces to avoid UNIX FS to drive crazy */
         historyFileName = historyFileName.replace(' ', '-').toLowerCase();
 
         /* create path to summary-report directory */
         String inputDirectory = extProperties.getProperty("inputDirectory");
         String summaryHistoryFile = inputDirectory + "/../../wiki-summary/" + historyFileName + ".wiki";
 
         String buildVersion = extProperties.getProperty("buildVersion");
         String buildStatus = summaryReport.getFailed() == 0 ? "(/)" : "(x)";
         String regressionPageLink = "[" + buildVersion + "|" + wikiProperties.getWikiSpace() + ":" + buildPageTitle + "]";
         String summaryHistory = " | " + buildStatus + "|" + regressionPageLink + " | " + summaryReport.getTotalTestsRun() + " | "
                 + summaryReport.getSuccess() + " | " + summaryReport.getFailed() + " | " + summaryReport.getSkipped() +
                 " | " + summaryReport.getSuspected() + " | " + getSuccessRate() + "%  | " + getDate() + " | ";
 
         /* append to history file */
         WikiUtils.writeToFile(summaryHistory, summaryHistoryFile, true /* append */);
 
         StringBuilder sb = new StringBuilder();
         sb.append("h1. Regression Results: \n");
         sb.append("|| Status || Build || Tests || Success || Failures || Skipped || Suspected ||Success Rate || Date ||\n");
 
         /* read summary wiki-history from history file and reverse the order with the newest build summary */
         List<String> historySummaryList = WikiUtils.getFileLines(summaryHistoryFile);
         Collections.reverse(historySummaryList);
         for (String line : historySummaryList) {
             sb.append(line + "\n");
         }
 
         return sb.toString();
     }
 
     private String getSuccessRate() {
         Double successRateDouble = 0.0;
         if (summaryReport.getTotalTestsRun() > 0 ) { //avoid division by zero
             successRateDouble = ((double)summaryReport.getSuccess() / summaryReport.getTotalTestsRun()) * 100;
         }
 
         /* format success rate to get fraction of 2 digital number i.e : 70.00% */
         NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault() );
         numberFormat.setMaximumFractionDigits(2);
         return numberFormat.format(successRateDouble);
     }
 
     private String getDate() {
         Date date = new Date();
         return date.toLocaleString();
     }
 
     private void backupWikiPagesToFile() {
         for (WikiPage page : wikiPages) {
             String inputDirectory = extProperties.getProperty("inputDirectory");
             String fileName = inputDirectory + "/../../wiki-backup/" + page.getTitlePage().replaceAll(" ", "_") + ".wiki";
             System.out.println("Backup page: " + page.getTitlePage() + " to: " + fileName);
             try {
                 WikiUtils.writeToFile(page.getContext(), fileName, false);
             } catch (IOException e) {
                 System.err.println("Caught exception while backuping up: " + page + " to: " + fileName + " - " + e
                         + "\n" + WikiUtils.getStackTrace(e));
             }
         }
     }
 
     private void uploadWikiPages() {
         WikiPage mainPage = null;
         for (WikiPage page : wikiPages) {
             if (mainPage == null)
                 mainPage = page;
             System.out.println("Removing page: " + page);
             try {
                 wikiClient.removePage(page);
             } catch (WikiConnectionException wce) {
                 System.err.println("Caught exception while removing page: " + page + " - " + wce
                         + "\n" + WikiUtils.getStackTrace(wce));
             }
             System.out.println("Uploading page: " + page);
             try {
                 wikiClient.uploadPage(page);
             } catch (WikiConnectionException wce) {
                 System.err.println("Caught exception while uploading page: " + page + " - " + wce
                         + "\n" + WikiUtils.getStackTrace(wce));
             }
         }
     }
 
     private String createReportUrl(WikiPage page) {
         String title = page.getTitlePage();
         title = title.replaceAll(" ", "%20");
         return wikiClient.getServerAdString() + "/display/" + page.getWikiSpace() + "/" + title;
     }
 
     /**
      * Transform suite results to wiki-markup syntax
      */
     private String createReportPage()
             throws IOException {
         /* number of passed test, the rest failed */
         StringBuilder sb = new StringBuilder();
         sb.append("{composition-setup}\n");
         appendSuiteSummarySection(sb);
 
         sb.append("{deck:id=mainDeck|class=aqua}\n");
         appendBreakdownSummaryTab(sb);
         appendTestsTabs(sb);
         appendMaxDurationsTab(sb);
         sb.append("{deck}\n");
 
         return sb.toString();
     }
 
     private void appendSuiteSummarySection(StringBuilder sb) {
         sb.append("h1. Suite Summary\n");
         //start section
         sb.append("{section}");
         //first column
         sb.append("{column}");
         appendSuiteSummaryContent(sb);
         sb.append("{column}");
         //second column
         sb.append("{column}");
         appendPieCharts(sb);
         sb.append("{column}");
         //third column
         sb.append("{column:width=100%}");
         sb.append(" ");
         sb.append("{column}");
         //close section
         sb.append("{section}\n");
     }
 
 
     private void appendBreakdownSummaryTab(StringBuilder sb) {
         sb.append("{card:label=Breakdown}\n");
         appendPackageSummaryTabContent(sb);
         appendClassSummaryTabContent(sb);
         sb.append("{card}\n");
     }
 
     /**
      * append Suite Summary content
      */
     private void appendSuiteSummaryContent(StringBuilder sb) {
         sb.append("|| Tests || Success || Failures || Skipped || Suspected ||Success Rate || Duration ||\n");
         sb.append(" | " + summaryReport.getTotalTestsRun() + " | " + markupNumericColor(summaryReport.getSuccess(), "green")
                 + " | " + markupNumericColor((summaryReport.getFailed()), "red") + " | " + summaryReport.getSkipped() + " | "
                 + markupNumericColor(summaryReport.getSuspected(),"coral" )+" | " + getSuccessRate()+ "% | " + formatDuration(summaryReport.getDuration()) + " |\n");
     }
 
     /**
      * append Package summary
      */
     private void appendPackageSummaryTabContent(StringBuilder sb) {
         sb.append("h1. Failed By Package\n");
         sb.append("|| Package || Failures ||\n");
 
         Map<String, Integer> packageFailureMap = new HashMap<String, Integer>();
 
         for (TestReport testReport : testsReport.getReports()) {
             if (testReport.isFailed()) {
                 String packageName = testReport.getTestngTestPackageName();
 
                 Integer sumOfFailuresPerPackage = packageFailureMap.get(packageName);
                 if (sumOfFailuresPerPackage == null) {
                     sumOfFailuresPerPackage = 1;
                 } else {
                     sumOfFailuresPerPackage = sumOfFailuresPerPackage.intValue() +1;
                 }
                 packageFailureMap.put(packageName, sumOfFailuresPerPackage);
             }
         }
 
         for (Map.Entry<String, Integer> entry : packageFailureMap.entrySet()) {
             String packageName = entry.getKey();
             Integer packageFailures = entry.getValue();
 
             sb.append("| " + packageName + "| " + markupNumericColor(packageFailures, "red") + " |\n");
         }
         sb.append("\n");
     }
 
     /**
      * append Test summary
      */
     private void appendClassSummaryTabContent(StringBuilder sb) {
         sb.append("h1. Failed By Class\n");
         sb.append("|| Test Class || Failures ||\n");
 
         Map<String, Integer> classFailureMap = new HashMap<String, Integer>();
 
         for (TestReport testReport : testsReport.getReports()) {
             if (testReport.isFailed()) {
                 String className = testReport.getTestngTestClassName();
 
                 Integer sumOfFailuresPerClass = classFailureMap.get(className);
                 if (sumOfFailuresPerClass == null) {
                     sumOfFailuresPerClass = 1;
                 } else {
                     sumOfFailuresPerClass = sumOfFailuresPerClass.intValue() +1;
                 }
                 classFailureMap.put(className, sumOfFailuresPerClass);
             }
         }
 
         for (Map.Entry<String, Integer> entry : classFailureMap.entrySet()) {
             String className = entry.getKey();
             Integer classFailures = entry.getValue();
 
             sb.append("| " + className + "| " + markupNumericColor(classFailures, "red") + " |\n");
         }
 
         sb.append("\n");
     }
 
     private void appendTestsTabs(StringBuilder sb) {
         appendAllFailedTabsContent(sb);
         appendAllPassedTabsContent(sb);
         appendAllSkippedTabsContent(sb);
         appendAllSuspectedTabsContent(sb);
     }
 
     private void appendPieCharts(StringBuilder sb) {
         sb.append(buildSuiteStatusPieChart().toWikiString());
         sb.append(addLineBreak());
     }
 
     private WikiChartStatistics buildSuiteStatusPieChart() {
         Statistics statistics = new Statistics();
         if (summaryReport.getSuccess() > 0) {
             statistics.put("Success", summaryReport.getSuccess());
         }
         if (summaryReport.getFailed() > 0) {
             statistics.put("Failed", summaryReport.getFailed());
         }
         if (summaryReport.getSuspected() > 0) {
             statistics.put("Suspected", summaryReport.getSuspected());
         }
         if (summaryReport.getSkipped() > 0) {
             statistics.put("Skipped", summaryReport.getSkipped());
         }
 
         WikiChartStatistics suiteStatusPieChart = new WikiChartStatistics(statistics);
         suiteStatusPieChart.setTitle("Suite Status");
         suiteStatusPieChart.setSubTitle("total run: " + summaryReport.getTotalTestsRun());
         suiteStatusPieChart.showLegend(false);
         suiteStatusPieChart.assignColor("Success", "#009900");
         suiteStatusPieChart.assignColor("Failed", "#FF0000");
         suiteStatusPieChart.assignColor("Skipped", "#FFCF79");
         suiteStatusPieChart.assignColor("Suspected", "#FF7F50");
 
         return suiteStatusPieChart;
     }
 
     private String formatDuration(long duration) {
         String durationStr = WikiUtils.formatDuration(duration);
         return durationStr.replaceAll(" ", "&nbsp;"); //avoid content wrapping
     }
 
     /**
      * markup the desired numeric with supplied color ONLY if numeric > 0
      */
     private String markupNumericColor(int numeric, String color) {
         if (numeric > 0)
             return "{color:" + color + "}" + numeric + "{color}";
         else
             return String.valueOf(numeric);
     }
 
     private String addLineBreak() {
         return ("\\\\\n");
     }
 
     private String addLink(String text, String link) {
         // in case a windows path is given
         link = link.replace("\\", "/");
         return "["+text+"|"+link+"]";
     }
 
     private StringBuilder addTestLogs(List<TestLog> testLogs) {
         StringBuilder sb = new StringBuilder();
         if (testLogs != null) {
             Iterator<TestLog> iterator = testLogs.iterator();
             while(iterator.hasNext()) {
                 TestLog log = iterator.next();
                 String logName = log.getName();
                 String logUrl = log.getUrl();
                 sb.append(addLink(logName, logUrl));
 
                 if (iterator.hasNext()) {
                     sb.append(addLineBreak());
                 }
             }
         }
 
         return sb;
     }
 
     private void appendNewTab(StringBuilder sb, String tabName, boolean isDefault) {
         sb.append("{card:label=*")
                 .append(tabName)
                 .append("*")
                 .append("|default=")
                 .append(isDefault)
                 .append("}\n");
     }
 
     private void appendCloseTab(StringBuilder sb) {
         sb.append("{card}\n");
     }
 
     private void appendAllFailedTabsContent(StringBuilder sb) {
         boolean isDefault = summaryReport.getFailed() > 0;
         appendNewTab(sb, "Failed Tests ("+summaryReport.getFailed()+")", isDefault);
         sb.append("{table}");
         Map<String, List<TestReport>> tests = groupTestsByClass("failed");
         for (Entry<String, List<TestReport>> entry : tests.entrySet()) {
             String className = entry.getKey();
             List<TestReport> testReportOfClass = entry.getValue();
             appendTestReportInfo(sb, className, testReportOfClass);
         }
         sb.append("{table}");
         appendCloseTab(sb);
     }
 
     private void appendAllPassedTabsContent(StringBuilder sb) {
         boolean isDefault = summaryReport.getFailed() == 0;
         appendNewTab(sb, "Passed Tests ("+summaryReport.getSuccess()+")", isDefault);
         sb.append("{table}");
         Map<String, List<TestReport>> tests = groupTestsByClass("success");
         for (Entry<String, List<TestReport>> entry : tests.entrySet()) {
             String className = entry.getKey();
             List<TestReport> testReportOfClass = entry.getValue();
             appendTestReportInfo(sb, className, testReportOfClass);
         }
         sb.append("{table}");
         appendCloseTab(sb);
     }
 
     private void appendTestReportInfo(StringBuilder sb, String className,
                                       List<TestReport> testReportOfClass) {
         sb.append("{tr}")
                 .append("{td:bgcolor=#c0c0c0}*").append(className).append("*{td}")
                 .append("{td:bgcolor=#c0c0c0}&nbsp;{td}") //failure col.
                 .append("{td:bgcolor=#c0c0c0}&nbsp;{td}") //logs col.
                 .append("{td:bgcolor=#c0c0c0}&nbsp;{td}") //duration col.
                 .append("{tr}\n");
         for (TestReport testReport : testReportOfClass) {
             sb.append("{tr}")
                     .append("{td:valign=top}&nbsp;").append(testReport.getTestngTestMethodName()).append("{td}")
                     .append("{td:valign=top}").append("{color:red}").append(testReport.getCause()!=null?testReport.getCause():"").append("{color}").append("{td}")
                     .append("{td:valign=top}").append(addTestLogs(testReport.getLogs())).append("{td}")
                     .append("{td:valign=top}").append(formatDuration(testReport.getDuration())).append("{td}")
                     .append("{tr}\n");
         }
     }
 
     private void appendAllSkippedTabsContent(StringBuilder sb) {
         boolean isDefault = false;
         appendNewTab(sb, "Skipped Tests ("+summaryReport.getSkipped()+")", isDefault);
         sb.append("{table}");
         Map<String, List<TestReport>> tests = groupTestsByClass("skipped");
         for (Entry<String, List<TestReport>> entry : tests.entrySet()) {
             String className = entry.getKey();
             List<TestReport> testReportOfClass = entry.getValue();
             appendTestReportInfo(sb, className, testReportOfClass);
         }
         sb.append("{table}");
         appendCloseTab(sb);
     }
 
     private void appendAllSuspectedTabsContent(StringBuilder sb) {
         boolean isDefault = false;
         appendNewTab(sb, "Suspected Tests ("+summaryReport.getSuspected()+")", isDefault);
         sb.append("{table}");
         Map<String, List<TestReport>> tests = groupTestsByClass("suspected");
         for (Entry<String, List<TestReport>> entry : tests.entrySet()) {
             String className = entry.getKey();
             List<TestReport> testReportOfClass = entry.getValue();
             appendTestReportInfo(sb, className, testReportOfClass);
         }
         sb.append("{table}");
         appendCloseTab(sb);
     }
 
     /**
      * @param filterBy "success", "failed", "skipped"
      */
     private Map<String, List<TestReport>> groupTestsByClass(String filterBy) {
         Map<String, List<TestReport>> tests = new HashMap<String, List<TestReport>>();
 
         for (TestReport testReport : testsReport.getReports()) {
             boolean acceptFilter = (filterBy.equals("success") && testReport.isSuccess())
                     || (filterBy.equals("failed") && testReport.isFailed())
                     || (filterBy.equals("skipped")  && testReport.isSkipped())
                     || (filterBy.equals("suspected")  && testReport.isSuspected());
 
             if (acceptFilter) {
                 String className = testReport.getTestngTestClassName();
                 List<TestReport> list = tests.get(className);
                 if (list == null) {
                     list = new ArrayList<TestReport>();
                     tests.put(className, list);
                 }
                 list.add(testReport);
             }
         }
 
         return tests;
     }
 
     private void appendMaxDurationsTab(StringBuilder sb) {
 
         TreeMap<Long, List<String>> failedDurations = orderByMaxDurations("failed");
         TreeMap<Long, List<String>> passedDurations = orderByMaxDurations("success");
 
         appendNewTab(sb, "Longest running", false);
         sb.append("h1. Failed tests\n");
         appendTestDurations(sb, failedDurations);
         sb.append("h1. Passed tests\n");
         appendTestDurations(sb, passedDurations);
         appendCloseTab(sb);
 
     }
 
     private void appendTestDurations(StringBuilder sb,
                                      TreeMap<Long, List<String>> failedDurations) {
         sb.append("|| Test || Duration ||\n");
         int top = 10;
         for (Entry<Long, List<String>> entry : failedDurations.entrySet()) {
             Long duration = entry.getKey();
             List<String> list = entry.getValue();
             for (String testName : list) {
                 sb
                         .append("| ").append(testName)
                         .append("| ").append(formatDuration(duration))
                         .append("|\n");
                 --top;
                 if (top == 0) {
                     break;
                 }
             }
             if (top == 0) {
                 break;
             }
         }
     }
 
     private TreeMap<Long, List<String>> orderByMaxDurations(String filterBy) {
         TreeMap<Long, List<String>> durations = new TreeMap<Long, List<String>>(new Comparator<Long>() {
             @Override
             public int compare(Long l1, Long l2) {
                 return l2.compareTo(l1); //reverse ordering - greater to lesser
             }
         });
 
         Map<String, List<TestReport>> groupTestsByClass = groupTestsByClass(filterBy);
         Collection<List<TestReport>> values = groupTestsByClass.values();
         Iterator<List<TestReport>> iterator = values.iterator();
         while (iterator.hasNext()) {
             List<TestReport> listOfTests = iterator.next();
             for (TestReport testReport : listOfTests) {
                 List<String> list = durations.get(testReport.getDuration());
                 if (list == null) {
                     list = new ArrayList<String>();
                 }
                 list.add(testReport.getName());
                 durations.put(testReport.getDuration(), list);
             }
         }
         return durations;
     }
 }
