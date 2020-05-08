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
 
 package de.minestar.castaway.blocks;
 
import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 import de.minestar.castaway.core.CastAwayCore;
 import de.minestar.castaway.data.ActionBlockType;
 import de.minestar.castaway.data.BlockVector;
 import de.minestar.castaway.data.Dungeon;
 import de.minestar.castaway.data.PlayerData;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class SetRespawnPositionBlock extends AbstractActionBlock {
 
     public SetRespawnPositionBlock(BlockVector vector, Dungeon dungeon) {
         super(vector, dungeon, ActionBlockType.SET_RESPAWN_POS);
         this.setHandlePhysical();
         this.setHandleLeftClick();
         this.setHandleRightClick();
     }
 
     @Override
     public boolean execute(Player player, PlayerData data) {
         // Player must be in a dungeon
         if (data.isInDungeon() && data.getDungeon().equals(this.dungeon)) {
             PlayerUtils.sendInfo(player, "Respawnposition wurde gesetzt!");
            Location location = this.vector.getLocation().clone();
            location.setX(location.getX() + 0.5d);
            location.setY(location.getY() + 0.5d);
            location.setZ(location.getZ() + 0.5d);
            data.setRespawnLocation(location);
             return false;
         }
 
         PlayerUtils.sendError(player, CastAwayCore.NAME, "Du musst in einem Dungeon sein!");
         PlayerUtils.sendInfo(player, "Wende dich an einen Admin falls du es eigentlich bist.");
         data.setRespawnLocation(null);
         return false;
     }
 }
