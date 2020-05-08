 /*
  * $Id$
  */
 package org.xins.client;
 
 import java.io.IOException;
 import java.net.ConnectException;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpConnection;
 import org.apache.commons.httpclient.HttpRecoverableException;
 import org.apache.commons.httpclient.methods.PostMethod;
 
 import org.apache.log4j.NDC;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.TimeOutException;
 
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.collections.PropertyReaderUtils;
 
 import org.xins.common.service.CallFailedException;
 import org.xins.common.service.Descriptor;
 import org.xins.common.service.ServiceCaller;
 import org.xins.common.service.TargetDescriptor;
 
 import org.xins.common.text.ParseException;
 
 import org.xins.logdoc.LogdocSerializable;
 
 /**
  * XINS service caller.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 0.146
  */
 public final class XINSServiceCaller extends ServiceCaller {
 
    // TODO: Allow configuration of HTTP call method (GET / POST)
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The number of constructed call executors.
     */
    private static int CALL_EXECUTOR_COUNT;
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Creates a <code>PostMethod</code> object for the specific base URL,
     * function name and parameter set. If any of the parameters does not have
     * both a key and a value, then it is not sent down.
     *
     * @param baseURL
     *    the base URL, cannot be <code>null</code>.
     *
     * @param functionName
     *    the name of the function, cannot be <code>null</code>.
     *
     * @param parameters
     *    the parameters to be passed, or <code>null</code>;
     *
     * @return
     *    the {@link PostMethod} object, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>baseURL == null || functionName == null</code>.
     */
    private static final PostMethod createPostMethod(String         baseURL,
                                                     String         functionName,
                                                     PropertyReader parameters)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("baseURL",      baseURL,
                                      "functionName", functionName);
 
       // TODO: More checks on the function name? It cannot be an empty string,
       //       for example.
 
       // Construct PostMethod object
       PostMethod method = new PostMethod(baseURL);
 
       // TODO: Allow this to be configured to either "function" or "_function"
       //       for now. Completely change to _function at some point.
       method.addParameter("function", functionName);
 
       // If a diagnostic context is available, pass it on
       String contextID = NDC.peek();
       if (contextID != null && contextID.length() > 0) {
          method.addParameter("_context", contextID);
       }
 
       // If there are parameters, then process them
       int paramCount = (parameters == null) ? 0 : parameters.size();
       if (paramCount > 0) {
 
          // Loop through them all
          Iterator names = parameters.getNames();
          for (int i = 0; i < paramCount; i++) {
 
             // Get the parameter key
             String key = (String) names.next();
 
             // Process key only if it is not null and not an empty string
             if (key != null && key.length() > 0) {
 
                // TODO: Improve checks to make sure the key is properly
                //       formatted, otherwise throw an InvalidKeyException
 
                // The key cannot start with an underscore
                if (key.charAt(0) == '_') {
                   throw new IllegalArgumentException("The parameter key \"" + key + "\" is invalid, since it cannot start with an underscore.");
 
                // The key cannot equal 'function'
                } else if ("function".equals(key)) {
                   throw new IllegalArgumentException("The parameter key \"function\" is invalid, since \"function\" is a reserved word.");
                }
 
                // Get the value
                Object value = parameters.get(key);
 
                // Add this parameter key/value combination
                if (value != null) {
 
                   // Convert the value object to a string
                   String valueString = value.toString();
 
                   // Only add the key/value combo if there is a value string
                   if (valueString != null && valueString.length() > 0) {
                      method.addParameter(key, valueString);
                   }
                }
             }
          }
       }
 
       return method;
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>XINSServiceCaller</code> with the specified
     * descriptor.
     *
     * @param descriptor
     *    the descriptor of the service, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>descriptor == null</code>.
     */
    public XINSServiceCaller(Descriptor descriptor)
    throws IllegalArgumentException {
       super(descriptor);
 
       _parser = new ResultParser();
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The result parser. This field cannot be <code>null</code>.
     */
    private final ResultParser _parser;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    protected Object doCallImpl(TargetDescriptor target,
                                Object           subject)
    throws Throwable {
 
       // Forward the call
       return call(target, (CallRequest) subject);
    }
 
    /**
     * Executes the specified call request on the specified XINS API. If the
     * call fails in any way or if the result is unsuccessful, then a
     * {@link CallException} is thrown.
     *
     * @param target
     *    the service target on which to execute the request, cannot be
     *    <code>null</code>.
     *
     * @param request
     *    the call request to execute, cannot be <code>null</code>.
     *
     * @return
     *    the call result, never <code>null</code> and always successful.
     *
     * @throws IllegalArgumentException
     *    if <code>target == null || request == null</code>.
     *
     * @throws CallException
     *    if the call to the specified target failed.
     *
     * @since XINS 0.198
     */
    public Result call(TargetDescriptor target,
                       CallRequest      request)
    throws IllegalArgumentException,
           CallException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("target", target, "request", request);
 
       // Get the name of the function to call
       String functionName = request.getFunctionName();
 
       // Get the input parameters
       PropertyReader     params    = request.getParameters();
       LogdocSerializable serParams = PropertyReaderUtils.serialize(params, "-");
 
       // Construct new HttpClient object
       HttpClient client = new HttpClient();
 
       // Determine URL and time-outs
       String url               = target.getURL();
       int    totalTimeOut      = target.getTotalTimeOut();
       int    connectionTimeOut = target.getConnectionTimeOut();
       int    socketTimeOut     = target.getSocketTimeOut();
 
       // Configure connection time-out and socket time-out
       client.setConnectionTimeout(connectionTimeOut);
       client.setTimeout          (socketTimeOut);
 
       // Construct the method object
       PostMethod method = createPostMethod(url, functionName, params);
 
       // Prepare a thread for execution of the call
       CallExecutor executor = new CallExecutor(NDC.peek(), client, method);
 
       // Log that we are about to call the API
       Log.log_2011(url, functionName, serParams, totalTimeOut, connectionTimeOut, socketTimeOut);
 
       // Call the API function
       boolean succeeded = false;
       long start = System.currentTimeMillis();
       long duration;
       try {
          controlTimeOut(executor, target);
          succeeded = true;
 
       // Total time-out exceeded
       } catch (TimeOutException exception) {
          duration = System.currentTimeMillis() - start;
          Log.log_2015(duration, url, functionName, serParams, totalTimeOut);
          throw new TotalTimeOutException(request, target, duration);
 
       } finally {
 
          // Determine the call duration
          duration = System.currentTimeMillis() - start;
 
          if (!succeeded) {
 
             // If there was an exception already, don't allow another one to
             // override it, so wrap the releasing of the connection in a
             // try-catch block.
             try {
                method.releaseConnection();
 
             // If there was an exception, then log it
             } catch (Throwable exception) {
                Log.log_2007(exception, url, functionName, serParams, exception.getClass().getName());
             }
          }
       }
 
       // Read response body (mandatory operation)
       byte[] xml = method.getResponseBody();
 
       // Release the connection
       method.releaseConnection();
 
       // Check for exceptions
       Throwable exception = executor._exception;
       if (exception != null) {
 
          // Connection refusal
          if (exception instanceof ConnectException) {
             Log.log_2012(duration, url, functionName, serParams);
             throw new ConnectionRefusedException(request, target, duration);
 
          // Connection time-out
          } else if (exception instanceof HttpConnection.ConnectionTimeoutException) {
             Log.log_2013(duration, url, functionName, serParams, connectionTimeOut);
             throw new ConnectionTimeOutException(request, target, duration);
 
          // Socket time-out
          } else if (exception instanceof HttpRecoverableException) {
 
             // XXX: This is an ugly way to detect a socket time-out, but there
             //      does not seem to be a better way in HttpClient 2.0. This
             //      will, however, be fixed in HttpClient 3.0. See:
             //      http://issues.apache.org/bugzilla/show_bug.cgi?id=19868
 
             String exMessage = exception.getMessage();
             if (exMessage != null && exMessage.startsWith("java.net.SocketTimeoutException")) {
                Log.log_2014(duration, url, functionName, serParams, socketTimeOut);
                throw new SocketTimeOutException(request, target, duration);
 
             // Unspecific I/O error
             } else {
                Log.log_2017(exception, duration, url, functionName, serParams);
                throw new CallIOException(request, target, duration, (IOException) exception);
             }
 
          // Unspecific I/O error
          } else if (exception instanceof IOException) {
             Log.log_2017(exception, duration, url, functionName, serParams);
             throw new CallIOException(request, target, duration, (IOException) exception);
 
          /* TODO: (1/2) add the lines below
          } else {
             Log.log_2018(exception, duration, url, functionName, serParams);
             throw new UnexpectedExceptionException(request, target, duration, exception);
          }
 
          // TODO: (2/2) remove the lines below
          */
 
          } else if (exception instanceof RuntimeException) {
             Log.log_2018(exception, duration, url, functionName, serParams);
             throw (RuntimeException) exception;
 
          } else if (exception instanceof Error) {
             Log.log_2018(exception, duration, url, functionName, serParams);
             throw (Error) exception;
          }
 
          // Unknown kind of exception caught
          throw new Error(exception);
       }
 
       // Check the code
       int code = executor._statusCode;
 
       Log.log_2016(System.currentTimeMillis() - start, url, functionName, serParams, code);
 
       // If HTTP status code is not in 2xx range, abort
       if (code < 200 || code > 299) {
          Log.log_2008(url, functionName, serParams, code);
          throw new UnexpectedHTTPStatusCodeException(request, target, duration, code);
       }
 
       // If the stream is null, then there was an error
       if (xml == null) {
          Log.log_2009(duration, url, functionName, serParams);
          throw new InvalidCallResultException(request, target, duration, "Failed to read the response body.", null);
       }
 
       // Parse the result
       Result result;
       try {
          result = _parser.parse(request, target, duration, xml);
       } catch (ParseException parseException) {
          throw new InvalidCallResultException(request, target, duration, "Failed to parse result.", parseException);
       }
 
       // On failure, throw UnsuccessfulCallException, otherwise return result
       if (result.getErrorCode() != null) {
          throw new UnsuccessfulCallException(result);
       } else {
          return result;
       }
    }
 
    /**
     * Executes the specified request. If possible, multiple targets will be
     * called if a target fails and fail-over is considered allowable.
     *
     * @param request
     *    the request to execute, cannot be <code>null</code>.
     *
     * @return
     *    the call result, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>request == null</code>.
     *
     * @throws CallException
     *    if the call failed.
     *
     * @since XINS 0.198
     */
    public Result execute(CallRequest request)
    throws IllegalArgumentException, CallException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("request", request);
 
       try {
          return (Result) doCall(request).getResult();
 
       // Link the CallException instances together and throw the first one
       } catch (CallFailedException cfe) {
 
          // Retrieve all CallExceptions
          List exceptions = cfe.getExceptions();
 
          // Remember the first one, since this one should be thrown
          CallException first = (CallException) exceptions.get(0);
 
          // Loop through all following CallExceptions to link them to the
          // previous one
          int count = exceptions.size();
          CallException previous = first;
          for (int i = 1; i < count; i++) {
             CallException current = (CallException) exceptions.get(i);
             previous.setNext(current);
             previous = current;
          }
 
          // Throw the first CallException
          throw first;
       }
    }
 
    /**
     * Determines whether a call to a XINS API should fail-over to the next
     * selected target.
     *
     * The implementation of this method in class
     * <code>XINSServiceCaller</code> allows fail-over if and only if the
     * specified exception indicates a connection problem (i.e. if it is an
     * instance of class {@link ConnectionException}.
     *
     * @param subject
     *    the subject for the call, as passed to {@link #doCall(Object)}, can
     *    be <code>null</code>.
     *
     * @param exception
     *    the exception caught while calling the most recently called target,
     *    never <code>null</code>.
     *
     * @return
     *    <code>true</code> if the call should fail-over to the next target, or
     *    <code>false</code> if it should not.
     */
    protected boolean shouldFailOver(Object subject, Throwable exception) {
 
       if (exception instanceof CallException) {
 
          // If fail-over is allowed even if request is already sent, then
          // short-circuit
          CallException callException = (CallException) exception;
          if (callException.getRequest().isFailOverAllowed()) {
             return true;
          }
 
          // Connection refusal or connection time-outs
          if (exception instanceof ConnectionException) {
             return true;
 
          // A non-2xx HTTP status code indicates the request was not handled
          } else if (exception instanceof UnexpectedHTTPStatusCodeException) {
             return true;
 
          // Some XINS error codes indicate the request was not accepted
          } else if (exception instanceof UnsuccessfulCallException) {
             String code = ((UnsuccessfulCallException) exception).getErrorCode();
             if ("_InvalidRequest".equals(code) ||
                 "_DisabledFunction".equals(code)) {
                return true;
             }
          }
       }
 
       return false;
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * Result of a call to a XINS API function.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
     *
     * @since XINS 0.146
     */
    public static final class Result extends Object {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>Result</code> object.
        *
        * @param request
        *    the original {@link CallRequest} that was used to perform the
        *    call, cannot be <code>null</code>.
        *
        * @param target
        *    the {@link TargetDescriptor} that was used to successfully get the
        *    result, cannot be <code>null</code>.
        *
        * @param duration
        *    the call duration, should be &gt;= 0.
        *
        * @param code
        *    the return code, if any, can be <code>null</code>.
        *
        * @param parameters
        *    output parameters returned by the function, or <code>null</code>.
        *
        * @param dataElement
        *    the data element returned by the function, or <code>null</code>; if
        *    specified then the name must be <code>"data"</code>, with no
        *    namespace.
        *
        * @throws IllegalArgumentException
        *    if <code>request    == null
        *          || target     == null
        *          || duration &lt; 0
        */
       public Result(CallRequest      request,
                     TargetDescriptor target,
                     long             duration,
                     String           code,
                     PropertyReader   parameters,
                     DataElement      dataElement)
       throws IllegalArgumentException {
 
          // Check preconditions
          MandatoryArgumentChecker.check("request", request, "target", target);
          if (duration < 0) {
             throw new IllegalArgumentException("duration (" + duration + ") < 0");
          }
 
          // Store all the information
          _request     = request;
          _target      = target;
          _duration    = duration;
          _code        = code;
          _parameters  = parameters;
          _dataElement = dataElement;
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       /**
        * The original <code>CallRequest</code>. Cannot be <code>null</code>.
        */
       private final CallRequest _request;
 
       /**
        * The <code>TargetDescriptor</code> that was used to produce this
        * result. Cannot be <code>null</code>.
        */
       private final TargetDescriptor _target;
 
       /**
        * The call duration. Guaranteed to be &gt;= 0.
        */
       private final long _duration;
 
       /**
       /**
        * The error code. This field is <code>null</code> if the call was
        * successful and thus no error code was returned.
        */
       private final String _code;
 
       /**
        * The parameters and their values. This field is never <code>null</code>.
        */
       private final PropertyReader _parameters;
 
       /**
        * The data element. This field is <code>null</code> if there is no data
        * element.
        */
       private final DataElement _dataElement;
 
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Returns the original <code>CallRequest</code>.
        *
        * @return
        *    the {@link CallRequest}, cannot be <code>null</code>.
        *
        * @since XINS 0.203
        */
       public CallRequest getRequest() {
          return _request;
       }
 
       /**
        * Returns the <code>TargetDescriptor</code> that was used to generate
        * this result.
        *
        * @return
        *    the {@link TargetDescriptor}, cannot be <code>null</code>.
        */
       public TargetDescriptor getTarget() {
          return _target;
       }
 
       /**
        * Returns the call duration in milliseconds.
        *
        * @return
        *    the call duration in milliseconds, guaranteed to be &gt;= 0.
        *
        * @since XINS 0.203
        */
       public long getDuration() {
          return _duration;
       }
 
       /**
        * Returns the error code.
        *
        * @return
        *    the error code or <code>null</code> if no code was returned.
        *
        * @deprecated
        *    Deprecated since XINS 0.182.
        *    Use {@link #getErrorCode()} instead.
        */
       public String getCode() {
          return _code;
       }
 
       /**
        * Returns the error code.
        *
        * @return
        *    the error code or <code>null</code> if no code was returned.
        */
       public String getErrorCode() {
          return _code;
       }
 
       /**
        * Returns the success indication.
        *
        * @return
        *    <code>true</code> if the result is successful, <code>false</code>
        *    otherwise.
        *
        * @deprecated
        *    Deprecated since XINS 0.182.
        *    Use {@link #getErrorCode()}<code> == null</code> instead.
        */
       public boolean isSuccess() {
          return getErrorCode() == null;
       }
 
       /**
        * Gets all parameters.
        *
        * @return
        *    a {@link PropertyReader} with all parameters, or <code>null</code>
        *    if there are none.
        */
       public PropertyReader getParameters() {
          return _parameters;
       }
 
       /**
        * Gets the value of the specified parameter.
        *
        * @param name
        *    the parameter element name, not <code>null</code>.
        *
        * @return
        *    string containing the value of the parameter element,
        *    not <code>null</code>.
        *
        * @throws IllegalArgumentException
        *    if <code>name == null</code>.
        */
       public String getParameter(String name)
       throws IllegalArgumentException {
 
          // Check preconditions
          MandatoryArgumentChecker.check("name", name);
 
          // Short-circuit if there are no parameters at all
          if (_parameters == null) {
             return null;
          }
 
          // Otherwise return the parameter value
          return _parameters.get(name);
       }
 
       /**
        * Returns the optional extra data. The data is an XML {@link DataElement}, or
        * <code>null</code>.
        *
        * @return
        *    the extra data as an XML {@link Element}, can be <code>null</code>;
        */
       public DataElement getDataElement() {
 
          return _dataElement;
       }
    }
 
    /**
     * Executor of calls to an API.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
     *
     * @since XINS 0.195
     */
    private static final class CallExecutor extends Thread {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>CallExecutor</code> for the specified call to
        * a XINS API.
        *
        * @param contextID
        *    the diagnostic context identifier, can be <code>null</code>.
        *
        * @param httpClient
        *    the HTTP client to use when executing the call, cannot be
        *    <code>null</code>.
        *
        * @param call
        *    the definition of the call to a XINS API, cannot be
        *    <code>null</code>.
        *
        * @throws IllegalArgumentException
        *    if <code>httpClient == null || call == null</code>.
        */
       private CallExecutor(String     contextID,
                            HttpClient httpClient,
                            PostMethod call)
       throws IllegalArgumentException {
 
          super("XINS call executor #" + CALL_EXECUTOR_COUNT++);
 
          // Check preconditions
          MandatoryArgumentChecker.check("httpClient", httpClient,
                                         "call",       call);
 
          // Store links to the objects
          _contextID  = contextID;
          _httpClient = httpClient;
          _call       = call;
 
          // The Java Virtual Machine exits when the only threads running are
          // all daemon threads. This is such a thread.
          setDaemon(true);
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       /**
        * The diagnostic context identifier. This field can be
        * <code>null</code>.
        */
       private final String _contextID;
 
       /**
        * The HTTP client to use when executing the call. This field cannot be
        * <code>null</code>.
        */
       private final HttpClient _httpClient;
 
       /**
        * The definition of the call to a XINS API to execute. This field
        * cannot be <code>null</code>.
        */
       private final PostMethod _call;
 
       /**
        * The HTTP status code returned by the call to the XINS API. This is
        * set to a negative number if there is no status code.
        */
       private int _statusCode;
 
       /**
        * The exception caught while executing the call. If there was no
        * exception, then this field is <code>null</code>.
        */
       private Throwable _exception;
 
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Runs this thread. It will call the XINS API. If that call was
        * successful, then the status code is stored in this object. Otherwise
        * there is an exception. In that case the exception is stored in this
        * object.
        */
       public void run() {
 
          // Reset for this run
          _exception  = null;
          _statusCode = -1;
 
          // Activate the diagnostic context ID
          if (_contextID != null) {
             NDC.push(_contextID);
          }
 
          // Execute the call to the XINS API
          try {
             _statusCode = _httpClient.executeMethod(_call);
 
          // If an exception is thrown, store it for processing at later stage
          } catch (Throwable exception) {
             _exception = exception;
          }
         _call.releaseConnection();
       }
    }
 }
