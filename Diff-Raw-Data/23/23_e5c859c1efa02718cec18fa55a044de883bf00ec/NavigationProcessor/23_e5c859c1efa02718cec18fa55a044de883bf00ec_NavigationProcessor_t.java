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
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
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
import org.eclipse.riena.navigation.IJumpTargetListener.JumpTargetState;
 import org.eclipse.riena.ui.core.marker.DisabledMarker;
 import org.eclipse.riena.ui.core.marker.HiddenMarker;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.IRidgetContainer;
 
 /**
  * Default implementation for the navigation processor
  */
 public class NavigationProcessor implements INavigationProcessor, INavigationHistory {
 
 	private static int maxStacksize = 40;
 	private Stack<INavigationNode<?>> histBack = new Stack<INavigationNode<?>>();
 	private Stack<INavigationNode<?>> histForward = new Stack<INavigationNode<?>>();
 	private Map<INavigationNode<?>, Stack<INavigationNode<?>>> jumpTargets = new HashMap<INavigationNode<?>, Stack<INavigationNode<?>>>();
 	private Map<INavigationNode<?>, List<IJumpTargetListener>> jumpTargetListeners = new HashMap<INavigationNode<?>, List<IJumpTargetListener>>();
 	private Map<INavigationNode<?>, INavigationNode<?>> navigationMap = new HashMap<INavigationNode<?>, INavigationNode<?>>();
 	private List<INavigationHistoryListener> navigationListener = new Vector<INavigationHistoryListener>();
 	private static boolean debugNaviProc = Trace.isOn(NavigationProcessor.class, "debug"); //$NON-NLS-1$
 	private final static Logger LOGGER = Log4r.getLogger(Activator.getDefault(), NavigationProcessor.class);;
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationProcessor#activate(org.eclipse.riena.navigation.INavigationNode)
 	 */
 	public void activate(INavigationNode<?> toActivate) {
 		if (toActivate != null) {
 			if (toActivate.isActivated()) {
 				if (debugNaviProc) {
					LOGGER
							.log(
									LogService.LOG_DEBUG,
									"NaviProc: - activate triggered for Node " + toActivate.getNodeId() + "but is already activated --> NOP"); //$NON-NLS-1$//$NON-NLS-2$
 				}
 				Nop.reason("see comment below."); //$NON-NLS-1$
 				// do nothing
 				// if toActivate is module, module group or sub application
 				// the same sub module will be activated on activation of
 				// the toActivate, in any case there is nothing to do
 			} else {
 				if (debugNaviProc) {
 					LOGGER.log(LogService.LOG_DEBUG,
 							"NaviProc: - activate triggered for Node " + toActivate.getNodeId()); //$NON-NLS-1$
 				}
 				if (!toActivate.isVisible() || !toActivate.isEnabled()) {
 					if (debugNaviProc) {
						LOGGER
								.log(
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
 				List<INavigationNode<?>> toActivateList = getNodesToActivateOnActivation(toActivate);
 				List<INavigationNode<?>> toDeactivateList = getNodesToDeactivateOnActivation(toActivate);
 				INavigationContext navigationContext = new NavigationContext(null, toActivateList, toDeactivateList);
 				if (allowsDeactivate(navigationContext)) {
 					if (allowsActivate(navigationContext)) {
 						deactivate(navigationContext);
 						activate(navigationContext);
 					} else {
 						INavigationNode<?> currentActive = getActiveChild(toActivate.getParent());
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
 	 * {@inheritDoc}
 	 * 
 	 * @since 2.0
 	 */
 	public void prepare(INavigationNode<?> toPrepare) {
 
 		List<INavigationNode<?>> toPreparedList = new LinkedList<INavigationNode<?>>();
 		toPreparedList.add(toPrepare);
 		INavigationContext navigationContext = new NavigationContext(toPreparedList, null, null);
 		toPrepare.prepare(navigationContext);
 
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
 			INavigationContext navigationContext = new NavigationContext(null, toActivateList, toDeactivateList);
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
 			unregisterJumpSource(nodeToDispose);
 			cleanupJumpTargetListeners(nodeToDispose);
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
 					INavigationContext navigationContext = new NavigationContext(null, toActivateList, toDeactivateList);
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
 			INavigationContext navigationContext = new NavigationContext(null, toMarkList, null);
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
 	 * {@inheritDoc}
 	 * 
 	 * @since 2.0
 	 */
 	public INavigationNode<?> create(INavigationNode<?> sourceNode, NavigationNodeId targetId) {
 		return provideNode(sourceNode, targetId, null);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @since 2.0
 	 */
 	public INavigationNode<?> create(INavigationNode<?> sourceNode, NavigationNodeId targetId,
 			NavigationArgument argument) {
 		return provideNode(sourceNode, targetId, argument);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @since 2.0
 	 */
 	public void move(INavigationNode<?> sourceNode, NavigationNodeId targetId) {
 		Assert.isTrue(ModuleNode.class.isAssignableFrom(sourceNode.getClass()));
 		ModuleNode moduleNode = ModuleNode.class.cast(sourceNode);
 		INavigationNode<?> targetNode = create(sourceNode, targetId);
 		Assert.isTrue(ModuleGroupNode.class.isAssignableFrom(targetNode.getClass()));
 		ModuleGroupNode targetModuleGroup = ModuleGroupNode.class.cast(targetNode);
 		ModuleGroupNode oldParentModuleGroup = ModuleGroupNode.class.cast(moduleNode.getParent());
 		if (targetModuleGroup.equals(oldParentModuleGroup)) {
 			return;
 		}
 		boolean isActivated = moduleNode.isActivated();
 		moduleNode.dispose(null);
 		moduleNode.deactivate(null);
 		oldParentModuleGroup.removeChild(moduleNode);
 		targetModuleGroup.addChild(moduleNode);
 		if (isActivated) {
 			moduleNode.activate();
 		}
 		if (oldParentModuleGroup.getChildren().size() == 0) {
 			oldParentModuleGroup.dispose();
 		} else {
 			oldParentModuleGroup.getChild(0).setSelected(true);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
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
 
 	/**
 	 * {@inheritDoc}
 	 */
 	private INavigationNode<?> navigateSync(final INavigationNode<?> sourceNode, final NavigationNodeId targetId,
 			final NavigationArgument navigation, NavigationType navigationType) {
 		INavigationNode<?> targetNode = null;
 		try {
 			targetNode = provideNode(sourceNode, targetId, navigation);
 		} catch (NavigationModelFailure failure) {
 			LOGGER.log(LogService.LOG_ERROR, failure.getMessage());
 		}
 		if (targetNode == null) {
 			return null;
 		}
 		INavigationNode<?> activateNode = targetNode.findNode(targetId);
 		INavigationNode<?> node = activateNode;
 		if (node == null) {
 			node = targetNode;
 		}
 		navigationMap.put(node, sourceNode);
 
 		if (NavigationType.JUMP == navigationType) {
 			registerJump(sourceNode, node);
 			fireJumpTargetStateChanged(node, JumpTargetState.ENABLED);
 		}
 
 		node.activate();
 		try {
 			setFocusOnRidget(node, navigation);
 		} catch (NavigationModelFailure failure) {
 			LOGGER.log(LogService.LOG_ERROR, failure.getMessage());
 		}
 
 		return node;
 	}
 
 	private void registerJump(INavigationNode<?> source, INavigationNode<?> target) {
 		Stack<INavigationNode<?>> sourceStack = jumpTargets.get(target);
 		if (sourceStack == null) {
 			sourceStack = new Stack<INavigationNode<?>>();
 			jumpTargets.put(target, sourceStack);
 		}
 		sourceStack.push(source);
 	}
 
 	private void unregisterJumpSource(INavigationNode<?> source) {
 		Iterator<Map.Entry<INavigationNode<?>, Stack<INavigationNode<?>>>> it = jumpTargets.entrySet().iterator();
 		while (it.hasNext()) {
 			Map.Entry<INavigationNode<?>, Stack<INavigationNode<?>>> entry = it.next();
 			while (entry.getValue().remove(source))
 				;
 
 			if (entry.getValue().size() == 0) {
 				fireJumpTargetStateChanged(entry.getKey(), JumpTargetState.DISABLED);
 				it.remove();
 			}
 		}
 
 	}
 
 	/**
 	 * @see INavigationProcessor#jump(INavigationNode, NavigationNodeId,
 	 *      NavigationArgument)
 	 * @since 2.0
 	 */
 	public void jump(INavigationNode<?> sourceNode, NavigationNodeId targetId, NavigationArgument argument) {
 		navigateSync(sourceNode, targetId, argument, NavigationType.JUMP);
 	}
 
 	/**
 	 * @see INavigationProcessor#jumpBack(INavigationNode, NavigationNodeId)
 	 * @since 2.0
 	 */
 	public void jumpBack(INavigationNode<?> sourceNode) {
 		Stack<INavigationNode<?>> sourceStack = jumpTargets.get(sourceNode);
 		if (sourceStack != null) {
 			if (sourceStack.size() > 0) {
 				INavigationNode<?> backTarget = sourceStack.pop();
 				backTarget.activate();
 			}
 			jumpTargets.remove(sourceNode);
 			fireJumpTargetStateChanged(sourceNode, JumpTargetState.DISABLED);
 		}
 	}
 
 	/**
 	 * @see INavigationProcessor#isJumpTarget(INavigationNode)
 	 * @since 2.0
 	 */
 	public boolean isJumpTarget(INavigationNode<?> node) {
 		Stack<INavigationNode<?>> sourceStack = jumpTargets.get(node);
 		return sourceStack != null && sourceStack.size() > 0;
 	}
 
 	/**
 	 * @see INavigationProcessor#addJumpTargetListener(INavigationNode,
 	 *      IJumpTargetListener)
 	 * @since 2.0
 	 */
 	public void addJumpTargetListener(INavigationNode<?> node, IJumpTargetListener listener) {
 		List<IJumpTargetListener> listeners = jumpTargetListeners.get(node);
 		if (listeners == null) {
 			listeners = new LinkedList<IJumpTargetListener>();
 			jumpTargetListeners.put(node, listeners);
 		}
 		listeners.add(listener);
 	}
 
 	/**
 	 * @see INavigationProcessor#removeJumpTargetListener(INavigationNode,
 	 *      IJumpTargetListener)
 	 * @since 2.0
 	 */
 	public void removeJumpTargetListener(INavigationNode<?> node, IJumpTargetListener listener) {
 		List<IJumpTargetListener> listeners = jumpTargetListeners.get(node);
 		if (listeners == null) {
 			return;
 		}
 		listeners.remove(listener);
 		if (listeners.size() == 0) {
 			jumpTargetListeners.remove(node);
 		}
 	}
 
 	private void cleanupJumpTargetListeners(INavigationNode<?> node) {
 		jumpTargetListeners.remove(node);
 	}
 
 	private void fireJumpTargetStateChanged(INavigationNode<?> node, JumpTargetState jumpTargetState) {
 		List<IJumpTargetListener> listeners = jumpTargetListeners.get(node);
 		if (listeners == null) {
 			return;
 		}
 		for (IJumpTargetListener listener : listeners) {
 			listener.jumpTargetStateChanged(node, jumpTargetState);
 		}
 	}
 
 	/**
 	 * Requests focus on given ridget. If the ridget is not found a
 	 * {@link RuntimeException} is thrown
 	 * 
 	 * @param activateNode
 	 * @param navigation
 	 */
 	private void setFocusOnRidget(INavigationNode<?> activateNode, NavigationArgument navigation) {
 		if (null != navigation && null != navigation.getRidgetId()) {
 			IRidgetContainer ridgetContainer = activateNode.getNavigationNodeController().getTypecastedAdapter(
 					IRidgetContainer.class);
 			IRidget ridget = ridgetContainer.getRidget(navigation.getRidgetId());
 			if (null != ridget) {
 				ridget.requestFocus();
 			} else {
 				throw new NavigationModelFailure(String.format("Ridget not found '%s'", navigation.getRidgetId())); //$NON-NLS-1$
 			}
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
 
 		try {
 			return getNavigationNodeProvider().provideNode(sourceNode, targetId, argument);
 		} catch (ExtensionPointFailure failure) {
 			throw new NavigationModelFailure(String.format("Node not found '%s'", targetId), failure); //$NON-NLS-1$
 		}
 	}
 
 	protected INavigationNodeProvider getNavigationNodeProvider() {
 		return NavigationNodeProvider.getInstance();
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
 	 *            the node to dispose
 	 * @return a list of nodes
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
 	 *            list of node
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
 	 *            the node do activate
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
 				addSelectableNode(nodesToActivate, toActivate);
 			} else {
 				addParentsToActivate(nodesToActivate, parent);
 				addSelectableNode(nodesToActivate, toActivate);
 			}
 		} else {
 			nodesToActivate.add(toActivate);
 		}
 	}
 
 	private void addSelectableNode(List<INavigationNode<?>> nodesToActivate, INavigationNode<?> toActivate) {
 		if (toActivate instanceof ISubModuleNode && !((ISubModuleNode) toActivate).isSelectable()) {
 			// do not add; not selectable
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
 			if (debugNaviProc) {
 				LOGGER.log(LogService.LOG_DEBUG, "NaviProc: - beforeActivate: " + nextToActivate.getNodeId()); //$NON-NLS-1$
 			}
 			nextToActivate.onBeforeActivate(context);
 		}
 		for (INavigationNode<?> nextToActivate : context.getToActivate()) {
 			if (debugNaviProc) {
 				LOGGER.log(LogService.LOG_DEBUG, "NaviProc: - activate: " + nextToActivate.getNodeId()); //$NON-NLS-1$
 			}
 			nextToActivate.activate(context);
 			setAsSelectedChild(nextToActivate);
 		}
 		for (INavigationNode<?> nextToActivate : copyReverse(context.getToActivate())) {
 			if (debugNaviProc) {
 				LOGGER.log(LogService.LOG_DEBUG, "NaviProc: - onAfterActivate: " + nextToActivate.getNodeId()); //$NON-NLS-1$
 			}
 			nextToActivate.onAfterActivate(context);
 		}
 	}
 
 	private void deactivate(INavigationContext context) {
 		Collection<INavigationNode<?>> previouslyActivatedNodes = new ArrayList<INavigationNode<?>>();
 		for (INavigationNode<?> nextToDeactivate : copyReverse(context.getToDeactivate())) {
 			if (nextToDeactivate.isActivated()) {
 				previouslyActivatedNodes.add(nextToDeactivate);
 				if (debugNaviProc) {
 					LOGGER.log(LogService.LOG_DEBUG, "NaviProc: - beforeDeactivate: " + nextToDeactivate.getNodeId()); //$NON-NLS-1$
 				}
 				nextToDeactivate.onBeforeDeactivate(context);
 			}
 		}
 		for (INavigationNode<?> nextToDeactivate : context.getToDeactivate()) {
 			// check for activated to make this method usable for disposing
 			if (previouslyActivatedNodes.contains(nextToDeactivate)) {
 				if (debugNaviProc) {
 					LOGGER.log(LogService.LOG_DEBUG, "NaviProc: - deactivate: " + nextToDeactivate.getNodeId()); //$NON-NLS-1$
 				}
 				nextToDeactivate.deactivate(context);
 			}
 		}
 		for (INavigationNode<?> nextToDeactivate : context.getToDeactivate()) {
 			if (previouslyActivatedNodes.contains(nextToDeactivate)) {
 				if (debugNaviProc) {
 					LOGGER.log(LogService.LOG_DEBUG, "NaviProc: - onAfterDeactivate: " + nextToDeactivate.getNodeId()); //$NON-NLS-1$
 				}
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
 			if (debugNaviProc) {
 				LOGGER.log(LogService.LOG_DEBUG, "NaviProc: - dispos: " + nextToDispose.getNodeId()); //$NON-NLS-1$
 			}
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
 		public NavigationContext(List<INavigationNode<?>> toPrepare, List<INavigationNode<?>> toActivate,
 				List<INavigationNode<?>> toDeactivate) {
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
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public List<INavigationNode<?>> getToActivate() {
 			return toActivate;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public List<INavigationNode<?>> getToDeactivate() {
 			return toDeactivate;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
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
 	private INavigationNode<?> getChildToActivate(INavigationNode<?> pNode) {
 		// for sub module is always null
 
 		ISubModuleNode subModuleNode = pNode.getTypecastedAdapter(ISubModuleNode.class);
 		if (subModuleNode != null) {
 
 			if (!subModuleNode.isSelectable()) {
 				if (!subModuleNode.getChildren().isEmpty()) {
 					return findSelectableChildNode(subModuleNode);
 				}
 
 				throw new RuntimeException("submodule node that is selectable=false must have at least one child"); //$NON-NLS-1$
 			}
 
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
 					// find the first selectable node in the list of childs
 					return findSelectableChildNode(moduleNode.getChild(0));
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
 
 	private ISubModuleNode findSelectableChildNode(ISubModuleNode startNode) {
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
 		for (ISubModuleNode child : startNode.getChildren()) {
 			ISubModuleNode found = findSelectableChildNode(child);
 			if (found != null) {
 				return found;
 			}
 		}
 		return null;
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
 	 *            the parent to reset in
 	 * @param child
 	 *            the child to set as selected
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
		INavigationHistoryEvent event = new NavigationHistoryEvent(histBack
				.subList(0, Math.max(0, histBack.size() - 1)));
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
