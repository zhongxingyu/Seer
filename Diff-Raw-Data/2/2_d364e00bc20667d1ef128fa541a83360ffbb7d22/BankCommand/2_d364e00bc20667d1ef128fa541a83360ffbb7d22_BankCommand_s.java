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
 
 package com.minestar.MineStarWarp.commands.bank;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 
 import com.gemo.utils.UtilPermissions;
 import com.minestar.MineStarWarp.Main;
 import com.minestar.MineStarWarp.commands.Command;
 import com.minestar.MineStarWarp.commands.SuperCommand;
 
 public class BankCommand extends SuperCommand {
 
     public BankCommand(String syntax, String arguments, String node,
             Server server, Command... commands) {
         super(syntax, arguments, node, server, commands);
     }
 
     @Override
     public void run(String[] args, Player player) {
         if (!super.runSubCommand(args, player)) {
             if (!hasRights(player)) {
                 player.sendMessage(NO_RIGHT);
                 return;
             }
 
             execute(args, player);
         }
     }
 
     @Override
     /**
      * Representing the command <br>
      * /bank (<PlayerName>) <br>
      * This teleports a player to his bank or a player to the bank of the player
      * 
      * @param player
      *            Called the command
      * @param args
      *            (args[0] is the players name(not command caller!) who can use the bank)
      */
     public void execute(String[] args, Player player) {
 
         Location bank = null;
 
         if (args.length == 0) {
             bank = Main.bankManager.getBank(player.getName());
             if (bank != null) {
                 player.teleport(bank);
                 player.sendMessage(Main.localization.get("bankCommand.welcome"));
             }
             else {
                 player.sendMessage(ChatColor.RED
                        + Main.localization.get("bankCommand.netFound"));
             }
         }
         else {
             if (!UtilPermissions.playerCanUseCommand(player,
                     "minestarwarp.commands.bankSpecific")) {
                 player.sendMessage(NO_RIGHT);
                 return;
             }
 
             bank = Main.bankManager.getBank(args[0]);
             if (bank != null) {
                 player.teleport(bank);
                 player.sendMessage(Main.localization.get(
                         "bankCommand.teleportToBank", args[0]));
             }
             else {
                 player.sendMessage(ChatColor.RED
                         + Main.localization.get("bankCommand.teleportNotFound",
                                 args[0]));
             }
         }
     }
 }
