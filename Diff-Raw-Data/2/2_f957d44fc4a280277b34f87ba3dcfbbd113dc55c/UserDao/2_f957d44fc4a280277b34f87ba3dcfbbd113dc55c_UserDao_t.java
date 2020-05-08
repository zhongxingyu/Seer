 package com.parq.server.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.Element;
 
 import com.parq.server.dao.exception.DuplicateEmailException;
 import com.parq.server.dao.model.object.User;
 
 /**
  * Dao class responsible for accessing and updating the User Table
  * 
  * 
  * @author GZ
  * 
  */
 public class UserDao extends AbstractParqDaoParent {
 
 	/**
 	 * Name of the local cache use by this dao
 	 */
 	private static final String cacheName = "UserCache";
 	private static Cache myCache;
 
 	private static final String sqlGetUserStatement = "SELECT user_id, password, email, phone_number FROM user ";
 	private static final String isNotDeleted = " AND is_deleted IS NOT TRUE";
 	private static final String sqlGetUserById = sqlGetUserStatement + "WHERE user_id = ? " + isNotDeleted;
 	private static final String sqlGetUserByEmail = sqlGetUserStatement + "WHERE email = ? " + isNotDeleted;
 
 	private static final String sqlDeleteUserById = "UPDATE user SET is_deleted = TRUE, email = ? WHERE user_id = ?";
 	private static final String sqlUpdateUser = "UPDATE user SET password = ?, email = ?, phone_number = ? "
 			+ " WHERE user_id = ?";
 	private static final String sqlCreateUser = "INSERT INTO user (password, email, phone_number, django_user_id) "
 			+ " VALUES (?, ?, ?, ?)";
 	
 	static final String sqlDjangoCreateAuthUser = "INSERT INTO auth_user (username, email, password) " +
 			" VALUES (?, ?, ?)";
 	static final String sqlDjangoUpdateAuthUser = "UPDATE auth_user SET username =?, email = ?, password = ?";
 	static final String sqlDjangoDeleteAuthUser = "UPDATE auth_user SET is_active = false, username = ?, email = ? " +
 			" WHERE id = (SELECT django_user_id FROM user WHERE user_id = ?)";
 	
 	static final String sqlGetNewDjangoAuthUserId = "SELECT MAX(id) AS id FROM auth_user WHERE email = ? ";
 	
 	private static final String emailCache = "getUserByEmail:";
 	private static final String idCache = "getUserById:";
 
 	public UserDao() {
 		super();
 		if (myCache == null) {
 			// create the cache.
 			myCache = setupCache(cacheName);
 		}
 	}
 
 	/**
 	 * Create the User model object from the DB query result set.
 	 * 
 	 * @param rs
 	 * @return
 	 * @throws SQLException
 	 */
 	private User createUserObject(ResultSet rs) throws SQLException {
 		if (rs == null || !rs.isBeforeFirst()) {
 			return null;
 		}
 		User user = new User();
 		rs.first();
 		user.setUserID(rs.getLong("user_id"));
 		user.setPassword(rs.getString("password"));
 		user.setEmail(rs.getString("email"));
 		user.setPhoneNumber(rs.getString("phone_number"));
 		return user;
 	}
 
 	/**
 	 * Retrieve the <code>User</code> object based on the userId, if no
 	 * <code>User</code> exist based on the userId or if the <code>User</code>
 	 * with this id has been deleted, then <code>NULL</code> is returned.
 	 * 
 	 * @param id
 	 *            The id of the user to retrive, must be > 0
 	 * @return <code>User</code> corresponding to the id, or <code>NULL</code>
 	 *         is no such user exist or the user has been deleted
 	 *         <code>NULL</code>
 	 */
 	public User getUserById(long id) {
 		// the cache key for this method call;
 		String cacheKey = idCache + id;
 		
 		User user = null;
 		Element cacheEntry = myCache.get(cacheKey);
 		if (cacheEntry  != null) {
 			user = (User) cacheEntry.getValue();
 			return user;
 		}
 
 		// query the DB for the user object
 		PreparedStatement pstmt = null;
 		Connection con = null;
 		try {
 			con = getConnection();
 			pstmt = con.prepareStatement(sqlGetUserById);
 			pstmt.setLong(1, id);
 			ResultSet rs = pstmt.executeQuery();
 
 			user = createUserObject(rs);
 
 		} catch (SQLException sqle) {
 			System.out.println("SQL statement is invalid: " + pstmt);
 			sqle.printStackTrace();
 			throw new RuntimeException(sqle);
 		} finally {
 			closeConnection(con);
 		}
 
 		// put result into cache
 		myCache.put(new Element(cacheKey, user));
 		
 		return user;
 	}
 
 	/**
 	 * Retrieve the <code>User</code> based on his/her email address. If the no
 	 * <code>User</code> exist for this email address or the user has been
 	 * delete. <code>NULL</code> is returned.
 	 * 
 	 * @param emailAddress
 	 *            the email address to search the user based on, must not be
 	 *            <code>NULL</code>
 	 * @return <code>User</code> corresponding to the email address, or
 	 *         <code>NULL</code> is no such user exist or the user has been
 	 *         deleted <code>NULL</code>
 	 */
 	public User getUserByEmail(String emailAddress) {
 		// the cache key for this method call;
 		String cacheKey = emailCache + emailAddress;
 
 		User user = null;
 		Element cacheEntry = myCache.get(cacheKey);
 		if (cacheEntry  != null) {
 			user = (User) cacheEntry.getValue();
 			return user;
 		}
 
 		// query the DB for the user object
 		PreparedStatement pstmt = null;
 		Connection con = null;
 		try {
 			con = getConnection();
 			pstmt = con.prepareStatement(sqlGetUserByEmail);
 			pstmt.setString(1, emailAddress);
 			ResultSet rs = pstmt.executeQuery();
 
 			user = createUserObject(rs);
 
 		} catch (SQLException sqle) {
 			System.out.println("SQL statement is invalid: " + pstmt);
 			sqle.printStackTrace();
 			throw new RuntimeException(sqle);
 		} finally {
 			closeConnection(con);
 		}
 
 		// put result into cache
 		myCache.put(new Element(cacheKey, user));
 		
 		return user;
 	}
 
 	/**
 	 * Delete the user with the id provided.
 	 * 
 	 * @param id
 	 *            the user id, must be > 0
 	 * @return <code>True</code> if delete is successful, <code>false</code>
 	 *         other wise.
 	 */
 	public synchronized boolean deleteUserById(long id) {
 
 		if (id <= 0) {
 			throw new IllegalStateException("Invalid user delete request");
 		}
 		
 		User delUser = getUserById(id);
 		
 		// clear out the cache entry for deleted user
 		revokeUserCacheById(id);
 		
 		PreparedStatement pstmt = null;
 		Connection con = null;
 		boolean deleteSuccessful = false;
 		
 		if (delUser != null) {
 			try {
 				con = getConnection();
 				pstmt = con.prepareStatement(sqlDeleteUserById);
 				String deletedEmail = delUser.getEmail() + " deleted_On:" + System.currentTimeMillis();
 				pstmt.setString(1, deletedEmail);
 				pstmt.setLong(2, id);
 				deleteSuccessful = pstmt.executeUpdate() > 0;
 				
 				if (deleteSuccessful) {
 					deleteSuccessful &= deleteDjangoUser(id, deletedEmail, con);
 				}
 	
 			} catch (SQLException sqle) {
 				System.out.println("SQL statement is invalid: " + pstmt);
 				sqle.printStackTrace();
 				throw new RuntimeException(sqle);
 			} finally {
 				closeConnection(con);
 			}
 		}
 		
 		return deleteSuccessful;
 	}
 
 
 	/**
 	 * Update the user information. Note all the field on the <code>User</code>
 	 * object must be set, if the field is not set, then the value in DB will be
 	 * set to <code>Null</code>
 	 * 
 	 * @param user
 	 * @return <code>true</code> if the user was updated successfully, <code>false</code> otherwise
 	 * 
 	 * @throws <code>com.parq.server.dao.exception.DuplicateEmailException</code> if
 	 *         the email already exist in the system, and is not tied to this user.
 	 */
 	public synchronized boolean updateUser(User user) {
 
 		if (user == null || user.getEmail() == null || user.getUserID() <= 0) {
 			throw new IllegalStateException("Invalid user update request");
 		}
 		
 		// test to make sure no duplicate email is used
 		User tempUser = getUserByEmail(user.getEmail());
 		if(tempUser != null && tempUser.getUserID() != user.getUserID()) {
 			throw new DuplicateEmailException("Email: " + user.getEmail() + " already exist");
 		}
 		
 		// clear out the cache entry for user that is going to be updated
		clearUserCache();
 		
 		PreparedStatement pstmt = null;
 		Connection con = null;
 		boolean updateSuccessful = false;
 		try {
 			con = getConnection();
 			pstmt = con.prepareStatement(sqlUpdateUser);
 			pstmt.setString(1, user.getPassword());
 			pstmt.setString(2, user.getEmail());
 			pstmt.setString(3, user.getPhoneNumber());
 			pstmt.setLong(4, user.getUserID());
 			updateSuccessful = pstmt.executeUpdate() > 0;
 			
 			if (updateSuccessful) {
 				updateSuccessful &= updateDjangoUser(user, con);
 			}
 
 		} catch (SQLException sqle) {
 			System.out.println("SQL statement is invalid: " + pstmt);
 			sqle.printStackTrace();
 			throw new RuntimeException(sqle);
 		} finally {
 			closeConnection(con);
 		}
 		
 		return updateSuccessful;
 	}
 
 
 	/**
 	 * Create a new user. Note all the field on the <code>User</code>, expect
 	 * the userId field object must be set, if any of the the fields is not set,
 	 * then the value in DB will be set to <code>Null</code>
 	 * 
 	 * @param user
 	 * @return <code>true</code> if the user was updated successfully,
 	 *         <code>false</code> otherwise
 	 * @throws <code>com.parq.server.dao.exception.DuplicateEmailException</code> if
 	 *         the email associated with this new user already exist.
 	 */
 	public synchronized boolean createNewUser(User user) {
 
 		if (user == null || user.getEmail() == null) {
 			throw new IllegalStateException("Invalid user create request");
 		}
 		// test to make sure no duplicate email is used
 		else if(getUserByEmail(user.getEmail()) != null) {
 			throw new DuplicateEmailException("Email: " + user.getEmail() + " already exist");
 		}
 		
 		// clear out the cache entry for user that is going to be updated
 		revokeCache(myCache, emailCache, user.getEmail());
 
 		PreparedStatement pstmt = null;
 		Connection con = null;
 		boolean newUserCreated = false;
 		try {
 			con = getConnection();
 			// create the auth_user table entry first before creating the user table entry
 			int djangoId = createDjangoUser(user, con);
 			if (djangoId > 0) {
 				pstmt = con.prepareStatement(sqlCreateUser);
 				pstmt.setString(1, user.getPassword());
 				pstmt.setString(2, user.getEmail());
 				pstmt.setString(3, user.getPhoneNumber());
 				pstmt.setInt(4, djangoId);
 				newUserCreated = pstmt.executeUpdate() == 1;
 			}
 
 		} catch (SQLException sqle) {
 			System.out.println("SQL statement is invalid: " + pstmt);
 			sqle.printStackTrace();
 			throw new RuntimeException(sqle);
 		} finally {
 			closeConnection(con);
 		}
 		
 		return newUserCreated;
 	}
 	
 	/**
 	 * Revoke all the cache instance of this User by id and email address.
 	 * @param userID
 	 */
 	private synchronized void revokeUserCacheById(long userID) {
 		if (userID < 0) {
 			return;
 		}
 		User user = getUserById(userID);
 		
 		revokeCache(myCache, idCache, "" + userID);
 		if (user != null) {
 			revokeCache(myCache, emailCache, user.getEmail());
 		}
 	}
 
 	/**
 	 * manually clear out the cache
 	 * @return
 	 */
 	public boolean clearUserCache() {
 		myCache.removeAll();
 		return true;
 	}
 	
 	/**
 	 * Secondary query to be with the createUser method to keep the Django auth_user table in sync
 	 */
 	private int createDjangoUser(User user, Connection con) throws SQLException {
 		int DjangoAuthUserId = -1;
 		PreparedStatement pstmt = null;
 		// create the new auth_user
 		pstmt = con.prepareStatement(sqlDjangoCreateAuthUser);
 		pstmt.setString(1, user.getEmail());
 		pstmt.setString(2, user.getEmail());
 		pstmt.setString(3, user.getPassword());
 		boolean newUserCreated = pstmt.executeUpdate() == 1;
 		// get the new auth_user's id
 		if (newUserCreated) {
 			pstmt = con.prepareStatement(sqlGetNewDjangoAuthUserId);
 			pstmt.setString(1, user.getEmail());
 			ResultSet rs = pstmt.executeQuery();
 			if (rs != null && rs.isBeforeFirst()) {
 				rs.next();
 				DjangoAuthUserId = rs.getInt("id");
 			}
 			else {
 				throw new RuntimeException("Invalid id from auth_user table");
 			}
 		}
 		return DjangoAuthUserId;
 	}
 
 	/**
 	 * Secondary query to be with the updateUser method to keep the Django auth_user table insync
 	 */
 	private boolean updateDjangoUser(User user, Connection con) throws SQLException {
 		PreparedStatement pstmt = con.prepareStatement(sqlDjangoUpdateAuthUser);
 		pstmt.setString(1, user.getEmail());
 		pstmt.setString(2, user.getEmail());
 		pstmt.setString(3, user.getPassword());
 		boolean updateSuccessful = pstmt.executeUpdate() > 0;
 		return updateSuccessful;
 	}
 	
 	/**
 	 * Secondary query to be with the deleteUser method to keep the Django auth_user table insync
 	 */
 	private boolean deleteDjangoUser(long id, String deletedEmail, Connection con) throws SQLException {
 		PreparedStatement pstmt = con.prepareStatement(sqlDjangoDeleteAuthUser);
 		pstmt.setString(1, deletedEmail);
 		pstmt.setString(2, deletedEmail);
 		pstmt.setLong(3, id);
 		boolean deleteSuccessful = pstmt.executeUpdate() > 0;
 		return deleteSuccessful;
 	}
 }
