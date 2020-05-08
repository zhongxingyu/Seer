 package com.araeosia.ArcherGames.utils;
 
 import com.araeosia.ArcherGames.ArcherGames;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.bukkit.entity.Player;
 
 /**
  * 
  * @author Bruce, Daniel
  */
 public class Database {
 	
 	public ArcherGames plugin;
 	
 	public Database(ArcherGames plugin){
 		this.plugin = plugin;
 	}
 	
 	public void setupTables() throws SQLException{
 		plugin.dbConnect();
 		
 		PreparedStatement s = plugin.conn.prepareStatement("CREATE TABLE IF NOT EXISTS points(name varchar(20), points integer)");
 		s.executeUpdate();
 		s.close();
 	}
 	public void addPoints(String name, int points) throws SQLException{
 		plugin.dbConnect();
 		
 		PreparedStatement s = plugin.conn.prepareStatement("INSERT INTO points (name,points) VALUES (?,?)");
 		s.setString(1, name);
 		s.setInt(2, points);
 		s.executeUpdate();
 		s.close();
 		plugin.conn.close();
 	}
 
 	public void recordJoin(String name) {
 		plugin.dbConnect();
 		
 		
 	}
 }
