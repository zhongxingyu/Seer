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
 
 import de.minestar.castaway.blocks.AbstractBlock;
 import de.minestar.castaway.blocks.DungeonEndBlock;
 import de.minestar.castaway.blocks.DungeonStartBlock;
 import de.minestar.castaway.blocks.FullHealthBlock;
 
 public enum BlockEnum {
     UNKNOWN(-1, null),
 
     DUNGEON_START(0, DungeonStartBlock.class),
 
     DUNGEON_END(1, DungeonEndBlock.class),
 
    SPECIAL_HEALTH_FULL(2, FullHealthBlock.class);
 
     private final int ID;
     private final Class<? extends AbstractBlock> clazz;
 
     private BlockEnum(int ID, Class<? extends AbstractBlock> clazz) {
         this.ID = ID;
         this.clazz = clazz;
     }
 
     public int getID() {
         return this.ID;
     }
 
     public Class<? extends AbstractBlock> getClazz() {
         return this.clazz;
     }
 
     public static Class<? extends AbstractBlock> byID(int ID) {
         for (BlockEnum type : BlockEnum.values()) {
             if (type.getID() == ID)
                 return type.getClazz();
         }
         return UNKNOWN.getClazz();
     }
 }
