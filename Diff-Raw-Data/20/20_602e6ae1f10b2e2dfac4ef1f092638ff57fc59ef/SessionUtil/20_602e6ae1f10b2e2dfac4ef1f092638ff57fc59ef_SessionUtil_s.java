 package util.security;
 
 import models.User;
 import play.mvc.Http.Context;
 import play.mvc.Http.Session;
 import services.UserService;
 
 public final class SessionUtil {
 
 	static final String USERNAME_KEY = "k_username";
 
 	private SessionUtil() {
 		throw new AssertionError();
 	}
 
 	public static boolean isAuthenticated() {
 		return getCurrentSession().containsKey(USERNAME_KEY);
 	}
 
 	public static void setUser(User user) {
 		getCurrentSession().put(USERNAME_KEY, user.username);
 	}
 
 	public static User currentUser() {
 		if (!isAuthenticated()) {
 			return null;
 		}
		return UserService
				.findByUsername(getCurrentSession().get(USERNAME_KEY));
 
 	}
 
 	public static void clear() {
 		if (isAuthenticated()) {
 			getCurrentSession().remove(USERNAME_KEY);
 		}
 		getCurrentSession().clear();
 	}
 
 	private static Session getCurrentSession() {
 		return Context.current().session();
 	}
 }
