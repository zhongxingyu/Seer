 /*
  * $Id$
  */
 package org.xins.client;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.MalformedURLException;
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
 import org.xins.util.text.FastStringBuffer;
 
 /**
  * Function caller implementation that actually sends an HTTP request to a
  * remote XINS API.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.41
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
     * specified URL. If the host part of the URL (see {@link URL#getHost()})
     * is a host name, it will be looked up immediately. This function caller
     * will only store IP addresses, not host names.
     *
     * @param url
     *    the URL for the API, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>url == null</code>.
     *
     * @throws SecurityException
     *    if a security manager does not allow the DNS lookup operation for the
     *    host specified in the URL.
     *
     * @throws UnknownHostException
     *    if no IP address could be found for the host specified in the URL.
     *
     * @throws MultipleIPAddressesException
     *    if the host specified in the URL resolves to multiple IP addresses.
     */
    public ActualFunctionCaller(URL url)
    throws IllegalArgumentException,
           SecurityException,
           UnknownHostException,
           MultipleIPAddressesException {
       this(url, null);
    }
 
    /**
     * Creates a <code>ActualFunctionCaller</code> object for a XINS API at the
     * specified URL, optionally specifying the host name to send with HTTP 1.1
     * requests. If the host part of the URL (see {@link URL#getHost()})
     * is a host name, it will be looked up immediately. This function caller
     * will only store IP addresses, not host names.
     *
     * @param url
     *    the URL for the API, not <code>null</code>.
     *
     * @param hostName
     *    the host name to send down to the API, or <code>null</code> if
     *    <code>url.getHost()</code> should be used.
     *
     * @throws IllegalArgumentException
     *    if <code>url == null</code>.
     *
     * @throws SecurityException
     *    if a security manager does not allow the DNS lookup operation for the
     *    host specified in the URL.
     *
     * @throws UnknownHostException
     *    if no IP address could be found for the host specified in the URL.
     *
     * @throws MultipleIPAddressesException
     *    if the host specified in the URL resolves to multiple IP addresses.
     */
    public ActualFunctionCaller(URL url, String hostName)
    throws IllegalArgumentException,
           SecurityException,
           UnknownHostException,
           MultipleIPAddressesException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("url", url);
 
       boolean debugEnabled = LOG.isDebugEnabled();
 
       // Get an instance number and increase the count
       _instanceNum = INSTANCE_COUNT++;
 
       if (debugEnabled) {
          if (hostName == null) {
             LOG.debug("Creating ActualFunctionCaller #" + _instanceNum + " for URL: " + url);
          } else {
             LOG.debug("Creating ActualFunctionCaller #" + _instanceNum + " for URL: " + url + ", hostname: " + hostName);
          }
       }
 
       // Perform DNS lookup
       String urlHostName = url.getHost();
       InetAddress[] addresses = InetAddress.getAllByName(urlHostName);
       if (addresses.length > 1) {
          throw new MultipleIPAddressesException(); // TODO: Pass host name and addresses
       }
 
       // Construct the internal URL, with absolute IP address, so no DNS
       // lookups will be necessary anymore
       try {
          _url = new URL(url.getProtocol(),             // protocol
                         addresses[0].getHostAddress(), // host
                         url.getPort(),                 // port
                         url.getFile());                // file
       } catch (MalformedURLException mue) {
          throw new InternalError("Caught MalformedURLException for a protocol that was previously accepted: \"" + url.getProtocol() + "\".");
       }
 
       // Initialize fields
       _hostName         = (hostName != null) ? hostName : urlHostName;
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
     * The host name as passed to the constructor in the URL. This field is
     * never <code>null</code>.
     */
    private final String _hostName;
 
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
       byte[] parameterStringBytes = parameterString.getBytes("US-ASCII");
 
       if (LOG.isDebugEnabled()) {
          LOG.debug("Calling " + _url.toString() + '?' + parameterString);
       }
 
       try {
          return requester.post(_url, parameterStringBytes, _hostName);
       } catch (Throwable exception) {
          String message = "Failed to call " + _url.toString() + '?' + parameterStringBytes;
          LOG.error(message, exception);
          throw new IOException(message);
       }
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
       try {
          return _callResultParser.parse(result.getString());
       } catch (ParseException exception) {
          throw new InvalidCallResultException(exception.getMessage(), exception.getCause());
       }
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
       FastStringBuffer buffer = new FastStringBuffer(PARAMETER_STRING_BUFFER_SIZE);
       buffer.append("_function=");
       buffer.append(functionName);
 
       // If there is a session identifier, process it
       if (sessionID != null) {
          buffer.append("&_session=");
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
                buffer.append(value.toString());
             }
          }
       }
 
       return buffer.toString();
    }
 }
