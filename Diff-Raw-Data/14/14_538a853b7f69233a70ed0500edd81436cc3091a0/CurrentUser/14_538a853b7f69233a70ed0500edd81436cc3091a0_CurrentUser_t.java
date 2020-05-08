 package util.security;
 
 import static util.security.SessionParameters.LOCALE;
 import static util.security.SessionParameters.USERNAME;
 
 import java.util.Locale;
 import java.util.UUID;
 
 import models.User;
 
 import org.apache.commons.lang3.LocaleUtils;
 
 import play.cache.Cache;
 import play.i18n.Lang;
 import play.mvc.Http.Context;
 import play.mvc.Http.Session;
 import scala.Option;
 import services.UserService;
 
 public final class CurrentUser {
 
 	private static final String SESSION_KEY = "SESSION_ID";
 
 	private CurrentUser() {
 		throw new AssertionError();
 	}
 
 	// --- USER ---
 
 	public static boolean isAuthenticated() {
 		return getCurrentSession().isDefined()
 				&& (getSessionParameter(USERNAME) != null);
 	}
 
 	public static void setUser(User user) {
 		setSessionParameter(USERNAME, user.username);
 		Cache.set(USERNAME.getKey(), user);
 		if (user.preferredLocale != null) {
 			setPreferredLocale(user.preferredLocale);
 		}
 	}
 
 	public static Option<User> currentUser() {
 		if (!isAuthenticated()) {
 			return Option.empty();
 		}
 
 		// Fetch current user.
 		Option<User> optUser;
 		User user = (User) Cache.get(USERNAME.getKey());
 		if (user == null) {
 			optUser = UserService.findByUsername(getSessionParameter(USERNAME));
 			if (optUser.isDefined()) {
 				Cache.set(USERNAME.getKey(), optUser.get());
 			}
 		} else {
 			optUser = Option.apply(user);
 		}
 
 		// If current user was not found, session cookie might be outdated.
 		// Clear session.
 		if (optUser.isEmpty()) {
			logout();
 		}
 
 		return optUser;
 	}
 
 	// --- LOCALE ---
 
 	public static void setPreferredLocale(Locale locale) {
 		if (locale == null) {
 			removeSessionParameter(LOCALE);
 		} else {
 			setSessionParameter(LOCALE, locale.toString());
 			Context.current().changeLang(locale.getLanguage());
 		}
 	}
 
 	public static Option<Locale> preferredLocale() {
 		Locale locale = null;
 		if (isAuthenticated() && currentUser().isDefined()) {
 			User user = currentUser().get();
 			locale = user.preferredLocale;
 		}
 		if ((locale == null) && sessionContainsParameter(LOCALE)) {
 			locale = LocaleUtils.toLocale(getSessionParameter(LOCALE));
 		}
 		if ((locale == null) && (Context.current != null)) {
 			locale = Context.current().lang().toLocale();
 		}
 		return Option.apply(locale);
 	}
 
 	public static Option<Lang> preferredLang() {
 		if (preferredLocale().isDefined()) {
 			return Option.apply(new Lang(new play.api.i18n.Lang(
 					preferredLocale().get().getLanguage(), preferredLocale()
 							.get().getCountry())));
 		}
 		return Option.empty();
 	}
 
 	// --- UTIL ---
 
	public static void logout() {
 		if (getCurrentSession().isDefined()) {
 			Session session = getCurrentSession().get();
 			if (isAuthenticated()) {
 				removeSessionParameter(USERNAME);
 			}
 			session.clear();
 		}
		Cache.remove(USERNAME.getKey());
 	}
 
 	static String sessionId() {
 		if (getCurrentSession().isEmpty()) {
 			return null;
 		}
 		if (getCurrentSession().get().containsKey(SESSION_KEY)) {
 			return getCurrentSession().get().get(SESSION_KEY);
 		}
 
 		// Create new session.
 		getCurrentSession().get().clear();
 		String sessionId = UUID.randomUUID().toString();
 		getCurrentSession().get().put(SESSION_KEY, sessionId);
 		return sessionId();
 	}
 
 	private static String getSessionParameter(SessionParameters parameter) {
 		return getCurrentSession().isDefined() ? getCurrentSession().get().get(
 				parameter.getKey()) : null;
 	}
 
 	private static void setSessionParameter(SessionParameters parameter,
 			String value) {
 		if (getCurrentSession().isDefined()) {
 			getCurrentSession().get().put(parameter.getKey(), value);
 		}
 	}
 
 	private static void removeSessionParameter(SessionParameters parameter) {
 		if (getCurrentSession().isDefined()) {
 			getCurrentSession().get().remove(parameter.getKey());
 		}
 	}
 
 	private static boolean sessionContainsParameter(SessionParameters parameter) {
 		return getCurrentSession().isDefined() ? getCurrentSession().get()
 				.containsKey(parameter.getKey()) : false;
 	}
 
 	private static Option<Session> getCurrentSession() {
 		return Context.current.get() == null ? Option.<Session> empty()
 				: Option.apply(Context.current().session());
 	}
 
 }
