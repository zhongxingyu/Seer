 package mdettlaff.cloudreader.service;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import mdettlaff.cloudreader.dao.FeedItemDao;
 import mdettlaff.cloudreader.domain.FeedItem;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.sun.syndication.io.FeedException;
 
 @Service
 public class FeedService {
 
 	private static final int INITIAL_SIZE = 14;
 	private static final int BUFFER_SIZE = 4;
 	private static final int LOAD_THRESHOLD = 10;
 
 	private final FeedItemDao dao;
 
 	@Autowired
 	public FeedService(FeedItemDao dao) {
 		this.dao = dao;
 	}
 
 	// no-arg constructor to make CGLIB happy
 	public FeedService() {
 		this(null);
 	}
 
 	public List<FeedItem> getFeedItems() throws FeedException, IOException {
 		return dao.find(false, INITIAL_SIZE, new ArrayList<String>());
 	}
 
 	@Transactional
 	public List<FeedItem> update(List<String> readPendingFeedItemsGuids, List<String> unreadFeedItemsGuids) throws FeedException, IOException {
 		markItemsAsRead(readPendingFeedItemsGuids);
 		return getNewItems(unreadFeedItemsGuids);
 	}
 
 	private void markItemsAsRead(List<String> feedItemsGuids) {
 		for (String guid : feedItemsGuids) {
			markItemAsRead(guid);
 		}
 	}
 
	private void markItemAsRead(String feedItemGuid) {
		dao.updateRead(feedItemGuid, true);
	}

 	private List<FeedItem> getNewItems(List<String> unreadFeedItemsGuids) {
 		List<FeedItem> result;
 		if (unreadFeedItemsGuids.size() < LOAD_THRESHOLD) {
			result =  dao.find(false, BUFFER_SIZE, unreadFeedItemsGuids);
 		} else {
 			result =  new ArrayList<>();
 		}
 		return result;
 	}
 
 	public long countUnreadItems() {
 		return dao.count(false);
 	}
 }
