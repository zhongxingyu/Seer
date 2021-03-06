 /*******************************************************************************
  * Copyright (c) 2000, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.core.resources;
 
 import org.eclipse.core.runtime.*;
 import org.eclipse.core.runtime.jobs.ISchedulingRule;
 
 /**
  * The workspace analog of file system files
  * and directories.  There are exactly four <i>types</i> of resource:
  * files, folders, projects and the workspace root.
  * <p>
  * File resources are similar to files in that they
  * hold data directly.  Folder resources are analogous to directories in that they
  * hold other resources but cannot directly hold data.  Project resources
  * group files and folders into reusable clusters.  The workspace root is the
  * top level resource under which all others reside.
  * </p>
  * <p>
  * Features of resources:
  * <ul>
  * <li><code>IResource</code> objects are <i>handles</i> to state maintained
  *		by a workspace.  That is, resource objects do not actually contain data
  *		themselves but rather represent resource state and give it behavior.  Programmers
  *		are free to manipulate handles for resources that do not exist in a workspace 
  *		but must keep in mind that some methods and operations require that an actual 
  *		resource be available.</li>
  * <li>Resources have two different kinds of properties as detailed below.  All 
  *		properties are keyed by qualified names.</li>
  * <ul>
  * <li>Session properties: Session properties live for the lifetime of one execution of
  *		the workspace.  They are not stored on disk.  They can carry arbitrary
  *		object values.  Clients should be aware that these values are kept in memory
  *		at all times and, as such, the values should not be large.</li>
  * <li>Persistent properties: Persistent properties have string values which are stored
  *		on disk across platform sessions.  The value of a persistent property is a 
  * 		string which should be short (i.e., under 2KB). </li>
  * </ul>
  * </li>
  * <li>Resources are identified by type and by their <i>path</i>, which is similar to a file system 
  * 		path.  The name of a resource is the last segment of its path. A resource's parent 
  *		is located by removing the last segment (the resource's name) from the resource's full path.</li>
  * <li>Resources can be local or non-local. A non-local resource is one whose
  * 		contents and properties have not been fetched from a repository.</li>
  * <li><i>Phantom</i> resources represent incoming additions or outgoing deletions
  *		which have yet to be reconciled with a synchronization partner. </li>
  * </ul>
  * </p>
  * <p>
  * This interface is not intended to be implemented by clients.
  * </p>
  * <p>
  * Resources implement the <code>IAdaptable</code> interface;
  * 	extensions are managed by the platform's adapter manager.
  * </p>
  *
  * @see IWorkspace
  * @see Platform#getAdapterManager()
  */
 public interface IResource extends IAdaptable, ISchedulingRule {
 
 	/*====================================================================
 	 * Constants defining resource types:  There are four possible resource types
 	 * and their type constants are in the integer range 1 to 8 as defined below.
 	 *====================================================================*/
 
 	/** 
 	 * Type constant (bit mask value 1) which identifies file resources.
 	 *
 	 * @see IResource#getType()
 	 * @see IFile
 	 */
 	public static final int FILE = 0x1;
 
 	/**
 	 * Type constant (bit mask value 2) which identifies folder resources.
 	 *
 	 * @see IResource#getType()
 	 * @see IFolder
 	 */
 	public static final int FOLDER = 0x2;
 
 	/**
 	 * Type constant (bit mask value 4) which identifies project resources.
 	 *
 	 * @see IResource#getType()
 	 * @see IProject
 	 */
 	public static final int PROJECT = 0x4;
 
 	/**
 	 * Type constant (bit mask value 8) which identifies the root resource.
 	 *
 	 * @see IResource#getType()
 	 * @see IWorkspaceRoot
 	 */
 	public static final int ROOT = 0x8;
 
 	/*====================================================================
 	 * Constants defining the depth of resource tree traversal:
 	 *====================================================================*/
 
 	/**
 	 * Depth constant (value 0) indicating this resource, but not any of its members.
 	 */
 	public static final int DEPTH_ZERO = 0;
 
 	/**
 	 * Depth constant (value 1) indicating this resource and its direct members.
 	 */
 	public static final int DEPTH_ONE = 1;
 
 	/**
 	 * Depth constant (value 2) indicating this resource and its direct and
 	 * indirect members at any depth.
 	 */
 	public static final int DEPTH_INFINITE = 2;
 
 	/*====================================================================
 	 * Constants for update flags for delete, move, copy, etc.:
 	 *====================================================================*/
 
 	/**
 	 * Update flag constant (bit mask value 1) indicating that the operation
 	 * should proceed even if the resource is out of sync with the local file
 	 * system.
 	 * 
 	 * @since 2.0
 	 */
 	public static final int FORCE = 0x1;
 
 	/**
 	 * Update flag constant (bit mask value 2) indicating that the operation
 	 * should maintain local history by taking snapshots of the contents of
 	 * files just before being overwritten or deleted.
 	 * 
 	 * @see IFile#getHistory(IProgressMonitor)
 	 * @since 2.0
 	 */
 	public static final int KEEP_HISTORY = 0x2;
 
 	/**
 	 * Update flag constant (bit mask value 4) indicating that the operation
 	 * should delete the files and folders of a project.
 	 * <p>
 	 * Deleting a project that is open ordinarily deletes all its files and folders, 
 	 * whereas deleting a project that is closed retains its files and folders.
 	 * Specifying <code>ALWAYS_DELETE_PROJECT_CONTENT</code> indicates that the contents
 	 * of a project are to be deleted regardless of whether the project is open or closed
 	 * at the time; specifying <code>NEVER_DELETE_PROJECT_CONTENT</code> indicates that
 	 * the contents of a project are to be retained regardless of whether the project
 	 * is open or closed at the time.
 	 * </p>
 	 * 
 	 * @see #NEVER_DELETE_PROJECT_CONTENT
 	 * @since 2.0
 	 */
 	public static final int ALWAYS_DELETE_PROJECT_CONTENT = 0x4;
 
 	/**
 	 * Update flag constant (bit mask value 8) indicating that the operation
 	 * should preserve the files and folders of a project.
 	 * <p>
 	 * Deleting a project that is open ordinarily deletes all its files and folders, 
 	 * whereas deleting a project that is closed retains its files and folders.
 	 * Specifying <code>ALWAYS_DELETE_PROJECT_CONTENT</code> indicates that the contents
 	 * of a project are to be deleted regardless of whether the project is open or closed
 	 * at the time; specifying <code>NEVER_DELETE_PROJECT_CONTENT</code> indicates that
 	 * the contents of a project are to be retained regardless of whether the project
 	 * is open or closed at the time.
 	 * </p>
 	 * 
 	 * @see #ALWAYS_DELETE_PROJECT_CONTENT
 	 * @since 2.0
 	 */
 	public static final int NEVER_DELETE_PROJECT_CONTENT = 0x8;
 
 	/**
 	 * Update flag constant (bit mask value 16) indicating that the link creation
 	 * should proceed even if the local file system file or directory is missing.
 	 * 
 	 * @see IFolder#createLink(IPath, int, IProgressMonitor)
 	 * @see IFile#createLink(IPath, int, IProgressMonitor)
 	 * @since 2.1
 	 */
 	public static final int ALLOW_MISSING_LOCAL = 0x10;
 
 	/**
 	 * Update flag constant (bit mask value 32) indicating that a copy or move
 	 * operation should only copy the link, rather than copy the underlying
 	 * contents of the linked resource.
 	 *
 	 * @see #copy(IPath, int, IProgressMonitor)
 	 * @see #move(IPath, int, IProgressMonitor)
 	 * @since 2.1
 	 */
 	public static final int SHALLOW = 0x20;
 
 	/**
 	 * Update flag constant (bit mask value 64) indicating that setting the
 	 * project description should not attempt to configure and deconfigure
 	 * natures.
 	 *
 	 * @see IProject#setDescription(IProjectDescription, int, IProgressMonitor)
 	 * @since 3.0
 	 */
 	public static final int AVOID_NATURE_CONFIG= 0x40;
 
 	/*====================================================================
 	 * Other constants:
 	 *====================================================================*/
 
 	/** 
 	 * Modification stamp constant (value -1) indicating no modification stamp is
 	 * available.
 	 *
 	 * @see #getModificationStamp()
 	 */
 	public static final int NULL_STAMP = -1;
 
 	/** 
 	 * General purpose zero-valued bit mask constant. Useful whenever you need to
 	 * supply a bit mask with no bits set.
 	 * <p>
 	 * Example usage:
 	 * <code>
 	 * <pre>
 	 *    delete(IResource.NONE, null)
 	 * </pre>
 	 * </code>
 	 * </p>
 	 *
 	 * @since 2.0
 	 */
 	public static final int NONE = 0;
 
 	/**
 	 * Accepts the given visitor for an optimized traversal. 
 	 * The visitor's <code>visit</code> method is called, and is provided with a
 	 * proxy to this resource.  The proxy is a transient object that can be queried
 	 * very quickly for information about the resource. If the actual resource
 	 * handle is needed, it can be obtained from the proxy. Requesting the resource
 	 * handle, or the full path of the resource, will degrade performance of the
 	 * visit.
 	 * <p>
 	 * The entire subtree under the given resource is traversed to infinite depth,
 	 * unless the visitor ignores a subtree by returning <code>false</code> from its
 	 * <code>visit</code> method.
 	 * </p>
 	 *  <p>No  guarantees are made about the behavior of this method if resources
 	 * are deleted or added during the traversal of this resource hierarchy.  If
 	 * resources are deleted during the traversal, they may still be passed to the
 	 * visitor; if resources are created, they may not be passed to the visitor.  If
 	 * resources other than the one being visited are modified during the traversal,
 	 * the resource proxy may contain stale information when that resource is
 	 * visited.
 	 * </p>
 	 <p>
 	 * If the <code>INCLUDE_PHANTOMS</code> flag is not specified in the member 
 	 * flags (recommended), only member resources that exist will be visited.
 	 * If the <code>INCLUDE_PHANTOMS</code> flag is specified, the visit will
 	 * also include any phantom member resource that the workspace is keeping track of.
 	 * </p>
 	 * <p>
 	 * If the <code>INCLUDE_TEAM_PRIVATE_MEMBERS</code> flag is not specified
 	 * (recommended), team private members will not be visited. If the 
 	 * <code>INCLUDE_TEAM_PRIVATE_MEMBERS</code> flag is specified in the member
 	 * flags, team private member resources are visited as well.
 	 * </p>
 	 *
 	 * @param visitor the visitor
 	 * @param memberFlags bit-wise or of member flag constants
 	 *   (<code>IContainer.INCLUDE_PHANTOMS</code> and <code>INCLUDE_TEAM_PRIVATE_MEMBERS</code>)
 	 *   indicating which members are of interest
 	 * @exception CoreException if this request fails. Reasons include:
 	 * <ul>
 	 * <li> the <code>INCLUDE_PHANTOMS</code> flag is not specified and
 	 *     this resource does not exist.</li>
 	 * <li> the <code>INCLUDE_PHANTOMS</code> flag is not specified and
 	 *     this resource is a project that is not open.</li>
 	 * <li> The visitor failed with this exception.</li>
 	 * </ul>
 	 * @see IContainer#INCLUDE_PHANTOMS
 	 * @see IContainer#INCLUDE_TEAM_PRIVATE_MEMBERS
 	 * @see IResource#isPhantom()
 	 * @see IResource#isTeamPrivateMember()
 	 * @see IResourceProxyVisitor#visit(IResourceProxy)
 	 * @since 2.1
 	 */
 	public void accept(final IResourceProxyVisitor visitor, int memberFlags) throws CoreException;
 
 	/**
 	 * Accepts the given visitor.
 	 * The visitor's <code>visit</code> method is called with this
 	 * resource. If the visitor returns <code>true</code>, this method
 	 * visits this resource's members.
 	 * <p>
 	 * This is a convenience method, fully equivalent to 
 	 * <code>accept(visitor,IResource.DEPTH_INFINITE, IResource.NONE)</code>.
 	 * </p>
 	 *
 	 * @param visitor the visitor
 	 * @exception CoreException if this method fails. Reasons include:
 	 * <ul>
 	 * <li> This resource does not exist.</li>
 	 * <li> The visitor failed with this exception.</li>
 	 * </ul>
 	 * @see IResourceVisitor#visit(IResource)
 	 * @see #accept(IResourceVisitor,int,int)
 	 */
 	public void accept(IResourceVisitor visitor) throws CoreException;
 
 	/**
 	 * Accepts the given visitor.
 	 * The visitor's <code>visit</code> method is called with this
 	 * resource. If the visitor returns <code>false</code>, 
 	 * this resource's members are not visited.
 	 * <p>
 	 * The subtree under the given resource is traversed to the supplied depth.
 	 * </p>
 	 * <p>
 	 * This is a convenience method, fully equivalent to:
 	 * <pre>
 	 *   accept(visitor, depth, includePhantoms ? INCLUDE_PHANTOMS : IResource.NONE);
 	 * </pre>
 	 * </p>
 	 *
 	 * @param visitor the visitor
 	 * @param depth the depth to which members of this resource should be
 	 *		visited.  One of <code>DEPTH_ZERO</code>, <code>DEPTH_ONE</code>,
 	 *		or <code>DEPTH_INFINITE</code>.
 	 * @param includePhantoms <code>true</code> if phantom resources are
 	 *   of interest; <code>false</code> if phantom resources are not of
 	 *   interest.
 	 * @exception CoreException if this request fails. Reasons include:
 	 * <ul>
 	 * <li> <code>includePhantoms</code> is <code>false</code> and
 	 *     this resource does not exist.</li>
 	 * <li> <code>includePhantoms</code> is <code>true</code> and
 	 *     this resource does not exist and is not a phantom.</li>
 	 * <li> The visitor failed with this exception.</li>
 	 * </ul>
 	 * @see IResource#isPhantom()
 	 * @see IResourceVisitor#visit(IResource)
 	 * @see IResource#DEPTH_ZERO
 	 * @see IResource#DEPTH_ONE
 	 * @see IResource#DEPTH_INFINITE
 	 * @see IResource#accept(IResourceVisitor,int,int)
 	 */
 	public void accept(IResourceVisitor visitor, int depth, boolean includePhantoms) throws CoreException;
 
 	/**
 	 * Accepts the given visitor.
 	 * The visitor's <code>visit</code> method is called with this
 	 * resource. If the visitor returns <code>false</code>, 
 	 * this resource's members are not visited.
 	 * <p>
 	 * The subtree under the given resource is traversed to the supplied depth.
 	 * </p>
 	 * <p>
 	 * No guarantees are made about the behavior of this method if resources are
 	 * deleted or added during the traversal of this resource hierarchy.  If
 	 * resources are deleted during the traversal, they may still be passed to the
 	 * visitor; if resources are created, they may not be passed to the visitor.
 	 * </p>
 	 * <p>
 	 * If the <code>INCLUDE_PHANTOMS</code> flag is not specified in the member 
 	 * flags (recommended), only member resources that exists will be visited.
 	 * If the <code>INCLUDE_PHANTOMS</code> flag is specified, the visit will
 	 * also include any phantom member resource that the workspace is keeping track of.
 	 * </p>
 	 * <p>
 	 * If the <code>INCLUDE_TEAM_PRIVATE_MEMBERS</code> flag is not specified
 	 * (recommended), team private members will not be visited. If the 
 	 * <code>INCLUDE_TEAM_PRIVATE_MEMBERS</code> flag is specified in the member
 	 * flags, team private member resources are visited as well.
 	 * </p>
 	 *
 	 * @param visitor the visitor
 	 * @param depth the depth to which members of this resource should be
 	 *		visited.  One of <code>DEPTH_ZERO</code>, <code>DEPTH_ONE</code>,
 	 *		or <code>DEPTH_INFINITE</code>.
 	 * @param memberFlags bit-wise or of member flag constants
 	 *   (<code>IContainer.INCLUDE_PHANTOMS</code> and <code>INCLUDE_TEAM_PRIVATE_MEMBERS</code>)
 	 *   indicating which members are of interest
 	 * @exception CoreException if this request fails. Reasons include:
 	 * <ul>
 	 * <li> the <code>INCLUDE_PHANTOMS</code> flag is not specified and
 	 *     this resource does not exist.</li>
 	 * <li> the <code>INCLUDE_PHANTOMS</code> flag is not specified and
 	 *     this resource is a project that is not open.</li>
 	 * <li> The visitor failed with this exception.</li>
 	 * </ul>
 	 * @see IContainer#INCLUDE_PHANTOMS
 	 * @see IContainer#INCLUDE_TEAM_PRIVATE_MEMBERS
 	 * @see IResource#isPhantom()
 	 * @see IResource#isTeamPrivateMember()
 	 * @see IResource#DEPTH_ZERO
 	 * @see IResource#DEPTH_ONE
 	 * @see IResource#DEPTH_INFINITE
 	 * @see IResourceVisitor#visit(IResource)
 	 * @since 2.0
 	 */
 	public void accept(IResourceVisitor visitor, int depth, int memberFlags) throws CoreException;
 
 	/**
 	 * Removes the local history of this resource and its descendants.
 	 * <p>
 	 * This operation is long-running; progress and cancellation are provided
 	 * by the given progress monitor. 
 	 * </p>
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting and cancellation are not desired
 	 */
 	public void clearHistory(IProgressMonitor monitor) throws CoreException;
 
 	/**
 	 * Makes a copy of this resource at the given path. 
 	 * <p>
 	 * This is a convenience method, fully equivalent to:
 	 * <pre>
 	 *   copy(destination, (force ? FORCE : IResource.NONE), monitor);
 	 * </pre>
 	 * </p>
 	 * <p> 
 	 * This operation changes resources; these changes will be reported
 	 * in a subsequent resource change event that will include 
 	 * an indication that the resource copy has been added to its new parent.
 	 * </p>
 	 * <p>
 	 * This operation is long-running; progress and cancellation are provided
 	 * by the given progress monitor. 
 	 * </p>
 	 *
 	 * @param destination the destination path 
 	 * @param force a flag controlling whether resources that are not
 	 *    in sync with the local file system will be tolerated
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting is not desired
 	 * @exception CoreException if this resource could not be copied. Reasons include:
 	 * <ul>
 	 * <li> This resource does not exist.</li>
 	 * <li> This resource or one of its descendents is not local.</li>
 	 * <li> The source or destination is the workspace root.</li>
 	 * <li> The source is a project but the destination is not.</li>
 	 * <li> The destination is a project but the source is not.</li>
 	 * <li> The resource corresponding to the parent destination path does not exist.</li>
 	 * <li> The resource corresponding to the parent destination path is a closed project.</li>
 	 * <li> A resource at destination path does exist.</li>
 	 * <li> This resource or one of its descendents is out of sync with the local file
 	 *      system and <code>force</code> is <code>false</code>.</li>
 	 * <li> The workspace and the local file system are out of sync
 	 *      at the destination resource or one of its descendents.</li>
 	 * <li> The source resource is a file and the destination path specifies a project.</li>
 	 * <li> Resource changes are disallowed during certain types of resource change 
 	 *       event notification. See <code>IResourceChangeEvent</code> for more details.</li>
 	 * </ul>
 	 * @exception OperationCanceledException if the operation is canceled. 
 	 * Cancelation can occur even if no progress monitor is provided.
 	 */
 	public void copy(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException;
 
 	/**
 	 * Makes a copy of this resource at the given path. The resource's
 	 * descendents are copied as well.  The path of this resource must not be a
 	 * prefix of the destination path. The workspace root may not be the source or
 	 * destination location of a copy operation, and a project can only be copied to
 	 * another project. After successful completion, corresponding new resources
 	 * will exist at the given path; their contents and properties will be copies of
 	 * the originals. The original resources are not affected.
 	 * <p>
 	 * The supplied path may be absolute or relative.  Absolute paths fully specify
 	 * the new location for the resource, including its project. Relative paths are
 	 * considered to be relative to the container of the resource being copied. A
 	 * trailing separator is ignored.
 	 * </p>
 	 * <p>
 	 * Calling this method with a one segment absolute destination path is
 	 * equivalent to calling:
 	 * <pre>
 	 *   copy(workspace.newProjectDescription(folder.getName()),updateFlags,monitor);
 	 * </pre>
 	 * </p>
 	 * <p> When a resource is copied, its persistent properties are copied with it.
 	 * Session properties and markers are not copied.
 	 * </p>
 	 * <p>
 	 * The <code>FORCE</code> update flag controls how this method deals with cases
 	 * where the workspace is not completely in sync with the local file system. If
 	 * <code>FORCE</code> is not specified, the method will only attempt to copy
 	 * resources that are in sync with the corresponding files and directories in
 	 * the local file system; it will fail if it encounters a resource that is out
 	 * of sync with the file system. However, if <code>FORCE</code> is specified,
 	 * the method copies all corresponding files and directories from the local file
 	 * system, including ones that have been recently updated or created. Note that
 	 * in both settings of the <code>FORCE</code> flag, the operation fails if the
 	 * newly created resources in the workspace would be out of sync with the local
 	 * file system; this ensures files in the file system cannot be accidentally
 	 * overwritten.
 	 * </p>
 	 * <p>
 	 * The <code>SHALLOW</code> update flag controls how this method deals with linked
 	 * resources.  If <code>SHALLOW</code> is not specified, then the underlying
 	 * contents of the linked resource will always be copied in the file system.  In
 	 * this case, the destination of the copy will never be a linked resource or
 	 * contain any linked resources.  If <code>SHALLOW</code> is specified when a
 	 * linked resource is copied into another project, a new linked resource is
 	 * created in the destination project that points to the same file system
 	 * location.  When a project containing linked resources is copied, the new
 	 * project will contain the same linked resources pointing to the same file
 	 * system locations.  For both of these shallow cases, no files on disk under
 	 * the linked resource are actually copied.  With the <code>SHALLOW</code> flag,
 	 * copying of linked resources into anything other than a project is not
 	 * permitted.  The <code>SHALLOW</code> update flag is ignored when copying non-
 	 * linked resources.
 	 * </p>
 	 * <p> 
 	 * Update flags other than <code>FORCE</code> and <code>SHALLOW</code> are ignored.
 	 * </p>
 	 * <p> 
 	 * This operation changes resources; these changes will be reported in a
 	 * subsequent resource change event that will include an indication that the
 	 * resource copy has been added to its new parent.
 	 * </p>
 	 * <p>
 	 * An attempt will be made to copy the local history for this resource and its children,
 	 * to the destination. Since local history existence is a safety-net mechanism, failure
 	 * of this action will not result in automatic failure of the copy operation.
 	 * </p>
 	 * <p>
 	 * This operation is long-running; progress and cancellation are provided
 	 * by the given progress monitor. 
 	 * </p>
 	 *
 	 * @param destination the destination path 
 	 * @param updateFlags bit-wise or of update flag constants
 	 *   (<code>FORCE</code> and <code>SHALLOW</code>)
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting is not desired
 	 * @exception CoreException if this resource could not be copied. Reasons include:
 	 * <ul>
 	 * <li> This resource does not exist.</li>
 	 * <li> This resource or one of its descendents is not local.</li>
 	 * <li> The source or destination is the workspace root.</li>
 	 * <li> The source is a project but the destination is not.</li>
 	 * <li> The destination is a project but the source is not.</li>
 	 * <li> The resource corresponding to the parent destination path does not exist.</li>
 	 * <li> The resource corresponding to the parent destination path is a closed project.</li>
 	 * <li> The source is a linked resource, but the destination is not a project,
 	 *      and <code>SHALLOW</code> is specified.</li>
 	 * <li> A resource at destination path does exist.</li>
 	 * <li> This resource or one of its descendents is out of sync with the local file
 	 *      system and <code>FORCE</code> is not specified.</li>
 	 * <li> The workspace and the local file system are out of sync
 	 *      at the destination resource or one of its descendents.</li>
 	 * <li> The source resource is a file and the destination path specifies a project.</li>
 	 * <li> The source is a linked resource, and the destination path does not
 	 * 	specify a project.</li>
 	 * <li> The location of the source resource on disk is the same or a prefix of
 	 * the location of the destination resource on disk.</li>
 	 * <li> Resource changes are disallowed during certain types of resource change 
 	 *       event notification. See <code>IResourceChangeEvent</code> for more details.</li>
 	 * </ul>
 	 * @exception OperationCanceledException if the operation is canceled. 
 	 * Cancelation can occur even if no progress monitor is provided.
 	 * @see #FORCE
 	 * @see #SHALLOW
 	 * @see IResourceRuleFactory#copyRule(IResource, IResource)
 	 * @since 2.0
 	 */
 	public void copy(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException;
 
 	/**
 	 * Makes a copy of this project using the given project description.
 	 * <p>
 	 * This is a convenience method, fully equivalent to:
 	 * <pre>
 	 *   copy(description, (force ? FORCE : IResource.NONE), monitor);
 	 * </pre>
 	 * </p>
 	 * <p> 
 	 * This operation changes resources; these changes will be reported
 	 * in a subsequent resource change event that will include 
 	 * an indication that the resource copy has been added to its new parent.
 	 * </p>
 	 * <p>
 	 * This operation is long-running; progress and cancellation are provided
 	 * by the given progress monitor. 
 	 * </p>
 	 *
 	 * @param description the destination project description
 	 * @param force a flag controlling whether resources that are not
 	 *    in sync with the local file system will be tolerated
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting is not desired
 	 * @exception CoreException if this resource could not be copied. Reasons include:
 	 * <ul>
 	 * <li> This resource does not exist.</li>
 	 * <li> This resource or one of its descendents is not local.</li>
 	 * <li> This resource is not a project.</li>
 	 * <li> The project described by the given description already exists.</li>
 	 * <li> This resource or one of its descendents is out of sync with the local file
 	 *      system and <code>force</code> is <code>false</code>.</li>
 	 * <li> The workspace and the local file system are out of sync
 	 *      at the destination resource or one of its descendents.</li>
 	 * <li> Resource changes are disallowed during certain types of resource change 
 	 *       event notification. See <code>IResourceChangeEvent</code> for more details.</li>
 	 * </ul>
 	 * @exception OperationCanceledException if the operation is canceled. 
 	 * Cancelation can occur even if no progress monitor is provided.
 	 */
 	public void copy(IProjectDescription description, boolean force, IProgressMonitor monitor) throws CoreException;
 
 	/**
 	 * Makes a copy of this project using the given project description. 
 	 * The project's descendents are copied as well. The description specifies the
 	 * name, location and attributes of the new project. After successful
 	 * completion, corresponding new resources will exist at the given path; their
 	 * contents and properties will be copies of the originals. The original
 	 * resources are not affected.
 	 * <p>
 	 * When a resource is copied, its persistent properties are copied with it.
 	 * Session properties and markers are not copied.
 	 * </p>
 	 * <p> The <code>FORCE</code> update flag controls how this method deals with
 	 * cases where the workspace is not completely in sync with the local file
 	 * system. If <code>FORCE</code> is not specified, the method will only attempt
 	 * to copy resources that are in sync with the corresponding files and
 	 * directories in the local file system; it will fail if it encounters a
 	 * resource that is out of sync with the file system. However, if
 	 * <code>FORCE</code> is specified, the method copies all corresponding files
 	 * and directories from the local file system, including ones that have been
 	 * recently updated or created. Note that in both settings of the
 	 * <code>FORCE</code> flag, the operation fails if the newly created resources
 	 * in the workspace would be out of sync with the local file system; this
 	 * ensures files in the file system cannot be accidentally overwritten.
 	 * </p>
 	 * <p>  
 	 * The <code>SHALLOW</code> update flag controls how this method deals with
 	 * linked resources.  If <code>SHALLOW</code> is not specified, then the
 	 * underlying contents of any linked resources in the project will always be
 	 * copied in the file system.  In this case, the destination of the copy will
 	 * never contain any linked resources.  If <code>SHALLOW</code> is specified
 	 * when a project containing linked resources is copied, new linked resources
 	 * are created in the destination project that point to the same file system
 	 * locations.  In this case, no files on disk under linked resources are
 	 * actually copied. The <code>SHALLOW</code> update flag is ignored when copying
 	 * non- linked resources.
 	 * </p>
 	 * <p>
 	 * Update flags other than <code>FORCE</code> or <code>SHALLOW</code> are ignored.
 	 * </p>
 	 * <p>
 	 * An attempt will be made to copy the local history for this resource and its children,
 	 * to the destination. Since local history existence is a safety-net mechanism, failure
 	 * of this action will not result in automatic failure of the copy operation.
 	 * </p>
 	 * <p> This operation changes resources; these changes will be reported in a
 	 * subsequent resource change event that will include an indication that the
 	 * resource copy has been added to its new parent.
 	 * </p>
 	 * <p>
 	 * This operation is long-running; progress and cancellation are provided
 	 * by the given progress monitor. 
 	 * </p>
 	 *
 	 * @param description the destination project description
 	 * @param updateFlags bit-wise or of update flag constants
 	 *   (<code>FORCE</code> and <code>SHALLOW</code>)
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting is not desired
 	 * @exception CoreException if this resource could not be copied. Reasons include:
 	 * <ul>
 	 * <li> This resource does not exist.</li>
 	 * <li> This resource or one of its descendents is not local.</li>
 	 * <li> This resource is not a project.</li>
 	 * <li> The project described by the given description already exists.</li>
 	 * <li> This resource or one of its descendents is out of sync with the local file
 	 *      system and <code>FORCE</code> is not specified.</li>
 	 * <li> The workspace and the local file system are out of sync
 	 *      at the destination resource or one of its descendents.</li>
 	 * <li> Resource changes are disallowed during certain types of resource change 
 	 *       event notification. See <code>IResourceChangeEvent</code> for more details.</li>
 	 * </ul>
 	 * @exception OperationCanceledException if the operation is canceled. 
 	 * Cancelation can occur even if no progress monitor is provided.
 	 * @see #FORCE
 	 * @see #SHALLOW
 	 * @see IResourceRuleFactory#copyRule(IResource, IResource)
 	 * @since 2.0
 	 */
 	public void copy(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException;
 
 	/**
 	 * Creates and returns the marker with the specified type on this resource.
 	 * Marker type ids are the id of an extension installed in the
 	 * <code>org.eclipse.core.resources.markers</code> extension
 	 * point. The specified type string must not be <code>null</code>.
 	 *
 	 * @param type the type of the marker to create
 	 * @return the handle of the new marker
 	 * @exception CoreException if this method fails. Reasons include:
 	 * <ul>
 	 * <li> This resource does not exist.</li>
 	 * <li> This resource is a project that is not open.</li>
 	 * </ul>
 	 * @see IResourceRuleFactory#markerRule(IResource)
 	 */
 	public IMarker createMarker(String type) throws CoreException;
 
 	/**
 	 * Deletes this resource from the workspace.
 	 * <p>
 	 * This is a convenience method, fully equivalent to:
 	 * <pre>
 	 *   delete(force ? FORCE : IResource.NONE, monitor);
 	 * </pre>
 	 * </p>
 	 * <p>
 	 * This method changes resources; these changes will be reported
 	 * in a subsequent resource change event.
 	 * </p>
 	 * <p>
 	 * This method is long-running; progress and cancellation are provided
 	 * by the given progress monitor. 
 	 * </p>
 	 * 
 	 * @param force a flag controlling whether resources that are not
 	 *    in sync with the local file system will be tolerated
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting is not desired
 	 * @exception CoreException if this method fails. Reasons include:
 	 * <ul>
 	 * <li> This resource could not be deleted for some reason.</li>
 	 * <li> This resource or one of its descendents is out of sync with the local file system
 	 *      and <code>force</code> is <code>false</code>.</li>
 	 * <li> Resource changes are disallowed during certain types of resource change 
 	 *       event notification. See <code>IResourceChangeEvent</code> for more details.</li>
 	 * </ul>
 	 * @exception OperationCanceledException if the operation is canceled. 
 	 * Cancelation can occur even if no progress monitor is provided.
 	 *
 	 * @see IResource#delete(int,IProgressMonitor)
 	 */
 	public void delete(boolean force, IProgressMonitor monitor) throws CoreException;
 
 	/**
 	 * Deletes this resource from the workspace.
 	 * Deletion applies recursively to all members of this resource in a "best-
 	 * effort" fashion.  That is, all resources which can be deleted are deleted.
 	 * Resources which could not be deleted are noted in a thrown exception. The
 	 * method does not fail if resources do not exist; it fails only if resources
 	 * could not be deleted.
 	 * <p>
 	 * Deleting a non-linked resource also deletes its contents from the local file
 	 * system. In the case of a file or folder resource, the corresponding file or
 	 * directory in the local file system is deleted. Deleting an open project
 	 * recursively deletes its members; deleting a closed project just gets rid of
 	 * the project itself (closed projects have no members); files in the project's
 	 * local content area are retained; referenced projects are unaffected.
 	 * </p>
 	 * <p>
 	 * Deleting a linked resource does not delete its contents from the file system,
 	 * it just removes that resource and its children from the workspace.  Deleting
 	 * children of linked resources does remove the contents from the file system.
 	 * </p>
 	 * <p>
 	 * Deleting a resource also deletes its session and persistent properties and
 	 * markers.
 	 * </p>
 	 * <p>
 	 * Deleting a non-project resource which has sync information converts the
 	 * resource to a phantom and retains the sync information for future use.
 	 * </p>
 	 * <p>
 	 * Deleting the workspace root resource recursively deletes all projects,
 	 * and removes all markers, properties, sync info and other data related to the
 	 * workspace root; the root resource itself is not deleted, however.
 	 * </p>
 	 * <p>
 	 * This method changes resources; these changes will be reported
 	 * in a subsequent resource change event.
 	 * </p>
 	 * <p>
 	 * This method is long-running; progress and cancellation are provided
 	 * by the given progress monitor. 
 	 * </p>
 	 * <p>
 	 * The <code>FORCE</code> update flag controls how this method deals with
 	 * cases where the workspace is not completely in sync with the local
 	 * file system. If <code>FORCE</code> is not specified, the method will only
 	 * attempt to delete files and directories in the local file system that
 	 * correspond to, and are in sync with, resources in the workspace; it will fail
 	 * if it encounters a file or directory in the file system that is out of sync
 	 * with  the workspace.  This option ensures there is no unintended data loss;
 	 * it is the recommended setting. However, if <code>FORCE</code> is specified,
 	 * the method will ruthlessly attempt to delete corresponding files and
 	 * directories in the local file system, including ones that have been recently
 	 * updated or created.
 	 * </p>
 	 * <p>
 	 * The <code>KEEP_HISTORY</code> update flag controls whether or not files that
 	 * are about to be deleted from the local file system have their current
 	 * contents saved in the workspace's local history. The local history mechanism
 	 * serves as a safety net to help the user recover from mistakes that might
 	 * otherwise result in data loss. Specifying <code>KEEP_HISTORY</code> is
 	 * recommended except in circumstances where past states of the files are of no
 	 * conceivable interest to the user. Note that local history is maintained
 	 * with each individual project, and gets discarded when a project is deleted
 	 * from the workspace. Hence <code>KEEP_HISTORY</code> is only really applicable
 	 * when deleting files and folders, but not projects.
 	 * </p>
 	 * <p>
 	 * The <code>ALWAYS_DELETE_PROJECT_CONTENTS</code> update flag controls how
 	 * project deletions are handled. If <code>ALWAYS_DELETE_PROJECT_CONTENTS</code>
 	 * is specified, then the files and folders in a project's local content area
 	 * are deleted, regardless of whether the project is open or closed;
 	 * <code>FORCE</code> is assumed regardless of whether it is specified. If
 	 * <code>NEVER_DELETE_PROJECT_CONTENTS</code> is specified, then the files and
 	 * folders in a project's local content area are retained, regardless of whether
 	 * the project is open or closed; the <code>FORCE</code> flag is ignored. If
 	 * neither of these flags is specified, files and folders in a project's local
 	 * content area from open projects (subject to the <code>FORCE</code> flag), but
 	 * never from closed projects.
 	 * </p>
 	 * 
 	 * @param updateFlags bit-wise or of update flag constants (
 	 *   <code>FORCE</code>, <code>KEEP_HISTORY</code>,
 	 *   <code>ALWAYS_DELETE_PROJECT_CONTENTS</code>,
 	 *   and <code>NEVER_DELETE_PROJECT_CONTENTS</code>)
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting is not desired
 	 * @exception CoreException if this method fails. Reasons include:
 	 * <ul>
 	 * <li> This resource could not be deleted for some reason.</li>
 	 * <li> This resource or one of its descendents is out of sync with the local file system
 	 *      and <code>FORCE</code> is not specified.</li>
 	 * <li> Resource changes are disallowed during certain types of resource change 
 	 *       event notification. See <code>IResourceChangeEvent</code> for more details.</li>
 	 * </ul>
 	 * @exception OperationCanceledException if the operation is canceled. 
 	 * Cancelation can occur even if no progress monitor is provided.
 	 * @see IFile#delete(boolean, boolean, IProgressMonitor)
 	 * @see IFolder#delete(boolean, boolean, IProgressMonitor)
 	 * @see #FORCE
 	 * @see #KEEP_HISTORY
 	 * @see #ALWAYS_DELETE_PROJECT_CONTENT
 	 * @see #NEVER_DELETE_PROJECT_CONTENT
 	 * @see IResourceRuleFactory#deleteRule(IResource)
 	 * @since 2.0
 	 */
 	public void delete(int updateFlags, IProgressMonitor monitor) throws CoreException;
 
 	/**
 	 * Deletes all markers on this resource of the given type, and, 
 	 * optionally, deletes such markers from its children.  If <code>includeSubtypes</code>
 	 * is <code>false</code>, only markers whose type exactly matches 
 	 * the given type are deleted.  
 	 * <p>
 	 * This method changes resources; these changes will be reported
 	 * in a subsequent resource change event.
 	 * </p>
 	 *
 	 * @param type the type of marker to consider, or <code>null</code> to indicate all types
 	 * @param includeSubtypes whether or not to consider subtypes of the given type
 	 * @param depth how far to recurse (see <code>IResource.DEPTH_* </code>)
 	 * @exception CoreException if this method fails. Reasons include:
 	 * <ul>
 	 * <li> This resource does not exist.</li>
 	 * <li> This resource is a project that is not open.</li>
 	 * <li> Resource changes are disallowed during certain types of resource change 
 	 *       event notification. See <code>IResourceChangeEvent</code> for more details.</li>
 	 * </ul>
 	 * @see IResource#DEPTH_ZERO
 	 * @see IResource#DEPTH_ONE
 	 * @see IResource#DEPTH_INFINITE
 	 * @see IResourceRuleFactory#markerRule(IResource)
 	 */
 	public void deleteMarkers(String type, boolean includeSubtypes, int depth) throws CoreException;
 
 	/**
 	 * Compares two objects for equality;
 	 * for resources, equality is defined in terms of their handles:
 	 * same resource type, equal full paths, and identical workspaces. 
 	 * Resources are not equal to objects other than resources.
 	 *
 	 * @param other the other object
 	 * @return an indication of whether the objects are equals
 	 * @see #getType()
 	 * @see #getFullPath()
 	 * @see #getWorkspace()
 	 */
 	public boolean equals(Object other);
 
 	/**
 	 * Returns whether this resource exists in the workspace.
 	 * <p>
 	 * <code>IResource</code> objects are lightweight handle objects
 	 * used to access resources in the workspace. However, having a
 	 * handle object does not necessarily mean the workspace really 
 	 * has such a resource. When the workspace does have a genuine
 	 * resource of a matching type, the resource is said to 
 	 * <em>exist</em>, and this method returns <code>true</code>; 
 	 * in all other cases, this method returns <code>false</code>.
 	 * In particular, it returns <code>false</code> if the workspace
 	 * has no resource at that path, or if it has a resource at that
 	 * path with a type different from the type of this resource handle.
 	 * </p>
 	 * <p>
 	 * Note that no resources ever exist under a project 
 	 * that is closed; opening a project may bring some
 	 * resources into existence.
 	 * </p>
 	 * <p>
 	 * The name and path of a resource handle may be invalid.
 	 * However, validation checks are done automatically as a 
 	 * resource is created; this means that any resource that exists
 	 * can be safely assumed to have a valid name and path.
 	 * </p>
 	 *
 	 * @return <code>true</code> if the resource exists, otherwise
 	 *    <code>false</code>
 	 */
 	public boolean exists();
 
 	/**
 	 * Returns the marker with the specified id on this resource,
 	 * Returns <code>null</code> if there is no matching marker.
 	 *
 	 * @param id the id of the marker to find
 	 * @return a marker or <code>null</code>
 	 * @exception CoreException if this method fails. Reasons include:
 	 * <ul>
 	 * <li> This resource does not exist.</li>
 	 * <li> This resource is a project that is not open.</li>
 	 * </ul>
 	 */
 	public IMarker findMarker(long id) throws CoreException;
 
 	/**
 	 * Returns all markers of the specified type on this resource,
 	 * and, optionally, on its children. If <code>includeSubtypes</code>
 	 * is <code>false</code>, only markers whose type exactly matches 
 	 * the given type are returned.  Returns an empty array if there 
 	 * are no matching markers.
 	 *
 	 * @param type the type of marker to consider, or <code>null</code> to indicate all types
 	 * @param includeSubtypes whether or not to consider subtypes of the given type
 	 * @param depth how far to recurse (see <code>IResource.DEPTH_* </code>)
 	 * @return an array of markers
 	 * @exception CoreException if this method fails. Reasons include:
 	 * <ul>
 	 * <li> This resource does not exist.</li>
 	 * <li> This resource is a project that is not open.</li>
 	 * </ul>
 	 * @see IResource#DEPTH_ZERO
 	 * @see IResource#DEPTH_ONE
 	 * @see IResource#DEPTH_INFINITE
 	 */
 	public IMarker[] findMarkers(String type, boolean includeSubtypes, int depth) throws CoreException;
 
 	/**
 	 * Returns the file extension portion of this resource's name,
 	 * or <code>null</code> if it does not have one.
 	 * <p>
 	 * The file extension portion is defined as the string
 	 * following the last period (".") character in the name.
 	 * If there is no period in the name, the path has no
 	 * file extension portion. If the name ends in a period,
 	 * the file extension portion is the empty string.
 	 * </p>
 	 * <p> 
 	 * This is a resource handle operation; the resource need 
 	 * not exist.
 	 * </p>
 	 *
 	 * @return a string file extension
 	 * @see #getName()
 	 */
 	public String getFileExtension();
 
 	/**
 	 * Returns the full, absolute path of this resource relative to the
 	 * workspace.
 	 * <p> 
 	 * This is a resource handle operation; the resource need 
 	 * not exist.
 	 * If this resource does exist, its path can be safely assumed to be valid.
 	 * </p>
 	 * <p>
 	 * A resource's full path indicates the route from the root of the workspace
 	 * to the resource.  Within a workspace, there is exactly one such path
 	 * for any given resource. The first segment of these paths name a project; 
 	 * remaining segments, folders and/or files within that project.
 	 * The returned path never has a trailing separator.  The path of the
 	 * workspace root is <code>Path.ROOT</code>.
 	 * </p>
 	 * <p>
 	 * Since absolute paths contain the name of the project, they are
 	 * vulnerable when the project is renamed. For most situations,
 	 * project-relative paths are recommended over absolute paths.
 	 * </p>
 	 *
 	 * @return the absolute path of this resource
 	 * @see #getProjectRelativePath()
 	 * @see Path#ROOT
 	 */
 	public IPath getFullPath();
 
 	/**
 	 * Returns a cached value of the local time stamp on disk for this resource, or 
 	 * <code>NULL_STAMP</code>  if the resource does not exist or is not local or is 
 	 * not accessible.  The return value is represented as the number of milliseconds 
 	 * since the epoch (00:00:00 GMT, January 1, 1970).
 	 * The returned value may not be the same as the actual time stamp
 	 * on disk if the file has been modified externally since the last local refresh.
 	 * <p>
 	 * Note that due to varying file system timing granularities, this value is not guaranteed
 	 * to change every time the file is modified.  For a more reliable indication of whether
 	 * the file has changed, use <code>getModificationStamp</code>.
 	 * 
 	 * @return a local file system time stamp, or <code>NULL_STAMP</code>.
 	 * @since 3.0
 	 */
 	public long getLocalTimeStamp();
 
 	/**
 	 * Returns the absolute path in the local file system to this resource, 
 	 * or <code>null</code> if no path can be determined.
 	 * <p>
 	 * If this resource is the workspace root, this method returns
 	 * the absolute local file system path of the platform working area.
 	 * </p><p>
 	 * If this resource is a project that exists in the workspace, this method
 	 * returns the path to the project's local content area. This is true regardless
 	 * of whether the project is open or closed. This value will be null in the case
 	 * where the location is relative to an undefined workspace path variable.
 	 * </p><p>
 	 * If this resource is a linked resource under a project that is open, this
 	 * method returns the resolved path to the linked resource's local contents.
 	 * This value will be null in the case where the location is relative to an
 	 * undefined workspace path variable.
 	 * </p><p>
 	 * If this resource is a file or folder under a project that exists, or a
 	 * linked resource under a closed project, this method returns a (non-
 	 * <code>null</code>) path computed from the location of the project's local
 	 * content area and the project- relative path of the file or folder. This is
 	 * true regardless of whether the file or folders exists, or whether the project
 	 * is open or closed. In the case of linked resources, the location of a linked resource
 	 * within a closed project is too computed from the location of the
 	 * project's local content area and the project-relative path of the resource. If the
 	 * linked resource resides in an open project then its location is computed
 	 * according to the link.
 	 * </p><p>
 	 * If this resource is a project that does not exist in the workspace,
 	 * or a file or folder below such a project, this method returns
 	 * <code>null</code>.
 	 * </p>
 	 * 
 	 * @return the absolute path of this resource in the local file system,
 	 *  or <code>null</code> if no path can be determined
 	 * @see #getRawLocation()
 	 * @see  IProjectDescription#setLocation(IPath)
 	 * @see Platform#getLocation()
 	 */
 	public IPath getLocation();
 
 	/**
 	 * Returns a marker handle with the given id on this resource.
 	 * This resource is not checked to see if it has such a marker.
 	 * The returned marker need not exist.
 	 * This resource need not exist.
 	 *
 	 * @param id the id of the marker
 	 * @return the specified marker handle
 	 * @see IMarker#getId()
 	 */
 	public IMarker getMarker(long id);
 
 	/**
 	 * Returns a non-negative modification stamp, or <code>NULL_STAMP</code> if 
 	 * the resource does not exist or is not local or is not accessible.
 	 * <p>
 	 * A resource's modification stamp gets updated each time a resource is modified.
 	 * If a resource's modification stamp is the same, the resource has not changed.
 	 * Conversely, if a resource's modification stamp is different, some aspect of it
 	 * (other than properties) has been modified at least once (possibly several times).
 	 * Resource modification stamps are preserved across project close/re-open,
 	 * and across workspace shutdown/restart.
 	 * The magnitude or sign of the numerical difference between two modification stamps 
 	 * is not significant.
 	 * </p>
 	 * <p>
 	 * The following things affect a resource's modification stamp:
 	 * <ul>
 	 * <li>creating a non-project resource (changes from <code>NULL_STAMP</code>)</li>
 	 * <li>changing the contents of a file</li>
 	 * <li><code>touch</code>ing a resource</li>
 	 * <li>setting the atttributes of a project presented in a project description</li>
 	 * <li>deleting a resource (changes to <code>NULL_STAMP</code>)</li>
 	 * <li>moving a resource (source changes to <code>NULL_STAMP</code>,
 	 destination changes from <code>NULL_STAMP</code>)</li>
 	 * <li>copying a resource (destination changes from <code>NULL_STAMP</code>)</li>
 	 * <li>making a resource local</li>
 	 * <li>closing a project (changes to <code>NULL_STAMP</code>)</li>
 	 * <li>opening a project (changes from <code>NULL_STAMP</code>)</li>
 	 * <li>adding or removing a project nature (changes from <code>NULL_STAMP</code>)</li>
 	 * </ul>
 	 * The following things do not affect a resource's modification stamp:
 	 * <ul>
 	 * <li>"reading" a resource</li>
 	 * <li>adding or removing a member of a project or folder</li>
 	 * <li>setting a session property</li>
 	 * <li>setting a persistent property</li>
 	 * <li>saving the workspace</li>
 	 * <li>shutting down and re-opening a workspace</li>
 	 * </ul>
 	 * </p>
 	 *
 	 * @return the modification stamp, or <code>NULL_STAMP</code> if this resource either does
 	 *    not exist or exists as a closed project
 	 * @see IResource#NULL_STAMP
 	 */
 	public long getModificationStamp();
 
 	/**
 	 * Returns the name of this resource. 
 	 * The name of a resource is synonymous with the last segment
 	 * of its full (or project-relative) path for all resources other than the 
 	 * workspace root.  The workspace root's name is the empty string.
 	 * <p> 
 	 * This is a resource handle operation; the resource need 
 	 * not exist.
 	 * </p>
 	 * <p>
 	 * If this resource exists, its name can be safely assumed to be valid.
 	 * </p>
 	 *
 	 * @return the name of the resource
 	 * @see #getFullPath()
 	 * @see #getProjectRelativePath()
 	 */
 	public String getName();
 
 	/**
 	 * Returns the resource which is the parent of this resource,
 	 * or <code>null</code> if it has no parent (that is, this
 	 * resource is the workspace root).
 	 * <p>
 	 * The full path of the parent resource is the same as this
 	 * resource's full path with the last segment removed.
 	 * </p>
 	 * <p>
 	 * This is a resource handle operation; neither the resource 
 	 * nor the resulting resource need exist.
 	 * </p>
 	 *
 	 * @return the parent resource of this resource,
 	 *    or <code>null</code> if it has no parent
 	 */
 	public IContainer getParent();
 
 	/**
 	 * Returns the value of the persistent property of this resource identified
 	 * by the given key, or <code>null</code> if this resource has no such property.
 	 *
 	 * @param key the qualified name of the property
 	 * @return the string value of the property, 
 	 *     or <code>null</code> if this resource has no such property
 	 * @exception CoreException if this method fails. Reasons include:
 	 * <ul>
 	 * <li> This resource does not exist.</li>
 	 * <li> This resource is not local.</li>
 	 * <li> This resource is a project that is not open.</li>
 	 * </ul>
 	 * @see #setPersistentProperty(QualifiedName, String)
 	 */
 	public String getPersistentProperty(QualifiedName key) throws CoreException;
 
 	/**
 	 * Returns the project which contains this resource.
 	 * Returns itself for projects and <code>null</code>
 	 * for the workspace root.
 	 * <p>
 	 * A resource's project is the one named by the first segment
 	 * of its full path.
 	 * </p>
 	 * <p>
 	 * This is a resource handle operation; neither the resource 
 	 * nor the resulting project need exist.
 	 * </p>
 	 *
 	 * @return the project handle
 	 */
 	public IProject getProject();
 
 	/**
 	 * Returns a relative path of this resource with respect to its project.
 	 * Returns the empty path for projects and the workspace root.
 	 * <p> 
 	 * This is a resource handle operation; the resource need not exist.
 	 * If this resource does exist, its path can be safely assumed to be valid.
 	 * </p>
 	 * <p>
 	 * A resource's project-relative path indicates the route from the project
 	 * to the resource.  Within a workspace, there is exactly one such path
 	 * for any given resource. The returned path never has a trailing slash.
 	 * </p>
 	 * <p>
 	 * Project-relative paths are recommended over absolute paths, since 
 	 * the former are not affected if the project is renamed.
 	 * </p>
 	 *
 	 * @return the relative path of this resource with respect to its project
 	 * @see #getFullPath()
 	 * @see #getProject()
 	 * @see Path#EMPTY
 	 */
 	public IPath getProjectRelativePath();
 
 	/**
 	 * Returns the file system location of this resource, or <code>null</code> if no
 	 * path can be determined.  The returned path will either be an absolute file
 	 * system path, or a relative path whose first segment is the name of a
 	 * workspace path variable.
 	 * <p>
 	 * If this resource is an existing project, the returned path will be equal to
 	 * the location path in the project description.  If this resource is a linked
 	 * resource in an open project, the returned path will be equal to the location
 	 * path supplied when the linked resource was created.  In all other cases, this
 	 * method returns the same value as <code>getLocation</code>.
 	 * </p>
 	 * 
 	 * @return the raw path of this resource in the local file system,  or
 	 * <code>null</code> if no path can be determined
 	 * @see #getLocation()
 	 * @see IFile#createLink(IPath, int, IProgressMonitor)
 	 * @see IFolder#createLink(IPath, int, IProgressMonitor)
 	 * @see IPathVariableManager
 	 * @see IProjectDescription#getLocation()
 	 * @since 2.1
 	 */
 	public IPath getRawLocation();
 
 	/**
 	 * Returns the value of the session property of this resource identified
 	 * by the given key, or <code>null</code> if this resource has no such property.
 	 *
 	 * @param key the qualified name of the property
 	 * @return the value of the session property, 
 	 *     or <code>null</code> if this resource has no such property
 	 * @exception CoreException if this method fails. Reasons include:
 	 * <ul>
 	 * <li> This resource does not exist.</li>
 	 * <li> This resource is not local.</li>
 	 * <li> This resource is a project that is not open.</li>
 	 * </ul>
 	 * @see #setSessionProperty(QualifiedName, Object)
 	 */
 	public Object getSessionProperty(QualifiedName key) throws CoreException;
 
 	/**
 	 * Returns the type of this resource.
 	 * The returned value will be one of <code>FILE</code>, 
 	 * <code>FOLDER</code>, <code>PROJECT</code>, <code>ROOT</code>.
 	 * <p>
 	 * <ul>
 	 * <li> All resources of type <code>FILE</code> implement <code>IFile</code>.</li>
 	 * <li> All resources of type <code>FOLDER</code> implement <code>IFolder</code>.</li>
 	 * <li> All resources of type <code>PROJECT</code> implement <code>IProject</code>.</li>
 	 * <li> All resources of type <code>ROOT</code> implement <code>IWorkspaceRoot</code>.</li>
 	 * </ul>
 	 * </p>
 	 * <p> 
 	 * This is a resource handle operation; the resource need 
 	 * not exist in the workspace.
 	 * </p>
 	 *
 	 * @return the type of this resource
 	 * @see #FILE
 	 * @see #FOLDER
 	 * @see #PROJECT
 	 * @see #ROOT
 	 */
 	public int getType();
 
 	/**
 	 * Returns the workspace which manages this resource.
 	 * <p>
 	 * This is a resource handle operation; the resource need 
 	 * not exist in the workspace.
 	 * </p>
 	 *
 	 * @return the workspace
 	 */
 	public IWorkspace getWorkspace();
 
 	/**
 	 * Returns whether this resource is accessible.  For files and folders,
 	 * this is equivalent to existing; for projects, 
 	 * this is equivalent to existing and being open.  The workspace root
 	 * is always accessible.
 	 *
 	 * @return <code>true</code> if this resource is accessible, and
 	 *   <code>false</code> otherwise
 	 * @see #exists()
 	 * @see IProject#isOpen()
 	 */
 	public boolean isAccessible();
 
 	/**
 	 * Returns whether this resource subtree is marked as derived. Returns
 	 * <code>false</code> if this resource does not exist.
 	 *
 	 * @return <code>true</code> if this resource is marked as derived, and
 	 *   <code>false</code> otherwise
 	 * @see #setDerived(boolean)
 	 * @since 2.0
 	 */
 	public boolean isDerived();
 
 	/**
 	 * Returns whether this resource and its members (to the 
 	 * specified depth) are expected to have their contents (and properties)
 	 * available locally.  Returns <code>false</code> in all other cases,
 	 * including the case where this resource does not exist.  The workspace
 	 * root and projects are always local.
 	 * <p>
 	 * When a resource is not local, its content and properties are
 	 * unavailable for both reading and writing.
 	 * </p>
 	 * 
 	 * @param depth valid values are <code>DEPTH_ZERO</code>, 
 	 *  <code>DEPTH_ONE</code>, or <code>DEPTH_INFINITE</code>
 	 * @return <code>true</code> if this resource is local, and
 	 *   <code>false</code> otherwise
 	 *
 	 * @see #setLocal(boolean, int, IProgressMonitor)
 	 */
 	public boolean isLocal(int depth);
 
 	/**
 	 * Returns <code>true</code> if this resource has been linked to 
 	 * a location other than the default location calculated by the platform. This
 	 * location can be outside the project's content area or another location
 	 * within the project. Returns <code>false</code>
 	 * in all other cases, including the case where this resource does not exist.
 	 * The workspace root and projects are never linked.
 	 * <p>
 	 * This method returns true only for a resource that has been linked using
 	 * the <code>createLink</code> method.  This method returns false for
 	 * children of linked resources.
 	 * </p>
 	 * 
 	 * @return <code>true</code> if this resource is linked, and 
 	 *   <code>false</code> otherwise
 	 * @see IFile#createLink(IPath, int, IProgressMonitor)
 	 * @see IFolder#createLink(IPath, int, IProgressMonitor)
 	 * @since 2.1
 	 */
 	public boolean isLinked();
 
 	/**
 	 * Returns whether this resource is a phantom resource.
 	 * <p>
 	 * The workspace uses phantom resources to remember outgoing deletions and
 	 * incoming additions relative to an external synchronization partner.  Phantoms
 	 * appear and disappear automatically as a byproduct of synchronization.   
 	 * Since the workspace root cannot be synchronized in this way, it is never a phantom.
 	 * Projects are also never phantoms.
 	 * </p>
 	 * <p>
 	 * The key point is that phantom resources do not exist (in the technical
 	 * sense of <code>exists</code>, which returns <code>false</code>
 	 * for phantoms) are therefore invisible except through a handful of
 	 * phantom-enabled API methods (notably <code>IContainer.members(boolean)</code>).
 	 * </p>
 	 *
 	 * @return <code>true</code> if this resource is a phantom resource, and
 	 *   <code>false</code> otherwise
 	 * @see #exists()
 	 * @see IContainer#members(boolean)
 	 * @see IContainer#findMember(String, boolean)
 	 * @see IContainer#findMember(IPath, boolean)
 	 * @see ISynchronizer
 	 */
 	public boolean isPhantom();
 
 	/**
 	 * Returns whether this resource is marked as read-only in the file system.
 	 *
 	 * @return <code>true</code> if this resource is read-only, 
 	 *		<code>false</code> otherwise
 	 */
 	public boolean isReadOnly();
 
 	/**
 	 * Returns whether this resource and its descendants to the given depth 
 	 * are considered to be in sync with the local file system.  
 	 * <p>
 	 * A resource is considered to be in sync if all of the following 
 	 * conditions are true:
 	 * <ul>
 	 * <li>The resource exists in both the workspace and the filesystem.</li>
 	 * <li>The timestamp in the filesystem has not changed since the 
 	 * last synchronization.</li>
 	 * <li>The resource in the workspace is of the same type as the corresponding
 	 * file in the filesystem (they are either both files or both folders).</li>
 	 * </ul>
 	 *  A resource is also considered to be in sync if it is missing from both
 	 * the workspace and the filesystem.  In all other cases the resource is
 	 * considered to be out of sync.
 	 * </p>
 	 * <p>
 	 * This operation interrogates files and folders in the local file system;
 	 * depending on the speed of the local file system and the requested depth,
 	 * this operation may be time-consuming.
 	 * </p>
 	 * 
 	 * @param depth the depth (one of <code>IResource.DEPTH_ZERO</code>,
 	 *   <code>DEPTH_ONE</code>, or <code>DEPTH_INFINITE</code>)
 	 * @return <code>true</code> if this resource and its descendants to the 
 	 *    specified depth are synchronized, and <code>false</code> in all other
 	 *    cases
 	 * @see IResource#DEPTH_ZERO
 	 * @see IResource#DEPTH_ONE
 	 * @see IResource#DEPTH_INFINITE
 	 * @see #refreshLocal(int, IProgressMonitor)
 	 * @since 2.0
 	 */
 	public boolean isSynchronized(int depth);
 
 	/**
 	 * Returns whether this resource is a team private member of its parent container.
 	 * Returns <code>false</code> if this resource does not exist.
 	 *
 	 * @return <code>true</code> if this resource is a team private member, and
 	 *   <code>false</code> otherwise
 	 * @see #setTeamPrivateMember(boolean)
 	 * @since 2.0
 	 */
 	public boolean isTeamPrivateMember();
 
 	/**
 	 * Moves this resource so that it is located at the given path.  
 	 * <p>
 	 * This is a convenience method, fully equivalent to:
 	 * <pre>
 	 *   move(destination, force ? FORCE : IResource.NONE, monitor);
 	 * </pre>
 	 * </p>
 	 * <p>
 	 * This method changes resources; these changes will be reported
 	 * in a subsequent resource change event that will include 
 	 * an indication that the resource has been removed from its parent
 	 * and that a corresponding resource has been added to its new parent.
 	 * Additional information provided with resource delta shows that these
 	 * additions and removals are related.
 	 * </p>
 	 * <p>
 	 * This method is long-running; progress and cancellation are provided
 	 * by the given progress monitor. 
 	 * </p>
 	 *
 	 * @param destination the destination path 
 	 * @param force a flag controlling whether resources that are not
 	 *    in sync with the local file system will be tolerated
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting is not desired
 	 * @exception CoreException if this resource could not be moved. Reasons include:
 	 * <ul>
 	 * <li> This resource does not exist.</li>
 	 * <li> This resource or one of its descendents is not local.</li>
 	 * <li> The source or destination is the workspace root.</li>
 	 * <li> The source is a project but the destination is not.</li>
 	 * <li> The destination is a project but the source is not.</li>
 	 * <li> The resource corresponding to the parent destination path does not exist.</li>
 	 * <li> The resource corresponding to the parent destination path is a closed 
 	 *      project.</li>
 	 * <li> A resource at destination path does exist.</li>
 	 * <li> A resource of a different type exists at the destination path.</li>
 	 * <li> This resource or one of its descendents is out of sync with the local file
 	 *      system and <code>force</code> is <code>false</code>.</li>
 	 * <li> The workspace and the local file system are out of sync
 	 *      at the destination resource or one of its descendents.</li>
 	 * <li> Resource changes are disallowed during certain types of resource change 
 	 *       event notification. See <code>IResourceChangeEvent</code> for more details.</li>
 	 * <li> The source resource is a file and the destination path specifies a project.</li>
 	 * </ul>
 	 * @exception OperationCanceledException if the operation is canceled. 
 	 * Cancelation can occur even if no progress monitor is provided.
 	 * @see IResourceDelta#getFlags()
 	 */
 	public void move(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException;
 
 	/**
 	 * Moves this resource so that it is located at the given path.  
 	 * The path of the resource must not be a prefix of the destination path. The
 	 * workspace root may not be the source or destination location of a move
 	 * operation, and a project can only be moved to another project. After
 	 * successful completion, the resource and any direct or indirect members will
 	 * no longer exist; but corresponding new resources will now exist at the given
 	 * path.
 	 * <p>
 	 * The supplied path may be absolute or relative.  Absolute paths fully specify
 	 * the new location for the resource, including its project.  Relative paths are
 	 * considered to be relative to the container of the resource being moved. A
 	 * trailing slash is ignored.
 	 * </p>
 	 * <p>
 	 * Calling this method with a one segment absolute destination path is
 	 * equivalent to calling:
 	 * <pre>
 	 IProjectDescription description = getDescription();
 	 description.setName(path.lastSegment());
 	 move(description, updateFlags, monitor);
 	 * </pre>
 	 * </p>
 	 * <p> When a resource moves, its session and persistent properties move with
 	 * it. Likewise for all other attributes of the resource including markers.
 	 * </p>
 	 * <p>
 	 * The <code>FORCE</code> update flag controls how this method deals with cases
 	 * where the workspace is not completely in sync with the local file system. If
 	 * <code>FORCE</code> is not specified, the method will only attempt to move
 	 * resources that are in sync with the corresponding files and directories in
 	 * the local file system; it will fail if it encounters a resource that is out
 	 * of sync with the file system. However, if <code>FORCE</code> is specified,
 	 * the method moves all corresponding files and directories from the local file
 	 * system, including ones that have been recently updated or created. Note that
 	 * in both settings of the <code>FORCE</code> flag, the operation fails if the
 	 * newly created resources in the workspace would be out of sync with the local
 	 * file system; this ensures files in the file system cannot be accidentally
 	 * overwritten.
 	 * </p>
 	 * <p>
 	 * The <code>KEEP_HISTORY</code> update flag controls whether or not 
 	 * file that are about to be deleted from the local file system have their
 	 * current contents saved in the workspace's local history. The local history
 	 * mechanism serves as a safety net to help the user recover from mistakes that
 	 * might otherwise result in data loss. Specifying <code>KEEP_HISTORY</code>
 	 * is recommended except in circumstances where past states of the files are of
 	 * no conceivable interest to the user. Note that local history is maintained
 	 * with each individual project, and gets discarded when a project is deleted
 	 * from the workspace. Hence <code>KEEP_HISTORY</code> is only really applicable
 	 * when moving files and folders, but not whole projects.
 	 * </p>
 	 * <p>
 	 * If this resource is not a project, an attempt will be made to copy the local history 
 	 * for this resource and its children, to the destination. Since local history existence 
 	 * is a safety-net mechanism, failure of this action will not result in automatic failure 
 	 * of the move operation.
 	 * </p>
 	 * <p>
 	 * The <code>SHALLOW</code> update flag controls how this method deals with linked
 	 * resources.  If <code>SHALLOW</code> is not specified, then the underlying
 	 * contents of the linked resource will always be moved in the file system.  In
 	 * this case, the destination of the move will never be a linked resource or
 	 * contain any linked resources. If <code>SHALLOW</code> is specified when a
 	 * linked resource is moved into another project, a new linked resource is
 	 * created in the destination project that points to the same file system
 	 * location.  When a project containing linked resources is moved, the new
 	 * project will contain the same linked resources pointing to the same file
 	 * system locations.  For either of these cases, no files on disk under the
 	 * linked resource are actually moved. With the <code>SHALLOW</code> flag,
 	 * moving of linked resources into anything other than a project is not
 	 * permitted.  The <code>SHALLOW</code> update flag is ignored when moving non-
 	 * linked resources.
 	 * </p>
 	 * <p> 
 	 * Update flags other than <code>FORCE</code>, <code>KEEP_HISTORY</code>and
 	 * <code>SHALLOW</code> are ignored.
 	 * </p>
 	 * <p>
 	 * This method changes resources; these changes will be reported in a subsequent
 	 * resource change event that will include an indication that the resource has
 	 * been removed from its parent and that a corresponding resource has been added
 	 * to its new parent. Additional information provided with resource delta shows
 	 * that these additions and removals are related.
 	 * </p>
 	 * <p>
 	 * This method is long-running; progress and cancellation are provided
 	 * by the given progress monitor. 
 	 * </p>
 	 *
 	 * @param destination the destination path 
 	 * @param updateFlags bit-wise or of update flag constants
 	 *   (<code>FORCE</code>, <code>KEEP_HISTORY</code> and <code>SHALLOW</code>)
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting is not desired
 	 * @exception CoreException if this resource could not be moved. Reasons include:
 	 * <ul>
 	 * <li> This resource does not exist.</li>
 	 * <li> This resource or one of its descendents is not local.</li>
 	 * <li> The source or destination is the workspace root.</li>
 	 * <li> The source is a project but the destination is not.</li>
 	 * <li> The destination is a project but the source is not.</li>
 	 * <li> The resource corresponding to the parent destination path does not exist.</li>
 	 * <li> The resource corresponding to the parent destination path is a closed 
 	 *      project.</li>
 	 * <li> The source is a linked resource, but the destination is not a project
 	 *      and  <code>SHALLOW</code> is specified.</li>
 	 * <li> A resource at destination path does exist.</li>
 	 * <li> A resource of a different type exists at the destination path.</li>
 	 * <li> This resource or one of its descendents is out of sync with the local file system
 	 *      and <code>force</code> is <code>false</code>.</li>
 	 * <li> The workspace and the local file system are out of sync
 	 *      at the destination resource or one of its descendents.</li>
 	 * <li> The source resource is a file and the destination path specifies a project.</li>
 	 * <li> The location of the source resource on disk is the same or a prefix of
 	 * the location of the destination resource on disk.</li>
 	 * <li> Resource changes are disallowed during certain types of resource change
 	 * event notification. See <code>IResourceChangeEvent</code> for more details.</li>
 	 * </ul>
 	 * @exception OperationCanceledException if the operation is canceled. 
 	 * Cancelation can occur even if no progress monitor is provided.
 	 * @see IResourceDelta#getFlags()
 	 * @see #FORCE
 	 * @see #KEEP_HISTORY
 	 * @see #SHALLOW
 	 * @see IResourceRuleFactory#moveRule(IResource, IResource)
 	 * @since  2.0
 	 */
 	public void move(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException;
 
 	/**
 	 * Renames or relocates this project so that it is the project specified by the given project 
 	 * description.
 	 * <p>
 	 * This is a convenience method, fully equivalent to:
 	 * <pre>
 	 *   move(description, (keepHistory ? KEEP_HISTORY : IResource.NONE) | (force ? FORCE : IResource.NONE), monitor);
 	 * </pre>
 	 * </p>
 	 * <p>
 	 * This operation changes resources; these changes will be reported
 	 * in a subsequent resource change event that will include 
 	 * an indication that the resource has been removed from its parent
 	 * and that a corresponding resource has been added to its new parent.
 	 * Additional information provided with resource delta shows that these
 	 * additions and removals are related.
 	 * </p>
 	 * <p>
 	 * This method is long-running; progress and cancellation are provided
 	 * by the given progress monitor. 
 	 * </p>
 	 *
 	 * @param description the destination project description
 	 * @param force a flag controlling whether resources that are not
 	 *    in sync with the local file system will be tolerated
 	 * @param keepHistory a flag indicating whether or not to keep
 	 *    local history for files
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting is not desired
 	 * @exception CoreException if this resource could not be moved. Reasons include:
 	 * <ul>
 	 * <li> This resource does not exist.</li>
 	 * <li> This resource or one of its descendents is not local.</li>
 	 * <li> This resource is not a project.</li>
 	 * <li> The project at the destination already exists.</li>
 	 * <li> This resource or one of its descendents is out of sync with the local file
 	 *      system and <code>force</code> is <code>false</code>.</li>
 	 * <li> The workspace and the local file system are out of sync
 	 *      at the destination resource or one of its descendents.</li>
 	 * <li> Resource changes are disallowed during certain types of resource change 
 	 *       event notification. See <code>IResourceChangeEvent</code> for more details.</li>
 	 * </ul>
 	 * @exception OperationCanceledException if the operation is canceled. 
 	 * Cancelation can occur even if no progress monitor is provided.
 	 * @see IResourceDelta#getFlags()
 	 */
 	public void move(IProjectDescription description, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException;
 
 	/**
 	 * Renames or relocates this project so that it is the project specified by the
 	 * given project description. The description specifies the name, location and
 	 * attributes of the new project. After successful completion, the old project
 	 * and any direct or indirect members will no longer exist; but corresponding
 	 * new resources will now exist at the project.
 	 * <p>
 	 * When a resource moves, its session and persistent properties move with it.
 	 * Likewise for all the other attributes of the resource including markers.
 	 * </p>
 	 * <p>
 	 * When this project's location is the default location, then the directories
 	 * and files on disk are moved to be in the location specified by the given
 	 * description. If the given description specifies the default location for the
 	 * project, the directories and files are moved to the default location. In all
 	 * other cases the directories and files on disk are left untouched. If the name
 	 * in the given description is the same as this project's name and the location
 	 * is different, then the project contents will be moved to the new location.
 	 * All other parts of the given description are ignored.
 	 * </p>
 	 * <p>
 	 * The <code>FORCE</code> update flag controls how this method deals with cases
 	 * where the workspace is not completely in sync with the local file system. If
 	 * <code>FORCE</code> is not specified, the method will only attempt to move
 	 * resources that are in sync with the corresponding files and directories in
 	 * the local file system; it will fail if it encounters a resource that is out
 	 * of sync with the file system. However, if <code>FORCE</code> is specified,
 	 * the method moves all corresponding files and directories from the local file
 	 * system, including ones that have been recently updated or created. Note that
 	 * in both settings of the <code>FORCE</code> flag, the operation fails if the
 	 * newly created resources in the workspace would be out of sync with the local
 	 * file system; this ensures files in the file system cannot be accidentally
 	 * overwritten.
 	 * </p>
 	 * <p>
 	 * The <code>KEEP_HISTORY</code> update flag controls whether or not file that
 	 * are about to be deleted from the local file system have their current
 	 * contents saved in the workspace's local history. The local history mechanism
 	 * serves as a safety net to help the user recover from mistakes that might
 	 * otherwise result in data loss. Specifying <code>KEEP_HISTORY</code> is
 	 * recommended except in circumstances where past states of the files are of no
 	 * conceivable interest to the user. Note that local history is maintained
 	 * with each individual project, and gets discarded when a project is deleted
 	 * from the workspace. Hence <code>KEEP_HISTORY</code> is only really applicable
 	 * when moving files and folders, but not whole projects.
 	 * </p>
 	 * <p>
 	 * Local history information for this project and its children will not be moved to the
 	 * destination.
 	 * </p>
 	 * <p>
 	 * The <code>SHALLOW</code> update flag controls how this method deals with linked
 	 * resources.  If <code>SHALLOW</code> is not specified, then the underlying
 	 * contents of any linked resource will always be moved in the file system.  In
 	 * this case, the destination of the move will not contain any linked resources.
 	 * If  <code>SHALLOW</code> is specified when a project containing linked
 	 * resources is moved, new linked resources are created in the destination
 	 * project pointing to the same file system locations.  In this case, no files
 	 * on disk under any linked resource are actually moved.  The
 	 * <code>SHALLOW</code> update flag is ignored when moving non- linked
 	 * resources.
 	 * </p>
 	 * <p>  
 	 * Update flags other than <code>FORCE</code>, <code>KEEP_HISTORY</code> and
 	 * <code>SHALLOW</code> are ignored.
 	 * </p>
 	 * <p>
 	 * This method changes resources; these changes will be reported in a subsequent
 	 * resource change event that will include an indication that the resource has
 	 * been removed from its parent and that a corresponding resource has been added
 	 * to its new parent. Additional information provided with resource delta shows
 	 * that these additions and removals are related.
 	 * </p>
 	 * <p>
 	 * This method is long-running; progress and cancellation are provided
 	 * by the given progress monitor. 
 	 * </p>
 	 *
 	 * @param description the destination project description
 	 * @param updateFlags bit-wise or of update flag constants
 	 *   (<code>FORCE</code>, <code>KEEP_HISTORY</code>, and <code>SHALLOW</code>)
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting is not desired
 	 * @exception CoreException if this resource could not be moved. Reasons include:
 	 * <ul>
 	 * <li> This resource does not exist.</li>
 	 * <li> This resource or one of its descendents is not local.</li>
 	 * <li> This resource is not a project.</li>
 	 * <li> The project at the destination already exists.</li>
 	 * <li> This resource or one of its descendents is out of sync with the
 	 *      local  file system and <code>FORCE</code> is not specified.</li>
 	 * <li> The workspace and the local file system are out of sync
 	 *      at the destination resource or one of its descendents.</li>
 	 * <li> Resource changes are disallowed during certain types of resource change 
 	 *      event notification. See <code>IResourceChangeEvent</code> for more details.</li>
 	 * </ul>
 	 * @exception OperationCanceledException if the operation is canceled. 
 	 * Cancelation can occur even if no progress monitor is provided.
 	 * @see IResourceDelta#getFlags()
 	 * @see #FORCE
 	 * @see #KEEP_HISTORY
 	 * @see #SHALLOW
 	 * @see IResourceRuleFactory#moveRule(IResource, IResource)
 	 * @since  2.0
 	 */
 	public void move(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException;
 
 	/**
 	 * Refreshes the resource hierarchy from this resource and its 
 	 * children (to the specified depth) relative to the local file system.
 	 * Creations, deletions, and changes detected in the local file system 
 	 * will be reflected in the workspace's resource tree.
 	 * This resource need not exist or be local.
 	 * <p>
 	 * This method may discover changes to resources; any such 
 	 * changes will be reported in a subsequent resource change event.
 	 * </p>
 	 * <p>
 	 * If a new file or directory is discovered in the local file
 	 * system at or below the location of this resource, 
 	 * any parent folders required to contain the new
 	 * resource in the workspace will also be created automatically as required.
 	 * </p>
 	 * <p>
 	 * This method is long-running; progress and cancellation are provided
 	 * by the given progress monitor.
 	 * </p>
 	 * 
 	 * @param depth valid values are <code>DEPTH_ZERO</code>, 
 	 *  <code>DEPTH_ONE</code>, or <code>DEPTH_INFINITE</code>
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting is not desired
 	 * @exception CoreException if this method fails. Reasons include:
 	 * <ul>
 	 * <li> Resource changes are disallowed during certain types of resource change 
 	 *       event notification. See <code>IResourceChangeEvent</code> for more details.</li>
 	 * </ul>
 	 * @exception OperationCanceledException if the operation is canceled. 
 	 * Cancelation can occur even if no progress monitor is provided.
 	 * @see IResource#DEPTH_ZERO
 	 * @see IResource#DEPTH_ONE
 	 * @see IResource#DEPTH_INFINITE
 	 * @see IResourceRuleFactory#refreshRule(IResource)
 	 */
 	public void refreshLocal(int depth, IProgressMonitor monitor) throws CoreException;
 
 	/**
 	 * Sets whether this resource subtree is marked as derived.
 	 * <p>
 	 * A <b>derived</b> resource is a regular file or folder that is
 	 * created in the course of translating, compiling, copying, or otherwise 
 	 * processing other files. Derived resources are not original data, and can be
 	 * recreated from other resources. It is commonplace to exclude derived 
 	 * resources from version and configuration management because they would
 	 * otherwise clutter the team repository with version of these ever-changing
 	 * files as each user regenerates them.
 	 * </p>
 	 * <p>
 	 * If a resource or any of its ancestors is marked as derived, a team 
 	 * provider should assume that the resource is not under version and
 	 * configuration management <it>by default</it>. That is, the resource
 	 * should only be stored in a team repository if the user explicitly indicates
 	 * that this resource is worth saving.
 	 * </p>
 	 * <p>
 	 * Newly-created resources are not marked as derived; rather, the mark must be
 	 * set explicitly using <code>setDerived(true)</code>. Derived marks are maintained
 	 * in the in-memory resource tree, and are discarded when the resources are deleted.
 	 * Derived marks are saved to disk when a project is closed, or when the workspace
 	 * is saved.
 	 * </p>
 	 * <p>
 	 * Projects and the workspace root are never considered derived; attempts to
 	 * mark them as derived are ignored.
 	 * </p>
 	 * <p>
 	 * This operation does <b>not</b> result in a resource change event, and does not
 	 * trigger auto-builds.
 	 * </p>
 	 * 
 	 * @param isDerived <code>true</code> if this resource is to be marked
 	 *   as derived, and <code>false</code> otherwise
 	 * @exception CoreException if this method fails. Reasons include:
 	 * <ul>
 	 * <li> This resource does not exist.</li>
 	 * <li> Resource changes are disallowed during certain types of resource change 
 	 *       event notification. See <code>IResourceChangeEvent</code> for more details.</li>
 	 * </ul>
 	 * @see #isDerived()
 	 * @since 2.0
 	 */
 	public void setDerived(boolean isDerived) throws CoreException;
 
 	/**
 	 * Set whether or not this resource and its members (to the 
 	 * specified depth) are expected to have their contents (and properties)
 	 * available locally.  The workspace root and projects are always local and 
 	 * attempting to set either to non-local (i.e., passing <code>false</code>) 
 	 * has no affect on the resource.
 	 * <p>
 	 * When a resource is not local, its content and properties are
 	 * unavailable for both reading and writing.
 	 * </p>
 	 * <p>
 	 * This method is long-running; progress and cancellation are provided
 	 * by the given progress monitor.
 	 * </p>
 	 * 
 	 * @param flag whether this resource should be considered local
 	 * @param depth valid values are <code>DEPTH_ZERO</code>, 
 	 *  <code>DEPTH_ONE</code>, or <code>DEPTH_INFINITE</code>
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting is not desired
 	 * @exception CoreException if this method fails. Reasons include:
 	 * <ul>
 	 * <li> Resource changes are disallowed during certain types of resource change 
 	 *       event notification. See <code>IResourceChangeEvent</code> for more details.</li>
 	 * </ul>
 	 * @exception OperationCanceledException if the operation is canceled. 
 	 * Cancelation can occur even if no progress monitor is provided.
 	 * @see #isLocal(int)
 	 */
 	public void setLocal(boolean flag, int depth, IProgressMonitor monitor) throws CoreException;
 
 	/**
 	 * Sets the local time stamp on disk for this resource.  The time must be represented 
 	 * as the number of milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
 	 * Returns the actual time stamp that was recorded.
 	 * Due to varying file system timing granularities, the provided value may be rounded
 	 * or otherwise truncated, so the actual recorded time stamp that is returned may
 	 * not be the same as the supplied value.
 	 * 
 	 * @param value a time stamp in millseconds.
 	 * @return a local file system time stamp.
 	 * @exception CoreException if this method fails. Reasons include:
 	 * <ul>
 	 * <li> This resource does not exist.</li>
 	 * <li> This resource is not local.</li>
 	 * <li> This resource is not accessible.</li>
 	 * <li> Resource changes are disallowed during certain types of resource change 
 	 *       event notification. See <code>IResourceChangeEvent</code> for more details.</li>
 	 * </ul>
 	 * @since 3.0
 	 */
 	public long setLocalTimeStamp(long value) throws CoreException;
 
 	/**
 	 * Sets the value of the persistent property of this resource identified
 	 * by the given key. If the supplied value is <code>null</code>,
 	 * the persistent property is removed from this resource. The change
 	 * is made immediately on disk.
 	 * <p>
 	 * Persistent properties are intended to be used by plug-ins to store
 	 * resource-specific information that should be persisted across platform sessions.
 	 * The value of a persistent property is a string which should be
 	 * short (i.e., under 2KB). Unlike session properties, persistent properties are
 	 * stored on disk and maintained across workspace shutdown and restart.
 	 * </p>
 	 * <p>
 	 * The qualifier part of the property name must be the unique identifier
 	 * of the declaring plug-in (e.g. <code>"com.example.plugin"</code>).
 	 * </p>
 	 *
 	 * @param key the qualified name of the property
 	 * @param value the string value of the property, 
 	 *     or <code>null</code> if the property is to be removed
 	 * @exception CoreException if this method fails. Reasons include:
 	 * <ul>
 	 * <li> This resource does not exist.</li>
 	 * <li> This resource is not local.</li>
 	 * <li> This resource is a project that is not open.</li>
 	 * <li> Resource changes are disallowed during certain types of resource change 
 	 *       event notification. See <code>IResourceChangeEvent</code> for more details.</li>
 	 * </ul>
 	 * @see #getPersistentProperty(QualifiedName)
 	 * @see #isLocal(int)
 	 */
 	public void setPersistentProperty(QualifiedName key, String value) throws CoreException;
 
 	/**
 	 * Sets or unsets this resource as read-only in the file system.
 	 *
 	 * @param readOnly <code>true</code> to set it to read-only, 
 	 *		<code>false</code> to unset
 	 */
 	public void setReadOnly(boolean readOnly);
 
 	/**
 	 * Sets the value of the session property of this resource identified
 	 * by the given key. If the supplied value is <code>null</code>,
 	 * the session property is removed from this resource. 
 	 * <p>
 	 * Sessions properties are intended to be used as a caching mechanism
 	 * by ISV plug-ins. They allow key-object associations to be stored with
 	 * existing resources in the workspace. These key-value associations are
 	 * maintained in memory (at all times), and the information is lost when a
 	 * resource is deleted from the workspace, when the parent project
 	 * is closed, or when the workspace is closed.
 	 * </p>
 	 * <p>
 	 * The qualifier part of the property name must be the unique identifier
 	 * of the declaring plug-in (e.g. <code>"com.example.plugin"</code>).
 	 * </p>
 	 *
 	 * @param key the qualified name of the property
 	 * @param value the value of the session property, 
 	 *     or <code>null</code> if the property is to be removed
 	 * @exception CoreException if this method fails. Reasons include:
 	 * <ul>
 	 * <li> This resource does not exist.</li>
 	 * <li> This resource is not local.</li>
 	 * <li> This resource is a project that is not open.</li>
 	 * <li> Resource changes are disallowed during certain types of resource change 
 	 *       event notification. See <code>IResourceChangeEvent</code> for more details.</li>
 	 * </ul>
 	 * @see #getSessionProperty(QualifiedName)
 	 */
 	public void setSessionProperty(QualifiedName key, Object value) throws CoreException;
 
 	/**
 	 * Sets whether this resource subtree is a team private member of its parent container.
 	 * <p>
 	 * A <b>team private member</b> resource is a special file or folder created by a team
 	 * provider to hold team-provider-specific information. Resources marked as team private
 	 * members are invisible to most clients. 
 	 * </p>
 	 * <p>
 	 * Newly-created resources are not team private members by default; rather, the
 	 * team provider must mark a resource explicitly using
 	 * <code>setTeamPrivateMember(true)</code>. Team private member marks are
 	 * maintained in the in-memory resource tree, and are discarded when the
 	 * resources are deleted. Team private member marks are saved to disk when a
 	 * project is closed, or when the workspace is saved.
 	 * </p>
 	 * <p>
 	 * Projects and the workspace root are never considered team private members;
 	 * attempts to mark them as team private are ignored.
 	 * </p>
 	 * <p>
 	 * This operation does <b>not</b> result in a resource change event, and does not
 	 * trigger auto-builds.
 	 * </p>
 	 * 
 	 * @param isTeamPrivate <code>true</code> if this resource is to be marked
 	 *   as team private, and <code>false</code> otherwise
 	 * @exception CoreException if this method fails. Reasons include:
 	 * <ul>
 	 * <li> This resource does not exist.</li>
 	 * <li> Resource changes are disallowed during certain types of resource change 
 	 *       event notification. See <code>IResourceChangeEvent</code> for more details.</li>
 	 * </ul>
 	 * @see #isTeamPrivateMember()
 	 * @since 2.0
 	 */
 	public void setTeamPrivateMember(boolean isTeamPrivate) throws CoreException;
 
 	/**
 	 * Marks this resource as having changed even though its content
 	 * may not have changed. This method can be used to trigger 
 	 * the rebuilding of resources/structures derived from this resource.
	 * Touching the workspace root has no effect. 
 	 * <p>
 	 * This method changes resources; these changes will be reported
	 * in a subsequent resource change event. If the resource is a project,
	 * the change event will indicate a description change.
 	 * </p>
 	 * <p>
 	 * This method is long-running; progress and cancellation are provided
 	 * by the given progress monitor. 
 	 * </p>
 	 *
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting is not desired
 	 * @exception CoreException if this method fails. Reasons include:
 	 * <ul>
 	 * <li> This resource does not exist.</li>
 	 * <li> This resource is not local.</li>
 	 * <li> Resource changes are disallowed during certain types of resource change 
 	 *       event notification. See <code>IResourceChangeEvent</code> for more details.</li>
 	 * </ul>
 	 * @exception OperationCanceledException if the operation is canceled. 
 	 * Cancelation can occur even if no progress monitor is provided.
 	 * @see IResourceRuleFactory#modifyRule(IResource)
	 * @see IResourceDelta#CONTENT
	 * @see IResourceDelta#DESCRIPTION
 	 */
 	public void touch(IProgressMonitor monitor) throws CoreException;
 }
