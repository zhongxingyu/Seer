 package jipdbs.admin.commands;
 
 import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import jipdbs.admin.Command;
 import jipdbs.admin.utils.EntityIterator;
 import jipdbs.admin.utils.EntityIterator.Callback;
 import jipdbs.core.model.Alias;
 import jipdbs.core.model.AliasIP;
 import jipdbs.core.model.Player;
 import jipdbs.core.model.dao.AliasDAO;
 import jipdbs.core.model.dao.impl.AliasDAOImpl;
 import jipdbs.core.util.LocalCache;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Transaction;
 
 public class UpdateAliasIp extends Command {
 
 	static int count = 0;
 
 	@Override
 	protected void execute(String[] args) throws Exception {
 		final long maxEntities = 10000000000L;
 
 		final AliasDAO aliasDAO = new AliasDAOImpl();
 
 		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
 
 		Query playerQuery = new Query("Player");
 		//playerQuery.addFilter("nickname", FilterOperator.EQUAL, "");
 
 		PreparedQuery pq = ds.prepare(playerQuery);
 		final int total = pq.countEntities(withLimit(Integer.MAX_VALUE));
 		count = 0;
 
 		System.out.println("Processing " + total + " records.");
 
 		EntityIterator.iterate(playerQuery, maxEntities, new Callback() {
 			@SuppressWarnings("deprecation")
 			@Override
 			public void withEntity(Entity entity, DatastoreService ds)
 					throws Exception {
 
 				final Player player = new Player(entity);
 
 				count = count + 1;
 
 				if (player.getNickname() != null) return;
 
 				Alias lastAlias = aliasDAO.getLastUsedAlias(player.getKey());
 
 				player.setNickname(lastAlias.getNickname());
 				player.setIp(lastAlias.getIp());
 
 				List<Alias> aliases = aliasDAO.findByPlayer(player.getKey(), 0,
 						1000, null);
 
 				List<Key> deleteAlias = new ArrayList<Key>();
 				Map<String, Entity> mapAlias = new LinkedHashMap<String, Entity>();
 				Map<String, Entity> mapIP = new LinkedHashMap<String, Entity>();
 
 				for (Alias alias : aliases) {
 					deleteAlias.add(alias.getKey());
 					if (alias.getCreated() == null) alias.setCreated(new Date());
 					if (alias.getUpdated() == null) alias.setUpdated(new Date());
 					Alias newAlias = null;
 					if (mapAlias.containsKey(alias.getNickname())) {
 						newAlias = new Alias(mapAlias.get(alias.getNickname()));
 						newAlias.setCount(newAlias.getCount() + 1L);
 						if (alias.getUpdated().after(newAlias.getUpdated())) {
 							newAlias.setUpdated(alias.getUpdated());
 						}
 						if (alias.getCreated().before(newAlias.getCreated())) {
 							newAlias.setCreated(alias.getCreated());
 						}
 					} else {
 						newAlias = new Alias(player.getKey());
 						newAlias.setCount(1L);
						newAlias.setNickname(alias.getNickname());
 						newAlias.setCreated(alias.getCreated());
 						newAlias.setUpdated(alias.getUpdated());
 						newAlias.setNgrams(alias.getNgrams());
 						newAlias.setServer(player.getServer());
 					}
 					mapAlias.put(alias.getNickname(), newAlias.toEntity());
 					AliasIP newIpAlias = null;
 					if (mapIP.containsKey(alias.getIp())) {
 						newIpAlias = new AliasIP(mapIP.get(alias.getIp()));
 						newIpAlias.setCount(newIpAlias.getCount() + 1L);
 						if (alias.getUpdated().after(newIpAlias.getUpdated())) {
 							newIpAlias.setUpdated(alias.getUpdated());
 						}
 						if (alias.getCreated().before(newIpAlias.getCreated())) {
 							newIpAlias.setCreated(alias.getCreated());
 						}
 					} else {
 						newIpAlias = new AliasIP(player.getKey());
 						newIpAlias.setCount(1L);
 						newIpAlias.setCreated(alias.getCreated());
 						newIpAlias.setIp(alias.getIp());
 						newIpAlias.setUpdated(alias.getUpdated());
 					}
 					mapIP.put(alias.getIp(), newIpAlias.toEntity());
 				}
 
 				Transaction tx = ds.beginTransaction();
 				try {
 					ds.put(player.toEntity());
 					ds.put(mapAlias.values());
 					ds.put(mapIP.values());
 					ds.delete(deleteAlias);
 
 					tx.commit();
 				} catch (Exception e) {
 					System.err.println("Player: " + player.getGuid());
 					System.err.println(e.getMessage());
 				} finally {
 					if (tx.isActive())
 						tx.rollback();
 				}
 
 				System.out.println("%" + (count * 100) / total);
 
 			}
 		});
 
 		LocalCache.getInstance().clearAll();
 		
 		System.out.print("Done");
 
 	}
 
 }
