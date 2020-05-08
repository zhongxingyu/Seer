 package edu.umw.cpsc330.twitterclone;
 
 import java.sql.*;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Database methods specifically for manipulating users.
  * 
  * @author Alex Lindeman
  */
 public class UserDatabase extends Database {
 
     /**
      * Default constructor. Attempts to create the user table if it does not
      * already exist.
      */
     public UserDatabase() {
 	try {
 	    Class.forName(DRIVER);
 	    db = DriverManager.getConnection(URI);
 	    Statement create = db.createStatement();
 	    create.execute("CREATE TABLE IF NOT EXISTS users ( id INTEGER PRIMARY KEY, username TEXT, pwhash TEXT, pwsalt TEXT, name TEXT, bio TEXT, following TEXT, UNIQUE (username) ON CONFLICT ABORT );");
 	} catch (ClassNotFoundException e) {
 	    System.err.println("Caught exception while attempting to use database driver: "
 			    + e.getMessage());
 	} catch (SQLException e) {
 	    System.err.println("Caught exception while doing something to the database: "
 			    + e.getMessage());
 	}
     }
 
     /**
      * Gets user information by username
      * 
      * @param username User to get data for
      * @return User
      * @throws SQLException
      */
     public User get(String username) throws SQLException {
 	String sql = "SELECT * FROM users WHERE username = ?";
 	PreparedStatement st = db.prepareStatement(sql);
 	st.setQueryTimeout(TIMEOUT);
 	st.setString(1, username);
 
 	ResultSet results = st.executeQuery();
 	List<User> parsed = parseResults(results);
 
 	return (parsed.size() == 1) ? parsed.get(0) : null;
     }
     
     /**
      * Dumps an array of users that exist in the database.
      * @return Array of users
      * @throws SQLException
      */
     public User[] dump() throws SQLException {
 	String sql = "SELECT * FROM USERS";
 	PreparedStatement st = db.prepareStatement(sql);
 	st.setQueryTimeout(TIMEOUT);
 	
 	ResultSet results = st.executeQuery();
 	List<User> parsed = parseResults(results);
 	
 	return parsed.toArray(new User[parsed.size()]);
     }
 
     /**
      * Adds a user to the database.
      * 
      * @param user The user to be added
      * @return number of rows affected (should be 1 if the add was successful)
      * @throws SQLException
      */
     public int add(User user) throws SQLException {
 	String sql = "INSERT INTO users VALUES ( null, ?, ?, ?, ?, ?, ? )";
 	PreparedStatement st = db.prepareStatement(sql);
 	st.setQueryTimeout(TIMEOUT);
 
 	st.setString(1, user.username);
 	st.setString(2, user.pwhash);
 	st.setString(3, user.pwsalt);
 	st.setString(4, user.name);
 	st.setString(5, user.bio);
 	st.setString(6, implode(user.following));
 
 	st.execute();
 	int result = st.getUpdateCount();
 	return result;
     }
 
     /**
      * Edits a user in the database
      * 
      * @param user User to edit
      * @return Number of rows affected
      * @throws SQLException
      */
     public int edit(User user) throws SQLException {
	String sql = "UPDATE users SET ( pwhash = ?, pwsalt = ?, name = ?, bio = ? ) WHERE username = ?";
 	PreparedStatement st = db.prepareStatement(sql);
 	st.setQueryTimeout(TIMEOUT);
 
 	st.setString(1, user.pwhash);
 	st.setString(2, user.pwsalt);
 	st.setString(3, user.name);
 	st.setString(4, user.bio);
 	st.setString(5, user.username);
 
 	st.execute();
 	int result = st.getUpdateCount();
 	return result;
     }
 
     /**
      * Removes a user from the database
      * 
      * @param username The user to be deleted
      * @return Number of rows affected (should be 1 if the deletion was successful)
      * @throws SQLException
      */
     public int delete(String username) throws SQLException {
 	String sql = "DELETE FROM users WHERE username = ?;";
 	PreparedStatement st = db.prepareStatement(sql);
 	st.setQueryTimeout(TIMEOUT);
 
 	st.setString(1, username);
 
 	st.execute();
 	int result = st.getUpdateCount();
 	return result;
     }
 
     /**
      * Modifies a user's followers.
      * 
      * @param user User to modify
      * @param follow List of users to follow (as strings)
      * @return number of rows affected
      * @throws SQLException
      */
     public int updateFollowers(String username, List<String> follow)
 	    throws SQLException {
 	String sql = "UPDATE users SET ( followers = ? ) WHERE username = ?";
 	PreparedStatement st = db.prepareStatement(sql);
 
 	st.setString(1, implode(follow));
 	st.setString(2, username);
 
 	st.execute();
 	int result = st.getUpdateCount();
 	return result;
     }
 
     /**
      * Iterates through a ResultSet and converts it to a list
      * 
      * @param results Results of a SQL query
      * @return List of users
      * @throws SQLException
      */
     private static List<User> parseResults(ResultSet results) throws SQLException {
 	List<User> userList = new LinkedList<User>();
 
 	// check that there are results
 	if (results.next()) {
 	    do {
 		User u = new User();
 
 		u.id = results.getInt("id");
 		u.username = results.getString("username");
 		u.pwhash = results.getString("pwhash");
 		u.pwsalt = results.getString("pwsalt");
 		u.name = results.getString("name");
 		u.bio = results.getString("bio");
 
 		String f = results.getString("following");
 		if (f != null && f.length() > 0)
 		    u.following = Arrays.asList(f.split(","));
 		else
 		    u.following = new LinkedList<String>();
 
 		userList.add(u);
 	    } while (results.next());
 	}
 
 	// return null if empty
 	return userList.isEmpty() ? null : userList;
     }
 
     /**
      * Implodes a list into a comma-separated string.
      * 
      * @param str String to implode
      * @return Comma-separated list
      */
     private static String implode(List<String> str) {
 	String[] collection = str.toArray(new String[str.size()]);
 
 	StringBuilder result = new StringBuilder();
 	for (String string : collection) {
 	    result.append(string);
 	    result.append(",");
 	}
 
 	return result.length() > 0 ? result.substring(0, result.length() - 1)
 		: "";
     }
 }
