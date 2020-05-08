 /*
  * Copyright (C) 2011 Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.jamgotchian.abcd.core.controlflow;
 
 import fr.jamgotchian.abcd.core.controlflow.util.TACInstComparator;
 import fr.jamgotchian.abcd.core.controlflow.util.VariableMapping;
import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 /**
  *
  * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  */
 public class Region {
 
     private BasicBlock entry;
 
     private BasicBlock exit;
 
     private Region parent;
 
     private final Set<Region> children = new LinkedHashSet<Region>();
 
     private ParentType parentType;
 
     private ChildType childType;
 
     public Region(BasicBlock entry, BasicBlock exit, ParentType parentType) {
         this.entry = entry;
         this.exit = exit;
         this.parentType = parentType;
         childType = ChildType.UNDEFINED;
     }
 
     public BasicBlock getEntry() {
         return entry;
     }
 
     public BasicBlock getExit() {
         return exit;
     }
 
     public void setEntry(BasicBlock entry) {
         this.entry = entry;
     }
 
     public void setExit(BasicBlock exit) {
         this.exit = exit;
     }
 
     public Region getParent() {
         return parent;
     }
 
     public void setParent(Region parent) {
         this.parent = parent;
        parent.addChild(this);
     }
 
     public Set<Region> getChildren() {
         return children;
     }
 
     public Region getFirstChild() {
         if (children.isEmpty()) {
             return null;
         } else {
             return children.iterator().next();
         }
     }
 
     public int getChildCount() {
         return children.size();
     }
 
     public void addChild(Region child) {
         children.add(child);
     }
 
     public void removeChildren() {
        children.clear();
    }

    public void removeChild(Region child) {
        children.remove(child);
     }
 
     public boolean hasChild() {
         return children.size() > 0;
     }
 
     public ParentType getParentType() {
         return parentType;
     }
 
     public void setParentType(ParentType parentType) {
         this.parentType = parentType;
     }
 
     public ChildType getChildType() {
         return childType;
     }
 
     public void setChildType(ChildType childType) {
         this.childType = childType;
     }
 
     public boolean isBasicBlock() {
         return parentType == ParentType.BASIC_BLOCK
                 || parentType == ParentType.BASIC_BLOCK_IF_THEN_BREAK
                 || parentType == ParentType.BASIC_BLOCK_IF_NOT_THEN_BREAK;
     }
 
     public Set<BasicBlock> getBasicBlocks() {
        Set<BasicBlock> bbs = new HashSet<BasicBlock>();
         addBasicBlocks(bbs);
         return bbs;
     }
 
     private void addBasicBlocks(Set<BasicBlock> bbs) {
         if (entry != null) {
             bbs.add(entry);
         }
         for (Region child : children) {
             child.addBasicBlocks(bbs);
         }
     }
 
     public boolean deepEquals(Region other) {
         return deepEquals(other, new VariableMapping());
     }
 
     private boolean deepEquals(Region other, VariableMapping mapping) {
         if (parentType == other.getParentType()
                 && childType == other.getChildType()) {
             if (isBasicBlock()) {
                 BasicBlock bb = entry;
                 BasicBlock otherBb = other.getEntry();
                 return TACInstComparator.equal(bb.getInstructions(),
                                                otherBb.getInstructions(),
                                                mapping);
             } else {
                 if (getChildCount() == other.getChildCount()) {
                     for (Region child : children) {
                         boolean found = false;
                         for (Region otherChild : other.getChildren()) {
                             if (child.deepEquals(otherChild, mapping)) {
                                 found = true;
                                 break;
                             }
                         }
                         if (!found) {
                             return false;
                         }
                     }
                     return true;
                 }
             }
         }
         return false;
     }
 
     @Override
     public String toString() {
         return "(" + entry + ", " + exit + ")";
     }
 }
