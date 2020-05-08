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
 package org.eclipse.wst.server.ui.internal.viewers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.wst.server.core.*;
 /**
  * Runtime type content provider.
  */
 public class RuntimeTypeTreeContentProvider extends AbstractTreeContentProvider {
 	public static final byte STYLE_VENDOR = 1;
 	public static final byte STYLE_VERSION = 2;
 	public static final byte STYLE_MODULE_TYPE = 3;
 	
 	protected boolean creation;
 	protected String type;
 	protected String version;
 	protected String runtimeTypeId;
 
 	/**
 	 * RuntimeTypeContentProvider constructor.
 	 * 
 	 * @param style a style
 	 * @param creation true to include runtimes that can be created
 	 */
 	public RuntimeTypeTreeContentProvider(byte style, boolean creation) {
 		super(style);
 		this.creation = creation;
 	}
 	
 	public RuntimeTypeTreeContentProvider(byte style, boolean creation, String type, String version, String runtimeTypeId) {
 		super(style, false);
 		this.type = type;
 		this.version = version;
 		this.runtimeTypeId = runtimeTypeId;
 		this.creation = creation;
 		
 		fillTree();
 	}
 	
 	public void fillTree() {
 		clean();
 		List list = new ArrayList();
 		if (style != STYLE_FLAT) {
 			IRuntimeType[] runtimeTypes = ServerUtil.getRuntimeTypes(type, version, runtimeTypeId);
 			if (runtimeTypes != null) {
 				int size = runtimeTypes.length;
 				for (int i = 0; i < size; i++) {
 					IRuntimeType runtimeType = runtimeTypes[i];
 					if (!creation || runtimeType.canCreate()) {
 						int order = getRuntimeOrder(runtimeType);
 						if (order > initialSelectionOrder) {
 							initialSelection = runtimeType;
 							initialSelectionOrder = order;
 						}
 						TreeElement ele = null;
 						if (style == STYLE_VENDOR) {
 							ele = getOrCreate(list, runtimeType.getVendor());
 							ele.contents.add(runtimeType);
 							elementToParentMap.put(runtimeType, ele);
 						} else if (style == STYLE_VERSION) {
 							ele = getOrCreate(list, runtimeType.getVersion());
 							ele.contents.add(runtimeType);
 							elementToParentMap.put(runtimeType, ele);
 						} else if (style == STYLE_MODULE_TYPE) {
							// TODO: does not handle "j2ee.*" format
 							IModuleType[] moduleTypes = runtimeType.getModuleTypes();
 							if (moduleTypes != null) {
 								int size2 = moduleTypes.length;
 								for (int j = 0; j < size2; j++) {
 									IModuleType mb = moduleTypes[j];
 									if (mb != null) {
 										ele = getOrCreate(list, mb.getName());
 										TreeElement ele2 = getOrCreate(ele.contents, mb.getName() + "/" + mb.getVersion(), mb.getVersion());
 										ele2.contents.add(runtimeType);
 										elementToParentMap.put(runtimeType, ele2);
 										elementToParentMap.put(ele2, ele);
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		} else {
 			IRuntimeType[] runtimeTypes = ServerUtil.getRuntimeTypes(type, version, runtimeTypeId);
 			if (runtimeTypes != null) {
 				int size = runtimeTypes.length;
 				for (int i = 0; i < size; i++) {
 					IRuntimeType runtimeType = runtimeTypes[i];
 					if (!creation || runtimeType.canCreate()) {
 						int order = getRuntimeOrder(runtimeType);
 						if (order > initialSelectionOrder) {
 							initialSelection = runtimeType;
 							initialSelectionOrder = order;
 						}
 						list.add(runtimeType);
 					}
 				}
 			}
 		}
 		elements = list.toArray();
 	}
 	
 	private int getRuntimeOrder(IRuntimeType runtimeType) {
 		return 0;
 	}
 }
