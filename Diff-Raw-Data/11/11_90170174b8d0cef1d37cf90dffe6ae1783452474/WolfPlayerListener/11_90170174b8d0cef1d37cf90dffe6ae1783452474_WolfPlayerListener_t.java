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
 
 package com.halvors.wolf.listeners;
 
 import net.minecraft.server.EntityPlayer;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerListener;
 
 import com.halvors.wolf.WolfPlugin;
 import com.halvors.wolf.util.ConfigManager;
 import com.halvors.wolf.util.WorldConfig;
 import com.halvors.wolf.wolf.SelectedWolfManager;
 import com.halvors.wolf.wolf.WolfManager;
 
 /**
  * Handle events for all Player related events.
  * 
  * @author halvors
  */
 public class WolfPlayerListener extends PlayerListener {
     private final WolfPlugin plugin;
     
     private final ConfigManager configManager;
     private final WolfManager wolfManager;
     private final SelectedWolfManager selectedWolfManager;
     
     public WolfPlayerListener(final WolfPlugin plugin) {
         this.plugin = plugin;
         this.configManager = plugin.getConfigManager();
         this.wolfManager = plugin.getWolfManager();
         this.selectedWolfManager = plugin.getSelectedWolfManager();
     }
     
     /*
     @Override
     public void onPlayerInteract(PlayerInteractEvent event) {
         Action action = event.getAction();
         Player player = event.getPlayer();
         String name = player.getName();
         
         if (event.hasItem() && selectedWolfManager.hasSelectedWolf(name)) {
             Wolf wolf = selectedWolfManager.getSelectedWolf(name);
             
             if (wolf.isTamed() && wolf.getOwner().equals(player) && wolfManager.hasWolf(wolf)) {
                 com.halvors.wolf.wolf.Wolf wolf1 = wolfManager.getWolf(wolf);
                 Material item = event.getItem().getType();
                 
                 if (item.equals(Material.SADDLE)) {
                     if (plugin.hasPermissions(player, "Wolf.wolf.target")) {
                         Location location = player.getTargetBlock(null, 120).getLocation();
                     	
                         if (action.equals(Action.RIGHT_CLICK_BLOCK) || action.equals(Action.RIGHT_CLICK_AIR)) {
                             PathPoint[] pathPoint = { new PathPoint(location.getBlockX(), location.getBlockY(), location.getBlockZ()) };
                             EntityWolf entityWolf = ((CraftWolf) wolf).getHandle();
                             entityWolf.a(new PathEntity(pathPoint));
                         
                             player.sendMessage(ChatColor.YELLOW + wolf1.getName() + ChatColor.WHITE + "has got a target.");
                         }
                     }
                 }
             }
         }
     }
     */
     
     @Override
     public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
         if (!event.isCancelled()) {
             Player player = event.getPlayer();
             Entity entity = event.getRightClicked();
             World world = entity.getWorld();
             WorldConfig worldConfig = configManager.getWorldConfig(world);
             
             if (entity instanceof Wolf) {
                 Wolf wolf = (Wolf) entity;
                 
                 if (wolf.isTamed() && wolf.getOwner().equals(player) && wolfManager.hasWolf(wolf)) {
                     com.halvors.wolf.wolf.Wolf wolf1 = wolfManager.getWolf(wolf);
                     Material item = player.getItemInHand().getType();
                     
                     if (item.equals(Material.BONE)) {
                         if (plugin.hasPermissions(player, "Wolf.wolf.select")) {
                             selectedWolfManager.addSelectedWolf(player.getName(), wolf);
                                 
                             player.sendMessage(ChatColor.GREEN + "Wolf selected.");
                            
                            wolf.setSitting(true);
                         }
                    } else if (item.equals(Material.BOOK)) {
                     	if (plugin.hasPermissions(player, "Wolf.wolf.info")) {
                     		int health = wolf.getHealth() / 2;
                             int maxHealth = 10;
                                 
                             player.sendMessage("Name: " + ChatColor.YELLOW + wolf1.getName());
                             player.sendMessage("Health: " + ChatColor.YELLOW + Integer.toString(health) + "/" + Integer.toString(maxHealth));
                            
                            wolf.setSitting(true);
                         }
                     } else if (item.equals(Material.CHEST)) {
                         if (plugin.hasPermissions(player, "Wolf.wolf.inventory")) {
                             if (worldConfig.inventoryEnable) {
                                 if (wolf1.hasInventory()) {
                                     EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
                                     entityPlayer.a(wolf1.getInventory());
                                 } else {
                                     // Add inventory.
                                     wolf1.addInventory();
                                         
                                     // Remove 1 chest for players inventory.
                                     player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
                                         
                                     player.sendMessage(ChatColor.YELLOW + wolf1.getName() + ChatColor.WHITE + " has now inventory. Right click with a chest to open it.");
                                 }
                             }
                            
                            wolf.setSitting(true);
                         }
                     }
                 }
             }
         }
     }
 }
