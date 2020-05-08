 /* Copyright 2013 Kevin Seiden. All rights reserved.
 
  This works is licensed under the Creative Commons Attribution-NonCommercial 3.0
 
  You are Free to:
     to Share: to copy, distribute and transmit the work
     to Remix: to adapt the work
 
  Under the following conditions:
     Attribution: You must attribute the work in the manner specified by the author (but not in any way that suggests that they endorse you or your use of the work).
     Non-commercial: You may not use this work for commercial purposes.
 
  With the understanding that:
     Waiver: Any of the above conditions can be waived if you get permission from the copyright holder.
     Public Domain: Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
     Other Rights: In no way are any of the following rights affected by the license:
         Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
         The author's moral rights;
         Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.
 
  Notice: For any reuse or distribution, you must make clear to others the license terms of this work. The best way to do this is with a link to this web page.
  http://creativecommons.org/licenses/by-nc/3.0/
  */
 
 package io.github.alshain01.flagscore;
 
 import io.github.alshain01.flags.Flag;
 import io.github.alshain01.flags.Flags;
 import io.github.alshain01.flags.ModuleYML;
 import io.github.alshain01.flags.System;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.enchantment.EnchantItemEvent;
 import org.bukkit.event.entity.*;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.weather.LightningStrikeEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.concurrent.ConcurrentHashMap;
 
 
 /**
  * Flags - Core Module that adds general flags to the plug-in Flags.
  * 
  * @author Alshain01
  */
 public class FlagsCore extends JavaPlugin {
 	/**
 	 * Called when this module is enabled
 	 */
 	@Override
 	public void onEnable() {
 		final PluginManager pm = Bukkit.getServer().getPluginManager();
 
 		if (!pm.isPluginEnabled("Flags")) {
 			getLogger().severe("Flags was not found. Shutting down.");
 			pm.disablePlugin(this);
 		}
 
 		// Connect to the data file and register the flags
 		Flags.getRegistrar().register(new ModuleYML(this, "flags.yml"), "Core");
 
 		// Load plug-in events and data
 		Bukkit.getServer().getPluginManager()
 				.registerEvents(new CoreListener(), this);
 	}
 	
 	/*
 	 * The event handlers for the flags we created earlier
 	 */
 	private class CoreListener implements Listener {
         private ConcurrentHashMap<String, ItemStack[][]> inventories = new ConcurrentHashMap<String, ItemStack[][]>();
 
 		/*
 		 * Handler for Enchanting
 		 */
 		@EventHandler(ignoreCancelled = true)
 		private void onEnchantItem(EnchantItemEvent e) {
 			final Flag flag = Flags.getRegistrar().getFlag("SpendExp");
 			if (flag != null) {
 				if (!System.getActive().getAreaAt(e.getEnchantBlock().getLocation()).getValue(flag, false)) {
 					e.setExpLevelCost(0);
 				}
 			}
 		}
 
 		/*
 		 * Handler for Zombie Door Break
 		 */
 		@EventHandler(ignoreCancelled = true)
 		private void onEntityBreakDoor(EntityBreakDoorEvent e) {
 			final Flag flag = Flags.getRegistrar().getFlag("DoorBreak");
 			if (flag != null) {
 				e.setCancelled(!System.getActive().getAreaAt(e.getEntity().getLocation()).getValue(flag, false));
 			}
 		}
 
 		/*
 		 * Handler for Healing
 		 */
 		@EventHandler(ignoreCancelled = true)
 		private void onEntityRegainHealth(EntityRegainHealthEvent e) {
 			final Flag flag = Flags.getRegistrar().getFlag("Healing");
 			if (flag != null && e.getEntity() instanceof Player) {
 				e.setCancelled(!System.getActive().getAreaAt(e.getEntity().getLocation())
 						.getValue(flag, false));
 			}
 		}
 
 		/*
 		 * Handler for Hunger
 		 */
 		@EventHandler(ignoreCancelled = true)
 		private void onFoodLevelChange(FoodLevelChangeEvent e) {
 			final Flag flag = Flags.getRegistrar().getFlag("Hunger");
 			if (flag != null) {
 				// Make sure it's a player and make sure the hunger bar is going
 				// down, not up.
 				if (e.getEntity() instanceof Player
 						&& e.getFoodLevel() < ((Player) e.getEntity()).getFoodLevel()) {
 					e.setCancelled(!System.getActive().getAreaAt(e.getEntity().getLocation()).getValue(flag, false));
 				}
 			}
 		}
 
 		/*
 		 * Handler for Lightning
 		 */
 		@EventHandler(ignoreCancelled = true)
 		private void onLightningStrike(LightningStrikeEvent e) {
 			final Flag flag = Flags.getRegistrar().getFlag("Lightning");
 			if (flag != null) {
 				e.setCancelled(!System.getActive().getAreaAt(e.getLightning().getLocation()).getValue(flag, false));
 			}
 		}
 
 		/*
 		 * Handler for Player Death
 		 */
 		@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 		private void onPlayerDeath(PlayerDeathEvent e) {
 			Flag flag = Flags.getRegistrar().getFlag("KeepExpOnDeath");
 			if (flag != null
 					&& System.getActive().getAreaAt(e.getEntity().getLocation()).getValue(flag, false)) {
 				e.setKeepLevel(true);
 			}
 
             flag = Flags.getRegistrar().getFlag("KeepInventory");
             if(flag != null
                     && System.getActive().getAreaAt(e.getEntity().getLocation()).getValue(flag, false)) {
                 ItemStack[] armor = e.getEntity().getInventory().getArmorContents();
                 ItemStack[] contents = e.getEntity().getInventory().getContents();
                 ItemStack[][] total = {armor, contents};
                 inventories.put(e.getEntity().getName(), total);
                 e.getDrops().clear();
                 return; // No need to check DropItemsOnDeath
             }
 
             flag = Flags.getRegistrar().getFlag("DropItemsOnDeath");
             if(flag != null
                    && System.getActive().getAreaAt(e.getEntity().getLocation()).getValue(flag, false)) {
                 e.getDrops().clear();
             }
 		}
 
         @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
         private void onPlayerQuit(PlayerQuitEvent e) {
             if(inventories.containsKey(e.getPlayer().getName())) {
                 inventories.remove(e.getPlayer().getName());
             }
         }
 
         @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
         private void onPlayerRespawn(PlayerRespawnEvent e) {
             if(inventories.containsKey(e.getPlayer().getName())) {
                 e.getPlayer().getInventory().setArmorContents(inventories.get(e.getPlayer().getName())[0]);
                 e.getPlayer().getInventory().setContents(inventories.get(e.getPlayer().getName())[1]);
                 inventories.remove(e.getPlayer().getName());
             }
         }
 
         /*
          * Handler for Creeper Explosions
          */
         @EventHandler(ignoreCancelled = true)
         private void onEntityExplode(EntityExplodeEvent e) {
             if(!(e.getEntity() instanceof Creeper)) {
                 return;
             }
 
             final Flag flag = Flags.getRegistrar().getFlag("CreeperExplosion");
             if(flag != null) {
                 e.setCancelled(!System.getActive().getAreaAt(e.getEntity().getLocation()).getValue(flag, false));
             }
         }
 	}
 }
