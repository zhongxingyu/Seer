 package com.jtbdevelopment.e_eye_o.entities.impl.reflection;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.jtbdevelopment.e_eye_o.entities.IdObject;
 import com.jtbdevelopment.e_eye_o.entities.annotations.IdObjectFieldSettings;
 import com.jtbdevelopment.e_eye_o.entities.reflection.IdObjectReflectionHelper;
 import org.apache.commons.beanutils.PropertyUtils;
 import org.springframework.stereotype.Service;
 
 import javax.annotation.Nullable;
 import java.beans.PropertyDescriptor;
 import java.beans.Transient;
 import java.lang.reflect.Method;
 import java.util.*;
 
 /**
  * Date: 1/28/13
  * Time: 10:39 PM
  */
 @Service
 @SuppressWarnings("unused")
 public class IdObjectReflectionHelperImpl implements IdObjectReflectionHelper {
 
     @SuppressWarnings("unchecked")
     public <T extends IdObject> Class<T> getIdObjectInterfaceForClass(final Class<T> entityType) {
         if (entityType.isInterface())
             return entityType;
 
         Class<T> found = null;
         for (Class i : entityType.getInterfaces()) {
             if (IdObject.class.isAssignableFrom(i)) {
                 found = (Class<T>) i;
                 break;
             }
         }
         return found;
     }
 
     @Override
    public Map<String, Method> getAllGetMethods(Class<? extends IdObject> entityType) {
         Function<PropertyDescriptor, Map.Entry<String, Method>> function = new Function<PropertyDescriptor, Map.Entry<String, Method>>() {
             @Nullable
             @Override
             public Map.Entry<String, Method> apply(@Nullable PropertyDescriptor property) {
                 if (property == null) {
                     return null;
                 }
                 return new AbstractMap.SimpleEntry<>(property.getName(), property.getReadMethod());
             }
         };
         Map<String, Method> readMethods = new HashMap<>();
         for (Map.Entry<String, Method> field : traverseInterfaces(entityType, function)) {
             readMethods.put(field.getKey(), field.getValue());
         }
         return readMethods;
     }
 
     @Override
    public Map<String, Method> getAllSetMethods(Class<? extends IdObject> entityType) {
         Function<PropertyDescriptor, Map.Entry<String, Method>> function = new Function<PropertyDescriptor, Map.Entry<String, Method>>() {
             @Nullable
             @Override
             public Map.Entry<String, Method> apply(@Nullable PropertyDescriptor property) {
                 if (property == null) {
                     return null;
                 }
                 return new AbstractMap.SimpleEntry<>(property.getName(), property.getWriteMethod());
             }
         };
         Map<String, Method> readMethods = new HashMap<>();
         for (Map.Entry<String, Method> field : traverseInterfaces(entityType, function)) {
             readMethods.put(field.getKey(), field.getValue());
         }
         return readMethods;
     }
 
     @Override
    public Map<String, IdObjectFieldSettings> getAllFieldPreferences(final Class<? extends IdObject> entityType) {
         final Function<PropertyDescriptor, Map.Entry<String, IdObjectFieldSettings>> function = new Function<PropertyDescriptor, Map.Entry<String, IdObjectFieldSettings>>() {
             @Nullable
             @Override
             public Map.Entry<String, IdObjectFieldSettings> apply(@Nullable PropertyDescriptor property) {
                 if (property == null) {
                     return null;
                 }
                 Method read = property.getReadMethod();
                 if (read == null) {
                     return null;
                 }
                 IdObjectFieldSettings preferences = read.getAnnotation(IdObjectFieldSettings.class);
                 if (preferences != null) {
                     return new AbstractMap.SimpleEntry<>(property.getName(), preferences);
                 }
                 return null;
             }
         };
 
         Map<String, IdObjectFieldSettings> fieldPreferencesMap = new HashMap<>();
         for (Map.Entry<String, IdObjectFieldSettings> field : traverseInterfaces(entityType, function)) {
             fieldPreferencesMap.put(field.getKey(), field.getValue());
         }
         return fieldPreferencesMap;
     }
 
     private <T extends IdObject, F> List<F> traverseInterfaces(Class<T> entityType, Function<PropertyDescriptor, F> function) {
         Class idObjectInterface = getIdObjectInterfaceForClass(entityType);
         List<F> traversalResults = new LinkedList<>();
         while (idObjectInterface != null) {
             List<PropertyDescriptor> propertyDescriptors = new LinkedList<>();
             Collections.addAll(propertyDescriptors, PropertyUtils.getPropertyDescriptors(idObjectInterface));
             Collection<PropertyDescriptor> filteredProperties = Collections2.filter(propertyDescriptors, new Predicate<PropertyDescriptor>() {
                 @Override
                 public boolean apply(@Nullable PropertyDescriptor property) {
                     if (property == null) {
                         return false;
                     }
                     Method read = property.getReadMethod();
                     return read != null && !"class".equals(read.getName()) && !read.isAnnotationPresent(Transient.class);
                 }
             });
             traversalResults.addAll(Collections2.transform(filteredProperties, function));
             final Class<?>[] interfaces = idObjectInterface.getInterfaces();
             if (interfaces != null && interfaces.length == 1) {
                 idObjectInterface = interfaces[0];
             } else {
                 idObjectInterface = null;
             }
         }
         return traversalResults;
     }
 }
