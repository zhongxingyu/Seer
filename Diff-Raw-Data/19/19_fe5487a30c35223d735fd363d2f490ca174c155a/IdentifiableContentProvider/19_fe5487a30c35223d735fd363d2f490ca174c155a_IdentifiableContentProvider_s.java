 package org.eclipse.stem.ui.views.explorer;
 
 /*******************************************************************************
  * Copyright (c) 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jface.viewers.AbstractTreeViewer;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.StructuredViewer;
 import org.eclipse.jface.viewers.TreePath;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.stem.core.CorePlugin;
 import org.eclipse.stem.core.common.Identifiable;
 import org.eclipse.stem.core.graph.Graph;
 import org.eclipse.stem.core.model.Decorator;
 import org.eclipse.stem.core.model.Model;
 import org.eclipse.stem.core.scenario.Scenario;
 import org.eclipse.stem.ui.Activator;
 import org.eclipse.stem.ui.Utility;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.navigator.CommonNavigator;
 import org.eclipse.ui.navigator.CommonViewer;
 import org.eclipse.ui.progress.UIJob;
 
 /**
  * This class is a {@link ITreeContentProvider} for
  * {@link IdentifiableInstanceTreeNode}s which appear in the
  * {@link CommonNavigator} framework that is used to explore STEM Projects.
  */
 @SuppressWarnings("unused")
 public class IdentifiableContentProvider implements ITreeContentProvider,
 		IResourceChangeListener, IResourceDeltaVisitor {
 
 	Viewer viewer;
 
 	/**
 	 * Default Constructor
 	 */
 	public IdentifiableContentProvider() {
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(this,
 				IResourceChangeEvent.POST_CHANGE);
 	}
 
 	/**
 	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
 	 */
 	public Object[] getChildren(Object parentElement) {
 		Object[] retValue = null;
 		// Right kind of parent?
 		if (parentElement instanceof IdentifiableTreeNode) {
 			// Yes
 			// if IdentifiableTreeNode
 			final List<Identifiable> temp = new ArrayList<Identifiable>();
 
 			final IdentifiableTreeNode itn = (IdentifiableTreeNode) parentElement;
 
 			final IFolder folder = itn.getProject().getFolder(
 					itn.getFolderName());
 
 			// Does the folder exist?
 			if (folder.exists()) {
 				// Yes
 				try {
 					for (final IResource identifiableFile : folder.members()) {
 						IFile ifile = (IFile) identifiableFile;
 						// exclude any operating system files starting with '.'
 						if((ifile.getName().length()>0)&&( ifile.getName().charAt(0)!='.' ) && !ifile.getName().endsWith("~")) {
 							try {
 								final Identifiable identifiable = Utility.getIdentifiable((IFile) identifiableFile);
 								if(identifiable != null) temp.add(identifiable);
 							} catch (final Throwable t) {
 								// here we want to protect the user and allow non STEM files in each Directory
 								// Just catch any exception and continue
 								Activator.logInformation("Error loading file "+ifile.getName()+" for display", t);
 							}
 
 						}
 						
 					}
 				} catch (final CoreException ce) {
 					Activator.logError("", ce);
 				} // for each IResource folder.members()
 			} // if folder exists
 
 			retValue = temp.toArray();
 
 		} else {
 			retValue = new Object[0];
 		}
 
 		return retValue;
 	} // getChildren
 
 	/**
 	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
 	 */
 	public Object getParent(Object element) {
 		Object retValue = null;
 		if (element instanceof IdentifiableInstanceTreeNode) {
 			retValue = ((IdentifiableInstanceTreeNode) element).getParent();
 		} // if IdentifiableInstanceTreeNode
 		return retValue;
 	}
 
 	/**
 	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
 	 */
 	public boolean hasChildren(Object element) {
 		Object[] children = getChildren(element);
 		return children != null && children.length > 0;
 	}
 
 	/**
 	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
 	 */
 	public Object[] getElements(Object inputElement) {
 		return null;
 	}
 
 	/**
 	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
 	 */
 	public void dispose() {
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
 	}
 
 	/**
 	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
 	 *      java.lang.Object, java.lang.Object)
 	 */
 	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		this.viewer = viewer;
 	}
 
 	/**
 	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org
 	 *      .eclipse.core.resources.IResourceChangeEvent)
 	 */
 	public void resourceChanged(final IResourceChangeEvent event) {
 		try {
 			event.getDelta().accept(this);
 		} catch (final CoreException e) {
 			Activator.logError("", e);
 		}
 	} // resourceChanged
 
 	/**
 	 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core
 	 *      .resources.IResourceDelta)
 	 */
 	public boolean visit(final IResourceDelta delta) throws CoreException {
 		
 		final IResource source = delta.getResource(); 
 
 	
 		IPath path = delta.getFullPath();
 
 /*		System.out.println("Path: "+path);
 		if((delta.getKind()&IResourceDelta.ADDED) == IResourceDelta.ADDED) System.out.println("ADDED");
 		if((delta.getKind()&IResourceDelta.ADDED_PHANTOM) == IResourceDelta.ADDED_PHANTOM) System.out.println("ADDED_PHANTOM");
 		if((delta.getKind()&IResourceDelta.ALL_WITH_PHANTOMS) == IResourceDelta.ALL_WITH_PHANTOMS) System.out.println("ALL_WITH_PHANTOMS");
 		if((delta.getKind()&IResourceDelta.CHANGED) == IResourceDelta.CHANGED) System.out.println("CHANGED");
 		if((delta.getKind()&IResourceDelta.CONTENT) == IResourceDelta.CONTENT) System.out.println("CONTENT");
 		if((delta.getKind()&IResourceDelta.COPIED_FROM) == IResourceDelta.COPIED_FROM) System.out.println("COPIED_FROM");
 		if((delta.getKind()&IResourceDelta.DESCRIPTION) == IResourceDelta.DESCRIPTION) System.out.println("DESCRIPTION");
 		if((delta.getKind()&IResourceDelta.ENCODING) == IResourceDelta.ENCODING) System.out.println("ENCODING");
 		if((delta.getKind()&IResourceDelta.LOCAL_CHANGED) == IResourceDelta.LOCAL_CHANGED) System.out.println("LOCAL_CHANGED");
 		if((delta.getKind()&IResourceDelta.MARKERS) == IResourceDelta.MARKERS) System.out.println("MARKERS");
 		if((delta.getKind()&IResourceDelta.MOVED_FROM) == IResourceDelta.MOVED_FROM) System.out.println("MOVED_FROM");
 		if((delta.getKind()&IResourceDelta.MOVED_TO) == IResourceDelta.MOVED_TO) System.out.println("MOVED_TO");
 		//if((delta.getKind()&IResourceDelta.NO_CHANGE) == IResourceDelta.NO_CHANGE) System.out.println("NO_CHANGE");
 		if((delta.getKind()&IResourceDelta.OPEN) == IResourceDelta.OPEN) System.out.println("OPEN");
 		if((delta.getKind()&IResourceDelta.REMOVED) == IResourceDelta.REMOVED) System.out.println("REMOVED");
 		if((delta.getKind()&IResourceDelta.REMOVED_PHANTOM) == IResourceDelta.REMOVED_PHANTOM) System.out.println("REMOVED_PHANTOM");
 		if((delta.getKind()&IResourceDelta.REPLACED) == IResourceDelta.REPLACED) System.out.println("REPLACED");
 		if((delta.getKind()&IResourceDelta.REMOVED_PHANTOM) == IResourceDelta.REMOVED_PHANTOM) System.out.println("REMOVED_PHANTOM");
 		if((delta.getKind()&IResourceDelta.REPLACED) == IResourceDelta.REPLACED) System.out.println("REPLACED");
 		if((delta.getKind()&IResourceDelta.SYNC) == IResourceDelta.SYNC) System.out.println("SYNC");
 		if((delta.getKind()&IResourceDelta.TYPE) == IResourceDelta.TYPE) System.out.println("TYPE");
 */	
 				
 		
 		URI uri = URI.createPlatformResourceURI(path.toString(), false);
 		
 		for(Resource r: org.eclipse.stem.core.Utility.resourceSet.getResources()) 
 			if(r.getURI().equals(uri)) {
 				r.setModified(true);
 				break;
 			}
 		
 		switch ( source.getType() ) { 
 			case IResource.FOLDER: 
 				final IFolder f = (IFolder)source; 
 				new UIJob("Update Folder") { 
 					public IStatus runInUIThread(IProgressMonitor monitor) { 
 						if (viewer != null 
 								&& !viewer.getControl().isDisposed()) { 
 							((StructuredViewer)viewer).refresh(f); 
 						
 						} 
 						return Status.OK_STATUS; 
 					} 
 				}.schedule(); 
 				break;
 		case IResource.ROOT: 
 			new UIJob("Update Root") { 
 				public IStatus runInUIThread(IProgressMonitor monitor) { 
 					if (viewer != null 
 							&& !viewer.getControl().isDisposed()) { 
 						((StructuredViewer)viewer).refresh(source); 
 					} 
 					return Status.OK_STATUS; 
 				} 
 			}.schedule(); 
 			break;
 		case IResource.PROJECT: 
 			final IProject project = (IProject)source; 
 			new UIJob("Update Project") { 
 				public IStatus runInUIThread(IProgressMonitor monitor) { 
 					if (viewer != null 
 							&& !viewer.getControl().isDisposed()) { 
 						((StructuredViewer)viewer).refresh(project); 
 					} 
 					return Status.OK_STATUS; 
 				} 
 			}.schedule();  
 			break;
 		case IResource.FILE: 
 			final IFile file = (IFile) source; 
 			// When files are updated, we need to mark as modified other files that also needs to 
 			// be refreshed since they are indirectly referencing the changed file
 			this.markIndirectFiles(delta);
 			new UIJob("Update File") { 
 				public IStatus runInUIThread(IProgressMonitor monitor) 
 				{ 
 					if (viewer != null 
 							&& !viewer.getControl().isDisposed()) { 
 						((StructuredViewer)viewer).refresh(file); 
 					} 
 					return Status.OK_STATUS; 
 				} 
 			}.schedule(); 
 			return false; 
 		default: 
 			break;
 		}
 		return true; 		
 	} // visit
 
 	/**
 	 * In STEM, models contain other models and graphs, and scenarios contain other models etc. When a resource is changed, we need to see
 	 * what other resources are potentially affected and need to be marked as modified
 	 * 
 	 * @param delta The delta
 	 */
 	
 	private void markIndirectFiles(IResourceDelta delta) {
 		URI uri = URI.createPlatformResourceURI(delta.getResource().getFullPath().toString(), false);
 		String filename = uri.lastSegment();
 		String project = getProject(uri);
 		String type = getType(uri);
 		
 		for(Resource r:org.eclipse.stem.core.Utility.resourceSet.getResources()) {
 			String p_project = getProject(r.getURI());
 			if(!project.equals(p_project)) continue;
 			EList<EObject> content = r.getContents();
 			if(content == null || content.size() == 0) continue;
 			EObject first = content.get(0);
 			URI u = ((Identifiable)first).getURI();
 			if(u == null) continue;
 			String proj = getProject(u);
 			if(proj == null) continue;
 			if(!proj.equals(project)) continue; // wrong project
 
 			if(type.equals("models")) {
				if(!(first instanceof Model) && !(first instanceof Scenario)) continue; // only models and scenarios contain other models				
 				boolean modified = false;
 				if(first instanceof Model)
 					modified = checkModel((Model)first, uri);
 				else if(first instanceof Scenario)
 					modified = checkScenario((Scenario)first, uri);			
 				if(modified) r.setModified(true);						
 			} else if(type.equals("decorators")) {
 				if(!(first instanceof Model) && !(first instanceof Scenario)) continue; // only models and scenarios contain decorators				
 				boolean modified = false;
 				if(first instanceof Model)
 					modified = checkModelDecorators((Model)first, uri);
 				else if(first instanceof Scenario)
 					modified = checkScenarioDecorators((Scenario)first, uri);			
 				if(modified) r.setModified(true);						
 			} else if(type.equals("graphs")) {
 				if(!(first instanceof Model)) continue; // only models  contain graphs		
 				boolean modified = false;
 				modified = checkModelGraphs((Model)first, uri);
 				if(modified) r.setModified(true);	
			}
 		}
 	}
 	
 	private boolean checkModel(Model model, URI modifiedURI) {
 		if(model == null || modifiedURI == null) 
 			CorePlugin.logError("Null model", new Exception());
 		if(model.getURI().lastSegment().equals(modifiedURI.lastSegment())) return true;
 		for(Model m:model.getModels()) {
 			boolean modified = checkModel(m, modifiedURI);
 			if(modified) return true;
 		}
 		return false;
 	}
 	
 	private boolean checkScenario(Scenario scenario, URI modifiedURI) {
 		Model m = scenario.getModel();
 		if(m == null) return false; // no model in scenario
 		return checkModel(m, modifiedURI);
 	}
 	
 	private boolean checkModelDecorators(Model model, URI modifiedURI) {
 		if(model == null || modifiedURI == null) 
 			CorePlugin.logError("Null model or decorator", new Exception());
 		
 		for(Decorator d:model.getNodeDecorators())
 			if(d.getURI().lastSegment().equals(modifiedURI.lastSegment())) return true;
 		
 		// Check sub model
 		for(Model m:model.getModels()) {
 			boolean modified = checkModelDecorators(m, modifiedURI);
 			if(modified) return true;
 		}
 		return false;
 	}
 	
 	private boolean checkScenarioDecorators(Scenario scenario, URI modifiedURI) {
 		Model m = scenario.getModel();
 		boolean modified =  checkModelDecorators(m, modifiedURI);
 		if(modified) return true;
 		// Check scenario decorator
 		for(Decorator d:scenario.getScenarioDecorators()) 
 			if(d.getURI().lastSegment().equals(modifiedURI.lastSegment())) return true;
 		return false;
 	}
 	
 	private boolean checkModelGraphs(Model model, URI modifiedURI) {
 		if(model == null || modifiedURI == null) 
 			CorePlugin.logError("Null model or uri", new Exception());
 		
 		for(Graph g:model.getGraphs())
 			if(g.getURI().lastSegment().equals(modifiedURI.lastSegment())) return true;
 		
 		// Check sub model
 		for(Model m:model.getModels()) {
 			boolean modified = checkModelGraphs(m, modifiedURI);
 			if(modified) return true;
 		}
 		return false;
 	}
 
 	
 	private String getProject(URI uri) {
 		if(!uri.toString().startsWith("platform:/resource")) return null;
 
 		String s1 = uri.toString().substring(19); // "platform:/resource"
 		int is = s1.indexOf("/");
 		String project = s1.substring(0, is); 
 		return project;
 	}
 	
 	private String getType(URI uri) {
 		if(!uri.toString().startsWith("platform:/resource")) return null;
 
 		String s1 = uri.toString().substring(19); // "platform:/resource"
 		int is = s1.indexOf("/");
 		String _project = s1.substring(is+1);
 		int is2 = _project.indexOf("/");
 		if(is2 == -1) return ""; // no type
 		String type = _project.substring(0, is2);
 		return type;
 	}
 } // IdentifiableContentProvider
