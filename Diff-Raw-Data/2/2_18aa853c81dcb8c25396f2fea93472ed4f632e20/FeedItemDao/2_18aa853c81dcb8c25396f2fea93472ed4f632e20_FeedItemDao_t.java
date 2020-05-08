 package mdettlaff.cloudreader.persistence;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import mdettlaff.cloudreader.domain.FeedItem;
 
 import org.springframework.stereotype.Repository;
 
 @Repository
 public class FeedItemDao {
 
 	@PersistenceContext
 	private EntityManager em;
 
 	@SuppressWarnings("unchecked")
 	public List<FeedItem> find(FeedItem.Status status, int limit, List<String> excludedItemsGuids) {
 		StringBuilder queryBuilder = new StringBuilder();
 		queryBuilder.append("FROM FeedItem ");
 		queryBuilder.append("WHERE status = :status ");
 		if (!excludedItemsGuids.isEmpty()) {
 			queryBuilder.append("AND guid NOT IN :guids ");
 		}
		queryBuilder.append("ORDER BY COALESCE(date, downloadDate), downloadDate, title");
 		Query query = em.createQuery(queryBuilder.toString());
 		query.setParameter("status", status);
 		if (!excludedItemsGuids.isEmpty()) {
 			query.setParameter("guids", excludedItemsGuids);
 		}
 		return query.setMaxResults(limit).getResultList();
 	}
 
 	public long count(FeedItem.Status status) {
 		return (long) em.createQuery(
 				"SELECT COUNT(i) FROM FeedItem i WHERE status = :status")
 				.setParameter("status", status)
 				.getSingleResult();
 	}
 
 	public void updateStatus(String guid, FeedItem.Status status) {
 		FeedItem item = em.find(FeedItem.class, guid);
 		item.setStatus(status);
 		em.flush();
 	}
 }
