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
     int aptNo;
     String city;
     String country;
     String sex;
     String publicStatus;
     String password;
 
     private Patient(int pid, Date dob, String name, int aptNo, String city, String country, String sex, String publicStatus, String password) {
         this.pid = pid;
         this.dob = dob;
         this.name = name;
         this.aptNo = aptNo;
         this.city = city;
         this.country = country;
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
                 return new Patient(rs.getInt("pid"), rs.getDate("dob"), rs.getString("name"), rs.getInt("aptNo"),
                         rs.getString("city"), rs.getString("country"), rs.getString("sex"), rs.getString("publicStatus"),
                         rs.getString("password"));
             }
         } catch (SQLException e) {
             throw new MyException("Could not get patient with id " + pid);
         }
         return null;
     }
 
     static int insert(Date dob, String name, int aptNo, String city, String country, String sex, String publicStatus,
                       String password, MyConnection conn)
             throws MyException {
         java.sql.Date longDOB = new java.sql.Date(dob.getTime());
         try {
             setSeqNum(conn);
             String query = "INSERT INTO patient values(?,?,?,?,?,?,?,?,?)";
             PreparedStatement pstmt = conn.conn.prepareStatement(query);
             pstmt.setInt(1, seqNum);
             pstmt.setDate(2, longDOB);
             pstmt.setString(3, name);
             pstmt.setInt(4, aptNo);
             pstmt.setString(5, city);
             pstmt.setString(6, country);
             pstmt.setString(7, sex);
             pstmt.setString(8, publicStatus);
             pstmt.setString(9, password);
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
                 prospectiveFriends.add(new Patient(resultSet.getInt(1), new Date(), resultSet.getString(2), 0, "", "",
                         "", "", ""));
             return prospectiveFriends;
 
         } catch (SQLException e) {
             e.printStackTrace();
             throw new MyException("Could not find friends for patient " + name);
         }
     }
 
     public static List<Patient> findHealthFriendsAtRisk(Integer pid, MyConnection conn) throws MyException {
         try {
             List<Patient> friendsAtRisk = new ArrayList<Patient>();
            String query = "select h.fid from healthfriend h,alert a where h.pid = "+pid+" and a.viewed='0' and a.pid=h.fid";
             ResultSet resultSet = conn.stmt.executeQuery(query);
 
             while(resultSet.next())
                 friendsAtRisk.add(getById(resultSet.getInt(1), conn));
             return friendsAtRisk;
         } catch (SQLException e) {
             throw new MyException("Could not find friends for this patient");
         }
     }
 
     public static List<Patient> getAllPatients(MyConnection myConn) throws MyException {
         List<Patient> patients = new ArrayList<Patient>();
         String query = "SELECT * from Patient";
         try {
             ResultSet resultSet = myConn.stmt.executeQuery(query);
             while (resultSet.next())
                 patients.add(new Patient(resultSet.getInt(1),resultSet.getDate(2),resultSet.getString(3), resultSet.getInt(4),
                         resultSet.getString(5), resultSet.getString(6), resultSet.getString(7), resultSet.getString(8),
                         resultSet.getString(9)));
 
         } catch (SQLException e) {
             throw new MyException("Error in retrieving Patients");
         }
         return patients;
     }
 
     public void addClass(Integer cid, MyConnection myConn) throws MyException {
         PatientClassRelationship isExisting = PatientClassRelationship.getById(this.pid, cid, myConn);
         if(isExisting == null){
             PatientClassRelationship.insert(this.pid, cid, myConn);
             System.out.println("Successfully added Patient to class");
             return;
         }
         System.out.println("Patient already belongs to this class.");
     }
 }
