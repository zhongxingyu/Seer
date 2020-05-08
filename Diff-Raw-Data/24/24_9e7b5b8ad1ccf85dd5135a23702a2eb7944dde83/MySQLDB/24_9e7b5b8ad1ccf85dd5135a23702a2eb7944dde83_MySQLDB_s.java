 package lad.db;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.LinkedList;
 import java.util.ListIterator;
 import java.util.Properties;
 import lad.game.Debug;
 import lad.game.LADJava;
 
 /**
  * Manages the connection to the MySQL DB.
  *
  * MySQL calls need a Connection to be able to process.  Primary entry point to
  * this class is the getConn method.
  *
  * @author msflowers
  */
 public class MySQLDB
 {
     /**
      * Holds the actual connection to the database.
      */
     private Connection conn = null;
 
     /**
      * Holds the runner that performs a delayed (not really) execution of MySQL
      * statements
      */
     private DelayedRunner runner = null;
 
     /**
      * A list of all the tables currently loaded into the database.
      */
     private LinkedList< String > loadedTables = new LinkedList<>();
 
     /**
      * Private ctor.
      */
     private MySQLDB()
     {
     }
 
     /**
      * Performs actual connection to the MySQL DB.
      *
      * Connects to the DB using specified username/password. Called if the
      * connection is not valid in @see getConn
      * 
      * @throws SQLException
      */
     public void initialize() throws SQLException
     {
         Properties connectionProps = new Properties();
         connectionProps.put( "user", "admin_lad" );
         connectionProps.put( "password", "password" );
         connectionProps.put( "autoReconnect", "true" );
         conn = DriverManager.getConnection( "jdbc:mysql://localhost/",
                                             connectionProps );
 
         try
         {
             Statement stmt = conn.createStatement();
             stmt.executeQuery( "USE admin_lad" );
         }
         catch( SQLException e )
         {
             System.err.println( "Error while selecting database." +
                                 e.toString() );
             System.exit( -1 );
         }
         
         // Start the Delayed runner thread
         runner = new DelayedRunner( conn.createStatement() );
         new Thread( runner ).start();
 
         // Populate the table list
         populateTableList();
     }
 
     /**
      * Static function to get the MySQL connection.
      *
      * @return A valid Connection to the MySQL Database
      */
     public static Connection getConn()
     {
         return getInstance().getConnection();
     }
 
     /**
      * Simply notifies the delayed runner thread to wake up.
      *
      * Used if the message pump needs to be refreshed or if the system is
      * exiting.
      */
     public static void notifyRunner()
     {
         Object notifier = getInstance().runner.notifier;
         synchronized( notifier )
         {
             notifier.notify();
         }
     }
 
     /**
      * Gets the connection to the MySQL DB.
      *
      * The initialization should be called first.  The internal handler for
      * initializing within this function is to exit on failure.
      *
      * @see lad.db.MySQLDB#initialize()
      * @return A valid connection
      */
     private Connection getConnection()
     {
         if( conn == null )
         {
             try
             {
                 initialize();
             }
             catch( SQLException e )
             {
                 System.err.println( "Failed to connect to MySQL DB." );
                 System.err.println( e.toString() );
                 System.exit( -1 );
             }
         }
         return conn;
     }
 
     /**
      * Initializes the tables list.
      *
      * Pulls the list of tables from the DB and populates the loaded tables
      * variable with all of the table names.
      *
      * @see lad.db.MySQLDB#loadedTables
      */
     public void populateTableList()
     {
         try
         {
             Statement stmt = conn.createStatement();
             ResultSet result = stmt.executeQuery( "SHOW TABLES" );
             while( result.next() )
             {
                 loadedTables.add( result.getString( 1 ) );
             }
         }
         catch( SQLException e )
         {
             System.err.println( "Error while retrieving table list." +
                                 e.toString() );
             System.exit( -1 );
         }
     }
 
     /**
      * Returns the list of tables that is currently loaded in the MySQL DB.
      *
      * @return loadedTables
      */
     public static String[] getTableList()
     {
         return getInstance().loadedTables.toArray( new String[0] );
     }
 
     /**
      * Validates that a MySQL Table is valid for information.
      *
      * @param fields Array of fields that the table should have
      * @param tblName Name of the table
      * @throws SQLException Thrown when either a header is mismatched or the
      *                      column count is wrong
      */
     public void validateStructure( String[] fields, String tblName )
                 throws SQLException
     {
         Statement stmt = getConnection().createStatement();
         ResultSet result = stmt.executeQuery( "SHOW COLUMNS FROM " + tblName );
         int i = 0;
 
         // Iterate over each column and make sure it matches
         while( result.next() )
         {
             if( fields[ i ].compareToIgnoreCase( result.getString( 1 ) ) != 0 )
             {
                 throw new SQLException( "Table header error." );
             }
             i++;
         }
 
         // Just in case a column is missing off the end
         if( i != fields.length )
         {
             throw new SQLException( "Table column miscount." );
         }
     }
 
     /**
      * Validates that a MySQL Table exists.
      *
      * @param tblName Name of the table
      * @param creationStr String to run to load the table if it is not created
      * @throws SQLException Thrown if the table fails to create
      */
     public void validateTable( String tblName, String creationStr )
                 throws SQLException
     {
         // Check the list to see if it's there
         ListIterator< String > iter = loadedTables.listIterator();
         while( iter.hasNext() )
         {
             String tbl = iter.next();
             if( tbl.compareToIgnoreCase( tblName ) == 0 )
             {
                 // Table exists, so exit
                 return;
             }
         }
 
         // Table doesn't exist, so load it
         Statement stmt = getConnection().createStatement();
         stmt.executeUpdate( creationStr );
 
         // Table was successfully added, so add it to the list
         loadedTables.add( tblName );
     }
 
     /**
      * Adds a statement to be delay-executed.
      *
      * The statement is added to a queue that is continuously emptied in a
      * separate thread.  That threads sole responsibility is to make sure the
      * queue remains empty so it will be relatively small.
      *
      * @param stmt The string to be delay-executed
      */
     public static void delaySQL( String stmt )
     {
         DelayedRunner r = getInstance().runner;
         r.addStmt( stmt );
         synchronized( r.notifier )
         {
             r.notifier.notify();
         }
     }
 
     /**
      * Adds a statement to be delay-executed.
      *
      * The statement is added to a queue that is continuously emptied in a
      * separate thread.  That threads sole responsibility is to make sure the
      * queue remains empty so it will be relatively small.
      *
      * @param stmt The prepared statement to be delay-executed
      */
     public static void delaySQL( PreparedStatement stmt )
     {
         DelayedRunner r = getInstance().runner;
         r.addStmt( stmt );
         synchronized( r.notifier )
         {
             r.notifier.notify();
         }
     }
 
     /**
      * Gets the instance
      *
      * @return Singleton
      */
     public static MySQLDB getInstance()
     {
         return MySQLDBHolder.INSTANCE;
     }
 
     private static class MySQLDBHolder
     {
         private static final MySQLDB INSTANCE = new MySQLDB();
     }
 
     private class DelayedRunner implements Runnable
     {
         private final Object notifier = new Object();
         private final LinkedList< String > stmts;
         private final LinkedList< PreparedStatement > pstmts;
         private final Statement stmt;
 
         DelayedRunner( Statement n_stmt )
         {
             // Set internal variables
             stmts = new LinkedList<>();
             pstmts = new LinkedList<>();
             stmt = n_stmt;
         }
 
         void addStmt( String str )
         {
             // Add statement (string)
             synchronized( stmts )
             {
                 stmts.add( str );
             }
         }
 
         void addStmt( PreparedStatement pstmt )
         {
             // Add statement (prepared)
             synchronized( pstmts )
             {
                 pstmts.add( pstmt );
             }
         }
 
         @Override
         public void run()
         {
             // Waits on notifier to wake up
             Debug.log( "Started MySQL Delayed Runner Thread", "Thread" );
             try
             {
                 while( LADJava.running )
                 {
                     synchronized( notifier )
                     {
                         notifier.wait();
                         emptyQueue();
                     }
                 }
             }
             catch( InterruptedException e )
             {
             }
             emptyQueue();
             Debug.log( "Ended MySQL Delayed Runner Thread", "Thread" );
         }
 
         private void emptyQueue()
         {
             // Empties both the string and the prepared queues by
             // executing all of the functions in the arrays
             try
             {
                 synchronized( stmts )
                 {
                     while( stmts.size() > 0 )
                     {
                         String stmtString = stmts.poll();
                        stmt.execute( stmtString );
 
                         Debug.log( "Delay ran a SQL stmt.", "MySQL" );
                     }
                 }
                synchronized( stmts )
                 {
                     while( pstmts.size() > 0 )
                     {
                         PreparedStatement pstmt = pstmts.poll();
                        pstmt.executeUpdate( );
 
                         Debug.log( "Delay ran a SQL pstmt.", "MySQL" );
                     }
                 }
             }
             catch( SQLException e )
             {
                 System.err.println( "Delayed execution of SQL failed." );
                 System.err.println( e.toString() );
             }
         }
     }
 }
