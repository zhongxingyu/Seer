 package org.ukiuni.irc4j.db;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import org.ukiuni.irc4j.Channel;
 import org.ukiuni.irc4j.User;
 import org.ukiuni.irc4j.entity.JoinLog;
 import org.ukiuni.irc4j.entity.Message;
 import org.ukiuni.irc4j.util.IOUtil;
 
 public class Database {
 	private static Database instance;
 	private Connection con;
 
 	public static Database getInstance() {
 		if (null == instance) {
 			synchronized (Database.class) {
 				if (null == instance) {
 					instance = new Database();
 				}
 			}
 		}
 		return instance;
 	}
 
 	public Database() {
 		try {
 			new File(System.getProperty("user.home") + "/.airc").mkdirs();
 			this.con = DriverManager.getConnection("jdbc:h2:file:" + System.getProperty("user.home") + "/.airc/.db");
 			Statement st = null;
 			try {
 				st = this.con.createStatement();
 				st.executeQuery("select * from message limit 1");
 			} catch (SQLException e) {
 				st.execute("create table message (id bigint auto_increment primary key, type varchar, senderFQUN varchar, sender_nick_name varchar,  target_channel varchar, message varchar,created_at timestamp)");
 			} finally {
 				IOUtil.close(st);
 			}
 			try {
 				st = this.con.createStatement();
 				st.executeQuery("select * from join_log limit 1");
 			} catch (SQLException e) {
 				st.execute("create table join_log (id bigint auto_increment primary key, channel_name varchar,  event varchar, user_id bigint, nickname varchar, created_at timestamp)");
 			} finally {
 				IOUtil.close(st);
 			}
 			try {
 				st = this.con.createStatement();
 				st.executeQuery("select nickname from user limit 1");
 			} catch (SQLException e) {
 				try {
 					st.execute("drop table user");
 				} catch (SQLException e1) {
 				}
 				st.execute("create table user (id bigint auto_increment primary key, name varchar, real_name varchar, email varchar, host_name varchar, nickname varchar unique, password_hashed varchar, description varchar, icon_image varchar, created_at timestamp, updated_at timestamp)");
 			} finally {
 				IOUtil.close(st);
 			}
 			try {
 				st = this.con.createStatement();
 				st.execute("create index user_nickname on user(nickname)");
 			} catch (SQLException e) {
 			} finally {
 				IOUtil.close(st);
 			}
 			try {
 				st = this.con.createStatement();
 				st.executeQuery("select * from user_and_channel_relation limit 1");
 			} catch (SQLException e) {
 				st.execute("create table user_and_channel_relation (id bigint auto_increment primary key, user_id long, channel_name varchar, created_at timestamp)");
 			} finally {
 				IOUtil.close(st);
 			}
 			try {
 				st = this.con.createStatement();
 				st.execute("create index user_and_channel_relation_user_id on user_and_channel_relation(user_id)");
 			} catch (SQLException e) {
 			} finally {
 				IOUtil.close(st);
 			}
 		} catch (Throwable e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void regist(Message message) {
 		PreparedStatement stmt = null;
 		try {
 			stmt = con.prepareStatement("insert into message (type, senderFQUN, sender_nick_name, target_channel, message, created_at) values ( ?, ?, ?, ?, ?, now())");
 			stmt.setString(1, message.getType());
 			stmt.setString(2, message.getSenderFQUN());
 			stmt.setString(3, message.getSenderNickName());
 			stmt.setString(4, message.getTargetChannel());
 			stmt.setString(5, message.getMessage());
 			stmt.executeUpdate();
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		} finally {
 			IOUtil.close(stmt);
 		}
 	}
 
 	public void regist(JoinLog joinLog) {
 		PreparedStatement stmt = null;
 		try {
 			stmt = con.prepareStatement("insert into join_log (channel_name, event, user_id, nickname, created_at) values (?, ?, ?, ?, now())");
 			stmt.setString(1, joinLog.getChannelName());
 			stmt.setString(2, joinLog.getEvent().toString());
 			stmt.setLong(3, joinLog.getUserId());
 			stmt.setString(4, joinLog.getNickName());
 			stmt.executeUpdate();
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		} finally {
 			IOUtil.close(stmt);
 		}
 	}
 
 	public void resetJoinChannel(User user, List<Channel> channelList) {
 		PreparedStatement stmt = null;
 		try {
 			stmt = con.prepareStatement("delete from user_and_channel_relation where user_id = ?");
 			stmt.setLong(1, user.getId());
 			stmt.execute();
 			stmt.close();
 			for (Channel channel : channelList) {
 				stmt = con.prepareStatement("insert into user_and_channel_relation (user_id, channel_name, created_at) values (?, ?, now())");
 				stmt.setLong(1, user.getId());
 				stmt.setString(2, channel.getName());
 				stmt.execute();
 				stmt.close();
 			}
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		} finally {
 			IOUtil.close(stmt);
 		}
 	}
 
 	public List<String> loadJoinedChannelNames(User user) {
 		PreparedStatement stmt = null;
 		try {
 			stmt = con.prepareStatement("select user_id, channel_name, created_at from user_and_channel_relation where user_id = ? order by created_at");
 			stmt.setLong(1, user.getId());
 			ResultSet resultSet = stmt.executeQuery();
 			List<String> channelNameList = new ArrayList<String>();
 			while (resultSet.next()) {
 				channelNameList.add(resultSet.getString("channel_name"));
 			}
 			return channelNameList;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		} finally {
 			IOUtil.close(stmt);
 		}
 	}
 
 	public void regist(User user) {
 		PreparedStatement stmt = null;
 		try {
 			if (user.getId() == 0) {
 				stmt = con.prepareStatement("insert into user (name, real_name, host_name, nickname, email, password_hashed, description, icon_image, created_at) values (?, ?, ?, ?, ?, ?, ?, ?, now())");
 				stmt.setString(1, user.getName());
 				stmt.setString(2, user.getRealName());
 				stmt.setString(3, user.getHostName());
 				stmt.setString(4, user.getNickName());
 				stmt.setString(5, user.getEmail());
 				stmt.setString(6, user.getPasswordHashed());
 				stmt.setString(7, user.getDescription());
 				stmt.setString(8, user.getIconImage());
 				stmt.executeUpdate();
 				PreparedStatement idQueryStmt = con.prepareStatement("select id from user where nickname = ?");
 				idQueryStmt.setString(1, user.getNickName());
 				ResultSet resultSet = idQueryStmt.executeQuery();
 				resultSet.next();
 				user.setId(resultSet.getLong("id"));
 				resultSet.close();
 				idQueryStmt.close();
 			} else {
 				StringBuilder sqlCreateBuilder = new StringBuilder("update user set");
 				if (null != user.getName()) {
 					sqlCreateBuilder.append(" name = ?,");
 				}
 				if (null != user.getRealName()) {
 					sqlCreateBuilder.append(" real_name = ?,");
 				}
 				if (null != user.getHostName()) {
 					sqlCreateBuilder.append(" host_name = ?,");
 				}
 				if (null != user.getNickName()) {
 					sqlCreateBuilder.append(" nickname = ?,");
 				}
 				if (null != user.getEmail()) {
 					sqlCreateBuilder.append(" email = ?,");
 				}
 				if (null != user.getPasswordHashed()) {
 					sqlCreateBuilder.append(" password_hashed = ?,");
 				}
 				if (null != user.getDescription()) {
 					sqlCreateBuilder.append(" description = ?,");
 				}
 				if (null != user.getIconImage()) {
 					sqlCreateBuilder.append(" icon_image = ?,");
 				}
 				sqlCreateBuilder.append(" updated_at = now()");
 				sqlCreateBuilder.append(" where id = ?");
 				stmt = con.prepareStatement(sqlCreateBuilder.toString());
 				int stmtIndex = 1;
 				if (null != user.getName()) {
 					stmt.setString(stmtIndex++, user.getName());
 				}
 				if (null != user.getRealName()) {
 					stmt.setString(stmtIndex++, user.getRealName());
 				}
 				if (null != user.getHostName()) {
 					stmt.setString(stmtIndex++, user.getHostName());
 				}
 				if (null != user.getNickName()) {
 					stmt.setString(stmtIndex++, user.getNickName());
 				}
 				if (null != user.getEmail()) {
 					stmt.setString(stmtIndex++, user.getEmail());
 				}
 				if (null != user.getPasswordHashed()) {
 					stmt.setString(stmtIndex++, user.getPasswordHashed());
 				}
 				if (null != user.getDescription()) {
 					stmt.setString(stmtIndex++, user.getDescription());
 				}
 				if (null != user.getIconImage()) {
 					stmt.setString(stmtIndex++, user.getIconImage());
 				}
 				stmt.setLong(stmtIndex++, user.getId());
 				stmt.executeUpdate();
 			}
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		} finally {
 			IOUtil.close(stmt);
 		}
 	}
 
 	public List<Message> loadMessage(String channel, int limit) {
 		return loadMessage(channel, limit, true);
 	}
 
 	public List<Message> loadMessage(String channel, int limit, boolean oldToNew) {
 		List<Message> messageList = loadMessage(channel, limit, "", oldToNew);
 		return messageList;
 	}
 
 	private List<Message> loadMessage(String channel, int limit, String where, boolean oldToNew) {
 		PreparedStatement stmt = null;
 		try {
 			String sql = "select id, type, senderFQUN, sender_nick_name, target_channel, message, created_at from message where target_channel = ? " + where + " order by id desc limit ?";
 			stmt = con.prepareStatement(sql);
 			stmt.setString(1, channel);
 			stmt.setInt(2, limit);
 			ResultSet rs = stmt.executeQuery();
 			List<Message> messageList = rsToMessage(rs);
 			if (oldToNew) {
 				Collections.reverse(messageList);
 			}
 			return messageList;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		} finally {
 			IOUtil.close(stmt);
 		}
 	}
 
 	private List<Message> rsToMessage(ResultSet rs) throws SQLException {
 		List<Message> messageList = new ArrayList<Message>();
 		while (rs.next()) {
 			Message message = new Message();
 			message.setId(rs.getLong("id"));
 			message.setType(rs.getString("type"));
 			message.setSenderFQUN(rs.getString("senderFQUN"));
 			message.setSenderNickName(rs.getString("sender_nick_name"));
 			message.setTargetChannel(rs.getString("target_channel"));
 			message.setMessage(rs.getString("message"));
 			message.setCreatedAt(new Date(rs.getTimestamp("created_at").getTime()));
 			messageList.add(message);
 		}
 		return messageList;
 	}
 
 	public List<Message> loadMessage(String channel, long maxId, int limit) {
 		return loadMessage(channel, limit, "and id <= " + maxId, true);
 	}
 
 	public List<Message> loadMessageOlderThan(String channel, long maxId, int limit) {
 		return loadMessage(channel, limit, "and id < " + maxId, false);
 	}
 
 	public List<Message> loadMessageNewerThan(String channel, long maxId, int limit) {
 		return loadMessage(channel, limit, "and id > " + maxId, false);
 	}
 
 	public long loadMaxId(String channel) {
 		List<Message> messageList = loadMessage(channel, 1, "", false);
 		return messageList.isEmpty() ? 0 : messageList.get(0).getId();
 	}
 
 	public User loadUser(String nickName) {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			String sql = "select id, name, real_name, host_name, nickname, email, password_hashed, description, icon_image, created_at from user where nickname = ?";
 			stmt = con.prepareStatement(sql);
 			stmt.setString(1, nickName);
 			rs = stmt.executeQuery();
 			if (!rs.next()) {
 				return null;
 			}
 			User user = new User();
 			user.setId(rs.getLong("id"));
 			user.setName(rs.getString("name"));
 			user.setRealName(rs.getString("real_name"));
 			user.setNickName(rs.getString("nickname"));
 			user.setEmail(rs.getString("email"));
 			user.setPasswordHashed(rs.getString("password_hashed"));
 			user.setDescription(rs.getString("description"));
 			user.setIconImage(rs.getString("icon_image"));
 			user.setCreatedAt(rs.getDate("created_at"));
 			return user;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		} finally {
 			IOUtil.close(rs);
 			IOUtil.close(stmt);
 		}
 	}
 
 	public void registJoinChannel(User user, String channelName) {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = con.prepareStatement("select user_id from user_and_channel_relation where user_id = ? and channel_name = ?");
 			stmt.setLong(1, user.getId());
 			stmt.setString(2, channelName);
 			rs = stmt.executeQuery();
 			if (!rs.next()) {
 				stmt = con.prepareStatement("insert into user_and_channel_relation (user_id, channel_name, created_at) values (?, ?, now())");
 				stmt.setLong(1, user.getId());
 				stmt.setString(2, channelName);
 				stmt.execute();
 			}
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		} finally {
 			IOUtil.close(rs);
 			IOUtil.close(stmt);
 		}
 	}
 
 	public void removePartChannel(User user, String channelName) {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = con.prepareStatement("delete from user_and_channel_relation where user_id = ? and channel_name = ?");
 			stmt.setLong(1, user.getId());
 			stmt.setString(2, channelName);
 			stmt.execute();
 			stmt.close();
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		} finally {
 			IOUtil.close(rs);
 			IOUtil.close(stmt);
 		}
 	}
 }
