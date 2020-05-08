 package server;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 /**
  * Manages logged in users;
  * users have unique names
  * @author Babz
  *
  */
 public class UserManagement {
 
 	private static final Logger LOG = Logger.getLogger(UserManagement.class);
 	private static UserManagement instance;
 
 	private UserManagement() {
 	}
 
 	public static synchronized UserManagement getInstance() {
 		if(instance == null) {
 			instance = new UserManagement();
 		}
 		return instance;
 	}
 
 	private Map<String, User> allUsers = Collections.synchronizedMap(new HashMap<String, User>());
 
 	/**
 	 * logs in the user if not already logged in
 	 * @param userName
 	 * @param userUdpPort
 	 * @return true: successfully logged in
 	 */
 	public boolean login(String userName, String userUdpPort) {
 		User tmpUser = getUserByName(userName);
 		if(tmpUser != null && tmpUser.isLoggedIn()) {
 			return false;
 		} else {
 			if(tmpUser == null) {
				User userNew = new User(userName, userUdpPort);
				allUsers.put(userName, userNew);
 			} else {
 				tmpUser.setUdpPort(userUdpPort);
 			}
 			tmpUser.logIn();
 			return true;
 		}
 	}
 
 	/**
 	 * logs out the user if not already logged out
 	 * @param userName 
 	 * @return true: successfully logged out
 	 */
 	public boolean logout(String userName) {
 		User tmpUser = getUserByName(userName);
 		if(!tmpUser.isLoggedIn()) {
 			return false;
 		} else {
 			tmpUser.logOut();
 			return true;
 		}
 	}
 
 	public User getUserByName(String username) {
 		return allUsers.get(username);
 	}
 
 }
