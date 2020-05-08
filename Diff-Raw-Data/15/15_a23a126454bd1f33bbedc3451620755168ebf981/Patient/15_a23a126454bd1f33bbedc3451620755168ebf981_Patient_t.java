 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class Patient {
     static int seqNum;
 
     static {
         seqNum = 0;
     }
 
     int pid;
     Date dob;
     String name;
     String address;
     String sex;
     String publicStatus;
     String password;
 
     private Patient(int pid, Date dob, String name, String address, String sex, String publicStatus, String password) {
         this.pid = pid;
         this.dob = dob;
         this.name = name;
         this.address = address;
         this.sex = sex;
         this.publicStatus = publicStatus;
         this.password = password;
     }
 
     private static void setSeqNum(MyConnection connection) throws SQLException {
         if(seqNum == 0) {
             String query = "SELECT COALESCE(MAX(pid), 0) as pid from Patient";
             ResultSet resultSet = connection.stmt.executeQuery(query);
             while (resultSet.next())
                 seqNum = resultSet.getInt("pid") + 1;
         }
     }
 
 
     static Patient getById(int pid, MyConnection conn) throws MyException {
         try {
             String query = "select * from patient where pid = " + pid;
             ResultSet rs = conn.stmt.executeQuery(query);
             while (rs.next()) {
                 return new Patient(rs.getInt("pid"), rs.getDate("dob"), rs.getString("name"), rs.getString("address"),
                         rs.getString("sex"), rs.getString("publicStatus"), rs.getString("password"));
             }
         } catch (SQLException e) {
             throw new MyException("Could not get patient with id " + pid);
         }
         return null;
     }
 
     static int insert(Date dob, String name, String address, String sex, String publicStatus, String password, MyConnection conn)
             throws MyException {
         java.sql.Date longDOB = new java.sql.Date(dob.getTime());
         try {
             setSeqNum(conn);
             String query = "INSERT INTO patient values(?,?,?,?,?,?,?)";
             PreparedStatement pstmt = conn.conn.prepareStatement(query);
             pstmt.setInt(1, seqNum);
             pstmt.setDate(2, longDOB);
             pstmt.setString(3, name);
             pstmt.setString(4, address);
             pstmt.setString(5, sex);
             pstmt.setString(6, publicStatus);
             pstmt.setString(7, password);
             int ret = pstmt.executeUpdate();
 
             if (ret != 0)
                 return seqNum++;
 
         } catch (SQLException e) {
             throw new MyException("Insertion of patient with name " + name + "failed");
         }
         return 0;
     }
 
     public List<Patient> findHealthFriends(MyConnection conn) throws MyException {
         try {
             List<Patient> prospectiveFriends = new ArrayList<Patient>();
            String query = "select DISTINCT p.pid, p.name from patient p, PatientClassRelationship pc where p.pid <> " + this.pid +
                    " AND p.publicStatus = 'y' " + "AND p.pid in (select DISTINCT pid from PatientClassRelationship where cid in " +
                     "(select DISTINCT cid from PatientClassRelationship where pid = "+ this.pid + "))";
             ResultSet resultSet = null;
 
             resultSet = conn.stmt.executeQuery(query);
 
             while(resultSet.next())
                 prospectiveFriends.add(new Patient(resultSet.getInt(1), new Date(), resultSet.getString(2), "", "", "", ""));
             return prospectiveFriends;
 
         } catch (SQLException e) {
            e.printStackTrace();
            throw new MyException("Could not find friends for patient " + name);
         }
     }
 
 }
