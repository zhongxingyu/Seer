 package playlist.model;
 
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.SimpleStatement;
 import playlist.exceptions.UserExistsException;
 import playlist.exceptions.UserLoginException;
 
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.UUID;
 
 /**
  * DataStax Academy Sample Application
  *
  * Copyright 2013 DataStax
  *
  */
 
 
 public class UserDAO extends CassandraData {
 
   private String email;
   private String password;
   private UUID userid;
   private SortedSet<String> playlist_names;
 
   private UserDAO(Row row) {
     email = row.getString("email");
     password = row.getString("password");
     userid = row.getUUID("user_id");
 
     // We do this because we want a sorted set, and Cassandra only returns a regular set
     // the driver gives us a HashLinkedSet. We need to choose our implementation.
     playlist_names = new TreeSet<>();
     playlist_names.addAll(row.getSet("playlist_names", String.class));
 
   }
 
   UserDAO(String email, String password, UUID userid) {
     this.userid = userid;
     this.password = password;
     this.email = email;
     this.playlist_names = new TreeSet<>();
   }
 
   /**
    * Static method to add a user.  It generates a new UUID for the user to use as its surrogate key.  It first checks to
    * ensure that the user does not already exist.  If it exists, we throw a UserExistsException
    *
    * @param email new user's email address
    * @param password new user's password
    * @return a UserDAO object which also contains the new user_id field (UUID)
    * @throws UserExistsException
    */
   public static UserDAO addUser(String email, String password) throws UserExistsException {
 
    if (getUserWithQuorum(email) != null) {
      throw new UserExistsException();
    }

     // Generate a new UUID to use as the user's surrogate key
     UUID userId = UUID.randomUUID();
 
    String queryText = "INSERT INTO users (email, password, user_id) values (?, ?, ?)";
 
     PreparedStatement preparedStatement = getSession().prepare(queryText);
 
    // We want to run this statement with CL quorum
    preparedStatement.setConsistencyLevel(ConsistencyLevel.QUORUM);
 
    getSession().execute(preparedStatement.bind(email, password, userId));
 
     // Return the new user so the caller can get the userid
     return new UserDAO(email, password, userId);
 
   }
 
   /**
    * Delete the user.  It does not need to check if the user already exists.
    */
   public void deleteUser() {
     SimpleStatement simpleStatement = new SimpleStatement("DELETE FROM users where email = '"
             + this.email + "'");
 
     // Delete users with CL = Quorum
     simpleStatement.setConsistencyLevel(ConsistencyLevel.QUORUM);
     getSession().execute(simpleStatement);
 
   }
 
 
   /**
    * Look up a user by e-mail address and return a UserDAO object for it
    *
    * @param email The email address to search
    * @return  A full UserDAO object
    */
   public static UserDAO getUser(String email) {
     String queryText = "SELECT * FROM users where email = '"
             + email + "'";
 
     Row userRow = getSession().execute(queryText).one();
 
     if (userRow == null) {
       return null;
     }
 
     return new UserDAO(userRow);  }
 
 
   /**
    * This routine retrieves a userDAO, but reads it with a consistency level of quorum
    *
    * @param email email address to search
    * @return a UserDAO object
    */
   private static UserDAO getUserWithQuorum(String email) {
 
     String queryText = "SELECT * FROM users where email = '"
             + email + "'";
 
     SimpleStatement simpleStatement = new SimpleStatement(queryText);
     simpleStatement.setConsistencyLevel(ConsistencyLevel.QUORUM);
     Row userRow = getSession().execute(simpleStatement).one();
 
     if (userRow == null) {
       return null;
     }
 
     return new UserDAO(userRow);
 
   }
 
   /**
    * The also retrieves a user based on the email address, but also validates its password.  If the password is invalid, a
    * UserLoginException is thrown
    *
    * @param email  email address to search
    * @param password  password to validate
    * @return  a completed UserDAO object
    * @throws UserLoginException
    */
   public static UserDAO validateLogin(String email, String password) throws UserLoginException {
 
     UserDAO user = getUserWithQuorum(email);
     if (user == null || !user.password.contentEquals(password)) {
       throw new UserLoginException();
     }
 
     return user;
   }
 
   public String getEmail() {
     return email;
   }
 
   public String getPassword() {
     return password;
   }
 
   public UUID getUserid() {
     return userid;
   }
 
   public Set<String> getPlaylist_names() {
     return playlist_names;
   }
 
 
 }
