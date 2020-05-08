 package de.softwareforge.eyewiki.variable;
 
 /*
  * ========================================================================
  *
  * eyeWiki - a WikiWiki clone written in Java
  *
  * ========================================================================
  *
  * Copyright (C) 2005 Henning Schmiedehausen <henning@software-forge.com>
  *
  * based on
  *
  * JSPWiki - a JSP-based WikiWiki clone.
  * Copyright (C) 2002-2005 Janne Jalkanen (Janne.Jalkanen@iki.fi)
  *
  * ========================================================================
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation; either version 2.1 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * ========================================================================
  */
 
 import org.picocontainer.Startable;
 
 
 import de.softwareforge.eyewiki.WikiContext;
 import de.softwareforge.eyewiki.manager.VariableManager;
 
 public class ConstantVariables
         implements Startable
 {
     private final VariableManager variableManager;
 
     public ConstantVariables(final VariableManager variableManager)
     {
         this.variableManager = variableManager;
     }
 
     public synchronized void start()
     {
     }
 
     public synchronized void stop()
     {
         // GNDN
     }
 
     private static class ConstantVariable
             extends AbstractSimpleVariable
             implements WikiVariable
     {
         private final String value;
 
         private ConstantVariable(final String value)
         {
             this.value = value;
         }
 
         public String getValue(WikiContext context, String variableName)
         {
             return value;
         }
     }
 }
 
