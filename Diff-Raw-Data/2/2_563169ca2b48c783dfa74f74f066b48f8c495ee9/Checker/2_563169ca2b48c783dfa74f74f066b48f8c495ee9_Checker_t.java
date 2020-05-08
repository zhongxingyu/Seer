 package me.BlockCat.RankUp;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class Checker {
 	
 
 	public String nextGroup(Player player, String ladder) {
 		try {
 			String[] order = Configuration.getConfig(ladder).getOrder();
 			if (order != null) {
 			for (int i = 0 ; i < order.length; i++) {
 				if (RankUp.permission.playerInGroup(player, order[i])) {
 					continue;
 				} else {
 					return order[i];
 				}
 			}
 
 			//TODO change to cusom message.
 			return "max";
 			}
 			return "Nay1&";
 		} catch(Exception e) {
 			return "Nay1&";
 			
 		}
 	}
 
 	public List<String> getAvailableLadders(Player player) {
 		List<String> list = new ArrayList<String>();
 		List<String> temp = Configuration.getLadders();
 
 		for (String x : temp) {
 			String[] f = Configuration.getConfig(x).getOrder();
 			
 			if (RankUp.permission.playerInGroup(player, f[0])) {
 				list.add(x);
 			}				
 		}
 		if (list.isEmpty()|| list == null) {
 			list = temp;
 		}
 		return list;
 	}
 
 	public void payAndRank(Player player, String group, double cost) {
 
 
 		if (RankUp.economy.has(player.getName(),cost)) {
			RankUp.economy.withdrawPlayer(player.getName(), cost);
 			player.sendMessage(ChatColor.RED + "You have been promoted to: "+ ChatColor.GOLD + group);
 			RankUp.permission.playerAddGroup(player, group);
 
 		} else {
 			player.sendMessage(ChatColor.DARK_RED + "You do not have enough money.");
 		}
 
 
 
 	}
 
 	public boolean ladderAllowed(Player player, String lad) {
 		List<String> x = getAvailableLadders(player);
 		if (x.contains(lad)) {
 			return true;
 		}
 		return false;
 
 	}
 }
