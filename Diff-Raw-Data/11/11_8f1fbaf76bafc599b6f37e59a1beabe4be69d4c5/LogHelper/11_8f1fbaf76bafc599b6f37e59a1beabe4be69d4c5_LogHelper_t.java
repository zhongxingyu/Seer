 package nl.nikhef.jgridstart.logging;
 
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 import nl.nikhef.jgridstart.util.GeneralUtils;
 
 /** Logging initialisation */
 public class LogHelper {
     static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.logging");
     
     /** Setup logging for the application */
     public static void setupLogging(boolean debug) {
 	// load config
 	final String conf = debug ? "/logging.debug.properties" : "/logging.properties";
 	try {
 	    LogManager.getLogManager().readConfiguration(LogHelper.class.getResourceAsStream(conf));
 	} catch(Exception e) {
 	    System.out.println("Warning: logging configuration could not be set");
 	}
    }
    
    /** Log environment information */
    public static void logEnvironment() {
 	// emit some extra info
 	logger.info("Platform: "+getSystemProperty("os.name")+" "+getSystemProperty("os.arch")+" " +
 		"version "+getSystemProperty("sys.version"));
 	logger.info("JRE: "+getSystemProperty("java.vendor")+" version "+getSystemProperty("java.version") +
 		" JIT "+getSystemProperty("java.compiler") + "; " +
 		"installed in "+getSystemProperty("java.home"));
 	logger.info("  classpath="+getSystemProperty("java.class.path"));
     }
 
     /** Retrieve system property safely.
      * <p>
      * This method doesn't throw an exception when the value cannot be accessed,
      * but returns {@literal "<protected>"} instead.
      */
     protected static String getSystemProperty(String name) {
 	return GeneralUtils.getSystemProperty(name);
     }
 }
