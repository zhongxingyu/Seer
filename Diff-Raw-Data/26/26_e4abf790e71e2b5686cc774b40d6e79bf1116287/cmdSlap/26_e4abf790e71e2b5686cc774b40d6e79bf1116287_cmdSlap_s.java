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
 
 package de.minestar.AdminStuff.commands;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 
 import de.minestar.AdminStuff.Core;
 import de.minestar.AdminStuff.manager.PlayerManager;
 import de.minestar.core.MinestarCore;
 import de.minestar.core.units.MinestarPlayer;
 import de.minestar.minestarlibrary.commands.AbstractCommand;
 import de.minestar.minestarlibrary.utils.ChatUtils;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class cmdSlap extends AbstractCommand {
 
     private PlayerManager pManager;
 
     public cmdSlap(String syntax, String arguments, String node, PlayerManager pManager) {
         super(Core.NAME, syntax, arguments, node);
         this.pManager = pManager;
     }
 
     @Override
     /**
      * Representing the command <br>
      * /slap <br>
      * SLAP A PLAYER
      * 
      * @param player
      *            Called the command
      * @param split
      * 			args[0] = Player to be slapped
      */
     public void execute(String[] args, Player player) {
         slap(args[0], player);
     }
 
     @Override
     public void execute(String[] args, ConsoleCommandSender console) {
         slap(args[0], console);
     }
 
     private void slap(String targetName, CommandSender sender) {
         Player target = PlayerUtils.getOnlinePlayer(targetName);
         if (target == null)
             ChatUtils.writeError(sender, pluginName, "Spieler '" + targetName + "' wurde nicht gefunden!");
         else if (target.isDead() || !target.isOnline())
             ChatUtils.writeError(sender, pluginName, "Spieler '" + target.getName() + "' ist tot oder nicht online!");
         else {
             MinestarPlayer mPlayer = MinestarCore.getPlayer(target);
             Boolean slapped = mPlayer.getBoolean("adminstuff.slapped");
             if (slapped == null || !slapped)
                 slapped = true;
             else
                 slapped = false;
             mPlayer.setBoolean("adminstuff.slapped", slapped);
            pManager.updatePrefix(target, mPlayer);
            if (slapped)
                 Bukkit.broadcastMessage(ChatColor.BLUE + sender.getName() + " schlaegt " + mPlayer.getNickName() + " mit einem kalten Fisch ins Gesicht!");
            else
                 Bukkit.broadcastMessage(ChatColor.BLUE + mPlayer.getNickName() + "s Fisch wurde von einer Sturmmoeve gefressen!");
         }
     }
 }
