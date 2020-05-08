 /*
  * This file is part of QuarterBukkit-Plugin.
  * Copyright (c) 2012 QuarterCode <http://www.quartercode.com/>
  *
  * QuarterBukkit-Plugin is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * QuarterBukkit-Plugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with QuarterBukkit-Plugin. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.quartercode.quarterbukkit.api;
 
 /**
  * A math helper class.
 * You can convert values with this Utility-Class.
  */
 public class MathUtil {
 
     /**
     * Convert milliseconds to minecraft-ticks.
      * 
      * @param millis The milliseconds.
     * @return The minecraft-ticks.
      */
     public static long getTicks(long millis) {
 
         return (long) (millis / 50D);
     }
 
     /**
     * Convert minecraft-ticks to milliseconds.
      * 
     * @param ticks The minecraft-ticks
     * @return The milliseconds
      */
     public static long getMillis(long ticks) {
 
         return (long) (ticks * 50D);
     }
 
     private MathUtil() {
 
     }
 
 }
