 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.core;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;
 
 /**
  * Extension point must contain nature entry and priority entry. Other entries
  * aren't required.
  * 
  * @author Haiodo
  * 
  */
 public class PriorityDLTKExtensionManager {
 	public final static String PRIORITY_ATTR = "priority";
 	
 	private Map extensions;
 
 	private String extensionPoint = null;
 	private String identifier = null;
 	/**
 	 * The preffered Id is not zero then
 	 */
 	private Map prefferedLevels = new HashMap();
 
 	protected void setIdentifierValue(String identifier) {
 		this.identifier = identifier;
 		if (this.extensions != null) {
 			this.extensions = null;
 		}
 	}
 
 	public static class ElementInfo {
 		int level;
 		public IConfigurationElement config;
 		public Object object;
 		public ElementInfo oldInfo;
 
 		protected ElementInfo(IConfigurationElement config) {
 			this.config = config;
 		}
 
 		public IConfigurationElement getConfig() {
 			return this.config;
 		}
 	}
 
 	public PriorityDLTKExtensionManager(String extensionPoint, String identifier) {
 		this.extensionPoint = extensionPoint;
 		this.identifier = identifier;
 		Assert.isNotNull(this.extensionPoint);
 		Assert.isNotNull(this.identifier);
 	}
 
 	private void initialize() {
 		if (extensions != null) {
 			return;
 		}
 
 		extensions = new HashMap(5);
 		IConfigurationElement[] cfg = Platform.getExtensionRegistry()
 				.getConfigurationElementsFor(this.extensionPoint);
 
 		for (int i = 0; i < cfg.length; i++) {
 			String nature = cfg[i].getAttribute(this.identifier);
 			ElementInfo oldInfo = (ElementInfo) extensions.get(nature);
 			if (oldInfo != null) {
 				int lev = getLevel(cfg[i]);
 				if (lev <= oldInfo.level) {
 					ElementInfo e = oldInfo;
 					ElementInfo nInfo = createNewInfo(cfg[i], null);
 					while (e != null) {
 						if (e.oldInfo == null) {
 							e.oldInfo = nInfo;
 							break;
 						} else {
 							if (nInfo.level < e.oldInfo.level) {
 								e = e.oldInfo;
 							} else {
 								nInfo.oldInfo = e.oldInfo;
 								e.oldInfo = nInfo;
 								break;
 							}
 						}
 					}
 					continue;
 				}
 			}
 			ElementInfo info = createNewInfo(cfg[i], oldInfo);
 			extensions.put(nature, info);
 		}
 	}
 
 	private ElementInfo createNewInfo(IConfigurationElement cfg,
 			ElementInfo oldInfo) {
 		ElementInfo info = createInfo(cfg);
 		info.level = getLevel(info.config);
 		info.oldInfo = oldInfo;
 		return info;
 	}
 
 	protected ElementInfo internalGetElementInfo(String id) {
 		initialize();
 		ElementInfo info = (ElementInfo) extensions.get(id);
 		Object level = prefferedLevels.get(id);
 		if( level != null) {
 			// Search for preffered id.
 			int prefferedLevel = ((Integer)level).intValue();
 			while( info != null ) {
 				if( info.level == prefferedLevel) {
 					return info;
 				}
 				info = info.oldInfo;
 			}
 		}
 		return info;
 	}
 	protected ElementInfo getElementInfo(String id) {
 		return internalGetElementInfo(id);
 	}
 
 	protected int getLevel(IConfigurationElement config) {
 		String priority = config.getAttribute(PRIORITY_ATTR);
 		if (priority == null) {
 			return 0;
 		}
 		try {
 			int parseInt = Integer.parseInt(priority);
 			return parseInt;
 		} catch (NumberFormatException ex) {
 			return 0;
 		}
 	}
 
 	public ElementInfo[] getElementInfos() {
 		initialize();
 //		Collection values = extensions.values();
 //		return (ElementInfo[]) values.toArray(new ElementInfo[values.size()]);
 		ElementInfo[] values = new ElementInfo[this.extensions.size()];
 		Iterator j = this.extensions.keySet().iterator();
 		for (int i = 0; i < values.length; i++) {
 			String key = (String) j.next();
 			values[i] = this.getElementInfo(key);
 		}
 		return values;
 	}
 
 	/**
 	 * Values config, nature, level are setted in current class.
 	 * 
 	 * @param config
 	 * @return
 	 */
 	protected ElementInfo createInfo(IConfigurationElement config) {
 		return new ElementInfo(config);
 	}
 
 	public String findScriptNature(IProject project) {
 		try {
 			String[] natureIds = project.getDescription().getNatureIds();
 			for (int i = 0; i < natureIds.length; i++) {
 				String natureId = natureIds[i];
 
 				if (getElementInfo(natureId) != null) {
 					return natureId;
 				}
 			}
 		} catch (CoreException e) {
 			return null;
 		}
 
 		return null;
 	}
 	public void setPreffetedLevel(String id, int level) {
 		if( level != -1 ) {
 			this.prefferedLevels.put( id, new Integer(level));
 		}
 		else {
 			this.prefferedLevels.put( id, null);
 		}
 	}
 }
