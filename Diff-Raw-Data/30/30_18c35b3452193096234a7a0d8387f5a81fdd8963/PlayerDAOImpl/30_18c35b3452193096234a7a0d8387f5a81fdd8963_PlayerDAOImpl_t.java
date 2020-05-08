 package jipdbs.core.model.dao.impl;
 
 import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
 import static com.google.appengine.api.datastore.FetchOptions.Builder.withOffset;
 import static com.google.appengine.api.datastore.FetchOptions.Builder.withPrefetchSize;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import jipdbs.core.model.Player;
 import jipdbs.core.model.dao.PlayerDAO;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.appengine.api.datastore.Query.SortDirection;
 
 public class PlayerDAOImpl implements PlayerDAO {
 
 	@Override
 	public void save(Player player, boolean commit) {
 		if (commit) {
 			DatastoreService service = DatastoreServiceFactory
 					.getDatastoreService();
 			Entity entity = player.toEntity();
 			service.put(entity);
 			player.setKey(entity.getKey());
 		}
 	}
 
 	@Override
 	public Player findByServerAndGuid(Key server, String guid) {
 
 		DatastoreService service = DatastoreServiceFactory
 				.getDatastoreService();
 
 		Query q = new Query("Player");
 		q.setAncestor(server);
 		q.addFilter("guid", FilterOperator.EQUAL, guid);
 		PreparedQuery pq = service.prepare(q);
 		Entity entity = pq.asSingleEntity();
 
 		if (entity != null)
 			return new Player(entity);
 
 		return null;
 	}
 
 	@Override
 	public List<Player> findLatest(int offset, int limit, int[] count) {
 
 		DatastoreService service = DatastoreServiceFactory
 				.getDatastoreService();
 
 		Query q = new Query("Player");
 		q.addSort("connected", SortDirection.DESCENDING);
 		q.addSort("updated", SortDirection.DESCENDING);
 		PreparedQuery pq = service.prepare(q);
 
 		count[0] = pq.countEntities(withPrefetchSize(limit));
 
 		List<Player> players = new ArrayList<Player>();
 
 		for (Entity entity : pq.asIterable(withOffset(offset).limit(limit)))
 			players.add(new Player(entity));
 
 		return players;
 	}
 
 	@Override
 	public List<Player> findBanned(int offset, int limit, int[] count) {
 
 		DatastoreService service = DatastoreServiceFactory
 				.getDatastoreService();
 
 		Query q = new Query("Player");
 		q.addFilter("baninfoupdated", FilterOperator.NOT_EQUAL, null);
 		q.addSort("baninfoupdated", SortDirection.DESCENDING);
 		PreparedQuery pq = service.prepare(q);
 
 		count[0] = pq.countEntities(withPrefetchSize(limit));
 
 		List<Player> players = new ArrayList<Player>();
 
 		for (Entity entity : pq.asIterable(withOffset(offset).limit(limit)))
 			players.add(new Player(entity));
 
 		return players;
 	}
 
 	@Override
 	public Player get(Key player) throws EntityNotFoundException {
 
 		DatastoreService service = DatastoreServiceFactory
 				.getDatastoreService();
 
 		return new Player(service.get(player));
 	}
 
 	@Override
 	public void truncate() {
 
 		DatastoreService service = DatastoreServiceFactory
 				.getDatastoreService();
 
 		Query q = new Query("Player");
 		q.setKeysOnly();
 		PreparedQuery pq = service.prepare(q);
 		List<Key> keys = new ArrayList<Key>();
 		for (Entity entity : pq.asIterable()) {
 			keys.add(entity.getKey());
 		}
 		service.delete(keys);
 	}
 
 	@Override
 	public void cleanConnected(Key server) {
 
 		DatastoreService service = DatastoreServiceFactory
 				.getDatastoreService();
 
 		Query q = new Query("Player");
 		q.setAncestor(server);
 		q.addFilter("connected", FilterOperator.EQUAL, new Boolean(true));
 		PreparedQuery pq = service.prepare(q);
 
 		List<Entity> commit = new ArrayList<Entity>();
 		for (Entity entity : pq.asIterable()) {
 			entity.setProperty("connected", new Boolean(false));
 			commit.add(entity);
 		}
 		if (commit.size() > 0)
 			service.put(commit);
 
 	}
 
 	@Override
 	public void save(Collection<Player> players, boolean commit) {
 		for (Player player : players)
 			save(player, commit);
 	}
 
 	@Override
 	public void save(Player player) {
 		save(player, true);
 	}
 
 	@Override
 	public void save(Collection<Player> players) {
 		save(players, true);
 	}
 
 	@Override
 	public int countConnected(Key server) {
 		DatastoreService service = DatastoreServiceFactory
 				.getDatastoreService();
 
 		Query q = new Query("Player");
 		q.setAncestor(server);
 		q.addFilter("connected", FilterOperator.EQUAL, new Boolean(true));
 		PreparedQuery pq = service.prepare(q);
 		return pq.countEntities(withLimit(100));
 
 	}
 
 	@Override
 	public List<Player> findByServer(String query, int offset, int limit,
 			int[] count) {
 
 		DatastoreService service = DatastoreServiceFactory
 				.getDatastoreService();
 
 		Key server = KeyFactory.stringToKey(query);
		Query q = new Query("Player");
 		q.setAncestor(server);
 		q.addSort("updated", SortDirection.DESCENDING);
 		PreparedQuery pq = service.prepare(q);
 
 		count[0] = pq.countEntities(withPrefetchSize(limit));
 
 		List<Player> result = new ArrayList<Player>();
 		for (Entity player : pq.asIterable(withLimit(limit).offset(offset))) {
 			result.add(new Player(player));
 		}
 		return result;
 	}
 }
