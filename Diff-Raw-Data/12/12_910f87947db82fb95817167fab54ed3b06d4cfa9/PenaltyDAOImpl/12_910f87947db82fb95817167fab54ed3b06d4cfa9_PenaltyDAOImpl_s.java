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
 
 import iddb.core.model.Penalty;
 import iddb.core.model.dao.PenaltyDAO;
 import iddb.exception.EntityDoesNotExistsException;
 import iddb.runtime.db.ConnectionFactory;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.lang.time.DateUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class PenaltyDAOImpl implements PenaltyDAO {
 
 	private static Logger logger = LoggerFactory.getLogger(PenaltyDAOImpl.class);
 	
 	/* (non-Javadoc)
 	 * @see jipdbs.dao.PenaltyDAO#save(jipdbs.model.entity.Penalty)
 	 */
 	@Override
 	public void save(Penalty penalty) {
 		String sql;
 		if (penalty.getKey() == null) {
 			sql = "insert into penalty (playerid, adminid, type, reason, duration, synced, active, created, updated, expires) values (?,?,?,?,?,?,?,?,?,?)"; 
 		} else {
 			sql = "update penalty set playerid = ?," +
 					"adminid = ?," +
 					"type = ?," +
 					"reason = ?," +
 					"duration = ?," +
 					"synced = ?," +
 					"active = ?," +
 					"created = ?," +
 					"updated = ?," +
 					"expires = ? where id = ? limit 1";
 		}
 		Connection conn = null;
 		try {
 			conn = ConnectionFactory.getMasterConnection();
 			PreparedStatement st = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
 			st.setLong(1, penalty.getPlayer());
 			if (penalty.getAdmin() != null) st.setLong(2, penalty.getAdmin());
 			else st.setNull(2, Types.INTEGER);
 			st.setInt(3, penalty.getType().intValue());
 			if (penalty.getReason() != null ) st.setString(4, penalty.getReason());
 			else st.setNull(4, Types.VARCHAR);
 			if (penalty.getDuration() == null) penalty.setDuration(0L);
 			st.setLong(5, penalty.getDuration());
 			st.setBoolean(6, penalty.getSynced());
 			st.setBoolean(7, penalty.getActive());
 			if (penalty.getCreated() == null) penalty.setCreated(new Date());
 			if (penalty.getUpdated() == null) penalty.setUpdated(new Date());
 			st.setTimestamp(8, new java.sql.Timestamp(penalty.getCreated().getTime()));
 			st.setTimestamp(9, new java.sql.Timestamp(penalty.getUpdated().getTime()));
 			st.setTimestamp(10, new java.sql.Timestamp(DateUtils.addMinutes(penalty.getCreated(), penalty.getDuration().intValue()).getTime()));
 			if (penalty.getKey() != null) st.setLong(11, penalty.getKey());
 			st.executeUpdate();
 			if (penalty.getKey() == null) {
 				ResultSet rs = st.getGeneratedKeys();
 				if (rs != null && rs.next()) {
 					penalty.setKey(rs.getLong(1));
 				} else {
 					logger.warn("Couldn't get id for penalty player id {}", penalty.getPlayer());
 				}
 			}
 		} catch (SQLException e) {
 			logger.error("Save: {}", e);
 		} catch (IOException e) {
 			logger.error("Save: {}", e);
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (Exception e) {
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see jipdbs.dao.PenaltyDAO#delete(jipdbs.model.entity.Penalty)
 	 */
 	@Override
 	public void delete(Penalty penalty) {
 		String sql = "delete from penalty where id = ?";
 		Connection conn = null;
 		try {
 			conn = ConnectionFactory.getMasterConnection();
 			PreparedStatement st = conn.prepareStatement(sql);
 			st.setLong(1, penalty.getKey());
 			int r = st.executeUpdate();
 			logger.debug("{} penalty removed", r);
 		} catch (SQLException e) {
 			logger.error("delete: {}", e);
 		} catch (IOException e) {
 			logger.error("delete: {}", e);
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (Exception e) {
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see jipdbs.dao.PenaltyDAO#get(java.lang.Long)
 	 */
 	@Override
 	public Penalty get(Long key) throws EntityDoesNotExistsException {
 		String sql = "select * from penalty where id = ?";
 		Connection conn = null;
 		Penalty penalty = null;
 		try {
 			conn = ConnectionFactory.getSecondaryConnection();
 			PreparedStatement st = conn.prepareStatement(sql);
 			st.setLong(1, key);
 			ResultSet rs = st.executeQuery();
 			if (rs.next()) {
 				penalty = new Penalty();
 				loadPenalty(penalty, rs);
 			} else {
 				throw new EntityDoesNotExistsException("Penalty with id %s was not found", key.toString());
 			}
 		} catch (SQLException e) {
 			logger.error("get: {}", e);
 		} catch (IOException e) {
 			logger.error("get: {}", e);
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (Exception e) {
 			}
 		}
 		return penalty;
 	}
 
 	/**
 	 * @param penalty
 	 * @param rs
 	 * @throws SQLException 
 	 */
 	private void loadPenalty(Penalty penalty, ResultSet rs) throws SQLException {
 		penalty.setKey(rs.getLong("id"));
 		penalty.setPlayer(rs.getLong("playerid"));
 		penalty.setAdmin(rs.getLong("adminid"));
 		penalty.setType(rs.getInt("type"));
 		penalty.setReason(rs.getString("reason"));
 		penalty.setDuration(rs.getLong("duration"));
 		penalty.setSynced(rs.getBoolean("synced"));
 		penalty.setActive(rs.getBoolean("active"));
 		penalty.setCreated(rs.getTimestamp("created"));
 		penalty.setUpdated(rs.getTimestamp("updated"));
 	}
 
 	/* (non-Javadoc)
 	 * @see jipdbs.dao.PenaltyDAO#findByPlayer(java.lang.Long)
 	 */
 	@Override
 	public List<Penalty> findByPlayer(Long player) {
 		String sql = "select * from penalty where playerid = ?";
 		Connection conn = null;
 		List<Penalty> list = new ArrayList<Penalty>();
 		try {
 			conn = ConnectionFactory.getSecondaryConnection();
 			PreparedStatement st = conn.prepareStatement(sql);
 			st.setLong(1, player);
 			ResultSet rs = st.executeQuery();
			if (rs.next()) {
 				Penalty penalty = new Penalty();
 				loadPenalty(penalty, rs);
 				list.add(penalty);
 			}
 		} catch (SQLException e) {
 			logger.error("findByPlayer: {}", e);
 		} catch (IOException e) {
 			logger.error("findByPlayer: {}", e);
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (Exception e) {
 			}
 		}
 		return list;
 	}
 
 	/* (non-Javadoc)
 	 * @see jipdbs.dao.PenaltyDAO#findByPlayer(java.lang.Long, int)
 	 */
 	@Override
 	public List<Penalty> findByPlayer(Long player, int limit) {
 		String sql = "select * from penalty where playerid = ? limit ?";
 		List<Penalty> list = new ArrayList<Penalty>();
 		Connection conn = null;
 		try {
 			conn = ConnectionFactory.getSecondaryConnection();
 			PreparedStatement st = conn.prepareStatement(sql);
 			st.setLong(1, player);
 			st.setInt(2, limit);
 			ResultSet rs = st.executeQuery();
			if (rs.next()) {
 				Penalty penalty = new Penalty();
 				loadPenalty(penalty, rs);
 				list.add(penalty);
 			}
 		} catch (SQLException e) {
 			logger.error("findByPlayer: {}", e);
 		} catch (IOException e) {
 			logger.error("findByPlayer: {}", e);
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (Exception e) {
 			}
 		}
 		return list;
 	}
 
 	/* (non-Javadoc)
 	 * @see jipdbs.dao.PenaltyDAO#findByType(java.lang.Long, int, int, int[])
 	 */
 	@Override
 	public List<Penalty> findByType(Long type, int offset, int limit,
 			int[] count) {
 		String sqlCount = "select count(id) from penalty where type = ?";
 		String sql = "select * from penalty where type = ? order by created desc limit ?,?";
 		Connection conn = null;
 		List<Penalty> list = new ArrayList<Penalty>();
 		try {
 			conn = ConnectionFactory.getSecondaryConnection();
 			PreparedStatement stC = conn.prepareStatement(sqlCount);
 			stC.setInt(1, type.intValue());
 			ResultSet rsC = stC.executeQuery(sqlCount);
 			if (rsC.next()) {
 				count[0] = rsC.getInt(1);
 			}
 			PreparedStatement st = conn.prepareStatement(sql);
 			stC.setInt(1, type.intValue());
 			st.setInt(2, offset);
 			st.setInt(3, limit);
 			ResultSet rs = st.executeQuery();
 			while (rs.next()) {
 				Penalty penalty = new Penalty();
 				loadPenalty(penalty, rs);
 				list.add(penalty);
 			}
 		} catch (SQLException e) {
 			logger.error("findByType: {}", e);
 		} catch (IOException e) {
 			logger.error("findByType: {}", e);
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (Exception e) {
 			}
 		}
 		return list;
 	}
 
 	/* (non-Javadoc)
 	 * @see jipdbs.dao.PenaltyDAO#findByPlayerAndTypeAndActive(java.lang.Long, java.lang.Long)
 	 */
 	@Override
 	public List<Penalty> findByPlayerAndTypeAndActive(Long player, Integer type) {
 		String sql = "select * from penalty where playerid = ? and type = ? and active = ? order by created desc";
 		Connection conn = null;
 		List<Penalty> list = new ArrayList<Penalty>();
 		try {
 			conn = ConnectionFactory.getSecondaryConnection();
 			PreparedStatement st = conn.prepareStatement(sql);
 			st.setLong(1, player);
 			st.setInt(2, type.intValue());
 			st.setBoolean(3, true);
 			ResultSet rs = st.executeQuery();
 			while (rs.next()) {
 				Penalty penalty = new Penalty();
 				loadPenalty(penalty, rs);
 				list.add(penalty);
 			}
 		} catch (SQLException e) {
 			logger.error("findByPlayerAndTypeAndActive: {}", e);
 		} catch (IOException e) {
 			logger.error("findByPlayerAndTypeAndActive: {}", e);
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (Exception e) {
 			}
 		}
 		return list;
 	}
 
 	/* (non-Javadoc)
 	 * @see jipdbs.dao.PenaltyDAO#findByPlayerAndType(java.lang.Long, java.lang.Long, int, int, int[])
 	 */
 	@Override
 	public List<Penalty> findByPlayerAndType(Long player, Integer type, int offset, int limit, int[] count) {
 		String sqlCount = "select count(id) from penalty where playerid = ? and type = ?";
 		String sql = "select * from penalty where playerid = ? and type = ? order by created desc limit ?,?";
 		Connection conn = null;
 		List<Penalty> list = new ArrayList<Penalty>();
 		try {
 			conn = ConnectionFactory.getSecondaryConnection();
 			PreparedStatement stC = conn.prepareStatement(sqlCount);
 			stC.setLong(1, player);
 			stC.setInt(2, type.intValue());
 			ResultSet rsC = stC.executeQuery(sqlCount);
 			if (rsC.next()) {
 				count[0] = rsC.getInt(1);
 			}
 			PreparedStatement st = conn.prepareStatement(sql);
 			st.setLong(1, player);
 			st.setInt(2, type.intValue());
 			st.setInt(3, offset);
 			st.setInt(4, limit);
 			ResultSet rs = st.executeQuery();
 			while (rs.next()) {
 				Penalty penalty = new Penalty();
 				loadPenalty(penalty, rs);
 				list.add(penalty);
 			}
 		} catch (SQLException e) {
 			logger.error("findByPlayerAndType: {}", e);
 		} catch (IOException e) {
 			logger.error("findByPlayerAndType: {}", e);
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (Exception e) {
 			}
 		}
 		return list;
 	}
 
 	/* (non-Javadoc)
 	 * @see jipdbs.dao.PenaltyDAO#save(java.util.List)
 	 */
 	@Override
 	public void save(List<Penalty> list) {
 		for (Penalty p : list) {
 			this.save(p);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see jipdbs.dao.PenaltyDAO#delete(java.util.List)
 	 */
 	@Override
 	public void delete(List<Penalty> list) {
 		String sql = "delete from penalty where id = ?";
 		Connection conn = null;
 		try {
 			conn = ConnectionFactory.getMasterConnection();
 			PreparedStatement st = conn.prepareStatement(sql);
 			for (Penalty p : list) {
 				st.setLong(1, p.getKey());
 				st.addBatch();
 			}
 			st.executeBatch();
 		} catch (SQLException e) {
 			logger.error("delete: {}", e);
 		} catch (IOException e) {
 			logger.error("delete: {}", e);
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (Exception e) {
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see jipdbs.dao.PenaltyDAO#findByPlayerAndType(java.lang.Long, java.lang.Long)
 	 */
 	@Override
 	public List<Penalty> findByPlayerAndType(Long player, Integer type) {
 		String sql = "select * from penalty where playerid = ? and type = ? order by created desc";
 		Connection conn = null;
 		List<Penalty> list = new ArrayList<Penalty>();
 		try {
 			conn = ConnectionFactory.getSecondaryConnection();
 			PreparedStatement st = conn.prepareStatement(sql);
 			st.setLong(1, player);
 			st.setInt(2, type.intValue());
 			ResultSet rs = st.executeQuery();
 			while (rs.next()) {
 				Penalty penalty = new Penalty();
 				loadPenalty(penalty, rs);
 				list.add(penalty);
 			}
 		} catch (SQLException e) {
 			logger.error("findByPlayerAndType: {}", e);
 		} catch (IOException e) {
 			logger.error("findByPlayerAndType: {}", e);			
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (Exception e) {
 			}
 		}
 		return list;
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.core.model.dao.PenaltyDAO#disable(java.util.List)
 	 */
 	@Override
 	public void disable(List<Penalty> list) {
 		String sql = "update penalty set active = ? where id = ?";
 		Connection conn = null;
 		try {
 			conn = ConnectionFactory.getMasterConnection();
 			PreparedStatement st = conn.prepareStatement(sql);
 			for (Penalty p : list) {
 				st.setBoolean(1, false);
 				st.setLong(2, p.getKey());
 				st.addBatch();
 			}
 			st.executeBatch();
 		} catch (SQLException e) {
 			logger.error("disable: {}", e);
 		} catch (IOException e) {
 			logger.error("disable: {}", e);
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (Exception e) {
 			}
 		}		
 	}
 	
 	/* (non-Javadoc)
 	 * @see iddb.core.model.dao.PenaltyDAO#findLastActivePenalty(java.lang.Long, java.lang.Long)
 	 */
 	@Override
 	public Penalty findLastActivePenalty(Long player, Integer type) {
 		String sql = "select * from penalty where playerid = ? and type = ? and active = ? order by updated desc limit 1";
 		Connection conn = null;
 		Penalty penalty = null;
 		try {
 			conn = ConnectionFactory.getSecondaryConnection();
 			PreparedStatement st = conn.prepareStatement(sql);
 			st.setLong(1, player);
 			st.setInt(2, type.intValue());
 			st.setBoolean(3, true);
 			ResultSet rs = st.executeQuery();
 			if (rs.next()) {
 				penalty = new Penalty();
 				loadPenalty(penalty, rs);
 			}
 		} catch (SQLException e) {
 			logger.error("findByPlayerAndTypeAndActive: {}", e);
 		} catch (IOException e) {
 			logger.error("findByPlayerAndTypeAndActive: {}", e);
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (Exception e) {
 			}
 		}
 		return penalty;
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.core.model.dao.PenaltyDAO#findExpired()
 	 */
 	@Override
 	public List<Penalty> findExpired() {
 		String sql = "select * from penalty where type = ? and duration > 0 and expires < CURRENT_TIMESTAMP and active = ?";
 		Connection conn = null;
 		List<Penalty> list = new ArrayList<Penalty>();
 		try {
 			conn = ConnectionFactory.getSecondaryConnection();
 			PreparedStatement st = conn.prepareStatement(sql);
 			st.setInt(1, Penalty.BAN);
 			st.setBoolean(2, true);
 			ResultSet rs = st.executeQuery();
 			while (rs.next()) {
 				Penalty penalty = new Penalty();
 				loadPenalty(penalty, rs);
 				list.add(penalty);
 			}
 		} catch (SQLException e) {
 			logger.error("findExpired: {}", e);
 		} catch (IOException e) {
 			logger.error("findExpired: {}", e);			
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (Exception e) {
 			}
 		}
 		return list;
 	}
 
 	@Override
 	public void deletePlayerPenalty(Long player, Integer type) {
 		String sql = "delete from penalty where playerid = ? and type = ?";
 		Connection conn = null;
 		try {
 			conn = ConnectionFactory.getMasterConnection();
 			PreparedStatement st = conn.prepareStatement(sql);
 			st.setLong(1, player);
 			st.setInt(2, type);
 			int r = st.executeUpdate();
 			logger.debug("{} penalties removed", r);
 		} catch (SQLException e) {
 			logger.error("deletePlayerPenalty: {}", e);
 		} catch (IOException e) {
 			logger.error("deletePlayerPenalty: {}", e);
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (Exception e) {
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.core.model.dao.PenaltyDAO#findPendingPenalties(java.lang.Long)
 	 */
 	@Override
 	public List<Penalty> findPendingPenalties(Long serverId) {
 		String sql = "select p.* from penalty p, player pa, penalty_history h where p.id = h.penaltyid and p.playerid = pa.id and p.synced = 0 and h.status = 0 and pa.serverid = ?";
 		Connection conn = null;
 		List<Penalty> list = new ArrayList<Penalty>();
 		try {
 			conn = ConnectionFactory.getSecondaryConnection();
 			PreparedStatement st = conn.prepareStatement(sql);
 			st.setLong(1, serverId);
 			ResultSet rs = st.executeQuery();
 			while (rs.next()) {
 				Penalty penalty = new Penalty();
 				loadPenalty(penalty, rs);
 				list.add(penalty);
 			}
 		} catch (SQLException e) {
 			logger.error("findPendingPenalties: {}", e);
 		} catch (IOException e) {
 			logger.error("findPendingPenalties: {}", e);			
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (Exception e) {
 			}
 		}
 		return list;
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.core.model.dao.PenaltyDAO#resetLongPendingPenalties(java.lang.Long, java.lang.Integer)
 	 */
 	@Override
 	public void resetLongPendingPenalties(Long serverId, Integer days) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
