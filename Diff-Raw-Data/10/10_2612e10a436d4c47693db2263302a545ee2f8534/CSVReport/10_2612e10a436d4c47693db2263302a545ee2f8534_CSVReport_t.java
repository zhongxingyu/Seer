 package uk.org.sappho.code.heatmap.report;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 
 import org.apache.log4j.Logger;
 
 import com.google.inject.Inject;
 
 import uk.org.sappho.code.heatmap.config.Configuration;
 import uk.org.sappho.code.heatmap.engine.HeatMap;
 import uk.org.sappho.code.heatmap.engine.HeatMapCollection;
 import uk.org.sappho.code.heatmap.engine.HeatMapItem;
 import uk.org.sappho.code.heatmap.issues.IssueWrapper;
 
 public class CSVReport implements Report {
 
     private final Configuration config;
     private static final Logger LOG = Logger.getLogger(CSVReport.class);
 
     @Inject
     public CSVReport(Configuration config) {
 
         LOG.info("Using CSV Report plugin");
         this.config = config;
     }
 
     public void writeReport(HeatMapCollection heatMapCollection) throws ReportException {
 
         Writer writer = null;
         try {
             String basePath = config.getProperty("report.path");
             String extension = config.getProperty("report.extension", ".csv");
             String seperator = config.getProperty("csv.seperator", ",");
             String header = config.getProperty("csv.header",
                     "Item Name" + seperator + "Change Issue Count" + seperator
                             + "Change Count" + seperator + "Issues");
             LOG.debug("CSV Report parameters:");
             LOG.debug("basePath:  " + basePath);
             LOG.debug("extension: " + extension);
             LOG.debug("seperator: " + seperator);
             LOG.debug("header:    " + header);
             for (String heatMapName : HeatMapCollection.HEATMAPS) {
                 String filename = basePath + heatMapName + extension;
                 LOG.info("Writing CSV report " + filename);
                 writer = new FileWriter(filename);
                 writer.write(header + "\n");
                 HeatMap heatMap = heatMapCollection.getHeatMap(heatMapName);
                 for (HeatMapItem item : heatMap.getSortedList()) {
                     writer.write(item.getName() + seperator + item.getWeight() + seperator + item.getIssueCount()
                             + seperator + item.getChangeCount());
                     for (IssueWrapper issue : item.getIssues()) {
                        String subTaskKey = issue.getSubTaskKey();
                        writer.write(seperator + issue.getKey() + (subTaskKey != null ? " via " + subTaskKey : ""));
                     }
                     writer.write("\n");
                 }
                 writer.close();
                 LOG.debug("Written " + filename);
             }
         } catch (Throwable t) {
             if (writer != null) {
                 try {
                     writer.close();
                 } catch (IOException e) {
                 }
             }
             throw new ReportException("Unable to write CSV reports", t);
         }
     }
 }
