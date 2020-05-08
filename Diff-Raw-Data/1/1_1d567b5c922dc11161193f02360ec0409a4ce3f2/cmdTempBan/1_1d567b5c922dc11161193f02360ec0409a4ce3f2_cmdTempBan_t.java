 /*
  * Copyright (C) 2011 MineStar.de 
  * 
  * This file is part of 'AdminStuff'.
  * 
  * 'AdminStuff' is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * 'AdminStuff' is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with 'AdminStuff'.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * AUTHOR: GeMoschen
  * 
  */
 
 package com.bukkit.Souli.AdminStuff.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 
 import com.bukkit.Souli.AdminStuff.ASCore;
 import com.bukkit.Souli.AdminStuff.ASPlayer;
 
 public class cmdTempBan extends Command {
 
     public cmdTempBan(String syntax, String arguments, String node, Server server) {
         super(syntax, arguments, node, server);
     }
 
     @Override
     /**
      * Representing the command <br>
      * /tempban <Player> <Time><br>
      * Temporary ban a single player
      * 
      * @param player
      *            Called the command
      * @param split
      *            split[0] is the targets name
      */
     public void execute(String[] args, Player player) {
         Player target = ASCore.getPlayer(args[0]);
         ASPlayer thisTarget = null;
         if (target == null)
             thisTarget = ASCore.getOrCreateASPlayer(args[0].toLowerCase());
         else
             thisTarget = ASCore.getOrCreateASPlayer(target);
 
         int days = 0;
         int hours = 0;
         int mins = 0;
         try {
             args[1] = args[1].toLowerCase();
            args[1] = " " + args[1]; 
 
             // GET DAYS
             int dIndex = args[1].indexOf('d');
             if (dIndex > -1) {
                 int preIndex = getPreIndex(args[1], dIndex);
                 days = Integer.valueOf(args[1].substring(preIndex, dIndex));
             }
             // GET HOURS
             int hIndex = args[1].indexOf('h');
             if (hIndex > -1) {
                 int preIndex = getPreIndex(args[1], hIndex);
                 hours = Integer.valueOf(args[1].substring(preIndex, hIndex));
             }
             // GET MINUTES
             int mIndex = args[1].indexOf('m');
             if (mIndex > -1) {
                 int preIndex = getPreIndex(args[1], mIndex);
                 mins = Integer.valueOf(args[1].substring(preIndex, mIndex));
             }
         } catch (Exception e) {
             player.sendMessage(ChatColor.RED + "Wrong timesyntax!");
             player.sendMessage(ChatColor.GRAY + "Example: 2d10h5m!");
             player.sendMessage(ChatColor.GRAY + "Example: 2d");
             player.sendMessage(ChatColor.GRAY + "Example: 10h!");
             player.sendMessage(ChatColor.GRAY + "Example: 5m!");
             return;
         }
 
         if (mins < 1 && hours < 1 && days < 1) {
             player.sendMessage(ChatColor.RED + "Wrong timesyntax!");
             player.sendMessage(ChatColor.GRAY + "Example: 2d10h5m!");
             player.sendMessage(ChatColor.GRAY + "Example: 2d");
             player.sendMessage(ChatColor.GRAY + "Example: 10h!");
             player.sendMessage(ChatColor.GRAY + "Example: 5m!");
             return;
         }
 
         thisTarget.setTempBanned(true);
         thisTarget.setBanEndTime(System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000) + (hours * 60 * 60 * 1000) + (mins * 60 * 1000));
         thisTarget.saveConfig(false, false, false, false, true, false, false);
         String message = "You were temporary banned for " + args[1] + ".";
 
         if (target != null) {
             target.kickPlayer(message);
             player.sendMessage(ChatColor.GRAY + "Player '" + ASCore.getPlayerName(target) + "' temporary banned!");
         } else {
             player.sendMessage(ChatColor.GRAY + "Player '" + args[0] + "' temporary banned!");
         }
     }
 
     private static int getPreIndex(String string, int Index) {
         for (int j = Index; j > 0; j--) {
             if (string.charAt(j) != '0' && string.charAt(j) != '1' && string.charAt(j) != '2' && string.charAt(j) != '3' && string.charAt(j) != '4' && string.charAt(j) != '5' && string.charAt(j) != '6' && string.charAt(j) != '7' && string.charAt(j) != '8' && string.charAt(j) != '9')
                 return j - 1;
         }
         return 0;
     }
 }
