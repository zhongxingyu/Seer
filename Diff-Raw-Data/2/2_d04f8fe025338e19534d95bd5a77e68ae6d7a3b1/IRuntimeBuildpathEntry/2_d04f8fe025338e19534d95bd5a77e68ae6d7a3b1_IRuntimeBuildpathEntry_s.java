 /*******************************************************************************
  * Copyright (c) 2000, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.dltk.launching;
 
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IDLTKProject;
 
 
 /**
  * Represents an entry on a runtime buildpath. A runtime buildpath entry
  * may refer to one of the following:
  * <ul>
  * 	<li>A script project (type <code>PROJECT</code>) - a project entry refers
  * 		to all of the scripts in a project.</li>
  * 	<li>An archive (type <code>ARCHIVE</code>) - an archive refers to a jar, zip, or
  * 		folder in the workspace or in the local file system containing script
  * 		files.</li>
  * 	<li>A library (type <code>CONTAINER</code>) - a container refers to buildpath
  * 		container variable which refers to a collection of archives derived
  * 		dynamically, on a per project basis.</li>
  *  <li>A contributed buildpath entry (type <code>OTHER</code>) - a contributed
  *      buildpath entry is an extension contributed by a plug-in. The resolution
  *      of a contributed buildpath entry is client defined. See
  * 		<code>IRuntimeBuildpathEntry2</code>.
  * </ul>
  * <p>
  * Clients may implement this interface for contributed a buildpath entry
  * types (i.e. type <code>OTHER</code>). Note, contributed buildpath entries
  * are new in 3.0, and are only intended to be contributed by the debugger.
  * </p>
 	 *
  * @see org.eclipse.dltk.launching.IRuntimeBuildpathEntry2
  */
 public interface IRuntimeBuildpathEntry {
 	
 	/**
 	 * Type identifier for project entries.
 	 */
 	public static final int PROJECT = 1;
 	
 	/**
 	 * Type identifier for archive entries.
 	 */
 	public static final int ARCHIVE = 2;
 	
 	/**
 	 * Type identifier for source entries.
 	 */
 	public static final int SOURCE = 3;
 	
 	/**
 	 * Type identifier for container entries.
 	 */
 	public static final int CONTAINER = 4;
 	
 	/**
 	 * Type identifier for contributed entries.
 	 *
 	 */
 	public static final int OTHER = 5;	
 
 
 	/**
 	 * Buildpath property identifier for entries that appear on the
 	 * bootstrap path by default.
 	 */
 	public static final int STANDARD_ENTRY = 1;	
 	
 	/**
 	 * Buildpath property identifier for entries that should appear on the
 	 * bootstrap path explicitly.
 	 */
 	public static final int BOOTSTRAP_ENTRY = 2;	
 		
 	/**
 	 * Buildpath property identifier for entries that should appear on the
 	 * user buildpath.
 	 */
 	public static final int USER_ENTRY = 3;	
 
 	/**
 	 * Returns this buildpath entry's type. The type of a runtime buildpath entry is
 	 * identified by one of the following constants:
 	 * <ul>
 	 * <li><code>PROJECT</code></li>
 	 * <li><code>ARCHIVE</code></li>
 	 * <li><code>CONTAINER</code></li>
 	 * <li><code>OTHER</code></li>
 	 * </ul>
 	 * <p>
 	 * Since 3.0, a type of <code>OTHER</code> may be returned.
 	 * </p>
 	 * @return this buildpath entry's type
 	 */
 	public int getType();
 	
 	/**
 	 * Returns a memento for this buildpath entry.
 	 * <p>
 	 * Since 3.0, the memento for a contributed buildpath entry (i.e. of
 	 * type <code>OTHER</code>), must be in the form of an XML document,
 	 * with the following element structure:
 	 * <pre>
 	 * <runtimeBuildpathEntry id="exampleId">
 	 *    <memento
 	 *       key1="value1"
 	 * 		 ...>
 	 *    </memento>
 	 * </runtimeBuildpathEntry>
 	 * </pre>
 	 * The <code>id</code> attribute is the unique identifier of the extension
 	 * that contributed this runtime buildpath entry type, via the extension
 	 * point <code>org.eclipse.dltk.launching.runtimeBuildpathEntries</code>.
 	 * The <code>memento</code> element will be used to initialize a
 	 * restored runtime buildpath entry, via the method
 	 * <code>IRuntimeBuildpathEntry2.initializeFrom(Element memento)</code>. The 
 	 * attributes of the <code>memento</code> element are client defined.
 	 * </p>
 	 * 
 	 * @return a memento for this buildpath entry
 	 * @exception CoreException if an exception occurs generating a memento
 	 */
 	public String getMemento() throws CoreException;
 	
 	/**
 	 * Returns the path associated with this entry, or <code>null</code>
 	 * if none. The format of the
 	 * path returned depends on this entry's type:
 	 * <ul>
 	 * <li><code>PROJECT</code> - a workspace relative path to the associated
 	 * 		project.</li>
 	 * <li><code>ARCHIVE</code> - the absolute path of the associated archive,
 	 * 		which may or may not be in the workspace.</li>
 	 * <li><code>VARIABLE</code> - the path corresponding to the associated
 	 * 		buildpath variable entry.</li>
 	 * <li><code>CONTAINER</code> - the path corresponding to the associated
 	 * 		buildpath container variable entry.</li>
 	 * <li><code>OTHER</code> - the path returned is client defined.</li>
 	 * </ul>
 	 * <p>
 	 * Since 3.0, this method may return <code>null</code>.
 	 * </p>
 	 * @return the path associated with this entry, or <code>null</code>
	 * @see org.eclipse.dltk.core.IClasspathEntry#getPath()
 	 */
 	public IPath getPath();
 	
 	/**
 	 * Returns the first segment of the path associated with this entry, or <code>null</code>
 	 * if this entry is not of type <code>CONTAINER</code>.
 	 * 
 	 * @return the first segment of the path associated with this entry, or <code>null</code>
 	 *  if this entry is not of type <code>CONTAINER</code>
 	 */
 	public String getContainerName();
 		
 	/**
 	 * Returns the resource associated with this entry, or <code>null</code>
 	 * if none. A project, archive, or folder entry may be associated
 	 * with a resource.
 	 * 
 	 * @return the resource associated with this entry, or <code>null</code>
 	 */ 
 	public IResource getResource();
 	
 	/**
 	 * Returns a constant indicating where this entry should appear on the 
 	 * runtime buildpath by default.
 	 * The value returned is one of the following:
 	 * <ul>
 	 * <li><code>STANDARD_CLASSES</code> - a standard entry does not need to appear
 	 * 		on the runtime buildpath</li>
 	 * <li><code>BOOTSTRAP_CLASSES</code> - a bootstrap entry should appear on the
 	 * 		boot path</li>
 	 * <li><code>USER_CLASSES</code> - a user entry should appear on the path
 	 * 		containing user or application classes</li>
 	 * </ul>
 	 * 
 	 * @return where this entry should appear on the runtime buildpath
 	 */
 	public int getBuildpathProperty();
 	
 	/**
 	 * Sets whether this entry should appear on the bootstrap buildpath,
 	 * the user buildpath, or whether this entry is a standard bootstrap entry
 	 * that does not need to appear on the buildpath.
 	 * The location is one of:
 	 * <ul>
 	 * <li><code>STANDARD_CLASSES</code> - a standard entry does not need to appear
 	 * 		on the runtime buildpath</li>
 	 * <li><code>BOOTSTRAP_CLASSES</code> - a bootstrap entry should appear on the
 	 * 		boot path</li>
 	 * <li><code>USER_CLASSES</code> - a user entry should appear on the path
 	 * 		conatining user or application classes</li>
 	 * </ul>
 	 * 
 	 * @param location a classpat property constant
 	 */
 	public void setBuildpathProperty(int location);	
 	
 	/**
 	 * Returns an absolute path in the local file system for this entry,
 	 * or <code>null</code> if none, or if this entry is of type <code>CONTAINER</code>.
 	 * 
 	 * @return an absolute path in the local file system for this entry,
 	 *  or <code>null</code> if none
 	 */
 	public String getLocation();
 		
 	
 	/**
 	 * Returns a buildpath entry equivalent to this runtime buildpath entry,
 	 * or <code>null</code> if none.
 	 * <p>
 	 * This method may return <code>null</code>.
 	 * </p>
 	 * @return a buildpath entry equivalent to this runtime buildpath entry,
 	 *  or <code>null</code>
 	 *
 	 */
 	public IBuildpathEntry getBuildpathEntry();
 	
 	/**
 	 * Returns the Script project associated with this runtime buildpath entry
 	 * or <code>null</code> if none. Runtime buildpath entries of type
 	 * <code>CONTAINER</code> may be associated with a project for the
 	 * purposes of resolving the entries in a container. 
 	 * 
 	 * @return the Script project associated with this runtime buildpath entry
 	 * or <code>null</code> if none
 	 *
 	 */
 	public IDLTKProject getDLTKProject();
 }
