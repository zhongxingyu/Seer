 /*
  * Copyright (c) 2011 Sergey Prilukin
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package anhttpserver;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Adapter which implements most methods of {@link SimpleHttpHandler}
  * So descendants should only implement method {@link SimpleHttpHandler#getResponse(HttpRequestContext)}
  *
  * @author Sergey Prilukin
  */
 public abstract class SimpleHttpHandlerAdapter implements SimpleHttpHandler {
     private Map<String, String> headers = new HashMap<String, String>();
 
     /**
      * Utility method which allows several response headers in one call.
      *
      * @param headers response headers which will be sent with response
      */
     protected void setResponseHeaders(Map<String, String> headers) {
         if (headers != null && headers.size() > 0) {
             this.headers.putAll(headers);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Map<String, String> getResponseHeaders() {
         return Collections.unmodifiableMap(headers);
     }
 
     /**
      * {@inheritDoc}
      */
     public void setResponseHeader(String name, String value) {
         headers.put(name, value);
     }
 
     /**
      * {@inheritDoc}
      *
      * If not overridden - returns status 200
      * which means HTTP OK
      */
     public int getResponseCode(HttpRequestContext httpRequestContext) {
         Object size = httpRequestContext.getAttribute(RESPONSE_CODE_ATTRIBUTE_KEY);
         if (size != null) {
             return (Integer)size;
         }
 
         return HttpURLConnection.HTTP_OK;
     }
 
     /**
      * Sets response code
      *
      * @param code code of the response {@see HttpURLConnection}
      * @param httpRequestContext instance of {@link HttpRequestContext} -
      *  facade for {@link com.sun.net.httpserver.HttpExchange}
      */
     protected void setResponseCode(int code, HttpRequestContext httpRequestContext) {
        httpRequestContext.setAttribute(RESPONSE_SIZE_ATTRIBUTE_KEY, code);
     }
 
     /**
      * Sets size of the reposnse
      *
      * @param size size of the response
      * @param httpRequestContext instance of {@link HttpRequestContext} -
      *  facade for {@link com.sun.net.httpserver.HttpExchange}
      */
     protected void setResponseSize(int size, HttpRequestContext httpRequestContext) {
         httpRequestContext.setAttribute(RESPONSE_SIZE_ATTRIBUTE_KEY, size);
     }
 
     /**
      * By default returns -1 which means that response size is unknown
      * @param httpRequestContext instance of {@link HttpRequestContext} -
      *  facade for {@link com.sun.net.httpserver.HttpExchange}
      * @return response size
      */
     public int getResponseSize(HttpRequestContext httpRequestContext) {
         Object size = httpRequestContext.getAttribute(RESPONSE_SIZE_ATTRIBUTE_KEY);
         if (size != null) {
             return (Integer)size;
         }
 
         // Do not allow your implementation to get there -
         // because your getResponse will be called twice.
         // this is only applicable for small responses
         try {
             return getResponse(httpRequestContext).length;
         } catch (IOException e) {
             return 0;
         }
     }
 
     /**
      * {@inheritDoc}
      *
      * An empty array returned by default
      *
      * @param httpRequestContext instance of {@link HttpRequestContext} -
      *  facade for {@link com.sun.net.httpserver.HttpExchange}
      * @return empty byte array
      * @throws IOException if exception occurs
      */
     public byte[] getResponse(HttpRequestContext httpRequestContext) throws IOException {
         return new byte[0];
     }
 
     /**
      * {@inheritDoc}
      *
      * In default implementation just call {@link #getResponse(HttpRequestContext)}
      * and convert it to InputStream.
      * This is OK for response of small length
      *
      * @param httpRequestContext instance of {@link HttpRequestContext} -
      *  facade for {@link com.sun.net.httpserver.HttpExchange}
      * @return {@link InputStream} with response
      * @throws IOException if exception occurs during getting response
      */
     public InputStream getResponseAsStream(HttpRequestContext httpRequestContext) throws IOException {
         return new ByteArrayInputStream(getResponse(httpRequestContext));
     }
 }
