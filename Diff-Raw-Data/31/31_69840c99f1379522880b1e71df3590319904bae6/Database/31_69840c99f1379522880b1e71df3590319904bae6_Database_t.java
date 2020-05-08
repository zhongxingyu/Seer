 package com.araeosia.ArcherGames.utils;
 
 import com.araeosia.ArcherGames.ArcherGames;
 import java.lang.reflect.Array;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import java.util.HashMap;
 
 /**
  *
  * @author Bruce, Daniel
  */
 public class Database {
 
 	public ArcherGames plugin;
 
 	public Database(ArcherGames plugin) {
 		this.plugin = plugin;
 
 		//Init tables
 
 		plugin.dbConnect();
 		try {
 
 			PreparedStatement s = plugin.conn.prepareStatement("CREATE TABLE IF NOT EXISTS points(name varchar(20), points integer)");
 			s.executeUpdate();
 			s.close();
 
 
 			s = plugin.conn.prepareStatement("CREATE TABLE IF NOT EXISTS money(name varchar(20), points integer)");
 			s.executeUpdate();
 			s.close();
 
 
 			s = plugin.conn.prepareStatement("CREATE TABLE IF NOT EXISTS wins(id NOT NULL AUTO_INCREMENT, name varchar(20))");
 			s.executeUpdate();
 			s.close();
 
 
 			s = plugin.conn.prepareStatement("CREATE TABLE IF NOT EXISTS joins(id NOT NULL AUTO_INCREMENT, name varchar(20), date varchar(20))");
 			s.executeUpdate();
 			s.close();
 
 
 			s = plugin.conn.prepareStatement("CREATE TABLE IF NOT EXISTS quits(id NOT NULL AUTO_INCREMENT, name varchar(20), date varchar(20))");
 			s.executeUpdate();
 			s.close();
 
 			plugin.conn.close();
 
 		} catch (Exception e) {
			e.printStackTrace();
 		}
 	}
 
 	public void addPoints(String name, int points) throws SQLException {
 		plugin.dbConnect();
 
		PreparedStatement s = plugin.conn.prepareStatement("INSERT INTO points (name,points) VALUES ('?','?')");
 		s.setString(1, name);
 		s.setInt(2, points);
 		s.executeUpdate();
 		s.close();
 		plugin.conn.close();
 	}
 
 	public void recordJoin(String name) {
 
 		try {
 			plugin.dbConnect();
			PreparedStatement s = plugin.conn.prepareStatement("INSERT INTO joins (name,time) VALUES ('?','?')");
 			s.setString(1, name);
 			s.setString(2, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime()));
 			s.executeUpdate();
 			s.close();
 			plugin.conn.close();
 		} catch (SQLException e) {
			e.printStackTrace();
 		}
 
 	}
 
 	public void recordQuit(String name) {
 
 		try {
 			plugin.dbConnect();
			PreparedStatement s = plugin.conn.prepareStatement("INSERT INTO quits (name,time) VALUES ('?','?')");
 			s.setString(1, name);
 			s.setString(2, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime()));
 			s.executeUpdate();
 			s.close();
 			plugin.conn.close();
 		} catch (SQLException e) {
			e.printStackTrace();
 		}
 	}
 
 	public void setMoney(String name, double d) {
 		plugin.dbConnect();
 		try {
			PreparedStatement s = plugin.conn.prepareStatement("INSERT INTO money (name,balance) VALUES ('?','?')");
 			s.setString(1, name);
 			s.setDouble(2, d);
 			s.executeUpdate();
 			s.close();
 			plugin.conn.close();
 		} catch (SQLException e) {
			e.printStackTrace();
 		}
 	}
 
 	public double getMoney(String name) {
 		plugin.dbConnect();
 		double money = 0;
 
 		try {
			PreparedStatement s = plugin.conn.prepareStatement("SELECT balance FROM money WHERE name='?'");
 			s.setString(1, name);
 			ResultSet set = s.executeQuery();
 
 			money = set.getDouble(1);
 
 			s.close();
 			plugin.conn.close();
 		} catch (SQLException e) {
			e.printStackTrace();
 		}
 
 		return money;
 	}
 
 	public void addWin(String name) {
 		plugin.dbConnect();
 		try {
			PreparedStatement s = plugin.conn.prepareStatement("INSERT INTO wins (name) VALUES ('?')");
 			s.setString(1, name);
 			s.executeUpdate();
 			s.close();
 			plugin.conn.close();
 		} catch (SQLException e) {
			e.printStackTrace();
 		}
 	}
 
 	public HashMap<String, Integer> getTopPlayers() {
 		HashMap<String, Integer> resultant = new HashMap<String, Integer>();
 		try {
 			PreparedStatement s = plugin.conn.prepareStatement("SELECT * FROM money ORDER BY balance DESC LIMIT 10");
 			ResultSet set = s.executeQuery();
 		} catch (SQLException e) {
			e.printStackTrace();
 		}
 		return resultant;
 	}
 }
