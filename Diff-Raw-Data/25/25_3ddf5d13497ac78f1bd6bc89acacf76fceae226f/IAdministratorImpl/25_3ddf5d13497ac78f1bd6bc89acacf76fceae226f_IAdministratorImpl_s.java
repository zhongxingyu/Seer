 package org.smartsnip.server;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.smartsnip.core.Logging;
 import org.smartsnip.core.User;
 import org.smartsnip.security.PrivilegeController;
 import org.smartsnip.shared.IAdministrator;
 import org.smartsnip.shared.NoAccessException;
 import org.smartsnip.shared.NotFoundException;
 import org.smartsnip.shared.XSession;
 import org.smartsnip.shared.XUser.UserState;
 
 /**
  * Implementation for the administrator user. Some methods have to be
  * overwritten because of the higher access privileges of the administrator
  * 
  * @author Felix Niederwanger
  * 
  */
 public class IAdministratorImpl extends IModeratorImpl implements IAdministrator {
 
 	/** Serialisation ID */
 	private static final long serialVersionUID = 2747963244731693434L;
 
 	@Override
 	public void setUserState(String username, UserState state) throws NotFoundException, NoAccessException {
		if (username == null || state == null)
 			return;
 
 		Session session = getSession();
 		User user = session.getUser();
 		if (user == null || !user.isAdministrator())
 			throw new NoAccessException();
 
 		User targetUser = User.getUser(username);
 		if (targetUser == null)
 			throw new NotFoundException();
 
 		targetUser.setState(state);
 
 		if (!user.equals(targetUser))
 			notifyUser(username, user.getUsername() + " (administrator) set your state to " + state, user.getUsername());
 	}
 
 	@Override
 	public boolean isAdministrator() {
 		Session session = getSession();
 		User user = session.getUser();
 		if (user == null)
 			return false;
 		return user.isAdministrator();
 	}
 
 	@Override
 	public void setPassword(String username, String password) throws NotFoundException, NoAccessException {
 		Session session = getSession();
 		User user = session.getUser();
 		if (user == null || !user.isAdministrator())
 			throw new NoAccessException();
 
 		User targetUser = User.getUser(username);
 		if (targetUser == null)
 			throw new NotFoundException();
 
 		if (password == null || password.isEmpty())
 			throw new IllegalArgumentException("Empty password");
 
 		if (!PrivilegeController.acceptPassword(password))
 			throw new IllegalArgumentException("Password denied");
 
 		targetUser.setPassword(password);
 
 		if (!user.equals(targetUser))
 			notifyUser(username, user.getUsername() + " (administrator) set your password", user.getUsername());
 	}
 
 	@Override
 	public void deleteUser(String username) throws NotFoundException, NoAccessException {
 		Session session = getSession();
 		User user = session.getUser();
 		if (user == null || !user.isAdministrator())
 			throw new NoAccessException();
 
 		User targetUser = User.getUser(username);
 		if (targetUser == null)
 			throw new NotFoundException();
 		targetUser.delete();
 	}
 
 	@Override
 	public List<XSession> getSessions() throws NoAccessException {
 		Session session = getSession();
 		User user = session.getUser();
 		if (user == null || !session.isLoggedIn())
 			throw new NoAccessException();
 		if (!user.isAdministrator())
 			throw new NoAccessException();
 
 		List<Session> sessions = Session.getSessions();
 		List<XSession> result = new ArrayList<XSession>(sessions.size());
 		for (Session s : sessions)
 			result.add(s.toXSession());
 
 		return result;
 	}
 
 	@Override
 	public void closeSession(String key) throws NotFoundException, NoAccessException {
 		if (key == null)
 			return;
 
 		Session session = getSession();
 		User user = session.getUser();
 		if (user == null || !session.isLoggedIn())
 			throw new NoAccessException();
 		if (!user.isAdministrator())
 			throw new NoAccessException();
 
 		Session targetSession = Session.getObfuscatedSession(key);
 		if (targetSession == null)
 			throw new NotFoundException();
 
 		User targetSessionUser = targetSession.getUser();
 		Logging.printInfo("MODERATOR: Terminating session for: "
 				+ (targetSessionUser == null ? "Guest session" : targetSessionUser.getUsername()));
 		targetSession.deleteSession();
 	}
 
 	@Override
 	public void messageToAdmin(String message, String email) throws NoAccessException, IllegalArgumentException {
 		if (message == null | message.isEmpty())
 			return;
 		if (!isValidEmailAddress(email))
 			throw new IllegalArgumentException("Invalid email-address");
 
 		// SPAM filter
 		if (PrivilegeController.checkSpam(message, email))
 			throw new NoAccessException();
 
 		// Seems legit. Send it to all administrator
 		List<User> allUsers = User.getUsers(0, Session.getUserCount());
 		for (User user : allUsers)
 			if (user.isAdministrator())
 				user.createNotification(message, email);
 
 	}
 
 	/**
 	 * Internal call to check if the email address is valid
 	 * 
 	 * @param email
 	 *            to be checked
 	 * @return true if valid otherwise false
 	 */
 	private static boolean isValidEmailAddress(String email) {
 		if (email == null || email.length() < 5)
 			return false;
 		int atSign = email.indexOf('@');
 		if (atSign < 1)
 			return false;
 		if (atSign >= email.length())
 			return false;
 		return true;
 	}
 
 }
