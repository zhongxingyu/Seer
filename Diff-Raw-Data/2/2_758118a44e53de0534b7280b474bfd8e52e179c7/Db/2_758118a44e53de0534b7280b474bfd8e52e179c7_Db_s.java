 package cloudDownload;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.LinkedList;
 import java.util.List;
 
 public class Db {
 	private final static String initUrl = "jdbc:mysql://localhost:3306";
 	private final static String dbName = "cloudDownload";
 	private final static String dbUrl = initUrl + "/" + dbName;
 	private final static String tableName = "tasks";
 	private final static String user = "root";
 	private final static String password = "root";
 	private final static String createDb = "CREATE DATABASE IF NOT EXISTS " + dbName + ";";
 	private final static String useDb = "USE " + dbName + ";";
 	// TODO should add 'reason' column
 	private final static String createTable =
 			"CREATE TABLE IF NOT EXISTS " + tableName + " ("
 			+ "id INT NOT NULL AUTO_INCREMENT,"
 			+ "url VARCHAR(200) NOT NULL,"
 			+ "progress INT(2) NOT NULL DEFAULT '0',"// 0~99
 			+ "state ENUM('pending', 'downloading', 'succeeded', 'failed', 'removed') DEFAULT 'pending',"
			+ "reason VARCHAR(100) DEFAULT ''"
 			+ "size BIGINT DEFAULT '0',"
 			+ "hit INT DEFAULT '0',"
 			+ "retrieveURL VARCHAR(40) DEFAULT '',"// should only contains filename
 			+ "PRIMARY KEY (id)" + ");";
 
 	public static class Task {
 		public int id;
 		public String url;
 		public int progress;
 		public String state;
 		public String reason;
 		public long size;
 		public int hit;
 		public String retrieveURL;
 	}
 
 	public static class RemovalInfo {
 		public int id;
 		public String retrieveURL;
 	}
 
 	public static class TaskInfo {
 		public int id;
 		public boolean isNew;
 	}
 
 	public enum State {pending, downloading, succeeded, failed, removed};
 
 	public static void initDb() throws SQLException {
 		Connection con = DriverManager.getConnection(initUrl, user, password);
 		Statement stm = con.createStatement();
 		stm.addBatch(createDb);
 		stm.addBatch(useDb);
 		stm.addBatch(createTable);
 		stm.executeBatch();
 	}
 
 	public static Task getTask(int id) throws SQLException {
 		Task result = new Task();
 		Connection con = DriverManager.getConnection(dbUrl, user, password);
 
 		PreparedStatement pst = con.prepareStatement("SELECT * FROM " + tableName + " WHERE id = ?");
 		pst.setInt(1, id);
 		ResultSet rs = pst.executeQuery();
 
 		if (rs.next()) {
 			result.id = id;
 			result.url = rs.getString("url");
 			result.progress = rs.getInt("progress");
 			result.state = rs.getString("state");
 			result.reason = rs.getString("reason");
 			result.size = rs.getLong("size");
 			result.hit = rs.getInt("hit");
 			result.retrieveURL = rs.getString("retrieveURL");
 		} else
 			throw new SQLException("no task have id " + id);
 
 		return result;
 	}
 
 	public static synchronized TaskInfo newTask(String taskUrl) throws SQLException {
 		TaskInfo info = new TaskInfo();
 		info.isNew = false;
 		taskUrl = taskUrl.trim();
 		Connection con = DriverManager.getConnection(dbUrl, user, password);
 
 		while (true) {
 			// this is very inefficient, but very simple to understand
 			PreparedStatement pst = con.prepareStatement("SELECT id, state FROM " + tableName + " WHERE url = ?");
 			pst.setString(1, taskUrl);
 			ResultSet rs = pst.executeQuery();
 
 			if (rs.next()) {
 				info.id = rs.getInt("id");
 				if (rs.getString("state").equals("removed")) {
 					info.isNew = true;
 					PreparedStatement pst2 = con.prepareStatement("UPDATE " + tableName + " SET state = 'pending' where id = ?");
 					pst2.setInt(1, info.id);
 					pst2.executeUpdate();
 				}
 				return info;
 			} else {
 				PreparedStatement insert = con.prepareStatement("INSERT INTO " + tableName + "(url) VALUES(?)");
 				insert.setString(1, taskUrl);
 				insert.executeUpdate();
 				info.isNew = true;
 				continue;
 			}
 		}
 	}
 
 	public static void updateProgress(int id, int progress) throws SQLException {
 		Connection con = DriverManager.getConnection(dbUrl, user, password);
 
 		PreparedStatement pst = con.prepareStatement("UPDATE " + tableName + " SET progress = ? where id = ?");
 		pst.setInt(1, progress);
 		pst.setInt(2, id);
 		pst.executeUpdate();
 	}
 
 	public static void changeState(int id, State state, String reason) throws SQLException {
 		Connection con = DriverManager.getConnection(dbUrl, user, password);
 
 		PreparedStatement pst = con.prepareStatement("UPDATE " + tableName + " SET state = ?, reason = ? where id = ?");
 		pst.setString(1, state.toString());
 		pst.setString(2, reason);
 		pst.setInt(3, id);
 		pst.executeUpdate();
 	}
 
 	public static void emptyTask(int id) throws SQLException {
 		Connection con = DriverManager.getConnection(dbUrl, user, password);
 
 		PreparedStatement pst = con.prepareStatement("UPDATE " + tableName + " SET state = ?, size = 0, retrieveURL = '' where id = ?");
 		pst.setString(1, "removed");
 		pst.setInt(2, id);
 		pst.executeUpdate();
 	}
 
 	public static List<RemovalInfo> gatherRemovalInfo(long sizeToFree) throws SQLException {
 		LinkedList<RemovalInfo> result = new LinkedList<RemovalInfo>();
 		Connection con = DriverManager.getConnection(dbUrl, user, password);
 
 		PreparedStatement pst = con.prepareStatement("SELECT id, retrieveURL, size FROM " + tableName + " where "
 				+ "size > 0 && state = 'succeeded' && retrieveURL != ''");
 		ResultSet rs = pst.executeQuery();
 
 		long size = 0;
 		while (rs.next() && size < sizeToFree) {
 			RemovalInfo info = new RemovalInfo();
 			size += rs.getLong("size");
 			info.id = rs.getInt("id");
 			info.retrieveURL = rs.getString("retrieveURL");
 			result.add(info);
 		}
 		return result;
 	}
 
 	public static void startDownload(int id) throws SQLException {
 		changeState(id, State.downloading, "");
 	}
 
 	public static void finishDownload(int id, String retrieveUrl, long size) throws SQLException {
 		changeState(id, State.succeeded, "");
 		Connection con = DriverManager.getConnection(dbUrl, user, password);
 
 		PreparedStatement pst = con.prepareStatement("UPDATE " + tableName + " SET retrieveURL = ?, size = ? where id = ?");
 		pst.setString(1, retrieveUrl);
 		pst.setLong(2, size);
 		pst.setInt(3, id);
 		pst.executeUpdate();
 	}
 
 	public static String retrieve(int id) throws SQLException {
 		// update hit and get retrieveURL, return "" if removed
 		Connection con = DriverManager.getConnection(dbUrl, user, password);
 
 		PreparedStatement pst = con.prepareStatement("UPDATE " + tableName + " SET hit = hit + 1 where id = ?");
 		pst.setInt(1, id);
 		pst.executeUpdate();
 
 		pst = con.prepareStatement("SELECT retrieveURL FROM " + tableName + " WHERE id = ?");
 		pst.setInt(1, id);
 		ResultSet rs = pst.executeQuery();
 
 		if (rs.next())
 			return rs.getString("retrieveURL");
 		else
 			return "";
 	}
 
 	public static long sumSize() throws SQLException {
 		long result = 0;
 		Connection con = DriverManager.getConnection(dbUrl, user, password);
 
 		PreparedStatement pst = con.prepareStatement("SELECT size FROM " + tableName + " where size > 0");
 		ResultSet rs = pst.executeQuery();
 
 		while (rs.next())
 			result += rs.getLong("size");
 		return result;
 	}
 
 	public static void penalizeHit() throws SQLException {
 		// minus hit of all tasks
 		Connection con = DriverManager.getConnection(dbUrl, user, password);
 
 		PreparedStatement pst = con.prepareStatement("UPDATE " + tableName + " SET hit = hit - 1 where hit > 0");
 		pst.executeUpdate();
 	}
 }
