 package com.jsne10.nodrops;
 
 import org.bukkit.ChatColor;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class NoDrops extends JavaPlugin {
 
 	@Override
 	public void onEnable() {
 
 		// Registers the Drop listener events.
		this.getServer().getPluginManager()
				.registerEvents(new DropsDisable(), this);
 
 	}
 
 	@Override
 	public void onDisable() {
 	}
 
 	/** Drop listener inner-class. */
 	class DropsDisable implements Listener {
 
 		@EventHandler
 		public void onBlockDrop(PlayerDropItemEvent event) {
 			if (!event.getPlayer().hasPermission("jnodrops.candrop")
 					&& !event.isCancelled()) {
 				event.setCancelled(true);
 				event.getPlayer().sendMessage(
 						ChatColor.RED + "[NoDrops] " + ChatColor.GRAY
 								+ "You must not share items!");
 			}
 		}
 
 		@EventHandler
 		public void onDeath(PlayerDeathEvent event) {
 			if (!event.getEntity().hasPermission("jnodrops.dropondeath")) {
 				event.getDrops().clear();
 			}
 		}
 
 	}
 
 }
 
