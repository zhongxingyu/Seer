 /*
  * Copyright 2011 Instituto Superior Tecnico
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the vaadin-framework.
  *
  *   The vaadin-framework Infrastructure is free software: you can 
  *   redistribute it and/or modify it under the terms of the GNU Lesser General 
  *   Public License as published by the Free Software Foundation, either version 
  *   3 of the License, or (at your option) any later version.*
  *
  *   vaadin-framework is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with vaadin-framework. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package com.vaadin.data.util;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.EventObject;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import pt.ist.fenixframework.pstm.AbstractDomainObject;
 
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 
 /**
  * @author Pedro Santos (pedro.miguel.santos@ist.utl.pt)
  * 
  */
 public abstract class AbstractDomainItem extends AbstractDomainProperty implements Item, Item.PropertySetChangeNotifier {
     private final Map<Object, Property> properties = new HashMap<Object, Property>();
 
     /**
      * Create from domain object instance.
      * 
      * @param value The instance to wrap.
      */
     public AbstractDomainItem(AbstractDomainObject value) {
 	super(value);
     }
 
     /**
      * Create from domain object type.
      * 
      * @param type The type of domain object.
      */
     public AbstractDomainItem(Class<? extends AbstractDomainObject> type) {
 	super(type);
     }
 
     /**
      * Item that hosts the object instance,
      * 
      * @param host
      * @param type
      */
     public AbstractDomainItem(AbstractDomainItem host, Class<? extends AbstractDomainObject> type) {
 	super(host, type);
     }
 
     /**
      * @see com.vaadin.data.Item#getItemProperty(java.lang.Object)
      */
     @Override
     public Property getItemProperty(Object propertyId) {
 	if (!properties.containsKey(propertyId)) {
 	    Property property;
 	    if (propertyId instanceof String) {
 		String id = (String) propertyId;
		int split = id.indexOf('.');
		if (split == -1) {
		    property = lazyCreateProperty(id);
		} else {
		    String first = id.substring(0, split);
		    String rest = id.substring(split + 1);
		    property = getItemProperty(first);
		    properties.put(first, property);
		    property = ((AbstractDomainItem) property).getItemProperty(rest);
 		}
 	    } else {
 		property = lazyCreateProperty(propertyId);
 	    }
 	    if (property != null) {
 		properties.put(propertyId, property);
 	    }
 	}
 	return properties.get(propertyId);
     }
 
     /**
      * @see com.vaadin.data.util.AbstractDomainProperty#getValue()
      */
     @Override
     public AbstractDomainObject getValue() {
 	return (AbstractDomainObject) super.getValue();
     }
 
     public AbstractDomainObject getOrCreateValue() {
 	if (getValue() == null) {
 	    try {
 		setValue(getType().newInstance());
 		fireInstanceCreation();
 	    } catch (InstantiationException e) {
 		throw new RuntimeException(e);
 	    } catch (IllegalAccessException e) {
 		throw new RuntimeException(e);
 	    }
 	}
 	return getValue();
     }
 
     /**
      * @see com.vaadin.data.util.AbstractDomainProperty#getType()
      */
     @Override
     public Class<? extends AbstractDomainObject> getType() {
 	return (Class<? extends AbstractDomainObject>) super.getType();
     }
 
     /**
      * @param id
      * @return
      */
     protected abstract Property lazyCreateProperty(Object propertyId);
 
     /**
      * @see com.vaadin.data.Item#getItemPropertyIds()
      */
     @Override
     public Collection<?> getItemPropertyIds() {
 	return Collections.unmodifiableCollection(properties.keySet());
     }
 
     /**
      * @see com.vaadin.data.Item#addItemProperty(java.lang.Object,
      *      com.vaadin.data.Property)
      */
     @Override
     public boolean addItemProperty(Object id, Property property) throws UnsupportedOperationException {
 	if (id == null) {
 	    throw new NullPointerException("Item property id can not be null");
 	}
 	if (properties.containsKey(id)) {
 	    return false;
 	}
 	properties.put(id, property);
 	fireItemPropertySetChange();
 	return true;
     }
 
     /**
      * @see com.vaadin.data.Item#removeItemProperty(java.lang.Object)
      */
     @Override
     public boolean removeItemProperty(Object id) throws UnsupportedOperationException {
 	if (properties.remove(id) == null) {
 	    return false;
 	}
 	fireItemPropertySetChange();
 	return true;
     }
 
     private List<PropertySetChangeListener> propertySetChangeListeners;
 
     private List<InstanceCreationListener> instanceCreationListeners;
 
     private class PropertySetChangeEvent extends EventObject implements Item.PropertySetChangeEvent {
 	private PropertySetChangeEvent(AbstractDomainItem source) {
 	    super(source);
 	}
 
 	@Override
 	public AbstractDomainItem getItem() {
 	    return (AbstractDomainItem) getSource();
 	}
     }
 
     /**
      * @see com.vaadin.data.Item.PropertySetChangeNotifier#addListener(com.vaadin.data.Item.PropertySetChangeListener)
      */
     @Override
     public void addListener(PropertySetChangeListener listener) {
 	if (propertySetChangeListeners == null) {
 	    propertySetChangeListeners = new LinkedList<PropertySetChangeListener>();
 	}
 	propertySetChangeListeners.add(listener);
     }
 
     /**
      * @see com.vaadin.data.Item.PropertySetChangeNotifier#removeListener(com.vaadin.data.Item.PropertySetChangeListener)
      */
     @Override
     public void removeListener(PropertySetChangeListener listener) {
 	if (propertySetChangeListeners != null) {
 	    propertySetChangeListeners.remove(listener);
 	}
     }
 
     protected void fireItemPropertySetChange() {
 	if (propertySetChangeListeners != null) {
 	    final PropertySetChangeListener[] l = propertySetChangeListeners.toArray(new PropertySetChangeListener[0]);
 	    final Item.PropertySetChangeEvent event = new PropertySetChangeEvent(this);
 	    for (int i = 0; i < l.length; i++) {
 		l[i].itemPropertySetChange(event);
 	    }
 	}
     }
 
     private class DomainItemInstanceCreationEvent extends EventObject implements InstanceCreationEvent {
 	protected DomainItemInstanceCreationEvent(AbstractDomainItem source) {
 	    super(source);
 	}
 
 	@Override
 	public AbstractDomainItem getDomainItem() {
 	    return (AbstractDomainItem) getSource();
 	}
     }
 
     public void addListener(InstanceCreationListener listener) {
 	if (instanceCreationListeners == null) {
 	    instanceCreationListeners = new LinkedList<InstanceCreationListener>();
 	}
 	instanceCreationListeners.add(listener);
     }
 
     public void removeListener(InstanceCreationListener listener) {
 	if (instanceCreationListeners != null) {
 	    instanceCreationListeners.remove(listener);
 	}
     }
 
     /**
      * Sends a read only status change event to all registered listeners.
      */
     protected void fireInstanceCreation() {
 	if (instanceCreationListeners != null) {
 	    final Object[] l = instanceCreationListeners.toArray();
 	    final InstanceCreationEvent event = new DomainItemInstanceCreationEvent(this);
 	    for (int i = 0; i < l.length; i++) {
 		((InstanceCreationListener) l[i]).itemCreation(event);
 	    }
 	}
     }
 }
