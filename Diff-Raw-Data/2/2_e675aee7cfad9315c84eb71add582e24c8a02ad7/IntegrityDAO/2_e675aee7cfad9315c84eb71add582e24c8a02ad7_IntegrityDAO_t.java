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
 
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.CHECKSUM_ALGORITHM;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.CHECKSUM_GUID;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.CHECKSUM_SALT;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.CHECKSUM_TABLE;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILES_CREATION_DATE;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILES_GUID;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILES_ID;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILES_TABLE;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILE_INFO_TABLE;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_CHECKSUM;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_CHECKSUM_GUID;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_CHECKSUM_STATE;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_FILE_GUID;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_FILE_STATE;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_GUID;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_LAST_CHECKSUM_UPDATE;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_LAST_FILE_UPDATE;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_PILLAR_GUID;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.PILLAR_GUID;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.PILLAR_ID;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.PILLAR_TABLE;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumType;
 import org.bitrepository.bitrepositoryelements.FileIDsData;
 import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
 import org.bitrepository.common.ArgumentValidator;
 import org.bitrepository.common.database.DBConnector;
 import org.bitrepository.common.database.DatabaseUtils;
 import org.bitrepository.common.utils.CalendarUtils;
 import org.bitrepository.integrityservice.cache.FileInfo;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Handles the communication with the integrity database.
  */
 public class IntegrityDAO {
     /** The log.*/
     private Logger log = LoggerFactory.getLogger(getClass());
     /** The connection to the database.*/
     private final DBConnector dbConnector;
     
     /** 
      * Constructor.
      * @param dbConnector The connection to the database, where the cache is stored.
      */
     public IntegrityDAO(DBConnector dbConnector) {
         ArgumentValidator.checkNotNull(dbConnector, "DBConnector dbConnector");
         
         this.dbConnector = dbConnector;
     }
     
     /**
      * Inserts the results of a GetFileIDs operation for a given pillar.
      * @param data The results of the GetFileIDs operation.
      * @param pillarId The pillar, where the GetFileIDsOperation has been performed.
      */
     public void updateFileIDs(FileIDsData data, String pillarId) {
         ArgumentValidator.checkNotNull(data, "FileIDsData data");
         ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
         
         log.info("Updating the file ids '" + data + "' for pillar '" + pillarId + "'");
         long pillarGuid = retrievePillarGuid(pillarId);
         
         for(FileIDsDataItem dataItem : data.getFileIDsDataItems().getFileIDsDataItem()) {
             long fileGuid = retrieveFileGuid(dataItem.getFileID());
             Date modifyDate = CalendarUtils.convertFromXMLGregorianCalendar(dataItem.getLastModificationTime());
             
             updateFileInfoLastFileUpdateTimestamp(pillarGuid, fileGuid, modifyDate);
         }
     }
     
     /**
      * Handles the result of a GetChecksums operation on a given pillar.
      * @param data The result data from the GetChecksums operation on the given pillar.
      * @param checksumType The type of checksum.
      * @param pillarId The id of the pillar, where the GetChecksums operation has been performed.
      */
     public void updateChecksumData(List<ChecksumDataForChecksumSpecTYPE> data, ChecksumSpecTYPE checksumType, 
             String pillarId) {
         ArgumentValidator.checkNotNullOrEmpty(data, "List<ChecksumDataForChecksumSpecTYPE data");
         ArgumentValidator.checkNotNull(checksumType, "ChecksumSpecTYPE checksumType");
         ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
         
         log.info("Updating the checksum data '" + data + "' for pillar '" + pillarId + "'");
         long pillarGuid = retrievePillarGuid(pillarId);
         long checksumGuid = retrieveChecksumSpecGuid(checksumType);
         
         for(ChecksumDataForChecksumSpecTYPE csData : data) {
             updateFileInfoWithChecksum(csData, pillarGuid, checksumGuid);
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
         final int indexChecksumGuid = 3;
         final int indexLastChecksumCheck = 4;
         final int indexPillarGuid = 5;
         final int indexFileState = 6;
         final int indexChecksumState = 7;
         
         long fileGuid = retrieveFileGuid(fileId);
         List<FileInfo> res = new ArrayList<FileInfo>();
         String sql = "SELECT " + FI_LAST_FILE_UPDATE + ", " + FI_CHECKSUM + ", " + FI_CHECKSUM_GUID + ", "
                 + FI_LAST_CHECKSUM_UPDATE + ", " + FI_PILLAR_GUID + ", " + FI_FILE_STATE + ", " + FI_CHECKSUM_STATE 
                 + " FROM " + FILE_INFO_TABLE + " WHERE " + FI_FILE_GUID + " = ?";
         
         try {
             ResultSet dbResult = null;
             try {
                 dbResult = DatabaseUtils.selectObject(dbConnector.getConnection(), sql, fileGuid);
                 
                 while(dbResult.next()) {
                     
                     Date lastFileCheck = dbResult.getDate(indexLastFileCheck);
                     String checksum = dbResult.getString(indexChecksum);
                     long checksumGuid = dbResult.getLong(indexChecksumGuid);
                     Date lastChecksumCheck = dbResult.getDate(indexLastChecksumCheck);
                     long pillarGuid = dbResult.getLong(indexPillarGuid);
                     
                     String pillarId = retrievePillarFromGuid(pillarGuid);
                     ChecksumSpecTYPE checksumType = retrieveChecksumSpecFromGuid(checksumGuid);
                     
                     FileState fileState = FileState.fromOrdinal(dbResult.getInt(indexFileState));
                     ChecksumState checksumState = ChecksumState.fromOrdinal(dbResult.getInt(indexChecksumState));
                     
                     FileInfo f = new FileInfo(fileId, CalendarUtils.getXmlGregorianCalendar(lastFileCheck), checksum, 
                             checksumType, CalendarUtils.getXmlGregorianCalendar(lastChecksumCheck), pillarId,
                             fileState, checksumState);
                     res.add(f);
                 }
             } finally {
                 if(dbResult != null) {
                     dbResult.close();
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
         return DatabaseUtils.selectStringList(dbConnector.getConnection(), sql, new Object[0]);
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
         return DatabaseUtils.selectIntValue(dbConnector.getConnection(), sql, pillarGuid, FileState.EXISTING.ordinal());
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
         return DatabaseUtils.selectIntValue(dbConnector.getConnection(), sql, pillarGuid, FileState.MISSING.ordinal());
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
         return DatabaseUtils.selectIntValue(dbConnector.getConnection(), sql, pillarGuid, ChecksumState.ERROR.ordinal());
     }
 
     /**
      * Sets a specific file to missing at a given pillar.
      * @param fileId The id of the file, which is missing at the pillar.
      * @param pillarId The id of the pillar which is missing the file.
      */
     public void setFileMissing(String fileId, String pillarId) {
         ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
         ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
         log.debug("Set file-state missing for file '" + fileId + "' at pillar '" + pillarId + "'");
         String sqlSelect = "SELECT " + FI_GUID + " FROM " + FILE_INFO_TABLE + " WHERE " + FI_FILE_GUID + " = "
                 + "( SELECT " + FILES_GUID + " FROM " + FILES_TABLE + " WHERE " + FILES_ID + " = ? ) AND " 
                 + FI_PILLAR_GUID + " = ( SELECT " + PILLAR_GUID + " FROM " + PILLAR_TABLE + " WHERE " + PILLAR_ID 
                 + " = ? )";
         Long guid = DatabaseUtils.selectLongValue(dbConnector.getConnection(), sqlSelect, fileId, pillarId);
         
         // If no guid, then the entry does not exist. Thus make a new entry for the file at the pillar, which is set
         // to missing. Otherwise set the current entry to have the file state missing.
         if(guid == null || guid < 1) {
             String insertSql = "INSERT INTO " + FILE_INFO_TABLE + " ( " + FI_PILLAR_GUID + ", " + FI_FILE_GUID + ", "
                     + FI_LAST_FILE_UPDATE + ", " + FI_LAST_CHECKSUM_UPDATE + ", " + FI_FILE_STATE + ", " 
                     + FI_CHECKSUM_STATE + ") VALUES ( ( SELECT " + PILLAR_GUID + " FROM " + PILLAR_TABLE + " WHERE " 
                     + PILLAR_ID + " = ? ), ( SELECT " + FILES_GUID + " FROM " + FILES_TABLE + " WHERE " + FILES_ID 
                     + " = ? ) , ?, ?, ?, ? )";
             DatabaseUtils.executeStatement(dbConnector.getConnection(), insertSql, pillarId, fileId, new Date(0),
                     new Date(0), FileState.MISSING.ordinal(), ChecksumState.UNKNOWN.ordinal());
         } else {
             String sqlUpdate = "UPDATE " + FILE_INFO_TABLE + " SET " + FI_FILE_STATE + " = ? WHERE " + FI_GUID 
                     + " = ? ";
             DatabaseUtils.executeStatement(dbConnector.getConnection(), sqlUpdate, FileState.MISSING.ordinal(), guid);
         }
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
         String sqlSelect = "SELECT " + FI_GUID + " FROM " + FILE_INFO_TABLE + " WHERE " + FI_FILE_GUID + " = "
                 + "( SELECT " + FILES_GUID + " FROM " + FILES_TABLE + " WHERE " + FILES_ID + " = ? ) AND " 
                 + FI_PILLAR_GUID + " = ( SELECT " + PILLAR_GUID + " FROM " + PILLAR_TABLE + " WHERE " + PILLAR_ID 
                 + " = ? )";
         Long guid = DatabaseUtils.selectLongValue(dbConnector.getConnection(), sqlSelect, fileId, pillarId);
         
         // If no guid, then the entry does not exist. Thus make a new entry for the file at the pillar, which is set
         // to checksum error. Otherwise set the current entry as having checksum error.
         if(guid == null || guid < 1) {
             String insertSql = "INSERT INTO " + FILE_INFO_TABLE + " ( " + FI_PILLAR_GUID + ", " + FI_FILE_GUID + ", "
                     + FI_LAST_FILE_UPDATE + ", " + FI_LAST_CHECKSUM_UPDATE + ", " + FI_FILE_STATE + ", " 
                     + FI_CHECKSUM_STATE + ") VALUES ( ( SELECT " + PILLAR_GUID + " FROM " + PILLAR_TABLE + " WHERE " 
                     + PILLAR_ID + " = ? ), ( SELECT " + FILES_GUID + " FROM " + FILES_TABLE + " WHERE " + FILES_ID 
                     + " = ? ) , ?, ?, ?, ? )";
             DatabaseUtils.executeStatement(dbConnector.getConnection(), insertSql, pillarId, fileId, new Date(0),
                     new Date(0), FileState.UNKNOWN.ordinal(), ChecksumState.ERROR.ordinal());
         } else {
             String sqlUpdate = "UPDATE " + FILE_INFO_TABLE + " SET " + FI_CHECKSUM_STATE + " = ? WHERE " + FI_GUID 
                     + " = ? ";
             DatabaseUtils.executeStatement(dbConnector.getConnection(), sqlUpdate, ChecksumState.ERROR.ordinal(), guid);
         }
     }
 
     /**
      * Sets a specific file have checksum errors at a given pillar.
      * If the pillar does not have the given file, then it is ignored by the database.
      * @param fileId The id of the file, which has checksum error at the pillar.
      * @param pillarId The id of the pillar which has checksum error on the file.
      */
     public void setChecksumValid(String fileId, String pillarId) {
         ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
         ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
         log.debug("Sets valid checksum for file '" + fileId + "' for pillar '" + pillarId + "'");
         String sql = "UPDATE " + FILE_INFO_TABLE + " SET " + FI_CHECKSUM_STATE + " = ? WHERE " + FI_PILLAR_GUID 
                 + " = ( SELECT " + PILLAR_GUID + " FROM " + PILLAR_TABLE + " WHERE " 
                     + PILLAR_ID + " = ? ) AND " + FI_FILE_GUID + " = ( SELECT " + FILES_GUID + " FROM " + FILES_TABLE 
                     + " WHERE " + FILES_ID + " = ? )";
         DatabaseUtils.executeStatement(dbConnector.getConnection(), sql, ChecksumState.VALID.ordinal(), pillarId, fileId);
     }
     
     /**
      * Remove the given file id from the file table, and all the entries for this file in the file info table. 
      * @param fileId The id of the file to be removed.
      */
     public void removeFileId(String fileId) {
         ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
         Long guid = retrieveFileGuid(fileId);
         
         log.info("Removing the file id '" + fileId + "' from the files table.");
         String removeFileIDSql = "DELETE FROM " + FILES_TABLE + " WHERE " + FILES_ID + " = ?";
         DatabaseUtils.executeStatement(dbConnector.getConnection(), removeFileIDSql, fileId);
         
         log.info("Removing the entries for the file with id '" + fileId + "' from the file info table.");
         String removeFileInfoEntrySql = "DELETE FROM " + FILE_INFO_TABLE + " WHERE " + FI_FILE_GUID + " = ?";
         DatabaseUtils.executeStatement(dbConnector.getConnection(), removeFileInfoEntrySql, guid);
     }
 
     /**
      * Updates or creates the given timestamp for the latest modified date of the given file on the given pillar.
      * @param pillarGuid The guid for the pillar.
      * @param fileGuid The guid for the file.
      * @param filelistTimestamp The timestamp for when the file was latest modified.
      */
     private void updateFileInfoLastFileUpdateTimestamp(long pillarGuid, long fileGuid, Date filelistTimestamp) {
         log.debug("Set Last_File_Update timestamp to '" + filelistTimestamp + "' for file with guid '" + fileGuid 
                 + "' at pillar with guid'" + pillarGuid + "'.");
         String retrievalSql = "SELECT " + FI_GUID + " FROM " + FILE_INFO_TABLE + " WHERE " + FI_PILLAR_GUID 
                 + " = ? AND " + FI_FILE_GUID + " = ?";
         Long guid = DatabaseUtils.selectLongValue(dbConnector.getConnection(), retrievalSql, pillarGuid, fileGuid);
         
         // if guid is null, then make new entry. Otherwise validate / update.
         if(guid == null) {
             String insertSql = "INSERT INTO " + FILE_INFO_TABLE + " ( " + FI_PILLAR_GUID + ", " + FI_FILE_GUID + ", "
                     + FI_LAST_FILE_UPDATE + ", " + FI_LAST_CHECKSUM_UPDATE + ", " + FI_FILE_STATE + ", " 
                     + FI_CHECKSUM_STATE + ") VALUES ( ?, ?, ?, ?, ?, ? )";
             DatabaseUtils.executeStatement(dbConnector.getConnection(), insertSql, pillarGuid, fileGuid, 
                     filelistTimestamp, new Date(0), FileState.EXISTING.ordinal(), ChecksumState.UNKNOWN.ordinal());
         } else {
             String validateSql = "SELECT " + FI_LAST_FILE_UPDATE + " FROM " + FILE_INFO_TABLE + " WHERE " + FI_GUID 
                     + " = ?";
             Date existingDate = DatabaseUtils.selectDateValue(dbConnector.getConnection(), validateSql, guid);
             
             // Only insert the date, if it is newer than the recorded one. 
             // In that case reject the current checksum state.
             if(existingDate == null || existingDate.getTime() < filelistTimestamp.getTime()) {
                 String updateSql = "UPDATE " + FILE_INFO_TABLE + " SET " + FI_LAST_FILE_UPDATE + " = ?, " 
                        + FI_FILE_STATE + " = ? , " + FI_CHECKSUM_STATE + " = ? WHERE " + FI_GUID + " = ?";
                 DatabaseUtils.executeStatement(dbConnector.getConnection(), updateSql, filelistTimestamp, 
                         FileState.EXISTING.ordinal(), ChecksumState.UNKNOWN.ordinal(), guid);
             } else {
                 log.debug("The existing entry '" + existingDate + "' is not older than the new entry '"
                         + filelistTimestamp + "'.");
             }
         }
     }
     
     /**
      * Updates an entry in the FileInfo table with the results of a GetChecksums operation of a single file.
      * @param data The result of the GetChecksums operation.
      * @param pillarGuid The guid of the pillar.
      * @param checksumGuid The guid of the checksum.
      */
     private void updateFileInfoWithChecksum(ChecksumDataForChecksumSpecTYPE data, long pillarGuid, long checksumGuid) {
         log.debug("Updating pillar with guid '" + pillarGuid + "' with checksum data '" + data + "' for checksum spec "
                 + "with guid '" + checksumGuid + "'");
         Date csTimestamp = CalendarUtils.convertFromXMLGregorianCalendar(data.getCalculationTimestamp());
         String checksum = new String(data.getChecksumValue());
         Long fileGuid = retrieveFileGuid(data.getFileID());
 
         // retrieve the guid if the entry already exists.
         String retrievalSql = "SELECT " + FI_GUID + " FROM " + FILE_INFO_TABLE + " WHERE " + FI_PILLAR_GUID 
                 + " = ? AND " + FI_FILE_GUID + " = ?";
         Long guid = DatabaseUtils.selectLongValue(dbConnector.getConnection(), retrievalSql, pillarGuid, fileGuid);
         
         // if guid is null, then make new entry. Otherwise validate / update.
         if(guid == null) {
             String insertSql = "INSERT INTO " + FILE_INFO_TABLE + " ( " + FI_PILLAR_GUID + ", " + FI_FILE_GUID + ", " 
                     + FI_CHECKSUM_GUID + ", " + FI_LAST_CHECKSUM_UPDATE + ", " + FI_CHECKSUM + ", " + FI_FILE_STATE 
                     + ", " + FI_CHECKSUM_STATE + ") VALUES ( ?, ?, ?, ?, ?, ?, ? )";
             DatabaseUtils.executeStatement(dbConnector.getConnection(), insertSql, pillarGuid, fileGuid, checksumGuid, csTimestamp,
                     checksum, FileState.EXISTING.ordinal(), ChecksumState.UNKNOWN.ordinal());
         } else {
             String validateSql = "SELECT " + FI_LAST_CHECKSUM_UPDATE + " FROM " + FILE_INFO_TABLE + " WHERE " 
                     + FI_GUID + " = ?";
             Date existingDate = DatabaseUtils.selectDateValue(dbConnector.getConnection(), validateSql, guid);
             
             // Only update, if it has a newer checksum timestamp than the recorded one.
             if(existingDate == null || existingDate.getTime() < csTimestamp.getTime()) {
                 String updateSql = "UPDATE " + FILE_INFO_TABLE + " SET " + FI_CHECKSUM_GUID + " = ?, "
                         + FI_LAST_CHECKSUM_UPDATE + " = ?, " + FI_CHECKSUM + " = ?, " + FI_FILE_STATE + " = ?, "
                         + FI_CHECKSUM_STATE + " = ? WHERE " + FI_GUID + " = ?";
                 DatabaseUtils.executeStatement(dbConnector.getConnection(), updateSql, checksumGuid, csTimestamp, checksum, 
                         FileState.EXISTING.ordinal(), ChecksumState.UNKNOWN.ordinal(), guid);
             }
         }
     }
     
     /**
      * Retrieves the guid corresponding to a given file id. If no such entry exists, then it is created.
      * @param fileId The id of the file to retrieve the guid of.
      * @return The guid of the file with the given id.
      */
     private long retrieveFileGuid(String fileId) {
         log.trace("Retrieving guid for file '{}'.", fileId);
         String sql = "SELECT " + FILES_GUID + " FROM " + FILES_TABLE + " WHERE " + FILES_ID + " = ?";
         Long guid = DatabaseUtils.selectLongValue(dbConnector.getConnection(), sql, fileId);
         // If no entry, then make one and extract the guid.
         if(guid == null) {
             insertFileID(fileId);
             guid = DatabaseUtils.selectLongValue(dbConnector.getConnection(), sql, fileId);
         }
         return guid;
     }
     
     /**
      * Inserts a new file id into the 'files' table in the database.
      * @param fileId The id of the file to insert.
      */
     private void insertFileID(String fileId) {
         log.debug("Inserting the file '" + fileId + "' into the files table.");
         String sql = "INSERT INTO " + FILES_TABLE + " ( " + FILES_ID + ", " + FILES_CREATION_DATE 
                 + " ) VALUES ( ?, ? )";
         DatabaseUtils.executeStatement(dbConnector.getConnection(), sql, fileId, new Date());
     }
     
     /**
      * Retrieves the guid corresponding to a given pillar id. If no such entry exists, then it is created.
      * @param pillarId The id of the pillar to retrieve the guid of.
      * @return The guid of the pillar with the given id.
      */
     private long retrievePillarGuid(String pillarId) {
         log.trace("Retrieving the guid for pillar '{}'.", pillarId);
         String sql = "SELECT " + PILLAR_GUID + " FROM " + PILLAR_TABLE + " WHERE " + PILLAR_ID + " = ?";
         Long guid = DatabaseUtils.selectLongValue(dbConnector.getConnection(), sql, pillarId);
         // If no entry, then make one and extract the guid.
         if(guid == null) {
             insertPillarID(pillarId);
             guid = DatabaseUtils.selectLongValue(dbConnector.getConnection(), sql, pillarId);
         }
         return guid;
     }
     
     /**
      * Retrieves the id of the pillar with the given guid.
      * @param guid The guid of the pillar, whose id should be retrieved.
      * @return The id of the requested pillar.
      */
     private String retrievePillarFromGuid(long guid) {
         log.trace("Retrieving the id of the pillar with the guid '{}'.", guid);
         String sql = "SELECT " + PILLAR_ID + " FROM " + PILLAR_TABLE + " WHERE " + PILLAR_GUID + " = ?";
         return DatabaseUtils.selectStringValue(dbConnector.getConnection(), sql, guid);
     }
     
     /**
      * Creates a new entry in the 'pillar' table for the given pillar id.
      * @param pillarId The id of the pillar which are to be inserted into the 'pillar' table.
      */
     private void insertPillarID(String pillarId) {
         log.debug("Inserting the pillar '" + pillarId + "' into the pillar table.");
         String sql = "INSERT INTO " + PILLAR_TABLE +" ( " + PILLAR_ID + " ) VALUES ( ? )";
         DatabaseUtils.executeStatement(dbConnector.getConnection(), sql, pillarId);
     }
     
     /**
      * Retrieves the checksum specification for a given checksum spec guid.
      * @param checksumGuid The guid of the checksum specification to be retrieved.
      * @return The requested checksum specification. Or null if no such entry can be found.
      */
     private ChecksumSpecTYPE retrieveChecksumSpecFromGuid(long checksumGuid) {
         log.trace("Retrieving the checksum specification corresponding to the guid '{}'.", checksumGuid);
         try {
             String sql = "SELECT " + CHECKSUM_ALGORITHM + ", " + CHECKSUM_SALT + " FROM " + CHECKSUM_TABLE + " WHERE "
                     + CHECKSUM_GUID + " = ?";
             ResultSet dbResult = null;
             
             try {
                 dbResult = DatabaseUtils.selectObject(dbConnector.getConnection(), sql, checksumGuid);
                 if(!dbResult.next()) {
                     log.debug("No checksum specification for the guid '" + checksumGuid 
                             + "' found with the SQL '" + sql + "'. A null is returned.");
                     return null;
                 }
                 ChecksumSpecTYPE res = new ChecksumSpecTYPE();
                 
                 res.setChecksumType(ChecksumType.fromValue(dbResult.getString(1)));
                 String salt = dbResult.getString(2);
                 if(salt != null && !salt.isEmpty()) {
                     res.setChecksumSalt(salt.getBytes());
                 }
                 
                 return res;
             } finally {
                 if(dbResult != null) {
                     dbResult.close();
                 }
             }
         } catch (SQLException e) {
             throw new IllegalStateException("Cannot retrive the requested checksum specification.", e);
         }
     }
     
     /**
      * Retrieves the guid corresponding to a given checksum specification. If no such entry exists, then it is created.
      * @param fileId The checksum specification to retrieve the guid of.
      * @return The guid of the give checksum specification.
      */
     private long retrieveChecksumSpecGuid(ChecksumSpecTYPE checksumType) {
         log.trace("Retrieving the guid for the checksum specification '{}'.", checksumType);
         String sql = "SELECT " + CHECKSUM_GUID + " FROM " + CHECKSUM_TABLE + " WHERE " + CHECKSUM_ALGORITHM 
                 + " = ? AND " + CHECKSUM_SALT + " = ?";
         Long guid = DatabaseUtils.selectLongValue(dbConnector.getConnection(), sql, checksumType.getChecksumType().toString(), 
                 new String(checksumType.getChecksumSalt()));
         // If no entry, then make one and extract the guid.
         if(guid == null) {
             insertChecksumSpec(checksumType);
             guid = DatabaseUtils.selectLongValue(dbConnector.getConnection(), sql, checksumType.getChecksumType().toString(), 
                     new String(checksumType.getChecksumSalt()));
         }
         return guid;
     }
     
     /**
      * Inserts a new entry in the 'checksum' table for the given checksum specification.
      * @param checksumType The checksum specification to insert into the 'checksum' table.
      */
     private void insertChecksumSpec(ChecksumSpecTYPE checksumType) {
         log.debug("Inserting the checksum specification '" + checksumType + "' into the checksum table.");
         String sql = "INSERT INTO " + CHECKSUM_TABLE + " ( " + CHECKSUM_ALGORITHM + ", " + CHECKSUM_SALT 
                 + " ) VALUES ( ?, ? )";
         DatabaseUtils.executeStatement(dbConnector.getConnection(), sql, checksumType.getChecksumType().toString(),
                 new String(checksumType.getChecksumSalt()));
     }
 }
