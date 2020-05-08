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
 package org.eclipse.riena.navigation.model;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Stack;
 import java.util.Vector;
 
 import org.osgi.service.log.LogService;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.equinox.log.Logger;
 
 import org.eclipse.riena.core.Log4r;
 import org.eclipse.riena.core.marker.IMarker;
 import org.eclipse.riena.core.util.Nop;
 import org.eclipse.riena.core.util.Trace;
 import org.eclipse.riena.internal.navigation.Activator;
 import org.eclipse.riena.navigation.IJumpTargetListener;
 import org.eclipse.riena.navigation.IJumpTargetListener.JumpTargetState;
 import org.eclipse.riena.navigation.IModuleGroupNode;
 import org.eclipse.riena.navigation.IModuleNode;
 import org.eclipse.riena.navigation.INavigationContext;
 import org.eclipse.riena.navigation.INavigationHistoryEvent;
 import org.eclipse.riena.navigation.INavigationHistoryListener;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.INavigationNode.State;
 import org.eclipse.riena.navigation.INavigationNodeProvider;
 import org.eclipse.riena.navigation.INavigationProcessor;
 import org.eclipse.riena.navigation.ISubApplicationNode;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.NavigationArgument;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.ui.core.marker.DisabledMarker;
 import org.eclipse.riena.ui.core.marker.HiddenMarker;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.IRidgetContainer;
 
 /**
  * Default implementation for the navigation processor
  */
 public class NavigationProcessor implements INavigationProcessor {
 
 	private List<ISubModuleNode> collNodes;
 	private final Stack<INavigationNode<?>> histBack = new Stack<INavigationNode<?>>();
 	private final Stack<INavigationNode<?>> histForward = new Stack<INavigationNode<?>>();
 	private final Map<INavigationNode<?>, Stack<JumpContext>> jumpTargets = new HashMap<INavigationNode<?>, Stack<JumpContext>>();
 	private final Map<INavigationNode<?>, List<IJumpTargetListener>> jumpTargetListeners = new HashMap<INavigationNode<?>, List<IJumpTargetListener>>();
 	private final Map<INavigationNode<?>, INavigationNode<?>> navigationMap = new HashMap<INavigationNode<?>, INavigationNode<?>>();
 	private final List<INavigationHistoryListener> navigationListener = new Vector<INavigationHistoryListener>();
 
 	private final static int MAX_HISTORY_LENGTH = 40;
 
 	private final static boolean DEBUG_NAVIGATION_PROCESSOR = Trace.isOn(NavigationProcessor.class, "debug"); //$NON-NLS-1$
 	private final static Logger LOGGER = Log4r.getLogger(Activator.getDefault(), NavigationProcessor.class);
 
 	public void activate(final INavigationNode<?> toActivate) {
 		if (toActivate != null) {
 			final IModuleNode moduleNode = toActivate.getParentOfType(IModuleNode.class);
 			collNodes = collectCollapsedNodes(moduleNode);
 
 			if (toActivate.isActivated()) {
 				if (DEBUG_NAVIGATION_PROCESSOR) {
 					LOGGER.log(
 							LogService.LOG_DEBUG,
 							"NaviProc: - activate triggered for Node " + toActivate.getNodeId() + "but is already activated --> NOP"); //$NON-NLS-1$//$NON-NLS-2$
 				}
 				Nop.reason("see comment below."); //$NON-NLS-1$
 				// do nothing
 				// if toActivate is module, module group or sub application
 				// the same sub module will be activated on activation of
 				// the toActivate, in any case there is nothing to do
 			} else {
 				if (DEBUG_NAVIGATION_PROCESSOR) {
 					LOGGER.log(LogService.LOG_DEBUG,
 							"NaviProc: - activate triggered for Node " + toActivate.getNodeId()); //$NON-NLS-1$
 				}
 				if (!toActivate.isVisible() || !toActivate.isEnabled()) {
 					if (DEBUG_NAVIGATION_PROCESSOR) {
 						LOGGER.log(
 								LogService.LOG_DEBUG,
 								"NaviProc: - activate triggered for Node " + toActivate.getNodeId() + "but is not visible or not enabled --> NOP"); //$NON-NLS-1$//$NON-NLS-2$
 					}
 
 					return;
 				}
 
 				// 1.find the chain to activate
 				// 2.find the chain to deactivate
 				// 3.check the deactivation chain
 				// 4.check the activation chain
 				// 5. do the deactivation chain
 				// 6. hande node on the histBack
 				// 7. do the activation chain
 				final List<INavigationNode<?>> toActivateList = getNodesToActivateOnActivation(toActivate);
 				if (toActivateList.isEmpty()) {
 					return;
 				}
 				final List<INavigationNode<?>> toDeactivateList = getNodesToDeactivateOnActivation(toActivate);
 				INavigationContext navigationContext = new NavigationContext(null, toActivateList, toDeactivateList);
 				if (allowsDeactivate(navigationContext)) {
 					if (allowsActivate(navigationContext)) {
						// the activation chain may have changed -> update context
						navigationContext = new NavigationContext(null, getNodesToActivateOnActivation(toActivate),
								getNodesToDeactivateOnActivation(toActivate));
 						deactivate(navigationContext);
 						activate(navigationContext);
 					} else {
 						final INavigationNode<?> currentActive = getActiveChild(toActivate.getParent());
						if (currentActive == null) {
							return;
						}
 						toActivateList.clear();
 						toActivateList.add(currentActive);
 						toDeactivateList.clear();
 						navigationContext = new NavigationContext(null, toActivateList, toDeactivateList);
 						activate(navigationContext);
 						currentActive.onAfterActivate(navigationContext);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	public void markNodesToCollapse(final INavigationNode<?> toActivate) {
 		if (toActivate.getContext("fromUI") == null) { //$NON-NLS-1$
 			if (collNodes != null) {
 				for (final ISubModuleNode node : collNodes) {
 					if (node.isExpanded()) {
 						node.setCloseSubTree(true);
 					}
 				}
 			}
 		} else {
 			toActivate.removeContext("fromUI"); //$NON-NLS-1$
 		}
 	}
 
 	private boolean isJumpSource(final ISubModuleNode node) {
 		final Iterator<Map.Entry<INavigationNode<?>, Stack<JumpContext>>> it = jumpTargets.entrySet().iterator();
 		while (it.hasNext()) {
 			final Map.Entry<INavigationNode<?>, Stack<JumpContext>> entry = it.next();
 			final Stack<JumpContext> value = entry.getValue();
 			if (!value.isEmpty() && value.peek().getSource().equals(node)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	private boolean isNodeOrChildJumpSource(final ISubModuleNode node) {
 		final List<ISubModuleNode> children = node.getChildren();
 		if (isJumpSource(node)) {
 			exemptNodeFromCollapsing(node);
 			return true;
 		}
 		for (final ISubModuleNode child : children) {
 			if (isJumpSource(child)) {
 				node.setCloseSubTree(false);
 				exemptNodeFromCollapsing(child);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private void exemptNodeFromCollapsing(final ISubModuleNode node) {
 		node.setCloseSubTree(false);
 		ISubModuleNode parent = node.getParentOfType(ISubModuleNode.class);
 		while (parent != null) {
 			if (collNodes.contains(parent)) {
 				parent.setCloseSubTree(false);
 			}
 			parent = parent.getParentOfType(ISubModuleNode.class);
 		}
 	}
 
 	private List<ISubModuleNode> collectCollapsedNodes(final INavigationNode<?> node) {
 		final List<ISubModuleNode> collapsedNodes = new LinkedList<ISubModuleNode>();
 		List<ISubModuleNode> children = new LinkedList<ISubModuleNode>();
 		if (node instanceof IModuleNode) {
 			children = ((IModuleNode) node).getChildren();
 		} else if (node instanceof ISubModuleNode) {
 			children = ((ISubModuleNode) node).getChildren();
 		}
 		for (final ISubModuleNode child : children) {
 			if (!collapsedNodes.contains(child) && (!child.isExpanded() || child.isCloseSubTree())) {
 				if (!isNodeOrChildJumpSource(child)) {
 					collapsedNodes.add(child);
 				}
 			}
 			if (!child.isLeaf()) {
 				collapsedNodes.addAll(collectCollapsedNodes(child));
 			}
 		}
 
 		return collapsedNodes;
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public void prepare(final INavigationNode<?> toPrepare) {
 
 		final List<INavigationNode<?>> toPreparedList = new LinkedList<INavigationNode<?>>();
 		toPreparedList.add(toPrepare);
 		final INavigationContext navigationContext = new NavigationContext(toPreparedList, null, null);
 		toPrepare.prepare(navigationContext);
 
 	}
 
 	/**
 	 * Remembers the node to push onto the navigation history stack. A maximum
 	 * of MAX_STACKSIZE elements are stored. Older elements are removed from the
 	 * stack.
 	 * 
 	 * @param toActivate
 	 */
 	private void buildHistory(final INavigationNode<?> toActivate) {
 		// filter out unnavigatable nodes
 		if (!(toActivate instanceof ISubModuleNode) || toActivate.isDisposed()) {
 			return;
 		}
 		if (histBack.isEmpty() || !histBack.peek().equals(toActivate)) {
 			histBack.push(toActivate);
 			// limit the stack size and remove older elements
 			if (histBack.size() > MAX_HISTORY_LENGTH) {
 				histBack.remove(histBack.firstElement());
 			}
 			fireBackHistoryChangedEvent();
 		}
 		// is forwarding history top stack element equals node toActivate,
 		// remove it!
 		if (!histForward.isEmpty() && histForward.peek().equals(toActivate)) {
 			histForward.pop();// remove newest node
 			fireForewardHistoryChangedEvent();
 		}
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	public void dispose(final INavigationNode<?> toDispose) {
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
 		final INavigationNode<?> nodeToDispose = getNodeToDispose(toDispose);
 		if (nodeToDispose != null && !nodeToDispose.isDisposed()) {
 			final State nodeStartState = nodeToDispose.getState();
 			handleJumpsOnDispose(nodeToDispose);
 			final List<INavigationNode<?>> toDeactivateList = getNodesToDeactivateOnDispose(nodeToDispose);
 			final List<INavigationNode<?>> toActivateList = getNodesToActivateOnDispose(nodeToDispose);
 			final INavigationContext navigationContext = new NavigationContext(null, toActivateList, toDeactivateList);
 			if (allowsDeactivate(navigationContext)) {
 				if (allowsDispose(navigationContext)) {
 					if (allowsActivate(navigationContext)) {
 						if (nodeStartState != nodeToDispose.getState()) {
 							final String msg = String.format("State of node '%s' changed unexpected!", toDispose); //$NON-NLS-1$
 							throw new NavigationModelFailure(msg);
 						}
 						deactivate(navigationContext);
 						dispose(navigationContext);
 						activate(navigationContext);
 					}
 				}
 			}
 			cleanupHistory(nodeToDispose);
 			cleanupJumpTargetListeners(nodeToDispose);
 		}
 	}
 
 	public void addMarker(final INavigationNode<?> node, final IMarker marker) {
 
 		if (node != null) {
 			if (node.isActivated() && (marker instanceof DisabledMarker || marker instanceof HiddenMarker)) {
 				final INavigationNode<?> nodeToHide = getNodeToDispose(node);
 				// if ((nodeToHide != null) && (nodeToHide.isVisible() && (nodeToHide.isEnabled()))) {
 				if (nodeToHide != null) {
 					final List<INavigationNode<?>> toDeactivateList = getNodesToDeactivateOnDispose(nodeToHide);
 					final List<INavigationNode<?>> toActivateList = getNodesToActivateOnDispose(nodeToHide);
 					final INavigationContext navigationContext = new NavigationContext(null, toActivateList,
 							toDeactivateList);
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
 
 			final List<INavigationNode<?>> toMarkList = new LinkedList<INavigationNode<?>>();
 			toMarkList.add(node);
 			final INavigationContext navigationContext = new NavigationContext(null, toMarkList, null);
 			addMarker(navigationContext, marker);
 		}
 
 	}
 
 	private void addMarker(final INavigationContext context, final IMarker marker) {
 		for (final INavigationNode<?> node : context.getToActivate()) {
 			node.addMarker(context, marker);
 		}
 	}
 
 	/**
 	 * Cleanup the History stacks and removes all occurrences of the node.
 	 * 
 	 * @param toDispose
 	 */
 	private void cleanupHistory(final INavigationNode<?> toDispose) {
 		while (histBack.contains(toDispose)) {
 			histBack.remove(toDispose);
 		}
 		fireBackHistoryChangedEvent();
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
 	 * @since 2.0
 	 */
 	public INavigationNode<?> create(final INavigationNode<?> sourceNode, final NavigationNodeId targetId) {
 		return provideNode(sourceNode, targetId, null);
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public INavigationNode<?> create(final INavigationNode<?> sourceNode, final NavigationNodeId targetId,
 			final NavigationArgument argument) {
 		return provideNode(sourceNode, targetId, argument);
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public void move(final INavigationNode<?> sourceNode, final NavigationNodeId targetId) {
 		Assert.isTrue(ModuleNode.class.isAssignableFrom(sourceNode.getClass()));
 		final ModuleNode sourceModuleNode = ModuleNode.class.cast(sourceNode);
 		final INavigationNode<?> targetNode = create(sourceNode, targetId);
 		Assert.isTrue(ModuleGroupNode.class.isAssignableFrom(targetNode.getClass()));
 		final ModuleGroupNode targetModuleGroup = ModuleGroupNode.class.cast(targetNode);
 		final ModuleGroupNode oldParentModuleGroup = ModuleGroupNode.class.cast(sourceModuleNode.getParent());
 		if (targetModuleGroup.equals(oldParentModuleGroup)) {
 			return;
 		}
 		final boolean isActivated = sourceModuleNode.isActivated();
 		final boolean isBlocked = sourceModuleNode.isBlocked();
 		final boolean isEnabled = sourceModuleNode.isEnabled();
 		final boolean isVisible = sourceModuleNode.isVisible();
 
 		sourceModuleNode.dispose(null);
 		sourceModuleNode.deactivate(null);
 		oldParentModuleGroup.removeChild(sourceModuleNode);
 		targetModuleGroup.addChild(sourceModuleNode);
 
 		for (final ISubModuleNode child : sourceModuleNode.getChildren()) {
 			child.setParent(sourceModuleNode);
 		}
 
 		sourceModuleNode.setBlocked(isBlocked || targetModuleGroup.isBlocked());
 		sourceModuleNode.setEnabled(isEnabled && targetModuleGroup.isEnabled());
 		sourceModuleNode.setVisible(isVisible && targetModuleGroup.isVisible());
 
 		if (isActivated) {
 			sourceModuleNode.activate();
 		}
 
 		if (oldParentModuleGroup.getChildren().size() == 0) {
 			oldParentModuleGroup.dispose();
 		} else {
 			oldParentModuleGroup.getChild(0).setSelected(true);
 		}
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public INavigationNode<?> navigate(final INavigationNode<?> sourceNode, final NavigationNodeId targetId,
 			final NavigationArgument navigation) {
 		// TODO see https://bugs.eclipse.org/bugs/show_bug.cgi?id=261832
 		//		if (navigation != null && navigation.isNavigateAsync()) {
 		//			navigateAsync(sourceNode, targetId, navigation);
 		//		} else {
 		return navigateSync(sourceNode, targetId, navigation, NavigationType.DEFAULT);
 		//		}
 	}
 
 	private enum NavigationType {
 		DEFAULT, JUMP;
 	}
 
 	private INavigationNode<?> navigateSync(final INavigationNode<?> sourceNode, final NavigationNodeId targetId,
 			final NavigationArgument navigation, final NavigationType navigationType) {
 		INavigationNode<?> targetNode = null;
 		try {
 			targetNode = provideNode(sourceNode, targetId, navigation);
 		} catch (final NavigationModelFailure failure) {
 			LOGGER.log(LogService.LOG_ERROR, failure.getMessage());
 		}
 		if (targetNode == null) {
 			return null;
 		}
 		final INavigationNode<?> activateNode = targetNode.findNode(targetId);
 		INavigationNode<?> node = activateNode;
 		if (node == null) {
 			node = targetNode;
 		}
 		navigationMap.put(node, sourceNode);
 
 		if (NavigationType.JUMP == navigationType) {
 			handleJump(sourceNode, node);
 		}
 
 		node.activate();
 		try {
 			setFocusOnRidget(node, navigation);
 		} catch (final NavigationModelFailure failure) {
 			LOGGER.log(LogService.LOG_ERROR, failure.getMessage());
 		}
 
 		return node;
 	}
 
 	/*
 	 * executes internal jump logic
 	 */
 	private void handleJump(final INavigationNode<?> sourceNode, final INavigationNode<?> node) {
 		runObserved(sourceNode, new Runnable() {
 
 			public void run() {
 				registerJump(sourceNode, node);
 			}
 		});
 	}
 
 	/*
 	 * locates the topmost root of the given node
 	 */
 	private INavigationNode<?> getRootNode(final INavigationNode<?> node) {
 		INavigationNode<?> topNode = node;
 		while (topNode.getParent() != null) {
 			topNode = topNode.getParent();
 		}
 		return topNode;
 	}
 
 	/*
 	 * saves the current jump state for the given node
 	 */
 	private Map<INavigationNode<?>, JumpTargetState> saveJumpState(final INavigationNode<?> node) {
 		final INavigationNode<?> topNode = getRootNode(node);
 		final Map<INavigationNode<?>, JumpTargetState> savedJumpState = new HashMap<INavigationNode<?>, IJumpTargetListener.JumpTargetState>();
 		saveJumpState(topNode, savedJumpState);
 		return savedJumpState;
 	}
 
 	/*
 	 * saves the current JumpTargetState for all nodes of the subtree starting
 	 * at the root node recursively
 	 */
 	private void saveJumpState(final INavigationNode<?> root,
 			final Map<INavigationNode<?>, JumpTargetState> savedJumpState) {
 		savedJumpState.put(root, isJumpTarget(root) ? JumpTargetState.ENABLED : JumpTargetState.DISABLED);
 		for (final INavigationNode<?> child : root.getChildren()) {
 			saveJumpState(child, savedJumpState);
 		}
 	}
 
 	/*
 	 * Calculates the JumpTargetState changes for the given nodes and notifies
 	 * observers
 	 */
 	private void notifyJumpStateChanged(final INavigationNode<?> node,
 			final Map<INavigationNode<?>, JumpTargetState> oldJumpState) {
 		final Map<INavigationNode<?>, JumpTargetState> savedJumpState = saveJumpState(node);
 		final Iterator<Entry<INavigationNode<?>, JumpTargetState>> iterator = savedJumpState.entrySet().iterator();
 		while (iterator.hasNext()) {
 			final Entry<INavigationNode<?>, JumpTargetState> entry = iterator.next();
 			if (oldJumpState.get(entry.getKey()) != entry.getValue()) {
 				notifyJumpStateChanged(entry.getKey(), entry.getValue());
 			}
 		}
 	}
 
 	/*
 	 * Notifies all jumpTargetState observers for the given node about the state
 	 * change
 	 */
 	private void notifyJumpStateChanged(final INavigationNode<?> node, final JumpTargetState jumpTargetState) {
 		final List<IJumpTargetListener> listeners = jumpTargetListeners.get(node);
 		if (listeners == null) {
 			return;
 		}
 		for (final IJumpTargetListener listener : listeners) {
 			listener.jumpTargetStateChanged(node, jumpTargetState);
 		}
 	}
 
 	private void registerJump(final INavigationNode<?> source, final INavigationNode<?> target) {
 		// get all sources of the current target
 		Stack<JumpContext> sourceStack = jumpTargets.get(target);
 		if (sourceStack == null) {
 			sourceStack = new Stack<JumpContext>();
 			jumpTargets.put(target, sourceStack);
 		}
 		// save the source
 		if (sourceStack.size() == 0 || !sourceStack.peek().getSource().equals(source)) {
 			sourceStack.push(new JumpContext(source, target));
 		}
 	}
 
 	/*
	 * unregisters the given node as and all of it�s children as jump targets
 	 * and sources recursively
 	 */
 	private void handleJumpsOnDispose(final INavigationNode<?> node) {
 		runObserved(node, new Runnable() {
 
 			public void run() {
 				unregisterNodeJumps(node);
 
 			}
 		});
 	}
 
 	private void unregisterNodeJumps(final INavigationNode<?> node) {
 
 		// as there is one NavigationProcess for the whole tree we need to unregister jump targets, too
 		jumpTargets.remove(node);
 
 		final Iterator<Map.Entry<INavigationNode<?>, Stack<JumpContext>>> it = jumpTargets.entrySet().iterator();
 		while (it.hasNext()) {
 			final Map.Entry<INavigationNode<?>, Stack<JumpContext>> entry = it.next();
 			//clear all occurrences of node as a source
 			final Stack<JumpContext> entryStack = entry.getValue();
 			JumpContext ctx = null;
 			for (final JumpContext ictx : entryStack) {
 				if (ictx.getSource().equals(node)) {
 					ctx = ictx;
 				}
 			}
 			entryStack.remove(ctx);
 
 			if (entryStack.isEmpty()) {
 				it.remove();
 			}
 		}
 
 		// unregister children
 		for (final INavigationNode<?> child : node.getChildren()) {
 			unregisterNodeJumps(child);
 		}
 
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public void jump(final INavigationNode<?> sourceNode, final NavigationNodeId targetId,
 			final NavigationArgument argument) {
 		navigateSync(sourceNode, targetId, argument, NavigationType.JUMP);
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public void jumpBack(final INavigationNode<?> node) {
 		runObserved(node, new Runnable() {
 
 			public void run() {
 				jumpBackInternal(node);
 
 			}
 		});
 	}
 
 	private void jumpBackInternal(final INavigationNode<?> node) {
 		final INavigationNode<?> lastJumpedNode = getLastJump(node);
 		// the sourceStack holds all sources (nodes) to the target
 		final Stack<JumpContext> sourceStack = jumpTargets.get(lastJumpedNode);
 		if (sourceStack == null) {
 			return;
 		}
 		if (!sourceStack.isEmpty()) {
 			final INavigationNode<?> backTarget = sourceStack.pop().getSource();
 			setCloseSubTreeOnJumpBack(backTarget);
 			if (sourceStack.isEmpty()) {
 				// remove node as it is no target anymore
 				jumpTargets.remove(lastJumpedNode);
 			}
 			// go back
 			backTarget.activate();
 		}
 	}
 
 	/*
 	 * executes the runnable collecting changes of the JumpTargetState
 	 */
 	private void runObserved(final INavigationNode<?> node, final Runnable runnable) {
 		final Map<INavigationNode<?>, JumpTargetState> savedJumpState = saveJumpState(node);
 		runnable.run();
 		notifyJumpStateChanged(node, savedJumpState);
 	}
 
 	/**
 	 * If the node is a child of / or an {@link ModuleGroupNode} locates the
 	 * latest target node inside the {@link ModuleGroupNode}. Else return the
 	 * given node itself.
 	 */
 	private INavigationNode<?> getLastJump(final INavigationNode<?> node) {
 		final ModuleGroupNode moduleGroupNode = (ModuleGroupNode) (node instanceof ModuleGroupNode ? node : node
 				.getParentOfType(ModuleGroupNode.class));
 		if (moduleGroupNode == null) {
 			return node;
 		}
 		final List<INavigationNode<?>> nodes = new LinkedList<INavigationNode<?>>();
 		collectNodes(moduleGroupNode, nodes);
 		final List<JumpContext> latestJumpSources = new ArrayList<JumpContext>();
 		for (final INavigationNode<?> targetNode : nodes) {
 			if (jumpTargets.containsKey(targetNode)) {
 				latestJumpSources.add(jumpTargets.get(targetNode).peek());
 			}
 		}
 		if (latestJumpSources.size() > 0) {
 			Collections.sort(latestJumpSources);
 			return latestJumpSources.get(latestJumpSources.size() - 1).getTarget();
 		}
 		return null;
 	}
 
 	/*
 	 * collect all nodes of the the navigation tree with the given root node
 	 */
 	private void collectNodes(final INavigationNode<?> root, final List<INavigationNode<?>> nodes) {
 		nodes.add(root);
 		for (final INavigationNode<?> child : root.getChildren()) {
 			collectNodes(child, nodes);
 		}
 
 	}
 
 	private void setCloseSubTreeOnJumpBack(final INavigationNode<?> backTarget) {
 		if (backTarget.isLeaf() && backTarget.getParentOfType(ISubModuleNode.class) != null) {
 			backTarget.getParentOfType(ISubModuleNode.class).setCloseSubTree(true);
 		} else {
 			((ISubModuleNode) backTarget).setCloseSubTree(true);
 		}
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public boolean isJumpTarget(final INavigationNode<?> node) {
 		final ModuleGroupNode moduleGroupNode = node.getParentOfType(ModuleGroupNode.class);
 		INavigationNode<?> targetNode = null;
 		if (moduleGroupNode == null) {
 			targetNode = node;
 		} else {
 			targetNode = getLastJump(moduleGroupNode);
 		}
 
 		final Stack<JumpContext> sourceStack = jumpTargets.get(targetNode);
 		if (sourceStack != null && sourceStack.size() > 0) {
 			return true;
 		}
 		return false;
 
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public void addJumpTargetListener(final INavigationNode<?> node, final IJumpTargetListener listener) {
 		List<IJumpTargetListener> listeners = jumpTargetListeners.get(node);
 		if (listeners == null) {
 			listeners = new LinkedList<IJumpTargetListener>();
 			jumpTargetListeners.put(node, listeners);
 		}
 		listeners.add(listener);
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public void removeJumpTargetListener(final INavigationNode<?> node, final IJumpTargetListener listener) {
 		final List<IJumpTargetListener> listeners = jumpTargetListeners.get(node);
 		if (listeners == null) {
 			return;
 		}
 		listeners.remove(listener);
 		if (listeners.size() == 0) {
 			jumpTargetListeners.remove(node);
 		}
 	}
 
 	private void cleanupJumpTargetListeners(final INavigationNode<?> node) {
 		jumpTargetListeners.remove(node);
 		for (final INavigationNode<?> child : node.getChildren()) {
 			cleanupJumpTargetListeners(child);
 		}
 	}
 
 	/**
 	 * Requests focus on given ridget. If the ridget is not found a
 	 * {@link RuntimeException} is thrown
 	 * 
 	 * @param activateNode
 	 * @param navigation
 	 */
 	private void setFocusOnRidget(final INavigationNode<?> activateNode, final NavigationArgument navigation) {
 		if (null != navigation && null != navigation.getRidgetId()) {
 			final IRidgetContainer ridgetContainer = activateNode.getNavigationNodeController().getTypecastedAdapter(
 					IRidgetContainer.class);
 			final IRidget ridget = ridgetContainer.getRidget(navigation.getRidgetId());
 			if (null != ridget) {
 				ridget.requestFocus();
 			} else {
 				throw new NavigationModelFailure(String.format("Ridget not found '%s'", navigation.getRidgetId())); //$NON-NLS-1$
 			}
 		}
 	}
 
 	//	/**
 	//	 *
 	//	 * @see org.eclipse.riena.navigation.INavigationProcessor#navigate(org.eclipse.riena.navigation.INavigationNode,
 	//	 *      org.eclipse.riena.navigation.NavigationNodeId,
 	//	 *      org.eclipse.riena.navigation.NavigationArgument)
 	//	 */
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
 
 	private INavigationNode<?> provideNode(final INavigationNode<?> sourceNode, final NavigationNodeId targetId,
 			final NavigationArgument argument) {
 
 		try {
 			return getNavigationNodeProvider().provideNode(sourceNode, targetId, argument);
 		} catch (final ExtensionPointFailure failure) {
 			throw new NavigationModelFailure(String.format("Node not found '%s'", targetId), failure); //$NON-NLS-1$
 		}
 	}
 
 	protected INavigationNodeProvider getNavigationNodeProvider() {
 		return NavigationNodeProvider.getInstance();
 	}
 
 	/**
 	 * Ascertain the correct node to dispose. If e.g. the first module in a
 	 * Group is disposed, then the whole group has to be disposed
 	 * 
 	 * @param toDispose
 	 * @return the correct node to dispose
 	 */
 	private INavigationNode<?> getNodeToDispose(final INavigationNode<?> toDispose) {
 		final IModuleNode moduleNode = toDispose.getTypecastedAdapter(IModuleNode.class);
 		if (moduleNode != null) {
 			final INavigationNode<?> parent = moduleNode.getParent();
 			if (parent != null) {
 				final int ind = parent.getIndexOfChild(moduleNode);
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
 	private List<INavigationNode<?>> getNodesToDeactivateOnDispose(final INavigationNode<?> toDispose) {
 		final List<INavigationNode<?>> allToDispose = new LinkedList<INavigationNode<?>>();
 		addAllChildren(allToDispose, toDispose);
 		if (isOnlySubModule(toDispose)) {
 			allToDispose.add(toDispose.getParent());
 		}
 		return allToDispose;
 	}
 
 	private boolean isOnlySubModule(final INavigationNode<?> node) {
 		return node.getParent() instanceof IModuleNode && node.getParent().getChildren().size() == 1;
 	}
 
 	private void addAllChildren(final List<INavigationNode<?>> allToDispose, final INavigationNode<?> toDispose) {
 		for (final Object nextChild : toDispose.getChildren()) {
 			addAllChildren(allToDispose, (INavigationNode<?>) nextChild);
 		}
 		allToDispose.add(toDispose);
 	}
 
 	/**
 	 * Find a node or list of nodes which have to activated when a specified
 	 * node is disposed
 	 * 
 	 * @param toDispose
 	 *            the node to dispose
 	 * @return a list of nodes
 	 */
 	private List<INavigationNode<?>> getNodesToActivateOnDispose(final INavigationNode<?> toDispose) {
 		// only if the module to dispose was active on dispose,
 		// something else has to be activated
 		// we assume, that if any submodule of this module was active,
 		// than this module is active itself
 		// while dispose of a node, the next brother node must be activated
 		if (toDispose.isActivated()) {
 			final INavigationNode<?> parentOfToDispose = getParentToActivate(toDispose);
 			INavigationNode<?> brotherToActivate = null;
 			if (parentOfToDispose != null) {
 				final List<?> childrenOfParentOfToDispose = parentOfToDispose.getChildren();
 				final List<INavigationNode<?>> activatableNode = getActivatableNodes(childrenOfParentOfToDispose);
 				if (childrenOfParentOfToDispose.size() > 1) {
 					// there must be a least 2 children: the disposed will be
 					// removed
 					// get the first child which is not the one to remove
 					for (final INavigationNode<?> nextChild : activatableNode) {
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
 
 	private INavigationNode<?> getParentToActivate(final INavigationNode<?> node) {
 		if (isOnlySubModule(node)) {
 			if (node.getParentOfType(IModuleGroupNode.class).getChildren().size() == 1) {
 				return node.getParentOfType(ISubApplicationNode.class);
 			}
 			return node.getParentOfType(IModuleGroupNode.class);
 		}
 		return node.getParent();
 
 	}
 
 	/**
 	 * Removes all not activatable nodes (e.g. hidden nodes) from the given
 	 * list.
 	 * 
 	 * @param nodes
 	 *            list of node
 	 * @return filtered list
 	 */
 	private List<INavigationNode<?>> getActivatableNodes(final List<?> nodes) {
 		final List<INavigationNode<?>> activatableNodes = new LinkedList<INavigationNode<?>>();
 		for (final Object node : nodes) {
 			if (node instanceof INavigationNode<?>) {
 				final INavigationNode<?> naviNode = (INavigationNode<?>) node;
 				if (naviNode.isVisible() && naviNode.isEnabled()) {
 					activatableNodes.add(naviNode);
 				}
 			}
 		}
 		return activatableNodes;
 	}
 
 	/**
 	 * Finds all the nodes to activate
 	 * 
 	 * @param toActivate
 	 *            the node do activate
 	 * @return a List of all nodes to activate
 	 */
 	private List<INavigationNode<?>> getNodesToActivateOnActivation(final INavigationNode<?> toActivate) {
 		final List<INavigationNode<?>> nodesToActivate = new LinkedList<INavigationNode<?>>();
 		// go up and add all parents
 		addParentsToActivate(nodesToActivate, toActivate);
 		// go down and add all children
 		addChildrenToActivate(nodesToActivate, toActivate);
 		// the first element in the list must be the last not active parent
 		return nodesToActivate;
 	}
 
 	private void addParentsToActivate(final List<INavigationNode<?>> nodesToActivate,
 			final INavigationNode<?> toActivate) {
 		// go up to the next active parent
 		final INavigationNode<?> parent = getActivationParent(toActivate);
 		if (parent != null) {
 			if (parent.isActivated()) {
 				addSelectableNode(nodesToActivate, toActivate);
 			} else {
 				addParentsToActivate(nodesToActivate, parent);
 				addSelectableNode(nodesToActivate, toActivate);
 			}
 		} else {
 			nodesToActivate.add(toActivate);
 		}
 	}
 
 	private void addSelectableNode(final List<INavigationNode<?>> nodesToActivate, final INavigationNode<?> toActivate) {
 		if (toActivate instanceof ISubModuleNode && !((ISubModuleNode) toActivate).isSelectable()) {
 			Nop.reason("do not add; not selectable"); //$NON-NLS-1$
 		} else {
 			nodesToActivate.add(toActivate);
 		}
 	}
 
 	private INavigationNode<?> getActivationParent(final INavigationNode<?> child) {
 		// by a subModule is it the module node
 		// by all other die direct parent
 		final ISubModuleNode subModuleNode = child.getTypecastedAdapter(ISubModuleNode.class);
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
 
 	private void addChildrenToActivate(final List<INavigationNode<?>> nodesToActivate,
 			final INavigationNode<?> toActivate) {
 		final INavigationNode<?> childToActivate = getChildToActivate(toActivate);
 		if (childToActivate != null) {
 			nodesToActivate.add(childToActivate);
 			addChildrenToActivate(nodesToActivate, childToActivate);
 		}
 	}
 
 	private List<INavigationNode<?>> getNodesToDeactivateOnActivation(final INavigationNode<?> toActivate) {
 		// get next active parent or root
 		final List<INavigationNode<?>> nodesToDeactivate = new LinkedList<INavigationNode<?>>();
 		final INavigationNode<?> activeParent = getNextActiveParent(toActivate);
 		if (activeParent != null) {
 			// Handle the case that no active child is available due
 			// to some errors or exceptions which occurred before
 			final INavigationNode<?> activeChild = getActiveChild(activeParent);
 			if (activeChild != null) {
 				addChildrenToDeactivate(nodesToDeactivate, activeChild);
 			}
 		} else {
 			// no one was active before
 			addChildrenToDeactivate(nodesToDeactivate, getTopParent(toActivate));
 		}
 		return nodesToDeactivate;
 
 	}
 
 	private void addChildrenToDeactivate(final List<INavigationNode<?>> nodesToDeactivate,
 			final INavigationNode<?> toAdd) {
 		final INavigationNode<?> activeChild = getActiveChild(toAdd);
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
 	private INavigationNode<?> getNextActiveParent(final INavigationNode<?> node) {
 		// the next active parent must be at least a Module Node
 		final ISubModuleNode subModuleNode = node.getTypecastedAdapter(ISubModuleNode.class);
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
 
 	private INavigationNode<?> getTopParent(final INavigationNode<?> node) {
 		if (node.getParent() != null) {
 			return getTopParent(node.getParent());
 		} else {
 			return node;
 		}
 
 	}
 
 	private boolean allowsActivate(final INavigationContext context) {
 		for (final INavigationNode<?> nextToActivate : context.getToActivate()) {
 			if (!nextToActivate.allowsActivate(context)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private boolean allowsDispose(final INavigationContext context) {
 		for (final INavigationNode<?> nextToDeactivate : context.getToDeactivate()) {
 			if (!nextToDeactivate.allowsDispose(context)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private boolean allowsDeactivate(final INavigationContext context) {
 		for (final INavigationNode<?> nextToDeactivate : context.getToDeactivate()) {
 			if (!nextToDeactivate.allowsDeactivate(context)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private void activate(final INavigationContext context) {
 		Assert.isNotNull(context);
 		Assert.isNotNull(context.getToActivate());
 		final List<INavigationNode<?>> nextNodesToActivate = context.getToActivate();
 		if (nextNodesToActivate.isEmpty()) {
 			LOGGER.log(LogService.LOG_DEBUG, "NaviProc: - There is no node to activate!"); //$NON-NLS-1$
 		}
 		for (final INavigationNode<?> nextToActivate : nextNodesToActivate) {
 			if (DEBUG_NAVIGATION_PROCESSOR) {
 				LOGGER.log(LogService.LOG_DEBUG, "NaviProc: - beforeActivate: " + nextToActivate.getNodeId()); //$NON-NLS-1$
 			}
 			nextToActivate.onBeforeActivate(context);
 		}
 		for (final INavigationNode<?> nextToActivate : nextNodesToActivate) {
 			if (DEBUG_NAVIGATION_PROCESSOR) {
 				LOGGER.log(LogService.LOG_DEBUG, "NaviProc: - activate: " + nextToActivate.getNodeId()); //$NON-NLS-1$
 			}
 			nextToActivate.activate(context);
 			setAsSelectedChild(nextToActivate);
 		}
 		for (final INavigationNode<?> nextToActivate : copyReverse(nextNodesToActivate)) {
 			if (DEBUG_NAVIGATION_PROCESSOR) {
 				LOGGER.log(LogService.LOG_DEBUG, "NaviProc: - onAfterActivate: " + nextToActivate.getNodeId()); //$NON-NLS-1$
 			}
 			nextToActivate.onAfterActivate(context);
 		}
 	}
 
 	private void deactivate(final INavigationContext context) {
 		final Collection<INavigationNode<?>> previouslyActivatedNodes = new ArrayList<INavigationNode<?>>();
 		for (final INavigationNode<?> nextToDeactivate : copyReverse(context.getToDeactivate())) {
 			if (nextToDeactivate.isActivated()) {
 				previouslyActivatedNodes.add(nextToDeactivate);
 				if (DEBUG_NAVIGATION_PROCESSOR) {
 					LOGGER.log(LogService.LOG_DEBUG, "NaviProc: - beforeDeactivate: " + nextToDeactivate.getNodeId()); //$NON-NLS-1$
 				}
 				nextToDeactivate.onBeforeDeactivate(context);
 			}
 		}
 		for (final INavigationNode<?> nextToDeactivate : context.getToDeactivate()) {
 			// check for activated to make this method usable for disposing
 			if (previouslyActivatedNodes.contains(nextToDeactivate)) {
 				if (DEBUG_NAVIGATION_PROCESSOR) {
 					LOGGER.log(LogService.LOG_DEBUG, "NaviProc: - deactivate: " + nextToDeactivate.getNodeId()); //$NON-NLS-1$
 				}
 				nextToDeactivate.deactivate(context);
 			}
 		}
 		for (final INavigationNode<?> nextToDeactivate : context.getToDeactivate()) {
 			if (previouslyActivatedNodes.contains(nextToDeactivate)) {
 				if (DEBUG_NAVIGATION_PROCESSOR) {
 					LOGGER.log(LogService.LOG_DEBUG, "NaviProc: - onAfterDeactivate: " + nextToDeactivate.getNodeId()); //$NON-NLS-1$
 				}
 				nextToDeactivate.onAfterDeactivate(context);
 			}
 		}
 	}
 
 	private void dispose(final INavigationContext context) {
 		for (final INavigationNode<?> nextToDispose : copyReverse(context.getToDeactivate())) {
 			nextToDispose.onBeforeDispose(context);
 		}
 		for (final INavigationNode<?> nextToDispose : context.getToDeactivate()) {
 			// check for activated to make this method usable for disposing
 			if (DEBUG_NAVIGATION_PROCESSOR) {
 				LOGGER.log(LogService.LOG_DEBUG, "NaviProc: - dispos: " + nextToDispose.getNodeId()); //$NON-NLS-1$
 			}
 			nextToDispose.dispose(context);
 			// remove the node from tree
 			final INavigationNode<?> parent = nextToDispose.getParent();
 			if (parent != null) {
 				parent.removeChild(nextToDispose);
 			}
 		}
 		for (final INavigationNode<?> nextToDispose : context.getToDeactivate()) {
 			nextToDispose.onAfterDispose(context);
 			// clean up history stacks
 			cleanupHistory(nextToDispose);
 		}
 	}
 
 	/**
 	 * Context with lists of nodes to be prepared, activated and deactivated.
 	 */
 	private static class NavigationContext implements INavigationContext {
 
 		private final static List<INavigationNode<?>> EMPTY_LIST = new LinkedList<INavigationNode<?>>();
 
 		private List<INavigationNode<?>> toDeactivate;
 		private List<INavigationNode<?>> toActivate;
 		private List<INavigationNode<?>> toPrepare;
 
 		/**
 		 * Creates a context with nodes to be prepared, activated and
 		 * deactivated.
 		 * 
 		 * @param toPrepare
 		 *            nodes to be prepared
 		 * @param toActivate
 		 *            nodes to be activated
 		 * @param toDeactivate
 		 *            nodes to be deactevated
 		 */
 		public NavigationContext(final List<INavigationNode<?>> toPrepare, final List<INavigationNode<?>> toActivate,
 				final List<INavigationNode<?>> toDeactivate) {
 			super();
 			this.toPrepare = toPrepare;
 			this.toActivate = toActivate;
 			this.toDeactivate = toDeactivate;
 			if (this.toPrepare == null) {
 				this.toPrepare = EMPTY_LIST;
 			}
 			if (this.toActivate == null) {
 				this.toActivate = EMPTY_LIST;
 			}
 			if (this.toDeactivate == null) {
 				this.toDeactivate = EMPTY_LIST;
 			}
 		}
 
 		public List<INavigationNode<?>> getToActivate() {
 			return toActivate;
 		}
 
 		public List<INavigationNode<?>> getToDeactivate() {
 			return toDeactivate;
 		}
 
 		public List<INavigationNode<?>> getToPrepare() {
 			return toPrepare;
 		}
 
 	}
 
 	/**
 	 * The navigation processor decides which child to activate even initially
 	 * 
 	 * @param pNode
 	 *            the node who's child is searched
 	 */
 	private INavigationNode<?> getChildToActivate(final INavigationNode<?> pNode) {
 		// for sub module is always null
 
 		final ISubModuleNode subModuleNode = pNode.getTypecastedAdapter(ISubModuleNode.class);
 		if (subModuleNode != null) {
 
 			if (!subModuleNode.isSelectable()) {
 				if (!subModuleNode.getChildren().isEmpty()) {
 					return findSelectableChildNode(subModuleNode);
 				}
 			}
 
 			return null;
 		}
 		// for module node is it the deepest selected sub Module node
 		final IModuleNode moduleNode = pNode.getTypecastedAdapter(IModuleNode.class);
 		if (moduleNode != null) {
 			ISubModuleNode nextChild = getSelectedChild(moduleNode);
 			if (nextChild != null) {
 				while (true) {
 					final ISubModuleNode nextTmp = getSelectedChild(nextChild);
 					if (nextTmp != null) {
 						nextChild = nextTmp;
 					} else {
 						return nextChild;
 					}
 				}
 			} else {
 				if (moduleNode.getChildren().size() > 0) {
 					// find the first selectable node in the list of childs
 					ISubModuleNode subModule = null;
 					final Iterator<ISubModuleNode> subIter = moduleNode.getChildren().iterator();
 					while (subModule == null && subIter.hasNext()) {
 						subModule = findSelectableChildNode(subIter.next());
 					}
 
 					return subModule;
 				} else {
 					return null;
 				}
 			}
 		}
 		// for all others is it the direct selected child
 		final INavigationNode<?> nextSelectedChild = getSelectedChild(pNode);
 		if (nextSelectedChild != null) {
 			return nextSelectedChild;
 		} else {
 			for (final INavigationNode<?> next : pNode.getChildren()) {
 				if (next.isVisible() && next.isEnabled()) {
 					return next;
 				}
 			}
 			return null;
 		}
 	}
 
 	private ISubModuleNode findSelectableChildNode(final ISubModuleNode startNode) {
 		// if node is not visible, return null (note: this method is recursive, see below)
 		if (!startNode.isVisible()) {
 			return null;
 		}
 		// selectable node found
 		if (startNode.isSelectable()) {
 			return startNode;
 		}
 		// if not selectable, expand it because it does not get activated some else need to expand it
 		startNode.setExpanded(true);
 		// check childs for selectable node
 		for (final ISubModuleNode child : startNode.getChildren()) {
 			final ISubModuleNode found = findSelectableChildNode(child);
 			if (found != null) {
 				return found;
 			}
 		}
 		return null;
 	}
 
 	private INavigationNode<?> getActiveChild(final INavigationNode<?> pNode) {
 		// for a Sub Module it is always null
 		final ISubModuleNode subModuleNode = pNode.getTypecastedAdapter(ISubModuleNode.class);
 		if (subModuleNode != null) {
 			return null;
 		}
 		// for a module node it is the last selected child
 		final IModuleNode moduleNode = pNode.getTypecastedAdapter(IModuleNode.class);
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
 		final INavigationNode<?> nextSelectedChild = getSelectedChild(pNode);
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
 	private void setAsSelectedChild(final INavigationNode<?> pNode) {
 
 		// when activating a sub module the chain must be set up to the module
 		// node
 		final ISubModuleNode subModuleNode = pNode.getTypecastedAdapter(ISubModuleNode.class);
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
 	 *            the parent to reset in
 	 * @param child
 	 *            the child to set as selected
 	 */
 	private void setSelectedChild(final INavigationNode<?> parent, final INavigationNode<?> child) {
 		for (final INavigationNode<?> next : parent.getChildren()) {
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
 
 	private INavigationNode<?> getSelectedChild(final INavigationNode<?> pNavigationNode) {
 		for (final INavigationNode<?> next : pNavigationNode.getChildren()) {
 			if (next.isSelected()) {
 				return next;
 			}
 		}
 		return null;
 	}
 
 	private ISubModuleNode getSelectedChild(final IModuleNode pModuleNode) {
 		for (final ISubModuleNode next : pModuleNode.getChildren()) {
 			if (next.isSelected()) {
 				return next;
 			}
 		}
 		return null;
 	}
 
 	private ISubModuleNode getSelectedChild(final ISubModuleNode pSubModuleNode) {
 		for (final ISubModuleNode next : pSubModuleNode.getChildren()) {
 			if (next.isSelected()) {
 				return next;
 			}
 		}
 		return null;
 	}
 
 	private List<INavigationNode<?>> copyReverse(final List<INavigationNode<?>> list) {
 
 		final List<INavigationNode<?>> listReverse = list.subList(0, list.size());
 		Collections.reverse(listReverse);
 
 		return listReverse;
 	}
 
 	public void historyBack() {
 		if (getHistoryBackSize() > 0) {
 			final INavigationNode<?> current = histBack.pop();// skip self
 			fireBackHistoryChangedEvent();
 			histForward.push(current);
 			if (histForward.size() > MAX_HISTORY_LENGTH) {
 				histForward.remove(histForward.firstElement());
 			}
 			fireForewardHistoryChangedEvent();
 			final INavigationNode<?> node = histBack.peek();// activate parent
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
 		final INavigationHistoryEvent event = new NavigationHistoryEvent(histBack.subList(0,
 				Math.max(0, histBack.size() - 1)));
 		for (final INavigationHistoryListener listener : navigationListener) {
 			listener.backHistoryChanged(event);
 		}
 	}
 
 	public void historyForward() {
 		if (getHistoryForwardSize() > 0) {
 			final INavigationNode<?> current = histForward.pop();
 			fireForewardHistoryChangedEvent();
 			if (current != null) {
 				histBack.push(current);
 				if (histBack.size() > MAX_HISTORY_LENGTH) {
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
 		final INavigationHistoryEvent event = new NavigationHistoryEvent(histForward.subList(0, histForward.size()));
 		for (final INavigationHistoryListener listener : navigationListener) {
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
 	public void navigateBack(final INavigationNode<?> targetNode) {
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
 
 	public synchronized void addNavigationHistoryListener(final INavigationHistoryListener listener) {
 		if (!navigationListener.contains(listener)) {
 			navigationListener.add(listener);
 			INavigationHistoryEvent event = new NavigationHistoryEvent(histBack.subList(0,
 					(histBack.size() > 0 ? histBack.size() - 1 : 0)));
 			listener.backHistoryChanged(event);
 			event = new NavigationHistoryEvent(histForward.subList(0, histForward.size()));
 			listener.forwardHistoryChanged(event);
 		}
 	}
 
 	public synchronized void removeNavigationHistoryListener(final INavigationHistoryListener listener) {
 		navigationListener.remove(listener);
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	public List<INavigationNode<?>> getHistory() {
 		return Collections.unmodifiableList(histBack);
 	}
 }
