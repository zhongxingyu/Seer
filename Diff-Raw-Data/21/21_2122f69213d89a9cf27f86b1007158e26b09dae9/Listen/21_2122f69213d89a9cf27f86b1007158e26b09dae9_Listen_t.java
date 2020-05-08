 package Access;
 
 import java.io.IOException;
 
 import twitter4j.StallWarning;
 import twitter4j.Status;
 import twitter4j.StatusDeletionNotice;
 import twitter4j.StatusListener;
 import twitter4j.TwitterException;
 import twitter4j.TwitterStream;
 import twitter4j.TwitterStreamFactory;
 import twitter4j.conf.ConfigurationBuilder;
 
 public class Listen {
 	public void listen(String CONSUMER_KEY,String CONSUMER_KEY_SECRET,String token,String tokenSecret) throws TwitterException, IOException{
 		System.out.println("sample");
		/*ConfigurationBuilder cb = new ConfigurationBuilder();
 	    cb.setDebugEnabled(true);
 	    cb.setOAuthConsumerKey(CONSUMER_KEY);
 	    cb.setOAuthConsumerSecret(CONSUMER_KEY_SECRET);
 	    cb.setOAuthAccessToken(token);
	    cb.setOAuthAccessTokenSecret(tokenSecret);    
	    TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();*/
 	    StatusListener listener = new StatusListener(){
 	        public void onStatus(Status status) {
 	            System.out.println(status.getUser().getName() + " : " + status.getText());
 	        }
 	        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
 	        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
 	        public void onException(Exception ex) {
 	            ex.printStackTrace();
 	        }
 			@Override
 			public void onScrubGeo(long arg0, long arg1) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onStallWarning(StallWarning arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 	    };
	    TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
 	    twitterStream.addListener(listener);
	    
 	    twitterStream.sample();
 	}
 }
