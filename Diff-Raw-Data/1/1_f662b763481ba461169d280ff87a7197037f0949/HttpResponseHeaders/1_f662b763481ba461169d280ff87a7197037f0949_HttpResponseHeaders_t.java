 /*
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  *  Copyright (c) 2011, Janrain, Inc.
  *
  *  All rights reserved.
  *
  *  Redistribution and use in source and binary forms, with or without modification,
  *  are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, this
  *    list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation and/or
  *    other materials provided with the distribution.
  *  * Neither the name of the Janrain, Inc. nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  *
  *
  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
  *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  */
 
 
 package com.janrain.android.engage.net.async;
 
import com.janrain.android.engage.types.JRDictionary;
 import com.janrain.android.utils.LogUtils;
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.cookie.CookieOrigin;
 import org.apache.http.cookie.CookieSpec;
 import org.apache.http.cookie.MalformedCookieException;
 import org.apache.http.impl.cookie.BestMatchSpec;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @class HttpResponseHeaders
  **/
 public class HttpResponseHeaders {
     private static final String TAG = HttpResponseHeaders.class.getSimpleName();
 
     public static final String HEADER_LAST_MODIFIED = "Last-Modified";
     public static final String HEADER_ETAG = "ETag";
     public static final String HEADER_CONTENT_ENCODING = "content-encoding";
     public static final String HEADER_CONTENT_TYPE = "content-type";
     public static final String HEADER_CONTENT_LENGTH = "content-length";
     public static final int RESPONSE_CODE_INVALID = -1;
 
     private int mResponseCode;
     private String mContentEncoding;
     private int mContentLength;
     private String mContentType;
     private long mLastModified;
     private String mLastModifiedUtc;
     private String mETag;
     private HttpUriRequest mRequest;
     private HttpResponse mResponse;
 
     private HttpResponseHeaders() {
         mResponseCode = RESPONSE_CODE_INVALID;
     }
 
     /**
      * @internal
      * Constructs a new HttpResponseHeaders instance from an HttpResponse
      * @param response
      *   The HttpResponse from which to copy the values used to construct a new instance
      * @param request
      *   The HttpRequest from which to use the host, port, path, and secureness to construct the Cookies
      *   parsed from the response.
      * @return
      *   The new instance
      */
     public static HttpResponseHeaders fromResponse(HttpResponse response, HttpUriRequest request) {
         LogUtils.logd(TAG, "[fromResponse] BEGIN");
 
         HttpResponseHeaders headers = new HttpResponseHeaders();
 
         headers.mResponseCode = response.getStatusLine().getStatusCode();
         headers.mContentEncoding = getResponseHeaderFirstValue(response, HEADER_CONTENT_ENCODING);
         try {
             headers.mContentLength = Integer.parseInt(
                     getResponseHeaderFirstValue(response, HEADER_CONTENT_LENGTH));
         } catch (NumberFormatException e) {
             headers.mContentLength = -1;
         }
         headers.mContentType = getResponseHeaderFirstValue(response, HEADER_CONTENT_TYPE);
         headers.mLastModified = getResponseLastModified(response);
         headers.mLastModifiedUtc = getResponseHeaderFirstValue(response, HEADER_LAST_MODIFIED);
         headers.mETag = getResponseHeaderFirstValue(response, HEADER_ETAG);
         headers.mResponse = response;
         headers.mRequest = request;
 
         return headers;
     }
 
     private static String getResponseHeaderFirstValue(HttpResponse response, String headerName) {
         Header[] h = response.getHeaders(headerName);
         if (h.length > 0) return h[0].getValue();
         return null;
     }
 
     private static long getResponseLastModified(HttpResponse response) {
         String lastModified = getResponseHeaderFirstValue(response, "last-modified");
         if (lastModified != null) {
             SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
             try {
                 return format.parse(lastModified).getTime();
             } catch (ParseException e) {
                 return 0;
             }
         }
         return 0;
     }
 
     /**
      * Returns the &lt;em>first&lt;/em> header field's value, given the header field's name. Case
      * insensitive.
      * @param headerFieldName
      *  The name of the header field
      * @return
      *  The value if the header field is present, or null otherwise
      */
     public String getHeaderField(String headerFieldName) {
         return getResponseHeaderFirstValue(mResponse, headerFieldName);
     }
 
     /**
      * Returns an array of Apache HttpClient Cookies found in this HttpResponseHeaders, parsed with the
      * Apache BestMatchSpec implementation.
      * @return
      *  The array of cookies set
      */
     public Cookie[] getCookies() {
         return getApacheCookies(new BestMatchSpec());
     }
 
     /**
      * Returns an array of Apache HttpClient Header objects contained by this HttpResponseHeaders object.
      * @return 
      *  The contained headers
      */
     public Header[] getHeaders() {
         return mResponse.getAllHeaders();
     }
 
     /**
      * @internal
      * Returns an array of Cookies set in this HttpResponseHeaders, parsed with the given CookieSpec
      * implementation.
      * @param spec
      *  The CookieSpec used to parse the "set-cookie" headers
      * @return
      *  The array of cookies set
      */
     private Cookie[] getApacheCookies(CookieSpec spec) {
         Header[] headers = mResponse.getHeaders("set-cookie");
         List<Cookie> cookies = new ArrayList<Cookie>();
         String host = mRequest.getURI().getHost();
         int port = mRequest.getURI().getPort();
         if (port == -1) port = 80;
         String path = mRequest.getURI().getPath();
         boolean secure = mRequest.getURI().getScheme().equalsIgnoreCase("https");
         for (Header h : headers) {
             try {
                 cookies.addAll(spec.parse(h, new CookieOrigin(host, port, path, secure)));
             } catch (MalformedCookieException e) {
                 throw new RuntimeException(e);
             }
         }
 
         return cookies.toArray(new Cookie[cookies.size()]);
     }
 
     /**
      * Returns the response code found in this instance
      * @return
      *  The response code
      */
     public int getResponseCode() {
         return mResponseCode;
     }
 
     /**
      * Returns the value of the Content-Encoding header.
      * Note: that this is not the the encoding of the text in the response entity, but it is rather the
      * encoding of the entity. (E.g. gzip/compress/deflate)
      * @return
      *  The Content-Encoding header field's value if present, null otherwise.
      */
     public String getContentEncoding() {
         return mContentEncoding;
     }
 
     /**
      * Returns the length of the response as described by the &lt;code>content-length&lt;/code> header.
      * @return
      *  The value of the header field, or -1 otherwise
      */
     public int getContentLength() {
         return mContentLength;
     }
 
     /**
      * Returns the content type of the response as described by the &lt;code>content-type&lt;/code> header.
      * @return
      *  The value of the header field if present, null otherwise.
      */
     public String getContentType() {
         return mContentType;
     }
 
     /**
      * @deprecated
      * This was the value of URLConnection#getDate, but is now broken.
      * @return
      *  Returns -1;
      */
     public long getDate() {
         return -1;
     }
 
     /**
      * Returns the date of the response as described by the &lt;code>last-modified&lt;/code> header.
      * @return
      *  The value of the header field as a time milliseconds since January 1, 1970 GMT or 0 if this
      *  timestamp is unknown.
      */
     public long getLastModified() {
         return mLastModified;
     }
 
     /**
      * Returns the date of the response as described by the &lt;code>last-modified&lt;/code> header.
      * @return
      *  The value of the header field if present, or null otherwise.
      */
     public String getLastModifiedUtc() {
         return mLastModifiedUtc;
     }
 
     /**
      * Returns the ETag of the response as described by the &lt;code>ETag&lt;/code> header.
      * @return
      *  The value of the header field if present, or null otherwise.
      */
     public String getETag() {
         return mETag;
     }
 
     /**
      * Produces a String representation of this object.
      * @return
      *  A string representation of this object suitable human inspection.
      */
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder("HttpResponseHeaders [");
         sb.append(" Response Code: ").append(mResponseCode);
         sb.append(" | Content Encoding: ").append(mContentEncoding);
         sb.append(" | Content Length: ").append(mContentLength);
         sb.append(" | Content Type: ").append(mContentType);
         sb.append(" | Content Last Modified: ").append(mLastModified);
         sb.append(" | Content Last Modified UTC: ").append(mLastModifiedUtc);
         sb.append(" | ETag: ").append(mETag);
         sb.append(" ]");
         return sb.toString();
     }
 
     public JRDictionary toJRDictionary() {
         JRDictionary retval = new JRDictionary();
         for (Header h : mResponse.getAllHeaders()) retval.put(h.getName(), h.getValue());
         return retval;
     }
 }
