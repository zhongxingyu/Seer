 package com.pk.cwierkacz.twitter;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.auth.AccessToken;
 import twitter4j.auth.RequestToken;
 
 import com.pk.cwierkacz.model.dao.TwitterAccountDao;
 import com.pk.cwierkacz.twitter.converters.TwitterAcountConverter;
 
 public class OAuthAuthentication extends TwitterResolver
 {
     private static final Logger LOGGER = LoggerFactory.getLogger(OAuthAuthentication.class);
     private final TwitterAccountDao account;
     private Twitter twitter;
     private RequestToken requestToken;
 
     /**
      * initialize user authentication service
      * 
      * @param user
      *            user which we wont authenticate
      * @throws TwitterAuthenticationException
      */
     public OAuthAuthentication( TwitterAccountDao account ) throws TwitterAuthenticationException {
         this.account = account;
         this.twitter = createTwitter(account, true);
     }
 
     /**
      * get information about if user is authenticate - in other words we check
      * if user have saved access token and secret
      */
     public boolean isAuthenticate( ) {
         return account.getAccessToken() != null && account.getAccessTokenSecret() != null;
     }
 
     /**
      * This method return URL where user may get PIN to app
      * 
      * @throws TwitterAuthenticationException
      */
     public String getAuthenticationURL( ) throws TwitterAuthenticationException {
         try {
             requestToken = twitter.getOAuthRequestToken();
             return requestToken.getAuthenticationURL();
         }
         catch ( TwitterException e ) {
            e.printStackTrace();
             LOGGER.error(e.getMessage());
             throw new TwitterAuthenticationException("Request token generation failure");
         }
     }
 
     /**
      * Authenticate current user.
      * 
      * @param pin
      *            PIN which user must put. Method getAuthenticationURL() allow
      *            user get this PIN
      * @param forceAuthorizations
      *            If false and if user is authenticate before do method do
      *            nothing.
      * @return user with access token and access token secret and with refreshed
      *         account name, name and external id
      * @throws TwitterAuthenticationException
      */
     public TwitterAccountDao authenticate( String pin, boolean forceAuthorizations ) throws TwitterAuthenticationException {
         if ( isAuthenticate() && !forceAuthorizations )
             return account;
 
         try {
             AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, pin);
             twitter = createTwitter(accessToken);
             twitter4j.User tweetUser = twitter.showUser(account.getAccountName());
             TwitterAcountConverter converter = new TwitterAcountConverter();
             TwitterAccountDao accountFromTweet = converter.toAccountFromTwitter(tweetUser);
             account.setAccountName(accountFromTweet.getAccountName());
             account.setName(accountFromTweet.getName());
             account.setExternalId(accountFromTweet.getExternalId());
             account.setAccessToken(accessToken.getToken());
             account.setAccessTokenSecret(accessToken.getTokenSecret());
 
             return account;
         }
         catch ( TwitterException e ) {
             LOGGER.error(e.getMessage());
             throw new TwitterAuthenticationException("Access token generation failure");
         }
         catch ( IllegalStateException e ) {
             LOGGER.error(e.getMessage());
             throw new TwitterAuthenticationException("Access token generation failure - no token available");
         }
 
     }
 
     /**
      * Authenticate current user. If user is authenticate before do nothing.
      * 
      * @param pin
      *            PIN which user must put. Method getAuthenticationURL() allow
      *            user get this PIN
      * @return user with access token and access token secret
      * @throws TwitterAuthenticationException
      */
     public TwitterAccountDao authenticate( String pin ) throws TwitterAuthenticationException {
         return authenticate(pin, false);
     }
 }
