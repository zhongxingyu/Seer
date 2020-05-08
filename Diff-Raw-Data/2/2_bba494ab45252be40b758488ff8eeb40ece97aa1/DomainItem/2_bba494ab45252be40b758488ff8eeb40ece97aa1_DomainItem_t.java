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
 
 import pt.ist.fenixframework.pstm.AbstractDomainObject;
 
 import com.vaadin.data.Property;
 import com.vaadin.data.util.metamodel.MetaModel;
 import com.vaadin.data.util.metamodel.PropertyDescriptor;
 
 /**
  * @author Pedro Santos (pedro.miguel.santos@ist.utl.pt)
  * 
  */
 public class DomainItem extends AbstractDomainItem {
     private PropertyDescriptor descriptor;
 
     public DomainItem(AbstractDomainObject value) {
 	super(value);
     }
 
     public DomainItem(Class<? extends AbstractDomainObject> type) {
 	super(type);
     }
 
     public DomainItem(AbstractDomainItem host, PropertyDescriptor descriptor) {
 	super(host, descriptor.getPropertyType());
 	this.descriptor = descriptor;
     }
 
     /**
      * @see com.vaadin.data.util.AbstractDomainProperty#isRequired()
      */
     @Override
     public boolean isRequired() {
	return descriptor != null ? descriptor.isRequired() : false;
     }
 
     /**
      * @see com.vaadin.data.util.AbstractDomainProperty#getNullValue()
      */
     @Override
     protected Object getNullValue() {
 	return null;
     }
 
     /**
      * @see com.vaadin.data.util.AbstractDomainProperty#getValueFrom(pt.ist.
      *      fenixframework.pstm.AbstractDomainObject)
      */
     @Override
     protected Object getValueFrom(AbstractDomainObject host) {
 	return descriptor.read(host);
     }
 
     /**
      * @see com.vaadin.data.util.AbstractDomainProperty#setValueOn(pt.ist.fenixframework
      *      .pstm.AbstractDomainObject, java.lang.Object)
      */
     @Override
     protected void setValueOn(AbstractDomainObject host, Object newValue) throws ConversionException {
 	descriptor.write(host, newValue);
     }
 
     /**
      * @see com.vaadin.data.util.AbstractDomainItem#lazyCreateProperty(java.lang.
      *      Object)
      */
     @Override
     protected Property lazyCreateProperty(Object propertyId) {
 	MetaModel model = MetaModel.findMetaModelForType(getType());
 	PropertyDescriptor descriptor = model.getPropertyDescriptor((String) propertyId);
 	if (descriptor != null) {
 	    if (descriptor.isCollection()) {
 		return new DomainContainer(this, descriptor);
 	    } else if (AbstractDomainObject.class.isAssignableFrom(descriptor.getPropertyType())) {
 		return new DomainItem(this, descriptor);
 	    } else {
 		return new DomainProperty(this, descriptor);
 	    }
 	}
 	return null;
     }
 
 }
