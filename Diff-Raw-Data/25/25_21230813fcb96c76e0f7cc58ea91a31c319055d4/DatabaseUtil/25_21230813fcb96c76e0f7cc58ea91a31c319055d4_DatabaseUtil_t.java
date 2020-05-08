 package gov.usgs.cida.ncetl.utils;
 
 import com.google.common.collect.Maps;
 import gov.usgs.webservices.jdbc.util.SqlUtils;
 import java.net.URI;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.naming.NamingException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author jwalker
  */
 public final class DatabaseUtil {
 
     private static final Logger LOG = LoggerFactory.getLogger(DatabaseUtil.class);
     private static final String DB_NAME = "NCETL";
     private static final String DB_LOCATION = FileHelper.getDatabaseDirectory() + DB_NAME;
     private static final String DB_CLASS_NAME = "org.apache.derby.jdbc.EmbeddedDriver";
     private static final String DB_URL = "jdbc:derby:" + DB_LOCATION;
     private static final String DB_STARTUP = DB_URL + ";create=true;";
     private static final String DB_SHUTDOWN = DB_URL + ";shutdown=true";
     private static final String JNDI_CONTEXT = "java:comp/env/jdbc/" + DB_NAME;
     private final static Map<String, String> CREATE_MAP;
 
     private DatabaseUtil() {
     }
 
     ;
 
     static {
         // TODO Switch to using ddl at some point
         CREATE_MAP = Maps.newHashMap();
         CREATE_MAP.put("GLOBAL_CONFIG",
                       "CREATE TABLE global_config (base_dir varchar(512), thredds_dir varchar(512))");
         CREATE_MAP.put("INGESTS",
                       "CREATE TABLE ingests (name varchar(128), ftpLocation varchar(512), rescanEvery bigint, fileRegex varchar(64), successDate date, successTime time, username varchar(64), password varchar(64), active boolean, inserted boolean, updated boolean)");
         // TODO probably needs more goodness
         CREATE_MAP.put("CATALOGS", 
                       "CREATE TABLE catalogs (id int, catalog_id int, location varchar(512), name varchar(64))");
         // TODO needs work to define table
         //CREATE_MAP.put("DATASETS", 
         //               "CREATE TABLE datasets (id int, catalog_id int, name varchar(64), ncid varchar(128))");
         // Check this for completeness
         //CREATE_MAP.put("KEYWORDS",
         //               "CREATE TABLE keywords (id int, dataset_id int, value varchar(64), vocab varchar(64))");
     }
 
     public static void setupDatabase() throws SQLException, NamingException,
                                               ClassNotFoundException {
         setupDatabase(DB_STARTUP, DB_CLASS_NAME);
     }
 
     public static void setupDatabase(final String dbConnection,
                                         final String dbClassName) throws
             SQLException, NamingException, ClassNotFoundException {
         LOG.info("*************** Initializing database.");
 
         System.setProperty("dbuser", "");
         System.setProperty("dbpass", "");
         System.setProperty("dburl", dbConnection);
         System.setProperty("dbclass", dbClassName);
 
         Connection myConn = null;
         try {
             myConn = getConnection();
             if (myConn != null) {
                 DatabaseMetaData dbMeta = myConn.getMetaData();
                 ResultSet rs = null;
                 try {
                     for (String table : CREATE_MAP.keySet()) {
                         rs = dbMeta.getTables(null, "APP", table, null);
                         if (!rs.next()) {
                             createTable(myConn, table);
                         }
                     }
                 }
                 finally {
                     rs.close();
                 }
             }
             LOG.info("*************** Database has been initialized.");
         }
         finally {
             SqlUtils.closeConnection(myConn);
         }
     }
 
     public static void shutdownDatabase() throws SQLException, NamingException,
                                                  ClassNotFoundException {
         shutdownDatabase(DB_SHUTDOWN);
     }
     
     public static void shutdownDatabase(String shutdown) throws SQLException, NamingException,
                                                  ClassNotFoundException {
         System.setProperty("dburl", shutdown);
         Connection myConn = SqlUtils.getConnection(JNDI_CONTEXT);
     }
 
     protected static Connection getConnection() throws SQLException,
                                                        NamingException,
                                                        ClassNotFoundException {
         return getConnection(JNDI_CONTEXT);
     }
 
     protected static Connection getConnection(String jndiContext) throws
             SQLException, NamingException, ClassNotFoundException {
         return SqlUtils.getConnection(jndiContext);
     }
 
     private static void createTable(Connection c, String table) throws
             SQLException,
                                                                        NamingException,
                                                                        ClassNotFoundException {
         Statement stmt = null;
         try {
             stmt = c.createStatement();
             stmt.execute(CREATE_MAP.get(table));
         }
         finally {
             stmt.close();
         }
     }
 
     public static Map<String, String> getCatalogInfo(URI location) {
         Connection connection = null;
         Map<String, String> rowMap = new HashMap<String, String>();
         try {
             connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT * FROM catalogs WHERE location = ?");
             stmt.setString(1, location.toString());
             ResultSet rs = stmt.executeQuery();
             ResultSetMetaData metaData = rs.getMetaData();
             int columnCount = metaData.getColumnCount();
             if (rs.next()) {
                 for (int i = 1; i <= columnCount; i++) {
                     String columnName = metaData.getColumnName(i);
                     String value = rs.getString(i);
                     rowMap.put(columnName, value);
                 }
             }
         }
         catch (SQLException ex) {
         }
         catch (ClassNotFoundException ex) {
         }
         catch (NamingException ex) {
         }
         finally {
             SqlUtils.closeConnection(connection);
             return rowMap;
         }
     }
 
     public static List<Map<String, String>> getDatasetInfos(String id) {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 }
