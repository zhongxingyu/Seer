 package de.quiz.UserManager;
 
 import java.util.ArrayList;
 
 import javax.servlet.http.HttpSession;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import de.fhwgt.quiz.application.Player;
 import de.fhwgt.quiz.application.Quiz;
 import de.fhwgt.quiz.error.QuizError;
 import de.quiz.LoggingManager.ILoggingManager;
 import de.quiz.ServiceManager.ServiceManager;
 import de.quiz.User.IUser;
 import de.quiz.User.User;
 
 /**
  * this service handles the users. It should get registered with the
  * ServiceManager by default!
  * 
  * @author Patrick Na
  */
 public class UserManager implements IUserManager {
 
 	private ArrayList<IUser> activeUser = new ArrayList<IUser>();
 
 	public UserManager() {
 	}
 
 	/**
 	 * delete single user from activeUser list and invalidates its session
 	 * 
 	 * @param user
 	 */
 	public void removeActiveUser(IUser user) {
 
 		for (int i = 0; i < activeUser.size(); i++) {
 
 			if (activeUser.get(i).getUserID().equals(user.getUserID())) {
 				QuizError error = new QuizError();
 				Quiz.getInstance().removePlayer(user.getPlayerObject(), error);
				if (error.isSet()) {
 					activeUser.remove(user);
 				} else {
 					ServiceManager.getInstance()
 							.getService(ILoggingManager.class).log(this, error);
 				}
 			}
 		}
 	}
 
 	/**
 	 * check user data and set session ID
 	 * 
 	 * @param id
 	 *            the user's ID
 	 * @param session
 	 *            the user's session
 	 * @throws Exception
 	 */
 	public IUser loginUser(String name, HttpSession session) throws Exception {
 
 		// check if session in use
 		if (getUserBySession(session) != null) {
 			ServiceManager
 					.getInstance()
 					.getService(ILoggingManager.class)
 					.log("User has had an existing session");
 			return getUserBySession(session);
 
 		} else {
 			// TODO: add websocketid
 			QuizError error = new QuizError();
 			Player newPlayer = Quiz.getInstance().createPlayer(name, error);
 
 			if (error.isSet()) {
 				ServiceManager.getInstance().getService(ILoggingManager.class)
 						.log(this, error);
 				return null;
 
 			} else {
 
 				IUser tmpUser = new User(newPlayer.getId().toString(),
 						newPlayer.getName(), session, newPlayer);
 				// add to list
 				activeUser.add(tmpUser);
 				// ServiceManager.getInstance().getService(ILoggingManager.class)
 				// .log(this, "Successfully logged in user" + name);
 				return tmpUser;
 			}
 		}
 
 	}
 
 	/**
 	 * returns an active user by it's ID
 	 * 
 	 * @param id
 	 *            the requested user's ID return the user object or null
 	 * @throws Exception
 	 */
 	public IUser getUserById(String id) throws Exception {
 
 		// find user
 		for (int i = 0; i < activeUser.size(); i++) {
 
 			if (activeUser.get(i).getUserID().equals(id)) {
 
 				return activeUser.get(i);
 			}
 		}
 		throw new Exception("UserByID not found...");
 	}
 
 	/**
 	 * check if the users session timed out!
 	 * 
 	 * @param session
 	 *            the requested user's session return the user object or null
 	 */
 	public IUser getUserBySession(HttpSession session) {
 
 		IUser tmpUser = null;
 		for (IUser item : activeUser) {
 
 			if (item.getSession().equals(session)) {
 				tmpUser = item;
 
 				if (checkUserForValidSession(item)) {
 					return tmpUser;
 				} else {
 					return null;
 				}
 			}
 		}
 		// ServiceManager.getInstance().getService(ILoggingManager.class).log("No session found by getUserBySession Method");
 		return null;
 	}
 
 	/**
 	 * set's the session for a user
 	 * 
 	 * @param user
 	 *            the user we want to add the session
 	 * @param session
 	 *            the session we want to add
 	 * @throws Exception
 	 */
 	@Override
 	public void setSessionForUser(IUser user, HttpSession session)
 			throws Exception {
 
 		// find user and set session
 		for (int i = 0; i < activeUser.size(); i++) {
 
 			if (activeUser.get(i).hasSameID(user)) {
 
 				activeUser.get(i).setSession(session);
 				return;
 			}
 		}
 		throw new Exception("can't set session! User not found...");
 	}
 
 	/**
 	 * logout user
 	 * 
 	 * @param session
 	 *            the user's session
 	 */
 	@Override
 	public void logoutUser(HttpSession session) throws Exception {
 
 		for (IUser user : activeUser) {
 			if (user.getSession().equals(session)) {
 
 				user.getSession().invalidate();
 				ServiceManager.getInstance().getService(ILoggingManager.class)
 						.log(this, "successfully logged out user");
 				return;
 			}
 		}
 		throw new Exception("unable to logout user");
 
 	}
 
 	/**
 	 * Returns a JSON with the playerlist. THIS METHOD HAS TO BE CALLED FROM THE
 	 * SERVER SEND EVENTS!!!
 	 * 
 	 * @return JSONObject Playerlist or null at failure.
 	 */
 	public JSONObject getPlayerList() {
 		JSONObject tmpJSON = new JSONObject();
 		int i = 0;
 		for (IUser user : this.activeUser) {
 			try {
 				tmpJSON.put("name" + i, user.getName());
 			} catch (JSONException e) {
 				ServiceManager.getInstance().getService(ILoggingManager.class)
 						.log("Failed to send Playerlist");
 			}
 			i++;
 		}
 		return tmpJSON;
 	}
 
 	/**
 	 * checks if the given user has a valid session if not valid the user will
 	 * be removed
 	 * 
 	 * @param user
 	 * @return true if valid, false if not valid
 	 */
 	private boolean checkUserForValidSession(IUser user) {
 		try {
 			user.getSession().getLastAccessedTime();
 			return true;
 		} catch (IllegalStateException e) {
 			ServiceManager.getInstance().getService(ILoggingManager.class)
 					.log(this, e);
 			removeActiveUser(user);
 			return false;
 		}
 	}
 
 }
