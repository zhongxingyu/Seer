 package org.shortbrain.vaadin.form;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.Map;
 
 import org.apache.commons.beanutils.PropertyUtils;
 
 import com.vaadin.data.Container;
 import com.vaadin.data.Item;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.Field;
 import com.vaadin.ui.FieldFactory;
 import com.vaadin.ui.FormFieldFactory;
 import com.vaadin.ui.Layout;
 
 /**
  * {@link FormFieldFactory} implementation used for {@link ViewBoundForm}. It takes an algorithm to get the fields from the view, add some
  * stuff and options if needed (containers, …).
  * 
  * @author Vincent Demeester <vincent@shortbrain.org>
  * 
  */
 public class ViewBoundFormFieldFactory implements FormFieldFactory {
 
     private static final long serialVersionUID = -676197212496028316L;
 
     private Map<Object, Container> propertyDataSource;
    private Layout layout;
 
     /**
      * Creates a {@link ViewBoundFormFieldFactory} with the propert
      * 
      * @param propertyDataSource
      * @param layout
      */
    public ViewBoundFormFieldFactory(Map<Object, Container> propertyDataSource, Layout layout) {
         this.propertyDataSource = propertyDataSource;
         this.layout = layout;
     }
 
     @Override
     public Field createField(Item item, Object propertyId, Component uiContext) {
         Field f = null;
         f = findField(layout, propertyId);
         return f;
     }
 
     private Field findField(Object view, Object propertyId) {
         Field f = null;
         final Class<?> viewClass = view.getClass();
         try {
             f = getFieldFromGetter(view, propertyId + "Field");
             // Add data
             if (propertyDataSource.containsKey(propertyId)) {
                 addDataSource(f, propertyId);
             }
         } catch (IllegalAccessException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (InvocationTargetException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (NoSuchMethodException e) {
             // There is no Field, fail silently (property not in the form)
         }
         return f;
     }
 
     private void addDataSource(Field f, Object propertyId) {
         Container container = propertyDataSource.get(propertyId);
         if (f instanceof Container.Viewer) {
             Container.Viewer viewer = (Container.Viewer) f;
             viewer.setContainerDataSource(container);
         }
     }
 
     /**
      * Find the field by looking for getter. It uses the BeanUtils {@link PropertyUtils#getProperty(Object, String)} methd.
      * 
      * @param view
      * @param propertyId
      * @return
      * @throws IllegalAccessException
      * @throws InvocationTargetException
      * @throws NoSuchMethodException
      * @throws IllegalArgumentException
      */
     private Field getFieldFromGetter(Object view, Object propertyId) throws IllegalAccessException,
             InvocationTargetException, NoSuchMethodException {
         if (!(propertyId instanceof String)) {
             throw new IllegalArgumentException("the propertyId has to be be a String.");
         }
         Object field = PropertyUtils.getProperty(view, (String) propertyId);
         if (field instanceof Field) {
             return (Field) field;
         } else {
             throw new IllegalArgumentException("the property should be a Field.");
         }
     }
 
 }
