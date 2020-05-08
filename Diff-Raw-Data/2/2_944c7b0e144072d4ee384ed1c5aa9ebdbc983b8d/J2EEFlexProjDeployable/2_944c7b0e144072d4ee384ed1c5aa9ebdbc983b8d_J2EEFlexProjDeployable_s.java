 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.deployables;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jst.server.core.IJ2EEModule;
 import org.eclipse.wst.common.modulecore.ModuleCore;
 import org.eclipse.wst.common.modulecore.ModuleCoreNature;
 import org.eclipse.wst.common.modulecore.WorkbenchModule;
 import org.eclipse.wst.server.core.IModuleType;
 import org.eclipse.wst.server.core.util.ProjectModule;
 
 /**
  * J2EE deployable superclass.
  */
 public abstract class J2EEFlexProjDeployable extends ProjectModule implements IJ2EEModule {
 	private String factoryId;
     protected WorkbenchModule wbModule = null;
 
 
 	/**
 	 * Constructor for J2EEFlexProjDeployable.
 	 * 
 	 * @param project
 	 */
 	public J2EEFlexProjDeployable(IProject project, String aFactoryId, WorkbenchModule aWorkbenchModule) {
 		super(project);
 		factoryId = aFactoryId;
 		wbModule = aWorkbenchModule;
 	}
 
 	public String getJ2EESpecificationVersion() {
 		return "TestVersion";
 	}
 
 	/*
 	 * @see IJ2EEModule#getLocation()
 	 */
 	public IPath getLocation() {
 		
 		IPath path = null;
 	       if ( ModuleCoreNature.getModuleCoreNature(project) != null ) {  
         	if( wbModule != null ){
         		
         		IFolder outputContainer = ModuleCore.getOutputContainerRoot(wbModule);
        		path = outputContainer.getFullPath();
         	}
         }    
 	
 		return path;
 
 	}
 	
 
 
 	/*
 	 * @see IModule#getFactoryId()
 	 */
 	public String getFactoryId() {
 		return factoryId;
 	}
 
 
 
 
 
 	/**
 	 * @see com.ibm.etools.server.j2ee.IJ2EEModule#isBinary()
 	 */
 	public boolean isBinary() {
 		return false;
 	}
 
 	public String getModuleTypeName() {
 		return getName();
 	}
 
 	public String getModuleTypeVersion() {
 		return getVersion();
 	}
 
 	public String getVersion() {
 		return "1.2"; //$NON-NLS-1$
 	}
 
 	public String getType() {
 		return "j2ee.ear"; //$NON-NLS-1$
 	}
 
 	public IModuleType getModuleType() {
 		return new IModuleType() {
 
 			public String getId() {
 				return getType();
 			}
 
 			public String getName() {
 				return getModuleTypeName();
 			}
 
 			public String getVersion() {
 				return getModuleTypeVersion();
 			}
 		};
 
 	}
 
 
 }
