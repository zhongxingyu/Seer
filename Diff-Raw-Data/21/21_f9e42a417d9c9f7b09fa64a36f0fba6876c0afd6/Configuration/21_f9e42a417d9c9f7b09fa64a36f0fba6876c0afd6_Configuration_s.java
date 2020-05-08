 package de.skuzzle.polly.sdk;
 
 import java.io.IOException;
 
 /**
  * <p>Provides access to the polly configuration file.</p>
  * 
  * <p>You can access all polly configuration properties, but there may come up 
  * restrictions in further polly versions for security reasons. So far, you have
  * read-only access to all properties and can retrieve them by passing any of the
  * static constants to the read methods.</p>
  * 
  * <p>Note that the {@link #setProperty(String, Object)} and {@link #store()} methods
  * will always throw an {@link UnsupportedOperationException} as they are no longer
  * supported.</p>
  * 
  * @author Simon
  * @since zero day
  * @version RC 1.0
  */
 public interface Configuration {
     
     /**
      * Config file property name which points the path to the log4j configuration file
      * @since Beta 0.5
      */
     public final static String LOG_CONFIG_FILE = "loggerSettings";
     
     /**
      * Config file property name which refers to a comma seperated list of plugin names
      * that are to be excluded upon loading.
      * @since Beta 0.5
      */
     public final static String PLUGIN_EXCLUDES = "pluginExcludes";
     
     /**
      * Config file property name which refers to a comma seperated list of channel names
      * that are to be joined upon starting.
      * @since Beta 0.5
      */
     public final static String CHANNELS = "channels";
     
     /**
      * Config file property name which refers to the irc-nickname of polly.
      * @since Beta 0.5
      */
     public final static String NICKNAME = "nickName";
     
     /**
      * Config file property name which refers to the nickserv password.
      * @since Beta 0.5
      */
     public final static String IDENT = "ident";
     
     /**
      * Config file property name which refers to the irc server address to connect to
      * on startup.
      * @since Beta 0.5
      */
     public final static String SERVER = "server";
     
     /**
      * Config file property name which refers to the {@link #SERVER}s port.
      * @since Beta 0.5
      */
     public final static String PORT = "port";
     
     /**
      * Config file property name which refers to the database url. You should not change
      * this config property!  lease Note that further polly version may restrict 
      * access to this field.
      * @since Beta 0.5
      */
     public final static String DB_URL = "dbUrl";
     
     /**
      * Config file property name which refers to the database username. You should not 
      * change this config property! Please Note that further polly version may restrict 
      * access to this field.
      * @since Beta 0.5
      */
     public final static String DB_USER = "dbUser";
     
     /**
      * Config file property name which refers to the database password. You should not
      * change this config property! Please Note that further polly version may restrict 
      * access to this field.
      * @since Beta 0.5
      */
     public final static String DB_PASSWORD = "dbPassword";
     
     /**
      * Config file property name which refers to the database driver name.
      * @since Beta 0.5
      */
     public final static String DB_DRIVER = "dbDriver";
     
     /**
      * Config file property name which enables/disables logging of irc messages.
      * @since Beta 0.5
      */
     public final static String IRC_LOGGING = "ircLogging";
     
     /**
      * Config file property name which refers to the name of the admin user.  You should 
      * not change this config property! Please Note that further polly version may 
      * restrict access to this field.
      * @since Beta 0.5
      */
     public final static String ADMIN_NAME = "adminUserName";
     
     /**
      * Config file property name which refers to the admin password which is not stored
      * as plaintext but as MD5 hash. You should not change this config property! 
      * Please Note that further polly version may restrict access to this field.
      * @since Beta 0.5
      */
     public final static String ADMIN_PASSWORD_HASH = "adminPasswordHash";
     
     /**
      * Config file property name which sets the user level for the admin user. You should
      * not change this property. If you do so, ensure to net set it lower than 
      * {@link UserManager#ADMIN}. Please note that further polly version may restrict 
      * access to this field.
      * @since Beta 0.5
      */
     public final static String ADMIN_USER_LEVEL = "adminUserLevel";
     
     /**
      * Config file property name which refers to the path where userdefined variable
      * declarations are stored.
      * @since Beta 0.5
      */
     public final static String DECLARATION_CACHE = "declarationCache";
     
     /**
      * Config file property name which refers to the global date format pattern.
      * @since Beta 0.5
      */
     public final static String DATE_FORMAT = "dateFormat";
     
     /**
      * Config file property name which refers to the global number format pattern.
      * @since Beta 0.5
      */
     public final static String NUMBER_FORMAT = "numberFormat";
     
     /**
      * Config file property name which refers to maximum count of event threads.
      * @since Beta 0.5
      */
     public final static String EVENT_THREADS = "eventThreads";
     
     /**
      * Config file property name which refers to a comma separated list of command names
      * which will be ignored when trying to register them.
      * @since Beta 0.5
      */
     public final static String IGNORED_COMMANDS ="ignoreCommands";
     
     /**
      * Config file property name which refers to the global encoding polly uses for 
      * incoming as well as outgoing messages.
      * @since Beta 0.5
      */
     public final static String ENCODING = "encoding";
     
     /**
      * Config file property name which refers to the maximum line length for outgoing
      * irc messages. Polly automatically wraps lines to this length.
      * @since Beta 0.5
      */
     public final static String LINE_LENGTH = "lineLength";
     
     /**
      * Config file property name which refers to the delay which polly waits when trying
      * to reestablish a lost connection. Given in milliseconds.
      * @since Beta 0.5
      */
     public final static String RECONNECT_DELAY = "reconnectDelay";
     
     /**
      * Config file property name which enables/disables the polly telnet server feature.
      * @since Beta 0.5
      */
     public final static String ENABLE_TELNET = "enableTelnet";
     
     /**
      * Config file property name which sets the port for the polly telnet server (if 
      * enabled).
      * @since Beta 0.5
      */
     public final static String TELNET_PORT = "telnetPort";
     
     
 
 	/**
 	 * <p>Stores a property with given name and value. If a property with the same name
 	 * existed before, it will be overridden. If it not existed, it will be created.</p>
 	 * 
 	 * <p>Note that you need to call {@link #store()} in order to persist these 
 	 * changes.</p>
 	 * 
 	 * @param name The property name.
 	 * @param value The properties value. This will be stored as a String, calling its
 	 * 		<code>toString</code> method.
 	 * @deprecated This method is no longer supported and will always throw an 
 	 *     {@link UnsupportedOperationException}.
 	 */
     @Deprecated
 	public abstract <T> void setProperty(String name, T value);
 	
 	
 	
 	/**
 	 * Reads a String from the properties. If no property with the given name exists,
 	 * an empty String is returned.
 	 * 
 	 * @param name The name of the property to read.
 	 * @return The properties value as a String.
 	 */
 	public abstract String readString(String name);
 	
 	
 	/**
 	 * Reads a String from the properties. If no property with the given name exists,
 	 * the defaultValue parameter is returned.
 	 * 
 	 * @param name The name of the property to read.
 	 * @param defaultValue The default value to return if the property does not exist.
 	 * @return The properties value as a String.
 	 */
 	public abstract String readString(String name, String defaultValue);
 	
 	
 	
 	/**
 	 * Reads an int from the properties. If no property with the given name exists or the
 	 * value forms no valid integer, 0 is returned.
 	 * 
 	 * @param name The name of the property to read.
 	 * @return The properties value as an int.
 	 */
 	public abstract int readInt(String name);
 	
 	
 	
 	/**
 	 * Reads an int from the properties. If no property with the given name exists or the
 	 * value forms no valid integer, the defaultValue parameter is returned.
 	 * 
 	 * @param name The name of the property to read.
 	 * @param defaultValue The default value to return if the property does not exist.
 	 * @return The properties value as an int.
 	 */
 	public abstract int readInt(String name, int defaultValue);
 	
 	
 	
 	/**
 	 * Writes the current properties back to the configuration file.
 	 * @throws IOException If writing the file fails.
      * @deprecated This method is no longer supported and will always throw an 
      *     {@link UnsupportedOperationException}.
      */
     @Deprecated
 	public abstract void store() throws IOException;
 }
