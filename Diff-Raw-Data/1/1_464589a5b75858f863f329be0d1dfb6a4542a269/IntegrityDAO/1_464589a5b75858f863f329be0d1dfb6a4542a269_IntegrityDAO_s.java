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
 
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.COLLECTIONS_TABLE;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.COLLECTION_ID;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.COLLECTION_KEY;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILES_CREATION_DATE;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILES_ID;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILES_KEY;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILES_TABLE;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILE_INFO_TABLE;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_CHECKSUM;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_CHECKSUM_STATE;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_FILE_KEY;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_FILE_SIZE;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_FILE_STATE;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_LAST_CHECKSUM_UPDATE;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_LAST_FILE_UPDATE;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_PILLAR_KEY;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.PILLAR_ID;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.PILLAR_KEY;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.PILLAR_TABLE;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.STATS_TABLE;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.STATS_COLLECTION_KEY;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.STATS_KEY;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.STATS_LAST_UPDATE;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.STATS_TIME;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.COLLECTION_STATS_TABLE;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.CS_CHECKSUM_ERRORS;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.CS_FILECOUNT;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.CS_FILESIZE;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.CS_KEY;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.CS_STAT_KEY;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.PS_CHECKSUM_ERRORS;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.PS_FILE_COUNT;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.PS_FILE_SIZE;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.PS_KEY;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.PS_STAT_KEY;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.PS_MISSING_FILES_COUNT;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.PS_PILLAR_KEY;
 import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.PILLAR_STATS_TABLE;
 
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
 import org.bitrepository.bitrepositoryelements.FileIDsData;
 import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
 import org.bitrepository.common.ArgumentValidator;
 import org.bitrepository.common.utils.Base16Utils;
 import org.bitrepository.common.utils.CalendarUtils;
 import org.bitrepository.integrityservice.cache.CollectionStat;
 import org.bitrepository.integrityservice.cache.FileInfo;
 import org.bitrepository.integrityservice.cache.PillarStat;
 import org.bitrepository.service.database.DBConnector;
 import org.bitrepository.service.database.DatabaseUtils;
 import org.bitrepository.settings.repositorysettings.Collections;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Handles the communication with the integrity database.
  */
 public class IntegrityDAO {
     /** The log.*/
     private Logger log = LoggerFactory.getLogger(getClass());
     /** The connector to the database.*/
     private final DBConnector dbConnector;
     /** Mapping from collectionID to the list of pillars in it */
     private final Map<String, List<String>> collectionPillarsMap;
     /** Caching of CollectionKeys (mapping from ID to Key) */
     private final Map<String, Long> collectionKeyCache;
     /** Caching of PillarKeys (mapping from ID to Key) */
     private final Map<String, Long> pillarKeyCache;
     
     /** 
      * Constructor.
      * @param dbConnector The connector to the database, where the cache is stored.
      * @param collections The collections object from the settings, to define the set of pillars and collections
      */
     public IntegrityDAO(DBConnector dbConnector, Collections collections) {
         ArgumentValidator.checkNotNull(dbConnector, "DBConnector dbConnector");
         
         this.dbConnector = dbConnector;
         collectionPillarsMap = new HashMap<String, List<String>>();
         for(org.bitrepository.settings.repositorysettings.Collection collection : collections.getCollection()) {
             collectionPillarsMap.put(collection.getID(), new ArrayList<String>(collection.getPillarIDs().getPillarID()));
         }
         collectionKeyCache = new HashMap<String, Long>();
         pillarKeyCache = new HashMap<String, Long>();
         initialisePillars();
         initializeCollections();
     }
     
     /**
      * Initialises the ids of all the pillars.
      * Ensures that all pillars in settings are defined properly in the database.
      */
     private synchronized void initialisePillars() {
         List<String> pillarsInDatabase = retrievePillarsInDatabase();
         if(pillarsInDatabase.isEmpty()) {
             insertAllPillarsIntoDatabase();
         } else {
             validateConsistencyBetweenPillarsInDatabaseAndSettings(pillarsInDatabase);
         }
     }
     
     private synchronized void initializeCollections() {
         List<String> collectionsInDatabase = retrieveCollectionsInDatabase();
         if(collectionsInDatabase.isEmpty()) {
             insertAllCollectionsIntoDatabase(collectionPillarsMap.keySet());
         } else {
             validateConsistencyBetweenCollectionsInDatabaseAndSettings(collectionsInDatabase);
         }
     }
     
     /**
      * @return The list of pillars defined in the database.
      */
     private List<String> retrievePillarsInDatabase() {
         String selectSql = "SELECT " + PILLAR_ID + " FROM " + PILLAR_TABLE;
         return DatabaseUtils.selectStringList(dbConnector, selectSql, new Object[0]);
     }
     
     /**
      * Inserts all pillar ids into the database.
      */
     private void insertAllPillarsIntoDatabase() {
         Set<String> pillars = new HashSet<String>();
         for(List<String> pillarList : collectionPillarsMap.values()) {
             pillars.addAll(pillarList);
         }
         for(String pillarId : pillars) {
             String sql = "INSERT INTO " + PILLAR_TABLE +" ( " + PILLAR_ID + " ) VALUES ( ? )";
             DatabaseUtils.executeStatement(dbConnector, sql, pillarId);
         }
     }
     
     /**
      *  @return The list of collections defined in the database.
      */
     private List<String> retrieveCollectionsInDatabase() {
         String selectSql = "SELECT " + COLLECTION_ID + " FROM " + COLLECTIONS_TABLE;
         return DatabaseUtils.selectStringList(dbConnector, selectSql, new Object[0]);
     }
     
     /**
      * Insert all collection ids into the database 
      */
     private void insertAllCollectionsIntoDatabase(Set<String> collections) {
         String sql = "INSERT INTO " + COLLECTIONS_TABLE + " ( " + COLLECTION_ID + " ) VALUES ( ? )";  
         for(String collection : collections) {
              DatabaseUtils.executeStatement(dbConnector, sql, collection);
         }
     }
     
     /**
      * Validates that the list of pillars in the settings corresponds to the list of pillars defined in the database.
      * It will throw an exception, if they are not similar.
      * TODO: BITMAG-846.
      * @param pillarsFromDatabase The pillars extracted from the database.
      */
     private void validateConsistencyBetweenPillarsInDatabaseAndSettings(List<String> pillarsFromDatabase) {
         Set<String> pillars = new HashSet<String>();
         for(List<String> pillarList : collectionPillarsMap.values()) {
             pillars.addAll(pillarList);
         }
         List<String> uniquePillarsInDatabase = new ArrayList<String>(pillarsFromDatabase);
         uniquePillarsInDatabase.removeAll(pillars);
         List<String> uniquePillarsInSettings = new ArrayList<String>(pillars);
         uniquePillarsInSettings.removeAll(pillarsFromDatabase);
         if(!uniquePillarsInDatabase.isEmpty() || !uniquePillarsInSettings.isEmpty()) {
             throw new IllegalStateException("There is inkonsistency between the pillars in the database, '" 
                     + pillarsFromDatabase + "', and the ones in the settings, '" + pillars + "'.");
         }
     }
     
     /**
      * Validates that the list of collections in the settings corresponds to the list of collections defined in the database.
      * It will throw an exception, if they are not similar.
      * TODO: BITMAG-860.
      * @param collectionsFromDatabase The collections extracted from the database.
      */
     private void validateConsistencyBetweenCollectionsInDatabaseAndSettings(List<String> collectionsFromDatabase) {
         List<String> uniqueCollectionsInDatabase = new ArrayList<String>(collectionsFromDatabase);
         uniqueCollectionsInDatabase.removeAll(collectionPillarsMap.keySet());
         List<String> uniqueCollectionsInSettings = new ArrayList<String>(collectionPillarsMap.keySet());
         uniqueCollectionsInSettings.removeAll(collectionsFromDatabase);
         if(!uniqueCollectionsInDatabase.isEmpty() || !uniqueCollectionsInSettings.isEmpty()) {
             throw new IllegalStateException("There is inkonsistency between the collections in the database, '" 
                     + collectionsFromDatabase + "', and the ones in the settings, '" + collectionPillarsMap.keySet() + "'.");
         }
     }
     
     /**
      * Inserts the results of a GetFileIDs operation for a given pillar.
      * @param data The results of the GetFileIDs operation.
      * @param pillarId The pillar, where the GetFileIDsOperation has been performed.
      * @param collectionId The id of the collection to update files in
      * @throws SQLException 
      */
     public void updateFileIDs(FileIDsData data, String pillarId, String collectionId) {
         ArgumentValidator.checkNotNull(data, "FileIDsData data");
         ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
         ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
         log.trace("Updating the file ids '" + data + "' for pillar '" + pillarId + "'");
         
         for(FileIDsDataItem dataItem : data.getFileIDsDataItems().getFileIDsDataItem()) {
             ensureFileIdExists(dataItem.getFileID(), collectionId);
             Date modifyDate = CalendarUtils.convertFromXMLGregorianCalendar(dataItem.getLastModificationTime());
             
             updateFileInfoLastFileUpdateTimestamp(pillarId, dataItem.getFileID(), modifyDate, collectionId);
             if(dataItem.isSetFileSize()) {
                 updateFileInfoFileSize(pillarId, dataItem.getFileID(), collectionId, dataItem.getFileSize().longValue());
             }
         }
     }
     
     /**
      * Handles the result of a GetChecksums operation on a given pillar.
      * @param data The result data from the GetChecksums operation on the given pillar.
      * @param pillarId The id of the pillar, where the GetChecksums operation has been performed.
      * @param collectionId The ID of the collection to update checksums in
      */
     public void updateChecksumData(List<ChecksumDataForChecksumSpecTYPE> data, String pillarId, String collectionId) {
         ArgumentValidator.checkNotNull(data, "List<ChecksumDataForChecksumSpecTYPE> data");
         ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
         ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
         
         log.trace("Updating the checksum data '" + data + "' for pillar '" + pillarId + "' in collection " + collectionId + "'");
         for(ChecksumDataForChecksumSpecTYPE csData : data) {
             ensureFileIdExists(csData.getFileID(), collectionId);
             updateFileInfoWithChecksum(csData, pillarId, collectionId);
         }
     }
     
     /**
      * Retrieves all the FileInfo information for a given file id.
      * @param fileId The id of the file.
      * @param collectionId The ID of the collection to get FileInfo's from
      * @return The list of information about this file.
      */
     public List<FileInfo> getFileInfosForFile(String fileId, String collectionId) {
         ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
         ArgumentValidator.checkNotNullOrEmpty(fileId, "String collectionId");
         
         // Define the index in the result set for the different variables to extract.
         final int indexLastFileCheck = 1;
         final int indexChecksum = 2;
         final int indexLastChecksumCheck = 3;
         final int indexPillarKey = 4;
         final int indexFileState = 5;
         final int indexChecksumState = 6;
         final int indexFileSize = 7;
         
         Long collectionKey = retrieveCollectionKey(collectionId);
         Long fileKey = retrieveFileKey(fileId, collectionKey);
         
         if(fileKey == null) {
             log.debug("Trying to retrieve file infos for non-existing file id: '" + fileId + "'.");
             return new ArrayList<FileInfo>();
         }
         
         List<FileInfo> res = new ArrayList<FileInfo>();
         String sql = "SELECT " + FI_LAST_FILE_UPDATE + ", " + FI_CHECKSUM + ", " + FI_LAST_CHECKSUM_UPDATE + ", " 
                 + FI_PILLAR_KEY + ", " + FI_FILE_STATE + ", " + FI_CHECKSUM_STATE + ", " + FI_FILE_SIZE
                 + " FROM " + FILE_INFO_TABLE 
                 + " WHERE " + FI_FILE_KEY + " = ?";
         
         try {
             ResultSet dbResult = null;
             PreparedStatement ps = null;
             Connection conn = null;
             try {
                 conn = dbConnector.getConnection();
                 ps = DatabaseUtils.createPreparedStatement(conn, sql, fileKey);
                 dbResult = ps.executeQuery();
                 
                 while(dbResult.next()) {
                     Date lastFileCheck = dbResult.getTimestamp(indexLastFileCheck);
                     String checksum = dbResult.getString(indexChecksum);
                     Date lastChecksumCheck = dbResult.getTimestamp(indexLastChecksumCheck);
                     long pillarKey = dbResult.getLong(indexPillarKey);
                     Long fileSize = dbResult.getLong(indexFileSize);
                     String pillarId = retrievePillarIDFromKey(pillarKey);
                     
                     FileState fileState = FileState.fromOrdinal(dbResult.getInt(indexFileState));
                     ChecksumState checksumState = ChecksumState.fromOrdinal(dbResult.getInt(indexChecksumState));
                     
                     FileInfo f = new FileInfo(fileId, CalendarUtils.getXmlGregorianCalendar(lastFileCheck), checksum, 
                             fileSize, CalendarUtils.getXmlGregorianCalendar(lastChecksumCheck), pillarId,
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
      * @param collectionId The ID of the collection to get fileIDs from
      * @return The list of all the file ids within the database.
      */
     public List<String> getAllFileIDs(String collectionId) {
         log.trace("Retrieving all file ids.");
         Long collectionKey = retrieveCollectionKey(collectionId);
         String sql = "SELECT " + FILES_ID + " FROM " + FILES_TABLE + " WHERE " + COLLECTION_KEY + " = ?";
         return DatabaseUtils.selectStringList(dbConnector, sql, collectionKey);
     }
     
     /**
      * Get the number of files in a given collection
      * @param collectionId The ID of the collection
      * @return the number of files in a collection 
      */
     public long getNumberOfFilesInCollection(String collectionId) {
         log.trace("Retrieving the number of fileIds in collection '" + collectionId + "'.");
         Long collectionKey = retrieveCollectionKey(collectionId);
         String sql = "SELECT COUNT(" + FILES_ID + ") FROM " + FILES_TABLE + " WHERE " + COLLECTION_KEY + " = ?";
         return DatabaseUtils.selectLongValue(dbConnector, sql, collectionKey);
     }
     
     public long getNumberOfChecksumErrorsIncollection(String collectionID) {
         log.trace("Retrieving the number of files with checksumerrors in collection '" + collectionID + "'.");
         Long collectionKey = retrieveCollectionKey(collectionID);
         String sql = " SELECT COUNT (DISTINCT " + FILE_INFO_TABLE + "." + FI_FILE_KEY + ") FROM " + FILE_INFO_TABLE
                     + " JOIN " + FILES_TABLE
                     + " ON " + FILE_INFO_TABLE + "." + FI_FILE_KEY + " = " + FILES_TABLE + "." + FILES_KEY
                     + " WHERE " + FILE_INFO_TABLE + "." + FI_CHECKSUM_STATE + " = ?"
                     + " AND " + FILES_TABLE + "." +COLLECTION_KEY + " = ?";
         return DatabaseUtils.selectLongValue(dbConnector, sql, ChecksumState.ERROR.ordinal(), collectionKey);
     }
     
     /**
      * Retrieves the number of files in the given pillar, which has the file state 'EXISTING'.
      * @param pillarId The id of the pillar.
      * @param collectionId The ID of the collection to get the number of existing files from
      * @return The number of files with file state 'EXISTING' for the given pillar.
      */
     public int getNumberOfExistingFilesForAPillar(String pillarId, String collectionId) {
         ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
         ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
         log.trace("Retrieving number of existing files at '{}'.", pillarId);
         Long pillarKey = retrievePillarKey(pillarId);
         Long collectionKey = retrieveCollectionKey(collectionId);
         String sql = "SELECT COUNT(*) FROM " + FILE_INFO_TABLE 
                 + " JOIN " + FILES_TABLE 
                 + " ON " + FILE_INFO_TABLE + "." + FI_FILE_KEY + " = " + FILES_TABLE + "." + FILES_KEY 
                 + " WHERE " + FI_PILLAR_KEY + " = ?" 
                 + " AND " + FI_FILE_STATE + " = ?"
                 + " AND " + COLLECTION_KEY + " = ?";
         return DatabaseUtils.selectIntValue(dbConnector, sql, pillarKey, FileState.EXISTING.ordinal(), collectionKey);
     }
     
     /**
      * Retrieves the number of files in the given pillar, which has the file state 'MISSING'.
      * @param pillarId The id of the pillar.
      * @param collectionId The ID of the collection to get the number of missing files from
      * @return The number of files with file state 'MISSING' for the given pillar.
      */
     public int getNumberOfMissingFilesForAPillar(String pillarId, String collectionId) {
         ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
         ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
         log.trace("Retrieving number of missing files at '{}'.", pillarId);
         Long pillarKey = retrievePillarKey(pillarId);
         Long collectionKey = retrieveCollectionKey(collectionId);
         String sql = "SELECT COUNT(*) FROM " + FILE_INFO_TABLE  
                 + " JOIN " + FILES_TABLE 
                 + " ON " + FILE_INFO_TABLE + "." + FI_FILE_KEY + " = " + FILES_TABLE + "." + FILES_KEY 
                 + " WHERE " + FI_PILLAR_KEY + " = ?" 
                 + " AND " + FI_FILE_STATE + " = ?"
                 + " AND " + COLLECTION_KEY + " = ?";
         return DatabaseUtils.selectIntValue(dbConnector, sql, pillarKey, FileState.MISSING.ordinal(), collectionKey);
     }
     
     /**
      * Retrieves the number of files in the given pillar, which has the checksum state 'INCONSISTENT'.
      * @param pillarId The id of the pillar.
      * @param collectionId The ID of the collection to get the number of checksum errors from
      * @return The number of files with checksum state 'INCONSISTENT' for the given pillar.
      */
     public int getNumberOfChecksumErrorsForAPillar(String pillarId, String collectionId) {
         ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
         ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
         log.trace("Retrieving files with checksum error at '{}'.", pillarId);
         Long pillarKey = retrievePillarKey(pillarId);
         Long collectionKey = retrieveCollectionKey(collectionId);
         String sql = "SELECT COUNT(*) FROM " + FILE_INFO_TABLE 
                 + " JOIN " + FILES_TABLE 
                 + " ON " + FILE_INFO_TABLE + "." + FI_FILE_KEY + " = " + FILES_TABLE + "." + FILES_KEY
                 + " WHERE " + FI_PILLAR_KEY + " = ?" 
                 + " AND " + FI_CHECKSUM_STATE + " = ?"
                 + " AND " + COLLECTION_KEY + " = ?";
         return DatabaseUtils.selectIntValue(dbConnector, sql, pillarKey, ChecksumState.ERROR.ordinal(), collectionKey);
     }
 
     /**
      * Sets a specific file to missing at a given pillar.
      * @param fileId The id of the file, which is missing at the pillar.
      * @param collectionId The ID of the collection to set the file missing in
      * @param pillarId The id of the pillar which is missing the file.
      */
     public void setFileMissing(String fileId, String pillarId, String collectionId) {
         ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
         ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
         ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
         log.debug("Set file-state missing for file '" + fileId + "' at pillar '" + pillarId + "' to be missing.");
         Long collectionKey = retrieveCollectionKey(collectionId);
         String sqlUpdate = "UPDATE " + FILE_INFO_TABLE + " SET " + FI_FILE_STATE + " = ? , " + FI_CHECKSUM_STATE + " = ?" 
                 + " WHERE " + FI_FILE_KEY + " = (" 
                     + " SELECT " + FILES_KEY + " FROM " + FILES_TABLE 
                     + " WHERE " + FILES_ID + " = ?" 
                     + " AND " + COLLECTION_KEY + " = ?)" 
                 + " AND " + FI_PILLAR_KEY + " = (" 
                     + " SELECT " + PILLAR_KEY + " FROM " + PILLAR_TABLE 
                     + " WHERE " + PILLAR_ID + " = ?)";
         DatabaseUtils.executeStatement(dbConnector, sqlUpdate, FileState.MISSING.ordinal(), 
                 ChecksumState.UNKNOWN.ordinal(), fileId, collectionKey, pillarId);
     }
     
     /**
      * Sets a specific file have checksum errors at a given pillar.
      * @param fileId The id of the file, which has checksum error at the pillar.
      * @param pillarId The id of the pillar which has checksum error on the file.
      * @param collectionId The ID of the collection to set checksum error for the file in.
      */
     public void setChecksumError(String fileId, String pillarId, String collectionId) {
         ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
         ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
         ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
         log.debug("Sets invalid checksum for file '" + fileId + "' at pillar '" + pillarId + "'");
         Long collectionKey = retrieveCollectionKey(collectionId);
         String sqlUpdate = "UPDATE " + FILE_INFO_TABLE + " SET " + FI_FILE_STATE + " = ? , " + FI_CHECKSUM_STATE + " = ?" 
                 + " WHERE " + FI_FILE_KEY + " = (" 
                     + " SELECT " + FILES_KEY + " FROM " + FILES_TABLE 
                     + " WHERE " + FILES_ID + " = ?" 
                     + " AND " + COLLECTION_KEY + " = ?)" 
                 + " AND " + FI_PILLAR_KEY + " = (" 
                     + " SELECT " + PILLAR_KEY + " FROM " + PILLAR_TABLE
                     + " WHERE " + PILLAR_ID + " = ?)";
         DatabaseUtils.executeStatement(dbConnector, sqlUpdate, FileState.EXISTING.ordinal(), 
                 ChecksumState.ERROR.ordinal(), fileId, collectionKey, pillarId);
     }
 
     /**
      * Sets a specific file have checksum errors at a given pillar.
      * Unless the given file is missing at the given pillar.
      * @param fileId The id of the file, which has checksum error at the pillar.
      * @param pillarId The id of the pillar which has checksum error on the file.
      * @param collectionId The ID of the collection to set the checksum for the file valid in
      */
     public void setChecksumValid(String fileId, String pillarId, String collectionId) {
         ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
         ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
         ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
         log.debug("Sets valid checksum for file '" + fileId + "' for pillar '" + pillarId + "'");
         Long collectionKey = retrieveCollectionKey(collectionId);
         String sql = "UPDATE " + FILE_INFO_TABLE + " SET " + FI_FILE_STATE + " = ? , " + FI_CHECKSUM_STATE + " = ?"
                 + " WHERE " + FI_PILLAR_KEY + " = (" 
                     + " SELECT " + PILLAR_KEY + " FROM " + PILLAR_TABLE 
                     + " WHERE " + PILLAR_ID + " = ? )" 
                 + " AND " + FI_FILE_KEY + " = (" 
                     + " SELECT " + FILES_KEY + " FROM " + FILES_TABLE 
                     + " WHERE " + FILES_ID + " = ?" 
                     + " AND " + COLLECTION_KEY + " = ?)"
                 + " AND " + FI_FILE_STATE + " != ?";
         DatabaseUtils.executeStatement(dbConnector, sql, FileState.EXISTING.ordinal(), 
                 ChecksumState.VALID.ordinal(), pillarId, fileId, collectionKey, FileState.MISSING.ordinal());
     }
     
     /**
      * Remove the given file id from the file table, and all the entries for this file in the file info table. 
      * @param fileId The id of the file to be removed.
      * @param collectionId The ID of the collection to remove the file from
      */
     public void removeFileId(String fileId, String collectionId) {
         ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
         ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
         Long collectionKey = retrieveCollectionKey(collectionId);
         Long key = retrieveFileKey(fileId, collectionKey);
         
         if(key == null) {
             log.warn("The file '" + fileId + "' has already been removed.");
             return;
         }
         
         log.info("Removing the entries for the file with id '" + fileId + "' from the file info table.");
         String removeFileInfoEntrySql = "DELETE FROM " + FILE_INFO_TABLE + " WHERE " + FI_FILE_KEY + " = ?";
         DatabaseUtils.executeStatement(dbConnector, removeFileInfoEntrySql, key);
         
         log.info("Removing the file id '" + fileId + "' from the files table.");
         String removeFileIDSql = "DELETE FROM " + FILES_TABLE + " WHERE " + FILES_KEY + " = ?";
         DatabaseUtils.executeStatement(dbConnector, removeFileIDSql, key);
     }
     
     /**
      * Finds the id of the files which at any pillar exists but is missing its checksum state.
      * @param collectionId The ID of the collection to find missing checksums in
      */
     public List<String> findMissingChecksums(String collectionId) {
         long startTime = System.currentTimeMillis();
         ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
         log.trace("Locating files which are missing the checksum at any pillar.");
         Long collectionKey = retrieveCollectionKey(collectionId);
         String requestSql = "SELECT " + FILES_TABLE + "." + FILES_ID + " FROM " + FILES_TABLE 
                 + " JOIN " + FILE_INFO_TABLE 
                 + " ON " + FILES_TABLE + "." + FILES_KEY + " = " + FILE_INFO_TABLE + "." + FI_FILE_KEY
                 + " WHERE " + FILES_TABLE + "." + COLLECTION_KEY + " = ? " 
                 + " AND " + FILE_INFO_TABLE + "." + FI_FILE_STATE + " = ?" 
                 + " AND " + FILE_INFO_TABLE + "." + FI_CHECKSUM_STATE + " = ?";
         List<String> result = DatabaseUtils.selectStringList(dbConnector, requestSql, collectionKey, 
                 FileState.EXISTING.ordinal(), ChecksumState.UNKNOWN.ordinal());
         log.debug("Located " + result.size() + " missing checksums in " + (System.currentTimeMillis() - startTime) + "ms");
         return result;
     }
 
     /**
      * Finds the id of the files which have a checksum older than a given date.
      * @param date The date for the checksum to be older than.
      * @param pillarID The ID of the pillar to find obsolete checksum for.
      * @param collectionId The ID of the collection to find files with obsolete checksums in
      * @return The list of ids for the files which have a checksum older than the given date.
      */
     public List<String> findFilesWithOldChecksum(Date date, String pillarID, String collectionId) {
         ArgumentValidator.checkNotNullOrEmpty(pillarID, "String fileId");
         ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
         long startTime = System.currentTimeMillis();
         log.trace("Locating files with obsolete checksums from pillar " + pillarID);
         Long pillarKey = retrievePillarKey(pillarID);
         Long collectionKey = retrieveCollectionKey(collectionId);
 
         String requestSql = "SELECT " + FILES_TABLE + "." + FILES_ID + " FROM " + FILES_TABLE 
                 + " JOIN " + FILE_INFO_TABLE 
                 + " ON " + FILES_TABLE + "." + FILES_KEY + " = " + FILE_INFO_TABLE + "." + FI_FILE_KEY
                 + " WHERE " + FILES_TABLE + "." + COLLECTION_KEY + " = ? "
                 + " AND " + FILE_INFO_TABLE + "." + FI_LAST_CHECKSUM_UPDATE + " < ?" 
                 + " AND " + FI_PILLAR_KEY + " = ?";
         List<String> result = DatabaseUtils.selectStringList(dbConnector, requestSql, collectionKey, date, pillarKey);
         log.debug("Located " + result.size() + " obsolete checksums on pillar " + pillarID + " in " +
             (System.currentTimeMillis() - startTime) + "ms");
         return result;
     }
     
     /**
      * Finds the id of the files which at any pillar does not exist.
      * @param collectionId The collection in which to look for missing files
      * @return The list of ids for the files which are missing at some pillar.
      */
     public List<String> findMissingFiles(String collectionId) {
         Long collectionKey = retrieveCollectionKey(collectionId);
         log.trace("Locating files which are missing at any pillar.");
         String requestSql = "SELECT " + FILES_TABLE + "." + FILES_ID + " FROM " + FILES_TABLE 
                 +" JOIN " + FILE_INFO_TABLE 
                 + " ON " + FILES_TABLE + "." + FILES_KEY + "=" + FILE_INFO_TABLE + "." + FI_FILE_KEY 
                 + " WHERE " + FILES_TABLE + "." + COLLECTION_KEY + " = ? " 
                 + " AND " + FILE_INFO_TABLE + "." + FI_FILE_STATE + " != ? ";
         return DatabaseUtils.selectStringList(dbConnector, requestSql, collectionKey, FileState.EXISTING.ordinal());
     }
     
     /**
      * Finds the ids of the pillars where the given file is missing.
      * @param fileId The id of the file which is missing.
      * @param collectionId The ID of the collection to get missing files for the given pillar
      * @return The list of ids for the pillars where the file is missing (empty list of the file is not missing).
      */
     public List<String> getMissingAtPillars(String fileId, String collectionId) {
         long startTime = System.currentTimeMillis();
         log.trace("Locating the pillars where fileId '" + fileId + "' in collection' " + collectionId + "' is missing.");
         Long collectionKey = retrieveCollectionKey(collectionId);
         String requestSql = "SELECT " + PILLAR_TABLE + "." + PILLAR_ID + " FROM " + PILLAR_TABLE 
                 + " JOIN " + FILE_INFO_TABLE 
                 + " ON " + PILLAR_TABLE + "." + PILLAR_KEY + "=" + FILE_INFO_TABLE + "." + FI_PILLAR_KEY 
                 + " WHERE " + FILE_INFO_TABLE + "." + FI_FILE_STATE + " != ?"
                 + " AND " + FILE_INFO_TABLE + "." + FI_FILE_KEY + " = ("
                     + " SELECT " + FILES_KEY + " FROM " + FILES_TABLE 
                     + " WHERE " + FILES_ID + " = ?" 
                     + " AND " + COLLECTION_KEY + " = ?)";
         List<String> result = DatabaseUtils.selectStringList(dbConnector, requestSql, FileState.EXISTING.ordinal(),
                 fileId, collectionKey);
         log.debug("Located " + result.size() + " checksums in " + (System.currentTimeMillis() -startTime) + "ms");
         return result;
     }
     
     /**
      * Extracting the file ids for the files with inconsistent checksums. Thus finding the files with checksum errors.
      * It is done in the following way:
      * 1. Find all the unique combinations of checksums and file keys in the FileInfo table. 
      *   Though not for the files which are reported as missing or the checksum is null.
      * 2. Count the number of occurrences for each file key (thus counting the amount of checksums for each file).
      * 3. Eliminate the file keys which only had one count (thus removing the file keys with consistent checksum).
      * 4. Select the respective file ids for the remaining file keys.
      *   Though only for those, where the creation date for the file is older than the given argument.
      * This is all combined into one single SQL statement for optimisation.
      * 
      * @param maxCreationDate The maximum date for the validation
      * @param collectionId The ID of the collection to find inconsistent checksums in
      * @return The list of file ids for the files with inconsistent checksums.
      */
     public List<String> findFilesWithInconsistentChecksums(Date maxCreationDate, String collectionId) {
         long startTime = System.currentTimeMillis();
         Long collectionKey = retrieveCollectionKey(collectionId);       
         log.trace("Localizing the file ids where the checksums are not consistent.");
         String findUniqueSql = "SELECT " + FI_CHECKSUM + " , " + FI_FILE_KEY + " FROM " + FILE_INFO_TABLE
                 + " WHERE " + FI_FILE_STATE + " <> ?"
                 + " AND " + FI_CHECKSUM + " IS NOT NULL"
                 + " GROUP BY " + FI_CHECKSUM + " , " + FI_FILE_KEY;
         String countSql = "SELECT " + FI_FILE_KEY + " , COUNT(*) AS num FROM ( " + findUniqueSql + " ) AS unique1 "
                 + " GROUP BY " + FI_FILE_KEY;
         String eliminateSql = "SELECT " + FI_FILE_KEY + " FROM ( " + countSql + " ) AS count1 WHERE count1.num > 1";
         String selectSql = "SELECT " + FILES_TABLE + "." + FILES_ID + " FROM " + FILES_TABLE 
                 + " JOIN ( " + eliminateSql + " ) AS eliminate1"
                 + " ON " + FILES_TABLE + "." + FILES_KEY + " = eliminate1." + FI_FILE_KEY 
                 + " WHERE " + FILES_CREATION_DATE + " < ?"
                 + " AND " + COLLECTION_KEY + " = ? ";
         List<String> res = DatabaseUtils.selectStringList(dbConnector, selectSql,  
                 FileState.MISSING.ordinal(), maxCreationDate, collectionKey);
         log.debug("Found " + res.size() + " inconsistencies in " + (System.currentTimeMillis() - startTime) + "ms");
         return res;
     }
     
     /**
      * Updating all the entries for the files with consistent checksums to set their checksum state to valid.
      * It is done in the following way:
      * 1. Find all the unique combinations of checksums and file keys in the FileInfo table. 
      *    Though not for the files which are reported as missing, or if the checksum is null.
      * 2. Count the number of occurrences for each file key (thus counting the amount of different checksums for 
      *    each file).
      * 3. Eliminate the files with a consistent checksum (select only those with the count of unique checksums of 1)
      * 4. Update the file infos for the respective files by settings their checksum state to valid.
      * This is all combined into one single SQL statement for optimisation.
      * @param collectionId The ID of the collections
      */
     public void setFilesWithConsistentChecksumsToValid(String collectionId) {
         long startTime = System.currentTimeMillis();
         Long collectionKey = retrieveCollectionKey(collectionId);       
         log.trace("Localizing the file ids where the checksums are consistent and setting them to the checksum state '"
                 + ChecksumState.VALID + "'.");
         String updateSql = "UPDATE " + FILE_INFO_TABLE + " SET " + FI_CHECKSUM_STATE + " = ?" 
                 + " WHERE " + FI_CHECKSUM_STATE + " <> ?"
                 + " AND EXISTS ("
                     + " SELECT 1 FROM " + FILE_INFO_TABLE + " AS inner_fi"
                     + " WHERE " + "inner_fi." + FI_FILE_STATE + " <> ?"
                     + " AND inner_fi." + FI_CHECKSUM + " IS NOT NULL"
                     + " AND inner_fi." + FI_FILE_KEY + " = " + FILE_INFO_TABLE + "." + FI_FILE_KEY 
                     + " GROUP BY inner_fi." + FI_FILE_KEY 
                     + " HAVING COUNT(DISTINCT " + FI_CHECKSUM + ") = 1)"
                 + " AND EXISTS (" 
                     + " SELECT 1 FROM " + FILES_TABLE 
                     + " WHERE " + FILES_TABLE + "." + FILES_KEY + " = " + FILE_INFO_TABLE + "." + FILES_KEY 
                     + " AND " + FILES_TABLE + "." + COLLECTION_KEY + " = ? )";
         DatabaseUtils.executeStatement(dbConnector, updateSql, ChecksumState.VALID.ordinal(), 
                 ChecksumState.VALID.ordinal(), FileState.MISSING.ordinal(), collectionKey);
         log.debug("Marked consistent files in " + (System.currentTimeMillis() - startTime) + "ms");
     }
 
     /**
      * Retrieves the date for the latest file entry for a given collection.
      * E.g. the date for the latest file which has been positively identified as existing.  
      * @param collectionId The pillar whose latest file entry is requested.
      * @return The requested date.
      */
     public Date getDateForNewestFileEntryForCollection(String collectionId) {
         Long collectionKey = retrieveCollectionKey(collectionId);
         String retrieveSql = "SELECT " + FI_LAST_FILE_UPDATE + " FROM " + FILE_INFO_TABLE 
                 + " JOIN " + FILES_TABLE 
                 + " ON " + FILE_INFO_TABLE + "." + FILES_KEY + " = " + FILES_TABLE + "." + FILES_KEY
                 + " WHERE " + FILES_TABLE + "." + COLLECTION_KEY + " = ?" 
                 + " AND " + FI_FILE_STATE + " = ?" 
                 + " ORDER BY " + FI_LAST_FILE_UPDATE + " DESC ";
         return DatabaseUtils.selectFirstDateValue(dbConnector, retrieveSql, collectionKey, 
                 FileState.EXISTING.ordinal());
     }
     
     /**
      * Retrieves the date for the latest file entry for a given pillar.
      * E.g. the date for the latest file which has been positively identified as existing on the given pillar.  
      * @param pillarId The pillar whose latest file entry is requested.
      * @param collectionId The ID of the collection 
      * @return The requested date.
      */
     public Date getDateForNewestFileEntryForPillar(String pillarId, String collectionId) {
         Long collectionKey = retrieveCollectionKey(collectionId);
         String retrieveSql = "SELECT " + FI_LAST_FILE_UPDATE + " FROM " + FILE_INFO_TABLE 
                 + " JOIN " + FILES_TABLE 
                 + " ON " + FILE_INFO_TABLE + "." + FILES_KEY + " = " + FILES_TABLE + "." + FILES_KEY
                 + " WHERE " + FILES_TABLE + "." + COLLECTION_KEY + " = ?" 
                 + " AND " + FI_FILE_STATE + " = ?" 
                 + " AND " + FI_PILLAR_KEY + " = (" 
                     + " SELECT " + PILLAR_KEY + " FROM " + PILLAR_TABLE 
                     + " WHERE " + PILLAR_ID + " = ? )" 
                 + " ORDER BY " + FI_LAST_FILE_UPDATE + " DESC ";
         return DatabaseUtils.selectFirstDateValue(dbConnector, retrieveSql, collectionKey, 
                 FileState.EXISTING.ordinal(), pillarId);
     }
     
     /**
      * Retrieves the date for the latest checksum entry for a given pillar.
      * E.g. the date for the latest checksum which has been positively identified as valid on the given pillar.  
      * @param pillarId The pillar whose latest checksum entry is requested.
      * @param collectionId The ID of the collection to get the newest checksum from
      * @return The requested date.
      */
     public Date getDateForNewestChecksumEntryForPillar(String pillarId, String collectionId) {
         Long collectionKey = retrieveCollectionKey(collectionId);
         String retrieveSql = "SELECT " + FI_LAST_CHECKSUM_UPDATE + " FROM " + FILE_INFO_TABLE 
                 + " JOIN " + FILES_TABLE 
                 + " ON " + FILE_INFO_TABLE + "." + FILES_KEY + " = " + FILES_TABLE + "." + FILES_KEY
                 + " WHERE " + FILES_TABLE + "." + COLLECTION_KEY + " = ?"
                 + " AND " + FI_FILE_STATE + " = ?"
                 + " AND " + FI_PILLAR_KEY + " = ("
                     + " SELECT " + PILLAR_KEY + " FROM " + PILLAR_TABLE 
                     + " WHERE " + PILLAR_ID + " = ? )"
                 + " ORDER BY " + FI_LAST_CHECKSUM_UPDATE + " DESC ";
         return DatabaseUtils.selectFirstDateValue(dbConnector, retrieveSql, collectionKey, 
                 FileState.EXISTING.ordinal(), pillarId);
     }
 
     /**
      * Settings the file state of all the files to UNKNOWN.
      * @param collectionId The collection to work on
      */
     public void setAllFileStatesToUnknown(String collectionId) {
         log.trace("Setting the file state for all files to '" + FileState.UNKNOWN + "'.");
         Long collectionKey = retrieveCollectionKey(collectionId);
         String updateSql = "UPDATE " + FILE_INFO_TABLE + " SET " + FI_FILE_STATE + " = ?"
                 + " WHERE EXISTS ("
                     + "SELECT 1 FROM " + FILES_TABLE 
                     + " WHERE " + FILES_TABLE + "." + FILES_KEY + " = " + FILE_INFO_TABLE + "." + FI_FILE_KEY 
                     + " AND " + FILES_TABLE + "." + COLLECTION_KEY + " = ?)";
         DatabaseUtils.executeStatement(dbConnector, updateSql, FileState.UNKNOWN.ordinal(), collectionKey);
     }
     
     /**
      * Settings the file state of all the UNKNOWN files to MISSING.
      * @param minAge The date for the minimum age of the file. 
      * @param collectionId The ID of the collection to set files to unknown in
      */
     public void setOldUnknownFilesToMissing(Date minAge, String collectionId) {
         log.trace("Setting the file state for all '" + FileState.UNKNOWN + "' files to '" + FileState.MISSING + "'.");
         Long collectionKey = retrieveCollectionKey(collectionId);
         String updateSql = "UPDATE " + FILE_INFO_TABLE + " SET " + FI_FILE_STATE + " = ?"
         		+ " WHERE " + FILE_INFO_TABLE + "." + FI_FILE_STATE + " = ?"
         		+ " AND " + " EXISTS ( " 
         				+ "SELECT 1 FROM " + FILES_TABLE 
         				+ " WHERE " + FILES_TABLE + "." + FILES_KEY + " = " + FILE_INFO_TABLE + "." + FI_FILE_KEY 
         				+ " AND " + FILES_TABLE + "." + COLLECTION_KEY + " = ?" 
         				+ " AND " + FILES_TABLE + "." + FILES_CREATION_DATE + " < ? )";
         
         DatabaseUtils.executeStatement(dbConnector, updateSql, FileState.MISSING.ordinal(), 
                 FileState.UNKNOWN.ordinal(), collectionKey, minAge);
     }
     
     /**
      * Retrieves list of existing files on a pillar, within the defined range.
      * @param pillarId The id of the pillar.
      * @param min The minimum count.
      * @param max The maximum count.
      * @param collectionId The ID of the collection to get file list from the pillar
      * @return The list of file ids between the two counts.
      */
     public List<String> getFilesOnPillar(String pillarId, long min, long max, String collectionId) {
         log.trace("Locating all existing files for pillar '" + pillarId + "' from number " + min + " to " + max +
         		" in collection '" + collectionId + "'.");
         Long collectionKey = retrieveCollectionKey(collectionId);
         String selectSql = "SELECT " + FILES_TABLE + "." + FILES_ID + " FROM " + FILES_TABLE 
                 + " JOIN " + FILE_INFO_TABLE 
                 + " ON " + FILES_TABLE + "." + FILES_KEY + "=" + FILE_INFO_TABLE + "." + FI_FILE_KEY 
                 + " WHERE " + FILE_INFO_TABLE + "." + FI_FILE_STATE + " = ?"
                 + " AND " + FILES_TABLE + "." + COLLECTION_KEY + "= ?"
                 + " AND " + FILE_INFO_TABLE + "." + FI_PILLAR_KEY + " = ("
                     + " SELECT " + PILLAR_KEY + " FROM " + PILLAR_TABLE 
                     + " WHERE " + PILLAR_ID + " = ?)" 
                 + " ORDER BY " + FILES_TABLE + "." + FILES_KEY;
         
         try {
             ResultSet dbResult = null;
             PreparedStatement ps = null;
             Connection conn = null;
             try {
                 conn = dbConnector.getConnection();
                 ps = DatabaseUtils.createPreparedStatement(conn, selectSql, FileState.EXISTING.ordinal(), 
                         collectionKey, pillarId);
                 dbResult = ps.executeQuery();
                 
                 List<String> res = new ArrayList<String>();
                 
                 int i = 0;
                 while(dbResult.next()) {
                     if(i >= min && i < max) {
                         res.add(dbResult.getString(1));
                         
                     }
                     i++;
                 }
                 
                 return res;
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
             throw new IllegalStateException("Could not retrieve the file ids for '" + pillarId + "' between counts "
                     + min + " and " + max + " with the SQL '" + selectSql + "'.", e);
         }
     }
 
     /**
      * Retrieves list of missing files on a pillar, within the defined range.
      * @param pillarId The id of the pillar.
      * @param min The minimum count.
      * @param max The maximum count.
      * @param collectionId The ID of the collection to get list of missing files from
      * @return The list of file ids between the two counts.
      */
     public List<String> getMissingFilesOnPillar(String pillarId, long min, long max, String collectionId) {
         log.trace("Locating all existing files for pillar '" + pillarId + "' from number " + min + " to " + max + ".");
         Long collectionKey = retrieveCollectionKey(collectionId);
         String selectSql = "SELECT " + FILES_TABLE + "." + FILES_ID + " FROM " + FILES_TABLE 
                 + " JOIN " + FILE_INFO_TABLE 
                 + " ON " + FILES_TABLE + "." + FILES_KEY + "=" + FILE_INFO_TABLE + "." + FI_FILE_KEY 
                 + " WHERE " + FILE_INFO_TABLE + "." + FI_FILE_STATE + " = ? "
                 + "AND "+ FILES_TABLE + "." + COLLECTION_KEY + "= ? " 
                 + "AND " + FILE_INFO_TABLE + "." + FI_PILLAR_KEY + " = ("
                     + " SELECT " + PILLAR_KEY + " FROM " + PILLAR_TABLE 
                     + " WHERE " + PILLAR_ID + " = ?)"
                 + " ORDER BY " + FILES_TABLE + "." + FILES_KEY;
         
         try {
             ResultSet dbResult = null;
             PreparedStatement ps = null;
             Connection conn = null;
             try {
                 conn = dbConnector.getConnection();
                 ps = DatabaseUtils.createPreparedStatement(conn, selectSql, FileState.MISSING.ordinal(), 
                         collectionKey, pillarId);
                 dbResult = ps.executeQuery();
                 
                 List<String> res = new ArrayList<String>();
                 
                 int i = 0;
                 while(dbResult.next()) {
                     if(i >= min && i < max) {
                         res.add(dbResult.getString(1));
                         
                     }
                     i++;
                 }
                 
                 return res;
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
             throw new IllegalStateException("Could not retrieve the file ids for '" + pillarId + "' between counts "
                     + min + " and " + max + " with the SQL '" + selectSql + "'.", e);
         }
     }
 
     /**
      * Retrieves list of existing files on a pillar, within the defined range.
      * @param pillarId The id of the pillar.
      * @param min The minimum count.
      * @param max The maximum count.
      * @param collectionId The ID of the collection to get list of checksum errors from
      * @return The list of file ids between the two counts.
      */
     public List<String> getFilesWithChecksumErrorsOnPillar(String pillarId, long min, long max, String collectionId) {
         log.trace("Locating all existing files for pillar '" + pillarId + "' from number " + min + " to " + max + ".");
         Long collectionKey = retrieveCollectionKey(collectionId);
         String selectSql = "SELECT " + FILES_TABLE + "." + FILES_ID + " FROM " + FILES_TABLE 
                 + " JOIN " + FILE_INFO_TABLE 
                 + " ON " + FILES_TABLE + "." + FILES_KEY + "=" + FILE_INFO_TABLE + "." + FI_FILE_KEY 
                 + " WHERE " + FILE_INFO_TABLE + "." + FI_CHECKSUM_STATE + " = ?"
                 + " AND "+ FILES_TABLE + "." + COLLECTION_KEY + "= ?"
                 + " AND " + FILE_INFO_TABLE + "." + FI_PILLAR_KEY + " = (" 
                     + " SELECT " + PILLAR_KEY + " FROM " + PILLAR_TABLE 
                     + " WHERE " + PILLAR_ID + " = ?)" 
                 + " ORDER BY " + FILES_TABLE + "." + FILES_KEY;
         
         try {
             ResultSet dbResult = null;
             PreparedStatement ps = null;
             Connection conn = null;
             try {
                 conn = dbConnector.getConnection();
                 ps = DatabaseUtils.createPreparedStatement(conn, selectSql, ChecksumState.ERROR.ordinal(), 
                         collectionKey, pillarId);
                 dbResult = ps.executeQuery();
                 
                 List<String> res = new ArrayList<String>();
                 
                 int i = 0;
                 while(dbResult.next()) {
                     if(i >= min && i < max) {
                         res.add(dbResult.getString(1));
                         
                     }
                     i++;
                 }
                 
                 return res;
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
             throw new IllegalStateException("Could not retrieve the file ids for '" + pillarId + "' between counts "
                     + min + " and " + max + " with the SQL '" + selectSql + "'.", e);
         }
     }
     
     /**
      * Gets the summarized size of the files in the given collection
      * @param collectionId The ID of the collection
      * @return The accumulated size of the files in the collection 
      */
     public Long getCollectionFileSize(String collectionId) {
         long startTime = System.currentTimeMillis();
         Long collectionKey = retrieveCollectionKey(collectionId);
         log.trace("Summarizing the filesizes for the files in collection '" + collectionId + "'.");
 
         String summarizeSql = "SELECT SUM (" + FI_FILE_SIZE + ") FROM" + " ("
                 + " SELECT DISTINCT " + FI_FILE_KEY + ", " + FI_FILE_SIZE + " FROM " + FILE_INFO_TABLE + " ) AS distinctKeys"
                 + " JOIN " + FILES_TABLE 
                 + " ON " + "distinctKeys." + FI_FILE_KEY + " = " + FILES_TABLE + "." + FILES_KEY
                 + " WHERE " + FILES_TABLE + "." + COLLECTION_KEY + " = ?";
 
         Long result = DatabaseUtils.selectLongValue(dbConnector, summarizeSql, collectionKey);        
         log.debug("Summarized the filesizes in " + (System.currentTimeMillis() - startTime) + "ms");
         return result;
     }
     
     /**
      * Gets the summarized data size of the files on a given pillar
      * @param pillarID The ID of the pillar
      * @return The accumulated data size of the files on the pillar 
      */
     public Long getPillarDataSize(String pillarID) {
         long startTime = System.currentTimeMillis();
         Long pillarKey = retrievePillarKey(pillarID);
         log.trace("Summarizing the data size for the files on pillar '" + pillarID + "'.");
 
         String summarizeSql = "SELECT SUM (" + FI_FILE_SIZE + ") FROM" + " ("
                         + " SELECT DISTINCT " + FI_FILE_KEY + ", " + FI_FILE_SIZE + " FROM " + FILE_INFO_TABLE 
                         + " WHERE " + FILE_INFO_TABLE + "." + FI_PILLAR_KEY + " = ?"
                         + " ) AS distinctKeys"
                     + " JOIN " + FILES_TABLE 
                     + " ON " + "distinctKeys." + FI_FILE_KEY + " = " + FILES_TABLE + "." + FILES_KEY;
 
         Long result = DatabaseUtils.selectLongValue(dbConnector, summarizeSql, pillarKey);        
         log.debug("Summarized the data size in " + (System.currentTimeMillis() - startTime) + "ms");
         return result;
     }  
     
     /**
      * Gets the summarized size of the files on the given pillar in the given collection
      * @param collectionId The ID of the collection
      * @param pillarId The ID of the pillar
      * @return The accumulated size of the files on the pillar in the collection. 
      */
     public Long getCollectionFileSizeAtPillar(String collectionId, String pillarId) {
         long startTime = System.currentTimeMillis();
         Long collectionKey = retrieveCollectionKey(collectionId);
         Long pillarKey = retrievePillarKey(pillarId);
         log.trace("Summarizing the filesizes for the files in collection '" + collectionId + "' on pillar '" + pillarId + "'.");
         
         String summarizeSql = "SELECT SUM (" + FI_FILE_SIZE + ") FROM " + FILE_INFO_TABLE
                 + " JOIN " + FILES_TABLE 
                 + " ON " + FILE_INFO_TABLE + "." + FI_FILE_KEY + " = " + FILES_TABLE + "." + FILES_KEY
                 + " JOIN " + PILLAR_TABLE 
                 + " ON " + FILE_INFO_TABLE + "." + FI_PILLAR_KEY + " = " + PILLAR_TABLE + "." + PILLAR_KEY
                 + " WHERE " + FILES_TABLE + "." + COLLECTION_KEY + " = ?"
                 + " AND " + PILLAR_TABLE + "." + PILLAR_KEY + "= ?";
 
         Long result = DatabaseUtils.selectLongValue(dbConnector, summarizeSql, collectionKey, pillarKey);        
         log.debug("Summarized the filesizes in " + (System.currentTimeMillis() - startTime) + "ms");
         return result;
     } 
     
     /**
      * Creates and populates a new statistics entry in the database. 
      * @param collectionID The ID of the collection to make the entry for.  
      */
     public void makeStatisticsEntry(String collectionID) {
         Long collectionKey = retrieveCollectionKey(collectionID);
         Long statsID = insertNewStatisticsEntry(collectionKey);
         insertCollectionStatistics(statsID, collectionID);
         for(String pillar : collectionPillarsMap.get(collectionID)) {
             insertPillarStatistics(statsID, collectionID, pillar);
         }
     }
     
     /**
      * Retrieves the latest set of statistics for pillars in a collection
      * @param collectionID The ID of the collection 
      * @return List<PillarStat> The list of PillarStat's for the collection
      */
     public List<PillarStat> getLatestPillarStats(String collectionID) {
         final int indexPillarKey = 1;
         final int indexFileCount = 2;
         final int indexDataSize = 3;
         final int indexMissingFiles = 4;
         final int indexChecksumErrors = 5;
         final int indexStatTime = 6;
         final int indexUpdateTime = 7;
         
         Long statsKey = getLatestStatisticsKey(collectionID);
         Long collectionKey = retrieveCollectionKey(collectionID);
         if(statsKey == null) {
             log.debug("Trying to retrieve pillar stats but none exists for collectionID: '" + collectionID + "'.");
             return new ArrayList<PillarStat>();
         }
         List<PillarStat> res = new ArrayList<PillarStat>();
         
         String sql = "SELECT p." + PS_PILLAR_KEY + ", p." + PS_FILE_COUNT +  ", p." + PS_FILE_SIZE 
                     +  ", p." + PS_MISSING_FILES_COUNT + ", p." + PS_CHECKSUM_ERRORS + ", s." + STATS_TIME
                     + ", s." + STATS_LAST_UPDATE 
                 + " FROM " + PILLAR_STATS_TABLE + " p "
                 + " JOIN " + STATS_TABLE + " s" 
                 + " ON  p." + PS_STAT_KEY + " = s." + STATS_KEY
                 + " WHERE s." + STATS_COLLECTION_KEY + " = ?"
                 + " AND s." + STATS_KEY + " = ?";
         
         try {
             ResultSet dbResult = null;
             PreparedStatement ps = null;
             Connection conn = null;
             try {
                 conn = dbConnector.getConnection();
                 ps = DatabaseUtils.createPreparedStatement(conn, sql, collectionKey, statsKey);
                 dbResult = ps.executeQuery();
                 
                 while(dbResult.next()) {
                     Long pillarKey = dbResult.getLong(indexPillarKey);
                     Long fileCount = dbResult.getLong(indexFileCount);
                     Long dataSize = dbResult.getLong(indexDataSize);
                     Long missingFiles = dbResult.getLong(indexMissingFiles);
                     Long checksumErrors = dbResult.getLong(indexChecksumErrors);
                     Date statsTime = dbResult.getTimestamp(indexStatTime);
                     Date updateTime = dbResult.getTimestamp(indexUpdateTime);
                     String pillarID = retrievePillarIDFromKey(pillarKey);
                     
                     PillarStat p = new PillarStat(pillarID, collectionID, fileCount, dataSize, missingFiles, 
                             checksumErrors, statsTime, updateTime);
                     res.add(p);
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
             throw new IllegalStateException("Could not retrieve the latest PillarStat's for '" + collectionID + "' " +
             		"with the SQL '" + sql + "'.", e);
         }
         return res;   
     }
     
     /**
      * Retrieves the latest set of statistics for pillars in a collection
      * @param collectionID The ID of the collection 
      * @return List<PillarStat> The list of PillarStat's for the collection
      */
     public List<CollectionStat> getLatestCollectionStats(String collectionID, Long count) {
         final int indexFileCount = 1;
         final int indexDataSize = 2;
         final int indexChecksumErrors = 3;
         final int indexStatTime = 4;
         final int indexUpdateTime = 5;
         
 
         Long collectionKey = retrieveCollectionKey(collectionID);
 
         List<CollectionStat> res = new ArrayList<CollectionStat>();
         
         String sql = "SELECT c." + CS_FILECOUNT +  ", c." + CS_FILESIZE + ", c." + CS_CHECKSUM_ERRORS
                             + ", s." + STATS_TIME + ", s." + STATS_LAST_UPDATE 
                             + " FROM " + COLLECTION_STATS_TABLE + " c "
                             + " JOIN " + STATS_TABLE + " s" 
                             + " ON  c." + CS_STAT_KEY + " = s." + STATS_KEY
                             + " WHERE s." + STATS_COLLECTION_KEY + " = ?"
                             + " ORDER BY s." + STATS_TIME + " DESC "
                             + " FETCH FIRST ? ROWS ONLY";
  
         try {
             ResultSet dbResult = null;
             PreparedStatement ps = null;
             Connection conn = null;
             try {
                 conn = dbConnector.getConnection();
                 ps = DatabaseUtils.createPreparedStatement(conn, sql, collectionKey, count);
                 dbResult = ps.executeQuery();
                 
                 while(dbResult.next()) {
                     Long fileCount = dbResult.getLong(indexFileCount);
                     Long dataSize = dbResult.getLong(indexDataSize);
                     Long checksumErrors = dbResult.getLong(indexChecksumErrors);
                     Date statsTime = dbResult.getTimestamp(indexStatTime);
                     Date updateTime = dbResult.getTimestamp(indexUpdateTime);
                     
                     CollectionStat stat = new CollectionStat(collectionID, fileCount, dataSize, checksumErrors, 
                             statsTime, updateTime);
                     res.add(stat);
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
             throw new IllegalStateException("Could not retrieve the latest PillarStat's for '" + collectionID + "' " +
                     "with the SQL '" + sql + "' with arguments '"
                             + Arrays.asList(collectionKey, count) + "'.", e);
         }
         java.util.Collections.reverse(res);
         return res;   
     }
 
     
     /**
      * Retrieves the key for the latest statistics entry for a given collection
      * @param collectionID The ID of the collection to get the latest statistics entry for.
      * @return Long The key for the latest statistics entry for the collection 
      */
     private Long getLatestStatisticsKey(String collectionID) {
         Long collectionKey = retrieveCollectionKey(collectionID);
         String selectStatisticsIDSql = "SELECT " + STATS_KEY + " FROM " + STATS_TABLE
                 + " WHERE " + STATS_COLLECTION_KEY + " = ?"
                 + " ORDER BY " + STATS_KEY + " DESC"
                 + " FETCH FIRST ROW ONLY ";
         Long statisticsID = DatabaseUtils.selectFirstLongValue(dbConnector, selectStatisticsIDSql, collectionKey);
         return statisticsID;  
     }
 
     /**
      * Method to insert a new entry into the stats table. 
      * @param collectionKey The key of the collection to make the statistics for
      * @return Long The key for the new statistics row. 
      */
     private synchronized Long insertNewStatisticsEntry(Long collectionKey) {
         Date entryTime = new Date();
         String newStatisticsSql = "INSERT INTO " + STATS_TABLE 
                 + " ( " + STATS_TIME + ", " + STATS_LAST_UPDATE + ", " + STATS_COLLECTION_KEY + " )" 
                     + " VALUES ( ?, ?, ? )";
         DatabaseUtils.executeStatement(dbConnector, newStatisticsSql, entryTime, entryTime, collectionKey);
         
         String selectStatisticsIDSql = "SELECT " + STATS_KEY + " FROM " + STATS_TABLE
                 + " ORDER BY " + STATS_KEY + " DESC"
                 + " FETCH FIRST ROW ONLY ";
         Long statisticsID = DatabaseUtils.selectFirstLongValue(dbConnector, selectStatisticsIDSql,  new Object[0]);
         return statisticsID;
     }
     
     /**
      * Method to insert a new row with collection statistics for given a statistics key
      * @param statisticsKey The key for the statistics entry to link to. 
      * @param collectionID The ID of the collection to make statistics for.
      */
     private void insertCollectionStatistics(Long statisticsKey, String collectionID) {
         Long fileCount = getNumberOfFilesInCollection(collectionID);
         Long dataSize = getCollectionFileSize(collectionID);
         Long checksumErrors = getNumberOfChecksumErrorsIncollection(collectionID);
         String newCollectionStatSql = "INSERT INTO " + COLLECTION_STATS_TABLE
                 + " ( " + CS_STAT_KEY + ", " + CS_FILECOUNT + ", " + CS_FILESIZE + ", " + CS_CHECKSUM_ERRORS + " )"
                 + " VALUES ( ?, ?, ?, ? )";
         DatabaseUtils.executeStatement(dbConnector, newCollectionStatSql, statisticsKey, fileCount, dataSize, checksumErrors);
     }
     
     /**
      * Method to insert a new row with pillar statistics for a given statistics key
      * @param statisticsKey The key for the statistics entry to link to.
      * @param collectionID The ID of the collection that the statistics belong to.
      * @param pillarID The ID of the pillar to insert the row for. 
      */
     private void insertPillarStatistics(Long statisticsKey, String collectionID, String pillarID) {
         Long pillarKey = retrievePillarKey(pillarID);
         Long fileCount = (long) getNumberOfExistingFilesForAPillar(pillarID, collectionID);
         Long dataSize = getCollectionFileSizeAtPillar(collectionID, pillarID);
         Long missingFiles = (long) getNumberOfMissingFilesForAPillar(pillarID, collectionID);
         Long checksumErrors = (long) getNumberOfChecksumErrorsForAPillar(pillarID, collectionID);
         
         String newPillarStatSql = "INSERT INTO " + PILLAR_STATS_TABLE 
                 + " ( " + PS_STAT_KEY + ", " + PS_PILLAR_KEY + ", " + PS_FILE_COUNT + ", " + PS_FILE_SIZE 
                     + ", " + PS_MISSING_FILES_COUNT + ", " + PS_CHECKSUM_ERRORS +" )"
                 + " VALUES ( ?, ?, ?, ?, ?, ? )";
         DatabaseUtils.executeStatement(dbConnector, newPillarStatSql, statisticsKey, pillarKey, fileCount, dataSize, 
                 missingFiles, checksumErrors);
     }
     
     
     /**
      * Updates the file info for the given file at the given pillar.
      * It is always set to 'EXISTING' and updates the filesize to the provided 
      * @param pillarId The id for the pillar.
      * @param fileId The id for the file.
      * @param collectionId The id of the collection
      * @param fileSize The size of the file. 
      */
     private void updateFileInfoFileSize(String pillarId, String fileId, String collectionId, Long fileSize) {
         long startTime = System.currentTimeMillis();
         Long collectionKey = retrieveCollectionKey(collectionId);
         log.trace("Set file_size to '" + fileSize + "' for file '" + fileId + "' at pillar '" + pillarId 
                 + "' in collection '" + collectionId + "'.");
         setFileExisting(fileId, pillarId, collectionId);
         
         Long pillarKey = retrievePillarKey(pillarId);
         
         String selectFilesKeySql = "SELECT " + FILES_KEY + " FROM " + FILES_TABLE
                     + " WHERE " + FILES_ID + " = ?"
                     + " AND " + COLLECTION_KEY + " = ?";
         Long filesKey = DatabaseUtils.selectLongValue(dbConnector, selectFilesKeySql, fileId, collectionKey);
         
         String updateFileSizeSql = "UPDATE " + FILE_INFO_TABLE + " SET " + FI_FILE_SIZE + " = ?" 
                 + " WHERE " + FI_FILE_KEY + " = ?"
                 + " AND " + FI_PILLAR_KEY + " = ?";        
         
         DatabaseUtils.executeStatement(dbConnector, updateFileSizeSql, fileSize, filesKey, pillarKey);
         log.debug("Updated fileInfo filesize in " + (System.currentTimeMillis() - startTime) + "ms");
     }
     
     /**
      * Updates the file info for the given file at the given pillar.
      * It is always set to 'EXISTING' and if the timestamp is new, then it is also updated along with setting the 
      * checksum state to 'UNKNOWN'.
      * @param pillarId The id for the pillar.
      * @param fileId The id for the file.
      * @param filelistTimestamp The timestamp for when the file was latest modified.
      * @param collectionId The ID of the collection to update the timestamp in
      */
     private void updateFileInfoLastFileUpdateTimestamp(String pillarId, String fileId, Date filelistTimestamp, 
             String collectionId) {
         long startTime = System.currentTimeMillis();
         Long collectionKey = retrieveCollectionKey(collectionId);
         log.trace("Set Last_File_Update timestamp to '" + filelistTimestamp + "' for file '" + fileId
                 + "' at pillar '" + pillarId + "'.");
         setFileExisting(fileId, pillarId, collectionId);
         
         Long pillarKey = retrievePillarKey(pillarId);
         
         String selectFilesKeySql = "SELECT " + FILES_KEY + " FROM " + FILES_TABLE 
                     + " WHERE " + FILES_ID + " = ? "
                     + " AND " + COLLECTION_KEY + " = ?";
         Long filesKey = DatabaseUtils.selectLongValue(dbConnector, selectFilesKeySql, fileId, collectionKey);
         // If it is newer than current file_id_timestamp, then update it and set 'checksums' to unknown.
         String updateTimestampSql = "UPDATE " + FILE_INFO_TABLE + " SET " + FI_LAST_FILE_UPDATE + " = ?, " 
                 + FI_CHECKSUM_STATE + " = ? " 
                 + " WHERE " + FI_FILE_KEY + " = ?"  
                 + " AND " + FI_PILLAR_KEY + " = ?"
                 + " AND " + FI_LAST_FILE_UPDATE + " < ?";
 
         DatabaseUtils.executeStatement(dbConnector, updateTimestampSql, filelistTimestamp, 
                 ChecksumState.UNKNOWN.ordinal(), filesKey, pillarKey, filelistTimestamp);
         log.debug("Updated fileInfo timestamps in " + (System.currentTimeMillis() - startTime) + "ms");
     }
     
     /**
      * Updates the entry in the file info table for the given pillar and the file with the checksum data, if it has a
      * newer timestamp than the existing entry.
      * In that case the new checksum and its timestamp is inserted, and the checksum state is set to 'UNKNOWN'.
      * @param data The result of the GetChecksums operation.
      * @param pillarId The key of the pillar.
      * @param collectionId The ID of the collection to update the checksum in
      */
     private void updateFileInfoWithChecksum(ChecksumDataForChecksumSpecTYPE data, String pillarId, String collectionId) {
         long startTime = System.currentTimeMillis();
         log.trace("Updating pillar '" + pillarId + "' with checksum data '" + data + "'");
         Long collectionKey = retrieveCollectionKey(collectionId);
         setFileExisting(data.getFileID(), pillarId, collectionId);
         
         Date csTimestamp = CalendarUtils.convertFromXMLGregorianCalendar(data.getCalculationTimestamp());
         String checksum = Base16Utils.decodeBase16(data.getChecksumValue());
         
         Long pillarKey = retrievePillarKey(pillarId);
         
         String selectFilesKeySql = "SELECT " + FILES_KEY + " FROM " + FILES_TABLE 
                         + " WHERE " + FILES_ID + " = ?" 
                         + " AND " + COLLECTION_KEY + " = ?";
         Long filesKey = DatabaseUtils.selectLongValue(dbConnector, selectFilesKeySql, data.getFileID(), collectionKey);
         
         String updateSql = "UPDATE " + FILE_INFO_TABLE + " SET " + FI_LAST_CHECKSUM_UPDATE + " = ?, "
                 + FI_CHECKSUM_STATE + " = ? , " + FI_CHECKSUM + " = ?"
                 + " WHERE " + FI_FILE_KEY + " = ?" 
                 + " AND " + FI_PILLAR_KEY  + " = ?" 
                 + " AND " + FI_LAST_CHECKSUM_UPDATE + " < ?";
         
         DatabaseUtils.executeStatement(dbConnector, updateSql, csTimestamp, 
                 ChecksumState.UNKNOWN.ordinal(), checksum, filesKey, pillarKey, csTimestamp);
         log.debug("Updated fileInfo checksums in " + (System.currentTimeMillis() - startTime) + "ms");
     }
     
     /**
      * Sets the file state to 'EXISTING' for a given file at a given pillar.
      * @param fileId The id of the file.
      * @param pillarId The id of the pillar.
      * @param collectionId The ID of the collection
      */
     private void setFileExisting(String fileId, String pillarId, String collectionId) {
         log.trace("Marked file " + fileId + " as existing on pillar " + pillarId);
         Long collectionKey = retrieveCollectionKey(collectionId);
         Long pillarKey = retrievePillarKey(pillarId);
         
         String selectFilesKeySql = "SELECT " + FILES_KEY + " FROM " + FILES_TABLE 
                     + " WHERE " + FILES_ID + " = ?" 
                     + " AND " + COLLECTION_KEY + " = ?";
         
         Long filesKey = DatabaseUtils.selectLongValue(dbConnector, selectFilesKeySql, fileId, collectionKey);
         
         String updateExistenceSql = "UPDATE " + FILE_INFO_TABLE + " SET " + FI_FILE_STATE + " = ?"
                 + " WHERE " + FI_FILE_KEY + " =  ?"
                 + " AND " + FI_PILLAR_KEY + " = ?";
         
         DatabaseUtils.executeStatement(dbConnector, updateExistenceSql, FileState.EXISTING.ordinal(),
                 filesKey, pillarKey);
     }
     
     /**
      * Ensures that the entries for the file with the given id exists (both in the 'files' table and 
      * the 'file_info' table)
      * @param fileId The id of the file to ensure the existence of.
      * @param collectionId The ID of the collection
      */
     private void ensureFileIdExists(String fileId, String collectionId) {
         log.trace("Retrieving key for file '{}'.", fileId);
         String sql = "SELECT " + FILES_KEY + " FROM " + FILES_TABLE 
                 + " WHERE " + FILES_ID + " = ? " 
                 + " AND " + COLLECTION_KEY + " = ?";
         if(DatabaseUtils.selectLongValue(dbConnector, sql, fileId, retrieveCollectionKey(collectionId)) == null) {
             insertNewFileID(fileId, collectionId);
         }
     }
     
     /**
      * Retrieves the key corresponding to a given file id. If no such entry exists, then a null is returned.
      * @param fileId The id of the file to retrieve the key of.
      * @param collectionKey the Key for the collection to retrieve the fileKey from
      * @return The key of the file with the given id, or null if the key does not exist.
      */
     private Long retrieveFileKey(String fileId, Long collectionKey) {
         log.trace("Retrieving key for file '{}'.", fileId);
         String sql = "SELECT " + FILES_KEY + " FROM " + FILES_TABLE 
                 + " WHERE " + FILES_ID + " = ?" 
                 + " AND " + COLLECTION_KEY + " = ?";
         return DatabaseUtils.selectLongValue(dbConnector, sql, fileId, collectionKey);
     }
     
     /**
      * Method to retrieve the database key for the given collectionId
      * @param collectionId The ID of the collection 
      */
     private Long retrieveCollectionKey(String collectionId) {
         Long key = collectionKeyCache.get(collectionId);
         if(key == null) {
             log.trace("Retrieving key for collection '{}'.", collectionId);
             String sql = "SELECT " + COLLECTION_KEY + " FROM " + COLLECTIONS_TABLE 
                     + " WHERE " + COLLECTION_ID + "= ?";
             key = DatabaseUtils.selectLongValue(dbConnector, sql, collectionId);
             collectionKeyCache.put(collectionId, key);
         }
         return key;
         
     }
     
     /**
      * Inserts a new file id into the 'files' table in the database.
      * Also creates an entry in the 'fileinfo' table for every pillar.
      * @param fileId The id of the file to insert.
      * @param collectionId The ID of the collection
      */
     private synchronized void insertNewFileID(String fileId, String collectionId) {
         log.trace("Inserting the file '" + fileId + "' into the files table.");
         Long collectionKey = retrieveCollectionKey(collectionId);
         String fileSql = "INSERT INTO " + FILES_TABLE + " ( " 
                 + FILES_ID + ", " + FILES_CREATION_DATE + ", " + COLLECTION_KEY + " )" 
                 + " VALUES ( ?, ?, ?)";
         DatabaseUtils.executeStatement(dbConnector, fileSql, fileId, new Date(), collectionKey);
         
         Date epoch = new Date(0);
         for(String pillar : collectionPillarsMap.get(collectionId))  {
             String fileinfoSql = "INSERT INTO " + FILE_INFO_TABLE 
                     + " ( " + FI_FILE_KEY + ", " + FI_PILLAR_KEY + ", " + FI_CHECKSUM_STATE + ", " 
                         + FI_LAST_CHECKSUM_UPDATE + ", " + FI_FILE_STATE + ", " + FI_LAST_FILE_UPDATE + " )" 
                     + " VALUES ( "
                         + "(SELECT " + FILES_KEY + " FROM " + FILES_TABLE 
                             + " WHERE " + FILES_ID + " = ?"
                             + " AND " + COLLECTION_KEY + " = ? ), "
                         + "(SELECT " + PILLAR_KEY + " FROM " + PILLAR_TABLE 
                             + " WHERE " + PILLAR_ID + " = ? )," 
                         + " ?, ?, ?, ? )";
             DatabaseUtils.executeStatement(dbConnector, fileinfoSql, fileId, collectionKey, pillar, 
                     ChecksumState.UNKNOWN.ordinal(), epoch, FileState.UNKNOWN.ordinal(), epoch);
         }
     }
     
     /**
      * Retrieves the key corresponding to a given pillar id. All the pillar ids should be created initially.
      * @param pillarId The id of the pillar to retrieve the key of.
      * @return The key of the pillar with the given id. 
      * Returns a null if the pillar id is not known by the database yet.
      */
     private Long retrievePillarKey(String pillarId) {
         Long key = pillarKeyCache.get(pillarId);
         if(key == null) {
             log.trace("Retrieving the key for pillar '{}'.", pillarId);
             String sql = "SELECT " + PILLAR_KEY + " FROM " + PILLAR_TABLE + " WHERE " + PILLAR_ID + " = ?";
             key = DatabaseUtils.selectLongValue(dbConnector, sql, pillarId);    
             pillarKeyCache.put(pillarId, key);
         }
         return key;
     }
     
     /**
      * Retrieves the id of the pillar with the given key.
      * @param key The key of the pillar, whose id should be retrieved.
      * @return The id of the requested pillar.
      */
     private String retrievePillarIDFromKey(long key) {
         log.trace("Retrieving the id of the pillar with the key '{}'.", key);
         String sql = "SELECT " + PILLAR_ID + " FROM " + PILLAR_TABLE + " WHERE " + PILLAR_KEY + " = ?";
         return DatabaseUtils.selectStringValue(dbConnector, sql, key);
     }
 
     /**
      * Destroys the DB connector.
      */
     public void close() {
         dbConnector.destroy();
     }
 }
