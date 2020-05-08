 package com.cole2sworld.ColeBans;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Map;
 
 import com.unibia.simplemysql.SimpleMySQL;
 
 /**
  * Manages logging of actions.
  * @author cole2
  *
  */
 public class LogManager {
 	public static enum Type {
 		BAN, UNBAN, TEMPBAN, KICK, SWITCH, LOCAL_BAN, OTHER, UNKNOWN;
 		public static Type forOrdinal(int ordinal) {
 			return values()[ordinal];
 		}
 	}
 	private static String tablePrefix;
 	private static SimpleMySQL sql;
 	private static String tbl;
 	private static boolean initialized = false;
 	/**
 	 * Initialize LogManager - connect to MySQL, set up variables, etc.
 	 */
 	public static void initialize() {
 		if (initialized) return;
 		if (!Main.instance.banHandler.getTruncatedName().equals("mysql")) {
 			Main.LOG.warning(GlobalConf.logPrefix+" [ActionLogger] Could not initalize - current ban handler is not MySQL.");
 			return;
 		}
 		Map<String, String> data = Main.getBanHandlerInitArgs();
 		sql = new SimpleMySQL();
 		sql.enableReconnect();
 		sql.setReconnectNumRetry(25);
 		sql.connect(data.get("host"), data.get("username"), data.get("password"));
 		sql.use(data.get("db"));
 		tablePrefix = data.get("prefix");
 		tbl = tablePrefix+"log";
 		initialized = true;
 	}
 	/**
 	 * De-initialize LogManager - just disconnects from MySQL.
 	 */
 	public static void deinitialize() {
 		if (!initialized) return;
 		sql.close();
 		sql = null;
 		initialized = false;
 	}
 	/**
 	 * Add an entry to the log, using the current time.<br/>
 	 * <i>Ban handlers should <b>never</b> call this, unless logging a 'UNKNOWN' or 'OTHER' action. All the command classes will log for you.</i>
 	 * @param type Type of action being logged (LogManager.Type)
 	 * @param admin Admin that took the action
 	 * @param victim Victim of the action
 	 */
 	public static void addEntry(Type type, String admin, String victim) {
 		verify();
 		PreparedStatement stmt = sql.prepare("INSERT INTO "+tbl+" (type, admin, victim, time) VALUES (?, ?, ?, ?);");
 		try {
 			stmt.setInt(1, type.ordinal());
 			stmt.setString(2, admin);
 			stmt.setString(3, victim);
 			stmt.setLong(4, System.currentTimeMillis());
 			stmt.execute();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				stmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	/**
 	 * Get all actions of the specified type
 	 * @param type Type to look up
 	 * @return All results
 	 */
 	public static List<LogEntry> getAll(Type type) {
 		verify();
 		ArrayList<LogEntry> entries = new ArrayList<LogEntry>();
 		PreparedStatement stmt = sql.prepare("SELECT * FROM "+tbl+" WHERE type=?");
 		try {
 			stmt.setInt(1, type.ordinal());
 			ResultSet result = stmt.executeQuery();
 			for (; result.next();) {
 				entries.add(new LogEntry(
 						Type.forOrdinal(result.getInt("type")),
 						result.getString("admin"),
 						result.getString("victim"),
 						result.getLong("time")
 						));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				stmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 	/**
 	 * Get all actions made by a specific admin
 	 * @param admin The admin to lookup
 	 * @return All results
 	 */
 	public static List<LogEntry> getBy(String admin) {
 		verify();
 		ArrayList<LogEntry> entries = new ArrayList<LogEntry>();
 		PreparedStatement stmt = sql.prepare("SELECT * FROM "+tbl+" WHERE admin=?");
 		try {
 			stmt.setString(1, admin);
 			ResultSet result = stmt.executeQuery();
 			for (; result.next();) {
 				entries.add(new LogEntry(
 						Type.forOrdinal(result.getInt("type")),
 						result.getString("admin"),
 						result.getString("victim"),
 						result.getLong("time")
 						));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				stmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 	/**
 	 * Get all actions made on a specific victim
 	 * @param victim The victim to lookup
 	 * @return All results
 	 */
 	public static List<LogEntry> getTo(String victim) {
 		verify();
 		ArrayList<LogEntry> entries = new ArrayList<LogEntry>();
 		PreparedStatement stmt = sql.prepare("SELECT * FROM "+tbl+" WHERE victim=?");
 		try {
 			stmt.setString(1, victim);
 			ResultSet result = stmt.executeQuery();
 			for (; result.next();) {
 				entries.add(new LogEntry(
 						Type.forOrdinal(result.getInt("type")),
 						result.getString("admin"),
 						result.getString("victim"),
 						result.getLong("time")
 						));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				stmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 	/**
 	 * Get all actions made on a specific victim, by a specific admin
 	 * @param admin Admin to lookup
 	 * @param victim Victim to lookup
 	 * @return All results
 	 */
 	public static List<LogEntry> getByOn(String admin, String victim) {
 		verify();
 		ArrayList<LogEntry> entries = new ArrayList<LogEntry>();
 		PreparedStatement stmt = sql.prepare("SELECT * FROM "+tbl+" WHERE (admin=?, victim=?)");
 		try {
 			stmt.setString(1, admin);
 			stmt.setString(2, victim);
 			ResultSet result = stmt.executeQuery();
 			for (; result.next();) {
 				entries.add(new LogEntry(
 						Type.forOrdinal(result.getInt("type")),
 						result.getString("admin"),
 						result.getString("victim"),
 						result.getLong("time")
 						));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				stmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 	/**
 	 * Cut a list of LogEntrys to a certain time
 	 * @param timeMillis Time to cut to
 	 * @return Modified list
 	 */
 	public static List<LogEntry> since(long timeMillis, List<LogEntry> oldlist) {
 		ArrayList<LogEntry> list = new ArrayList<LogEntry>();
 		for (LogEntry entry : oldlist) {
 			if (entry.getTime() >= timeMillis) {
 				list.add(entry);
 			}
 		}
 		return list;
 	}
 	private static void verify() {
 		initialize();
 		if (!sql.checkTable(tbl)) {
			sql.query("CREATE  TABLE "+tbl+".`new_table` ("+
 					"`type` INT UNSIGNED NOT NULL ,"+
 					"`admin` VARCHAR(45) NULL ,"+
 					"`victim` VARCHAR(45) NULL ,"+
 					"`time` BIGINT UNSIGNED NULL ,"+
 					"INDEX `main` (`type` ASC, `admin` ASC, `victim` ASC, `time` ASC) );");
 		}
 	}
 }
