 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2013 Per Cederberg. All rights reserved.
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the BSD license.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  * See the RapidContext LICENSE.txt file for more details.
  */
 
 package org.rapidcontext.core.web;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.FileItemIterator;
 import org.apache.commons.fileupload.FileItemStream;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.time.DateUtils;
 import org.rapidcontext.core.data.Binary;
 import org.rapidcontext.core.data.Dict;
 import org.rapidcontext.util.FileUtil;
 import org.rapidcontext.util.HttpUtil;
 import org.rapidcontext.util.StringUtil;
 
 /**
  * A request wrapper class. This class encapsulates the HTTP servlet
  * request and response objects for simplified handling. It also
  * provides limited support for file uploads.
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public class Request implements HttpUtil {
 
     /**
      * The class logger.
      */
     private static final Logger LOG =
         Logger.getLogger(Request.class.getName());
 
     /**
      * The session cookie name.
      */
     public static final String SESSION_COOKIE = "sessionid";
 
     /**
      * The no response type. This type is used when no request
      * response has been issued.
      */
     private static final int NO_RESPONSE = 0;
 
     /**
      * The authentication failure response type. This type is used
      * when an authentication failure has been set as the request
      * response.
      */
     private static final int AUTH_RESPONSE = 1;
 
     /**
      * The text data response type. This type is used when a text
      * string has been set as the request response.
      */
     private static final int TEXT_RESPONSE = 2;
 
     /**
      * The binary data response type. This type is used when a file
      * or similar has been set as the request response. The response
      * data contains the Binary object when this type is set.
      */
     private static final int BINARY_RESPONSE = 3;
 
     /**
      * The redirect response type. This type is used when a request
      * redirect has been issued. The response data contains the
      * redirect URI (absolute or relative) when this type is set.
      */
     private static final int REDIRECT_RESPONSE = 4;
 
     /**
      * The error response type. This type is used when a response
      * error code should be sent. The response code, MIME type and
      * data string may be set when sending this response.
      */
     private static final int ERROR_RESPONSE = 5;
 
     /**
      * The regular expression for extracting header values.
      */
     private static final Pattern RE_HEADER_VALUE = Pattern.compile("[^,\'\"\\s]+");
 
     /**
      * The system time when creating this request.
      */
     private long requestTime = System.currentTimeMillis();
 
     /**
      * The local request path. This is initially set to null, but may
      * be modified during request processing to simplify or modify
      * resource lookup.
      */
     private String requestPath = null;
 
     /**
      * The servlet request.
      */
     private HttpServletRequest request = null;
 
     /**
      * The servlet response.
      */
     private HttpServletResponse response = null;
 
     /**
      * The cached request URL.
      */
     private String requestUrl = null;
 
     /**
      * The cached request protocol scheme.
      */
     private String requestProtocol = null;
 
     /**
      * The cached request host name.
      */
     private String requestHost = null;
 
     /**
      * The cached request port number.
      */
     private int requestPort = 0;
 
     /**
      * The cached request IP address.
      */
     private String requestIp = null;
 
     /**
      * The response type. This flag is set to true if the response
      * object has been modified.
      */
     private int responseType = NO_RESPONSE;
 
     /**
      * The response HTTP code. Only used when sending error responses.
      */
     private int responseCode = STATUS.OK;
 
     /**
      * The response MIME type.
      */
     private String responseMimeType = null;
 
     /**
      * The response data.
      */
     private Object responseData = null;
 
     /**
      * The response headers only flag.
      */
     private boolean responseHeadersOnly = false;
 
     /**
      * The multi-part request file iterator.
      */
     private FileItemIterator fileIter = null;
 
     /**
      * Creates a new request wrapper.
      *
      * @param request        the servlet request
      * @param response       the servlet response
      */
     public Request(HttpServletRequest request, HttpServletResponse response) {
         this.request = request;
         this.response = response;
         if (request.getCharacterEncoding() == null) {
             try {
                 request.setCharacterEncoding("UTF-8");
             } catch (UnsupportedEncodingException ignore) {
                 // Do nothing
             }
         }
         logRequest();
     }
 
     /**
      * Returns a string representation of this request.
      *
      * @return a string representation of this request
      */
     public String toString() {
         return getUrl();
     }
 
     /**
      * Checks if this request contains a file upload. These requests
      * must be handled differently from ordinary requests, since the
      * normal request parameter access is limited to query
      * parameters. Files can only be accessed by iterating through
      * them with the getNextFile() method.
      *
      * @return true if this request contains a file upload, or
      *         false otherwise
      *
      * @see #getNextFile()
      */
     public boolean isFileUpload() {
         return ServletFileUpload.isMultipartContent(request);
     }
 
     /**
      * Checks if the request method is the specified one. Normally
      * the "GET" or "POST" methods are used, but other HTTP are
      * sometimes also used (with other expected semantics).
      *
      * @param method         the HTTP method name
      *
      * @return true if the method is the specified one, or
      *         false otherwise
      */
     public boolean hasMethod(String method) {
         return getMethod().equals(method);
     }
 
     /**
      * Checks if this request contains a response.
      *
      * @return true if the request contains a response, or
      *         false otherwise
      */
     public boolean hasResponse() {
         return responseType != NO_RESPONSE;
     }
 
     /**
      * Returns the full request URL with protocol, hostname, port and
      * path. No query parameters will be included in the URL, however.
      *
      * @return the full request URL
      */
     public String getUrl() {
         if (requestUrl == null) {
             StringBuilder buffer = new StringBuilder();
             String scheme = getProtocol();
             String host = getHost();
             int port = getPort();
             buffer.append(scheme);
             buffer.append("://");
             if (scheme.equals("http") && port == 80) {
                 buffer.append(host);
             } else if (scheme.equals("https") && port == 443) {
                 buffer.append(host);
             } else {
                 buffer.append(host);
                 buffer.append(":");
                 buffer.append(port);
             }
             buffer.append(getAbsolutePath());
             requestUrl = buffer.toString();
         }
         return requestUrl;
     }
 
     /**
      * Returns the protocol name in the request, i.e. "http" or
      * "https".
      *
      * @return the protocol name
      */
     public String getProtocol() {
         if (requestProtocol == null) {
             String str1 = request.getHeader("X-Forwarded-Scheme");
             String str2 = request.getHeader("X-Forwarded-Proto");
             String str3 = request.getScheme();
             str1 = StringUtil.match(str1, RE_HEADER_VALUE);
             str2 = StringUtil.match(str2, RE_HEADER_VALUE);
             requestProtocol = StringUtil.first(str1, str2, str3);
         }
         return requestProtocol;
     }
 
     /**
      * Returns the host name in the request.
      *
      * @return the host name
      */
     public String getHost() {
         if (requestHost == null) {
             String str1 = request.getHeader("X-Forwarded-Server");
             String str2 = request.getHeader("X-Forwarded-Host");
             String str3 = request.getServerName();
             str1 = StringUtil.match(str1, RE_HEADER_VALUE);
             str2 = StringUtil.match(str2, RE_HEADER_VALUE);
             str2 = StringUtils.substringBefore(str2, ":");
             requestHost = StringUtil.first(str1, str2, str3);
         }
         return requestHost;
     }
 
     /**
      * Returns the port number in the request.
      *
      * @return the port number
      */
     public int getPort() {
         if (requestPort <= 0) {
             String str1 = request.getHeader("X-Forwarded-Port");
             String str2 = request.getHeader("X-Forwarded-Host");
             str1 = StringUtil.match(str1, RE_HEADER_VALUE);
             str2 = StringUtil.match(str2, RE_HEADER_VALUE);
             if (str1 != null) {
                 requestPort = Integer.parseInt(str1);
             } else if (str2 != null) {
                 str2 = StringUtils.substringAfter(str2, ":");
                 str2 = StringUtils.defaultIfEmpty(str2, "80");
                 requestPort = Integer.parseInt(str2);
             } else {
                 requestPort = request.getServerPort();
             }
         }
         return requestPort;
     }
 
     /**
      * Returns the request method name. Normally this is "GET" or
      * "POST", but other HTTP methods can also be used.
      *
      * @return the request method name
      */
     public String getMethod() {
         return request.getMethod();
     }
 
     /**
      * Returns the full request path with file name. This path starts
      * with a '/' character and contains the absolute request path,
      * including the servlet path.
      *
      * @return the full request path with file name
      */
     public String getAbsolutePath() {
         String  path = request.getPathInfo();
 
         if (path == null) {
             return request.getContextPath();
         } else {
             return request.getContextPath() + path;
         }
     }
 
     /**
      * Returns the local request path with file name. This path has
      * been shortened to ONLY include the relevant portions of the
      * path, removing any initial mapping URL portions of the path.
      * A root path may thus be an empty string.
      *
      * @return the local request path
      */
     public String getPath() {
         if (requestPath == null) {
             String path = request.getPathInfo();
             path = StringUtils.removeStart(path, "/");
             requestPath = (path == null) ? "" : path;
         }
         return requestPath;
     }
 
     /**
      * Sets the local request path. Use this method to shorten the
      * request path from any additional prefixes. It can also be used
      * to rewrite one request path into another.
      *
      * @param path           the new local path, or null to reset
      */
     public void setPath(String path) {
         requestPath = path;
     }
 
     /**
      * Matches and modifies the request path with the specified path
      * prefix. If a match is found, the prefix is removed from the
      * request path and true is returned. If the prefix ends with a /
      * and the path would match the shorter prefix, a redirect is
      * sent and false is returned.
      *
      * @param prefix         the path prefix to check
      *
      * @return true if the request path matched the prefix, or
      *         false otherwise
      */
     public boolean matchPath(String prefix) {
         String path = getPath();
         if (path.startsWith(prefix)) {
             if (prefix.length() > 0) {
                 setPath(path.substring(prefix.length()));
             }
             return true;
         } else if (path.startsWith(StringUtils.removeEnd(prefix, "/"))) {
             sendRedirect(getUrl() + "/");
         }
         return false;
     }
 
     /**
      * Returns the request content type value. This is normally set
      * to "application/x-www-form-urlencoded" for POST data, but
      * other MIME types may occasionally be used.
      *
      * @return the HTTP content type header value
      */
     public String getContentType() {
         return request.getContentType();
     }
 
     /**
      * Returns the IP address of the request sender.
      *
      * @return the IP address of the request sender
      */
     public String getRemoteAddr() {
         if (requestIp == null) {
             String str1 = request.getHeader("X-Forwarded-For");
             String str2 = request.getHeader("X-Real-IP");
             String str3 = request.getRemoteAddr();
             str1 = StringUtil.match(str1, RE_HEADER_VALUE);
             str2 = StringUtil.match(str2, RE_HEADER_VALUE);
             requestIp = (str1 != null) ? str1 : (str2 != null) ? str2 : str3;
         }
         return requestIp;
     }
 
     /**
      * Returns the parsed "Authorization" request header data. The
      * authentication type will be stored with the "type" key. Other
      * fields will be stored with their corresponding keys. Any
      * unidentified data will be stored with the "data" key.
      *
      * @return the parsed authentication data, or
      *         null if not present
      */
     public Dict getAuthentication() {
         String auth = request.getHeader("Authorization");
         if (auth == null || !auth.contains(" ")) {
             return null;
         } else {
             Dict dict = new Dict();
             String[] pairs = auth.split("[ \t\n\r,]");
             dict.set("type", pairs[0]);
             for (int i = 1; i < pairs.length; i++) {
                 String[] pair = pairs[i].split("=");
                 if (pair.length == 2) {
                     dict.set(pair[0], StringUtils.strip(pair[1], "\""));
                 } else {
                     dict.set("data", pairs[i]);
                 }
             }
             return dict;
         }
     }
 
     /**
      * Returns an HTTP request header.
      *
      * @param name           the request header name
      *
      * @return the header value, or
      *         null if not set
      */
     public String getHeader(String name) {
         return request.getHeader(name);
     }
 
     /**
      * Returns the value of a request parameter.
      *
      * @param name           the request parameter name
      *
      * @return the request parameter value, or
      *         null if no such parameter was found
      */
     public String getParameter(String name) {
         return getParameter(name, null);
     }
 
     /**
      * Returns the value of a request parameter. If the specified
      * parameter does not exits, a default value will be returned.
      *
      * @param name           the request parameter name
      * @param defVal         the default parameter value
      *
      * @return the request parameter value, or
      *         the default value if no such parameter was found
      */
     public String getParameter(String name, String defVal) {
         String  value = request.getParameter(name);
 
         return (value == null) ? defVal : value;
     }
 
     /**
      * Returns the next available file item stream in the request.
      * If the request is not a file upload request or no further
      * files are available, null will be returned. Note that file
      * upload requests must be handled differently from ordinary
      * requests, since the normal request parameter access is
      * limited to query parameters. Also, the file items returned
      * from this method are controlled to be actual files, and any
      * other request parameters are simply ignored.<p>
      *
      * The recommended way to implement file uploads is by sending
      * them as the sole request parameter in a form and storing the
      * file content for later processing. Preferably the form
      * posting is also performed from an IFRAME in order to process
      * in the background.
      *
      * @return the next file item stream, or
      *         null if none is available
      *
      * @throws IOException if the file upload
      */
     public FileItemStream getNextFile() throws IOException {
         FileItemStream  item;
 
         if (isFileUpload() && fileIter == null) {
             try {
                 fileIter = new ServletFileUpload().getItemIterator(request);
             } catch (FileUploadException e) {
                 throw new IOException("failed to parse file upload: " +
                                       e.getMessage());
             }
         }
         if (fileIter != null) {
             try {
                 while (fileIter.hasNext()) {
                     item = fileIter.next();
                     if (!item.isFormField()) {
                         return item;
                     }
                 }
             } catch (FileUploadException e) {
                 throw new IOException("failed to parse file upload: " +
                                       e.getMessage());
             }
         }
         return null;
     }
 
     /**
      * Returns a named request attribute value.
      *
      * @param name           the attribute name
      *
      * @return the attribute value, or
      *         null if not set
      */
     public Object getAttribute(String name) {
         return request.getAttribute(name);
     }
 
     /**
      * Sets a request attribute value.
      *
      * @param name           the attribute name
      * @param value          the attribute value
      */
     public void setAttribute(String name, Object value) {
         request.setAttribute(name, value);
     }
 
     /**
      * Returns the request session id (as sent in an HTTP cookie).
      *
      * @return the session id from the cookie, or
      *         null if no session cookie was present
      */
     public String getSessionId() {
         Cookie[] cookies = request.getCookies();
         for (int i = 0; cookies != null && i < cookies.length; i++) {
             if (SESSION_COOKIE.equals(cookies[i].getName())) {
                 return cookies[i].getValue();
             }
         }
         return null;
     }
 
     /**
      * Returns the HTTP request object for direct access to the raw
      * request data.
      *
      * @return the HTTP request object
      */
     public HttpServletRequest getHttpRequest() {
         return request;
     }
 
     /**
      * Returns the HTTP response object for direct access to the raw
      * response data.
      *
      * @return the HTTP response object
      */
     public HttpServletResponse getHttpResponse() {
         return response;
     }
 
     /**
      * Returns the time in milliseconds since this request wrapper
      * was created. The number returned will always be one (1) or
      * greater.
      *
      * @return the approximate request processing time
      */
     public long getProcessTime() {
         return Math.max(System.currentTimeMillis() - requestTime, 1);
     }
 
     /**
      * Returns the request input stream data as a string. Once this
      * method has been called, the request input stream cannot be
      * read again. Also, this method should only be called if the
      * request data is required to be in text format.
      *
      * @return the request input stream data as a string
      */
     public String getInputString() {
         ByteArrayOutputStream  os = new ByteArrayOutputStream();
 
         try {
             FileUtil.copy(request.getInputStream(), os);
             return os.toString("UTF-8");
         } catch (IOException e) {
             LOG.warning("failed to read request input data: " + e.getMessage());
             return "";
         }
     }
 
     /**
      * Returns the request input stream. Once the input stream has
      * been read once, it cannot be read again. Also, this method
      * should only be called if the request data is indeed in a
      * binary format.
      *
      * @return the request input stream
      *
      * @throws IOException if the input stream couldn't be read
      */
     public InputStream getInputStream() throws IOException {
         return request.getInputStream();
     }
 
     /**
      * Clears any previously sent but non-committed response. Note
      * that this method DOES NOT clear any response headers or
      * cookies already set.
      */
     public void sendClear() {
         responseType = NO_RESPONSE;
         responseCode = STATUS.OK;
         responseMimeType = null;
         responseData = null;
         responseHeadersOnly = false;
     }
 
     /**
      * Sends a digest authentication request as the request response.
      * Any previous response will be cleared.
      *
      * @param realm          the security realm to use
      * @param nonce          the generated one-time code to use
      *
      * @see #sendClear()
      */
     public void sendAuthenticationRequest(String realm, String nonce) {
         sendClear();
         responseType = AUTH_RESPONSE;
         responseData = "Digest realm=\"" + realm + "\", qop=\"auth\", " +
                        "nonce=\"" + nonce+ "\"";
     }
 
     /**
      * Sends the specified text data as the request response. Any
      * previous response will be cleared.
      *
      * @param mimeType       the data MIME type
      * @param text           the text data to send
      *
      * @see #sendClear()
      */
     public void sendText(String mimeType, String text) {
         sendText(STATUS.OK, mimeType, text);
     }
 
     /**
      * Sends the specified text data as the request response. Any
      * previous response will be cleared.
      *
      * @param code           the HTTP response code to send
      * @param mimeType       the optional MIME type, null for default
      * @param text           the text data to send
      *
      * @see #sendClear()
      */
     public void sendText(int code, String mimeType, String text) {
         sendClear();
         responseType = TEXT_RESPONSE;
         responseCode = code;
         if (mimeType == null || mimeType.length() == 0) {
             responseMimeType = "text/plain; charset=UTF-8";
         } else if (mimeType.indexOf("charset") > 0) {
             responseMimeType = mimeType;
         } else {
             responseMimeType = mimeType + "; charset=UTF-8";
         }
         responseData = text;
     }
 
     /**
      * Sends the contents of a file as the request response. The file
      * name extension will be used for determining the MIME type for
      * the file contents. The cache header for the file may be
      * limited to private by setting the limit cache header. Any
      * previous response will be cleared.
      *
      * @param data           the file containing the response
      * @param limitCache     the limited cache flag
      *
      * @see #sendClear()
      */
     public void sendBinary(Binary data, boolean limitCache) {
         sendClear();
         responseType = BINARY_RESPONSE;
         responseCode = STATUS.OK;
         responseMimeType = data.mimeType();
         responseData = data;
         if (limitCache) {
             response.setHeader(HEADER.CACHE_CONTROL, "private");
         }
     }
 
     /**
      * Redirects this request by sending a temporary redirection URL
      * to the browser. The location specified may be either an
      * absolute or a relative URL. This method will set a request
      * response. Any previous response will be cleared.
      *
      * @param location       the destination location
      *
      * @see #sendClear()
      */
     public void sendRedirect(String location) {
         sendClear();
         responseType = REDIRECT_RESPONSE;
         responseData = location;
     }
 
     /**
      * Sends the specified error code. This method does not provide
      * any visible error page to the user. Any previous response will
      * be cleared.
      *
      * @param code           the HTTP response code to send
      *
      * @see #sendClear()
      * @see #sendError(int, String, String)
      */
     public void sendError(int code) {
         sendClear();
         responseType = ERROR_RESPONSE;
         responseCode = code;
         responseMimeType = null;
         responseData = null;
     }
 
     /**
      * Sends the specified error code and data as the request
      * response. Any previous response will be cleared.
      *
      * @param code           the HTTP response code to send
      * @param mimeType       the optional MIME type, null for default
      * @param text           the text data to send (error page content)
      *
      * @see #sendClear()
      */
     public void sendError(int code, String mimeType, String text) {
         sendText(code, mimeType, text);
         responseType = ERROR_RESPONSE;
     }
 
     /**
      * Sets an HTTP response header value. This method should only be
      * called once a response has been set but before the call to
      * commit(). Normally, this method should be avoided, as the
      * headers set aren't removed unless explicitly overwritten.
      *
      * @param name           the HTTP header name
      * @param value          the HTTP header value
      */
     public void setResponseHeader(String name, String value) {
         response.setHeader(name, value);
     }
 
     /**
      * Sets or clears the response headers only flag. If set, only
      * the HTTP response headers will be sent. No actual data will
      * be transferred.
      *
      * @param value          the new flag value
      */
     public void setResponseHeadersOnly(boolean value) {
         responseHeadersOnly = value;
     }
 
     /**
      * Sets the session id cookie in the HTTP response. This method
      * can also be used to clear the session cookie in the web
      * browser (by setting a null value).
      *
      * @param sessionId      the session identifier
      * @param expiry         the maximum age of the cookie in seconds
      */
     public void setSessionId(String sessionId, int expiry) {
         String value = (sessionId == null) ? "deleted" : sessionId;
         Cookie cookie = new Cookie(SESSION_COOKIE, value);
         cookie.setPath(request.getContextPath() + "/");
         cookie.setSecure(request.isSecure());
         cookie.setMaxAge((sessionId == null) ? 0 : expiry);
         response.addCookie(cookie);
     }
 
     /**
      * Disposes of all resources used by this request object. This
      * method shouldn't be called until a response has been sent to
      * the client.
      */
     public void dispose() {
         request = null;
         response = null;
         responseData = null;
     }
 
     /**
      * Sends the request response to the underlying HTTP response
      * object. This method shouldn't be called more than once per
      * request, and should not be called in case no response has been
      * stored in the request.
      *
      * @throws IOException if an IO error occurred while attempting to
      *             commit the response
      * @throws ServletException if a configuration error was
      *             encountered while sending the response
      */
     public void commit() throws IOException, ServletException {
         response.setHeader(HEADER.SERVER, "RapidContext");
         response.setDateHeader(HEADER.DATE, System.currentTimeMillis());
         switch (responseType) {
         case AUTH_RESPONSE:
             response.setHeader(HEADER.WWW_AUTHENTICATE, (String) responseData);
             response.sendError(STATUS.UNAUTHORIZED);
             logResponse();
             break;
         case TEXT_RESPONSE:
             commitText();
             break;
         case BINARY_RESPONSE:
             commitBinary();
             break;
         case REDIRECT_RESPONSE:
             commitHeaders(false, 0);
             response.sendRedirect((String) responseData);
             logResponse();
             break;
         case ERROR_RESPONSE:
             if (responseData == null) {
                 response.sendError(responseCode);
                 logResponse();
             } else {
                 commitText();
             }
             break;
         default:
             throw new ServletException("No request response available: " +
                                        getUrl());
         }
     }
 
     /**
      * Sets the HTTP cache, expires and last modified headers. If the
      * last modified time is zero (0) or negative, the current system
      * time will be used instead.
      *
      * @param cache          the cache permission flag
      * @param lastModified   the last modification time, or
      *                       zero (0) for the current system time
      */
     private void commitHeaders(boolean cache, long lastModified) {
         if (!response.containsHeader(HEADER.CACHE_CONTROL)) {
             String cacheControl = cache ? "public" : "no-cache";
             response.setHeader(HEADER.CACHE_CONTROL, cacheControl);
         }
         if (!response.containsHeader(HEADER.EXPIRES)) {
             if (cache) {
                 long nextHour = System.currentTimeMillis() +
                                 DateUtils.MILLIS_PER_HOUR;
                 response.setDateHeader(HEADER.EXPIRES, nextHour);
             } else {
                 response.setHeader(HEADER.EXPIRES, "-1");
             }
         }
         if (lastModified <= 0) {
             lastModified = System.currentTimeMillis();
         }
         response.setDateHeader(HEADER.LAST_MODIFIED, lastModified);
     }
 
     /**
      * Sends the text data response to the underlying HTTP response
      * object.
      *
      * @throws IOException if an IO error occurred while attempting
      *             to commit the response
      */
     private void commitText() throws IOException {
         response.setStatus(responseCode);
         commitHeaders(false, 0);
         response.setContentType(responseMimeType);
         if (responseData == null) {
             response.setContentLength(0);
             logResponse();
         } else {
             byte[] data = ((String) responseData).getBytes("UTF-8");
             response.setContentLength(data.length);
             logResponse();
             OutputStream os = response.getOutputStream();
             os.write(data);
             os.close();
         }
     }
 
     /**
      * Sends the file response to the underlying HTTP response object.
      *
      * @throws IOException if an IO error occurred while attempting to
      *             commit the response
      */
     private void commitBinary() throws IOException {
         Binary       data = (Binary) responseData;
         long         modified;
         InputStream  is;
 
         modified = request.getDateHeader(HEADER.IF_MODIFIED_SINCE);
         if (modified != -1 && data.lastModified() < modified + 1000) {
             response.setStatus(STATUS.NOT_MODIFIED);
             logResponse();
             return;
         }
         response.setStatus(responseCode);
         commitHeaders(true, data.lastModified());
         response.setContentType(data.mimeType());
         if (data.size() >= 0) {
             response.setContentLength((int) data.size());
         }
         logResponse();
         if (!responseHeadersOnly) {
             try {
                 is = data.openStream();
             } catch (Exception e) {
                 response.sendError(STATUS.NOT_FOUND);
                 return;
             }
             FileUtil.copy(is, response.getOutputStream());
         }
     }
 
     /**
      * Logs the request for debugging purposes.
      */
     private void logRequest() {
         StringBuilder  buffer;
 
         if (LOG.isLoggable(Level.FINE)) {
             buffer = new StringBuilder();
             buffer.append("[");
             buffer.append(request.getRemoteAddr());
             buffer.append("] ");
             buffer.append(getMethod());
             buffer.append(" ");
             buffer.append(getUrl());
             buffer.append("\n");
             buffer.append(request);
             LOG.fine(buffer.toString());
         }
     }
 
     /**
      * Logs the response for debugging purposes.
      */
     private void logResponse() {
         StringBuilder  buffer;
 
         if (LOG.isLoggable(Level.FINE)) {
             buffer = new StringBuilder();
             buffer.append("[");
             buffer.append(request.getRemoteAddr());
             buffer.append("] ");
             buffer.append(getMethod());
             buffer.append(" ");
             buffer.append(getUrl());
             buffer.append("\n");
             buffer.append(response);
             if (responseType == AUTH_RESPONSE) {
                 buffer.append("Type: Authentication Request\n");
             } else if (responseType == TEXT_RESPONSE) {
                 buffer.append("Type: Text Data\n");
             } else if (responseType == BINARY_RESPONSE) {
                 buffer.append("Type: Binary Data\n");
             } else if (responseType == REDIRECT_RESPONSE) {
                 buffer.append("Type: Redirect\n");
             } else if (responseType == ERROR_RESPONSE) {
                 buffer.append("Type: Error\n");
             }
             if (responseHeadersOnly) {
                 buffer.append("Only HTTP headers in response.\n");
             } else if (responseData != null) {
                 buffer.append("Data: ");
                 buffer.append(responseData);
                 buffer.append("\n");
             }
             LOG.fine(buffer.toString());
         }
     }
 }
