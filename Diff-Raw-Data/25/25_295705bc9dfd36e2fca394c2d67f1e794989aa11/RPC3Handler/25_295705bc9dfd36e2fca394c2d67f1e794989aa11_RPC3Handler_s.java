 package jipdbs.xmlrpc.handler;
 
 import iddb.api.Events;
 import iddb.api.ServerManager;
 import iddb.api.v2.Update;
 import iddb.core.IDDBService;
 import iddb.core.model.Penalty;
 import iddb.core.model.Server;
 import iddb.exception.UnauthorizedUpdateException;
 import iddb.exception.UpdateApiException;
 import iddb.info.PenaltyInfo;
 import iddb.info.PlayerInfo;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import jipdbs.xmlrpc.JIPDBSXmlRpc3Servlet;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class RPC3Handler {
 
 	private static final Logger log = LoggerFactory.getLogger(RPC3Handler.class);
 	
 	@SuppressWarnings("unused")
 	private final IDDBService app;
 	private final Update updateApi;
 
 	private static final int maxListSize = 100;
 
 	public RPC3Handler(IDDBService app) {
 		this.app = app;
 		this.updateApi = new Update();
 	}
 
 	public void updateName(String key, String name, Object[] data) {
 		this.updateApi.updateName(key, name, (String) data[0],
 				(Integer) data[1], JIPDBSXmlRpc3Servlet.getClientIpAddress());
 	}
 
 	public Integer register(String key, String userid, Object[] data) throws UpdateApiException, Exception {
 		try {
 			Server server = ServerManager.getAuthorizedServer(key,
 					JIPDBSXmlRpc3Servlet.getClientIpAddress());
 			
 			PlayerInfo playerInfo = new PlayerInfo("register",
 													(String) data[0],
 													(String) data[1],
 													parseLong(data[2]),
 													(String) data[3],
 													parseLong(data[4]));
 			
 			return this.updateApi.linkUser(server, userid, playerInfo);
 			
 		} catch (UnauthorizedUpdateException e) {
 			log.warn(e.getMessage());
 //			StringWriter w = new StringWriter();
 //			e.printStackTrace(new PrintWriter(w));
 //			log.error(w.getBuffer().toString());
 			throw new UpdateApiException(e.getMessage());
 		} catch (Exception e) {
 			log.error(e.getMessage());
 			StringWriter w = new StringWriter();
 			e.printStackTrace(new PrintWriter(w));
 			log.error(w.getBuffer().toString());
 			throw e;
 		}
 	}
 	
 	public void update(String key, Object[] plist) throws UpdateApiException, Exception {
 
 		try {
 			Server server = ServerManager.getAuthorizedServer(key,
 					JIPDBSXmlRpc3Servlet.getClientIpAddress());
 
 			List<PlayerInfo> list = new ArrayList<PlayerInfo>();
 			for (Object o : plist) {
 				try {
 					list.add(processEventInfo(o));
 				} catch (Exception e) {
 					log.error(e.getMessage());
 				}
 			}
 			if (list.size() > 0) {
 				if (list.size() > maxListSize) {
 					log.warn("List size is " + Integer.toString(list.size()));
 					// this is too much to process
 					list = list.subList(list.size() - maxListSize, list.size());
 				} else {
 					log.info("List size is " + Integer.toString(list.size()));
 				}
 				updateApi.updatePlayer(server, list);
 			} else {
				if (server.getOnlinePlayers() > 0) {
 					log.debug("Cleaning server " + server.getName());
 					updateApi.cleanServer(server);
 				}
 			}
 		} catch (UnauthorizedUpdateException e) {
 			log.warn(e.getMessage());
 //			StringWriter w = new StringWriter();
 //			e.printStackTrace(new PrintWriter(w));
 //			log.error(w.getBuffer().toString());
 //			throw new ApplicationHandlerException(e.getMessage());
 		} catch (Exception e) {
 			log.error(e.getMessage());
 			StringWriter w = new StringWriter();
 			e.printStackTrace(new PrintWriter(w));
 			log.error(w.getBuffer().toString());
 			throw e;
 		}
 	}
 
 	private PlayerInfo processEventInfo(Object o) throws Exception {
 		Object[] values = ((Object[]) o);
		if (log.isDebugEnabled()) log.debug(Arrays.toString(values));
 		String event = (String) values[0];
 		PlayerInfo playerInfo = new PlayerInfo(event,
 												(String) values[1],
 												(String) values[2],
 												parseLong(values[3]),
 												(String) values[4],
 												parseLong(values[5]));
 		if (values.length > 6) {
 			Date updated;
 			if (values[6] instanceof Date) {
 				updated = (Date) values[6];
 			} else {
 				try {
 					updated = new Date((Integer) values[6] * 1000L);
 				} catch (Exception e) {
 					log.error(e.getMessage());
 					updated = new Date();
 				}
 			}
 			playerInfo.setUpdated(updated);
 		}
 		if (Events.BAN.equals(event)) {
 			Object[] data = (Object[]) values[7];
			if (log.isDebugEnabled()) log.debug(Arrays.toString(data));
 			PenaltyInfo penalty = new PenaltyInfo();
 			penalty.setType(Penalty.BAN);
 			penalty.setCreated(parseLong(data[1]));
 			penalty.setDuration(parseLong(data[2]));
 			penalty.setReason((String) data[3]);
 			penalty.setAdmin((String) data[4]);
 			penalty.setAdminId(smartCast(data[5]));
 			playerInfo.setPenaltyInfo(penalty);
 		} else if (Events.ADDNOTE.equals(event)) {
 			Object[] data = (Object[]) values[7];
			if (log.isDebugEnabled()) log.debug(Arrays.toString(data));
 			PenaltyInfo penalty = new PenaltyInfo();
 			penalty.setType(Penalty.NOTICE);
 			penalty.setCreated(parseLong(data[0]));
 			penalty.setReason((String) data[1]);
 			penalty.setAdmin((String) data[2]);
 			penalty.setAdminId(smartCast(data[3]));
 			playerInfo.setPenaltyInfo(penalty);			
 		}
 		return playerInfo;
 	}
 
 	/**
 	 * @param object
 	 * @return
 	 */
 	private String smartCast(Object obj) {
 		if (obj instanceof Number) {
 			return ((Number) obj).toString();
 		} else if (obj instanceof String) {
 			return (String) obj;
 		}
 		return null;
 	}
 
 	private Long parseLong(Object s) {
 		try {
 			if (s instanceof String) {
 				return Long.parseLong((String) s);
 			} else if (s instanceof Number) {
 				return ((Number) s).longValue();
 			}
 			return null;
 		} catch (NumberFormatException e) {
 			log.trace(e.getMessage());
 			return null;
 		}
 	}
 }
