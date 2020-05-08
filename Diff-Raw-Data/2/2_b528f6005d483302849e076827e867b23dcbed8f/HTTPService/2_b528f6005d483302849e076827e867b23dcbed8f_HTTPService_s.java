 /*
  * $Id$
  */
 package org.xins.util.service.http;
 
 import java.io.UnsupportedEncodingException;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.util.collections.PropertyReader;
 import org.xins.util.service.CallFailedException;
 import org.xins.util.service.CallResult;
 import org.xins.util.service.Descriptor;
 import org.xins.util.service.Service;
 import org.xins.util.service.ServiceDescriptor;
 
 /**
  * HTTP service accessor.
  *
  * <p>Subjects for this kind of service must be instances of class
  * {@link PropertyReader}.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.115
  */
 public final class HTTPService extends Service {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>HTTPService</code> object.
     *
     * @param descriptor
     *    the descriptor of the service, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>descriptor == null</code>.
     */
    public HTTPService(Descriptor descriptor, String baseURL)
    throws IllegalArgumentException {
       super(descriptor);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    public Result call(PropertyReader parameters)
    throws CallFailedException {
       CallResult callResult = doCall(parameters);
      return callResult.getResult();
    }
 
    protected Object doCallImpl(ServiceDescriptor target,
                                Object            subject)
    throws Throwable {
 
       // Convert subject to PropertyReader
       PropertyReader reader = (PropertyReader) subject;
 
       // Construct a new HTTP client object
       HttpClient client = new HttpClient();
 
       // Set the correct time-out
       client.setTimeout(target.getTimeOut());
 
       // Use the POST method
       // TODO: Allow configuration of method?
       HttpMethod method = new PostMethod(target.getURL());
 
       // Execute the request
       client.executeMethod(method);
 
       // Read response body (mandatory operation) and determine status
       byte[] data = method.getResponseBody();
       int    code = method.getStatusCode();
 
       // Release the connection
       // TODO: Do this in a finally section?
       method.releaseConnection();
 
       return new Result(code, data);
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * Result returned from an HTTP request.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
     *
     * @since XINS 0.115
     */
    public final class Result extends Object {
 
       //----------------------------------------------------------------------
       // Constructor
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>Result</code> object.
        *
        * @param code
        *    the HTTP return code, must be &gt;= 0.
        *
        * @param data
        *    the retrieved data, not <code>null</code>.
        *
        * @throws IllegalArgumentException
        *    if <code>code &lt; 0 || data == null</code>.
        */
       private Result(int code, byte[] data)
       throws IllegalArgumentException {
 
          // Check preconditions
          MandatoryArgumentChecker.check("data", data);
          if (code < 0) {
             throw new IllegalArgumentException("code (" + code + ") < 0");
          }
 
          _code = code;
          _data = data;
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       /**
        * The HTTP return code.
        */
       private final int _code;
 
       /**
        * The data returned.
        */
       private final byte[] _data;
 
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Returns the HTTP code.
        *
        * @return
        *    the HTTP return code.
        */
       public int getCode() {
          return _code;
       }
 
       /**
        * Returns the result data as a byte array. Note that this is not a copy
        * or clone of the internal data structure, but it is a link to the
        * actual data structure itself.
        *
        * @return
        *    a byte array of the result data, not <code>null</code>.
        */
       public byte[] getData() {
          return _data;
       }
 
       /**
        * Returns the returned data as a <code>String</code>. The encoding
        * <code>US-ASCII</code> is assumed.
        *
        * @return
        *    the result data as a text string, not <code>null</code>.
        */
       public String getString() {
          final String ENCODING = "US-ASCII";
          try {
             return getString(ENCODING);
          } catch (UnsupportedEncodingException exception) {
             throw new Error("Encoding \"" + ENCODING + "\" is unsupported.");
          }
       }
 
       /**
        * Returns the returned data as a <code>String</code> in the specified
        * encoding.
        *
        * @param encoding
        *    the encoding to use in the conversion from bytes to a text string,
        *    not <code>null</code>.
        *
        * @return
        *    the result data as a text string, not <code>null</code>.
        *
        * @throws UnsupportedEncodingException
        *    if the specified encoding is not supported.
        */
       public String getString(String encoding)
       throws UnsupportedEncodingException {
          byte[] bytes = getData();
          return new String(bytes, encoding);
       }
    }
 }
