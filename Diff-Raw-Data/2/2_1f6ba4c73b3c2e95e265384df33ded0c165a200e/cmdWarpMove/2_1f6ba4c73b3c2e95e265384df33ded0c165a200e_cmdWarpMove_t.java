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
 
 package de.minestar.FifthElement.commands.warp;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 import de.minestar.FifthElement.core.Core;
 import de.minestar.FifthElement.data.Warp;
 import de.minestar.FifthElement.statistics.warp.WarpMoveStat;
 import de.minestar.minestarlibrary.stats.StatisticHandler;
 import de.minestar.minestarlibrary.commands.AbstractCommand;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class cmdWarpMove extends AbstractCommand {
 
     private final static String PUBLIC_MOVE_PERMISSION = "fifthelement.command.movepublic";
 
     public cmdWarpMove(String syntax, String arguments, String node) {
         super(Core.NAME, syntax, arguments, node);
     }
 
     @Override
     public void execute(String[] args, Player player) {
 
         // CHECK IF WARP CAN PLACED IN THIS WORLD
         if (!Core.warpManager.isWarpAllowedIn(player.getWorld())) {
             PlayerUtils.sendError(player, pluginName, "Du kannst in diese Welt keine Warps verschieben!");
             return;
         }
 
         Warp warp = Core.warpManager.getWarp(args[0]);
         if (warp == null) {
             PlayerUtils.sendError(player, pluginName, "Warp '" + args[0] + "' existiert nicht!");
             return;
         }
         if (!warp.canEdit(player)) {
             PlayerUtils.sendError(player, pluginName, "Du kannst den Warp '" + warp.getName() + "' nicht bewegen!");
             return;
         }
         // ONLY ADMINS CAN MOVE PUBLIC WARPS
         if (warp.isPublic() && !checkSpecialPermission(player, PUBLIC_MOVE_PERMISSION)) {
             PlayerUtils.sendError(player, pluginName, "Du kannst keine ffentlichen Warps bewegen!");
             return;
         }
         Location oldLocation = warp.getLocation();
         Core.warpManager.moveWarp(warp, player);
 
         PlayerUtils.sendSuccess(player, pluginName, "Der Warp '" + warp.getName() + "' wurde verschoben.");
 
         if (warp.isPublic())
            Bukkit.broadcastMessage(ChatColor.AQUA + "[" + Core.NAME + "]: " + ChatColor.WHITE + "Der ffentliche Warp '" + warp.getName() + "' wurde verschoben.");
 
         // FIRE STATISTIC
         StatisticHandler.handleStatistic(new WarpMoveStat(player.getName(), warp.getName(), oldLocation, warp.getLocation()));
     }
 
 }
