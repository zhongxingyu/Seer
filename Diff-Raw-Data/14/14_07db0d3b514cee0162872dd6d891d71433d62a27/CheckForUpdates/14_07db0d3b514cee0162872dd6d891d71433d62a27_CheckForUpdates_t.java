 package retweeter;
 
 import java.io.IOException;
 import java.util.HashSet;
 
 import javax.jdo.Extent;
 import javax.jdo.PersistenceManager;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.datanucleus.util.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import winterwell.jtwitter.Twitter;
 import winterwell.jtwitter.TwitterException;
 import winterwell.jtwitter.Twitter.Status;
 import winterwell.jtwitter.TwitterException.E401;
 import winterwell.jtwitter.TwitterException.RateLimit;
 
 @SuppressWarnings("serial")
 public class CheckForUpdates extends HttpServlet {
 	final Logger logger = LoggerFactory.getLogger(getClass());
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		
 		// 1. Iterate over the users in the DB
 		// 2. Keep the screen name up-to-date for each user in case they changed it in twitter
 		// 3. Do searches for things to re-tweet.
 		
 		PersistenceManager pm = DB.getPersistenceManager();
 		try {
 			Extent<User> extent = pm.getExtent(User.class, false);
 			for(User u : extent) {
 				if(u.getAccountsToWatch().isEmpty()) {
 					logger.info("Account "+u.getScreenName()+" has no accounts to watch set.  Skipping.");
 				} else if(u.getHashTagsToWatch().isEmpty()) {
 					logger.info("Account "+u.getScreenName()+" has no hash tags to watch set.  Skipping.");
 				} else if(StringUtils.isEmpty(u.getUserOAuthSecret()) || StringUtils.isEmpty(u.getUserOAuthToken())) {
 					logger.info("Account "+u.getScreenName()+" has no valid OAuth token.  Skipping.");
 				}
 				try {
 					Twitter t = new Twitter(u.getScreenName(), new TwitterLogin(u));
 					
 					HashSet<Long> retweetedAlready = new HashSet<Long>();
 					for(String accountToWatch : u.getAccountsToWatch()) {
 						if(StringUtils.isEmpty(accountToWatch))
 							continue;
 						for(String hashTag : u.getHashTagsToWatch()) {
 							if(StringUtils.isEmpty(hashTag))
 								continue;
 							String query = "#"+hashTag+" from:"+accountToWatch;
 							Long lastStatusId = u.getLastStatusId(query);
 							t.setSinceId(lastStatusId);
 							t.setMaxResults(5); // Don't re-tweet more than 5 for a given combination
 							for(Status status : t.search(query, null, 5)) {
 								// If the user put multiple hashtags that we are watching, only retweet the status once
 								if(!retweetedAlready.add(status.getId())) {
 									// Re-tweet it!
									Status retweet = t.retweet(status);
									logger.info(u.getScreenName()+": RT @"+u.getScreenName()+" "+status+"  - http://twitter.com/"+u.getScreenName()+"/statuses/"+retweet.getId());
 								}
 								
 								// Don't update the saved last status unless the retweet is successful. 
 								if(lastStatusId == null || status.getId() > lastStatusId.longValue()) {
 									u.setLastStatusId(query, status.getId());
 								}
 							}
 						}
 					}
 				} catch(E401 authError) {
 					logger.warn("Authentication failed for "+u.getScreenName()+" - user with have to re-authenticate to active the service again.");
 					u.setUserOAuthSecret(null);
 					u.setUserOAuthToken(null);
 				} catch(RateLimit rateLimit) {
 					logger.error("Hit rate limit when looking for tweets for "+u.getScreenName()+" maybe they have too many users/hashtags to watch!");
 				} catch(TwitterException te) {
 					logger.error("Error doing re-tweets for "+u.getScreenName(), te);
 				}
 			}
 		} finally {
 			pm.close();
 		}
 	}
 }
