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
 package org.eclipse.riena.navigation.ui.controllers;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import org.eclipse.riena.core.marker.IMarker;
 import org.eclipse.riena.navigation.INavigationContext;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.INavigationNodeController;
 import org.eclipse.riena.navigation.common.TypecastingObject;
 import org.eclipse.riena.navigation.listener.INavigationNodeListenerable;
 import org.eclipse.riena.navigation.listener.NavigationNodeListener;
 import org.eclipse.riena.ui.core.resource.IIconManager;
 import org.eclipse.riena.ui.core.resource.IconManagerAccessor;
 import org.eclipse.riena.ui.core.resource.internal.IconSize;
 import org.eclipse.riena.ui.filter.IUIFilter;
 import org.eclipse.riena.ui.filter.IUIFilterAttribute;
 import org.eclipse.riena.ui.ridgets.IMarkableRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.IRidgetContainer;
 import org.eclipse.riena.ui.ridgets.IWindowRidget;
 import org.eclipse.riena.ui.ridgets.controller.IController;
 import org.eclipse.riena.ui.ridgets.filter.RidgetUIFilterAttributeMarker;
 import org.eclipse.riena.ui.ridgets.filter.RidgetUIFilterAttributeVisible;
 
 /**
  * An abstract controller superclass that manages the navigation node of a
  * controller N - Type of the Navigation node
  */
 public abstract class NavigationNodeController<N extends INavigationNode<?>> extends TypecastingObject implements
 		INavigationNodeController, IController {
 
 	private N navigationNode;
 	private Map<String, IRidget> ridgets;
 	private PropertyChangeListener propertyChangeListener;
 	private MyNavigationNodeListener nodeListener;
 
 	/**
 	 * Create a new Navigation Node view Controller. Set the navigation node
 	 * later.
 	 */
 	public NavigationNodeController() {
 		this(null);
 	}
 
 	/**
 	 * Create a new Navigation Node view Controller on the specified
 	 * navigationNode. Register this controller as the presentation of the
 	 * Navigation node.
 	 * 
 	 * @param navigationNode
 	 *            - the node to work on
 	 */
 	public NavigationNodeController(N navigationNode) {
 
 		ridgets = new HashMap<String, IRidget>();
 		propertyChangeListener = new PropertyChangeHandler();
 		nodeListener = new MyNavigationNodeListener();
 
 		if (navigationNode != null) {
 			setNavigationNode(navigationNode);
 		}
 
 	}
 
 	/**
 	 * @return the navigationNode
 	 */
 	public N getNavigationNode() {
 		return navigationNode;
 	}
 
 	/**
 	 * @param navigationNode
 	 *            the navigationNode to set
 	 */
 	public void setNavigationNode(N navigationNode) {
 		if (getNavigationNode() instanceof INavigationNodeListenerable) {
 			((INavigationNodeListenerable) getNavigationNode()).removeListener(nodeListener);
 		}
 		this.navigationNode = navigationNode;
 		navigationNode.setNavigationNodeController(this);
 		if (getNavigationNode() instanceof INavigationNodeListenerable) {
 			((INavigationNodeListenerable) getNavigationNode()).addListener(nodeListener);
 		}
 	}
 
 	/**
 	 * Overwrite in concrete subclass
 	 * 
 	 * @see org.eclipse.riena.navigation.IActivateable#allowsActivate(org.eclipse.riena.navigation.INavigationNode)
 	 */
 	public boolean allowsActivate(INavigationNode<?> pNode, INavigationContext context) {
 		return true;
 	}
 
 	/**
 	 * Overwrite in concrete subclass
 	 * 
 	 * @see org.eclipse.riena.navigation.IActivateable#allowsDeactivate(org.eclipse.riena.navigation.INavigationNode)
 	 */
 	public boolean allowsDeactivate(INavigationNode<?> pNode, INavigationContext context) {
 		return true;
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.controller.IController#afterBind()
 	 */
 	public void afterBind() {
 		updateNavigationNodeMarkers();
 	}
 
 	/**
 	 * @return true if the controller is activated
 	 */
 	public boolean isActivated() {
 		return getNavigationNode() != null && getNavigationNode().isActivated();
 	}
 
 	/**
 	 * @return true if the controller is activated
 	 */
 	public boolean isDeactivated() {
 		return getNavigationNode() == null || getNavigationNode().isDeactivated();
 	}
 
 	/**
 	 * @return true if the controller is activated
 	 */
 	public boolean isCreated() {
 		return getNavigationNode() == null || getNavigationNode().isCreated();
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationNodeController#allowsDispose(org.eclipse.riena.navigation.INavigationNode,
 	 *      org.eclipse.riena.navigation.INavigationContext)
 	 */
 	public boolean allowsDispose(INavigationNode<?> node, INavigationContext context) {
 		return true;
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.IRidgetContainer#addRidget(java.lang.String,
 	 *      org.eclipse.riena.ui.ridgets.IRidget)
 	 */
 	public void addRidget(String id, IRidget ridget) {
 		ridget.addPropertyChangeListener(IMarkableRidget.PROPERTY_MARKER, propertyChangeListener);
 		ridgets.put(id, ridget);
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.IRidgetContainer#getRidget(java.lang.String)
 	 */
 	public IRidget getRidget(String id) {
 		return ridgets.get(id);
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.IRidgetContainer#getRidgets()
 	 */
 	public Collection<? extends IRidget> getRidgets() {
 		return ridgets.values();
 	}
 
 	private Collection<IMarker> getRidgetMarkers() {
 
 		Collection<IMarker> combinedMarkers = new HashSet<IMarker>();
 
 		addRidgetMarkers(this, combinedMarkers);
 
 		return combinedMarkers;
 	}
 
 	private void addRidgetMarkers(IRidget ridget, Collection<IMarker> combinedMarkers) {
 
 		if (ridget instanceof IMarkableRidget) {
 			// TODO: scp ridget.isShowing()
 			// if (ridget instanceof IMarkableRidget && ((IMarkableRidget)
 			// ridget).isShowing()) {
 			addRidgetMarkers((IMarkableRidget) ridget, combinedMarkers);
 		} else if (ridget instanceof IRidgetContainer) {
 			addRidgetMarkers((IRidgetContainer) ridget, combinedMarkers);
 		}
 	}
 
 	private void addRidgetMarkers(IMarkableRidget ridget, Collection<IMarker> combinedMarkers) {
 		combinedMarkers.addAll(ridget.getMarkers());
 	}
 
 	private void addRidgetMarkers(IRidgetContainer ridgetContainer, Collection<IMarker> combinedMarkers) {
 		for (IRidget ridget : ridgetContainer.getRidgets()) {
 			addRidgetMarkers(ridget, combinedMarkers);
 		}
 	}
 
 	protected void updateNavigationNodeMarkers() {
 
 		getNavigationNode().removeAllMarkers();
 		for (IMarker marker : getRidgetMarkers()) {
 			getNavigationNode().addMarker(marker);
 		}
 	}
 
 	protected void updateIcon(IWindowRidget windowRidget) {
 
 		if (windowRidget == null) {
 			return;
 		}
 
 		String nodeIcon = getNavigationNode().getIcon();
 		if (nodeIcon != null) {
 			IIconManager iconManager = IconManagerAccessor.fetchIconManager();
 			if (!iconManager.hasExtension(nodeIcon)) {
 				nodeIcon = iconManager.getIconID(nodeIcon, IconSize.A);
 			}
 		}
 		windowRidget.setIcon(nodeIcon);
 	}
 
 	private class PropertyChangeHandler implements PropertyChangeListener {
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @seejava.beans.PropertyChangeListener#propertyChange(java.beans.
 		 * PropertyChangeEvent)
 		 */
 		public void propertyChange(PropertyChangeEvent evt) {
 
 			updateNavigationNodeMarkers();
 		}
 	}
 
 	// public IProgressVisualizer getProgressVisualizer(Object context) {
 	// return new ProgressVisualizer();
 	// }
 
 	public void setBlocked(boolean blocked) {
 		if (getNavigationNode() != null) {
 			getNavigationNode().setBlocked(blocked);
 		}
 
 	}
 
 	public boolean isBlocked() {
 		return getNavigationNode() != null && getNavigationNode().isBlocked();
 	}
 
 	public NavigationNodeController<?> getParentController() {
 		if ((getNavigationNode() != null) && (getNavigationNode().getParent() == null)) {
 			return null;
 		} else {
 			return (NavigationNodeController<?>) navigationNode.getParent().getNavigationNodeController();
 		}
 	}
 
 	private class MyNavigationNodeListener extends NavigationNodeListener {
 
 		public void applyFilters(Collection<? extends IUIFilter> filters) {
 
 			for (IUIFilter filter : filters) {
				Collection<? extends IUIFilterAttribute> filterItems = filter.getFilterItems();
 				for (IUIFilterAttribute filterAttribute : filterItems) {
 					for (IRidget ridget : getRidgets()) {
 						if (filterAttribute.matches(ridget.getID())) {
 							if (filterAttribute instanceof RidgetUIFilterAttributeVisible) {
 								ridget.setVisible(((RidgetUIFilterAttributeVisible) filterAttribute).isVisible());
 							} else if (filterAttribute instanceof RidgetUIFilterAttributeMarker) {
 								if (ridget instanceof IMarkableRidget) {
 									IMarkableRidget markableRidget = (IMarkableRidget) ridget;
 									RidgetUIFilterAttributeMarker attributeMarker = (RidgetUIFilterAttributeMarker) filterAttribute;
 									markableRidget.addMarker(attributeMarker.getMarker());
 								}
 							}
 						}
 					}
 				}
 
 			}
 
 		}
 
 		@Override
 		public void activated(INavigationNode source) {
 			super.activated(source);
 			applyFilters(source.getFilters());
 		}
 
 		@Override
 		public void filtersChanged(INavigationNode source) {
 			super.filtersChanged(source);
 			applyFilters(source.getFilters());
 		}
 
 	}
 
 }
