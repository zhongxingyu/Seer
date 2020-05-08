 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.javascript.typeinference;
 
 import java.util.ArrayList;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.internal.javascript.reference.resolvers.ReferenceResolverContext;
 import org.eclipse.dltk.javascript.typeinference.IScriptableTypeProvider;
 import org.mozilla.javascript.Scriptable;
 
 public class ReferenceFactory {
 
 	private static IScriptableTypeProvider[] providers;
 
 	static {
 		initProviders();
 	}
 
 	private static void initProviders() {
 		IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
 				.getExtensionPoint(
 						"org.eclipse.dltk.javascript.core.customtype");
 		IExtension[] extensions = extensionPoint.getExtensions();
 		ArrayList providerList = new ArrayList();
 		for (int a = 0; a < extensions.length; a++) {
 			IConfigurationElement[] configurationElements = extensions[a]
 					.getConfigurationElements();
 			for (int b = 0; b < configurationElements.length; b++) {
 
 				IConfigurationElement configurationElement = configurationElements[b];
 				try {
 					Object createExecutableExtension = configurationElement
 							.createExecutableExtension("class");
 					if (createExecutableExtension instanceof IScriptableTypeProvider) {
 						providerList.add(createExecutableExtension);
 					}
 				} catch (CoreException e) {
 					e.printStackTrace();
 				}
 				// System.out.println(configurationElement.getName());
 			}
 		}
 		IScriptableTypeProvider[] pr = new IScriptableTypeProvider[providerList
 				.size()];
 		providerList.toArray(pr);
 		providers = pr;
 	}
 
	/**
	 * @since 2.0
	 */
 	public static IScriptableTypeProvider[] getScriptTypeProviders() {
 		return providers;
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public static StandardSelfCompletingReference createNumberReference(
 			String name) {
 		return new NativeNumberReference(name);
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public static StandardSelfCompletingReference createStringReference(
 			String name) {
 		return new NativeStringReference(name);
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public static StandardSelfCompletingReference createBooleanReference(
 			String name) {
 		return new NativeBooleanReference(name);
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public static StandardSelfCompletingReference createArrayReference(
 			String name) {
 		return new NativeArrayReference(name);
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public static StandardSelfCompletingReference createDateReference(
 			String name) {
 		return new NativeDateReference(name);
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public static StandardSelfCompletingReference createXMLReference(String name) {
 		return new NativeXMLReference(name);
 	}
 
 	/**
 	 * @param paramOrVarName
 	 * @param typeLowerCase
 	 * @return
 	 */
 	public static IReference createTypeReference(String paramOrVarName,
 			String type, ReferenceResolverContext rrc) {
 		if (type != null) {
 			String typeLowerCase = type.toLowerCase();
 			if ("boolean".equals(typeLowerCase)) {
 				return createBooleanReference(paramOrVarName);
 			}
 			if ("number".equals(typeLowerCase)) {
 				return createNumberReference(paramOrVarName);
 			}
 			if ("string".equals(typeLowerCase)) {
 				return createStringReference(paramOrVarName);
 			}
 			if ("date".equals(typeLowerCase)) {
 				return createDateReference(paramOrVarName);
 			}
 			if ("array".equals(typeLowerCase)) {
 				return createArrayReference(paramOrVarName);
 			}
 			if ("xml".equals(typeLowerCase)) {
 				return createXMLReference(paramOrVarName);
 			}
 
 			if (providers != null) {
 				for (int i = 0; i < providers.length; i++) {
 					IScriptableTypeProvider element = (IScriptableTypeProvider) providers[i];
 					Scriptable ref = element.getType(paramOrVarName, type);
 					if (ref != null)
 						return new ScriptableScopeReference(paramOrVarName,
 								ref, rrc);
 				}
 			}
 
 		}
 		return new StandardSelfCompletingReference(paramOrVarName, false);
 	}
 }
