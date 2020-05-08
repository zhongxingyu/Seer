 package com.vaadin.data.util;
 
 import java.lang.reflect.Field;
 import java.util.Collection;
 import java.util.Collections;
import java.util.HashMap;
 import java.util.Map;
 
 import com.vaadin.data.Property;
 
 /**
 * Extensible subclass of AbstractBeanContainer. BeanItem properties are
 * initialized lazily.
  * 
  * @author Henri Sara
  */
 public class ExtensibleBeanContainer<IDTYPE, BEANTYPE> extends
         AbstractBeanContainer<IDTYPE, BEANTYPE> {
 
     private Field modelField;
 
     private Map<Object, VaadinPropertyDescriptor<BEANTYPE>> properties;
 
     public ExtensibleBeanContainer(Class<? super BEANTYPE> beanBaseClass) {
         super(beanBaseClass);
 
         try {
             modelField = AbstractBeanContainer.class.getDeclaredField("model");
             modelField.setAccessible(true);
         } catch (SecurityException e) {
            throw new RuntimeException(ExtensibleBeanContainer.class.getName()
                    + " is incompatible with the Vaadin version used");
         } catch (NoSuchFieldException e) {
            throw new RuntimeException(ExtensibleBeanContainer.class.getName()
                    + " is incompatible with the Vaadin version used");
         }
     }
 
     /**
      * This variant of BeanItem creates property instances only when requested
      * and tries to minimize memory overhead by sharing metadata between them.
      */
     public class CompactBeanItem extends BeanItem<BEANTYPE> {
 
         public CompactBeanItem(BEANTYPE bean) {
            super(bean,
                    new HashMap<String, VaadinPropertyDescriptor<BEANTYPE>>());
         }
 
         @Override
         public Collection<?> getItemPropertyIds() {
             return Collections.unmodifiableCollection(properties.keySet());
         }
 
         @Override
         public Property getItemProperty(Object id) {
             Property property = super.getItemProperty(id);
             if (property == null) {
                 // create and add a property only when requested
                 property = createProperty(id);
                 addItemProperty(id, property);
             }
             return property;
         }
 
         protected Property createProperty(Object id) {
             VaadinPropertyDescriptor pd = getModel().get(id);
             Property property;
             // TODO optimize by sharing metadata for
             // MethodPropertyDescriptor and NestedPropertyDescriptor
             property = pd.createProperty(getBean());
             return property;
         }
     }
 
     @Override
     protected BeanItem<BEANTYPE> createBeanItem(BEANTYPE bean) {
         return bean == null ? null : new CompactBeanItem(bean);
     }
 
     protected Map<String, VaadinPropertyDescriptor<BEANTYPE>> getModel() {
         try {
             return (Map<String, VaadinPropertyDescriptor<BEANTYPE>>) modelField
                     .get(this);
         } catch (IllegalArgumentException e) {
             throw new IllegalStateException("Container model not accessible");
         } catch (IllegalAccessException e) {
             throw new IllegalStateException("Container model not accessible");
         }
     }
 
     @Override
     public boolean addContainerProperty(Object propertyId, Class<?> type,
             Object defaultValue) throws UnsupportedOperationException {
         // TODO implement
         return false;
     }
 
 }
