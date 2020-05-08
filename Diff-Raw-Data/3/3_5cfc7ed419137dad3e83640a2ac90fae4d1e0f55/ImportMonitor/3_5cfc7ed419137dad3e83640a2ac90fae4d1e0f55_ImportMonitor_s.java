 package com.worthsoln.monitoring;
 
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
 import org.springframework.util.StringUtils;
 
 import javax.mail.Address;
 import javax.mail.Message;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * Monitors XML Import process to see if it is stalled or not working properly
  *
  * @author Deniz Ozger
  */
 public class ImportMonitor {
 
     private static final int NUMBER_OF_LINES_TO_READ = 10;
     private static final int PENDING_FILE_LIMIT = 10;
     private static final int IMPORTER_EXECUTION_FREQUENCY_IN_MINUTES = 1;
 
     private static final String RECORD_DATA_DELIMITER = ",";
     private static final String RECORD_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
     private static final int DATE_POSITION_IN_RECORD = 0;
     private static final int NUMBER_OF_FILES_IN_PROTON_DIRECTORY_INFORMATION_POSITION_IN_RECORD = 1;
     private static final int NUMBER_OF_FILES_IN_RPV_XML_DIRECTORY_INFORMATION_POSITION_IN_RECORD = 2;
     private static final String PROJECT_PROPERTIES_FILE = "patientview.properties";
     private static final String COUNT_LOG_FILENAME_FORMAT = "yyyy-MM-dd";
 
     private static enum ImporterError {
         NUMBER_OF_PROTON_FILES_IS_STATIC,
         NUMBER_OF_RPV_XML_FILES_IS_STATIC,
         NUMBER_OF_PROTON_FILES_EXCEEDS_LIMIT,
         NUMBER_OF_RPV_XML_FILES_EXCEEDS_LIMIT
     }
 
     private static enum FileType {
         PROTON,
         RPV_XML
     }
 
     private static final int LINE_FEED = 0xA;
     private static final int CARRIAGE_RETURN = 0xD;
 
     private static final Logger LOGGER = LoggerFactory.getLogger(ImportMonitor.class);
 
     public static void main(String[] args) {
         while (true) {
             LOGGER.info("ImportMonitor wakes up");
             StopWatch sw = new StopWatch();
             sw.start();
 
             /**
              * Check if importer is working fine
              */
             monitorImportProcess();
 
             sw.stop();
             LOGGER.info("ImportMonitor ends, it took {} ",
                     new SimpleDateFormat("mm:ss").format(sw.getTotalTimeMillis()));
 
             long maxTimeToSleep = IMPORTER_EXECUTION_FREQUENCY_IN_MINUTES * 60 * 1000;
             long executionTime = sw.getTotalTimeMillis();
             long timeToSleep = maxTimeToSleep - executionTime;
 
             // if execution time is more than max time to sleep, then sleep for the max time
             if (timeToSleep < 0) {
                 timeToSleep = maxTimeToSleep;
             }
 
             LOGGER.info("Importer will sleep for {} seconds", timeToSleep / 1000);
 
             try {
                 Thread.sleep(timeToSleep);
             } catch (InterruptedException e) {
                 LOGGER.error("Import Monitor could not sleep: ", e); // possible insomnia
                 System.exit(0);
             }
         }
     }
 
     private static void monitorImportProcess() {
         /**
          * Count the number of pending files in both importer directories
          */
         int protonDirectoryFileCount = getNumberOfFilesInDirectory(getProperty("importer.proton_files.directory.path"));
         int rpvXmlDirectoryFileCount = getNumberOfFilesInDirectory(getProperty("importer.rpvxml_files.directory.path"));
 
         /**
          * Write the counts to the file
          */
         logNumberOfFiles(protonDirectoryFileCount, rpvXmlDirectoryFileCount);
 
         /**
          * Read some lines from the file
          */
 
         List<String> lines = getLastNLinesOfFile(NUMBER_OF_LINES_TO_READ);
 
         /**
          * Convert them to Record objects
          */
         List<CountRecord> countRecords = getCountRecordsFromLines(lines);
 
         /**
          * Check if files are static or they exceed the limit, send an email if they are
          */
         checkForErrorsAndSendAWarningEmail(countRecords);
     }
 
     /**
      * Appends the current time and file counts to importer data file
      */
     private static void logNumberOfFiles(int protonDirectoryFileCount, int rpvXmlDirectoryFileCount) {
         File countFile = getTodaysCountFile();
 
         try {
             FileOutputStream fileOutStream = new FileOutputStream(countFile, true); // append data
 
             String countRecord = getCountDataToWriteToFile(protonDirectoryFileCount, rpvXmlDirectoryFileCount);
 
             fileOutStream.write(countRecord.getBytes());
             LOGGER.info("Appended {} to count file {}", countRecord, countFile.getAbsolutePath());
 
             fileOutStream.flush();
             fileOutStream.close();
         } catch (IOException e) {
             LOGGER.error("Could not persist number of Proton and RpvXml files", e);
         }
     }
 
     private static File getTodaysCountFile() {
         SimpleDateFormat dateTimeFormat = new SimpleDateFormat(COUNT_LOG_FILENAME_FORMAT);
 
         String fileName = getProperty("importer.data.file.name") + dateTimeFormat.format(new Date());
 
         return new File(getProperty("importer.data.file.directory.path") + "/" + fileName);
     }
 
     /**
      * Returns the record that needs to be appended to importer data file
      */
     private static String getCountDataToWriteToFile(int protonDirectoryFileCount, int rpvXmlDirectoryFileCount) {
         SimpleDateFormat dateTimeFormat = new SimpleDateFormat(RECORD_DATE_FORMAT);
 
         return "\n" + dateTimeFormat.format(new Date()) + RECORD_DATA_DELIMITER + protonDirectoryFileCount +
                 RECORD_DATA_DELIMITER + rpvXmlDirectoryFileCount;
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
 
     private static void checkForErrorsAndSendAWarningEmail(List<CountRecord> countRecords) {
         if (countRecords.size() == NUMBER_OF_LINES_TO_READ && (isNumberOfProtonFilesStatic(countRecords) ||
                 isNumberOfRpvXmlFilesStatic(countRecords) ||
                 doesNumberOfPendingProtonFilesExceedLimit(countRecords, PENDING_FILE_LIMIT) ||
                 doesNumberOfPendingXmlRpvFilesExceedLimit(countRecords, PENDING_FILE_LIMIT))) {
 
             try {
                 Resource resource = new ClassPathResource("/" + PROJECT_PROPERTIES_FILE);
                 Properties props = PropertiesLoaderUtils.loadProperties(resource);
 
                 String subject = "Problems encountered in Patient View XML Importer";
 
                 String body = getWarningEmailBody(countRecords);
 
                 String fromAddress = props.getProperty("noreply.email");
 
                 String[] toAddresses = {props.getProperty("support.email")};
 
                 sendEmail(fromAddress, toAddresses, null, subject, body);
             } catch (IOException e) {
                 LOGGER.error("Could not find properties file: {}", e);
             }
         } else {
            LOGGER.info("There aren't enough data ({} lines) to monitor or everything works well.");
         }
     }
 
     public static void sendEmail(String from, String[] to, String[] bcc, String subject, String body) {
         if (!StringUtils.hasLength(from)) {
             throw new IllegalArgumentException("Cannot send mail missing 'from'");
         }
 
         if ((to == null || to.length == 0) && (bcc == null || bcc.length == 0)) {
             throw new IllegalArgumentException("Cannot send mail missing recipients");
         }
 
         if (!StringUtils.hasLength(subject)) {
             throw new IllegalArgumentException("Cannot send mail missing 'subject'");
         }
 
         if (!StringUtils.hasLength(body)) {
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
 
     private static String getWarningEmailBody(List<CountRecord> countRecords) {
         String emailBody = "";
         String newLine = System.getProperty("line.separator");
 
         emailBody += "[This is an automated email from Renal PatientView - do not reply to this email]";
         emailBody += newLine;
         emailBody += newLine + "There are some problems in XML Importer. Please see below for details.";
         emailBody += newLine;
         emailBody += newLine;
 
         if (isNumberOfProtonFilesStatic(countRecords)) {
             emailBody += newLine + "Importer has not imported any Proton files recently.";
             emailBody += newLine;
         }
 
         if (isNumberOfRpvXmlFilesStatic(countRecords)) {
             emailBody += newLine + "Importer has not imported any RpvXml files recently.";
             emailBody += newLine;
         }
 
         if (doesNumberOfPendingProtonFilesExceedLimit(countRecords, PENDING_FILE_LIMIT)) {
             emailBody += newLine + "Number of Proton files waiting to be imported is above threshold.";
             emailBody += newLine;
         }
 
         if (doesNumberOfPendingXmlRpvFilesExceedLimit(countRecords, PENDING_FILE_LIMIT)) {
             emailBody += newLine + "Number of Rpv Xml files waiting to be imported is above threshold.";
             emailBody += newLine;
         }
 
         emailBody += newLine;
         emailBody += newLine;
         emailBody += newLine + "Please see the following most recent file count records:";
         emailBody += newLine;
         emailBody += newLine;
 
         List<CountRecord> countRecordsToSend = new ArrayList<CountRecord>(countRecords);
         Collections.sort(countRecordsToSend, CountRecord.Order.ByRecordTime.ascending());
 
         for (CountRecord countRecord : countRecordsToSend) {
             emailBody += newLine + countRecord;
             emailBody += newLine;
         }
 
         return emailBody;
     }
 
     private static boolean isNumberOfProtonFilesStatic(List<CountRecord> countRecords) {
         return isNumberOfFilesStatic(FileType.PROTON, countRecords);
     }
 
     private static boolean isNumberOfRpvXmlFilesStatic(List<CountRecord> countRecords) {
         return isNumberOfFilesStatic(FileType.RPV_XML, countRecords);
     }
 
     private static boolean doesNumberOfPendingProtonFilesExceedLimit(List<CountRecord> countRecords,
                                                                      int pendingFileLimit) {
         return doesNumberOfPendingFilesExceedLimit(FileType.PROTON, countRecords, pendingFileLimit);
     }
 
     private static boolean doesNumberOfPendingXmlRpvFilesExceedLimit(List<CountRecord> countRecords,
                                                                      int pendingFileLimit) {
         return doesNumberOfPendingFilesExceedLimit(FileType.RPV_XML, countRecords, pendingFileLimit);
     }
 
     /**
      * Checks if the number of files in given directories are static over a certain period of time
      */
     private static boolean isNumberOfFilesStatic(FileType fileType, List<CountRecord> countRecords) {
         boolean filesAreStatic = true;
 
         if (countRecords.size() == NUMBER_OF_LINES_TO_READ) {
             int firstRecordsFileCount;
 
             if (fileType == FileType.PROTON) {
                 firstRecordsFileCount = countRecords.get(0).getNumberOfFilesInProtonDirectory();
             } else {
                 firstRecordsFileCount = countRecords.get(0).getNumberOfFilesInRpvXmlDirectory();
             }
 
             for (CountRecord countRecord : countRecords) {
                 if ((fileType == FileType.PROTON &&
                         countRecord.getNumberOfFilesInProtonDirectory() != firstRecordsFileCount) ||
                         (fileType == FileType.RPV_XML &&
                                 countRecord.getNumberOfFilesInRpvXmlDirectory() != firstRecordsFileCount)) {
                     filesAreStatic = false;
                 }
             }
 
             if (firstRecordsFileCount == 0) { // ignore if the file count is 0
                 filesAreStatic = false;
             }
         } else {
             LOGGER.warn("There are not enough records to check (only {} records in the file)", countRecords.size());
 
             return false;
         }
 
         LOGGER.debug("{} files are static: {}", fileType, filesAreStatic);
 
         return filesAreStatic;
     }
 
     /**
      * Checks if the number of pending files for importer exceeds a given limit
      *
      * @param pendingFileLimit max number of files that's allowed
      */
     private static boolean doesNumberOfPendingFilesExceedLimit(FileType fileType, List<CountRecord> countRecords,
                                                                int pendingFileLimit) {
         if (countRecords.size() > 0) {
             List<CountRecord> countRecordsToTest = new ArrayList<CountRecord>(countRecords);
 
             Collections.sort(countRecordsToTest, CountRecord.Order.ByRecordTime.descending());
 
             CountRecord latestRecord = countRecordsToTest.get(0);
 
             LOGGER.debug("Proton files exceed limit: {}",
                     (latestRecord.getNumberOfFilesInProtonDirectory() > pendingFileLimit));
             LOGGER.debug("RpvXml files exceed limit: " +
                     (latestRecord.getNumberOfFilesInRpvXmlDirectory() > pendingFileLimit));
 
             if (latestRecord.getNumberOfFilesInProtonDirectory() > pendingFileLimit ||
                     latestRecord.getNumberOfFilesInRpvXmlDirectory() > pendingFileLimit) {
                 return true;
             }
         }
 
         return false;
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
                     numberOfLinesRead++;
 
                     if (numberOfLinesRead == numberOfLinesToReturn) {
                         break;
                     }
                     /**
                      * add line to line list
                      */
                     String currentLine = sb.reverse().toString();
                     sb = new StringBuilder();
 
                     if (StringUtils.hasLength(currentLine)) {
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
      * Convert lines on the file into sensible objects
      */
     private static List<CountRecord> getCountRecordsFromLines(List<String> lines) {
         List<CountRecord> countRecords = new ArrayList<CountRecord>();
 
         for (String line : lines) {
             String date = extractDateAsString(line);
             String numberOfFilesInProtonDirectory = extractNumberOfFilesInProtonDirectoryAsString(line);
             String numberOfFilesInRpvXmlDirectory = extractNumberOfFilesInRpvXmlDirectoryAsString(line);
 
             if (isValidCountRecord(date, RECORD_DATE_FORMAT, numberOfFilesInProtonDirectory,
                     numberOfFilesInRpvXmlDirectory)) {
 
                 countRecords.add(CountRecord.fromDateProtonAndRpvXmlCounts(date, RECORD_DATE_FORMAT,
                         Integer.parseInt(numberOfFilesInProtonDirectory),
                         Integer.parseInt(numberOfFilesInRpvXmlDirectory)));
             } else {
                 if (!StringUtils.hasLength(line)) {
                     LOGGER.info("Empty record is encountered, this might be the first line");
                 } else {
                     LOGGER.error("Invalid record: {}", line);
                 }
             }
         }
 
         return countRecords;
     }
 
     /**
      * Check if the parameters are in correct formats
      */
     private static boolean isValidCountRecord(String dateString, String dateFormant,
                                               String numberOfFilesInProtonDirectory,
                                               String numberOfFilesInRpvXmlDirectory) {
         return isInteger(numberOfFilesInProtonDirectory) && isInteger(numberOfFilesInRpvXmlDirectory) &&
                 parseDate(dateString, dateFormant) != null;
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
 
     public static boolean isInteger(String s) {
         try {
             Integer.parseInt(s);
         } catch (NumberFormatException e) {
             return false;
         }
 
         return true;
     }
 
 
     private static String extractDateAsString(String line) {
         return extractRecordDataOnThisPosition(line, DATE_POSITION_IN_RECORD);
     }
 
     private static String extractNumberOfFilesInProtonDirectoryAsString(String line) {
         return extractRecordDataOnThisPosition(line,
                 NUMBER_OF_FILES_IN_PROTON_DIRECTORY_INFORMATION_POSITION_IN_RECORD);
     }
 
     private static String extractNumberOfFilesInRpvXmlDirectoryAsString(String line) {
         return extractRecordDataOnThisPosition(line,
                 NUMBER_OF_FILES_IN_RPV_XML_DIRECTORY_INFORMATION_POSITION_IN_RECORD);
     }
 
     private static String extractRecordDataOnThisPosition(String line, int position) {
         String[] recordDataSections = line.split(RECORD_DATA_DELIMITER);
 
         if (recordDataSections != null && recordDataSections.length > position) {
             return recordDataSections[position];
         } else {
             return null;
         }
     }
 
     private static class CountRecord {
         private Date recordTime;
         private int numberOfFilesInProtonDirectory;
         private int numberOfFilesInRpvXmlDirectory;
 
         private CountRecord() {
 
         }
 
         public static CountRecord fromDateProtonAndRpvXmlCounts(String dateString, String dateFormat,
                                                                 int numberOfFilesInProtonDirectory,
                                                                 int numberOfFilesInRpvXmlDirectory) {
             CountRecord countRecord = new CountRecord();
             countRecord.setRecordTime(parseDate(dateString, dateFormat));
             countRecord.setNumberOfFilesInProtonDirectory(numberOfFilesInProtonDirectory);
             countRecord.setNumberOfFilesInRpvXmlDirectory(numberOfFilesInRpvXmlDirectory);
 
             return countRecord;
         }
 
         public static enum Order implements Comparator<CountRecord> {
             ByRecordTime() {
                 public int compare(CountRecord leftRecord, CountRecord rightRecord) {
                     return leftRecord.getRecordTime().compareTo(rightRecord.getRecordTime());
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
 
         public int getNumberOfFilesInProtonDirectory() {
             return numberOfFilesInProtonDirectory;
         }
 
         public void setNumberOfFilesInProtonDirectory(int numberOfFilesInProtonDirectory) {
             this.numberOfFilesInProtonDirectory = numberOfFilesInProtonDirectory;
         }
 
         public int getNumberOfFilesInRpvXmlDirectory() {
             return numberOfFilesInRpvXmlDirectory;
         }
 
         public void setNumberOfFilesInRpvXmlDirectory(int numberOfFilesInRpvXmlDirectory) {
             this.numberOfFilesInRpvXmlDirectory = numberOfFilesInRpvXmlDirectory;
         }
 
         public String toString() {
             return "Date: " + recordTime + ", " +
                     "Proton directory file count: " + numberOfFilesInProtonDirectory + ", " +
                     "Rpv Xml directory file count: " + numberOfFilesInRpvXmlDirectory;
         }
     }
 }
