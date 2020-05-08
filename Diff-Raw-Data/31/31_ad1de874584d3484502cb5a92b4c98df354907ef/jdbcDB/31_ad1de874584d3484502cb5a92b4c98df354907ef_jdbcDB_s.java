 package gameCommons.balanceSettings;
 
 import java.sql.DriverManager;
 import java.sql.Connection;
 import java.sql.Statement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 
 import gameCommons.balanceSettings.DataStore;
 import gameCommons.system.Trace;
 
 
 public class jdbcDB implements DataStore {
 
     protected HashMap<String,Connection> connMap = new HashMap<String,Connection>();
     protected Trace t;
 
     public jdbcDB(Trace t) {
         this.t = t;
     }
 
     public boolean closeAll() {
         return true;
     }
 
     public boolean addPooledConnection(String key, DbManager connectPool) {
         boolean success = false;
         Connection con = null;
         
         try {
             con = this.connectPool(connectPool);
         } catch (SQLException e) {
             t.trace("SQLException: " + e.getMessage());
             t.trace("SQLState: " + e.getSQLState());
             t.trace("VendorError: " + e.getErrorCode());
         }
         
         if (con != null) {
             connMap.put(key, con);
             success = true;
         } else {
             if (t.verbose && (t.verbose_level>=1)) t.trace("error connection NULL for="+ key);
         }
         return success;
     }
 
     public HashMap<String,Connection> retrieve() {
         return connMap;
     }
 
     protected Connection connect() {
        // Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/test"+"user=reader&password=hello");
         return con;
     }
 
     private Connection connectPool(DbManager conPool) throws SQLException {
         Connection con = conPool.getConnection();
         return con;
     }
 
 
     public int execute( String sql, String region, String key ) {
         if (t.verbose && (t.verbose_level>=4)) t.trace( key +":"+ sql);
 
         Statement stmt = null;
         int resultCode = 0;
         Connection con = null;
         
         con = connMap.get(key);
         if (con == null) {
             t.trace( "Can't get a connection to the database");
             return resultCode;
         }
 
         if (t.trace_timers ) t.timer.push(t.getTimer());
         try {
             stmt = con.createStatement();
             resultCode = stmt.executeUpdate(sql);
         } catch (SQLException e) {
             t.trace("SQLException: " + e.getMessage());
             t.trace("SQLState: " + e.getSQLState());
             t.trace("VendorError: " + e.getErrorCode());
         }
         if (t.trace_timers ) t.timer.push(t.getTimer());
         if (t.trace_timers ) t.trace("DB Request took: " + ( t.timer.pop()-t.timer.pop()) + " ms.");
 
         if (stmt != null) {
             try {
                 stmt.close();
             } catch (SQLException e) { } // ignore
             stmt = null;
         }
         
         return resultCode;
     }
 
 
     public ResultSet query(String sql, String region, String key)
     /**
      * ABSTRACT
      * Sends a query to the database and returns a ResultSet object.
      * Use size() and get(<<int>>) to extract a row from the result and 
      * getItem(<<column name>>) to retrieve a value from a row.
      *
      */
     {
         if (t.verbose && (t.verbose_level>=3)) t.trace( key +":"+ sql);
 
         Statement stmt = null;
         ResultSet rs = null;
         Connection con = null;
         
         con = connMap.get(key);
         if (con == null) {
             t.trace( "Can't get a connection to the database");
         }
 
         if (t.trace_timers ) t.timer.push(t.getTimer());
         try {
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
         } catch (SQLException e) {
             t.trace("SQLException: " + e.getMessage());
             t.trace("SQLState: " + e.getSQLState());
             t.trace("VendorError: " + e.getErrorCode());
         }
         if (t.trace_timers ) t.timer.push(t.getTimer());
         if (t.trace_timers ) t.trace("DB Request took: " + ( t.timer.pop()-t.timer.pop()) + " ms.");
 
     /*
         if (stmt != null) {
             try {
                 stmt.close();
             } catch (SQLException e) { } // ignore
             stmt = null;
         }
     */
 
 	return rs;
 	// rs.close()
     }
 }
