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
 package iddb.runtime.db.model.dao.impl.mysql;
 
 import iddb.core.model.UserServer;
 import iddb.core.model.dao.UserServerDAO;
 import iddb.exception.EntityDoesNotExistsException;
 import iddb.runtime.db.ConnectionFactory;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class UserServerDAOImpl implements UserServerDAO {
 
 	private static Logger logger = LoggerFactory.getLogger(UserServerDAOImpl.class);
 	
 	/* (non-Javadoc)
 	 * @see iddb.core.model.dao.UserServerDAO#save(iddb.core.model.UserServer)
 	 */
 	@Override
 	public void save(UserServer userServer) {
 		String sql;
 		if (userServer.getKey() == null) {
 			sql = "insert into userserver (userid, serverid, playerid, owner, updated, created) values (?,?,?,?,?,?)"; 
 		} else {
			sql = "update userserver set userid = ?," +
 					"serverid = ?," +
 					"playerid = ?," +
 					"owner = ?, updated = ? where id = ? limit 1";
 		}
 		Connection conn = null;
 		try {
 			conn = ConnectionFactory.getMasterConnection();
 			PreparedStatement st = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
 			st.setLong(1, userServer.getUser());
 			st.setLong(2, userServer.getServer());
 			if (userServer.getPlayer() == null) st.setNull(3, Types.INTEGER);
 			else st.setLong(3, userServer.getServer());
 			st.setBoolean(4, userServer.getOwner());
 			st.setTimestamp(5, new Timestamp(new Date().getTime()));
 			if (userServer.getKey() != null) {
 				st.setLong(6, userServer.getKey());
 			} else {
 				st.setTimestamp(6, new Timestamp(new Date().getTime()));
 			}
 			st.executeUpdate();
 			if (userServer.getKey() == null) {
 				ResultSet rs = st.getGeneratedKeys();
 				if (rs != null && rs.next()) {
 					userServer.setKey(rs.getLong(1));
 				} else {
 					logger.warn("Couldn't get id for userServer {}-{}", userServer.getUser(), userServer.getServer());
 				}
 			}
 		} catch (SQLException e) {
 			logger.error("Save", e);
 		} catch (IOException e) {
 			logger.error("Save", e);
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (Exception e) {
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.core.model.dao.UserServerDAO#findByUser(java.lang.Long)
 	 */
 	@Override
 	public List<UserServer> findByUser(Long user) {
 		String sql = "select * from userserver where userid = ?";
 		List<UserServer> list = new ArrayList<UserServer>();
 		Connection conn = null;
 		try {
 			conn = ConnectionFactory.getSecondaryConnection();
 			PreparedStatement st = conn.prepareStatement(sql);
 			st.setLong(1, user);
 			ResultSet rs = st.executeQuery();
 			while (rs.next()) {
 				UserServer userServer = new UserServer();
 				loadUserServer(userServer, rs);
 				list.add(userServer);
 			}
 		} catch (SQLException e) {
 			logger.error("findByUser", e);
 		} catch (IOException e) {
 			logger.error("findByUser", e);
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (Exception e) {
 			}
 		}
 		return list;
 	}
 
 	/**
 	 * @param userServer
 	 * @param rs
 	 * @throws SQLException 
 	 */
 	private void loadUserServer(UserServer userServer, ResultSet rs) throws SQLException {
 		userServer.setUser(rs.getLong("userid"));
 		userServer.setServer(rs.getLong("serverid"));
 		userServer.setOwner(rs.getBoolean("owner"));
 		userServer.setKey(rs.getLong("id"));
 		userServer.setPlayer(rs.getLong("playerid"));
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.core.model.dao.UserServerDAO#findByServer(java.lang.Long)
 	 */
 	@Override
 	public List<UserServer> findByServer(Long server) {
 		String sql = "select * from userserver where serverid = ?";
 		List<UserServer> list = new ArrayList<UserServer>();
 		Connection conn = null;
 		try {
 			conn = ConnectionFactory.getSecondaryConnection();
 			PreparedStatement st = conn.prepareStatement(sql);
 			st.setLong(1, server);
 			ResultSet rs = st.executeQuery();
 			while (rs.next()) {
 				UserServer userServer = new UserServer();
 				loadUserServer(userServer, rs);
 				list.add(userServer);
 			}
 		} catch (SQLException e) {
 			logger.error("findByServer", e);
 		} catch (IOException e) {
 			logger.error("findByServer", e);
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (Exception e) {
 			}
 		}
 		return list;
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.core.model.dao.UserServerDAO#get(java.lang.Long)
 	 */
 	@Override
 	public UserServer get(Long key) throws EntityDoesNotExistsException {
 		String sql = "select * from userserver where id = ? limit 1";
 		UserServer userServer = null;
 		Connection conn = null;
 		try {
 			conn = ConnectionFactory.getSecondaryConnection();
 			PreparedStatement st = conn.prepareStatement(sql);
 			st.setLong(1, key);
 			ResultSet rs = st.executeQuery();
 			if (rs.next()) {
 				userServer = new UserServer();
 				loadUserServer(userServer, rs);
 			} else {
 				throw new EntityDoesNotExistsException("UserServer with id %s was not found", key);
 			}
 		} catch (SQLException e) {
 			logger.error("get", e);
 		} catch (IOException e) {
 			logger.error("get", e);
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (Exception e) {
 			}
 		}
 		return userServer;
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.core.model.dao.UserServerDAO#findByUserAndServer(java.lang.Long, java.lang.Long)
 	 */
 	@Override
 	public UserServer findByUserAndServer(Long user, Long server)
 			throws EntityDoesNotExistsException {
 		String sql = "select * from userserver where userid = ? and serverid = ? limit 1";
 		UserServer userServer = null;
 		Connection conn = null;
 		try {
 			conn = ConnectionFactory.getSecondaryConnection();
 			PreparedStatement st = conn.prepareStatement(sql);
 			st.setLong(1, user);
 			st.setLong(2, server);
 			ResultSet rs = st.executeQuery();
 			if (rs.next()) {
 				userServer = new UserServer();
 				loadUserServer(userServer, rs);
 			} else {
 				throw new EntityDoesNotExistsException("UserServer with for user %s and server %s was not found", user, server);
 			}
 		} catch (SQLException e) {
 			logger.error("findByUserAndServer", e);
 		} catch (IOException e) {
 			logger.error("findByUserAndServer", e);
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (Exception e) {
 			}
 		}
 		return userServer;
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.core.model.dao.UserServerDAO#findByPlayerAndServer(java.lang.Long, java.lang.Long)
 	 */
 	@Override
 	public UserServer findByPlayerAndServer(Long player, Long server)
 			throws EntityDoesNotExistsException {
 		String sql = "select * from userserver where playerid = ? and serverid = ? limit 1";
 		UserServer userServer = null;
 		Connection conn = null;
 		try {
 			conn = ConnectionFactory.getSecondaryConnection();
 			PreparedStatement st = conn.prepareStatement(sql);
 			st.setLong(1, player);
 			st.setLong(2, server);
 			ResultSet rs = st.executeQuery();
 			if (rs.next()) {
 				userServer = new UserServer();
 				loadUserServer(userServer, rs);
 			} else {
 				throw new EntityDoesNotExistsException("UserServer with for player %s and server %s was not found", player, server);
 			}
 		} catch (SQLException e) {
 			logger.error("findByPlayerAndServer", e);
 		} catch (IOException e) {
 			logger.error("findByPlayerAndServer", e);
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (Exception e) {
 			}
 		}
 		return userServer;
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.core.model.dao.UserServerDAO#existsAny(java.lang.Long, java.lang.Integer)
 	 */
 	@Override
 	public Boolean existsAny(Long user, Integer level) {
 		String sql = "SELECT * FROM userserver u INNER JOIN player p ON u.playerid = p.id WHERE u.userid = ? AND p.level >= ? limit 1";
 		Boolean res = Boolean.FALSE;
 		Connection conn = null;
 		try {
 			conn = ConnectionFactory.getSecondaryConnection();
 			PreparedStatement st = conn.prepareStatement(sql);
 			st.setLong(1, user);
 			st.setInt(2, level);
 			ResultSet rs = st.executeQuery();
 			if (rs.next()) {
 				res = Boolean.TRUE;
 			}
 		} catch (SQLException e) {
 			logger.error("existsAny", e);
 		} catch (IOException e) {
 			logger.error("existsAny", e);
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (Exception e) {
 			}
 		}
 		return res;
 	}
 
 	@Override
 	public List<UserServer> listUserServers(Long user, Integer level) {
 		String sql = "SELECT * FROM userserver u INNER JOIN player p ON u.playerid = p.id WHERE u.userid = ? AND p.level >= ?";
 		List<UserServer> list = new ArrayList<UserServer>();
 		Connection conn = null;
 		try {
 			conn = ConnectionFactory.getSecondaryConnection();
 			PreparedStatement st = conn.prepareStatement(sql);
 			st.setLong(1, user);
 			st.setInt(2, level);
 			ResultSet rs = st.executeQuery();
 			if (rs.next()) {
 				UserServer userServer = new UserServer();
 				loadUserServer(userServer, rs);
 				list.add(userServer);
 			}
 		} catch (SQLException e) {
 			logger.error("listUserServers", e);
 		} catch (IOException e) {
 			logger.error("listUserServers", e);
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (Exception e) {
 			}
 		}
 		return list;
 	}
 	
 	@Override
 	public List<UserServer> findServerAdmins(Long server, Integer level) {
 		String sql = "SELECT * FROM userserver u INNER JOIN player p ON u.playerid = p.id WHERE u.serverid = ? AND p.level >= ?";
 		List<UserServer> list = new ArrayList<UserServer>();
 		Connection conn = null;
 		try {
 			conn = ConnectionFactory.getSecondaryConnection();
 			PreparedStatement st = conn.prepareStatement(sql);
 			st.setLong(1, server);
 			st.setInt(2, level);
 			ResultSet rs = st.executeQuery();
 			if (rs.next()) {
 				UserServer userServer = new UserServer();
 				loadUserServer(userServer, rs);
 				list.add(userServer);
 			}
 		} catch (SQLException e) {
 			logger.error("findServerAdmins", e);
 		} catch (IOException e) {
 			logger.error("findServerAdmins", e);
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (Exception e) {
 			}
 		}
 		return list;
 	}
 	
 }
