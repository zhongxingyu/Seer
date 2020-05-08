 package org.gooddata.connector.backend;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.gooddata.connector.driver.AbstractSqlDriver;
 
 import com.gooddata.connector.model.PdmSchema;
 import com.gooddata.exception.InternalErrorException;
 import com.gooddata.exception.ModelException;
 import com.gooddata.integration.model.Column;
 import com.gooddata.integration.model.DLI;
 import com.gooddata.integration.model.DLIPart;
 import com.gooddata.integration.rest.GdcRESTApiWrapper;
 import com.gooddata.util.FileUtil;
 import com.gooddata.util.JdbcUtil;
 
 /**
  * GoodData abstract connector.
  * This connector creates a GoodData LDM schema from a source schema, extracts the data from the source,
  * normalizes the data, and create the GoodData data deployment package.
  *
  * @author zd <zd@gooddata.com>
  * @version 1.0
  */
 public abstract class AbstractConnectorBackend implements ConnectorBackend {
 
     private static Logger l = Logger.getLogger("org.gooddata.connector.backend");
 
     // Connector backends
     public static final int CONNECTOR_BACKEND_DERBY_SQL = 1;
     public static final int CONNECTOR_BACKEND_MYSQL = 2;
 
     /**
      * The SQL driver
      */
     protected AbstractSqlDriver sg;
 
 
     /**
      * PDM schema
      */
     private PdmSchema pdm;
 
     /**
      * The project id
      */
     protected String projectId;
 
 
     // MySQL username
     private String username;
 
     // MySQL password
     private String password;
     
 
     /**
      * The config file name
      */
     protected String configFileName;
 
     /**
      * The ZIP archive suffix
      */
     protected static final String DLI_ARCHIVE_SUFFIX = ".zip";
 
 
     /**
      * Constructor
      * @param projectId project id
      * @param configFileName config file name
      * @param pdm PDM schema
      * @param username database backend username
      * @param username database backend password 
      * @throws IOException in case of an IO issue 
      */
     protected AbstractConnectorBackend(String projectId, String configFileName, PdmSchema pdm, String username,
                                        String password) throws IOException {
         this.projectId = projectId;
         this.configFileName = configFileName;
         this.pdm = pdm;
         this.username = username;
         this.password = password;
     }
 
     /**
      * Drops all snapshots
      * @return  a msg
      */
     public abstract void dropSnapshots();
     
     
 
     /**
      * Create the GoodData data package with the ALL data
      * @param dli the Data Loading Interface that contains the required data structures
      * @param parts the Data Loading Interface parts
      * @param dir target directory where the data package will be stored
      * @param archiveName the name of the target ZIP archive
      * @throws IOException IO issues
      * @throws ModelException in case of PDM schema issues 
      */
     public void deploy(DLI dli, List<DLIPart> parts, String dir, String archiveName)
             throws IOException, ModelException {
         deploySnapshot(dli, parts, dir, archiveName, null);
     }
 
     /**
      * Adds CSV headers to all CSV files
      * @param parts the Data Loading Interface parts
      * @param dir target directory where the data package will be stored
      * @throws IOException IO issues
      */
     protected void addHeaders(List<DLIPart> parts, String dir) throws IOException {
         for(DLIPart part : parts) {
             String fn = part.getFileName();
             List<Column> cols = part.getColumns();
             String header = "";
             for(Column col : cols) {
                 if(header != null && header.length() > 0) {
                     header += ","+col.getName();
                 }
                 else {
                     header += col.getName();                    
                 }
             }
             File original = new File(dir + System.getProperty("file.separator") + fn);
             File tmpFile = FileUtil.appendCsvHeader(header, original);
             original.delete();
             tmpFile.renameTo(original);
         }
     }
 
     
     /**
      * Create the GoodData data package with the data from specified snapshots
      * @param dli the Data Loading Interface that contains the required data structures
      * @param parts the Data Loading Interface parts
      * @param dir target directory where the data package will be stored
      * @param archiveName the name of the target ZIP archive
      * @param snapshotIds snapshot ids that are going to be loaded (if NULL, all snapshots are going to be loaded) 
      * @throws IOException IO issues
      * @throws ModelException in case of PDM schema issues 
      */
     public void deploySnapshot(DLI dli, List<DLIPart> parts, String dir, String archiveName, int[] snapshotIds)
             throws IOException, ModelException {
         loadSnapshot(parts, dir, snapshotIds);
         FileUtil.writeStringToFile(dli.getDLIManifest(parts), dir + System.getProperty("file.separator") +
                 GdcRESTApiWrapper.DLI_MANIFEST_FILENAME);
         addHeaders(parts, dir);
         FileUtil.compressDir(dir, archiveName);
     }
 
     /**
      * PDM schema getter
      * @return pdm schema
      */
     public PdmSchema getPdm() {
         return pdm;
     }
 
     /**
      * PDM schema setter
      * @param pdm PDM schema
      */
     public void setPdm(PdmSchema pdm) {
         this.pdm = pdm;
     }
 
     /**
      * Initializes the Derby database schema that is going to be used for the data normalization
      * @throws ModelException imn case of PDM schema issues
      */
     public void initialize() throws ModelException {
         Connection con = null;
         try {
         	con = connect();
             if(!isInitialized()) {
                 sg.executeSystemDdlSql(con);
             }
             sg.executeDdlSql(con, getPdm());    
         }
         catch (SQLException e) {
             throw new InternalError(e.getMessage());
         }
         finally {
             try {
                 if (con != null && !con.isClosed())
                     con.close();
             }
             catch (SQLException e) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Perform the data normalization (generate lookups) in the Derby database. The database must contain the required
      * tables
      * @throws ModelException in case of PDM schema issues
      */
     public void transform() throws ModelException {
         Connection con = null;
         try {
             con = connect();
             sg.executeNormalizeSql(con, getPdm());
         }
         catch (SQLException e) {
            throw new RuntimeException("Error normalizing PDM Schema " + getPdm().getName() + " " + getPdm().getTables(), e);
         }
         finally {
             try {
                 if (con != null && !con.isClosed())
                     con.close();
             }
             catch (SQLException e) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Lists the current snapshots
      * @return list of snapshots as String
      * @throws com.gooddata.exception.InternalErrorException in case of internal issues (e.g. uninitialized schema)
      */
     public String listSnapshots() throws InternalErrorException {
         String result = "ID        FROM ROWID        TO ROWID        TIME\n";
               result += "------------------------------------------------\n";
         Connection con = null;
         Statement s = null;
         ResultSet r = null;
         try {
             con = connect();
             s = con.createStatement();
             r = JdbcUtil.executeQuery(s, "SELECT id,firstid,lastid,tmstmp FROM snapshots");
             for(boolean rc = r.next(); rc; rc = r.next()) {
                 int id = r.getInt(1);
                 int firstid = r.getInt(2);
                 int lastid = r.getInt(3);
                 long tmstmp = r.getLong(4);
                 Date tm = new Date(tmstmp);
                 result += id + "        " + firstid + "        " + lastid + "        " + tm + "\n";
             }
         }
         catch (SQLException e) {
             throw new InternalErrorException(e.getMessage());
         }
         finally {
             try {
                 if(r != null && !r.isClosed())
                     r.close();
                 if (s != null && !s.isClosed())
                     s.close();
                 if(con != null && !con.isClosed())
                     con.close();
             }
             catch (SQLException ee) {
                ee.printStackTrace();
             }
         }
         return result;
     }
 
     /**
      * Get last snapshot number
      * @return last snapshot number
      * @throws InternalErrorException in case of internal issues (e.g. uninitialized schema)
      */
     public int getLastSnapshotId() throws InternalErrorException {
         Connection con = null;
         Statement s = null;
         ResultSet r = null;
         try {
             con = connect();
             s = con.createStatement();
             r = s.executeQuery("SELECT MAX(id) FROM snapshots");
             for(boolean rc = r.next(); rc; rc = r.next()) {
                 int id = r.getInt(1);
                 return id;
             }
         }
         catch (SQLException e) {
             throw new InternalErrorException(e.getMessage());
         }
         finally {
             try {
                 if(r != null && !r.isClosed())
                     r.close();
                 if(s != null && !s.isClosed())
                     s.close();
                 if(con != null && !con.isClosed())
                     con.close();
             }
             catch (SQLException ee) {
                 ee.printStackTrace();
             }
         }
         throw new InternalErrorException("Can't retrieve the last snapshot number.");
     }
 
     /**
      * Figures out if the connector is initialized
      * @return the initialization status
      * @throws InternalErrorException 
      */
     public boolean isInitialized() {
         return exists("snapshots");
     }
 
     /**
      * Returns true if the specified table exists in the DB
      * @param tbl table name
      * @return true if the table exists, false otherwise
      * @throws InternalErrorException 
      */
     public boolean exists(String tbl) {
         Connection con = null;
         try {
             con = connect();
             return sg.exists(con, tbl);
         }
         catch (SQLException e) {
         	throw new InternalError(e.getMessage());
 		}
         finally {
             try {
                 if(con != null && !con.isClosed())
                     con.close();
             }
             catch (SQLException ee) {
                 ee.printStackTrace();
             }
         }
     }
 
     /**
      * Extracts the source data CSV to the Derby database where it is going to be transformed
      * @param dataFile the data file to extract
      * @throws ModelException in case of PDM schema issues
      */
     public void extract(File dataFile) throws ModelException {
         Connection con = null;
         try {
             con = connect();
             sg.executeExtractSql(con, getPdm(), dataFile.getAbsolutePath());
         }
         catch (SQLException e) {
             throw new InternalError(e.getMessage());
         }
         finally {
             try {
                 if (con != null && !con.isClosed())
                     con.close();
             }
             catch (SQLException e) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Load the all normalized data from the Derby SQL to the GoodData data package on the disk
      * @param parts the Data Loading Interface parts
      * @param dir target directory where the data package will be stored
      * @throws ModelException in case of PDM schema issues
      */
     public void load(List<DLIPart> parts, String dir) throws ModelException {
         loadSnapshot(parts, dir, null);
     }
 
     /**
      * Load the normalized data from the Derby SQL to the GoodData data package on the disk
      * incrementally (specific snapshot)
      * @param parts the Data Loading Interface parts
      * @param dir target directory where the data package will be stored
      * @param snapshotIds snapshot ids that are going to be loaded (if NULL, all snapshots are going to be loaded)
      * @throws ModelException in case of PDM schema issues
      */
     public void loadSnapshot(List<DLIPart> parts, String dir, int[] snapshotIds) throws ModelException {
         Connection con = null;
         try {
             con = connect();
             String sql = "";
             // generate SELECT INTO CSV Derby SQL
             // the required data structures are taken for each DLI part
             for (DLIPart p : parts) {
                 sg.executeLoadSql(con, getPdm(), p, dir, snapshotIds);
             }
         }
         catch (SQLException e) {
             throw new InternalError(e.getMessage());
         }
         finally {
             try {
                 if (con != null && !con.isClosed())
                     con.close();
             }
             catch (SQLException e) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Database backend username getter
      * @return database backend username
      */
     public String getUsername() {
         return username;
     }
 
     /**
      * Database backend username setter
      * @param username database backend username
      */
     public void setUsername(String username) {
         this.username = username;
     }
 
     /**
      * Database backend password getter
      * @return  database backend password
      */
     public String getPassword() {
         return password;
     }
 
     /**
      * Database backend password setter
      * @param password database backend password
      */
     public void setPassword(String password) {
         this.password = password;
     }
 }
