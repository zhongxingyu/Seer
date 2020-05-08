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
 
 package de.minestar.castaway.data;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.entity.Player;
 
 public class PlayerData {
     private final String playerName;
     private boolean inDungeon;
     private Dungeon dungeon;
 
     public PlayerData(String playerName) {
         this.playerName = playerName;
         this.inDungeon = false;
     }
 
     public void updateDungeon(Dungeon dungeon) {
         if (dungeon == null) {
             this.quitDungeon();
             return;
         }
 
         this.dungeon = dungeon;
         this.inDungeon = true;
         this.updateBukkitPlayer();
     }
 
     public void quitDungeon() {
         this.inDungeon = false;
         this.dungeon = null;
         this.updateBukkitPlayer();
     }
 
     private void updateBukkitPlayer() {
         Player player = Bukkit.getPlayerExact(this.playerName);
         if (player != null) {
             if (this.inDungeon) {
                 player.setGameMode(GameMode.ADVENTURE);
             } else {
                 player.setGameMode(GameMode.SURVIVAL);
             }
         }
     }
 
     public String getPlayerName() {
         return playerName;
     }
 
     public boolean isInDungeon() {
         return inDungeon;
     }
 
     public Dungeon getDungeon() {
         return this.dungeon;
     }
 }
