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
 
 package de.minestar.FifthElement.commands.home;
 
 import org.bukkit.entity.Player;
 
 import de.minestar.FifthElement.core.Core;
 import de.minestar.FifthElement.data.Home;
 import de.minestar.minestarlibrary.commands.AbstractExtendedCommand;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class cmdHome extends AbstractExtendedCommand {
 
     private static final String OTHER_HOME_PERMISSION = "fifthelement.command.otherhome";
 
     public cmdHome(String syntax, String arguments, String node) {
         super(Core.NAME, syntax, arguments, node);
     }
 
     @Override
     public void execute(String[] args, Player player) {
 
         Home home = null;
         // OWN HOME
         if (args.length == 0) {
             home = Core.homeManager.getHome(player.getName());
             if (home == null) {
                 PlayerUtils.sendError(player, pluginName, "Du hast kein Zuhause erstellt!");
                 PlayerUtils.sendInfo(player, "Mit '/setHome' erstellst du dir ein Zuhause.");
                 return;
             }
             player.teleport(home.getLocation());
             PlayerUtils.sendSuccess(player, pluginName, "Willkommen zu Hause.");
         }
         // HOME OF OTHER PLAYER
         else if (args.length == 1) {
             // CAN PLAYER USE OTHER HOMES
             if (checkSpecialPermission(player, OTHER_HOME_PERMISSION)) {
                 // FIND THE CORRECT PLAYER NAME
                 String targetName = PlayerUtils.getCorrectPlayerName(args[0]);
                 if (targetName == null) {
                     PlayerUtils.sendError(player, targetName, "Kann den Spieler '" + args[0] + "' nicht finden!");
                     return;
                 }
                 home = Core.homeManager.getHome(targetName);
                 if (home == null) {
                    PlayerUtils.sendError(player, pluginName, "Der Spieler '" + targetName + "'hat kein Zuhause erstellt!");
                     return;
                 }
                 PlayerUtils.sendSuccess(player, pluginName, "Haus von '" + home.getOwner() + "'.");
             }
         }
         // WRONG COMMAND SYNTAX
         else {
             PlayerUtils.sendError(player, pluginName, getHelpMessage());
             return;
         }
     }
 }
