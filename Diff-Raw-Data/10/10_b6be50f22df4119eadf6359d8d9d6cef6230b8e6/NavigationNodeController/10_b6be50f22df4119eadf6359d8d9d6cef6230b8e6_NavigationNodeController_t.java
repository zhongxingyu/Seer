 /*******************************************************************************
  * Copyright (c) 2007, 2013 compeople AG and others.
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
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.Assert;
 
 import org.eclipse.riena.core.RienaStatus;
 import org.eclipse.riena.core.marker.IMarker;
 import org.eclipse.riena.navigation.INavigationContext;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.INavigationNodeController;
 import org.eclipse.riena.navigation.NavigationArgument;
 import org.eclipse.riena.navigation.annotation.processor.NavigationNodeControllerAnnotationProcessor;
 import org.eclipse.riena.navigation.common.TypecastingObject;
 import org.eclipse.riena.navigation.listener.INavigationNodeListenerable;
 import org.eclipse.riena.ui.core.context.IContext;
 import org.eclipse.riena.ui.core.marker.ErrorMarker;
 import org.eclipse.riena.ui.core.marker.MandatoryMarker;
 import org.eclipse.riena.ui.ridgets.AbstractRidget;
 import org.eclipse.riena.ui.ridgets.ClassRidgetMapper;
 import org.eclipse.riena.ui.ridgets.ComplexRidgetResolver;
 import org.eclipse.riena.ui.ridgets.IBasicMarkableRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.IRidgetResolver;
 import org.eclipse.riena.ui.ridgets.IStatuslineRidget;
 import org.eclipse.riena.ui.ridgets.IWindowRidget;
 import org.eclipse.riena.ui.ridgets.RidgetToStatuslineSubscriber;
 import org.eclipse.riena.ui.ridgets.SubModuleUtils;
 import org.eclipse.riena.ui.ridgets.controller.IController;
 import org.eclipse.riena.ui.ridgets.marker.MarkerUtil;
 
 /**
  * An abstract controller superclass that manages the navigation node of a controller.
  * 
  * @param <N>
  *            Type of the navigation node
  */
 public abstract class NavigationNodeController<N extends INavigationNode<?>> extends TypecastingObject implements INavigationNodeController, IController,
 		IContext {
 	/**
 	 * @since 5.0
 	 */
 	protected IRidgetResolver ridgetResolver = new ComplexRidgetResolver();
 
 	private N navigationNode;
 	private Map<String, IRidget> ridgets;
 	private NavigationUIFilterApplier<N> nodeListener;
 	private PropertyChangeListener propertyChangeListener;
 	private boolean configured = false;
 	private final RidgetToStatuslineSubscriber ridgetToStatuslineSubscriber = new RidgetToStatuslineSubscriber();
 
 	/**
 	 * Create a new Navigation Node view Controller. Set the navigation node later.
 	 */
 	public NavigationNodeController() {
 		this(null);
 	}
 
 	/**
 	 * Create a new Navigation Node view Controller on the specified navigationNode. Register this controller as the presentation of the Navigation node.
 	 * 
 	 * @param navigationNode
 	 *            the node to work on
 	 */
 	public NavigationNodeController(final N navigationNode) {
 
 		ridgets = new HashMap<String, IRidget>();
 		propertyChangeListener = new PropertyChangeHandler();
 		nodeListener = new NavigationUIFilterApplier<N>();
 
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
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void setNavigationNode(final N navigationNode) {
 		if (getNavigationNode() instanceof INavigationNodeListenerable) {
 			((INavigationNodeListenerable) getNavigationNode()).removeListener(nodeListener);
 		}
 		this.navigationNode = navigationNode;
 		navigationNode.setNavigationNodeController(this);
 		updateNavigationNodeMarkers();
 		if (getNavigationNode() instanceof INavigationNodeListenerable) {
 			((INavigationNodeListenerable) getNavigationNode()).addListener(nodeListener);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * Override in concrete subclass.
 	 */
 	public boolean allowsActivate(final INavigationNode<?> pNode, final INavigationContext context) {
 		return true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * Override in concrete subclass.
 	 */
 	public boolean allowsDeactivate(final INavigationNode<?> pNode, final INavigationContext context) {
 		return true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * All methods that overrides this method must call <code>super.afterBind()</code>.
 	 */
 	public void afterBind() {
 		NavigationNodeControllerAnnotationProcessor.getInstance().processAnnotations(this);
 		updateNavigationNodeMarkers();
 	}
 
 	/**
 	 * @return <code>true</code> if the controller is activated
 	 */
 	public boolean isActivated() {
 		return getNavigationNode() != null && getNavigationNode().isActivated();
 	}
 
 	/**
 	 * @return <code>true</code> if the node is enabled
 	 */
 	public boolean isEnabled() {
 		return getNavigationNode() != null && getNavigationNode().isEnabled();
 	}
 
 	/**
 	 * @return <code>true</code> if the node is visible
 	 */
 	public boolean isVisible() {
 		return getNavigationNode() != null && getNavigationNode().isVisible();
 	}
 
 	/**
 	 * @return true if the controller is activated
 	 */
 	public boolean isDeactivated() {
 		return getNavigationNode() == null || getNavigationNode().isDeactivated();
 	}
 
 	/**
 	 * @return <code>true</code> if the controller is created
 	 */
 	public boolean isCreated() {
 		return getNavigationNode() == null || getNavigationNode().isCreated();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * Override in concrete subclass.
 	 */
 	public boolean allowsDispose(final INavigationNode<?> node, final INavigationContext context) {
 		return true;
 	}
 
 	public void addRidget(final String id, final IRidget ridget) {
 		ridget.addPropertyChangeListener(IBasicMarkableRidget.PROPERTY_MARKER, propertyChangeListener);
 		ridget.addPropertyChangeListener(IBasicMarkableRidget.PROPERTY_MARKER_HIDING, propertyChangeListener);
 		ridget.addPropertyChangeListener(IRidget.PROPERTY_SHOWING, propertyChangeListener);
 		ridget.addPropertyChangeListener(AbstractRidget.COMMAND_UPDATE, propertyChangeListener);
 		ridgets.put(id, ridget);
 		ridgetToStatuslineSubscriber.addRidget(ridget);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @since 5.0
 	 */
 	public boolean removeRidget(final String id) {
 		ridgetToStatuslineSubscriber.removeRidget(getRidget(id));
 		return ridgets.remove(id) != null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @since 5.0
 	 */
 	public void setStatuslineToShowMarkerMessages(final IStatuslineRidget statuslineToShowMarkerMessages) {
 		ridgetToStatuslineSubscriber.setStatuslineToShowMarkerMessages(statuslineToShowMarkerMessages, getRidgets());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * Implementation note: for ridgets of the type IRidgetContainer, this method supports two-part ids (i.e. nested ids). For example, if the ridget
 	 * "searchComposite" containts the ridget "txtName", you can request: "searchComposite.txtName":
 	 * 
 	 * <pre>
 	 * ITextRidget txtName = getRidget(&quot;searchComposite.txtName&quot;);
 	 * </pre>
 	 * 
 	 * @since 3.0
 	 */
 	public <R extends IRidget> R getRidget(final String id) {
 		return (R) ridgetResolver.getRidget(id, ridgets);
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public <R extends IRidget> R getRidget(final Class<R> ridgetClazz, final String id) {
 
 		R ridget = getRidget(id);
 		if (ridget != null) {
 			return ridget;
 		}
 
 		if (!SubModuleUtils.isPrepareView() || RienaStatus.isTest()) {
 			try {
 				if (ridgetClazz.isInterface() || Modifier.isAbstract(ridgetClazz.getModifiers())) {
 					final Class<R> mappedRidgetClazz = (Class<R>) ClassRidgetMapper.getInstance().getRidgetClass(ridgetClazz);
 					if (mappedRidgetClazz != null) {
 						ridget = mappedRidgetClazz.newInstance();
 					}
 					Assert.isNotNull(ridget,
 							"Could not find a corresponding implementation for " + ridgetClazz.getName() + " in " + ClassRidgetMapper.class.getName()); //$NON-NLS-1$ //$NON-NLS-2$
 				} else {
 					ridget = ridgetClazz.newInstance();
 				}
 			} catch (final InstantiationException e) {
 				throw new RuntimeException(e);
 			} catch (final IllegalAccessException e) {
 				throw new RuntimeException(e);
 			}
 
 			ridgetResolver.addRidget(id, ridget, this, ridgets);
 		}
 
 		return ridget;
 	}
 
 	public Collection<? extends IRidget> getRidgets() {
 		return ridgets.values();
 	}
 
 	/**
 	 * The idea in this method is to have at most one marker of each type (to avoid firing too many events when redundantly adding/removing markers of the same
 	 * type)
 	 */
 	protected void updateNavigationNodeMarkers() {
 		final Collection<ErrorMarker> errorInRidgets = new ArrayList<ErrorMarker>();
 		final Collection<MandatoryMarker> mandatoryInRidgets = new ArrayList<MandatoryMarker>();
 
 		// add error and/or mandatory marker, if a Ridget has an error marker and/or a (enabled) mandatory marker
		for (final IMarker marker : getRidgetMarkers()) {
 			if (marker instanceof ErrorMarker) {
 				errorInRidgets.add((ErrorMarker) marker);
 			} else if (marker instanceof MandatoryMarker) {
 				final MandatoryMarker mandatoryMarker = (MandatoryMarker) marker;
 				if (!mandatoryMarker.isDisabled()) {
 					mandatoryInRidgets.add(mandatoryMarker);
 				}
 			}
 		}
 
 		// handle error markers
 		final Collection<ErrorMarker> errorInNode = getNavigationNode().getMarkersOfType(ErrorMarker.class);
 		// case 1:
 		// marker in node    - no
 		// marker in ridgets - yes
 		// => we have to add a marker to the node
 		if (errorInNode.isEmpty() && !errorInRidgets.isEmpty()) {
 			// error markers are unique, so just add the first one
 			getNavigationNode().addMarker(errorInRidgets.iterator().next());
 		}
 		// case 2:
 		// marker in node    - yes
 		// marker in ridgets - no
 		// => we have to remove the marker from the node
 		if (!errorInNode.isEmpty() && errorInRidgets.isEmpty()) {
 			getNavigationNode().removeMarker(errorInNode.iterator().next());
 		}
 
 		// now we repeat the same for the mandatory markers
 		final Collection<MandatoryMarker> mandatoryInNode = getNavigationNode().getMarkersOfType(MandatoryMarker.class);
 		// case 1:
 		// marker in node    - no
 		// marker in ridgets - yes
 		// => we have to add a marker to the node
 		if (mandatoryInNode.isEmpty() && !mandatoryInRidgets.isEmpty()) {
 			getNavigationNode().addMarker(mandatoryInRidgets.iterator().next());
 		}
 		// case 2:
 		// marker in node    - yes
 		// marker in ridgets - no
 		// => we have to remove the marker from the node
 		if (!mandatoryInNode.isEmpty() && mandatoryInRidgets.isEmpty()) {
 			getNavigationNode().removeMarker(mandatoryInNode.iterator().next());
 		}
 		// case 3:
 		// enabled marker in node      - no
 		// disabled marker in node     - yes
 		// (enabled) marker in ridgets - yes
 		// => we have to remove the disabled marker from the node and add the enabled marker from the ridgets
 		final List<MandatoryMarker> enabledMandatoryInNode = new ArrayList<MandatoryMarker>();
 		final List<MandatoryMarker> disabledMandatoryInNode = new ArrayList<MandatoryMarker>();
 		for (final MandatoryMarker m : new ArrayList<MandatoryMarker>(mandatoryInNode)) {
 			if (m.isDisabled()) {
 				disabledMandatoryInNode.add(m);
 			} else {
 				enabledMandatoryInNode.add(m);
 			}
 		}
 		if (enabledMandatoryInNode.isEmpty() && !disabledMandatoryInNode.isEmpty() && !mandatoryInRidgets.isEmpty()) {
 			getNavigationNode().removeMarker(disabledMandatoryInNode.iterator().next());
 			getNavigationNode().addMarker(mandatoryInRidgets.iterator().next());
 		}
 	}
 
	/**
	 * this method is invoked by test code, so do not inline it
	 */
	private List<IMarker> getRidgetMarkers() {
		return MarkerUtil.getRidgetMarkers(this);
	}

 	protected void updateIcon(final IWindowRidget windowRidget) {
 		if (windowRidget == null) {
 			return;
 		}
 		final String nodeIcon = getNavigationNode().getIcon();
 		windowRidget.setIcon(nodeIcon);
 	}
 
 	public void setBlocked(final boolean blocked) {
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
 
 	private class PropertyChangeHandler implements PropertyChangeListener {
 		public void propertyChange(final PropertyChangeEvent evt) {
 			updateNavigationNodeMarkers();
 		}
 	}
 
 	/**
 	 * @since 1.2
 	 */
 	public void setContext(final String key, final Object value) {
 		Assert.isNotNull(getNavigationNode(), "NavigationNode may not be null"); //$NON-NLS-1$
 		getNavigationNode().setContext(key, value);
 	}
 
 	/**
 	 * @since 1.2
 	 */
 	public Object getContext(final String key) {
 		Assert.isNotNull(getNavigationNode(), "NavigationNode may not be null"); //$NON-NLS-1$
 		return getNavigationNode().getContext(key);
 	}
 
 	public void navigationArgumentChanged(final NavigationArgument argument) {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @since 4.0
 	 */
 	public void setConfigured(final boolean configured) {
 		this.configured = configured;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @since 4.0
 	 */
 	public boolean isConfigured() {
 		return configured;
 	}
 
 }
