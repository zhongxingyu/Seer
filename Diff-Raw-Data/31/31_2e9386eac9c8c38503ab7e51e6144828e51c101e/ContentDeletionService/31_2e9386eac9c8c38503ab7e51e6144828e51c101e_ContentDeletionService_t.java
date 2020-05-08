 package nz.co.searchwellington.modification;
 
 import nz.co.searchwellington.feeds.RssfeedNewsitemService;
 import nz.co.searchwellington.model.Feed;
 import nz.co.searchwellington.model.Newsitem;
 import nz.co.searchwellington.model.PublishedResource;
 import nz.co.searchwellington.model.Resource;
 import nz.co.searchwellington.model.Tag;
 import nz.co.searchwellington.model.Watchlist;
 import nz.co.searchwellington.model.Website;
 import nz.co.searchwellington.repositories.HandTaggingDAO;
 import nz.co.searchwellington.repositories.ResourceRepository;
 import nz.co.searchwellington.repositories.SnapshotDAO;
 import nz.co.searchwellington.repositories.SupressionService;
 import nz.co.searchwellington.repositories.TagDAO;
 import nz.co.searchwellington.repositories.solr.SolrQueryService;
 
 import org.apache.log4j.Logger;
 
 public class ContentDeletionService {
 	
     private static Logger log = Logger.getLogger(ContentDeletionService.class);
     	
 	private SupressionService supressionService;
 	private RssfeedNewsitemService rssfeedNewsitemService;
 	private ResourceRepository resourceDAO;
 	private SnapshotDAO snapshotDAO;
 	private SolrQueryService solrQueryService;
 	private HandTaggingDAO handTaggingDAO;
 	private TagDAO tagDAO;
 
 	public ContentDeletionService(SupressionService supressionService,
 			RssfeedNewsitemService rssfeedNewsitemService,
 			ResourceRepository resourceDAO, 
 			SnapshotDAO snapshotDAO,
 			SolrQueryService solrQueryService, 
 			HandTaggingDAO handTaggingDAO,
 			TagDAO tagDAO) {		
 		this.supressionService = supressionService;
 		this.rssfeedNewsitemService = rssfeedNewsitemService;
 		this.resourceDAO = resourceDAO;
 		this.snapshotDAO = snapshotDAO;
 		this.solrQueryService = solrQueryService;
 		this.handTaggingDAO = handTaggingDAO;
 		this.tagDAO = tagDAO;
 	}
 
 
 	public void performDelete(Resource resource) {		
 		handTaggingDAO.clearTags(resource);
 		
 		if (resource.getType().equals("W")) {
 			removePublisherFromPublishersContent(resource);            	
 		}
 		
 		if (resource.getType().equals("F")) {
 			removeFeedFromFeedNewsitems((Feed) resource);			
 			removeRelatedFeedFromTags((Feed) resource);
 		}
 		
 		if (resource.getType().equals("N")) {
			log.info("Deleted item is a newsitem; checking if it's in an accepted feed.");
 			Newsitem deletedNewsitem = (Newsitem) resource;
 			if (rssfeedNewsitemService.isUrlInAcceptedFeeds(deletedNewsitem.getUrl())) {
 				log.info("Supressing deleted newsitem url as it still visible in an automatically deleted feed: " + deletedNewsitem.getUrl());
 				suppressDeletedNewsitem(deletedNewsitem);
			} else {
				log.info("Not found in live feeds; not supressing");
 			}
 		}
		
 		snapshotDAO.evict(resource.getUrl());
 		solrQueryService.deleteResourceFromIndex(resource.getId());
 		resourceDAO.deleteResource(resource);
 	}
 
 
 	private void removeRelatedFeedFromTags(Feed editResource) {
 		for (Tag tag : tagDAO.getAllTags()) {
 			if (tag.getRelatedFeed() != null && tag.getRelatedFeed().equals(editResource)) {
 				tag.setRelatedFeed(null);
 			}
 		}
 	}
 
 
 	private void suppressDeletedNewsitem(Newsitem deletedNewsitem) {
 		log.info("Deleting a newsitem whose url still appears in a feed; suppressing the url: " + deletedNewsitem.getUrl());			
 		supressionService.suppressUrl(deletedNewsitem.getUrl());
 	}
 
 		
 	private void removePublisherFromPublishersContent(Resource editResource) {
     	Website publisher = (Website) editResource;
     	for (PublishedResource published : resourceDAO.getNewsitemsForPublishers(publisher)) {
     		published.setPublisher(null);
     		resourceDAO.saveResource(publisher);					
     	}
     	for (Feed feed : publisher.getFeeds()) {
     		feed.setPublisher(null);
     		resourceDAO.saveResource(feed);					
     	}
     	for (Watchlist watchlist : publisher.getWatchlist()) {
     		watchlist.setPublisher(null);
     		resourceDAO.saveResource(watchlist);					
     	}
     }
     
 	
     private void removeFeedFromFeedNewsitems(Feed feed) {    	
     	for (Newsitem newsitem : resourceDAO.getNewsitemsForFeed(feed)) {
     		newsitem.setFeed(null);
     		resourceDAO.saveResource(newsitem);					
     	}    	
     }
 
 }
