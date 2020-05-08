import java.sql.*;
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
        Time obvTime = new Time(obvTimestamp.getTime());
        Time recTime = new Time(recTimestamp.getTime());
 
         try {
             String query = "INSERT INTO Observation values(?,?,?,?,?,?)";
             PreparedStatement pstmt = conn.conn.prepareStatement(query);
             pstmt.setInt(1, pid);
             pstmt.setInt(2, otid);
            pstmt.setTime(3, obvTime);
            pstmt.setTime(4, recTime);
             pstmt.setInt(5, qid);
             pstmt.setString(6, answer);
             int ret = pstmt.executeUpdate();
 
             if(ret != 0)
                 return pid;
 
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return 0;
     }
 }
