 /*
 * Sonar C# Plugin :: Dependency
  * Copyright (C) 2010 Jose Chillan, Alexandre Victoor and SonarSource
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
 package org.sonar.plugins.csharp.dependency.results;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.sonar.api.BatchExtension;
 import org.sonar.api.design.Dependency;
 
 public class DependencyResultCache implements BatchExtension {
   private Map<String, DependencyItemResult> typeDependencyMap = new HashMap<String, DependencyItemResult>();
 
   private Map<String, Dependency> projectDependencyMap = new HashMap<String, Dependency>();
 
   public Map<String, DependencyItemResult> getTypeDependencyMap() {
     return typeDependencyMap;
   }
 
   public void setTypeDependencyMap(Map<String, DependencyItemResult> typeDependencyMap) {
     this.typeDependencyMap = typeDependencyMap;
   }
 
   public Map<String, Dependency> getProjectDependencyMap() {
     return projectDependencyMap;
   }
 
   public void setProjectDependencyMap(Map<String, Dependency> projectDependencyMap) {
     this.projectDependencyMap = projectDependencyMap;
   }
 }
