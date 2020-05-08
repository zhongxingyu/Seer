 package org.terasoluna.gfw.web.view;
 
 import java.beans.PropertyDescriptor;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.springframework.beans.BeanUtils;
 import org.springframework.beans.BeanWrapper;
 import org.springframework.beans.PropertyAccessorFactory;
 import org.springframework.core.convert.TypeDescriptor;
 
 public class FormFunctions {
 
     public static List<String> getPaths(Object form) {
         if (form == null) {
             return Collections.emptyList();
         }
         List<String> paths = getPaths(form, null);
         return paths;
     }
 
     private static List<String> getPaths(Object bean, String pathPrefix) {
 
         BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);
         TypeDescriptor beanTypeDescriptor = TypeDescriptor.forObject(bean);
         List<String> paths = new ArrayList<String>();
 
         if (beanTypeDescriptor.isCollection() || beanTypeDescriptor.isArray()) {
             collectPathsOfCollection(paths, bean, pathPrefix, beanTypeDescriptor, beanWrapper);
             return paths;
 
         } else if (beanTypeDescriptor.isMap()) {
             collectPathsOfMap(paths, bean, pathPrefix);
             return paths;
 
         }
 
         PropertyDescriptor[] beanPropertyDescriptors = beanWrapper.getPropertyDescriptors();
 
         for (PropertyDescriptor beanPropertyDescriptor : beanPropertyDescriptors) {
             String propertyName = beanPropertyDescriptor.getName();
 
             if (!beanWrapper.isReadableProperty(propertyName) || !beanWrapper.isWritableProperty(propertyName)) {
                 continue;
             }
             Object value = beanWrapper.getPropertyValue(propertyName);
             if (value == null) {
                 continue;
             }
 
             String path = null;
             if (pathPrefix == null) {
                 path = propertyName;
             } else {
                 path = pathPrefix + "." + propertyName;
             }
 
             TypeDescriptor propertyTypeDescriptor = beanWrapper.getPropertyTypeDescriptor(propertyName);
             if (propertyTypeDescriptor.isCollection() || propertyTypeDescriptor.isArray()) {
                 collectPathsOfCollection(paths, value, path, propertyTypeDescriptor, beanWrapper);
 
             } else if (propertyTypeDescriptor.isMap()) {
                 collectPathsOfMap(paths, value, path);
 
             } else {
                 collectPathsObject(paths, value, path);
 
             }
 
         }
 
         return paths;
     }
 
     private static void collectPathsOfCollection(List<String> paths, Object value, String path,
             TypeDescriptor typeDescriptor, BeanWrapper beanWrapper) {
 
         Collection<?> collection;
         if (typeDescriptor.isCollection()) {
             collection = (Collection<?>) value;
         } else {
             Object[] array = beanWrapper.convertIfNecessary(value, Object[].class);
             collection = Arrays.asList(array);
         }
 
         int i = 0;
         for (Object elementValue : collection) {
            if (elementValue == null) {
                continue;
            }
            String collectionPath = path + "[" + i + "]";
            if (BeanUtils.isSimpleValueType(elementValue.getClass())) {
                paths.add(collectionPath);
            } else {
                List<String> nestedPaths = getPaths(elementValue, collectionPath);
                if (!nestedPaths.isEmpty()) {
                    paths.addAll(nestedPaths);
                 }
                i++;
             }
         }
     }
 
     private static void collectPathsOfMap(List<String> paths, Object value, String path) {
         Map<?, ?> map = (Map<?, ?>) value;
         for (Entry<?, ?> e : map.entrySet()) {
             if (e.getValue() == null) {
                 continue;
             }
             String mapPath = path + "[" + e.getKey() + "]";
             if (BeanUtils.isSimpleValueType(e.getValue().getClass())) {
                 paths.add(mapPath);
             } else {
                 List<String> nestedPaths = getPaths(e.getValue(), mapPath);
                 if (!nestedPaths.isEmpty()) {
                     paths.addAll(nestedPaths);
                 }
             }
         }
     }
 
     private static void collectPathsObject(List<String> paths, Object value, String path) {
         if (BeanUtils.isSimpleValueType(value.getClass())) {
             paths.add(path);
         } else {
             List<String> nestedPaths = getPaths(value, path);
             if (!nestedPaths.isEmpty()) {
                 paths.addAll(nestedPaths);
             }
         }
     }
 }
