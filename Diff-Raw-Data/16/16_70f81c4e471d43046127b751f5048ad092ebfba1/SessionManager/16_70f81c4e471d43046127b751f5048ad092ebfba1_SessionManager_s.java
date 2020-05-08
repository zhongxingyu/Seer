 package nl.sense_os.commonsense.common.client.util;
 
 import java.util.Date;
 

 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Cookies;
 
 public class SessionManager {
 
 	private static final String KEY = "session_id";
 
 	/**
 	 * @return The session ID from the sense-os.nl cookie
 	 */
 	public static String getSessionId() {
 		String sessionId = Cookies.getCookie(KEY);
 		if ("".equals(sessionId)) {
 			sessionId = null;
 		}
 		return sessionId;
 	}
 
 	/**
 	 * Removes the session ID from the sense-os.nl cookie
 	 */
 	public static void removeSessionId() {
		if (GWT.isProdMode()) {
 			String domain = Constants.DEV_MODE ? "dev.sense-os.nl" : ".sense-os.nl";
 			Cookies.setCookie(KEY, "", new Date(), domain, null, false);
		} else {
			Cookies.removeCookie(KEY);
 		}
 	}
 
 	/**
 	 * Stores the session ID in the sense-os.nl cookie
 	 * 
 	 * @param sessionId
 	 */
 	public static void setSessionId(String sessionId) {
		if (GWT.isProdMode()) {
 			String domain = Constants.DEV_MODE ? "dev.sense-os.nl" : ".sense-os.nl";
 			Cookies.setCookie(KEY, sessionId, null, domain, "", false);
			Cookies.setCookie(KEY, sessionId);
 		} else {
 			Cookies.setCookie(KEY, sessionId);
 		}
 	}
 
 	private SessionManager() {
 		// private constructor to prevent instantiation
 	}
 }
