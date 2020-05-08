 package nz.co.searchwellington.feeds;
 
 import java.util.Collections;
 import java.util.List;
 
 import nz.co.searchwellington.model.Feed;
 import nz.co.searchwellington.model.FeedNewsitem;
 import nz.co.searchwellington.repositories.ResourceRepository;
 import nz.co.searchwellington.repositories.SupressionRepository;
 
 public class CachingRssfeedNewsitemService extends RssfeedNewsitemService {
 
 	private FeedNewsitemCache feedNewsitemCache;
 	
	public CachingRssfeedNewsitemService(ResourceRepository resourceDAO, SupressionRepository suppressionDAO, FeedNewsitemCache feedNewsitemCache) {
 		this.resourceDAO = resourceDAO;
 		this.suppressionDAO = suppressionDAO;
 		this.feedNewsitemCache = feedNewsitemCache;
 	}
 
 	@Override
 	public List<FeedNewsitem> getFeedNewsitems(Feed feed) {
 		List<FeedNewsitem> cachedItems = feedNewsitemCache.getFeeditems(feed.getUrl());
 		if (cachedItems != null) {
 			return cachedItems;
 		}
 		return Collections.emptyList();
 	}
 	
 }
