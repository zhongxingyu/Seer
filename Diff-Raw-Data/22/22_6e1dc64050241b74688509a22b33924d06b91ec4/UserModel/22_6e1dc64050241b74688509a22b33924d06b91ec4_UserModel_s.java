 package models;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 
 import jgravatar.Gravatar;
 import jgravatar.GravatarDefaultImage;
 import jgravatar.GravatarRating;
 
 import org.apache.commons.codec.digest.DigestUtils;
 
 /**
  * The user model
  * 
  * Represents a user record from the database
  * 
  * @author David Knezic <davidknezic@gmail.com>
  */
 public class UserModel {
 	
 	/**
 	 * UserPermissions
 	 */
 	public static final int USER = 0;
 	public static final int ADMIN = 1;
 	public static final int SUPER_ADMIN = 2;
 
 	/**
 	 * The userId
 	 */
 	private int userId;
 	
 	/**
 	 * The permission
 	 */
 	private int permission;
 
 	/**
 	 * The username
 	 */
 	private String username;
 
 	/**
 	 * The user password
 	 */
 	private String password;
 
 	/**
 	 * The email address
 	 */
 	private String email;
 	
 	/**
 	 * The website
 	 */
 	private String website;
 	
 	/**
 	 * The location
 	 */
 	private String location;
 	
 	/**
 	 * First name
 	 */
 	private String firstName;
 	
 	/**
 	 * Last name
 	 */
 	private String lastName;
 
 	/**
 	 * Date of birth
 	 */
 	private Timestamp dateOfBirth;
 	
 	/**
 	 * The activation status
 	 */
 	private boolean active;
 	
 	/**
 	 * The creation date
 	 */
 	private Timestamp createdOn;
 
 	/**
 	 * Initialize an empty user
 	 */
 	public UserModel() {
 		// Indicate that this object is not yet saved
 		this.userId = 0;
 		this.createdOn = new Timestamp(new Date().getTime());
 	}
 
 	/**
 	 * Initialize the given user
 	 * 
 	 * @param userId
 	 * @throws Throwable
 	 */
 	public UserModel(int userId) throws Throwable {
 		Connection conn = DBConnection.getInstance().getConnection();
 
 		PreparedStatement stmt = conn
 				.prepareStatement("SELECT username, password, email, active, permission, website, location, firstName, lastName, dateOfBirth, createdOn FROM user WHERE userId = ?");
 		stmt.setInt(1, userId);
 		ResultSet res = stmt.executeQuery();
 
 		if (res.first()) {
 			// Assign the retrieved information
 			this.userId = userId;
 			this.username = res.getString(1);
 			this.password = res.getString(2);
 			this.email = res.getString(3);
 			this.active = (res.getInt(4) == 0 ? false : true);
 			this.permission = res.getInt(5);
 			this.website = res.getString(6);
 			this.location = res.getString(7);
 			this.firstName = res.getString(8);
 			this.lastName = res.getString(9);
 			this.dateOfBirth = res.getTimestamp(10);
 			this.createdOn = res.getTimestamp(11);
 		} else {
 			// No user with given id found
 			throw new Exception("Could not find user");
 		}
 	}
 
 	/**
 	 * Either inserts a new record or updates an existing one depending on the
 	 * userId
 	 * 
 	 * @throws Throwable
 	 */
 	public void save() throws Throwable {
 		Connection conn = DBConnection.getInstance().getConnection();
 		PreparedStatement stmt;
 
 		if (this.userId == 0) {
 			// Insert a new record
 			stmt = conn
 					.prepareStatement(
 							"INSERT INTO user (username, password, email, active, permission, website, location, firstName, lastName, dateOfBirth, createdOn) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
 							Statement.RETURN_GENERATED_KEYS);
 		} else {
 			// Update record
 			stmt = conn
 					.prepareStatement(
 							"UPDATE user SET username = ?, password = ?, email = ?, active = ?, permission = ?, website = ?, location = ?, firstName = ?, lastName = ?, dateOfBirth = ?, createdOn = ? WHERE userId = ?",
 							Statement.RETURN_GENERATED_KEYS);
 			stmt.setInt(12, this.userId);
 		}
 
 		// Set global variables
 		stmt.setString(1, this.username);
 		stmt.setString(2, this.password);
 		stmt.setString(3, this.email);
 		stmt.setInt(4, (this.active == true ? 1 : 0));
 		stmt.setInt(5, this.permission);
 		
 		
 		stmt.setString(6, this.website);
 		stmt.setString(7, this.location);
 		stmt.setString(8, this.firstName);
 		stmt.setString(9, this.lastName);
 		stmt.setTimestamp(10, this.dateOfBirth);
 		stmt.setTimestamp(11, this.createdOn);
 
 		// Execute query
 		stmt.executeUpdate();
 
 		// Get the generated key if we made an insert
 		if (this.userId == 0) {
 			ResultSet set = stmt.getGeneratedKeys();
 
 			if (set.next()) {
 				this.userId = set.getInt(1);
 			}
 		}
 	}
 	
 	/**
 	 * Deletes the current row
 	 * 
 	 * @throws Throwable
 	 */
 	public void delete() throws Throwable {
 		Connection conn = DBConnection.getInstance().getConnection();
 		PreparedStatement stmt;
 		
 		stmt = conn.prepareStatement("DELETE FROM user WHERE userId = ?");
 		stmt.setInt(1, this.userId);
 		
 		stmt.executeUpdate();
 	}
 
 	/**
 	 * Get the userId
 	 * 
 	 * @return int
 	 */
 	public int getUserId() {
 		return userId;
 	}
 
 	/**
 	 * Get the username
 	 * 
 	 * @return String
 	 */
 	public String getUsername() {
 		return username;
 	}
 
 	/**
 	 * Set the username
 	 * 
 	 * @param username
 	 */
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	/**
 	 * Get the password
 	 * 
 	 * @return String
 	 */
 	public String getPassword() {
 		return password;
 	}
 
 	/**
 	 * Set the password
 	 * 
 	 * @param password
 	 */
 	public void setPassword(String password) {
 		this.password = DigestUtils.md5Hex(password);
 	}
 
 	/**
 	 * Get email address
 	 * 
 	 * @return String
 	 */
 	public String getEmail() {
 		return email;
 	}
 
 	/**
 	 * Set email address
 	 * 
 	 * @param email
 	 */
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	/**
 	 * Is active
 	 * 
 	 * @return boolean
 	 */
 	public boolean isActive() {
 		return active;
 	}
 
 	/**
 	 * Set active
 	 * 
 	 * @param active
 	 */
 	public void setActive(boolean active) {
 		this.active = active;
 	}
 	
 	/**
 	 * Get permission role
 	 * 
 	 * @return int
 	 */
 	public int getPermission() {
 		return this.permission;
 	}
 	
 	/**
 	 * Set permission role
 	 * 
 	 * @param permission
 	 */
 	public void setPermission(int permission) {
 		this.permission = permission;
 	}
 
 	/**
 	 * Get website
 	 * 
 	 * @return String
 	 */
 	public String getWebsite() {
 		return website;
 	}
 
 	/**
 	 * Set website
 	 * 
 	 * @param website
 	 */
 	public void setWebsite(String website) {
 		this.website = website;
 	}
 
 	/**
 	 * Get location
 	 * 
 	 * @return String
 	 */
 	public String getLocation() {
 		return location;
 	}
 
 	/**
 	 * Set location
 	 * 
 	 * @param location
 	 */
 	public void setLocation(String location) {
 		this.location = location;
 	}
 
 	/**
 	 * Get first name
 	 * 
 	 * @return String
 	 */
 	public String getFirstName() {
 		return firstName;
 	}
 
 	/**
 	 * Set first name
 	 * 
 	 * @param firstName
 	 */
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 
 	/**
 	 * Get last name
 	 * 
 	 * @return String
 	 */
 	public String getLastName() {
 		return lastName;
 	}
 
 	/**
 	 * Set last name
 	 * 
 	 * @param lastName
 	 */
 	public void setLastName(String lastName) {
 		this.lastName = lastName;
 	}
 
 	/**
 	 * Get the "timestamp of birth" as a standard java Date
 	 * 
 	 * @return Date
 	 */
 	public Date getDateOfBirth() {
 		return (Date) dateOfBirth;
 	}
 
 	/**
 	 * Set the "timestamp of birth" as a standard java Date
 	 * 
 	 * @param createdOn
 	 */
 	public void setDateOfBirth(Timestamp dateOfBirth) {
		this.dateOfBirth = new Timestamp(dateOfBirth.getTime());
 	}
 	
 	public void setDateOfBirth(Date dob) {
		this.dateOfBirth = new Timestamp(dob.getTime());
 	}
 
 	/**
 	 * Get the creation timestamp as a standard java Date
 	 * 
 	 * @return Date
 	 */
 	public Date getCreatedOn() {
 		return (Date) createdOn;
 	}
 
 	/**
 	 * Set the creation timestamp as a standard java Date
 	 * 
 	 * @param createdOn
 	 */
 	public void setCreatedOn(Timestamp createdOn) {
 		this.createdOn = new Timestamp(createdOn.getTime());
 	}
 
 	/**
 	 * Get number of threads belonging to this user
 	 * 
 	 * @return int
 	 * @throws Throwable
 	 */
 	public int getThreadCount() throws Throwable {
 		Connection conn = DBConnection.getInstance().getConnection();
 
 		PreparedStatement stmt = conn
 				.prepareStatement("SELECT COUNT(*) FROM thread WHERE userId = ?");
 		stmt.setInt(1, this.userId);
 		ResultSet res = stmt.executeQuery();
 
 		if (res.first()) {
 			// Return the thread count
 			return res.getInt(1);
 		} else {
 			// An error occured, return zero
 			return 0;
 		}
 	}
 
 	/**
 	 * Get number of replies belonging to this user
 	 * 
 	 * @return int
 	 * @throws Throwable
 	 */
 	public int getReplyCount() throws Throwable {
 		Connection conn = DBConnection.getInstance().getConnection();
 
 		PreparedStatement stmt = conn
 				.prepareStatement("SELECT COUNT(*) FROM reply WHERE userId = ?");
 		stmt.setInt(1, this.userId);
 		ResultSet res = stmt.executeQuery();
 
 		if (res.first()) {
 			// Return the reply count
 			return res.getInt(1);
 		} else {
 			// An error occured, return zero
 			return 0;
 		}
 	}
 
 	/**
 	 * Get threads
 	 * 
 	 * @return ArrayList<ThreadModel>
 	 * @throws Throwable
 	 */
 	public ArrayList<ThreadModel> getThreads(int off, int max) throws Throwable {
 		Connection conn = DBConnection.getInstance().getConnection();
 		ArrayList<ThreadModel> threads = new ArrayList<ThreadModel>();
 
 		PreparedStatement stmt = conn
 				.prepareStatement("SELECT threadId FROM thread WHERE userId = ? ORDER BY createdOn ASC LIMIT ?, ?");
 		stmt.setInt(1, this.userId);
 		stmt.setInt(2, off);
 		stmt.setInt(3, max);
 		ResultSet res = stmt.executeQuery();
 
 		// For each thread
 		while (res.next()) {
 			// Add current thread object to array
 			threads.add(new ThreadModel(res.getInt(1)));
 		}
 
 		return threads;
 	}
 
 	/**
 	 * Get replies
 	 * 
 	 * @return ArrayList<ReplyModel>
 	 * @throws Throwable
 	 */
 	public ArrayList<ReplyModel> getReplies(int off, int max) throws Throwable {
 		Connection conn = DBConnection.getInstance().getConnection();
 		ArrayList<ReplyModel> threads = new ArrayList<ReplyModel>();
 
 		PreparedStatement stmt = conn
 				.prepareStatement("SELECT replyId FROM reply WHERE userId = ? ORDER BY createdOn ASC LIMIT ?, ?");
 		stmt.setInt(1, this.userId);
 		stmt.setInt(2, off);
 		stmt.setInt(3, max);
 		ResultSet res = stmt.executeQuery();
 
 		// For each reply
 		while (res.next()) {
 			// Add current reply object to array
 			threads.add(new ReplyModel(res.getInt(1)));
 		}
 
 		return threads;
 	}
 
 	/**
 	 * Get the avatar picture url
 	 * 
 	 * @param size
 	 * @return String
 	 */
 	public String getAvatar(Long size) {
 		Gravatar gravatar = new Gravatar();
 		gravatar.setSize(size.intValue());
 		gravatar.setRating(GravatarRating.GENERAL_AUDIENCES);
 		gravatar.setDefaultImage(GravatarDefaultImage.IDENTICON);
 		String url = gravatar.getUrl(this.email);
 		return url;
 	}
 
 	/**
 	 * Get user list
 	 * 
 	 * @return ArrayList<UserModel>
 	 * @throws Throwable
 	 */
 	public static ArrayList<UserModel> getUsers(int off, int max)
 			throws Throwable {
 		Connection conn = DBConnection.getInstance().getConnection();
 		ArrayList<UserModel> users = new ArrayList<UserModel>();
 
 		PreparedStatement stmt = conn
 				.prepareStatement("SELECT userId FROM user LIMIT ?, ?");
 		stmt.setInt(1, off);
 		stmt.setInt(2, max);
 		ResultSet res = stmt.executeQuery();
 
 		// For each user
 		while (res.next()) {
 			// Add current user object to array
 			users.add(new UserModel(res.getInt(1)));
 		}
 
 		return users;
 	}
 
 	/**
 	 * Get number of users
 	 * 
 	 * @return int
 	 * @throws Throwable
 	 */
 	public static int getUserCount() throws Throwable {
 		Connection conn = DBConnection.getInstance().getConnection();
 
 		PreparedStatement stmt = conn
 				.prepareStatement("SELECT COUNT(*) FROM user");
 		ResultSet res = stmt.executeQuery();
 
 		if (res.first()) {
 			// Return the user count
 			return res.getInt(1);
 		} else {
 			// An error occured, return zero
 			return 0;
 		}
 	}
 
 	/**
 	 * Get user by username
 	 * 
 	 * TODO: Check if this is necessary!
 	 * 
 	 * @return UserModel
 	 * @throws Throwable
 	 */
 	public static UserModel getUserByUsername(String username) throws Throwable {
 		Connection conn = DBConnection.getInstance().getConnection();
 
 		PreparedStatement stmt = conn
 				.prepareStatement("SELECT userId FROM user WHERE username = ?");
 		stmt.setString(1, username);
 		ResultSet res = stmt.executeQuery();
 
 		if (res.first()) {
 			// Return the user object
 			return new UserModel(res.getInt(1));
 		} else {
 			// No user found, return null
 			return null;
 		}
 	}
 
 	/**
 	 * Get user by identity / credential
 	 * 
 	 * @return UserModel
 	 * @throws Throwable
 	 */
 	public static UserModel authenticate(String username, String password)
 			throws Throwable {
 		Connection conn = DBConnection.getInstance().getConnection();
 
 		PreparedStatement stmt = conn
 				.prepareStatement("SELECT userId FROM user WHERE username = ? AND password = MD5(?)");
 		stmt.setString(1, username);
 		stmt.setString(2, password);
 		ResultSet res = stmt.executeQuery();
 
 		if (res.first()) {
 			// Return the user object
 			return new UserModel(res.getInt(1));
 		} else {
 			// No user found, return null
 			return null;
 		}
 	}
 }
