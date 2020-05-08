 package jipdbs.admin.utils;
 
 import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
 
 import com.google.appengine.api.datastore.Cursor;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.QueryResultList;
 
 public final class EntityIterator {
 
 	public static interface Callback {
 
 		void withEntity(Entity entity, DatastoreService ds) throws Exception;
 
 	}
 
 	public static void iterate(Query q, long maxElements, int offset, int batchSize, Callback callback) {
 
 		batchSize = Math.min(batchSize, 1000);
 
 		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
 		PreparedQuery pq = ds.prepare(q);
 
 		QueryResultList<Entity> list = pq
 				.asQueryResultList(withLimit(batchSize).offset(offset));
 
 		long totalElements = 0;
 
 		Cursor cursor = list.getCursor();
 
 		while (totalElements <= maxElements && list.size() > 0) {
 			for (Entity entity : list) {
				totalElements++;
				if (totalElements > maxElements) break;
 				try {
 					callback.withEntity(entity,
 							DatastoreServiceFactory.getDatastoreService());
 				} catch (Exception e) {
 					System.err.println(e.getMessage());
 					e.printStackTrace();
 					break;
 				}
 			}
 			list = pq.asQueryResultList(withLimit(
 					batchSize).startCursor(cursor));
 			cursor = list.getCursor();
 		}
 	}
 
 	public static void iterate(Query query, long maxElements,
 			Callback callback) {
 		iterate(query, maxElements, 0, 100, callback);
 	}
 	
 	public static void iterate(String entityName, long maxElements,
 			int batchSize, Callback callback) {
 		Query q = new Query(entityName);
 		iterate(q, maxElements, 0, batchSize, callback);
 	}
 	
 	public static void iterate(String entityName, long maxElements,
 			Callback callback) {
 		iterate(entityName, maxElements, 100, callback);
 	}
 	
 	public static void iterate(String entityName, long maxElements,
 			int offset, int batchSize, Callback callback) {
 		Query q = new Query(entityName);
 		iterate(q, maxElements, offset, batchSize, callback);
 	}
 	
 }
