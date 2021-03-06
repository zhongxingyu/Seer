 /*******************************************************************************
  * Copyright (c) 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.core.resources.mapping;
 
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 
 /**
  * This factory is used to build a resource delta that represents a proposed change
  * that can then be passed to the {@link ResourceChangeValidator#validateChange(IResourceDelta, IProgressMonitor)}
  * method in order to validate the change with any model providers stored in those resources.
  * The deltas created by calls to the methods of this interface will be the same as 
  * those generated by the workspace if the proposed operations were performed.
  * <p>
  * This factory does not validate that the proposed operation is valid given the current
  * state of the resources and any other proposed changes. It only records the
  * delta that would result.
  * <p>
  * This interface is not intended to be implemented by clients.
  * </p>
  * 
  * @see ResourceChangeValidator
  * @see ModelProvider
 * 
  * @since 3.2
  */
 public interface IResourceChangeDescriptionFactory {
 
 	/**
 	 * Record a delta that represents a content change for the given file.
 	 * @param file the file whose contents will be changed
 	 */
 	public void change(IFile file);
 
 	/**
 	 *  Record the set of deltas representing the closed of a project.
 	 * @param project the project that will be closed
 	 */
 	public void close(IProject project);
 
 	/**
 	 * Record the set of deltas representing a copy of the given resource to the
	 * given path. 
	 * TODO: Additional flag needed to describe change
 	 * @param resource the resource that will be copied
	 * @param destination the destination the resource is being copied to
 	 */
 	public void copy(IResource resource, IPath destination);
	
 	/**
 	 * Record a delta that represents a resource being created.
 	 * @param resource the resource that is created
 	 */
 	public void create(IResource resource);
 
 	/**
 	 * Record the set of deltas representing a deletion of the given resource.
 	 * @param resource the resource that will be deleted
 	 */
 	public void delete(IResource resource);
 
 	/**
 	 * Return the proposed delta that has been accumulated by this factory.
 	 * @return the proposed delta that has been accumulated by this factory
 	 */
 	public IResourceDelta getDelta();
 
 	/**
 	 * Record the set of deltas representing a move of the given resource to the
	 * given path. 
 	 * @param resource the resource that will be moved
	 * @param destination the destination the resource is being moved to
 	 */
 	public void move(IResource resource, IPath destination);
 
 }
