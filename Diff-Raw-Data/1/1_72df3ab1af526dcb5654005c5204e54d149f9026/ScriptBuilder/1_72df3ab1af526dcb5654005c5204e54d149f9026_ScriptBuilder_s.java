 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.core.builder;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelElementVisitor;
 import org.eclipse.dltk.core.IModelMarker;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.builder.IScriptBuilder;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.SearchEngine;
 import org.eclipse.dltk.internal.core.BuildpathEntry;
 import org.eclipse.dltk.internal.core.BuiltinProjectFragment;
 import org.eclipse.dltk.internal.core.BuiltinSourceModule;
 import org.eclipse.dltk.internal.core.ExternalProjectFragment;
 import org.eclipse.dltk.internal.core.ExternalSourceModule;
 import org.eclipse.dltk.internal.core.ModelManager;
 import org.eclipse.dltk.internal.core.ScriptProject;
 import org.eclipse.dltk.internal.core.util.HandleFactory;
 
 public class ScriptBuilder extends IncrementalProjectBuilder {
 	public static final boolean DEBUG = DLTKCore.DEBUG_SCRIPT_BUILDER;
 	public static final boolean TRACE = DLTKCore.TRACE_SCRIPT_BUILDER;
 
 	public IProject currentProject = null;
 	ScriptProject scriptProject = null;
 	State lastState;
 
 	/**
 	 * Last build following resource count.
 	 */
 	public long lastBuildResources = 0;
 	public long lastBuildSourceFiles = 0;
 
 	static class ResourceVisitor implements IResourceDeltaVisitor,
 			IResourceVisitor {
 		private Set resources;
 		private IProgressMonitor monitor;
 
 		public ResourceVisitor(Set resources, IProgressMonitor monitor) {
 			this.resources = resources;
 			this.monitor = monitor;
 		}
 
 		public boolean visit(IResourceDelta delta) throws CoreException {
 			// monitor.worked(1);
 			if (monitor.isCanceled()) {
 				return false;
 			}
 			IResource resource = delta.getResource();
 			switch (delta.getKind()) {
 			case IResourceDelta.ADDED:
 			case IResourceDelta.CHANGED:
 				if (!this.resources.contains(resource)
 						&& resource.getType() == IResource.FILE) {
 					resources.add(resource);
 					return false;
 				}
 				break;
 			}
 			return true;
 		}
 
 		public boolean visit(IResource resource) {
 			// monitor.worked(1);
 			if (monitor.isCanceled()) {
 				return false;
 			}
 			if (!this.resources.contains(resource)
 					&& resource.getType() == IResource.FILE) {
 				resources.add(resource);
 				return false;
 			}
 			return true;
 		}
 	}
 
 	class ExternalModuleVisitor implements IModelElementVisitor {
 		private Set elements;
 		private IProgressMonitor monitor;
 
 		public ExternalModuleVisitor(Set elements, IProgressMonitor monitor) {
 			this.elements = elements;
 			this.monitor = monitor;
 		}
 
 		/**
 		 * Visit only external source modules, witch we aren't builded yet.
 		 */
 		public boolean visit(IModelElement element) {
 			// monitor.worked(1);
 			if (monitor.isCanceled()) {
 				return false;
 			}
 			if (element.getElementType() == IModelElement.PROJECT_FRAGMENT) {
 				if (!(element instanceof ExternalProjectFragment)
 						&& !(element instanceof BuiltinProjectFragment)) {
 					return false;
 				}
 				IProjectFragment fragment = (IProjectFragment) element;
 
 				if (lastState.externalFolderLocations.contains(fragment
 						.getPath())) {
 					return false;
 				} else {
 					lastState.externalFolderLocations.add(fragment.getPath());
 				}
 			}
 			if (element.getElementType() == IModelElement.SOURCE_MODULE
 					&& (element instanceof ExternalSourceModule || element instanceof BuiltinSourceModule)) {
 				if (!elements.contains(element)) {
 					elements.add(element);
 				}
 				return false; // do not enter into source module content.
 			}
 			return true;
 		}
 	}
 
 	/**
 	 * Hook allowing to initialize some static state before a complete build
 	 * iteration. This hook is invoked during PRE_AUTO_BUILD notification
 	 */
 	public static void buildStarting() {
 		// build is about to start
 	}
 
 	/**
 	 * Hook allowing to reset some static state after a complete build
 	 * iteration. This hook is invoked during POST_AUTO_BUILD notification
 	 */
 	public static void buildFinished() {
 		if (DLTKCore.DEBUG)
 			System.out.println("build finished");
 	}
 
 	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
 			throws CoreException {
 		long start = 0;
 		lastBuildResources = 0;
 		lastBuildSourceFiles = 0;
 		if (TRACE) {
 			start = System.currentTimeMillis();
 		}
 		this.currentProject = getProject();
 
 		if (!DLTKLanguageManager.hasScriptNature(this.currentProject)) {
 			return null;
 		}
 		this.scriptProject = (ScriptProject) DLTKCore.create(currentProject);
 
 		if (currentProject == null || !currentProject.isAccessible())
 			return new IProject[0];
 
 		if (kind == FULL_BUILD) {
 			fullBuild(monitor);
 		} else {
 			if ((this.lastState = getLastState(currentProject, monitor)) == null) {
 				if (DEBUG)
 					System.out
 							.println("Performing full build since last saved state was not found"); //$NON-NLS-1$
 				fullBuild(monitor);
 			} else {
 				IResourceDelta delta = getDelta(getProject());
 				if (delta == null) {
 					fullBuild(monitor);
 				} else {
 					incrementalBuild(delta, monitor);
 				}
 			}
 		}
 		IProject[] requiredProjects = getRequiredProjects(true);
 		if (DEBUG)
 			System.out.println("Finished build of " + currentProject.getName() //$NON-NLS-1$
 					+ " @ " + new Date(System.currentTimeMillis())); //$NON-NLS-1$
 		if (TRACE) {
 			System.out
 					.println("-----SCRIPT-BUILDER-INFORMATION-TRACE----------------------------");
 			System.out
 					.println("Finished build of project:"
 							+ currentProject.getName()
 							+ "\n"
 							+ "Building time:"
 							+ Long.toString(System.currentTimeMillis() - start)
 							+ "\n"
 							+ "Resources count:"
 							+ this.lastBuildResources
 							+ "\n"
 							+ "Sources count:"
 							+ this.lastBuildSourceFiles
 							+ "\n"
 							+ "Build type:"
 							+ (kind == FULL_BUILD ? "Full build"
 									: "Incremental build"));
 			System.out
 					.println("-----------------------------------------------------------------");
 		}
 		return requiredProjects;
 	}
 
 	private IProject[] getRequiredProjects(boolean includeBinaryPrerequisites) {
 		if (scriptProject == null)
 			return new IProject[0];
 		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
 		ArrayList projects = new ArrayList();
 		try {
 			IBuildpathEntry[] entries = scriptProject
 					.getExpandedBuildpath(true);
 			for (int i = 0, l = entries.length; i < l; i++) {
 				IBuildpathEntry entry = entries[i];
 				IPath path = entry.getPath();
 				IProject p = null;
 				switch (entry.getEntryKind()) {
 				case IBuildpathEntry.BPE_PROJECT:
 					p = workspaceRoot.getProject(path.lastSegment()); // missing
 					// projects
 					// are
 					// considered
 					// too
 					if (((BuildpathEntry) entry).isOptional()
 							&& !ScriptProject.hasScriptNature(p)) // except if
 						// entry is
 						// optional
 						p = null;
 					break;
 				case IBuildpathEntry.BPE_LIBRARY:
 					if (includeBinaryPrerequisites && path.segmentCount() > 1) {
 						// some binary resources on the class path can come from
 						// projects that are not included in the project
 						// references
 						IResource resource = workspaceRoot.findMember(path
 								.segment(0));
 						if (resource instanceof IProject)
 							p = (IProject) resource;
 					}
 				}
 				if (p != null && !projects.contains(p))
 					projects.add(p);
 			}
 		} catch (ModelException e) {
 			return new IProject[0];
 		}
 		IProject[] result = new IProject[projects.size()];
 		projects.toArray(result);
 		return result;
 	}
 
 	public State getLastState(IProject project, IProgressMonitor monitor) {
 		return (State) ModelManager.getModelManager().getLastBuiltState(
 				project, monitor);
 	}
 
 	private State clearLastState() {
 		State state = new State(this);
 		State prevState = (State) ModelManager.getModelManager().getLastBuiltState(currentProject, null);
 		if( prevState != null ) {
 			if( prevState.noCleanExternalFolders) {
 				state.externalFolderLocations = prevState.externalFolderLocations;
 				return state;
 			}
 		}
 		ModelManager.getModelManager().setLastBuiltState(currentProject, null);
 		return state;
 	}
 
 	protected void fullBuild(final IProgressMonitor monitor)
 			throws CoreException {
 
 		State newState = clearLastState();
 		lastState = newState;
 		try {
 			monitor.beginTask(MessageFormat.format("Building \"{0}\" project...",
 					new Object[] { currentProject.getName() }), 66);
 			Set resources = getResourcesFrom(currentProject, monitor, 1);
 			Set elements = getExternalElementsFrom(scriptProject, monitor, 1);
 			// Project external resources should also be added into list. Only
 			// on full build we need to manage this.
 			// Call builders for resources.
 			int totalFiles = resources.size() + elements.size();
 			if (totalFiles == 0) totalFiles = 1;
 			int resourceTicks = 64 * resources.size() / totalFiles; 
 			
 			buildResources(resources, monitor, resourceTicks, IScriptBuilder.FULL_BUILD);
 
 			List els = new ArrayList();
 			els.addAll(elements);
 			
 			buildElements(els, monitor, 64 - resourceTicks, IScriptBuilder.FULL_BUILD);
 			lastBuildSourceFiles += elements.size();
 			lastBuildResources = resources.size() + elements.size();
 		} catch (CoreException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		} finally {
 			ModelManager.getModelManager().setLastBuiltState(currentProject,
 					this.lastState);
 		}
 	}
 
 	private Set getResourcesFrom(Object el, final IProgressMonitor monitor,
 			int ticks) throws CoreException {
 		Set resources = new HashSet();
 		String name = MessageFormat.format("Scanning \"{0}\" project...",
 				new Object[] { currentProject.getName() });
 		monitor.subTask(name);
 		try {
 			ResourceVisitor resourceVisitor = new ResourceVisitor(resources,
 					monitor);
 			if (el instanceof IProject) {
 				IProject prj = (IProject) el;
 				prj.accept(resourceVisitor);
 			} else if (el instanceof IResourceDelta) {
 				IResourceDelta delta = (IResourceDelta) el;
 				delta.accept(resourceVisitor);
 			}
 			return resources;
 		} finally {
 			monitor.worked(ticks);
 		}
 	}
 
 	private Set getExternalElementsFrom(ScriptProject prj,
 			final IProgressMonitor monitor, int tiks) throws ModelException {
 		Set elements = new HashSet();
 		String name = MessageFormat.format(
 				"Scanning external resources for \"{0}\" project...",
 				new Object[] { currentProject.getName() });
 		monitor.subTask(name);
 		try {
 			prj.accept(new ExternalModuleVisitor(elements, monitor));
 			return elements;
 		} finally {
 			monitor.worked(tiks);
 		}
 	}
 
 	protected void incrementalBuild(IResourceDelta delta,
 			IProgressMonitor monitor) throws CoreException {
 		State newState = new State(this);
 		if (this.lastState != null) {
 			newState.copyFrom(this.lastState);
 		}
 		this.lastState = newState;
 		try {
 			monitor.beginTask("Incremental building", 100);
 
 			Set resources = getResourcesFrom(delta, monitor, 5);
 
 			// Call builders for resources.
 			Set actualResourcesToBuild = findDependencies(resources);
 			monitor.done();
 
 			buildResources(actualResourcesToBuild, monitor, 60,
 					IScriptBuilder.INCREMENTAL_BUILD);
 			//
 			Set elements = getExternalElementsFrom(scriptProject, monitor, 5);
 			List els = new ArrayList();
 			els.addAll(elements);
 			buildElements(els, monitor, 30, IScriptBuilder.INCREMENTAL_BUILD);
 			lastBuildResources = resources.size() + elements.size();
 		} finally {
 			ModelManager.getModelManager().setLastBuiltState(currentProject,
 					this.lastState);
 		}
 	}
 
 	private HandleFactory factory = new HandleFactory();
 
 	protected void buildResources(Set resources, IProgressMonitor monitor,
 			int tiks, int buildType) {
 		List status = new ArrayList();
 		IDLTKSearchScope scope = SearchEngine
 				.createSearchScope(new IModelElement[] { scriptProject });
 
 		List realResources = new ArrayList(); // real resources
 		List elements = new ArrayList(); // Model elements
 		String name = "Locate Elements for "
 				+ this.scriptProject.getElementName();
 		IProgressMonitor sub = new SubProgressMonitor(monitor, tiks / 3);
 		// sub.subTask(name);
 		sub.beginTask(name, resources.size());
 		for (Iterator iterator = resources.iterator(); iterator.hasNext();) {
 			IResource res = (IResource) iterator.next();
 
 			sub.worked(1);
 			if (sub.isCanceled()) {
 				return;
 			}
 			IModelElement element = factory.createOpenable(res.getFullPath()
 					.toString(), scope);
 			if (element != null
 					&& element.getElementType() == IModelElement.SOURCE_MODULE
 					&& element.exists()) {
 				elements.add(element);
 			} else {
 				realResources.add(res);
 			}
 		}
 		sub.done();
 		lastBuildSourceFiles += elements.size();
 		// Else build as resource.
 		String[] natureIds = null;
 		try {
 			natureIds = currentProject.getDescription().getNatureIds();
 		} catch (CoreException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 			return;
 		}
 		if (realResources.size() == 0) {
 			monitor.worked(tiks / 3);
 		} else {
 			Set alreadyPassed = new HashSet();
 			for (int j = 0; j < natureIds.length; j++) {
 				try {
 					IScriptBuilder[] builders = ScriptBuilderManager
 							.getScriptBuilders(natureIds[j]);
 					if (builders != null) {
 						for (int k = 0; k < builders.length; k++) {
 							IProgressMonitor ssub = new SubProgressMonitor(
 									monitor,
 									(tiks / 3)
 											/ (builders.length * natureIds.length));
 							ssub.beginTask("Building", 1);
 							if (!alreadyPassed.contains(builders[k])) {
 								alreadyPassed.add(builders[k]);
 								IStatus[] st = builders[k].buildResources(
 										this.scriptProject, realResources,
 										ssub, buildType);
 								if (st != null) {
 									for (int i = 0; i < st.length; i++) {
 										IStatus s = st[i];
 										if (s != null
 												&& s.getSeverity() != IStatus.OK) {
 											status.add(s);
 										}
 									}
 								}
 							}
 							ssub.done();
 						}
 					}
 				} catch (CoreException e) {
 					if (DLTKCore.DEBUG) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 
 		buildElements(elements, monitor, tiks / 3, buildType);
 		// sub.done();
 	}
 
 	protected void buildElements(List elements, IProgressMonitor monitor,
 			int ticks, int buildType) {
 		List status = new ArrayList();
 		IDLTKLanguageToolkit toolkit = null;
 		try {
 			toolkit = DLTKLanguageManager.getLanguageToolkit(scriptProject);
 			IScriptBuilder[] builders = ScriptBuilderManager
 					.getScriptBuilders(toolkit.getNatureId());
 
 			//TODO: replace this stuff with multistatus
 			if (builders != null) {
 				int total = 0;
 				for (int k = 0; k < builders.length; k++) {
 					total += builders[k].estimateElementsToBuild(elements);
 				}
 				
 				for (int k = 0; k < builders.length; k++) {
 					int builderLength = (total > 0) ? ticks * builders[k].estimateElementsToBuild(elements) / total : 1;
 					IProgressMonitor sub = new SubProgressMonitor(monitor, builderLength);
 					IStatus[] st = builders[k].buildModelElements(
 							scriptProject, elements, sub, buildType);
 					if (st != null) {
 						for (int i = 0; i < st.length; i++) {
 							IStatus s = st[i];
 							if (s != null && s.getSeverity() != IStatus.OK) {
 								status.add(s);
 							}
 						}
 					}
 				}
 
 			}
 		} catch (CoreException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 			return;
 		}
 		// TODO: Do something with status.
 	}
 
 	private Set findDependencies(Set resources) {
 		try {
 			IScriptBuilder[] builders = ScriptBuilderManager
 					.getAllScriptBuilders();
 
 			List elementsToCheck = new ArrayList();
 			elementsToCheck.addAll(resources);
 			Set result = new HashSet();
 			result.addAll(resources);
 			while (elementsToCheck.size() > 0) {
 				Set newElementsToCheck = new HashSet();
 				for (int i = 0; i < builders.length; ++i) {
 					List newResources = builders[i].getDependencies(
 							this.scriptProject, elementsToCheck);
 					if (newResources != null) {
 						newElementsToCheck.addAll(newResources);
 					}
 				}
 
 				for (Iterator iterator = newElementsToCheck.iterator(); iterator
 						.hasNext();) {
 					Object o = iterator.next();
 					if (!result.contains(o)) {
 						result.add(o);
 					}
 				}
 				elementsToCheck.clear();
 				elementsToCheck.addAll(newElementsToCheck);
 			}
 			return result;
 		} catch (CoreException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 		return resources;
 	}
 
 	public static void removeProblemsAndTasksFor(IResource resource) {
 		try {
 			if (resource != null && resource.exists()) {
 				resource.deleteMarkers(
 						IModelMarker.SCRIPT_MODEL_PROBLEM_MARKER, false,
 						IResource.DEPTH_INFINITE);
 				resource.deleteMarkers(IModelMarker.TASK_MARKER, false,
 						IResource.DEPTH_INFINITE);
 
 				// delete managed markers
 			}
 		} catch (CoreException e) {
 			// assume there were no problems
 		}
 	}
 
 	public static void writeState(Object state, DataOutputStream out)
 			throws IOException {
 		((State) state).write(out);
 	}
 
 	public static State readState(IProject project, DataInputStream in)
 			throws IOException {
 		State state = State.read(project, in);
 		return state;
 	}
 }
