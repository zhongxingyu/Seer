 package net.madz.db.utils;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 public class ResourceManagementUtils {
 
    public static void closeResultSet(ResultSet rs) throws SQLException {
        if ( null != rs && !rs.isClosed() ) {
            rs.close();
         }
     }
 }
