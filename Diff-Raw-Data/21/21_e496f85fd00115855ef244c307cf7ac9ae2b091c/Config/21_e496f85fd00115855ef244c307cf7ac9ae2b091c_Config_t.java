 package socio;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.util.Properties;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 public class Config {
 
 	/**
 	 * Implementation of a thread safe, synchronized singleton pattern
 	 * {@link http://de.wikibooks.org/wiki/Java_Standard:_Muster_Singleton}.
 	 */
 	private static Config instance;
 
 	public static synchronized Config getInstance() {
 		if (Config.instance == null) {
 			Config.instance = new Config(false);
 		}
 		return Config.instance;
 	}
 
 	/**
 	 * Enables JUnit test mode
 	 * 
 	 * @return
 	 */
 	public static synchronized Config testmode() {
 		Config.instance = new Config(true);
 		return Config.instance;
 	}
 
 	private static final Logger LOGGER = Logger.getLogger(Config.class);
 
 	/**
 	 * Try to load the file from these ENV-variables. Descending order of
 	 * importance.
 	 */
 	private static final String[] SEARCH_PATHS = new String[] { "SOCIO_HOME", "USERPROFILE", "HOME", "APPDATA" };
 	private static final String FILE_NAME = "socio.properties";
 
 	private Properties properties;
 	private Properties defaultProperties;
 
	private Config(Boolean forceDebug) {
		loadProperties(forceDebug);
 
		while (!forceDebug && !isValidXmppId(properties.getProperty("xmpp.user"))) {
 			setupWizard();
 		}
 	}
 
	private void loadProperties(Boolean forceDebug) {
 		try {
 			defaultProperties = new Properties();
 			defaultProperties.load(Launcher.class.getClassLoader().getResourceAsStream("default.properties"));
 		} catch (Exception e) {
 			LOGGER.error("Could not load default properties file");
 		}
 
 		try {
			if (properties == null)
				properties = new Properties(defaultProperties);
 
			InputStream propertiesFile = (properties.getProperty("debug").equals("true") || forceDebug) ? Launcher.class.getClassLoader().getResourceAsStream("debug.properties")
					: new FileInputStream(pathfinder(FILE_NAME));
 			properties.load(propertiesFile);
 
 			Logger.getRootLogger().setLevel(Level.toLevel(properties.getProperty("rootlogger.level")));
 			LOGGER.debug("Set log level to " + properties.getProperty("rootlogger.level"));
 		} catch (Exception e) {
 			LOGGER.error("Could not load properties file", e);
 		}
 	}
 
 	private void setupWizard() {
 		try {
 			System.out.println("SocIO setup");
 			this.properties = new Properties();
 			String input = null;
 
 			while (!isValidXmppId(input)) {
 				System.out.print("Please input a valid XMPP id (in the form xmpp://user@example.com): ");
 				byte[] buffer = new byte[100];
 				System.in.read(buffer, 0, 99);
 
 				input = (new String(buffer)).trim();
 				LOGGER.debug("User entered username \"" + input + "\"");
 			}
 
 			properties.setProperty("xmpp.user", input);
 			input = "";
 
 			while ("".equals(input)) {
 				System.out.print("Please input an password: ");
 				byte[] buffer = new byte[100];
 				System.in.read(buffer, 0, 99);
 
 				input = (new String(buffer)).trim();
 				LOGGER.debug("User entered a password");
 			}
 
 			properties.setProperty("xmpp.pass", input);
 
 		} catch (Exception e) {
 			LOGGER.error("Could not retrieve user inputs:", e);
 		}
 
 		try {
 			properties.store(new FileOutputStream(FILE_NAME), "SocIO Configuration File");
 		} catch (Exception e) {
 			LOGGER.error("Could not store properties file " + FILE_NAME, e);
 		}
 	}
 
 	public void parseCommandline(String[] args) {
 		for (String parameter : args) {
 			LOGGER.info("Manual override: " + parameter + " = true");
 			Config.getInstance().properties.setProperty(parameter, "true");
 		}
		
		loadProperties(false);
 	}
 
 	/**
 	 * Basic user name validations
 	 * 
 	 * @return A valid user name / null
 	 */
 	public static Boolean isValidXmppId(String userName) {
 		if (userName == null)
 			return false;
 
 		if (!userName.startsWith("xmpp://"))
 			return false;
 
 		if (!(userName.split("@").length == 2))
 			return false;
 
 		return true;
 	}
 
 	/**
 	 * Tries to find the given file at several paths. Defaults to current
 	 * working directory.
 	 */
 	public String pathfinder(String filename) {
 		LOGGER.debug("Trying to find " + filename);
 		for (String path : SEARCH_PATHS) {
 			String probePath = System.getenv(path) + "/" + filename;
 			LOGGER.debug("Probing " + probePath);
 			if (new File(probePath).exists()) {
 				LOGGER.debug("Found properties file at " + probePath);
 				return probePath;
 			}
 		}
 		return filename;
 	}
 
 	// Generic getter methods
 
 	private static String getProperty(String key) {
 		return Config.getInstance().properties.getProperty(key);
 	}
 
 	private static String getStringProperty(String key) {
 		return (getProperty(key) != null ? getProperty(key) : "");
 	}
 
 	private static Boolean getBooleanProperty(String key) {
 		return getProperty(key) != null && getProperty(key).equals("true");
 	}
 
 	private static int getIntegerProperty(String key) {
 		return (getProperty(key) != null ? new Integer(getProperty(key)) : 0);
 	}
 
 	// Getter methods for specific settings
 
 	public static String getRootLogLevel() {
 		return getStringProperty("rootlogger.level");
 	}
 
 	public static String getProxyAddress() {
 		return getStringProperty("proxy.address");
 	}
 
 	public static int getProxyPort() {
 		return getIntegerProperty("proxy.port");
 	}
 
 	public static String getServerAddress() {
 		return getXmppUserId().split("@")[1];
 	}
 
 	public static String getUserName() {
 		return getXmppUserId().split("@")[0].replaceAll("xmpp://", "");
 	}
 
 	public static int getServerPort() {
 		return getIntegerProperty("xmpp.server.port");
 	}
 
 	public static String getXmppUserId() {
 		return getStringProperty("xmpp.user");
 	}
 
 	public static String getPassword() {
 		return getStringProperty("xmpp.pass");
 	}
 
 	public static Boolean useProxy() {
 		return getBooleanProperty("useproxy");
 	}
 
 	public static Boolean isHeadless() {
 		return getBooleanProperty("headless");
 	}
 
 	public static Boolean isDebug() {
 		return getBooleanProperty("debug");
 	}
 
 	public static boolean isReadonly() {
 		return getBooleanProperty("readonly");
 	}
 
 	public static boolean isOffline() {
 		return getBooleanProperty("offline");
 	}
 
 	public static boolean disableTray() {
 		return getBooleanProperty("disabletray");
 	}
 
 	public static String getRdfStoreFile() {
 		return getStringProperty("rdf.file");
 	}
 
 	public static int getRestPort() {
 		return getIntegerProperty("rest.port");
 	}
 
 }
