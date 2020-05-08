 package com.jtbdevelopment.e_eye_o.ria.vaadin.utils;
 
 import com.vaadin.ui.AbstractComponent;
 import com.vaadin.ui.AbstractField;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.HasComponents;
 
 /**
  * Date: 3/30/13
  * Time: 8:59 PM
  */
 public class ComponentUtils {
     private static interface Callback<T extends AbstractComponent> {
         public void doWork(final T component);
     }
 
     public static void removeAllValidators(final Component component) {
         processAllComponents(component, AbstractField.class, new Callback<AbstractField>() {
             @Override
             public void doWork(final AbstractField component) {
                 component.removeAllValidators();
             }
         });
     }
 
     public static void setImmediateForAll(final Component component, final boolean immediate) {
         processAllComponents(component, AbstractComponent.class, new Callback<AbstractComponent>() {
             @Override
             public void doWork(final AbstractComponent component) {
                 component.setImmediate(immediate);
             }
         });
     }
 
    public static void clearAllErrors(final Component component) {
        processAllComponents(component, AbstractComponent.class, new Callback<AbstractComponent>() {
            @Override
            public void doWork(AbstractComponent component) {
                component.setComponentError(null);
            }
        });
    }

     private static <T extends AbstractComponent> void processAllComponents(final Component topComponent, final Class<T> componentTypes, final Callback<T> callback) {
         if (componentTypes.isAssignableFrom(topComponent.getClass())) {
             callback.doWork((T) topComponent);
         }
         if (topComponent instanceof HasComponents) {
             for (Component childComponent : ((HasComponents) topComponent)) {
                 processAllComponents(childComponent, componentTypes, callback);
             }
         }
     }
 }
