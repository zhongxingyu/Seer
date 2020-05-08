 package com.teamboid.twitterapi.client;
 
 import org.scribe.builder.ServiceBuilder;
 import org.scribe.builder.api.TwitterApi;
 import org.scribe.model.Token;
 import org.scribe.model.Verifier;
 import org.scribe.oauth.OAuthService;
 
 /**
  * Used to authenticate accounts and get logged in instances of {@link Twitter}
  *
  * @author Aidan Follestad
  */
 public class Authorizer {
 
     private Authorizer(String consumer, String secret, String callback) {
         service = new ServiceBuilder()
                 .provider(TwitterApi.class)
                 .apiKey(consumer)
                 .apiSecret(secret)
                 .callback(callback)
                 .build();
         _debugMode = DebugLevel.OFF;
     }
 
     public static enum DebugLevel {
         /**
          * Debug mode is off. Default.
          */
         OFF,
         /**
          * Requested URLs will be printed to the console.
          */
         LIGHT,
         /**
          * Requested URLs and received raw JSON will be printed to the console.
          */
         DEEP
     }
 
     private OAuthService service;
     private Token requestToken;
     private DebugLevel _debugMode;
     private boolean _ssl;
 
     /**
      * Intializes a new Authorizer for generating authenticated {@link Twitter} instances.
      * @param consumer The OAuth consumer of your application that's registered on dev.twitter.com.
      * @param secret The OAuth secret of your application that's registered on dev.twitter.com.
      * @param callback The callback URL called after the user authorizes their account on the web page returned from getUrl().
      * @return A new Authorizer instance
      */
     public static Authorizer create(String consumer, String secret, String callback) {
         return new Authorizer(consumer, secret, callback);
     }
 
     /**
      * This method is used to get an un-authorized {@link Twitter} instance; it will
      * not have permission to request protected resources (any functions that require you
      * to be logged in).
      */
     public Twitter getUnauthorizedInstance() {
         TwitterBase toReturn = new TwitterBase();
         toReturn._debugOn = _debugMode;
         toReturn._ssl = _ssl;
         return toReturn;
     }
 
     /**
      * This method is used to get an authorized {@link Twitter} instance if you had already gotten
      * authorization previously and stored the returned access token.
      * @param accessKey The access key previously stored.
      * @param accessSecret The access secret preeviously stored.
      * @return An authenticated Twitter instance.
      */
     public Twitter getAuthorizedInstance(String accessKey, String accessSecret) {
         TwitterBase toReturn = new TwitterBase();
         toReturn._debugOn = _debugMode;
         toReturn._ssl = _ssl;
         toReturn._oauthToken = new Token(accessKey, accessSecret);
         toReturn._oauth = service;
         return toReturn;
     }
 
     /**
      * The method called after your receive a callback from the web browser (after using getUrl()).
      * @param verifier The oauth_verifier paramter sent from the browser through the callback.
      */
     public Twitter getAuthorizedInstance(String verifier) throws Exception {
    	if(requestToken == null) {
    		throw new Exception("There's no request token in the current Authorizer state, you must call getAuthorizeUrl() first.");
    	}
         TwitterBase toReturn = new TwitterBase();
         toReturn._debugOn = _debugMode;
         toReturn._ssl = _ssl;
         toReturn._oauthToken = service.getAccessToken(requestToken, new Verifier(verifier));
         toReturn._oauth = service;
         return toReturn;
     }
 
     /**
      * The initial step of authentication, returns the URL of Twitter's authorization page that you must open
      * in the web browser. When they login and click 'Authorize', the callback you specified in the constructor
      * will be invoked. Make sure your app is set up to receive this callback and use the callback() method.
      * @return
      */
     public String getAuthorizeUrl() throws Exception {
         requestToken = service.getRequestToken();
         return service.getAuthorizationUrl(requestToken);
     }
 
     /**
      * Sets whether or not SSL will be used with requests to Twitter. Defaults as true.
      *
      * @deprecated Use {@link Twitter#setSslEnabled(boolean)} instead.
      */
     public Authorizer setUseSSL(boolean ssl) {
         _ssl = ssl;
         return this;
     }
 
     /**
      * Sets whether or not debug is on, and what level of output it's creates.
      */
     public Authorizer setDebugMode(DebugLevel debug) {
         _debugMode = debug;
         return this;
     }
 }
