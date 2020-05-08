 /*
  * $Id$
  */
 package org.xins.client;
 
 import java.io.IOException;
 import java.net.ConnectException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Collections;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpConnection;
 import org.apache.commons.httpclient.HttpRecoverableException;
 import org.apache.commons.httpclient.methods.PostMethod;
 
 import org.apache.log4j.NDC;
 
 import org.jdom.Element;
 import org.jdom.Namespace;
 
 import org.xins.common.ExceptionUtils;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.TimeOutException;
 
 import org.xins.common.collections.CollectionUtils;
 
 import org.xins.common.service.CallFailedException;
 import org.xins.common.service.CallResult;
 import org.xins.common.service.Descriptor;
 import org.xins.common.service.ServiceCaller;
 import org.xins.common.service.TargetDescriptor;
 import org.xins.common.text.ParseException;
 
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
     * function name and parameter set.
     *
     * @param baseURL
     *    the base URL, cannot be <code>null</code>.
     *
     * @param functionName
     *    the name of the function, cannot be <code>null</code>.
     *
     * @param parameters
     *    the parameters to be passed, or <code>null</code>; keys must be
     *    either <code>null</code> or otherwise {@link String} instances;
     *    values can be of any class; if
     *    <code>(key == null
     *        || key.</code>{@link String#length() length()} &lt; 1
     *        || value == null
     *        || value.</code>{@link Object#toString() toString()} == null
     *        || value.</code>{@link Object#toString() toString()}<code>.</code>{@link String#length() length()}<code> &lt; 1)</code>,
     *    then this parameter will not be sent down.
     *
     * @return
     *    the {@link PostMethod} object, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>baseURL == null || functionName == null</code>.
     */
    private static final PostMethod createPostMethod(String baseURL,
                                                     String functionName,
                                                     Map parameters)
    throws IllegalArgumentException {
 
       // TODO: Consider using an IndexedMap, for improved iteration
       //       performance
 
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
          Iterator keys = parameters.keySet().iterator();
          for (int i = 0; i < paramCount; i++) {
 
             // Get the parameter key
             String key = (String) keys.next();
 
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
     * Constructs a new <code>XINSServiceCaller</code> object.
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
     * The result parser.
     */
    private final ResultParser _parser;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    protected Object doCallImpl(TargetDescriptor target,
                                Object           subject)
    throws Throwable {
 
       // Convert subject to a CallRequest
       CallRequest request = (CallRequest) subject;
 
       // Disect the CallRequest and forward the method call
       return call(target,
                   request.getFunctionName(),
                   request.getParameters());
    }
 
    /**
     * Calls the XINS service at the specified target.
     *
     * @param target
     *    the service target on which to execute the request, cannot be
     *    <code>null</code>.
     *
     * @param functionName
     *    the name of the function to be called, not <code>null</code>.
     *
     * @param parameters
     *    the parameters to be passed to that function, or
     *    <code>null</code>; keys must be {@link String Strings}, values can be
     *    of any class; this {@link Map} may be unmodifiable, since it is
     *    guaranteed that it will not be changed.
     *
     * @return
     *    the call result, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>target == null || functionName == null</code>.
     *
     * @throws CallException
     *    if the call to the specified target failed.
     */
    public Result call(TargetDescriptor target,
                       String           functionName,
                       Map              parameters)
    throws IllegalArgumentException,
           CallException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("target",       target,
                                      "functionName", functionName);
 
       // Construct new HttpClient object
       HttpClient client = new HttpClient();
 
       // Determine URL and time-outs
       String url               = target.getURL();
       int    totalTimeOut      = target.getTimeOut();
       int    connectionTimeOut = target.getConnectionTimeOut();
       int    socketTimeOut     = target.getSocketTimeOut();
 
       Log.log_2011(url, functionName, totalTimeOut, connectionTimeOut, socketTimeOut);
 
       // Configure connection time-out and socket time-out
       client.setConnectionTimeout(connectionTimeOut);
       client.setTimeout          (socketTimeOut);
 
       // Construct the method object
       PostMethod method = createPostMethod(url, functionName, parameters);
 
       // Prepare a thread for execution of the call
       CallExecutor executor = new CallExecutor(NDC.peek(), client, method);
 
       boolean succeeded = false;
       try {
          controlTimeOut(executor, target);
          succeeded = true;
 
       } catch (TimeOutException exception) {
          Log.log_2015(url, functionName, totalTimeOut);
          throw new TotalTimeOutException();
 
       } finally {
          if (succeeded == false) {
 
             // If there was an exception already, don't allow another one to
             // override it, so wrap the releasing of the connection in a
             // try-catch block.
             try {
                method.releaseConnection();
 
             // If there was an exception, then log the root cause for it
             } catch (Throwable exception) {
                exception = ExceptionUtils.getRootCause(exception);
                Log.log_2007(exception, exception.getClass().getName());
             }
          }
       }
 
       // Read response body (mandatory operation)
       String body = method.getResponseBodyAsString();
 
       // Release the connection
       method.releaseConnection();
 
       // Check for exceptions
       Throwable exception =  executor._exception;
       if (exception != null) {
 
          // Connection refusal
          if (exception instanceof ConnectException) {
             Log.log_2012(url, functionName);
             throw new ConnectionRefusedException();
 
          // Connection time-out
          } else if (exception instanceof HttpConnection.ConnectionTimeoutException) {
             Log.log_2013(url, functionName, connectionTimeOut);
             throw new ConnectionTimeOutException();
 
          // Socket time-out
          } else if (exception instanceof HttpRecoverableException) {
 
             // XXX: This is an ugly way to detect a socket time-out, but there
             //      does not seem to be a better way in HttpClient 2.0. This
             //      will, however, be fixed in HttpClient 3.0. See:
             //      http://issues.apache.org/bugzilla/show_bug.cgi?id=19868
 
             String exMessage = exception.getMessage();
             if (exMessage != null && exMessage.startsWith("java.net.SocketTimeoutException")) {
                Log.log_2014(url, functionName, socketTimeOut);
                throw new SocketTimeOutException();
             } else {
                // TODO: Log this?
                throw new CallIOException((IOException) exception);
             }
 
          } else if (exception instanceof IOException) {
             // TODO: Log this?
             throw new CallIOException((IOException) exception);
 
          } else if (exception instanceof RuntimeException) {
             throw (RuntimeException) exception;
 
          } else if (exception instanceof Error) {
             throw (Error) exception;
          }
 
          // Unknown kind of exception caught
          // XXX: Could this be improved?
          throw new Error(exception);
       }
 
       // Check the code
       // TODO: Throw specific exception that stores the HTTP code so it can be
       //       used to determine whether or not fail-over should be attempted.
       int code = executor._statusCode;
 
       Log.log_2016(url, functionName, code);
 
       // If HTTP status code is not in 2xx range, abort
       if (code < 200 || code > 299) {
          Log.log_2008(url, functionName, code);
          throw new UnexpectedHTTPStatusCodeException(code);
       }
 
       // If the body is null, then there was an error
       if (body == null) {
          Log.log_2009();
          throw new InvalidCallResultException("Failed to read the response body.");
       }
 
       // Parse and return the result
       try {
          return _parser.parse(target, body);
       } catch (ParseException parseException) {
          throw new InvalidCallResultException(parseException.getMessage());
       }
    }
 
    /**
     * Calls the specified API function with the specified
     * parameters.
     *
     * @param functionName
     *    the name of the function to be called, not <code>null</code>.
     *
     * @param parameters
     *    the parameters to be passed to that function, or
     *    <code>null</code>; keys must be {@link String Strings}, values can be
     *    of any class; this {@link Map} may be unmodifiable, since it is
     *    guaranteed that it will not be changed.
     *
     * @return
     *    the call result, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>functionName == null</code>.
     *
     * @throws CallException
     *    if the call failed.
     */
    public Result call(String functionName, Map parameters)
    throws IllegalArgumentException, CallException {
       return call(new CallRequest(functionName, parameters));
    }
 
    /**
     * Executes the specified request.
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
     */
    public Result call(CallRequest request)
    throws IllegalArgumentException, CallException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("request", request);
 
       // Attempt to perform the call
       try {
          CallResult callResult = doCall(request);
          return (Result) callResult.getResult();
 
       } catch (CallFailedException cfe) {
 
          // TODO: Improve this code
          List exceptions = cfe.getExceptions();
          Throwable ex = (Throwable) exceptions.get(0);
          if (ex instanceof CallException) {
             throw (CallException) ex;
          } else if (ex instanceof Error) {
             throw (Error) ex;
          } else {
             String message = "Unexpected " + ex.getClass().getName() + " caught while calling doCall(org.xins.util.service.CallRequest).";
             Log.log_2010(ex, ex.getClass().getName());
             throw new Error(message);
          }
       }
    }
 
    /**
     * Determines whether a call to a XINS API should fail-over to the next
     * selected target.
 
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
      return (exception instanceof ConnectionException) ||
             (exception instanceof UnexpectedHTTPStatusCodeException);
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
        * @param target
        *    the {@link TargetDescriptor} that was used to successfully get the
        *    result, cannot be <code>null</code>.
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
        *    if <code>target == null || (dataElement != null &amp;&amp;
        *             !("data".equals(dataElement.</code>{@link Element#getName() getName()}<code>) &amp;&amp;</code>
        *               {@link Namespace#NO_NAMESPACE}<code>.equals(dataElement.</code>{@link Element#getNamespace() getNamespace()}<code>)))</code>
        */
       public Result(TargetDescriptor target,
                     String           code,
                     Map              parameters,
                     Element          dataElement)
       throws IllegalArgumentException {
 
          // Clone the data element if there is one
          MandatoryArgumentChecker.check("target", target);
          if (dataElement != null) {
             String    dataElementName = dataElement.getName();
             Namespace ns              = dataElement.getNamespace();
             if (!"data".equals(dataElement.getName())) {
                throw new IllegalArgumentException("dataElement.getName() returned \"" + dataElementName + "\", instead of \"data\".");
             } else if (!Namespace.NO_NAMESPACE.equals(ns)) {
                throw new IllegalArgumentException("dataElement.getNamespace() returned a namespace with URI \"" + ns.getURI() + "\", instead of no namespace.");
             }
             dataElement = (Element) dataElement.clone();
          }
 
          // Store all the information
          _target      = target;
          _code        = code;
          _parameters  = parameters == null
                       ? CollectionUtils.EMPTY_MAP
                       : Collections.unmodifiableMap(parameters);
          _dataElement = dataElement;
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       /**
        * The <code>TargetDescriptor</code> that was used to produced this
        * result. Cannot be <code>null</code>.
        */
       private final TargetDescriptor _target;
 
       /**
        * The result code. This field is <code>null</code> if no code was
        * returned.
        */
       private final String _code;
 
       /**
        * The parameters and their values. This field is never <code>null</code>.
        * If there are no parameters, then this field will be set to
        * {@link CollectionUtils#EMPTY_MAP}.
        */
       private final Map _parameters;
 
       /**
        * The data element. This field is <code>null</code> if there is no data
        * element.
        */
       private final Element _dataElement;
 
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
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
        * Returns the result code.
        *
        * @return
        *    the result code or <code>null</code> if no code was returned.
        * @deprecated Use getErrorCode().
        */
       public String getCode() {
          return _code;
       }
 
       /**
        * Returns the result code.
        *
        * @return
        *    the result code or <code>null</code> if no code was returned.
        */
       public String getErrorCode() {
          return _code;
       }
 
       /**
        * Returns the success indication.
        *
        * @return true if the result is successful, false otherwise.
        * @deprecated Use getErrorCode() == null.
        */
       public boolean isSuccess() {
          return getErrorCode() == null;
       }
       /**
        * Gets all parameters.
        *
        * @return
        *    a <code>Map</code> containing all parameters, never
        *    <code>null</code>; the keys will be the names of the parameters
        *    ({@link String} objects, cannot be <code>null</code>), the values
        *    will be the parameter values ({@link String} objects as well, cannot
        *    be <code>null</code>).
        */
       public Map getParameters() {
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
          return (String) _parameters.get(name);
       }
 
       /**
        * Returns the optional extra data. The data is an XML {@link Element}, or
        * <code>null</code>.
        *
        * @return
        *    the extra data as an XML {@link Element}, can be <code>null</code>;
        *    if it is not <code>null</code>, then
        *    <code><em>return</em>.{@link Element#getName() getName()}.equals("data") &amp;&amp; <em>return</em>.{@link Element#getNamespace() getNamespace()}.equals({@link Namespace#NO_NAMESPACE NO_NAMESPACE})</code>.
        */
       public Element getDataElement() {
 
          // If there is no data element, return null
          if (_dataElement == null) {
             return null;
 
          // Otherwise return a clone of the data element
          } else {
             return (Element) _dataElement.clone();
          }
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
       }
    }
 }
