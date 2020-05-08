 package org.jbundle.base.db.jdbc;
 
 /**
  * @(#)JDBCDatabase.java    1.16 95/12/14 Don Corley
  *
  * Copyright (c) 2009 tourapp.com. All Rights Reserved.
  *      don@tourgeek.com
  */
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.ListResourceBundle;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.ConnectionPoolDataSource;
 import javax.sql.DataSource;
 
 import org.jbundle.base.db.BaseDatabase;
 import org.jbundle.base.db.BaseTable;
 import org.jbundle.base.db.DatabaseException;
 import org.jbundle.base.db.PassThruTable;
 import org.jbundle.base.db.QueryRecord;
 import org.jbundle.base.db.QueryTable;
 import org.jbundle.base.db.Record;
 import org.jbundle.base.db.RecordOwner;
 import org.jbundle.base.db.SQLParams;
 import org.jbundle.base.db.jdbc.datasource.DatasourceFactory;
 import org.jbundle.base.db.jdbc.properties.DBProperties_access;
 import org.jbundle.base.db.jdbc.properties.DBProperties_instantdb;
 import org.jbundle.base.db.jdbc.properties.DBProperties_mysql;
 import org.jbundle.base.db.jdbc.properties.DBProperties_pointbase;
 import org.jbundle.base.db.jdbc.properties.DBProperties_postgresql;
 import org.jbundle.base.db.jdbc.properties.DBProperties_sybase;
 import org.jbundle.base.field.DateTimeField;
 import org.jbundle.base.util.DBConstants;
 import org.jbundle.base.util.DBParams;
 import org.jbundle.base.util.DatabaseOwner;
 import org.jbundle.base.util.Debug;
 import org.jbundle.base.util.Environment;
 import org.jbundle.base.util.Utility;
 import org.jbundle.main.db.DatabaseInfo;
 import org.jbundle.model.DBException;
 import org.jbundle.thin.base.db.Converter;
 /**
  * JDBCDatabase - Implement the JDBC database.
  * <p />JDBCDatabases require a few parameters to use non-default behavior:<br />
  * databaseproduct - Set this if you want the setup to be automatic.<br />
  * jdbcdriver - The JDBC Driver class name.<br />
  * jdbcurl - The JDBC URL with &lt;dbname/&ft; in the URL to replace the database name.<br />
  * username - DB Username.<br />
  * password - DB Password.
  * <p />By default all transactions auto-commit. If you want to use transactions, you must:<br />
  * 1. Set the AUTO_COMMIT_PARAM property of your recordowner to FALSE.<br />
  * 2. Open your files<br />
  * 3. Do your transactions<br />
  * 4. Call record.getTable().getDatabase().commit();
  *
  * @version 1.0.0
  * @author    Don Corley
  */
 public class JdbcDatabase extends BaseDatabase
 {
     /**
      * The JDBC driver. This is only used to keep from having to load the driver every time.
      * But this limits the system to only one JDBC driver - easily fixed if needs change.
      */
     private static Object m_classDB = null;
     /**
      * The JDBC driver. This is only used to keep from having to load the driver every time.
      * But this limits the system to only one JDBC driver - easily fixed if needs change.
      */
     private static InitialContext m_initialContext = null;
     /**
      *  Only used for DB's that can't keep two DB's open, such as pointbase.
      */
     private static Connection m_sharedConnection = null;
     /**
      * The JDBC Connection.
      */
     protected Connection m_JDBCConnection = null;
     /**
      * For debugging.
      */
     public static String JDBC_CATEGORY = JdbcDatabase.class.getName();
     /**
      * The datasource factory for this database.
      */
     public DatasourceFactory m_datasourceFactory = null;
     /**
      * Use these properties until you figure out what kind of DB you are connecting to.
      * If a JDBC_DRIVER_PARAM (jdbcdriver) is not specified, this object will have
      * the driver name.
      */
     public static String DEFAULT_DATABASE_PRODUCT = "mysql";    // My SQL
     /**
      * Maximum pooled connections.
      */
     public int MAX_POOLED_CONNECTIONS = 20;
     /**
      * Maximum pooled connections.
      */
     public int POOL_TIMEOUT = 60;
 
     /**
      * Constructor.
      */
     public JdbcDatabase()
     {
         super();
     }
     /**
      * Constructor.
      * @param databaseOwner My databaseOwner.
      * @param strDBName The database name.
      * @param iDatabaseType The database type (LOCAL/REMOTE).
      */
     public JdbcDatabase(DatabaseOwner databaseOwner, String strDbName, int iDatabaseType)
     {
         this();
         this.init(databaseOwner, strDbName, iDatabaseType, null);
     }
     /**
      * Initialize this database and add it to the databaseOwner.
      * @param databaseOwner My databaseOwner.
      * @param iDatabaseType The database type (LOCAL/REMOTE).
      * @param strDBName The database name.
      */
     public void init(DatabaseOwner databaseOwner, String strDbName, int iDatabaseType, Map<String, Object> properties)
     {
         super.init(databaseOwner, strDbName, iDatabaseType, properties);
         // Typically, JDBC databases do not support locking (Change to "true" otherwise on JDBC open)
         this.setProperty(SQLParams.EDIT_DB_PROPERTY, SQLParams.DB_EDIT_NOT_SUPPORTED);
         m_bAutosequenceSupport = false;     // JDBC databases generally do not support identity, and usually I don't use this capability.
         
         // The following settings are true because JDBC is always running over a LAN (and the server version should send and receive messages).
         this.setProperty(DBParams.MESSAGES_TO_REMOTE, DBConstants.TRUE);   // Sent by server version
         this.setProperty(DBParams.CREATE_REMOTE_FILTER, DBConstants.TRUE);  // Yes, for remote tables
         this.setProperty(DBParams.UPDATE_REMOTE_FILTER, DBConstants.TRUE); // Updated by server version
     }
     /**
      * Free the objects.
      */
     public void free()
     {
         super.free(); // Do any inherited
     } 
     /**
      * Open the physical database.
      * @exception DBException On open errors.
      */
     public void open() throws DBException
     {
         if (m_JDBCConnection == null)
         {
             String strJdbcDriver = this.getProperty(SQLParams.JDBC_DRIVER_PARAM);
             if ((strJdbcDriver == null) || (strJdbcDriver.length() == 0))
             { // If a JDBC driver isn't specified, set up for the default driver
                 String strDataSource = this.getProperty(SQLParams.DATASOURCE_PARAM);
                 if (strDataSource == null)
                     strDataSource = this.getProperty(SQLParams.DEFAULT_DATASOURCE_PARAM);
                 if (strDataSource == null)
                     strDataSource = SQLParams.DEFAULT_DATASOURCE;
                 boolean bDataSourceConnection = false;
                 if (strDataSource != null)
                     bDataSourceConnection = this.setupDataSourceConnection(strDataSource);
 
                 String strDatabaseProductName = null;
                 try {
 					if (m_JDBCConnection != null)
 					{
 						strDatabaseProductName = m_JDBCConnection.getMetaData().getDatabaseProductName();
 						strDatabaseProductName = this.fixDatabaseProductName(strDatabaseProductName);
 					}
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
                 if ((strDatabaseProductName == null) || (strDatabaseProductName.length() == 0))
                 	strDatabaseProductName = this.getProperty(SQLParams.DATABASE_PRODUCT_PARAM);
                 if ((strDatabaseProductName == null) || (strDatabaseProductName.length() == 0))
                     strDatabaseProductName = DEFAULT_DATABASE_PRODUCT;
                 this.setProperty(SQLParams.INTERNAL_DB_NAME, strDatabaseProductName);
                 this.setupDatabaseProperties(); // If I haven't read the db properties, read them now!
 
                 if (!bDataSourceConnection)
                 {	// Datasource connection could be supplied in the db properties
                     String strDBDataSource = this.getProperty(SQLParams.DATASOURCE_PARAM);
                     if (strDBDataSource == null)
                     	strDBDataSource = this.getProperty(SQLParams.DEFAULT_DATASOURCE_PARAM);
                     if (strDBDataSource != null)
                     	if (!strDBDataSource.equalsIgnoreCase(strDataSource))
                         	bDataSourceConnection = this.setupDataSourceConnection(strDBDataSource);                	
                 }
                 if (!bDataSourceConnection)
                     bDataSourceConnection = this.setupDirectDataSourceConnection();
                 if (!bDataSourceConnection)
                     strJdbcDriver = this.getProperty(SQLParams.JDBC_DRIVER_PARAM);  // Default driver
             }
             if (strJdbcDriver != null)
                 this.setupJDBCConnection(strJdbcDriver);
             this.initConnection();
             this.loadDatabaseProperties();
         }
         
         super.open(); // Do any inherited
     }
     /**
      * Fix the database product name.
      * @param strDatabaseProductName
      * @return
      */
     public String fixDatabaseProductName(String strDatabaseProductName)
     {
     	if (strDatabaseProductName == null)
     		return null;
     	if (strDatabaseProductName.lastIndexOf(' ') != -1)
     		strDatabaseProductName = strDatabaseProductName.substring(strDatabaseProductName.lastIndexOf(' ') + 1);
     	return strDatabaseProductName.toLowerCase();
     }
     /**
      * Open the physical database.
      * @exception DBException On open errors.
      */
     public boolean setupDirectDataSourceConnection() throws DBException
     {
         if (m_JDBCConnection != null)
             return true;
         try {
             if (m_datasourceFactory == null)
             {
                 String strClassName = this.getProperty(SQLParams.DATASOURCE_FACTORY);
             	strClassName = Utility.getFullClassName(strClassName);
                 try {
              	    Class<?> c = Class.forName(strClassName);
              	    m_datasourceFactory = (DatasourceFactory)c.newInstance();
                 } catch (Exception e) {
             		Utility.getLogger().warning("Error on create class: " + strClassName);
             		e.printStackTrace();
                 }
             }
             if (m_datasourceFactory == null)
                 return false;   // No factory
             ConnectionPoolDataSource poolDataSource = m_datasourceFactory.getPooledDataSource(this);
             if (poolDataSource != null)
             	if (poolDataSource.getPooledConnection() != null)
             		m_JDBCConnection = poolDataSource.getPooledConnection().getConnection();
             if (m_JDBCConnection == null)
             {   // Otherwise, just try plain datasource connection
                 DataSource dataSource = m_datasourceFactory.getFakePooledDataSource(this);
                 if (dataSource != null)
                     m_JDBCConnection = dataSource.getConnection();
             }
             if (m_JDBCConnection == null)
             {   // Otherwise, just try plain datasource connection
                 DataSource dataSource = m_datasourceFactory.getDataSource(this);
                 if (dataSource != null)
                     m_JDBCConnection = dataSource.getConnection();
                 else
                     return false;   // Failure
             }
         } catch (SQLException e) {
             return false;   // Failure
         }
         return true;    // Success!
     }
     /**
      * Open the physical database.
      * @exception DBException On open errors.
      */
     public boolean setupDataSourceConnection(String strDataSource) throws DBException
     {
         if (m_JDBCConnection != null)
             return true;
         if ((DBConstants.TRUE.equalsIgnoreCase(this.getProperty(DBParams.SERVLET)))
                 || ((this.getDatabaseOwner() != null) && (DBConstants.TRUE.equalsIgnoreCase(this.getDatabaseOwner().getEnvironment().getProperty(DBParams.SERVLET)))))
         {   // Datasource connections are only for servlets (NOTE: Letting this code run otherwise messes stuff up)
             try {
                 if (m_initialContext == null)
                     m_initialContext = new InitialContext();
                 String strDatabaseName = this.getDatabaseName(true);
                 strDataSource = Utility.replace(strDataSource, "{dbname}", strDatabaseName);
                 DataSource ds = (DataSource)m_initialContext.lookup(strDataSource);
                 if (ds == null)
                     return false;   // Failure
                 m_JDBCConnection = ds.getConnection();
             } catch (NamingException e) {
                 return false;   // Failure
             } catch (SQLException e) {
                 return false;   // Failure
             } catch (NoClassDefFoundError e) {
                 return false;   // Failure
             } catch (Exception e) {
                 return false;   // Failure
             }
             return true;    // Success!
         }
         else
             return false;   // Not supported
     }
     /**
      * Open the physical database.
      * @exception DBException On open errors.
      */
     public void setupJDBCConnection(String strJdbcDriver) throws DBException
     {
         if (m_JDBCConnection != null)
             return;
 
         if (m_classDB == null)
         	m_classDB = Utility.makeObjectFromClassName(strJdbcDriver);
         Utility.getLogger().info("Driver found: " + (m_classDB != null));
         m_JDBCConnection = this.getJDBCConnection();    // Setup the initial connection
     }
     /**
      * Close the physical database.
      */
     public void close()
     {
         super.close();  // Do any inherrited
         try   {
             if (m_JDBCConnection != null)
                 m_JDBCConnection.close();
         } catch (SQLException ex) {
                 // Ignore error
         }
         m_JDBCConnection = null;
     } 
 
     private boolean m_bFirstErrorCheck = false;
     protected String m_strFileNotFoundErrorText = null;
     protected String m_strFileNotFoundErrorCode = null;
     protected String m_strDBNotFoundErrorText = null;
     protected String m_strDBNotFoundErrorCode = null;
     protected String m_strDNotFoundErrorCode = null;
     protected String m_strDuplicateKeyErrorText = null;
     protected String m_strDuplicateKeyErrorCode = null;
     protected String m_strKeyNotFoundErrorText = null;
     protected String m_strKeyNotFoundErrorCode = null;
     protected String m_strRecordLockedErrorText = null;
     protected String m_strRecordLockedErrorCode = null;
     protected String m_strBrokenPipeErrorText = null;
     protected String m_strBrokenPipeErrorCode = null;
     /**
      * Convert this error to a DBException.
      * @param ex The exception to convert (usually a SQL exception).
      * @param The DBException.
      */
     public DatabaseException convertError(Exception ex)
     {
         DatabaseException eDB = null;
         int iError = DBConstants.ERROR_RETURN;
         String strMessage = "Error!";
         if (ex != null)
             strMessage = ex.getMessage();
         if (ex instanceof SQLException)
         {
             SQLException sqlex = (SQLException)ex;
             String strState = sqlex.getSQLState();
             strMessage += strState;
             if (!m_bFirstErrorCheck)
             {
                 m_bFirstErrorCheck = true;
                 m_strFileNotFoundErrorText = (String)this.getProperties().get(SQLParams.TABLE_NOT_FOUND_ERROR_TEXT);
                 if (m_strFileNotFoundErrorText != null)
                     m_strFileNotFoundErrorText = m_strFileNotFoundErrorText.toLowerCase();
                 m_strFileNotFoundErrorCode = (String)this.getProperties().get(SQLParams.TABLE_NOT_FOUND_ERROR_CODE);
                 m_strDBNotFoundErrorText = (String)this.getProperties().get(SQLParams.DB_NOT_FOUND_ERROR_TEXT);
                 if (m_strDBNotFoundErrorText != null)
                     m_strDBNotFoundErrorText = m_strDBNotFoundErrorText.toLowerCase();
                 m_strDBNotFoundErrorCode = (String)this.getProperties().get(SQLParams.DB_NOT_FOUND_ERROR_CODE);
                 m_strDuplicateKeyErrorText = (String)this.getProperties().get(SQLParams.DUPLICATE_KEY_ERROR_TEXT);
                 if (m_strDuplicateKeyErrorText != null)
                     m_strDuplicateKeyErrorText = m_strDuplicateKeyErrorText.toLowerCase();
                 m_strDuplicateKeyErrorCode = (String)this.getProperties().get(SQLParams.DUPLICATE_KEY_ERROR_CODE);
                 m_strKeyNotFoundErrorText = (String)this.getProperties().get(SQLParams.KEY_NOT_FOUND_ERROR_TEXT);
                 if (m_strKeyNotFoundErrorText != null)
                     m_strKeyNotFoundErrorText = m_strDuplicateKeyErrorText.toLowerCase();
                 m_strKeyNotFoundErrorCode = (String)this.getProperties().get(SQLParams.KEY_NOT_FOUND_ERROR_CODE);
                 m_strRecordLockedErrorText = (String)this.getProperties().get(SQLParams.RECORD_LOCKED_ERROR_TEXT);
                 if (m_strRecordLockedErrorText != null)
                     m_strRecordLockedErrorText = m_strRecordLockedErrorText.toLowerCase();
                 m_strRecordLockedErrorCode = (String)this.getProperties().get(SQLParams.RECORD_LOCKED_ERROR_CODE);
                 m_strBrokenPipeErrorText = (String)this.getProperties().get(SQLParams.BROKEN_PIPE_ERROR_TEXT);
                 if (m_strBrokenPipeErrorText != null)
                     m_strBrokenPipeErrorText = m_strBrokenPipeErrorText.toLowerCase();
                 m_strBrokenPipeErrorCode = (String)this.getProperties().get(SQLParams.BROKEN_PIPE_ERROR_CODE);
             }
                 // Yeah, I know this is slow, but at least exceptions are not that common.
             if (this.checkForError(ex, DBConstants.FILE_NOT_FOUND, m_strFileNotFoundErrorText, m_strFileNotFoundErrorCode))
                 iError = DBConstants.FILE_NOT_FOUND;
             if (this.checkForError(ex, DBConstants.DB_NOT_FOUND, m_strDBNotFoundErrorText, m_strDBNotFoundErrorCode))
                 iError = DBConstants.DB_NOT_FOUND;
             if (this.checkForError(ex, DBConstants.DUPLICATE_KEY, m_strDuplicateKeyErrorText, m_strDuplicateKeyErrorCode))
                 iError = DBConstants.DUPLICATE_KEY;
             if (this.checkForError(ex, DBConstants.DUPLICATE_COUNTER, m_strDuplicateKeyErrorText, m_strDuplicateKeyErrorCode))
                 iError = DBConstants.DUPLICATE_COUNTER;
             if (this.checkForError(ex, DBConstants.KEY_NOT_FOUND, m_strKeyNotFoundErrorText, m_strKeyNotFoundErrorCode))
                 iError = DBConstants.KEY_NOT_FOUND;
             if (this.checkForError(ex, DBConstants.RECORD_LOCKED, m_strRecordLockedErrorText, m_strRecordLockedErrorCode))
                 iError = DBConstants.RECORD_LOCKED;
             if (this.checkForError(ex, DBConstants.BROKEN_PIPE, m_strBrokenPipeErrorText, m_strBrokenPipeErrorCode))
                 iError = DBConstants.BROKEN_PIPE;
         }
         if (iError == DBConstants.ERROR_RETURN)
         {
             Utility.getLogger().info("Unknown sql exception: " + strMessage);
             eDB = new DatabaseException(strMessage);
         }
         else if (iError == DBConstants.RECORD_LOCKED)
             eDB = new DatabaseException(iError, strMessage);    // On locked, tells user/computer
         else
             eDB = new DatabaseException(iError);    // Standard error
         return eDB;
     }
     /**
      * Check out this error, see if it matches.
      * Note: This is not very efficient code, but error should not happen that often.
      * @param ex exception thrown.
      * @param iErrorType see if error matches.
      * @param strErrorText String to match in the error string (must be lower case).
      * @return true if file was not found and was successfully created.
      */
     public boolean checkForError(Exception ex, int iErrorType, String strErrorText, String strErrorCode)
     {
         boolean bFound = false;
         String strError = ex.getMessage();
         if (strError != null)
             strError = strError.toLowerCase();
         if (iErrorType == DBConstants.FILE_NOT_FOUND)
         {
             if (strError != null)
                 if (strError.indexOf("table") != -1)
                 if (strError.indexOf("not") != -1)
                 if ((strError.indexOf("found") != -1) || (strError.indexOf("find") != -1))
                     bFound = true;
         }
         if (iErrorType == DBConstants.DUPLICATE_KEY)
         {
             if (strError != null)
                 if ((strError.indexOf("duplicate") != -1)
                     || (strError.indexOf("already exists") != -1))
                         bFound = true;
         }
         if (iErrorType == DBConstants.DUPLICATE_COUNTER)
         {
             if (strError != null)
                 if ((strError.indexOf("duplicate") != -1)
                     || (strError.indexOf("already exists") != -1))
                         if ((strError.indexOf("key 1") != -1)
                         	|| (strError.indexOf("primary") != -1)
                             || (strError.indexOf("id") != -1))
                                 bFound = true;
         }
         if (iErrorType == DBConstants.BROKEN_PIPE)
         {
             if (strError != null)
                 if ((strError.indexOf("timeout") != -1)
                     || (strError.indexOf("time-out") != -1)
                     || (strError.indexOf("time out") != -1)
                     || ((strError.indexOf("communication") != -1) || (strError.indexOf("link") != -1) || (strError.indexOf("connection") != -1) &&
                             ((strError.indexOf("failure") != -1) || (strError.indexOf("lost") != -1)))
                     || (strError.indexOf("broken") != -1))
                         bFound = true;
         }
         if (!bFound)
             if ((strErrorText != null) && (strErrorText.length() > 0))
                 if (strError != null)
                     if (strError.indexOf(strErrorText) != -1)
                         bFound = true;
         if (!bFound)
             if ((strErrorCode != null) && (strErrorCode.length() > 0))
             {
                 int iErrorCode = Integer.parseInt(strErrorCode);
                 if (ex instanceof SQLException)
                     if (((SQLException)ex).getErrorCode() == iErrorCode)
                         bFound = true;
             }
         return bFound;
     }
     /**
      * Make a table for this record.
      * @param record (look at the database type).
      * @return The new table for this record.
      */
     public BaseTable doMakeTable(Record record)
     {
         BaseTable table = null;
         boolean bIsQueryRecord = record.isQueryRecord();
         boolean bIsQuerySupported = !DBConstants.FALSE.equalsIgnoreCase(this.getProperty(SQLParams.SQL_JOINS_SUPPORTED));
         if ((bIsQueryRecord) && (bIsQuerySupported))
             if (!DBConstants.TRUE.equalsIgnoreCase(this.getProperty(SQLParams.SQL_VIEWS_BUILT)))
         { // If this is a query and there are no SQL Views, make sure this query doesn't have an object (multi) query as one of it's records.
             if (record instanceof QueryRecord)  // Always
                 bIsQuerySupported = !((QueryRecord)record).isComplexQuery();
         }
         int iRawDBType = (record.getDatabaseType() & DBConstants.TABLE_TYPE_MASK);
         if (((iRawDBType == DBConstants.LOCAL) || (iRawDBType == DBConstants.TABLE))
                 && (!bIsQueryRecord))
             table = new JdbcTable(this, record);
         else if ((bIsQueryRecord) &&
             ((!bIsQuerySupported)
             || ((record.getDatabaseType() & DBConstants.MANUAL_QUERY) != 0)))
         { // Physical Tables cannot process SQL queries, so use QueryTable!
             PassThruTable passThruTable = new QueryTable(this, record);
             passThruTable.addTable(record.getRecordlistAt(0).getTable());
             table = passThruTable;
         }
         else if ((iRawDBType == DBConstants.LOCAL)
                 || (iRawDBType == DBConstants.REMOTE)
                     || (iRawDBType == DBConstants.TABLE))
             table = new JdbcTable(this, record);
         else
             table = super.doMakeTable(record);
         return table;
     }
     /**
      * Get the Physical database connection.
      * <p />Note: The JDBC Driver must be loaded before calling this.
      * @return The JDBC Connection.
      */
     public Connection getJDBCConnection() throws DBException
     {
         boolean bSharedConnection = false;
         if (m_sharedConnection != null)
         { // Special code for databases that can't handle multiple databases
             bSharedConnection = true;
             if (m_sharedConnection != m_JDBCConnection)
             {
                 if (m_sharedConnection != null)
                 {
                     try   {
                         m_sharedConnection.close();
                     } catch (SQLException ex) {
                         ex.printStackTrace();
                     }
                     m_sharedConnection = null;
                 }
             }
         }
         if (m_JDBCConnection == null)	// Always
             this.setupDBConnection();
         if (bSharedConnection)
             m_sharedConnection = m_JDBCConnection;
         return m_JDBCConnection;
     }
     /**
      * Setup the Database connection.
      * This method requires the jdbcurl= param with <dbname/> specified.
      * Usually, you will have to supply a username and password.
      */
     public void setupDBConnection() throws DBException
     {
         String strURL = this.getProperty(SQLParams.JDBC_URL_PARAM);
         if ((strURL == null) || (strURL.length() == 0))
             strURL = this.getProperty(SQLParams.DEFAULT_JDBC_URL_PARAM);    // Default
         String strServer = this.getProperty(SQLParams.DB_SERVER_PARAM);
         if ((strServer == null) || (strServer.length() == 0))
             strServer = this.getProperty(SQLParams.DEFAULT_DB_SERVER_PARAM);    // Default
         if ((strServer == null) || (strServer.length() == 0))
             strServer = "localhost"; //this.getProperty(DBParams.SERVER);    // ??       
         String strDatabaseName = this.getDatabaseName(true);
         strURL = Utility.replace(strURL, "{dbname}", strDatabaseName);
         if (strServer != null)
             strURL = Utility.replace(strURL, "{dbserver}", strServer);
         String strUsername = this.getProperty(SQLParams.USERNAME_PARAM);
         if ((strUsername == null) || (strUsername.length() == 0))
             strUsername = this.getProperty(SQLParams.DEFAULT_USERNAME_PARAM); // Default
         String strPassword = this.getProperty(SQLParams.PASSWORD_PARAM);
         if ((strPassword == null) || (strPassword.length() == 0))
             strPassword = this.getProperty(SQLParams.DEFAULT_PASSWORD_PARAM); // Default
         // Open the database for non-exclusive access
         try   {
             Utility.getLogger().info("Connecting: " + strURL + " User: " + strUsername);
             m_JDBCConnection = DriverManager.getConnection(strURL, strUsername, strPassword);
             Utility.getLogger().info("Connection okay: " + (m_JDBCConnection != null));
         }
         catch (SQLException e)
         {
             DBException ex = this.convertError(e);    // Probably "DB Not found"
             if ((ex.getErrorCode() == DBConstants.DB_NOT_FOUND)
                 && (!DBConstants.FALSE.equalsIgnoreCase(this.getProperty(DBConstants.CREATE_DB_IF_NOT_FOUND)))
                     && (DBConstants.TRUE.equals(this.getProperty(SQLParams.CREATE_DATABASE_SUPPORTED))))
             {
                 if (this.create(strDatabaseName)) // Database not found, create it.
                     return;     // Success
             	e.printStackTrace();
             }
             if (ex.getErrorCode() != DBConstants.DB_NOT_FOUND)
             	e.printStackTrace();	// Ignore if create is turned off
             throw ex;
         }
         catch (java.lang.Exception ex)
         {
             ex.printStackTrace();
             Debug.print(ex, "Error on open db");
             System.exit(0);
         }
     }
     /**
      * Setup the new connection.
      */
     public void initConnection()
     {
         try   {
             boolean bAutoCommit = true;
             if (DBConstants.FALSE.equalsIgnoreCase(this.getProperty(SQLParams.AUTO_COMMIT_PARAM)))
                 bAutoCommit = false;
             m_JDBCConnection.setAutoCommit(bAutoCommit);
             DatabaseMetaData dma = m_JDBCConnection.getMetaData();
             String strDatabaseProductName = dma.getDatabaseProductName();
             Utility.getLogger().info("DB Name: " + strDatabaseProductName);
             this.setProperty(SQLParams.INTERNAL_DB_NAME, strDatabaseProductName);
             this.setupDatabaseProperties(); // If I haven't read the db properties, read them now!
             String strDateQuote = this.getProperty(SQLParams.SQL_DATE_QUOTE);
             if ((strDateQuote != null) && (strDateQuote.length() > 0))
                 DateTimeField.setSQLQuote(strDateQuote.charAt(0));
             Converter.initGlobals();
             String strFormat = this.getProperty(SQLParams.SQL_DATE_FORMAT);
             if (strFormat != null)
                 Converter.gDateSqlFormat = new java.text.SimpleDateFormat(strFormat);
             strFormat = this.getProperty(SQLParams.SQL_TIME_FORMAT);
             if (strFormat != null)
                 Converter.gTimeSqlFormat = new java.text.SimpleDateFormat(strFormat);
             strFormat = this.getProperty(SQLParams.SQL_DATETIME_FORMAT);
             if (strFormat != null)
                 Converter.gDateTimeSqlFormat = new java.text.SimpleDateFormat(strFormat);
             if (DBConstants.TRUE.equals(this.getProperty(SQLParams.SHARE_JDBC_CONNECTION)))
                 m_sharedConnection = m_JDBCConnection;  // HACK - Only one DB open at a time (pointbase)!
             Utility.getLogger().info("Connected to db: " + this.getDatabaseName(true));
         }
         catch (SQLException ex)
         {
             // Ignore errors to these non-essential calls;  // Probably "DB Not found"
         }
     }
     /**
      * Commit the transactions since the last commit.
      * Override this for SQL implementations.
      * @exception DBException An exception.
      */
     public void commit() throws DBException
     {
         super.commit();   // Will throw an error if something is not set up right.
         try   {
             if (m_JDBCConnection != null)
                 m_JDBCConnection.commit();
         } catch (SQLException ex) {
             throw this.convertError(ex);
         }
     }
     /**
      * Rollback the transactions since the last commit.
      * Override this for SQL implementations.
      * @exception DBException An exception.
      */
     public void rollback() throws DBException
     {
         super.rollback(); // Will throw an error if something is not set up right.
         try   {
             if (m_JDBCConnection != null)
                 m_JDBCConnection.rollback();
         } catch (SQLException ex) {
             throw this.convertError(ex);
         }
     }
     /**
      * Create a new empty database using the definition in the record.
      * @exception DBException Open errors passed from SQL.
      * @return true if successful.
      */
     public boolean create(String strDatabaseName) throws DBException
     {
         try {
             String strDBName = this.getProperty(SQLParams.INTERNAL_DB_NAME);
             int iDatabaseType = DBConstants.SYSTEM_DATABASE;
             DatabaseOwner env = this.getDatabaseOwner();
             JdbcDatabase db = (JdbcDatabase)env.getDatabase(strDBName, iDatabaseType, null);    // Do NOT pass properties when opening system database
             Statement m_queryStatement = db.getJDBCConnection().createStatement();
 
             String strSQL = "create database " + strDatabaseName + ";";
             m_queryStatement.execute(strSQL);
             db.free();
 
             String oldValue = this.getProperty(DBConstants.CREATE_DB_IF_NOT_FOUND);
             this.setProperty(DBConstants.CREATE_DB_IF_NOT_FOUND, DBConstants.FALSE);
             
             this.setupDBConnection();
 
             this.setProperty(DBConstants.CREATE_DB_IF_NOT_FOUND, oldValue);
             
             if (this.getProperty(DBConstants.LOAD_INITIAL_DATA) != null)
             	if ((this.getDatabaseType() & (DBConstants.SHARED_DATA | DBConstants.USER_DATA)) == DBConstants.SHARED_DATA)	// Yes, only shared data can have DatabaseInfo
             {
                 DatabaseInfo recDatabaseInfo = new DatabaseInfo();
                 try {
                 	recDatabaseInfo.setDatabaseName(this.getDatabaseName(false));
                 	recDatabaseInfo.setTable(this.doMakeTable(recDatabaseInfo));    // This makes a jdbc table
                 	recDatabaseInfo.init(null); // This should create the table if an archive exists
                 } catch (Exception ex) {
                 	// Ignore
                 } finally {
                 	recDatabaseInfo.free();
                 }
             }
             return true;    // Success
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
 
         return super.create(strDatabaseName);   // Error, not found
     }
     /**
      * Get the initial DB properties (null means no properties).
      * @param objDatabaseParam database specific parameter (ie., for jdbc this is the db name)
      * @return The initial properties.
      */
     public Map<String,String> getDatabaseProperties()
     {
         String strDatabaseProductName = this.getProperty(SQLParams.INTERNAL_DB_NAME);
         if (strDatabaseProductName == null)
             return null;    // Can't get the properties without the database being used
         Map<String,String> map = super.getDatabaseProperties();        
         strDatabaseProductName = strDatabaseProductName.toLowerCase();
         ResourceBundle dbProperties = null;
         if (strDatabaseProductName.indexOf("access") != -1)
             dbProperties = new DBProperties_access();
         else if (strDatabaseProductName.indexOf("instantdb") != -1)
             dbProperties = new DBProperties_instantdb();
         else if (strDatabaseProductName.indexOf("sybase") != -1)
             dbProperties = new DBProperties_sybase();
         else if (strDatabaseProductName.indexOf("pointBase") != -1)
             dbProperties = new DBProperties_pointbase();
         else if (strDatabaseProductName.indexOf("mysql") != -1)
             dbProperties = new DBProperties_mysql();
         else if (strDatabaseProductName.indexOf("postgresql") != -1)
             dbProperties = new DBProperties_postgresql();
         else
         {
             if (strDatabaseProductName.indexOf('.') == -1)
             {
                 String strClassName = DBProperties_mysql.class.getName();
                 strDatabaseProductName = strClassName.substring(0, strClassName.length() - "mysql".length()) + strDatabaseProductName;
             }
 //            dbProperties = (ListResourceBundle)Utility.makeObjectFromClassName(Object.class.getName(), strDatabaseProductName);
 
             
             
            try {
         	   Class<?> c = Class.forName(strDatabaseProductName);
         	   dbProperties = (ListResourceBundle)c.newInstance();
            } catch (Exception e) {
     		   Utility.getLogger().warning("Error on create class: " + strDatabaseProductName);
     		   e.printStackTrace();
            }
            
         }
         // Merge the properties with my properties.
         if (dbProperties != null)
         {
             if (map == null)
                 map = new HashMap<String,String>();
             for (Enumeration<String> e = dbProperties.getKeys() ; e.hasMoreElements() ;)
             {
                 String key = e.nextElement();
                 String value = dbProperties.getString(key);
                 map.put(key, value);
             }
         }
         
         return map;
     }
     /**
 	 * Load ant set the database properties from this database's (database) info table.
 	 */
     public void loadDatabaseProperties()
     {
         if (this.getProperty(STARTING_ID) != null)
             return; // Already read (This value will be cached in the environment database cache)
         int iStartID = 1;
         /* This code works, but I prefer to use the DatabaseInfo record.
         try {
             Statement queryStatement = this.getJDBCConnection().createStatement();
     
             String strSQL = "select StartID from DatabaseInfo where ID=1;";
             queryStatement.execute(strSQL);
             
             ResultSet resultset = queryStatement.getResultSet();
             boolean more = resultset.next();
             if (more)
             {
                 iStartID = resultset.getInt(1);
             }
             resultset.close();
             queryStatement.close();
         } catch (SQLException e) {
             // Ignore table not found error.
         }
         */
     	if ((this.getDatabaseType() & (DBConstants.SHARED_DATA | DBConstants.USER_DATA)) != DBConstants.SHARED_DATA)
     		return;		// DatabaseInfo would only exist for shared databases
 
     	RecordOwner recordOwner = null; // ((BaseApplication)this.getDatabaseOwner().getEnvironment().getDefaultApplication()).getMainTask().; // For now
         DatabaseInfo recDatabaseInfo = new DatabaseInfo();
         recDatabaseInfo.setTable(this.doMakeTable(recDatabaseInfo));    // This makes a jdbc table
         String dbName = this.getDatabaseName(true);
         if (dbName.endsWith(SHARED_SUFFIX))
         	dbName = dbName.substring(0, dbName.length() - SHARED_SUFFIX.length());	// Always
         else if (dbName.endsWith(USER_SUFFIX))
         	dbName = dbName.substring(0, dbName.length() - USER_SUFFIX.length());	// Never
         recDatabaseInfo.setDatabaseName(dbName);
 
         int iOldOpenMode = recDatabaseInfo.getOpenMode();
         recDatabaseInfo.setOpenMode(recDatabaseInfo.getOpenMode() | DBConstants.OPEN_DONT_CREATE);
 
         recDatabaseInfo.init(recordOwner);
 
         recDatabaseInfo.setOpenMode(iOldOpenMode);
         
         if (recDatabaseInfo.getEditMode() == DBConstants.EDIT_CURRENT)
         {
             iStartID = (int)recDatabaseInfo.getField(DatabaseInfo.kStartID).getValue();
             if (!recDatabaseInfo.getField(DatabaseInfo.kEndID).isNull())
                 this.addDatabaseProperty(ENDING_ID, Integer.toString((int)recDatabaseInfo.getField(DatabaseInfo.kEndID).getValue()));
             if (!recDatabaseInfo.getField(DatabaseInfo.kBaseDatabase).isNull())
                 this.addDatabaseProperty(BaseDatabase.BASE_DATABASE, recDatabaseInfo.getField(DatabaseInfo.kBaseDatabase).toString());
         }
         if (iStartID <= 0)
             iStartID = 1;
         recDatabaseInfo.free();
         this.addDatabaseProperty(STARTING_ID, Integer.toString(iStartID));
         
         if (m_databaseBase == null) // Always
             if (this.getProperty(BASE_DATABASE) != null)
         {
             Map<String,Object> properties = new HashMap<String,Object>();
             properties.putAll(this.getProperties());
             properties.remove(BASE_DATABASE);
             properties.put(DBConstants.DB_USER_PREFIX, DBConstants.BLANK);
             properties.put(DBConstants.SUB_SYSTEM_LN_SUFFIX, DBConstants.BLANK);
             m_databaseBase = m_databaseOwner.getDatabase(this.getProperty(BASE_DATABASE), this.getDatabaseType() & DBConstants.TABLE_MASK, properties);
         }
     }
     /**
      * Add this database property.
      * @param strKey
      * @param strValue
      */
     public void addDatabaseProperty(String strKey, String strValue)
     {
         this.setProperty(strKey, strValue);
         if (this.getDatabaseOwner().getEnvironment().getCachedDatabaseProperties(this.getDatabaseName(true)) != null)  // Always
             if (this.getDatabaseOwner().getEnvironment().getCachedDatabaseProperties(this.getDatabaseName(true)) != Environment.DATABASE_DOESNT_EXIST)  // Always
             	this.getDatabaseOwner().getEnvironment().getCachedDatabaseProperties(this.getDatabaseName(true)).put(strKey, strValue);
     }
 }
