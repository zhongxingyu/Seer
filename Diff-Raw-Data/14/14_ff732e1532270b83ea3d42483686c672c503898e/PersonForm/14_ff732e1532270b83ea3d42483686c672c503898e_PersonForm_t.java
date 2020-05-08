 package com.vaadin.addon.sqlcontainer.demo.addressbook.ui;
 
 import java.sql.SQLException;
 import java.util.Arrays;
 import java.util.List;
 
 import com.vaadin.addon.sqlcontainer.RowId;
 import com.vaadin.addon.sqlcontainer.demo.addressbook.AddressBookApplication;
 import com.vaadin.addon.sqlcontainer.demo.addressbook.data.DatabaseHelper;
 import com.vaadin.addon.sqlcontainer.query.QueryDelegate;
 import com.vaadin.addon.sqlcontainer.query.QueryDelegate.RowIdChangeEvent;
 import com.vaadin.data.Buffered;
 import com.vaadin.data.Item;
 import com.vaadin.data.validator.EmailValidator;
 import com.vaadin.data.validator.RegexpValidator;
 import com.vaadin.ui.AbstractSelect;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.ComboBox;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.DefaultFieldFactory;
 import com.vaadin.ui.Field;
 import com.vaadin.ui.Form;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 
 @SuppressWarnings("serial")
 public class PersonForm extends Form implements ClickListener,
         QueryDelegate.RowIdChangeListener {
 
     private Button save = new Button("Save", (ClickListener) this);
     private Button cancel = new Button("Cancel", (ClickListener) this);
     private Button edit = new Button("Edit", (ClickListener) this);
     private final ComboBox cities = new ComboBox();
 
     private final AddressBookApplication app;
 
     public PersonForm(final AddressBookApplication app) {
         this.app = app;
 
         /*
          * Enable buffering so that commit() must be called for the form before
          * input is written to the data source. (Form input is not written
          * immediately through to the underlying object.)
          */
         setWriteThrough(false);
 
         /* Init form footer */
         HorizontalLayout footer = new HorizontalLayout();
         footer.setSpacing(true);
         footer.addComponent(save);
         footer.addComponent(cancel);
         footer.addComponent(edit);
         footer.setVisible(false);
         setFooter(footer);
 
         /* Allow the user to enter new cities */
         cities.setNewItemsAllowed(true);
         /* We do not want to use null values */
         cities.setNullSelectionAllowed(false);
 
         /* Cities selection */
         cities.setContainerDataSource(app.getDbHelp().getCityContainer());
         cities.setItemCaptionPropertyId("NAME");
 
         cities.setImmediate(true);
 
         /* NewItemHandler to add new cities */
         cities.setNewItemHandler(new AbstractSelect.NewItemHandler() {
             public void addNewItem(String newItemCaption) {
                 app.getDbHelp().addCity(newItemCaption);
             }
         });
 
         /*
          * Field factory for overriding how the fields are created.
          */
         setFormFieldFactory(new DefaultFieldFactory() {
             @Override
             public Field createField(Item item, Object propertyId,
                     Component uiContext) {
                 Field field;
                 if (propertyId.equals("CITYID")) {
                     field = cities;
                 } else {
                     field = super.createField(item, propertyId, uiContext);
                 }
 
                 if (propertyId.equals("POSTALCODE")) {
                     /* Add a validator for postalCode and make it required */
                     field
                             .addValidator(new RegexpValidator("[1-9][0-9]{4}",
                                     "Postal code must be a five digit number and cannot start with a zero."));
                     field.setRequired(true);
                 } else if (propertyId.equals("EMAIL")) {
                     /* Add a validator for email and make it required */
                     field.addValidator(new EmailValidator(
                             "Email must contain '@' and have full domain."));
                     field.setRequired(true);
                 }
                 /* Set null representation of all text fields to empty */
                 if (field instanceof TextField) {
                     ((TextField) field).setNullRepresentation("");
                 }
 
                 field.setWidth("200px");
 
                 /* Set the correct caption to each field */
                 for (int i = 0; i < DatabaseHelper.NATURAL_COL_ORDER.length; i++) {
                     if (DatabaseHelper.NATURAL_COL_ORDER[i].equals(propertyId)) {
                         field.setCaption(DatabaseHelper.COL_HEADERS_ENGLISH[i]);
                     }
                 }
                 return field;
             }
         });
 
         /* Add PersonForm as RowIdChangeListener to the CityContainer */
         app.getDbHelp().getCityContainer().addListener(this);
     }
 
     public void buttonClick(ClickEvent event) {
         Button source = event.getButton();
         if (source == save) {
             /* If the given input is not valid there is no point in continuing */
             if (!isValid()) {
                 return;
             }
             commit();
         } else if (source == cancel) {
             discard();
         } else if (source == edit) {
             setReadOnly(false);
         }
     }
 
     @Override
     public void setItemDataSource(Item newDataSource) {
         if (newDataSource != null) {
             setReadOnly(false);
             List<Object> orderedProperties = Arrays
                     .asList(DatabaseHelper.NATURAL_COL_ORDER);
             super.setItemDataSource(newDataSource, orderedProperties);
             /* Select correct city from the cities ComboBox */
             if (newDataSource.getItemProperty("CITYID").getValue() != null) {
                 cities.select(new RowId(new Object[] { newDataSource
                         .getItemProperty("CITYID").getValue() }));
             } else {
                 cities.select(cities.getItemIds().iterator().next());
             }
             setReadOnly(true);
             getFooter().setVisible(true);
         } else {
             super.setItemDataSource(null);
             getFooter().setVisible(false);
         }
     }
 
     @Override
     public void setReadOnly(boolean readOnly) {
         super.setReadOnly(readOnly);
         save.setVisible(!readOnly);
         cancel.setVisible(!readOnly);
         edit.setVisible(readOnly);
     }
 
     public void addContact() {
         /* Roll back changes just in case */
         try {
             app.getDbHelp().getPersonContainer().rollback();
         } catch (SQLException ignored) {
         }
         /* Create a new item and set it as the data source for this form */
         Object tempItemId = app.getDbHelp().getPersonContainer().addItem();
         setItemDataSource(app.getDbHelp().getPersonContainer().getItem(
                 tempItemId));
         setReadOnly(false);
     }
 
     @Override
     public void commit() throws Buffered.SourceException {
         /* Commit the data entered to the person form to the actual item. */
         super.commit();
         /* Commit changes to the database. */
         try {
             app.getDbHelp().getPersonContainer().commit();
         } catch (SQLException e) {
             e.printStackTrace();
         }
         setReadOnly(true);
     }
 
     @Override
     public void discard() throws Buffered.SourceException {
         super.discard();
         /* On discard roll back the changes. */
         try {
             app.getDbHelp().getPersonContainer().rollback();
         } catch (SQLException e) {
             e.printStackTrace();
         }
        /* Clear the form */
         setItemDataSource(null);
         setReadOnly(true);
     }
 
     /*
      * Receive the new row ID of an added city and set it to the item data
      * source
      */
     public void rowIdChange(RowIdChangeEvent event) {
         cities.setValue(event.getNewRowId());
         getItemDataSource().getItemProperty("CITYID").setValue(
                 event.getNewRowId().getId()[0]);
     }
 }
