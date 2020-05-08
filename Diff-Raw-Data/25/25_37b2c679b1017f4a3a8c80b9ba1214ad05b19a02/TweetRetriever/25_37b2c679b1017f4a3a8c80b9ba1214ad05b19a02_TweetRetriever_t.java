 package toctep.skynet.backend.bll;
 
 import toctep.skynet.backend.Skynet;
 import twitter4j.Status;
 import twitter4j.StatusDeletionNotice;
 import twitter4j.StatusListener;
 import twitter4j.TwitterStream;
 import twitter4j.TwitterStreamFactory;
 
 public abstract class TweetRetriever implements Runnable {
 
 	private TweetParser tweetParser;
	private TwitterStream twitterStream;
 	
 	public TweetRetriever() {
 		initialize();
 	}
 	
 	protected void initialize() {
 		//System.setProperty ("twitter4j.loggerFactory", "twitter4j.internal.logging.NullLoggerFactory");
 				
 		tweetParser = TweetParser.getInstance();
 		
 		StatusListener statusListener = new StatusListener() {
 			public void onStatus(Status status) {
 				if (isDutch(status)) {
 					tweetParser.parse(status).save();
 				}
 	        }
 			
 	        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) { }
 	        public void onTrackLimitationNotice(int numberOfLimitedStatuses) { }
 	        public void onScrubGeo(long userId, long upToStatusId) { }
 	
 	        public void onException(Exception e) {
 	            Skynet.LOG.error(e.getMessage(), e);
 	        }
 	    };
 	    
 	    twitterStream = new TwitterStreamFactory().getInstance();
 	    twitterStream.addListener(statusListener);
 	}
 
 	@Override
 	public abstract void run();
 	
 	public abstract boolean isDutch(Status status);
 	
	protected TwitterStream getTwitterStream() {
		return twitterStream;
	}
	
 }
