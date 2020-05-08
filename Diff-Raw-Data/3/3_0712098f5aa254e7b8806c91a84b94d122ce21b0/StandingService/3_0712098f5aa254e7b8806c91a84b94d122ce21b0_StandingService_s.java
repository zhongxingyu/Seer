 package com.sas.comp.service;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.sas.comp.models.Standing;
 import com.sas.comp.mysql.Database;
 
 public class StandingService {
 
 	public List<Standing> getStandings(final Integer seasonId) {
 		final List<Standing> standings = new ArrayList<Standing>();
 
 		try {
 			final Connection conn = Database.getConnection();
 			final PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM standings WHERE season_id = ?");
 			pstmt.setInt(1, seasonId);
 
 			final ResultSet rs = pstmt.executeQuery();
 			while (rs.next()) {
 				final Standing standing = new Standing();
 				standing.setTeam(rs.getString(2));
 				standing.setPoints(rs.getInt(3));
 				standing.setWins(rs.getInt(4));
 				standing.setLosses(rs.getInt(5));
 				standing.setTies(rs.getInt(6));
 				standing.setGoalsFor(rs.getInt(7));
 				standing.setGoalsAgainst(rs.getInt(8));
 				standing.setGoalDifference(rs.getInt(9));
 				standing.setShutouts(rs.getInt(10));
 				standings.add(standing);
 			}
 		} catch (final Exception e) {
 			e.printStackTrace();
 		}
 
 		return standings;
 	}
 
 }
