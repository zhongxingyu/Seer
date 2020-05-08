 /*
  * $Id$
  */
 package org.xins.client;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.UnknownHostException;
 import java.net.URL;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.zip.Checksum;
 import java.util.zip.CRC32;
 import org.apache.log4j.Logger;
 import org.jdom.Document;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.util.http.HTTPRequester;
 import org.xins.util.net.URLEncoding;
 import org.xins.util.text.HexConverter;
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
     * Initial buffer size for a parameter string. See
     * {@link #createParameterString(String,String,Map)}.
     */
    private static int PARAMETER_STRING_BUFFER_SIZE = 256;
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Set the flag that indicates if at the construction of an
     * <code>ActualFunctionCaller</code> the backend API is called to see if it
     * is up.
     *
     * @param b
     *    the new value for the flag.
     *
     * @deprecated
     *    Deprecated since XINS 0.137. Use {@link #ping()} if necessary. This
     *    method is a no-op.
     */
    public static void setCallAtConstruction(boolean b) {
       // XXX: empty
    }
 
    /**
     * Computes the CRC-32 checksum for the specified URL.
     *
     * @param url
     *    the URL for which to compute the checksum, or <code>null</code>.
     *
     * @return
     *    the checksum for <code>url.</code>{@link URL#toString() toString()}.
     *
     * @throws IllegalArgumentException
     *    if <code>url == null</code>.
     */
    private int computeCRC32(URL url)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("url", url);
 
       // Get the string
       String string = url.toString();
 
       Checksum checksum = new CRC32();
       byte[] bytes;
       final String ENCODING = "US-ASCII";
       try {
          bytes = string.getBytes(ENCODING);
       } catch (UnsupportedEncodingException exception) {
          throw new Error("Encoding \"" + ENCODING + "\" is not supported.");
       }
       checksum.update(bytes, 0, bytes.length);
       return (int) (checksum.getValue() & 0x00000000ffffffffL);
    }
 
 
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
 
       String urlString = url.toString();
       if (debugEnabled) {
          if (hostName == null) {
             LOG.debug("Creating ActualFunctionCaller for URL: " + urlString);
          } else {
             LOG.debug("Creating ActualFunctionCaller for URL: " + urlString + ", hostname: " + hostName);
          }
       }
 
       // Perform DNS lookup
       String urlHostName = url.getHost();
       InetAddress[] addresses = InetAddress.getAllByName(urlHostName);
       if (addresses.length > 1) {
          throw new MultipleIPAddressesException(urlHostName, addresses);
       }
 
       // Construct the internal URL, with absolute IP address, so no DNS
       // lookups will be necessary anymore
       try {
          _url = new URL(url.getProtocol(),             // protocol
                         addresses[0].getHostAddress(), // host
                         url.getPort(),                 // port
                         url.getFile());                // file
       } catch (MalformedURLException mue) {
          throw new Error("Caught MalformedURLException for a protocol that was previously accepted: \"" + url.getProtocol() + "\".");
       }
 
       // Initialize fields
       _hostName         = (hostName != null) ? hostName : urlHostName;
       _callResultParser = new CallResultParser();
       _crc32            = computeCRC32(_url);
      _crc32String      = HexConverter.toHexString(((long) _crc32) & 0x00000000ffffffffL); // TODO: Not convert to long

       _urlString        = urlString;
 
       // Log message
       if (debugEnabled) {
          LOG.debug("Created ActualFunctionCaller for URL \"" + _urlString + "\", CRC-32 is " + _crc32String + '.');
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The URL for the API this object represents. This field is never
     * <code>null</code>.
     */
    private final URL _url;
 
    /**
     * The URL for the API, as a string. This field is never <code>null</code>.
     */
    private final String _urlString;
 
    /**
     * The host name as passed to the constructor in the URL. This field is
     * never <code>null</code>.
     */
    private final String _hostName;
 
    /**
     * Call result parser. This field cannot be <code>null</code>.
     */
    private final CallResultParser _callResultParser;
 
    /**
     * The CRC-32 checksum for the URL.
     */
    private final int _crc32;
 
    /**
     * The CRC-32 checksum for the URL, as a String.
     */
    private final String _crc32String;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
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
    private final String createParameterString(String sessionID,
                                               String functionName,
                                               Map    parameters)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("functionName", functionName);
 
       // Initialize a buffer
       // TODO: Use _function
       FastStringBuffer buffer = new FastStringBuffer(PARAMETER_STRING_BUFFER_SIZE);
       buffer.append("function=");
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
 
             // Process key only if it is not null and not an empty string
             if (key != null && key.length() > 0) {
 
                // The key cannot start with an underscore
                if (key.charAt(0) == '_') {
                   throw new IllegalArgumentException("The parameter key \"" + key + "\" is invalid, since it cannot start with an underscore.");
 
                // The key cannot equal 'function'
                } else if ("function".equals(key)) {
                   throw new IllegalArgumentException("The parameter key \"function\" is invalid, since \"function\" is a reserved word.");
                }
 
                // TODO: Make sure the key is properly formatted
 
                // Get the value
                Object value = parameters.get(key);
 
                // Add this parameter key/value combination
                if (value != null) {
 
                   // Convert the value object to a string
                   String valueString = value.toString();
 
                   // Only add the key/value combo if there is a value string
                   if (valueString != null && valueString.length() > 0) {
                      buffer.append('&');
                      buffer.append(URLEncoding.encode(key));
                      buffer.append('=');
                      buffer.append(URLEncoding.encode(valueString));
                   }
                }
             }
          }
       }
 
       return buffer.toString();
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
          String message = "Failed to call " + _url.toString() + '?' + parameterString + " due to " + exception.getClass().getName() + ", message is: \"" + exception.getMessage() + "\".";
          LOG.error(message);
          throw new IOException(message);
       }
    }
 
    /**
     * Returns the URL for the API this object represents.
     *
     * @return
     *    the URL, never <code>null</code>.
     */
    public URL getURL() {
       return _url;
    }
 
    public CallResult call(String sessionID, String functionName, Map parameters)
    throws IllegalArgumentException,
           CallIOException,
           InvalidCallResultException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("functionName", functionName);
 
       boolean debugEnabled = LOG.isDebugEnabled();
 
       // Prepare an HTTP request
       String parameterString = createParameterString(sessionID, functionName, parameters);
 
       // Execute the request
       if (debugEnabled) {
          LOG.debug("Posting to API: " + _url + '?' + parameterString);
       }
       HTTPRequester.Result result;
       try {
          result = performRequest(parameterString);
       } catch (IOException ioException) {
          throw new CallIOException(ioException);
       }
       int httpCode = result.getCode();
 
       // Evaluate the HTTP response code
       if (httpCode != 200) {
          throw new InvalidCallResultException("HTTP return code is " + httpCode + '.');
       }
 
       // Parse the result of the HTTP call
       try {
          return _callResultParser.parse(this, result.getString());
       } catch (IOException ioException) {
          throw new CallIOException(ioException);
       } catch (ParseException exception) {
          throw new InvalidCallResultException(exception.getMessage(), exception.getCauseException());
       }
    }
 
    /**
     * Returns the CRC-32 checksum for the URL of this function caller.
     *
     * @return
     *    the CRC-32 checksum.
     */
    public long getCRC32() {
       return (long) _crc32;
    }
 
    /**
     * Returns the CRC-32 checksum for the URL of this function caller, as a
     * String.
     *
     * @return
     *    the CRC-32 checksum, as a {@link String} containing an unsigned hex
     *    number.
     */
    public String getCRC32String() {
       return _crc32String;
    }
 
    public ActualFunctionCaller getActualFunctionCallerByCRC32(String crc32)
    throws IllegalArgumentException {
       MandatoryArgumentChecker.check("crc32", crc32);
       return _crc32String.equals(crc32) ? this : null;
    }
 
    /**
     * Pings the API. This is done by calling the <strong>_NoOp</strong>
     * function. The call result is ignored.
     *
     * @throws CallIOException
     *    if the call failed due to an I/O error.
     *
     * @throws InvalidCallResultException
     *    if the call result was not a valid XINS result document.
     *
     * @since XINS 0.137
     */
    public void ping()
    throws CallIOException, InvalidCallResultException {
       call(null, "_NoOp", null);
    }
 
    /**
     * Returns the remote XINS version.
     *
     * @return
     *    the remote XINS version, or <code>null</code> if it could not be
     *    retrieved.
     *
     * @throws CallIOException
     *    if the call failed due to an I/O error.
     *
     * @throws InvalidCallResultException
     *    if the call result was not a valid XINS result document.
     *
     * @throws UnsuccessfulCallException
     *    if the call was unsuccessful.
     *
     * @since XINS 0.137
     */
    public String getRemoteXINSVersion()
    throws CallIOException,
           InvalidCallResultException,
           UnsuccessfulCallException {
 
       // Call the function
       CallResult result = call(null, "_GetVersion", null);
 
       // The call must be successful
       if (!result.isSuccess()) {
          throw new UnsuccessfulCallException(result);
       }
 
       // Return the 'xins.version' parameter value
       String version = result.getParameter("xins.version");
       if (version == null || version.trim().length() < 1) {
          return null;
       } else {
          return version;
       }
    }
 }
