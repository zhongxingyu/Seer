 /*
  * $Id$
  */
 package org.xins.common.http;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
 import java.net.ConnectException;
 
 import java.util.Iterator;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpConnection;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.HttpRecoverableException;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 
 import org.apache.log4j.NDC;
 
 import org.xins.common.Log;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.TimeOutException;
 
 import org.xins.common.collections.BasicPropertyReader;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.collections.PropertyReaderUtils;
 
 import org.xins.common.net.URLEncoding;
 
 import org.xins.common.service.CallException;
 import org.xins.common.service.CallExceptionList;
 import org.xins.common.service.CallRequest;
 import org.xins.common.service.CallResult;
 import org.xins.common.service.ConnectionRefusedCallException;
 import org.xins.common.service.ConnectionTimeOutCallException;
 import org.xins.common.service.Descriptor;
 import org.xins.common.service.GenericCallException;
 import org.xins.common.service.IOCallException;
 import org.xins.common.service.ServiceCaller;
 import org.xins.common.service.SocketTimeOutCallException;
 import org.xins.common.service.TargetDescriptor;
 import org.xins.common.service.TotalTimeOutCallException;
 import org.xins.common.service.UnexpectedExceptionCallException;
 
 import org.xins.common.text.FastStringBuffer;
 
 /**
  * HTTP service caller. This class can be used to perform a call to an HTTP
  * server and fail-over to other HTTP servers if the first one fails.
  *
  * <p>The following example code snippet constructs an
  * <code>HTTPServiceCaller</code> instance:
  *
  * <blockquote><pre>// Initialize properties for the services. Normally these
 // properties would come from a configuration source, like a file.
 {@link BasicPropertyReader} properties = new {@link BasicPropertyReader#BasicPropertyReader() BasicPropertyReader}();
 properties.{@link BasicPropertyReader#set(String,String) set}("myapi",         "group, random, server1, server2");
 properties.{@link BasicPropertyReader#set(String,String) set}("myapi.server1", "service, http://server1/myapi, 10000");
 properties.{@link BasicPropertyReader#set(String,String) set}("myapi.server2", "service, http://server2/myapi, 12000");
 
 // Construct a descriptor and an HTTPServiceCaller instance
 {@link Descriptor Descriptor} descriptor = {@link org.xins.common.service.DescriptorBuilder DescriptorBuilder}.{@link org.xins.common.service.DescriptorBuilder#build(PropertyReader,String) build}(properties, "myapi");
 HTTPServiceCaller caller = new {@link #HTTPServiceCaller(Descriptor) HTTPServiceCaller}(descriptor);</pre></blockquote>
  *
  * <p>Then the following code snippet uses this <code>HTTPServiceCaller</code>
  * to perform an HTTP GET call:
  *
  * <blockquote><pre>{@link BasicPropertyReader} params = new {@link BasicPropertyReader BasicPropertyReader}();
 params.{@link BasicPropertyReader#set(String,String) set}("street",      "Broadband Avenue");
 params.{@link BasicPropertyReader#set(String,String) set}("houseNumber", "12");
 
 {@link HTTPCallRequest} request = new {@link HTTPCallRequest#HTTPCallRequest(HTTPMethod,PropertyReader) HTTPCallRequest}({@link HTTPMethod}.{@link HTTPMethod#GET GET}, params);
 {@link HTTPCallResult} result = caller.{@link #call(HTTPCallRequest) call}(request);</pre></blockquote>
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 0.207
  */
 public final class HTTPServiceCaller extends ServiceCaller {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Creates an appropriate <code>HttpMethod</code> object for the specified
     * URL.
     *
     * @param url
     *    the URL for which to create an {@link HttpMethod} object, should not
     *    be <code>null</code>.
     *
     * @param request
     *    the HTTP call request, not <code>null</code>.
     *
     * @return
     *    the constructed {@link HttpMethod} object, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>url == null || request == null</code>.
     */
    private static HttpMethod createMethod(String          url,
                                           HTTPCallRequest request)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("url", url, "request", request);
 
       // Get the HTTP method (like GET and POST) and parameters
       HTTPMethod     method     = request.getMethod();
       PropertyReader parameters = request.getParameters();
 
       // HTTP POST request
       if (method == HTTPMethod.POST) {
          PostMethod postMethod = new PostMethod(url);
 
          // Loop through the parameters
          Iterator keys = parameters.getNames();
          while (keys.hasNext()) {
 
             // Get the parameter key
             String key = (String) keys.next();
 
             // Get the value
             Object value = parameters.get(key);
 
             // Add this parameter key/value combination, but only if both the
             // key and the value are not null. A general rule in XINS is that
             // if a parameter (input or output) has the empty string as the
             // value, this is equivalent to having the parameter not set at
             // all.
             if (key != null && value != null) {
                postMethod.addParameter(key, value.toString());
             }
          }
          return postMethod;
 
       // HTTP GET request
       } else if (method == HTTPMethod.GET) {
          GetMethod getMethod = new GetMethod(url);
 
          // Loop through the parameters
          FastStringBuffer query = new FastStringBuffer(255);
          Iterator keys = parameters.getNames();
          while (keys.hasNext()) {
 
             // Get the parameter key
             String key = (String) keys.next();
 
             // Get the value
             Object value = parameters.get(key);
 
             // Add this parameter key/value combination, but only if both the
             // key and the value are not null. A general rule in XINS is that
             // if a parameter (input or output) has the empty string as the
             // value, this is equivalent to having the parameter not set at
             // all.
             if (key != null && value != null) {
            if (key != null && value != null) {
 
                if (query.getLength() > 0) {
                   query.append(",");
                }
                query.append(URLEncoding.encode(key));
                query.append("=");
                query.append(URLEncoding.encode(value.toString()));
             }
          }
          if (query.getLength() > 0) {
             getMethod.setQueryString(query.toString());
          }
          return getMethod;
 
       // Unrecognized HTTP method (only GET and POST are supported)
       } else {
          throw new Error("Unrecognized method \"" + method + "\".");
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>HTTPServiceCaller</code> object.
     *
     * @param descriptor
     *    the descriptor of the service, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>descriptor == null</code>.
     */
    public HTTPServiceCaller(Descriptor descriptor)
    throws IllegalArgumentException {
       super(descriptor);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Calls the specified target using the specified subject. If the call
     * succeeds, then a {@link HTTPCallResult} object is returned, otherwise a
     * {@link CallException} is thrown.
     *
     * <p>The implementation of this method in class
     * <code>HTTPServiceCaller</code> delegates to
     * {@link #call(HTTPCallRequest,TargetDescriptor)}.
     *
     * @param target
     *    the target to call, cannot be <code>null</code>.
     *
     * @param request
     *    the call request to be executed, must be an instance of class
     *    {@link HTTPCallRequest}, cannot be <code>null</code>.
     *
     * @return
     *    the result, if and only if the call succeeded, always an instance of
     *    class {@link HTTPCallResult}, never <code>null</code>.
     *
     * @throws ClassCastException
     *    if the specified <code>request</code> object is not <code>null</code>
     *    and not an instance of class {@link HTTPCallRequest}.
     *
     * @throws IllegalArgumentException
     *    if <code>target == null || request == null</code>.
     *
     * @throws CallException
     *    if the call to the specified target failed.
     */
    protected Object doCallImpl(CallRequest      request,
                                TargetDescriptor target)
    throws ClassCastException, IllegalArgumentException, CallException {
 
       // Delegate to method with more specialized interface
       return call((HTTPCallRequest) request, target);
    }
 
    /**
     * Performs the specified request towards the HTTP service. If the call
     * succeeds with one of the targets, then a {@link HTTPCallResult} object
     * is returned, that combines the HTTP status code and the data returned.
     * Otherwise, if none of the targets could successfully be called, a
     * {@link CallException} is thrown.
     *
     * @param request
     *    the call request, not <code>null</code>.
     *
     * @return
     *    the result of the call, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>request == null</code>.
     *
     * @throws GenericCallException
     *    if the first call attempt failed due to a generic reason and all the
     *    other call attempts failed as well.
     *
     * @throws HTTPCallException
     *    if the first call attempt failed due to an HTTP-related reason and
     *    all the other call attempts failed as well.
     */
    public HTTPCallResult call(HTTPCallRequest request)
    throws IllegalArgumentException,
           GenericCallException,
           HTTPCallException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("request", request);
 
       // Perform the call
       CallResult callResult;
       try {
          callResult = doCall(request);
 
       // Allow GenericCallException, HTTPCallException and Error to proceed,
       // but block other kinds of exceptions and throw an Error instead.
       } catch (GenericCallException exception) {
          throw exception;
       } catch (HTTPCallException exception) {
          throw exception;
       } catch (Exception exception) {
          throw new Error(getClass().getName() + ".doCall(" + request.getClass().getName() + ") threw " + exception.getClass().getName() + '.');
       }
 
       return (HTTPCallResult) callResult;
    }
 
    /**
     * Executes the specified HTTP call request on the specified target. If the
     * call fails in any way, then a {@link CallException} is thrown.
     *
     * @param request
     *    the call request to execute, cannot be <code>null</code>.
     *
     * @param target
     *    the service target on which to execute the request, cannot be
     *    <code>null</code>.
     *
     * @return
     *    the call result, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>target == null || request == null</code>.
     *
     * @throws GenericCallException
     *    if the first call attempt failed due to a generic reason and all the
     *    other call attempts failed as well.
     *
     * @throws HTTPCallException
     *    if the first call attempt failed due to an HTTP-related reason and
     *    all the other call attempts failed as well.
     */
    public HTTPCallResult call(HTTPCallRequest  request,
                               TargetDescriptor target)
    throws IllegalArgumentException,
           GenericCallException,
           HTTPCallException {
 
       // TODO: Review log message 3304. Perhaps remove it.
 
       // NOTE: Preconditions are checked by the CallExecutor constructor
       // Prepare a thread for execution of the call
       CallExecutor executor = new CallExecutor(request, target, NDC.peek());
 
       // TODO: Log that we are about to make an HTTP call
 
       // Perform the HTTP call
       boolean succeeded = false;
       long start = System.currentTimeMillis();
       long duration;
       try {
          controlTimeOut(executor, target);
          succeeded = true;
 
       // Total time-out exceeded
       } catch (TimeOutException exception) {
          duration = System.currentTimeMillis() - start;
          // TODO: Log the total time-out (2015 ?)
          throw new TotalTimeOutCallException(request, target, duration);
 
       } finally {
 
          // Determine the call duration
          duration = System.currentTimeMillis() - start;
       }
 
       // Check for exceptions
       Throwable exception = executor.getException();
       if (exception != null) {
 
          // Connection refusal
          if (exception instanceof ConnectException) {
             // TODO: Log connection refusal (2012 ?)
             throw new ConnectionRefusedCallException(request, target, duration);
 
          // Connection time-out
          } else if (exception instanceof HttpConnection.ConnectionTimeoutException) {
             // TODO: Log connection time-out (2013 ?)
             throw new ConnectionTimeOutCallException(request, target, duration);
 
          // Socket time-out
          } else if (exception instanceof HttpRecoverableException) {
 
             // XXX: This is an ugly way to detect a socket time-out, but there
             //      does not seem to be a better way in HttpClient 2.0. This
             //      will, however, be fixed in HttpClient 3.0. See:
             //      http://issues.apache.org/bugzilla/show_bug.cgi?id=19868
 
             String exMessage = exception.getMessage();
             if (exMessage != null && exMessage.startsWith("java.net.SocketTimeoutException")) {
                // TODO: Log socket time-out (2014 ?)
                throw new SocketTimeOutCallException(request, target, duration);
 
             // Unspecific I/O error
             } else {
                // TODO: Log unspecific I/O error (2017 ?)
                throw new IOCallException(request, target, duration, (IOException) exception);
             }
 
          // Unspecific I/O error
          } else if (exception instanceof IOException) {
             // TODO: Log unspecific I/O error (2017 ?)
             throw new IOCallException(request, target, duration, (IOException) exception);
 
          // Unrecognized kind of exception caught
          } else {
             // TODO: Log unrecognized exception error (2018 ?)
             throw new UnexpectedExceptionCallException(request, target, duration, null, exception);
          }
       }
 
       // TODO: Log (2016 ?)
 
       // Grab the result from the HTTP call
       HTTPCallResult.Data data = executor.getData();
 
       // Check the status code, if necessary
       HTTPStatusCodeVerifier verifier = request.getStatusCodeVerifier();
       if (verifier != null) {
 
          int code = data.getStatusCode();
 
          if (! verifier.isAcceptable(code)) {
             // TODO: Pass down body as well. Perhaps just pass down complete
             //       HTTPCallResult object and add getter for the body to the
             //       StatusCodeHTTPCallException class.
             throw new StatusCodeHTTPCallException(request, target, duration, code);
          }
       }
 
       return new HTTPCallResult(request, target, duration, null, data);
    }
 
    /**
     * Constructs an appropriate <code>CallResult</code> object for a
     * successful call attempt. This method is called from
     * {@link #doCall(CallRequest)}.
     *
     * <p>The implementation of this method in class
     * {@link HTTPServiceCaller} expects an {@link HTTPCallRequest} and
     * returns an {@link HTTPCallResult}.
     *
     * @param request
     *    the {@link CallRequest} that was to be executed, never
     *    <code>null</code> when called from {@link #doCall(CallRequest)};
     *    should be an instance of class {@link HTTPCallRequest}.
     *
     * @param succeededTarget
     *    the {@link TargetDescriptor} for the service that was successfully
     *    called, never <code>null</code> when called from
     *    {@link #doCall(CallRequest)}.
     *
     * @param duration
     *    the call duration in milliseconds, must be a non-negative number.
     *
     * @param exceptions
     *    the list of {@link CallException} instances, or <code>null</code> if
     *    there were no call failures.
     *
     * @param result
     *    the result from the call, which is the object returned by
     *    {@link #doCallImpl(CallRequest,TargetDescriptor)}, always an instance
     *    of class {@link HTTPCallResult}, never <code>null</code>; .
     *
     * @return
     *    an {@link HTTPCallResult} instance, never <code>null</code>.
     *
     * @throws ClassCastException
     *    if either <code>request</code> or <code>result</code> is not of the
     *    correct class.
     *
     * @since XINS 0.207
     */
    protected CallResult createCallResult(CallRequest       request,
                                          TargetDescriptor  succeededTarget,
                                          long              duration,
                                          CallExceptionList exceptions,
                                          Object            result)
    throws ClassCastException {
 
 
       return new HTTPCallResult((HTTPCallRequest) request,
                                 succeededTarget,
                                 duration,
                                 exceptions,
                                 (HTTPCallResult.Data) result);
    }
 
    /**
     * Determines whether a call should fail-over to the next selected target.
     *
     * @param request
     *    the request for the call, as passed to {@link #doCall(CallRequest)},
     *    should not be <code>null</code>.
     *
     * @param exception
     *    the exception caught while calling the most recently called target,
     *    should not be <code>null</code>.
     *
     * @return
     *    <code>true</code> if the call should fail-over to the next target, or
     *    <code>false</code> if it should not.
     */
    protected boolean shouldFailOver(CallRequest request,
                                     Throwable   exception) {
 
       // First let the superclass do it's job
       if (super.shouldFailOver(request, exception)) {
          return true;
 
       // A non-2xx HTTP status code indicates the request was not handled
       } else if (exception instanceof StatusCodeHTTPCallException) {
          int code = ((StatusCodeHTTPCallException) exception).getStatusCode();
          return (code < 200 || code > 299);
 
       // Otherwise do not fail over
       } else {
          return false;
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * Executor of calls to an API.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
     *
     * @since XINS 0.207
     */
    private static final class CallExecutor extends Thread {
 
       //-------------------------------------------------------------------------
       // Class fields
       //-------------------------------------------------------------------------
 
       /**
        * The number of constructed call executors.
        */
       private static int CALL_EXECUTOR_COUNT;
 
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>CallExecutor</code> for the specified call to
        * an HTTP service.
        *
        * <p>A <em>Nested Diagnostic Context identifier</em> (NDC) may be
        * specified, which will be set for the new thread when it is executed.
        * If the NDC is <code>null</code>, then it will be left unchanged. See
        * the {@link NDC} class.
        *
        * @param request
        *    the call request to execute, cannot be <code>null</code>.
        *
        * @param target
        *    the service target on which to execute the request, cannot be
        *    <code>null</code>.
        *
        * @param context
        *    the <em>Nested Diagnostic Context identifier</em> (NDC), or
        *    <code>null</code>.
        *
        * @throws IllegalArgumentException
        *    if <code>target == null || request == null</code>.
        */
       private CallExecutor(HTTPCallRequest  request,
                            TargetDescriptor target,
                            String           context)
       throws IllegalArgumentException {
 
          // Create textual representation of this object
          _asString = "HTTP call executor #" + (++CALL_EXECUTOR_COUNT);
 
          // Check preconditions
          MandatoryArgumentChecker.check("request", request, "target", target);
 
          // Store data for later use in the run() method
          _request = request;
          _target  = target;
          _context = context;
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       /**
        * Textual representation of this object. Never <code>null</code>.
        */
       private final String _asString;
 
       /**
        * The call request to execute. Never <code>null</code>.
        */
       private final HTTPCallRequest _request;
 
       /**
        * The service target on which to execute the request. Never
        * <code>null</code>.
        */
       private final TargetDescriptor _target;
 
       /**
        * The <em>Nested Diagnostic Context identifier</em> (NDC). Is set to
        * <code>null</code> if it should be left unchanged.
        */
       private final String _context;
 
       /**
        * The exception caught while executing the call. If there was no
        * exception, then this field is <code>null</code>.
        */
       private Throwable _exception;
 
       /**
        * The result from the call. The value of this field is
        * <code>null</code> if the call was unsuccessful or if it was not
        * executed yet.
        */
       private HTTPCallResult.Data _result;
 
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Runs this thread. It will call the HTTP service. If that call was
        * successful, then the result is stored in this object. Otherwise
        * there is an exception, in which case that exception is stored in this
        * object instead.
        */
       public void run() {
 
          // TODO: Check if this request was already executed, since this is a
          //       stateful object. If not, mark it as executing within a
          //       synchronized section, so it may no 2 threads may execute
          //       this request at the same time.
 
          // XXX: Note that performance could be improved by using local
          //      variables for _target and _request
 
          // Activate the diagnostic context ID
          if (_context != null) {
             NDC.push(_context);
          }
 
          // Get the input parameters
          PropertyReader params = _request.getParameters();
 
          // TODO: Uncomment or remove the following line:
          // LogdocSerializable serParams = PropertyReaderUtils.serialize(params, "-");
 
          // Construct new HttpClient object
          HttpClient client = new HttpClient();
 
          // Determine URL and time-outs
          String url               = _target.getURL();
          int    totalTimeOut      = _target.getTotalTimeOut();
          int    connectionTimeOut = _target.getConnectionTimeOut();
          int    socketTimeOut     = _target.getSocketTimeOut();
 
          // Configure connection time-out and socket time-out
          client.setConnectionTimeout(connectionTimeOut);
          client.setTimeout          (socketTimeOut);
 
          // Construct the method object
          HttpMethod method = createMethod(url, _request);
 
          // Log that we are about to make the HTTP call
          // TODO: Uncomment or remove the following line:
          // Log.log_2011(url, functionName, serParams, totalTimeOut, connectionTimeOut, socketTimeOut);
 
          // Perform the HTTP call
          try {
             int    statusCode = client.executeMethod(method);
             byte[] body       = method.getResponseBody();
 
             // Store the result
             _result = new HTTPCallResult.Data(statusCode, body);
 
          // If an exception is thrown, store it for processing at later stage
          } catch (Throwable exception) {
             _exception = exception;
          }
 
          // Release the HTTP connection immediately
          try {
             method.releaseConnection();
          } catch (Throwable exception) {
             // TODO: Log
          }
 
          // Unset the diagnostic context ID
          if (_context != null) {
             NDC.pop();
          }
 
          // TODO: Mark this CallExecutor object as executed, so it may not be
          //       run again
       }
 
       /**
        * Gets the exception if any generated when calling the method.
        *
        * @return
        *    the invocation exception or <code>null</code> if the call
        *    performed successfully.
        */
       private Throwable getException() {
          return _exception;
       }
 
       /**
        * Returns the result if the call was successful. If the call was
        * unsuccessful, then <code>null</code> is returned.
        *
        * @return
        *    the result from the call, or <code>null</code> if it was
        *    unsuccessful.
        */
       private HTTPCallResult.Data getData() {
          return _result;
       }
    }
 }
