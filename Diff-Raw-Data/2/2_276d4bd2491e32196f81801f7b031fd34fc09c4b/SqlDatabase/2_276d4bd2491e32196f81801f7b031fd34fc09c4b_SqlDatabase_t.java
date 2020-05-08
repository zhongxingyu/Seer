 package com.psddev.dari.db;
 
 import com.jolbox.bonecp.BoneCPDataSource;
 
 import com.psddev.dari.util.ObjectUtils;
 import com.psddev.dari.util.PaginatedResult;
 import com.psddev.dari.util.PeriodicValue;
 import com.psddev.dari.util.PullThroughValue;
 import com.psddev.dari.util.Settings;
 import com.psddev.dari.util.SettingsException;
 import com.psddev.dari.util.Stats;
 import com.psddev.dari.util.StringUtils;
 import com.psddev.dari.util.TypeDefinition;
 import java.io.ByteArrayInputStream;
 
 import java.lang.annotation.Documented;
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 import java.lang.ref.WeakReference;
 import java.sql.BatchUpdateException;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.Driver;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.SQLIntegrityConstraintViolationException;
 import java.sql.SQLTimeoutException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.sql.DataSource;
 
 import org.iq80.snappy.Snappy;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /** Database backed by a SQL engine. */
 public class SqlDatabase extends AbstractDatabase<Connection> {
 
     public static final String DATA_SOURCE_SETTING = "dataSource";
     public static final String JDBC_DRIVER_CLASS_SETTING = "jdbcDriverClass";
     public static final String JDBC_URL_SETTING = "jdbcUrl";
     public static final String JDBC_USER_SETTING = "jdbcUser";
     public static final String JDBC_PASSWORD_SETTING = "jdbcPassword";
     public static final String JDBC_POOL_SIZE_SETTING = "jdbcPoolSize";
 
     public static final String READ_DATA_SOURCE_SETTING = "readDataSource";
     public static final String READ_JDBC_DRIVER_CLASS_SETTING = "readJdbcDriverClass";
     public static final String READ_JDBC_URL_SETTING = "readJdbcUrl";
     public static final String READ_JDBC_USER_SETTING = "readJdbcUser";
     public static final String READ_JDBC_PASSWORD_SETTING = "readJdbcPassword";
     public static final String READ_JDBC_POOL_SIZE_SETTING = "readJdbcPoolSize";
 
     public static final String VENDOR_CLASS_SETTING = "vendorClass";
     public static final String COMPRESS_DATA_SUB_SETTING = "compressData";
 
     public static final String RECORD_TABLE = "Record";
     public static final String RECORD_UPDATE_TABLE = "RecordUpdate";
     public static final String SYMBOL_TABLE = "Symbol";
     public static final String ID_COLUMN = "id";
     public static final String TYPE_ID_COLUMN = "typeId";
     public static final String IN_ROW_INDEX_COLUMN = "inRowIndex";
     public static final String DATA_COLUMN = "data";
     public static final String SYMBOL_ID_COLUMN = "symbolId";
     public static final String UPDATE_DATE_COLUMN = "updateDate";
     public static final String VALUE_COLUMN = "value";
 
     public static final String EXTRA_COLUMNS_QUERY_OPTION = "sql.extraColumns";
     public static final String EXTRA_JOINS_QUERY_OPTION = "sql.extraJoins";
     public static final String EXTRA_WHERE_QUERY_OPTION = "sql.extraWhere";
     public static final String EXTRA_HAVING_QUERY_OPTION = "sql.extraHaving";
     public static final String MYSQL_INDEX_HINT_QUERY_OPTION = "sql.mysqlIndexHint";
     public static final String RETURN_ORIGINAL_DATA_QUERY_OPTION = "sql.returnOriginalData";
     public static final String USE_JDBC_FETCH_SIZE_QUERY_OPTION = "sql.useJdbcFetchSize";
     public static final String USE_READ_DATA_SOURCE_QUERY_OPTION = "sql.useReadDataSource";
     public static final String SKIP_INDEX_STATE_EXTRA = "sql.skipIndex";
 
     public static final String INDEX_TABLE_INDEX_OPTION = "sql.indexTable";
 
     public static final String EXTRA_COLUMN_EXTRA_PREFIX = "sql.extraColumn.";
     public static final String ORIGINAL_DATA_EXTRA = "sql.originalData";
 
     private static final Logger LOGGER = LoggerFactory.getLogger(SqlDatabase.class);
     private static final Stats STATS = new Stats("SQL");
     private static final String QUERY_OPERATION = "Query";
     private static final String UPDATE_OPERATION = "Update";
 
     private final static List<SqlDatabase> INSTANCES = new ArrayList<SqlDatabase>();
 
     {
         INSTANCES.add(this);
     }
 
     private volatile DataSource dataSource;
     private volatile DataSource readDataSource;
     private volatile SqlVendor vendor;
     private volatile boolean compressData;
 
     /**
      * Quotes the given {@code identifier} so that it's safe to use
      * in a SQL query.
      */
     public static String quoteIdentifier(String identifier) {
         return "\"" + StringUtils.replaceAll(identifier, "\\\\", "\\\\\\\\", "\"", "\"\"") + "\"";
     }
 
     /**
      * Quotes the given {@code value} so that it's safe to use
      * in a SQL query.
      */
     public static String quoteValue(Object value) {
         if (value == null) {
             return "NULL";
         } else if (value instanceof Number) {
             return value.toString();
         } else if (value instanceof byte[]) {
             return "X'" + StringUtils.hex((byte[]) value) + "'";
         } else {
             return "'" + value.toString().replace("'", "''").replace("\\", "\\\\") + "'";
         }
     }
 
     /** Closes all resources used by all instances. */
     public static void closeAll() {
         for (SqlDatabase database : INSTANCES) {
             database.close();
         }
         INSTANCES.clear();
     }
 
     /**
      * Creates an {@link SqlDatabaseException} that occurred during
      * an execution of a query.
      */
     private SqlDatabaseException createQueryException(
             SQLException error,
             String sqlQuery,
             Query<?> query) {
 
         String message = error.getMessage();
         if (error instanceof SQLTimeoutException || message.contains("timeout")) {
             return new SqlDatabaseException.ReadTimeout(this, error, sqlQuery, query);
         } else {
             return new SqlDatabaseException(this, error, sqlQuery, query);
         }
     }
 
     /** Returns the JDBC data source used for general database operations. */
     public DataSource getDataSource() {
         return dataSource;
     }
 
     private static final Map<String, Class<? extends SqlVendor>> VENDOR_CLASSES; static {
         Map<String, Class<? extends SqlVendor>> m = new HashMap<String, Class<? extends SqlVendor>>();
         m.put("H2", SqlVendor.H2.class);
         m.put("MySQL", SqlVendor.MySQL.class);
         m.put("PostgreSQL", SqlVendor.PostgreSQL.class);
         m.put("Oracle", SqlVendor.Oracle.class);
         VENDOR_CLASSES = m;
     }
 
     /** Sets the JDBC data source used for general database operations. */
     public void setDataSource(DataSource dataSource) {
         this.dataSource = dataSource;
         if (dataSource == null) {
             return;
         }
 
         synchronized (this) {
             try {
                 if (vendor == null) {
                     Connection connection = openConnection();
 
                     try {
                         DatabaseMetaData meta = connection.getMetaData();
                         String vendorName = meta.getDatabaseProductName();
                         Class<? extends SqlVendor> vendorClass = VENDOR_CLASSES.get(vendorName);
 
                         LOGGER.info(
                                 "Initializing SQL vendor for [{}]: [{}] -> [{}]",
                                 new Object[] { getName(), vendorName, vendorClass });
 
                         vendor = vendorClass != null ? TypeDefinition.getInstance(vendorClass).newInstance() : new SqlVendor();
                         vendor.setDatabase(this);
 
                     } finally {
                         closeConnection(connection);
                     }
                 }
 
                 tableNames.refresh();
                 symbols.invalidate();
 
                 vendor.createRecord(this);
                 vendor.createRecordUpdate(this);
                 vendor.createSymbol(this);
 
                 for (SqlIndex index : SqlIndex.values()) {
                     if (index != SqlIndex.CUSTOM) {
                         vendor.createRecordIndex(
                                 this,
                                 index.getReadTable(this, null).getName(this, null),
                                 index);
                     }
                 }
 
                 tableNames.refresh();
                 symbols.invalidate();
 
             } catch (SQLException ex) {
                 throw new SqlDatabaseException(this, "Can't check for required tables!", ex);
             }
         }
     }
 
     /** Returns the JDBC data source used exclusively for read operations. */
     public DataSource getReadDataSource() {
         return this.readDataSource;
     }
 
     /** Sets the JDBC data source used exclusively for read operations. */
     public void setReadDataSource(DataSource readDataSource) {
         this.readDataSource = readDataSource;
     }
 
     /** Returns the vendor-specific SQL engine information. */
     public SqlVendor getVendor() {
         return vendor;
     }
 
     /** Sets the vendor-specific SQL engine information. */
     public void setVendor(SqlVendor vendor) {
         this.vendor = vendor;
     }
 
     /** Returns {@code true} if the data should be compressed. */
     public boolean isCompressData() {
         return compressData;
     }
 
     /** Sets whether the data should be compressed. */
     public void setCompressData(boolean compressData) {
         this.compressData = compressData;
     }
 
     /**
      * Returns {@code true} if the {@link #RECORD_TABLE} in this database
      * has the {@link #IN_ROW_INDEX_COLUMN}.
      */
     public boolean hasInRowIndex() {
         return hasInRowIndex;
     }
 
     /**
      * Returns {@code true} if all comparisons executed in this database
      * should ignore case by default.
      */
     public boolean comparesIgnoreCase() {
         return comparesIgnoreCase;
     }
 
     /**
      * Returns {@code true} if this database contains a table with
      * the given {@code name}.
      */
     public boolean hasTable(String name) {
         if (name == null) {
             return false;
         } else {
             Set<String> names = tableNames.get();
             return names != null && names.contains(name.toLowerCase());
         }
     }
 
     private transient volatile boolean hasInRowIndex;
     private transient volatile boolean comparesIgnoreCase;
 
     private final transient PeriodicValue<Set<String>> tableNames = new PeriodicValue<Set<String>>(0.0, 60.0) {
 
         @Override
         protected Set<String> update() {
             if (getDataSource() == null) {
                 return Collections.emptySet();
             }
 
             Connection connection = openConnection();
 
             try {
                 SqlVendor vendor = getVendor();
                 String recordTable = null;
                 int maxStringVersion = 0;
                 Set<String> loweredNames = new HashSet<String>();
 
                 for (String name : vendor.getTables(connection)) {
                     String loweredName = name.toLowerCase();
 
                     loweredNames.add(loweredName);
 
                     if ("record".equals(loweredName)) {
                         recordTable = name;
 
                     } else if (loweredName.startsWith("recordstring")) {
                         int version = ObjectUtils.to(int.class, loweredName.substring(12));
                         if (version > maxStringVersion) {
                             maxStringVersion = version;
                         }
                     }
                 }
 
                 if (recordTable != null) {
                     hasInRowIndex = vendor.hasInRowIndex(connection, recordTable);
                 }
 
                 comparesIgnoreCase = maxStringVersion >= 3;
 
                 return loweredNames;
 
             } catch (SQLException error) {
                 LOGGER.error("Can't query table names!", error);
                 return get();
 
             } finally {
                 closeConnection(connection);
             }
         }
     };
 
     /**
      * Returns an unique numeric ID for the given {@code symbol}.
      */
     public int getSymbolId(String symbol) {
         Integer id = symbols.get().get(symbol);
         if (id == null) {
 
             SqlVendor vendor = getVendor();
             Connection connection = openConnection();
 
             try {
                 List<Object> parameters = new ArrayList<Object>();
                 StringBuilder insertBuilder = new StringBuilder();
 
                 insertBuilder.append("INSERT /*! IGNORE */ INTO ");
                 vendor.appendIdentifier(insertBuilder, SYMBOL_TABLE);
                 insertBuilder.append(" (");
                 vendor.appendIdentifier(insertBuilder, VALUE_COLUMN);
                 insertBuilder.append(") VALUES (");
                 vendor.appendBindValue(insertBuilder, symbol, parameters);
                 insertBuilder.append(")");
 
                 String insertSql = insertBuilder.toString();
                 try {
                     Static.executeUpdateWithList(connection, insertSql, parameters);
 
                 } catch (SQLException ex) {
                     if (!Static.isIntegrityConstraintViolation(ex)) {
                         throw createQueryException(ex, insertSql, null);
                     }
                 }
 
                 StringBuilder selectBuilder = new StringBuilder();
                 selectBuilder.append("SELECT ");
                 vendor.appendIdentifier(selectBuilder, SYMBOL_ID_COLUMN);
                 selectBuilder.append(" FROM ");
                 vendor.appendIdentifier(selectBuilder, SYMBOL_TABLE);
                 selectBuilder.append(" WHERE ");
                 vendor.appendIdentifier(selectBuilder, VALUE_COLUMN);
                 selectBuilder.append("=");
                 vendor.appendValue(selectBuilder, symbol);
 
                 String selectSql = selectBuilder.toString();
                 Statement statement = null;
                 ResultSet result = null;
 
                 try {
                     statement = connection.createStatement();
                     result = statement.executeQuery(selectSql);
                     result.next();
                     id = result.getInt(1);
                     symbols.get().put(symbol, id);
 
                 } catch (SQLException ex) {
                     throw createQueryException(ex, selectSql, null);
 
                 } finally {
                     closeResources(null, statement, result);
                 }
 
             } finally {
                 closeConnection(connection);
             }
         }
 
         return id;
     }
 
     // Cache of all internal symbols.
     private transient PullThroughValue<Map<String, Integer>> symbols = new PullThroughValue<Map<String, Integer>>() {
 
         @Override
         protected Map<String, Integer> produce() {
             SqlVendor vendor = getVendor();
             StringBuilder selectBuilder = new StringBuilder();
             selectBuilder.append("SELECT ");
             vendor.appendIdentifier(selectBuilder, SYMBOL_ID_COLUMN);
             selectBuilder.append(",");
             vendor.appendIdentifier(selectBuilder, VALUE_COLUMN);
             selectBuilder.append(" FROM ");
             vendor.appendIdentifier(selectBuilder, SYMBOL_TABLE);
 
             String selectSql = selectBuilder.toString();
             Connection connection = openConnection();
             Statement statement = null;
             ResultSet result = null;
 
             try {
                 statement = connection.createStatement();
                 result = statement.executeQuery(selectSql);
 
                 Map<String, Integer> symbols = new ConcurrentHashMap<String, Integer>();
                 while (result.next()) {
                     symbols.put(new String(result.getBytes(2), StringUtils.UTF_8), result.getInt(1));
                 }
 
                 return symbols;
 
             } catch (SQLException ex) {
                 throw createQueryException(ex, selectSql, null);
 
             } finally {
                 closeResources(connection, statement, result);
             }
         }
     };
 
     /**
      * Returns the underlying JDBC connection.
      *
      * @deprecated Use {@link #openConnection} instead.
      */
     @Deprecated
     public Connection getConnection() {
         return openConnection();
     }
 
     /** Closes any resources used by this database. */
     public void close() {
         DataSource dataSource = getDataSource();
         if (dataSource instanceof BoneCPDataSource) {
             LOGGER.info("Closing BoneCP data source in {}", getName());
             ((BoneCPDataSource) dataSource).close();
         }
 
         DataSource readDataSource = getReadDataSource();
         if (readDataSource instanceof BoneCPDataSource) {
             LOGGER.info("Closing BoneCP read data source in {}", getName());
             ((BoneCPDataSource) readDataSource).close();
         }
 
         setDataSource(null);
         setReadDataSource(null);
     }
 
     /**
      * Builds an SQL statement that can be used to get a count of all
      * objects matching the given {@code query}.
      */
     public String buildCountStatement(Query<?> query) {
         return new SqlQuery(this, query).countStatement();
     }
 
     /**
      * Builds an SQL statement that can be used to delete all rows
      * matching the given {@code query}.
      */
     public String buildDeleteStatement(Query<?> query) {
         return new SqlQuery(this, query).deleteStatement();
     }
 
     /**
      * Builds an SQL statement that can be used to get all objects
      * grouped by the values of the given {@code groupFields}.
      */
     public String buildGroupStatement(Query<?> query, String... groupFields) {
         return new SqlQuery(this, query).groupStatement(groupFields);
     }
 
     /**
      * Builds an SQL statement that can be used to get when the objects
      * matching the given {@code query} were last updated.
      */
     public String buildLastUpdateStatement(Query<?> query) {
         return new SqlQuery(this, query).lastUpdateStatement();
     }
 
     /**
      * Builds an SQL statement that can be used to list all rows
      * matching the given {@code query}.
      */
     public String buildSelectStatement(Query<?> query) {
         return new SqlQuery(this, query).selectStatement();
     }
 
     /** Closes all the given SQL resources safely. */
     private void closeResources(Connection connection, Statement statement, ResultSet result) {
         if (result != null) {
             try {
                 result.close();
             } catch (SQLException ex) {
             }
         }
 
         if (statement != null) {
             try {
                 statement.close();
             } catch (SQLException ex) {
             }
         }
 
         if (connection != null &&
                 connection != readConnection.get()) {
             try {
                 connection.close();
             } catch (SQLException ex) {
             }
         }
     }
 
     private byte[] serializeData(Map<String, Object> dataMap) {
         byte[] dataBytes = ObjectUtils.toJson(dataMap).getBytes(StringUtils.UTF_8);
 
         if (isCompressData()) {
             byte[] compressed = new byte[Snappy.maxCompressedLength(dataBytes.length)];
             int compressedLength = Snappy.compress(dataBytes, 0, dataBytes.length, compressed, 0);
             dataBytes = new byte[compressedLength + 1];
             dataBytes[0] = 's';
             System.arraycopy(compressed, 0, dataBytes, 1, compressedLength);
         }
 
         return dataBytes;
     }
 
     @SuppressWarnings("unchecked")
     private Map<String, Object> unserializeData(byte[] dataBytes) {
         char format = '\0';
 
         while (true) {
             format = (char) dataBytes[0];
 
             if (format == 's') {
                 dataBytes = Snappy.uncompress(dataBytes, 1, dataBytes.length - 1);
 
             } else if (format == '{') {
                 return (Map<String, Object>) ObjectUtils.fromJson(new String(dataBytes, StringUtils.UTF_8));
 
             } else {
                 break;
             }
         }
 
         throw new IllegalStateException(String.format(
                 "Unknown format! ([%s])", format));
     }
 
     /**
      * Creates a previously saved object using the given {@code resultSet}.
      */
     private <T> T createSavedObjectWithResultSet(ResultSet resultSet, Query<T> query) throws SQLException {
         T object = createSavedObject(resultSet.getObject(2), resultSet.getObject(1), query);
         State objectState = State.getInstance(object);
 
         if (!objectState.isReferenceOnly()) {
             byte[] data = resultSet.getBytes(3);
             if (data != null) {
                 objectState.putAll(unserializeData(data));
                 Boolean returnOriginal = ObjectUtils.to(Boolean.class, query.getOptions().get(RETURN_ORIGINAL_DATA_QUERY_OPTION));
                 if (returnOriginal == null) {
                     returnOriginal = Boolean.FALSE;
                 }
                 if (returnOriginal) {
                     objectState.getExtras().put(ORIGINAL_DATA_EXTRA, data);
                 }
             }
         }
 
         ResultSetMetaData meta = resultSet.getMetaData();
         for (int i = 4, count = meta.getColumnCount(); i <= count; ++ i) {
             objectState.getExtras().put(EXTRA_COLUMN_EXTRA_PREFIX + meta.getColumnLabel(i), resultSet.getObject(i));
         }
 
         return swapObjectType(query, object);
     }
 
     /**
      * Executes the given read {@code statement} (created from the given
      * {@code sqlQuery}) before the given {@code timeout} (in seconds).
      */
     private ResultSet executeQueryBeforeTimeout(
             Statement statement,
             String sqlQuery,
             int timeout)
             throws SQLException {
 
         if (timeout > 0 && !(vendor instanceof SqlVendor.PostgreSQL)) {
             statement.setQueryTimeout(timeout);
         }
 
         Stats.Timer timer = STATS.startTimer();
         try {
             return statement.executeQuery(sqlQuery);
 
         } finally {
             double duration = timer.stop(QUERY_OPERATION);
             LOGGER.debug(
                     "Read from the SQL database using [{}] in [{}]ms",
                     sqlQuery, duration);
         }
     }
 
     /**
      * Selects the first object that matches the given {@code sqlQuery}
      * with options from the given {@code query}.
      */
     public <T> T selectFirstWithOptions(String sqlQuery, Query<T> query) {
         sqlQuery = vendor.rewriteQueryWithLimitClause(sqlQuery, 1, 0);
 
         Connection connection = null;
         Statement statement = null;
         ResultSet result = null;
 
         try {
             connection = openQueryConnection(query);
             statement = connection.createStatement();
             result = executeQueryBeforeTimeout(statement, sqlQuery, getQueryReadTimeout(query));
             return result.next() ? createSavedObjectWithResultSet(result, query) : null;
 
         } catch (SQLException ex) {
             throw createQueryException(ex, sqlQuery, query);
 
         } finally {
             closeResources(connection, statement, result);
         }
     }
 
     /**
      * Selects the first object that matches the given {@code sqlQuery}
      * without a timeout.
      */
     public Object selectFirst(String sqlQuery) {
         return selectFirstWithOptions(sqlQuery, null);
     }
 
     /**
      * Selects a list of objects that match the given {@code sqlQuery}
      * with options from the given {@code query}.
      */
     public <T> List<T> selectListWithOptions(String sqlQuery, Query<T> query) {
         Connection connection = null;
         Statement statement = null;
         ResultSet result = null;
         List<T> objects = new ArrayList<T>();
         int timeout = getQueryReadTimeout(query);
 
         try {
             connection = openQueryConnection(query);
             statement = connection.createStatement();
             result = executeQueryBeforeTimeout(statement, sqlQuery, timeout);
             while (result.next()) {
                 objects.add(createSavedObjectWithResultSet(result, query));
             }
 
             return objects;
 
         } catch (SQLException ex) {
             throw createQueryException(ex, sqlQuery, query);
 
         } finally {
             closeResources(connection, statement, result);
         }
     }
 
     /**
      * Selects a list of objects that match the given {@code sqlQuery}
      * without a timeout.
      */
     public List<Object> selectList(String sqlQuery) {
         return selectListWithOptions(sqlQuery, null);
     }
 
     /**
      * Returns an iterable that selects all objects matching the given
      * {@code sqlQuery} with options from the given {@code query}.
      */
     public <T> Iterable<T> selectIterableWithOptions(
             final String sqlQuery,
             final int fetchSize,
             final Query<T> query) {
 
         return new Iterable<T>() {
             @Override
             public Iterator<T> iterator() {
                 return new SqlIterator<T>(sqlQuery, fetchSize, query);
             }
         };
     }
 
     private class SqlIterator<T> implements Iterator<T> {
 
         private final String sqlQuery;
         private final Query<T> query;
 
         private final Connection connection;
         private final Statement statement;
         private final ResultSet result;
 
         private boolean hasNext = true;
 
         public SqlIterator(String initialSqlQuery, int fetchSize, Query<T> initialQuery) {
             sqlQuery = initialSqlQuery;
             query = initialQuery;
 
             try {
                 connection = openConnection();
                 statement = connection.createStatement();
                 statement.setFetchSize(
                         getVendor() instanceof SqlVendor.MySQL ? Integer.MIN_VALUE :
                         fetchSize <= 0 ? 200 :
                         fetchSize);
                 result = statement.executeQuery(sqlQuery);
                 moveToNext();
 
             } catch (SQLException ex) {
                 close();
                 throw createQueryException(ex, sqlQuery, query);
             }
         }
 
         private void moveToNext() throws SQLException {
             if (hasNext) {
                 hasNext = result.next();
                 if (!hasNext) {
                     close();
                 }
             }
         }
 
         public void close() {
             hasNext = false;
             closeResources(connection, statement, result);
         }
 
         @Override
         public boolean hasNext() {
             return hasNext;
         }
 
         @Override
         public T next() {
             if (!hasNext) {
                 throw new NoSuchElementException();
             }
 
             try {
                 T object = createSavedObjectWithResultSet(result, query);
                 moveToNext();
                 return object;
 
             } catch (SQLException ex) {
                 close();
                 throw createQueryException(ex, sqlQuery, query);
             }
         }
 
         @Override
         public void remove() {
             throw new UnsupportedOperationException();
         }
 
         @Override
         protected void finalize() {
             close();
         }
     }
 
     /**
      * Fills the placeholders in the given {@code sqlQuery} with the given
      * {@code parameters}.
      */
     private static String fillPlaceholders(String sqlQuery, Object... parameters) {
         StringBuilder filled = new StringBuilder();
         int prevPh = 0;
         for (int ph, index = 0; (ph = sqlQuery.indexOf('?', prevPh)) > -1; ++ index) {
             filled.append(sqlQuery.substring(prevPh, ph));
             prevPh = ph + 1;
             filled.append(quoteValue(parameters[index]));
         }
         filled.append(sqlQuery.substring(prevPh));
         return filled.toString();
     }
 
     /**
      * Executes the given write {@code sqlQuery} with the given
      * {@code parameters}.
      *
      * @deprecated Use {@link Static#executeUpdate} instead.
      */
     @Deprecated
     public int executeUpdate(String sqlQuery, Object... parameters) {
         try {
             return Static.executeUpdateWithArray(getConnection(), sqlQuery, parameters);
         } catch (SQLException ex) {
             throw createQueryException(ex, fillPlaceholders(sqlQuery, parameters), null);
         }
     }
 
     /**
      * Reads the given {@code resultSet} into a list of maps
      * and closes it.
      */
     public List<Map<String, Object>> readResultSet(ResultSet resultSet) throws SQLException {
         try {
             ResultSetMetaData meta = resultSet.getMetaData();
             List<String> columnNames = new ArrayList<String>();
             for (int i = 1, count = meta.getColumnCount(); i < count; ++ i) {
                 columnNames.add(meta.getColumnName(i));
             }
 
             List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
             while (resultSet.next()) {
                 Map<String, Object> map = new LinkedHashMap<String, Object>();
                 maps.add(map);
                 for (int i = 0, size = columnNames.size(); i < size; ++ i) {
                     map.put(columnNames.get(i), resultSet.getObject(i + 1));
                 }
             }
 
             return maps;
 
         } finally {
             resultSet.close();
         }
     }
 
     // --- AbstractDatabase support ---
 
     @Override
     public Connection openConnection() {
         DataSource dataSource = getDataSource();
         if (dataSource == null) {
             throw new SqlDatabaseException(this, "No SQL data source!");
         }
 
         try {
             return dataSource.getConnection();
         } catch (SQLException ex) {
             throw new SqlDatabaseException(this, "Can't connect to the SQL engine!", ex);
         }
     }
 
     @Override
     protected Connection doOpenReadConnection() {
         Connection connection = readConnection.get();
         if (connection != null) {
             return connection;
         }
 
         DataSource readDataSource = getReadDataSource();
         if (readDataSource == null) {
             readDataSource = getDataSource();
         }
 
         if (readDataSource == null) {
             throw new SqlDatabaseException(this, "No SQL data source!");
         }
 
         try {
             return readDataSource.getConnection();
         } catch (SQLException ex) {
             throw new SqlDatabaseException(this, "Can't connect to the SQL engine!", ex);
         }
     }
 
     // Opens a connection that should be used to execute the given query.
     private Connection openQueryConnection(Query<?> query) {
         if (query != null) {
             Boolean useRead = ObjectUtils.to(Boolean.class, query.getOptions().get(USE_READ_DATA_SOURCE_QUERY_OPTION));
             if (useRead == null) {
                 useRead = Boolean.TRUE;
             }
             if (!useRead) {
                 return openConnection();
             }
         }
         return openReadConnection();
     }
 
     @Override
     public void closeConnection(Connection connection) {
         if (connection != null &&
                 connection != readConnection.get()) {
             try {
                 connection.close();
             } catch (SQLException ex) {
             }
         }
     }
 
     @Override
     protected boolean isRecoverableError(Exception error) {
         if (error instanceof SQLException) {
             SQLException sqlError = (SQLException) error;
             return "40001".equals(sqlError.getSQLState());
         }
 
         return false;
     }
 
     private final ThreadLocal<Connection> readConnection = new ThreadLocal<Connection>();
 
     public void beginThreadLocalReadConnection() {
         Connection connection = readConnection.get();
         if (connection == null) {
             connection = openReadConnection();
             readConnection.set(connection);
         }
     }
 
     public void endThreadLocalReadConnection() {
         Connection connection = readConnection.get();
         if (connection != null) {
             try {
                 connection.close();
             } catch (SQLException ex) {
             } finally {
                 readConnection.remove();
             }
         }
     }
 
     @Override
     protected void doInitialize(String settingsKey, Map<String, Object> settings) {
         close();
         setDataSource(createDataSource(
                 settings,
                 DATA_SOURCE_SETTING,
                 JDBC_DRIVER_CLASS_SETTING,
                 JDBC_URL_SETTING,
                 JDBC_USER_SETTING,
                 JDBC_PASSWORD_SETTING,
                 JDBC_POOL_SIZE_SETTING));
         setReadDataSource(createDataSource(
                 settings,
                 READ_DATA_SOURCE_SETTING,
                 READ_JDBC_DRIVER_CLASS_SETTING,
                 READ_JDBC_URL_SETTING,
                 READ_JDBC_USER_SETTING,
                 READ_JDBC_PASSWORD_SETTING,
                 READ_JDBC_POOL_SIZE_SETTING));
 
         String vendorClassName = ObjectUtils.to(String.class, settings.get(VENDOR_CLASS_SETTING));
         Class<?> vendorClass = null;
 
         if (vendorClassName != null) {
             vendorClass = ObjectUtils.getClassByName(vendorClassName);
             if (vendorClass == null) {
                 throw new SettingsException(
                         VENDOR_CLASS_SETTING,
                         String.format("Can't find [%s]!",
                         vendorClassName));
             } else if (!SqlVendor.class.isAssignableFrom(vendorClass)) {
                 throw new SettingsException(
                         VENDOR_CLASS_SETTING,
                         String.format("[%s] doesn't implement [%s]!",
                         vendorClass, Driver.class));
             }
         }
 
         if (vendorClass != null) {
             setVendor((SqlVendor) TypeDefinition.getInstance(vendorClass).newInstance());
         }
 
         Boolean compressData = ObjectUtils.coalesce(
                 ObjectUtils.to(Boolean.class, settings.get(COMPRESS_DATA_SUB_SETTING)),
                 Settings.get(Boolean.class, "dari/isCompressSqlData"));
         if (compressData != null) {
             setCompressData(compressData);
         }
     }
 
     private static final Map<String, String> DRIVER_CLASS_NAMES; static {
         Map<String, String> m = new HashMap<String, String>();
         m.put("h2", "org.h2.Driver");
         m.put("jtds", "net.sourceforge.jtds.jdbc.Driver");
         m.put("mysql", "com.mysql.jdbc.Driver");
         m.put("postgresql", "org.postgresql.Driver");
         DRIVER_CLASS_NAMES = m;
     }
 
     private static final Set<WeakReference<Driver>> REGISTERED_DRIVERS = new HashSet<WeakReference<Driver>>();
 
     private DataSource createDataSource(
             Map<String, Object> settings,
             String dataSourceSetting,
             String jdbcDriverClassSetting,
             String jdbcUrlSetting,
             String jdbcUserSetting,
             String jdbcPasswordSetting,
             String jdbcPoolSizeSetting) {
 
         Object dataSourceObject = settings.get(dataSourceSetting);
         if (dataSourceObject instanceof DataSource) {
             return (DataSource) dataSourceObject;
 
         } else {
             String url = ObjectUtils.to(String.class, settings.get(jdbcUrlSetting));
             if (ObjectUtils.isBlank(url)) {
                 return null;
 
             } else {
                 String driverClassName = ObjectUtils.to(String.class, settings.get(jdbcDriverClassSetting));
                 Class<?> driverClass = null;
 
                 if (driverClassName != null) {
                     driverClass = ObjectUtils.getClassByName(driverClassName);
                     if (driverClass == null) {
                         throw new SettingsException(
                                 jdbcDriverClassSetting,
                                 String.format("Can't find [%s]!",
                                 driverClassName));
                     } else if (!Driver.class.isAssignableFrom(driverClass)) {
                         throw new SettingsException(
                                 jdbcDriverClassSetting,
                                 String.format("[%s] doesn't implement [%s]!",
                                 driverClass, Driver.class));
                     }
 
                 } else {
                     int firstColonAt = url.indexOf(':');
                     if (firstColonAt > -1) {
                         ++ firstColonAt;
                         int secondColonAt = url.indexOf(':', firstColonAt);
                         if (secondColonAt > -1) {
                             driverClass = ObjectUtils.getClassByName(DRIVER_CLASS_NAMES.get(url.substring(firstColonAt, secondColonAt)));
                         }
                     }
                 }
 
                 if (driverClass != null) {
                     Driver driver = null;
                     for (Enumeration<Driver> e = DriverManager.getDrivers(); e.hasMoreElements(); ) {
                         Driver d = e.nextElement();
                         if (driverClass.isInstance(d)) {
                             driver = d;
                             break;
                         }
                     }
 
                     if (driver == null) {
                         driver = (Driver) TypeDefinition.getInstance(driverClass).newInstance();
                         try {
                             LOGGER.info("Registering [{}]", driver);
                             DriverManager.registerDriver(driver);
                         } catch (SQLException ex) {
                             LOGGER.warn("Can't register [{}]!", driver);
                         }
                     }
 
                     if (driver != null) {
                         REGISTERED_DRIVERS.add(new WeakReference<Driver>(driver));
                     }
                 }
 
                 String user = ObjectUtils.to(String.class, settings.get(jdbcUserSetting));
                 String password = ObjectUtils.to(String.class, settings.get(jdbcPasswordSetting));
 
                 Integer poolSize = ObjectUtils.to(Integer.class, settings.get(jdbcPoolSizeSetting));
                 if (poolSize == null || poolSize <= 0) {
                     poolSize = 24;
                 }
 
                 int partitionCount = 3;
                 int connectionsPerPartition = poolSize / partitionCount;
                 LOGGER.info("Automatically creating BoneCP data source:" +
                         "\n\turl={}" +
                         "\n\tusername={}" +
                         "\n\tpoolSize={}" +
                         "\n\tconnectionsPerPartition={}" +
                         "\n\tpartitionCount={}", new Object[] {
                             url,
                             user,
                             poolSize,
                             connectionsPerPartition,
                             partitionCount
                         });
 
                 BoneCPDataSource bone = new BoneCPDataSource();
                 bone.setJdbcUrl(url);
                 bone.setUsername(user);
                 bone.setPassword(password);
                 bone.setMinConnectionsPerPartition(connectionsPerPartition);
                 bone.setMaxConnectionsPerPartition(connectionsPerPartition);
                 bone.setPartitionCount(partitionCount);
                 return bone;
             }
         }
     }
 
     /** Returns the read timeout associated with the given {@code query}. */
     private int getQueryReadTimeout(Query<?> query) {
         if (query != null) {
             Double timeout = query.getTimeout();
             if (timeout == null) {
                 timeout = getReadTimeout();
             }
             if (timeout > 0.0) {
                 return (int) Math.round(timeout);
             }
         }
         return 0;
     }
 
     @Override
     public <T> List<T> readAll(Query<T> query) {
         return selectListWithOptions(buildSelectStatement(query), query);
     }
 
     @Override
     public long readCount(Query<?> query) {
         String sqlQuery = buildCountStatement(query);
         Connection connection = null;
         Statement statement = null;
         ResultSet result = null;
 
         try {
             connection = openQueryConnection(query);
             statement = connection.createStatement();
             result = executeQueryBeforeTimeout(statement, sqlQuery, getQueryReadTimeout(query));
 
             if (result.next()) {
                 Object countObj = result.getObject(1);
                 if (countObj instanceof Number) {
                     return ((Number) countObj).longValue();
                 }
             }
 
             return 0;
 
         } catch (SQLException ex) {
             throw createQueryException(ex, sqlQuery, query);
 
         } finally {
             closeResources(connection, statement, result);
         }
     }
 
     @Override
     public <T> T readFirst(Query<T> query) {
         if (query.getSorters().isEmpty()) {
 
             Predicate predicate = query.getPredicate();
             if (predicate instanceof CompoundPredicate) {
 
                 CompoundPredicate compoundPredicate = (CompoundPredicate) predicate;
                 if (PredicateParser.OR_OPERATOR.equals(compoundPredicate.getOperator())) {
 
                     for (Predicate child : compoundPredicate.getChildren()) {
                         Query<T> childQuery = query.clone();
                         childQuery.setPredicate(child);
 
                         T first = readFirst(childQuery);
                         if (first != null) {
                             return first;
                         }
                     }
 
                     return null;
                 }
             }
         }
 
         return selectFirstWithOptions(buildSelectStatement(query), query);
     }
 
     @Override
     public <T> Iterable<T> readIterable(Query<T> query, int fetchSize) {
         Boolean useJdbc = ObjectUtils.to(Boolean.class, query.getOptions().get(USE_JDBC_FETCH_SIZE_QUERY_OPTION));
         if (useJdbc == null) {
             useJdbc = Boolean.TRUE;
         }
         if (useJdbc) {
             return selectIterableWithOptions(buildSelectStatement(query), fetchSize, query);
         } else {
             return new ByIdIterable<T>(query, fetchSize);
         }
     }
 
     private static class ByIdIterable<T> implements Iterable<T> {
 
         private final Query<T> query;
         private final int fetchSize;
 
         public ByIdIterable(Query<T> query, int fetchSize) {
             this.query = query;
             this.fetchSize = fetchSize;
         }
 
         @Override
         public Iterator<T> iterator() {
             return new ByIdIterator<T>(query, fetchSize);
         }
     }
 
     private static class ByIdIterator<T> implements Iterator<T> {
 
         private final Query<T> query;
         private final int fetchSize;
         private UUID lastTypeId;
         private UUID lastId;
         private List<T> items;
         private int index;
 
         public ByIdIterator(Query<T> query, int fetchSize) {
             if (!query.getSorters().isEmpty()) {
                 throw new IllegalArgumentException("Can't iterate over a query that has sorters!");
             }
 
             this.query = query.clone().timeout(0.0).sortAscending("_type").sortAscending("_id");
             this.fetchSize = fetchSize > 0 ? fetchSize : 200;
         }
 
         @Override
         public boolean hasNext() {
             if (items != null && items.isEmpty()) {
                 return false;
             }
 
             if (items == null || index >= items.size()) {
                 Query<T> nextQuery = query.clone();
                 if (lastTypeId != null) {
                     nextQuery.and("_type = ? and _id > ?", lastTypeId, lastId);
                 }
 
                 items = nextQuery.select(0, fetchSize).getItems();
 
                 int size = items.size();
                 if (size < 1) {
                     if (lastTypeId == null) {
                         return false;
 
                     } else {
                         nextQuery = query.clone().and("_type > ?", lastTypeId);
                         items = nextQuery.select(0, fetchSize).getItems();
                         size = items.size();
 
                         if (size < 1) {
                             return false;
                         }
                     }
                 }
 
                 State lastState = State.getInstance(items.get(size - 1));
                 lastTypeId = lastState.getTypeId();
                 lastId = lastState.getId();
                 index = 0;
             }
 
             return true;
         }
 
         @Override
         public T next() {
             if (hasNext()) {
                 T object = items.get(index);
                 ++ index;
                 return object;
 
             } else {
                 throw new NoSuchElementException();
             }
         }
 
         @Override
         public void remove() {
             throw new UnsupportedOperationException();
         }
     }
 
     @Override
     public Date readLastUpdate(Query<?> query) {
         String sqlQuery = buildLastUpdateStatement(query);
         Connection connection = null;
         Statement statement = null;
         ResultSet result = null;
 
         try {
             connection = openQueryConnection(query);
             statement = connection.createStatement();
             result = executeQueryBeforeTimeout(statement, sqlQuery, getQueryReadTimeout(query));
 
             if (result.next()) {
                 Double date = result.getDouble(1);
                 if (date != null) {
                     return new Date((long) (date * 1000L));
                 }
             }
 
             return null;
 
         } catch (SQLException ex) {
             throw createQueryException(ex, sqlQuery, query);
 
         } finally {
             closeResources(connection, statement, result);
         }
     }
 
     @Override
     public <T> PaginatedResult<T> readPartial(final Query<T> query, long offset, int limit) {
         List<T> objects = selectListWithOptions(
                 vendor.rewriteQueryWithLimitClause(buildSelectStatement(query), limit + 1, offset),
                 query);
 
         int size = objects.size();
         if (size <= limit) {
             return new PaginatedResult<T>(offset, limit, offset + size, objects);
 
         } else {
             objects.remove(size - 1);
             return new PaginatedResult<T>(offset, limit, 0, objects) {
 
                 private Long count;
 
                 @Override
                 public long getCount() {
                     if (count == null) {
                         count = readCount(query);
                     }
                     return count;
                 }
 
                 @Override
                 public boolean hasNext() {
                     return true;
                 }
             };
         }
     }
 
     @Override
     public <T> PaginatedResult<Grouping<T>> readPartialGrouped(Query<T> query, long offset, int limit, String... fields) {
         List<Grouping<T>> groupings = new ArrayList<Grouping<T>>();
         String sqlQuery = buildGroupStatement(query, fields);
         Connection connection = null;
         Statement statement = null;
         ResultSet result = null;
 
         try {
             connection = openQueryConnection(query);
             statement = connection.createStatement();
             result = executeQueryBeforeTimeout(statement, sqlQuery, getQueryReadTimeout(query));
 
             int fieldsLength = fields.length;
             int groupingsCount = 0;
 
             for (int i = 0, last = (int) offset + limit; result.next(); ++ i, ++ groupingsCount) {
                 if (i < offset || i >= last) {
                     continue;
                 }
 
                 long count = ObjectUtils.to(long.class, result.getObject(1));
                 List<Object> keys = new ArrayList<Object>();
                 for (int j = 0; j < fieldsLength; ++ j) {
                     keys.add(result.getObject(j + 2));
                 }
                 groupings.add(new SqlGrouping<T>(keys, query, fields, count));
             }
 
             int groupingsSize = groupings.size();
 
             for (int i = 0; i < fieldsLength; ++ i) {
                 ObjectField field = query.mapEmbeddedKey(getEnvironment(), fields[i]).getField();
 
                 if (field != null) {
                     Map<String, Object> rawKeys = new HashMap<String, Object>();
                     for (int j = 0; j < groupingsSize; ++ j) {
                         rawKeys.put(String.valueOf(j), groupings.get(j).getKeys().get(i));
                     }
 
                     String itemType = field.getInternalItemType();
                     if (ObjectField.RECORD_TYPE.equals(itemType)) {
                         for (Map.Entry<String, Object> entry : rawKeys.entrySet()) {
                             Map<String, Object> ref = new HashMap<String, Object>();
                             ref.put(StateValueUtils.REFERENCE_KEY, entry.getValue());
                             entry.setValue(ref);
                         }
                     }
 
                     Map<?, ?> convertedKeys = (Map<?, ?>) StateValueUtils.toJavaValue(query.getDatabase(), null, field, "map/" + itemType, rawKeys);
                     for (int j = 0; j < groupingsSize; ++ j) {
                         groupings.get(j).getKeys().set(i, convertedKeys.get(String.valueOf(j)));
                     }
                 }
             }
 
             return new PaginatedResult<Grouping<T>>(offset, limit, groupingsCount, groupings);
 
         } catch (SQLException ex) {
             throw createQueryException(ex, sqlQuery, query);
 
         } finally {
             closeResources(connection, statement, result);
         }
     }
 
     /** SQL-specific implementation of {@link Grouping}. */
     private class SqlGrouping<T> extends AbstractGrouping<T> {
 
         private long count;
 
         public SqlGrouping(List<Object> keys, Query<T> query, String[] fields, long count) {
             super(keys, query, fields);
             this.count = count;
         }
 
         // --- AbstractGrouping support ---
 
         @Override
         protected Aggregate createAggregate(String field) {
             throw new UnsupportedOperationException();
         }
 
         @Override
         public long getCount() {
             return count;
         }
     }
 
     @Override
     protected void beginTransaction(Connection connection, boolean isImmediate) throws SQLException {
         connection.setAutoCommit(false);
     }
 
     @Override
     protected void commitTransaction(Connection connection, boolean isImmediate) throws SQLException {
         connection.commit();
     }
 
     @Override
     protected void rollbackTransaction(Connection connection, boolean isImmediate) throws SQLException {
         connection.rollback();
     }
 
     @Override
     protected void endTransaction(Connection connection, boolean isImmediate) throws SQLException {
         connection.setAutoCommit(true);
     }
 
     @Override
     protected void doSaves(Connection connection, boolean isImmediate, List<State> states) throws SQLException {
         List<State> indexStates = null;
         for (State state1 : states) {
             if (Boolean.TRUE.equals(state1.getExtra(SKIP_INDEX_STATE_EXTRA))) {
                 indexStates = new ArrayList<State>();
                 for (State state2 : states) {
                     if (!Boolean.TRUE.equals(state2.getExtra(SKIP_INDEX_STATE_EXTRA))) {
                         indexStates.add(state2);
                     }
                 }
                 break;
             }
         }
 
         if (indexStates == null) {
             indexStates = states;
         }
 
         SqlIndex.Static.deleteByStates(this, connection, indexStates);
         Map<State, String> inRowIndexes = SqlIndex.Static.insertByStates(this, connection, indexStates);
         boolean hasInRowIndex = hasInRowIndex();
         SqlVendor vendor = getVendor();
         double now = System.currentTimeMillis() / 1000.0;
 
         for (State state : states) {
             boolean isNew = state.isNew();
             boolean saveInRowIndex = hasInRowIndex && !Boolean.TRUE.equals(state.getExtra(SKIP_INDEX_STATE_EXTRA));
             UUID id = state.getId();
             UUID typeId = state.getTypeId();
             byte[] dataBytes = null;
             String inRowIndex = inRowIndexes.get(state);
             byte[] inRowIndexBytes = inRowIndex != null ? inRowIndex.getBytes(StringUtils.UTF_8) : new byte[0];
 
             while (true) {
                 if (isNew) {
                     try {
                         if (dataBytes == null) {
                             dataBytes = serializeData(state.getSimpleValues());
                         }
 
                         List<Object> parameters = new ArrayList<Object>();
                         StringBuilder insertBuilder = new StringBuilder();
 
                         insertBuilder.append("INSERT INTO ");
                         vendor.appendIdentifier(insertBuilder, RECORD_TABLE);
                         insertBuilder.append(" (");
                         vendor.appendIdentifier(insertBuilder, ID_COLUMN);
                         insertBuilder.append(",");
                         vendor.appendIdentifier(insertBuilder, TYPE_ID_COLUMN);
                         insertBuilder.append(",");
                         vendor.appendIdentifier(insertBuilder, DATA_COLUMN);
 
                         if (saveInRowIndex) {
                             insertBuilder.append(",");
                             vendor.appendIdentifier(insertBuilder, IN_ROW_INDEX_COLUMN);
                         }
 
                         insertBuilder.append(") VALUES (");
                         vendor.appendBindValue(insertBuilder, id, parameters);
                         insertBuilder.append(",");
                         vendor.appendBindValue(insertBuilder, typeId, parameters);
                         insertBuilder.append(",");
                         vendor.appendBindValue(insertBuilder, dataBytes, parameters);
 
                         if (saveInRowIndex) {
                             insertBuilder.append(",");
                             vendor.appendBindValue(insertBuilder, inRowIndexBytes, parameters);
                         }
 
                         insertBuilder.append(")");
                         Static.executeUpdateWithList(connection, insertBuilder.toString(), parameters);
 
                     } catch (SQLException ex) {
                         if (Static.isIntegrityConstraintViolation(ex)) {
                             isNew = false;
                             continue;
                         } else {
                             throw ex;
                         }
                     }
 
                 } else {
                     List<AtomicOperation> atomicOperations = state.getAtomicOperations();
                     if (atomicOperations.isEmpty()) {
                         if (dataBytes == null) {
                             dataBytes = serializeData(state.getSimpleValues());
                         }
 
                         List<Object> parameters = new ArrayList<Object>();
                         StringBuilder updateBuilder = new StringBuilder();
 
                         updateBuilder.append("UPDATE ");
                         vendor.appendIdentifier(updateBuilder, RECORD_TABLE);
                         updateBuilder.append(" SET ");
                         vendor.appendIdentifier(updateBuilder, TYPE_ID_COLUMN);
                         updateBuilder.append("=");
                         vendor.appendBindValue(updateBuilder, typeId, parameters);
                         updateBuilder.append(",");
 
                         if (saveInRowIndex) {
                             vendor.appendIdentifier(updateBuilder, IN_ROW_INDEX_COLUMN);
                             updateBuilder.append("=");
                             vendor.appendBindValue(updateBuilder, inRowIndexBytes, parameters);
                             updateBuilder.append(",");
                         }
 
                         vendor.appendIdentifier(updateBuilder, DATA_COLUMN);
                         updateBuilder.append("=");
                         vendor.appendBindValue(updateBuilder, dataBytes, parameters);
                         updateBuilder.append(" WHERE ");
                         vendor.appendIdentifier(updateBuilder, ID_COLUMN);
                         updateBuilder.append("=");
                         vendor.appendBindValue(updateBuilder, id, parameters);
 
                         if (Static.executeUpdateWithList(connection, updateBuilder.toString(), parameters) < 1) {
                             isNew = true;
                             continue;
                         }
 
                     } else {
                         Object oldObject = Query.
                                 from(Object.class).
                                 where("_id = ?", id).
                                 using(this).
                                 resolveToReferenceOnly().
                                 option(RETURN_ORIGINAL_DATA_QUERY_OPTION, Boolean.TRUE).
                                 option(USE_READ_DATA_SOURCE_QUERY_OPTION, Boolean.FALSE).
                                 first();
                         if (oldObject == null) {
                             isNew = true;
                             continue;
                         }
 
                         State oldState = State.getInstance(oldObject);
                         UUID oldTypeId = oldState.getTypeId();
                         byte[] oldData = Static.getOriginalData(oldObject);
 
                         for (AtomicOperation operation : atomicOperations) {
                             String field = operation.getField();
                             state.putValue(field, oldState.getValue(field));
                         }
 
                         for (AtomicOperation operation : atomicOperations) {
                             operation.execute(state);
                         }
 
                         dataBytes = serializeData(state.getSimpleValues());
 
                         List<Object> parameters = new ArrayList<Object>();
                         StringBuilder updateBuilder = new StringBuilder();
 
                         updateBuilder.append("UPDATE ");
                         vendor.appendIdentifier(updateBuilder, RECORD_TABLE);
                         updateBuilder.append(" SET ");
                         vendor.appendIdentifier(updateBuilder, TYPE_ID_COLUMN);
                         updateBuilder.append("=");
                         vendor.appendBindValue(updateBuilder, typeId, parameters);
 
                         if (saveInRowIndex) {
                             updateBuilder.append(",");
                             vendor.appendIdentifier(updateBuilder, IN_ROW_INDEX_COLUMN);
                             updateBuilder.append("=");
                             vendor.appendBindValue(updateBuilder, inRowIndexBytes, parameters);
                         }
 
                         updateBuilder.append(",");
                         vendor.appendIdentifier(updateBuilder, DATA_COLUMN);
                         updateBuilder.append("=");
                         vendor.appendBindValue(updateBuilder, dataBytes, parameters);
                         updateBuilder.append(" WHERE ");
                         vendor.appendIdentifier(updateBuilder, ID_COLUMN);
                         updateBuilder.append("=");
                         vendor.appendBindValue(updateBuilder, id, parameters);
                         updateBuilder.append(" AND ");
                         vendor.appendIdentifier(updateBuilder, TYPE_ID_COLUMN);
                         updateBuilder.append("=");
                         vendor.appendBindValue(updateBuilder, oldTypeId, parameters);
                         updateBuilder.append(" AND ");
                         vendor.appendIdentifier(updateBuilder, DATA_COLUMN);
                         updateBuilder.append("=");
                         vendor.appendBindValue(updateBuilder, oldData, parameters);
 
                         if (Static.executeUpdateWithList(connection, updateBuilder.toString(), parameters) < 1) {
                             continue;
                         }
                     }
                 }
 
                 break;
             }
 
             while (true) {
                 if (isNew) {
                     List<Object> parameters = new ArrayList<Object>();
                     StringBuilder insertBuilder = new StringBuilder();
 
                     insertBuilder.append("INSERT INTO ");
                     vendor.appendIdentifier(insertBuilder, RECORD_UPDATE_TABLE);
                     insertBuilder.append(" (");
                     vendor.appendIdentifier(insertBuilder, ID_COLUMN);
                     insertBuilder.append(",");
                     vendor.appendIdentifier(insertBuilder, TYPE_ID_COLUMN);
                     insertBuilder.append(",");
                     vendor.appendIdentifier(insertBuilder, UPDATE_DATE_COLUMN);
                     insertBuilder.append(") VALUES (");
                     vendor.appendBindValue(insertBuilder, id, parameters);
                     insertBuilder.append(",");
                     vendor.appendBindValue(insertBuilder, typeId, parameters);
                     insertBuilder.append(",");
                     vendor.appendBindValue(insertBuilder, now, parameters);
                     insertBuilder.append(")");
 
                     try {
                         Static.executeUpdateWithList(connection, insertBuilder.toString(), parameters);
 
                     } catch (SQLException ex) {
                         if (Static.isIntegrityConstraintViolation(ex)) {
                             isNew = false;
                             continue;
                         } else {
                             throw ex;
                         }
                     }
 
                 } else {
                     List<Object> parameters = new ArrayList<Object>();
                     StringBuilder updateBuilder = new StringBuilder();
 
                     updateBuilder.append("UPDATE ");
                     vendor.appendIdentifier(updateBuilder, RECORD_UPDATE_TABLE);
                     updateBuilder.append(" SET ");
                     vendor.appendIdentifier(updateBuilder, TYPE_ID_COLUMN);
                     updateBuilder.append("=");
                     vendor.appendBindValue(updateBuilder, typeId, parameters);
                     updateBuilder.append(",");
                     vendor.appendIdentifier(updateBuilder, UPDATE_DATE_COLUMN);
                     updateBuilder.append("=");
                     vendor.appendBindValue(updateBuilder, now, parameters);
                     updateBuilder.append(" WHERE ");
                     vendor.appendIdentifier(updateBuilder, ID_COLUMN);
                     updateBuilder.append("=");
                     vendor.appendBindValue(updateBuilder, id, parameters);
 
                     if (Static.executeUpdateWithList(connection, updateBuilder.toString(), parameters) < 1) {
                         isNew = true;
                         continue;
                     }
                 }
 
                 break;
             }
         }
     }
 
     @Override
     protected void doIndexes(Connection connection, boolean isImmediate, List<State> states) throws SQLException {
         SqlIndex.Static.deleteByStates(this, connection, states);
         Map<State, String> inRowIndexes = SqlIndex.Static.insertByStates(this, connection, states);
 
         if (!hasInRowIndex()) {
             return;
         }
 
         SqlVendor vendor = getVendor();
         for (Map.Entry<State, String> entry : inRowIndexes.entrySet()) {
             StringBuilder updateBuilder = new StringBuilder();
             updateBuilder.append("UPDATE ");
             vendor.appendIdentifier(updateBuilder, RECORD_TABLE);
             updateBuilder.append(" SET ");
             vendor.appendIdentifier(updateBuilder, IN_ROW_INDEX_COLUMN);
             updateBuilder.append("=");
             vendor.appendValue(updateBuilder, entry.getValue());
             updateBuilder.append(" WHERE ");
             vendor.appendIdentifier(updateBuilder, ID_COLUMN);
             updateBuilder.append("=");
             vendor.appendValue(updateBuilder, entry.getKey().getId());
             Static.executeUpdateWithArray(connection, updateBuilder.toString());
         }
     }
 
     /** @deprecated Use {@link #index} instead. */
     @Deprecated
     public void fixIndexes(List<State> states) {
         Connection connection = openConnection();
 
         try {
             doIndexes(connection, true, states);
 
         } catch (SQLException ex) {
             List<UUID> ids = new ArrayList<UUID>();
             for (State state : states) {
                 ids.add(state.getId());
             }
             throw new SqlDatabaseException(this, String.format(
                     "Can't index states! (%s)", ids));
 
         } finally {
             closeConnection(connection);
         }
     }
 
     @Override
     protected void doDeletes(Connection connection, boolean isImmediate, List<State> states) throws SQLException {
         SqlVendor vendor = getVendor();
 
         StringBuilder whereBuilder = new StringBuilder();
         whereBuilder.append(" WHERE ");
         vendor.appendIdentifier(whereBuilder, ID_COLUMN);
         whereBuilder.append(" IN (");
 
         for (State state : states) {
             vendor.appendValue(whereBuilder, state.getId());
             whereBuilder.append(",");
         }
 
         whereBuilder.setCharAt(whereBuilder.length() - 1, ')');
 
         StringBuilder deleteBuilder = new StringBuilder();
         deleteBuilder.append("DELETE FROM ");
         vendor.appendIdentifier(deleteBuilder, RECORD_TABLE);
         deleteBuilder.append(whereBuilder);
         Static.executeUpdateWithArray(connection, deleteBuilder.toString());
 
         SqlIndex.Static.deleteByStates(this, connection, states);
 
         StringBuilder updateBuilder = new StringBuilder();
         updateBuilder.append("UPDATE ");
         vendor.appendIdentifier(updateBuilder, RECORD_UPDATE_TABLE);
         updateBuilder.append(" SET ");
         vendor.appendIdentifier(updateBuilder, UPDATE_DATE_COLUMN);
         updateBuilder.append("=");
         vendor.appendValue(updateBuilder, System.currentTimeMillis() / 1000.0);
         updateBuilder.append(whereBuilder);
         Static.executeUpdateWithArray(connection, updateBuilder.toString());
     }
 
     /** Specifies the name of the table for storing target field values. */
     @Documented
     @ObjectField.AnnotationProcessorClass(FieldIndexTableProcessor.class)
     @Retention(RetentionPolicy.RUNTIME)
     @Target(ElementType.FIELD)
     public @interface FieldIndexTable {
         String value();
     }
 
     private static class FieldIndexTableProcessor implements ObjectField.AnnotationProcessor<FieldIndexTable> {
         @Override
         public void process(ObjectType type, ObjectField field, FieldIndexTable annotation) {
             field.getOptions().put(INDEX_TABLE_INDEX_OPTION, annotation.value());
         }
     }
 
     /** {@link SqlDatabase} utility methods. */
     public static final class Static {
 
         private Static() {
         }
 
         public static List<SqlDatabase> getAll() {
             return INSTANCES;
         }
 
         public static void deregisterAllDrivers() {
             for (WeakReference<Driver> driverRef : REGISTERED_DRIVERS) {
                 Driver driver = driverRef.get();
                 if (driver != null) {
                     LOGGER.info("Deregistering [{}]", driver);
                     try {
                         DriverManager.deregisterDriver(driver);
                     } catch (SQLException ex) {
                         LOGGER.warn("Can't deregister [{}]!", driver);
                     }
                 }
             }
         }
 
         /**
          * Log a batch update exception with values.
          */
         static void logBatchUpdateException(BatchUpdateException bue, String sqlQuery, List<? extends List<?>> parameters) {
             int i = 0;
             int failureOffset = bue.getUpdateCounts().length;
             List<?> rowData = parameters.get(failureOffset);
 
             StringBuilder errorBuilder = new StringBuilder();
             errorBuilder.append("Batch update failed with query '");
             errorBuilder.append(sqlQuery);
             errorBuilder.append("' with values (");
             for (Object value : rowData) {
                 if (i++ != 0) {
                     errorBuilder.append(", ");
                 }
 
                 if (value instanceof byte[]) {
                     errorBuilder.append(StringUtils.hex((byte[]) value));
                 } else {
                     errorBuilder.append(value);
                 }
             }
             errorBuilder.append(")");
 
             Exception ex = bue.getNextException() != null ? bue.getNextException() : bue;
             LOGGER.error(errorBuilder.toString(), ex);
         }
 
         static void logUpdateException(String sqlQuery, List<?> parameters) {
             int i = 0;
 
             StringBuilder errorBuilder = new StringBuilder();
             errorBuilder.append("Batch update failed with query '");
             errorBuilder.append(sqlQuery);
             errorBuilder.append("' with values (");
             for (Object value : parameters) {
                 if (i++ != 0) {
                     errorBuilder.append(", ");
                 }
 
                 if (value instanceof byte[]) {
                     errorBuilder.append(StringUtils.hex((byte[]) value));
                 } else {
                     errorBuilder.append(value);
                 }
             }
             errorBuilder.append(")");
 
             LOGGER.error(errorBuilder.toString());
         }
 
         // Safely binds the given parameter to the given statement at the
         // given index.
         private static void bindParameter(PreparedStatement statement, int index, Object parameter) throws SQLException {
             if (parameter instanceof String) {
                 parameter = ((String) parameter).getBytes(StringUtils.UTF_8);
             }
 
             if (parameter instanceof byte[]) {
                 byte[] parameterBytes = (byte[]) parameter;
                 int parameterBytesLength = parameterBytes.length;
                 if (parameterBytesLength > 2000) {
                     statement.setBinaryStream(index, new ByteArrayInputStream(parameterBytes), parameterBytesLength);
                     return;
                 }
             }
 
             statement.setObject(index, parameter);
         }
 
         /**
          * Executes the given batch update {@code sqlQuery} with the given
          * list of {@code parameters} within the given {@code connection}.
          *
          * @return Array of number of rows affected by the update query.
          */
         public static int[] executeBatchUpdate(
                 Connection connection,
                 String sqlQuery,
                 List<? extends List<?>> parameters) throws SQLException {
 
            PreparedStatement prepared = connection.prepareStatement(sqlQuery);
             List<?> currentRow = null;
 
             try {
                 for (List<?> row : parameters) {
                     currentRow = row;
                     int columnIndex = 1;
 
                     for (Object parameter : row) {
                         bindParameter(prepared, columnIndex, parameter);
                         columnIndex++;
                     }
 
                     prepared.addBatch();
                 }
 
                 int[] affected = null;
                 Stats.Timer timer = STATS.startTimer();
 
                 try {
                     return (affected = prepared.executeBatch());
 
                 } finally {
                     double time = timer.stop(UPDATE_OPERATION);
                     if (LOGGER.isDebugEnabled()) {
                         LOGGER.debug(
                                 "SQL batch update: [{}], Parameters: {}, Affected: {}, Time: [{}]ms",
                                 new Object[] { sqlQuery, parameters, affected != null ? Arrays.toString(affected) : "[]", time });
                     }
                 }
 
             } catch (SQLException error) {
                 logUpdateException(sqlQuery, currentRow);
                 throw error;
 
             } finally {
                 try {
                     prepared.close();
                 } catch (SQLException error) {
                 }
             }
         }
 
         /**
          * Executes the given update {@code sqlQuery} with the given
          * {@code parameters} within the given {@code connection}.
          *
          * @return Number of rows affected by the update query.
          */
         public static int executeUpdateWithList(
                 Connection connection,
                 String sqlQuery,
                 List<?> parameters)
                 throws SQLException {
 
             if (parameters == null) {
                 return executeUpdateWithArray(connection, sqlQuery);
 
             } else {
                 Object[] array = parameters.toArray(new Object[parameters.size()]);
                 return executeUpdateWithArray(connection, sqlQuery, array);
             }
         }
 
         /**
          * Executes the given update {@code sqlQuery} with the given
          * {@code parameters} within the given {@code connection}.
          *
          * @return Number of rows affected by the update query.
          */
         public static int executeUpdateWithArray(
                 Connection connection,
                 String sqlQuery,
                 Object... parameters)
                 throws SQLException {
 
             boolean hasParameters = parameters != null && parameters.length > 0;
             PreparedStatement prepared;
             Statement statement;
 
             if (hasParameters) {
                 prepared = connection.prepareStatement(sqlQuery);
                 statement = prepared;
 
             } else {
                 prepared = null;
                 statement = connection.createStatement();
             }
 
             try {
                 if (hasParameters) {
                     for (int i = 0; i < parameters.length; i++) {
                         bindParameter(prepared, i + 1, parameters[i]);
                     }
                 }
 
                 Integer affected = null;
                 Stats.Timer timer = STATS.startTimer();
 
                 try {
                     return (affected = hasParameters ?
                             prepared.executeUpdate() :
                             statement.executeUpdate(sqlQuery));
 
                 } finally {
                     double time = timer.stop(UPDATE_OPERATION);
                     if (LOGGER.isDebugEnabled()) {
                         LOGGER.debug(
                                 "SQL update: [{}], Affected: [{}], Time: [{}]ms",
                                 new Object[] { fillPlaceholders(sqlQuery, parameters), affected, time });
                     }
                 }
 
             } finally {
                 try {
                     statement.close();
                 } catch (SQLException ex) {
                 }
             }
         }
 
         /**
          * Returns {@code true} if the given {@code error} looks like a
          * {@link SQLIntegrityConstraintViolationException}.
          */
         public static boolean isIntegrityConstraintViolation(SQLException error) {
             if (error instanceof SQLIntegrityConstraintViolationException) {
                 return true;
             } else {
                 String state = error.getSQLState();
                 return state != null && state.startsWith("23");
             }
         }
 
         /**
          * Returns the name of the table for storing the values of the
          * given {@code index}.
          */
         public static String getIndexTable(ObjectIndex index) {
             return ObjectUtils.to(String.class, index.getOptions().get(INDEX_TABLE_INDEX_OPTION));
         }
 
         /**
          * Sets the name of the table for storing the values of the
          * given {@code index}.
          */
         public static void setIndexTable(ObjectIndex index, String table) {
             index.getOptions().put(INDEX_TABLE_INDEX_OPTION, table);
         }
 
         public static Object getExtraColumn(Object object, String name) {
             return State.getInstance(object).getExtra(EXTRA_COLUMN_EXTRA_PREFIX + name);
         }
 
         public static byte[] getOriginalData(Object object) {
             return (byte[]) State.getInstance(object).getExtra(ORIGINAL_DATA_EXTRA);
         }
 
         // --- Deprecated ---
 
         /** @deprecated Use {@link #executeUpdateWithArray} instead. */
         @Deprecated
         public static int executeUpdate(
                 Connection connection,
                 String sqlQuery,
                 Object... parameters)
                 throws SQLException {
 
             return executeUpdateWithArray(connection, sqlQuery, parameters);
         }
     }
 }
