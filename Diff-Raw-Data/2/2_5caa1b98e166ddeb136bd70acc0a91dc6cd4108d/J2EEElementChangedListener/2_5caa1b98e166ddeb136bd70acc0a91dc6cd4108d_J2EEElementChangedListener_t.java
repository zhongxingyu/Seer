 /*******************************************************************************
  * Copyright (c) 2005, 2007 BEA Systems, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * rfrost@bea.com - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.refactor.listeners;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.resources.WorkspaceJob;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.core.ElementChangedEvent;
 import org.eclipse.jdt.core.IElementChangedListener;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaElementDelta;
 import org.eclipse.jdt.core.IJavaModel;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.ModuleCoreNature;
 import org.eclipse.wst.common.componentcore.internal.ComponentResource;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.WorkbenchComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 
 /**
  * Implementation of <code>IElementChangedListener that updates mappings for src folders
  * in the .component file in response to changes in a project's Java classpath. 
  */
 public class J2EEElementChangedListener implements IElementChangedListener {
 
 	/**
 	 * Name of the Job family in which all component update jobs belong.
 	 */
 	public static final String PROJECT_COMPONENT_UPDATE_JOB_FAMILY =  "org.eclipse.jst.j2ee.refactor.component"; //$NON-NLS-1$
 	
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jdt.core.IElementChangedListener#elementChanged(org.eclipse.jdt.core.ElementChangedEvent)
 	 */
 	public void elementChanged(final ElementChangedEvent event) {
 		processJavaElementDelta(event.getDelta());
 	}
 	
 	private void processJavaElementDelta(final IJavaElementDelta delta) {
 		final int kind = delta.getKind();
 		if (kind == IJavaElementDelta.CHANGED) {
 			final int flags = delta.getFlags();
 			final IJavaElement element = delta.getElement();
 			if (element instanceof IJavaModel) {
 				if ((flags & IJavaElementDelta.F_CHILDREN) == IJavaElementDelta.F_CHILDREN) {
 					final IJavaElementDelta[] children = delta.getChangedChildren();
 					for (int i = 0; i < children.length; i++) {
 						// handle all of the IJavaProject children
 						processJavaElementDelta(children[i]);
 					}
 				} else {
 					// not a Java project (i.e. could be an EAR project)
 					processResourceDeltas(flags, kind, delta);
 				}
 			} else if (element instanceof IJavaProject) {
 				processJavaProject((IJavaProject) element, flags, kind, delta);
 			}
 		}
 	}
 	
 	private void processJavaProject(final IJavaProject jproject, final int flags, final int kind, final IJavaElementDelta delta) {
 
 		final IProject project = jproject.getProject();
 		final List pathsToAdd = new ArrayList();
 		final List pathsToRemove = new ArrayList();
 		final List changedJavaPaths = new ArrayList();
 		
 		// make certain this is a J2EE project
		if (ModuleCoreNature.isFlexibleProject(project)) {
 			IVirtualComponent c = ComponentCore.createComponent(project);
 			try {
 				// Did the classpath change?
 				if ((flags & IJavaElementDelta.F_CHILDREN) == IJavaElementDelta.F_CHILDREN) {
 					final boolean cpChanged = (flags & IJavaElementDelta.F_CLASSPATH_CHANGED) != 0; 
 					getJavaSrcMappings(c, delta.getAffectedChildren(), cpChanged, jproject, pathsToAdd, pathsToRemove, changedJavaPaths);
 				}
 
 				// Did a non-Java folder change name?
 				final IResourceDelta[] deltas = delta.getResourceDeltas();
 				if (deltas != null && deltas.length > 0) {
 					getNonJavaFolderMappings(deltas, c, pathsToAdd, pathsToRemove, changedJavaPaths);
 				}
 		
 			} catch (CoreException ce) {
 				Logger.getLogger(J2EEPlugin.PLUGIN_ID).logError(ce);
 				return;
 			}
 			updateMappingsInJob(pathsToAdd, pathsToRemove);
 		}		
 	}
 	
 	private void processResourceDeltas(final int flags, final int kind, final IJavaElementDelta delta) {
 		final List pathsToAdd = new ArrayList();
 		final List pathsToRemove = new ArrayList();
 
 		final IResourceDelta[] deltas = delta.getResourceDeltas();
 		if (deltas != null && deltas.length > 0) {
 			try {
 				getNonJavaFolderMappings(deltas, null, pathsToAdd, pathsToRemove, Collections.EMPTY_LIST);
 			} catch (CoreException ce) {
 				Logger.getLogger(J2EEPlugin.PLUGIN_ID).logError(ce);
 				return;
 			}
 		}
 		
 		updateMappingsInJob(pathsToAdd, pathsToRemove);
 	}
 	
 	/*
 	 * Adds and removes the specified component resource mappings in a WorkspaceJob
 	 */
 	private void updateMappingsInJob(final List pathsToAdd, final List pathsToRemove) {
 		// If there are corrections to the virtual path mappings, execute them in a Job
 		if (!pathsToAdd.isEmpty() || !pathsToRemove.isEmpty()) {
 			WorkspaceJob job = new WorkspaceJob("J2EEComponentMappingUpdateJob") {							
 				public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
 					for(int i=0;i<pathsToAdd.size(); i++){
 						Object[] toAdd = (Object[]) pathsToAdd.get(i);
 						final IVirtualFolder destFolder = (IVirtualFolder) toAdd[1];
 						final IPath pathToAdd = (IPath) toAdd[0];
 						destFolder.createLink(pathToAdd, 0, monitor);
 					}
 					for(int i=0;i<pathsToRemove.size(); i++){
 						Object[] toRemove = (Object[]) pathsToRemove.get(i);
 						final IVirtualFolder destFolder = (IVirtualFolder) toRemove[1];
 						final IPath pathToRemove = (IPath) toRemove[0];
 						destFolder.removeLink(pathToRemove, 0, monitor);
 					}
 					return Status.OK_STATUS;
 				}
 				public boolean belongsTo(final Object family) {
 					return PROJECT_COMPONENT_UPDATE_JOB_FAMILY.equals(family);
 				}
 			};
 			job.setRule(ResourcesPlugin.getWorkspace().getRoot());
 			job.schedule();
 		}						
 	}
 	
 	/*
 	 * Computes the virtual component path mapping changes the need to be made due to 
 	 * Java src path changes.
 	 */ 
 	private void getJavaSrcMappings(final IVirtualComponent virtualComp, final IJavaElementDelta[] children, final boolean cpChanged, final IJavaProject jproject, final List pathsToAdd, final List pathsToRemove, final List changedPaths) 
 		throws CoreException {
 		
 		// get the default destination folder
 		final IVirtualFolder defaultDestFolder = getDestinationFolder(virtualComp);
 		
 		for (int i = 0; i < children.length; i++) {
 			final IJavaElementDelta delta = children[i];
 			final IJavaElement element = delta.getElement();
 			if(element.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT){
 				final IPackageFragmentRoot root = (IPackageFragmentRoot) element;
 				int cpeKind = IPackageFragmentRoot.K_SOURCE;
 				boolean abortAdd = false;
 				try {
 					cpeKind = root.getKind();
 				} catch (JavaModelException jme) {
 					// this is thrown if the folder corresponding to the CPE has been deleted
 					// since it could represent another error, we need to abort adding. 
 					abortAdd = true;
 				}
 				// only update if we know it is a src folder
 				if (cpeKind == IPackageFragmentRoot.K_SOURCE) {
 					final int kind = delta.getKind();					
 					if (!cpChanged) {
 						// if the classpath is not changed, save modifications to the Java src path
 						if (kind == IJavaElementDelta.CHANGED || kind == IJavaElementDelta.REMOVED) {
 							changedPaths.add(root.getPath().removeFirstSegments(1));		
 						}
 					} else {
 					
 						// kind and flags for CP additions are somewhat sporadic; either:
 						// -kind is ADDED and flags are 0
 						//   or
 						// -kind is CHANGED and flags are F_ADDED_TO_CLASSPATH
 						final int flags = delta.getFlags();
 
 						if (kind == IJavaElementDelta.ADDED || 
 								(flags & IJavaElementDelta.F_ADDED_TO_CLASSPATH) == IJavaElementDelta.F_ADDED_TO_CLASSPATH) {
 							if (!abortAdd) {
 								final IPath pathToAdd = root.getPath().removeFirstSegments(1);
 								pathsToAdd.add(new Object[]{pathToAdd, defaultDestFolder});
 								// if the added src path was moved from another location, remove any mapping for that
 								// location
 								if ((flags & IJavaElementDelta.F_MOVED_FROM) == IJavaElementDelta.F_MOVED_FROM) {
 									final IJavaElement movedFromElement = delta.getMovedFromElement();
 									final IPath pathToRemove = movedFromElement.getPath().removeFirstSegments(1);
 									pathsToRemove.add(new Object[]{pathToRemove, defaultDestFolder});
 								}
 							}
 							// getting a kind = REMOVED and flags = 0 for removal of the folder (w/o removing the CPE), probably
 							// should not be generated
 						} else if ((flags & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) == IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) {
 							IPath path = root.getPath().removeFirstSegments(1);
 							pathsToRemove.add(new Object[]{path, defaultDestFolder});
 						} 
 					}			
 				}
 			}
 		}
 	}
 	
 	/*
 	 * Computes the virtual component path mapping changes the need to be made due to changes to
 	 * non-Java folders. 
 	 */ 
 	private void getNonJavaFolderMappings(final IResourceDelta[] deltas, final IVirtualComponent virtualComp, final List pathsToAdd, final List pathsToRemove, final List changedJavaPaths) throws CoreException {
 		IVirtualFolder rootFolder = null;
 		if (virtualComp != null) {
 			rootFolder = virtualComp.getRootFolder();
 		}
 		Map sourceToRuntime = null;
 		if (virtualComp != null) {
 			sourceToRuntime = getResourceMappings(virtualComp.getProject());
 		}
 		for (int i = 0; i < deltas.length; i++) {
 			final IResourceDelta delta = deltas[i];
 			processResourceDelta(delta, rootFolder, sourceToRuntime, pathsToAdd, pathsToRemove, changedJavaPaths);
 		}
 	}
 	
 	/*
 	 * Processes a single IResourceDelta.
 	 */
 	private void processResourceDelta(final IResourceDelta delta, IVirtualFolder rootFolder, Map sourceToRuntime, final List pathsToAdd, final List pathsToRemove, final List changedJavaPaths) throws CoreException {
 		final int kind = delta.getKind();
 		if (kind == IResourceDelta.CHANGED) {
 			IResourceDelta[] childDeltas = delta.getAffectedChildren();
 			for (int i = 0; i < childDeltas.length; i++) {
 				processResourceDelta(childDeltas[i], rootFolder, sourceToRuntime, pathsToAdd, pathsToRemove, changedJavaPaths);
 			}
 		} else {
 			final int flags = delta.getFlags();
 			if ((flags & IResourceDelta.MOVED_FROM) == IResourceDelta.MOVED_FROM) {
 				if (rootFolder == null) {
 					final IProject project = delta.getResource().getProject();
 					// make certain this is a J2EE project
 					if (ModuleCoreNature.getModuleCoreNature(project) != null) {
 						IVirtualComponent c = ComponentCore.createComponent(project);
 						rootFolder = c.getRootFolder();
 						sourceToRuntime = getResourceMappings(project);
 					} else {
 						// not a J2EE project
 						return;
 					}
 				}
 				final IPath movedFrom = delta.getMovedFromPath().removeFirstSegments(1);
 				if (changedJavaPaths.contains(movedFrom)) {
 					// don't update renamed Java src paths
 					return;
 				}
 				final IPath movedTo = delta.getFullPath().removeFirstSegments(1);
 				final IPath runtimePath = (IPath) sourceToRuntime.get(movedFrom);
 				// does the old path have a virtual component mapping?
 				if (runtimePath != null) {
 					final IVirtualFolder folder = rootFolder.getFolder(runtimePath);
 					// only add if the project relative paths are not equal (these can be equal when the root folder is mapped and the project is renamed)
 					if (!movedFrom.equals(movedTo)) {
 						pathsToRemove.add(new Object[]{movedFrom, folder});
 						pathsToAdd.add(new Object[]{movedTo, folder});
 					}
 				}
 			}
 		}
 	}
 	
 	private Map getResourceMappings(final IProject project){
 		final Map sourceToRuntime = new HashMap();
 		StructureEdit core = null;
 		try {
 			core = StructureEdit.getStructureEditForRead(project);
 			final WorkbenchComponent component = core.getComponent();
 			if (null != component) {
 				final List currentResources = component.getResources();
 				for (Iterator iter = currentResources.iterator(); iter.hasNext();) {
 					final ComponentResource resource = (ComponentResource) iter.next();
 					sourceToRuntime.put(resource.getSourcePath().makeRelative(), resource.getRuntimePath());
 				}
 			}
 			return sourceToRuntime;
 		} finally {
 			if (core != null)
 				core.dispose();
 		}
 	}
 
 	/*
 	 * Retrieves the IVirtualFolder to which Java src folders should be mapped
 	 */
 	private IVirtualFolder getDestinationFolder(final IVirtualComponent c) throws CoreException {
 		final IVirtualFolder root = c.getRootFolder();
 		if (J2EEProjectUtilities.isDynamicWebProject(c.getProject())) {
 			// web projects map to WEB-INF/classes
 			return root.getFolder(new Path(J2EEConstants.WEB_INF_CLASSES));
 		}
 		// all other J2EE project types (that are Java projects) map 
 		// Java src folders to the project root
 		return root;
 	}
 	
 }
