 package nl.nikhef.jgridstart.util;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import nl.nikhef.jgridstart.gui.Main;
 
 /** General utility functions that don't fit elsewhere */
 public class GeneralUtils {
     
     static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.util");
     
     /** Properties resource containing standard configuration @see #loadConfig */
     protected static final String standardConfig = "/resources/conf/global.properties";
     
     /** Loads standard system properties from configuration.
      * <p>
      * Reads the standard configuration properties into the {@linkplain System}
      * property store. Doesn't overwrite existing properties, so that they
      * can be overridden from the command-line.
      * <p>
      * In addition to this, all properties that have a name starting with
      * {@literal jnlp.jgridstart.<x>} or {@literal javaws.jgridstart.<x>} are
      * renamed to {@literal jgridstart.<x>} before the standard configuration
      * is read. This allows one to modify jgridstart
      * <a href="http://java.sun.com/javase/6/docs/technotes/guides/javaws/developersguide/syntax.html#resources">properties</a>
      * in the jnlp file without requiring to
      * <a href="http://java.sun.com/javase/technologies/desktop/javawebstart/download-spec.html">sign</a> it.
      * 
      * @throws IOException
      */
     public static void loadConfig() throws IOException {
 	// rename java web start properties
 	ArrayList<String> keys = new ArrayList<String>();
 	for (Enumeration<?> e = System.getProperties().keys(); e.hasMoreElements(); ) {
 	    String key = (String)e.nextElement();
 	    if (key.startsWith("jnlp.jgridstart.") || key.startsWith("javaws.jgridstart."))
 		keys.add(key);
 	}
 	for (Iterator<String> it = keys.iterator(); it.hasNext(); ) {
 	    String key = it.next();
 	    String newkey = key.replaceFirst("^(jnlp|javaws)\\.(jgridstart\\.)", "$2");
 	    System.setProperty(newkey, System.getProperty(key));
 	    System.clearProperty(key);
 	}
 	// load standard configuration
 	Properties p = getConfig();
 	for (Enumeration<?> e = p.keys(); e.hasMoreElements(); ) {
 	    String key = (String)e.nextElement();
 	    if (key!=null && System.getProperty(key)==null)
 		System.setProperty(key, p.getProperty(key));
 	}
 	logger.info(getVersionString() + ", configuration loaded");
 	// set user-agent now that jgridstart version is known; but don't make it fatal
 	try {
 	    System.setProperty("http.agent", getUserAgentString());
	    logger.fine("Set user-agent: "+System.getProperty("user.agent"));
 	} catch(Exception e) {
 	    logger.warning("Could not set user-agent: "+e.getLocalizedMessage());
 	}
     }
     
     /** Returns default properties @see #loadConfig */
     public static Properties getConfig() throws IOException {
 	Properties p = new Properties();
 	p.load(Main.class.getResourceAsStream(standardConfig));
 	return p;
     }
     
     /** Retrieve system property safely.
      * <p>
      * This method doesn't throw an exception when the value cannot be accessed,
      * but returns {@literal "<protected>"} instead.
      */
     public static String getSystemProperty(String name) {
 	try {
 	    return System.getProperty(name);
 	} catch(java.security.AccessControlException e) {
 	    return "<protected>";
 	}
     }
     
     /** Return jGridstart's version string */
     public static String getVersionString() {
 	return "jGridstart " + getSystemProperty("jgridstart.version") +
 	       " r" + getSystemProperty("jgridstart.revision");
     }
     
     /** Return jGridstart's user-agent string.
      * <p>
      * This string is a complete user-agent string, described by
      * <a href="http://www-archive.mozilla.org/build/revised-user-agent-strings.html">http://www-archive.mozilla.org/build/revised-user-agent-strings.html</a>,
      * although the OS/CPU value is not strictly translated to Mozilla's conventions.
      * <p>
      * Added to the comment (the stuff between brackets) is the CA implementation.
      * <p>
      * Example output:
      *   {@literal jGridstart/1.1_1234 (X11; I; Linux amd64; TestCA)}
      * <p>
      * Note that Java adds something like {@literal Java/1.6.0_01} at the end for the
      * final user-agent sent to the remote end.
      */
     public static String getUserAgentString() {
 	String platform = "X11";
 	if (getSystemProperty("os.name").startsWith("Mac")) platform = "Macintosh";
 	else if (getSystemProperty("os.name").startsWith("Win")) platform = "Windows";
 	return "jGridstart/" + getSystemProperty("jgridstart.version") +
 	       "_" + getSystemProperty("jgridstart.revision") +
 	       " (" + platform + "; I; " +
 	           getSystemProperty("os.name") + " " + getSystemProperty("os.arch") + "; " +
 	           getSystemProperty("jgridstart.ca.provider") +
 	       ")";
     }
 }
