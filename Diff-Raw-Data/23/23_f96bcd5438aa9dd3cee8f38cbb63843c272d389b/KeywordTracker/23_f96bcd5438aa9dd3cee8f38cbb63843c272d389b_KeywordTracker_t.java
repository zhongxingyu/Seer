 package org.atlasapi.tracking.twitter;
 
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.util.StringUtils;
 
 import twitter4j.TwitterException;
 import twitter4j.TwitterStream;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 /**
  * Tracker that uses the Twitter streaming API to track and process all tweets containing given keywords.
  *  
  * @author Robert Chatley (robert@metabroadcast.com)
  */
 public class KeywordTracker implements Runnable {
 
 	private TwitterStream stream;
 	private List<String> baseKeywords;
 	private Log log = LogFactory.getLog(getClass());
 	private StatusUpdateListener statusUpdateListener;
 	private boolean running = false;
 	private Set<String> extraKeywords = Sets.newHashSet();
 	
 	public KeywordTracker(String filteredHashtag, StatusProcessor republisher, String twitterAccountName, String twitterPassword) {
 		StatusUpdateListener statusListener = new StatusUpdateListener(hashTagFor(filteredHashtag), republisher);
 		stream = new TwitterStream(twitterAccountName, twitterPassword, statusListener);
 	}
 	
 	private String hashTagFor(String filteredHashtag) {
 		if ("NONE".equals(filteredHashtag)) {
 			return null;
 		} else {
 			return "#" + filteredHashtag;
 		}
 	}
 	
 	/**
 	 * Keywords to track.
 	 */
 	public void setKeywords(List<String> keywords) {
 		this.baseKeywords = keywords;
 	}
 
 	public void run() {
 		if (!running) {
 			running = true;
 			log.info("Running twitter keyword tracker");
 			track();
 		}
 	}
 
 	private void track() {
 		try {
 			List<String> keywords = Lists.newArrayList(baseKeywords);
 			for (String keyword : extraKeywords) {
 				String[] parts = keyword.split(" ");
 				for (String part : parts) {
 					if (part.length() > 1 && !StringUtils.containsWhitespace(part)) {
 						keywords.add(part);
 					}
 				}
 			}
 			log.info("Tracking: " + keywords);
 			stream.track(keywords.toArray(new String[0]));
 		} catch (TwitterException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	public void shutdown() {
	    if (stream != null) {
	        stream.cleanup();
	    }
	    if (statusUpdateListener != null) {
	        statusUpdateListener.shutdown();
	    }
 	}
 }
