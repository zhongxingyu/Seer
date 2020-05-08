 package com.isitgeo.randomgift;
 
 import java.io.IOException;
 import java.util.Random;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class RandomGiftGen {
 
 	private RandomGift plugin;
 
 	public RandomGiftGen(RandomGift plugin) {
 		this.plugin = plugin;
 	}
 
 	public void check(Player player) throws IOException {
 
 		if (System.currentTimeMillis() - plugin.cooldown >= plugin.cooldownTime) {
 			if (player.hasPermission("randomgift.trigger")) {
 				getPlayers(player);
 				plugin.cooldown = System.currentTimeMillis();
 			} else {
 				return;
 			}
 
 		}
 	}
 
 	public void getPlayers(Player player) {
             
             Player[] pListTotal = plugin.getServer().getOnlinePlayers();
             
             String pList = "";
             for (Player p : plugin.getServer().getOnlinePlayers()){
                 if (p.hasPermission("randomgift.receive")){
                     pList += p.getName() + " ";
                 }
             }
             
             String[] pListArray = pList.split("\\s+");
             
             if (plugin.allPlayers == true){
                 if (pListTotal.length < plugin.getConfig().getInt("minimum-players")) {
                     return;
                }
            } else {
                if (pListArray.length < plugin.getConfig().getInt("minimum-players")){
                    return;
                 }
             }
       
            
             Random pSelect = new Random();
             int pRand = pSelect.nextInt(pListArray.length);
 
             Player rPlayer = plugin.getServer().getPlayer(pListArray[pRand]);
 		
             if (plugin.broadcastMessage == true) {	
                     plugin.getServer().broadcastMessage(plugin.broadcastTag + rPlayer.getName() + " has been given a random gift!");
             }
 
             rPlayer.sendMessage(plugin.broadcastTag + "Be sure to thank " + player.getName() + " for your random gift!");
             generateGift(rPlayer);
 	}
 
 	@SuppressWarnings("deprecation")
 	public void generateGift(Player rPlayer) {
 
 		Random gSelect = new Random();
 		int gRand = gSelect.nextInt(plugin.gList.length);
 
 		String[] itemQuant = plugin.gList[gRand].split(" ");
 
 		int itemQuantity = Integer.parseInt(itemQuant[1]);
 
 		String[] itemDV;
 
 		if (!(itemQuant[0].contains(":"))) {
 
 			int itemNumber = Integer.parseInt(itemQuant[0]);
 
 			rPlayer.getInventory().addItem(
 					new ItemStack(Material.getMaterial(itemNumber),
 							itemQuantity));
 
 		} else {
 			itemDV = itemQuant[0].split(":");
 
 			int itemNumber = Integer.parseInt(itemDV[0]);
 			int itemDataV = Integer.parseInt(itemDV[1]);
 
 			rPlayer.getInventory().addItem(
 					new ItemStack(Material.getMaterial(itemNumber),
 							itemQuantity, (short) itemDataV));
 		}
 	}
 }
