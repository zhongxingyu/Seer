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
 package org.eclipse.riena.ui.ridgets;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.riena.core.marker.IMarker;
 import org.eclipse.riena.core.marker.IMarkerAttributeChangeListener;
 import org.eclipse.riena.ui.core.marker.IMarkerPropertyChangeEvent;
 
 /**
  * Helper class for Ridgets to delegate their marker issues to.
  */
 public abstract class AbstractMarkerSupport {
 
 	private Set<IMarker> markers;
 	protected IMarkableRidget ridget;
 	private PropertyChangeSupport propertyChangeSupport;
 	private IMarkerAttributeChangeListener markerAttributeChangeListener;
 
 	public AbstractMarkerSupport(IMarkableRidget ridget, PropertyChangeSupport propertyChangeSupport) {
 		this.ridget = ridget;
 		this.propertyChangeSupport = propertyChangeSupport;
 		markerAttributeChangeListener = new MarkerAttributeChangeListener();
 	}
 
 	/**
 	 * Updates the UI-control to display the current markers.
 	 * 
 	 * @see #getUIControl()
 	 * @see #getMarkers()
 	 */
 	public abstract void updateMarkers();
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.IMarkableRidget#addMarker(org.eclipse.riena.core.marker.IMarker)
 	 */
 	public void addMarker(IMarker marker) {
 		if (marker == null) {
 			return;
 		}
 		Collection<IMarker> oldValue = getMarkers();
 		if (markers == null) {
 			markers = new HashSet<IMarker>(1, 1.0f);
 		}
 		if (markers.add(marker)) {
 			updateMarkers();
 			fireMarkerPropertyChangeEvent(oldValue);
 			marker.addAttributeChangeListener(markerAttributeChangeListener);
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.IMarkableRidget#getMarkers()
 	 */
 	public Collection<IMarker> getMarkers() {
 		if (markers != null) {
 			return new HashSet<IMarker>(markers);
 		} else {
 			return Collections.emptySet();
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.IMarkableRidget#getMarkersOfType(java.lang.Class)
 	 */
 	public <T extends IMarker> Collection<T> getMarkersOfType(Class<T> type) {
 		if (type == null) {
 			return Collections.emptyList();
 		}
 		List<T> typedMarkerList = new ArrayList<T>();
 
 		for (IMarker marker : getMarkers()) {
 			if (type.isAssignableFrom(marker.getClass())) {
 				typedMarkerList.add((T) marker);
 			}
 		}
 		return typedMarkerList;
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.IMarkableRidget#removeAllMarkers()
 	 */
 	public void removeAllMarkers() {
 		if (markers != null) {
 			Collection<IMarker> oldValue = getMarkers();
 			for (IMarker marker : markers) {
 				marker.removeAttributeChangeListener(markerAttributeChangeListener);
 			}
 			markers.clear();
 			if (oldValue.size() > 0) {
 				updateMarkers();
 				fireMarkerPropertyChangeEvent(oldValue);
 			}
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.IMarkableRidget#removeMarker(org.eclipse.riena.core.marker.IMarker)
 	 */
 	public void removeMarker(IMarker marker) {
 		if (markers != null) {
 			Collection<IMarker> oldValue = getMarkers();
 			if (markers.remove(marker)) {
 				updateMarkers();
 				fireMarkerPropertyChangeEvent(oldValue);
 				marker.removeAttributeChangeListener(markerAttributeChangeListener);
 			}
 		}
 	}
 
 	protected Object getUIControl() {
 		return ridget.getUIControl();
 	}
 
 	private void fireMarkerPropertyChangeEvent(Collection<IMarker> oldValue) {
 		propertyChangeSupport.firePropertyChange(new MarkerPropertyChangeEvent(oldValue, ridget, getMarkers()));
 	}
 
 	protected void handleMarkerAttributesChanged() {
 		propertyChangeSupport.firePropertyChange(new MarkerPropertyChangeEvent(true, ridget, getMarkers()));
 	}
 
 	private static final class MarkerPropertyChangeEvent extends PropertyChangeEvent implements
 			IMarkerPropertyChangeEvent {
 
 		private static final long serialVersionUID = 1L;
 
 		private boolean attributeRelated = false;
 
 		private MarkerPropertyChangeEvent(Object oldValue, IMarkableRidget source, Object newValue) {
 			super(source, IMarkableRidget.PROPERTY_MARKER, oldValue, newValue);
 		}
 
 		private MarkerPropertyChangeEvent(boolean attributeRelated, IMarkableRidget source, Object newValue) {
 			this(null, source, newValue);
 			this.attributeRelated = attributeRelated;
 		}
 
 		public boolean isAttributeRelated() {
 			return attributeRelated;
 		}
 	}
 
 	private class MarkerAttributeChangeListener implements IMarkerAttributeChangeListener {
 
 		/**
 		 * @see de.compeople.spirit.core.client.uibinding.IMarkerAttributeChangeListener#attributesChanged()
 		 */
 		public void attributesChanged() {
 			handleMarkerAttributesChanged();
 		}
 	}
 
 }
