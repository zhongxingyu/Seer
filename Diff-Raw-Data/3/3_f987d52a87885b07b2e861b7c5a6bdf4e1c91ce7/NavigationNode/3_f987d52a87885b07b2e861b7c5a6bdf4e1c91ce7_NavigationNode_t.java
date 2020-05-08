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
 package org.eclipse.riena.navigation.model;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.Assert;
 
 import org.eclipse.riena.core.marker.IMarkable;
 import org.eclipse.riena.core.marker.IMarker;
 import org.eclipse.riena.core.marker.Markable;
 import org.eclipse.riena.navigation.ApplicationNodeManager;
 import org.eclipse.riena.navigation.IAction;
 import org.eclipse.riena.navigation.IJumpTargetListener;
 import org.eclipse.riena.navigation.INavigationContext;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.INavigationNodeController;
 import org.eclipse.riena.navigation.INavigationProcessor;
 import org.eclipse.riena.navigation.ISimpleNavigationNodeListener;
 import org.eclipse.riena.navigation.NavigationArgument;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.navigation.common.TypecastingObject;
 import org.eclipse.riena.navigation.listener.INavigationNodeListener;
 import org.eclipse.riena.navigation.listener.INavigationNodeListenerable;
 import org.eclipse.riena.ui.core.context.IContext;
 import org.eclipse.riena.ui.core.marker.DisabledMarker;
 import org.eclipse.riena.ui.core.marker.HiddenMarker;
 import org.eclipse.riena.ui.filter.IUIFilter;
 import org.eclipse.riena.ui.filter.IUIFilterable;
 import org.eclipse.riena.ui.filter.impl.UIFilterable;
 import org.eclipse.riena.ui.ridgets.tree2.ITreeNode2;
 
 /**
  * Default implementation of all features common to all navigation node objects
  * The parent-child relations are not included!
  * 
  * @param <S>
  *            the type of implemented node
  * @param <C>
  *            the type of the child nodes
  * @param <L>
  *            the type of the listener
  */
 public abstract class NavigationNode<S extends INavigationNode<C>, C extends INavigationNode<?>, L extends INavigationNodeListener<S, C>>
 		extends TypecastingObject implements INavigationNode<C>, INavigationNodeListenerable<S, C, L>, IContext {
 
 	private NavigationNodeId nodeId;
 
 	private State state;
 	private String label;
 	private String icon;
 	private boolean expanded;
 	private INavigationNodeController navigationNodeController;
 	private INavigationProcessor navigationProcessor;
 	private List<C> children;
 	private boolean selected;
 	private List<L> listeners;
 	private List<ISimpleNavigationNodeListener> simpleListeners;
 	private IMarkable markable;
 	private IUIFilterable filterable;
 	private Map<String, Object> context;
 	private Set<IAction> actions;
 	private PropertyChangeSupport propertyChangeSupport;
 	private IMarker hiddenMarker;
 	private IMarker disabledMarker;
 	private Boolean cachedVisible;
 	private Boolean cachedEnabled;
 	private boolean isNodeIdChange;
 
 	/**
 	 * Creates a NavigationNode.
 	 * 
 	 * @param nodeId
 	 *            Identifies the node in the application model tree.
 	 */
 	public NavigationNode(final NavigationNodeId nodeId) {
 		super();
 
 		this.nodeId = nodeId;
 		listeners = new LinkedList<L>();
 		propertyChangeSupport = new PropertyChangeSupport(this);
 		simpleListeners = new LinkedList<ISimpleNavigationNodeListener>();
 		children = new LinkedList<C>();
 		markable = createMarkable();
 		filterable = createFilterable();
 		actions = new LinkedHashSet<IAction>();
 		state = State.CREATED;
 		context = null;
 		isNodeIdChange = false;
 	}
 
 	/**
 	 * Creates a NavigationNode.
 	 * 
 	 * @param nodeId
 	 *            Identifies the node in the application model tree.
 	 * @param pLabel
 	 *            The label of the node.
 	 */
 	public NavigationNode(final NavigationNodeId nodeId, final String pLabel) {
 		this(nodeId);
 		setLabel(pLabel);
 	}
 
 	public void setNavigationNodeController(final INavigationNodeController pNavigationNodeController) {
 		navigationNodeController = pNavigationNodeController;
 		notifyControllerChanged();
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyControllerChanged() {
 		for (final L next : getListeners()) {
 			next.presentationChanged((S) this);
 		}
 		for (final ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.presentationChanged(this);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifySelectedChanged() {
 		for (final L next : getListeners()) {
 			next.selectedChanged((S) this);
 		}
 		for (final ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.selectedChanged(this);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyLabelChanged() {
 		for (final L next : getListeners()) {
 			next.labelChanged((S) this);
 		}
 		for (final ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.labelChanged(this);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyIconChanged() {
 		for (final L next : getListeners()) {
 			next.iconChanged((S) this);
 		}
 		for (final ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.iconChanged(this);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyChildAdded(final C pChild) {
 		for (final L next : getListeners()) {
 			next.childAdded((S) this, pChild);
 		}
 		for (final ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.childAdded(this, pChild);
 		}
 	}
 
 	/**
 	 * Notifies every registered listener that this node is prepared now.
 	 */
 	@SuppressWarnings("unchecked")
 	private void notifyPrepared() {
 		for (final L next : getListeners()) {
 			next.prepared((S) this);
 		}
 		for (final ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.prepared(this);
 		}
 
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyActivated() {
 		for (final L next : getListeners()) {
 			next.activated((S) this);
 		}
 		for (final ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.activated(this);
 		}
 
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyBeforeActivated() {
 		for (final L next : getListeners()) {
 			next.beforeActivated((S) this);
 		}
 		for (final ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.beforeActivated(this);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyAfterActivated() {
 		for (final L next : getListeners()) {
 			next.afterActivated((S) this);
 		}
 		for (final ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.afterActivated(this);
 		}
 
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyDeactivated() {
 		for (final L next : getListeners()) {
 			next.deactivated((S) this);
 		}
 		for (final ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.deactivated(this);
 		}
 
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyBeforeDeactivated() {
 		for (final L next : getListeners()) {
 			next.beforeDeactivated((S) this);
 		}
 		for (final ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.beforeDeactivated(this);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyAfterDeactivated() {
 		for (final L next : getListeners()) {
 			next.afterDeactivated((S) this);
 		}
 		for (final ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.afterDeactivated(this);
 		}
 
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyChildRemoved(final C pChild) {
 		for (final L next : getListeners()) {
 			next.childRemoved((S) this, pChild);
 		}
 		for (final ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.childRemoved(this, pChild);
 		}
 	}
 
 	public void setNavigationProcessor(final INavigationProcessor pProcessor) {
 		navigationProcessor = pProcessor;
 	}
 
 	/**
 	 * Checks if the given class (or a superclass) implements the correct type
 	 * of children.
 	 * 
 	 * @param childClass
 	 *            class of child
 	 */
 	protected boolean checkChildClass(final Class<?> childClass) {
 
 		Assert.isNotNull(childClass);
 
 		final Type[] types = childClass.getInterfaces();
 		for (final Type type : types) {
 			if (type == getValidChildType()) {
 				return true;
 			}
 			if (type == INavigationNode.class) {
 				return false;
 			}
 		}
 
 		final Class<?> superClass = childClass.getSuperclass();
 		if (superClass != null) {
 			return checkChildClass(superClass);
 		}
 
 		return false;
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void addChild(final C pChild) {
 		checkChild(pChild);
 		final List<C> oldList = new ArrayList<C>(children);
 		children.add(pChild);
 		fireChildAdded(pChild, oldList);
 		// Adds the parent to the child after all listeners are notified that the child was added to the parent!
 		addChildParent(pChild);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @since 2.0
 	 */
 	public void addChild(final int index, final C pChild) {
 		checkChild(pChild);
 		final List<C> oldList = new ArrayList<C>(children);
 		children.add(index, pChild);
 		fireChildAdded(pChild, oldList);
 		// Adds the parent to the child after all listeners are notified that the child was added to the parent!
 		addChildParent(pChild);
 	}
 
 	private void fireChildAdded(final C pChild, final List<C> oldList) {
 		propertyChangeSupport.firePropertyChange(INavigationNodeListenerable.PROPERTY_CHILDREN, oldList, children);
 		notifyChildAdded(pChild);
 	}
 
 	private void checkChild(final C pChild) {
 		if (pChild == null) {
 			throw new NavigationModelFailure("Cannot add null!"); //$NON-NLS-1$
 		}
 		if (hasChild(pChild)) {
 			throw new NavigationModelFailure("Child node \"" + pChild.toString() + "\" is already added!"); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		if (pChild.isDisposed()) {
 			throw new NavigationModelFailure("Cannot add disposed child node \"" + pChild.toString() + "\"!"); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		if (pChild == this) {
 			throw new NavigationModelFailure("Cannot add node \"" + pChild.toString() + "\" to itself!"); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		if (!checkChildClass(pChild.getClass())) {
 			String msg = "Cannot add \"" + pChild.toString() + "\" to \"" + this.toString() + "\"!"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			msg += " Because node isn't instance of " + getValidChildType().toString() + "."; //$NON-NLS-1$ //$NON-NLS-2$
 			throw new NavigationModelFailure(msg);
 		}
 	}
 
 	protected boolean hasChild(final INavigationNode<?> pChild) {
 		return children.contains(pChild);
 	}
 
 	protected void addChildParent(final C child) {
 		child.setParent(this);
 	}
 
 	public List<C> getChildren() {
 		return children;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void removeChild(final INavigationNode<?> child) {
 
 		if (child == null) {
 			throw new NavigationModelFailure("Cannot remove null!"); //$NON-NLS-1$
 		}
 		if (!hasChild(child)) {
 			throw new NavigationModelFailure(
 					"Node \"" + child.toString() + "\" isn't a child of \"" + this.toString() + "\"!"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		}
 		if (child.isActivated()) {
 			throw new NavigationModelFailure("Cannot remove active child \"" + child.toString() + "\"!"); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		final List<C> oldList = new ArrayList<C>(children);
 		children.remove(child);
 		child.setParent(null);
 
 		propertyChangeSupport.firePropertyChange(INavigationNodeListenerable.PROPERTY_CHILDREN, oldList, children);
 		// if this node has the child, than it can be casted to C,
 		// because it must be C
 		notifyChildRemoved((C) child);
 
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#getChild(int)
 	 */
 	public C getChild(final int index) {
 		if (children != null && children.size() > index) {
 			return children.get(index);
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#findNode(org.eclipse.riena.navigation.NavigationNodeId)
 	 */
 	public INavigationNode<?> findNode(final NavigationNodeId nodeId) {
 		if (getNodeId() != null && getNodeId().equals(nodeId)) {
 			return this;
 		}
 		for (final C child : children) {
 			final INavigationNode<?> foundChild = child.findNode(nodeId);
 			if (foundChild != null) {
 				return foundChild;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.IActivateable#activate()
 	 */
 	public void activate() {
 		getNavigationProcessor().activate(this);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @since 2.0
 	 */
 	public void prepare() {
 		getNavigationProcessor().prepare(this);
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.IActivateable#allowsActivate(org.eclipse.riena.navigation.INavigationContext)
 	 */
 	public boolean allowsActivate(final INavigationContext context) {
 		final INavigationNodeController localPresentation = getNavigationNodeController();
 		if (localPresentation != null) {
 			return localPresentation.allowsActivate(this, context);
 		} else {
 			return true;
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.IActivateable#allowsDeactivate(org.eclipse.riena.navigation.INavigationContext)
 	 */
 	public boolean allowsDeactivate(final INavigationContext context) {
 		final INavigationNodeController localPresentation = getNavigationNodeController();
 		if (localPresentation != null) {
 			return localPresentation.allowsDeactivate(this, context);
 		} else {
 			return true;
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.listener.INavigationNodeListenerable#addPropertyChangeListener(java.beans.PropertyChangeListener)
 	 */
 	public void addPropertyChangeListener(final PropertyChangeListener propertyChangeListener) {
 		propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.listener.INavigationNodeListenerable#
 	 *      removePropertyChangeListener ()
 	 */
 	public void removePropertyChangeListener(final PropertyChangeListener propertyChangeListener) {
 		propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.listener.INavigationNodeListenerable#addListener(org.eclipse.riena.navigation.listener.INavigationNodeListener)
 	 */
 	public void addListener(final L listener) {
 		listeners.add(listener);
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#addSimpleListener(org.eclipse
 	 *      .riena.navigation.model.ISimpleNavigationNodeListener)
 	 */
 	public void addSimpleListener(final ISimpleNavigationNodeListener simpleListener) {
 		simpleListeners.add(simpleListener);
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#removeSimpleListener(org
 	 *      .eclipse.riena.navigation.model.ISimpleNavigationNodeListener)
 	 */
 	public void removeSimpleListener(final ISimpleNavigationNodeListener simpleListener) {
 		simpleListeners.remove(simpleListener);
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.listener.INavigationNodeListenerable#removeListener(org.eclipse.riena.navigation.listener.INavigationNodeListener)
 	 */
 	public void removeListener(final L listener) {
 		listeners.remove(listener);
 
 	}
 
 	protected List<L> getListeners() {
 		return new LinkedList<L>(listeners);
 	}
 
 	protected List<ISimpleNavigationNodeListener> getSimpleListeners() {
 		return new LinkedList<ISimpleNavigationNodeListener>(simpleListeners);
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#getLabel()
 	 */
 	public String getLabel() {
 		return label;
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#setLabel(java.lang.String)
 	 */
 	public void setLabel(final String label) {
 		final String old = this.label;
 		this.label = label;
 		propertyChangeSupport.firePropertyChange(INavigationNode.PROPERTY_LABEL, old, label);
 		notifyLabelChanged();
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#getIcon()
 	 */
 	public String getIcon() {
 		return icon;
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#setIcon(java.lang.String)
 	 */
 	public void setIcon(final String icon) {
 		this.icon = icon;
 		notifyIconChanged();
 	}
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		final String llabel = getLabel();
 		String nodeIDString;
 		if (nodeId != null) {
 			nodeIDString = "nodeId=" + nodeId.getTypeId(); //$NON-NLS-1$
 		} else {
 			nodeIDString = "nodeId=null"; //$NON-NLS-1$
 		}
 		if (llabel != null) {
 			return llabel + " " + nodeIDString; //$NON-NLS-1$
 		} else {
 			return "no label " + nodeIDString; //$NON-NLS-1$
 		}
 	}
 
 	private INavigationNode<?> parent;
 	private boolean blocked;
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#getParent()
 	 */
 	public INavigationNode<?> getParent() {
 		return parent;
 	}
 
 	public void setParent(final INavigationNode<?> pParent) {
 		parent = pParent;
 		notifyParentChanged();
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyParentChanged() {
 		for (final L next : getListeners()) {
 			next.parentChanged((S) this);
 		}
 		for (final ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.parentChanged(this);
 		}
 	}
 
 	/**
 	 * Look for the navigation processor in the hierarchy. The navigation
 	 * processor can be set at any level
 	 */
 	public INavigationProcessor getNavigationProcessor() {
 		if (navigationProcessor != null) {
 			return navigationProcessor;
 		} else if (getParent() != null) {
 			return getParent().getNavigationProcessor();
 		} else if (ApplicationNodeManager.getApplicationNode() != null) {
 			// if no navigation processor was found in the hierarchy, maybe the application node has one
 			return ApplicationNodeManager.getApplicationNode().getNavigationProcessor();
 		} else {
 			// if nobody ha a navigation processor, return the default navigation processor
 			return ApplicationNodeManager.getDefaultNavigationProcessor();
 		}
 	}
 
 	/**
 	 * @return the controller of this node
 	 */
 	public INavigationNodeController getNavigationNodeController() {
 		return navigationNodeController;
 	}
 
 	/**
 	 * Look for the next in the hierarchy available controller
 	 */
 	public INavigationNodeController getNextNavigationNodeController() {
 		if (navigationNodeController != null) {
 			return navigationNodeController;
 		} else if (getParent() != null) {
 			return getParent().getNavigationNodeController();
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#isExpanded()
 	 */
 	public boolean isExpanded() {
 		return expanded;
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#setExpanded(boolean)
 	 */
 	public void setExpanded(final boolean pExpanded) {
 
 		//		if (expanded != pExpanded) {
 		expanded = pExpanded;
 		notifyExpandedChanged();
 		//		}
 
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyExpandedChanged() {
 		for (final L next : getListeners()) {
 			next.expandedChanged((S) this);
 		}
 		for (final ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.expandedChanged(this);
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#isLeaf()
 	 */
 	public boolean isLeaf() {
 		return getChildren().size() < 1;
 	}
 
 	/**
 	 * Each node has its own markable
 	 * 
 	 * @return the markable helper object of this node
 	 */
 	public IMarkable getMarkable() {
 		return markable;
 	}
 
 	/**
 	 * Creates the markable, can return null, then will be the markable from the
 	 * parent used
 	 * 
 	 * @return a Markable or null
 	 */
 	protected IMarkable createMarkable() {
 		return new Markable();
 	}
 
 	public Collection<? extends IMarker> getMarkers() {
 		return getMarkable().getMarkers();
 	}
 
 	private void clearCachedValues() {
 		cachedVisible = null;
 		cachedEnabled = null;
 	}
 
 	public void addMarker(final IMarker marker) {
 		final INavigationProcessor proc = getNavigationProcessor();
 		if (proc != null) {
 			final boolean oldEnabled = isEnabled();
 			final boolean oldVisible = isVisible();
 			proc.addMarker(this, marker);
 			if (oldEnabled != isEnabled()) {
 				propertyChangeSupport.firePropertyChange(ITreeNode2.PROPERTY_ENABLED, oldEnabled, isEnabled());
 			}
 			if (oldVisible != isVisible()) {
 				propertyChangeSupport.firePropertyChange(ITreeNode2.PROPERTY_VISIBLE, oldVisible, isVisible());
 			}
 		}
 	}
 
 	public void addMarker(final INavigationContext context, final IMarker marker) {
 		getMarkable().addMarker(marker);
 		clearCachedValues();
 		notifyMarkersChanged(marker);
 		if ((marker instanceof DisabledMarker) || (marker instanceof HiddenMarker)) {
 			for (final C child : getChildren()) {
 				child.addMarker(marker);
 			}
 		}
 	}
 
 	/**
 	 * @param <T>
 	 * @param type
 	 * @return
 	 * @see org.eclipse.riena.core.marker.IMarkable#getMarkersOfType(java.lang.Class)
 	 */
 	public <T extends IMarker> Collection<T> getMarkersOfType(final Class<T> type) {
 		return getMarkable().getMarkersOfType(type);
 	}
 
 	/**
 	 * 
 	 * @see org.eclipse.riena.core.marker.IMarkable#removeAllMarkers()
 	 */
 	public void removeAllMarkers() {
 		if (getMarkable().getMarkers().isEmpty()) {
 			return;
 		}
 		final boolean oldEnabled = isEnabled();
 		final boolean oldVisible = isVisible();
 		// getMarkable().removeAllMarkers();
 		while (!getMarkable().getMarkers().isEmpty()) {
 			final IMarker marker = getMarkable().getMarkers().iterator().next();
 			getMarkable().removeMarker(marker);
 			clearCachedValues();
 			notifyMarkersChanged(marker);
 		}
 		if (oldEnabled != isEnabled()) {
 			propertyChangeSupport.firePropertyChange(ITreeNode2.PROPERTY_ENABLED, oldEnabled, isEnabled());
 		}
 		if (oldVisible != isVisible()) {
 			propertyChangeSupport.firePropertyChange(ITreeNode2.PROPERTY_VISIBLE, oldVisible, isVisible());
 		}
 	}
 
 	/**
 	 * @param marker
 	 * @see org.eclipse.riena.core.marker.IMarkable#removeMarker(org.eclipse.riena.core.marker.IMarker)
 	 * @since 3.0
 	 */
 	public boolean removeMarker(final IMarker marker) {
 		final boolean removedMarker = getMarkable().removeMarker(marker);
 		if (!removedMarker) {
 			return false;
 		}
 		final boolean oldEnabled = isEnabled();
 		final boolean oldVisible = isVisible();
 		clearCachedValues();
 		if (oldEnabled != isEnabled()) {
 			propertyChangeSupport.firePropertyChange(ITreeNode2.PROPERTY_ENABLED, oldEnabled, isEnabled());
 		}
 		if (oldVisible != isVisible()) {
 			propertyChangeSupport.firePropertyChange(ITreeNode2.PROPERTY_VISIBLE, oldVisible, isVisible());
 		}
 		notifyMarkersChanged(marker);
 		if ((marker instanceof DisabledMarker) || (marker instanceof HiddenMarker)) {
 			for (final C child : getChildren()) {
 				child.removeMarker(marker);
 			}
 		}
 		return true;
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyMarkersChanged(final IMarker marker) {
 		for (final L next : getListeners()) {
 			next.markerChanged((S) this, marker);
 		}
 		for (final ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.markerChanged(this, marker);
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#getContext()
 	 */
 	public Object getContext(final String key) {
 		if (context == null) {
 			return null;
 		}
 		return context.get(key);
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#setContext(String,
 	 *      Object)
 	 */
 	public void setContext(final String key, final Object value) {
 		if (context == null) {
 			context = new HashMap<String, Object>();
 		}
 		checkForNavigationArgumentUpdate(key, value);
 		context.put(key, value);
 	}
 
 	private void checkForNavigationArgumentUpdate(final String key, final Object value) {
 		if (NavigationArgument.CONTEXTKEY_ARGUMENT.equals(key) && getNavigationNodeController() != null) {
 			final NavigationArgument oldArgument = (NavigationArgument) getContext(NavigationArgument.CONTEXTKEY_ARGUMENT);
 			if (oldArgument == null || !oldArgument.equals(value)) {
 				getNavigationNodeController().navigationArgumentChanged((NavigationArgument) value);
 			}
 		}
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public void removeContext(final String key) {
 		if (context != null) {
 			context.remove(key);
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#addAction(org.eclipse.riena.navigation.IAction)
 	 */
 	public void addAction(final IAction pAction) {
 		actions.add(pAction);
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#getActions()
 	 */
 	public Set<IAction> getActions() {
 		return actions;
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#getAllActions()
 	 */
 	public Set<IAction> getAllActions() {
 		final Set<IAction> allActions = new HashSet<IAction>();
 		allActions.addAll(getActions());
 		if (getParent() != null) {
 			allActions.addAll(getParent().getAllActions());
 		}
 		return allActions;
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#removeAction(org.eclipse.riena.navigation.IAction)
 	 */
 	public void removeAction(final IAction pAction) {
 		actions.remove(pAction);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * Changes the state and notifies listeners.
 	 * 
 	 * @param context
 	 *            is not used; only <b>this</b> node is prepared
 	 * 
 	 * @since 2.0
 	 */
 	public void prepare(final INavigationContext context) {
 		Assert.isTrue(isCreated() || isPrepared());
 		setState(State.PREPARED);
 		notifyPrepared();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void activate(final INavigationContext context) {
 		setState(State.ACTIVATED);
 		notifyActivated();
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#onBeforeActivate(org.eclipse.riena.navigation.INavigationContext)
 	 */
 	public void onBeforeActivate(final INavigationContext context) {
 		notifyBeforeActivated();
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#onAfterActivate(org.eclipse.riena.navigation.INavigationContext)
 	 */
 	public void onAfterActivate(final INavigationContext context) {
 		notifyAfterActivated();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void deactivate(final INavigationContext context) {
 		setState(State.DEACTIVATED);
 		notifyDeactivated();
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#onBeforeDeactivate(org.eclipse.riena.navigation.INavigationContext)
 	 */
 	public void onBeforeDeactivate(final INavigationContext context) {
 		notifyBeforeDeactivated();
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#onAfterDeactivate(org.eclipse.riena.navigation.INavigationContext)
 	 */
 	public void onAfterDeactivate(final INavigationContext context) {
 		notifyAfterDeactivated();
 	}
 
 	private void setState(final State pState) {
 		if (pState != state) {
 			final State oldState = state;
 			state = pState;
 			notifyStateChanged(oldState, state);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyStateChanged(final State oldState, final State newState) {
 		for (final L next : getListeners()) {
 			next.stateChanged((S) this, oldState, newState);
 		}
 		for (final ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.stateChanged(this, oldState, newState);
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#getState()
 	 */
 	public State getState() {
 		return state;
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#isActivated()
 	 */
 	public boolean isActivated() {
 		return state == State.ACTIVATED;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @since 2.0
 	 */
 	public boolean isPrepared() {
 		return state == State.PREPARED;
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#isCreated()
 	 */
 	public boolean isCreated() {
 		return state == State.CREATED;
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#isDeactivated()
 	 */
 	public boolean isDeactivated() {
 		return state == State.DEACTIVATED;
 	}
 
 	/**
 	 * @return the selected
 	 */
 	public boolean isSelected() {
 		return selected;
 	}
 
 	/**
 	 * @param selected
 	 *            the selected to set
 	 */
 	public void setSelected(final boolean selected) {
 		if (selected != this.selected) {
 			this.selected = selected;
 			notifySelectedChanged();
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#allowsDispose(org.eclipse.riena.navigation.INavigationContext)
 	 */
 	public boolean allowsDispose(final INavigationContext context) {
 		final INavigationNodeController pres = getNavigationNodeController();
 		if (pres != null) {
 			return pres.allowsDispose(this, context);
 		} else {
 			return true;
 		}
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	public void dispose() {
 		getNavigationProcessor().dispose(this);
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyDisposed() {
 		for (final L next : getListeners()) {
 			next.disposed((S) this);
 		}
 		for (final ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.disposed(this);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyBeforeDisposed() {
 		for (final L next : getListeners()) {
 			next.beforeDisposed((S) this);
 		}
 		for (final ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.beforeDisposed(this);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyAfterDisposed() {
 		for (final L next : getListeners()) {
 			next.afterDisposed((S) this);
 		}
 		for (final ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.afterDisposed(this);
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#dispose(org.eclipse.riena.navigation.INavigationContext)
 	 */
 	public void dispose(final INavigationContext context) {
 		setState(State.DISPOSED);
 		notifyDisposed();
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#onBeforeDispose(org.eclipse.riena.navigation.INavigationContext)
 	 */
 	public void onBeforeDispose(final INavigationContext context) {
 		notifyBeforeDisposed();
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#onAfterDispose(org.eclipse.riena.navigation.INavigationContext)
 	 */
 	public void onAfterDispose(final INavigationContext context) {
 		notifyAfterDisposed();
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#isDisposed()
 	 */
 	public boolean isDisposed() {
 		return getState() == State.DISPOSED;
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#isBlocked()
 	 */
 	public boolean isBlocked() {
 		return blocked;
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#setBlocked(boolean)
 	 */
 	public void setBlocked(final boolean blocked) {
 		this.blocked = blocked;
 		notifyBlockedChanged();
		for (final INavigationNode<?> child : getChildren()) {
			child.setBlocked(blocked);
		}
 	}
 
 	/**
 	 * If the node (and no parent node) hasn't a {@link DisabledMarker} the node
 	 * is enabled; otherwise the node is disabled.
 	 * 
 	 * @see org.eclipse.riena.navigation.INavigationNode#isEnabled()
 	 */
 	public boolean isEnabled() {
 		return isEnabled(this);
 	}
 
 	private boolean isEnabled(final NavigationNode<?, ?, ?> node) {
 		//cachedEnabled = null; // TURN OFF CACHE
 		if (node.cachedEnabled == null) {
 			node.cachedEnabled = node.getMarkersOfType(DisabledMarker.class).isEmpty();
 		}
 		boolean enabled = node.cachedEnabled;
 		if (enabled && (node.getParent() != null)) {
 			enabled = isEnabled((NavigationNode<?, ?, ?>) node.getParent());
 		}
 		return enabled;
 	}
 
 	/**
 	 * Adds {@link DisabledMarker} if {@code enabled} is {@code false}. Removes
 	 * {@link DisabledMarker} if {@code enabled} is {@code true}.
 	 * 
 	 * @see org.eclipse.riena.navigation.INavigationNode#setEnabled(boolean)
 	 */
 	public void setEnabled(final boolean enabled) {
 
 		if (disabledMarker == null) {
 			disabledMarker = new DisabledMarker();
 		}
 
 		if (enabled) {
 			removeMarker(disabledMarker);
 		} else {
 			addMarker(disabledMarker);
 		}
 
 	}
 
 	/**
 	 * If the node (and no parent) hasn't a {@link HiddenMarker} the node is
 	 * visible; otherwise the node is hidden.
 	 * 
 	 * @see org.eclipse.riena.navigation.INavigationNode#isEnabled()
 	 */
 	public boolean isVisible() {
 		return isVisible(this);
 	}
 
 	private boolean isVisible(final NavigationNode<?, ?, ?> node) {
 		//cachedVisible = null; // TURN OFF CACHE
 		if (node.cachedVisible == null) {
 			node.cachedVisible = node.getMarkersOfType(HiddenMarker.class).isEmpty();
 		}
 		boolean visible = node.cachedVisible;
 		if (visible && (node.getParent() != null)) {
 			visible = isVisible((NavigationNode<?, ?, ?>) node.getParent());
 		}
 		return visible;
 	}
 
 	/**
 	 * Adds {@link HiddenMarker} if {@code visible} is {@code false}. Removes
 	 * {@link HiddenMarker} if {@code visible} is {@code true}.
 	 * 
 	 * @see org.eclipse.riena.navigation.INavigationNode#setEnabled(boolean)
 	 */
 	public final void setVisible(final boolean visible) {
 
 		if (hiddenMarker == null) {
 			hiddenMarker = new HiddenMarker();
 		}
 
 		if (visible) {
 			removeMarker(hiddenMarker);
 		} else {
 			addMarker(hiddenMarker);
 		}
 
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyBlockedChanged() {
 		for (final L next : getListeners()) {
 			next.block((S) this, isBlocked() /* && isActivated() */);
 		}
 		for (final ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.block(this, isBlocked() /* && isActivated() */);
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#getIndexOfChild(org.eclipse.riena.navigation.INavigationNode)
 	 */
 	public int getIndexOfChild(final INavigationNode<?> child) {
 		return children.indexOf(child);
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#getParentOfType(java.lang.Class)
 	 */
 	@SuppressWarnings("unchecked")
 	public <N extends INavigationNode<?>> N getParentOfType(final Class<N> clazz) {
 
 		if (getParent() == null) {
 			return null;
 		}
 
 		if (clazz.isAssignableFrom(getParent().getClass())) {
 			return (N) getParent();
 		} else {
 			return getParent().getParentOfType(clazz);
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#create(org.eclipse.riena.navigation.NavigationNodeId)
 	 */
 	public void create(final NavigationNodeId targetId) {
 		getNavigationProcessor().create(this, targetId);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.riena.navigation.INavigationNode#create(org.eclipse.riena
 	 * .navigation.NavigationNodeId,
 	 * org.eclipse.riena.navigation.NavigationArgument)
 	 */
 	public void create(final NavigationNodeId targetId, final NavigationArgument argument) {
 		getNavigationProcessor().create(this, targetId, argument);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.riena.navigation.INavigationNode#moveTo(org.eclipse.riena
 	 * .navigation.NavigationNodeId)
 	 */
 	public void moveTo(final NavigationNodeId targetId) {
 		throw new UnsupportedOperationException("Only ModuleNodes can be moved to a new target."); //$NON-NLS-1$
 
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#navigate(org.eclipse.riena.navigation.NavigationNodeId)
 	 */
 	public void navigate(final NavigationNodeId targetId) {
 		navigate(targetId, null);
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#navigate(org.eclipse.riena.navigation.NavigationNodeId,
 	 *      org.eclipse.riena.navigation.NavigationArgument)
 	 */
 	public void navigate(final NavigationNodeId targetId, final NavigationArgument argument) {
 		getNavigationProcessor().navigate(this, targetId, argument);
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#navigateBack()
 	 */
 	public void navigateBack() {
 		if (getNavigationProcessor() != null) {
 			getNavigationProcessor().navigateBack(this);
 		}
 	}
 
 	/**
 	 * @see INavigationNode#jump(NavigationNodeId)
 	 */
 	public void jump(final NavigationNodeId targetId) {
 		jump(targetId, null);
 	}
 
 	/**
 	 * @see INavigationNode#jump(NavigationNodeId, NavigationArgument)
 	 */
 	public void jump(final NavigationNodeId targetId, final NavigationArgument argument) {
 		getNavigationProcessor().jump(this, targetId, argument);
 	}
 
 	/**
 	 * @see INavigationNode#jumpBack(NavigationNodeId)
 	 */
 	public void jumpBack() {
 		getNavigationProcessor().jumpBack(this);
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#isJumpTarget()
 	 */
 	public boolean isJumpTarget() {
 		return getNavigationProcessor().isJumpTarget(this);
 	}
 
 	/**
 	 * @see INavigationNode#addJumpTargetListener(IJumpTargetListener)
 	 */
 	public void addJumpTargetListener(final IJumpTargetListener listener) {
 		getNavigationProcessor().addJumpTargetListener(this, listener);
 
 	}
 
 	/**
 	 * @see INavigationNode#removeJumpTargetListener(IJumpTargetListener)
 	 */
 	public void removeJumpTargetListener(final IJumpTargetListener listener) {
 		getNavigationProcessor().removeJumpTargetListener(this, listener);
 
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationHistory#historyBack()
 	 */
 	public void historyBack() {
 		if (getNavigationProcessor() != null) {
 			getNavigationProcessor().historyBack();
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationHistory#historyPrev()
 	 */
 	public void historyForward() {
 		if (getNavigationProcessor() != null) {
 			getNavigationProcessor().historyForward();
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#getNodeId()
 	 */
 	public NavigationNodeId getNodeId() {
 		return nodeId;
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#setNodeId(org.eclipse.riena.navigation.NavigationNodeId)
 	 */
 	public void setNodeId(final NavigationNodeId nodeId) {
 		if (!isNodeIdChange) {
 			isNodeIdChange = true;
 			notifyNodeIdChange(nodeId);
 			isNodeIdChange = false;
 		}
 		this.nodeId = nodeId;
 	}
 
 	/**
 	 * Creates the UI filterable.
 	 * 
 	 * @return a UI filterable
 	 */
 	protected IUIFilterable createFilterable() {
 		return new UIFilterable();
 	}
 
 	private IUIFilterable getFilterable() {
 		return filterable;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void addFilter(final IUIFilter filter) {
 		getFilterable().addFilter(filter);
 		notifyFilterAdded(filter);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void removeFilter(final IUIFilter filter) {
 		getFilterable().removeFilter(filter);
 		notifyFilterRemoved(filter);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void removeFilter(final String filterID) {
 
 		final Collection<? extends IUIFilter> filters = getFilters();
 
 		final List<IUIFilter> toRemove = new ArrayList<IUIFilter>();
 		for (final IUIFilter filter : filters) {
 			if (filter.getFilterID() != null && filter.getFilterID().equals(filterID)) {
 				toRemove.add(filter);
 			}
 		}
 
 		for (final IUIFilter filter : toRemove) {
 			removeFilter(filter);
 		}
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void removeAllFilters() {
 		final Collection<? extends IUIFilter> filters = new ArrayList<IUIFilter>(getFilters());
 		getFilterable().removeAllFilters();
 		for (final IUIFilter filter : filters) {
 			notifyFilterRemoved(filter);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Collection<? extends IUIFilter> getFilters() {
 		return getFilterable().getFilters();
 	}
 
 	/**
 	 * Notifies every interested listener that the filters have changed.
 	 */
 	@SuppressWarnings("unchecked")
 	private void notifyFilterAdded(final IUIFilter filter) {
 		for (final L next : getListeners()) {
 			next.filterAdded((S) this, filter);
 		}
 		for (final ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.filterAdded(this, filter);
 		}
 	}
 
 	/**
 	 * Notifies every interested listener that the filters have changed.
 	 */
 	@SuppressWarnings("unchecked")
 	private void notifyFilterRemoved(final IUIFilter filter) {
 		for (final L next : getListeners()) {
 			next.filterRemoved((S) this, filter);
 		}
 		for (final ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.filterRemoved(this, filter);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyNodeIdChange(final NavigationNodeId newId) {
 		final NavigationNodeId oldId = getNodeId();
 		for (final L next : getListeners()) {
 			next.nodeIdChange((S) this, oldId, newId);
 		}
 		for (final ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.nodeIdChange(this, oldId, newId);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.navigation.INavigationNode#getNavigationArgument()
 	 */
 	public NavigationArgument getNavigationArgument() {
 		final NavigationArgument navigationArgument = (NavigationArgument) getContext(NavigationArgument.CONTEXTKEY_ARGUMENT);
 		if (navigationArgument != null) {
 			return navigationArgument;
 		}
 		if (getParent() != null) {
 			return getParent().getNavigationArgument();
 		}
 		return navigationArgument;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
 		//result = prime * result + ((getLabel() == null) ? 0 : getLabel().hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(final Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		final NavigationNode<?, ?, ?> other = (NavigationNode<?, ?, ?>) obj;
 		//		if (getLabel() == null) {
 		//			if (other.getLabel() != null) {
 		//				return false;
 		//			}
 		//		} else if (!getLabel().equals(other.getLabel())) {
 		//			return false;
 		//		}
 		if (nodeId == null) {
 			return (other.nodeId == null);
 		}
 		return nodeId.equals(other.nodeId);
 	}
 }
