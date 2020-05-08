 /*
  * $Id$
  *
  * Copyright 2003-2006 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import java.io.IOException;
 import java.util.Iterator;
 
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
 import org.xins.common.text.TextUtils;
 import org.xins.logdoc.ExceptionUtils;
 import org.xins.logdoc.LogCentral;
 
 /**
  * XINS server engine. The engine is a delegate of the {@link APIServlet} that
  * is responsible for initialization and request handling.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:ernst.dehaan@orange-ft.com">Ernst de Haan</a>
  * @author <a href="mailto:anthony.goubard@orange-ft.com">Anthony Goubard</a>
  * @author <a href="mailto:mees.witteman@orange-ft.com">Mees Witteman</a>
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
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>Engine</code> object.
     *
     * @param config
     *    the {@link ServletConfig} object which contains build-time properties
     *    for this servlet, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>config == null</code>.
     *
     * @throws ServletException
     *    if the engine could not be constructed.
     */
    Engine(ServletConfig config)
    throws IllegalArgumentException, ServletException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("config", config);
 
       // Construct the EngineStarter
       _starter = new EngineStarter(config);
 
       // Construct a configuration manager and store the servlet configuration
       _configManager = new ConfigManager(this, config);
       _servletConfig = config;
 
       // Proceed to first actual stage
       _stateMachine.setState(EngineState.BOOTSTRAPPING_FRAMEWORK);
 
       // Read configuration details
       _configManager.determineConfigFile();
       _configManager.readRuntimeProperties();
 
       // Log boot messages
       _starter.logBootMessages();
 
       // Construct and bootstrap the API
       _stateMachine.setState(EngineState.CONSTRUCTING_API);
       try {
          _api = _starter.constructAPI();
       } catch (ServletException se) {
          _stateMachine.setState(EngineState.API_CONSTRUCTION_FAILED);
          throw se;
       }
       boolean bootstrapped = bootstrapAPI();
       if (!bootstrapped) {
          throw new ServletException(); // XXX
       }
 
       // Done bootstrapping the framework
       Log.log_3225(Library.getVersion());
 
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
    private final EngineStateMachine _stateMachine = new EngineStateMachine();
 
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
     * The manager for the calling conventions. This field can be and initially
     * is <code>null</code>. This field is initialized by
     * {@link #bootstrapAPI()}.
     */
    private CallingConventionManager _conventionManager;
 
    /**
     * Pattern which incoming diagnostic context identifiers must match. Can be
     * <code>null</code> in case no pattern has been specified. Initially this
     * field is indeed <code>null</code>.
     */
    private Pattern _contextIDPattern;
 
    /**
     * The set of supported HTTP methods, as a comma-separated string. Is
     * initialized by {@link #initAPI()}.
     */
    private String _supportedMethodsString;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Bootstraps the API. The following steps will be performed:
     *
     * <ul>
     *   <li>determine the API name;
     *   <li>load the Logdoc, if available;
     *   <li>bootstrap the API;
     *   <li>construct and bootstrap the calling conventions;
     *   <li>link the engine to the API;
     *   <li>construct and bootstrap a context ID generator;
     *   <li>perform JMX initialization.
     * </ul>
     *
     * @return
     *    <code>true</code> if the bootstrapping of the API succeeded,
     *    <code>false</code> if it failed.
     */
    private boolean bootstrapAPI() {
 
       // Proceed to next stage
       _stateMachine.setState(EngineState.BOOTSTRAPPING_API);
 
       PropertyReader bootProps;
       try {
 
          // Determine the name of the API
          _apiName = _starter.determineAPIName();
 
          // Load the Logdoc if available
          _starter.loadLogdoc();
 
          // Actually bootstrap the API
          bootProps = _starter.bootstrap(_api);
 
       // Handle any failures
       } catch (ServletException se) {
          _stateMachine.setState(EngineState.API_BOOTSTRAP_FAILED);
          return false;
       }
 
       // Create the calling convention manager
       _conventionManager = new CallingConventionManager(_api);
 
       // Bootstrap the calling convention manager
       try {
          _conventionManager.bootstrap(bootProps);
 
       // Missing required property
       } catch (MissingRequiredPropertyException exception) {
          Log.log_3209(exception.getPropertyName(), exception.getDetail());
          return false;
 
       // Invalid property value
       } catch (InvalidPropertyValueException exception) {
          Log.log_3210(exception.getPropertyName(),
                       exception.getPropertyValue(),
                       exception.getReason());
          return false;
 
       // Other bootstrap error
       } catch (Throwable exception) {
          Log.log_3211(exception);
          return false;
       }
 
       // Make the API have a link to this Engine
       _api.setEngine(this);
 
       // Construct a generator for diagnostic context IDs
       _contextIDGenerator = new ContextIDGenerator(_api.getName());
       try {
          _contextIDGenerator.bootstrap(bootProps);
       } catch (Exception exception) {
         return false;
       }
 
       // Perform JMX initialization
       _starter.registerMBean(_api);
 
       // Succeeded
       return true;
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
 
       _stateMachine.setState(EngineState.INITIALIZING_API);
 
       // Determine the current runtime properties
       PropertyReader properties = _configManager.getRuntimeProperties();
 
       boolean succeeded = false;
 
       // Determine the locale for logging
       boolean localeInitialized = _configManager.determineLogLocale();
       if (! localeInitialized) {
          _stateMachine.setState(EngineState.API_INITIALIZATION_FAILED);
          return false;
       }
 
       // Determine at what level should the stack traces be displayed
       String stackTraceAtMessageLevel = properties.get(LogCentral.LOG_STACK_TRACE_AT_MESSAGE_LEVEL);
       if ("true".equals(stackTraceAtMessageLevel)) {
           LogCentral.setStackTraceAtMessageLevel(true);
       } else if ("false".equals(stackTraceAtMessageLevel)) {
           LogCentral.setStackTraceAtMessageLevel(false);
       } else if (stackTraceAtMessageLevel != null) {
          // XXX: Report this error in some way
          _stateMachine.setState(EngineState.API_INITIALIZATION_FAILED);
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
          Log.log_3411(exception.getPropertyName(), exception.getDetail());
 
       // Invalid property value
       } catch (InvalidPropertyValueException exception) {
          Log.log_3412(exception.getPropertyName(),
                       exception.getPropertyValue(),
                       exception.getReason());
 
       // Initialization of API failed for some other reason
       } catch (InitializationException exception) {
          Log.log_3413(exception);
 
       // Other error
       } catch (Throwable exception) {
          Log.log_3414(exception);
 
       // Always leave the object in a well-known state
       } finally {
          if (succeeded) {
             _stateMachine.setState(EngineState.READY);
          } else {
             _stateMachine.setState(EngineState.API_INITIALIZATION_FAILED);
          }
       }
 
       return succeeded;
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
       // XXX: Store "org.xins.server.contextID.filter" in a constant
       String propName  = "org.xins.server.contextID.filter";
       String propValue = properties.get(propName);
 
       // If the property value is empty, then there is no pattern
       Pattern pattern;
       if (TextUtils.isEmpty(propValue)) {
          pattern = null;
          Log.log_3431();
 
       // Otherwise we must provide a Pattern instance
       } else {
 
          // Convert the string to a Pattern
          try {
             // XXX: Why is the pattern made case-insensitive?
             int mask = Perl5Compiler.READ_ONLY_MASK
                      | Perl5Compiler.CASE_INSENSITIVE_MASK;
             pattern  = PATTERN_COMPILER.compile(propValue, mask);
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
     *    in case of an I/O error.
     */
    void service(HttpServletRequest  request,
                 HttpServletResponse response)
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
    private String determineContextID(HttpServletRequest request) {
 
       // See if the request already specifies a diagnostic context identifier
       // XXX: Store "_context" in a constant
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
     *    in case of an I/O error.
     */
    private void doService(HttpServletRequest  request,
                           HttpServletResponse response)
    throws IOException {
 
       // Determine current time
       long start = System.currentTimeMillis();
 
       // Log that we have received an HTTP request
       String remoteIP    = request.getRemoteAddr();
       String method      = request.getMethod().toUpperCase();
       String path        = request.getRequestURI();
       String queryString = request.getQueryString();
       Log.log_3521(remoteIP, method, path, queryString);
 
       // If the current state is not usable, then return an error immediately
       EngineState state = _stateMachine.getState();
       if (! state.allowsInvocations()) {
          handleUnusableState(state, request, response);
 
       // Support the HTTP method "OPTIONS"
       } else if ("OPTIONS".equals(method)) {
         if ("*".equals(queryString)) {
             handleOptionsForAll(response);
          } else {
             delegateToCC(start, request, response);
          }
 
       // Fail if the method is not supported by any calling convention
       } else if (! _conventionManager.getSupportedMethods().contains(method)) {
          handleUnknownMethod(request, response);
 
       // The request should be handled by a calling convention
       } else {
          delegateToCC(start, request, response);
       }
    }
 
    /**
     * Handles an unprocessable request (low-level function). The response is
     * filled for the request.
     *
     * @param request
     *    the HTTP request, cannot be <code>null</code>.
     *
     * @param response
     *    the HTTP request, cannot be <code>null</code>.
     *
     * @param statusCode
     *    the HTTP status code to return.
     *
     * @param reason
     *    explanation, can be <code>null</code>.
     *
     * @throws IOException
     *    in case of an I/O error.
     */
    private void handleUnprocessableRequest(HttpServletRequest  request,
                                            HttpServletResponse response,
                                            int                 statusCode,
                                            String              reason)
    throws IOException {
 
       // Log
       Log.log_3523(request.getRemoteAddr(),
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getQueryString(),
                    statusCode,
                    reason);
 
       // Send the HTTP status code to the client
       response.sendError(statusCode);
    }
 
    /**
     * Handles a request that comes in while function invocations are currently
     * not allowed.
     *
     * @param state
     *    the current state, cannot be <code>null</code>.
     *
     * @param request
     *    the HTTP request, cannot be <code>null</code>.
     *
     * @param response
     *    the HTTP response to fill, cannot be <code>null</code>.
     *
     * @throws IOException
     *    in case of an I/O error.
     */
    private void handleUnusableState(EngineState         state,
                                     HttpServletRequest  request,
                                     HttpServletResponse response)
    throws IOException {
 
       // Log and respond
       int statusCode = state.isError()
                      ? HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                      : HttpServletResponse.SC_SERVICE_UNAVAILABLE;
       String reason  = "XINS/Java Server Framework engine state \""
                      + state
                      + "\" does not allow incoming requests.";
       handleUnprocessableRequest(request, response, statusCode, reason);
    }
 
    /**
     * Handles an incoming request that specifies an HTTP method that not
     * supported by any of the calling conventions.
     *
     * @param request
     *    the servlet request, should not be <code>null</code>.
     *
     * @param response
     *    the servlet response, should not be <code>null</code>.
     *
     * @throws IOException
     *    in case of an I/O error.
     */
    private void handleUnknownMethod(HttpServletRequest  request,
                                     HttpServletResponse response)
    throws IOException {
 
       // Log and respond
       int statusCode = HttpServletResponse.SC_NOT_IMPLEMENTED;
       String reason = "The HTTP method \""
                     + request.getMethod()
                     + "\" is not known by any of the usable calling "
                     + "conventions.";
       handleUnprocessableRequest(request, response, statusCode, reason);
    }
 
    /**
     * Delegates the specified incoming request to the appropriate
     * <code>CallingConvention</code>. The request may either be a function
     * invocation or an <em>OPTIONS</em> request.
     *
     * @param start
     *    timestamp indicating when the call was received by the framework, in
     *    milliseconds since the
     *    <a href="http://en.wikipedia.org/wiki/Unix_Epoch">UNIX Epoch</a>.
     *
     * @param request
     *    the servlet request, should not be <code>null</code>.
     *
     * @param response
     *    the servlet response, should not be <code>null</code>.
     *
     * @throws IOException
     *    in case of an I/O error.
     */
    private void delegateToCC(long                start,
                              HttpServletRequest  request,
                              HttpServletResponse response)
    throws IOException {
 
       // Determine the calling convention to use
       CallingConvention cc = determineCC(request, response);
 
       // If it is null, then there was an error. This error will have been
       // handled completely, including logging and response output.
       if (cc != null) {
 
          // Handle OPTIONS calls separately
          String method = request.getMethod().toUpperCase();
          if ("OPTIONS".equals(method)) {
             handleOptions(cc, request, response);
 
          // Non-OPTIONS requests are function invocations
          } else {
             invokeFunction(start, cc, request, response);
          }
       }
    }
 
    /**
     * Determines which calling convention should be used for the specified
     * request. In case of an error, an error response will be produced and
     * sent to the client.
     *
     * @param request
     *    the HTTP request for which to determine the calling convention to use
     *    cannot be <code>null</code>.
     *
     * @param response
     *    the HTTP response, cannot be <code>null</code>.
     *
     * @return
     *    the {@link CallingConvention} to use, or <code>null</code> if the
     *    calling convention to use could not be determined.
     *
     * @throws IOException
     *    in case of an I/O error.
     */
    private final CallingConvention determineCC(HttpServletRequest  request,
                                                HttpServletResponse response)
    throws IOException {
 
       // Determine the calling convention; if an existing calling convention
       // is specified in the request, then use that, otherwise use the default
       // calling convention for this engine
       CallingConvention cc = null;
       try {
          cc = _conventionManager.getCallingConvention(request);
 
       // Only an InvalidRequestException is expected. If a different kind of
       // exception is received, then that is considered a programming error.
       } catch (Throwable exception) {
          int    statusCode;
          String reason;
          if (exception instanceof InvalidRequestException) {
             statusCode = HttpServletResponse.SC_BAD_REQUEST;
             reason     = "Unable to activate appropriate calling convention";
             String exceptionMessage = exception.getMessage();
             if (TextUtils.isEmpty(exceptionMessage)) {
                reason += '.';
             } else {
                reason += ": " + exceptionMessage;
             }
          } else {
             statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
             reason     = "Internal error while trying to determine "
                        + "appropriate calling convention.";
 
             Utils.logProgrammingError(
                Engine.class.getName(),
                "determineCC(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)",
                _conventionManager.getClass().getName(),
                "getCallingConvention(javax.servlet.http.HttpServletRequest)",
                null,
                exception);
          }
 
          // Log and respond
          handleUnprocessableRequest(request, response, statusCode, reason);
       }
 
       return cc;
    }
 
    /**
     * Invokes a function, using the specified calling convention to from an
     * HTTP request and to an HTTP response.
     *
     * @param start
     *    timestamp indicating when the call was received by the framework, in
     *    milliseconds since the
     *    <a href="http://en.wikipedia.org/wiki/Unix_Epoch">UNIX Epoch</a>.
     *
     * @param cc
     *    the calling convention to use, cannot be <code>null</code>.
     *
     * @param request
     *    the HTTP request, cannot be <code>null</code>.
     *
     * @param response
     *    the HTTP response, cannot be <code>null</code>.
     *
     * @throws IOException
     *    in case of an I/O error.
     */
    private void invokeFunction(long                start,
                                CallingConvention   cc,
                                HttpServletRequest  request,
                                HttpServletResponse response)
    throws IOException {
 
       // Convert the HTTP request to a XINS request
       FunctionRequest xinsRequest;
       try {
          xinsRequest = cc.convertRequest(request);
 
       // Only an InvalidRequestException or a FunctionNotSpecifiedException is
       // expected. If a different kind of exception is received, then that is
       // considered a programming error.
       } catch (Throwable exception) {
 
          int    statusCode;
          String reason;
 
          // Unsupported HTTP method
          if (exception instanceof UnsupportedMethodException) {
             statusCode = HttpServletResponse.SC_METHOD_NOT_ALLOWED;
             reason     = "Calling convention \""
                        + cc.getClass().getName()
                        + "\" cannot process the request";
             String exceptionMessage = exception.getMessage();
             if (! TextUtils.isEmpty(exceptionMessage)) {
                reason += ": " + exceptionMessage;
             } else {
                reason += '.';
             }
          } else if (exception instanceof InvalidRequestException) {
             statusCode = HttpServletResponse.SC_BAD_REQUEST;
             reason     = "Calling convention \""
                        + cc.getClass().getName()
                        + "\" cannot process the request";
             String exceptionMessage = exception.getMessage();
             if (! TextUtils.isEmpty(exceptionMessage)) {
                reason += ": " + exceptionMessage;
             } else {
                reason += '.';
             }
          } else if (exception instanceof FunctionNotSpecifiedException) {
             statusCode = HttpServletResponse.SC_NOT_FOUND;
             reason     = "Cannot determine which function to invoke.";
          } else {
             statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
             reason     = "Internal error.";
 
             Utils.logProgrammingError(
                Engine.class.getName(),
                "invokeFunction(long,org.xins.server.CallingConvention,javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)",
                cc.getClass().getName(),
                "convertRequest(javax.servlet.http.HttpServletRequest)",
                null,
                exception);
          }
 
          // Log and respond
          handleUnprocessableRequest(request, response, statusCode, reason);
 
          return;
       }
 
       // Call the function
       FunctionResult result;
       try {
          result = _api.handleCall(start, xinsRequest, request.getRemoteAddr());
 
       // The only expected exceptions are NoSuchFunctionException and
       // AccessDeniedException. Other exceptions are considered to indicate
       // a programming error.
       } catch (Throwable exception) {
 
          int    statusCode;
          String reason;
 
          // Access denied
          if (exception instanceof AccessDeniedException) {
             statusCode = HttpServletResponse.SC_FORBIDDEN;
             reason     = "Access is denied.";
 
          // No such function
          } else if (exception instanceof NoSuchFunctionException) {
             statusCode = HttpServletResponse.SC_NOT_FOUND;
             reason     = "The specified function \""
                        + xinsRequest.getFunctionName()
                        + "\" is unknown.";
 
          // Internal error
          } else {
             statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
             reason     = "Internal error while processing function call.";
 
             Utils.logProgrammingError(
                Engine.class.getName(),
                "invokeFunction(long,org.xins.server.CallingConvention,javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)",
                _api.getClass().getName(),
                "handleCall(long,org.xins.server.FunctionRequest,java.lang.String)",
                null,
                exception);
          }
 
          // Log and respond
          handleUnprocessableRequest(request, response, statusCode, reason);
 
          return;
       }
 
 
       // Convert the XINS result to an HTTP response
       try {
          cc.convertResult(result, response, request);
 
       // NOTE: If the convertResult method throws an exception, then it
       //       will have been logged within the CallingConvention class
       //       already.
       } catch (Throwable exception) {
          response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          return;
       }
    }
 
    /**
     * Handles an <em>OPTIONS</em> request for the resource <code>*</code>.
     *
     * @param response
     *    the response to fill, never <code>null</code>.
     *
     * @throws IOException
     *    in case of an I/O error.
     */
    private void handleOptionsForAll(HttpServletResponse response)
    throws IOException {
 
       // Create the string with the supported HTTP methods
       Iterator it = _conventionManager.getSupportedMethods().iterator();
       String string = "OPTIONS";
       while (it.hasNext()) {
          String next = (String) it.next();
          string += ", " + next.toUpperCase();
       }
 
       // Return the full response
       response.setStatus(HttpServletResponse.SC_OK);
       response.setHeader("Accept", string);
       response.setContentLength(0);
    }
 
    /**
     * Handles an <em>OPTIONS</em> request for a specific calling convention.
     *
     * @param cc
     *    the calling convention, never <code>null</code>.
     *
     * @param response
     *    the response to fill, never <code>null</code>.
     *
     * @throws IOException
     *    in case of an I/O error.
     */
    private void handleOptions(CallingConvention   cc,
                               HttpServletRequest  request,
                               HttpServletResponse response)
    throws IOException {
 
       // Create the string with the supported HTTP methods
       String[] methods = cc.getSupportedMethods(request);
       String string = "OPTIONS";
       for (int i = 0; i < methods.length; i++) {
          string += ", " + methods[i].toUpperCase();
       }
 
       // Return the full response
       response.setStatus(HttpServletResponse.SC_OK);
       response.setHeader("Accept", string);
       response.setContentLength(0);
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
       _stateMachine.setState(EngineState.DISPOSING);
 
       // Destroy the configuration manager
       if (_configManager != null) {
          try {
             _configManager.destroy();
          } catch (Throwable exception) {
             Utils.logIgnoredException(
                Engine.class.getName(),              "destroy()",
                _configManager.getClass().getName(), "destroy()",
                exception);
          }
       }
 
       // Destroy the API
       if (_api != null) {
          try {
             _api.deinit();
          } catch (Throwable exception) {
             Utils.logIgnoredException(
                Engine.class.getName(),    "destroy()",
                _api.getClass().getName(), "deinit()",
                exception);
          }
       }
 
       // Set the state to DISPOSED
       _stateMachine.setState(EngineState.DISPOSED);
 
       // Log: Shutdown completed
       Log.log_3602();
    }
 
    /**
     * Re-initializes the configuration file listener if there is no file
     * watcher; otherwise interrupts the file watcher.
     */
    void reloadPropertiesIfChanged() {
       _configManager.reloadPropertiesIfChanged();
    }
 
    /**
     * Returns the <code>ServletConfig</code> object which contains the
     * build-time properties for this servlet. The returned
     * {@link ServletConfig} object is the one that was passed to the
     * constructor.
     *
     * @return
     *    the {@link ServletConfig} object that was used to initialize this
     *    servlet, never <code>null</code>.
     */
    ServletConfig getServletConfig() {
       return _servletConfig;
    }
 }
