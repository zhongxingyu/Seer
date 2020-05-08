 /*
  * Licensed to the Sakai Foundation (SF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The SF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 package org.sakaiproject.nakamura.lite.storage.jdbc;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UTFDataFormatException;
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.MessageFormat;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.apache.commons.lang.StringUtils;
 import org.sakaiproject.nakamura.api.lite.CacheHolder;
 import org.sakaiproject.nakamura.api.lite.ClientPoolException;
 import org.sakaiproject.nakamura.api.lite.DataFormatException;
 import org.sakaiproject.nakamura.api.lite.RemoveProperty;
 import org.sakaiproject.nakamura.api.lite.StorageClientException;
 import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
 import org.sakaiproject.nakamura.api.lite.StorageConstants;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
 import org.sakaiproject.nakamura.api.lite.content.Content;
 import org.sakaiproject.nakamura.api.lite.util.PreemptiveIterator;
 import org.sakaiproject.nakamura.lite.storage.spi.DirectCacheAccess;
 import org.sakaiproject.nakamura.lite.storage.spi.Disposable;
 import org.sakaiproject.nakamura.lite.storage.spi.DisposableIterator;
 import org.sakaiproject.nakamura.lite.storage.spi.Disposer;
 import org.sakaiproject.nakamura.lite.storage.spi.RowHasher;
 import org.sakaiproject.nakamura.lite.storage.spi.SparseMapRow;
 import org.sakaiproject.nakamura.lite.storage.spi.SparseRow;
 import org.sakaiproject.nakamura.lite.storage.spi.StorageClient;
 import org.sakaiproject.nakamura.lite.storage.spi.StorageClientListener;
 import org.sakaiproject.nakamura.lite.storage.spi.content.FileStreamContentHelper;
 import org.sakaiproject.nakamura.lite.storage.spi.content.StreamedContentHelper;
 import org.sakaiproject.nakamura.lite.storage.spi.types.Types;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableMap.Builder;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 public class JDBCStorageClient implements StorageClient, RowHasher, Disposer {
 
 
 
 
 
     public class SlowQueryLogger {
         // only used to define the logger.
     }
     
     private static final String INVALID_DATA_ERROR = "Data invalid for storage.";
     private static final Logger LOGGER = LoggerFactory.getLogger(JDBCStorageClient.class);
     static final Logger SQL_LOGGER = LoggerFactory.getLogger(SlowQueryLogger.class);
     private static final String SQL_VALIDATE = "validate";
     private static final String SQL_CHECKSCHEMA = "check-schema";
     private static final String SQL_NAME_PADDING = "sql-name-padding";
     private static final String SQL_MAX_NAME_LENGTH = "sql-max-name-length";
     private static final String SQL_COMMENT = "#";
     private static final String SQL_EOL = ";";
     public static final String SQL_INDEX_COLUMN_NAME_SELECT = "index-column-name-select";
     private static final String SQL_INDEX_COLUMN_NAME_INSERT = "index-column-name-insert";
     static final String SQL_DELETE_STRING_ROW = "delete-string-row";
     static final String SQL_INSERT_STRING_COLUMN = "insert-string-column";
     static final String SQL_REMOVE_STRING_COLUMN = "remove-string-column";
 
     static final String SQL_BLOCK_DELETE_ROW = "block-delete-row";
     static final String SQL_BLOCK_SELECT_ROW = "block-select-row";
     static final String SQL_BLOCK_INSERT_ROW = "block-insert-row";
     static final String SQL_BLOCK_UPDATE_ROW = "block-update-row";
 
     private static final String PROP_HASH_ALG = "rowid-hash";
     private static final String USE_BATCH_INSERTS = "use-batch-inserts";
     private static final String JDBC_SUPPORT_LEVEL = "jdbc-support-level";
     private static final String SQL_STATEMENT_SEQUENCE = "sql-statement-sequence";
     private static final String UPDATE_FIRST_SEQUENCE = "updateFirst";
     private static final Object SLOW_QUERY_THRESHOLD = "slow-query-time";
     private static final Object VERY_SLOW_QUERY_THRESHOLD = "very-slow-query-time";
     /**
      * A set of columns that are indexed to allow operations within the driver.
      */
     static final Set<String> AUTO_INDEX_COLUMNS_TYPES = ImmutableSet.of(
             "cn:_:parenthash=String",
             "au:_:parenthash=String",
             "ac:_:parenthash=String");
     static final Set<String> AUTO_INDEX_COLUMNS = ImmutableSet.of(
             "cn:_:parenthash",
             "au:_:parenthash",
             "ac:_:parenthash");
     private static final Map<String, String> COLUMN_NAME_MAPPING = ImmutableMap.of("_:parenthash","parenthash");
 
     private JDBCStorageClientPool jcbcStorageClientConnection;
     private Map<String, Object> sqlConfig;
     private boolean active;
     private StreamedContentHelper streamedContentHelper;
     private List<Disposable> toDispose = Lists.newArrayList();
     private Exception destroyed;
     private Exception passivate;
     private String rowidHash;
     private Map<String, AtomicInteger> counters = Maps.newConcurrentMap();
     private Set<String> indexColumns;
     private Indexer indexer;
     private long slowQueryThreshold;
     private long verySlowQueryThreshold;
     private Object desponseLock = new Object();
     private StorageClientListener storageClientListener;
     private boolean sqlNamePadding;
     private int maxNameLength;
 
     public JDBCStorageClient(JDBCStorageClientPool jdbcStorageClientConnectionPool,
            Map<String, Object> properties, Map<String, Object> sqlConfig, Set<String> indexColumns, Set<String> indexColumnTypes, Map<String, String> indexColumnsNames) throws SQLException,
             NoSuchAlgorithmException, StorageClientException {
         if ( jdbcStorageClientConnectionPool == null ) {
             throw new StorageClientException("Null Connection Pool, cant create Client");
         }
         if ( properties == null ) {
             throw new StorageClientException("Null Connection Properties, cant create Client");
         }
         if ( sqlConfig == null ) {
             throw new StorageClientException("Null SQL COnfiguration, cant create Client");
         }
         if ( indexColumns == null ) {
             throw new StorageClientException("Null Index Colums, cant create Client");
         }
         this.jcbcStorageClientConnection = jdbcStorageClientConnectionPool;
         streamedContentHelper = new FileStreamContentHelper(this, properties);
 
         this.sqlConfig = sqlConfig;
         this.indexColumns = indexColumns;
         rowidHash = getSql(PROP_HASH_ALG);
         if (rowidHash == null) {
             rowidHash = "MD5";
         }
         this.sqlNamePadding = Boolean.parseBoolean(StorageClientUtils.getSetting(getSql(SQL_NAME_PADDING),"false"));
         this.maxNameLength = Integer.parseInt(StorageClientUtils.getSetting(getSql(SQL_MAX_NAME_LENGTH),"50"));
         active = true;
         if ( indexColumnsNames != null ) {
             indexer = new WideColumnIndexer(this,indexColumnsNames, indexColumnTypes, sqlConfig);
         } else if ("1".equals(getSql(USE_BATCH_INSERTS))) {
             indexer = new BatchInsertIndexer(this, indexColumns, sqlConfig);
         } else {
             indexer = new NonBatchInsertIndexer(this, indexColumns, sqlConfig);
         }
         
         slowQueryThreshold = 50L;
         verySlowQueryThreshold = 100L;
         if (sqlConfig.containsKey(SLOW_QUERY_THRESHOLD)) {
             slowQueryThreshold = Long.parseLong((String)sqlConfig.get(SLOW_QUERY_THRESHOLD));
         }
         if (sqlConfig.containsKey(VERY_SLOW_QUERY_THRESHOLD)) {
             verySlowQueryThreshold = Long.parseLong((String)sqlConfig.get(VERY_SLOW_QUERY_THRESHOLD));
         }
 
     }
 
     public Map<String, Object> get(String keySpace, String columnFamily, String key)
             throws StorageClientException {
         checkActive();
         String rid = rowHash(keySpace, columnFamily, key);
         return internalGet(keySpace, columnFamily, rid, null); // gets through this route should have already consulted the cache.
     }
     Map<String, Object> internalGet(String keySpace, String columnFamily, String rid, DirectCacheAccess cachingManager) throws StorageClientException {
         if ( cachingManager != null ) {
             CacheHolder ch = cachingManager.getFromCache(rid);
             if ( ch != null ) {
                 Map<String, Object> cached = ch.get();
                 if ( cached == null ) {
                     // the cache was an empty object, we respond with empty.
                     cached = Maps.newHashMap();
                 }
                 return cached;
             }
         }
         ResultSet body = null;
         Map<String, Object> result = Maps.newHashMap();
         PreparedStatement selectStringRow = null;
         try {
             selectStringRow = getStatement(keySpace, columnFamily, SQL_BLOCK_SELECT_ROW, rid, null);
             inc("A");
             selectStringRow.clearWarnings();
             selectStringRow.clearParameters();
             selectStringRow.setString(1, rid);
             body = selectStringRow.executeQuery();
             inc("B");
             if (body.next()) {
                 Types.loadFromStream(rid, result, body.getBinaryStream(1), columnFamily);
             }
         } catch (SQLException e) {
             LOGGER.warn("Failed to perform get operation on  " + keySpace + ":" + columnFamily
                     + ":" + rid, e);
             if (passivate != null) {
                 LOGGER.warn("Was Pasivated ", passivate);
             }
             if (destroyed != null) {
                 LOGGER.warn("Was Destroyed ", destroyed);
             }
             throw new StorageClientException(e.getMessage(), e);
         } catch (IOException e) {
             LOGGER.warn("Failed to perform get operation on  " + keySpace + ":" + columnFamily
                     + ":" + rid, e);
             if (passivate != null) {
                 LOGGER.warn("Was Pasivated ", passivate);
             }
             if (destroyed != null) {
                 LOGGER.warn("Was Destroyed ", destroyed);
             }
             throw new StorageClientException(e.getMessage(), e);
         } finally {
             close(body, "B");
             close(selectStringRow, "A");
         }
         if ( cachingManager != null ) {
             cachingManager.putToCache(rid, new CacheHolder(result),true);
         }
         return result;
     }
     
     public String rowHash(String keySpace, String columnFamily, String key)
             throws StorageClientException {
         MessageDigest hasher;
         try {
             hasher = MessageDigest.getInstance(rowidHash);
         } catch (NoSuchAlgorithmException e1) {
             throw new StorageClientException("Unable to get hash algorithm " + e1.getMessage(), e1);
         }
         String keystring = keySpace + ":" + columnFamily + ":" + key;
         byte[] ridkey;
         try {
             ridkey = keystring.getBytes("UTF8");
         } catch (UnsupportedEncodingException e) {
             ridkey = keystring.getBytes();
         }
         return StorageClientUtils.encode(hasher.digest(ridkey));
     }
 
     public void insert(String keySpace, String columnFamily, String key, Map<String, Object> values, boolean probablyNew)
             throws StorageClientException {
         checkActive();
 
         Map<String, PreparedStatement> statementCache = Maps.newHashMap();
         boolean autoCommit = true;
         try {
             autoCommit = startBlock();
             String rid = rowHash(keySpace, columnFamily, key);
             for (Entry<String, Object> e : values.entrySet()) {
                 String k = e.getKey();
                 Object o = e.getValue();
                 if (o instanceof byte[]) {
                     throw new RuntimeException("Invalid content in " + k
                             + ", storing byte[] rather than streaming it");
                 }
             }
 
             Map<String, Object> m = get(keySpace, columnFamily, key);
             if ( storageClientListener != null ) {
                 storageClientListener.before(keySpace,columnFamily,key,m);
             }
             if ( TRUE.equals(m.get(DELETED_FIELD)) ) {
                 // if the map was previously deleted, delete all content since we don't want the old map becoming part of the new map.
                 m.clear();
             }
             for (Entry<String, Object> e : values.entrySet()) {
                 String k = e.getKey();
                 Object o = e.getValue();
 
                 if (o instanceof RemoveProperty || o == null) {
                     m.remove(k);
                 } else {
                     m.put(k, o);
                 }
             }
             if ( storageClientListener != null ) {
                 storageClientListener.after(keySpace,columnFamily,key,m);
             }
             LOGGER.debug("Saving {} {} {} ", new Object[]{key, rid, m});
             if ( probablyNew && !UPDATE_FIRST_SEQUENCE.equals(getSql(SQL_STATEMENT_SEQUENCE))) {
                 PreparedStatement insertBlockRow = getStatement(keySpace, columnFamily,
                         SQL_BLOCK_INSERT_ROW, rid, statementCache);
                 insertBlockRow.clearWarnings();
                 insertBlockRow.clearParameters();
                 insertBlockRow.setString(1, rid);
                 InputStream insertStream = null;
                 try {
                   insertStream = Types.storeMapToStream(rid, m, columnFamily);
                 } catch (UTFDataFormatException e) {
                   throw new DataFormatException(INVALID_DATA_ERROR, e);
                 }
               if ("1.5".equals(getSql(JDBC_SUPPORT_LEVEL))) {
                   insertBlockRow.setBinaryStream(2, insertStream, insertStream.available());
                 } else {
                   insertBlockRow.setBinaryStream(2, insertStream);
                 }
                 int rowsInserted = 0;
                 try {
                     long t1 = System.currentTimeMillis();
                     rowsInserted = insertBlockRow.executeUpdate();
                     checkSlow(t1, getSql(keySpace, columnFamily,SQL_BLOCK_INSERT_ROW));
                 } catch ( SQLException e ) {
                     LOGGER.debug(e.getMessage(),e);
                 }
                 if ( rowsInserted == 0 ) {
                     PreparedStatement updateBlockRow = getStatement(keySpace, columnFamily,
                             SQL_BLOCK_UPDATE_ROW, rid, statementCache);
                     updateBlockRow.clearWarnings();
                     updateBlockRow.clearParameters();
                     updateBlockRow.setString(2, rid);
                   try {
                     insertStream = Types.storeMapToStream(rid, m, columnFamily);
                   } catch (UTFDataFormatException e) {
                     throw new DataFormatException(INVALID_DATA_ERROR, e);
                   }
                   if ("1.5".equals(getSql(JDBC_SUPPORT_LEVEL))) {
                       updateBlockRow.setBinaryStream(1, insertStream, insertStream.available());
                     } else {
                       updateBlockRow.setBinaryStream(1, insertStream);
                     }
                     long t = System.currentTimeMillis();
                     int u = updateBlockRow.executeUpdate();
                     checkSlow(t, getSql(keySpace, columnFamily, SQL_BLOCK_UPDATE_ROW));
                     if( u == 0) {
                         throw new StorageClientException("Failed to save " + rid);
                     } else {
                         LOGGER.debug("Updated {} ", rid);
                     }
                 } else {
                     LOGGER.debug("Inserted {} ", rid);                    
                 }                
             } else {
                 PreparedStatement updateBlockRow = getStatement(keySpace, columnFamily,
                         SQL_BLOCK_UPDATE_ROW, rid, statementCache);
                 updateBlockRow.clearWarnings();
                 updateBlockRow.clearParameters();
                 updateBlockRow.setString(2, rid);
               InputStream updateStream = null;
               try {
                 updateStream = Types.storeMapToStream(rid, m, columnFamily);
               } catch (UTFDataFormatException e) {
                   throw new DataFormatException(INVALID_DATA_ERROR, e);
               }
               if ("1.5".equals(getSql(JDBC_SUPPORT_LEVEL))) {
                   updateBlockRow.setBinaryStream(1, updateStream, updateStream.available());
                 } else {
                   updateBlockRow.setBinaryStream(1, updateStream);
                 }
                 long t = System.currentTimeMillis();
                 int u = updateBlockRow.executeUpdate();
                 checkSlow(t, getSql(keySpace, columnFamily, SQL_BLOCK_UPDATE_ROW));
                 if (u == 0) {
                     PreparedStatement insertBlockRow = getStatement(keySpace, columnFamily,
                             SQL_BLOCK_INSERT_ROW, rid, statementCache);
                     insertBlockRow.clearWarnings();
                     insertBlockRow.clearParameters();
                     insertBlockRow.setString(1, rid);
                     try {
                       updateStream = Types.storeMapToStream(rid, m, columnFamily);
                     } catch (UTFDataFormatException e) {
                       throw new DataFormatException(INVALID_DATA_ERROR, e);
                     }
                     if ("1.5".equals(getSql(JDBC_SUPPORT_LEVEL))) {
                        insertBlockRow.setBinaryStream(2, updateStream, updateStream.available());
                     } else {
                        insertBlockRow.setBinaryStream(2, updateStream);
                     }
                     t = System.currentTimeMillis();
                     u = insertBlockRow.executeUpdate();
                     checkSlow(t, getSql(keySpace, columnFamily, SQL_BLOCK_INSERT_ROW));
                   
                     if (u == 0) {
                         throw new StorageClientException("Failed to save " + rid);
                     } else {
                         LOGGER.debug("Inserted {} ", rid);
                     }
                 } else {
                     LOGGER.debug("Updated {} ", rid);
                 }
             }
             
             // Indexing ---------------------------------------------------------------------------
             indexer.index(statementCache, keySpace, columnFamily, key, rid, values);
             
             endBlock(autoCommit);
         } catch (SQLException e) {
             abandonBlock(autoCommit);
             LOGGER.warn("Failed to perform insert/update operation on {}:{}:{} ", new Object[] {
                     keySpace, columnFamily, key }, e);
             throw new StorageClientException(e.getMessage(), e);
         } catch (IOException e) {
             abandonBlock(autoCommit);
             LOGGER.warn("Failed to perform insert/update operation on {}:{}:{} ", new Object[] {
                     keySpace, columnFamily, key }, e);
             throw new StorageClientException(e.getMessage(), e);
         } finally {
             closeStatementCache(statementCache);
         }
     }
 
     private void checkSlow(long t, String sql) {
         t = System.currentTimeMillis() - t;
         if ( t > 100 ) {
             SQL_LOGGER.info("Slow Query {} {} ",t, sql);
         }        
     }
 
     String getSql(String keySpace, String columnFamily, String name) {
         return getSql(new String[]{
            name+"."+keySpace+"."+columnFamily,
            name+"."+keySpace,
            name
         });
     }
 
     private void abandonBlock(boolean autoCommit) {
         if (autoCommit) {
             try {
                 Connection connection = jcbcStorageClientConnection.getConnection();
                 connection.rollback();
                 connection.setAutoCommit(autoCommit);
                 if ( storageClientListener != null ) {
                     storageClientListener.rollback();
                 }
             } catch (SQLException e) {
                 LOGGER.warn(e.getMessage(), e);
             }
         }
     }
 
     private void endBlock(boolean autoCommit) throws SQLException {
         if (autoCommit) {
             Connection connection = jcbcStorageClientConnection.getConnection();
             connection.commit();
             connection.setAutoCommit(autoCommit);
             if ( storageClientListener != null ) {
                 storageClientListener.commit();
             }
         }
     }
 
     private boolean startBlock() throws SQLException {
         Connection connection = jcbcStorageClientConnection.getConnection();
         boolean autoCommit = connection.getAutoCommit();
         connection.setAutoCommit(false);
         if ( storageClientListener != null ) {
             storageClientListener.begin();
         }
         return autoCommit;
       }
 
 
     String getDebugRowId(String keySpace, String columnFamily, String key) {
         return keySpace + ":" + columnFamily + ":" + key;
     }
 
     public void remove(String keySpace, String columnFamily, String key)
             throws StorageClientException {
         checkActive();
         PreparedStatement deleteStringRow = null;
         PreparedStatement deleteBlockRow = null;
         String rid = rowHash(keySpace, columnFamily, key);
         boolean autoCommit = false;
         try {
             autoCommit = startBlock();
             if ( storageClientListener != null ) {
                 storageClientListener.delete(keySpace, columnFamily, key);
             }
             deleteStringRow = getStatement(keySpace, columnFamily, SQL_DELETE_STRING_ROW, rid, null);
             inc("deleteStringRow");
             deleteStringRow.clearWarnings();
             deleteStringRow.clearParameters();
             deleteStringRow.setString(1, rid);
             deleteStringRow.executeUpdate();
 
             deleteBlockRow = getStatement(keySpace, columnFamily, SQL_BLOCK_DELETE_ROW, rid, null);
             inc("deleteBlockRow");
             deleteBlockRow.clearWarnings();
             deleteBlockRow.clearParameters();
             deleteBlockRow.setString(1, rid);
             deleteBlockRow.executeUpdate();
             endBlock(autoCommit);
         } catch (SQLException e) {
             abandonBlock(autoCommit);
             LOGGER.warn("Failed to perform delete operation on {}:{}:{} ", new Object[] { keySpace,
                     columnFamily, key }, e);
             throw new StorageClientException(e.getMessage(), e);
         } finally {
             close(deleteStringRow, "deleteStringRow");
             close(deleteBlockRow, "deleteBlockRow");
         }
     }
     
     public void close() {
         passivate();
         jcbcStorageClientConnection.releaseClient(this);
     }
     
     public void destroy() {
         if (destroyed == null) {
             try {
                 destroyed = new Exception("Connection Closed Traceback");
             } catch (Throwable t) {
                 LOGGER.error("Failed to dispose connection ", t);
             }
         }        
     }
 
     private void checkActive() throws StorageClientException {
      checkActive(true);
     }
 
     private void checkActive(boolean checkForActive) throws StorageClientException {
         if (destroyed != null) {
             LOGGER.warn("Using a disposed storage client ");
             throw new StorageClientException(
                     "Client was destroyed, traceback of destroy location follows ", destroyed);
         }
         if ( checkForActive ) {
             if ( passivate != null ) {
                 LOGGER.warn("Using a passive storage client");
                     throw new StorageClientException(
                             "Client has been passivated traceback of passivate location follows ", passivate);            
             }
             if ( ! active ) {
                 LOGGER.warn("Using a passive storage client, no passivate location");
                 throw new StorageClientException(
                         "Client has been passivated");            
                 
             }
         }
     }
 
     /**
      * Get a prepared statement, potentially optimized and sharded.
      * 
      * @param keySpace
      * @param columnFamily
      * @param sqlSelectStringRow
      * @param rid
      * @param statementCache
      * @return
      * @throws SQLException
      */
     PreparedStatement getStatement(String keySpace, String columnFamily,
             String sqlSelectStringRow, String rid, Map<String, PreparedStatement> statementCache)
             throws SQLException {
         String shard = rid.substring(0, 1);
         String[] keys = new String[] {
                 sqlSelectStringRow + "." + keySpace + "." + columnFamily + "._" + shard,
                 sqlSelectStringRow + "." + columnFamily + "._" + shard,
                 sqlSelectStringRow + "." + keySpace + "._" + shard,
                 sqlSelectStringRow + "._" + shard,
                 sqlSelectStringRow + "." + keySpace + "." + columnFamily,
                 sqlSelectStringRow + "." + columnFamily, sqlSelectStringRow + "." + keySpace,
                 sqlSelectStringRow };
         for (String k : keys) {
             if (sqlConfig.containsKey(k)) {
                 LOGGER.debug("Using Statement {} ",sqlConfig.get(k));
                 if (statementCache != null && statementCache.containsKey(k)) {
                     return statementCache.get(k);
                 } else {
                     
                     PreparedStatement pst = jcbcStorageClientConnection.getConnection()
                             .prepareStatement((String) sqlConfig.get(k));
                     if (statementCache != null) {
                         inc("cachedStatement");
                         statementCache.put(k, pst);
                     }
                     return pst;
                 }
             }
         }
         return null;
     }
     
     PreparedStatement getStatement(String sql,   Map<String, PreparedStatement> statementCache) throws SQLException {
         PreparedStatement pst = null;
         if ( statementCache != null ) {
             if ( statementCache.containsKey(sql)) {
                 pst =  statementCache.get(sql);
             } else {
                 pst = jcbcStorageClientConnection.getConnection().prepareStatement(sql);
                 inc("cachedStatement");
                 statementCache.put(sql, pst);
             }
         } else {
             pst = jcbcStorageClientConnection.getConnection().prepareStatement(sql);            
         }
         return pst;
     }
 
 
     private void disposeDisposables() {
         List<Disposable> dList = null;
         // this shoud not be necessary, but just in case.
         synchronized (desponseLock ) {
             dList = toDispose;
             toDispose = Lists.newArrayList();            
         }
         for (Disposable d : dList) {
             d.close();
         }
         dList.clear();
     }
     
     public void unregisterDisposable(Disposable disposable) {
         synchronized (desponseLock) {
             toDispose.remove(disposable);
         }
     }
 
     public <T extends Disposable> T registerDisposable(T disposable) {
         // this should not be necessary, but just in case some one is sharing the client between threads.
         synchronized (desponseLock) {
             toDispose.add(disposable);
             disposable.setDisposer(this);
         }
         return disposable;
     }
 
     public boolean validate() throws StorageClientException {
         checkActive(false);
         Statement statement = null;
         try {
             statement = jcbcStorageClientConnection.getConnection().createStatement();
             inc("vaidate");
 
             statement.execute(getSql(SQL_VALIDATE));
             return true;
         } catch (SQLException e) {
             LOGGER.warn("Failed to validate connection ", e);
             return false;
         } finally {
             try {
                 statement.close();
                 dec("vaidate");
             } catch (Throwable e) {
                 LOGGER.debug("Failed to close statement in validate ", e);
             }
         }
     }
     
     String getSql(String[] keys) {
         for (String statementKey : keys) {
             String sql = getSql(statementKey);
             if (sql != null) {
                 return sql;
             }
         }
         return null;
     }
 
     private String getSql(String statementName) {
         return (String) sqlConfig.get(statementName);
     }
 
     public void checkSchema(String[] clientConfigLocations) throws ClientPoolException,
             StorageClientException {
         checkActive();
         Statement statement = null;
         try {
 
             statement = jcbcStorageClientConnection.getConnection().createStatement();
             try {
                 statement.execute(getSql(SQL_CHECKSCHEMA));
                 inc("schema");
                 LOGGER.info("Schema Exists");
                 return;
             } catch (SQLException e) {
                 LOGGER.info("Schema does not exist {}", e.getMessage());
             }
 
             for (String clientSQLLocation : clientConfigLocations) {
                 String clientDDL = clientSQLLocation + ".ddl";
                 InputStream in = this.getClass().getClassLoader().getResourceAsStream(clientDDL);
                 if (in != null) {
                     try {
                         BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF8"));
                         int lineNo = 1;
                         String line = br.readLine();
                         StringBuilder sqlStatement = new StringBuilder();
                         while (line != null) {
                             line = StringUtils.stripEnd(line, null);
                             if (!line.isEmpty()) {
                                 if (line.startsWith(SQL_COMMENT)) {
                                     LOGGER.info("Comment {} ", line);
                                 } else if (line.endsWith(SQL_EOL)) {
                                     sqlStatement.append(line.substring(0, line.length() - 1));
                                     String ddl = sqlStatement.toString();
                                     try {
                                         statement.executeUpdate(ddl);
                                         LOGGER.info("SQL OK    {}:{} {} ", new Object[] {
                                                 clientDDL, lineNo, ddl });
                                     } catch (SQLException e) {
                                         LOGGER.warn("SQL ERROR {}:{} {} {} ", new Object[] {
                                                 clientDDL, lineNo, ddl, e.getMessage() });
                                     }
                                     sqlStatement = new StringBuilder();
                                 } else {
                                     sqlStatement.append(line);
                                 }
                             }
                             line = br.readLine();
                             lineNo++;
                         }
                         br.close();
                         LOGGER.info("Schema Created from {} ", clientDDL);
 
                         break;
                     } catch (Throwable e) {
                         LOGGER.error("Failed to load Schema from {}", clientDDL, e);
                     } finally {
                         try {
                             in.close();
                         } catch (IOException e) {
                             LOGGER.error("Failed to close stream from {}", clientDDL, e);
                         }
 
                     }
                 } else {
                     LOGGER.info("No Schema found at {} ", clientDDL);
                 }
 
             }
 
         } catch (SQLException e) {
             LOGGER.info("Failed to create schema ", e);
             throw new ClientPoolException("Failed to create schema ", e);
         } finally {
             try {
                 statement.close();
                 dec("schema");
             } catch (Throwable e) {
                 LOGGER.debug("Failed to close statement in validate ", e);
             }
         }
         
         
     }
 
     public void activate() {
         passivate = null;
         active = true;
     }
 
     public void passivate() {
         if (active) {
             passivate = new Exception("Passivate Traceback");
             disposeDisposables();
             active = false;
         }
     }
 
     public Map<String, Object> streamBodyIn(String keySpace, String columnFamily, String contentId,
             String contentBlockId, String streamId, Map<String, Object> content, InputStream in)
             throws StorageClientException, AccessDeniedException, IOException {
         checkActive();
         return streamedContentHelper.writeBody(keySpace, columnFamily, contentId, contentBlockId,
                 streamId, content, in);
     }
 
     public InputStream streamBodyOut(String keySpace, String columnFamily, String contentId,
             String contentBlockId, String streamId, Map<String, Object> content)
             throws StorageClientException, AccessDeniedException, IOException {
         checkActive();
         final InputStream in = streamedContentHelper.readBody(keySpace, columnFamily,
                 contentBlockId, streamId, content);
         if ( in != null ) {
             registerDisposable(new StreamDisposable(in));
         }
         return in;
     }
 
     public boolean hasBody(Map<String, Object> content, String streamId) {
         return streamedContentHelper.hasStream(content, streamId);
     }
 
     protected Connection getConnection() throws StorageClientException, SQLException {
         checkActive();
         return jcbcStorageClientConnection.getConnection();
     }
 
     public DisposableIterator<Map<String, Object>> listChildren(String keySpace, String columnFamily, String key, DirectCacheAccess cachingManager) throws StorageClientException {
         // this will load all child object directly.
         String hash = rowHash(keySpace, columnFamily, key);
         LOGGER.debug("Finding {}:{}:{} as {} ",new Object[]{keySpace,columnFamily, key, hash});
         return find(keySpace, columnFamily, ImmutableMap.of(Content.PARENT_HASH_FIELD, (Object)hash, StorageConstants.CUSTOM_STATEMENT_SET, "listchildren"), cachingManager);
     }
 
     public DisposableIterator<Map<String,Object>> find(final String keySpace, final String columnFamily,
             Map<String, Object> properties, DirectCacheAccess cachingManager) throws StorageClientException {
         checkActive();
         return indexer.find(keySpace, columnFamily, properties, cachingManager);
         
 
     }
 
     
     public DisposableIterator<SparseRow> listAll(String keySpace, final String columnFamily) throws StorageClientException {
         String[] keys = new String[] { "list-all." + keySpace + "." + columnFamily,
                 "list-all." + columnFamily, "list-all" };     
         String sql = null;
         for (String statementKey : keys) {
             sql = getSql(statementKey);
             if (sql != null) {
                 break;
             }
         }
         if ( sql == null ) {
             throw new StorageClientException("Cant find sql statement for one of "+Arrays.toString(keys));
         }
         PreparedStatement tpst = null;
         ResultSet trs = null;
         try {
             LOGGER.debug("Preparing {} ", sql);
             tpst = jcbcStorageClientConnection.getConnection().prepareStatement(sql);
             inc("iterator");
             tpst.clearParameters();
 
             long qtime = System.currentTimeMillis();
             trs = tpst.executeQuery();
             qtime = System.currentTimeMillis() - qtime;
             if ( qtime > slowQueryThreshold && qtime < verySlowQueryThreshold) {
                 SQL_LOGGER.warn("Slow Query {}ms {} params:[{}]",new Object[]{qtime,sql});
             } else if ( qtime > verySlowQueryThreshold ) {
                 SQL_LOGGER.error("Very Slow Query {}ms {} params:[{}]",new Object[]{qtime,sql});
             }
             inc("iterator r");
             LOGGER.debug("Executed ");
 
             // pass control to the iterator.
             final PreparedStatement pst = tpst;
             final ResultSet rs = trs;
             tpst = null;
             trs = null;
             return registerDisposable(new PreemptiveIterator<SparseRow>() {
 
                 private SparseRow nextValue = null;
                 private boolean open = true;
 
                 @Override
                 protected SparseRow internalNext() {
                     return nextValue;
                 }
 
                 @Override
                 protected boolean internalHasNext() {
                     try {
                         while (open && rs.next()) {
                             try {
                                 Map<String, Object> values = Maps.newHashMap();
                                 String rid = rs.getString(1);
                                 Types.loadFromStream(rid, values, rs.getBinaryStream(2), columnFamily);
                                 nextValue = new SparseMapRow(rid,values);
                                 return true;
                             } catch (IOException e) {
                                 LOGGER.error(e.getMessage(),e);
                                 nextValue = null;
                             }
                         }
                         close();
                         nextValue = null;
                         LOGGER.debug("End of Set ");
                         return false;
                     } catch (SQLException e) {
                         LOGGER.error(e.getMessage(), e);
                         close();
                         nextValue = null;
                         return false;
                     }
                 }
 
                 @Override
                 public void close() {
                     if (open) {
                         open = false;
                         try {
                             if (rs != null) {
                                 rs.close();
                                 dec("iterator r");
                             }
                         } catch (SQLException e) {
                             LOGGER.warn(e.getMessage(), e);
                         }
                         try {
                             if (pst != null) {
                                 pst.close();
                                 dec("iterator");
                             }
                         } catch (SQLException e) {
                             LOGGER.warn(e.getMessage(), e);
                         }
                         super.close();
                     }
 
                 }
             });
         } catch (SQLException e) {
             LOGGER.error(e.getMessage(), e);
             throw new StorageClientException(e.getMessage() + " SQL Statement was " + sql,
                     e);
         } finally {
             // trs and tpst will only be non null if control has not been passed
             // to the iterator.
             try {
                 if (trs != null) {
                     trs.close();
                     dec("iterator r");
                 }
             } catch (SQLException e) {
                 LOGGER.warn(e.getMessage(), e);
             }
             try {
                 if (tpst != null) {
                     tpst.close();
                     dec("iterator");
                 }
             } catch (SQLException e) {
                 LOGGER.warn(e.getMessage(), e);
             }
         }
 
    }
 
     void dec(String key) {
         AtomicInteger cn = counters.get(key);
         if (cn == null) {
             LOGGER.warn("Never Statement/ResultSet Created Counter {} ", key);
         } else {
             cn.decrementAndGet();
         }
     }
 
     void inc(String key) {
         AtomicInteger cn = counters.get(key);
         if (cn == null) {
             cn = new AtomicInteger();
             counters.put(key, cn);
         }
         int c = cn.incrementAndGet();
         if (c > 10) {
             LOGGER.warn(
                     "Counter {} Leaking {}, please investigate. This will eventually cause an OOM Error. ",
                     key, c);
         }
     }
 
     private void close(ResultSet rs, String name) {
         try {
             if (rs != null) {
                 rs.close();
                 dec(name);
             }
         } catch (Throwable e) {
             LOGGER.debug("Failed to close result set, ok to ignore this message ", e);
         }
     }
 
     private void close(PreparedStatement pst, String name) {
         try {
             if (pst != null) {
                 pst.close();
                 dec(name);
             }
         } catch (Throwable e) {
             LOGGER.debug("Failed to close prepared set, ok to ignore this message ", e);
         }
     }
 
     public void closeStatementCache(Map<String, PreparedStatement> statementCache) {
         for (PreparedStatement pst : statementCache.values()) {
             if (pst != null) {
                 try {
                     pst.close();
                     dec("cachedStatement");
                 } catch (SQLException e) {
                     LOGGER.debug(e.getMessage(), e);
                 }
             }
         }
     }
 
     public Map<String, String> syncIndexColumns() throws StorageClientException, SQLException {
         checkActive();
         String selectColumns = getSql(SQL_INDEX_COLUMN_NAME_SELECT);
         String insertColumns = getSql(SQL_INDEX_COLUMN_NAME_INSERT);
         String updateTable = getSql("alter-widestring-table");
         String updateIndexes = getSql("index-widestring-table");
         if ( selectColumns == null || insertColumns == null ) {
             LOGGER.warn("Using Key Value Pair Tables for indexing ");
             LOGGER.warn("     This will cause scalability problems eventually, please see KERN-1957 ");
             LOGGER.warn("     To fix, port your SQL Configuration file to use a wide index table. ");
             return null; // no wide column support in this JDBC config.
         }
         PreparedStatement selectColumnsPst = null;
         PreparedStatement insertColumnsPst = null;
         ResultSet rs = null;
         Connection connection = jcbcStorageClientConnection.getConnection();
         Statement statement = null;
         try {
             selectColumnsPst = connection.prepareStatement(selectColumns);
             insertColumnsPst = connection.prepareStatement(insertColumns);
             statement = connection.createStatement();
             rs = selectColumnsPst.executeQuery();
             Map<String, String> cnames = Maps.newHashMap();  
             Set<String> usedColumns = Sets.newHashSet();  
             while(rs.next()) {
                 String columnFamily = rs.getString(1);
                 String column = rs.getString(2);
                 String columnName = rs.getString(3);
                 cnames.put(columnFamily+":"+column, columnName);
                 usedColumns.add(columnFamily+":"+columnName);
             }
             // maxCols contiains the max col number for each cf.
             // cnames contains a map of column Families each containing a map of columns with numbers.
             for (String k : Sets.union(indexColumns, AUTO_INDEX_COLUMNS)) {
                 String[] cf = StringUtils.split(k,":",2);
                 if ( !cnames.containsKey(k) ) {
                     String cv = makeNameSafeSQL(cf[1], sqlNamePadding, maxNameLength);
                     if ( usedColumns.contains(cf[0]+":"+cv)) {
                         LOGGER.info(
                                 "Column already exists, please provide explicit mapping indexing {}  already used column {} ",
                                 k, cv);
                         throw new StorageClientException(
                                 "Column already exists, please provide explicit mapping indexing ["
                                         + k + "]  already used column [" + cv + "]");
                     }
                     insertColumnsPst.clearParameters();
                     insertColumnsPst.setString(1, cf[0]);
                     insertColumnsPst.setString(2, cf[1]);
                     insertColumnsPst.setString(3, cv);
                     insertColumnsPst.executeUpdate();
                     cnames.put(k, cv);
                     usedColumns.add(cf[0]+":"+cv);
                     try {
                         statement.executeUpdate(MessageFormat.format(updateTable, cf[0], cv));
                         LOGGER.info("Added Index Column OK    {}   Table:{} Column:{} ",
                                 new Object[] { k, cf[0], cv });
                     } catch (SQLException e) {
                         LOGGER.warn(
                                 "Added Index Column Error    {}   Table:{} Column:{} Cause:{} ",
                                 new Object[] { k, cf[0], cv, e.getMessage() });
                         LOGGER.warn("SQL is {} ",MessageFormat.format(updateTable, cf[0], cv));
                         throw new StorageClientException(e.getMessage(),e);
                     }
                     try {
                         statement.executeUpdate(MessageFormat.format(updateIndexes, cf[0], cv));
                         LOGGER.info("Added Index Column OK    {}   Table:{} Column:{} ",
                                 new Object[] { k, cf[0], cv });
                     } catch (SQLException e) {
                         LOGGER.warn(
                                 "Added Index Column Error    {}   Table:{} Column:{} Cause:{} ",
                                 new Object[] { k, cf[0], cv, e.getMessage() });
                         LOGGER.warn("SQL is {} ",MessageFormat.format(updateIndexes, cf[0], cv));
                         throw new StorageClientException(e.getMessage(),e);
                     }
                 }
             }
             // sync done, now create a quick lookup table to extract the storage column for any column name, 
             Builder<String, String> b = ImmutableMap.builder();
             for (Entry<String,String> e : cnames.entrySet()) {
                 b.put(e.getKey(), e.getValue());
                 LOGGER.info("Column Config {} maps to {} ",e.getKey(), e.getValue());
             }
             
             
             
             return b.build();
         } finally {
             if ( rs != null ) {
                 try {
                     rs.close();
                 } catch ( SQLException e ) {
                     LOGGER.debug(e.getMessage(),e);
                 }
             }
             if ( selectColumnsPst != null ) {
                 try {
                     selectColumnsPst.close();
                 } catch ( SQLException e ) {
                     LOGGER.debug(e.getMessage(),e);
                 }
             }
             if ( insertColumnsPst != null ) {
                 try {
                     insertColumnsPst.close();
                 } catch ( SQLException e ) {
                     LOGGER.debug(e.getMessage(),e);
                 }
             }
             if ( statement != null ) {
                 try {
                     statement.close();
                 } catch ( SQLException e ) {
                     LOGGER.debug(e.getMessage(),e);
                 }
             }
         }
     }
 
     private String makeNameSafeSQL(String name, boolean padding, int maxLength) {
         if ( COLUMN_NAME_MAPPING.containsKey(name)) {
             return COLUMN_NAME_MAPPING.get(name);
         }
         char[] c = name.toCharArray();
         char[] cout = new char[c.length];
         int e = 0;
         int start = 0;
         if ( c[0] == '_') {
             if ( padding ) {
                 cout[e] = 'X';
                 e++;
             }
             start = 1;
         } 
         for(int i = start; i < c.length; i++) {
             if ( !Character.isLetterOrDigit(c[i]) ) {
                 if ( !padding ) {
                     cout[e] = '_';
                     e++;
                 }
             } else {
                 cout[e] = c[i];
                 e++;
             }
         }
         return new String(cout,0,Math.min(e, maxLength));
     }
 
     public long getSlowQueryThreshold() {
         return slowQueryThreshold;
     }
 
     public long getVerySlowQueryThreshold() {
         return verySlowQueryThreshold;
     }
 
     public Indexer getIndexer() {
         return indexer;
     }
 
     public long allCount(String keySpace, String columnFamily) throws StorageClientException {
         
         String[] keys = new String[] { "list-all-count." + keySpace + "." + columnFamily,
                 "list-all-count." + columnFamily, "list-all-count" };     
         String sql = null;
         for (String statementKey : keys) {
             sql = getSql(statementKey);
             if (sql != null) {
                 break;
             }
         }
         if ( sql == null ) {
             throw new StorageClientException("Cant find sql statement for one of "+Arrays.toString(keys));
         }
         PreparedStatement tpst = null;
         ResultSet trs = null;
         try {
             LOGGER.debug("Preparing {} ", sql);
             tpst = jcbcStorageClientConnection.getConnection().prepareStatement(sql);
             inc("iterator");
             tpst.clearParameters();
 
             long qtime = System.currentTimeMillis();
             trs = tpst.executeQuery();
             qtime = System.currentTimeMillis() - qtime;
             if ( qtime > slowQueryThreshold && qtime < verySlowQueryThreshold) {
                 SQL_LOGGER.warn("Slow Query {}ms {} params:[{}]",new Object[]{qtime,sql});
             } else if ( qtime > verySlowQueryThreshold ) {
                 SQL_LOGGER.error("Very Slow Query {}ms {} params:[{}]",new Object[]{qtime,sql});
             }
             inc("iterator r");
             LOGGER.debug("Executed ");
             if ( trs.next() ) {
                 return trs.getLong(1);
             } 
             return 0;
         } catch (SQLException e) {
             LOGGER.error(e.getMessage(), e);
             throw new StorageClientException(e.getMessage() + " SQL Statement was " + sql,
                     e);
         } finally {
             try {
                 if (trs != null) {
                     trs.close();
                     dec("iterator r");
                 }
             } catch (SQLException e) {
                 LOGGER.warn(e.getMessage(), e);
             }
             try {
                 if (tpst != null) {
                     tpst.close();
                     dec("iterator");
                 }
             } catch (SQLException e) {
                 LOGGER.warn(e.getMessage(), e);
             }
         }
     }
     
     
     public void setStorageClientListener(StorageClientListener storageClientListener) {
         this.storageClientListener = storageClientListener;
     }
 }
