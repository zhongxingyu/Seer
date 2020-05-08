 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.sql.Time;
 
 import java.util.ArrayList;
 
 public class Derby
 {
     /* the default framework is embedded*/
     private String framework = "embedded";
     private String driver = "org.apache.derby.jdbc.EmbeddedDriver";
     private String protocol = "jdbc:derby:";
     
     Connection conn = null;
     /* This ArrayList usage may cause a warning when compiling this class
      * with a compiler for J2SE 5.0 or newer. We are not using generics
      * because we want the source to support J2SE 1.4.2 environments. */
     ArrayList statements = new ArrayList(); // list of Statements, PreparedStatements    
     Statement s = null;
     ResultSet rs = null;
 
     PreparedStatement entryInsert;
     PreparedStatement taskInsert;
     PreparedStatement eventInsert;
 
     PreparedStatement entryUpdate;
     PreparedStatement taskUpdate;    
     PreparedStatement eventUpdate;
 
     PreparedStatement entryDelete;
     PreparedStatement taskDelete;
     PreparedStatement eventDelete;
 
     PreparedStatement getId;
 
     //see doStatement()
     int answer;
     //entry columns
     String name, desc;
     //task columns
     int status, priority;
     Timestamp timestamp;
     //event columns
     boolean isAllDay; int endYear, endMonth, endDay, endHour, endMinute, repeating;
     Timestamp startTime, endTime;
 
     public void go() {
         System.out.println("Derby starting in " + framework + " mode");
         loadDriver();
 
         try
         {
             String dbName = "derbyDB"; // the name of the database
             conn = DriverManager.getConnection(protocol + dbName + ";create=true");
             System.out.println("Connected to and created database " + dbName);
             conn.setAutoCommit(false);
 
             /* Creating a statement object that we can use for running various
              * SQL statements commands against the database.*/
             s = conn.createStatement(); 
             createTables();      
             System.out.println("Database Initialization Complete");
             prepareThyStatements();
 
             /*========================================================*/
             /** All inserts, updates, deletes, queries are done here **/
             /*========================================================*/
 
                 answer = DatabaseRW.getAnswer();
                 name = DatabaseRW.getName();
                 desc = DatabaseRW.getDesc();
                 timestamp = DatabaseRW.getTimestamp();
                 status = DatabaseRW.getStatus();
                 priority = DatabaseRW.getPriority();
 
                 doStatement(answer);
 
             //Making data persistent in the database
             conn.commit();
             System.out.println("Committed the transaction");
         }
         catch (SQLException sqle){
             printSQLException(sqle);
         } finally {
             // release all open resources to avoid unnecessary memory usage
 
             // ResultSet
             try {
                 if (rs != null) {
                     rs.close();
                     rs = null;
                 }
             } catch (SQLException sqle) {
                 printSQLException(sqle);
             }
 
             // Statements and PreparedStatements
             int i = 0;
             while (!statements.isEmpty()) {
                 // PreparedStatement extend Statement
                 Statement st = (Statement)statements.remove(i);
                 try {
                     if (st != null) {
                         st.close();
                         st = null;
                     }
                 } catch (SQLException sqle) {
                     printSQLException(sqle);
                 }
             }
 
             //Connection
             try {
                 if (conn != null) {
                     conn.close();
                     conn = null;
                 }
             } catch (SQLException sqle) {
                 printSQLException(sqle);
             }
         }
     }
     
     private void createTables() { //If the tables already exist, this code block will NOT execute
         try{
             System.out.println("Creating Tables");
             s.execute("CREATE TABLE entry(E_id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), name varchar(255), description varchar(255), startTime timestamp, CONSTRAINT pk PRIMARY KEY (E_id))");
             System.out.println("Created table entry");
             s.execute("CREATE TABLE event(E_id int NOT NULL PRIMARY KEY, isAllDay boolean, endTime timestamp, repeating smallint)");
             System.out.println("Created table event");
             s.execute("CREATE TABLE task(E_id int NOT NULL PRIMARY KEY, status smallint, priority smallint)");
             System.out.println("Created table task");
         } catch(SQLException sqle){
             //Do nothing
         }
     }
 
     /*=====================================*/
     /** All statements are prepared here **/
     /*===================================*/
     public void prepareThyStatements() {
         try{
             //Insert statements
             entryInsert = conn.prepareStatement("INSERT INTO entry (name, description, startTime) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
             taskInsert = conn.prepareStatement("INSERT INTO task (E_id, status, priority) VALUES (?, ?, ?)");
             eventInsert = conn.prepareStatement("INSERT INTO event (E_id, isAllDay, endTime, repeating) VALUES (?, ?, ?, ?)");
 
             //Update statements
             entryUpdate = conn.prepareStatement("UPDATE entry SET name=?, description=?, startTime=? WHERE E_id=?");
             taskUpdate = conn.prepareStatement("UPDATE task SET status=?, priority=? where E_id=?");
             eventUpdate = conn.prepareStatement("UPDATE event SET isAllDay=?, endTime=?, repeating=? WHERE E_id=?");
 
             //Delete statements
             entryDelete = conn.prepareStatement("DELETE FROM entry WHERE E_id=?");
             taskDelete = conn.prepareStatement("DELETE from task WHERE E_id=?");
             eventDelete = conn.prepareStatement("DELETE FROM event WHERE E_id=?");
 
             getId = conn.prepareStatement("SELECT MAX(E_id) from entry");
         } catch(SQLException sqle){
             printSQLException(sqle);
         }
     }
 
     /*=====================================/
                 11 = insert task
                 12 = insert event
                 21 = update task
                 22 = update event
                 31 = delete task
                 32 = delete event
                 41 = query task
                 42 = query event
     /*===================================*/
     public void doStatement(int m){
         try{
          ResultSet id = getId.executeQuery();
          if(m == 11){
             entryInsert.setString(1, name);
             entryInsert.setString(2, desc);
             entryInsert.setTimestamp(3, timestamp);
             entryInsert.executeUpdate();
             id.next();
             int E_id = id.getInt(1); //Grab the Primary Key of the Entry to be used as a Foreign Key for Event
             taskInsert.setInt(1, E_id);
             taskInsert.setInt(2, status);
             taskInsert.setInt(3, priority);
             taskInsert.executeUpdate();
             System.out.println("Insert Task Succesful!");
          } else if(m == 12){
             // entryInsert.setString(1, name);
             // entryInsert.setString(2, desc);
             // entryInsert.setTimestamp(3, startTime);
             // entryInsert.executeUpdate();
             // ResultSet id = getId.executeQuery();
             // id.next();
             // int E_id = id.getInt(1); //Grab the Primary Key of the Entry to be used as a Foreign Key for Event
             // eventInsert.setInt(1, E_id);
             // eventInsert.setBoolean(2, isAllDay);
             // eventInsert.setTimestamp(3, endTime);
             // eventInsert.setInt(4,repeating);
             // eventInsert.executeUpdate();
          } else if(m == 21){
             // entryUpdate.setString(1, name);
             // entryUpdate.setString(2, desc);
             // entryUpdate.setTimestamp(3, startTime);
             // entryUpdate.setInt(4, id);
             // entryUpdate.executeUpdate();
             // taskUpdate.setInt(1, status);
             // taskUpdate.setInt(2, priority);
             // taskUpdate.setInt(3, id);
             // taskUpdate.executeUpdate();
          } else if(m == 22){
             // entryUpdate.setString(1, name);
             // entryUpdate.setString(2, desc);
             //entryInsert.setTimestamp(3, startTime);
             // entryUpdate.setInt(4, id);
             // entryUpdate.executeUpdate();
             // eventUpdate.setBoolean(1, isAllDay);
             //eventUpdate.setTimestamp(2, endTime);
             // eventUpdate.setInt(3, repeating);
             // eventUpdate.setInt(4, id);
             // eventUpdate.executeUpdate();
          } else if(m == 31){
             // int E_id = id.getInt(1);
             // entryDelete.setInt(1, E_id);
             // entryDelete.executeUpdate();
             // taskDelete.setInt(1,E_id);
             // taskDelete.executeUpdate();
          } else if(m == 32){
             // int E_id = id.getInt(1);
             // entryDelete.setInt(1, E_id);
             // entryDelete.executeUpdate();
             // eventDelete.setInt(1,E_id);
             // eventDelete.executeUpdate();
          } else if(m == 41){
             rs = s.executeQuery("SELECT entry.E_id, entry.name, entry.description, entry.startTime, task.status, task.priority FROM entry JOIN task ON entry.E_id=task.E_id");
             System.out.println("=========================================");
             while(rs.next()){
                 System.out.println(rs.getString(1)+"::::"+rs.getString(2));
                 System.out.println(rs.getTimestamp(3));
             }
             System.out.println("=========================================");
             rs.close();
          } else if(m == 42){
             // rs = s.executeQuery("SELECT entry.E_id, entry.name, entry.description, entry.startTime, task.status, task.priority FROM entry JOIN task ON entry.E_id=task.E_id");
             // System.out.println("=========================================");
             // while(rs.next()){
             //     //System.out.println(rs.getWhatever(1)+"::::"+rs.getWhatever());
             // }
             // System.out.println("=========================================");
             // rs.close();
          }
         } catch(SQLException sqle){
             printSQLException(sqle);
         }
     }
     
     /*=================================================*/
     /** The following are copypasta from Derby sauce **/
     /*===============================================*/
 
     public void closeDatabase() {
         /*
          * In embedded mode, an application should shut down the database.
          * If the application fails to shut down the database,
          * Derby will not perform a checkpoint when the JVM shuts down.
          * This means that it will take longer to boot (connect to) the
          * database the next time, because Derby needs to perform a recovery
          * operation.
          *
          * It is also possible to shut down the Derby system/engine, which
          * automatically shuts down all booted databases.
          *
          * Explicitly shutting down the database or the Derby engine with
          * the connection URL is preferred. This style of shutdown will
          * always throw an SQLException.
          *
          * Not shutting down when in a client environment, see method
          * Javadoc.
          */
        try
         {
             // the shutdown=true attribute shuts down Derby
             DriverManager.getConnection("jdbc:derby:;shutdown=true");
 
             // To shut down a specific database only, but keep the
             // engine running (for example for connecting to other
             // databases), specify a database in the connection URL:
             //DriverManager.getConnection("jdbc:derby:" + dbName + ";shutdown=true");
         }
         catch (SQLException se)
         {
             if (( (se.getErrorCode() == 50000)
                     && ("XJ015".equals(se.getSQLState()) ))) {
                 // we got the expected exception
                 System.out.println("Derby shut down normally");
                 // Note that for single database shutdown, the expected
                 // SQL state is "08006", and the error code is 45000.
             } else {
                 // if the error code or SQLState is different, we have
                 // an unexpected exception (shutdown failed)
                 System.err.println("Derby did not shut down normally");
                 printSQLException(se);
             }
         }
     }
     
     //Loads the embedded driver
     private void loadDriver() {
         /*
          *  The JDBC driver is loaded by loading its class.
          *  If you are using JDBC 4.0 (Java SE 6) or newer, JDBC drivers may
          *  be automatically loaded, making this code optional.
          *
          *  In an embedded environment, this will also start up the Derby
          *  engine (though not any databases), since it is not already
          *  running.
          */
         try {
             Class.forName(driver).newInstance();
             System.out.println("Loaded the appropriate driver");
         } catch (ClassNotFoundException cnfe) {
             System.err.println("\nUnable to load the JDBC driver " + driver);
             System.err.println("Please check your CLASSPATH.");
             cnfe.printStackTrace(System.err);
         } catch (InstantiationException ie) {
             System.err.println(
                         "\nUnable to instantiate the JDBC driver " + driver);
             ie.printStackTrace(System.err);
         } catch (IllegalAccessException iae) {
             System.err.println(
                         "\nNot allowed to access the JDBC driver " + driver);
             iae.printStackTrace(System.err);
         }
     }
 
     /**
      * Reports a data verification failure to System.err with the given message.
      *
      * @param message A message describing what failed.
      */
     private void reportFailure(String message) {
         System.err.println("\nData verification failed:");
         System.err.println('\t' + message);
     }
 
     /**
      * Prints details of an SQLException chain to <code>System.err</code>.
      * Details included are SQL State, Error code, Exception message.
      *
      * @param e the SQLException from which to print details.
      */
     public static void printSQLException(SQLException e)
     {
         // Unwraps the entire exception chain to unveil the real cause of the
         // Exception.
         while (e != null)
         {
             System.err.println("\n----- SQLException -----");
             System.err.println("  SQL State:  " + e.getSQLState());
             System.err.println("  Error Code: " + e.getErrorCode());
             System.err.println("  Message:    " + e.getMessage());
             // for stack traces, refer to derby.log or uncomment this:
             e.printStackTrace(System.err);
             e = e.getNextException();
         }
     }    
 }
