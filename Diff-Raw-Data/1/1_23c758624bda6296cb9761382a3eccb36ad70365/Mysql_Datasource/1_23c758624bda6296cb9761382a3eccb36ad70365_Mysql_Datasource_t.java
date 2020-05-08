 package data_source_interface;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 
 import JSONtypes.Connect;
 import JSONtypes.Disconnect;
 import JSONtypes.Invalid;
 import JSONtypes.Line;
 import JSONtypes.Other;
 import JSONtypes.Server;
 import JSONtypes.SubSystemReq;
 import enums.AuthType;
 import enums.Status;
 import enums.SubSystem;
 
 public class Mysql_Datasource implements SSHD_log_vis_datasource {
 
 	@Override
 	public List<Line> getEntriesFromDataSource(String serverName,
 			String startTime, String endTime) {
 		String query;
 		if (serverName == null) {
 			query = "SELECT entry.timestamp, entry.connid, entry.reqtype, "
 					+ "entry.authtype, entry.status, user.name as user, entry.source, entry.port, entry.subsystem, entry.code, "
 					+ "entry.isfreqtime, entry.isfreqloc, entry.rawline "
 					+ "FROM entry LEFT JOIN server ON entry.server = server.id "
 					+ "LEFT JOIN user ON entry.user = user.id "
 					+ "WHERE server.name = ? AND "
 					+ "entry.timestamp BETWEEN ? AND ?;";
 		} else {
 			query = "SELECT entry.timestamp, server.name as server, entry.connid, entry.reqtype, "
 					+ "entry.authtype, entry.status, user.name as user, entry.source, entry.port, entry.subsystem, entry.code, "
 					+ "entry.isfreqtime, entry.isfreqloc, entry.rawline "
 					+ "FROM entry LEFT JOIN server ON entry.server = server.id "
 					+ "LEFT JOIN user ON entry.user = user.id "
 					+ "WHERE entry.timestamp BETWEEN ? AND ?;";
 		}
 		List<Line> lines = null;
 		Context context = null;
 		Connection connection = null;
 		PreparedStatement state = null;
 		ResultSet result = null;
 		try {
 			context = new InitialContext();
 
 			connection = ((DataSource) context
 					.lookup("java:comp/env/jdbc/sshd_vis_db")).getConnection();
 			state = connection.prepareStatement(query);
 
			//TODO fix date format
 			SimpleDateFormat formatter = new SimpleDateFormat("",
 					Locale.ENGLISH);
 			if (serverName != null) {
 				state.setString(1, serverName);
 				state.setTimestamp(2, new Timestamp(formatter.parse(startTime)
 						.getTime()));
 				state.setTimestamp(3, new Timestamp(formatter.parse(endTime)
 						.getTime()));
 			} else {
 				state.setTimestamp(1, new Timestamp(formatter.parse(startTime)
 						.getTime()));
 				state.setTimestamp(2, new Timestamp(formatter.parse(endTime)
 						.getTime()));
 			}
 			state.execute();
 			result = state.getResultSet();
 			lines = new ArrayList<Line>();
 			Line res;
 			while (result.next()) {
 				if (result.getString("entry.reqtype") == "connect") {
 					res = loadConnect(result);
 				} else if (result.getString("entry.reqtype") == "disconnect") {
 					res = loadDisconnect(result);
 				} else if (result.getString("entry.reqtype") == "subsystem") {
 					res = loadSubsystem(result);
 				} else if (result.getString("entry.reqtype") == "invalid") {
 					res = loadInvalid(result);
 				} else {
 					res = loadOther(result);
 				}
 				lines.add(res);
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NamingException e1) {
 			e1.printStackTrace();
 		} finally {
 			try {
 				if (result != null) {
 					result.close();
 				}
 				if (state != null) {
 					state.close();
 				}
 				if (connection != null) {
 					connection.close();
 				}
 				if (context != null) {
 					context.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			} catch (NamingException e) {
 				e.printStackTrace();
 			}
 		}
 		return lines;
 	}
 
 	private Line loadOther(ResultSet result) throws SQLException {
 		Timestamp time = result.getTimestamp("timestamp");
 		Server s;
 		if (result.getMetaData().getColumnCount() == 11) {
 			s = new Server(null, null);
 		} else {
 			s = new Server(result.getString("server"), null);
 		}
 		String msg = result.getString("rawline");
 		msg = msg.substring(msg.indexOf("]:") + 2);
 		return new Other(time, s, result.getInt("connectid"), msg,
 				result.getString("rawline"));
 	}
 
 	private Line loadInvalid(ResultSet result) throws SQLException {
 		Timestamp time = result.getTimestamp("timestamp");
 		Server s;
 		if (result.getMetaData().getColumnCount() == 11) {
 			s = new Server(null, null);
 		} else {
 			s = new Server(result.getString("server"), null);
 		}
 		return new Invalid(time, s, result.getInt("connectid"),
 				result.getString("user"), result.getString("source"),
 				result.getString("rawlwine"));
 	}
 
 	private Line loadSubsystem(ResultSet result) throws SQLException {
 		Timestamp time = result.getTimestamp("timestamp");
 		Server s;
 		if (result.getMetaData().getColumnCount() == 11) {
 			s = new Server(null, null);
 		} else {
 			s = new Server(result.getString("server"), null);
 		}
 		SubSystem sub;
 		if (result.getString("subsystem").equals("sftp")) {
 			sub = SubSystem.SFTP;
 		} else {
 			sub = SubSystem.SCP;
 		}
 		return new SubSystemReq(time, s, result.getInt("connectid"), sub,
 				result.getString("rawline"));
 	}
 
 	private Line loadDisconnect(ResultSet result) throws SQLException {
 		Timestamp time = result.getTimestamp("timestamp");
 		Server s;
 		if (result.getMetaData().getColumnCount() == 11) {
 			s = new Server(null, null);
 		} else {
 			s = new Server(result.getString("server"), null);
 		}
 		return new Disconnect(time, s, result.getInt("connectid"),
 				result.getInt("code"), result.getString("source"),
 				result.getString("rawline"));
 	}
 
 	private Line loadConnect(ResultSet result) throws SQLException {
 		Timestamp time = result.getTimestamp("timestamp");
 		Server s;
 		if (result.getMetaData().getColumnCount() == 11) {
 			s = null;
 		} else {
 			s = new Server(result.getString("server"), null);
 		}
 
 		AuthType auth;
 		String temp = result.getString("authtype");
 		if (temp.equals("pass")) {
 			auth = AuthType.PASS;
 		} else if (temp.equals("key")) {
 			auth = AuthType.KEY;
 		} else if (temp.equals("host")) {
 			auth = AuthType.HOST;
 		} else if (temp.equals("gssapi")) {
 			auth = AuthType.GSSAPI;
 		} else {
 			auth = AuthType.NONE;
 		}
 
 		Status status;
 		if (result.getString("status").equals("accepted")) {
 			status = Status.ACCEPTED;
 		} else {
 			status = Status.FAILED;
 		}
 
 		return new Connect(time, s, result.getInt("connectid"), status, auth,
 				result.getString("user"), result.getString("source"),
 				result.getInt("port"), result.getLong("isfreqtime"),
 				result.getInt("isfreqloc"), result.getString("rawline"));
 	}
 
 }
