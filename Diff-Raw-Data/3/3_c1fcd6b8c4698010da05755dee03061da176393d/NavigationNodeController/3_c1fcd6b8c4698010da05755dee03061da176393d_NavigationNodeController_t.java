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
 package org.eclipse.riena.navigation.ui.controllers;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
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
 import org.eclipse.riena.ui.ridgets.IBasicMarkableRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.IRidgetContainer;
 import org.eclipse.riena.ui.ridgets.IWindowRidget;
 import org.eclipse.riena.ui.ridgets.controller.IController;
 
 /**
  * An abstract controller superclass that manages the navigation node of a
  * controller.
  * 
  * @param <N>
  *            Type of the navigation node
  */
 public abstract class NavigationNodeController<N extends INavigationNode<?>> extends TypecastingObject implements
 		INavigationNodeController, IController, IContext {
 
 	private N navigationNode;
 	private Map<String, IRidget> ridgets;
 	private NavigationUIFilterApplier<N> nodeListener;
 	private PropertyChangeListener propertyChangeListener;
 
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
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * Implementation note: for ridgets of the type IRidgetContainer, this
 	 * method supports two-part ids (i.e. nested ids). For example, if the
 	 * ridget "searchComposite" containts the ridget "txtName", you can request:
 	 * "searchComposite.txtName":
 	 * 
 	 * <pre>
 	 * ITextRidget txtName = getRidget(&quot;searchComposite.txtName&quot;);
 	 * </pre>
 	 * 
 	 * @since 3.0
 	 */
 	@SuppressWarnings("unchecked")
 	public <R extends IRidget> R getRidget(final String id) {
 		IRidget result = ridgets.get(id);
 		if (result == null && id.indexOf('.') != -1) {
 			final String parentId = id.substring(0, id.lastIndexOf('.'));
 			final String childId = id.substring(id.lastIndexOf('.') + 1);
 			final IRidget parent = ridgets.get(parentId);
 			if (parent instanceof IRidgetContainer) {
 				result = ((IRidgetContainer) parent).getRidget(childId);
 			}
 		}
 		return (R) result;
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	@SuppressWarnings("unchecked")
 	public <R extends IRidget> R getRidget(final Class<R> ridgetClazz, final String id) {
 		R ridget = getRidget(id);
 
 		if (ridget != null) {
 			return ridget;
 		}
 		if (RienaStatus.isTest()) {
 			try {
 				if (ridgetClazz.isInterface() || Modifier.isAbstract(ridgetClazz.getModifiers())) {
 					final Class<R> mappedRidgetClazz = (Class<R>) ClassRidgetMapper.getInstance().getRidgetClass(
 							ridgetClazz);
 					if (mappedRidgetClazz != null) {
 						ridget = mappedRidgetClazz.newInstance();
 					}
 					Assert.isNotNull(
 							ridget,
 							"Could not find a corresponding implementation for " + ridgetClazz.getName() + " in " + ClassRidgetMapper.class.getName()); //$NON-NLS-1$ //$NON-NLS-2$
 				} else {
 					ridget = ridgetClazz.newInstance();
 				}
 			} catch (final InstantiationException e) {
 				throw new RuntimeException(e);
 			} catch (final IllegalAccessException e) {
 				throw new RuntimeException(e);
 			}
 
 			addRidget(id, ridget);
 		}
 
 		return ridget;
 	}
 
 	public Collection<? extends IRidget> getRidgets() {
 		return ridgets.values();
 	}
 
 	private void addRidgetMarkers(final IRidget ridget, final List<IMarker> combinedMarkers) {
 		if (ridget instanceof IBasicMarkableRidget && ((IBasicMarkableRidget) ridget).isVisible()
 				&& ((IBasicMarkableRidget) ridget).isEnabled()) {
 			addRidgetMarkers((IBasicMarkableRidget) ridget, combinedMarkers);
 		} else if (ridget instanceof IRidgetContainer) {
 			addRidgetMarkers((IRidgetContainer) ridget, combinedMarkers);
 		}
 	}
 
 	private void addRidgetMarkers(final IBasicMarkableRidget ridget, final List<IMarker> combinedMarkers) {
 		combinedMarkers.addAll(getNotHiddenMarkers(ridget));
 	}
 
 	private List<? extends IMarker> getNotHiddenMarkers(final IBasicMarkableRidget ridget) {
 		final Collection<? extends IMarker> markers = ridget.getMarkers();
 		final List<? extends IMarker> notHiddenMarkers = new ArrayList<IMarker>(markers);
 		final Set<Class<IMarker>> hiddenTypes = ridget.getHiddenMarkerTypes();
 		for (final Class<IMarker> hiddenType : hiddenTypes) {
 			for (final IMarker marker : markers) {
 				if (hiddenType.isAssignableFrom(marker.getClass())) {
 					notHiddenMarkers.remove(marker);
 				}
 			}
 		}
 		return notHiddenMarkers;
 	}
 
 	private void addRidgetMarkers(final IRidgetContainer ridgetContainer, final List<IMarker> combinedMarkers) {
 		for (final IRidget ridget : ridgetContainer.getRidgets()) {
 			addRidgetMarkers(ridget, combinedMarkers);
 		}
 	}
 
 	protected void updateNavigationNodeMarkers() {
 		final Collection<ErrorMarker> errorMarkers = getNavigationNode().getMarkersOfType(ErrorMarker.class);
 		final Collection<MandatoryMarker> mandatoryMarkers = getNavigationNode()
 				.getMarkersOfType(MandatoryMarker.class);
 		final Collection<ErrorMarker> currentErrors = new ArrayList<ErrorMarker>();
 		final Collection<MandatoryMarker> currentMandatory = new ArrayList<MandatoryMarker>();
 
 		// add error and/or mandatory marker, if a Ridget has an error marker and/or a (enabled) mandatory marker
 		for (final IMarker marker : getRidgetMarkers()) {
 			if (marker instanceof ErrorMarker) {
 				currentErrors.add(ErrorMarker.class.cast(marker));
 			} else if (marker instanceof MandatoryMarker) {
 				final MandatoryMarker mandatoryMarker = (MandatoryMarker) marker;
 				if (!mandatoryMarker.isDisabled()) {
 					currentMandatory.add(MandatoryMarker.class.cast(marker));
 				}
 			}
 		}
 
 		for (final IMarker marker : errorMarkers) {
 			if (!currentErrors.contains(marker)) {
 				getNavigationNode().removeMarker(marker);
 			}
 		}
 		for (final IMarker marker : mandatoryMarkers) {
 			if (!currentMandatory.contains(marker)) {
 				getNavigationNode().removeMarker(marker);
 			}
 		}
 
 		for (final IMarker marker : currentErrors) {
 			getNavigationNode().addMarker(marker);
 		}
 
 		for (final IMarker marker : currentMandatory) {
 			getNavigationNode().addMarker(marker);
 		}
 	}
 
 	private List<IMarker> getRidgetMarkers() {
 		final List<IMarker> combinedMarkers = new ArrayList<IMarker>();
 		addRidgetMarkers(this, combinedMarkers);
 		return combinedMarkers;
 	}
 
 	protected void updateIcon(final IWindowRidget windowRidget) {
 		if (windowRidget == null) {
 			return;
 		}
 		final String nodeIcon = getNavigationNode().getIcon();
 		windowRidget.setIcon(nodeIcon);
 	}
 
 	// public IProgressVisualizer getProgressVisualizer(Object context) {
 	// return new ProgressVisualizer();
 	// }
 
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
 
 }
