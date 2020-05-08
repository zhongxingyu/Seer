 package org.jessies.calc;
 
 /*
  * This file is part of LittleHelper.
  * Copyright (C) 2009 Elliott Hughes <enh@jessies.org>.
  * 
  * LittleHelper is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import java.math.*;
 
 public class CalculatorVariableNode implements Node {
     private final String name;
     
     public CalculatorVariableNode(String name) {
         this.name = name;
     }
     
     public String name() {
         return name;
     }
     
     public BigDecimal value(Calculator environment) {
        final Node variable = environment.getVariable(name);
        if (variable == null) {
             throw new CalculatorError("use of undefined variable '" + name + "'");
         }
        return variable.value(environment);
     }
 }
