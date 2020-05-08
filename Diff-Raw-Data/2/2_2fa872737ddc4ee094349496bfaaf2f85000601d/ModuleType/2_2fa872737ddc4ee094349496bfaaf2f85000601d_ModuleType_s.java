 /*******************************************************************************
  * Copyright (c) 2003, 2007 IBM Corporation and others.
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
 
 	//	cached copy of all module kinds
 	private static List moduleKinds;
 
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
 	 * Returns the module type with the given id and version, or create a new one if
 	 * none already exists.
 	 * 
 	 * @param id the module type id
 	 * @param version the module type version
 	 * @return the module type
 	 */
	public static ModuleType getModuleType(String id, String version) {
 		if (moduleTypes == null)
 			moduleTypes = new ArrayList();
 		
 		// look for an existing one first
 		Iterator iterator = moduleTypes.iterator();
 		while (iterator.hasNext()) {
 			ModuleType mt = (ModuleType) iterator.next();
 			if ((id == null && mt.id == null) || (id != null && id.equals(mt.id))) {
 				if ((version == null && mt.version == null) || (version != null && version.equals(mt.version)))
 					return mt;
 			}
 		}
 		
 		// otherwise create one
 		ModuleType mt = new ModuleType(id, version);
 		moduleTypes.add(mt);
 		return mt;
 	}
 
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
 	private static ModuleKind findModuleType(String id) {
 		if (id == null)
 			throw new IllegalArgumentException();
 
 		if (moduleKinds == null)
 			loadModuleTypes();
 		
 		Iterator iterator = moduleKinds.iterator();
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
 		if (moduleKinds != null)
 			return;
 		Trace.trace(Trace.EXTENSION_POINT, "->- Loading .moduleTypes extension point ->-");
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IConfigurationElement[] cf = registry.getConfigurationElementsFor(ServerPlugin.PLUGIN_ID, "moduleTypes");
 
 		int size = cf.length;
 		moduleKinds = new ArrayList(size);
 		for (int i = 0; i < size; i++) {
 			try {
 				ModuleKind moduleType = new ModuleKind(cf[i]);
 				moduleKinds.add(moduleType);
 				Trace.trace(Trace.EXTENSION_POINT, "  Loaded moduleType: " + cf[i].getAttribute("id"));
 			} catch (Throwable t) {
 				Trace.trace(Trace.SEVERE, "  Could not load moduleType: " + cf[i].getAttribute("id"), t);
 			}
 		}
 		
 		Trace.trace(Trace.EXTENSION_POINT, "-<- Done loading .moduleTypes extension point -<-");
 	}
 
 	public int hashCode() {
 		int hash = 17;
 		if (id != null)
 			hash += id.hashCode();
 		if (version != null)
 			hash += version.hashCode();
 		return hash;
 	}
 
 	public boolean equals(Object obj) {
 		if (obj == this)
 			return true;
 		
 		if (!(obj instanceof ModuleType))
 			return false;
 		
 		ModuleType mt = (ModuleType) obj;
 		if (!matches(id, mt.id))
 			return false;
 		
 		if (!matches(version, mt.version))
 			return false;
 		
 		return true;
 	}
 
 	private static boolean matches(String a, String b) {
 		if (a == null || b == null || "*".equals(a) || "*".equals(b) || a.startsWith(b) || b.startsWith(a)
 			|| (a.endsWith(".*") && b.startsWith(a.substring(0, a.length() - 1)))
 			|| (b.endsWith(".*") && a.startsWith(b.substring(0, b.length() - 1))))
 			return true;
 		return false;
 	}
 
 	public String toString() {
 		return "ModuleType[" + id + ", " + version + "]";
 	}
 }
