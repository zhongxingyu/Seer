 package data_source_interface;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 
 import org.jooq.Condition;
 import org.jooq.DSLContext;
 import org.jooq.Insert;
 import org.jooq.JoinType;
 import org.jooq.Query;
 import org.jooq.SQLDialect;
 import org.jooq.Select;
 import org.jooq.SelectWhereStep;
 import org.jooq.impl.DSL;
 
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
 
 public class Mysql_Datasource implements LogDataSource {
 	private Context context = null;
 	private Connection connection = null;
 	private DSLContext queryBuilder = null;
 
 
 	public Mysql_Datasource() throws NamingException, SQLException{
 		this.context = new InitialContext();
 		this.connection = ((DataSource) context.lookup("java:comp/env/jdbc/sshd_vis_db")).getConnection();
 		this.queryBuilder = DSL.using(SQLDialect.MYSQL);
 	}
 
 	public Mysql_Datasource(String dbName) throws NamingException, SQLException {
 		this.context = new InitialContext();
 		this.connection = ((DataSource) context.lookup("java:comp/env/jdbc/" + dbName)).getConnection();
 	}
 
 	@Override
 	public List<Line> getEntriesFromDataSource(String serverName, String source,
 			String user, String startTime, String endTime) throws DataSourceException {
 
 		SelectWhereStep base = this.queryBuilder.select(DSL.fieldByName("entry", "id"), DSL.fieldByName("entry", "timestamp"), DSL.fieldByName("server", "name").as("server")
 				, DSL.fieldByName("entry", "connid") , DSL.fieldByName("entry", "reqtype"), DSL.fieldByName("entry", "authtype"), DSL.fieldByName("entry", "status")
 				, DSL.fieldByName("user", "name").as("user"), DSL.fieldByName("entry", "source"), DSL.fieldByName("entry", "port"), DSL.fieldByName("entry", "subsystem")
 				, DSL.fieldByName("entry", "code"), DSL.fieldByName("entry", "isFreqTime"), DSL.fieldByName("entry", "isFreqLoc"), DSL.fieldByName("entry", "rawline"))
 				.from(DSL.tableByName("entry"))
 				.join(DSL.tableByName("server"), JoinType.JOIN)
 				.on(DSL.fieldByName("entry", "server").equal(DSL.fieldByName("server", "id")))
 				.join(DSL.tableByName("user"), JoinType.LEFT_OUTER_JOIN)
 				.on(DSL.fieldByName("entry", "user").equal(DSL.fieldByName("user", "id")));
 
 		Condition cond = null;
 		if (serverName != null){
 			cond = DSL.fieldByName("server", "name").equal(serverName);
 		}
 		if (source != null) {
 			if (cond != null) {
 				cond.and(DSL.fieldByName("entry", "source").like(source));
 			} else {
 				cond = DSL.fieldByName("entry", "source").like(source);
 			}
 		}
 		if (user != null) {
 			if (cond != null) {
 				cond.and(DSL.fieldByName("entry", "user").like(user));
 			} else {
 				cond = DSL.fieldByName("entry", "user").like(user);
 			}
 		}
 		if (cond != null) {
 			cond.and(DSL.fieldByName("entry", "timestamp").between(startTime, endTime));
 		} else {
 			cond = DSL.fieldByName("entry", "timestamp").between(startTime, endTime);
 		}
 
 		Query query = base.where(cond);
 		String sql = query.getSQL();
 
 		List<Line> lines = null;
 		PreparedStatement state = null;
 		ResultSet result = null;
 		try {
 			state = connection.prepareStatement(sql);
 
 			//FIXME iterate over bound variables, binding in prepared statement.
 			List<Object> vars = query.getBindValues();
 			for (int i = 0; i < vars.size(); i++){
 				state.setObject(i+1, vars.get(i));
 			}
 
 			state.execute();
 			result = state.getResultSet();
 			lines = parseResults(result);
 
 		} catch (SQLException e) {
 			throw new DataSourceException(e);
 		} catch (NumberFormatException e) {
 			throw new DataSourceException(e);
 		} finally {
 			try {
 				if (result != null) {
 					result.close();
 				}
 				if (state != null) {
 					state.close();
 				}
 			} catch (SQLException e) {
 				throw new DataSourceException(e);
 			}
 		}
 		return lines;
 	}
 
 	private List<Line> parseResults(ResultSet result) throws SQLException{
 		List<Line> lines = new ArrayList<Line>();
 		Line res;
 		String colVal;
 		while (result.next()) {
 			colVal = result.getString("entry.reqtype");
 			if ("connect".equalsIgnoreCase(colVal)) {
 				res = loadConnect(result);
 			} else if ("disconnect".equalsIgnoreCase(colVal)) {
 				res = loadDisconnect(result);
 			} else if ("subsystem".equalsIgnoreCase(colVal)) {
 				res = loadSubsystem(result);
 			} else if ("invalid".equalsIgnoreCase(colVal)) {
 				res = loadInvalid(result);
 			} else {
 				res = loadOther(result);
 			}
 			lines.add(res);
 		}
 		return lines;
 	}
 
 	private Line loadOther(ResultSet result) throws SQLException {
 		long time = result.getLong("timestamp");
 		Server s;
 		if (result.getMetaData().getColumnCount() == 11) {
 			s = new Server(null, null);
 		} else {
 			s = new Server(result.getString("server"), null);
 		}
 		String msg = result.getString("rawline");
 		msg = msg.substring(msg.indexOf("]:") + 2);
 		return new Other(result.getInt("id"), time, s, result.getInt("connid"), msg,
 				result.getString("rawline"));
 	}
 
 	private Line loadInvalid(ResultSet result) throws SQLException {
 		long time = result.getLong("timestamp");
 		Server s;
 		if (result.getMetaData().getColumnCount() == 11) {
 			s = new Server(null, null);
 		} else {
 			s = new Server(result.getString("server"), null);
 		}
 		return new Invalid(result.getInt("id"), time, s, result.getInt("connid"),
 				result.getString("user"), result.getString("source"),
 				result.getString("rawline"));
 	}
 
 	private Line loadSubsystem(ResultSet result) throws SQLException {
 		long time = result.getLong("timestamp");
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
 		return new SubSystemReq(result.getInt("id"), time, s, result.getInt("connid"), sub,
 				result.getString("rawline"));
 	}
 
 	private Line loadDisconnect(ResultSet result) throws SQLException {
 		long time = result.getLong("timestamp");
 		Server s;
 		if (result.getMetaData().getColumnCount() == 11) {
 			s = new Server(null, null);
 		} else {
 			s = new Server(result.getString("server"), null);
 		}
 		return new Disconnect(result.getInt("id"), time, s, result.getInt("connid"),
 				result.getInt("code"), result.getString("source"),
 				result.getString("rawline"));
 	}
 
 	private Line loadConnect(ResultSet result) throws SQLException {
 		long time = result.getLong("timestamp");;
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
 
 		return new Connect(result.getInt("id"), time, s, result.getInt("connid"), status, auth,
 				result.getString("user"), result.getString("source"),
 				result.getInt("port"), result.getLong("isfreqtime"),
 				result.getInt("isfreqloc"), result.getString("rawline"));
 	}
 
 	@Override
 	public long[] getStartAndEndOfUniverse() throws DataSourceException {
		Select query = this.queryBuilder.select(DSL.min(DSL.fieldByName("entry", "timestamp")).as("start"),
 												DSL.max(DSL.fieldByName("entry", "timestamp")).as("end"))
 												.from("entry");
 		long[] res = new long[2];
 
 		Statement state = null;
 		ResultSet result = null;
 		try {
 			state = connection.createStatement();
 			result = state.executeQuery(query.getSQL());
 			if (result.first()){
 				res[0] = result.getLong("start");
 				res[1] = result.getLong("end");
 			}
 		} catch (SQLException e) {
 			throw new DataSourceException(e);
 		} catch (NumberFormatException e) {
 			throw new DataSourceException(e);
 		} finally {
 			try {
 				if (result != null) {
 					result.close();
 				}
 				if (state != null) {
 					state.close();
 				}
 			} catch (SQLException e) {
 				throw new DataSourceException(e);
 			}
 		}
 		return res;
 	}
 
 	@Override
 	public List<Server> getAllServers() throws DataSourceException {
 		Select query = this.queryBuilder.select(DSL.fieldByName("server",  "id"), DSL.fieldByName("server", "name"), DSL.fieldByName("server", "block"))
 						.from(DSL.tableByName("server"));
 		List<Server> res = new ArrayList<Server>();
 
 		Statement state = null;
 		ResultSet result = null;
 		try {
 			state = connection.createStatement();
 			result = state.executeQuery(query.getSQL());
 			while (result.next()) {
 				res.add(new Server(result.getInt("id"), result.getString("name"), result.getString("block")));
 			}
 		} catch (SQLException e) {
 			throw new DataSourceException(e);
 		} catch (NumberFormatException e) {
 			throw new DataSourceException(e);
 		} finally {
 			try {
 				if (result != null) {
 					result.close();
 				}
 				if (state != null) {
 					state.close();
 				}
 			} catch (SQLException e) {
 				throw new DataSourceException(e);
 			}
 		}
 		return res;
 	}
 
 	@Override
 	public boolean writeComment(long entry_id, String comment)
 			throws DataSourceException {
 		if (entry_id < 0 || comment == null || comment.equals("")){
 			throw new DataSourceException("invalid arguments");
 		}
 		Insert query = this.queryBuilder.insertInto(DSL.tableByName("entry_comment"), DSL.fieldByName("entry_comment", "id"), DSL.fieldByName("entry_comment", "entry_id"), DSL.fieldByName("entry_comment", "text"))
 										.values(DSL.val("DEFAULT"), DSL.val(entry_id), DSL.val(comment));
 
 		PreparedStatement state = null;
 		boolean result = false;
 		try {
 			state = connection.prepareStatement(query.getSQL());
 			List<Object> vars = query.getBindValues();
 			for (int i = 0; i < vars.size(); i++){
 				state.setObject(i+1, vars.get(i));
 			}
 			//state.setLong(1, entry_id);
 			//state.setString(2, comment);
 			int res = state.executeUpdate();
 			result = (res == 1) ? true : false;
 		} catch (SQLException e) {
 			throw new DataSourceException(e);
 		} catch (NumberFormatException e) {
 			throw new DataSourceException(e);
 		} finally {
 			try {
 				if (state != null) {
 					state.close();
 				}
 			} catch (SQLException e) {
 				throw new DataSourceException(e);
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public void destroy() throws DataSourceException {
 		try {
 			if (this.connection != null){
 				connection.close();
 			}
 			if (this.context != null){
 				context.close();
 			}
 		} catch (SQLException e) {
 			throw new DataSourceException(e);
 		} catch (NamingException e) {
 			throw new DataSourceException(e);
 		}
 	}
 
 }
