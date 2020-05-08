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
 
 import org.orcas.iocl.exp.OclExpression;
 import org.orcas.iocl.exp.Type;
 import org.orcas.iocl.exp.VariableInitExp;
 
 public class VariableInitExpImpl extends ImperativeExpImpl
     implements VariableInitExp {
 
     public String getVarName() {
         return _varName;
     }
 
     public OclExpression getVarValue() {
         return _varValue;
     }
 
     public Type getType() {
         return _type;
     }
 
     public void setVarName(String varName) {
         _varName = varName;
     }
 
     public void setVarValue(OclExpression varValue) {
         _varValue = varValue;
     }
 
     public void setType(Type type) {
         _type = type;
     }
 
    private String _varName;
    private Type _type;
    private OclExpression _varValue;
 
 }
