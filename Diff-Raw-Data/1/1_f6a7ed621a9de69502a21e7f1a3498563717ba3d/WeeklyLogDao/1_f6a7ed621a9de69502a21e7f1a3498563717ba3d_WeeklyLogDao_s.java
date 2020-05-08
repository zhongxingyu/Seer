 package com.astrider.sfc.src.model.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import com.astrider.sfc.app.lib.BaseDao;
 import com.astrider.sfc.app.lib.Mapper;
 import com.astrider.sfc.src.model.vo.db.WeeklyLogVo;
 
 public class WeeklyLogDao extends BaseDao {
 	public WeeklyLogDao() {
 		super();
 	}
 
 	public WeeklyLogDao(Connection con) {
 		super(con);
 	}
 
 	public WeeklyLogVo selectNewest(int userId) {
 		WeeklyLogVo week = null;
 		PreparedStatement pstmt = null;
 		try {
 			StringBuilder sb = new StringBuilder();
 			sb.append("SELECT * FROM weekly_nut_amounts WHERE user_id = ? ");
 			sb.append("AND first_date = (SELECT MAX(first_date) FROM weekly_nut_amounts WHERE user_id = ?)");
 			pstmt = con.prepareStatement(sb.toString());
 			pstmt.setInt(1, userId);
 			pstmt.setInt(2, userId);
 			ResultSet rs = pstmt.executeQuery();
 			while (rs.next()) {
 				Mapper<WeeklyLogVo> mapper = new Mapper<WeeklyLogVo>();
 				week = mapper.fromResultSet(rs);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (pstmt != null) {
 					pstmt.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return week;
 	}
 
 	public WeeklyLogVo selectCurrentWeek(int userId) {
 		WeeklyLogVo week = null;
 		PreparedStatement pstmt = null;
 		try {
 			StringBuilder sb = new StringBuilder();
 			sb.append("SELECT * FROM weekly_nut_amounts WHERE user_id = ? AND first_date BETWEEN ");
 			sb.append("(SELECT TRUNC(SYSDATE, 'Day') FROM dual) AND (SELECT TRUNC(SYSDATE+7 , 'Day') FROM dual)");
 			pstmt = con.prepareStatement(sb.toString());
 			pstmt.setInt(1, userId);
 			ResultSet rs = pstmt.executeQuery();
 			while (rs.next()) {
 				Mapper<WeeklyLogVo> mapper = new Mapper<WeeklyLogVo>();
 				week = mapper.fromResultSet(rs);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (pstmt != null) {
 					pstmt.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return week;
 	}
 
 	public WeeklyLogVo selectByWeekAgo(int userId, int weekAgo) {
 		WeeklyLogVo week = null;
 		PreparedStatement pstmt = null;
 		try {
 			StringBuilder sb = new StringBuilder();
 			sb.append("SELECT * FROM weekly_nut_amounts WHERE user_id = ? AND first_date BETWEEN ");
 			if (0 < weekAgo) {
 				int start = -7 * weekAgo;
 				int end = start + 7;
 				if (start != 0) {
 					sb.append("(SELECT TRUNC(SYSDATE " + start + ", 'Day') FROM dual) AND ");
 				} else {
 					sb.append("(SELECT TRUNC(SYSDATE, 'Day') FROM dual) AND ");
 				}
 				
 				if (end != 0) {
 					sb.append("(SELECT TRUNC(SYSDATE " + end + ", 'Day') FROM dual)");
 				} else {
 					sb.append("(SELECT TRUNC(SYSDATE, 'Day') FROM dual)");
 				}
 				
 			} else {
 				sb.append("(SELECT TRUNC(SYSDATE, 'Day') FROM dual) AND (SELECT TRUNC(SYSDATE + 7, 'Day') FROM dual)");
 			}
			System.out.println(sb.toString());
 			pstmt = con.prepareStatement(sb.toString());
 			pstmt.setInt(1, userId);
 			ResultSet rs = pstmt.executeQuery();
 			while (rs.next()) {
 				Mapper<WeeklyLogVo> mapper = new Mapper<WeeklyLogVo>();
 				week = mapper.fromResultSet(rs);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (pstmt != null) {
 					pstmt.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return week;
 	}
 
 }
