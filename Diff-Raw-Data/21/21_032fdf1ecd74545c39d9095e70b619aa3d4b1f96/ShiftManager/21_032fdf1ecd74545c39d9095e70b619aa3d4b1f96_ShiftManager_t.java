 package cz.muni.fi.pv168.clockcard;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 /**
  *  Class for manage shifts.
  * @author Fires
  */

 public class ShiftManager {
 
     private final static String GET_ALL = "SELECT * FROM APP.shifts";
     private final static String GET_BY_ID = "SELECT * FROM APP.shifts WHERE id=?";
     private final static String GET_COUNT = "SELECT COUNT(*) FROM APP.shifts";
     private final static String GET_BY_WORKER_ID = "SELECT * FROM APP.shifts WHERE worker_id=?";
 
     /**
      * Method for get all shifts in database.
      * @return List<Shift> list of shifts
      * @throws SQLException
      */
     public static List<Shift> getAll() throws SQLException{
         Connection connection = ConnectionManager.getConnection();
         Statement statement = connection.createStatement();
         ResultSet resultSet = statement.executeQuery(GET_ALL);
 
         ArrayList<Shift> result = new ArrayList<Shift>();
         while (resultSet.next()) {
             long id = resultSet.getLong("id");
             long worker_id = resultSet.getLong("worker_id");
             Calendar shift_start = new GregorianCalendar();
                      shift_start.setTimeInMillis(resultSet.getTimestamp("shift_start").getTime());
             Calendar shift_end = new GregorianCalendar();
                      shift_end.setTimeInMillis(resultSet.getTimestamp("shift_end").getTime());
             Calendar last_break = new GregorianCalendar();
                      last_break.setTimeInMillis(resultSet.getTimestamp("last_break").getTime());
             long total_break_time = resultSet.getLong("total_break_time");
             Shift shift = new Shift(id, worker_id, shift_start, shift_end, last_break, total_break_time);
             result.add(shift);
         }
         return Collections.unmodifiableList(result);
     }
 
     /**
      * Method to get shift by shift id.
      * @param id
      * @return Shift
      * @throws SQLException
      * @throws ShiftException
      */
     public static Shift getById(long id) throws SQLException, ShiftException{
         Connection connection = ConnectionManager.getConnection();
         PreparedStatement statement = connection.prepareStatement(GET_BY_ID);
             statement.setLong(1, id);
         ResultSet result = statement.executeQuery();
         if(!result.next()){
             throw new ShiftException("Zaznam nenalezen.");
         }
         long id_result = result.getLong("id");
             long worker_id = result.getLong("worker_id");
             Calendar shift_start = new GregorianCalendar();
                      shift_start.setTimeInMillis(result.getTimestamp("shift_start").getTime());
             Calendar shift_end = new GregorianCalendar();
                      shift_end.setTimeInMillis(result.getTimestamp("shift_end").getTime());
             Calendar last_break = new GregorianCalendar();
                      last_break.setTimeInMillis(result.getTimestamp("last_break").getTime());
             long total_break_time = result.getLong("total_break_time");
             Shift shift = new Shift(id_result, worker_id, shift_start, shift_end, last_break, total_break_time);
             return shift;
     }
 
     /**
      * Method to get shift count in database.
      * @return int number of shifts in database
      * @throws SQLException
      */
     public static int getCount() throws SQLException{
         Connection connection = ConnectionManager.getConnection();
         Statement statement = connection.createStatement();
         ResultSet result = statement.executeQuery(GET_COUNT);
         result.next();
         int count = result.getInt(1);
         return count;
     }
 
     /**
      * Method to get all shift what are assigned to worker.
      * @param workerid
      * @return List<Shift> list of shifts
      * @throws SQLException
      */
     public static List<Shift> getShiftsByWorkerId(long workerid) throws SQLException{
         Connection connection = ConnectionManager.getConnection();
         PreparedStatement statement = connection.prepareStatement(GET_BY_WORKER_ID);
             statement.setLong(1, workerid);
         ResultSet resultSet = statement.executeQuery();
 
         ArrayList<Shift> result = new ArrayList<Shift>();
         while (resultSet.next()) {
             long id = resultSet.getLong("id");
             long worker_id = resultSet.getLong("worker_id");
             Calendar shift_start = new GregorianCalendar();
                      shift_start.setTimeInMillis(resultSet.getTimestamp("shift_start").getTime());
             Calendar shift_end = new GregorianCalendar();
                      shift_end.setTimeInMillis(resultSet.getTimestamp("shift_end").getTime());
             Calendar last_break = new GregorianCalendar();
                      last_break.setTimeInMillis(resultSet.getTimestamp("last_break").getTime());
             long total_break_time = resultSet.getLong("total_break_time");
             Shift shift = new Shift(id, worker_id, shift_start, shift_end, last_break, total_break_time);
             result.add(shift);
         }
         return Collections.unmodifiableList(result);
     }
 
 }
