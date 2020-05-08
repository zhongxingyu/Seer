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
 
 package de.minestar.AdminStuff.data;
 
 public enum Direction {
 
     // TODO: Correct directions
     //@formatter:off
    N (338, 359, "Norden"),
    N_(0,   23,  "Norden"),
     NE(23,  68,  "Nord-Osten"),
     E (68,  113, "Osten"),
     SE(113, 158, "Sued-Osten"),
     S (158, 203, "Sueden"),
     SW(203, 248, "Sued-West"),
     W (248, 293, "Westen"),
     NW(293, 338, "Nord-West");
     //@formatter:on
 
     private final int lowBorder;
     private final int highBorder;
     private final String name;
 
     private Direction(int lowBorder, int highBorder, String name) {
         this.lowBorder = lowBorder;
         this.highBorder = highBorder;
         this.name = name;
     }
 
     private boolean isDirection(int angle) {
         return angle >= lowBorder && angle < highBorder;
     }
 
     public String getName() {
         return name;
     }
 
     public static Direction getDirection(int angle) {
         for (Direction dir : values())
             if (dir.isDirection(angle))
                 return dir;
         return null;
     }
 }
