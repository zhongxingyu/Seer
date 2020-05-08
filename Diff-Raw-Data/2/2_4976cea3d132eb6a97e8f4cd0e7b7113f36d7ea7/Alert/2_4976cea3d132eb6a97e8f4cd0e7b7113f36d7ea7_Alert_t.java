 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class Alert {
 
     int pid;
     String text;
     String viewed;
     Date timestamp;
 
     private Alert(int pid, String text, String viewed, Date timestamp) {
         this.pid = pid;
         this.text = text;
         this.viewed = viewed;
         this.timestamp = timestamp;
     }
 
     static Alert getById(int pid, String text, MyConnection conn) throws MyException {
         try {
             String query = "select * from alert where pid = " + pid + " AND text = " + text;
             ResultSet rs = conn.stmt.executeQuery(query);
             while (rs.next()) {
                 return new Alert(rs.getInt("pid"), rs.getString("text"), rs.getString("viewed"), rs.getDate("timestamp"));
             }
         } catch (SQLException e) {
             e.printStackTrace();
             throw new MyException("Could not get alert for pid = " + pid + " and text = " + text);
         }
         return null;
     }
 
     static List<Alert> getByPId(int pid, MyConnection conn) throws MyException {
         List<Alert> alertList = new ArrayList<Alert>();
         try {
             String query = "select * from alert where pid = " + pid;
             ResultSet rs = conn.stmt.executeQuery(query);
             while (rs.next()) {
                 alertList.add(new Alert(rs.getInt("pid"), rs.getString("text"), rs.getString("viewed"), rs.getDate("timestamp")));
             }
         } catch (SQLException e) {
             e.printStackTrace();
             throw new MyException("Could not get alert for pid = " + pid);
         }
         return alertList;
     }
 
     static void insert(int pid, String text, String viewed, Date timestamp, MyConnection conn) {
         Timestamp longTimestamp = new Timestamp(timestamp.getTime());
         try {
             String query = "INSERT INTO alert values(?,?,?,?)";
             PreparedStatement pstmt = conn.conn.prepareStatement(query);
             pstmt.setInt(1, pid);
             pstmt.setString(2, text);
             pstmt.setString(3, viewed);
             pstmt.setTimestamp(4, longTimestamp);
             pstmt.executeUpdate();
         } catch (SQLException e) {
             return;
         }
     }
 
     void markViewed(MyConnection conn) throws MyException {
         this.viewed = "1";
        String query = "UPDATE alert SET viewed = '1' where pid = "+this.pid+" AND text = '" + this.text + "'";
         try {
             conn.stmt.executeUpdate(query);
         } catch (SQLException e) {
             e.printStackTrace();
             throw new MyException("Marking alert for pid = " + pid + " as viewed failed");
         }
     }
 
     static void deleteViewedAlerts(int pid, MyConnection conn) throws MyException {
         String query = "delete from alert where pid = " + pid + " AND viewed = '1'";
         try {
             conn.stmt.executeQuery(query);
         } catch (SQLException e) {
             e.printStackTrace();
             throw new MyException("Deletion of viewed alerts failed for pid = " + pid);
         }
     }
 
     static boolean ignoredAlertExists(int pid, MyConnection connection) throws MyException {
         Timestamp sevenDaysAgo = new Timestamp(new Date(System.currentTimeMillis() - (7L * 24 * 3600 * 1000)).getTime());
         try {
             String query = "select * from alert where pid = ? AND viewed = '0' AND timestamp >= ?";
             PreparedStatement pstmt = null;
             pstmt = connection.conn.prepareStatement(query);
             pstmt.setInt(1, pid);
             pstmt.setTimestamp(2, sevenDaysAgo);
             ResultSet rs = pstmt.executeQuery();
             while (rs.next())
                 return true;
 
         } catch (SQLException e) {
             e.printStackTrace();
             throw new MyException("Error while checking ignored alerts");
         }
         return false;
     }
 
 }
