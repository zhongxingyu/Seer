 package com.sas.comp.service;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import com.sas.comp.models.Schedule;
 import com.sas.comp.mysql.Database;
 
 public class ScheduleService {
 
 	public List<Schedule> getLeagueSchedule(final Integer seasonId) {
 		return getSchedules(seasonId, false);
 	}
 
 	public List<Schedule> getPlayoffSchedule(final Integer seasonId) {
 		return getSchedules(seasonId, true);
 	}
 
 	private List<Schedule> getSchedules(final Integer seasonId, final Boolean playoff) {
 		final List<Schedule> schedules = new ArrayList<Schedule>();
 
 		try {
 			final Connection conn = Database.getConnection();
 			final PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM schedule WHERE playoff = ? AND season_id = ?");
 			pstmt.setBoolean(1, playoff);
 			pstmt.setInt(2, seasonId);
 
 			final ResultSet rs = pstmt.executeQuery();
 			while (rs.next()) {
 				final Schedule schedule = new Schedule();
 				schedule.setHome(rs.getString("home"));
 				schedule.setHomeId(rs.getInt("home_id"));
 				schedule.setAway(rs.getString("away"));
 				schedule.setAwayId(rs.getInt("away_id"));
 				schedule.setDate(rs.getTimestamp("date"));
 				schedule.setGameId(rs.getInt("game_id"));
 
 				schedule.setHomeScore(rs.getInt("home_score"));
 				if (rs.wasNull()) {
 					schedule.setHomeScore(null);
 				}
 
 				schedule.setAwayScore(rs.getInt("away_score"));
 				if (rs.wasNull()) {
 					schedule.setAwayScore(null);
 				}
 
 				schedules.add(schedule);
 			}
 
 			rs.close();
 			pstmt.close();
 			conn.close();
 		} catch (final Exception e) {
 			e.printStackTrace();
 		}
 
 		return schedules;
 	}
 
 	public List<Schedule> getSchedules(final Date date) {
 		final List<Schedule> schedules = new ArrayList<Schedule>();
 
 		try {
 			final Connection conn = Database.getConnection();
			final PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM schedule WHERE date between ? AND ?");
 			pstmt.setTimestamp(1, new java.sql.Timestamp(date.getTime()));
 
			final Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.DATE, 1); //minus number would decrement the days

			pstmt.setTimestamp(2, new java.sql.Timestamp(cal.getTime().getTime()));

 			final ResultSet rs = pstmt.executeQuery();
 			while (rs.next()) {
 				final Schedule schedule = new Schedule();
 				schedule.setHome(rs.getString("home"));
 				schedule.setHomeId(rs.getInt("home_id"));
 				schedule.setAway(rs.getString("away"));
 				schedule.setAwayId(rs.getInt("away_id"));
 				schedule.setDate(rs.getTimestamp("date"));
 				schedule.setGameId(rs.getInt("game_id"));
 
 				schedule.setHomeScore(rs.getInt("home_score"));
 				if (rs.wasNull()) {
 					schedule.setHomeScore(null);
 				}
 
 				schedule.setAwayScore(rs.getInt("away_score"));
 				if (rs.wasNull()) {
 					schedule.setAwayScore(null);
 				}
 
 				schedules.add(schedule);
 			}
 
 			rs.close();
 			pstmt.close();
 			conn.close();
 		} catch (final Exception e) {
 			e.printStackTrace();
 		}
 
 		return schedules;
 	}
 
 }
