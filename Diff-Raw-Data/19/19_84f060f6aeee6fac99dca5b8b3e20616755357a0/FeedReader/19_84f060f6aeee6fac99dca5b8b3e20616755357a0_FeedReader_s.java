 package nz.co.searchwellington.feeds;
 
 import java.util.Calendar;
 import java.util.List;
 import java.util.Set;
 
 import nz.co.searchwellington.dates.DateFormatter;
 import nz.co.searchwellington.model.Feed;
 import nz.co.searchwellington.model.FeedNewsitem;
 import nz.co.searchwellington.model.Newsitem;
 import nz.co.searchwellington.model.Resource;
 import nz.co.searchwellington.model.Tag;
 import nz.co.searchwellington.modification.ContentUpdateService;
 import nz.co.searchwellington.repositories.ResourceRepository;
 import nz.co.searchwellington.repositories.SuggestionDAO;
 import nz.co.searchwellington.tagging.AutoTaggingService;
 import nz.co.searchwellington.utils.UrlCleaner;
 import nz.co.searchwellington.utils.UrlFilters;
 
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 
 public class FeedReader {
       
 	Logger log = Logger.getLogger(FeedReader.class);
     
     private ResourceRepository resourceDAO;
     private RssfeedNewsitemService rssfeedNewsitemService;
     private AutoTaggingService autoTagger;   
     private FeedAcceptanceDecider feedAcceptanceDecider;
     private DateFormatter dateFormatter;   
     private UrlCleaner urlCleaner;
     private SuggestionDAO suggestionDAO;
     private ContentUpdateService contentUpdateService;
  
     
     public FeedReader() {        
     }
     
         
     public FeedReader(ResourceRepository resourceDAO, RssfeedNewsitemService rssfeedNewsitemService, AutoTaggingService autoTagger, FeedAcceptanceDecider feedAcceptanceDecider, DateFormatter dateFormatter, UrlCleaner urlCleaner, SuggestionDAO suggestionDAO, ContentUpdateService contentUpdateService) {
         this.resourceDAO = resourceDAO;
         this.rssfeedNewsitemService = rssfeedNewsitemService;
         this.autoTagger = autoTagger;
         this.feedAcceptanceDecider = feedAcceptanceDecider;      
         this.dateFormatter = dateFormatter;
         this.urlCleaner = urlCleaner;
         this.suggestionDAO = suggestionDAO;
         this.contentUpdateService = contentUpdateService;
     }
 
     
    @Transactional(propagation = Propagation.REQUIRES_NEW)
     public void processFeed(int feedId) {
     	Feed feed = (Feed) resourceDAO.loadResourceById(feedId);
     	
     	log.info("Processing feed: " + feed.getName() + ". Last read: " + dateFormatter.formatDate(feed.getLastRead(), DateFormatter.TIME_DAY_MONTH_YEAR_FORMAT));               
 
     	// TODO can this move onto the enum?
         boolean shouldLookAtFeed =  feed.getAcceptancePolicy() != null && feed.getAcceptancePolicy().equals("accept") 
         	|| feed.getAcceptancePolicy().equals("accept_without_dates")
         	|| feed.getAcceptancePolicy().equals("suggest");
 
         if (shouldLookAtFeed) {
             List<FeedNewsitem> feedNewsitems = rssfeedNewsitemService.getFeedNewsitems(feed);
             
             if (!feedNewsitems.isEmpty()) {
             	processFeedItems(feed, feedNewsitems);
             	feed.setHttpStatus(200);
             } else {
             	log.warn("Incoming feed '" + feed.getName() + "' contained no items");
             	feed.setHttpStatus(-3);
             }
             
             feed.setLatestItemDate(rssfeedNewsitemService.getLatestPublicationDate(feed));
             log.info("Feed latest item publication date is: " + feed.getLatestItemDate());
             
         } else {
             log.debug("Ignoring feed " + feed.getName() + "; acceptance policy is not set to accept");
         }
         
         feed.setLastRead(Calendar.getInstance().getTime());        
         contentUpdateService.update(feed, false);
 		log.info("Done processing feed.");
         return;
     }
 
 
 	private void processFeedItems(Feed feed, List<FeedNewsitem> feedNewsitems) {
 		for (FeedNewsitem feednewsitem : feedNewsitems) {
 			String cleanSubmittedItemUrl = urlCleaner.cleanSubmittedItemUrl(feednewsitem.getUrl());
 			feednewsitem.setUrl(cleanSubmittedItemUrl);
 		    
 		    if (feed.getAcceptancePolicy().startsWith("accept")) {
 		    	boolean acceptThisItem = feedAcceptanceDecider.getAcceptanceErrors(feednewsitem, feed.getAcceptancePolicy()).size() == 0;
 		    	if (acceptThisItem) {                		
 		    		acceptFeedItem(feednewsitem, feed);
 		    	}
 		    	
 		    } else {                	
 		    	if (feedAcceptanceDecider.shouldSuggest(feednewsitem)) {
 		    		log.info("Suggesting: " + feed.getName() + ": " + feednewsitem.getName());
 		    		suggestionDAO.addSuggestion(suggestionDAO.createSuggestion(feed, feednewsitem.getUrl(), new DateTime().toDate()));
 		    	}
 		    }
 		}
 	}
 
 
     @Transactional(propagation = Propagation.REQUIRES_NEW)
     private void acceptFeedItem(FeedNewsitem feeditem, Feed feed) {
         log.info("Accepting: " + feeditem.getName());
         Newsitem resource = rssfeedNewsitemService.makeNewsitemFromFeedItem(feeditem, feed);     
         resource.setFeed(feed);
         
         log.info("Item body after makeNewsitemFromFeedItem: " + resource.getDescription());
         
         flattenLoudCapsInTitle(resource);
         
         if (resource.getDate() == null) {
         	log.info("Accepting a feeditem with no date; setting date to current time");            
             resource.setDate(new DateTime().toDate());
         }
         
         tagAcceptedFeedItem(resource, feed.getTags());
         log.info("Item body before save: " + resource.getDescription());
         contentUpdateService.update(resource, true);
     }
 
     
 	private void flattenLoudCapsInTitle(Resource resource) {
 		String flattenedTitle = UrlFilters.lowerCappedSentence(resource.getName());           
         if (!flattenedTitle.equals(resource.getName())) {
         	resource.setName(flattenedTitle);
             log.info("Flatten capitalised sentence to '" + flattenedTitle + "'");
         }
 	}
 
 
     private void tagAcceptedFeedItem(Resource resource, Set<Tag> feedTags) {       
         for (Tag tag : feedTags) {
             resource.addTag(tag);
         }
         autoTagger.autotag(resource);
     }
     
     
 }
