 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.NDC;
 
 import org.apache.oro.text.regex.MalformedPatternException;
 import org.apache.oro.text.regex.Pattern;
 import org.apache.oro.text.regex.Perl5Compiler;
 import org.apache.oro.text.regex.Perl5Matcher;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.Utils;
 import org.xins.common.collections.InvalidPropertyValueException;
 import org.xins.common.collections.MissingRequiredPropertyException;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.manageable.InitializationException;
 import org.xins.common.servlet.ServletConfigPropertyReader;
 import org.xins.common.text.TextUtils;
 import org.xins.logdoc.ExceptionUtils;
 
 /**
  * XINS server engine. An <code>Engine</code> is a delegate of the
  * {@link APIServlet} that is responsible for initialization and request
  * handling.
  *
  * <p>When an <code>Engine</code> instance is constructed, it gathers
  * initialization information from different sources:
  *
  * <dl>
  *    <dt><strong>1. Build-time settings</strong></dt>
  *    <dd>The application package contains a <code>web.xml</code> file with
  *    build-time settings. Some of these settings are required in order for
  *    the XINS/Java Server Framework to start up, while others are optional.
  *    These build-time settings are passed to the servlet by the application
  *    server as a {@link ServletConfig} object. See
  *    {@link APIServlet#init(ServletConfig)}.
  *    <br>The servlet configuration is the responsibility of the
  *    <em>assembler</em>.</dd>
  *
  *    <dt><strong>2. System properties</strong></dt>
  *    <dd>The location of the configuration file must be passed to the Java VM
  *    at startup, as a system property.
  *    <br>System properties are the responsibility of the
  *    <em>system administrator</em>.
  *    <br>Example:
  *    <br><code>java -Dorg.xins.server.config=`pwd`/config/xins.properties
  *    -jar orion.jar</code></dd>
  *
  *    <dt><strong>3. Configuration file</strong></dt>
  *    <dd>The configuration file should contain runtime configuration
  *    settings, like the settings for the logging subsystem.
  *    <br>Runtime properties are the responsibility of the
  *    <em>system administrator</em>.
  *    <br>Example contents for a configuration file:
  *    <blockquote><code>log4j.rootLogger=DEBUG, console
  *    <br>log4j.appender.console=org.apache.log4j.ConsoleAppender
  *    <br>log4j.appender.console.layout=org.apache.log4j.PatternLayout
  *    <br>log4j.appender.console.layout.ConversionPattern=%d %-5p [%c]
  *    %m%n</code></blockquote>
  * </dl>
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  * @author Mees Witteman (<a href="mailto:mees.witteman@nl.wanadoo.com">mees.witteman@nl.wanadoo.com</a>)
  */
 final class Engine extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Perl 5 pattern compiler.
     */
    private static final Perl5Compiler PATTERN_COMPILER = new Perl5Compiler();
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>Engine</code> object.
     *
     * @param config
     *    the {@link ServletConfig} object which contains build properties for
     *    this servlet, as specified by the <em>assembler</em>, cannot be
     *    <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>config == null</code>.
     *
     * @throws ServletException
     *    if the engine could not be constructed.
     */
    Engine(final ServletConfig config)
    throws IllegalArgumentException, ServletException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("config", config);
 
       // Construct the EngineStarter
       _starter = new EngineStarter(config);
 
       // Construct a configuration manager and store the servlet configuration
       _configManager = new ConfigManager(this, config);
       _servletConfig = config;
 
       // Proceed to first actual stage
       _state.setState(EngineState.BOOTSTRAPPING_FRAMEWORK);
 
       // Read configuration details
       _configManager.determineConfigFile();
       _configManager.readRuntimeProperties();
 
       // Log version of XINS/Java Server Framework
       _starter.checkAndLogVersionNumber();
 
       // Construct and bootstrap the API
       _state.setState(EngineState.CONSTRUCTING_API);
       try {
          _api = _starter.constructAPI();
       } catch (ServletException se) {
          _state.setState(EngineState.API_CONSTRUCTION_FAILED);
          throw se;
       }
       bootstrapAPI();
 
       // Construct a generator for diagnostic context IDs
       _contextIDGenerator = new ContextIDGenerator(_api.getName());
       try {
          _contextIDGenerator.bootstrap(new ServletConfigPropertyReader(config));
       } catch (Exception exception) {
         throw EngineStarter.servletExceptionFor(exception);
       }
 
       // Initialize the configuration manager
       _configManager.init();
 
       // Check post-conditions
       if (_api == null) {
          throw Utils.logProgrammingError("_api == null");
       } else if (_apiName == null) {
          throw Utils.logProgrammingError("_apiName == null");
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The state machine for this engine. Never <code>null</code>.
     */
    private final EngineStateMachine _state = new EngineStateMachine();
    
    /**
     * The starter of this engine. Never <code>null</code>.
     */
    private final EngineStarter _starter;
 
    /**
     * The stored servlet configuration object. Never <code>null</code>.
     */
    private final ServletConfig _servletConfig;
 
    /**
     * The API that this engine forwards requests to. Never <code>null</code>.
     */
    private final API _api;
 
    /**
     * Diagnostic context ID generator. Never <code>null</code>.
     */
    private ContextIDGenerator _contextIDGenerator;
 
    /**
     * The name of the API. Never <code>null</code>.
     */
    private String _apiName;
 
    /**
     * The manager for the runtime configuration file. Never <code>null</code>.
     */
    private final ConfigManager _configManager;
 
    /**
     * The manager for the calling conventions. Initially this
     * field is indeed <code>null</code>.
     */
    private CallingConventionManager _conventionManager;
    
    /**
     * Pattern which incoming diagnostic context identifiers must match. Can be
     * <code>null</code> in case no pattern has been specified. Initially this
     * field is indeed <code>null</code>.
     */
    private Pattern _contextIDPattern;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Retrieves the API name.
     *
     * @return
     *    the API name, never <code>null</code>.
     */
    String getAPIName() {
       return _apiName;
    }
 
    /**
     * Determines the filter for diagnostic context identifiers.
     *
     * @param properties
     *    the runtime properties to retrieve information from, cannot be
     *    <code>null</code>.
     *
     * @return
     *    the filter as a {@link Pattern} object, or <code>null</code> if no
     *    filter is specified.
     *
     * @throws IllegalArgumentException
     *    if <code>properties == null</code>.
     *
     * @throws InvalidPropertyValueException
     *    if the value for the filter property is considered invalid.
     */
    private Pattern determineContextIDPattern(PropertyReader properties)
    throws IllegalArgumentException, InvalidPropertyValueException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("properties", properties);
 
       // Determine pattern string
       // TODO: Store "org.xins.server.contextID.filter" in a constant
      final String propName  = "org.xins.server.contextID.filter";
       String propValue = properties.get(propName);
 
       // If the property value is empty, then there is no pattern
       Pattern pattern;
       if (propValue == null || propValue.trim().length() < 1) {
          pattern = null;
          Log.log_3431();
 
       // Otherwise we must provide a Pattern instance
       } else {
 
          // Convert the string to a Pattern
          try {
            final int mask = Perl5Compiler.READ_ONLY_MASK
                           | Perl5Compiler.CASE_INSENSITIVE_MASK;
            pattern = PATTERN_COMPILER.compile(propValue, mask);
             Log.log_3432(propValue);
 
          // Malformed pattern indicates an invalid value
          } catch (MalformedPatternException exception) {
             Log.log_3433(propValue);
             InvalidPropertyValueException ipve;
             ipve = new InvalidPropertyValueException(propName, propValue);
             ExceptionUtils.setCause(ipve, exception);
             throw ipve;
          }
       }
 
       return pattern;
    }
 
    /**
     * Bootstraps the API. The following steps will be performed:
     * <ul>
     *   <li>Determine API name
     *   <li>Load the Logdoc if available
     *   <li>Bootstrap the API itself
     *   <li>Determine the calling convention
     *   <li>Link the engine to the API
     * </ul>
     *
     * @throws ServletException
     *    if bootstrap fails.
     */
    private void bootstrapAPI() throws ServletException {
 
       // Proceed to next stage
       _state.setState(EngineState.BOOTSTRAPPING_API);
 
       try {
          
          // Determine the name of the API
          _apiName = _starter.determineAPIName();
 
          // Load the Logdoc if available
          _starter.loadLogDoc();
 
          // Actually bootstrap the API
          _starter.bootstrap(_api);
 
          // Initialize the calling convention manager
          _conventionManager = new CallingConventionManager(_servletConfig, _api);
       } catch (ServletException se) {
          _state.setState(EngineState.API_BOOTSTRAP_FAILED);
          throw se;
       }
 
       // Make the API have a link to this Engine
       _api.setEngine(this);
    }
 
    /**
     * Initializes the API using the current runtime settings. This method
     * should be called whenever the runtime properties changed.
     *
     * @return
     *    <code>true</code> if the initialization succeeded, otherwise
     *    <code>false</code>.
     */
    boolean initAPI() {
 
       _state.setState(EngineState.INITIALIZING_API);
 
       // Determine the current runtime properties
       PropertyReader properties = _configManager.getRuntimeProperties();
 
       boolean succeeded = false;
 
       boolean localeInitialized = _configManager.determineLogLocale();
       if (!localeInitialized) {
          _state.setState(EngineState.API_INITIALIZATION_FAILED);
          return false;
       }
 
       try {
 
          // Determine filter for incoming diagnostic context IDs
          _contextIDPattern = determineContextIDPattern(properties);
 
          // Initialize the diagnostic context ID generator
          _contextIDGenerator.init(properties);
 
          // Initialize the API
          _api.init(properties);
 
          // Initialize the default calling convention for this API
          _conventionManager.init(properties);
 
          succeeded = true;
 
       // Missing required property
       } catch (MissingRequiredPropertyException exception) {
          Log.log_3411(exception.getPropertyName());
 
       // Invalid property value
       } catch (InvalidPropertyValueException exception) {
          Log.log_3412(exception.getPropertyName(),
                       exception.getPropertyValue(),
                       exception.getReason());
 
       // Initialization of API failed for some other reason
       } catch (InitializationException exception) {
          Log.log_3413(exception.getMessage());
 
       // Unexpected error
       //
       // XXX: According to the documentation of the Manageable class, this
       //      cannot happen.
       } catch (Throwable exception) {
          Log.log_3414(exception);
 
       // Always leave the object in a well-known state
       } finally {
          if (succeeded) {
             _state.setState(EngineState.READY);
             Log.log_3415();
          } else {
             _state.setState(EngineState.API_INITIALIZATION_FAILED);
          }
       }
 
       return succeeded;
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
    void service(final HttpServletRequest  request,
                 final HttpServletResponse response)
    throws IOException {
 
       // Associate the current diagnostic context identifier with this thread
       String contextID = determineContextID(request);
       NDC.push(contextID);
 
       // Handle the request
       try {
          doService(request, response);
 
       // Catch and log all exceptions
       } catch (Throwable exception) {
          Log.log_3003(exception);
 
       // Finally always disassociate the diagnostic context identifier from
       // this thread
       } finally {
          NDC.pop();
          NDC.remove();
       }
    }
 
    /**
     * Returns an applicable diagnostic context identifier. If the request
     * already specifies a diagnostic context identifier, then that will be
     * used. Otherwise a new one will be generated.
     *
     * @param request
     *    the HTTP servlet request, should not be <code>null</code>.
     *
     * @return
     *    the diagnostic context identifier, never <code>null</code> and never
     *    an empty string.
     */
    private String determineContextID(final HttpServletRequest request) {
 
       // See if the request already specifies a diagnostic context identifier
       // TODO: Store "_context" in a constant
       String contextID = request.getParameter("_context");
       if (TextUtils.isEmpty(contextID)) {
          Log.log_3580();
          contextID = null;
 
       // Indeed there is a context ID in the request, make sure it's valid
       } else {
 
          // Valid context ID
          if (isValidContextID(contextID)) {
             Log.log_3581(contextID);
 
          // Invalid context ID
          } else {
             Log.log_3582(contextID);
             contextID = null;
          }
       }
 
       // If we have no (acceptable) context ID yet, then generate one now
       if (contextID == null) {
          // TODO: Support custom format (SF.net RFE #1078846)
          contextID = _contextIDGenerator.generate();
          Log.log_3583(contextID);
       }
 
       return contextID;
    }
 
    /**
     * Determines if the specified incoming context identifier is considered
     * valid.
     *
     * @param contextID
     *    the incoming diagnostic context identifier, should not be
     *    <code>null</code>.
     *
     * @return
     *    <code>true</code> if <code>contextID</code> is considered acceptable,
     *    <code>false</code> if it is considered unacceptable.
     */
    private boolean isValidContextID(String contextID) {
 
       // If a filter is specified, validate that the ID matches it
       if (_contextIDPattern != null) {
          // TODO: Investigate whether this causes a performance bottleneck
          Perl5Matcher matcher = new Perl5Matcher();
          return matcher.matches(contextID, _contextIDPattern);
 
       // No filter is specified, everything is allowed
       } else {
          return true;
       }
    }
 
    /**
     * Handles a request to this servlet (implementation method). If any of the
     * arguments is <code>null</code>, then the behaviour of this method is
     * undefined.
     *
     * <p>This method is called from the corresponding wrapper method,
     * {@link #service(HttpServletRequest,HttpServletResponse)}.
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
 
       // Determine current time
       long start = System.currentTimeMillis();
 
       // Determine the remote IP address and the query string
       String remoteIP    = request.getRemoteAddr();
       String queryString = request.getQueryString();
 
       // TODO: Return 'Server' header
 
       // Check the HTTP request method
       String  method = request.getMethod();
       boolean sendOutput;
 
       // Support HTTP GET
       if ("GET".equals(method)) {
          sendOutput = true;
 
       // Support HTTP POST
       } else if ("POST".equals(method)) {
          sendOutput = true;
 
       // Support HTTP HEAD (returns no output)
       } else if ("HEAD".equals(method)) {
          sendOutput = false;
          response.setContentLength(0);
 
       // Support HTTP OPTIONS
       } else if ("OPTIONS".equals(method)) {
          Log.log_3521(remoteIP, method, request.getRequestURI(), queryString);
          response.setContentLength(0);
          response.setHeader("Accept", "GET, HEAD, POST");
          response.setStatus(HttpServletResponse.SC_OK);
          return;
 
       // Otherwise the HTTP method is unrecognized, so return
       // '405 Method Not Allowed'
       } else {
          Log.log_3520(remoteIP, method, queryString);
          response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
          return;
       }
 
       // Log that we have received an HTTP request
       Log.log_3521(remoteIP, method, request.getRequestURI(), queryString);
 
       // Determine the calling convention; if an existing calling convention
       // is specified in the request, then use that, otherwise use the default
       // calling convention for this engine
       String ccParam = (String) request.getParameter(
          APIServlet.CALLING_CONVENTION_PARAMETER);
       CallingConvention cc = null;
       
       try {
          cc= _conventionManager.getCallingConvention(ccParam);
 
       // The calling convention could not be created or initialized
       } catch (Exception ex) {
 
          Log.log_3560(ex, ccParam);
          // TODO: Is this behaviour described?
          response.sendError(HttpServletResponse.SC_BAD_REQUEST);
          return;
       }
 
       // Call the API if the state is READY
       FunctionResult result;
       EngineState state = _state.getState();
       if (state == EngineState.READY) {
 
          String subjectClass  = cc.getClass().getName();
          String subjectMethod = "convertRequest(javax.servlet.http.HttpServletRequest)";
          try {
 
             // Convert the HTTP request to a XINS request
             FunctionRequest xinsRequest = cc.convertRequest(request);
 
             // Call the function
             subjectClass  = _api.getClass().getName();
             subjectMethod = "handleCall(long,"
                           + FunctionRequest.class.getName()
                           + ",java.lang.String)";
             result = _api.handleCall(start, xinsRequest, remoteIP);
 
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
 
             // Otherwise an unexpected exception is thrown, return
             // '500 Internal Server Error'
             } else {
                error = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
 
                Utils.logProgrammingError(
                   Engine.class.getName(),
                   "doService(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)",
                   subjectClass,
                   subjectMethod,
                   null,
                   exception);
             }
 
             Log.log_3522(exception, error);
 
             response.sendError(error);
             return;
          }
 
       // Otherwise return an appropriate 50x HTTP response code
       } else if (state == EngineState.INITIAL
               || state == EngineState.BOOTSTRAPPING_FRAMEWORK
               || state == EngineState.CONSTRUCTING_API
               || state == EngineState.BOOTSTRAPPING_API
               || state == EngineState.INITIALIZING_API) {
          response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
          return;
       } else {
          response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          return;
       }
 
       // Send the output only if GET or POST; convert it from a XINS function
       // result to an HTTP response
       if (sendOutput) {
          cc.convertResult(result, response, request);
       }
    }
 
    /**
     * Destroys this servlet. A best attempt will be made to release all
     * resources.
     *
     * <p>After this method has finished, it will set the state to
     * <em>disposed</em>. In that state no more requests will be handled.
     */
    void destroy() {
 
       // Log: Shutting down XINS/Java Server Framework
       Log.log_3600();
 
       // Set the state temporarily to DISPOSING
       _state.setState(EngineState.DISPOSING);
 
       // Destroy the configuration manager
       try {
          _configManager.destroy();
       } catch (Throwable t) {
          // ignore
       }
 
       // Destroy the API
       if (_api != null) {
          try {
             _api.deinit();
          } catch (Throwable exception) {
             Log.log_3601(exception);
          }
       }
 
       // Set the state to DISPOSED
       _state.setState(EngineState.DISPOSED);
 
       // Log: Shutdown completed
       Log.log_3602();
    }
 
    /**
     * Returns the config manager.
     *
     * @return
     *    the config manager, never <code>null</code>.
     */
    ConfigManager getConfigManager() {
       return _configManager;
    }
 
    /**
     * Returns the <code>ServletConfig</code> object which contains the
     * build properties for this servlet. The returned {@link ServletConfig}
     * object is the one passed to the constructor.
     *
     * @return
     *    the {@link ServletConfig} object that was used to initialize this
     *    servlet, never <code>null</code>.
     */
    ServletConfig getServletConfig() {
       return _servletConfig;
    }
 }
