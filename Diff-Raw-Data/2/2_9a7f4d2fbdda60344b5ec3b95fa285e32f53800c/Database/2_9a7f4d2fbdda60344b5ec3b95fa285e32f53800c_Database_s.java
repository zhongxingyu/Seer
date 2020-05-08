 package com.araeosia.ArcherGames.utils;
 
 import com.araeosia.ArcherGames.ArcherGames;
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
 
 			PreparedStatement s = plugin.conn.prepareStatement("CREATE TABLE IF NOT EXISTS `points` (`name` varchar(20), `points` integer)");
 			s.executeUpdate();
 			s.close();
 
 
 			s = plugin.conn.prepareStatement("CREATE TABLE IF NOT EXISTS `money` (`id` int(11) NOT NULL AUTO_INCREMENT, `name` varchar(20) NOT NULL, `balance` int(11) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1");
 			s.executeUpdate();
 			s.close();
 
 
 			s = plugin.conn.prepareStatement("CREATE TABLE IF NOT EXISTS `wins` (`id` int(11) NOT NULL AUTO_INCREMENT, `name` varchar(20) NOT NULL, `wins` int(11) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1");
 			s.executeUpdate();
 			s.close();
 
 			
 			s = plugin.conn.prepareStatement("CREATE TABLE IF NOT EXISTS `plays`(`id` int(11) NOT NULL AUTO_INCREMENT, `name` varchar(20) NOT NULL, `plays` int(11) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1");
 			s.executeUpdate();
 			s.close();
 			
 
 			s = plugin.conn.prepareStatement("CREATE TABLE IF NOT EXISTS `joins` (`id` int(11) NOT NULL AUTO_INCREMENT, `name` varchar(20) NOT NULL, `joins` int(11) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1");
 			s.executeUpdate();
 			s.close();
 
 
 			s = plugin.conn.prepareStatement("CREATE TABLE IF NOT EXISTS `playtime` (`id` int(11) NOT NULL AUTO_INCREMENT, `name` varchar(20) NOT NULL, `time` int(11) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1");
 			s.executeUpdate();
 			s.close();
 			
 			
 			s = plugin.conn.prepareStatement("CREATE TABLE IF NOT EXISTS `deaths` (`id` int(11) NOT NULL AUTO_INCREMENT, `name` varchar(20) NOT NULL, `deaths` int(11) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1");
 			s.executeUpdate();
 			s.close();
 
 			plugin.conn.close();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void recordJoin(String name) {
 		
 		try {
 			plugin.dbConnect();
 			PreparedStatement s = plugin.conn.prepareStatement("INSERT INTO `joins` VALUES ('?','?')");
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
			PreparedStatement s = plugin.conn.prepareStatement("SELECT `time` FROM `joins` WHERE `player`=? ORDER BY `id` DESC");
 			s.setString(1, name);
 			ResultSet rs = s.executeQuery();
 			s.close();
 			
 			if(rs.next()){
 				String timeStamp = rs.getString(1);
 				rs.close();
 				
 				Date joinDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS").parse(timeStamp);
 				
 				Date now = new Date();
 				
 				long played = (now.getTime() - joinDate.getTime()) / 1000;
 				
 				s = plugin.conn.prepareStatement("SELECT EXISTS(SELECT 1 FROM `playtime` WHERE `player` = ?)");
 				s.setString(1, name);
 				rs = s.executeQuery();
 				s.close();
 				if(rs.first()){
 					s = plugin.conn.prepareStatement("UPDATE `playtime` SET `time` = (`time` + ?) WHERE `name` = ?");
 					s.setLong(1, played);
 					s.setString(2, name);
 					s.executeUpdate();
 					s.close();
 					plugin.conn.close();
 				} else {
 					s = plugin.conn.prepareStatement("INSERT INTO `playtime` VALUES(?,?)");
 					s.setString(1, name);
 					s.setLong(2, played);
 					s.executeUpdate();
 					s.close();
 					
 				}
 				s = plugin.conn.prepareStatement("DELETE FROM `joins` WHERE `player`=?");
 				s.setString(1, name);
 				s.executeUpdate();
 				s.close();
 				plugin.conn.close();
 				
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public String getPlayTime(String pName){
 		try {
 			PreparedStatement s = plugin.conn.prepareStatement("SELECT `time` FROM `playtime` WHERE `name`=?");
 			s.setString(1, pName);
 			ResultSet rs = s.executeQuery();
 			s.close();
 			plugin.conn.close();
 			int timeInSeconds = rs.getInt(1);
 			int hours = (timeInSeconds / 60) / 60;
 			int minutes = (timeInSeconds / 60) - (hours * 60);
 			int seconds = (timeInSeconds) - (minutes * 60) - (hours * 3600);
 			return (hours + " hours, " + minutes + " minutes, and " + seconds + " seconds.");
 		} catch(SQLException e){
 			e.printStackTrace();
 			return "";
 		}
 	}
 	
 	public void setMoney(String name, double d) {
 		plugin.dbConnect();
 		try {
 			PreparedStatement s = plugin.conn.prepareStatement("SELECT `balance` FROM `money` WHERE name=?");
 			s.setString(1, name);
 			ResultSet result = s.executeQuery();
 			int i=0;
 			while(result.next()){
 				i++;
 			}
 			if(i>0){
 				s = plugin.conn.prepareStatement("UPDATE `money` SET `balance`=`?` WHERE `name`=`?`");
 				s.setDouble(1, d);
 				s.setString(2, name);
 				s.executeUpdate();
 				s.close();
 				plugin.conn.close();
 			} else {
 				s = plugin.conn.prepareStatement("INSERT INTO `money` VALUES(`balance`=?,`name`=?)");
 				s.setDouble(1, d);
 				s.setString(2, name);
 				s.executeUpdate();
 				s.close();
 				plugin.conn.close();
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	
 	public void takeMoney(String name, double d) {
 		plugin.dbConnect();
 		try {
 			PreparedStatement s = plugin.conn.prepareStatement("SELECT `balance` FROM `money` WHERE name=?");
 			s.setString(1, name);
 			ResultSet result = s.executeQuery();
 			int i=0;
 			while(result.next()){
 				i++;
 			}
 			if(i>0){
 				s = plugin.conn.prepareStatement("UPDATE `money` SET `balance`=(`balance` - ?) WHERE `name`=`?`");
 				s.setDouble(1, d);
 				s.setString(2, name);
 				s.executeUpdate();
 				s.close();
 				plugin.conn.close();
 			} else {
 				s = plugin.conn.prepareStatement("INSERT INTO `money` VALUES(`balance`=?,`name`=?)");
 				s.setDouble(1, -d);
 				s.setString(2, name);
 				s.executeUpdate();
 				s.close();
 				plugin.conn.close();
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	
 	public void addMoney(String name, double d) {
 		plugin.dbConnect();
 		try {
 			PreparedStatement s = plugin.conn.prepareStatement("SELECT `balance` FROM `money` WHERE name=?");
 			s.setString(1, name);
 			ResultSet result = s.executeQuery();
 			int i=0;
 			while(result.next()){
 				i++;
 			}
 			if(i>0){
 				s = plugin.conn.prepareStatement("UPDATE `money` SET `balance`=(`balance` + ?) WHERE `name`=`?`");
 				s.setDouble(1, d);
 				s.setString(2, name);
 				s.executeUpdate();
 				s.close();
 				plugin.conn.close();
 			} else {
 				s = plugin.conn.prepareStatement("INSERT INTO `money` VALUES(`balance`=?,`name`=?)");
 				s.setDouble(1, d);
 				s.setString(2, name);
 				s.executeUpdate();
 				s.close();
 				plugin.conn.close();
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public boolean hasMoney(String name, double d) {
 		plugin.dbConnect();
 		try {
 			PreparedStatement s = plugin.conn.prepareStatement("SELECT `balance` FROM `money` WHERE name=?");
 			s.setString(1, name);
 			ResultSet result = s.executeQuery();
 			if(result.getInt("balance") >= d){
 				return true;
 			}
 			return false;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public double getMoney(String name) {
 		plugin.dbConnect();
 		double money = 0;
 
 		try {
 			PreparedStatement s = plugin.conn.prepareStatement("SELECT `balance` FROM `money` WHERE `name`=?");
 			s.setString(1, name);
 			ResultSet set = s.executeQuery();
 
 			money = set.getInt(1);
 
 			s.close();
 			plugin.conn.close();
 			return money;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return 100000.0;
 	}
 
 	public void addWin(String player) {
 				try {
 			plugin.dbConnect();
 			PreparedStatement s = plugin.conn.prepareStatement("SELECT EXISTS(SELECT 1 FROM `wins` WHERE `player` = ?)");
 			s.setString(1, player);
 			ResultSet rs = s.executeQuery();
 			s.close();
 			if(rs.first()){
 				s = plugin.conn.prepareStatement("UPDATE `wins` SET `wins` = (`wins` + ?) WHERE `name` = ?");
 				s.setInt(1, 1);
 				s.setString(2, player);
 				s.executeUpdate();
 				s.close();
 				plugin.conn.close();
 			} else {
 				s = plugin.conn.prepareStatement("INSERT INTO `wins` VALUES(?,?)");
 				s.setString(1, player);
 				s.setInt(2, 1);
 				s.executeUpdate();
 				s.close();
 				plugin.conn.close();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void addPlay(String player){
 		try {
 			plugin.dbConnect();
 			PreparedStatement s = plugin.conn.prepareStatement("SELECT EXISTS(SELECT 1 FROM `plays` WHERE `plays` = ?)");
 			s.setString(1, player);
 			ResultSet rs = s.executeQuery();
 			s.close();
 			if(rs.first()){
 				s = plugin.conn.prepareStatement("UPDATE `plays` SET `plays` = (`plays` + ?) WHERE `name` = ?");
 				s.setInt(1, 1);
 				s.setString(2, player);
 				s.executeUpdate();
 				s.close();
 				plugin.conn.close();
 			} else {
 				s = plugin.conn.prepareStatement("INSERT INTO `plays` VALUES(?,?)");
 				s.setString(1, player);
 				s.setInt(2, 1);
 				s.executeUpdate();
 				s.close();
 				plugin.conn.close();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public HashMap<String, Integer> getTopMoney() {
 		HashMap<String, Integer> resultant = new HashMap<String, Integer>();
 		try {
 			PreparedStatement s = plugin.conn.prepareStatement("SELECT TOP 10 FROM money ORDER BY balance DESC");
 			ResultSet set = s.executeQuery();
 			
 			while(set.next()){
 				resultant.put(set.getString(1), set.getInt(2));
 			}
 			set.close();
 			s.close();
 			plugin.conn.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return resultant;
 	}
 
 	
 	public int getWins(String name){
 		try{
 			PreparedStatement s = plugin.conn.prepareStatement("SELECT `wins` FROM `wins` WHERE name=?");
 			s.setString(1, name);
 			ResultSet set = s.executeQuery();
 
 			int wins = set.getInt(1);
 
 			s.close();
 			plugin.conn.close();
 			
 			return wins;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return 0;
 	}
 	
 	public int getPlays(String name){
 		try{
 			PreparedStatement s = plugin.conn.prepareStatement("SELECT `plays` FROM `plays` WHERE `name`=?");
 			s.setString(1, name);
 			ResultSet set = s.executeQuery();
 			int plays = set.getInt(1);
 
 			s.close();
 			plugin.conn.close();
 			
 			return plays;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return 0;
 	}
 	
 	public HashMap<String, Integer> getTopWinners(){
 		HashMap<String, Integer> resultant = new HashMap<String, Integer>();
 		try {
 			PreparedStatement s = plugin.conn.prepareStatement("SELECT TOP 10 FROM `wins` ORDER BY `wins` DESC");
 			ResultSet set = s.executeQuery();
 			
 			while(set.next()){
 				resultant.put(set.getString(1), set.getInt(2));
 			}
 			set.close();
 			s.close();
 			plugin.conn.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return resultant;
 	}
 	
 	public int getPoints(String player){
 		try{
 			PreparedStatement s = plugin.conn.prepareStatement("SELECT `points` FROM `points` WHERE `name`=?");
 			s.setString(1, player);
 			ResultSet set = s.executeQuery();
 			int points = set.getInt(1);
 
 			s.close();
 			plugin.conn.close();
 			
 			return points;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return 0;
 	}
 	
 	public void addPoints(String player, int points){
 		try {
 			plugin.dbConnect();
 			PreparedStatement s = plugin.conn.prepareStatement("SELECT EXISTS(SELECT 1 FROM `points` WHERE `player` = ?)");
 			s.setString(1, player);
 			ResultSet rs = s.executeQuery();
 			s.close();
 			if(rs.first()){
 				s = plugin.conn.prepareStatement("UPDATE `points` SET `points` = (`points` + ?) WHERE `name` = ?");
 				s.setInt(1, points);
 				s.setString(2, player);
 				s.executeUpdate();
 				s.close();
 				plugin.conn.close();
 			} else {
 				s = plugin.conn.prepareStatement("INSERT INTO `points` VALUES(?,?)");
 				s.setString(1, player);
 				s.setInt(2, points);
 				s.executeUpdate();
 				s.close();
 				plugin.conn.close();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public HashMap<String, Integer> getTopPoints(){
 		HashMap<String, Integer> resultant = new HashMap<String, Integer>();
 		try {
 			PreparedStatement s = plugin.conn.prepareStatement("SELECT TOP 10 FROM `points` ORDER BY `points` DESC");
 			ResultSet set = s.executeQuery();
 			
 			while(set.next()){
 				resultant.put(set.getString(1), set.getInt(2));
 			}
 			set.close();
 			s.close();
 			plugin.conn.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return resultant;
 	}
 	
 		public int getDeaths(String player){
 		try{
 			PreparedStatement s = plugin.conn.prepareStatement("SELECT `deaths` FROM `deaths` WHERE `name`=?");
 			s.setString(1, player);
 			ResultSet set = s.executeQuery();
 			int points = set.getInt(1);
 
 			s.close();
 			plugin.conn.close();
 			
 			return points;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return 0;
 	}
 	
 	public void addDeath(String player){
 		try {
 			plugin.dbConnect();
 			PreparedStatement s = plugin.conn.prepareStatement("SELECT EXISTS(SELECT 1 FROM `deaths` WHERE `player` = ?)");
 			s.setString(1, player);
 			ResultSet rs = s.executeQuery();
 			s.close();
 			if(rs.first()){
 				s = plugin.conn.prepareStatement("UPDATE `deaths` SET `deaths` = (`deaths` + ?) WHERE `name` = ?");
 				s.setInt(1, 1);
 				s.setString(2, player);
 				s.executeUpdate();
 				s.close();
 				plugin.conn.close();
 			} else {
 				s = plugin.conn.prepareStatement("INSERT INTO `deaths` VALUES(?,?)");
 				s.setString(1, player);
 				s.setInt(2, 1);
 				s.executeUpdate();
 				s.close();
 				plugin.conn.close();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
