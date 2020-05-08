 package me.FluffyWolfers.Jet;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.scheduler.BukkitRunnable;
 
 public class JetFuelCheck extends BukkitRunnable {
 
 	@SuppressWarnings("deprecation")
 	public void run(){
 		
 		if(!JetCraft.flying.isEmpty()){
 			
 			for(int i = 0; i < JetCraft.flying.size(); i++){
 				
 				Player p = JetCraft.flying.get(0);
 				
 				//if(!p.getGameMode().equals(GameMode.CREATIVE)){
 					
 					if(p.getInventory().getChestplate() != null){
 						
 						if(p.isFlying() && !p.isOnGround()){
 							
 							ItemStack chest = p.getInventory().getChestplate();
 							ItemMeta meta = chest.getItemMeta();
 							List<String> lore = meta.getLore();
 							
 							String per = ChatColor.stripColor(lore.get(0)).split("/")[0].substring(0, ChatColor.stripColor(lore.get(0)).split("/")[0].length() - 1);
 							
 							int val = Integer.parseInt(per);
 							
 							if(val <= 0){
 								
 								p.setFlying(false);
 								p.setAllowFlight(false);
 								
 								JetCraft.flying.remove(p);
 								JetCraft.time.remove(p);
 								
 								p.sendMessage(ChatColor.DARK_PURPLE + "Your Jet Pack ran out of fuel!");
 								
 							}else{
 								
 								if(!JetCraft.time.containsKey(p)){
 									
 									int sec = JetCraft.j.getConfig().getInt("options.seconds-to-burn-fuel");
 									JetCraft.time.put(p, System.currentTimeMillis() + (sec * 1000));
 									
 								}else{
 									
 									long time = JetCraft.time.get(p);
 									
 									if(System.currentTimeMillis() >= time){
 										
 										int cur = Integer.parseInt(per);
										int amm = JetCraft.j.getConfig().getInt("options.ammount-of-fuel-to-burn");
 										cur -= amm;
 										String str = String.valueOf(cur);
 										if(cur <= 25){
 											ArrayList<String> list = new ArrayList<String>();
 											list.add(ChatColor.DARK_RED + str + " / 100");
 											meta.setLore(list);
 											chest.setItemMeta(meta);
 										}
 										if(cur >= 26 && cur <= 75){
 											ArrayList<String> list = new ArrayList<String>();
 											list.clear();
 											list.add(ChatColor.GOLD + str + " / 100");
 											meta.setLore(list);
 											chest.setItemMeta(meta);
 										}
 										if(cur >= 76 && cur <= 100){
 											ArrayList<String> list = new ArrayList<String>();
 											list.clear();
 											list.add(ChatColor.GREEN + str + " / 100");
 											meta.setLore(list);
 											chest.setItemMeta(meta);
 										}
 										
 										if(cur == 15){
 											
 											p.sendMessage(ChatColor.DARK_RED + "WARNING: YOUR FUEL IS RUNNING LOW! PLEASE REFUEL YOUR JET PACK! YOU HAVE 15 LEFT!");
 											
 										}
 										
 										if(cur == 10){
 											
 											p.sendMessage(ChatColor.DARK_RED + "WARNING: YOUR FUEL IS RUNNING LOW! PLEASE REFUEL YOUR JET PACK! YOU HAVE 10 LEFT!");
 											
 										}
 										
 										if(cur == 5){
 											
 											p.sendMessage(ChatColor.DARK_RED + "WARNING: YOUR FUEL IS RUNNING LOW! PLEASE REFUEL YOUR JET PACK! YOU HAVE 5 LEFT!");
 											
 										}
 										
 										if(cur == 2){
 											
 											p.sendMessage(ChatColor.DARK_RED + "WARNING: YOUR FUEL IS RUNNING LOW! PLEASE REFUEL YOUR JET PACK! YOU HAVE 2 LEFT!");
 											
 										}
 										
 										if(cur == 1){
 											
 											p.sendMessage(ChatColor.DARK_RED + "WARNING: YOUR FUEL IS RUNNING LOW! PLEASE REFUEL YOUR JET PACK! YOU HAVE 1 LEFT!");
 											
 										}
 										
 										p.getInventory().setChestplate(null);
 										p.getInventory().setChestplate(chest);
 										p.updateInventory();
 										
 										JetCraft.time.remove(p);
 										int sec = JetCraft.j.getConfig().getInt("options.seconds-to-burn-fuel");
 										JetCraft.time.put(p, System.currentTimeMillis() + (sec * 1000));
 										
 									}
 									
 								}
 								
 							}
 							
 							Material type = chest.getType();
 							
 							if(type.equals(Material.LEATHER_CHESTPLATE)){
 								p.setFlySpeed(0.075F);
 							}
 							if(type.equals(Material.IRON_CHESTPLATE)){
 								p.setFlySpeed(0.085F);
 							}
 							if(type.equals(Material.GOLD_CHESTPLATE)){
 								p.setFlySpeed(0.095F);
 							}
 							if(type.equals(Material.DIAMOND_CHESTPLATE)){
 								p.setFlySpeed(0.115F);
 							}
 							
 						}
 						
 					}else{
 						
 						p.setFlySpeed(0.1F);
 						
 						p.setFlying(false);
 						p.setAllowFlight(false);
 						
 						JetCraft.flying.remove(p);
 						JetCraft.time.remove(p);
 						
 					}
 					
 				//}
 				
 			}
 			
 		}
 		
 	}
 }
