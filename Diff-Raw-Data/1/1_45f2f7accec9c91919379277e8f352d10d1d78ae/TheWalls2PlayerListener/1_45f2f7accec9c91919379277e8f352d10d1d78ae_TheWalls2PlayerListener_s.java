 /** TheWalls2: The Walls 2 plugin.
   * Copyright (C) 2012  Andrew Stevanus (Hoot215) <hoot893@gmail.com>
   * 
   * This program is free software: you can redistribute it and/or modify
   * it under the terms of the GNU Affero General Public License as published by
   * the Free Software Foundation, either version 3 of the License, or
   * (at your option) any later version.
   * 
   * This program is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty of
   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   * GNU Affero General Public License for more details.
   * 
   * You should have received a copy of the GNU Affero General Public License
   * along with this program.  If not, see <http://www.gnu.org/licenses/>.
   */
 
 package me.Hoot215.TheWalls2;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 public class TheWalls2PlayerListener implements Listener {
 	private TheWalls2 plugin;
 	
 	public TheWalls2PlayerListener(TheWalls2 instance) {
 		plugin = instance;
 	}
 	
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onBlockBreak(BlockBreakEvent event) {
 		Player player = event.getPlayer();
 		TheWalls2GameList gameList = plugin.getGameList();
 		
 		if (player.getWorld().getName().equals(TheWalls2.worldName)) {
 			if (gameList == null) {
 				if (plugin.getQueue().isInQueue(player.getName())) {
 					event.setCancelled(true);
 					player.sendMessage(ChatColor.RED + "You can't do that until the game starts!");
 					return;
 				}
 			}
 			else if (gameList.isInGame(player.getName())) {
 				if (plugin.getLocationData().isPartOfWall(event.getBlock().getLocation())) {
 					event.setCancelled(true);
 					player.sendMessage(ChatColor.RED + "Don't break the rules!");
 					return;
 				}
 				for (Location loc : plugin.getLocationData().getSlots()) {
 					if (loc.getBlockX() == event.getBlock().getX() && loc.getBlockZ() == event.getBlock().getZ()) {
 						event.setCancelled(true);
 						player.sendMessage(ChatColor.RED + "Don't break the rules!");
 						return;
 					}
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onBlockPlace(BlockPlaceEvent event) {
 		Player player = event.getPlayer();
 		TheWalls2GameList gameList = plugin.getGameList();
 		
 		if (player.getWorld().getName().equals(TheWalls2.worldName)) {
 			if (gameList == null) {
 				if (plugin.getQueue().isInQueue(player.getName())) {
 					event.setCancelled(true);
 					player.sendMessage(ChatColor.RED + "You can't do that until the game starts!");
 					return;
 				}
 			}
 			else if (gameList.isInGame(player.getName())) {
 				if (plugin.getLocationData().isPartOfWall(event.getBlock().getLocation())) {
 					event.setCancelled(true);
 					player.sendMessage(ChatColor.RED + "Don't break the rules!");
 					return;
 				}
 				if (event.getBlock().getY() > 93) {
 					event.setCancelled(true);
 					player.sendMessage(ChatColor.RED + "Don't break the rules!");
 					return;
 				}
 				for (Location loc : plugin.getLocationData().getSlots()) {
 					if (loc.getBlockX() == event.getBlock().getX() && loc.getBlockZ() == event.getBlock().getZ()) {
 						event.setCancelled(true);
 						player.sendMessage(ChatColor.RED + "Don't break the rules!");
 						return;
 					}
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		Player player = event.getPlayer();
 		
 		if (player.getWorld().getName().equals(TheWalls2.worldName)) {
 			if (plugin.getGameList() == null) {
 				if (plugin.getQueue().isInQueue(player.getName())) {
 					event.setCancelled(true);
 					player.sendMessage(ChatColor.RED + "You can't do that until the game starts!");
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onEntityDamage(EntityDamageEvent event) {
 		if (!(event.getEntity() instanceof Player))
 			return;
 		
 		Player player = (Player) event.getEntity();
 		
 		if (player.getWorld().getName().equals(TheWalls2.worldName)) {
 			if (plugin.getGameList() == null) {
 				if (plugin.getQueue().isInQueue(player.getName())) {
 					event.setCancelled(true);
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.LOW)
 	public void onPlayerDeath(PlayerDeathEvent event) {
 		Player player = event.getEntity();
 		String playerName = player.getName();
 		TheWalls2GameList gameList = plugin.getGameList();
 		TheWalls2RespawnQueue respawnQueue = plugin.getRespawnQueue();
 		
 		if (gameList == null)
 			return;
 		
 		if (gameList.isInGame(playerName)) {
 			plugin.getServer().broadcastMessage(ChatColor.YELLOW + playerName + ChatColor.RED + " has been defeated in a game of The Walls 2!");
 			gameList.removeFromGame(playerName);
 			respawnQueue.addPlayer(playerName, player.getLocation());
 			plugin.checkIfGameIsOver();
 			return;
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		Player player = event.getPlayer();
 		String playerName = player.getName();
 		TheWalls2GameList gameList = plugin.getGameList();
 		TheWalls2PlayerQueue queue = plugin.getQueue();
 		
 		if (gameList == null) {
 			if (queue.isInQueue(playerName)) {
 				queue.removePlayer(playerName, true);
 				return;
 			}
 		}
 		
 		if (gameList.isInGame(playerName)) {
 			plugin.getServer().broadcastMessage(ChatColor.YELLOW + playerName + ChatColor.RED + " has quit a game of The Walls 2!");
 			gameList.removeFromGame(playerName);
 			queue.removePlayer(playerName, true);
 			plugin.checkIfGameIsOver();
 			return;
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		Player player = event.getPlayer();
 		String playerName = player.getName();
 		TheWalls2RespawnQueue respawnQueue = plugin.getRespawnQueue();
 		
 		if (respawnQueue.isInRespawnQueue(playerName)) {
 			event.setRespawnLocation(respawnQueue.getLastPlayerLocation(playerName));
 			respawnQueue.removePlayer(playerName);
 		}
 	}
 }
