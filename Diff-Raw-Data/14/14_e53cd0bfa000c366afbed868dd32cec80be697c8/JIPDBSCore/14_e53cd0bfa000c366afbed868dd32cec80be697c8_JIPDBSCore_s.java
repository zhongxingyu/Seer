 package jipdbs;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
 import jipdbs.data.Alias;
 import jipdbs.data.AliasDAO;
 import jipdbs.data.Player;
 import jipdbs.data.PlayerDAO;
 import jipdbs.data.Server;
 import jipdbs.data.ServerDAO;
 import jipdbs.util.LocalCache;
 import jipdbs.util.NGrams;
 
 import org.datanucleus.util.StringUtils;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Transaction;
 
 public class JIPDBSCore {
 
 	private static final Logger log = Logger.getLogger(JIPDBSCore.class
 			.getName());
 
 	protected final ServerDAO serverDAO = new ServerDAO();
 	protected final PlayerDAO playerDAO = new PlayerDAO();
 	protected final AliasDAO aliasDAO = new AliasDAO();
 
 	public void start() {
 		// Reserved.
 	}
 
 	public void stop() {
 		// Reserved.
 	}
 
 	// xmlrpc methods //////////////////////////////////////////////////////////
 
 	/**
 	 * Updates the name of a server given its uid.
 	 * <p>
 	 * Invoked by the servers when they change their public server name.
 	 * 
 	 * @param key
 	 *            the server uid.
 	 * @param name
 	 *            the server's new name.
 	 */
 	public void updateName(String key, String name, String remoteAddr) {
 
 		DatastoreService service = DatastoreServiceFactory
 				.getDatastoreService();
 
 		Transaction tx = service.beginTransaction();
 		try {
 			Server server = serverDAO.findByUid(service, key);
 
 			if (server != null) {
 				if (remoteAddr != null
 						&& StringUtils.notEmpty(server.getAddress())) {
 					if (!remoteAddr.equals(server.getAddress())) {
 						log.warning("Unauthorized update");
 						return;
 					}
 				}
 				server.setName(name);
 				server.setUpdated(new Date());
 				serverDAO.save(service, server);
 				tx.commit();
 			} else {
 				log.severe("Trying to update non existing server (" + key + ","
 						+ name + ")");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 		}
 	}
 
 	/**
 	 * Updates the current list of players for a given server.
 	 * 
 	 * @param key
 	 *            the server key.
 	 * @param list
 	 *            the list of currently logged in players.
 	 */
 	public void updateConnect(String key, List<PlayerInfo> list,
 			String remoteAddr) {
 
 		DatastoreService service = DatastoreServiceFactory
 				.getDatastoreService();
 
 		try {
 			Date stamp = new Date();
 
 			Server server = serverDAO.findByUid(service, key);
 
 			if (server != null) {
 
 				if (remoteAddr != null
 						&& StringUtils.notEmpty(server.getAddress())) {
 					if (!remoteAddr.equals(server.getAddress())) {
 						log.warning("Unauthorized update");
 						return;
 					}
 				}
 
 				List<Entity> entities = new ArrayList<Entity>();
 
 				for (PlayerInfo info : list) {
 					String playerKey = "player-"
 							+ KeyFactory.keyToString(server.getKey())
 							+ info.getGuid();
 					Player player = (Player) LocalCache.getInstance().get(
 							playerKey);
 					if (player == null) {
 						player = playerDAO.findByServerAndGuid(service,
 								server.getKey(), info.getGuid());
 					}
 
 					if (player == null) {
 						player = new Player();
 						player.setCreated(stamp);
 						player.setGuid(info.getGuid());
 						player.setServer(server.getKey());
 						player.setBanInfo(null);
 						player.setUpdated(stamp);
 						playerDAO.save(service, player);
 						LocalCache.getInstance().put(playerKey, player);
 					} else {
 						if (player.getBanInfo() != null) {
 							player.setBanInfo(null);
 						}
 						player.setUpdated(stamp);
 						entities.add(player.toEntity());
 						LocalCache.getInstance().put(playerKey, player);
 					}
 
 					String aliasKey = "alias-"
 							+ KeyFactory.keyToString(player.getKey())
 							+ info.getName() + info.getIp();
 					Alias alias = (Alias) LocalCache.getInstance()
 							.get(aliasKey);
 					if (alias == null) {
 						alias = aliasDAO.findByPlayerAndNicknameAndIp(service,
 								player.getKey(), info.getName(), info.getIp());
 					}
 
 					if (alias == null) {
 						alias = new Alias();
 						alias.setCount(1);
 						alias.setCreated(stamp);
 						alias.setNickname(info.getName());
 						alias.setNgrams(NGrams.ngrams(info.getName()));
 						alias.setPlayer(player.getKey());
 						alias.setIp(info.getIp());
 						alias.setServer(server.getKey());
 						alias.setUpdated(stamp);
 					} else {
 						alias.setUpdated(stamp);
 						alias.setCount(alias.getCount() + 1);
 						LocalCache.getInstance().put(aliasKey, alias);
 					}
 					entities.add(alias.toEntity());
 				}
 				server.setUpdated(stamp);
 				serverDAO.cache(server);
 				entities.add(server.toEntity());
 				service.put(entities);
 			} else {
 				log.severe("Trying to update non existing server (" + key + ")");
 			}
 		} catch (Exception e) {
 			log.severe(e.getMessage());
 		}
 	}
 
 	public void updateDisconnect(String key, List<PlayerInfo> list,
 			String remoteAddr) {
 
 		DatastoreService service = DatastoreServiceFactory
 				.getDatastoreService();
 
 		try {
 			Date stamp = new Date();
 
 			Server server = serverDAO.findByUid(service, key);
 
 			if (server != null) {
 
 				if (remoteAddr != null
 						&& StringUtils.notEmpty(server.getAddress())) {
 					if (!remoteAddr.equals(server.getAddress())) {
 						log.warning("Unauthorized update");
 						return;
 					}
 				}
 
 				List<Entity> entities = new ArrayList<Entity>();
 
 				for (PlayerInfo info : list) {
 					String playerKey = "player-"
 							+ KeyFactory.keyToString(server.getKey())
 							+ info.getGuid();
 					Player player = (Player) LocalCache.getInstance().get(
 							playerKey);
 					if (player == null) {
 						player = playerDAO.findByServerAndGuid(service,
 								server.getKey(), info.getGuid());
 					}
 
 					if (player == null) {
 						player = new Player();
 						player.setCreated(stamp);
 						player.setUpdated(stamp);
 						player.setGuid(info.getGuid());
 						player.setServer(server.getKey());
 						player.setBanInfo(null);
 						playerDAO.save(service, player);
 						LocalCache.getInstance().put(playerKey, player);
 					} else {
 						player.setUpdated(stamp);
 						entities.add(player.toEntity());
 						LocalCache.getInstance().put(playerKey, player);
 					}
 
 					String aliasKey = "alias-"
 							+ KeyFactory.keyToString(player.getKey())
 							+ info.getName() + info.getIp();
 					Alias alias = (Alias) LocalCache.getInstance()
 							.get(aliasKey);
 					if (alias == null) {
 						alias = aliasDAO.findByPlayerAndNicknameAndIp(service,
 								player.getKey(), info.getName(), info.getIp());
 					}
 
 					if (alias == null) {
 						alias = new Alias();
 						alias.setCount(1);
 						alias.setCreated(stamp);
 						alias.setNickname(info.getName());
 						alias.setNgrams(NGrams.ngrams(info.getName()));
 						alias.setPlayer(player.getKey());
 						alias.setIp(info.getIp());
 						alias.setUpdated(stamp);
 						alias.setServer(server.getKey());
 						entities.add(alias.toEntity());
 					} else {
 						LocalCache.getInstance().put(aliasKey, alias);	
 					}
 				}
 				server.setUpdated(stamp);
 				serverDAO.cache(server);
 				entities.add(server.toEntity());
 				service.put(entities);
 			} else {
 				log.severe("Trying to update non existing server (" + key + ")");
 			}
 		} catch (Exception e) {
 			log.severe(e.getMessage());
 		}
 
 	}
 
 	/**
 	 * Bulk updates banning information for the given players.
 	 * 
 	 * @param key
 	 *            the server uid.
 	 * @param list
 	 *            the ban information list. If {@link BanInfo#getReason()} is
 	 *            null or empty, stores <code>null</code> y the datastore.
 	 *            That's in order to unban a player.
 	 */
 	public void updateBanInfo(String key, List<BanInfo> list, String remoteAddr) {
 
 		DatastoreService service = DatastoreServiceFactory
 				.getDatastoreService();
 
 		try {
 
 			Date stamp = new Date();
 
 			Server server = serverDAO.findByUid(service, key);
 
 			if (server != null) {
 
 				if (remoteAddr != null
 						&& StringUtils.notEmpty(server.getAddress())) {
 					if (!remoteAddr.equals(server.getAddress())) {
 						log.warning("Unauthorized update");
 						return;
 					}
 				}
 
 				List<Entity> entities = new ArrayList<Entity>();
 				
 				for (BanInfo info : list) {
 
 					Player player = playerDAO.findByServerAndGuid(service,
 							server.getKey(), info.getGuid());
 
 					if (player == null) {
 						player = new Player();
 						player.setCreated(stamp);
 						player.setGuid(info.getGuid());
 						player.setServer(server.getKey());
 					}
 
 					String reason = info.getReason();
 
 					if (reason.isEmpty())
 						reason = null;
 
 					player.setUpdated(stamp);
 					player.setBanInfo(reason);
 					entities.add(player.toEntity());
 				}
 				service.put(entities);
 			} else {
 				log.severe("Trying to update non existing server (" + key + ")");
 			}
 
 		} catch (Exception e) {
 			log.severe(e.getMessage());
 		}
 	}
 
 }
