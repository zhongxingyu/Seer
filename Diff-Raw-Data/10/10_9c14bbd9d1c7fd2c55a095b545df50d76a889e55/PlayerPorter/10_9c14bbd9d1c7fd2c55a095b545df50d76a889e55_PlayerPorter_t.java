 package com.imdeity.deitynether.util;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.imdeity.deitynether.DeityNether;
 
 public class PlayerPorter {
 
 	private DeityNether plugin = null;
 	
 	
 	public PlayerPorter(DeityNether instance){
 		plugin = instance;
 	}
 	
 	public void sendToNether(Player player){
 		if(player.hasPermission(DeityNether.GENERAL_PERMISSION)){
 			if(player.getWorld() == plugin.getServer().getWorld(plugin.config.getNetherWorldName())){
 				player.sendMessage(DeityNether.HEADER + "cYou are already in the nether");
 			}else{
 				//TODO test time left till next port
 				int result = testPlayerInventory(player);
 				if(result == 1){
					player.getInventory().removeItem(new ItemStack(Material.GOLD_BLOCK, plugin.config.getNeededGold()));
 					player.sendMessage(DeityNether.HEADER + "aTeleporting you to the nether...");
 					player.teleport(plugin.config.getNetherWorldSpawn());
				}else if(result == 0){ //not enough gold
 					player.sendMessage(DeityNether.HEADER + "cYou do not have enough gold to go to the nether. Go get more.");
				}else if(result == -1){ //Illegal items
					player.sendMessage(DeityNether.HEADER + "cYou have items in your inventory that are not allowed in the nether. Remove them and try again");
 				}
 			}
 		}else if(player.hasPermission(DeityNether.OVERRIDE_PERMISSION)){
 			player.sendMessage(DeityNether.HEADER + "aTeleporting you to the nether...");
 			player.teleport(plugin.config.getNetherWorldSpawn());
 		}else{
 			player.sendMessage(DeityNether.HEADER + "cYou don't have permission");
 		}
 	}
 	
 	public void sendToOverworld(Player player){
 		if(player.getWorld() == plugin.getServer().getWorld(plugin.config.getMainWorldName())){
 			player.sendMessage(DeityNether.HEADER + "cYou aren't in the nether");
 		}else{
 			player.sendMessage(DeityNether.HEADER + "aTeleporting you to the main world...");
 			player.teleport(plugin.config.getMainWorldSpawn());
 		}
 	}
 	
 	private int testPlayerInventory(Player p){
 		int gold = 0;
 		for(ItemStack i : p.getInventory()){
 			if(i != null)
 				if(i.getType() == Material.GOLD_BLOCK){
 					gold += i.getAmount();
 				}else{
 					if(!AllowedItems.contains(i.getTypeId())) return -1;
 				}
 		}
 		
 		if(gold >= plugin.config.getNeededGold()){
 			return 1;
 		}else{
 			return 0;
 		}
 	}
 	
 }
