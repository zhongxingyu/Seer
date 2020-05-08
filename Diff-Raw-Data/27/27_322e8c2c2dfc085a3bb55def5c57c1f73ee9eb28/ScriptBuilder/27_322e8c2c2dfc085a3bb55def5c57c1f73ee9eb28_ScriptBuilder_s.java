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
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.dltk.core.DLTKContentTypeManager;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IModelMarker;
 import org.eclipse.dltk.core.IScriptProjectFilenames;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.ScriptProjectUtil;
 import org.eclipse.dltk.core.builder.IBuildChange;
 import org.eclipse.dltk.core.builder.IBuildState;
 import org.eclipse.dltk.core.builder.IProjectChange;
 import org.eclipse.dltk.core.builder.IScriptBuilder;
 import org.eclipse.dltk.core.environment.EnvironmentManager;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.internal.core.BuildpathEntry;
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
 			@SuppressWarnings("rawtypes") Map args, IProgressMonitor monitor)
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
 		if (monitor == null) {
 			monitor = new NullProgressMonitor();
 		}
 		try {
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
 					} else if (isProjectConfigChange(delta)) {
 						if (DEBUG)
 							log("Performing full build since .project/.buildpath change"); //$NON-NLS-1$
 						fullBuild(monitor);
 					} else {
 						if (DEBUG)
 							log("Performing incremental build"); //$NON-NLS-1$
 						incrementalBuild(delta, monitor);
 					}
 				}
 			}
 		} catch (OperationCanceledException e) {
 			// TODO what?
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
 					+ "Build type:" //$NON-NLS-1$
 					+ (kind == FULL_BUILD ? "Full build" //$NON-NLS-1$
 							: "Incremental build")); //$NON-NLS-1$
 			System.out
 					.println("-----------------------------------------------------------------"); //$NON-NLS-1$
 		}
 		monitor.done();
 		return requiredProjects;
 	}
 
 	private static boolean isProjectConfigChange(IResourceDelta projectDelta) {
 		final String[] filenames = {
 				IScriptProjectFilenames.BUILDPATH_FILENAME,
 				IScriptProjectFilenames.PROJECT_FILENAME };
 		for (String filename : filenames) {
 			final IResourceDelta delta = projectDelta.findMember(new Path(
 					filename));
 			if (delta != null) {
 				switch (delta.getKind()) {
 				case IResourceDelta.ADDED:
 				case IResourceDelta.REMOVED:
 					return true;
 				case IResourceDelta.CHANGED:
 					return (delta.getFlags() & (IResourceDelta.CONTENT | IResourceDelta.ENCODING)) != 0;
 				}
 			}
 		}
 		return false;
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
 
 	protected static class BuildStateStub implements IBuildState {
 		public BuildStateStub() {
 		}
 
 		public void recordImportProblem(IPath path) {
 		}
 
 		public void recordDependency(IPath path, IPath dependency) {
 		}
 	}
 
 	private class BuildState implements IBuildState {
 		public BuildState() {
 		}
 
 		public void recordImportProblem(IPath path) {
 			ScriptBuilder.this.lastState.recordImportProblem(path);
 		}
 
 		public void recordDependency(IPath path, IPath dependency) {
 			ScriptBuilder.this.lastState.recordDependency(path, dependency);
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
 					Messages.ScriptBuilder_cleaningScriptsIn,
 					currentProject.getName()), 66);
 			if (monitor.isCanceled()) {
 				return;
 			}
 			ModelManager.getModelManager().setLastBuiltState(currentProject,
 					null);
 
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
 			resetBuilders(builders, new BuildStateStub(), monitor);
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
 
 	static final String NONAME = ""; //$NON-NLS-1$
 
 	protected void fullBuild(final IProgressMonitor monitor) {
 		this.lastState = clearLastState();
 		final IBuildState buildState = new BuildState();
 		IScriptBuilder[] builders = null;
 		try {
 			monitor.setTaskName(NLS.bind(
 					Messages.ScriptBuilder_buildingScriptsIn,
 					currentProject.getName()));
 			monitor.beginTask(NONAME, WORK_RESOURCES + WORK_EXTERNAL
 					+ WORK_SOURCES + WORK_BUILD);
 			builders = getScriptBuilders();
 			if (builders == null || builders.length == 0) {
 				return;
 			}
 			final IBuildChange buildChange = new FullBuildChange(
 					currentProject, monitor);
 			for (IScriptBuilder builder : builders) {
 				if (monitor.isCanceled()) {
 					throw new OperationCanceledException();
 				}
 				builder.prepare(buildChange, buildState, monitor);
 			}
 			for (IScriptBuilder builder : builders) {
 				if (monitor.isCanceled()) {
 					throw new OperationCanceledException();
 				}
 				final long start = TRACE ? System.currentTimeMillis() : 0;
 				builder.build(buildChange, buildState, monitor);
 				if (TRACE) {
 					System.out.println(builder.getClass().getName() + " "
 							+ (System.currentTimeMillis() - start) + "ms");
 				}
 			}
 			updateExternalFolderLocations(buildChange);
 		} catch (CoreException e) {
 			DLTKCore.error(e);
 		} finally {
 			resetBuilders(builders, buildState, monitor);
 			ModelManager.getModelManager().setLastBuiltState(currentProject,
 					this.lastState);
 			monitor.done();
 		}
 	}
 
 	protected void resetBuilders(IScriptBuilder[] builders, IBuildState state,
 			IProgressMonitor monitor) {
 		if (builders != null) {
 			for (int k = 0; k < builders.length; k++) {
 				builders[k].endBuild(scriptProject, state, monitor);
 			}
 		}
 	}
 
 	protected void incrementalBuild(IResourceDelta delta,
 			IProgressMonitor monitor) throws CoreException {
 		State newState = new State(this);
 
 		final Set<IPath> externalFoldersBefore;
 		if (this.lastState != null) {
 			newState.copyFrom(this.lastState);
 			externalFoldersBefore = new HashSet<IPath>(
 					newState.getExternalFolders());
 		} else {
 			externalFoldersBefore = new HashSet<IPath>();
 		}
 
 		this.lastState = newState;
 		final IBuildState buildState = new BuildState();
 		IScriptBuilder[] builders = null;
 		try {
 			monitor.setTaskName(NLS.bind(
 					Messages.ScriptBuilder_buildingScriptsIn,
 					currentProject.getName()));
 			monitor.beginTask(NONAME, WORK_RESOURCES + WORK_EXTERNAL
 					+ WORK_SOURCES + WORK_BUILD);
 			builders = getScriptBuilders();
 			if (builders == null || builders.length == 0) {
 				return;
 			}
 			IBuildChange buildChange = new IncrementalBuildChange(delta,
 					currentProject, monitor, new ArrayList<IPath>(
 							externalFoldersBefore));
 			for (IScriptBuilder builder : builders) {
 				if (monitor.isCanceled()) {
 					throw new OperationCanceledException();
 				}
 				builder.prepare(buildChange, buildState, monitor);
 				if (buildChange.getBuildType() == IScriptBuilder.FULL_BUILD
 						&& buildChange instanceof IncrementalBuildChange) {
 					buildChange = new FullBuildChange(currentProject, monitor);
 					this.lastState.resetDependencies();
 				}
 			}
 			final Set<IPath> processed = new HashSet<IPath>();
 			final Set<IPath> queue = new HashSet<IPath>();
 			if (buildChange instanceof IncrementalBuildChange) {
 				final Set<IPath> changes = ((IncrementalBuildChange) buildChange)
 						.getChangedPaths();
 				if (TRACE) {
 					System.out.println("Changes: " + changes);
 				}
				queue.addAll(this.lastState.dependenciesOf(changes));
 				this.lastState.removeDependenciesFor(changes);
 				processed.addAll(changes);
 				queue.removeAll(processed);
 			}
 			for (IScriptBuilder builder : builders) {
 				if (monitor.isCanceled()) {
 					throw new OperationCanceledException();
 				}
 				final long start = TRACE ? System.currentTimeMillis() : 0;
 				builder.build(buildChange, buildState, monitor);
 				if (TRACE) {
 					System.out.println(builder.getClass().getName() + " "
 							+ (System.currentTimeMillis() - start) + "ms");
 				}
 			}
 			while (!queue.isEmpty()) {
 				if (TRACE) {
 					System.out.println("Queue: " + queue);
 				}
 				final Set<IPath> nextQueue = new HashSet<IPath>();
				nextQueue.addAll(this.lastState.dependenciesOf(queue));
 				this.lastState.removeDependenciesFor(queue);
 				final IWorkspaceRoot root = ResourcesPlugin.getWorkspace()
 						.getRoot();
 				final List<IFile> files = new ArrayList<IFile>();
 				for (IPath path : queue) {
 					files.add(root.getFile(path));
 				}
 				final BuildChange qChange = new BuildChange(currentProject,
 						delta, files, monitor);
 				for (IScriptBuilder builder : builders) {
 					if (monitor.isCanceled()) {
 						throw new OperationCanceledException();
 					}
 					final long start = TRACE ? System.currentTimeMillis() : 0;
 					builder.build(qChange, buildState, monitor);
 					if (TRACE) {
 						System.out.println(builder.getClass().getName() + " "
 								+ (System.currentTimeMillis() - start) + "ms");
 					}
 				}
 				processed.addAll(queue);
 				nextQueue.removeAll(processed);
 				queue.clear();
 				// TODO prevent cycles
 				queue.addAll(nextQueue);
 			}
 			updateExternalFolderLocations(buildChange);
 		} catch (CoreException e) {
 			DLTKCore.error(e);
 		} finally {
 			resetBuilders(builders, buildState, monitor);
 			ModelManager.getModelManager().setLastBuiltState(currentProject,
 					this.lastState);
 			monitor.done();
 		}
 	}
 
 	private void updateExternalFolderLocations(IBuildChange buildChange)
 			throws CoreException {
 		this.lastState.externalFolderLocations.clear();
 		this.lastState.externalFolderLocations.addAll(buildChange
 				.getExternalPaths(IProjectChange.DEFAULT));
 	}
 
 	/**
 	 * Return script builders for the current project. ScriptBuilders are
 	 * initialized here so this method should be called only once during build
 	 * operation.
 	 * 
 	 * @return
 	 * @throws CoreException
 	 */
 	protected IScriptBuilder[] getScriptBuilders() throws CoreException {
 		IDLTKLanguageToolkit toolkit = DLTKLanguageManager
 				.getLanguageToolkit(scriptProject);
 		if (toolkit != null) {
 			IScriptBuilder[] builders = ScriptBuilderManager
 					.getScriptBuilders(toolkit.getNatureId());
 			if (builders != null) {
 				final List<IScriptBuilder> result = new ArrayList<IScriptBuilder>(
 						builders.length);
 				for (int k = 0; k < builders.length; k++) {
 					if (builders[k].initialize(scriptProject)) {
 						result.add(builders[k]);
 					}
 				}
 				builders = result.toArray(new IScriptBuilder[result.size()]);
 			}
 			return builders;
 		} else {
 			return null;
 		}
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
