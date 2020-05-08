 package jipdbs.data;
 
 import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
 import static com.google.appengine.api.datastore.FetchOptions.Builder.withPrefetchSize;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import jipdbs.util.Functions;
 import jipdbs.util.LocalCache;
 import jipdbs.util.NGrams;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.appengine.api.datastore.Query.SortDirection;
 
 public class AliasDAO {
 
 	private Entity map(Alias alias) {
 
 		Entity entity = alias.getKey() == null ? new Entity("Alias")
 				: new Entity(alias.getKey());
 
 		entity.setProperty("created", alias.getCreated());
 		entity.setProperty("updated", alias.getUpdated());
 		entity.setProperty("count", alias.getCount());
 		entity.setProperty("ip", Functions.ipToDecimal(alias.getIp()));
 		entity.setProperty("nickname", alias.getNickname());
 		entity.setProperty("ngrams", alias.getNgrams());
 		entity.setProperty("player", alias.getPlayer());
 
 		return entity;
 	}
 
 	@SuppressWarnings("unchecked")
 	private Alias map(Entity entity) {
 
 		Alias alias = new Alias();
 
 		alias.setKey(entity.getKey());
 		alias.setCreated((Date) entity.getProperty("created"));
 		alias.setUpdated((Date) entity.getProperty("updated"));
 		alias.setCount(((Long) entity.getProperty("count")).intValue());
 		alias.setIp((String) Functions.decimalToIp((Long) entity
 				.getProperty("ip")));
 		alias.setPlayer((Key) entity.getProperty("player"));
 		alias.setNickname((String) entity.getProperty("nickname"));
 		alias.setNgrams((Collection<String>) entity.getProperty("ngrams"));
 
 		return alias;
 	}
 
 	public void save(DatastoreService service, Alias alias) {
 		Entity entity = map(alias);
 		service.put(entity);
 		alias.setKey(entity.getKey());
 		// save to cache
 		LocalCache.getInstance().put(
 				"alias-" + KeyFactory.keyToString(alias.getPlayer())
 						+ alias.getNickname() + alias.getIp(), alias);
 	}
 
 	public Alias findByPlayerAndNicknameAndIp(DatastoreService service,
 			Key player, String nickname, String ip) {
 
 		// retrieve from cache
 		Alias p = (Alias) LocalCache.getInstance().get(
 				"alias-" + KeyFactory.keyToString(player) + nickname + ip);
 		if (p != null)
 			return p;
 
 		Query q = new Query("Alias");
 		q.addFilter("player", FilterOperator.EQUAL, player);
 		q.addFilter("nickname", FilterOperator.EQUAL, nickname);
 		q.addFilter("ip", FilterOperator.EQUAL, ip);
 		PreparedQuery pq = service.prepare(q);
 		Entity entity = pq.asSingleEntity();
 
 		if (entity != null)
 			return map(entity);
 
 		return null;
 	}
 
 	public Alias getLastUsedAlias(DatastoreService service, Key player) {
 
 		Query q = new Query("Alias");
 		q.addFilter("player", FilterOperator.EQUAL, player);
 		q.addSort("updated", SortDirection.DESCENDING);
 
 		PreparedQuery pq = service.prepare(q);
 		List<Entity> list = pq.asList(withLimit(1));
 		if (list.size() == 0)
 			return null;
 
 		return map(list.get(0));
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
 			result.add(map(alias));
 
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
 			result.add(map(alias));
 
 		return result;
 	}
 
 	public List<Alias> findByPlayer(DatastoreService service, Key player,
 			int offset, int limit, int[] count) {
 		Query q = new Query("Alias");
 		q.addFilter("player", FilterOperator.EQUAL, player);
 		q.addSort("count", SortDirection.DESCENDING);
 
 		PreparedQuery pq = service.prepare(q);
 
 		count[0] = pq.countEntities(withPrefetchSize(limit));
 
 		List<Entity> list = pq.asList(withLimit(limit).offset(offset));
 
 		List<Alias> result = new ArrayList<Alias>();
 
 		for (Entity entity : list)
 			result.add(map(entity));
 
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
 			result.add(map(entity));
 
 		return result;
 	}
 }
