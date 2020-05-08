 /*******************************************************************************
  * Copyright (c) 2009 Red Hat and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.common.internal.modulecore;
 
 import java.util.List;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.wst.common.componentcore.internal.flat.AbstractFlattenParticipant;
 import org.eclipse.wst.common.componentcore.internal.flat.IFlatResource;
 import org.eclipse.wst.common.componentcore.internal.flat.VirtualComponentFlattenUtility;
 import org.eclipse.wst.common.componentcore.internal.flat.FlatVirtualComponent.FlatComponentTaskModel;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 
 /**
  * Copy the classpath LIB references from a child with 
  * a ../ runtime path into a receiving parent.
  * 
  * @author rob
  *
  */
 public class AddClasspathLibReferencesParticipant extends AbstractFlattenParticipant {
 	private List<IFlatResource> list;
 	@Override
 	public boolean shouldIgnoreReference(IVirtualComponent rootComponent,
 			IVirtualReference referenced, FlatComponentTaskModel dataModel) {
 		if( referenced.getRuntimePath().equals(IClasspathDependencyReceiver.RUNTIME_MAPPING_INTO_CONTAINER_PATH))
 			return true;
 		return false;
 	}
 
 	
 	@Override
 	public void finalize(IVirtualComponent component,
 			FlatComponentTaskModel dataModel, List<IFlatResource> resources) {
 		this.list = resources;
 		if( !(component instanceof IClasspathDependencyReceiver ))
 			return;
		addReferencedComponentClasspathDependencies((IClasspathDependencyReceiver)component);
 	}
 
 	private void addReferencedComponentClasspathDependencies(final IClasspathDependencyReceiver component) {
 		final IVirtualReference[] refs = component.getReferences();
 		for (int i = 0; i < refs.length; i++) {
 			final IVirtualReference reference = refs[i];
 			final IPath runtimePath = reference.getRuntimePath();
 			final IVirtualComponent referencedComponent = reference.getReferencedComponent();
 
 			// if the reference cannot export dependencies, skip
 			if( !(referencedComponent instanceof IClasspathDependencyProvider) )
 				continue;
 			
 			if (!referencedComponent.isBinary() && referencedComponent instanceof IClasspathDependencyProvider) {
 				final IVirtualReference[] cpRefs = ((IClasspathDependencyProvider) referencedComponent).getJavaClasspathReferences();
 				for (int j = 0; j < cpRefs.length; j++) {
 					final IVirtualReference cpRef = cpRefs[j];
 					IPath cpRefRuntimePath = cpRef.getRuntimePath();
 
 					if (cpRef.getReferencedComponent() instanceof IClasspathDependencyComponent) {
 						// want to avoid adding dups
 						IClasspathDependencyComponent cpComp = (IClasspathDependencyComponent) cpRef.getReferencedComponent();
 						// don't want to process class folder refs here
 						if (cpComp.isClassFolder())
 							continue;
 
 						//if path isn't ../, it shouldn't be added here [bug 247090]
 						if( !cpRefRuntimePath.equals(IClasspathDependencyReceiver.RUNTIME_MAPPING_INTO_CONTAINER_PATH))
 							continue;
 						
 						// TODO: verify this cpRefRuntimePath is acceptable?
 						//if( !runtimePath.equals(component.getClasspathFolderPath(cpComp))) continue;
 						
 						cpRefRuntimePath = runtimePath;
 						new VirtualComponentFlattenUtility(list, null).addFile(cpComp, cpRefRuntimePath, cpComp);
 					}
 				}
 			}
 		}
 	}
 	
 //	private boolean canExportClasspathComponentDependencies(IVirtualComponent component) {
 //		final IProject project = component.getProject();
 //		// check for valid project type
 //		if (JavaEEProjectUtilities.isEJBProject(project) 
 //				|| JavaEEProjectUtilities.isDynamicWebProject(project)
 //				|| JavaEEProjectUtilities.isJCAProject(project)
 //    			|| JavaEEProjectUtilities.isUtilityProject(project)) {
 //			return true;
 //		}
 //		return false;
 //	}
 }
