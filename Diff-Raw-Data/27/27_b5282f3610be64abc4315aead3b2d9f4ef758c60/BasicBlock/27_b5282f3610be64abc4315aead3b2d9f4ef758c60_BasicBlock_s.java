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
 
 import fr.jamgotchian.abcd.core.graph.Orderable;
 import fr.jamgotchian.abcd.core.util.Range;
 import java.util.Set;
 
 /**
  *
  * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  */
 public interface BasicBlock extends Orderable {
 
     Range getRange();
 
     BasicBlockType getType();
 
     void setType(BasicBlockType type);
 
     int getLoopLevel();
 
     void setLoopLevel(int loopLevel);
 
     IRInstSeq getInstructions();
 
     void setInstructions(IRInstSeq instructions);
 
     VariableStack getInputStack();
 
     void setInputStack(VariableStack inputStack);
 
     VariableStack getOutputStack();
 
     void setOutputStack(VariableStack outputStack);
 
     void resetState();
 
     Region getParent();
 
     void setParent(Region region);
 
     void addAttribute(BasicBlockAttribute attr);
 
     boolean hasAttribute(BasicBlockAttribute attr);
 
     Set<BasicBlockAttribute> getAttributes();
 
    void setAttributes(Set<BasicBlockAttribute> attributes) ;
 
     Object getData();
 
     void setData(Object data);
 }
