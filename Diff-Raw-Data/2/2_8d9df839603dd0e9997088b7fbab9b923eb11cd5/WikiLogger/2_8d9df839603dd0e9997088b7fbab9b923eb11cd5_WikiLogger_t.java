 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.utils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.Properties;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * This class provides a wrapper around the {@link java.util.logging.Logger}
  * class, allowing additional utility methods to be included such as allowing
  * a log message to include a Throwable object.  From an implementation
  * standpoint it would have been much easier to simply sub-class the Logger
  * class, but that class is implemented in such a way to make sub-classes
  * exceedingly difficult to create.
  *
  * @see org.jamwiki.utils.WikiLogFormatter
  */
 public class WikiLogger {
 
 	private final Logger logger;
 
 	private static FileHandler DEFAULT_LOG_HANDLER = null;
 	private static Level DEFAULT_LOG_LEVEL = null;
 	/** Log configuration property file. */
 	public final static String LOG_PROPERTIES_FILENAME = "logging.properties";
 	public final static String DEFAULT_LOG_FILENAME = "jamwiki.log.0";
 
 	static {
 		initializeLogParams();
 	}
 
 	/**
 	 *
 	 */
 	private WikiLogger(Logger logger) {
 		this.logger = logger;
 	}
 
 	/**
 	 * Return the current ClassLoader.  First try to get the current thread's
 	 * ClassLoader, and if that fails return the ClassLoader that loaded this
 	 * class instance.
 	 *
 	 * @return An instance of the current ClassLoader.
 	 */
 	private static ClassLoader getClassLoader() {
 		// NOTE: This method duplicates the Utilities.getClassLoader method, but
 		// is copied here to prevent this class from having any dependencies on
 		// other JAMWiki classes.
 		ClassLoader loader = null;
 		try {
 			loader = Thread.currentThread().getContextClassLoader();
 		} catch (Exception e) {
 			// ignore, try the current class's ClassLoader
 		}
 		if (loader == null) {
 			loader = WikiLogger.class.getClassLoader();
 		}
 		return loader;
 	}
 
 	/**
 	 *
 	 */
 	public static String getDefaultLogFile() {
 		System.out.println(System.getProperties());
 		String logFile = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + DEFAULT_LOG_FILENAME;
 		return logFile;
 	}
 
 	/**
 	 *
 	 */
 	public static String getLogConfigFile() {
 		String logConfig = System.getProperty("file.separator") + "WEB-INF" + System.getProperty("file.separator") + "classes" + System.getProperty("file.separator") + LOG_PROPERTIES_FILENAME;
 		return logConfig;
 	}
 
 	/**
 	 * Retrieve a named <code>WikiLogger</code> object.
 	 *
 	 * @param name The name of the log object to retrieve or create.
 	 * @return A logger instance for the given name.
 	 */
 	public static WikiLogger getLogger(String name) {
 		Logger logger = Logger.getLogger(name);
 		if (WikiLogger.DEFAULT_LOG_HANDLER != null) {
 			logger.addHandler(WikiLogger.DEFAULT_LOG_HANDLER);
 			logger.setLevel(DEFAULT_LOG_LEVEL);
 		}
 		return new WikiLogger(logger);
 	}
 
 	/**
 	 *
 	 */
 	private static void initializeLogParams() {
 		FileInputStream stream = null;
 		try {
 			File propertyFile = WikiLogger.loadProperties();
 			stream = new FileInputStream(propertyFile);
 			Properties properties = new Properties();
 			properties.load(stream);
 			String pattern = properties.getProperty("org.jamwiki.pattern");
 			int limit = Integer.valueOf(properties.getProperty("org.jamwiki.limit"));
 			int count = Integer.valueOf(properties.getProperty("org.jamwiki.count"));
 			boolean append = Boolean.valueOf(properties.getProperty("org.jamwiki.append"));
 			String datePattern = properties.getProperty("org.jamwiki.timestamp");
 			DEFAULT_LOG_LEVEL = Level.parse(properties.getProperty("org.jamwiki.level"));
 			WikiLogger.DEFAULT_LOG_HANDLER = new FileHandler(pattern, limit, count, append);
 			DEFAULT_LOG_HANDLER.setFormatter(new WikiLogFormatter(datePattern));
 			DEFAULT_LOG_HANDLER.setLevel(DEFAULT_LOG_LEVEL);
 			// test the logger to verify permissions are OK
 			Logger logger = Logger.getLogger(WikiLogger.class.getName());
 			logger.addHandler(WikiLogger.DEFAULT_LOG_HANDLER);
 			logger.setLevel(DEFAULT_LOG_LEVEL);
 			logger.config("JAMWiki log initialized from " + propertyFile.getPath() + " with pattern " + pattern);
 		} catch (Exception e) {
			System.out.println("WARNING: Unable to load custom JAMWiki logging configuration, using system default.  The error message is: " + e.getMessage());
 			WikiLogger.DEFAULT_LOG_HANDLER = null;
 		} finally {
 			if (stream != null) {
 				try {
 					stream.close();
 				} catch (Exception ex) {}
 			}
 		}
 	}
 
 	/**
 	 *
 	 */
 	private static File loadProperties() throws FileNotFoundException {
 		ClassLoader loader = WikiLogger.getClassLoader();
 		URL url = loader.getResource(LOG_PROPERTIES_FILENAME);
 		if (url == null) {
 			throw new FileNotFoundException("Log initialization file " + LOG_PROPERTIES_FILENAME + " could not be found");
 		}
 		String fileName = url.getFile();
 		try {
 			fileName = URLDecoder.decode(fileName, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			// this should never happen
 			throw new IllegalStateException("Unsupporting encoding UTF-8");
 		} 
 		File propertyFile = new File(fileName);
 		if (!propertyFile.exists()) {
 			throw new FileNotFoundException("Log initialization file " + LOG_PROPERTIES_FILENAME + " could not be found");
 		}
 		return propertyFile;
 	}
 
 	/**
 	 * Log a message at the {@link java.util.logging.Level#CONFIG} level,
 	 * provided that the current log level is {@link java.util.logging.Level#CONFIG}
 	 * or greater.
 	 *
 	 * @param msg The message to be written to the log.
 	 */
 	public void config(String msg) {
 		this.logger.config(msg);
 	}
 
 	/**
 	 * Log a message and an exception at the {@link java.util.logging.Level#CONFIG}
 	 * level, provided that the current log level is {@link java.util.logging.Level#CONFIG}
 	 * or greater.
 	 *
 	 * @param msg The message to be written to the log.
 	 * @param thrown An exception to be written to the log.
 	 */
 	public void config(String msg, Throwable thrown) {
 		this.logger.log(Level.CONFIG, msg, thrown);
 	}
 
 	/**
 	 * Log a message at the {@link java.util.logging.Level#FINE} level,
 	 * provided that the current log level is {@link java.util.logging.Level#FINE}
 	 * or greater.
 	 *
 	 * @param msg The message to be written to the log.
 	 */
 	public void fine(String msg) {
 		this.logger.fine(msg);
 	}
 
 	/**
 	 * Log a message and an exception at the {@link java.util.logging.Level#FINE}
 	 * level, provided that the current log level is {@link java.util.logging.Level#FINE}
 	 * or greater.
 	 *
 	 * @param msg The message to be written to the log.
 	 * @param thrown An exception to be written to the log.
 	 */
 	public void fine(String msg, Throwable thrown) {
 		this.logger.log(Level.FINE, msg, thrown);
 	}
 
 	/**
 	 * Log a message at the {@link java.util.logging.Level#FINER} level,
 	 * provided that the current log level is {@link java.util.logging.Level#FINER}
 	 * or greater.
 	 *
 	 * @param msg The message to be written to the log.
 	 */
 	public void finer(String msg) {
 		this.logger.finer(msg);
 	}
 
 	/**
 	 * Log a message and an exception at the {@link java.util.logging.Level#FINER}
 	 * level, provided that the current log level is {@link java.util.logging.Level#FINER}
 	 * or greater.
 	 *
 	 * @param msg The message to be written to the log.
 	 * @param thrown An exception to be written to the log.
 	 */
 	public void finer(String msg, Throwable thrown) {
 		this.logger.log(Level.FINER, msg, thrown);
 	}
 
 	/**
 	 * Log a message at the {@link java.util.logging.Level#FINEST} level,
 	 * provided that the current log level is {@link java.util.logging.Level#FINEST}
 	 * or greater.
 	 *
 	 * @param msg The message to be written to the log.
 	 */
 	public void finest(String msg) {
 		this.logger.finest(msg);
 	}
 
 	/**
 	 * Log a message and an exception at the {@link java.util.logging.Level#FINEST}
 	 * level, provided that the current log level is {@link java.util.logging.Level#FINEST}
 	 * or greater.
 	 *
 	 * @param msg The message to be written to the log.
 	 * @param thrown An exception to be written to the log.
 	 */
 	public void finest(String msg, Throwable thrown) {
 		this.logger.log(Level.FINEST, msg, thrown);
 	}
 
 	/**
 	 * Log a message at the {@link java.util.logging.Level#INFO} level,
 	 * provided that the current log level is {@link java.util.logging.Level#INFO}
 	 * or greater.
 	 *
 	 * @param msg The message to be written to the log.
 	 */
 	public void info(String msg) {
 		this.logger.info(msg);
 	}
 
 	/**
 	 * Log a message and an exception at the {@link java.util.logging.Level#INFO}
 	 * level, provided that the current log level is {@link java.util.logging.Level#INFO}
 	 * or greater.
 	 *
 	 * @param msg The message to be written to the log.
 	 * @param thrown An exception to be written to the log.
 	 */
 	public void info(String msg, Throwable thrown) {
 		this.logger.log(Level.INFO, msg, thrown);
 	}
 
 	/**
 	 * Return <code>true</code> if a log message of level CONFIG can be logged.
 	 */
 	public boolean isConfigEnabled() {
 		return this.logger.isLoggable(Level.CONFIG);
 	}
 
 	/**
 	 * Return <code>true</code> if a log message of level FINE can be logged.
 	 */
 	public boolean isFineEnabled() {
 		return this.logger.isLoggable(Level.FINE);
 	}
 
 	/**
 	 * Return <code>true</code> if a log message of level FINER can be logged.
 	 */
 	public boolean isFinerEnabled() {
 		return this.logger.isLoggable(Level.FINER);
 	}
 
 	/**
 	 * Return <code>true</code> if a log message of level FINEST can be logged.
 	 */
 	public boolean isFinestEnabled() {
 		return this.logger.isLoggable(Level.FINEST);
 	}
 
 	/**
 	 * Return <code>true</code> if a log message of level INFO can be logged.
 	 */
 	public boolean isInfoEnabled() {
 		return this.logger.isLoggable(Level.INFO);
 	}
 
 	/**
 	 * Log a message at the {@link java.util.logging.Level#SEVERE} level,
 	 * provided that the current log level is {@link java.util.logging.Level#SEVERE}
 	 * or greater.
 	 *
 	 * @param msg The message to be written to the log.
 	 */
 	public void severe(String msg) {
 		this.logger.severe(msg);
 	}
 
 	/**
 	 * Log a message and an exception at the {@link java.util.logging.Level#SEVERE}
 	 * level, provided that the current log level is {@link java.util.logging.Level#SEVERE}
 	 * or greater.
 	 *
 	 * @param msg The message to be written to the log.
 	 * @param thrown An exception to be written to the log.
 	 */
 	public void severe(String msg, Throwable thrown) {
 		this.logger.log(Level.SEVERE, msg, thrown);
 	}
 
 	/**
 	 * Log a message at the {@link java.util.logging.Level#WARNING} level,
 	 * provided that the current log level is {@link java.util.logging.Level#WARNING}
 	 * or greater.
 	 *
 	 * @param msg The message to be written to the log.
 	 */
 	public void warning(String msg) {
 		this.logger.warning(msg);
 	}
 
 	/**
 	 * Log a message and an exception at the {@link java.util.logging.Level#WARNING}
 	 * level, provided that the current log level is {@link java.util.logging.Level#WARNING}
 	 * or greater.
 	 *
 	 * @param msg The message to be written to the log.
 	 * @param thrown An exception to be written to the log.
 	 */
 	public void warning(String msg, Throwable thrown) {
 		this.logger.log(Level.WARNING, msg, thrown);
 	}
 }
