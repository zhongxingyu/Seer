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
 import javax.servlet.Servlet;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.apache.log4j.helpers.NullEnumeration;
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.util.collections.PropertiesPropertyReader;
 import org.xins.util.io.FileWatcher;
 import org.xins.util.servlet.ServletUtils;
 import org.xins.util.text.Replacer;
 
 /**
  * Servlet that forwards requests to an <code>API</code>.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public final class APIServlet
 extends Object
 implements Servlet {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The <em>uninitialized</em> state. See {@link #_state}.
     */
    private static final State UNINITIALIZED = new State("UNINITIALIZED");
 
    /**
     * The <em>initializing</em> state. See {@link #_state}.
     */
    private static final State INITIALIZING = new State("INITIALIZING");
 
    /**
     * The <em>ready</em> state. See {@link #_state}.
     */
    private static final State READY = new State("READY");
 
    /**
     * The <em>disposing</em> state. See {@link #_state}.
     */
    private static final State DISPOSING = new State("DISPOSING");
 
    /**
     * The <em>disposed</em> state. See {@link #_state}.
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
     * The name of the initialization property that specifies the name of the
     * API class to load.
     */
    public static final String API_CLASS_PROPERTY = "org.xins.api.class";
 
    /**
     * The name of the configuration property that specifies the interval
     * for the configuration file modification checks, in seconds.
     */
    public static final String CONFIG_RELOAD_INTERVAL_PROPERTY = "org.xins.server.config.reload";
 
    /**
     * The default configuration file modification check interval, in seconds.
     */
    public static final int DEFAULT_CONFIG_RELOAD_INTERVAL = 60;
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Loads an API instance based on the specified servlet configuration.
     *
     * @param config
     *    the servlet configuration, cannot be <code>null</code>.
     *
     * @return
     *    the constructed {@link API} instance, never <code>null</code>.
     *
     * @throws ServletException
     *    if an API instance could not be initialized.
     */
    private static API loadAPI(ServletConfig config)
    throws ServletException { 
 
       API api;
 
       // Determine the API class
       String apiClassName = config.getInitParameter(API_CLASS_PROPERTY);
       if (apiClassName == null || apiClassName.trim().length() < 1) {
          String message = "Invalid application package. API class name not set in initialization parameter \"" + API_CLASS_PROPERTY + "\".";
          Library.STARTUP_LOG.fatal(message);
          throw new ServletException(message);
       }
 
       // Load the API class
       Class apiClass;
       try {
          apiClass = Class.forName(apiClassName);
       } catch (Exception e) {
          String message = "Invalid application package. Failed to load API class set in initialization parameter \"" + API_CLASS_PROPERTY + "\": \"" + apiClassName + "\".";
          Library.STARTUP_LOG.fatal(message, e);
          throw new ServletException(message);
       }
 
       // Check that the loaded API class is derived from the API base class
       if (! API.class.isAssignableFrom(apiClass)) {
          String message = "Invalid application package. The \"" + apiClassName + "\" is not derived from class " + API.class.getName() + '.';
          Library.STARTUP_LOG.fatal(message);
          throw new ServletException(message);
       }
 
       // Get the SINGLETON field
       Field singletonField;
       try {
          singletonField = apiClass.getDeclaredField("SINGLETON");
       } catch (Exception e) {
          String message = "Invalid application package. Failed to lookup class field SINGLETON in API class \"" + apiClassName + "\".";
          Library.STARTUP_LOG.fatal(message, e);
          throw new ServletException(message);
       }
 
       // Get the value of the SINGLETON field
       try {
          api = (API) singletonField.get(null);
       } catch (Exception e) {
          String message = "Invalid application package. Failed to get value of the SINGLETON field of API class \"" + apiClassName + "\".";
          Library.STARTUP_LOG.fatal(message, e);
          throw new ServletException(message);
       }
 
       // Make sure that the field is an instance of that same class
       if (api == null) {
          String message = "Invalid application package. The value of the SINGLETON field of API class \"" + apiClassName + "\" is null.";
          Library.STARTUP_LOG.fatal(message);
          throw new ServletException(message);
       } else if (api.getClass() != apiClass) {
          String message = "Invalid application package. The value of the SINGLETON field of API class \"" + apiClassName + "\" is not an instance of that class.";
          Library.STARTUP_LOG.fatal(message);
          throw new ServletException(message);
       }
 
       // Get the name of the API
       String apiName = api.getName();
       Library.STARTUP_LOG.debug("Loaded \"" + apiName + "\" API.");
 
       return api;
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>APIServlet</code> object.
     */
    public APIServlet() {
       _stateLock = new Object();
       _state     = UNINITIALIZED;
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
    private Object _stateLock;
 
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
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
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
     * <p>Note that if a {@link ServletException} is thrown during the
     * initialization, a <em>fatal</em> message is logged and the state is
     * reset to <em>uninitialized</em>.
     *
     * @param config
     *    the {@link ServletConfig} object which contains initialization and
     *    startup parameters for this servlet, as specified by the
     *    <em>assembler</em>, cannot be <code>null</code>.
     *
     * @throws ServletException
     *    if <code>config == null</code>, if this servlet is not uninitialized
     *    or if the initialization failed for some other reason.
     */
    public void init(ServletConfig config)
    throws ServletException {
 
       // Make sure the Library class is initialized
       String version = Library.getVersion();
 
       // Hold the state lock
       synchronized (_stateLock) {
 
          // Check preconditions
          if (_state != UNINITIALIZED) {
             String message = "Application server malfunction detected. State is " + _state + " instead of " + UNINITIALIZED + '.';
             Library.STARTUP_LOG.fatal(message);
             throw new ServletException(message);
          } else if (config == null) {
             String message = "Application server malfunction detected. No servlet configuration object passed.";
             Library.STARTUP_LOG.fatal(message);
             throw new ServletException(message);
          }
 
          // Get the ServletContext
          ServletContext context = config.getServletContext();
          if (context == null) {
             String message = "Application server malfunction detected. No servlet context available.";
             Library.STARTUP_LOG.fatal(message);
             throw new ServletException(message);
          }
 
          // Check the expected vs implemented Java Servlet API version
          int major = context.getMajorVersion();
          int minor = context.getMinorVersion();
          if (major != EXPECTED_SERVLET_VERSION_MAJOR || minor != EXPECTED_SERVLET_VERSION_MINOR) {
             Library.STARTUP_LOG.warn("Application server implements Java Servlet API version " + major + '.' + minor + " instead of the expected version " + EXPECTED_SERVLET_VERSION_MAJOR + '.' + EXPECTED_SERVLET_VERSION_MINOR + ". The application may or may not work correctly.");
          }
 
          // Set the state
          _state = INITIALIZING;
 
          try {
             // Determine configuration file location
             _configFile = System.getProperty(CONFIG_FILE_SYSTEM_PROPERTY);
 
             // Read properties from the config file
             Properties runtimeProperties = null;
             if (_configFile == null || _configFile.length() < 1) {
                Library.STARTUP_LOG.error("System administration issue detected. System property \"" + CONFIG_FILE_SYSTEM_PROPERTY + "\" is not set.");
             } else {
                runtimeProperties = applyConfigFile(Library.STARTUP_LOG);
             }
 
             // Initialization starting
             Library.STARTUP_LOG.debug("XINS/Java Server Framework " + version + " is initializing.");
 
             // Load the API instance
             _api = loadAPI(config);
             String apiName = _api.getName();
 
             // Initialize the API
             Library.STARTUP_LOG.debug("Initializing \"" + apiName + "\" API.");
             try {
                // TODO: Use ServletConfigPropertyReader
                _api.init(new PropertiesPropertyReader(ServletUtils.settingsAsProperties(config)),
                          new PropertiesPropertyReader(runtimeProperties));
 
                Library.STARTUP_LOG.debug("Initialized \"" + apiName + "\" API.");
             } catch (Throwable e) {
                String message = "Failed to initialize \"" + apiName + "\" API.";
                Library.STARTUP_LOG.error(message, e);
             }
 
             // Watch the configuration file
             if (_configFile != null) {
                FileWatcher.Listener listener = new ConfigurationFileListener();
                int interval = 10; // TODO: Read from config file
                FileWatcher watcher = new FileWatcher(_configFile, interval, listener);
                watcher.start();
                Library.STARTUP_LOG.info("Using config file \"" + _configFile + "\". Checking for changes every " + interval + " seconds.");
             }
 
             // Initialization done
             Library.STARTUP_LOG.info("XINS/Java Server Framework " + version + " is initialized.");
 
             // Finally enter the ready state
             _state = READY;
 
             // Store the ServletConfig object, per the Servlet API Spec, see:
             // http://java.sun.com/products/servlet/2.3/javadoc/javax/servlet/Servlet.html#getServletConfig()
             _servletConfig = config;
 
          // If an exception is thrown, then reset the state
          } finally {
             if (_state != READY) {
                _state = UNINITIALIZED;
             }
          }
       }
    }
 
    /**
     * Reads the configuration file and applies the settings in it. If this
     * fails, then an error is logged on the specified logger. Still, a
     * {@link Properties} object is always returned.
     *
     * @param log
     *    the logger to log messages to, should not be <code>null</code>.
     *
     * @return
     *    the properties read from the config file, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>log = null</code>.
     */
    private Properties applyConfigFile(Logger log)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("log", log);
 
       Properties properties = new Properties();
 
       try {
          FileInputStream in = new FileInputStream(_configFile);
          properties.load(in);
 
          Library.configure(log, properties);
       } catch (FileNotFoundException exception) {
          log.error("System administration issue detected. Configuration file \"" + _configFile + "\" cannot be opened.");
       } catch (SecurityException exception) {
          log.error("System administration issue detected. Access denied while loading configuration file \"" + _configFile + "\".");
       } catch (IOException exception) {
          log.error("System administration issue detected. Unable to read configuration file \"" + _configFile + "\".");
       }
 
       return properties;
    }
 
    /**
     * Returns the <code>ServletConfig</code> object which contains the
     * initialization and startup parameters for this servlet. The returned
     * {@link ServletConfig} object is the one passed to the
     * {@link #init(ServletConfig)} method. 
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
     * Handles a request to this servlet.
     *
     * @param request
     *    the servlet request, should not be <code>null</code>.
     *
     * @param response
     *    the servlet response, should not be <code>null</code>.
     *
     * @throws ServletException
     *    if the state of this servlet is not <em>ready</em>, or
     *    if <code>request == null || response == null</code>.
     *
     * @throws IOException
     *    if there is an error error writing to the response output stream.
     */
    public void service(ServletRequest request, ServletResponse response)
    throws ServletException, IOException {
 
       // Determine current time
       long start = System.currentTimeMillis();
 
       // Check state
       if (_state != READY) {
          String message = "Application server malfunction detected. State is " + _state + " instead of " + READY + '.';
          Library.RUNTIME_LOG.error(message);
          throw new ServletException(message);
       }
 
       // Check arguments
       if (request == null || response == null) {
          String message = "Application server malfunction detected. ";
          if (request == null && response == null) {
             message += "Both request and response are null.";
          } else if (request == null) {
             message += "Request is null.";
          } else {
             message += "Response is null.";
          }
          Library.RUNTIME_LOG.error(message);
          throw new ServletException(message);
       }
 
       // TODO: Support and use OutputStream instead of Writer, for improved
       //       performance
 
       // Call the API
       CallResult result = _api.handleCall(start, request);
 
       // Determine the XSLT to link to
       String xslt = request.getParameter("_xslt");
 
       // Send the XML output to the stream and flush
       PrintWriter out = response.getWriter(); 
       response.setContentType("text/xml");
       CallResultOutputter.output(out, result, xslt);
       out.flush();
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
     * Destroys this servlet. A best attempt will be made to release all
     * resources.
     *
     * <p>After this method has finished, it will set the state to
     * <em>disposed</em>. In that state no more requests will be handled.
     */
    public void destroy() {
 
       Library.SHUTDOWN_LOG.debug("Shutting down XINS/Java Server Framework.");
 
       synchronized (_stateLock) {
          // Set the state temporarily to DISPOSING
          _state = DISPOSING;
 
          // Destroy the API
          if (_api != null) {
             _api.destroy();
          }
 
          // Set the state to DISPOSED
          _state = DISPOSED;
       }
 
       Library.SHUTDOWN_LOG.info("XINS/Java Server Framework shutdown completed.");
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
          Library.REINIT_LOG.info("Configuration file \"" + _configFile + "\" changed. Re-initializing XINS/Java Server Framework.");
         applyConfigFile(REINIT_LOG);
          // TODO: reinit API
          Library.REINIT_LOG.info("XINS/Java Server Framework re-initialized.");
       }
 
       public void fileNotFound() {
          Library.REINIT_LOG.error("System administration issue detected. Configuration file \"" + _configFile + "\" cannot be opened.");
       }
 
       public void fileNotModified() {
          Library.REINIT_LOG.debug("Configuration file \"" + _configFile + "\" is not modified.");
       }
 
       public void securityException(SecurityException exception) {
          Library.REINIT_LOG.error("System administration issue detected. Access denied while reading file \"" + _configFile + "\".");
       }
    }
 }
