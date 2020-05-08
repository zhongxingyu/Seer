 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.model;
 
 import org.eclipse.riena.navigation.IModuleNode;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.navigation.listener.IModuleNodeListener;
 
 /**
  * Default implementation for the module node
  */
 public class ModuleNode extends NavigationNode<IModuleNode, ISubModuleNode, IModuleNodeListener> implements IModuleNode {
 
 	private boolean presentSingleSubModule;
 	private boolean closeable;
 
 	/**
 	 * Creates a ModuleNode.
 	 * 
 	 */
 	public ModuleNode() {
 		super(null);
 		initialize();
 	}
 
 	public Class<ISubModuleNode> getValidChildType() {
 		return ISubModuleNode.class;
 	}
 
 	/**
 	 * Creates a ModuleNode.
 	 * 
 	 * @param nodeId
 	 *            Identifies the node in the application model tree.
 	 */
 	public ModuleNode(NavigationNodeId nodeId) {
 		super(nodeId);
 		initialize();
 	}
 
 	/**
 	 * Creates a ModuleNode.
 	 * 
 	 * @param nodeId
 	 *            Identifies the node in the application model tree.
 	 * @param label
 	 *            Label of the module displayed in the modules title bar.
 	 */
 	public ModuleNode(NavigationNodeId nodeId, String label) {
 		super(nodeId, label);
 		initialize();
 	}
 
 	/**
 	 * Creates a ModuleNode.
 	 * 
 	 * @param label
 	 *            Label of the module displayed in the modules title bar.
 	 */
 	public ModuleNode(String label) {
 		this(null, label);
 	}
 
 	/**
 	 * Initializes the properties of the module.
 	 */
 	private void initialize() {
 		presentSingleSubModule = false;
 		closeable = true;
 	}
 
 	/**
 	 * @return the presentSingleSubModule
 	 */
 	public boolean isPresentSingleSubModule() {
 		return presentSingleSubModule;
 	}
 
 	/**
 	 * @param presentSingleSubModule
 	 *            the presentSingleSubModule to set
 	 */
 	public void setPresentSingleSubModule(boolean presentSingleSubModule) {
 		this.presentSingleSubModule = presentSingleSubModule;
 		notifyPresentSingleSubModuleChanged();
 	}
 
 	private void notifyPresentSingleSubModuleChanged() {
		for (IModuleNodeListener next : getListeners()) {
			next.presentSingleSubModuleChanged(this);
 		}
 	}
 
 	public boolean isPresentSubModules() {
 		return isPresentSingleSubModule() || !(getChildren().size() == 1 && getChild(0).getChildren().isEmpty());
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.IModuleNode#calcDepth()
 	 */
 	public int calcDepth() {
 
 		if (!isPresentSubModules()) {
 			return 0;
 		}
 
 		return calcDepth(this);
 
 	}
 
 	/**
 	 * Calculates the number of the visible and expanded children below the
 	 * given node.
 	 * 
 	 * @param node
 	 *            - start node
 	 * @return number of children
 	 */
 	private int calcDepth(INavigationNode<?> node) {
 
 		int depth = 0;
 		if ((node == this) || node.isExpanded()) {
 			for (INavigationNode<?> child : node.getChildren()) {
 				if (child.isVisible()) {
 					depth++;
 					depth += calcDepth(child);
 				}
 			}
 		}
 
 		return depth;
 
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.IModuleNode#isClosable()
 	 */
 	public boolean isClosable() {
 		return closeable;
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.IModuleNode#setClosable(boolean)
 	 */
 	public void setClosable(boolean closeable) {
 		this.closeable = closeable;
 	}
 
 }
