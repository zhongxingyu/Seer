 package org.kilabit;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.Cookie;
 
 public class ServletUtilities {
 	public static int getIntParameter (HttpServletRequest request
 					, String paramName
 					, int defaultValue) {
 		String paramString = request.getParameter (paramName);
		if (null == paramString) {
			return defaultValue;
		}
 		int paramValue;
 		try {
 			paramValue = Integer.parseInt (paramString);
 		} catch(NumberFormatException nfe) { // Handles null and bad format
 			paramValue = defaultValue;
 		}
 		return (paramValue);
 	}
 
 	public static String getCookieValue (Cookie[] cookies
 						, String cookieName
 						, String defaultValue) {
 		if (null == cookies) {
 			return defaultValue;
 		}
 		for (int i=0; i<cookies.length; i++) {
 			Cookie cookie = cookies[i];
 			if (cookieName.equals(cookie.getName())) {
 				return(cookie.getValue());
 			}
 		}
 		return(defaultValue);
 	}
 
 	public static final int SECONDS_PER_MONTH = 60*60*24*30;
 }
