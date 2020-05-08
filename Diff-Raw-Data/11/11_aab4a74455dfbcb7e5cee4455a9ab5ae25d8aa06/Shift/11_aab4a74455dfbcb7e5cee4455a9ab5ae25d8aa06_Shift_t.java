 package cz.muni.fi.pv168.clockcard;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.sql.*;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Properties;
 
 /**
  * Represents a shift worked by a worker.
  *
  * @author David Stein
  * @author Marek Osvald
  *
  * @version 2011.0518
  */
 public class Shift extends ADatabaseStoreable {
     private final static String PROPERTY_FILE = "src/Shift.properties";
 
     private Properties properties = loadProperties();
     private Long id;
     private long workerId = 0;

     private Calendar start = new GregorianCalendar(0, 0, 0, 0, 0, 0);
     private Calendar end = new GregorianCalendar(0, 0, 0, 0, 0, 0);
     private Calendar lastBreakStart = new GregorianCalendar(0, 0, 0, 0, 0, 0);
     private long totalBreakTime = 0;
 
     /**
      * TODO: Javadoc me!
      * 
      * @param id
      * @param workerID
      * @param start
      * @param end
      * @param lastBreakStart
      * @param totalBreakTime
      * @return
      */
     public static Shift loadShift(Long id, long workerID, Calendar start, Calendar end, Calendar lastBreakStart, long totalBreakTime) {
         return new Shift(id, workerID, start, end, lastBreakStart, totalBreakTime);
     }
 
     /**
      * Parametric constructor.
      *
      * @param id unique ID of the shift
      * @param workerId unique ID of the workrer
      * @param start date and time of the shift's start
      * @param end date and time of the shift's start
      * @param lastBreakStart date and time of the start of the last break
      * @param totalBreakTime total break time
      */
     private Shift(Long id, long workerID, Calendar start, Calendar end, Calendar lastBreakStart, long totalBreakTime) {
         this.id = id;
         this.workerId = workerID;
         this.start = start;
         this.end = end;
         this.lastBreakStart = lastBreakStart;
         this.totalBreakTime = totalBreakTime;
     }
 
     /**
      * Parametric constructor. Creates shift based on worker's ID and date and time of the shift's start.
      * @param workerId unique ID of the worker
      * @param start date and time of the shift's starts
      */
     public Shift(long workerId, Calendar start) {
         this.workerId = workerId;
         this.start = start;
     }
 
     @Override
     public boolean save() {
         Connection connection = null;
         PreparedStatement preparedStatement;
         ResultSet resultSet;
         boolean result = false;
 
         try {
             connection = ConnectionManager.getConnection();
 
             if (id == null) {
                 preparedStatement = connection.prepareStatement(properties.getProperty("saveQuery"), Statement.RETURN_GENERATED_KEYS);
                 preparedStatement.setLong(1, workerId);
                 preparedStatement.setTimestamp(2, new Timestamp(start.getTimeInMillis()));
                 preparedStatement.setTimestamp(3, new Timestamp(end.getTimeInMillis()));
                 preparedStatement.setTimestamp(4, new Timestamp(lastBreakStart.getTimeInMillis()));
                 preparedStatement.setLong(5, totalBreakTime);
                 resultSet = preparedStatement.executeQuery();
                 resultSet.next();
                 id = resultSet.getLong(1);
                 result = true;
             } else {
                 preparedStatement = connection.prepareStatement(properties.getProperty("updateQuery"));
                 preparedStatement.setTimestamp(1, new Timestamp(start.getTimeInMillis()));
                 preparedStatement.setTimestamp(2, new Timestamp(end.getTimeInMillis()));
                 preparedStatement.setTimestamp(3, new Timestamp(lastBreakStart.getTimeInMillis()));
                 preparedStatement.setLong(4, totalBreakTime);
                 result = (preparedStatement.executeUpdate() == 1);
             }
         } catch (SQLException ex) {
             //TODO: log an exception
         } finally {
             try {
                 connection.close();
             } catch (SQLException ex) {
                 //TODO: log an exception
             }
         }
 
         return result;
     }
 
     /**
      * Deletes a shift from the database.
      *
      * @throws SQLException
      */
     @Override
     public boolean destroy() {
         Connection connection = null;
         PreparedStatement preparedStatement;
         boolean result = false;
 
         try {
             connection = ConnectionManager.getConnection();
             preparedStatement = connection.prepareStatement(properties.getProperty("deleteQuery"));
             preparedStatement.setLong(1, id);
             result = (preparedStatement.executeUpdate() == 1);
         } catch (SQLException ex) {
             //log an exception
         } finally {
             try {
                 connection.close();
             } catch (SQLException ex) {
                 //TODO: log an exception
             }
         }
 
         return result;
     }
 
     /**
      * Returns date and time of the end of the shift.
      *
      * @return date and time of the end of the shift
      */
     public Calendar getEnd() {
         return end;
     }
 
     /**
      * Sets end date and time of the shift.
      * 
      * @param end end date and time of the shift
      */
     public void setEnd(Calendar end) {
         this.end = end;
     }
 
     /**
      * Returns unique ID of the shift.
      *
      * @return unique ID of the shift
      */
     public long getID() {
         return id;
     }
 
     /**
      * Returns start date and time of the last break during shift.
      *
      * @return start date and time of the last break during shift
      */
     public Calendar getLastBreakStart() {
         return lastBreakStart;
     }
 
     /**
      * Sets start date and time of the last break during shift.
      *
      * @param lastBreakStart start date and time of the last break during shift
      */
     public void setLastBreakStart(Calendar lastBreakStart) {
         this.lastBreakStart = lastBreakStart;
     }
 
     public Calendar getStart() {
         return start;
     }
 
     public long getTotalBreakTime() {
         return totalBreakTime;
     }
 
     public void setTotalBreakTime(long totalBreakTime) {
         this.totalBreakTime = totalBreakTime;
     }
 
     public long getWorkerId() {
         return workerId;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null || getClass() != obj.getClass()) {
             return false;
         }
 
         final Shift other = (Shift) obj;
         if (this.id != other.id) {
             return false;
         }
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 59 * hash + (int) (this.id ^ (this.id >>> 32));
         return hash;
     }
 
     public static Properties loadProperties() {
         try {
             FileInputStream inputStream = new FileInputStream(PROPERTY_FILE);
             Properties prop = new Properties();
             prop.load(inputStream);
             inputStream.close();
             return prop;
         } catch (IOException e) {
             return new Properties();
             //TODO: LOG fatal error, Property file not found.
         }
     }
 }
