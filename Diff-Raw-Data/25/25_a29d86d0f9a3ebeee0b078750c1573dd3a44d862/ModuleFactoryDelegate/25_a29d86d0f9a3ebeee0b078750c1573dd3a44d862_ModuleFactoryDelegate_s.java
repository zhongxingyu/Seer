 /*******************************************************************************
  * Copyright (c) 2004, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.wst.server.core.model;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.internal.Module;
 import org.eclipse.wst.server.core.internal.ModuleFactory;
 /**
  * A module factory delegate provides a mechanism for discovering
  * modules. A module factory delegate is specified by the
  * <code>class</code> attribute of a <code>moduleFactories</code> extension.
  * <p>
  * When the module factory needs to be given a delegate, the delegate class
  * specified for the module factory is instantiated with a 0-argument
  * constructor.
  * </p>
  * <p>
  * Module factory delegates may keep state in instance fields, but that state is
  * transient and will not be persisted across workbench sessions.
  * </p>
  * <p>
  * This abstract class is intended to be extended only by clients
  * to extend the <code>moduleFactories</code> extension point.
  * </p>
  * 
  * @see org.eclipse.wst.server.core.IModule
  * @see ModuleDelegate
  * @since 1.0
  */
 public abstract class ModuleFactoryDelegate {
 	private ModuleFactory factory;
 	
 	/**
 	 * Delegates must have a public 0-arg constructor.
 	 */
 	public ModuleFactoryDelegate() {
 		// do nothing
 	}
 
 	/**
 	 * Initializes this module factory delegate with its life-long module
 	 * factory instance.
 	 * <p>
 	 * This method is called by the server core framework.
 	 * Clients should never call this method.
 	 * </p>
 	 * 
 	 * @param newFactory the module factory instance
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting and cancellation are not desired
 	 */
 	final void initialize(ModuleFactory newFactory, IProgressMonitor monitor) {
 		factory = newFactory;
 	}
 
 	/**
 	 * Returns the id of this factory.
 	 * Each factory has a distinct id, fixed for its lifetime. Ids are intended to
 	 * be used internally as keys; they are not intended to be shown to end users.
 	 * 
 	 * @return the factory id
 	 */
 	protected final String getId() {
 		return factory.getId();
 	}
 
 	/**
 	 * Creates a module instance with the given static information. This method is used
 	 * by module factory delegates to create module instances.
 	 *  
 	 * @param id the module id
 	 * @param name the module name
 	 * @param type the module type id
 	 * @param version the module version id
 	 * @param project the project that the module is contained in
 	 * @return a module instance
 	 */
 	protected final IModule createModule(String id, String name, String type, String version, IProject project) {
 		return new Module(factory, id, name, type, version, project);
 	}
 
 	/**
 	 * Creates the module delegate for a module with the given information.
 	 * This method is called when a client needs to access the module delegate
 	 * associated with the given module.
 	 * 
 	 * @param module a module
 	 * @return the module delegate
 	 */
 	public abstract ModuleDelegate getModuleDelegate(IModule module);
 
 	/**
 	 * Return all modules created by this factory. 
 	 * <p>
 	 * This method is normally called by the web server core framework.
 	 * Clients (other than the delegate) should never call this method.
 	 * </p>
 	 * <p>
 	 * A new array is returned on each call, so clients may store or modify the result.
 	 * </p>
 	 * 
 	 * @return a possibly-empty array of modules {@link IModule}
 	 */
 	public abstract IModule[] getModules();
 }
