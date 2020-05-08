 package hammer;
 
 /**
  * A logging class for HAMMER.
  * [TODO] Save logs to file.
  * @author rynor_000
  *
  */
 public class HammerLog {
 	public static enum LogLevel implements Comparable<LogLevel> {
 		DEBUG,
 		INFO,
 		ERROR,
 		NONE;
 	}
 	
 	private static LogLevel LEVEL = LogLevel.INFO;
 	
 	public static void setLogLevel(LogLevel level) {
 		LEVEL = level;
 	}
 	
 	public static void log(String text, LogLevel level, boolean newLine) {
 		if (level.compareTo(LEVEL) >= 0) {
 			if (newLine) {
 				System.out.println(text);
 			}
 			else {
 				System.out.print(text);
 			}
 		}
 	}
 	
 	public static void log(String text, LogLevel level) {
 		log(text, level, true);
 	}
 	
 	public static void info(String text) {
 		log(text, LogLevel.INFO);
 	}
 	
 	public static void debug(String text) {
 		log(text, LogLevel.DEBUG);
 	}
 	
 	public static void error(String text) {
 		log(text, LogLevel.ERROR);
 	}
 }
