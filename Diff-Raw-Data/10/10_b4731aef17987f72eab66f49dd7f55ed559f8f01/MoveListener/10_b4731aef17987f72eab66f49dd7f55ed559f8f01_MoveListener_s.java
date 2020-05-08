 package net.kiwz.ThePlugin.listeners;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 import net.kiwz.ThePlugin.utils.Color;
 import net.kiwz.ThePlugin.utils.Place;
 import net.kiwz.ThePlugin.utils.MyWorld;
 import net.kiwz.ThePlugin.utils.TPRune;
 
 public class MoveListener implements Listener {
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerMove(PlayerMoveEvent event) {
 		Player player = event.getPlayer();
 		Location fromLoc = event.getFrom();
 		Location toLoc = event.getTo();
 		Block block = event.getTo().getBlock();
 		if (fromLoc.getBlockX() == toLoc.getBlockX() && fromLoc.getBlockZ() == toLoc.getBlockZ()) {
 			return;
 		}
 		
 		Place fromPlace = Place.getPlace(fromLoc);
 		Place toPlace = Place.getPlace(toLoc);
 		if (fromPlace != toPlace) {
 			if (toPlace == null) {
 				if (MyWorld.getWorld(player.getWorld()).getPvp()) {
 					player.sendMessage(Color.UNSAFE + fromPlace.getLeave());
 				} else {
 					player.sendMessage(Color.SAFE + fromPlace.getLeave());
 				}
 			} else if (fromPlace == null) {
 				if (toPlace.getPvP()) {
 					player.sendMessage(Color.UNSAFE + toPlace.getEnter());
 				} else {
 					player.sendMessage(Color.SAFE + toPlace.getEnter());
 				}
 			} else {
 				if (fromPlace.getPvP()) {
 					player.sendMessage(Color.UNSAFE + fromPlace.getLeave());
 				} else {
 					player.sendMessage(Color.SAFE + fromPlace.getLeave());
 				}
 
 				if (toPlace.getPvP()) {
 					player.sendMessage(Color.UNSAFE + toPlace.getEnter());
 				} else {
 					player.sendMessage(Color.SAFE + toPlace.getEnter());
 				}
 			}
 		}
 		
 		if (MyWorld.getWorld(toLoc.getWorld()).reachedBorder(toLoc)) {
 			player.teleport(fromLoc);
 			player.sendMessage(Color.WARNING + "Du har ndd enden av denne verden");
 		}
 		
 		if (block.getRelative(0, -2, 0).getType().equals(Material.DIAMOND_BLOCK)) {
 			TPRune tpRune = new TPRune();
 			tpRune.teleportRune1(player, block.getRelative(0, -2, 0), Material.GOLD_BLOCK);
 		}
 		if (block.getRelative(0, -3, 0).getType().equals(Material.DIAMOND_BLOCK)) {
 			TPRune tpRune = new TPRune();
 			tpRune.teleportRune2(player, block.getRelative(0, -3, 0), Material.GOLD_BLOCK);
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
 		Location fromLoc = event.getFrom();
 		Location toLoc = event.getTo();
 		
 		World fromWorld = event.getFrom().getWorld();
 		World toWorld = event.getTo().getWorld();
 		Place fromPlace = Place.getPlace(fromLoc);
 		Place toPlace = Place.getPlace(toLoc);
 		
 		if (MyWorld.getWorld(toLoc.getWorld()).reachedBorder(toLoc)) {
 			event.setCancelled(true);
 			player.sendMessage(Color.WARNING + "Du kan ikke teleportere deg hit");
 		} else if (fromPlace != toPlace) {
 			if (toPlace == null) {
 				if (MyWorld.getWorld(player.getWorld()).getPvp()) {
 					player.sendMessage(Color.UNSAFE + fromPlace.getLeave());
 				} else {
 					player.sendMessage(Color.SAFE + fromPlace.getLeave());
 				}
 			} else if (fromPlace == null) {
 				if (toPlace.getPvP()) {
 					player.sendMessage(Color.UNSAFE + toPlace.getEnter());
 				} else {
 					player.sendMessage(Color.SAFE + toPlace.getEnter());
 				}
 			} else {
 				if (fromPlace.getPvP()) {
 					player.sendMessage(Color.UNSAFE + fromPlace.getLeave());
 				} else {
 					player.sendMessage(Color.SAFE + fromPlace.getLeave());
 				}
 
 				if (toPlace.getPvP()) {
 					player.sendMessage(Color.UNSAFE + toPlace.getEnter());
 				} else {
 					player.sendMessage(Color.SAFE + toPlace.getEnter());
 				}
 			}
 		} else if (fromWorld != toWorld) {
 			if (MyWorld.getWorld(toWorld).getPvp()) {
 				player.sendMessage(Color.UNSAFE + "PvP er aktivert for denne verden");
 			} else {
 				player.sendMessage(Color.SAFE + "PvP er deaktivert for denne verden");
 			}
 		}
 	}
 }
