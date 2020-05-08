 package nz.co.searchwellington.signin.twitter;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import nz.co.searchwellington.model.User;
 import nz.co.searchwellington.repositories.UserRepository;
 import nz.co.searchwellington.signin.SigninHandler;
 import nz.co.searchwellington.twitter.TwitterApiFactory;
 
 import org.apache.log4j.Logger;
 import org.scribe.oauth.Scribe;
 import org.scribe.oauth.Token;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 
 public class TwitterLoginHandler implements SigninHandler {
 
 	static Logger log = Logger.getLogger(TwitterLoginHandler.class);
 	
 	private static final String OAUTH_AUTHEN_URL = "http://api.twitter.com/oauth/authenticate?oauth_token=";
 	
 	private OAuthScribeFactory scribeFactory;
 	private UserRepository userDAO;
 	private TwitterApiFactory twitterApiFactory;
 	private Map<String, Token> tokens;
 	
 	public TwitterLoginHandler(OAuthScribeFactory scribeFactory, UserRepository userDAO, TwitterApiFactory twitterApiFactory) {
 		this.scribeFactory = scribeFactory;
 		this.userDAO = userDAO;
 		this.twitterApiFactory = twitterApiFactory;
 		this.tokens = new HashMap<String, Token>();
 	}
 	
 	@Override
 	public ModelAndView getLoginView(HttpServletRequest request, HttpServletResponse response) throws Exception {
 		try {
 			log.info("Getting request token");
 			Scribe scribe = scribeFactory.getScribe();
 			Token requestToken = scribe.getRequestToken();
 			if (requestToken != null) {
 				log.info("Got request token: " + requestToken.getToken());
 				tokens.put(requestToken.getToken(), requestToken);
 				
 				final String authorizeUrl = OAUTH_AUTHEN_URL + requestToken.getToken();				
 				RedirectView redirectView = new RedirectView(authorizeUrl);
 				log.info("Redirecting user to: " + redirectView.getUrl());
 				return new ModelAndView(redirectView);
 			}
 			
 		} catch (Exception e) {
 			log.warn("Failed to obtain request token" + e.getMessage());
 		}
 		return null;	
 	}
 	
 	
 	@Override
 	public Object getExternalUserIdentifierFromCallbackRequest(HttpServletRequest request) {
 		if (request.getParameter("oauth_token") != null && request.getParameter("oauth_verifier") != null) {
 			final String token = request.getParameter("oauth_token");
 			final String verifier = request.getParameter("oauth_verifier");
 
 			log.info("Looking for request token: " + token);
 			Token requestToken = tokens.get(token);
 			if (requestToken != null) {
 				log.debug("Found stored request token: " + requestToken.getToken());
 				
 				log.debug("Exchanging for access token");				
 				Scribe scribe = scribeFactory.getScribe();
 				Token accessToken = scribe.getAccessToken(requestToken, verifier);
 				
 				if (accessToken != null) {
					log.info("Got access token: '" + accessToken.getToken() + "', '" + accessToken.getSecret() + "'");
 					tokens.remove(requestToken.getToken());
 					log.debug("Using access token to lookup twitter user details");
 					twitter4j.User twitterUser = getTwitteUserCredentials(accessToken);
 					if (twitterUser != null) {
 						return twitterUser;
 					} else {
 						log.warn("Failed up obtain twitter user details");
 					}
 					
 				} else {
 					log.warn("Could not get access token for: " + requestToken.getToken());
 				}								
 			} else {
 				log.warn("Could not find request token for: " + token);
 			}		
 		
 		} else {
 			log.error("oauth token or verifier missing from callback request");
 		}
 		return null;
 	}
 	
 	
 	@Override
 	public User getUserByExternalIdentifier(Object externalIdentifier) {
 		twitter4j.User twitterUser = (twitter4j.User) externalIdentifier;
 		return userDAO.getUserByTwitterId(twitterUser.getId());
 	}
 
 		
 	@Override
 	public void decorateUserWithExternalSigninIndenfier(User user, Object externalIdentifier) {		
 		twitter4j.User twitterUser = (twitter4j.User) externalIdentifier;
 		if (user.getProfilename() == null || user.isUnlinkedAnonAccount()) {
 			final String twitterScreenName = twitterUser.getScreenName();
 			if (userDAO.getUserByProfileName(twitterScreenName) == null) {
 				user.setProfilename(twitterScreenName);
 			}
 		}
 		user.setTwitterId(twitterUser.getId());
 	}
 	
 	
 	private twitter4j.User getTwitteUserCredentials(Token accessToken) {
 		Twitter twitterApi = twitterApiFactory.getOauthedTwitterApiForAccessToken(accessToken.getToken(), accessToken.getSecret());
 		try {
 			return twitterApi.verifyCredentials();
 		} catch (TwitterException e) {
 			log.warn("Failed up obtain twitter user details due to Twitter exception: " + e.getMessage());
 			return null;
 		}
 	}
 	
 }
