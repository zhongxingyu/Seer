 package com.mcprohosting.plugins.imraising;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 
 public class WhitelistHandler {
 	public static void whitelistFromJSON(JSONObject jsonObject) {
 		JSONArray donationJSON = jsonObject.getJSONArray("donation");
 
 		for (int i = 0; i < donationJSON.length(); i++) {
 			String jsonObjectString = donationJSON.get(i).toString();
 			
 			String playerName = jsonObjectString.substring(jsonObjectString.indexOf("custom")+9, jsonObjectString.length()-2);
 			double donationAmount = 0;
 			
 			String message = jsonObjectString.substring(jsonObjectString.indexOf("comment\":")+10, jsonObjectString.indexOf("custom")-3);
 			
 			if (!Bukkit.getWhitelistedPlayers().contains(playerName)) {
 				try {
 					donationAmount = Double.parseDouble(jsonObjectString.substring(jsonObjectString.indexOf("amount:")+11, jsonObjectString.indexOf(",\"screen")));
 				} catch(Exception e) {
 					System.out.println("Invalid data for donation amount from array!");
 				}
 				
 				if (donationAmount >= 25) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "whitelist add " + playerName);
 					
 					whitelistPlayer(playerName);
 					
 					Bukkit.broadcastMessage(ChatColor.GREEN + "Thank you to " + ChatColor.YELLOW + playerName + ChatColor.GREEN + " for donating " + ChatColor.YELLOW + "$" + donationAmount + ChatColor.GREEN + "!");
 					Bukkit.broadcastMessage(ChatColor.GREEN + message);
 				}
 			}
 		}
 	}
 	
 	public static void whitelistPlayer(String playername) {
 		OfflinePlayer player = Bukkit.getOfflinePlayer(playername);
 		
 		if (!Bukkit.getWhitelistedPlayers().contains(player)) {
 			Bukkit.getWhitelistedPlayers().add(player);
 		}
 	}
 }
