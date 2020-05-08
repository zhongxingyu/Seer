 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of CastAway.
  * 
  * CastAway is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * CastAway is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with CastAway.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.castaway.command;
 
 import org.bukkit.entity.Player;
 
 import de.minestar.castaway.core.CastAwayCore;
 import de.minestar.castaway.data.PlayerData;
 import de.minestar.minestarlibrary.commands.AbstractCommand;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class cmdRespawn extends AbstractCommand {
 
     public cmdRespawn(String syntax, String arguments, String node) {
         super(CastAwayCore.NAME, syntax, arguments, node);
         this.description = "Respawn.";
     }
 
     @Override
     public void execute(String[] args, Player player) {
         PlayerData playerData = CastAwayCore.playerManager.getPlayerData(player);
         if (!playerData.isInDungeon()) {
             PlayerUtils.sendError(player, CastAwayCore.NAME, "Du musst in einem Dungeon sein!");
         } else {
             String dungeonName = playerData.getDungeon().getName();
            playerData.quitDungeon();
             PlayerUtils.sendSuccess(player, "Du hast den Dungeon '" + dungeonName + "' verlassen!");
             player.setHealth(0);
         }
     }
 }
