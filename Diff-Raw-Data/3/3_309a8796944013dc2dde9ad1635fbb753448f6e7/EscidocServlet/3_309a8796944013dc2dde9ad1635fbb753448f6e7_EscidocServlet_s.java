 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
  * only (the "License"). You may not use this file except in compliance with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License for
  * the specific language governing permissions and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
  * license/ESCIDOC.LICENSE. If applicable, add the following below this CDDL HEADER, with the fields enclosed by
  * brackets "[]" replaced with your own identifying information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  *
  * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer wissenschaftlich-technische Information mbH
  * and Max-Planck-Gesellschaft zur Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to license
  * terms.
  */
 
 package de.escidoc.core.common.servlet;
 
 import de.escidoc.core.common.business.fedora.EscidocBinaryContent;
 import de.escidoc.core.common.exceptions.EscidocException;
 import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
 import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
 import de.escidoc.core.common.exceptions.application.security.SecurityException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.servlet.invocation.BeanMethod;
 import de.escidoc.core.common.servlet.invocation.MapperInterface;
 import de.escidoc.core.common.servlet.invocation.MethodMapper;
 import de.escidoc.core.common.servlet.invocation.XMLBase;
 import de.escidoc.core.common.util.IOUtils;
 import de.escidoc.core.common.util.configuration.EscidocConfiguration;
 import de.escidoc.core.common.util.string.StringUtility;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.om.service.interfaces.EscidocServiceRedirectInterface;
 import org.aopalliance.aop.AspectException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.security.context.SecurityContextHolder;
 import org.xml.sax.SAXException;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.UndeclaredThrowableException;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * The eSciDoc servlet. Maps a REST request to the specified resource and invokes the specified (if one is
  * configured).<br /> All methods of this class that send an http response have to assure that this response is properly
  * initialized by calling the one of the <code>initHttpResponse</code> methods.
  *
  * @author Michael Schneider
  */
 public class EscidocServlet extends HttpServlet {
 
     public static final String AUTHENTICATION = "eSciDocUserHandle";
 
     /**
      * Pattern used to detect the eSciDoc user handle in the query string of the request in order to redirect the user
      * to the same URL without the user handle.
      */
     private static final Pattern PATTERN_USER_HANDLE_IN_QUERY = Pattern.compile("[&]?" + AUTHENTICATION + "=([^&]*)");
 
     private static final String HEADER_ESCIDOC_EXCEPTION = "eSciDocException";
 
     private static final String UNEXPECTED_INTERNAL_RESPONSE =
         "The request could not be executed " + "due to an unexpected response for the http method.";
 
     public static final String HTTP_DELETE = "DELETE";
 
     public static final String HTTP_GET = "GET";
 
     public static final String HTTP_HEAD = "HEAD";
 
     public static final String HTTP_POST = "POST";
 
     public static final String HTTP_PUT = "PUT";
 
     /**
      * The serial version UID.
      */
     private static final long serialVersionUID = 7530500912744342535L;
 
     private static final String XML_RESPONSE_CONTENT_TYPE = "text/xml; charset=" + XmlUtility.CHARACTER_ENCODING;
 
     private static final String HTML_RESPONSE_CONTENT_TYPE = "text/html; charset=" + XmlUtility.CHARACTER_ENCODING;
 
     /**
      * The LOGGER.
      */
     private static final Logger LOGGER = LoggerFactory.getLogger(EscidocServlet.class);
 
     /**
      * HTTP header Cache-Control (since HTTP 1.1).
      */
     private static final String HTTP_HEADER_CACHE_CONTROL = "Cache-Control";
 
     /**
      * The No-Cache directive for Cache-Control Header and Pragma to prevent caching of the http response.
      */
     private static final String HTTP_HEADER_VALUE_NO_CACHE = "no-cache";
 
     /**
      * HTTP Pragma.
      */
     private static final String HTTP_HEADER_PRAGMA = "Pragma";
 
     /**
      * The parameter name of the init-param in web.xml holding the name of the method descriptor file.
      */
     protected static final String PARAM_DESCRIPTOR = "resource-descriptor";
 
     private static final String HTTP_PARAM_DESCRIPTOR = "descriptor";
 
     /**
      * Already read method MAPPINGS.
      */
     private static final Map<String, MapperInterface> MAPPINGS = new HashMap<String, MapperInterface>();
 
     /**
      * The target URL to which the user shall be redirected after the authentication process.
      */
     public static final String PARAM_TARGET = "target";
 
     public static final String ENCODING = XmlUtility.CHARACTER_ENCODING;
 
     /**
      * The URL of the assertion consumer service, previously known as shire.
      */
     public static final String PARAM_SHIRE = "shire";
 
     /**
      * The provider id.
      */
     public static final String PARAM_PROVIDER_ID = "providerId";
 
     public static final String COOKIE_LOGIN = "escidocCookie";
 
     /**
      * The http content type header.
      */
     public static final String HTTP_HEADER_CONTENT_TYPE = "Content-type";
 
     /**
      * The central service method. Maps a REST request to the specified resource and invokes the specified (if one is
      * configured). If a GET or HEAD request contains user handle information in the URL (as parameter), a redirect to
      * the same URL without the handle parameter is sent back to enable browsers to remove this security information
      * from the displayed URL.
      *
      * @param request  The servlet request.
      * @param response The servlet response
      * @throws ServletException If anything fails.
      * @throws IOException      If anything fails.
      */
     @Override
     public void service(final ServletRequest request, final ServletResponse response) throws ServletException,
         IOException, UnsupportedEncodingException {
 
         try {
             final String protocol = request.getProtocol();
             if (protocol.startsWith("HTTP") && request instanceof HttpServletRequest
                 && response instanceof HttpServletResponse) {
                 final HttpServletRequest httpRequest = (HttpServletRequest) request;
                 final HttpServletResponse httpResponse = (HttpServletResponse) response;
 
                 if (getQueryParamValue(httpRequest, HTTP_PARAM_DESCRIPTOR) != null) {
                     handleDescriptorRequest(httpResponse);
                     return;
                 }
 
                 final String httpMethod = httpRequest.getMethod();
                 // unsupported request methods,
                 // must be handled before determine bean method
                 if (HTTP_HEAD.equals(httpMethod)) {
                     // FIXME check if valid HEAD response can be send
                     doSendStringResponse(httpResponse, null, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                     return;
                 }
 
                 // Handle problem with eSciDoc user handle information in
                 // Request URL. This could be a request from a browser which
                 // displays this complete URL in the URL-line.
                 // In case of GET and HEAD requests, a redirect to the same
                 // URL
                 // without the URL parameters is sent.
                 final String queryString = addCookie(httpRequest, httpResponse);
                 if (queryString != null) {
                     final StringBuffer location = httpRequest.getRequestURL();
                     if (queryString.length() > 0) {
                         location.append('?');
                         location.append(queryString);
                     }
                     final String locationString = location.toString();
                     doRedirect(httpResponse, null, "<html><body><a href=\"" + locationString
                         + "\">Resource available under this location: " + locationString + "</a></body></html>",
                         locationString, HttpServletResponse.SC_MOVED_PERMANENTLY);
                     return;
                 }
 
                 // Get the authentication values.
                 final String[] authValues = getAuthValues(httpRequest, httpResponse);
 
                 BeanMethod method = null;
                 try {
 
                     final MapperInterface methodMapper = getMethodMapper(getInitParameter(PARAM_DESCRIPTOR));
                     method = methodMapper.getMethod(httpRequest);
 
                     final Object result = method.invoke(authValues[0], authValues[1]);
                     if (result == null) {
                         doSendVoidResponse(httpResponse, httpMethod);
                     }
                     else if (result instanceof EscidocBinaryContent) {
                         doSendBinaryContentResponse(httpResponse, httpMethod, (EscidocBinaryContent) result);
                     }
                     else if (result instanceof String) {
                         doSendStringResponse(httpResponse, httpMethod, (String) result);
                     }
                     else if (result instanceof EscidocServiceRedirectInterface) {
                         doRedirectResponse(httpResponse, httpMethod, (EscidocServiceRedirectInterface) result);
                     }
                     else {
                         doDeclineHttpRequest(httpResponse, new WebserverSystemException(StringUtility.format(
                             UNEXPECTED_INTERNAL_RESPONSE, httpMethod, result)));
                     }
 
                     if (!httpResponse.isCommitted()) {
                         if (LOGGER.isDebugEnabled()) {
                             LOGGER.debug("Request not commited.");
                         }
                     }
                 }
                 catch (final Exception e) {
                     handleException(httpRequest, httpResponse, method, e);
                 }
 
             }
         }
         finally {
             // clear the user Context as it must not be reused in another
             // request.
             SecurityContextHolder.clearContext();
         }
     }
 
     /**
      * Handles a request for the descriptor.
      *
      * @param httpResponse The http response.
      * @throws IOException In case of an I/O error.
      */
     private void handleDescriptorRequest(final HttpServletResponse httpResponse) throws IOException {
 
         try {
             final XMLBase base = new XMLBase();
             final String descriptor = base.getFileContents(getInitParameter(PARAM_DESCRIPTOR));
             doSendStringResponse(httpResponse, descriptor, HttpServletResponse.SC_OK);
         }
         catch (final IOException e) {
             doDeclineHttpRequest(httpResponse, new WebserverSystemException("Descriptor not found.", e));
         }
     }
 
     /**
      * Handles an <code>Exception</code>.<br> Depending on the exception that caused the invocation target exception,
      * different responses are created and sent to the client.
      *
      * @param httpRequest  The http request.
      * @param httpResponse The http response.
      * @param method       The resource method that has been called and cause the exception.
      * @param e            The exception to handle.
      * @return Returns <code>true</code> if the exception has been handled.
      * @throws IOException In case of any failure.
      * @throws java.io.UnsupportedEncodingException
      */
     private static boolean handleException(
         final HttpServletRequest httpRequest, final HttpServletResponse httpResponse, final BeanMethod method,
         final Throwable e) throws IOException, UnsupportedEncodingException {
 
         boolean ret = false;
 
         if (e == null) {
             return false;
         }
         else if (e instanceof InvocationTargetException) {
             ret =
                 handleException(httpRequest, httpResponse, method, ((InvocationTargetException) e).getTargetException());
         }
         else if (e instanceof AspectException) {
             ret = handleException(httpRequest, httpResponse, method, e.getCause());
         }
         else if (e instanceof AuthenticationException) {
             doRedirect(httpRequest, httpResponse, (SecurityException) e);
             ret = true;
         }
         else if (e instanceof AuthorizationException) {
             final String[] authValues = getAuthValues(httpRequest, httpResponse);
             if (authValues == null || authValues[1].length() == 0) {
                 doRedirect(httpRequest, httpResponse, (SecurityException) e);
             }
             else {
                 doDeclineHttpRequest(httpResponse, (EscidocException) e);
             }
             ret = true;
         }
         else if (e instanceof EscidocException) {
             doDeclineHttpRequest(httpResponse, (EscidocException) e);
             ret = true;
         }
         else if (e instanceof UndeclaredThrowableException) {
             final Throwable undeclaredThrowable = ((UndeclaredThrowableException) e).getUndeclaredThrowable();
             if (undeclaredThrowable.getClass().getName().equals(AuthenticationException.class.getName())) {
                 doRedirect(httpRequest, httpResponse, (SecurityException) undeclaredThrowable);
             }
             else {
                 doDeclineHttpRequest(httpResponse, new WebserverSystemException(StringUtility.format(
                     "Undeclared throwable during method execution", undeclaredThrowable.getClass().getName()),
                     undeclaredThrowable));
             }
             ret = true;
         }
 
         if (!ret) {
             LOGGER.error(StringUtility.format("Caught exception cannot be handled, returning "
                 + WebserverSystemException.class.getName() + " to client.", e.getClass().getName(), e.getMessage()), e);
             if (e.getMessage() != null) {
                 doDeclineHttpRequest(httpResponse, new WebserverSystemException(e.getMessage(), e));
             }
             else {
                 doDeclineHttpRequest(httpResponse, new WebserverSystemException(e.getClass().getName(), e));
             }
             ret = true;
         }
 
         return ret;
     }
 
     /**
      * Retrieves the method mapper offering the MAPPINGS contained in the given filename.
      *
      * @param filename The MAPPINGS file.
      * @return The method mapper.
      * @throws IOException                  If anything fails.
      * @throws TransformerException         If anything fails.
      * @throws ParserConfigurationException If anything fails.
      * @throws SAXException                 If anything fails.
      * @throws java.io.FileNotFoundException
      */
     private static MapperInterface getMethodMapper(final String filename) throws IOException, TransformerException,
         ParserConfigurationException, SAXException, FileNotFoundException {
         MapperInterface result = MAPPINGS.get(filename);
         if (result == null) {
             result = new MethodMapper(filename);
             MAPPINGS.put(filename, result);
         }
         return result;
     }
 
     /**
      * Handles a response for a method that returns a string value.
      *
      * @param httpResponse The {@link HttpServletResponse} object.
      * @param httpMethod   The http method of the request.
      * @param result       The {@link String} object that shall be sent in the response.
      * @throws IOException If anything fails.
      */
     private static void doSendStringResponse(
         final HttpServletResponse httpResponse, final String httpMethod, final String result) throws IOException {
 
         if (HTTP_GET.equals(httpMethod) || HTTP_PUT.equals(httpMethod) || HTTP_POST.equals(httpMethod)) {
             doSendStringResponse(httpResponse, result, HttpServletResponse.SC_OK);
         }
         else {
             doDeclineHttpRequest(httpResponse, new WebserverSystemException(StringUtility.format(
                 UNEXPECTED_INTERNAL_RESPONSE, httpMethod, result)));
         }
     }
 
     /**
      * Handles a response for a method that returns a string value.
      *
      * @param httpResponse The {@link HttpServletResponse} object.
      * @param httpMethod   The http method of the request.
      * @param result       The {@link String} object that shall be sent in the response.
      * @throws IOException If anything fails.
      */
     private static void doRedirectResponse(
         final HttpServletResponse httpResponse, final String httpMethod, final EscidocServiceRedirectInterface result)
         throws IOException {
 
         if (HTTP_GET.equals(httpMethod) || HTTP_PUT.equals(httpMethod) || HTTP_POST.equals(httpMethod)) {
 
             initHttpResponse(httpResponse);
             httpResponse.setContentType(HTML_RESPONSE_CONTENT_TYPE);
             httpResponse.getWriter().println(result.getContent());
 
             httpResponse.setStatus(HttpServletResponse.SC_FOUND);
         }
         else {
             doDeclineHttpRequest(httpResponse, new WebserverSystemException(StringUtility.format(
                 UNEXPECTED_INTERNAL_RESPONSE, httpMethod, result)));
         }
     }
 
     /**
      * Handles a response for a method that does not return a value.
      *
      * @param httpResponse The {@link HttpServletResponse} object.
      * @param httpMethod   The http method of the request.
      * @throws IOException If anything fails.
      */
     private static void doSendVoidResponse(final HttpServletResponse httpResponse, final String httpMethod)
         throws IOException {
 
         if (HTTP_DELETE.equals(httpMethod)) {
             doSendStringResponse(httpResponse, null, HttpServletResponse.SC_NO_CONTENT);
         }
         else if (HTTP_GET.equals(httpMethod) || HTTP_PUT.equals(httpMethod) || HTTP_POST.equals(httpMethod)) {
             doSendStringResponse(httpResponse, null, HttpServletResponse.SC_OK);
         }
         else {
             doDeclineHttpRequest(httpResponse, new WebserverSystemException(StringUtility.format(
                 UNEXPECTED_INTERNAL_RESPONSE, httpMethod, "void")));
         }
     }
 
     /**
      * Handles a response for an access to binary content. Sends the http response with either status OK and the binary
      * content in the response body or a redirect to an external managed content.
      *
      * @param httpResponse  The {@link HttpServletResponse} object.
      * @param httpMethod    The http method of the request.
      * @param binaryContent The {@link EscidocBinaryContent} object holding the data that shall be sent in the
      *                      response.
      * @throws IOException If anything fails.
      */
     private static void doSendBinaryContentResponse(
         final HttpServletResponse httpResponse, final String httpMethod, final EscidocBinaryContent binaryContent)
         throws IOException {
 
         if (HTTP_GET.equals(httpMethod)) {
             final String externalContentRedirectUrl = binaryContent.getRedirectUrl();
             if (externalContentRedirectUrl != null) {
                 // redirect
                 doRedirect(httpResponse, null, "<html><body><a href=\"" + externalContentRedirectUrl
                     + "\">The requested binary content" + " is externally available under this location: "
                     + externalContentRedirectUrl + "</a></body></html>", externalContentRedirectUrl,
                     HttpServletResponse.SC_MOVED_TEMPORARILY);
             }
             else {
                 // response with content
                 httpResponse.setHeader(HTTP_HEADER_CACHE_CONTROL, HTTP_HEADER_VALUE_NO_CACHE);
                 httpResponse.setHeader(HTTP_HEADER_PRAGMA, HTTP_HEADER_VALUE_NO_CACHE);
 
                 httpResponse.setContentType(binaryContent.getMimeType());
                 if (binaryContent.getFileName() != null) {
                    httpResponse.setHeader("file-name", binaryContent.getFileName());
                 }
                 final ServletOutputStream out = httpResponse.getOutputStream();
                 final InputStream content = binaryContent.getContent();
                 IOUtils.copyAndCloseInput(content, out);
             }
         }
         else {
             doDeclineHttpRequest(httpResponse, new WebserverSystemException(StringUtility.format(
                 UNEXPECTED_INTERNAL_RESPONSE, httpMethod, "void")));
         }
     }
 
     /**
      * Sends the http response with provided status and the provided text in the response body. <br/> Before sending,
      * the no-cache headers are added.
      *
      * @param httpResponse The {@link HttpServletResponse} object.
      * @param text         The {@link String} object to be sent in the response body.
      * @param status       The http response status.
      * @throws IOException If anything fails.
      */
     private static void doSendStringResponse(final HttpServletResponse httpResponse, final String text, final int status)
         throws IOException {
 
         initHttpResponse(httpResponse);
         if (text != null) {
             httpResponse.setContentType(XML_RESPONSE_CONTENT_TYPE);
             httpResponse.getWriter().println(text);
         }
         httpResponse.setStatus(status);
     }
 
     /**
      * Decline an incoming http request with given error code and message taken from the exception. <br/> Before
      * sending, the no-cache headers are added.
      *
      * @param httpResponse The http response.
      * @param exception    The exception.
      * @throws IOException If anything fails.
      */
     private static void doDeclineHttpRequest(final HttpServletResponse httpResponse, final EscidocException exception)
         throws IOException {
 
         httpResponse.reset();
         initHttpResponse(httpResponse);
         httpResponse.setHeader(HEADER_ESCIDOC_EXCEPTION, exception.getClass().getName());
         httpResponse.setStatus(exception.getHttpStatusCode());
         if (exception instanceof SecurityException) {
             httpResponse.setHeader("Location", ((SecurityException) exception).getRedirectLocation());
         }
         String body;
         try {
             body = XmlUtility.DOCUMENT_START + XmlUtility.getStylesheetDefinition() + exception.toXmlString();
         }
         catch (final WebserverSystemException e) {
             LOGGER.error("Error on serialising exception to XML string.", e);
             body = XmlUtility.DOCUMENT_START + exception.toXmlString();
         }
         httpResponse.getWriter().println(body);
     }
 
     /**
      * Redirect the user to the URL provided within the exception that causes this redirect.<br/> This method extracts
      * the values from the provided exception and delegates to <code>doRedirect(HttpServletResponse, String, String
      * redirectLocation, int httpStatusCode)</code> <br/> Before sending, the no-cache headers are added.
      *
      * @param httpRequest  The http request.
      * @param httpResponse The http response.
      * @param exception    The exception that causes the redirect.
      * @throws IOException If anything fails.
      * @throws java.io.UnsupportedEncodingException
      */
     private static void doRedirect(
         final HttpServletRequest httpRequest, final HttpServletResponse httpResponse, final SecurityException exception)
         throws IOException, UnsupportedEncodingException {
 
         final String message = exception.toXmlString();
         final String redirectLocation =
             exception.getRedirectLocation()
                 + '?'
                 + PARAM_TARGET
                 + '='
                 + URLEncoder.encode(httpRequest.getRequestURL().toString(), ENCODING)
                 + '&'
                 + PARAM_SHIRE
                 + '='
                 + URLEncoder.encode(EscidocConfiguration.getInstance().appendToSelfURL("/shibboleth/acs").replace(
                     "http://", "https://"), ENCODING) + '&' + PARAM_PROVIDER_ID + '='
                 + URLEncoder.encode("https://www.escidoc.de/shibboleth", ENCODING);
 
         final int httpStatusCode = exception.getHttpStatusCode();
 
         doRedirect(httpResponse, exception.getClass().getName(), message, redirectLocation, httpStatusCode);
     }
 
     /**
      * Redirects the user to the provided Location using the provided statusCode.<br/> Before sending, the no-cache
      * headers are added.
      *
      * @param httpResponse     The http response.
      * @param exceptionName    The name of the exception. If this value is not <code>null</code>, an additional header
      *                         named 'eSciDocException' will be set using the provided value.
      * @param message          The message.
      * @param redirectLocation The location to redirect to.
      * @param httpStatusCode   The http status code.
      * @throws IOException If an errors occurs handling the http response.
      */
     public static void doRedirect(
         final HttpServletResponse httpResponse, final String exceptionName, final String message,
         final String redirectLocation, final int httpStatusCode) throws IOException {
 
         initHttpResponse(httpResponse);
         try {
             httpResponse.setStatus(httpStatusCode);
             httpResponse.setHeader("Location", redirectLocation);
             if (exceptionName != null) {
                 httpResponse.setHeader(HEADER_ESCIDOC_EXCEPTION, exceptionName);
             }
             httpResponse.getWriter().println(message);
         }
         catch (final UnsupportedEncodingException e) {
             doDeclineHttpRequest(httpResponse, new WebserverSystemException(e));
         }
     }
 
     /**
      * Retrieve the value of a param from the query string of the given http servlet request. If the parameter is set
      * but has no value the empty string is returned.
      *
      * @param request The request.
      * @param param   The name of the param.
      * @return The value of the param.
      */
     protected static String getQueryParamValue(final HttpServletRequest request, final String param) {
         String result = null;
         if (request.getQueryString() != null) {
             final StringTokenizer queryToken = new StringTokenizer(request.getQueryString(), "&");
             while (queryToken.hasMoreTokens()) {
                 final String next = queryToken.nextToken();
                 if (next.startsWith(param + '=')) {
                     result = next.substring(param.length() + 1);
                     break;
                 }
                 else if (next.equals(param)) {
                     result = "";
                     break;
 
                 }
             }
         }
         return result;
     }
 
     /**
      * Initializes the provided <code>HttpServletResponse</code> object to prevent caching of the response and to
      * specify the content-type.<br/> The content-type is initialized to the value of XML_RESPONSE_CONTENT_TYPE.
      *
      * @param httpResponse The <code>HttpServletResponse</code> object to that the no-cache headers shall be added.
      */
     private static void initHttpResponse(final HttpServletResponse httpResponse) {
 
         initHttpResponse(httpResponse, XML_RESPONSE_CONTENT_TYPE);
     }
 
     /**
      * Initializes the provided <code>HttpServletResponse</code> object to prevent caching of the response and to
      * specify the content-type.<br/> The content-type is initialized to the value of XML_RESPONSE_CONTENT_TYPE.
      *
      * @param httpResponse The <code>HttpServletResponse</code> object to that the no-cache headers shall be added.
      * @param contentType  The value of the Content-Type header to set.
      */
     private static void initHttpResponse(final HttpServletResponse httpResponse, final String contentType) {
 
         httpResponse.setHeader(HTTP_HEADER_CACHE_CONTROL, HTTP_HEADER_VALUE_NO_CACHE);
         httpResponse.setHeader(HTTP_HEADER_PRAGMA, HTTP_HEADER_VALUE_NO_CACHE);
         httpResponse.setContentType(contentType);
     }
 
     /**
      * Get a cookie from the provided http request.
      *
      * @param name    The name of the cookie.
      * @param request the request.
      * @return The cookie.
      */
     public static Cookie getCookie(final String name, final HttpServletRequest request) {
         Cookie result = null;
         final Cookie[] cookies = request.getCookies();
         if (cookies != null && cookies.length > 0) {
             for (final Cookie cooky : cookies) {
                 if (name.equals(cooky.getName())) {
                     result = cooky;
                     break;
                 }
             }
         }
         return result;
     }
 
     /**
      * Gets the http basic authorization values from the provided http servlet request.<br> The following steps are
      * performed: <ul> <li>If the provided http request contains the http authorization header
      * <code>Authorization</code>, username and password are extracted from the header and returned.</li> <li>If no
      * authorization header has been found, a cookie containing the escidoc handle is searched. If such a cookie is
      * found, the username &quot;Shibboleth-user&quot; and the handle as password are returned.</li> <li>If no
      * authorization header and no valid cookie has been found, a redirect to the escidoc login servlet is sent back and
      * the method returns <code>null</code>. </ul>
      *
      * @param request  The http request.
      * @param response The http response.
      * @return Returns an <code>String</code> array with the user name value at the first position and the password at
      *         the second position.<br> If no Authorization header has been set, <code>null</code> is returned.
      * @throws IOException In case of an I/O error.
      */
     public static String[] getAuthValues(final HttpServletRequest request, final HttpServletResponse response)
         throws IOException {
 
         // Authentication via browser cookie
         final Cookie cookie = getCookie(COOKIE_LOGIN, request);
         if (cookie != null) {
             final String handle = cookie.getValue();
             if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("Received handle in cookie: " + handle);
             }
             return new String[] { "ShibbolethUser", handle };
         }
         // Authentication via Auth-Header
         else if (request.getHeader("Authorization") != null && request.getHeader("Authorization").length() != 0) {
             String authHeader = request.getHeader("Authorization");
             authHeader = authHeader.substring(authHeader.indexOf(' '));
             try {
                 final String decoded = UserHandleCookieUtil.createDecodedUserHandle(authHeader);
                 final int i = decoded.indexOf(':');
                 return new String[] { "ShibbolethUser", decoded.substring(i + 1) };
             }
             catch (final WebserverSystemException e) {
                 throw new IOException("cannot decode user handle", e);
             }
         }
         else {
             LOGGER.info("No handle in cookie received, assuming  anonymous access.");
             return new String[] { "", "" };
         }
     }
 
     /**
      * Remove the user handle from the query string and add it as cookie to the request. Return the modified query
      * string or null.
      *
      * @param httpRequest  servlet request
      * @param httpResponse servlet response
      * @return the modified query string or null
      * @throws ServletException thrown in case of an internal error
      */
     public static String addCookie(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse)
         throws ServletException {
         // Handle problem with eSciDoc user handle information in
         // Request URL. This could be a request from a browser which
         // displays this complete URL in the URL-line.
         // In case of GET and HEAD requests, a redirect to the same
         // URL
         // without the URL parameters is sent.
         String result = null;
         String queryString = httpRequest.getQueryString();
 
         if (queryString != null) {
             final Matcher m = PATTERN_USER_HANDLE_IN_QUERY.matcher(queryString);
 
             if (m.find()) {
                 final String handle = m.group(1);
 
                 queryString = m.replaceAll("");
                 try {
                     httpResponse.addCookie(UserHandleCookieUtil.createAuthCookie(UserHandleCookieUtil
                         .createDecodedUserHandle(handle)));
                 }
                 catch (final WebserverSystemException e) {
                     throw new ServletException(e);
                 }
                 if (queryString.startsWith("&")) {
                     queryString = queryString.substring(1);
                 }
 
                 final String httpMethod = httpRequest.getMethod();
 
                 if (HTTP_GET.equals(httpMethod) || HTTP_HEAD.equals(httpMethod)) {
                     result = queryString;
                 }
             }
         }
         return result;
     }
 }
