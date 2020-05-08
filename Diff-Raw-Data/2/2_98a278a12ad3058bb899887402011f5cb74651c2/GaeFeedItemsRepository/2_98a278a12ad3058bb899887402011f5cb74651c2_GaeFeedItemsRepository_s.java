 package nmd.rss.collector.gae.persistence;
 
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import nmd.rss.collector.feed.FeedItem;
 import nmd.rss.collector.feed.FeedItemsMergeReport;
 import nmd.rss.collector.updater.FeedItemsRepository;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
 import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
 import static java.lang.Integer.MAX_VALUE;
 import static nmd.rss.collector.gae.persistence.FeedItemConverter.KIND;
 import static nmd.rss.collector.gae.persistence.GaeRootRepository.DATASTORE_SERVICE;
 import static nmd.rss.collector.gae.persistence.GaeRootRepository.getFeedRootKey;
 import static nmd.rss.collector.util.Assert.assertNotNull;
 
 /**
  * User: igu
  * Date: 16.10.13
  */
 public class GaeFeedItemsRepository implements FeedItemsRepository {
 
     @Override
     public void mergeItems(final UUID feedId, final FeedItemsMergeReport feedItemsMergeReport) {
         assertNotNull(feedId);
         assertNotNull(feedItemsMergeReport);
 
        deleteItems(feedId);

         final Key feedRootKey = getFeedRootKey(feedId);
 
         for (final FeedItem feedItem : feedItemsMergeReport.removed) {
             final Key victim = getFeedItemKey(feedId, feedItem.guid);
 
             if (victim != null) {
                 DATASTORE_SERVICE.delete(victim);
             }
         }
 
         for (final FeedItem feedItem : feedItemsMergeReport.added) {
             final Entity entity = FeedItemConverter.convert(feedItem, feedRootKey);
 
             DATASTORE_SERVICE.put(entity);
         }
     }
 
     @Override
     public List<FeedItem> loadItems(final UUID feedId) {
         assertNotNull(feedId);
 
         final Key feedRootKey = getFeedRootKey(feedId);
 
         if (feedRootKey == null) {
             return null;
         }
 
         final Query query = new Query(KIND).setAncestor(feedRootKey);
         final PreparedQuery preparedQuery = DATASTORE_SERVICE.prepare(query);
 
         final List<Entity> entities = preparedQuery.asList(withLimit(MAX_VALUE));
 
         final List<FeedItem> feedItems = new ArrayList<>(entities.size());
 
         for (final Entity entity : entities) {
             final FeedItem feedItem = FeedItemConverter.convert(entity);
 
             feedItems.add(feedItem);
         }
 
         return feedItems;
     }
 
     @Override
     public void deleteItems(final UUID feedId) {
         assertNotNull(feedId);
 
         final Key feedRootKey = getFeedRootKey(feedId);
         final Query query = new Query(KIND).setAncestor(feedRootKey).setKeysOnly();
         final PreparedQuery preparedQuery = DATASTORE_SERVICE.prepare(query);
 
         final List<Entity> victims = preparedQuery.asList(withLimit(MAX_VALUE));
 
         for (final Entity victim : victims) {
             DATASTORE_SERVICE.delete(victim.getKey());
         }
     }
 
     private Key getFeedItemKey(final UUID feedId, final String itemGuid) {
         final Key feedRootKey = getFeedRootKey(feedId);
         final Query query = new Query(KIND).setAncestor(feedRootKey).setKeysOnly().setFilter(new Query.FilterPredicate(FeedItemConverter.GUID, EQUAL, itemGuid));
         final PreparedQuery preparedQuery = DATASTORE_SERVICE.prepare(query);
 
         final Entity found = preparedQuery.asSingleEntity();
 
         return found == null ? null : found.getKey();
     }
 
 }
