 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.core;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStreamWriter;
 import java.io.StringReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ProjectScope;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.AssertionFailedException;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.core.runtime.preferences.IScopeContext;
 import org.eclipse.dltk.compiler.util.ObjectVector;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IBuildpathContainer;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelElementMemento;
 import org.eclipse.dltk.core.IModelMarker;
 import org.eclipse.dltk.core.IModelProvider;
 import org.eclipse.dltk.core.IModelStatus;
 import org.eclipse.dltk.core.IModelStatusConstants;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.IRegion;
 import org.eclipse.dltk.core.IScriptFolder;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ISearchableEnvironment;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ITypeHierarchy;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.WorkingCopyOwner;
 import org.eclipse.dltk.core.environment.EnvironmentManager;
 import org.eclipse.dltk.core.environment.EnvironmentPathUtils;
 import org.eclipse.dltk.internal.core.util.MementoTokenizer;
 import org.eclipse.dltk.internal.core.util.Messages;
 import org.eclipse.dltk.internal.core.util.Util;
 import org.eclipse.dltk.utils.CorePrinter;
 import org.osgi.service.prefs.BackingStoreException;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 public class ScriptProject extends Openable implements IScriptProject {
 	/**
 	 * Name of file containing project buildpath
 	 */
 	public static final String BUILDPATH_FILENAME = ".buildpath"; //$NON-NLS-1$
 	/**
 	 * Value of the project's raw buildpath if the .buildpath file contains
 	 * invalid entries.
 	 */
 	public static final IBuildpathEntry[] INVALID_BUILDPATH = new IBuildpathEntry[0];
 	/**
 	 * Whether the underlying file system is case sensitive.
 	 */
 	protected static final boolean IS_CASE_SENSITIVE = !new File("Temp").equals(new File("temp")); //$NON-NLS-1$ //$NON-NLS-2$
 	/**
 	 * An empty array of strings indicating that a project doesn't have any
 	 * prerequesite projects.
 	 */
 	protected static final String[] NO_PREREQUISITES = new String[0];
 	/*
 	 * Value of project's resolved buildpath while it is being resolved
 	 */
 	private static final IBuildpathEntry[] RESOLUTION_IN_PROGRESS = new IBuildpathEntry[0];
 	/**
 	 * The platform project this <code>IDylanProject</code> is based on
 	 */
 	protected IProject project;
 	private IDLTKLanguageToolkit toolkit = null;
 
 	public ScriptProject(IProject project, ModelElement parent) {
 		super(parent);
 		this.project = project;
 		toolkit = DLTKLanguageManager.findToolkit(project);
 	}
 
 	public IDLTKLanguageToolkit getLanguageToolkit() {
 		if (toolkit == null) {
 			toolkit = DLTKLanguageManager.findToolkit(project);
 		}
 		return toolkit;
 	}
 
 	/**
 	 * The path is known to match a source/library folder entry.
 	 * 
 	 * @param path
 	 *            IPath
 	 * @return IProjectFragment
 	 */
 	public IProjectFragment getFolderProjectFragment(IPath path) {
 		if (path.segmentCount() == 1) { // default project root
 			return getProjectFragment(this.project);
 		}
 		return getProjectFragment(this.project.getWorkspace().getRoot()
 				.getFolder(path));
 	}
 
 	public IProject getProject() {
 		return this.project;
 	}
 
 	public IProjectFragment getProjectFragment(IResource resource) {
 		switch (resource.getType()) {
 		case IResource.FILE:
 			if (org.eclipse.dltk.compiler.util.Util.isArchiveFileName(
					DLTKLanguageManager.getLanguageToolkit(this), resource
							.getName())) {
 				return createArchiveFragment(resource);
 			} else {
 				return null;
 			}
 		case IResource.FOLDER:
 			return new ProjectFragment(resource, this);
 		case IResource.PROJECT:
 			return new ProjectFragment(resource, this);
 		default:
 			return null;
 		}
 	}
 
 	private IProjectFragment createArchiveFragment(IResource resource) {
 		IDLTKLanguageToolkit toolkit = DLTKLanguageManager
 				.getLanguageToolkit(this);
 		if (toolkit != null) {
 			if (toolkit.languageSupportZIPBuildpath()) {
 				return new ArchiveProjectFragment(resource, this);
 			}
 		}
 		return null;
 	}
 
 	public IProjectFragment getProjectFragment(String path) {
 		return getProjectFragment(canonicalizedPath(new Path(path)));
 	}
 
 	/*
 	 * no path canonicalization
 	 */
 	public IProjectFragment getProjectFragment0(IPath archivePath) {
 		IDLTKLanguageToolkit toolkit = DLTKLanguageManager
 				.getLanguageToolkit(this);
 		if (toolkit != null) {
 			if (toolkit.languageSupportZIPBuildpath()) {
 				return new ArchiveProjectFragment(archivePath, this);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @param path
 	 *            IPath
 	 * @return A handle to the package fragment root identified by the given
 	 *         path. This method is handle-only and the element may or may not
 	 *         exist. Returns <code>null</code> if unable to generate a handle
 	 *         from the path (for example, an absolute path that has less than 1
 	 *         segment. The path may be relative or absolute.
 	 */
 	public IProjectFragment getProjectFragment(IPath path) {
 		boolean isSpecial = !path.isEmpty()
 				&& path.segment(0)
 						.startsWith(IBuildpathEntry.BUILDPATH_SPECIAL);
 		if (!path.isAbsolute() && !isSpecial) {
 			path = getPath().append(path);
 		}
 		int segmentCount = path.segmentCount();
 		switch (segmentCount) {
 		case 0:
 			return null;
 		case 1:
 			if (path.equals(getPath())) { // see
 				// https://bugs.eclipse.org/bugs/show_bug.cgi?id=75814
 				// default root
 				return getProjectFragment(this.project);
 			}
 		default:
 			// a path ending with .jar/.zip is still ambiguous and could
 			// still
 			// resolve to a source/lib folder
 			// thus will try to guess based on existing resource
 			if (isSpecial
 					&& path.segment(0).startsWith(
 							IBuildpathEntry.BUILTIN_EXTERNAL_ENTRY_STR)) {
 				return new BuiltinProjectFragment(path, this);
 			}
 			if (org.eclipse.dltk.compiler.util.Util.isArchiveFileName(
					DLTKLanguageManager.getLanguageToolkit(this), path
							.lastSegment())) {
 				IResource resource = this.project.getWorkspace().getRoot()
 						.findMember(path);
 				if (resource != null && resource.getType() == IResource.FOLDER) {
 					return getProjectFragment(resource);
 				}
 				return getProjectFragment0(path);
 			} else if (segmentCount == 1) {
 				// lib being another project
 				return getProjectFragment(this.project.getWorkspace().getRoot()
 						.getProject(path.lastSegment()));
 			} else {
 				// lib being a folder
 				IResource folder = this.project.getWorkspace().getRoot()
 						.findMember(path);
 				if (folder != null) {
 					IProjectFragment projectFragment = getProjectFragment(folder);
 					return projectFragment;
 				}
 				// No folders with such path exist in workspace, lets
 				// getAllFragments and check.
 				IProjectFragment[] fragments;
 				try {
 					fragments = getProjectFragments();
 					for (int i = 0; i < fragments.length; i++) {
 						if (fragments[i].getPath().equals(path)) {
 							return fragments[i];
 						}
 					}
 				} catch (ModelException e) {
 					if (DLTKCore.DEBUG) {
 						e.printStackTrace();
 					}
 				}
 				return null;
 			}
 		}
 	}
 
 	/*
 	 * Returns the cached resolved buildpath, or compute it ignoring unresolved
 	 * entries and cache it.
 	 */
 	public IBuildpathEntry[] getResolvedBuildpath() throws ModelException {
 		return getResolvedBuildpath(true/* ignoreUnresolvedEntry */, false/*
 																		 * don't
 																		 * generateMarkerOnError
 																		 */,
 				false/*
 					 * don't returnResolutionInProgress
 					 */); // force
 		// the
 		// reverse
 		// rawEntry
 		// cache
 		// to
 		// be
 		// populated
 	}
 
 	/*
 	 * Internal variant which can create marker on project for invalid entries
 	 * and caches the resolved buildpath on perProjectInfo. If requested, return
 	 * a special buildpath (RESOLUTION_IN_PROGRESS) if the buildpath is being
 	 * resolved.
 	 */
 	public IBuildpathEntry[] getResolvedBuildpath(
 			boolean ignoreUnresolvedEntry, boolean generateMarkerOnError,
 			boolean returnResolutionInProgress) throws ModelException {
 
 		ModelManager manager = ModelManager.getModelManager();
 		ModelManager.PerProjectInfo perProjectInfo = null;
 		if (ignoreUnresolvedEntry && !generateMarkerOnError) {
 			perProjectInfo = getPerProjectInfo();
 			if (perProjectInfo != null) {
 				// resolved path is cached on its info
 				IBuildpathEntry[] infoPath = perProjectInfo.resolvedBuildpath;
 				if (infoPath != null) {
 					return infoPath;
 				} else if (returnResolutionInProgress
 						&& manager.isBuildpathBeingResolved(this)) {
 					if (ModelManager.BP_RESOLVE_VERBOSE) {
 						Util
 								.verbose("CPResolution: reentering raw buildpath resolution, will use empty buildpath instead" + //$NON-NLS-1$
 										"	project: " + getElementName() + '\n' + //$NON-NLS-1$
 										"	invocation stack trace:"); //$NON-NLS-1$
 						new Exception("<Fake exception>").printStackTrace(System.out); //$NON-NLS-1$
 					}
 					return RESOLUTION_IN_PROGRESS;
 				}
 			}
 		}
 		Map rawReverseMap = perProjectInfo == null ? null : new HashMap(5);
 		IBuildpathEntry[] resolvedPath = null;
 		boolean nullOldResolvedCP = perProjectInfo != null
 				&& perProjectInfo.resolvedBuildpath == null;
 		try {
 			// protect against misbehaving clients (see
 			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=61040)
 			if (nullOldResolvedCP)
 				manager.setBuildpathBeingResolved(this, true);
 			resolvedPath = getResolvedBuildpath(getRawBuildpath(
 					generateMarkerOnError, !generateMarkerOnError),
 					ignoreUnresolvedEntry, generateMarkerOnError, rawReverseMap);
 		} finally {
 			if (nullOldResolvedCP)
 				perProjectInfo.resolvedBuildpath = null;
 		}
 
 		if (perProjectInfo != null) {
 			if (perProjectInfo.rawBuildpath == null // .buildpath file could not
 					// be read
 					&& generateMarkerOnError
 					&& DLTKLanguageManager.hasScriptNature(this.project)) {
 				// flush .buildpath format markers (bug 39877), but only when
 				// file cannot be read (bug 42366)
 				this.flushBuildpathProblemMarkers(false, true);
 				this.createBuildpathProblemMarker(new ModelStatus(
 						IModelStatusConstants.INVALID_BUILDPATH_FILE_FORMAT,
 						Messages.bind(
 								Messages.buildpath_cannotReadBuildpathFile,
 								this.getElementName())));
 			}
 
 			perProjectInfo.resolvedBuildpath = resolvedPath;
 			perProjectInfo.resolvedPathToRawEntries = rawReverseMap;
 			manager.setBuildpathBeingResolved(this, false);
 		}
 		return resolvedPath;
 	}
 
 	/**
 	 * Internal variant which can process any arbitrary buildpath
 	 * 
 	 * @param buildpathEntries
 	 *            IBuildpathEntry[]
 	 * @param projectOutputLocation
 	 *            IPath
 	 * @param ignoreUnresolvedEntry
 	 *            boolean
 	 * @param generateMarkerOnError
 	 *            boolean
 	 * @param rawReverseMap
 	 *            Map
 	 * @return IBuildpathEntry[]
 	 * @throws ModelException
 	 */
 	public IBuildpathEntry[] getResolvedBuildpath(
 			IBuildpathEntry[] buildpathEntries, boolean ignoreUnresolvedEntry, // if
 			// unresolved
 			// entries
 			// are
 			// met,
 			// should
 			// it
 			// trigger
 			// initializations
 			boolean generateMarkerOnError, Map rawReverseMap) // can be null
 			// if not
 			// interested in
 			// reverse
 			// mapping
 			throws ModelException {
 
 		IModelStatus status;
 		if (generateMarkerOnError) {
 			flushBuildpathProblemMarkers(false, false);
 		}
 
 		int length = buildpathEntries.length;
 		List resolvedEntries = new ArrayList();
 
 		for (int i = 0; i < length; i++) {
 
 			IBuildpathEntry rawEntry = buildpathEntries[i];
 			IPath resolvedPath;
 			status = null;
 
 			/* validation if needed */
 			if (generateMarkerOnError || !ignoreUnresolvedEntry) {
 				status = BuildpathEntry.validateBuildpathEntry(this, rawEntry,
 						false /*
 							 * do not recurse in containers, done later to
 							 * accumulate
 							 */);
 				if (generateMarkerOnError && !status.isOK()) {
 					if (status.getCode() == IModelStatusConstants.INVALID_PATH
 							&& ((BuildpathEntry) rawEntry).isOptional())
 						continue; // ignore this entry
 					createBuildpathProblemMarker(status);
 				}
 			}
 
 			switch (rawEntry.getEntryKind()) {
 
 			case IBuildpathEntry.BPE_CONTAINER:
 
 				IBuildpathContainer container = DLTKCore.getBuildpathContainer(
 						rawEntry.getPath(), this);
 				if (container == null) {
 					if (!ignoreUnresolvedEntry)
 						throw new ModelException(status);
 					break;
 				}
 
 				IBuildpathEntry[] containerEntries = container
 						.getBuildpathEntries(this);
 				if (containerEntries == null)
 					break;
 
 				// container was bound
 				for (int j = 0, containerLength = containerEntries.length; j < containerLength; j++) {
 					BuildpathEntry cEntry = (BuildpathEntry) containerEntries[j];
 					if (generateMarkerOnError) {
 						IModelStatus containerStatus = BuildpathEntry
 								.validateBuildpathEntry(this, cEntry, true /* recurse */);
 						if (!containerStatus.isOK())
 							createBuildpathProblemMarker(containerStatus);
 					}
 					// if container is exported or restricted, then its nested
 					// entries must in turn be exported (21749) and/or propagate
 					// restrictions
 					cEntry = cEntry.combineWith((BuildpathEntry) rawEntry);
 					if (rawReverseMap != null) {
 						if (rawReverseMap.get(resolvedPath = cEntry.getPath()) == null)
 							rawReverseMap.put(resolvedPath, rawEntry);
 					}
 					resolvedEntries.add(cEntry);
 				}
 				break;
 
 			default:
 
 				if (rawReverseMap != null) {
 					if (rawReverseMap.get(resolvedPath = rawEntry.getPath()) == null)
 						rawReverseMap.put(resolvedPath, rawEntry);
 				}
 				resolvedEntries.add(rawEntry);
 
 			}
 		}
 		IBuildpathEntry[] resolvedPath = new IBuildpathEntry[resolvedEntries
 				.size()];
 
 		resolvedEntries.toArray(resolvedPath);
 
 		if (generateMarkerOnError) {
 			status = BuildpathEntry.validateBuildpath(this, resolvedPath);
 			if (!status.isOK())
 				createBuildpathProblemMarker(status);
 		}
 		return resolvedPath;
 	}
 
 	/*
 	 * Internal variant which can create marker on project for invalid entries
 	 * and caches the resolved buildpath on perProjectInfo. If requested, return
 	 * a special buildpath (RESOLUTION_IN_PROGRESS) if the buildpath is being
 	 * resolved.
 	 */
 	public IBuildpathEntry[] getResolvedBuildpath(boolean ignoreUnresolvedEntry)
 			throws ModelException {
 		return getResolvedBuildpath(ignoreUnresolvedEntry, false, // don't
 				// generateMarkerOnError
 				true // returnResolutionInProgress
 		);
 	}
 
 	public IBuildpathEntry[] getResolvedBuildpath(
 			boolean ignoreUnresolvedEntry, boolean generateMarkerOnError)
 			throws ModelException {
 
 		return getResolvedBuildpath(ignoreUnresolvedEntry,
 				generateMarkerOnError, true // returnResolutionInProgress
 		);
 	}
 
 	/**
 	 * This is a helper method returning the expanded buildpath for the project,
 	 * as a list of buildpath entries, where all buildpath variable entries have
 	 * been resolved and substituted with their final target entries. All
 	 * project exports have been appended to project entries.
 	 * 
 	 * @param ignoreUnresolvedVariable
 	 *            boolean
 	 * @return IBuildpathEntry[]
 	 * @throws ModelException
 	 */
 	public IBuildpathEntry[] getExpandedBuildpath(
 			boolean ignoreUnresolvedVariable) throws ModelException {
 
 		return getExpandedBuildpath(ignoreUnresolvedVariable, false/*
 																	 * don't
 																	 * create
 																	 * markers
 																	 */, null);
 	}
 
 	/**
 	 * Internal variant which can create marker on project for invalid entries,
 	 * it will also perform buildpath expansion in presence of project
 	 * prerequisites exporting their entries.
 	 * 
 	 * @param ignoreUnresolvedVariable
 	 *            boolean
 	 * @param generateMarkerOnError
 	 *            boolean
 	 * @param preferredBuildpaths
 	 *            Map
 	 * @param preferredOutputs
 	 *            Map
 	 * @return IBuildpathEntry[]
 	 * @throws ModelException
 	 */
 	public IBuildpathEntry[] getExpandedBuildpath(
 			boolean ignoreUnresolvedVariable, boolean generateMarkerOnError,
 			Map preferredBuildpaths) throws ModelException {
 
 		ObjectVector accumulatedEntries = new ObjectVector();
 		computeExpandedBuildpath(null, ignoreUnresolvedVariable,
 				generateMarkerOnError, new HashSet(5), accumulatedEntries,
 				preferredBuildpaths);
 
 		IBuildpathEntry[] expandedPath = new IBuildpathEntry[accumulatedEntries
 				.size()];
 		accumulatedEntries.copyInto(expandedPath);
 
 		return expandedPath;
 	}
 
 	/**
 	 * Internal computation of an expanded buildpath. It will eliminate
 	 * duplicates, and produce copies of exported or restricted buildpath
 	 * entries to avoid possible side-effects ever after.
 	 */
 	private void computeExpandedBuildpath(BuildpathEntry referringEntry,
 			boolean ignoreUnresolvedVariable, boolean generateMarkerOnError,
 			HashSet rootIDs, ObjectVector accumulatedEntries,
 			Map preferredBuildpaths) throws ModelException {
 
 		String projectRootId = this.rootID();
 		if (rootIDs.contains(projectRootId)) {
 			return; // break cycles if any
 		}
 		rootIDs.add(projectRootId);
 
 		IBuildpathEntry[] preferredBuildpath = preferredBuildpaths != null ? (IBuildpathEntry[]) preferredBuildpaths
 				.get(this)
 				: null;
 		IBuildpathEntry[] immediateBuildpath = preferredBuildpath != null ? getResolvedBuildpath(
 				preferredBuildpath, ignoreUnresolvedVariable,
 				generateMarkerOnError, null /* no reverse map */)
 				: getResolvedBuildpath(ignoreUnresolvedVariable,
 						generateMarkerOnError, false/*
 													 * don't
 													 * returnResolutionInProgress
 													 */);
 
 		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
 		boolean isInitialProject = referringEntry == null;
 		for (int i = 0, length = immediateBuildpath.length; i < length; i++) {
 			BuildpathEntry entry = (BuildpathEntry) immediateBuildpath[i];
 			if (isInitialProject || entry.isExported()) {
 				String rootID = entry.rootID();
 				if (rootIDs.contains(rootID)) {
 					continue;
 				}
 				// combine restrictions along the project chain
 				BuildpathEntry combinedEntry = entry
 						.combineWith(referringEntry);
 				accumulatedEntries.add(combinedEntry);
 
 				// recurse in project to get all its indirect exports (only
 				// consider exported entries from there on)
 				if (entry.getEntryKind() == IBuildpathEntry.BPE_PROJECT) {
 					IResource member = workspaceRoot
 							.findMember(entry.getPath());
 					if (member != null && member.getType() == IResource.PROJECT) { // double
 						// check
 						// if
 						// bound
 						// to
 						// project
 						// (23977)
 						IProject projRsc = (IProject) member;
 						if (ScriptProject.hasScriptNature(projRsc)) {
 							ScriptProject scriptProject = (ScriptProject) DLTKCore
 									.create(projRsc);
 							scriptProject.computeExpandedBuildpath(
 									combinedEntry, ignoreUnresolvedVariable,
 									false /*
 										 * no marker when recursing in prereq
 										 */, rootIDs, accumulatedEntries,
 									preferredBuildpaths);
 						}
 					}
 				} else {
 					rootIDs.add(rootID);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Computes the project fragments identified by the given entry. Only works
 	 * with resolved entry
 	 * 
 	 * @param resolvedEntry
 	 *            IBuildpathEntry
 	 * @return IProjectFragment[]
 	 */
 	public IProjectFragment[] computeProjectFragments(
 			IBuildpathEntry resolvedEntry) {
 		try {
 			return computeProjectFragments(
 					new IBuildpathEntry[] { resolvedEntry }, false, // don't
 					// retrieve
 					// exported
 					// roots
 					null /* no reverse map */
 			);
 		} catch (ModelException e) {
 			return new IProjectFragment[] {};
 		}
 	}
 
 	public int getElementType() {
 		return SCRIPT_PROJECT;
 	}
 
 	protected boolean buildStructure(OpenableElementInfo info,
 			IProgressMonitor pm, Map newElements, IResource underlyingResource)
 			throws ModelException {
 		// check whether the dltk project can be opened
 
 		if (!underlyingResource.isAccessible()) {
 			throw newNotPresentException();
 		}
 
 		// cannot refresh bp markers on opening (emulate cp check on startup)
 		// since can create deadlocks (see bug 37274)
 		IBuildpathEntry[] resolvedBuildpath = getResolvedBuildpath();
 		// compute the project fragements
 		IModelElement[] children = computeProjectFragments(resolvedBuildpath,
 				false, null);
 		setProjectInfoChildren(info, children);
 
 		// remember the timestamps of external libraries the first time they are
 		// looked up
 		getPerProjectInfo().rememberExternalLibTimestamps();
 		return true;
 	}
 
 	private void setProjectInfoChildren(OpenableElementInfo info,
 			IModelElement[] children) {
 		List fragments = new ArrayList();
 		fragments.addAll(Arrays.asList(children));
 		// Call for extra model providers
 		IDLTKLanguageToolkit toolkit = DLTKLanguageManager
 				.getLanguageToolkit(this);
 		IModelProvider[] providers = ModelProviderManager.getProviders(toolkit
 				.getNatureId());
 		if (providers != null) {
 			for (int i = 0; i < providers.length; i++) {
 				providers[i].provideModelChanges(this, fragments);
 			}
 		}
 
 		info.setChildren((IModelElement[]) fragments
 				.toArray(new IModelElement[fragments.size()]));
 	}
 
 	public ModelManager.PerProjectInfo getPerProjectInfo()
 			throws ModelException {
 		return ModelManager.getModelManager().getPerProjectInfoCheckExistence(
 				this.project);
 	}
 
 	/**
 	 * Returns (local/all) the project fragments identified by the given
 	 * project's buildpath. Note: this follows project buildpath references to
 	 * find required project contributions, eliminating duplicates silently.
 	 * Only works with resolved entries
 	 * 
 	 * @param resolvedBuildpath
 	 *            IBuildpathEntry[]
 	 * @param retrieveExportedRoots
 	 *            boolean
 	 * @return IProjectFragment[]
 	 * @throws ModelException
 	 */
 	public IProjectFragment[] computeProjectFragments(
 			IBuildpathEntry[] resolvedBuildpath, boolean retrieveExportedRoots,
 			Map rootToResolvedEntries) throws ModelException {
 		ObjectVector accumulatedRoots = new ObjectVector();
 		computeProjectFragments(resolvedBuildpath, accumulatedRoots,
 				new HashSet(5), // rootIDs
 				null, // inside original project
 				true, // check existency
 				retrieveExportedRoots, rootToResolvedEntries);
 
 		List fragments = accumulatedRoots.asList();
 		IProjectFragment[] rootArray = new IProjectFragment[fragments.size()];
 		fragments.toArray(rootArray);
 		return rootArray;
 	}
 
 	/**
 	 * Returns (local/all) the package fragment roots identified by the given
 	 * project's buildpath. Note: this follows project buildpath references to
 	 * find required project contributions, eliminating duplicates silently.
 	 * Only works with resolved entries
 	 * 
 	 * @param resolvedBuildpath
 	 *            IBuildpathEntry[]
 	 * @param accumulatedRoots
 	 *            List
 	 * @param rootIDs
 	 *            HashSet
 	 * @param referringEntry
 	 *            project entry referring to this CP or null if initial project
 	 * @param checkExistency
 	 *            boolean
 	 * @param retrieveExportedRoots
 	 *            boolean
 	 * @throws ModelException
 	 */
 	public void computeProjectFragments(IBuildpathEntry[] resolvedBuildpath,
 			ObjectVector accumulatedRoots, HashSet rootIDs,
 			IBuildpathEntry referringEntry, boolean checkExistency,
 			boolean retrieveExportedRoots, Map rootToResolvedEntries)
 			throws ModelException {
 		if (referringEntry == null) {
 			rootIDs.add(rootID());
 		}
 		for (int i = 0, length = resolvedBuildpath.length; i < length; i++) {
 			computeProjectFragments(resolvedBuildpath[i], accumulatedRoots,
 					rootIDs, referringEntry, checkExistency,
 					retrieveExportedRoots, rootToResolvedEntries);
 		}
 
 	}
 
 	/**
 	 * Returns the package fragment roots identified by the given entry. In case
 	 * it refers to a project, it will follow its buildpath so as to find
 	 * exported roots as well. Only works with resolved entry
 	 * 
 	 * @param resolvedEntry
 	 *            IBuildpathEntry
 	 * @param accumulatedRoots
 	 *            List
 	 * @param rootIDs
 	 *            HashSet
 	 * @param referringEntry
 	 *            the BP entry (project) referring to this entry, or null if
 	 *            initial project
 	 * @param checkExistency
 	 *            boolean
 	 * @param retrieveExportedRoots
 	 *            boolean
 	 * @throws ModelException
 	 */
 	public void computeProjectFragments(IBuildpathEntry resolvedEntry,
 			ObjectVector accumulatedRoots, HashSet rootIDs,
 			IBuildpathEntry referringEntry, boolean checkExistency,
 			boolean retrieveExportedRoots, Map rootToResolvedEntries)
 			throws ModelException {
 		String rootID = ((BuildpathEntry) resolvedEntry).rootID();
 		if (rootIDs.contains(rootID))
 			return;
 		IPath projectPath = this.project.getFullPath();
 		IPath entryPath = resolvedEntry.getPath();
 		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
 		IProjectFragment root = null;
 		switch (resolvedEntry.getEntryKind()) {
 		// source folder
 		case IBuildpathEntry.BPE_SOURCE:
 			if (projectPath.isPrefixOf(entryPath)) {
 				if (checkExistency) {
 					Object target = Model.getTarget(workspaceRoot, entryPath,
 							checkExistency);
 					if (target == null)
 						return;
 					if (target instanceof IFolder || target instanceof IProject) {
 						root = getProjectFragment((IResource) target);
 					}
 				} else {
 					root = getFolderProjectFragment(entryPath);
 				}
 			}
 			break;
 		// internal/external ZIP or folder
 		case IBuildpathEntry.BPE_LIBRARY:
 			if (referringEntry != null && !resolvedEntry.isExported())
 				return;
 			if (checkExistency) {
 				if (entryPath.segment(0).startsWith(
 						IBuildpathEntry.BUILTIN_EXTERNAL_ENTRY_STR)
 						&& BuiltinProjectFragment.isSupported(this)) {
 					root = new BuiltinProjectFragment(entryPath, this);
 					break;
 				}
 				Object target = Model.getTarget(workspaceRoot, entryPath,
 						checkExistency);
 				if (target == null)
 					return;
 				if (!resolvedEntry.isExternal()) {
 					if (target instanceof IResource) {
 						// internal target
 						root = getProjectFragment((IResource) target);
 					}
 				} else {// external target
 					// This is external folder or zip.
 					if (Model.isFile(target)
 							&& (org.eclipse.dltk.compiler.util.Util
									.isArchiveFileName(DLTKLanguageManager
											.getLanguageToolkit(this),
 											entryPath.lastSegment()))) {
 						// root = new ArchiveProjectFragment(entryPath, this);
 						root = getProjectFragment0(entryPath);
 					} else {
 						if (resolvedEntry.isContainerEntry()) {
 							root = new ExternalProjectFragment(entryPath, this,
 									true, true);
 						} else {
 							root = new ExternalProjectFragment(entryPath, this,
 									true, true);
 						}
 					}
 				}
 			} else {
 				root = getProjectFragment(entryPath);
 			}
 			break;
 		// recurse into required project
 		case IBuildpathEntry.BPE_PROJECT:
 			if (!retrieveExportedRoots)
 				return;
 			if (referringEntry != null && !resolvedEntry.isExported())
 				return;
 			IResource member = workspaceRoot.findMember(entryPath);
 			if (member != null && member.getType() == IResource.PROJECT) {// double
 				/*
 				 * check if bound to project (23977)
 				 */
 				IProject requiredProjectRsc = (IProject) member;
 				rootIDs.add(rootID);
 				ScriptProject requiredProject = (ScriptProject) DLTKCore
 						.create(requiredProjectRsc);
 				requiredProject
 						.computeProjectFragments(
 								requiredProject.getResolvedBuildpath(),
 								accumulatedRoots,
 								rootIDs,
 								rootToResolvedEntries == null ? resolvedEntry
 										: ((BuildpathEntry) resolvedEntry)
 												.combineWith((BuildpathEntry) referringEntry),
 								checkExistency, retrieveExportedRoots,
 								rootToResolvedEntries);
 			}
 			break;
 		}
 		if (root != null) {
 			accumulatedRoots.add(root);
 			rootIDs.add(rootID);
 			if (rootToResolvedEntries != null)
 				rootToResolvedEntries.put(root,
 						((BuildpathEntry) resolvedEntry)
 								.combineWith((BuildpathEntry) referringEntry));
 		}
 	}
 
 	public String[] projectPrerequisites(IBuildpathEntry[] entries)
 			throws ModelException {
 		ArrayList prerequisites = new ArrayList();
 		// need resolution
 		entries = getResolvedBuildpath(entries, true, false, null/*
 																 * no reverse
 																 * map
 																 */);
 		for (int i = 0, length = entries.length; i < length; i++) {
 			IBuildpathEntry entry = entries[i];
 			if (entry.getEntryKind() == IBuildpathEntry.BPE_PROJECT) {
 				prerequisites.add(entry.getPath().lastSegment());
 			}
 		}
 		int size = prerequisites.size();
 		if (size == 0) {
 			return NO_PREREQUISITES;
 		} else {
 			String[] result = new String[size];
 			prerequisites.toArray(result);
 			return result;
 		}
 	}
 
 	/**
 	 * Returns a default build path. This is the root of the project
 	 */
 	protected IBuildpathEntry[] defaultBuildpath() {
 		return new IBuildpathEntry[] { DLTKCore.newSourceEntry(this.project
 				.getFullPath()) };
 	}
 
 	/**
 	 * Answers an ID which is used to distinguish project/entries during project
 	 * fragment root computations
 	 * 
 	 * @return String
 	 */
 	public String rootID() {
 		return "[PRJ]" + this.project.getFullPath(); //$NON-NLS-1$
 	}
 
 	protected Object createElementInfo() {
 		return new ProjectElementInfo();
 	}
 
 	public IResource getResource() {
 		return project;
 	}
 
 	public IPath getPath() {
 		return project.getFullPath();
 	}
 
 	/**
 	 * Returns a canonicalized path from the given external path. Note that the
 	 * return path contains the same number of segments and it contains a device
 	 * only if the given path contained one.
 	 * 
 	 * @param externalPath
 	 *            IPath
 	 * @see java.io.File for the definition of a canonicalized path
 	 * @return IPath
 	 */
 	public static IPath canonicalizedPath(IPath externalPath) {
 		if (externalPath == null)
 			return null;
 		if (IS_CASE_SENSITIVE) {
 			return externalPath;
 		}
 		// if not external path, return original path
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		if (workspace == null)
 			return externalPath; // protection during shutdown (30487)
 		if (workspace.getRoot().findMember(externalPath) != null) {
 			return externalPath;
 		}
 		// TODO Check this
 		return externalPath;
 	}
 
 	/**
 	 * Convenience method that returns the specific type of info for a project.
 	 */
 	protected ProjectElementInfo getProjectElementInfo() throws ModelException {
 		return (ProjectElementInfo) getElementInfo();
 	}
 
 	/*
 	 * Resets this project's caches
 	 */
 	public void resetCaches() {
 		ProjectElementInfo info = (ProjectElementInfo) ModelManager
 				.getModelManager().peekAtInfo(this);
 		if (info != null) {
 			info.resetCaches();
 		}
 	}
 
 	/**
 	 * Reset the collection of project fragments (local ones) - only if opened.
 	 */
 	public void updateProjectFragments() {
 		if (this.isOpen()) {
 			try {
 				ProjectElementInfo info = getProjectElementInfo();
 				computeChildren(info);
 				info.resetCaches(); // discard caches (hold onto roots and pkg
 				// fragments)
 			} catch (ModelException e) {
 				try {
 					close(); // could not do better
 				} catch (ModelException ex) {
 					// ignore
 				}
 			}
 		}
 	}
 
 	/**
 	 * Computes the collection of project fragments (local ones) and set it on
 	 * the given info. Need to check *all* project fragments in order to reset
 	 * NameLookup
 	 * 
 	 * @param info
 	 *            ProjectElementInfo
 	 * @throws ModelException
 	 */
 	public void computeChildren(ProjectElementInfo info) throws ModelException {
 		IBuildpathEntry[] buildpath = getResolvedBuildpath();
 		ProjectElementInfo.ProjectCache projectCache = info.projectCache;
 		if (projectCache != null) {
 			IProjectFragment[] newRoots = computeProjectFragments(buildpath,
 					true, null /*
 								 * no reverse map
 								 */);
 			checkIdentical: { // compare all pkg fragment root lists
 				IProjectFragment[] oldRoots = projectCache.allProjectFragmentCache;
 				if (oldRoots.length == newRoots.length) {
 					for (int i = 0, length = oldRoots.length; i < length; i++) {
 						if (!oldRoots[i].equals(newRoots[i])) {
 							break checkIdentical;
 						}
 					}
 					return; // no need to update
 				}
 			}
 		}
 		info.setForeignResources(null);
 		IProjectFragment[] fragments = computeProjectFragments(buildpath,
 				false, null);
 		setProjectInfoChildren(info, fragments);
 	}
 
 	public IBuildpathEntry[] getRawBuildpath() throws ModelException {
 		// Do not create marker but log problems while getting raw buildpath
 		return getRawBuildpath(false, true);
 	}
 
 	/*
 	 * Internal variant allowing to parameterize problem creation/logging
 	 */
 	public IBuildpathEntry[] getRawBuildpath(boolean createMarkers,
 			boolean logProblems) throws ModelException {
 		ModelManager.PerProjectInfo perProjectInfo = null;
 		IBuildpathEntry[] buildpath;
 		if (createMarkers) {
 			flushBuildpathProblemMarkers(false/* cycle */, true/* format */);
 			buildpath = readBuildpathFile(createMarkers, logProblems);
 		} else {
 			perProjectInfo = getPerProjectInfo();
 			buildpath = perProjectInfo.rawBuildpath;
 			if (buildpath != null)
 				return buildpath;
 			buildpath = readBuildpathFile(createMarkers, logProblems);
 		}
 		if (buildpath == null) {
 			return defaultBuildpath();
 		}
 		if (!createMarkers) {
 			perProjectInfo.rawBuildpath = buildpath;
 		}
 		return buildpath;
 	}
 
 	/**
 	 * Reads the .buildpath file from disk and returns the list of entries it
 	 * contains (including output location entry) Returns null if .classfile is
 	 * not present. Returns INVALID_CLASSPATH if it has a format problem.
 	 */
 	protected IBuildpathEntry[] readBuildpathFile(boolean createMarker,
 			boolean logProblems) {
 		return readBuildpathFile(createMarker, logProblems, null/*
 																 * not
 																 * interested in
 																 * unknown
 																 * elements
 																 */);
 	}
 
 	protected IBuildpathEntry[] readBuildpathFile(boolean createMarker,
 			boolean logProblems, Map unknownElements) {
 		try {
 			String xmlBuildpath = getSharedProperty(BUILDPATH_FILENAME);
 			if (xmlBuildpath == null) {
 				if (createMarker && this.project.isAccessible()) {
 					createBuildpathProblemMarker(new ModelStatus(
 							IModelStatusConstants.INVALID_BUILDPATH_FILE_FORMAT,
 							Messages.bind(
 									Messages.buildpath_cannotReadBuildpathFile,
 									this.getElementName())));
 				}
 				return null;
 			}
 			return decodeBuildpath(xmlBuildpath, createMarker, logProblems,
 					unknownElements);
 		} catch (CoreException e) {
 			// file does not exist (or not accessible)
 			if (createMarker && this.project.isAccessible()) {
 				createBuildpathProblemMarker(new ModelStatus(
 						IModelStatusConstants.INVALID_BUILDPATH_FILE_FORMAT,
 						Messages.bind(
 								Messages.buildpath_cannotReadBuildpathFile,
 								this.getElementName())));
 			}
 			if (logProblems) {
 				Util.log(e, "Exception while retrieving " + this.getPath() //$NON-NLS-1$
 						+ "/.buildpath, will revert to default buildpath"); //$NON-NLS-1$
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Record a new marker denoting a buildpath problem
 	 */
 	void createBuildpathProblemMarker(IModelStatus status) {
 		IMarker marker = null;
 		int severity;
 		String[] arguments = new String[0];
 		boolean isCycleProblem = false, isBuildpathFileFormatProblem = false;
 		switch (status.getCode()) {
 		case IModelStatusConstants.BUILDPATH_CYCLE:
 			isCycleProblem = true;
 			if (DLTKCore.ERROR.equals(getOption(
 					DLTKCore.CORE_CIRCULAR_BUILDPATH, true))) {
 				severity = IMarker.SEVERITY_ERROR;
 			} else {
 				severity = IMarker.SEVERITY_WARNING;
 			}
 			break;
 		case IModelStatusConstants.INVALID_BUILDPATH_FILE_FORMAT:
 			isBuildpathFileFormatProblem = true;
 			severity = IMarker.SEVERITY_ERROR;
 			break;
 		default:
 			IPath path = status.getPath();
 			if (path != null) {
 				arguments = new String[] { path.toString() };
 			}
 			if (DLTKCore.ERROR.equals(getOption(
 					DLTKCore.CORE_INCOMPLETE_BUILDPATH, true))) {
 				severity = IMarker.SEVERITY_ERROR;
 			} else {
 				severity = IMarker.SEVERITY_WARNING;
 			}
 			break;
 		}
 		try {
 			marker = this.project
 					.createMarker(IModelMarker.BUILDPATH_PROBLEM_MARKER);
 			marker.setAttributes(new String[] { IMarker.MESSAGE,
 					IMarker.SEVERITY, IMarker.LOCATION,
 					IModelMarker.CYCLE_DETECTED,
 					IModelMarker.BUILDPATH_FILE_FORMAT, IModelMarker.ID,
 					IModelMarker.ARGUMENTS, }, new Object[] {
 					status.getMessage(),
 					new Integer(severity),
 					Messages.buildpath_buildPath,
 					isCycleProblem ? "true" : "false",//$NON-NLS-1$ //$NON-NLS-2$
 					isBuildpathFileFormatProblem ? "true" : "false",//$NON-NLS-1$ //$NON-NLS-2$
 					new Integer(status.getCode()),
 					Util.getProblemArgumentsForMarker(arguments), });
 		} catch (CoreException e) {
 			// could not create marker: cannot do much
 			if (ModelManager.VERBOSE) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Remove all markers denoting buildpath problems
 	 */
 	protected void flushBuildpathProblemMarkers(boolean flushCycleMarkers,
 			boolean flushBuildpathFormatMarkers) {
 		try {
 			if (this.project.isAccessible()) {
 				IMarker[] markers = this.project.findMarkers(
 						IModelMarker.BUILDPATH_PROBLEM_MARKER, false,
 						IResource.DEPTH_ZERO);
 				for (int i = 0, length = markers.length; i < length; i++) {
 					IMarker marker = markers[i];
 					if (flushCycleMarkers && flushBuildpathFormatMarkers) {
 						marker.delete();
 					} else {
 						String cycleAttr = (String) marker
 								.getAttribute(IModelMarker.CYCLE_DETECTED);
 						String buildpathFileFormatAttr = (String) marker
 								.getAttribute(IModelMarker.BUILDPATH_FILE_FORMAT);
 						if ((flushCycleMarkers == (cycleAttr != null && cycleAttr
 								.equals("true"))) //$NON-NLS-1$
 								&& (flushBuildpathFormatMarkers == (buildpathFileFormatAttr != null && buildpathFileFormatAttr
 										.equals("true")))) { //$NON-NLS-1$
 							marker.delete();
 						}
 					}
 				}
 			}
 		} catch (CoreException e) {
 			// could not flush markers: not much we can do
 			if (ModelManager.VERBOSE) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Reads and decode an XML buildpath string
 	 */
 	protected IBuildpathEntry[] decodeBuildpath(String xmlBuildpath,
 			boolean createMarker, boolean logProblems) {
 		return decodeBuildpath(xmlBuildpath, createMarker, logProblems, null/*
 																			 * not
 																			 * interested
 																			 * in
 																			 * unknown
 																			 * elements
 																			 */);
 	}
 
 	/**
 	 * Reads and decode an XML buildpath string
 	 */
 	protected IBuildpathEntry[] decodeBuildpath(String xmlBuildpath,
 			boolean createMarker, boolean logProblems, Map unknownElements) {
 		ArrayList paths = new ArrayList();
 		try {
 			if (xmlBuildpath == null)
 				return null;
 			StringReader reader = new StringReader(xmlBuildpath);
 			Element cpElement;
 			try {
 				DocumentBuilder parser = DocumentBuilderFactory.newInstance()
 						.newDocumentBuilder();
 				cpElement = parser.parse(new InputSource(reader))
 						.getDocumentElement();
 			} catch (SAXException e) {
 				throw new IOException(Messages.file_badFormat);
 			} catch (ParserConfigurationException e) {
 				throw new IOException(Messages.file_badFormat);
 			} finally {
 				reader.close();
 			}
 			if (!cpElement.getNodeName().equalsIgnoreCase("buildpath")) { //$NON-NLS-1$
 				throw new IOException(Messages.file_badFormat);
 			}
 			NodeList list = cpElement.getElementsByTagName("buildpathentry"); //$NON-NLS-1$
 			int length = list.getLength();
 			for (int i = 0; i < length; ++i) {
 				Node node = list.item(i);
 				if (node.getNodeType() == Node.ELEMENT_NODE) {
 					IBuildpathEntry entry = BuildpathEntry.elementDecode(
 							(Element) node, this, unknownElements);
 					if (entry != null)
 						paths.add(entry);
 				}
 			}
 		} catch (IOException e) {
 			// bad format
 			if (createMarker && this.project.isAccessible()) {
 				this.createBuildpathProblemMarker(new ModelStatus(
 						IModelStatusConstants.INVALID_BUILDPATH_FILE_FORMAT,
 						Messages.bind(Messages.buildpath_xmlFormatError,
 								new String[] { this.getElementName(),
 										e.getMessage() })));
 			}
 			if (logProblems) {
 				Util.log(e, "Exception while retrieving " + this.getPath() //$NON-NLS-1$
 						+ "/.buildpath, will mark buildpath as invalid"); //$NON-NLS-1$
 			}
 			return INVALID_BUILDPATH;
 		} catch (AssertionFailedException e) {
 			// failed creating CP entries from file
 			if (createMarker && this.project.isAccessible()) {
 				this.createBuildpathProblemMarker(new ModelStatus(
 						IModelStatusConstants.INVALID_BUILDPATH_FILE_FORMAT,
 						Messages.bind(
 								Messages.buildpath_illegalEntryInBuildpathFile,
 								new String[] { this.getElementName(),
 										e.getMessage() })));
 			}
 			if (logProblems) {
 				Util.log(e, "Exception while retrieving " + this.getPath() //$NON-NLS-1$
 						+ "/.buildpath, will mark buildpath as invalid"); //$NON-NLS-1$
 			}
 			return INVALID_BUILDPATH;
 		}
 		IBuildpathEntry[] entries = new IBuildpathEntry[paths.size()];
 		paths.toArray(entries);
 		return entries;
 	}
 
 	/*
 	 * Reads and decode an XML buildpath string
 	 */
 	public IBuildpathEntry[] decodeBuildpath(String xmlBuildpath,
 			Map unknownElements) throws IOException, AssertionFailedException {
 		ArrayList paths = new ArrayList();
 		StringReader reader = new StringReader(xmlBuildpath);
 		Element cpElement;
 		try {
 			DocumentBuilder parser = DocumentBuilderFactory.newInstance()
 					.newDocumentBuilder();
 			cpElement = parser.parse(new InputSource(reader))
 					.getDocumentElement();
 		} catch (SAXException e) {
 			throw new IOException(Messages.file_badFormat);
 		} catch (ParserConfigurationException e) {
 			throw new IOException(Messages.file_badFormat);
 		} finally {
 			reader.close();
 		}
 		if (!cpElement.getNodeName().equalsIgnoreCase("buildpath")) { //$NON-NLS-1$
 			throw new IOException(Messages.file_badFormat);
 		}
 		NodeList list = cpElement.getElementsByTagName("buildpathentry"); //$NON-NLS-1$
 		int length = list.getLength();
 		for (int i = 0; i < length; ++i) {
 			Node node = list.item(i);
 			if (node.getNodeType() == Node.ELEMENT_NODE) {
 				IBuildpathEntry entry = BuildpathEntry.elementDecode(
 						(Element) node, this, unknownElements);
 				if (entry != null) {
 					paths.add(entry);
 				}
 			}
 		}
 		// return a new empty buildpath is it size is 0, to differenciate from
 		// an INVALID_BUILDPATH
 		int pathSize = paths.size();
 		IBuildpathEntry[] entries = new IBuildpathEntry[pathSize];
 		paths.toArray(entries);
 		return entries;
 	}
 
 	public IBuildpathEntry decodeBuildpathEntry(String encodedEntry) {
 		try {
 			if (encodedEntry == null)
 				return null;
 			StringReader reader = new StringReader(encodedEntry);
 			Element node;
 			try {
 				DocumentBuilder parser = DocumentBuilderFactory.newInstance()
 						.newDocumentBuilder();
 				node = parser.parse(new InputSource(reader))
 						.getDocumentElement();
 			} catch (SAXException e) {
 				return null;
 			} catch (ParserConfigurationException e) {
 				return null;
 			} finally {
 				reader.close();
 			}
 			if (!node.getNodeName().equalsIgnoreCase("buildpathentry") //$NON-NLS-1$
 					|| node.getNodeType() != Node.ELEMENT_NODE) {
 				return null;
 			}
 			return BuildpathEntry.elementDecode(node, this, null/*
 																 * not
 																 * interested in
 																 * unknown
 																 * elements
 																 */);
 		} catch (IOException e) {
 			// bad format
 			return null;
 		}
 	}
 
 	/**
 	 * Retrieve a shared property on a project. If the property is not defined,
 	 * answers null. Note that it is orthogonal to IResource persistent
 	 * properties, and client code has to decide which form of storage to use
 	 * appropriately. Shared properties produce real resource files which can be
 	 * shared through a VCM onto a server. Persistent properties are not
 	 * shareable.
 	 * 
 	 * @param key
 	 *            String
 	 * @see IScriptProject#setSharedProperty(String, String)
 	 * @return String
 	 * @throws CoreException
 	 */
 	public String getSharedProperty(String key) throws CoreException {
 		String property = null;
 		IFile rscFile = this.project.getFile(key);
 		if (rscFile.exists()) {
 			byte[] bytes = Util.getResourceContentsAsByteArray(rscFile);
 			try {
 				property = new String(bytes,
 						org.eclipse.dltk.compiler.util.Util.UTF_8); // .buildpath
 				// always
 				// encoded
 				// with
 				// UTF-8
 			} catch (UnsupportedEncodingException e) {
 				Util.log(e, "Could not read .buildpath with UTF-8 encoding"); //$NON-NLS-1$
 				// fallback to default
 				property = new String(bytes);
 			}
 		} else {
 			// when a project is imported, we get a first delta for the addition
 			// of the .project, but the .buildpath is not accessible
 			// so default to using java.io.File
 			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=96258
 			URI location = rscFile.getLocationURI();
 			if (location != null) {
 				File file = Util.toLocalFile(location, null/*
 															 * no progress
 															 * monitor available
 															 */);
 				if (file != null && file.exists()) {
 					byte[] bytes;
 					try {
 						bytes = org.eclipse.dltk.compiler.util.Util
 								.getFileByteContent(file);
 					} catch (IOException e) {
 						return null;
 					}
 					try {
 						property = new String(bytes,
 								org.eclipse.dltk.compiler.util.Util.UTF_8); // .
 						// buildpath
 						// always
 						// encoded
 						// with
 						// UTF-8
 					} catch (UnsupportedEncodingException e) {
 						Util
 								.log(e,
 										"Could not read .buildpath with UTF-8 encoding"); //$NON-NLS-1$
 						// fallback to default
 						property = new String(bytes);
 					}
 				}
 			}
 		}
 		return property;
 	}
 
 	public IProjectFragment[] getProjectFragments() throws ModelException {
 		Object[] children;
 		int length;
 		IProjectFragment[] roots;
 		System.arraycopy(children = getChildren(), 0,
 				roots = new IProjectFragment[length = children.length], 0,
 				length);
 		return roots;
 	}
 
 	/**
 	 * Saves the buildpath in a shareable format (VCM-wise) only when necessary,
 	 * that is, if it is semantically different from the existing one in file.
 	 * Will never write an identical one.
 	 * 
 	 * @param newBuildpath
 	 *            IBuildpathEntry[]
 	 * @param newOutputLocation
 	 *            IPath
 	 * @return boolean Return whether the .buildpath file was modified.
 	 * @throws ModelException
 	 */
 	public boolean saveBuildpath(IBuildpathEntry[] newBuildpath)
 			throws ModelException {
 		if (!this.project.isAccessible())
 			return false;
 		Map unknownElements = new HashMap();
 		IBuildpathEntry[] fileEntries = readBuildpathFile(false /*
 																 * don't create
 																 * markers
 																 */, false/*
 																		 * don't
 																		 * log
 																		 * problems
 																		 */,
 				unknownElements);
 		if (fileEntries != null
 				&& isBuildpathEqualsTo(newBuildpath, fileEntries)) {
 			// no need to save it, it is the same
 			return false;
 		}
 		// actual file saving
 		try {
 			setSharedProperty(ScriptProject.BUILDPATH_FILENAME,
 					encodeBuildpath(newBuildpath, true, unknownElements));
 			return true;
 		} catch (CoreException e) {
 			throw new ModelException(e);
 		} catch (NullPointerException e) {
 			e.printStackTrace();
 			throw e;
 		}
 	}
 
 	/**
 	 * Record a shared persistent property onto a project. Note that it is
 	 * orthogonal to IResource persistent properties, and client code has to
 	 * decide which form of storage to use appropriately. Shared properties
 	 * produce real resource files which can be shared through a VCM onto a
 	 * server. Persistent properties are not shareable.
 	 * 
 	 * shared properties end up in resource files, and thus cannot be modified
 	 * during delta notifications (a CoreException would then be thrown).
 	 * 
 	 * @param key
 	 *            String
 	 * @param value
 	 *            String
 	 * @see IScriptProject#getSharedProperty(String key)
 	 * @throws CoreException
 	 */
 	public void setSharedProperty(String key, String value)
 			throws CoreException {
 		IFile rscFile = this.project.getFile(key);
 		byte[] bytes = null;
 		try {
 			bytes = value.getBytes(org.eclipse.dltk.compiler.util.Util.UTF_8); // .
 			// buildpath
 			// always
 			// encoded
 			// with
 			// UTF-8
 		} catch (UnsupportedEncodingException e) {
 			Util.log(e, "Could not write .buildpath with UTF-8 encoding "); //$NON-NLS-1$
 			// fallback to default
 			bytes = value.getBytes();
 		}
 		InputStream inputStream = new ByteArrayInputStream(bytes);
 		// update the resource content
 		if (rscFile.exists()) {
 			if (rscFile.isReadOnly()) {
 				// provide opportunity to checkout read-only .buildpath file
 				// (23984)
 				ResourcesPlugin.getWorkspace().validateEdit(
 						new IFile[] { rscFile }, null);
 			}
 			rscFile.setContents(inputStream, IResource.FORCE, null);
 		} else {
 			rscFile.create(inputStream, IResource.FORCE, null);
 		}
 	}
 
 	/**
 	 * Compare current buildpath with given one to see if any different. Note
 	 * that the argument buildpath contains its binary output.
 	 * 
 	 * @param newBuildpath
 	 *            IBuildpathEntry[]
 	 * @param newOutputLocation
 	 *            IPath
 	 * @param otherBuildpathWithOutput
 	 *            IBuildpathEntry[]
 	 * @return boolean
 	 */
 	public boolean isBuildpathEqualsTo(IBuildpathEntry[] newBuildpath,
 			IBuildpathEntry[] otherBuildpath) {
 		if (otherBuildpath == null || otherBuildpath.length == 0)
 			return false;
 		int length = otherBuildpath.length;
 		if (length != newBuildpath.length)
 			// output is amongst file entries (last one)
 			return false;
 		// compare buildpath entries
 		for (int i = 0; i < length; i++) {
 			if (!otherBuildpath[i].equals(newBuildpath[i]))
 				return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Returns the XML String encoding of the buildpath.
 	 */
 	protected String encodeBuildpath(IBuildpathEntry[] buildpath,
 			boolean indent, Map unknownElements) throws ModelException {
 		try {
 			ByteArrayOutputStream s = new ByteArrayOutputStream();
 			OutputStreamWriter writer = new OutputStreamWriter(s, "UTF8"); //$NON-NLS-1$
 			XMLWriter xmlWriter = new XMLWriter(writer, this, true/*
 																 * print XML
 																 * version
 																 */);
 			xmlWriter.startTag(BuildpathEntry.TAG_BUILDPATH, indent);
 			for (int i = 0; i < buildpath.length; ++i) {
 				((BuildpathEntry) buildpath[i]).elementEncode(xmlWriter,
 						this.project.getFullPath(), indent, true,
 						unknownElements);
 			}
 			xmlWriter.endTag(BuildpathEntry.TAG_BUILDPATH, indent, true/*
 																		 * insert
 																		 * new
 																		 * line
 																		 */);
 			writer.flush();
 			writer.close();
 			return s.toString("UTF8");//$NON-NLS-1$
 		} catch (IOException e) {
 			throw new ModelException(e, IModelStatusConstants.IO_EXCEPTION);
 		}
 	}
 
 	public String encodeBuildpathEntry(IBuildpathEntry buildpathEntry) {
 		try {
 			ByteArrayOutputStream s = new ByteArrayOutputStream();
 			OutputStreamWriter writer = new OutputStreamWriter(s, "UTF8"); //$NON-NLS-1$
 			XMLWriter xmlWriter = new XMLWriter(writer, this, false/*
 																	 * don't
 																	 * print XML
 																	 * version
 																	 */);
 			((BuildpathEntry) buildpathEntry)
 					.elementEncode(xmlWriter, this.project.getFullPath(),
 							true/* indent */, true/*
 												 * insert new line
 												 */, null/*
 														 * not interested in
 														 * unknown elements
 														 */);
 			writer.flush();
 			writer.close();
 			return s.toString("UTF8");//$NON-NLS-1$
 		} catch (IOException e) {
 			return null; // never happens since all is done in memory
 		}
 	}
 
 	/**
 	 * @see IScriptProject
 	 */
 	public boolean hasBuildpathCycle(IBuildpathEntry[] preferredBuildpath) {
 		HashSet cycleParticipants = new HashSet();
 		HashMap preferredBuildpaths = new HashMap(1);
 		preferredBuildpaths.put(this, preferredBuildpath);
 		updateCycleParticipants(new ArrayList(2), cycleParticipants,
 				ResourcesPlugin.getWorkspace().getRoot(), new HashSet(2),
 				preferredBuildpaths);
 		return !cycleParticipants.isEmpty();
 	}
 
 	public boolean hasCycleMarker() {
 		return this.getCycleMarker() != null;
 	}
 
 	public int hashCode() {
 		return this.project.hashCode();
 	}
 
 	/*
 	 * Returns the cycle marker associated with this project or null if none.
 	 */
 	public IMarker getCycleMarker() {
 		try {
 			if (this.project.isAccessible()) {
 				IMarker[] markers = this.project.findMarkers(
 						IModelMarker.BUILDPATH_PROBLEM_MARKER, false,
 						IResource.DEPTH_ZERO);
 				for (int i = 0, length = markers.length; i < length; i++) {
 					IMarker marker = markers[i];
 					String cycleAttr = (String) marker
 							.getAttribute(IModelMarker.CYCLE_DETECTED);
 					if (cycleAttr != null && cycleAttr.equals("true")) { //$NON-NLS-1$
 						return marker;
 					}
 				}
 			}
 		} catch (CoreException e) {
 			// could not get markers: return null
 		}
 		return null;
 	}
 
 	/**
 	 * Update cycle markers for all projects
 	 * 
 	 * @param preferredBuildpaths
 	 *            Map
 	 * @throws ModelException
 	 */
 	public static void updateAllCycleMarkers(Map preferredBuildpaths)
 			throws ModelException {
 		// long start = System.currentTimeMillis();
 		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
 		IProject[] rscProjects = workspaceRoot.getProjects();
 		int length = rscProjects.length;
 		ScriptProject[] projects = new ScriptProject[length];
 		HashSet cycleParticipants = new HashSet();
 		HashSet traversed = new HashSet();
 		// compute cycle participants
 		ArrayList prereqChain = new ArrayList();
 		for (int i = 0; i < length; i++) {
 			ScriptProject project = (projects[i] = (ScriptProject) DLTKCore
 					.create(rscProjects[i]));
 			if (!traversed.contains(project.getPath())) {
 				prereqChain.clear();
 				project.updateCycleParticipants(prereqChain, cycleParticipants,
 						workspaceRoot, traversed, preferredBuildpaths);
 			}
 		}
 		for (int i = 0; i < length; i++) {
 			ScriptProject project = projects[i];
 			if (project != null) {
 				if (cycleParticipants.contains(project.getPath())) {
 					IMarker cycleMarker = project.getCycleMarker();
 					String circularCPOption = project.getOption(
 							DLTKCore.CORE_CIRCULAR_BUILDPATH, true);
 					int circularCPSeverity = DLTKCore.ERROR
 							.equals(circularCPOption) ? IMarker.SEVERITY_ERROR
 							: IMarker.SEVERITY_WARNING;
 					if (cycleMarker != null) {
 						// update existing cycle marker if needed
 						try {
 							int existingSeverity = ((Integer) cycleMarker
 									.getAttribute(IMarker.SEVERITY)).intValue();
 							if (existingSeverity != circularCPSeverity) {
 								cycleMarker.setAttribute(IMarker.SEVERITY,
 										circularCPSeverity);
 							}
 						} catch (CoreException e) {
 							throw new ModelException(e);
 						}
 					} else {
 						// create new marker
 						project
 								.createBuildpathProblemMarker(new ModelStatus(
 										IModelStatusConstants.BUILDPATH_CYCLE,
 										project));
 					}
 				} else {
 					project.flushBuildpathProblemMarkers(true, false);
 				}
 			}
 		}
 	}
 
 	public String getOption(String optionName, boolean inheritCoreOptions) {
 		String propertyName = optionName;
 		if (ModelManager.getModelManager().optionNames.contains(propertyName)) {
 			IEclipsePreferences projectPreferences = getEclipsePreferences();
 			String javaCoreDefault = inheritCoreOptions ? DLTKCore
 					.getOption(propertyName) : null;
 			if (projectPreferences == null)
 				return javaCoreDefault;
 			String value = projectPreferences
 					.get(propertyName, javaCoreDefault);
 			return value == null ? null : value.trim();
 		}
 		return null;
 	}
 
 	public Map getOptions(boolean inheritCoreOptions) {
 		// initialize to the defaults from DLTKCore options pool
 		Map options = inheritCoreOptions ? DLTKCore.getOptions()
 				: new Hashtable(5);
 		// Get project specific options
 		ModelManager.PerProjectInfo perProjectInfo = null;
 		Hashtable projectOptions = null;
 		HashSet optionNames = ModelManager.getModelManager().optionNames;
 		try {
 			perProjectInfo = getPerProjectInfo();
 			projectOptions = perProjectInfo.options;
 			if (projectOptions == null) {
 				// get eclipse preferences
 				IEclipsePreferences projectPreferences = getEclipsePreferences();
 				if (projectPreferences == null)
 					return options; // cannot do better (non-script project)
 				// create project options
 				String[] propertyNames = projectPreferences.keys();
 				projectOptions = new Hashtable(propertyNames.length);
 				for (int i = 0; i < propertyNames.length; i++) {
 					String propertyName = propertyNames[i];
 					String value = projectPreferences.get(propertyName, null);
 					if (value != null && optionNames.contains(propertyName)) {
 						projectOptions.put(propertyName, value.trim());
 					}
 				}
 				// cache project options
 				perProjectInfo.options = projectOptions;
 			}
 		} catch (ModelException jme) {
 			projectOptions = new Hashtable();
 		} catch (BackingStoreException e) {
 			projectOptions = new Hashtable();
 		}
 		// Inherit from DLTKCore options if specified
 		if (inheritCoreOptions) {
 			Iterator propertyNames = projectOptions.keySet().iterator();
 			while (propertyNames.hasNext()) {
 				String propertyName = (String) propertyNames.next();
 				String propertyValue = (String) projectOptions
 						.get(propertyName);
 				if (propertyValue != null && optionNames.contains(propertyName)) {
 					options.put(propertyName, propertyValue.trim());
 				}
 			}
 			return options;
 		}
 		return projectOptions;
 	}
 
 	public void setOption(String optionName, String optionValue) {
 		if (!ModelManager.getModelManager().optionNames.contains(optionName))
 			return; // unrecognized option
 		if (optionValue == null)
 			return; // invalid value
 		IEclipsePreferences projectPreferences = getEclipsePreferences();
 		String defaultValue = DLTKCore.getOption(optionName);
 		if (optionValue.equals(defaultValue)) {
 			// set default value => remove preference
 			projectPreferences.remove(optionName);
 		} else {
 			projectPreferences.put(optionName, optionValue);
 		}
 		// Dump changes
 		try {
 			projectPreferences.flush();
 		} catch (BackingStoreException e) {
 			// problem with pref store - quietly ignore
 		}
 	}
 
 	public void setOptions(Map newOptions) {
 		IEclipsePreferences projectPreferences = getEclipsePreferences();
 		try {
 			if (newOptions == null) {
 				projectPreferences.clear();
 			} else {
 				Iterator keys = newOptions.keySet().iterator();
 				while (keys.hasNext()) {
 					String key = (String) keys.next();
 					if (!ModelManager.getModelManager().optionNames
 							.contains(key))
 						continue; // unrecognized option
 					// no filtering for encoding (custom encoding for project is
 					// allowed)
 					String value = (String) newOptions.get(key);
 					projectPreferences.put(key, value);
 				}
 				// reset to default all options not in new map
 				// @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=26255
 				// @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=49691
 				String[] pNames = projectPreferences.keys();
 				int ln = pNames.length;
 				for (int i = 0; i < ln; i++) {
 					String key = pNames[i];
 					if (!newOptions.containsKey(key)) {
 						projectPreferences.remove(key); // old preferences =>
 						// remove from
 						// preferences table
 					}
 				}
 			}
 			// persist options
 			projectPreferences.flush();
 			// flush cache immediately
 			try {
 				getPerProjectInfo().options = null;
 			} catch (ModelException e) {
 				// do nothing
 			}
 		} catch (BackingStoreException e) {
 			// problem with pref store - quietly ignore
 		}
 	}
 
 	/**
 	 * @see IScriptProject#setRawBuildpath(IBuildpathEntry[],IPath,boolean,IProgressMonitor)
 	 */
 	public void setRawBuildpath(IBuildpathEntry[] entries,
 			boolean canModifyResources, IProgressMonitor monitor)
 			throws ModelException {
 
 		setRawBuildpath(entries, monitor, canModifyResources,
 				getResolvedBuildpath(true/* ignoreUnresolvedEntry */, false/*
 																		 * don't
 																		 * generateMarkerOnError
 																		 */,
 						false/*
 							 * don't returnResolutionInProgress
 							 */), true, // needValidation
 				canModifyResources); // save only if modifying resources is
 		// allowed
 	}
 
 	/**
 	 * @see IScriptProject#setRawClasspath(IClasspathEntry[],IPath,IProgressMonitor)
 	 */
 	public void setRawBuildpath(IBuildpathEntry[] entries,
 			IProgressMonitor monitor) throws ModelException {
 
 		setRawBuildpath(entries, monitor, true, // canChangeResource (as per API
 				// contract)
 				getResolvedBuildpath(true/* ignoreUnresolvedEntry */, false/*
 																		 * don't
 																		 * generateMarkerOnError
 																		 */,
 						false/*
 							 * don't returnResolutionInProgress
 							 */), true, // needValidation
 				true); // need to save
 	}
 
 	public void setRawBuildpath(IBuildpathEntry[] newEntries,
 			IProgressMonitor monitor, boolean canChangeResource,
 			IBuildpathEntry[] oldResolvedPath, boolean needValidation,
 			boolean needSave) throws ModelException {
 
 		ModelManager manager = ModelManager.getModelManager();
 		try {
 			IBuildpathEntry[] newRawPath = newEntries;
 			if (newRawPath == null) { // are we already with the default
 				// buildpath
 				newRawPath = defaultBuildpath();
 			}
 			SetBuildpathOperation op = new SetBuildpathOperation(this,
 					oldResolvedPath, newRawPath, canChangeResource,
 					needValidation, needSave);
 			op.runOperation(monitor);
 
 		} catch (ModelException e) {
 			manager.getDeltaProcessor().flush();
 			throw e;
 		}
 	}
 
 	/**
 	 * Returns the project custom preference pool. Project preferences may
 	 * include custom encoding.
 	 * 
 	 * @return IEclipsePreferences
 	 */
 	public IEclipsePreferences getEclipsePreferences() {
 		// Get cached preferences if exist
 		ModelManager.PerProjectInfo perProjectInfo = ModelManager
 				.getModelManager().getPerProjectInfo(this.project, true);
 		if (perProjectInfo.preferences != null)
 			return perProjectInfo.preferences;
 		// Init project preferences
 		IScopeContext context = new ProjectScope(getProject());
 		final IEclipsePreferences eclipsePreferences = context
 				.getNode(DLTKCore.PLUGIN_ID);
 		perProjectInfo.preferences = eclipsePreferences;
 		// Listen to node removal from parent in order to reset cache (see bug
 		// 68993)
 		IEclipsePreferences.INodeChangeListener nodeListener = new IEclipsePreferences.INodeChangeListener() {
 			public void added(IEclipsePreferences.NodeChangeEvent event) {
 				// do nothing
 			}
 
 			public void removed(IEclipsePreferences.NodeChangeEvent event) {
 				if (event.getChild() == eclipsePreferences) {
 					ModelManager.getModelManager().resetProjectPreferences(
 							ScriptProject.this);
 				}
 			}
 		};
 		((IEclipsePreferences) eclipsePreferences.parent())
 				.addNodeChangeListener(nodeListener);
 		// Listen to preference changes
 		IEclipsePreferences.IPreferenceChangeListener preferenceListener = new IEclipsePreferences.IPreferenceChangeListener() {
 			public void preferenceChange(
 					IEclipsePreferences.PreferenceChangeEvent event) {
 				ModelManager.getModelManager().resetProjectOptions(
 						ScriptProject.this);
 			}
 		};
 		eclipsePreferences.addPreferenceChangeListener(preferenceListener);
 		return eclipsePreferences;
 	}
 
 	/*
 	 * Update .buildpath format markers.
 	 */
 	public void updateBuildpathMarkers(Map preferredBuildpaths) {
 
 		this.flushBuildpathProblemMarkers(false/* cycle */, true/* format */);
 		this.flushBuildpathProblemMarkers(false/* cycle */, false/* format */);
 
 		IBuildpathEntry[] buildpath = this.readBuildpathFile(true/* marker */,
 				false/* log */);
 		// remember invalid path so as to avoid reupdating it again later on
 		if (preferredBuildpaths != null) {
 			preferredBuildpaths.put(this, buildpath == null ? INVALID_BUILDPATH
 					: buildpath);
 		}
 
 		// force buildpath marker refresh
 		if (buildpath != null) {
 			for (int i = 0; i < buildpath.length; i++) {
 				IModelStatus status = BuildpathEntry.validateBuildpathEntry(
 						this, buildpath[i], true /* recurse in container */);
 				if (!status.isOK()) {
 					if (status.getCode() == IModelStatusConstants.INVALID_BUILDPATH
 							&& ((BuildpathEntry) buildpath[i]).isOptional())
 						continue; // ignore this entry
 					this.createBuildpathProblemMarker(status);
 				}
 			}
 			IModelStatus status = BuildpathEntry.validateBuildpath(this,
 					buildpath);
 			if (!status.isOK())
 				this.createBuildpathProblemMarker(status);
 		}
 	}
 
 	/**
 	 * If a cycle is detected, then cycleParticipants contains all the paths of
 	 * projects involved in this cycle (directly and indirectly), no cycle if
 	 * the set is empty (and started empty)
 	 * 
 	 * @param prereqChain
 	 *            ArrayList
 	 * @param cycleParticipants
 	 *            HashSet
 	 * @param workspaceRoot
 	 *            IWorkspaceRoot
 	 * @param traversed
 	 *            HashSet
 	 * @param preferredBuildpaths
 	 *            Map
 	 */
 	public void updateCycleParticipants(ArrayList prereqChain,
 			HashSet cycleParticipants, IWorkspaceRoot workspaceRoot,
 			HashSet traversed, Map preferredBuildpaths) {
 		IPath path = this.getPath();
 		prereqChain.add(path);
 		traversed.add(path);
 		try {
 			IBuildpathEntry[] buildpath = null;
 			if (preferredBuildpaths != null)
 				buildpath = (IBuildpathEntry[]) preferredBuildpaths.get(this);
 			if (buildpath == null)
 				buildpath = getResolvedBuildpath();
 			for (int i = 0, length = buildpath.length; i < length; i++) {
 				IBuildpathEntry entry = buildpath[i];
 				if (entry.getEntryKind() == IBuildpathEntry.BPE_PROJECT) {
 					IPath prereqProjectPath = entry.getPath();
 					int index = cycleParticipants.contains(prereqProjectPath) ? 0
 							: prereqChain.indexOf(prereqProjectPath);
 					if (index >= 0) { // refer to cycle, or in cycle itself
 						for (int size = prereqChain.size(); index < size; index++) {
 							cycleParticipants.add(prereqChain.get(index));
 						}
 					} else {
 						if (!traversed.contains(prereqProjectPath)) {
 							IResource member = workspaceRoot
 									.findMember(prereqProjectPath);
 							if (member != null
 									&& member.getType() == IResource.PROJECT) {
 								ScriptProject scriptProject = (ScriptProject) DLTKCore
 										.create((IProject) member);
 								scriptProject.updateCycleParticipants(
 										prereqChain, cycleParticipants,
 										workspaceRoot, traversed,
 										preferredBuildpaths);
 							}
 						}
 					}
 				}
 			}
 		} catch (ModelException e) {
 			// project doesn't exist: ignore
 		}
 		prereqChain.remove(path);
 	}
 
 	/**
 	 * Returns true if this handle represents the same project as the given
 	 * handle. Two handles represent the same project if they are identical or
 	 * if they represent a project with the same underlying resource and
 	 * occurrence counts.
 	 * 
 	 * @see ModelElement#equals(Object)
 	 */
 	public boolean equals(Object o) {
 		if (this == o)
 			return true;
 		if (!(o instanceof ScriptProject))
 			return false;
 		ScriptProject other = (ScriptProject) o;
 		return this.project.equals(other.getProject());
 	}
 
 	public boolean exists() {
 		return project.exists() && DLTKLanguageManager.hasScriptNature(project);
 	}
 
 	public String getElementName() {
 		return project.getName();
 	}
 
 	/*
 	 * Returns whether the given resource is accessible through the children or
 	 * the non-script resources of this project. Returns true if the resource is
 	 * not in the project. Assumes that the resource is a folder or a file.
 	 */
 	public boolean contains(IResource resource) {
 		IBuildpathEntry[] buildpath;
 		try {
 			buildpath = getResolvedBuildpath();
 		} catch (ModelException e) {
 			return false;
 		}
 		IPath fullPath = resource.getFullPath();
 		IBuildpathEntry innerMostEntry = null;
 		for (int j = 0, cpLength = buildpath.length; j < cpLength; j++) {
 			IBuildpathEntry entry = buildpath[j];
 			IPath entryPath = entry.getPath();
 			if ((innerMostEntry == null || innerMostEntry.getPath().isPrefixOf(
 					entryPath))
 					&& entryPath.isPrefixOf(fullPath)) {
 				innerMostEntry = entry;
 			}
 		}
 		if (innerMostEntry != null) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean isValid() {
 		IDLTKLanguageToolkit toolkit = DLTKLanguageManager
 				.getLanguageToolkit(this);
 		return toolkit != null;
 	}
 
 	public void printNode(CorePrinter output) {
 		output.formatPrint("ScriptProject:" + getElementName()); //$NON-NLS-1$
 		output.indent();
 		try {
 			IModelElement modelElements[] = this.getChildren();
 			for (int i = 0; i < modelElements.length; ++i) {
 				IModelElement element = modelElements[i];
 				if (element instanceof ModelElement) {
 					((ModelElement) element).printNode(output);
 				} else {
 					output.print("Unknown element:" + element); //$NON-NLS-1$
 				}
 			}
 		} catch (ModelException ex) {
 			output.formatPrint(ex.getLocalizedMessage());
 		}
 		output.dedent();
 	}
 
 	/*
 	 * Reads the buildpath file entries of this project's .buildpath file. This
 	 * includes the output entry. As a side effect, unknown elements are stored
 	 * in the given map (if not null) Throws exceptions if the file cannot be
 	 * accessed or is malformed.
 	 */
 	public IBuildpathEntry[] readFileEntriesWithException(Map unknownElements)
 			throws CoreException, IOException, AssertionFailedException {
 		String xmlBuildpath;
 		IFile rscFile = this.project.getFile(ScriptProject.BUILDPATH_FILENAME);
 		if (rscFile.exists()) {
 			byte[] bytes = Util.getResourceContentsAsByteArray(rscFile);
 			try {
 				xmlBuildpath = new String(bytes, Util.UTF_8); // .buildpath
 				// always
 				// encoded with
 				// UTF-8
 			} catch (UnsupportedEncodingException e) {
 				Util.log(e, "Could not read .buildpath with UTF-8 encoding"); //$NON-NLS-1$
 				// fallback to default
 				xmlBuildpath = new String(bytes);
 			}
 		} else {
 			// when a project is imported, we get a first delta for the addition
 			// of the .project, but the .buildpath is not accessible
 			// so default to using java.io.File
 			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=96258
 			URI location = rscFile.getLocationURI();
 			if (location == null)
 				throw new IOException(
 						"Cannot obtain a location URI for " + rscFile); //$NON-NLS-1$
 			File file = Util.toLocalFile(location, null/*
 														 * no progress monitor
 														 * available
 														 */);
 			if (file == null)
 				throw new IOException("Unable to fetch file from " + location); //$NON-NLS-1$
 			byte[] bytes;
 			try {
 				bytes = org.eclipse.dltk.compiler.util.Util
 						.getFileByteContent(file);
 			} catch (IOException e) {
 				if (!file.exists())
 					return defaultBuildpath();
 				throw e;
 			}
 			try {
 				xmlBuildpath = new String(bytes, Util.UTF_8); // .buildpath
 				// always
 				// encoded with
 				// UTF-8
 			} catch (UnsupportedEncodingException e) {
 				Util.log(e, "Could not read .buildpath with UTF-8 encoding"); //$NON-NLS-1$
 				// fallback to default
 				xmlBuildpath = new String(bytes);
 			}
 		}
 		return decodeBuildpath(xmlBuildpath, unknownElements);
 	}
 
 	public static boolean areBuildpathsEqual(IBuildpathEntry[] firstBuildpath,
 			IBuildpathEntry[] secondBuildpath) {
 		if (secondBuildpath == null || secondBuildpath.length == 0)
 			return false;
 		int length = firstBuildpath.length;
 		if (length != secondBuildpath.length)
 			return false;
 		for (int i = 0; i < length; i++) {
 			if (!firstBuildpath[i].equals(secondBuildpath[i]))
 				return false;
 		}
 		return true;
 	}
 
 	/*
 	 * Detect cycles in the buildpath of the workspace's projects and create
 	 * markers if necessary. @param preferredBuildpaths Map @throws
 	 * ModelException
 	 */
 	public static void validateCycles(Map preferredBuildpaths)
 			throws ModelException {
 		// long start = System.currentTimeMillis();
 		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
 		IProject[] rscProjects = workspaceRoot.getProjects();
 		int length = rscProjects.length;
 		ScriptProject[] projects = new ScriptProject[length];
 		HashSet cycleParticipants = new HashSet();
 		HashSet traversed = new HashSet();
 		// compute cycle participants
 		ArrayList prereqChain = new ArrayList();
 		for (int i = 0; i < length; i++) {
 			if (DLTKLanguageManager.hasScriptNature(rscProjects[i])) {
 				ScriptProject project = (projects[i] = (ScriptProject) DLTKCore
 						.create(rscProjects[i]));
 				if (!traversed.contains(project.getPath())) {
 					prereqChain.clear();
 					project.updateCycleParticipants(prereqChain,
 							cycleParticipants, workspaceRoot, traversed,
 							preferredBuildpaths);
 				}
 			}
 		}
 		// System.out.println("updateAllCycleMarkers: " +
 		// (System.currentTimeMillis() - start) + " ms");
 		for (int i = 0; i < length; i++) {
 			ScriptProject project = projects[i];
 			if (project != null) {
 				if (cycleParticipants.contains(project.getPath())) {
 					IMarker cycleMarker = project.getCycleMarker();
 					String circularCPOption = project.getOption(
 							DLTKCore.CORE_CIRCULAR_BUILDPATH, true);
 					int circularCPSeverity = DLTKCore.ERROR
 							.equals(circularCPOption) ? IMarker.SEVERITY_ERROR
 							: IMarker.SEVERITY_WARNING;
 					if (cycleMarker != null) {
 						// update existing cycle marker if needed
 						try {
 							int existingSeverity = ((Integer) cycleMarker
 									.getAttribute(IMarker.SEVERITY)).intValue();
 							if (existingSeverity != circularCPSeverity) {
 								cycleMarker.setAttribute(IMarker.SEVERITY,
 										circularCPSeverity);
 							}
 						} catch (CoreException e) {
 							throw new ModelException(e);
 						}
 					} else {
 						// create new marker
 						project
 								.createBuildpathProblemMarker(new ModelStatus(
 										IModelStatusConstants.BUILDPATH_CYCLE,
 										project));
 					}
 				} else {
 					project.flushBuildpathProblemMarkers(true, false);
 				}
 			}
 		}
 	}
 
 	// Search operations
 	/**
 	 * @see IScriptProject
 	 */
 	public IModelElement findElement(IPath path) throws ModelException {
 		return findElement(path, DefaultWorkingCopyOwner.PRIMARY);
 	}
 
 	/**
 	 * @see IScriptProject
 	 */
 	public IModelElement findElement(IPath path, WorkingCopyOwner owner)
 			throws ModelException {
 		if (path == null) {
 			// throw new ModelException(
 			// new ModelStatus(IModelStatusConstants.INVALID_PATH, path));
 			// }
 			return null;
 		}
 		try {
 
 			String extension = path.getFileExtension();
 			if (extension == null) {
 				String packageName = path.toString();// .replace(IPath.SEPARATOR,
 				// '.');
 
 				NameLookup lookup = newNameLookup((WorkingCopyOwner) null/*
 																		 * no
 																		 * need
 																		 * to
 																		 * look
 																		 * at
 																		 * working
 																		 * copies
 																		 * for
 																		 * pkgs
 																		 */);
 				IScriptFolder[] pkgFragments = lookup.findScriptFolders(
 						packageName, false);
 				if (pkgFragments == null) {
 					return null;
 
 				} else {
 					// try to return one that is a child of this project
 					for (int i = 0, length = pkgFragments.length; i < length; i++) {
 
 						IScriptFolder pkgFragment = pkgFragments[i];
 						if (this.equals(pkgFragment.getParent().getParent())) {
 							return pkgFragment;
 						}
 					}
 					// default to the first one
 					return pkgFragments[0];
 				}
 			} else if (Util.isValidSourceModule(this, path)) {
 				IPath packagePath = path.removeLastSegments(1);
 				String packageName = packagePath.toString();// .replace(IPath.
 				// SEPARATOR,
 				// '.');
 				String typeName = path.lastSegment();
 				typeName = typeName.substring(0, typeName.length()
 						- extension.length() - 1);
 				String qualifiedName = null;
 				if (packageName.length() > 0) {
 					qualifiedName = packageName
 							+ IScriptFolder.PACKAGE_DELIMETER_STR + typeName;
 				} else {
 					qualifiedName = typeName;
 				}
 
 				// lookup type
 				NameLookup lookup = newNameLookup(owner);
 				NameLookup.Answer answer = lookup.findType(qualifiedName,
 						false, NameLookup.ACCEPT_ALL, true/*
 														 * consider secondary
 														 * types
 														 */, false/*
 																 * do NOT wait
 																 * for indexes
 																 */, false/*
 																		 * don't
 																		 * check
 																		 * restrictions
 																		 */,
 						null);
 
 				if (answer != null) {
 					return answer.type.getParent();
 				} else {
 					return null;
 				}
 			} else {
 				// unsupported extension
 				return null;
 			}
 		} catch (ModelException e) {
 			if (e.getStatus().getCode() == IModelStatusConstants.ELEMENT_DOES_NOT_EXIST) {
 				return null;
 			} else {
 				throw e;
 			}
 		}
 	}
 
 	/**
 	 * @see IScriptProject
 	 */
 	public IScriptFolder findScriptFolder(IPath path) throws ModelException {
 		return findScriptFolder0(ScriptProject.canonicalizedPath(path));
 	}
 
 	/*
 	 * non path canonicalizing version
 	 */
 	private IScriptFolder findScriptFolder0(IPath path) throws ModelException {
 		/* no need to look at working copies for pkgs */
 		NameLookup lookup = newNameLookup((WorkingCopyOwner) null);
 		return lookup.findScriptFolder(path);
 	}
 
 	/**
 	 * @see IScriptProject
 	 */
 	public IProjectFragment findProjectFragment(IPath path)
 			throws ModelException {
 		return findProjectFragment0(ScriptProject.canonicalizedPath(path));
 	}
 
 	/*
 	 * no path canonicalization
 	 */
 	public IProjectFragment findProjectFragment0(IPath path)
 			throws ModelException {
 		if (!EnvironmentPathUtils.isFull(path)
 				&& (path.segmentCount() == 0 || !path.segment(0).startsWith(
 						IBuildpathEntry.BUILDPATH_SPECIAL))) {
 			path = EnvironmentPathUtils.getFullPath(EnvironmentManager
 					.getEnvironment(this), path);
 		}
 		IProjectFragment[] allRoots = this.getAllProjectFragments();
 		if (!path.isAbsolute()) {
 			if (path.segmentCount() == 0
 					|| !path.segment(0).startsWith(
 							IBuildpathEntry.BUILDPATH_SPECIAL)) {
 				throw new IllegalArgumentException(Messages.path_mustBeAbsolute);
 			}
 		}
 		for (int i = 0; i < allRoots.length; i++) {
 			IProjectFragment buildpathRoot = allRoots[i];
 			if (buildpathRoot.getPath().equals(path)) {
 				return buildpathRoot;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @see IScriptProject
 	 */
 	public IProjectFragment[] findProjectFragments(IBuildpathEntry entry) {
 		try {
 			IBuildpathEntry[] buildpath = this.getRawBuildpath();
 			for (int i = 0, length = buildpath.length; i < length; i++) {
 				if (buildpath[i].equals(entry)) { // entry may need to be
 					// resolved
 					return computeProjectFragments(
 							getResolvedBuildpath(
 									new IBuildpathEntry[] { entry }, true,
 									false, null/* no reverse map */), false, // don
 							// 't
 							// retrieve
 							// exported
 							// roots
 							null); /* no reverse map */
 				}
 			}
 		} catch (ModelException e) {
 			// project doesn't exist: return an empty array
 		}
 		return new IProjectFragment[] {};
 	}
 
 	/**
 	 * @see IScriptProject#findType(String)
 	 */
 	public IType findType(String fullyQualifiedName) throws ModelException {
 		return findType(fullyQualifiedName, DefaultWorkingCopyOwner.PRIMARY);
 	}
 
 	/**
 	 * @see IScriptProject#findType(String, IProgressMonitor)
 	 */
 	public IType findType(String fullyQualifiedName,
 			IProgressMonitor progressMonitor) throws ModelException {
 		return findType(fullyQualifiedName, DefaultWorkingCopyOwner.PRIMARY,
 				progressMonitor);
 	}
 
 	/*
 	 * Internal findType with instanciated name lookup
 	 */
 	IType findType(String fullyQualifiedName, Object lookup,
 			boolean considerSecondaryTypes, IProgressMonitor progressMonitor)
 			throws ModelException {
 		if (DLTKCore.DEBUG) {
 			System.err.println("Search Need to be implemented"); //$NON-NLS-1$
 		}
 		return null;
 	}
 
 	/**
 	 * @see IScriptProject#findType(String, String)
 	 */
 	public IType findType(String packageName, String typeQualifiedName)
 			throws ModelException {
 		return findType(packageName, typeQualifiedName,
 				DefaultWorkingCopyOwner.PRIMARY);
 	}
 
 	/**
 	 * @see IScriptProject#findType(String, String, IProgressMonitor)
 	 */
 	public IType findType(String packageName, String typeQualifiedName,
 			IProgressMonitor progressMonitor) throws ModelException {
 		return findType(packageName, typeQualifiedName,
 				DefaultWorkingCopyOwner.PRIMARY, progressMonitor);
 	}
 
 	/*
 	 * Internal findType with instanciated name lookup
 	 */
 	IType findType(String packageName, String typeQualifiedName, Object lookup,
 			boolean considerSecondaryTypes, IProgressMonitor progressMonitor)
 			throws ModelException {
 		if (DLTKCore.DEBUG) {
 			System.err.println("Search Need to be implemented"); //$NON-NLS-1$
 		}
 		return null;
 	}
 
 	/**
 	 * @see IScriptProject#findType(String, String, WorkingCopyOwner)
 	 */
 	public IType findType(String packageName, String typeQualifiedName,
 			WorkingCopyOwner owner) throws ModelException {
 		if (DLTKCore.DEBUG) {
 			System.err.println("Search Need to be implemented"); //$NON-NLS-1$
 		}
 		return null;
 	}
 
 	/**
 	 * @see IScriptProject#findType(String, String, WorkingCopyOwner,
 	 *      IProgressMonitor)
 	 */
 	public IType findType(String packageName, String typeQualifiedName,
 			WorkingCopyOwner owner, IProgressMonitor progressMonitor)
 			throws ModelException {
 		if (DLTKCore.DEBUG) {
 			System.err.println("Search Need to be implemented"); //$NON-NLS-1$
 		}
 		return null;
 	}
 
 	public IType findType(String fullyQualifiedName, WorkingCopyOwner owner)
 			throws ModelException {
 		if (DLTKCore.DEBUG) {
 			System.err.println("Search Need to be implemented"); //$NON-NLS-1$
 		}
 		return null;
 	}
 
 	public IType findType(String fullyQualifiedName, WorkingCopyOwner owner,
 			IProgressMonitor progressMonitor) throws ModelException {
 		if (DLTKCore.DEBUG) {
 			System.err.println("Search Need to be implemented"); //$NON-NLS-1$
 		}
 		return null;
 	}
 
 	public IBuildpathEntry[] readRawBuildpath() {
 		// Read buildpath file without creating markers nor logging problems
 		IBuildpathEntry[] buildpath = readFileEntries(null/*
 														 * not interested in
 														 * unknown elements
 														 */);
 		if (buildpath == ScriptProject.INVALID_BUILDPATH)
 			return defaultBuildpath();
 		return buildpath;
 	}
 
 	/*
 	 * Reads the buildpath file entries of this project's .buildpath file. This
 	 * includes the output entry. As a side effect, unknown elements are stored
 	 * in the given map (if not null)
 	 */
 	private IBuildpathEntry[] readFileEntries(Map unkwownElements) {
 		try {
 			return readFileEntriesWithException(unkwownElements);
 		} catch (CoreException e) {
 			Util
 					.log(
 							e,
 							"Exception while reading " + getPath().append(ScriptProject.BUILDPATH_FILENAME)); //$NON-NLS-1$
 			return ScriptProject.INVALID_BUILDPATH;
 		} catch (IOException e) {
 			Util
 					.log(
 							e,
 							"Exception while reading " + getPath().append(ScriptProject.BUILDPATH_FILENAME)); //$NON-NLS-1$
 			return ScriptProject.INVALID_BUILDPATH;
 		} catch (AssertionFailedException e) {
 			Util
 					.log(
 							e,
 							"Exception while reading " + getPath().append(ScriptProject.BUILDPATH_FILENAME)); //$NON-NLS-1$
 			return ScriptProject.INVALID_BUILDPATH;
 		}
 	}
 
 	/**
 	 * Returns an array of non-java resources contained in the receiver.
 	 */
 	public Object[] getForeignResources() throws ModelException {
 		return ((ProjectElementInfo) getElementInfo())
 				.getForeignResources(this);
 	}
 
 	public boolean isOnBuildpath(IModelElement element) {
 		IBuildpathEntry[] rawBuildpath;
 		try {
 			rawBuildpath = getRawBuildpath();
 		} catch (ModelException e) {
 			return false; // not a script project
 		}
 		int elementType = element.getElementType();
 		boolean isFolderPath = false;
 		// boolean isSource = false;
 		boolean isProjectFragment = false;
 		switch (elementType) {
 		case IModelElement.SCRIPT_MODEL:
 			return false;
 		case IModelElement.SCRIPT_PROJECT:
 			break;
 		case IModelElement.PROJECT_FRAGMENT:
 			isProjectFragment = true;
 			break;
 		case IModelElement.SCRIPT_FOLDER:
 			isFolderPath = !((IProjectFragment) element.getParent())
 					.isArchive();
 			break;
 		case IModelElement.SOURCE_MODULE:
 			// isSource = true;
 			break;
 		default:
 			// isSource = element.getAncestor(IModelElement.SOURCE_MODULE) !=
 			// null;
 			break;
 		}
 		IPath elementPath = element.getPath();
 		// first look at unresolved entries
 		int length = rawBuildpath.length;
 		for (int i = 0; i < length; i++) {
 			IBuildpathEntry entry = rawBuildpath[i];
 			switch (entry.getEntryKind()) {
 			case IBuildpathEntry.BPE_LIBRARY:
 			case IBuildpathEntry.BPE_PROJECT:
 			case IBuildpathEntry.BPE_SOURCE:
 				if (isOnBuildpathEntry(elementPath, isFolderPath,
 						isProjectFragment, entry))
 					return true;
 				break;
 			}
 		}
 		// no need to go further for compilation units and elements inside a
 		// compilation unit
 		// it can only be in a source folder, thus on the raw buildpath
 		// if (isSource)
 		// return false;
 		// then look at resolved entries
 		for (int i = 0; i < length; i++) {
 			IBuildpathEntry rawEntry = rawBuildpath[i];
 			switch (rawEntry.getEntryKind()) {
 			case IBuildpathEntry.BPE_CONTAINER:
 				IBuildpathContainer container;
 				try {
 					container = DLTKCore.getBuildpathContainer(rawEntry
 							.getPath(), this);
 				} catch (ModelException e) {
 					break;
 				}
 				if (container == null)
 					break;
 				IBuildpathEntry[] containerEntries = container
 						.getBuildpathEntries(this);
 				if (containerEntries == null)
 					break;
 				// container was bound
 				for (int j = 0, containerLength = containerEntries.length; j < containerLength; j++) {
 					IBuildpathEntry resolvedEntry = containerEntries[j];
 					if (isOnBuildpathEntry(elementPath, isFolderPath,
 							isProjectFragment, resolvedEntry))
 						return true;
 				}
 				break;
 			}
 		}
 		// Check for all fragments for custom elements
 		try {
 			IProjectFragment[] allProjectFragments = getAllProjectFragments();
 			for (IProjectFragment fragment : allProjectFragments) {
 				if (fragment.isExternal()) {
 					IPath path = fragment.getPath();
 					if (path.isPrefixOf(elementPath)) {
 						return true;
 					}
 				}
 			}
 		} catch (ModelException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 		return false;
 	}
 
 	/*
 	 * @see IScriptProject
 	 */
 	public boolean isOnBuildpath(IResource resource) {
 		IPath exactPath = resource.getFullPath();
 		IPath path = exactPath;
 		// ensure that folders are only excluded if all of their children are
 		// excluded
 		int resourceType = resource.getType();
 		boolean isFolderPath = resourceType == IResource.FOLDER
 				|| resourceType == IResource.PROJECT;
 		IBuildpathEntry[] buildpath;
 		try {
 			buildpath = this.getResolvedBuildpath();
 		} catch (ModelException e) {
 			return false; // not a script project
 		}
 		for (int i = 0; i < buildpath.length; i++) {
 			IBuildpathEntry entry = buildpath[i];
 			IPath entryPath = entry.getPath();
 			if (entryPath.equals(exactPath)) { // package fragment roots must
 				// match exactly entry pathes
 				// (no exclusion there)
 				return true;
 			}
 			if (entryPath.isPrefixOf(path)
 					&& !Util.isExcluded(path, ((BuildpathEntry) entry)
 							.fullInclusionPatternChars(),
 							((BuildpathEntry) entry)
 									.fullExclusionPatternChars(), isFolderPath)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean isOnBuildpathEntry(IPath elementPath, boolean isFolderPath,
 			boolean isProjectFragment, IBuildpathEntry entry) {
 		IPath entryPath = entry.getPath();
 		if (isProjectFragment) {
 			// package fragment roots must match exactly entry pathes (no
 			// exclusion there)
 			if (entryPath.equals(elementPath))
 				return true;
 		} else {
 			if (entryPath.isPrefixOf(elementPath)
 					&& !Util.isExcluded(elementPath, ((BuildpathEntry) entry)
 							.fullInclusionPatternChars(),
 							((BuildpathEntry) entry)
 									.fullExclusionPatternChars(), isFolderPath))
 				return true;
 		}
 		return false;
 	}
 
 	public IModelElement getHandleFromMemento(String token,
 			MementoTokenizer memento, WorkingCopyOwner owner) {
 		switch (token.charAt(0)) {
 		case JEM_PROJECTFRAGMENT:
 			String rootPath = IProjectFragment.DEFAULT_PACKAGE_ROOT;
 			token = null;
 			while (memento.hasMoreTokens()) {
 				token = memento.nextToken();
 				char firstChar = token.charAt(0);
 				if (firstChar != JEM_SCRIPTFOLDER && firstChar != JEM_COUNT
 						&& firstChar != JEM_USER_ELEMENT) {
 					rootPath += token;
 				} else {
 					break;
 				}
 			}
 			ModelElement root = null;
 			if (rootPath.indexOf(JEM_SKIP_DELIMETER) != -1) {
 				root = getExternalScriptFolderOrContainerFolder(rootPath);
 			} else {
 				root = (ModelElement) getProjectFragment(new Path(rootPath));
 			}
 			if (token != null && token.charAt(0) == JEM_SCRIPTFOLDER
 					&& root != null) {
 				return root.getHandleFromMemento(token, memento, owner);
 			} else if (token != null && token.charAt(0) == JEM_USER_ELEMENT
 					&& root != null) {
 				return root.getHandleFromMemento(token, memento, owner);
 			} else if (root != null) {
 				return root.getHandleFromMemento(memento, owner);
 			}
 			return null;
 		case JEM_USER_ELEMENT:
 			// We need to construct project children and return appropriate
 			// element from it.
 			token = null;
 			String name = "";
 			while (memento.hasMoreTokens()) {
 				token = memento.nextToken();
 				char firstChar = token.charAt(0);
 				if (ModelElement.JEM_USER_ELEMENT_ENDING.indexOf(firstChar) == -1
 						&& firstChar != JEM_COUNT) {
 					name += token;
 				} else {
 					break;
 				}
 			}
 			try {
 				IModelElement[] children = getProjectFragments();
 				for (int i = 0; i < children.length; i++) {
 					if (name.equals(children[i].getElementName())
 							&& children[i] instanceof IModelElementMemento) {
 						IModelElementMemento childMemento = (IModelElementMemento) children[i];
 						return childMemento.getHandleFromMemento(token,
 								memento, owner);
 					}
 				}
 			} catch (ModelException e) {
 				DLTKCore.error("Incorrect handle resolving", e);
 			}
 		}
 		return null;
 	}
 
 	private ModelElement getExternalScriptFolderOrContainerFolder(
 			String rootPath) {
 		IProjectFragment[] allRoots;
 		try {
 			allRoots = this.getProjectFragments();
 		} catch (ModelException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 		for (int i = 0; i < allRoots.length; i++) {
 			IProjectFragment buildpathRoot = allRoots[i];
 			if (buildpathRoot.getElementName().equals(rootPath)) {
 				return (ModelElement) buildpathRoot;
 			}
 		}
 		return null;
 	}
 
 	protected char getHandleMementoDelimiter() {
 		return JEM_SCRIPTPROJECT;
 	}
 
 	public IResource getUnderlyingResource() throws ModelException {
 		if (!exists())
 			throw newNotPresentException();
 		return this.project;
 	}
 
 	/*
 	 * Returns a new search name environment for this project. This name
 	 * environment first looks in the given working copies.
 	 */
 	public ISearchableEnvironment newSearchableNameEnvironment(
 			ISourceModule[] workingCopies) throws ModelException {
 		return new SearchableEnvironment(this, workingCopies);
 	}
 
 	/*
 	 * Returns a new search name environment for this project. This name
 	 * environment first looks in the working copies of the given owner.
 	 */
 	public SearchableEnvironment newSearchableNameEnvironment(
 			WorkingCopyOwner owner) throws ModelException {
 		return new SearchableEnvironment(this, owner);
 	}
 
 	/**
 	 * @see IJavaProject
 	 */
 	public ITypeHierarchy newTypeHierarchy(IRegion region,
 			IProgressMonitor monitor) throws ModelException {
 
 		return newTypeHierarchy(region, DefaultWorkingCopyOwner.PRIMARY,
 				monitor);
 	}
 
 	/**
 	 * @see IJavaProject
 	 */
 	public ITypeHierarchy newTypeHierarchy(IRegion region,
 			WorkingCopyOwner owner, IProgressMonitor monitor)
 			throws ModelException {
 
 		if (region == null) {
 			throw new IllegalArgumentException(Messages.hierarchy_nullRegion);
 		}
 		ISourceModule[] workingCopies = ModelManager.getModelManager()
 				.getWorkingCopies(owner, true/* add primary working copies */);
 		CreateTypeHierarchyOperation op = new CreateTypeHierarchyOperation(
 				region, workingCopies, null, true);
 		op.runOperation(monitor);
 		return op.getResult();
 	}
 
 	/**
 	 * @see IJavaProject
 	 */
 	public ITypeHierarchy newTypeHierarchy(IType type, IRegion region,
 			IProgressMonitor monitor) throws ModelException {
 
 		return newTypeHierarchy(type, region, DefaultWorkingCopyOwner.PRIMARY,
 				monitor);
 	}
 
 	/**
 	 * @see IJavaProject
 	 */
 	public ITypeHierarchy newTypeHierarchy(IType type, IRegion region,
 			WorkingCopyOwner owner, IProgressMonitor monitor)
 			throws ModelException {
 
 		if (type == null) {
 			throw new IllegalArgumentException(Messages.hierarchy_nullFocusType);
 		}
 		if (region == null) {
 			throw new IllegalArgumentException(Messages.hierarchy_nullRegion);
 		}
 		ISourceModule[] workingCopies = ModelManager.getModelManager()
 				.getWorkingCopies(owner, true/* add primary working copies */);
 		CreateTypeHierarchyOperation op = new CreateTypeHierarchyOperation(
 				region, workingCopies, type, true/* compute subtypes */);
 		op.runOperation(monitor);
 		return op.getResult();
 	}
 
 	/**
 	 * Returns the buildpath entry that refers to the given path or
 	 * <code>null</code> if there is no reference to the path.
 	 * 
 	 * @param path
 	 *            IPath
 	 * @return IBuildpathEntry
 	 * @throws ModelException
 	 */
 	public IBuildpathEntry getBuildpathEntryFor(IPath path)
 			throws ModelException {
 		IBuildpathEntry[] entries = getExpandedBuildpath(true);
 		for (int i = 0; i < entries.length; i++) {
 			if (entries[i].getPath().equals(path)) {
 				return entries[i];
 			}
 		}
 		return null;
 	}
 
 	/*
 	 * Returns a new name lookup. This name lookup first looks in the given
 	 * working copies.
 	 */
 	public NameLookup newNameLookup(ISourceModule[] workingCopies)
 			throws ModelException {
 		return getProjectElementInfo().newNameLookup(this, workingCopies);
 	}
 
 	/*
 	 * Returns a new name lookup. This name lookup first looks in the working
 	 * copies of the given owner.
 	 */
 	public NameLookup newNameLookup(WorkingCopyOwner owner)
 			throws ModelException {
 		ModelManager manager = ModelManager.getModelManager();
 		ISourceModule[] workingCopies = owner == null ? null : manager
 				.getWorkingCopies(owner, true/*
 											 * add primary WCs
 											 */);
 		return newNameLookup(workingCopies);
 	}
 
 	/*
 	 * Force the project to reload its <code>.buildpath</code> file from disk
 	 * and update the buildpath accordingly. Usually, a change to the
 	 * <code>.buildpath</code> file is automatically noticed and reconciled at
 	 * the next resource change notification event. If required to consider such
 	 * a change prior to the next automatic refresh, then this functionnality
 	 * should be used to trigger a refresh. In particular, if a change to the
 	 * file is performed, during an operation where this change needs to be
 	 * reflected before the operation ends, then an explicit refresh is
 	 * necessary. Note that buildpath markers are NOT created.
 	 * 
 	 * @param monitor a progress monitor for reporting operation progress
 	 * 
 	 * @exception ModelException if the buildpath could not be updated. Reasons
 	 * include: <ul> <li> This Script element does not exist
 	 * (ELEMENT_DOES_NOT_EXIST)</li> <li> Two or more entries specify source
 	 * roots with the same or overlapping paths (NAME_COLLISION) <li> A entry of
 	 * kind <code>CPE_PROJECT</code> refers to this project (INVALID_PATH)
 	 * <li>This Script element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
 	 * <li>The output location path refers to a location not contained in this
 	 * project (<code>PATH_OUTSIDE_PROJECT</code>) <li>The output location path
 	 * is not an absolute path (<code>RELATIVE_PATH</code>) <li>The output
 	 * location path is nested inside a package fragment root of this project
 	 * (<code>INVALID_PATH</code>) <li> The buildpath is being modified during
 	 * resource change event notification (CORE_EXCEPTION) </ul>
 	 */
 	protected void forceBuildpathReload(IProgressMonitor monitor)
 			throws ModelException {
 
 		if (monitor != null && monitor.isCanceled())
 			return;
 
 		// check if any actual difference
 		boolean wasSuccessful = false; // flag recording if .buildpath file
 		// change got reflected
 		try {
 			// force to (re)read the property file
 			IBuildpathEntry[] fileEntries = readBuildpathFile(false/*
 																	 * don't
 																	 * create
 																	 * markers
 																	 */, false/*
 																			 * don't
 																			 * log
 																			 * problems
 																			 */);
 			if (fileEntries == null) {
 				return; // could not read, ignore
 			}
 			ModelManager.PerProjectInfo info = getPerProjectInfo();
 			if (info.rawBuildpath != null) { // if there is an in-memory
 				// buildpath
 				if (isBuildpathEqualsTo(info.rawBuildpath, fileEntries)) {
 					wasSuccessful = true;
 					return;
 				}
 			}
 
 			// will force an update of the buildpath/output location based on
 			// the file information
 			// extract out the output location
 			IBuildpathEntry[] oldResolvedBuildpath = info.resolvedBuildpath;
 			setRawBuildpath(
 					fileEntries,
 					monitor,
 					!ResourcesPlugin.getWorkspace().isTreeLocked(), // canChangeResource
 					oldResolvedBuildpath != null ? oldResolvedBuildpath
 							: getResolvedBuildpath(
 									true/* ignoreUnresolvedEntry */, false/*
 																		 * don't
 																		 * generateMarkerOnError
 																		 */,
 									false/* don't returnResolutionInProgress */),
 					true, // needValidation
 					false); // no need to save
 
 			// if reach that far, the buildpath file change got absorbed
 			wasSuccessful = true;
 		} catch (RuntimeException e) {
 			// setRawBuildpath might fire a delta, and a listener may throw an
 			// exception
 			if (this.project.isAccessible()) {
 				Util.log(e, "Could not set buildpath for " + getPath()); //$NON-NLS-1$
 			}
 			throw e; // rethrow
 		} catch (ModelException e) { // CP failed validation
 			if (!ResourcesPlugin.getWorkspace().isTreeLocked()) {
 				if (this.project.isAccessible()) {
 					if (e.getModelStatus().getException() instanceof CoreException) {
 						// happens if the .buildpath could not be written to
 						// disk
 						createBuildpathProblemMarker(new ModelStatus(
 								IModelStatusConstants.INVALID_BUILDPATH_FILE_FORMAT,
 								Messages
 										.bind(
 												Messages.buildpath_couldNotWriteBuildpathFile,
 												new String[] {
 														getElementName(),
 														e.getMessage() })));
 					} else {
 						createBuildpathProblemMarker(new ModelStatus(
 								IModelStatusConstants.INVALID_BUILDPATH_FILE_FORMAT,
 								Messages
 										.bind(
 												Messages.buildpath_invalidBuildpathInBuildpathFile,
 												new String[] {
 														getElementName(),
 														e.getMessage() })));
 					}
 				}
 			}
 			throw e; // rethrow
 		} finally {
 			if (!wasSuccessful) {
 				try {
 					this.getPerProjectInfo().updateBuildpathInformation(
 							ScriptProject.INVALID_BUILDPATH);
 					updateProjectFragments();
 				} catch (ModelException e) {
 					// ignore
 				}
 			}
 		}
 	}
 
 	public IProjectFragment[] getAllProjectFragments() throws ModelException {
 		return getAllProjectFragments(null /* no reverse map */);
 	}
 
 	public IProjectFragment[] getAllProjectFragments(Map rootToResolvedEntries)
 			throws ModelException {
 		IProjectFragment[] computed = computeProjectFragments(
 				getResolvedBuildpath(true/* ignoreUnresolvedEntry */, false/*
 																		 * don't
 																		 * generateMarkerOnError
 																		 */,
 						false/*
 							 * don't returnResolutionInProgress
 							 */), true/* retrieveExportedRoots */,
 				rootToResolvedEntries);
 		// Add all user project fragments
 		List fragments = new ArrayList();
 		fragments.addAll(Arrays.asList(computed));
 		// Call for extra model providers
 		IDLTKLanguageToolkit toolkit = DLTKLanguageManager
 				.getLanguageToolkit(this);
 		IModelProvider[] providers = ModelProviderManager.getProviders(toolkit
 				.getNatureId());
 		if (providers != null) {
 			for (int i = 0; i < providers.length; i++) {
 				providers[i].provideModelChanges(this, fragments);
 			}
 		}
 		return (IProjectFragment[]) fragments
 				.toArray(new IProjectFragment[fragments.size()]);
 	}
 
 	public static boolean hasScriptNature(IProject p) {
 		return DLTKLanguageManager.hasScriptNature(p);
 	}
 
 	public IScriptFolder[] getScriptFolders() throws ModelException {
 
 		IProjectFragment[] roots = getProjectFragments();
 		return getScriptFoldersInFragments(roots);
 	}
 
 	public IScriptFolder[] getScriptFoldersInFragments(IProjectFragment[] roots) {
 
 		ArrayList frags = new ArrayList();
 		for (int i = 0; i < roots.length; i++) {
 			IProjectFragment root = roots[i];
 			try {
 				IModelElement[] rootFragments = root.getChildren();
 				for (int j = 0; j < rootFragments.length; j++) {
 					frags.add(rootFragments[j]);
 				}
 			} catch (ModelException e) {
 				// do nothing
 			}
 		}
 		IScriptFolder[] fragments = new IScriptFolder[frags.size()];
 		frags.toArray(fragments);
 		return fragments;
 	}
 }
