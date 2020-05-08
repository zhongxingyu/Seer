 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 public class HealthProfessional {
 
     static int seqNum;
 
     static {
         seqNum = 0;
     }
 
     int hpid;
     String name;
     String clinic;
     String password;
 
     private HealthProfessional(int hpid, String name, String clinic, String password) {
         this.hpid = hpid;
         this.name = name;
         this.clinic = clinic;
         this.password = password;
     }
 
     private static void setSeqNum(MyConnection connection) throws SQLException {
         if(seqNum == 0) {
             String query = "SELECT COALESCE(MAX(hpid), 0) as hpid from HealthProfessional";
             ResultSet resultSet = connection.stmt.executeQuery(query);
             while (resultSet.next())
                 seqNum = resultSet.getInt("hpid") + 1;
         }
     }
 
 
     static HealthProfessional getById(int hpid, MyConnection conn) throws MyException {
         try {
            String query = "select * from patient where hpid = " + hpid;
             ResultSet rs = conn.stmt.executeQuery(query);
             while (rs.next()) {
                 return new HealthProfessional(rs.getInt("hpid"), rs.getString("name"),
                         rs.getString("clinic"), rs.getString("password"));
             }
         } catch (SQLException e) {
             throw new MyException("Could not get Health Professional with id " + hpid);
         }
         return null;
     }
 
     static int insert(String name, String clinic, String password, MyConnection conn) throws MyException {
         try {
             setSeqNum(conn);
             String query = "INSERT INTO HealthProfessional values(?,?,?,?)";
             PreparedStatement pstmt = conn.conn.prepareStatement(query);
             pstmt.setInt(1, seqNum);
             pstmt.setString(2, name);
             pstmt.setString(3, clinic);
             pstmt.setString(4, password);
             int ret = pstmt.executeUpdate();
 
             if (ret != 0)
                 return seqNum++;
 
         } catch (SQLException e) {
             throw new MyException("Insertion of Health Professional with name " + name + "failed");
         }
         return 0;
     }
 
 }
