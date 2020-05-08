 /*
  * Copyright (C) 2011 halvors <halvors@skymiastudios.com>
  * Copyright (C) 2011 speeddemon92 <speeddemon92@gmail.com>
  *
  * This file is part of Wolf.
  *
  * Wolf is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Wolf is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Wolf.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.halvors.Wolf.listeners;
 
 import net.minecraft.server.EntityPlayer;
 import net.minecraft.server.EntityWolf;
 import net.minecraft.server.PathEntity;
 import net.minecraft.server.PathPoint;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.craftbukkit.entity.CraftWolf;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.ItemStack;
 
 import com.halvors.Wolf.wolf.SelectedWolfManager;
 import com.halvors.Wolf.wolf.WolfManager;
import com.halvors.Wolf.wolf.WolfTable;
 
 /**
  * Handle events for all Player related events.
  * 
  * @author halvors
  */
 public class WolfPlayerListener extends PlayerListener {
     private final com.halvors.Wolf.Wolf plugin;
     
 //    private final ConfigManager configManager;
     private final WolfManager wolfManager;
     private final SelectedWolfManager selectedWolfManager;
     
     public WolfPlayerListener(final com.halvors.Wolf.Wolf plugin) {
         this.plugin = plugin;
 //        this.configManager = plugin.getConfigManager();
         this.wolfManager = plugin.getWolfManager();
         this.selectedWolfManager = plugin.getSelectedWolfManager();
     }
     
     @Override
     public void onPlayerInteract(PlayerInteractEvent event) {
         Action action = event.getAction();
         Player player = event.getPlayer();
         
         if (event.hasItem() && selectedWolfManager.hasSelectedWolf(player.getName())) {
             Location pos = player.getTargetBlock(null, 120).getLocation();
                 
             if (event.getItem().getType().equals(Material.SADDLE)) {
                 if (plugin.hasPermissions(player, "Wolf.target")) {
                     if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
                         PathPoint[] pathPoint = { new PathPoint(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ()) };
                         EntityWolf wolf = ((CraftWolf) selectedWolfManager.getSelectedWolf(player.getName())).getHandle();
                         wolf.a(new PathEntity(pathPoint));
                         
                         player.sendMessage(ChatColor.GREEN + "Wolf target set.");
                     }
                 }
             }
         }
     }
     
     @Override
     public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
         if (!event.isCancelled()) {
             Player player = event.getPlayer();
             Entity entity = event.getRightClicked();
             
             if (entity instanceof Wolf) {
                 Wolf wolf = (Wolf)entity;
                 
                 if (wolf.isTamed() && wolf.getOwner().equals(player)) {
                     Material item = player.getItemInHand().getType();
                     
                     if (item.equals(Material.BONE)) {
                         if (plugin.hasPermissions(player, "Wolf.select")) {
                             if (!wolfManager.hasWolf(wolf.getUniqueId())) {
                                 wolfManager.addWolf(wolf);
                                     
                                 player.sendMessage(ChatColor.GREEN + "Your wolf was named: " + ChatColor.YELLOW + wolfManager.getName(wolf.getUniqueId()));
                                 
                                 // TODO: Remove temporary database info message
                                 player.sendMessage("Wolf UniqueId: " + wolf.getUniqueId() + ", Database ID: " + wolfManager.getWolfTable(wolf.getUniqueId()).getId());
                             }
                                 
                             selectedWolfManager.addSelectedWolf(player.getName(), wolf);
                                 
                             player.sendMessage(ChatColor.GREEN + "Wolf selected.");
                         }
                     } else if (item.equals(Material.CHEST)) {
                         if (plugin.hasPermissions(player, "Wolf.chest")) {
                             if (wolfManager.hasWolf(wolf.getUniqueId())) {
                             	if (wolfManager.hasInventory(wolf.getUniqueId())) {                       		
                             		EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
                                     entityPlayer.a(plugin.getWolfInventoryManager().getWolfInventory(wolf.getUniqueId()).getInventory());
                                     
 //                                    wolf.setSitting(!wolf.isSitting()); // TODO: Check this
                             	} else {
                             		// Add inventory.
                             		wolfManager.addInventory(wolf.getUniqueId());
                             		
                             		// Remove 1 chest for players inventory.
                            		player.getInventory().remove(new ItemStack(Material.CHEST, 1));
                             	}
                             } else {
                                 // Add some message here.
                             }
                         }
                     }
                 }
             }
         }
     }
 }
