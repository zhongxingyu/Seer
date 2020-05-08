 package nz.co.searchwellington.feeds;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 
import org.apache.log4j.Logger;

 import nz.co.searchwellington.model.DiscoveredFeed;
 import nz.co.searchwellington.model.Feed;
 import nz.co.searchwellington.model.FeedNewsitem;
 import nz.co.searchwellington.model.Newsitem;
 import nz.co.searchwellington.model.NewsitemImpl;
 import nz.co.searchwellington.model.Resource;
 import nz.co.searchwellington.model.Tag;
 import nz.co.searchwellington.model.Twit;
import nz.co.searchwellington.modification.ContentDeletionService;
 import nz.co.searchwellington.repositories.ResourceRepository;
 import nz.co.searchwellington.repositories.SupressionRepository;
 
 public abstract class RssfeedNewsitemService {
 
	static Logger log = Logger.getLogger(RssfeedNewsitemService.class);

 	public abstract List<FeedNewsitem> getFeedNewsitems(Feed feed);
 
 	protected ResourceRepository resourceDAO;
 	protected SupressionRepository suppressionDAO;
 	
 	
 	public final Date getLatestPublicationDate(Feed feed) {
 		Date latestPublicationDate = null;
 		List<FeedNewsitem> feeditems = getFeedNewsitems(feed);
 		for (Resource resource : feeditems) {
 			if (resource.getDate() != null && (latestPublicationDate == null || resource.getDate().after(latestPublicationDate))) {
 				latestPublicationDate = resource.getDate();           
 			}
 		}
 		return latestPublicationDate;
 	}
 	
 	// TODO merge with addSuppressAndLocalCopyInformation?
 	public final Newsitem makeNewsitemFromFeedItem(FeedNewsitem feedNewsitem, Feed feed) {
 		// TODO why are we newing up an instance of our superclass?
 	    String description =  feedNewsitem.getDescription() != null ? feedNewsitem.getDescription() : ""; 
 		Newsitem newsitem = new NewsitemImpl(0, feedNewsitem.getName(), feedNewsitem.getUrl(), description, feedNewsitem.getDate(), feedNewsitem.getPublisher(),
 	    		new HashSet<DiscoveredFeed>(), null, new HashSet<Twit>());
 	    newsitem.setImage(feedNewsitem.getImage());		
 	    newsitem.setPublisher(feed.getPublisher());
         newsitem.setFeed(feed);
 	    return newsitem;
 	}
 	
 	
 	// TODO should return a FeedNewsitem
 	public Newsitem getFeedNewsitemByUrl(String url) {
 		for(Feed feed : resourceDAO.getAllFeeds()) {
 			List <FeedNewsitem> feednewsItems = this.getFeedNewsitems(feed);
 			for (FeedNewsitem feedNewsitem : feednewsItems) {                	
 				if (feedNewsitem.getUrl().equals(url)) {					
 					return this.makeNewsitemFromFeedItem(feedNewsitem, feed);					
 				}
 			}
 		}
 		return null;
 	}
 	
 	
 	public FeedNewsitem getFeedNewsitemByUrl(Feed feed, String url) {
 		List<FeedNewsitem> feedNewsitems = this.getFeedNewsitems(feed);
 		Iterator<FeedNewsitem> i = feedNewsitems.iterator();
 		while (i.hasNext()) {
 			FeedNewsitem feedNewsitem = i.next();
 			if (feedNewsitem.getUrl().equals(url)) {
 				return feedNewsitem;
 			}
 		}
 		return null;
 	}
 	
 	
 	public void addSupressionAndLocalCopyInformation(List<FeedNewsitem> feedNewsitems) {
 		for (FeedNewsitem feedNewsitem : feedNewsitems) {
 			if (feedNewsitem.getUrl() != null) {
 				Resource localCopy = resourceDAO.loadResourceByUrl(feedNewsitem.getUrl());
 				if (localCopy != null) {
 					feedNewsitem.setLocalCopy(localCopy);
 				}				
 				boolean isSuppressed = suppressionDAO.isSupressed(feedNewsitem.getUrl());					
 				feedNewsitem.setSuppressed(isSuppressed);						
 			}
 		}
 	}
 
 	
 	public boolean isUrlInAcceptedFeeds(String url) {
		log.info("Looking for url in accepted feeds: " + url);
 		for(Feed feed : resourceDAO.getAllFeeds()) {
			if (feed.getAcceptancePolicy().equals("accept") || feed.getAcceptancePolicy().equals("accept_without_dates")) {
				log.debug("Checking feed: " + feed.getName());
 				List <FeedNewsitem> feednewsItems = this.getFeedNewsitems(feed);
				for (FeedNewsitem feedNewsitem : feednewsItems) {
					log.debug("Checking feeditem: " + feedNewsitem.getUrl());
					if (feedNewsitem.getUrl().equals(url)) {
						log.info("Found: " + url);
 						return true;
 					}
 				}
 			}
 		}
 		return false;		
 	}
 	
 }
