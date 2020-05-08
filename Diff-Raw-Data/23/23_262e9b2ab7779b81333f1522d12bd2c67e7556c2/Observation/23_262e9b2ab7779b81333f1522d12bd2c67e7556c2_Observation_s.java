import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class Observation {
 
     int pid;
     int otid;
     Date obvTimestamp;
     Date recTimestamp;
     int qid;
     String answer;
 
     private Observation(int pid, int otid, Date obvTimestamp, Date recTimestamp, int qid, String answer) {
         this.pid = pid;
         this.otid = otid;
         this.obvTimestamp = obvTimestamp;
         this.recTimestamp = recTimestamp;
         this.qid = qid;
         this.answer = answer;
     }
 
     static Observation getById(int pid, int otid, int qid, Date obvTimestamp, MyConnection conn) {
         try {
             String query = "select * from Observation where pid = " + pid + " AND otid = " + otid +
                     " AND qid = " + qid + " AND obvTimestamp = " + obvTimestamp;
             ResultSet rs = conn.stmt.executeQuery(query);
             while (rs.next())
                 return new Observation(rs.getInt("pid"), rs.getInt("otid"), rs.getDate("obvTimestamp"),
                         rs.getDate("recTimestamp"), rs.getInt("qid"), rs.getString("answer"));
 
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     static int insert(int pid, int otid, Date obvTimestamp, Date recTimestamp, int qid, String answer, MyConnection conn) {
        java.sql.Date obvTime = new java.sql.Date(obvTimestamp.getTime());
        obvTime.setTime(obvTimestamp.getTime());
        System.out.println("IN insert!!");
        java.sql.Date recTime = new java.sql.Date(recTimestamp.getTime());
        recTime.setTime(recTimestamp.getTime());
        System.out.println("IN insert!!");
 
         try {
             String query = "INSERT INTO Observation values(?,?,?,?,?,?)";
             PreparedStatement pstmt = conn.conn.prepareStatement(query);
             pstmt.setInt(1, pid);
             pstmt.setInt(2, otid);
            pstmt.setDate(3, obvTime);
            pstmt.setDate(4, recTime);
             pstmt.setInt(5, qid);
             pstmt.setString(6, answer);
            System.out.println("before execute update!!");
             int ret = pstmt.executeUpdate();
            System.out.println("After execute update!!");
 
             if(ret != 0)
                 return pid;
 
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return 0;
     }
 }
