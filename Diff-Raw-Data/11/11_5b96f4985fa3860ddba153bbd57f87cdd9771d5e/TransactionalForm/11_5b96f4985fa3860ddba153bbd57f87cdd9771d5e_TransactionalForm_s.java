 package pt.ist.vaadinframework.ui;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.ResourceBundle;
 
 import jvstm.cps.ConsistencyException;
 import pt.ist.fenixWebFramework.services.Service;
 import pt.ist.fenixframework.pstm.AbstractDomainObject.UnableToDetermineIdException;
 import pt.ist.fenixframework.pstm.IllegalWriteException;
 import pt.ist.vaadinframework.VaadinResourceConstants;
 import pt.ist.vaadinframework.VaadinResources;
 import pt.ist.vaadinframework.ui.layout.ControlsLayout;
 
 import com.vaadin.data.Buffered;
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Field;
 import com.vaadin.ui.Form;
 import com.vaadin.ui.OptionGroup;
 import com.vaadin.ui.Window.Notification;
 
 public class TransactionalForm extends Form implements VaadinResourceConstants {
     private final ControlsLayout controls = new ControlsLayout();
 
     public TransactionalForm(ResourceBundle bundle) {
 	setFormFieldFactory(new DefaultFieldFactory(bundle));
 	setFooter(controls);
     }
 
     public void addSubmitButton() {
 	addButton(VaadinResources.getString(COMMONS_ACTION_SUBMIT), new ClickListener() {
 	    @Override
 	    public void buttonClick(ClickEvent event) {
 		commit();
 		getWindow().showNotification(VaadinResources.getString(COMMONS_MESSAGE_SUBMIT),
 			Notification.TYPE_TRAY_NOTIFICATION);
 		if (getWindow().isClosable() && getWindow().getParent() != null) {
 		    getWindow().getParent().removeWindow(getWindow());
 		}
 	    }
 	});
     }
 
     public void addClearButton() {
 	addButton(VaadinResources.getString(COMMONS_ACTION_DISCARD), new ClickListener() {
 	    @Override
 	    public void buttonClick(ClickEvent event) {
 		discard();
 		getWindow().showNotification(VaadinResources.getString(COMMONS_MESSAGE_DISCARD),
 			Notification.TYPE_TRAY_NOTIFICATION);
 	    }
 	});
     }
 
     public void addCancelButton() {
 	addButton(VaadinResources.getString(COMMONS_ACTION_CANCEL), new ClickListener() {
 	    @Override
 	    public void buttonClick(ClickEvent event) {
 		discard();
 		getWindow().showNotification(VaadinResources.getString(COMMONS_MESSAGE_CANCEL),
 			Notification.TYPE_TRAY_NOTIFICATION);
 		if (getWindow().isClosable() && getWindow().getParent() != null) {
 		    getWindow().getParent().removeWindow(getWindow());
 		}
 	    }
 	});
     }
 
     public void addButton(String caption, ClickListener listener) {
 	Button button = new Button(caption, listener);
 	controls.addComponent(button);
     }
 
     public OptionGroup replaceWithOptionGroup(Object propertyId, Object[] values, Object[] descriptions) {
 	// Checks the parameters
 	if (propertyId == null || values == null || descriptions == null) {
 	    throw new NullPointerException("All parameters must be non-null");
 	}
 	if (values.length != descriptions.length) {
 	    throw new IllegalArgumentException("Value and description list are of different size");
 	}
 
 	// Gets the old field
 	final Field oldField = getField(propertyId);
 	if (oldField == null) {
 	    throw new IllegalArgumentException("Field with given propertyid '" + propertyId.toString() + "' can not be found.");
 	}
 	final Object value = oldField.getPropertyDataSource() == null ? oldField.getValue() : oldField.getPropertyDataSource()
 		.getValue();
 
 	// Checks that the value exists and check if the select should
 	// be forced in multiselect mode
 	boolean found = false;
 	boolean isMultiselect = false;
 	for (int i = 0; i < values.length && !found; i++) {
 	    if (values[i] == value || (value != null && value.equals(values[i]))) {
 		found = true;
 	    }
 	}
 	if (value != null && !found) {
 	    if (value instanceof Collection) {
 		for (final Iterator<?> it = ((Collection<?>) value).iterator(); it.hasNext();) {
 		    final Object val = it.next();
 		    found = false;
 		    for (int i = 0; i < values.length && !found; i++) {
 			if (values[i] == val || (val != null && val.equals(values[i]))) {
 			    found = true;
 			}
 		    }
 		    if (!found) {
 			throw new IllegalArgumentException("Currently selected value '" + val + "' of property '"
 				+ propertyId.toString() + "' was not found");
 		    }
 		}
 		isMultiselect = true;
 	    } else {
 		throw new IllegalArgumentException("Current value '" + value + "' of property '" + propertyId.toString()
 			+ "' was not found");
 	    }
 	}
 
 	// Creates the new field matching to old field parameters
 	final OptionGroup newField = new OptionGroup();
 	if (isMultiselect) {
 	    newField.setMultiSelect(true);
 	}
 	newField.setCaption(oldField.getCaption());
 	newField.setReadOnly(oldField.isReadOnly());
 	newField.setReadThrough(oldField.isReadThrough());
 	newField.setWriteThrough(oldField.isWriteThrough());
 
 	// Creates the options list
 	newField.addContainerProperty("desc", String.class, "");
 	newField.setItemCaptionPropertyId("desc");
 	for (int i = 0; i < values.length; i++) {
 	    Object id = values[i];
 	    final Item item;
 	    if (id == null) {
 		id = newField.addItem();
 		item = newField.getItem(id);
 		newField.setNullSelectionItemId(id);
 	    } else {
 		item = newField.addItem(id);
 	    }
 
 	    if (item != null) {
 		item.getItemProperty("desc").setValue(descriptions[i].toString());
 	    }
 	}
 
 	// Sets the property data source
 	final Property property = oldField.getPropertyDataSource();
 	oldField.setPropertyDataSource(null);
 	newField.setPropertyDataSource(property);
 
 	// Replaces the old field with new one
 	getLayout().replaceComponent(oldField, newField);
 	attachField(propertyId, newField);
 	// newField.addListener(fieldValueChangeListener);
 	// oldField.removeListener(fieldValueChangeListener);
 
 	return newField;
     }
 
     @Override
     @Service
     public void commit() {
 	try {
	    super.commit();
 	    if (isValid() && getItemDataSource() instanceof Buffered) {
 		Buffered buffer = (Buffered) getItemDataSource();
 		if (!buffer.isWriteThrough()) {
 		    buffer.commit();
 		}
 	    }
 	} catch (Buffered.SourceException e) {
 	    findIllegalWritesInside(e);
 	    focus();
 	    throw e;
 	}
     }
 
     @Override
     @Service
     public void discard() throws SourceException {
 	try {
 	    if (getItemDataSource() instanceof Buffered) {
 		Buffered buffer = (Buffered) getItemDataSource();
 		buffer.discard();
 	    }
 	    super.discard();
 	} catch (Buffered.SourceException e) {
 	    findIllegalWritesInside(e);
 	    focus();
 	    throw e;
 	}
     }
 
     public void findIllegalWritesInside(Throwable throwable) {
 	if (throwable instanceof IllegalWriteException) {
 	    throw (IllegalWriteException) throwable;
 	} else if (throwable instanceof ConsistencyException) {
 	    throw (ConsistencyException) throwable;
 	} else if (throwable instanceof UnableToDetermineIdException) {
 	    throw (UnableToDetermineIdException) throwable;
 	} else if (throwable instanceof Buffered.SourceException) {
 	    for (Throwable cause : ((Buffered.SourceException) throwable).getCauses()) {
 		findIllegalWritesInside(cause);
 	    }
 	} else if (throwable.getCause() != null) {
 	    findIllegalWritesInside(throwable.getCause());
 	}
     }
 }
