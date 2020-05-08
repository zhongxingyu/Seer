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
 
 
 			s = plugin.conn.prepareStatement("CREATE TABLE IF NOT EXISTS `AG2-Money` (`id` int(11) NOT NULL AUTO_INCREMENT, `name` varchar(20) NOT NULL, `balance` int(11) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1");
 			s.executeUpdate();
 			s.close();
 
 
 			s = plugin.conn.prepareStatement("CREATE TABLE IF NOT EXISTS `AG2-Wins` (`id` int(11) NOT NULL AUTO_INCREMENT, `name` varchar(20) NOT NULL, `wins` int(11) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1");
 			s.executeUpdate();
 			s.close();
 
 
 			s = plugin.conn.prepareStatement("CREATE TABLE IF NOT EXISTS `AG2-Joins` (`id` int(11) NOT NULL AUTO_INCREMENT, `name` varchar(20) NOT NULL, `joins` int(11) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1");
 			s.executeUpdate();
 			s.close();
 
 
 			s = plugin.conn.prepareStatement("CREATE TABLE IF NOT EXISTS `AG2-Quits` (`id` int(11) NOT NULL AUTO_INCREMENT, `name` varchar(20) NOT NULL, `quits` int(11) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1");
 			s.executeUpdate();
 			s.close();
 
 			plugin.conn.close();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void recordJoin(String name) {
 /*
 		try {
 			plugin.dbConnect();
 			PreparedStatement s = plugin.conn.prepareStatement("INSERT INTO joins (name,time) VALUES ('?','?')");
			s.setString(0, name);
			s.setString(1, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime()));
 			s.executeUpdate();
 			s.close();
 			plugin.conn.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}*/
 
 	}
 
 	public void recordQuit(String name) {
 /*
 		try {
 			plugin.dbConnect();
 			PreparedStatement s = plugin.conn.prepareStatement("INSERT INTO quits (name,time) VALUES ('?','?')");
			s.setString(0, name);
			s.setString(1, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime()));
 			s.executeUpdate();
 			s.close();
 			plugin.conn.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}*/
 	}
 
 	public void setMoney(String name, double d) {
 		plugin.dbConnect();
 		try {
 			PreparedStatement s = plugin.conn.prepareStatement("SELECT balance FROM `AG2-Money` WHERE name='?'");
 			s.setString(1, name);
 			ResultSet result = s.executeQuery();
 			int i=0;
 			while(result.next()){
 				i++;
 			}
 			if(i>0){
 				s = plugin.conn.prepareStatement("UPDATE `AG2-Money` SET balance=`?` WHERE name=`?`");
				s.setDouble(0, d);
				s.setString(1, name);
 				s.executeUpdate();
 				s.close();
 			}
 			plugin.conn.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public double getMoney(String name) {
 		plugin.dbConnect();
 		double money = 0;
 
 		try {
 			PreparedStatement s = plugin.conn.prepareStatement("SELECT balance FROM `AG2-Money` WHERE name='?'");
			s.setString(0, name);
 			ResultSet set = s.executeQuery();
 
 			money = set.getInt("balance");
 
 			s.close();
 			plugin.conn.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return money;
 	}
 
 	public void addWin(String name) {
 		plugin.dbConnect();/*
 		try {
 			PreparedStatement s = plugin.conn.prepareStatement("INSERT INTO wins (name) VALUES ('?')");
			s.setString(0, name);
 			s.executeUpdate();
 			s.close();
 			plugin.conn.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}*/
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
 	private int countResults(ResultSet results) throws SQLException{
 		int i=0;
 		while(results.next()){
 			i++;
 		}
 		return i;
 	}
 }
