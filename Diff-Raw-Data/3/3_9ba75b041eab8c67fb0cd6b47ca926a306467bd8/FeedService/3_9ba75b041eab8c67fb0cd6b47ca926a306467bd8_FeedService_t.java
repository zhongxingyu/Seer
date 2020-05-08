 package ar.edu.utn.tacs.group5.service;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.util.List;
 
 import org.slim3.datastore.Datastore;
 import org.slim3.datastore.ModelQuery;
 
 import ar.edu.utn.tacs.group5.meta.FeedMeta;
 import ar.edu.utn.tacs.group5.model.Feed;
 import ar.edu.utn.tacs.group5.model.Item;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.appengine.api.datastore.Transaction;
 
 public class FeedService {
 
     static final String DEFAULT_FEED_TITLE = "My Feed";
    static final String DEFAULT_FEED_DESCRIPTON = "Default feed";
     private FeedMeta feedMeta = FeedMeta.get();
 
     public void insert(Long userId) {
         Feed feed = new Feed();
         feed.setUserId(userId);
         feed.setTitle(DEFAULT_FEED_TITLE);
        feed.setDescription(DEFAULT_FEED_DESCRIPTON);
         insert(feed);
     }
 
     public void insert(Feed feed) {
         Transaction tx = Datastore.beginTransaction();
         Datastore.put(tx, feed);
         tx.commit();
     }
 
     public boolean hasDefaultFeed(Long userId) {
         return queryFeedBy(userId).count() > 0;
     }
 
     private ModelQuery<Feed> queryFeedBy(Long userId) {
         return Datastore.query(feedMeta).filter(feedMeta.userId.getName(), FilterOperator.EQUAL, userId);
     }
 
     public Feed getDefaultFeed(Long userId) {
         return queryFeedBy(userId)
                 .filter(feedMeta.title.getName(), FilterOperator.EQUAL, DEFAULT_FEED_TITLE)
                 .asSingle();
     }
 
     public void addItem(Feed feed, Item item) {
         checkNotNull(feed);
         checkNotNull(item);
         item.getFeedRef().setModel(feed);
         feed.getItems().add(item);
         Datastore.put(feed, item);
     }
 
     public Feed getByKey(Key key) {
         checkNotNull(key);
         Feed feed = Datastore.get(feedMeta, key);
         feed.setItems(feed.getItemListRef().getModelList());
         return feed;
     }
 
     public Feed getByKey(String key) {
         checkNotNull(key);
         return getByKey(KeyFactory.stringToKey(key));
     }
 
     public List<Feed> getAll(Long userId) {
         List<Feed> feeds = queryFeedBy(userId).asList();
         for (Feed feed : feeds) {
             feed.setItems(feed.getItemListRef().getModelList());
         }
         return feeds;
     }
 
 }
