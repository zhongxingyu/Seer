 /*
  * Sonar, open source software quality management tool.
  * Copyright (C) 2009 SonarSource SA
  * mailto:contact AT sonarsource DOT com
  *
  * Sonar is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.plugins.sigmm;
 
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.Map;
 
 public enum MMRank {
   MINUSMINUS(-2.0), MINUS(-1.0), ZERO(0.0), PLUS(1.0), PLUSPLUS(2.0);
 
   private double value;
   private static final Map<Double, MMRank> lookup
     = new HashMap<Double, MMRank>();
 
   static {
    for (MMRank s : EnumSet.allOf(MMRank.class))
      lookup.put(s.getValue(), s);
   }
 
 
   private MMRank(double value) {
     this.value = value;
   }
 
   public static MMRank[] ascSortedRanks() {
    MMRank[] mmr = {MINUSMINUS, MINUS, ZERO, PLUS, PLUSPLUS};
    return mmr;
   }
 
   public static MMRank[] descSortedRanks() {
    MMRank[] mmr = {PLUSPLUS, PLUS, ZERO, MINUS, MINUSMINUS};
    return mmr;
   }
 
   public double getValue() {
     return value;
   }
 
   public static MMRank getRank(double value) {
     return lookup.get(value);
   }
 
   public static MMRank averageRank(MMRank... list) {
     float average = 0;
     int nbElements=0;
     for (int i = 0; i < list.length; i++) {
       if (list[i] == null) {
         continue;
       }
       average += list[i].getValue();
       nbElements++;
     }
     if (nbElements == 0) {
       return null;
     }
     average /= nbElements;
     return getRank(Math.round(average));
   }
 }
