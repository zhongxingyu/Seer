 package toctep.skynet.backend.bll;
 
 import toctep.skynet.backend.log.Log;
 import twitter4j.Status;
 import twitter4j.StatusDeletionNotice;
 import twitter4j.StatusListener;
 import twitter4j.TwitterStream;
 import twitter4j.TwitterStreamFactory;
 
 public abstract class TweetRetriever implements Runnable {
 
 	private TwitterStream twitterStream;
 	
 	public TweetRetriever() {
 		initialize();
 	}
 	
 	private void initialize() {
 		//System.setProperty ("twitter4j.loggerFactory", "twitter4j.internal.logging.NullLoggerFactory");
 		
 		StatusListener statusListener = new StatusListener() {
 			public void onStatus(Status status) {
 				process(status);
 	        }
 			
 	        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) { }
 	        public void onTrackLimitationNotice(int numberOfLimitedStatuses) { }
 	        public void onScrubGeo(long userId, long upToStatusId) { }
 	
 	        public void onException(Exception e) {
 	            Log.error(e.getMessage(), e);
 	        }
 	    };
 	    
 	    twitterStream = new TwitterStreamFactory().getInstance();
 	    twitterStream.addListener(statusListener);
 	}
 	
 	@Override
 	public abstract void run();
 	public abstract void process(Status status);
 	
 	protected TwitterStream getTwitterStream() {
 		return twitterStream;
 	}
 	
 }
