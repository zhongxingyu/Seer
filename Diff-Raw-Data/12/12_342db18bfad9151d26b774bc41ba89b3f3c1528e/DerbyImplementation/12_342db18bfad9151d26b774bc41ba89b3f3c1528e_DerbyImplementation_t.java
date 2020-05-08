 package org.hackystat.sensorbase.db.derby;
 
 import static org.hackystat.sensorbase.server.ServerProperties.DB_DIR_KEY;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.List;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.hackystat.sensorbase.db.DbImplementation;
 import org.hackystat.utilities.stacktrace.StackTrace;
 import org.hackystat.utilities.tstamp.Tstamp;
 import org.hackystat.sensorbase.resource.projects.jaxb.Project;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
 import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataType;
 import org.hackystat.sensorbase.resource.users.jaxb.User;
 import org.hackystat.sensorbase.server.Server;
 import org.hackystat.sensorbase.uripattern.UriPattern;
 
 /**
  * Provides a implementation of DbImplementation using Derby in embedded mode.
  * @author Philip Johnson
  */
 public class DerbyImplementation extends DbImplementation {
   
   /** The JDBC driver. */
   private static final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
   
   /** The Database name. */
   private static final String dbName = "sensorbase";
   
   /**  The Derby connection URL. */ 
   private static final String connectionURL = "jdbc:derby:" + dbName + ";create=true";
   
   /** Indicates whether this database was initialized or was pre-existing. */
   private boolean isFreshlyCreated;
   
   /** The SQL state indicating that INSERT tried to add data to a table with a preexisting key. */
   private static final String DUPLICATE_KEY = "23505";
   
   /** The key for putting/retrieving the directory where Derby will create its databases. */
   private static final String derbySystemKey = "derby.system.home";
   
   /** The logger message for connection closing errors. */
   private static final String errorClosingMsg = "Derby: Error while closing. \n";
   
   /** The logger message when executing a query. */
   private static final String executeQueryMsg = "Derby: Executing query ";
   
   /** Required by PMD since this string occurs multiple times in this file. */
   private static final String ownerEquals = " owner = '";
   
   /** Required by PMD as above. */
   private static final String andClause = "' AND ";
   
   private static final String derbyError = "Derby: Error ";
 
   /**
    * Instantiates the Derby implementation.  Throws a Runtime exception if the Derby
    * jar file cannot be found on the classpath.
    * @param server The SensorBase server instance. 
    */
   public DerbyImplementation(Server server) {
     super(server);
     // Set the directory where the DB will be created and/or accessed.
     // This must happen before loading the driver. 
     String dbDir = server.getServerProperties().get(DB_DIR_KEY);
     System.getProperties().put(derbySystemKey, dbDir);
     // Try to load the derby driver. 
     try {
       Class.forName(driver); 
     } 
     catch (java.lang.ClassNotFoundException e) {
       String msg = "Derby: Exception during DbManager initialization: Derby not on CLASSPATH.";
       this.logger.warning(msg + "\n" + StackTrace.toString(e));
       throw new RuntimeException(msg, e);
     }
   }
 
 
   /** {@inheritDoc} */
   @Override
   public void initialize() {
     try {
      // Create a shutdown hook that shuts down derby.
      Runtime.getRuntime().addShutdownHook(new Thread() {
        /** Run the shutdown hook for shutting down Derby. */
        public void run() {
          try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
          }
          catch (Exception e) {
            System.out.println("Derby shutdown hook results: " + e.getMessage());
          }
        }
      });
       // Initialize the database table structure if necessary.
       this.isFreshlyCreated = !isPreExisting();
       String dbStatusMsg = (this.isFreshlyCreated) ? 
           "Derby: uninitialized." : "Derby: previously initialized.";
       this.logger.info(dbStatusMsg);
       if (this.isFreshlyCreated) {
         this.logger.info("Derby: creating DB in: " + System.getProperty(derbySystemKey));
         createTables();
       }
     }
     catch (Exception e) {
       String msg = "Derby: Exception during DerbyImplementation initialization:";
       this.logger.warning(msg + "\n" + StackTrace.toString(e));
       throw new RuntimeException(msg, e);
     }
 
   }
   
   /**
    * Determine if the database has already been initialized with correct table definitions. 
    * Table schemas are checked by seeing if a dummy insert on the table will work OK.
    * @return True if the database exists and tables are set up correctly.
    * @throws SQLException If problems occur accessing the database or the tables are set right. 
    */
   private boolean isPreExisting() throws SQLException {
     Connection conn = null;
     Statement s = null;
     try {
       conn = DriverManager.getConnection(connectionURL);
       s = conn.createStatement();
       s.execute(testSensorDataTableStatement);
       s.execute(testSensorDataTypeTableStatement);
       s.execute(testUserTableStatement);
       s.execute(testProjectTableStatement);
     }  
     catch (SQLException e) {
       String theError = (e).getSQLState();
       if ("42X05".equals(theError)) {
         // Database doesn't exist.
         return false;
       }  
       else if ("42X14".equals(theError) || "42821".equals(theError))  {
         // Incorrect table definition. 
         throw e;   
       } 
       else { 
         // Unknown SQLException
         throw e; 
       }
     }
     finally {
       if (s != null) {
         s.close();
       }
       if (conn != null) {
         conn.close();
       }
     }
     // If table exists will get -  WARNING 02000: No row was found 
     return true;
   }
   
   /**
    * Initialize the database by creating tables for each resource type.
    * @throws SQLException If table creation fails.
    */
   private void createTables() throws SQLException {
     Connection conn = null;
     Statement s = null;
     try {
       conn = DriverManager.getConnection(connectionURL);
       s = conn.createStatement();
       s.execute(createSensorDataTableStatement);
       s.execute(indexSensorDataTableStatement);
       s.execute(createSensorDataTypeTableStatement);
       s.execute(indexSensorDataTypeTableStatement);
       s.execute(createUserTableStatement);
       s.execute(indexUserTableStatement);
       s.execute(createProjectTableStatement);
       s.execute(indexProjectTableStatement);
       s.close();
     }
     finally {
       s.close();
       conn.close();
     }
   }
   
   // ********************   Start  Sensor Data specific stuff here *****************  //
   
   /** The SQL string for creating the SensorData table. */
   private static final String createSensorDataTableStatement = 
     "create table SensorData  "
     + "("
     + " Owner VARCHAR(64) NOT NULL, "
     + " Tstamp TIMESTAMP NOT NULL, "
     + " Sdt VARCHAR(64) NOT NULL, "
     + " Runtime TIMESTAMP NOT NULL, "
     + " Tool VARCHAR(64) NOT NULL, "
     + " Resource VARCHAR(512) NOT NULL, "
     + " XmlSensorData VARCHAR(32000) NOT NULL, "
     + " XmlSensorDataRef VARCHAR(1000) NOT NULL, "
     + " LastMod TIMESTAMP NOT NULL, " //NOPMD (Don't worry about repeat occurrences of this string)
     + " PRIMARY KEY (Owner, Tstamp) "
     + ")" ;
   
   /** An SQL string to test whether the SensorData table exists and has the correct schema. */
   private static final String testSensorDataTableStatement = 
     " UPDATE SensorData SET "
     + " Owner = 'TestUser', " 
     + " Tstamp = '" + new Timestamp(new Date().getTime()).toString() + "', " //NOPMD (dup string)
     + " Sdt = 'testSdt',"
     + " Runtime = '" + new Timestamp(new Date().getTime()).toString() + "', "
     + " Tool = 'testTool', "
     + " Resource = 'testResource', "
     + " XmlSensorData = 'testXmlResource', "
     + " XmlSensorDataRef = 'testXmlRef', "
     + " LastMod = '" + new Timestamp(new Date().getTime()).toString() + "' " //NOPMD (dup string)
     + " WHERE 1=3"; //NOPMD (duplicate string)
   
   /** The statement that sets up an index for the SensorData table. */
   private static final String indexSensorDataTableStatement = 
     "CREATE UNIQUE INDEX SensorDataIndex ON SensorData(Owner, Tstamp)";
 
 
   /** {@inheritDoc} */
  @Override
   public boolean storeSensorData(SensorData data, String xmlSensorData, String xmlSensorDataRef) {
     Connection conn = null;
     PreparedStatement s = null;
     try {
       conn = DriverManager.getConnection(connectionURL);
       s = conn.prepareStatement("INSERT INTO SensorData VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
       // Order: Owner Tstamp Sdt Runtime Tool Resource XmlSensorData XmlSensorDataRef LastMod
       s.setString(1, data.getOwner());
       s.setTimestamp(2, Tstamp.makeTimestamp(data.getTimestamp()));
       s.setString(3, data.getSensorDataType());
       s.setTimestamp(4, Tstamp.makeTimestamp(data.getRuntime()));
       s.setString(5, data.getTool());
       s.setString(6, data.getResource());
       s.setString(7, xmlSensorData);
       s.setString(8, xmlSensorDataRef);
       s.setTimestamp(9, new Timestamp(new Date().getTime()));
       s.executeUpdate();
       this.logger.fine("Derby: Inserted " + data.getOwner() + " " + data.getTimestamp());
     }
     catch (SQLException e) {
       if (DUPLICATE_KEY.equals(e.getSQLState())) {
         try {
           // Do an update, not an insert.
           s = conn.prepareStatement(
               "UPDATE SensorData SET "
               + " Sdt=?, Runtime=?, Tool=?, Resource=?, XmlSensorData=?, " 
               + " XmlSensorDataRef=?, LastMod=?"
               + " WHERE Owner=? AND Tstamp=?");
           s.setString(1, data.getSensorDataType());
           s.setTimestamp(2, Tstamp.makeTimestamp(data.getRuntime()));
           s.setString(3, data.getTool());
           s.setString(4, data.getResource());
           s.setString(5, xmlSensorData);
           s.setString(6, xmlSensorDataRef);
           s.setTimestamp(7, new Timestamp(new Date().getTime()));
           s.setString(8, data.getOwner());
           s.setTimestamp(9, Tstamp.makeTimestamp(data.getTimestamp()));
           s.executeUpdate();
           this.logger.fine("Derby: Updated " + data.getOwner() + " " + data.getTimestamp());
         }
         catch (SQLException f) {
           this.logger.info(derbyError + StackTrace.toString(f));
         }
       }
     }
     finally {
       try {
         s.close();
         conn.close();
       }
       catch (SQLException e) {
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
     return true;
   }
 
 
   /** {@inheritDoc} */
   @Override
   public boolean isFreshlyCreated() {
     return this.isFreshlyCreated;
   }
   
   
   /** {@inheritDoc} */
   @Override
   public String getSensorDataIndex() {
     String st = "SELECT XmlSensorDataRef FROM SensorData";
     return getIndex("SensorData", st); //NOPMD  (See below)
   }
   
   /*
    * Interestingly, I could not refactor out the string "SensorData" to avoid the PMD error
    * resulting from multiple occurrences of the same string. 
    * This is because if I made it a private String, then Findbugs would throw a warning asking
    * for it to be static:
    * 
    * private static final String sensorData = "SensorData"; 
    * 
    *  However, the above declaration causes the system to deadlock! 
    *  So, I'm just ignoring the PMD error. 
    */
   
   /** {@inheritDoc} */
   @Override
   public String getSensorDataIndex(User user) {
     String st = "SELECT XmlSensorDataRef FROM SensorData WHERE owner='" + user.getEmail() + "'"; 
     return getIndex("SensorData", st);
   }
 
   /** {@inheritDoc} */
   @Override
   public String getSensorDataIndex(User user, String sdtName) {
     String st = 
       "SELECT XmlSensorDataRef FROM SensorData WHERE " 
       + ownerEquals + user.getEmail() + "', " 
       + " sdt='" + sdtName + "'";
     return getIndex("SensorData", st);
   }
   
   /** {@inheritDoc} */
   @Override
   public String getSensorDataIndex(User user, XMLGregorianCalendar startTime, 
       XMLGregorianCalendar endTime, List<UriPattern> uriPatterns) {
     String statement = 
       "SELECT XmlSensorDataRef, Resource FROM SensorData WHERE "
       + ownerEquals + user.getEmail() + andClause 
       + " Tstamp BETWEEN TIMESTAMP('" + Tstamp.makeTimestamp(startTime) + "') AND "
       + " TIMESTAMP('" + Tstamp.makeTimestamp(endTime) + "')";
     return getIndex("SensorData", statement);
   }
 
   /** {@inheritDoc} */
   @Override
   public boolean hasSensorData(User user, XMLGregorianCalendar timestamp) {
     Connection conn = null;
     PreparedStatement s = null;
     ResultSet rs = null;
     boolean isFound = false;
     try {
       conn = DriverManager.getConnection(connectionURL);
       // 
       String statement = 
         "SELECT XmlSensorDataRef FROM SensorData WHERE "
         + ownerEquals + user.getEmail() + andClause 
         + " Tstamp='" + Tstamp.makeTimestamp(timestamp) + "'";
       server.getLogger().fine(executeQueryMsg + statement);
       s = conn.prepareStatement(statement);
       rs = s.executeQuery();
       // If a record was retrieved, we'll enter the loop, otherwise we won't. 
       while (rs.next()) {
         isFound = true;
       }
     }
     catch (SQLException e) {
       this.logger.info("Derby: Error in hasSensorData()" + StackTrace.toString(e));
     }
     finally {
       try {
         rs.close();
         s.close();
         conn.close();
       }
       catch (SQLException e) {
         this.logger.warning("Derby: Error closing the connection" + StackTrace.toString(e));
       }
     }
     return isFound;
   }
 
   /** {@inheritDoc} */
   @Override
   public void deleteSensorData(User user, XMLGregorianCalendar timestamp) {
     String statement =
       "DELETE FROM SensorData WHERE "
       + ownerEquals + user.getEmail() + andClause 
       + " Tstamp='" + Tstamp.makeTimestamp(timestamp) + "'";
     deleteResource(statement);
   }
 
   /** {@inheritDoc} */
   @Override
   public String getSensorData(User user, XMLGregorianCalendar timestamp) {
     String statement =
       "SELECT XmlSensorData FROM SensorData WHERE "
       + ownerEquals + user.getEmail() + andClause 
       + " Tstamp='" + Tstamp.makeTimestamp(timestamp) + "'";
     return getResource("SensorData", statement);
   }
 
   // ********************   Start SensorDataType specific stuff here *****************  //
 
   /** The SQL string for creating the SensorDataType table. */
   private static final String createSensorDataTypeTableStatement = 
     "create table SensorDataType  "
     + "("
     + " Name VARCHAR(64) NOT NULL, "
     + " XmlSensorDataType VARCHAR(32000) NOT NULL, "
     + " XmlSensorDataTypeRef VARCHAR(1000) NOT NULL, "
     + " LastMod TIMESTAMP NOT NULL, "
     + " PRIMARY KEY (Name) "
     + ")" ;
   
   /** An SQL string to test whether the SensorDataType table exists and has the correct schema. */
   private static final String testSensorDataTypeTableStatement = 
     " UPDATE SensorDataType SET "
     + " Name = 'TestSdt', " 
     + " XmlSensorDataType = 'testXmlResource', "
     + " XmlSensorDataTypeRef = 'testXmlRef', "
     + " LastMod = '" + new Timestamp(new Date().getTime()).toString() + "' "
     + " WHERE 1=3";
   
   /** Generates an index on the Name column for this table. */
   private static final String indexSensorDataTypeTableStatement = 
     "CREATE UNIQUE INDEX SensorDataTypeIndex ON SensorDataType(Name)";
 
   /** {@inheritDoc} */
   @Override
   public boolean storeSensorDataType(SensorDataType sdt, String xmlSensorDataType, 
       String xmlSensorDataTypeRef) {
     Connection conn = null;
     PreparedStatement s = null;
     try {
       conn = DriverManager.getConnection(connectionURL);
       s = conn.prepareStatement("INSERT INTO SensorDataType VALUES (?, ?, ?, ?)");
       // Order: Name XmlSensorData XmlSensorDataRef LastMod
       s.setString(1, sdt.getName());
       s.setString(2, xmlSensorDataType);
       s.setString(3, xmlSensorDataTypeRef);
       s.setTimestamp(4, new Timestamp(new Date().getTime()));
       s.executeUpdate();
       this.logger.fine("Derby: Inserted SDT" + sdt.getName());
     }
     catch (SQLException e) {
       if (DUPLICATE_KEY.equals(e.getSQLState())) {
         try {
           // Do an update, not an insert.
           s = conn.prepareStatement(
               "UPDATE SensorDataType SET "
               + " XmlSensorDataType=?, " 
               + " XmlSensorDataTypeRef=?, "
               + " LastMod=?"
               + " WHERE Name=?");
           s.setString(1, xmlSensorDataType);
           s.setString(2, xmlSensorDataTypeRef);
           s.setTimestamp(3, new Timestamp(new Date().getTime()));
           s.setString(4, sdt.getName());
           s.executeUpdate();
           this.logger.fine("Derby: Updated SDT " + sdt.getName());
         }
         catch (SQLException f) {
           this.logger.info(derbyError + StackTrace.toString(f));
         }
       }
     }
     finally {
       try {
         s.close();
         conn.close();
       }
       catch (SQLException e) {
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
     return true;
   }
 
   /** {@inheritDoc} */
   @Override
   public void deleteSensorDataType(String sdtName) {
     String statement = "DELETE FROM SensorDataType WHERE Name='" + sdtName + "'";
     deleteResource(statement);
   }
 
   /** {@inheritDoc} */
   @Override
   public String getSensorDataTypeIndex() {
     return getIndex("SensorDataType", "SELECT XmlSensorDataTypeRef FROM SensorDataType");
   }
 
   /** {@inheritDoc} */
   @Override
   public String getSensorDataType(String sdtName) {
     String statement = 
       "SELECT XmlSensorDataType FROM SensorDataType WHERE Name = '" + sdtName + "'";
     return getResource("SensorDataType", statement);
   }
   
   // ********************   Start  User specific stuff here *****************  //
   /** The SQL string for creating the HackyUser table. So named because 'User' is reserved. */
   private static final String createUserTableStatement = 
     "create table HackyUser  "
     + "("
     + " Email VARCHAR(128) NOT NULL, "
     + " Password VARCHAR(128) NOT NULL, "
     + " Role CHAR(16), "
     + " XmlUser VARCHAR(32000) NOT NULL, "
     + " XmlUserRef VARCHAR(1000) NOT NULL, "
     + " LastMod TIMESTAMP NOT NULL, "
     + " PRIMARY KEY (Email) "
     + ")" ;
   
   /** An SQL string to test whether the User table exists and has the correct schema. */
   private static final String testUserTableStatement = 
     " UPDATE HackyUser SET "
     + " Email = 'TestEmail@foo.com', " 
     + " Password = 'changeme', " 
     + " Role = 'basic', " 
     + " XmlUser = 'testXmlResource', "
     + " XmlUserRef = 'testXmlRef', "
     + " LastMod = '" + new Timestamp(new Date().getTime()).toString() + "' "
     + " WHERE 1=3";
   
   /** Generates an index on the Name column for this table. */
   private static final String indexUserTableStatement = 
     "CREATE UNIQUE INDEX UserIndex ON HackyUser(Email)";
 
   /** {@inheritDoc} */
   @Override
   public void deleteUser(String email) {
     String statement = "DELETE FROM HackyUser WHERE Email='" + email + "'";
     deleteResource(statement);
   }
 
   /** {@inheritDoc} */
   @Override
   public String getUser(String email) {
     String statement = "SELECT XmlUser FROM HackyUser WHERE Email = '" + email + "'";
     return getResource("User", statement);
   }
 
 
   /** {@inheritDoc} */
   @Override
   public String getUserIndex() {
     return getIndex("User", "SELECT XmlUserRef FROM HackyUser");
   }
 
   /** {@inheritDoc} */
   @Override
   public boolean storeUser(User user, String xmlUser, String xmlUserRef) {
     Connection conn = null;
     PreparedStatement s = null;
     try {
       conn = DriverManager.getConnection(connectionURL);
       s = conn.prepareStatement("INSERT INTO HackyUser VALUES (?, ?, ?, ?, ?, ?)");
       // Order: Email Password Role XmlUser XmlUserRef LastMod
       s.setString(1, user.getEmail());
       s.setString(2, user.getPassword());
       s.setString(3, user.getRole());
       s.setString(4, xmlUser);
       s.setString(5, xmlUserRef);
       s.setTimestamp(6, new Timestamp(new Date().getTime()));
       s.executeUpdate();
       this.logger.fine("Derby: Inserted User" + user.getEmail());
     }
     catch (SQLException e) {
       if (DUPLICATE_KEY.equals(e.getSQLState())) {
         try {
           // Do an update, not an insert.
           s = conn.prepareStatement(
               "UPDATE HackyUser SET "
               + " Password=?, " 
               + " Role=?, " 
               + " XmlUser=?, " 
               + " XmlUserRef=?, "
               + " LastMod=?"
               + " WHERE Email=?");
           s.setString(1, user.getPassword());
           s.setString(2, user.getRole());
           s.setString(3, xmlUser);
           s.setString(4, xmlUserRef);
           s.setTimestamp(5, new Timestamp(new Date().getTime()));
           s.setString(6, user.getEmail());
           s.executeUpdate();
           this.logger.fine("Derby: Updated User " + user.getEmail());
         }
         catch (SQLException f) {
           this.logger.info(derbyError + StackTrace.toString(f));
         }
       }
       else {
         this.logger.info(derbyError + StackTrace.toString(e));
       }
     }
     finally {
       try {
         s.close();
         conn.close();
       }
       catch (SQLException e) {
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
     return true;
   }
 
   // ********************   Start Project specific stuff here *****************  //
 
   /** The SQL string for creating the Project table.  */
   private static final String createProjectTableStatement = 
     "create table Project  "
     + "("
     + " Owner VARCHAR(128) NOT NULL, "
     + " ProjectName VARCHAR(128) NOT NULL, "
     + " StartTime TIMESTAMP NOT NULL, "
     + " EndTime TIMESTAMP NOT NULL, "
     + " XmlProject VARCHAR(32000) NOT NULL, "
     + " XmlProjectRef VARCHAR(1000) NOT NULL, "
     + " LastMod TIMESTAMP NOT NULL, "
     + " PRIMARY KEY (Owner, ProjectName) "
     + ")" ;
   
   /** An SQL string to test whether the Project table exists and has the correct schema. */
   private static final String testProjectTableStatement = 
     " UPDATE Project SET "
     + " Owner = 'TestEmail@foo.com', " 
     + " ProjectName = 'TestProject', " 
     + " StartTime = '" + new Timestamp(new Date().getTime()).toString() + "', "
     + " EndTime = '" + new Timestamp(new Date().getTime()).toString() + "', "
     + " XmlProject = 'testXmlResource', "
     + " XmlProjectRef = 'testXmlRef', "
     + " LastMod = '" + new Timestamp(new Date().getTime()).toString() + "' "
     + " WHERE 1=3";
   
   /** Generates an index on the Owner/ProjectName columns for this table. */
   private static final String indexProjectTableStatement = 
     "CREATE UNIQUE INDEX ProjectIndex ON Project(Owner, ProjectName)";
 
   /** {@inheritDoc} */
   @Override
   public void deleteProject(User owner, String projectName) {
     String statement =
       "DELETE FROM Project WHERE "
       + ownerEquals + owner.getEmail() + andClause 
       + " ProjectName = '" + projectName + "'";
     deleteResource(statement);
   }
 
   /** {@inheritDoc} */
   @Override
   public String getProject(User owner, String projectName) {
     String statement =
       "SELECT XmlProject FROM Project WHERE "
       + ownerEquals + owner.getEmail() + andClause 
       + " ProjectName ='" + projectName + "'";
     return getResource("Project", statement);
   }
 
   /** {@inheritDoc} */
   @Override
   public String getProjectIndex() {
     return getIndex("Project", "SELECT XmlProjectRef FROM Project");
   }
 
   /** {@inheritDoc} */
   @Override
   public boolean storeProject(Project project, String xmlProject, String xmlProjectRef) {
     Connection conn = null;
     PreparedStatement s = null;
     try {
       conn = DriverManager.getConnection(connectionURL);
       s = conn.prepareStatement("INSERT INTO Project VALUES (?, ?, ?, ?, ?, ?, ?)");
       // Order: Owner ProjectName StartTime EndTime XmlProject XmlProjectRef LastMod
       s.setString(1, project.getOwner());
       s.setString(2, project.getName());
       s.setTimestamp(3, Tstamp.makeTimestamp(project.getStartTime()));
       s.setTimestamp(4, Tstamp.makeTimestamp(project.getEndTime()));
       s.setString(5, xmlProject);
       s.setString(6, xmlProjectRef);
       s.setTimestamp(7, Tstamp.makeTimestamp(project.getLastMod()));
       s.executeUpdate();
       this.logger.fine("Derby: Inserted " + project.getOwner() + " " + project.getName());
     }
     catch (SQLException e) {
       if (DUPLICATE_KEY.equals(e.getSQLState())) {
         try {
           // Do an update, not an insert.
           s = conn.prepareStatement(
               "UPDATE Project SET "
               + " StartTime=?, EndTime=?, XmlProject=?, " 
               + " XmlProjectRef=?, LastMod=?"
               + " WHERE Owner=? AND ProjectName=?");
           s.setTimestamp(1, Tstamp.makeTimestamp(project.getStartTime()));
           s.setTimestamp(2, Tstamp.makeTimestamp(project.getEndTime()));
           s.setString(3, xmlProject);
           s.setString(4, xmlProjectRef);
           s.setTimestamp(5, Tstamp.makeTimestamp(project.getEndTime()));
           s.setString(6, project.getOwner());
           s.setString(7, project.getName());
           s.executeUpdate();
           this.logger.fine("Derby: Updated " + project.getOwner() + " " + project.getName());
         }
         catch (SQLException f) {
           this.logger.info(derbyError + StackTrace.toString(f));
         }
       }
     }
     finally {
       try {
         s.close();
         conn.close();
       }
       catch (SQLException e) {
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
     return true;
   }
   
   // **************************** Internal helper functions *****************************
   
   /**
    * Returns a string containing the Index for the given resource indicated by resourceName.
    * @param resourceName The resource name, such as "Project". 
    * @param statement The SQL Statement to be used to retrieve the resource references.
    * @return The aggregate Index XML string. 
    */
   private String getIndex(String resourceName, String statement) {
     StringBuilder builder = new StringBuilder(512);
     builder.append("<").append(resourceName).append("Index>");
     // Retrieve all the SensorData
     Connection conn = null;
     PreparedStatement s = null;
     ResultSet rs = null;
     try {
       conn = DriverManager.getConnection(connectionURL);
       s = conn.prepareStatement(statement);
       rs = s.executeQuery();
       String resourceRefColumnName = "Xml" + resourceName + "Ref";
       while (rs.next()) {
         builder.append(rs.getString(resourceRefColumnName));
       }
     }
     catch (SQLException e) {
       this.logger.info("Derby: Error in getIndex()" + StackTrace.toString(e));
     }
     finally {
       try {
         rs.close();
         s.close();
         conn.close();
       }
       catch (SQLException e) {
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
     builder.append("</").append(resourceName).append("Index>");
     return builder.toString();
   }
   
   /**
    * Returns a string containing the Resource as XML.
    * @param resourceName The name of the resource, such as "User".
    * @param statement The select statement used to retrieve the resultset containing a single
    * row with that resource.
    * @return The string containing the resource as an XML string.
    */
   private String getResource(String resourceName, String statement) {
     StringBuilder builder = new StringBuilder(512);
     Connection conn = null;
     PreparedStatement s = null;
     ResultSet rs = null;
     try {
       conn = DriverManager.getConnection(connectionURL);
       server.getLogger().fine(executeQueryMsg + statement);
       s = conn.prepareStatement(statement);
       rs = s.executeQuery();
       String resourceXmlColumnName = "Xml" + resourceName;
       while (rs.next()) { // the select statement must guarantee only one row is returned.
         builder.append(rs.getString(resourceXmlColumnName));
       }
     }
     catch (SQLException e) {
       this.logger.info("DB: Error in getResource()" + StackTrace.toString(e));
     }
     finally {
       try {
         rs.close();
         s.close();
         conn.close();
       }
       catch (SQLException e) {
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
     return builder.toString();
   }
   
   /**
    * Deletes the resource, given the SQL statement to perform the delete.
    * @param statement The SQL delete statement. 
    */
   private void deleteResource(String statement) {
     Connection conn = null;
     PreparedStatement s = null;
     try {
       conn = DriverManager.getConnection(connectionURL);
       server.getLogger().fine("Derby: " + statement);
       s = conn.prepareStatement(statement);
       s.executeUpdate();
     }
     catch (SQLException e) {
       this.logger.info("Derby: Error in deleteResource()" + StackTrace.toString(e));
     }
     finally {
       try {
         s.close();
         conn.close();
       }
       catch (SQLException e) {
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
   }
 }
