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
 
 import java.util.HashMap;
 import java.util.Map;
 
 import de.minestar.castaway.blocks.AbstractActionBlock;
 import de.minestar.castaway.blocks.DungeonEndBlock;
 import de.minestar.castaway.blocks.DungeonStartBlock;
 import de.minestar.castaway.blocks.FullHealthBlock;
 import de.minestar.castaway.blocks.SignBlock;
 import de.minestar.castaway.blocks.VelocityReverseXZBlock;
 import de.minestar.castaway.blocks.VelocityUpBlock;
 
 public enum ActionBlockType {
 
     HALL_OF_FAME_SIGN(-1, SignBlock.class, "WALL"),
 
     DUNGEON_START(0, DungeonStartBlock.class, "START"),
 
     DUNGEON_END(1, DungeonEndBlock.class, "END"),
 
     SPECIAL_HEALTH_FULL(2, FullHealthBlock.class, "FULL_HEALTH"),
 
     VELOCITY_UP(3, VelocityUpBlock.class, "JUMP_UP"),
 
    VELOCITY_REVERSE_XZ(4, VelocityReverseXZBlock.class, "REVERSE_XZ");
 
     private final int ID;
     private final Class<? extends AbstractActionBlock> clazz;
     private final String commandName;
 
     private static Map<Integer, ActionBlockType> mapByID;
     private static Map<String, ActionBlockType> mapByCommand;
 
     static {
         mapByID = new HashMap<Integer, ActionBlockType>();
         mapByCommand = new HashMap<String, ActionBlockType>();
         for (ActionBlockType b : ActionBlockType.values()) {
             mapByID.put(b.ID, b);
             mapByCommand.put(b.commandName.toLowerCase(), b);
         }
     }
 
     private ActionBlockType(int ID, Class<? extends AbstractActionBlock> clazz, String commandName) {
         this.ID = ID;
         this.clazz = clazz;
         this.commandName = commandName;
     }
 
     public int getID() {
         return this.ID;
     }
 
     public Class<? extends AbstractActionBlock> getClazz() {
         return this.clazz;
     }
 
     public String getCommandName() {
         return commandName;
     }
 
     public static ActionBlockType get(int ID) {
         return mapByID.get(ID);
     }
 
     public static ActionBlockType get(String commandName) {
         return mapByCommand.get(commandName);
     }
 }
