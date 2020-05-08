 package de.consistec.syncframework.impl.adapter;
 
 import static de.consistec.syncframework.common.MdTableDefaultValues.FLAG_PROCESSED;
 import static de.consistec.syncframework.common.i18n.MessageReader.read;
 import static de.consistec.syncframework.common.util.PropertiesUtil.defaultIfNull;
 import static de.consistec.syncframework.common.util.PropertiesUtil.readString;
 
 import de.consistec.syncframework.common.Config;
 import de.consistec.syncframework.common.adapter.DatabaseAdapterCallback;
 import de.consistec.syncframework.common.adapter.IDatabaseAdapter;
 import de.consistec.syncframework.common.data.schema.Column;
 import de.consistec.syncframework.common.data.schema.Constraint;
 import de.consistec.syncframework.common.data.schema.ConstraintType;
 import de.consistec.syncframework.common.data.schema.CreateSchemaToSQLConverter;
 import de.consistec.syncframework.common.data.schema.CreateTableToSQLConverter;
 import de.consistec.syncframework.common.data.schema.ISQLConverter;
 import de.consistec.syncframework.common.data.schema.Schema;
 import de.consistec.syncframework.common.data.schema.Table;
 import de.consistec.syncframework.common.exception.SchemaConverterException;
 import de.consistec.syncframework.common.exception.database_adapter.DatabaseAdapterException;
 import de.consistec.syncframework.common.exception.database_adapter.DatabaseAdapterInstantiationException;
 import de.consistec.syncframework.common.util.CollectionsUtil;
 import de.consistec.syncframework.common.util.LoggingUtil;
 import de.consistec.syncframework.common.util.StringUtil;
 import de.consistec.syncframework.impl.i18n.DBAdapterErrors;
 import de.consistec.syncframework.impl.i18n.DBAdapterWarnings;
 
 import java.sql.BatchUpdateException;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Types;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import org.slf4j.Marker;
 import org.slf4j.MarkerFactory;
 import org.slf4j.cal10n.LocLogger;
 
 /**
  * Adapter which matches most relational database systems like MySQL, PostgreSQL and SQLite.
  * <p/>
  * Adapters should <b>NOT</b> be instantiated directly (with {@code new} keyword)! <br/>
  * Adapter class has to be set in frameworks configuration
  * (see {@link de.consistec.syncframework.common.Config#setClientDatabaseAdapter(java.lang.Class) }
  * and {@link de.consistec.syncframework.common.Config#setServerDatabaseAdapter(java.lang.Class) } ) and it will be
  * instantiated together with {@link de.consistec.syncframework.common.SyncContext SyncContext}. <br/>
  * Therefore, adapters not intended for subclassing (e.g. created for only one database ),
  * should be final and have private constructor.
  * Framework will create instant of such a class with help of java reflection.
  * <p/>
  * Extending this class is good starting point to create new adapters.<br/>
  * This adapter should be full compatible with <a href="http://www.postgresql.org/">PostgreSQL</a>,
  * <a href="http://www.sqlite.org/">SQLite</a> and <a href="http://www.mysql.com/">MySQL Community Server</a> databases.
  * Tests for these databases are provided in test jar package. One can launch it choosing appropriate maven profile.
  * <p/>
  * Descendant class, which require more configuration options, can add this options to framework's config file but
  * they have to be preceded with {@code framework.server.db_adapter.} or
  * {@code framework.server.db_adapter.framework.client.db_adapter.}
  * prefix (for server and client providers accordingly).<br/>
  * Names for new options <b style="style: color:red">should</b> be a {@code public static final String}
  * fields in the class.
  * <p/>
  *
  * @author Markus Backes
  * @company Consistec Engineering and Consulting GmbH
  * @date 03.07.12 11:35
  * @since 0.0.1-SNAPSHOT
  */
 public class GenericDatabaseAdapter implements IDatabaseAdapter {
 
     //<editor-fold defaultstate="expanded" desc=" Class fields " >
     //<editor-fold defaultstate="collapsed" desc="---- Properties names -----" >
     /**
      * This option specify jdbc driver class canonical name for a database.
      * <p/>
      * Value: {@value}.
      */
     public static final String PROPS_DRIVER_NAME = "driver";
     /**
      * This option specify jdbc url for database.
      * <p/>
      * Value: {@value}.
      */
     public static final String PROPS_URL = "url";
     /**
      * This option specify the username of the database to connect to.
      * <p/>
      * Value: {@value}.
      */
     public static final String PROPS_SYNC_USERNAME = "user";
     /**
      * This option specify database user password.
      * <p/>
      * Value: {@value}.
      */
     public static final String PROPS_SYNC_PASSWORD = "password";
     /**
      * This option specifies the database username of an external user (unknown to the syncframework).
      * <p/>
      * Value: {@value}.
      */
     public static final String PROPS_EXTERN_USERNAME = "extern.user";
     /**
      * This option specifies the database password of an external user (unknown to the syncframework).
      * <p/>
      * Value: {@value}.
      */
     public static final String PROPS_EXTERN_PASSWORD = "extern.password";
     /**
      * This option specify the database schema to connect to.
      * <p/>
      * Value: {@value}.
      */
     public static final String PROPS_SCHEMA = "schema";
     /**
      * SQLite database property file.
      * Value: {@value}
      */
     public static final String SQLITE_CONFIG_FILE = "/config_sqlite.properties";
     //</editor-fold>
     /**
      * Part of a description of table columns available in a catalog.
      * <p/>
      * COLUMN_NAME represents the columns name.
      * <p/>
      * <
      * p/>
      * Value: {@value}.
      * <p/>
      *
      * @see org.postgresql.jdbc2.AbstractJdbc2DatabaseMetaData.getColumnNamesFromTable((String catalog,
      * String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException
      */
     protected static final String COLUMN_NAME = "COLUMN_NAME";
     /**
      * Part of a description of table columns available in a catalog.
      * <p/>
      * COLUMN_SIZE represents the columns size. For char or date types
      * this is the maximum number of characters, for numeric or decimal types
      * this is precision.
      * <p/>
      * <
      * p/>
      * Value: {@value}.
      * <p/>
      *
      * @see org.postgresql.jdbc2.AbstractJdbc2DatabaseMetaData.getColumnNamesFromTable((String catalog,
      * String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException
      */
     protected static final String COLUMN_SIZE = "COLUMN_SIZE";
     /**
      * Part of a description of table columns available in a catalog.
      * <p/>
      * is NULL allowed?
      * <UL>
      * <LI> columnNoNulls - might not allow NULL values
      * <LI> columnNullable - definitely allows NULL values
      * <LI> columnNullableUnknown - nullability unknown
      * </UL>
      * <p/>
      * Value: {@value}.
      * <p/>
      * @see org.postgresql.jdbc2.AbstractJdbc2DatabaseMetaData.getColumnNamesFromTable((String catalog,
      * String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException
      */
     protected static final String NULLABLE = "NULLABLE";
     /**
      * Part of a description of table columns available in a catalog.
      * <p/>
      * DATA_TYPE represents the SQL type from java.sql.Types
      * <p/>
      * Value: {@value}.
      * <p/>
      *
      * @see org.postgresql.jdbc2.AbstractJdbc2DatabaseMetaData.getColumnNamesFromTable((String catalog,
      * String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException
      */
     protected static final String DATA_TYPE = "DATA_TYPE";
     /**
      * Part of a description of table columns available in a catalog.
      * <p/>
      * DECIMAL_DIGITS represents the number of fractional digits
      * <p/>
      * Value: {@value}.
      * <p/>
      *
      * @see org.postgresql.jdbc2.AbstractJdbc2DatabaseMetaData.getColumnNamesFromTable((String catalog,
      * String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException
      */
     protected static final String DECIMAL_DIGITS = "DECIMAL_DIGITS";
     /**
      * Part of a description of table columns available in a catalog.
      * <p/>
      * TABLE_NAME represents the tables name.
      * <p/>
      * Value: {@value}.
      * <p/>
      *
      * @see org.postgresql.jdbc2.AbstractJdbc2DatabaseMetaData.getColumnNamesFromTable((String catalog,
      * String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException
      */
     protected static final String TABLE_NAME = "TABLE_NAME";
     /**
      * Part of a description of table columns available in a catalog.
      * <p/>
      * PK_NAME represents the primary key name (may be null)
      * <p/>
      * Value: {@value}.
      * <p/>
      *
      * @see org.postgresql.jdbc2.AbstractJdbc2DatabaseMetaData.getPrimaryKeys(String catalog,
      * String schema, String table) throws SQLException
      */
     protected static final String PK_NAME = "PK_NAME";
     /**
      * MDV_COLUMN_SIZE represents the size of mdv column in meta data table.
      * <p/>
      * Value: {@value}.
      * <p/>
      */
     protected static final int MDV_COLUMN_SIZE = 500;
     private static final LocLogger LOGGER = LoggingUtil.createLogger(GenericDatabaseAdapter.class.getCanonicalName());
     private static final Marker FATAL_MARKER = MarkerFactory.getMarker("FATAL");
     private static final Config CONF = Config.getInstance();
     /**
      * Jdbc connection on which adapter operates.
      * <p/>
      * This connection can be an external provided connection, or an internal connection built by the adapter itself.
      */
     protected Connection connection; //NOSONAR
     /**
      * Jdbc connection url used for creating the connection
      * (if no external connection is provided for adapter construction).
      * <p/>
      * <b>Necessary</b> for internal connection creation.
      */
     protected String connectionUrl; //NOSONAR
     /**
      * Jdbc driver name.
      * <p/>
      * <b>Necessary</b> for internal connection creation.
      */
     protected String driverName; //NOSONAR
     /**
      * Database username.
      * <p/>
      * <b>Necessary</b> for internal connection creation.
      */
     protected String username; //NOSONAR
     /**
      * User password.
      * <p/>
      * <b>Necessary</b> for internal connection creation.
      */
     protected String password; //NOSONAR
     /**
      * Name of the database schema.
      * <p/>
      * Used to obtain metadata from jdbc connection.
      */
     protected String schemaOfConnection = "PUBLIC"; //NOSONAR
 
     //</editor-fold>
     //<editor-fold defaultstate="collapsed" desc=" Class constructors " >
     /**
      * Do not create adapter instances directly!.
      * This constructor has scope {@code protected} only to allow subclasses.
      */
     protected GenericDatabaseAdapter() {
         LOGGER.debug("creating new {} ...", getClass().getCanonicalName());
     }
 
     //</editor-fold>
     //<editor-fold defaultstate="expanded" desc=" Class methods " >
     /**
      * Initialize adapter with external connection.
      *
      * @param connection External managed connection (e.g. from application server pool).
      * @see IDatabaseAdapter#init(java.sql.Connection)
      */
     @Override
     public void init(Connection connection) {
         this.connection = connection;
     }
 
     /**
      * This method initializes the adapter object.
      * <p/>
      * This version of init method will create connection object based on connection options provided in
      * {@code adapterConfig} parameter.
      * <p/>
      * If you need a specific routine to initialize this object, you have to override this method.
      * <p/>
      *
      * @param adapterConfig configuration for adapter.
      * @see IDatabaseAdapter#init(java.util.Properties)
      */
     @Override
     public void init(Properties adapterConfig) throws DatabaseAdapterInstantiationException {
 
         LOGGER.debug("initializing {} adapter", getClass().getCanonicalName());
 
         driverName = readString(adapterConfig, PROPS_DRIVER_NAME, true);
         connectionUrl = readString(adapterConfig, PROPS_URL, true);
         username = readString(adapterConfig, PROPS_SYNC_USERNAME, false);
         password = readString(adapterConfig, PROPS_SYNC_PASSWORD, false);
         schemaOfConnection = defaultIfNull(schemaOfConnection, readString(adapterConfig, PROPS_SCHEMA, false));
 
         LOGGER.debug("driverName=\"{}\", connectionUrl=\"{}\", username=\"{}\", password=\"{}\"",
             driverName, connectionUrl, username, password);
 
         createConnection();
 
         LOGGER.debug("{} adapter initialized", getClass().getCanonicalName());
     }
 
     @Override
     public Connection getConnection() {
         return connection;
     }
 
     /**
      * Creates database connection.
      *
      * @throws DatabaseAdapterInstantiationException
      */
     protected void createConnection() throws DatabaseAdapterInstantiationException {
         try {
             Class.forName(driverName);
 
             LOGGER.debug("create connection to {} ", connectionUrl);
 
             if (StringUtil.isNullOrEmpty(password) || StringUtil.isNullOrEmpty(username)) {
                 connection = DriverManager.getConnection(connectionUrl);
             } else {
                 connection = DriverManager.getConnection(connectionUrl, username, password);
             }
         } catch (ClassNotFoundException e) {
             String msg = read(DBAdapterErrors.CANT_LOAD_JDBC_DIRVER, driverName);
             LOGGER.error(FATAL_MARKER, msg, e);
             throw new DatabaseAdapterInstantiationException(msg, e);
         } catch (Exception e) {
             String msg = read(DBAdapterErrors.CANT_CREATE_ADAPTER_INSTANCE, getClass().getCanonicalName());
             LOGGER.error(FATAL_MARKER, msg, e);
             throw new DatabaseAdapterInstantiationException(msg, e);
         }
 
         try {
             connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
         } catch (SQLException e) {
             LOGGER.warn(read(DBAdapterWarnings.CANT_SET_TRANS_ISOLATION_LEVEL, "TRANSACTION_SERIALIZABLE"), e);
         }
     }
 
     @Override
     public void commit() throws DatabaseAdapterException {
         try {
             connection.commit();
         } catch (SQLException e) {
             throw new DatabaseAdapterException(read(DBAdapterErrors.COMMITTING_THE_CONNECTION_FAILS), e);
         }
     }
 
     @Override
     public List<String> getColumnNamesFromTable(String tableName) throws DatabaseAdapterException {
         LOGGER.debug("Reading columns for table {} ", tableName);
 
         ResultSet columns = null; //NOSONAR
         List<String> columnList;
         try {
             columns = connection.getMetaData().getColumns(connection.getCatalog(), getSchemaOfConnection(),
                 tableName, null);
             columnList = CollectionsUtil.newArrayList();
             while (columns.next()) {
                 columnList.add(columns.getString(COLUMN_NAME));
             }
         } catch (SQLException e) {
             throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_READ_TABLE_COLUMNS, tableName), e);
         } finally {
             closeResultSets(columns);
         }
         return columnList;
     }
 
     @Override
     public void applySchema(Schema schema) throws DatabaseAdapterException {
 
         Statement stmt = null; //NOSONAR
         try {
             stmt = connection.createStatement();
             removeExistentTablesFromSchema(schema);
             String sqlSchema = getSchemaConverter().toSQL(schema);
 
             LOGGER.debug("applying schema: {}", sqlSchema);
 
             String[] tableScripts = sqlSchema.split(";");
             for (String tableSql : tableScripts) {
                 stmt.addBatch(tableSql);
             }
             stmt.executeBatch();
         } catch (BatchUpdateException e) {
             throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_APPLY_DB_SCHEMA), e);
         } catch (SQLException e) {
             throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_APPLY_DB_SCHEMA), e);
         } catch (SchemaConverterException e) {
             throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_CONVERT_SCHEMA_TO_SQL), e);
         } finally {
             closeStatements(stmt);
         }
     }
 
     @Override
     public ISQLConverter getSchemaConverter() {
         return new CreateSchemaToSQLConverter();
     }
 
     @Override
     public ISQLConverter getTableConverter() {
         return new CreateTableToSQLConverter();
     }
 
     /**
      * Removes the tables that already exist in the schema to avoid duplication.
      *
      * @param schema schema
      * @throws DatabaseAdapterException
      */
     protected void removeExistentTablesFromSchema(Schema schema) throws DatabaseAdapterException {
         List<String> databaseTables = getTableNamesFromDatabase();
 
         for (Table tab : schema.getTables()) {
             if (databaseTables.contains(tab.getName())) {
                 schema.removeTables(tab);
             }
         }
     }
 
     /**
      * Checks if all configured tables and their md equivalents exist.
      *
      * @return boolean
      * @throws DatabaseAdapterException
      */
     @Override
     public boolean hasSchema() throws DatabaseAdapterException {
 
         List<String> tabsInDb = getTableNamesFromDatabase();
 
         if (tabsInDb.containsAll(CONF.getSyncTables())) {
 
             // check if md tables exists
             List<String> mdTabs = CollectionsUtil.newArrayList(CONF.getSyncTables().size());
             for (String tab : CONF.getSyncTables()) {
                 mdTabs.add(tab + CONF.getMdTableSuffix());
             }
 
             return tabsInDb.containsAll(mdTabs);
         }
         return false;
     }
 
     /**
      * Reads tables names from database schema.
      *
      * @return All tables name from database schema.
      * @throws DatabaseAdapterException
      */
     protected List<String> getTableNamesFromDatabase() throws DatabaseAdapterException {
         ResultSet tables = null; //NOSONAR
         List<String> databaseTables = CollectionsUtil.newArrayList();
         try {
             tables = connection.getMetaData().getTables(connection.getCatalog(), getSchemaOfConnection(), null,
                 new String[]{"TABLE"});
             // create a list with all table names from database
             while (tables.next()) {
                 databaseTables.add(tables.getString(TABLE_NAME).toLowerCase());
             }
         } catch (SQLException ex) {
             throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_READ_TABLES), ex);
         } finally {
             closeResultSets(tables);
         }
         return databaseTables;
     }
 
     @Override
     public Column getPrimaryKeyColumn(String table) throws DatabaseAdapterException {
         ResultSet primaryKeys = null; //NOSONAR
 
         try {
             LOGGER.debug("looking for primary key in the meta data table with catalog: {}, schema: {} and table: {}",
                 connection.getCatalog(), getSchemaOfConnection(), table);
 
             // look for primary key with lowercase table name
             primaryKeys = connection.getMetaData().getPrimaryKeys(connection.getCatalog(),
                 getSchemaOfConnection(), table);
 
             if (primaryKeys.next()) {
                 return createColumnForPrimaryKey(primaryKeys, table);
             } else {
                 // some database store their values in uppercase
                 primaryKeys = connection.getMetaData().getPrimaryKeys(connection.getCatalog(),
                     getSchemaOfConnection(), table.toUpperCase());
                 if (primaryKeys.next()) {
                     return createColumnForPrimaryKey(primaryKeys, table.toUpperCase());
                 }
             }
 
             throw new DatabaseAdapterException(read(DBAdapterErrors.NO_PK_COLUMN, table));
         } catch (SQLException e) {
             throw new DatabaseAdapterException(read(DBAdapterErrors.ERROR_WHILE_LOOKING_FOR_PK, table), e);
         } finally {
             this.closeResultSets(primaryKeys);
         }
     }
 
     private Column createColumnForPrimaryKey(ResultSet primaryKeys, String table) throws
         DatabaseAdapterException {
         ResultSet columns = null;
 
         try {
             String primaryKeyColumnName = primaryKeys.getString(COLUMN_NAME);
             columns = connection.getMetaData().getColumns(connection.getCatalog(), getSchemaOfConnection(), table,
                 null);
 
             while (columns.next()) {
                 if (primaryKeyColumnName.equalsIgnoreCase(columns.getString(COLUMN_NAME))) {
                     Column newColumn = new Column(primaryKeyColumnName, columns.getInt(DATA_TYPE),
                         columns.getInt(COLUMN_SIZE),
                         columns.getInt(DECIMAL_DIGITS), columns.getBoolean(NULLABLE));
                     LOGGER.debug("primary key column for {} is {}", table, newColumn);
                     return newColumn;
                 }
             }
         } catch (SQLException e) {
             throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_APPLY_DB_SCHEMA), e);
         } finally {
             if (columns != null) {
                 try {
                     columns.close();
                 } catch (SQLException e) {
                     throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_APPLY_DB_SCHEMA), e);
                 }
             }
         }
 
         throw new DatabaseAdapterException(read(DBAdapterErrors.NO_PK_COLUMN, table));
     }
 
     @Override
     public int getNextRevision() throws DatabaseAdapterException {
 
         // CALCULATE REVISION
         int rev = getLastRevision() + 1;
 
         LOGGER.debug("next revision is {} ", rev);
 
         return rev;
     }
 
     @Override
     public int getLastRevision() throws DatabaseAdapterException {
 
         int rev = 0;
         Statement stmt = null; //NOSONAR
         ResultSet rst = null; //NOSONAR
         try {
             // CALCULATE REVISION
             String mdTable;
             stmt = connection.createStatement();
 
             for (String table : CONF.getSyncTables()) {
                 mdTable = table + CONF.getMdTableSuffix();
                 rst = stmt.executeQuery(String.format("select MAX(rev) from %s", mdTable));
                 while (rst.next()) {
                     int tmp = rst.getInt(1);
                     if (tmp > rev) {
                         rev = tmp;
                     }
                 }
             }
         } catch (SQLException e) {
             throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_READ_LAST_REVISION), e);
         } finally {
             closeResultSets(rst);
             closeStatements(stmt);
         }
 
         LOGGER.debug("current revision is at {}", rev);
         return rev;
     }
 
     @Override
     public void updateMdRow(final int rev, final int flag, final Object pk, final String mdv, final String tableName)
         throws DatabaseAdapterException {
 
         LOGGER.debug("updating md row with values: pk:{} mdv:{} rev:{} flag: {} tablename:{}{}", pk, mdv, rev, flag,
             tableName, CONF.getMdTableSuffix());
 
         String statement;
 
         statement = String.format("update %s%s SET mdv=?, rev=?, f=? where pk=?", tableName, CONF.getMdTableSuffix());
 
         PreparedStatement updateStatement = null;
         try {
 
             updateStatement = connection.prepareStatement(statement);
 
             if (mdv == null) {
                 updateStatement.setNull(1, Types.VARCHAR);
             } else {
                 updateStatement.setString(1, mdv);
             }
             updateStatement.setInt(2, rev);
             updateStatement.setInt(3, flag); //NOSONAR
             updateStatement.setObject(4, pk);
 
             if (updateStatement.executeUpdate() <= 0) {
                 throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_UPDATE_MD_ROW, tableName));
             }
         } catch (SQLException e) {
             throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_UPDATE_MD_ROW, tableName), e);
         } finally {
             closeStatements(updateStatement);
         }
     }
 
     @Override
     public void deleteRow(Object primaryKey, String tableName) throws DatabaseAdapterException {
 
         String primaryKeyColumnName = this.getPrimaryKeyColumn(tableName).getName();
 
         LOGGER.debug(String.format("deleting row: %s:%s on table %s", primaryKeyColumnName, primaryKey, tableName));
 
         PreparedStatement stmt = null;
 
         try {
 
             stmt = connection.prepareStatement(
                 String.format("delete from %s where %s = ?", tableName, primaryKeyColumnName));
             stmt.setObject(1, primaryKey);
             if (stmt.executeUpdate() <= 0) {
                 LOGGER.warn(DBAdapterWarnings.NO_ROW_DELETED, primaryKey, tableName);
             }
 
         } catch (SQLException e) {
             throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_DELETE_ROW, primaryKey, tableName), e);
         } finally {
             closeStatements(stmt);
         }
     }
 
     @Override
     public void insertMdRow(int rev, int f, Object pk, String mdv, String tableName) throws DatabaseAdapterException {
 
         LOGGER.debug(String.format("inserting md row with values: pk:%s mdv:%s rev:%d flag: %d tablename:%s",
             pk, mdv, rev, f, tableName));
 
         final String statement = String.format("insert into %s (pk,mdv,rev,f) VALUES (?,?,?,?)",
             tableName + CONF.getMdTableSuffix());
         PreparedStatement insertStatement = null;
 
         try {
 
             insertStatement = connection.prepareStatement(statement);
             insertStatement.setObject(1, pk);
             insertStatement.setString(2, mdv);
             if (rev == -1) {
                 insertStatement.setNull(3, Types.INTEGER); //NOSONAR
             } else {
                 insertStatement.setInt(3, rev); //NOSONAR
             }
 
             insertStatement.setInt(4, f); //NOSONAR
 
             if (insertStatement.executeUpdate() <= 0) {
                 throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_INSERT_MD_ROW, tableName));
             }
 
         } catch (SQLException e) {
             throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_INSERT_MD_ROW, tableName), e);
         } finally {
             closeStatements(insertStatement);
         }
     }
 
     @Override
     public void insertDataRow(final Map<String, Object> data, final String tableName) throws DatabaseAdapterException {
 
         String statement = "insert into %s (%s) VALUES (%s)";
         StringBuilder keyString = new StringBuilder();
         StringBuilder valueString = new StringBuilder();
 
         for (Map.Entry<String, Object> column : data.entrySet()) {
             if (0 != keyString.length()) {
                 keyString.append(",");
             }
             keyString.append(column.getKey());
             if (0 != valueString.length()) {
                 valueString.append(",");
             }
             valueString.append("?");
         }
 
         String formattedStatement = String.format(statement, tableName, keyString.toString(), valueString.toString());
         LOGGER.debug(formattedStatement);
 
         PreparedStatement insertStatement = null;
 
         try {
 
             insertStatement = connection.prepareStatement(formattedStatement); //NOSONAR
             int i = 1;
             for (Map.Entry<String, Object> column : data.entrySet()) {
                 insertStatement.setObject(i, column.getValue());
                 i++;
             }
             if (insertStatement.executeUpdate() <= 0) {
                 throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_INSERT_DATA_ROW, tableName));
             }
         } catch (SQLException e) {
             LOGGER.error(read(DBAdapterErrors.CANT_INSERT_DATA_ROW, tableName), e);
             throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_INSERT_DATA_ROW, tableName), e);
         } finally {
             closeStatements(insertStatement);
         }
     }
 
     @Override
     public void updateDataRow(Map<String, Object> data, Object primaryKey, String tableName) throws
         DatabaseAdapterException {
 
         String statement = "update %s SET %s where %s = ?";
         StringBuilder values = new StringBuilder();
         int count = 1;
 
         for (Map.Entry<String, Object> column : data.entrySet()) {
             if (values.length() > 0) {
                 values.append(",");
             }
             values.append(column.getKey());
             values.append("=?");
             count++;
         }
 
         String formattedStatement = String.format(statement, tableName, values.toString(),
             this.getPrimaryKeyColumn(tableName).getName());
         LOGGER.debug(formattedStatement);
 
         int i = 1;
 
         PreparedStatement updateStatement = null;
 
         try {
 
             updateStatement = connection.prepareStatement(formattedStatement); //NOSONAR
             for (Map.Entry<String, Object> column : data.entrySet()) {
                 updateStatement.setObject(i, column.getValue());
                 i++;
             }
 
             updateStatement.setObject(count, primaryKey);
             if (updateStatement.executeUpdate() <= 0) {
                 throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_UPDATE_DATA_ROW, tableName));
             }
 
         } catch (SQLException e) {
             throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_UPDATE_DATA_ROW, tableName), e);
         } finally {
             closeStatements(updateStatement);
         }
     }
 
     @Override
     public void getChangesForRevision(int rev, String table, DatabaseAdapterCallback<ResultSet> callback) throws
         DatabaseAdapterException {
 
         String mdTableName = table + CONF.getMdTableSuffix();
         final String query = String.format("select * from %s left join %s on %s.pk = %s.%s where rev > ?", mdTableName,
             table, mdTableName, table, getPrimaryKeyColumn(table).getName());
 
         LOGGER.debug("reading changes for revision with query: {}", query);
 
         PreparedStatement stmt = null;
         ResultSet rst = null; //NOSONAR
         try {
 
             stmt = connection.prepareStatement(query);
             stmt.setInt(1, rev);
             rst = stmt.executeQuery();
             callback.onSuccess(rst);
 
         } catch (SQLException e) {
             throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_READ_CHANGES_FOR_REVISION, rev, table), e);
         } finally {
             closeResultSets(rst);
             closeStatements(stmt);
         }
     }
 
     @Override
     public void getChangesByFlag(String table, DatabaseAdapterCallback<ResultSet> callback) throws
         DatabaseAdapterException {
 
         String mdTableName = table + CONF.getMdTableSuffix();
         final String query = String.format("select * from %s left join %s on %s.pk = %s.%s where f <> 0", mdTableName,
             table, mdTableName, table, getPrimaryKeyColumn(table).getName());
 
         LOGGER.debug("reading changes by flag with query: {}", query);
 
         PreparedStatement stmt = null;
         ResultSet rst = null; //NOSONAR
         try {
 
             stmt = connection.prepareStatement(query);
             rst = stmt.executeQuery();
             callback.onSuccess(rst);
 
         } catch (SQLException e) {
             throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_READ_CHANGES_FOR_FLAG, table), e);
         } finally {
             closeResultSets(rst);
             closeStatements(stmt);
         }
     }
 
     @Override
     public int updateRevision(int rev, String table, Object pk) throws DatabaseAdapterException {
         int updateCount;
         String statement = String.format("update %s SET rev = ?, f = ? where pk = ?", table);
 
         PreparedStatement stmt = null;
         try {
 
             stmt = connection.prepareStatement(statement);
             stmt.setInt(1, rev);
             stmt.setInt(2, FLAG_PROCESSED);
             stmt.setObject(3, pk);
             updateCount = stmt.executeUpdate();
             return updateCount;
 
         } catch (SQLException e) {
             throw new DatabaseAdapterException(read(DBAdapterErrors.UPDATING_REVISON_FAILED, table), e);
         } finally {
             closeStatements(stmt);
         }
     }
 
     @Override
     public void getRowForPrimaryKey(Object primaryKey, String tableName, DatabaseAdapterCallback<ResultSet> callback)
         throws DatabaseAdapterException {
 
         String primaryKeyName = getPrimaryKeyColumn(tableName).getName();
         String statement = String.format("select * from %s where %s = ?", tableName, primaryKeyName);
 
         PreparedStatement stmt = null; //NOSONAR
         ResultSet rst = null; //NOSONAR
 
         try {
 
             stmt = connection.prepareStatement(statement);
             stmt.setObject(1, primaryKey);
             LOGGER.debug("Executing with pk={}: {}", new Object[]{primaryKey, statement});
             rst = stmt.executeQuery();
             callback.onSuccess(rst);
 
         } catch (SQLException e) {
             throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_READ_THE_ROW, tableName, primaryKey), e);
         } finally {
             closeResultSets(rst);
             closeStatements(stmt);
         }
     }
 
     @Override
     public void getDeletedRowsForTable(String tableName, DatabaseAdapterCallback<ResultSet> callback) throws
         DatabaseAdapterException {
 
         Statement deleteStmt = null; //NOSONAR
         ResultSet deletedRows = null; //NOSONAR
 
         try {
 
             deleteStmt = connection.createStatement();
             String mdTable = tableName + CONF.getMdTableSuffix();
             String tmpPkName = tableName + "." + getPrimaryKeyColumn(tableName).getName();
             deletedRows = deleteStmt.executeQuery(
                 String.format("select rev, pk, mdv, f from %s left join %s on %s.pk = %s where %s is null", mdTable,
                 tableName, mdTable, tmpPkName, tmpPkName));
             callback.onSuccess(deletedRows);
 
         } catch (SQLException e) {
             throw new DatabaseAdapterException(read(DBAdapterErrors.ERROR_SEARCHING_DELETED_ROWS, tableName), e);
         } finally {
             closeResultSets(deletedRows);
             closeStatements(deleteStmt);
         }
     }
 
     @Override
     public void getAllRowsFromTable(String table, DatabaseAdapterCallback<ResultSet> callback) throws
         DatabaseAdapterException {
 
         String statement = String.format("select * from %s", table);
         PreparedStatement stat = null;
         ResultSet rows = null; //NOSONAR
         try {
 
             stat = connection.prepareStatement(statement);
             rows = stat.executeQuery();
             callback.onSuccess(rows);
 
         } catch (SQLException e) {
             throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_GET_ALL_ROWS, table), e);
         } finally {
             closeResultSets(rows);
             closeStatements(stat);
         }
     }
 
     @Override
     public Schema getSchema() throws DatabaseAdapterException {
         try {
             String catalog = connection.getCatalog();
             DatabaseMetaData metaData = connection.getMetaData();
             return buildSchema(catalog, metaData);
         } catch (SQLException e) {
             throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_BUILD_SCHEMA), e);
         }
     }
 
     private Schema buildSchema(String catalog, DatabaseMetaData metaData) throws DatabaseAdapterException {
         Schema schema = new Schema();
         ResultSet columns = null; //NOSONAR
         ResultSet primaryKeys = null; //NOSONAR
         Table table;
         Column column;
         Constraint constraint;
         try {
             for (String tableName : CONF.getSyncTables()) {
                 table = new Table(tableName);
                 columns = metaData.getColumns(catalog, getSchemaOfConnection(), tableName, null);
                 while (columns.next()) {
                     column = new Column(columns.getString(COLUMN_NAME), columns.getInt(DATA_TYPE));
                     if ("0".equalsIgnoreCase(columns.getString(NULLABLE))) {
                         column.setNullable(false);
                     }
                     column.setSize(columns.getInt(COLUMN_SIZE));
                     column.setDecimalDigits(columns.getInt(DECIMAL_DIGITS));
                     table.add(column);
                 }
 
                 primaryKeys = metaData.getPrimaryKeys(catalog, getSchemaOfConnection(), tableName);
                 while (primaryKeys.next()) {
                     constraint = new Constraint(ConstraintType.PRIMARY_KEY, primaryKeys.getString(PK_NAME),
                         primaryKeys.getString(COLUMN_NAME));
                     table.add(constraint);
                 }
 
                 schema.addTables(table);
             }
         } catch (SQLException e) {
             throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_APPLY_DB_SCHEMA), e);
         } finally {
             closeResultSets(columns, primaryKeys);
         }
         return schema;
     }
 
     @Override
     public void createClientMDSchema() throws DatabaseAdapterException {
         for (String tableName : CONF.getSyncTables()) {
             if (!existsMDTable(tableName)) {
                 createClientMDTable(tableName);
             }
         }
     }
 
     @Override
     public void createClientMDTable(final String tableName) throws DatabaseAdapterException {
         String mdTableName = tableName + CONF.getMdTableSuffix();
         LOGGER.debug("creating new metadata table: {}", mdTableName);
 
         Column pkColumn = getPrimaryKeyColumn(tableName);
         Table mdTable = new Table(mdTableName);
         mdTable.add(new Column("pk", pkColumn.getType(), pkColumn.getSize(), pkColumn.getDecimalDigits(), false));
         mdTable.add(new Column("mdv", Types.VARCHAR, MDV_COLUMN_SIZE, 0, true));
         mdTable.add(new Column("rev", Types.INTEGER, 0, 0, true));
         mdTable.add(new Column("f", Types.INTEGER, 0, 0, false));
         mdTable.add(new Constraint(ConstraintType.PRIMARY_KEY, "MDPK", "pk"));
 
         try {
             String sqlTableStatement = getTableConverter().toSQL(mdTable);
             executeSqlQuery(sqlTableStatement);
         } catch (SchemaConverterException e) {
             throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_CONVERT_SCHEMA_TO_SQL), e);
         }
     }
 
     @Override
     public void createMDSchema() throws DatabaseAdapterException {
         for (String tableName : CONF.getSyncTables()) {
             if (!existsMDTable(tableName)) {
                 createMDTable(tableName);
             }
         }
     }
 
     @Override
     public void createMDTable(final String tableName) throws DatabaseAdapterException {
         String mdTableName = tableName + CONF.getMdTableSuffix();
         LOGGER.debug("creating new metadata table: {}", mdTableName);
 
         Column pkColumn = getPrimaryKeyColumn(tableName);
         Table mdTable = new Table(mdTableName);
         mdTable.add(new Column("pk", pkColumn.getType(), pkColumn.getSize(), pkColumn.getDecimalDigits(), false));
         mdTable.add(new Column("mdv", Types.VARCHAR, MDV_COLUMN_SIZE, 0, true));
         mdTable.add(new Column("rev", Types.INTEGER, 0, 0, true));
         mdTable.add(new Column("f", Types.INTEGER, 0, 0, false));
         mdTable.add(new Constraint(ConstraintType.PRIMARY_KEY, "MDPK", "pk"));
 
         try {
             String sqlTableStatement = getTableConverter().toSQL(mdTable);
             executeSqlQuery(sqlTableStatement);
         } catch (SchemaConverterException e) {
             throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_CONVERT_SCHEMA_TO_SQL), e);
         }
     }
 
     /**
      * Returns true if the corresponding metadata table exists.
     * @param table the table name
      * @return true if exists
      * @throws DatabaseAdapterException
      */
     @Override
     public boolean existsMDTable(final String tableName) throws DatabaseAdapterException {
         String mdTableName = tableName + CONF.getMdTableSuffix();
         List<String> tableNames = getTableNamesFromDatabase();
         return tableNames.contains(mdTableName);
     }
 
     /**
      * Executes a single SQL query.
      * <p/>
      * @param query the query to execute
      * @throws DatabaseAdapterException
      */
     protected void executeSqlQuery(String query) throws DatabaseAdapterException {
         executeSqlQueries(new String[]{query});
     }
 
     /**
      * Executes many SQL queries, one after the other.
      * <p/>
      * @param queries the queries to execute
      * @throws DatabaseAdapterException
      */
     protected void executeSqlQueries(String[] queries) throws DatabaseAdapterException {
         Statement stmt = null; //NOSONAR
         try {
             stmt = connection.createStatement();
             for (String query : queries) {
                 if (query != null && !query.startsWith("--") && !query.trim().isEmpty()) {
                     stmt.execute(query);
                 }
             }
         } catch (SQLException e) {
             throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_APPLY_DB_SCHEMA), e);
         } finally {
             closeStatements(stmt);
         }
     }
 
     /**
      * Executes many SQL queries, one after the other.
      * <p/>
      * @param queries the queries to execute
      * @throws DatabaseAdapterException
      */
     protected void executeBatch(String[] queries) throws DatabaseAdapterException {
         Statement stmt = null; //NOSONAR
         try {
             stmt = connection.createStatement();
             for (String query : queries) {
                 stmt.addBatch(query);
             }
             stmt.executeBatch();
         } catch (SQLException e) {
             throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_APPLY_DB_SCHEMA), e);
         } finally {
             closeStatements(stmt);
         }
     }
 
     /**
      * Closes the given Statements and wraps raising SQLExceptions to DatabaseAdapterException.
      *
      * @param statements The Statements to close
      * @throws DatabaseAdapterException
      */
     protected void closeStatements(Statement... statements) throws DatabaseAdapterException {
         if (statements != null) {
             for (Statement stmt : statements) {
                 try {
                     if (stmt != null) {
                         stmt.close();
                     }
                 } catch (SQLException e) {
                     throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_CLOSE_STATEMENT), e);
                 }
             }
         }
     }
 
     /**
      * Closes the given Statements and wraps raising SQLExceptions to DatabaseAdapterException.
      *
      * @param resultSets The ResultSets to close
      * @throws DatabaseAdapterException
      */
     protected void closeResultSets(ResultSet... resultSets) throws DatabaseAdapterException {
         if (resultSets != null) {
             for (ResultSet rst : resultSets) {
                 try {
                     if (rst != null) {
                         rst.close();
                     }
                 } catch (SQLException e) {
                     throw new DatabaseAdapterException(read(DBAdapterErrors.CANT_CLOSE_RESULTSET), e);
                 }
             }
         }
     }
 
     /**
      * Return database schema to which point adapter's connection.
      *
      * @return Database schema.
      */
     protected String getSchemaOfConnection() {
         return schemaOfConnection;
     }
 
     /**
      * Description of object's state.
      * <p/>
      * E.g.
      * <code>
      * GenericDatabaseAdapter{connection=initialised, connectionUrl=null, driverName=driver.class ....}
      * </code>
      * <p/>
      * This description is a subject of change.
      *
      * @return Description of object's state.
      */
     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder(getClass().getSimpleName());
         builder.append("{\nconnection");
         builder.append(connection == null ? "null" : "initialized");
         builder.append(",\n connectionUrl=");
         builder.append(connectionUrl);
         builder.append(",\n driverName=");
         builder.append(driverName);
         builder.append(",\n username=");
         builder.append(
             StringUtil.isNullOrEmpty(username) ? "null or empty" : "has value (not printed for security reasons)");
         builder.append(",\n password=");
         builder.append(
             StringUtil.isNullOrEmpty(password) ? "null or empty" : "has value (not printed for security reasons)");
         builder.append(" }");
         return builder.toString();
     }
     //</editor-fold>
 }
