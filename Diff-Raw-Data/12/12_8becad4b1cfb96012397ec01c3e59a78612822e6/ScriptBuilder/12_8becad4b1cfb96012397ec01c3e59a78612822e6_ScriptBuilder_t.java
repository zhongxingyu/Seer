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
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IMarker;
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
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.dltk.core.DLTKContentTypeManager;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IExternalSourceModule;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelElementVisitor;
 import org.eclipse.dltk.core.IModelMarker;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.IScriptFolder;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.ScriptProjectUtil;
 import org.eclipse.dltk.core.builder.IScriptBuilder;
 import org.eclipse.dltk.core.builder.IScriptBuilderExtension;
 import org.eclipse.dltk.core.environment.EnvironmentManager;
 import org.eclipse.dltk.core.environment.EnvironmentPathUtils;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.internal.core.BuildpathEntry;
 import org.eclipse.dltk.internal.core.BuiltinSourceModule;
 import org.eclipse.dltk.internal.core.ModelManager;
 import org.eclipse.dltk.internal.core.ScriptProject;
 import org.eclipse.osgi.util.NLS;
 
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
 		final Set<IResource> resources = new HashSet<IResource>();
 		private final IProgressMonitor monitor;
 
 		public ResourceVisitor(IProgressMonitor monitor) {
 			this.monitor = monitor;
 		}
 
 		public boolean visit(IResourceDelta delta) throws CoreException {
 			if (monitor.isCanceled()) {
 				return false;
 			}
 			IResource resource = delta.getResource();
 			if (resource.getType() == IResource.FOLDER) {
 				this.monitor
 						.subTask(Messages.ScriptBuilder_scanningProjectFolder
 								+ resource.getProjectRelativePath().toString());
 			}
 			if (resource.getType() == IResource.FILE) {
 				switch (delta.getKind()) {
 				case IResourceDelta.ADDED:
 				case IResourceDelta.CHANGED:
 					resources.add(resource);
 					break;
 				}
 				return false;
 			}
 			return true;
 		}
 
 		public boolean visit(IResource resource) {
 			if (monitor.isCanceled()) {
 				return false;
 			}
 			if (resource.getType() == IResource.FOLDER) {
 				this.monitor
 						.subTask(Messages.ScriptBuilder_scanningProjectFolder
 								+ resource.getProjectRelativePath().toString());
 			}
 			if (resource.getType() == IResource.FILE) {
 				resources.add(resource);
 				return false;
 			}
 			return true;
 		}
 	}
 
 	static class ExternalModuleVisitor implements IModelElementVisitor {
 		final Set<ISourceModule> elements = new HashSet<ISourceModule>();
 		private final IProgressMonitor monitor;
 
 		public ExternalModuleVisitor(IProgressMonitor monitor) {
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
 				if (!(element instanceof IProjectFragment && ((IProjectFragment) element)
 						.isExternal())) {
 					return false;
 				}
 				IProjectFragment fragment = (IProjectFragment) element;
 
 				String localPath = EnvironmentPathUtils.getLocalPath(
 						fragment.getPath()).toString();
 				if (!localPath.startsWith("#")) { //$NON-NLS-1$
 					this.monitor
 							.subTask(Messages.ScriptBuilder_scanningExternalFolder
 									+ localPath);
 				}
 			} else if (element.getElementType() == IModelElement.SOURCE_MODULE) {
 				if (element instanceof IExternalSourceModule
 						|| element instanceof BuiltinSourceModule) {
 					elements.add((ISourceModule) element);
 				}
 				return false; // do not enter into source module content.
 			} else if (element.getElementType() == IModelElement.SCRIPT_FOLDER) {
 				IScriptFolder folder = (IScriptFolder) element;
 				String localPath = EnvironmentPathUtils.getLocalPath(
 						folder.getPath()).toString();
 				if (!localPath.startsWith("#")) { //$NON-NLS-1$
 					this.monitor
 							.subTask(Messages.ScriptBuilder_scanningExternalFolder
 									+ localPath);
 				}
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
 		if (TRACE)
 			System.out.println("build finished"); //$NON-NLS-1$
 	}
 
 	private static void log(String message) {
 		System.out.println(message);
 	}
 
 	private static final QualifiedName PROPERTY_BUILDER_VERSION = new QualifiedName(
 			DLTKCore.PLUGIN_ID, "builderVersion"); //$NON-NLS-1$
 
 	private static final String CURRENT_VERSION = "200810012003-2123"; //$NON-NLS-1$
 
 	@Override
 	protected IProject[] build(int kind,
 			@SuppressWarnings("unchecked") Map args, IProgressMonitor monitor)
 			throws CoreException {
 		this.currentProject = getProject();
 		if (currentProject == null || !currentProject.isAccessible())
 			return new IProject[0];
 		if (!DLTKLanguageManager.hasScriptNature(this.currentProject)) {
 			return null;
 		}
 		long startTime = 0;
 		if (DEBUG || TRACE) {
 			startTime = System.currentTimeMillis();
 			log("\nStarting build of " + this.currentProject.getName() //$NON-NLS-1$
 					+ " @ " + new Date(startTime)); //$NON-NLS-1$
 		}
 		this.scriptProject = (ScriptProject) DLTKCore.create(currentProject);
 		if (!ScriptProjectUtil.isBuilderEnabled(scriptProject)) {
 			if (monitor != null) {
 				monitor.done();
 			}
 			return null;
 		}
 		IEnvironment environment = EnvironmentManager
 				.getEnvironment(scriptProject);
 		if (environment == null || !environment.isConnected()) {
 			// Do not build if environment is not available.
 			// TODO: Store build requests and call builds when connection will
 			// be established.
 			if (monitor != null) {
 				monitor.done();
 			}
 			return null;
 		}
 		final String version = currentProject
 				.getPersistentProperty(PROPERTY_BUILDER_VERSION);
 		if (version == null) {
 			removeWrongTaskMarkers();
 			currentProject.setPersistentProperty(PROPERTY_BUILDER_VERSION,
 					CURRENT_VERSION);
 			kind = FULL_BUILD;
 		} else if (!CURRENT_VERSION.equals(version)) {
 			if ("200810012003".equals(version)) { //$NON-NLS-1$
 				removeWrongTaskMarkers();
 			}
 			currentProject.setPersistentProperty(PROPERTY_BUILDER_VERSION,
 					CURRENT_VERSION);
 			kind = FULL_BUILD;
 		}
 		lastBuildResources = 0;
 		lastBuildSourceFiles = 0;
 		if (monitor == null) {
 			monitor = new NullProgressMonitor();
 		}
 		if (kind == FULL_BUILD) {
 			if (DEBUG)
 				log("Performing full build as requested by user"); //$NON-NLS-1$
 			fullBuild(monitor);
 		} else {
 			if ((this.lastState = getLastState(currentProject, monitor)) == null) {
 				if (DEBUG)
 					log("Performing full build since last saved state was not found"); //$NON-NLS-1$
 				fullBuild(monitor);
 			} else {
 				IResourceDelta delta = getDelta(getProject());
 				if (delta == null) {
 					if (DEBUG)
 						log("Performing full build since deltas are missing after incremental request"); //$NON-NLS-1$
 					fullBuild(monitor);
 				} else {
 					if (DEBUG)
 						log("Performing incremental build"); //$NON-NLS-1$
 					incrementalBuild(delta, monitor);
 				}
 			}
 		}
 		IProject[] requiredProjects = getRequiredProjects(true);
 		long endTime = 0;
 		if (DEBUG || TRACE) {
 			endTime = System.currentTimeMillis();
 		}
 		if (DEBUG) {
 			log("Finished build of " + currentProject.getName() //$NON-NLS-1$
 					+ " @ " + new Date(endTime) + ", elapsed " + (endTime - startTime) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		}
 		if (TRACE) {
 			System.out
 					.println("-----SCRIPT-BUILDER-INFORMATION-TRACE----------------------------"); //$NON-NLS-1$
 			System.out.println("Finished build of project:" //$NON-NLS-1$
 					+ currentProject.getName() + "\n" //$NON-NLS-1$
 					+ "Building time:" //$NON-NLS-1$
 					+ Long.toString(endTime - startTime) + "\n" //$NON-NLS-1$
 					+ "Resources count:" //$NON-NLS-1$
 					+ this.lastBuildResources + "\n" //$NON-NLS-1$
 					+ "Sources count:" //$NON-NLS-1$
 					+ this.lastBuildSourceFiles + "\n" //$NON-NLS-1$
 					+ "Build type:" //$NON-NLS-1$
 					+ (kind == FULL_BUILD ? "Full build" //$NON-NLS-1$
 							: "Incremental build")); //$NON-NLS-1$
 			System.out
 					.println("-----------------------------------------------------------------"); //$NON-NLS-1$
 		}
 		monitor.done();
 		return requiredProjects;
 	}
 
 	/**
 	 * Remove incorrect task markers.
 	 * 
 	 * DLTK 0.95 were creating wrong task markers, so this function is here to
 	 * remove them. New markers will be created by the builder later.
 	 * 
 	 * @param project
 	 * @throws CoreException
 	 */
 	private void removeWrongTaskMarkers() throws CoreException {
 		final IMarker[] markers = currentProject.findMarkers(IMarker.TASK,
 				false, IResource.DEPTH_INFINITE);
 		for (int i = 0; i < markers.length; ++i) {
 			final IMarker marker = markers[i];
 			final IResource resource = marker.getResource();
 			if (resource.getType() != IResource.FILE) {
 				continue;
 			}
 			if (!DLTKContentTypeManager.isValidResourceForContentType(
 					scriptProject.getLanguageToolkit(), resource)) {
 				continue;
 			}
 			final Map<?, ?> attributes = marker.getAttributes();
 			if (attributes == null) {
 				continue;
 			}
 			if (!Boolean.FALSE.equals(attributes.get(IMarker.USER_EDITABLE))) {
 				continue;
 			}
 			if (attributes.containsKey(IMarker.LINE_NUMBER)
 					&& attributes.containsKey(IMarker.MESSAGE)
 					&& attributes.containsKey(IMarker.PRIORITY)
 					&& attributes.containsKey(IMarker.CHAR_START)
 					&& attributes.containsKey(IMarker.CHAR_END)) {
 				marker.delete();
 			}
 		}
 	}
 
 	@Override
 	protected void clean(IProgressMonitor monitor) throws CoreException {
 		long start = 0;
 		if (TRACE) {
 			start = System.currentTimeMillis();
 		}
 
 		this.currentProject = getProject();
 
 		if (!DLTKLanguageManager.hasScriptNature(this.currentProject)) {
 			return;
 		}
 		this.scriptProject = (ScriptProject) DLTKCore.create(currentProject);
 
 		if (currentProject == null || !currentProject.isAccessible())
 			return;
 
 		try {
 			monitor.beginTask(NLS.bind(
 					Messages.ScriptBuilder_cleaningScriptsIn, currentProject
 							.getName()), 66);
 			if (monitor.isCanceled()) {
 				return;
 			}
 
 			IScriptBuilder[] builders = getScriptBuilders();
 
 			if (builders != null) {
 				for (int k = 0; k < builders.length; k++) {
 					IProgressMonitor sub = new SubProgressMonitor(monitor, 1);
 					builders[k].clean(scriptProject, sub);
 
 					if (monitor.isCanceled()) {
 						break;
 					}
 				}
 			}
 			resetBuilders(builders, monitor);
 		} catch (CoreException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 
 		if (TRACE) {
 			System.out
 					.println("-----SCRIPT-BUILDER-INFORMATION-TRACE----------------------------"); //$NON-NLS-1$
 			System.out.println("Finished clean of project:" //$NON-NLS-1$
 					+ currentProject.getName() + "\n" //$NON-NLS-1$
 					+ "Building time:" //$NON-NLS-1$
 					+ Long.toString(System.currentTimeMillis() - start));
 			System.out
 					.println("-----------------------------------------------------------------"); //$NON-NLS-1$
 		}
 		monitor.done();
 	}
 
 	private IProject[] getRequiredProjects(boolean includeBinaryPrerequisites) {
 		if (scriptProject == null)
 			return new IProject[0];
 		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
 		ArrayList<IProject> projects = new ArrayList<IProject>();
 		try {
 			IBuildpathEntry[] entries = scriptProject
 					.getExpandedBuildpath(true);
 			for (int i = 0, l = entries.length; i < l; i++) {
 				IBuildpathEntry entry = entries[i];
 				IPath path = entry.getPath();
 				IProject p = null;
 				switch (entry.getEntryKind()) {
 				case IBuildpathEntry.BPE_PROJECT:
 					p = workspaceRoot.getProject(path.lastSegment());
 					// missing projects are considered too
 					if (((BuildpathEntry) entry).isOptional()
 							&& !ScriptProject.hasScriptNature(p))
 						// except if entry is optional
 						p = null;
 					break;
 				case IBuildpathEntry.BPE_LIBRARY:
 					if (includeBinaryPrerequisites && path.segmentCount() > 1) {
 						/*
 						 * some binary resources on the class path can come from
 						 * projects that are not included in the project
 						 * references
 						 */
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
 		return projects.toArray(new IProject[projects.size()]);
 	}
 
 	public State getLastState(IProject project, IProgressMonitor monitor) {
 		return (State) ModelManager.getModelManager().getLastBuiltState(
 				project, monitor);
 	}
 
 	private State clearLastState() {
 		State state = new State(this);
 		State prevState = (State) ModelManager.getModelManager()
 				.getLastBuiltState(currentProject, null);
 		if (prevState != null) {
 			if (prevState.noCleanExternalFolders) {
 				state.externalFolderLocations = prevState.externalFolderLocations;
 				return state;
 			}
 		}
 		ModelManager.getModelManager().setLastBuiltState(currentProject, null);
 		return state;
 	}
 
 	private static final int WORK_RESOURCES = 50;
 	private static final int WORK_EXTERNAL = 100;
 	private static final int WORK_SOURCES = 100;
 	private static final int WORK_BUILD = 750;
 
 	private static final String NONAME = ""; //$NON-NLS-1$
 
 	protected void fullBuild(final IProgressMonitor monitor) {
 
 		this.lastState = clearLastState();
 		IScriptBuilder[] builders = null;
 		try {
 			monitor.setTaskName(NLS.bind(
 					Messages.ScriptBuilder_buildingScriptsIn, currentProject
 							.getName()));
 			monitor.beginTask(NONAME, WORK_RESOURCES + WORK_EXTERNAL
 					+ WORK_SOURCES + WORK_BUILD);
 			Set<IResource> resources = getResourcesFrom(currentProject,
 					monitor, WORK_RESOURCES);
 			if (monitor.isCanceled()) {
 				return;
 			}
 			Set<ISourceModule> externalElements = getExternalElementsFrom(
 					scriptProject, monitor, WORK_EXTERNAL, true);
 			Set<IPath> externalFolders = new HashSet<IPath>(
 					this.lastState.externalFolderLocations);
 			if (monitor.isCanceled()) {
 				return;
 			}
 			// Project external resources should also be added into list. Only
 			// on full build we need to manage this.
 			// Call builders for resources.
 			int totalFiles = resources.size() + externalElements.size();
 			if (totalFiles == 0)
 				totalFiles = 1;
 
 			builders = getScriptBuilders();
 
 			List<IResource> localResources = new ArrayList<IResource>();
 			List<ISourceModule> localElements = new ArrayList<ISourceModule>();
 			locateSourceModules(monitor, WORK_SOURCES, resources,
 					localElements, localResources);
 
 			if (monitor.isCanceled()) {
 				return;
 			}
 			int resourceTicks = WORK_BUILD
 					* (resources.size() - localElements.size()) / totalFiles;
 			resourceTicks = Math.min(resourceTicks, WORK_BUILD / 4);
 
 			try {
 				final BuilderState[] states = estimateBuild(localElements,
 						externalElements, Collections.<IPath> emptySet(),
 						externalFolders, builders, IScriptBuilder.FULL_BUILD);
 				buildElements(states, monitor, WORK_BUILD - resourceTicks);
 				lastBuildSourceFiles += externalElements.size();
 			} catch (CoreException e) {
 				DLTKCore.error(Messages.ScriptBuilder_errorBuildElements, e);
 			}
 
 			if (monitor.isCanceled()) {
 				return;
 			}
 			buildResources(localResources, monitor, resourceTicks, FULL_BUILD,
 					builders);
 
 			lastBuildResources = resources.size() + externalElements.size();
 		} catch (CoreException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		} finally {
 			resetBuilders(builders, monitor);
 
 			monitor.done();
 			ModelManager.getModelManager().setLastBuiltState(currentProject,
 					this.lastState);
 		}
 	}
 
 	private void resetBuilders(IScriptBuilder[] builders,
 			IProgressMonitor monitor) {
 		if (builders != null) {
 			for (int k = 0; k < builders.length; k++) {
 				builders[k].endBuild(scriptProject, monitor);
 			}
 		}
 	}
 
 	private Set<IResource> getResourcesFrom(Object el,
 			final IProgressMonitor monitor, int ticks) throws CoreException {
 		monitor.subTask(Messages.ScriptBuilder_scanningProject);
 		try {
 			ResourceVisitor resourceVisitor = new ResourceVisitor(monitor);
 			if (el instanceof IProject) {
 				IProject prj = (IProject) el;
 				prj.accept(resourceVisitor);
 			} else if (el instanceof IResourceDelta) {
 				IResourceDelta delta = (IResourceDelta) el;
 				delta.accept(resourceVisitor);
 			}
 			return resourceVisitor.resources;
 		} finally {
 			monitor.worked(ticks);
 		}
 	}
 
 	private Set<ISourceModule> getExternalElementsFrom(ScriptProject prj,
 			final IProgressMonitor monitor, int tiks, boolean updateState)
 			throws ModelException {
 		final String name = Messages.ScriptBuilder_scanningExternalFolders;
 		monitor.subTask(name);
 		final SubProgressMonitor mon = new SubProgressMonitor(monitor, tiks);
 
 		final IProjectFragment[] fragments = prj.getAllProjectFragments();
 		// new external fragments
 		final List<IProjectFragment> extFragments = new ArrayList<IProjectFragment>(
 				fragments.length);
 		// external fragments paths
 		final List<IPath> fragmentPaths = new ArrayList<IPath>(fragments.length);
 		for (int i = 0; i < fragments.length; i++) {
 			final IProjectFragment fragment = fragments[i];
 			if (fragment.isExternal()) {
 				final IPath path = fragment.getPath();
 				if (!updateState
 						|| !this.lastState.externalFolderLocations
 								.contains(path)) {
 					extFragments.add(fragment);
 				} else {
 					fragmentPaths.add(path);
 				}
 			}
 		}
 		// monitor.subTask(name);
 		final ExternalModuleVisitor visitor = new ExternalModuleVisitor(mon);
 		mon.beginTask(name, extFragments.size());
 		for (IProjectFragment fragment : extFragments) {
 			// New project fragment, we need to obtain all modules
 			// from this fragment.
			try {
				fragment.accept(visitor);
				if (updateState) {
					fragmentPaths.add(fragment.getPath());
				}
			} catch (ModelException e) {
				if (!e.isDoesNotExist()) {
					throw e;
				}
 			}
 			mon.worked(1);
 		}
 		mon.done();
 		if (updateState) {
 			this.lastState.externalFolderLocations.clear();
 			this.lastState.externalFolderLocations.addAll(fragmentPaths);
 		}
 
 		return visitor.elements;
 	}
 
 	protected void incrementalBuild(IResourceDelta delta,
 			IProgressMonitor monitor) throws CoreException {
 		State newState = new State(this);
 
 		final Set<IPath> externalFoldersBefore;
 		if (this.lastState != null) {
 			newState.copyFrom(this.lastState);
 			externalFoldersBefore = new HashSet<IPath>(newState
 					.getExternalFolders());
 		} else {
 			externalFoldersBefore = new HashSet<IPath>();
 		}
 
 		this.lastState = newState;
 		IScriptBuilder[] builders = null;
 		try {
 			monitor.setTaskName(NLS.bind(
 					Messages.ScriptBuilder_buildingScriptsIn, currentProject
 							.getName()));
 			monitor.beginTask(NONAME, WORK_RESOURCES + WORK_EXTERNAL
 					+ WORK_SOURCES + WORK_BUILD);
 
 			if (monitor.isCanceled()) {
 				return;
 			}
 			Set<IResource> resources = getResourcesFrom(delta, monitor,
 					WORK_RESOURCES);
 			if (monitor.isCanceled()) {
 				return;
 			}
 			if (DEBUG) {
 				String msg = "Changed resources in delta: " + resources.size(); //$NON-NLS-1$
 				if (!resources.isEmpty() && resources.size() < 10) {
 					msg += ", " + resources.toString(); //$NON-NLS-1$
 				}
 				log(msg);
 			}
 			Set<ISourceModule> externalElements = getExternalElementsFrom(
 					scriptProject, monitor, WORK_EXTERNAL, true);
 			// New external folders set
 			Set<IPath> externalFolders = new HashSet<IPath>(
 					lastState.externalFolderLocations);
 			if (monitor.isCanceled()) {
 				return;
 			}
 
 			int totalFiles = resources.size() + externalElements.size();
 			if (totalFiles == 0)
 				totalFiles = 1;
 
 			builders = getScriptBuilders();
 
 			List<IResource> localResources = new ArrayList<IResource>();
 			List<ISourceModule> localElements = new ArrayList<ISourceModule>();
 			locateSourceModules(monitor, WORK_SOURCES, resources,
 					localElements, localResources);
 
 			if (monitor.isCanceled()) {
 				return;
 			}
 			int resourceTicks = WORK_BUILD
 					* (resources.size() - localElements.size()) / totalFiles;
 
 			if (monitor.isCanceled()) {
 				return;
 			}
 			try {
 				final BuilderState[] states = estimateBuild(localElements,
 						externalElements, externalFoldersBefore,
 						externalFolders, builders,
 						IScriptBuilder.INCREMENTAL_BUILD);
 				buildElements(states, monitor, WORK_BUILD - resourceTicks);
 			} catch (CoreException e) {
 				DLTKCore.error(Messages.ScriptBuilder_errorBuildElements, e);
 			}
 			lastBuildSourceFiles += externalElements.size();
 
 			if (monitor.isCanceled()) {
 				return;
 			}
 			buildResources(localResources, monitor, resourceTicks, FULL_BUILD,
 					builders);
 
 			lastBuildResources = resources.size() + externalElements.size();
 		} finally {
 			resetBuilders(builders, monitor);
 
 			monitor.done();
 			ModelManager.getModelManager().setLastBuiltState(currentProject,
 					this.lastState);
 		}
 	}
 
 	/**
 	 * Return script builders for the current project. ScriptBuilders are
 	 * initialized here so this method should be called only once during build
 	 * operation.
 	 * 
 	 * @return
 	 * @throws CoreException
 	 */
 	private IScriptBuilder[] getScriptBuilders() throws CoreException {
 		IDLTKLanguageToolkit toolkit = DLTKLanguageManager
 				.getLanguageToolkit(scriptProject);
 		if (toolkit != null) {
 			IScriptBuilder[] builders = ScriptBuilderManager
 					.getScriptBuilders(toolkit.getNatureId());
 			if (builders != null) {
 				for (int k = 0; k < builders.length; k++) {
 					builders[k].initialize(scriptProject);
 				}
 			}
 			return builders;
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Splits the <code>resources</code> into <code>realElements</code> and
 	 * <code>realResources</code>
 	 * 
 	 * @param monitor
 	 * @param tiks
 	 * @param resources
 	 * @param realResources
 	 * @return
 	 */
 	protected void locateSourceModules(IProgressMonitor monitor, int tiks,
 			Set<IResource> resources, List<ISourceModule> realElements,
 			List<IResource> realResources) {
 		IProgressMonitor sub = new SubProgressMonitor(monitor, tiks / 3);
 		sub.beginTask(NONAME, resources.size());
 		int remainingWork = resources.size();
 		for (Iterator<IResource> iterator = resources.iterator(); iterator
 				.hasNext();) {
 			if (monitor.isCanceled()) {
 				return;
 			}
 			IResource res = iterator.next();
 			sub.subTask(NLS.bind(
 					Messages.ScriptBuilder_Locating_source_modules, String
 							.valueOf(remainingWork), res.getName()));
 			sub.worked(1);
 			if (sub.isCanceled()) {
 				return;
 			}
 			IModelElement element = DLTKCore.create(res);
 			if (element != null
 					&& element.getElementType() == IModelElement.SOURCE_MODULE
 					&& element.exists()) {
 				realElements.add((ISourceModule) element);
 			} else {
 				realResources.add(res);
 			}
 			--remainingWork;
 		}
 		sub.done();
 		lastBuildSourceFiles += realElements.size();
 	}
 
 	/**
 	 * Build only resources, if some resources are elements they they will be
 	 * returned.
 	 */
 	protected void buildResources(List<IResource> realResources,
 			IProgressMonitor monitor, int tiks, int buildType,
 			IScriptBuilder[] builders) {
 		if (builders == null || builders.length == 0 || realResources.isEmpty()) {
 			monitor.worked(tiks);
 			return;
 		}
 		final IProgressMonitor ssub = new SubProgressMonitor(monitor, tiks);
 		try {
 			ssub.beginTask(Messages.ScriptBuilder_building, builders.length);
 			for (int k = 0; k < builders.length; k++) {
 				builders[k].buildResources(scriptProject, realResources,
 						new SubProgressMonitor(ssub, 1), buildType);
 			}
 		} finally {
 			ssub.done();
 		}
 	}
 
 	private static class BuilderState {
 		final IScriptBuilder builder;
 		int buildType;
 		int workEstimation;
 		List<ISourceModule> elements;
 		List<ISourceModule> externalElements;
 
 		BuilderState(IScriptBuilder builder, int buildType) {
 			this.builder = builder;
 			this.buildType = buildType;
 		}
 	}
 
 	private void buildElements(final BuilderState[] states,
 			IProgressMonitor monitor, int ticks) {
 		int total = 0;
 		for (int k = 0; k < states.length; k++) {
 			total += states[k].workEstimation;
 		}
 
 		// TODO collect statuses from the individual builders?
 		for (int k = 0; k < states.length; k++) {
 			if (monitor.isCanceled()) {
 				return;
 			}
 			final BuilderState state = states[k];
 			final IScriptBuilder builder = state.builder;
 			int builderWork = state.workEstimation * ticks / total;
 			final List<ISourceModule> buildExternalElements = state.externalElements;
 			if (buildExternalElements != null
 					&& buildExternalElements.size() > 0
 					&& builder instanceof IScriptBuilderExtension) {
 				final int step = buildExternalElements.size() * ticks / total;
 				builderWork -= step;
 				monitor.subTask(NLS.bind(
 						Messages.ScriptBuilder_building_N_externalModules,
 						Integer.toString(buildExternalElements.size())));
 				((IScriptBuilderExtension) builder).buildExternalElements(
 						scriptProject, buildExternalElements,
 						new SubProgressMonitor(monitor, step), state.buildType);
 			}
 			final List<ISourceModule> buildElementsList = state.elements;
 			if (buildElementsList.size() > 0) {
 				final int step = buildElementsList.size() * ticks / total;
 				builderWork -= step;
 				monitor.subTask(NLS.bind(
 						Messages.ScriptBuilder_building_N_localModules, Integer
 								.toString(buildElementsList.size())));
 				builder.buildModelElements(scriptProject, buildElementsList,
 						new SubProgressMonitor(monitor, step), state.buildType);
 			}
 			if (builderWork > 0) {
 				monitor.worked(builderWork);
 			}
 		}
 	}
 
 	/**
 	 * Processes input data and fills the <code>workEstimations</code>,
 	 * <code>builderToElements</code> and <code>builderExternalElements</code>
 	 * arrays.
 	 * 
 	 * @param localElements
 	 *            changed source modules
 	 * @param externalElements
 	 *            newly added external source modules
 	 * @param externalFoldersBefore
 	 *            old external fragments
 	 * @param externalFolders
 	 *            new external fragments
 	 * @param builders
 	 *            array of builders
 	 * @param workEstimations
 	 *            arrays of work estimations
 	 * @param builderToElements
 	 *            arrays of elements to be build by each builder
 	 * @param builderExternalElements
 	 *            arrays of external elements to be build by each builder
 	 * @param buildTypes
 	 *            resulting build types for each builder
 	 * @throws CoreException
 	 */
 	private BuilderState[] estimateBuild(List<ISourceModule> localElements,
 			Set<ISourceModule> externalElements,
 			Set<IPath> externalFoldersBefore, Set<IPath> externalFolders,
 			IScriptBuilder[] builders, final int buildType)
 			throws CoreException {
 		if (builders == null) {
 			return new BuilderState[0];
 		}
 		final BuilderState[] states = new BuilderState[builders.length];
 		final HashSet<ISourceModule> elementsAsSet = new HashSet<ISourceModule>(
 				localElements);
 		List<ISourceModule> projectElements = null;
 		List<ISourceModule> projectExternalElements = null;
 		for (int k = 0; k < builders.length; k++) {
 			final IScriptBuilder builder = builders[k];
 			final BuilderState state = new BuilderState(builder, buildType);
 			states[k] = state;
 			final IScriptBuilder.DependencyResponse response = builder
 					.getDependencies(this.scriptProject, state.buildType,
 							elementsAsSet, externalElements,
 							externalFoldersBefore, externalFolders);
 			final List<ISourceModule> buildElementsList;
 			final List<ISourceModule> buildExternalElements;
 			if (response == null) {
 				buildElementsList = localElements;
 				buildExternalElements = null;
 			} else {
 				if (response.isFullLocalBuild()) {
 					if (state.buildType == IScriptBuilder.FULL_BUILD) {
 						buildElementsList = localElements;
 					} else {
 						if (projectElements == null) {
 							projectElements = collectLocalElements();
 						}
 						buildElementsList = projectElements;
 					}
 				} else {
 					@SuppressWarnings("unchecked")
 					final Set<ISourceModule> dependencies = response
 							.getLocalDependencies();
 					if (dependencies != null && !dependencies.isEmpty()
 							&& !elementsAsSet.containsAll(dependencies)) {
 						final Set<ISourceModule> e = new HashSet<ISourceModule>(
 								elementsAsSet.size() + dependencies.size());
 						e.addAll(elementsAsSet);
 						e.addAll(dependencies);
 						buildElementsList = new ArrayList<ISourceModule>(e);
 					} else {
 						buildElementsList = localElements;
 					}
 				}
 				if (response.isFullExternalBuild()) {
 					if (state.buildType == IScriptBuilder.FULL_BUILD) {
 						buildExternalElements = new ArrayList<ISourceModule>(
 								externalElements);
 					} else {
 						if (projectExternalElements == null) {
 							projectExternalElements = new ArrayList<ISourceModule>(
 									getExternalElementsFrom(scriptProject,
 											new NullProgressMonitor(), 1, false));
 						}
 						buildExternalElements = projectExternalElements;
 					}
 				} else {
 					@SuppressWarnings("unchecked")
 					final Set<ISourceModule> dependencies = response
 							.getExternalDependencies();
 					if (dependencies != null && !dependencies.isEmpty()
 							&& !externalElements.containsAll(dependencies)) {
 						final Set<ISourceModule> e = new HashSet<ISourceModule>(
 								externalElements.size() + dependencies.size());
 						e.addAll(externalElements);
 						e.addAll(dependencies);
 						buildExternalElements = new ArrayList<ISourceModule>(e);
 					} else {
 						buildExternalElements = null;
 					}
 				}
 				if (response.isFullLocalBuild()
 						|| response.isFullExternalBuild()) {
 					state.buildType = IScriptBuilder.FULL_BUILD;
 				}
 			}
 			state.elements = buildElementsList;
 			state.externalElements = buildExternalElements;
 			int work = buildElementsList.size();
 			if (buildExternalElements != null) {
 				work += buildExternalElements.size();
 			}
 			state.workEstimation = Math.max(work, 1);
 		}
 		return states;
 	}
 
 	private List<ISourceModule> collectLocalElements() throws CoreException {
 		final IProgressMonitor nullMon = new NullProgressMonitor();
 		final Set<IResource> resources = getResourcesFrom(currentProject,
 				nullMon, 1);
 		final List<ISourceModule> elements = new ArrayList<ISourceModule>();
 		locateSourceModules(nullMon, 1, resources, elements,
 				new ArrayList<IResource>());
 		return elements;
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
