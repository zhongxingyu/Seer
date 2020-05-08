 package nz.co.searchwellington.feeds;
 
 import java.util.List;
 
 import nz.co.searchwellington.model.Feed;
 import nz.co.searchwellington.model.User;
 import nz.co.searchwellington.repositories.HibernateBackedUserDAO;
import nz.co.searchwellington.repositories.HibernateResourceDAO;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
 
 @Component
 public class FeedReaderRunner {
 
 	private static Logger log = Logger.getLogger(FeedReaderRunner.class);
 
 	private static final String FEED_READER_PROFILE_NAME = "feedreader";
 	
 	private FeedReader feedReader;
 	private HibernateBackedUserDAO userDAO;
	private HibernateResourceDAO resourceDAO;
	
	public FeedReaderRunner() {
	}
 	
 	@Autowired
	public FeedReaderRunner(FeedReader feedReader, HibernateBackedUserDAO userDAO, HibernateResourceDAO resourceDAO) {		
 		this.feedReader = feedReader;
 		this.userDAO = userDAO;
		this.resourceDAO = resourceDAO;
 	}
 	
	@Transactional
	@Scheduled(fixedRate=1200000)
	public void readFeeds() {
		log.info("Running feed reader.");
		readAllFeeds(resourceDAO.getAllFeeds());
		log.info("Finished reading feeds.");
	}
		
 	public void readAllFeeds(List<Feed> feeds) {
 		for (Feed feed: feeds) {      
 			this.feedReader.processFeed(feed.getId(), getFeedReaderUser());
 		}
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
