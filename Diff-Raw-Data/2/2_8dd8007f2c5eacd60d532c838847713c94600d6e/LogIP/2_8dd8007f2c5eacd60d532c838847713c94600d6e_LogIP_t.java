 package me.cmastudios.plugins.WarhubModChat;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.logging.Logger;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import me.cmastudios.plugins.WarhubModChat.util.Config;
 
 public class LogIP implements Runnable {
	public static CopyOnWriteArrayList<String> loggedPlayers = new CopyOnWriteArrayList<String>();
 
 	@Override
 	public void run() {
 		if (WarhubModChat.playerNameToLog == null)
 			return;
 		if (WarhubModChat.playerIpToLog == null)
 			return;
 		if (Config.config.getString("mysql.password").equals(null)
 				|| Config.config.getString("mysql.password").equals("invalid")) {
 			return;
 		}
 		if (LogIP.loggedPlayers.contains(WarhubModChat.playerNameToLog)) {
 			return;
 		} else {
 			LogIP.loggedPlayers.add(WarhubModChat.playerNameToLog);
 		}
 		String user = Config.config.getString("mysql.username");
 		String pass = Config.config.getString("mysql.password");
 		String url = "jdbc:mysql://" + Config.config.getString("mysql.host")
 				+ ":" + Config.config.getString("mysql.port") + "/"
 				+ Config.config.getString("mysql.database");
 		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
 		Date date = new Date();
 		String time = dateFormat.format(date);
 		Connection conn;
 		try {
 			conn = DriverManager.getConnection(url, user, pass);
 			PreparedStatement sampleQueryStatement = conn
 					.prepareStatement("INSERT INTO `"
 							+ Config.config.getString("mysql.database")
 							+ "`.`iplogs` (`id`, `ip`, `player`, `date`) "
 							+ "VALUES (NULL, '" + WarhubModChat.playerIpToLog
 							+ "', '" + WarhubModChat.playerNameToLog + "', '"
 							+ time + "');");
 			sampleQueryStatement.executeUpdate();
 			sampleQueryStatement.close();
 			conn.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			Logger.getLogger("Minecraft").severe("[WHChat] Cannot add IP to database! " + e);
 		}
 
 	}
 
 }
