 package nz.co.searchwellington.feeds;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Set;
 
 import nz.co.searchwellington.dates.DateFormatter;
 import nz.co.searchwellington.mail.Notifier;
 import nz.co.searchwellington.model.Feed;
 import nz.co.searchwellington.model.LinkCheckerQueue;
 import nz.co.searchwellington.model.Resource;
 import nz.co.searchwellington.model.Tag;
 import nz.co.searchwellington.repositories.FeedRepository;
 import nz.co.searchwellington.repositories.ResourceRepository;
 import nz.co.searchwellington.tagging.PlaceAutoTagger;
 import nz.co.searchwellington.utils.UrlCleaner;
 import nz.co.searchwellington.utils.UrlFilters;
 
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.sun.syndication.io.FeedException;
 
 
 public class FeedReader {
     
    private static final int NUMEBR_OF_FEEDS_TO_READ = 100;
 
 	Logger log = Logger.getLogger(FeedReader.class);
     
     private ResourceRepository resourceDAO;
     private FeedRepository feedDAO;
     private LinkCheckerQueue linkCheckerQueue;
     private PlaceAutoTagger placeAutoTagger;   
     private Notifier notifier;
     private String notificationReciept;
     private FeedAcceptanceDecider feedAcceptanceDecider;
     private DateFormatter dateFormatter;   
     private UrlCleaner urlCleaner;
  
     
     public FeedReader() {        
     }
     
     
     
     public FeedReader(ResourceRepository resourceDAO, FeedRepository feedDAO, LinkCheckerQueue linkCheckerQueue, PlaceAutoTagger placeAutoTagger, Notifier notifier, String notificationReciept, FeedAcceptanceDecider feedAcceptanceDecider, DateFormatter dateFormatter, UrlCleaner urlCleaner) {
         this.resourceDAO = resourceDAO;
         this.feedDAO = feedDAO;
         this.linkCheckerQueue = linkCheckerQueue;
         this.placeAutoTagger = placeAutoTagger;        
         this.notifier = notifier;
         this.notificationReciept = notificationReciept;
         this.feedAcceptanceDecider = feedAcceptanceDecider;      
         this.dateFormatter = dateFormatter;
         this.urlCleaner = urlCleaner;
     }
 
 
     @Transactional
     public void acceptFeeditems() throws FeedException, IOException {              
         log.info("Accepting feeds.");        
         int processed = 0;        
         for (Feed feed: resourceDAO.getFeedsToRead()) {      
             if (processed < NUMEBR_OF_FEEDS_TO_READ) {
                 processFeed(feed);
                 processed ++;
             }
         }
         log.info("Finished reading feeds.");
     }
 
  
     public void processFeed(Feed feed) throws FeedException, IOException {        
         log.info("Processing feed: " + feed.getName() + ". Last read: " + dateFormatter.formatDate(feed.getLastRead(), DateFormatter.TIME_DAY_MONTH_YEAR_FORMAT));
        
         // TODO can this move the the enum?
         boolean canAcceptFromFeed =  feed.getAcceptancePolicy() != null && feed.getAcceptancePolicy().equals("accept") || feed.getAcceptancePolicy().equals("accept_without_dates");
         if (canAcceptFromFeed) {
             List<Resource> feedNewsitems = feedDAO.getFeedNewsitems(feed);
         
             for (Resource resource : feedNewsitems) {
                 resource.setUrl(urlCleaner.cleanSubmittedItemUrl(resource.getUrl()));
                 boolean acceptThisItem = feedAcceptanceDecider.getAcceptanceErrors(resource, feed.getAcceptancePolicy()).size() == 0;
                 if (acceptThisItem) {
                     acceptFeedItem(resource, feed.getTags());
                 }
             }
     
             // TODO what's this all about.
             // Everytime we look at a feed, we should update the latest publication field.
             feed.setLatestItemDate(feedDAO.getLatestPublicationDate(feed));
             
             log.info("Feed latest item publication date is: " + feed.getLatestItemDate());
         } else {
             log.debug("Ignoring feed " + feed.getName() + "; acceptance policy is not set to accept");
         }
         
         feed.setLastRead(Calendar.getInstance().getTime());        
         log.info("Done processing feed.");      
     }
 
 
 
     private void acceptFeedItem(Resource resource, Set<Tag> feedTags) {
         log.info("Accepting: " + resource.getName());                        
         
         flattenLoudCapsInTitle(resource);
         
         if (resource.getDate() == null) {
         	log.info("Accepting a feeditem with no date; setting date to current time");            
             resource.setDate(new DateTime().toDate());
         }
       
         tagAcceptedFeedItem(resource, feedTags);        
         resourceDAO.saveResource(resource);
         linkCheckerQueue.add(resource.getId());
         notifier.sendAcceptanceNotification(notificationReciept, "Accepted newsitem from feed", resource);
     }
 
 
 
 	private void flattenLoudCapsInTitle(Resource resource) {
 		String flattenedTitle = UrlFilters.lowerCappedSentence(resource.getName());           
         if (!flattenedTitle.equals(resource.getName())) {
         	resource.setName(flattenedTitle);
             log.info("Flatten capitalised sentence to '" + flattenedTitle + "'");
         }
 	}
 
 
 
     private void tagAcceptedFeedItem(Resource resource, Set<Tag> feedTags) {
         placeAutoTagger.tag(resource);
         for (Tag tag : feedTags) {
             resource.addTag(tag);
         }
     }
     
     
 }
