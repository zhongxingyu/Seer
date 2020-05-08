 /*
  * Sonar Switch Off Violations Plugin
  * Copyright (C) 2011 SonarSource
  * dev@sonar.codehaus.org
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.plugins.switchoffviolations.pattern;
 
 import com.google.common.collect.Sets;
 
 import java.util.Set;
 
 public class LineRange {
  private int from;
  private int to;
 
   public LineRange(int from, int to) {
     if (to < from) {
       throw new IllegalArgumentException("Line range is not valid: " + from + " must be greater than " + to);
     }
     this.from = from;
     this.to = to;
   }
 
   public boolean in(int lineId) {
     return from <= lineId && lineId <= to;
   }
 
   public Set<Integer> toLines() {
     Set<Integer> lines = Sets.newLinkedHashSet();
     for (int index = from; index <= to; index++) {
       lines.add(index);
     }
     return lines;
   }
 
   @Override
   public String toString() {
     return "[" + from + "-" + to + "]";
   }
 
   @Override
   public int hashCode() {
     final int prime = 31;
     int result = 1;
     result = prime * result + from;
     result = prime * result + to;
     return result;
   }
 
   @Override
   public boolean equals(Object obj) {
     if (this == obj) {
       return true;
     }
     if (obj == null) {
       return false;
     }
     if (getClass() != obj.getClass()) {
       return false;
     }
     if (fieldsDiffer((LineRange) obj)) {
       return false;
     }
     return true;
   }
 
   private boolean fieldsDiffer(LineRange other) {
     if (from != other.from) {
       return true;
     }
     if (to != other.to) {
       return true;
     }
     return false;
   }
 }
