 package services;
 
 import models.User;
 import services.exception.AlreadyRegisteredUser;
 import services.exception.NoAuthenfiedUserInSessionException;
 import ui.tags.Messages;
 import util.security.PasswordUtil;
 import util.security.SessionUtil;
 
 import com.avaje.ebean.Ebean;
 import com.avaje.ebean.ExpressionList;
 
 /**
  * User management service.
  * 
  * @author adericbourg
  * 
  */
 public final class UserService {
 
 	private UserService() {
 		// No instance.
 		throw new AssertionError();
 	}
 
 	public static void registerUser(User user) {
 		if (user.id != null) {
 			throw new IllegalArgumentException("user already has an id");
 		}
 		if (findByUsername(user.username) != null) {
 			throw new AlreadyRegisteredUser(user.username);
 		}
 		user.registered = true;
 		user.save();
 	}
 
 	public static void updateUserProfile(User user) {
 		User currentUser = SessionUtil.currentUser();
 		if (currentUser == null) {
 			throw new NoAuthenfiedUserInSessionException();
 		}
 		currentUser.email = user.email;
 		currentUser.displayName = user.displayName;
 		currentUser.save();
 	}
 
 	public static boolean updateUserPassword(String oldPassword,
 			String newPassword) {
 		User currentUser = SessionUtil.currentUser();
 
 		if (!currentUser.passwordHash.equals(PasswordUtil.hashPassword(
 				currentUser.username, oldPassword))) {
 			Messages.error("Wrong old password");
 			return false;
 		}
 
 		currentUser.passwordHash = PasswordUtil.hashPassword(
 				currentUser.username, newPassword);
 		currentUser.save();
 		return true;
 	}
 
 	public static User authenticate(String login, String password) {
 		String trimmedLogin = login.trim();
 		ExpressionList<User> el = Ebean
 				.find(User.class)
 				.where()
 				.ieq("username", trimmedLogin)
 				.ieq("password_hash",
 						PasswordUtil.hashPassword(login, password));
 		if (el.findRowCount() == 0) {
 			return null;
 		}
 		return el.findUnique();
 	}
 
 	public static User findByUsername(String name) {
 		String trimmedUsername = name.trim();
 		ExpressionList<User> el = Ebean.find(User.class).where()
 				.ieq("username", trimmedUsername).ieq("registered", "true");
 		if (el.findRowCount() == 0) {
 			return null;
 		}
 		return el.findUnique();
 	}
 
 	public static User registerAnonymousUser(String name) {
 		User user = new User();
 		user.username = name.trim();
 		user.registered = false;
 		user.save();
 		return user;
 	}
 }
