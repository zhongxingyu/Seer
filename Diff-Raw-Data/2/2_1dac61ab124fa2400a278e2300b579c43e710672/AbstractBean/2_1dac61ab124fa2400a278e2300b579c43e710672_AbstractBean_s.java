 /*
 * utils - AbstractBean.java * Copyright © 2008-2010 David Roden
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  */
 
 package net.pterodactylus.util.beans;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Abstract bean super class that contains property change listener management.
  *
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public abstract class AbstractBean {
 
 	/** Property change listeners. */
 	private final List<PropertyChangeListener> propertyChangeListeners = Collections.synchronizedList(new ArrayList<PropertyChangeListener>());
 
 	/**
 	 * Adds a property change listener.
 	 *
 	 * @param propertyChangeListener
 	 *            The property change listener to add
 	 */
 	public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
 		propertyChangeListeners.add(propertyChangeListener);
 	}
 
 	/**
 	 * Removes a property change listener.
 	 *
 	 * @param propertyChangeListener
 	 *            The property change listener to remove
 	 */
 	public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
 		propertyChangeListeners.remove(propertyChangeListener);
 	}
 
 	/**
 	 * Notifies all listeners that a property has changed.
 	 *
 	 * @param property
 	 *            The name of the property
 	 * @param oldValue
 	 *            The old value of the property
 	 * @param newValue
 	 *            The new value of the property
 	 */
 	protected void firePropertyChange(String property, Object oldValue, Object newValue) {
 		PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(this, property, oldValue, newValue);
 		for (PropertyChangeListener propertyChangeListener : propertyChangeListeners) {
 			propertyChangeListener.propertyChange(propertyChangeEvent);
 		}
 
 	}
 
 	/**
 	 * Fires a property change event if the two values are not equal.
 	 *
 	 * @param propertyName
 	 *            The name of the property
 	 * @param oldValue
 	 *            The old value of the property
 	 * @param newValue
 	 *            The new value of the property
 	 */
 	protected void fireIfPropertyChanged(String propertyName, Object oldValue, Object newValue) {
 		if (!equal(oldValue, newValue)) {
 			firePropertyChange(propertyName, oldValue, newValue);
 		}
 	}
 
 	//
 	// PRIVATE METHODS
 	//
 
 	/**
 	 * Compares the two objects and returns whether they are equal according to
 	 * {@link Object#equals(Object)}. This method takes <code>null</code>
 	 * into account as a valid value for an object.
 	 *
 	 * @param first
 	 *            The first object
 	 * @param second
 	 *            The second object
 	 * @return <code>true</code> if the two objects are equal,
 	 *         <code>false</code> otherwise
 	 */
 	private boolean equal(Object first, Object second) {
 		return ((first == null) && (second == null)) || ((first != null) && first.equals(second)) || second.equals(first);
 	}
 
 }
