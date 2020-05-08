 package ucmsite.util;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 // This class is incomplete
 public class AppEngineLogger {
 
	private static Logger gaeLog = Logger.getAnonymousLogger();
 	
 	public static void log(String message) {
 		gaeLog.setLevel(Level.ALL);
 		gaeLog.log(Level.SEVERE, message);
 	}
 	
 }
