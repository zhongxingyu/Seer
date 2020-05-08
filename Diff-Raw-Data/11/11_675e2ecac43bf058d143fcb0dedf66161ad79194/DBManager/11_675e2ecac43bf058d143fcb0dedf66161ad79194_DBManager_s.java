 package lad.db;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 /**
  * Low level class for handling the loading of all DB tables.
  *
  * @author msflowers
  */
 public abstract class DBManager
 {
     /**
      * Empty ctor
      */
     public DBManager()
     {
         
     }
 
     /**
      * Implement to return all of the table profiles to load
      *
      * @return List of profiles
      */
     public abstract TableProfile[] profiles();
 
     /**
      * Initializes the table.
      *
      * Starts by performing validation on the given table in SQL.  If the
      * headers do not match/do not exist, then the table is re/created.
      * Once the table is known to be good, all rows are pulled and sent through
      * the profile for handling.  Any errors that occur during this process are
      * fatal and will cause the server to shutdown immediately.
      */
     public void initialize()
     {
         TableProfile[] profs = profiles();
         final int proflen = profs.length;
         Connection conn = MySQLDB.getConn();
 
         for( int i = 0; i < proflen; i++ )
         {
             String[] tableHeaders = profs[ i ].tableHeaders();
             String   tableName = profs[ i ].tableName();
             String   createStr = profs[ i ].createString();
             MySQLDB db = MySQLDB.getInstance();
             try
             {
                 db.validateTable( tableName, createStr );
             }
             catch( SQLException e )
             {
                 System.err.println( "Error with " + tableName + " table:" +
                                     e.toString() );
                 System.exit( -1 );
             }
             try
             {
                 db.validateStructure( tableHeaders, tableName );
             }
             catch( SQLException e )
             {
                 System.err.println( "Error with " + tableName + " headers." +
                                     e.toString() );
                     System.exit( -1 );
                 }
 
             try
             {
                 if( profs[ i ].loadData() )
                 {
                     Statement stmt = conn.createStatement();
                     ResultSet result = stmt.executeQuery( "SELECT * FROM " +
                                                         tableName );
                     while( result.next() )
                     {
                         profs[ i ].loadRow( result );
                     }
                 }

                profs[ i ].postinit();
             }
             catch( SQLException e )
             {
                 System.err.println( "Error while initializing " + tableName +
                                     ": " + e.toString() );
                 System.exit( -1 );
             }
         }
     }
 }
