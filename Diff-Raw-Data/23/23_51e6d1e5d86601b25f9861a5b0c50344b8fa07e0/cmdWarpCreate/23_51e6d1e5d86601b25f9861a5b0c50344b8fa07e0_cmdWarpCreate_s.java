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
 
 import org.bukkit.entity.Player;
 
 import de.minestar.FifthElement.core.Core;
 import de.minestar.FifthElement.data.WarpCounter;
 import de.minestar.minestarlibrary.commands.AbstractCommand;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class cmdWarpCreate extends AbstractCommand {
 
     public cmdWarpCreate(String syntax, String arguments, String node) {
         super(Core.NAME, syntax, arguments, node);
     }
 
     @Override
     public void execute(String[] args, Player player) {
         // PLAYER CAN CREATE A NEW WARP
         if (Core.warpManager.canCreatePrivate(player.getName())) {
             String warpName = args[0];
             // NO DUPLICATE WARPS
             if (Core.warpManager.isWarpExisting(warpName)) {
                 PlayerUtils.sendError(player, pluginName, "Es existiert bereits ein Warp names '" + warpName + "' !");
                 return;
             }
 
             // CREATE THE NEW WARP
             Core.warpManager.createWarp(warpName, player);
             PlayerUtils.sendSuccess(player, pluginName, "Der Warp '" + warpName + "' wurde erstellt!");
             WarpCounter counter = Core.warpManager.getWarpCounter(player.getName());
            PlayerUtils.sendInfo(player, "Du kannst noch " + counter.getPrivateWarps() + " private Warps erstellen.");
         }
         // PLAYER HAS TOO MANY WARPS
         else {
             PlayerUtils.sendError(player, pluginName, "Du kannst keinen weiteren privaten Warp erstellen!");
             return;
         }
     }
 }
