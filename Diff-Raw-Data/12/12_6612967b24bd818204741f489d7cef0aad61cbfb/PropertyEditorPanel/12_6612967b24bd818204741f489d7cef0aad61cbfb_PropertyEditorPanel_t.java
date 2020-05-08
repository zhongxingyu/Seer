 package org.iplantc.core.tito.client.panels;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.metadata.client.PropertyType;
 import org.iplantc.core.metadata.client.property.DataObject;
 import org.iplantc.core.metadata.client.property.Property;
 import org.iplantc.core.metadata.client.property.PropertyTypeCategory;
 import org.iplantc.core.tito.client.I18N;
 import org.iplantc.core.tito.client.events.CommandLineArgumentChangeEvent;
 import org.iplantc.core.tito.client.services.EnumerationServices;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.events.EventBus;
 
 import com.extjs.gxt.ui.client.core.FastMap;
 import com.extjs.gxt.ui.client.data.BaseModelData;
 import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.Label;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.VerticalPanel;
 import com.extjs.gxt.ui.client.widget.form.ComboBox;
 import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 /**
  * Panel for editing property specific data.
  * 
  * @author amuir
  * 
  */
 public class PropertyEditorPanel extends ContentPanel {
     private static final String ID_PROPERTY_TYPE = "idPropertyType"; //$NON-NLS-1$
 
     private final Property property;
     private LayoutContainer containerMain;
     private PropertyTypeEditorPanel pnlPropertyTypeEditor;
 
     private FastMap<List<PropertyType>> propertyTypes; // category -> property types
 
     private ComboBox<CategoryListItem> comboPropertyTypeCategory;
 
     /**
      * Instantiate from the property group container and property to be edited.
      * 
      * @param container parent property group container (needed for validating against external fields).
      * @param property property to be edited.
      */
     public PropertyEditorPanel(final Property property) {
         this.property = property;
 
         init();
 
         buildInstanceWidgets();
     }
 
     private void init() {
         setHeaderVisible(false);
         setBodyStyle("background-color: #EDEDED"); //$NON-NLS-1$
         setLayout(new FitLayout());
     }
 
     private void buildInstanceWidgets() {
         buildPropertyTypeCategoryList();
 
         // This should be called last, since it will build the propertyTypes map and call
         // initFromPropertyCategory, which will make a selection in the PropertyTypeCategory list, which
         // should be initialized before this method call, and will call handlePropertyCategoryChange,
         // which builds and displays the correct PropertyTypeEditorPanel.
         populateWidgetTypeList();
     }
 
     private void buildPropertyTypeCategoryList() {
         comboPropertyTypeCategory = new ComboBox<CategoryListItem>();
         comboPropertyTypeCategory.setId(ID_PROPERTY_TYPE);
         comboPropertyTypeCategory.setWidth("140px"); //$NON-NLS-1$
 
         // enable auto-complete
         comboPropertyTypeCategory.setEditable(true);
         comboPropertyTypeCategory.setTypeAhead(true);
         comboPropertyTypeCategory.setQueryDelay(1000);
 
         comboPropertyTypeCategory.setFireChangeEventOnSetValue(true);
         comboPropertyTypeCategory.setForceSelection(true);
         comboPropertyTypeCategory.setTriggerAction(TriggerAction.ALL);
 
         ListStore<CategoryListItem> store = new ListStore<CategoryListItem>();
         comboPropertyTypeCategory.setStore(store);
 
         for (PropertyTypeCategory category : PropertyTypeCategory.values()) {
             store.add(new CategoryListItem(category));
         }
 
         // NOTE: We don't select a default category here, since the category will be selected from the
         // property type in initFromPropertyCategory, which is only called after the propertyTypes map is
         // built from a service call.
         // Also, this listener is only fired if the category changes, which won't happen if we select a
         // default category that is the same for the given property type, but we need this listener to
         // fire from the selection made in initFromPropertyCategory since handlePropertyCategoryChange
         // will initialize the correct PropertyTypeEditorPanel (which may also depend on an initialized
         // propertyTypes map).
         comboPropertyTypeCategory
                 .addSelectionChangedListener(new SelectionChangedListener<CategoryListItem>() {
                     @Override
                     public void selectionChanged(SelectionChangedEvent<CategoryListItem> se) {
                         PropertyTypeCategory category = getSelectedPropertyTypeCategory();
                         if (category != null) {
                             handlePropertyCategoryChange(category);
                         }
                         
                         // changing the category resets the default value, so fire a change event
                         EventBus.getInstance().fireEvent(new CommandLineArgumentChangeEvent(property));
                     }
                 });
     }
 
     private PropertyTypeCategory getSelectedPropertyTypeCategory() {
         List<CategoryListItem> selection = comboPropertyTypeCategory.getSelection();
 
         if (selection != null && selection.size() > 0) {
             CategoryListItem categoryModel = selection.get(0);
 
             if (categoryModel != null) {
                 return categoryModel.getCategory();
             }
         }
 
         return null;
     }
 
     private LayoutContainer buildPropertyTypeDropdown() {
         VerticalPanel ret = new VerticalPanel();
 
         ret.add(new Label(I18N.DISPLAY.parameterType() + ": ")); //$NON-NLS-1$
         ret.add(comboPropertyTypeCategory);
 
         return ret;
     }
 
     private void handlePropertyCategoryChange(final PropertyTypeCategory category) {
         // Check if the current default value is already valid for the new category.
         // If editing a saved property, or if the user only navigated away from this property then back,
         // the value should not be reset.
         boolean resetDefault = true;
 
         String propertyType = property.getType();
         List<PropertyType> types = propertyTypes.get(category.toString());
 
         for (PropertyType type : types) {
             if (type.getName().equals(propertyType)) {
                 // The property's type is valid for this category, so don't reset it's default value.
                 resetDefault = false;
                 break;
             }
         }
 
         if (resetDefault) {
             resetPropertyValue(category);
         }
 
         updateEditorPanel(category);
     }
 
     /**
      * Resets the property value to the default for the given type category.
      * 
      * @param category The category that determines what default property value to use.
      */
     private void resetPropertyValue(PropertyTypeCategory category) {
         switch (category) {
             case BOOLEAN:
                 property.setValue(BooleanPropertyEditorPanel.DEFAULT_BOOLEAN);
                 break;
 
             default:
                 property.setValue(PropertyTypeEditorPanel.DEFAULT_STRING);
                 break;
         }
     }
 
     /**
      * Replace the contents of the center panel.
      * 
      * @param container a new component to set in the center of the BorderLayout.
      */
     private void updateEditorPanel(final PropertyTypeCategory category) {
         if (containerMain != null && pnlPropertyTypeEditor != null) {
             containerMain.remove(pnlPropertyTypeEditor);
         }
 
         switch (category) {
             case INPUT:
                 pnlPropertyTypeEditor = new InputDataObjectFormPanel(property);
                 break;
 
             case OUTPUT:
                 pnlPropertyTypeEditor = new OutputDataObjectFormPanel(property);
                 break;
 
             case BOOLEAN:
                 pnlPropertyTypeEditor = new BooleanPropertyEditorPanel(property);
                 break;
 
             case STRING:
                 pnlPropertyTypeEditor = new StringPropertyEditorPanel(property);
                 break;
 
             case NUMBER:
                 pnlPropertyTypeEditor = new NumberPropertyEditorPanel(property);
                 break;
 
             default:
                 pnlPropertyTypeEditor = null;
                 break;
         }
 
         if (containerMain != null && pnlPropertyTypeEditor != null) {
             containerMain.add(pnlPropertyTypeEditor);
 
             if (pnlPropertyTypeEditor instanceof PropertySubTypeEditorPanel) {
                 // update the PropertySubTypeEditorPanel with the correct list of widget-types.
                 ((PropertySubTypeEditorPanel)pnlPropertyTypeEditor)
                         .updatePropertyTypesToListBox(propertyTypes.get(category.toString()));
             }
 
             layout();
         }
     }
 
     private String getCategoryFromType(final String propertyType) {
         if (DataObject.INPUT_TYPE.equals(propertyType)) {
             return PropertyTypeCategory.INPUT.toString();
         }
         if (DataObject.OUTPUT_TYPE.equals(propertyType)) {
             return PropertyTypeCategory.OUTPUT.toString();
         }
 
         String ret = ""; // assume failure //$NON-NLS-1$
 
         for (String category : propertyTypes.keySet()) {
             List<PropertyType> types = propertyTypes.get(category);
 
             for (PropertyType type : types) {
                 if (type.getName().equals(propertyType)) {
                     ret = category;
                     break;
                 }
             }
         }
 
         return ret;
     }
 
     private void initFromPropertyCategory() {
         String category = getCategoryFromType(property.getType());
 
         ListStore<CategoryListItem> store = comboPropertyTypeCategory.getStore();
 
         // assume failure... default to first item
         CategoryListItem selectedCategory = store.getAt(0);
 
         for (CategoryListItem categoryModel : store.getModels()) {
             if (categoryModel.getDisplay().equals(category)) {
                 selectedCategory = categoryModel;
                 break;
             }
         }
 
         comboPropertyTypeCategory.setValue(selectedCategory);
     }
 
     private void initPropertyTypes() {
         propertyTypes = new FastMap<List<PropertyType>>();
 
         for (PropertyTypeCategory category : PropertyTypeCategory.values()) {
             propertyTypes.put(category.toString(), new ArrayList<PropertyType>());
         }
     }
 
     private void populateWidgetTypeList() {
         mask(I18N.DISPLAY.loadingMask());
 
         EnumerationServices services = new EnumerationServices();
         services.getWidgetTypes(new AsyncCallback<String>() {
             @Override
             public void onSuccess(String result) {
                 if (result != null) {
                     initPropertyTypes();
 
                     JSONArray property_types = JsonUtil.getArray(JsonUtil.getObject(result),
                             "property_types"); //$NON-NLS-1$
 
                     if (property_types != null) {
                         for (int i = 0,len = property_types.size(); i < len; i++) {
                             PropertyType typeProperty = new PropertyType(JsonUtil.getObjectAt(
                                     property_types, i));
 
                            // Ensure that the PropertyType has a known value_type field before adding it
                            // to propertyTypes.
                            String valueType = typeProperty.getType();
                            if (valueType != null && !valueType.isEmpty()) {
                                List<PropertyType> list = propertyTypes.get(valueType);

                                if (list != null) {
                                    list.add(typeProperty);
                                }
                            }
                         }
 
                         // now that we've populated our property types list, select the correct category,
                         // which will init the correct PropertyTypeEditorPanel.
                         initFromPropertyCategory();
                     } else {
                         ErrorHandler.post(I18N.DISPLAY.cantLoadWidgetTypes());
                     }
                 } else {
                     ErrorHandler.post(I18N.DISPLAY.cantLoadWidgetTypes());
                 }
 
                 unmask();
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(I18N.DISPLAY.cantLoadWidgetTypes(), caught);
                 unmask();
             }
         });
     }
 
     private LayoutContainer buildPanel() {
         VerticalPanel ret = new VerticalPanel();
         ret.setLayout(new FitLayout());
         ret.setStyleAttribute("background-color", "#EDEDED"); //$NON-NLS-1$ //$NON-NLS-2$
         ret.setSpacing(8);
 
         ret.add(buildPropertyTypeDropdown());
 
         if (pnlPropertyTypeEditor != null) {
             ret.add(pnlPropertyTypeEditor);
         }
 
         return ret;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void onRender(Element parent, int pos) {
         super.onRender(parent, pos);
 
         containerMain = buildPanel();
         add(containerMain);
     }
 
     /**
      * A BaseModelData class for the Property types ComboBox ListStore.
      * 
      * @author psarando
      * 
      */
     protected class CategoryListItem extends BaseModelData {
         private static final long serialVersionUID = 4583897203393642484L;
 
         public CategoryListItem(PropertyTypeCategory category) {
             set("text", category.toString()); //$NON-NLS-1$
             set("value", category); //$NON-NLS-1$
         }
 
         public String getDisplay() {
             return get("text"); //$NON-NLS-1$
         }
 
         public PropertyTypeCategory getCategory() {
             return get("value"); //$NON-NLS-1$
         }
         
         @Override
         public String toString() {
             return getDisplay() + " " + getCategory(); //$NON-NLS-1$
         }
     }
 }
