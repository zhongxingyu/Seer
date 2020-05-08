 /*
  * #%L
  * Bitrepository Integrity Client
  * 
  * $Id$
  * $HeadURL$
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
 package org.bitrepository.integrityservice.cache.database;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
 import org.bitrepository.bitrepositoryelements.FileIDsData;
 import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
 import org.bitrepository.common.ArgumentValidator;
 import org.bitrepository.service.database.DBConnector;
 import org.bitrepository.service.database.DatabaseUtils;
 import org.bitrepository.common.utils.Base16Utils;
 import org.bitrepository.common.utils.CalendarUtils;
 import org.bitrepository.integrityservice.cache.FileInfo;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.*;
 
 /**
  * Handles the communication with the integrity database.
  */
 public class IntegrityDAO {
     /** The log.*/
     private Logger log = LoggerFactory.getLogger(getClass());
     /** The connector to the database.*/
     private final DBConnector dbConnector;
     /** The ids of the pillars.*/
     private final List<String> pillarIds;
     
     /** 
      * Constructor.
      * @param dbConnector The connector to the database, where the cache is stored.
      */
     public IntegrityDAO(DBConnector dbConnector, List<String> pillarIds) {
         ArgumentValidator.checkNotNull(dbConnector, "DBConnector dbConnector");
         
         this.dbConnector = dbConnector;
         this.pillarIds = pillarIds;
         initialisePillars();
     }
     
     /**
      * Initialises the ids of all the pillars.
      */
     private void initialisePillars() {
         for(String pillarId : pillarIds) {
             if(retrievePillarGuid(pillarId) == null) {
                 log.trace("Inserting the pillar '" + pillarId + "' into the pillar table.");
                 String sql = "INSERT INTO " + PILLAR_TABLE +" ( " + PILLAR_ID + " ) VALUES ( ? )";
                 DatabaseUtils.executeStatement(dbConnector, sql, pillarId);
             } else {
                 log.trace("Already know the pillar '" + pillarId + "'. Will not recreate the entry in the database.");
             }
         }
     }
     
     /**
      * Inserts the results of a GetFileIDs operation for a given pillar.
      * @param data The results of the GetFileIDs operation.
      * @param pillarId The pillar, where the GetFileIDsOperation has been performed.
      */
     public void updateFileIDs(FileIDsData data, String pillarId) {
         ArgumentValidator.checkNotNull(data, "FileIDsData data");
         ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
         
         log.trace("Updating the file ids '" + data + "' for pillar '" + pillarId + "'");
         
         for(FileIDsDataItem dataItem : data.getFileIDsDataItems().getFileIDsDataItem()) {
             ensureFileIdExists(dataItem.getFileID());
             Date modifyDate = CalendarUtils.convertFromXMLGregorianCalendar(dataItem.getLastModificationTime());
             
             updateFileInfoLastFileUpdateTimestamp(pillarId, dataItem.getFileID(), modifyDate);
         }
     }
     
     /**
      * Handles the result of a GetChecksums operation on a given pillar.
      * @param data The result data from the GetChecksums operation on the given pillar.
      * @param pillarId The id of the pillar, where the GetChecksums operation has been performed.
      */
     public void updateChecksumData(List<ChecksumDataForChecksumSpecTYPE> data, String pillarId) {
         ArgumentValidator.checkNotNull(data, "List<ChecksumDataForChecksumSpecTYPE> data");
         ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
         
         log.trace("Updating the checksum data '" + data + "' for pillar '" + pillarId + "'");
         for(ChecksumDataForChecksumSpecTYPE csData : data) {
             ensureFileIdExists(csData.getFileID());
             updateFileInfoWithChecksum(csData, pillarId);
         }
     }
     
     /**
      * Retrieves all the FileInfo information for a given file id.
      * @param fileId The id of the file.
      * @return The list of information about this file.
      */
     public List<FileInfo> getFileInfosForFile(String fileId) {
         ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
         
         // Define the index in the result set for the different variables to extract.
         final int indexLastFileCheck = 1;
         final int indexChecksum = 2;
         final int indexLastChecksumCheck = 3;
         final int indexPillarGuid = 4;
         final int indexFileState = 5;
         final int indexChecksumState = 6;
         
         Long fileGuid = retrieveFileGuid(fileId);
         
         if(fileGuid == null) {
             log.debug("Trying to retrieve file infos for non-existing file id: '" + fileId + "'.");
             return new ArrayList<FileInfo>();
         }
         
         List<FileInfo> res = new ArrayList<FileInfo>();
         String sql = "SELECT " + FI_LAST_FILE_UPDATE + ", " + FI_CHECKSUM + ", " + FI_LAST_CHECKSUM_UPDATE + ", " 
                 + FI_PILLAR_GUID + ", " + FI_FILE_STATE + ", " + FI_CHECKSUM_STATE + " FROM " + FILE_INFO_TABLE 
                 + " WHERE " + FI_FILE_GUID + " = ?";
         
         try {
             ResultSet dbResult = null;
             PreparedStatement ps = null;
             Connection conn = null;
             try {
                 conn = dbConnector.getConnection();
                 ps = DatabaseUtils.createPreparedStatement(conn, sql, fileGuid);
                 dbResult = ps.executeQuery();
                 
                 while(dbResult.next()) {
                     Date lastFileCheck = dbResult.getTimestamp(indexLastFileCheck);
                     String checksum = dbResult.getString(indexChecksum);
                     Date lastChecksumCheck = dbResult.getTimestamp(indexLastChecksumCheck);
                     long pillarGuid = dbResult.getLong(indexPillarGuid);
                     
                     String pillarId = retrievePillarFromGuid(pillarGuid);
                     
                     FileState fileState = FileState.fromOrdinal(dbResult.getInt(indexFileState));
                     ChecksumState checksumState = ChecksumState.fromOrdinal(dbResult.getInt(indexChecksumState));
                     
                     FileInfo f = new FileInfo(fileId, CalendarUtils.getXmlGregorianCalendar(lastFileCheck), checksum, 
                             CalendarUtils.getXmlGregorianCalendar(lastChecksumCheck), pillarId,
                             fileState, checksumState);
                     res.add(f);
                 }
             } finally {
                 if(dbResult != null) {
                     dbResult.close();
                 }
                 if(ps != null) {
                     ps.close();
                 }
                 if(conn != null) {
                     conn.close();
                 }
             }
         } catch (SQLException e) {
             throw new IllegalStateException("Could not retrieve the FileInfo for '" + fileId + "' with the SQL '"
                     + sql + "'.", e);
         }
         return res;
     }
     
     /**
      * @return The list of all the file ids within the database.
      */
     public List<String> getAllFileIDs() {
         log.trace("Retrieving all file ids.");
         String sql = "SELECT " + FILES_ID + " FROM " + FILES_TABLE;
         return DatabaseUtils.selectStringList(dbConnector, sql, new Object[0]);
     }
     
     /**
      * Retrieves the number of files in the given pillar, which has the file state 'EXISTING'.
      * @param pillarId The id of the pillar.
      * @return The number of files with file state 'EXISTING' for the given pillar.
      */
     public int getNumberOfExistingFilesForAPillar(String pillarId) {
         ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
         log.trace("Retrieving number of existing files at '{}'.", pillarId);
         Long pillarGuid = retrievePillarGuid(pillarId);
         String sql = "SELECT COUNT(*) FROM " + FILE_INFO_TABLE + " WHERE " + FI_PILLAR_GUID + " = ? AND "
                 + FI_FILE_STATE + " = ?";
         return DatabaseUtils.selectIntValue(dbConnector, sql, pillarGuid, FileState.EXISTING.ordinal());
     }
     
     /**
      * Retrieves the number of files in the given pillar, which has the file state 'MISSING'.
      * @param pillarId The id of the pillar.
      * @return The number of files with file state 'MISSING' for the given pillar.
      */
     public int getNumberOfMissingFilesForAPillar(String pillarId) {
         ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
         log.trace("Retrieving number of missing files at '{}'.", pillarId);
         Long pillarGuid = retrievePillarGuid(pillarId);
         String sql = "SELECT COUNT(*) FROM " + FILE_INFO_TABLE + " WHERE " + FI_PILLAR_GUID + " = ? AND "
                 + FI_FILE_STATE + " = ?";
         return DatabaseUtils.selectIntValue(dbConnector, sql, pillarGuid, FileState.MISSING.ordinal());
     }
     
     /**
      * Retrieves the number of files in the given pillar, which has the checksum state 'INCONSISTENT'.
      * @param pillarId The id of the pillar.
      * @return The number of files with checksum state 'INCONSISTENT' for the given pillar.
      */
     public int getNumberOfChecksumErrorsForAPillar(String pillarId) {
         ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
         log.trace("Retrieving files with checksum error at '{}'.", pillarId);
         Long pillarGuid = retrievePillarGuid(pillarId);
         String sql = "SELECT COUNT(*) FROM " + FILE_INFO_TABLE + " WHERE " + FI_PILLAR_GUID + " = ? AND "
                 + FI_CHECKSUM_STATE + " = ?";
         return DatabaseUtils.selectIntValue(dbConnector, sql, pillarGuid, ChecksumState.ERROR.ordinal());
     }
 
     /**
      * Sets a specific file to missing at a given pillar.
      * @param fileId The id of the file, which is missing at the pillar.
      * @param pillarId The id of the pillar which is missing the file.
      */
     public void setFileMissing(String fileId, String pillarId) {
         ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
         ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
         log.debug("Set file-state missing for file '" + fileId + "' at pillar '" + pillarId + "' to be missing.");
         String sqlUpdate = "UPDATE " + FILE_INFO_TABLE + " SET " + FI_FILE_STATE + " = ? , " + FI_CHECKSUM_STATE 
                 + " = ? WHERE " + FI_FILE_GUID + " = (SELECT " + FILES_GUID + " FROM " + FILES_TABLE + " WHERE " 
                 + FILES_ID + " = ? ) and " + FI_PILLAR_GUID + " = (SELECT " + PILLAR_GUID + " FROM " + PILLAR_TABLE 
                 + " WHERE " + PILLAR_ID + " = ?)";
         DatabaseUtils.executeStatement(dbConnector, sqlUpdate, FileState.MISSING.ordinal(), 
                 ChecksumState.UNKNOWN.ordinal(), fileId, pillarId);
     }
     
     /**
      * Sets a specific file have checksum errors at a given pillar.
      * @param fileId The id of the file, which has checksum error at the pillar.
      * @param pillarId The id of the pillar which has checksum error on the file.
      */
     public void setChecksumError(String fileId, String pillarId) {
         ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
         ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
         log.debug("Sets invalid checksum for file '" + fileId + "' at pillar '" + pillarId + "'");
         String sqlUpdate = "UPDATE " + FILE_INFO_TABLE + " SET " + FI_FILE_STATE + " = ? , " + FI_CHECKSUM_STATE 
                 + " = ? WHERE " + FI_FILE_GUID + " = (SELECT " + FILES_GUID + " FROM " + FILES_TABLE + " WHERE "
                 + FILES_ID + " = ? ) and " + FI_PILLAR_GUID + " = (SELECT " + PILLAR_GUID + " FROM " + PILLAR_TABLE
                 + " WHERE " + PILLAR_ID + " = ?)";
         DatabaseUtils.executeStatement(dbConnector, sqlUpdate, FileState.EXISTING.ordinal(), 
                 ChecksumState.ERROR.ordinal(), fileId, pillarId);
     }
 
     /**
      * Sets a specific file have checksum errors at a given pillar.
      * Unless the given file is missing at the given pillar.
      * @param fileId The id of the file, which has checksum error at the pillar.
      * @param pillarId The id of the pillar which has checksum error on the file.
      */
     public void setChecksumValid(String fileId, String pillarId) {
         ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
         ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
         log.debug("Sets valid checksum for file '" + fileId + "' for pillar '" + pillarId + "'");
         String sql = "UPDATE " + FILE_INFO_TABLE + " SET " + FI_FILE_STATE + " = ? , " + FI_CHECKSUM_STATE 
                 + " = ? WHERE " + FI_PILLAR_GUID + " = ( SELECT " + PILLAR_GUID + " FROM " + PILLAR_TABLE + " WHERE " 
                 + PILLAR_ID + " = ? ) AND " + FI_FILE_GUID + " = ( SELECT " + FILES_GUID + " FROM " + FILES_TABLE 
                 + " WHERE " + FILES_ID + " = ? ) AND " + FI_FILE_STATE + " != ?";
         DatabaseUtils.executeStatement(dbConnector, sql, FileState.EXISTING.ordinal(), 
                 ChecksumState.VALID.ordinal(), pillarId, fileId, FileState.MISSING.ordinal());
     }
     
     /**
      * Remove the given file id from the file table, and all the entries for this file in the file info table. 
      * @param fileId The id of the file to be removed.
      */
     public void removeFileId(String fileId) {
         ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
         Long guid = retrieveFileGuid(fileId);
         
         if(guid == null) {
             log.warn("The file '" + fileId + "' has already been removed.");
             return;
         }
         
         log.info("Removing the file id '" + fileId + "' from the files table.");
         String removeFileIDSql = "DELETE FROM " + FILES_TABLE + " WHERE " + FILES_ID + " = ?";
         DatabaseUtils.executeStatement(dbConnector, removeFileIDSql, fileId);
         
         log.info("Removing the entries for the file with id '" + fileId + "' from the file info table.");
         String removeFileInfoEntrySql = "DELETE FROM " + FILE_INFO_TABLE + " WHERE " + FI_FILE_GUID + " = ?";
         DatabaseUtils.executeStatement(dbConnector, removeFileInfoEntrySql, guid);
     }
     
     /**
      * Finds the id of the files which at any pillar exists but is missing its checksum state.
      */
     public List<String> findMissingChecksums() {
         long startTime = System.currentTimeMillis();
         log.trace("Locating files which are missing the checksum at any pillar.");
         String requestSql = "SELECT " + FILES_TABLE + "." + FILES_ID + " FROM " + FILES_TABLE + " JOIN " 
                 + FILE_INFO_TABLE + " ON " + FILES_TABLE + "." + FILES_GUID + "=" + FILE_INFO_TABLE + "." 
                 + FI_FILE_GUID + " WHERE " + FILE_INFO_TABLE + "." + FI_FILE_STATE + " = ? AND " + FILE_INFO_TABLE 
                 + "." + FI_CHECKSUM_STATE + " = ?";
         List<String> result = DatabaseUtils.selectStringList(dbConnector, requestSql, FileState.EXISTING.ordinal(),
                 ChecksumState.UNKNOWN.ordinal());
         log.debug("Located " + result.size() + " missing checksums in " + (System.currentTimeMillis() - startTime) + "ms");
         return result;
     }
 
     /**
      * Finds the id of the files which have a checksum older than a given date.
      * @param date The date for the checksum to be older than.
      * @return The list of ids for the files which have a checksum older than the given date.
      */
     public List<String> findFilesWithOldChecksum(Date date) {
         long startTime = System.currentTimeMillis();
         log.trace("Locating files which are missing at any pillar.");
         String requestSql = "SELECT " + FILES_TABLE + "." + FILES_ID + " FROM " + FILES_TABLE + " JOIN " 
                 + FILE_INFO_TABLE + " ON " + FILES_TABLE + "." + FILES_GUID + "=" + FILE_INFO_TABLE + "."
                 + FI_FILE_GUID + " WHERE " + FILE_INFO_TABLE + "." + FI_LAST_CHECKSUM_UPDATE + " < ? ";
         List<String> result = DatabaseUtils.selectStringList(dbConnector, requestSql, date);
         log.debug("Located " + result.size() + " obsolete checksums in " + (System.currentTimeMillis() - startTime) +
                 "ms");
         return result;
     }
     
     /**
      * Finds the id of the files which at any pillar does not exist.
      * @return The list of ids for the files which are missing at some pillar.
      */
     public List<String> findMissingFiles() {
         log.trace("Locating files which are missing at any pillar.");
         String requestSql = "SELECT " + FILES_TABLE + "." + FILES_ID + " FROM " + FILES_TABLE + " JOIN " 
                 + FILE_INFO_TABLE + " ON " + FILES_TABLE + "." + FILES_GUID + "=" + FILE_INFO_TABLE + "."
                 + FI_FILE_GUID + " WHERE " + FILE_INFO_TABLE + "." + FI_FILE_STATE + " != ? ";
         return DatabaseUtils.selectStringList(dbConnector, requestSql, FileState.EXISTING.ordinal());
     }
     
     /**
      * Finds the ids of the pillars where the given file is missing.
      * @param fileId The id of the file which is missing.
      * @return The list of ids for the pillars where the file is missing (empty list of the file is not missing).
      */
     public List<String> getMissingAtPillars(String fileId) {
         long startTime = System.currentTimeMillis();
         log.trace("Locating the pillars where a given file is missing.");
         String requestSql = "SELECT " + PILLAR_TABLE + "." + PILLAR_ID + " FROM " + PILLAR_TABLE + " JOIN "
                 + FILE_INFO_TABLE + " ON " + PILLAR_TABLE + "." + PILLAR_GUID + "=" + FILE_INFO_TABLE + "."
                 + FI_PILLAR_GUID + " WHERE " + FILE_INFO_TABLE + "." + FI_FILE_STATE + " != ? AND " + FILE_INFO_TABLE 
                 + "." + FI_FILE_GUID + " = ( SELECT " + FILES_GUID + " FROM " + FILES_TABLE + " WHERE " + FILES_ID 
                 + " = ? )";
         List<String> result = DatabaseUtils.selectStringList(dbConnector, requestSql, FileState.EXISTING.ordinal(),
                 fileId);
         log.debug("Located " + result.size() + " checksums in " + (System.currentTimeMillis() -startTime) + "ms");
         return result;
     }
     
     /**
      * Extracting the file ids for the files with inconsistent checksums. Thus finding the files with checksum errors.
      * It is done in the following way:
      * 1. Find all the unique combinations of checksums and file guids in the FileInfo table. 
      *   Though not for the files which are reported as missing or the checksum is null.
      * 2. Count the number of occurrences for each file guid (thus counting the amount of checksums for each file).
      * 3. Eliminate the file guids which only had one count (thus removing the file guids with consistent checksum).
      * 4. Select the respective file ids for the remaining file guids.
      *   Though only for those, where the creation date for the file is older than the given argument.
      * This is all combined into one single SQL statement for optimisation.
      * 
      * @param maxCreationDate The maximum date for the validation
      * @return The list of file ids for the files with inconsistent checksums.
      */
     public List<String> findFilesWithInconsistentChecksums(Date maxCreationDate) {
         long startTime = System.currentTimeMillis();
         log.trace("Localizing the file ids where the checksums are not consistent.");
         String findUniqueSql = "SELECT " + FI_CHECKSUM + " , " + FI_FILE_GUID + " FROM " + FILE_INFO_TABLE
                 + " WHERE " + FI_FILE_STATE + " != ? AND " + FI_CHECKSUM + " IS NOT NULL GROUP BY " + FI_CHECKSUM 
                 + " , " + FI_FILE_GUID;
         String countSql = "SELECT " + FI_FILE_GUID + " , COUNT(*) as num FROM ( " + findUniqueSql + " ) as unique1 "
                 + " GROUP BY " + FI_FILE_GUID;
         String eliminateSql = "SELECT " + FI_FILE_GUID + " FROM ( " + countSql + " ) as count1 WHERE count1.num > 1";
         String selectSql = "SELECT " + FILES_TABLE + "." + FILES_ID + " FROM " + FILES_TABLE + " JOIN ( " 
                 + eliminateSql + " ) as eliminate1 ON " + FILES_TABLE + "." + FILES_GUID + "=eliminate1." 
                 + FI_FILE_GUID + " WHERE " + FILES_CREATION_DATE + " < ?";
         List<String> res = DatabaseUtils.selectStringList(dbConnector, selectSql, FileState.MISSING.ordinal(),
                 maxCreationDate);
         log.debug("Found " + res.size() + " inconsistencies in " + (System.currentTimeMillis() - startTime) + "ms");
         return res;
     }
     
     /**
      * Updating all the entries for the files with consistent checksums to set their checksum state to valid.
      * It is done in the following way:
      * 1. Find all the unique combinations of checksums and file guids in the FileInfo table. 
      *    Though not for the files which are reported as missing, or if the checksum is null.
      * 2. Count the number of occurrences for each file guid (thus counting the amount of different checksums for 
      *    each file).
      * 3. Eliminate the files with a consistent checksum (select only those with the count of unique checksums of 1)
      * 4. Update the file infos for the respective files by settings their checksum state to valid.
      * This is all combined into one single SQL statement for optimisation.
      */
     public void setFilesWithConsistentChecksumsToValid() {
         long startTime = System.currentTimeMillis();
         log.trace("Localizing the file ids where the checksums are consistent and setting them to the checksum state '"
                 + ChecksumState.VALID + "'.");
         String updateSql = "UPDATE " + FILE_INFO_TABLE + " SET " + FI_CHECKSUM_STATE + " = ? WHERE "
                 + FI_CHECKSUM_STATE + " <> ? AND EXISTS (SELECT 1 FROM " + FILE_INFO_TABLE + " AS inner_fi WHERE "
                 + "inner_fi." + FI_FILE_STATE + " <> ? AND inner_fi." + FI_CHECKSUM + " IS NOT NULL AND inner_fi."
                 + FI_FILE_GUID + " = " + FILE_INFO_TABLE + "." + FI_FILE_GUID + " GROUP BY inner_fi." + FI_FILE_GUID
                + " HAVING COUNT(*) = 1)";
         DatabaseUtils.executeStatement(dbConnector, updateSql, ChecksumState.VALID.ordinal(), 
                 ChecksumState.VALID.ordinal(), FileState.MISSING.ordinal());
         log.debug("Marked consistent files in " + (System.currentTimeMillis() - startTime) + "ms");
     }
     
     /**
      * Updates the file info for the given file at the given pillar.
      * It is always set to 'EXISTING' and if the timestamp is new, then it is also updated along with setting the 
      * checksum state to 'UNKNOWN'.
      * @param pillarId The id for the pillar.
      * @param fileId The id for the file.
      * @param filelistTimestamp The timestamp for when the file was latest modified.
      */
     private void updateFileInfoLastFileUpdateTimestamp(String pillarId, String fileId, Date filelistTimestamp) {
         long startTime = System.currentTimeMillis();
         log.trace("Set Last_File_Update timestamp to '" + filelistTimestamp + "' for file '" + fileId
                 + "' at pillar '" + pillarId + "'.");
         setFileExisting(fileId, pillarId);
         
         // If it is newer than current file_id_timestamp, then update it and set 'checksums' to unknown.
         String updateTimestampSql = "UPDATE " + FILE_INFO_TABLE + " SET " + FI_LAST_FILE_UPDATE + " = ?, " 
                 + FI_CHECKSUM_STATE + " = ? " + " WHERE " + FI_FILE_GUID + " = ( SELECT " + FILES_GUID + " FROM " 
                 + FILES_TABLE + " WHERE " + FILES_ID + " = ? ) and " + FI_PILLAR_GUID + " = ( SELECT " + PILLAR_GUID 
                 + " FROM " + PILLAR_TABLE + " WHERE " + PILLAR_ID + " = ? ) and " + FI_LAST_FILE_UPDATE + " < ?";
         DatabaseUtils.executeStatement(dbConnector, updateTimestampSql, filelistTimestamp, 
                 ChecksumState.UNKNOWN.ordinal(), fileId, pillarId, filelistTimestamp);
         log.debug("Updated fileInfo timestamps in " + (System.currentTimeMillis() - startTime) + "ms");
     }
     
     /**
      * Updates the entry in the file info table for the given pillar and the file with the checksum data, if it has a
      * newer timestamp than the existing entry.
      * In that case the new checksum and its timestamp is inserted, and the checksum state is set to 'UNKNOWN'.
      * @param data The result of the GetChecksums operation.
      * @param pillarId The guid of the pillar.
      */
     private void updateFileInfoWithChecksum(ChecksumDataForChecksumSpecTYPE data, String pillarId) {
         long startTime = System.currentTimeMillis();
         log.trace("Updating pillar '" + pillarId + "' with checksum data '" + data + "'");
         setFileExisting(data.getFileID(), pillarId);
         
         Date csTimestamp = CalendarUtils.convertFromXMLGregorianCalendar(data.getCalculationTimestamp());
         String checksum = Base16Utils.decodeBase16(data.getChecksumValue());
         String updateSql = "UPDATE " + FILE_INFO_TABLE + " SET " + FI_LAST_CHECKSUM_UPDATE + " = ?, "
                 + FI_CHECKSUM_STATE + " = ? , " + FI_CHECKSUM + " = ? WHERE " + FI_FILE_GUID + " = ( SELECT " 
                 + FILES_GUID + " FROM " + FILES_TABLE + " WHERE " + FILES_ID + " = ? ) and " + FI_PILLAR_GUID 
                 + " = ( SELECT " + PILLAR_GUID + " FROM " + PILLAR_TABLE + " WHERE " + PILLAR_ID + " = ? ) and " 
                 + FI_LAST_CHECKSUM_UPDATE + " < ?";
         DatabaseUtils.executeStatement(dbConnector, updateSql, csTimestamp, 
                 ChecksumState.UNKNOWN.ordinal(), checksum, data.getFileID(), pillarId, 
                 csTimestamp);
         log.debug("Updated fileInfo checksums in " + (System.currentTimeMillis() - startTime) + "ms");
     }
     
     /**
      * Sets the file state to 'EXISTING' for a given file at a given pillar.
      * @param fileId The id of the file.
      * @param pillarId The id of the pillar.
      */
     private void setFileExisting(String fileId, String pillarId) {
         log.trace("Marked file " + fileId + " as existing on pillar " + pillarId);
         String updateExistenceSql = "UPDATE " + FILE_INFO_TABLE + " SET " + FI_FILE_STATE + " = ? WHERE " 
                 + FI_FILE_GUID + " = ( SELECT " + FILES_GUID + " FROM " + FILES_TABLE + " WHERE " + FILES_ID
                 + " = ? ) and " + FI_PILLAR_GUID + " = ( SELECT " + PILLAR_GUID + " FROM " + PILLAR_TABLE + " WHERE "
                 + PILLAR_ID + " = ? )";
         DatabaseUtils.executeStatement(dbConnector, updateExistenceSql, FileState.EXISTING.ordinal(),
                 fileId, pillarId);
     }
     
     /**
      * Ensures that the entries for the file with the given id exists (both in the 'files' table and 
      * the 'file_info' table)
      * @param fileId The id of the file to ensure the existence of.
      */
     private void ensureFileIdExists(String fileId) {
         log.trace("Retrieving guid for file '{}'.", fileId);
         String sql = "SELECT " + FILES_GUID + " FROM " + FILES_TABLE + " WHERE " + FILES_ID + " = ?";
         if(DatabaseUtils.selectLongValue(dbConnector, sql, fileId) == null) {
             insertNewFileID(fileId);
         }
     }
     
     /**
      * Retrieves the guid corresponding to a given file id. If no such entry exists, then a null is returned.
      * @param fileId The id of the file to retrieve the guid of.
      * @return The guid of the file with the given id, or null if the guid does not exist.
      */
     private Long retrieveFileGuid(String fileId) {
         log.trace("Retrieving guid for file '{}'.", fileId);
         String sql = "SELECT " + FILES_GUID + " FROM " + FILES_TABLE + " WHERE " + FILES_ID + " = ?";
         return DatabaseUtils.selectLongValue(dbConnector, sql, fileId);
     }
     
     /**
      * Inserts a new file id into the 'files' table in the database.
      * Also creates an entry in the 'fileinfo' table for every pillar.
      * @param fileId The id of the file to insert.
      */
     private void insertNewFileID(String fileId) {
         log.trace("Inserting the file '" + fileId + "' into the files table.");
         String fileSql = "INSERT INTO " + FILES_TABLE + " ( " + FILES_ID + ", " + FILES_CREATION_DATE 
                 + " ) VALUES ( ?, ? )";
         DatabaseUtils.executeStatement(dbConnector, fileSql, fileId, new Date());
         
         Date epoch = new Date(0);
         for(String pillar : pillarIds)  {
             String fileinfoSql = "INSERT INTO " + FILE_INFO_TABLE + " ( " + FI_FILE_GUID + ", " + FI_PILLAR_GUID + ", "
                     + FI_CHECKSUM_STATE + ", " + FI_LAST_CHECKSUM_UPDATE + ", " + FI_FILE_STATE + ", " 
                     + FI_LAST_FILE_UPDATE + " ) VALUES ( (SELECT " + FILES_GUID + " FROM " + FILES_TABLE + " WHERE " 
                     + FILES_ID + " = ? ), (SELECT " + PILLAR_GUID + " FROM " + PILLAR_TABLE + " WHERE " + PILLAR_ID 
                     + " = ? ), ?, ?, ?, ? )";
             DatabaseUtils.executeStatement(dbConnector, fileinfoSql, fileId, pillar, 
                     ChecksumState.UNKNOWN.ordinal(), epoch, FileState.UNKNOWN.ordinal(), epoch);
         }
     }
     
     /**
      * Retrieves the guid corresponding to a given pillar id. All the pillar ids should be created initially.
      * @param pillarId The id of the pillar to retrieve the guid of.
      * @return The guid of the pillar with the given id. 
      * Returns a null if the pillar id is not known by the database yet.
      */
     private Long retrievePillarGuid(String pillarId) {
         log.trace("Retrieving the guid for pillar '{}'.", pillarId);
         String sql = "SELECT " + PILLAR_GUID + " FROM " + PILLAR_TABLE + " WHERE " + PILLAR_ID + " = ?";
         return DatabaseUtils.selectLongValue(dbConnector, sql, pillarId);
     }
     
     /**
      * Retrieves the id of the pillar with the given guid.
      * @param guid The guid of the pillar, whose id should be retrieved.
      * @return The id of the requested pillar.
      */
     private String retrievePillarFromGuid(long guid) {
         log.trace("Retrieving the id of the pillar with the guid '{}'.", guid);
         String sql = "SELECT " + PILLAR_ID + " FROM " + PILLAR_TABLE + " WHERE " + PILLAR_GUID + " = ?";
         return DatabaseUtils.selectStringValue(dbConnector, sql, guid);
     }
 }
