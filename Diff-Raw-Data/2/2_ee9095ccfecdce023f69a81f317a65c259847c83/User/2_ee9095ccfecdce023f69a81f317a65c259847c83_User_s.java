 package connectivity;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Florentijn Cornet
  */
 public class User {
 
     // Variable that sets the maximum ammount of incorrect login attempts
     public final int MAX_INCORRECT_LOGINS = 3;
 
     private final DbManager db = new DbManager();
 
     private int userId;
     private int permissionId;
     private int incorrectLogin;
     private String username;
     private String firstName;
     private String lastName;
     private String password;
     private boolean isLoggedIn = false;
 
     public User() {
         db.openConnection();
     }
 
     // Constructor for the user object (used with the arraylist that is created 
     // by the search method
     public User(int userId, String firstName, String lastName, String username,
             int permissionId, int incorrectLogin) {
         this.userId = userId;
         this.firstName = firstName;
         this.lastName = lastName;
         this.username = username;
         this.permissionId = permissionId;
         this.incorrectLogin = incorrectLogin;
     }
 
     // determines whether a user is logging in with the correct information
     public String login(String tfUsername, String tfPasswd) {
         this.getUserData(tfUsername);
         if (this.username.equals(tfUsername)) {
            if (BCrypt.checkpw(tfUsername, this.getPassword())) {
                 this.setIsLoggedIn(true);
                 return "Login success";
             } else {
                 return "Password is incorrect";
             }
         } else {
             return "Username doesn't exist";
         }
     }
 
     public String getFirstName() {
         return this.firstName;
     }
 
     public String getLastName() {
         return this.lastName;
     }
 
     public String getUsername() {
         return this.username;
     }
 
     public int getUserId() {
         return this.userId;
     }
 
     public int getPermissionId() {
         return this.permissionId;
     }
 
     public int getIncorrectLogin() {
         return this.incorrectLogin;
     }
 
     // Checks whether the old password is correct (USED IN COMBINATION WITH updatePassword METHOD)
     public boolean checkOldPassword(String oldPassword, String storedUsername) {
         this.getUserData(storedUsername);
         return this.getPassword().equals(oldPassword);
     }
 
     // Determines whether a user's account has been locked
     public boolean getLockState() {
         return this.incorrectLogin >= MAX_INCORRECT_LOGINS;
     }
 
     // Gets all data of one selected user
     public void getUserData(String tfUsername) {
         try {
             String sql = "SELECT *, COUNT(*) as `rows` FROM `user` WHERE `username`='" + tfUsername + "'";
             ResultSet result = db.doQuery(sql);
             if (result.next()) {
                 if (result.getInt("rows") >= 1) {
                     this.setUsername(result.getString("username"));
                     this.setUserId(result.getInt("user_id"));
                     this.setFirstName(result.getString("first_name"));
                     this.setLastName(result.getString("last_name"));
                     this.setPermissionId(result.getInt("permission_id"));
                     this.setPassword(result.getString("password"));
                     this.setIncorrectLogin(result.getInt("incorrect_login"));
                 } else {
                     this.setUsername("INVALID");
                 }
             }
         } catch (SQLException e) {
             System.out.println(db.SQL_EXCEPTION + e.getMessage());
         }
     }
 
     // Used to create a new user, User ID is auto increment
     public void setNewUser(String tfUsername, String tfFirstName, String tfLastName, String tfPassword, int inputPermissionId) {
         tfPassword = BCrypt.hashpw(tfPassword, BCrypt.gensalt());
         String sql = "INSERT INTO `user` (username, first_name, last_name, password, permission_id, incorrect_login) VALUES ('"
                 + tfUsername + "', '" + tfFirstName + "', '" + tfLastName
                 + "', '" + tfPassword + "', " + inputPermissionId + ", 0)";
         db.insertQuery(sql);
     }
 
     // Deletes user in database
     public void deleteUser(String tfUsername) {
         String sql = "DELETE FROM `user` WHERE `username` = '" + tfUsername + "'";
         db.insertQuery(sql);
     }
 
     // User to change desired user information (STRINGS)
     public void changeUserStringData(String inputUsername, String dbField, String newValue) {
         String sql = "UPDATE `user` SET `" + dbField + "` = '" + newValue + "' WHERE `username` = '" + inputUsername + "'";
         db.insertQuery(sql);
     }
 
     // Used to change desired user information (INTEGERS)
     public void changeUserIntData(String inputUsername, String dbField, int newValue) {
         String sql = "UPDATE `user` SET `" + dbField + "` = '" + newValue + "' WHERE `username` = '" + inputUsername + "'";
         db.insertQuery(sql);
     }
 
     // Increases incorrect login count by one on incorrect login attempt
     public void setIncorrectLogin() {
         String sql = "UPDATE `user` SET `incorrect_login` = `incorrect_login` + 1 WHERE `user_id` = '" + this.userId + "'";
         db.insertQuery(sql);
         System.out.println(incorrectLogin + 1);
     }
 
     // Sets incorrect login count to 0
     public void resetIncorrectLogin() {
         String sql = "UPDATE `user` SET `incorrect_login` = 0 WHERE `user_id` = '" + this.userId + "'";
         db.insertQuery(sql);
     }
 
     // Password update method
     public void updatePassword(String tfPassword, String tfUsername) {
         String sql = "UPDATE `user` SET `password` = '" + tfPassword + "' WHERE `username`='" + tfUsername + "'";
         db.insertQuery(sql);
     }
 
     // Method for filling jTable and searching database
     public List<User> searchUserList(int dbField, String searchArg) {
         List<User> users = new ArrayList<>();
         String sql, sqlSelect = "SELECT * FROM `user`";
 
         // Statement for searching all collumns
         if (dbField == 0) {
             sql = sqlSelect + " WHERE `user_id` LIKE '%" + searchArg + "%'"
                     + "OR `last_name` LIKE '%" + searchArg + "%'"
                     + "OR `first_name` LIKE '%" + searchArg + "%'"
                     + "OR `username` LIKE '%" + searchArg + "%'"
                     + "OR `permission_id` LIKE '%" + searchArg + "%'";
         } // Statement for searching userId collumns
         else if (dbField == 1) {
             sql = sqlSelect + " WHERE `user_id` LIKE '%" + searchArg + "%'";
         } // firstName collumns
         else if (dbField == 2) {
             sql = sqlSelect + " WHERE `first_name` LIKE '%" + searchArg + "%'";
         } // lastName collumns
         else if (dbField == 3) {
             sql = sqlSelect + " WHERE `last_name` LIKE '%" + searchArg + "%'";
         } // username collumns
         else if (dbField == 4) {
             sql = sqlSelect + " WHERE `username` LIKE '%" + searchArg + "%'";
         } // permissionId collumns
         else if (dbField == 5) {
             sql = sqlSelect + " WHERE `permission_id` LIKE '%" + searchArg + "%'";
         } // Else statement is used to fill the table with all users
         else {
             sql = sqlSelect;
         }
 //        System.out.println(sql);
         try {
             ResultSet result = db.doQuery(sql);
             while (result.next()) {
                 users.add(new User(result.getInt("user_id"),
                         result.getString("first_name"),
                         result.getString("last_name"),
                         result.getString("username"),
                         result.getInt("permission_id"),
                         result.getInt("incorrect_login")));
             }
         } catch (SQLException e) {
             System.out.println(db.SQL_EXCEPTION + e.getMessage());
         }
         return users;
     }
 
     /**
      * @param userId the userId to set
      */
     public void setUserId(int userId) {
         this.userId = userId;
     }
 
     /**
      * @param permissionId the permissionId to set
      */
     public void setPermissionId(int permissionId) {
         this.permissionId = permissionId;
     }
 
     /**
      * @param incorrectLogin the incorrectLogin to set
      */
     public void setIncorrectLogin(int incorrectLogin) {
         this.incorrectLogin = incorrectLogin;
     }
 
     /**
      * @param username the username to set
      */
     public void setUsername(String username) {
         this.username = username;
     }
 
     /**
      * @param firstName the firstName to set
      */
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     /**
      * @param lastName the lastName to set
      */
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     /**
      * @return the password
      */
     public String getPassword() {
         return password;
     }
 
     /**
      * @param password the password to set
      */
     public void setPassword(String password) {
         this.password = password;
     }
 
     /**
      * @return the isLoggedIn
      */
     public boolean isIsLoggedIn() {
         return isLoggedIn;
     }
 
     /**
      * @param isLoggedIn the isLoggedIn to set
      */
     public void setIsLoggedIn(boolean isLoggedIn) {
         this.isLoggedIn = isLoggedIn;
     }
 }
