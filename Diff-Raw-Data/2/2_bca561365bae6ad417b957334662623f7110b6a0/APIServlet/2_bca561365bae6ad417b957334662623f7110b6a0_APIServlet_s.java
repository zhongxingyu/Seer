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
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Properties;
 import java.util.Random;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.NDC;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.PropertyConfigurator;
 import org.apache.log4j.helpers.NullEnumeration;
 import org.xins.common.manageable.BootstrapException;
 
 import org.xins.logdoc.LogCentral;
 import org.xins.logdoc.UnsupportedLocaleException;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.Utils;
 
 import org.xins.common.collections.InvalidPropertyValueException;
 import org.xins.common.collections.MissingRequiredPropertyException;
 import org.xins.common.collections.PropertiesPropertyReader;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.collections.PropertyReaderConverter;
 
 import org.xins.common.io.FileWatcher;
 
 import org.xins.common.manageable.InitializationException;
 
 import org.xins.common.net.IPAddressUtils;
 
 import org.xins.common.servlet.ServletConfigPropertyReader;
 import org.xins.common.servlet.ServletRequestPropertyReader;
 
 import org.xins.common.text.FastStringBuffer;
 import org.xins.common.text.HexConverter;
 import org.xins.common.text.TextUtils;
 
 import org.xins.logdoc.ExceptionUtils;
 
 /**
  * HTTP servlet that forwards requests to an <code>API</code>.
  *
  * <p>This servlet supports the following HTTP request methods:
  *
  * <ul>
  *   <li>GET
  *   <li>POST
  *   <li>HEAD
  *   <li>OPTIONS
  * </ul>
  *
  * <p>A method with any other request method will make this servlet return:
  * <blockquote><code>405 Method Not Allowed</code></blockquote>
  *
  * <p>If no matching function is found, then this servlet will return:
  * <blockquote><code>404 Not Found</code></blockquote>
  *
  * <p>If the state is not <em>ready</em>, then depending on the state, an HTTP
  * response code will be returned:
  *
  * <table class="APIServlet_HTTP_response_codes">
  *    <tr><th>State</th>                     <th>HTTP response code</th>       </tr>
  *    <tr><td>Initial</td>                   <td>503 Service Unavailable</td>  </tr>
  *    <tr><td>Bootstrapping framework</td>   <td>503 Service Unavailable</td>  </tr>
  *    <tr><td>Framework bootstrap failed</td><td>500 Internal Server Error</td></tr>
  *    <tr><td>Constructing API</td>          <td>503 Service Unavailable</td>  </tr>
  *    <tr><td>API construction failed</td>   <td>500 Internal Server Error</td></tr>
  *    <tr><td>Bootstrapping API</td>         <td>503 Service Unavailable</td>  </tr>
  *    <tr><td>API bootstrap failed</td>      <td>500 Internal Server Error</td></tr>
  *    <tr><td>Initializing API</td>          <td>503 Service Unavailable</td>  </tr>
  *    <tr><td>API initialization failed</td> <td>500 Internal Server Error</td></tr>
  *    <tr><td>Disposing</td>                 <td>500 Internal Server Error</td></tr>
  *    <tr><td>Disposed</td>                  <td>500 Internal Server Error</td></tr>
  * <table>
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public final class APIServlet
 extends HttpServlet {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The fully-qualified name of this class.
     */
    private static final String CLASSNAME = APIServlet.class.getName();
 
    /**
     * The <em>INITIAL</em> state.
     */
    private static final State INITIAL = new State("INITIAL");
 
    /**
     * The <em>BOOTSTRAPPING_FRAMEWORK</em> state.
     */
    private static final State BOOTSTRAPPING_FRAMEWORK = new State("BOOTSTRAPPING_FRAMEWORK");
 
    /**
     * The <em>FRAMEWORK_BOOTSTRAP_FAILED</em> state.
     */
    private static final State FRAMEWORK_BOOTSTRAP_FAILED = new State("FRAMEWORK_BOOTSTRAP_FAILED");
 
    /**
     * The <em>CONSTRUCTING_API</em> state.
     */
    private static final State CONSTRUCTING_API = new State("CONSTRUCTING_API");
 
    /**
     * The <em>API_CONSTRUCTION_FAILED</em> state.
     */
    private static final State API_CONSTRUCTION_FAILED = new State("API_CONSTRUCTION_FAILED");
 
    /**
     * The <em>BOOTSTRAPPING_API</em> state.
     */
    private static final State BOOTSTRAPPING_API = new State("BOOTSTRAPPING_API");
 
    /**
     * The <em>API_BOOTSTRAP_FAILED</em> state.
     */
    private static final State API_BOOTSTRAP_FAILED = new State("API_BOOTSTRAP_FAILED");
 
    /**
     * The <em>DETERMINE_INTERVAL</em> state.
     */
    private static final State DETERMINE_INTERVAL = new State("DETERMINE_INTERVAL");
 
    /**
     * The <em>DETERMINE_INTERVAL_FAILED</em> state.
     */
    private static final State DETERMINE_INTERVAL_FAILED = new State("DETERMINE_INTERVAL_FAILED");
 
    /**
     * The <em>INITIALIZING_API</em> state.
     */
    private static final State INITIALIZING_API = new State("INITIALIZING_API");
 
    /**
     * The <em>API_INITIALIZATION_FAILED</em> state.
     */
    private static final State API_INITIALIZATION_FAILED = new State("API_INITIALIZATION_FAILED");
 
    /**
     * The <em>READY</em> state.
     */
    private static final State READY = new State("READY");
 
    /**
     * The <em>DISPOSING</em> state.
     */
    private static final State DISPOSING = new State("DISPOSING");
 
    /**
     * The <em>DISPOSED</em> state.
     */
    private static final State DISPOSED = new State("DISPOSED");
 
    /**
     * The date formatter used for the context identifier.
     */
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyMMdd-HHmmssSSS");
 
    /**
     * The name of the system property that specifies the location of the
     * configuration file.
     */
    public static final String CONFIG_FILE_SYSTEM_PROPERTY = "org.xins.server.config";
 
    /**
     * The name of the runtime property that specifies the interval
     * for the configuration file modification checks, in seconds.
     */
    public static final String CONFIG_RELOAD_INTERVAL_PROPERTY = "org.xins.server.config.reload";
 
    /**
     * The name of the runtime property that hostname for the server
     * running the API.
     */
    public static final String HOSTNAME_PROPERTY = "org.xins.server.hostname";
 
    /**
     * The default configuration file modification check interval, in seconds.
     */
    public static final int DEFAULT_CONFIG_RELOAD_INTERVAL = 60;
 
    /**
     * The name of the build property that specifies the name of the
     * API class to load.
     */
    public static final String API_CLASS_PROPERTY = "org.xins.api.class";
 
    /**
     * The name of the build property that specifies the name of the
     * API.
     */
    public static final String API_NAME_PROPERTY = "org.xins.api.name";
 
    /**
     * The name of the build property that specifies the version with which the
     * API was built.
     */
    public static final String API_BUILD_VERSION_PROPERTY = "org.xins.api.build.version";
 
    /**
     * The name of the build property that specifies the default calling convention.
     */
    public static final String API_CALLING_CONVENTION_PROPERTY = "org.xins.api.calling.convention";
 
    /**
     * The name of the build property that specifies the class of the default calling convention.
     */
    public static final String API_CALLING_CONVENTION_CLASS_PROPERTY = "org.xins.api.calling.convention.class";
 
    /**
     * The parameter of the query to specify the calling convention.
     */
    public static final String CALLING_CONVENTION_PARAMETER = "_convention";
 
    /**
     * The standard calling convention.
     */
    public static final String STANDARD_CALLING_CONVENTION = "_xins-std";
 
    /**
     * The old style calling convention.
     */
    public static final String OLD_STYLE_CALLING_CONVENTION = "_xins-old";
 
    /**
     * The XML calling convention.
     */
    public static final String XML_CALLING_CONVENTION = "_xins-xml";
 
    /**
     * The XSLT calling convention.
     */
    public static final String XSLT_CALLING_CONVENTION = "_xins-xslt";
 
    /**
     * The name of the runtime property that specifies the locale for the log
     * messages.
     *
     * @deprecated
     *    Use {@link LogCentral#LOG_LOCALE_PROPERTY}.
     */
    public static final String LOG_LOCALE_PROPERTY = "org.xins.server.log.locale";
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Initializes the loggers to log to the console using a simple format
     * and no threshold. This is done by calling
     * {@link #configureLoggerFallback()}.
     */
    static {
       configureLoggerFallback();
    }
 
    /**
     * Initializes the logging subsystem with fallback default settings.
     */
    private static final void configureLoggerFallback() {
       Properties settings = new Properties();
       settings.setProperty("log4j.rootLogger",                                "ALL, console");
       settings.setProperty("log4j.appender.console",                          "org.apache.log4j.ConsoleAppender");
       settings.setProperty("log4j.appender.console.layout",                   "org.apache.log4j.PatternLayout");
       settings.setProperty("log4j.appender.console.layout.ConversionPattern", "%16x %6c{1} %-6p %m%n");
       PropertyConfigurator.configure(settings);
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>APIServlet</code> object.
     */
    public APIServlet() {
       _stateLock             = new Object();
       _state                 = INITIAL;
       _configFileListener    = new ConfigurationFileListener();
       _random                = new Random();
       _runtimePropertiesLock = new Object();
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * Lock for the <code>_state</code> field. This object must be locked on
     * before _state may be read or changed.
     */
    private final Object _stateLock;
 
    /**
     * The current state.
     */
    private State _state;
 
    /**
     * The listener that is notified when the configuration file changes. Only
     * one instance is created ever.
     */
    private final ConfigurationFileListener _configFileListener;
 
    /**
     * Pseudo-random number generator. Used for the automatic generation of
     * diagnostic context identifiers.
     */
    private final Random _random;
 
    /**
     * The hostname for localhost.
     */
    private String _hostname;
 
    /**
     * The stored servlet configuration object.
     */
    private ServletConfig _servletConfig;
 
    /**
     * The default calling convention stored in the Servlet.
     */
    private String _defaultCallingConvention;
 
    /**
     * The name of the runtime configuration file.
     */
    private String _configFile;
 
    /**
     * Runtime configuration file watcher.
     */
    private FileWatcher _configFileWatcher;
 
    /**
     * The properties read from the runtime configuration file.
     */
    private Object _runtimePropertiesLock;
 
    /**
     * The properties read from the runtime configuration file.
     */
    private PropertyReader _runtimeProperties;
 
    /**
     * The name of the API.
     */
    private String _apiName;
 
    /**
     * The API that this servlet forwards requests to.
     */
    private API _api;
 
    /**
     * The default calling convention for the API. Can be overridden by the
     * input parameter {@link #CALLING_CONVENTION_PARAMETER}.
     *
     * <p>This field can never be <code>null</code> so there will always be a
     * calling convention to fall back to.
     */
    private CallingConvention _callingConvention;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Generates a diagnostic context identifier. The generated context
     * identifier will be in the format:
     *
     * <blockqoute>app@host:time:rnd</blockqoute>
     *
     * where:
     *
     * <ul>
     *    <li><em>app</em> is the name of the deployed application, e.g.
     *       <code>"sso"</code>;
     *
     *    <li><em>host</em> is the hostname the computer running this servlet,
     *       e.g. <code>"freddy.bravo.com"</code>;
     *
     *    <li><em>time</em> is the current date and time in the format
     *    <code>yyMMdd-HHmmssNNN</code>, e.g. <code>"050806-171522358"</code>;
     *
     *    <li><em>rnd</em> is a 5 hex-digits randomly generated number, e.g.
     *        <code>"2f4e6"</code>.
     * </ul>
     *
     * @return
     *    the generated diagnostic context identifier, never <code>null</code>.
     */
    private String generateContextID() {
 
       // TODO: Improve performance of this method
       // XXX: Consider moving this method to a separate utility class
 
       String currentDate = DATE_FORMATTER.format(new Date());
 
       FastStringBuffer buffer = new FastStringBuffer(16);
       HexConverter.toHexString(buffer, _random.nextLong());
       String randomFive = buffer.toString().substring(0, 5);
 
       FastStringBuffer contextID = new FastStringBuffer(_apiName.length() + _hostname.length() + 27);
       contextID.append(_apiName);
       contextID.append('@');
       contextID.append(_hostname);
       contextID.append(':');
       contextID.append(currentDate);
       contextID.append(':');
       contextID.append(randomFive);
       return contextID.toString();
    }
 
    /**
     * Gets the current state. This method first synchronizes on
     * {@link #_stateLock} and then returns the value of {@link #_state}.
     *
     * @return
     *    the current state, cannot be <code>null</code>.
     */
    private State getState() {
       synchronized (_stateLock) {
          return _state;
       }
    }
 
    /**
     * Changes the current state. This method first synchronizes on
     * {@link #_stateLock} and then sets the value of {@link #_state}.
     *
     * @param newState
     *    the new state, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>newState == null</code>.
     */
    private void setState(State newState)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("newState", newState);
 
       // Check state
       if (newState == INITIAL
        || (_state == INITIAL                         && newState != BOOTSTRAPPING_FRAMEWORK                                                  )
        || (_state == BOOTSTRAPPING_FRAMEWORK         && newState != FRAMEWORK_BOOTSTRAP_FAILED && newState != CONSTRUCTING_API               )
        || (_state == FRAMEWORK_BOOTSTRAP_FAILED      && newState != BOOTSTRAPPING_FRAMEWORK                                                  )
        || (_state == CONSTRUCTING_API                && newState != API_CONSTRUCTION_FAILED    && newState != BOOTSTRAPPING_API              )
        || (_state == API_CONSTRUCTION_FAILED         && newState != CONSTRUCTING_API                                                         )
        || (_state == BOOTSTRAPPING_API               && newState != API_BOOTSTRAP_FAILED       && newState != DETERMINE_INTERVAL             )
        || (_state == API_BOOTSTRAP_FAILED            && newState != BOOTSTRAPPING_API                                                        )
        || (_state == DETERMINE_INTERVAL              && newState != INITIALIZING_API           && newState != DETERMINE_INTERVAL_FAILED      )
        || (_state == DETERMINE_INTERVAL_FAILED       && newState != DETERMINE_INTERVAL                                                       )
        || (_state == INITIALIZING_API                && newState != API_INITIALIZATION_FAILED  && newState != READY                          )
        || (_state == READY                           && newState != DETERMINE_INTERVAL         && newState != DISPOSING                      )
        || (_state == DISPOSING                       && newState != DISPOSED))
       {
          Log.log_3101(_state == null ? null : _state.getName(), newState.getName());
          throw new IllegalArgumentException("The state " + newState + " cannot follow the state " + _state + '.');
       }
 
       State oldState;
 
       synchronized (_stateLock) {
 
          // Short-circuit if the current is the new state
          if (_state == newState) {
             return;
          }
 
          // Store the old state
          oldState = _state;
 
          // Change the current state
          _state = newState;
       }
 
       Log.log_3100(oldState.getName(), newState.getName());
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
    private int determineConfigReloadInterval()
    throws InvalidPropertyValueException {
 
       setState(DETERMINE_INTERVAL);
 
       // Get the runtime property
       String s = _runtimeProperties.get(CONFIG_RELOAD_INTERVAL_PROPERTY);
       int interval = -1;
 
       // If the property is set, parse it
       if (s != null && s.length() >= 1) {
          try {
             interval = Integer.parseInt(s);
             if (interval < 0) {
                Log.log_3409(_configFile, CONFIG_RELOAD_INTERVAL_PROPERTY, s);
                setState(DETERMINE_INTERVAL_FAILED);
                throw new InvalidPropertyValueException(CONFIG_RELOAD_INTERVAL_PROPERTY, s, "Negative value.");
             } else {
                Log.log_3410(_configFile, CONFIG_RELOAD_INTERVAL_PROPERTY, s);
             }
          } catch (NumberFormatException nfe) {
             Log.log_3409(_configFile, CONFIG_RELOAD_INTERVAL_PROPERTY, s);
             setState(DETERMINE_INTERVAL_FAILED);
             throw new InvalidPropertyValueException(CONFIG_RELOAD_INTERVAL_PROPERTY, s, "Not a 32-bit integer number.");
          }
 
       // Otherwise, if the property is not set, use the default
       } else {
          Log.log_3408(_configFile, CONFIG_RELOAD_INTERVAL_PROPERTY);
          interval = DEFAULT_CONFIG_RELOAD_INTERVAL;
       }
 
       return interval;
    }
 
    /**
     * Returns information about this servlet, as plain text.
     *
     * @return
     *    textual description of this servlet, not <code>null</code> and not an
     *    empty character string.
     */
    public String getServletInfo() {
       return "XINS/Java Server Framework " + Library.getVersion();
    }
 
    /**
     * Initializes this servlet using the specified configuration. The
     * (required) {@link ServletConfig} argument is stored internally and is
     * returned from {@link #getServletConfig()}.
     *
     * <p>The initialization procedure will take required information from 3
     * sources, initially:
     *
     * <dl>
     *    <dt><strong>1. Build-time settings</strong></dt>
     *    <dd>The application package contains a <code>web.xml</code> file with
     *        build-time settings. Some of these settings are required in order
     *        for the XINS/Java Server Framework to start up, while others are
     *        optional. These build-time settings are passed to the servlet by
     *        the application server as a {@link ServletConfig} object. See
     *        {@link #init(ServletConfig)}.
     *        <br />The servlet configuration is the responsibility of the
     *        <em>assembler</em>.</dd>
     *
     *    <dt><strong>2. System properties</strong></dt>
     *    <dd>The location of the configuration file must be passed to the
     *        Java VM at startup, as a system property.
     *        <br />System properties are the responsibility of the
     *        <em>system administrator</em>.
     *        <br />Example:
     *        <br /><code>java -Dorg.xins.server.config=`pwd`/conf/xins.properties orion.jar</code></dd>
     *
     *    <dt><strong>3. Configuration file</strong></dt>
     *    <dd>The configuration file should contain runtime configuration
     *        settings, like the settings for the logging subsystem.
     *        <br />Runtime properties are the responsibility of the
     *        <em>system administrator</em>.
     *        <br />Example contents for a configuration file:
     *        <blockquote><code>log4j.rootLogger=DEBUG, console
     *        <br />log4j.appender.console=org.apache.log4j.ConsoleAppender
     *        <br />log4j.appender.console.layout=org.apache.log4j.PatternLayout
     *        <br />log4j.appender.console.layout.ConversionPattern=%d %-5p [%c] %m%n</code></blockquote>
     * </dl>
     *
     * @param config
     *    the {@link ServletConfig} object which contains build properties for
     *    this servlet, as specified by the <em>assembler</em>, cannot be
     *    <code>null</code>.
     *
     * @throws ServletException
     *    if the servlet could not be initialized.
     */
    public void init(ServletConfig config)
    throws ServletException {
 
       // Log: Bootstrapping XINS/Java Server Framework
       String serverVersion = Library.getVersion();
       Log.log_3200(serverVersion);
 
       // Warn if Server version differs from Common version
       String commonVersion = org.xins.common.Library.getVersion();
       if (! serverVersion.equals(commonVersion)) {
          Log.log_3226(serverVersion, commonVersion);
       }
 
       // Warn if the current XINS version is not a production version
       if (! Library.isProductionRelease(serverVersion)) {
          Log.log_3227(serverVersion);
       }
 
 
       //-------------------------------------------------------------------//
       //                     Checks and preparations                       //
       //-------------------------------------------------------------------//
 
       // Check preconditions
       synchronized (_stateLock) {
          if (_state != INITIAL                 && _state != FRAMEWORK_BOOTSTRAP_FAILED
           && _state != API_CONSTRUCTION_FAILED && _state != API_BOOTSTRAP_FAILED
           && _state != API_INITIALIZATION_FAILED) {
             Log.log_3201(_state == null ? null : _state.getName());
             throw new ServletException();
          } else if (config == null) {
             Log.log_3202("config == null");
             throw new ServletException();
          }
 
          // Get the ServletContext
          ServletContext context = config.getServletContext();
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
 
          // Store the ServletConfig object, per the Servlet API Spec, see:
          // http://java.sun.com/products/servlet/2.3/javadoc/javax/servlet/Servlet.html#getServletConfig()
          // TODO: Store this only if the initialization completely succeeded
          //       and no exception was thrown.
          _servletConfig = config;
 
          // Store the localhost hostname for the contextID
          _hostname = IPAddressUtils.getLocalHost();
 
          //----------------------------------------------------------------//
          //                     Bootstrap framework                        //
          //----------------------------------------------------------------//
 
          // Proceed to first actual stage
          setState(BOOTSTRAPPING_FRAMEWORK);
 
          // Determine configuration file location
          try {
             _configFile = System.getProperty(CONFIG_FILE_SYSTEM_PROPERTY);
          } catch (SecurityException exception) {
             Log.log_3230(exception, CONFIG_FILE_SYSTEM_PROPERTY);
          }
 
          // If the config file is not set at start-up try to get it from the web.xml
          if (_configFile == null) {
             Log.log_3231(CONFIG_FILE_SYSTEM_PROPERTY);
             _configFile = config.getInitParameter(CONFIG_FILE_SYSTEM_PROPERTY);
          }
 
          // Property value must be set
          // NOTE: Don't trim the configuration file name, since it may start
          //       with a space or other whitespace character.
          if (_configFile == null || _configFile.length() < 1) {
             Log.log_3205(CONFIG_FILE_SYSTEM_PROPERTY);
             setState(FRAMEWORK_BOOTSTRAP_FAILED);
             throw new ServletException();
          }
          
          // Set the path separator correctly if needed
          _configFile = _configFile.replace('/', File.separatorChar).replace('\\', File.separatorChar);
 
          // Initialize the logging subsystem
          readRuntimeProperties();
 
          // Log XINS version
          Log.log_3225(serverVersion);
 
          // Warn if API build version is more recent than running version
          if (Library.isProductionRelease(serverVersion)) {
             String buildVersion = config.getInitParameter(API_BUILD_VERSION_PROPERTY);
             if (buildVersion == null || (Library.isProductionRelease(buildVersion) && Library.isMoreRecent(buildVersion))) {
                Log.log_3229(buildVersion, serverVersion);
             }
          }
 
 
          //----------------------------------------------------------------//
          //                        Construct API                           //
          //----------------------------------------------------------------//
 
          // Proceed to next stage
          setState(CONSTRUCTING_API);
 
          // Determine the API class
          // TODO: Should we trim all init parameters?
          String apiClassName = config.getInitParameter(API_CLASS_PROPERTY);
          apiClassName = (apiClassName == null) ? apiClassName : apiClassName.trim();
          if (apiClassName == null || apiClassName.length() < 1) {
             Log.log_3206(API_CLASS_PROPERTY);
             setState(API_CONSTRUCTION_FAILED);
             throw new ServletException();
          }
 
          // Load the API class
          Class apiClass;
          try {
             apiClass = Class.forName(apiClassName);
          } catch (Throwable exception) {
             Log.log_3207(exception, API_CLASS_PROPERTY, apiClassName);
             setState(API_CONSTRUCTION_FAILED);
             ServletException servletException = new ServletException();
             ExceptionUtils.setCause(servletException, exception);
             throw servletException;
          }
 
          // Load the Logdoc if available
          try {
             // XXX: What if the API class name does not contain a dot?
             String packageName = apiClassName.substring(0, apiClassName.lastIndexOf('.') + 1);
 
             // This should execute the static initializer
             Class.forName(packageName + "Log");
             // TODO: Log if the API indeed has logdoc
 
          // API does not have any logdoc.
          } catch (ClassNotFoundException cnfe) {
             // TODO: Log if the API does not have any logdoc
             // ignore
 
          } catch (Throwable t) {
 
             // FIXME for Anthony: Distinguish between case where locale is not
             //                    supported (handled here) and other cases.
 
             // TODO: Should this not be an _init_ instead of a _bootstrap_
             //       issue?
 
             // The locale is not supported by the API
             Log.log_3309(LogCentral.getLocale(), config.getInitParameter(API_NAME_PROPERTY));
             setState(API_CONSTRUCTION_FAILED);
             ServletException servletException = new ServletException();
             ExceptionUtils.setCause(servletException, t);
             throw servletException;
          }
 
          // Check that the loaded API class is derived from the API base class
          if (! API.class.isAssignableFrom(apiClass)) {
             Log.log_3208(API_CLASS_PROPERTY, apiClassName, API.class.getName() + ".class.isAssignableFrom(apiClass) == false");
             setState(API_CONSTRUCTION_FAILED);
             throw new ServletException();
          }
 
          // Get the SINGLETON field and the value of it
          Field singletonField;
          try {
             singletonField = apiClass.getDeclaredField("SINGLETON");
             _api = (API) singletonField.get(null);
          } catch (Throwable exception) {
             // XXX: Log unexpected exception (#3052) here?
             Log.log_3208(API_CLASS_PROPERTY, apiClassName, exception.getClass().getName());
             setState(API_CONSTRUCTION_FAILED);
             ServletException servletException = new ServletException();
             ExceptionUtils.setCause(servletException, exception);
             throw servletException;
          }
 
          // Make sure that the field is an instance of that same class and not
          // of a subclass
          // TODO: Why check this at all?
          if (_api == null) {
             Log.log_3208(API_CLASS_PROPERTY, apiClassName, "apiClass.getDeclaredField(\"SINGLETON\").get(null) == null");
             setState(API_CONSTRUCTION_FAILED);
             throw new ServletException();
          } else if (_api.getClass() != apiClass) {
             Log.log_3208(API_CLASS_PROPERTY, apiClassName, "apiClass.getDeclaredField(\"SINGLETON\").get(null).getClass() != apiClass");
             setState(API_CONSTRUCTION_FAILED);
             throw new ServletException();
          }
 
 
          //----------------------------------------------------------------//
          //                        Bootstrap API                           //
          //----------------------------------------------------------------//
 
          // Proceed to next stage
          setState(BOOTSTRAPPING_API);
 
          // Store the name of the API
          _apiName = config.getInitParameter(API_NAME_PROPERTY);
          if (_apiName == null) {
 
             // Should never happen
             _apiName= "-";
          }
 
          // TODO: Enable this for XINS 2.0.0:
 /*
          if (TextUtils.isEmpty(_apiName)) {
             Log.log_3209(API_NAME_PROPERTY);
             setState(API_BOOTSTRAP_FAILED);
             throw new ServletException();
          }
  */
          // XXX: Trim the API name?
 
          // Bootstrap the API
          Throwable caught;
          try {
             _api.bootstrap(new ServletConfigPropertyReader(config));
             caught = null;
 
          // Missing required property
          } catch (MissingRequiredPropertyException exception) {
             Log.log_3209(exception.getPropertyName());
             caught = exception;
 
          // Invalid property value
          } catch (InvalidPropertyValueException exception) {
             Log.log_3210(exception.getPropertyName(), exception.getPropertyValue(), exception.getReason());
             caught = exception;
 
          // Other bootstrap error
          } catch (Throwable exception) {
             Log.log_3211(exception);
             caught = exception;
          }
 
          // Determine the default calling convention
          try {
             _defaultCallingConvention = config.getInitParameter(API_CALLING_CONVENTION_PROPERTY);
             if (! TextUtils.isEmpty(_defaultCallingConvention)) {
                _callingConvention = createCallingConvention(_defaultCallingConvention);
                if (_callingConvention == null) {
                   Log.log_3210(API_CALLING_CONVENTION_PROPERTY, _defaultCallingConvention, "No such calling convention.");
                   setState(API_BOOTSTRAP_FAILED);
                   throw new ServletException();
                }
                // TODO: Log that we use the specified calling convention
             } else {
                // TODO: Use shared StandardCallingConvention instance
                _defaultCallingConvention = "_xins-std";
                _callingConvention = new StandardCallingConvention();
                _callingConvention.bootstrap(new ServletConfigPropertyReader(_servletConfig));
                // TODO: Log that we use the default calling convention
             }
          } catch (Throwable exception) {
             // TODO log
             caught = exception;
          }
 
          // Throw a ServletException if the bootstrap failed
          if (caught != null) {
             setState(API_BOOTSTRAP_FAILED);
             ServletException se = new ServletException("API bootstrap failed.");
             ExceptionUtils.setCause(se, caught);
             throw se;
          }
 
          // Make the API have a link to this APIServlet
          _api.setAPIServlet(this);
 
 
          //----------------------------------------------------------------//
          //                Determine config file reload interval           //
          //----------------------------------------------------------------//
 
          int interval;
          boolean intervalParsed;
          try {
             interval = determineConfigReloadInterval();
             intervalParsed = true;
          } catch (InvalidPropertyValueException exception) {
             intervalParsed = false;
             interval = DEFAULT_CONFIG_RELOAD_INTERVAL;
          }
 
 
          //----------------------------------------------------------------//
          //                      Initialize the API                        //
          //----------------------------------------------------------------//
 
          if (intervalParsed) {
             initAPI();
          }
 
 
          //----------------------------------------------------------------//
          //                      Watch the config file                     //
          //----------------------------------------------------------------//
 
          // Create and start a file watch thread
          if (interval > 0) {
             _configFileWatcher = new FileWatcher(_configFile, interval, _configFileListener);
             _configFileWatcher.start();
          }
       }
    }
 
    /**
     * Initializes the API using the current runtime settings.
     */
    void initAPI() {
 
       setState(INITIALIZING_API);
 
       synchronized (_runtimePropertiesLock) {
 
          boolean succeeded = false;
 
          // Determine the log locale
          String newLocale = _runtimeProperties.get(LogCentral.LOG_LOCALE_PROPERTY);
          if (newLocale == null) {
             newLocale = _runtimeProperties.get(LOG_LOCALE_PROPERTY);
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
                   setState(API_INITIALIZATION_FAILED);
                   return;
                }
             }
          }
 
          try {
             
             _api.init(_runtimeProperties);
             
             // Initialize the default calling convention for this API
             if (_callingConvention != null) {
                _callingConvention.init(_runtimeProperties);
             }
 
             succeeded = true;
          } catch (MissingRequiredPropertyException exception) {
             Log.log_3411(exception.getPropertyName());
          } catch (InvalidPropertyValueException exception) {
             Log.log_3412(exception.getPropertyName(), exception.getPropertyValue(), exception.getReason());
          } catch (InitializationException exception) {
             Log.log_3413(exception.getMessage());
          } catch (Throwable exception) {
             Log.log_3414(exception);
          } finally {
 
             if (succeeded) {
                setState(READY);
                Log.log_3415();
             } else {
                setState(API_INITIALIZATION_FAILED);
             }
          }
       }
    }
 
    /**
     * Reads the runtime properties file, initializes the logging subsystem
     * with the read properties and then returns those properties. If the
     * properties cannot be read from the file for any reason, then an empty
     * set of properties is returned.
     */
    private void readRuntimeProperties() {
 
       Log.log_3300(_configFile);
 
       synchronized (_runtimePropertiesLock) {
 
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
 
          // Change the hostname if needed
          String hostname = properties.getProperty(HOSTNAME_PROPERTY);
          if (hostname != null && !hostname.trim().equals("") && !hostname.equals(_hostname)) {
             Log.log_3310(_hostname, hostname);
             _hostname = hostname;
          }
 
          _runtimeProperties = new PropertiesPropertyReader(properties);
       }
    }
 
    /**
     * Configure the Log4J system
     *
     * @param properties
     *    the runtime properties containing the Log4J configuration.
     */
    private void configureLogger(Properties properties) {
       PropertyConfigurator.configure(properties);
 
       // Determine if Log4J is properly initialized
       Enumeration appenders = LogManager.getLoggerRepository().getRootLogger().getAllAppenders();
       if (appenders instanceof NullEnumeration) {
          Log.log_3304(_configFile);
          configureLoggerFallback();
       } else {
          Log.log_3305();
       }
    }
 
    /**
     * Returns the <code>ServletConfig</code> object which contains the
     * build properties for this servlet. The returned {@link ServletConfig}
     * object is the one passed to the {@link #init(ServletConfig)} method.
     *
     * @return
     *    the {@link ServletConfig} object that was used to initialize this
     *    servlet, not <code>null</code> if this servlet is indeed already
     *    initialized.
     */
    public ServletConfig getServletConfig() {
       return _servletConfig;
    }
 
    /**
     * Handles a request to this servlet (wrapper method). If any of the
     * arguments is <code>null</code>, then the behaviour of this method is
     * undefined.
     *
     * @param request
     *    the servlet request, should not be <code>null</code>.
     *
     * @param response
     *    the servlet response, should not be <code>null</code>.
     *
     * @throws IOException
     *    if there is an error error writing to the response output stream.
     */
    public void service(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
 
       // Determine diagnostic context ID
       String contextID = request.getParameter("_context");
 
       // If there is no diagnostic context ID, then generate one.
       if ((contextID == null) || (contextID.length() < 1)) {
          contextID = generateContextID();
       }
 
       // Associate the context ID with this thread
       NDC.push(contextID);
 
       // Handle the request
       try {
          doService(request, response);
 
       // And disassociate the context ID from this thread
       } finally {
          NDC.pop();
          NDC.remove();
       }
    }
 
    /**
     * Handles a request to this servlet (implementation method). If any of the
     * arguments is <code>null</code>, then the behaviour of this method is
     * undefined.
     *
     * <p>This method is called from
     * {@link #service(HttpServletRequest,HttpServletResponse)}. The latter
     * first determines the <em>nested diagnostic context</em> and then
     * forwards the call to this method.
     *
     * @param request
     *    the servlet request, should not be <code>null</code>.
     *
     * @param response
     *    the servlet response, should not be <code>null</code>.
     *
     * @throws IOException
     *    if there is an error error writing to the response output stream.
     */
    private void doService(final HttpServletRequest request,
                           final HttpServletResponse response)
    throws IOException {
 
       final String THIS_METHOD = "doService("
                                + HttpServletRequest.class.getName()
                                + ','
                                + HttpServletResponse.class.getName()
                                + ')';
 
       // TODO: TRACE: Enter method
 
       // Determine current time
       long start = System.currentTimeMillis();
 
       // Determine the remote IP address and the query string
       String ip          = request.getRemoteAddr();
       String queryString = request.getQueryString();
 
       // Check the HTTP request method
       String method = request.getMethod();
       boolean sendOutput = "GET".equals(method) || "POST".equals(method);
       if (!sendOutput) {
          if ("OPTIONS".equals(method)) {
             Log.log_3521(ip, method, queryString);
             response.setContentLength(0);
             response.setHeader("Accept", "GET, HEAD, POST");
             response.setStatus(HttpServletResponse.SC_OK);
             return;
          } else if ("HEAD".equals(method)) {
             response.setContentLength(0);
 
          // If the method is not recognized, return '405 Method Not Allowed'
          } else {
             Log.log_3520(ip, method, queryString);
             response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
             return;
          }
       }
       Log.log_3521(ip, method, queryString);
 
 
       // Determine the calling convention. If an existing calling convention
       // is specified in the request, then use that, otherwise use the calling
       // convention stored in the field.
       String ccParam = (String) request.getParameter(CALLING_CONVENTION_PARAMETER);
       CallingConvention callingConvention = null;
       if (ccParam != null && !ccParam.equals(_defaultCallingConvention)) {
          try {
             callingConvention = createCallingConvention(ccParam);
             if (callingConvention != null) {
                callingConvention.init(_runtimeProperties);
             }
          } catch (Exception ex) {
             
             // the calling convention could not be created or initialized
             response.sendError(HttpServletResponse.SC_BAD_REQUEST);
             return;
          }
       }
       
       if (callingConvention == null) {
          callingConvention = _callingConvention;
          // TODO: Log that no calling convention was specified
       }
 
       // Call the API if the state is READY
       FunctionResult result;
       State state = getState();
       if (state == READY) {
 
          String SUBJECT_CLASS  = callingConvention.getClass().getName();
          String SUBJECT_METHOD = "convertRequest("
                                  + HttpServletRequest.class.getName()
                                  + ')';
          try {
 
             // Convert the HTTP request to an incoming XINS request
             FunctionRequest xinsRequest = callingConvention.convertRequest(request);
 
             // Call the function
             SUBJECT_CLASS  = _api.getClass().getName();
             SUBJECT_METHOD = "handleCall(long," + FunctionRequest.class.getName() + ",java.lang.String)";
             result = _api.handleCall(start, xinsRequest, ip);
 
          } catch (Throwable exception) {
 
             int error;
 
             // If the function is not specified, then return '404 Not Found'
             if (exception instanceof FunctionNotSpecifiedException) {
                error = HttpServletResponse.SC_NOT_FOUND;
 
             // If the request is invalid, then return '400 Bad Request'
             } else if (exception instanceof InvalidRequestException) {
                error = HttpServletResponse.SC_BAD_REQUEST;
 
             // If access is denied, return '403 Forbidden'
             } else if (exception instanceof AccessDeniedException) {
                error = HttpServletResponse.SC_FORBIDDEN;
 
             // If no matching function is found, return '404 Not Found'
             } else if (exception instanceof NoSuchFunctionException) {
                error = HttpServletResponse.SC_NOT_FOUND;
 
             // Otherwise an unexpected exception is thrown
             } else {
                error = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                Utils.logProgrammingError(CLASSNAME,
                                          THIS_METHOD,
                                          SUBJECT_CLASS,
                                          SUBJECT_METHOD,
                                          null,
                                          exception);
             }
 
             response.sendError(error);
             return;
          }
 
       // Otherwise return an appropriate 50x HTTP response code
       } else if (state == INITIAL
               || state == BOOTSTRAPPING_FRAMEWORK
               || state == CONSTRUCTING_API
               || state == BOOTSTRAPPING_API
               || state == INITIALIZING_API) {
          response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
          return;
       } else {
          response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          return;
       }
 
       // Send the output only if GET or POST
       if (sendOutput) {
          callingConvention.convertResult(result, response, request);
       }
    }
 
    /**
     * Retrieves a calling convention based on a name. If the name is not
     * recognized as identifying a certain calling convention, then
     * <code>null</code> is returned.
     *
     * <p>Either an existing {@link CallingConvention} object is retrieved or a
     * new one is constructed.
     *
     * @param name
     *    the name of the calling convention to retrieve, can be <code>null</code>.
     *
     * @return
     *    a {@link CallingConvention} object that matches the specified calling
     *    convention name, or <code>null</code> if no match is found.
     */
    private CallingConvention createCallingConvention(String name) 
    throws MissingRequiredPropertyException,
           InvalidPropertyValueException,
           BootstrapException {
 
       CallingConvention createdConvention = null;
       // Old-style calling convention
       if (OLD_STYLE_CALLING_CONVENTION.equals(name)) {
          createdConvention = new OldStyleCallingConvention();
 
       // Standard calling convention
       } else if (STANDARD_CALLING_CONVENTION.equals(name)) {
          createdConvention = new StandardCallingConvention();
 
       // XML calling convention
       } else if (XML_CALLING_CONVENTION.equals(name)) {
          createdConvention = new XMLCallingConvention();
 
       // XSLT calling convention
       } else if (XSLT_CALLING_CONVENTION.equals(name)) {
          createdConvention = new XSLTCallingConvention();
 
       // Custom calling convention
       } else if (name.charAt(0) != '_') {
          if (!name.equals(_servletConfig.getInitParameter(API_CALLING_CONVENTION_PROPERTY))) {
             
             // TODO Log
             return null;
          }
          String conventionClass = _servletConfig.getInitParameter(API_CALLING_CONVENTION_CLASS_PROPERTY);
          try {
             
             // First try with a constructor with the API as parameter then with the empty constructor
             try {
                Class[] construtorClasses = { API.class };
                Object[] constructorArgs = { _api };
                Constructor customConstructor = Class.forName(conventionClass).getConstructor(construtorClasses);
                createdConvention = (CustomCallingConvention) customConstructor.newInstance(constructorArgs);
             } catch (NoSuchMethodException nsmex) {
                createdConvention = (CustomCallingConvention) Class.forName(conventionClass).newInstance();
             }
          } catch (Exception ex) {
 
             // TODO Log
             ex.printStackTrace();
             return null;
          }
 
       // Otherwise return nothing
       } else {
          return null;
       }
       createdConvention.bootstrap(new ServletConfigPropertyReader(_servletConfig));
       return createdConvention;
    }
 
    /**
     * Re-initialise the properties if the property file has changed.
     */
    void reloadPropertiesIfChanged() {
       if (_configFileWatcher == null) {
          _configFileListener.reinit();
       } else {
          _configFileWatcher.interrupt();
       }
    }
 
    /**
     * Destroys this servlet. A best attempt will be made to release all
     * resources.
     *
     * <p>After this method has finished, it will set the state to
     * <em>disposed</em>. In that state no more requests will be handled.
     */
    public void destroy() {
 
       // Stop the FileWatcher
       if (_configFileWatcher != null) {
          _configFileWatcher.end();
       }
 
       Log.log_3600();
 
       // Set the state temporarily to DISPOSING
       setState(DISPOSING);
 
       // Destroy the API
       if (_api != null) {
          try {
             _api.deinit();
          } catch (Throwable exception) {
             Log.log_3601(exception);
          }
       }
 
       // Set the state to DISPOSED
       setState(DISPOSED);
 
       Log.log_3602();
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * State of an <code>APIServlet</code>.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
     *
     * @since XINS 1.0.0
     */
    private static final class State extends Object {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>State</code> object.
        *
        * @param name
        *    the name of this state, cannot be <code>null</code>.
        *
        * @throws IllegalArgumentException
        *    if <code>name == null</code>.
        */
       private State(String name) throws IllegalArgumentException {
 
          // Check preconditions
          MandatoryArgumentChecker.check("name", name);
 
          _name = name;
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       /**
        * The name of this state. Cannot be <code>null</code>.
        */
       private final String _name;
 
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Returns the name of this state.
        *
        * @return
        *    the name of this state, cannot be <code>null</code>.
        */
       public String getName() {
          return _name;
       }
 
       /**
        * Returns a textual representation of this object.
        *
        * @return
        *    the name of this state, never <code>null</code>.
        */
       public String toString() {
          return _name;
       }
    }
 
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
 
          synchronized (_runtimePropertiesLock) {
 
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
 
             // Reconfigure the Log4J system
             LogManager.getLoggerRepository().resetConfiguration();
             configureLogger(PropertyReaderConverter.toProperties(_runtimeProperties));
 
             // Re-initialize the API
             initAPI();
 
             // Update the file watch interval
             int oldInterval = _configFileWatcher == null ? 0 : _configFileWatcher.getInterval();
             if (oldInterval != newInterval) {
                if (newInterval == 0 && _configFileWatcher != null) {
                   _configFileWatcher.end();
                   _configFileWatcher = null;
               } else if (newInterval > 0 && _configFileWatcher != null) {
                   _configFileWatcher = new FileWatcher(_configFile, newInterval, _configFileListener);
                   _configFileWatcher.start();
                } else {
                   _configFileWatcher.setInterval(newInterval);
                   Log.log_3403(_configFile, oldInterval, newInterval);
                }
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
