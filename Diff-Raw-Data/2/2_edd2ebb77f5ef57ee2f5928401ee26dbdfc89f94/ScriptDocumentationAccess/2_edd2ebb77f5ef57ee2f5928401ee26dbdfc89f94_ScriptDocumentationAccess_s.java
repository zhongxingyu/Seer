 /*******************************************************************************
  * Copyright (c) 2000, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.dltk.ui.documentation;
 
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.core.IMember;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 
 
 /**
  * Helper needed to get access to script documentation.
  * 
  * <p>
  * This class is not intended to be subclassed or instantiated by clients.
  * </p>
  */
 public class ScriptDocumentationAccess {
 	private static final String DOCUMENTATION_PROVIDERS_EXTENSION_POINT = "org.eclipse.dltk.ui.scriptDocumentationProviders";
 	private static final String ATTR_CLASS = "class";
 	private static final String ATTR_NATURE = "nature";
 	private static IScriptDocumentationProvider[] documentationProviders = null;
 	private static Map providerNatures = new HashMap();	
 
 	private ScriptDocumentationAccess() {
 	// do not instantiate
 	}
 
 	/**
 	 * Creates {@link IScriptDocumentationProvider} objects from configuration
 	 * elements.
 	 */
 	private static IScriptDocumentationProvider[] createProviders(IConfigurationElement[] elements) {
 		List result = new ArrayList(elements.length);
 		for (int i = 0; i < elements.length; i++) {
 			IConfigurationElement element = elements[i];
 			try {
 				IScriptDocumentationProvider pr = (IScriptDocumentationProvider) element.createExecutableExtension(ATTR_CLASS);
 				String nature = element.getAttribute(ATTR_NATURE);
 				result.add(pr);				
 				providerNatures.put(pr, nature);
 			} catch (CoreException e) {
 				DLTKUIPlugin.log(e);
 			}
 		}
 		return (IScriptDocumentationProvider[]) result.toArray(new IScriptDocumentationProvider[result.size()]);
 	}
 
 	/**
 	 * Returns all contributed documentation documentationProviders.
 	 */
 	private static IScriptDocumentationProvider[] getContributedProviders() {
 		if (documentationProviders == null) {
 			IExtensionRegistry registry = Platform.getExtensionRegistry();
 			IConfigurationElement[] elements = registry.getConfigurationElementsFor(DOCUMENTATION_PROVIDERS_EXTENSION_POINT);
 			providerNatures.clear();
 			documentationProviders = createProviders(elements);
 		}
 		return documentationProviders;
 	}
 
 	/**
 	 * Gets a reader for an IMember documentation. Content are found using
 	 * documentation documentationProviders, contributed via extension point.
 	 * The content does contain html code describing member. It may be for ex.
 	 * header comment or a man page. (if <code>allowExternal</code> is
 	 * <code>true</code>)
 	 * 
 	 * @param member
 	 *            The member to get documentation for.
 	 * @param allowInherited
 	 *            For procedures and methods: if member doesn't have it's own
 	 *            documentation, look into parent types methods.
 	 * @param allowExternal
 	 *            Allows external documentation like man-pages.
 	 * @return Reader for a content, or <code>null</code> if no documentation
 	 *         is found.
 	 * @throws ModelException
 	 *             is thrown when the elements documentaion can not be accessed
 	 */
 	public static Reader getHTMLContentReader(String nature, IMember member, boolean allowInherited, boolean allowExternal) throws ModelException {
 		IScriptDocumentationProvider[] providers = getContributedProviders();
 		for (int i = 0; i < providers.length; i++) {
 			IScriptDocumentationProvider p = providers[i];
 			String pNature  = (String) providerNatures.get(p);
 			if (!pNature.equals(nature))
 				continue;
 			Reader reader = p.getInfo(member, allowInherited, allowExternal);
 			if (reader != null) {
 				// TODO: add mechanism to combine several sources to one
 				return reader;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Gets a reader for an keyword documentaion. Content are found using ALL
 	 * documentation documentationProviders, contributed via extension point.
 	 * The content does contain html code describing member.
 	 * 
 	 * @param content
 	 *            The keyword to find.
 	 * @return Reader for a content, or <code>null</code> if no documentation
 	 *         is found.
 	 * @throws ModelException
 	 *             is thrown when the elements documentaion can not be accessed
 	 */
 	public static Reader getHTMLContentReader(String nature, String content) throws ModelException {
 		IScriptDocumentationProvider[] providers = getContributedProviders();
 		for (int i = 0; i < providers.length; i++) {
 			IScriptDocumentationProvider p = providers[i];
 			String pNature  = (String) providerNatures.get(p);
			if (!pNature.equals(nature))
 				continue;
 			Reader reader = p.getInfo(content);
 			if (reader != null) {
 				// TODO: add mechanism to combine several sources to one
 				return reader;
 			}
 		}
 		return null;
 	}
 }
