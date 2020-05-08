 /*
  * $Id$
  */
 package org.xins.server;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.lang.reflect.Field;
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
 
 import org.xins.logdoc.LogCentral;
 import org.xins.logdoc.UnsupportedLocaleException;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.collections.InvalidPropertyValueException;
 import org.xins.common.collections.MissingRequiredPropertyException;
 import org.xins.common.collections.PropertiesPropertyReader;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.io.FileWatcher;
 import org.xins.common.manageable.BootstrapException;
 import org.xins.common.manageable.InitializationException;
 import org.xins.common.servlet.ServletConfigPropertyReader;
 import org.xins.common.text.HexConverter;
 
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
  */
 public final class APIServlet
 extends HttpServlet {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
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
     * The default configuration file modification check interval, in seconds.
     */
    public static final int DEFAULT_CONFIG_RELOAD_INTERVAL = 60;
 
    /**
     * The name of the build property that specifies the name of the
     * API class to load.
     */
    public static final String API_CLASS_PROPERTY = "org.xins.api.class";
 
    /**
     * The name of the runtime property that specifies the locale for the log
     * messages.
     */
    public static final String LOG_LOCALE_PROPERTY = "org.xins.server.log.locale";
 
    /**
     * The default locale used for starting up when the locale is not defined in
     * command line arguments.
     */
    public static final String DEFAULT_LOCALE = "us_US";
 
    /**
     * The response encoding format.
     */
    public static final String RESPONSE_ENCODING = "UTF-8";
    // TODO: Allow this to be configured
 
    /**
     * The content type of the HTTP response.
     */
    public static final String RESPONSE_CONTENT_TYPE = "text/xml;charset=" + RESPONSE_ENCODING;
 
 
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
       _stateLock          = new Object();
       _state              = INITIAL;
       _configFileListener = new ConfigurationFileListener();
       _random             = new Random();
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * Lock for the <code>_state</code> field. This object must be locked on
    * before {@link _state} may be read or changed.
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
     * The stored servlet configuration object.
     */
    private ServletConfig _servletConfig;
 
    /**
     * The name of the configuration file.
     */
    private String _configFile;
 
    /**
     * The API that this servlet forwards requests to.
     */
    private API _api;
 
    /**
     * Configuration file watcher.
     */
    private FileWatcher _configFileWatcher;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Generates a random diagnostic context identifier.
     *
     * @return
     *    the generated diagnostic context identifier, never <code>null</code>
     *    but always a 16-character string.
     */
    private String generateContextID() {
       return HexConverter.toHexString(_random.nextLong());
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
     */
    private void setState(State newState)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("newState", newState);
 
       // TODO: Check state
 
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
 
       Log.log_1100(oldState._name, newState._name);
    }
 
    /**
     * Determines the interval for checking the runtime properties file for
     * modifications.
     *
     * @param properties
     *    the runtime properties to read from, should not be <code>null</code>.
     *
     * @return
     *    the interval to use, always &gt;= 1.
     */
    private int determineConfigReloadInterval(PropertyReader properties) {
 
       // Get the runtime property
       String s = properties.get(CONFIG_RELOAD_INTERVAL_PROPERTY);
       int interval = -1;
 
       // If the property is set, parse it
       if (s != null && s.length() >= 1) {
          try {
             interval = Integer.parseInt(s);
             if (interval < 1) {
                Log.log_1410(_configFile, CONFIG_RELOAD_INTERVAL_PROPERTY, s);
             } else {
                Log.log_1411(_configFile, CONFIG_RELOAD_INTERVAL_PROPERTY, s);
             }
          } catch (NumberFormatException nfe) {
             Log.log_1410(_configFile, CONFIG_RELOAD_INTERVAL_PROPERTY, s);
          }
 
       // Otherwise, if the property is not set, use the default
       } else {
          Log.log_1408(_configFile, CONFIG_RELOAD_INTERVAL_PROPERTY);
       }
 
       // If the interval is not set, using the default
       if (interval < 0) {
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
     *        <br />System properties are the responsibility of the
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
 
       Log.log_1200();
 
       //-------------------------------------------------------------------//
       //                     Checks and preparations                       //
       //-------------------------------------------------------------------//
 
       // Make sure the Library class is initialized
       String version = Library.getVersion();
 
       // Check preconditions
       synchronized (_stateLock) {
          if (_state != INITIAL                 && _state != FRAMEWORK_BOOTSTRAP_FAILED
           && _state != API_CONSTRUCTION_FAILED && _state != API_BOOTSTRAP_FAILED
           && _state != API_INITIALIZATION_FAILED) {
             Log.log_1201(_state == null ? null : _state._name);
             throw new ServletException();
          } else if (config == null) {
             Log.log_1202("config == null");
             throw new ServletException();
          }
 
          // Get the ServletContext
          ServletContext context = config.getServletContext();
          if (context == null) {
             Log.log_1202("config.getServletContext() == null");
             throw new ServletException();
          }
 
          // Check the expected vs implemented Java Servlet API version
 	 // Both 2.2 and 2.3 are supported
          int major = context.getMajorVersion();
          int minor = context.getMinorVersion();
          if (major != 2 || (minor != 2 && minor != 3)) {
             String expected = "2.2/2.3";
             String actual   = "" + major + '.' + minor;
             Log.log_1203(actual, expected);
          }
 
          // Store the ServletConfig object, per the Servlet API Spec, see:
          // http://java.sun.com/products/servlet/2.3/javadoc/javax/servlet/Servlet.html#getServletConfig()
          _servletConfig = config;
 
 
          //----------------------------------------------------------------//
          //                     Bootstrap framework                        //
          //----------------------------------------------------------------//
 
          // Proceed to first actual stage
          setState(BOOTSTRAPPING_FRAMEWORK);
 
          // Determine configuration file location
          try {
             _configFile = System.getProperty(CONFIG_FILE_SYSTEM_PROPERTY);
          } catch (SecurityException exception) {
             Log.log_1204(exception, CONFIG_FILE_SYSTEM_PROPERTY);
             setState(FRAMEWORK_BOOTSTRAP_FAILED);
             throw new ServletException();
          }
 
          // Property value must be set
          // NOTE: Don't trim the configuration file name, since it may start
          //       with a space or other whitespace character.
          if (_configFile == null || _configFile.length() < 1) {
             Log.log_1205(CONFIG_FILE_SYSTEM_PROPERTY);
             setState(FRAMEWORK_BOOTSTRAP_FAILED);
             throw new ServletException();
          }
 
          // Initialize the logging subsystem
          PropertyReader runtimeProperties = readRuntimeProperties();
 
 
          //----------------------------------------------------------------//
          //                        Construct API                           //
          //----------------------------------------------------------------//
 
          // Proceed to next stage
          setState(CONSTRUCTING_API);
 
          // Determine the API class
          String apiClassName = config.getInitParameter(API_CLASS_PROPERTY);
          apiClassName = (apiClassName == null) ? apiClassName : apiClassName.trim();
          if (apiClassName == null || apiClassName.length() < 1) {
             Log.log_1206(API_CLASS_PROPERTY);
             setState(API_CONSTRUCTION_FAILED);
             throw new ServletException();
          }
 
          // Load the API class
          Class apiClass;
          try {
             apiClass = Class.forName(apiClassName);
          } catch (Throwable exception) {
             Log.log_1207(exception, API_CLASS_PROPERTY, apiClassName);
             setState(API_CONSTRUCTION_FAILED);
             throw new ServletException();
          }
 
          // Check that the loaded API class is derived from the API base class
          if (! API.class.isAssignableFrom(apiClass)) {
             Log.log_1208(API_CLASS_PROPERTY, apiClassName, API.class.getName() + ".class.isAssignableFrom(apiClass) == false");
             setState(API_CONSTRUCTION_FAILED);
             throw new ServletException();
          }
 
          // Get the SINGLETON field and the value of it
          Field singletonField;
          try {
             singletonField = apiClass.getDeclaredField("SINGLETON");
             _api = (API) singletonField.get(null);
          } catch (Throwable exception) {
             Log.log_1208(API_CLASS_PROPERTY, apiClassName, exception.getClass().getName());
             setState(API_CONSTRUCTION_FAILED);
             throw new ServletException();
          }
 
          // Make sure that the field is an instance of that same class
          if (_api == null) {
             Log.log_1208(API_CLASS_PROPERTY, apiClassName, "apiClass.getDeclaredField(\"SINGLETON\").get(null) == null");
             setState(API_CONSTRUCTION_FAILED);
             throw new ServletException();
          } else if (_api.getClass() != apiClass) {
             Log.log_1208(API_CLASS_PROPERTY, apiClassName, "apiClass.getDeclaredField(\"SINGLETON\").get(null).getClass() != apiClass");
             setState(API_CONSTRUCTION_FAILED);
             throw new ServletException();
          }
 
 
          //----------------------------------------------------------------//
          //                        Bootstrap API                           //
          //----------------------------------------------------------------//
 
          // Proceed to next stage
          setState(BOOTSTRAPPING_API);
 
          // Bootstrap the API
          boolean succeeded = false;
          try {
             _api.bootstrap(new ServletConfigPropertyReader(config));
             succeeded = true;
          } catch (MissingRequiredPropertyException exception) {
             Log.log_1209(exception.getPropertyName());
          } catch (InvalidPropertyValueException exception) {
             Log.log_1210(exception.getPropertyName(), exception.getPropertyValue());
          } catch (BootstrapException exception) {
             Log.log_1211(exception.getMessage());
          } catch (Throwable exception) {
             Log.log_1212(exception);
          } finally {
             if (!succeeded) {
                setState(API_BOOTSTRAP_FAILED);
                throw new ServletException();
             }
          }
 
 
          //----------------------------------------------------------------//
          //                      Initialize the API                        //
          //----------------------------------------------------------------//
 
          initAPI(runtimeProperties);
 
 
          //----------------------------------------------------------------//
          //                      Watch the config file                     //
          //----------------------------------------------------------------//
 
          int interval = determineConfigReloadInterval(runtimeProperties);
 
          // Create and start a file watch thread
          _configFileWatcher = new FileWatcher(_configFile, interval, _configFileListener);
          Log.log_1412(_configFile, CONFIG_RELOAD_INTERVAL_PROPERTY, interval);
          _configFileWatcher.start();
       }
    }
 
    /**
     * Initializes the API using the specified runtime settings.
     *
     * @param runtimeProperties
     *    the runtime settings, guaranteed not to be <code>null</code>.
     */
    private void initAPI(PropertyReader runtimeProperties) {
 
       setState(INITIALIZING_API);
 
       boolean succeeded = false;
       try {
          _api.init(runtimeProperties);
          succeeded = true;
       } catch (MissingRequiredPropertyException exception) {
          Log.log_1413(exception.getPropertyName());
       } catch (InvalidPropertyValueException exception) {
          Log.log_1414(exception.getPropertyName(), exception.getPropertyValue());
       } catch (InitializationException exception) {
          Log.log_1415(exception.getMessage());
       } catch (Throwable exception) {
          Log.log_1416(exception);
       } finally {
 
          if (succeeded) {
             setState(READY);
             Log.log_1418();
          } else {
             setState(API_INITIALIZATION_FAILED);
             return;
          }
       }
    }
 
    /**
     * Reads the runtime properties file, initializes the logging subsystem
     * with the read properties and then returns those properties. If the
     * properties cannot be read from the file for any reason, then an empty
     * set of properties is returned.
     *
     * @return
     *    the properties read from the config file, never <code>null</code>.
     */
    private PropertyReader readRuntimeProperties() {
 
       Log.log_1300();
 
       Properties properties = new Properties();
       try {
 
          // Open the file
          FileInputStream in = new FileInputStream(_configFile);
 
          // Load the properties
          properties.load(in);
 
          // Close the file
          in.close();
       } catch (FileNotFoundException exception) {
          Log.log_1301(exception, _configFile);
       } catch (SecurityException exception) {
          Log.log_1302(exception, _configFile);
       } catch (IOException exception) {
          Log.log_1303(exception, _configFile);
       }
 
       // TODO: Should we reset the logging subsystem if the Log4J
       // TODO  properties have been removed from the xins.properties file?
       // TODO  Determine the current behaviour and make a decision.
 
 
       // Attempt to configure Log4J
       PropertyConfigurator.configure(properties);
 
       // Determine if Log4J is properly initialized
       Enumeration appenders = LogManager.getLoggerRepository().getRootLogger().getAllAppenders();
       if (appenders instanceof NullEnumeration) {
          Log.log_1304(_configFile);
          configureLoggerFallback();
       } else {
          Log.log_1305();
       }
 
       // Determine the log locale
       String newLocale = properties.getProperty(LOG_LOCALE_PROPERTY);
 
       // If the log locale is set, apply it
       if (newLocale != null) {
          String currentLocale = Log.getTranslationBundle().getName();
          if (!currentLocale.equals(newLocale)) {
             Log.log_1306(currentLocale, newLocale);
             try {
                LogCentral.setLocale(newLocale);
                Log.log_1307(currentLocale, newLocale);
             } catch (UnsupportedLocaleException exception) {
                Log.log_1308(currentLocale, newLocale);
             }
          }
       }
 
       return new PropertiesPropertyReader(properties);
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
       }
 
       // TODO: Document _context somewhere
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
    private void doService(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
 
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
             Log.log_1501(ip, method, queryString);
             response.setContentLength(0);
             response.setHeader("Accept", "GET, HEAD, POST");
             response.setStatus(HttpServletResponse.SC_OK);
             return;
          } else if ("HEAD".equals(method)) {
             response.setContentLength(0);
 
          // If the method is not recognized, return '405 Method Not Allowed'
          } else {
             Log.log_1500(ip, method, queryString);
             response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
             return;
          }
       }
       Log.log_1501(ip, method, queryString);
 
       // XXX: Consider using OutputStream instead of Writer, for improved
       // XXX: performance
 
       // Call the API if the state is READY
       CallResult result;
       State state = getState();
       if (state == READY) {
          try {
             result = _api.handleCall(start, request);
 
          // If access is denied, return '403 Forbidden'
          } catch (AccessDeniedException exception) {
             response.sendError(HttpServletResponse.SC_FORBIDDEN);
             return;
 
          // If no matching function is found, return '404 Not Found'
          } catch (NoSuchFunctionException exception) {
             response.sendError(HttpServletResponse.SC_NOT_FOUND);
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
 
          // Determine the XSLT to link to
          String xslt = request.getParameter("_xslt");
 
          // Send the XML output to the stream and flush
          PrintWriter out = response.getWriter();
          response.setContentType(RESPONSE_CONTENT_TYPE);
          response.setStatus(HttpServletResponse.SC_OK);
          CallResultOutputter.output(out, RESPONSE_ENCODING, result, xslt);
          out.flush();
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
 
       Log.log_1600();
 
       // Set the state temporarily to DISPOSING
       setState(DISPOSING);
 
       // Destroy the API
       if (_api != null) {
          try {
             _api.deinit();
          } catch (Throwable exception) {
             Log.log_1601(exception);
          }
       }
 
       // Set the state to DISPOSED
       setState(DISPOSED);
 
       Log.log_1602();
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
     * @since XINS 0.121
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
       String getName() {
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
     * @since XINS 0.121
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
 
       public void fileModified() {
 
          Log.log_1407(_configFile);
 
          // Apply the new runtime settings to the logging subsystem
          PropertyReader runtimeProperties = readRuntimeProperties();
 
          // Determine the interval
          int newInterval = determineConfigReloadInterval(runtimeProperties);
 
          // Update the file watch interval
          int oldInterval = _configFileWatcher.getInterval();
          if (oldInterval != newInterval) {
             _configFileWatcher.setInterval(newInterval);
             Log.log_1403(_configFile, oldInterval, newInterval);
          }
 
          // Re-initialize the API
          initAPI(runtimeProperties);
       }
 
       public void fileNotFound() {
          Log.log_1400(_configFile);
       }
 
       public void fileNotModified() {
          Log.log_1402(_configFile);
       }
 
       public void securityException(SecurityException exception) {
          Log.log_1401(exception, _configFile);
       }
    }
 }
