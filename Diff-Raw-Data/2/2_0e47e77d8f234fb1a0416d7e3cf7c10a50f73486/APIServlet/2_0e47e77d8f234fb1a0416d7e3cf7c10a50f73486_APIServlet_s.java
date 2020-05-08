 /*
  * $Id$
  */
 package org.xins.server;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.lang.reflect.Field;
 import java.util.Enumeration;
 import java.util.Properties;
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
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.util.collections.BasicPropertyReader;
 import org.xins.util.collections.InvalidPropertyValueException;
 import org.xins.util.collections.MissingRequiredPropertyException;
 import org.xins.util.collections.PropertiesPropertyReader;
 import org.xins.util.collections.PropertyReader;
 import org.xins.util.collections.ProtectedPropertyReader;
 import org.xins.util.io.FileWatcher;
 import org.xins.util.manageable.BootstrapException;
 import org.xins.util.manageable.InitializationException;
 import org.xins.util.servlet.ServletConfigPropertyReader;
 import org.xins.util.text.FastStringBuffer;
 
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
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
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
     * The expected version of the Java Servlet Specification, major part.
     */
    private static final int EXPECTED_SERVLET_VERSION_MAJOR = 2;
 
    /**
     * The expected version of the Java Servlet Specification, minor part.
     */
    private static final int EXPECTED_SERVLET_VERSION_MINOR = 3;
 
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
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Creates a string for use in an error message in case an unexpected
     * exception is caught.
     *
     * @param exception
     *    the caught exception, cannot be <code>null</code>.
     *
     * @return
     *    the character string to use in an error message, never
     *    <code>null</code>, always starts with <code>"due to
     *    unexpected"</code> and always ends with a full stop character
     *    (<code>'.'</code>).
     */
    static final String dueToUnexpected(Throwable exception)
    throws NullPointerException {
 
       // TODO: Remove this function
 
       FastStringBuffer buffer = new FastStringBuffer(4014);
       buffer.append("due to unexpected ");
       buffer.append(exception.getClass().getName());
 
       String message = exception.getMessage();
       if (message == null || message.length() < 1) {
          buffer.append(" with no message.");
       } else {
          buffer.append(" with message \"");
          buffer.append(message);
          buffer.append("\".");
       }
 
       return buffer.toString();
    }
 
    /**
     * Initializes the loggers to log to the console using a simple format
     * and no threshold.
     */
    static {
       configureLoggerFallback();
    }
 
    /**
     * Initializes the logging subsystem with fallback default settings.
     */
    private static final void configureLoggerFallback() {
       Properties settings = new Properties();
       settings.setProperty("log4j.rootLogger",              "ALL, console");
       settings.setProperty("log4j.appender.console",        "org.apache.log4j.ConsoleAppender");
       settings.setProperty("log4j.appender.console.layout", "org.apache.log4j.SimpleLayout");
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
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The current state.
     */
    private State _state;
 
    /**
     * Lock for <code>_state</code>
     */
    private final Object _stateLock;
 
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
     * The listener that is notified when the configuration file changes. Only
     * one instance is created ever.
     */
    private final ConfigurationFileListener _configFileListener;
 
    /**
     * Configuration file watcher.
     */
    private FileWatcher _configFileWatcher;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
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
 
       Log.log_1000(oldState._name, newState._name);
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
    private final int determineConfigReloadInterval(PropertyReader properties) {
 
       // Get the runtime property
       String s = properties.get(CONFIG_RELOAD_INTERVAL_PROPERTY);
       int interval = -1;
 
       // If the property is set, parse it
       if (s != null && s.length() >= 1) {
          Log.log_4009(_configFile, CONFIG_RELOAD_INTERVAL_PROPERTY, s);
          try {
             interval = Integer.parseInt(s);
             if (interval < 1) {
                Log.log_4010(_configFile, CONFIG_RELOAD_INTERVAL_PROPERTY, s);
             } else {
                Log.log_4011(_configFile, CONFIG_RELOAD_INTERVAL_PROPERTY, s);
             }
          } catch (NumberFormatException nfe) {
             Log.log_4010(_configFile, CONFIG_RELOAD_INTERVAL_PROPERTY, s);
          }
 
       // Otherwise, if the property is not set, use the default
       } else {
          Log.log_4008(_configFile, CONFIG_RELOAD_INTERVAL_PROPERTY);
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
 
       Log.log_2000();
 
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
             Log.log_2001(_state == null ? null : _state._name);
             throw new ServletException();
          } else if (config == null) {
             Log.log_2002("config == null");
             throw new ServletException();
          }
 
          // Get the ServletContext
          ServletContext context = config.getServletContext();
          if (context == null) {
             Log.log_2002("config.getServletContext() == null");
             throw new ServletException();
          }
 
          // Check the expected vs implemented Java Servlet API version
          int major = context.getMajorVersion();
          int minor = context.getMinorVersion();
          if (major != EXPECTED_SERVLET_VERSION_MAJOR || minor != EXPECTED_SERVLET_VERSION_MINOR) {
             String expected = "" + EXPECTED_SERVLET_VERSION_MAJOR + '.' + EXPECTED_SERVLET_VERSION_MINOR;
             String actual   = "" + major + '.' + minor;
             Log.log_2003(actual, expected);
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
             Log.log_2004(CONFIG_FILE_SYSTEM_PROPERTY, exception.getClass().getName(), exception.getMessage());
             setState(FRAMEWORK_BOOTSTRAP_FAILED);
             throw new ServletException();
          }
          
          // Property value must be set
          // NOTE: Don't trim the configuration file name, since it may start
          //       with a space or other whitespace character.
          if (_configFile == null || _configFile.length() < 1) {
             Log.log_2005(CONFIG_FILE_SYSTEM_PROPERTY);
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
             Log.log_2006(API_CLASS_PROPERTY);
             setState(API_CONSTRUCTION_FAILED);
             throw new ServletException();
          }
 
          // Load the API class
          Class apiClass;
          try {
             apiClass = Class.forName(apiClassName);
          } catch (Throwable exception) {
             Log.log_2007(API_CLASS_PROPERTY, apiClassName, exception.getClass().getName(), exception.getMessage());
             setState(API_CONSTRUCTION_FAILED);
             throw new ServletException();
          }
 
          // Check that the loaded API class is derived from the API base class
          if (! API.class.isAssignableFrom(apiClass)) {
             Log.log_2008(API_CLASS_PROPERTY, apiClassName, API.class.getName() + ".class.isAssignableFrom(apiClass) == false");
             setState(API_CONSTRUCTION_FAILED);
             throw new ServletException();
          }
 
          // Get the SINGLETON field and the value of it
          Field singletonField;
          try {
             singletonField = apiClass.getDeclaredField("SINGLETON");
             _api = (API) singletonField.get(null);
          } catch (Throwable exception) {
             Log.log_2008(API_CLASS_PROPERTY, apiClassName, exception.getClass().getName());
             setState(API_CONSTRUCTION_FAILED);
             throw new ServletException();
          }
 
          // Make sure that the field is an instance of that same class
          if (_api == null) {
             Log.log_2008(API_CLASS_PROPERTY, apiClassName, "apiClass.getDeclaredField(\"SINGLETON\").get(null) == null");
             setState(API_CONSTRUCTION_FAILED);
             throw new ServletException();
          } else if (_api.getClass() != apiClass) {
             Log.log_2008(API_CLASS_PROPERTY, apiClassName, "apiClass.getDeclaredField(\"SINGLETON\").get(null).getClass() != apiClass");
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
             Log.log_2009(exception.getPropertyName());
          } catch (InvalidPropertyValueException exception) {
             Log.log_2010(exception.getPropertyName(), exception.getPropertyValue());
          } catch (BootstrapException exception) {
             Log.log_2011(exception.getMessage());
          } catch (Throwable exception) {
             Log.log_2012(exception.getClass().getName(), exception.getMessage());
          } finally {
             if (succeeded == false) {
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
          Log.log_4012(_configFile, CONFIG_RELOAD_INTERVAL_PROPERTY, interval);
          _configFileWatcher.start();
       }
    }
 
    /**
     * Initializes the API using the specified runtime settings.
     *
     * @param runtimeProperties
     *    the runtime settings, guaranteed not to be <code>null</code>.
     */
    private final void initAPI(PropertyReader runtimeProperties) {
 
       setState(INITIALIZING_API);
 
       boolean succeeded = false;
       try {
          _api.init(runtimeProperties);
          succeeded = true;
       } catch (MissingRequiredPropertyException exception) {
          Log.log_4013(exception.getPropertyName());
       } catch (InvalidPropertyValueException exception) {
          Log.log_4014(exception.getPropertyName(), exception.getPropertyValue());
       } catch (InitializationException exception) {
          Log.log_4015(exception.getMessage());
       } catch (Throwable exception) {
          Log.log_4016(exception.getClass().getName(), exception.getMessage());
       } finally {
 
          if (succeeded) {
             setState(READY);
             Log.log_4018();
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
 
       Log.log_3000();
 
       Properties properties = new Properties();
       try {
 
          // Open the file
          FileInputStream in = new FileInputStream(_configFile);
 
          // Load the properties
          properties.load(in);
 
       } catch (FileNotFoundException exception) {
          Log.log_3001(_configFile, exception.getClass().getName(), exception.getMessage());
       } catch (SecurityException exception) {
          Log.log_3002(_configFile, exception.getClass().getName(), exception.getMessage());
       } catch (IOException exception) {
          Log.log_3003(_configFile, exception.getClass().getName(), exception.getMessage());
       }
 
       // TODO: Should we reset the logging subsystem if the Log4J
       // TODO  properties have been removed from the xins.properties file?
       // TODO  Determine the current behaviour and make a decision.
 
 
       // Attempt to configure Log4J
       PropertyConfigurator.configure(properties);
 
       // Determine if Log4J is properly initialized
       Enumeration appenders = LogManager.getLoggerRepository().getRootLogger().getAllAppenders();
       if (appenders instanceof NullEnumeration) {
          Log.log_3004(_configFile);
          configureLoggerFallback();
       } else {
          Log.log_3005();
       }
 
       // Determine the log locale
       String newLocale = properties.getProperty(LOG_LOCALE_PROPERTY);
 
       // If the log locale is set, apply it
       if (newLocale != null) {
          String currentLocale = Log.getTranslationBundle().getName();
          if (currentLocale.equals(newLocale) == false) {
             Log.log_3006(currentLocale, newLocale);
             try {
                Log.setTranslationBundle(newLocale);
                Log.log_3007(currentLocale, newLocale);
             } catch (NoSuchTranslationBundleException exception) {
                Log.log_3008(currentLocale, newLocale);
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
 
       // If there is a diagnostic context ID, then apply it and perform the
       // request...
       boolean haveContextID = (contextID != null) && (contextID.length() > 0);
       if (haveContextID) {
          NDC.push(contextID);
          try {
             doService(request, response);
          } finally {
             NDC.pop();
          }
 
       // ...otherwise just perform the request.
       } else {
          doService(request, response);
       }
 
       // TODO: Document _context somewhere
    }
 
    /**
     * Handles a request to this servlet (implementation method). If any of the
     * arguments is <code>null</code>, then the behaviour of this method is
     * undefined.
     *
     * <p>This method is called from
    * {@link #service(HttpServletRequest,HttpServletResponse}. The latter
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
 
       // Determine the remote IP address
       String ip = request.getRemoteAddr();
 
       // Check the HTTP request method
       String method = request.getMethod();
       boolean sendOutput = "GET".equals(method) || "POST".equals(method);
       if (!sendOutput) {
          if ("OPTIONS".equals(method)) {
             Log.log_5001(ip, method);
             response.setContentLength(0);
             response.setHeader("Accept", "GET, HEAD, POST");
             response.setStatus(HttpServletResponse.SC_OK);
             return;
          } else if ("HEAD".equals(method)) {
             response.setContentLength(0);
 
          // If the method is not recognized, return '405 Method Not Allowed'
          } else {
             Log.log_5000(ip, method);
             response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
             return;
          }
       }
       Log.log_5001(ip, method);
 
       // XXX: Consider using OutputStream instead of Writer, for improved performance
 
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
          response.setContentType("text/xml");
          response.setStatus(HttpServletResponse.SC_OK);
          CallResultOutputter.output(out, result, xslt);
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
 
       Log.log_6000();
 
       // Set the state temporarily to DISPOSING
       setState(DISPOSING);
 
       // Destroy the API
       if (_api != null) {
          try {
             _api.deinit();
          } catch (Throwable exception) {
             Log.log_6001(exception.getClass().getName(), exception.getMessage());
          }
       }
 
       // Set the state to DISPOSED
       setState(DISPOSED);
 
       Log.log_6002();
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * State of an <code>APIServlet</code>.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
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
     * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
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
 
          Log.log_4007(_configFile);
 
          // Apply the new runtime settings to the logging subsystem
          PropertyReader runtimeProperties = readRuntimeProperties();
 
          // Determine the interval
          int newInterval = determineConfigReloadInterval(runtimeProperties);
 
          // Update the file watch interval
          int oldInterval = _configFileWatcher.getInterval();
          if (oldInterval != newInterval) {
             _configFileWatcher.setInterval(newInterval);
             Log.log_4003(_configFile, oldInterval, newInterval);
          }
 
          // Re-initialize the API
          initAPI(runtimeProperties);
       }
 
       public void fileNotFound() {
          Log.log_4000(_configFile);
       }
 
       public void fileNotModified() {
          Log.log_4002(_configFile);
       }
 
       public void securityException(SecurityException exception) {
          Log.log_4001(_configFile, exception.getClass().getName(), exception.getMessage());
       }
    }
 }
