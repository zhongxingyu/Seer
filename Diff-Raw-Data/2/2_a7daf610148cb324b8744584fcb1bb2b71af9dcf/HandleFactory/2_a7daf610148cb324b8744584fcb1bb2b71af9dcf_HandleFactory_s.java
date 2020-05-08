 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.core.util;
 
 import java.util.HashMap;
 import java.util.HashSet;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.compiler.CharOperation;
 import org.eclipse.dltk.compiler.env.lookup.Scope;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.IScriptFolder;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.internal.compiler.lookup.TypeScope;
 import org.eclipse.dltk.internal.core.Model;
 import org.eclipse.dltk.internal.core.ModelManager;
 import org.eclipse.dltk.internal.core.Openable;
 import org.eclipse.dltk.internal.core.ScriptProject;
 
 /**
  * Creates script element handles.
  */
 public class HandleFactory {
 
 	/**
 	 * Cache package fragment root information to optimize speed performance.
 	 */
 	private String lastPkgFragmentRootPath;
 	private IProjectFragment lastPkgFragmentRoot;
 
 	/**
 	 * Cache package handles to optimize memory.
 	 */
 	private HashtableOfArrayToObject packageHandles;
 
 	private Model model;
 
 	public HandleFactory() {
 		this.model = ModelManager.getModelManager().getModel();
 	}
 
 	/**
 	 * Creates an Openable handle from the given resource path.
 	 * 
 	 * If not null, uses the given scope as a hint for getting DLTK project
 	 * handles.
 	 */
 	public Openable createOpenable(String resourcePath, IDLTKSearchScope scope) {
 		int separatorIndex;
 		if ((separatorIndex = resourcePath
 				.indexOf(IDLTKSearchScope.FILE_ENTRY_SEPARATOR)) > -1) {
 			// path to a class file inside a archive
 			// Optimization: cache package fragment root handle and package
 			// handles
 			int rootPathLength;
 			if (this.lastPkgFragmentRootPath == null
 					|| (rootPathLength = this.lastPkgFragmentRootPath.length()) != resourcePath
 							.length()
 					|| !resourcePath.regionMatches(0,
 							this.lastPkgFragmentRootPath, 0, rootPathLength)) {
 				String archivePath = resourcePath.substring(0, separatorIndex);
 				IProjectFragment root = this.getArchiveProjectFragment(
 						archivePath, scope);
 				if (root == null)
 					return null; // match is outside classpath
 				this.lastPkgFragmentRootPath = archivePath;
 				this.lastPkgFragmentRoot = root;
 				this.packageHandles = new HashtableOfArrayToObject(5);
 			}
 			// create handle
 			String classFilePath = resourcePath.substring(separatorIndex + 1);
 			String[] simpleNames = Path.fromPortableString(classFilePath)
 					.segments();
 			String[] pkgName;
 			int length = simpleNames.length - 1;
 			if (length > 0) {
 				pkgName = new String[length];
 				System.arraycopy(simpleNames, 0, pkgName, 0, length);
 			} else {
 				pkgName = CharOperation.NO_STRINGS;
 			}
 			IScriptFolder pkgFragment = (IScriptFolder) this.packageHandles
 					.get(pkgName);
 			if (pkgFragment == null) {
 				pkgFragment = ((IProjectFragment) this.lastPkgFragmentRoot)
 						.getScriptFolder(toPath(pkgName));
 				this.packageHandles.put(pkgName, pkgFragment);
 			}
 			ISourceModule classFile = pkgFragment
 					.getSourceModule(simpleNames[length]);
 			return (Openable) classFile;
 		} else {
 			// path to a file in a directory
 			// Optimization: cache package fragment root handle and package
 			// handles
 			int rootPathLength = -1;
 			if (this.lastPkgFragmentRootPath == null
 					|| !(resourcePath.startsWith(this.lastPkgFragmentRootPath)
 							&& (rootPathLength = this.lastPkgFragmentRootPath
 									.length()) > 0 && resourcePath
 							.charAt(rootPathLength) == '/')) {
 				IProjectFragment root = this.getProjectFragment(resourcePath,
 						scope);
 				if (root == null)
 					return null; // match is outside classpath
 				this.lastPkgFragmentRoot = root;
 				this.lastPkgFragmentRootPath = this.lastPkgFragmentRoot
 						.getPath().toString();
 				this.packageHandles = new HashtableOfArrayToObject(5);
 			}
 			// create handle
 			resourcePath = resourcePath.substring(this.lastPkgFragmentRootPath
					.length() + 1);
 			String[] simpleNames = Path.fromPortableString(resourcePath)
 					.segments();
 			String[] pkgName;
 			int length = simpleNames.length - 1;
 			if (length > 0) {
 				pkgName = new String[length];
 				System.arraycopy(simpleNames, 0, pkgName, 0, length);
 			} else {
 				pkgName = CharOperation.NO_STRINGS;
 			}
 			IScriptFolder pkgFragment = (IScriptFolder) this.packageHandles
 					.get(pkgName);
 			if (pkgFragment == null) {
 				IPath ppath = toPath(pkgName);
 				IProjectFragment root = (IProjectFragment) this.lastPkgFragmentRoot;
 				pkgFragment = root.getScriptFolder(ppath);
 				this.packageHandles.put(pkgName, pkgFragment);
 			}
 			String simpleName = simpleNames[length];
 			ISourceModule unit = pkgFragment.getSourceModule(simpleName);
 			return (Openable) unit;
 		}
 	}
 
 	private IPath toPath(String[] pkgName) {
 		IPath path = new Path(""); //$NON-NLS-1$
 		for (int i = 0; i < pkgName.length; ++i) {
 			path = path.append(pkgName[i]);
 		}
 		return path;
 	}
 
 	/**
 	 * Returns a handle denoting the class member identified by its scope.
 	 */
 	public IModelElement createElement(TypeScope scope, ISourceModule unit,
 			HashSet existingElements, HashMap knownScopes) {
 		return createElement(scope, scope.referenceContext.sourceStart(), unit,
 				existingElements, knownScopes);
 	}
 
 	/**
 	 * Create handle by adding child to parent obtained by recursing into parent
 	 * scopes.
 	 */
 	private IModelElement createElement(Scope scope, int elementPosition,
 			ISourceModule unit, HashSet existingElements, HashMap knownScopes) {
 		if (DLTKCore.DEBUG) {
 			System.err.println("TODO: HandleFactory: Add implementation..."); //$NON-NLS-1$
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the package fragment root that corresponds to the given jar path.
 	 * See createOpenable(...) for the format of the jar path string. If not
 	 * null, uses the given scope as a hint for getting script project handles.
 	 */
 	private IProjectFragment getArchiveProjectFragment(
 			String archivePathString, IDLTKSearchScope scope) {
 
 		IPath archivePath = Path.fromPortableString(archivePathString);
 
 		Object target = Model.getTarget(ResourcesPlugin.getWorkspace()
 				.getRoot(), archivePath, false);
 		if (target instanceof IFile) {
 			// internal jar: is it on the classpath of its project?
 			// e.g. org.eclipse.swt.win32/ws/win32/swt.jar
 			// is NOT on the classpath of org.eclipse.swt.win32
 			IFile archiveFile = (IFile) target;
 			ScriptProject scriptProject = (ScriptProject) this.model
 					.getScriptProject(archiveFile);
 			IBuildpathEntry[] classpathEntries;
 			try {
 				classpathEntries = scriptProject.getResolvedBuildpath();
 				for (int j = 0, entryCount = classpathEntries.length; j < entryCount; j++) {
 					if (classpathEntries[j].getPath().equals(archivePath)) {
 						return scriptProject.getProjectFragment(archiveFile);
 					}
 				}
 			} catch (ModelException e) {
 				// ignore and try to find another project
 			}
 		}
 
 		// walk projects in the scope and find the first one that has the given
 		// archive path in its classpath
 		IScriptProject[] projects;
 		if (scope != null) {
 			IPath[] enclosingProjectsAndArchives = scope
 					.enclosingProjectsAndZips();
 			int length = enclosingProjectsAndArchives.length;
 			projects = new IScriptProject[length];
 			int index = 0;
 			for (int i = 0; i < length; i++) {
 				IPath path = enclosingProjectsAndArchives[i];
 				if (!org.eclipse.dltk.compiler.util.Util.isArchiveFileName(
 						scope.getLanguageToolkit(), path.lastSegment())) {
 					projects[index++] = this.model.getScriptProject(path
 							.segment(0));
 				}
 			}
 			if (index < length) {
 				System.arraycopy(projects, 0,
 						projects = new IScriptProject[index], 0, index);
 			}
 			IProjectFragment root = getArchiveFolder(archivePath, target,
 					projects);
 			if (root != null) {
 				return root;
 			}
 		}
 
 		// not found in the scope, walk all projects
 		try {
 			IDLTKLanguageToolkit toolkit = null;
 			if (scope != null) {
 				toolkit = scope.getLanguageToolkit();
 			}
 			if (toolkit == null) {
 				return null;
 			}
 			projects = this.model.getScriptProjects(toolkit.getNatureId());
 		} catch (ModelException e) {
 			// script model is not accessible
 			return null;
 		}
 		return getArchiveFolder(archivePath, target, projects);
 	}
 
 	private IProjectFragment getArchiveFolder(IPath archivePath, Object target,
 			IScriptProject[] projects) {
 		for (int i = 0, projectCount = projects.length; i < projectCount; i++) {
 			try {
 				ScriptProject scriptProject = (ScriptProject) projects[i];
 				IBuildpathEntry[] classpathEntries = scriptProject
 						.getResolvedBuildpath(true/* ignoreUnresolvedEntry */,
 								false/* don't generateMarkerOnError */, false/*
 																			 * don't
 																			 * returnResolutionInProgress
 																			 */);
 				for (int j = 0, entryCount = classpathEntries.length; j < entryCount; j++) {
 					if (classpathEntries[j].getPath().equals(archivePath)) {
 						if (target instanceof IFile) {
 							// internal jar
 							return scriptProject
 									.getProjectFragment((IFile) target);
 						} else {
 							// external jar
 							return scriptProject
 									.getProjectFragment0(archivePath);
 						}
 					}
 				}
 			} catch (ModelException e) {
 				// ModelException from getResolvedClasspath - a problem occured
 				// while accessing project: nothing we can do, ignore
 			}
 		}
 		return null;
 	}
 
 	private static IProject[] getAllProjects() {
 		return ResourcesPlugin.getWorkspace().getRoot().getProjects();
 	}
 
 	private static boolean checkScope(IProject project, IPath[] scopeProjects) {
 		final IPath location = project.getFullPath();
 		for (int j = 0; j < scopeProjects.length; j++) {
 			if (scopeProjects[j].equals(location)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Returns the package fragment root that contains the given resource path.
 	 * 
 	 * @param scope
 	 */
 	private IProjectFragment getProjectFragment(String pathString,
 			IDLTKSearchScope scope) {
 
 		IPath path = Path.fromPortableString(pathString);
 		IProject[] projects = getAllProjects();
 		IPath[] enclosingProjectsAndZips = scope.enclosingProjectsAndZips();
 		for (int i = 0, max = projects.length; i < max; i++) {
 			try {
 				IProject project = projects[i];
 				if (!checkScope(project, enclosingProjectsAndZips)) {
 					continue;
 				}
 
 				if (!project.isAccessible()
 						|| !DLTKLanguageManager.hasScriptNature(project))
 					continue;
 				IScriptProject scriptProject = this.model
 						.getScriptProject(project);
 				IProjectFragment[] roots = scriptProject.getProjectFragments();
 				for (int j = 0, rootCount = roots.length; j < rootCount; j++) {
 					IProjectFragment root = (IProjectFragment) roots[j];
 					if (root.getPath().isPrefixOf(path)
 							&& !Util.isExcluded(path, root, false)) {
 						return root;
 					}
 				}
 			} catch (CoreException e) {
 				// CoreException from hasNature - should not happen since we
 				// check that the project is accessible
 				// ModelException from getProjectFragments - a problem occured
 				// while accessing project: nothing we can do, ignore
 			}
 		}
 		return null;
 	}
 }
