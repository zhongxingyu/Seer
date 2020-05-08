 /*******************************************************************************
  * Copyright (c) 2003, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.common.classpath;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IAccessRule;
 import org.eclipse.jdt.core.IClasspathAttribute;
 import org.eclipse.jdt.core.IClasspathContainer;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.internal.core.ClasspathEntry;
 import org.eclipse.jdt.internal.core.JavaProject;
 import org.eclipse.jdt.internal.core.util.Util;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.common.jdt.internal.classpath.ClasspathDecorations;
 import org.eclipse.jst.common.jdt.internal.classpath.ClasspathDecorationsManager;
 import org.eclipse.jst.j2ee.internal.common.J2EECommonMessages;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.builder.DependencyGraphManager;
 import org.eclipse.wst.common.componentcore.internal.resources.VirtualArchiveComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 
 /**
  * This classpath container is based on the Component references; not the manifest entries. Other
  * mechanisms are in place to ensure that the component references are updated when the manifest is
  * updated, and also to make sure the manifest is updated when the component references are updated.
  * 
  */
 public class J2EEComponentClasspathContainer implements IClasspathContainer {
 
 	public static final String CONTAINER_ID = "org.eclipse.jst.j2ee.internal.module.container"; //$NON-NLS-1$
 	public static final IPath CONTAINER_PATH = new Path(CONTAINER_ID);
 
 	private static IPath WEBLIB = new Path("/WEB-INF/lib"); //$NON-NLS-1$
 	
 	private static ClasspathDecorationsManager decorationsManager = new ClasspathDecorationsManager(J2EEPlugin.PLUGIN_ID);
 
 	public static ClasspathDecorationsManager getDecorationsManager() {
         return decorationsManager;
     }
 	
 	private IPath containerPath;
 	private IJavaProject javaProject;
 	private IClasspathEntry[] entries = new IClasspathEntry[0];
 	private static Map keys = new Hashtable();
 	private static Map previousSelves = new Hashtable();
 	
 	private class LastUpdate {
 		private long dotClasspathModificationStamp = -1;
 		private int refCount = 0;
 		private boolean[] isBinary = new boolean[refCount];
 		private IPath[] paths = new IPath[refCount];
 	}
 
 	private LastUpdate lastUpdate = new LastUpdate();
 
 	public J2EEComponentClasspathContainer(IPath path, IJavaProject javaProject) {
 		this.containerPath = path;
 		this.javaProject = javaProject;
 	}
 
 	private boolean requiresUpdate() {
 		IVirtualComponent component = ComponentCore.createComponent(javaProject.getProject());
 		if (component == null) {
 			return false;
 		}
 		
 		IFile dotClasspath = javaProject.getProject().getFile(ProjectUtilities.DOT_CLASSPATH);
 		long dotClasspathModificationStamp = dotClasspath.exists() ? dotClasspath.getModificationStamp() : 0;
 		if(dotClasspathModificationStamp != lastUpdate.dotClasspathModificationStamp){
 			return true;
 		}
 		
 		IVirtualReference[] refs = component.getReferences();
 		IVirtualComponent comp = null;
 
 		// avoid updating the container if references haven't changed
 		if (refs.length == lastUpdate.refCount) {
 			for (int i = 0; i < lastUpdate.refCount; i++) {
 				comp = refs[i].getReferencedComponent();
 				if (comp.isBinary() != lastUpdate.isBinary[i]) {
 					return true;
 				} else {
 					IPath path = null;
 					if (comp.isBinary()) {
 						VirtualArchiveComponent archiveComp = (VirtualArchiveComponent) comp;
 						java.io.File diskFile = archiveComp.getUnderlyingDiskFile();
 						if (diskFile.exists())
 							path = new Path(diskFile.getAbsolutePath());
 						else {
 							IFile iFile = archiveComp.getUnderlyingWorkbenchFile();
 							path = iFile.getFullPath();
 						}
 					} else {
 						path = comp.getProject().getFullPath();
 					}
 					if (!path.equals(lastUpdate.paths[i])) {
 						return true;
 					}
 				}
 			}
 			return false;
 		}
 		return true;
 	}
 	
 	private void update() {
 		if(!javaProject.isOpen()){
 			try {
 				if(javaProject.getProject().exists() && javaProject.getProject().hasNature(JavaCore.NATURE_ID)){
 					javaProject.open(null);
 				} else {
 					return;
 				}
 			} catch (JavaModelException e) {
 				Logger.getLogger().logError(e);
 			} catch (CoreException e) {
 				//ignore 
 				return;
 			}
 		}
 		
 		IVirtualComponent component = ComponentCore.createComponent(javaProject.getProject());
 		Object key = keys.get(new Integer(javaProject.getProject().hashCode()));
 		J2EEComponentClasspathContainer firstPreviousSelf = (J2EEComponentClasspathContainer)previousSelves.get(key);
 		if (component == null) {
 			return;
 		}
 		
 		IFile dotClasspath = javaProject.getProject().getFile(ProjectUtilities.DOT_CLASSPATH);
 		lastUpdate.dotClasspathModificationStamp = dotClasspath.exists() ? dotClasspath.getModificationStamp() : 0;
 		
 		IVirtualComponent comp = null;
 		IVirtualReference ref = null;
 		
 		IVirtualReference[] refs = component.getReferences();
 		List refsList = new ArrayList();
 		Set refedComps = new HashSet();
 		refedComps.add(component);
 		for(int i = 0; i<refs.length;i++){
 			refsList.add(refs[i]);
 			refedComps.add(refs[i].getReferencedComponent());
 		}
 		for(int i=0; i< refsList.size(); i++){
 			comp = ((IVirtualReference)refsList.get(i)).getReferencedComponent();
 			if(comp.isBinary()){
 				IVirtualReference [] binaryRefs = comp.getReferences();
 				for(int j = 0; j<binaryRefs.length; j++){
 					if(!refedComps.contains(binaryRefs[j].getReferencedComponent())){
 						refsList.add(binaryRefs[j]);
 						refedComps.add(binaryRefs[j].getReferencedComponent());
 					}
 				}
 			}
 		}
 		
 		lastUpdate.refCount = refsList.size();
 		lastUpdate.isBinary = new boolean[lastUpdate.refCount];
 		lastUpdate.paths = new IPath[lastUpdate.refCount];
 
 		boolean isWeb = J2EEProjectUtilities.isDynamicWebProject(component.getProject());
 		boolean shouldAdd = true;
 
 		List entriesList = new ArrayList();
 
 		try {
 			IJavaProject javaProject = JavaCore.create(component.getProject());
 			Set existingEntries = new HashSet();
 			try {
 				IClasspathContainer container = JavaCore.getClasspathContainer(CONTAINER_PATH, javaProject);
 				List previousEntries = null;
 				if(null != container){
 					final IClasspathEntry[] containerEntries = container.getClasspathEntries();
 					previousEntries = Arrays.asList(containerEntries);
 				}
 				existingEntries.addAll(Arrays.asList(javaProject.getResolvedClasspath(true)));
 				if(null != previousEntries){
 					existingEntries.removeAll(previousEntries);
 				}
 				if(firstPreviousSelf != null){
 					existingEntries.removeAll(Arrays.asList(firstPreviousSelf.entries));
 				}
 				J2EEComponentClasspathContainer secondPreviousSelf = (J2EEComponentClasspathContainer)previousSelves.get(key);
 				if(firstPreviousSelf != secondPreviousSelf && secondPreviousSelf != null){
 					existingEntries.removeAll(Arrays.asList(secondPreviousSelf.entries));
 				}
 				
 				existingEntries.removeAll(Arrays.asList(entries));
 			
 			} catch (JavaModelException e) {
 				Logger.getLogger().logError(e);
 			}
 			for (int i = 0; i < refsList.size(); i++) {
 				ref = (IVirtualReference)refsList.get(i);
 				comp = ref.getReferencedComponent();
 				lastUpdate.isBinary[i] = comp.isBinary();
 				shouldAdd = !(isWeb && ref.getRuntimePath().equals(WEBLIB));
 				if (!shouldAdd) {
 					continue;
 				}
 				if (comp.isBinary()) {
 					VirtualArchiveComponent archiveComp = (VirtualArchiveComponent) comp;
 					java.io.File diskFile = archiveComp.getUnderlyingDiskFile();
 					if (diskFile.exists()) {
 						lastUpdate.paths[i] = new Path(diskFile.getAbsolutePath());
 					} else {
 						IFile iFile = archiveComp.getUnderlyingWorkbenchFile();
 						lastUpdate.paths[i] = iFile.getFullPath();
 					}
 					if (!isAlreadyOnClasspath(existingEntries, lastUpdate.paths[i])) {
 						ClasspathDecorations dec = decorationsManager.getDecorations( getPath().toString(), lastUpdate.paths[i].toString() );
 						
 						IPath srcpath = null;
 				        IPath srcrootpath = null;
 				        IClasspathAttribute[] attrs = {};
 				        IAccessRule[] access = {};
 						
 				        if( dec != null ) {
 				            srcpath = dec.getSourceAttachmentPath();
 				            srcrootpath = dec.getSourceAttachmentRootPath();
 				            attrs = dec.getExtraAttributes();
 				        }
 			        
 				        entriesList.add(JavaCore.newLibraryEntry( lastUpdate.paths[i], srcpath, srcrootpath, access, attrs, true ));
 					}
 				} else {
 					IProject project = comp.getProject();
 					lastUpdate.paths[i] = project.getFullPath();
 					if (!isAlreadyOnClasspath(existingEntries, lastUpdate.paths[i])) {
 						entriesList.add(JavaCore.newProjectEntry(lastUpdate.paths[i], true));
 					}
 				}
 			}
 		} finally {
 			entries = new IClasspathEntry[entriesList.size()];
 			for (int i = 0; i < entries.length; i++) {
 				entries[i] = (IClasspathEntry) entriesList.get(i);
 			}
 		}
 		previousSelves.put(key, this);
 	}
 
 	public static void install(IPath containerPath, IJavaProject javaProject) {
 		try{
 			J2EEComponentClasspathUpdater.getInstance().pauseUpdates();
 			Integer hashCode = new Integer(javaProject.getProject().hashCode());
 			Object key = keys.get(hashCode);
 			if(key == null){
 				keys.put(hashCode, hashCode);
 				key = hashCode;
 			}
 			final IJavaProject[] projects = new IJavaProject[]{javaProject};
 			final J2EEComponentClasspathContainer container = new J2EEComponentClasspathContainer(containerPath, javaProject);
 			container.update();
 			final IClasspathContainer[] conts = new IClasspathContainer[]{container};
 			try {
 				JavaCore.setClasspathContainer(containerPath, projects, conts, null);
 				previousSelves.put(key, container);
 			} catch (JavaModelException e) {
 				Logger.getLogger().log(e);
 			}
 		} finally {
 			J2EEComponentClasspathUpdater.getInstance().resumeUpdates();
 		}
 	}
 
 	public void refresh(boolean force){
 		if(force || requiresUpdate()){
 			install(containerPath, javaProject);
			if (J2EEComponentClasspathUpdater.shouldUpdateDependencyGraph())
			{
				// Update dependency graph
				DependencyGraphManager.getInstance().forceRefresh();
			}
 		}
 	}
 	
 	public void refresh() {
 		refresh(false);
 	}
 
 	private boolean isUpdating = false;
 	
 	public IClasspathEntry[] getClasspathEntries() {
 		if(!isUpdating){
 			if(this != J2EEComponentClasspathContainerUtils.getInstalledEARLibrariesContainer(javaProject.getProject())){
 				try {
 					isUpdating = true;
 					update();
 				} finally{
 					isUpdating = false;
 				}
 			}
 		}
 		return entries;
 	}
 
 	public String getDescription() {
 		return J2EECommonMessages.J2EE_MODULE_CLASSPATH_CONTAINER_NAME;
 	}
 
 	public int getKind() {
 		return K_APPLICATION;
 	}
 
 	public IPath getPath() {
 		return containerPath;
 	}
 
 	/**
 	 * Taken from {@link JavaProject#isOnClasspath(org.eclipse.core.resources.IResource)}
 	 * 
 	 * @param classpath
 	 * @param newPath
 	 * @return
 	 */
 	private static boolean isAlreadyOnClasspath(Set classpath, IPath newPath) {
 		for (Iterator itr = classpath.iterator(); itr.hasNext();) {
 			IClasspathEntry entry = (IClasspathEntry) itr.next();
 			IPath entryPath = entry.getPath();
 			if (entryPath.equals(newPath)) { // package fragment roots must match exactly entry
 				// pathes (no exclusion there)
 				return true;
 			}
 			if (entryPath.isPrefixOf(newPath) && !Util.isExcluded(newPath, ((ClasspathEntry) entry).fullInclusionPatternChars(), ((ClasspathEntry) entry).fullExclusionPatternChars(), false)) {
 				return true;
 			}
 		}
 		return false;
 	}
 }
