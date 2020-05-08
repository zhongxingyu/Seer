 package util.security;
 
 import models.User;
import play.cache.Cache;
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
		Cache.set(USERNAME_KEY, user);
 	}
 
 	public static User currentUser() {
 		if (!isAuthenticated()) {
 			return null;
 		}
 
		User user = (User) Cache.get(USERNAME_KEY);
		if (user == null) {
			user = UserService.findByUsername(getCurrentSession().get(
					USERNAME_KEY));
			Cache.set(USERNAME_KEY, user);
		}
		return user;
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
