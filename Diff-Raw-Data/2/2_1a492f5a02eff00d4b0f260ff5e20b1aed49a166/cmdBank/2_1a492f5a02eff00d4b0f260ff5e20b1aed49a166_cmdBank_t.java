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
 
 package de.minestar.FifthElement.commands.bank;
 
 import org.bukkit.entity.Player;
 
 import de.minestar.FifthElement.core.Core;
 import de.minestar.FifthElement.data.Bank;
 import de.minestar.FifthElement.statistics.bank.BankStat;
 import de.minestar.illuminati.IlluminatiCore;
 import de.minestar.minestarlibrary.commands.AbstractCommand;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class cmdBank extends AbstractCommand {
 
     private static final String OTHER_BANK_PERMISSION = "fifthelement.command.otherbank";
 
     public cmdBank(String syntax, String arguments, String node) {
         super(Core.NAME, syntax, arguments, node);
     }
 
     @Override
     public void execute(String[] args, Player player) {
         Bank bank = null;
         // OWN HOME
         if (args.length == 0) {
             bank = Core.bankManager.getBank(player.getName());
             if (bank == null) {
                 PlayerUtils.sendError(player, pluginName, "Du hast keine Bank!");
                 return;
             }
             player.teleport(bank.getLocation());
             PlayerUtils.sendSuccess(player, pluginName, "Willkommen in deiner Bank.");
         }
         // HOME OF OTHER PLAYER
         else if (args.length == 1) {
            // CAN PLAYER USE OTHER BANKS
             if (checkSpecialPermission(player, OTHER_BANK_PERMISSION)) {
                 // FIND THE CORRECT PLAYER NAME
                 String targetName = PlayerUtils.getCorrectPlayerName(args[0]);
                 if (targetName == null) {
                     PlayerUtils.sendError(player, targetName, "Kann den Spieler '" + args[0] + "' nicht finden!");
                     return;
                 }
                 bank = Core.bankManager.getBank(targetName);
                 if (bank == null) {
                     PlayerUtils.sendError(player, pluginName, "Der Spieler '" + targetName + "' hat keine Bank!");
                     return;
                 }
                 PlayerUtils.sendSuccess(player, pluginName, "Bank von '" + bank.getOwner() + "'.");
             }
         }
         // WRONG COMMAND SYNTAX
         else {
             PlayerUtils.sendError(player, pluginName, getHelpMessage());
             return;
         }
 
         // FIRE STATISTIC
         IlluminatiCore.handleStatistic(new BankStat(player.getName(), bank.getOwner()));
     }
 }
