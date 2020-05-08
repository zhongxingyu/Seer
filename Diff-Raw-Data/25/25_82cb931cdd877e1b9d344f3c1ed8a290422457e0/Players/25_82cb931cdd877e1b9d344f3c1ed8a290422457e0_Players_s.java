 /**
  *  Name: Players.java
  *  Date: 08:16:34 - 10 sep 2012
  * 
  *  Author: LucasEmanuel @ bukkit forums
  *  
  *  
  *  Copyright 2013 Lucas Arnstrm
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program. If not, see <http://www.gnu.org/licenses/>.
  *  
  *
  *  Filedescription:
  *  
  *  Eventlistener concerning player related events.
  *  
  */
 
 package se.lucasarnstrom.survivalgamesmultiverse.listeners;
 
 import java.util.ArrayList;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityRegainHealthEvent;
 import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryType.SlotType;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 //import org.bukkit.event.player.PlayerItemHeldEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import se.lucasarnstrom.survivalgamesmultiverse.Main;
 import se.lucasarnstrom.survivalgamesmultiverse.events.PlayerRemoveEvent;
 import se.lucasarnstrom.survivalgamesmultiverse.managers.LanguageManager;
 import se.lucasarnstrom.survivalgamesmultiverse.managers.PlayerManager;
 import se.lucasarnstrom.survivalgamesmultiverse.managers.StatsManager;
 import se.lucasarnstrom.survivalgamesmultiverse.managers.WorldManager;
 import se.lucasarnstrom.survivalgamesmultiverse.managers.StatusManager.StatusFlag;
 import se.lucasarnstrom.survivalgamesmultiverse.utils.ConsoleLogger;
 
 public class Players implements Listener {
 	
 	private Main plugin;
 	private ConsoleLogger logger;
 	
 	private WorldManager    worldmanager;
 	private PlayerManager   playermanager;
 	private LanguageManager language;
 	
 	public Players(Main instance) {
 		plugin = instance;
 		logger = new ConsoleLogger("PlayerListener");
 		
 		worldmanager  = plugin.getWorldManager();
 		playermanager = plugin.getPlayerManager();
 		language      = plugin.getLanguageManager();
 		
 		logger.debug("Initiated");
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 	public void onInventoryClick(InventoryClickEvent event) {
 		
 		Player player = (Player) event.getWhoClicked();
 		
 		if(worldmanager.isGameWorld(player.getWorld())
 				&& playermanager.isInGame(player)
 				&& plugin.getConfig().getBoolean("halloween.forcepumpkin")
 				&& !player.hasPermission("survivalgames.ignore.forcepumpkin")
 				&& event.getSlotType().equals(SlotType.ARMOR)
 				&& event.getCurrentItem().getType().equals(Material.PUMPKIN)) {
 			
 			player.sendMessage(ChatColor.RED + language.getString("forcedPumpkin"));
 			event.setCancelled(true);
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
 		
 		ArrayList<String> allowedcommands = (ArrayList<String>) plugin.getConfig().getList("allowedCommandsInGame");
 		
 		Player player  = event.getPlayer();
 		String command = event.getMessage();
 		
 		if(playermanager.isInGame(player)
 				&& worldmanager.isGameWorld(player.getWorld())
 				&& !allowedcommands.contains(command)
 				&& !player.hasPermission("survivalgames.ignore.commandfilter")) {
 			
 			event.setCancelled(true);
 			event.getPlayer().sendMessage(ChatColor.RED + language.getString("blockedCommand"));
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 	public void onPlayerTeleport(PlayerTeleportEvent event) {
 		
 		Player   player = event.getPlayer();
 		Location from   = event.getFrom();
 		Location to     = event.getTo();
 		
		if(worldmanager.isGameWorld(from.getWorld()) && !worldmanager.isGameWorld(to.getWorld())) {
 			playermanager.restoreInventory(player);
 			if(playermanager.isInGame(player)) {
 				logger.debug("Removing player " + player.getName() + " due to teleportation!");
 				
 				playermanager.removePlayer(from.getWorld().getName(), player);
 				
 				String message = "[" 
 						+ ChatColor.GOLD + "SGAnti-Cheat"
 						+ ChatColor.WHITE + "] :: " 
 						+ ChatColor.RED + player.getName() 
 						+ ChatColor.WHITE + " - " 
 						+ language.getString("anticheat.teleported");
 				
 				worldmanager.broadcast(from.getWorld(), message);
 				
 				plugin.getSignManager().updateSigns();
 			}
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		
 		Block  block  = event.getClickedBlock();
 		Player player = event.getPlayer();
 		
 		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
 			if(block.getType().equals(Material.SIGN_POST) 
 					|| block.getType().equals(Material.WALL_SIGN)) {
 				
 				/* 
 				 *  ---- Join sign
 				 */
 				
 				String worldname = plugin.getSignManager().getGameworldName(block);
 				
 				if(worldname != null && worldmanager.isGameWorld(Bukkit.getWorld(worldname))) {
 					if(plugin.getStatusManager().getStatusFlag(worldname) == StatusFlag.WAITING) {
 						if(playermanager.isInGame(player) == false) {
 							if(plugin.getLocationManager().tpToStart(player, worldname)) {
 								
 								playermanager.addPlayer(worldname, player);
 								
 								plugin.getStatsManager().checkAndAddScoreboard(player.getName());
 								
 								player.sendMessage(ChatColor.GOLD + language.getString("youJoinedTheGame"));
 								worldmanager.broadcast(Bukkit.getWorld(worldname), ChatColor.GOLD + player.getName() + 
 										ChatColor.WHITE + " " + language.getString("playerJoinedGame"));
 								
 								plugin.getSignManager().updateSigns();
 								
 								// Now we have to wait for more players!
 								if(playermanager.getPlayerAmount(worldname) == 1)
 									plugin.getStatusManager().startPlayerCheck(worldname);
 							}
 							else
 								player.sendMessage(ChatColor.RED + language.getString("gameIsFull"));
 						}
 						else
 							player.sendMessage(ChatColor.RED + language.getString("alreadyPlaying"));
 					}
 					else if(plugin.getStatusManager().getStatusFlag(worldname) == StatusFlag.STARTED)
 						player.sendMessage(ChatColor.RED + language.getString("gameHasAlreadyStarted"));
 					else if(plugin.getStatusManager().getStatusFlag(worldname) == StatusFlag.FROZEN)
 						player.sendMessage(ChatColor.RED + language.getString("Join_Blocked_Frozen"));
 				}
 				
 				/*
 				 *  ---- Sign Registration
 				 */
 				
 				else if(((Sign)block.getState()).getLine(0).equalsIgnoreCase("[sginfo]") 
 						&& player.hasPermission("survivalgames.signs.sginfo")) {
 					plugin.getSignManager().registerSign(block);
 				}
 			}
 			
 			/*
 			 *  ---- Chest logging
 			 */
 			
 			else if(block.getType().equals(Material.CHEST) && worldmanager.isGameWorld(block.getWorld())) {
 				plugin.getChestManager().randomizeChest((Chest)block.getState());
 			}
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 	public void onPlayerRespawn(final PlayerRespawnEvent event) {
 		
 		playermanager.restoreInventory(event.getPlayer());
 		
 		if(worldmanager.isGameWorld(event.getRespawnLocation().getWorld())) {
 			
 			/* The players should be teleported directly after respawn instead of resetting the respawnlocation
 			 * to the desired value. This is to make sure that the player shifts worlds correctly and ensure
 			 * that any multiverse monitor like MultiInv catches the world change correctly. */
 			
 			final Location lobby = Bukkit.getWorld(plugin.getConfig().getString("lobbyworld")).getSpawnLocation();
 			
 			new BukkitRunnable() {
 				@Override
 				public void run() {
 					event.getPlayer().teleport(lobby);
 				}
 			}.runTask(plugin);
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		
 		if(worldmanager.isGameWorld(event.getPlayer().getWorld())) {
 			event.getPlayer().teleport(Bukkit.getWorld(plugin.getConfig().getString("lobbyworld")).getSpawnLocation());
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		
 		Player player = event.getPlayer();
 		
 		if(worldmanager.isGameWorld(player.getWorld()) 
 				&& playermanager.isInGame(player)) {
 			
 			String message =  "[" 
 					+ ChatColor.GOLD + "SGAnti-Cheat"
 					+ ChatColor.WHITE + "] :: " 
 					+ ChatColor.RED + player.getName() 
 					+ ChatColor.WHITE + " - " 
 					+ language.getString("anticheat.disconnect");
 			
 			playermanager.removePlayer(player.getWorld().getName(), player);
 			worldmanager.broadcast(player.getWorld(), message);
 			
 			plugin.getSignManager().updateSigns();
 			
 			// Is the game over?
 			plugin.gameover(player.getWorld());
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 	public void onPlayerDeath(PlayerDeathEvent event) {
 		
 		Player victim = event.getEntity();
 		
 		if(worldmanager.isGameWorld(victim.getWorld())) {
 			
 			// Block all deathmessages in the SG worlds
 			event.setDeathMessage(null);
 			
 			if(playermanager.isInGame(victim)) {
 				StatsManager statsmanager = plugin.getStatsManager();
 				
 				// Was this player killed by another player?
 				Player killer = event.getEntity().getKiller();
 				
 				if(killer != null) {
 					worldmanager.broadcast(victim.getWorld(), ChatColor.RED + victim.getName() 
 							+ ChatColor.WHITE + " " 
 							+ language.getString("wasKilledBy") 
 							+ " " + ChatColor.GOLD + killer.getName());
 					
 					if(!killer.hasPermission("survivalgames.ignore.stats")) 
 						statsmanager.addKillPoints(killer.getName(), 1, true);
 				}
 				else
 					worldmanager.broadcast(victim.getWorld(), ChatColor.RED + victim.getName() 
 							+ ChatColor.WHITE + " " + language.getString("isOutOfTheGame"));
 				
 				if(!victim.hasPermission("survivalgames.ignore.stats")) 
 					statsmanager.addDeathPoints(victim.getName(), 1, true);
 				
 				playermanager.removePlayer(victim.getWorld().getName(), victim);
 				plugin.getStatsManager().removeScoreboard(victim.getName());
 				plugin.getSignManager().updateSigns();
 				
 				// Is the game over?
 				plugin.gameover(victim.getWorld());
 			}
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 	public void onPlayerMove(PlayerMoveEvent event) {
 		
 		Player player = event.getPlayer();
 		
 		// If it is a SG world and the game hasnt started and the player is in the game
 		if(worldmanager.isGameWorld(player.getWorld())) {
 			
 			if(playermanager.isInGame(player)) {
 				if(plugin.getStatusManager().getStatusFlag(player.getWorld().getName()) == StatusFlag.WAITING) {
 					
 					double fromX = event.getFrom().getX();
 					double fromZ = event.getFrom().getZ();
 					
 					double toX   = event.getTo().getX();
 					double toZ   = event.getTo().getZ();
 					
 					if(fromX != toX && fromZ != toZ) {
 						player.sendMessage(ChatColor.RED + language.getString("blockedMovement"));
 						player.teleport(event.getFrom());
 					}
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 	public void onEntityDamage(EntityDamageEvent event) {
 		
 		if(event.getEntity() instanceof Player) {
 			
 			Player player = (Player) event.getEntity();
 			
 			if(worldmanager.isGameWorld(player.getWorld())
 					&& playermanager.isInGame(player)
 					&& plugin.getStatusManager().getStatusFlag(player.getWorld().getName()) == StatusFlag.WAITING) {
 				
 				event.setCancelled(true);
 			}
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 	public void onEntityRegainHealth(EntityRegainHealthEvent event) {
 		if(event.getEntity() instanceof Player) {
 			Player p = (Player) event.getEntity();
 			
 			if(worldmanager.isGameWorld(p.getWorld()) 
 					&& event.getRegainReason().equals(RegainReason.SATIATED)
 					&& !worldmanager.allowHealthRegen(p.getWorld())) {
 				event.setCancelled(true);
 			}
 		}
 	}
 	
 //	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 //	public void onPlayerItemHeldEvent(PlayerItemHeldEvent event) {
 //		Player p = event.getPlayer();
 //		
 //		if(worldmanager.isGameWorld(p.getWorld())
 //				&& p.getItemInHand().getType().equals(Material.COMPASS)) {
 //			
 //			Location origin  = p.getLocation();
 //			Location closest = null;
 //			
 //			for(Player temp : playermanager.getPlayerList(p.getWorld().getName())) {
 //				
 //				// Skip the holder.
 //				if(temp == p) continue;
 //				
 //				if(closest == null) {
 //					closest = temp.getLocation();
 //					continue;
 //				}
 //				
 //				if(closest.distanceSquared(origin) >= temp.getLocation().distanceSquared(origin)) {
 //					closest = temp.getLocation();
 //				}
 //			}
 //			
 //			if(closest != null) {
 //				p.sendMessage(language.getString("compassLock"));
 //				p.setCompassTarget(closest);
 //			}
 //		}
 //	}
 	
 	@EventHandler
 	public void onPlayerRemove(PlayerRemoveEvent event) {
 		plugin.getStatsManager().updateMySQL(event.getPlayer().getName());
 	}
 }
