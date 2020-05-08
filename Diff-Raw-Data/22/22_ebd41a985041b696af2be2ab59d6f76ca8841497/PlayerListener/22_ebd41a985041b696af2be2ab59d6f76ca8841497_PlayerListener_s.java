 package com.gravypod.PersonalWorlds.Listener;
 
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Vehicle;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.vehicle.VehicleMoveEvent;
 import org.bukkit.metadata.MetadataValue;
 import org.bukkit.scheduler.BukkitScheduler;
 
 import com.gravypod.PersonalWorlds.PersonalPerms;
 import com.gravypod.PersonalWorlds.PersonalWorlds;
 import com.gravypod.PersonalWorlds.utils.PluginUtil;
 
 /**
  * Handler for player events.
  * 
  * @author gravypod
  * 
  */
 public class PlayerListener implements Listener {
 	
 	final PersonalWorlds plugin;
 	
 	final BukkitScheduler bukkitScheduler;
 	
 	public PlayerListener(final PersonalWorlds plugin) {
 	
 		this.plugin = plugin;
 		
 		bukkitScheduler = plugin.getServer().getScheduler();
 		
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void playerListener(final PlayerMoveEvent event) {
 	
 		/*
 		 * Abort if we havn't really moved
 		 */
 		if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ() && event.getFrom().getBlockY() == event.getTo().getBlockY()) {
 			return;
 		}
 		
 		final Player player = event.getPlayer();
 		
 		// Checks if the player name == a world name; If so, it /must/ be a
 		// player world
 		
 		final List<MetadataValue> m = player.getWorld().getMetadata(plugin.getPluginName());
 		
 		if (m.isEmpty()) {
 			return;
 		}
 		
 		if (m.get(0).asBoolean()) {
 			
 			final Location loc = event.getTo();
 			
 			if (PluginUtil.borderTest(loc)) {
 				
 				player.teleport(PluginUtil.safeSpawnLoc(loc));
 				
 				player.sendMessage("You have reached the end of this world!");
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void prePlayerLogin(final AsyncPlayerPreLoginEvent event) {
 	
 		synchronized(plugin) {
 			bukkitScheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
 				
 				@Override
 				public void run() {
 				
 					PluginUtil.loadWorld(event.getName());
 				}
 			});
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void playerLogin(final PlayerLoginEvent event) {
 	
 		final Player player = event.getPlayer();
 		
 		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 			
 			@Override
 			public void run() {
 			
 				PersonalPerms.assignPermissions(player);
 				
 			}
 			
 		}, 1);
 		
 		if (!player.getWorld().hasMetadata(plugin.getPluginName())) {
 			plugin.getServer().unloadWorld(PluginUtil.worldName(player.getName()), true);
 		}
 		
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void playerLogoutEvent(final PlayerQuitEvent event) {
 	
 		final String playerName = event.getPlayer().getName();
 		
 		PluginUtil.kickPlayersOut(playerName, true, "The world owner has left the game! You have been kicked out.");;
 		
 		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 			
 			@Override
 			public void run() {
 			
 				plugin.getServer().unloadWorld(PluginUtil.worldName(playerName), true);
 				
 			}
 		});
 		
 		PersonalPerms.removeAttachment(playerName);
 		
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void playerChangeWorldListener(final PlayerChangedWorldEvent event) {
 	
 		final World worldFrom = event.getFrom();
 		
 		final Player player = event.getPlayer();
 		
 		PersonalPerms.assignPermissions(player);
 		
 		if (worldFrom.getPlayers().size() <= 1) {
 			if (PluginUtil.isPlayerWorld(worldFrom.getName())) {
 				
 				if (plugin.getServer().unloadWorld(worldFrom, true)) {
 					;
 				}
 				
 			}
 		}
 		
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void playerTpEvent(final PlayerTeleportEvent event) {
 	
 		final Player player = event.getPlayer();
 		
 		if (PluginUtil.isPlayerWorld(player.getName())) {
 			if (PluginUtil.borderTest(event.getTo())) {
 				player.sendMessage("You cannot teleport out of your world!");
 				event.setCancelled(true);
 			}
 		}
 		
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void entityMoveEvent(final VehicleMoveEvent event) {
	
 		final Vehicle v = event.getVehicle();
 		
 		if (v instanceof Vehicle) {
 			
 			if (v.getPassenger() instanceof Player) {
 				
 				final Location l = event.getTo();
 				
 				if (PluginUtil.borderTest(l)) {
 					
 					v.eject();
 					v.remove();
 					
 				}
 				
 			}
 			
 		}
 		
 	}
 	
 }
