 package org.hackystat.sensorbase.db.postgres;
 
 import java.io.ByteArrayInputStream;
 import java.math.BigInteger;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.hackystat.sensorbase.db.DbImplementation;
 import org.hackystat.sensorbase.resource.projects.jaxb.Project;
 import org.hackystat.sensorbase.resource.projects.jaxb.ProjectSummary;
 import org.hackystat.sensorbase.resource.projects.jaxb.SensorDataSummaries;
 import org.hackystat.sensorbase.resource.projects.jaxb.SensorDataSummary;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
 import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataType;
 import org.hackystat.sensorbase.resource.users.jaxb.User;
 import org.hackystat.sensorbase.server.Server;
 import org.hackystat.sensorbase.server.ServerProperties;
 import org.hackystat.utilities.stacktrace.StackTrace;
 import org.hackystat.utilities.tstamp.Tstamp;
 
 /**
  * Provides a implementation of DbImplementation using Postgres.
  * 
  * @author Philip Johnson
  * @author Austen Ito
  */
 public class PostgresImplementation extends DbImplementation {
   /** The database connection url. */
   private final String connectionURL;
   /** The database connection. */
   private Connection connection = null;
   /** Indicates whether this database was initialized or was pre-existing. */
   private boolean isFreshlyCreated;
 
   /** The logger message for connection closing errors. */
   private static final String errorClosingMsg = "Postgres: Error while closing. \n";
 
   /** The logger message when executing a query. */
   private static final String executeQueryMsg = "Postgres: Executing query ";
 
   /** Required by PMD since this string occurs multiple times in this file. */
   private static final String ownerIdEquals = " Owner_Id='";
   private static final String sdtIdEquals = " Sdt_Id= '";
   private static final String toolEquals = " tool = '";
 
   /** Required by PMD as above. */
   private static final String quoteAndClause = "' AND ";
   private static final String andClause = " AND ";
   private static final String tstampBetweenTstamp = " Tstamp BETWEEN TIMESTAMP '";
   private static final String timeStampClause = " TIMESTAMP '";
   private static final String selectPrefix = "SELECT XmlSensorDataRef FROM SensorData WHERE ";
   private static final String selectSnapshot = "SELECT XmlSensorDataRef, Runtime, Tool FROM "
       + "SensorData WHERE ";
   private static final String orderByTstamp = " ORDER BY tstamp";
   private static final String orderByRuntime = " ORDER BY runtime DESC";
   private static final String postgresError = "Postgres: Error ";
   private static final String indexSuffix = "Index>";
   private static final String xml = "Xml";
   /** The postgres database name. */
   public static final String POSTGRES_DB = "sensorbase.db.postgres.db";
   /** The postgres server username. */
   public static final String POSTGRES_USER = "sensorbase.db.postgres.user";
   /** The postgres server password. */
   public static final String POSTGRES_PASSWORD = "sensorbase.db.postgres.password";
   
   /** The SQL state indicating that INSERT tried to add data to a table with a preexisting key. */
   private static final String DUPLICATE_KEY = "23505";
 
 
   /**
    * Instantiates the Postgres implementation. Throws a Runtime exception if the
    * Postgres jar file cannot be found on the classpath.
    * @param server The SensorBase server instance.
    */
   public PostgresImplementation(Server server) {
     super(server);
     ServerProperties props = new ServerProperties();
    System.out.println(props.get(POSTGRES_DB));
     this.connectionURL = "jdbc:postgresql:" + props.get(POSTGRES_DB)
         + "?user=" + props.get(POSTGRES_USER) + "&password="
         + props.get(POSTGRES_PASSWORD);
     // Try to load the derby driver. 
     try {
       Class.forName("org.postgresql.Driver"); 
     } 
     catch (java.lang.ClassNotFoundException e) {
       String msg = "Postgres: Exception during DbManager initialization: "
         + "Postgres not on CLASSPATH.";
       this.logger.warning(msg + "\n" + StackTrace.toString(e));
       throw new RuntimeException(msg, e);
     }
 
     try {
       this.connection = DriverManager.getConnection(this.connectionURL);
     }
     catch (SQLException e) {
       this.logger.warning("Postgres: failed to open connection." + StackTrace.toString(e));
     }
   }
 
   /** {@inheritDoc} */
   @Override
   public void initialize() {
     // No initialization is needed.
   }
 
   /** {@inheritDoc} */
   @Override
    public boolean storeSensorData(SensorData data, String xmlSensorData, String xmlSensorDataRef) {
      PreparedStatement preparedStatement = null;
      try {
        preparedStatement = this.connection.prepareStatement("INSERT INTO SensorData VALUES (?, "
            + "(select id from hackyuser where email = ?), ?, "
            + "(select id from sensordatatype where name = ?), ?, ?, ?, ?, ?, ?)");
        // Order: Id Owner_Id Tstamp Sdt_id Runtime Tool Resource LastMod
        // XmlSensorData XmlSensorDataRef
        Object uuid = UUID.randomUUID();
        preparedStatement.setObject(1, uuid, Types.OTHER);
        preparedStatement.setString(2, data.getOwner());
        preparedStatement.setTimestamp(3, Tstamp.makeTimestamp(data.getTimestamp()));
        preparedStatement.setString(4, data.getSensorDataType());
        preparedStatement.setTimestamp(5, Tstamp.makeTimestamp(data.getRuntime()));
        preparedStatement.setString(6, data.getTool());
        preparedStatement.setString(7, data.getResource());
        preparedStatement.setTimestamp(8, new Timestamp(new Date().getTime()));
        preparedStatement.setString(9, xmlSensorData);
        preparedStatement.setString(10, xmlSensorDataRef);
        preparedStatement.executeUpdate();
        this.storeSensorDataProperties(uuid, xmlSensorData, false);
        this.logger.fine("Postgres: Inserted " + data.getOwner() + " " + data.getTimestamp());
      }
      catch (SQLException e) {
        if (DUPLICATE_KEY.equals(e.getSQLState())) {
          PreparedStatement sensordataIdStatement = null;
          ResultSet sensordataIdResultSet = null;
          try {
            preparedStatement = this.connection.prepareStatement("UPDATE SensorData SET "
                + " sdt_id=(select id from sensordatatype where name = ?), runtime=?, tool=?, " 
                + " resource=?, xmlsensordata=?, xmlsensordataRef=?, lastmod=?"
                + " WHERE owner_id=(select id from hackyuser where email = ?) AND tstamp=?");
            // Order: Id Owner_Id Tstamp Sdt_id Runtime Tool Resource LastMod
            // XmlSensorData XmlSensorDataRef
            preparedStatement.setString(1, data.getSensorDataType());
            preparedStatement.setTimestamp(2, Tstamp.makeTimestamp(data.getRuntime()));
            preparedStatement.setString(3, data.getTool());
            preparedStatement.setString(4, data.getResource());
            preparedStatement.setString(5, xmlSensorData);
            preparedStatement.setString(6, xmlSensorDataRef);
            preparedStatement.setTimestamp(7, new Timestamp(new Date().getTime()));
            preparedStatement.setString(8, data.getOwner());
            preparedStatement.setTimestamp(9, Tstamp.makeTimestamp(data.getTimestamp()));
            preparedStatement.executeUpdate();
            
            String query = "SELECT sensordata.id FROM SensorData, hackyuser where email = '"  
              + data.getOwner() + "' and sensordata.owner_id = hackyuser.id AND "
              + " SensorData.Tstamp = '" + Tstamp.makeTimestamp(data.getTimestamp()) + "'";
            sensordataIdStatement = this.connection.prepareStatement(query);
            sensordataIdResultSet = sensordataIdStatement.executeQuery();
            Object uuid = null;
            if (sensordataIdResultSet.next()) {
              uuid = sensordataIdResultSet.getObject(1);
            }
            
            this.storeSensorDataProperties(uuid, xmlSensorData, true);
          }
          catch (SQLException f) {
            this.logger.info(postgresError + StackTrace.toString(f));
          }
          finally {
            try {
              sensordataIdStatement.close();
              sensordataIdResultSet.close();
            }
            catch (SQLException e2) {
              this.logger.warning(errorClosingMsg + StackTrace.toString(e2));
            }
          }
          this.logger.fine("Postgres: Updated " + data.getOwner() + " " + data.getTimestamp());
        }
      }
      finally {
        try {
          preparedStatement.close();
        }
        catch (SQLException e) {
          this.logger.warning(errorClosingMsg + StackTrace.toString(e));
        }
      }
      return true;
    }
   
   /** {@inheritDoc} */
   public boolean storeSensorData2(SensorData data, String xmlSensorData, String xmlSensorDataRef) {
     PreparedStatement s = null;
     ResultSet userResultSet = null;
     ResultSet sdtResultSet = null;
     ResultSet dataResultSet = null;
 
     try {
       // Get the user and sdt associated with the data.
       userResultSet = this.getUserRecord(this.connection, data.getOwner());
       sdtResultSet = this.getSdtRecord(this.connection, data.getSensorDataType());
 
       // Only store data if the sdt and user exists.
       boolean hasSdt = sdtResultSet.next();
       boolean hasUser = userResultSet.next();
       if (hasSdt && hasUser) {
         Object ownerId = userResultSet.getObject("Id");
         Object sdtId = sdtResultSet.getObject("Id");
 
         // Get the amount of records with the owner and timestamp.
         dataResultSet = this
             .getSensorDataRecord(this.connection, ownerId, data.getTimestamp());
 
         // If the user with the same timestamp exists, perform an update.
         if (dataResultSet.next()) {
           s = this.connection.prepareStatement("UPDATE SensorData SET "
               + " Sdt_Id=?, Runtime=?, Tool=?, Resource=?, XmlSensorData=?, "
               + " XmlSensorDataRef=?, LastMod=?" + " WHERE Owner_Id=?" + andClause
               + "Tstamp=?");
           s.setObject(1, sdtId, Types.OTHER);
           s.setTimestamp(2, Tstamp.makeTimestamp(data.getRuntime()));
           s.setString(3, data.getTool());
           s.setString(4, data.getResource());
           s.setString(5, xmlSensorData);
           s.setString(6, xmlSensorDataRef);
           s.setTimestamp(7, new Timestamp(new Date().getTime()));
           s.setObject(8, ownerId, Types.OTHER);
           s.setTimestamp(9, Tstamp.makeTimestamp(data.getTimestamp()));
           s.executeUpdate();
           this.storeSensorDataProperties(dataResultSet.getObject("Id"), xmlSensorData, true);
           this.logger.fine("Postgres: Updated " + data.getOwner() + " " + data.getTimestamp());
         }
         // Insert a new sensordata record.
         else {
           s = this.connection
               .prepareStatement("INSERT INTO SensorData VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
           // Order: Id Owner_Id Tstamp Sdt_id Runtime Tool Resource LastMod
           // XmlSensorData XmlSensorDataRef
           Object uuid = UUID.randomUUID();
           s.setObject(1, uuid, Types.OTHER);
           s.setObject(2, ownerId, Types.OTHER);
           s.setTimestamp(3, Tstamp.makeTimestamp(data.getTimestamp()));
           s.setObject(4, sdtId, Types.OTHER);
           s.setTimestamp(5, Tstamp.makeTimestamp(data.getRuntime()));
           s.setString(6, data.getTool());
           s.setString(7, data.getResource());
           s.setTimestamp(8, new Timestamp(new Date().getTime()));
           s.setString(9, xmlSensorData);
           s.setString(10, xmlSensorDataRef);
           s.executeUpdate();
           this.storeSensorDataProperties(uuid, xmlSensorData, false);
           this.logger
               .fine("Postgres: Inserted " + data.getOwner() + " " + data.getTimestamp());
         }
       }
       else {
         this.logger.fine("Postgres: Data not stored.  " + data.getSensorDataType()
             + " SDT exists: " + hasSdt + ". " + data.getOwner() + " user exists: " + hasUser);
       }
     }
     catch (SQLException e) {
       this.logger.info(postgresError + StackTrace.toString(e));
     }
     finally {
       try {
         userResultSet.close();
         sdtResultSet.close();
         if (dataResultSet != null) {
           dataResultSet.close();
         }
         if (s != null) {
           s.close();
         }
       }
       catch (SQLException e) {
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
     return true;
   }
 
   /**
    * Stores the optional properties found in the specified sensor data string.
    * The properties are related to a sensor data record via the specified id.
    * @param sensorDataId the specified sensor data record id.
    * @param xmlSensorData the string containing the sensor data properties.
    * @param isUpdating true if the properties are to be updated, false to insert
    * a new record.
    */
   private void storeSensorDataProperties(Object sensorDataId, String xmlSensorData,
       boolean isUpdating) {
     PreparedStatement s = null;
 
     try {
       // conn = DriverManager.getConnection(connectionURL);
       Map<String, String> keyValMap = this.getPropertiesMap(xmlSensorData);
       // The sensordata properties exists, let's update it.
       if (isUpdating) {
         for (Map.Entry<String, String> entry : keyValMap.entrySet()) {
           s = this.connection.prepareStatement("UPDATE SensorData_Properties SET "
               + " Key=?, Value=?" + " WHERE SensorData_Id=?");
           s.setString(1, entry.getKey());
           s.setString(2, entry.getValue());
           s.setObject(3, sensorDataId, Types.OTHER);
           s.executeUpdate();
           this.logger.fine("Postgres: Update Key=" + entry.getKey() + ", Value="
               + entry.getValue());
         }
       }
       else { // No properties, let's create a new record.
         for (Map.Entry<String, String> entry : keyValMap.entrySet()) {
           s = this.connection
               .prepareStatement("INSERT INTO SensorData_Properties VALUES (?, ?, ?, ?)");
           // Order: Id SensorData_Id Key Value
           // XmlSensorData XmlSensorDataRef
           s.setObject(1, UUID.randomUUID(), Types.OTHER);
           s.setObject(2, sensorDataId, Types.OTHER);
           s.setString(3, entry.getKey());
           s.setString(4, entry.getValue());
           s.executeUpdate();
           this.logger.fine("Postgres: Inserted Key=" + entry.getKey() + ", Value="
               + entry.getValue());
         }
       }
     }
     catch (SQLException e) {
       this.logger.warning(errorClosingMsg + StackTrace.toString(e));
     }
     finally {
       try {
         if (s != null) {
           s.close();
         }
       }
       catch (SQLException e) {
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
   }
 
   /**
    * The helper method returning a a mapping of Property Key-> Property Value.
    * @param xmlSensorData the xml string with the property keys and values.
    * @return the properties map.
    */
   private Map<String, String> getPropertiesMap(String xmlSensorData) {
     try {
       SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
       SensorPropertiesHandler handler = new SensorPropertiesHandler();
       parser.parse(new ByteArrayInputStream(xmlSensorData.getBytes()), handler);
       return handler.getKeyValMap();
     }
     catch (Exception e) {
       this.logger
           .warning("Error reading the sensor data properties:" + StackTrace.toString(e));
     }
     return new HashMap<String, String>();
   }
 
   /**
    * The helper method used to return a HackyUser ResultSet which has the
    * specified email.
    * @param conn the connection used to obtain the record.
    * @param email the email associated with the record.
    * @return the result set containing the record with the specified email.
    * @throws SQLException thrown if the record could not be returned.
    */
   private ResultSet getUserRecord(Connection conn, String email) throws SQLException {
     String query = "SELECT * FROM HackyUser where Email='" + email + "'";
     PreparedStatement statement = conn.prepareStatement(query,
         ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
     return statement.executeQuery();
   }
 
   /**
    * The helper method used to return a SensorDataType ResultSet which has the
    * specified sensor data type name..
    * @param conn the connection used to obtain the record.
    * @param sdtName the name of sensor data type to find.
    * @return the result set containing the record with the specified sdt.
    * @throws SQLException thrown if the record could not be returned.
    */
   private ResultSet getSdtRecord(Connection conn, String sdtName) throws SQLException {
     String query = "SELECT * FROM SensorDataType where Name='" + sdtName + "'";
     PreparedStatement statement = conn.prepareStatement(query,
         ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
     return statement.executeQuery();
   }
 
   /**
    * The helper method used to return a Project ResultSet with the specified
    * project name.
    * @param conn the connection used to obtain the record.
    * @param projectName the name of project.
    * @return the result set containing the record with the specified project
    * name.
    * @throws SQLException thrown if the record could not be returned.
    */
   private ResultSet getProjectRecord(Connection conn, String projectName) throws SQLException {
     String query = "SELECT * FROM Project where ProjectName='" + projectName + "'";
     PreparedStatement statement = conn.prepareStatement(query,
         ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
     return statement.executeQuery();
   }
 
   /**
    * The helper method used to return a ProjectUri ResultSet with the related to
    * the project with the specified project name.
    * @param conn the connection used to obtain the record.
    * @param projectName the name of project.
    * @return the result set containing the record with the specified project
    * uri.
    * @throws SQLException thrown if the record could not be returned.
    */
   private ResultSet getProjectUriRecords(Connection conn, String projectName)
       throws SQLException {
     String query = "SELECT * FROM ProjectUri where Project_Id IN "
         + "(SELECT Id FROM Project WHERE ProjectName='" + projectName + "')";
     PreparedStatement statement = conn.prepareStatement(query,
         ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
     return statement.executeQuery();
   }
 
   /**
    * The helper method used to return a SensorData ResultSet which has the
    * specified owner and timestamp.
    * @param conn the connection used to obtain the record.
    * @param ownerId the record id of the user owning the returned SensorData.
    * @param timestamp the timestamp of the sensor data.
    * @return the result set containing the sensor data record.
    * @throws SQLException thrown if the record could not be returned.
    */
   private ResultSet getSensorDataRecord(Connection conn, Object ownerId,
       XMLGregorianCalendar timestamp) throws SQLException {
     String query = "SELECT * FROM SensorData where" + ownerIdEquals + ownerId
         + "' AND Tstamp='" + timestamp + "'";
     PreparedStatement statement = conn.prepareStatement(query,
         ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
     return statement.executeQuery();
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
     return getIndex("SensorData", st); // NOPMD (See below)
   }
 
   /*
    * Interestingly, I could not refactor out the string "SensorData" to avoid
    * the PMD error resulting from multiple occurrences of the same string. This
    * is because if I made it a private String, then Findbugs would throw a
    * warning asking for it to be static:
    * 
    * private static final String sensorData = "SensorData";
    * 
    * However, the above declaration causes the system to deadlock! So, I'm just
    * ignoring the PMD error.
    */
 
   /** {@inheritDoc} */
   @Override
   public String getSensorDataIndex(User user) {
     ResultSet ownerResultSet = null;
     try {
       ownerResultSet = this.getUserRecord(this.connection, user.getEmail());
       if (ownerResultSet.next()) {
         String st = "SELECT XmlSensorDataRef FROM SensorData WHERE" + ownerIdEquals
             + ownerResultSet.getObject("Id") + "'";
         return getIndex("SensorData", st);
       }
     }
     catch (SQLException e) {
       this.logger.info(postgresError + StackTrace.toString(e));
     }
     finally {
       try {
         ownerResultSet.close();
       }
       catch (SQLException e) {
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
     return "";
   }
 
   /** {@inheritDoc} */
   @Override
   public String getSensorDataIndex(User user, String sdtName) {
     ResultSet ownerResults = null;
     ResultSet sdtResults = null;
 
     try {
       ownerResults = this.getUserRecord(this.connection, user.getEmail());
       sdtResults = this.getSdtRecord(this.connection, sdtName);
       if (ownerResults.next() && sdtResults.next()) {
         String st = selectPrefix + ownerIdEquals + ownerResults.getObject("Id")
             + quoteAndClause + sdtIdEquals + sdtResults.getObject("Id") + "'" + orderByTstamp;
         return getIndex("SensorData", st);
       }
     }
     catch (SQLException e) {
       this.logger.info(postgresError + StackTrace.toString(e));
     }
     finally {
       try {
         ownerResults.close();
         sdtResults.close();
       }
       catch (SQLException e) {
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
     return "";
   }
 
   /** {@inheritDoc} */
   @Override
   public String getSensorDataIndex(List<User> users, XMLGregorianCalendar startTime,
       XMLGregorianCalendar endTime, List<String> uriPatterns, String sdt) {
     String statement = null;
     ResultSet results = null;
 
     try {
       if (sdt == null) { // Retrieve sensor data of all SDTs
         statement = selectPrefix + constructOwnerClause(users) + andClause + " ("
             + tstampBetweenTstamp + Tstamp.makeTimestamp(startTime) + quoteAndClause
             + timeStampClause + Tstamp.makeTimestamp(endTime) + "')"
             + constructLikeClauses(uriPatterns) + orderByTstamp;
       }
       else { // Retrieve sensor data of the specified SDT.
         results = this.getSdtRecord(this.connection, sdt);
         if (results.next()) {
           statement = selectPrefix + constructOwnerClause(users) + andClause + sdtIdEquals
               + results.getObject("Id") + quoteAndClause + " (" + tstampBetweenTstamp
               + Tstamp.makeTimestamp(startTime) + quoteAndClause + timeStampClause
               + Tstamp.makeTimestamp(endTime) + "')" + constructLikeClauses(uriPatterns)
               + orderByTstamp;
         }
         else {
           return "";
         }
       }
     }
     catch (SQLException e) {
       this.logger.info(postgresError + StackTrace.toString(e));
       return "";
     }
     finally {
       try {
         if (results != null) {
           results.close();
         }
       }
       catch (SQLException e) {
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
     return getIndex("SensorData", statement);
   }
 
   /** {@inheritDoc} */
   @Override
   public String getProjectSensorDataSnapshot(List<User> users, XMLGregorianCalendar startTime,
       XMLGregorianCalendar endTime, List<String> uriPatterns, String sdt, String tool) {
     String statement = null;
     ResultSet results = null;
     try {
       results = this.getSdtRecord(this.connection, sdt);
       if (results.next()) {
         Object sdtId = results.getObject("Id");
         if (tool == null) { // Retrieve sensor data with latest runtime
           // regardless
           // of tool.
           statement = selectSnapshot + constructOwnerClause(users) + andClause + sdtIdEquals
               + sdtId + quoteAndClause + " (" + tstampBetweenTstamp
               + Tstamp.makeTimestamp(startTime) + quoteAndClause + timeStampClause
               + Tstamp.makeTimestamp(endTime) + "')" // NOPMD
               + constructLikeClauses(uriPatterns) + orderByRuntime;
         }
         else { // Retrieve sensor data with the latest runtime for the
           // specified
           // tool.
           statement = selectSnapshot + constructOwnerClause(users) + andClause + sdtIdEquals
               + sdtId + quoteAndClause + toolEquals + tool + quoteAndClause + " ("
               + tstampBetweenTstamp + Tstamp.makeTimestamp(startTime) + quoteAndClause
               + timeStampClause + Tstamp.makeTimestamp(endTime) + "')"
               + constructLikeClauses(uriPatterns) + orderByRuntime;
         }
       }
       else {
         return "";
       }
     }
     catch (SQLException e) {
       this.logger.info(postgresError + StackTrace.toString(e));
       return "";
     }
     finally {
       try {
         results.close();
       }
       catch (SQLException e) {
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
     // Generate a SensorDataIndex string that contains only entries with the
     // latest runtime.
     return getSnapshotIndex(statement);
   }
 
   /** {@inheritDoc} */
   @Override
   public String getSensorDataIndex(List<User> users, XMLGregorianCalendar startTime,
       XMLGregorianCalendar endTime, List<String> uriPatterns, int startIndex, int maxInstances) {
     String statement = selectPrefix + constructOwnerClause(users) + andClause + " ("
         + tstampBetweenTstamp + Tstamp.makeTimestamp(startTime) + quoteAndClause
         + timeStampClause + Tstamp.makeTimestamp(endTime) + "')"
         + constructLikeClauses(uriPatterns) + orderByTstamp;
     return getIndex("SensorData", statement, startIndex, maxInstances);
   }
 
   /**
    * Constructs a set of LIKE clauses corresponding to the passed set of
    * UriPatterns.
    * <p>
    * Each UriPattern is translated in the following way:
    * <ul>
    * <li> If there is an occurrence of a "\" or a "/" in the UriPattern, then
    * two translated UriPatterns are generated, one with all "\" replaced with
    * "/", and one with all "/" replaced with "\".
    * <li> The escape character is "\", unless we are generating a LIKE clause
    * containing a "\", in which case the escape character will be "/".
    * <li> All occurrences of "%" in the UriPattern are escaped.
    * <li> All occurrences of "_" in the UriPattern are escaped.
    * <li> All occurrences of "*" are changed to "%".
    * </ul>
    * The new set of 'translated' UriPatterns are now used to generate a set of
    * LIKE clauses with the following form:
    * 
    * <pre>
    * (RESOURCE like 'translatedUriPattern1' escape 'escapeChar1') OR
    * (RESOURCE like 'translatedUriPattern2' escape 'escapeChar2') ..
    * </pre>
    * 
    * <p>
    * There is one special case. If the List<UriPattern> is null, empty, or
    * consists of exactly one UriPattern which is "**" or "*", then the empty
    * string is returned. This is an optimization for the common case where all
    * resources should be matched and so we don't need any LIKE clauses.
    * <p>
    * We return either the empty string (""), or else a string of the form: " AND
    * ([like clause] AND [like clause] ... )" This enables the return value to be
    * appended to the SELECT statement.
    * <p>
    * This method is static and package private to support testing. See the class
    * TestConstructUriPattern for example invocations and expected return values.
    * 
    * @param uriPatterns The list of uriPatterns.
    * @return The String to be used in the where clause to check for resource
    * correctness.
    */
   static String constructLikeClauses(List<String> uriPatterns) {
     // Deal with special case. UriPatterns is null, or empty, or "**", or "*"
     if (((uriPatterns == null) || uriPatterns.isEmpty())
         || ((uriPatterns.size() == 1) && uriPatterns.get(0).equals("**"))
         || ((uriPatterns.size() == 1) && uriPatterns.get(0).equals("*"))) {
       return "";
     }
     // Deal with the potential presence of path separator character in
     // UriPattern.
     List<String> translatedPatterns = new ArrayList<String>();
     for (String pattern : uriPatterns) {
       if (pattern.contains("\\") || pattern.contains("/")) {
         translatedPatterns.add(pattern.replace('\\', '/'));
 
         // Postgres allows POSIX pattern matching so '\' must be escaped.
         translatedPatterns.add(pattern.replace("\\", "\\" + "\\"));
         translatedPatterns.add(pattern.replace("/", "\\" + "\\"));
       }
       else {
         translatedPatterns.add(pattern);
       }
     }
     // Now escape the SQL wildcards, and make our UriPattern wildcard into the
     // SQL wildcard.
     for (int i = 0; i < translatedPatterns.size(); i++) {
       String pattern = translatedPatterns.get(i);
       pattern = pattern.replace("%", "`%"); // used to be /
       pattern = pattern.replace("_", "`_"); // used to be /
       pattern = pattern.replace('*', '%');
       translatedPatterns.set(i, pattern);
     }
 
     // Now generate the return string: " AND (<like clause> OR <like clause> ...
     // )".
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
     StringBuffer buff = new StringBuffer(); // NOPMD generates false warning
     // about buff size.
     if (patterns.isEmpty()) {
       return buff;
     }
     for (Iterator<String> i = patterns.iterator(); i.hasNext();) {
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
     ResultSet results = null;
     try {
       for (Iterator<User> i = users.iterator(); i.hasNext();) {
         User user = i.next();
         results = this.getUserRecord(this.connection, user.getEmail());
         if (results.next()) {
           buff.append(ownerIdEquals);
           buff.append(results.getObject("Id"));
           buff.append('\'');
           if (i.hasNext()) {
             buff.append(" OR");
           }
         }
       }
       buff.append(") ");
       return buff.toString();
     }
     catch (SQLException e) {
       this.logger.warning(errorClosingMsg + StackTrace.toString(e));
     }
     finally {
       try {
         results.close();
       }
       catch (SQLException e) {
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
     return "";
   }
 
   /** {@inheritDoc} */
   @Override
   public String getSensorDataIndexLastMod(User user, XMLGregorianCalendar lastModStartTime,
       XMLGregorianCalendar lastModEndTime) {
     Connection conn = null;
     ResultSet ownerResults = null;
     try {
       conn = DriverManager.getConnection(connectionURL);
       ownerResults = this.getUserRecord(conn, user.getEmail());
       if (ownerResults.next()) {
         String statement = selectPrefix + ownerIdEquals + ownerResults.getObject("Id")
             + quoteAndClause + " LastMod BETWEEN TIMESTAMP '"
             + Tstamp.makeTimestamp(lastModStartTime) + "' AND " + timeStampClause
             + Tstamp.makeTimestamp(lastModEndTime) + "'";
         return getIndex("SensorData", statement);
       }
     }
     catch (SQLException e) {
       this.logger.info(postgresError + StackTrace.toString(e));
     }
     finally {
       try {
         conn.close();
         ownerResults.close();
       }
       catch (SQLException e) {
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
     return "";
   }
 
   /** {@inheritDoc} */
   @Override
   public boolean hasSensorData(User user, XMLGregorianCalendar timestamp) {
     PreparedStatement s = null;
     ResultSet rs = null;
     ResultSet ownerResults = null;
     boolean isFound = false;
     try {
       ownerResults = this.getUserRecord(this.connection, user.getEmail());
       if (ownerResults.next()) {
         String statement = selectPrefix + ownerIdEquals + ownerResults.getObject("Id")
             + quoteAndClause + " Tstamp='" + Tstamp.makeTimestamp(timestamp) + "'";
         server.getLogger().fine(executeQueryMsg + statement);
         s = this.connection.prepareStatement(statement);
         rs = s.executeQuery();
         // If a record was retrieved, we'll enter the loop, otherwise we won't.
         while (rs.next()) {
           isFound = true;
         }
       }
     }
     catch (SQLException e) {
       this.logger.info("Postgres: Error in hasSensorData()" + StackTrace.toString(e));
     }
     finally {
       try {
         s.close();
         ownerResults.close();
         if (rs != null) {
           rs.close();
         }
       }
       catch (SQLException e) {
         this.logger.warning("Postgres: Error closing the connection" + StackTrace.toString(e));
       }
     }
     return isFound;
   }
 
   /** {@inheritDoc} */
   @Override
   public void deleteSensorData(User user, XMLGregorianCalendar timestamp) {
     ResultSet ownerResults = null;
 
     try {
       ownerResults = this.getUserRecord(this.connection, user.getEmail());
       if (ownerResults.next()) {
         String statement = "DELETE FROM SensorData WHERE " + ownerIdEquals
             + ownerResults.getObject("Id") + quoteAndClause + " Tstamp='"
             + Tstamp.makeTimestamp(timestamp) + "'";
         deleteResource(statement);
       }
     }
     catch (SQLException e) {
       this.logger.info(postgresError + StackTrace.toString(e));
     }
     finally {
       try {
         ownerResults.close();
       }
       catch (SQLException e) {
         this.logger.warning("Postgres: Error closing the connection" + StackTrace.toString(e));
       }
     }
   }
 
   /** {@inheritDoc} */
   @Override
   public void deleteSensorData(User user) {
 // no op for now. 
 //    if (true) {
 //      this.logger.fine("Postgres: Not Deleted" + user.getEmail());
 //      return;       
 //    }
 
     ResultSet ownerResults = null;
     try {
       ownerResults = this.getUserRecord(this.connection, user.getEmail());
       if (ownerResults.next()) {
         String statement = "DELETE FROM SensorData WHERE" + ownerIdEquals
             + ownerResults.getObject("Id") + "'";
         deleteResource(statement);
       }
     }
     catch (SQLException e) {
       this.logger.info(postgresError + StackTrace.toString(e));
     }
     finally {
       try {
         ownerResults.close();
       }
       catch (SQLException e) {
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
   }
 
   /** {@inheritDoc} */
   @Override
   public String getSensorData(User user, XMLGregorianCalendar timestamp) {
     ResultSet ownerResults = null;
     try {
       ownerResults = this.getUserRecord(this.connection, user.getEmail());
       if (ownerResults.next()) {
         String statement = "SELECT XmlSensorData FROM SensorData WHERE" + ownerIdEquals
             + ownerResults.getObject("Id") + quoteAndClause + " Tstamp='"
             + Tstamp.makeTimestamp(timestamp) + "'";
         return getResource("SensorData", statement);
       }
     }
     catch (SQLException e) {
       this.logger.info(postgresError + StackTrace.toString(e));
     }
     finally {
       try {
         ownerResults.close();
       }
       catch (SQLException e) {
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
     return "";
   }
 
   /** {@inheritDoc} */
   @Override
   public boolean storeSensorDataType(SensorDataType sdt, String xmlSensorDataType,
       String xmlSensorDataTypeRef) {
     PreparedStatement countStatement = null;
     PreparedStatement dataStatement = null;
     ResultSet countResultSet = null;
     try {
       String countQuery = "SELECT * FROM SensorDataType where name='" + sdt.getName() + "'";
       countStatement = this.connection.prepareStatement(countQuery,
           ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
       countResultSet = countStatement.executeQuery();
       // If an SDT with the same name exists, let's update it.
       if (countResultSet.next()) {
         dataStatement = this.connection.prepareStatement("UPDATE SensorDataType SET "
             + " LastMod=?," + "XmlSensorDataType=?, " + " XmlSensorDataTypeRef=?"
             + " WHERE Name=?");
         dataStatement.setTimestamp(1, new Timestamp(new Date().getTime()));
         dataStatement.setString(2, xmlSensorDataType);
         dataStatement.setString(3, xmlSensorDataTypeRef);
         dataStatement.setString(4, sdt.getName());
         dataStatement.executeUpdate();
         this.logger.fine("Postgres: Updated SDT " + sdt.getName());
       }
       // Insert the new SDT.
       else {
         dataStatement = this.connection
             .prepareStatement("INSERT INTO SensorDataType VALUES (?, ?, ?, ?, ?)");
         // Order: id name lastmod xmlsensordatatype xmlsensordatatyperef
         dataStatement.setObject(1, UUID.randomUUID(), Types.OTHER);
         dataStatement.setString(2, sdt.getName());
         dataStatement.setTimestamp(3, new Timestamp(new Date().getTime()));
         dataStatement.setString(4, xmlSensorDataType);
         dataStatement.setString(5, xmlSensorDataTypeRef);
         dataStatement.executeUpdate();
         this.logger.fine("Postgres: Inserted SDT" + sdt.getName());
       }
     }
     catch (SQLException e) {
       this.logger.info(postgresError + StackTrace.toString(e));
     }
     finally {
       try {
         countStatement.close();
         dataStatement.close();
         countResultSet.close();
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
     String statement = "SELECT XmlSensorDataType FROM SensorDataType WHERE Name = '" + sdtName
         + "'";
     return getResource("SensorDataType", statement);
   }
 
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
     PreparedStatement countStatement = null;
     PreparedStatement userStatement = null;
     ResultSet countResultSet = null;
     try {
       String countQuery = "SELECT * FROM HackyUser where email='" + user.getEmail() + "'";
       countStatement = this.connection.prepareStatement(countQuery,
           ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
       countResultSet = countStatement.executeQuery();
 
       // If the user with the same email exists, perform an update.
       if (countResultSet.next()) {
         userStatement = this.connection.prepareStatement("UPDATE HackyUser SET "
             + " Password=?, " + " Role=?, " + " LastMod=?," + " XmlUser=?, "
             + " XmlUserRef=? " + " WHERE Email=?");
         userStatement.setString(1, user.getPassword());
         userStatement.setString(2, user.getRole());
         userStatement.setTimestamp(3, new Timestamp(new Date().getTime()));
         userStatement.setString(4, xmlUser);
         userStatement.setString(5, xmlUserRef);
         userStatement.setString(6, user.getEmail());
         userStatement.executeUpdate();
         this.logger.fine("Postgres: Updated User " + user.getEmail());
       }
       // Insert the new user into the database.
       else {
         userStatement = this.connection
             .prepareStatement("INSERT INTO HackyUser VALUES (?, ?, ?, ?, ?, ?, ?)");
         // Order: id email password role lastmod xmluser xmluserref
         userStatement.setObject(1, UUID.randomUUID(), Types.OTHER);
         userStatement.setString(2, user.getEmail());
         userStatement.setString(3, user.getPassword());
         userStatement.setString(4, user.getRole());
         userStatement.setTimestamp(5, new Timestamp(new Date().getTime()));
         userStatement.setString(6, xmlUser);
         userStatement.setString(7, xmlUserRef);
         userStatement.executeUpdate();
         this.logger.fine("Postgres: Inserted User" + user.getEmail());
       }
     }
     catch (SQLException e) {
       this.logger.info(postgresError + StackTrace.toString(e));
     }
     finally {
       try {
         userStatement.close();
         countStatement.close();
         countResultSet.close();
       }
       catch (SQLException e) {
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
     return true;
   }
 
   /** {@inheritDoc} */
   @Override
   public void deleteProject(User owner, String projectName) {
     ResultSet projectUriResults = null;
     ResultSet userResults = null;
     try {
       // Removes the ProjectUri records associated with the project.
       projectUriResults = this.getProjectUriRecords(this.connection, projectName);
       if (projectUriResults.next()) {
         String statement = "DELETE FROM ProjectUri WHERE Id='"
             + projectUriResults.getObject("Id") + "'";
         deleteResource(statement);
       }
 
       // Removes the Project.
       userResults = this.getUserRecord(this.connection, owner.getEmail());
       if (userResults.next()) {
         String statement = "DELETE FROM Project WHERE " + ownerIdEquals
             + userResults.getObject("Id") + quoteAndClause + " ProjectName = '" + projectName
             + "'";
         deleteResource(statement);
       }
     }
     catch (SQLException e) {
       this.logger.info(postgresError + StackTrace.toString(e));
     }
     finally {
       try {
         projectUriResults.close();
         userResults.close();
       }
       catch (SQLException e) {
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
   }
 
   /** {@inheritDoc} */
   @Override
   public String getProject(User owner, String projectName) {
     ResultSet userResults = null;
     try {
       userResults = this.getUserRecord(this.connection, owner.getEmail());
       if (userResults.next()) {
         Object userId = userResults.getObject("Id");
         String statement = "SELECT XmlProject FROM Project WHERE" + ownerIdEquals + userId
             + quoteAndClause + " ProjectName ='" + projectName + "'";
         return getResource("Project", statement);
       }
     }
     catch (SQLException e) {
       this.logger.info(postgresError + StackTrace.toString(e));
     }
     finally {
       try {
         userResults.close();
       }
       catch (SQLException e) {
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
     return "";
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
     PreparedStatement dataStatement = null;
     ResultSet dataResultSet = null;
     ResultSet sdtResultSet = null;
     PreparedStatement sdtStatement = null;
 
     // Create the [SDT, Tool] -> NumInstances data structure.
     Map<String, Map<String, Integer>> sdtInstances = new HashMap<String, Map<String, Integer>>();
     try {
       // Make a statement to return all SensorData for this project in the time
       // period.
       String statement = "SELECT Sdt_Id, Tool FROM SensorData WHERE "
           + constructOwnerClause(users) + andClause + " (" + tstampBetweenTstamp
           + Tstamp.makeTimestamp(startTime) + quoteAndClause + timeStampClause
           + Tstamp.makeTimestamp(endTime) + "')" + constructLikeClauses(uriPatterns);
 
       // Retrieve the sensordata for this project and time period.
       dataStatement = this.connection.prepareStatement(statement);
       dataResultSet = dataStatement.executeQuery();
       // Loop through all retrieved SensorData records.
       while (dataResultSet.next()) {
         String sdt = "";
         String sdtId = dataResultSet.getString("Sdt_Id");
         String tool = dataResultSet.getString("Tool");
 
         String sdtQuery = "SELECT * FROM SensorDataType WHERE ID='" + sdtId + "';";
         sdtStatement = this.connection.prepareStatement(sdtQuery,
             ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
         sdtResultSet = sdtStatement.executeQuery();
         if (sdtResultSet.next()) {
           // Don't want null SDTs or Tools, call them the empty string instead.
           sdt = sdtResultSet.getString("name");
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
         // Second, initialize the data structure if this is a new tool for a
         // preexisting SDT.
         if (tool2NumInstances.get(tool) == null) {
           tool2NumInstances.put(tool, 0);
         }
         // Finally, increment this entry.
         tool2NumInstances.put(tool, tool2NumInstances.get(tool) + 1);
       }
     }
     catch (SQLException e) {
       this.logger.info("Postgres: Error in getProjectSummary()" + StackTrace.toString(e));
     }
     finally {
       try {
         dataResultSet.close();
         dataStatement.close();
         if (sdtStatement != null) {
           sdtStatement.close();
         }
         if (sdtResultSet != null) {
           sdtResultSet.close();
         }
       }
       catch (SQLException e) {
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
 
     // Now create the project summary object from our data structures.
     return makeProjectSummary(href, startTime, endTime, sdtInstances);
   }
 
   /**
    * Creates a ProjectSummary instances from the passed data.
    * @param href The Href representing this resource.
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
     PreparedStatement countStatement = null;
     PreparedStatement projectStatement = null;
     PreparedStatement projectUriStatement = null;
     ResultSet countResultSet = null;
     ResultSet userResultSet = null;
     ResultSet projectUriResultSet = null;
     ResultSet projectResultSet = null;
     try {
       // Get the amount of projects with the specified name.
       String countQuery = "SELECT * FROM Project where ProjectName='" + project.getName()
           + "'";
       countStatement = this.connection.prepareStatement(countQuery,
           ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
       countResultSet = countStatement.executeQuery();
 
       userResultSet = this.getUserRecord(this.connection, project.getOwner());
       if (userResultSet.next()) {
         // Get the user associated with the inserted/updated project.
         Object ownerId = userResultSet.getObject("Id");
 
         // If a project with the same name exists, let's update the record.
         if (countResultSet.next()) {
           // First, delete the ProjectUri Records linked to the updated Project.
           projectUriResultSet = this.getProjectUriRecords(this.connection, project.getName());
           while (projectUriResultSet.next()) {
             String statement = "DELETE FROM ProjectUri WHERE Id='"
                 + projectUriResultSet.getObject("Id") + "'";
             deleteResource(statement);
           }
 
           // Then add the new uri record. A remove and add action is done
           // because there is no way to figure out which project uri to update.
           projectResultSet = this.getProjectRecord(this.connection, project.getName());
           projectResultSet.next();
           for (String pattern : project.getUriPatterns().getUriPattern()) {
             projectUriStatement = this.connection
                 .prepareStatement("INSERT INTO ProjectUri VALUES (?, ?, ?)");
             // Order: Id Project_Id Uri
             projectUriStatement.setObject(1, UUID.randomUUID(), Types.OTHER);
             projectUriStatement.setObject(2, projectResultSet.getObject("Id"), Types.OTHER);
             projectUriStatement.setString(3, pattern);
             projectUriStatement.executeUpdate();
           }
 
           projectStatement = this.connection.prepareStatement("UPDATE Project SET "
               + " StartTime=?, EndTime=?, LastMod=?, XmlProject=?, XmlProjectRef=?"
               + " WHERE Owner_Id=?" + andClause + "ProjectName=?");
           projectStatement.setTimestamp(1, Tstamp.makeTimestamp(project.getStartTime()));
           projectStatement.setTimestamp(2, Tstamp.makeTimestamp(project.getEndTime()));
           projectStatement.setTimestamp(3, Tstamp.makeTimestamp(project.getEndTime()));
           projectStatement.setString(4, xmlProject);
           projectStatement.setString(5, xmlProjectRef);
           projectStatement.setObject(6, ownerId, Types.OTHER);
           projectStatement.setString(7, project.getName());
           projectStatement.executeUpdate();
           this.logger.fine("Postres: Updated " + project.getOwner() + " " + project.getName());
         }
         // Let's create a new project record.
         else {
           projectStatement = this.connection
               .prepareStatement("INSERT INTO Project VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
           // Order: Id ProjectName Owner_Id StartTime EndTime LastMod XmlProject
           // XmlProjectRef
           UUID projectId = UUID.randomUUID();
           projectStatement.setObject(1, projectId, Types.OTHER);
           projectStatement.setString(2, project.getName());
           projectStatement.setObject(3, ownerId, Types.OTHER);
           projectStatement.setTimestamp(4, Tstamp.makeTimestamp(project.getStartTime()));
           projectStatement.setTimestamp(5, Tstamp.makeTimestamp(project.getEndTime()));
           projectStatement.setTimestamp(6, Tstamp.makeTimestamp(project.getLastMod()));
           projectStatement.setString(7, xmlProject);
           projectStatement.setString(8, xmlProjectRef);
           projectStatement.executeUpdate();
 
           for (String pattern : project.getUriPatterns().getUriPattern()) {
             projectUriStatement = this.connection
                 .prepareStatement("INSERT INTO ProjectUri VALUES (?, ?, ?)");
             // Order: Id Project_Id Uri
             projectUriStatement.setObject(1, UUID.randomUUID(), Types.OTHER);
             projectUriStatement.setObject(2, projectId, Types.OTHER);
             projectUriStatement.setString(3, pattern);
             projectUriStatement.executeUpdate();
           }
           this.logger.fine("Postgres: Inserted " + project.getOwner() + " "
               + project.getName());
         }
       }
     }
     catch (SQLException e) {
       this.logger.info(postgresError + StackTrace.toString(e));
     }
     finally {
       try {
         if (projectStatement != null) {
           projectStatement.close();
         }
         if (projectUriStatement != null) {
           projectUriStatement.close();
         }
         if (projectUriResultSet != null) {
           projectUriResultSet.close();
         }
         if (projectResultSet != null) {
           projectResultSet.close();
         }
         countStatement.close();
         countResultSet.close();
         userResultSet.close();
       }
       catch (SQLException e) {
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
     return true;
   }
 
   /**
    * Returns a string containing the Index for the given resource indicated by
    * resourceName.
    * @param resourceName The resource name, such as "Project".
    * @param statement The SQL Statement to be used to retrieve the resource
    * references.
    * @return The aggregate Index XML string.
    */
   private String getIndex(String resourceName, String statement) {
     StringBuilder builder = new StringBuilder(512);
     builder.append("<").append(resourceName).append(indexSuffix);
     // Retrieve all the SensorData
     PreparedStatement s = null;
     ResultSet rs = null;
     try {
       s = this.connection.prepareStatement(statement);
       rs = s.executeQuery();
       String resourceRefColumnName = xml + resourceName + "Ref";
       while (rs.next()) {
         builder.append(rs.getString(resourceRefColumnName));
       }
     }
     catch (SQLException e) {
       this.logger.info("Postgres: Error in getIndex()" + StackTrace.toString(e));
     }
     finally {
       try {
         rs.close();
         s.close();
       }
       catch (SQLException e) {
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
     builder.append("</").append(resourceName).append(indexSuffix);
     return builder.toString();
   }
 
   /**
    * Returns a string containing the Index of all of the SensorData whose
    * runtime field matches the first runtime in the result set. Since the passed
    * statement will retrieve sensor data in the given time period ordered in
    * descending order by runtime, this should result in an index containing only
    * @param statement The SQL Statement to be used to retrieve the resource
    * references.
    * @return The aggregate Index XML string.
    */
   private String getSnapshotIndex(String statement) {
     String resourceName = "SensorData";
     StringBuilder builder = new StringBuilder(512);
     builder.append("<").append(resourceName).append(indexSuffix);
     // Retrieve all the SensorData
     PreparedStatement s = null;
     ResultSet rs = null;
     String firstRunTime = null;
     try {
       this.connection = DriverManager.getConnection(connectionURL);
       s = this.connection.prepareStatement(statement);
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
           // Now add every entry whose runtime equals the first retrieved run
           // time.
           if (runtime.equals(firstRunTime)) {
             builder.append(rs.getString(resourceRefColumnName));
           }
           else {
             // As soon as we find a runtime not equal to firstRunTime, we can
             // stop.
             finished = true;
           }
         }
       }
     }
     catch (SQLException e) {
       this.logger.info("Postgres: Error in getIndex()" + StackTrace.toString(e));
     }
     finally {
       try {
         rs.close();
         s.close();
       }
       catch (SQLException e) {
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
     builder.append("</").append(resourceName).append(indexSuffix);
     return builder.toString();
   }
 
   /**
    * Returns a string containing the Index for the given resource indicated by
    * resourceName, returning only the instances starting at startIndex, and with
    * the maximum number of returned instances indicated by maxInstances.
    * @param resourceName The resource name, such as "Project".
    * @param startIndex The (zero-based) starting index for instances to be
    * returned.
    * @param maxInstances The maximum number of instances to return.
    * @param statement The SQL Statement to be used to retrieve the resource
    * references.
    * @return The aggregate Index XML string.
    */
   private String getIndex(String resourceName, String statement, int startIndex,
       int maxInstances) {
     StringBuilder builder = new StringBuilder(512);
     builder.append("<").append(resourceName).append(indexSuffix);
     // Retrieve all the SensorData to start.
     PreparedStatement s = null;
     ResultSet rs = null;
     try {
       s = this.connection.prepareStatement(statement);
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
       this.logger.info("Postgres: Error in getIndex()" + StackTrace.toString(e));
     }
     finally {
       try {
         rs.close();
         s.close();
       }
       catch (SQLException e) {
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
     builder.append("</").append(resourceName).append(indexSuffix);
     return builder.toString();
   }
 
   /**
    * Returns a string containing the Resource as XML.
    * @param resourceName The name of the resource, such as "User".
    * @param statement The select statement used to retrieve the resultset
    * containing a single row with that resource.
    * @return The string containing the resource as an XML string.
    */
   private String getResource(String resourceName, String statement) {
     StringBuilder builder = new StringBuilder(512);
     PreparedStatement s = null;
     ResultSet rs = null;
     try {
       server.getLogger().fine(executeQueryMsg + statement);
       s = this.connection.prepareStatement(statement);
       rs = s.executeQuery();
       String resourceXmlColumnName = xml + resourceName;
       while (rs.next()) { // the select statement must guarantee only one row is
         // returned.
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
     PreparedStatement s = null;
     try {
       server.getLogger().fine("Postgres: " + statement);
       s = this.connection.prepareStatement(statement);
       s.executeUpdate();
     }
     catch (SQLException e) {
       this.logger.info("Postgres: Error in deleteResource()" + StackTrace.toString(e));
     }
     finally {
       try {
         s.close();
       }
       catch (SQLException e) {
         e.printStackTrace();
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
   }
 
   /**
    * Always returns true because compression is not supported in this Postgres
    * implementation.
    * @return returns true.
    */
   @Override
   public boolean compressTables() {
     return true;
   }
 
   /** 
    * {@inheritDoc}. This is an estimate, it turns out that postgreSQL has some problems
    * counting its row counts. 
    */
   @Override
   public int getRowCount(String table) {
     int numRows = -1;
     PreparedStatement s = null;
     ResultSet rs = null;
     String statement = "select n_live_tup, relname, last_analyze from pg_stat_user_tables " 
       + " where relname = '" + table.toLowerCase() + "'";
     try {
       s = this.connection.prepareStatement(statement);
       rs = s.executeQuery();
       rs.next();
       numRows = rs.getInt(1);
     }
     catch (SQLException e) {
       this.logger.info("Postgres: Error in getRowCount: " + StackTrace.toString(e));
     }
     finally {
       try {
         rs.close();
         s.close();
       }
       catch (SQLException e) {
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
     return numRows;
   }
 
   /** {@inheritDoc} */
   @Override
   public Set<String> getTableNames() {
     Set<String> tableNames = new HashSet<String>();
     tableNames.add("SensorData");
     tableNames.add("SensorDataType");
     tableNames.add("HackyUser");
     tableNames.add("Project");
     return tableNames;
   }
 
   /** {@inheritDoc} */
   @Override
   public String getSensorDataIndex(List<User> users, XMLGregorianCalendar startTime,
       XMLGregorianCalendar endTime, List<String> uriPatterns, String sdt, String tool) {
     String statement = null;
     ResultSet results = null;
 
     try {
       results = this.getSdtRecord(this.connection, sdt);
       if (results.next()) {
         statement = selectPrefix + constructOwnerClause(users) + andClause + sdtIdEquals
             + results.getObject("Id") + quoteAndClause + toolEquals + tool + quoteAndClause
             + " (Tstamp BETWEEN TIMESTAMP '" + Tstamp.makeTimestamp(startTime) + "' AND " // NOPMD
             + " TIMESTAMP '" + Tstamp.makeTimestamp(endTime) + "')" // NOPMD
             + constructLikeClauses(uriPatterns) + orderByTstamp;
         return getIndex("SensorData", statement);
       }
     }
     catch (SQLException e) {
       this.logger.info(postgresError + StackTrace.toString(e));
       return "";
     }
     finally {
       try {
         results.close();
       }
       catch (SQLException e) {
         this.logger.warning(errorClosingMsg + StackTrace.toString(e));
       }
     }
     return "";
   }
 
   /**
    * Always returns true because indexing is not supported in this Postgres
    * implementation.
    * @return returns true.
    */
   @Override
   public boolean indexTables() {
     return true;
   }
 }
