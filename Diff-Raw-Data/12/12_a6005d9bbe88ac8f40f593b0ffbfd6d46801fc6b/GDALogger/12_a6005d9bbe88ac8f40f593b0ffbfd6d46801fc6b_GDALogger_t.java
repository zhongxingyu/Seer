 /*-
  * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
  * Facilities Council Daresbury Laboratory
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.util;
 
 import gda.device.corba.CorbaDeviceException;
 
 import java.util.ArrayList;
 import java.util.regex.Pattern;
 
 import org.slf4j.LoggerFactory;
 
 import sun.reflect.Reflection;
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.classic.joran.JoranConfigurator;
 import ch.qos.logback.core.joran.spi.JoranException;
 
 /**
  * Static class which does away with the need to have an instance of Logger for each class and reintroduces the ability
  * to set a debug level. All methods need to be synchronized because there is only one logger for many threads.
  */
 public class GDALogger {
 
 	private static int debugLevel = 0;
 	private static ArrayList<Class<?>> excludedClasses = new ArrayList<Class<?>>();
 
 	/**
 	 * Configure the logging system.
 	 * 
 	 * @param xmlConfigFile
 	 */
 	public static void configureLogging(String xmlConfigFile) {
 
 		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
 
 		JoranConfigurator configurator = new JoranConfigurator();
 		configurator.setContext(lc);
 		lc.reset();
 		try {
 			configurator.doConfigure(xmlConfigFile);
 		} catch (JoranException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Sets the debug level from a set of main program args
 	 * 
 	 * @param args
 	 */
 	public static synchronized void setDebugLevel(String[] args) {
 
 		Pattern p = Pattern.compile("-d[0-9]");
 		for (String arg : args) {
 			if (p.matcher(arg).matches()) {
 				setDebugLevel(Integer.valueOf(arg.substring(2)));
 			}
 		}
 	}
 
 	/**
 	 * Sets the debug level.
 	 * 
 	 * @param newLevel
 	 */
 	public static synchronized void setDebugLevel(int newLevel) {
 
 		debugLevel = newLevel;
 	}
 
 	/**
 	 * Output a debug string depending on level parameter.
 	 * 
 	 * @param string
 	 *            the string to output
 	 * @param level
 	 *            the level
 	 */
 	public static synchronized void debug(String string, int level) {
 
 		if (debugLevel >= level) {
 			Class<?> c = getCallerClass();
 			if (!excludedClasses.contains(c)) {
 				LoggerFactory.getLogger(c).debug(string);
 			}
 		}
 	}
 
 	/**
 	 * Output a debug string depending on level parameter, but use SLF4J parameterized format.
 	 * 
 	 * @param format
 	 *            the string to output
 	 * @param arg
 	 *            the object to place in the string
 	 * @param level
 	 *            the level
 	 */
 	public static synchronized void debug(String format, Object arg, int level) {
 
 		if (debugLevel >= level) {
 			Class<?> c = getCallerClass();
 			if (!excludedClasses.contains(c)) {
 				LoggerFactory.getLogger(c).debug(format, arg);
 			}
 		}
 	}
 
 	/**
 	 * Output a debug string depending on level parameter, but use SLF4J parameterized format.
 	 * 
 	 * @param format
 	 *            the string to output
 	 * @param arg
 	 *            the object to place in the string
 	 * @param arg2
 	 *            the 2nd object to place in the string
 	 * @param level
 	 *            the level
 	 */
 	public static synchronized void debug(String format, Object arg, Object arg2, int level) {
 
 		if (debugLevel >= level) {
 			Class<?> c = getCallerClass();
 			if (!excludedClasses.contains(c)) {
 				LoggerFactory.getLogger(c).debug(format, arg, arg2);
 			}
 		}
 	}
 
 	/**
 	 * Output a debug string depending on level parameter, provides information about the calling method as well as the
 	 * calling class.
 	 * 
 	 * @param string
 	 *            the string to output
 	 * @param level
 	 *            the level
 	 */
 	public static synchronized void superDebug(String string, int level) {
 
 		if (debugLevel >= level) {
 			Class<?> c = getCallerClass();
 			if (!excludedClasses.contains(c)) {
 				StackTraceElement ste = new Throwable().getStackTrace()[1];
 				LoggerFactory.getLogger(c).debug(ste.getMethodName() + "(line: " + ste.getLineNumber() + ")" + string);
 			}
 		}
 	}
 
 	/**
 	 * Outputs a debug string regardless of the setting of debugLevel.
 	 * 
 	 * @param string
 	 */
 	public static synchronized void debug(String string) {
 
 		Class<?> c = getCallerClass();
 		if (!excludedClasses.contains(c)) {
 			LoggerFactory.getLogger(c).debug(string);
 		}
 	}
 
 	/**
 	 * Output a debug string depending on level parameter, but use SLF4J parameterized format.
 	 * 
 	 * @param format
 	 *            the string to output
 	 * @param arg
 	 *            the object to place in the string
 	 */
 	public static synchronized void debug(String format, Object arg) {
 
 		Class<?> c = getCallerClass();
 		if (!excludedClasses.contains(c)) {
 			LoggerFactory.getLogger(c).debug(format, arg);
 		}
 	}
 
 	/**
 	 * Output a debug string depending on level parameter, but use SLF4J parameterized format.
 	 * 
 	 * @param format
 	 *            the string to output
 	 * @param arg
 	 *            the object to place in the string
 	 * @param arg2
 	 *            the 2nd object to place in the string
 	 */
 	public static synchronized void debug(String format, Object arg, Object arg2) {
 
 		Class<?> c = getCallerClass();
 		if (!excludedClasses.contains(c)) {
 			LoggerFactory.getLogger(c).debug(format, arg, arg2);
 		}
 	}
 
 	/**
 	 * Outputs a warning string.
 	 * 
 	 * @param string
 	 */
 	public static synchronized void warn(String string) {
 
 		Class<?> c = getCallerClass();
 		if (!excludedClasses.contains(c)) {
 			LoggerFactory.getLogger(c).warn(string);
 		}
 	}
 
 	/**
 	 * Outputs a warning string.
 	 * 
 	 * @param string
 	 * @param arg1
 	 */
 	public static synchronized void warn(String string, Object arg1) {
 
 		Class<?> c = getCallerClass();
 		if (!excludedClasses.contains(c)) {
 			LoggerFactory.getLogger(c).warn(string, arg1);
 		}
 	}
 
 	/**
 	 * Outputs a warning string.
 	 * 
 	 * @param string
 	 * @param arg1
 	 * @param arg2
 	 */
 	public static synchronized void warn(String string, Object arg1, Object arg2) {
 
 		Class<?> c = getCallerClass();
 		if (!excludedClasses.contains(c)) {
 			LoggerFactory.getLogger(c).warn(string, arg1, arg2);
 		}
 	}
 
 	/**
 	 * Outputs an error string.
 	 * 
 	 * @param string
 	 *            the string to output
 	 */
 	public static synchronized void error(String string) {
 
 		Class<?> c = getCallerClass();
 		if (!excludedClasses.contains(c)) {
 			LoggerFactory.getLogger(c).error(string);
 		}
 	}
 
 	/**
 	 * Outputs an error string.
 	 * 
 	 * @param string
 	 *            the string to output
 	 * @param arg1
 	 */
 	public static synchronized void error(String string, Object arg1) {
 
 		Class<?> c = getCallerClass();
 		if (!excludedClasses.contains(c)) {
 			LoggerFactory.getLogger(c).error(string, arg1);
 		}
 	}
 
 	/**
 	 * Outputs an error string.
 	 * 
 	 * @param string
 	 *            the string to output
 	 * @param arg1
 	 * @param arg2
 	 */
 	public static synchronized void error(String string, Object arg1, Object arg2) {
 
 		Class<?> c = getCallerClass();
 		if (!excludedClasses.contains(c)) {
 			LoggerFactory.getLogger(c).error(string, arg1, arg2);
 		}
 	}
 
 	/**
 	 * Outputs an info string
 	 * 
 	 * @param string
 	 *            the string to output
 	 */
 	public static synchronized void info(String string) {
 
 		Class<?> c = getCallerClass();
 		if (!excludedClasses.contains(c)) {
 			LoggerFactory.getLogger(c).info(string);
 		}
 	}
 
 	/**
 	 * Outputs an info string
 	 * 
 	 * @param string
 	 *            the string to output
 	 * @param arg1
 	 */
 	public static synchronized void info(String string, Object arg1) {
 
 		Class<?> c = getCallerClass();
 		if (!excludedClasses.contains(c)) {
 			LoggerFactory.getLogger(c).info(string, arg1);
 		}
 	}
 
 	/**
 	 * Outputs an info string
 	 * 
 	 * @param string
 	 *            the string to output
 	 * @param arg1
 	 * @param arg2
 	 */
 	public static synchronized void info(String string, Object arg1, Object arg2) {
 
 		Class<?> c = getCallerClass();
 		if (!excludedClasses.contains(c)) {
 			LoggerFactory.getLogger(c).info(string, arg1, arg2);
 		}
 	}
 
 	/**
 	 * @param e
 	 * @return message
 	 */
 	public static String getMessage(Throwable e) {
 
 		if (e instanceof CorbaDeviceException) {
 			return ((CorbaDeviceException) e).message + "." + ((CorbaDeviceException) e).getLocalizedMessage();
 		}
 		return e.toString();
 	}
 
 	/**
 	 * @param e
 	 * @return full stack message
 	 */
 	public static String getFullStackMsg(Throwable e) {
 
 		String msg = getMessage(e);
 		Throwable cause = e.getCause();
 		while (cause != null && cause != cause.getCause()) {
 			msg += "\n" + getMessage(cause);
 			cause = cause.getCause();
 		}
 		return msg;
 	}
 
 	/**
 	 * @param e
 	 */
 	public static void logException(Throwable e) {
 
 		error(getFullStackMsg(e));
 	}
 
 	/**
 	 * @param msg
 	 * @param e
 	 */
 	public static void logException(String msg, Throwable e) {
 
 		error(msg + " " + getFullStackMsg(e));
 	}
 
 	/**
 	 * Adds a class to the list of excluded classes, messages from classes on the excluded list will not appear.
 	 * 
 	 * @param c
 	 *            the class
 	 */
 	public static synchronized void excludeClass(Class<?> c) {
 
 		if (!excludedClasses.contains(c)) {
 			excludedClasses.add(c);
 		}
 	}
 
 	/**
 	 * Removes a class from the list of excluded classes, messages from classes on the excluded list will not appear.
 	 * 
 	 * @param c
 	 *            the class
 	 */
 	public static synchronized void includeClass(Class<?> c) {
 
 		excludedClasses.remove(c);
 	}
 
 	/**
 	 * Removes all classes from the excluded list.
 	 */
 	public static synchronized void includeAll() {
 
 		excludedClasses.clear();
 	}
 
	static {
		// getCallerClass broke in 7u25, but was fixed in 7u40:
		//   http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=8016814
		final String javaVersion = System.getProperty("java.version");
		CALLER_CLASS_FRAMES_TO_SKIP = javaVersion.equals("1.7.0_25") ? 4 : 3;
	}

	private static final int CALLER_CLASS_FRAMES_TO_SKIP;

 	private static Class<?> getCallerClass() {
		return Reflection.getCallerClass(CALLER_CLASS_FRAMES_TO_SKIP);
 	}
 }
