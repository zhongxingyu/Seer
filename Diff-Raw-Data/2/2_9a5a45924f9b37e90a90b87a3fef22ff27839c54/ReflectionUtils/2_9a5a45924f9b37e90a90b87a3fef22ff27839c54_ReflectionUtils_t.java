 /*
  * Copyright 2011-2013 StackFrame, LLC
  *
  * This is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3
  * as published by the Free Software Foundation.
  *
  * You should have received a copy of the GNU General Public License
  * along with this file.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.stackframe.reflect;
 
 import com.google.common.base.Predicate;
 import java.beans.IntrospectionException;
 import java.beans.Introspector;
 import java.beans.PropertyDescriptor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  * Useful utilities that leverage reflection.
  *
  * @author Gene McCulley
  */
 public class ReflectionUtils {
 
     private ReflectionUtils() {
         // Inhibit construction as this is a utility class.
     }
 
     private static Map<String, Object> toString(Class c, Object o) {
         Map<String, Object> map = new TreeMap<String, Object>();
         Field[] fields = c.getDeclaredFields();
         for (Field field : fields) {
             if (field.isSynthetic()) {
                 continue;
             }
 
             if (!field.isAccessible()) {
                 field.setAccessible(true);
             }
 
             try {
                 map.put(field.getName(), field.get(o));
             } catch (IllegalAccessException iae) {
                 throw new AssertionError(iae);
             }
         }
 
         Class superclass = c.getSuperclass();
         if (superclass != null) {
             map.putAll(toString(superclass, o));
         }
 
         Class[] interfaces = c.getInterfaces();
         for (Class iface : interfaces) {
             map.putAll(toString(iface, o));
         }
 
         return map;
     }
 
     /**
      * Print out all of the fields in an object. This is useful as a debugging
      * implementation of toString() that classes can delegate to.
      *
      * @param o the Object to print
      * @return a String that contains all of the field names and their values
      */
     public static String toString(Object o) {
         return toString(o.getClass(), o).toString();
     }
 
     private static PropertyDescriptor propertyDescriptor(Class c, String property) throws IntrospectionException {
         PropertyDescriptor[] pds = Introspector.getBeanInfo(c).getPropertyDescriptors();
         for (PropertyDescriptor pd : pds) {
             if (pd.getName().equals(property)) {
                 return pd;
             }
         }
 
         return null;
     }
 
     /**
      * Given a simple boolean property on a class, make a Predicate which invokes it.
      *
      * @param <T> The JavaBeans type to look up the property on
     * @param c the Class of the JavaBeans type, constrained to T
      * @param property the property name
      * @return a Predicate which invokes the getter for the specified property
      */
     public static <T> Predicate<T> predicateForProperty(Class<T> c, String property) {
         try {
             PropertyDescriptor pd = propertyDescriptor(c, property);
             if (pd == null) {
                 throw new IllegalArgumentException(String.format("property '%s' does not exist on class '%s'", property, c.getName()));
             }
 
             assert pd.getPropertyType() == boolean.class;
             final Method getter = pd.getReadMethod();
             assert getter.getParameterTypes().length == 0;
             return new Predicate<T>() {
                 @Override
                 public boolean apply(T t) {
                     try {
                         return (Boolean) getter.invoke(t, (Object[]) null);
                     } catch (Exception e) {
                         throw new RuntimeException(e);
                     }
                 }
             };
         } catch (IntrospectionException ie) {
             throw new AssertionError(ie);
         }
     }
 }
