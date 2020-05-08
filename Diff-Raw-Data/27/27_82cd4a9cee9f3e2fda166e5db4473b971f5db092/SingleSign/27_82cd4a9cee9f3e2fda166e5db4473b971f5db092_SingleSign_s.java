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
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 
 public class SingleSign {
     private final Dungeon dungeon;
     private final BlockVector vector;
     private final byte subData;
 
     public SingleSign(Dungeon dungeon, BlockVector vector, byte subData) {
         this.dungeon = dungeon;
         this.vector = vector;
         this.subData = subData;
     }
 
     public Dungeon getDungeon() {
         return dungeon;
     }
 
     public byte getSubData() {
         return subData;
     }
 
     public BlockVector getVector() {
         return vector;
     }
 
     public boolean recreateSign() {
         // get the location
         Location location = vector.getLocation();
         if (location == null) {
             return false;
         }
 
         // return if the sign already exists
         Block block = location.getBlock();
         if (block.getTypeId() == Material.WALL_SIGN.getId() && block.getData() == this.subData) {
             return true;
         }
 
         // create the sign
         block.setTypeIdAndData(Material.WALL_SIGN.getId(), this.subData, true);
         return true;
     }
 
     public boolean fillWithInformation(String[] lines) {
         // recreate the sign
         if (!this.recreateSign()) {
             return false;
         }
 
         // the lines must be valid
         if (lines == null || lines.length != 4) {
             return false;
         }
 
         // set the lines
         Sign sign = (Sign) (this.vector.getLocation().getBlock().getState());
         String text;
         for (int line = 0; line < lines.length; line++) {
             if (lines[line] == null) {
                 sign.setLine(0, "");
             }
             text = lines[line];
             if (text.length() > 15) {
                 text = text.substring(0, 15);
             }
             sign.setLine(line, text);
         }
         return true;
     }
 
     public boolean isFilledWithPlayerData() {
         // get the location
         Location location = vector.getLocation();
         if (location == null) {
             return false;
         }
 
         // return if the sign already exists
         Block block = location.getBlock();
         if (block.getTypeId() != Material.WALL_SIGN.getId() || block.getData() != this.subData) {
             return false;
         }
 
         Sign sign = (Sign) (this.vector.getLocation().getBlock().getState());
         return (sign.getLine(1).length() > 0 && sign.getLine(2).length() > 0 && sign.getLine(3).length() > 0);
     }
 
     @Override
     public boolean equals(Object object) {
         if (object == null) {
             return false;
         }
 
         if (!(object instanceof SingleSign)) {
             return false;
         }
 
         return ((SingleSign) object).getVector().equals(this.vector);
     }
 
     @Override
     public String toString() {
         return "SingleSign = { " + this.dungeon + " , " + this.vector + ", SubData=" + this.subData + " }";
     }
 }
