 package net.retakethe.policyauction.business.impl;
 
 import java.util.Date;
 
 import net.retakethe.policyauction.business.api.UserManager;
 import net.retakethe.policyauction.data.api.DAOManager;
 import net.retakethe.policyauction.data.api.UserDAOManager;
 import net.retakethe.policyauction.data.api.UserDAOManager.NoSuchUserException;
 import net.retakethe.policyauction.data.api.dao.UserDAO;
 import net.retakethe.policyauction.data.api.dao.UserPendingDAO;
 import net.retakethe.policyauction.data.api.dao.UsernameDAO;
 import net.retakethe.policyauction.entities.EntityFactory;
 import net.retakethe.policyauction.entities.User;
 import net.retakethe.policyauction.services.EmailSender;
 import net.retakethe.policyauction.services.EmailSender.EmailNotSentException;
 import net.retakethe.policyauction.util.AssertArgument;
 import net.retakethe.policyauction.util.CollectionUtils;
 
 import org.apache.commons.lang.RandomStringUtils;
 import org.mindrot.jbcrypt.BCrypt;
 
 public class UserManagerImpl implements UserManager {
 	
 	private DAOManager daoManager;

 	private EmailSender emailSender;

	public UserManagerImpl(final DAOManager daoManager, final EmailSender emailSender) {
 		this.daoManager = daoManager;
		this.emailSender = emailSender;
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public User createUser() {
 		return EntityFactory.makeUser(daoManager.getUserDAOManager().createUser());
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public User provisionUser(final User user, final String url) {
 		AssertArgument.notNull(user.getEmail(), "user.getEmail()");
 		
 		UserDAO userDAO = EntityFactory.getUserDAO(user);
 		userDAO.setActivationCode(RandomStringUtils.randomAlphanumeric(32));
 		userDAO.setCreatedTimestamp(new Date());
 		daoManager.getUserDAOManager().persist(userDAO);
 		String body = "Hi, you're almost there, just click through on the following link and you're in - " + url + "?email=" + userDAO.getEmail() + "&code=" + userDAO.getActivationCode() + " \r\n";
 		try {
 			emailSender.sendMail("newuser@idvoteforthis.co.nz", CollectionUtils.list(userDAO.getEmail()), "New user activation", body);
 		} catch (EmailNotSentException e) {
 			throw new RuntimeException("Email unable to be sent", e);
 		}
 		return user;
 	}
 
 	@Override
 	public User loadNewUser(String email, String activationCode) {
 		AssertArgument.notNull(email, "email");
 		AssertArgument.notNull(activationCode, "activationCode");
 		
 		UserDAOManager userManager = daoManager.getUserDAOManager();
 		UserPendingDAO userPendingDAO;
 		try {
 			userPendingDAO = userManager.getUserPending(email);
 			if (userPendingDAO.getActivationCode().equalsIgnoreCase(activationCode)) {
 				return EntityFactory.makeUser(userManager.getUser(userPendingDAO.getUserID()));
 			}
 		} catch (NoSuchUserException e) {
 			return null;
 		}
 		return null;
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public User activateUser(final User user) {
 		user.setPasswordExpiryTimestamp(new Date());
 		user.setPasswordHash(BCrypt.hashpw(user.getPasswordHash(), BCrypt.gensalt()));
 		user.setVoteSalaryDate(new Date());
 		user.setVoteSalaryLastPaidTimestamp(new Date());
 
 		daoManager.getUserDAOManager().update(EntityFactory.getUserDAO(user));
 		
 		return user;
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public User authenticateUser(final String username, final String password) {
 		try {
 			UsernameDAO usernameDAO = daoManager.getUserDAOManager().getUsername(username);
 			if (usernameDAO != null) {
 				User unAuthenticatedUser = EntityFactory.makeUser(daoManager.getUserDAOManager().getUser(usernameDAO.getUserID()));
 				if (BCrypt.checkpw(password, unAuthenticatedUser.getPasswordHash())) {
 					return unAuthenticatedUser;
 				}
 			}
 		} catch (NoSuchUserException e) {
 			// TODO this should be logging.
 		}
 		return null;
 	}
 }
