 /*******************************************************************************
  * Copyright (c) 2004, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.core.internal.resources;
 
 import java.io.*;
 import java.util.*;
 import org.eclipse.core.internal.preferences.EclipsePreferences;
 import org.eclipse.core.internal.preferences.ExportedPreferences;
 import org.eclipse.core.internal.utils.*;
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.*;
 import org.eclipse.core.runtime.jobs.ISchedulingRule;
 import org.eclipse.core.runtime.jobs.MultiRule;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.core.runtime.preferences.IExportedPreferences;
 import org.eclipse.osgi.util.NLS;
 import org.osgi.service.prefs.BackingStoreException;
 import org.osgi.service.prefs.Preferences;
 
 /**
  * Represents a node in the Eclipse preference hierarchy which stores preference
  * values for projects.
  * 
  * @since 3.0
  */
 public class ProjectPreferences extends EclipsePreferences {
 
 	// cache
 	private int segmentCount;
 	private String qualifier;
 	private IProject project;
 	private IEclipsePreferences loadLevel;
 	private IFile file;
 	// cache which nodes have been loaded from disk
 	protected static Set loadedNodes = new HashSet();
 	private boolean isWriting;
 	private boolean initialized = false;
 
 	class SortedProperties extends Properties {
 
 		private static final long serialVersionUID = 1L;
 
 		class IteratorWrapper implements Enumeration {
 			Iterator iterator;
 
 			public IteratorWrapper(Iterator iterator) {
 				this.iterator = iterator;
 			}
 
 			public boolean hasMoreElements() {
 				return iterator.hasNext();
 			}
 
 			public Object nextElement() {
 				return iterator.next();
 			}
 		}
 
 		/* (non-Javadoc)
 		 * @see java.util.Hashtable#keys()
 		 */
 		public synchronized Enumeration keys() {
 			TreeSet set = new TreeSet();
 			for (Enumeration e = super.keys(); e.hasMoreElements();)
 				set.add(e.nextElement());
 			return new IteratorWrapper(set.iterator());
 		}
 	}
 
 	/**
 	 * Default constructor. Should only be called by #createExecutableExtension.
 	 */
 	public ProjectPreferences() {
 		super(null, null);
 	}
 
 	private ProjectPreferences(EclipsePreferences parent, String name) {
 		super(parent, name);
 		
 		// cache the segment count
 		String path = absolutePath();
 		segmentCount = getSegmentCount(path);
 
 		if (segmentCount == 1)
 			return;
 
 		// cache the project name
 		String projectName = getSegment(path, 1);
 		if (projectName != null)
 			project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
 
 		// cache the qualifier
 		if (segmentCount > 2)
 			qualifier = getSegment(path, 2);
 
 		if (segmentCount != 2)
 			return;
 
 		// else segmentCount == 2 so we initialize the children
 		if (initialized)
 			return;
 		try {
 			synchronized (this) {
 				String[] names = computeChildren(project.getLocation());
 				for (int i = 0; i < names.length; i++)
 					addChild(names[i], null);
 			}
 		} finally {
 			initialized = true;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.internal.preferences.EclipsePreferences#nodeExists(java.lang.String)
 	 * 
 	 * If we are at the /project node and we are checking for the existance of a child, we
 	 * want special behaviour. If the child is a single segment name, then we want to
 	 * return true if the node exists OR if a project with that name exists in the workspace.
 	 */
 	public boolean nodeExists(String path) throws BackingStoreException {
 		if (segmentCount != 1)
 			return super.nodeExists(path);
 		if (path.length() == 0)
 			return super.nodeExists(path);
 		if (path.charAt(0) == IPath.SEPARATOR)
 			return super.nodeExists(path);
 		if (path.indexOf(IPath.SEPARATOR) != -1)
 			return super.nodeExists(path);
 		// if we are checking existance of a single segment child of /project, base the answer on
 		// whether or not it exists in the workspace.
 		return ResourcesPlugin.getWorkspace().getRoot().getProject(path).exists() || super.nodeExists(path);
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
 		if (project == null || qualifier == null)
 			return null;
 		IPath path = project.getLocation();
 		return computeLocation(path, qualifier);
 	}
 
 	protected boolean isAlreadyLoaded(IEclipsePreferences node) {
 		return loadedNodes.contains(node.absolutePath());
 	}
 
 	protected void loaded() {
 		loadedNodes.add(absolutePath());
 	}
 
 	/*
 	 * Return the node at which these preferences are loaded/saved.
 	 */
 	protected IEclipsePreferences getLoadLevel() {
 		if (loadLevel == null) {
 			if (project == null || qualifier == null)
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
 
 	protected EclipsePreferences internalCreate(EclipsePreferences nodeParent, String nodeName, Plugin context) {
 		return new ProjectPreferences(nodeParent, nodeName);
 	}
 
 	private IFile getFile() {
 		if (file == null) {
 			if (project == null || qualifier == null)
 				return null;
 			file = getFile(project, qualifier);
 		}
 		return file;
 	}
 
 	/*
 	 * Return the preferences file for the given project and qualifier.
 	 */
 	static IFile getFile(IProject project, String qualifier) {
 		return project.getFile(new Path(DEFAULT_PREFERENCES_DIRNAME).append(qualifier).addFileExtension(PREFS_FILE_EXTENSION));
 	}
 
 	/*
 	 * Return the preferences file for the given folder and qualifier.
 	 */
 	static IFile getFile(IFolder folder, String qualifier) {
 		Assert.isLegal(folder.getName().equals(DEFAULT_PREFERENCES_DIRNAME));
 		return folder.getFile(new Path(qualifier).addFileExtension(PREFS_FILE_EXTENSION));
 	}
 	
 	/*
 	 * Return the corresponding IFile for the given preference node, or null
 	 * if it cannot be calculated.
 	 */
 	static IFile getFile(IEclipsePreferences node) {
 		String path = node.absolutePath();
 		String projectName = getSegment(path, 2);
 		if (projectName == null)
 			return null;
 		String qualifier = getSegment(path, 3);
 		if (qualifier == null)
 			return null;
 		return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(projectName).append(DEFAULT_PREFERENCES_DIRNAME).append(qualifier).addFileExtension(PREFS_FILE_EXTENSION));
 	}
 
 	protected void save() throws BackingStoreException {
 		final IFile fileInWorkspace = getFile();
 		if (fileInWorkspace == null) {
 			if (Policy.DEBUG_PREFERENCES)
 				Policy.debug("Not saving preferences since there is no file for node: " + absolutePath()); //$NON-NLS-1$
 			return;
 		}
 		Properties table = convertToProperties(new SortedProperties(), ""); //$NON-NLS-1$
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		IResourceRuleFactory factory = workspace.getRuleFactory();
 		try {
 			if (table.isEmpty()) {
 				IWorkspaceRunnable operation = new IWorkspaceRunnable() {
 					public void run(IProgressMonitor monitor) throws CoreException {
 						// nothing to save. delete existing file if one exists.
 						if (fileInWorkspace.exists()) {
 							if (Policy.DEBUG_PREFERENCES)
 								Policy.debug("Deleting preference file: " + fileInWorkspace.getFullPath()); //$NON-NLS-1$
 							if (fileInWorkspace.isReadOnly()) {
 								IStatus status = fileInWorkspace.getWorkspace().validateEdit(new IFile[] {fileInWorkspace}, null);
 								if (!status.isOK())
 									throw new CoreException(status);
 							}
 							try {
 								fileInWorkspace.delete(true, null);
 							} catch (CoreException e) {
 								String message = NLS.bind(Messages.preferences_deleteException, fileInWorkspace.getFullPath());
 								log(new Status(IStatus.WARNING, ResourcesPlugin.PI_RESOURCES, IStatus.WARNING, message, null));
 							}
 						}
 					}
 				};
 				ISchedulingRule rule = factory.deleteRule(fileInWorkspace);
				try {
					ResourcesPlugin.getWorkspace().run(operation, rule, IResource.NONE, null);
				} catch (OperationCanceledException e) {
					throw new BackingStoreException(Messages.preferences_operationCanceled);
				}
 				return;
 			}
 			table.put(VERSION_KEY, VERSION_VALUE);
 			ByteArrayOutputStream output = new ByteArrayOutputStream();
 			try {
 				table.store(output, null);
 			} catch (IOException e) {
 				String message = NLS.bind(Messages.preferences_saveProblems, absolutePath());
 				log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.ERROR, message, e));
 				throw new BackingStoreException(message);
 			} finally {
 				try {
 					output.close();
 				} catch (IOException e) {
 					// ignore
 				}
 			}
 			final InputStream input = new BufferedInputStream(new ByteArrayInputStream(output.toByteArray()));
 			IWorkspaceRunnable operation = new IWorkspaceRunnable() {
 				public void run(IProgressMonitor monitor) throws CoreException {
 					if (fileInWorkspace.exists()) {
 						if (Policy.DEBUG_PREFERENCES)
 							Policy.debug("Setting preference file contents for: " + fileInWorkspace.getFullPath()); //$NON-NLS-1$
 						if (fileInWorkspace.isReadOnly()) {
 							IStatus status = fileInWorkspace.getWorkspace().validateEdit(new IFile[] {fileInWorkspace}, null);
 							if (!status.isOK())
 								throw new CoreException(status);
 						}
 						// set the contents
 						fileInWorkspace.setContents(input, IResource.KEEP_HISTORY, null);
 					} else {
 						// create the file
 						IFolder folder = (IFolder) fileInWorkspace.getParent();
 						if (!folder.exists()) {
 							if (Policy.DEBUG_PREFERENCES)
 								Policy.debug("Creating parent preference directory: " + folder.getFullPath()); //$NON-NLS-1$
 							folder.create(IResource.NONE, true, null);
 						}
 						if (Policy.DEBUG_PREFERENCES)
 							Policy.debug("Creating preference file: " + fileInWorkspace.getLocation()); //$NON-NLS-1$
 						fileInWorkspace.create(input, IResource.NONE, null);
 					}
 				}
 			};
 			//don't bother with scheduling rules if we are already inside an operation
			try {
				if (((Workspace)workspace).getWorkManager().isLockAlreadyAcquired()) {
					operation.run(null);
				} else {
					// we might: create the .settings folder, create the file, or modify the file.
					ISchedulingRule rule = MultiRule.combine(factory.createRule(fileInWorkspace.getParent()), factory.modifyRule(fileInWorkspace));
						workspace.run(operation, rule, IResource.NONE, null);
				}
			} catch (OperationCanceledException e) {
				throw new BackingStoreException(Messages.preferences_operationCanceled);
 			}
 		} catch (CoreException e) {
 			String message = NLS.bind(Messages.preferences_saveProblems, fileInWorkspace.getFullPath());
 			log(new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, message, e));
 			throw new BackingStoreException(message);
 		}
 	}
 
 	protected void load() throws BackingStoreException {
 		IFile localFile = getFile();
 		if (localFile == null || !localFile.exists()) {
 			if (Policy.DEBUG_PREFERENCES)
 				Policy.debug("Unable to determine preference file or file does not exist for node: " + absolutePath()); //$NON-NLS-1$
 			return;
 		}
 		if (Policy.DEBUG_PREFERENCES)
 			Policy.debug("Loading preferences from file: " + localFile.getFullPath()); //$NON-NLS-1$
 		Properties fromDisk = new Properties();
 		InputStream input = null;
 		try {
 			input = new BufferedInputStream(localFile.getContents(true));
 			fromDisk.load(input);
 		} catch (CoreException e) {
 			String message = NLS.bind(Messages.preferences_loadException, localFile.getFullPath());
 			log(new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, message, e));
 			throw new BackingStoreException(message);
 		} catch (IOException e) {
 			String message = NLS.bind(Messages.preferences_loadException, localFile.getFullPath());
 			log(new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, message, e));
 			throw new BackingStoreException(message);
 		} finally {
 			if (input != null)
 				try {
 					input.close();
 				} catch (IOException e) {
 					// ignore
 				}
 		}
 		convertFromProperties(this, fromDisk, true);
 	}
 
 	private static Properties loadProperties(IFile file) throws BackingStoreException {
 		if (Policy.DEBUG_PREFERENCES)
 			Policy.debug("Loading preferences from file: " + file.getFullPath()); //$NON-NLS-1$
 		Properties result = new Properties();
 		InputStream input = null;
 		try {
 			input = new BufferedInputStream(file.getContents(true));
 			result.load(input);
 		} catch (CoreException e) {
 			String message = NLS.bind(Messages.preferences_loadException, file.getFullPath());
 			log(new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, message, e));
 			throw new BackingStoreException(message);
 		} catch (IOException e) {
 			String message = NLS.bind(Messages.preferences_loadException, file.getFullPath());
 			log(new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, message, e));
 			throw new BackingStoreException(message);
 		} finally {
 			if (input != null)
 				try {
 					input.close();
 				} catch (IOException e) {
 					// ignore
 				}
 		}
 		return result;
 	}
 
 	private static void read(ProjectPreferences node, IFile file) throws BackingStoreException, CoreException {
 		if (file == null || !file.exists()) {
 			if (Policy.DEBUG_PREFERENCES)
 				Policy.debug("Unable to determine preference file or file does not exist for node: " + node.absolutePath()); //$NON-NLS-1$
 			return;
 		}
 		Properties fromDisk = loadProperties(file);
 		// no work to do
 		if (fromDisk.isEmpty())
 			return;
 		// create a new node to store the preferences in.
 		IExportedPreferences myNode = (IExportedPreferences) ExportedPreferences.newRoot().node(node.absolutePath());
 		convertFromProperties((EclipsePreferences) myNode, fromDisk, false);
 		Platform.getPreferencesService().applyPreferences(myNode);
 	}
 
 	public static void updatePreferences(IFile file) throws CoreException {
 		IPath path = file.getFullPath();
 		// if we made it this far we are inside /project/.settings and might
 		// have a change to a preference file
 		if (!PREFS_FILE_EXTENSION.equals(path.getFileExtension()))
 			return;
 
 		String project = path.segment(0);
 		String qualifier = path.removeFileExtension().lastSegment();
 		Preferences root = Platform.getPreferencesService().getRootNode();
 		Preferences node = root.node(ProjectScope.SCOPE).node(project).node(qualifier);
 		String message = null;
 		try {
 			message = NLS.bind(Messages.preferences_syncException, node.absolutePath());
 			if (!(node instanceof ProjectPreferences))
 				return;
 			ProjectPreferences projectPrefs = (ProjectPreferences) node;
 			if (projectPrefs.isWriting)
 				return;
 			read(projectPrefs, file);
 			// make sure that we generate the appropriate resource change events
 			// if encoding settings have changed
 			if (ResourcesPlugin.PI_RESOURCES.equals(qualifier))
 				preferencesChanged(file.getProject());
 		} catch (BackingStoreException e) {
 			IStatus status = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, message, e);
 			throw new CoreException(status);
 		}
 	}
 
 	private static void preferencesChanged(IProject project) {
 		Workspace workspace = ((Workspace) ResourcesPlugin.getWorkspace());
 		workspace.getCharsetManager().projectPreferencesChanged(project);
 		workspace.getContentDescriptionManager().projectPreferencesChanged(project);
 	}
 
 	static void removeNode(Preferences node) throws CoreException {
 		String message = NLS.bind(Messages.preferences_removeNodeException, node.absolutePath());
 		try {
 			node.removeNode();
 		} catch (BackingStoreException e) {
 			IStatus status = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, message, e);
 			throw new CoreException(status);
 		}
 		String path = node.absolutePath();
 		for (Iterator i = loadedNodes.iterator(); i.hasNext();) {
 			String key = (String) i.next();
 			if (key.startsWith(path))
 				i.remove();
 		}
 	}
 
 	/*
 	 * The whole project has been removed so delete all of the project settings
 	 */
 	static void deleted(IProject project) throws CoreException {
 		// The settings dir has been removed/moved so remove all project prefs
 		// for the resource. We have to do this now because (since we aren't
 		// synchronizing) there is short-circuit code that doesn't visit the
 		// children.
 		Preferences root = Platform.getPreferencesService().getRootNode();
 		Preferences projectNode = root.node(ProjectScope.SCOPE).node(project.getName());
 		// check if we need to notify the charset manager
 		boolean hasResourcesSettings = getFile(project, ResourcesPlugin.PI_RESOURCES).exists();
 		// remove the preferences
 		removeNode(projectNode);
 		// notifies the CharsetManager 		
 		if (hasResourcesSettings)
 			preferencesChanged(project);
 	}
 
 	static void deleted(IFile file) throws CoreException {
 		IPath path = file.getFullPath();
 		int count = path.segmentCount();
 		if (count != 3)
 			return;
 		// check if we are in the .settings directory
 		if (!EclipsePreferences.DEFAULT_PREFERENCES_DIRNAME.equals(path.segment(1)))
 			return;
 		Preferences root = Platform.getPreferencesService().getRootNode();
 		String project = path.segment(0);
 		String qualifier = path.removeFileExtension().lastSegment();
 		Preferences projectNode = root.node(ProjectScope.SCOPE).node(project);
 		try {
 			if (!projectNode.nodeExists(qualifier))
 				return;
 		} catch (BackingStoreException e) {
 			// ignore
 		}
 		// remove the preferences
 		removeNode(projectNode.node(qualifier));
 		// notifies the CharsetManager if needed
 		if (qualifier.equals(ResourcesPlugin.PI_RESOURCES))
 			preferencesChanged(file.getProject());
 	}
 
 	static void deleted(IResource resource) throws CoreException {
 		switch (resource.getType()) {
 			case IResource.FILE :
 				deleted((IFile) resource);
 				return;
 			case IResource.FOLDER :
 				deleted((IFolder) resource);
 				return;
 			case IResource.PROJECT :
 				deleted((IProject) resource);
 				return;
 		}
 	}
 
 	static void deleted(IFolder folder) throws CoreException {
 		IPath path = folder.getFullPath();
 		int count = path.segmentCount();
 		if (count != 2)
 			return;
 		// check if we are the .settings directory
 		if (!EclipsePreferences.DEFAULT_PREFERENCES_DIRNAME.equals(path.segment(1)))
 			return;
 		Preferences root = Platform.getPreferencesService().getRootNode();
 		// The settings dir has been removed/moved so remove all project prefs
 		// for the resource.
 		String project = path.segment(0);
 		Preferences projectNode = root.node(ProjectScope.SCOPE).node(project);
 		// check if we need to notify the charset manager
 		boolean hasResourcesSettings = getFile(folder, ResourcesPlugin.PI_RESOURCES).exists();
 		// remove the preferences
 		removeNode(projectNode);
 		// notifies the CharsetManager 		
 		if (hasResourcesSettings)
 			preferencesChanged(folder.getProject());
 	}
 
 	public void flush() throws BackingStoreException {
 		isWriting = true;
 		try {
 			super.flush();
 		} finally {
 			isWriting = false;
 		}
 	}
 }
