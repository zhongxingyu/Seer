 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.tools;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import javax.sql.DataSource;
 import java.sql.Connection;
 import java.sql.Statement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.Validate;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
 
 import com.globant.katari.tools.database.MySqlDropAllObjects;
 import com.globant.katari.tools.database.PostgreSqlDropAllObjects;
 import com.globant.katari.tools.database.OracleDropAllObjects;
 
 /** DatabaseTestSupport abstract class for classes that can drop objects and
  * run sql sentences.
  *
  * @author nicolas.frontini
  */
 public abstract class DatabaseTestSupport {
 
   /** A logger.
    */
   private static Logger log =
     LoggerFactory.getLogger(DatabaseTestSupport.class);
 
   /** The data base drop objects Map.
    */
   private static Map<String, DatabaseTestSupport> dbDropObjectsMap
     = new HashMap<String, DatabaseTestSupport>();
 
   /** Registers a handler for an additional dialect.
    *
    * @param dialect The hibernate dialect. It cannot be null.
    *
    * @param support The DatabaseTestSupport that will handle the dialect. It
    * cannot be null.
    */
   public static void registerDialect(final String dialect, final
       DatabaseTestSupport support) {
     dbDropObjectsMap.put(dialect, support);
   }
 
   static {
     initialize();
   }
 
   /** Registers the classes responsible for effectively dropping the database
    * objects.
    */
   private static void initialize() {
     registerDialect("org.hibernate.dialect.MySQLDialect",
         new MySqlDropAllObjects());
     registerDialect("org.hibernate.dialect.MySQL5InnoDBDialect",
         new MySqlDropAllObjects());
     registerDialect("org.hibernate.dialect.PostgreSQLDialect",
         new PostgreSqlDropAllObjects());
     registerDialect("org.hibernate.dialect.Oracle8iDialect",
         new OracleDropAllObjects());
     registerDialect("org.hibernate.dialect.Oracle9Dialect",
         new OracleDropAllObjects());
     registerDialect("org.hibernate.dialect.Oracle9iDialect",
         new OracleDropAllObjects());
     registerDialect("org.hibernate.dialect.Oracle10gDialect",
         new OracleDropAllObjects());
   }
 
   /** Returns the corresponding DatabaseTestSupport instance.
    *
    * The returned instance depends on the database used.
    *
    * @param session The session factory. It cannot be null.
    *
    * @return The correspinding DatabaseTestSupport.
    */
   public static DatabaseTestSupport create(
       final LocalSessionFactoryBean session) {
     Validate.notNull(session, "The session factory cannot be null.");
     String dialect = (String) session.getConfiguration()
         .getProperties().get("hibernate.dialect");
     DatabaseTestSupport databaseTestSupport = dbDropObjectsMap.get(dialect);
     Validate.notNull(databaseTestSupport, "Dialect " + dialect
         + " not supported.");
     return databaseTestSupport;
   }
 
   /** Drops all tables from the data base.
    *
    * Checks if the marker table exists and contains 'YES DROP ME'. If it does,
    * it drops all objects in the database, excluding the marker table.
    *
    * @param dataSource The data source to obtain the connection from. It cannot
    * be null.
    *
    * @param markerTable The marker table name. This table must exist and have
    * one column named drop_database with 'YES DROP ME'.
    */
   public void dropAll(final DataSource dataSource, final String markerTable) {
     try {
       Connection connection = dataSource.getConnection();
       assertDevelopmentDatabase(connection, markerTable);
       doDropAll(connection, markerTable);
       // We are not that careful closing the connection because we exit in case
       // of error.
       connection.close();
     } catch (Exception e) {
       e.printStackTrace();
       System.exit(1);
     }
   }
 
   /** Template method to drop all tables from the data base.
    *
    * @param connection The database connectino to use to drop all the objects.
    * It cannot be null.
    *
    * @param markerTable The name of the marker table. It cannot be null.
    */
   protected abstract void doDropAll(final Connection connection, final String
       markerTable) throws Exception;
 
   /** Runs a set of sql sentences stored in a file.
    *
    * Sentences in the file are delimited by a line ending in ;.
    *
    * @param fileName the String with the file name.
    */
   public void runSqlSentences(final DataSource dataSource, final String
       fileName) {
     log.trace("Entering runSqlSentences('" + fileName + "')");
     Validate.notNull(fileName, "The file name cannot be null.");
 
     Connection connection = null;
     StringBuffer sentence = new StringBuffer();
     try {
       connection = dataSource.getConnection();
       Statement statement = connection.createStatement();
 
       BufferedReader in = new BufferedReader(new FileReader(fileName));
       String line = null;
       while (null != (line = in.readLine())) {
         if (line.endsWith(";")) {
           sentence.append(line.substring(0, line.length() - 1));
           log.debug("Executing: {}", sentence.toString());
           statement.execute(StringEscapeUtils.unescapeJava(
               sentence.toString()));
           sentence = new StringBuffer();
         } else {
           sentence.append(line);
           sentence.append("\n");
         }
       }
     } catch (Exception e) {
       System.out.println("Error executing " + sentence);
       e.printStackTrace();
       System.exit(1);
     } finally {
       if (connection != null) {
         try {
           connection.close();
         } catch (Exception e) {
           // This should never happen.
           e.printStackTrace();
           System.exit(1);
         }
       }
     }
     log.trace("Leaving runSqlSentences");
   }
 
   /** Verifies if it is a development database, that is, if it has the mark
    * table.
    *
    * It throws an exception if it is not a development database.
    *
    * @param connection The connection to execute sql sentences. It cannot be
    * null.
    *
    * @param markerTable The marker table name. Is the marker of the db of
    * permision.
    *
    * @throws SQLException if a database access error occurs.
    */
   private void assertDevelopmentDatabase(final Connection connection,
       final String markerTable) throws SQLException {
     Validate.notNull(connection, "The connection cannot be null.");
     Statement st = connection.createStatement();
 
     /* Check if we are running against a scratch database.
      */
     log.debug("Verifying if it is a test database");
     ResultSet rs = null;
     try {
       rs = st.executeQuery("select drop_database from " + markerTable);
     } catch (SQLException e) {
       // An exeption. Give some explanation just in case.
       log.info("An exception was caught selecting from {}"
           + ". It is probable because the table does not exist. "
           + "Please create it with:", markerTable);
       log.info("create table {} (drop_database varchar (50));",  markerTable);
      log.info("insert into {} values ('YES, DROP ME');", markerTable );
 
       System.out.println("An exception was caught selecting from "
           + markerTable + ". It is probable because the table does"
           + " not exist. Please create it with:");
       System.out.println("create table " + markerTable
           + "(drop_database varchar (50));");
       System.out.println("insert into " + markerTable
           + " values ('YES, DROP ME');");
       throw e;
     }
     String message = null;
     if (rs.next()) {
       message = rs.getString("drop_database");
     }
     if (message == null) {
       throw new RuntimeException("Marker table does not contain a row");
     }
     if (!message.equals("YES, DROP ME")) {
       throw new RuntimeException("Marker table does not contain the"
           + " correct row");
     }
   }
 }
 
