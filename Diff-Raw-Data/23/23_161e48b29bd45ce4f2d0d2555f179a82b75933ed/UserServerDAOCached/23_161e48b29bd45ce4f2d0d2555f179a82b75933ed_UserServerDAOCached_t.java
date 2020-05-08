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
 
 import iddb.core.model.UserServer;
 import iddb.core.model.dao.UserServerDAO;
 import iddb.exception.EntityDoesNotExistsException;
 
 import java.util.List;
 
 
 public class UserServerDAOCached extends CachedDAO implements UserServerDAO {
 
 	private UserServerDAO impl;
 	
 	public UserServerDAOCached(UserServerDAO impl) {
 		this.impl = impl;
 		initializeCache();
 	}
 	
 	@Override
 	public void save(UserServer userServer) {
 		this.impl.save(userServer);
 	}
 
 	@Override
 	public List<UserServer> findByUser(Long user) {
 		return this.impl.findByUser(user);
 	}
 
 	@Override
 	public UserServer get(Long userServer) throws EntityDoesNotExistsException {
 		return this.impl.get(userServer);
 	}
 
 	@Override
 	public UserServer findByUserAndServer(Long user, Long server) throws EntityDoesNotExistsException {
 		UserServer userServer = (UserServer) cacheGet("u-" + user.toString() + "s" + server.toString());
 		if (userServer != null) return userServer;
 		userServer = this.impl.findByUserAndServer(user, server);
 		cachePut("u-" + user.toString() + "s" + server.toString(), userServer);
 		return userServer;
 	}
 
 	@Override
 	public List<UserServer> findByServer(Long server) {
 		return this.impl.findByServer(server);
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.core.model.dao.cached.CachedDAO#initializeCache()
 	 */
 	@Override
 	protected void initializeCache() {
 		createCache("userserver");
 	}
 
	/* (non-Javadoc)
	 * @see iddb.core.model.dao.UserServerDAO#findByPlayerAndServer(java.lang.Long, java.lang.Long)
	 */
	@Override
	public UserServer findByPlayerAndServer(Long player, Long server)
			throws EntityDoesNotExistsException {
		UserServer userServer = (UserServer) cacheGet("p-" + player.toString() + "s" + server.toString());
		if (userServer != null) return userServer;
		userServer = this.impl.findByPlayerAndServer(player, server);
		cachePut("p-" + player.toString() + "s" + server.toString(), userServer);
		return userServer;
	}

 }
