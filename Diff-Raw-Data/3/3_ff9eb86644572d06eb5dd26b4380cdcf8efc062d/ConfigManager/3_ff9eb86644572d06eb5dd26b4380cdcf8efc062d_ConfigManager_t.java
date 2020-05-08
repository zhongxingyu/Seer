 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Properties;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.PropertyConfigurator;
 import org.apache.log4j.helpers.NullEnumeration;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.collections.InvalidPropertyValueException;
 import org.xins.common.collections.PropertiesPropertyReader;
 import org.xins.common.collections.PropertyReaderConverter;
 import org.xins.common.collections.PropertyReaderUtils;
 import org.xins.common.io.FileWatcher;
 import org.xins.common.text.TextUtils;
 import org.xins.logdoc.LogCentral;
 import org.xins.logdoc.UnsupportedLocaleException;
 
 /**
  * XINS configuration file manager. Contains both the config file watcher and
  * the listener. Has the ability to check the ServletConfig object and can
  * configure the default Log settings.
  *
  * @version $Revision$
  * @author Mees Witteman (<a href="mailto:mees.witteman@nl.wanadoo.com">mees.witteman@nl.wanadoo.com</a>)
  *
  */
 final class ConfigManager {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The object to synchronize on when reading and initializing from the
     * runtime configuration file.
     */
    static final Object RUNTIME_PROPERTIES_LOCK = new Object();
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Initializes the logging subsystem with fallback default settings.
     */
    static void configureLoggerFallback() {
 
       Properties settings = new Properties();
 
       // Send all log messages to the logger named 'console'
       settings.setProperty("log4j.rootLogger",
       "ALL, console");
 
       // Define the type of the logger named 'console'
       settings.setProperty("log4j.appender.console",
       "org.apache.log4j.ConsoleAppender");
 
       // Use a pattern-layout for the logger
       settings.setProperty("log4j.appender.console.layout",
       "org.apache.log4j.PatternLayout");
 
       // Define the pattern for the logger
       settings.setProperty("log4j.appender.console.layout.ConversionPattern",
       "%16x %6c{1} %-6p %m%n");
 
       // Perform Log4J configuration
       PropertyConfigurator.configure(settings);
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>ConfigManager</code> object.
     *
     * @param engine The servlet Engine.
     * @param config The servlet configuration object
     */
    ConfigManager(Engine engine, ServletConfig config) {
       _engine = engine;
       _config = config;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The name of the runtime configuration file.
     */
    private String _configFile;
 
    /**
     * The engine.
     */
    private Engine _engine;
 
    /**
     * Servlet confuration object.
     */
    private ServletConfig _config;
 
    /**
     * The listener that is notified when the configuration file changes. Only
     * one instance is created ever.
     */
    private final ConfigurationFileListener _configFileListener = new ConfigurationFileListener();
 
    /**
     * Runtime configuration file watcher.
     */
    private FileWatcher _configFileWatcher;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Determines the reload interval for the config file, initializes the API
     * if the interval has changed and starts the config file watcher.
     */
    void init() {
       int interval = determineReloadIntervalAndInitAPI();
       startConfigFileWatcher(interval);
    }
 
    /**
     * Checks the servlet config object.
     *
     * @throws ServletException
     *    if the config is <code>null</code> or if the context in the
     *    config is null
     *
     */
    void checkServletConfig()
    throws ServletException {
 
       // TODO: Logdoc entry 3201 is never logged anymore. Remote it.
 
       if (_config == null) {
          Log.log_3202("config == null");
          throw new ServletException();
       }
 
       // Get the ServletContext
       ServletContext context = _config.getServletContext();
       if (context == null) {
          Log.log_3202("config.getServletContext() == null");
          throw new ServletException();
       }
 
       // Check the expected vs implemented Java Servlet API version
       // 2.2, 2.3 and 2.4 are supported
       int major = context.getMajorVersion();
       int minor = context.getMinorVersion();
       if (major != 2 || (minor != 2 && minor != 3 && minor != 4)) {
          String expected = "2.2/2.3/2.4";
          String actual   = "" + major + '.' + minor;
          Log.log_3203(actual, expected);
       }
    }
 
    /**
     * Determines the config file name by getting the name from the System
     * properties. If this doesn't succeed then the value will be retrieved
     * from the servlet init parameters (config). Will be stored in the configFile field.
     *
     */
    void determineConfigFile() {
       String configFile = null;
       try {
          configFile = System.getProperty(APIServlet.CONFIG_FILE_SYSTEM_PROPERTY);
       } catch (SecurityException exception) {
          Log.log_3230(exception, APIServlet.CONFIG_FILE_SYSTEM_PROPERTY);
       }
 
       // If the config file is not set at start-up try to get it from the
       // web.xml file
       if (configFile == null) {
          Log.log_3231(APIServlet.CONFIG_FILE_SYSTEM_PROPERTY);
          configFile = _config.getInitParameter(APIServlet.CONFIG_FILE_SYSTEM_PROPERTY);
       }
       _configFile = configFile;
    }
 
    /**
     * Starts the config file watcher watch thread.
     *
     * @param interval
     *    the interval in seconds, must be greater than or equal to 1.
     */
    void startConfigFileWatcher(int interval) {
 
       // Create and start a file watch thread
       if (_configFile != null && _configFile.length() > 0 && interval > 0) {
          _configFileWatcher = new FileWatcher(_configFile,
                                               interval,
                                               _configFileListener);
          _configFileWatcher.start();
       }
    }
 
    /**
     * If the config file watcher == <code>null</code>, then the config file
     * listener will be re-initialized. If not the file watcher will be
     * interrupted.
     */
    void reloadPropertiesIfChanged() {
       if (_configFileWatcher == null) {
          _configFileListener.reinit();
       } else {
          _configFileWatcher.interrupt();
       }
    }
 
    /**
     * Unifies the file separator character on the _configFile property and then
     * reads the runtime properties file, initializes the logging subsystem
     * with the read properties and then stores those properties on the engine.
     * If the _configFile is empty, then an empty set of properties is set on the
     * engine.
     *
     */
    void readRuntimeProperties() {
       // If the value is not set only localhost can access the API.
       // NOTE: Don't trim the configuration file name, since it may start
       //       with a space or other whitespace character.
       if (_configFile == null || _configFile.length() < 1) {
          Log.log_3205(APIServlet.CONFIG_FILE_SYSTEM_PROPERTY);
          _engine.setRuntimeProperties(PropertyReaderUtils.EMPTY_PROPERTY_READER);
       } else {
 
          // Unify the file separator character
          _configFile = _configFile.replace('/',  File.separatorChar);
          _configFile = _configFile.replace('\\', File.separatorChar);
 
          // Initialize the logging subsystem
          Log.log_3300(_configFile);
 
          synchronized (ConfigManager.RUNTIME_PROPERTIES_LOCK) {
 
             Properties properties = new Properties();
             try {
 
                // Open the file
                FileInputStream in = new FileInputStream(_configFile);
 
                // Load the properties
                properties.load(in);
 
                // Close the file
                in.close();
             } catch (FileNotFoundException exception) {
                Log.log_3301(exception, _configFile);
             } catch (SecurityException exception) {
                Log.log_3302(exception, _configFile);
             } catch (IOException exception) {
                Log.log_3303(exception, _configFile);
             }
 
             // Attempt to configure Log4J
             configureLogger(properties);
 
             ContextIDGenerator.changeHostNameIfNeeded(properties);
 
             // Store the runtime properties on the engine
             _engine.setRuntimeProperties(new PropertiesPropertyReader(properties));
          }
       }
    }
 
    /**
     * Configure the Log4J system.
     *
     * @param properties
     *    the runtime properties containing the Log4J configuration.
     *
     * @throws IllegalArgumentException
     *    if <code>properties == null</code>.
     */
    void configureLogger(Properties properties)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("properties", properties);
 
       // Reset Log4J configuration
       LogManager.getLoggerRepository().resetConfiguration();
 
       // Reconfigure Log4J
       PropertyConfigurator.configure(properties);
 
       // Determine if Log4J is properly initialized
       Enumeration appenders =
          LogManager.getLoggerRepository().getRootLogger().getAllAppenders();
 
       if (appenders instanceof NullEnumeration) {
          Log.log_3304(_configFile);
          configureLoggerFallback();
       } else {
          Log.log_3305();
       }
    }
 
    /**
     * Determines the interval for checking the runtime properties file for
     * modifications.
     *
     * @return
     *    the interval to use, always &gt;= 1.
     *
     * @throws InvalidPropertyValueException
     *    if the interval cannot be determined because it does not qualify as a
     *    positive 32-bit unsigned integer number.
     */
    int determineConfigReloadInterval()
    throws InvalidPropertyValueException {
 
       _engine.getState().setState(EngineState.DETERMINE_INTERVAL);
 
       // Get the runtime property
       String s = _engine.getRunTimeProperties().get(APIServlet.CONFIG_RELOAD_INTERVAL_PROPERTY);
       int interval = -1;
 
       // If the property is set, parse it
       if (s != null && s.length() >= 1) {
          try {
             interval = Integer.parseInt(s);
             if (interval < 0) {
                Log.log_3409(_configFile,
                             APIServlet.CONFIG_RELOAD_INTERVAL_PROPERTY, s);
                _engine.getState().setState(EngineState.DETERMINE_INTERVAL_FAILED);
                throw new InvalidPropertyValueException(APIServlet.CONFIG_RELOAD_INTERVAL_PROPERTY,
                                                        s,
                                                        "Negative value.");
             } else {
                Log.log_3410(_configFile, APIServlet.CONFIG_RELOAD_INTERVAL_PROPERTY, s);
             }
          } catch (NumberFormatException nfe) {
             Log.log_3409(_configFile, APIServlet.CONFIG_RELOAD_INTERVAL_PROPERTY, s);
             _engine.getState().setState(EngineState.DETERMINE_INTERVAL_FAILED);
             throw new InvalidPropertyValueException(APIServlet.CONFIG_RELOAD_INTERVAL_PROPERTY,
                                                     s,
                                                     "Not a 32-bit integer number.");
          }
 
          // Otherwise, if the property is not set, use the default
       } else {
          Log.log_3408(_configFile, APIServlet.CONFIG_RELOAD_INTERVAL_PROPERTY);
          interval = APIServlet.DEFAULT_CONFIG_RELOAD_INTERVAL;
       }
 
       return interval;
    }
 
    /**
     * Determines the reload interval and initialises the API if no exception is
     * thrown from the determination.
     *
     * @return
     *    the reload interval
     */
    int determineReloadIntervalAndInitAPI() {
       int interval;
       boolean intervalParsed;
       try {
          interval = determineConfigReloadInterval();
          intervalParsed = true;
       } catch (InvalidPropertyValueException exception) {
          intervalParsed = false;
          interval = APIServlet.DEFAULT_CONFIG_RELOAD_INTERVAL;
       }
 
       if (intervalParsed) {
          _engine.initAPI();
       }
       return interval;
    }
 
    /**
     * Determines the log locale.
     */
    void determineLogLocale() {
 
       String newLocale = _engine.getRunTimeProperties().get(
       LogCentral.LOG_LOCALE_PROPERTY);
 
       if (TextUtils.isEmpty(newLocale)) {
          newLocale = _engine.getRunTimeProperties().get(APIServlet.LOG_LOCALE_PROPERTY);
       }
 
       // If the log locale is set, apply it
       if (newLocale != null) {
          String currentLocale = LogCentral.getLocale();
          if (!currentLocale.equals(newLocale)) {
             Log.log_3306(currentLocale, newLocale);
             try {
                LogCentral.setLocale(newLocale);
                Log.log_3307(currentLocale, newLocale);
             } catch (UnsupportedLocaleException exception) {
                Log.log_3308(currentLocale, newLocale);
                _engine.getState().setState(EngineState.API_INITIALIZATION_FAILED);
                return;
             }
          }
       }
 
    }
 
    /**
     * Stops the config file watcher thread.
     */
    void destroy() {
       // Stop the FileWatcher
       if (_configFileWatcher != null) {
          _configFileWatcher.end();
       }
 
    }
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * Listener that reloads the configuration file if it changes.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
     *
     * @since XINS 1.0.0
     */
    private final class ConfigurationFileListener
    extends Object
    implements FileWatcher.Listener {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>ConfigurationFileListener</code> object.
        */
       private ConfigurationFileListener() {
          // empty
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Re-initializes the framework. The run-time properties are re-read,
        * the configuration file reload interval is determined, the API is
        * re-initialized and then the new interval is applied to the watch
        * thread for the configuration file.
        */
       private void reinit() {
 
          Log.log_3407(_configFile);
 
          synchronized (RUNTIME_PROPERTIES_LOCK) {
 
             // Apply the new runtime settings to the logging subsystem
             readRuntimeProperties();
 
             // Determine the interval
             int newInterval;
             try {
                newInterval = determineConfigReloadInterval();
             } catch (InvalidPropertyValueException exception) {
                // Logging is already done in determineConfigReloadInterval()
                return;
             }
 
             // Re-initialize the API
             _engine.initAPI();
 
             updateFileWatcher(newInterval);
          }
       }
 
       /**
        * Updates the file watch interval and initializes the file watcher if
        * needed.
        *
        * @param newInterval The new interval to watch the config file
        */
       private void updateFileWatcher(int newInterval) {
          // Update the file watch interval
          int oldInterval = 0;
          if (_configFileWatcher != null) {
             oldInterval = _configFileWatcher.getInterval();
          }
 
          if (oldInterval != newInterval) {
             if (newInterval == 0 && _configFileWatcher != null) {
                _configFileWatcher.end();
                _configFileWatcher = null;
             } else if (newInterval > 0 && _configFileWatcher == null) {
                _configFileWatcher = new FileWatcher(_configFile,
                                                     newInterval,
                                                     _configFileListener);
                _configFileWatcher.start();
             } else {
                _configFileWatcher.setInterval(newInterval);
                Log.log_3403(_configFile, oldInterval, newInterval);
             }
          }
       }
 
       /**
        * Callback method called when the configuration file is found while it
        * was previously not found.
        *
        * <p>This will trigger re-initialization.
        */
       public void fileFound() {
          reinit();
       }
 
       /**
        * Callback method called when the configuration file is (still) not
        * found.
        *
        * <p>The implementation of this method does not perform any actions.
        */
       public void fileNotFound() {
          Log.log_3400(_configFile);
       }
 
       /**
        * Callback method called when the configuration file is (still) not
        * modified.
        *
        * <p>The implementation of this method does not perform any actions.
        */
       public void fileNotModified() {
          Log.log_3402(_configFile);
       }
 
       /**
        * Callback method called when the configuration file could not be
        * examined due to a <code>SecurityException</code>.
        *
        * <p>The implementation of this method does not perform any actions.
        *
        * @param exception
        *    the caught security exception, should not be <code>null</code>
        *    (although this is not checked).
        */
       public void securityException(SecurityException exception) {
          Log.log_3401(exception, _configFile);
       }
 
       /**
        * Callback method called when the configuration file is modified since
        * the last time it was checked.
        *
        * <p>This will trigger re-initialization.
        */
       public void fileModified() {
          reinit();
       }
    }
 }
