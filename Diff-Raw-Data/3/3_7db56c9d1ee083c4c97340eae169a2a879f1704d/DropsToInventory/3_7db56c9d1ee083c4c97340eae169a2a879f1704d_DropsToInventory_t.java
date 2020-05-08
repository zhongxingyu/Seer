 package net.amunak.bukkit.plugin_DropsToInventory;
 
 /**
  * Copyright 2013 Jiří Barouš
  *
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.bukkit.Material;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * This is the main class of plugin DropsToInventory. It acts both as a plugin
  * and listener.
  *
  * @author Amunak
  */
 public class DropsToInventory extends JavaPlugin implements Listener {
 
     protected static Log log;
     protected FileConfiguration config;
     public List<String> filter;
     public List<String> safeBlocks;
 
     @Override
     public void onEnable() {
         log = new Log(this);
         log.fine("Plugin enabled");
 
         this.saveDefaultConfig();
         config = this.getConfig();
         filter = config.getStringList("filter");
         safeBlocks = config.getStringList("safeBlocks");
         for (String string : filter) {
             string = string.toUpperCase();
         }
 
         if (config.getBoolean("options.checkVersion")) {
             CheckVersion.check(this);
         }
 
         this.getServer().getPluginManager().registerEvents(this, this);
     }
 
     @Override
     public void onDisable() {
         HandlerList.unregisterAll((JavaPlugin) this);
         log.fine("Plugin disabled");
     }
 
     @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
     public void onBlockBreakEvent(BlockBreakEvent event) {
         if (!config.getBoolean("options.useOnlySafeBlocks")
                 || safeBlocks.contains(event.getBlock().getType().toString())) {
             if (config.getBoolean("options.ignoreEnchantmentBug")
                     || (event.getPlayer().getInventory().getItemInHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 0
                     && event.getPlayer().getInventory().getItemInHand().getEnchantmentLevel(Enchantment.SILK_TOUCH) == 0)) {
                 if (config.get("options.filterMode").equals("blacklist")) {
                     if (!filter.contains(event.getBlock().getType().toString())) {
                         this.moveToInventory(event);
                     }
                 } else if (config.get("options.filterMode").equals("whitelist")) {
                     if (filter.contains(event.getBlock().getType().toString())) {
                         this.moveToInventory(event);
                     }
                 } else {
                     this.moveToInventory(event);
                 }
             }
         }
     }
 
     private void moveToInventory(BlockBreakEvent event) {
         HashMap<Integer, ItemStack> leftover;
         Player player;
 
         player = event.getPlayer();
 
        player.giveExp(event.getExpToDrop());
        event.setCancelled(true);        
 
         leftover = player.getInventory().addItem(event.getBlock().getDrops(player.getItemInHand()).toArray(new ItemStack[0]));
 
         for (Map.Entry<Integer, ItemStack> entry : leftover.entrySet()) {
             event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), entry.getValue());
         }
 
         event.getBlock().setTypeId(Material.AIR.getId());
     }
 }
