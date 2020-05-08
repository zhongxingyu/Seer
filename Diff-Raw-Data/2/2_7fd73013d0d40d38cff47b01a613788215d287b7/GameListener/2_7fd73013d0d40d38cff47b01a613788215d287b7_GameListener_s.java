 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of CastAway.
  * 
  * CastAway is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * CastAway is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with CastAway.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.castaway.listener;
 
 import java.util.HashSet;
 
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityRegainHealthEvent;
 import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
 import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
 import org.bukkit.event.entity.FoodLevelChangeEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 import com.bukkit.gemo.utils.BlockUtils;
 import com.bukkit.gemo.utils.UtilPermissions;
 
 import de.minestar.castaway.blocks.AbstractActionBlock;
 import de.minestar.castaway.core.CastAwayCore;
 import de.minestar.castaway.core.Settings;
 import de.minestar.castaway.data.BlockVector;
 import de.minestar.castaway.data.Dungeon;
 import de.minestar.castaway.data.DungeonOption;
 import de.minestar.castaway.data.PlayerData;
 import de.minestar.core.MinestarCore;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class GameListener implements Listener {
 
     private PlayerData playerData;
     private BlockVector vector;
     private static HashSet<Action> acceptedActions;
     private static HashSet<RegainReason> blockedRegainReasons;
 
     static {
         acceptedActions = new HashSet<Action>();
         acceptedActions.add(Action.LEFT_CLICK_BLOCK);
         acceptedActions.add(Action.RIGHT_CLICK_BLOCK);
 
         blockedRegainReasons = new HashSet<RegainReason>();
         blockedRegainReasons.add(RegainReason.SATIATED);
         blockedRegainReasons.add(RegainReason.REGEN);
         blockedRegainReasons.add(RegainReason.MAGIC);
         blockedRegainReasons.add(RegainReason.MAGIC_REGEN);
     }
 
     public GameListener() {
         this.vector = new BlockVector("", 0, 0, 0);
     }
 
     // //////////////////////////////////////
     //
     // BLOCK-HANDLING
     //
     // //////////////////////////////////////
 
     @EventHandler(ignoreCancelled = true)
     public void onBlockPlace(BlockPlaceEvent event) {
         // cancel event, if in dungeon mode
         this.playerData = CastAwayCore.playerManager.getPlayerData(event.getPlayer());
         if (this.playerData.isInDungeon()) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onBlockBreak(BlockBreakEvent event) {
         // cancel event, if in dungeon mode
         this.playerData = CastAwayCore.playerManager.getPlayerData(event.getPlayer());
         if (this.playerData.isInDungeon()) {
             event.setCancelled(true);
         }
     }
 
     // //////////////////////////////////////
     //
     // COMMAND-HANDLING
     //
     // //////////////////////////////////////
 
     private String getCommand(String message) {
         String[] split = message.split(" ");
         if (split != null && split.length > 0) {
             return split[0];
         }
         return "";
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
         // Player must be in DungeonMode
         this.playerData = CastAwayCore.playerManager.getPlayerData(event.getPlayer());
         if (!this.playerData.isInDungeon()) {
             return;
         }
 
         // is the command accepted
         if (!Settings.getAcceptedCommands().contains(this.getCommand(event.getMessage()))) {
             PlayerUtils.sendError(event.getPlayer(), CastAwayCore.NAME, "Du befindest dich zur Zeit in einem Dungeon!");
             PlayerUtils.sendInfo(event.getPlayer(), "Nutzbare Kommandos:");
             // FORMAT ACCEPTED COMMANDS
             String s = Settings.getAcceptedCommands().toString();
             s = s.substring(1, s.length() - 1).replaceAll(", ", " | ");
             PlayerUtils.sendInfo(event.getPlayer(), s);
             event.setCancelled(true);
             return;
         }
     }
 
     // //////////////////////////////////////
     //
     // ENTITY-HANDLING
     //
     // //////////////////////////////////////
 
     @EventHandler(ignoreCancelled = true)
     public void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event) {
         // only players are affected
         if (event.getTarget().getType() == EntityType.PLAYER) {
             // get the player
             Player player = (Player) event.getTarget();
 
             // get PlayerData
             this.playerData = CastAwayCore.playerManager.getPlayerData(player);
 
             // cencel event, if the player is in a dungeon
             event.setCancelled(this.playerData.isInDungeon());
         }
     }
 
     // //////////////////////////////////////
     //
     // PLAYER-HANDLING
     //
     // //////////////////////////////////////
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerRespawn(PlayerRespawnEvent event) {
         // get PlayerData
         this.playerData = CastAwayCore.playerManager.getPlayerData(event.getPlayer());
 
         // do we have a respawnposition?
         if (this.playerData.hasRespawnLocation()) {
             // UPDATE THE SPAWN POSITION
             event.setRespawnLocation(this.playerData.getRespawnLocation());
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerInteract(PlayerInteractEvent event) {
         // get PlayerData
         this.playerData = CastAwayCore.playerManager.getPlayerData(event.getPlayer());
 
         // do we have a valid action?
         if (!acceptedActions.contains(event.getAction()))
             return;
 
         // update BlockVector
         this.vector.update(event.getClickedBlock());
 
         // is the block registered?
         AbstractActionBlock block = CastAwayCore.gameManager.getBlock(this.vector);
         if (block == null) {
             return;
         }
 
         // Player must be in DungeonMode, if the block wishes it
         if (!block.isExecuteIfNotInDungeon() && !this.playerData.isInDungeon()) {
             return;
         }
 
         // does the player have permissions to use dungeons?
         if (!UtilPermissions.playerCanUseCommand(event.getPlayer(), "castaway.dungeons.use")) {
             PlayerUtils.sendError(event.getPlayer(), CastAwayCore.NAME, "Du darfst Dungeons nicht benutzen!");
             event.setCancelled(true);
             return;
         }
 
         // handle action
         final Action action = event.getAction();
         boolean cancelEvent = false;
         switch (action) {
             case LEFT_CLICK_BLOCK : {
                 // handle left-click on a block
                 if (!block.isHandleLeftClick()) {
                     break;
                 }
                 cancelEvent = block.execute(event.getPlayer(), this.playerData);
                 break;
             }
             case RIGHT_CLICK_BLOCK : {
                 // handle right-click on a block
                 if (!block.isHandleRightClick()) {
                     break;
                 }
                 cancelEvent = block.execute(event.getPlayer(), this.playerData);
                 break;
             }
             default : {
                 // do nothing :{
                 break;
             }
         }
 
         // cancel the event, if the block wishes it
         if (cancelEvent) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onFoodLevelChange(FoodLevelChangeEvent event) {
         // only handle players
         if (event.getEntityType() != EntityType.PLAYER) {
             return;
         }
 
         Player player = (Player) event.getEntity();
         if (CastAwayCore.playerManager.getPlayerData(player.getName()).isInDungeon() && !CastAwayCore.playerManager.getPlayerData(player.getName()).getDungeon().hasOption(DungeonOption.HUNGER)) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onRegainHealth(EntityRegainHealthEvent event) {
         // only handle players
         if (event.getEntityType() != EntityType.PLAYER) {
             return;
         }
 
         Player player = (Player) event.getEntity();
         if (CastAwayCore.playerManager.getPlayerData(player.getName()).isInDungeon()) {
             Dungeon dungeon = CastAwayCore.playerManager.getPlayerData(player.getName()).getDungeon();
 
            if (dungeon.hasOption(DungeonOption.BLOCK_AUTO_REGAIN_HEALTH) && !event.getRegainReason().equals(RegainReason.MAGIC_REGEN) && !event.getRegainReason().equals(RegainReason.MAGIC)) {
                 event.setCancelled(true);
                 return;
             }
 
             if (!CastAwayCore.playerManager.getPlayerData(player.getName()).getDungeon().hasOption(DungeonOption.ENABLE_AUTO_REGAIN_HEALTH)) {
                 if (blockedRegainReasons.contains(event.getRegainReason())) {
                     event.setCancelled(true);
                     return;
                 }
             }
         }
     }
 
     @EventHandler
     public void onPlayerMove(PlayerMoveEvent event) {
         // only on blockchange
         if (BlockUtils.LocationEquals(event.getFrom(), event.getTo())) {
             return;
         }
 
         // update BlockVector
         this.vector.update(event.getTo().getBlock());
 
         // is the block registered?
         AbstractActionBlock block = CastAwayCore.gameManager.getBlock(this.vector);
         if (block == null) {
             return;
         }
 
         // Player must be in DungeonMode, if the block wishes it
         if (!block.isExecuteIfNotInDungeon() && !this.playerData.isInDungeon()) {
             return;
         }
 
         // handle physical action
         if (!block.isHandlePhysical()) {
             return;
         }
 
         // does the player have permissions to use dungeons?
         if (!UtilPermissions.playerCanUseCommand(event.getPlayer(), "castaway.dungeons.use")) {
             PlayerUtils.sendError(event.getPlayer(), CastAwayCore.NAME, "Du darfst Dungeons nicht benutzen!");
             event.setTo(event.getFrom().clone());
             event.setCancelled(true);
             return;
         }
 
         // get PlayerData
         this.playerData = CastAwayCore.playerManager.getPlayerData(event.getPlayer());
 
         // if we have a registered block -> handle the action
         boolean cancelEvent = block.execute(event.getPlayer(), this.playerData);
         if (cancelEvent) {
             event.setTo(event.getFrom().clone());
             event.setCancelled(true);
         }
     }
 
     @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
     public void onPlayerDeath(PlayerDeathEvent event) {
         this.playerData = CastAwayCore.playerManager.getPlayerData(((Player) event.getEntity()).getName());
         if (this.playerData.isInDungeon()) {
             Player player = (Player) event.getEntity();
             event.setDeathMessage("");
             event.setKeepLevel(true);
             event.setDroppedExp(0);
 
             Dungeon dungeon = CastAwayCore.playerManager.getPlayerData(player).getDungeon();
 
             // CLEAR INVENTORY ON DEATH
             if (dungeon.hasOption(DungeonOption.CLEAR_INVENTORY_ON_DEATH)) {
                 event.getDrops().clear();
                 player.getInventory().clear();
                 player.getInventory().setBoots(null);
                 player.getInventory().setChestplate(null);
                 player.getInventory().setHelmet(null);
                 player.getInventory().setLeggings(null);
             }
 
             // FIRE
             if (!dungeon.hasOption(DungeonOption.KEEP_DUNGEON_MODE_ON_DEATH)) {
                 this.playerData.quitDungeon();
             }
         }
     }
 
     private void onPlayerDisconnect(Player player) {
         this.playerData = CastAwayCore.playerManager.getPlayerData(player);
         if (this.playerData.isInDungeon()) {
             // exit the dungeon
             this.playerData.getDungeon().playerQuit(this.playerData);
             // TP to spawn on next connect
             MinestarCore.getPlayer(player).setBoolean("main.wasHere", false);
         }
     }
 
     @EventHandler
     public void onPlayerQuit(PlayerQuitEvent event) {
         this.onPlayerDisconnect(event.getPlayer());
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerKick(PlayerKickEvent event) {
         this.onPlayerDisconnect(event.getPlayer());
     }
 }
