 /**********************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
 * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  **********************************************************************/
 package org.eclipse.wst.server.core.util;
 
 import java.util.*;
 
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.internal.ModuleFactory;
 import org.eclipse.wst.server.core.internal.ServerPlugin;
 import org.eclipse.wst.server.core.internal.Trace;
 import org.eclipse.wst.server.core.model.ModuleFactoryDelegate;
 /**
  * 
  * 
  * @plannedfor 1.0
  */
 public abstract class ProjectModuleFactoryDelegate extends ModuleFactoryDelegate {
 	protected static IResourceChangeListener listener;
 	
 	protected static List factories = new ArrayList();
 
 	protected List added;
 	protected List removed;
 
 	// map from IProject to IModule[]
 	protected final Map projects = new HashMap();
 	protected boolean initialized = false;
 
 	/**
 	 * Construct a new ProjectModuleFactoryDelegate.
 	 */
 	public ProjectModuleFactoryDelegate() {
 		super();
 		
 		factories.add(this);
 		
 		//addListener();
 	}
 
 	/**
 	 * Cache any preexisting module.
 	 * TODO: When/where is this called?
 	 */
 	protected void cacheModules() {
 		cacheModules(true);
 	}
 
 	/**
 	 * Cache any preexisting module.
 	 * TODO: When/where is this called?
 	 */
 	protected void cacheModules(boolean forceUpdate) { 
 		try {
 			IProject[] projects2 = getWorkspaceRoot().getProjects();
 			int size = projects2.length;
 			for (int i = 0; i < size; i++) {
 				//Trace.trace("caching: " + this + " " + projects[i] + " " + isValidModule(projects[i]));
 				if(!projects2[i].isAccessible())
 					removeModules(projects2[i]);
 				else if (forceUpdate || needsUpdating(projects2[i]) && (isValidModule(projects2[i]))) {
 					addModules(projects2[i]);
 				} 
 			}
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Error caching modules", e);
 		} finally {
 			initialized = true;
 		}
 	}
 
 	protected boolean needsUpdating(IProject project) {
 		return true;
 	}
 
 	/**
 	 * Return the workspace root.
 	 * 
 	 * @return the workspace root
 	 */
 	private static IWorkspaceRoot getWorkspaceRoot() {
 		return ResourcesPlugin.getWorkspace().getRoot();
 	}
 
 	/**
 	 * Returns the modules for the given project, or null
 	 * if this factory does not have a module for the given project.
 	 * 
 	 * @param project a project
 	 * @return an array of modules.
 	 */
 	public IModule[] getModules(IProject project) {
 		try {
 			return (IModule[]) projects.get(project);
 		} catch (Exception e) {
 			// ignore
 		}
 		return null;
 	}
 
 	/**
 	 * Add a resource listener to the workspace.
 	 */
	/*protected static void addListener() {
		if (listener != null)
 			return;
 
 		listener = new IResourceChangeListener() {
 			public void resourceChanged(IResourceChangeEvent event) {
 				Trace.trace(Trace.FINEST, "->- ProjectModuleFactoryDelegate listener responding to resource change: " + event.getType() + " ->-");
 				try {
 					IResourceDelta delta = event.getDelta();
 					
 					if (!ResourceManager.deltaContainsChangedFiles(delta))
 						return;
 					
 					delta.accept(new IResourceDeltaVisitor() {
 						public boolean visit(IResourceDelta visitorDelta) {
 							IResource resource = visitorDelta.getResource();
 							// Trace.trace(Trace.FINEST, "resource: " + resource);
 							
 							// only respond to changes within projects
 							if (resource != null && resource instanceof IProject) {
 								IProject project = (IProject) resource;
 								handleGlobalProjectChange(project, visitorDelta);
 								return false;
 							} else if (resource != null && resource.getProject() != null) {
 								return false;
 							} else
 								return true;
 						}
 					});
 				} catch (Exception e) {
 					//Trace.trace(Trace.SEVERE, "Error responding to resource change", e);
 				}
 				fireGlobalEvents();
 				Trace.trace(Trace.FINEST, "-<- Done ProjectModuleFactoryDelegate responding to resource change -<-");
 			}
 		};
 		
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
	}*/
 
 	/**
 	 * Handle changes to a project.
 	 * 
 	 * @param project a project
 	 * @param delta a resource delta
 	 */
 	public static void handleGlobalProjectChange(final IProject project, IResourceDelta delta) {
 		if (!deltaContainsChangedModules(delta))
 			return;
 		
 		//	 clear cache
 		ModuleFactory[] factories2 = ServerPlugin.getModuleFactories();
 		int size = factories2.length;
 		for (int i = 0; i < size; i++)
 			factories2[i].clearModuleCache();
 		
 		
 		Trace.trace(Trace.FINEST, "Firing global project change");
 		// handle project level changes
 		Iterator iterator = factories.iterator();
 		while (iterator.hasNext()) {
 			ProjectModuleFactoryDelegate factory = (ProjectModuleFactoryDelegate) iterator.next();
 			Trace.trace(Trace.FINEST, "Firing to: " + factory);
 			factory.handleProjectChange(project, delta);
 		}
 		
 		// handle internal updates
 		// TODO uncomment - this is already being called by the temporary updateProjects() method
 		/*iterator = factories.iterator();
 		while (iterator.hasNext()) {
 			ProjectModuleFactoryDelegate factory = (ProjectModuleFactoryDelegate) iterator.next();
 			Trace.trace(Trace.FINER, "Firing to: " + factory);
 			factory.handleProjectInternalChange(project, delta);
 		}*/
 		
 		fireGlobalEvents();
 	}
 
 	/**
 	 * Fire the accumulated module factory events.
 	 */
 	protected static void fireGlobalEvents() {
 		Trace.trace(Trace.FINEST, "Firing global module event");
 		Iterator iterator = factories.iterator();
 		while (iterator.hasNext()) {
 			ProjectModuleFactoryDelegate factory = (ProjectModuleFactoryDelegate) iterator.next();
 			Trace.trace(Trace.FINEST, "Firing to: " + factory);
 			factory.updateProjects();
 		}
 	}
 
 	/**
 	 * Temporary to make sure that all project modules are updated.
 	 */
 	private void updateProjects() {
 		IModule[] modules2 = getModules();
 		if (modules2 != null) {
 			int size = modules2.length;
 			for (int i = 0; i < size; i++) {
 				if (modules2[i] instanceof ProjectModule)
 					((ProjectModule) modules2[i]).update();
 			}
 		}
 	}
 
 	/**
 	 * Handle changes to a project.
 	 * 
 	 * @param project a project
 	 * @param delta a resource delta
 	 */
 	private void handleProjectChange(final IProject project, IResourceDelta delta) {
 		if(!initialized)
 			cacheModules(false);
 		if (projects.containsKey(project)) {
 			// already a module
 			if (((delta.getKind() &  IResourceDelta.REMOVED) != 0) || !isValidModule(project)) {
 				removeModules(project);
 			}
 		} else {
 			// not a module
 			if (isValidModule(project)) {
 				addModules(project);
 			}
 		}
 	}
 
 	/**
 	 * Handle changes to a project.
 	 * 
 	 * @param project a project
 	 * @param delta a resource delta
 	 */
 	/*private void handleProjectInternalChange(final IProject project, IResourceDelta delta) {
 		final IPath[] paths = getListenerPaths();
 		if (paths != null) {
 			final IModule[] modules = getModules(project);
 			if (modules != null) {
 				for (int i = 0; i < modules.length; i++) {
 					final IModule module = modules[i];
 					if (module != null && module instanceof ProjectModule) {
 						// check for listener paths
 						final int size = paths.length;
 						class Temp {
 							boolean found = false;
 						}
 						final Temp temp = new Temp();
 						try {
 							delta.accept(new IResourceDeltaVisitor() {
 								public boolean visit(IResourceDelta visitorDelta) {
 									if (temp.found)
 										return false;
 									IPath path = visitorDelta.getProjectRelativePath();
 									
 									boolean prefix = false;
 									for (int j = 0; j < size && !temp.found; j++) {
 										if (paths[j].equals(path))
 											temp.found = true;
 										else if (path.isPrefixOf(paths[j]))
 											prefix = true;
 									}
 									if (temp.found) {
 										((ProjectModule) module).update();
 										return false;
 									} else if (prefix)
 										return true;
 									else
 										return false;
 								}
 							});
 						} catch (Exception e) {
 							Trace.trace(Trace.SEVERE, "Error searching for listening paths", e);
 						}
 					}
 				}
 			}
 		}
 	}*/
 
 	/**
 	 * Add a module for the given project.
 	 * 
 	 * @param project a project
 	 */
 	protected void addModules(IProject project) {
 		IModule[] modules = createModules(project);
 		if (modules == null || modules.length == 0)
 			return;
 		projects.put(project, modules);
 		added = new ArrayList(2);
 		added.addAll(Arrays.asList(modules));
 	}
 
 	/**
 	 * Remove the modules that represents the given project.
 	 * 
 	 * @param project a project
 	 */
 	protected void removeModules(IProject project) {
 		try {
 			IModule[] modules = (IModule[]) projects.get(project);
 			
 			projects.remove(project);
 			if (removed == null)
 				removed = new ArrayList(2);
 			if(modules == null)
 				return;
 			removed.addAll(Arrays.asList(modules));
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Error removing module project", e);
 		}
 	}
 
 	/**
 	 * Returns true if the project may contain modules of the correct type.
 	 * This method is used only to improve performance.
 	 * 
 	 * @param project a project
 	 * @return <code>true</code> if the project may contain modules, and
 	 *    <code>false</code> if it definitely does not
 	 */
 	protected abstract boolean isValidModule(IProject project);
 
 	/**
 	 * Creates the modules for a given project.
 	 * 
 	 * @param project a project to create modules for
 	 * @return a possibly empty array of modules
 	 */
 	protected abstract IModule[] createModules(IProject project);
 	
 	/**
 	 * Returns <code>true</code> if at least one file in the delta is changed,
 	 * and <code>false</code> otherwise.
 	 * 
 	 * @param delta a resource delta
 	 * @return <code>true</code> if at least one file in the delta is changed,
 	 *    and <code>false</code> otherwise
 	 */
 	private static boolean deltaContainsChangedModules(IResourceDelta delta) {
 		class Temp {
 			boolean b = false;
 		}
 		final Temp t = new Temp();
 		try {
 			delta.accept(new IResourceDeltaVisitor() {
 				public boolean visit(IResourceDelta delta2) throws CoreException {
 					if (t.b)
 						return false;
 					//Trace.trace(Trace.FINEST, delta2.getResource() + "  " + delta2.getKind() + " " + delta2.getFlags());
 					if (".wtpmodules".equals(delta2.getResource().getName())) {
 						t.b = true;
 						return false;
 					}
 					return true;
 				}
 			});
 		} catch (Exception e) {
 			// ignore
 		}
 		//Trace.trace(Trace.FINEST, "Delta contains change: " + t.b);
 		return t.b;
 	}
 
 	/**
 	 * Returns the list of resources that the module should listen to
 	 * for state changes. The paths should be project relative paths.
 	 * Subclasses can override this method to provide the paths.
 	 *
 	 * @return a possibly empty array of paths
 	 */
 	protected IPath[] getListenerPaths() {
 		return null;
 	}
 }
