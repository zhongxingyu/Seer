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
 import java.sql.SQLNonTransientConnectionException;
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
         CREATE_MAP = Maps.newLinkedHashMap();
         
         // Lookup Tables
         CREATE_MAP.put("COLLECTION_TYPES", 
                        "CREATE TABLE collection_types (id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), type varchar(32), inserted boolean, updated boolean, PRIMARY KEY (id))");
         CREATE_MAP.put("DATA_TYPES", 
                        "CREATE TABLE data_types (id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), type varchar(32), inserted boolean, updated boolean, PRIMARY KEY (id))");
         CREATE_MAP.put("DATA_FORMATS", 
                        "CREATE TABLE data_format (id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), type varchar(32), inserted boolean, updated boolean, PRIMARY KEY (id))");
         CREATE_MAP.put("DOCUMENTATION_TYPES", 
                        "CREATE TABLE documentation_types (id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), type varchar(32), inserted boolean, updated boolean, PRIMARY KEY (id))");
         
         CREATE_MAP.put("GLOBAL_CONFIG",
                       "CREATE TABLE global_config (id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), base_dir varchar(512), thredds_dir varchar(512), PRIMARY KEY (id))");
         CREATE_MAP.put("CATALOGS", 
                        "CREATE TABLE catalogs (id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), catalog_id INT CONSTRAINT CATALOGS1_FK REFERENCES catalogs, location varchar(512), name varchar(64), expires date, version varchar(8), inserted boolean, updated boolean, PRIMARY KEY (id))"); //TODO- service, property and dataset can be subtables
         CREATE_MAP.put("INGESTS",
                       "CREATE TABLE ingests (id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), catalog_id INT CONSTRAINT CATALOGS2_FK REFERENCES catalogs, name varchar(128), ftpLocation varchar(512), rescanEvery bigint, fileRegex varchar(64), successDate date, successTime time, username varchar(64), password varchar(64), active boolean, inserted boolean, updated boolean, PRIMARY KEY (id))");
         CREATE_MAP.put("DATASETS", 
                        "CREATE TABLE datasets (id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), catalog_id INT CONSTRAINT CATALOGS3_FK REFERENCES catalogs, collection_type_id INT CONSTRAINT COLLECTION1_FK REFERENCES collection_types, data_type_id INT CONSTRAINT DATATYPE_FK REFERENCES data_types, name varchar(64), ncid varchar(128), authority varchar(64), inserted boolean, updated boolean, PRIMARY KEY (id))");
         CREATE_MAP.put("SERVICES", 
                        "CREATE TABLE services (id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), catalog_id INT CONSTRAINT CATALOGS4_FK REFERENCES catalogs, service_id INT CONSTRAINT SERVICE1_FK REFERENCES services,  /*service_type_id INT CONSTRAINT SERVICETYPE_FK REFERENCES service_types,*/ name varchar(64), base varchar(32),  description varchar(512), suffix varchar(32), inserted boolean, updated boolean, PRIMARY KEY (id))");
         CREATE_MAP.put("ACCESS", 
                        "CREATE TABLE access (id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), dataset_id INT CONSTRAINT DATASET1_FK REFERENCES datasets, service_id INT CONSTRAINT SERVICE2_FK REFERENCES services, dataformat_id INT CONSTRAINT DATAFORMAT_FK REFERENCES data_format, url_path varchar(512), inserted boolean, updated boolean, PRIMARY KEY (id))"); //TODO - Can have data_size
         
         CREATE_MAP.put("DOCUMENTATION", 
                        "CREATE TABLE documentation (id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), dataset_id INT CONSTRAINT DATASET2_FK REFERENCES datasets, documentation_type_id INT CONSTRAINT DOCTYPE_FK REFERENCES documentation_types, xlink_href varchar(256), xlink_title varchar(256), text varchar(1024), inserted boolean, updated boolean, PRIMARY KEY (id))");
         CREATE_MAP.put("PROPERTY", 
                        "CREATE TABLE property (id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), dataset_id INT CONSTRAINT DATASET3_FK REFERENCES datasets, name varchar(128), value varchar(512), inserted boolean, updated boolean, PRIMARY KEY (id))");
         
         // TODO- Create service type table
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
     
     public static boolean shutdownDatabase(String shutdown) throws SQLException, NamingException,
                                                  ClassNotFoundException {
         System.setProperty("dburl", shutdown);
         try {
             Connection myConn = SqlUtils.getConnection(JNDI_CONTEXT);
         }
         catch (SQLNonTransientConnectionException ex) {
             // Derby throws this if database succeeds in shutting down
             return true;
         }
         return false;
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
     
     /**
      * Convenience method to do proper house-keeping of connections
      * @param connection close this
      */
     public static void closeConnection(Connection connection) {
         SqlUtils.closeConnection(connection);
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
 
     public static Map<String, String> getCatalogInfo(URI location) throws SQLException, NamingException, ClassNotFoundException {
         Connection connection = null;
         Map<String, String> rowMap = new HashMap<String, String>();
         ResultSet rs = null;
         try {
             connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT * FROM catalogs WHERE location = ?");
             stmt.setString(1, location.toString());
             rs = stmt.executeQuery();
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
         finally {
             try {if (rs != null) rs.close();} catch (Exception e) { /* ignore */ };
             SqlUtils.closeConnection(connection);
         }
         return rowMap;
     }
 
     public static List<Map<String, String>> getDatasetInfos(String id) {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 }
