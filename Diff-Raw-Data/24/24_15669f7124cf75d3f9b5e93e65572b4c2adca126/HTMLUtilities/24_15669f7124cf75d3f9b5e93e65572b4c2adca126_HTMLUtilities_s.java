 package kimpel;
 
 /**
  * Servlet Utility class
  * 
  * Updated for HW1 part 2
  * 
  * Joe Kimpel CS667
  * 
  * 6.16.2012
  * 
  */
 
 public class HTMLUtilities{
 	
 	public static final String INCOMPLETE_HEADER = "<!doctype html><html lang='en'>" +
 			"<head><title>Customer Registration</title><meta charset='utf-8'/></head><body>" +
 			"<h3>Received Partial Customer Info:</h3>";
 	
 	public static final String COMPLETE_HEADER = "<!doctype html><html lang='en'>" +
 			"<head><title>Confirmation</title><meta charset='utf-8'/></head><body>" +
 			"<h3>Received Complete Customer Info:</h3>";
 	
 	public static final String FORM_OPENER = "<h4>More Information Needed!</h4>"
 			+ "<form name='customer' action='CustomerInfoServlet' method='post'>";
 	
 	public static final String FORM_CLOSER = "<div><input value='Submit' type='submit'/> " +
 			"<input value='Clear' type='reset'/></div>";
 	
 	public static final String FOOTER = "<div><small>&copy;Joe Kimpel 2012</small></div></body></html>";

	public static final String ID_WARNING = "<h4>The User ID is non-unique. Please try Another</h4>";
 	
 	// This method takes whatever the user put into the form for a particular field, and returns
 	// a string that will:
 	// if the input is non-empty, display it and, if needed, add a hidden input with it's value, or
 	// if the input is empty, prompt the user for a value
 	public static String formEntry(String input, String displayName, String internalName, boolean complete){
 		String response;
 		if (!input.equals("")){
 			response = "<div>" + displayName + ": " + input + "</div>";
 			if (!complete)
 				response += "<input name='" + internalName + "' value='" + input + "' type='hidden'/>";
 		}else{
			response = "<div>" + displayName + ": <input name='" + internalName + "' type='text'/></div>";
 		}
 		return response;	
 	}
 	  
 	//  Taken from Core Servlets and JavaServer Pages 2nd Edition
 	//  from Prentice Hall and Sun Microsystems Press,
 	//  http://www.coreservlets.com/.
 	//  &copy; 2003 Marty Hall; may be freely used or adapted.
 	//
 	// Given a string, this method replaces all occurrences of
 	//  '<' with '&lt;', all occurrences of '>' with
 	//  '&gt;', and (to handle cases that occur inside attribute
 	//  values), all occurrences of double quotes with
 	//  '&quot;' and all occurrences of '&' with '&amp;'.
 	//  Without such filtering, an arbitrary string
 	// could not safely be inserted in a Web page.
 	public static String filter(String input) {
 		StringBuffer filtered = new StringBuffer(input.length());
 		char c;
 		for(int i=0; i<input.length(); i++) {
 			c = input.charAt(i);
 			if (c == '<') {
 				filtered.append("&lt;");
 			} else if (c == '>') {
 				filtered.append("&gt;");
 			} else if (c == '"') {
 				filtered.append("&quot;");
 			} else if (c == '&') {
 				filtered.append("&amp;");
 			} else {
 				filtered.append(c);
 			}
 		}
 		return(filtered.toString());
 	}
 }
