 package com.opensajux.integration;
 
 import java.io.Serializable;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 
 import twitter4j.Status;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.conf.ConfigurationBuilder;
 
 import com.opensajux.common.Chosen;
 import com.opensajux.dto.SiteDetails;
 
 @Singleton
 public class TwitterClient implements Serializable {
 	private static final long serialVersionUID = 7845735358098351478L;
 
 	private static Logger LOGGER = Logger.getLogger(TwitterClient.class.getName());
 	private Twitter twitter;
 
 	@Inject
 	public TwitterClient(@Chosen SiteDetails siteDetails) throws Exception {
 		ConfigurationBuilder cb = new ConfigurationBuilder();
 		cb.setDebugEnabled(true).setOAuthConsumerKey(siteDetails.getTwitterConsumerKey())
 				.setOAuthConsumerSecret(siteDetails.getTwitterConsumerSecret())
 				.setOAuthAccessToken(siteDetails.getTwitterAccessToken())
				.setOAuthAccessTokenSecret(siteDetails.getTwitterAccessTokenSecret());
 		TwitterFactory tf = new TwitterFactory(cb.build());
 		twitter = tf.getInstance();
 	}
 
 	public List<Status> getUserStatuses(String user) throws TwitterException {
 		return twitter.getUserTimeline(user);
 	}
 }
