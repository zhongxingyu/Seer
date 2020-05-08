 /*
  * This file is part of Aqualock.
  *
  * Copyright (c) 2012, AlmuraDev <http://www.almuramc.com/>
  * Aqualock is licensed under the Almura Development License.
  *
  * Aqualock is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * As an exception, all classes which do not reference GPL licensed code
  * are hereby licensed under the GNU Lesser Public License, as described
  * in Almura Development License.
  *
  * Aqualock is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License,
  * the GNU Lesser Public License (for classes that fulfill the exception)
  * and the Almura Development License along with this program. If not, see
  * <http://www.gnu.org/licenses/> for the GNU General Public License and
  * the GNU Lesser Public License.
  */
 package com.almuramc.aqualock.bukkit;
 
 import com.almuramc.bolt.lock.Lock;
 import com.almuramc.bolt.registry.Registry;
 
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.block.BlockFormEvent;
 import org.bukkit.event.block.BlockFromToEvent;
 import org.bukkit.event.block.BlockIgniteEvent;
 import org.bukkit.event.block.BlockPistonExtendEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.EntityBlockFormEvent;
 import org.bukkit.event.block.LeavesDecayEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.weather.LightningStrikeEvent;
 
 public class AqualockListener implements Listener {
 	private final AqualockPlugin plugin;
 
 	public AqualockListener(AqualockPlugin plugin) {
 		this.plugin = plugin;
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onBlockDamage(BlockDamageEvent event) {
 		Player damager = event.getPlayer();
 		Block damaged = event.getBlock();
 		Registry registry = plugin.getRegistry();
 		if (registry.contains(damaged.getWorld().getUID(), damaged.getX(), damaged.getY(), damaged.getZ())) {
 			Lock lock = registry.getLock(damaged.getWorld().getUID(), damaged.getX(), damaged.getY(), damaged.getZ());
 			if (!lock.getOwner().equals(damager.getName()) || !(lock.getCoOwners().contains(damager.getName()))) {
 				damager.sendMessage("[" + ChatColor.AQUA + "Aqualock" + ChatColor.WHITE + "] This voxel is locked.");
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onBlockBreak(BlockBreakEvent event) {
 		Player breaker = event.getPlayer();
 		Block breaking = event.getBlock();
 		Registry registry = plugin.getRegistry();
 		if (registry.contains(breaking.getWorld().getUID(), breaking.getX(), breaking.getY(), breaking.getZ())) {
 			Lock lock = registry.getLock(breaking.getWorld().getUID(), breaking.getX(), breaking.getY(), breaking.getZ());
 			if (!lock.getOwner().equals(breaker.getName()) || !(lock.getCoOwners().contains(breaker.getName()))) {
 				breaker.sendMessage("[" + ChatColor.AQUA + "Aqualock" + ChatColor.WHITE + "] This voxel is locked.");
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onBlockFromTo(BlockFromToEvent event) {
 		Block to = event.getToBlock();
 		Block from = event.getBlock();
 		Registry registry = plugin.getRegistry();
 		if (registry.contains(from.getWorld().getUID(), from.getX(), from.getY(), from.getZ())) {
 			if (registry.contains(to.getWorld().getUID(), to.getX(), to.getY(), to.getZ())) {
 				Lock fromLock = registry.getLock(from.getWorld().getUID(), from.getX(), from.getY(), from.getZ());
 				Lock toLock = registry.getLock(to.getWorld().getUID(), to.getX(), to.getY(), to.getZ());
 				if (fromLock.equals(toLock)) {
 					return;
 				}
 			}
 		}
 		event.setCancelled(true);
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onLeavesDecay(LeavesDecayEvent event) {
 		Block decaying = event.getBlock();
 		Registry registry = plugin.getRegistry();
 		if (registry.contains(decaying.getWorld().getUID(), decaying.getX(), decaying.getY(), decaying.getZ())) {
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onSignChange(SignChangeEvent event) {
 		Player writer = event.getPlayer();
 		Block theSign = event.getBlock();
 		Registry registry = plugin.getRegistry();
 		if (registry.contains(theSign.getWorld().getUID(), theSign.getX(), theSign.getY(), theSign.getZ())) {
 			Lock lock = registry.getLock(theSign.getWorld().getUID(), theSign.getX(), theSign.getY(), theSign.getZ());
 			if (!lock.getOwner().equals(writer.getName()) || !(lock.getCoOwners().contains(writer.getName()))) {
 				writer.sendMessage("[" + ChatColor.AQUA + "Aqualock" + ChatColor.WHITE + "] This voxel is locked.");
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onBlockForm(BlockFormEvent event) {
 		Block formed = event.getBlock();
 		Registry registry = plugin.getRegistry();
 		if (registry.contains(formed.getWorld().getUID(), formed.getX(), formed.getY(), formed.getZ())) {
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onEntityBlockForm(EntityBlockFormEvent event) {
 		Player former = null;
 		if (event.getEntity() instanceof Player) {
 			former = (Player) event.getEntity();
 		}
 		Block formed = event.getBlock();
 		Registry registry = plugin.getRegistry();
 		if (registry.contains(formed.getWorld().getUID(), formed.getX(), formed.getY(), formed.getZ())) {
 			if (former != null) {
 				Lock lock = registry.getLock(formed.getWorld().getUID(), formed.getX(), formed.getY(), formed.getZ());
 				if (!lock.getOwner().equals(former.getName()) || !(lock.getCoOwners().contains(former.getName()))) {
 					former.sendMessage("[" + ChatColor.AQUA + "Aqualock" + ChatColor.WHITE + "] This voxel is locked.");
 				}
 			}
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPistonExtend(BlockPistonExtendEvent event) {
 		Block source = event.getBlock();
 		Block extension = source.getRelative(event.getDirection());
 		Registry registry = plugin.getRegistry();
 		if (registry.contains(source.getWorld().getUID(), source.getX(), source.getY(), source.getZ())) {
 			if (registry.contains(extension.getWorld().getUID(), extension.getX(), extension.getY(), extension.getZ())) {
 				Lock sourceLock = registry.getLock(source.getWorld().getUID(), source.getX(), source.getY(), source.getZ());
 				Lock extensionLock = registry.getLock(extension.getWorld().getUID(), extension.getX(), extension.getY(), extension.getZ());
 				if (!sourceLock.equals(extensionLock)) {
 					event.setCancelled(true);
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onBlockIgnite(BlockIgniteEvent event) {
 		Block ignited = event.getBlock();
 		Registry registry = plugin.getRegistry();
 		if (registry.contains(ignited.getWorld().getUID(), ignited.getX(), ignited.getY(), ignited.getZ())) {
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onLightningStrike(LightningStrikeEvent event) {
 		Block struck = event.getLightning().getLocation().getBlock();
 		Registry registry = plugin.getRegistry();
 		if (registry.contains(struck.getWorld().getUID(), struck.getX(), struck.getY(), struck.getZ())) {
 			event.setCancelled(true);
 		}
 	}
 }
