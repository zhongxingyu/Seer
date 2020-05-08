 package utils;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 public class LoggerInitializer {
 	
 	private static final Logger LOG = Logger.getLogger(LoggerInitializer.class.getName());
 	
 	private static final String PROP_NAME = "logging.properties";
 	
	static {
 		String loggingProperties = System.getProperty(PROP_NAME);
 		if (loggingProperties == null) {
 			LOG.severe("Could not find property: " + PROP_NAME);
 		} else {
 			try {
 				LogManager.getLogManager().readConfiguration(new FileInputStream(loggingProperties));
 				LOG.info("Logger configuration loaded up correctly from file: " + loggingProperties);
 			} catch (IOException e) {
 				LOG.severe("Could not load up logger configuration file at: " + loggingProperties);
 			}			
 		}
 	}
 
 }
