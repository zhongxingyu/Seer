 package mdettlaff.cloudreader.dao;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import mdettlaff.cloudreader.domain.Feed;
 import mdettlaff.cloudreader.domain.FeedItem;
 
 import org.springframework.stereotype.Repository;
 
 @Repository
 public class FeedItemDao {
 
 	@PersistenceContext
 	private EntityManager em;
 
 	@SuppressWarnings("unchecked")
 	public List<Feed> findFeeds() {
 		return em.createQuery("FROM Feed f ORDER BY f.url").getResultList();
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<FeedItem> find(boolean read, int limit, List<String> excludedItemsGuids) {
 		StringBuilder queryBuilder = new StringBuilder();
 		queryBuilder.append("FROM FeedItem i ");
 		queryBuilder.append("WHERE i.read = :read ");
 		if (!excludedItemsGuids.isEmpty()) {
 			queryBuilder.append("AND i.guid NOT IN :guids ");
 		}
 		queryBuilder.append("ORDER BY i.date, i.downloadDate");
 		Query query = em.createQuery(queryBuilder.toString());
 		query.setParameter("read", read);
 		if (!excludedItemsGuids.isEmpty()) {
 			query.setParameter("guids", excludedItemsGuids);
 		}
 		return query.setMaxResults(limit).getResultList();
 	}
 
 	public long count(boolean read) {
 		return (long) em.createQuery(
 				"SELECT COUNT(i) FROM FeedItem i WHERE i.read = :read")
 				.setParameter("read", read)
 				.getSingleResult();
 	}
 
 	public int saveFeed(Feed feed) {
 		List<FeedItem> newItems = filterNewItems(feed);
 		feed.setItems(newItems);
 		em.find(Feed.class, feed.getUrl());
 		em.merge(feed);
 		em.flush();
 		return newItems.size();
 	}
 
 	private List<FeedItem> filterNewItems(Feed feed) {
 		List<FeedItem> result = new ArrayList<>();
 		for (FeedItem item : feed.getItems()) {
			if (em.find(FeedItem.class, item.getGuid()) == null) {
 				result.add(item);
 			}
 		}
 		return result;
 	}
 
 	public void updateRead(String guid, boolean read) {
 		FeedItem item = em.find(FeedItem.class, guid);
 		item.setRead(read);
 		em.flush();
 	}
 }
