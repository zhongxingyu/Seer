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
 
 import java.util.List;
 
 import org.sonar.api.Plugin;
 
import com.google.common.collect.Lists;
 
 public class CCheckPlugin implements Plugin {
 
   public String getKey() {
     return CChecksConstants.PLUGIN_KEY;
   }
 
   public String getName() {
     return CChecksConstants.PLUGIN_NAME;
   }
 
   public String getDescription() {
     return "C checks for C projects";
   }
 
  @SuppressWarnings({ "rawtypes", "unchecked" })
   public List getExtensions() {
    List extensions = Lists.newArrayList();
    extensions.add(BooleanExpressionComplexityCheck.class);
    extensions.add(CollapsibleIfStatementsCheck.class);
    extensions.add(EmptyBlockCheck.class);
    extensions.add(ExcessiveParameterListCheck.class);
    extensions.add(FileLocCheck.class);
    extensions.add(FileNameCheck.class);
    extensions.add(ForLoopWithoutBracesCheck.class);
    extensions.add(FunctionComplexityCheck.class);
    extensions.add(FunctionLocCheck.class);
    extensions.add(FunctionNameCheck.class);
    extensions.add(IfStatementWithoutBracesCheck.class);
    extensions.add(NestedIfDepthCheck.class);
    extensions.add(ParsingErrorCheck.class);
    extensions.add(SwitchStatementWithoutDefaultCheck.class);
    extensions.add(WhileLoopWithoutBracesCheck.class);
     return extensions;
   }
 }
