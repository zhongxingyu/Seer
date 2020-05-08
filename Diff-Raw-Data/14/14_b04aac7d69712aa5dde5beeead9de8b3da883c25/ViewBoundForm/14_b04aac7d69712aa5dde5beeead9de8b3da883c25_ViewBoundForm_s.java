 package org.shortbrain.vaadin.form;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.vaadin.data.Container;
 import com.vaadin.data.Item;
 import com.vaadin.ui.ComponentContainer;
 import com.vaadin.ui.CssLayout;
 import com.vaadin.ui.CustomComponent;
 import com.vaadin.ui.Field;
 import com.vaadin.ui.Form;
 import com.vaadin.ui.Layout;
 
 /**
  * A customized {@link Form} implementation that uses pre created components specified in the given Layout (a {@link CustomComponent}).
  * 
  * This implementation will bind data from an {@link Item} and several {@link Container}s to add data to the, already defined, fields,
  * label, etc.
  * 
  * The basic usage should be :
  * 
  * <pre>
  * // Instance of the view (Layout with Fields)
  * GridLayout layout = new MyViewLayout();
  * ViewBoundForm form = new ViewBoundForm();
  * // Add containers for properties (if needed)
  * form.setDataSource(&quot;aValueOf&quot;, myContainer);
  * form.setContent(layout);
  * </pre>
  * 
  * @author Vincent Demeester <vincent+git@demeester.fr>
  * 
  */
 public class ViewBoundForm extends Form {
 
     private static final long serialVersionUID = 3644494410218426355L;
 
     /**
      * The map of data source by properties
      */
     private Map<Object, Container> propertyDataSource;
 
     /**
      * Create a {@link ViewBoundForm} with the default FieldsHelper.
      */
     public ViewBoundForm() {
         this(new HashMap<Object, Container>());
     }
 
     public ViewBoundForm(ComponentContainer layout) {
         this(layout, new HashMap<Object, Container>());
     }
 
     public ViewBoundForm(Map<Object, Container> propertyDataSource) {
         if (propertyDataSource == null) {
             throw new IllegalArgumentException("propertyDataSource cannot be null");
         }
         this.propertyDataSource = propertyDataSource;
     }
 
     public ViewBoundForm(ComponentContainer layout, Map<Object, Container> propertyDataSource) {
         this(propertyDataSource);
         if (layout == null) {
             throw new IllegalArgumentException("layout cannot be null");
         }
         setContent(layout);
     }
 
     /**
      * Set the external data source for the given property.
      * 
      * @param property
      *            the object representing the property
      * @param container
      *            the container with data
      */
     public void setDataSource(Object property, Container container) {
         propertyDataSource.put(property, container);
     }
 
     /**
      * Sets the layout (aka view) of this form. The layout is used as a primary source for pre created fields, the actual form as a
      * secondary source.
      * 
      * @param layout
      */
     public void setContent(ComponentContainer layout) {
         if (layout instanceof Layout) {
             super.setLayout((Layout) layout);
         } else {
             // form only accepts Layout as content so wrap into CssLayout if
             // necessary (e.g. CustomComponent)
             CssLayout cssLayout = new CssLayout();
             cssLayout.addComponent(layout);
             if (layout.getWidthUnits() == UNITS_PERCENTAGE) {
                 cssLayout.setWidth("100%");
             }
             if (layout.getHeightUnits() == UNITS_PERCENTAGE) {
                 cssLayout.setHeight("100%");
             }
             super.setLayout(cssLayout);
         }
        setFormFieldFactory(new ViewBoundFormFieldFactory(propertyDataSource, super.getLayout()));
     }
 
     @Override
     public void setLayout(Layout newLayout) {
         if (newLayout == null) {
             super.setLayout(newLayout);
         } else {
             setContent(newLayout);
         }
     }
 
     @Override
     protected void attachField(Object propertyId, Field field) {
         // Doing nothing as fields are already attached.
     }
 
     @Override
     protected void detachField(Field field) {
         // Doing nothing as fiels are already attached.
     }
 
 }
