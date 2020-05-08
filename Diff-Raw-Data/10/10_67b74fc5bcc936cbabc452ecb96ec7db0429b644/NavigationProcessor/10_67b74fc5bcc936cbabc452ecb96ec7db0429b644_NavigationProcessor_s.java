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
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 import java.util.Vector;
 
import org.eclipse.equinox.log.Logger;

import org.eclipse.riena.core.Log4r;
 import org.eclipse.riena.core.marker.IMarker;
import org.eclipse.riena.internal.navigation.Activator;
 import org.eclipse.riena.navigation.IModuleNode;
 import org.eclipse.riena.navigation.INavigationContext;
 import org.eclipse.riena.navigation.INavigationHistory;
 import org.eclipse.riena.navigation.INavigationHistoryEvent;
 import org.eclipse.riena.navigation.INavigationHistoryListener;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.INavigationNodeProvider;
 import org.eclipse.riena.navigation.INavigationProcessor;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.NavigationArgument;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.ui.core.marker.DisabledMarker;
 import org.eclipse.riena.ui.core.marker.HiddenMarker;
 
 /**
  * Default implementation for the navigation processor
  */
 public class NavigationProcessor implements INavigationProcessor, INavigationHistory {
 
	private static final Logger LOGGER = Log4r.getLogger(Activator.getDefault(), NavigationProcessor.class);
 	private static int maxStacksize = 40;
 	private Stack<INavigationNode<?>> histBack = new Stack<INavigationNode<?>>();
 	private Stack<INavigationNode<?>> histForward = new Stack<INavigationNode<?>>();
 	private Map<INavigationNode<?>, INavigationNode<?>> navigationMap = new HashMap<INavigationNode<?>, INavigationNode<?>>();
 	private List<INavigationHistoryListener> navigationListener = new Vector<INavigationHistoryListener>();
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationProcessor#activate(org.eclipse.riena.navigation.INavigationNode)
 	 */
 	public void activate(INavigationNode<?> toActivate) {
 		if (toActivate != null) {
 			if (toActivate.isActivated()) {
 				// do nothing
 				// if toActivate is module, module group or sub application
 				// the same sub module will be activated on activation of
 				// the toActivate, in any case there is nothing to do
 			} else {
 				if (!toActivate.isVisible() || !toActivate.isEnabled()) {
 					return;
 				}
 				// 1.find the chain to activate
 				// 2.find the chain to deactivate
 				// 3.check the deactivation chain
 				// 4.check the activation chain
 				// 5. do the deactivation chain
 				// 6. hande node on the histBack
 				// 7. do the activation chain
 				List<INavigationNode<?>> toActivateList = getNodesToActivateOnActivation(toActivate);
 				List<INavigationNode<?>> toDeactivateList = getNodesToDeactivateOnActivation(toActivate);
 				INavigationContext navigationContext = new NavigationContext(toActivateList, toDeactivateList);
 				if (allowsDeactivate(navigationContext)) {
 					if (allowsActivate(navigationContext)) {
 						deactivate(navigationContext);
 						activate(navigationContext);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Remembers the node to push onto the navigation history stack. A maximum
 	 * of MAX_STACKSIZE elements are stored. Older elements are removed from the
 	 * stack.
 	 * 
 	 * @param toActivate
 	 */
 	private void buildHistory(INavigationNode<?> toActivate) {
 		// filter out unnavigatable nodes
 		if (!(toActivate instanceof ISubModuleNode) || toActivate.isDisposed()) {
 			return;
 		}
 		if (histBack.isEmpty() || !histBack.peek().equals(toActivate)) {
 			histBack.push(toActivate);
 			// limit the stack size and remove older elements
 			if (histBack.size() > maxStacksize) {
 				histBack.remove(histBack.firstElement());
 			}
 			fireBackHistoryChangedEvent();
 		}
 		// is forewarding history top stack element equals node toActivate,
 		// remove it!
 		if (!histForward.isEmpty() && histForward.peek().equals(toActivate)) {
 			histForward.pop();// remove newest node
 			fireForewardHistoryChangedEvent();
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationProcessor#dispose(org.eclipse.riena.navigation.INavigationNode)
 	 */
 	public void dispose(INavigationNode<?> toDispose) {
 		// 1. check which nodes are active from the node toDispose and all its
 		// children must be deactivated
 		// 2. find nodes to activate automatically
 		// 3. check if the new nodes can be activated
 		// 4. deactivate the previously active nodes
 		// 5. dispose the nodes
 		// 6. remove the nodes from the tree
 		// 7. activate the other node
 		// Special handling: if the node toDispose is the first module in a
 		// moduleGroup,
 		// than the whole Module Group has to be disposed
 		// if there was no sub module active in the module,
 		// than no other module has to be activated
 		INavigationNode<?> nodeToDispose = getNodeToDispose(toDispose);
 		if (nodeToDispose != null && !nodeToDispose.isDisposed()) {
 			List<INavigationNode<?>> toDeactivateList = getNodesToDeactivateOnDispose(nodeToDispose);
 			List<INavigationNode<?>> toActivateList = getNodesToActivateOnDispose(nodeToDispose);
 			INavigationContext navigationContext = new NavigationContext(toActivateList, toDeactivateList);
 			if (allowsDeactivate(navigationContext)) {
 				if (allowsDispose(navigationContext)) {
 					if (allowsActivate(navigationContext)) {
 						deactivate(navigationContext);
 						dispose(navigationContext);
 						activate(navigationContext);
 					}
 				}
 			}
 			cleanupHistory(nodeToDispose);
 		}
 	}
 
 	public void addMarker(INavigationNode<?> node, IMarker marker) {
 
 		if (node != null) {
 			if (node.isActivated() && (marker instanceof DisabledMarker || marker instanceof HiddenMarker)) {
 				INavigationNode<?> nodeToHide = getNodeToDispose(node);
 				// if ((nodeToHide != null) && (nodeToHide.isVisible() && (nodeToHide.isEnabled()))) {
 				if (nodeToHide != null) {
 					List<INavigationNode<?>> toDeactivateList = getNodesToDeactivateOnDispose(nodeToHide);
 					List<INavigationNode<?>> toActivateList = getNodesToActivateOnDispose(nodeToHide);
 					INavigationContext navigationContext = new NavigationContext(toActivateList, toDeactivateList);
 					if (allowsDeactivate(navigationContext) && allowsActivate(navigationContext)) {
 						deactivate(navigationContext);
 						activate(navigationContext);
 					} else {
 						return;
 					}
 				}
 			}
 			if (marker instanceof DisabledMarker || marker instanceof HiddenMarker) {
 				node.setSelected(false);
 			}
 
 			List<INavigationNode<?>> toMarkList = new LinkedList<INavigationNode<?>>();
 			toMarkList.add(node);
 			List<INavigationNode<?>> emptyList = new LinkedList<INavigationNode<?>>();
 			INavigationContext navigationContext = new NavigationContext(toMarkList, emptyList);
 			addMarker(navigationContext, marker);
 		}
 
 	}
 
 	private void addMarker(INavigationContext context, IMarker marker) {
 		for (INavigationNode<?> node : context.getToActivate()) {
 			node.addMarker(context, marker);
 		}
 	}
 
 	/**
 	 * Cleanup the History stacks and removes all occurrences of the node.
 	 * 
 	 * @param toDispose
 	 */
 	private void cleanupHistory(INavigationNode<?> toDispose) {
 		boolean bhc = false;
 		while (histBack.contains(toDispose)) {
 			histBack.remove(toDispose);
 			bhc = true;
 		}
 		if (bhc) {
 			fireBackHistoryChangedEvent();
 		}
 		boolean fhc = false;
 		while (histForward.contains(toDispose)) {
 			histForward.remove(toDispose);
 			fhc = true;
 		}
 		if (fhc) {
 			fireForewardHistoryChangedEvent();
 		}
 		if (navigationMap.containsKey(toDispose)) {
 			navigationMap.remove(toDispose);
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationProcessor#create(org.eclipse.riena.navigation.INavigationNode,
 	 *      org.eclipse.riena.navigation.NavigationNodeId)
 	 */
 	public void create(INavigationNode<?> sourceNode, NavigationNodeId targetId) {
 		provideNode(sourceNode, targetId, null);
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationProcessor#navigate(org.eclipse.riena.navigation.INavigationNode,
 	 *      org.eclipse.riena.navigation.NavigationNodeId,
 	 *      org.eclipse.riena.navigation.NavigationArgument)
 	 */
 	public void navigate(final INavigationNode<?> sourceNode, final NavigationNodeId targetId,
 			final NavigationArgument navigation) {
 		// TODO see https://bugs.eclipse.org/bugs/show_bug.cgi?id=261832
 		//		if (navigation != null && navigation.isNavigateAsync()) {
 		//			navigateAsync(sourceNode, targetId, navigation);
 		//		} else {
 		navigateSync(sourceNode, targetId, navigation);
 		//		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationProcessor#navigate(org.eclipse.riena.navigation.INavigationNode,
 	 *      org.eclipse.riena.navigation.NavigationNodeId,
 	 *      org.eclipse.riena.navigation.NavigationArgument)
 	 */
 	private void navigateSync(final INavigationNode<?> sourceNode, final NavigationNodeId targetId,
 			final NavigationArgument navigation) {
 		INavigationNode<?> targetNode = provideNode(sourceNode, targetId, navigation);
 		if (targetNode == null) {
 			return;
 		}
 		INavigationNode<?> activateNode = targetNode.findNode(targetId);
 		if (activateNode != null) {
 			navigationMap.put(activateNode, sourceNode);
 			activateNode.activate();
 		} else {
 			navigationMap.put(targetNode, sourceNode);
 			targetNode.activate();
 		}
 	}
 
 	/**
 	 * 
 	 * @see org.eclipse.riena.navigation.INavigationProcessor#navigate(org.eclipse.riena.navigation.INavigationNode,
 	 *      org.eclipse.riena.navigation.NavigationNodeId,
 	 *      org.eclipse.riena.navigation.NavigationArgument)
 	 */
 
 	// TODO see https://bugs.eclipse.org/bugs/show_bug.cgi?id=261832
 	//	private void navigateAsync(final INavigationNode<?> sourceNode, final NavigationNodeId targetId,
 	//
 	//	final NavigationArgument navigation) {
 	//
 	//		final boolean debug = LOGGER.isLoggable(LogService.LOG_DEBUG);
 	//
 	//		if (debug) {
 	//			LOGGER.log(LogService.LOG_DEBUG, "async navigation to " + targetId + " started..."); //$NON-NLS-1$ //$NON-NLS-2$
 	//		}
 	//
 	//		UIProcess p = new UIProcess("navigate", true, sourceNode) { //$NON-NLS-1$
 	//			private INavigationNode<?> targetNode;
 	//			private final long startTime = System.currentTimeMillis();
 	//
 	//			/*
 	//			 * (non-Javadoc)
 	//			 * 
 	//			 * @see
 	//			 * org.eclipse.riena.ui.core.uiprocess.UIProcess#runJob(org.eclipse
 	//			 * .core.runtime.IProgressMonitor)
 	//			 */
 	//			@Override
 	//			public boolean runJob(IProgressMonitor monitor) {
 	//
 	//				targetNode = provideNode(sourceNode, targetId, navigation);
 	//
 	//				return true;
 	//			}
 	//
 	//			private void activateOrFlash(INavigationNode<?> activationNode) {
 	//
 	//				if (System.currentTimeMillis() - startTime < 200) {
 	//					activationNode.activate();
 	//				} else {
 	//					// TODO FLASHING but no activation
 	//				}
 	//			}
 	//
 	//			@Override
 	//			public void finalUpdateUI() {
 	//
 	//				if (targetNode != null) {
 	//					INavigationNode<?> activateNode = targetNode.findNode(targetId);
 	//					if (activateNode == null) {
 	//						activateNode = targetNode;
 	//					}
 	//
 	//					navigationMap.put(activateNode, sourceNode);
 	//					activateOrFlash(activateNode);
 	//				}
 	//
 	//				if (debug) {
 	//					LOGGER.log(LogService.LOG_DEBUG, "async navigation to " + targetId + " completed"); //$NON-NLS-1$//$NON-NLS-2$
 	//				}
 	//			}
 	//
 	//			@Override
 	//			protected int getTotalWork() {
 	//				return 10;
 	//			}
 	//		};
 	//
 	//		// TODO must be set?
 	//		p.setNote("sample uiProcess note"); //$NON-NLS-1$ 
 	//		p.setTitle("sample uiProcess title"); //$NON-NLS-1$
 	//		p.start();
 	//	}
 	private INavigationNode<?> provideNode(INavigationNode<?> sourceNode, NavigationNodeId targetId,
 			NavigationArgument argument) {
 
 		return getNavigationNodeProvider().provideNode(sourceNode, targetId, argument);
 	}
 
 	protected INavigationNodeProvider getNavigationNodeProvider() {
 		return NavigationNodeProviderAccessor.getNavigationNodeProvider();
 	}
 
 	/**
 	 * Ascertain the correct node to dispose. If e.g. the first module in a
 	 * Group is disposed, than the whole group has to be disposed
 	 * 
 	 * @param toDispose
 	 * @return the correct node to dispose
 	 */
 	private INavigationNode<?> getNodeToDispose(INavigationNode<?> toDispose) {
 		IModuleNode moduleNode = toDispose.getTypecastedAdapter(IModuleNode.class);
 		if (moduleNode != null) {
 			INavigationNode<?> parent = moduleNode.getParent();
 			if (parent != null) {
 				int ind = parent.getIndexOfChild(moduleNode);
 				if (ind == 0) {
 					return parent;
 				}
 			}
 		}
 		return toDispose;
 	}
 
 	/**
 	 * Find all nodes which have to deactivated and disposed with the passed
 	 * node. These are all children and its children, in backward order
 	 * 
 	 * @param toDispose
 	 *            the node to dispose
 	 * @return
 	 */
 	private List<INavigationNode<?>> getNodesToDeactivateOnDispose(INavigationNode<?> toDispose) {
 		List<INavigationNode<?>> allToDispose = new LinkedList<INavigationNode<?>>();
 		addAllChildren(allToDispose, toDispose);
 		return allToDispose;
 	}
 
 	private void addAllChildren(List<INavigationNode<?>> allToDispose, INavigationNode<?> toDispose) {
 		for (Object nextChild : toDispose.getChildren()) {
 			addAllChildren(allToDispose, (INavigationNode<?>) nextChild);
 		}
 		allToDispose.add(toDispose);
 	}
 
 	/**
 	 * Find a node or list of nodes which have to activated when a specified
 	 * node is disposed
 	 * 
 	 * @param toDispose
 	 *            - the node to dispose
 	 * @return return a list of nodes
 	 */
 	private List<INavigationNode<?>> getNodesToActivateOnDispose(INavigationNode<?> toDispose) {
 		// on dispose must only then something be activated, if one of the
 		// modules to dispose was active
 		// we assume, that if any submodule of this module was active,
 		// than this module is active itself
 		// while dispose of a node, the next brother node must be activated
 		if (toDispose.isActivated()) {
 			INavigationNode<?> parentOfToDispose = toDispose.getParent();
 			INavigationNode<?> brotherToActivate = null;
 			if (parentOfToDispose != null) {
 				List<?> childrenOfParentOfToDispose = parentOfToDispose.getChildren();
 				List<INavigationNode<?>> activateableNode = getActivateableNodes(childrenOfParentOfToDispose);
 				if (childrenOfParentOfToDispose.size() > 1) {
 					// there must be a least 2 children: the disposed will be
 					// removed
 					// get the first child which is not the one to remove
 					for (INavigationNode<?> nextChild : activateableNode) {
 						if (!nextChild.equals(toDispose)) {
 							brotherToActivate = nextChild;
 							break;
 						}
 					}
 				}
 			}
 			if (brotherToActivate != null) {
 				return getNodesToActivateOnActivation(brotherToActivate);
 			}
 		}
 		return new LinkedList<INavigationNode<?>>();
 	}
 
 	/**
 	 * Removes all not activateable nodes (e.g. hidden nodes) from the given
 	 * list.
 	 * 
 	 * @param nodes
 	 *            - list of node
 	 * @return filtered list
 	 */
 	private List<INavigationNode<?>> getActivateableNodes(List<?> nodes) {
 		List<INavigationNode<?>> activateableNodes = new LinkedList<INavigationNode<?>>();
 		for (Object node : nodes) {
 			if (node instanceof INavigationNode<?>) {
 				INavigationNode<?> naviNode = (INavigationNode<?>) node;
 				if (naviNode.isVisible() && naviNode.isEnabled()) {
 					activateableNodes.add(naviNode);
 				}
 			}
 		}
 		return activateableNodes;
 	}
 
 	/**
 	 * Finds all the nodes to activate
 	 * 
 	 * @param toActivate
 	 *            - the node do activate
 	 * @return a List of all nodes to activate
 	 */
 	private List<INavigationNode<?>> getNodesToActivateOnActivation(INavigationNode<?> toActivate) {
 		List<INavigationNode<?>> nodesToActivate = new LinkedList<INavigationNode<?>>();
 		// go up and add all parents
 		addParentsToActivate(nodesToActivate, toActivate);
 		// go down and add all children
 		addChildrenToActivate(nodesToActivate, toActivate);
 		// the first element in the list must be the last not active parent
 		return nodesToActivate;
 	}
 
 	private void addParentsToActivate(List<INavigationNode<?>> nodesToActivate, INavigationNode<?> toActivate) {
 		// go up to the next active parent
 		INavigationNode<?> parent = getActivationParent(toActivate);
 		if (parent != null) {
 			if (parent.isActivated()) {
 				nodesToActivate.add(toActivate);
 			} else {
 				addParentsToActivate(nodesToActivate, parent);
 				nodesToActivate.add(toActivate);
 			}
 		} else {
 			nodesToActivate.add(toActivate);
 		}
 	}
 
 	private INavigationNode<?> getActivationParent(INavigationNode<?> child) {
 		// by a subModule is it the module node
 		// by all other die direct parent
 		ISubModuleNode subModuleNode = child.getTypecastedAdapter(ISubModuleNode.class);
 		if (subModuleNode != null) {
 			INavigationNode<?> parent = child.getParent();
 			while (parent != null) {
 				if (parent.getTypecastedAdapter(IModuleNode.class) != null) {
 					return parent;
 				} else {
 					parent = parent.getParent();
 				}
 			}
 		} else {
 			return child.getParent();
 		}
 		return null;
 
 	}
 
 	private void addChildrenToActivate(List<INavigationNode<?>> nodesToActivate, INavigationNode<?> toActivate) {
 		INavigationNode<?> childToActivate = getChildToActivate(toActivate);
 		if (childToActivate != null) {
 			nodesToActivate.add(childToActivate);
 			addChildrenToActivate(nodesToActivate, childToActivate);
 		}
 	}
 
 	private List<INavigationNode<?>> getNodesToDeactivateOnActivation(INavigationNode<?> toActivate) {
 		// get next active parent or root
 		List<INavigationNode<?>> nodesToDeactivate = new LinkedList<INavigationNode<?>>();
 		INavigationNode<?> activeParent = getNextActiveParent(toActivate);
 		if (activeParent != null) {
 			//
 			// EAC: fix for bug
 			// http://www.spiritframework.de/bugzilla/show_bug.cgi?id=67
 			// This fix handles the case that no active child is available due
 			// to some errors or exceptions which occurred before
 			//
 			INavigationNode<?> activeChild = getActiveChild(activeParent);
 			if (activeChild != null) {
 				addChildrenToDeactivate(nodesToDeactivate, activeChild);
 			}
 		} else {
 			// no one was active before
 			addChildrenToDeactivate(nodesToDeactivate, getTopParent(toActivate));
 		}
 		return nodesToDeactivate;
 
 	}
 
 	private void addChildrenToDeactivate(List<INavigationNode<?>> nodesToDeactivate, INavigationNode<?> toAdd) {
 		INavigationNode<?> activeChild = getActiveChild(toAdd);
 		if (activeChild != null) {
 			addChildrenToDeactivate(nodesToDeactivate, activeChild);
 			nodesToDeactivate.add(toAdd);
 		} else {
 			nodesToDeactivate.add(toAdd);
 		}
 
 	}
 
 	/**
 	 * find the next active parent
 	 */
 	private INavigationNode<?> getNextActiveParent(INavigationNode<?> node) {
 		// the next active parent must be at least a Module Node
 		ISubModuleNode subModuleNode = node.getTypecastedAdapter(ISubModuleNode.class);
 		if (subModuleNode != null) {
 			// if sub module node go up
 			return getNextActiveParent(subModuleNode.getParent());
 		} else if (node.isActivated()) {
 			// it is not a sub module node and is activated
 			return node;
 		} else if (node.getParent() != null) {
 			// it is not a sub module and is not active
 			return getNextActiveParent(node.getParent());
 		} else {
 			return null;
 		}
 
 	}
 
 	/**
 	 * find the top parent
 	 */
 
 	private INavigationNode<?> getTopParent(INavigationNode<?> node) {
 		if (node.getParent() != null) {
 			return getTopParent(node.getParent());
 		} else {
 			return node;
 		}
 
 	}
 
 	private boolean allowsActivate(INavigationContext context) {
 		for (INavigationNode<?> nextToActivate : context.getToActivate()) {
 			if (!nextToActivate.allowsActivate(context)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private boolean allowsDispose(INavigationContext context) {
 		for (INavigationNode<?> nextToDeactivate : context.getToDeactivate()) {
 			if (!nextToDeactivate.allowsDispose(context)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private boolean allowsDeactivate(INavigationContext context) {
 		for (INavigationNode<?> nextToDeactivate : context.getToDeactivate()) {
 			if (!nextToDeactivate.allowsDeactivate(context)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private void activate(INavigationContext context) {
 		for (INavigationNode<?> nextToActivate : context.getToActivate()) {
 			nextToActivate.onBeforeActivate(context);
 		}
 		for (INavigationNode<?> nextToActivate : context.getToActivate()) {
 			nextToActivate.activate(context);
 			setAsSelectedChild(nextToActivate);
 		}
 		for (INavigationNode<?> nextToActivate : copyReverse(context.getToActivate())) {
 			nextToActivate.onAfterActivate(context);
 		}
 	}
 
 	private void deactivate(INavigationContext context) {
 		Collection<INavigationNode<?>> previouslyActivatedNodes = new ArrayList<INavigationNode<?>>();
 		for (INavigationNode<?> nextToDeactivate : copyReverse(context.getToDeactivate())) {
 			if (nextToDeactivate.isActivated()) {
 				previouslyActivatedNodes.add(nextToDeactivate);
 				nextToDeactivate.onBeforeDeactivate(context);
 			}
 		}
 		for (INavigationNode<?> nextToDeactivate : context.getToDeactivate()) {
 			// check for activated to make this method usable for disposing
 			if (previouslyActivatedNodes.contains(nextToDeactivate)) {
 				nextToDeactivate.deactivate(context);
 			}
 		}
 		for (INavigationNode<?> nextToDeactivate : context.getToDeactivate()) {
 			if (previouslyActivatedNodes.contains(nextToDeactivate)) {
 				nextToDeactivate.onAfterDeactivate(context);
 			}
 		}
 	}
 
 	private void dispose(INavigationContext context) {
 		for (INavigationNode<?> nextToDispose : copyReverse(context.getToDeactivate())) {
 			nextToDispose.onBeforeDispose(context);
 		}
 		for (INavigationNode<?> nextToDispose : context.getToDeactivate()) {
 			// check for activated to make this method usable for disposing
 			nextToDispose.dispose(context);
 			// remove the node from tree
 			INavigationNode<?> parent = nextToDispose.getParent();
 			if (parent != null) {
 				parent.removeChild(nextToDispose);
 			}
 		}
 		for (INavigationNode<?> nextToDispose : context.getToDeactivate()) {
 			nextToDispose.onAfterDispose(context);
 			// clean up history stacks
 			cleanupHistory(nextToDispose);
 		}
 	}
 
 	private static class NavigationContext implements INavigationContext {
 
 		private List<INavigationNode<?>> toDeactivate;
 		private List<INavigationNode<?>> toActivate;
 
 		/**
 		 * @param toDeactivate
 		 * @param toActivate
 		 */
 		public NavigationContext(List<INavigationNode<?>> toActivate, List<INavigationNode<?>> toDeactivate) {
 			super();
 			this.toDeactivate = toDeactivate;
 			this.toActivate = toActivate;
 		}
 
 		/**
 		 * @see org.eclipse.riena.navigation.INavigationContext#getToActivate()
 		 */
 		public List<INavigationNode<?>> getToActivate() {
 			return toActivate;
 		}
 
 		/**
 		 * @see org.eclipse.riena.navigation.INavigationContext#getToDeactivate()
 		 */
 		public List<INavigationNode<?>> getToDeactivate() {
 			return toDeactivate;
 		}
 
 	}
 
 	/**
 	 * The navigation processor decides which child to activate even initially
 	 * 
 	 * @param pNode
 	 *            - the node who's child is searched
 	 */
 	private INavigationNode<?> getChildToActivate(INavigationNode<?> pNode) {
 		// for sub module is always null
 		ISubModuleNode subModuleNode = pNode.getTypecastedAdapter(ISubModuleNode.class);
 		if (subModuleNode != null) {
 			return null;
 		}
 		// for module node is it the deepest selected sub Module node
 		IModuleNode moduleNode = pNode.getTypecastedAdapter(IModuleNode.class);
 		if (moduleNode != null) {
 			ISubModuleNode nextChild = getSelectedChild(moduleNode);
 			if (nextChild != null) {
 				while (true) {
 					ISubModuleNode nextTmp = getSelectedChild(nextChild);
 					if (nextTmp != null) {
 						nextChild = nextTmp;
 					} else {
 						return nextChild;
 					}
 				}
 			} else {
 				if (moduleNode.getChildren().size() > 0) {
 					return moduleNode.getChild(0);
 				} else {
 					return null;
 				}
 			}
 		}
 		// for all others is it the direct selected child
 		INavigationNode<?> nextSelectedChild = getSelectedChild(pNode);
 		if (nextSelectedChild != null) {
 			return nextSelectedChild;
 		} else {
 			for (INavigationNode<?> next : pNode.getChildren()) {
 				if (next.isVisible() && next.isEnabled()) {
 					return next;
 				}
 			}
 			return null;
 		}
 	}
 
 	private INavigationNode<?> getActiveChild(INavigationNode<?> pNode) {
 		// for a Sub Module it is always null
 		ISubModuleNode subModuleNode = pNode.getTypecastedAdapter(ISubModuleNode.class);
 		if (subModuleNode != null) {
 			return null;
 		}
 		// for a module node it is the last selected child
 		IModuleNode moduleNode = pNode.getTypecastedAdapter(IModuleNode.class);
 		if (moduleNode != null) {
 			ISubModuleNode nextChild = getSelectedChild(moduleNode);
 			if (nextChild != null) {
 				while (nextChild != null) {
 					if (nextChild.isActivated()) {
 						return nextChild;
 					} else if (getSelectedChild(nextChild) != null) {
 						nextChild = getSelectedChild(nextChild);
 					} else {
 						return null;
 					}
 				}
 			} else {
 				return null;
 			}
 		}
 		// for all other the selectedChild if any and active
 		INavigationNode<?> nextSelectedChild = getSelectedChild(pNode);
 		if (nextSelectedChild != null && nextSelectedChild.isActivated()) {
 			return nextSelectedChild;
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * since the navigation processor decides, who is the next child to
 	 * activate: so the processor can jump over nodes and decides which nodes
 	 * are deactivated, the navigation processor also must set the selected
 	 * chain. While activation the navigation processor works with the selected
 	 * chain to find which nodes have to be activated an which to be
 	 * deactivated. The selected chain must always show the way from the to
 	 * element to the active one
 	 * 
 	 * @param pNode
 	 *            the node to set the selected chain for.
 	 */
 	private void setAsSelectedChild(INavigationNode<?> pNode) {
 
 		// when activating a sub module the chain must be set up to the module
 		// node
 		ISubModuleNode subModuleNode = pNode.getTypecastedAdapter(ISubModuleNode.class);
 		if (subModuleNode != null) {
 			ISubModuleNode nextToSet = subModuleNode;
 			while (nextToSet != null) {
 				if (nextToSet.getParent() != null) {
 					setSelectedChild(nextToSet.getParent(), nextToSet);
 					nextToSet = nextToSet.getParent().getTypecastedAdapter(ISubModuleNode.class);
 				}
 			}
 			// when a sub module node is activated it sets its own selected
 			// child to null to break the selected chain
 			setSelectedChild(subModuleNode, null);
 			return;
 		}
 		// for all others only to the next parent
 		if (pNode.getParent() != null) {
 			setSelectedChild(pNode.getParent(), pNode);
 		}
 
 	}
 
 	/**
 	 * this navigation processor allows only one selected child, so it resets
 	 * the flag in all children before marking the one
 	 * 
 	 * @param parent
 	 *            - the parent to reset in
 	 * @param child
 	 *            - the child to set as selected
 	 */
 	private void setSelectedChild(INavigationNode<?> parent, INavigationNode<?> child) {
 		for (INavigationNode<?> next : parent.getChildren()) {
 			if (next.equals(child)) {
 				next.setSelected(true);
 				if (next.isActivated()) {//remember only active subModule nodes
 					//We have a new selected child. Remember in history.
 					buildHistory(next);
 				}
 			} else {
 				next.setSelected(false);
 			}
 		}
 	}
 
 	private INavigationNode<?> getSelectedChild(INavigationNode<?> pNavigationNode) {
 		for (INavigationNode<?> next : pNavigationNode.getChildren()) {
 			if (next.isSelected()) {
 				return next;
 			}
 		}
 		return null;
 	}
 
 	private ISubModuleNode getSelectedChild(IModuleNode pModuleNode) {
 		for (ISubModuleNode next : pModuleNode.getChildren()) {
 			if (next.isSelected()) {
 				return next;
 			}
 		}
 		return null;
 	}
 
 	private ISubModuleNode getSelectedChild(ISubModuleNode pSubModuleNode) {
 		for (ISubModuleNode next : pSubModuleNode.getChildren()) {
 			if (next.isSelected()) {
 				return next;
 			}
 		}
 		return null;
 	}
 
 	private List<INavigationNode<?>> copyReverse(List<INavigationNode<?>> list) {
 
 		List<INavigationNode<?>> listReverse = list.subList(0, list.size());
 		Collections.reverse(listReverse);
 
 		return listReverse;
 	}
 
 	/**
 	 * Navigates one step back in the navigation history
 	 * 
 	 * @see org.eclipse.riena.navigation.INavigationNode#navigateHistoryBack()
 	 */
 	public void historyBack() {
 		if (getHistoryBackSize() > 0) {
 			INavigationNode<?> current = histBack.pop();// skip self
 			fireBackHistoryChangedEvent();
 			histForward.push(current);
 			if (histForward.size() > maxStacksize) {
 				histForward.remove(histForward.firstElement());
 			}
 			fireForewardHistoryChangedEvent();
 			INavigationNode<?> node = histBack.peek();// activate parent
 			if (node != null) {
 				activate(node);
 			}
 		}
 	}
 
 	/**
 	 * Fires a INavigationHistoryEvent when the backward history changes.
 	 */
 	private void fireBackHistoryChangedEvent() {
 		if (navigationListener.size() == 0) {
 			return;
 		}
 		INavigationHistoryEvent event = new NavigationHistoryEvent(histBack.subList(0, histBack.size() - 1));
 		for (INavigationHistoryListener listener : navigationListener) {
 			listener.backHistoryChanged(event);
 		}
 	}
 
 	/**
 	 * Navigates one step forward in the navigation history
 	 * 
 	 * @see org.eclipse.riena.navigation.INavigationNode#navigateHistoryBack()
 	 */
 	public void historyForward() {
 		if (getHistoryForwardSize() > 0) {
 			INavigationNode<?> current = histForward.pop();
 			fireForewardHistoryChangedEvent();
 			if (current != null) {
 				histBack.push(current);
 				if (histBack.size() > maxStacksize) {
 					histBack.remove(histBack.firstElement());
 				}
 				activate(current);
 				fireBackHistoryChangedEvent();
 			}
 		}
 	}
 
 	/**
 	 * Fires a INavigationHistoryEvent when the forward history changes.
 	 */
 	private void fireForewardHistoryChangedEvent() {
 		if (navigationListener.size() == 0) {
 			return;
 		}
 		INavigationHistoryEvent event = new NavigationHistoryEvent(histForward.subList(0, histForward.size()));
 		for (INavigationHistoryListener listener : navigationListener) {
 			listener.forwardHistoryChanged(event);
 		}
 	}
 
 	/**
 	 * Answer the current size of the next navigation history
 	 * 
 	 * @see org.eclipse.riena.navigation.INavigationNode#getHistorySize()
 	 * @return the amount of navigation nodes on the navigation stack
 	 */
 	public int getHistoryBackSize() {
 		return histBack.size() - 1;
 	}
 
 	/**
 	 * Answers the currently selected navigation node in the NavigationTree
 	 * 
 	 * @return the currently selected SubModuleNode in the NavigationTree or
 	 *         null
 	 */
 	public INavigationNode<?> getSelectedNode() {
 		//always the top most histback item is the currently selected
 		return histBack.peek();
 	}
 
 	/**
 	 * Answer the current size of the previous navigation history
 	 * 
 	 * @see org.eclipse.riena.navigation.INavigationNode#getHistorySize()
 	 * @return the amount of navigation nodes on the navigation stack
 	 */
 	public int getHistoryForwardSize() {
 		return histForward.size();
 	}
 
 	/**
 	 * Navigates to the caller (the source node) of the given targetNode. If
 	 * there is no previous caller, no navigation is performed. If the
 	 * targetNode itself has no caller in the navigationMap, the tree hierarchy
 	 * is searched up to the tree root.
 	 * 
 	 * @param targetNode
 	 *            The node where we have navigate to and return from
 	 */
 	public void navigateBack(INavigationNode<?> targetNode) {
 		INavigationNode<?> sourceNode = null;
 		INavigationNode<?> lookupNode = targetNode;
 		while (sourceNode == null) {
 			sourceNode = navigationMap.get(lookupNode);
 			if (sourceNode == null) {
 				lookupNode = lookupNode.getParent();
 				if (lookupNode == null) {
 					return;
 				}
 			}
 		}
 		navigate(targetNode, sourceNode.getNodeId(), null);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.navigation.INavigationHistoryListernable#
 	 * addNavigationHistoryListener
 	 * (org.eclipse.riena.navigation.INavigationHistoryListener)
 	 */
 	public synchronized void addNavigationHistoryListener(INavigationHistoryListener listener) {
 		if (!navigationListener.contains(listener)) {
 			navigationListener.add(listener);
 			INavigationHistoryEvent event = new NavigationHistoryEvent(histBack.subList(0,
 					(histBack.size() > 0 ? histBack.size() - 1 : 0)));
 			listener.backHistoryChanged(event);
 			event = new NavigationHistoryEvent(histForward.subList(0, histForward.size()));
 			listener.forwardHistoryChanged(event);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.navigation.INavigationHistoryListernable#
 	 * removeNavigationHistoryListener
 	 * (org.eclipse.riena.navigation.INavigationHistoryListener)
 	 */
 	public synchronized void removeNavigationHistoryListener(INavigationHistoryListener listener) {
 		navigationListener.remove(listener);
 	}
 }
