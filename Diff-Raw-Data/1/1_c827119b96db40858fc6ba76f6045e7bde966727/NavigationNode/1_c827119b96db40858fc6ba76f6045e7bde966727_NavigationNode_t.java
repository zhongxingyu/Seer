 /*******************************************************************************
  * Copyright (c) 2007 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.model;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.riena.core.marker.IMarkable;
 import org.eclipse.riena.core.marker.IMarker;
 import org.eclipse.riena.core.marker.Markable;
 import org.eclipse.riena.navigation.IAction;
 import org.eclipse.riena.navigation.INavigationContext;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.INavigationNodeListener;
 import org.eclipse.riena.navigation.INavigationNodeListenerable;
 import org.eclipse.riena.navigation.INavigationProcessor;
 import org.eclipse.riena.navigation.IPresentation;
 import org.eclipse.riena.navigation.common.TypecastingObject;
 
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
 		extends TypecastingObject implements INavigationNode<C>, INavigationNodeListenerable<S, C, L> {
 
 	private State state;
 	private String label;
 	private String icon;
 	private boolean expanded;
 	private IPresentation presentation;
 	private INavigationProcessor navigationProcessor;
 	private List<C> children;
 	private boolean selected;
 	private List<L> listeners;
 	private List<ISimpleNavigationNodeListener> simpleListeners;
 	private IMarkable markable;
 	private Object context;
 	private Set<IAction> actions;
 
 	/**
 	 * Create a new instance with empty children list
 	 */
 	public NavigationNode() {
 		super();
 
 		listeners = new LinkedList<L>();
		simpleListeners = new LinkedList<ISimpleNavigationNodeListener>();
 		children = new LinkedList<C>();
 		markable = createMarkable();
 		actions = new LinkedHashSet<IAction>();
 		state = State.CREATED;
 		// TODO: scp How can we use IIconManager.DEFAULT_ICON
 		// icon = "0044";
 	}
 
 	/**
 	 * Create a new instance with empty children list
 	 */
 	public NavigationNode(String pLabel) {
 		this();
 		setLabel(pLabel);
 	}
 
 	/**
 	 * Create a new instance an initialize the list
 	 */
 	public NavigationNode(C... pChildren) {
 		this();
 		for (C next : pChildren) {
 			addChild(next);
 		}
 	}
 
 	/**
 	 * Create a new instance an initialize the list
 	 */
 	public NavigationNode(String pLabel, C... pChildren) {
 		this(pLabel);
 		for (C next : pChildren) {
 			addChild(next);
 		}
 	}
 
 	public void setPresentation(IPresentation pPresentation) {
 		presentation = pPresentation;
 		notifyPresentationChanged();
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyPresentationChanged() {
 		for (L next : getListeners()) {
 			next.presentationChanged((S) this);
 		}
 		for (ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.presentationChanged(this);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifySelectedChanged() {
 		for (L next : getListeners()) {
 			next.selectedChanged((S) this);
 		}
 		for (ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.selectedChanged(this);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyLabelChanged() {
 		for (L next : getListeners()) {
 			next.labelChanged((S) this);
 		}
 		for (ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.labelChanged(this);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyIconChanged() {
 		for (L next : getListeners()) {
 			next.iconChanged((S) this);
 		}
 		for (ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.iconChanged(this);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyChildAdded(C pChild) {
 		for (L next : getListeners()) {
 			next.childAdded((S) this, pChild);
 		}
 		for (ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.childAdded(this, pChild);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyActivated() {
 		for (L next : getListeners()) {
 			next.activated((S) this);
 		}
 		for (ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.activated(this);
 		}
 
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyBeforeActivated() {
 		for (L next : getListeners()) {
 			next.beforeActivated((S) this);
 		}
 		for (ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.beforeActivated(this);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyAfterActivated() {
 		for (L next : getListeners()) {
 			next.afterActivated((S) this);
 		}
 		for (ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.afterActivated(this);
 		}
 
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyDeactivated() {
 		for (L next : getListeners()) {
 			next.deactivated((S) this);
 		}
 		for (ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.deactivated(this);
 		}
 
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyBeforeDeactivated() {
 		for (L next : getListeners()) {
 			next.beforeDeactivated((S) this);
 		}
 		for (ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.beforeDeactivated(this);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyAfterDeactivated() {
 		for (L next : getListeners()) {
 			next.afterDeactivated((S) this);
 		}
 		for (ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.afterDeactivated(this);
 		}
 
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyChildRemoved(C pChild) {
 		for (L next : getListeners()) {
 			next.childRemoved((S) this, pChild);
 		}
 		for (ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.childRemoved(this, pChild);
 		}
 	}
 
 	public void setNavigationProcessor(INavigationProcessor pProcessor) {
 		navigationProcessor = pProcessor;
 	}
 
 	public void addChild(C pChild) {
 		if (pChild != null && !hasChild(pChild) && !pChild.isDisposed()) {
 			children.add(pChild);
 			addChildParent(pChild);
 			notifyChildAdded(pChild);
 		}
 		// TODO do something if the node cannot be added
 	}
 
 	protected boolean hasChild(INavigationNode<?> pChild) {
 		return children.contains(pChild);
 	}
 
 	protected void addChildParent(C child) {
 		child.setParent(this);
 	}
 
 	public List<C> getChildren() {
 		return children;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void removeChild(INavigationContext context, INavigationNode<?> child) {
 		if (child != null && hasChild(child) && !child.isActivated()) {
 			children.remove(child);
 			// if this node has the child, than it can be casted to C,
 			// because it must be C
 			notifyChildRemoved((C) child);
 			child.setParent(null);
 		}
 		// TODO do something if the node cannot be removed
 	}
 
 	public C getChild(int index) {
 		if (children != null && children.size() > index) {
 			return children.get(index);
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.IActivateable#activate()
 	 */
 	public void activate() {
 		getNavigationProcessor().activate(this);
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.IActivateable#allowsActivate(org.eclipse.riena.navigation.INavigationContext)
 	 */
 	public boolean allowsActivate(INavigationContext context) {
 		IPresentation localPresentation = getPresentation();
 		if (localPresentation != null) {
 			return localPresentation.allowsActivate(this, context);
 		} else {
 			return true;
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.IActivateable#allowsDeactivate(org.eclipse.riena.navigation.INavigationContext)
 	 */
 	public boolean allowsDeactivate(INavigationContext context) {
 		IPresentation localPresentation = getPresentation();
 		if (localPresentation != null) {
 			return localPresentation.allowsDeactivate(this, context);
 		} else {
 			return true;
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNodeListenerable#addListener(org.eclipse.riena.navigation.INavigationNodeListener)
 	 */
 	public void addListener(L listener) {
 		listeners.add(listener);
 
 	}
 
 	public void addSimpleListener(ISimpleNavigationNodeListener simpleListener) {
 		simpleListeners.add(simpleListener);
 	}
 
 	public void removeSimpleListener(ISimpleNavigationNodeListener simpleListener) {
 		simpleListeners.remove(simpleListener);
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNodeListenerable#removeListener(org.eclipse.riena.navigation.INavigationNodeListener)
 	 */
 	public void removeListener(L listener) {
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
 	public void setLabel(String label) {
 		this.label = label;
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
 	public void setIcon(String icon) {
 		this.icon = icon;
 		notifyIconChanged();
 	}
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		String llabel = getLabel();
 		if (llabel != null) {
 			return llabel;
 		}
 		return super.toString();
 	}
 
 	private INavigationNode<?> parent;
 	private boolean blocked;
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#getParent()
 	 */
 	public INavigationNode<?> getParent() {
 		return parent;
 	}
 
 	public void setParent(INavigationNode<?> pParent) {
 		parent = pParent;
 		notifyParentChanged();
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyParentChanged() {
 		for (L next : getListeners()) {
 			next.parentChanged((S) this);
 		}
 		for (ISimpleNavigationNodeListener next : getSimpleListeners()) {
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
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * @return the Presentation of this node
 	 */
 	public IPresentation getPresentation() {
 		return presentation;
 	}
 
 	/**
 	 * Look for the next in the hierarchy available presentation
 	 */
 	public IPresentation getNextPresentation() {
 		if (presentation != null) {
 			return presentation;
 		} else if (getParent() != null) {
 			return getParent().getPresentation();
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
 	public void setExpanded(boolean pExpanded) {
 
 		expanded = pExpanded;
 		notifyExpandedChanged();
 
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyExpandedChanged() {
 		for (L next : getListeners()) {
 			next.expandedChanged((S) this);
 		}
 		for (ISimpleNavigationNodeListener next : getSimpleListeners()) {
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
 
 	/**
 	 * @param marker
 	 * @see org.eclipse.riena.core.marker.IMarkable#addMarker(org.eclipse.riena.core.marker.IMarker)
 	 */
 	public void addMarker(IMarker marker) {
 		getMarkable().addMarker(marker);
 		notifyMarkersChanged();
 	}
 
 	/**
 	 * @return
 	 * @see org.eclipse.riena.core.marker.IMarkable#getMarkers()
 	 */
 	public Collection<? extends IMarker> getMarkers() {
 		return getMarkable().getMarkers();
 	}
 
 	/**
 	 * @param <T>
 	 * @param type
 	 * @return
 	 * @see org.eclipse.riena.core.marker.IMarkable#getMarkersOfType(java.lang.Class)
 	 */
 	public <T extends IMarker> Collection<T> getMarkersOfType(Class<T> type) {
 		return getMarkable().getMarkersOfType(type);
 	}
 
 	/**
 	 * 
 	 * @see org.eclipse.riena.core.marker.IMarkable#removeAllMarkers()
 	 */
 	public void removeAllMarkers() {
 		getMarkable().removeAllMarkers();
 		notifyMarkersChanged();
 	}
 
 	/**
 	 * @param marker
 	 * @see org.eclipse.riena.core.marker.IMarkable#removeMarker(org.eclipse.riena.core.marker.IMarker)
 	 */
 	public void removeMarker(IMarker marker) {
 		getMarkable().removeMarker(marker);
 		notifyMarkersChanged();
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyMarkersChanged() {
 		for (L next : getListeners()) {
 			next.markersChanged((S) this);
 		}
 		for (ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.markersChanged(this);
 		}
 	}
 
 	/**
 	 * @see java.lang.Object#getContext()
 	 */
 	public Object getContext() {
 		return context;
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#setContext(java.lang.Object)
 	 */
 	public void setContext(Object pContext) {
 		context = pContext;
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#addAction(org.eclipse.riena.navigation.IAction)
 	 */
 	public void addAction(IAction pAction) {
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
 		Set<IAction> allActions = new HashSet<IAction>();
 		allActions.addAll(getActions());
 		if (getParent() != null) {
 			allActions.addAll(getParent().getAllActions());
 		}
 		return allActions;
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#removeAction(org.eclipse.riena.navigation.IAction)
 	 */
 	public void removeAction(IAction pAction) {
 		actions.remove(pAction);
 	}
 
 	/**
 	 * TODO: hide the method!
 	 * 
 	 * @see org.eclipse.riena.navigation.INavigationNode#activate(INavigationContext)
 	 */
 	public void activate(INavigationContext context) {
 		setState(State.ACTIVATED);
 		notifyActivated();
 	}
 
 	/**
 	 * TODO: hide the method!
 	 * 
 	 * @see org.eclipse.riena.navigation.INavigationNode#onBeforeActivate(org.eclipse.riena.navigation.INavigationContext)
 	 */
 	public void onBeforeActivate(INavigationContext context) {
 		notifyBeforeActivated();
 	}
 
 	/**
 	 * TODO: hide the method!
 	 * 
 	 * @see org.eclipse.riena.navigation.INavigationNode#onAfterActivate(org.eclipse.riena.navigation.INavigationContext)
 	 */
 	public void onAfterActivate(INavigationContext context) {
 		notifyAfterActivated();
 	}
 
 	/**
 	 * TODO: hide the method!
 	 * 
 	 * @see org.eclipse.riena.navigation.INavigationNode#deactivate(INavigationContext)
 	 */
 	public void deactivate(INavigationContext context) {
 		setState(State.DEACTIVATED);
 		notifyDeactivated();
 	}
 
 	/**
 	 * TODO: hide the method!
 	 * 
 	 * @see org.eclipse.riena.navigation.INavigationNode#onBeforeDeactivate(org.eclipse.riena.navigation.INavigationContext)
 	 */
 	public void onBeforeDeactivate(INavigationContext context) {
 		notifyBeforeDeactivated();
 	}
 
 	/**
 	 * TODO: hide the method!
 	 * 
 	 * @see org.eclipse.riena.navigation.INavigationNode#onAfterDeactivate(org.eclipse.riena.navigation.INavigationContext)
 	 */
 	public void onAfterDeactivate(INavigationContext context) {
 		notifyAfterDeactivated();
 	}
 
 	private void setState(State pState) {
 		if (pState != state) {
 			State oldState = state;
 			state = pState;
 			notifyStateChanged(oldState, state);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyStateChanged(State oldState, State newState) {
 		for (L next : getListeners()) {
 			next.stateChanged((S) this, oldState, newState);
 		}
 		for (ISimpleNavigationNodeListener next : getSimpleListeners()) {
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
 	 *            - the selected to set
 	 */
 	public void setSelected(boolean selected) {
 		if (selected != this.selected) {
 			this.selected = selected;
 			notifySelectedChanged();
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#allowsDispose(org.eclipse.riena.navigation.INavigationContext)
 	 */
 	public boolean allowsDispose(INavigationContext context) {
 		IPresentation presentation = getPresentation();
 		if (presentation != null) {
 			return presentation.allowsDispose(this, context);
 		} else {
 			return true;
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#dispose()
 	 */
 	public void dispose() {
 		getNavigationProcessor().dispose(this);
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyDisposed() {
 		for (L next : getListeners()) {
 			next.disposed((S) this);
 		}
 		for (ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.disposed(this);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyBeforeDisposed() {
 		for (L next : getListeners()) {
 			next.beforeDisposed((S) this);
 		}
 		for (ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.beforeDisposed(this);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyAfterDisposed() {
 		for (L next : getListeners()) {
 			next.afterDisposed((S) this);
 		}
 		for (ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.afterDisposed(this);
 		}
 	}
 
 	/**
 	 * TODO: hide the method!
 	 * 
 	 * @see org.eclipse.riena.navigation.INavigationNode#dispose(org.eclipse.riena.navigation.INavigationContext)
 	 */
 	public void dispose(INavigationContext context) {
 		setState(State.DISPOSED);
 		notifyDisposed();
 	}
 
 	/**
 	 * TODO: hide the method!
 	 * 
 	 * @see org.eclipse.riena.navigation.INavigationNode#onBeforeDispose(org.eclipse.riena.navigation.INavigationContext)
 	 */
 	public void onBeforeDispose(INavigationContext context) {
 		notifyBeforeDisposed();
 	}
 
 	/**
 	 * TODO: hide the method!
 	 * 
 	 * @see org.eclipse.riena.navigation.INavigationNode#onAfterDispose(org.eclipse.riena.navigation.INavigationContext)
 	 */
 	public void onAfterDispose(INavigationContext context) {
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
 	public void setBlocked(boolean blocked) {
 
 		this.blocked = blocked;
 		notifyBlockedChanged();
 
 	}
 
 	@SuppressWarnings("unchecked")
 	private void notifyBlockedChanged() {
 		for (L next : getListeners()) {
 			next.block((S) this, isBlocked() && isActivated());
 		}
 		for (ISimpleNavigationNodeListener next : getSimpleListeners()) {
 			next.block(this, isBlocked() && isActivated());
 		}
 
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#getIndexOfChild(org.eclipse.riena.navigation.INavigationNode)
 	 */
 	public int getIndexOfChild(INavigationNode<?> child) {
 		return children.indexOf(child);
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNode#getParentOfType(java.lang.Class)
 	 */
 	public <N extends INavigationNode<?>> N getParentOfType(Class<N> clazz) {
 
 		if (getParent() == null) {
 			return null;
 		}
 
 		if (clazz.isAssignableFrom(getParent().getClass())) {
 			return (N) getParent();
 		} else {
 			return getParent().getParentOfType(clazz);
 		}
 	}
 }
