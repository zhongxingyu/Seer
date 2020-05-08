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
 
 import org.xins.logdoc.AbstractLog;
 import org.xins.logdoc.LogCentral;
 import org.xins.logdoc.UnsupportedLocaleError;
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
  *    <tr><th>State               </th><th>HTTP response code       </th></tr>
  *
  *    <tr><td>Initial             </td><td>503 Service Unavailable  </td></tr>
  *    <tr><td>Bootstrapping
  *            framework           </td><td>503 Service Unavailable  </td></tr>
  *    <tr><td>Framework bootstrap
  *                          failed</td><td>500 Internal Server Error</td></tr>
  *    <tr><td>Constructing API    </td><td>503 Service Unavailable  </td></tr>
  *    <tr><td>API construction
  *            failed              </td><td>500 Internal Server Error</td></tr>
  *    <tr><td>Bootstrapping API   </td><td>503 Service Unavailable  </td></tr>
  *    <tr><td>API bootstrap failed</td><td>500 Internal Server Error</td></tr>
  *    <tr><td>Initializing API    </td><td>503 Service Unavailable  </td></tr>
  *    <tr><td>API initialization
  *            failed              </td><td>500 Internal Server Error</td></tr>
  *    <tr><td>Disposing           </td><td>500 Internal Server Error</td></tr>
  *    <tr><td>Disposed            </td><td>500 Internal Server Error</td></tr>
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
    // TODO: Add trace logging for all methods
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The fully-qualified name of this class.
     */
    private static final String CLASSNAME = APIServlet.class.getName();
 
    /**
     * The name of the system property that specifies the location of the
     * configuration file.
     */
    public static final String CONFIG_FILE_SYSTEM_PROPERTY =
       Engine.CONFIG_FILE_SYSTEM_PROPERTY;
 
    /**
     * The name of the runtime property that specifies the interval
     * for the configuration file modification checks, in seconds.
     */
    public static final String CONFIG_RELOAD_INTERVAL_PROPERTY =
       Engine.CONFIG_RELOAD_INTERVAL_PROPERTY;
 
    /**
     * The name of the runtime property that hostname for the server
     * running the API.
     */
    public static final String HOSTNAME_PROPERTY =
       Engine.HOSTNAME_PROPERTY;
 
    /**
     * The default configuration file modification check interval, in seconds.
     */
    public static final int DEFAULT_CONFIG_RELOAD_INTERVAL =
       Engine.DEFAULT_CONFIG_RELOAD_INTERVAL;
 
    /**
     * The name of the build property that specifies the name of the
     * API class to load.
     */
    public static final String API_CLASS_PROPERTY =
       Engine.API_CLASS_PROPERTY;
 
    /**
     * The name of the build property that specifies the name of the
     * API.
     */
    public static final String API_NAME_PROPERTY =
       Engine.API_NAME_PROPERTY;
 
    /**
     * The name of the build property that specifies the version with which the
     * API was built.
     */
    public static final String API_BUILD_VERSION_PROPERTY =
       Engine.API_BUILD_VERSION_PROPERTY;
 
    /**
     * The name of the build property that specifies the default calling
     * convention.
     */
    public static final String API_CALLING_CONVENTION_PROPERTY =
       Engine.API_CALLING_CONVENTION_PROPERTY;
 
    /**
     * The name of the build property that specifies the class of the default
     * calling convention.
     */
    public static final String API_CALLING_CONVENTION_CLASS_PROPERTY =
       Engine.API_CALLING_CONVENTION_CLASS_PROPERTY;
 
    /**
     * The parameter of the query to specify the calling convention.
     */
    public static final String CALLING_CONVENTION_PARAMETER =
       Engine.CALLING_CONVENTION_PARAMETER;
 
    /**
     * The standard calling convention.
     */
    public static final String STANDARD_CALLING_CONVENTION =
       Engine.STANDARD_CALLING_CONVENTION;
 
    /**
     * The old style calling convention.
     */
    public static final String OLD_STYLE_CALLING_CONVENTION =
       Engine.OLD_STYLE_CALLING_CONVENTION;
 
    /**
     * The XML calling convention.
     */
    public static final String XML_CALLING_CONVENTION =
       Engine.XML_CALLING_CONVENTION;
 
    /**
     * The XSLT calling convention.
     */
    public static final String XSLT_CALLING_CONVENTION =
       Engine.XSLT_CALLING_CONVENTION;
 
    /**
     * The name of the runtime property that specifies the locale for the log
     * messages.
     *
     * @deprecated
     *    Use {@link LogCentral#LOG_LOCALE_PROPERTY}.
     */
    public static final String LOG_LOCALE_PROPERTY =
       Engine.LOG_LOCALE_PROPERTY;
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Initializes the loggers to log to the console using a simple format
     * and no threshold. This is done by calling
     * {@link Engine#configureLoggerFallback()}.
     */
    static {
       Engine.configureLoggerFallback();
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>APIServlet</code> object.
     */
    public APIServlet() {
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * XINS server engine. Never <code>null</code>.
     */
    private Engine _engine;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
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
     * Initializes this servlet using the specified configuration (wrapper
    * method).
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
 
       // Starting servlet initialization
       Log.log_3000();
 
       try {
 
          // Construct an engine
          _engine = new Engine(config);
 
          // Initialization succeeded
          Log.log_3001();
       } catch (Throwable exception) {
 
          // Initialization failed, log the exception
          Log.log_3002(exception);
 
          // TODO: Make sure the current state is an error state?
 
          // Pass the exception through
          if (exception instanceof ServletException) {
             throw (ServletException) exception;
          } else if (exception instanceof Error) {
             throw (Error) exception;
          } else if (exception instanceof RuntimeException) {
             throw (RuntimeException) exception;
 
          } else {
             // Should in theory never happen, but because of the design of the
             // JVM this cannot be guaranteed
             throw new Error();
          }
       }
    }
 
    /**
     * Initializes the API using the current runtime settings.
     */
    void initAPI() {
       _engine.initAPI();
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
       return (_engine == null) ? null : _engine.getServletConfig();
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
    public void service(HttpServletRequest  request,
                        HttpServletResponse response)
    throws IOException {
       _engine.service(request, response);
    }
 
    /**
     * Re-initialise the properties if the property file has changed.
     */
    void reloadPropertiesIfChanged() {
       _engine.reloadPropertiesIfChanged();
    }
 
    /**
     * Destroys this servlet. A best attempt will be made to release all
     * resources.
     *
     * <p>After this method has finished, it will set the state to
     * <em>disposed</em>. In that state no more requests will be handled.
     */
    public void destroy() {
       _engine.destroy();
    }
 }
