 /*
  * This file is part of MinecartRevolution-Core.
  * Copyright (c) 2012 QuarterCode <http://www.quartercode.com/>
  *
  * MinecartRevolution-Core is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * MinecartRevolution-Core is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with MinecartRevolution-Core. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.quartercode.minecartrevolution.core.listener;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import com.quartercode.minecartrevolution.core.MinecartRevolution;
 import com.quartercode.minecartrevolution.core.control.block.ControlBlock;
 import com.quartercode.minecartrevolution.core.control.block.ControlBlockInfo;
 import com.quartercode.minecartrevolution.core.control.sign.ControlSign;
 import com.quartercode.minecartrevolution.core.control.sign.ControlSignInfo;
 import com.quartercode.minecartrevolution.core.get.Perm;
 import com.quartercode.quarterbukkit.api.ItemData;
 
 public class BlockListener implements Listener {
 
     private final MinecartRevolution minecartRevolution;
 
     public BlockListener(MinecartRevolution minecartRevolution) {
 
         this.minecartRevolution = minecartRevolution;
         Bukkit.getPluginManager().registerEvents(this, minecartRevolution.getPlugin());
     }
 
     @EventHandler
     public void onBlockPlace(BlockPlaceEvent event) {
 
         if (event.getBlock().getType() == Material.RAILS || event.getBlock().getType() == Material.POWERED_RAIL || event.getBlock().getType() == Material.DETECTOR_RAIL) {
             for (Block block : minecartRevolution.getControlBlockExecutor().getBlocks(event.getBlock().getLocation())) {
                 for (ControlBlock controlBlock : minecartRevolution.getControlBlockExecutor().getControlBlocks()) {
                     ControlBlockInfo info = controlBlock.getInfo();
 
                     for (ItemData itemData : info.getItemDatas()) {
                         if (itemData.equals(block)) {
                             if (!Perm.has(event.getPlayer(), info.getPlacePermission()) || !controlBlock.allowPlace(event.getPlayer(), block)) {
                                 event.setCancelled(true);
                                 event.getPlayer().sendMessage(MinecartRevolution.getLang().get("control.place.noPermission"));
                             } else {
                                 event.getPlayer().sendMessage(MinecartRevolution.getLang().get("control.place", "type", info.getName()));
                             }
                         }
                     }
                 }
             }
         }
     }
 
     @EventHandler
     public void onBlockBreak(BlockBreakEvent event) {
 
         if (event.getBlock().getType() == Material.RAILS || event.getBlock().getType() == Material.POWERED_RAIL || event.getBlock().getType() == Material.DETECTOR_RAIL) {
             for (Block block : minecartRevolution.getControlBlockExecutor().getBlocks(event.getBlock().getLocation())) {
                 for (ControlBlock controlBlock : minecartRevolution.getControlBlockExecutor().getControlBlocks()) {
                     ControlBlockInfo info = controlBlock.getInfo();
 
                     for (ItemData itemData : info.getItemDatas()) {
                         if (itemData.equals(block)) {
                             if (!Perm.has(event.getPlayer(), info.getDestroyPermission()) || !controlBlock.allowDestroy(event.getPlayer(), block)) {
                                 event.setCancelled(true);
                                 event.getPlayer().sendMessage(MinecartRevolution.getLang().get("control.destroy.noPermission"));
                             } else {
                                 event.getPlayer().sendMessage(MinecartRevolution.getLang().get("control.destroy", "type", info.getName()));
                             }
                         }
                     }
                 }
             }
         }
     }
 
     @EventHandler
     public void onSignChange(SignChangeEvent event) {
 
         String label = event.getLine(0);
         label = ControlSignInfo.getUnformattedLabel(label);
 
         for (ControlSign controlSign : minecartRevolution.getControlSignExecutor().getControlSigns()) {
             ControlSignInfo info = controlSign.getInfo();
 
             for (String signLabel : info.getLabels()) {
                 if (signLabel.equalsIgnoreCase(label)) {
                     if (!Perm.has(event.getPlayer(), info.getPlacePermission()) || !controlSign.allowPlace(event.getPlayer(), event.getLines(), (Sign) event.getBlock().getState())) {
                         if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                             event.getBlock().setType(Material.AIR);
                         } else {
                             event.getBlock().breakNaturally();
                         }
 
                         event.getPlayer().sendMessage(MinecartRevolution.getLang().get("control.place.noPermission"));
                     } else {
                         event.getPlayer().sendMessage(MinecartRevolution.getLang().get("control.place", "type", info.getName()));
                     }
                 }
             }
         }
     }
 
     @EventHandler
     public void onSignBreak(BlockBreakEvent event) {
 
        if (event.getBlock().getState() instanceof Sign) {
             Sign sign = (Sign) event.getBlock().getState();
             String label = sign.getLine(0);
             label = ControlSignInfo.getUnformattedLabel(label);
 
             for (ControlSign controlSign : minecartRevolution.getControlSignExecutor().getControlSigns()) {
                 ControlSignInfo info = controlSign.getInfo();
 
                 for (String signLabel : info.getLabels()) {
                     if (signLabel.equalsIgnoreCase(label)) {
                         if (!Perm.has(event.getPlayer(), info.getDestroyPermission()) || !controlSign.allowDestroy(event.getPlayer(), ((Sign) event.getBlock().getState()).getLines(), (Sign) event.getBlock().getState())) {
                             event.setCancelled(true);
                             event.getPlayer().sendMessage(MinecartRevolution.getLang().get("control.destroy.noPermission"));
                         } else {
                             event.getPlayer().sendMessage(MinecartRevolution.getLang().get("control.destroy", "type", info.getName()));
                         }
                     }
                 }
             }
         }
     }
 
 }
