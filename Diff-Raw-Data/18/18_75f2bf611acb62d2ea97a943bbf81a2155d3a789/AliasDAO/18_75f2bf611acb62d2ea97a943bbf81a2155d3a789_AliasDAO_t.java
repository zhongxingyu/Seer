 package jipdbs.data;
 
 import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
 import static com.google.appengine.api.datastore.FetchOptions.Builder.withPrefetchSize;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
import java.util.logging.Logger;
 
 import jipdbs.util.Functions;
 import jipdbs.util.NGrams;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.appengine.api.datastore.Query.SortDirection;
 
 public class AliasDAO {
 
	private static final Logger log = Logger.getLogger(AliasDAO.class.getName());
	
 	public void save(DatastoreService service, Alias alias) {
 		Entity entity = alias.toEntity();
 		service.put(entity);
 		alias.setKey(entity.getKey());
 	}
 
 	public Alias findByPlayerAndNicknameAndIp(DatastoreService service,
 			Key player, String nickname, String ip) {
 
 		Query q = new Query("Alias");
 		q.setAncestor(player);
 		q.addFilter("nickname", FilterOperator.EQUAL, nickname);
 		q.addFilter("ip", FilterOperator.EQUAL, Functions.ipToDecimal(ip));
 		PreparedQuery pq = service.prepare(q);
		Entity entity = null;
		try {
			entity = pq.asSingleEntity();			
		} catch (TooManyResultsException e) {
			log.severe("DUPLICATED:" + nickname + " with ip " + ip + " for player " + player);
			List<Entity> list = pq.asList(withLimit(1));
			if (list.size()>0) {
				entity = list.get(0);
			}
		}
 		if (entity != null)
 			return new Alias(entity);
 		return null;
 	}
 
 	public Alias getLastUsedAlias(DatastoreService service, Key player) {
 
 		Query q = new Query("Alias");
 		q.setAncestor(player);
 		q.addSort("updated", SortDirection.DESCENDING);
 
 		PreparedQuery pq = service.prepare(q);
 		List<Entity> list = pq.asList(withLimit(1));
 		if (list.size() == 0)
 			return null;
 
 		return new Alias(list.get(0));
 	}
 
 	public List<Alias> findByNickname(DatastoreService service, String query,
 			int offset, int limit, int[] count) {
 
 		Collection<String> ngrams = new ArrayList<String>();
 		ngrams.add(query.toLowerCase());
 
 		Query q = new Query("Alias");
 		q.addFilter("ngrams", FilterOperator.IN, ngrams);
 		q.addSort("updated", SortDirection.DESCENDING);
 
 		PreparedQuery pq = service.prepare(q);
 
 		count[0] = pq.countEntities(withPrefetchSize(limit));
 
 		List<Entity> list = pq.asList(withLimit(limit).offset(offset));
 
 		List<Alias> result = new ArrayList<Alias>();
 
 		for (Entity alias : list)
 			result.add(new Alias(alias));
 
 		return result;
 	}
 
 	public List<Alias> findByNGrams(DatastoreService service, String query,
 			int offset, int limit, int[] count) {
 
 		Collection<String> ngrams = NGrams.ngrams(query);
 
 		if (ngrams.size() == 0)
 			return Collections.emptyList();
 
 		Query q = new Query("Alias");
 		q.addFilter("ngrams", FilterOperator.IN, ngrams);
 
 		PreparedQuery pq = service.prepare(q);
 
 		count[0] = pq.countEntities(withPrefetchSize(limit));
 
 		List<Entity> list = pq.asList(withLimit(limit).offset(offset));
 
 		List<Alias> result = new ArrayList<Alias>();
 
 		for (Entity alias : list)
 			result.add(new Alias(alias));
 
 		return result;
 	}
 
 	public List<Alias> findByPlayer(DatastoreService service, Key player,
 			int offset, int limit, int[] count) {
 		Query q = new Query("Alias");
 		q.setAncestor(player);
 		q.addSort("updated", SortDirection.DESCENDING);
 		
 		PreparedQuery pq = service.prepare(q);
 
 		count[0] = pq.countEntities(withPrefetchSize(limit));
 
 		List<Entity> list = pq.asList(withLimit(limit).offset(offset));
 
 		List<Alias> result = new ArrayList<Alias>();
 
 		for (Entity entity : list)
 			result.add(new Alias(entity));
 
 		return result;
 	}
 
 	public List<Alias> findByIP(DatastoreService service, String query,
 			int offset, int limit, int[] count) {
 		Query q = new Query("Alias");
 
 		Long[] range = Functions.getIpRange(query);
 		q.addFilter("ip", FilterOperator.GREATER_THAN_OR_EQUAL, range[0]);
 		q.addFilter("ip", FilterOperator.LESS_THAN_OR_EQUAL, range[1]);
 		q.addSort("ip", SortDirection.ASCENDING);
 		q.addSort("updated", SortDirection.DESCENDING);
 
 		PreparedQuery pq = service.prepare(q);
 
 		count[0] = pq.countEntities(withPrefetchSize(limit));
 
 		List<Entity> list = pq.asList(withLimit(limit).offset(offset));
 
 		List<Alias> result = new ArrayList<Alias>();
 
 		for (Entity entity : list)
 			result.add(new Alias(entity));
 
 		return result;
 	}
 	
 	public List<Alias> findByServer(DatastoreService service, String query,
 			int offset, int limit, int[] count) {
 		
 		Key server = KeyFactory.stringToKey(query);
 		Query q = new Query("Player").setKeysOnly();
 		q.setAncestor(server);
 		q.addSort("updated", SortDirection.DESCENDING);
 		PreparedQuery pq = service.prepare(q);
 		
 		count[0] = pq.countEntities(withPrefetchSize(limit));
 		
 		List<Alias> result = new ArrayList<Alias>();
 		for (Entity player : pq.asIterable(withLimit(limit).offset(offset))) {
 			Alias alias = getLastUsedAlias(service, player.getKey());
 			if (alias != null) result.add(alias);
 		}
 		return result;
 	}
 	
 	public void truncate(DatastoreService service) {
 		Query q = new Query("Alias");
 		q.setKeysOnly();
 		PreparedQuery pq = service.prepare(q);
 		List<Key> keys = new ArrayList<Key>();
 		for (Entity entity : pq.asIterable()) {
 			keys.add(entity.getKey());
 		}
 		service.delete(keys);
 	}
 	
 }
