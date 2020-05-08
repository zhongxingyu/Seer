 package DAL;
 
 import BE.Member;
 import com.microsoft.sqlserver.jdbc.SQLServerException;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Reese
  */
 public class MemberAccess
 {
     /* Management */
     // Instance variables \\
     private Connector connector;
     private static MemberAccess instance = null;
 
     // Constructor \\
     /**
      * Constructs an instance of the MemberAccess class
      *
      * @throws FileNotFoundException if the config file is not found
      * @throws IOException if there's an error reading the config file
      */
     private MemberAccess() throws FileNotFoundException, IOException
     {
         connector = Connector.getInstance();
     }
 
     // Singleton \\
     /**
      * Returns an instance of the MemberAccess class, and creates one first, if
      * there's none available
      *
      * @return an instance of the MemberAccess class
      * @throws FileNotFoundException if the config file can't be found
      * @throws IOException if there's an error reading the config file
      */
     public static MemberAccess getInstance() throws FileNotFoundException, IOException
     {
         if (instance == null)
         {
             instance = new MemberAccess();
         }
         return instance;
     }
     
     // Methods \\
     /**
      * Get a member based on his/hers member id
      * @param id the id of the member
      * @return the member corresponding to the id
      * @throws SQLServerException if there's an error connecting to the database
      * @throws SQLException if there's an error in the SQL query execution
      */
     public Member get(int id) throws SQLServerException, SQLException
     {
         try (Connection con = connector.getConnection())
         {
             String sql = "SELECT * FROM Member WHERE Id = ?";
             PreparedStatement ps = con.prepareStatement(sql);
             ps.setInt(1, id);
 
             ResultSet rs = ps.executeQuery();
             if (rs.next())
             {
                 return getOneMember(rs);
             }
             return null;
         }
     }
     
     public List<Member> getAll() throws SQLServerException, SQLException
     {
         System.out.println("DEBUG: running getAll!");
         try (Connection con = connector.getConnection())
         {
             List<Member> members = new ArrayList<>();
             String sql = "SELECT * FROM Member";
             PreparedStatement ps = con.prepareStatement(sql);
             
 
             ResultSet rs = ps.executeQuery();
             int i = 0;
             while (rs.next())
             {
                 members.add(getOneMember(rs));
                 i++;
                 System.out.println(i + " member objects created.");
             }
             System.out.println("DEBUG: returned list containing " + members.size());
             return members;
         }
     }
     
     /**
      * Gets a member based on the DTU Licence number
      * @param DTUNo the DTU Licence number
      * @return the member with the corresponding licence number
      * @throws SQLServerException if there's an error connecting to the database
      * @throws SQLException if there's an error in the SQL query execution
      */
     public Member getByDTU(int DTUNo) throws SQLServerException, SQLException
     {
         try (Connection con = connector.getConnection())
         {
             String sql = "SELECT * FROM Member WHERE DTUNo = ?";
             PreparedStatement ps = con.prepareStatement(sql);
             ps.setInt(1, DTUNo);
 
             ResultSet rs = ps.executeQuery();
             if (rs.next())
             {
                 return getOneMember(rs);
             }
             return null;
         }
     }
     
     /**
      * Updates a member in the database
      * @param m the member to update
      * @throws SQLServerException if there's an error connecting to the database
      * @throws SQLException if there's an error in the SQL query execution
      */
     public void update(Member m) throws SQLServerException, SQLException
     {
         try (Connection con = connector.getConnection())
         {
             String sql = "UPDATE Member "
                    + "SET First_Name = ?, Last_Name = ?, Address = ?, Birth_Year = ?, Phone_No = ?, Email = ?, MemberType = ?, WHERE Id = ?";
             PreparedStatement ps = con.prepareStatement(sql);
             ps.setString(1, m.getFirstName());
             ps.setString(2, m.getLastName());
             ps.setString(3, m.getAddress());
             ps.setInt(4, m.getBirthYear());
             ps.setInt(5, m.getPhoneNo());
             ps.setString(6, m.getEmail());
             ps.setString(7, m.getMemberType());
             ps.setInt(8, m.getMemberNo());
             
             int affectedRows = ps.executeUpdate();
             if (affectedRows < 1)
             {
                 throw new SQLException("Unable to update member");
             }
         }
     }
     
     /**
      * Deletes a member from the database
      * @param id the id of the member to delete
      * @throws SQLServerException if there's an error connecting to the database
      * @throws SQLException if there's an error in the SQL query execution
      */
     public void delete(int id) throws SQLServerException, SQLException
     {
         try (Connection con = connector.getConnection())
         {
             String sql = "DELETE FROM Member WHERE Id = ?";
             PreparedStatement ps = con.prepareStatement(sql);
             ps.setInt(1, id);
             
             int affectedRows = ps.executeUpdate();
             if (affectedRows < 1)
             {
                 throw new SQLException("Unable to delete member");
             }
         }
     }
     
     public void add(Member m) throws SQLException, SQLServerException
     {
         System.out.println("DEBUG: running debugAdd!");
         try (Connection con = connector.getConnection())
         {
             String sql = "INSERT INTO Member (First_Name, Last_Name, Address, Birth_Year, Phone_No, Email, MemberType) "
                     + "VALUES (?,?,?,?,?,?,?)";
             PreparedStatement ps = con.prepareStatement(sql);
             ps.setString(1, m.getFirstName());
             ps.setString(2, m.getLastName());
             ps.setString(3, m.getAddress());
             ps.setInt(4, m.getBirthYear());
             ps.setInt(5, m.getPhoneNo());
             ps.setString(6, m.getEmail());
             ps.setString(7,m.getMemberType());
             int affectedRows = ps.executeUpdate();
             if (affectedRows < 1)
             {
                 throw new SQLException("Unable to update member :( ");
             }
         }
     }
     
     
     private Member getOneMember(ResultSet rs) throws SQLException
     {
         int memberNo = rs.getInt(1);
         String firstName = rs.getString(2);
         String lastName = rs.getString(3);
         String address = rs.getString(4);
         int birthYear = rs.getInt(5);
         int phoneNo = rs.getInt(6);
         String email = rs.getString(7);
         String memberType = rs.getString(9);
 //        int DTULicence = rs.getInt();
 //        int DTUPoints = rs.getInt(10);
         
         return Member.fromDataBase(memberNo, lastName, firstName, address, birthYear, phoneNo, email, memberType, 0, 0);
     }
 }
