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
 
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 
 import com.minestar.MineStarWarp.Main;
 import com.minestar.MineStarWarp.Warp;
 import com.minestar.MineStarWarp.commands.Command;
 
 public class CreateCommand extends Command {
 
     public CreateCommand(String syntax, String arguments, String node,
             Server server) {
         super(syntax, arguments, node, server);
         this.description = Main.localization.get("createCommand.description");
     }
 
     @Override
     /**
      * Representing the command <br>
      * /warp create <br>
      * This creates a new warp at the location the player is at the moment.
      * Every warp is created private and must convert to public manuelly.
      * The player can only create a new warp when he haven't hit the maximum warp number (public warps does not count)
      * 
      * @param player
      *            Called the command
      * @param split
      *            args[0] is the warp name
      */
     public void execute(String[] args, Player player) {
 
         String warpName = args[0];
         if (isKeyWord(warpName.toLowerCase())) {
             player.sendMessage(ChatColor.RED
                     + Main.localization.get("createCommand.keyWord", warpName));
             return;
         }
         if (Main.warpManager.hasFreeWarps(player)) {
             if (!Main.warpManager.isWarpExisting(warpName))
                 Main.warpManager.addWarp(player, warpName, new Warp(player));
             else
                 player.sendMessage(ChatColor.RED
                         + Main.localization.get("createCommand.sameName",
                                 warpName));
         }
         else
             player.sendMessage(ChatColor.RED
                     + Main.localization.get("createCommand.limitWarps"));
     }
 
     public static boolean isKeyWord(String warpName) {
         return warpName.equals("create") || warpName.equals("delete")
                 || warpName.equals("invite") || warpName.equals("uninvite")
                 || warpName.equals("list") || warpName.equals("private")
                 || warpName.equals("public") || warpName.equals("search")
                 || warpName.equals("uninvite") || warpName.equals("move")
                || warpName.equals("rename");
     }
 }
