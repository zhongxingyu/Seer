 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *     BEA - Daniel R Somerfield - Bug 88939
  *******************************************************************************/
 package org.eclipse.dltk.internal.launching;
 
 import java.io.IOException;
 import java.net.URI;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.core.BuildpathContainerInitializer;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.environment.EnvironmentPathUtils;
import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.dltk.launching.IRuntimeBuildpathEntry;
 import org.eclipse.dltk.launching.LaunchingMessages;
 import org.eclipse.dltk.launching.ScriptLaunchConfigurationConstants;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.ibm.icu.text.MessageFormat;
 
 /**
  * An entry on the runtime buildpath that the user can manipulate and share in a
  * launch configuration.
  */
 public class RuntimeBuildpathEntry implements IRuntimeBuildpathEntry {
 
 	/**
 	 * This entry's type - must be set on creation.
 	 */
 	private int fType = -1;
 
 	/**
 	 * This entry's buildpath property.
 	 */
 	private int fBuildpathProperty = -1;
 
 	/**
 	 * This entry's associated build path entry.
 	 */
 	private IBuildpathEntry fBuildpathEntry = null;
 
 	/**
 	 * Associated Script project, or <code>null</code>
 	 */
 	private IScriptProject fProject = null;
 
 	/**
 	 * The path if the entry was invalid and fBuildpathEntry is null
 	 */
 	private IPath fInvalidPath;
 
 	/**
 	 * Constructs a new runtime buildpath entry based on the (build) buildpath
 	 * entry.
 	 * 
 	 * @param entry
 	 *            the associated buildpath entry
 	 */
 	public RuntimeBuildpathEntry(IBuildpathEntry entry) {
 		switch (entry.getEntryKind()) {
 		case IBuildpathEntry.BPE_PROJECT:
 			setType(PROJECT);
 			break;
 		case IBuildpathEntry.BPE_LIBRARY:
 			setType(ARCHIVE);
 			break;
 		case IBuildpathEntry.BPE_SOURCE:
 			setType(SOURCE);
 			break;
 		default:
 			throw new IllegalArgumentException(
 					MessageFormat
 							.format(
 									LaunchingMessages.RuntimeBuildpathEntry_Illegal_classpath_entry__0__1,
 									new String[] { entry.toString() }));
 		}
 		setBuildpathEntry(entry);
 		initializeBuildpathProperty();
 	}
 
 	/**
 	 * Constructs a new container entry in the context of the given project
 	 * 
 	 * @param entry
 	 *            buildpath entry
 	 * @param buildpathProperty
 	 *            this entry's buildpath property
 	 */
 	public RuntimeBuildpathEntry(IBuildpathEntry entry, int buildpathProperty) {
 		switch (entry.getEntryKind()) {
 		case IBuildpathEntry.BPE_CONTAINER:
 			setType(CONTAINER);
 			break;
 		default:
 			throw new IllegalArgumentException(
 					MessageFormat
 							.format(
 									LaunchingMessages.RuntimeBuildpathEntry_Illegal_classpath_entry__0__1,
 									new String[] { entry.toString() }));
 		}
 		setBuildpathEntry(entry);
 		setBuildpathProperty(buildpathProperty);
 	}
 
 	/**
 	 * Reconstructs a runtime buildpath entry from the given XML document root
 	 * not.
 	 * 
 	 * @param root
 	 *            a memento root doc element created by this class
 	 * @exception CoreException
 	 *                if unable to restore from the given memento
 	 */
 	public RuntimeBuildpathEntry(Element root) throws CoreException {
 		try {
 			setType(Integer.parseInt(root.getAttribute("type"))); //$NON-NLS-1$
 		} catch (NumberFormatException e) {
 			abort(
 					LaunchingMessages.RuntimeBuildpathEntry_Unable_to_recover_runtime_class_path_entry_type_2,
 					e);
 		}
 		try {
 			setBuildpathProperty(Integer.parseInt(root.getAttribute("path"))); //$NON-NLS-1$
 		} catch (NumberFormatException e) {
 			abort(
 					LaunchingMessages.RuntimeBuildpathEntry_Unable_to_recover_runtime_class_path_entry_location_3,
 					e);
 		}
 
 		String path;
 
 		switch (getType()) {
 		case PROJECT:
 			String name = root.getAttribute("projectName"); //$NON-NLS-1$
 			if (isEmpty(name)) {
 				abort(
 						LaunchingMessages.RuntimeBuildpathEntry_Unable_to_recover_runtime_class_path_entry___missing_project_name_4,
 						null);
 			} else {
 				IProject proj = ResourcesPlugin.getWorkspace().getRoot()
 						.getProject(name);
 				setBuildpathEntry(DLTKCore.newProjectEntry(proj.getFullPath()));
 			}
 			break;
 		case ARCHIVE:
 			path = root.getAttribute("externalArchive"); //$NON-NLS-1$
 			if (isEmpty(path)) {
 				// internal
 				path = root.getAttribute("internalArchive"); //$NON-NLS-1$
 				if (isEmpty(path)) {
 					abort(
 							LaunchingMessages.RuntimeBuildpathEntry_Unable_to_recover_runtime_class_path_entry___missing_archive_path_5,
 							null);
 				} else {
 					setBuildpathEntry(createLibraryEntry(path));
 				}
 			} else {
 				// external
 				setBuildpathEntry(createLibraryEntry(path));
 			}
 			break;
 		case CONTAINER:
 			String var = root.getAttribute("containerPath"); //$NON-NLS-1$
 			if (isEmpty(var)) {
 				abort(
 						LaunchingMessages.RuntimeBuildpathEntry_Unable_to_recover_runtime_class_path_entry___missing_variable_name_6,
 						null);
 			} else {
 				setBuildpathEntry(DLTKCore.newContainerEntry(new Path(var)));
 			}
 			break;
 		}
 
 		String name = root.getAttribute("scriptProject"); //$NON-NLS-1$
 		if (isEmpty(name)) {
 			fProject = null;
 		} else {
 			IProject project2 = ResourcesPlugin.getWorkspace().getRoot()
 					.getProject(name);
 			fProject = DLTKCore.create(project2);
 		}
 	}
 
 	private IBuildpathEntry createLibraryEntry(String path) {
 		Path p = new Path(path);
 		if (!p.isAbsolute()) {
 			fInvalidPath = p;
 			return null;
 			// abort("There was a problem with path \" " + path + "\": paths
 			// must be absolute.", null);
 		}
 
 		return DLTKCore.newLibraryEntry(p);
 	}
 
 	/**
 	 * Throws an internal error exception
 	 */
 	protected void abort(String message, Throwable e) throws CoreException {
 		IStatus s = new Status(IStatus.ERROR, DLTKLaunchingPlugin
 				.getUniqueIdentifier(),
 				ScriptLaunchConfigurationConstants.ERR_INTERNAL_ERROR, message,
 				e);
 		throw new CoreException(s);
 	}
 
 	/**
 	 * @see IRuntimeBuildpathEntry#getType()
 	 */
 	public int getType() {
 		return fType;
 	}
 
 	/**
 	 * Sets this entry's type
 	 * 
 	 * @param type
 	 *            this entry's type
 	 */
 	private void setType(int type) {
 		fType = type;
 	}
 
 	/**
 	 * Sets the buildpath entry associated with this runtime buildpath entry.
 	 * Clears the cache of the resolved entry.
 	 * 
 	 * @param entry
 	 *            the buildpath entry associated with this runtime buildpath
 	 *            entry
 	 */
 	private void setBuildpathEntry(IBuildpathEntry entry) {
 		fBuildpathEntry = entry;
 	}
 
 	/**
 	 * @see IRuntimeBuildpathEntry#getBuildpathEntry()
 	 */
 	public IBuildpathEntry getBuildpathEntry() {
 		return fBuildpathEntry;
 	}
 
 	/**
 	 * @see IRuntimeBuildpathEntry#getMemento()
 	 */
 	public String getMemento() throws CoreException {
 
 		Document doc;
 		try {
 			doc = DLTKLaunchingPlugin.getDocument();
 		} catch (ParserConfigurationException e) {
 			IStatus status = new Status(
 					IStatus.ERROR,
 					DLTKLaunchingPlugin.getUniqueIdentifier(),
 					ScriptLaunchConfigurationConstants.ERR_INTERNAL_ERROR,
 					LaunchingMessages.RuntimeBuildpathEntry_An_exception_occurred_generating_runtime_classpath_memento_8,
 					e);
 			throw new CoreException(status);
 		}
 		Element node = doc.createElement("runtimeBuildpathEntry"); //$NON-NLS-1$
 		doc.appendChild(node);
 		node.setAttribute("type", (new Integer(getType())).toString()); //$NON-NLS-1$
 		node.setAttribute(
 				"path", (new Integer(getBuildpathProperty())).toString()); //$NON-NLS-1$
 		switch (getType()) {
 		case PROJECT:
 			node.setAttribute("projectName", getPath().lastSegment()); //$NON-NLS-1$
 			break;
 		case ARCHIVE:
 			IResource res = getResource();
 			if (res == null) {
 				node.setAttribute("externalArchive", getPath().toString()); //$NON-NLS-1$
 			} else {
 				node.setAttribute(
 						"internalArchive", res.getFullPath().toString()); //$NON-NLS-1$
 			}
 			break;
 		case CONTAINER:
 			node.setAttribute("containerPath", getPath().toString()); //$NON-NLS-1$
 			break;
 		}
 		if (getScriptProject() != null) {
 			node.setAttribute(
 					"scriptProject", getScriptProject().getElementName()); //$NON-NLS-1$
 		}
 		try {
 			return DLTKLaunchingPlugin.serializeDocument(doc);
 		} catch (IOException e) {
 			IStatus status = new Status(
 					IStatus.ERROR,
 					DLTKLaunchingPlugin.getUniqueIdentifier(),
 					ScriptLaunchConfigurationConstants.ERR_INTERNAL_ERROR,
 					LaunchingMessages.RuntimeBuildpathEntry_An_exception_occurred_generating_runtime_classpath_memento_8,
 					e);
 			throw new CoreException(status);
 		} catch (TransformerException e) {
 			IStatus status = new Status(
 					IStatus.ERROR,
 					DLTKLaunchingPlugin.getUniqueIdentifier(),
 					ScriptLaunchConfigurationConstants.ERR_INTERNAL_ERROR,
 					LaunchingMessages.RuntimeBuildpathEntry_An_exception_occurred_generating_runtime_classpath_memento_8,
 					e);
 			throw new CoreException(status);
 		}
 	}
 
 	/**
 	 * @see IRuntimeBuildpathEntry#getPath()
 	 */
 	public IPath getPath() {
 		IBuildpathEntry entry = getBuildpathEntry();
 		return entry != null ? entry.getPath() : fInvalidPath;
 	}
 
 	/**
 	 * @see IRuntimeBuildpathEntry#getResource()
 	 */
 	public IResource getResource() {
 		switch (getType()) {
 		case CONTAINER:
 			return null;
 		default:
 			return getResource(getPath());
 		}
 	}
 
 	/**
 	 * Returns the resource in the workspace assciated with the given absolute
 	 * path, or <code>null</code> if none. The path may have a device.
 	 * 
 	 * @param path
 	 *            absolute path, or <code>null</code>
 	 * @return resource or <code>null</code>
 	 */
 	protected IResource getResource(IPath path) {
 		if (path != null) {
 			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 			if (path.getDevice() == null) {
 				// search relative to the workspace if no device present
 				return root.findMember(path);
 			}
 			// look for files or folders with the given path
 			IFile[] files = root.findFilesForLocation(path);
 			if (files.length > 0) {
 				return files[0];
 			}
 			IContainer[] containers = root.findContainersForLocation(path);
 			if (containers.length > 0) {
 				return containers[0];
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Initlaizes the buildpath property based on this entry's type.
 	 */
 	private void initializeBuildpathProperty() {
 		switch (getType()) {
 		case PROJECT:
 		case ARCHIVE:
 		case SOURCE:
 			setBuildpathProperty(USER_ENTRY);
 			break;
 		default:
 			break;
 		}
 	}
 
 	/**
 	 * @see IRuntimeBuildpathEntry#setBuildpathProperty(int)
 	 */
 	public void setBuildpathProperty(int location) {
 		fBuildpathProperty = location;
 	}
 
 	/**
 	 * @see IRuntimeBuildpathEntry#setBuildpathProperty(int)
 	 */
 	public int getBuildpathProperty() {
 		return fBuildpathProperty;
 	}
 
 	/**
 	 * @see IRuntimeBuildpathEntry#getLocation()
 	 */
 	public String getLocation() {
 
 		IPath path = null;
 		switch (getType()) {
 		case PROJECT:
 			IScriptProject pro = (IScriptProject) DLTKCore
 					.create(getResource());
 			if (pro != null) {
 				path = pro.getPath();
 			}
 			break;
 		case ARCHIVE:
 			path = getPath();
			if (EnvironmentPathUtils.isFull(path)) {
				IFileHandle file = EnvironmentPathUtils.getFile(path);
				if (file != null) {
					path = file.getPath();
				}
			}
 			break;
 		case SOURCE:
 			path = getPath();
 			break;
 		case CONTAINER:
 			break;
 		}
 		return resolvePath(path).toPortableString();
 	}
 
 	/**
 	 * Returns the OS path for the given absolute or workspace relative path
 	 */
 	protected IPath resolvePath(IPath path) {
 		if (path != null) {
 			IResource res = null;
 			if (path.getDevice() == null) {
 				// if there is no device specified, find the resource
 				res = getResource(path);
 			}
 			if (res == null) {
 				return path;
 			}
 			URI locationURI = res.getLocationURI();
 			if (locationURI != null) {
 				return new Path(locationURI.getPath());
 			}
 			IPath location = res.getLocation();
 			if (location != null) {
 				return location;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @see Object#equals(Object)
 	 */
 	public boolean equals(Object obj) {
 		if (obj instanceof IRuntimeBuildpathEntry) {
 			IRuntimeBuildpathEntry r = (IRuntimeBuildpathEntry) obj;
 			if (getType() == r.getType()
 					&& getBuildpathProperty() == r.getBuildpathProperty()) {
 				if (getType() == IRuntimeBuildpathEntry.CONTAINER) {
 					String id = getPath().segment(0);
 					BuildpathContainerInitializer initializer = DLTKCore
 							.getBuildpathContainerInitializer(id);
 					IScriptProject scriptProject1 = getScriptProject();
 					IScriptProject scriptProject2 = r.getScriptProject();
 					if (initializer == null || scriptProject1 == null
 							|| scriptProject2 == null) {
 						// containers are equal if their ID is equal by default
 						return getPath().equals(r.getPath());
 					}
 					Object comparisonID1 = initializer.getComparisonID(
 							getPath(), scriptProject1);
 					Object comparisonID2 = initializer.getComparisonID(r
 							.getPath(), scriptProject2);
 					return comparisonID1.equals(comparisonID2);
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Returns whether the given objects are equal, accounting for null
 	 */
 	protected boolean equal(Object one, Object two) {
 		if (one == null) {
 			return two == null;
 		}
 		return one.equals(two);
 	}
 
 	/**
 	 * @see Object#hashCode()
 	 */
 	public int hashCode() {
 		if (getType() == CONTAINER) {
 			return getPath().segment(0).hashCode() + getType();
 		}
 		return getPath().hashCode() + getType();
 	}
 
 	/**
 	 * Creates a new underlying buildpath entry for this runtime buildpath entry
 	 * with the given paths, due to a change in source attachment.
 	 */
 	protected void updateBuildpathEntry(IPath path) {
 		IBuildpathEntry entry = null;
 		IBuildpathEntry original = getBuildpathEntry();
 		switch (getType()) {
 		case ARCHIVE:
 			entry = DLTKCore.newLibraryEntry(path, original.getAccessRules(),
 					original.getExtraAttributes(), original.isExported(),
 					original.isExternal());
 			break;
 		default:
 			return;
 		}
 		setBuildpathEntry(entry);
 	}
 
 	protected boolean isEmpty(String string) {
 		return string == null || string.length() == 0;
 	}
 
 	public String toString() {
 		if (fBuildpathEntry != null) {
 			return fBuildpathEntry.toString();
 		}
 		return super.toString();
 
 	}
 
 	public IScriptProject getScriptProject() {
 		return fProject;
 	}
 
 	/**
 	 * Sets the Script project associated with this buildpath entry.
 	 * 
 	 * @param project
 	 *            Script project
 	 */
 	public void setScriptProject(IScriptProject project) {
 		fProject = project;
 	}
 
 	public String getContainerName() {
 		if (getType() == IRuntimeBuildpathEntry.CONTAINER) {
 			return getPath().segment(0);
 		}
 		return null;
 	}
 }
