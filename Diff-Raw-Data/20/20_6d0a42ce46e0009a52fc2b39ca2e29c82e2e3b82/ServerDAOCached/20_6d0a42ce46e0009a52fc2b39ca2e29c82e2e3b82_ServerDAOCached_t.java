 /**
  *   Copyright(c) 2010-2011 CodWar Soft
  * 
  *   This file is part of IPDB UrT.
  *
  *   IPDB UrT is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This software is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this software. If not, see <http://www.gnu.org/licenses/>.
  */
 package iddb.core.model.dao.cached;
 
 import iddb.core.DAOException;
 import iddb.core.model.Server;
 import iddb.core.model.dao.ServerDAO;
 import iddb.exception.EntityDoesNotExistsException;
 
 import java.util.Date;
 import java.util.List;
 
 public class ServerDAOCached extends CachedDAO implements ServerDAO {
 
 	private final ServerDAO impl;
 
 	public ServerDAOCached(ServerDAO impl) {
 		super("server");
 		this.impl = impl;
 	}
 
 	@Override
 	public void save(Server server) throws DAOException {
        Boolean lastStatus = server.getDisabled();
 		if (server.getKey() == null) {
 			// if the server is new, we clear the cache for the listed servers
 			cacheClear();
         }
 		impl.save(server);
 		cachePut(cacheKey(server.getUid()), server);
 		cachePut("server"+server.getKey().toString(), server);
        if (!lastStatus.equals(server.getDisabled())) {
 			// if the status was updated clean the cache
 			cacheClear();        
         }
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<Server> findAll(int offset, int limit, int[] count) {
 		String key = "all" + Integer.toString(offset) + "L"
 				+ Integer.toString(limit);
 		List<Server> servers = (List<Server>) getCachedList(key, count);
 		if (servers != null)
 			return servers;
 		servers = impl.findAll(offset, limit, count);
 		putCachedList(key, servers, count);
 		return servers;
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<Server> findEnabled(int offset, int limit, int[] count) {
 		String key = "enabled" + Integer.toString(offset) + "L"
 				+ Integer.toString(limit);
 		List<Server> servers = (List<Server>) getCachedList(key, count);
 		if (servers != null)
 			return servers;
 		servers = impl.findEnabled(offset, limit, count);
 		putCachedList(key, servers, count);
 		return servers;
 	}
 	
 	@Override
 	public Server findByUid(String uid) {
 
 		Server server = (Server) cacheGet(cacheKey(uid));
 
 		if (server != null)
 			return server;
 
 		server = impl.findByUid(uid);
 
 		if (server != null)
 			cachePut(cacheKey(server.getUid()), server);
 
 		return server;
 	}
 
 	@Override
 	public Server get(Long key) throws EntityDoesNotExistsException, DAOException {
 		Server s = (Server) cacheGet("server"+key.toString());
 		if (s != null) return s;
 		s = impl.get(key);
 		if (s != null) cachePut("server"+key.toString(), s);
 		return s;
 	}
 
 	private String cacheKey(String uid) {
 		return "key-" + uid;
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.core.model.dao.ServerDAO#listNotUpdatedSince(java.util.Date)
 	 */
 	@Override
 	public List<Server> listNotUpdatedSince(Date date) {
 		return impl.listNotUpdatedSince(date);
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.core.model.dao.ServerDAO#savePermissions(iddb.core.model.Server)
 	 */
 	@Override
 	public void savePermissions(Server server) throws DAOException {
 		this.impl.savePermissions(server);
 		cachePut("sp+" + server.getKey().toString(), server);
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.core.model.dao.ServerDAO#get(java.lang.Long, boolean)
 	 */
 	@Override
 	public Server get(Long key, boolean fetchPermissions)
 			throws EntityDoesNotExistsException, DAOException {
 		if (fetchPermissions) {
 			Server server = (Server) cacheGet("sp+" + key.toString());
 			if (server == null) {
 				server = impl.get(key, fetchPermissions);
 				if (server != null) cachePut("sp+" + key.toString(), server);
 			}
 			return server;
 		} else {
 			return get(key);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.core.model.dao.ServerDAO#saveBanPermissions(iddb.core.model.Server)
 	 */
 	@Override
 	public void saveBanPermissions(Server server) throws DAOException {
 		this.impl.saveBanPermissions(server);
 		cachePut("sp+" + server.getKey().toString(), server);
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.core.model.dao.ServerDAO#loadPermissions(iddb.core.model.Server)
 	 */
 	@Override
 	public void loadPermissions(Server server) throws DAOException {
 		this.impl.loadPermissions(server);
 	}
 }
