 /*
  * Copyright (C) 2011 MineStar.de 
  * 
  * This file is part of MineStarWarp.
  * 
  * MineStarWarp is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * MineStarWarp is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with MineStarWarp.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.minestar.MineStarWarp.commands.warp;
 
 import java.util.Map.Entry;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 
 import com.minestar.MineStarWarp.Main;
 import com.minestar.MineStarWarp.Warp;
 import com.minestar.MineStarWarp.commands.Command;
 import com.minestar.MineStarWarp.commands.SuperCommand;
 
 public class WarpToCommand extends SuperCommand {
 
     public WarpToCommand(String syntax, String arguments, String node,
             Server server, Command... subCommands) {
         super(syntax, arguments, node, server, subCommands);
         this.description = Main.localization.get("warpToCommand.description");
     }
 
     @Override
     /**
      * Representing the command <br>
      * /warp <br>
      * This teleports the player to the warp with the same name or the first found with a similiar name
      * 
      * @param player
      *            Called the command
      * @param split
      *            args[0] is the warp name
      */
     public void execute(String[] args, Player player) {
 
         String warpName = args[0];
         Entry<String, Warp> entry = Main.warpManager.getSimiliarWarp(warpName);
         if (entry != null) {
             Warp warp = entry.getValue();
             if (warp.canUse(player)) {
                 player.teleport(warp.getLoc());
                 player.sendMessage(ChatColor.AQUA
                         + Main.localization.get("warpToCommand.welcome",
                                 entry.getKey()));
             }
             else
                 player.sendMessage(ChatColor.RED
                        + Main.localization.get("warpToCommand.noPermission",
                                 entry.getKey()));
         }
         else
             player.sendMessage(ChatColor.RED
                     + Main.localization.get("warpToCommand.notExisting",
                             warpName));
     }
 }
