 package nz.co.searchwellington.feeds;
 
 import java.util.List;
 
 import nz.co.searchwellington.model.Feed;
 import nz.co.searchwellington.model.User;
 import nz.co.searchwellington.repositories.HibernateBackedUserDAO;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class FeedReaderRunner {
 
 	private static Logger log = Logger.getLogger(FeedReaderRunner.class);
 
 	private static final String FEED_READER_PROFILE_NAME = "feedreader";
 	
 	private FeedReader feedReader;
 	private HibernateBackedUserDAO userDAO;
 	
 	@Autowired
	public FeedReaderRunner(FeedReader feedReader, HibernateBackedUserDAO userDAO) {		
 		this.feedReader = feedReader;
 		this.userDAO = userDAO;
 	}
 	
 	public void readAllFeeds(List<Feed> feeds) {
		log.info("Running Reader.");
 		for (Feed feed: feeds) {      
 			this.feedReader.processFeed(feed.getId(), getFeedReaderUser());
 		}
		log.info("Finished reading feeds.");		
 	}
 
 	private User getFeedReaderUser() {
 		User feedReaderUser = userDAO.getUserByProfileName(FEED_READER_PROFILE_NAME);
 		if (feedReaderUser == null) {
 			log.warn("Feed reader could not run as no user was found with profile name: " + FEED_READER_PROFILE_NAME);
 			return null;
 		}
 		return feedReaderUser;
 	}
 	
 }
