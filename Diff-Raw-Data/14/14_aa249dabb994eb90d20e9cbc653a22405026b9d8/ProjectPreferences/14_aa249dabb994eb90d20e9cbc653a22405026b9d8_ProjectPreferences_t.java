 /*******************************************************************************
  * Copyright (c) 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.core.internal.resources;
 
import java.util.HashSet;
import java.util.Set;
 import org.eclipse.core.internal.preferences.EclipsePreferences;
 import org.eclipse.core.internal.runtime.InternalPlatform;
 import org.eclipse.core.resources.ProjectScope;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.osgi.service.prefs.BackingStoreException;
 
 /**
  * Represents a node in the Eclipse preference hierarchy which stores preference
  * values for projects.
  * 
  * @since 3.0
  */
 public class ProjectPreferences extends EclipsePreferences {
 
 	private static final String PREFS_FILE_EXTENSION = "prefs"; //$NON-NLS-1$
 
 	protected boolean isLoading = false;
 
 	// cache
 	private int segmentCount = 0;
 	private String qualifier;
 	private String projectName;
 	private EclipsePreferences loadLevel;
	// cache which nodes have been loaded from disk
	private static Set loadedNodes = new HashSet();
 
 	/**
 	 * Default constructor. Should only be called by #createExecutableExtension.
 	 */
 	public ProjectPreferences() {
 		super(null, null);
 	}
 
 	private ProjectPreferences(IEclipsePreferences parent, String name) {
 		super(parent, name);
 		initialize();
 	}
 
 	/*
 	 * Calculate and return the file system location for this preference node.
 	 * Use the absolute path of the node to find out the project name so 
 	 * we can get its location on disk.
 	 * 
 	 * NOTE: we cannot cache the location since it may change over the course
 	 * of the project life-cycle.
 	 */
 	protected IPath getLocation() {
 		if (projectName == null || qualifier == null)
 			return null;
 		IPath path = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName).getLocation();
 		return path == null ? null : path.append(DEFAULT_PREFERENCES_DIRNAME).append(qualifier).addFileExtension(PREFS_FILE_EXTENSION);
 	}
 
 	/*
 	 * Parse this node's absolute path and initialize some cached values for
 	 * later use.
 	 */
 	private void initialize() {
 		// cache the segment count
 		IPath path = new Path(absolutePath());
 		segmentCount = path.segmentCount();
 		if (segmentCount < 2)
 			return;
 
 		// cache the project name
 		String scope = path.segment(0);
 		if (ProjectScope.SCOPE.equals(scope))
 			projectName = path.segment(1);
 
 		// cache the qualifier
 		if (segmentCount > 2)
 			qualifier = path.segment(2);
 	}
 
 	/*
 	 * @see org.osgi.service.prefs.Preferences#sync()
 	 */
 	public void sync() throws BackingStoreException {
 		// TODO install a resource change listener on the preference file to
 		// automatically get changes into the running instance.
 		IPath location = getLocation();
 		if (location == null) {
 			if (InternalPlatform.DEBUG_PREFERENCES)
 				System.out.println("Unable to determine location of preference file for node: " + absolutePath()); //$NON-NLS-1$
 			return;
 		}
 		IEclipsePreferences node = getLoadLevel();
 		if (node == null) {
 			if (InternalPlatform.DEBUG_PREFERENCES)
 				System.out.println("Preference node is not a load root: " + absolutePath()); //$NON-NLS-1$
 			return;
 		}
 		if (node instanceof EclipsePreferences) {
 			((EclipsePreferences) node).load(location);
 			node.flush();
 		}
 	}
 
	protected boolean isAlreadyLoaded(IEclipsePreferences node) {
		return loadedNodes.contains(node.name());
	}

	protected void loaded() {
		loadedNodes.add(name());
	}

 	/*
 	 * Return the node at which these preferences are loaded/saved.
 	 */
 	protected IEclipsePreferences getLoadLevel() {
 		if (loadLevel == null) {
 			if (projectName == null || qualifier == null)
 				return null;
 			// Make it relative to this node rather than navigating to it from the root.
 			// Walk backwards up the tree starting at this node.
 			// This is important to avoid a chicken/egg thing on startup.
 			EclipsePreferences node = this;
 			for (int i = 3; i < segmentCount; i++)
 				node = (EclipsePreferences) node.parent();
 			loadLevel = node;
 		}
 		return loadLevel;
 	}
 
 	protected EclipsePreferences internalCreate(IEclipsePreferences nodeParent, String nodeName) {
 		return new ProjectPreferences(nodeParent, nodeName);
 	}
 
 }
