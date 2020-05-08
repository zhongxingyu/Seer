 /*
  * Made with all the love in the world
  * by scireum in Remshalden, Germany
  *
  * Copyright by scireum GmbH
  * http://www.scireum.de - info@scireum.de
  */
 
 package sirius.web.http;
 
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.google.common.hash.Hashing;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.jboss.netty.buffer.ChannelBufferInputStream;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.handler.codec.http.*;
 import org.jboss.netty.handler.codec.http.multipart.*;
 import org.jboss.netty.util.CharsetUtil;
 import sirius.kernel.async.CallContext;
 import sirius.kernel.commons.Callback;
 import sirius.kernel.commons.Strings;
 import sirius.kernel.commons.Tuple;
 import sirius.kernel.commons.Value;
 import sirius.kernel.di.std.ConfigValue;
 import sirius.kernel.health.Exceptions;
 import sirius.kernel.health.HandledException;
 import sirius.kernel.nls.NLS;
 import sirius.kernel.xml.StructuredInput;
 import sirius.kernel.xml.XMLStructuredInput;
 
 import javax.annotation.Nonnull;
 import java.io.*;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * Provides access to a request received by the WebServer.
  * <p>
  * This can be used to obtain all infos received for a HTTP request and also to create an appropriate response.
  * </p>
  * <p>
  * This context can either be passed along as variable or be accessed using {@link CallContext#get(Class)}
  * </p>
  *
  * @author Andreas Haufler (aha@scireum.de)
  * @since 2013/08
  */
 public class WebContext {
 
     /**
      * Used to specify the source of a server session
      */
     public static enum ServerSessionSource {
         UNKNOWN, PARAMETER, COOKIE;
     }
 
     /*
      * Underlying channel to send and receive data
      */
     private ChannelHandlerContext ctx;
 
     /*
      * Internal attributes which can be set and read back during processing. This will not contain any posted or
      * other parameters.
      */
     private Map<String, Object> attribute;
 
     /*
      * The underlying request created by netty
      */
     private HttpRequest request;
 
     /*
      * The effective request uri (without the query string)
      */
     private String requestedURI;
 
     /*
      * Contains the parameters submitted in the query string (?param=value...)
      */
     private Map<String, List<String>> queryString;
 
     /*
      * Contains decoded cookies which where sent by the client
      */
     private Map<String, Cookie> cookiesIn;
 
     /*
      * Contains cookies which will be sent to the client
      */
     private Map<String, Cookie> cookiesOut;
 
     /*
      * Stores the decoder which was used to process a POST or PUT request
      */
     private HttpPostRequestDecoder postDecoder;
 
     /*
      * A list of files to deleted once this call is handled
      */
     private List<File> filesToCleanup;
 
     /*
      * If the submitted data (from the client) was stored to a file, this will be stored here
      */
     private File contentAsFile;
 
     /*
      * Raw content submitted via POST or PUT
      */
     protected Attribute content;
 
     /*
      * Contains decoded data of the client session - this is sent back and forth using a cookie. This data
      * will not be stored on the server.
      */
     private Map<String, String> session;
 
     /*
      * Determines if the client session was modified and should be re-set via a cookie
      */
     private boolean sessionModified;
 
     /*
      * Contains the decoded language as two-letter code
      */
     private String lang;
 
     /*
      * Specifies the microtiming key used for this request. If null, no microtiming will be recorded.
      */
     protected String microtimingKey;
 
     /*
      * Used by Response - but stored here, since a new Response might be created....
      */
     protected boolean responseCommitted;
 
     /*
      * Invoked once the call is completely handled
      */
     protected Callback<CallContext> completionCallback;
 
     /*
      * Stores the source of the server session
      */
     private ServerSessionSource serverSessionSource;
 
     /*
      * Stores the requested session id
      */
     private String requestedSessionId;
 
     /*
      * Stores the server session once it was fetched
      */
     private ServerSession serverSession;
 
     /*
      * Name of the cookie used to store and load the client session
      */
     @ConfigValue("http.sessionCookieName")
     private static String sessionCookieName;
 
     /*
      * Shared secret used to protect the client session. If empty one will be created on startup.
      */
     @ConfigValue("http.sessionSecret")
     private static String sessionSecret;
 
     /*
      * Parameter name in which the server session is expected
      */
     @ConfigValue("http.serverSessionParameterName")
     private static String serverSessionParameterName;
 
     /*
      * Cookie name used to store the server session
      */
     @ConfigValue("http.serverSessionCookieName")
     private static String serverSessionCookieName;
 
     /*
      * Context prefix (constant path prefix) used for this server
      */
     @ConfigValue("http.contextPrefix")
     private static String contextPrefix;
 
     /*
      * Input size limit for structured data (as this is loaded into heap)
      */
     @ConfigValue("http.maxStructuredInputSize")
     private static long maxStructuredInputSize;
 
     /*
      * Determines if a dummy P3P header should be created to disable P3P handling.
      */
     @ConfigValue("http.addP3PHeader")
     protected static boolean addP3PHeader;
 
     private static final ObjectMapper mapper = new ObjectMapper();
 
 
     public WebContext() {
     }
 
     /**
      * Provides access to the underlying ChannelHandlerContext
      *
      * @return the underlying channel handler context
      */
     protected ChannelHandlerContext getCtx() {
         return ctx;
     }
 
     /**
      * Enables microtiming for this request.
      * <p>If <tt>null</tt> is passed in as key, the request uri is used.</p>
      * <p>If the microtiming was already enabled, it will remain enabled, with the original key</p>
      *
      * @param key the key used to pass to the microtiming framework.
      * @return <tt>this</tt> to fluently work with this context
      */
     public WebContext enableTiming(String key) {
         if (microtimingKey == null) {
             if (key == null) {
                 microtimingKey = getRequestedURI();
             } else {
                 microtimingKey = key;
             }
         }
 
         return this;
     }
 
     /**
      * Used to provide a handle which is invoked once the call is completely handled.
      * <p>
      * Note that calling this method, removes the last completion handler.
      * </p>
      *
      * @param onComplete the handler to be invoked once the request is completely handled
      */
     public void onComplete(Callback<CallContext> onComplete) {
         completionCallback = onComplete;
     }
 
     /**
      * Sets the ChannelHandlerContext for this context.
      *
      * @param ctx the channel handler context to use
      */
     protected void setCtx(ChannelHandlerContext ctx) {
         this.ctx = ctx;
     }
 
     /**
      * Provides access to the underlying netty HttpRequest
      *
      * @return the underlying request
      */
     public HttpRequest getRequest() {
         return request;
     }
 
     /**
      * Sets the underlying HttpRequest
      *
      * @param request the request on which this context is based
      */
     protected void setRequest(HttpRequest request) {
         this.request = request;
     }
 
     /**
      * Returns a value or parameter supplied by the  request.
      * <p>
      * This method first checks if an attribute with the given key exists. If not, the query string is scanned. After
      * that, the posted content is looked through to find an appropriate value.
      * </p>
      *
      * @param key the key used to look for the value
      * @return a Value representing the provided data.
      */
     @Nonnull
     public Value get(String key) {
         if (attribute != null && attribute.containsKey(key)) {
             return Value.of(attribute.get(key));
         }
         if (queryString.containsKey(key)) {
             List<String> val = getParameters(key);
             if (val.size() == 1) {
                 return Value.of(val.get(0));
             } else if (val.size() == 0) {
                 return Value.of(null);
             } else {
                 return Value.of(val);
             }
         }
         if (postDecoder != null) {
             try {
                 InterfaceHttpData data = postDecoder.getBodyHttpData(key);
                 if (data != null && data instanceof Attribute) {
                     return Value.of(((Attribute) data).getValue());
                 }
             } catch (Throwable e) {
                 Exceptions.handle(WebServer.LOG, e);
             }
         }
         return Value.of(null);
     }
 
     /**
      * Returns the posted part with the given key.
      *
      * @param key used to specify which part of the post request should be returned.
      * @return the data provided for the given key or <tt>null</tt> if no data was supplied.
      */
     public HttpData getHttpData(String key) {
         if (postDecoder == null) {
             return null;
         }
         try {
             InterfaceHttpData data = postDecoder.getBodyHttpData(key);
             if (data != null && data instanceof HttpData) {
                 return (HttpData) data;
             }
         } catch (Throwable e) {
             Exceptions.handle(WebServer.LOG, e);
         }
         return null;
     }
 
     /**
      * Returns the file upload supplied for the given key.
      *
      * @param key used to specify which part of the post request should be used.
      * @return a file upload sent for the given key or <tt>null</tt> if no upload data is available
      */
     public FileUpload getFileData(String key) {
         if (postDecoder == null) {
             return null;
         }
         try {
             InterfaceHttpData data = postDecoder.getBodyHttpData(key);
             if (data != null && data instanceof FileUpload) {
                 return (FileUpload) data;
             }
         } catch (Throwable e) {
             Exceptions.handle(WebServer.LOG, e);
         }
         return null;
     }
 
     /**
      * Sets an attribute for the current request.
      * <p>
      * Attributes are neither stored nor transmitted to the client. Therefore they are only visible during the
      * processing of this request.
      * </p>
      *
      * @param key   name of the attribute
      * @param value value of the attribute
      */
     public void setAttribute(String key, Object value) {
         if (attribute == null) {
             attribute = Maps.newTreeMap();
         }
         attribute.put(key, value);
     }
 
     /*
      * Loads and parses the client session (cookie)
      */
     private void initSession() {
         session = Maps.newHashMap();
         String encodedSession = getCookieValue(sessionCookieName);
         if (Strings.isFilled(encodedSession)) {
             Tuple<String, String> sessionInfo = Strings.split(encodedSession, ":");
             if (Strings.areEqual(sessionInfo.getFirst(),
                                  Hashing.md5().hashString(sessionInfo.getSecond() + getSessionSecret()).toString())) {
                 QueryStringDecoder qsd = new QueryStringDecoder(encodedSession);
                 for (Map.Entry<String, List<String>> entry : qsd.getParameters().entrySet()) {
                     session.put(entry.getKey(), Iterables.getFirst(entry.getValue(), null));
                 }
             } else {
                 WebServer.LOG.FINE("Resetting client session due to security breach: %s", encodedSession);
             }
         }
     }
 
     /**
      * Stores a value in the client session.
      * <p>
      * As this session is transmitted to the client, the given value should not be large and needs a parseable
      * string representation
      * </p>
      *
      * @param key   the name of th value to set
      * @param value the value to set
      */
     public void setSessionValue(String key, Object value) {
         if (session == null) {
             initSession();
         }
         session.put(key, NLS.toMachineString(value));
         sessionModified = true;
     }
 
     /**
      * Loads a value from the client session
      *
      * @param key the name of the value to load
      * @return the value previously set in the session or an empty Value if no data is present
      */
     public Value getSessionValue(String key) {
         if (session == null) {
             initSession();
         }
         return Value.of(session.get(key));
     }
 
     /**
      * Clears (invalidated) the client session by removing all values.
      */
     public void clearSession() {
         if (session != null) {
             session.clear();
             sessionModified = true;
         }
     }
 
     /**
      * Returns the server sided session based on the session parameter or cookie.
      * <p>
      * If no session was found, a new one is created if create is <tt>true</tt>. Otherwise <tt>null</tt> is
      * returned.
      * </p>
      *
      * @param create determines if a new session should be created if no active session was found
      * @return the session associated with the client (based on session id parameter or cookie) or <tt>null</tt> if
      *         neither an active session was found nor a new one was created.
      */
     public ServerSession getServerSession(boolean create) {
         if (serverSession != null) {
             return serverSession;
         }
         if (serverSessionSource == null) {
             requestedSessionId = getParameter(serverSessionParameterName);
             serverSessionSource = ServerSessionSource.PARAMETER;
             if (Strings.isEmpty(requestedSessionId)) {
                 serverSessionSource = ServerSessionSource.COOKIE;
                 requestedSessionId = getCookieValue(serverSessionCookieName);
                 if (Strings.isEmpty(requestedSessionId)) {
                     serverSessionSource = null;
                 }
             }
         }
         if (Strings.isFilled(requestedSessionId) || create) {
             serverSession = ServerSession.getSession(requestedSessionId);
             if (serverSession.isNew()) {
                 serverSession.putValue(ServerSession.INITIAL_URI, getRequestedURI());
                 serverSession.putValue(ServerSession.USER_AGENT, getHeader(HttpHeaders.Names.USER_AGENT));
             }
         }
         return serverSession;
     }
 
     /**
      * Returns the server sided session based on the session parameter or cookie.
      * <p>
      * This method will create a new session if no active session was found.
      * </p>
      * <p>
      * This is a shortcut for <code>getServerSession(true)</code>
      * </p>
      *
      * @return the currently active session for this client. Will create a new session if no active session was found
      */
     public ServerSession getServerSession() {
         return getServerSession(true);
     }
 
     /**
      * Returns the session id requested by the client.
      *
      * @return the session id (server session) sent by the client.
      */
     public String getRequestedSessionId() {
         if (serverSession == null) {
             getServerSession(false);
         }
         return requestedSessionId;
     }
 
     /**
      * Returns the source from which the server session id was obtained.
      * <p>
      * If a session id is submitted via cookie and via parameter, the parameter always has precedence.
      * </p>
      *
      * @return the source from which the session id for the current server session was obtained.
      */
     public ServerSessionSource getServerSessionSource() {
         if (serverSessionSource == null && serverSession == null) {
             getServerSession(false);
         }
 
         return serverSessionSource;
     }
 
     /**
      * Returns the requested URI of the underlying HTTP request, without the query string
      *
      * @return the uri of the underlying request
      */
     public String getRequestedURI() {
         if (requestedURI == null) {
             decodeQueryString();
         }
         return requestedURI;
     }
 
     /**
      * Returns the query string or POST parameter with the given name.
      * <p>
      * If a POST request with query string is present, parameters in the query string have precedence.
      * </p>
      *
      * @param key the name of the parameter to fetch
      * @return the first value or <tt>null</tt> if the parameter was not set or empty
      */
     public String getParameter(String key) {
         return Iterables.getFirst(getParameters(key), null);
     }
 
     /**
      * Returns all query string or POST parameters with the given name.
      * <p>
      * If a POST request with query string is present, parameters in the query string have precedence. If values
      * in the query string are found, the POST parameters are discarded and not added to the resulting list.
      * </p>
      *
      * @param key the name of the parameter to fetch
      * @return all values in the query string
      */
     public List<String> getParameters(String key) {
         if (queryString == null) {
             decodeQueryString();
         }
         if (queryString.containsKey(key)) {
             List<String> result = queryString.get(key);
             if (result == null) {
                 return Collections.emptyList();
             }
             return result;
         }
         if (postDecoder != null) {
             try {
                 List<InterfaceHttpData> data = postDecoder.getBodyHttpDatas(key);
                 if (data == null || data.isEmpty()) {
                     return Collections.emptyList();
                 }
                 List<String> result = new ArrayList<String>();
                 for (InterfaceHttpData dataItem : data) {
                     if (dataItem != null && dataItem instanceof Attribute) {
                         result.add(((Attribute) dataItem).getValue());
                     }
                 }
                 return result;
             } catch (Throwable e) {
                 Exceptions.handle(WebServer.LOG, e);
             }
         }
         return Collections.emptyList();
     }
 
     /*
      * Decodes the query string on demand
      */
     private void decodeQueryString() {
         QueryStringDecoder qsd = new QueryStringDecoder(request.getUri(), CharsetUtil.UTF_8);
         requestedURI = qsd.getPath();
         queryString = qsd.getParameters();
     }
 
     /**
      * Returns all cookies submitted by the client
      *
      * @return a list of cookies sent by the client
      */
     public Collection<Cookie> getCookies() {
         fillCookies();
         return Collections.unmodifiableCollection(cookiesIn.values());
     }
 
     /**
      * Returns a cookie with the given name, sent by the client
      *
      * @param name the cookie to fetch
      * @return the client cookie with the given name, or nzl<tt>null</tt> if no matching cookie was found
      */
     public Cookie getCookie(String name) {
         fillCookies();
 
         return cookiesIn.get(name);
     }
 
     /*
      * Loads the cookies sent by the client
      */
     private void fillCookies() {
         if (cookiesIn == null) {
             cookiesIn = Maps.newHashMap();
             CookieDecoder cd = new CookieDecoder();
             String cookies = request.getHeader(HttpHeaders.Names.COOKIE);
             if (Strings.isFilled(cookies)) {
                 for (Cookie cookie : cd.decode(cookies)) {
                     this.cookiesIn.put(cookie.getName(), cookie);
                 }
             }
         }
     }
 
     /**
      * Returns the data of the given client cookie wrapped as <tt>Value</tt>
      *
      * @param name the cookie to fetch
      * @return the contents of the cookie wrapped as <tt>Value</tt>
      */
     @Nonnull
     public String getCookieValue(String name) {
         Cookie c = getCookie(name);
         if (c == null) {
             return null;
         }
         return c.getValue();
     }
 
     /**
      * Sets the given cookie to be sent back to the client
      *
      * @param cookie the cookie to send to the client
      */
     public void setCookie(Cookie cookie) {
         if (cookiesOut == null) {
             cookiesOut = Maps.newTreeMap();
         }
         cookiesOut.put(cookie.getName(), cookie);
     }
 
     /**
      * Sets a cookie value to be sent back to the client
      * <p>The generated cookie will be a session cookie and varnish once the user agent is closed</p>
      *
      * @param name  the cookie to create
      * @param value the contents of the cookie
      */
     public void setSessionCookie(String name, String value) {
         setCookie(name, value, Integer.MIN_VALUE);
     }
 
     /**
      * Sets a http only cookie value to be sent back to the client.
      * <p>The generated cookie will be a session cookie and varnish once the user agent is closed. Also this cookie
      * will not be accessible by JavaScript and therefore slightly more secure.</p>
      *
      * @param name  the cookie to create
      * @param value the contents of the cookie
      */
     public void setHTTPSessionCookie(String name, String value) {
         DefaultCookie cookie = new DefaultCookie(name, value);
         cookie.setMaxAge((int) Integer.MIN_VALUE);
         cookie.setHttpOnly(true);
         setCookie(cookie);
     }
 
     /**
      * Sets a http only cookie value to be sent back to the client.
      *
      * @param name  the cookie to create
      * @param value the contents of the cookie
      */
     public void setCookie(String name, String value, int maxAgeSeconds) {
         DefaultCookie cookie = new DefaultCookie(name, value);
         cookie.setMaxAge((int) maxAgeSeconds);
         setCookie(cookie);
     }
 
     /**
      * Returns all cookies to be sent to the client. Used by {@link Response} to construct an appropriate header.
      *
      * @return a list of all cookies to be sent to the client.
      */
     protected Collection<Cookie> getOutCookies() {
         if (serverSession != null && serverSession.isNew()) {
             setHTTPSessionCookie(serverSessionCookieName, serverSession.getId());
         }
         if (sessionModified) {
             QueryStringEncoder encoder = new QueryStringEncoder("");
             for (Map.Entry<String, String> e : session.entrySet()) {
                 encoder.addParam(e.getKey(), e.getValue());
             }
             String value = encoder.toString();
             String protection = Hashing.md5().hashString(value + getSessionSecret()).toString();
             setHTTPSessionCookie(sessionCookieName, protection + ":" + value);
         }
         return cookiesOut == null ? null : cookiesOut.values();
     }
 
     /**
      * Returns the accepted language of the client as two-letter language code.
      *
      * @return the two-letter code of the accepted language of the user agent. Returns the current language, if no
      *         supported language was submitted.
      */
     public String getLang() {
         if (lang == null) {
             lang = parseAcceptLanguage();
         }
         return lang;
     }
 
     /*
      * Parses the accept language header
      */
     private String parseAcceptLanguage() {
         double bestQ = 0;
         String lang = CallContext.getCurrent().getLang();
         String header = getHeader(HttpHeaders.Names.ACCEPT_LANGUAGE);
         if (Strings.isEmpty(header)) {
             return lang;
         }
         header = header.toLowerCase();
         for (String str : header.split(",")) {
             String[] arr = str.trim().replace("-", "_").split(";");
 
             //Parse the q-value
             double q = 1.0D;
             for (String s : arr) {
                 s = s.trim();
                 if (s.startsWith("q=")) {
                     q = Double.parseDouble(s.substring(2).trim());
                     break;
                 }
             }
 
             //Parse the locale
             Locale locale = null;
             String[] l = arr[0].split("_");
             if (l.length > 0 && q > bestQ && NLS.isSupportedLanguage(l[0])) {
                 lang = l[0];
                 bestQ = q;
             }
         }
 
         return lang;
     }
 
     /*
      * Secret used to compute the protection keys for client sessions
      */
     private String getSessionSecret() {
         if (Strings.isEmpty(sessionSecret)) {
             sessionSecret = UUID.randomUUID().toString();
         }
         return sessionSecret;
     }
 
     /**
      * Creates a response for this request.
      *
      * @return a new response used to send data to the client.
      */
     public Response respondWith() {
         return new Response(this);
     }
 
     /**
      * Date format used by HTTP date headers
      */
     public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
 
     /**
      * The default timezone used by HTTP dates.
      */
     public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
 
     /**
      * Returns the request header with the given name
      *
      * @param header name of the header to fetch.
      * @return the value of the given header or <tt>null</tt> if no such header is present
      */
     public String getHeader(String header) {
         return request.getHeader(header);
     }
 
     /**
      * Returns the request header wrapped as <tt>Value</tt>
      *
      * @param header name of the header to fetch.
      * @return the contents of the named header wrapped as <tt>Value</tt>
      */
     public Value getHeaderValue(String header) {
         return Value.of(request.getHeader(header));
     }
 
     /**
      * Returns the value of a date header as UNIX timestamp in milliseconds.
      *
      * @param header the name of the header to fetch
      * @return the value in milliseconds of the submitted date or 0 if the header was not present.
      */
     public long getDateHeader(String header) {
         String value = request.getHeader(header);
         if (Strings.isEmpty(value)) {
             return 0;
         }
         try {
             SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
             return dateFormatter.parse(value).getTime();
         } catch (ParseException e) {
             return 0;
         }
     }
 
     /**
      * Returns a collection of all parameters names.
      * <p>
      * This will combine both, the query string and POST parameters.
      * </p>
      *
      * @return a collection of all parameters sent by the client
      */
     public Collection<String> getParameterNames() {
         if (queryString == null) {
             decodeQueryString();
         }
         Set<String> names = Sets.newTreeSet(queryString.keySet());
         if (postDecoder != null) {
             try {
                 for (InterfaceHttpData data : postDecoder.getBodyHttpDatas()) {
                     names.add(data.getName());
                 }
             } catch (Throwable e) {
                 Exceptions.handle(WebServer.LOG, e);
             }
         }
 
         return names;
     }
 
     /**
      * Returns the original query string sent by the client
      *
      * @return the query string (?x=y&z=a...)
      */
     public String getQueryString() {
         return request.getUri().substring(getRequestedURI().length());
     }
 
     /**
      * Returns the context prefix (constant path prefix).
      * <p>
      * Can be used to let the app behave like it would be hosted in a sub directory.
      * </p>
      *
      * @return the content prefix or "" if no prefix is set
      */
     public String getContextPrefix() {
         return contextPrefix;
     }
 
     /**
      * Returns the post decoder used to decode the posted data.
      *
      * @return the post decoder or <tt>null</tt>, if no post request is available
      */
     public HttpPostRequestDecoder getPostDecoder() {
         return postDecoder;
     }
 
     /**
      * Determines if the current request is a POST request.
      * <p>
      * A POST request signal the server to alter its state, knowing that side effects will occur.
      * </p>
      *
      * @return <tt>true</tt> if the method of the current request is POST, false otherwise
      */
     public boolean isPOST() {
         return request.getMethod() == HttpMethod.POST;
     }
 
     /*
      * Sets the post decoder used to decode the posted data
      */
     void setPostDecoder(HttpPostRequestDecoder postDecoder) {
         this.postDecoder = postDecoder;
     }
 
     /**
      * Provides to body of the request as stream.
      *
      * @return an input stream reading from the body of the request.
      */
     public InputStream getContent() throws IOException {
         if (content == null) {
             return null;
         }
         if (!content.isInMemory()) {
             return new FileInputStream(content.getFile());
         }
         //Backup the original size...
         contentSize = (long) content.getChannelBuffer().readableBytes();
         return new ChannelBufferInputStream(content.getChannelBuffer());
     }
 
     /*
      * Caches the content size as the "readableBytes" value changes once a stream is on it.
      */
     private Long contentSize;
 
     /**
      * Returns the size in bytes of the body of the request.
      *
      * @return the size in bytes of the http body.
      */
     public long getContentSize() {
         if (contentSize == null) {
             try {
                 if (content == null) {
                     contentSize = 0L;
                 } else if (!content.isInMemory()) {
                     contentSize = content.getFile().length();
                 } else {
                     contentSize = (long) content.getChannelBuffer().readableBytes();
                 }
             } catch (IOException e) {
                 Exceptions.handle(WebServer.LOG, e);
                 return 0;
             }
         }
         return contentSize;
     }
 
     /**
      * Returns the content of the HTTP request as file on disk.
      * <p>
      * Note that the file will be deleted once the request is completely handled.
      * </p>
      *
      * @return the file pointing to the content sent by the client
      * @throws IOException in case of an IO error
      */
     public File getFileContent() throws IOException {
         if (content == null) {
             return null;
         }
         if (!content.isInMemory()) {
             return content.getFile();
         }
         if (contentAsFile == null) {
             contentAsFile = File.createTempFile("http", "");
             addFileToCleanup(contentAsFile);
             FileOutputStream outputStream = new FileOutputStream(contentAsFile);
             try {
                 outputStream.write(content.get());
             } finally {
                 outputStream.close();
             }
         }
         return contentAsFile;
     }
 
     /**
      * Adds a file to the cleanup list.
      * <p>
      * All files in this list will be deleted once the request is completely handled. This can be used to wipe
      * any intermediate files created while handling this request.
      * </p>
      *
      * @param file the file to be deleted once the request is completed.
      */
     public void addFileToCleanup(File file) {
         if (filesToCleanup == null) {
             filesToCleanup = Lists.newArrayList();
         }
         filesToCleanup.add(file);
     }
 
     /**
      * Returns the body of the HTTP request as XML data.
      * <p>
      * Note that all data is loaded into the heap. Therefore certain limits apply. If the data is too large, an
      * exception will be thrown.
      * </p>
      *
      * @return the body of the HTTP request as XML input
      */
     public StructuredInput getXMLContent() {
         try {
             if (content == null) {
                 throw Exceptions.handle()
                                 .to(WebServer.LOG)
                                 .withSystemErrorMessage("Expected valid XML as body of this request.")
                                 .handle();
             }
             if (content.isInMemory()) {
                 return new XMLStructuredInput(new ByteArrayInputStream(content.get()), true);
             } else {
                 if (content.getFile().length() > maxStructuredInputSize && maxStructuredInputSize > 0) {
                     throw Exceptions.handle()
                                     .to(WebServer.LOG)
                                     .withSystemErrorMessage(
                                             "Request body is too large to parse as XML. The limit is %d bytes",
                                             maxStructuredInputSize)
                                     .handle();
                 }
                 return new XMLStructuredInput(new FileInputStream(content.getFile()), true);
             }
         } catch (HandledException e) {
             throw e;
         } catch (Throwable e) {
             throw Exceptions.handle()
                             .to(WebServer.LOG)
                             .error(e)
                             .withSystemErrorMessage("Expected valid XML as body of this request: %s (%s).")
                             .handle();
         }
     }
 
     /**
      * Returns the body of the HTTP request as JSON data.
      * <p>
      * Note that all data is loaded into the heap. Therefore certain limits apply. If the data is too large, an
      * exception will be thrown.
      * </p>
      *
      * @return the body of the HTTP request as JSON input
      */
     public Map<String, Object> getJSONContent() {
         try {
             if (content == null) {
                 throw Exceptions.handle()
                                 .to(WebServer.LOG)
                                 .withSystemErrorMessage("Expected a valid JSON map as body of this request.")
                                 .handle();
             }
             if (content.isInMemory()) {
                 return mapper.readValue(new ByteArrayInputStream(content.get()), Map.class);
             } else {
                 if (content.getFile().length() > maxStructuredInputSize && maxStructuredInputSize > 0) {
                     throw Exceptions.handle()
                                     .to(WebServer.LOG)
                                     .withSystemErrorMessage(
                                             "Request body is too large to parse as JSON. The limit is %d bytes",
                                             maxStructuredInputSize)
                                     .handle();
                 }
                 return mapper.readValue(content.getFile(), Map.class);
             }
         } catch (HandledException e) {
             throw e;
         } catch (Throwable e) {
             throw Exceptions.handle()
                             .to(WebServer.LOG)
                             .error(e)
                             .withSystemErrorMessage("Expected a valid JSON map as body of this request: %s (%s).")
                             .handle();
         }
     }
 
     /**
      * Releases all data associated with this request.
      */
     void release() {
         if (postDecoder != null) {
             try {
                 postDecoder.cleanFiles();
             } catch (Exception e) {
                 Exceptions.handle(WebServer.LOG, e);
             }
             postDecoder = null;
         }
         if (content != null) {
             // Delete manually if anything like a file or so was allocated
             try {
                 content.delete();
             } catch (Exception e) {
                 Exceptions.handle(WebServer.LOG, e);
             }
             // Also tell the factory to release all allocated data, as it keeps an internal reference to the request
             // (...along with all its data!).
             try {
                 WebServer.getHttpDataFactory().cleanRequestHttpDatas(request);
             } catch (Exception e) {
                 Exceptions.handle(WebServer.LOG, e);
             }
             content = null;
             contentAsFile = null;
         }
         if (filesToCleanup != null) {
             for (File file : filesToCleanup) {
                 try {
                     if (file != null && file.exists()) {
                         file.delete();
                     }
                 } catch (Exception e) {
                     Exceptions.handle(WebServer.LOG, e);
                 }
 
             }
             filesToCleanup = null;
         }
     }
 }
