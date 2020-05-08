 /***************************************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: IBM Corporation - initial API and implementation
  **************************************************************************************************/
 package org.eclipse.jst.j2ee.internal.deployables;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.j2ee.application.Application;
 import org.eclipse.jst.j2ee.componentcore.util.EARArtifactEdit;
 import org.eclipse.jst.server.core.IEnterpriseApplication;
 import org.eclipse.jst.server.core.IJ2EEModule;
 import org.eclipse.jst.server.core.ILooseArchive;
 import org.eclipse.jst.server.core.ILooseArchiveSupport;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 import org.eclipse.wst.server.core.IModule;
 
 
 
 public class EnterpriseApplicationDeployable extends J2EEFlexProjDeployable implements IEnterpriseApplication, ILooseArchiveSupport {
 
 	public static final String EAR_MODULE_TYPE = "jst.ear"; //$NON-NLS-1$
 
 	public EnterpriseApplicationDeployable(IProject project, String aFactoryId, IVirtualComponent aComponent) {
 		super(project, aFactoryId, aComponent);
 
 
 	}
 
 	public String getJ2EESpecificationVersion() {
 		String Version = "1.2"; //$NON-NLS-1$
 
 		EARArtifactEdit earEdit = null;
 		try {
 			earEdit = EARArtifactEdit.getEARArtifactEditForRead(component);
 			if (earEdit != null) {
 				int nVersion = earEdit.getJ2EEVersion();
 				switch (nVersion) {
 					case 12 :
 						Version = IModuleConstants.J2EE_VERSION_1_2;
 						break;
 					case 13 :
 						Version = IModuleConstants.J2EE_VERSION_1_3;
 						break;
 					case 14 :
 						Version = IModuleConstants.J2EE_VERSION_1_4;
 						break;
 					default :
 						Version = IModuleConstants.J2EE_VERSION_1_2;
 						break;
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (earEdit != null)
 				earEdit.dispose();
 		}
 
 		return Version;
 	}
 
 	public IModule[] getModules() {
 		List modules = new ArrayList(3);
 		EARArtifactEdit earEdit = null;
 		try {
 			earEdit = EARArtifactEdit.getEARArtifactEditForRead(component);
 			if (earEdit != null) {
 				List components = earEdit.getJ2EEModuleReferences();
 				for (Iterator iter = components.iterator(); iter.hasNext();) {
 					IVirtualReference reference = (IVirtualReference) iter.next();
 					IVirtualComponent virtualComp = reference.getReferencedComponent();
 					Object module = FlexibleProjectServerUtil.getModule(virtualComp);
 					modules.add(module);
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (earEdit != null)
 				earEdit.dispose();
 		}
 		IModule[] moduleArray = new IModule[modules.size()];
 		modules.toArray(moduleArray);
 		return moduleArray;
 
 	}
 
 	public String getURI(IJ2EEModule module) {
 		// TODO Auto-generated method stub
 		J2EEFlexProjDeployable mod = (J2EEFlexProjDeployable)module;
 		IVirtualComponent comp = ComponentCore.createComponent(mod.getProject(),mod.getComponentHandle().getName());
 		EARArtifactEdit earEdit = null;
 		String aURI = null;
 		try {
 			earEdit = EARArtifactEdit.getEARArtifactEditForRead(component);
 			if (earEdit != null) {
 				aURI = earEdit.getModuleURI(comp);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (earEdit != null)
 				earEdit.dispose();
 		}
 		
 		return aURI;
 	}
 
 	public boolean containsLooseModules() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 
 	public IModule[] getLooseArchives() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public String getURI(ILooseArchive archive) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
