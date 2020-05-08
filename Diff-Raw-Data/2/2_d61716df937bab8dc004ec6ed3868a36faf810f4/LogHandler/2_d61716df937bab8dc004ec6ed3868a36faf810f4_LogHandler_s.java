 package kea.kme.pullpit.server.persistence;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 import kea.kme.pullpit.client.objects.LogEntry;
 
 
 public class LogHandler {
 
 	public static void writeToLog(String userName, int actionType, int objectType, int objectID) throws SQLException {
 		Connection con = DBConnector.getInstance().getConnection();
		PreparedStatement ps = con.prepareStatement("INSERT INTO log VALUES(userName=?, actionType=?, objectType=?, objectID=?, timestamp=?, editDate=?)");
 		ps.setString(1, userName);
 		ps.setInt(2, actionType);
 		ps.setInt(3, objectType);
 		ps.setInt(4, objectID);
 		ps.setString(5, getCurrentTime());
 		ps.setString(6, getCurrentDate());
 		ps.executeUpdate();
 		ps.close();
 	}
 	
 	private static String getCurrentTime() {
 		Date date = Calendar.getInstance().getTime();
 		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		return df.format(date);
 	}
 	
 	private static String getCurrentDate() {
 		Date date = Calendar.getInstance().getTime();
 		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
 		return df.format(date);
 	}
 	
 	public static LogEntry[] getLog(int offset, int limit) throws SQLException {
 		setLogDates();
 		Connection con = DBConnector.getInstance().getConnection();
 		ArrayList<LogEntry> temp = new ArrayList<LogEntry>();
 		String sql = "SELECT DISTINCT userName, actionType, objectType, objectID, editDate FROM pullpitlog ORDER BY timestamp DESC LIMIT " + offset + "," + limit;
 		Statement s = con.createStatement();
 		ResultSet rs = s.executeQuery(sql);
 		while (rs.next()) {
 			String userName = rs.getString("userName");
 			int actionType = rs.getInt("actionType");
 			int objectType = rs.getInt("objectType");
 			int objectID = rs.getInt("objectID");
 			String lastEdit = rs.getString("editDate");
 			temp.add(new LogEntry(userName, actionType, objectType, objectID, lastEdit));
 		}
 		s.close();
 		return temp.toArray(new LogEntry[temp.size()]);
 	}
 	
 	private static void setLogDates() throws SQLException {
 		Connection con = DBConnector.getInstance().getConnection();
 		String sql = "UPDATE pullpitlog SET editDate=timestamp";
 		PreparedStatement ps = con.prepareStatement(sql);
 		ps.executeUpdate();
 		ps.close();
 	}
 }
