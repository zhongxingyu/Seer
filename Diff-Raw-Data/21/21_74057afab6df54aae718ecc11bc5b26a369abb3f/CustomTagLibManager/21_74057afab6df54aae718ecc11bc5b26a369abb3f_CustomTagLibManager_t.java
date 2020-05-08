 /******************************************************************************* 
  * Copyright (c) 2009 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.kb.taglib;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.Platform;
 import org.jboss.tools.jst.web.kb.WebKbPlugin;
 import org.jboss.tools.jst.web.kb.internal.taglib.CustomTagLibAttribute;
 import org.jboss.tools.jst.web.kb.internal.taglib.CustomTagLibrary;
 import org.osgi.framework.Bundle;
 
 /**
  * @author Alexey Kazakov
  */
 public class CustomTagLibManager {
 
 	private static final CustomTagLibManager INSTANCE = new CustomTagLibManager();
 
 	private ICustomTagLibrary[] libs = null;
 
 	private CustomTagLibManager() {
 	}
 
 	public static CustomTagLibManager getInstance() {
 		return INSTANCE;
 	}
 
 	/**
 	 * Returns all custom tag libraries which are defined via extension point org.jboss.tools.jst.web.kb.KbTagLib.
 	 * @param project
 	 * @param uri
 	 * @return
 	 */
 	public ICustomTagLibrary[] getLibraries() {
 		if(libs==null) {
 			Set<ICustomTagLibrary> libSet = new HashSet<ICustomTagLibrary>();
 	        IExtensionRegistry registry = Platform.getExtensionRegistry();
 			IExtensionPoint extensionPoint = registry.getExtensionPoint("org.jboss.tools.jst.web.kb.KbTagLib"); //$NON-NLS-1$
			if (extensionPoint == null) 
				return null;
 			IExtension[] extensions = extensionPoint.getExtensions();
 			for (int i=0; i<extensions.length; i++) {
 				IExtension extension = extensions[i];
 				IConfigurationElement[] elements = extension.getConfigurationElements();
 				for(int j=0; j<elements.length; j++) {
 					String uri = elements[j].getAttribute("uri"); //$NON-NLS-1$
 					String location = elements[j].getAttribute("location"); //$NON-NLS-1$
 					String version = elements[j].getAttribute("version"); //$NON-NLS-1$
 					String name = elements[j].getAttribute("version"); //$NON-NLS-1$
 					Bundle sourcePlugin = Platform.getBundle(elements[j].getNamespaceIdentifier());
 					File schemaLocation = null;
 					try {
 						String pluginPath = FileLocator.resolve(sourcePlugin.getEntry("/")).getPath();
 						if(uri==null || uri.length()==0 || location==null || location.length()==0) {
 							WebKbPlugin.getDefault().logWarning("Incorrect org.jboss.tools.jst.web.kb.KbTagLib extension in " + pluginPath + " plugin. URI or location can't be empty."); //$NON-NLS-1$ $NON-NLS-2$
 							continue;
 						}
 						schemaLocation = new File(pluginPath, location);
 					} catch (IOException e) {
 						WebKbPlugin.getDefault().logError(e);
 						continue;
 					}
 					if(schemaLocation.isFile()) {
 						CustomTagLibrary lib = new CustomTagLibrary(schemaLocation, uri, version, name);
 						libSet.add(lib);
 					} else {
 						WebKbPlugin.getDefault().logWarning("Can't load KB schema: " + schemaLocation); //$NON-NLS-1$
 					}
 				}
 			}
 			libs = libSet.toArray(new ICustomTagLibrary[0]);
 		}
 		return libs;
 	}
 
 	public CustomTagLibAttribute[] getComponentExtensions() {
 		//TODO
 		return null;
 	}
 }
