 
 /***************************************************************************
  *   Copyright (C) 2010 iCoreTech research labs                            *
  *   Contributed code from:                                                *
  *   - Valerio Chiodino - keytwo at keytwo dot net                         *
  *   - Fabio Tunno      - fat at fatshotty dot net                         *
  *                                                                         *
  *   This program is free software: you can redistribute it and/or modify  *
  *   it under the terms of the GNU General Public License as published by  *
  *   the Free Software Foundation, either version 3 of the License, or     *
  *   (at your option) any later version.                                   *
  *                                                                         *
  *   This program is distributed in the hope that it will be useful,       *
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
  *   GNU General Public License for more details.                          *
  *                                                                         *
  *   You should have received a copy of the GNU General Public License     *
  *   along with this program. If not, see http://www.gnu.org/licenses/     *
  *                                                                         *
  ***************************************************************************/
 
 package fm.audiobox;
 
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.zip.GZIPInputStream;
 
 import org.apache.http.Header;
 import org.apache.http.HeaderElement;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpException;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpRequestInterceptor;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpResponseInterceptor;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpHead;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.conn.params.ConnManagerParams;
 import org.apache.http.conn.params.ConnPerRouteBean;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.entity.HttpEntityWrapper;
 import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.protocol.HttpContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fm.audiobox.core.exceptions.LoginException;
 import fm.audiobox.core.exceptions.ServiceException;
 import fm.audiobox.core.models.AbstractCollectionEntity;
 import fm.audiobox.core.models.User;
 import fm.audiobox.core.observables.Event;
 import fm.audiobox.interfaces.IConfiguration;
 import fm.audiobox.interfaces.IConfiguration.Connectors;
 import fm.audiobox.interfaces.IConfiguration.ContentFormat;
 import fm.audiobox.interfaces.IConnector;
 import fm.audiobox.interfaces.IEntity;
 import fm.audiobox.interfaces.IFactory;
 
 
 /**
  * AudioBox is the main class.<br />
  * It uses a {@link IConfiguration} class.
  * To get information about {@link User user} use the {@link AudioBox#getUser()} method.
  * <p>
  *
  * Keep in mind that this library provides only the common browsing actions and some other few feature.<br/>
  * AudioBox does not streams, nor play or provide a BitmapFactory for albums covers.
  *
  * <p>
  * You can extend default extendable models setting them into {@link IFactory#setEntity(String, Class)} class through {@link IConfiguration#getFactory()} method 
  *
  * <p>
  *
  * Note that some of the requests, such as the {@link AbstractCollectionEntity} population requests, can be done
  * asynchronously.<br/>
  * To keep track of the collection building process you can use {@link Observer}.
  *
  */
 public class AudioBox extends Observable {
 
   private static Logger log = LoggerFactory.getLogger(AudioBox.class);
 
 
   /** Prefix used to store each property into properties file */
   public static final String PREFIX = AudioBox.class.getPackage().getName() + ".";
 
   private final IConfiguration configuration;
   private User user;
 
   
   /**
    * Creates a new {@code AudioBox} instance ready to be used
    * @param config the {@link IConfiguration} used by this instance
    */
   public AudioBox(IConfiguration config) {
     this(config, IConfiguration.Environments.live);
   }
   
   /**
    * Creates a new {@code AudioBox} instance ready to be used
    * @param config the {@link IConfiguration} used by this instance
    */
   public AudioBox(IConfiguration config, IConfiguration.Environments env) {
     log.trace("New AudioBox is going to be instantiated");
     this.configuration = config;
     
     this.setEnvironment(env);
 
     log.trace("New AudioBox correctly instantiated");
   }
 
 
 
   /**
    * @return Returns the {@code environment} of the connectors
    */
   public IConfiguration.Environments getEnvironment(){
     return this.configuration.getEnvironment();
   }
   
   /**
    * This method is used for setting the {@code environemnt} of the connectors
    */
   @SuppressWarnings("deprecation")
   public void setEnvironment(IConfiguration.Environments env) {
     this.configuration.setEnvironment(env);
     
     // Create connectors
     IConnector standardConnector = new Connector(IConfiguration.Connectors.RAILS);
     IConnector uploaderConnector = new Connector(IConfiguration.Connectors.NODE);
     IConnector daemonerConnector = new Connector(IConfiguration.Connectors.DAEMON);
 
     this.configuration.getFactory().addConnector(IConfiguration.Connectors.RAILS, standardConnector );
     this.configuration.getFactory().addConnector(IConfiguration.Connectors.NODE, uploaderConnector );
     this.configuration.getFactory().addConnector(IConfiguration.Connectors.DAEMON, daemonerConnector );
     
     log.info("Environment set to: " + env);
   }
 
 
   /**
    * Getter method for the {@link User user} Object
    * <p>Note: it can be {@code null} if {@code user} is not logged in<p>
    *
    * @return current {@link User} instance
    */
   public User getUser(){
     return this.user;
   }
 
 
   /**
    * This is the main method returns the {@link User} instance.
    * <p>
    * It performs a login on AudioBox.fm and returns the logged in User.
    * </p>
    * 
    * <p>
    * It fires the {@link Event.States#CONNECTED} event passing the {@link User}.
    * </p>
    * 
    * @param username is the {@code email} of the user
    * @param password of the User
    * @param async make this request asynchronously. <b>{@code async} should be always {@code false}</b>
    * 
    * @return the {@link User} instance if everything went ok
    * 
    * @throws LoginException identifies invalid credentials or user cannot be logged in due to subscription error.
    * @throws ServiceException if any connection problem occurs.
    */
   public User login(final String username, final String password, boolean async) throws LoginException, ServiceException {
     log.info("Executing login for user: " + username);
 
     // Destroy old user's pointer
     this.logout();
 
     User user = (User) this.configuration.getFactory().getEntity(User.TAGNAME, this.getConfiguration() );
     user.setUsername(username);
     user.setPassword( password );
     
     user.load(async);
     
     // User can now be set. Note: set user before notifing observers
     this.user = user;
 
     // User has been authenticated, notify observers
     Event event = new Event(this.user, Event.States.CONNECTED);
     this.setChanged();
     this.notifyObservers(event);
 
     return this.user;
   }
 
  
   /**
    * It calls the {@link AudioBox#login(String, String, boolean)} passing {@code false} as {@code async}
    * switch
    * @param username is the {@code email} of the user
    * @param password of the User
    * 
    * @return the {@link User} instance if everything went ok
    */
   public User login(String username, String password) throws LoginException, ServiceException {
     return this.login( username, password, false);
   }
 
 
   /**
    * Logouts the current logged in {@link User}.
    * <p>
    * This method clear the {@link User} instance and 
    * fires the {@link Event.States#DISCONNECTED} passing no arguments
    * </p>
    * <p>
    * <b>Note: this method set User to {@code null}</b>
    * </p>
    * 
    * @return boolean: {@code true} if User has been logged out. {@code false} if no logged in user found
    */
   public boolean logout() {
 
     if ( this.user != null ) {
       // Destroy old user's pointer
       this.user = null;
 
 
       // notify all observer User has been destroyed
       Event event = new Event(new Object(), Event.States.DISCONNECTED);
       this.setChanged();
       this.notifyObservers(event);
 
       return true;
     }
 
     return false;
   }
 
 
   /**
    * Gets current {@link IConfiguration} related to this instance
    * @return current {@link IConfiguration}
    */
   public IConfiguration getConfiguration(){
     return this.configuration;
   }
 
 
 
 
   /**
    * Connector is the AudioBox http request wrapper.
    * 
    * <p>
    * This class intantiates a {@link IConnector.IConnectionMethod} used for
    * invoking AudioBox.fm server through HTTP requests
    * </p>
    * <p>
    * Note: you can use your own {@code IConnection} class
    * using {@link IFactory#addConnector(Connectors, IConnector)} method
    * </p>
    */
   public class Connector implements Serializable, IConnector {
 
     private final Logger log = LoggerFactory.getLogger(Connector.class);
 
     private static final long serialVersionUID = -1947929692214926338L;
 
     // Default value of the server
     private IConfiguration.Connectors SERVER = IConfiguration.Connectors.RAILS;
 
     /** Get informations from configuration file */
     private String PROTOCOL = "";
     private String HOST = "";
     private String PORT = "";
 
     private String API_PATH = "";
 
     private ThreadSafeClientConnManager mCm;
     private DefaultHttpClient mClient;
 
 
 
     /** Default constructor builds http connector */
     protected Connector(IConfiguration.Connectors server) {
 
       log.debug("New Connector is going to be instantiated, server: " + server.toString() );
 
       SERVER = server;
       PROTOCOL = configuration.getProtocol( SERVER );
       HOST = configuration.getHost( SERVER );
       PORT = String.valueOf( configuration.getPort( SERVER ) );
 
       API_PATH = PROTOCOL + "://" + HOST + ":" + PORT;
 
       log.info("Remote host for " + server.toString() + " will be: " + API_PATH );
 
       buildClient();
     }
 
 
     public void abort() {
       this.destroy();
       buildClient();
     }
 
     public void destroy() {
       log.warn("All older requests will be aborted");
       this.mCm.shutdown();
       this.mCm = null;
       this.mClient = null;
     }
 
     /**
      * Use this method to configure the timeout limit for reqests made against AudioBox.fm.
      *
      * @param timeout the milliseconds of the timeout limit
      */
     public void setTimeout(int timeout) {
       log.info("Setting timeout parameter to: " + timeout);
       HttpConnectionParams.setConnectionTimeout( mClient.getParams() , timeout);
     }
 
 
     public int getTimeout() {
       return HttpConnectionParams.getConnectionTimeout( mClient.getParams() );
     }
 
 
     /**
      * Creates a HttpRequestBase
      *
      * @param httpVerb the HTTP method to use for the request (ie: GET, PUT, POST and DELETE)
      * @param source usually reffers the Model that invokes method
      * @param dest Model that intercepts the response
      * @param action the remote action to execute on the model that executes the action (ex. "scrobble")
      * @param entity HttpEntity used by POST and PUT method
      *
      * @return the HttpRequestBase
      */
     private HttpRequestBase createConnectionMethod(String httpVerb, String path, String action, ContentFormat format, List<NameValuePair> params) {
 
       if ( httpVerb == null ) {
         httpVerb = IConnectionMethod.METHOD_GET;
       }
 
       String url = this.buildRequestUrl(path, action, httpVerb, format, params);
 
       HttpRequestBase method = null;
 
       if ( IConnectionMethod.METHOD_POST.equals( httpVerb ) ) {
         log.debug("Building HttpMethod POST");
         method = new HttpPost(url);
       } else if ( IConnectionMethod.METHOD_PUT.equals( httpVerb ) ) {
         log.debug("Building HttpMethod PUT");
         method = new HttpPut(url);
       } else if ( IConnectionMethod.METHOD_DELETE.equals( httpVerb ) ) {
         log.debug("Building HttpMethod DELETE");
         method = new HttpDelete(url);
       } else if ( IConnectionMethod.METHOD_HEAD.equals( httpVerb ) ) {
         log.debug("Building HttpMethod HEAD");
         method = new HttpHead(url);
       } else {
         log.debug("Building HttpMethod GET");
         method = new HttpGet(url);
       }
 
       log.info( "[ " + httpVerb + " ] " + url );
 
       if ( log.isDebugEnabled() ) {
         log.debug("Setting default headers");
         log.debug("-> Accept-Encoding: gzip");
         log.debug("-> User-Agent: " + getConfiguration().getUserAgent() );
       }
 
       if ( getConfiguration().getEnvironment() == IConfiguration.Environments.live ){
         method.addHeader("Accept-Encoding", "gzip");
       }
       method.addHeader("User-Agent",  getConfiguration().getUserAgent());
 
       return method;
     }
 
 
     public IConnectionMethod head(IEntity destEntity, String action, List<NameValuePair> params) {
       return head(destEntity, destEntity.getApiPath(), action, params);
     }
 
 
     public IConnectionMethod head(IEntity destEntity, String path, String action, List<NameValuePair> params) {
       return head(destEntity, path, action, getConfiguration().getRequestFormat(), params);
     }
 
     public IConnectionMethod head(IEntity destEntity, String path, String action, ContentFormat format, List<NameValuePair> params) {
       IConnectionMethod method = getConnectionMethod();
 
       if ( method != null ) {
         HttpRequestBase originalMethod = this.createConnectionMethod(IConnectionMethod.METHOD_HEAD, path, action, format, params);
         method.init(destEntity, originalMethod, this.mClient, getConfiguration(), format );
         method.setUser( user );
       }
 
       return method;
     }
     
 
     public IConnectionMethod get(IEntity destEntity, String action, List<NameValuePair> params) {
      return get(destEntity, destEntity.getApiPath(), action, params);
     }
 
 
     public IConnectionMethod get(IEntity destEntity, String path, String action, List<NameValuePair> params) {
      return get(destEntity, path, action, getConfiguration().getRequestFormat(), params);
     }
 
     public IConnectionMethod get(IEntity destEntity, String path, String action, ContentFormat format, List<NameValuePair> params) {
       IConnectionMethod method = getConnectionMethod();
 
       if ( method != null ) {
         HttpRequestBase originalMethod = this.createConnectionMethod(IConnectionMethod.METHOD_GET, path, action, format, params);
         method.init(destEntity, originalMethod, this.mClient, getConfiguration(), format );
         method.setUser( user );
       }
 
       return method;
     }
 
 
 
     public IConnectionMethod put(IEntity destEntity, String action) {
       return put(destEntity, destEntity.getApiPath(), action, getConfiguration().getRequestFormat() );
     }
 
     public IConnectionMethod put(IEntity destEntity, String path, String action) {
       return put(destEntity, path, action, getConfiguration().getRequestFormat() );
     }
 
     public IConnectionMethod put(IEntity destEntity, String path, String action, ContentFormat format) {
       IConnectionMethod method = getConnectionMethod();
 
       if ( method != null ) {
         HttpRequestBase originalMethod = this.createConnectionMethod(IConnectionMethod.METHOD_PUT, path, action, format, null);
         method.init(destEntity, originalMethod, this.mClient, getConfiguration(), format );
         method.setUser( user );
       }
 
       return method;
     }
 
 
 
     public IConnectionMethod post(IEntity destEntity, String action) {
       return this.post(destEntity, destEntity.getApiPath(), action);
     }
 
     public IConnectionMethod post(IEntity destEntity, String path, String action) {
       return this.post(destEntity, path, action, getConfiguration().getRequestFormat());
     }
 
     public IConnectionMethod post(IEntity destEntity, String path, String action, ContentFormat format) {
       IConnectionMethod method = getConnectionMethod();
 
       if ( method != null ) {
         HttpRequestBase originalMethod = this.createConnectionMethod(IConnectionMethod.METHOD_POST, path, action, format, null);
         method.init(destEntity, originalMethod, this.mClient, getConfiguration(), format );
         method.setUser( user );
       }
 
       return method;
     }
 
 
     public IConnectionMethod delete(IEntity destEntity, String action, List<NameValuePair> params) {
      return delete(destEntity, destEntity.getApiPath(), action, params);
     }
 
     public IConnectionMethod delete(IEntity destEntity, String path, String action, List<NameValuePair> params) {
      return delete(destEntity, path, action, getConfiguration().getRequestFormat(), params);
     }
 
     public IConnectionMethod delete(IEntity destEntity, String path, String action, ContentFormat format, List<NameValuePair> params) {
       IConnectionMethod method = getConnectionMethod();
 
       if ( method != null ) {
         HttpRequestBase originalMethod = this.createConnectionMethod(IConnectionMethod.METHOD_DELETE, path, action, format, params);
         method.init(destEntity, originalMethod, this.mClient, getConfiguration(), format );
         method.setUser( user );
       }
 
       return method;
     }
 
     
     
     
     /* --------------- */
     /* Private methods */
     /* --------------- */
 
 
     /**
      * This method is used to build the HttpClient used for connections
      */
     private void buildClient() {
 
       //   this.mAudioBoxRoute = new HttpRoute(new HttpHost( HOST, Integer.parseInt(PORT) ) );
 
       SchemeRegistry schemeRegistry = new SchemeRegistry();
       schemeRegistry.register( new Scheme("http", PlainSocketFactory.getSocketFactory(), Integer.parseInt( PORT ) ));
       schemeRegistry.register( new Scheme("https", SSLSocketFactory.getSocketFactory(), 443 ));
 
       HttpParams params = new BasicHttpParams();
      params.setParameter("http.protocol.-charset", "UTF-8");
 
       HttpConnectionParams.setConnectionTimeout(params, 30 * 1000);
       HttpConnectionParams.setSoTimeout(params, 30 * 1000);
       
 
       ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(50));
 
       this.mCm = new ThreadSafeClientConnManager(params, schemeRegistry);
       this.mClient = new DefaultHttpClient( this.mCm, params );
       
       this.mClient.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy() {
         public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
           long keepAlive = super.getKeepAliveDuration(response, context);
           if (keepAlive == -1) {
               // Keep connections alive 5 seconds if a keep-alive value 
               // has not be explicitly set by the server
               keepAlive = 5000;
           }
           return keepAlive;
         }
       });
       
       if ( log.isDebugEnabled() ) {
         this.mClient.addRequestInterceptor(new HttpRequestInterceptor() {
           public void process( final HttpRequest request,  final HttpContext context) throws HttpException, IOException {
             log.debug("New request detected");
           }
         });
       }
 
 
 
       this.mClient.addResponseInterceptor(new HttpResponseInterceptor() {
 
         public void process( final HttpResponse response, final HttpContext context) throws HttpException, IOException {
           log.trace("New response intercepted");
           HttpEntity entity = response.getEntity();
           if (entity != null) {
             Header ceheader = entity.getContentEncoding();
             if (ceheader != null) {
               HeaderElement[] codecs = ceheader.getElements();
               for (int i = 0; i < codecs.length; i++) {
                 if (codecs[i].getName().equalsIgnoreCase("gzip")) {
                   log.info("Response is gzipped");
 
                   response.setEntity(new HttpEntityWrapper(entity){
                     private GZIPInputStream stream = null;
                     
                     @Override
                     public InputStream getContent() throws IOException, IllegalStateException {
                       // the wrapped entity's getContent() decides about repeatability
                       if ( stream == null ) {
                         InputStream wrappedin = wrappedEntity.getContent();
                         stream = new GZIPInputStream(wrappedin);
                       }
                       return stream;
                     }
 
                     @Override
                     public long getContentLength() { return 1; }
 
                   });
 
                   return;
                 }
               }
             }
           }
         }
       });
 
     }
 
 
     /**
      * This method creates a {@link IConnector.IConnectionMethod} class
      * that will be used for invoking AudioBox.fm servers
      * <p>
      * Note: you can use your own {@code IConnectionMethod} class using {@link IConfiguration#setHttpMethodType(Class)} method
      * </p> 
      * @return the {@link IConnector.IConnectionMethod} associated with this {@link AudioBox} class
      */
     protected IConnectionMethod getConnectionMethod(){
 
       Class<? extends IConnectionMethod> klass = getConfiguration().getHttpMethodType();
 
       if ( log.isDebugEnabled() )
         log.trace("Instantiating IConnectionMethod by class: " + klass.getName() );
 
       try {
         IConnectionMethod method = klass.newInstance();
         return method;
       } catch (InstantiationException e) {
         log.error("An error occurred while instantiating IConnectionMethod class", e);
       } catch (IllegalAccessException e) {
         log.error("An error occurred while accessing to IConnectionMethod class", e);
       }
       return null;
     }
 
 
     /**
      * Creates the correct url starting from parameters
      *
      * @param entityPath the partial url to call. Typically this is the {@link IEntity#getApiPath()} 
      * @param action the {@code namespace} of the {@link IEntity} your are invoking. Typically this is the {@link IEntity#getNamespace()}
      * @param httpVerb one of {@link IConnector.IConnectionMethod#METHOD_GET GET} {@link IConnector.IConnectionMethod#METHOD_POST POST} {@link IConnector.IConnectionMethod#METHOD_PUT PUT} {@link IConnector.IConnectionMethod#METHOD_DELETE DELETE}
      * @param format the {@link ContentFormat} for this request
      * @param params the query string parameters used for this request. <b>Used in case of {@link IConnector.IConnectionMethod#METHOD_GET GET} {@link IConnector.IConnectionMethod#METHOD_DELETE DELETE} only</b>
      * @return the URL string
      */
     protected String buildRequestUrl(String entityPath, String action, String httpVerb, ContentFormat format, List<NameValuePair> params) {
 
       if ( params == null ){
         params = new ArrayList<NameValuePair>();
       }
       if ( httpVerb == null ) {
         httpVerb = IConnectionMethod.METHOD_GET;
       }
 
       action = ( ( action == null ) ? "" : IConnector.URI_SEPARATOR.concat(action) ).trim();
 
       String url = API_PATH + configuration.getPath( SERVER ) + entityPath + action;
 
       // add extension to request path
       if ( format != null ){
         url += IConnector.DOT + format.toString().toLowerCase();
       }
 
       if ( httpVerb.equals( IConnectionMethod.METHOD_GET ) || httpVerb.equals( IConnectionMethod.METHOD_DELETE ) || httpVerb.equals( IConnectionMethod.METHOD_HEAD )  ){
         String query = URLEncodedUtils.format( params , HTTP.UTF_8 );
         if ( query.length() > 0 )
           url += "?" + query;
       }
 
       return url;
     }
 
   }
 
 }
