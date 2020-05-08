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
 package org.eclipse.jst.j2ee.componentcore.util;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.j2ee.application.Module;
 import org.eclipse.jst.j2ee.internal.common.classpath.J2EEComponentClasspathUpdater;
 import org.eclipse.jst.j2ee.internal.plugin.IJ2EEModuleConstants;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.ReferencedComponent;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.WorkbenchComponent;
 import org.eclipse.wst.common.componentcore.internal.resources.VirtualArchiveComponent;
 import org.eclipse.wst.common.componentcore.internal.resources.VirtualComponent;
 import org.eclipse.wst.common.componentcore.internal.util.IComponentImplFactory;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 import org.eclipse.wst.common.componentcore.resources.IVirtualResource;
 
 public class EARVirtualComponent extends VirtualComponent implements IComponentImplFactory {
 	
 	public EARVirtualComponent() {
 		super();
 	}
 
 	public EARVirtualComponent(IProject aProject, IPath aRuntimePath) {
 		super(aProject, aRuntimePath);
 	}
 
 	public IVirtualComponent createComponent(IProject aProject) {
 		return new EARVirtualComponent(aProject, new Path("/")); //$NON-NLS-1$
 	}
 
 	private static String getJarURI(final ReferencedComponent ref, final IVirtualComponent moduleComp) {
 		String uri = ref.getArchiveName();
 		if (uri == null || uri.length() < 0) {
 			uri = moduleComp.getName() + IJ2EEModuleConstants.JAR_EXT;
 		} else {
 			String prefix = ref.getRuntimePath().makeRelative().toString();
 			if (prefix.length() > 0) {
 				uri = prefix + "/" + uri; //$NON-NLS-1$
 			}
 		}
 		return uri;
 	}
 
 	private static List getHardReferences(IVirtualComponent earComponent) {
 		StructureEdit core = null;
 		List hardReferences = new ArrayList();
 		try {
 			core = StructureEdit.getStructureEditForRead(earComponent.getProject());
 			if (core != null && core.getComponent() != null) {
 				WorkbenchComponent component = core.getComponent();
 				if (component != null) {
 					List referencedComponents = component.getReferencedComponents();
 					for (Iterator iter = referencedComponents.iterator(); iter.hasNext();) {
 						ReferencedComponent referencedComponent = (ReferencedComponent) iter.next();
 						if (referencedComponent == null)
 							continue;
 						IVirtualReference vReference = StructureEdit.createVirtualReference(earComponent, referencedComponent);
 						if (vReference != null) {
 							IVirtualComponent referencedIVirtualComponent = vReference.getReferencedComponent();
 							if (referencedIVirtualComponent != null && referencedIVirtualComponent.exists()) {
 								String archiveName = null;
 								if (referencedComponent.getDependentObject() != null) {
 									archiveName = ((Module) referencedComponent.getDependentObject()).getUri();
 								} else {
 									if (referencedIVirtualComponent.isBinary()) {
 										archiveName = getJarURI(referencedComponent, referencedIVirtualComponent);
 									} else {
 										IProject referencedProject = referencedIVirtualComponent.getProject();
 										// If dependent object is not set, assume
 										// compname is module name + proper
 										// extension
 										if (J2EEProjectUtilities.isDynamicWebProject(referencedProject) || J2EEProjectUtilities.isStaticWebProject(referencedProject)) {
 											archiveName = referencedIVirtualComponent.getName() + IJ2EEModuleConstants.WAR_EXT;
 										} else if (J2EEProjectUtilities.isJCAProject(referencedProject)) {
 											archiveName = referencedIVirtualComponent.getName() + IJ2EEModuleConstants.RAR_EXT;
 										} else if (J2EEProjectUtilities.isUtilityProject(referencedProject)) {
 											archiveName = getJarURI(referencedComponent, referencedIVirtualComponent);
 										} else {
 											archiveName = referencedIVirtualComponent.getName() + IJ2EEModuleConstants.JAR_EXT;
 										}
 									}
 								}
 								vReference.setArchiveName(archiveName);
 								hardReferences.add(vReference);
 							}
 						}
 					}
 				}
 			}
 		} finally {
 			if (core != null)
 				core.dispose();
 		}
 		return hardReferences;
 	}
 
 	private static List getLooseArchiveReferences(IVirtualComponent earComponent, List hardReferences) {
 		List dynamicReferences = null;
 		IVirtualFolder rootFolder = earComponent.getRootFolder();
 		try {
 			IVirtualResource[] members = rootFolder.members();
 			for (int i = 0; i < members.length; i++) {
 				if (IVirtualResource.FILE == members[i].getType()) {
 					String archiveName = members[i].getName();
 					if (archiveName.toLowerCase().endsWith(".jar")) {
 						boolean shouldInclude = true;
 						for (int j = 0; j < hardReferences.size() && shouldInclude; j++) {
 							String tempArchiveName = ((IVirtualReference) hardReferences.get(j)).getArchiveName();
 							if (null != tempArchiveName && tempArchiveName.equals(archiveName)) {
 								shouldInclude = false;
 							}
 						}
 						if (shouldInclude) {
 							IResource iResource = members[i].getUnderlyingResource();
 							IVirtualComponent dynamicComponent = ComponentCore.createArchiveComponent(earComponent.getProject(), VirtualArchiveComponent.LIBARCHIVETYPE + iResource.getFullPath().toString());
 							IVirtualReference dynamicRef = ComponentCore.createReference(earComponent, dynamicComponent);
 							dynamicRef.setArchiveName(archiveName);
 							if (null == dynamicReferences) {
 								dynamicReferences = new ArrayList();
 							}
 							dynamicReferences.add(dynamicRef);
 						}
 					}
 				}
 			}
 		} catch (CoreException e) {
 			Logger.getLogger().logError(e);
 		}
 		return dynamicReferences;
 	}
 
 
 	public IVirtualReference[] getReferences() {
 		J2EEComponentClasspathUpdater.getInstance().trackEAR(getProject());
 		List hardReferences = getHardReferences(this);
 		List dynamicReferences = getLooseArchiveReferences(this, hardReferences);
 
 		if (dynamicReferences != null) {
 			hardReferences.addAll(dynamicReferences);
 		}
 
 		return (IVirtualReference[]) hardReferences.toArray(new IVirtualReference[hardReferences.size()]);
 	}
 
 	public IVirtualReference[] getAllReferences() {
 		// TODO Auto-generated method stub
 		return super.getAllReferences();
 	}
 
 	public void setReferences(IVirtualReference[] references) {
 		// TODO Auto-generated method stub
 		super.setReferences(references);
 	}
 
 }
