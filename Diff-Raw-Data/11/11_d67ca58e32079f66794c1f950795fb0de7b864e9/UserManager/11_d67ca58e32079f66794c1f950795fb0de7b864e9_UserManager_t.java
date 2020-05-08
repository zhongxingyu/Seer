 package de.fhb.autobday.manager.user;
 
 import de.fhb.autobday.commons.EMailValidator;
 import de.fhb.autobday.commons.HashHelper;
 import de.fhb.autobday.commons.PasswordGenerator;
 import de.fhb.autobday.dao.AbdUserFacade;
 import de.fhb.autobday.data.AbdAccount;
 import de.fhb.autobday.data.AbdUser;
 import de.fhb.autobday.exception.commons.HashFailException;
 import de.fhb.autobday.exception.mail.MailException;
 import de.fhb.autobday.exception.user.*;
 import de.fhb.autobday.manager.LoggerInterceptor;
 import de.fhb.autobday.manager.mail.GoogleMailManagerLocal;
 import java.io.UnsupportedEncodingException;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.ejb.EJB;
 import javax.ejb.Local;
 import javax.ejb.Stateless;
 import javax.interceptor.Interceptors;
 
 /**
  * Implementation of UserManager.
  *
  * @author Andy Klay mail: klay@fh-brandenburg.de
  * @author Michael Koppen mail: koppen@fh-brandenburg.de
  *
  */
 @Stateless
 @Local
 @Interceptors(LoggerInterceptor.class)
 public class UserManager implements UserManagerLocal {
 
 	private final static Logger LOGGER = Logger.getLogger(UserManager.class.getName());
 	@EJB
 	private AbdUserFacade userDAO;
 	@EJB
 	private GoogleMailManagerLocal mailManager;
 
 	public UserManager() {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @see de.fhb.autobday.manager.user.UserManagerLocal#getUser(int)
 	 */
 	@Override
 	public AbdUser getUser(int userid) {
 		return userDAO.find(userid);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @throws HashFailException
 	 * @see
 	 * de.fhb.autobday.manager.user.UserManagerLocal#login(java.lang.String,
 	 * java.lang.String)
 	 */
 	@Override
 	public AbdUser login(String loginName, String password)
 			throws UserException, HashFailException {
 
 		AbdUser user = null;
 		String hash = "";
 
 		if (loginName == null || password == null || loginName.equals("") || password.equals("")) {
 			LOGGER.log(Level.SEVERE, "Invalid input!");
 			throw new IncompleteLoginDataException("Invalid input!");
 		}
 
 		try {
 			user = userDAO.findUserByUsername(loginName);
 		} catch (Exception e) {
 			LOGGER.log(Level.SEVERE, "Invalid loginame!");
 			throw new IncompleteLoginDataException("Invalid loginame!");
 		}
 
 		System.out.println("user: " + user);
 
 		if (user == null) {
 			LOGGER.log(Level.SEVERE, "User not found!");
 			throw new UserNotFoundException("User not found!");
 		}
 
 
 		//check password
 		try {
 
 			hash = HashHelper.calcSHA1(password + user.getSalt());
 
 		} catch (UnsupportedEncodingException e) {
 			LOGGER.log(Level.SEVERE, "UnsupportedEncodingException  {0}", e.getMessage());
 			throw new HashFailException("UnsupportedEncodingException in Hashhelper");
 		} catch (NoSuchAlgorithmException e) {
 			LOGGER.log(Level.SEVERE, "NoSuchAlgorithmException  {0}", e.getMessage());
 			throw new HashFailException("NoSuchAlgorithmException in Hashhelper");
 		}
 
 		if (!user.getPasswort().equals(hash)) {
 			LOGGER.log(Level.SEVERE, "Invalid password!");
 			throw new PasswordInvalidException("Invalid password!");
 		}
 
 		return user;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @see de.fhb.autobday.manager.user.UserManagerLocal#logout()
 	 */
 	@Override
 	public void logout() {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @return abdUser
 	 * @throws HashFailException
 	 * @see
 	 * de.fhb.autobday.manager.user.UserManagerLocal#register(java.lang.String,
 	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
 	 * java.lang.String)
 	 */
 	@Override
 	public AbdUser register(String firstName, String name, String userName, String mail, String password, String passwordRepeat)
 			throws IncompleteUserRegisterException, NoValidUserNameException, HashFailException {
 
 		AbdUser user;
 		AbdUser checkUser;
 		String salt = "";
 		String hash = "";
 
 
 		if (firstName.equals("")) {
 			LOGGER.log(Level.SEVERE, "No firstname given!");
 			throw new IncompleteUserRegisterException("No firstname given!");
 		}
 
 		if (name.equals("")) {
 			LOGGER.log(Level.SEVERE, "No name given!");
 			throw new IncompleteUserRegisterException("No firstname given!");
 		}
 
 		if (!mail.equals("")) {
 			if (!EMailValidator.isEmail(mail)) {
 				LOGGER.log(Level.SEVERE, "Mail is not a valid mail!");
 				throw new IncompleteUserRegisterException("Mail is not a valid mail!");
 			}
 		} else {
 			LOGGER.log(Level.SEVERE, "No mail given!");
 			throw new IncompleteUserRegisterException("No mail given!");
 		}
 		if (userName.equals("")) {
 			LOGGER.log(Level.SEVERE, "No username given!");
 			throw new IncompleteUserRegisterException("No username given");
 		}
 
 		if (userName.length() < 5) {
 			LOGGER.log(Level.SEVERE, "No valid Username!");
 			throw new NoValidUserNameException("No valid Username!");
 		}
 
 		if (password.equals("")) {
 			LOGGER.log(Level.SEVERE, "No password given!");
 			throw new IncompleteUserRegisterException("No password given");
 		}
 
 		if (passwordRepeat.equals("")) {
 			LOGGER.log(Level.SEVERE, "No password repetition given!");
 			throw new IncompleteUserRegisterException("No  password repetition given");
 		}
 
 		if (!password.equals(passwordRepeat)) {
 			LOGGER.log(Level.SEVERE, "Password not similar to the repetition!");
 			throw new IncompleteUserRegisterException("Password not similar to the repetition!");
 		}
 
 		if (password.length() < 5) {
 			LOGGER.log(Level.SEVERE, "Password too short!");
 			throw new IncompleteUserRegisterException("Password too short!");
 		}
 
 
 		//check if userName is unique
 		checkUser = userDAO.findUserByUsername(userName);
 
 		if (checkUser != null) {
 			LOGGER.log(Level.SEVERE, "UserName does already exists!");
 			throw new NoValidUserNameException("UserName does already exists!");
 		}
 
 
 		// generate Salt
 		salt = PasswordGenerator.generateSalt();
 
 		//user init
 		user = new AbdUser();
 		user.setId(Integer.SIZE);
 		user.setFirstname(firstName);
 		user.setName(name);
 		user.setUsername(userName);
 		user.setMail(mail);
 		user.setSalt(salt);
 
 		//hash
 		try {
 			hash = HashHelper.calcSHA1(password + salt);
 		} catch (UnsupportedEncodingException e) {
 			LOGGER.log(Level.SEVERE, "UnsupportedEncodingException  {0}", e.getMessage());
 			throw new HashFailException("UnsupportedEncodingException in Hashhelper");
 		} catch (NoSuchAlgorithmException e) {
 			LOGGER.log(Level.SEVERE, "NoSuchAlgorithmException  {0}", e.getMessage());
 			throw new HashFailException("NoSuchAlgorithmException in Hashhelper");
 		}
 
 		user.setPasswort(hash);
 
 		//save in to db
 		userDAO.create(user);
 
 		return user;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @see
 	 * de.fhb.autobday.manager.user.UserManagerLocal#getAllAccountsFromUser(de.fhb.autobday.data.AbdUser)
 	 */
 	@Override
 	public List<AbdAccount> getAllAccountsFromUser(AbdUser user)
 			throws UserNotFoundException {
 		return getAllAccountsFromUser(user.getId());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @see
 	 * de.fhb.autobday.manager.user.UserManagerLocal#getAllAccountsFromUser(int)
 	 */
 	@Override
 	public List<AbdAccount> getAllAccountsFromUser(int userId)
 			throws UserNotFoundException {
 
 		AbdUser user;
 		List<AbdAccount> outputCollection = new ArrayList<AbdAccount>();
 
 		//lookup for user
 		user = findUser(userId);
 		
 		userDAO.refresh(user);
 		for (AbdAccount actualAccount : user.getAbdAccountCollection()) {
 			outputCollection.add(actualAccount);
 		}
 
 
 		return outputCollection;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @param userName
 	 * @throws HashFailException
 	 * @see de.fhb.autobday.manager.mail.GoogleMailManagerLocal#sendSystemMail(String, String, String)
 	 */
 	@Override
 	public void sendForgotPasswordMail(String userName)
 			throws MailException, UserNotFoundException, HashFailException {
 
 		//getUser
 		AbdUser user;
 		String newPassword;
 		String mailBody;
 		String hash = "";
 		String salt = "";
 
 		user = userDAO.findUserByUsername(userName);
 
 		if (user == null) {
 			LOGGER.log(Level.SEVERE, "User {0} not found!", userName);
 			throw new UserNotFoundException("User " + userName + "not found!");
 		}
 
 		// generate Salt
 		salt = PasswordGenerator.generateSalt();
 
 		//generate new Password
 		newPassword = PasswordGenerator.generatePassword();
 
 
 		//hash
 		try {
 			hash = HashHelper.calcSHA1(newPassword + salt);
 		} catch (UnsupportedEncodingException e) {
 			LOGGER.log(Level.SEVERE, "UnsupportedEncodingException  {0}", e.getMessage());
 			throw new HashFailException("UnsupportedEncodingException in Hashhelper");
 		} catch (NoSuchAlgorithmException e) {
 			LOGGER.log(Level.SEVERE, "NoSuchAlgorithmException  {0}", e.getMessage());
 			throw new HashFailException("NoSuchAlgorithmException in Hashhelper");
 		}
 
 		// save new password into database
 		user.setSalt(salt);
 		user.setPasswort(hash);
 		userDAO.edit(user);
 
 		mailBody = "You recieved a new password for your autobdayaccount: " + newPassword + "\n\n" + "greetz your Autobdayteam";
 
 		// Send mail with new Password
 		try {
 			mailManager.sendSystemMail("Autobday Notification", mailBody, user.getMail());
 		} catch (Exception e) {
 			LOGGER.log(Level.SEVERE, e.getMessage());
 			throw new MailException(e.getMessage());
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @param user
 	 * @param password
 	 * @param passwordRepeat
 	 * @throws PasswordInvalidException
 	 * @throws HashFailException
 	 * @see de.fhb.autobday.manager.mail.GoogleMailManagerLocal#sendSystemMail(String, String, String)
 	 */
 	@Override
 	public void changePassword(AbdUser user, String oldPassword, String password, String passwordRepeat)
 			throws UserNotFoundException, PasswordInvalidException, HashFailException {
 		changePassword(user.getId(), oldPassword, password, passwordRepeat);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @param passwordRepeat
 	 * @param password
 	 * @throws HashFailException
 	 * @throws PasswordInvalidException
 	 * @see de.fhb.autobday.manager.mail.GoogleMailManagerLocal#sendSystemMail(String, String, String)
 	 */
 	@Override
 	public void changePassword(int userId, String oldPassword, String password, String passwordRepeat)
 			throws UserNotFoundException, PasswordInvalidException, HashFailException {
 
 		AbdUser user;
 		String salt = "";
 		String hash = "";
 
 		if (oldPassword.equals("") || password.equals("") || passwordRepeat.equals("")) {
 			LOGGER.log(Level.SEVERE, "Incomplete passwordsfields!");
 			throw new PasswordInvalidException("Incomplete passwordsfields!");
 		}
		//TODO Ueberpruefen ob oldpassword auch das oldPassword is....
 		if (!password.equals(passwordRepeat)) {
 			LOGGER.log(Level.SEVERE, "Password not similar to the repetition!");
 			throw new PasswordInvalidException("Password not similar to the repetition!");
 		}
 
 		if (password.length() < 5) {
 			LOGGER.log(Level.SEVERE, "Password too short!");
 			throw new PasswordInvalidException("Password too short!");
 		}
 
 		//lookup for user
 		user = findUser(userId);
 
 		// generate Salt
 		salt = PasswordGenerator.generateSalt();
 
 
 		//hash
 		try {
 			hash = HashHelper.calcSHA1(password + salt);
 		} catch (UnsupportedEncodingException e) {
 			LOGGER.log(Level.SEVERE, "UnsupportedEncodingException  {0}", e.getMessage());
 			throw new HashFailException("UnsupportedEncodingException in Hashhelper");
 		} catch (NoSuchAlgorithmException e) {
 			LOGGER.log(Level.SEVERE, "NoSuchAlgorithmException  {0}", e.getMessage());
 			throw new HashFailException("NoSuchAlgorithmException in Hashhelper");
 		}
 
 		// save new password into database
 		user.setSalt(salt);
 		user.setPasswort(hash);
 		userDAO.edit(user);
 	}
 	/**
 	 * Method to lookup for a user.
 	 * if no user exists exception is thrown.
 	 * 
 	 * @param userId user to find
 	 * @return found user
 	 * @throws UserNotFoundException 
 	 */
 	protected AbdUser findUser(int userId) throws UserNotFoundException{
 		AbdUser user;
 		//search User
 		user = userDAO.find(userId);
 
 		//if account not found
 		if (user == null) {
 			LOGGER.log(Level.SEVERE, "User {0} not found!", userId);
 			throw new UserNotFoundException("User " + userId + " not found!");
 		}
 		return user;
 	}
 }
