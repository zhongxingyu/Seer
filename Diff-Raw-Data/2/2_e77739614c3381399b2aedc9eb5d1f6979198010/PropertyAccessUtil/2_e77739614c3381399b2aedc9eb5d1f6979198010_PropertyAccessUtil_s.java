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
 
 package org.napile.compiler.lang.resolve;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.napile.compiler.lang.descriptors.ClassDescriptor;
 import org.napile.compiler.lang.descriptors.MethodDescriptor;
 import org.napile.compiler.lang.descriptors.PropertyAccessInfo;
 import org.napile.compiler.lang.descriptors.VariableDescriptor;
 import org.napile.compiler.lang.lexer.NapileTokens;
 import org.napile.compiler.lang.psi.NapileNamedMethod;
 import com.intellij.openapi.util.Pair;
 import com.intellij.psi.tree.IElementType;
 
 /**
  * @author VISTALL
  * @date 10:12/03.11.12
  */
 public class PropertyAccessUtil
 {
 	public static void record(@NotNull BindingTrace trace, @NotNull VariableDescriptor variableDescriptor, @NotNull ClassDescriptor classDescriptor, @NotNull NapileNamedMethod namedMethod)
 	{
 		Map<ClassDescriptor, PropertyAccessInfo> map = trace.get(BindingContext.PROPERTY_ACCESS_INFO, variableDescriptor);
 		if(map == null)
 			trace.record(BindingContext.PROPERTY_ACCESS_INFO, variableDescriptor, map = new LinkedHashMap<ClassDescriptor, PropertyAccessInfo>());
 
 		PropertyAccessInfo propertyAccessInfo = map.get(classDescriptor);
 		if(propertyAccessInfo == null)
 			map.put(classDescriptor, propertyAccessInfo = new PropertyAccessInfo());
 
 		MethodDescriptor methodDescriptor = trace.safeGet(BindingContext.METHOD, namedMethod);
 		IElementType elementType = namedMethod.getPropertyAccessType();
 		if(elementType == NapileTokens.LAZY_KEYWORD)
 			propertyAccessInfo.setLazy(new Pair<MethodDescriptor, NapileNamedMethod>(methodDescriptor, namedMethod));
 		else if(elementType == NapileTokens.SET_KEYWORD)
 			propertyAccessInfo.setSet(new Pair<MethodDescriptor, NapileNamedMethod>(methodDescriptor, namedMethod));
 		else if(elementType == NapileTokens.GET_KEYWORD)
 			propertyAccessInfo.setGet(new Pair<MethodDescriptor, NapileNamedMethod>(methodDescriptor, namedMethod));
 		else
 			throw new IllegalArgumentException();
 	}
 
 	@Nullable
 	public static MethodDescriptor getPropertyDescriptor(@NotNull BindingReader trace, @NotNull VariableDescriptor variableDescriptor, @NotNull IElementType elementType)
 	{
 		return getPropertyDescriptor(trace, variableDescriptor, (ClassDescriptor) variableDescriptor.getContainingDeclaration(), elementType);
 	}
 
 	@Nullable
 	public static MethodDescriptor getPropertyDescriptor(@NotNull BindingReader trace, @NotNull VariableDescriptor variableDescriptor, @NotNull ClassDescriptor classDescriptor, @NotNull IElementType elementType)
 	{
 		Map<ClassDescriptor, PropertyAccessInfo> map = trace.get(BindingContext.PROPERTY_ACCESS_INFO, variableDescriptor);
 		if(map == null)
 			return null;
 		PropertyAccessInfo propertyAccessInfo = map.get(classDescriptor);
 		if(propertyAccessInfo == null)
 			return null;
 		Pair<MethodDescriptor, NapileNamedMethod> propertyInfo = null;
 		if(elementType == NapileTokens.LAZY_KEYWORD)
 			propertyInfo = propertyAccessInfo.getLazy();
 		else if(elementType == NapileTokens.SET_KEYWORD)
 			propertyInfo = propertyAccessInfo.getSet();
 		else if(elementType == NapileTokens.GET_KEYWORD)
 			propertyInfo = propertyAccessInfo.getGet();
 		else
 			throw new IllegalArgumentException();
 
 		return propertyInfo == null ? null : propertyInfo.getFirst();
 	}
 
 	@Nullable
 	public static Pair<MethodDescriptor, NapileNamedMethod> get(@NotNull BindingReader trace, @NotNull VariableDescriptor variableDescriptor, @NotNull IElementType elementType)
 	{
 		return get(trace, variableDescriptor, (ClassDescriptor) variableDescriptor.getContainingDeclaration(), elementType);
 	}
 
 	@Nullable
 	public static Pair<MethodDescriptor, NapileNamedMethod> get(@NotNull BindingReader trace, @NotNull VariableDescriptor variableDescriptor, @NotNull ClassDescriptor classDescriptor, @NotNull IElementType elementType)
 	{
 		Map<ClassDescriptor, PropertyAccessInfo> map = trace.get(BindingContext.PROPERTY_ACCESS_INFO, variableDescriptor);
 		if(map == null)
 			return null;
 		PropertyAccessInfo propertyAccessInfo = map.get(classDescriptor);
 		if(propertyAccessInfo == null)
 			return null;
 		if(elementType == NapileTokens.LAZY_KEYWORD)
 			return propertyAccessInfo.getLazy();
 		else if(elementType == NapileTokens.SET_KEYWORD)
 			return propertyAccessInfo.getSet();
 		else if(elementType == NapileTokens.GET_KEYWORD)
 			return propertyAccessInfo.getGet();
 		else
 			throw new IllegalArgumentException();
 	}
 }
