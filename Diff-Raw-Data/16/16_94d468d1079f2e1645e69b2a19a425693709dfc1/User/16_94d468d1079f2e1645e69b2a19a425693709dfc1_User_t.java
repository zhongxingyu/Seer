 package puf.m2.hms.model;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import puf.m2.hms.db.Database;
 import puf.m2.hms.db.DatabaseImpl;
 
 public class User {
     
     private static Database db = DatabaseImpl.defaultDb;
     
     private String username;
     private String password;
     private String useremail;
     private String usertypeCode;
 
     public boolean login(String username, String password) {
 
         try {
             db.createConnection();
             db.createStatement();
             ResultSet rs = db.getResultSet("SELECT username FROM User WHERE username='"
                     + username + "' " + "and password = '" + password + "'");
 
             db.closeConnection();
             // if (rs==null) then login unsucessful
            while (rs.next())
            	return true;
         } catch (SQLException e) {
             e.printStackTrace();
         }
        return false;
     }
     
     public String getUsername() {
         return username;
     }
     public void setUsername(String username) {
         this.username = username;
     }
     public String getPassword() {
         return password;
     }
     public void setPassword(String password) {
         this.password = password;
     }
     public String getUseremail() {
         return useremail;
     }
     public void setUseremail(String useremail) {
         this.useremail = useremail;
     }
     public String getUsertypeCode() {
         return usertypeCode;
     }
     public void setUsertypeCode(String usertypeCode) {
         this.usertypeCode = usertypeCode;
     }
 
 }
