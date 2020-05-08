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
 package org.eclipse.riena.ui.ridgets;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeSupport;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.eclipse.riena.core.marker.IMarker;
 import org.eclipse.riena.core.marker.IMarkerAttributeChangeListener;
 import org.eclipse.riena.core.marker.Markable;
 import org.eclipse.riena.ui.core.marker.DisabledMarker;
 import org.eclipse.riena.ui.core.marker.HiddenMarker;
 import org.eclipse.riena.ui.core.marker.IMarkerPropertyChangeEvent;
 import org.eclipse.riena.ui.core.marker.OutputMarker;
 
 /**
  * Helper class for Ridgets to delegate their marker issues to.
  */
 public abstract class AbstractMarkerSupport {
 
 	private Set<IMarker> markers;
 	private IBasicMarkableRidget ridget;
 	private PropertyChangeSupport propertyChangeSupport;
 	private IMarkerAttributeChangeListener markerAttributeChangeListener;
 
 	/**
 	 * @since 2.0
 	 */
 	public AbstractMarkerSupport() {
 		markerAttributeChangeListener = new MarkerAttributeChangeListener();
 	}
 
 	public AbstractMarkerSupport(final IBasicMarkableRidget ridget, final PropertyChangeSupport propertyChangeSupport) {
 		super();
 		init(ridget, propertyChangeSupport);
 	}
 
 	/**
 	 * Initializes this marker support with the Ridget and the property change
 	 * support.
 	 * 
 	 * @param ridget
 	 *            the Ridget that needs the marker support.
 	 * @param propertyChangeSupport
 	 * @since 2.0
 	 */
 	public void init(final IBasicMarkableRidget ridget, final PropertyChangeSupport propertyChangeSupport) {
 		this.ridget = ridget;
 		this.propertyChangeSupport = propertyChangeSupport;
 	}
 
 	/**
 	 * Updates the UI-control to display the current markers.
 	 * 
 	 * @see #getUIControl()
 	 * @see #getMarkers()
 	 */
 	public abstract void updateMarkers();
 
 	/**
 	 * @see org.eclipse.riena.ui.internal.ridgets.IBasicMarkableRidget#addMarker(org.eclipse.riena.core.marker.IMarker)
 	 */
 	public void addMarker(final IMarker marker) {
 		if (marker == null) {
 			return;
 		}
 
 		initializeMarkers();
 		final Collection<IMarker> oldValue = cloneMarkers();
 		if (markers.add(marker)) {
 			updateMarkers();
 			final Collection<IMarker> newValue = getMarkers();
 			fireMarkerPropertyChangeEvent(oldValue, newValue);
 			fireOutputPropertyChangeEvent(oldValue, newValue);
 			fireEnabledPropertyChangeEvent(oldValue, newValue);
 			marker.addAttributeChangeListener(markerAttributeChangeListener);
 		}
 	}
 
 	private void initializeMarkers() {
 		if (markers == null) {
 			markers = new HashSet<IMarker>(1, 1.0f);
 		}
 	}
 
 	private void clearMarkers() {
 		markers.clear();
 	}
 
 	private Collection<IMarker> cloneMarkers() {
 		if (markers != null) {
 			return new HashSet<IMarker>(markers);
 		} else {
 			return Collections.emptySet();
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.internal.ridgets.IBasicMarkableRidget#getMarkers()
 	 */
 	public Collection<IMarker> getMarkers() {
		if (markers != null) {
			return Collections.unmodifiableSet(markers);
		} else {
			return Collections.emptySet();
		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.internal.ridgets.IBasicMarkableRidget#getMarkersOfType(java.lang.Class)
 	 */
 	public <T extends IMarker> Collection<T> getMarkersOfType(final Class<T> type) {
 		return Markable.getMarkersOfType(getMarkers(), type);
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.internal.ridgets.IBasicMarkableRidget#removeAllMarkers()
 	 */
 	public void removeAllMarkers() {
 		if ((markers != null) && !markers.isEmpty()) {
 			for (final IMarker marker : markers) {
 				marker.removeAttributeChangeListener(markerAttributeChangeListener);
 			}
 			final Collection<IMarker> oldValue = cloneMarkers();
 			clearMarkers();
 			updateMarkers();
 			final Collection<IMarker> newValue = getMarkers();
 			fireMarkerPropertyChangeEvent(oldValue, newValue);
 			fireOutputPropertyChangeEvent(oldValue, newValue);
 			fireEnabledPropertyChangeEvent(oldValue, newValue);
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.internal.ridgets.IBasicMarkableRidget#removeMarker(org.eclipse.riena.core.marker.IMarker)
 	 * @since 2.1
 	 */
 	public boolean removeMarker(final IMarker marker) {
 		if ((markers != null) && !markers.isEmpty()) {
 			final Collection<IMarker> oldValue = cloneMarkers();
 			if (markers.remove(marker)) {
 				updateMarkers();
 				final Collection<IMarker> newValue = getMarkers();
 				fireMarkerPropertyChangeEvent(oldValue, newValue);
 				fireOutputPropertyChangeEvent(oldValue, newValue);
 				fireEnabledPropertyChangeEvent(oldValue, newValue);
 				marker.removeAttributeChangeListener(markerAttributeChangeListener);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	protected Object getUIControl() {
 		return getRidget().getUIControl();
 	}
 
 	protected IBasicMarkableRidget getRidget() {
 		return ridget;
 	}
 
 	protected void handleMarkerAttributesChanged() {
 		propertyChangeSupport.firePropertyChange(new MarkerPropertyChangeEvent(true, getRidget(), getMarkers()));
 	}
 
 	// helping methods
 	//////////////////
 
 	private Boolean isEnabled(final Collection<IMarker> markers) {
 		boolean result = true;
 		if (markers != null) {
 			final Iterator<IMarker> iter = markers.iterator();
 			while (result && iter.hasNext()) {
 				result = !(iter.next() instanceof DisabledMarker);
 			}
 		}
 		return Boolean.valueOf(result);
 	}
 
 	private Boolean isOutput(final Collection<IMarker> markers) {
 		boolean result = false;
 		if (markers != null) {
 			final Iterator<IMarker> iter = markers.iterator();
 			while (!result && iter.hasNext()) {
 				result = (iter.next() instanceof OutputMarker);
 			}
 		}
 		return Boolean.valueOf(result);
 	}
 
 	private void fireEnabledPropertyChangeEvent(final Collection<IMarker> oldMarkers,
 			final Collection<IMarker> newMarkers) {
		final Boolean oldValue = isOutput(oldMarkers);
		final Boolean newValue = isOutput(newMarkers);
 		if (!oldValue.equals(newValue)) {
			final PropertyChangeEvent evt = new PropertyChangeEvent(getRidget(), IRidget.PROPERTY_ENABLED,
					isEnabled(oldMarkers), isEnabled(newMarkers));
 			propertyChangeSupport.firePropertyChange(evt);
 		}
 	}
 
 	private void fireMarkerPropertyChangeEvent(final Collection<IMarker> oldMarkers,
 			final Collection<IMarker> newMarkers) {
 		propertyChangeSupport.firePropertyChange(new MarkerPropertyChangeEvent(oldMarkers, getRidget(), newMarkers));
 	}
 
 	private void fireOutputPropertyChangeEvent(final Collection<IMarker> oldMarkers,
 			final Collection<IMarker> newMarkers) {
 		final Boolean oldValue = isOutput(oldMarkers);
 		final Boolean newValue = isOutput(newMarkers);
 		if (!oldValue.equals(newValue)) {
 			final PropertyChangeEvent evt = new PropertyChangeEvent(getRidget(), IMarkableRidget.PROPERTY_OUTPUT_ONLY,
 					oldValue, newValue);
 			propertyChangeSupport.firePropertyChange(evt);
 		}
 	}
 
 	public void fireShowingPropertyChangeEvent() {
 		propertyChangeSupport.firePropertyChange(IRidget.PROPERTY_SHOWING, !ridget.isVisible(), ridget.isVisible());
 		updateMarkers();
 	}
 
 	protected boolean hasHiddenMarkers() {
 		return !getRidget().getMarkersOfType(HiddenMarker.class).isEmpty();
 	}
 
 	// helping classes
 	//////////////////
 
 	private static final class MarkerPropertyChangeEvent extends PropertyChangeEvent implements
 			IMarkerPropertyChangeEvent {
 
 		private static final long serialVersionUID = 1L;
 
 		private boolean attributeRelated = false;
 
 		private MarkerPropertyChangeEvent(final Object oldValue, final IBasicMarkableRidget source,
 				final Object newValue) {
 			super(source, IBasicMarkableRidget.PROPERTY_MARKER, oldValue, newValue);
 		}
 
 		private MarkerPropertyChangeEvent(final boolean attributeRelated, final IBasicMarkableRidget source,
 				final Object newValue) {
 			this(null, source, newValue);
 			this.attributeRelated = attributeRelated;
 		}
 
 		public boolean isAttributeRelated() {
 			return attributeRelated;
 		}
 	}
 
 	private final class MarkerAttributeChangeListener implements IMarkerAttributeChangeListener {
 
 		public void attributesChanged() {
 			handleMarkerAttributesChanged();
 		}
 	}
 
 }
