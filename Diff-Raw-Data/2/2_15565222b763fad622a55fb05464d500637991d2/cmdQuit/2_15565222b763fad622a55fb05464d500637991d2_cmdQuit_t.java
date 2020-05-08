 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of AdminStuff.
  * 
  * AdminStuff is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * AdminStuff is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with AdminStuff.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.AdminStuff.commands;
 
 import org.bukkit.entity.Player;
 
 import de.minestar.AdminStuff.Core;
 import de.minestar.AdminStuff.manager.PlayerManager;
 import de.minestar.minestarlibrary.commands.AbstractCommand;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class cmdQuit extends AbstractCommand {
 
     private PlayerManager pManager;
 
     public cmdQuit(String syntax, String arguments, String node, PlayerManager pManager) {
        super(Core.NAME, syntax, arguments, node);
         this.description = "Spieler wird fuer alle andere Spieler unsichtbar";
         this.pManager = pManager;
     }
 
     @Override
     public void execute(String[] args, Player player) {
         if (pManager.isHidden(player))
             PlayerUtils.sendError(player, pluginName, "Du bist bereits unsichtbar!");
         else {
             pManager.hidePlayer(player);
             PlayerUtils.sendSuccess(player, pluginName, "Du bist nun unsichtbar!");
         }
 
     }
 
 }
