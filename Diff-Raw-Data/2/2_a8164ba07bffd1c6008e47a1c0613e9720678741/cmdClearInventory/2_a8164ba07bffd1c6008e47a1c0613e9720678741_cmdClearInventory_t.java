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
 
 import org.bukkit.entity.Player;
 
 import de.minestar.AdminStuff.Core;
 import de.minestar.minestarlibrary.commands.AbstractExtendedCommand;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class cmdClearInventory extends AbstractExtendedCommand {
 
     public cmdClearInventory(String syntax, String arguments, String node) {
         super(Core.NAME, syntax, arguments, node);
     }
 
     @Override
     /**
      * Representing the command <br>
      * /clearinventory <br>
      * This clears your inventory
      * 
      * @param player
      *            Called the command
      * @param split
      */
     public void execute(String[] args, Player player) {
         // delete own inventory
         if (args.length == 0) {
             player.getInventory().clear();
             PlayerUtils.sendSuccess(player, pluginName, "Inventar geleert");
 
         }
         // delete other's inventory
         else if (args.length == 1 && checkSpecialPermission(player, "commands.admin.clearinventoryother")) {
 
             Player target = PlayerUtils.getOnlinePlayer(args[0]);
             if (target == null)
                 PlayerUtils.sendError(player, pluginName, "Spieler '" + args[0] + "' nicht gefunden oder offline!");
             else if (target.isDead() || !target.isOnline())
                 PlayerUtils.sendError(player, pluginName, "Spieler '" + target.getName() + "' ist offline oder tot!");
             else {
                 target.getInventory().clear();
                PlayerUtils.sendSuccess(player, pluginName, "Das Inventar von '" + target.getName() + "' wurde geleert!");
                 PlayerUtils.sendInfo(target, pluginName, "Dein Inventar wurde geleert!");
             }
         } else
             PlayerUtils.sendError(player, pluginName, getHelpMessage());
     }
 }
