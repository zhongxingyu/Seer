 /*
  * Copyright (C) 2012 BangL <henno.rickowski@googlemail.com>
  *                    mewin <mewin001@hotmail.de>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.bangl.wgtff.listeners;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.flags.StateFlag;
 import com.sk89q.worldguard.protection.flags.StateFlag.State;
 import de.bangl.wgtff.WGTreeFarmFlagPlugin;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.LeavesDecayEvent;
 import org.bukkit.inventory.ItemStack;
 
 /**
  *
  * @author BangL <henno.rickowski@googlemail.com>
  * @author mewin <mewin001@hotmail.de>
  */
 public class PlayerListener implements Listener {
     private WGTreeFarmFlagPlugin plugin;
     private boolean hasBlockRestricter;
 
     // Command flags
     public static final StateFlag FLAG_TREEFARM = new StateFlag("treefarm", true);
 
     public PlayerListener(WGTreeFarmFlagPlugin plugin) {
         this.plugin = plugin;
 
         // Register custom flags
         plugin.getWGCFP().addCustomFlag(FLAG_TREEFARM);
 
         // Register events
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
         
         hasBlockRestricter = plugin.getServer().getPluginManager().getPlugin("WGBlockRestricter") != null;
 
     }
 
     @EventHandler
     public void onBlockPlace(BlockPlaceEvent event) {
         
         final WorldGuardPlugin wgp = plugin.getWGP();
         final Block block = event.getBlock();
         final Player player = event.getPlayer();
         
         //lets block restricter handle it
         if (!hasBlockRestricter
                 && !wgp.getRegionManager(block.getWorld()).getApplicableRegions(block.getLocation()).allows(FLAG_TREEFARM)) {
             // treefarm is set to "deny"
             // so let's cancel this placement
             // an op/member/owner can still build, if treefarm is set to "allow".
             final String msg = this.plugin.getConfig().getString("messages.block.blockplace");
             player.sendMessage(ChatColor.RED + msg);
             event.setCancelled(true);
         }
     }
 
     public void damageItemInHand(Player player) {
         if (player.getGameMode() == GameMode.CREATIVE) {
             return;
         }
         ItemStack result = new ItemStack(player.getItemInHand());
         short dur = result.getDurability();
         short max = 0;
         switch (result.getType()) {
             case GOLD_AXE:
             case GOLD_HOE:
             case GOLD_SPADE:
             case GOLD_PICKAXE:
             case GOLD_SWORD:
                 max = 33;
                 dur = Short.valueOf(String.valueOf(dur + 1));
                 break;
             case WOOD_AXE:
             case WOOD_HOE:
             case WOOD_SPADE:
             case WOOD_PICKAXE:
             case WOOD_SWORD:
                 max = 60;
                 dur = Short.valueOf(String.valueOf(dur + 1));
                 break;
             case FISHING_ROD:
                 max = 65;
                 dur = Short.valueOf(String.valueOf(dur + 1));
                 break;
             case STONE_AXE:
             case STONE_HOE:
             case STONE_SPADE:
             case STONE_PICKAXE:
             case STONE_SWORD:
                 max = 132;
                 dur = Short.valueOf(String.valueOf(dur + 1));
                 break;
             case SHEARS:
                 max = 238;
                 dur = Short.valueOf(String.valueOf(dur + 1));
                 break;
             case IRON_AXE:
             case IRON_HOE:
             case IRON_SPADE:
             case IRON_PICKAXE:
             case IRON_SWORD:
                 max = 251;
                 dur = Short.valueOf(String.valueOf(dur + 1));
                 break;
             case BOW:
                 max = 385;
                 dur = Short.valueOf(String.valueOf(dur + 1));
                 break;
             case DIAMOND_AXE:
             case DIAMOND_HOE:
             case DIAMOND_SPADE:
             case DIAMOND_PICKAXE:
             case DIAMOND_SWORD:
                 max = 1562;
                 dur = Short.valueOf(String.valueOf(dur + 1));
                 break;
             default:
                 max = 0;
                 break;
         }
         if (max > 0 && dur >= max) {
             result = null;
         } else {
             result.setDurability(dur);
         }
         player.setItemInHand(result);
     }
     
     @EventHandler
     public void onBlockBreak(BlockBreakEvent event) {
 
         final WorldGuardPlugin wgp = plugin.getWGP();
         final Location loc = event.getBlock().getLocation();
         final Block block = event.getBlock();
         final World world = block.getWorld();
         final Player player = event.getPlayer();
         final Material material = block.getType();
         final State state = wgp.getRegionManager(world).getApplicableRegions(loc).getFlag(FLAG_TREEFARM);
 
         // handle if ((allowed treefarm region
         // and player is not op
         // and can not build)
         // or denied treefarm region)
         if (state != null
                 && (state == State.DENY
                 || (!player.isOp()
                 && !wgp.canBuild(player, block)))) {
 
             if (material == Material.LOG) {
                 // Log destroyed
                 byte data = block.getData();
 
                 // drop log if player is not in creative mode
                if (player.getGameMode() == GameMode.CREATIVE) {
                     if (player.getItemInHand() == null) {
                         block.breakNaturally();
                     } else {
                         block.breakNaturally(player.getItemInHand());
                     }
                 }
 
                 // this was a tree-base?
                 final Location locUnder = event.getBlock().getLocation();
                 locUnder.setY(block.getY() - 1.0D);
                 if ((locUnder.getBlock().getType() == Material.DIRT)
                         || (locUnder.getBlock().getType() == Material.GRASS)) {
                     // Turn log to sapling
                     block.setTypeIdAndData(Material.SAPLING.getId(), data, false);
                 } else {
                     // Turn log to air
                     block.setType(Material.AIR);
                 }
                 damageItemInHand(player);
             } else if (material == Material.LEAVES) {
                 // Leaf destroyed
 
                 // Turn leaf to air
                 block.setType(Material.AIR);
                 damageItemInHand(player);
             } else if (material == Material.SAPLING) {
                 // Sapling destroyed.
 
                 // Send Warning
                 final String msg = this.plugin.getConfig().getString("messages.block.saplingdestroy");
                 player.sendMessage(ChatColor.RED + msg);
             } else if (!hasBlockRestricter
                     || !com.mewin.WGBlockRestricter.Utils.blockAllowedAtLocation(wgp, material, loc)) {
                 // Any other block destroyed
                         
                 // Send Warning
                 final String msg = this.plugin.getConfig().getString("messages.block.blockdestroy");
                 player.sendMessage(ChatColor.RED + msg);
             } else {
                 return;
             }
             event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void onLeaveDecay(LeavesDecayEvent event) {
         
         final Block block = event.getBlock();
         final Location loc = block.getLocation();
         final World world = block.getWorld();
 
         // Cancel if treefarm region
         if (plugin.getWGP().getRegionManager(world).getApplicableRegions(loc).getFlag(FLAG_TREEFARM) != null) {
             // turn leave to air
             block.setType(Material.AIR);
             event.setCancelled(true);
         }
     }
 }
