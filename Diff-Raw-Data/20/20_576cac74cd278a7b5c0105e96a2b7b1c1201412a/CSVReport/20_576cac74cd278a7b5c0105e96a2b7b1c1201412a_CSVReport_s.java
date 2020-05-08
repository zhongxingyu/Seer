 package uk.org.sappho.code.heatmap.report;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 
 import org.apache.log4j.Logger;
 
 import com.google.inject.Inject;
 
 import uk.org.sappho.code.heatmap.config.Configuration;
 import uk.org.sappho.code.heatmap.engine.HeatMap;
 import uk.org.sappho.code.heatmap.engine.HeatMapItem;
 import uk.org.sappho.code.heatmap.engine.HeatMaps;
 import uk.org.sappho.code.heatmap.engine.Releases;
 import uk.org.sappho.code.heatmap.issues.IssueWrapper;
 
 public class CSVReport implements Report {
 
     private final Configuration config;
     private static final Logger LOG = Logger.getLogger(CSVReport.class);
 
     @Inject
     public CSVReport(Configuration config) {
 
         LOG.info("Using CSV Report plugin");
         this.config = config;
     }
 
     public void writeReport(Releases releases) throws ReportException {
 
         Writer writer = null;
         try {
             String basePath = config.getProperty("report.path");
             String extension = config.getProperty("report.extension", ".csv");
             String seperator = config.getProperty("csv.seperator", ",");
             String header = config.getProperty("csv.header",
                     "Item Name" + seperator + "Change Issue Count" + seperator
                             + "Change Count" + seperator + "Issues" + seperator + "Formula");
             LOG.debug("CSV Report parameters:");
             LOG.debug("basePath:  " + basePath);
             LOG.debug("extension: " + extension);
             LOG.debug("seperator: " + seperator);
             LOG.debug("header:    " + header);
            for (String release : releases.getReleaseNames()) {
                 HeatMaps heatMaps = releases.getHeatMaps(release);
                 for (String heatMapName : HeatMaps.HEATMAPS) {
                     String path = basePath + "/" + release + "/";
                     new File(path).mkdirs();
                     String filename = path + heatMapName + extension;
                     LOG.info("Writing CSV report " + filename);
                     writer = new FileWriter(filename);
                     writer.write(header + "\n");
                     HeatMap heatMap = heatMaps.getHeatMap(heatMapName);
                     for (HeatMapItem heatMapItem : heatMap.getSortedHeatMapItems()) {
                         writer.write(heatMapItem.getHeatMapItemName() + seperator + heatMapItem.getWeight() + seperator
                                 + heatMapItem.getIssues().size() + seperator + heatMapItem.getChangeCount());
                         String prefix = seperator;
                         for (IssueWrapper issue : heatMapItem.getIssues()) {
                             writer.write(prefix + issue.getKey());
                             prefix = " ";
                         }
                         writer.write(seperator + heatMapItem.getWeightFormula() + "\n");
                     }
                     writer.close();
                     LOG.debug("Written " + filename);
                 }
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
