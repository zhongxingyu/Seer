 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package cz.muni.fi.pv168.clockcard;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 /**
  *
  * @author Marek Osvald
  */
 
 public class Shift {
 
    private final static String INSERT_SHIFT = "INSERT INTO APP.SHIFTS(worker_id, shift_start,shift_end,last_break,total_break_time) values(?,?,?,?,?)";
    private final static String UPDATE_SHIFT = "UPDATE APP.SHIFTS SET shift_start=?,shift_end=?,last_break=?,total_break_time=?";
    private final static String DELETE_SHIFT = "DELETE FROM APP.SHIFTS WHERE id=?";
 
    private long id=0;
    private long workerId=0;
    private Calendar start=new GregorianCalendar(0, 0, 0, 0, 0, 0);
    private Calendar end=new GregorianCalendar(0, 0, 0, 0, 0, 0);
    private Calendar lastBreakStart=new GregorianCalendar(0, 0, 0, 0, 0, 0);
    private long totalBreakTime=0;
 
    /**
     * Constructor for manager
     * @param id
     * @param workerId
     * @param start
     * @param end
     * @param lastBreakStart
     * @param totalBreakTime
     */
    public Shift(long id,long workerId, Calendar start, Calendar end, Calendar lastBreakStart, long totalBreakTime){
         this.id=id;
         this.workerId=workerId;
         this.start=start;
         this.end=end;
         this.lastBreakStart=lastBreakStart;
         this.totalBreakTime=totalBreakTime;
    }
 
    /**
     * Constructor for start shift
     * @param workerId
     * @param start
     */
     public Shift(long workerId, Calendar start) {
         this.workerId = workerId;
         this.start = start;
     }
 
     /**
      * Method to save Shift to db.
      * If shift is created by "short" constructor, save method save shift to db and set generated id to this object, else just save shift to db.
      * @throws SQLException
      */
     public void save() throws SQLException{
         Connection con = ConnectionManager.getConnection();
         if(id==0){
             PreparedStatement prepareStatement = con.prepareStatement(INSERT_SHIFT,Statement.RETURN_GENERATED_KEYS);
                 prepareStatement.setLong(1, workerId);
                 prepareStatement.setTimestamp(2, new Timestamp(start.getTimeInMillis()));
                 prepareStatement.setTimestamp(3, new Timestamp(end.getTimeInMillis()));
                 prepareStatement.setTimestamp(4, new Timestamp(lastBreakStart.getTimeInMillis()));
                 prepareStatement.setLong(5, totalBreakTime);
                 prepareStatement.execute();
             ResultSet result =  prepareStatement.getGeneratedKeys();
                 result.next();
                 id = result.getLong(1);
         }else{
             PreparedStatement prepareStatement = con.prepareStatement(UPDATE_SHIFT);
                 prepareStatement.setTimestamp(1, new Timestamp(start.getTimeInMillis()));
                 prepareStatement.setTimestamp(2, new Timestamp(end.getTimeInMillis()));
                 prepareStatement.setTimestamp(3, new Timestamp(lastBreakStart.getTimeInMillis()));
                 prepareStatement.setLong(4, totalBreakTime);
                 prepareStatement.executeUpdate();
         }
 
     }
 
     /**
      * Delete shift from db.
      * @throws SQLException
      */
     public void destroy() throws SQLException{
         Connection con = ConnectionManager.getConnection();
         PreparedStatement prep = con.prepareStatement(DELETE_SHIFT);
             prep.setLong(1, this.id);
             prep.execute();
     }
 
     public Calendar getEnd() {
         return end;
     }
 
     public void setEnd(Calendar end) {
         this.end = end;
     }
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     public Calendar getLastBreakStart() {
         return lastBreakStart;
     }
 
     public void setLastBreakStart(Calendar lastBreakStart) {
         this.lastBreakStart = lastBreakStart;
     }
 
     public Calendar getStart() {
         return start;
     }
 
     public void setStart(Calendar start) {
         this.start = start;
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
 
     public void setWorkerId(long workerId) {
         this.workerId = workerId;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Shift other = (Shift) obj;
         if (this.id != other.getId()) {
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


 }
