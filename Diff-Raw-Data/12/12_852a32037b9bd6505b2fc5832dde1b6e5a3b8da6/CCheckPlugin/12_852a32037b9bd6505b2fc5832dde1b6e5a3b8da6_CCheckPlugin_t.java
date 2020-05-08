 /*
  * Sonar C-Rules Plugin
  * Copyright (C) 2010 SonarSource
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
 
 package org.sonar.c.checks;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.sonar.api.Plugin;
 
 import com.sonarsource.c.plugin.CCheck;
 
 public class CCheckPlugin implements Plugin {
 
   public String getKey() {
     return CChecksConstants.PLUGIN_KEY;
   }
 
   public String getName() {
     return CChecksConstants.PLUGIN_NAME;
   }
 
   public String getDescription() {
    return "Provide a C rule engine. This C-Rules plugin requires the C plugin to be installed in order to work.";
   }
 
   public List getExtensions() {
     List<Class<? extends CCheck>> extensions = new ArrayList<Class<? extends CCheck>>();
     for (Class<? extends CCheck> cCheckClass : new CheckRepository().getCheckClasses()) {
       extensions.add(cCheckClass);
     }
     return extensions;
   }
 }
