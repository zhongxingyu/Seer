 package me.arno.blocklog.database;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 
 import me.arno.blocklog.Config;
 
 public class DatabaseSettings {
 	public static Connection getConnection() {
 		try {
 			Config cfg = new Config();
 			String MySQLHost = cfg.getConfig().getString("mysql.host");
 			String MySQLUser = cfg.getConfig().getString("mysql.username");
 			String MySQLPass = cfg.getConfig().getString("mysql.password");
 			String MySQLDatabase = cfg.getConfig().getString("mysql.database");
 			int MySQLPort = cfg.getConfig().getInt("mysql.port");
 			
 			String MySQLUrl = "jdbc:mysql://" + MySQLHost + ":" + MySQLPort + "/" + MySQLDatabase;
 			
 			Connection conn = DriverManager.getConnection(MySQLUrl, MySQLUser, MySQLPass);
 			return conn;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
