 package me.FluffyWolfers.Jet;
 
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 public class JetListener implements Listener{
 	
 	@SuppressWarnings("deprecation")
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void addFuel(PlayerInteractEvent e){
 		
 		Player p = e.getPlayer();
 		
 		//if(!p.getGameMode().equals(GameMode.CREATIVE)){
 			
 			if(p.hasPermission("jet.fly")){
 				
 				if(e.getAction().equals(Action.RIGHT_CLICK_AIR)||e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
 
 					if(p.getInventory().getChestplate() != null && p.getInventory().getChestplate().getItemMeta().getDisplayName().equals(ChatColor.DARK_PURPLE + "Jet Pack")){
 
 						ItemStack chest = p.getInventory().getChestplate();
 						ItemMeta im = chest.getItemMeta();
 						
 						if(p.getItemInHand().getType().equals(Material.COAL)){
 
 							String per = ChatColor.stripColor(im.getLore().get(0)).split("/")[0].substring(0, ChatColor.stripColor(im.getLore().get(0)).split("/")[0].length() - 1);
 							
 							int cur = Integer.parseInt(per);
 							int orig = cur;
 							cur+=3;
 							if(cur >= 100){
 								cur = 100;
 							}
 							String str = String.valueOf(cur);
 							if(cur <= 25){
 								ArrayList<String> list = new ArrayList<String>();
 								list.add(ChatColor.DARK_RED + str + " / 100");
 								im.setLore(list);
 								chest.setItemMeta(im);
 								p.sendMessage(ChatColor.DARK_PURPLE + "Your jetpack now has " + ChatColor.DARK_RED + str + ChatColor.DARK_PURPLE  + " fuel!");
 							}
 							if(cur >= 26 && cur <= 75){
 								ArrayList<String> list = new ArrayList<String>();
 								list.clear();
 								list.add(ChatColor.GOLD + str + " / 100");
 								im.setLore(list);
 								chest.setItemMeta(im);
 								p.sendMessage(ChatColor.DARK_PURPLE + "Your jetpack now has " + ChatColor.GOLD + str + ChatColor.DARK_PURPLE + " fuel!");
 							}
 							if(cur >= 76 && cur <= 100){
 								ArrayList<String> list = new ArrayList<String>();
 								list.clear();
 								list.add(ChatColor.GREEN + str + " / 100");
 								im.setLore(list);
 								chest.setItemMeta(im);
 								if(cur != 100)
 									p.sendMessage(ChatColor.DARK_PURPLE + "Your jetpack now has " + ChatColor.GREEN + str + ChatColor.DARK_PURPLE + " fuel!");
 							}
 							p.getInventory().setChestplate(null);
 							p.getInventory().setChestplate(chest);
 							p.updateInventory();
 							if(orig >= 100){
 								p.sendMessage(ChatColor.DARK_PURPLE + "Your jetpack already has " + ChatColor.GREEN + str + ChatColor.DARK_PURPLE + " fuel!");
 							}else{
 								if(p.getInventory().getItemInHand().getAmount()>1){
 									p.getInventory().getItemInHand().setAmount(p.getInventory().getItemInHand().getAmount() - 1);
 								}else{
 									p.getInventory().remove(p.getInventory().getItemInHand());
									p.sendMessage(ChatColor.DARK_PURPLE + "Your jetpack now has " + ChatColor.GREEN + str + ChatColor.DARK_PURPLE + " fuel!");
 								}
 							}
 							
 							if(!JetCraft.flying.contains(p)){
 								JetCraft.flying.add(p);
 							}
 							p.setAllowFlight(true);
 							
 						}
 						
 					}
 					
 				}
 				
 			}
 			
 		//}
 			
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onMove(PlayerMoveEvent e){
 		
 		Player p = e.getPlayer();
 		
 		//if(!p.getGameMode().equals(GameMode.CREATIVE)){
 			
 			if(p.hasPermission("jet.fly")){
 				
 				if(p.getInventory().getChestplate() != null && p.getInventory().getChestplate().getItemMeta().getDisplayName().equals(ChatColor.DARK_PURPLE + "Jet Pack")){
 					
 					ItemStack chest = p.getInventory().getChestplate();
 					ItemMeta im = chest.getItemMeta();
 					
 					String per = ChatColor.stripColor(im.getLore().get(0)).split("/")[0].substring(0, ChatColor.stripColor(im.getLore().get(0)).split("/")[0].length() - 1);
 					
 					if(Integer.parseInt(per) > 0){
 						
 						if(!JetCraft.flying.contains(p)){
 							
 							JetCraft.flying.add(p);
 							
 						}
 						
 						p.setAllowFlight(true);
 						
 					}
 					
 				}
 				
 			}
 			
 		//}
 		
 	}
 	
 }
