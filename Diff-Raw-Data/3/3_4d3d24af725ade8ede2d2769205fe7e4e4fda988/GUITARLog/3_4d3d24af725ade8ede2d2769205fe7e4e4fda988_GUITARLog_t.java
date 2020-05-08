 /**
  * 
  */
 package edu.umd.cs.guitar.util;
 
 import java.io.IOException;
 
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 
 /**
  * Global runtime log for GUITAR
  * 
  * <p>
  * 
  * @author Bao Nguyen
  * 
  */
 public class GUITARLog {
 	
 	/**
 	 * 
 	 */
 	public static final String LOGFILE_NAME_SYSTEM_PROPERTY = "logfile.name";
 	
 	public static Logger log ;//= Logger.getLogger("GUITAR");
 	
 	
 	/**
 	 * 
 	 */
 	private static final String LOG_LAYOUT_PATTER = "%-6r [%t] %-5p - %m%n";
 	/**
 	 * 
 	 */
 	private static final String GUITAR_DEFAULT_LOG = "GUITAR-Default.log";
 
 	/**
 	 * Logging level
 	 */
//	private static Level level = Level.DEBUG;
	private static Level level = Level.INFO;
 
 	static {
 		
 		log = Logger.getLogger(GUITARLog.class);
 
 		PatternLayout layout = new org.apache.log4j.PatternLayout();
 		layout.setConversionPattern(LOG_LAYOUT_PATTER);
 
 		ConsoleAppender stdout = new ConsoleAppender(layout);
 		log.addAppender(stdout);
 
 		FileAppender file = null;
 
 		String logFileName = System.getProperty(LOGFILE_NAME_SYSTEM_PROPERTY);
 
 		if (logFileName == null) {
 			logFileName = GUITAR_DEFAULT_LOG;
 		}
 
 		try {
 			file = new FileAppender(layout, logFileName, false);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		log.addAppender(file);
 
 		log.setLevel(level);
 	}
 
 }
