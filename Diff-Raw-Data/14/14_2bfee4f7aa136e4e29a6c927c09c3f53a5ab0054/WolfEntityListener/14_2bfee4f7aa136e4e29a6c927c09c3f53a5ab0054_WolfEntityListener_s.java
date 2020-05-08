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
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;
 
 import com.halvors.Wolf.util.ConfigManager;
 import com.halvors.Wolf.util.WorldConfig;
 import com.halvors.Wolf.wolf.WolfInventory;
 import com.halvors.Wolf.wolf.WolfManager;
 //import com.halvors.Wolf.wolf.WolfTable;
 
 /**
  * Handle events for all Entity related events.
  * 
  * @author halvors
  */
 public class WolfEntityListener extends EntityListener {
     private final com.halvors.Wolf.Wolf plugin;
     
     private final ConfigManager configManager;
     private final WolfManager wolfManager;
     
     public WolfEntityListener(final com.halvors.Wolf.Wolf plugin) {
         this.plugin = plugin;
         this.configManager = plugin.getConfigManager();
         this.wolfManager = plugin.getWolfManager();
     }
     
     @Override
     public void onCreatureSpawn(CreatureSpawnEvent event) {
         if (!event.isCancelled()) {
             Entity entity = event.getEntity();
             World world = entity.getWorld();
             WorldConfig worldConfig = configManager.getWorldConfig(world);
             
             if (entity instanceof Wolf) {
 //                Wolf wolf = (Wolf)entity;
                 
                 if (!worldConfig.wolfEnable) {
                     event.setCancelled(true);
                 }
             }
         }
     }
     
     @Override
     public void onEntityDamage(EntityDamageEvent event) {
         if (!event.isCancelled()) {
             Entity entity = event.getEntity();
             
             if (event instanceof EntityDamageByEntityEvent) {
                 Entity damager = ((EntityDamageByEntityEvent)event).getDamager();
                 
                 if (entity instanceof Wolf) {
                     Wolf wolf = (Wolf)entity;
                     
                     if (damager instanceof Player) {
                         Player attacker = (Player)damager;
                         
                         if (wolf.isTamed() && wolfManager.hasWolf(wolf.getUniqueId())) {
                             Player player = (Player)wolf.getOwner();
                             
                             if (attacker.equals(player)) {
                                 Material item = player.getItemInHand().getType();
                                 
                                 if (item.equals(Material.BONE)) {
                                     if (plugin.hasPermissions(player, "Wolf.info")) {
                                         String name = wolfManager.getName(wolf.getUniqueId());
                                         int health = wolf.getHealth();
                                         int maxHealth = 20;
                                         
                                         player.sendMessage("Name: " + ChatColor.YELLOW + name);
                                         player.sendMessage("Health: " + ChatColor.YELLOW + Integer.toString(health) + "/" + maxHealth);
                                         
                                         event.setCancelled(true);
                                         
                                         return;
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
     }
     
     @Override
     public void onEntityDeath(EntityDeathEvent event) {
         Entity entity = event.getEntity();
         World world = entity.getWorld();
         
         if (entity instanceof Wolf) {
             Wolf wolf = (Wolf)entity;
             
             if (wolf.isTamed() && wolfManager.hasWolf(wolf.getUniqueId())) {
                 //WolfTable wt = wolfManager.getWolfTable(wolf.getUniqueId());
                 
                 if (plugin.getWolfInventoryManager().hasWolfInventory(wolf.getUniqueId())) {
                     WolfInventory wi = plugin.getWolfInventoryManager().getWolfInventory(wolf.getUniqueId());
                     
                    for (ItemStack is : wi.getContents()) {
                        world.dropItem(wolf.getLocation(), is);
                     }
                 }
                 
                 wolfManager.removeWolf(wolf.getUniqueId());
             }
         }
     }
     
     /*
     @Override
     public void onEntityTame(EntityTameEvent event) {
         if (!event.isCancelled()) {
             Entity entity = event.getEntity();
             Player player = (Player) event.getOwner();
             World world = entity.getWorld();
             WorldConfig worldConfig = configManager.getWorldConfig(world);
             
             if (entity instanceof Wolf) {
                 Wolf wolf = (Wolf)entity;
                 List<WolfTable> wts = wolfManager.getWolfTables(player.getName());
                 
                 int size = wts.size();
                 int limit = worldConfig.limitValue;
                 
                 if (worldConfig.limitEnable && limit <= size) {
                     event.setCancelled(true);
                     
                     player.sendMessage("You can't tame this wolf, you already have " + ChatColor.YELLOW + Integer.toString(limit)+ ChatColor.WHITE + " of " + ChatColor.YELLOW + Integer.toString(size) + " wolves.");
                 }
                 
                 if (!wolfManager.hasWolf(wolf.getEntityId())) {
                     wolfManager.addWolf(wolf);
                     
                     player.sendMessage("You're wolf is now tame. Type /setname <name> for give your new wolf a name.");
                 }
             }
         }
     }
     */
     
     @Override
     public void onEntityTarget(EntityTargetEvent event) {
         if (!event.isCancelled()) {
             Entity entity = event.getEntity();
             Entity target = event.getTarget();
             
             if (entity instanceof Wolf) {
                 Wolf wolf = (Wolf)target;
                 
                 if (target instanceof Player) {
 //                    Player player = (Player)target;
                     
                     if (wolf.isTamed() && wolfManager.hasWolf(wolf.getUniqueId())) {
 //                        WolfTable wt = wolfManager.getWolfTable(wolf.getUniqueId());
                     }
                 }
             }
         }
     }
 }
