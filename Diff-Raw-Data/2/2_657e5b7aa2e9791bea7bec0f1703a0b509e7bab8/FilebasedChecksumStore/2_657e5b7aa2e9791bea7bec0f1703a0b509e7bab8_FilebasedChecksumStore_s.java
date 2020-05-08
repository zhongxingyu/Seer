 /*
  * #%L
  * Bitrepository Reference Pillar
  * %%
  * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.pillar.checksumpillar.cache;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bitrepository.bitrepositoryelements.FileIDs;
 import org.bitrepository.common.ArgumentValidator;
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.common.utils.FileUtils;
 import static org.bitrepository.pillar.checksumpillar.cache.ChecksumEntry.CHECKSUM_SEPARATOR;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A checksum archive in the form of a file (as alternative to a database).<br>
  * 
  * Each entry in the file is on its own line, thus the number of lines is the 
  * number of entries.<br>
  * The entries on a line is in the format of a ChecksumJob: <br>
  * <b>'filename' + ## + 'checksum'</b> <br>
  * The lines are not sorted.
  * 
  * If no file exists when the class is instantiated then it will be created,
  * and if an 'admin.data' file exists, then it will be loaded and put into the
  * archive file.
  */
 public class FilebasedChecksumStore implements ChecksumStore {
     /** The prefix to the filename. */
     private static final String FILENAME_PREFIX = "checksum_";
     /** The suffix to the filename. */
     private static final String FILENAME_SUFFIX = ".checksum";
     /** The suffix of the filename of the recreation file.*/
     private static final String RECREATE_PREFIX = "recreate_";
     /** The suffix of the filename of the recreation file.*/
     private static final String RECREATE_SUFFIX = ".checksum";
     /** The prefix to the removedEntryFile. */
     private static final String WRONG_FILENAME_PREFIX = "removed_";
     /** The suffix to the removedEntryFile. */
     private static final String WRONG_FILENAME_SUFFIX = ".checksum";
     
     /**
      * The logger used by this class.
      */
     private Logger log = LoggerFactory.getLogger(getClass());
     
     /**
      * The file to store the checksum.
      * Each line should contain the following:
      * arc-filename + ## + checksum.
      */
     private File checksumFile;
     
     /**
      * The file for storing all the deleted entries.
      * Each entry should be: 'date :' + 'wrongEntry'.
      */
     private File wrongEntryFile;
     
     /**
      * The last modified date for the checksum file. This variable is used
      * for determining whether to reload the archive from the checksum file, 
      * when they are synchronized.
      * This has to be updated whenever the checksum file is changed.
      */
     private long lastModifiedChecksumFile;
     
     /**
      * This map consists of the archive loaded into the memory. It is faster to 
      * use a memory archive than the the checksum file, though all entries must
      * exist both in the file and the memory.
      * 
      * Map(file -> ChecksumEntry).
      */
     private Map<String, ChecksumEntry> checksumArchive = Collections.synchronizedMap(
             new HashMap<String, ChecksumEntry>());
     
     /**
      * The settings.
      */
     private final Settings settings;
     
     /**
      * The minimum space left.
      */
     private long minSpaceLeft;
     
     /**
      * Constructor.
      * Retrieves the minimum space left variable, and ensures the existence of
      * the archive file. If the file does not exist, then it is created.
      */
     public FilebasedChecksumStore(Settings settings) {
         super();
         this.settings = settings;
         
         // Get the minimum space left setting.
         minSpaceLeft = settings.getReferenceSettings().getPillarSettings().getMinimumSizeLeft();
         // make sure, that minSpaceLeft is non-negative.
         ArgumentValidator.checkPositive(minSpaceLeft, "Minimum space left may not be negative.");
 
         // Initialize the archive and bad-entry files.
         initializeFiles();
     }
     
     /**
      * Method for retrieving the name of the checksum file.
      * 
      * @return The checksum file name.
      */
     public String getFileName() {
         return checksumFile.getPath();
     }
     
     /**
      * Method for retrieving the name of the wrongEntryFile.
      * 
      * @return The wrong entry file name.
      */
     public String getWrongEntryFilename() {
         return wrongEntryFile.getPath();
     }
 
     /**
      * Method for testing where there is enough space left on local drive.
      * 
      * @return Whether there is enough space left.
      */
     public boolean hasEnoughSpace() {
         // The file must be valid and have enough space.
         if (checkArchiveFile(checksumFile)
                 && (getBytesFree() > minSpaceLeft)) {
             return true;
         }
         return false;
     }
     
     /**
      * Method for testing whether there is enough left on the local drive
      * for recreating the checksum file.
      *  
      * @return False only if there is not enough space left.
      */
     private boolean hasEnoughSpaceForRecreate() {
         // check if the checksum file is larger than space left and the minimum
         // space left.
         if(checksumFile.length() + minSpaceLeft 
                 > getBytesFree()) {
             return false;
         }
         
         return true;
     }
     
     /**
      * Method for initializing the files.
      * Starts by initializing the removedEntryFile before initializing the 
      * checksumFile.
      * If the checksum file already exists, then it is loaded into memory.
      */
     private void initializeFiles() {
         // Extract the dir-name and create the dir (if it does not yet exist).
         File checksumDir = FileUtils.retrieveDirectory(
                 settings.getReferenceSettings().getPillarSettings().getFileDir());
         
         // Get the name and initialise the wrong entry file.
         wrongEntryFile = new File(checksumDir, makeWrongEntryFileName());
         
         // ensure that the file exists.
         if(!wrongEntryFile.exists()) {
             try {
                 wrongEntryFile.createNewFile();
             } catch (IOException e) {
                 String msg = "Cannot create 'wrongEntryFile'!";
                 log.error(msg);
                 throw new IllegalStateException(msg, e);
             }
         }
 
         // get the name of the file and initialise it.
         checksumFile = new File(checksumDir, makeChecksumFileName());
         
         // Create file is checksumFile does not exist.
         if(!checksumFile.exists()) {
             try {
                 checksumFile.createNewFile();
                 lastModifiedChecksumFile = checksumFile.lastModified();
             } catch (IOException e) {
                 String msg = "Cannot create checksum archive file!";
                 log.error(msg);
                 throw new IllegalStateException(msg, e);
             }
         } else {
             // If the archive file already exists, then it must consist of the archive for this replica. It must 
             // therefore be loaded into the memory.
             loadFile();
         }
     }
     
     /**
      * Loads an existing checksum archive file into the memory.
      * This will go through every line, and if the line is valid, then it is loaded into the checksumArchive map in 
      * the memory. If the line is invalid then a warning is issued and the line is put into the wrongEntryFile.
      * 
      * If a bad entry is found, then the archive file has to be recreated afterwards, since the bad entry otherwise 
      * still would be in the archive file.
      */
     private void loadFile() {
         // Checks whether a bad entry was found, to decide whether the archive file should be recreated.
         boolean recreate = false;
         
         
         BufferedReader reader = null;
         try {
             reader = new BufferedReader(new FileReader(checksumFile));
             
             String line;
             while((line = reader.readLine()) != null) {
                 ChecksumEntry cs = parseLine(line);
                 
                 if(cs == null) {
                     appendWrongRecordToWrongEntryFile(line);
                     recreate = true;
                 } else {
                     checksumArchive.put(cs.getFileId(), cs);
                 }
             }
         } catch (IOException e) {
             throw new IllegalStateException("Cannot load the current checksum file.", e);
         } finally {
             if(reader != null) {
                 try {
                     reader.close();
                 } catch (IOException e) {
                     throw new IllegalStateException("Cannot close the reading of the checksum archive file.", e);
                 }
             }
         }
 
         // If a bad entry is found, then the archive file should be recreated.
         // Otherwise the bad entries might still be in the archive file next time the FileChecksumArchive is 
         // initialized/restarted.
         if(recreate) {
             recreateArchiveFile();
         }
         
         // retrieve the 'last modified' from the checksum file.
         lastModifiedChecksumFile = checksumFile.lastModified();
     }
     
     /**
      * Recreates the archive file from the memory.
      * Makes a new file which contains the entire archive, and then move the new archive file on top of the old one.
      * This is used when to recreate the archive file, when an record has been removed.
      */
     private void recreateArchiveFile() {
         try {
             // Handle the case, when there is not enough space left for 
             // recreating the 
             if(!hasEnoughSpaceForRecreate()) {
                 String errMsg = "Not enough space left to recreate the checksum file.";
                 log.error(errMsg);
                 throw new IllegalStateException(errMsg);
             }
 
             // This should be synchronized, so no new entries can be made
             // while recreating the archive file.
             synchronized(checksumFile) {
                 // initialize and create the file.
                 File recreateFile = new File(checksumFile.getParentFile(), makeRecreateFileName());
                 if(!recreateFile.createNewFile()) {
                     log.warn("Cannot create new file. The recreate checksum file did already exist.");
                 }
 
                 // put the archive into the file.
                 FileWriter fw = new FileWriter(recreateFile);
                 try {
                     for(ChecksumEntry cs : checksumArchive.values()) {
                         String record = cs.getFileId() + CHECKSUM_SEPARATOR + cs.getChecksum();
                         fw.append(record + "\n");
                     }
                 } finally {
                     fw.flush();
                     fw.close();
                 }
                 
                 recreateFile.renameTo(checksumFile);
             }
         } catch (IOException e) {
             String errMsg = "The checksum file has not been recreated as attempted. The archive in memory and the one "
                     + "on file are no longer identical.";
             log.error(errMsg, e);
             throw new IllegalStateException(errMsg, e);
         }
     }
     
     /**
      * Creates the string for the name of the checksum file.
      * E.g. checksum_PILLAR.md5.
      * 
      * @return The name of the file.
      */
     private String makeChecksumFileName() {
         return FILENAME_PREFIX + settings.getReferenceSettings().getPillarSettings().getPillarID() + FILENAME_SUFFIX;
     }
     
     /**
      * Creates the string for the name of the recreate file.
      * E.g. recreate_PILLAR.checksum.
      * 
      * @return The name of the file for recreating the checksum file.
      */
     private String makeRecreateFileName() {
         return RECREATE_PREFIX + settings.getReferenceSettings().getPillarSettings().getPillarID() + RECREATE_SUFFIX;
     }
     
     /**
      * Creates the string for the name of the wrongEntryFile.
      * E.g. removed_PILLAR.checksum
      * 
      * @return The name of the wrongEntryFile.
      */
     private String makeWrongEntryFileName() {
         return WRONG_FILENAME_PREFIX + settings.getReferenceSettings().getPillarSettings().getPillarID() 
                 + WRONG_FILENAME_SUFFIX;
     }
     
     /**
      * Method for validating a file for use as checksum file.
      * This basically checks whether the file exists, 
      * whether it is a directory instead of a file, 
      * and whether it is writable. 
      * 
      * It has to exist and be writable, but it may not be a directory.
      * 
      * @param file The file to validate.
      * @return Whether the file is valid.
      */
     private boolean checkArchiveFile(File file) {
         // The file must exist.
         if (!file.isFile()) {
             log.warn("The file '" + file.getAbsolutePath() + "' is not a valid file.");
             return false;
         }
         // It must be writable.
         if (!file.canWrite()) {
             log.warn("The file '" + file.getAbsolutePath() 
                     + "' is not writable");
             return false;
         }
         return true;
     }
 
     /**
      * Appending an checksum archive entry to the checksum file.
      * The record string is created and appended to the file.
      *  
      * @param filename The name of the file to add.
      * @param checksum The checksum of the file to add.
      */
     private synchronized void appendEntryToFile(String filename, String checksum) {
         // initialise the record.
         String record = filename + CHECKSUM_SEPARATOR + checksum + "\n";
         
         // get a filewriter for the checksum file, and append the record. 
         boolean appendToFile = true;
         
         // Synchronize to ensure that the file is not overridden during the
         // appending of the new entry.
         synchronized(checksumFile) {
             try {
                 FileWriter fwrite = new FileWriter(checksumFile, appendToFile);
                 try {
                     fwrite.append(record);
                 } finally {
                     // close fileWriter.
                     fwrite.flush();
                     fwrite.close();
                 }
             } catch(IOException e) {
                 throw new IllegalStateException("An error occurred while appending an entry to the archive file.", e);
             }
             
             // The checksum file has been updated and so has its timestamp. 
             // Thus update the last modified date for the checksum file.  
             lastModifiedChecksumFile = checksumFile.lastModified();
         }
     }
 
     /**
      * Method for appending a 'wrong' entry in the wrongEntryFile.
      * It will be written when the wrong entry was appended:
      * date + " : " + wrongRecord.
      * 
      * @param wrongRecord The record to append.
      * @throws IOFailure If the wrong record cannot be appended correctly.
      */
     private synchronized void appendWrongRecordToWrongEntryFile(String wrongRecord) {
         try {
             // Create the string to append: date + 'wrong record'.
             String entry = new Date().toString() + " : " + wrongRecord + "\n";
 
             // get a filewriter for the checksum file, and append the record. 
             boolean appendToFile = true;
             FileWriter fwrite = new FileWriter(wrongEntryFile, appendToFile);
             fwrite.append(entry);
 
             // close fileWriter.
             fwrite.flush();
             fwrite.close();
         } catch (IOException e) {
             String errMsg = "Cannot put a bad record to the 'wrongEntryFile'.";
             log.warn(errMsg, e);
             throw new IllegalStateException(errMsg, e);
         }
     }
     
     @Override
     public void putEntry(String fileId, String checksum) {
         ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
         ArgumentValidator.checkNotNullOrEmpty(checksum, "String checksum");
         synchronizeMemoryWithFile();
         
         if(hasFile(fileId)) {
             if((getChecksum(fileId) != checksum)) {
                 throw new IllegalStateException("The file '" + fileId + "' is trying to be uploaded with another "
                         + "checksum: '" + checksum + "'. Already knows the file with checksum '" + getChecksum(fileId)
                         + "'.");
             } else {
                 log.warn("The file '" + fileId + "' is already known to the cache with the identical checksum '"
                         + checksum + "'.");
             }
         }
         
         checksumArchive.put(fileId, new ChecksumEntry(fileId, checksum));
         appendEntryToFile(fileId, checksum);
     }
     
     @Override
     public boolean hasFile(String fileId) {
         ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
         synchronizeMemoryWithFile();
         
         // Return whether the archive contains an entry with the filename.
         return checksumArchive.containsKey(fileId);
     }
     
     @Override
     public String getChecksum(String fileId) {
         ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
         synchronizeMemoryWithFile();
         
         if(!checksumArchive.containsKey(fileId)) {
             throw new IllegalStateException("No entry for file '" + fileId + "' to delete.");
         }
         
         // Return the checksum of the record.
         return checksumArchive.get(fileId).getChecksum();
     }
     
     @Override
     public Collection<String> getFileIDs(FileIDs fileIds) {
         ArgumentValidator.checkNotNull(fileIds, "FileIDs fileIds");
         synchronizeMemoryWithFile();
         
         if(fileIds.isSetAllFileIDs()) {
             return checksumArchive.keySet();
         }
         
         String fileId = fileIds.getFileID();
         if(hasFile(fileId)) {
             return Arrays.asList(fileId);
         } else {
             return Collections.emptyList();
         }
     }
 
     @Override
     public Map<String, Date> getLastModifiedDate(FileIDs fileIds) {
         ArgumentValidator.checkNotNull(fileIds, "FileIDs fileIds");
         synchronizeMemoryWithFile();
         Collection<String> files = getFileIDs(fileIds);
         
         Date now = new Date();
         Map<String, Date> res = new HashMap<String, Date>();
         for(String file : files) {
             res.put(file, now);
         }
         return res;
     }
     
     @Override
     public void replaceEntry(String fileId, String oldChecksum, String newChecksum) {
         ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
         ArgumentValidator.checkNotNullOrEmpty(oldChecksum, "String oldChecksum");
         ArgumentValidator.checkNotNullOrEmpty(newChecksum, "String newChecksum");
         synchronizeMemoryWithFile();
         
         if(!checksumArchive.containsKey(fileId)) {
             throw new IllegalStateException("No entry for file '" + fileId + "' to delete.");
         }
         
         ChecksumEntry ce = checksumArchive.get(fileId);
        if(ce.getChecksum() != oldChecksum) {
             throw new IllegalStateException("Cannot replace the entry '" + fileId + "', since it does not have the "
                     + "checksum '" + oldChecksum + "'.");
         }
 
         checksumArchive.put(fileId, new ChecksumEntry(fileId, newChecksum));
         recreateArchiveFile();
     }
     
     @Override
     public void deleteEntry(String fileId) {
         ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
         synchronizeMemoryWithFile();
 
         if(!checksumArchive.containsKey(fileId)) {
             throw new IllegalStateException("No entry for file '" + fileId + "' to delete.");
         }
         
         // First add to 'wrongRecord', then remove.
         ChecksumEntry ce = checksumArchive.get(fileId);
         appendWrongRecordToWrongEntryFile(ce.toString());
         checksumArchive.remove(fileId);
         recreateArchiveFile();
     }
 
     /**
      * Ensures that the file and memory archives are identical.
      * 
      * The timestamp of last communication with the file (read/write) will
      * be checked whether it corresponds the 'last modified' date of the file.
      * If they are different, then the memory archive is reloaded from the file.
      */
     private synchronized void synchronizeMemoryWithFile() {
         log.debug("Synchronizing memory archive with file archive.");
         
         // Check if the checksum file has changed since last access.
         if(checksumFile.lastModified() > lastModifiedChecksumFile) {
             log.warn("Archive in memory out of sync with archive in file.");
             
             // The archive is then reloaded by clearing the current memory 
             // archive and loading the file again.
             checksumArchive.clear();
             // The 'last modified' is reset during loading.
             loadFile();
         }
     }
     
     /**
      * @return The number of bytes available on the local file system, where the checksum directory is located.
      */
     private long getBytesFree() {
         return checksumFile.getFreeSpace();
     }
     
     /**
      * Parses a line in the format 'fileid##checksum' into the ChecksumEntry data format.
      * 
      * @param line The line to parse.
      * @return A ChecksumEntry corresponding the given line, or if it is wrongly
      */
     private ChecksumEntry parseLine(String line) {
         String[] parts = line.split(CHECKSUM_SEPARATOR);
         
         if(parts.length < 2) {
             return null;
         } 
         return new ChecksumEntry(parts[0], parts[1]);
     }
 }
