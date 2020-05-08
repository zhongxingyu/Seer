 package edu.psu.sweng.ff.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import edu.psu.sweng.ff.common.DatabaseException;
 import edu.psu.sweng.ff.common.Player;
 import edu.psu.sweng.ff.common.Roster;
 import edu.psu.sweng.ff.common.RosterStore;
 import edu.psu.sweng.ff.common.Team;
 
 public class RosterDAO extends BaseDAO implements RosterStore {
 
 	private final static String SELECT_BY_TEAM = "SELECT " +
 		"starter, player_id FROM ff_roster_player WHERE team_id = ? and week = ?";
 	
 	private final static String STORE = "INSERT INTO ff_rosters (team_id, " +
 		"week) VALUES (?, ?)";
 	
 	private final static String STORE_PLAYER = "INSERT INTO ff_roster_player (team_id, " +
 			"week, starter, player_id) VALUES (?, ?, ?, ?)";
 	
 	private final static String CLEAR_ROSTER = "DELETE FROM ff_rosters WHERE " +
 		"team_id = ? AND week = ?";
 
 	private final static String CLEAR_PLAYERS = "DELETE FROM ff_roster_player WHERE " +
 	"team_id = ? AND week = ?";
 
 	private final static String REMOVE = "DELETE FROM ff_roster WHERE team_id = ?";
 	
	public Roster loadByTeamAndWeek(Team t, int week) throws DatabaseException {
 
 		Roster r = new Roster();
 		r.setTeamId(t.getId());
 		r.setWeek(week);
 
 		DatabaseConnectionManager dbcm = new DatabaseConnectionManager();
 		Connection conn = dbcm.getConnection();
 
 		PreparedStatement stmt1 = null;
 		ResultSet rs = null;
 
 		try {
 
 			stmt1 = conn.prepareStatement(SELECT_BY_TEAM);
 			stmt1.setInt(1, t.getId());
 			stmt1.setInt(2, week);
 
 			rs = stmt1.executeQuery();
 
 			while(rs.next()) {
 
 				boolean starter = rs.getBoolean(1);
 				String pid = rs.getString(2);
 
 				PlayerDAO pDao = new PlayerDAO();
 				Player p = pDao.getById(pid);
 
 				if (starter) {
 					r.addStartingPlayer(p);
 				} else {
 					r.addBenchPlayer(p);
 				}
 
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
			throw new DatabaseException();
 		} finally {
 			close(rs);
 			close(stmt1);
 			close(conn);
 		}
 
 		return r;
 		
 	}
 	
	public List<Roster> loadByTeam(Team t) throws DatabaseException {
 
 		List<Roster> rosters = new ArrayList<Roster>();
 		
 		DatabaseConnectionManager dbcm = new DatabaseConnectionManager();
 		Connection conn = dbcm.getConnection();
 
 		PreparedStatement stmt1 = null;
 		ResultSet rs = null;
 
		for (int w = 1; w <= 13; w++) {
 
 			Roster r = new Roster();
 			r.setTeamId(t.getId());
 			r.setWeek(w);
 
 			try {
 
 				stmt1 = conn.prepareStatement(SELECT_BY_TEAM);
 				stmt1.setInt(1, t.getId());
 				stmt1.setInt(2, w);
 
 				rs = stmt1.executeQuery();
 
 				while(rs.next()) {
 
 					boolean starter = rs.getBoolean(1);
 					String pid = rs.getString(2);
 
 					PlayerDAO pDao = new PlayerDAO();
 					Player p = pDao.getById(pid);
 					
 					if (starter) {
 						r.addStartingPlayer(p);
 					} else {
 						r.addBenchPlayer(p);
 					}
 
 				}
 
 			} catch (Exception e) {
 				e.printStackTrace();
				throw new DatabaseException();
 			} finally {
 				close(rs);
 				close(stmt1);
 			}
 			
			// only include a roster if it has players
			if (r.getStartingPlayers().size() > 0 || r.getBenchPlayers().size() > 0) {
				rosters.add(r);
			}
 
 		}
 
 		close(conn);
 		
 		return rosters;
 		
 	}
 
 	public void store(Roster r) throws DatabaseException {
 
 		DatabaseConnectionManager dbcm = new DatabaseConnectionManager();
 		Connection conn = dbcm.getConnection();
 
 		PreparedStatement stmt0 = null;
 		PreparedStatement stmt1 = null;
 
 		int teamId = r.getTeamId();
 		int week = r.getWeek();
 
 		List<Player> starters = r.getStartingPlayers();
 		List<Player> bench = r.getBenchPlayers();
 		
 		try {
 			
 			stmt0 = conn.prepareStatement(CLEAR_PLAYERS);
 			stmt0.setInt(1, teamId);
 			stmt0.setInt(2, week);
 			stmt0.executeUpdate();
 			
 		} catch (Exception e) {
 			e.printStackTrace();
			throw new DatabaseException();
 		} finally {
 			close(stmt0);
 		}
 
 		try {
 			
 			stmt0 = conn.prepareStatement(CLEAR_ROSTER);
 			stmt0.setInt(1, teamId);
 			stmt0.setInt(2, week);
 			stmt0.executeUpdate();
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new DatabaseException();
 		} finally {
 			close(stmt0);
 		}
 
 		try {
 			
 			stmt0 = conn.prepareStatement(STORE);
 			stmt0.setInt(1, teamId);
 			stmt0.setInt(2, week);
 			stmt0.executeUpdate();
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new DatabaseException();
 		} finally {
 			close(stmt0);
 		}
 		
 		for (Iterator<Player> ips = starters.iterator(); ips.hasNext();) {
 
 			Player p = ips.next();
 			
 			try {
 
 				stmt1 = conn.prepareStatement(STORE_PLAYER);
 				stmt1.setInt(1, teamId);
 				stmt1.setInt(2, week);
 				stmt1.setBoolean(3, true);
 				stmt1.setString(4, p.getId());
 				
 				stmt1.executeUpdate();
 
 			} catch (Exception e) {
 				e.printStackTrace();
 				throw new DatabaseException();
 			} finally {
 				close(stmt1);
 			}
 
 		}
 
 		for (Iterator<Player> ipb = bench.iterator(); ipb.hasNext();) {
 
 			Player p = ipb.next();
 			
 			try {
 
 				stmt1 = conn.prepareStatement(STORE_PLAYER);
 				stmt1.setInt(1, teamId);
 				stmt1.setInt(2, week);
 				stmt1.setBoolean(3, false);
 				stmt1.setString(4, p.getId());
 				
 				stmt1.executeUpdate();
 
 			} catch (Exception e) {
 				e.printStackTrace();
 				throw new DatabaseException();
 			} finally {
 				close(stmt1);
 			}
 
 		}
 		
 		close(conn);
 
 	}
 	
 }
