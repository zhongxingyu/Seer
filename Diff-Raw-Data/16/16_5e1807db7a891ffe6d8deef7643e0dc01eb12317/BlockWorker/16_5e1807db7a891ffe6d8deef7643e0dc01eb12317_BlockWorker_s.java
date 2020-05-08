 /*
  * Copyright 2013 Topher Donovan (themaskedcrusader.com)
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package com.themaskedcrusader.tmcz.modules.items;
 
 import com.themaskedcrusader.bukkit.config.Settings;
 import com.themaskedcrusader.tmcz.data.BlockBean;
 import com.themaskedcrusader.tmcz.data.PlayerUtil;
 import com.themaskedcrusader.tmcz.settings.Permissions;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class BlockWorker extends Items implements Listener {
     private static HashMap<Integer, BlockBean> allowedItems = new HashMap<Integer, BlockBean>();
     private static HashMap<Location, BlockBean> placedBlocks = new HashMap<Location, BlockBean>();
     protected static HashMap<Location, BlockBean> brokenBlocks = new HashMap<Location, BlockBean>();
 
     private static final String SUBSYSTEM   = ".block-protection";
     public static final String _PLACE       = SYSTEM + SUBSYSTEM + ".place";
     public static final String _BREAK       = SYSTEM + SUBSYSTEM + ".break";
     public static final String _IN_GAME     = SYSTEM + SUBSYSTEM + IN_GAME;
     public static final String _SERVER_WIDE = SYSTEM + SUBSYSTEM + SERVER_WIDE;
     public static final String _OP_BUILD    = SYSTEM + SUBSYSTEM + ".op-can-build";
     public static final String _ITEMS       = SYSTEM + SUBSYSTEM + ".allowed-items";
 
     protected BlockWorker(JavaPlugin plugin) {
         registerAllowedItems(plugin);
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
 
         if (Settings.getConfig().getBoolean(_PLACE)) {
             plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
                 public void run() {
                     BlockWorker.cleanUpPlacedBlocks(false);
                 }
             }, 15L, 600L);
         }
 
         if (Settings.getConfig().getBoolean(_BREAK)) {
             plugin.getServer().getPluginManager().registerEvents(this, plugin);
             plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
                 public void run() {
                     BlockWorker.cleanUpBrokenBlocks(false);
                 }
             }, 15L, 600L);
         }
     }
 
     @EventHandler
     public void onBlockPlace(BlockPlaceEvent event) {
         event.setCancelled(trackBlockInteraction(event.getPlayer(), event.getBlockPlaced(), placedBlocks, true));
     }
 
     @EventHandler
     public void onBlockBreak(BlockBreakEvent event) {
         if (placedBlocks.containsKey(event.getBlock().getLocation())) {
             breakPlacedBlock(event);
         } else {
             boolean blocked = trackBlockInteraction(event.getPlayer(), event.getBlock(), brokenBlocks, false);
             if (blocked) {
                 event.setCancelled(true);
             } else {
                 if (event.getBlock().getType() == Material.TORCH) {
                     event.setCancelled(true);
                     event.getBlock().setType(Material.AIR);
                 }
             }
         }
     }
 
     @SuppressWarnings("unchecked")
     public boolean trackBlockInteraction(Player player, Block block, Map tracker, boolean place) {
         if (isProtectionInPlace(player)) {
             if (player.hasPermission(Permissions.BUILDER) || (
                     Settings.getConfig().getBoolean(_OP_BUILD) && player.isOp())) {
                 return false;
             } else if (Settings.getConfig().getBoolean(_IN_GAME) && !PlayerUtil.isPlaying(player)) {
                 return true;
             } else {
                 if (!allowedItems.containsKey(block.getTypeId())) { return true; }
                 if (place && allowedItems.get(block.getTypeId()).getDespawnSeconds() == 0)   { return true;   }
                 if (place && allowedItems.get(block.getTypeId()).getDespawnSeconds() == -1)  { return false;  }
                 if (!place && allowedItems.get(block.getTypeId()).getRespawnSeconds() == 0)  { return true;   }
                 if (!place && allowedItems.get(block.getTypeId()).getRespawnSeconds() == -1) { return false;  }
                 tracker.put(block.getLocation(), new BlockBean(block));
             }
         }
         return false;
     }
 
     public void breakPlacedBlock(BlockBreakEvent event) {
         if (!event.getPlayer().hasPermission(Permissions.BUILDER) && (
                 !Settings.getConfig().getBoolean(_OP_BUILD) || !event.getPlayer().isOp()) &&
                 isProtectionInPlace(event.getPlayer())) {
 
             if (placedBlocks.containsKey(event.getBlock().getLocation())) {
                 if (Settings.getConfig().getBoolean(_IN_GAME) && !PlayerUtil.isPlaying(event.getPlayer())) {
                     event.setCancelled(true);
                 } else {
                     placedBlocks.remove(event.getBlock().getLocation());
                 }
             } else {
                 event.setCancelled(true);
             }
         }
     }
 
     private boolean isProtectionInPlace(Player player) {
         return isAllowed(player, _SERVER_WIDE, IN_GAME);
     }
 
     private void registerAllowedItems(JavaPlugin plugin) {
         List<String> config = Settings.getConfig().getStringList(_ITEMS);
         for (String s : config) {
             try {
                 String[] item = s.split("\\|");
                 int id = Integer.parseInt(item[0]);
                 int despawnSeconds = Integer.parseInt(item[1]);
                 int respawnSeconds = Integer.parseInt(item[2]);
                 allowedItems.put(id, new BlockBean(id, despawnSeconds, respawnSeconds));
             } catch (Exception e) {
                 plugin.getLogger().info("Error parsing allowed items: incorrect format - [" + s + "]");
             }
         }
     }
 
     public static void cleanUpBlocks(boolean force) {
         cleanUpPlacedBlocks(force);
         cleanUpBrokenBlocks(force);
     }
 
     private static void cleanUpPlacedBlocks(boolean force) {
         long now = new Date().getTime();
         for (Map.Entry<Location, BlockBean> entry : placedBlocks.entrySet()) {
            BlockBean block = entry.getValue();
            BlockBean info  = allowedItems.get(block.getMaterial().getId());
            long place = block.getDate().getTime();
            if (force || now - place > info.getDespawnSeconds() * 1000) {
                entry.getKey().getWorld().getBlockAt(entry.getKey()).setType(Material.AIR);
                placedBlocks.remove(entry.getKey());
             }
         }
     }
 
     private static void cleanUpBrokenBlocks(boolean force) {
         long now = new Date().getTime();
         for (Map.Entry<Location, BlockBean> entry : brokenBlocks.entrySet()) {
             BlockBean block = entry.getValue();
             BlockBean info = allowedItems.get(block.getMaterial().getId());
             long broken = block.getDate().getTime();
             if (force || (now - broken > info.getRespawnSeconds() * 1000)) {
                 Block toRepair = entry.getKey().getWorld().getBlockAt(entry.getKey());
                 toRepair.setType(block.getMaterial());
                 toRepair.setData(block.getData());
                 placedBlocks.remove(entry.getKey());
             }
         }
     }
 }
