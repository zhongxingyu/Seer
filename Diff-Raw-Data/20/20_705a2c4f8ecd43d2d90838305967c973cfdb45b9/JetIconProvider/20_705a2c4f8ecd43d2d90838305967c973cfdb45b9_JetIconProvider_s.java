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
 
 package org.napile.idea.plugin;
 
 import java.util.List;
 
 import javax.swing.Icon;
 
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.napile.compiler.analyzer.AnalyzeExhaust;
 import org.napile.compiler.lang.descriptors.ClassDescriptor;
 import org.napile.compiler.lang.resolve.AnnotationUtils;
 import org.napile.compiler.lang.resolve.BindingContext;
 import org.napile.compiler.lang.rt.NapileAnnotationPackage;
 import org.napile.compiler.lexer.JetTokens;
 import org.napile.compiler.lang.psi.*;
 import org.napile.idea.plugin.project.WholeProjectAnalyzerFacade;
 import com.intellij.ide.IconProvider;
 import com.intellij.openapi.util.Condition;
 import com.intellij.openapi.util.IconLoader;
 import com.intellij.openapi.util.Iconable;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.util.PsiTreeUtil;
 import com.intellij.ui.LayeredIcon;
 import com.intellij.ui.RowIcon;
 import com.intellij.util.BitUtil;
 import com.intellij.util.PlatformIcons;
 import com.intellij.util.containers.ContainerUtil;
 
 /**
  * @author yole
  */
 public class JetIconProvider extends IconProvider
 {
 	private static final Icon FINAL_MARK_ICON = IconLoader.getIcon("/nodes/finalMark.png");
 
 	public static JetIconProvider INSTANCE = new JetIconProvider();
 
 	@Override
 	public Icon getIcon(@NotNull PsiElement psiElement, int flags)
 	{
 		Icon icon = null;
 		if(psiElement instanceof NapileFile)
 		{
 			NapileFile file = (NapileFile) psiElement;
 			NapileLikeClass mainClass = getMainClass(file);
 			icon = mainClass != null && file.getDeclarations().size() == 1 ? getIcon(mainClass, flags) : JetIcons.FILE;
 		}
 		else if(psiElement instanceof NapileNamespaceHeader)
 		{
			icon = (flags & Iconable.ICON_FLAG_OPEN) != 0 ? PlatformIcons.PACKAGE_OPEN_ICON : PlatformIcons.PACKAGE_ICON;
 		}
 		else if(psiElement instanceof NapileNamedFunction)
 		{
 			icon = PsiTreeUtil.getParentOfType(psiElement, NapileNamedDeclaration.class) instanceof NapileClass ? PlatformIcons.METHOD_ICON : JetIcons.FUNCTION;
 			if(((NapileMethod) psiElement).getReceiverTypeRef() != null)
 				icon = JetIcons.EXTENSION_FUNCTION;
 		}
 		else if(psiElement instanceof NapileConstructor)
 			icon = JetIcons.CONSTRUCTOR;
 		else if(psiElement instanceof NapileTypeParameter)
 			icon = JetIcons.TYPE_PARAMETER;
 		else if(psiElement instanceof NapileClass)
 		{
 			NapileClass napileClass = (NapileClass) psiElement;
 
 			AnalyzeExhaust analyzeExhaust = WholeProjectAnalyzerFacade.analyzeProjectWithCacheOnAFile(napileClass.getContainingFile());
 			ClassDescriptor descriptor = (ClassDescriptor) analyzeExhaust.getBindingContext().get(BindingContext.DECLARATION_TO_DESCRIPTOR, napileClass);
 
 			icon = napileClass.isEnum() ? JetIcons.ENUM : JetIcons.CLASS;
 
 			if(napileClass.hasModifier(JetTokens.ABSTRACT_KEYWORD))
 				icon = JetIcons.ABSTRACT_CLASS;
 
 			if(descriptor != null && AnnotationUtils.isAnnotation(descriptor))
 			{
 				icon = JetIcons.ANNOTATION;
 				if(AnnotationUtils.hasAnnotation(descriptor, NapileAnnotationPackage.REPEATABLE))
 					icon = JetIcons.REPEATABLE_ANNOTATION;
 			}
 		}
 		else if(psiElement instanceof NapileEnumEntry)
 			icon = JetIcons.VAL;
 		else if(psiElement instanceof NapileParameter)
 		{
 			icon = JetIcons.PARAMETER;
 			NapileParameter parameter = (NapileParameter) psiElement;
 			if(parameter.getValOrVarNode() != null)
 			{
 				NapileParameterList parameterList = PsiTreeUtil.getParentOfType(psiElement, NapileParameterList.class);
 				if(parameterList != null && parameterList.getParent() instanceof NapileClass)
 					icon = parameter.isMutable() ? JetIcons.FIELD_VAR : JetIcons.FIELD_VAL;
 			}
 		}
 		else if(psiElement instanceof NapileProperty)
 		{
 			NapileProperty property = (NapileProperty) psiElement;
 			icon = property.isVar() ? JetIcons.FIELD_VAR : JetIcons.FIELD_VAL;
 		}
 
 		return icon == null ? null : modifyIcon(psiElement instanceof NapileModifierListOwner ? ((NapileModifierListOwner) psiElement) : null, icon, flags);
 	}
 
 	@Nullable
 	public static NapileLikeClass getMainClass(@NotNull NapileFile file)
 	{
 		List<NapileClass> aClasses = ContainerUtil.filter(file.getDeclarations(), new Condition<NapileClass>()
 		{
 			@Override
 			public boolean value(NapileClass jetDeclaration)
 			{
 				return jetDeclaration instanceof NapileLikeClass;
 			}
 		});
 		if(aClasses.size() == 1)
 			return aClasses.get(0);
 		return null;
 	}
 
 	public static Icon modifyIcon(NapileModifierListOwner modifierList, Icon baseIcon, int flags)
 	{
 		RowIcon icon = new RowIcon(2);
 
 		if(baseIcon != null)
 			icon.setIcon(modifierList == null ? baseIcon : modifierList.hasModifier(JetTokens.FINAL_KEYWORD) ? new LayeredIcon(baseIcon, FINAL_MARK_ICON) : baseIcon, 0);
 
 		if(modifierList != null && BitUtil.isSet(flags, Iconable.ICON_FLAG_VISIBILITY))
 		{
 			if(modifierList.hasModifier(JetTokens.LOCAL_KEYWORD))
 				icon.setIcon(PlatformIcons.PRIVATE_ICON, 1);
 			else if(modifierList.hasModifier(JetTokens.COVERED_KEYWORD))
 				icon.setIcon(PlatformIcons.PROTECTED_ICON, 1);
 			else if(modifierList.hasModifier(JetTokens.HERITABLE_KEYWORD))
 				icon.setIcon(JetIcons.C_HERITABLE, 1);
 			else
 				icon.setIcon(PlatformIcons.PUBLIC_ICON, 1);
 		}
 
 		return icon;
 	}
 }
