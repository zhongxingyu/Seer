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
 package pt.ist.vaadinframework.data.reflect;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import pt.ist.fenixframework.pstm.AbstractDomainObject;
 import pt.ist.vaadinframework.data.BufferedItem;
 import pt.ist.vaadinframework.data.HintedProperty;
 import pt.ist.vaadinframework.data.VBoxProperty;
 import pt.ist.vaadinframework.data.hints.Required;
 import pt.ist.vaadinframework.data.metamodel.MetaModel;
 import pt.ist.vaadinframework.data.metamodel.PropertyDescriptor;
 
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 
 public class DomainItem<Type extends AbstractDomainObject> extends BufferedItem<Object, Type> {
     private final Map<String, PropertyDescriptor> descriptorCache = new HashMap<String, PropertyDescriptor>();
 
     public DomainItem(HintedProperty value) {
 	super(value);
     }
 
     public DomainItem(Type instance) {
 	this(new VBoxProperty(instance));
     }
 
     public DomainItem(Class<? extends Type> type) {
 	this(new VBoxProperty(type));
     }
 
     @Override
     protected Property makeProperty(Object propertyId) {
 	int split = ((String) propertyId).indexOf('.');
 	Property property;
 	if (split == -1) {
 	    property = fromDescriptor((String) propertyId);
 	    if (property != null) {
 		addItemProperty(propertyId, property);
 	    }
 	} else {
 	    String first = ((String) propertyId).substring(0, split);
 	    String rest = ((String) propertyId).substring(split + 1);
 	    property = getItemProperty(first);
 	    if (property != null && property instanceof Item) {
 		addItemProperty(first, property);
 		property = ((Item) property).getItemProperty(rest);
 	    } else {
 		throw new RuntimeException("could not load property: " + propertyId + " for type: " + getType());
 	    }
 	}
 	return property;
     }
 
     private Property fromDescriptor(String propertyId) {
 	PropertyDescriptor descriptor = getDescriptor(propertyId);
 	if (descriptor != null) {
 	    BufferedProperty property;
 	    if (descriptor.isRequired()) {
 		property = new BufferedProperty(propertyId, descriptor.getPropertyType(), new Required());
 	    } else {
 		property = new BufferedProperty(propertyId, descriptor.getPropertyType());
 	    }
 	    if (AbstractDomainObject.class.isAssignableFrom(descriptor.getPropertyType())) {
 		return new DomainItem(property);
 	    } else if (descriptor.isCollection()) {
 		return new DomainContainer(property, descriptor.getCollectionElementType());
 	    }
 	    return property;
 	}
 	throw new RuntimeException("could not load property: " + propertyId + " for type: " + getType());
     }
 
     @Override
     protected Object readPropertyValue(AbstractDomainObject host, Object propertyId) {
 	PropertyDescriptor descriptor = getDescriptor((String) propertyId);
 	if (descriptor != null) {
 	    return descriptor.read(host);
 	}
 	return null;
     }
 
     @Override
     protected void writePropertyValue(AbstractDomainObject host, Object propertyId, Object newValue) {
 	PropertyDescriptor descriptor = getDescriptor((String) propertyId);
 	if (descriptor != null) {
 	    descriptor.write(host, newValue);
 	}
     }
 
     private PropertyDescriptor getDescriptor(String propertyId) {
 	if (!descriptorCache.containsKey(propertyId)) {
 	    MetaModel model = MetaModel.findMetaModelForType(getType());
 	    descriptorCache.put(propertyId, model.getPropertyDescriptor(propertyId));
 	}
 	return descriptorCache.get(propertyId);
     }
 }
