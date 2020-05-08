 package uk.co.quartzcraft.kingdoms.entity;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.UUID;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 import uk.co.quartzcraft.core.QuartzCore;
 import uk.co.quartzcraft.core.entity.QPlayer;
 import uk.co.quartzcraft.kingdoms.QuartzKingdoms;
 import uk.co.quartzcraft.kingdoms.kingdom.Kingdom;
 
 public class QKPlayer {
 
 	public HashMap getData(Player player) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
     /**
      * Retrieves the name of the kingdom that the specified player is in.
      *
      * @param player The player that is being queried.
      * @return Name of the kingdom
      */
 	public static String getKingdom(Player player) {
 		String error = "error";
 		String kingdom = null;
 		//UUID UUID = player.getUniqueId();
 		//String SUUID = UUID.toString();
 		
 		try {
 			Statement s1 = QuartzCore.MySQLcore.openConnection().createStatement();
 			Statement s2 = QuartzKingdoms.MySQLking.openConnection().createStatement();
 			Statement s3 = QuartzKingdoms.MySQLking.openConnection().createStatement();
 
             ResultSet res2 = s2.executeQuery("SELECT * FROM KingdomsPlayerData WHERE PlayerID='" + QPlayer.getUserID(player) + "';");
             if(res2.next()) {
                 int kingdomID = res2.getInt("KingdomID");
                 if(kingdomID == 0) {
                     return null;
                 }
                ResultSet res3 = s3.executeQuery("SELECT * FROM Kingdoms WHERE id=" + kingdomID + ";");
                 if(res3.next()) {
                     kingdom = res3.getString("KingdomName");
                     return kingdom;
                 }
             } else {
                 return null;
             }
 	        
 		} catch(SQLException e) {
             e.printStackTrace();
 			return null;
 		}
 		
 		return null;
 	}
 
 	public static boolean createKingdomsPlayer(Player player) {
 		
 	    try {
    			java.sql.Connection connection = QuartzKingdoms.MySQLking.openConnection();
    			java.sql.PreparedStatement s = connection.prepareStatement("INSERT INTO KingdomsPlayerData (PlayerID) VALUES (" + QPlayer.getUserID(player) +");");
    			if(s.executeUpdate() == 1) {
    				return true;
    			} else {
    				return false;
    			}
    		} catch (SQLException e) {
    			e.printStackTrace();
    			return false;
    		}
 	}
 
     public static boolean kingdom(Player player) {
         try {
             Statement s2 = QuartzKingdoms.MySQLking.openConnection().createStatement();
 
             ResultSet res2 = s2.executeQuery("SELECT * FROM KingdomsPlayerData WHERE PlayerID='" + QPlayer.getUserID(player) + "';");
             if(res2.next()) {
                 int kingdomID = res2.getInt("KingdomID");
                 if(kingdomID == 0) {
                     return false;
                 } else if(Kingdom.exists(kingdomID)) {
                     return true;
                 } else {
                     return false;
                 }
             } else {
                 return false;
             }
 
         } catch(SQLException e) {
             e.printStackTrace();
             return false;
         }
     }
 
     /**
      * Finds out whether a player is a king, can check one kingdom.
      *
      * @param kingdomName This value can be null
      * @param player This is the player you are querying, this is a required value
      * @return boolean
      */
 	public static boolean isKing(String kingdomName, Player player) {
         if(kingdomName != null) {
             java.sql.Connection connection = QuartzKingdoms.MySQLking.openConnection();
             try {
                 Statement s = QuartzKingdoms.MySQLking.openConnection().createStatement();
                 ResultSet res2 = s.executeQuery("SELECT * FROM Kingdoms WHERE id=" + Kingdom.getID(kingdomName) + ";");
                 if(res2.next()) {
                     int kingID = res2.getInt("KingID");
                     if(QKPlayer.getID(player) == kingID) {
                         return true;
                     }
                 } else {
                     return false;
                 }
             } catch (SQLException e) {
                 e.printStackTrace();
                 return false;
             }
         }
         return false;
 	}
 
 	public static int getCoreID(int id) {
 		Statement s;
 		
 		try {
 			s = QuartzKingdoms.MySQLking.openConnection().createStatement();
 			ResultSet res = s.executeQuery("SELECT * FROM KingdomsPlayerData WHERE PlayerID='" + id + "';");
 	        if(res.next()) {
 	        	return res.getInt(2);
 	        } else {
 	        	return 0;
 	        }
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return 0;
 		}
 	}
 	
 	public static int getID(Player player) {
 		Statement s;
 		int userID = QPlayer.getUserID(player);
 		
 		try {
 			s = QuartzKingdoms.MySQLking.openConnection().createStatement();
 			ResultSet res = s.executeQuery("SELECT * FROM KingdomsPlayerData WHERE PlayerID='" + userID + "';");
 	        if(res.next()) {
 	        	return res.getInt(1);
 	        } else {
 	        	return 0;
 	        }
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return 0;
 		}
 	}
 	
 	public static boolean joinKingdom(Player player, String kingdomName) {
 		int userID = getID(player);
 		int kingdomID = Kingdom.getID(kingdomName);
 		
 		try {
 			java.sql.Connection connection = QuartzKingdoms.MySQLking.openConnection();
 			java.sql.PreparedStatement s = connection.prepareStatement("UPDATE KingdomsPlayerData SET KingdomID=" + kingdomID + " WHERE id=" + userID + ";");
             s.executeUpdate();
             return true;
 		} catch (SQLException e) {
 			return false;
 		}
 	}
 	
 	public static boolean leaveKingdom(Player player, String kingdomName) {
         try {
             java.sql.Connection connection = QuartzKingdoms.MySQLking.openConnection();
             java.sql.PreparedStatement s = connection.prepareStatement("UPDATE KingdomsPlayerData SET KingdomID=0 WHERE id=" + QKPlayer.getID(player) + ";");
             if(s.executeUpdate() == 1) {
                 return true;
             } else {
                 return false;
             }
         } catch (SQLException e) {
             e.printStackTrace();
             return false;
         }
 	}
 	
 	public static int getKingdomID(Player player) {
 		String kingdom = null;
 		UUID UUID = player.getUniqueId();
 		String SUUID = UUID.toString();
         int playerID = QPlayer.getUserID(player);
 		
 		try {
 			Statement s1 = QuartzCore.MySQLcore.openConnection().createStatement();
 			Statement s2 = QuartzKingdoms.MySQLking.openConnection().createStatement();
 			Statement s3 = QuartzKingdoms.MySQLking.openConnection().createStatement();
 
             ResultSet res2 = s2.executeQuery("SELECT * FROM KingdomsPlayerData WHERE PlayerID=" + playerID + ";");
             if(res2.next()) {
                 int kingdomID = res2.getInt("KingdomID");
                 return kingdomID;
             } else {
                 return 0;
             }
 	        
 		} catch(SQLException e) {
 			e.printStackTrace();
             return 0;
 		}
 	}
 
     public static int getPower(Player player) {
         try {
             Statement s1 = QuartzCore.MySQLcore.openConnection().createStatement();
             Statement s2 = QuartzKingdoms.MySQLking.openConnection().createStatement();
             Statement s3 = QuartzKingdoms.MySQLking.openConnection().createStatement();
 
             ResultSet res1 = s1.executeQuery("SELECT * FROM PlayerData WHERE UUID ='" + player.getUniqueId().toString() + "';");
 
             if(res1.next()) {
                 ResultSet res2 = s2.executeQuery("SELECT * FROM KingdomsPlayerData WHERE playerID =" + res1.getInt("id") + ";");
                 if(res2.next()) {
                     int power = res2.getInt("Power");
                     return power;
                 } else {
                     return 0;
                 }
             } else {
                 return 0;
             }
 
         } catch(SQLException e) {
             return 0;
         }
     }
 
     public static boolean setPower(Player player, boolean addRemove, int amount) {
         int currentPower = QKPlayer.getPower(player);
         int powerAmount;
         if(addRemove) {
             powerAmount = currentPower + amount;
         } else {
             powerAmount = currentPower - amount;
         }
         if(powerAmount > 50) {
             powerAmount = 50;
         } else if(powerAmount < -50) {
             powerAmount = -50;
         }
         try {
             java.sql.Connection connection = QuartzKingdoms.MySQLking.openConnection();
             java.sql.PreparedStatement s = connection.prepareStatement("UPDATE KingdomsPlayerData SET Power=" + powerAmount + " WHERE id=" + QKPlayer.getID(player) + ");");
             if(s.executeUpdate() == 1) {
                 return true;
             } else {
                 return false;
             }
         } catch (SQLException e) {
             return false;
         }
     }
 
 }
