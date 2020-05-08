 package net.nitram509.twitter;
 
 import net.nitram509.config.EnvironmentConfig;
 import twitter4j.StatusUpdate;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.auth.AccessToken;
 import twitter4j.auth.RequestToken;
 
 public class TwitterService {
 
   private TwitterInMemoryStorage storage = new TwitterInMemoryStorage();
   private EnvironmentConfig config = new EnvironmentConfig();
  private TwitterTextHelper twitterTextHelper = new TwitterTextHelper("#epforum");
 
   public TwitterService() {
     if (config.consumerKey() == null) {
       throw new IllegalStateException("You have to provide 'consumerKey' as ENV VAR!");
     }
     if (config.consumerSecret() == null) {
       throw new IllegalStateException("You have to provide 'consumerSecret' as ENV VAR!");
     }
   }
 
   public String signinAndGetAuthenticationUrl(String requestUrl) throws TwitterException {
     Twitter twitter = getTwitter();
 
     StringBuilder callbackUrl = new StringBuilder(requestUrl);
     int index = callbackUrl.lastIndexOf("/");
     callbackUrl.replace(index, callbackUrl.length(), "").append("/callback");
 
     RequestToken requestToken = twitter.getOAuthRequestToken(callbackUrl.toString());
     storage.setRequestToken(requestToken);
     return requestToken.getAuthenticationURL();
   }
 
   public boolean isSignedIn() {
     return storage.getAccessToken() != null;
   }
 
   public void doCallback(String oauth_verifier) throws TwitterException {
     Twitter twitter = getTwitter();
     RequestToken requestToken = storage.getRequestToken();
     AccessToken oAuthAccessToken = twitter.getOAuthAccessToken(requestToken, oauth_verifier);
     storage.setAccessToken(oAuthAccessToken);
     storage.setRequestToken(null);
   }
 
   public void postMessage(String message) throws TwitterException {
     Twitter twitter = getTwitter();
     if (storage.getAccessToken() != null) {
       twitter.setOAuthAccessToken(storage.getAccessToken());
     }
     message = twitterTextHelper.appendDefaultHashtag(message);
     twitter.updateStatus(new StatusUpdate(createMessage(formatMessage(message))));
   }
 
   private String formatMessage(String message) {
     message = message.substring(0, Math.min(message.length(), 140));
     return message;
   }
 
   private String createMessage(String message) {
     return message;
   }
 
   private Twitter getTwitter() {
     Twitter twitter = TwitterFactory.getSingleton();
     if (!twitter.getAuthorization().isEnabled()) {
       twitter.setOAuthConsumer(config.consumerKey(), config.consumerSecret());
       configureOptionalAccessToken();
     }
     return twitter;
   }
 
   private void configureOptionalAccessToken() {
     if (config.accessToken() != null && config.accessTokenSecret() != null) {
       AccessToken knownAccessToken = new AccessToken(config.accessToken(), config.accessTokenSecret());
       storage.setAccessToken(knownAccessToken);
     }
   }
 }
