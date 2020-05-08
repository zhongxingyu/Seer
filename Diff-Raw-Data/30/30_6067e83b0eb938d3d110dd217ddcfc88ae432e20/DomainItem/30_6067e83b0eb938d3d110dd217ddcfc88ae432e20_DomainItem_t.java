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
 import pt.ist.vaadinframework.data.AbstractBufferedItem;
 import pt.ist.vaadinframework.data.BufferedProperty;
 import pt.ist.vaadinframework.data.HintedProperty;
 import pt.ist.vaadinframework.data.hints.Required;
 import pt.ist.vaadinframework.data.metamodel.MetaModel;
 import pt.ist.vaadinframework.data.metamodel.PropertyDescriptor;
 
 import com.vaadin.data.Property;
 import com.vaadin.data.util.AbstractProperty;
 
 public class DomainItem<Type> extends AbstractBufferedItem<Object, Type> {
     public class DescriptorProperty extends AbstractProperty {
 	private final PropertyDescriptor descriptor;
 
 	public DescriptorProperty(PropertyDescriptor descriptor) {
 	    this.descriptor = descriptor;
 	}
 
 	@Override
 	public Class<?> getType() {
	    final Object value = getValue();
	    if (value != null) {
		return value.getClass();
	    }
 	    return descriptor.getPropertyType();
 	}
 
 	@Override
 	public Object getValue() {
 	    Type host = getHost();
 	    return host != null ? descriptor.read(host) : null;
 	}
 
 	@Override
 	public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
 	    descriptor.write(getHost(), newValue);
 	}
 
 	protected Type getHost() {
 	    return DomainItem.this.getValue();
 	}
     }
 
     private final Map<Object, PropertyDescriptor> descriptorCache = new HashMap<Object, PropertyDescriptor>();
 
     public DomainItem(Property wrapped, Hint... hints) {
 	super(wrapped, hints);
     }
 
     public DomainItem(Class<? extends Type> type, Hint... hints) {
 	super(type, hints);
     }
 
     public DomainItem(Type value, Hint... hints) {
 	super(value, hints);
     }
 
     public DomainItem(Type value, Class<? extends Type> type, Hint... hints) {
 	super(value, type, hints);
     }
 
     public void discoverAllItems() {
 	if (AbstractDomainObject.class.isAssignableFrom(getType())) {
 	    MetaModel model = MetaModel.findMetaModelForType((Class<? extends AbstractDomainObject>) getType());
 	    for (String propertyId : model.getPropertyIds()) {
 		getItemProperty(propertyId);
 	    }
 	}
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
 	    if (property != null && property instanceof AbstractBufferedItem) {
 		addItemProperty(first, property);
 		property = ((AbstractBufferedItem<?, ?>) property).getItemProperty(rest);
 	    } else {
 		throw new RuntimeException("could not load property: " + propertyId + " for type: " + getType());
 	    }
 	}
 	return property;
     }
 
     private Property fromDescriptor(String propertyId) {
 	PropertyDescriptor descriptor = getDescriptor(propertyId);
 	if (descriptor != null) {
 	    Property property;
 	    if (AbstractDomainObject.class.isAssignableFrom(descriptor.getPropertyType())) {
 		property = new DomainItem(new DescriptorProperty(descriptor));
 	    } else if (descriptor.isCollection()) {
 		property = new DomainContainer(new DescriptorProperty(descriptor), descriptor.getCollectionElementType());
 	    } else {
 		property = new BufferedProperty(new DescriptorProperty(descriptor));
 	    }
 	    if (descriptor.isRequired()) {
 		if (property instanceof HintedProperty) {
 		    ((HintedProperty<?>) property).addHint(new Required());
 		}
 	    }
 	    return property;
 	}
 	throw new RuntimeException("could not load property: " + propertyId + " for type: " + getType());
     }
 
     private PropertyDescriptor getDescriptor(String propertyId) {
 	if (!descriptorCache.containsKey(propertyId)) {
	    Class<? extends AbstractDomainObject> type = (Class<? extends AbstractDomainObject>) getType();
	    if (AbstractDomainObject.class.isAssignableFrom(type)) {
		MetaModel model = MetaModel.findMetaModelForType(type);
 		descriptorCache.put(propertyId, model.getPropertyDescriptor(propertyId));
 	    }
 	}
 	return descriptorCache.get(propertyId);
     }

 }
