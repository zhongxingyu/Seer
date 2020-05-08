 package org.slf4j.impl;
 
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Properties;
 
import java.io.InputStream;
import java.io.IOException;
 
 import android.util.Log;
 
 /**
  * <p>This class adapts the SLF4J interface to the Android Log system.
  * The intention is to make it easy to control logging from legacy
  * code systems properly. As such each log method checks to see if
  * that log level is enabled since the legacy code this adapts for
  * is often written with out such checks counting on the logging
  * system to handle filtering out these messages. This is not the
  * best thing for performance but we view it as the lesser of
  * two evils.
  * </p>
  * <p>Note that most legacy logging system use a class name as the
  * tag. Android however requires tags be of a specific (and short)
  * length. As such all class names are automatically shortened in
  * some way. To see how the class name you are interested in gets
  * shortened monitor the 'slf4j' tag. Generally we try
  * to use the whole class name if it will fit but more often
  * this will get shortened to the first letters of each portion
  * of the path followed by the class name or simply the class
  * name if that will not fit.
  * </p>
  * <p>Note also that we check isDebugEnabled with every call
  * despite this adding possible overhead since a great deal of
  * legacy code does not check the logging level first.
  * </p>
  * <p>To configure this logger you can include an SLF4J.properties
  * file in your apk with lines of the form:<pre>
  * &lt;package&gt;.&lt;class&gt;=&lt;level&gt;
  * </pre>
  * where level is one of:
  * 'disabled', 'trace', 'debug', 'info', 'warn' or 'error'<br/>
  * You may also include a 'default.log.level=<level>' line
  * to set the default level for all classes. Note that
  * it is allowed to specify a class twice in which case the
  * lowest level specified in the file will be used except
  * for the default where the last instance in the file will be used.
  * </p>
  * <p>We search for this properties file in the root of your JAR,
  * in the META-INF directory then the org/slf4j directory then
  * org/slf4j/impl directory and stop searching as soon as we
  * find one.
  * </p>
  **/
 public class AndroidLogger extends MarkerIgnoringBase {
 
     /**
      * Serial Version ID
      */
     private static final long serialVersionUID = 1L;
 
     /**
      * Log tag for SLF4J itself
      **/
     private static final String SLF4J_TAG = "slf4j";
 
     /**
      * The tag this logger will log with
      */
     private final String tag;
 
 	private final int level;
 
 	private static final int DISABLED = 0;
 	private static final int TRACE = 1;
 	private static final int DEBUG = 2;
 	private static final int INFO = 3;
 	private static final int WARN = 4;
 	private static final int ERROR = 5;
 	private static final int DEFAULT_LOG_LEVEL = INFO;
 
 	private static int sDefaultLevel = DEFAULT_LOG_LEVEL;
 
 	private static final String[] levels = {
 		"disabled", "trace", "debug", "info", "warn", "error"
 	};
 	private static final String sConfigFile = "SLF4J.properties";
 
 	// This is dumb. You can't have an array of generics?
 	@SuppressWarnings("unchecked")
 	private static final ArrayList<String> levelLists[] = new ArrayList[5];
 
 	private static final String DEFAULT_LEVEL_NAME = "default.log.level";
 
 	private static int parseLevel(String levelName) {
 		for (int i = 0; i < levels.length; i++) {
 			if (levels[i].equals(levelName)) {
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	/**
 	   Android offers no way to turn off info and higher level messages
 	   This is not so great for integrating legacy services that
 	   expect a more advanced logger that can do some filtering.
 	   We thus use a property file in the form class.path.Class=level
 	   where level is one of "disabled", "trace", "debug", "info", "warn" or
 	   "error";
 	**/
 	static {
 		// Load and parse the properties.
 		Log.d(SLF4J_TAG, "Trying to load properties from: " + sConfigFile);
 		InputStream in = AndroidLogger.class.getClassLoader().getResourceAsStream(sConfigFile);
 		if (in == null) {
 			Log.d(SLF4J_TAG, "Trying to load from: " + "META-INF/" + sConfigFile);
 			in = AndroidLogger.class.getClassLoader().getResourceAsStream("META-INF/" + sConfigFile);
 		}
 		if (in == null) {
 			Log.d(SLF4J_TAG, "Trying to load from: " + "org/slf4j/" + sConfigFile);
 			in = AndroidLogger.class.getClassLoader().getResourceAsStream("org/slf4j/" + sConfigFile);
 		}
 		if (in == null) {
 			Log.d(SLF4J_TAG, "Trying to load from: " + "org/slf4j/impl/" + sConfigFile);
 			in = AndroidLogger.class.getClassLoader().getResourceAsStream("org/slf4j/impl/" + sConfigFile);
 		}
 		if (null != in) {
 			Log.d(SLF4J_TAG, "Loading properties...");
 			Properties props = new java.util.Properties();
 			try {
 				props.load(in);
 				for (Enumeration names = props.propertyNames(); names.hasMoreElements();) {
 					String name = (String)names.nextElement();
 					// What level is this?
 					int value = parseLevel(props.getProperty(name));
 					if (value >= 0) {
 						if (DEFAULT_LEVEL_NAME.equals(name)) {
 							Log.d(SLF4J_TAG, "Setting default level to: " + levels[value]);
 							sDefaultLevel = value;
 						} else {
 							// Add it to the list for that level.
 							Log.d(SLF4J_TAG, "Setting level for: "
 									+ name + " to: " + levels[value]);
 							if (levelLists[value] == null) {
 								levelLists[value] = new ArrayList<String>();
 							}
 							levelLists[value].add(name);
 						}
 					}
 					else {
 						Log.w(SLF4J_TAG, "Unknown level for: " + name + ": '" +
 							  props.getProperty(name) +
 							  "'. Using default.");
 					}
 				}
 			} catch (IOException e) {
 				Log.e(SLF4J_TAG, "Error while loading properties: " + e.getMessage());
 			}
 		}
 		else {
 			Log.w(SLF4J_TAG, "No configuration file found: " + in);
 		}
 	}
 
     /**
      * Package access allows only {@link AndroidLoggerFactory} to instantiate
      * AndroidLogger instances.
 	 *
      * @return
      */
     AndroidLogger(String tag) {
 		// Android only supports tags of length <= 23
 		if (tag.length() > 23) {
 			// We try to do something smart here to shorten
 			StringBuffer shortTag = new StringBuffer();
 			String[] parts = tag.split("\\.");
 			String lastPart = parts[parts.length - 1];
 			// Can we use the whole last part?
 			if (lastPart.length() < 23) {
 				if ( ( (parts.length-1) * 2 ) + lastPart.length() <= 23 ) {
 					for (int x=0; x<parts.length - 1; x++) {
 						shortTag.append(parts[x].charAt(0));
 						shortTag.append('.');
 					}
 				}
 				shortTag.append(lastPart);
 			} else {
 				shortTag.append(lastPart.substring(0,10));
 				shortTag.append("...");
 				shortTag.append(lastPart.substring(lastPart.length() - 10));
 			}
 			this.tag = shortTag.toString();
 			Log.d(SLF4J_TAG, "Tag: " + tag + " shortened to: " + this.tag);
 		} else {
 			this.tag = tag;
 		}
 
 		// Now to figure out what level this should be at
 		// We take the lowest level we can find and run with it
 		int foundLevel = -1;
 		for (int i = 0; i < levelLists.length; i++) {
 			if (levelLists[i] != null) {
 				if (levelLists[i].contains(this.tag) ||
 					levelLists[i].contains(tag)) {
 					foundLevel = i;
 					break;
 				}
 			}
 		}
 		if (foundLevel < 0) {
 			this.level = sDefaultLevel;
 		} else {
 			this.level = foundLevel;
 		}
 		Log.d(SLF4J_TAG, "Level for: " + this.tag + " set to: " + levels[this.level]);
     }
 
     public void debug(String arg0) {
 		if (isDebugEnabled())
 			Log.d(tag, arg0);
     }
 
     public void debug(String arg0, Object arg1) {
 		if (isDebugEnabled())
 			Log.d(tag, MessageFormatter.format(arg0, arg1));
     }
 
     public void debug(String arg0, Object[] arg1) {
 		if (isDebugEnabled())
 			Log.d(tag, MessageFormatter.arrayFormat(arg0, arg1));
     }
 
     public void debug(String arg0, Throwable arg1) {
 		if (isDebugEnabled())
 			Log.d(tag, arg0, arg1);
     }
 
     public void debug(String arg0, Object arg1, Object arg2) {
 		if (isDebugEnabled())
 			Log.d(tag, MessageFormatter.format(arg0, arg1, arg2));
     }
 
     public void error(String arg0) {
 		if (isErrorEnabled())
 			Log.e(tag, arg0);
     }
 
     public void error(String arg0, Object arg1) {
 		if (isErrorEnabled())
 			Log.e(tag, MessageFormatter.format(arg0, arg1));
     }
 
     public void error(String arg0, Object[] arg1) {
 		if (isErrorEnabled())
 			Log.e(tag, MessageFormatter.arrayFormat(arg0, arg1));
     }
 
     public void error(String arg0, Throwable arg1) {
 		if (isErrorEnabled())
 			Log.e(tag, arg0, arg1);
     }
 
     public void error(String arg0, Object arg1, Object arg2) {
 		if (isErrorEnabled())
 			Log.e(tag, MessageFormatter.format(arg0, arg1, arg1));
     }
 
     public void info(String arg0) {
 		if (isInfoEnabled())
 			Log.i(tag, arg0);
     }
 
     public void info(String arg0, Object arg1) {
 		if (isInfoEnabled())
 			Log.i(tag, MessageFormatter.format(arg0, arg1));
     }
 
     public void info(String arg0, Object[] arg1) {
 		if (isInfoEnabled())
 			Log.i(tag, MessageFormatter.format(arg0, arg1));
     }
 
     public void info(String arg0, Throwable arg1) {
 		if (isInfoEnabled())
			Log.i(tag, MessageFormatter.format(arg0, arg1));
     }
 
     public void info(String arg0, Object arg1, Object arg2) {
 		if (isInfoEnabled())
 			Log.i(tag, MessageFormatter.format(arg0, arg1, arg2));
     }
 
     public boolean isDebugEnabled() {
 		return this.level <= DEBUG && Log.isLoggable(tag, Log.DEBUG);
     }
 
     public boolean isErrorEnabled() {
 		return this.level <= ERROR && Log.isLoggable(tag, Log.ERROR);
     }
 
     public boolean isInfoEnabled() {
         return this.level <= INFO && Log.isLoggable(tag, Log.INFO);
     }
 
     public boolean isTraceEnabled() {
         return this.level <= TRACE && Log.isLoggable(tag, Log.VERBOSE);
     }
 
     public boolean isWarnEnabled() {
         return this.level <= WARN && Log.isLoggable(tag, Log.WARN);
     }
 
     public void trace(String arg0) {
 		if (isTraceEnabled())
 			Log.v(tag, arg0);
     }
 
     public void trace(String arg0, Object arg1) {
 		if (isTraceEnabled())
 			Log.v(tag, MessageFormatter.format(arg0, arg1));
     }
 
     public void trace(String arg0, Object[] arg1) {
 		if (isTraceEnabled())
 			Log.v(tag, MessageFormatter.arrayFormat(arg0, arg1));
     }
 
     public void trace(String arg0, Throwable arg1) {
 		if (isTraceEnabled())
			Log.v(tag, MessageFormatter.format(arg0, arg1));
     }
 
     public void trace(String arg0, Object arg1, Object arg2) {
 		if (isTraceEnabled())
 			Log.v(tag, MessageFormatter.format(arg0, arg1, arg2));
     }
 
     public void warn(String arg0) {
 		if (isWarnEnabled())
 			Log.w(tag, arg0);
     }
 
     public void warn(String arg0, Object arg1) {
 		if (isWarnEnabled())
 			Log.w(tag, MessageFormatter.format(arg0, arg1));
     }
 
     public void warn(String arg0, Object[] arg1) {
 		if (isWarnEnabled())
			Log.w(tag, MessageFormatter.format(arg0, arg1));
     }
 
     public void warn(String arg0, Throwable arg1) {
 		if (isWarnEnabled())
			Log.w(tag, MessageFormatter.format(arg0, arg1));
     }
 
     public void warn(String arg0, Object arg1, Object arg2) {
 		if (isWarnEnabled())
 			Log.w(tag, MessageFormatter.format(arg0, arg1, arg2));
     }
 
 }
