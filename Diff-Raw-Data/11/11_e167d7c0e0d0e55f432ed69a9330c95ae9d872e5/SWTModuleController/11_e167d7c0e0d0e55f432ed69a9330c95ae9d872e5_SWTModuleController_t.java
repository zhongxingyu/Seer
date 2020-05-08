 /*******************************************************************************
  * Copyright (c) 2007, 2010 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.ui.swt.views;
 
 import java.util.List;
 
 import org.eclipse.riena.navigation.IModuleNode;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.listener.ModuleNodeListener;
 import org.eclipse.riena.navigation.listener.NavigationTreeObserver;
 import org.eclipse.riena.navigation.listener.SubModuleNodeListener;
 import org.eclipse.riena.navigation.model.SubModuleNode;
 import org.eclipse.riena.navigation.ui.controllers.ModuleController;
 import org.eclipse.riena.ui.ridgets.ISelectableRidget;
 import org.eclipse.riena.ui.ridgets.ITreeRidget;
 import org.eclipse.riena.ui.ridgets.tree2.ITreeNode;
 import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.lnf.rienadefault.RienaDefaultLnf;
 import org.eclipse.riena.ui.swt.uiprocess.SwtUISynchronizer;
 
 /**
  * Controller of a module.
  */
 public class SWTModuleController extends ModuleController {
 
 	private ITreeRidget tree;
 	private final static String PROPERTY_ENABLED = "enabled"; //$NON-NLS-1$
 	private final static String PROPERTY_VISIBLE = "visible"; //$NON-NLS-1$
 	private final static String PROPERTY_IMAGE = "icon"; //$NON-NLS-1$
 	private final static String PROPERTY_EXPANDED = "expanded"; //$NON-NLS-1$
 
 	private final boolean showOneSubTree;
 	private NavigationTreeObserver navigationTreeObserver;
 
 	/**
 	 * @param navigationNode
 	 */
 	public SWTModuleController(final IModuleNode navigationNode) {
 		super(navigationNode);
 		addListeners();
 		final RienaDefaultLnf lnf = LnfManager.getLnf();
 		showOneSubTree = lnf.getBooleanSetting(LnfKeyConstants.SUB_MODULE_TREE_SHOW_ONE_SUB_TREE, false);
 	}
 
 	/**
 	 * @param tree
 	 *            the tree to set
 	 */
 	public void setTree(final ITreeRidget tree) {
 		this.tree = tree;
 	}
 
 	/**
 	 * @return the tree
 	 */
 	public ITreeRidget getTree() {
 		return tree;
 	}
 
 	@Override
 	public void afterBind() {
 		super.afterBind();
 		updateNavigationNodeMarkers();
 		bindTree();
 	}
 
 	// helping methods
 	//////////////////
 
 	/**
 	 * Adds listeners for sub-module and module nodes.
 	 */
 	private void addListeners() {
 		navigationTreeObserver = new NavigationTreeObserver();
 		navigationTreeObserver.addListener(new ModuleListener());
 		navigationTreeObserver.addListener(new SubModuleListener());
 		navigationTreeObserver.addListenerTo(getNavigationNode());
 	}
 
 	/**
 	 * Binds the tree to a selection model and tree model.
 	 */
 	private void bindTree() {
 		tree.setRootsVisible(false);
 		final INavigationNode<?>[] roots = createTreeRootNodes();
 		tree.bindToModel(roots, SubModuleNode.class, ITreeNode.PROPERTY_CHILDREN, ITreeNode.PROPERTY_PARENT,
 				"label", PROPERTY_ENABLED, PROPERTY_VISIBLE, PROPERTY_IMAGE, null, PROPERTY_EXPANDED); //$NON-NLS-1$
 		tree.setSelectionType(ISelectableRidget.SelectionType.SINGLE);
 		selectActiveNode();
 	}
 
 	/**
 	 * Creates the list of the root nodes of the tree.
 	 * <p>
 	 * This method returns only one root, the module node. So dynamically
 	 * sub-module can be added directly below the module node and they are
 	 * displayed in the tree (without generating and binding a new model with
 	 * new root nodes.)
 	 * 
 	 * @return root nodes
 	 */
 	private IModuleNode[] createTreeRootNodes() {
 		final IModuleNode moduleNode = getNavigationNode();
 		return new IModuleNode[] { moduleNode };
 	}
 
 	private void runAsync(final Runnable op) {
 		new SwtUISynchronizer().asyncExec(op);
 	}
 
 	/**
 	 * Selects the active sub-module of this module in the tree.
 	 */
 	private void selectActiveNode() {
 		setSelectedNode(getNavigationNode());
 	}
 
 	/**
 	 * Ensures that only the sub-tree with the given node is expanded; all other
 	 * trees are collapsed.
 	 * 
 	 * @param activeNode
 	 *            active sub-module node
 	 */
 	private void showOneSubTree(final ISubModuleNode activeNode) {
 
 		if (isShowOneSubTree()) {
 			collapseSibling(activeNode);
 			getNavigationNode().getNavigationProcessor().markNodesToCollapse(activeNode);
 			//			if (!activeNode.isExpanded()) {
 			//				activeNode.setExpanded(true);
 			//			}
 		}
 
 	}
 
 	/**
 	 * Collapses all sibling nodes; also in upper levels. Only nodes that are
 	 * marked as closeSubTree == true are being collapsed automatically.
 	 * 
 	 * @param node
 	 *            sub-module node the active node
 	 */
 	private void collapseSibling(final ISubModuleNode node) {
 		final INavigationNode<?> parent = node.getParent();
 		if (parent == null) {
 			return;
 		}
 		for (final INavigationNode<?> sibling : parent.getChildren()) {
 			if (sibling instanceof ISubModuleNode) {
 				final ISubModuleNode siblingSubModuleNode = (ISubModuleNode) sibling;
 				if (siblingSubModuleNode != node && siblingSubModuleNode.isCloseSubTree()
 						&& siblingSubModuleNode.isExpanded()) {
 					siblingSubModuleNode.setExpanded(false);
 				}
 				siblingSubModuleNode.setCloseSubTree(false);
 				if (siblingSubModuleNode != node && !siblingSubModuleNode.isLeaf()) {
 					collapseChildren(siblingSubModuleNode);
 				}
 				if (parent instanceof ISubModuleNode) {
 					collapseSibling((ISubModuleNode) parent);
 					if (node != siblingSubModuleNode) {
 						collapseChildren(siblingSubModuleNode);
 					}
 				}
 			}
 		}
 
 	}
 
 	private void collapseChildren(final ISubModuleNode node) {
 		final List<ISubModuleNode> childen = node.getChildren();
 		for (final ISubModuleNode child : childen) {
 			if (child.isCloseSubTree() && (child.isExpanded())) {
 				child.setExpanded(false);
 			}
 			child.setCloseSubTree(false);
 			collapseChildren(child);
 		}
 	}
 
 	/**
 	 * Selects the active sub-module in the tree.
 	 * 
 	 * @param node
 	 */
 	private void setSelectedNode(final INavigationNode<?> node) {
 		if (node.isActivated() && (node != getNavigationNode())) {
 			tree.setSelection(node);
 			expandAllParents(node);
 		}
 		for (final INavigationNode<?> child : node.getChildren()) {
 			setSelectedNode(child);
 		}
 	}
 
 	private void expandAllParents(final INavigationNode<?> node) {
 		INavigationNode<?> parent = node.getParent();
 		while (parent instanceof SubModuleNode) {
 			tree.expand(parent);
 			parent = parent.getParent();
 		}
 	}
 
 	/**
 	 * Returns whether only one sub-tree inside the module is expanded at the
 	 * same time.
 	 * 
 	 * @return {@code true} only one sub-tree can be expanded; {@code false}
 	 *         more than one sub-tree can be expanded
 	 */
 	private boolean isShowOneSubTree() {
 		return showOneSubTree;
 	}
 
 	// helping classes
 	//////////////////
 
 	/**
 	 * Updates the tree if a sub-module node is added or remove form parent
 	 * sub-module node.
 	 */
 	private class SubModuleListener extends SubModuleNodeListener {
 		@Override
 		public void afterActivated(final ISubModuleNode source) {
 			// if activation was changed programmatically, we need to select
 			// the activated node (i.e. undo / redo navigation). Since other
 			// activation changes may already be on the event queue, we have
 			// to do this asynchronously (i.e. put this into the end of the 
 			// queue), to preserve the ordering
 			runAsync(new Runnable() {
 				public void run() {
 					selectActiveNode();
 					showOneSubTree(source);
 				}
 			});
 		}
 
 		@Override
 		public void expandedChanged(final ISubModuleNode source) {
 			super.expandedChanged(source);
 			if (tree != null) {
 				if (source.isExpanded()) {
 					tree.expand(source);
 				} else {
 					tree.collapse(source);
 				}
 			}
 		}
 
 		@Override
 		public void childRemoved(final ISubModuleNode source, final ISubModuleNode childRemoved) {
 			super.childRemoved(source, childRemoved);
 			if (tree != null) {
 				if (source.getChildren().size() == 0) {
 					tree.collapse(source);
 					return;
 				}
 				updateTree(childRemoved);
 			}
 
 		}
 
 		@Override
 		public void childAdded(final ISubModuleNode source, final ISubModuleNode childAdded) {
 			super.childAdded(source, childAdded);
 			if (source.getIndexOfChild(childAdded) < source.getChildren().size() - 1) {
 				tree.updateFromModel();
 				return;
 			}
 			updateTree(childAdded);
 		}
 	}
 
 	/**
 	 * updates the tree whenever submodule are added
 	 */
 	private class ModuleListener extends ModuleNodeListener {
 		@Override
 		public void childAdded(final IModuleNode source, final ISubModuleNode childAdded) {
 			super.childAdded(source, childAdded);
 			if (source.getIndexOfChild(childAdded) < source.getChildren().size() - 1) {
 				tree.updateFromModel();
 				return;
 			}
 			updateTree(childAdded);
 		}
 
 		@Override
 		public void presentationChanged(final IModuleNode source) {
 			if (!getNavigationNode().getNavigationNodeController().equals(SWTModuleController.this)) {
 				navigationTreeObserver.removeListenerFrom(getNavigationNode());
 			}
 		}
 
 	}
 
 	private void updateTree(final ISubModuleNode source) {
 		if (tree == null || !tree.isVisible()) {
 			return;
 		}
 		if (source == null || !source.isVisible()) {
 			return;
 		}
 		final IModuleNode moduleNode = source.getParentOfType(IModuleNode.class);
		if (moduleNode != null && !moduleNode.isActivated()) {
 			return;
 		}
 		tree.updateFromModel();
 	}
 }
