 package auth;
 
 import com.google.common.base.Throwables;
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.auth.AccessToken;
 import twitter4j.auth.RequestToken;
 import twitter4j.internal.http.HttpResponseCode;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Map;
 
 import static com.google.common.collect.Maps.newHashMap;
 
 public class TwitterAuthenticator implements Authenticator {
   private final TwitterFactory twitterFactory;
  private final String oAuthCallback;
  private Map<String, RequestToken> oauthRequestByToken;
 
  @Inject
   public TwitterAuthenticator(TwitterFactory twitterFactory, @Named("oAuth.callback") String oAuthCallback) {
    this.twitterFactory = twitterFactory;
     this.oAuthCallback = oAuthCallback;
     this.oauthRequestByToken = newHashMap();
   }
 
   @Override
   public URL getAuthenticateURL() throws AuthenticationException {
     Twitter twitter = twitterFactory.getInstance();
     try {
       RequestToken oauthRequestToken = twitter.getOAuthRequestToken(oAuthCallback);
       oauthRequestByToken.put(oauthRequestToken.getToken(), oauthRequestToken);
       return new URL(oauthRequestToken.getAuthenticationURL());
     } catch (MalformedURLException e) {
       throw new AuthenticationException(e);
     } catch (TwitterException e) {
       throw new AuthenticationException(e);
     }
   }
 
   @Override
   public User authenticate(String oauthToken, String oauthVerifier) throws AuthenticationException {
     Twitter twitter = twitterFactory.getInstance();
     try {
       AccessToken oAuthAccessToken = twitter.getOAuthAccessToken(oauthRequestByToken.remove(oauthToken), oauthVerifier);
       return new User(oAuthAccessToken.getUserId(), oAuthAccessToken.getScreenName(), oAuthAccessToken.getToken(),
               oAuthAccessToken.getTokenSecret());
     } catch (TwitterException e) {
       if (HttpResponseCode.UNAUTHORIZED == e.getStatusCode()) {
         throw new AuthenticationException(e);
       }
       throw Throwables.propagate(e);
     }
   }
 }
