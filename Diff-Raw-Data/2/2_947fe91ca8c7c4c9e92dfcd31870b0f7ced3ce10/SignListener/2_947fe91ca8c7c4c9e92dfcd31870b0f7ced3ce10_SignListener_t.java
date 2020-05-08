 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of FifthElement.
  * 
  * FifthElement is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * FifthElement is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with FifthElement.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.FifthElement.listener;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import com.bukkit.gemo.utils.UtilPermissions;
 
 import de.minestar.FifthElement.core.Core;
 import de.minestar.FifthElement.data.Warp;
 import de.minestar.FifthElement.statistics.warp.WarpSignStat;
 import de.minestar.illuminati.IlluminatiCore;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class SignListener implements Listener {
 
     private final static String IGNORE_USE_MODE = "fifthelement.command.ignoreusemode";
 
     @EventHandler
     public void onSignChange(SignChangeEvent event) {
         if (event.isCancelled() || !(event.getBlock().getType().equals(Material.SIGN_POST) || event.getBlock().getType().equals(Material.WALL_SIGN)))
             return;
 
         String[] lines = event.getLines();
         if (lines[1] != null && lines[1].equalsIgnoreCase("[warp]") && lines[2] != null) {
             Player player = event.getPlayer();
             Warp warp = Core.warpManager.getWarp(lines[2]);
             if (warp != null) {
                PlayerUtils.sendSuccess(player, Core.NAME, "Ein Rechtsklick auf das Schild teleportiert dich zum Warp '" + warp.getName() + "'.");
                 event.setLine(2, warp.getName());
             } else {
                 PlayerUtils.sendError(player, Core.NAME, "Der Warp '" + lines[2] + "' existiert nicht!");
                 event.setCancelled(true);
                 event.getBlock().breakNaturally();
             }
         }
     }
 
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.isCancelled() || !event.hasBlock() || !event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
             return;
 
         Block block = event.getClickedBlock();
         if (block.getType().equals(Material.SIGN_POST) || block.getType().equals(Material.WALL_SIGN)) {
             Sign sign = (Sign) block.getState();
             String[] lines = sign.getLines();
             if (lines[1] != null && lines[1].equalsIgnoreCase("[warp]") && lines[2] != null && lines[2].length() >= 1) {
                 Warp warp = Core.warpManager.getWarp(lines[2]);
                 // WARP DOES NOT EXIST ANYMORE
                 if (warp == null) {
                     sign.getBlock().breakNaturally();
                     PlayerUtils.sendError(event.getPlayer(), Core.NAME, "Der Warp '" + lines[2] + "' existiert nicht mehr! Das Schild wurde abgerissen.");
                 } else
                     handleWarp(warp, event.getPlayer(), sign);
             }
         }
     }
 
     private void handleWarp(Warp warp, Player player, Sign sign) {
         if (!canUse(warp, player)) {
             PlayerUtils.sendError(player, Core.NAME, "Du kannst den Warp '" + warp.getName() + "' nicht benutzen!");
             return;
         }
         player.teleport(warp.getLocation());
         PlayerUtils.sendSuccess(player, Core.NAME, "Willkommen beim Warp '" + warp.getName() + "'.");
 
         // FIRE STATISTIC
         IlluminatiCore.handleStatistic(new WarpSignStat(player.getName(), warp.getName(), sign.getLocation()));
     }
 
     private boolean canUse(Warp warp, Player player) {
         if (warp.isOwner(player) || UtilPermissions.playerCanUseCommand(player, IGNORE_USE_MODE))
             return true;
         return warp.canUse(player) && warp.canUsedBy(Warp.SIGN_USEMODE);
     }
 }
