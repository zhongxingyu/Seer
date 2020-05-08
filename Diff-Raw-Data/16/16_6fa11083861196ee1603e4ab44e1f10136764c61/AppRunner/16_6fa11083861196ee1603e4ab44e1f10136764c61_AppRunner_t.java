 package com.github.mnicky.bible4j;
 
 import java.util.logging.ConsoleHandler;
 import java.util.logging.FileHandler;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 import org.slf4j.LoggerFactory;
 
 import com.github.mnicky.bible4j.cli.CommandParser;
 import com.github.mnicky.bible4j.storage.H2DbBibleStorageFactory;
 
 public final class AppRunner {
     
     private final static org.slf4j.Logger logger = LoggerFactory.getLogger(AppRunner.AppLogger.class);
     
     /**
      * This class has disabled instantiating
      */
     private AppRunner() {}
     
     public static void main(String[] args) {
 
 	try {
 	    AppLogger.setUpLoggers(Level.OFF, new ConsoleHandler());
 	    CommandParser cp = new CommandParser(new H2DbBibleStorageFactory());
 	    cp.launch(args);
 	} catch (Exception e) {
 	    // for user
 	    System.out.println("Error: " + e.getMessage());
 	    // for log
 	    logger.error("Exception caught", e);
 	}
 
     }
 
     // used as Logger for SLF4J
     public static class AppLogger {
 	
 	private final static org.slf4j.Logger logger = LoggerFactory.getLogger(AppRunner.AppLogger.class);
 	
 	private static Level level = null;
 	private static Handler handler = null;
 	
 	private static final String LOG_LEVEL_PROPERTY_NAME = "log.level";
 	private static final String LOG_FILE_PROPERTY_NAME = "log.file";
 
 	/**
 	 * Sets the default log handler and log level for Logger used in bible4j.
 	 * This method also checks for presence of system properties 'log.level',
 	 * and 'log.file' and if exists, their value overrides default logging settings.
 	 */
 	public static void setUpLoggers(Level defaultLevel, Handler defaultHandler) {
 	    level = defaultLevel;
 	    handler = defaultHandler;
 	    getLoggingProperties();
 	    
 	    //reset logger
 	    Logger logger = Logger.getLogger(AppRunner.AppLogger.class.getName());
 	    LogManager.getLogManager().reset();
 	    
 	    //set logger settings
 	    logger.setLevel(level);
 	    handler.setLevel(level);
 	    logger.addHandler(handler);
 	    LogManager.getLogManager().addLogger(logger);
 	}
 
 	private static void getLoggingProperties() {
 	    if (System.getProperty(LOG_LEVEL_PROPERTY_NAME) != null)
 		try {
 		    level = Level.parse(System.getProperty(LOG_LEVEL_PROPERTY_NAME));
 		} catch (Exception e) {
 		    logger.warn("Exception caught when trying to set log level", e);
 		}
 	    
 	    if (System.getProperty(LOG_FILE_PROPERTY_NAME) != null)
 		try {
 		    handler = new FileHandler(System.getProperty(LOG_FILE_PROPERTY_NAME));
 		} catch (Exception e) {
 		    logger.warn("Exception caught when trying to set log file", e);
 		}
 	}
     }
 
 }
