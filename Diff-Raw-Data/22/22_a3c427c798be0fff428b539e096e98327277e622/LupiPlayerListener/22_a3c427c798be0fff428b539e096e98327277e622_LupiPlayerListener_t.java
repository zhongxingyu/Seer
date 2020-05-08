 /*
  * Copyright (C) 2011 halvors <halvors@skymiastudios.com>
  * Copyright (C) 2011 speeddemon92 <speeddemon92@gmail.com>
  *
  * This file is part of Lupi.
  *
  * Lupi is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Lupi is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Lupi.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.halvors.lupi.listeners;
 
 import net.minecraft.server.EntityPlayer;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerListener;
 
 import com.halvors.lupi.Lupi;
 import com.halvors.lupi.util.ConfigurationManager;
 import com.halvors.lupi.util.WorldConfiguration;
 import com.halvors.lupi.wolf.SelectedWolfManager;
 import com.halvors.lupi.wolf.WolfManager;
 
 /**
  * Handle events for all Player related events.
  * 
  * @author halvors
  */
 public class LupiPlayerListener extends PlayerListener {
 //    private Lupi plugin;
     
     private final ConfigurationManager configManager;
 
     public LupiPlayerListener(Lupi plugin) {
 //        this.plugin = plugin;
         this.configManager = plugin.getConfigurationManager();
     }
     
     @Override
     public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
         if (!event.isCancelled()) {
             Player player = event.getPlayer();
             Entity entity = event.getRightClicked();
             World world = entity.getWorld();
             WorldConfiguration worldConfig = configManager.get(world);
             
             if (entity instanceof Wolf) {
                 Wolf wolf = (Wolf) entity;
                 
                 if (wolf.isTamed() && wolf.getOwner().equals(player) && WolfManager.hasWolf(wolf)) {
                     com.halvors.lupi.wolf.Wolf wolf1 = WolfManager.getWolf(wolf);
                     int item = player.getItemInHand().getTypeId();
                     
                     if (item == worldConfig.wolfItem) {
                         if (Lupi.hasPermissions(player, "Wolf.wolf.select")) {
                             SelectedWolfManager.addSelectedWolf(player, wolf);
                                 
                             player.sendMessage(ChatColor.GREEN + "Wolf selected.");
                         }
                     } else if (item == worldConfig.infoItem) {
                     	if (Lupi.hasPermissions(player, "Wolf.wolf.info")) {
                     		int health = wolf.getHealth() / 2;
                             int maxHealth = 10;
                                 
                             player.sendMessage("Name: " + ChatColor.YELLOW + wolf1.getName());
                             player.sendMessage("Health: " + ChatColor.YELLOW + Integer.toString(health) + "/" + Integer.toString(maxHealth));
                         }
                     } else if (item == worldConfig.inventoryItem) {
                         if (Lupi.hasPermissions(player, "Wolf.wolf.inventory")) {
                             if (worldConfig.inventoryEnable) {
                                 if (wolf1.hasInventory()) {
                                    if (!wolf1.isInventoryLoaded()) {
                                        wolf1.loadInventory();
                                    }
                                     EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
                                     entityPlayer.a(wolf1.getInventory());
                                 } else {
                                     // Add inventory.
                                     wolf1.addInventory();
                                         
                                     // Remove 1 chest for players inventory.
                                     player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
                                         
                                     player.sendMessage(ChatColor.YELLOW + wolf1.getName() + ChatColor.WHITE + " has now inventory. Right click with a chest to open it.");
                                 }
                             } else {
                             	player.sendMessage(ChatColor.RED + "Wolf inventory is not enabled.");
                             }
                         }
                     }
                 }
             }
         }
     }
 }
