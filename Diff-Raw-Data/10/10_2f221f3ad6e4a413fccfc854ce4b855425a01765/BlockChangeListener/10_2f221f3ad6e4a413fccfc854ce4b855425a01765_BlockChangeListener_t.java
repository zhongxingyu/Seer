 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of TheRock.
  * 
  * TheRock is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * TheRock is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with TheRock.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.therock.listener;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockFromToEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.entity.EntityChangeBlockEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
 import org.bukkit.event.player.PlayerBucketFillEvent;
 
 import de.minestar.therock.Core;
 import de.minestar.therock.data.BlockEventTypes;
 import de.minestar.therock.manager.MainConsumer;
 import de.minestar.therock.manager.MainManager;
 
 public class BlockChangeListener implements Listener {
 
     private MainManager mainManager;
     private MainConsumer mainConsumer;
     private StringBuilder queueBuilder;
 
     private static final Set<Integer> nonFluidProofBlocks = new HashSet<Integer>(Arrays.asList(6, 27, 28, 31, 32, 37, 38, 39, 40, 50, 51, 55, 59, 66, 69, 70, 72, 75, 76, 78, 83, 93, 94, 104, 105, 106, 115, 127, 131, 132));
     private final BlockFace[] faces = new BlockFace[]{BlockFace.DOWN, BlockFace.NORTH, BlockFace.WEST, BlockFace.EAST, BlockFace.SOUTH};
 
     public BlockChangeListener() {
         this.mainManager = Core.mainManager;
         this.mainConsumer = Core.mainConsumer;
         this.queueBuilder = new StringBuilder();
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onBlockBreak(BlockBreakEvent event) {
         // /////////////////////////////////
         // event cancelled => return
         // /////////////////////////////////
         if (event.isCancelled() || !this.mainManager.isWorldWatched(event.getBlock().getWorld().getName()) || !this.mainManager.getWorld(event.getPlayer()).logBlockBreak())
             return;
 
         // /////////////////////////////////
         // create data
         // /////////////////////////////////
         this.addBlockChange(event.getPlayer().getName(), BlockEventTypes.PLAYER_BREAK.getID(), event.getBlock().getWorld().getName(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), event.getBlock().getTypeId(), event.getBlock().getData(), Material.AIR.getId(), (byte) Material.AIR.getId());
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onBlockPlace(BlockPlaceEvent event) {
         // /////////////////////////////////
         // event cancelled => return
         // /////////////////////////////////
         if (event.isCancelled() || !this.mainManager.isWorldWatched(event.getBlock().getWorld().getName()) || !this.mainManager.getWorld(event.getPlayer()).logBlockPlace())
             return;
 
         // /////////////////////////////////
         // create data : all, except signs
         // /////////////////////////////////
         if (event.getBlock().getTypeId() != Material.WALL_SIGN.getId() && event.getBlock().getTypeId() != Material.SIGN_POST.getId()) {
             this.addBlockChange(event.getPlayer().getName(), BlockEventTypes.PLAYER_PLACE.getID(), event.getBlock().getWorld().getName(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), event.getBlockReplacedState().getTypeId(), event.getBlockReplacedState().getRawData(), event.getBlockPlaced().getTypeId(), event.getBlockPlaced().getData());
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onBlockExplode(EntityExplodeEvent event) {
         // /////////////////////////////////
         // event cancelled => return
         // /////////////////////////////////
         if (event.isCancelled() || !this.mainManager.isWorldWatched(event.getEntity().getWorld().getName()) || !this.mainManager.getWorld(event.getEntity().getWorld()).logEntityBlockExplode())
             return;
 
         for (Block block : event.blockList()) {
             // /////////////////////////////////
             // create data
             // /////////////////////////////////
             this.addBlockChange(event.getEntityType().getName(), BlockEventTypes.PHYSICS_DESTROY.getID(), block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), block.getTypeId(), block.getData(), Material.AIR.getId(), (byte) Material.AIR.getId());
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onEntityChangeBlock(EntityChangeBlockEvent event) {
         // /////////////////////////////////
         // event cancelled => return
         // /////////////////////////////////
         if (event.isCancelled() || !this.mainManager.isWorldWatched(event.getEntity().getWorld().getName()) || !this.mainManager.getWorld(event.getEntity().getWorld()).logEntityBlockChange())
             return;
 
         // /////////////////////////////////
         // create data
         // /////////////////////////////////
         this.addBlockChange(event.getEntityType().getName(), BlockEventTypes.PHYSICS_DESTROY.getID(), event.getBlock().getWorld().getName(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), event.getBlock().getTypeId(), event.getBlock().getData(), event.getTo().getId(), (byte) 0);
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onBlockFromTo(BlockFromToEvent event) {
         // /////////////////////////////////
         // event cancelled => return
         // /////////////////////////////////
         if (event.isCancelled() || !this.mainManager.isWorldWatched(event.getBlock().getWorld().getName()))
             return;
 
         final int typeFrom = event.getBlock().getTypeId();
         final Block toBlock = event.getToBlock();
         final int replacedID = toBlock.getState().getTypeId();
         final byte replacedData = toBlock.getState().getRawData();
         final int newID = event.getBlock().getTypeId();
         final byte newData = (byte) (event.getBlock().getData() + 1);
 
         final boolean canFlow = (replacedID == Material.AIR.getId() || nonFluidProofBlocks.contains(replacedID));
 
         if (!canFlow) {
             return;
         }
 
         if (this.mainManager.getWorld(event.getBlock()).logLavaFlow() && (typeFrom == 10 || typeFrom == 11)) {
             // /////////////////////////////////
             // create data : lavaflow & blockcreation
             // /////////////////////////////////
             if (replacedID != Material.AIR.getId()) {
                 // DONE!
                 Bukkit.getServer().broadcastMessage("Lavaflow destroyed " + Material.getMaterial(replacedID) + ":" + replacedData);
                 this.addBlockChange("Lavaflow", BlockEventTypes.PHYSICS_DESTROY.getID(), toBlock.getWorld().getName(), toBlock.getX(), toBlock.getY(), toBlock.getZ(), replacedID, replacedData, newID, newData);
             }
 
             for (BlockFace blockFace : faces) {
                 // TODO: fix here
                 final Block lower = toBlock.getRelative(blockFace);
                 if (lower.getTypeId() == 8 || lower.getTypeId() == 9) {
                     if (lower.getData() <= 2) {
                         Bukkit.getServer().broadcastMessage("Lavaflow created " + Material.getMaterial(1) + ":" + replacedData);
                         this.addBlockChange("Lavaflow", BlockEventTypes.PHYSICS_CREATE.getID(), toBlock.getWorld().getName(), lower.getX(), lower.getY(), lower.getZ(), lower.getTypeId(), lower.getData(), Material.STONE.getId(), (byte) 0);
                     } else {
                         Bukkit.getServer().broadcastMessage("Lavaflow created " + Material.getMaterial(4) + ":" + replacedData);
                         this.addBlockChange("Lavaflow", BlockEventTypes.PHYSICS_CREATE.getID(), toBlock.getWorld().getName(), lower.getX(), lower.getY(), lower.getZ(), lower.getTypeId(), lower.getData(), Material.COBBLESTONE.getId(), (byte) 0);
                     }
                 }
             }
         } else if (this.mainManager.getWorld(event.getBlock()).logWaterFlow() && (typeFrom == 8 || typeFrom == 9)) {
             // /////////////////////////////////
             // create data : waterflow & blockcreation
             // /////////////////////////////////
             if (replacedID != Material.AIR.getId()) {
                 // DONE!
                 Bukkit.getServer().broadcastMessage("Waterflow destroyed " + Material.getMaterial(replacedID) + ":" + replacedData);
                 this.addBlockChange("Waterflow", BlockEventTypes.PHYSICS_DESTROY.getID(), toBlock.getWorld().getName(), toBlock.getX(), toBlock.getY(), toBlock.getZ(), replacedID, replacedData, 8, newData);
             }
 
             for (BlockFace blockFace : faces) {
                 // TODO: fix here
                 final Block relative = toBlock.getRelative(blockFace);
                 if (relative.getTypeId() == 10 || relative.getTypeId() == 11) {
                     if (relative.getData() == 0) {
                         Bukkit.getServer().broadcastMessage("Waterflow created " + Material.getMaterial(49) + ":" + replacedData);
                         this.addBlockChange("Waterflow", BlockEventTypes.PHYSICS_CREATE.getID(), toBlock.getWorld().getName(), relative.getX(), relative.getY(), relative.getZ(), relative.getTypeId(), relative.getData(), Material.OBSIDIAN.getId(), (byte) 0);
                     } else {
                         if (relative.getData() < 6) {
                             Bukkit.getServer().broadcastMessage("Waterflow created " + Material.getMaterial(4) + ":" + replacedData);
                             this.addBlockChange("Waterflow", BlockEventTypes.PHYSICS_CREATE.getID(), toBlock.getWorld().getName(), relative.getX(), relative.getY(), relative.getZ(), relative.getTypeId(), relative.getData(), Material.COBBLESTONE.getId(), (byte) 0);
                         }
                     }
                 }
             }
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
         // event cancelled => return
         if (event.isCancelled() || !this.mainManager.isWorldWatched(event.getBlockClicked().getWorld().getName()) || !this.mainManager.getWorld(event.getPlayer()).logBucketEmpty())
             return;
 
         // /////////////////////////////////
         // create data
         // /////////////////////////////////
         Block block = event.getBlockClicked().getRelative(event.getBlockFace());
         this.addBlockChange(event.getPlayer().getName(), BlockEventTypes.PLAYER_PLACE.getID(), block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), block.getState().getTypeId(), block.getState().getRawData(), (event.getBucket() == Material.WATER_BUCKET ? 9 : 11), (byte) 0);
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerBucketFill(PlayerBucketFillEvent event) {
         // event cancelled => return
         if (event.isCancelled() || !this.mainManager.isWorldWatched(event.getBlockClicked().getWorld().getName()) || !this.mainManager.getWorld(event.getPlayer()).logBucketFill())
             return;
 
         // /////////////////////////////////
         // create data
         // /////////////////////////////////
         Block block = event.getBlockClicked().getRelative(event.getBlockFace());
         this.addBlockChange(event.getPlayer().getName(), BlockEventTypes.PLAYER_BREAK.getID(), block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), block.getState().getTypeId(), block.getState().getRawData(), Material.AIR.getId(), (byte) 0);
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onSignChange(SignChangeEvent event) {
         // event cancelled => return
         if (event.isCancelled() || !this.mainManager.isWorldWatched(event.getBlock().getWorld()) || !this.mainManager.getWorld(event.getPlayer()).logBlockPlace())
             return;
 
         // /////////////////////////////////
         // create data
         // /////////////////////////////////
         Block block = event.getBlock();
         String signData = event.getLine(0) + "`" + event.getLine(1) + "`" + event.getLine(2) + "`" + event.getLine(3);
         this.addBlockChange(event.getPlayer().getName(), BlockEventTypes.PLAYER_PLACE.getID(), block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), 0, (byte) 0, block.getTypeId(), block.getData(), signData);
     }
 
     private void addBlockChange(String reason, int eventType, String worldName, int blockX, int blockY, int blockZ, int fromID, byte fromData, int toID, byte toData) {
        this.addBlockChange(reason, eventType, worldName, blockX, blockY, blockZ, fromID, fromData, toID, toData, "");
     }
 
     private void addBlockChange(String reason, int eventType, String worldName, int blockX, int blockY, int blockZ, int fromID, byte fromData, int toID, byte toData, String extraData) {
         // "("
         this.queueBuilder.append("(");
         // "TIMESTAMP"
         this.queueBuilder.append(System.currentTimeMillis());
         // "REASON"
         this.queueBuilder.append(", ");
         this.queueBuilder.append("'" + reason + "'");
         // "EVENTTYPE"
         this.queueBuilder.append(", ");
         this.queueBuilder.append(eventType);
         // "POSITION: X"
         this.queueBuilder.append(", ");
         this.queueBuilder.append(blockX);
         // "POSITION: Y"
         this.queueBuilder.append(", ");
         this.queueBuilder.append(blockY);
         // "POSITION: Z"
         this.queueBuilder.append(", ");
         this.queueBuilder.append(blockZ);
         // "FROM ID"
         this.queueBuilder.append(", ");
         this.queueBuilder.append(fromID);
         // "FROM SUBDATA"
         this.queueBuilder.append(", ");
         this.queueBuilder.append(fromData);
         // "TO ID"
         this.queueBuilder.append(", ");
         this.queueBuilder.append(toID);
         // "TO SUBDATA"
         this.queueBuilder.append(", ");
         this.queueBuilder.append(toData);
         // "EXTRADATA"
         this.queueBuilder.append(", ");
         this.queueBuilder.append("'");
         this.queueBuilder.append(extraData);
         this.queueBuilder.append("'");
         // ")"
         this.queueBuilder.append(")");
 
         // /////////////////////////////////
         // add to queue
         // /////////////////////////////////
         this.mainConsumer.appendBlockEvent(worldName, this.queueBuilder);
 
         // /////////////////////////////////
         // reset data
         // /////////////////////////////////
         this.queueBuilder.setLength(0);
     }
 }
