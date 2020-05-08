 package com.parq.server.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import com.parq.server.dao.model.object.UserSelfReporting;
 
 public class MiscellaneousDao extends AbstractParqDaoParent {
 
	private static final String sqlUpdateCount = "INSERT INTO count (tempValue) VALUE ('')";
 	private static final String sqlGetHighestCount = "SELECT count_id FROM count ORDER BY count_id DESC LIMIT 1";
 	
 	private static final String sqlInsertUserReport = "INSERT INTO userselfreporting "
 			+ "(user_id, space_id, space_status, score_1, score_2, score_3, score_4, score_5, score_6)"
 			+ "VALUE (?, ?, ?, ?, ?, ?, ?, ?, ?)";
 	
 	private static final String sqlGetAllUserReport = "SELECT "
 			+ " report_id, user_id, space_id, space_status, report_datetime, score_1, score_2, score_3, score_4, score_5, score_6"
 			+ " FROM userselfreporting WHERE user_id = ? ORDER BY report_id DESC";
 	
 	/**
 	 * Get the next highest count number
 	 * @return <B>long</B>
 	 */
 	public long getNextCount() {
 		PreparedStatement pstmt = null;
 		Connection con = null;
 		long countId = 0;
 		try {
 			con = getConnection();
 			pstmt = con.prepareStatement(sqlUpdateCount);
 			if (pstmt.executeUpdate() == 1) {
 				pstmt = con.prepareStatement(sqlGetHighestCount);
 				ResultSet rs = pstmt.executeQuery();
 				rs.first();
 				countId = rs.getLong("count_id");
 			}
 		} catch (SQLException sqle) {
 			System.out.println("SQL statement is invalid: " + pstmt);
 			sqle.printStackTrace();
 			throw new RuntimeException(sqle);
 		} finally {
 			closeConnection(con);
 		}
 		return countId;
 	}
 	
 	public boolean insertUserSelfReporting(UserSelfReporting report) {
 		PreparedStatement pstmt = null;
 		Connection con = null;
 		boolean updateSuccessful = false;
 		try {
 			con = getConnection();
 			pstmt = con.prepareStatement(sqlInsertUserReport);
 			
 			pstmt.setLong(1, report.getUserId());
 			pstmt.setLong(2, report.getSpaceId());
 			pstmt.setString(3, report.getParkingSpaceStatus());
 			pstmt.setInt(4, report.getScore1());
 			pstmt.setInt(5, report.getScore2());
 			pstmt.setInt(6, report.getScore3());
 			pstmt.setInt(7, report.getScore4());
 			pstmt.setInt(8, report.getScore5());
 			pstmt.setInt(9, report.getScore6());
 						
 			updateSuccessful = pstmt.executeUpdate() == 1;
 		} catch (SQLException sqle) {
 			System.out.println("SQL statement is invalid: " + pstmt);
 			sqle.printStackTrace();
 			throw new RuntimeException(sqle);
 		} finally {
 			closeConnection(con);
 		}
 		return updateSuccessful;
 	}
 	
 	public List<UserSelfReporting> getUserSelfReportingHistoryForUser(long userId) {
 		PreparedStatement pstmt = null;
 		Connection con = null;
 		List<UserSelfReporting> reportHistory = null;
 		try {
 			con = getConnection();
 			pstmt = con.prepareStatement(sqlGetAllUserReport);
 			pstmt.setLong(1, userId);
 			ResultSet rs = pstmt.executeQuery();
 			reportHistory = createUserReportingHistory(rs);
 		} catch (SQLException sqle) {
 			System.out.println("SQL statement is invalid: " + pstmt);
 			sqle.printStackTrace();
 			throw new RuntimeException(sqle);
 		} finally {
 			closeConnection(con);
 		}
 		return reportHistory;
 	}
 
 	private List<UserSelfReporting> createUserReportingHistory(ResultSet rs) throws SQLException {
 		if (rs == null || !rs.isBeforeFirst()) {
 			return Collections.emptyList();
 		}
 		List<UserSelfReporting> reportHistory = new ArrayList<UserSelfReporting>();
 		while (rs.next()) {
 			UserSelfReporting report = new UserSelfReporting();
 			report.setReportId(rs.getLong("report_id"));
 			report.setUserId(rs.getLong("user_id"));
 			report.setSpaceId(rs.getLong("space_id"));
 			report.setParkingSpaceStatus(rs.getString("space_status"));
 			report.setReportDateTime(rs.getTimestamp("report_datetime"));
 			report.setScore1(rs.getInt("score_1"));
 			report.setScore2(rs.getInt("score_2"));
 			report.setScore3(rs.getInt("score_3"));
 			report.setScore4(rs.getInt("score_4"));
 			report.setScore5(rs.getInt("score_5"));
 			report.setScore6(rs.getInt("score_6"));
 			
 			reportHistory.add(report);
 		}
 		return reportHistory;
 	}
 }
