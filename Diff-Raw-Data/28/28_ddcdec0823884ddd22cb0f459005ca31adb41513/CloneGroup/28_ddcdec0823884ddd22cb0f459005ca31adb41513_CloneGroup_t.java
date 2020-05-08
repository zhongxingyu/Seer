 /*
  * Sonar, open source software quality management tool.
  * Written (W) 2011 Andrew Tereskin
  * Copyright (C) 2008-2011 SonarSource
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
 package org.sonar.duplications.index;
 
import com.google.common.collect.Lists;

 import java.util.Collections;
 import java.util.List;
 
 public class CloneGroup extends ClonePartContainerBase<CloneGroup> {
 
   private boolean sorted = true;
 
   public CloneGroup() {
    this.parts = Lists.newArrayList();
   }
 
   public CloneGroup(int cloneUnitLength) {
    this.parts = Lists.newArrayList();
     this.cloneLength = cloneUnitLength;
   }
 
   public CloneGroup addPart(ClonePart part) {
     parts.add(part);
     sorted = false;
     return this;
   }
 
   void sortParts() {
     Collections.sort(parts, null);
   }
 
   boolean isSorted() {
     return sorted;
   }
 
   public List<ClonePart> getCloneParts() {
     if (!sorted) {
       sortParts();
       sorted = true;
     }
     return Collections.unmodifiableList(parts);
   }
 
 }
