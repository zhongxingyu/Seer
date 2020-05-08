 package uk.co.quartzcraft.kingdoms.kingdom;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.Bukkit;
 
 import uk.co.quartzcraft.core.QuartzCore;
 import uk.co.quartzcraft.core.entity.QPlayer;
 import uk.co.quartzcraft.kingdoms.QuartzKingdoms;
 import uk.co.quartzcraft.kingdoms.entity.QKPlayer;
 
 public class Kingdom {
 	
 	private static QuartzKingdoms plugin;
 	
 	public void QuartzKingdomsConfig(QuartzKingdoms plugin) {
 		this.plugin = plugin;
 	}
 	
 	public static int getKingdomID(String playername) {
 		String kingdom = null;
 		Player player = Bukkit.getServer().getPlayer(playername);
 		UUID UUID = player.getUniqueId();
 		String SUUID = UUID.toString();
 		
 		try {
 			Statement s1 = QuartzCore.MySQLcore.openConnection().createStatement();
 			Statement s2 = QuartzKingdoms.MySQLking.openConnection().createStatement();
 			Statement s3 = QuartzKingdoms.MySQLking.openConnection().createStatement();
 			
 	        ResultSet res1 = s1.executeQuery("SELECT * FROM PlayerData WHERE UUID ='" + SUUID + "';");
 	        
 	        if(res1.next()) {
 		        ResultSet res2 = s2.executeQuery("SELECT * FROM KingdomsPlayerData WHERE playerID =" + res1.getInt(1) + ";");
 		        if(res2.next()) {
 		        	int kingdomID = res2.getInt(4);
 		        	return kingdomID;
 		        } else {
 		        	return 0;
 		        }
 	        } else {
 	        	return 0;
 	        }
 	        
 		} catch(SQLException e) {
 			
 		}
 		return 0;
 	}
 	
 	public static String createKingdom(String kingdomName, CommandSender sender) {
 		String name_error = "name_error";
 		String error = "error";
 		Player player = (Player) sender;
 		int userID = QPlayer.getUserID(player);
 		if(exists(kingdomName)) {
 			return name_error;
 		}
 		
 		if(userID == 0) {
 			return error;
 		} 
 		
 		try {
 			java.sql.Connection connection = QuartzKingdoms.MySQLking.openConnection();
 			java.sql.PreparedStatement s = connection.prepareStatement("INSERT INTO Kingdoms (KingdomName, KingID, MemberIDs) VALUES ('" + kingdomName + "', '" + userID + "', '" + userID + "');");
 			if(s.executeUpdate() == 1) {
 				return kingdomName;
 			} else {
 				return error;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return error;
 		}
 	}
 
 	public static String deleteKingdom(String kingdomName, CommandSender sender) {
 		String name_error = "name_error";
 		String king_error = "error";
 		String error = "error";
 		Player player = (Player) sender;
 		int userID = QPlayer.getUserID(player);
 		
 		if(exists(kingdomName)) {
 			
 		} else {
 			return error;
 		}
 		
 		if(QKPlayer.isKing(kingdomName, userID)) {
 			
 		} else {
 			return king_error;
 		}
 		
 		try {
 			java.sql.Connection connection = QuartzKingdoms.MySQLking.openConnection();
 			java.sql.PreparedStatement s = connection.prepareStatement("DELETE FROM Kingdoms WHERE KingdomName = '" + kingdomName + "' AND KingID = '" + userID + "';");
 			if(s.executeUpdate() == 1) {
 				return kingdomName;
 			} else {
 				return error;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return error;
 		}
 		
 	}
 	
 	public static boolean promotePlayer(String kingdomName, CommandSender sender, String playerToPromote, String group) {
 		String[] ranks = null;
 		ranks[0] = "Citizen";
 		ranks[1] = "Knight";
 		ranks[2] = "Nobel";
 		
 		int i = 1;
 		int a = 1;
 		if(i == 1) {
 			for(String rank : ranks) {
 				if(group.equalsIgnoreCase(rank)) {
 					if(QPlayer.addSecondaryGroup(sender, playerToPromote, group)) {
 						return true;
 					} else {
 						return false;
 					}
 				} 
 			}
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public static boolean exists(String kingdomName) {
 		java.sql.Connection connection = QuartzKingdoms.MySQLking.openConnection();
 		try {
			java.sql.PreparedStatement s = connection.prepareStatement("SELECT * FROM Kingdoms WHERE KingdomName =" + kingdomName + ";");
			ResultSet res2 = s.executeQuery();
 			if(res2.next()) {
 		    	 return true;
 		     } else {
 		    	 return false;
 		     }
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public static Map getInfo(String kingdomName) {
 		HashMap<String, String> info = new HashMap<String, String>();
 		
 		java.sql.Connection connection = QuartzKingdoms.MySQLking.openConnection();
 		try {
			java.sql.PreparedStatement s = connection.prepareStatement("SELECT * FROM Kingdoms WHERE KingdomName =" + kingdomName + ";");
			ResultSet res2 = s.executeQuery();
 			if(res2.next()) {
 				info.put("id", res2.getString(1));
 				info.put("Name", res2.getString(2));
 				info.put("Invite Only", res2.getString(3));
 				info.put("King", QPlayer.getDisplayName(QKPlayer.getCoreID(res2.getInt(4))));
 				info.put("Members", res2.getString(5));
 		    	 return info;
 		     } else {
 		    	 return null;
 		     }
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public static String getName(int id) {
 		java.sql.Connection connection = QuartzKingdoms.MySQLking.openConnection();
 		try {
			Statement s = QuartzKingdoms.MySQLking.openConnection().createStatement();
 			ResultSet res2 = s.executeQuery("SELECT * FROM Kingdoms WHERE id =" + id + ";");
 			if(res2.next()) {
 		    	 String kingdomName = res2.getString("KingdomName");
 		    	 return kingdomName;
 		     } else {
 		    	 return null;
 		     }
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	        	
 	}
 	
 	public static boolean setRelationshipStatus(String kingdom, String relatingKingdom, int status) {
 		switch(status) {
 			case 1:
 				//Neutral
 				//Update
 			case 2:
 				//Ally
 				//Update
 			case 3:
 				//War
 				//Update
 			default:
 				//Do nothing
 				return false;
 		}
 		
 	}
 	
 	public boolean setOpen(String kingdomName, boolean status) {
 		
 		if(status = true) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public static int getID(String kingdomName) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	public static boolean addUser(Player player) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 }
