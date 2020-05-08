 package com.zolli.rodolffoutilsreloaded.listeners;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Spider;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 
 import com.zolli.rodolffoutilsreloaded.rodolffoUtilsReloaded;
 import com.zolli.rodolffoutilsreloaded.utils.configUtils;
 
 public class playerListener implements Listener {
 	
 	private rodolffoUtilsReloaded plugin;
 	public configUtils cu;
 	public playerListener(rodolffoUtilsReloaded instance) {
 		plugin = instance;
 		cu = new configUtils(instance);
 	}
 	
 	
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void overrideBukkitDefaults(PlayerCommandPreprocessEvent e) {
 		
 		String command = e.getMessage();
 		Player commandSender = e.getPlayer();
 		
 		if((!commandSender.isOp() || !plugin.perm.has(commandSender, "rur.allowSeeBukkitVer")) && (command.equalsIgnoreCase("/ver") || command.equalsIgnoreCase("/version"))) {
 			commandSender.sendMessage(plugin.config.getString("fakePluginsList"));
 			e.setCancelled(true);
 		}
 		
 		if((!commandSender.isOp() || !plugin.perm.has(commandSender, "rur.allowSeeRealPlugins")) && (command.equalsIgnoreCase("/pl") || command.equalsIgnoreCase("/plugins"))) {
 			commandSender.sendMessage(plugin.config.getString("fakeBukkitVerString"));
 			e.setCancelled(true);
 		}
 		
 		if(command.equalsIgnoreCase("/reload")) {
 			commandSender.sendMessage(ChatColor.DARK_RED + "Ne használd ezt a prancsot, inkább indítsd újra a szervert!");
 			e.setCancelled(true);
 		}
 		
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void giveBackSaddle(PlayerInteractEntityEvent e) {
 		
 		Entity entity = e.getRightClicked();
 		Player player = e.getPlayer();
 		
 		if(entity instanceof Pig) {
 			
 			Pig entityPig = (Pig) entity;
 			
 			if(entityPig.hasSaddle() && entityPig.getPassenger() == null && plugin.perm.has(player, "rur.getBackSaddle")) {
 				
 				entityPig.setSaddle(false);
 				entityPig.getWorld().dropItem(entityPig.getLocation(), new ItemStack(Material.SADDLE, 1));
 				
 			}
 			
 		}
 		
 		if(entity instanceof Spider) {
 			
 			Spider entitySpider = (Spider) entity;
 			
 			if(plugin.perm.has(player, "rur.rideSpider") && (entitySpider.getPassenger() == null)) {
 				entitySpider.setPassenger(player);
 			}
 			
 		}
 		
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void giveBackSaddleOnDeath(EntityDeathEvent e) {
 		
 		Entity entity = e.getEntity();
 		
 		if(e.getEntity() instanceof Pig) {
 			
 			Pig entityPig = (Pig) entity;
 			
 			if(plugin.perm.has(entityPig.getKiller(), "rur.getBackSaddle") && plugin.config.getBoolean("pigDropSaddleOnDeath")) {
 				
 				e.getDrops().add(new ItemStack(Material.SADDLE, 1));
 				
 			}
 			
 		}
 		
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void buttonPress(PlayerInteractEvent e) {
 		
 		if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
 			
			if(e.getClickedBlock().getType().equals(Material.STONE_BUTTON) && plugin.SelectorPlayer != "") {
 					
 				Location buttonLoc = e.getClickedBlock().getLocation();
 				cu.setLocation(buttonLoc, plugin.selectType, plugin.selectName);
				plugin.SelectorPlayer = "";
 				plugin.saveConfiguration();
 				
 				e.getPlayer().sendMessage("A gomb sikeresen felvéve!");
 					
 			}
 			
 		}
 		
 	}
 	
 }
