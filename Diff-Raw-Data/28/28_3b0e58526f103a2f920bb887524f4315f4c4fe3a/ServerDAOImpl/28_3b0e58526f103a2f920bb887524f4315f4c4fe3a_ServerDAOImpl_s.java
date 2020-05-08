 package jipdbs.core.model.dao.impl;
 
 import static com.google.appengine.api.datastore.FetchOptions.Builder.withOffset;
 import static com.google.appengine.api.datastore.FetchOptions.Builder.withPrefetchSize;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import jipdbs.core.model.Server;
 import jipdbs.core.model.dao.ServerDAO;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.appengine.api.datastore.Query.SortDirection;
 
 public class ServerDAOImpl implements ServerDAO {
 
 	@Override
 	public void save(Server server) {
 
 		DatastoreService service = DatastoreServiceFactory
 				.getDatastoreService();
 
 		Entity entity = server.toEntity();
 		service.put(entity);
 		server.setKey(entity.getKey());
 	}
 
 	@Override
 	public List<Server> findAll(int offset, int limit, int[] count) {
 
 		DatastoreService service = DatastoreServiceFactory
 				.getDatastoreService();
 
 		Query q = new Query("Server");
 		q.addSort("name", SortDirection.ASCENDING);
 
 		PreparedQuery pq = service.prepare(q);
 		count[0] = pq.countEntities(withPrefetchSize(limit));
 
 		List<Server> list = new ArrayList<Server>();
 
 		for (Entity e : pq.asIterable(withOffset(offset).limit(limit)))
 			list.add(new Server(e));
 
 		return list;
 	}
 
 	@Override
 	public Server findByUid(String uid) {
 
 		DatastoreService service = DatastoreServiceFactory
 				.getDatastoreService();
 
 		Query q = new Query("Server");
 		q.addFilter("uid", FilterOperator.EQUAL, uid);
 		PreparedQuery pq = service.prepare(q);
 		Entity entity = pq.asSingleEntity();
 
 		if (entity != null)
 			return new Server(entity);
 
 		return null;
 	}
 
 	@Override
 	public Server get(Key server) throws EntityNotFoundException {
 
 		DatastoreService service = DatastoreServiceFactory
 				.getDatastoreService();

		return new Server(service.get(server));
 	}
 }
