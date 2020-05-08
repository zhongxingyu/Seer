 package jipdbs.api.v2;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
 import jipdbs.api.Events;
 import jipdbs.api.ServerManager;
 import jipdbs.core.model.Player;
 import jipdbs.core.model.Server;
 import jipdbs.core.model.dao.AliasDAO;
 import jipdbs.core.model.dao.PlayerDAO;
 import jipdbs.core.model.dao.ServerDAO;
 import jipdbs.core.model.dao.cached.AliasCachedDAO;
 import jipdbs.core.model.dao.cached.PlayerCachedDAO;
 import jipdbs.core.model.dao.cached.ServerCachedDAO;
 import jipdbs.core.model.dao.impl.AliasDAOImpl;
 import jipdbs.core.model.dao.impl.PlayerDAOImpl;
 import jipdbs.core.model.dao.impl.ServerDAOImpl;
 import jipdbs.core.model.util.AliasManager;
 import jipdbs.core.util.MailAdmin;
 import jipdbs.exception.UnauthorizedUpdateException;
 import jipdbs.info.BanInfo;
 import jipdbs.info.PlayerInfo;
 
 public class Update {
 
 	private static final Logger log = Logger.getLogger(Update.class.getName());
 
 	protected final ServerDAO serverDAO = new ServerCachedDAO(
 			new ServerDAOImpl());
 	protected final PlayerDAO playerDAO = new PlayerCachedDAO(
 			new PlayerDAOImpl());
 	protected final AliasDAO aliasDAO = new AliasCachedDAO(new AliasDAOImpl());
 	
 	/**
 	 * Updates the name of a server given its uid.
 	 * <p>
 	 * Invoked by the servers when they change their public server name.
 	 * 
 	 * @param key
 	 *            the server uid.
 	 * @param name
 	 *            the server's new name.
 	 * @param remoteAddr
 	 *            the server's remote address.
 	 * @param version
 	 *            the server's B3 plugin version. Can be null.
 	 * @since 0.5
 	 */
 	public void updateName(String key, String name, String version,
 			String remoteAddr) {
 		try {
 			Server server = ServerManager.getAuthorizedServer(key, remoteAddr,name);
 			server.setName(name);
 			server.setUpdated(new Date());
 			server.setPluginVersion(version);
 			serverDAO.save(server);
 
 		} catch (UnauthorizedUpdateException e) {
 			MailAdmin.sendMail("WARN", e.getMessage());
 			log.severe(e.getMessage());
 			StringWriter w = new StringWriter();
 			e.printStackTrace(new PrintWriter(w));
 			log.severe(w.getBuffer().toString());
 		} catch (Exception e) {
 			log.severe(e.getMessage());
 			StringWriter w = new StringWriter();
 			e.printStackTrace(new PrintWriter(w));
 			log.severe(w.getBuffer().toString());
 		}
 	}
 
 	public void cleanServer(Server server) {
 		cleanServer(server, true);
 	}
 
 	public void cleanServer(Server server, boolean updateDate) {
 		playerDAO.cleanConnected(server.getKey());
 		server.setOnlinePlayers(0);
 		server.setDirty(false);
 		if (updateDate) {
 			server.setUpdated(new Date());	
 		}
 		serverDAO.save(server);
 	}
 	
 	/**
 	 * Update player info
 	 * 
 	 * @param server
 	 *            the server instance
 	 * @param list
 	 *            a list of jipdbs.bean.PlayerInfo
 	 * @throws Exception 
 	 * @since 0.5
 	 */
 	public void updatePlayer(Server server, List<PlayerInfo> list) throws Exception {
 		
 		log.info("Processing server: " + server.getName());
 		
 		try {
 			for (PlayerInfo playerInfo : list) {
 				try {
 					Date playerLastUpdate;
 					Player player = playerDAO.findByServerAndGuid(server.getKey(),
 							playerInfo.getGuid());
 					if (player == null) {
 						player = new Player();
 						player.setCreated(new Date());
 						player.setGuid(playerInfo.getGuid());
 						player.setLevel(playerInfo.getLevel());
 						player.setClientId(playerInfo.getClientId());
 						player.setServer(server.getKey());
 						player.setBanInfo(null);
 						player.setBanInfoUpdated(null);
 						playerLastUpdate = playerInfo.getUpdated();
 					} else {
 						player.setLevel(playerInfo.getLevel());
 						if (player.getClientId() == null) {
 							player.setClientId(playerInfo.getClientId());
 						}
 						playerLastUpdate = player.getUpdated();
 					}
 					player.setNickname(playerInfo.getName());
 					player.setIp(playerInfo.getIp());
					if (player.getUpdated() == null || playerInfo.getUpdated().after(player.getUpdated())) {
 						player.setUpdated(playerInfo.getUpdated());	
 					}
 					
 					handlePlayerEvent(playerInfo, player);
 					
 					playerDAO.save(player);
 
 					boolean update = false;
 					if (Events.CONNECT.equals(playerInfo.getEvent())) {
 						if (server.getUpdated() == null
 								|| server.getUpdated().after(playerLastUpdate)) {
 							update = true;
 						}
 					}
 					AliasManager.createAlias(player, update);
 
 				} catch (Exception e) {
 					log.severe(e.getMessage());
 					StringWriter w = new StringWriter();
 					e.printStackTrace(new PrintWriter(w));
 					log.severe(w.getBuffer().toString());
 				}
 			}
 			server.setDirty(true);
 			server.setUpdated(new Date());
 			serverDAO.save(server);
 		} catch (Exception e) {
 			log.severe(e.getMessage());
 			StringWriter w = new StringWriter();
 			e.printStackTrace(new PrintWriter(w));
 			log.severe(w.getBuffer().toString());
 			throw e;
 		}
 	}
 
 	/**
 	 * 
 	 * @param playerInfo
 	 * @param player
 	 */
 	private void handlePlayerEvent(PlayerInfo playerInfo, Player player) {
 		if (Events.BAN.equals(playerInfo.getEvent())) {
 			BanInfo banInfo = new BanInfo(playerInfo.getExtra());
 			player.setBanInfo(banInfo.toString());
 			player.setBanInfoUpdated(playerInfo.getUpdated());
 			player.setConnected(false);
 		} else if (Events.CONNECT.equals(playerInfo.getEvent())
 				|| Events.DISCONNECT.equals(playerInfo.getEvent())
 				|| Events.UNBAN.equals(playerInfo.getEvent())
 				|| Events.UPDATE.equals(playerInfo.getEvent())) {
 			player.setBanInfo(null);
 			player.setBanInfoUpdated(null);
 			if (Events.CONNECT.equals(playerInfo.getEvent()) || Events.UPDATE.equals(playerInfo.getEvent())) {
 				player.setConnected(true);
 			} else if (Events.DISCONNECT.equals(playerInfo.getEvent())) {
 				player.setConnected(false);
 			}
 		} else if (Events.ADDNOTE.equals(playerInfo.getEvent())) {
 			player.setNote(playerInfo.getExtra());
 		} else if (Events.DELNOTE.equals(playerInfo.getEvent())) {
 			player.setNote(null);
 		}
 	}
 
 }
