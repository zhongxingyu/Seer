 package twitter;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import jobs.OAuthSettings;
 import models.User;
 import models.UserToken;
 import play.Logger;
 import play.Play;
 import twitter4j.ResponseList;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.auth.AccessToken;
 import exception.NoAvailableTokenException;
 import exception.TSGException;
 import exception.UserProtectedException;
 
 public class TwitterProxyImpl implements TwitterProxy {
 	private static final SimpleDateFormat dt = new SimpleDateFormat(
 			"yyyy-mm-dd");
 	
 	Twitter twitter = null;
 
 	UserToken token; 
 	
 	public TwitterProxyImpl(User user) {
 		twitter = new TwitterFactory().getInstance();
 		twitter.setOAuthConsumer(OAuthSettings.getConsumerKey(),
 				OAuthSettings.getConsumerSecret());
 		twitter.setOAuthAccessToken(new AccessToken(user.authToken,
 				user.authTokenSecret));
 	}
 	public TwitterProxyImpl(String consKey, String consSecret, String authToken, String authTokenSecret) {
 		
 		twitter = new TwitterFactory().getInstance();
 		twitter.setOAuthConsumer(OAuthSettings.getConsumerKey(), OAuthSettings.getConsumerSecret());
 		twitter.setOAuthAccessToken(new AccessToken(authToken, authTokenSecret));
 	}
 	public TwitterProxyImpl(Long tokenOwnerId) throws TSGException {
 		
 		token = UserToken.getByOwnerId(tokenOwnerId);
 		
 		twitter = new TwitterFactory().getInstance();
 		twitter.setOAuthConsumer(OAuthSettings.getConsumerKey(), OAuthSettings.getConsumerSecret());
 		twitter.setOAuthAccessToken(new AccessToken(token.accessToken, token.accessTokenSecret));
 	}
 	public TwitterProxyImpl() throws TSGException {
 		
 		twitter = new TwitterFactory().getInstance();
 		twitter.setOAuthConsumer(Play.configuration.getProperty("twitstreetConsumerKey"), Play.configuration.getProperty("twitstreetConsumerSecret"));
 		findAvailableToken();
 	}
 
 	
 	
 
 	@Override
 	public List<Long> getFollowingIds(Long id) throws NoAvailableTokenException, UserProtectedException  {
 		ArrayList<Long> followingList = new ArrayList<Long>();
 		try {
 			for (Long friendId : twitter.getFriendsIDs(id, -1).getIDs()) {
 				followingList.add(friendId);
 			}
 		} catch (TwitterException e1) {
 			
 			boolean repeat = handleTwitterException(e1, id);
 			if(repeat){
 				getFollowingIds(id);
 			}
		} catch (Exception e1) {
			Logger.error(e1, e1.getMessage());

		}
 		return followingList;
 	}
 
 	@Override
 	public User getUser(Long userId) throws NoAvailableTokenException  {
 		User user = null;
 		try {
 			twitter4j.User twuser = twitter.showUser(userId);
 			user = new User(twuser);
 		} catch (TwitterException e1) {
 			
 			boolean repeat = false;
 			try {
 				repeat = handleTwitterException(e1);
 			} catch (UserProtectedException e) {
 				Logger.error(e, "This method shouldn't have given 'user protected' error!\n"+e.getMessage());
 			}
 			if(repeat){
 				user = getUser(userId);
 			}
 		} catch (Exception e1) {
 			Logger.error(e1, e1.getMessage());
 
 		}
 		return user;
 	}
 	
 
 	@Override
 	public User getUser(String screenName) throws NoAvailableTokenException  {
 		User user = null;
 		try {
 			twitter4j.User twuser = twitter.showUser(screenName);
 			user = new User(twuser);
 		} catch (TwitterException e1) {
 			
 			boolean repeat = false;
 			try {
 				repeat = handleTwitterException(e1);
 			} catch (UserProtectedException e) {
 				Logger.error(e, "This method shouldn't have given 'user protected' error!\n"+e.getMessage());
 			}
 			if(repeat){
 				user = getUser(screenName);
 			}
 		} catch (Exception e1) {
 			Logger.error(e1, e1.getMessage());
 
 		}
 		return user;
 	}
 
 	@Override
 	public List<User> getUsers(List<Long> idList) throws NoAvailableTokenException  {
 		
 		List<User> users = new ArrayList<User>();
 
 		if (idList.size() > 100) {
 			int size = idList.size();
 			users.addAll(getUsers(idList.subList(0, size / 2)));
 			users.addAll(getUsers(idList.subList((size / 2), size)));
 
 		} else {
 
 			try {
 				
 				long[] ids = new long[idList.size()];
 				int i = 0;
 				for(Long id : idList){
 					ids[i] = id;
 					i++;
 				}
 				 ResponseList<twitter4j.User> t4jUsers = twitter.lookupUsers(ids);
 				 if(t4jUsers!=null){
 
 					 for(twitter4j.User tu : t4jUsers){
 						 users.add(new User(tu));
 						 
 					 }
 				 }
 			}catch (TwitterException e1) {
 				
 				boolean repeat = false;
 				try {
 					repeat = handleTwitterException(e1);
 				} catch (UserProtectedException e) {
 					Logger.error(e, "This method shouldn't have given 'user protected' error!\n"+e.getMessage());
 				}
 				if(repeat){
 					users = getUsers(idList);
 				}
 			} catch (Exception e1) {
 				Logger.error(e1, e1.getMessage());
 
 			}
 		}
 
 		return users;
 	
 	}
 	private void findAvailableToken() throws NoAvailableTokenException{
 		token = UserToken.getAvailabeToken();
 		if (token != null) {
 			twitter.setOAuthAccessToken(new AccessToken(token.accessToken, token.accessTokenSecret));
 			//token.setInUse();
 		
 		} else {
 			throw new NoAvailableTokenException();
 
 		}
 	}
 	
 	private boolean handleTwitterException(TwitterException e1,  long ownerOfFollowingList) throws NoAvailableTokenException, UserProtectedException {
 		boolean repeat = false;		
 
 		if (e1.exceededRateLimitation()) {
 			token.setRateLimited(e1.getRateLimitStatus().getResetTimeInSeconds());
 			Logger.info("Rate limited: "+token.ownerScreenName);
 			
 			repeat = true;
 			findAvailableToken();
 		
 		} else if (e1.getMessage().contains("Invalid or expired token")) {
 			token.setInvalid();			
 			Logger.info("Invalid token: " + token.ownerScreenName);
 			Logger.info(e1.getMessage());
 			
 			repeat = true;			
 			findAvailableToken();
 		} else if (e1.getMessage().contains("Not authorized")) {
 			throw new UserProtectedException(ownerOfFollowingList);
 		} else if (e1.getMessage().contains("connect timed out")) {
          Logger.info("Connect timed out: " + token.ownerScreenName);
          repeat = true;    
          findAvailableToken();
       }else if (e1.getMessage().contains("Read-only application cannot POST")) {
          Logger.info("Read-only application cannot POST: " + token.ownerScreenName);
          repeat = true;    
          findAvailableToken();
       } else {
 			Logger.info("UNKNOWN ERROR: " + token.ownerScreenName);
 			Logger.error(e1.getMessage(), e1);
 
 		}
 		return repeat;
 	}
 	
 	private boolean handleTwitterException(TwitterException e1) throws NoAvailableTokenException, UserProtectedException {
 		return handleTwitterException(e1, -1);
 		
 	}
 
 }
