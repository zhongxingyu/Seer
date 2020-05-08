 /******************************************************************************* 
  * Copyright (c) 2007 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.kb.internal.scanner;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.jboss.tools.common.model.XJob;
 import org.jboss.tools.common.model.XJob.XRunnable;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.model.plugin.ModelPlugin;
 import org.jboss.tools.common.model.project.ext.AbstractClassPathMonitor;
 import org.jboss.tools.common.model.util.EclipseResourceUtil;
 import org.jboss.tools.jst.web.WebModelPlugin;
 import org.jboss.tools.jst.web.kb.IKbProject;
 import org.jboss.tools.jst.web.kb.KbMessages;
 import org.jboss.tools.jst.web.kb.KbProjectFactory;
 import org.jboss.tools.jst.web.kb.internal.KbProject;
 import org.jboss.tools.jst.web.model.helpers.InnerModelHelper;
 
 /**
  * Monitors class path of project and loads kb components of it.
  *  
  * @author Viacheslav Kabanovich
  */
 public class ClassPathMonitor extends AbstractClassPathMonitor<KbProject> {
 
 	/**
 	 * Creates instance of class path for kb project
 	 * @param project
 	 */
 	public ClassPathMonitor(KbProject project) {
 		this.project = project;
 	}
 	
 	/**
 	 * Initialization of inner model.
 	 */
 	public void init() {
 		model = InnerModelHelper.createXModel(project.getProject());
 		super.init();
 	}
 	
 	public IProject getProjectResource() {
 		return project.getProject();
 	}
 	
 	/**
 	 * Loads kb components from items recently added to class path. 
 	 */
 	public void process() {
 		if(paths == null) {
 			ModelPlugin.getDefault().logError("Failed to process class path in kb builder for project " + project);
 			return;
 		}
 		for (String p: syncProcessedPaths()) {
 			project.pathRemoved(new Path(p));
 		}
 		for (int i = 0; i < paths.size(); i++) {
 			String p = paths.get(i);
 			if(!requestForLoad(p)) continue;
 
 			LibraryScanner scanner = new LibraryScanner();
 
 			String fileName = new File(p).getName();
 			if(EclipseResourceUtil.SYSTEM_JAR_SET.contains(fileName)) continue;
 			String jsname = "lib-" + fileName; //$NON-NLS-1$
 			XModelObject o = model.getByPath("FileSystems").getChildByPath(jsname); //$NON-NLS-1$
 			if(o == null) continue;
 			
 			LoadedDeclarations c = null;
 			try {
 				if(scanner.isLikelyComponentSource(o)) {
 					c = scanner.parse(o, new Path(p), project);
 				}
 			} catch (ScannerException e) {
 				WebModelPlugin.getDefault().logError(e);
 			}
 			if(c == null) {
 				c = new LoadedDeclarations();
 			}
 			if(c != null) {
 				componentsLoaded(c, new Path(p));
 			}
 		}
 		
 		validateProjectDependencies();
 	}
 	
 	public void validateProjectDependencies() {
 		List<KbProject> ps = null;
 		
 		try {
 			ps = getKbProjects(project.getProject());
 		} catch (CoreException e) {
 			WebModelPlugin.getPluginLog().logError(e);
 		}
 		if(ps != null) {
 			Set<KbProject> set = project.getKbProjects();
 			Set<KbProject> removable = new HashSet<KbProject>();
 			removable.addAll(set);
 			removable.removeAll(ps);
 			ps.removeAll(set);
 			for (KbProject p : ps) {
 				project.addKbProject(p);
 			}
 			for (KbProject p : removable) {
 				project.removeKbProject(p);
 			}
 		}
 	}
 
 	public boolean hasToUpdateProjectDependencies() {
 		List<KbProject> ps = null;
 		
 		try {
 			ps = getKbProjects(project.getProject());
 		} catch (CoreException e) {
 			WebModelPlugin.getPluginLog().logError(e);
 		}
 		if(ps != null) {
 			Set<KbProject> set = project.getKbProjects();
 			Set<KbProject> removable = new HashSet<KbProject>();
 			removable.addAll(set);
 			removable.removeAll(ps);
 			ps.removeAll(set);
 			for (KbProject p : ps) {
 				return true;
 			}
 			for (KbProject p : removable) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	void componentsLoaded(LoadedDeclarations c, IPath path) {
 		if(c == null) return;
 		project.registerComponents(c, path);
 	}
 
 	List<KbProject> getKbProjects(IProject project) throws CoreException {
 		List<KbProject> list = new ArrayList<KbProject>();
		if(project.isAccessible() && project.hasNature(JavaCore.NATURE_ID)) {
 			IJavaProject javaProject = JavaCore.create(project);
 			IClasspathEntry[] es = javaProject.getResolvedClasspath(true);
 			for (int i = 0; i < es.length; i++) {
 				if(es[i].getEntryKind() == IClasspathEntry.CPE_PROJECT) {
 					IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(es[i].getPath().lastSegment());
 					if(p == null || !p.isAccessible()) continue;
 					KbProject.checkKBBuilderInstalled(p);
 					IKbProject sp = KbProjectFactory.getKbProject(p, false);
 					if(sp != null) list.add((KbProject)sp);
 				}
 			}
 			
 		}
 		return list;
 	}
 
 	public void pathsChanged(List<String> paths) {
 		super.pathsChanged(paths);
 		if(project.isStorageResolved()) {
 			XJob.addRunnableWithPriority(new XRunnable() {
 				
 				public void run() {
 					if(update()) {
 						System.out.println("Running " + getId());
 						process();
 					}					
 				}
 				
 				public String getId() {
 					return "Update class path of kb project " + project.getProject().getName(); //$NON-NLS-1$
 				}
 			});
 		}
 	}
 }
