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
 package pt.ist.vaadinframework.ui;
 
 import java.util.ResourceBundle;
 
 import pt.ist.vaadinframework.VaadinFrameworkLogger;
 import pt.ist.vaadinframework.data.BufferedContainer;
 
 import com.vaadin.data.Container;
 import com.vaadin.data.Container.PropertySetChangeEvent;
 import com.vaadin.data.Property;
 import com.vaadin.ui.AbstractComponent;
 import com.vaadin.ui.Field;
 import com.vaadin.ui.Table;
 
 public class TransactionalTable extends Table {
     private final ResourceBundle bundle;
 
     private final PropertySetChangeListener headerUpdater = new PropertySetChangeListener() {
 	@Override
 	public void containerPropertySetChange(PropertySetChangeEvent event) {
 	    if (event.getContainer() instanceof BufferedContainer) {
 		for (Object propertyId : event.getContainer().getContainerPropertyIds()) {
 		    computeHeader((BufferedContainer<?, ?, ?>) event.getContainer(), propertyId);
 		}
 	    }
 	}
     };
 
     public TransactionalTable(ResourceBundle bundle) {
 	super();
 	this.bundle = bundle;
     }
 
     @Override
     protected Object getPropertyValue(Object rowId, Object colId, Property property) {
 	Object v = super.getPropertyValue(rowId, colId, property);
 	if (v instanceof Field) {
 	    Field field = (Field) v;
 	    // field.setWriteThrough(isWriteThrough());
 	    // field.setReadOnly(isReadOnly());
 	    if (isImmediate() && field instanceof AbstractComponent) {
 		((AbstractComponent) field).setImmediate(true);
 	    }
 	}
 	return v;
     }
 
     public void refresh() {
 	refreshRenderedCells();
     }
 
     @Override
     public void setContainerDataSource(Container newDataSource) {
 	if (getContainerDataSource() != null && getContainerDataSource() instanceof PropertySetChangeNotifier) {
	    ((PropertySetChangeNotifier) getContainerDataSource()).removeListener(headerUpdater);
 	}
 	if (newDataSource instanceof BufferedContainer) {
 	    for (Object propertyId : newDataSource.getContainerPropertyIds()) {
 		computeHeader((BufferedContainer<?, ?, ?>) newDataSource, propertyId);
 	    }
 	}
 	if (newDataSource instanceof PropertySetChangeNotifier) {
 	    ((PropertySetChangeNotifier) newDataSource).addListener(headerUpdater);
 	}
 	super.setContainerDataSource(newDataSource);
     }
 
     private void computeHeader(BufferedContainer<?, ?, ?> container, Object propertyId) {
 	String key = container.getElementType().getName() + "." + propertyId;
 	if (bundle.containsKey(key)) {
 	    setColumnHeader(propertyId, bundle.getString(key));
 	} else {
 	    VaadinFrameworkLogger.getLogger().warn("i18n opportunity missed: " + key);
 	}
     }
 }
