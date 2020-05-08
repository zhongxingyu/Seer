 package edu.psu.sweng.ff.dao;
 
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.psu.sweng.ff.common.Player;
 import edu.psu.sweng.ff.common.PlayerSource;
 
 public class PlayerDAO extends BaseDAO implements PlayerSource {
 
 	private final static String CHECK = "SELECT playerid FROM ff_players WHERE playerid = ?";
 	
 	private final static String STORE = "INSERT INTO ff_players (playerid, firstname, " +
 		"lastname, birthdate, height, weight, college, nflteam, " +
 		"position, jerseynumber) VALUES (?,?,?,?,?,?,?,?,?,?)";
 
 	private final static String UPDATE = "UPDATE ff_players SET firstname = ?, " +
 		"lastname = ?, birthdate = ?, height = ?, weight = ?, college = ?, nflteam = ?, " +
 		"position = ?, jerseynumber = ? WHERE playerid = ?";
 	
 	private final static String REMOVE = "DELETE FROM ff_players WHERE playerid = ?";
 
 	private final static String LOAD_BY_ID = "SELECT firstname, lastname, " +
 		"birthdate, height, weight, college, nflteam, position, jerseynumber " +
 		"FROM ff_players WHERE playerid = ?";
 
 	private final static String LOAD_BY_ID_WITH_POINTS = "SELECT firstname, lastname, " +
		"birthdate, height, weight, college, nflteam, position, jerseynumber, points " +
		"FROM ff_players, ff_playerpoints WHERE ff_players.playerid = ff_playerpoints.player_id " +
		"AND ff_players.playerid = ? AND ff_playerpoints.week = ?";
 
 	private final static String LOAD_BY_TYPE = "SELECT playerid, firstname, lastname, " +
 		"birthdate, height, weight, college, nflteam, position, jerseynumber " +
 		"FROM ff_players WHERE position = ?";
 
 	private final static String LOAD_BY_TYPES = "SELECT playerid, firstname, lastname, " +
 		"birthdate, height, weight, college, nflteam, position, jerseynumber " +
 		"FROM ff_players WHERE position IN ";
 
 	private final static String RESTRICT = " AND playerid NOT IN (SELECT player_id " +
 		"FROM ff_roster_player, ff_teams WHERE ff_teams.league_id = ? AND " +
 		"ff_roster_player.team_id = ff_teams.id)";
 	
 	
 	public List<Player> getByType(String type) {
 		return this.getByType(-1, type);
 	}
 	
 	public List<Player> getByType(String... types) {
 		return this.getByType(-1, types);
 	}
 	
 	public List<Player> getByType(int leagueId, String type) {
 
 		List<Player> lp = new ArrayList<Player>();
 
 		DatabaseConnectionManager dbcm = new DatabaseConnectionManager();
 		Connection conn = dbcm.getConnection();
 
 		PreparedStatement stmt1 = null;
 		ResultSet rs = null;
 		
 		try {
 
 			if (leagueId != -1) {
 				stmt1 = conn.prepareStatement(LOAD_BY_TYPE + RESTRICT);
 				stmt1.setString(1, type);
 				stmt1.setInt(2, leagueId);
 			} else {
 				stmt1 = conn.prepareStatement(LOAD_BY_TYPE);
 				stmt1.setString(1, type);
 			}
 			
 			rs = stmt1.executeQuery();
 			
 			while (rs.next()) {
 				
 				Player p = new Player();
 				p.setId(rs.getString(1));
 				p.setFirstName(rs.getString(2));
 				p.setLastName(rs.getString(3));
 				p.setBirthdate(new java.util.Date(rs.getDate(4).getTime()));
 				p.setHeight(rs.getInt(5));
 				p.setWeight(rs.getInt(6));
 				p.setCollege(rs.getString(7));
 				p.setNflTeam(rs.getString(8));
 				p.setPosition(rs.getString(9));
 				p.setJerseyNumber(rs.getInt(10));
 				lp.add(p);
 				
 			}
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			close(rs);
 			close(stmt1);
 			close(conn);
 		}
 		
 		return lp;
 
 	}
 
 	public List<Player> getByType(int leagueId, String... types) {
 
 		List<Player> lp = new ArrayList<Player>();
 
 		DatabaseConnectionManager dbcm = new DatabaseConnectionManager();
 		Connection conn = dbcm.getConnection();
 
 		PreparedStatement stmt1 = null;
 		ResultSet rs = null;
 		
 		StringBuilder typeParams = new StringBuilder("(");
 		for (int i = 0; i < types.length; i++) {
 			if (i > 0) typeParams.append(",");
 			typeParams.append("?");
 		}
 		typeParams.append(")");
 		
 		try {
 
 			if (leagueId != -1) {
 				stmt1 = conn.prepareStatement(LOAD_BY_TYPES + typeParams.toString() + RESTRICT);
 				for (int i = 0; i < types.length; i++) {
 					stmt1.setString(i+1, types[i]);
 				}
 				stmt1.setInt(types.length+1, leagueId);
 			} else {
 				stmt1 = conn.prepareStatement(LOAD_BY_TYPES + typeParams.toString());
 				for (int i = 0; i < types.length; i++) {
 					stmt1.setString(i+1, types[i]);
 				}
 			}
 			
 			rs = stmt1.executeQuery();
 			
 			while (rs.next()) {
 				
 				Player p = new Player();
 				p.setId(rs.getString(1));
 				p.setFirstName(rs.getString(2));
 				p.setLastName(rs.getString(3));
 				p.setBirthdate(new java.util.Date(rs.getDate(4).getTime()));
 				p.setHeight(rs.getInt(5));
 				p.setWeight(rs.getInt(6));
 				p.setCollege(rs.getString(7));
 				p.setNflTeam(rs.getString(8));
 				p.setPosition(rs.getString(9));
 				p.setJerseyNumber(rs.getInt(10));
 				lp.add(p);
 				
 			}
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			close(rs);
 			close(stmt1);
 			close(conn);
 		}
 		
 		
 		return lp;
 		
 	}
 
 	public Player getById(String id) {
 
 		DatabaseConnectionManager dbcm = new DatabaseConnectionManager();
 		Connection conn = dbcm.getConnection();
 		Player player = null;
 		try {
 			player = this.getById(id, -1, conn);
 		} finally {
 			close(conn);
 		}
 		return player;
 		
 	}
 	
 	public Player getById(String id, int week, Connection conn) {
 		
 		Player p = null;
 		
 		PreparedStatement stmt1 = null;
 		ResultSet rs = null;
 		
 		try {
 
 			if (week < 1) {
 				stmt1 = conn.prepareStatement(LOAD_BY_ID);
 				stmt1.setString(1, id);
 			} else {
 				stmt1 = conn.prepareStatement(LOAD_BY_ID_WITH_POINTS);
				stmt1.setString(1, id);
				stmt1.setInt(2, week);
 			}
 			
 			rs = stmt1.executeQuery();
 			
 			if (rs.next()) {
 				
 				p = new Player();
 				p.setId(id);
 				p.setFirstName(rs.getString(1));
 				p.setLastName(rs.getString(2));
 				p.setBirthdate(new java.util.Date(rs.getDate(3).getTime()));
 				p.setHeight(rs.getInt(4));
 				p.setWeight(rs.getInt(5));
 				p.setCollege(rs.getString(6));
 				p.setNflTeam(rs.getString(7));
 				p.setPosition(rs.getString(8));
 				p.setJerseyNumber(rs.getInt(9));
 				if (week > 0) {
 					p.setPoints(rs.getInt(10));
 					p.setWeek(week);
 				}
 				
 			}
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			close(rs);
 			close(stmt1);
 		}
 		
 		return p;
 		
 	}
 	
 	public boolean store(Player p) {
 
 		DatabaseConnectionManager dbcm = new DatabaseConnectionManager();
 		Connection conn = dbcm.getConnection();
 
 		PreparedStatement stmt1 = null;
 		PreparedStatement stmt2 = null;
 		ResultSet rs1 = null;
 
 		try {
 
 			stmt1 = conn.prepareStatement(CHECK);
 			stmt1.setString(1, p.getId());
 			rs1 = stmt1.executeQuery();
 			
 			if (rs1.next()) {
 				// this player is already in the table. update.
 				stmt2 = conn.prepareStatement(UPDATE);
 				stmt2.setString(1, p.getFirstName());
 				stmt2.setString(2, p.getLastName());
 				stmt2.setDate(3, new Date(p.getBirthdate().getTime()));
 				stmt2.setInt(4, p.getHeight());
 				stmt2.setInt(5, p.getWeight());
 				stmt2.setString(6, p.getCollege());
 				stmt2.setString(7, p.getNflTeam());
 				stmt2.setString(8, p.getPosition());
 				stmt2.setInt(9, p.getJerseyNumber());
 				stmt2.setString(10, p.getId());
 				stmt2.executeUpdate();
 			} else {
 				// this player is new. insert.
 				stmt2 = conn.prepareStatement(STORE);
 				stmt2.setString(1, p.getId());
 				stmt2.setString(2, p.getFirstName());
 				stmt2.setString(3, p.getLastName());
 				stmt2.setDate(4, new Date(p.getBirthdate().getTime()));
 				stmt2.setInt(5, p.getHeight());
 				stmt2.setInt(6, p.getWeight());
 				stmt2.setString(7, p.getCollege());
 				stmt2.setString(8, p.getNflTeam());
 				stmt2.setString(9, p.getPosition());
 				stmt2.setInt(10, p.getJerseyNumber());
 				stmt2.executeUpdate();
 			}
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		} finally {
 			close(rs1);
 			close(stmt1);
 			close(stmt2);
 			close(conn);
 		}
 		
 		return true;
 		
 	}
 	
 	public boolean remove(Player p) {
 
 		boolean ret = true;
 		DatabaseConnectionManager dbcm = new DatabaseConnectionManager();
 		Connection conn = dbcm.getConnection();
 
 		PreparedStatement stmt1 = null;
 		PreparedStatement stmt2 = null;
 		ResultSet rs1 = null;
 
 		try {
 
 			stmt1 = conn.prepareStatement(REMOVE);
 			stmt1.setString(1, p.getId());
 			if (stmt1.executeUpdate() != 1) {
 				ret = false;
 			}
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			ret = false;
 		} finally {
 			close(rs1);
 			close(stmt1);
 			close(stmt2);
 			close(conn);
 		}
 		
 		return ret;
 		
 	}
 
 }
