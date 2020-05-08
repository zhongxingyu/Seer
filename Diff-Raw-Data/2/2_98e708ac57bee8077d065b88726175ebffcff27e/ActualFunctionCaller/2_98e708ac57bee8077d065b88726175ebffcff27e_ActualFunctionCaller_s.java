 /*
  * $Id$
  */
 package org.xins.client;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.net.URL;
 import java.util.Iterator;
 import java.util.Map;
 import org.apache.log4j.Logger;
 import org.jdom.Document;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.util.http.HTTPRequester;
 
 /**
  * Function caller implementation that actually sends an HTTP request to a
  * remote XINS API.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public class ActualFunctionCaller
 extends AbstractFunctionCaller {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The logging category used by this class. This class field is never
     * <code>null</code>.
     */
    private final static Logger LOG = Logger.getLogger(ActualFunctionCaller.class.getName());
 
    /**
     * The number of instances for this class. This field is initialized to 0
     * and updated by the constructor. It is never decreased, only increased.
     */
    private static int INSTANCE_COUNT;
 
    /**
     * Initial buffer size for a parameter string. See
     * {@link #createParameterString(String,Map)}.
     */
    private static int PARAMETER_STRING_BUFFER_SIZE = 256;
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Creates a <code>ActualFunctionCaller</code> object for a XINS API at the
     * specified URL.
     *
     * @param url
     *    the URL for the API, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>url == null</code>.
     */
    public ActualFunctionCaller(URL url)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("url", url);
 
       boolean debugEnabled = LOG.isDebugEnabled();
 
       // Get an instance number and increase the count
       _instanceNum = INSTANCE_COUNT++;
 
       if (debugEnabled) {
          LOG.debug("Creating ActualFunctionCaller #" + _instanceNum + " for URL: " + url);
       }
 
       _url      = url;
       _protocol = url.getProtocol();
       _host     = url.getHost();
       _port     = url.getPort();
       _file     = url.getFile();
 
       _hostAddressIndexLock = new Object();
 
       _callResultParser = new CallResultParser();
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * Unique number of this instance.
     */
    private final int _instanceNum;
 
    /**
     * The URL for the API this object represents. This field is never
     * <code>null</code>.
     */
    private final URL _url;
 
    /**
     * The protocol for the URL. This field is never <code>null</code>.
     */
    private final String _protocol;
 
    /**
     * The hostname for the URL. This field is never <code>null</code>.
     */
    private final String _host;
 
    /**
     * The port for the URL.
     */
    private final int _port;
 
    /**
     * The file part for the URL.
     */
    private final String _file;
 
    /**
     * Flag that indicates if DNS-based round-robin access is used.
     */
    private boolean _roundRobin;
 
    /**
     * Index of the host address last used.
     */
    private int _hostAddressIndex;
 
    /**
     * Lock object for <code>_hostAddressIndex</code>.
     */
    private final Object _hostAddressIndexLock;
 
    /**
     * Call result parser. This field cannot be <code>null</code>.
     */
    private final CallResultParser _callResultParser;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the URL for the API this object represents.
     *
     * @return
     *    the URL, never <code>null</code>.
     */
    public URL getURL() {
       return _url;
    }
 
    /**
     * Performs an HTTP request using the specified parameter string.
     *
     * @param parameterString
     *    the parameter string to pass down, not <code>null</code>.
     *
     * @return
     *    the result of the HTTP request, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>parameterString == null</code>.
     *
     * @throws IOException
     *    if there is an I/O error.
     */
    private HTTPRequester.Result performRequest(String parameterString)
    throws IllegalArgumentException, IOException {
 
       // Check precondition
       MandatoryArgumentChecker.check("parameterString", parameterString);
 
       HTTPRequester requester = new HTTPRequester();
 
       InetAddress[] addresses = null;
       try {
          addresses = InetAddress.getAllByName(_host);
       } catch (UnknownHostException uhe) {
          throw new IOException("Unknown host: " + _host + '.');
       }
 
       int addressCount = addresses.length;
       int startIndex;
       synchronized (_hostAddressIndexLock) {
          _hostAddressIndex++;
          if (_hostAddressIndex >= addressCount) {
             _hostAddressIndex = 0;
          }
          startIndex = _hostAddressIndex;
       }
 
       // TODO: Allow configuration of soft load balancing
       final int maxAttempts = 2;
       Exception lastException = null;
       byte[] parameterStringBytes = parameterString.getBytes("US-ASCII");
       for (int attempt = 0; attempt < maxAttempts; attempt++) {
          for (int n = 0; n < addressCount; n++) {
             int index = (startIndex + n) % addressCount;
             InetAddress address = addresses[index];
             String host = address.getHostAddress();
 
             try {
                LOG.debug("Calling API at " + _host + '/' + host + " (attempt " + attempt + ").");
                URL url = new URL(_url.getProtocol(), host, _url.getPort(), _url.getFile());
                return requester.post(url, parameterStringBytes, _host);
             } catch (Exception exception) {
                LOG.warn("Failed to access " + host + '.', exception);
                lastException = exception;
             }
          }
       }
 
       String message = "Unable to access " + _url;
       LOG.error(message, lastException);
       throw new IOException(message);
    }
 
    public CallResult call(String sessionID, String functionName, Map parameters)
    throws IllegalArgumentException, IOException, InvalidCallResultException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("functionName", functionName);
 
       boolean debugEnabled = LOG.isDebugEnabled();
 
       // Prepare an HTTP request
       String parameterString = createParameterString(sessionID, functionName, parameters);
 
       // Execute the request
       if (debugEnabled) {
          LOG.debug("Posting to API: " + _url + '?' + parameterString);
       }
       HTTPRequester.Result result = performRequest(parameterString);
       int httpCode = result.getCode();
 
       // Evaluate the HTTP response code
       if (httpCode != 200) {
          throw new InvalidCallResultException("HTTP return code is " + httpCode + '.');
       }
 
       // Parse the result of the HTTP call
       return _callResultParser.parse(result.getString());
    }
 
    /**
     * Creates a parameter string for a HTTP GET call.
     *
     * @param sessionID
     *    the session identifier, if any, or <code>null</code>.
     *
     * @param functionName
     *    the name of the function to be called, not <code>null</code>.
     *
     * @param parameters
     *    the parameters to be passed, or <code>null</code>; keys must be
     *    {@link String Strings}, values can be of any class.
     *
     * @throws IllegalArgumentException
     *    if <code>functionName == null</code>.
     *
     * @return
     *    the string that can be used in an HTTP GET call, never
     *    <code>null</code> nor empty.
     */
    private final String createParameterString(String sessionID, String functionName, Map parameters)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("functionName", functionName);
 
       // Initialize a buffer
       StringBuffer buffer = new StringBuffer(PARAMETER_STRING_BUFFER_SIZE);
       buffer.append("_function=");
       buffer.append(functionName);
 
       // If there is a session identifier, process it
       if (sessionID != null) {
         buffer.append("_session=");
          buffer.append(sessionID);
       }
 
       // If there are parameters, then process them
       if (parameters != null) {
 
          // Loop through them all
          Iterator keys = parameters.keySet().iterator();
          while (keys.hasNext()) {
 
             // Get the parameter key
             String key = (String) keys.next();
 
             // The key cannot equal 'function'
             if ("function".equals(key)) {
                throw new IllegalArgumentException("The function parameter \"function\" cannot be used for a normal parameter.");
             }
 
             // TODO: Make sure the key does not start with an underscore
             // TODO: Make sure the key is properly formatted
             // TODO: URL encode the value
 
             // Add this parameter key/value combination
             Object value = parameters.get(key);
             if (value != null) {
                buffer.append('&');
                buffer.append(key);
                buffer.append('=');
                buffer.append(value);
             }
          }
       }
 
       return buffer.toString();
    }
 }
