 /*
  * PatientView
  *
  * Copyright (c) Worth Solutions Limited 2004-2013
  *
  * This file is part of PatientView.
  *
  * PatientView is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  * PatientView is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License along with PatientView in a file
  * titled COPYING. If not, see <http://www.gnu.org/licenses/>.
  *
  * @package PatientView
  * @link http://www.patientview.org
  * @author PatientView <info@patientview.org>
  * @copyright Copyright (c) 2004-2013, Worth Solutions Limited
  * @license http://www.gnu.org/licenses/gpl-3.0.html The GNU General Public License V3.0
  */
 
 package com.worthsoln.monitoring;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.Resource;
 import org.springframework.core.io.support.PropertiesLoaderUtils;
 import org.springframework.mail.javamail.JavaMailSender;
 import org.springframework.mail.javamail.MimeMessageHelper;
 import org.springframework.util.StopWatch;
 
 import javax.mail.Address;
 import javax.mail.Message;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 /**
  * Logs the number of files that XML Import needs to process
  * <p/>
  * Monitors those files to see if the XML Import is stalled or not working properly
  *
  * @author Deniz Ozger
  */
 public final class ImportMonitor {
 
     // Timings and limitations
     private static final int FREQUENCY_OF_LOGGING_IMPORT_FILE_COUNTS_IN_MINUTES = 1;
     /**
      * Should always be equal to monitoringFrequencyInMinutes /
      * FREQUENCY_OF_LOGGING_IMPORT_FILE_COUNTS_IN_MINUTES. Thus; it is calculated in runtime.
      */
     private static int numberOfLinesToRead = -1;
 
     // Import data file format
     private static final String RECORD_DATA_DELIMITER = ",";
     private static final String RECORD_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
     private static final int DATE_POSITION_IN_RECORD = 0;
     private static final String COUNT_LOG_FILENAME_FORMAT = "yyyy-MM-dd";
     private static final String COMMENT_PREFIX = "#";
 
     // Class constants
     private static final String PROJECT_PROPERTIES_FILE = "patientview.properties";
     private static final Logger LOGGER = LoggerFactory.getLogger(ImportMonitor.class);
 
     private static final int LINE_FEED = 0xA;
     private static final int CARRIAGE_RETURN = 0xD;
 
     private static final int SECONDS_IN_MINUTE = 60;
     private static final int MILLISECONDS = 1000;
 
     private static final int HASH_SEED_17 = 17;
     private static final int HASH_SEED_31 = 31;
 
     private ImportMonitor() {
 
     }
 
     public static void main(String[] args) {
 
         int importFileCheckCount = 0;
 
         while (true) {
             LOGGER.info("******** Import Logger & Monitor wakes up ********");
 
             int monitoringFrequencyInMinutes = Integer.parseInt(getProperty("importerMonitor.frequency.minutes"));
             numberOfLinesToRead = monitoringFrequencyInMinutes / FREQUENCY_OF_LOGGING_IMPORT_FILE_COUNTS_IN_MINUTES;
 
             LOGGER.info("Import file counts will be logged every {} minutes, whereas a "
                     + "health check will be done every {} minutes. Each monitoring will check the last {} lines "
                     + "of the log", new Object[]{FREQUENCY_OF_LOGGING_IMPORT_FILE_COUNTS_IN_MINUTES,
                     monitoringFrequencyInMinutes, numberOfLinesToRead});
 
             StopWatch sw = new StopWatch();
             sw.start();
 
             importFileCheckCount = importFileCheckCount + FREQUENCY_OF_LOGGING_IMPORT_FILE_COUNTS_IN_MINUTES;
 
             /**
              * Get the folders that will be monitored
              */
             List<FolderToMonitor> foldersToMonitor = getFoldersToMonitor();
 
             /**
              * Count the number of files in these folders
              */
             setTheNumberOfCurrentFilesToFolderObjects(foldersToMonitor);
 
             /**
              * Log counts to a file
              */
             logNumberOfFiles(foldersToMonitor);
 
             /**
              * If it is time, check the overall monitor stability as well
              */
             if (importFileCheckCount == numberOfLinesToRead) {
                 monitorImportProcess(foldersToMonitor);
 
                 importFileCheckCount = 0;
             } else {
                 LOGGER.info("Next monitoring will happen in {} minutes",
                         numberOfLinesToRead - importFileCheckCount);
             }
 
             sw.stop();
             LOGGER.info("ImportMonitor ends, it took {} (mm:ss)",
                     new SimpleDateFormat("mm:ss").format(sw.getTotalTimeMillis()));
 
             /**
              * Sleep for (frequency - execution time) seconds
              */
             long maxTimeToSleep = FREQUENCY_OF_LOGGING_IMPORT_FILE_COUNTS_IN_MINUTES * SECONDS_IN_MINUTE * MILLISECONDS;
             long executionTime = sw.getTotalTimeMillis();
             long timeToSleep = maxTimeToSleep - executionTime;
 
             // if execution time is more than max time to sleep, then sleep for the max time
             if (timeToSleep < 0) {
                 timeToSleep = maxTimeToSleep;
             }
 
             LOGGER.info("ImportMonitor will now sleep for {} (mm:ss)",
                     new SimpleDateFormat("mm:ss").format(timeToSleep));
 
             try {
                 Thread.sleep(timeToSleep);
             } catch (InterruptedException e) {
                 LOGGER.error("Import Monitor could not sleep: ", e); // possible insomnia
                 System.exit(0);
             }
         }
     }
 
     /**
      * Counts the number of files in folders and sets those values to folder objects
      */
     private static void setTheNumberOfCurrentFilesToFolderObjects(List<FolderToMonitor> foldersToMonitor) {
         for (FolderToMonitor folderToMonitor : foldersToMonitor) {
             folderToMonitor.setCurrentNumberOfFiles(getNumberOfFilesInDirectory(
                     folderToMonitor.getPathIncludingName()));
         }
     }
 
     private static void monitorImportProcess(List<FolderToMonitor> foldersToMonitor) {
         /**
          * Read some lines from the file
          */
 
         List<String> lines = getLastNLinesOfFile(numberOfLinesToRead);
 
         /**
          * Make sure all lines have the same number of folders, and that matches folders to monitor now
          */
         if (doNumberOfFoldersInLogFileAndPropertiesFileMatch(lines, foldersToMonitor)) {
             /**
              * Convert them to meaningful objects
              */
             List<CountRecord> countRecords = getCountRecordsFromLines(lines, foldersToMonitor);
 
             /**
              * Check the records to see if files are static or they exceed the limit
              */
             if (areThereEnoughDataToMonitor(countRecords)) {
 
                 List<FolderToMonitor> foldersThatHaveStaticFiles = getFoldersWhoseNumberOfFilesAreStatic(countRecords);
                 List<FolderToMonitor> foldersWhoseNumberOfFilesExceedTheirLimits =
                         getFoldersWhoseNumberOfFilesExceedTheirLimits(countRecords);
 
                 if (foldersThatHaveStaticFiles.size() > 0 || foldersWhoseNumberOfFilesExceedTheirLimits.size() > 0) {
                     /**
                      * Send an email if there are problems with the importer
                      */
                     sendAWarningEmail(foldersThatHaveStaticFiles, foldersWhoseNumberOfFilesExceedTheirLimits,
                             countRecords);
                 } else {
                     LOGGER.info("Importer appears to be working fine.");
                 }
             }
         } else {
             LOGGER.error("Skipping monitoring folders as number of folders in log file and number of folders "
                     + "defined in properties file do not match.");
         }
     }
 
     private static boolean doNumberOfFoldersInLogFileAndPropertiesFileMatch(List<String> logLines,
                                                                             List<FolderToMonitor> foldersToMonitor) {
         boolean folderSizeInLinesMatch;
         int lastFolderSizeInLine = -1;
 
         /**
          * Check if lines in log file have the same number of folders
          */
         if (logLines.size() > 0) {
             lastFolderSizeInLine = getNumberOfFoldersInLogLine(logLines.get(0));
         }
 
         for (String line : logLines) {
             if (StringUtils.isBlank(line)) {
                 LOGGER.info("Empty record is encountered, this might be the first line");
             } else {
                 int currentNumberOfFoldersInThisLogLine = getNumberOfFoldersInLogLine(line);
 
                 folderSizeInLinesMatch = lastFolderSizeInLine == currentNumberOfFoldersInThisLogLine;
 
                 if (!folderSizeInLinesMatch) {
                     LOGGER.warn("Folders in properties file and log file do not match. If some folders were "
                             + "added/removed recently, then this may be the reason. Folder count of previous line "
                             + " is {}, whereas another line's count is {} ({})",
                             new Object[]{currentNumberOfFoldersInThisLogLine, lastFolderSizeInLine, line});
 
                     return false;
                 } else {
                     lastFolderSizeInLine = getNumberOfFoldersInLogLine(line);
                 }
             }
         }
 
         /**
          * Compare the folder count in log lines with the count of folders that we will monitor now
          */
         return foldersToMonitor.size() == lastFolderSizeInLine;
     }
 
     /**
      * Returns the number of folders defined in a line of log.
      * <p/>
      * If log file looks like 1985-07-03,1,2,3 then it returns 3
      */
     private static int getNumberOfFoldersInLogLine(String line) {
         String[] recordDataSections = line.split(RECORD_DATA_DELIMITER);
 
         if (recordDataSections != null) {
             // first part is date, and the last part is comment sections, so subtract 2 from the total number
             return recordDataSections.length - 2;
         } else {
             return 0;
         }
     }
 
     /**
      * Lists the folders to be monitored, which are defined in properties files
      */
     private static List<FolderToMonitor> getFoldersToMonitor() {
         List<FolderToMonitor> foldersToMonitor = new ArrayList<FolderToMonitor>();
 
         List<String> propertyNames = getPropertyNamesStartingWith("importerMonitor.directory.path");
 
         FolderToMonitor folderToMonitor;
         for (String propertyName : propertyNames) {
             folderToMonitor = new FolderToMonitor();
 
             String[] propertyNameParts = propertyName.split("\\.");
 
             if (propertyNameParts != null && propertyNameParts.length > 0) {
                 try {
                     folderToMonitor.setId(Integer.parseInt(propertyNameParts[propertyNameParts.length - 1]));
                     folderToMonitor.setPathIncludingName(getProperty(propertyName));
                     folderToMonitor.setMaxNumberOfFiles(Integer.parseInt(getProperty(
                             "importerMonitor.directory.maxNumberOfFiles." + folderToMonitor.getId())));
                 } catch (NumberFormatException e) {
                     LOGGER.error("Could not retrieve property value {}, possible faulty property name definition",
                             propertyName, e);
                 }
 
                 if (!foldersToMonitor.contains(folderToMonitor)) {
                     foldersToMonitor.add(folderToMonitor);
                 }
             }
         }
 
         Collections.sort(foldersToMonitor, FolderToMonitor.Order.ById.descending());
 
         return foldersToMonitor;
     }
 
     /**
      * Appends the current time and file counts to importer data file
      */
     private static void logNumberOfFiles(List<FolderToMonitor> foldersToMonitor) {
         File countFile = getTodaysCountFile();
 
         try {
             FileOutputStream fileOutStream = new FileOutputStream(countFile, true); // append data
 
             String countRecord = getCountDataToWriteToFile(foldersToMonitor);
 
             fileOutStream.write(countRecord.getBytes());
             LOGGER.info("Appended this line: \"{}\" to count file {}", countRecord, countFile.getAbsolutePath());
 
             fileOutStream.flush();
             fileOutStream.close();
         } catch (IOException e) {
             LOGGER.error("Could not persist number of files in folders", e);
         }
     }
 
     /**
      * Returns the log file that will be used today - there is a rotation in log files
      */
     private static File getTodaysCountFile() {
         SimpleDateFormat dateTimeFormat = new SimpleDateFormat(COUNT_LOG_FILENAME_FORMAT);
 
         String fileName = getProperty("importer.data.file.name") + dateTimeFormat.format(new Date());
 
         return new File(getProperty("importer.data.file.directory.path") + "/" + fileName);
     }
 
     /**
      * Returns the record that needs to be appended to importer data file
      */
     private static String getCountDataToWriteToFile(List<FolderToMonitor> foldersToMonitor) {
         SimpleDateFormat dateTimeFormat = new SimpleDateFormat(RECORD_DATE_FORMAT);
 
         String countData = "\n" + dateTimeFormat.format(new Date());
 
         /**
          * Append counts
          */
         for (FolderToMonitor folderToMonitor : foldersToMonitor) {
             countData = countData + RECORD_DATA_DELIMITER + folderToMonitor.getCurrentNumberOfFiles();
         }
 
         /**
          * Append folder names for reference
          */
         countData = countData + "," + COMMENT_PREFIX + "Folders:";
         for (FolderToMonitor folderToMonitor : foldersToMonitor) {
             countData = countData + folderToMonitor.getName() + "-";
         }
 
         return countData;
     }
 
     /**
      * Returns number of files in a directory, excluding hidden files of unix environment and directories
      */
     private static int getNumberOfFilesInDirectory(String directoryPath) {
         File[] files = new File(directoryPath).listFiles();
 
         int count = 0;
         if (files != null) {
             for (File file : files) {
                 if (file.isFile() && !file.getName().startsWith(".")) { // no no no, we don't want any hidden files
                     count++;
                 }
             }
         }
 
         LOGGER.info("Number of files in directory {} is {}", directoryPath, count);
 
         return count;
     }
 
     /**
      * Checks if there is enough data (file counts) in log file for monitoring
      */
     private static boolean areThereEnoughDataToMonitor(List<CountRecord> countRecords) {
         if (countRecords.size() != numberOfLinesToRead) {
             LOGGER.info("There are not enough data (only {} lines) to monitor. There should be at least {} lines "
                     + "for Import monitor to process.",
                     countRecords.size(), numberOfLinesToRead);
 
             return false;
         }
 
         return true;
     }
 
     /**
      * Checks if the number of files in given directories are static over a certain period of time
      */
     private static List<FolderToMonitor> getFoldersWhoseNumberOfFilesAreStatic(List<CountRecord> countRecords) {
         List<FolderToMonitor> foldersWhoseFilesAreStatic = new ArrayList<FolderToMonitor>();
 
         /**
          * First, treat all folders with files as static
          */
         for (CountRecord countRecord : countRecords) {
             for (FolderToMonitor folderToMonitor : countRecord.getFoldersToMonitor()) {
                if (folderToMonitor.getCurrentNumberOfFiles() > 0 &&
                        !foldersWhoseFilesAreStatic.contains(folderToMonitor)) {
                     foldersWhoseFilesAreStatic.add(folderToMonitor);
                 }
             }
         }
 
         /**
          * What we have in log files is a matrix (x,y) where x denotes folders and y denotes number of files in
          *      that folder. It is advised that you see the log file before continuing.. The data is stored in a
          *      primitive file instead of a relational database, hence the long explanations..
          *
          * We will first compare (x, y), (x, y+1), (x, y+2) to see if files in a folder was static over time.
          * Then we will check the rest of the folders, starting by comparing (x+1, y), (x+1, y+1), (x+1, y+2) ...
          */
 
         if (countRecords.size() == numberOfLinesToRead) {
 
             // first line of the log
             CountRecord firstLogRecord = countRecords.get(0);
 
             // total number of folders that are logged in his line
             int numberOfFoldersToCheck = firstLogRecord.getFoldersToMonitor().size();
 
             /**
              * We will make iterations totaling to the number of folders that needs to be monitored
              */
             for (int i = 0; i < numberOfFoldersToCheck; i++) {
 
                 /**
                  * i-th folder of the first log record. We will compare this value with other log records (other
                  *      recordings recently)
                  */
                 int firstRecordsFolderFileCount = firstLogRecord.getFoldersToMonitor().get(i).getCurrentNumberOfFiles();
 
                 /**
                  * Go backwards in time (logs) to see if the file count was always the same or not
                  */
                 for (CountRecord countRecord : countRecords) {
 
                     // i-th folder of the current log record
                     FolderToMonitor thisRecordsFolder = countRecord.getFoldersToMonitor().get(i);
 
                     // number of files of i-th folder of the current log record
                     int thisRecordsFolderFileCount = thisRecordsFolder.getCurrentNumberOfFiles();
 
                     /**
                      * If this log record's i-th folder's file count is different than the first log record's
                      *      i-th folder's file count, then it means importer is working on this folder
                      */
                     if (firstRecordsFolderFileCount != thisRecordsFolderFileCount) {
                         foldersWhoseFilesAreStatic.remove(thisRecordsFolder);
                     }
                 }
             }
         } else {
             LOGGER.warn("There are not enough records to check (only {} records in the file)", countRecords.size());
         }
 
         /**
          * Log the findings
          */
         for (FolderToMonitor monitoredFolder : foldersWhoseFilesAreStatic) {
             LOGGER.info("Files are static in directory {}", monitoredFolder.getPathIncludingName());
         }
 
         return foldersWhoseFilesAreStatic;
     }
 
     /**
      * Checks if the number of pending files for importer exceeds a given limit
      */
     private static List<FolderToMonitor> getFoldersWhoseNumberOfFilesExceedTheirLimits(List<CountRecord> countRecords) {
         List<FolderToMonitor> foldersWhoseFilesExceedTheirLimits = new ArrayList<FolderToMonitor>();
 
         if (countRecords.size() > 0) {
             List<CountRecord> countRecordsToTest = new ArrayList<CountRecord>(countRecords);
 
             Collections.sort(countRecordsToTest, CountRecord.Order.ByRecordTime.descending());
             CountRecord firstCountRecord = countRecords.get(0);
 
             int numberOfFoldersToCheck = firstCountRecord.getFoldersToMonitor().size();
 
             for (int i = 0; i < numberOfFoldersToCheck; i++) {
 
                 for (CountRecord countRecord : countRecords) {
 
                     FolderToMonitor thisRecordsFolder = countRecord.getFoldersToMonitor().get(i);
 
                     if (thisRecordsFolder.getCurrentNumberOfFiles() > thisRecordsFolder.getMaxNumberOfFiles()
                             && !foldersWhoseFilesExceedTheirLimits.contains(thisRecordsFolder)) {
                         LOGGER.info("Folder {}'s files ({}) exceed its limit ({})",
                                 new Object[]{thisRecordsFolder.getId(), thisRecordsFolder.getCurrentNumberOfFiles(),
                                         thisRecordsFolder.getMaxNumberOfFiles()});
 
                         foldersWhoseFilesExceedTheirLimits.add(thisRecordsFolder);
                     }
                 }
             }
         }
 
         return foldersWhoseFilesExceedTheirLimits;
     }
 
     private static void sendAWarningEmail(List<FolderToMonitor> foldersThatHaveStaticFiles,
                                           List<FolderToMonitor> foldersWhoseNumberOfFilesExceedTheirLimits,
                                           List<CountRecord> countRecords) {
         try {
             Resource resource = new ClassPathResource("/" + PROJECT_PROPERTIES_FILE);
             Properties props = PropertiesLoaderUtils.loadProperties(resource);
 
             String subject = "Problems encountered in Patient View XML Importer";
 
             String body = getWarningEmailBody(foldersThatHaveStaticFiles, foldersWhoseNumberOfFilesExceedTheirLimits,
                     countRecords);
 
             String fromAddress = props.getProperty("noreply.email");
 
             // todo For testing purposes this property is overridden
 //            String[] toAddresses = {props.getProperty("support.email")};
             String[] toAddresses = {"patientview-testing@solidstategroup.com"};
 
             sendEmail(fromAddress, toAddresses, null, subject, body);
         } catch (IOException e) {
             LOGGER.error("Could not find properties file: {}", e);
         }
     }
 
     public static void sendEmail(String from, String[] to, String[] bcc, String subject, String body) {
         if (StringUtils.isBlank(from)) {
             throw new IllegalArgumentException("Cannot send mail missing 'from'");
         }
 
         if ((to == null || to.length == 0) && (bcc == null || bcc.length == 0)) {
             throw new IllegalArgumentException("Cannot send mail missing recipients");
         }
 
         if (StringUtils.isBlank(subject)) {
             throw new IllegalArgumentException("Cannot send mail missing 'subject'");
         }
 
         if (StringUtils.isBlank(body)) {
             throw new IllegalArgumentException("Cannot send mail missing 'body'");
         }
 
         ApplicationContext context =
                 new ClassPathXmlApplicationContext(new String[]{"classpath*:context-standalone.xml"});
 
         JavaMailSender javaMailSender = (JavaMailSender) context.getBean("javaMailSender");
 
         MimeMessage message = javaMailSender.createMimeMessage();
         MimeMessageHelper messageHelper;
 
         try {
             messageHelper = new MimeMessageHelper(message, true);
             messageHelper.setTo(to);
             if (bcc != null && bcc.length > 0) {
                 Address[] bccAddresses = new Address[bcc.length];
                 for (int i = 0; i < bcc.length; i++) {
                     bccAddresses[i] = new InternetAddress(bcc[i]);
                 }
                 message.addRecipients(Message.RecipientType.BCC, bccAddresses);
             }
             messageHelper.setFrom(from);
             messageHelper.setSubject(subject);
             messageHelper.setText(body, false); // Note: the second param indicates to send plaintext
 
             javaMailSender.send(messageHelper.getMimeMessage());
 
             LOGGER.info("Sent an email about Importer issues. From: {} To: {}", from, Arrays.toString(to));
         } catch (Exception e) {
             LOGGER.error("Could not send email: {}", e);
         }
     }
 
     private static String getWarningEmailBody(List<FolderToMonitor> foldersThatHaveStaticFiles,
                                               List<FolderToMonitor> foldersWhoseNumberOfFilesExceedTheirLimits,
                                               List<CountRecord> countRecords) {
         String emailBody = "";
         String newLine = System.getProperty("line.separator");
 
         emailBody += "[This is an automated email from Renal PatientView - do not reply to this email]";
         emailBody += newLine;
         emailBody += newLine + "There are some problems in XML Importer. Please see below for details.";
         emailBody += newLine;
 
         if (foldersThatHaveStaticFiles.size() > 0) {
             emailBody += newLine + "Importer has not imported any files in some folders recently. These folders are:";
             emailBody += newLine;
 
             for (FolderToMonitor folder : foldersThatHaveStaticFiles) {
                 emailBody += newLine + "Folder ID: " + folder.getId() + " Path: " + folder.getPathIncludingName()
                         + " Number of files: " + folder.getCurrentNumberOfFiles();
             }
 
             emailBody += newLine;
             emailBody += newLine;
         }
 
         if (foldersWhoseNumberOfFilesExceedTheirLimits.size() > 0) {
             emailBody += newLine + "Files are in some folders are above the threshold. These folders are:";
             emailBody += newLine;
 
             for (FolderToMonitor folder : foldersWhoseNumberOfFilesExceedTheirLimits) {
                 emailBody += newLine + "Folder ID: " + folder.getId() + " Path: " + folder.getPathIncludingName()
                         + " Number of files: " + folder.getCurrentNumberOfFiles() + " Threshold: "
                         + folder.getMaxNumberOfFiles();
             }
         }
 
         emailBody += newLine;
         emailBody += newLine;
         emailBody += newLine + "Please see the following most recent file count records for reference:";
         emailBody += newLine;
 
         List<CountRecord> countRecordsToSend = new ArrayList<CountRecord>(countRecords);
         Collections.sort(countRecordsToSend, CountRecord.Order.ByRecordTime.descending());
 
         for (CountRecord countRecord : countRecordsToSend) {
             emailBody += countRecord;
         }
 
         return emailBody;
     }
 
     /**
      * Returns the last N lines of a file. Assumes lines are terminated by |n ascii character
      */
     private static List<String> getLastNLinesOfFile(int numberOfLinesToReturn) {
         List<String> lastNLines = new ArrayList<String>();
         java.io.RandomAccessFile fileHandler = null;
 
         try {
             File file = getTodaysCountFile();
 
             fileHandler = new java.io.RandomAccessFile(file, "r");
 
             long totalNumberOfCharactersInFile = file.length() - 1;
 
             StringBuilder sb = new StringBuilder();
             int numberOfLinesRead = 0;
 
             /**
              * loop through characters in file, construct lines out of characters, add lines to a list
              */
             for (long currentCharacter = totalNumberOfCharactersInFile; currentCharacter != -1; currentCharacter--) {
                 fileHandler.seek(currentCharacter);
 
                 int readByte = fileHandler.readByte();
 
                 if (readByte == LINE_FEED || readByte == CARRIAGE_RETURN) {
                     if (numberOfLinesRead == numberOfLinesToReturn) {
                         break;
                     }
 
                     numberOfLinesRead++;
 
                     /**
                      * add line to line list
                      */
                     String currentLine = sb.reverse().toString();
                     sb = new StringBuilder();
 
                     if (StringUtils.isNotBlank(currentLine)) {
                         lastNLines.add(currentLine);
                     } else {
                         LOGGER.error("Read line does not contain any data");
                         continue;
                     }
                 } else {
                     sb.append((char) readByte);
                 }
             }
 
             /**
              * add the last line
              */
             lastNLines.add(sb.reverse().toString());
         } catch (Exception e) {
             LOGGER.error("Can not find today's file", e);
         } finally {
             if (fileHandler != null) {
                 try {
                     fileHandler.close();
                 } catch (IOException e) {
                     fileHandler = null;
                 }
             }
         }
 
         return lastNLines;
     }
 
     private static String getProperty(String propertyName) {
         Resource resource = new ClassPathResource("/" + PROJECT_PROPERTIES_FILE);
         Properties props = null;
         String propertyValue = "";
 
         try {
             props = PropertiesLoaderUtils.loadProperties(resource);
 
             propertyValue = props.getProperty(propertyName);
         } catch (IOException e) {
             LOGGER.error("Could not find properties file: {}", e);
         }
 
         return propertyValue;
     }
 
     /**
      * Returns all property names that starts with the given string
      *
      * @return empty array list if no property name is found
      */
     private static List<String> getPropertyNamesStartingWith(String queriedProperyName) {
         Resource resource = new ClassPathResource("/" + PROJECT_PROPERTIES_FILE);
         Properties props = null;
         Set<String> allPropertyNames;
         List<String> propertyNames = new ArrayList<String>();
 
         try {
             props = PropertiesLoaderUtils.loadProperties(resource);
 
             allPropertyNames = props.stringPropertyNames();
 
             for (String propertyName : allPropertyNames) {
                 if (propertyName.startsWith(queriedProperyName)) {
                     propertyNames.add(propertyName);
                 }
             }
         } catch (IOException e) {
             LOGGER.error("Could not find properties file: {}", e);
         }
 
         return propertyNames;
     }
 
     /**
      * Convert lines on the file into sensible objects
      */
     private static List<CountRecord> getCountRecordsFromLines(List<String> lines,
                                                               List<FolderToMonitor> foldersToMonitor) {
         List<CountRecord> countRecords = new ArrayList<CountRecord>();
 
         for (String line : lines) {
             String date = extractDateAsString(line);
 
             List<Integer> numberOfFilesInDirectories = extractNumberOfFilesInDirectories(line);
 
             if (isValidCountRecord(date, RECORD_DATE_FORMAT, numberOfFilesInDirectories)) {
 
                 countRecords.add(CountRecord.fromDateAndFileCounts(date, RECORD_DATE_FORMAT,
                         numberOfFilesInDirectories, foldersToMonitor));
             } else {
                 LOGGER.error("Invalid record: {}", line);
             }
         }
 
         return countRecords;
     }
 
     /**
      * Check if the parameters are in correct formats
      */
     private static boolean isValidCountRecord(String dateString, String dateFormant,
                                               List<Integer> numberOfFilesInDirectories) {
         return numberOfFilesInDirectories != null && numberOfFilesInDirectories.size() > 0
                 && parseDate(dateString, dateFormant) != null;
     }
 
     private static Date parseDate(String maybeDate, String format) {
         Date date = null;
 
         try {
             DateTimeFormatter fmt = DateTimeFormat.forPattern(format);
             DateTime dateTime = fmt.parseDateTime(maybeDate);
             date = dateTime.toDate();
         } catch (Exception e) {
             LOGGER.error("Invalid date: {}", maybeDate, e);
         }
 
         return date;
     }
 
     private static String extractDateAsString(String line) {
         return extractRecordDataOnThisPosition(line, DATE_POSITION_IN_RECORD);
     }
 
     private static List<Integer> extractNumberOfFilesInDirectories(String line) {
         List<Integer> numberOfFilesList = new ArrayList<Integer>();
 
         if (StringUtils.isBlank(line)) {
             LOGGER.info("Empty record is encountered, this might be the first line");
         } else {
             String[] recordDataSections = line.split(RECORD_DATA_DELIMITER);
 
             for (int i = DATE_POSITION_IN_RECORD + 1; recordDataSections.length > i; i++) {
                 try {
                     if (!recordDataSections[i].startsWith(COMMENT_PREFIX)) {
                         numberOfFilesList.add(Integer.parseInt(recordDataSections[i]));
                     }
                 } catch (NumberFormatException e) {
                     LOGGER.error("Could not parse file count {} into an integer. The log line is: {}",
                             new Object[]{recordDataSections[i], line}, e);
                     break;
                 }
             }
         }
 
         return numberOfFilesList;
     }
 
     private static String extractRecordDataOnThisPosition(String line, int position) {
         String[] recordDataSections = line.split(RECORD_DATA_DELIMITER);
 
         if (recordDataSections != null && recordDataSections.length > position) {
             return recordDataSections[position];
         } else {
             return null;
         }
     }
 
     private static class FolderToMonitor {
 
         private int id;
         private String pathIncludingName;
         private int maxNumberOfFiles;
         private int currentNumberOfFiles;
 
         public String getPathIncludingName() {
             return pathIncludingName;
         }
 
         public void setPathIncludingName(String pathIncludingName) {
             this.pathIncludingName = pathIncludingName;
         }
 
         public int getMaxNumberOfFiles() {
             return maxNumberOfFiles;
         }
 
         public void setMaxNumberOfFiles(int maxNumberOfFiles) {
             this.maxNumberOfFiles = maxNumberOfFiles;
         }
 
         public String getName() {
             if (StringUtils.isNotBlank(pathIncludingName)) {
                 String[] pathParts = pathIncludingName.split("\\/");
 
                 if (pathParts != null && pathParts.length > 0) {
                     return pathParts[pathParts.length - 1];
                 } else {
                     LOGGER.error("Could not retrieve folder name from path {}");
                 }
             }
 
             return pathIncludingName;
         }
 
         public int getCurrentNumberOfFiles() {
             return currentNumberOfFiles;
         }
 
         public void setCurrentNumberOfFiles(int currentNumberOfFiles) {
             this.currentNumberOfFiles = currentNumberOfFiles;
         }
 
         public int getId() {
             return id;
         }
 
         public void setId(int id) {
             this.id = id;
         }
 
         public static enum Order implements Comparator<FolderToMonitor> {
             ById() {
                 public int compare(FolderToMonitor leftRecord, FolderToMonitor rightRecord) {
                     return Double.compare(leftRecord.getId(), rightRecord.getId()) * -1;
                 }
             };
 
             public abstract int compare(FolderToMonitor leftRecord, FolderToMonitor rightRecord);
 
             public Comparator ascending() {
                 return this;
             }
 
             public Comparator descending() {
                 return Collections.reverseOrder(this);
             }
         }
 
         public boolean equals(Object obj) {
             if (obj == null) {
                 return false;
             }
             if (obj == this) {
                 return true;
             }
             if (!(obj instanceof FolderToMonitor)) {
                 return false;
             }
 
             FolderToMonitor rhs = (FolderToMonitor) obj;
             return new EqualsBuilder().append(pathIncludingName, rhs.getPathIncludingName()).isEquals();
         }
 
         public int hashCode() {
             return new HashCodeBuilder(HASH_SEED_17, HASH_SEED_31).append(pathIncludingName)
                     .append(maxNumberOfFiles).toHashCode();
         }
     }
 
     private static final class CountRecord {
         private Date recordTime = null;
         private List<FolderToMonitor> foldersToMonitor;
 
         private CountRecord() {
 
         }
 
         public static CountRecord fromDateAndFileCounts(String dateString, String dateFormat,
                                                         List<Integer> currentFileCountsInFolders,
                                                         List<FolderToMonitor> foldersInPropertiesFileToMonitor) {
             CountRecord countRecord = new CountRecord();
             countRecord.setRecordTime(parseDate(dateString, dateFormat));
 
             List<FolderToMonitor> foldersInLogFileToMonitor = new ArrayList<FolderToMonitor>();
 
             for (int i = 0; i < currentFileCountsInFolders.size()
                     && i < foldersInPropertiesFileToMonitor.size(); i++) {
 
                 FolderToMonitor folderToMonitorInPropertiesFile = foldersInPropertiesFileToMonitor.get(i);
 
                 FolderToMonitor folderToMonitor = new FolderToMonitor();
                 folderToMonitor.setId(folderToMonitorInPropertiesFile.getId());
                 folderToMonitor.setPathIncludingName(folderToMonitorInPropertiesFile.getPathIncludingName());
                 folderToMonitor.setMaxNumberOfFiles(folderToMonitorInPropertiesFile.getMaxNumberOfFiles());
                 folderToMonitor.setCurrentNumberOfFiles(currentFileCountsInFolders.get(i));
 
                 foldersInLogFileToMonitor.add(folderToMonitor);
             }
 
             countRecord.setFoldersToMonitor(foldersInLogFileToMonitor);
 
             return countRecord;
         }
 
         public static enum Order implements Comparator<CountRecord> {
             ByRecordTime() {
                 public int compare(CountRecord leftRecord, CountRecord rightRecord) {
                     return leftRecord.getRecordTime().compareTo(rightRecord.getRecordTime()) * -1;
                 }
             };
 
             public abstract int compare(CountRecord leftRecord, CountRecord rightRecord);
 
             public Comparator ascending() {
                 return this;
             }
 
             public Comparator descending() {
                 return Collections.reverseOrder(this);
             }
         }
 
         public Date getRecordTime() {
             return recordTime;
         }
 
         public void setRecordTime(Date recordTime) {
             this.recordTime = recordTime;
         }
 
         public List<FolderToMonitor> getFoldersToMonitor() {
             return foldersToMonitor;
         }
 
         public void setFoldersToMonitor(List<FolderToMonitor> foldersToMonitor) {
             this.foldersToMonitor = foldersToMonitor;
         }
 
         public String toString() {
             SimpleDateFormat dateTimeFormat = new SimpleDateFormat(RECORD_DATE_FORMAT);
             String countRecordAsString = "\n" + dateTimeFormat.format(recordTime);
 
             for (FolderToMonitor folderToMonitor : foldersToMonitor) {
                 countRecordAsString = countRecordAsString + RECORD_DATA_DELIMITER
                         + folderToMonitor.getCurrentNumberOfFiles();
             }
 
             countRecordAsString = countRecordAsString + "," + COMMENT_PREFIX + "Folders:";
             for (FolderToMonitor folderToMonitor : foldersToMonitor) {
                 countRecordAsString = countRecordAsString + folderToMonitor.getName() + "-";
             }
 
             return countRecordAsString;
         }
     }
 }
