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
 import com.minestar.MineStarWarp.utils.PlayerUtil;
 
 public class UninviteCommand extends Command {
 
     public UninviteCommand(String syntax, String arguments, String node,
             Server server) {
         super(syntax, arguments, node, server);
         this.description = Main.localization.get(UNINVITE_DESCRIPTION);
     }
 
     @Override
     /**
      * Representing the command <br>
      * /warp uninvite <br>
      * This disallows the player to use the private warp anymore
      * 
      * @param player
      *            Called the command
      * @param split
      *            args[0] is the player name
      *            args[1] is the warps name
      */
     public void execute(String[] args, Player player) {
 
         String guestName = args[0];
         String warpName = args[1];
         Warp warp = Main.warpManager.getWarp(warpName);
         if (warp != null) {
             if (warp.canEdit(player)) {
                 Player guest = PlayerUtil.getPlayer(server, guestName);
                 if (guest != null)
                     guestName = guest.getName();
                 else
                     player.sendMessage(Main.localization.get(
                            UNINVITE_NOT_EXACT_PLAYER, args));
                 if (Main.warpManager.removeGuest(player, warpName, guestName)
                         && guest != null)
                     guest.sendMessage(ChatColor.RED
                             + Main.localization.get(UNINVITE_GUEST_MESSAGE,
                                     warpName));
             }
             else
                 player.sendMessage(ChatColor.RED
                         + Main.localization.get(UNINVITE_NOT_OWNER, warpName));
         }
         else
             player.sendMessage(ChatColor.RED
                     + Main.localization.get(INVITE_NOT_EXIST, warpName));
     }
 }
