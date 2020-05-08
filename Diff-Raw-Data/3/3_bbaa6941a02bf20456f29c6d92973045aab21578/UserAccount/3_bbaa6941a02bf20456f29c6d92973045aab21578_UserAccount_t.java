 package com.untzuntz.ustack.data;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 import org.bson.types.ObjectId;
 import org.jasypt.salt.RandomSaltGenerator;
 import org.jasypt.util.password.StrongPasswordEncryptor;
 
 import com.Ostermiller.util.CSVParser;
 import com.Ostermiller.util.LabeledCSVParser;
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.BasicDBObjectBuilder;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 import com.untzuntz.ustack.aaa.ResourceDefinition;
 import com.untzuntz.ustack.aaa.ResourceLink;
 import com.untzuntz.ustack.aaa.RoleDefinition;
 import com.untzuntz.ustack.exceptions.AccountExistsException;
 import com.untzuntz.ustack.exceptions.ForgotPasswordLinkExpired;
 import com.untzuntz.ustack.exceptions.InvalidAccessAttempt;
 import com.untzuntz.ustack.exceptions.InvalidUserAccountName;
 import com.untzuntz.ustack.exceptions.PasswordException;
 import com.untzuntz.ustack.exceptions.PasswordLengthException;
 import com.untzuntz.ustack.exceptions.PasswordPriorException;
 import com.untzuntz.ustack.main.Msg;
 import com.untzuntz.ustack.main.UAppCfg;
 import com.untzuntz.ustack.main.UOpts;
 
 /**
  * A User Account
  * 
  * @author jdanner
  *
  */
 public class UserAccount extends UntzDBObject {
 
 	private static final long serialVersionUID = 1L;
 	private static Logger logger = Logger.getLogger(UserAccount.class);
 	
 	public static final String STATUS_DISABLED = "Disabled";
 	public static final String STATUS_ACTIVE = "Active";
 	
 	public String getCollectionName() { return "users"; }
 	
 	public static UserAccount getTestObject() {
 		
 		UserAccount u = new UserAccount();
 		
 		u.setUserName("testguy@untzuntz.com");
 		u.setFirstName("Test");
 		u.setLastName("Guy");
 		u.setAddress1("123 Main St.");
 		u.setAddress2("Suite 123");
 		u.setCity("San Francisco");
 		u.setState("California");
 		u.setCountry("United States");
 		u.setPostalCode("98401");
 		u.setTimeZone("Usa/Pacific");
 		u.setPrimaryEmail("testguy@untzuntz.com");
 		u.setPrimaryTelephone(new BasicDBObject("countryCode", "1").append("phoneNumber", "999-888-7777"));
 		
 		return u;
 		
 	}
 	
 	private UserAccount()
 	{
 		// setup basic values on account
 		put("created", new Date());
 		setStatus(STATUS_ACTIVE);
 	}
 	
 	public Date getCreated() {
 		return (Date)get("created");
 	}
 
 	public String getUserId() {
 		return get("_id") + "";
 	}
 
 	/** Gets the DB Collection for the UserAccount object */
 	public static DBCollection getDBCollection() {
 		return new UserAccount().getCollection();
 	}
 
 	/** Return the name of the database that houses the 'users' collection */
 	public static final String getDatabaseName() {
 		
 		if (UOpts.getString(UAppCfg.DATABASE_USERS_COL) != null)
 			return UOpts.getString(UAppCfg.DATABASE_USERS_COL);
 		
 		return UOpts.getAppName();
 		
 	}
 
 	/**
 	 * Generate a UserAccount object from the MongoDB object
 	 * @param user
 	 */
 	public UserAccount(DBObject user) {
 		super(user);
 	}
 
 	/** Sets the username */
 	private void setUserName(String userName)
 	{
 		userName = userName.toLowerCase();
 		put("userName", userName);
 	}
 
 	/** Returns the username */
 	public String getUserName()
 	{
 		return (String)get("userName");
 	}
 
 	/** first name + last name */
 	public String getFullName()
 	{
 		return getFirstName() + " " + getLastName();
 	}
 	
 	public void setFirstName(String val)
 	{
 		if (AuditLog.changed(get("firstName"), val))
 			AuditLog.log("core", "core", "ChangeInfo", new BasicDBObject("userName", getUserName()).append("type", "firstName").append("original", get("firstName")).append("new", val));
 
 		put("firstName", val);
 	}
 	
 	public String getFirstName()
 	{
 		return getString("firstName");
 	}
 	
 	public void setMiddleInitial(String val)
 	{
 		if (AuditLog.changed(get("middleInit"), val))
 			AuditLog.log("core", "core", "ChangeInfo", new BasicDBObject("userName", getUserName()).append("type", "middleInit").append("original", get("middleInit")).append("new", val));
 		
 		if (val == null || val.length() == 0)
 			removeField("middleInit");
 		else
 			put("middleInit", val);
 	}
 
 	public String getMiddleInitial()
 	{
 		return getString("middleInit");
 	}
 
 	public void setLastName(String val)
 	{
 		if (AuditLog.changed(get("lastName"), val))
 			AuditLog.log("core", "core", "ChangeInfo", new BasicDBObject("userName", getUserName()).append("type", "lastName").append("original", get("lastName")).append("new", val));
 		
 		put("lastName", val);
 	}
 
 	public String getLastName()
 	{
 		return getString("lastName");
 	}
 
 	public void setSalutation(String val)
 	{
 		if (AuditLog.changed(get("salutation"), val))
 			AuditLog.log("core", "core", "ChangeInfo", new BasicDBObject("userName", getUserName()).append("type", "salutation").append("original", get("salutation")).append("new", val));
 
 		if (val == null || val.length() == 0)
 			removeField("salutation");
 		else
 			put("salutation", val);
 	}
 	
 	public String getSalutation()
 	{
 		return getString("salutation");
 	}
 	
 	public void setSuffix(String val)
 	{
 		if (AuditLog.changed(get("suffix"), val))
 			AuditLog.log("core", "core", "ChangeInfo", new BasicDBObject("userName", getUserName()).append("type", "suffix").append("original", get("suffix")).append("new", val));
 
 		if (val == null || val.length() == 0)
 			removeField("suffix");
 		else
 			put("suffix", val);
 	}
 
 	public String getSuffix()
 	{
 		return getString("suffix");
 	}
 	
 	public String getTimeZone()
 	{
 		String tz = getString("timeZone");
 		if (tz == null || tz.length() == 0)
 			return "Etc/GMT-0";
 		
 		return tz;
 	}
 	
 	public void setTimeZone(String tz)
 	{
 		if (AuditLog.changed(get("timeZone"), tz))
 			AuditLog.log("core", "core", "ChangeInfo", new BasicDBObject("userName", getUserName()).append("type", "timeZone").append("original", get("timeZone")).append("new", tz));
 		
 		if (tz != null && tz.length() > 0)
 			put("timeZone", tz);
 		else
 			removeField("timeZone");
 	}
 	
 	public String getCountry()
 	{
 		return getString("country");
 	}
 	
 	public void setCountry(String c)
 	{
 		if (AuditLog.changed(get("country"), c))
 			AuditLog.log("core", "core", "ChangeInfo", new BasicDBObject("userName", getUserName()).append("type", "country").append("original", get("country")).append("new", c));
 
 		if (c != null && c.length() > 0)
 			put("country", c);
 		else
 			removeField("country");
 	}
 	
 	public boolean isPayOnReceive()
 	{
 		if ("true".equalsIgnoreCase(getString("payOnReceive")))
 			return true;
 		
 		return false;
 	}
 	
 	public void setPayOnReceive(boolean pay)
 	{
 		put("payOnReceive", pay);
 	}
 	
 	public String getCreditAccount()
 	{
 		return getString("creditAccountId");
 	}
 	
 	public void setCreditAccount(String c)
 	{
 		if (AuditLog.changed(get("creditAccountId"), c))
 			AuditLog.log("core", "core", "ChangeInfo", new BasicDBObject("userName", getUserName()).append("type", "creditAccountId").append("original", get("creditAccountId")).append("new", c));
 		
 		if (c != null && c.length() > 0)
 			put("creditAccountId", c);
 		else
 			removeField("creditAccountId");
 	}
 	
 	public String getAddress1()
 	{
 		return getString("address1");
 	}
 	
 	public void setAddress1(String data)
 	{
 		if (AuditLog.changed(get("address1"), data))
 			AuditLog.log("core", "core", "ChangeInfo", new BasicDBObject("userName", getUserName()).append("type", "address1").append("original", get("address1")).append("new", data));
 		
 		if (data != null && data.length() > 0)
 			put("address1", data);
 		else
 			removeField("address1");
 	}
 	
 	public String getAddress2()
 	{
 		return getString("address2");
 	}
 	
 	public void setAddress2(String data)
 	{
 		if (AuditLog.changed(get("address2"), data))
 			AuditLog.log("core", "core", "ChangeInfo", new BasicDBObject("userName", getUserName()).append("type", "address2").append("original", get("address2")).append("new", data));
 		
 		if (data != null && data.length() > 0)
 			put("address2", data);
 		else
 			removeField("address2");
 	}
 	
 	public String getCity()
 	{
 		return getString("city");
 	}
 	
 	public void setCity(String data)
 	{
 		if (AuditLog.changed(get("city"), data))
 			AuditLog.log("core", "core", "ChangeInfo", new BasicDBObject("userName", getUserName()).append("type", "city").append("original", get("city")).append("new", data));
 		
 		if (data != null && data.length() > 0)
 			put("city", data);
 		else
 			removeField("city");
 	}
 	
 	public String getState()
 	{
 		return getString("state");
 	}
 	
 	public void setState(String data)
 	{
 		if (AuditLog.changed(get("state"), data))
 			AuditLog.log("core", "core", "ChangeInfo", new BasicDBObject("userName", getUserName()).append("type", "state").append("original", get("state")).append("new", data));
 		
 		if (data != null && data.length() > 0)
 			put("state", data);
 		else
 			removeField("state");
 	}
 
 	public String getPostalCode()
 	{
 		return getString("postalCode");
 	}
 	
 	public void setPostalCode(String data)
 	{
 		if (AuditLog.changed(get("postalCode"), data))
 			AuditLog.log("core", "core", "ChangeInfo", new BasicDBObject("userName", getUserName()).append("type", "postalCode").append("original", get("postalCode")).append("new", data));
 		
 		if (data != null && data.length() > 0)
 			put("postalCode", data);
 		else
 			removeField("postalCode");
 	}
 	
 	/**
 	 * Returns the total number of user accounts
 	 * 
 	 * @return
 	 */
 	public static long getAccountCount()
 	{
 		return MongoDB.getCollection(getDatabaseName(), "users").count();
 	}
 
 	/**
 	 * Increase failed password count and lock account if necessary
 	 */
 	public void increasePasswordErrorCount()
 	{
 		Integer errCnt = (Integer)get("passwordErrorCount");
 		if (errCnt == null)
 			errCnt = new Integer(0);
 		
 		errCnt++;
 		put("passwordErrorCount", errCnt);
 		
 		logger.info("Password Error Cout: " + errCnt);
 
 		AuditLog.log("core", UOpts.SUBSYS_AUTH, "PasswordError", new BasicDBObject("userName", getUserName()).append("errorCount", errCnt));
 
 		if (errCnt >= UOpts.getInt(UAppCfg.PASSWORD_ERROR_LIMIT)) // we hit the max - lock it up!
 			lockAccount();
 		
 		save(UOpts.SUBSYS_AUTH);
 	}
 
 	/** Resets the password error count value */
 	public void resetPasswordErrorCount()
 	{
 		removeField("passwordErrorCount");
 		save(UOpts.SUBSYS_AUTH);
 		
 		AuditLog.log("core", UOpts.SUBSYS_AUTH, "PasswordErrorCountReset", new BasicDBObject("userName", getUserName()));
 	}
 	
 	/** Returns the number of failed password attempts */
 	public int getPasswordErrorCount()
 	{
 		if (get("passwordErrorCount") == null)
 			return 0;
 		
 		return (Integer)get("passwordErrorCount");
 	}
 	
 	/**
 	 * Locks the user account for the configed amount of time
 	 */
 	public void lockAccount()
 	{
 		int lockSec = UOpts.getInt(UAppCfg.USER_ACCOUNT_LOCKTIME_SEC);
 		Calendar now = Calendar.getInstance();
 		now.add(Calendar.SECOND, lockSec);
 		put("locked", now.getTime());		
 		
 		AuditLog.log("core", UOpts.SUBSYS_AUTH, "LockedAccount", new BasicDBObject("userName", getUserName()).append("lockedUntil", now.getTime()));
 	}
 	
 	/**
 	 * Salts, encrypts and stores password
 	 * 
 	 * @param password
 	 */
 	public void setPassword(String actor, String password) throws PasswordException
 	{
 		if (password.length() < UOpts.getInt(UAppCfg.PASSWORD_MIN_LENGTH)) // verify password length
 		{
 			AuditLog.log("core", actor, "SetPasswordError", new BasicDBObject("userName", getUserName()).append("reason", String.format("Minimum Length (%d) not met - attempted %d", UOpts.getInt(UAppCfg.PASSWORD_MIN_LENGTH), password.length())));
 			throw new PasswordLengthException(UOpts.getInt(UAppCfg.PASSWORD_MIN_LENGTH));
 		}
 		
 		if (isDisabled())
 		{
 			AuditLog.log("core", actor, "SetPasswordError", new BasicDBObject("userName", getUserName()).append("reason", "Account is disabled"));
 			return;
 		}
 		
 		/*
 		 * Check for prior password usage
 		 */
 		StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();
 		BasicDBList past = (BasicDBList)get("past");
 		for (int i = 0; past != null && i < past.size(); i++)
 		{
 			DBObject p = (DBObject)past.get(i);
 			
 			if (encryptor.checkPassword(p.get("s") + password, (String)p.get("p")))
 			{
 				AuditLog.log("core", actor, "SetPasswordError", new BasicDBObject("userName", getUserName()).append("reason", String.format("User tried to set password to a prior password [idx: %d]", i)));
 				throw new PasswordPriorException();
 			}
 		}
 
 		/*
 		 * Update prior password listing
 		 */
 		if (past == null)
 			past = new BasicDBList();
 
 		past.add(new BasicDBObject("s", get("salt")).append("p", get("password")));
 		
 		if (past.size() > 5)
 			past.remove(0);
 		
 		put("past", past);
 
 		
 		// salt + password setup
 		RandomSaltGenerator rsg = new RandomSaltGenerator();
 		String saltStr = new String(rsg.generateSalt(10));
 		
 		int passwordLength = password.length();
 		
 		put("salt", saltStr);
 		password = saltStr + password; // salt the password for enhanced one-way hash
 		
 		String encPassword = encryptor.encryptPassword(password); // encrypt
 		put("password", encPassword);
 		put("passwordChangeDate", new Date());
 
 		
 		AuditLog.log("core", UOpts.SUBSYS_AUTH, "SetPassword", new BasicDBObject("userName", getUserName()).append("length", passwordLength));
 
 		setPasswordExpiration();
 		
 		// clear locking data
 		unlock();
 	}
 
 	/** Returns the date the password was last changed (or set) */
 	public Date getPasswordChangeDate()
 	{
 		return (Date)get("passwordChangeDate");
 	}
 
 	/** Returns the date the password will expire */
 	public Date getPasswordExpirationDate()
 	{
 		return (Date)get("passwordExpirationDate");
 	}
 
 	/** Forces the password expiration date to 'now' */
 	public void expirePassword(String actor)
 	{
 		// TODO: Implement audit log
 		put("passwordExpirationDate", new Date());
 	}
 	
 	/** Sets the password expiration date based on the application settings */
 	private Date setPasswordExpiration()
 	{
 		// setup a password expiration date if applicable by config
 		int pwExpDays = UOpts.getInt(UAppCfg.PASSWORD_EXPIRATION_IN_DAYS);
 		if (pwExpDays > 0)
 		{
 			Calendar now = Calendar.getInstance();
 			now.add(Calendar.DAY_OF_YEAR, pwExpDays);
 			put("passwordExpirationDate", now.getTime());
 			
 			logger.info("Setting Password Expiration to '" + now.getTime() + "' for user '" + getUserName() + "' ==> " + pwExpDays + " days from now");
 			
 			AuditLog.log("core", UOpts.SUBSYS_AUTH, "SetPasswordExpiration", new BasicDBObject("userName", getUserName()).append("passwordExpirationDate", now.getTime()));
 			
 			return now.getTime();
 		}
 		else if (get("passwordExpirationDate") != null)
 			removeField("passwordExpirationDate");
 		
 		return null;
 	}
 	
 	/**
 	 * Clears the profile update flag
 	 */
 	public void resetProfileUpdateRequired()
 	{
 		removeField("profileUpdateRequired");
 	}
 
 	/**
 	 * Determines if the system should ask the user to update their profile info
 	 * @return
 	 */
 	public boolean isProfileUpdateRequired()
 	{
 		if ("true".equalsIgnoreCase(getString("profileUpdateRequired")))
 			return true;
 		
 		return false;
 	}
 	
 	/**
 	 * Determines if the user account is disabled by the user account status
 	 * @return
 	 */
 	public boolean isDisabled()
 	{
 		String status = getStatus();
 		
 		if (STATUS_DISABLED.equalsIgnoreCase(status))
 			return true;
 		
 		return false;
 	}
 
 	/**
 	 * Determines if the user's password has expired
 	 * @return
 	 */
 	public boolean isPasswordExpired()
 	{
 		Date expDate = (Date)get("passwordExpirationDate");
 		if (expDate == null) // will happen if the app user adds this feature after the fact
 		{
 			expDate = setPasswordExpiration();
 			save();
 		}
 		
 		if (expDate != null && expDate.before(new Date()))
 			return true;
 		
 		return false;
 	}
 
 	/** Sets the user's status */
 	public void setStatus(String status)
 	{
 		if (AuditLog.changed(get("status"), status))
 			AuditLog.log("core", "core", "ChangeUserStatus", new BasicDBObject("userName", getUserName()).append("oldStatus", getString("status")).append("newStatus", status));
 		
 		put("status", status);
 	}
 
 	/**
 	 * Returns the current user status
 	 * 
 	 * @return
 	 */
 	public String getStatus()
 	{
 		String status = (String)get("status");
 		if (status == null)
 			return STATUS_ACTIVE;
 		
 		return status;
 	}
 	
 	/**
 	 * Determine if the user account is currently locked
 	 * 
 	 * @return
 	 */
 	public boolean isLocked()
 	{
 		if (get("locked") == null)
 			return false;
 		
 		// the lock date is set to when the account will be unlocked, if it is before 'now' we are not locked
 		Date lockDate = (Date)get("locked");
 		if (lockDate.before(new Date()))
 		{
 			unlock();
 			return false;
 		}
 		
 		return true;
 	}
 
 	/**
 	 * Clears fields to set account to a unlocked state
 	 */
 	public void unlock()
 	{
		Date lockDate = (Date)get("locked");
		if (lockDate.before(new Date()))
 			AuditLog.log("core", UOpts.SUBSYS_AUTH, "UnlockedUserAccount", new BasicDBObject("userName", getUserName()));
 		
 		removeField("locked");
 		removeField("passwordErrorCount");
 		
 		// clear forgot password values
 		removeField("forgotPassExpiration");
 		removeField("forgotPassUid");		
 	}
 	
 	/**
 	 * Indicates the user has logged into the system, reset password failures and updates lastLogin value
 	 */
 	public void loggedIn()
 	{
 		unlock();
 		put("lastLogin", new Date());
 		save();
 		
 		AuditLog.log("core", UOpts.SUBSYS_AUTH, "Login", new BasicDBObject("userName", getUserName()));
 	}
 
 	public void loggedOut()
 	{
 		AuditLog.log("core", UOpts.SUBSYS_AUTH, "Logout", new BasicDBObject("userName", getUserName()).append("type", "explicit"));
 	}
 	
 	/**
 	 * Sets the name of server/host that the user was last logged into -- this is to help locate logs or other info if the app is running on multiple servers
 	 * @param hostName
 	 */
 	public void setLastLoginHost(String hostName)
 	{
 		logger.info("User '" + getUserName() + "' logged into host '" + hostName + "'");
 		
 		put("lastLoginHost", hostName);
 		save();
 		
 		AuditLog.log("core", UOpts.SUBSYS_AUTH, "LoginHost", new BasicDBObject("userName", getUserName()).append("host", hostName));
 	}
 
 	/**
 	 * Returns the primary email address associated with this user account
 	 * @return
 	 */
 	public String getPrimaryEmail()
 	{
 		return getString("primaryEmailAddress");
 	}
 	
 	/**
 	 * Sets the primary email address for this user account
 	 * @param email
 	 */
 	public void setPrimaryEmail(String email)
 	{
 		put("primaryEmailAddress", email);
 	}
 	
 	/**
 	 * Sets the address book value for this object
 	 * @param book
 	 */
 	public void setAddressBook(AddressBook book)
 	{
 		if (book == null)
 			removeField("addrBookId");
 		else
 			put("addrBookId", book.getAddressBookId());
 	}
 
 	/**
 	 * Returns the address book for this object
 	 * @return
 	 */
 	public AddressBook getAddressBook()
 	{
 		AddressBook ret = null;
 		String id = getString("addrBookId");
 
 		if (id == null)
 		{
 			try {
 				ret = AddressBook.getByUserId(getUserId());
 				ret.save();
 				setAddressBook(ret);
 				save();
 			} catch (Exception er) {
 				logger.error("General failure while trying to create an address book", er);
 			}
 		}
 		else
 			ret = AddressBook.getById( id );
 
 		return ret;
 	}
 	
 	public void setPrimaryTelephone(DBObject phone)
 	{
 		if (AuditLog.changed(get("primaryTelephone"), phone))
 			AuditLog.log("core", UOpts.SUBSYS_AUTH, "ChangePhoneNumber", new BasicDBObject("userName", getUserName()).append("original", get("primaryTelephone")).append("new", phone));
 
 		if (phone == null)
 			removeField("primaryTelephone");
 		else
 			put("primaryTelephone", phone);
 	}
 	
 	public DBObject getPrimaryTelephone() {
 		return (DBObject)get("primaryTelephone");
 	}
 	
 	public String getPrimaryTelephoneString()
 	{
 		DBObject phone = getPrimaryTelephone();
 		if (phone == null)
 			return null;
 		return (String)phone.get("countryCode") + " " + (String)phone.get("phoneNumber");
 	}
 	
 	public void setFaxNumber(DBObject phone)
 	{
 		if (AuditLog.changed(get("faxNumber"), phone))
 			AuditLog.log("core", UOpts.SUBSYS_AUTH, "ChangeFaxNumber", new BasicDBObject("userName", getUserName()).append("original", get("faxNumber")).append("new", phone));
 		
 		if (phone == null)
 			removeField("faxNumber");
 		else
 			put("faxNumber", phone);
 	}
 	
 	public DBObject getFaxNumber() {
 		return (DBObject)get("faxNumber");
 	}
 	
 	public String getFaxNumberString()
 	{
 		DBObject phone = getFaxNumber();
 		if (phone == null)
 			return null;
 		return (String)phone.get("countryCode") + " " + (String)phone.get("phoneNumber");
 	}
 	
 //	/**
 //	 * Sends a forgot password link via email to the user
 //	 */
 //	public void sendForgotPassword(String from) throws RequiredAccountDataMissingException,AddressException
 //	{
 //		if (getPrimaryEmail() == null)
 //			throw new RequiredAccountDataMissingException("Email Address");
 //		
 //		int fgpwExpireHours = UOpts.getInt(UAppCfg.PASSWORD_FORGOT_LINK_IN_HOURS);
 //		
 //		String linkUid = UUID.randomUUID().toString();
 //		StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();
 //		String encLink = encryptor.encryptPassword(linkUid); // encrypt
 //		
 //		Calendar now = Calendar.getInstance();
 //		now.add(Calendar.HOUR, fgpwExpireHours);
 //		put("forgotPassExpiration", now.getTime());
 //		put("forgotPassUid", encLink);
 //
 //		String url = "http://localhost:8080/TProject/setup/rdr?act=forgotpw&user=" + getUserName() + "&uid=" + linkUid;
 //		Emailer.postMail(getPrimaryEmail(), from, null, Msg.getString("PasswordReset-EmailSubject"), Msg.getString("PasswordReset-Email", url), null);
 //	}
 
 	/**
 	 * Determines if the provided uid is valid for this user
 	 * @param uid
 	 * @return
 	 * @throws InvalidAccessAttempt
 	 * @throws ForgotPasswordLinkExpired
 	 */
 	public boolean isValidForgotPassword(String uid) throws InvalidAccessAttempt,ForgotPasswordLinkExpired
 	{
 		StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();
 		if (!encryptor.checkPassword(uid, getString("forgotPassUid")))
 			throw new InvalidAccessAttempt();
 		
 		Date lockDate = (Date)get("forgotPassExpiration");
 		if (lockDate.before(new Date()))
 			throw new ForgotPasswordLinkExpired();
 		
 		return true;
 	}
 	
 
 	/** Sets the tos list */
 	private void setTermsConditions(BasicDBList list)
 	{
 		put("tosList", list);
 	}
 
 	/** Add a TOS (this indicates the user has accepted) */
 	public void addTOS(TermsConditions tos)
 	{
 		DBObject lTos = getTOS(tos.getName());
 		if (lTos == null)
 			lTos = new BasicDBObject();
 		
 		if (tos.getRenewalDays() > 0)
 		{
 			Calendar cal = Calendar.getInstance();
 			cal.add(Calendar.DAY_OF_YEAR, tos.getRenewalDays());
 			lTos.put("nextApproval", cal.getTime());
 		}
 
 		lTos.put("name", tos.getName());
 		lTos.put("approved", new Date());
 		
 		BasicDBList list = getTermsConditions();
 		boolean found = false;
 		for (int i = 0; i < list.size(); i++)
 		{
 			DBObject thisObj = (DBObject)list.get(i);
 			if (tos.getName().equalsIgnoreCase( (String)thisObj.get("name") ))
 			{
 				found = true;
 				list.set(i, lTos);
 			}
 		}
 		
 		if (!found)
 			list.add(lTos);
 		
 		setTermsConditions(list);
 	}
 
 	/** Returns a TOS Item */
 	public DBObject getTOS(String name)
 	{
 		BasicDBList list = getTermsConditions();
 		for (int i = 0; i < list.size(); i++)
 		{
 			DBObject tos = (DBObject)list.get(i);
 			if (name.equalsIgnoreCase( (String)tos.get("name") ))
 				return tos;
 		}
 		return null;
 	}
 
 	/**
 	 * returns 'true' if the tos has been agreed to, false otherwise
 	 * 
 	 * @param name
 	 * @return
 	 */
 	public boolean isTOSAgreed(String name)
 	{
 		DBObject tos = getTOS(name);
 		if (tos == null)
 			return false;
 		
 		Date napproval = (Date)tos.get("nextApproval");
 		if (napproval == null)
 			return true;
 			
 		if (napproval.before(new Date()))
 			return false;
 		
 		return true;
 	}
 	
 	/**
 	 * Returns a list of TermsConditions names that the user needs to complete
 	 * @param containerName
 	 * @return
 	 */
 	public List<String> getTermsConditionsRenewList()
 	{
 		Hashtable<String,String> tosRollup = new Hashtable<String,String>();
 		List<String> ret = new Vector<String>();
 		BasicDBList resourceLinkList = getResourceLinkList();
 		for (int i = 0; i < resourceLinkList.size(); i++)
 		{
 			ResourceLink link = new ResourceLink((DBObject)resourceLinkList.get(i));
 			ResourceDefinition def = ResourceDefinition.getByName( link.getName() );
 			if (def != null)
 			{
 				RoleDefinition role = def.getRoleByName(link.getRoleName());
 	
 				if (role != null)
 				{
 					BasicDBList tosList = role.getTOSList();
 					for (int j = 0; j < tosList.size(); j++)
 						tosRollup.put( (String)tosList.get(j), "T" );
 				}
 			}
 		}
 
 		// check if user has agreed to all tos agreements
 		Enumeration<String> enu = tosRollup.keys();
 		while (enu.hasMoreElements())
 		{
 			String tosName = enu.nextElement();
 			if (!isTOSAgreed(tosName))
 				ret.add(tosName);
 		}
 		
 		return ret;
 	}
 	
 	public static UserAccount getByAPIMapping(String name, String userId)
 	{
 		BasicDBList items = new BasicDBList();
 		items.add(new BasicDBObject("name", name));
 		items.add(new BasicDBObject("userId", userId));
 		DBObject match = new BasicDBObject("$elemMatch", items);
 		DBObject search = new BasicDBObject("apiMappingList", match);
 		logger.info("getByAPIMapping -> Search: " + search);
 		
 		DBObject user = new UserAccount().getCollection().findOne(search);
 		
 		if (user == null)
 			return null;
 		
 		return new UserAccount(user);
 	}
 	
 
 	/** Find a user by a field on the account */
 	public static UserAccount getByField(String field, String value)
 	{
 		DBObject search = BasicDBObjectBuilder.start(field, value).get();
 		logger.info("getByField -> Search: " + search);
 		
 		DBObject user = new UserAccount().getCollection().findOne(search);
 		
 		if (user == null)
 			return null;
 		
 		return new UserAccount(user);
 	}
 	
 	public static UserAccount createInMemoryUser(String userName)
 	{
 		UserAccount user = new UserAccount();
 		user.setUserName(userName);
 		user.put("doNotSave", "true");
 		return user;
 	}
 	
 	/**
 	 * Create a new user account
 	 * 
 	 * @param userName
 	 * @param password
 	 * @return
 	 * @throws AccountExistsException
 	 * @throws PasswordLengthException
 	 */
 	public static UserAccount createUser(String actor, String userName, String password) throws AccountExistsException,PasswordException
 	{
 		if (userName == null || userName.length() == 0)
 			throw new InvalidUserAccountName(Msg.getString("Invalid-UserName"));
 
 		userName = userName.toLowerCase().trim();
 		
 		UserAccount user = getUser(userName);
 		if (user != null) // already exists
 			throw new AccountExistsException("User");
 		
 		// create the actual account
 		user = new UserAccount();
 		user.put("createdBy", actor);
 		user.setUserName(userName);
 		user.setPassword(actor, password);
 		logger.info("Creating user account '" + userName + "'");
 		
 		AuditLog.log("core", actor, "CreateUser", new BasicDBObject("userName", userName));
 
 		return user;
 	}
 
 	public static UserAccount getByAPIToken(String clientId, String token) {
 	
 		if (clientId == null || clientId.length() == 0 || token == null || token.length() == 0)
 			return null;
 
 		clientId = clientId.toLowerCase().trim();
 		
 		DBObject elemMatch = new BasicDBObject("$elemMatch", new BasicDBObject("name", clientId).append("t", token) );
 
 		DBObject user = null;
 		try {
 			user = new UserAccount().getCollection().findOne(BasicDBObjectBuilder.start("apiMappingList", elemMatch).get());
 		} catch (Exception exp) { 
 			return null;
 		}
 		
 		if (user == null)
 			return null;
 		
 		return new UserAccount(user);
 
 	}
 	
 	/**
 	 * Get a user account by name
 	 * 
 	 * @param userName
 	 * @return
 	 */
 	public static UserAccount getUser(String userName)
 	{
 		if (userName == null || userName.length() == 0)
 			return null;
 		
 		userName = userName.toLowerCase().trim();
 		
 		DBObject user = null;
 		try {
 			user = new UserAccount().getCollection().findOne(BasicDBObjectBuilder.start("userName", userName).get());
 		} catch (Exception exp) { 
 			return null;
 		}
 		
 		if (user == null)
 			return null;
 		
 		return new UserAccount(user);
 	}
 	
 	public final static String[] SULATATIONS = new String[] { "Mr.", "Mrs.", "Ms.", "Dr.", "Drs." };
 	public final static String[] SUFFIXS = new String[] { "D.C.", "D.O.", "J.D.", "M.D.", "Ph.D.", "D.P.M." };
 	
 	
 	/**
 	 * Import Data
 	 * 
 	 * @param col
 	 * @param in
 	 * @throws IOException
 	 */
 	public static int importData(DBCollection col, InputStream in, String actor) throws Exception
 	{
 		LabeledCSVParser myParser = new LabeledCSVParser( new CSVParser(in) );
 		
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
 		String year = sdf.format(new Date());
 
 		Country usa = Country.getCountryByName("United States");
 		Hashtable<String,ResourceDefinition> defs = new Hashtable<String,ResourceDefinition>();
 		
 		SiteAccount check = null;
 		
 		int cnt = 0;
 		while(myParser.getLine() != null)
 		{
 			logger.info("Importing " +  cnt + "...");
 
 			String siteId = myParser.getValueByLabel("siteId");
 			if (check == null && siteId != null && siteId.length() > 0)
 			{
 				check = SiteAccount.getSiteById(siteId);
 				if (check == null)
 					throw new Exception("Invalid site id provided");
 			}
 
 			String userName = myParser.getValueByLabel("userName");
 			String pass = myParser.getValueByLabel("lastName").toLowerCase().replaceAll(" ", "") + year + "!";
 			
 			logger.info("User Password: " + pass);
 			
 			UserAccount user = UserAccount.getUser(userName);
 			if (user == null)
 			{
 				user = UserAccount.createUser(actor, userName, pass);
 				user.expirePassword(actor);
 			}
 			
 			String[] labels = myParser.getLabels();
 			for (String hdr : labels)
 			{
 				String val = myParser.getValueByLabel(hdr);
 				if (val != null)
 				{
 					if ("primaryTelephone".equalsIgnoreCase(hdr))
 					{
 						if (val.length() > 0)
 							user.setPrimaryTelephone(UntzDBObject.getPhoneObject("1", val));
 					}
 					else if ("faxNumber".equalsIgnoreCase(hdr))
 					{
 						if (val.length() > 0)
 							user.setFaxNumber(UntzDBObject.getPhoneObject("1", val));
 					}
 					else if ("resourceRole".equalsIgnoreCase(hdr)) {}
 					else if ("userName".equalsIgnoreCase(hdr)) {}
 					else if ("siteId".equalsIgnoreCase(hdr)) {}
 					else if (hdr.startsWith("resRole"))
 					{
 						String resName = val.substring(0, val.indexOf("|"));
 						String roleName = val.substring(val.indexOf("|") + 1);
 						
 						ResourceDefinition def = defs.get(resName);
 						if (def == null)
 							def = ResourceDefinition.getByName(resName);
 
 						ResourceLink link = new ResourceLink(def, roleName);
 						if (siteId != null && siteId.length() > 0)
 						{
 							link.put("siteId", siteId);
 							link.put("linkText", check.getSiteName());
 						}
 						
 						user.addResourceLink(link);
 					}
 					else if ("resourceName".equalsIgnoreCase(hdr))
 					{
 						ResourceDefinition def = defs.get(val);
 						if (def == null)
 							def = ResourceDefinition.getByName(val);
 
 						ResourceLink link = new ResourceLink(def, myParser.getValueByLabel("resourceRole"));
 						user.addResourceLink(link);
 					}
 					else if ("stateAbbrev".equalsIgnoreCase(hdr))
 					{
 						if (user.get("state") == null)
 						{
 							if ("United States".equalsIgnoreCase(myParser.getValueByLabel("country")))
 							{
 								DBObject state = usa.getStateByAbbrev(val);
 								if (state != null)
 									user.put("state", (String)state.get("state"));
 							}
 						}
 					}
 					else if ("managedBy".equalsIgnoreCase(hdr))
 						user.addManagedBy(val);
 					else
 						user.put(hdr, val);
 				}
 			}
 
 			UDataMgr.calculateLatLong(user);
 			UserAccount.save(user, actor);
 			
 			cnt++;
 		}		
 
 		in.close();
 
 		return cnt;
 	}
 
 	/**
 	 * Get a user account by uid
 	 * 
 	 * @param userName
 	 * @return
 	 */
 	public static UserAccount getUserById(String uid)
 	{
 		if (uid == null)
 			return null;
 		
 		DBObject user = new UserAccount().getCollection().findOne(BasicDBObjectBuilder.start("_id", new ObjectId(uid)).get());
 		
 		if (user == null)
 			return null;
 		
 		return new UserAccount(user);
 	}
 
 	
 }
