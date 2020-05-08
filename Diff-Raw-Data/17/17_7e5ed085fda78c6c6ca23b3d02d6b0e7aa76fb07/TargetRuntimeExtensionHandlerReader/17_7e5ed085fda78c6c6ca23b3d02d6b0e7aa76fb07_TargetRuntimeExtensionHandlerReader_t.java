 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  * Created on Feb 5, 2004
  *
  * To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package org.eclipse.jst.j2ee.internal.servertarget;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.jem.util.RegistryReader;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 
 /**
  * @author vijayb
  * @deprecated
  * To change the template for this generated type comment go to Window - Preferences - Java - Code
  * Generation - Code and Comments
  */
 public class TargetRuntimeExtensionHandlerReader extends RegistryReader {
 	static TargetRuntimeExtensionHandlerReader instance = null;
 	protected ITargetRuntimeExtensionHandler targetRuntimeExtHandler;
 	protected String HANDLER_EXT_Id = "targetRuntimeExtensionHandler"; //$NON-NLS-1$
 	protected TargetRuntimeExtension extension = null;
 	protected String HANDLER_CLASSNAME = "className"; //$NON-NLS-1$
 	protected String HANDLER_GROUP_ID = "groupID"; //$NON-NLS-1$
 	protected HashMap handlerExtensions = null;
 
 	/**
 	 * @param registry
 	 * @param plugin
 	 * @param extensionPoint
 	 */
 	public TargetRuntimeExtensionHandlerReader(IPluginRegistry registry, String plugin, String extensionPoint) {
 		super(plugin, extensionPoint);
 	}
 
 	/**
 	 *  
 	 */
 	public TargetRuntimeExtensionHandlerReader() {
 		super(J2EEPlugin.PLUGIN_ID, "TargetRuntimeExtHandler"); //$NON-NLS-1$
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.internal.RegistryReader#readElement(org.eclipse.core.runtime.IConfigurationElement)
 	 */
 	public boolean readElement(IConfigurationElement element) {
 		if (!element.getName().equals(HANDLER_EXT_Id))
 			return false;
 		String group = element.getAttribute(HANDLER_GROUP_ID);
 		String className = element.getAttribute(HANDLER_CLASSNAME);
 		if (group == null) {
 			logMissingAttribute(element, "Missing group target runtime extension specification."); //$NON-NLS-1$
 			return false;
 		}
 		try {
			String pluginId = element.getDeclaringExtension().getNamespace();
			Plugin plugin = Platform.getPlugin(pluginId);
 			extension = new TargetRuntimeExtension(plugin, element, group, className);
 			addExtensionPoint(extension);
 			return true;
		} catch (Exception ce) {
 			ce.printStackTrace();
 		}
 		return false;
 	}
 
 	/**
 	 * Sets the extension point.
 	 * 
 	 * @param extensions
 	 *            The extensions to set
 	 */
 	protected void addExtensionPoint(TargetRuntimeExtension newExtension) {
 		if (handlerExtensions == null)
 			handlerExtensions = new HashMap();
 		Collection temp = null;
 		Object holder = handlerExtensions.get(newExtension.getGroupId());
 		if (temp == null) {
 			temp = new ArrayList();
 			temp.add(newExtension);
 		} else {
 			handlerExtensions.remove(newExtension.getGroupId());
 			temp = (Collection) holder;
 			temp.add(newExtension);
 		}
 		handlerExtensions.put(newExtension.getGroupId(), temp);
 	}
 
 	/**
 	 * Gets the instance.
 	 * 
 	 * @return Returns a TargetRuntimeExtensionHandlerReader
 	 */
 	public static TargetRuntimeExtensionHandlerReader getInstance() {
 		if (instance == null)
 			instance = new TargetRuntimeExtensionHandlerReader();
 		return instance;
 	}
 
 	public ITargetRuntimeExtensionHandler getEJBExtHandler() {
 		if (targetRuntimeExtHandler == null && handlerExtensions != null) {
 			TargetRuntimeExtension codegenExt = null;
 			ArrayList ibmExtensions = (ArrayList) handlerExtensions.get("IBM"); //$NON-NLS-1$
 			if (ibmExtensions != null) {
 				for (int i = 0; i < ibmExtensions.size(); i++) {
 					Object obj = ibmExtensions.get(i);
 					if (obj instanceof TargetRuntimeExtension) {
 						codegenExt = (TargetRuntimeExtension) obj;
 						break;
 					}
 				}
 			}
 			if (codegenExt != null) {
 				IConfigurationElement cElement = codegenExt.configElement;
 				try {
 					targetRuntimeExtHandler = (ITargetRuntimeExtensionHandler) cElement.createExecutableExtension("run"); //$NON-NLS-1$
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return targetRuntimeExtHandler;
 	}
 }
