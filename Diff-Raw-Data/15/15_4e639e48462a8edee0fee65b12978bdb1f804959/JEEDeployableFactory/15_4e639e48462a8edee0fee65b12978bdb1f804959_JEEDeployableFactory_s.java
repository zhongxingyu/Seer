 /*******************************************************************************
  * Copyright (c) 2003, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jee.internal.deployables;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.jst.j2ee.internal.deployables.J2EEDeployableFactory;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.model.ModuleDelegate;
 
 /**
  * J2EE module factory.
  */
 public class JEEDeployableFactory extends J2EEDeployableFactory {
 	protected Map <IModule, ModuleDelegate> moduleDelegates = new HashMap<IModule, ModuleDelegate>(5);
 
 	public static final String JEE_ID = "org.eclipse.jst.jee.server"; //$NON-NLS-1$
 	public static JEEDeployableFactory JEE_INSTANCE;
 	public static JEEDeployableFactory jeeInstance() {
 		if( JEE_INSTANCE == null ) {
 			ensureFactoryLoaded(JEE_ID);
 		}
 		return JEE_INSTANCE;
 	}
 	
 	public JEEDeployableFactory() {
 		super();
		JEE_INSTANCE = this;
 	}
 
 	@Override
 	protected boolean canHandleProject(IProject p) {
 		return J2EEProjectUtilities.isJEEProject(p);
 	}
 
 	@Override
	protected ModuleDelegate getNestedDelegate(IVirtualComponent component) {
 		return new JEEFlexProjDeployable(component.getProject(), component);
 	}
 
 }
