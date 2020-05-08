 package server.operations;
 
 import java.util.Date;
 
 import javax.ws.rs.core.NewCookie;
 
 import server.entities.Cookie;
 import server.exceptions.CookieInvalidException;
 import server.queries.CookieQuery;
 
 /**
  * Used to creates and validated session cookies for the client.
  * 
  * @author dennis.markmann
  * @since JDK.1.7.0_25
  * @version 1.0
  */
 
 public class CookieHandler {
 
 	public final NewCookie createCookie() {
 
 		final DateHelper dateHelper = new DateHelper();
 		dateHelper.addTime(0, 1, 0, 0, 0, 0);
 
 		final String cookieID = new PasswordEncryptor().generateEncryptedPassword();
		final NewCookie cookie = new NewCookie("NSA-Cookie", cookieID, "/", null, 1, null, (int)30 * 24 * 60 * 60, false);
 
 		new CookieQuery().createCookie(cookieID, dateHelper.parseStringToDate(dateHelper.getFullDate()));
 
 		return cookie;
 	}
 
 	public final boolean validateCookie(final String cookieValue) throws CookieInvalidException {
 		final Cookie cookie = new CookieQuery().getCookie(cookieValue);
 		if (cookie == null) {
 			throw new CookieInvalidException();
 		}
 		return true;
 	}
 
 	public final void deleteInvalidCookies() {
 		new CookieQuery().removeInvalidCookies(new Date());
 	}
 
 	public final boolean deleteCookie(final String cookie) {
 		return new CookieQuery().removeCookie(cookie);
 	}
 }
