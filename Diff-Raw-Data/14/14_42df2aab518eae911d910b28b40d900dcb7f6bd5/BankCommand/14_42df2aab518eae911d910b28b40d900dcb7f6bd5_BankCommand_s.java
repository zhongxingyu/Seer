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
 
 package de.minestar.MineStarWarp.commands.bank;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
import com.bukkit.gemo.utils.UtilPermissions;

 import de.minestar.MineStarWarp.Core;
 import de.minestar.MineStarWarp.dataManager.BankManager;
 import de.minestar.minestarlibrary.commands.Command;
 import de.minestar.minestarlibrary.commands.SuperCommand;
 import de.minestar.minestarlibrary.utils.ChatUtils;
 
 public class BankCommand extends SuperCommand {
 
     private BankManager bankManager;
 
     public BankCommand(String syntax, String arguments, String node, BankManager bankManager, Command... commands) {
         super(Core.NAME, syntax, arguments, node, true, commands);
         this.bankManager = bankManager;
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
             bank = bankManager.getBank(player.getName());
             if (bank != null) {
                 player.teleport(bank);
                 ChatUtils.printSuccess(player, pluginName, "Willkommen in deiner Bank!");
             } else
                 ChatUtils.printError(player, pluginName, "Du hast keine Bank!");
 
         } else {
            if (!UtilPermissions.playerCanUseCommand(player, "minestarwarp.commands.bankSpecific")) {
                ChatUtils.printError(player, pluginName, NO_RIGHT);
                 return;
            }
             String targetName = args[0];
             bank = bankManager.getBank(targetName);
             if (bank != null) {
                 player.teleport(bank);
                 ChatUtils.printSuccess(player, pluginName, "Du bist in der Bank von '" + targetName + "'!");
             } else
                ChatUtils.printError(player, pluginName, "Keine Bank f�r '" + targetName + "' gefunden!");
         }
     }
 }
