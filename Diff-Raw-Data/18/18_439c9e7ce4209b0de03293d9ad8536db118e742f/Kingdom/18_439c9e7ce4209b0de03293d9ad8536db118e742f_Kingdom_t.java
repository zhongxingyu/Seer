 package uk.co.quartzcraft.kingdoms.kingdom;
 
 import java.sql.SQLException;
 import java.util.UUID;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.Bukkit;
 
 import uk.co.quartzcraft.core.entity.QPlayer;
 import uk.co.quartzcraft.kingdoms.QuartzKingdoms;
 import uk.co.quartzcraft.kingdoms.entity.QKPlayer;
 
 public class Kingdom {
 	
 	private static QuartzKingdoms plugin;
 	
 	public void QuartzKingdomsConfig(QuartzKingdoms plugin) {
 		this.plugin = plugin;
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
 	
 	private static boolean exists(String kingdomName) {
 		// TODO Auto-generated method stub
 		return false;
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
 	
 	public static String[] getInfo(String kingdomName) {
 		return null;
 		
 	}
 	
 	public static String getName() {
 		return null;
 		
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
 }
