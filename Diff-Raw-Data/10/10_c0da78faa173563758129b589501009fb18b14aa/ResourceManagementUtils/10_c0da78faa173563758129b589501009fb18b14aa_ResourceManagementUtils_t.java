 package net.madz.db.utils;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 public class ResourceManagementUtils {
 
    public static void closeResultSet(ResultSet rs) {
        if ( null != rs ) {
            try {
                rs.close();
            } catch (SQLException ignored) {
                LogUtils.debug(ResourceManagementUtils.class, ignored.getMessage());
            }
         }
     }
 }
