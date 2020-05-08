 package org.hackystat.sensorbase.db.derby;
 
 import static org.hackystat.sensorbase.server.ServerProperties.DB_DIR_KEY;
 
 import java.math.BigInteger;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.hackystat.sensorbase.db.DbImplementation;
 import org.hackystat.utilities.stacktrace.StackTrace;
 import org.hackystat.utilities.tstamp.Tstamp;
 import org.hackystat.sensorbase.resource.projects.jaxb.Project;
 import org.hackystat.sensorbase.resource.projects.jaxb.ProjectSummary;
 import org.hackystat.sensorbase.resource.projects.jaxb.SensorDataSummaries;
 import org.hackystat.sensorbase.resource.projects.jaxb.SensorDataSummary;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
 import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataType;
 import org.hackystat.sensorbase.resource.users.jaxb.User;
 import org.hackystat.sensorbase.server.Server;
 
 
 /**
  * Provides a implementation of DbImplementation using Derby in embedded mode.
  * 
  * Note: If you are using this implementation as a guide for implementing an alternative database,
  * you should be aware that this implementation does not do connection pooling.  It turns out
  * that embedded Derby does not require connection pooling, so it is not present in this code.
  * You will probably want it for your version, of course. 
  * 
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
 
   /** Required by PMD since this string occurs multiple times in this file. */
   private static final String sdtEquals = " sdt = '";
   private static final String toolEquals = " tool = '";
   
   /** Required by PMD as above. */
   private static final String quoteAndClause = "' AND ";
   private static final String andClause = " AND ";
   private static final String selectPrefix = "SELECT XmlSensorDataRef FROM SensorData WHERE "; 
   private static final String selectSnapshot = 
     "SELECT XmlSensorDataRef, Runtime, Tool FROM SensorData WHERE "; 
   private static final String orderByTstamp = " ORDER BY tstamp";
   private static final String orderByRuntime = " ORDER BY runtime DESC";
   private static final String derbyError = "Derby: Error ";
   private static final String indexSuffix = "Index>";
   private static final String xml = "Xml";
 
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
         @Override 
         public void run() {
           Connection conn = null;
           try {
             conn = DriverManager.getConnection("jdbc:derby:;shutdown=true");
           }
           catch (Exception e) {
             System.out.println("Derby shutdown hook results: " + e.getMessage());
           }
           finally {
             try {
               conn.close();
             }
             catch (Exception e) { //NOPMD
               // we tried.
             }
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
       s.execute(indexSensorDataTstampStatement);
       s.execute(indexSensorDataRuntimeStatement);
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
   
   private static final String indexSensorDataTstampStatement = 
     "CREATE INDEX TstampIndex ON SensorData(Tstamp asc)";
 
   private static final String indexSensorDataRuntimeStatement = 
     "CREATE INDEX RuntimeIndex ON SensorData(Runtime desc)";
 
 
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
       selectPrefix
       + ownerEquals + user.getEmail() + quoteAndClause
       + " Sdt='" + sdtName + "'"
       + orderByTstamp;
     return getIndex("SensorData", st);
   }
   
   /** {@inheritDoc} */
   @Override
   public String getSensorDataIndex(List<User> users, XMLGregorianCalendar startTime, 
       XMLGregorianCalendar endTime, List<String> uriPatterns, String sdt) {
     String statement;
     if (sdt == null) { // Retrieve sensor data of all SDTs 
       statement =
         selectPrefix
         + constructOwnerClause(users)
         + andClause 
         + " (Tstamp BETWEEN TIMESTAMP('" + Tstamp.makeTimestamp(startTime) + "') AND " //NOPMD
         + " TIMESTAMP('" + Tstamp.makeTimestamp(endTime) + "'))" //NOPMD
         + constructLikeClauses(uriPatterns)
         + orderByTstamp;
     }
     else { // Retrieve sensor data of the specified SDT.
       statement = 
         selectPrefix
         + constructOwnerClause(users)
         + andClause  
         + sdtEquals + sdt + quoteAndClause 
         + " (Tstamp BETWEEN TIMESTAMP('" + Tstamp.makeTimestamp(startTime) + "') AND " //NOPMD
         + " TIMESTAMP('" + Tstamp.makeTimestamp(endTime) + "'))" //NOPMD
         + constructLikeClauses(uriPatterns)
         + orderByTstamp;
     }
     //System.out.println(statement);
     return getIndex("SensorData", statement);
   }
   
   /** {@inheritDoc} */
   @Override
   public String getProjectSensorDataSnapshot(List<User> users, XMLGregorianCalendar startTime, 
       XMLGregorianCalendar endTime, List<String> uriPatterns, String sdt, String tool) {
     String statement;
     if (tool == null) { // Retrieve sensor data with latest runtime regardless of tool.
       statement =
         selectSnapshot
         + constructOwnerClause(users)
         + andClause 
         + sdtEquals + sdt + quoteAndClause 
         + " (Tstamp BETWEEN TIMESTAMP('" + Tstamp.makeTimestamp(startTime) + "') AND " //NOPMD
         + " TIMESTAMP('" + Tstamp.makeTimestamp(endTime) + "'))" //NOPMD
         + constructLikeClauses(uriPatterns)
         + orderByRuntime;
     }
     else { // Retrieve sensor data with the latest runtime for the specified tool.
       statement = 
         selectSnapshot
         + constructOwnerClause(users)
         + andClause  
         + sdtEquals + sdt + quoteAndClause 
         + toolEquals + tool + quoteAndClause 
         + " (Tstamp BETWEEN TIMESTAMP('" + Tstamp.makeTimestamp(startTime) + "') AND " //NOPMD
         + " TIMESTAMP('" + Tstamp.makeTimestamp(endTime) + "'))" //NOPMD
         + constructLikeClauses(uriPatterns)
         + orderByRuntime;
     }
     //Generate a SensorDataIndex string that contains only entries with the latest runtime.
     //System.out.println(statement);
     return getSnapshotIndex(statement);
   }
   
   /** {@inheritDoc} */
   @Override
   public String getSensorDataIndex(List<User> users, XMLGregorianCalendar startTime, 
       XMLGregorianCalendar endTime, List<String> uriPatterns, int startIndex, 
       int maxInstances) {
     String statement;
 
     statement =
         selectPrefix
         + constructOwnerClause(users)
         + andClause 
         + " (Tstamp BETWEEN TIMESTAMP('" + Tstamp.makeTimestamp(startTime) + "') AND " //NOPMD
         + " TIMESTAMP('" + Tstamp.makeTimestamp(endTime) + "'))" //NOPMD
         + constructLikeClauses(uriPatterns)
         + orderByTstamp;
     //System.out.println(statement);
     return getIndex("SensorData", statement, startIndex, maxInstances);
   }
   
   /**
    * Constructs a set of LIKE clauses corresponding to the passed set of UriPatterns.
    * <p>
    * Each UriPattern is translated in the following way:
    * <ul>
    * <li> If there is an occurrence of a "\" or a "/" in the UriPattern, then 
    * two translated UriPatterns are generated, one with all "\" replaced with "/", and one with 
    * all "/" replaced with "\".
    * <li> The escape character is "\", unless we are generating a LIKE clause containing a 
    * "\", in which case the escape character will be "/".
    * <li> All occurrences of "%" in the UriPattern are escaped.
    * <li> All occurrences of "_" in the UriPattern are escaped.
    * <li> All occurrences of "*" are changed to "%".
    * </ul>
    * The new set of 'translated' UriPatterns are now used to generate a set of LIKE clauses
    * with the following form:
    * <pre>
    * (RESOURCE like 'translatedUriPattern1' escape 'escapeChar1') OR
    * (RESOURCE like 'translatedUriPattern2' escape 'escapeChar2') ..
    * </pre>
    * <p>
    * There is one special case.  If the List<UriPattern> is null, empty, or consists of exactly one 
    * UriPattern which is "**" or "*", then the empty string is returned. This is an optimization for
    * the common case where all resources should be matched and so we don't need any LIKE clauses.
    * <p>
    * We return either the empty string (""), or else a string of the form:
    * " AND ([like clause] AND [like clause] ... )"
    * This enables the return value to be appended to the SELECT statement.
    * <p>
    * This method is static and package private to support testing. See the class 
    * TestConstructUriPattern for example invocations and expected return values. 
    *  
    * @param uriPatterns The list of uriPatterns.
    * @return The String to be used in the where clause to check for resource correctness.
    */
   static String constructLikeClauses(List<String> uriPatterns) {
     // Deal with special case. UriPatterns is null, or empty, or "**", or "*"
     if (((uriPatterns == null) || uriPatterns.isEmpty()) ||
         ((uriPatterns.size() == 1) && uriPatterns.get(0).equals("**")) ||
         ((uriPatterns.size() == 1) && uriPatterns.get(0).equals("*"))) {
       return "";
     }
     // Deal with the potential presence of path separator character in UriPattern.
     List<String> translatedPatterns = new ArrayList<String>();
     for (String pattern : uriPatterns) {
       if (pattern.contains("\\") || pattern.contains("/")) {
         translatedPatterns.add(pattern.replace('\\', '/'));
         translatedPatterns.add(pattern.replace('/', '\\'));
       }
       else {
         translatedPatterns.add(pattern);
       }        
     }
     // Now escape the SQL wildcards, and make our UriPattern wildcard into the SQL wildcard.
     for (int i = 0; i < translatedPatterns.size(); i++) {
       String pattern = translatedPatterns.get(i);
       pattern = pattern.replace("%", "`%"); // used to be /
       pattern = pattern.replace("_", "`_"); // used to be /
       pattern = pattern.replace('*', '%');
       translatedPatterns.set(i, pattern);
     }
 
     // Now generate the return string: " AND (<like clause> OR <like clause> ... )".
     StringBuffer buff = new StringBuffer();
     buff.append(" AND (");
     if (!translatedPatterns.isEmpty()) {
       buff.append(makeLikeClause(translatedPatterns, "`")); // used to be /
     }
 
     buff.append(')');
     
     return buff.toString();
   }
   
   /**
    * Creates a set of LIKE clauses with the specified escape character.
    * @param patterns The patterns. 
    * @param escape The escape character.
    * @return The StringBuffer with the LIKE clauses. 
    */
   private static StringBuffer makeLikeClause(List<String> patterns, String escape) {
     StringBuffer buff = new StringBuffer(); //NOPMD generates false warning about buff size.
     if (patterns.isEmpty()) {
       return buff;
     }
     for (Iterator<String> i = patterns.iterator(); i.hasNext(); ) {
       String pattern = i.next();
       buff.append("(RESOURCE LIKE '");
       buff.append(pattern);
       buff.append("' ESCAPE '");
       buff.append(escape);
       buff.append("')");
       if (i.hasNext()) {
         buff.append(" OR ");
       }
     }
     buff.append(' ');
     return buff;
   }
   
   /**
    * Constructs a clause of form ( OWNER = 'user1' [ OR OWNER = 'user2']* ). 
    * @param users The list of users whose ownership is being searched for.
    * @return The String to be used in the where clause to check for ownership.
    */
   private String constructOwnerClause(List<User> users) {
     StringBuffer buff = new StringBuffer();
     buff.append('(');
     // Use old school iterator so we can do a hasNext() inside the loop.
     for (Iterator<User> i = users.iterator(); i.hasNext(); ) {
       User user = i.next();
       buff.append(ownerEquals);
       buff.append(user.getEmail());
       buff.append('\'');
       if (i.hasNext()) {
         buff.append(" OR");
       }
     }
     buff.append(") ");
     return buff.toString();
   }
   
   /** {@inheritDoc} */
   @Override
   public String getSensorDataIndexLastMod(User user, XMLGregorianCalendar lastModStartTime,
       XMLGregorianCalendar lastModEndTime) {
     String statement = 
       selectPrefix
       + ownerEquals + user.getEmail() + quoteAndClause 
       + " LastMod BETWEEN TIMESTAMP('" + Tstamp.makeTimestamp(lastModStartTime) + "') AND "
       + " TIMESTAMP('" + Tstamp.makeTimestamp(lastModEndTime) + "')";
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
         selectPrefix
         + ownerEquals + user.getEmail() + quoteAndClause 
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
       + ownerEquals + user.getEmail() + quoteAndClause 
       + " Tstamp='" + Tstamp.makeTimestamp(timestamp) + "'";
     deleteResource(statement);
   }
   
   /** {@inheritDoc} */
   @Override
   public void deleteSensorData(User user) {
     String statement =
       "DELETE FROM SensorData WHERE " + ownerEquals + user.getEmail() + "'";
     deleteResource(statement);
     //compressTables();  // this should be done separately as part of some maintenance. 
   }
 
   /** {@inheritDoc} */
   @Override
   public String getSensorData(User user, XMLGregorianCalendar timestamp) {
     String statement =
       "SELECT XmlSensorData FROM SensorData WHERE "
       + ownerEquals + user.getEmail() + quoteAndClause 
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
       + ownerEquals + owner.getEmail() + quoteAndClause 
       + " ProjectName = '" + projectName + "'";
     deleteResource(statement);
   }
 
   /** {@inheritDoc} */
   @Override
   public String getProject(User owner, String projectName) {
     String statement =
       "SELECT XmlProject FROM Project WHERE "
       + ownerEquals + owner.getEmail() + quoteAndClause 
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
   public ProjectSummary getProjectSummary(List<User> users, XMLGregorianCalendar startTime, 
       XMLGregorianCalendar endTime, List<String> uriPatterns, String href) {
     // Make a statement to return all SensorData for this project in the time period.
     String statement = 
       "SELECT Sdt, Tool FROM SensorData WHERE "
       + constructOwnerClause(users)
       + andClause 
       + " (Tstamp BETWEEN TIMESTAMP('" + Tstamp.makeTimestamp(startTime) + "') AND "
       + " TIMESTAMP('" + Tstamp.makeTimestamp(endTime) + "'))"
       + constructLikeClauses(uriPatterns);
     
     // Create the [SDT, Tool] -> NumInstances data structure.
     Map<String, Map<String, Integer>> sdtInstances = new HashMap<String, Map<String, Integer>>();
     
     // Retrieve the sensordata for this project and time period.
     Connection conn = null;
     PreparedStatement s = null;
     ResultSet rs = null;
     try {
       conn = DriverManager.getConnection(connectionURL);
       s = conn.prepareStatement(statement);
       rs = s.executeQuery();
       // Loop through all retrieved SensorData records.
       while (rs.next()) {
         String sdt = rs.getString("Sdt");
         String tool = rs.getString("Tool");
         // Don't want null SDTs or Tools, call them the empty string instead.
         if (sdt == null) {
           sdt = "";
         }
         if (tool == null) {
           tool = "";
         }
         // Now update our numInstance data structure.
         // First, initialize the data structure if this is a new SDT.
         if (!sdtInstances.containsKey(sdt)) {
           Map<String, Integer> tool2NumInstances = new HashMap<String, Integer>();
           tool2NumInstances.put(tool, 0);
           sdtInstances.put(sdt, tool2NumInstances);
         }
         Map<String, Integer> tool2NumInstances = sdtInstances.get(sdt);
         // Second, initialize the data structure if this is a new tool for a preexisting SDT.
         if (tool2NumInstances.get(tool) == null) {
           tool2NumInstances.put(tool, 0);
         }
         // Finally, increment this entry.
         tool2NumInstances.put(tool, tool2NumInstances.get(tool) + 1);
       }
     }
     catch (SQLException e) {
       this.logger.info("Derby: Error in getProjectSummary()" + StackTrace.toString(e));
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
     
     //Now create the project summary object from our data structures.
     return makeProjectSummary(href, startTime, endTime, sdtInstances);
   }
 
   /**
    * Creates a ProjectSummary instances from the passed data. 
    * @param href  The Href representing this resource.
    * @param startTime The startTime for this data.
    * @param endTime The endTime for this data.
    * @param sdtInstances The data structure containing the instances. 
    * @return The ProjectSummary instance. 
    */
   private ProjectSummary makeProjectSummary(String href, XMLGregorianCalendar startTime, 
       XMLGregorianCalendar endTime, Map<String, Map<String, Integer>> sdtInstances) {
     ProjectSummary projectSummary = new ProjectSummary();
     projectSummary.setHref(href);
     projectSummary.setStartTime(startTime);
     projectSummary.setEndTime(endTime);
     projectSummary.setLastMod(Tstamp.makeTimestamp());
     SensorDataSummaries summaries = new SensorDataSummaries();
     projectSummary.setSensorDataSummaries(summaries);
     int totalInstances = 0;
     for (Map.Entry<String, Map<String, Integer>> entry : sdtInstances.entrySet()) {
       String sdt = entry.getKey();
       Map<String, Integer> tool2NumInstances = entry.getValue();
       for (Map.Entry<String, Integer> entry2 : tool2NumInstances.entrySet()) {
         SensorDataSummary summary = new SensorDataSummary();
         summary.setSensorDataType(sdt);
         summary.setTool(entry2.getKey());
         int numInstances = entry2.getValue();
         totalInstances += numInstances;
         summary.setNumInstances(BigInteger.valueOf(numInstances));
         summaries.getSensorDataSummary().add(summary);
       }
     }
     summaries.setNumInstances(BigInteger.valueOf(totalInstances));
     return projectSummary;
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
     builder.append("<").append(resourceName).append(indexSuffix);
     // Retrieve all the SensorData
     Connection conn = null;
     PreparedStatement s = null;
     ResultSet rs = null;
     try {
       conn = DriverManager.getConnection(connectionURL);
       s = conn.prepareStatement(statement);
       rs = s.executeQuery();
       String resourceRefColumnName = xml + resourceName + "Ref";
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
     builder.append("</").append(resourceName).append(indexSuffix);
     //System.out.println(builder.toString());
     return builder.toString();
   }
   
   /**
    * Returns a string containing the Index of all of the SensorData whose runtime field matches
    * the first runtime in the result set.  Since the passed statement will retrieve sensor
    * data in the given time period ordered in descending order by runtime, this should result
    * in an index containing only  
    * @param statement The SQL Statement to be used to retrieve the resource references.
    * @return The aggregate Index XML string. 
    */
   private String getSnapshotIndex(String statement) {
     String resourceName = "SensorData";
     StringBuilder builder = new StringBuilder(512);
     builder.append("<").append(resourceName).append(indexSuffix);
     // Retrieve all the SensorData
     Connection conn = null;
     PreparedStatement s = null;
     ResultSet rs = null;
     String firstRunTime = null;
     try {
       conn = DriverManager.getConnection(connectionURL);
       s = conn.prepareStatement(statement);
       rs = s.executeQuery();
       String resourceRefColumnName = xml + resourceName + "Ref";
       boolean finished = false;
       // Add all entries with the first retrieved nruntime value to the index.
       while (rs.next() && !finished) {
         String runtime = rs.getString("Runtime");
         // Should never be null, but just in case. 
         if (runtime != null) {
           // Initial firstRunTime to the first retrieved non-null runtime value.
           if (firstRunTime == null) {
             firstRunTime = runtime;
           }
           // Now add every entry whose runtime equals the first retrieved run time.
           if (runtime.equals(firstRunTime)) {
             builder.append(rs.getString(resourceRefColumnName));
           }
           else {
             // As soon as we find a runtime not equal to firstRunTime, we can stop.
             finished = true;
           }
         }
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
     builder.append("</").append(resourceName).append(indexSuffix);
     //System.out.println(builder.toString());
     return builder.toString();
   }
   
   /**
    * Returns a string containing the Index for the given resource indicated by resourceName, 
    * returning only the instances starting at startIndex, and with the maximum number of
    * returned instances indicated by maxInstances.   
    * @param resourceName The resource name, such as "Project".
    * @param startIndex The (zero-based) starting index for instances to be returned.
    * @param maxInstances The maximum number of instances to return.  
    * @param statement The SQL Statement to be used to retrieve the resource references.
    * @return The aggregate Index XML string. 
    */
   private String getIndex(String resourceName, String statement, int startIndex, int maxInstances) {
     StringBuilder builder = new StringBuilder(512);
     builder.append("<").append(resourceName).append(indexSuffix);
     // Retrieve all the SensorData to start.
     Connection conn = null;
     PreparedStatement s = null;
     ResultSet rs = null;
     try {
       conn = DriverManager.getConnection(connectionURL);
       s = conn.prepareStatement(statement);
       rs = s.executeQuery();
       int currIndex = 0;
       int totalInstances = 0;
       String resourceRefColumnName = xml + resourceName + "Ref";
       while (rs.next()) {
         if ((currIndex >= startIndex) && (totalInstances < maxInstances)) {
           builder.append(rs.getString(resourceRefColumnName));
           totalInstances++;
         }
         currIndex++;
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
     builder.append("</").append(resourceName).append(indexSuffix);
     //System.out.println(builder.toString());
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
       String resourceXmlColumnName = xml + resourceName;
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
 
 //  TO BE ADDED TO A GENERAL 'MAINTENANCE' API 
 //  /**
 //   * A utility procedure that reclaims disk space after large deletes. 
 //   */
 //  private void compressTables() {
 //    this.logger.fine("Starting to compress tables.");
 //    Connection conn = null;
 //    CallableStatement cs = null;
 //    try {
 //      conn = DriverManager.getConnection(connectionURL);
 //      cs = conn.prepareCall("CALL SYSCS_UTIL.SYSCS_COMPRESS_TABLE(?, ?, ?)");
 //      cs.setString(1, "APP");
 //      cs.setString(2, "SENSORDATA");
 //      cs.setShort(3, (short) 1);
 //      cs.execute();
 //      cs.setString(2, "SENSORDATATYPE");
 //      cs.execute();
 //      cs.setString(2, "HACKYUSER");
 //      cs.execute();
 //      cs.setString(2, "PROJECT");
 //      cs.execute();
 //    }
 //    catch (SQLException e) {
 //      this.logger.info("Derby: Error in compressTables()" + StackTrace.toString(e));
 //    }
 //    finally {
 //      try {
 //        cs.close();
 //        conn.close();
 //      }
 //      catch (SQLException e) {
 //        this.logger.warning(errorClosingMsg + StackTrace.toString(e));
 //      }
 //    }
 //    this.logger.fine("Finished compressing tables.");
 //  }
 }
