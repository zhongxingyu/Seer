 /* --------------------------------------------------------------------------
  * @author Hauke Walden
  * @created 28.06.2011 
  * Copyright 2011 by Hauke Walden 
  * All rights reserved.
  * --------------------------------------------------------------------------
  */
 package de.mbaaba.util;
 
 /**
  * properties for utility.
  * @author seidelt_a1
  */
 public final class UtilityProperties {
 
 
 	/**
 	 * types of appender.
 	 */
 	public enum AppenderType {
 		/** normal appender */ 
 		STANDARD, 
 		/** hub appender */
 		HUB,
 		/** async appender */
 		ASYNC, 
 		/** no appender */
 		NONE
 	}
 
 	/** property */
 	private static final String XSLFILTERCONFIG = "utility.xslFilterConfig";
 
 	/** property */
 	private static final String LOG_PROPERTIES_FILE = "utility.log.properties";
 
 	/** property */
 	private static final String LOG_LEVEL = "utility.log.level";
 
 	/** property */
 	private static final String LOG_HOST = "utility.log.host";
 
 	/** property */
 	private static final String LOG_PORT = "utility.log.port";
 
 	/** property */
 	private static final String LOG_FILE = "utility.log.file";
 
 	/** property */
 	private static final String LOG_MODE = "utility.log.mode";
 
 	/** property */
 	private static final String LOG_EXIT_ON_FATAL = "utility.log.exit_on_fatal";
 
 	/** property */
 	private static final String SYSLOG_HOST = "utility.syslog.host";
 
 	/** property */
 	private static final String SYSLOG_FACILITY = "utility.syslog.facility";
 
 	/** property */
 	private static final String LOG_CREATE_PREFIX = "utility.log.create_prefix";
 
 	/** property */
 	private static final String LOG_APPENDER = "utility.log.appender";
 
 	/** property */
 	private static final String ENHANCE_COMMONS_LOGGING = "utility.log.enhance_commons_logging";
 	
 	/** property */
 	private static final String ASYNC_BUFFER_SIZE = "utility.log.async_buffer_size";
 	
 	/** property */
 	private static final int DEFAULT_LOGPORT = 4445;
 
 	/** property */
 	private static final String DEFAULT_LOG_EXIT_ON_FATAL = "true";
 
 	/** property */
 	private static final String DEFAULT_LOGMODE = "true";
 
 	/** property */
 	private static final String DEFAULT_LOG_CREATE_PREFIX = "true";
 
 	/** property */
 	private static final String DEFAULT_ENHANCE_COMMONS_LOGGING = "true";
 	
 	/** property */
 	private static final AppenderType DEFAULT_LOG_APPENDER = AppenderType.STANDARD;
 
 	/** property */
 	private static final String DEFAULT_LOG_PROPERTIES_FILE = "/log4j.properties";
 	
 	/** property */
 	private static final int DEFAULT_ASYNC_BUFFER_SIZE = 10000;
 
 	/**
 	 * inhibit instanciation
 	 */
 	private UtilityProperties() {
 	}
 
 	/**
 	 * return state repository startup.
 	 * @return state repository startup.
 	 */
 	public static boolean exitOnFatal() {
 		String s = PropertiesHelper.getProperty(LOG_EXIT_ON_FATAL, DEFAULT_LOG_EXIT_ON_FATAL);
 		return s.equalsIgnoreCase("true");
 	}
 
 	/**
 	 * return the logmode, <code>true</code> for devel-mode (the old mode) and <code>false</code> for admin-mode.
 	 * @return the logmode, <code>true</code> for devel-mode (the old mode) and <code>false</code> for admin-mode.
 	 */
 	public static boolean isDevelLogMode() {
 		String s = PropertiesHelper.getProperty(LOG_MODE, DEFAULT_LOGMODE);
 		return s.equalsIgnoreCase("true");
 	}
 
 	/**
 	 * return path to xsl filter
 	 * @return path to xsl filter
 	 */
 	public static String getXslFilterConfig() {
 		return PropertiesHelper.getProperty(XSLFILTERCONFIG);
 	}
 
 	/**
 	 * return logging level.
 	 * @return logging level.
 	 */
 	public static String getLogLevel() {
 		return PropertiesHelper.getProperty(LOG_LEVEL, "debug");
 	}
 
 	/**
 	 * return logging host.
 	 * @return logging host.
 	 */
 	public static String getLoghost() {
 		return PropertiesHelper.getProperty(LOG_HOST, "localhost");
 	}
 
 	/**
 	 * return logging port.
 	 * @return logging port.
 	 */
 	public static int getLogport() {
 		try {
 			return Integer.parseInt(PropertiesHelper.getProperty(LOG_PORT, String.valueOf(DEFAULT_LOGPORT)));
 		} catch (NumberFormatException e) {
 			System.out.println("illegal logport specified, using default " + DEFAULT_LOGPORT);
 			return DEFAULT_LOGPORT;
 		}
 	}
 
 	/**
 	 * return logging file.
 	 * @return logging file.
 	 */
 	public static String getLog4JPropertiesFile() {
 		return PropertiesHelper.getProperty(LOG_PROPERTIES_FILE, DEFAULT_LOG_PROPERTIES_FILE);
 	}
 
 	/**
 	 * return logging file.
 	 * @return logging file.
 	 */
 	public static String getLogfile() {
 		return PropertiesHelper.getProperty(LOG_FILE);
 	}
 
 	/**
 	 * return logging syslog host.
 	 * @return logging syslog host.
 	 */
 	public static String getSysloghost() {
 		return PropertiesHelper.getProperty(SYSLOG_HOST);
 	}
 
 	/**
 	 * return logging syslog facility.
 	 * @return logging syslog facility.
 	 */
 	public static String getSyslogFacility() {
 		return PropertiesHelper.getProperty(SYSLOG_FACILITY);
 	}
 
 	/**
 	 * return true, if prefixes shall be used for logger names.
 	 * @return true, if prefixes shall be used for logger names.
 	 */
 	public static boolean isUsePrefix() {
 		String s = PropertiesHelper.getProperty(LOG_CREATE_PREFIX, DEFAULT_LOG_CREATE_PREFIX);
 		return s.equalsIgnoreCase("true");
 	}
 
 	/**
 	 * return true, if commons logging shall be enhanced
 	 * @return true, if commons logging shall be enhanced.
 	 */
 	public static boolean isEnhanceCommonsLogging() {
 		String s = PropertiesHelper.getProperty(ENHANCE_COMMONS_LOGGING, DEFAULT_ENHANCE_COMMONS_LOGGING);
 		return s.equalsIgnoreCase("true");
 	}
 	
 	/**
 	 * returns the async buffer size
 	 * @return the async buffer size
 	 */
 	public static int getAsyncBufferSize() {
 		try {
 			return Integer.parseInt(PropertiesHelper.getProperty(ASYNC_BUFFER_SIZE, String.valueOf(DEFAULT_ASYNC_BUFFER_SIZE)));
 		} catch (NumberFormatException e) {
 			System.out.println("illegal async buffer size specified, using default " + DEFAULT_ASYNC_BUFFER_SIZE);
 			return DEFAULT_ASYNC_BUFFER_SIZE;
 		}
 	}
 	
 	/**
 	 * return the appender.
 	 * @return the appender.
 	 */
 	public static AppenderType getLogAppender() {
 		AppenderType at;
 		try {
 			at = AppenderType.valueOf(PropertiesHelper.getProperty(LOG_APPENDER, AppenderType.STANDARD.name()).toUpperCase());
 		} catch (IllegalArgumentException e) {
 			System.out.println("illegal appender specified, using default " + DEFAULT_LOG_APPENDER.name());
 			return DEFAULT_LOG_APPENDER;
 		}
 
 		return at;
 	}
 	
 	public static void main(String[] args) {
		Wasneues1.main(args);
 	}
 	
 }
