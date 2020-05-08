 package com.Zolli.EnderCore.Storage;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Map;
 
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 import com.Zolli.EnderCore.Storage.Storage.storageEngine;
 import com.Zolli.EnderCore.Utils.tagHelper;
 
 public class storageActions {
 
 	private Storage storage;
 	private Connection conn;
 	private storageEngine selectedEngine;
 	private tagHelper tagHelper;
 	private YamlConfiguration ffStorage;
 	
 	public storageActions(Storage s) {
 		this.storage = s;
 		this.tagHelper = new tagHelper();
 		this.selectedEngine = this.storage.getSelectedEngine();
 		conn = this.storage.getConnection();
 		
 		if(selectedEngine.equals(storageEngine.FLATFILE)) {
 			this.ffStorage = storage.getFfStorage();
 		}
 	}
 	
 	public boolean addPlayer(Player pl) {
 		switch(this.selectedEngine) {
 			case MySQL:
 			case SQLITE:
 			case H2DB:
 				try {
 					PreparedStatement pstmt = conn.prepareStatement("INSERT INTO `players` (playerName,dragonDefeated) VALUES(?, ?)");
 					pstmt.setString(1, pl.getName());
 					pstmt.setString(2, "0");
 					pstmt.executeUpdate();
 					return true;
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			break;
 			case FLATFILE:
 				if(this.ffStorage.contains(pl.getName())) {
 					this.ffStorage.set(pl.getName() + ".dragonDefeated", "0");
 				}
 			break;
 			case NBT:
 				
 			break;
 		default:
 			break;
 		}
 		return false;
 	}
 	
 	public boolean setDefeated(String name, boolean b) {
 		switch(this.selectedEngine) {
 			case MySQL:
 			case SQLITE:
 			case H2DB:
 				try {
 					PreparedStatement pstmt = conn.prepareStatement("UPDATE `players` SET dragonDefeated=?");
 					if(b) {
 						pstmt.setString(1, "1");
 					} else {
 						pstmt.setString(1, "0");
 					}
 					pstmt.executeUpdate();
 					return true;
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 				break;
 		default:
 			break;
 		}
 		return false;
 	}
 	
 	public boolean setTag(String newTags) {
 		switch (this.selectedEngine) {
 		case MySQL:
 		case SQLITE:
 			try {
 				Statement stmt = conn.createStatement();
 				int rows = stmt.executeUpdate("UPDATE `users` SET uniqueTags = CONCAT(uniqueTags, '" + newTags + "')");
 				if(rows == 1) {
 					return true;
 				} else {
 					return false;
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			return false;
 		default:
 			break;
 		}
 		return false;
 	}
 	
	@SuppressWarnings("unchecked")
 	public Map<String, String> getTag(Player pl) {
 		String object = null;
 		
 		switch (this.selectedEngine) {
 		case MySQL:
 		case SQLITE:
 			try {
 				Statement stmt = conn.createStatement();
 				ResultSet rs = stmt.executeQuery("SELECT uniqueTags FROM `users` WHERE userName=" + pl.getName() + " LIMIT 1");
 				object = rs.getString(0);
 				stmt.close();
 				return tagHelper.createMap(object);
 				
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			return null;
 		default:
 			break;
 		}
 		return null;
 	}
 }
