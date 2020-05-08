 /*
  * Funambol is a mobile platform developed by Funambol, Inc. 
  * Copyright (C) 2010 Funambol, Inc.
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation with the addition of the following permission 
  * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
  * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE 
  * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Affero General Public License 
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  * 
  * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite 
  * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
  * 
  * The interactive user interfaces in modified source and object code versions
  * of this program must display Appropriate Legal Notices, as required under
  * Section 5 of the GNU Affero General Public License version 3.
  * 
  * In accordance with Section 7(b) of the GNU Affero General Public License
  * version 3, these Appropriate Legal Notices must retain the display of the
  * "Powered by Funambol" logo. If the display of the logo is not reasonably 
  * feasible for technical reasons, the Appropriate Legal Notices must display
  * the words "Powered by Funambol". 
  */
 
 package com.funambol.platform;
 
 import java.net.HttpURLConnection;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ByteArrayInputStream;
 import java.io.OutputStream;
 import java.util.Map;
 import java.util.HashMap;
 
 //import android.net.http.AndroidHttpClient;
 
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.StatusLine;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpEntity;
 import org.apache.http.Header;
 import org.apache.http.HttpHost;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.conn.params.ConnRouteParams;
 import org.apache.http.entity.InputStreamEntity;
 
 
 import com.funambol.platform.net.ProxyConfig;
 import com.funambol.util.Log;
 
 /**
  * This class is a simple HttpConnection class that wraps
  *
  * A portable code must use this class only to perform http connections, and must take care
  * of closing the connection when not used anymore.
  * <pre>
  * Example:
  * 
  *   void httpConnectionExample(String url) throws IOException {
  *      HttpConnectionAdapter conn = new HttpConnectionAdapter();
  *
  *      // Open the connection
  *      conn.open(url);
  *
  *      conn.setRequestMethod(HttpConnectionAdapter.POST);
  *      conn.setRequestProperty("CUSTOM-HEADER", "CUSTOM-VALUE");
  *
  *      OutputStream os = conn.openOutputStream();
  *      os.write("TEST");
  *      os.close();
  *
  *      // Suppose the answer is bound to 1KB
  *      byte anwser[] = new byte[1024];
  *      InputStream is = conn.openInputStream();
  *      is.read(answer);
  *      is.close();
  *
  *      // Close the connection
  *      conn.close();
  * </pre>
  */
 public class HttpConnectionAdapter {
 
     private static final String TAG_LOG         = "HttpConnectionAdapter";
 
     public static int HTTP_ACCEPTED             = HttpURLConnection.HTTP_ACCEPTED;
     public static int HTTP_BAD_GATEWAY          = HttpURLConnection.HTTP_BAD_GATEWAY;
     public static int HTTP_BAD_METHOD           = HttpURLConnection.HTTP_BAD_METHOD;
     public static int HTTP_BAD_REQUEST          = HttpURLConnection.HTTP_BAD_REQUEST;
     public static int HTTP_CLIENT_TIMEOUT       = HttpURLConnection.HTTP_CLIENT_TIMEOUT;
     public static int HTTP_CONFLICT             = HttpURLConnection.HTTP_CONFLICT;
     public static int HTTP_CREATED              = HttpURLConnection.HTTP_CREATED;
     public static int HTTP_ENTITY_TOO_LARGE     = HttpURLConnection.HTTP_ENTITY_TOO_LARGE;
     public static int HTTP_FORBIDDEN            = HttpURLConnection.HTTP_FORBIDDEN;
     public static int HTTP_GATEWAY_TIMEOUT      = HttpURLConnection.HTTP_GATEWAY_TIMEOUT;
     public static int HTTP_GONE                 = HttpURLConnection.HTTP_GONE;
     public static int HTTP_INTERNAL_ERROR       = HttpURLConnection.HTTP_INTERNAL_ERROR;
     public static int HTTP_LENGTH_REQUIRED      = HttpURLConnection.HTTP_LENGTH_REQUIRED;
     public static int HTTP_MOVED_PERM           = HttpURLConnection.HTTP_MOVED_PERM;
     public static int HTTP_MOVED_TEMP           = HttpURLConnection.HTTP_MOVED_TEMP;
     public static int HTTP_MULT_CHOICE          = HttpURLConnection.HTTP_MULT_CHOICE;
     public static int HTTP_NO_CONTENT           = HttpURLConnection.HTTP_NO_CONTENT;
     public static int HTTP_NOT_ACCEPTABLE       = HttpURLConnection.HTTP_NOT_ACCEPTABLE;
     public static int HTTP_NOT_AUTHORITATIVE    = HttpURLConnection.HTTP_NOT_AUTHORITATIVE;
     public static int HTTP_NOT_FOUND            = HttpURLConnection.HTTP_NOT_FOUND;
     public static int HTTP_NOT_IMPLEMENTED      = HttpURLConnection.HTTP_NOT_IMPLEMENTED;
     public static int HTTP_NOT_MODIFIED         = HttpURLConnection.HTTP_NOT_MODIFIED;
     public static int HTTP_OK                   = HttpURLConnection.HTTP_OK;
     public static int HTTP_PARTIAL              = HttpURLConnection.HTTP_PARTIAL;
     public static int HTTP_PAYMENT_REQUIRED     = HttpURLConnection.HTTP_PAYMENT_REQUIRED;
     public static int HTTP_PRECON_FAILED        = HttpURLConnection.HTTP_PRECON_FAILED;
     public static int HTTP_PROXY_AUTH           = HttpURLConnection.HTTP_PROXY_AUTH;
     public static int HTTP_REQ_TOO_LONG         = HttpURLConnection.HTTP_REQ_TOO_LONG;
     public static int HTTP_RESET                = HttpURLConnection.HTTP_RESET;
     public static int HTTP_SEE_OTHER            = HttpURLConnection.HTTP_SEE_OTHER;
     public static int HTTP_UNAUTHORIZED         = HttpURLConnection.HTTP_UNAUTHORIZED;
     public static int HTTP_UNAVAILABLE          = HttpURLConnection.HTTP_UNAVAILABLE;
     public static int HTTP_UNSUPPORTED_TYPE     = HttpURLConnection.HTTP_UNSUPPORTED_TYPE;
     public static int HTTP_USE_PROXY            = HttpURLConnection.HTTP_USE_PROXY;
     public static int HTTP_VERSION              = HttpURLConnection.HTTP_VERSION;
 
     public static int HTTP_EXPECT_FAILED        = 417;
     public static int HTTP_UNSUPPORTED_RANGE    = 416;
     public static int HTTP_TEMP_REDIRECT        = 307;
 
     // These are the constants that can be specified in the setRequestMethod
     public static final String GET              = "GET";
     public static final String POST             = "POST";
     public static final String PUT              = "PUT";
     public static final String HEAD             = "HEAD";
 
     private static final String HTTP_DEFAULT_PORT  = "80";
     private static final String HTTPS_DEFAULT_PORT = "443";
 
     private Map<String,String> requestHeaders;
     private Header responseHeaders[];
     
     private String requestMethod = GET;
 
     private HttpRequestBase request;
     private DefaultHttpClient httpClient = new DefaultHttpClient();
     private int responseCode;
     private OutputStream outputStream;
     private String url;
     private HttpResponse httpResponse = null;
     private InputStream respInputStream;
     private ProxyConfig proxyConfig;
     private int chunkLength = -1;
 
     public HttpConnectionAdapter() {
         HttpParams params = new BasicHttpParams();
         //params.(SOCKET_BUFFER_SIZE, 8192);
         //SchemeRegistry registry = new SchemeRegistry();
         //registry.register(new Scheme("http", new KeepAliveSocketFactory(), 80));
         //httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(params, registry), params);
         httpClient = new DefaultHttpClient(params);
     }
 
     /**
      * Open the connection to the given url.
      */
     public void open(String url, ProxyConfig proxyConfig) throws IOException {
         this.url = url;
         this.proxyConfig = proxyConfig;
     }
 
 
     /**
      * This method closes this connection. It does not close the corresponding
      * input and output stream which need to be closed separately (if they were
      * previously opened)
      *
      * @throws IOException if the connection cannot be closed
      */
     public void close() throws IOException {
         if (requestHeaders != null) {
             requestHeaders.clear();
         }
     }
 
     /**
      * Open the input stream. The ownership of the stream is transferred to the
     * caller which is responsible to close and release the resource once it is
      * no longer used. This method shall be called only once per connection.
      *
      * @throws IOException if the input stream cannot be opened or the output
      * stream has not been closed yet.
      */
     public InputStream openInputStream() throws IOException {
 
         if (request == null) {
             // This is a GET request
             HttpGet req = new HttpGet(url);
             performRequest(req);
         }
         return respInputStream;
     }
 
     /*
     public void execute(InputStream is) throws IOException {
         execute(is, -1);
     }
     */
 
     /**
      * Open the output stream. The ownership of the stream is transferred to the
     * caller which is responsible to close and release the resource once it is
      * no longer used. This method shall be called only once per connection.
      *
      * @throws IOException if the output stream cannot be opened.
      */
     public void execute(InputStream is, long length) throws IOException {
 
         Log.debug(TAG_LOG, "Opening url: " + url);
         Log.debug(TAG_LOG, "Opening with method " + requestMethod);
 
         String contentType = null;
         if (requestHeaders != null) {
             contentType = requestHeaders.get("Content-Type");
         }
         if (contentType == null) {
             contentType = "binary/octet-stream";
         }
 
         if (POST.equals(requestMethod)) {
             request = new HttpPost(url);
             if (is != null) {
                 InputStreamEntity reqEntity = new InputStreamEntity(is, length); 
                 reqEntity.setContentType(contentType); 
                 if (chunkLength > 0) {
                     reqEntity.setChunked(true); 
                 }
                 ((HttpPost)request).setEntity(reqEntity);
             }
         } else if (PUT.equals(requestMethod)) {
             request = new HttpPut(url);
             if (is != null) {
                 InputStreamEntity reqEntity = new InputStreamEntity(is, length); 
                 reqEntity.setContentType(contentType); 
                 if (chunkLength > 0) {
                     reqEntity.setChunked(true); 
                 }
                 ((HttpPost)request).setEntity(reqEntity);
             }
         } else {
             request = new HttpGet(url);
         }
 
         performRequest(request);
     }
 
     /**
      * Returns the HTTP response status code. It parses responses like:
      *
      * HTTP/1.0 200 OK
      * HTTP/1.0 401 Unauthorized
      * 
      * and extracts the ints 200 and 401 respectively. from the response (i.e., the response is not valid HTTP).
      *
      * Returns:
      *   the HTTP Status-Code or -1 if no status code can be discerned. 
      * Throws:
      *   IOException - if an error occurred connecting to the server.
      */
     public int getResponseCode() throws IOException {
         return responseCode;
     }
 
     /**
      * Returns the HTTP response message. It parses responses like:
      *
      * HTTP/1.0 200 OK
      * HTTP/1.0 401 Unauthorized
      * 
      * and extracts the strings OK and Unauthorized respectively. from the response (i.e., the response is not valid HTTP).
      *
      * Returns:
      *   the HTTP Response-Code or null if no status message can be discerned. 
      * Throws:
      *   IOException - if an error occurred connecting to the server.
      */
     public String getResponseMessage() throws IOException {
         return null;
     }
 
 
     /**
      * Set the method for the URL request, one of:
      * GET
      * POST
      * HEAD 
      * are legal, subject to protocol restrictions. The default method is GET. 
      */
     public void setRequestMethod(String method) throws IOException {
         requestMethod = method;
     }
 
     /**
      * Set chunked encoding for the content to be uploaded. This avoid the output
      * stream to buffer all data before transmitting it.
      * This is currently not supported by this implementation and the method has
      * no effect because httpclient performs chunking by default.
      *
      * @param chunkLength the length of the single chunk
      */
     public void setChunkedStreamingMode(int chunkLength) throws IOException {
         this.chunkLength = chunkLength;
     }
     
     /**
      * Sets the general request property. If a property with the key already exists,
      * overwrite its value with the new value.
      *
      * NOTE: HTTP requires all request properties which can legally have multiple instances
      * with the same key to use a comma-seperated list syntax which enables multiple
      * properties to be appended into a single property.
      *
      * @param key the keyword by which the request is known (e.g., "accept").
      * @param value the value associated with it.
      */
     public void setRequestProperty(String key, String value) throws IOException {
         if (requestHeaders == null) {
             requestHeaders = new HashMap<String, String>();
         }
         requestHeaders.put(key, value);
     }
 
     /**
      * Returns the value of the named header field.
      *
      * @param key name of a header field.
      * @return the value of the named header field, or null if there is no such field in the header.
      * @throws IOException if an error occurred connecting to the server.
      */
     public String getHeaderField(String key) throws IOException {
         if (request != null) {
 
 
             //////////////////////////////////////////////////////////////
             Header headers[] = httpResponse.getHeaders(key);
             if (headers != null) {
                 for(int i=0;i<headers.length;++i) {
                     Header h = headers[i];
                     Log.error("MARCO", "Header " + key + " has value " + h.getValue());
                 }
             }
             //////////////////////////////////////////////////////////////
 
             Header header = httpResponse.getFirstHeader(key);
             if (header != null) {
                 return header.getValue();
             }
         }
         return null;
     }
 
     /**
      * Returns the key for the nth header field. Some implementations may treat the
      * 0th header field as special, i.e. as the status line returned by the HTTP server.
      * In this case, getHeaderField(0)  returns the status line, but getHeaderFieldKey(0) returns null.
      */
     public String getHeaderFieldKey(int num) throws IOException {
         if (num < responseHeaders.length) {
             Header header = responseHeaders[num];
             return header.getName();
         } else {
             return null;
         }
     }
 
 
     /**
      * Returns the answer length (excluding headers. This is the content-length
      * field length)
      */
     public int getLength() throws IOException {
         String len = getHeaderField("content-length");
         if (len == null) {
             len = getHeaderField("Content-Length");
         }
         if (len != null) {
             try {
                 return Integer.parseInt(len);
             } catch (Exception e) {
                 return -1;
             }
         } else {
             return -1;
         }
     }
 
     /**
      * Sets the timeout value in milliseconds for establishing the connection
      * to the pointed resource. A {@link SocketTimeoutException} is thrown if
      * the connection could not be established in this time. Default is 0 which
      * stands for an infinite timeout.
      * 
      * @param timeout the connecting timeout in milliseconds.
      * @throws IllegalArgumentException if the parameter <code>timeout</code> is less than zero. 
      */
     public void setConnectTimeout(int timeout) throws IllegalArgumentException {
     }
     
     /**
      * Sets the timeout value in milliseconds for reading from the input stream
      * of an established connection to the resource. A {@link SocketTimeoutException}
      * is thrown if the connection could not be established in this time. Default is
      * 0 which stands for an infinite timeout.
      * 
      * @param timeout the reading timeout in milliseconds.
      * @throws IllegalArgumentException if the parameter <code>timeout</code> is less than zero. 
      */
     public void setReadTimeout(int timeout) throws IllegalArgumentException {
     }
 
     /**
      * Returns the error input stream pointer
      */
     public InputStream getErrorStream() throws IOException {
         return new ByteArrayInputStream("".getBytes());
     }
 
     /**
      * Establishes the connection to the earlier configured resource.
      * The connection can only be set up before this method has been called.
      */
     public void connect() throws IOException {
     }
 
     private void performRequest(HttpRequestBase req) throws IOException {
         // Set all the headers
         if (requestHeaders != null) {
             for(String key : requestHeaders.keySet()) {
                 String value = requestHeaders.get(key);
                 // The content length is set by httpclient and it is feteched
                 // from the request stream
                 if (!"Content-Length".equals(key)) {
                     Log.trace(TAG_LOG, "Setting header: " + key + "=" + value);
                     req.addHeader(key, value);
                 }
             }
         }
 
         // Set the proxy if necessary
         if (proxyConfig != null) {
             HttpParams params = new BasicHttpParams();
             ConnRouteParams.setDefaultProxy(params, new HttpHost(proxyConfig.getAddress(), proxyConfig.getPort()));
             httpClient.setParams(params);
         }
 
         try {
             Log.trace(TAG_LOG, "Executing request");
             httpResponse = httpClient.execute(req);
         } catch (Exception e) {
             Log.error(TAG_LOG, "Exception while executing request", e);
             throw new IOException(e.toString());
         }
         // Set the response
         StatusLine statusLine = httpResponse.getStatusLine();
         responseCode = statusLine.getStatusCode();
 
         HttpEntity respEntity = httpResponse.getEntity();
         if (respEntity != null) {
             respInputStream = respEntity.getContent();
         }
         responseHeaders = httpResponse.getAllHeaders();
     }
 
 }
 
 
