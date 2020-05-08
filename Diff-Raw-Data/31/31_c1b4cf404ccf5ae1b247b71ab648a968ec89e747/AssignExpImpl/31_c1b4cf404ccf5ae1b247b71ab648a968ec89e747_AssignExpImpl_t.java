 /**
  * Copyright (C) 2010  Marcellus C. Tavares
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.orcas.iocl.exp.impl;
 
 import org.orcas.iocl.exp.AssignExp;
 import org.orcas.iocl.exp.OclExpression;
 import org.orcas.iocl.exp.SimpleName;
 
 public class AssignExpImpl extends ImperativeExpImpl implements AssignExp {
 
     public SimpleName getLeft() {
         return _left;
     }
     public OclExpression getValue() {
         return _value;
     }
     public boolean isReset() {
         return _reset;
     }
     public void setLeft(SimpleName left) {
         _left = left;
     }
 
     public void setReset(boolean reset) {
         _reset = reset;
     }
 
     public void setValue(OclExpression value) {
         _value = value;
     }
 
    private boolean _reset;
    private SimpleName _left;
    private OclExpression _value;
 
 }
