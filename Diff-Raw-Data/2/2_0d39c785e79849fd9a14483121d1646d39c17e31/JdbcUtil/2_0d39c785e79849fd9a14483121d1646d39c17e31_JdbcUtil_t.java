 package de.hswt.hrm.common.database;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 /**
  * Provides some common JDBC utility methods.
  */
 public final class JdbcUtil {
     
     private JdbcUtil() { }
 
     /**
      * Parses an ID field (or FK).
      * 
      * @param rs ResultSet which contains the column.
      * @param column Name of the column that contains the ID.
      * @return The ID or '-1' if the columns value is null.
      * @throws SQLException 
      */
     public static int getId(final ResultSet rs, final String column) throws SQLException {
         Object value = rs.getObject(column);
         if (value == null) {
             return -1;
         }
         
         return (int) value;
     }
     
     /**
      * @param calendar
      * @return Timestamp set to the time of the given calendar.
      */
     public static Timestamp timestampFromCalendar(final Calendar calendar) {
     	Timestamp ts = new Timestamp(calendar.getTimeInMillis());
     	return ts;
     }
     
     /**
      * 
      * @param ts
      * @return Calendar which is set to the time of the given timestamp.
      */
    public static Calendar calendarfromTimestamp(final Timestamp ts) {
     	Calendar calendar = GregorianCalendar.getInstance();
     	calendar.setTimeInMillis(ts.getTime());
     	
     	return calendar;
     }
 }
