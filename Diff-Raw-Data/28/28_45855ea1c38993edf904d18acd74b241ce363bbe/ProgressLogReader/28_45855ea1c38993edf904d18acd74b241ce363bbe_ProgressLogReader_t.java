 package nl.ctmm.trait.proteomics.qcviewer.input;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.StringTokenizer;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import nl.ctmm.trait.proteomics.qcviewer.Main;
 import nl.ctmm.trait.proteomics.qcviewer.utils.Constants;
 
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.lang.time.DurationFormatUtils;
 
 /**
  * The class for reading the qc_status.log file. This log file contains QC pipeline entries in following format:
  *
  * 2013-06-04 13:40:01.165000    QE2_101109_OPL0004_TSV_mousecelllineL_Q1_2.raw    running
  * 2013-06-04 13:40:01.191000    QE2_101109_OPL0004_TSV_mousecelllineL_Q1_2.raw    completed
  *
  * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
  * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
  */
 public class ProgressLogReader implements FileChangeListener {
     /**
     * The logger for this class.
     */
     private static final Logger logger = Logger.getLogger(ProgressLogReader.class.getName());
 
     /**
      * The date format for parsing date/time strings from the log file.
      */
     private static final DateFormat LOG_FILE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
    /**
     * The singleton instance of this class.
     */
    private static final ProgressLogReader INSTANCE = new ProgressLogReader();
     
     /**
      * The current status of the NIST QC pipeline.
      */
     private String currentStatus = "";
 
     /**
      * The name of the msrun that is currently being processed by the QC pipeline.
      */
     private String runningMsrunName = "";
 
     /**
      * The log file of the QC pipeline.
      */
     private File logFile;
 
    /**
     * The timer used for checking the log file at regular intervals.
     */
     private Timer timer; 
 
     /**
      * Constructor.
      */
     private ProgressLogReader() {
     }
     
     /**
      * Parses the current status from the log file and initiate a timer to monitor changes in the log file.
      *
      * @param progressLogFilePath path to the progress log file.
      */
     public void setProgressLogFile(final String progressLogFilePath) {
         logFile = new File(FilenameUtils.normalize(progressLogFilePath));
         parseCurrentStatus(logFile);
         logger.fine("Current QC Pipeline Status: " + currentStatus);
         if (timer != null) {
            timer.cancel();
         }
         // Create a timer and run the timer thread as daemon.
         timer = new Timer(true);
         final StatusMonitorTask task = new StatusMonitorTask();
         timer.schedule(task, Constants.POLL_INTERVAL_PIPELINE_LOG, Constants.POLL_INTERVAL_PIPELINE_LOG);
     }
 
     /**
      * Gets the file monitor instance.
      * 
      * @return file monitor instance
      */
     public static ProgressLogReader getInstance() {
        return INSTANCE;
     }
     
     /**
      * Get the current/latest QC pipeline status.
      *
      * @return the current/latest QC pipeline status.
      */
     public String getCurrentStatus() {
         return currentStatus; 
     }
     
     /**
      * Get the name of the MS RAW file being processed by the QC pipeline.
      *
      * @return the name of the MS RAW file being processed currently.
      */
     public String getRunningMsrunName() {
         return runningMsrunName; 
     }
     
     /**
      * Push the current pipeline status to the Main class.
      */
     public void pushPipelineStatus() {
         parseCurrentStatus(logFile);
         Main.getInstance().notifyUpdatePipelineStatus(currentStatus);
     }
 
     /**
      * Parse the current pipeline status from the log file.
      *
      * Examples of how last lines could look:
      * 2013-05-28 11:11:52.617000    data01QE2_yymmdd_OPLnnnn_DescriptionOfResearchProject.raw    running
      * 2013-05-28 13:04:16.180000    QE1_yymmdd_OPLmmmm_AndYetAnotherResearchProject.raw    completed
      *
      * @param logFile the log file to parse.
      */
     private void parseCurrentStatus(final File logFile) {
         final String lastLine = getLastLine(logFile);
         // Hopefully not the Y2K problem!!!!
         if (lastLine != null && lastLine.startsWith("20")) {
             try {
                 // Get timestamp without microseconds.
                 final String duration = getDuration(lastLine.substring(0, lastLine.indexOf('.')));
                 runningMsrunName = "";
                 if (lastLine.endsWith("running")) {
                     final String rawFileName = getRawFileName(lastLine);
                     currentStatus = String.format("Currently analyzing %s | | | | | Active for %s", rawFileName,
                                                   duration);
                     // Remove the trailing .raw extension for runningMsrunName.
                     runningMsrunName = rawFileName.substring(0, rawFileName.length() - ".raw".length());
                 } else if (lastLine.endsWith("completed")) {
                     currentStatus = String.format("Idle.. | | | | | Inactive for %s", duration);
                 } else {
                     currentStatus = String.format("Unexpected line (%s) encountered while parsing pipeline logfile (%s)",
                                                   lastLine, Constants.PROGRESS_LOG_FILE_NAME);
                 }
             } catch (final ParseException e) {
                 final String message = String.format("Exception occurred while parsing QC pipeline logfile (%s)",
                                                      Constants.PROGRESS_LOG_FILE_NAME);
                 logger.log(Level.WARNING, message, e);
                 currentStatus = message + ": " + e.getMessage();
             }
         } else if (lastLine == null) {
             currentStatus = "Logfile doesn't exist. | | | | | Configured file path: " + logFile.getAbsolutePath();
         } else {
             currentStatus = "QC pipeline logfile " + Constants.PROGRESS_LOG_FILE_NAME + " appears to be empty.";
         }
     }
 
     /**
      * Retrieve the last line from the log file.
      *
      * @param logFile the log file to parse.
      * @return the last line read from the file.
      */
     private String getLastLine(final File logFile) {
         String lastLine = "";
         try {
             final InputStreamReader streamReader = new InputStreamReader(new FileInputStream(logFile));
             final BufferedReader bufferedReader = new BufferedReader(streamReader);
             //Also check for empty lines and white spaces
             while (bufferedReader.ready()) {
                 final String thisLine = bufferedReader.readLine().trim();
                 if (thisLine.length() > 0) {
                     lastLine = thisLine;
                 }
             }
             bufferedReader.close();
         } catch (final IOException e) {
             logger.log(Level.SEVERE, "Something went wrong while reading logfile "
                                      + FilenameUtils.normalize(logFile.getAbsolutePath()));
             lastLine = null;
         }
         return lastLine != null ? lastLine.trim() : lastLine;
     }
 
     /**
      * Get the time difference between a date/time string and the current date/time.
      *
      * @param timeStamp the date/time string to compare to the current date/time.
      * @return the time difference as a string specifying elapsed days, hours, minutes and seconds.
      * @throws ParseException if the timeStamp can not be parsed to a date/time.
      */
     private String getDuration(final String timeStamp) throws ParseException {
         final Date logDate = LOG_FILE_DATE_TIME_FORMAT.parse(timeStamp);
         return DurationFormatUtils.formatDurationWords(System.currentTimeMillis() - logDate.getTime(), true, false);
     }
 
     /**
      * Get the raw file name from the last line in the log file.
      *
      * @param lastLine the last line in the log file.
      * @return the raw file name.
      */
     private String getRawFileName(final String lastLine) {
         final StringTokenizer lineTokenizer = new StringTokenizer(lastLine);
         // Skip the date and time fields.
         lineTokenizer.nextToken();
         lineTokenizer.nextToken();
         return lineTokenizer.nextToken().trim();
     }
 
     /**
      * TODO: check whether this can be removed (if frequently polling the log file is good enough).
      *
      * @param logFile the log file that has changed.
      */
     @Override
     public void fileChanged(final File logFile) {
         logger.fine("ProgressLogReader: logFile changed. Refreshing current status..");
         parseCurrentStatus(logFile);
         logger.fine("Now current status is " + getCurrentStatus());
         Main.getInstance().notifyProgressLogFileChanged(getCurrentStatus());
     }
     
     /**
      * File monitoring task.
      */
     private class StatusMonitorTask extends TimerTask {
         @Override
         public void run() {
             pushPipelineStatus();
         }
     }
 }
