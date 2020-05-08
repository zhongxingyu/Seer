 /**
  * 
  */
 package DatabaseFrontend;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Set;
 
 import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
 
 public class User {
 
 	@Override
 	public String toString() {
 		return "User [contactInfo=" + contactInfo + ", email=" + email
 				+ ", id=" + id + ", passwordHash=" + passwordHash
 				+ ", permissions=" + permissions + ", salt=" + salt
 				+ ", userName=" + userName + ", addr="
 				+ super.toString().split("@")[1] + "]";
 	}
 
 	private static final String CONTACT_INFO_FIELD_NAME = "ContactInfo";
 	private static final String EMAIL_FIELD_NAME = "email";
 	private static final String ID_FIELD_NAME = "Id";
 	private static final String PASSWORD_HASH_FIELD_NAME = "PasswordHash";
 	private static final String PERMISSION_LEVEL_FIELD_NAME = "PermissionLevel";
 	private static final String SALT_FIELD_NAME = "Salt";
 	private static final String TABLE_NAME = "Users";
 	private static final String USER_NAME_FIELD_NAME = "UserName";
 
 	private static GarbageCollectingConcurrentMap<Long, User> userCache = new GarbageCollectingConcurrentMap<Long, User>();
 
 	private static User getUserFromCache(final ResultSet res)
 			throws SQLException {
 		final long userID = res.getLong("id");
 		User u = userCache.get(userID);
 		if (u == null)
 			u = userCache.getOrPut(userID, new User(res));
 		return u;
 	}
 
 	public static User getUserFromEmail(final String email) throws SQLException {
 		final String sql = "select * from " + TABLE_NAME + " where "
 				+ EMAIL_FIELD_NAME + " = ?;";
 		final PreparedStatement st = DataManager.getCon().prepareStatement(sql);
 		final User u;
 		try {
 			st.setString(1, email);
 			final ResultSet res = st.executeQuery();
 			if (!res.next())
 				return null;
 			u = getUserFromCache(res);
 		} finally {
 			try {
 				if (st != null)
 					st.close();
 			} catch (final SQLException e) {
 				System.out.println("Error closing prepared statement : "
 						+ e.getMessage());
 			}
 		}
 		return u;
 	}
 
 	public static User getUserFromID(final long userID) throws SQLException {
 		final String sql = "select * from " + TABLE_NAME + " where "
 				+ ID_FIELD_NAME + " = ?;";
 		final PreparedStatement st = DataManager.getCon().prepareStatement(sql);
 		final User u;
 		try {
 			st.setLong(1, userID);
 			final ResultSet res = st.executeQuery();
 			if (!res.next())
 				return null;
 			u = getUserFromCache(res);
 		} finally {
 			try {
 				if (st != null)
 					st.close();
 			} catch (final SQLException e) {
 				System.out.println("Error closing prepared statement : "
 						+ e.getMessage());
 			}
 		}
 		return u;
 	}
 
 	public static User getUserFromUserName(final String userName)
 			throws SQLException {
 		final String sql = "select * from " + TABLE_NAME + " where "
 				+ USER_NAME_FIELD_NAME + " = ?;";
 		final PreparedStatement st = DataManager.getCon().prepareStatement(sql);
 		final User u;
 		try {
 			st.setString(1, userName);
 			final ResultSet res = st.executeQuery();
 			if (!res.next())
 				return null;
 			u = getUserFromCache(res);
 		} finally {
 			try {
 				if (st != null)
 					st.close();
 			} catch (final SQLException e) {
 				System.out.println("Error closing prepared statement : "
 						+ e.getMessage());
 			}
 		}
 		return u;
 	}
 
 	public static User makeUser(final String email, final String userName,
 			final String contactInfo, final String passwordHash,
 			final Set<Permission> permissions, String salt) throws SQLException {
 		final String sql = "insert into " + TABLE_NAME + " ("
 				+ EMAIL_FIELD_NAME + ", " + USER_NAME_FIELD_NAME + ", "
 				+ CONTACT_INFO_FIELD_NAME + ", " + PASSWORD_HASH_FIELD_NAME
 				+ ", " + SALT_FIELD_NAME + ", " + PERMISSION_LEVEL_FIELD_NAME
 				+ ") values (?, ?, ?, ?, ?, ?);";
 		final PreparedStatement st = DataManager.getCon().prepareStatement(sql,
 				Statement.RETURN_GENERATED_KEYS);
 		final long userId;
 		try {
 			st.setString(1, email);
 			st.setString(2, userName);
 			st.setString(3, contactInfo);
 			st.setString(4, passwordHash);
 			st.setString(5, salt);
 			st.setLong(6, Permission.toLong(permissions));
 			try {
 				final int res = st.executeUpdate();
 				if (res != 1)
 					return null;
 			} catch (MySQLIntegrityConstraintViolationException s) {
 				return null;
 			}
 			final ResultSet rs = st.getGeneratedKeys();
 			if (!rs.next())
 				throw new SQLException("No key returned for generated user "
 						+ sql);
 			userId = rs.getLong(1);
 
 		} finally {
 			try {
 				if (st != null)
 					st.close();
 			} catch (final SQLException e) {
 				System.out.println("Error closing prepared statement : "
 						+ e.getMessage());
 			}
 		}
 		final User toRet = getUserFromID(userId);
 		return toRet;
 	}
 
 	private String contactInfo;
 	private String email;
 	private final long id;
 	private String passwordHash;
 	private final Set<Permission> permissions;
 	private final String salt;
 	private String userName;
 
 	private User(final ResultSet res) throws SQLException {
 		id = res.getLong(ID_FIELD_NAME);
 		email = res.getString(EMAIL_FIELD_NAME);
 		userName = res.getString(USER_NAME_FIELD_NAME);
 		passwordHash = res.getString(PASSWORD_HASH_FIELD_NAME);
 		salt = res.getString(SALT_FIELD_NAME);
 		contactInfo = res.getString(CONTACT_INFO_FIELD_NAME);
 		permissions = Permission.fromLong(res
 				.getInt(PERMISSION_LEVEL_FIELD_NAME));
 	}
 
 	public synchronized boolean deletePermission(final Permission permission)
 			throws SQLException {
 		if (!permissions.remove(permission))
 			return false;
 		syncPermissions();
 		return true;
 	}
 
 	public synchronized boolean deleteUser() throws SQLException {
 		final String sql = "delete from " + TABLE_NAME + " where "
 				+ ID_FIELD_NAME + " = ?";
 		final PreparedStatement st = DataManager.getCon().prepareStatement(sql);
 		final int res;
 		try {
 			st.setLong(1, id);
 			res = st.executeUpdate();
 		} finally {
 			try {
 				if (st != null)
 					st.close();
 			} catch (final SQLException e) {
 				System.out.println("Error closing prepared statement : "
 						+ e.getMessage());
 			}
 		}
 		return res == 1;
 	}
 
 	public Set<Bid> getBidsMade() throws SQLException {
 		return Bid.getBidsByUser(this);
 	}
 
 	public String getContactInfo() {
 		return contactInfo;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
	public long getID() {
 		return id;
 	}
 
 	public Set<Item> getItemsSelling() throws SQLException {
 		return Item.getItemsByUserSelling(this);
 	}
 
 	public String getPasswordHash() {
 		return passwordHash;
 	}
 
 	public Set<Permission> getPermissions() {
 		return permissions;
 	}
 
 	public String getSalt() {
 		return salt;
 	}
 
 	public String getUserName() {
 		return userName;
 	}
 
 	public synchronized boolean setContactInfo(final String contactInfo)
 			throws SQLException {
 		this.contactInfo = contactInfo;
 		final String sql = "update " + TABLE_NAME + " set "
 				+ CONTACT_INFO_FIELD_NAME + " = ? where " + ID_FIELD_NAME
 				+ " = ?;";
 		final PreparedStatement st = DataManager.getCon().prepareStatement(sql);
 		final int res;
 		try {
 			st.setString(1, contactInfo);
 			st.setLong(2, id);
 			res = st.executeUpdate();
 		} finally {
 			try {
 				if (st != null)
 					st.close();
 			} catch (final SQLException e) {
 				System.out.println("Error closing prepared statement : "
 						+ e.getMessage());
 			}
 		}
 		return res == 1;
 	}
 
 	public synchronized boolean setEmail(final String email)
 			throws SQLException {
 		this.email = email;
 		final String sql = "update " + TABLE_NAME + " set " + EMAIL_FIELD_NAME
 				+ " = ? where " + ID_FIELD_NAME + " = ?;";
 		final PreparedStatement st = DataManager.getCon().prepareStatement(sql);
 		final int res;
 		try {
 			st.setString(1, email);
 			st.setLong(2, id);
 			try {
 				res = st.executeUpdate();
 			} catch (MySQLIntegrityConstraintViolationException s) {
 				return false;
 			}
 		} finally {
 			try {
 				if (st != null)
 					st.close();
 			} catch (final SQLException e) {
 				System.out.println("Error closing prepared statement : "
 						+ e.getMessage());
 			}
 		}
 		return res == 1;
 	}
 
 	public synchronized boolean setPasswordHash(final String passwordHash)
 			throws SQLException {
 		this.passwordHash = passwordHash;
 		final String sql = "update " + TABLE_NAME + " set "
 				+ PASSWORD_HASH_FIELD_NAME + " = ? where " + ID_FIELD_NAME
 				+ " = ?;";
 		final PreparedStatement st = DataManager.getCon().prepareStatement(sql);
 		final int res;
 		try {
 			st.setString(1, passwordHash);
 			st.setLong(2, id);
 			res = st.executeUpdate();
 		} finally {
 			try {
 				if (st != null)
 					st.close();
 			} catch (final SQLException e) {
 				System.out.println("Error closing prepared statement : "
 						+ e.getMessage());
 			}
 		}
 		return res == 1;
 	}
	
	public long getId() {
		return id;
	}
 
 	public synchronized boolean setPermission(final Permission permission)
 			throws SQLException {
 		if (!permissions.add(permission))
 			return false;
 		syncPermissions();
 		return true;
 	}
 
 	public synchronized boolean setUserName(final String userName)
 			throws SQLException {
 		this.userName = userName;
 		final String sql = "update " + TABLE_NAME + " set "
 				+ USER_NAME_FIELD_NAME + " = ? where " + ID_FIELD_NAME
 				+ " = ?;";
 		final PreparedStatement st = DataManager.getCon().prepareStatement(sql);
 		final int res;
 		try {
 			st.setString(1, userName);
 			st.setLong(2, id);
 			try {
 				res = st.executeUpdate();
 			} catch (MySQLIntegrityConstraintViolationException s) {
 				return false;
 			}
 		} finally {
 			try {
 				if (st != null)
 					st.close();
 			} catch (final SQLException e) {
 				System.out.println("Error closing prepared statement : "
 						+ e.getMessage());
 			}
 		}
 		return res == 1;
 	}
 
 	private synchronized void syncPermissions() throws SQLException {
 		final long permLong = Permission.toLong(permissions);
 		final String sql = "update " + TABLE_NAME + " set "
 				+ PERMISSION_LEVEL_FIELD_NAME + " = ? where " + ID_FIELD_NAME
 				+ " = ?;";
 		final PreparedStatement st = DataManager.getCon().prepareStatement(sql);
 		final int res;
 		try {
 			st.setLong(1, permLong);
 			st.setLong(2, id);
 			res = st.executeUpdate();
 		} finally {
 			try {
 				if (st != null)
 					st.close();
 			} catch (final SQLException e) {
 				System.out.println("Error closing prepared statement : "
 						+ e.getMessage());
 			}
 		}
 		assert (res == 1);
 	}
 }
