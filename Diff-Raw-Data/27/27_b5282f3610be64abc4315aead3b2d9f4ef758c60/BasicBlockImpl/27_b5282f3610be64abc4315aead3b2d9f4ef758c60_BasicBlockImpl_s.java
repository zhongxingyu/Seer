 /*
  * Copyright (C) 2010 Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
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
 
 package fr.jamgotchian.abcd.core.ir;
 
 import java.util.Set;
 import java.util.EnumSet;
 import fr.jamgotchian.abcd.core.util.Range;
 import fr.jamgotchian.abcd.core.util.RangeImpl;
 
 /**
  *
  * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  */
 public class BasicBlockImpl implements BasicBlock {
 
     private final Range range;
 
     private BasicBlockType type;
 
     private int order;
 
     private int loopLevel;
 
     private IRInstSeq instructions;
 
     private VariableStack inputStack;
 
     private VariableStack outputStack;
 
     private Region parent;
 
     private Set<BasicBlockAttribute> attributes;
 
     private Object data;
 
     public BasicBlockImpl(Range range, BasicBlockType type) {
         this.range = range;
         this.type = type;
         order = -1;
         loopLevel = 0;
         attributes = EnumSet.noneOf(BasicBlockAttribute.class);
     }
 
     public BasicBlockImpl(int firstInstn, int lastInstn, BasicBlockType type) {
         this(new RangeImpl(firstInstn, lastInstn), type);
     }
 
     public BasicBlockImpl(BasicBlockType type) {
         this(null, type);
     }
 
     public BasicBlockImpl() {
         this(null, null);
     }
 
     public Range getRange() {
         return range;
     }
 
     public BasicBlockType getType() {
         return type;
     }
 
     public void setType(BasicBlockType type) {
         this.type = type;
     }
 
     public void setOrder(int order) {
         this.order = order;
     }
 
     public int getOrder() {
         return order;
     }
 
     public int getLoopLevel() {
         return loopLevel;
     }
 
     public void setLoopLevel(int loopLevel) {
         this.loopLevel = loopLevel;
     }
 
     public IRInstSeq getInstructions() {
         return instructions;
     }
 
     public void setInstructions(IRInstSeq instructions) {
         this.instructions = instructions;
     }
 
     public VariableStack getInputStack() {
         return inputStack;
     }
 
     public void setInputStack(VariableStack inputStack) {
         this.inputStack = inputStack;
     }
 
     public VariableStack getOutputStack() {
         return outputStack;
     }
 
     public void setOutputStack(VariableStack outputStack) {
         this.outputStack = outputStack;
     }
 
     public void resetState() {
         loopLevel = 0;
     }
 
     public Region getParent() {
         return parent;
     }
 
     public void setParent(Region parent) {
         this.parent = parent;
     }
 
     public void addAttribute(BasicBlockAttribute attr) {
         attributes.add(attr);
     }
 
     public boolean hasAttribute(BasicBlockAttribute attr) {
         return attributes.contains(attr);
     }
 
     public Set<BasicBlockAttribute> getAttributes() {
         return attributes;
     }
 
    public void setAttributes(Set<BasicBlockAttribute> attributes) {
        this.attributes = attributes;
     }
 
     public Object getData() {
         return data;
     }
 
     public void setData(Object data) {
         this.data = data;
     }
 
     @Override
     public String toString() {
         if (type != null) {
             switch (type) {
                 case ENTRY:
                 case EXIT:
                     return type.name();
             }
         }
         return range == null ? "[]" : range.toString();
     }
 }
