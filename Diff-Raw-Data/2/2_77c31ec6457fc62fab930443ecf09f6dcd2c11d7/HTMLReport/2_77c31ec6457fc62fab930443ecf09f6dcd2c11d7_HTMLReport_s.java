 package framework.report;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * Called by ant run.xml after report is generated.
  * Updates the page with latest link to build results.
  */
 public class HTMLReport {
 
     private void appendReportLink(String reportPath, String newBuildreport) {
         String suiteName = System.getProperty("sgtest.suiteName");
         File report = new File(reportPath);
         StringBuilder sb = new StringBuilder();
         for (File f : report.listFiles()) {
             if (f.getName().equals("index.htm")) {
                 try {
                     BufferedReader in = new BufferedReader(new FileReader(f));
                     String str = "";
 
                     while ((str = in.readLine()) != null) {
                         sb.append(str);
                     }
                     int startLinkIndex = sb.indexOf("<links>") + 7;
                     Date date = new Date();
                     DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                     String link = null;
                     if (suiteName.contains("webui")) {
                    	link = "<a href=http://192.168.9.121:8087/sgtest.webui/" + "build_" + newBuildreport + "/" + suiteName + "/html/><font color=\"0000FF\">" +
                         dateFormat.format(date) +" " + newBuildreport + "</font></a></br>";
                     }
                     else {
                     	link = "<a href=http://192.168.9.121:8087/sgtest-cloudify/" + newBuildreport + "/" + suiteName + "/html/><font color=\"0000FF\">" +
                     		dateFormat.format(date) +" " + newBuildreport + "</font></a></br>";
                     }
                     sb.insert(startLinkIndex, link);
                     BufferedWriter out = new BufferedWriter(new FileWriter(f));
                     out.write(sb.toString());
 
                     in.close();
                     out.close();
                 } catch (IOException e) {
                     throw new RuntimeException(e);
                 }
             }
         }
     }
 
 
     public static void main(String[] args) {
         String mainReport = args[0];
         String buildNumber = args[1];
         HTMLReport report = new HTMLReport();
         report.appendReportLink(mainReport, buildNumber);
 
 
     }
 
 }
