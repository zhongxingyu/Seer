 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.wst.server.core.internal;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.wst.server.core.IModuleType;
 /**
  * 
  */
 public class ModuleType implements IModuleType {
 	protected String id;
 	protected String version;
 	
 	//	cached copy of all module types
 	private static List moduleTypes;
 	
 	public ModuleType(String id, String version) {
 		super();
 		this.id = id;
 		this.version = version;
 	}
 
 	public String getId() {
 		return id;
 	}
 	
 	public String getName() {
 		ModuleKind mt = findModuleType(id);
 		if (mt != null)
 			return mt.getName();
		return Messages.moduleTypeUnknown;
 	}
 
 	public String getVersion() {
 		return version;
 	}
 	
 	/**
 	 * Returns an array of all known module types.
 	 * <p>
 	 * A new array is returned on each call, so clients may store or modify the result.
 	 * </p>
 	 * 
 	 * @return the array of module types {@link IModuleType}
 	 */
 	/*public static IModuleType[] getModuleTypes() {
 		if (moduleTypes == null)
 			loadModuleTypes();
 		
 		IModuleType[] mt = new IModuleType[moduleTypes.size()];
 		moduleTypes.toArray(mt);
 		return mt;
 	}*/
 
 	/**
 	 * Returns the module type with the given id, or <code>null</code>
 	 * if none. This convenience method searches the list of known
 	 * module types for the one a matching
 	 * module type id ({@link ModuleType#getId()}). The id may not be null.
 	 *
 	 * @param id the module type id
 	 * @return the module type, or <code>null</code> if there is no module type
 	 * with the given id
 	 */
 	public static ModuleKind findModuleType(String id) {
 		if (id == null)
 			throw new IllegalArgumentException();
 
 		if (moduleTypes == null)
 			loadModuleTypes();
 		
 		Iterator iterator = moduleTypes.iterator();
 		while (iterator.hasNext()) {
 			ModuleKind moduleType = (ModuleKind) iterator.next();
 			if (id.equals(moduleType.getId()))
 				return moduleType;
 		}
 		return null;
 	}
 	
 	/**
 	 * Load the module types.
 	 */
 	private static synchronized void loadModuleTypes() {
 		if (moduleTypes != null)
 			return;
 		Trace.trace(Trace.EXTENSION_POINT, "->- Loading .moduleTypes extension point ->-");
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IConfigurationElement[] cf = registry.getConfigurationElementsFor(ServerPlugin.PLUGIN_ID, "moduleTypes");
 
 		int size = cf.length;
 		moduleTypes = new ArrayList(size);
 		for (int i = 0; i < size; i++) {
 			try {
 				ModuleKind moduleType = new ModuleKind(cf[i]);
 				moduleTypes.add(moduleType);
 				Trace.trace(Trace.EXTENSION_POINT, "  Loaded moduleType: " + cf[i].getAttribute("id"));
 			} catch (Throwable t) {
 				Trace.trace(Trace.SEVERE, "  Could not load moduleType: " + cf[i].getAttribute("id"), t);
 			}
 		}
 		
 		Trace.trace(Trace.EXTENSION_POINT, "-<- Done loading .moduleTypes extension point -<-");
 	}
 
 	public String toString() {
 		return "ModuleType[" + id + ", " + version + "]";
 	}
 }
