 /**
  * ******************************************************************************************
  * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations (FAO). All rights
  * reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted
  * provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice,this list of conditions
  * and the following disclaimer. 2. Redistributions in binary form must reproduce the above
  * copyright notice,this list of conditions and the following disclaimer in the documentation and/or
  * other materials provided with the distribution. 3. Neither the name of FAO nor the names of its
  * contributors may be used to endorse or promote products derived from this software without
  * specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
  * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 package org.sola.common.mapping;
 
 import java.lang.reflect.Field;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import org.dozer.Mapper;
 import org.dozer.MappingException;
 import org.sola.common.SOLAException;
 import org.sola.common.messaging.ServiceMessage;
 
 /**
  * Basic utility class that provides some mapping related functions that may be useful to other
  * system functions.
  */
 public class MappingUtility {
 
     /**
      * Helper method that uses reflection to retrieve all fields from a class including public,
      * protected, private and inherited fields
      *
      * @param type The class type to retrieve the fields from
      * @return A list of all fields from the class.
      */
     public static List<Field> getAllFields(Class<?> type) {
         List<Field> fields = new ArrayList<Field>();
         for (Class<?> c = type; c != null; c = c.getSuperclass()) {
             fields.addAll(Arrays.asList(c.getDeclaredFields()));
         }
         return fields;
     }
 
     /**
      * Helper method that retrieves a specific field from the list of fields on the class based on
      * the field name
      *
      * @param fieldList The list of all fields on the class.
      * @param fieldName The name of the field to find in the list
      * @return The field matching the fieldName or null if the field is not in the list
      * @see #getAllFields(java.lang.Class)
      */
     public static Field getField(List<Field> fieldList, String fieldName) {
         Field result = null;
         for (Field field : fieldList) {
             if (field.getName().equalsIgnoreCase(fieldName)) {
                 result = field;
                 break;
             }
         }
         return result;
     }
 
     /**
      * Uses java generics to create a new ArrayList that can contain elements of the specified
      * elementClass. Calling a method appears to be the easiest way to achieve this.
      *
      * @param <T> Generic type parameter
      * @param elementClass The class of T
      * @return A new ArrayList that can contain elements of class elementClass.
      */
     public static <T> List<T> createList(Class<T> elementClass) {
         return new ArrayList<T>();
     }
 
     /**
      * Determines if a class is a primitive type or one of the boxed types that are considered
      * simple (e.g. String, Date, BigDecimal, Array, etc.
      *
      * @param checkClass The class to check
      * @return True if the class is a simple type
      */
     public static boolean isSimpleType(Class<?> checkClass) {
         return checkClass.isPrimitive()
                 || String.class.isAssignableFrom(checkClass)
                 || Date.class.isAssignableFrom(checkClass)
                 || BigDecimal.class.isAssignableFrom(checkClass)
                 || Boolean.class.isAssignableFrom(checkClass)
                 || Character.class.isAssignableFrom(checkClass)
                 || Integer.class.isAssignableFrom(checkClass)
                 || checkClass.isArray();
     }
 
     /**
      * Maps the simple fields on the srcObj to the destClass so that the resulting destination
      * object can be used for comparison in the destination list.
      *
      * @param <T> The generic type of the destination class
      * @param srcObj The source object being translated
      * @param destClass The class for the destination object
      * @return A destination object with only its simple (i.e. primitive and boxed fields) set based
      * on the values from the srcObj.
      * @see MappingUtility#getAllFields(java.lang.Class)
      * @see MappingUtility#isSimpleType(java.lang.Class)
      */
     private static <T> T mapSimpleFields(Object srcObj, Class<T> destClass) {
         T result = null;
         if (srcObj != null) {
             List<Field> srcFieldList = MappingUtility.getAllFields(srcObj.getClass());
             List<Field> destFieldList = MappingUtility.getAllFields(destClass);
             for (Field srcField : srcFieldList) {
                 if (MappingUtility.isSimpleType(srcField.getType())) {
                     Field destField = MappingUtility.getField(destFieldList, srcField.getName());
                     if (destField == null) {
                        break;
                     }
                     if (result == null) {
                         result = org.dozer.util.ReflectionUtils.newInstance(destClass);
                     }
                     try {
                         destField.setAccessible(true);
                         srcField.setAccessible(true);
                         destField.set(result, srcField.get(srcObj));
                     } catch (IllegalArgumentException ex1) {
                         throw new SOLAException(ServiceMessage.GENERAL_UNEXPECTED,
                                 new Object[]{"Argument exception", ex1});
                     } catch (IllegalAccessException ex2) {
                         throw new SOLAException(ServiceMessage.GENERAL_UNEXPECTED,
                                 new Object[]{"Access exception", ex2});
                     }
                 }
             }
         }
         return result;
     }
 
     /**
      * Translates a source list into the destination list. Checks each item in the source list to
      * determine if the item exists in the destination list. If so, the source item is translated
      * over the item in the destination list. If the source item does not exist in the destination
      * list, it is translated and added as a new item to the destination list. <p> Due to Java type
      * erasure, it is not possible to use reflection to determine the list class for the destination
      * list. The list class to use must be passed to this method as destClass.</p> 
      *
      * @param source The list to translate from.
      * @param destination The list to translate to
      * @param destClass The class of the items in the destination list
      * @param mapper The mapper to use to continue to translate the items in the list.
      * @return The destination list containing the items translated from the source list. If the
      * source list is null or empty, the result will be the destination list.
      * @throws SOLAException
      * @throws MappingException
      */
     public static <T> List<T> translateList(List source, List destination, Class<T> destClass, Mapper mapper)
             throws SOLAException, MappingException {
         if (source != null && source.size() > 0) {
             if (destClass == null) {
                 throw new SOLAException(ServiceMessage.GENERAL_UNEXPECTED,
                         new Object[]{"Unable to determine destination class type from source class "
                             + source.get(0).getClass().getName()});
             }
 
             boolean matchSrcObj = destination != null && destination.size() > 0;
             if (destination == null) {
                 destination = MappingUtility.createList(destClass);
             }
             for (Object srcObj : source) {
                int index = -1;
                 if (matchSrcObj) {
                     Object tempDestObj = mapSimpleFields(srcObj, destClass);
                    index = destination.indexOf(tempDestObj);
                 }
                if (index > -1) {
                    mapper.map(srcObj, destination.get(index));
                 } else {
                     destination.add(mapper.map(srcObj, destClass));
                 }
             }
         }
         return destination;
     }
 }
