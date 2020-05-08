 package no.ntnu.idi.simplebank;
 
 import java.util.List;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 
 public class Utilities {
	
 	public static String getCurrentlyLoggedInUser(HttpServletRequest request) {
 		Cookie[] cookies = request.getCookies();
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals("logged_in_user")){
				return cookie.getValue();
 			}
 		}
 		return null;
 	}
 
 	public static List<Account> getAccountsForUser(String currentlyLoggedInUser) {
		
 		Database database = new Database();
 		return database.getAccountsForUsername(currentlyLoggedInUser);
		
 	}
 
 }
