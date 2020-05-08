 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.core.search;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.compiler.util.SimpleSet;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IDLTKProject;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IScriptModel;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.SearchPattern;
 import org.eclipse.dltk.core.search.indexing.IndexManager;
 import org.eclipse.dltk.core.search.matching.MatchLocator;
 import org.eclipse.dltk.internal.core.ArchiveProjectFragment;
 import org.eclipse.dltk.internal.core.DLTKProject;
 import org.eclipse.dltk.internal.core.ModelManager;
 
 /**
  * Selects the indexes that correspond to projects in a given search scope and
  * that are dependent on a given focus element.
  */
 public class IndexSelector {
 	IDLTKSearchScope searchScope;
 	SearchPattern pattern;
 	IPath[] indexLocations; // cache of the keys for looking index up
 //	public boolean mixin = false; // Set to true then mixin search are used.
 
 	// Filter some builtin elements.
 
 	public IndexSelector(IDLTKSearchScope searchScope, SearchPattern pattern) {
 		this.searchScope = searchScope;
 		this.pattern = pattern;
 	}
 
 	/**
 	 * Returns whether elements of the given project or jar can see the given
 	 * focus (an IScriptProject or a JarScriptFolderRot) either because the
 	 * focus is part of the project or the jar, or because it is accessible
 	 * throught the project's classpath
 	 */
 	public static boolean canSeeFocus(IModelElement focus,
 			boolean isPolymorphicSearch, IPath projectOrArchivePath) {
 		try {
 			IBuildpathEntry[] focusEntries = null;
 			if (isPolymorphicSearch) {
 				DLTKProject focusProject = focus instanceof ArchiveProjectFragment ? (DLTKProject) focus
 						.getParent()
 						: (DLTKProject) focus;
 				focusEntries = focusProject.getExpandedBuildpath(true);
 			}
 			IScriptModel model = focus.getModel();
 			IDLTKProject project = getScriptProject(projectOrArchivePath, model);
 			if (project != null)
 				return canSeeFocus(focus, (DLTKProject) project, focusEntries);
 			// projectOrJarPath is a jar
 			// it can see the focus only if it is on the buildpath of a project
 			// projectOrArchivePath is a archive
 			// it can see the focus only if it is on the classpath of a project
 			// that can see the focus
 			IDLTKProject[] allProjects = model.getScriptProjects();
 			for (int i = 0, length = allProjects.length; i < length; i++) {
 				DLTKProject otherProject = (DLTKProject) allProjects[i];
 				IBuildpathEntry[] entries = otherProject.getResolvedBuildpath(
 						true/* ignoreUnresolvedEntry */,
 						false/* don't generateMarkerOnError */, false/*
 																		 * don't
 																		 * returnResolutionInProgress
 																		 */);
 				for (int j = 0, length2 = entries.length; j < length2; j++) {
 					IBuildpathEntry entry = entries[j];
 					if (entry.getEntryKind() == IBuildpathEntry.BPE_LIBRARY
 							&& entry.getPath().equals(projectOrArchivePath))
 						if (canSeeFocus(focus, otherProject, focusEntries))
 							return true;
 				}
 			}
 			return false;
 		} catch (ModelException e) {
 			return false;
 		}
 	}
 
 	public static boolean canSeeFocus(IModelElement focus,
 			DLTKProject scriptProject,
 			IBuildpathEntry[] focusEntriesForPolymorphicSearch) {
 		try {
 			if (focus.equals(scriptProject))
 				return true;
 			if (focusEntriesForPolymorphicSearch != null) {
 				// look for refering project
 				IPath projectPath = scriptProject.getProject().getFullPath();
 				for (int i = 0, length = focusEntriesForPolymorphicSearch.length; i < length; i++) {
 					IBuildpathEntry entry = focusEntriesForPolymorphicSearch[i];
 					if (entry.getEntryKind() == IBuildpathEntry.BPE_PROJECT
 							&& entry.getPath().equals(projectPath))
 						return true;
 				}
 			}
 			if (focus instanceof ArchiveProjectFragment) {
 				// focus is part of a archive
 				IPath focusPath = focus.getPath();
 				IBuildpathEntry[] entries = scriptProject
 						.getExpandedBuildpath(true);
 				for (int i = 0, length = entries.length; i < length; i++) {
 					IBuildpathEntry entry = entries[i];
 					if (entry.getEntryKind() == IBuildpathEntry.BPE_LIBRARY
 							&& entry.getPath().equals(focusPath))
 						return true;
 				}
 				return false;
 			}
 			// look for dependent projects
 			IPath focusPath = ((DLTKProject) focus).getProject().getFullPath();
 			IBuildpathEntry[] entries = scriptProject
 					.getExpandedBuildpath(true);
 			for (int i = 0, length = entries.length; i < length; i++) {
 				IBuildpathEntry entry = entries[i];
 				if (entry.getEntryKind() == IBuildpathEntry.BPE_PROJECT
 						&& entry.getPath().equals(focusPath))
 					return true;
 			}
 			return false;
 		} catch (ModelException e) {
 			return false;
 		}
 	}
 
 	/*
 	 * Compute the list of paths which are keying index files.
 	 */
 	private void initializeIndexLocations() {
 		IPath[] projectsAndArchives = this.searchScope
 				.enclosingProjectsAndZips();
 //		System.out.println("********************IndexSelector************************");
 //		for (int i = 0; i < projectsAndArchives.length; i++) {
 //			System.out.println("Root:" + projectsAndArchives[i].toString());
 //		}
 //		System.out.println("********************IndexSelector************************");
 		IndexManager manager = ModelManager.getModelManager().getIndexManager();
 		SimpleSet locations = new SimpleSet();
 		IModelElement focus = MatchLocator.projectOrArchiveFocus(this.pattern);
 		// Add all special indexes for selected project.
 		/*
 		 * if( focus != null && focus.getElementType() ==
 		 * IModelElement.SCRIPT_PROJECT ) { String prjPath = "#special#mixin:" +
 		 * ((IDLTKProject)focus).getProject().getFullPath().toString();
 		 * checkSpecialCase(manager, locations, prjPath); // builtin index file
 		 * prjPath = "#special#builtin:" +
 		 * ((IDLTKProject)focus).getProject().getFullPath().toString();
 		 * checkSpecialCase(manager, locations, prjPath); }
 		 */
 		
 		boolean mix = false;//this.pattern instanceof MixinPattern;
 		
 		IScriptModel model = ModelManager.getModelManager().getModel();
 		if (focus == null) {
 			for (int i = 0; i < projectsAndArchives.length; i++) {
 //				if (!mixin) {
 				
 				if (!mix) {				
 					locations.add(manager
 							.computeIndexLocation(projectsAndArchives[i]));
 				} 
 //				}
 
 					checkSpecial(projectsAndArchives[i], manager, locations, model);
 				
 			}
 		} else {
 			try {
 				// find the projects from projectsAndArchives that see the focus
 				// then walk those projects looking for the archives from
 				// projectsAndArchives
 				int length = projectsAndArchives.length;
 				DLTKProject[] projectsCanSeeFocus = new DLTKProject[length];
 				SimpleSet visitedProjects = new SimpleSet(length);
 				int projectIndex = 0;
 				SimpleSet archivesToCheck = new SimpleSet(length);
 				IBuildpathEntry[] focusEntries = null;
 				if (this.pattern != null
 						&& MatchLocator.isPolymorphicSearch(this.pattern)) { // isPolymorphicSearch
 					DLTKProject focusProject = focus instanceof ArchiveProjectFragment ? (DLTKProject) focus
 							.getParent()
 							: (DLTKProject) focus;
 					focusEntries = focusProject.getExpandedBuildpath(true);
 				}
 
 				for (int i = 0; i < length; i++) {
 					IPath path = projectsAndArchives[i];
 					DLTKProject project = (DLTKProject) getScriptProject(path,
 							model);
 					if (project != null) {
 						visitedProjects.add(project);
 						if (canSeeFocus(focus, project, focusEntries)) {
 //							if (!mixin) {
 							if (!mix) {
 								locations.add(manager
 										.computeIndexLocation(path));
 							} 
 //							}
 								checkSpecial(path, manager, locations, model);
 							
 							projectsCanSeeFocus[projectIndex++] = project;
 						}
 					} else {
 						archivesToCheck.add(path);
 					}
 				}
 				for (int i = 0; i < projectIndex
 						&& archivesToCheck.elementSize > 0; i++) {
 					IBuildpathEntry[] entries = projectsCanSeeFocus[i]
 							.getResolvedBuildpath(
 									true/* ignoreUnresolvedEntry */,
 									false/* don't generateMarkerOnError */,
 									false/* don't returnResolutionInProgress */);
 					for (int j = entries.length; --j >= 0;) {
 						IBuildpathEntry entry = entries[j];
 						if (entry.getEntryKind() == IBuildpathEntry.BPE_LIBRARY) {
 							IPath path = entry.getPath();
 							if (archivesToCheck.includes(path)) {
 //								if (!mixin) {
 								if (!mix) {
 									locations.add(manager
 											.computeIndexLocation(entry
 													.getPath()));
 								}
 //								}
 								archivesToCheck.remove(path);
 							}
 						}
 					}
 				}
 				// archive files can be included in the search scope without
 				// including one of the projects that references them, so scan
 				// all projects that have not been visited
 				if (archivesToCheck.elementSize > 0) {
 					IDLTKProject[] allProjects = model.getScriptProjects();
 					for (int i = 0, l = allProjects.length; i < l
 							&& archivesToCheck.elementSize > 0; i++) {
 						DLTKProject project = (DLTKProject) allProjects[i];
 						if (!visitedProjects.includes(project)) {
 							IBuildpathEntry[] entries = project
 									.getResolvedBuildpath();
 							for (int j = entries.length; --j >= 0;) {
 								IBuildpathEntry entry = entries[j];
 								if (entry.getEntryKind() == IBuildpathEntry.BPE_LIBRARY) {
 									IPath path = entry.getPath();
 									if (archivesToCheck.includes(path)) {
 //										if (!mixin) {
 										if (!mix) {
 											locations.add(manager
 													.computeIndexLocation(entry
 															.getPath()));
 										}
 //										}
 										archivesToCheck.remove(path);
 									}
 								}
 							}
 						}
 					}
 				}
 			} catch (ModelException e) {
 				// ignored
 			}
 		}
 		this.indexLocations = new IPath[locations.elementSize];
 		Object[] values = locations.values;
 		int count = 0;
 		for (int i = values.length; --i >= 0;)
 			if (values[i] != null)
 				this.indexLocations[count++] = new Path((String) values[i]);
 	}
 
 	private void checkSpecial(IPath projectsAndArchives, IndexManager manager,
 			SimpleSet locations, IScriptModel model) {
 		// check for special cases
 		String prjPath = "#special#mixin#" + projectsAndArchives.toString();
 		// checkSpecialCase(manager, locations, prjPath);
 		locations.add(manager.computeIndexLocation(new Path(prjPath)));
 		// add builtin indexes
 //		IPath path = projectsAndArchives;
 ////		if (!mixin) {
 //			if (!path.toString().startsWith(IBuildpathEntry.BUILTIN_EXTERNAL_ENTRY_STR)) {
 //				DLTKProject project = (DLTKProject) getScriptProject(path,
 //						model);
 //				if (project != null) {
 //					IPath p = new Path("#special#builtin#")
 //							.append(projectsAndArchives);
 //					locations.add(manager.computeIndexLocation(p));
 //				}
 //			}
 //			else {
 //				path = path.removeFirstSegments(1);
 //				DLTKProject project = (DLTKProject) getScriptProject(path,
 //						model);
 //				if (project != null) {
 //					IPath p = new Path("#special#builtin#")
 //							.append(projectsAndArchives);
 //					locations.add(manager.computeIndexLocation(p));
 //				}
 //			}
 ////		}
 	}
 
 //	private void checkSpecialCase(IndexManager manager, SimpleSet locations,
 //			String prjPath) {
 //		Object[] keyTable = manager.indexLocations.keyTable;
 //		for (int i = 0; i < keyTable.length; ++i) {
 //			IPath path = (IPath) keyTable[i];
 //			if (path != null) {
 //				String sPath = path.toString();
 //				if (sPath.startsWith(prjPath)) {
 //					locations.add(manager.indexLocations.get(path));
 //				}
 //			}
 //		}
 //	}
 
 	public IPath[] getIndexLocations() {
 		if (this.indexLocations == null) {
 			this.initializeIndexLocations();
 		}
 		return this.indexLocations;
 	}
 
 	/**
 	 * Returns thescriptproject that corresponds to the given path. Returns null
 	 * if the path doesn't correspond to a project.
 	 */
 	private static IDLTKProject getScriptProject(IPath path, IScriptModel model) {
 		IDLTKProject project = model.getScriptProject(path.lastSegment());
 		if (project.exists()) {
 			return project;
 		}
 		return null;
 	}
 }
