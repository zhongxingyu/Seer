 package org.bitrepository.integrityservice.reports;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class to handle writing (streaming) report parts, and generation (writing) of the final report 
  */
 public class IntegrityReportWriter {
     private static final Logger log = LoggerFactory.getLogger(IntegrityReportWriter.class);
 
     private static final String DELETED_FILE = "deletedFile";
     private static final String CHECKSUM_ISSUE_PREFIX = "checksumIssue-";
     private static final String MISSING_CHECKSUM_PREFIX = "missingChecksum-";
     private static final String OBSOLETE_CHECKSUM_PREFIX = "obsoleteChecksum-";
     private static final String MISSING_FILE_PREFIX = "missingFile-";
     private static final String REPORT_FILE = "report";
     
     private static final String SECTION_HEADER_START_STOP = "========";
     private static final String PILLAR_HEADER_START_STOP = "--------";
     
     private final Map<String, BufferedWriter> missingFiles = new TreeMap<String, BufferedWriter>();
     private final Map<String, BufferedWriter> checksumIssues = new TreeMap<String, BufferedWriter>();
     private final Map<String, BufferedWriter> missingChecksums = new TreeMap<String, BufferedWriter>();
     private final Map<String, BufferedWriter> obsoleteChecksums = new TreeMap<String, BufferedWriter>();
     private BufferedWriter deletedFilesWriter;
     private BufferedWriter reportFileWriter;
     private final File reportDir;
     
     public IntegrityReportWriter(File reportDir) {
         this.reportDir = reportDir;
     }
     
     public String getReportFilePath() {
         File reportFile = new File(reportDir, REPORT_FILE);
         log.debug("getReportFilePath: Report file located at: " + reportFile.getAbsolutePath());
         return reportFile.getAbsolutePath();
     }
     
     /**
      * Method to handle writing of a deleted file entry to on-disk storage 
      * @param fileID The ID of the file to be added to the report as deleted from system.
      */
     public void writeDeletedFile(String fileID) throws IOException {
         if(deletedFilesWriter == null) {
             File deletedFilesFile = new File(reportDir, DELETED_FILE);
             if(deletedFilesFile.exists()) {
                 deletedFilesFile.delete();
             }
             deletedFilesWriter = new BufferedWriter(new FileWriter(deletedFilesFile, true));
         }
         deletedFilesWriter.append(fileID);
         deletedFilesWriter.newLine();
         deletedFilesWriter.flush();
     }
     
     /**
      * Method to handle writing of a checksum issue for a file on a given pillar
      * @param pillarID The ID of the pillar with a checksum issue
      * @param fileID The ID of the file which have a checksum issue 
      */
     public void writeChecksumIssue(String pillarID, String fileID) throws IOException {
         BufferedWriter checksumIssueWriter;
         String key = CHECKSUM_ISSUE_PREFIX + pillarID;
         if(!checksumIssues.containsKey(key)) {
             File checksumIssueFile = new File(reportDir, key);
             if(checksumIssueFile.exists()) {
                 checksumIssueFile.delete();
             }
             checksumIssueWriter = new BufferedWriter(new FileWriter(checksumIssueFile, true));
             checksumIssues.put(key, checksumIssueWriter);
         } else {
             checksumIssueWriter = checksumIssues.get(key); 
         }
         checksumIssueWriter.append(fileID);
         checksumIssueWriter.newLine();
         checksumIssueWriter.flush();
         
     }
     
     /**
      * Method to handle writing of a missing file entry on a given pillar
      * @param pillarID The ID of the pillar where the file is missing
      * @param fileID The ID of the missing file
      */
     public void writeMissingFile(String pillarID, String fileID) throws IOException {
         BufferedWriter missingFileWriter;
         String key = MISSING_FILE_PREFIX + pillarID;
         if(!missingFiles.containsKey(key)) {
             File missingFileFile = new File(reportDir, key);
             if(missingFileFile.exists()) {
                 missingFileFile.delete();
             }
             missingFileWriter = new BufferedWriter(new FileWriter(missingFileFile, true));
             missingFiles.put(key, missingFileWriter);
         } else {
             missingFileWriter = missingFiles.get(key);
         }
         missingFileWriter.append(fileID);
         missingFileWriter.newLine();
         missingFileWriter.flush();
     }
     
     /**
      * Method to handle writing of a obsolete checksum entry for a given pillar
      * @param pillarID The ID of the pillar with the obsolete checksum
      * @param fileID The ID of the file which have an obsolete checksum 
      */
     public void writeObsoleteChecksum(String pillarID, String fileID) throws IOException {
         BufferedWriter obsoleteChecksumWriter;
         String key = OBSOLETE_CHECKSUM_PREFIX + pillarID;
         if(!obsoleteChecksums.containsKey(key)) {
             File obsoleteChecksumFile = new File(reportDir, key);
             if(obsoleteChecksumFile.exists()) {
                 obsoleteChecksumFile.delete();
             }
             obsoleteChecksumWriter = new BufferedWriter(new FileWriter(obsoleteChecksumFile, true));
             obsoleteChecksums.put(key, obsoleteChecksumWriter);
         } else {
             obsoleteChecksumWriter = obsoleteChecksums.get(key);
         }
         obsoleteChecksumWriter.append(fileID);
         obsoleteChecksumWriter.newLine();
         obsoleteChecksumWriter.flush();
     }
     
     /**
      * Method to handle writing of a missing checksum entry for a given pillar
      * @param pillarID The ID of the pillar with the missing checksum
      * @param fileID The ID of the file missing a checksum 
      */
     public void writeMissingChecksum(String pillarID, String fileID) throws IOException {
         BufferedWriter missingChecksumWriter;
         String key = MISSING_CHECKSUM_PREFIX + pillarID;
         if(!missingChecksums.containsKey(key)) {
             File missingChecksumFile = new File(reportDir, key);
             if(missingChecksumFile.exists()) {
                 missingChecksumFile.delete();
             }
             missingChecksumWriter = new BufferedWriter(new FileWriter(missingChecksumFile, true));
             missingChecksums.put(key, missingChecksumWriter);
         } else {
             missingChecksumWriter = missingChecksums.get(key);
         }
         missingChecksumWriter.append(fileID);
         missingChecksumWriter.newLine();
         missingChecksumWriter.flush();
     }
     
     /**
      * Method to write the full report. If a report already exists, the old file will be deleted 
      * and a fresh one generated.   
      */
     public void writeReport() throws IOException {
         flushAll();
         if(reportFileWriter == null) {
             File reportFile = new File(reportDir, REPORT_FILE);
             if(reportFile.exists()) {
                 reportFile.delete();
             }
             reportFileWriter = new BufferedWriter(new FileWriter(reportFile, true));
         }
         
        if(deletedFilesWriter != null) {
             writeSectionHeader(reportFileWriter, "Deleted files");
             writeSectionPart(reportFileWriter, new File(reportDir, DELETED_FILE));
         }
         
         if(!missingFiles.isEmpty()) {
             writeReportSection(reportFileWriter, missingFiles.keySet(), "Missing files");
         }
         
         if(!checksumIssues.isEmpty()) {
             writeReportSection(reportFileWriter, checksumIssues.keySet(), "Checksum issues");
         }
         
         if(!missingChecksums.isEmpty()) {
             writeReportSection(reportFileWriter, missingChecksums.keySet(), "Missing checksums");
         }
         
         if(!obsoleteChecksums.isEmpty()) {
             writeReportSection(reportFileWriter, obsoleteChecksums.keySet(), "Obsolete checksums");
         }
         
         reportFileWriter.flush();
         
     }
     
     /**
      * Flushes all open files 
      */
     private void flushAll() throws IOException {
         if(deletedFilesWriter != null) {
             deletedFilesWriter.flush();
         }
         
         for(BufferedWriter writer : missingFiles.values()) {
             writer.flush();
         }
         
         for(BufferedWriter writer : checksumIssues.values()) {
             writer.flush();
         }
         
         for(BufferedWriter writer : missingChecksums.values()) {
             writer.flush();
         }
         
         for(BufferedWriter writer : obsoleteChecksums.values()) {
             writer.flush();
         }
     }
     
     /**
      * Method to close all open writers/streams
      * Only call close after finished using object.  
      */
     public void close() throws IOException {
         if(reportFileWriter != null) {
             reportFileWriter.close();
         }
         
         if(deletedFilesWriter != null) {
             deletedFilesWriter.close();
         }
         
         for(BufferedWriter writer : missingFiles.values()) {
             writer.close();
         }
         
         for(BufferedWriter writer : checksumIssues.values()) {
             writer.close();
         }
         
         for(BufferedWriter writer : missingChecksums.values()) {
             writer.close();
         }
         
         for(BufferedWriter writer : obsoleteChecksums.values()) {
             writer.close();
         }
     }
     
     private void writeSectionHeader(BufferedWriter report, String sectionName) throws IOException {
         report.append(SECTION_HEADER_START_STOP + " " + sectionName + " " + SECTION_HEADER_START_STOP);
         report.newLine();
     }
     
     private void writePillarHeader(BufferedWriter report, String pillarName) throws IOException {
         report.append(PILLAR_HEADER_START_STOP + " " + pillarName + " " + PILLAR_HEADER_START_STOP);
         report.newLine();
     }
     
     private void writeReportSection(BufferedWriter report, Set<String> section, String sectionName) throws IOException {
        log.debug("Writing section: " + sectionName);
         writeSectionHeader(report, sectionName);
         for(String part : section) {
             String pillarName = part.split("-")[1];
             File partFile = new File(reportDir, part);
            log.debug("Writing part for pillar: " + pillarName);
             writePillarHeader(report, pillarName);
             writeSectionPart(report, partFile);
         }
     }
     
     private void writeSectionPart(BufferedWriter report, File partData) throws FileNotFoundException {
         BufferedReader br = new BufferedReader(new FileReader(partData));
         String fileID;
         try {
             while ((fileID = br.readLine()) != null) {
                 report.append(fileID);
                 report.newLine();
             }
         } catch (IOException e) {
            log.error(e.getMessage(), e);
         } finally {
             try {
                 br.close();
             } catch (IOException e) {
                log.error(e.getMessage(), e);
             }
         }
     }
 }
