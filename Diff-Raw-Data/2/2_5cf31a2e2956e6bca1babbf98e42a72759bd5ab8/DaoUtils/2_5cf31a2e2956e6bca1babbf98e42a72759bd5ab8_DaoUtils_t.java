 /*****************************************************************************
  * 
  * Copyright (C) Zenoss, Inc. 2010, all rights reserved.
  * 
  * This content is made available according to terms specified in
  * License.zenoss under the directory where your Zenoss product is installed.
  * 
  ****************************************************************************/
 
 
 package org.zenoss.zep.dao.impl;
 
 import com.google.protobuf.Message;
 import org.apache.tomcat.jdbc.pool.DataSource;
 import org.apache.tomcat.jdbc.pool.PoolProperties;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.dao.DataAccessException;
 import org.springframework.dao.DeadlockLoserDataAccessException;
 import org.springframework.dao.DuplicateKeyException;
 import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
 import org.springframework.jdbc.support.DatabaseMetaDataCallback;
 import org.springframework.jdbc.support.JdbcUtils;
 import org.springframework.jdbc.support.MetaDataAccessException;
 import org.zenoss.protobufs.JsonFormat;
 import org.zenoss.zep.ZepInstance;
 import org.zenoss.zep.ZepUtils;
 import org.zenoss.zep.dao.impl.compat.DatabaseCompatibility;
 import org.zenoss.zep.dao.impl.compat.DatabaseCompatibilityMySQL;
 import org.zenoss.zep.dao.impl.compat.DatabaseCompatibilityPostgreSQL;
 import org.zenoss.zep.dao.impl.compat.NestedTransactionCallback;
 import org.zenoss.zep.dao.impl.compat.NestedTransactionContext;
 import org.zenoss.zep.dao.impl.compat.NestedTransactionService;
 
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.DatabaseMetaData;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.concurrent.Callable;
 
 public final class DaoUtils {
     private static final Logger logger = LoggerFactory.getLogger(DaoUtils.class.getName());
     
     private static final String MYSQL_PROTOCOL = "mysql";
     private static final String POSTGRESQL_PROTOCOL = "postgresql";
     private static int NUM_DEADLOCK_RETRIES = 5;
     private DaoUtils() {
     }
 
     private static int getIntProperty(String value, int defaultValue) {
         Integer val = getIntProperty(value);
         return (val == null) ? defaultValue : val;
     }
 
     private static Integer getIntProperty(String value) {
         Integer intVal = null;
         if (value != null) {
             try {
                 intVal = Integer.parseInt(value.trim());
             } catch (NumberFormatException e) {
                 logger.warn("Invalid value for property: {}", value);
             }
         }
         return intVal;
     }
 
     private static Boolean getBoolProperty(String value) {
         Boolean boolVal = null;
         if (value != null) {
             boolVal = Boolean.valueOf(value.trim());
         }
         return boolVal;
     }
 
     private static final String POOL_PREFIX = "zep.jdbc.pool.";
 
     private static PoolProperties getPoolProperties(Properties globalConf, ZepInstance zepInstance) {
         final PoolProperties p = new PoolProperties();
 
         final Map<String,String> zepConfig = zepInstance.getConfig();
         final String protocol = globalConf.getProperty("zep-db-type", zepConfig.get("zep.jdbc.protocol"));
         final String hostname = globalConf.getProperty("zep-host", zepConfig.get("zep.jdbc.hostname"));
         final String port = globalConf.getProperty("zep-port", zepConfig.get("zep.jdbc.port"));
         final String dbname = globalConf.getProperty("zep-db", zepConfig.get("zep.jdbc.dbname"));
         p.setUrl(String.format("jdbc:%s://%s:%s/%s", protocol, hostname, port, dbname));
         p.setUsername(globalConf.getProperty("zep-user", zepConfig.get("zep.jdbc.username")));
         p.setPassword(globalConf.getProperty("zep-password", zepConfig.get("zep.jdbc.password")));
 
         final String connectionProperties = zepConfig.get(POOL_PREFIX + "connection_properties");
         if (connectionProperties != null) {
             p.setConnectionProperties(connectionProperties);
         }
         if (MYSQL_PROTOCOL.equals(protocol)) {
             p.setDriverClassName("com.mysql.jdbc.Driver");
             // MySQL has default connection properties if not overridden from configuration file
             if (connectionProperties == null) {
                 p.setConnectionProperties("characterEncoding=UTF-8;rewriteBatchedStatements=true;");
             }
         }
         else if (POSTGRESQL_PROTOCOL.equals(protocol)) {
             p.setDriverClassName("org.postgresql.Driver");
         }
         else {
             throw new RuntimeException("Unsupported database protocol: " + protocol);
         }
 
         p.setValidationQuery("SELECT 1");
         p.setDefaultAutoCommit(getBoolProperty(zepConfig.get(POOL_PREFIX + "default_auto_commit")));
         p.setDefaultReadOnly(getBoolProperty(zepConfig.get(POOL_PREFIX + "default_read_only")));
         p.setDefaultTransactionIsolation(getIntProperty(zepConfig.get(POOL_PREFIX + "default_transaction_isolation")));
         p.setMaxActive(getIntProperty(zepConfig.get(POOL_PREFIX + "max_active")));
         p.setMaxIdle(getIntProperty(zepConfig.get(POOL_PREFIX + "max_idle")));
         p.setMinIdle(getIntProperty(zepConfig.get(POOL_PREFIX + "min_idle")));
         p.setInitialSize(getIntProperty(zepConfig.get(POOL_PREFIX + "initial_size")));
         p.setMaxWait(getIntProperty(zepConfig.get(POOL_PREFIX + "max_wait")));
         p.setTestOnBorrow(getBoolProperty(zepConfig.get(POOL_PREFIX + "test_on_borrow")));
         p.setTestOnReturn(getBoolProperty(zepConfig.get(POOL_PREFIX + "test_on_return")));
         p.setTestWhileIdle(getBoolProperty(zepConfig.get(POOL_PREFIX + "test_while_idle")));
         p.setTimeBetweenEvictionRunsMillis(
                 getIntProperty(zepConfig.get(POOL_PREFIX + "time_between_eviction_runs_millis")));
         p.setMinEvictableIdleTimeMillis(getIntProperty(zepConfig.get(POOL_PREFIX + "min_evictable_idle_time_millis")));
         p.setJdbcInterceptors(zepConfig.get(POOL_PREFIX + "jdbc_interceptors"));
         p.setValidationInterval(getIntProperty(zepConfig.get(POOL_PREFIX + "validation_interval")));
         p.setJmxEnabled(getBoolProperty(zepConfig.get(POOL_PREFIX + "jmx_enabled")));
         p.setFairQueue(getBoolProperty(zepConfig.get(POOL_PREFIX + "fair_queue")));
         p.setAbandonWhenPercentageFull(getIntProperty(zepConfig.get(POOL_PREFIX + "abandon_when_percentage_full")));
         p.setMaxAge(getIntProperty(zepConfig.get(POOL_PREFIX + "max_age")));
         p.setUseEquals(getBoolProperty(zepConfig.get(POOL_PREFIX + "use_equals")));
         p.setRemoveAbandoned(getBoolProperty(zepConfig.get(POOL_PREFIX + "remove_abandoned")));
         p.setRemoveAbandonedTimeout(getIntProperty(zepConfig.get(POOL_PREFIX + "remove_abandoned_timeout")));
         p.setLogAbandoned(getBoolProperty(zepConfig.get(POOL_PREFIX + "log_abandoned")));
         p.setSuspectTimeout(getIntProperty(zepConfig.get(POOL_PREFIX + "suspect_timeout")));
 
         // Set the database deadlock retry count
         DaoUtils.NUM_DEADLOCK_RETRIES = getIntProperty(zepConfig.get("zep.jdbc.deadlock_retries"), 5);
 
        logger.debug("Connection pool properties: {}", p);

         return p;
     }
 
     public static DataSource createDataSource(Properties globalConf, ZepInstance zepInstance) {
         final DataSource ds = new DataSource();
         ds.setPoolProperties(getPoolProperties(globalConf, zepInstance));
         return ds;
     }
 
     public static DatabaseCompatibility createDatabaseCompatibility(Properties globalConf, ZepInstance zepInstance) {
         final Map<String,String> zepConfig = zepInstance.getConfig();
         final String protocol = globalConf.getProperty("zep-db-type", zepConfig.get("zep.jdbc.protocol"));
         if (MYSQL_PROTOCOL.equals(protocol)) {
             return new DatabaseCompatibilityMySQL();
         }
         else if (POSTGRESQL_PROTOCOL.equals(protocol)) {
             return new DatabaseCompatibilityPostgreSQL();
         }
         throw new RuntimeException("Unsupported database protocol: " + protocol);
     }
 
     /**
      * Calculate a SHA-1 hash from the specified string.
      * 
      * @param str
      *            String to hash.
      * @return SHA-1 hash for string.
      */
     public static byte[] sha1(String str) {
         try {
             MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
             return sha1.digest(str.getBytes(Charset.forName("UTF-8")));
         } catch (NoSuchAlgorithmException e) {
             throw new RuntimeException("Must support SHA-1", e);
         }
     }
 
     /**
      * Truncates the specified string to fit in the specified maximum number of
      * UTF-8 bytes. This method will not split strings in the middle of
      * surrogate pairs.
      * 
      * @param original
      *            The original string.
      * @param maxBytes
      *            The maximum number of UTF-8 bytes available to store the
      *            string.
      * @return If the string doesn't overflow the number of specified bytes,
      *         then the original string is returned, otherwise the string is
      *         truncated to the number of bytes available to encode
      */
     public static String truncateStringToUtf8(final String original,
             final int maxBytes) {
         final int length = original.length();
         int newLength = 0;
         int currentBytes = 0;
         while (newLength < length) {
             final char c = original.charAt(newLength);
             boolean isSurrogate = false;
             if (c <= 0x7f) {
                 ++currentBytes;
             } else if (c <= 0x7FF) {
                 currentBytes += 2;
             } else if (c <= Character.MAX_HIGH_SURROGATE) {
                 currentBytes += 4;
                 isSurrogate = true;
             } else if (c <= 0xFFFF) {
                 currentBytes += 3;
             } else {
                 currentBytes += 4;
             }
             if (currentBytes > maxBytes) {
                 break;
             }
             if (isSurrogate) {
                 newLength += 2;
             } else {
                 ++newLength;
             }
         }
         return (newLength == length) ? original : original.substring(0,
                 newLength);
     }
 
     /**
      * Create an insert SQL string for the table with the specified insert columns.
      *
      * @param tableName Table name.
      * @param columnNames Column names for insert.
      * @return An insert SQL statement with the names (suitable for passing to Spring named
      *         parameter template).
      */
     public static String createNamedInsert(String tableName, Collection<String> columnNames) {
         StringBuilder names = new StringBuilder();
         StringBuilder values = new StringBuilder();
         Iterator<String> it = columnNames.iterator();
         while (it.hasNext()) {
             final String columnName = it.next();
             names.append(columnName);
             values.append(':').append(columnName);
             if (it.hasNext()) {
                 names.append(',');
                 values.append(',');
             }
         }
         return "INSERT INTO " + tableName + " (" + names + ") VALUES (" + values + ")";
     }
 
     /**
      * Returns a list of column names in the specified table.
      *
      * @param dataSource DataSource to use.
      * @param tableName Table name.
      * @return A list of column names in the table.
      * @throws MetaDataAccessException If an exception occurs.
      */
     public static List<String> getColumnNames(final javax.sql.DataSource dataSource, final String tableName)
             throws MetaDataAccessException {
         final List<String> columnNames = new ArrayList<String>();
         JdbcUtils.extractDatabaseMetaData(dataSource, new DatabaseMetaDataCallback() {
             @Override
             public Object processMetaData(DatabaseMetaData dbmd) throws SQLException, MetaDataAccessException {
                 ResultSet rs = dbmd.getColumns(null, null, tableName, null);
                 while (rs.next()) {
                     String columnName = rs.getString("COLUMN_NAME");
                     columnNames.add(columnName);
                 }
                 rs.close();
                 return null;
             }
         });
         return columnNames;
     }
 
     /**
      * Returns a map of column names to their JDBC type in the specified table. The map is returned in the order
      * returned by the getColumns query.
      *
      * @param dataSource DataSource to use.
      * @param tableName Table name.
      * @return A map of column names to the column types in the specified table.
      * @throws MetaDataAccessException If an exception occurs.
      */
     public static Map<String, Integer> getColumnNamesAndTypes(final DataSource dataSource, final String tableName)
             throws MetaDataAccessException {
         final Map<String, Integer> columnNamesToTypes = new LinkedHashMap<String, Integer>();
         JdbcUtils.extractDatabaseMetaData(dataSource, new DatabaseMetaDataCallback() {
             @Override
             public Object processMetaData(DatabaseMetaData dbmd) throws SQLException, MetaDataAccessException {
                 ResultSet rs = dbmd.getColumns(null, null, tableName, null);
                 while (rs.next()) {
                     String columnName = rs.getString("COLUMN_NAME");
                     int columnType = rs.getInt("DATA_TYPE");
                     columnNamesToTypes.put(columnName, columnType);
                 }
                 rs.close();
                 return null;
             }
         });
         return columnNamesToTypes;
     }
 
     /**
      * Converts the protobuf message to JSON (wrapping exceptions).
      *
      * @param message Protobuf message.
      * @param <T> Type of protobuf.
      * @return JSON string representation of protobuf.
      * @throws RuntimeException If an exception occurs.
      */
     public static <T extends Message> String protobufToJson(T message) throws RuntimeException {
         try {
             return JsonFormat.writeAsString(message);
         } catch (IOException e) {
             throw new RuntimeException(e.getLocalizedMessage(), e);
         }
     }
 
     /**
      * Converts the JSON to the protobuf message (wrapping exceptions).
      *
      * @param json JSON string representation of protobuf.
      * @param defaultInstance Default instance of protobuf.
      * @param <T> Type of protobuf.
      * @return The deserialized message from the JSON representation.
      * @throws RuntimeException If an error occurs.
      */
     @SuppressWarnings({"unchecked"})
     public static <T extends Message> T protobufFromJson(String json, T defaultInstance) throws RuntimeException {
         try {
             return (T) JsonFormat.merge(json, defaultInstance.newBuilderForType());
         } catch (IOException e) {
             throw new RuntimeException(e.getLocalizedMessage(), e);
         }
     }
 
     /**
      * Performs the equivalent to an INSERT ... ON DUPLICATE KEY UPDATE (or UPSERT) using the specified insert and
      * update SQL and parameters.
      *
      * @param transactionService NestedTransactionService used to safely perform an insert which may lead to
      * DuplicateKeyErrors.
      * @param jdbcOperations A SimpleJdbcOperations interface which is used to perform the update (if needed).
      * @param insertSql The SQL to execute to perform an insert of the specified row.
      * @param updateSql The SQL to execute to perform an update of the specified row.
      * @param fields The fields used as parameters in the insert/update SQL statements.
      * @return The number of affected rows by the query.
      * @throws DataAccessException If an exception occurs (other than a DuplicateKeyException).
      */
     public static int insertOrUpdate(NestedTransactionService transactionService,
                                      SimpleJdbcOperations jdbcOperations, final String insertSql,
                                      String updateSql, final Map<String,?> fields) throws DataAccessException {
         int numRows;
         try {
             numRows = transactionService.executeInNestedTransaction(new NestedTransactionCallback<Integer>() {
                 @Override
                 public Integer doInNestedTransaction(NestedTransactionContext context) throws DataAccessException {
                     return context.getSimpleJdbcTemplate().update(insertSql, fields);
                 }
             });
         } catch (DuplicateKeyException e) {
             numRows = jdbcOperations.update(updateSql, fields);
         }
         return numRows;
     }
 
     /**
      * Performs the same operation as <code>insertOrUpdate</code>, but it attempts the update first, then an insert
      * and update. This method should be preferred to insertOrUpdate when the majority of operations will be updates
      * of existing rows instead of new rows added.
      *
      * @param transactionService NestedTransactionService used to safely perform an insert which may lead to
      * DuplicateKeyErrors.
      * @param jdbcOperations A SimpleJdbcOperations interface which is used to perform the update (if needed).
      * @param insertSql The SQL to execute to perform an insert of the specified row.
      * @param updateSql The SQL to execute to perform an update of the specified row.
      * @param fields The fields used as parameters in the insert/update SQL statements.
      * @return The number of affected rows by the query.
      * @throws DataAccessException If an exception occurs (other than a DuplicateKeyException).
      */
     public static int updateOrInsert(NestedTransactionService transactionService,
                                      SimpleJdbcOperations jdbcOperations, String insertSql, String updateSql,
                                      Map<String,?> fields) throws DataAccessException {
         int numRows = jdbcOperations.update(updateSql, fields);
         if (numRows == 0) {
             numRows = insertOrUpdate(transactionService, jdbcOperations, insertSql, updateSql, fields);
         }
         return numRows;
     }
 
     /**
      * Attempts to execute the specified method, returning the result. If a deadlock exception is detected, then the
      * method is retried up to {@link #NUM_DEADLOCK_RETRIES} times.
      *
      * @param callable The callable to invoke.
      * @param <T> The return type of the callable.
      * @return The result of calling the callable method.
      * @throws Exception If an exception occurs (other than a deadlock exception) or if the maximum number of deadlock
      *                   retries is exhausted.
      */
     public static <T> T deadlockRetry(Callable<T> callable) throws Exception {
         Exception lastException;
         int i = 0;
         do {
             ++i;
             try {
                 return callable.call();
             } catch (Exception e) {
                 if (!ZepUtils.isExceptionOfType(e, DeadlockLoserDataAccessException.class)) {
                     throw e;
                 }
                 // Retry transaction.
                 lastException = e;
             }
         } while (i < NUM_DEADLOCK_RETRIES);
 
         throw lastException;
     }
 }
