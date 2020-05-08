 /* Copyright (c) 2007 Bug Labs, Inc.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package com.buglabs.osgi.concierge.core;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IClasspathContainer;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 
 import com.buglabs.osgi.concierge.core.utils.ManifestUtils;
 import com.buglabs.osgi.concierge.core.utils.ProjectUtils;
 import com.buglabs.osgi.concierge.jdt.OSGiBundleClassPathContainer;
 import com.buglabs.osgi.concierge.jdt.OSGiBundleClassPathContainerInitializer;
 
 /*
  * @author Angel Roman - roman@mdesystems.com
  */
 public class BundleModelManager implements IResourceChangeListener {
 	private static BundleModelManager bmm;
 	private Vector projects;
 	private Map projectExports;
 	private Map projectImports;
 
 	private BundleModelManager() {
 		projectExports = new HashMap();
 		projectImports = new HashMap();
 
 		projects = new Vector();
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
 	}
 
 	public synchronized static BundleModelManager getInstance() {
 		if(bmm == null) {
 			bmm = new BundleModelManager();
 		}
 
 		return bmm;
 	}
 
 	public synchronized void addProject(IProject project) throws CoreException {
 		if(!projects.contains(project)) {
 			projects.add(project);	
 			IFile manifest = ProjectUtils.getManifestFile(project);
 			if(manifest != null) {
 				if(manifest.exists()) {
 					try {
 						updateProjectPackages(project);
 					} catch (IOException e) {
						projects.remove(project);
 					}
 				}
 			}
 		}
 	}
 
 	public synchronized List getProjectImports(IProject project) {
 		Vector imports = new Vector();
 
 		if(projects.contains(project)) {
 			List importsFromProject = (List) projectImports.get(project);
 			if(importsFromProject != null) {
 				imports.addAll((List) projectImports.get(project));
 			}
 		}
 
 		return imports;
 	}
 
 	public synchronized List getProjectExports(IProject project) {
 		Vector exports = new Vector();
 
 		if(projects.contains(project)) {
 			List projExp = (List) projectExports.get(project);
 			if(projExp != null) {
 				exports.addAll(projExp);
 			}
 		}
 
 		return exports;
 	}
 
 	private void updateProjectPackages(IProject project) throws IOException, CoreException {
 		IFile manifest = ProjectUtils.getManifestFile(project);
 
 		if(manifest != null) {
 			if(manifest.exists()) {
 				List importedPackages = ManifestUtils.getImportedPackages(manifest.getContents());
 				List exportedPackages = ManifestUtils.getExportedPackages(manifest.getContents());
 
 				projectImports.put(project, importedPackages);
 				projectExports.put(project, exportedPackages);
 			}
 		}
 	}
 
 	public synchronized boolean hasProject(IProject proj) {
 		return projects.contains(proj);
 	}
 
 	/**
 	 * 
 	 * @param importStr package to be imported
 	 * @param projInterested proj that is interested in finding a match
 	 * @return a project that is not equal to projInterested and exports 
 	 * 				the importStr. Null if no match is found.
 	 */
 	public synchronized IProject findProjectThatExports(String importStr, IProject projInterested) {
 		Iterator projectIter = projects.iterator();
 
 		String splitImport[] = importStr.split(";");
 		importStr = splitImport[0];
 
 		while(projectIter.hasNext()) {
 			IProject proj = (IProject) projectIter.next();
 
 			if(!proj.equals(projInterested)) {
 
 				Iterator  exportIter = getProjectExports(proj).iterator();
 				while(exportIter.hasNext()) {
 					String exportStr = (String) exportIter.next();
 					String splitExport[] = exportStr.split(";");
 					exportStr = splitExport[0];
 
 					if(exportStr.equals(importStr)) {
 						return proj;
 					}
 				}
 			}
 		}
 
 		return null;
 	}
 
 	public synchronized void resourceChanged(IResourceChangeEvent event) {
 		switch(event.getType()) {
 		case IResourceChangeEvent.POST_CHANGE:
 			//IFile manifest = findManifestInDelta(event.getDelta());
 
 			IResourceDeltaVisitor deltaVisitor = new IResourceDeltaVisitor(){
 
 				public boolean visit(IResourceDelta delta) throws CoreException {
 					IResource res = delta.getResource();
 
 					switch(delta.getKind()) {
 					case IResourceDelta.REMOVED:
 						if(isProjectOfInterest(res)) {
 							removeProject((IProject) res);
 							updateProjectClasspathContainer();
 							return false;
 						} else if(isManifestOfInterest(res)) {
 							resetProjectImportsAndExports((IProject) res.getProject());
 							updateProjectClasspathContainer();
 						}
 						break;
 					case IResourceDelta.ADDED:
 					case IResourceDelta.CHANGED:
 						if(isManifestOfInterest(res)) {
 							//see if this manifest.mf exists in one of our projects
 							if(projects.contains(res.getProject())) {
 								try {
 									updateProjectPackages(res.getProject());
 									updateProjectClasspathContainer();
 								} catch (IOException e) {
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 								}
 							} 
 						}
 						break;
 					}
 					return true;
 				}};
 
 				try {
 					event.getDelta().accept(deltaVisitor);
 				} catch (CoreException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				break;
 		}
 	}
 
 	private boolean isProjectOfInterest(IResource res) {
 		if(res instanceof IProject) {
 			if(projects.contains(res)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private void resetProjectImportsAndExports(IProject project) {
 		projectExports.put(project, new Vector());
 		projectImports.put(project, new Vector());
 	}
 
 	private boolean isManifestOfInterest(IResource res) {
 		if(res instanceof IFile) {
 			if(res.getProjectRelativePath().toString().equals("META-INF/MANIFEST.MF") && projects.contains(res.getProject())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * 
 	 * @return a copy of the projects handle by the BundleModelManager
 	 */
 	public List getProjects() {
 		ArrayList list = new ArrayList();
 		list.addAll(projects);
 		return list;
 	}
 	
 	public synchronized  void removeProject(IProject proj) {
 		projects.remove(proj);
 		projectExports.remove(proj);
 		projectImports.remove(proj);
 	}
 
 	private void updateProjectClasspathContainer() throws JavaModelException {
 		Iterator projectIter = projects.iterator();
 		while(projectIter.hasNext()) {
 			IProject proj = (IProject) projectIter.next();
 			IJavaProject jproj = JavaCore.create(proj);
 
 			IClasspathContainer container = new OSGiBundleClassPathContainer(jproj);
 			JavaCore.setClasspathContainer(new Path(OSGiBundleClassPathContainerInitializer.ID), new IJavaProject[]{jproj}, new IClasspathContainer[]{container}, new NullProgressMonitor());
 		}
 	}
 }
