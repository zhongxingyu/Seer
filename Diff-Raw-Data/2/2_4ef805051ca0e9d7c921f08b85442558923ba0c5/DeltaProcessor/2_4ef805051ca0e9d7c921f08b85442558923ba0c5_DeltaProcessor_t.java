 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
 
  *******************************************************************************/
 package org.eclipse.dltk.internal.core;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.ISafeRunnable;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.PerformanceStats;
 import org.eclipse.core.runtime.SafeRunner;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.ElementChangedEvent;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IElementChangedListener;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelElementDelta;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.IProjectFragmentTimestamp;
 import org.eclipse.dltk.core.IScriptFolder;
 import org.eclipse.dltk.core.IScriptModel;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ISourceElementParser;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.environment.EnvironmentManager;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.dltk.core.search.indexing.IndexManager;
 import org.eclipse.dltk.core.search.indexing.SourceIndexerRequestor;
 import org.eclipse.dltk.internal.core.builder.ScriptBuilder;
 import org.eclipse.dltk.internal.core.search.DLTKWorkspaceScope;
 import org.eclipse.dltk.internal.core.search.ProjectIndexerManager;
 import org.eclipse.dltk.internal.core.util.Util;
 
 /**
  * This class is used by <code>ModelManager</code> to convert
  * <code>IResourceDelta</code>s into <code>IModelElementDelta</code>s. It also
  * does some processing on the <code>ModelElement</code>s involved (e.g. closing
  * them or updating buildpaths).
  */
 public class DeltaProcessor {
 	static class RootInfo {
 		char[][] inclusionPatterns;
 		char[][] exclusionPatterns;
 		IPath rootPath;
 		ScriptProject project;
 		int entryKind;
 		IProjectFragment root;
 
 		RootInfo(ScriptProject project, IPath rootPath,
 				char[][] inclusionPatterns, char[][] exclusionPatterns,
 				int entryKind) {
 			this.project = project;
 			this.rootPath = rootPath;
 			this.inclusionPatterns = inclusionPatterns;
 			this.exclusionPatterns = exclusionPatterns;
 			this.entryKind = entryKind;
 		}
 
 		IProjectFragment getProjectFragment(IResource resource) {
 			if (this.root == null) {
 				if (resource != null) {
 					this.root = this.project.getProjectFragment(resource);
 				} else {
 					Object target = Model
 							.getTarget(
 									ResourcesPlugin.getWorkspace().getRoot(),
 									this.rootPath, false/*
 														 * don't check existence
 														 */);
 					if (target instanceof IResource) {
 						this.root = this.project
 								.getProjectFragment((IResource) target);
 					} else {
 						this.root = this.project
 								.getProjectFragment(this.rootPath.toOSString());
 					}
 				}
 			}
 			return this.root;
 		}
 
 		boolean isRootOfProject(IPath path) {
 			return this.rootPath.equals(path)
 					&& this.project.getProject().getFullPath().isPrefixOf(path);
 		}
 
 		public String toString() {
 			StringBuffer buffer = new StringBuffer("project="); //$NON-NLS-1$
 			if (this.project == null) {
 				buffer.append("null"); //$NON-NLS-1$
 			} else {
 				buffer.append(this.project.getElementName());
 			}
 			buffer.append("\npath="); //$NON-NLS-1$
 			if (this.rootPath == null) {
 				buffer.append("null"); //$NON-NLS-1$
 			} else {
 				buffer.append(this.rootPath.toString());
 			}
 			buffer.append("\nincluding="); //$NON-NLS-1$
 			if (this.inclusionPatterns == null) {
 				buffer.append("null"); //$NON-NLS-1$
 			} else {
 				for (int i = 0, length = this.inclusionPatterns.length; i < length; i++) {
 					buffer.append(new String(this.inclusionPatterns[i]));
 					if (i < length - 1) {
 						buffer.append("|"); //$NON-NLS-1$
 					}
 				}
 			}
 			buffer.append("\nexcluding="); //$NON-NLS-1$
 			if (this.exclusionPatterns == null) {
 				buffer.append("null"); //$NON-NLS-1$
 			} else {
 				for (int i = 0, length = this.exclusionPatterns.length; i < length; i++) {
 					buffer.append(new String(this.exclusionPatterns[i]));
 					if (i < length - 1) {
 						buffer.append("|"); //$NON-NLS-1$
 					}
 				}
 			}
 			return buffer.toString();
 		}
 	}
 
 	private final static int IGNORE = 0;
 	private final static int SOURCE = 1;
 	private final static int BINARY = 2;
 
 	private final static String EXTERNAL_ZIP_ADDED = "external zip added"; //$NON-NLS-1$
 	private final static String EXTERNAL_ZIP_CHANGED = "external zip changed"; //$NON-NLS-1$
 	private final static String EXTERNAL_ZIP_REMOVED = "external zip removed"; //$NON-NLS-1$
 	private final static String EXTERNAL_ZIP_UNCHANGED = "external zip unchanged"; //$NON-NLS-1$
 	private final static String INTERNAL_ZIP_IGNORE = "internal zip ignore"; //$NON-NLS-1$
 	private final static int NON_SCRIPT_RESOURCE = -1;
 	public static boolean DEBUG = false;
 	public static boolean VERBOSE = false;
 	public static boolean PERF = false;
 	public static final int DEFAULT_CHANGE_EVENT = 0; // must not collide with
 	// ElementChangedEvent
 	// event masks
 
 	/*
 	 * Cache SourceElementParser for the project being visited
 	 */
 	private ISourceElementParser sourceElementParserCache;
 
 	private SourceIndexerRequestor sourceRequestorCache;
 
 	/*
 	 * Answer a combination of the lastModified stamp and the size. Used for
 	 * detecting external JAR changes
 	 */
 	public static long getTimeStamp(IFileHandle file) {
 		long lmodif = 0;
 		// if (file instanceof EFSFileHandle) {
 		// lmodif = ((EFSFileHandle) file).lastModified();
 		// } else {
 		lmodif = file.lastModified();
 		// }
 		return lmodif + file.length();
 	}
 
 	/*
 	 * The global state of delta processing.
 	 */
 	private DeltaProcessingState state;
 	/*
 	 * The script model manager
 	 */
 	ModelManager manager;
 	/*
 	 * The <code>ModelElementDelta</code> corresponding to the
 	 * <code>IResourceDelta</code> being translated.
 	 */
 	private ModelElementDelta currentDelta;
 	/*
 	 * The model element that was last created (see createElement(IResource)).
 	 * This is used as a stack of model elements (using getParent() to pop it,
 	 * and using the various get(...) to push it.
 	 */
 	private Openable currentElement;
 	/*
 	 * Queue of deltas created explicily by the script Model that have yet to be
 	 * fired.
 	 */
 	public ArrayList<IModelElementDelta> modelDeltas = new ArrayList<IModelElementDelta>();
 	/*
 	 * Queue of reconcile deltas on working copies that have yet to be fired.
 	 * This is a table form IWorkingCopy to IModelElementDelta
 	 */
 	public HashMap reconcileDeltas = new HashMap();
 	/*
 	 * Turns delta firing on/off. By default it is on.
 	 */
 	private boolean isFiring = true;
 	/*
 	 * Used to update the Model for <code>IModelElementDelta</code>s.
 	 */
 	private final ModelUpdater modelUpdater = new ModelUpdater();
 	/* A set of IDLTKProject whose caches need to be reset */
 	private HashSet<IScriptProject> projectCachesToReset = new HashSet<IScriptProject>();
 	/*
 	 * A list of IModelElement used as a scope for external archives refresh
 	 * during POST_CHANGE. This is null if no refresh is needed.
 	 */
 	private HashSet<IModelElement> refreshedElements;
 
 	/*
 	 * A table from IScriptProject to an array of IProjectFragment. This table
 	 * contains the pkg fragment roots of the project that are being deleted.
 	 */
 	public Map removedRoots;
 	/*
 	 * A table from IDylanProject to an array of IProjectFragment. This table
 	 * contains the pkg fragment roots of the project that are being deleted.
 	 */
 	public Map oldRoots;
 
 	/* A set of IDylanProject whose package fragment roots need to be refreshed */
 	private HashSet<IScriptProject> rootsToRefresh = new HashSet<IScriptProject>();
 	/** {@link Runnable}s that should be called after model is updated */
 	private final ArrayList<Runnable> postActions = new ArrayList<Runnable>();
 	/*
 	 * Type of event that should be processed no matter what the real event type
 	 * is.
 	 */
 	public int overridenEventType = -1;
 
 	// /*
 	// * Map from IProject to BuildpathChange
 	// */
 	// public HashMap buildpathChanges = new HashMap();
 
 	public DeltaProcessor(DeltaProcessingState state, ModelManager manager) {
 		this.state = state;
 		this.manager = manager;
 	}
 
 	/*
 	 * Adds the dependents of the given project to the list of the projects to
 	 * update.
 	 */
 	private void addDependentProjects(IScriptProject project,
 			HashMap projectDependencies, HashSet result) {
 		IScriptProject[] dependents = (IScriptProject[]) projectDependencies
 				.get(project);
 		if (dependents == null) {
 			return;
 		}
 		for (int i = 0, length = dependents.length; i < length; i++) {
 			IScriptProject dependent = dependents[i];
 			if (result.contains(dependent)) {
 				continue; // no need to go further as the project is already
 			}
 			// known
 			result.add(dependent);
 			this.addDependentProjects(dependent, projectDependencies, result);
 		}
 	}
 
 	/*
 	 * Adds the given element to the list of elements used as a scope for
 	 * external jars refresh.
 	 */
 	public void addForRefresh(IModelElement element) {
 		if (this.refreshedElements == null) {
 			this.refreshedElements = new HashSet<IModelElement>();
 		}
 		this.refreshedElements.add(element);
 	}
 
 	/*
 	 * Adds the given child handle to its parent's cache of children.
 	 */
 	private void addToParentInfo(Openable child) {
 		Openable parent = (Openable) child.getParent();
 		if (parent != null && parent.isOpen()) {
 			try {
 				ModelElementInfo info = (ModelElementInfo) parent
 						.getElementInfo();
 				info.addChild(child);
 			} catch (ModelException e) {
 				// do nothing - we already checked if open
 			}
 		}
 	}
 
 	/*
 	 * Adds the given project and its dependents to the list of the roots to
 	 * refresh.
 	 */
 	private void addToRootsToRefreshWithDependents(IScriptProject scriptProject) {
 		this.rootsToRefresh.add(scriptProject);
 		this.addDependentProjects(scriptProject,
 				this.state.projectDependencies, this.rootsToRefresh);
 	}
 
 	/*
 	 * Check all external archive (referenced by given roots, projects or model)
 	 * status and issue a corresponding root delta. Also triggers index updates
 	 */
 	public void checkExternalChanges(IModelElement[] elementsToRefresh,
 			IProgressMonitor monitor) throws ModelException {
 		try {
 			for (int i = 0, length = elementsToRefresh.length; i < length; i++) {
 				this.addForRefresh(elementsToRefresh[i]);
 			}
 			boolean hasDelta = false;
 			if (this.refreshedElements != null) {
 				Set<IModelElement> refreshedElementsCopy = null;
 				if (refreshedElements != null) {
 					refreshedElementsCopy = new HashSet<IModelElement>();
 					refreshedElementsCopy.addAll(refreshedElements);
 					// To avoid concurrent modifications
 					this.refreshedElements = null;
 				}
 				hasDelta = this.createExternalArchiveDelta(null,
 						refreshedElementsCopy);
 				hasDelta |= this.createCustomElementDelta(null,
 						refreshedElementsCopy);
 			} else {
 				return;
 			}
 
 			if (monitor != null && monitor.isCanceled()) {
 				return;
 			}
 			if (hasDelta) {
 				// force buildpath marker refresh of affected projects
 				Model.flushExternalFileCache();
 				// flush zip type cache
 				ModelManager.getModelManager().resetZIPTypeCache();
 				IModelElementDelta[] projectDeltas = this.currentDelta
 						.getAffectedChildren();
 				final int length = projectDeltas.length;
 				final IProject[] projectsToTouch = new IProject[length];
 				for (int i = 0; i < length; i++) {
 					IModelElementDelta delta = projectDeltas[i];
 					ScriptProject scriptProject = (ScriptProject) delta
 							.getElement();
 					projectsToTouch[i] = scriptProject.getProject();
 				}
 				// touch the projects to force them to be recompiled while
 				// taking the workspace lock
 				// so that there is no concurrency with the builder
 				// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=96575
 				IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
 					public void run(IProgressMonitor progressMonitor)
 							throws CoreException {
 						for (int i = 0; i < length; i++) {
 							IProject project = projectsToTouch[i];
 							// touch to force a build of this project
 							if (DLTKCore.DEBUG) {
 								System.out
 										.println("Touching project " + project.getName() + " due to external jar file change"); //$NON-NLS-1$ //$NON-NLS-2$
 							}
 							project.touch(progressMonitor);
 						}
 					}
 				};
 				try {
 					ResourcesPlugin.getWorkspace().run(runnable, monitor);
 				} catch (CoreException e) {
 					throw new ModelException(e);
 				}
 				if (this.currentDelta != null) { // if delta has not been
 					// fired while creating
 					// markers
 					this.fire(this.currentDelta, DEFAULT_CHANGE_EVENT);
 				}
 			}
 		} finally {
 			this.currentDelta = null;
 			if (monitor != null) {
 				monitor.done();
 			}
 		}
 	}
 
 	/*
 	 * Process the given delta and look for projects being added, opened, closed
 	 * or with a script nature being added or removed. Note that projects being
 	 * deleted are checked in deleting(IProject). In all cases, add the
 	 * project's dependents to the list of projects to update so that the
 	 * buildpath related markers can be updated.
 	 */
 	private void checkProjectsBeingAddedOrRemoved(IResourceDelta delta) {
 		IResource resource = delta.getResource();
 		IResourceDelta[] children = null;
 
 		switch (resource.getType()) {
 		case IResource.ROOT:
 			// workaround for bug 15168 circular errors not reported
 			this.state.getOldScriptProjectNames(); // force list to be computed
 			children = delta.getAffectedChildren();
 			break;
 		case IResource.PROJECT:
 			// NB: No need to check project's nature as if the project is not a
 			// script project:
 			// - if the project is added or changed this is a noop for
 			// projectsBeingDeleted
 			// - if the project is closed, it has already lost its script nature
 			IProject project = (IProject) resource;
 			ScriptProject scriptProject = (ScriptProject) DLTKCore
 					.create(project);
 			switch (delta.getKind()) {
 			case IResourceDelta.ADDED:
 				this.manager.batchContainerInitializations = true;
 
 				// remember project and its dependents
 				this.addToRootsToRefreshWithDependents(scriptProject);
 
 				// workaround for bug 15168 circular errors not reported
 				if (DLTKLanguageManager.hasScriptNature(project)) {
 					this.addToParentInfo(scriptProject);
 
 					// ensure project references are updated (see
 					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=121569)
 					try {
 						this.state.updateProjectReferences(scriptProject,
 								null/* no old buildpath */, null/*
 																 * compute new
 																 * resolved
 																 * buildpath
 																 * later
 																 */, null/*
 																		 * read
 																		 * raw
 																		 * buildpath
 																		 * later
 																		 */,
 								false/* cannot change resources */);
 					} catch (ModelException e1) {
 						// project always exists
 					}
 				}
 
 				this.state.rootsAreStale = true;
 				break;
 
 			case IResourceDelta.CHANGED:
 				if ((delta.getFlags() & IResourceDelta.OPEN) != 0) {
 					this.manager.batchContainerInitializations = true;
 
 					// project opened or closed: remember project and its
 					// dependents
 					this.addToRootsToRefreshWithDependents(scriptProject);
 
 					// workaround for bug 15168 circular errors not reported
 					if (project.isOpen()) {
 						if (DLTKLanguageManager.hasScriptNature(project)) {
 							this.addToParentInfo(scriptProject);
 							// readRawBuildpath(scriptProject);
 							// ensure project references are updated
 							this.checkProjectReferenceChange(project,
 									scriptProject);
 						}
 					} else {
 						try {
 							scriptProject.close();
 						} catch (ModelException e) {
 							// script project doesn't exist: ignore
 						}
 						this.removeFromParentInfo(scriptProject);
 						this.manager.removePerProjectInfo(scriptProject);
 						this.manager.containerRemove(scriptProject);
 					}
 					this.state.rootsAreStale = true;
 				} else if ((delta.getFlags() & IResourceDelta.DESCRIPTION) != 0) {
 					boolean wasScriptProject = this.state.findProject(project
 							.getName()) != null;
 					boolean isScriptProject = DLTKLanguageManager
 							.hasScriptNature(project);
 					if (wasScriptProject != isScriptProject) {
 						this.manager.batchContainerInitializations = true;
 
 						// script nature added or removed: remember project and
 						// its dependents
 						this.addToRootsToRefreshWithDependents(scriptProject);
 
 						// workaround for bug 15168 circular errors not reported
 						if (isScriptProject) {
 							this.addToParentInfo(scriptProject);
 							// readRawClasspath(scriptProject);
 							// ensure project references are updated (see
 							// https://bugs.eclipse.org/bugs/show_bug.cgi?id=
 							// 172666)
 							this.checkProjectReferenceChange(project,
 									scriptProject);
 						} else {
 							// remove buildpath cache so that initializeRoots()
 							// will not consider the project has a buildpath
 							this.manager.removePerProjectInfo(scriptProject);
 							// remove container cache for this project
 							this.manager.containerRemove(scriptProject);
 							// close project
 							try {
 								scriptProject.close();
 							} catch (ModelException e) {
 								// script project doesn't exist: ignore
 							}
 							this.removeFromParentInfo(scriptProject);
 						}
 						this.state.rootsAreStale = true;
 					} else {
 						// in case the project was removed then added then
 						// changed (see bug 19799)
 						if (isScriptProject) { // need nature check - 18698
 							this.addToParentInfo(scriptProject);
 							children = delta.getAffectedChildren();
 						}
 					}
 				} else {
 					// workaround for bug 15168 circular errors not reported
 					// in case the project was removed then added then changed
 					if (DLTKLanguageManager.hasScriptNature(project)) { // need
 						// nature
 						// check
 						// -
 						// 18698
 						this.addToParentInfo(scriptProject);
 						children = delta.getAffectedChildren();
 					}
 				}
 				break;
 
 			case IResourceDelta.REMOVED:
 				this.manager.batchContainerInitializations = true;
 
 				// remove buildpath cache so that initializeRoots() will not
 				// consider the project has a buildpath
 				this.manager.removePerProjectInfo(scriptProject);
 				// remove container cache for this project
 				this.manager.containerRemove(scriptProject);
 
 				this.state.rootsAreStale = true;
 				break;
 			}
 
 			// in all cases, refresh the external jars for this project
 			this.addForRefresh(scriptProject);
 
 			break;
 		case IResource.FILE:
 			IFile file = (IFile) resource;
 			/* buildpath file change */
 			if (file.getName().equals(ScriptProject.BUILDPATH_FILENAME)) {
 				this.manager.batchContainerInitializations = true;
 				this.reconcileBuildpathFileUpdate(delta,
 						(ScriptProject) DLTKCore.create(file.getProject()));
 				this.state.rootsAreStale = true;
 			}
 			break;
 		}
 		if (children != null) {
 			for (int i = 0; i < children.length; i++) {
 				this.checkProjectsBeingAddedOrRemoved(children[i]);
 			}
 		}
 	}
 
 	private void checkProjectReferenceChange(IProject project,
 			ScriptProject javaProject) {
 		// BuildpathChange change = (BuildpathChange) this.buildpathChanges
 		// .get(project);
 		this.state.addProjectReferenceChange(javaProject, /* change == null ? */
 		null
 		/* : change.oldResolvedClasspath */);
 	}
 
 	private void reconcileBuildpathFileUpdate(IResourceDelta delta,
 			ScriptProject project) {
 
 		switch (delta.getKind()) {
 		case IResourceDelta.REMOVED: // recreate one based on in-memory
 			// buildpath
 			break;
 		case IResourceDelta.CHANGED:
 			int flags = delta.getFlags();
 			if ((flags & IResourceDelta.CONTENT) == 0 // only consider content
 					// change
 					&& (flags & IResourceDelta.ENCODING) == 0 // and encoding
 					// change
 					&& (flags & IResourceDelta.MOVED_FROM) == 0) {// and also
 				// move and
 				// overide
 				// scenario
 				// (see
 				// http://dev.eclipse.org/bugs/show_bug.cgi?id=21420)
 				break;
 			}
 			// fall through
 		case IResourceDelta.ADDED:
 			try {
 				project.forceBuildpathReload(null);
 			} catch (RuntimeException e) {
 				if (VERBOSE) {
 					e.printStackTrace();
 				}
 			} catch (ModelException e) {
 				if (VERBOSE) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	/*
 	 * Closes the given element, which removes it from the cache of open
 	 * elements.
 	 */
 	private void close(Openable element) {
 		try {
 			element.close();
 		} catch (ModelException e) {
 			// do nothing
 		}
 	}
 
 	/*
 	 * Generic processing for elements with changed contents:<ul> <li>The
 	 * element is closed such that any subsequent accesses will re-open the
 	 * element reflecting its new structure. <li>An entry is made in the delta
 	 * reporting a content change (K_CHANGE with F_CONTENT flag set). </ul>
 	 * Delta argument could be null if processing an external JAR change
 	 */
 	private void contentChanged(Openable element) {
 		boolean isPrimary = false;
 		boolean isPrimaryWorkingCopy = false;
 		if (element.getElementType() == IModelElement.SOURCE_MODULE) {
 			SourceModule cu = (SourceModule) element;
 			isPrimary = cu.isPrimary();
 			isPrimaryWorkingCopy = isPrimary && cu.isWorkingCopy();
 		}
 		if (isPrimaryWorkingCopy) {
 			// filter out changes to primary compilation unit in working copy
 			// mode
 			// just report a change to the resource (see
 			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=59500)
 			this.currentDelta().changed(element,
 					IModelElementDelta.F_PRIMARY_RESOURCE);
 		} else {
 			this.close(element);
 			int flags = IModelElementDelta.F_CONTENT;
 			if (element instanceof ArchiveProjectFragment) {
 				flags |= IModelElementDelta.F_ARCHIVE_CONTENT_CHANGED;
 				// need also to reset project cache otherwise it will be
 				// out-of-date
 				// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=162621
 				this.projectCachesToReset.add(element.getScriptProject());
 			}
 			if (isPrimary) {
 				flags |= IModelElementDelta.F_PRIMARY_RESOURCE;
 			}
 			this.currentDelta().changed(element, flags);
 		}
 	}
 
 	/*
 	 * Creates the openables corresponding to this resource. Returns null if
 	 * none was found.
 	 */
 	private Openable createElement(IResource resource, int elementType,
 			RootInfo rootInfo) {
 		if (resource == null) {
 			return null;
 		}
 
 		IPath path = resource.getFullPath();
 		IModelElement element = null;
 		switch (elementType) {
 		case IModelElement.SCRIPT_PROJECT:
 			// note that non-script resources rooted at the project level will
 			// also enter this code with
 			// an elementType SCRIPT_PROJECT (see #elementType(...)).
 			if (resource instanceof IProject) {
 				this.popUntilPrefixOf(path);
 				if (this.currentElement != null
 						&& this.currentElement.getElementType() == IModelElement.SCRIPT_PROJECT
 						&& ((IScriptProject) this.currentElement).getProject()
 								.equals(resource)) {
 					return this.currentElement;
 				}
 				if (rootInfo != null
 						&& rootInfo.project.getProject().equals(resource)) {
 					element = rootInfo.project;
 					break;
 				}
 				IProject proj = (IProject) resource;
 				if (DLTKLanguageManager.hasScriptNature(proj)) {
 					element = DLTKCore.create(proj);
 				} else {
 					// script project may have been been closed or removed
 					// (look
 					// for
 					// element amongst old script project s list).
 					element = this.state.findProject(proj.getName());
 				}
 			}
 			break;
 		case IModelElement.PROJECT_FRAGMENT:
 			element = rootInfo == null ? DLTKCore.create(resource) : rootInfo
 					.getProjectFragment(resource);
 			break;
 		case IModelElement.SCRIPT_FOLDER:
 			if (rootInfo != null) {
 				if (rootInfo.project.contains(resource)) {
 					IProjectFragment root = (IProjectFragment) rootInfo
 							.getProjectFragment(null);
 					// create package handle
 					IPath pkgPath = path.removeFirstSegments(rootInfo.rootPath
 							.segmentCount());
 					element = root.getScriptFolder(pkgPath);
 				}
 			} else {
 				// find the element that encloses the resource
 				this.popUntilPrefixOf(path);
 				if (this.currentElement == null) {
 					element = DLTKCore.create(resource);
 				} else {
 					// find the root
 					IProjectFragment root = this.currentElement
 							.getProjectFragment();
 					if (root == null) {
 						element = DLTKCore.create(resource);
 					} else if (((ScriptProject) root.getScriptProject())
 							.contains(resource)) {
 						// create package handle
 						IPath pkgPath = path.removeFirstSegments(root.getPath()
 								.segmentCount());
 						element = root.getScriptFolder(pkgPath);
 					}
 				}
 			}
 			break;
 		case IModelElement.SOURCE_MODULE:
 		case IModelElement.BINARY_MODULE:
 			// find the element that encloses the resource
 			this.popUntilPrefixOf(path);
 			if (this.currentElement == null) {
 				element = rootInfo == null ? DLTKCore.create(resource)
 						: ModelManager.create(resource, rootInfo.project);
 			} else {
 				// find the package
 				IScriptFolder pkgFragment = null;
 				switch (this.currentElement.getElementType()) {
 				case IModelElement.PROJECT_FRAGMENT:
 					IProjectFragment root = (IProjectFragment) this.currentElement;
 					IPath rootPath = root.getPath();
 					IPath pkgPath = path.removeLastSegments(1);
 					IPath pkgName = pkgPath.removeFirstSegments(rootPath
 							.segmentCount());
 					pkgFragment = root.getScriptFolder(pkgName);
 					break;
 				case IModelElement.SCRIPT_FOLDER:
 					Openable pkg = this.currentElement;
 					if (pkg.getPath().equals(path.removeLastSegments(1))) {
 						pkgFragment = (IScriptFolder) pkg;
 					} // else case of package x which is a prefix of
 					// x.y
 					break;
 				case IModelElement.SOURCE_MODULE:
 				case IModelElement.BINARY_MODULE:
 					pkgFragment = (IScriptFolder) this.currentElement
 							.getParent();
 					break;
 				}
 				if (pkgFragment == null) {
 					element = rootInfo == null ? DLTKCore.create(resource)
 							: ModelManager.create(resource, rootInfo.project);
 				} else {
 					if (elementType == IModelElement.SOURCE_MODULE) {
 						// create compilation unit handle
 						// fileName validation has been done in
 						// elementType(IResourceDelta, int, boolean)
 						String fileName = path.lastSegment();
 						element = pkgFragment.getSourceModule(fileName);
 					} else {
 						// create binary module handle
 						// fileName validation has been done in
 						// elementType(IResourceDelta, int, boolean)
 						// String fileName = path.lastSegment();
 						throw new RuntimeException("not implemented"); //$NON-NLS-1$
 						// element = pkgFragment.getClassFile(fileName);
 					}
 				}
 			}
 			break;
 		}
 		if (element == null) {
 			return null;
 		}
 		this.currentElement = (Openable) element;
 		return this.currentElement;
 	}
 
 	/*
 	 * Check if external archives have changed and create the corresponding
 	 * deltas. Returns whether at least on delta was created.
 	 */
 	private boolean createExternalArchiveDelta(IProgressMonitor monitor,
 			Set refreshedElements) {
 		if (refreshedElements == null) {
 			return false;
 		}
 		HashMap externalArchivesStatus = new HashMap();
 		boolean hasDelta = false;
 		// find JARs to refresh
 		HashSet archivePathsToRefresh = new HashSet();
 		Iterator iterator = refreshedElements.iterator();
 		// modification exception (see
 		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=63534)
 		while (iterator.hasNext()) {
 			IModelElement element = (IModelElement) iterator.next();
 			switch (element.getElementType()) {
 			case IModelElement.PROJECT_FRAGMENT:
 				archivePathsToRefresh.add(element.getPath());
 				break;
 			case IModelElement.SCRIPT_PROJECT:
 				ScriptProject scriptProject = (ScriptProject) element;
 				if (!DLTKLanguageManager.hasScriptNature(scriptProject
 						.getProject())) {
 					// project is not accessible or has lost its script
 					// nature
 					break;
 				}
 				IEnvironment environment = EnvironmentManager
 						.getEnvironment(scriptProject);
 				if (environment != null && !environment.isConnected()) {
 					// Project environment is not connected.
 					break;
 				}
 				IBuildpathEntry[] buildpath;
 				try {
 					buildpath = scriptProject.getResolvedBuildpath();
 					for (int j = 0, cpLength = buildpath.length; j < cpLength; j++) {
 						if (buildpath[j].getEntryKind() == IBuildpathEntry.BPE_LIBRARY) {
 							archivePathsToRefresh.add(buildpath[j].getPath());
 						}
 					}
 				} catch (ModelException e) {
 					// project doesn't exist -> ignore
 				}
 				break;
 			case IModelElement.SCRIPT_MODEL:
 				Iterator projectNames = this.state.getOldScriptProjectNames()
 						.iterator();
 				while (projectNames.hasNext()) {
 					String projectName = (String) projectNames.next();
 					IProject project = ResourcesPlugin.getWorkspace().getRoot()
 							.getProject(projectName);
 					if (!DLTKLanguageManager.hasScriptNature(project)) {
 						// project is not accessible or has lost its Script
 						// nature
 						continue;
 					}
 					scriptProject = (ScriptProject) DLTKCore.create(project);
 					try {
 						buildpath = scriptProject.getResolvedBuildpath();
 					} catch (ModelException e2) {
 						// project doesn't exist -> ignore
 						continue;
 					}
 					for (int k = 0, cpLength = buildpath.length; k < cpLength; k++) {
 						if (buildpath[k].getEntryKind() == IBuildpathEntry.BPE_LIBRARY) {
 							archivePathsToRefresh.add(buildpath[k].getPath());
 						}
 					}
 				}
 				break;
 			}
 		}
 		// perform refresh
 		Iterator projectNames = this.state.getOldScriptProjectNames()
 				.iterator();
 		IWorkspaceRoot wksRoot = ResourcesPlugin.getWorkspace().getRoot();
 		while (projectNames.hasNext()) {
 			if (monitor != null && monitor.isCanceled()) {
 				break;
 			}
 			String projectName = (String) projectNames.next();
 			IProject project = wksRoot.getProject(projectName);
 			if (!DLTKLanguageManager.hasScriptNature(project)) {
 				// project is not accessible or has lost its Script nature
 				continue;
 			}
 			ScriptProject scriptProject = (ScriptProject) DLTKCore
 					.create(project);
 			IBuildpathEntry[] entries;
 			try {
 				entries = scriptProject.getResolvedBuildpath();
 			} catch (ModelException e1) {
 				// project does not exist -> ignore
 				continue;
 			}
 			for (int j = 0; j < entries.length; j++) {
 				IBuildpathEntry entry = entries[j];
 				if (entry.getEntryKind() == IBuildpathEntry.BPE_LIBRARY) {
 					IPath entryPath = entry.getPath();
 					if (!archivePathsToRefresh.contains(entryPath)) {
 						continue; // not supposed to be refreshed
 					}
 					String status = (String) externalArchivesStatus
 							.get(entryPath);
 					if (status == null) {
 						// compute shared status
 						Object targetLibrary = Model.getTarget(wksRoot,
 								entryPath, true);
 						if (targetLibrary == null) { // missing JAR
 							if (this.state.getExternalLibTimeStamps().remove(
 									entryPath) != null) {
 								externalArchivesStatus.put(entryPath,
 										EXTERNAL_ZIP_REMOVED);
 								// the jar was physically removed: remove the
 								// index
 								this.manager.indexManager
 										.removeIndex(entryPath);
 								ProjectIndexerManager.removeLibrary(
 										scriptProject, entryPath);
 							}
 						} else if (targetLibrary instanceof IFileHandle) { // external
 							// JAR
 							IFileHandle externalFile = (IFileHandle) targetLibrary;
 							// check timestamp to figure if JAR has changed in
 							// some way
 							Long oldTimestamp = (Long) this.state
 									.getExternalLibTimeStamps().get(entryPath);
 							long newTimeStamp = getTimeStamp(externalFile);
 							final BuildpathEntry bpEntry = (BuildpathEntry) entry;
 							if (oldTimestamp != null) {
 								if (newTimeStamp == 0) { // file doesn't
 									// exist
 									externalArchivesStatus.put(entryPath,
 											EXTERNAL_ZIP_REMOVED);
 									this.state.getExternalLibTimeStamps()
 											.remove(entryPath);
 									// remove the index
 									this.manager.indexManager
 											.removeIndex(entryPath);
 									ProjectIndexerManager.removeLibrary(
 											scriptProject, entryPath);
 								} else if (oldTimestamp.longValue() != newTimeStamp) {
 									externalArchivesStatus.put(entryPath,
 											EXTERNAL_ZIP_CHANGED);
 									this.state.getExternalLibTimeStamps().put(
 											entryPath, new Long(newTimeStamp));
 									// first remove the index so that it is
 									// forced to be re-indexed
 									this.manager.indexManager
 											.removeIndex(entryPath);
 									// then index the jar
 									ProjectIndexerManager.indexLibrary(
 											scriptProject, entryPath);
 								} else {
 									externalArchivesStatus.put(entryPath,
 											EXTERNAL_ZIP_UNCHANGED);
 								}
 							} else {
 								if (newTimeStamp == 0) { // jar still doesn't
 									// exist
 									externalArchivesStatus.put(entryPath,
 											EXTERNAL_ZIP_UNCHANGED);
 								} else {
 									externalArchivesStatus.put(entryPath,
 											EXTERNAL_ZIP_ADDED);
 									this.state.getExternalLibTimeStamps().put(
 											entryPath, new Long(newTimeStamp));
 									// index the new jar
 									ProjectIndexerManager.indexLibrary(
 											scriptProject, entryPath);
 								}
 							}
 						} else { // internal ZIP
 							externalArchivesStatus.put(entryPath,
 									INTERNAL_ZIP_IGNORE);
 						}
 					}
 					// according to computed status, generate a delta
 					status = (String) externalArchivesStatus.get(entryPath);
 					if (status != null) {
 						if (status == EXTERNAL_ZIP_ADDED) {
 							IProjectFragment root = scriptProject
 									.getProjectFragment(entryPath);
 							if (VERBOSE) {
 								System.out
 										.println("- External ZIP ADDED, affecting root: " + root.getElementName()); //$NON-NLS-1$
 							}
 							this.elementAdded((Openable) root, null, null);
 							// in case it contains a chained jar
 							scriptProject.resetResolvedBuildpath();
 							// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185733
 							this.state.addBuildpathValidation(scriptProject);
 							hasDelta = true;
 						} else if (status == EXTERNAL_ZIP_CHANGED) {
 							IProjectFragment root = scriptProject
 									.getProjectFragment(entryPath);
 							if (VERBOSE) {
 								System.out
 										.println("- External ZIP CHANGED, affecting root: " + root.getElementName()); //$NON-NLS-1$
 							}
 							this.contentChanged((Openable) root);
 							hasDelta = true;
 						} else if (status == EXTERNAL_ZIP_REMOVED) {
 							IProjectFragment root = scriptProject
 									.getProjectFragment(entryPath);
 							if (VERBOSE) {
 								System.out
 										.println("- External ZIP REMOVED, affecting root: " + root.getElementName()); //$NON-NLS-1$
 							}
 							this.elementRemoved((Openable) root, null, null);
 
 							// in case it contains a chained jar
 							scriptProject.resetResolvedBuildpath();
 							// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185733
 							this.state.addBuildpathValidation(scriptProject);
 							hasDelta = true;
 						}
 					}
 				}
 			}
 		}
 		// Check for external project fragment changes via timestamps.
 		return hasDelta;
 	}
 
 	private ModelElementDelta currentDelta() {
 		if (this.currentDelta == null) {
 			this.currentDelta = new ModelElementDelta(this.manager.getModel());
 		}
 		return this.currentDelta;
 	}
 
 	/*
 	 * Note that the project is about to be deleted.
 	 */
 	private void deleting(IProject project) {
 
 		try {
 			// discard indexing jobs that belong to this project so that the
 			// project can be
 			// deleted without interferences from the index manager
 			this.manager.indexManager.discardJobs(project.getName());
 
 			ScriptProject scriptProject = (ScriptProject) DLTKCore
 					.create(project);
 
 			// remember roots of this project
 			if (this.removedRoots == null) {
 				this.removedRoots = new HashMap();
 			}
 			if (scriptProject.isOpen()) {
 				this.removedRoots.put(scriptProject, scriptProject
 						.getProjectFragments());
 			} else {
 				// compute roots without opening project
 				this.removedRoots.put(scriptProject, scriptProject
 						.computeProjectFragments(scriptProject
 								.getResolvedBuildpath(
 										true/* ignoreUnresolvedEntry */,
 										false/*
 											 * don't generateMarkerOnError
 											 */, false/*
 													 * don't
 													 * returnResolutionInProgress
 													 */), false, null /*
 																	 * no
 																	 * reverse
 																	 * map
 																	 */));
 			}
 
 			scriptProject.close();
 
 			// workaround for bug 15168 circular errors not reported
 			this.state.getOldScriptProjectNames(); // foce list to be computed
 
 			this.removeFromParentInfo(scriptProject);
 
 			// remove preferences from per project info
 			this.manager.resetProjectPreferences(scriptProject);
 		} catch (ModelException e) {
 			// script project doesn't exist: ignore
 		}
 	}
 
 	/*
 	 * Processing for an element that has been added:<ul> <li>If the element is
 	 * a project, do nothing, and do not process children, as when a project is
 	 * created it does not yet have any natures - specifically a script nature.
 	 * <li>If the elemet is not a project, process it as added (see
 	 * <code>basicElementAdded</code>. </ul> Delta argument could be null if
 	 * processing an external ZIP change
 	 */
 	private void elementAdded(Openable element, IResourceDelta delta,
 			RootInfo rootInfo) {
 		int elementType = element.getElementType();
 		if (elementType == IModelElement.SCRIPT_PROJECT) {
 			// project add is handled by DylanProject.configure() because
 			// when a project is created, it does not yet have a script nature
 			if (delta != null
 					&& DLTKLanguageManager.hasScriptNature((IProject) delta
 							.getResource())) {
 				this.addToParentInfo(element);
 				if ((delta.getFlags() & IResourceDelta.MOVED_FROM) != 0) {
 					Openable movedFromElement = (Openable) element.getModel()
 							.getScriptProject(
 									delta.getMovedFromPath().lastSegment());
 					this.currentDelta().movedTo(element, movedFromElement);
 				} else {
 					// Force the project to be closed as it might have been
 					// opened
 					// before the resource modification came in and it might
 					// have a new child
 					// For example, in an IWorkspaceRunnable:
 					// 1. create a Java project P (where P=src)
 					// 2. open project P
 					// 3. add folder f in P's pkg fragment root
 					// When the resource delta comes in, only the addition of P
 					// is notified,
 					// but the pkg fragment root of project P is already opened,
 					// thus its children are not recomputed
 					// and it appears to contain only the default package.
 					this.close(element);
 
 					this.currentDelta().added(element);
 				}
 				this.state.updateRoots(element.getPath(), delta, this);
 				// refresh pkg fragment roots and caches of the project (and its
 				// dependents)
 				final IScriptProject project = (IScriptProject) element;
 				this.rootsToRefresh.add(project);
 				this.projectCachesToReset.add(project);
 			}
 		} else {
 			if (delta == null
 					|| (delta.getFlags() & IResourceDelta.MOVED_FROM) == 0) {
 				// regular element addition
 				if (this.isPrimaryWorkingCopy(element, elementType)) {
 					// filter out changes to primary compilation unit in working
 					// copy mode
 					// just report a change to the resource (see
 					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=59500)
 					this.currentDelta().changed(element,
 							IModelElementDelta.F_PRIMARY_RESOURCE);
 				} else {
 					this.addToParentInfo(element);
 					// Force the element to be closed as it might have been
 					// opened
 					// before the resource modification came in and it might
 					// have a new child
 					// For example, in an IWorkspaceRunnable:
 					// 1. create a package fragment p using a script model
 					// operation
 					// 2. open package p
 					// 3. add file X.java in folder p
 					// When the resource delta comes in, only the addition of p
 					// is notified,
 					// but the package p is already opened, thus its children
 					// are not recomputed
 					// and it appears empty.
 					this.close(element);
 					this.currentDelta().added(element);
 				}
 			} else {
 				// element is moved
 				this.addToParentInfo(element);
 				this.close(element);
 				IPath movedFromPath = delta.getMovedFromPath();
 				IResource res = delta.getResource();
 				IResource movedFromRes;
 				if (res instanceof IFile) {
 					movedFromRes = res.getWorkspace().getRoot().getFile(
 							movedFromPath);
 				} else {
 					movedFromRes = res.getWorkspace().getRoot().getFolder(
 							movedFromPath);
 				}
 				// find the element type of the moved from element
 				RootInfo movedFromInfo = this.enclosingRootInfo(movedFromPath,
 						IResourceDelta.REMOVED);
 				int movedFromType = this.elementType(movedFromRes,
 						IResourceDelta.REMOVED, element.getParent()
 								.getElementType(), movedFromInfo);
 				// reset current element as it might be inside a nested root
 				// (popUntilPrefixOf() may use the outer root)
 				this.currentElement = null;
 				// create the moved from element
 				Openable movedFromElement = elementType != IModelElement.SCRIPT_PROJECT
 						&& movedFromType == IModelElement.SCRIPT_PROJECT ? null
 						: // outside buildpath
 						this.createElement(movedFromRes, movedFromType,
 								movedFromInfo);
 				if (movedFromElement == null) {
 					// moved from outside buildpath
 					this.currentDelta().added(element);
 				} else {
 					this.currentDelta().movedTo(element, movedFromElement);
 				}
 			}
 			switch (elementType) {
 			case IModelElement.PROJECT_FRAGMENT:
 				// when a root is added, and is on the buildpath, the
 				// project
 				// must be updated
 				ScriptProject project = (ScriptProject) element
 						.getScriptProject();
 				// refresh pkg fragment roots and caches of the project (and
 				// its
 				// dependents)
 				this.rootsToRefresh.add(project);
 				this.projectCachesToReset.add(project);
 				break;
 			case IModelElement.SCRIPT_FOLDER:
 				// reset project's package fragment cache
 				project = (ScriptProject) element.getScriptProject();
 				this.projectCachesToReset.add(project);
 				break;
 			}
 		}
 	}
 
 	/*
 	 * Generic processing for a removed element:<ul> <li>Close the element,
 	 * removing its structure from the cache <li>Remove the element from its
 	 * parent's cache of children <li>Add a REMOVED entry in the delta </ul>
 	 * Delta argument could be null if processing an external ZIP change
 	 */
 	private void elementRemoved(Openable element, IResourceDelta delta,
 			RootInfo rootInfo) {
 		int elementType = element.getElementType();
 		if (delta == null || (delta.getFlags() & IResourceDelta.MOVED_TO) == 0) {
 			// regular element removal
 			if (this.isPrimaryWorkingCopy(element, elementType)) {
 				// filter out changes to primary compilation unit in working
 				// copy mode
 				// just report a change to the resource (see
 				// https://bugs.eclipse.org/bugs/show_bug.cgi?id=59500)
 				this.currentDelta().changed(element,
 						IModelElementDelta.F_PRIMARY_RESOURCE);
 			} else {
 				this.close(element);
 				this.removeFromParentInfo(element);
 				this.currentDelta().removed(element);
 			}
 		} else {
 			// element is moved
 			this.close(element);
 			this.removeFromParentInfo(element);
 			IPath movedToPath = delta.getMovedToPath();
 			IResource res = delta.getResource();
 			IResource movedToRes;
 			switch (res.getType()) {
 			case IResource.PROJECT:
 				movedToRes = res.getWorkspace().getRoot().getProject(
 						movedToPath.lastSegment());
 				break;
 			case IResource.FOLDER:
 				movedToRes = res.getWorkspace().getRoot()
 						.getFolder(movedToPath);
 				break;
 			case IResource.FILE:
 				movedToRes = res.getWorkspace().getRoot().getFile(movedToPath);
 				break;
 			default:
 				return;
 			}
 			// find the element type of the moved from element
 			RootInfo movedToInfo = this.enclosingRootInfo(movedToPath,
 					IResourceDelta.ADDED);
 			int movedToType = this.elementType(movedToRes,
 					IResourceDelta.ADDED, element.getParent().getElementType(),
 					movedToInfo);
 			// reset current element as it might be inside a nested root
 			// (popUntilPrefixOf() may use the outer root)
 			this.currentElement = null;
 			// create the moved To element
 			Openable movedToElement = elementType != IModelElement.SCRIPT_PROJECT
 					&& movedToType == IModelElement.SCRIPT_PROJECT ? null : // outside
 					// buildpath
 					this.createElement(movedToRes, movedToType, movedToInfo);
 			if (movedToElement == null) {
 				// moved outside buildpath
 				this.currentDelta().removed(element);
 			} else {
 				this.currentDelta().movedFrom(element, movedToElement);
 			}
 		}
 		switch (elementType) {
 		case IModelElement.SCRIPT_MODEL:
 			this.manager.indexManager.reset();
 			break;
 		case IModelElement.SCRIPT_PROJECT: {
 			this.state.updateRoots(element.getPath(), delta, this);
 			// refresh pkg fragment roots and caches of the project (and its
 			// dependents)
 			final IScriptProject project = (IScriptProject) element;
 			this.rootsToRefresh.add(project);
 			this.projectCachesToReset.add(project);
 			break;
 		}
 		case IModelElement.PROJECT_FRAGMENT: {
 			IScriptProject project = element.getScriptProject();
 			// refresh pkg fragment roots and caches of the project (and its
 			// dependents)
 			this.rootsToRefresh.add(project);
 			this.projectCachesToReset.add(project);
 			break;
 		}
 		case IModelElement.SCRIPT_FOLDER: {
 			// reset package fragment cache
 			IScriptProject project = element.getScriptProject();
 			this.projectCachesToReset.add(project);
 			break;
 		}
 		}
 	}
 
 	/*
 	 * Returns the type of the model element the given delta matches to. Returns
 	 * NON_SCRIPT_RESOURCE if unknown (e.g. a non-script resource or excluded
 	 * file)
 	 */
 	private int elementType(IResource res, int kind, int parentType,
 			RootInfo rootInfo) {
 		switch (parentType) {
 		case IModelElement.SCRIPT_MODEL:
 			// case of a movedTo or movedFrom project (other cases are
 			// handled
 			// in processResourceDelta(...)
 			return IModelElement.SCRIPT_PROJECT;
 		case NON_SCRIPT_RESOURCE:
 		case IModelElement.SCRIPT_PROJECT:
 			if (rootInfo == null) {
 				rootInfo = this.enclosingRootInfo(res.getFullPath(), kind);
 			}
 			if (rootInfo != null && rootInfo.isRootOfProject(res.getFullPath())) {
 				return IModelElement.PROJECT_FRAGMENT;
 			}
 			// not yet in a package fragment root or root of another project
 			// or package fragment to be included (see below)
 			// -> let it go through
 		case IModelElement.PROJECT_FRAGMENT:
 		case IModelElement.SCRIPT_FOLDER:
 			if (rootInfo == null) {
 				rootInfo = this.enclosingRootInfo(res.getFullPath(), kind);
 			}
 			if (rootInfo == null) {
 				return NON_SCRIPT_RESOURCE;
 			}
 			if (Util.isExcluded(res, rootInfo.inclusionPatterns,
 					rootInfo.exclusionPatterns)) {
 				return NON_SCRIPT_RESOURCE;
 			}
 			if (res.getType() == IResource.FOLDER) {
 				if (parentType == NON_SCRIPT_RESOURCE
 						&& !Util.isExcluded(res.getParent(),
 								rootInfo.inclusionPatterns,
 								rootInfo.exclusionPatterns)) {
 					// parent is a non-script resource because it doesn't
 					// have a
 					// valid package name (see
 					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=130982)
 					return NON_SCRIPT_RESOURCE;
 				}
 				if (Util.isValidFolderNameForPackage(res.getName())) {
 					return IModelElement.SCRIPT_FOLDER;
 				}
 				return NON_SCRIPT_RESOURCE;
 			}
 
 			// String fileName = res.getName();
 			IProject project = res.getProject();
 			IScriptProject scriptProject = DLTKCore.create(project);
 			if (scriptProject != null) {
 				if (Util.isValidSourceModule(scriptProject, res)) {
 					return IModelElement.SOURCE_MODULE;
 				}
 				// FIXME: Add support of checking files without extensions here.
 			} else if (this.rootInfo(res.getFullPath(), kind) != null) {
 				// case of proj=src=bin and resource is a jar file on the
 				// buildpath
 				return IModelElement.PROJECT_FRAGMENT;
 			} else {
 				return NON_SCRIPT_RESOURCE;
 			}
 		default:
 			return NON_SCRIPT_RESOURCE;
 		}
 	}
 
 	/*
 	 * Flushes all deltas without firing them.
 	 */
 	public void flush() {
 		this.modelDeltas = new ArrayList<IModelElementDelta>();
 	}
 
 	/*
 	 * Finds the root info this path is included in. Returns null if not found.
 	 */
 	private RootInfo enclosingRootInfo(IPath path, int kind) {
 		while (path != null && path.segmentCount() > 0) {
 			RootInfo rootInfo = this.rootInfo(path, kind);
 			if (rootInfo != null) {
 				return rootInfo;
 			}
 			path = path.removeLastSegments(1);
 		}
 		return null;
 	}
 
 	/*
 	 * Fire script Model delta, flushing them after the fact after post_change
 	 * notification. If the firing mode has been turned off, this has no effect.
 	 */
 	public void fire(IModelElementDelta customDelta, int eventType) {
 		if (!this.isFiring) {
 			return;
 		}
 		if (DEBUG) {
 			System.out
 					.println("-----------------------------------------------------------------------------------------------------------------------");//$NON-NLS-1$
 		}
 		IModelElementDelta deltaToNotify;
 		if (customDelta == null) {
 			deltaToNotify = this.mergeDeltas(this.modelDeltas);
 		} else {
 			deltaToNotify = customDelta;
 		}
 		// Refresh internal scopes
 		if (deltaToNotify != null) {
 			// Iterator scopes = this.manager.searchScopes.keySet().iterator();
 			// while (scopes.hasNext()) {
 			// AbstractSearchScope scope = (AbstractSearchScope) scopes.next();
 			// scope.processDelta(deltaToNotify);
 			// }
 			if (this.manager.workspaceScope != null) {
 				Iterator wsIter = this.manager.workspaceScope.values()
 						.iterator();
 				for (; wsIter.hasNext();) {
 					DLTKWorkspaceScope workspaceScope = (DLTKWorkspaceScope) wsIter
 							.next();
 					if (workspaceScope != null) {
 						workspaceScope.processDelta(deltaToNotify);
 					}
 				}
 			}
 		}
 		// Notification
 		// Important: if any listener reacts to notification by updating the
 		// listeners list or mask, these lists will
 		// be duplicated, so it is necessary to remember original lists in a
 		// variable (since field values may change under us)
 		IElementChangedListener[] listeners;
 		int[] listenerMask;
 		int listenerCount;
 		synchronized (this.state) {
 			listeners = this.state.elementChangedListeners;
 			listenerMask = this.state.elementChangedListenerMasks;
 			listenerCount = this.state.elementChangedListenerCount;
 		}
 		switch (eventType) {
 		case DEFAULT_CHANGE_EVENT:
 			this.firePostChangeDelta(deltaToNotify, listeners, listenerMask,
 					listenerCount);
 			this.fireReconcileDelta(listeners, listenerMask, listenerCount);
 			break;
 		case ElementChangedEvent.POST_CHANGE:
 			this.firePostChangeDelta(deltaToNotify, listeners, listenerMask,
 					listenerCount);
 			this.fireReconcileDelta(listeners, listenerMask, listenerCount);
 			break;
 		}
 	}
 
 	private void firePostChangeDelta(IModelElementDelta deltaToNotify,
 			IElementChangedListener[] listeners, int[] listenerMask,
 			int listenerCount) {
 		// post change deltas
 		if (DEBUG) {
 			System.out
 					.println("FIRING POST_CHANGE Delta [" + Thread.currentThread() + "]:"); //$NON-NLS-1$//$NON-NLS-2$
 			System.out
 					.println(deltaToNotify == null ? "<NONE>" : deltaToNotify.toString()); //$NON-NLS-1$
 		}
 		if (deltaToNotify != null) {
 			// flush now so as to keep listener reactions to post their own
 			// deltas for subsequent iteration
 			this.flush();
 			this.notifyListeners(deltaToNotify,
 					ElementChangedEvent.POST_CHANGE, listeners, listenerMask,
 					listenerCount);
 		}
 	}
 
 	private void fireReconcileDelta(IElementChangedListener[] listeners,
 			int[] listenerMask, int listenerCount) {
 		IModelElementDelta deltaToNotify = this
 				.mergeDeltas(this.reconcileDeltas.values());
 		if (DEBUG) {
 			System.out
 					.println("FIRING POST_RECONCILE Delta [" + Thread.currentThread() + "]:"); //$NON-NLS-1$//$NON-NLS-2$
 			System.out
 					.println(deltaToNotify == null ? "<NONE>" : deltaToNotify.toString()); //$NON-NLS-1$
 		}
 		if (deltaToNotify != null) {
 			// flush now so as to keep listener reactions to post their own
 			// deltas for subsequent iteration
 			this.reconcileDeltas = new HashMap();
 			this.notifyListeners(deltaToNotify,
 					ElementChangedEvent.POST_RECONCILE, listeners,
 					listenerMask, listenerCount);
 		}
 	}
 
 	/*
 	 * Returns whether a given delta contains some information relevant to the
 	 * Model, in particular it will not consider SYNC or MARKER only deltas.
 	 */
 	private boolean isAffectedBy(IResourceDelta rootDelta) {
 		// if (rootDelta == null) System.out.println("NULL DELTA");
 		// long start = System.currentTimeMillis();
 		if (rootDelta != null) {
 			// use local exception to quickly escape from delta traversal
 			class FoundRelevantDeltaException extends RuntimeException {
 				private static final long serialVersionUID = 7137113252936111022L; // backward
 				// compatible
 				// only the class name is used (to differenciate from other
 				// RuntimeExceptions)
 			}
 			try {
 				rootDelta.accept(new IResourceDeltaVisitor() {
 					public boolean visit(IResourceDelta delta) /*
 																 * throws
 																 * CoreException
 																 */{
 						switch (delta.getKind()) {
 						case IResourceDelta.ADDED:
 						case IResourceDelta.REMOVED:
 							throw new FoundRelevantDeltaException();
 						case IResourceDelta.CHANGED:
 							// if any flag is set but SYNC or MARKER, this
 							// delta
 							// should be considered
 							if (delta.getAffectedChildren().length == 0 // only
 									// check
 									// leaf
 									// delta
 									// nodes
 									&& (delta.getFlags() & ~(IResourceDelta.SYNC | IResourceDelta.MARKERS)) != 0) {
 								throw new FoundRelevantDeltaException();
 							}
 						}
 						return true;
 					}
 				});
 			} catch (FoundRelevantDeltaException e) {
 				// System.out.println("RELEVANT DELTA detected in: "+
 				// (System.currentTimeMillis() - start));
 				return true;
 			} catch (CoreException e) { // ignore delta if not able to traverse
 			}
 		}
 		// System.out.println("IGNORE SYNC DELTA took: "+
 		// (System.currentTimeMillis() - start));
 		return false;
 	}
 
 	/*
 	 * Returns whether the given element is a primary compilation unit in
 	 * working copy mode.
 	 */
 	private boolean isPrimaryWorkingCopy(IModelElement element, int elementType) {
 		if (elementType == IModelElement.SOURCE_MODULE) {
 			SourceModule cu = (SourceModule) element;
 			return cu.isPrimary() && cu.isWorkingCopy();
 		}
 		return false;
 	}
 
 	/*
 	 * Merges all awaiting deltas.
 	 */
 	private IModelElementDelta mergeDeltas(Collection deltas) {
 		if (deltas.size() == 0) {
 			return null;
 		}
 		if (deltas.size() == 1) {
 			return (IModelElementDelta) deltas.iterator().next();
 		}
 		if (VERBOSE) {
 			System.out
 					.println("MERGING " + deltas.size() + " DELTAS [" + Thread.currentThread() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		}
 		Iterator iterator = deltas.iterator();
 		ModelElementDelta rootDelta = new ModelElementDelta(this.manager.model);
 		boolean insertedTree = false;
 		while (iterator.hasNext()) {
 			ModelElementDelta delta = (ModelElementDelta) iterator.next();
 			if (VERBOSE) {
 				System.out.println(delta.toString());
 			}
 			IModelElement element = delta.getElement();
 			if (this.manager.model.equals(element)) {
 				IModelElementDelta[] children = delta.getAffectedChildren();
 				for (int j = 0; j < children.length; j++) {
 					ModelElementDelta projectDelta = (ModelElementDelta) children[j];
 					rootDelta.insertDeltaTree(projectDelta.getElement(),
 							projectDelta);
 					insertedTree = true;
 				}
 				IResourceDelta[] resourceDeltas = delta.getResourceDeltas();
 				if (resourceDeltas != null) {
 					for (int i = 0, length = resourceDeltas.length; i < length; i++) {
 						rootDelta.addResourceDelta(resourceDeltas[i]);
 						insertedTree = true;
 					}
 				}
 			} else {
 				rootDelta.insertDeltaTree(element, delta);
 				insertedTree = true;
 			}
 		}
 		if (insertedTree) {
 			return rootDelta;
 		}
 		return null;
 	}
 
 	private void notifyListeners(IModelElementDelta deltaToNotify,
 			int eventType, IElementChangedListener[] listeners,
 			int[] listenerMask, int listenerCount) {
 		final ElementChangedEvent extraEvent = new ElementChangedEvent(
 				deltaToNotify, eventType);
 		for (int i = 0; i < listenerCount; i++) {
 			if ((listenerMask[i] & eventType) != 0) {
 				final IElementChangedListener listener = listeners[i];
 				long start = -1;
 				if (VERBOSE) {
 					System.out
 							.print("Listener #" + (i + 1) + "=" + listener.toString());//$NON-NLS-1$//$NON-NLS-2$
 					start = System.currentTimeMillis();
 				}
 				// wrap callbacks with Safe runnable for subsequent listeners to
 				// be called when some are causing grief
 				SafeRunner.run(new ISafeRunnable() {
 					public void handleException(Throwable exception) {
 						Util
 								.log(exception,
 										"Exception occurred in listener of script element change notification"); //$NON-NLS-1$
 					}
 
 					public void run() throws Exception {
 						PerformanceStats stats = null;
 						if (PERF) {
 							stats = PerformanceStats.getStats(
 									ModelManager.DELTA_LISTENER_PERF, listener);
 							stats.startRun();
 						}
 						listener.elementChanged(extraEvent);
 						if (PERF) {
 							stats.endRun();
 						}
 					}
 				});
 				if (VERBOSE) {
 					System.out
 							.println(" -> " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
 				}
 			}
 		}
 	}
 
 	private void notifyTypeHierarchies(IElementChangedListener[] listeners,
 			int listenerCount) {
 		// TODO implement
 		if (DLTKCore.DEBUG) {
 			System.out.println("notify type hierarchies"); //$NON-NLS-1$
 		}
 	}
 
 	/*
 	 * Generic processing for elements with changed contents:<ul> <li>The
 	 * element is closed such that any subsequent accesses will re-open the
 	 * element reflecting its new structure. <li>An entry is made in the delta
 	 * reporting a content change (K_CHANGE with F_CONTENT flag set). </ul>
 	 */
 	private void nonScriptResourcesChanged(Openable element,
 			IResourceDelta delta) throws ModelException {
 		// reset non-script resources if element was open
 		if (element.isOpen()) {
 			ModelElementInfo info = (ModelElementInfo) element.getElementInfo();
 			switch (element.getElementType()) {
 			case IModelElement.SCRIPT_MODEL:
 				((ModelInfo) info).foreignResources = null;
 				this.currentDelta().addResourceDelta(delta);
 				return;
 			case IModelElement.SCRIPT_PROJECT:
 				((ProjectElementInfo) info).setForeignResources(null);
 				// if a package fragment root is the project, clear it too
 				ScriptProject project = (ScriptProject) element;
 				IProjectFragment projectRoot = project
 						.getProjectFragment(project.getProject());
 				if (projectRoot.isOpen()) {
 					((ProjectFragmentInfo) ((Openable) projectRoot)
 							.getElementInfo()).setForeignResources(null);
 				}
 				break;
 			case IModelElement.SCRIPT_FOLDER:
 				((ScriptFolderInfo) info).setForeignResources(null);
 				break;
 			case IModelElement.PROJECT_FRAGMENT:
 				((ProjectFragmentInfo) info).setForeignResources(null);
 			}
 		}
 		ModelElementDelta current = this.currentDelta();
 		ModelElementDelta elementDelta = current.find(element);
 		if (elementDelta == null) {
 			// don't use find after creating the delta as it can be null (see
 			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=63434)
 			elementDelta = current.changed(element,
 					IModelElementDelta.F_CONTENT);
 		}
 		elementDelta.addResourceDelta(delta);
 	}
 
 	/*
 	 * Returns the other root infos for the given path. Look in the old other
 	 * roots table if kind is REMOVED.
 	 */
 	private ArrayList otherRootsInfo(IPath path, int kind) {
 		if (kind == IResourceDelta.REMOVED) {
 			return (ArrayList) this.state.oldOtherRoots.get(path);
 		}
 		return (ArrayList) this.state.otherRoots.get(path);
 	}
 
 	private void popUntilPrefixOf(IPath path) {
 		while (this.currentElement != null) {
 			IPath currentElementPath = null;
 			if (this.currentElement instanceof IProjectFragment) {
 				currentElementPath = ((IProjectFragment) this.currentElement)
 						.getPath();
 			} else {
 				IResource currentElementResource = this.currentElement
 						.getResource();
 				if (currentElementResource != null) {
 					currentElementPath = currentElementResource.getFullPath();
 				}
 			}
 			if (currentElementPath != null) {
 				if (this.currentElement instanceof IScriptFolder
 						&& ((IScriptFolder) this.currentElement).isRootFolder()
 						&& currentElementPath.segmentCount() != path
 								.segmentCount() - 1) {
 					// default package and path is not a direct child
 					this.currentElement = (Openable) this.currentElement
 							.getParent();
 				}
 				if (currentElementPath.isPrefixOf(path)) {
 					return;
 				}
 			}
 			this.currentElement = (Openable) this.currentElement.getParent();
 		}
 	}
 
 	/*
 	 * Converts a <code>IResourceDelta</code> rooted in a <code>Workspace</code>
 	 * into the corresponding set of <code>IModelElementDelta</code>, rooted in
 	 * the relevant <code>Model</code>s.
 	 */
 	private IModelElementDelta processResourceDelta(IResourceDelta changes) {
 		try {
 			IScriptModel model = this.manager.getModel();
 			if (!model.isOpen()) {
 				// force opening of script model so that model element delta are
 				// reported
 				try {
 					model.open(null);
 				} catch (ModelException e) {
 					if (VERBOSE) {
 						e.printStackTrace();
 					}
 					return null;
 				}
 			}
 			this.state.initializeRoots();
 			this.currentElement = null;
 			// get the workspace delta, and start processing there.
 			IResourceDelta[] deltas = changes.getAffectedChildren();
 			for (int i = 0; i < deltas.length; i++) {
 				IResourceDelta delta = deltas[i];
 				IResource res = delta.getResource();
 				// find out the element type
 				RootInfo rootInfo = null;
 				int elementType;
 				IProject proj = (IProject) res;
 				boolean wasDylanProject = this.state
 						.findProject(proj.getName()) != null;
 				boolean isDylanProject = DLTKLanguageManager
 						.hasScriptNature(proj);
 				if (!wasDylanProject && !isDylanProject) {
 					elementType = NON_SCRIPT_RESOURCE;
 				} else {
 					rootInfo = this.enclosingRootInfo(res.getFullPath(), delta
 							.getKind());
 					if (rootInfo != null
 							&& rootInfo.isRootOfProject(res.getFullPath())) {
 						elementType = IModelElement.PROJECT_FRAGMENT;
 					} else {
 						elementType = IModelElement.SCRIPT_PROJECT;
 					}
 				}
 				// traverse delta
 				this.traverseDelta(delta, elementType, rootInfo);
 				if (elementType == NON_SCRIPT_RESOURCE
 						|| (wasDylanProject != isDylanProject && (delta
 								.getKind()) == IResourceDelta.CHANGED)) {
 					/*
 					 * project has changed nature (description or open/closed)
 					 */
 					try {
 						// add child as non script resource
 						this.nonScriptResourcesChanged((Model) model, delta);
 					} catch (ModelException e) {
 						// script model could not be opened
 					}
 				}
 			}
 			this.refreshProjectFragments();
 			this.resetProjectCaches();
 			this.executePostActions();
 			return this.currentDelta;
 		} finally {
 			this.currentDelta = null;
 			this.rootsToRefresh.clear();
 			this.projectCachesToReset.clear();
 			this.postActions.clear();
 		}
 	}
 
 	private void executePostActions() {
 		if (postActions.size() == 0) {
 			return;
 		}
 		for (Iterator<Runnable> i = postActions.iterator(); i.hasNext();) {
 			i.next().run();
 		}
 	}
 
 	/*
 	 * Traverse the set of projects which have changed namespace, and reset
 	 * their caches and their dependents
 	 */
 	private void resetProjectCaches() {
 		if (this.projectCachesToReset.size() == 0) {
 			return;
 		}
 		ModelManager.getModelManager().resetZIPTypeCache();
 		Iterator iterator = this.projectCachesToReset.iterator();
 		HashMap projectDepencies = this.state.projectDependencies;
 		HashSet affectedDependents = new HashSet();
 		while (iterator.hasNext()) {
 			ScriptProject project = (ScriptProject) iterator.next();
 			project.resetCaches();
 			this.addDependentProjects(project, projectDepencies,
 					affectedDependents);
 		}
 		// reset caches of dependent projects
 		iterator = affectedDependents.iterator();
 		while (iterator.hasNext()) {
 			ScriptProject project = (ScriptProject) iterator.next();
 			project.resetCaches();
 		}
 	}
 
 	/*
 	 * Refresh package fragment roots of projects that were affected
 	 */
 	private void refreshProjectFragments() {
 		Iterator iterator = this.rootsToRefresh.iterator();
 		while (iterator.hasNext()) {
 			ScriptProject project = (ScriptProject) iterator.next();
 			project.updateProjectFragments();
 		}
 	}
 
 	/*
 	 * Registers the given delta with this delta processor.
 	 */
 	public void registerModelDelta(IModelElementDelta delta) {
 		this.modelDeltas.add(delta);
 	}
 
 	/*
 	 * Removes the given element from its parents cache of children. If the
 	 * element does not have a parent, or the parent is not currently open, this
 	 * has no effect.
 	 */
 	private void removeFromParentInfo(Openable child) {
 		Openable parent = (Openable) child.getParent();
 		if (parent != null && parent.isOpen()) {
 			try {
 				ModelElementInfo info = (ModelElementInfo) parent
 						.getElementInfo();
 				info.removeChild(child);
 			} catch (ModelException e) {
 				// do nothing - we already checked if open
 			}
 		}
 	}
 
 	/*
 	 * Notification that some resource changes have happened on the platform,
 	 * and that the script Model should update any required internal structures
 	 * such that its elements remain consistent. Translates
 	 * <code>IResourceDeltas</code> into <code>IModelElementDeltas</code>.
 	 * 
 	 * @see IResourceDelta
 	 * 
 	 * @see IResource
 	 */
 	public void resourceChanged(IResourceChangeEvent event) {
 		int eventType = this.overridenEventType == -1 ? event.getType()
 				: this.overridenEventType;
 		IResource resource = event.getResource();
 		IResourceDelta delta = event.getDelta();
 
 		switch (eventType) {
 		case IResourceChangeEvent.PRE_DELETE:
 			if (resource.getType() == IResource.PROJECT
 					&& DLTKLanguageManager.hasScriptNature((IProject) resource)) {
 
 				this.deleting((IProject) resource);
 			}
 			return;
 
 		case IResourceChangeEvent.POST_CHANGE:
 			if (this.isAffectedBy(delta)) { // avoid populating for SYNC or
 				// MARKER
 				// deltas
 				try {
 					try {
 						this.stopDeltas();
 						this.checkProjectsBeingAddedOrRemoved(delta);
 						// generate external archive change deltas
 						if (this.refreshedElements != null) {
 							Set<IModelElement> refreshedElementsCopy = null;
 							if (refreshedElements != null) {
 								refreshedElementsCopy = new HashSet<IModelElement>();
 								refreshedElementsCopy.addAll(refreshedElements);
 								// To avoid concurrent modifications
 								this.refreshedElements = null;
 							}
 							// Call archive or custom deltas only if project are
 							// correctly connected
 
 							this.createExternalArchiveDelta(null,
 									refreshedElementsCopy);
 							this.createCustomElementDelta(null,
 									refreshedElementsCopy);
 						}
 						IModelElementDelta translatedDelta = this
 								.processResourceDelta(delta);
 						if (translatedDelta != null) {
 							this.registerModelDelta(translatedDelta);
 						}
 					} finally {
 						this.sourceElementParserCache = null; // don't hold
 						// onto parser
 						// longer than
 						// necessary
 						this.startDeltas();
 					}
 					IElementChangedListener[] listeners;
 					int listenerCount;
 					synchronized (this.state) {
 						listeners = this.state.elementChangedListeners;
 						listenerCount = this.state.elementChangedListenerCount;
 					}
 					this.notifyTypeHierarchies(listeners, listenerCount);
 					this.fire(null, ElementChangedEvent.POST_CHANGE);
 				} finally {
 					// workaround for bug 15168 circular errors not reported
 					this.state.resetOldScriptProjectNames();
 					this.removedRoots = null;
 				}
 			}
 			return;
 
 		case IResourceChangeEvent.PRE_BUILD:
 			if (!this.isAffectedBy(delta)) {
 				return; // avoid populating for SYNC or MARKER deltas
 			}
 
 			// create classpath markers if necessary
 			boolean needCycleValidation = validateBuildpaths(delta);
 			BuildpathValidation[] validations = this.state
 					.removeBuildpathValidations();
 			if (validations != null) {
 				for (int i = 0, length = validations.length; i < length; i++) {
 					BuildpathValidation validation = validations[i];
 					validation.validate();
 				}
 			}
 
 			// update project references if necessary
 			ProjectReferenceChange[] projectRefChanges = this.state
 					.removeProjectReferenceChanges();
 			if (projectRefChanges != null) {
 				for (int i = 0, length = projectRefChanges.length; i < length; i++) {
 					try {
 						projectRefChanges[i]
 								.updateProjectReferencesIfNecessary();
 					} catch (ModelException e) {
 						// project doesn't exist any longer, continue with next
 						// one
 					}
 				}
 			}
 			if (needCycleValidation || projectRefChanges != null) {
 				// update all cycle markers since the project references changes
 				// may have affected cycles
 				try {
 					ScriptProject.validateCycles(null);
 				} catch (ModelException e) {
 					// a project no longer exists
 				}
 			}
 			Model.flushExternalFileCache();
 			ScriptBuilder.buildStarting();
 
 			// does not fire any deltas
 			return;
 
 		case IResourceChangeEvent.POST_BUILD:
 			ScriptBuilder.buildFinished();
 			return;
 		}
 	}
 
 	/**
 	 * Create delta for custom user project fragments.
 	 * 
 	 * @param refreshedElementsCopy
 	 */
 	private boolean createCustomElementDelta(IProgressMonitor monitor,
 			Set refreshedElements) {
 		if (refreshedElements == null) {
 			return false;
 		}
 		boolean hasDelta = false;
 
 		HashSet<IProjectFragment> fragmentsToRefresh = new HashSet<IProjectFragment>();
 		Iterator<?> iterator = refreshedElements.iterator();
 		while (iterator.hasNext()) {
 			IModelElement element = (IModelElement) iterator.next();
 			switch (element.getElementType()) {
 			case IModelElement.PROJECT_FRAGMENT:
 				IProjectFragment fragment = (IProjectFragment) element;
 				try {
 					if (fragment.isExternal()
 							&& fragment.getRawBuildpathEntry() == null) {
 						fragmentsToRefresh.add(fragment);
 					}
 				} catch (ModelException e1) {
 					if (DLTKCore.DEBUG) {
 						e1.printStackTrace();
 					}
 				}
 				break;
 			case IModelElement.SCRIPT_PROJECT:
 				ScriptProject scriptProject = (ScriptProject) element;
 				if (!DLTKLanguageManager.hasScriptNature(scriptProject
 						.getProject())) {
 					// project is not accessible or has lost its script
 					// nature
 					break;
 				}
 				IEnvironment environment = EnvironmentManager
 						.getEnvironment(scriptProject);
 				if (environment != null && !environment.isConnected()) {
 					break; // Project environment is not connected.
 				}
 				try {
 					IProjectFragment[] fragments = scriptProject
 							.getProjectFragments();
 					for (int i = 0; i < fragments.length; i++) {
 						if (fragments[i].isExternal()
 								&& fragments[i].getRawBuildpathEntry() == null) {
 							fragmentsToRefresh.add(fragments[i]);
 						}
 					}
 					fragments = scriptProject.getAllProjectFragments();
 					for (int i = 0; i < fragments.length; i++) {
 						if (fragments[i].isExternal()
 								&& fragments[i].getRawBuildpathEntry() == null) {
 							fragmentsToRefresh.add(fragments[i]);
 						}
 					}
 				} catch (ModelException e1) {
 					if (DLTKCore.DEBUG) {
 						e1.printStackTrace();
 					}
 				}
 				break;
 			case IModelElement.SCRIPT_MODEL:
 				Iterator<String> projectNames = this.state
 						.getOldScriptProjectNames().iterator();
 				while (projectNames.hasNext()) {
 					String projectName = projectNames.next();
 					IProject project = ResourcesPlugin.getWorkspace().getRoot()
 							.getProject(projectName);
 					if (!DLTKLanguageManager.hasScriptNature(project)) {
 						// project is not accessible or has lost its Script
 						// nature
 						continue;
 					}
 					scriptProject = (ScriptProject) DLTKCore.create(project);
 					try {
 						IProjectFragment[] fragments = scriptProject
 								.getProjectFragments();
 						for (int i = 0; i < fragments.length; i++) {
 							if (fragments[i].isExternal()
 									&& fragments[i].getRawBuildpathEntry() == null) {
 								fragmentsToRefresh.add(fragments[i]);
 							}
 						}
 					} catch (ModelException e1) {
 						if (DLTKCore.DEBUG) {
 							e1.printStackTrace();
 						}
 					}
 				}
 				break;
 			}
 		}
 		// perform refresh
 		Iterator<String> projectNames = this.state.getOldScriptProjectNames()
 				.iterator();
 		IWorkspaceRoot wksRoot = ResourcesPlugin.getWorkspace().getRoot();
 		while (projectNames.hasNext()) {
 			if (monitor != null && monitor.isCanceled()) {
 				break;
 			}
 			String projectName = projectNames.next();
 			IProject project = wksRoot.getProject(projectName);
 			if (!DLTKLanguageManager.hasScriptNature(project)) {
 				// project is not accessible or has lost its Script nature
 				continue;
 			}
 			ScriptProject scriptProject = (ScriptProject) DLTKCore
 					.create(project);
 			IProjectFragment[] fragments;
 			Set<IProjectFragment> fragmentsSet = new HashSet<IProjectFragment>();
 			Set<IProjectFragment> fragmentsSetOld = new HashSet<IProjectFragment>();
 			;
 			try {
 				fragmentsSetOld.addAll(Arrays.asList(scriptProject
 						.getProjectFragments()));
 				fragmentsSet.addAll(Arrays.asList(scriptProject
 						.getAllProjectFragments()));
 				fragmentsSet.addAll(fragmentsSetOld);
 				fragments = fragmentsSet
 						.toArray(new IProjectFragment[fragmentsSet.size()]);
 				for (int i = 0; i < fragments.length; i++) {
 					if (!fragmentsToRefresh.contains(fragments[i])) {
 						continue;
 					}
 					IProjectFragment fragment = fragments[i];
 					if (fragment instanceof IProjectFragmentTimestamp) {
 						Long oldTimestamp = (Long) this.state
 								.getCustomTimeStamps().get(fragment.getPath());
 						long newTimeStamp = ((IProjectFragmentTimestamp) fragment)
 								.getTimeStamp();
 						boolean old = fragmentsSetOld.contains(fragment);
 						if (oldTimestamp == null || oldTimestamp == 0
 								|| old == false) {
 							if (newTimeStamp != 0) {
 								/**
 								 * This is new element
 								 **/
 								this.state.getCustomTimeStamps().put(
 										fragment.getPath(),
 										new Long(newTimeStamp));
 								// index new library
 								ProjectIndexerManager.indexProjectFragment(
 										scriptProject, fragment.getPath());
 								if (fragment instanceof Openable) {
 									this.elementAdded((Openable) fragment,
 											null, null);
 								}
 								hasDelta = true;
 							}
 						} else {
 							if (newTimeStamp == 0) {
 								this.state.getCustomTimeStamps().remove(
 										fragment.getPath());
 								ProjectIndexerManager.removeProjectFragment(
 										scriptProject, fragment.getPath());
 								if (fragment instanceof Openable) {
 									this.elementRemoved((Openable) fragment,
 											null, null);
 								}
 								hasDelta = true;
 							} else if (oldTimestamp.longValue() != newTimeStamp) {
 								this.state.getCustomTimeStamps().put(
 										fragment.getPath(),
 										new Long(newTimeStamp));
 								// index new library
 								ProjectIndexerManager.indexProjectFragment(
 										scriptProject, fragment.getPath());
 								if (fragment instanceof Openable) {
 									this.contentChanged((Openable) fragment);
 								}
 								hasDelta = true;
 							}
 						}
 					}
 				}
 			} catch (ModelException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return hasDelta;
 	}
 
 	/*
 	 * Returns the root info for the given path. Look in the old roots table if
 	 * kind is REMOVED.
 	 */
 	private RootInfo rootInfo(IPath path, int kind) {
 		if (kind == IResourceDelta.REMOVED) {
 			return (RootInfo) this.state.oldRoots.get(path);
 		}
 		return (RootInfo) this.state.roots.get(path);
 	}
 
 	/*
 	 * Turns the firing mode to on. That is, deltas that are/have been
 	 * registered will be fired.
 	 */
 	private void startDeltas() {
 		this.isFiring = true;
 	}
 
 	/*
 	 * Turns the firing mode to off. That is, deltas that are/have been
 	 * registered will not be fired until deltas are started again.
 	 */
 	private void stopDeltas() {
 		this.isFiring = false;
 	}
 
 	/*
 	 * Converts an <code>IResourceDelta</code> and its children into the
 	 * corresponding <code>IModelElementDelta</code>s.
 	 */
 	private void traverseDelta(IResourceDelta delta, int elementType,
 			RootInfo rootInfo) {
 
 		IResource res = delta.getResource();
 
 		// set stack of elements
 		if (this.currentElement == null && rootInfo != null) {
 			this.currentElement = rootInfo.project;
 		}
 		// process current delta
 		boolean processChildren = true;
 		if (res instanceof IProject) {
 			// reset source element parser cache
 			// this.sourceElementParserCache = null;
 			processChildren = this
 					.updateCurrentDeltaAndIndex(
 							delta,
 							elementType == IModelElement.PROJECT_FRAGMENT ? IModelElement.SCRIPT_PROJECT
 									: // case
 									// of
 									// prj=src
 									elementType, rootInfo);
 		} else if (rootInfo != null) {
 			processChildren = this.updateCurrentDeltaAndIndex(delta,
 					elementType, rootInfo);
 		} else {
 			// not yet inside a package fragment root
 			processChildren = true;
 		}
 		// process children if needed
 		if (processChildren) {
 			IResourceDelta[] children = delta.getAffectedChildren();
 			boolean oneChildOnBuildpath = false;
 			int length = children.length;
 			IResourceDelta[] orphanChildren = null;
 			Openable parent = null;
 			boolean isValidParent = true;
 			for (int i = 0; i < length; i++) {
 				IResourceDelta child = children[i];
 				IResource childRes = child.getResource();
 				// find out whether the child is a package fragment root of the
 				// current project
 				IPath childPath = childRes.getFullPath();
 				int childKind = child.getKind();
 				RootInfo childRootInfo = this.rootInfo(childPath, childKind);
 				if (childRootInfo != null
 						&& !childRootInfo.isRootOfProject(childPath)) {
 					// package fragment root of another project (dealt with
 					// later)
 					childRootInfo = null;
 				}
 				// compute child type
 				int childType = this.elementType(childRes, childKind,
 						elementType, rootInfo == null ? childRootInfo
 								: rootInfo);
 				boolean isNestedRoot = rootInfo != null
 						&& childRootInfo != null;
 				if (!isNestedRoot) { // do not treat as non-script rsc if
 					// nested root
 					this.traverseDelta(child, childType,
 							rootInfo == null ? childRootInfo : rootInfo);
 					// traverse delta for child in the same project
 					if (childType == NON_SCRIPT_RESOURCE) {
 						if (rootInfo != null) { // if inside a package fragment
 							// root
 							if (!isValidParent) {
 								continue;
 							}
 							if (parent == null) {
 								/*
 								 * find the parent of the non-script resource to
 								 * attach to
 								 */
 								if (this.currentElement == null
 										|| !rootInfo.project
 												.equals(this.currentElement
 														.getScriptProject())) {
 									/*
 									 * note if currentElement is the IModel,
 									 * getScriptProject() is null force the
 									 * currentProject to be used
 									 */
 									this.currentElement = rootInfo.project;
 								}
 								if (elementType == IModelElement.SCRIPT_PROJECT
 										|| (elementType == IModelElement.PROJECT_FRAGMENT && res instanceof IProject)) {
 									// NB: attach non-script resource to project
 									// (not to its package fragment root)
 									parent = rootInfo.project;
 								} else {
 									parent = this.createElement(res,
 											elementType, rootInfo);
 								}
 								if (parent == null) {
 									isValidParent = false;
 									continue;
 								}
 							}
 							// add child as non script resource
 							try {
 								this.nonScriptResourcesChanged(parent, child);
 							} catch (ModelException e) {
 								// ignore
 							}
 						} else {
 							// the non-script resource (or its parent folder)
 							// will
 							// be attached to the dltk project
 							if (orphanChildren == null) {
 								orphanChildren = new IResourceDelta[length];
 							}
 							orphanChildren[i] = child;
 						}
 					} else {
 						oneChildOnBuildpath = true;
 					}
 				} else {
 					oneChildOnBuildpath = true; // to avoid reporting child
 					// delta as non-script resource
 					// delta
 				}
 				// if child is a nested root
 				// or if it is not a package fragment root of the current
 				// project
 				// but it is a package fragment root of another project,
 				// traverse delta too
 				if (isNestedRoot
 						|| (childRootInfo == null && (childRootInfo = this
 								.rootInfo(childPath, childKind)) != null)) {
 					this.traverseDelta(child, IModelElement.PROJECT_FRAGMENT,
 							childRootInfo); // binary
 					// output
 					// of
 					// childRootInfo.project
 					// cannot
 					// be
 					// this
 					// root
 				}
 				// if the child is a package fragment root of one or several
 				// other projects
 				ArrayList rootList;
 				if ((rootList = this.otherRootsInfo(childPath, childKind)) != null) {
 					Iterator iterator = rootList.iterator();
 					while (iterator.hasNext()) {
 						childRootInfo = (RootInfo) iterator.next();
 						this.traverseDelta(child,
 								IModelElement.PROJECT_FRAGMENT, childRootInfo); // binary
 						// output
 						// of
 						// childRootInfo.project
 						// cannot
 						// be
 						// this
 						// root
 					}
 				}
 			}
 			if (orphanChildren != null && (oneChildOnBuildpath // orphan
 					// children are
 					// siblings of a
 					// package
 					// fragment root
 					|| res instanceof IProject)) { // non-script resource
 				// directly under a project
 				// attach orphan children
 				IProject rscProject = res.getProject();
 				ScriptProject adoptiveProject = (ScriptProject) DLTKCore
 						.create(rscProject);
 				if (adoptiveProject != null
 						&& DLTKLanguageManager.hasScriptNature(rscProject)) { // delta
 					// iff
 					// script
 					// project
 					// (18698)
 					for (int i = 0; i < length; i++) {
 						if (orphanChildren[i] != null) {
 							try {
 								this.nonScriptResourcesChanged(adoptiveProject,
 										orphanChildren[i]);
 							} catch (ModelException e) {
 								// ignore
 							}
 						}
 					}
 				}
 			} // else resource delta will be added by parent
 		} // else resource delta will be added by parent
 	}
 
 	private void validateBuildpaths(IResourceDelta delta,
 			HashSet<IPath> affectedProjects) {
 		IResource resource = delta.getResource();
 		boolean processChildren = false;
 		switch (resource.getType()) {
 		case IResource.ROOT:
 			if (delta.getKind() == IResourceDelta.CHANGED) {
 				processChildren = true;
 			}
 			break;
 		case IResource.PROJECT:
 			IProject project = (IProject) resource;
 			int kind = delta.getKind();
 			boolean isDylanProject = DLTKLanguageManager
 					.hasScriptNature(project);
 			switch (kind) {
 			case IResourceDelta.ADDED:
 				processChildren = isDylanProject;
 				affectedProjects.add(project.getFullPath());
 				break;
 			case IResourceDelta.CHANGED:
 				processChildren = isDylanProject;
 				if ((delta.getFlags() & IResourceDelta.OPEN) != 0) {
 					// project opened or closed
 					if (isDylanProject) {
 						ScriptProject scriptProject = (ScriptProject) DLTKCore
 								.create(project);
 						this.state.addBuildpathValidation(scriptProject);
 						// in case .buildpath got modified while closed
 					}
 					affectedProjects.add(project.getFullPath());
 				} else if ((delta.getFlags() & IResourceDelta.DESCRIPTION) != 0) {
 					boolean wasScriptProject = this.state.findProject(project
 							.getName()) != null;
 					if (wasScriptProject != isDylanProject) {
 						// project gained or lost script nature
 						ScriptProject scriptProject = (ScriptProject) DLTKCore
 								.create(project);
 						// add/remove buildpath markers
 						this.state.addBuildpathValidation(scriptProject);
 						affectedProjects.add(project.getFullPath());
 					}
 				}
 				break;
 			case IResourceDelta.REMOVED:
 				affectedProjects.add(project.getFullPath());
 				break;
 			}
 			break;
 		case IResource.FILE:
 			/* check buildpath file change */
 			IFile file = (IFile) resource;
 			if (file.getName().equals(ScriptProject.BUILDPATH_FILENAME)) {
 				ScriptProject scriptProject = (ScriptProject) DLTKCore
 						.create(file.getProject());
 				this.state.addBuildpathValidation(scriptProject);
 				affectedProjects.add(file.getProject().getFullPath());
 				break;
 			}
 			break;
 		}
 		if (processChildren) {
 			IResourceDelta[] children = delta.getAffectedChildren();
 			for (int i = 0; i < children.length; i++) {
 				validateBuildpaths(children[i], affectedProjects);
 			}
 		}
 	}
 
 	/*
 	 * Validate the buildpaths of the projects affected by the given delta.
 	 * Create markers if necessary. Returns whether cycle markers should be
 	 * recomputed.
 	 */
 	private boolean validateBuildpaths(IResourceDelta delta) {
 		HashSet<IPath> affectedProjects = new HashSet<IPath>(5);
 		validateBuildpaths(delta, affectedProjects);
 		boolean needCycleValidation = false;
 		// validate buildpaths of affected projects (dependent projects
 		// or projects that reference a library in one of the projects that have
 		// changed)
 		if (!affectedProjects.isEmpty()) {
 			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace()
 					.getRoot();
 			IProject[] projects = workspaceRoot.getProjects();
 			int length = projects.length;
 			for (int i = 0; i < length; i++) {
 				IProject project = projects[i];
 				ScriptProject scriptProject = (ScriptProject) DLTKCore
 						.create(project);
 				try {
 					IPath projectPath = project.getFullPath();
 					IBuildpathEntry[] buildpath = scriptProject
 							.getResolvedBuildpath();
 					/*
 					 * allowed to reuse model cache
 					 */
 					for (int j = 0, cpLength = buildpath.length; j < cpLength; j++) {
 						IBuildpathEntry entry = buildpath[j];
 						switch (entry.getEntryKind()) {
 						case IBuildpathEntry.BPE_PROJECT:
 							if (affectedProjects.contains(entry.getPath())) {
 								this.state
 										.addBuildpathValidation(scriptProject);
 								needCycleValidation = true;
 							}
 							break;
 						case IBuildpathEntry.BPE_LIBRARY:
 							IPath entryPath = entry.getPath();
 							IPath libProjectPath = entryPath
 									.removeLastSegments(entryPath
 											.segmentCount() - 1);
 							if (!libProjectPath.equals(projectPath)
 							/*
 							 * if library contained in another project
 							 */
 							&& affectedProjects.contains(libProjectPath)) {
 								this.state
 										.addBuildpathValidation(scriptProject);
 							}
 							break;
 						}
 					}
 				} catch (ModelException e) {
 					// project no longer exist
 				}
 			}
 		}
 		return needCycleValidation;
 	}
 
 	/*
 	 * Update the current delta (ie. add/remove/change the given element) and
 	 * update the correponding index. Returns whether the children of the given
 	 * delta must be processed. @throws a ModelException if the delta doesn't
 	 * correspond to a model element of the given type.
 	 */
 	public boolean updateCurrentDeltaAndIndex(IResourceDelta delta,
 			int elementType, RootInfo rootInfo) {
 		Openable element;
 		switch (delta.getKind()) {
 		case IResourceDelta.ADDED:
 			IResource deltaRes = delta.getResource();
 			element = this.createElement(deltaRes, elementType, rootInfo);
 			if (element == null) {
 				// resource might be containing shared roots (see bug 19058)
 				this.state.updateRoots(deltaRes.getFullPath(), delta, this);
 				return rootInfo != null && rootInfo.inclusionPatterns != null;
 			}
 			this.updateIndex(element, delta);
 			this.elementAdded(element, delta, rootInfo);
 			if (elementType == IModelElement.PROJECT_FRAGMENT) {
 				this.state.addBuildpathValidation(rootInfo.project);
 			}
 			return elementType == IModelElement.SCRIPT_FOLDER;
 		case IResourceDelta.REMOVED:
 			deltaRes = delta.getResource();
 			element = this.createElement(deltaRes, elementType, rootInfo);
 			if (element == null) {
 				// resource might be containing shared roots (see bug 19058)
 				this.state.updateRoots(deltaRes.getFullPath(), delta, this);
 				return rootInfo != null && rootInfo.inclusionPatterns != null;
 			}
 			this.updateIndex(element, delta);
 			this.elementRemoved(element, delta, rootInfo);
 			if (elementType == IModelElement.PROJECT_FRAGMENT) {
 				this.state.addBuildpathValidation(rootInfo.project);
 			}
 
 			if (deltaRes.getType() == IResource.PROJECT) {
 				// reset the corresponding project built state, since cannot
 				// reuse if added back
 				if (DLTKCore.DEBUG) {
 					System.out
 							.println("Clearing last state for removed project : " + deltaRes); //$NON-NLS-1$
 				}
 				this.manager.setLastBuiltState((IProject) deltaRes, null /*
 																		 * no
 																		 * state
 																		 */);
 				// clean up previous session containers (see
 				// https://bugs.eclipse.org/bugs/show_bug.cgi?id=89850)
 				this.manager.previousSessionContainers.remove(element);
 			}
 			return elementType == IModelElement.SCRIPT_FOLDER;
 		case IResourceDelta.CHANGED:
 			int flags = delta.getFlags();
 			if ((flags & IResourceDelta.CONTENT) != 0
 					|| (flags & IResourceDelta.ENCODING) != 0) {
 				// content or encoding has changed
 				element = this.createElement(delta.getResource(), elementType,
 						rootInfo);
 				if (element == null) {
 					return false;
 				}
 				this.updateIndex(element, delta);
 				this.contentChanged(element);
 			} else if (elementType == IModelElement.SCRIPT_PROJECT) {
 				if ((flags & IResourceDelta.OPEN) != 0) {
 					// project has been opened or closed
 					final IProject res = (IProject) delta.getResource();
 					element = this.createElement(res, elementType, rootInfo);
 					if (element == null) {
 						// resource might be containing shared roots (see
 						// bug
 						// 19058)
 						this.state.updateRoots(res.getFullPath(), delta, this);
 						return false;
 					}
 					if (res.isOpen()) {
 						if (DLTKLanguageManager.hasScriptNature(res)) {
 							this.addToParentInfo(element);
 							this.currentDelta().opened(element);
 							this.state.updateRoots(element.getPath(), delta,
 									this);
 							// refresh pkg fragment roots and caches of the
 							// project (and its dependents)
 							final IScriptProject project = (IScriptProject) element;
 							this.rootsToRefresh.add(project);
 							this.projectCachesToReset.add(project);
 							this.postActions.add(new Runnable() {
 								public void run() {
 									ProjectIndexerManager.indexProject(res);
 								}
 							});
 						}
 					} else {
 						boolean wasDylanProject = this.state.findProject(res
 								.getName()) != null;
 						if (wasDylanProject) {
 							this.close(element);
 							this.removeFromParentInfo(element);
 							this.currentDelta().closed(element);
 							this.manager.indexManager.discardJobs(element
 									.getElementName());
 							final IPath projectPath = res.getFullPath();
 							this.manager.indexManager
 									.removeIndexFamily(projectPath);
 							ProjectIndexerManager.removeProject(projectPath);
 						}
 					}
 					return false; // when a project is open/closed don't
 					// process children
 				}
 				if ((flags & IResourceDelta.DESCRIPTION) != 0) {
 					IProject res = (IProject) delta.getResource();
 					boolean wasDylanProject = this.state.findProject(res
 							.getName()) != null;
 					boolean isDylanProject = DLTKLanguageManager
 							.hasScriptNature(res);
 					if (wasDylanProject != isDylanProject) {
 						// project's nature has been added or removed
 						element = this
 								.createElement(res, elementType, rootInfo);
 						if (element == null) {
 							return false; // note its resources are still
 						}
 						// visible as roots to other
 						// projects
 						if (isDylanProject) {
 							this.elementAdded(element, delta, rootInfo);
 							ProjectIndexerManager.indexProject(res);
 						} else {
 							this.elementRemoved(element, delta, rootInfo);
 							this.manager.indexManager.discardJobs(element
 									.getElementName());
 							final IPath projectPath = res.getFullPath();
 							this.manager.indexManager
 									.removeIndexFamily(projectPath);
 							ProjectIndexerManager.removeProject(projectPath);
 							// reset the corresponding project built state,
 							// since cannot reuse if added back
 							if (DLTKCore.DEBUG) {
 								System.out
 										.println("Clearing last state for project loosing script nature: " + res); //$NON-NLS-1$
 							}
 							this.manager.setLastBuiltState(res, null /*
 																	 * no state
 																	 */);
 						}
 						return false; // when a project's nature is
 						// added/removed don't process children
 					}
 				}
 			}
 			return true;
 		}
 		return true;
 	}
 
 	private void updateIndex(Openable element, IResourceDelta delta) {
 		IndexManager indexManager = this.manager.indexManager;
 		if (indexManager == null) {
 			return;
 		}
 		switch (element.getElementType()) {
 		case IModelElement.SCRIPT_PROJECT:
 			switch (delta.getKind()) {
 			case IResourceDelta.ADDED:
 				final IScriptProject scriptProject = element.getScriptProject();
 				this.postActions.add(new Runnable() {
 					public void run() {
 						ProjectIndexerManager.indexProject(scriptProject);
 					}
 				});
 				break;
 			case IResourceDelta.REMOVED:
 				final IPath projectPath = element.getScriptProject()
 						.getProject().getFullPath();
 				indexManager.removeIndexFamily(projectPath);
 				ProjectIndexerManager.removeProject(projectPath);
 				// NB: Discarding index jobs belonging to this project
 				// was done
 				// during PRE_DELETE
 				break;
 			// NB: Update of index if project is opened, closed, or its
 			// script
 			// nature is added or removed
 			// is done in updateCurrentDeltaAndIndex
 			}
 			break;
 		case IModelElement.PROJECT_FRAGMENT:
 			if (element instanceof ArchiveProjectFragment
 					|| element instanceof ExternalProjectFragment) {
 				IProjectFragment root = (IProjectFragment) element;
 				// index External or zip fragment only once (if the root is
 				// in its declaring
 				// project)
 				IPath jarPath = root.getPath();
 				BuildpathEntry buildpathEntry;
 				char[][] fullInclusionPatternChars = null;
 				char[][] fullExclusionPatternChars = null;
 				try {
 					if (root instanceof ProjectFragment) {
 						buildpathEntry = (BuildpathEntry) ((ProjectFragment) root)
 								.getBuildpathEntry();
 					} else {
 						buildpathEntry = (BuildpathEntry) root
 								.getRawBuildpathEntry();
 					}
 					fullInclusionPatternChars = buildpathEntry
 							.fullInclusionPatternChars();
 					fullExclusionPatternChars = buildpathEntry
 							.fullExclusionPatternChars();
 				} catch (ModelException e) {
 					if (DLTKCore.DEBUG) {
 						e.printStackTrace();
 					}
 				}
 				final IScriptProject scriptProject = root.getScriptProject();
 				switch (delta.getKind()) {
 				case IResourceDelta.ADDED:
 					// index the new jar
 					ProjectIndexerManager.indexLibrary(scriptProject, jarPath);
 					break;
 				case IResourceDelta.CHANGED:
 					// first remove the index so that it is forced to be
 					// re-indexed
 					indexManager.removeIndex(jarPath);
 					// then index the jar
 					ProjectIndexerManager.indexLibrary(scriptProject, jarPath);
 					break;
 				case IResourceDelta.REMOVED:
 					// the jar was physically removed: remove the index
 					indexManager.discardJobs(jarPath.toString());
 					indexManager.removeIndex(jarPath);
 					ProjectIndexerManager.removeLibrary(scriptProject, jarPath);
 					break;
 				}
 				break;
 			}
 			int kind = delta.getKind();
 			if (kind == IResourceDelta.ADDED || kind == IResourceDelta.REMOVED) {
 				IProjectFragment root = (IProjectFragment) element;
 				this.updateRootIndex(root, Path.EMPTY, delta);
 				break;
 			}
 			// don't break as packages of the package fragment root can be
 			// indexed below
 		case IModelElement.SCRIPT_FOLDER:
 			switch (delta.getKind()) {
 			case IResourceDelta.ADDED:
 			case IResourceDelta.REMOVED:
 				IScriptFolder pkg = null;
 				if (element instanceof IProjectFragment) {
 					IProjectFragment root = (IProjectFragment) element;
 					pkg = root.getScriptFolder(Path.EMPTY);
 				} else {
 					pkg = (IScriptFolder) element;
 				}
 				RootInfo rootInfo = this.rootInfo(pkg.getParent().getPath(),
 						delta.getKind());
 				boolean isSource = rootInfo == null // if null, defaults
 						// to
 						// source
 						|| rootInfo.entryKind == IBuildpathEntry.BPE_SOURCE;
 				IResourceDelta[] children = delta.getAffectedChildren();
 				for (int i = 0, length = children.length; i < length; i++) {
 					IResourceDelta child = children[i];
 					IResource resource = child.getResource();
 					// TODO (philippe) Why do this? Every child is added
 					// anyway
 					// as the delta is walked
 					if (resource instanceof IFile) {
 						String name = resource.getName();
 						if (isSource) {
 							if (Util.isValidSourceModule(pkg, resource)) {
 								Openable cu = (Openable) pkg
 										.getSourceModule(name);
 								this.updateIndex(cu, child);
 							}
 						}
 					}
 				}
 				break;
 			}
 			break;
 		case IModelElement.BINARY_MODULE:
 			IFile file = (IFile) delta.getResource();
 			IPath binaryFolderPath = element.getProjectFragment().getPath();
 			switch (delta.getKind()) {
 			case IResourceDelta.CHANGED:
 				// no need to index if the content has not changed
 				int flags = delta.getFlags();
 				if ((flags & IResourceDelta.CONTENT) == 0
 						&& (flags & IResourceDelta.ENCODING) == 0) {
 					break;
 				}
 				// case IResourceDelta.ADDED:
 				// indexManager.addExternal(file, binaryFolderPath);
 				// break;
 			case IResourceDelta.REMOVED:
 				String containerRelativePath = Util.relativePath(file
 						.getFullPath(), binaryFolderPath.segmentCount());
 				indexManager.remove(containerRelativePath, binaryFolderPath);
 				break;
 			}
 			break;
 		case IModelElement.SOURCE_MODULE:
 			file = (IFile) delta.getResource();
 			switch (delta.getKind()) {
 			case IResourceDelta.CHANGED:
 				// no need to index if the content has not changed
 				int flags = delta.getFlags();
 				if ((flags & IResourceDelta.CONTENT) == 0
 						&& (flags & IResourceDelta.ENCODING) == 0) {
 					break;
 				}
 			case IResourceDelta.ADDED:
 				if (ProjectIndexerManager.isIndexerEnabled(file.getProject())) {
 					IDLTKLanguageToolkit toolkit = null;
 					toolkit = DLTKLanguageManager.getLanguageToolkit(element);
 					ProjectIndexerManager.indexSourceModule(
 							(ISourceModule) element, toolkit);
 					if (DLTKCore.DEBUG) {
 						System.err
 								.println("update index: some actions are required to perform here...."); //$NON-NLS-1$
 					}
 				}
 				// Clean file from secondary types cache but do not
 				// update
 				// indexing secondary type cache as it will be updated
 				// through
 				// indexing itself
 				// this.manager.secondaryTypesRemoving(file, false);
 				break;
 			case IResourceDelta.REMOVED:
 				final IProject project = file.getProject();
 				/* remove project segment */
 				final String path = Util.relativePath(file.getFullPath(), 1);
 				indexManager.remove(path, project.getFullPath());
 				ProjectIndexerManager.removeSourceModule(DLTKCore
 						.create(project), path);
 				// Clean file from secondary types cache and update
 				// indexing
 				// secondary type cache as indexing cannot remove
 				// secondary
 				// types from cache
 				// this.manager.secondaryTypesRemoving(file, true);
 				// System.err
 				// .println("this.manager.secondaryTypesRemoving(file, true);");
 				break;
 			}
 		}
 	}
 
 	private ISourceElementParser getSourceElementParser(Openable element) {
 		if (this.sourceElementParserCache == null) {
 			this.sourceElementParserCache = this.manager.indexManager
 					.getSourceElementParser(element.getScriptProject());
 		}
 		return this.sourceElementParserCache;
 	}
 
 	private SourceIndexerRequestor getSourceRequestor(Openable element) {
 		if (this.sourceRequestorCache == null) {
 			this.sourceRequestorCache = this.manager.indexManager
 					.getSourceRequestor(element.getScriptProject());
 		}
 		return this.sourceRequestorCache;
 	}
 
 	/*
 	 * Update Model given some delta
 	 */
 	public void updateModel(IModelElementDelta customDelta) {
 		if (customDelta == null) {
 			for (int i = 0, length = this.modelDeltas.size(); i < length; i++) {
 				IModelElementDelta delta = this.modelDeltas.get(i);
 				this.modelUpdater.processDelta(delta);
 			}
 		} else {
 			this.modelUpdater.processDelta(customDelta);
 		}
 	}
 
 	/*
 	 * Updates the index of the given root (assuming it's an addition or a
 	 * removal). This is done recusively, pkg being the current package.
 	 */
 	private void updateRootIndex(IProjectFragment root, IPath pkgPath,
 			IResourceDelta delta) {
 		Openable pkg = (Openable) root.getScriptFolder(pkgPath);
 		this.updateIndex(pkg, delta);
 		IResourceDelta[] children = delta.getAffectedChildren();
 		for (int i = 0, length = children.length; i < length; i++) {
 			IResourceDelta child = children[i];
 			IResource resource = child.getResource();
 			if (resource instanceof IFolder) {
 				this.updateRootIndex(root, pkgPath.append(resource.getName()),
 						child);
 			}
 		}
 	}
 
	public void clearCustomTimestampsFor(IModelElement[] elements) {
 		if (state == null) {
 			return;
 		}
 		for (IModelElement e : elements) {
 			state.customTimeStamps.getTimestamps().remove(e);
 
 		}
 	}
 
 	// /*
 	// * Check whether .buildpath files are affected by the given delta.
 	// * Creates/removes problem markers if needed. Remember the affected
 	// projects
 	// * in the given set.
 	// */
 	// private void updateBuildpathMarkers(IResourceDelta delta,
 	// HashSet affectedProjects, Map preferredBuildpaths) {
 	// IResource resource = delta.getResource();
 	// boolean processChildren = false;
 	//
 	// switch (resource.getType()) {
 	//
 	// case IResource.ROOT:
 	// if (delta.getKind() == IResourceDelta.CHANGED) {
 	// processChildren = true;
 	// }
 	// break;
 	// case IResource.PROJECT:
 	// IProject project = (IProject) resource;
 	// int kind = delta.getKind();
 	// boolean isScriptProject = DLTKLanguageManager
 	// .hasScriptNature(project);
 	// switch (kind) {
 	// case IResourceDelta.ADDED:
 	// processChildren = isScriptProject;
 	// affectedProjects.add(project.getFullPath());
 	// break;
 	// case IResourceDelta.CHANGED:
 	// processChildren = isScriptProject;
 	// if ((delta.getFlags() & IResourceDelta.OPEN) != 0) {
 	// // project opened or closed: remember project and its
 	// // dependents
 	// affectedProjects.add(project.getFullPath());
 	// if (isScriptProject) {
 	// ScriptProject scriptProject = (ScriptProject) DLTKCore
 	// .create(project);
 	// scriptProject.updateBuildpathMarkers(preferredBuildpaths); // in
 	// // case
 	// // .buildpath
 	// // got
 	// // modified
 	// // while
 	// // closed
 	// }
 	// } else if ((delta.getFlags() & IResourceDelta.DESCRIPTION) != 0) {
 	// boolean wasScriptProject = this.state.findProject(project
 	// .getName()) != null;
 	// if (wasScriptProject && !isScriptProject) {
 	// // project no longer has Script nature, discard Script
 	// // related obsolete markers
 	// affectedProjects.add(project.getFullPath());
 	// // flush buildpath markers
 	// ScriptProject scriptProject = (ScriptProject) DLTKCore
 	// .create(project);
 	// scriptProject.flushBuildpathProblemMarkers(true, // flush
 	// // cycle
 	// // markers
 	// true // flush buildpath format markers
 	// );
 	//
 	// // remove problems and tasks created by the builder
 	// ScriptBuilder.removeProblemsAndTasksFor(project);
 	// }
 	// } else if (isScriptProject) {
 	// // check if all entries exist
 	// try {
 	// ScriptProject scriptProject = (ScriptProject) DLTKCore
 	// .create(project);
 	// scriptProject.getResolvedBuildpath(
 	// true/* ignoreUnresolvedEntry */,
 	// true/* generateMarkerOnError */, false/*
 	// * don't
 	// * returnResolutionInProgress
 	// */);
 	// } catch (ModelException e) {
 	// // project doesn't exist: ignore
 	// }
 	// }
 	// break;
 	// case IResourceDelta.REMOVED:
 	// affectedProjects.add(project.getFullPath());
 	// break;
 	// }
 	// break;
 	// case IResource.FILE:
 	// /* check buildpath file change */
 	// IFile file = (IFile) resource;
 	// if (file.getName().equals(ScriptProject.BUILDPATH_FILENAME)) {
 	// affectedProjects.add(file.getProject().getFullPath());
 	// ScriptProject scriptProject = (ScriptProject) DLTKCore.create(file
 	// .getProject());
 	// scriptProject.updateBuildpathMarkers(preferredBuildpaths);
 	// break;
 	// }
 	// // /* check custom preference file change */
 	// // if (file.getName().equals(ScriptProject.PREF_FILENAME)) {
 	// // reconcilePreferenceFileUpdate(delta, file, project);
 	// // break;
 	// // }
 	// break;
 	// }
 	// if (processChildren) {
 	// IResourceDelta[] children = delta.getAffectedChildren();
 	// for (int i = 0; i < children.length; i++) {
 	// updateBuildpathMarkers(children[i], affectedProjects,
 	// preferredBuildpaths);
 	// }
 	// }
 	// }
 
 	// /*
 	// * Update the .buildpath format, missing entries and cycle markers for the
 	// * projects affected by the given delta.
 	// */
 	// private void updateBuildpathMarkers(IResourceDelta delta,
 	// DeltaProcessingState.ProjectUpdateInfo[] updates) {
 	//
 	// Map preferredBuildpaths = new HashMap(5);
 	// HashSet affectedProjects = new HashSet(5);
 	//
 	// // read .buildpath files that have changed, and create markers if format
 	// // is wrong or if an entry cannot be found
 	// Model.flushExternalFileCache();
 	// updateBuildpathMarkers(delta, affectedProjects, preferredBuildpaths);
 	//
 	// // update .buildpath format markers for affected projects (dependent
 	// // projects
 	// // or projects that reference a library in one of the projects that have
 	// // changed)
 	// if (!affectedProjects.isEmpty()) {
 	// IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace()
 	// .getRoot();
 	// IProject[] projects = workspaceRoot.getProjects();
 	// int length = projects.length;
 	// for (int i = 0; i < length; i++) {
 	// IProject project = projects[i];
 	// ScriptProject scriptProject = (ScriptProject) DLTKCore
 	// .create(project);
 	// if (preferredBuildpaths.get(scriptProject) == null) { // not
 	// // already
 	// // updated
 	// try {
 	// IPath projectPath = project.getFullPath();
 	// IBuildpathEntry[] buildpath = scriptProject
 	// .getResolvedBuildpath(
 	// true/* ignoreUnresolvedEntry */,
 	// false/* don't generateMarkerOnError */,
 	// false/*
 	// * don't
 	// * returnResolutionInProgress
 	// */); // allowed
 	// // to
 	// // reuse
 	// // model
 	// // cache
 	// for (int j = 0, cpLength = buildpath.length; j < cpLength; j++) {
 	// IBuildpathEntry entry = buildpath[j];
 	// switch (entry.getEntryKind()) {
 	// case IBuildpathEntry.BPE_PROJECT:
 	// if (affectedProjects.contains(entry.getPath())) {
 	// scriptProject.updateBuildpathMarkers(null);
 	// }
 	// break;
 	// case IBuildpathEntry.BPE_LIBRARY:
 	// IPath entryPath = entry.getPath();
 	// IPath libProjectPath = entryPath
 	// .removeLastSegments(entryPath
 	// .segmentCount() - 1);
 	// if (!libProjectPath.equals(projectPath) // if
 	// // library
 	// // contained
 	// // in
 	// // another
 	// // project
 	// && affectedProjects
 	// .contains(libProjectPath)) {
 	// scriptProject.updateBuildpathMarkers(null);
 	// }
 	// break;
 	// }
 	// }
 	// } catch (ModelException e) {
 	// // project no longer exists
 	// }
 	// }
 	// }
 	// }
 	// if (!affectedProjects.isEmpty() || updates != null) {
 	// // update all cycle markers since the given delta may have affected
 	// // cycles
 	// if (updates != null) {
 	// for (int i = 0, length = updates.length; i < length; i++) {
 	// DeltaProcessingState.ProjectUpdateInfo info = updates[i];
 	// if (!preferredBuildpaths.containsKey(info.project))
 	// preferredBuildpaths.put(info.project,
 	// info.newResolvedPath);
 	// }
 	// }
 	// try {
 	// ScriptProject.updateAllCycleMarkers(preferredBuildpaths);
 	// } catch (ModelException e) {
 	// // project no longer exist
 	// }
 	// }
 	// }
 
 }
