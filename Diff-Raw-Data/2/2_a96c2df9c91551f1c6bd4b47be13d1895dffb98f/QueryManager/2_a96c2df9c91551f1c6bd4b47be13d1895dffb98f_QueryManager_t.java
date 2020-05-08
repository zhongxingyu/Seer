 package connectivity;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import main.Session;
 import model.*;
 
 /**
  *
  * @author Niels Reijn
  */
 public class QueryManager {
 
     private final DatabaseManager db = new DatabaseManager();
 
     private int permissionId, incorrectLogin, userId;
     private String username, firstName, lastName, password;
     private boolean isLoggedIn = false;
     public final int MAX_INCORRECT_LOGINS = 3;
     public PreparedStatement preparedStatement = null;
 
 
     /**
      * Compares username and password to database entries, denies or grants
      * access depending on the result.
      *
      * @param tfUsername username to be checked by this method.
      * @param tfPassword password to match the username.
      * @return String returns specific string depending on the result of the
      * method.
      */
     public String login(String tfUsername, String tfPassword) {
         User user = getUserData(tfUsername);
         if (user.getUsername().equals(tfUsername)) {
             if (BCrypt.checkpw(tfPassword, user.getPassword())) {
                 user.setIsLoggedIn(true);
                 return "Login success";
             } else {
                 return "Password is incorrect";
             }
         } else {
             return "Username doesn't exist";
         }
     }
 
     /**
      * Checks whether the old password is correct (USED IN COMBINATION WITH
      * updatePassword METHOD)
      *
      * @param oldPassword 'old' password to be checked.
      * @param storedUsername Username data pulled from current session.
      * @return
      */
     public boolean checkOldPassword(String oldPassword, String storedUsername) {
         User user = getUserData(storedUsername);
         return BCrypt.checkpw(oldPassword, user.getPassword());
     }
 
     /**
      * Determines whether a user's account has been locked.
      *
      * @return boolean returns true if the amount of incorrect logins has
      * exceeded the maximum allowed amount.
      */
     public boolean getLockState() {
         return this.incorrectLogin >= MAX_INCORRECT_LOGINS;
     }
 
     /**
      * Method that pulls data entries for one specific user.
      *
      * @param username String parameter to determine which user data is pulled
      * from the database.
      * @return users
      */
     public User getUserData(String username) {
         User user = new User();
         try {
             db.openConnection();
             String sql = "SELECT *, COUNT(*) as `rows` FROM `user` WHERE `username`='" + username + "'";
             System.out.println(username);
             ResultSet result = db.doQuery(sql);
             if (result.next()) {
                 if (result.getInt("rows") >= 1) {
                     user.setUserId(result.getInt("user_id"));
                     user.setUsername(result.getString("username"));
                     user.setFirstName(result.getString("first_name"));
                     user.setLastName(result.getString("last_name"));
                     user.setPermissionId(result.getInt("permission_id"));
                     user.setPassword(result.getString("password"));
                     user.setIncorrectLogin(result.getInt("incorrect_login"));
                 } else {
                     user.setUsername("INVALID");
                 }
             }
         } catch (SQLException e) {
             System.out.println(db.SQL_EXCEPTION + e.getMessage());
         }
         finally
         {
             db.closeConnection();
         }
         return user;
     }
 
     /**
      * Method that pulls data entries for one specific user.
      *
      * @param userId integer parameter to determine which user data is pulled
      * from the database.
      */
     public User getUserDataInt(int userId) {
         User user = new User();
         try {
             db.openConnection();
             String sql = "SELECT *, COUNT(*) as `rows` FROM `user` WHERE `user_id`='" + userId + "'";
             ResultSet result = db.doQuery(sql);
             if (result.next()) {
                 if (result.getInt("rows") >= 1) {
                     user.setUserId(result.getInt("user_id"));
                     user.setUsername(result.getString("username"));
                     user.setFirstName(result.getString("first_name"));
                     user.setLastName(result.getString("last_name"));
                     user.setPermissionId(result.getInt("permission_id"));
                     user.setPassword(result.getString("password"));
                     user.setIncorrectLogin(result.getInt("incorrect_login"));
                 } else {
                     user.setUsername("INVALID");
                 }
             }
         } catch (SQLException e) {
             System.out.println(db.SQL_EXCEPTION + e.getMessage());
         }
         finally
         {
             db.closeConnection();
         }
         return user;
     }
 
     /**
      * Method used to create and insert a new user into the database.
      *
      * @param tfUsername parameter to specify the new username.
      * @param tfFirstName parameter to specify the new user's first name.
      * @param tfLastName parameter to specify the new user's last name.
      * @param tfPassword parameter to specify the new user's password.
      * @param inputPermissionId parameter to specify the role of the new user 1
      * = Employee, 2 = Manager, 3 = Administrator.
      */
     public void createUser(String tfUsername, String tfFirstName, String tfLastName,
             String tfPassword, int inputPermissionId) {
         try {
             db.openConnection();
             tfPassword = BCrypt.hashpw(tfPassword, BCrypt.gensalt());
             preparedStatement = db.connection.prepareStatement("INSERT INTO"
                     + "`user`(`permission_id`,"
                     + "`username`, `first_name`, `last_name`, `password`)"
                     + "VALUES (?,?,?,?,?)");
             preparedStatement.setInt(1, inputPermissionId);
             preparedStatement.setString(2, tfUsername);
             preparedStatement.setString(3, tfFirstName);
             preparedStatement.setString(4, tfLastName);
             preparedStatement.setString(5, tfPassword);
             preparedStatement.executeUpdate();
         } catch (SQLException ex) {
             Logger.getLogger(QueryManager.class.getName()).log(Level.SEVERE, null, ex);
         }
           finally
         {
             db.closeConnection();
         }
     }
 
     /**
      * Method used to update already existing user data.
      *
      * @param username parameter to specify the new username.
      * @param firstName parameter to specify the new first name.
      * @param lastName parameter to specify the new last name.
      * @param permissionId parameter to specify the new role. 1 = Employee, 2 =
      * Manager, 3 = Administrator.
      */
     public void updateUser(String username, String firstName, String lastName,
             int permissionId) {
         try {
             db.openConnection();
             
             String updateSQL = "UPDATE `user` SET `first_name` = ?, `last_name` = ?,`permission_id` = ? WHERE `username` = ?";
                        
             preparedStatement = db.connection.prepareStatement(updateSQL);
             preparedStatement.setString(1, firstName);
             preparedStatement.setString(2, lastName);
             preparedStatement.setInt(3, permissionId);
             preparedStatement.setString(4, username);          
            
             preparedStatement.executeUpdate();
         } catch (SQLException ex) {
             Logger.getLogger(QueryManager.class.getName()).log(Level.SEVERE, null, ex);
         }
           finally
         {
             db.closeConnection();
         }
     }
 
     /**
      * Method called to remove user database entry.
      *
      * @param tfUsername parameter to specify which user entry should be
      * removed.
      */
     public void deleteUser(String tfUsername) {
         try {
             db.openConnection();
             preparedStatement = db.connection.prepareStatement("DELETE FROM `user` WHERE `username` = ?");
             preparedStatement.setString(1, tfUsername);
             preparedStatement.executeUpdate();
         } catch (SQLException ex) {
             Logger.getLogger(QueryManager.class.getName()).log(Level.SEVERE, null, ex);
         }
           finally
         {
             db.closeConnection();
         }
     }
 
     /**
      * Method that checks whether or not a specific username already exists or
      * not.
      *
      * @param username username to check.
      * @return true if username already exists.
      */
     public boolean checkUsernameInUse(String username) {
         boolean usernameInUse = true;
         try {
             db.openConnection();
             String sql = "SELECT * FROM `user` WHERE `username` LIKE '%" + username + "%'";
             ResultSet result = db.doQuery(sql);
 
             if (result.next()) {
                 usernameInUse = true;
             } else {
                 usernameInUse = false;
             }
         } catch (SQLException e) {
             System.out.println(db.SQL_EXCEPTION + e.getMessage());
         }
           finally
         {
             db.closeConnection();
         }
 
         return usernameInUse;
     }
 
     /**
      * Method that changes specific user data depending on the dbField
      * parameter.
      *
      * @param username parameter to specify user to be altered.
      * @param dbField parameter that specifies the field to be altered.
      * @param newValue String value to be inputted into the specific database
      * field.
      */
     public void unlockUser(String username, String newValue) {
         try {
             db.openConnection();
             preparedStatement = db.connection.prepareStatement("UPDATE `user` SET `incorrect_login` = ? WHERE `username` = ?");
             preparedStatement.setString(1, newValue);
             preparedStatement.setString(2, username);
             preparedStatement.executeUpdate();
         } catch (SQLException ex) {
             Logger.getLogger(QueryManager.class.getName()).log(Level.SEVERE, null, ex);
         }
           finally
         {
             db.closeConnection();
         }
     }
 
     /**
      * Method that changes specific user data depending on the dbField
      * parameter.
      *
      * @param username parameter to specify user to be altered.
      * @param newValue Integer value to be inputted into the specific database
      * field.
      */
     public void lockUserAccount(String username, int newValue) {
         try {
             db.openConnection();
             preparedStatement = db.connection.prepareStatement("UPDATE `user` SET `incorrect_login` = ? WHERE `username` = ?");
             preparedStatement.setInt(1, newValue);
             preparedStatement.setString(2, username);
             preparedStatement.executeUpdate();
         } catch (SQLException ex) {
             Logger.getLogger(QueryManager.class.getName()).log(Level.SEVERE, null, ex);
         }
           finally
         {
             db.closeConnection();
         }
     }
 
     /**
      * Increases incorrect login count by one on incorrect login attempt.
      */
     public void setIncorrectLogin() {
         
         try {
             db.openConnection();
             preparedStatement = db.connection.prepareStatement("UPDATE `user` SET `incorrect_login` = `incorrect_login`"
                     + "+ 1 WHERE `username` = ?");
             preparedStatement.setString(1, Session.storedUsername);
             preparedStatement.executeUpdate();
         } catch (SQLException ex) {
             Logger.getLogger(QueryManager.class.getName()).log(Level.SEVERE, null, ex);
         }
           finally
         {
             db.closeConnection();
         }
     }
 
     /**
      * Sets incorrect login count to 0.
      */
     public void resetIncorrectLogin() {
         try {
             db.openConnection();
             preparedStatement = db.connection.prepareStatement("UPDATE `user` SET `incorrect_login` = 0 WHERE `username` = ?");
             preparedStatement.setString(1, Session.storedUsername);
             preparedStatement.executeUpdate();
         } catch (SQLException ex) {
             Logger.getLogger(QueryManager.class.getName()).log(Level.SEVERE, null, ex);
         }
           finally
         {
             db.closeConnection();
         }
     }
 
     /**
      * Method used to update a specific user's password.
      *
      * @param tfPassword parameter that determines the new password.
      * @param tfUsername parameter to specify the user to be altered.
      */
     public void updatePassword(String tfPassword, String tfUsername) {
         tfPassword = BCrypt.hashpw(tfPassword, BCrypt.gensalt());
         try {
             db.openConnection();
             preparedStatement = db.connection.prepareStatement("UPDATE `user` SET `password` = ? WHERE `username` = ?");
             preparedStatement.setString(1, tfPassword);
             preparedStatement.setString(2, tfUsername);
             preparedStatement.executeUpdate();
         } catch (SQLException ex) {
             Logger.getLogger(QueryManager.class.getName()).log(Level.SEVERE, null, ex);
         }
           finally
         {
             db.closeConnection();
         }
     }
 
     /**
      * Method for filling jTable and searching database
      *
      * @param dbField parameter to specify which field to search in, if this
      * parameter is set to 0 this method will search in all fields.
      * @param searchArg argument used to search in the database.
      * @return users that match the search argument.
      */
     public List<User> searchUserList(int dbField, String searchArg) {
         
         List<User> users = new ArrayList<>();
         String sql, sqlSelect = "SELECT * FROM `user`";
 
         // Statement for searching all collumns
         if (dbField == 0) {
             sql = sqlSelect + " WHERE `last_name` LIKE '%" + searchArg + "%'"
                     + "OR `first_name` LIKE '%" + searchArg + "%'"
                     + "OR `username` LIKE '%" + searchArg + "%'"
                     + "OR `permission_id` LIKE '%" + searchArg + "%'";
         } // firstName collumns
         else if (dbField == 1) {
             sql = sqlSelect + " WHERE `first_name` LIKE '%" + searchArg + "%'";
         } // lastName collumns
         else if (dbField == 2) {
             sql = sqlSelect + " WHERE `last_name` LIKE '%" + searchArg + "%'";
         } // username collumns
         else if (dbField == 3) {
             sql = sqlSelect + " WHERE `username` LIKE '%" + searchArg + "%'";
         } // permissionId collumns
         else if (dbField == 4) {
             sql = sqlSelect + " WHERE `permission_id` LIKE '%" + searchArg + "%'";
         } // Else statement is used to fill the table with all users
         else {
             sql = sqlSelect;
         }
 
         try {
             db.openConnection();
             ResultSet result = db.doQuery(sql);
             while (result.next()) {
                 users.add(new User(result.getString("first_name"),
                         result.getString("last_name"),
                         result.getString("username"),
                         result.getInt("permission_id"),
                         result.getInt("incorrect_login"),
                         result.getInt("user_id")));
             }
         } catch (SQLException e) {
             System.out.println(db.SQL_EXCEPTION + e.getMessage());
         }
           finally
         {
             db.closeConnection();
         }
         return users;
     }
 
     /**
      * Gets customer data for one specific user
      *
      * @param tfInput the value databaseVariable has to be.
      * @param databaseVariable the column in the table that will be searched in.
      */
     public Customer getCustomerData(String tfInput, String databaseVariable) {
         Customer tempCustomer = new Customer();
         try {
             db.openConnection();
             String sql = "SELECT *, COUNT(*) as `rows` FROM `customer` WHERE `"
                     + databaseVariable + "`='" + tfInput + "'";
             ResultSet result = tempCustomer.getDb().doQuery(sql);
             if (result.next()) {
                 if (result.getInt("rows") >= 1) {
 //                    user.setUserId(result.getInt("user_id"));
                     tempCustomer.setEmail(result.getString("email"));
                     tempCustomer.setCustomerId(result.getInt("customer_id"));
                     tempCustomer.setFirstName(result.getString("first_name"));
                     tempCustomer.setLastName(result.getString("last_name"));
                     tempCustomer.setPostalCode(result.getString("postal_code"));
                     tempCustomer.setPhoneHome(result.getString("phone_home"));
                     tempCustomer.setAddress(result.getString("address"));
                     tempCustomer.setPhoneMobile(result.getString("phone_mobile"));
                     tempCustomer.setCity(result.getString("city"));
                     tempCustomer.setCountry(result.getString("country"));
                     tempCustomer.setDateChanged(result.getString("date_changed"));
                     tempCustomer.setLastChangedBy(result.getInt("last_changed_by"));
                     tempCustomer.setResortId(result.getInt("resort_id"));
                 } else {
                     System.out.println("SOMETHING WENT WRONG");
                 }
             }
         } catch (SQLException e) {
             System.out.println(tempCustomer.getDb().SQL_EXCEPTION + e.getMessage());
         }
           finally
         {
             db.closeConnection();
         }
         return tempCustomer;
     }
 
     /**
      * Used to populate jTables and search database for customers
      *
      * @param dbField can be 0-10, specifies different columns.
      * @param searchArg the data that will be searched for.
      * @return
      */
     public List<Customer> searchCustomerList(int dbField, String searchArg) {
         List<Customer> customers = new ArrayList<>();
         Customer customer = new Customer();
         String sql, sqlSelect = "SELECT * FROM `customer`";
 
         // Statement for searching all collumns
         if (dbField == 0) {
             sql = sqlSelect + " WHERE `customer_id` LIKE '%" + searchArg + "%'"
                     + "OR `first_name` LIKE '%" + searchArg + "%'"
                     + "OR `last_name` LIKE '%" + searchArg + "%'"
                     + "OR `address` LIKE '%" + searchArg + "%'"
                     + "OR `postal_code` LIKE '%" + searchArg + "%'"
                     + "OR `city` LIKE '%" + searchArg + "%'"
                     + "OR `country` LIKE '%" + searchArg + "%'"
                     + "OR `email` LIKE '%" + searchArg + "%'"
                     + "OR `phone_home` LIKE '%" + searchArg + "%'"
                     + "OR `phone_mobile` LIKE '%" + searchArg + "%'";
 
         } // for searching customerId
         else if (dbField == 1) {
             sql = sqlSelect + " WHERE `customer_id` LIKE '%" + searchArg + "%'";
         } // firstName
         else if (dbField == 2) {
             sql = sqlSelect + " WHERE `first_name` LIKE '%" + searchArg + "%'";
         } //lastName
         else if (dbField == 3) {
             sql = sqlSelect + " WHERE `last_name` LIKE '%" + searchArg + "%'";
         } // address
         else if (dbField == 4) {
             sql = sqlSelect + " WHERE `address` LIKE '%" + searchArg + "%'";
         } // postalCode
         else if (dbField == 5) {
             sql = sqlSelect + " WHERE `postal_code` LIKE '%" + searchArg + "%'";
         } // city
         else if (dbField == 6) {
             sql = sqlSelect + " WHERE `city` LIKE '%" + searchArg + "%'";
         } // country
         else if (dbField == 7) {
             sql = sqlSelect + " WHERE `country` LIKE '%" + searchArg + "%'";
         } // email
         else if (dbField == 8) {
             sql = sqlSelect + " WHERE `email` LIKE '%" + searchArg + "%'";
         } // phoneHome
         else if (dbField == 9) {
             sql = sqlSelect + " WHERE `phone_home` LIKE '%" + searchArg + "%'";
         } // phoneMobile
         else if (dbField == 10) {
             sql = sqlSelect + " WHERE `phone_mobile` LIKE '%" + searchArg + "%'";
         } // Else statement is used to fill the table with all users
         else {
             sql = sqlSelect;
         }
 
         try {
             db.openConnection();
             ResultSet result = customer.getDb().doQuery(sql);
             while (result.next()) {
                 customers.add(new Customer(result.getInt("customer_id"),
                         result.getString("first_name"),
                         result.getString("last_name"),
                         result.getString("address"),
                         result.getString("postal_code"),
                         result.getString("city"),
                         result.getString("country"),
                         result.getString("email"),
                         result.getString("phone_home"),
                         result.getString("phone_mobile"),
                         result.getString("date_changed"),
                         result.getInt("resort_id"),
                         result.getInt("last_changed_by")));
             }
         } catch (SQLException e) {
             System.out.println(customer.getDb().SQL_EXCEPTION + e.getMessage());
         }
           finally
         {
             db.closeConnection();
         }
         return customers;
     }
 
     /**
      * Creates a new customer. All parameters below are attributes of the
      * customer.
      *
      * @param tfFirstName
      * @param tfLastName
      * @param tfAddress
      * @param tfPostalCode
      * @param tfCity
      * @param tfCountry
      * @param tfEmail
      * @param tfPhoneHome
      * @param tfPhoneMobile
      */
     public void setNewCustomer(String tfFirstName, String tfLastName,
             String tfAddress, String tfPostalCode, String tfCity, String tfCountry,
             String tfEmail, String tfPhoneHome, String tfPhoneMobile) {
         try {
              db.openConnection();
             preparedStatement = db.connection.prepareStatement("INSERT INTO `customer` (first_name, last_name, address,"
                     + "postal_code, city, country, email, phone_home, phone_mobile,"
                     + "date_changed, last_changed_by)"
                     + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)");
             preparedStatement.setString(1, tfFirstName);
             preparedStatement.setString(2, tfLastName);
             preparedStatement.setString(3, tfAddress);
             preparedStatement.setString(4, tfPostalCode);
             preparedStatement.setString(5, tfCity);
             preparedStatement.setString(6, tfCountry);
             preparedStatement.setString(7, tfEmail);
             preparedStatement.setString(8, tfPhoneHome);
             preparedStatement.setString(9, tfPhoneMobile);
             preparedStatement.setInt(10, Session.storedUserId);
             preparedStatement.executeUpdate();
             db.closeConnection();
         } catch (SQLException ex) {
             Logger.getLogger(QueryManager.class.getName()).log(Level.SEVERE, null, ex);
         }
           finally
         {
             db.closeConnection();
         }
     }
 
     /**
      * Deletes a customer with CustomerID.
      *
      * @param tfCustomerId id of the customer that will be deleted.
      */
     public void deleteCustomer(String tfCustomerId) {
         try {
             db.openConnection();
             preparedStatement = db.connection.prepareStatement("DELETE FROM `customer` WHERE `customer_id` = ?");
             preparedStatement.setString(1, tfCustomerId);
             preparedStatement.executeUpdate();
 
         } catch (SQLException ex) {
             Logger.getLogger(QueryManager.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             db.closeConnection();
         }
     }
 
     /**
      * Updates the following attributes of a customer.
      *
      * @param firstName
      * @param lastName
      * @param address
      * @param postalCode
      * @param city
      * @param country
      * @param email
      * @param phoneHome
      * @param phoneMobile
      */
     public void updateCustomer(String firstName, String lastName, String address,
             String postalCode, String city, String country, String email,
             String phoneHome, String phoneMobile) {
         try {
             db.openConnection();
             preparedStatement = db.connection.prepareStatement("UPDATE `customer` SET `first_name` = ?, `last_name` = ?"
                     + ", `address` = ?, `postal_code` = ?, `city` = ?, `country` = ?, `email` = ?, `phone_home` = ?, `last_changed_by` = ?, `phone_mobile` = ?, `date_changed` = CURRENT_TIMESTAMP WHERE `customer_id` = ?");
             preparedStatement.setString(1, firstName);
             preparedStatement.setString(2, lastName);
             preparedStatement.setString(3, address);
             preparedStatement.setString(4, postalCode);
             preparedStatement.setString(5, city);
             preparedStatement.setString(6, country);
             preparedStatement.setString(7, email);
             preparedStatement.setString(8, phoneHome);
             preparedStatement.setInt(9, Session.storedUserId);
             preparedStatement.setString(10, phoneMobile);
             preparedStatement.setString(11, Session.storedCustomerId);
             preparedStatement.executeUpdate();
         } catch (SQLException ex) {
             Logger.getLogger(QueryManager.class.getName()).log(Level.SEVERE, null, ex);
         }
           finally
         {
             db.closeConnection();
         }
     }
 
     /**
      * Get all Luggage data from DB.
      *
      * @param tfInput
      * @param databaseVariable
      */
     public Luggage getLuggageData(String tfInput, String databaseVariable) {
         Luggage tempLuggage = new Luggage();
         try {
             db.openConnection();
             String sql = "SELECT *, COUNT(*) as `rows` FROM `luggage` WHERE `"
                     + databaseVariable + "`='" + tfInput + "'";
             ResultSet result = tempLuggage.getDb().doQuery(sql);
             if (result.next()) {
                 if (result.getInt("rows") >= 0) {
                     tempLuggage.setLuggageId(result.getInt("luggage_id"));
                     tempLuggage.setCustomerId(result.getInt("customer_id"));
                     tempLuggage.setDescription(result.getString("description"));
                     tempLuggage.setLocation(result.getString("location"));
                     tempLuggage.setDateLost(result.getString("date_lost"));
                     tempLuggage.setStatus(result.getInt("status"));
                     tempLuggage.setDateChanged(result.getString("date_changed"));
                     tempLuggage.setDateHandled(result.getString("date_handled"));
                     tempLuggage.setDateFound(result.getString("date_found"));
                     tempLuggage.setLastChangedBy(result.getInt("last_changed_by"));
                 } else {
                     System.out.println("SOMETHING WENT WRONG");
                 }
             }
         } catch (SQLException e) {
             System.out.println(tempLuggage.getDb().SQL_EXCEPTION + e.getMessage());
         }
           finally
         {
             db.closeConnection();
         }
         return tempLuggage;
     }
 
     /**
      * Used to populate jTables and search database for Luggage.
      *
      * @param dbField the row given by a textfield
      * @param searchArg the search parameter.
      * @param handled searchs for handled items if 1 else it searches for 0.
      * @return a list of items.
      */
     public List<Luggage> searchLuggageList(int dbField, String searchArg, int handled) {
         List<Luggage> luggages = new ArrayList<>();
         Luggage tempLuggage = new Luggage();
         String showHandled, sql, sqlSelect = "SELECT * FROM `luggage`";
 
         if (handled == 1) {
             showHandled = " AND `status` = 3";
         } else {
             showHandled = "";
         }
 
         // Statement for searching all collumns
         if (dbField == 0) {
             sql = sqlSelect + " WHERE `luggage_id` LIKE '%" + searchArg + "%'" + showHandled
                     + " OR `customer_id` LIKE '%" + searchArg + "%'" + showHandled
                     + " OR `description` LIKE '%" + searchArg + "%'" + showHandled
                     + " OR `location` LIKE '%" + searchArg + "%'" + showHandled
                     + " OR `date_lost` LIKE '%" + searchArg + "%'" + showHandled;
         } // for searching luggageId
         else if (dbField == 1) {
             sql = sqlSelect + " WHERE `luggage_id` LIKE '%" + searchArg + "%'"
                     + showHandled;
         } // customerId
         else if (dbField == 2) {
             sql = sqlSelect + " WHERE `customer_id` LIKE '%" + searchArg + "%'"
                     + showHandled;
         } // description
         else if (dbField == 3) {
             sql = sqlSelect + " WHERE `description` LIKE '%" + searchArg + "%'"
                     + showHandled;
         } // location
         else if (dbField == 4) {
             sql = sqlSelect + " WHERE `location` LIKE '%" + searchArg + "%'"
                     + showHandled;
         } // date
         else if (dbField == 5) {
             sql = sqlSelect + " WHERE `date_lost` LIKE '%" + searchArg + "%'"
                     + showHandled;
         } //lost luggage
         else if (dbField == 6) {
             sql = sqlSelect + " WHERE `date_lost` LIKE '%" + searchArg + "%'"
                     + " AND status = 1";
         } //lost luggage, regardless if it is still lost or not
         else if (dbField == 7) {
             sql = sqlSelect + " WHERE `date_lost` LIKE '%" + searchArg + "%'";
         } //found luggage
         else if (dbField == 8) {
             sql = sqlSelect + " WHERE `date_found` LIKE '%" + searchArg + "%'"
                     + " AND status = 2";
         } //found luggage, regardless if it is still found or not
         else if (dbField == 9) {
             sql = sqlSelect + " WHERE `date_found` LIKE '%" + searchArg + "%'";
         } //handled luggage
         else if (dbField == 10) {
             sql = sqlSelect + " WHERE `date_handled` LIKE '%" + searchArg + "%'"
                     + " AND `status` = 3";
         } else if (dbField == 11) {
             sql = sqlSelect + " WHERE `customer_id` = '" + searchArg + "'";
         } // Else statement is used to fill the table with all users
         else {
             if (handled == 1) {
                 sql = sqlSelect + " WHERE `status` != 3";
             } else {
                 sql = sqlSelect;
             }
         }
 
         try {
             db.openConnection();
             ResultSet result = tempLuggage.getDb().doQuery(sql);
             while (result.next()) {
                 luggages.add(new Luggage(result.getInt("luggage_id"),
                         result.getInt("customer_id"),
                         result.getString("description"),
                         result.getString("location"),
                         result.getString("date_lost"),
                         result.getInt("status"),
                         result.getString("date_changed"),
                         result.getString("date_handled"),
                         result.getString("date_found"),
                         result.getInt("last_changed_by")));
             }
         } catch (SQLException e) {
             System.out.println(tempLuggage.getDb().SQL_EXCEPTION + e.getMessage());
         }
           finally
         {
             db.closeConnection();
         }
         return luggages;
     }
 
     /**
      * Method to create new luggage with data given by textfields.
      *
      * @param customerId textfield Input.
      * @param description textfield Input.
      * @param location textfield Input.
      * @param status textfield Input.
      */
     public void createLuggage(String customerId, String description,
             String location, int status) {
         if (customerId.equals("")) {
             customerId = null;
         }
         try {
             db.openConnection();
             preparedStatement = db.connection.prepareStatement("INSERT INTO `luggage` (customer_id, description, location, "
                     + "status, last_changed_by , date_changed) VALUES (?, ?,? ,? ,? ,CURRENT_TIMESTAMP)");
             preparedStatement.setString(1, customerId);
             preparedStatement.setString(2, description);
             preparedStatement.setString(3, location);
             preparedStatement.setInt(4, status);
             preparedStatement.setInt(5, Session.storedUserId);
             preparedStatement.executeUpdate();
         } catch (SQLException ex) {
             Logger.getLogger(QueryManager.class.getName()).log(Level.SEVERE, null, ex);
         }
         finally
         {
             db.closeConnection();
         }
     }
 
     /**
      * Used to update already existing luggage.
      *
      * @param luggageId
      * @param description
      * @param location
      * @param status
      */
     public void updateLuggage(int luggageId, String description,
             String location, int status) {
         String dateHandled = "";
         if (status == 3) {
             dateHandled = ", `date_handled` = CURRENT_TIMESTAMP";
         }
         try {
             db.openConnection();
            preparedStatement = db.connection.prepareStatement("UPDATE `luggage` SET `description` = ?, `location` = ?, `status` = ?, `date_changed` = CURRENT_TIMESTAMP" + dateHandled + ", `last_changed_by` = ? WHERE `luggage_id` = ?");
             preparedStatement.setString(1, description);
             preparedStatement.setString(2, location);
             preparedStatement.setInt(3, status);
             preparedStatement.setInt(4, Session.storedUserId);
             preparedStatement.setInt(5, luggageId);
             preparedStatement.executeUpdate();
         } catch (SQLException ex) {
             Logger.getLogger(QueryManager.class.getName()).log(Level.SEVERE, null, ex);
             
         }
           finally
         {
             db.closeConnection();
         }
     }
 
     /**
      * Method to link luggage so a relation is created between the luggage and
      * the customer.
      *
      * @param customerId
      * @param luggageId
      */
     public void linkCustomerId(int customerId, int luggageId) {
         try {
             db.openConnection();
             preparedStatement = db.connection.prepareStatement("UPDATE `luggage` SET `customer_id` = ? WHERE `luggage_id` = ?");
             preparedStatement.setInt(1, customerId);
             preparedStatement.setInt(2, luggageId);
             preparedStatement.executeUpdate();
         } catch (SQLException ex) {
             Logger.getLogger(QueryManager.class.getName()).log(Level.SEVERE, null, ex);
         }
         finally{
             db.closeConnection();
         }
     }
 
     /**
      * Deletes luggage from database.
      *
      * @param luggageId
      */
     public void deleteLuggage(String luggageId) {
         try {
             db.openConnection();
             preparedStatement = db.connection.prepareStatement("DELETE FROM `luggage` WHERE `luggage_id` = ?");
             preparedStatement.setString(1, luggageId);
             preparedStatement.executeUpdate();
         } catch (SQLException ex) {
             Logger.getLogger(QueryManager.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             db.closeConnection();
         }
     }
 
     /**
      * Create a new Resort recort. Create a new resort.
      *
      * @param tfName
      * @param tfAddress
      * @param tfCountry
      * @param tfCity
      * @param tfPhoneResort
      * @param tfEmail
      * @param tfpostalCode
      */
     public void setNewResort(String tfName, String tfAddress, String tfCountry, String tfCity,
             String tfPhoneResort, String tfEmail, String tfpostalCode) {
         try {
             db.openConnection();
             preparedStatement = db.connection.prepareStatement("INSERT INTO `resort` (resort_name, address, country, city,"
                     + "phone_resort, email, postal_code)"
                     + "VALUES (?, ?, ?, ?, ?, ?, ?)");
             preparedStatement.setString(1, tfName);
             preparedStatement.setString(2, tfAddress);
             preparedStatement.setString(3, tfCountry);
             preparedStatement.setString(4, tfCity);
             preparedStatement.setString(5, tfPhoneResort);
             preparedStatement.setString(6, tfEmail);
             preparedStatement.setString(7, tfpostalCode);
             preparedStatement.executeUpdate();
         } catch (SQLException ex) {
             Logger.getLogger(QueryManager.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             db.closeConnection();
         }
     }
 
     /**
      * Delete a single resort record.
      *
      * @param tfResortId
      */
     public void deleteResort(String tfResortId) {
         try {
             db.openConnection();
             preparedStatement = db.connection.prepareStatement("DELETE FROM `resort` WHERE `resort_id` = ?");
             preparedStatement.setString(1, tfResortId);
             preparedStatement.executeUpdate();
         } catch (SQLException ex) {
             Logger.getLogger(QueryManager.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             db.closeConnection();
         }
 
     }
 
     /**
      * Update a singel resort record. Update a resort record.
      *
      * @param tfId
      * @param tfName
      * @param tfAddress
      * @param tfCountry
      * @param tfCity
      * @param tfPhoneResort
      * @param tfEmail
      * @param tfpostalCode
      */
     public void updateResort(int tfId, String tfName, String tfAddress, String tfCountry, String tfCity,
             String tfPhoneResort, String tfEmail, String tfpostalCode) {
         try {
             db.openConnection();
             preparedStatement = db.connection.prepareStatement("UPDATE `resort` SET `resort_name` = ? `address` = ?, `country` = ?, `city` = ?, `phone_resort` = ?, `email` = ? `postal_code` = ? WHERE `resort_id` = ?");
             preparedStatement.setString(1, tfName);
             preparedStatement.setString(2, tfAddress);
             preparedStatement.setString(3, tfCountry);
             preparedStatement.setString(4, tfCity);
             preparedStatement.setString(5, tfPhoneResort);
             preparedStatement.setString(6, tfEmail);
             preparedStatement.setString(7, tfpostalCode);
             preparedStatement.executeUpdate();
         } catch (SQLException ex) {
             Logger.getLogger(QueryManager.class.getName()).log(Level.SEVERE, null, ex);
         }
           finally
         {
             db.closeConnection();
         }
     }
 
     /**
      * get a single resort record.
      *
      * @param tfInput
      * @param databaseVariable
      */
     public Resort getResortData(String tfInput, String databaseVariable) {
         Resort tempResort = new Resort();
         try {
             db.openConnection();
             String sql = "SELECT *, COUNT(*) as `rows` FROM `resort` WHERE `"
                     + databaseVariable + "`='" + tfInput + "'";
 
             ResultSet result = tempResort.getDb().doQuery(sql);
             if (result.next()) {
 
                 if (result.getInt("rows") >= 0) {
                     tempResort.setId(result.getInt("resort_id"));
                     tempResort.setName(result.getString("resort_name"));
                     tempResort.setAddress(result.getString("address"));
                     tempResort.setCountry(result.getString("country"));
                     tempResort.setCity(result.getString("city"));
                     tempResort.setPhone(result.getString("phone_resort"));
                     tempResort.setEmail(result.getString("email"));
                     tempResort.setPostalCode(result.getString("postal_code"));
                 } else {
                     System.out.println("SOMETING WENT WRONG");
                 }
             }
 
         } catch (SQLException e) {
             System.out.println(tempResort.getDb().SQL_EXCEPTION + e.getMessage());
         }
           finally
         {
             db.closeConnection();
         }
         return tempResort;
     }
 
     /**
      * Method to search the entity Resorts.
      *
      * @param dbField field to search, if null it searches al fields.
      * @param searchArg the paramater to search with.
      * @return a list of resorts coresponding to the parameters.
      */
     public List<Resort> searchResortList(int dbField, String searchArg) {
         List<Resort> resorts = new ArrayList<>();
         Resort tempResort = new Resort();
         String sql, sqlSelect = "SELECT * FROM `resort`";
 
         if (dbField == 0) {
             sql = sqlSelect + "WHERE `resort_id` LIKE '%" + searchArg + "%'"
                     + "OR `resort_name` LIKE '%" + searchArg + "%'"
                     + "OR `address` LIKE '%" + searchArg + "%'"
                     + "OR `country` LIKE '%" + searchArg + "%'"
                     + "OR `city` LIKE '%" + searchArg + "%'"
                     + "OR `phone_resort` LIKE '%" + searchArg + "%'"
                     + "OR `email` LIKE '%" + searchArg + "%'"
                     + "OR `postal_code`  LIKE '%" + searchArg + "%'";
         } else if (dbField == 1) {
             sql = sqlSelect + "WHERE `resort_name` LIKE '%" + searchArg + "%'";
         } else if (dbField == 2) {
             sql = sqlSelect + "WHERE `address` LIKE '%" + searchArg + "%'";
         } else if (dbField == 3) {
             sql = sqlSelect + "WHERE `country` LIKE '%" + searchArg + "%'";
         } else if (dbField == 4) {
             sql = sqlSelect + "WHERE `city` LIKE '%" + searchArg + "%'";
         } else if (dbField == 5) {
             sql = sqlSelect + "WHERE `phone_resort` LIKE '%" + searchArg + "%'";
         } else if (dbField == 6) {
             sql = sqlSelect + "WHERE `email` LIKE '%" + searchArg + "%'";
         } else if (dbField == 7) {
             sql = sqlSelect + "WHERE `postal_code` LIKE '%" + searchArg + "%'";
         } else {
             sql = sqlSelect;
         }
 
         try {
             db.openConnection();
             ResultSet result = tempResort.getDb().doQuery(sql);
             while (result.next()) {
                 resorts.add(new Resort(result.getInt("resort_id"),
                         result.getString("resort_name"),
                         result.getString("address"),
                         result.getString("country"),
                         result.getString("city"),
                         result.getString("phone_resort"),
                         result.getString("email"),
                         result.getString("postal_code")));
             }
         } catch (SQLException e) {
             System.out.println(tempResort.getDb().SQL_EXCEPTION + e.getMessage());
         }
           finally
         {
             db.closeConnection();
         }
         return resorts;
     }
 
     /**
      * links a resort to a customer.
      *
      * @param customerId
      * @param resortId
      */
     public void linkCustomerIdToResort(int customerId, int resortId) {
         try {
             db.openConnection();
         String sql = "UPDATE `customer` SET `resort_id` = " + resortId
                 + "WHERE `customer_id` =" + customerId;
         
         db.insertQuery(sql);
         
             preparedStatement = db.connection.prepareStatement("UPDATE `customer` SET `resort_id` = ? WHERE `customer_id` = ?");
             preparedStatement.setInt(1, resortId);
             preparedStatement.setInt(1, customerId);
             preparedStatement.executeUpdate();
         } catch (SQLException ex) {
             Logger.getLogger(QueryManager.class.getName()).log(Level.SEVERE, null, ex);
         }
             finally
         {
             db.closeConnection();
         }
         
     }
 
 }
