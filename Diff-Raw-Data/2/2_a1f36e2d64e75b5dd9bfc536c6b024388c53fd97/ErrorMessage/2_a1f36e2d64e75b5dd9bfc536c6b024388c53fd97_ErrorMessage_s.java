 package net.jtmcgee.project.happybirthday.utils;
 
 public class ErrorMessage {
 	public static String getErrorHTML(String errorMsg) {
 		return "<div class=\"error\">" +
 			errorMsg + "</div>";
 	}
 	public static String OAuthError() {
		return getErrorHTML("Your OAuth Token is expired. Get a <a href='#'>new one?</a>" +
 	  				"<br /> Or perhaps you want to report a <a href='bugs.jsp'>bug?</a>");
 	}
 	public static String GeneralError() {
 		return getErrorHTML("A random problem happened. <a href='bugs.jsp'>Report it?</a>");
 	}
 }
