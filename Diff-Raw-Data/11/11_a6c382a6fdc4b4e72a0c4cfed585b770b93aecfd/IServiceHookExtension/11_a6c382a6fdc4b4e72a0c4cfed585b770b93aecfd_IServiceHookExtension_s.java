 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.communication.publisher;
 
 import org.eclipse.riena.communication.core.hooks.IServiceHook;
 import org.eclipse.riena.core.injector.extension.ExtensionInterface;
 import org.eclipse.riena.core.injector.extension.MapName;
 
 /**
  * Extension point interface for service hooks
  */
 @ExtensionInterface(id = "serviceHooks")
 public interface IServiceHookExtension {
 
 	/**
 	 * The unique name of the service hook.
 	 * 
 	 * @return the unique name
 	 */
 	String getName();
 
 	/**
 	 * The comma-separated list of service hook names that shall be executed
 	 * before this hook.
 	 * 
 	 * @return the pre hooks list
 	 */
 	String getPreHooks();
 
 	/**
 	 * The comma-separated list of service hook names that shall be executed
 	 * after this hook.
 	 * 
 	 * @return the post hooks list
 	 */
 	String getPostHooks();
 
 	/**
 	 * Get the service hook instance.
 	 * 
 	 * @return the service hook
 	 */
 	@MapName("class")
 	IServiceHook getServiceHook();
 }
