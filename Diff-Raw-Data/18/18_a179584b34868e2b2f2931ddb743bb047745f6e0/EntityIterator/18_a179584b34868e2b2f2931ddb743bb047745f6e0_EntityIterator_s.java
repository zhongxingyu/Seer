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
 
 	private static final int WAIT = 30000;
 	
 	public static interface Callback {
 
 		void withEntity(Entity entity, DatastoreService ds, Cursor cursor, long totalElements) throws Exception;
 
 	}
 
 	public static void iterate(Query q, long maxElements, int batchSize, Cursor startCursor, Callback callback) {
 
		batchSize = Math.min(batchSize, 1000);
 
 		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
 		PreparedQuery pq = ds.prepare(q);
 
 		QueryResultList<Entity> list = null;
 		if (startCursor == null) {
 			list = pq.asQueryResultList(withLimit(batchSize));
 		} else {
 			list = pq.asQueryResultList(withLimit(batchSize).startCursor(startCursor));
 		}
 
 		long totalElements = 0;
 
 		Cursor cursor = list.getCursor();
 		
		while (totalElements <= maxElements && list.size() > 0) {
 			for (Entity entity : list) {
 				totalElements++;
				if (totalElements > maxElements) break;
 				try {
 					callback.withEntity(entity,
 							DatastoreServiceFactory.getDatastoreService(), list.getCursor(), totalElements);
 				} catch (Exception e) {
 					System.err.println(e.getMessage());
 					e.printStackTrace();
 					break;
 				}
 			}
 			// wait before fetching new data
 			try {
 				Thread.sleep(WAIT);
 			} catch (InterruptedException e) {
 				System.err.println(e.getMessage());
 			}
 			
			list = pq.asQueryResultList(withLimit(
					batchSize).startCursor(cursor));
 			cursor = list.getCursor();
 		}
 	}
 
 	public static void iterate(Query query, long maxElements, Cursor startCursor, Callback callback) {
 		iterate(query, maxElements, 100, startCursor, callback);
 	}
 	
 	public static void iterate(String entityName, long maxElements,	int batchSize, Cursor startCursor, Callback callback) {
 		Query q = new Query(entityName);
 		iterate(q, maxElements, batchSize, startCursor, callback);
 	}
 	
 	public static void iterate(String entityName, long maxElements,	Cursor startCursor, Callback callback) {
 		iterate(entityName, maxElements, 100, startCursor, callback);
 	}
 	
 	public static void iterate(String entityName, long maxElements,	int offset, int batchSize, Cursor startCursor, Callback callback) {
 		Query q = new Query(entityName);
 		iterate(q, maxElements, batchSize, startCursor, callback);
 	}
 	
 }
