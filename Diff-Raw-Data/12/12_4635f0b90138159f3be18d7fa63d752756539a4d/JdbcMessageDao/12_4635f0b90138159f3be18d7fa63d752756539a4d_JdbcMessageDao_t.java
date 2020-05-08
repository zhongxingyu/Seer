 package com.ctb.pilot.chat.dao.jdbc;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.ctb.pilot.chat.dao.MessageDao;
 import com.ctb.pilot.chat.model.Message;
 
 public class JdbcMessageDao implements MessageDao {
 
 	public JdbcMessageDao() {
 		try {
 			Class.forName("org.gjt.mm.mysql.Driver");
 			Class.forName("org.apache.commons.dbcp.PoolingDriver");
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public List<Message> getMessages(int pageSize, int pageNo) {
 		Connection con = null;
 		Statement stmt = null;
 		ResultSet rs = null;
 		List<Message> messageList = new ArrayList<Message>();
 		try {
 			con = DriverManager
 					.getConnection("jdbc:apache:commons:dbcp:/ctb_db_pool");
 			stmt = con.createStatement();
 
 			int offset = (pageNo - 1) * pageSize;
 			rs = stmt
 					.executeQuery("select * from tb_chat_message cm, tb_user u where cm.user_seq = u.seq order by cm.seq desc limit "
 							+ pageSize + " offset " + offset);
 			while (rs.next()) {
 				Message message = new Message();
 				message.setSequence(rs.getInt("seq"));
 				message.setCreatedTime(rs.getTimestamp("created_time"));
 				message.setUserSequence(rs.getInt("user_seq"));
 				message.setNickname(rs.getString("nickname"));
 				message.setMessage(rs.getString("message"));
 				messageList.add(message);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 			if (stmt != null) {
 				try {
 					stmt.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 			if (con != null) {
 				try {
 					con.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return messageList;
 	}
 
 	@Override
 	public void insertMessage(int userSequence, String message) {
 		Connection con = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			// con = DriverManager.getConnection(CONNECTION_URL);
 			con = DriverManager
 					.getConnection("jdbc:apache:commons:dbcp:/ctb_db_pool");
 			stmt = con
 					.prepareStatement("insert into tb_chat_message (created_time, user_seq, message) values (now(), ?, ?)");
 			stmt.setInt(1, userSequence);
 			stmt.setString(2, message);
 			stmt.executeUpdate();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 			if (stmt != null) {
 				try {
 					stmt.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 			if (con != null) {
 				try {
 					con.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	@Override
 	public long getAllMessageCount() {
		Connection con = null;
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
			con = DriverManager
 					.getConnection("jdbc:apache:commons:dbcp:/ctb_db_pool");
 			stmt = con.createStatement();
 
 			rs = stmt
 					.executeQuery("select count(1) total from tb_chat_message");
 			rs.next();
 			return rs.getLong("total");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 			if (stmt != null) {
 				try {
 					stmt.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
 		}
 		return 0;
 	}
 
 }
