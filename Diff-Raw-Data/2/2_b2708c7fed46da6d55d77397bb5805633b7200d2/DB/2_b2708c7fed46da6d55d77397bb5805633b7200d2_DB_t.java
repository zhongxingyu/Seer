 package utils;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.PreparedStatement;
 import java.sql.Statement;
 
 import models.User;
 
 public class DB {
 
     private Connection con = null;
     private static DB instance = null;
 
     private final String dbConn = "jdbc:mysql://localhost:3306/friendtrackerdb";
 
     private DB() throws SQLException {
         con = DriverManager.getConnection(dbConn, "root", "FriendTracker");
     }
 
     public static DB getInstance() throws SQLException {
         if (instance == null) {
             instance = new DB();
         }
         return instance;
     }
 
     private Connection getConnection() throws SQLException {
         if (con == null) {
             con = DriverManager.getConnection(dbConn);
         }
         return con;
     }
 
     public boolean insertUser(User user) throws SQLException {
         String query = "replace into user(ppId,x,y) values (?,?,?)";
         PreparedStatement st = getConnection().prepareStatement(query);
         st.setString(1, user.getppId());
         st.setDouble(2, user.getX());
         st.setDouble(3, user.getY());
        st.setInt(4, user.getVisiblity());
         st.executeUpdate();
 
         return true;
     }
 
     public User getUser(String ppId) throws SQLException {
         String query = "select * from user where ppId = ?";
         PreparedStatement st = getConnection().prepareStatement(query);
         st.setString(1, ppId);
         ResultSet rs = st.executeQuery();
         if (rs.next()) {
             User user = new User(ppId, rs.getDouble(3), rs.getDouble(4), rs.getInt(5));
             return user;
         } else {
             return null;
         }
     }
 }
