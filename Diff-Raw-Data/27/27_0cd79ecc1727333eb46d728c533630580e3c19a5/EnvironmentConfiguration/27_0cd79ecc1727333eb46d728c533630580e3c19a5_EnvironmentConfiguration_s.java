 package com.gentics.cr.configuration;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.apache.jcs.engine.control.CompositeCacheManager;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import com.gentics.cr.util.CRUtil;
 
 /**
  * Environment Configuration manages the configuration of log4j and jcs.
  * Last changed: $Date: 2011-11-28 09:44:25 +0100 (Mo, 28 Nov 2011) $
  * @version $LastChangedRevision: 1545 $
  * @author $Author: bigbear.ap@gmail.com $
  *
  */
 public final class EnvironmentConfiguration {
 
 	/**
	 * Path were the configuration files are found
 	 */
 	private static String configurationPath = "${" + CRUtil.PORTALNODE_CONFPATH + "}";
 
 	/**
 	 * Path were the configuration files are found.
 	 * This variable is deprecated. Use {@link #getConfigPath()} instead.
 	 */
 	@Deprecated
 	public static final String CONFPATH = configurationPath;
 
 	/**
 	 * Path to the default log4j property file.
 	 */
 	private static String loggerFilePath = configurationPath + "/nodelog.properties";
 
 	/**
 	 * Path to the jcs configuration file.
 	 */
 	private static String cacheFilePath = configurationPath + "/cache.ccf";
 
 	/**
 	 * Configuration key if we should use the same caches as Gentics
 	 * Portal.Node.
 	 */
 	private static final String USE_PORTAL_CACHE_KEY = "com.gentics.cr.useportalcaches";
 
 	/**
 	 * Default log4j logger.
 	 */
 	private static Logger log;
 
 	/**
 	 * Detect if log4j property loading already failed, so we put out the error
 	 * message only once.
 	 */
 	private static boolean loggerInitFailed = false;
 
 	/**
 	 * Detect if cache property loading already failed, so we put out the error
 	 * message only once.
 	 */
 	private static boolean cacheInitFailed = false;
 
 	/**
 	 * private constructor to prevent initializing of the utility class.
 	 */
 	private EnvironmentConfiguration() {
 	}
 
 	/**
 	 * Load Environment Properties.
 	 *		 - load logger properties for log4j
 	 *		 - load cache properties for JCS
 	 */
 	public static void loadEnvironmentProperties() {
 		loadLoggerProperties();
 		loadCacheProperties();
 	}
 
 	/**
 	 * Load Property file for log4j.
 	 */
 	public static void loadLoggerProperties() {
 		Properties logprops = new Properties();
 		String confpath = CRUtil.resolveSystemProperties(loggerFilePath);
 		StringBuilder errorMessage = new StringBuilder("Could not find nodelog.properties at: ").append(confpath);
 		try {
 			logprops.load(new FileInputStream(confpath));
 			PropertyConfigurator.configure(logprops);
 			log = Logger.getLogger(EnvironmentConfiguration.class);
 		} catch (IOException e) {
 			if (!loggerInitFailed) {
 				System.out.println(errorMessage.toString());
 				loggerInitFailed = true;
 			}
 		} catch (NullPointerException e) {
 			if (!loggerInitFailed) {
 				System.out.println(errorMessage.toString());
 				loggerInitFailed = true;
 			}
 		}
 	}
 
 	/**
 	 * @return <code>true</code> if the logger init has not (yet) failed, otherwhise false.
 	 */
 	public static boolean getLoggerState() {
 		return !loggerInitFailed;
 	}
 
 	/**
 	 * Load Property file for JCS cache.
 	 */
 	public static void loadCacheProperties() {
 		String confpath = CRUtil.resolveSystemProperties(cacheFilePath);
		StringBuilder errorMessage = new StringBuilder("Could not load cache configuration. Perhaps you are missing the file cache.ccf in ").append(confpath).append("!");
 		logDebug("Loading cache configuration from " + confpath);
 		try {
 			//LOAD CACHE CONFIGURATION
 			Properties cacheProps = new Properties();
 			cacheProps.load(new FileInputStream(confpath));
			if (cacheProps.containsKey(USE_PORTAL_CACHE_KEY)
					&& Boolean.parseBoolean(cacheProps.getProperty(USE_PORTAL_CACHE_KEY))) {
				logDebug("Will not initialize ContentConnector Cache - Using the "
						+ "cache configured by portalnode instead.");
 			} else {
 				CompositeCacheManager cManager = CompositeCacheManager.getUnconfiguredInstance();
 				cManager.configure(cacheProps);
 			}
 		} catch (NullPointerException e) {
 			if (!cacheInitFailed) {
 				logError(errorMessage.toString());
 				cacheInitFailed = true;
 			}
 		} catch (FileNotFoundException e) {
 			if (!cacheInitFailed) {
 				logError(errorMessage.toString());
 				cacheInitFailed = true;
 			}
 		} catch (IOException e) {
 			if (!cacheInitFailed) {
 				logError(errorMessage.toString());
 				cacheInitFailed = true;
 			}
 		}
 	}
 
 	/**
 	 * detect if log4j was initialized properly.
 	 * @return true if we got an log4j logger false otherwise
 	 */
 	private static boolean loggerInitialized() {
 		return !(log == null);
 	}
 
 	/**
 	 * Method to log an error message.
 	 * @param message error message to log
 	 */
 	private static void logError(final String message) {
 		if (!loggerInitialized()) {
 			loadLoggerProperties();
 		}
 		if (loggerInitialized()) {
 			log.error(message);
 		} else {
 			System.out.println(message);
 		}
 
 	}
 
 	/**
 	 * Method to log a debug message.
 	 * @param message debug message to log
 	 */
 	private static void logDebug(final String message) {
 		if (!loggerInitialized()) {
 			loadLoggerProperties();
 		}
 		if (loggerInitialized()) {
 			log.debug(message);
 		} else {
 			System.out.println(message);
 		}
 	}
 
 	/**
 	 * @return the current log4j property file path.
 	 */
 	public static String getLoggerConfigPath() {
 		return loggerFilePath;
 	}
 
 	/**
 	 * set a new logger configuration file path where the log4j properties file is located from .
	 * @param newCacheFilePath - path for the log4j configuration file
 	 */
	public static void setLoggerConfigPath(String newLoggerFilePath) {
 		loggerFilePath = newLoggerFilePath;
 	}
 
 	/**
 	 * set the path for the configuration files.
 	 * @param configLocation - directory which contains the configuration files
 	 */
	public static void setConfigPath(String configLocation) {
 		configurationPath = configLocation;
 		System.setProperty(CRUtil.PORTALNODE_CONFPATH, configLocation);
 		loggerFilePath = configurationPath + "/nodelog.properties";
 		cacheFilePath = configurationPath + "/cache.ccf";
 	}
 
 	/**
 	 * @return the path where the configuration files are loaded from.
 	 */
 	public static String getConfigPath() {
 		return configurationPath;
 	}
 
 	/**
 	 * @return the current cache file path.
 	 */
 	public static String getCacheFilePath() {
 		return cacheFilePath;
 	}
 
 	/**
 	 * set a new cache file path where the jcs cache.ccf is located.
 	 * @param newCacheFilePath - path for the jcs cache configuration file
 	 */
	public static void setCacheFilePath(String newCacheFilePath) {
 		cacheFilePath = newCacheFilePath;
 	}
 }
