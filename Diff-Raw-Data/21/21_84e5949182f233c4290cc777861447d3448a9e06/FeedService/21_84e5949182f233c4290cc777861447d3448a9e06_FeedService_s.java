 package ar.edu.utn.tacs.group5.service;
 
 import org.slim3.datastore.Datastore;
 import org.slim3.datastore.ModelQuery;
 
 import ar.edu.utn.tacs.group5.meta.FeedMeta;
 import ar.edu.utn.tacs.group5.model.Feed;
 import ar.edu.utn.tacs.group5.model.Item;
 
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.appengine.api.datastore.Transaction;
 
 
 public class FeedService {
 
 	private static final String DEFAULT_FEED = "My Feed";
 	private FeedMeta feedMeta = FeedMeta.get();
 
 	public void insert(Long userId) {
 		Feed feed = new Feed();
 		feed.setUserId(userId);
 		feed.setTitle(DEFAULT_FEED);
 		insert(feed);
 	}
 
 	public void insert(Feed feed) {
 		Transaction tx = Datastore.beginTransaction();
 		Datastore.put(tx, feed);
 		tx.commit();
 	}
 
 	public boolean hasDefaultFeed(Long userId) {
		return queryFeedBy(userId).count() == 0;
 	}
 
 	private ModelQuery<Feed> queryFeedBy(Long userId) {
 		return Datastore.query(feedMeta).filter(feedMeta.userId.getName(), FilterOperator.EQUAL, userId);
 	}
 
 	public Feed getByUserId(Long userId) {
 		Feed feed = queryFeedBy(userId).asSingle();
 		feed.setItems(feed.getItemListRef().getModelList());
 		return feed;
 	}
 
 	public void addTorrent(Long userId, String link) {
 		Feed feed = getByUserId(userId);
 		Item item = new Item();
 		item.setLink(link);
 		item.getFeedRef().setModel(feed);
 		Datastore.put(feed, item);
 	}
 
 }
