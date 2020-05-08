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
 package org.eclipse.riena.internal.communication.core.factory;
 
 import org.eclipse.riena.communication.core.hooks.ICallHook;
 import org.eclipse.riena.core.injector.extension.ExtensionInterface;
 import org.eclipse.riena.core.injector.extension.MapName;
 
 /**
  * Extension point interface for call hooks
  */
 @ExtensionInterface(id = "callHooks")
 public interface ICallHookExtension {
 
 	/**
 	 * The unique name of the call hook.
 	 * 
 	 * @return the unique name
 	 */
 	String getName();
 
 	/**
 	 * The comma-separated list of call hook names that shall be executed before
 	 * this hook.
 	 * 
 	 * @return the pre hooks list
 	 */
 	String getPreHooks();
 
 	/**
 	 * The comma-separated list of call hook names that shall be executed after
 	 * this hook.
 	 * 
 	 * @return the post hooks list
 	 */
 	String getPostHooks();
 
 	/**
 	 * Get the call hook instance.
 	 * 
 	 * @return the call hook
 	 */
 	@MapName("class")
 	ICallHook getCallHook();
 }
