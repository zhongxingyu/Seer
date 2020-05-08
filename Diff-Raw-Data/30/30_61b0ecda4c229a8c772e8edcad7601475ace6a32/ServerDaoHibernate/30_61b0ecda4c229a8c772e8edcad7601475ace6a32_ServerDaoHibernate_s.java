 package com.zzvc.mmps.dao.hibernate;
 
 import java.util.Date;
 import java.util.List;
 
 import org.appfuse.dao.hibernate.GenericDaoHibernate;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.stereotype.Repository;
 
 import com.zzvc.mmps.dao.ServerDao;
 import com.zzvc.mmps.model.Server;
 
 @Repository("serverDao")
 public class ServerDaoHibernate extends GenericDaoHibernate<Server, Long> implements ServerDao {
 
 	public ServerDaoHibernate() {
 		super(Server.class);
 	}
 	
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<Server> findByCode(String code) {
 		return getSession().createCriteria(Server.class).add(Restrictions.eq("code", code)).list();
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<Server> findByHeartbeatBefore(Date time) {
 		return getSession().createCriteria(Server.class).add(Restrictions.or(Restrictions.isNull("lastHeartbeat"), Restrictions.le("lastHeartbeat", time))).add(Restrictions.eq("enabled", true)).list();
 	}
 
 	@Override
 	public void reportStartup(String code, String address) {
		Server server = findUniqueByCode(code);
 		if (server == null) {
 			return;
 		}
 
 		server.setStarted(true);
 		server.setStartupTime(new Date());
 		server.setLastHeartbeat(new Date());
 		server.setLastConnectAddress(address);
 		save(server);
 	}
 
 	@Override
 	public void reportHeartbeat(String code, String address) {
		Server server = findUniqueByCode(code);
 		if (server == null) {
 			return;
 		}
 
 		if (server.getLastConnectAddress() == null
 				|| server.getLastConnectAddress().equals(address)) {
 			server.setStarted(true);
 			server.setLastHeartbeat(new Date());
 			server.setLastConnectAddress(address);
 			save(server);
 		}
 	}
 
 	@Override
 	public void reportShutdown(String code, String address) {
		Server server = findUniqueByCode(code);
 
 		if (server.getLastConnectAddress() == null
 				|| server.getLastConnectAddress().equals(address)) {
 			server.setStarted(false);
 			server.setShutdownTime(new Date());
 			server.setLastConnectAddress(address);
 			save(server);
 		}
 	}
 	
	private Server findUniqueByCode(String code) {
 		List<Server> servers = findByCode(code);
		if (!servers.isEmpty()) {
			return servers.get(0);
 		}
 		
 		Server server = new Server();
 		server.setCode(code);
 		server.setEnabled(false);
 		return server;
 	}

 }
