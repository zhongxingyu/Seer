 /*
  * Copyright 2013, Bruce Mitchener, Jr.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.dylanfoundry.deft.filetypes.dylan;
 
 import com.intellij.navigation.ChooseByNameContributor;
 import com.intellij.navigation.NavigationItem;
 import com.intellij.openapi.project.Project;
 import com.intellij.psi.search.GlobalSearchScope;
 import com.intellij.util.ArrayUtil;
 import org.dylanfoundry.deft.filetypes.dylan.index.DylanSymbolIndex;
 import org.dylanfoundry.deft.filetypes.dylan.psi.DylanDefiner;
 import org.jetbrains.annotations.NotNull;
 
 import java.util.Collection;
 
public class DylanChooseByNameContributor implements ChooseByNameContributor {
   @NotNull
   @Override
   public String[] getNames(Project project, boolean includeNonProjectItems) {
     Collection<String> names = DylanSymbolIndex.getNames(project);
     return ArrayUtil.toStringArray(names);
   }
 
   @NotNull
   @Override
   public NavigationItem[] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {
     GlobalSearchScope scope = includeNonProjectItems ? GlobalSearchScope.allScope(project) : GlobalSearchScope.projectScope(project);
     Collection<DylanDefiner> result = DylanSymbolIndex.getItemsByName(project, name, scope);
     return result.toArray(new NavigationItem[result.size()]);
   }
 }
