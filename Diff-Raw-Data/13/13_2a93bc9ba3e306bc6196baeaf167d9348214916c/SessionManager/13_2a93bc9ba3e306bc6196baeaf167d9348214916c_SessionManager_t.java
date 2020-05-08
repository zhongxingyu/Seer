 package nl.sense_os.commonsense.common.client.communication;
 
 import java.util.Date;
 
 import nl.sense_os.commonsense.common.client.util.Constants;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.Window.Location;
 
 public class SessionManager {
 
 	private static final String KEY = "session_id";
 
 	/**
 	 * Tries to get a session ID from either the URL parameters or from the domain cookies.
 	 * 
 	 * @return The session ID or null
 	 */
 	public static String getSessionId() {
 
 		// check session ID cookie
 		String sessionId = Cookies.getCookie(KEY);
 
        if (null == sessionId || "".equals(sessionId)) {
 
            // sometimes the session ID parameter can be found in the URL
 			sessionId = Location.getParameter("session_id");
 
            // store the session ID in the cookie if we found it in the URL
            if (null != sessionId && !"".equals(sessionId)) {
                setSessionId(sessionId);
            } else {
                sessionId = null;
 			}
 		}
 
 		return sessionId;
 	}
 
 	/**
 	 * Removes the session ID from the sense-os.nl cookie
 	 */
 	public static void removeSessionId() {
 		if (GWT.isProdMode() && Location.getHostName().contains("sense-os.nl")) {
 			String domain = Constants.DEV_MODE ? "dev.sense-os.nl" : ".sense-os.nl";
 			Cookies.setCookie(KEY, "", new Date(), domain, null, false);
 		}
 		Cookies.removeCookie(KEY);
 	}
 
 	/**
 	 * Stores the session ID in the sense-os.nl cookie
 	 * 
 	 * @param sessionId
 	 */
 	public static void setSessionId(String sessionId) {
 		if (GWT.isProdMode() && Location.getHostName().contains("sense-os.nl")) {
 			String domain = Constants.DEV_MODE ? "dev.sense-os.nl" : ".sense-os.nl";
 			Cookies.setCookie(KEY, sessionId, null, domain, "", false);
 		} else {
 			Cookies.setCookie(KEY, sessionId);
 		}
 	}
 
 	private SessionManager() {
 		// private constructor to prevent instantiation
 	}
 }
