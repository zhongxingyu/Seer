 /*
  * Copyright 2010-2012 napile.org
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
 
 package org.napile.idea.plugin.highlighter.linemarker;
 
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.Icon;
 
 import org.jetbrains.annotations.NotNull;
 import org.napile.compiler.analyzer.AnalyzeExhaust;
 import org.napile.compiler.lang.descriptors.CallableDescriptor;
 import org.napile.compiler.lang.descriptors.ClassDescriptor;
 import org.napile.compiler.lang.descriptors.DeclarationDescriptor;
 import org.napile.compiler.lang.descriptors.SimpleMethodDescriptor;
 import org.napile.compiler.lang.psi.NapileClass;
 import org.napile.compiler.lang.psi.NapileClassLike;
 import org.napile.compiler.lang.psi.NapileElement;
 import org.napile.compiler.lang.psi.NapileMethod;
 import org.napile.compiler.lang.resolve.BindingContext;
 import org.napile.compiler.lang.resolve.BindingContextUtils;
 import org.napile.compiler.lang.resolve.BodiesResolveContext;
 import org.napile.compiler.lang.resolve.DescriptorUtils;
 import org.napile.idea.plugin.caches.JetShortNamesCache;
 import org.napile.idea.plugin.project.WholeProjectAnalyzerFacade;
 import com.intellij.codeHighlighting.Pass;
 import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
 import com.intellij.codeInsight.daemon.LineMarkerInfo;
 import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
 import com.intellij.icons.AllIcons;
 import com.intellij.ide.util.DefaultPsiElementCellRenderer;
 import com.intellij.openapi.editor.markup.GutterIconRenderer;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiNameIdentifierOwner;
 import com.intellij.util.Function;
 
 /**
  * @author VISTALL
  * @date 17:54/23.08.12
  */
 public enum LineMarkers
 {
 	METHOD_OVERRIDING
 			{
 				@NotNull
 				@Override
 				protected List<NapileElement> getTargets(PsiElement element)
 				{
 					if(!(element instanceof NapileElement))
 						return Collections.emptyList();
 
 					NapileElement napileElement = (NapileElement) element;
 					AnalyzeExhaust analyzeExhaust = WholeProjectAnalyzerFacade.analyzeProjectWithCacheOnAFile(napileElement.getContainingFile());
 
 					DeclarationDescriptor descriptor = analyzeExhaust.getBindingContext().get(BindingContext.DECLARATION_TO_DESCRIPTOR, napileElement);
 					if(!(descriptor instanceof CallableDescriptor))
 						return Collections.emptyList();
 
 					List<NapileElement> list = new ArrayList<NapileElement>(((CallableDescriptor) descriptor).getOverriddenDescriptors().size());
 					for(CallableDescriptor overrideDescriptor : ((CallableDescriptor) descriptor).getOverriddenDescriptors())
 					{
 						NapileElement declarationPsiElement = (NapileElement) BindingContextUtils.descriptorToDeclaration(analyzeExhaust.getBindingContext(), overrideDescriptor);
 						list.add(declarationPsiElement);
 					}
 
 					return list;
 				}
 
 				@NotNull
 				@Override
 				protected String getTitle()
 				{
 					return "Go to overriding methods";
 				}
 
 				@NotNull
 				@Override
 				protected Icon getIcon()
 				{
 					return AllIcons.Gutter.OverridingMethod;
 				}
 			},
 	METHOD_OVERRIDEN
 			{
 				@NotNull
 				@Override
 				protected List<NapileElement> getTargets(PsiElement element)
 				{
 					if(!(element instanceof NapileMethod))
 						return Collections.emptyList();
 
 					NapileMethod napileMethod = (NapileMethod) element;
 					AnalyzeExhaust analyzeExhaust = WholeProjectAnalyzerFacade.analyzeProjectWithCacheOnAFile(napileMethod.getContainingFile());
 
 					BodiesResolveContext context = analyzeExhaust.getBodiesResolveContext();
 
 					SimpleMethodDescriptor descriptor = analyzeExhaust.getBindingContext().get(BindingContext.METHOD, napileMethod);
 					if(descriptor == null)
 						return Collections.emptyList();
 
 					ClassDescriptor ownerDescriptor = (ClassDescriptor)descriptor.getContainingDeclaration();
 
 					List<NapileElement> list = new ArrayList<NapileElement>();
 				/*	for(Map.Entry<NapileClass, MutableClassDescriptor> entry : context.getClasses().entrySet())
 					{
 						MutableClassDescriptor clazzDescriptor = entry.getValue();
 
 						if(DescriptorUtils.isSubclass(clazzDescriptor, ownerDescriptor))
 							for(MethodDescriptor clazzMethodDescriptor : clazzDescriptor.getMethods())
 								for(MethodDescriptor overrideMethodDescriptor : clazzMethodDescriptor.getOverriddenDescriptors())
 								{
 									PsiElement psiElement = BindingContextUtils.descriptorToDeclaration(analyzeExhaust.getBindingContext(), overrideMethodDescriptor);
 									if(element == psiElement)
 									{
 										List<PsiElement> elements = BindingContextUtils.descriptorToDeclarations(analyzeExhaust.getBindingContext(), clazzMethodDescriptor);
 										for(PsiElement e : elements)
 											list.add((NapileElement) e);
 									}
 								}
 					}    */
 
 					return list;
 				}
 
 				@NotNull
 				@Override
 				protected String getTitle()
 				{
 					return "Go to overrided methods";
 				}
 
 				@NotNull
 				@Override
 				protected Icon getIcon()
 				{
 					return AllIcons.Gutter.OverridenMethod;
 				}
 			},
 	CLASS_OVERRIDEN
 			{
 				@NotNull
 				@Override
 				public Icon getIcon()
 				{
 					return AllIcons.Gutter.OverridenMethod;
 				}
 
 				@NotNull
 				@Override
 				public String getTitle()
 				{
 					return "Go to overriding classes";
 				}
 
 				@NotNull
 				@Override
 				protected List<NapileElement> getTargets(PsiElement element)
 				{
 					if(!(element instanceof NapileClass))
 						return Collections.emptyList();
 
 					final NapileClass napileClass = (NapileClass) element;
 					AnalyzeExhaust analyzeExhaust = WholeProjectAnalyzerFacade.analyzeProjectWithCacheOnAFile(napileClass.getContainingFile());
 
 					BindingContext bindingContext = analyzeExhaust.getBindingContext();
 					ClassDescriptor classDeclaration = (ClassDescriptor) bindingContext.get(BindingContext.DECLARATION_TO_DESCRIPTOR, element);
 					if(classDeclaration == null)
 						return Collections.emptyList();
 					List<NapileElement> result = new ArrayList<NapileElement>();
 
 					Map<NapileClassLike, ClassDescriptor> res = JetShortNamesCache.getInstance(napileClass.getProject()).getAllClassesAndDescriptors(napileClass, napileClass.getResolveScope());
 					for(Map.Entry<NapileClassLike, ClassDescriptor> entry : res.entrySet())
 					{
 						if(entry.getValue() == classDeclaration)
 							continue;
 
 						if(DescriptorUtils.isSubclass(entry.getValue(), classDeclaration))
 							result.add(entry.getKey());
 					}
 
 					return result;
 				}
 			};
 
 	public final LineMarkerInfo getLineMarkers(@NotNull PsiElement element)
 	{
 		if(element instanceof PsiNameIdentifierOwner)
 		{
 			List<NapileElement> result = getTargets(element);
 
 			if(!result.isEmpty())
 			{
 				PsiElement name = ((PsiNameIdentifierOwner)element).getNameIdentifier();
 				if(name == null)
 					return null;
 
 				return new LineMarkerInfo<PsiElement>(name, name.getTextRange(), getIcon(), Pass.UPDATE_OVERRIDEN_MARKERS, new Function<PsiElement, String>()
 				{
 					@Override
 					public String fun(PsiElement element)
 					{
 						return getTitle();
 					}
 				}, new GutterIconNavigationHandler<PsiElement>()
 				{
 					@Override
 					public void navigate(MouseEvent e, PsiElement elt)
 					{
 						List<NapileElement> objects = getTargets(elt.getParent());
						PsiElementListNavigator.openTargets(e, objects.toArray(new NapileElement[objects.size()]), getTitle(), getTitle(), new DefaultPsiElementCellRenderer());
 					}
 				}, GutterIconRenderer.Alignment.LEFT
 				);
 			}
 		}
 		return null;
 	}
 
 	@NotNull
 	protected abstract List<NapileElement> getTargets(PsiElement element);
 
 	@NotNull
 	protected abstract String getTitle();
 
 	@NotNull
 	protected abstract Icon getIcon();
 }
