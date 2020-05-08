 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.core;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 import java.util.zip.ZipFile;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.ISaveContext;
 import org.eclipse.core.resources.ISaveParticipant;
 import org.eclipse.core.resources.ISavedState;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.core.runtime.Preferences;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.core.runtime.preferences.DefaultScope;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.core.runtime.preferences.IPreferencesService;
 import org.eclipse.core.runtime.preferences.IScopeContext;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.dltk.compiler.problem.IProblem;
 import org.eclipse.dltk.compiler.util.HashtableOfObjectToInt;
 import org.eclipse.dltk.core.BuildpathContainerInitializer;
 import org.eclipse.dltk.core.DLTKContentTypeManager;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IAccessRule;
 import org.eclipse.dltk.core.IBuildpathAttribute;
 import org.eclipse.dltk.core.IBuildpathContainer;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IBuiltinModuleProvider;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelStatus;
 import org.eclipse.dltk.core.IParent;
 import org.eclipse.dltk.core.IProblemRequestor;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.IProjectFragmentTimestamp;
 import org.eclipse.dltk.core.IScriptFolder;
 import org.eclipse.dltk.core.IScriptModel;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ISourceModuleInfoCache;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.WorkingCopyOwner;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.dltk.core.search.indexing.IndexManager;
 import org.eclipse.dltk.internal.core.builder.ScriptBuilder;
 import org.eclipse.dltk.internal.core.search.DLTKWorkspaceScope;
 import org.eclipse.dltk.internal.core.search.ProjectIndexerManager;
 import org.eclipse.dltk.internal.core.util.Messages;
 import org.eclipse.dltk.internal.core.util.Util;
 import org.eclipse.dltk.internal.core.util.WeakHashSet;
 import org.eclipse.osgi.util.NLS;
 import org.osgi.service.prefs.BackingStoreException;
 
 public class ModelManager implements ISaveParticipant {
 	private final static int CONTAINERS_FILE_VERSION = 1;
 	public final static String BP_VARIABLE_PREFERENCES_PREFIX = DLTKCore.PLUGIN_ID
 			+ ".buildpathVariable."; //$NON-NLS-1$
 	public final static String BP_CONTAINER_PREFERENCES_PREFIX = DLTKCore.PLUGIN_ID
 			+ ".buildpathContainer."; //$NON-NLS-1$
 	public final static String BP_USERLIBRARY_PREFERENCES_PREFIX = DLTKCore.PLUGIN_ID
 			+ ".userLibrary."; //$NON-NLS-1$
 	public final static String BP_ENTRY_IGNORE = "##<cp entry ignore>##"; //$NON-NLS-1$
 	public final static IPath BP_ENTRY_IGNORE_PATH = new Path(BP_ENTRY_IGNORE);
 	public final static String TRUE = "true"; //$NON-NLS-1$
 	/**
 	 * Unique handle onto the Model
 	 */
 	final Model model = new Model();
 	/**
 	 * Buildpath variables pool
 	 */
 	public HashMap variables = new HashMap(5);
 	public HashSet variablesWithInitializer = new HashSet(5);
 	public HashSet readOnlyVariables = new HashSet(5);
 	public HashMap previousSessionVariables = new HashMap(5);
 	private ThreadLocal variableInitializationInProgress = new ThreadLocal();
 
 	/**
 	 * Buildpath containers pool
 	 */
 	public HashMap containers = new HashMap(5);
 	public HashMap previousSessionContainers = new HashMap(5);
 	private ThreadLocal containerInitializationInProgress = new ThreadLocal();
 	public boolean batchContainerInitializations = false;
 	public HashMap containerInitializersCache = new HashMap(5);
 	/**
 	 * Special value used for recognizing ongoing initialization and breaking
 	 * initialization cycles
 	 */
 	public final static IPath VARIABLE_INITIALIZATION_IN_PROGRESS = new Path(
 			"Variable Initialization In Progress"); //$NON-NLS-1$
 
 	public final static IBuildpathContainer CONTAINER_INITIALIZATION_IN_PROGRESS = new IBuildpathContainer() {
 		public IBuildpathEntry[] getBuildpathEntries(IScriptProject project) {
 			return null;
 		}
 
 		public String getDescription(IScriptProject project) {
 			return "Container Initialization In Progress";} //$NON-NLS-1$
 
 		public int getKind() {
 			return 0;
 		}
 
 		public IPath getPath() {
 			return null;
 		}
 
 		public String toString() {
 			return getDescription(null);
 		}
 
 		public IBuiltinModuleProvider getBuiltinProvider(IScriptProject project) {
 			return null;
 		}
 	};
 	/*
 	 * A HashSet that contains the IScriptProject whose buildpath is being
 	 * resolved.
 	 */
 	private ThreadLocal buildpathsBeingResolved = new ThreadLocal();
 
 	public static class PerProjectInfo {
 		public IProject project;
 		public Object savedState;
 		public boolean triedRead;
 		public IBuildpathEntry[] rawBuildpath;
 		public IModelStatus rawBuildpathStatus;
 		public IBuildpathEntry[] resolvedBuildpath;
 		public IModelStatus unresolvedEntryStatus;
 		public Map resolvedPathToRawEntries; // reverse map from resolved
 		// path to raw entries
 		public IEclipsePreferences preferences;
 		public Hashtable options;
 
 		public PerProjectInfo(IProject project) {
 			this.triedRead = false;
 			this.savedState = null;
 			this.project = project;
 		}
 
 		public void rememberExternalLibTimestamps() {
 			IBuildpathEntry[] buildpath = this.resolvedBuildpath;
 			if (buildpath == null)
 				return;
 			Map externalTimeStamps = ModelManager.getModelManager().deltaState
 					.getExternalLibTimeStamps();
 			for (int i = 0, length = buildpath.length; i < length; i++) {
 				IBuildpathEntry entry = buildpath[i];
 				if (entry.getEntryKind() == IBuildpathEntry.BPE_LIBRARY) {
 					IPath path = entry.getPath();
 					if (externalTimeStamps.get(path) == null) {
 						Object target = Model.getTarget(ResourcesPlugin
 								.getWorkspace().getRoot(), path, true);
 						if (target instanceof IFileHandle) {
 							long timestamp = DeltaProcessor
 									.getTimeStamp((IFileHandle) target);
 							externalTimeStamps.put(path, new Long(timestamp));
 						}
 					}
 				}
 			}
 			Map customTimeStamps = ModelManager.getModelManager().deltaState
 					.getCustomTimeStamps();
 			// Save custom project fragments timestamps.
 			try {
 				ScriptProject scriptProject = (ScriptProject) DLTKCore
 						.create(project);
 				IProjectFragment[] fragments = scriptProject
 						.getAllProjectFragments();
 				for (int i = 0; i < fragments.length; i++) {
 					if (fragments[i] instanceof IProjectFragmentTimestamp) {
 						IProjectFragmentTimestamp stamp = (IProjectFragmentTimestamp) fragments[i];
 						long timeStamp = stamp.getTimeStamp();
 						customTimeStamps.put(fragments[i].getPath(), new Long(
 								timeStamp));
 					}
 				}
 			} catch (ModelException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		// updating raw buildpath need to flush obsoleted cached information
 		// about resolved entries
 		public synchronized void updateBuildpathInformation(
 				IBuildpathEntry[] newRawBuildpath) {
 			this.rawBuildpath = newRawBuildpath;
 			this.resolvedBuildpath = null;
 			this.resolvedPathToRawEntries = null;
 		}
 
 		public String toString() {
 			StringBuffer buffer = new StringBuffer();
 			buffer.append("Info for "); //$NON-NLS-1$
 			buffer.append(this.project.getFullPath());
 			buffer.append("\nRaw buildpath:\n"); //$NON-NLS-1$
 			if (this.rawBuildpath == null) {
 				buffer.append("  <null>\n"); //$NON-NLS-1$
 			} else {
 				for (int i = 0, length = this.rawBuildpath.length; i < length; i++) {
 					buffer.append("  "); //$NON-NLS-1$
 					buffer.append(this.rawBuildpath[i]);
 					buffer.append('\n');
 				}
 			}
 			buffer.append("Resolved buildpath:\n"); //$NON-NLS-1$
 			IBuildpathEntry[] resolvedCP = this.resolvedBuildpath;
 			if (resolvedCP == null) {
 				buffer.append("  <null>\n"); //$NON-NLS-1$
 			} else {
 				for (int i = 0, length = resolvedCP.length; i < length; i++) {
 					buffer.append("  "); //$NON-NLS-1$
 					buffer.append(resolvedCP[i]);
 					buffer.append('\n');
 				}
 			}
 			return buffer.toString();
 		}
 
 	}
 
 	public static class PerWorkingCopyInfo implements IProblemRequestor {
 		int useCount = 0;
 		IProblemRequestor problemRequestor;
 		ISourceModule workingCopy;
 
 		public PerWorkingCopyInfo(ISourceModule workingCopy,
 				IProblemRequestor problemRequestor) {
 			this.workingCopy = workingCopy;
 			this.problemRequestor = problemRequestor;
 		}
 
 		public void acceptProblem(IProblem problem) {
 			if (this.problemRequestor == null)
 				return;
 			this.problemRequestor.acceptProblem(problem);
 		}
 
 		public void beginReporting() {
 			if (this.problemRequestor == null)
 				return;
 			this.problemRequestor.beginReporting();
 		}
 
 		public void endReporting() {
 			if (this.problemRequestor == null)
 				return;
 			this.problemRequestor.endReporting();
 		}
 
 		public ISourceModule getWorkingCopy() {
 			return this.workingCopy;
 		}
 
 		public boolean isActive() {
 			return this.problemRequestor != null
 					&& this.problemRequestor.isActive();
 		}
 
 		public String toString() {
 			StringBuffer buffer = new StringBuffer();
 			buffer.append("Info for "); //$NON-NLS-1$
 			buffer.append(((ModelElement) this.workingCopy)
 					.toStringWithAncestors());
 			buffer.append("\nUse count = "); //$NON-NLS-1$
 			buffer.append(this.useCount);
 			buffer.append("\nProblem requestor:\n  "); //$NON-NLS-1$
 			buffer.append(this.problemRequestor);
 			return buffer.toString();
 		}
 	}
 
 	public static boolean VERBOSE = DLTKCore.VERBOSE_MODEL_MANAGER;
 	public static boolean BP_RESOLVE_VERBOSE = DLTKCore.VERBOSE_BP_RESOLVE;
 	/**
 	 * Name of the extension point for contributing buildpath variable
 	 * initializers
 	 */
 	public static final String BPVARIABLE_INITIALIZER_EXTPOINT_ID = "buildpathVariableInitializer"; //$NON-NLS-1$
 
 	/**
 	 * Name of the extension point for contributing buildpath container
 	 * initializers
 	 */
 	public static final String BPCONTAINER_INITIALIZER_EXTPOINT_ID = "buildpathContainerInitializer"; //$NON-NLS-1$
 	public static boolean ZIP_ACCESS_VERBOSE = DLTKCore.VERBOSE_ZIP_ACCESS;
 	/**
 	 * A cache of opened zip files per thread. (for a given thread, the object
 	 * value is a HashMap from IPath to java.io.ZipFile)
 	 */
 	private ThreadLocal zipFiles = new ThreadLocal();
 
 	/**
 	 * A cache of resource content.
 	 */
 	private IFileCache fileCache = null;
 
 	private UserLibraryManager userLibraryManager;
 
 	public final static ISourceModule[] NO_WORKING_COPY = new ISourceModule[0];
 	/**
 	 * The singleton manager
 	 */
 	private static ModelManager MANAGER = new ModelManager();
 	/**
 	 * Infos cache.
 	 */
 	public ModelCache cache;// = new ModelCache();
 	/*
 	 * Temporary cache of newly opened elements
 	 */
 	private ThreadLocal temporaryCache = new ThreadLocal();
 	/**
 	 * Set of elements which are out of sync with their buffers.
 	 */
 	protected HashSet elementsOutOfSynchWithBuffers = new HashSet(11);
 	/**
 	 * Holds the state used for delta processing.
 	 */
 	public DeltaProcessingState deltaState = new DeltaProcessingState();
 	public IndexManager indexManager = new IndexManager();
 	/**
 	 * Table from IProject to PerProjectInfo. NOTE: this object itself is used
 	 * as a lock to synchronize creation/removal of per project infos
 	 */
 	protected Map perProjectInfos = new HashMap(5);
 	/**
 	 * Table from WorkingCopyOwner to a table of ISourceModule (working copy
 	 * handle) to PerWorkingCopyInfo. NOTE: this object itself is used as a lock
 	 * to synchronize creation/removal of per working copy infos
 	 */
 	protected Map perWorkingCopyInfos = new HashMap(5);
 	public final IEclipsePreferences[] preferencesLookup = new IEclipsePreferences[2];
 	static final int PREF_INSTANCE = 0;
 	static final int PREF_DEFAULT = 1;
 	// Preferences
 	HashSet optionNames = new HashSet(20);
 	Hashtable optionsCache;
 	/*
 	 * Pools of symbols used in the model. Used as a replacement for
 	 * String#intern() that could prevent garbage collection of strings on some
 	 * Interpreters.
 	 */
 	private WeakHashSet stringSymbols = new WeakHashSet(5);
 	Map workspaceScope = null;
 	public static final String DELTA_LISTENER_PERF = DLTKCore.PLUGIN_ID
 			+ "/perf/deltalistener"; //$NON-NLS-1$
 
 	private ExternalFoldersManager externalFoldersManager = new ExternalFoldersManager();
 
 	/**
 	 * Constructs a new ModelManager
 	 */
 	private ModelManager() {
 		// singleton: prevent others from creating a new instance
 		if (Platform.isRunning())
 			this.indexManager = new IndexManager();
 	}
 
 	/**
 	 * Returns the handle to the active script model.
 	 */
 	public final Model getModel() {
 		return this.model;
 	}
 
 	/**
 	 * Returns the singleton ModelManager
 	 */
 	public final static ModelManager getModelManager() {
 		return MANAGER;
 	}
 
 	/**
 	 * Returns the set of elements which are out of synch with their buffers.
 	 */
 	protected HashSet getElementsOutOfSynchWithBuffers() {
 		return this.elementsOutOfSynchWithBuffers;
 	}
 
 	/**
 	 * Returns the info for the element.
 	 */
 	public synchronized Object getInfo(IModelElement element) {
 		HashMap tempCache = (HashMap) this.temporaryCache.get();
 		if (tempCache != null) {
 			Object result = tempCache.get(element);
 			if (result != null) {
 				return result;
 			}
 		}
 		return this.cache.getInfo(element);
 	}
 
 	/**
 	 * Returns the info for this element without disturbing the cache ordering.
 	 */
 	protected synchronized Object peekAtInfo(IModelElement element) {
 		HashMap tempCache = (HashMap) this.temporaryCache.get();
 		if (tempCache != null) {
 			Object result = tempCache.get(element);
 			if (result != null) {
 				return result;
 			}
 		}
 		return this.cache.peekAtInfo(element);
 	}
 
 	/*
 	 * Removes all cached info for the given element (including all children)
 	 * from the cache. Returns the info for the given element, or null if it was
 	 * closed.
 	 */
 	public synchronized Object removeInfoAndChildren(ModelElement element)
 			throws ModelException {
 		Object info = this.cache.peekAtInfo(element);
 		if (info != null) {
 			boolean wasVerbose = false;
 			try {
 				if (VERBOSE) {
 					String elementType;
 					switch (element.getElementType()) {
 					case IModelElement.SCRIPT_PROJECT:
 						elementType = "project"; //$NON-NLS-1$
 						break;
 					case IModelElement.PROJECT_FRAGMENT:
 						elementType = "root"; //$NON-NLS-1$
 						break;
 					case IModelElement.SCRIPT_FOLDER:
 						elementType = "folder"; //$NON-NLS-1$
 						break;
 					case IModelElement.BINARY_MODULE:
 						elementType = "binary module"; //$NON-NLS-1$
 						break;
 					case IModelElement.SOURCE_MODULE:
 						elementType = "source module"; //$NON-NLS-1$
 						break;
 					default:
 						elementType = "element"; //$NON-NLS-1$
 					}
 					System.out
 							.println(Thread.currentThread()
 									+ " CLOSING " + elementType + " " + element.toStringWithAncestors()); //$NON-NLS-1$//$NON-NLS-2$
 					wasVerbose = true;
 					VERBOSE = false;
 				}
 				element.closing(info);
 				if (element instanceof IParent
 						&& info instanceof ModelElementInfo) {
 					IModelElement[] children = ((ModelElementInfo) info)
 							.getChildren();
 					for (int i = 0, size = children.length; i < size; ++i) {
 						ModelElement child = (ModelElement) children[i];
 						child.close();
 					}
 				}
 				this.cache.removeInfo(element);
 				if (wasVerbose) {
 					System.out.println(this.cache.toStringFillingRation("-> ")); //$NON-NLS-1$
 				}
 			} finally {
 				ModelManager.VERBOSE = wasVerbose;
 			}
 			return info;
 		}
 		return null;
 	}
 
 	/*
 	 * Puts the infos in the given map (keys are IModelElements and values are
 	 * ModelElementInfos) in the model cache in an atomic way. First checks that
 	 * the info for the opened element (or one of its ancestors) has not been
 	 * added to the cache. If it is the case, another thread has opened the
 	 * element (or one of its ancestors). So returns without updating the cache.
 	 */
 	protected synchronized void putInfos(IModelElement openedElement,
 			Map newElements) {
 		// remove children
 		Object existingInfo = this.cache.peekAtInfo(openedElement);
 		if (openedElement instanceof IParent
 				&& existingInfo instanceof ModelElementInfo) {
 			IModelElement[] children = ((ModelElementInfo) existingInfo)
 					.getChildren();
 			for (int i = 0, size = children.length; i < size; ++i) {
 				ModelElement child = (ModelElement) children[i];
 				try {
 					child.close();
 				} catch (ModelException e) {
 					// ignore
 				}
 			}
 		}
 		// Need to put any ArchiveProjectFragment in first.
 		// This is due to the way the LRU cache flushes entries.
 		// When a BinaryFolder is flused from the LRU cache, the entire
 		// archive is flushed by removing the ArchiveProjectFragment and all of
 		// its
 		// children (see ElementCache.close()). If we flush the BinaryFolder
 		// when its ArchiveProjectFragment is not in the cache and the root is
 		// about to be
 		// added (during the 'while' loop), we will end up in an inconsist
 		// state.
 		// Subsequent resolution against package in the archive would fail as a
 		// result.
 		for (Iterator it = newElements.entrySet().iterator(); it.hasNext();) {
 			Map.Entry entry = (Map.Entry) it.next();
 			IModelElement element = (IModelElement) entry.getKey();
 			if (element instanceof ArchiveProjectFragment
 					|| element instanceof ExternalScriptFolder) {
 				Object info = entry.getValue();
 				it.remove();
 				this.cache.putInfo(element, info);
 			}
 		}
 		Iterator iterator = newElements.keySet().iterator();
 		while (iterator.hasNext()) {
 			IModelElement element = (IModelElement) iterator.next();
 			Object info = newElements.get(element);
 			this.cache.putInfo(element, info);
 		}
 	}
 
 	/**
 	 * Returns the temporary cache for newly opened elements for the current
 	 * thread. Creates it if not already created.
 	 */
 	public HashMap getTemporaryCache() {
 		HashMap result = (HashMap) this.temporaryCache.get();
 		if (result == null) {
 			result = new HashMap();
 			this.temporaryCache.set(result);
 		}
 		return result;
 	}
 
 	/*
 	 * Resets the temporary cache for newly created elements to null.
 	 */
 	public void resetTemporaryCache() {
 		this.temporaryCache.set(null);
 	}
 
 	/*
 	 * Returns whether there is a temporary cache for the current thread.
 	 */
 	public boolean hasTemporaryCache() {
 		return this.temporaryCache.get() != null;
 	}
 
 	/**
 	 * Returns the model element corresponding to the given file, its project
 	 * being the given project. Returns <code>null</code> if unable to associate
 	 * the given file with a model element.
 	 * 
 	 * Creating a model element has the side effect of creating and opening all
 	 * of the element's parents if they are not yet open.
 	 */
 	public static IModelElement create(IFile file, IScriptProject project) {
 		if (file == null) {
 			return null;
 		}
 		if (project == null) {
 			project = DLTKCore.create(file.getProject());
 		}
 		// FIXME at the moment we can create source modules only
 		if (Util.isValidSourceModule(project, file)) {
 			return createSourceModuleFrom(file, project);
 		}
 		return null;
 	}
 
 	/**
 	 * Creating an element has the side effect of creating and opening all of
 	 * the element's parents if they are not yet open.
 	 */
 	public static IModelElement create(IResource resource,
 			IScriptProject project) {
 		if (resource == null) {
 			return null;
 		}
 		int type = resource.getType();
 		switch (type) {
 		case IResource.PROJECT:
 			return DLTKCore.create((IProject) resource);
 		case IResource.FILE:
 			return create((IFile) resource, project);
 		case IResource.FOLDER:
 			return create((IFolder) resource, project);
 		case IResource.ROOT:
 			return DLTKCore.create((IWorkspaceRoot) resource);
 		default:
 			return null;
 		}
 	}
 
 	public static IModelElement create(IFolder folder, IScriptProject project) {
 		if (folder == null) {
 			return null;
 		}
 		IModelElement element;
 		if (project == null) {
 			project = DLTKCore.create(folder.getProject());
 			element = determineIfOnBuildpath(folder, project);
 			if (element == null) {
 				// walk all projects and find one that have the given folder on
 				// its buildpath
 				IScriptProject[] projects;
 				try {
 					projects = ModelManager.getModelManager().getModel()
 							.getScriptProjects();
 				} catch (ModelException e) {
 					return null;
 				}
 				for (int i = 0, length = projects.length; i < length; i++) {
 					project = projects[i];
 					element = determineIfOnBuildpath(folder, project);
 					if (element != null)
 						break;
 				}
 			}
 		} else {
 			element = determineIfOnBuildpath(folder, project);
 		}
 		return element;
 	}
 
 	/**
 	 * Creates and returns a source module element for the given file, its
 	 * project being the given project. Returns <code>null</code> if unable to
 	 * recognize the source module.
 	 */
 	public static ISourceModule createSourceModuleFrom(IFile file,
 			IScriptProject project) {
 		if (file == null)
 			return null;
 		if (project == null) {
 			project = DLTKCore.create(file.getProject());
 		}
 		IScriptFolder folder = (IScriptFolder) determineIfOnBuildpath(file,
 				project);
 		if (folder == null) {
 			// not on buildpath - make the root its folder
 			IProjectFragment root = project
 					.getProjectFragment(file.getParent());
 			folder = root.getScriptFolder(Path.EMPTY);
 			if (VERBOSE) {
 				System.out
 						.println("WARNING : creating module element outside buildpath (" + Thread.currentThread() + "): " + file.getFullPath()); //$NON-NLS-1$//$NON-NLS-2$
 			}
 		}
 		return folder.getSourceModule(file.getName());
 	}
 
 	/**
 	 * Returns the project fragment root represented by the resource, or the
 	 * folder the given resource is located in, or <code>null</code> if the
 	 * given resource is not on the buildpath of the given project.
 	 */
 	public static IModelElement determineIfOnBuildpath(IResource resource,
 			IScriptProject project) {
 		IPath resourcePath = resource.getFullPath();
 		try {
 			IBuildpathEntry[] entries = ((ScriptProject) project)
 					.getResolvedBuildpath();
 			for (int i = 0; i < entries.length; i++) {
 				IBuildpathEntry entry = entries[i];
 				if (entry.getEntryKind() == IBuildpathEntry.BPE_PROJECT)
 					continue;
 				IPath rootPath = entry.getPath();
 				if (rootPath.equals(resourcePath)) {
 					return project.getProjectFragment(resource);
 				} else if (rootPath.isPrefixOf(resourcePath)) {
 					BuildpathEntry bpe = (BuildpathEntry) entry;
 					if (!Util.isExcluded(resource, bpe
 							.fullInclusionPatternChars(), bpe
 							.fullExclusionPatternChars())) {
 						/*
 						 * given we have a resource child of the root, it cannot
 						 * be a ZIP fragment
 						 */
 						ProjectFragment root = (ProjectFragment) ((ScriptProject) project)
 								.getFolderProjectFragment(rootPath);
 						if (root == null)
 							return null;
 						IPath folderPath = resourcePath
 								.removeFirstSegments(rootPath.segmentCount());
 						if (resource.getType() == IResource.FILE) {
 							/*
 							 * if the resource is a file, then remove the last
 							 * segment which is the file name in the folder
 							 */
 							folderPath = folderPath.removeLastSegments(1);
 						}
 						return root.getScriptFolder(folderPath);
 					}
 				}
 			}
 		} catch (ModelException npe) {
 			return null;
 		}
 		return null;
 	}
 
 	/*
 	 * Returns the per-project info for the given project. If specified, create
 	 * the info if the info doesn't exist.
 	 */
 	public PerProjectInfo getPerProjectInfo(IProject project, boolean create) {
 		synchronized (this.perProjectInfos) { // use the perProjectInfo
 			// collection as its own lock
 			PerProjectInfo info = (PerProjectInfo) this.perProjectInfos
 					.get(project);
 			if (info == null && create) {
 				info = new PerProjectInfo(project);
 				this.perProjectInfos.put(project, info);
 			}
 			return info;
 		}
 	}
 
 	/*
 	 * Returns the per-project info for the given project. If the info doesn't
 	 * exist, check for the project existence and create the info. @throws
 	 * ModelException if the project doesn't exist.
 	 */
 	public PerProjectInfo getPerProjectInfoCheckExistence(IProject project)
 			throws ModelException {
 		ModelManager.PerProjectInfo info = getPerProjectInfo(project, false /*
 																			 * don't
 																			 * create
 																			 * info
 																			 */);
 		if (info == null) {
 			if (!ScriptProject.hasScriptNature(project)) {
 				throw ((ScriptProject) DLTKCore.create(project))
 						.newNotPresentException();
 			}
 			info = getPerProjectInfo(project, true /* create info */);
 		}
 		return info;
 	}
 
 	/*
 	 * Returns the per-working copy info for the given working copy at the given
 	 * path. If it doesn't exist and if create, add a new per-working copy info
 	 * with the given problem requestor. If recordUsage, increment the
 	 * per-working copy info's use count. Returns null if it doesn't exist and
 	 * not create.
 	 */
 	public PerWorkingCopyInfo getPerWorkingCopyInfo(SourceModule workingCopy,
 			boolean create, boolean recordUsage,
 			IProblemRequestor problemRequestor) {
 		synchronized (this.perWorkingCopyInfos) { // use the
 			// perWorkingCopyInfo
 			// collection as its own
 			// lock
 			WorkingCopyOwner owner = workingCopy.getOwner();
 			Map workingCopyToInfos = (Map) this.perWorkingCopyInfos.get(owner);
 			if (workingCopyToInfos == null && create) {
 				workingCopyToInfos = new HashMap();
 				this.perWorkingCopyInfos.put(owner, workingCopyToInfos);
 			}
 			PerWorkingCopyInfo info = workingCopyToInfos == null ? null
 					: (PerWorkingCopyInfo) workingCopyToInfos.get(workingCopy);
 			if (info == null && create) {
 				info = new PerWorkingCopyInfo(workingCopy, problemRequestor);
 				workingCopyToInfos.put(workingCopy, info);
 			}
 
 			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=267008
 			// problem requester added to PerWorkingCopyInfo after it already
 			// created and exists.
 			// is it ok or problem requester should be set during the creation?
 			if (info != null && problemRequestor != null) {
 				info.problemRequestor = problemRequestor;
 			}
 			if (info != null && recordUsage)
 				info.useCount++;
 			return info;
 		}
 	}
 
 	/*
 	 * Returns all the working copies which have the given owner. Adds the
 	 * working copies of the primary owner if specified. Returns null if it has
 	 * none.
 	 */
 	public ISourceModule[] getWorkingCopies(WorkingCopyOwner owner,
 			boolean addPrimary) {
 		synchronized (this.perWorkingCopyInfos) {
 			ISourceModule[] primaryWCs = addPrimary
 					&& owner != DefaultWorkingCopyOwner.PRIMARY ? getWorkingCopies(
 					DefaultWorkingCopyOwner.PRIMARY, false)
 					: null;
 			Map workingCopyToInfos = (Map) this.perWorkingCopyInfos.get(owner);
 			if (workingCopyToInfos == null)
 				return primaryWCs;
 			int primaryLength = primaryWCs == null ? 0 : primaryWCs.length;
 			int size = workingCopyToInfos.size(); // note size is > 0
 			// otherwise
 			// pathToPerWorkingCopyInfos
 			// would be null
 			ISourceModule[] result = new ISourceModule[primaryLength + size];
 			int index = 0;
 			if (primaryWCs != null) {
 				for (int i = 0; i < primaryLength; i++) {
 					ISourceModule primaryWorkingCopy = primaryWCs[i];
 					boolean validSrcModule = false;
 					IResource res = primaryWorkingCopy.getResource();
 					if (res != null) {
 						validSrcModule = Util.isValidSourceModule(
 								primaryWorkingCopy, res);
 					} else {
 						validSrcModule = Util.isValidSourceModule(
 								primaryWorkingCopy, primaryWorkingCopy
 										.getPath());
 					}
 					if (validSrcModule) {
 						ISourceModule workingCopy = new SourceModule(
 								(ScriptFolder) primaryWorkingCopy.getParent(),
 								primaryWorkingCopy.getElementName(), owner);
 						if (!workingCopyToInfos.containsKey(workingCopy))
 							result[index++] = primaryWorkingCopy;
 					} else {
 						System.err.println("Not valid primary working copy:" //$NON-NLS-1$
 								+ primaryWorkingCopy.getElementName());
 					}
 				}
 				if (index != primaryLength)
 					System.arraycopy(result, 0,
 							result = new ISourceModule[index + size], 0, index);
 			}
 			Iterator iterator = workingCopyToInfos.values().iterator();
 			while (iterator.hasNext()) {
 				result[index++] = ((ModelManager.PerWorkingCopyInfo) iterator
 						.next()).getWorkingCopy();
 			}
 			return result;
 		}
 	}
 
 	public DeltaProcessor getDeltaProcessor() {
 		return this.deltaState.getDeltaProcessor();
 	}
 
 	public static ExternalFoldersManager getExternalManager() {
 		return MANAGER.externalFoldersManager;
 	}
 
 	public IndexManager getIndexManager() {
 		return this.indexManager;
 	}
 
 	public Object getLastBuiltState(IProject project, IProgressMonitor monitor) {
 		if (!DLTKLanguageManager.hasScriptNature(project)) {
 			if (ScriptBuilder.DEBUG)
 				System.out.println(project + " is not a Java project"); //$NON-NLS-1$
 			return null; // should never be requested on non-Java projects
 		}
 		PerProjectInfo info = getPerProjectInfo(project, true/*
 															 * create if missing
 															 */);
 		if (!info.triedRead) {
 			info.triedRead = true;
 			try {
 				if (monitor != null)
 					monitor.subTask(Messages
 							.bind(Messages.build_readStateProgress, project
 									.getName()));
 				info.savedState = readState(project);
 			} catch (CoreException e) {
 				e.printStackTrace();
 			}
 		}
 		return info.savedState;
 	}
 
 	public String getOption(String optionName) {
 		if (DLTKCore.CORE_ENCODING.equals(optionName)) {
 			return DLTKCore.getEncoding();
 		}
 		String propertyName = optionName;
 		if (this.optionNames.contains(propertyName)) {
 			IPreferencesService service = Platform.getPreferencesService();
 			String value = service
 					.get(optionName, null, this.preferencesLookup);
 			return value == null ? null : value.trim();
 		}
 		return null;
 	}
 
 	/**
 	 * Get workpsace eclipse preference for ScriptCore plugin.
 	 */
 	public IEclipsePreferences getInstancePreferences() {
 		return preferencesLookup[PREF_INSTANCE];
 	}
 
 	public void setOptions(Hashtable newOptions) {
 		try {
 			IEclipsePreferences defaultPreferences = getDefaultPreferences();
 			IEclipsePreferences instancePreferences = getInstancePreferences();
 			if (newOptions == null) {
 				instancePreferences.clear();
 			} else {
 				Enumeration keys = newOptions.keys();
 				while (keys.hasMoreElements()) {
 					String key = (String) keys.nextElement();
 					if (!this.optionNames.contains(key))
 						continue; // unrecognized option
 					if (key.equals(DLTKCore.CORE_ENCODING))
 						continue; // skipped, contributed by resource prefs
 					String value = (String) newOptions.get(key);
 					String defaultValue = defaultPreferences.get(key, null);
 					if (defaultValue != null && defaultValue.equals(value)) {
 						instancePreferences.remove(key);
 					} else {
 						instancePreferences.put(key, value);
 					}
 				}
 			}
 			// persist options
 			instancePreferences.flush();
 			// update cache
 			this.optionsCache = newOptions == null ? null : new Hashtable(
 					newOptions);
 		} catch (BackingStoreException e) {
 			// ignore
 		}
 	}
 
 	public Hashtable getOptions() {
 		// return cached options if already computed
 		if (this.optionsCache != null)
 			return new Hashtable(this.optionsCache);
 		if (!Platform.isRunning()) {
 			return this.optionsCache = getDefaultOptionsNoInitialization();
 		}
 		// init
 		Hashtable options = new Hashtable(10);
 		IPreferencesService service = Platform.getPreferencesService();
 		// set options using preferences service lookup
 		Iterator iterator = optionNames.iterator();
 		while (iterator.hasNext()) {
 			String propertyName = (String) iterator.next();
 			String propertyValue = service.get(propertyName, null,
 					this.preferencesLookup);
 			if (propertyValue != null) {
 				options.put(propertyName, propertyValue);
 			}
 		}
 		// get encoding through resource plugin
 		options.put(DLTKCore.CORE_ENCODING, DLTKCore.getEncoding());
 		// store built map in cache
 		this.optionsCache = new Hashtable(options);
 		// return built map
 		return options;
 	}
 
 	/*
 	 * Reset project options stored in info cache.
 	 */
 	public void resetProjectOptions(ScriptProject scriptProject) {
 		synchronized (this.perProjectInfos) { // use the perProjectInfo
 			// collection as its own lock
 			IProject project = scriptProject.getProject();
 			PerProjectInfo info = (PerProjectInfo) this.perProjectInfos
 					.get(project);
 			if (info != null) {
 				info.options = null;
 			}
 		}
 	}
 
 	/*
 	 * Reset project preferences stored in info cache.
 	 */
 	public void resetProjectPreferences(ScriptProject scriptProject) {
 		synchronized (this.perProjectInfos) { // use the perProjectInfo
 			// collection as its own lock
 			IProject project = scriptProject.getProject();
 			PerProjectInfo info = (PerProjectInfo) this.perProjectInfos
 					.get(project);
 			if (info != null) {
 				info.preferences = null;
 			}
 		}
 	}
 
 	public void setBuildpathBeingResolved(IScriptProject project,
 			boolean buildpathIsResolved) {
 		if (buildpathIsResolved) {
 			getBuildpathBeingResolved().add(project);
 		} else {
 			getBuildpathBeingResolved().remove(project);
 		}
 	}
 
 	private HashSet getBuildpathBeingResolved() {
 		HashSet result = (HashSet) this.buildpathsBeingResolved.get();
 		if (result == null) {
 			result = new HashSet();
 			this.buildpathsBeingResolved.set(result);
 		}
 		return result;
 	}
 
 	public boolean isBuildpathBeingResolved(IScriptProject project) {
 		return getBuildpathBeingResolved().contains(project);
 	}
 
 	/*
 	 * Discards the per working copy info for the given working copy (making it
 	 * a compilation unit) if its use count was 1. Otherwise, just decrement the
 	 * use count. If the working copy is primary, computes the delta between its
 	 * state and the original compilation unit and register it. Close the
 	 * working copy, its buffer and remove it from the shared working copy
 	 * table. Ignore if no per-working copy info existed. NOTE: it must NOT be
 	 * synchronized as it may interact with the element info cache (if useCount
 	 * is decremented to 0), see bug 50667. Returns the new use count (or -1 if
 	 * it didn't exist).
 	 */
 	public int discardPerWorkingCopyInfo(SourceModule workingCopy)
 			throws ModelException {
 		// create the delta builder (this remembers the current content of the
 		// working copy)
 		// outside the perWorkingCopyInfos lock (see bug 50667)
 		ModelElementDeltaBuilder deltaBuilder = null;
 		if (workingCopy.isPrimary() && workingCopy.hasUnsavedChanges()) {
 			deltaBuilder = new ModelElementDeltaBuilder(workingCopy);
 		}
 		PerWorkingCopyInfo info = null;
 		synchronized (this.perWorkingCopyInfos) {
 			WorkingCopyOwner owner = workingCopy.getOwner();
 			Map workingCopyToInfos = (Map) this.perWorkingCopyInfos.get(owner);
 			if (workingCopyToInfos == null)
 				return -1;
 			info = (PerWorkingCopyInfo) workingCopyToInfos.get(workingCopy);
 			if (info == null)
 				return -1;
 			if (--info.useCount == 0) {
 				// remove per working copy info
 				workingCopyToInfos.remove(workingCopy);
 				if (workingCopyToInfos.isEmpty()) {
 					this.perWorkingCopyInfos.remove(owner);
 				}
 			}
 		}
 		if (info.useCount == 0) { // info cannot be null here (check was done
 			// above)
 			// remove infos + close buffer (since no longer working copy)
 			// outside the perWorkingCopyInfos lock (see bug 50667)
 			removeInfoAndChildren(workingCopy);
 			workingCopy.closeBuffer();
 			// compute the delta if needed and register it if there are changes
 			if (deltaBuilder != null) {
 				getSourceModuleInfoCache().remove(workingCopy);
 				deltaBuilder.buildDeltas();
 				if ((deltaBuilder.delta != null)
 						&& (deltaBuilder.delta.getAffectedChildren().length > 0)) {
 					getDeltaProcessor().registerModelDelta(deltaBuilder.delta);
 				}
 			}
 		}
 		return info.useCount;
 	}
 
 	public synchronized String intern(String s) {
 		// make sure to copy the string (so that it doesn't hold on the
 		// underlying char[] that might be much bigger than necessary)
 		return (String) this.stringSymbols.add(s);
 	}
 
 	public void startup() throws CoreException {
 		try {
 			// initialize Model model cache
 			this.cache = new ModelCache();
 			// request state folder creation (workaround 19885)
 			DLTKCore.getPlugin().getStateLocation();
 			// Initialize eclipse preferences
 			initializePreferences();
 			// Listen to preference changes
 			Preferences.IPropertyChangeListener propertyListener = new Preferences.IPropertyChangeListener() {
 				public void propertyChange(Preferences.PropertyChangeEvent event) {
 					ModelManager.this.optionsCache = null;
 					if (DLTKCore.FILE_CACHE.equals(event.getProperty())) {
 						final IFileCache newCache = createFileCache();
 						if (newCache instanceof IFileCacheManagement) {
 							((IFileCacheManagement) newCache).start();
 						}
 						final IFileCache oldCache = fileCache;
 						fileCache = newCache;
 						if (oldCache != null
 								&& oldCache instanceof IFileCacheManagement) {
 							((IFileCacheManagement) oldCache).stop();
 						}
 					}
 				}
 			};
 			DLTKCore.getPlugin().getPluginPreferences()
 					.addPropertyChangeListener(propertyListener);
 			long start = -1;
 			if (VERBOSE)
 				start = System.currentTimeMillis();
 			loadContainers();
 			if (VERBOSE)
 				traceContainers("Loaded", start); //$NON-NLS-1$
 			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
 			workspace.addResourceChangeListener(this.deltaState,
 					IResourceChangeEvent.PRE_BUILD
 							| IResourceChangeEvent.POST_BUILD
 							| IResourceChangeEvent.POST_CHANGE
 							| IResourceChangeEvent.PRE_DELETE
 							| IResourceChangeEvent.PRE_CLOSE);
 			DLTKContentTypeManager.installListener();
 			fileCache = createFileCache();
 			if (fileCache instanceof IFileCacheManagement) {
 				((IFileCacheManagement) fileCache).start();
 			}
 			sourceModuleInfoCache = new SourceModuleInfoCache();
 			sourceModuleInfoCache.start();
 			startIndexing();
 			// process deltas since last activated in indexer thread so that
 			// indexes are up-to-date.
 			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=38658
 			Job processSavedState = new Job(Messages.savedState_jobName) {
 				protected IStatus run(IProgressMonitor monitor) {
 					try {
 						// add save participant and process delta atomically
 						// see
 						// https://bugs.eclipse.org/bugs/show_bug.cgi?id=59937
 						workspace.run(new IWorkspaceRunnable() {
 							public void run(IProgressMonitor progress)
 									throws CoreException {
 								ISavedState savedState = workspace
 										.addSaveParticipant(DLTKCore
 												.getPlugin(), ModelManager.this);
 								if (savedState != null) {
 									// the event type coming from the saved
 									// state is always POST_AUTO_BUILD
 									// force it to be POST_CHANGE so that the
 									// delta processor can handle it
 									ModelManager.this.deltaState
 											.getDeltaProcessor().overridenEventType = IResourceChangeEvent.POST_CHANGE;
 									savedState
 											.processResourceChangeEvents(ModelManager.this.deltaState);
 								}
 							}
 						}, monitor);
 					} catch (CoreException e) {
 						return e.getStatus();
 					}
 					return Status.OK_STATUS;
 				}
 			};
 			processSavedState.setSystem(true);
 			processSavedState.setPriority(Job.SHORT); // process asap
 			processSavedState.schedule();
 		} catch (RuntimeException e) {
 			shutdown();
 			throw e;
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	private IFileCache createFileCache() {
 		final String selectedCacheId = DLTKCore.getPlugin()
 				.getPluginPreferences().getString(DLTKCore.FILE_CACHE);
 		if (selectedCacheId != null) {
 			final String fileCacheExtPoint = DLTKCore.PLUGIN_ID + ".fileCache"; //$NON-NLS-1$
 			final IConfigurationElement[] elements = Platform
 					.getExtensionRegistry().getConfigurationElementsFor(
 							fileCacheExtPoint);
 			for (int i = 0; i < elements.length; ++i) {
 				final IConfigurationElement element = elements[i];
 				if (selectedCacheId.equals(element.getAttribute("id"))) { //$NON-NLS-1$
 					try {
 						final IFileCache cache = (IFileCache) element
 								.createExecutableExtension("class"); //$NON-NLS-1$
 						if (selectedCacheId.equals(cache.getId())) {
 							return cache;
 						}
 					} catch (Exception e) {
 						DLTKCore.error("FileCache create error", e); //$NON-NLS-1$
 					}
 					break;
 				}
 			}
 		}
 		return new FileCacheStub();
 	}
 
 	private void startIndexing() {
 		getIndexManager().reset();
 		ProjectIndexerManager.startIndexing();
 	}
 
 	/**
 	 * Update the buildpath variable cache
 	 */
 	public static class EclipsePreferencesListener implements
 			IEclipsePreferences.IPreferenceChangeListener {
 		/**
 		 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener#preferenceChange(org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent)
 		 */
 		public void preferenceChange(
 				IEclipsePreferences.PreferenceChangeEvent event) {
 			String propertyName = event.getKey();
 			if (propertyName.startsWith(BP_CONTAINER_PREFERENCES_PREFIX)) {
 				recreatePersistedContainer(propertyName, (String) event
 						.getNewValue(), false);
 			} else if (propertyName
 					.startsWith(BP_USERLIBRARY_PREFERENCES_PREFIX)) {
 				String libName = propertyName
 						.substring(BP_USERLIBRARY_PREFERENCES_PREFIX.length());
 				UserLibraryManager manager = ModelManager
 						.getUserLibraryManager();
 				manager
 						.updateUserLibrary(libName, (String) event
 								.getNewValue());
 			}
 		}
 	}
 
 	/**
 	 * Reads the build state for the relevant project.
 	 */
 	protected Object readState(IProject project) throws CoreException {
 		File file = getSerializationFile(project);
 		if (file != null && file.exists()) {
 			try {
 				DataInputStream in = new DataInputStream(
 						new BufferedInputStream(new FileInputStream(file)));
 				try {
 					String pluginID = in.readUTF();
 					if (!pluginID.equals(DLTKCore.PLUGIN_ID))
 						throw new IOException(Messages.build_wrongFileFormat);
 					String kind = in.readUTF();
 					if (!kind.equals("STATE")) //$NON-NLS-1$
 						throw new IOException(Messages.build_wrongFileFormat);
 					if (in.readBoolean())
 						return ScriptBuilder.readState(project, in);
 					if (ScriptBuilder.DEBUG)
 						System.out
 								.println("Saved state thinks last build failed for " + project.getName()); //$NON-NLS-1$
 				} finally {
 					in.close();
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 				throw new CoreException(
 						new Status(
 								IStatus.ERROR,
 								DLTKCore.PLUGIN_ID,
 								Platform.PLUGIN_ERROR,
 								"Error reading last build state for project " + project.getName(), e)); //$NON-NLS-1$
 			}
 		} else if (ScriptBuilder.DEBUG) {
 			if (file == null)
 				System.out.println("Project does not exist: " + project); //$NON-NLS-1$
 			else
 				System.out
 						.println("Build state file " + file.getPath() + " does not exist"); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		return null;
 	}
 
 	public static void recreatePersistedContainer(String propertyName,
 			String containerString, boolean addToContainerValues) {
 		int containerPrefixLength = BP_CONTAINER_PREFERENCES_PREFIX.length();
 		int index = propertyName.indexOf('|', containerPrefixLength);
 		if (containerString != null)
 			containerString = containerString.trim();
 		if (index > 0) {
 			String projectName = propertyName.substring(containerPrefixLength,
 					index).trim();
 			IScriptProject project = getModelManager().getModel()
 					.getScriptProject(projectName);
 			IPath containerPath = new Path(propertyName.substring(index + 1)
 					.trim());
 			recreatePersistedContainer(project, containerPath, containerString,
 					addToContainerValues);
 		}
 	}
 
 	private static void recreatePersistedContainer(
 			final IScriptProject project, final IPath containerPath,
 			String containerString, boolean addToContainerValues) {
 		if (!project.getProject().isAccessible())
 			return; // avoid leaking deleted project's persisted container
 		if (containerString == null) {
 			getModelManager().containerPut(project, containerPath, null);
 		} else {
 			IBuildpathEntry[] entries;
 			try {
 				entries = ((ScriptProject) project).decodeBuildpath(
 						containerString, null/*
 											 * not interested in unknown
 											 * elements
 											 */);
 			} catch (IOException e) {
 				Util
 						.log(
 								e,
 								"Could not recreate persisted container: \n" + containerString); //$NON-NLS-1$
 				entries = ScriptProject.INVALID_BUILDPATH;
 			}
 			if (entries != ScriptProject.INVALID_BUILDPATH) {
 				final IBuildpathEntry[] containerEntries = entries;
 				IBuildpathContainer container = new IBuildpathContainer() {
 					public IBuildpathEntry[] getBuildpathEntries(
 							IScriptProject project) {
 						return containerEntries;
 					}
 
 					public String getDescription(IScriptProject project) {
 						return "Persisted container [" + containerPath + " for project [" + project.getElementName() + "]"; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
 					}
 
 					public int getKind() {
 						return 0;
 					}
 
 					public IPath getPath() {
 						return containerPath;
 					}
 
 					public String toString() {
 						return getDescription(project);
 					}
 
 					public IBuiltinModuleProvider getBuiltinProvider(
 							IScriptProject project) {
 						return null;
 					}
 				};
 				if (addToContainerValues) {
 					getModelManager().containerPut(project, containerPath,
 							container);
 				}
 				Map projectContainers = (Map) getModelManager().previousSessionContainers
 						.get(project);
 				if (projectContainers == null) {
 					projectContainers = new HashMap(1);
 					getModelManager().previousSessionContainers.put(project,
 							projectContainers);
 				}
 				projectContainers.put(containerPath, container);
 			}
 		}
 	}
 
 	/**
 	 * Initialize preferences lookups for DLTKCore plugin.
 	 */
 	public void initializePreferences() {
 		// Create lookups
 		preferencesLookup[PREF_INSTANCE] = ((IScopeContext) new InstanceScope())
 				.getNode(DLTKCore.PLUGIN_ID);
 		preferencesLookup[PREF_DEFAULT] = ((IScopeContext) new DefaultScope())
 				.getNode(DLTKCore.PLUGIN_ID);
 		// Listen to instance preferences node removal from parent in order to
 		// refresh stored one
 		IEclipsePreferences.INodeChangeListener listener = new IEclipsePreferences.INodeChangeListener() {
 			public void added(IEclipsePreferences.NodeChangeEvent event) {
 				// do nothing
 			}
 
 			public void removed(IEclipsePreferences.NodeChangeEvent event) {
 				if (event.getChild() == preferencesLookup[PREF_INSTANCE]) {
 					preferencesLookup[PREF_INSTANCE] = ((IScopeContext) new InstanceScope())
 							.getNode(DLTKCore.PLUGIN_ID);
 					preferencesLookup[PREF_INSTANCE]
 							.addPreferenceChangeListener(new EclipsePreferencesListener());
 				}
 			}
 		};
 		((IEclipsePreferences) preferencesLookup[PREF_INSTANCE].parent())
 				.addNodeChangeListener(listener);
 		preferencesLookup[PREF_INSTANCE]
 				.addPreferenceChangeListener(new EclipsePreferencesListener());
 		// Listen to default preferences node removal from parent in order to
 		// refresh stored one
 		listener = new IEclipsePreferences.INodeChangeListener() {
 			public void added(IEclipsePreferences.NodeChangeEvent event) {
 				// do nothing
 			}
 
 			public void removed(IEclipsePreferences.NodeChangeEvent event) {
 				if (event.getChild() == preferencesLookup[PREF_DEFAULT]) {
 					preferencesLookup[PREF_DEFAULT] = ((IScopeContext) new DefaultScope())
 							.getNode(DLTKCore.PLUGIN_ID);
 				}
 			}
 		};
 		((IEclipsePreferences) preferencesLookup[PREF_DEFAULT].parent())
 				.addNodeChangeListener(listener);
 	}
 
 	public void shutdown() {
 		DLTKCore.getDefault().savePluginPreferences();
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		workspace.removeResourceChangeListener(this.deltaState);
 		DLTKContentTypeManager.uninstallListener();
 		workspace.removeSaveParticipant(DLTKCore.getDefault());
 
 		if (fileCache != null) {
 			if (fileCache instanceof IFileCacheManagement) {
 				((IFileCacheManagement) fileCache).stop();
 			}
 		}
 		if (sourceModuleInfoCache != null) {
 			sourceModuleInfoCache.stop();
 		}
 		if (this.indexManager != null) { // no more indexing
 			this.indexManager.shutdown();
 		}
 		// wait for the initialization job to finish
 		try {
 			Job.getJobManager().join(DLTKCore.PLUGIN_ID, null);
 		} catch (InterruptedException e) {
 			// ignore
 		}
 	}
 
 	public void removePerProjectInfo(ScriptProject scriptProject) {
 		synchronized (this.perProjectInfos) { // use the perProjectInfo
 			// collection as its own lock
 			IProject project = scriptProject.getProject();
 			PerProjectInfo info = (PerProjectInfo) this.perProjectInfos
 					.get(project);
 			if (info != null) {
 				this.perProjectInfos.remove(project);
 			}
 		}
 	}
 
 	public synchronized IPath variableGet(String variableName) {
 		// check initialization in progress first
 		HashSet initializations = variableInitializationInProgress();
 		if (initializations.contains(variableName)) {
 			return VARIABLE_INITIALIZATION_IN_PROGRESS;
 		}
 		return (IPath) this.variables.get(variableName);
 	}
 
 	private synchronized IPath variableGetDefaultToPreviousSession(
 			String variableName) {
 		IPath variablePath = (IPath) this.variables.get(variableName);
 		if (variablePath == null)
 			return getPreviousSessionVariable(variableName);
 		return variablePath;
 	}
 
 	/*
 	 * Returns the set of variable names that are being initialized in the
 	 * current thread.
 	 */
 	private HashSet variableInitializationInProgress() {
 		HashSet initializations = (HashSet) this.variableInitializationInProgress
 				.get();
 		if (initializations == null) {
 			initializations = new HashSet();
 			this.variableInitializationInProgress.set(initializations);
 		}
 		return initializations;
 	}
 
 	public synchronized String[] variableNames() {
 		int length = this.variables.size();
 		String[] result = new String[length];
 		Iterator vars = this.variables.keySet().iterator();
 		int index = 0;
 		while (vars.hasNext()) {
 			result[index++] = (String) vars.next();
 		}
 		return result;
 	}
 
 	public synchronized void variablePut(String variableName, IPath variablePath) {
 
 		// set/unset the initialization in progress
 		HashSet initializations = variableInitializationInProgress();
 		if (variablePath == VARIABLE_INITIALIZATION_IN_PROGRESS) {
 			initializations.add(variableName);
 
 			// do not write out intermediate initialization value
 			return;
 		} else {
 			initializations.remove(variableName);
 
 			// update cache - do not only rely on listener refresh
 			if (variablePath == null) {
 				// if path is null, record that the variable was removed to
 				// avoid asking the initializer to initialize it again
 				// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=112609
 				this.variables.put(variableName, BP_ENTRY_IGNORE_PATH);
 				// clean other variables caches
 				this.variablesWithInitializer.remove(variableName);
 			} else {
 				this.variables.put(variableName, variablePath);
 			}
 			// discard obsoleted information about previous session
 			this.previousSessionVariables.remove(variableName);
 		}
 	}
 
 	public void variablePreferencesPut(String variableName, IPath variablePath) {
 		String variableKey = BP_VARIABLE_PREFERENCES_PREFIX + variableName;
 		if (variablePath == null) {
 			getInstancePreferences().remove(variableKey);
 		} else {
 			getInstancePreferences().put(variableKey, variablePath.toString());
 		}
 		try {
 			getInstancePreferences().flush();
 		} catch (BackingStoreException e) {
 			// ignore exception
 		}
 	}
 
 	/*
 	 * Optimize startup case where 1 variable is initialized at a time with the
 	 * same value as on shutdown.
 	 */
 	public boolean variablePutIfInitializingWithSameValue(
 			String[] variableNames, IPath[] variablePaths) {
 		if (variableNames.length != 1)
 			return false;
 		String variableName = variableNames[0];
 		IPath oldPath = variableGetDefaultToPreviousSession(variableName);
 		if (oldPath == null)
 			return false;
 		IPath newPath = variablePaths[0];
 		if (!oldPath.equals(newPath))
 			return false;
 		variablePut(variableName, newPath);
 		return true;
 	}
 
 	/**
 	 * Sets the last built state for the given project, or null to reset it.
 	 */
 	public void setLastBuiltState(IProject project, Object state) {
 		if (DLTKLanguageManager.hasScriptNature(project)) {
 			// should never be requested on non-script projects
 			PerProjectInfo info = getPerProjectInfo(project, true /*
 																 * create if
 																 * missing
 																 */);
 			info.triedRead = true; // no point trying to re-read once using
 			// setter
 			info.savedState = state;
 		}
 		if (state == null) { // delete state file to ensure a full build
 			// happens if the workspace crashes
 			try {
 				File file = getSerializationFile(project);
 				if (file != null && file.exists())
 					file.delete();
 			} catch (SecurityException se) {
 				// could not delete file: cannot do much more
 			}
 		}
 	}
 
 	/**
 	 * Returns the File to use for saving and restoring the last built state for
 	 * the given project.
 	 */
 	private File getSerializationFile(IProject project) {
 		if (!project.exists())
 			return null;
 		IPath workingLocation = project.getWorkingLocation(DLTKCore.PLUGIN_ID);
 		return workingLocation.append("state.dat").toFile(); //$NON-NLS-1$
 	}
 
 	// If modified, also modify the method getDefaultOptionsNoInitialization()
 	public Hashtable getDefaultOptions() {
 		Hashtable defaultOptions = new Hashtable(10);
 		// see
 		// DLTKCorePreferenceInitializer#initializeDefaultPluginPreferences()
 		// for changing default settings
 		// If modified, also modify the method
 		// getDefaultOptionsNoInitialization()
 		IEclipsePreferences defaultPreferences = getDefaultPreferences();
 		// initialize preferences to their default
 		Iterator iterator = this.optionNames.iterator();
 		while (iterator.hasNext()) {
 			String propertyName = (String) iterator.next();
 			String value = defaultPreferences.get(propertyName, null);
 			if (value != null)
 				defaultOptions.put(propertyName, value);
 		}
 		// get encoding through resource plugin
 		defaultOptions.put(DLTKCore.CORE_ENCODING, DLTKCore.getEncoding());
 		return defaultOptions;
 	}
 
 	/**
 	 * Get default eclipse preference for DLTKCore plugin.
 	 */
 	public IEclipsePreferences getDefaultPreferences() {
 		return preferencesLookup[PREF_DEFAULT];
 	}
 
 	// Do not modify without modifying getDefaultOptions()
 	private Hashtable getDefaultOptionsNoInitialization() {
 		System.err
 				.println("Add language dependent compiler options. Or implement it in another whan in DLTK way..."); //$NON-NLS-1$
 		Map defaultOptionsMap = new HashMap(); // compiler defaults
 		return new Hashtable(defaultOptionsMap);
 	}
 
 	public IBuildpathContainer getBuildpathContainer(IPath containerPath,
 			IScriptProject project) throws ModelException {
 		IBuildpathContainer container = containerGet(project, containerPath);
 		if (container == null) {
 			if (this.batchContainerInitializations) {
 				// avoid deep recursion while initializaing container on
 				// workspace restart
 				// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=60437)
 				this.batchContainerInitializations = false;
 				return initializeAllContainers(project, containerPath);
 			}
 			return initializeContainer(project, containerPath);
 		}
 		return container;
 	}
 
 	public synchronized IBuildpathContainer containerGet(
 			IScriptProject project, IPath containerPath) {
 		// check initialization in progress first
 		HashSet projectInitializations = containerInitializationInProgress(project);
 		if (projectInitializations.contains(containerPath)) {
 			return CONTAINER_INITIALIZATION_IN_PROGRESS;
 		}
 		Map projectContainers = (Map) this.containers.get(project);
 		if (projectContainers == null) {
 			return null;
 		}
 		IBuildpathContainer container = (IBuildpathContainer) projectContainers
 				.get(containerPath);
 		return container;
 	}
 
 	/*
 	 * Returns the set of container paths for the given project that are being
 	 * initialized in the current thread.
 	 */
 	private HashSet containerInitializationInProgress(IScriptProject project) {
 		Map initializations = (Map) this.containerInitializationInProgress
 				.get();
 		if (initializations == null) {
 			initializations = new HashMap();
 			this.containerInitializationInProgress.set(initializations);
 		}
 		HashSet projectInitializations = (HashSet) initializations.get(project);
 		if (projectInitializations == null) {
 			projectInitializations = new HashSet();
 			initializations.put(project, projectInitializations);
 		}
 		return projectInitializations;
 	}
 
 	/*
 	 * Initialize all container at the same time as the given container. Return
 	 * the container for the given path and project.
 	 */
 	private IBuildpathContainer initializeAllContainers(
 			IScriptProject scriptProjectToInit, IPath containerToInit)
 			throws ModelException {
 		/*
 		 * if (BP_RESOLVE_VERBOSE) { Util.verbose( "CPContainer INIT - batching
 		 * containers initialization\n" + //$NON-NLS-1$ " project to init: " +
 		 * scriptProjectToInit.getElementName() + '\n' + //$NON-NLS-1$ "
 		 * container path to init: " + containerToInit); //$NON-NLS-1$ }
 		 */
 		// collect all container paths
 		final HashMap allContainerPaths = new HashMap();
 		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
 				.getProjects();
 		for (int i = 0, length = projects.length; i < length; i++) {
 			IProject project = projects[i];
 			if (!DLTKLanguageManager.hasScriptNature(project))
 				continue;
 			ScriptProject scriptProject = new ScriptProject(project, getModel());
 			HashSet paths = null;
 			IBuildpathEntry[] rawBuildpath = scriptProject.getRawBuildpath();
 			for (int j = 0, length2 = rawBuildpath.length; j < length2; j++) {
 				IBuildpathEntry entry = rawBuildpath[j];
 				IPath path = entry.getPath();
 				if (entry.getEntryKind() == IBuildpathEntry.BPE_CONTAINER
 						&& containerGet(scriptProject, path) == null) {
 					if (paths == null) {
 						paths = new HashSet();
 						allContainerPaths.put(scriptProject, paths);
 					}
 					paths.add(path);
 				}
 			}
 		}
 		HashSet containerPaths = (HashSet) allContainerPaths
 				.get(scriptProjectToInit);
 		if (containerPaths == null) {
 			containerPaths = new HashSet();
 			allContainerPaths.put(scriptProjectToInit, containerPaths);
 		}
 		containerPaths.add(containerToInit);
 		// end block
 		// mark all containers as being initialized
 		this.containerInitializationInProgress.set(allContainerPaths);
 		// initialize all containers
 		boolean ok = false;
 		try {
 			// if possible run inside an IWokspaceRunnable with AVOID_UPATE to
 			// avoid unwanted builds
 			// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=118507)
 			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
 				public void run(IProgressMonitor monitor) throws CoreException {
 					Set keys = allContainerPaths.keySet();
 					int length = keys.size();
 					IScriptProject[] scriptProjects = new IScriptProject[length]; // clone
 					// as
 					// the
 					// following
 					// will
 					// have
 					// a
 					// side
 					// effect
 					keys.toArray(scriptProjects);
 					for (int i = 0; i < length; i++) {
 						IScriptProject scriptProject = scriptProjects[i];
 						HashSet pathSet = (HashSet) allContainerPaths
 								.get(scriptProject);
 						if (pathSet == null)
 							continue;
 						int length2 = pathSet.size();
 						IPath[] paths = new IPath[length2];
 						pathSet.toArray(paths); // clone as the following will
 						// have a side effect
 						for (int j = 0; j < length2; j++) {
 							IPath path = paths[j];
 							initializeContainer(scriptProject, path);
 						}
 					}
 				}
 			};
 			IWorkspace workspace = ResourcesPlugin.getWorkspace();
 			if (workspace.isTreeLocked())
 				runnable.run(null/* no progress available */);
 			else
 				workspace.run(runnable, null/* don't take any lock */,
 						IWorkspace.AVOID_UPDATE, null/*
 													 * no progress available
 													 * here
 													 */);
 			ok = true;
 		} catch (CoreException e) {
 			// ignore
 			System.err.println("Exception while initializing all containers"); //$NON-NLS-1$
 			// Util.log(e, "Exception while initializing all containers");
 			// //$NON-NLS-1$
 		} finally {
 			if (!ok) {
 				// if we're being traversed by an exception, ensure that that
 				// containers are
 				// no longer marked as initialization in progress
 				// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=66437)
 				this.containerInitializationInProgress.set(null);
 			}
 		}
 		return containerGet(scriptProjectToInit, containerToInit);
 	}
 
 	IBuildpathContainer initializeContainer(IScriptProject project,
 			IPath containerPath) throws ModelException {
 		IBuildpathContainer container = null;
 		final BuildpathContainerInitializer initializer = DLTKCore
 				.getBuildpathContainerInitializer(containerPath.segment(0));
 		if (initializer != null) {
 			if (BP_RESOLVE_VERBOSE) {
 				verbose_triggering_container_initialization(project,
 						containerPath, initializer);
 			}
 			// PerformanceStats stats = null;
 			containerPut(project, containerPath,
 					CONTAINER_INITIALIZATION_IN_PROGRESS); // avoid
 			// initialization
 			// cycles
 			boolean ok = false;
 			try {
 				// let OperationCanceledException go through
 				// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=59363)
 				initializer.initialize(containerPath, project);
 				// retrieve value (if initialization was successful)
 				container = containerGet(project, containerPath);
 				if (container == CONTAINER_INITIALIZATION_IN_PROGRESS)
 					return null; // break cycle
 				ok = true;
 			} catch (CoreException e) {
 				if (e instanceof ModelException) {
 					throw (ModelException) e;
 				} else {
 					throw new ModelException(e);
 				}
 			} catch (RuntimeException e) {
 				if (ModelManager.BP_RESOLVE_VERBOSE) {
 					e.printStackTrace();
 				}
 				throw e;
 			} catch (Error e) {
 				if (ModelManager.BP_RESOLVE_VERBOSE) {
 					e.printStackTrace();
 				}
 				throw e;
 			} finally {
 				if (!ok) {
 					// just remove initialization in progress and keep previous
 					// session container so as to avoid a full build
 					// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=92588
 					containerRemoveInitializationInProgress(project,
 							containerPath);
 					if (BP_RESOLVE_VERBOSE) {
 						verbose_container_initialization_failed(project,
 								containerPath, container, initializer);
 					}
 				}
 			}
 			if (BP_RESOLVE_VERBOSE) {
 				verbose_container_value_after_initialization(project,
 						containerPath, container);
 			}
 		} else {
 			if (BP_RESOLVE_VERBOSE) {
 				Util.verbose("CPContainer INIT - no initializer found\n" + //$NON-NLS-1$
 						"	project: " + project.getElementName() + '\n' + //$NON-NLS-1$
 						"	container path: " + containerPath); //$NON-NLS-1$
 			}
 		}
 		return container;
 	}
 
 	private void verbose_triggering_container_initialization(
 			IScriptProject project, IPath containerPath,
 			final BuildpathContainerInitializer initializer) {
 		Util.verbose("BPContainer INIT - triggering initialization\n" + //$NON-NLS-1$
 				"	project: " + project.getElementName() + '\n' + //$NON-NLS-1$
 				"	container path: " + containerPath + '\n' + //$NON-NLS-1$
 				"	initializer: " + initializer + '\n' + //$NON-NLS-1$
 				"	invocation stack trace:"); //$NON-NLS-1$
 		new Exception("<Fake exception>").printStackTrace(System.out); //$NON-NLS-1$
 	}
 
 	private void verbose_container_value_after_initialization(
 			IScriptProject project, IPath containerPath,
 			IBuildpathContainer container) {
 		StringBuffer buffer = new StringBuffer();
 		buffer.append("CPContainer INIT - after resolution\n"); //$NON-NLS-1$
 		buffer.append("	project: " + project.getElementName() + '\n'); //$NON-NLS-1$
 		buffer.append("	container path: " + containerPath + '\n'); //$NON-NLS-1$
 		if (container != null) {
 			buffer
 					.append("	container: " + container.getDescription(project) + " {\n"); //$NON-NLS-2$//$NON-NLS-1$
 			IBuildpathEntry[] entries = container.getBuildpathEntries(project);
 			if (entries != null) {
 				for (int i = 0; i < entries.length; i++) {
 					buffer.append("		" + entries[i] + '\n'); //$NON-NLS-1$
 				}
 			}
 			buffer.append("	}");//$NON-NLS-1$
 		} else {
 			buffer.append("	container: {unbound}");//$NON-NLS-1$
 		}
 		Util.verbose(buffer.toString());
 	}
 
 	private void verbose_container_initialization_failed(
 			IScriptProject project, IPath containerPath,
 			IBuildpathContainer container,
 			final BuildpathContainerInitializer initializer) {
 		if (container == CONTAINER_INITIALIZATION_IN_PROGRESS) {
 			Util
 					.verbose("CPContainer INIT - FAILED (initializer did not initialize container)\n" + //$NON-NLS-1$
 							"	project: " //$NON-NLS-1$
 							+ project.getElementName()
 							+ '\n'
 							+ "	container path: " //$NON-NLS-1$
 							+ containerPath
 							+ '\n'
 							+ "	initializer: " + initializer); //$NON-NLS-1$
 		} else {
 			Util.verbose("CPContainer INIT - FAILED (see exception above)\n" + //$NON-NLS-1$
 					"	project: " //$NON-NLS-1$
 					+ project.getElementName() + '\n' + "	container path: " //$NON-NLS-1$
 					+ containerPath + '\n' + "	initializer: " + initializer); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Returns a persisted container from previous session if any. Note that it
 	 * is not the original container from previous session (i.e. it did not get
 	 * serialized) but rather a summary of its entries recreated for CP
 	 * initialization purpose. As such it should not be stored into container
 	 * caches.
 	 */
 	public IBuildpathContainer getPreviousSessionContainer(IPath containerPath,
 			IScriptProject project) {
 		Map previousContainerValues = (Map) this.previousSessionContainers
 				.get(project);
 		if (previousContainerValues != null) {
 			IBuildpathContainer previousContainer = (IBuildpathContainer) previousContainerValues
 					.get(containerPath);
 			if (previousContainer != null) {
 				if (ModelManager.BP_RESOLVE_VERBOSE) {
 					StringBuffer buffer = new StringBuffer();
 					buffer
 							.append("CPContainer INIT - reentering access to project container during its initialization, will see previous value\n"); //$NON-NLS-1$ 
 					buffer
 							.append("	project: " + project.getElementName() + '\n'); //$NON-NLS-1$
 					buffer.append("	container path: " + containerPath + '\n'); //$NON-NLS-1$
 					buffer.append("	previous value: "); //$NON-NLS-1$
 					buffer.append(previousContainer.getDescription(project));
 					buffer.append(" {\n"); //$NON-NLS-1$
 					IBuildpathEntry[] entries = previousContainer
 							.getBuildpathEntries(project);
 					if (entries != null) {
 						for (int j = 0; j < entries.length; j++) {
 							buffer.append(" 		"); //$NON-NLS-1$
 							buffer.append(entries[j]);
 							buffer.append('\n');
 						}
 					}
 					buffer.append(" 	}"); //$NON-NLS-1$
 					Util.verbose(buffer.toString());
 					new Exception("<Fake exception>").printStackTrace(System.out); //$NON-NLS-1$
 				}
 				return previousContainer;
 			}
 		}
 		return null; // break cycle if none found
 	}
 
 	/**
 	 * Returns a persisted container from previous session if any
 	 */
 	public IPath getPreviousSessionVariable(String variableName) {
 		IPath previousPath = (IPath) this.previousSessionVariables
 				.get(variableName);
 		if (previousPath != null) {
 			// if (BP_RESOLVE_VERBOSE_ADVANCED)
 			// verbose_reentering_variable_access(variableName, previousPath);
 			return previousPath;
 		}
 		return null; // break cycle
 	}
 
 	public synchronized void containerPut(IScriptProject project,
 			IPath containerPath, IBuildpathContainer container) {
 		// set/unset the initialization in progress
 		if (container == CONTAINER_INITIALIZATION_IN_PROGRESS) {
 			HashSet projectInitializations = containerInitializationInProgress(project);
 			projectInitializations.add(containerPath);
 			// do not write out intermediate initialization value
 			return;
 		} else {
 			containerRemoveInitializationInProgress(project, containerPath);
 			Map projectContainers = (Map) this.containers.get(project);
 			if (projectContainers == null) {
 				projectContainers = new HashMap(1);
 				this.containers.put(project, projectContainers);
 			}
 			if (container == null) {
 				projectContainers.remove(containerPath);
 			} else {
 				projectContainers.put(containerPath, container);
 			}
 			// discard obsoleted information about previous session
 			Map previousContainers = (Map) this.previousSessionContainers
 					.get(project);
 			if (previousContainers != null) {
 				previousContainers.remove(containerPath);
 			}
 		}
 		// container values are persisted in preferences during save operations,
 		// see #saving(ISaveContext)
 	}
 
 	/*
 	 * The given project is being removed. Remove all containers for this
 	 * project from the cache.
 	 */
 	public synchronized void containerRemove(IScriptProject project) {
 		Map initializations = (Map) this.containerInitializationInProgress
 				.get();
 		if (initializations != null) {
 			initializations.remove(project);
 		}
 		this.containers.remove(project);
 	}
 
 	private void containerRemoveInitializationInProgress(
 			IScriptProject project, IPath containerPath) {
 		HashSet projectInitializations = containerInitializationInProgress(project);
 		projectInitializations.remove(containerPath);
 		if (projectInitializations.size() == 0) {
 			Map initializations = (Map) this.containerInitializationInProgress
 					.get();
 			initializations.remove(project);
 		}
 	}
 
 	/*
 	 * Optimize startup case where a container for 1 project is initialized at a
 	 * time with the same entries as on shutdown.
 	 */
 	public boolean containerPutIfInitializingWithSameEntries(
 			IPath containerPath, IScriptProject[] projects,
 			IBuildpathContainer[] respectiveContainers) {
 		int projectLength = projects.length;
 		if (projectLength != 1)
 			return false;
 		final IBuildpathContainer container = respectiveContainers[0];
 		if (container == null)
 			return false;
 		final IScriptProject project = projects[0];
 		if (!containerInitializationInProgress(project).contains(containerPath))
 			return false;
 		IBuildpathContainer previousSessionContainer = getPreviousSessionContainer(
 				containerPath, project);
 		final IBuildpathEntry[] newEntries = container
 				.getBuildpathEntries(project);
 		if (previousSessionContainer == null)
 			if (newEntries.length == 0) {
 				containerPut(project, containerPath, container);
 				return true;
 			} else {
 				return false;
 			}
 		final IBuildpathEntry[] oldEntries = previousSessionContainer
 				.getBuildpathEntries(project);
 		if (oldEntries.length != newEntries.length)
 			return false;
 		for (int i = 0, length = newEntries.length; i < length; i++) {
 			if (!newEntries[i].equals(oldEntries[i])) {
 				if (BP_RESOLVE_VERBOSE) {
 					Util.verbose("CPContainer SET  - missbehaving container\n" + //$NON-NLS-1$
 							"	container path: " //$NON-NLS-1$
 							+ containerPath
 							+ '\n'
 							+ "	projects: {" //$NON-NLS-1$
 							+ Util.toString(projects, new Util.Displayable() {
 								public String displayString(Object o) {
 									return ((IScriptProject) o)
 											.getElementName();
 								}
 							})
 							+ "}\n	values on previous session: {\n" + //$NON-NLS-1$
 							Util.toString(respectiveContainers,
 									new Util.Displayable() {
 										public String displayString(Object o) {
 											StringBuffer buffer = new StringBuffer(
 													"		"); //$NON-NLS-1$
 											if (o == null) {
 												buffer.append("<null>"); //$NON-NLS-1$
 												return buffer.toString();
 											}
 											buffer.append(container
 													.getDescription(project));
 											buffer.append(" {\n"); //$NON-NLS-1$
 											for (int j = 0; j < oldEntries.length; j++) {
 												buffer.append(" 			"); //$NON-NLS-1$
 												buffer.append(oldEntries[j]);
 												buffer.append('\n');
 											}
 											buffer.append(" 		}"); //$NON-NLS-1$
 											return buffer.toString();
 										}
 									})
 							+ "}\n	new values: {\n" + //$NON-NLS-1$
 							Util.toString(respectiveContainers,
 									new Util.Displayable() {
 										public String displayString(Object o) {
 											StringBuffer buffer = new StringBuffer(
 													"		"); //$NON-NLS-1$
 											if (o == null) {
 												buffer.append("<null>"); //$NON-NLS-1$
 												return buffer.toString();
 											}
 											buffer.append(container
 													.getDescription(project));
 											buffer.append(" {\n"); //$NON-NLS-1$
 											for (int j = 0; j < newEntries.length; j++) {
 												buffer.append(" 			"); //$NON-NLS-1$
 												buffer.append(newEntries[j]);
 												buffer.append('\n');
 											}
 											buffer.append(" 		}"); //$NON-NLS-1$
 											return buffer.toString();
 										}
 									}) + "\n	}"); //$NON-NLS-1$
 				}
 				return false;
 			}
 		}
 		containerPut(project, containerPath, container);
 		return true;
 	}
 
 	/**
 	 * Returns the open ZipFile at the given path. If the ZipFile does not yet
 	 * exist, it is created, opened, and added to the cache of open ZipFiles.
 	 * 
 	 * NOTE: closeZipFile() must be called for the resulting ZipFile, when the
 	 * client is done using it.
 	 * 
 	 * The path must be a file system path if representing an external zip, or
 	 * it must be an absolute workspace relative path if representing a zip
 	 * inside the workspace.
 	 * 
 	 * @exception CoreException
 	 *                If unable to create/open the ZipFile
 	 */
 	public ZipFile getZipFile(IPath path) throws CoreException {
 		HashMap map;
 		ZipFile zipFile;
 		if ((map = (HashMap) this.zipFiles.get()) != null
 				&& (zipFile = (ZipFile) map.get(path)) != null) {
 			return zipFile;
 		}
 		File localFile = null;
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		IResource file = root.findMember(path);
 		if (file != null) {
 			// internal resource
 			URI location;
 			if (file.getType() != IResource.FILE
 					|| (location = file.getLocationURI()) == null) {
 				throw new CoreException(new Status(IStatus.ERROR,
 						DLTKCore.PLUGIN_ID, -1, Messages.bind(
 								Messages.file_notFound, path.toString()), null));
 			}
 			localFile = Util.toLocalFile(location, null/*
 														 * no progress
 														 * availaible
 														 */);
 			if (localFile == null)
 				throw new CoreException(new Status(IStatus.ERROR,
 						DLTKCore.PLUGIN_ID, -1, Messages.bind(
 								Messages.file_notFound, path.toString()), null));
 		} else {
 			// external resource -> it is ok to use toFile()
 			localFile = path.toFile();
 		}
 		try {
 			if (ZIP_ACCESS_VERBOSE) {
 				System.out
 						.println("(" + Thread.currentThread() + ") [ModelManager.getZipFile(IPath)] Creating ZipFile on " + localFile); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 			zipFile = new ZipFile(localFile);
 			if (map != null) {
 				map.put(path, zipFile);
 			}
 			return zipFile;
 		} catch (IOException e) {
 			throw new CoreException(new Status(IStatus.ERROR,
 					DLTKCore.PLUGIN_ID, -1, Messages.status_IOException, e));
 		}
 	}
 
 	public IFileCache getFileCache() {
 		return fileCache;
 	}
 
 	/**
 	 * Starts caching ZipFiles. Ignores if there are already clients.
 	 */
 	public void cacheZipFiles() {
 		if (this.zipFiles.get() != null)
 			return;
 		this.zipFiles.set(new HashMap());
 	}
 
 	public void closeZipFile(ZipFile zipFile) {
 		if (zipFile == null)
 			return;
 		if (this.zipFiles.get() != null) {
 			return; // zip file will be closed by call to flushZipFiles
 		}
 		try {
 			if (ModelManager.ZIP_ACCESS_VERBOSE) {
 				System.out
 						.println("(" + Thread.currentThread() + ") [ModelManager.closeZipFile(ZipFile)] Closing ZipFile on " + zipFile.getName()); //$NON-NLS-1$	//$NON-NLS-2$
 			}
 			zipFile.close();
 		} catch (IOException e) {
 			// problem occured closing zip file: cannot do much more
 		}
 	}
 
 	public void doneSaving(ISaveContext context) {
 		// nothing
 	}
 
 	public void prepareToSave(ISaveContext context) throws CoreException {
 		// TODO Auto-generated method stub
 	}
 
 	public void rollback(ISaveContext context) {
 		// TODO Auto-generated method stub
 	}
 
 	private void traceContainers(String action, long start) {
 		Long delta = new Long(System.currentTimeMillis() - start);
 		Long length = new Long(getContainersFile().length());
 		String pattern = "{0} {1} bytes in containers.dat in {2}ms"; //$NON-NLS-1$
 		String message = NLS.bind(pattern,
 				new Object[] { action, length, delta });
 		System.out.println(message);
 	}
 
 	public void saving(ISaveContext context) throws CoreException {
 		// save variable and container values on snapshot/full save
 		long start = -1;
 		if (VERBOSE)
 			start = System.currentTimeMillis();
 		savesContainers();
 		if (VERBOSE)
 			traceContainers("Saved", start); //$NON-NLS-1$
 		if (context.getKind() == ISaveContext.FULL_SAVE) {
 			// will need delta since this save (see
 			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=38658)
 			context.needDelta();
 			// clean up indexes on workspace full save
 			// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=52347)
 			IndexManager manager = this.indexManager;
 			if (manager != null
 			// don't force initialization of workspace scope as we could be
 					// shutting down
 					// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=93941)
 					&& this.workspaceScope != null) {
 				manager.cleanUpIndexes();
 			}
 		}
 		IProject savedProject = context.getProject();
 		if (savedProject != null) {
 			if (!ScriptProject.hasScriptNature(savedProject))
 				return; // ignore
 			PerProjectInfo info = getPerProjectInfo(savedProject, true /*
 																		 * create
 																		 * info
 																		 */);
 			saveState(info, context);
 			info.rememberExternalLibTimestamps();
 			return;
 		}
 		ArrayList vStats = null; // lazy initialized
 		ArrayList values = null;
 		synchronized (this.perProjectInfos) {
 			values = new ArrayList(this.perProjectInfos.values());
 		}
 		Iterator iterator = values.iterator();
 		while (iterator.hasNext()) {
 			try {
 				PerProjectInfo info = (PerProjectInfo) iterator.next();
 				saveState(info, context);
 				info.rememberExternalLibTimestamps();
 			} catch (CoreException e) {
 				if (vStats == null)
 					vStats = new ArrayList();
 				vStats.add(e.getStatus());
 			}
 		}
 		if (vStats != null) {
 			IStatus[] stats = new IStatus[vStats.size()];
 			vStats.toArray(stats);
 			throw new CoreException(
 					new MultiStatus(DLTKCore.PLUGIN_ID, IStatus.ERROR, stats,
 							Messages.build_cannotSaveStates, null));
 		}
 		// save external libs timestamps
 		this.deltaState.saveExternalLibTimeStamps();
 	}
 
 	private File getContainersFile() {
 		return DLTKCore.getPlugin().getStateLocation()
 				.append("Containers.dat").toFile(); //$NON-NLS-1$
 	}
 
 	private void saveState(PerProjectInfo info, ISaveContext context)
 			throws CoreException {
 		// passed this point, save actions are non trivial
 		if (context.getKind() == ISaveContext.SNAPSHOT)
 			return;
 		// save built state
 		if (info.triedRead)
 			saveBuiltState(info);
 	}
 
 	/**
 	 * Saves the built state for the project.
 	 */
 	private void saveBuiltState(PerProjectInfo info) throws CoreException {
 		if (ScriptBuilder.DEBUG)
 			System.out.println(Messages.bind(Messages.build_saveStateProgress,
 					info.project.getName()));
 		File file = getSerializationFile(info.project);
 		if (file == null)
 			return;
 		long t = System.currentTimeMillis();
 		try {
 			DataOutputStream out = new DataOutputStream(
 					new BufferedOutputStream(new FileOutputStream(file)));
 			try {
 				out.writeUTF(DLTKCore.PLUGIN_ID);
 				out.writeUTF("STATE"); //$NON-NLS-1$
 				if (info.savedState == null) {
 					out.writeBoolean(false);
 				} else {
 					out.writeBoolean(true);
 					ScriptBuilder.writeState(info.savedState, out);
 				}
 			} finally {
 				out.close();
 			}
 		} catch (RuntimeException e) {
 			try {
 				file.delete();
 			} catch (SecurityException se) {
 				// could not delete file: cannot do much more
 			}
 			throw new CoreException(new Status(IStatus.ERROR,
 					DLTKCore.PLUGIN_ID, Platform.PLUGIN_ERROR, Messages.bind(
 							Messages.build_cannotSaveState, info.project
 									.getName()), e));
 		} catch (IOException e) {
 			try {
 				file.delete();
 			} catch (SecurityException se) {
 				// could not delete file: cannot do much more
 			}
 			throw new CoreException(new Status(IStatus.ERROR,
 					DLTKCore.PLUGIN_ID, Platform.PLUGIN_ERROR, Messages.bind(
 							Messages.build_cannotSaveState, info.project
 									.getName()), e));
 		}
 		if (ScriptBuilder.DEBUG) {
 			t = System.currentTimeMillis() - t;
 			System.out.println(Messages.bind(Messages.build_saveStateComplete,
 					String.valueOf(t)));
 		}
 	}
 
 	private void savesContainers() throws CoreException {
 		File file = getContainersFile();
 		DataOutputStream out = null;
 		try {
 			out = new DataOutputStream(new BufferedOutputStream(
 					new FileOutputStream(file)));
 			out.writeInt(CONTAINERS_FILE_VERSION);
 			new ContainersSaveHelper(out).save();
 			// old code retained for performance comparisons
 			// containers
 			IScriptProject[] projects = getModel().getScriptProjects();
 			int length = projects.length;
 			out.writeInt(length);
 			for (int i = 0; i < length; i++) {
 				IScriptProject project = projects[i];
 				// clone while iterating (see
 				// https://bugs.eclipse.org/bugs/show_bug.cgi?id=59638)
 				Map projectContainers = containerClone(project);
 				out.writeUTF(project.getElementName());
 				if (projectContainers == null) {
 					out.writeInt(0);
 					continue;
 				}
 				HashMap containersToSave = new HashMap();
 				for (Iterator iterator = projectContainers.keySet().iterator(); iterator
 						.hasNext();) {
 					IPath containerPath = (IPath) iterator.next();
 					IBuildpathContainer container = (IBuildpathContainer) projectContainers
 							.get(containerPath);
 					String containerString = null;
 					try {
 						if (container == null) {
 							// container has not been initialized yet, use
 							// previous session value
 							// (see
 							// https://bugs.eclipse.org/bugs/show_bug.cgi?id=73969
 							// )
 							container = getPreviousSessionContainer(
 									containerPath, project);
 						}
 						if (container != null) {
 							IBuildpathEntry[] entries = container
 									.getBuildpathEntries(project);
 							containerString = ((ScriptProject) project)
 									.encodeBuildpath(entries, false, null/*
 																		 * not
 																		 * interested
 																		 * in
 																		 * unknown
 																		 * elements
 																		 */);
 						}
 					} catch (ModelException e) {
 						// could not encode entry: will not persist
 						Util
 								.log(
 										e,
 										"Could not persist container " + containerPath + " for project " + project.getElementName()); //$NON-NLS-1$ //$NON-NLS-2$
 					}
 					if (containerString != null)
 						containersToSave.put(containerPath, containerString);
 				}
 				out.writeInt(containersToSave.size());
 				Iterator iterator = containersToSave.keySet().iterator();
 				while (iterator.hasNext()) {
 					IPath containerPath = (IPath) iterator.next();
 					out.writeUTF(containerPath.toPortableString());
 					String containerString = (String) containersToSave
 							.get(containerPath);
 					out.writeInt(containerString.length());
 					out.writeBytes(containerString);
 				}
 			}
 		} catch (IOException e) {
 			IStatus status = new Status(IStatus.ERROR, DLTKCore.PLUGIN_ID,
 					IStatus.ERROR,
 					"Problems while saving variables and containers", e); //$NON-NLS-1$
 			throw new CoreException(status);
 		} finally {
 			if (out != null) {
 				try {
 					out.close();
 				} catch (IOException e) {
 					// nothing we can do: ignore
 				}
 			}
 		}
 	}
 
 	protected synchronized void resetZIPTypeCache() {
 		this.cache.resetZIPTypeCache();
 	}
 
 	public DLTKWorkspaceScope getWorkspaceScope(IDLTKLanguageToolkit toolkit) {
 		if (this.workspaceScope == null) {
 			this.workspaceScope = new HashMap();
 		}
 		if (this.workspaceScope.containsKey(toolkit)) {
 			return (DLTKWorkspaceScope) this.workspaceScope.get(toolkit);
 		} else {
 			DLTKWorkspaceScope scope = new DLTKWorkspaceScope(toolkit);
 			this.workspaceScope.put(toolkit, scope);
 			return scope;
 		}
 	}
 
 	private final class ContainersSaveHelper {
 		private final HashtableOfObjectToInt buildpathEntryIds;
 		// -> int
 		private final DataOutputStream out;
 		private final HashtableOfObjectToInt stringIds; // Strings -> int
 
 		ContainersSaveHelper(DataOutputStream out) {
 			super();
 			this.buildpathEntryIds = new HashtableOfObjectToInt();
 			this.out = out;
 			this.stringIds = new HashtableOfObjectToInt();
 		}
 
 		void save() throws IOException, ModelException {
 			saveProjects(ModelManager.this.getModel().getScriptProjects());
 		}
 
 		private void saveAccessRule(BuildpathAccessRule rule)
 				throws IOException {
 			saveInt(rule.problemId);
 			savePath(rule.getPattern());
 		}
 
 		private void saveAccessRules(IAccessRule[] rules) throws IOException {
 			int count = rules == null ? 0 : rules.length;
 			saveInt(count);
 			for (int i = 0; i < count; ++i)
 				saveAccessRule((BuildpathAccessRule) rules[i]);
 		}
 
 		private void saveAttribute(IBuildpathAttribute attribute)
 				throws IOException {
 			saveString(attribute.getName());
 			saveString(attribute.getValue());
 		}
 
 		private void saveAttributes(IBuildpathAttribute[] attributes)
 				throws IOException {
 			int count = attributes == null ? 0 : attributes.length;
 			saveInt(count);
 			for (int i = 0; i < count; ++i)
 				saveAttribute(attributes[i]);
 		}
 
 		private void saveBuildpathEntries(IBuildpathEntry[] entries)
 				throws IOException {
 			int count = entries == null ? 0 : entries.length;
 			saveInt(count);
 			for (int i = 0; i < count; ++i)
 				saveBuildpathEntry(entries[i]);
 		}
 
 		private void saveBuildpathEntry(IBuildpathEntry entry)
 				throws IOException {
 			if (saveNewId(entry, this.buildpathEntryIds)) {
 				saveInt(entry.getContentKind());
 				saveInt(entry.getEntryKind());
 				savePath(entry.getPath());
 				savePaths(entry.getInclusionPatterns());
 				savePaths(entry.getExclusionPatterns());
 				this.out.writeBoolean(entry.isExported());
 				this.out.writeBoolean(entry.isExternal());
 				saveAccessRules(entry.getAccessRules());
 				this.out.writeBoolean(entry.combineAccessRules());
 				saveAttributes(entry.getExtraAttributes());
 			}
 		}
 
 		private void saveContainers(IScriptProject project, Map containerMap)
 				throws IOException {
 			saveInt(containerMap.size());
 			for (Iterator i = containerMap.entrySet().iterator(); i.hasNext();) {
 				Entry entry = (Entry) i.next();
 				IPath path = (IPath) entry.getKey();
 				IBuildpathContainer container = (IBuildpathContainer) entry
 						.getValue();
 				IBuildpathEntry[] cpEntries = null;
 				if (container == null) {
 					// container has not been initialized yet, use previous
 					// session value
 					// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=73969)
 					container = ModelManager.this.getPreviousSessionContainer(
 							path, project);
 				}
 				if (container != null)
 					cpEntries = container.getBuildpathEntries(project);
 				savePath(path);
 				saveBuildpathEntries(cpEntries);
 			}
 		}
 
 		private void saveInt(int value) throws IOException {
 			this.out.writeInt(value);
 		}
 
 		private boolean saveNewId(Object key, HashtableOfObjectToInt map)
 				throws IOException {
 			int id = map.get(key);
 			if (id == -1) {
 				int newId = map.size();
 				map.put(key, newId);
 				saveInt(newId);
 				return true;
 			} else {
 				saveInt(id);
 				return false;
 			}
 		}
 
 		private void savePath(IPath path) throws IOException {
 			if (path == null) {
 				this.out.writeBoolean(true);
 			} else {
 				this.out.writeBoolean(false);
 				saveString(path.toPortableString());
 			}
 		}
 
 		private void savePaths(IPath[] paths) throws IOException {
 			int count = paths == null ? 0 : paths.length;
 			saveInt(count);
 			for (int i = 0; i < count; ++i)
 				savePath(paths[i]);
 		}
 
 		private void saveProjects(IScriptProject[] projects)
 				throws IOException, ModelException {
 			int count = projects.length;
 			saveInt(count);
 			for (int i = 0; i < count; ++i) {
 				IScriptProject project = projects[i];
 				saveString(project.getElementName());
 				Map containerMap = (Map) ModelManager.this.containers
 						.get(project);
 				if (containerMap == null) {
 					containerMap = Collections.EMPTY_MAP;
 				} else {
 					// clone while iterating
 					// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=59638)
 					containerMap = new HashMap(containerMap);
 				}
 				saveContainers(project, containerMap);
 			}
 		}
 
 		private void saveString(String string) throws IOException {
 			if (saveNewId(string, this.stringIds))
 				this.out.writeUTF(string);
 		}
 	}
 
 	private synchronized Map containerClone(IScriptProject project) {
 		Map originalProjectContainers = (Map) this.containers.get(project);
 		if (originalProjectContainers == null)
 			return null;
 		Map projectContainers = new HashMap(originalProjectContainers.size());
 		projectContainers.putAll(originalProjectContainers);
 		return projectContainers;
 	}
 
 	public void loadContainers() throws CoreException {
 		// backward compatibility, load variables and containers from
 		// preferences into cache
 		loadVariablesAndContainers(getDefaultPreferences());
 		loadVariablesAndContainers(getInstancePreferences());
 		// load variables and containers from saved file into cache
 		File file = getContainersFile();
 		DataInputStream in = null;
 		try {
 			in = new DataInputStream(new BufferedInputStream(
 					new FileInputStream(file)));
 			switch (in.readInt()) {
 			case 1:
 				new ContainersLoadHelper(in).load();
 				break;
 			}
 		} catch (IOException e) {
 			if (file.exists())
 				Util.log(e, "Unable to read variable and containers file"); //$NON-NLS-1$
 		} catch (RuntimeException e) {
 			if (file.exists())
 				Util
 						.log(e,
 								"Unable to read variable and containers file (file is corrupt)"); //$NON-NLS-1$
 		} finally {
 			if (in != null) {
 				try {
 					in.close();
 				} catch (IOException e) {
 					// nothing we can do: ignore
 				}
 			}
 		}
 		// override persisted values for containers which have a registered
 		// initializer
 		containersReset(getRegisteredContainerIDs());
 	}
 
 	private void loadVariablesAndContainers(IEclipsePreferences preferences) {
 		try {
 			// only get variable from preferences not set to their default
 			String[] propertyNames = preferences.keys();
 			int variablePrefixLength = BP_VARIABLE_PREFERENCES_PREFIX.length();
 			for (int i = 0; i < propertyNames.length; i++) {
 				String propertyName = propertyNames[i];
 				if (propertyName.startsWith(BP_VARIABLE_PREFERENCES_PREFIX)) {
 					String varName = propertyName
 							.substring(variablePrefixLength);
 					String propertyValue = preferences.get(propertyName, null);
 					if (propertyValue != null) {
 						String pathString = propertyValue.trim();
 
 						if (BP_ENTRY_IGNORE.equals(pathString)) {
 							// cleanup old preferences
 							preferences.remove(propertyName);
 							continue;
 						}
 
 						// add variable to table
 						IPath varPath = new Path(pathString);
 						this.variables.put(varName, varPath);
 						this.previousSessionVariables.put(varName, varPath);
 					}
 				} else if (propertyName
 						.startsWith(BP_CONTAINER_PREFERENCES_PREFIX)) {
 					String propertyValue = preferences.get(propertyName, null);
 					if (propertyValue != null) {
 						// cleanup old preferences
 						preferences.remove(propertyName);
 
 						// recreate container
 						recreatePersistedContainer(propertyName, propertyValue,
 								true/* add to container values */);
 					}
 				}
 			}
 		} catch (BackingStoreException e1) {
 			// TODO (frederic) see if it's necessary to report this failure...
 		}
 	}
 
 	private static final class PersistedBuildpathContainer implements
 			IBuildpathContainer {
 		private final IPath containerPath;
 		private final IBuildpathEntry[] entries;
 		private final IScriptProject project;
 
 		PersistedBuildpathContainer(IScriptProject project,
 				IPath containerPath, IBuildpathEntry[] entries) {
 			super();
 			this.containerPath = containerPath;
 			this.entries = entries;
 			this.project = project;
 		}
 
 		public IBuildpathEntry[] getBuildpathEntries(IScriptProject project) {
 			return entries;
 		}
 
 		public String getDescription(IScriptProject prj) {
 			return "Persisted container [" + containerPath //$NON-NLS-1$
 					+ " for project [" + project.getElementName() //$NON-NLS-1$
 					+ "]]"; //$NON-NLS-1$  
 		}
 
 		public int getKind() {
 			return 0;
 		}
 
 		public IPath getPath() {
 			return containerPath;
 		}
 
 		public String toString() {
 			return getDescription(project);
 		}
 
 		public IBuiltinModuleProvider getBuiltinProvider(IScriptProject project) {
 			return null;
 		}
 	}
 
 	private final class ContainersLoadHelper {
 		private static final int ARRAY_INCREMENT = 200;
 		private IBuildpathEntry[] allBuildpathEntries;
 		private int allBuildpathEntryCount;
 		private final Map allPaths; // String -> IPath
 		private String[] allStrings;
 		private int allStringsCount;
 		private final DataInputStream in;
 
 		ContainersLoadHelper(DataInputStream in) {
 			super();
 			this.allBuildpathEntries = null;
 			this.allBuildpathEntryCount = 0;
 			this.allPaths = new HashMap();
 			this.allStrings = null;
 			this.allStringsCount = 0;
 			this.in = in;
 		}
 
 		void load() throws IOException {
 			loadProjects(ModelManager.this.getModel());
 		}
 
 		private IAccessRule loadAccessRule() throws IOException {
 			int problemId = loadInt();
 			IPath pattern = loadPath();
 			return new BuildpathAccessRule(pattern.toString().toCharArray(),
 					problemId);
 		}
 
 		private IAccessRule[] loadAccessRules() throws IOException {
 			int count = loadInt();
 			if (count == 0)
 				return BuildpathEntry.NO_ACCESS_RULES;
 			IAccessRule[] rules = new IAccessRule[count];
 			for (int i = 0; i < count; ++i)
 				rules[i] = loadAccessRule();
 			return rules;
 		}
 
 		private IBuildpathAttribute loadAttribute() throws IOException {
 			String name = loadString();
 			String value = loadString();
 			return new BuildpathAttribute(name, value);
 		}
 
 		private IBuildpathAttribute[] loadAttributes() throws IOException {
 			int count = loadInt();
 			if (count == 0)
 				return BuildpathEntry.NO_EXTRA_ATTRIBUTES;
 			IBuildpathAttribute[] attributes = new IBuildpathAttribute[count];
 			for (int i = 0; i < count; ++i)
 				attributes[i] = loadAttribute();
 			return attributes;
 		}
 
 		private boolean loadBoolean() throws IOException {
 			return this.in.readBoolean();
 		}
 
 		private IBuildpathEntry[] loadBuildpathEntries() throws IOException {
 			int count = loadInt();
 			IBuildpathEntry[] entries = new IBuildpathEntry[count];
 			for (int i = 0; i < count; ++i)
 				entries[i] = loadBuildpathEntry();
 			return entries;
 		}
 
 		private IBuildpathEntry loadBuildpathEntry() throws IOException {
 			int id = loadInt();
 			if (id < 0 || id > this.allBuildpathEntryCount)
 				throw new IOException("Unexpected buildpathentry id"); //$NON-NLS-1$
 			if (id < this.allBuildpathEntryCount)
 				return this.allBuildpathEntries[id];
 			int contentKind = loadInt();
 			int entryKind = loadInt();
 			IPath path = loadPath();
 			IPath[] inclusionPatterns = loadPaths();
 			IPath[] exclusionPatterns = loadPaths();
 			boolean isExported = loadBoolean();
 			boolean isExternal = loadBoolean();
 			IAccessRule[] accessRules = loadAccessRules();
 			boolean combineAccessRules = loadBoolean();
 			IBuildpathAttribute[] extraAttributes = loadAttributes();
 			IBuildpathEntry entry = new BuildpathEntry(contentKind, entryKind,
 					path, isExported, inclusionPatterns, exclusionPatterns,
 					accessRules, combineAccessRules, extraAttributes,
 					isExternal);
 			IBuildpathEntry[] array = this.allBuildpathEntries;
 			if (array == null || id == array.length) {
 				array = new IBuildpathEntry[id + ARRAY_INCREMENT];
 				if (id != 0)
 					System.arraycopy(this.allBuildpathEntries, 0, array, 0, id);
 				this.allBuildpathEntries = array;
 			}
 			array[id] = entry;
 			this.allBuildpathEntryCount = id + 1;
 			return entry;
 		}
 
 		private void loadContainers(IScriptProject project) throws IOException {
 			boolean projectIsAccessible = project.getProject().isAccessible();
 			int count = loadInt();
 			for (int i = 0; i < count; ++i) {
 				IPath path = loadPath();
 				IBuildpathEntry[] entries = loadBuildpathEntries();
 				if (!projectIsAccessible)
 					// avoid leaking deleted project's persisted container,
 					// but still read the container as it is is part of the file
 					// format
 					continue;
 				IBuildpathContainer container = new PersistedBuildpathContainer(
 						project, path, entries);
 				ModelManager.this.containerPut(project, path, container);
 				Map oldContainers = (Map) ModelManager.this.previousSessionContainers
 						.get(project);
 				if (oldContainers == null) {
 					oldContainers = new HashMap();
 					ModelManager.this.previousSessionContainers.put(project,
 							oldContainers);
 				}
 				oldContainers.put(path, container);
 			}
 		}
 
 		private int loadInt() throws IOException {
 			return this.in.readInt();
 		}
 
 		private IPath loadPath() throws IOException {
 			if (loadBoolean())
 				return null;
 			String portableString = loadString();
 			IPath path = (IPath) this.allPaths.get(portableString);
 			if (path == null) {
 				path = Path.fromPortableString(portableString);
 				this.allPaths.put(portableString, path);
 			}
 			return path;
 		}
 
 		private IPath[] loadPaths() throws IOException {
 			int count = loadInt();
 			IPath[] pathArray = new IPath[count];
 			for (int i = 0; i < count; ++i)
 				pathArray[i] = loadPath();
 			return pathArray;
 		}
 
 		private void loadProjects(IScriptModel model) throws IOException {
 			int count = loadInt();
 			for (int i = 0; i < count; ++i) {
 				String projectName = loadString();
 				loadContainers(model.getScriptProject(projectName));
 			}
 		}
 
 		private String loadString() throws IOException {
 			int id = loadInt();
 			if (id < 0 || id > this.allStringsCount)
 				throw new IOException("Unexpected string id"); //$NON-NLS-1$
 			if (id < this.allStringsCount)
 				return this.allStrings[id];
 			String string = this.in.readUTF();
 			String[] array = this.allStrings;
 			if (array == null || id == array.length) {
 				array = new String[id + ARRAY_INCREMENT];
 				if (id != 0)
 					System.arraycopy(this.allStrings, 0, array, 0, id);
 				this.allStrings = array;
 			}
 			array[id] = string;
 			this.allStringsCount = id + 1;
 			return string;
 		}
 	}
 
 	/**
 	 * Returns the name of the container IDs for which an CP container
 	 * initializer is registered through an extension point
 	 */
 	public static String[] getRegisteredContainerIDs() {
 
 		Plugin dltkCorePlugin = DLTKCore.getPlugin();
 		if (dltkCorePlugin == null)
 			return null;
 
 		ArrayList containerIDList = new ArrayList(5);
 		IExtensionPoint extension = Platform.getExtensionRegistry()
 				.getExtensionPoint(DLTKCore.PLUGIN_ID,
 						ModelManager.BPCONTAINER_INITIALIZER_EXTPOINT_ID);
 		if (extension != null) {
 			IExtension[] extensions = extension.getExtensions();
 			for (int i = 0; i < extensions.length; i++) {
 				IConfigurationElement[] configElements = extensions[i]
 						.getConfigurationElements();
 				for (int j = 0; j < configElements.length; j++) {
 					String idAttribute = configElements[j].getAttribute("id"); //$NON-NLS-1$
 					if (idAttribute != null)
 						containerIDList.add(idAttribute);
 				}
 			}
 		}
 		String[] containerIDs = new String[containerIDList.size()];
 		containerIDList.toArray(containerIDs);
 		return containerIDs;
 	}
 
 	private synchronized void containersReset(String[] containerIDs) {
 		for (int i = 0; i < containerIDs.length; i++) {
 			String containerID = containerIDs[i];
 			Iterator projectIterator = this.containers.keySet().iterator();
 			while (projectIterator.hasNext()) {
 				IScriptProject project = (IScriptProject) projectIterator
 						.next();
 				Map projectContainers = (Map) this.containers.get(project);
 				if (projectContainers != null) {
 					Iterator containerIterator = projectContainers.keySet()
 							.iterator();
 					while (containerIterator.hasNext()) {
 						IPath containerPath = (IPath) containerIterator.next();
 						if (containerPath.segment(0).equals(containerID)) { // registered
 							// container
 							projectContainers.put(containerPath, null); // reset
 							// container
 							// value,
 							// but
 							// leave
 							// entry
 							// in
 							// Map
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Flushes ZipFiles cache if there are no more clients.
 	 */
 	public void flushZipFiles() {
 		Thread currentThread = Thread.currentThread();
 		HashMap map = (HashMap) this.zipFiles.get();
 		if (map == null)
 			return;
 		this.zipFiles.set(null);
 		Iterator iterator = map.values().iterator();
 		while (iterator.hasNext()) {
 			try {
 				ZipFile zipFile = (ZipFile) iterator.next();
 				if (ModelManager.ZIP_ACCESS_VERBOSE) {
 					System.out
 							.println("(" + currentThread + ") [ModelManager.flushZipFiles()] Closing ZipFile on " + zipFile.getName()); //$NON-NLS-1$//$NON-NLS-2$
 				}
 				zipFile.close();
 			} catch (IOException e) {
 				// problem occured closing zip file: cannot do much more
 			}
 		}
 	}
 
 	private SourceModuleInfoCache sourceModuleInfoCache = null;
 
 	public ISourceModuleInfoCache getSourceModuleInfoCache() {
 		return sourceModuleInfoCache;
 	}
 
 	public static UserLibraryManager getUserLibraryManager() {
 		if (MANAGER.userLibraryManager == null) {
 			UserLibraryManager libraryManager = new UserLibraryManager();
 			synchronized (MANAGER) {
 				if (MANAGER.userLibraryManager == null) { // ensure another
 					// library manager
 					// was not set while
 					// creating the
 					// instance above
 					MANAGER.userLibraryManager = libraryManager;
 				}
 			}
 		}
 		return MANAGER.userLibraryManager;
 	}
 }
