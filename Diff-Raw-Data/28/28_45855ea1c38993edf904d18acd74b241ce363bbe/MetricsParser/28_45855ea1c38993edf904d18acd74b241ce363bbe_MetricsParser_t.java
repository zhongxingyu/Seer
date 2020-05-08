 package nl.ctmm.trait.proteomics.qcviewer.input;
 
 import java.io.BufferedReader;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import nl.ctmm.trait.proteomics.qcviewer.gui.SortedListModel;
 import nl.ctmm.trait.proteomics.qcviewer.utils.Constants;
 
import org.apache.commons.io.FilenameUtils;

 /**
  * The class for parsing metrics IDs and descriptions from the MetricsListing.txt file.
  *
  * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
  * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
  */
 public class MetricsParser {
     /**
      * The logger for this class.
      */
     private static final Logger logger = Logger.getLogger(MetricsParser.class.getName());
 
     /**
      * The application properties.
      * <p/>
      * TODO: maybe it's better to put the responsibility for writing properties in a separate Settings class? [Freek]
      */
     private Properties appProperties;
 
     /**
      * The map with all metrics supported by the NIST QC pipeline. The keys are "category:code" strings and the values
      * are descriptions of the metrics.
      * <p/>
      * For example, key: "dyn:ds-1a" and value: "Ratios of Peptide Ions IDed (Once/Twice)".
      */
     private Map<String, String> allMetricsMap;
 
     /**
      * Construct a metrics parser.
      *
      * @param appProperties the application properties.
      */
     public MetricsParser(final Properties appProperties) {
         this.appProperties = appProperties;
     }
 
     /**
      * Get all the metrics by reading them from the MetricsListing.txt file.
      *
      * @return map containing key and value of metrics.
      */
     public Map<String, String> getMetricsListing() {
         if (allMetricsMap == null) {
             readMetricsListing();
         }
         return allMetricsMap;
     }
 
     /**
      * Read all the metrics from the MetricsListing.txt file.
      */
     private void readMetricsListing() {
         allMetricsMap = new HashMap<>();
         try {
             final BufferedReader reader = new BufferedReader(new FileReader(FilenameUtils.normalize(Constants.METRICS_LISTING_FILE_NAME)));
             String line;
             while ((line = reader.readLine()) != null) {
                 final String separator = ":";
                 final StringTokenizer lineTokenizer = new StringTokenizer(line, separator);
                 // The first two tokens together form key of the metric e.g. "ion:is-1b".
                 final String key = lineTokenizer.nextToken() + separator + lineTokenizer.nextToken();
                 // The second token is description of the metric.
                 final String value = lineTokenizer.nextToken();
                 allMetricsMap.put(key, value);
             }
             reader.close();
         } catch (final NumberFormatException | IOException e) {
             final String message = "Something went wrong while reading the metrics definition file: %s.";
             logger.log(Level.SEVERE, String.format(message, Constants.METRICS_LISTING_FILE_NAME), e);
         }
     }
 
     /**
      * Update metrics in the report frame and update the TopColumnNamesV2 property in the application properties file.
      *
      * TODO: looks like updating the GUI is done elsewhere. If that's the case, we can rename this method. [Freek]
      *
      * @param selectedMetrics a sorted list containing the names of the selected metrics.
      */
     public void updateMetricsToDisplay(final SortedListModel selectedMetrics) {
         String selectedMetricsString = "";
         for (int metricIndex = 0; metricIndex < selectedMetrics.getSize(); metricIndex++) {
             selectedMetricsString += selectedMetrics.getElementAt(metricIndex) + ",";
         }
         appProperties.setProperty(Constants.PROPERTY_TOP_COLUMN_NAMESV2, selectedMetricsString);
         try {
             final FileOutputStream outputStream = new FileOutputStream(FilenameUtils.normalize(Constants.PROPERTIES_FILE_NAME));
             appProperties.store(outputStream, null);
             outputStream.close();
         } catch (final IOException e) {
             final String message = "Something went wrong while writing the application properties file: %s.";
             logger.log(Level.SEVERE, String.format(message, Constants.PROPERTIES_FILE_NAME), e);
         }
     }
 }
