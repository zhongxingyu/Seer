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
 package org.eclipse.jst.common.componentcore.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jem.workbench.utility.JemProjectUtilities;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.validation.internal.IProjectValidationHelper;
 
 public class ProjectValidationHelper implements IProjectValidationHelper {
 
 	private static IContainer[] EMPTY_RESULT = new IContainer[] {};
 	
 	public IContainer[] getOutputContainers(IProject project) {
 		
 		if (project == null || JemProjectUtilities.getJavaProject(project)==null)
 			return EMPTY_RESULT;
 		IVirtualComponent comp = ComponentCore.createComponent(project);
 		if (comp == null || !comp.exists())
 			return EMPTY_RESULT;
 		return ComponentUtilities.getOutputContainers(comp);
 	}
 	
 	public IContainer[] getSourceContainers(IProject project) {
 		if (project == null || JemProjectUtilities.getJavaProject(project)==null)
 			return EMPTY_RESULT;
 		IVirtualComponent comp = ComponentCore.createComponent(project);
 		if (comp == null || !comp.exists())
 			return EMPTY_RESULT;
 		IPackageFragmentRoot[] roots = ComponentUtilities.getSourceContainers(comp);
 		List result = new ArrayList();
 		for (int i=0; i<roots.length; i++) {
			if (roots[i].getResource() != null && roots[i].getResource() instanceof IContainer)
				result.add(roots[i].getResource());
 		}
 		return (IContainer[]) result.toArray(new IContainer[result.size()]);
 	}
 
 }
