 package com.readmill.api;
 
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.*;
 import org.apache.http.client.params.HttpClientParams;
 import org.apache.http.conn.ConnectionKeepAliveStrategy;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HttpContext;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 
 public class ReadmillWrapper {
   private String mClientId;
   private String mClientSecret;
   private Environment mEnv;
   private Token mToken;
   private HttpClient mHttpClient;
   private URI mRedirectURI;
   private String mScope;
   private String mUserAgent;
 
   /**
    * A list of clients that are interested to know when the token has changed.
    */
   private TokenChangeListener mTokenListener;
 
   /**
    * Creates a wrapper for a given client and environment.
    *
    * @param clientId     Client Identifier
    * @param clientSecret Client secret
    * @param env          Server environment
    */
   public ReadmillWrapper(String clientId, String clientSecret, Environment env) {
     mClientId = clientId;
     mClientSecret = clientSecret;
     mEnv = env;
   }
 
   /**
    * Gets the current client id
    *
    * @return The current client id or null if not set.
    */
   public String getClientId() {
     return mClientId;
   }
 
   /**
    * Gets the current client secret
    *
    * @return The current client secret or null if not set.
    */
   public String getClientSecret() {
     return mClientSecret;
   }
 
   /**
    * Gets the current environment
    *
    * @return The current environment or null if not set.
    */
   public Environment getEnvironment() {
     return mEnv;
   }
 
   /**
    * Gets the current token
    *
    * @return The current token or null if not set.
    */
 
   public Token getToken() {
     return mToken;
   }
 
   /**
    * Sets the token used for requests.
    *
    * @param token token used for authenticating requests.
    */
   public void setToken(Token token) {
     mToken = token;
     if (mTokenListener != null) {
       mTokenListener.onTokenChanged(token);
     }
   }
 
   /**
    * Sets the current token change listener.
    *
    * @param listener The client that receives notifications about token changes.
    */
   public void setTokenChangeListener(TokenChangeListener listener) {
     mTokenListener = listener;
   }
 
   /**
    * Sets the redirect uri used for generating an authentication url
    * and for obtaining a token.
    *
    * @param redirectURI Redirection uri for requesting tokens
    */
   public void setRedirectURI(URI redirectURI) {
     mRedirectURI = redirectURI;
   }
 
   /**
    * Sets the scope for which to ask authorization.
    * <p/>
    * Affects getAuthorizationURL() and obtainToken().
    *
    * @param scope scope to ask permission for
    */
   public void setScope(String scope) {
     mScope = scope;
   }
 
   /**
    * Sets the user agent string to use for all subsequent requests.
    *
    * @param userAgent the string to send as "User-Agent" header
    */
   public void setUserAgent(String userAgent) {
     mUserAgent = userAgent;
   }
 
   /**
    * Gets the user agent string used for requests.
    */
   public String getUserAgent() {
       return mUserAgent;
   }
 
   /**
    * Constructs a url to where the user can authenticate the wrapper.
    *
    * @return The url or null if no redirect uri is set or if it is invalid.
    */
   public URL getAuthorizationURL() {
     if (mRedirectURI == null) {
       return null;
     }
 
     try {
       String template = "%s/oauth/authorize?response_type=code&client_id=%s&redirect_uri=%s";
       String authorizeURL = String.format(template, mEnv.getWebHost().toURI(), mClientId, mRedirectURI.toString());
 
       if (mScope != null) {
         authorizeURL += "&scope=" + mScope;
       }
 
       return URI.create(authorizeURL).toURL();
     } catch (MalformedURLException e) {
       e.printStackTrace();
       return null;
     }
   }
 
   /**
    * Obtains a token by exchanging an authorization code.
    *
    * @param authorizationCode Authorization code
    * @return The obtained token or null
    * @see #obtainTokenOrThrow(String)
    */
   public Token obtainToken(String authorizationCode) {
     try {
       return obtainTokenOrThrow(authorizationCode);
     } catch (IOException e) {
       e.printStackTrace();
     } catch (JSONException e) {
       e.printStackTrace();
     }
     return null;
   }
 
   /**
    * Obtains a token by providing an authorization code.
    * <p/>
    * Uses the redirect uri and scope set on the wrapper with setRedirectURI()
    * and #setScope().
    * <p/>
    * Obtaining a token only works for an authorization code that was requested
    * with the same redirect uri and scope.
    *
    * @param authorizationCode The authorization code
    * @return The obtained token
    * @throws IOException   if a network error occurs
    * @throws JSONException if the response was not proper json
    */
   public Token obtainTokenOrThrow(String authorizationCode) throws IOException, JSONException {
     if (mRedirectURI == null) {
       throw new RuntimeException("Redirect URI must be set before calling obtainToken()");
     }
 
     String resourceUrl = String.format("%s/oauth/token", mEnv.getWebHost());
 
     Request obtainRequest = Request.to(resourceUrl).withParams(
       "grant_type", "authorization_code",
       "client_id", mClientId,
       "client_secret", mClientSecret,
       "code", authorizationCode,
       "redirect_uri", mRedirectURI
     );
 
     if (mScope != null) {
       obtainRequest.withParams("scope", mScope);
     }
 
     String tokenResponse = getResponseText(obtainRequest, HttpPost.class);
     JSONObject tokenJson = new JSONObject(tokenResponse);
     return new Token(tokenJson);
   }
 
   /**
    * Obtains a token by providing username and password credentials.
    * <p/>
    * Uses scope set on the wrapper with #setScope().
    *
    * @param username The Readmill account username (email address)
    * @param password The account password
    * @return The obtained token
    * @throws IOException   if a network error occurs
    * @throws JSONException if the response was not proper json
    */
   public Token login(String username, String password) throws IOException, JSONException {
     if (username == null || password == null) {
       throw new IllegalArgumentException("username or password is null");
     }
 
     String resourceUrl = String.format("%s/oauth/token", mEnv.getWebHost());
 
     final Request obtainRequest = Request.to(resourceUrl).withParams(
       "grant_type", "password",
       "client_id", mClientId,
       "client_secret", mClientSecret,
       "username", username,
       "password", password
     );
 
     if (mScope != null) {
       obtainRequest.withParams("scope", mScope);
     }
 
     String tokenResponse = getResponseText(obtainRequest, HttpPost.class);
     JSONObject tokenJson = new JSONObject(tokenResponse);
     return new Token(tokenJson);
   }
 
   /**
    * Gets the http client used to make requests.
    *
    * @return The HttpClient used for making requests with this wrapper
    */
   public HttpClient getHttpClient() {
     if (mHttpClient == null) {
       mHttpClient = createHttpClient();
     }
     return mHttpClient;
   }
 
   /**
    * Creates the HttpClient used for making requests to the API.
    * <p/>
    * Override this method if you want to use a different configuration.
    *
    * @return the
    */
   protected HttpClient createHttpClient() {
     HttpParams httpParams = new BasicHttpParams();
 
     HttpClientParams.setRedirecting(httpParams, false);
 
     Scheme httpScheme = new Scheme("http", PlainSocketFactory.getSocketFactory(), 80);
     Scheme httpsScheme = new Scheme("https", SSLSocketFactory.getSocketFactory(), 443);
 
     final SchemeRegistry registry = new SchemeRegistry();
     registry.register(httpScheme);
     registry.register(httpsScheme);
 
     return new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams, registry), httpParams) {
       {
         setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
           @Override
           public long getKeepAliveDuration(HttpResponse httpResponse, HttpContext httpContext) {
             return 20 * 1000;
           }
         });
       }
     };
   }
 
   /**
    * Starts building a GET request to a given endpoint.
    *
    * @param uri Endpoint for the request
   * @return a RequestBuilder for a request to the given endpint and verb
    */
   public RequestBuilder get(String uri) {
     return new RequestBuilder(this, HttpGet.class, uri);
   }
 
   /**
    * Starts building a HEAD request to a given endpoint.
    *
    * @param uri Endpoint for the request
    * @return a RequestBuilder for a request to the given endpoint and verb
    */
   public RequestBuilder head(String uri) {
     return new RequestBuilder(this, HttpHead.class, uri);
   }
 
   /**
    * Starts building a POST request to a given endpoint.
    *
    * @param uri Endpoint for the request
   * @return a RequestBuilder for a request to the given endpint and verb
    */
   public RequestBuilder post(String uri) {
     return new RequestBuilder(this, HttpPost.class, uri);
   }
 
   /**
    * Starts building a DELETE request to a given endpoint.
    *
    * @param uri Endpoint for the request
   * @return a RequestBuilder for a request to the given endpint and verb
    */
   public RequestBuilder delete(String uri) {
     return new RequestBuilder(this, HttpDelete.class, uri);
   }
 
   /**
    * Starts building a PUT request to a given endpoint.
    *
    * @param uri Endpoint for the request
   * @return a RequestBuilder for a request to the given endpint and verb
    */
   public RequestBuilder put(String uri) {
     return new RequestBuilder(this, HttpPut.class, uri);
   }
 
 
   /**
    * Sends a request as GET.
    *
    * @param request Request to send
    * @return The HttpResponse
    * @throws IOException if a network error occurred
    */
   public HttpResponse get(Request request) throws IOException {
     return execute(request, HttpGet.class);
   }
 
   /**
    * Sends a request as HEAD.
    *
    * @param request Request to send
    * @return The HttpResponse
    * @throws IOException if a network error occurred
    */
   public HttpResponse head(Request request) throws IOException {
     return execute(request, HttpHead.class);
   }
 
   /**
    * Sends a request as PUT.
    *
    * @param request Request to send
    * @return The HttpResponse
    * @throws IOException if a network error occurred
    */
   public HttpResponse put(Request request) throws IOException {
     return execute(request, HttpPut.class);
   }
 
   /**
    * Sends a request as POST.
    *
    * @param request Request to send
    * @return The HttpResponse
    * @throws IOException if a network error occurred
    */
   public HttpResponse post(Request request) throws IOException {
     return execute(request, HttpPost.class);
   }
 
   /**
    * Sends a request as DELETE.
    *
    * @param request Request to send
    * @return The HttpResponse
    * @throws IOException if a network error occurred
    */
   public HttpResponse delete(Request request) throws IOException {
     return execute(request, HttpDelete.class);
   }
 
 
   /*
   * Protected
   */
 
   /**
    * Executes a request with a provided HTTP verb.
    *
    * @param request Request to execute
    * @param klass   HTTP verb to use for request (HttpPost, HttpGet, etc.)
    * @return The HttpResponse
    * @throws java.io.IOException if a network error occurs
    */
   protected HttpResponse execute(Request request, Class<? extends HttpRequestBase> klass) throws IOException {
     authorizeRequest(request);
     HttpRequestBase httpRequest = request.build(klass);
     if(mUserAgent != null) {
         httpRequest.setHeader("User-Agent", mUserAgent);
     }
     return getHttpClient().execute(resolveTarget(request), httpRequest);
   }
 
   /**
    * Executes a request and return the response body as a string.
    *
    * @param request Request to execute
    * @param klass   HttpRequest class to execute as (HttpPost, HttpGet etc)
    * @return The response body as a string
    * @throws IOException if a network error occurs
    */
   protected String getResponseText(Request request, Class<? extends HttpRequestBase> klass) throws IOException {
     HttpResponse response = execute(request, klass);
     return HttpUtils.getString(response);
   }
 
   /**
    * Adds the strongest available authorization to a request.
    * <p/>
    * Order of authentication strenghts (strongest to weakest)
    * <p/>
    * - Token assigned to the Request
    * - Token assigned to the wrapper
    * - Client id assigned to the wrapper
    *
    * @param request Request to authorize.
    * @return The authorized request.
    */
   protected Request authorizeRequest(Request request) {
     if (request.getToken() != null) {
       // Already authenticated with token
       return request;
     }
 
     if (mToken != null) {
       // Authenticate with wrapper token
       return request.usingToken(mToken);
     }
 
     if (mClientId != null) {
       // Authenticate with client id
       return request.withParams("client_id", mClientId);
     }
 
     return request;
   }
 
   // Private
 
   /**
    * Resolves the target host to use when executing HTTP requests.
    *
    * @param request The request to resolve target for
    * @return The resolved HttpHost
    */
   private HttpHost resolveTarget(Request request) {
     URI uri = URI.create(mEnv.getApiHost().toURI()).resolve(request.toUrl());
     return new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
   }
 }
