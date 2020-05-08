 /*
  * Copyright 2010-2012 JetBrains s.r.o.
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
 
 package org.napile.idea.plugin.caches;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.jetbrains.annotations.NonNls;
 import org.jetbrains.annotations.NotNull;
 import org.napile.asm.resolve.name.FqName;
 import org.napile.compiler.lang.descriptors.ClassDescriptor;
 import org.napile.compiler.lang.descriptors.DeclarationDescriptor;
 import org.napile.compiler.lang.descriptors.SimpleMethodDescriptor;
 import org.napile.compiler.lang.psi.NapileClass;
 import org.napile.compiler.lang.psi.NapileClassLike;
 import org.napile.compiler.lang.psi.NapileFile;
 import org.napile.compiler.lang.psi.NapileNamedDeclaration;
 import org.napile.compiler.lang.psi.NapileNamedMethodOrMacro;
 import org.napile.compiler.lang.resolve.BindingTraceKeys;
 import org.napile.compiler.lang.resolve.BindingTrace;
 import org.napile.idea.plugin.module.ModuleAnalyzerUtil;
 import org.napile.idea.plugin.stubindex.NapileFullClassNameIndex;
 import org.napile.idea.plugin.stubindex.NapileShortClassNameIndex;
 import org.napile.idea.plugin.stubindex.NapileShortMethodNameIndex;
 import com.intellij.openapi.components.ServiceManager;
 import com.intellij.openapi.module.ModuleUtil;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.util.Condition;
 import com.intellij.openapi.util.Pair;
 import com.intellij.psi.search.GlobalSearchScope;
 import com.intellij.util.ArrayUtil;
 
 /**
  * @author VISTALL
  */
 public class NapileClassResolver
 {
 	private final Project project;
 
 	@NotNull
 	public static NapileClassResolver getInstance(final Project project)
 	{
 		return ServiceManager.getService(project, NapileClassResolver.class);
 	}
 
 	public NapileClassResolver(Project project)
 	{
 		this.project = project;
 	}
 
 	@NotNull
 	public String[] getAllClassNames()
 	{
 		Collection<String> classNames = NapileShortClassNameIndex.getInstance().getAllKeys(project);
 		return ArrayUtil.toStringArray(classNames);
 	}
 
 	@NotNull
 	public NapileClass[] getClassesByName(@NotNull @NonNls String name, @NotNull GlobalSearchScope scope)
 	{
 		Collection<NapileClass> classOrObjects = NapileShortClassNameIndex.getInstance().get(name, project, scope);
 
 		return classOrObjects.isEmpty() ? NapileClass.EMPTY_ARRAY : classOrObjects.toArray(NapileClass.EMPTY_ARRAY);
 	}
 
 	@NotNull
 	public NapileClass[] getClassesByFqName(@NotNull @NonNls String name, @NotNull GlobalSearchScope scope)
 	{
 		Collection<NapileClass> classOrObjects = NapileFullClassNameIndex.getInstance().get(name, project, scope);
 
 		return classOrObjects.isEmpty() ? NapileClass.EMPTY_ARRAY : classOrObjects.toArray(NapileClass.EMPTY_ARRAY);
 	}
 
 	public List<Pair<DeclarationDescriptor, NapileNamedDeclaration>> getDescriptorsForImport(@NotNull Condition<String> acceptedShortNameCondition, @NotNull NapileFile napileFile)
 	{
 		BindingTrace context = ModuleAnalyzerUtil.lastAnalyze(napileFile).getBindingTrace();
 		List<Pair<DeclarationDescriptor, NapileNamedDeclaration>> map = new ArrayList<Pair<DeclarationDescriptor, NapileNamedDeclaration>>();
 		GlobalSearchScope scope = GlobalSearchScope.moduleWithLibrariesScope(ModuleUtil.findModuleForPsiElement(napileFile));
 
 		for(String fqName : NapileFullClassNameIndex.getInstance().getAllKeys(project))
 		{
 			FqName classFQName = new FqName(fqName);
			if(!classFQName.isRoot() && acceptedShortNameCondition.value(classFQName.shortName().getName()))
 			{
 				Collection<NapileClass> list = NapileFullClassNameIndex.getInstance().get(fqName, project, scope);
 				for(NapileClassLike classLike : list)
 				{
 					ClassDescriptor classDescriptor = context.get(BindingTraceKeys.CLASS, classLike);
 					if(classDescriptor != null && classDescriptor.isStatic())
 						map.add(new Pair<DeclarationDescriptor, NapileNamedDeclaration>(classDescriptor, classLike));
 				}
 			}
 		}
 
 		for(String name : NapileShortMethodNameIndex.getInstance().getAllKeys(project))
 		{
 			if(acceptedShortNameCondition.value(name))
 			{
 				Collection<NapileNamedMethodOrMacro> list = NapileShortMethodNameIndex.getInstance().get(name, project, scope);
 				for(NapileNamedMethodOrMacro method : list)
 				{
 					SimpleMethodDescriptor methodDescriptor = context.get(BindingTraceKeys.METHOD, method);
 					if(methodDescriptor != null && methodDescriptor.isStatic())
 						map.add(new Pair<DeclarationDescriptor, NapileNamedDeclaration>(methodDescriptor, method));
 				}
 			}
 		}
 
 		return map;
 	}
 }
