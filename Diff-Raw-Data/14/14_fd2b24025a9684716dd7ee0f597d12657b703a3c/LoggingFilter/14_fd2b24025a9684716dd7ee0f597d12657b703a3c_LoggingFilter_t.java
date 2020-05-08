 package controllers;
 
 import play.Logger;
 import play.Play;
 import play.mvc.Before;
 import play.mvc.Controller;
 
 /**
  * 
  * @author Alex Jarvis axj7@aber.ac.uk
  */
 public class LoggingFilter extends Controller {
 	
 	@Before
 	public static void logBefore(String body) {
 		if (Play.mode == Play.Mode.DEV) {
 			Logger.debug("Request: " + request.toString());
 			
			if (body != null && body.length() > 0) {
 				Logger.debug("Request body: \n" + body);
 			}
 		}
 	}
 
 }
