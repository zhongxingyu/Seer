 /*
  * Copyright 2010-2013 napile.org
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
 
 package org.napile.idea.plugin.run;
 
 import org.jetbrains.annotations.Nullable;
 import org.napile.asm.resolve.name.FqName;
 import org.napile.compiler.analyzer.AnalyzeExhaust;
 import org.napile.compiler.lang.descriptors.MutableClassDescriptor;
 import org.napile.compiler.lang.descriptors.SimpleMethodDescriptor;
 import org.napile.compiler.lang.psi.NapileClass;
 import org.napile.compiler.lang.psi.NapileDeclaration;
 import org.napile.compiler.lang.psi.NapileFile;
 import org.napile.compiler.lang.resolve.BindingContext;
 import org.napile.compiler.lang.resolve.DescriptorUtils;
 import org.napile.compiler.util.RunUtil;
 import org.napile.idea.plugin.module.Analyzer;
 import com.intellij.execution.Location;
 import com.intellij.execution.RunnerAndConfigurationSettings;
 import com.intellij.execution.actions.ConfigurationContext;
 import com.intellij.execution.junit.RuntimeConfigurationProducer;
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.module.ModuleUtilCore;
 import com.intellij.openapi.projectRoots.Sdk;
 import com.intellij.openapi.roots.ModuleRootManager;
 import com.intellij.psi.PsiElement;
 
 /**
  * @author VISTALL
  * @date 13:22/08.01.13
  */
 public class NapileConfigurationProducer extends RuntimeConfigurationProducer implements Cloneable
 {
 	private PsiElement element;
 
 	public NapileConfigurationProducer()
 	{
 		super(NapileConfigurationType.getInstance());
 	}
 
 	@Override
 	public PsiElement getSourceElement()
 	{
 		return element;
 	}
 
 	@Nullable
 	@Override
 	protected RunnerAndConfigurationSettings createConfigurationByElement(Location location, ConfigurationContext context)
 	{
 		PsiElement element = location.getPsiElement();
 		NapileClass napileClass = null;
 		AnalyzeExhaust analyzeExhaust = null;
 
 		if(element instanceof NapileFile)
 		{
 			analyzeExhaust = Analyzer.analyzeAll((NapileFile) element);
 
			if(((NapileFile) element).getDeclarations().length == 0)
 				return null;
 
 			napileClass = ((NapileFile) element).getDeclarations()[0];
 		}
 		else if(element instanceof NapileClass)
 		{
 			napileClass = (NapileClass) element;
 			analyzeExhaust = Analyzer.analyzeAll(((NapileClass) element).getContainingFile());
 		}
 		else
 			return null;
 
 		MutableClassDescriptor descriptor = (MutableClassDescriptor) analyzeExhaust.getBindingContext().get(BindingContext.CLASS, napileClass);
 		if(descriptor == null)
 			return null;
 
 		NapileDeclaration mainMethod = null;
 		for(NapileDeclaration inner : napileClass.getDeclarations())
 		{
 			SimpleMethodDescriptor methodDescriptor = analyzeExhaust.getBindingContext().get(BindingContext.METHOD, inner);
 			if(methodDescriptor != null && RunUtil.isRunPoint(methodDescriptor))
 			{
 				mainMethod = inner;
 				break;
 			}
 		}
 
 		if(mainMethod != null)
 		{
 			this.element = mainMethod;
 
 			Module module = mainMethod.isValid() ? ModuleUtilCore.findModuleForPsiElement(mainMethod) : null;
 			if(module == null)
 				return null;
 
 			FqName fqName = DescriptorUtils.getFQName(descriptor).toSafe();
 
 			ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
 			Sdk sdk = rootManager.getSdk();
 			if(sdk == null)
 				return null;
 
 			RunnerAndConfigurationSettings settings = cloneTemplateConfiguration(location.getProject(), context);
 			NapileRunConfiguration configuration = (NapileRunConfiguration) settings.getConfiguration();
 			configuration.setModule(module);
 			configuration.setName(fqName.getFqName());
 			configuration.mainClass = fqName.getFqName();
 			configuration.jdkName = sdk.getName();
 
 			return settings;
 		}
 
 		return null;
 	}
 
 	@Override
 	public int compareTo(Object o)
 	{
 		return PREFERED;
 	}
 }
