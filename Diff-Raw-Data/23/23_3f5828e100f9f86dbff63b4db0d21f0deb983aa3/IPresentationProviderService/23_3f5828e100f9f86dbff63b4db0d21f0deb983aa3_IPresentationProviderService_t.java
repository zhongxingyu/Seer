 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation;
 
 import org.eclipse.riena.ui.ridgets.controller.IController;
 
 /**
  * Implementation of this service interface provides service methods to get
  * information provided by WorkAreaPresentationDefinitions and
  * NavigationNodePresentationDefitinios identified by a given presentationID.
  */
 public interface IPresentationProviderService {
 
 	/**
 	 * Returns a navigationNode identified by the given navigationNodeId. The
 	 * node is created if it not yet exists.
 	 * 
 	 * @param sourceNode
 	 * @param targetId
 	 * @param argument
 	 * @return
 	 */
	INavigationNode<?> provideNode(INavigationNode<?> sourceNode, NavigationNodeId targetId, NavigationArgument argument);
 
 	/**
 	 * Return an Object representing a instance of a view. In case of SWT/Riena
 	 * it is the String representation of the view (the view id).
 	 * 
 	 * @return the view id of the matching view
 	 */
 	Object provideView(NavigationNodeId targetId);
 
 	/**
 	 * Returns the view controller for the work area presentation for the given
 	 * navigationNodeId
 	 */
 	IController provideController(INavigationNode<?> node);
 
 	/**
 	 * Return true if the specified view should be a shared view, false
 	 * otherwise
 	 */
 	boolean isViewShared(NavigationNodeId targetId);
 
 	/**
 	 * Perform some housekeeping
 	 */
 	void cleanUp();

	ISubModuleTypeDefinition[] getSubModuleTypeDefitinions();
 }
