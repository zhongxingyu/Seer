 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 public class ObservationThreshold {
     int qid;
     int threshold;
 
     private ObservationThreshold(int qid, int threshold) {
         this.qid = qid;
         this.threshold = threshold;
     }
 
     static int insert(int qid, int value, MyConnection conn) throws MyException {
         try {
             String query = "INSERT INTO ObservationThreshold values(?,?)";
             PreparedStatement pstmt = conn.conn.prepareStatement(query);
             pstmt.setInt(1, qid);
             pstmt.setInt(2, value);
             int ret = pstmt.executeUpdate();
 
             if (ret != 0)
                 return qid;
 
         } catch (SQLException e) {
             throw new MyException("Insertion of threshold for question id " + qid + " failed");
         }
         return 0;
     }
 
     public static int getThresholdFor(int qid, MyConnection conn) throws MyException {
         try {
             String query = "select * from ObservationThreshold where qid = " + qid;
             ResultSet rs = conn.stmt.executeQuery(query);
             while (rs.next())
                return rs.getInt("threshold");
 
         } catch (SQLException e) {
             throw new MyException("Could not get threshold for question with id = " + qid);
         }
         return 0;
     }
 
     public static boolean crossesThreshold(int qid, int value, MyConnection connection) throws MyException {
         int threshold = getThresholdFor(qid, connection);
         if(threshold != 0) {
             if(threshold < 0)
                 return Math.abs(threshold) > value;
             return value > threshold;
         }
         return false;
     }
 }
