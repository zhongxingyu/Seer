 /*******************************************************************************
  * Copyright (c) 2008 Ralf Ebert
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Ralf Ebert - initial API and implementation
  *******************************************************************************/
 package com.swtxml.extensions;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.Platform;
 
 import com.swtxml.definition.INamespaceDefinition;
 import com.swtxml.definition.INamespaceResolver;
 import com.swtxml.util.parser.ParseException;
 
 /**
  * @author Ralf Ebert <info@ralfebert.de>
  */
 public class ExtensionsNamespaceResolver implements INamespaceResolver {
 
 	private static final String NAMESPACE_EXTENSION_POINT_ID = "com.swtxml.namespaces";
 
 	public INamespaceDefinition resolveNamespace(String uri) {
 
 		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(
 				NAMESPACE_EXTENSION_POINT_ID);
 
 		for (IExtension extension : extensionPoint.getExtensions()) {
 			for (IConfigurationElement configurationElement : extension.getConfigurationElements()) {
 				if (!"namespaceResolver".equals(configurationElement.getName())) {
 					throw new ParseException("Invalid " + NAMESPACE_EXTENSION_POINT_ID
 							+ " element: " + configurationElement);
 				}
 				INamespaceResolver resolver = getNamespaceResolver(configurationElement);
 				INamespaceDefinition namespace = resolver.resolveNamespace(uri);
 				if (namespace != null) {
 					return namespace;
 				}
 			}
 		}
 
 		return null;
 	}
 
 	public static boolean isAvailable() {
 		try {
 			return (Platform.getExtensionRegistry() != null);
		} catch (Exception e) {
 			return false;
 		}
 	}
 
 	private INamespaceResolver getNamespaceResolver(IConfigurationElement configurationElement) {
 		try {
 			return (INamespaceResolver) configurationElement.createExecutableExtension("class");
 		} catch (CoreException e) {
 			throw new ParseException(e.getMessage(), e);
 		}
 	}
 
 }
