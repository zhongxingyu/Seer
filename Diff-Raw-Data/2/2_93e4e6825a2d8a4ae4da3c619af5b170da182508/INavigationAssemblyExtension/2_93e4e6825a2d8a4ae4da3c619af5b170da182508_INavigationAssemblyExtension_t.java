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
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.riena.core.injector.extension.ExtensionInterface;
 import org.eclipse.riena.core.injector.extension.MapName;
 import org.osgi.framework.Bundle;
 
 /**
  * Interface for a NavigationNodeType extension that defines how to create a
  * node or a subtree in the application model tree.
  */
 @ExtensionInterface
 public interface INavigationAssemblyExtension {
 
 	String EXTENSIONPOINT = "org.eclipse.riena.navigation.assemblies"; //$NON-NLS-1$
 
 	IConfigurationElement getConfigurationElement();
 
 	/**
 	 * Return the contributing bundle of the extension.
 	 * 
 	 * @return The contributing bundle
 	 */
 	Bundle getContributingBundle();
 
 	/**
 	 * @return A navigation assembler that creates a node or a subtree for the
 	 *         application model tree.
 	 */
 	@MapName("assembler")
 	INavigationAssembler createNavigationAssembler();
 
 	/**
 	 * @return Same as above but only returns the name of the class of the
 	 *         assembler.
 	 */
 	@MapName("assembler")
 	String getNavigationAssembler();
 
 	/**
 	 * @return ID of the parent indicating where to insert a node or subtree
 	 *         created with this definition in the application model tree.
 	 */
 	String getParentTypeId();
 
 	/**
 	 * @return This assemblies id.
 	 */
 	String getId();
 
 	/**
 	 * @return The index this assembly takes in the system startup sequence. 0
 	 *         or less indicates that automatic startup of this assembly is not
 	 *         desired.
 	 */
 	@MapName("autostartsequence")
 	int getAutostartSequence();
 
 	/**
 	 * @return A subapplication node definition
 	 */
 	@MapName("subapplication")
 	ISubApplicationNodeExtension getSubApplicationNode();
 
 	/**
 	 * @return A module group node definition
 	 */
 	@MapName("modulegroup")
 	IModuleGroupNodeExtension getModuleGroupNode();
 
 	/**
 	 * @return A module node definition
 	 */
 	@MapName("module")
 	IModuleNodeExtension getModuleNode();
 
 	/**
 	 * @return A submodule node definition
 	 */
 	@MapName("submodule")
 	ISubModuleNodeExtension getSubModuleNode();
 
 	/**
 	 * @return The id of the assembly to be included. If this value is not null
	 *         all other attributes or elements must be null.
 	 */
 	String getRef();
 }
