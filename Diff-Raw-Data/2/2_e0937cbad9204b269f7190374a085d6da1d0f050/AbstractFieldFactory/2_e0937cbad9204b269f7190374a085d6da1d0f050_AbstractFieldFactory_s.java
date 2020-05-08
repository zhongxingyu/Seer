 package pt.ist.vaadinframework.ui;
 
 import java.util.ResourceBundle;
 
 import pt.ist.vaadinframework.VaadinFrameworkLogger;
 import pt.ist.vaadinframework.VaadinResourceConstants;
 import pt.ist.vaadinframework.VaadinResources;
 
 import com.vaadin.data.Container;
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 import com.vaadin.data.util.AbstractDomainItem;
 import com.vaadin.data.util.AbstractDomainProperty;
 import com.vaadin.ui.AbstractSelect;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.DefaultFieldFactory;
 import com.vaadin.ui.Field;
 import com.vaadin.ui.FormFieldFactory;
 import com.vaadin.ui.TableFieldFactory;
 
 public abstract class AbstractFieldFactory implements FormFieldFactory, TableFieldFactory, VaadinResourceConstants {
     protected final ResourceBundle bundle;
 
     public AbstractFieldFactory(ResourceBundle bundle) {
 	this.bundle = bundle;
     }
 
     @Override
     public final Field createField(Container container, Object itemId, Object propertyId, Component uiContext) {
 	Item item = container.getItem(itemId);
 	Field field = makeField(item, propertyId, uiContext);
 	return initField(field, item, propertyId, uiContext);
     }
 
     @Override
     public final Field createField(Item item, Object propertyId, Component uiContext) {
 	Field field = makeField(item, propertyId, uiContext);
 	return initField(field, item, propertyId, uiContext);
     }
 
     protected Field initField(Field field, Item item, Object propertyId, Component uiContext) {
 	String caption = makeCaption(item, propertyId, uiContext);
 	field.setCaption(caption);
 	field.setDescription(makeDescription(item, propertyId, uiContext));
 	boolean required = isRequired(item.getItemProperty(propertyId));
 	field.setRequired(required);
	field.setRequiredError(VaadinResources.getString(REQUIRED_ERROR, caption));
 	if (field instanceof AbstractSelect) {
 	    ((AbstractSelect) field).setNullSelectionAllowed(!required);
 	}
 	return field;
     }
 
     private boolean isRequired(Property property) {
 	if (property instanceof AbstractDomainProperty) {
 	    return ((AbstractDomainProperty) property).isRequired();
 	}
 	return false;
     }
 
     protected abstract Field makeField(Item item, Object propertyId, Component uiContext);
 
     protected String makeCaption(Item item, Object propertyId, Component uiContext) {
 	if (item instanceof AbstractDomainItem) {
 	    String key = ((AbstractDomainItem) item).getLabelKey(bundle,propertyId);
 	    if (bundle.containsKey(key)) {
 		return bundle.getString(key);
 	    }
 	    VaadinFrameworkLogger.getLogger().warn("i18n opportunity missed: " + key);
 	}
 	return DefaultFieldFactory.createCaptionByPropertyId(propertyId);
     }
 
     protected String makeDescription(Item item, Object propertyId, Component uiContext) {
 	if (item instanceof AbstractDomainItem) {
 	    String key = ((AbstractDomainItem) item).getDescriptionKey(bundle,propertyId);
 	    if (bundle.containsKey(key)) {
 		return bundle.getString(key);
 	    }
 	    VaadinFrameworkLogger.getLogger().warn("i18n opportunity missed: " + key);
 	}
 	return makeCaption(item, propertyId, uiContext);
     }
 }
