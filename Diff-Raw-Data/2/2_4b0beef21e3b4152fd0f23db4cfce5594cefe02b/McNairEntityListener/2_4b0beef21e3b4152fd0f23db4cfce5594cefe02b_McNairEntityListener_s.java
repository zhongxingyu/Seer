 package com.sadmean.mc.McNairMinecraftManager;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityListener;
 
 public class McNairEntityListener extends EntityListener {
 	
 	public static McNairMinecraftManager plugin; public McNairEntityListener(McNairMinecraftManager instance) { 
 		plugin = instance;
 	}
 	
 	public void onEntityDeath(EntityDeathEvent event) {
 		//LavaItemCollection.getThisPlugin().getServer().broadcastMessage("DEBUG:" + event.getEntity().getLastDamageCause().getCause().name());
 		if(event.getEntity() instanceof Player) {
 			Player player = (Player) event.getEntity();
			if(player.getName() == "jarinex") {
 				player.getServer().broadcastMessage("Jarinex died! Its time to " + ChatColor.DARK_PURPLE + "P" + ChatColor.GREEN + "A" + ChatColor.GOLD + "R" + ChatColor.BLUE + "T" + ChatColor.RED + "Y" + ChatColor.AQUA + "!");
 				uselessFunctions.randomzizeSheep();
 			}
 				//event.getDrops().clear();
 		}
 		
 	}
 }
