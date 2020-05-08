 package tracker.accounts;
 
 import play.Logger;
 import play.exceptions.UnexpectedException;
 import play.libs.Codec;
 import tracker.Config;
 import tracker.DatabaseFactory;
 import tracker.cache.AccountStatsCache;
 import tracker.cache.AccountsCache;
 import tracker.cache.ActiveTorrentsCache;
 import tracker.cache.ICache;
 import tracker.util.Utils;
 
 import java.io.Serializable;
 import java.security.MessageDigest;
 import java.sql.*;
 
 public class Account implements Serializable, ICache {
 	protected Long id;
 	protected String login;
 	protected String passwordHash;
 	protected String email;
 	protected String passkey;
 	protected String displayName;
 	protected Timestamp registrationDate;
 	protected Timestamp lastActivityDate;
 
 	protected volatile ProfileData profileData;
 
 	public Account() { }
 
 	public void save() throws Throwable {
 		String query =
 				"INSERT INTO accounts (`id`, `login`, `password_hash`, `email`, `passkey`, `display_name`, `registration_date`, `last_activity_date`, `profile_data`) " +
 				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
 				"ON DUPLICATE KEY UPDATE " +
 					"`login` = VALUES(`login`)," +
 					"`password_hash` = VALUES(`password_hash`)," +
 					"`email` = VALUES(`email`)," +
 					"`passkey` = VALUES(`passkey`)," +
 					"`display_name` = VALUES(`display_name`)," +
 					"`registration_date` = VALUES(`registration_date`)," +
 					"`last_activity_date` = VALUES(`last_activity_date`)," +
 					"`profile_data` = VALUES(`profile_data`)";
 
 		Connection connection = null;
 		PreparedStatement statement = null;
 
 		try {
 			connection = DatabaseFactory.getInstance().getConnection();
 			statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
 
			statement.setLong(1, this.getId());
 			statement.setString(2, this.getLogin());
 			statement.setString(3, this.getPasswordHash());
 			statement.setString(4, this.getEmail());
 			statement.setString(5, this.getPasskey());
 			statement.setString(6, this.getDisplayName());
 			statement.setTimestamp(7, this.getRegistrationDate());
 			statement.setTimestamp(8, this.getLastActivityDate());
 			statement.setString(9, this.getProfileData().toString());
 			statement.execute();
 
 			if (this.getId() == null) {
 				ResultSet keys = statement.getGeneratedKeys();
 				if (keys.next()) {
 					this.setId(keys.getLong(1));
 				}
 			}
 		} finally {
 			DatabaseFactory.close(connection, statement);
 		}
 
 		AccountsCache.getInstance().put(this);
 	}
 
 	public Long getId() { return this.id; }
 	public void setId(Long id) { this.id = id; }
 
 	public String getLogin() { return this.login; }
 	public void setLogin(String login) { this.login = login; }
 
 	public String getPasswordHash() { return this.passwordHash; }
 	public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
 
 	public void setPassword(String password) { this.passwordHash = hashPassword(this.getLogin(), password); }
 
 	public String getEmail() { return this.email; }
 	public void setEmail(String email) { this.email = email; }
 
 	public String getPasskey() { return this.passkey; }
 	public void setPasskey(String passkey) { this.passkey = passkey; }
 
 	public String getDisplayName() { return this.displayName; }
 	public void setDisplayName(String displayName) { this.displayName = displayName; }
 
 	public Timestamp getRegistrationDate() { return this.registrationDate; }
 //	public String getRegistrationDateFormatted(String pattern) { return (new SimpleDateFormat(pattern)).format(this.getRegistrationDate()); }
 	public void setRegistrationDate(Timestamp registrationDate) { this.registrationDate = registrationDate; }
 
 	public Timestamp getLastActivityDate() { return this.lastActivityDate; }
 //	public String getLastActivityDateFormatted(String pattern) { return (new SimpleDateFormat(pattern)).format(this.getLastActivityDate()); }
 	public void setLastActivityDate(Timestamp lastActivityDate) { this.lastActivityDate = lastActivityDate; }
 
 	public synchronized ProfileData getProfileData() {
 		if (this.profileData == null) {
 			this.profileData = new ProfileData();
 		}
 		return this.profileData;
 	}
 	public void setProfileData(ProfileData profileData) { this.profileData = profileData; }
 
 	public ActiveTorrents getActiveTorrents() throws Throwable {
 		return ActiveTorrentsCache.getInstance().getByAccountId(this.getId());
 	}
 
 	public AccountStats getStats() throws Throwable {
 		return AccountStatsCache.getInstance().getByAccountId(this.getId());
 	}
 
 	public String getGravatarUrl(Integer size) {
 		if (this.getEmail() == null) {
 			return null;
 		}
 
 		String hash = Codec.hexMD5(this.getEmail().toLowerCase().trim());
 		return String.format("http://www.gravatar.com/avatar/%s?s=%d", hash, size);
 	}
 
 	/**
 	 * Задать новый уникальный passkey.
 	 */
 	public String createPasskey() throws Throwable {
 		byte[] hashBytes;
 		try {
 			byte[] passkeyBytes = ("" + System.currentTimeMillis() + Utils.getRandom().nextLong()).getBytes("UTF-8");
 			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
 			hashBytes = messageDigest.digest(passkeyBytes);
 		} catch (Exception exception) {
 			throw new UnexpectedException(exception);
 		}
 
 		String passkey = Utils.getHexString(hashBytes);
 
 		String query = "SELECT COUNT(*) FROM `accounts` WHERE `passkey` = ?";
 
 		Connection connection = null;
 		PreparedStatement statement = null;
 		try {
 			connection = DatabaseFactory.getInstance().getConnection();
 			statement = connection.prepareStatement(query);
 			statement.setString(1, passkey);
 
 			ResultSet resultSet = statement.executeQuery();
 			if (!resultSet.next() || resultSet.getInt(1) > 0) {
 				return this.createPasskey();
 			}
 		} finally {
 			DatabaseFactory.close(connection, statement);
 		}
 
 		this.setPasskey(passkey);
 
 		return this.getPasskey();
 	}
 
 	public String toString() {
 		return this.getLogin() + " [" + this.getId() + "]";
 	}
 
 	public boolean equals(Object object) {
 		if (object == null)
 			return false;
 		if (object == this)
 			return true;
 		if (object.getClass() != this.getClass())
 			return false;
 
 		return this.getId().equals(((Account)object).getId());
 	}
 
 	public String[] getCacheKeys() {
 		return new String[] {
 			"id-" + this.getId().toString(),
 			"login-" + this.getLogin(),
 			"passkey-" + this.getPasskey(),
 		};
 	}
 
 	public static Account byId(Long id) throws Throwable {
 		if (id == null || id < 1) {
 			return null;
 		}
 
 		String condition = String.format("id = %d", id);
 		return byCondition(condition);
 	}
 
 	public static Account byLogin(String login) throws Throwable {
 		String condition = String.format("login = \"%s\"", login);
 		return byCondition(condition);
 	}
 
 	public static Account byEmail(String email) throws Throwable {
 		String condition = String.format("email = \"%s\"", email);
 		return byCondition(condition);
 	}
 
 	public static Account byPasskey(String passkey) throws Throwable {
 		String condition = String.format("passkey = \"%s\"", passkey);
 		return byCondition(condition);
 	}
 
 	public static Account byDisplayName(String displayName) throws Throwable {
 		String condition = String.format("display_name = \"%s\"", displayName);
 		return byCondition(condition);
 	}
 
 	public static Account byCondition(String condition) throws Throwable {
 		Logger.debug("Load account by condition " + condition);
 		String query = "SELECT " +
 				"`id`, `login`, `password_hash`, `email`, `passkey`, `display_name`, `registration_date`, `last_activity_date`, `profile_data` " +
 				"FROM `accounts` WHERE " + condition;
 
 		Account account = null;
 		Connection connection = null;
 		PreparedStatement statement = null;
 		try {
 			connection = DatabaseFactory.getInstance().getConnection();
 			statement = connection.prepareStatement(query);
 
 			ResultSet resultSet = statement.executeQuery();
 			if (!resultSet.next()) {
 				return null;
 			}
 
 			account = new Account();
 			account.setId(resultSet.getLong("id"));
 			account.setLogin(resultSet.getString("login"));
 			account.setPasswordHash(resultSet.getString("password_hash"));
 			account.setEmail(resultSet.getString("email"));
 			account.setPasskey(resultSet.getString("passkey"));
 			account.setDisplayName(resultSet.getString("display_name"));
 			account.setRegistrationDate(resultSet.getTimestamp("registration_date"));
 			account.setLastActivityDate(resultSet.getTimestamp("last_activity_date"));
 			account.setProfileData(new ProfileData(resultSet.getString("profile_data")));
 
 			resultSet.close();
 		} finally {
 			DatabaseFactory.close(connection, statement);
 		}
 
 		return account;
 	}
 
 	public static Account create(String login, String password, String email, String displayName) throws Throwable {
 		Account account = new Account();
 
 		account.setLogin(login);
 		account.setPassword(password);
 		account.setEmail(email);
 		account.setDisplayName(displayName);
 
 		account.createPasskey();
 		account.setRegistrationDate(new Timestamp(System.currentTimeMillis()));
 		account.setLastActivityDate(account.getRegistrationDate());
 
 		account.save();
 
 		Logger.info("Created new account: " + account);
 
 		return account;
 	}
 
 	public static String hashPassword(String login, String password) {
 		byte[] hashBytes;
 		try {
 			byte[] passwordBytes = (login + password + Config.getString("accounts.password.hash.salt")).getBytes("UTF-8");
 			MessageDigest messageDigest = MessageDigest.getInstance(Config.getString("accounts.password.hash.algorithm"));
 			hashBytes = messageDigest.digest(passwordBytes);
 		} catch (Exception exception) {
 			throw new UnexpectedException(exception);
 		}
 		return Utils.getHexString(hashBytes);
 	}
 }
