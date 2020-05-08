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
 
 import iddb.core.model.AliasIP;
 import iddb.core.model.dao.AliasIPDAO;
 import iddb.core.util.Functions;
 import iddb.runtime.db.ConnectionFactory;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 public class AliasIPDAOImpl implements AliasIPDAO {
 
 	private static Logger logger = LoggerFactory.getLogger(AliasIPDAOImpl.class);
 	
 	/* (non-Javadoc)
 	 * @see jipdbs.dao.IPAliasDAO#save(jipdbs.model.entity.IPAlias)
 	 * 
 	 */
 	@Override
 	public void save(AliasIP alias) {
 		String sql;
 		if (alias.getKey() == null) {
 			sql = "insert into aliasip (playerid, ip, created, updated, count) values (?,?,?,?,?)"; 
 		} else {
 			sql = "update aliasip set playerid = ?," +
 					"ip = ?," +
 					"created = ?," +
 					"updated = ?," +
 					"count = ? where id = ? limit 1";
 		}
 		Connection conn = null;
 		try {
 			conn = ConnectionFactory.getMasterConnection();
 			PreparedStatement st = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
 			st.setLong(1, alias.getPlayer());
 			st.setLong(2, Functions.ipToDecimal(alias.getIp()));
 			if (alias.getCreated() == null) alias.setCreated(new Date());
 			if (alias.getUpdated() == null) alias.setUpdated(new Date());			
 			st.setTimestamp(3, new java.sql.Timestamp(alias.getCreated().getTime()));
 			st.setTimestamp(4, new java.sql.Timestamp(alias.getUpdated().getTime()));
 			st.setLong(5, alias.getCount());
 			if (alias.getKey() != null) st.setLong(6, alias.getKey());
 			st.executeUpdate();
 			if (alias.getKey() == null) {
 				ResultSet rs = st.getGeneratedKeys();
 				if (rs != null && rs.next()) {
 					alias.setKey(rs.getLong(1));
 				} else {
 					logger.warn("Couldn't get id for alias ip {}", alias.getIp());
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
 	 * @see jipdbs.dao.IPAliasDAO#findByPlayerAndIp(java.lang.Long, java.lang.String)
 	 */
 	@Override
 	public AliasIP findByPlayerAndIp(Long player, String ip) {
 		String sql = "select * from aliasip where playerid = ? and ip = ?";
 		Connection conn = null;
 		AliasIP alias = null;
 		try {
 			conn = ConnectionFactory.getSecondaryConnection();
 			PreparedStatement st = conn.prepareStatement(sql);
 			st.setLong(1, player);
 			st.setLong(2, Functions.ipToDecimal(ip));
 			ResultSet rs = st.executeQuery();
 			if (rs.next()) {
 				alias = new AliasIP();
 				loadAlias(alias, rs);
 			}
 		} catch (SQLException e) {
 			logger.error("findByPlayerAndIp: {}", e);
 		} catch (IOException e) {
 			logger.error("findByPlayerAndIp: {}", e);
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (Exception e) {
 			}
 		}
 		return alias;
 	}
 
 	/**
 	 * @param alias
 	 * @param rs
 	 * @throws SQLException 
 	 */
 	private void loadAlias(AliasIP alias, ResultSet rs) throws SQLException {
 		alias.setKey(rs.getLong("id"));
 		alias.setPlayer(rs.getLong("playerid"));
 		alias.setIp(Functions.decimalToIp(rs.getLong("ip")));
 		alias.setCreated(rs.getTimestamp("created"));
 		alias.setUpdated(rs.getTimestamp("updated"));
 		alias.setCount(rs.getLong("count"));
 	}
 
 	/* (non-Javadoc)
 	 * @see jipdbs.dao.IPAliasDAO#findByPlayer(java.lang.Long, int, int, int[])
 	 */
 	@Override
 	public List<AliasIP> findByPlayer(Long player, int offset, int limit,
 			int[] count) {
 		String sqlCount = "select count(id) from aliasip where playerid = ?";
 		String sql = "select * from aliasip where playerid = ? order by updated desc limit ?,?";
 		Connection conn = null;
 		List<AliasIP> list = new ArrayList<AliasIP>();
 		try {
 			conn = ConnectionFactory.getSecondaryConnection();
 			PreparedStatement stC = conn.prepareStatement(sqlCount);
 			stC.setLong(1, player);
 			ResultSet rsC = stC.executeQuery();
 			if (rsC.next()) {
 				count[0] = rsC.getInt(1);
 			}
 			PreparedStatement st = conn.prepareStatement(sql);
 			st.setLong(1, player);
 			st.setInt(2, offset);
 			st.setInt(3, limit);
 			ResultSet rs = st.executeQuery();
 			while (rs.next()) {
 				AliasIP alias = new AliasIP();
 				loadAlias(alias, rs);
 				list.add(alias);
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
 	 * @see jipdbs.dao.IPAliasDAO#findByIP(java.lang.String, int, int, int[])
 	 */
 	@Override
 	public List<AliasIP> findByIP(String query, int offset, int limit,
 			int[] count) {
		String sqlCount = "select count(id) from aliasip where ip between ? and ? group by playerid";
 		String sql = "select * from aliasip where ip between ? and ? group by playerid order by updated desc limit ?,?";
 		Connection conn = null;
 		List<AliasIP> list = new ArrayList<AliasIP>();
 		try {
 			conn = ConnectionFactory.getSecondaryConnection();
 			Long[] range = Functions.getIpRange(query);
 			PreparedStatement stC = conn.prepareStatement(sqlCount);
 			stC.setLong(1, range[0]);
 			stC.setLong(2, range[1]);
 			ResultSet rsC = stC.executeQuery();
 			if (rsC.next()) {
 				count[0] = rsC.getInt(1);
 			}
 			PreparedStatement st = conn.prepareStatement(sql);
 			st.setLong(1, range[0]);
 			st.setLong(2, range[1]);
 			st.setInt(3, offset);
 			st.setInt(4, limit);
 			ResultSet rs = st.executeQuery();
 			while (rs.next()) {
 				AliasIP alias = new AliasIP();
 				loadAlias(alias, rs);
 				list.add(alias);
 			}
 		} catch (SQLException e) {
 			logger.error("findByIP: {}", e);
 		} catch (IOException e) {
 			logger.error("findByIP: {}", e);
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (Exception e) {
 			}
 		}
 		return list;
 	}
 
 }
