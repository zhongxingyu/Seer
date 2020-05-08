 /*
  * Copyright 2012 Eng Kam Hon (kamhon@gmail.com)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.kamhon.ieagle.util;
 
 import java.beans.PropertyDescriptor;
 import java.lang.reflect.Method;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import net.kamhon.ieagle.FrameworkConst;
 import net.kamhon.ieagle.exception.DataException;
 
 import org.apache.commons.beanutils.NestedNullException;
 import org.apache.commons.beanutils.PropertyUtils;
 import org.apache.commons.lang3.reflect.MethodUtils;
 import org.springframework.beans.BeanWrapper;
 import org.springframework.beans.BeanWrapperImpl;
 
 import com.opensymphony.xwork2.conversion.annotations.TypeConversion;
 
 public class CollectionUtil {
 
     private CollectionUtil() {
     }
 
     /**
      * true if collection is null or empty.
      * 
      * @param collection
      */
     public static boolean isEmpty(Collection<?> collection) {
         return collection == null || collection.isEmpty();
     }
 
     /**
      * true if collection is not null and not empty.
      * 
      * @param collection
      * @return
      */
     public static boolean isNotEmpty(Collection<?> collection) {
         return collection != null && !collection.isEmpty();
     }
 
     /**
      * Remove duplicate object base on hashcode
      * 
      * @param retList
      */
     public static <T> List<T> removeDuplicateInList(List<T> retList) {
         Set<T> set = new HashSet<T>(retList);
         retList = new ArrayList<T>(set);
         return retList;
     }
 
     /**
      * Convert Arrays to HashSet
      * 
      * @param <T>
      * @param objects
      * @return
      */
     public static <T> Set<T> toSet(T... objects) {
         List<T> list = Arrays.asList(objects);
 
         return new HashSet<T>(list);
     }
 
     /**
      * Concatenates two arrays.
      * 
      * @param array1
      *            - first array
      * @param array2
      *            - second array
      * @param <T>
      *            - object class
      * @return array contatenation
      */
     public static <T> T[] concatenate2Arrays(T[] array1, T... array2) {
         List<T> result = new ArrayList<T>();
         result.addAll(Arrays.asList(array1));
         result.addAll(Arrays.asList(array2));
 
         return result.toArray(array1);
     }
 
     /**
      * This function used to convert particular properties in Object to array.
      * 
      * @param objects
      * @param propertiesNames
      * @param isSetNullToEmptyString
      * @return
      */
     public static Object[][] to2DArray(List<?> objects, String[] propertiesNames, boolean isSetNullToEmptyString) {
         if (isEmpty(objects)) {
             return new Object[0][0];
         }
 
         List<Object[]> result = new ArrayList<Object[]>();
         for (Object object : objects) {
             List<Object> record = new ArrayList<Object>();
 
             for (String propertyName : propertiesNames) {
                 try {
                     Object propValue = PropertyUtils.getProperty(object, propertyName);
                     record.add(propValue == null ? "" : propValue);
                 } catch (NestedNullException ex) {
                     // if nested bean referenced is null
                     record.add("");
                 } catch (Exception ex) {
                     throw new DataException(ex);
                 }
             }
 
             result.add(record.toArray(new Object[0]));
         }
 
         return result.toArray(new Object[1][1]);
     }
 
     /**
      * This function used to convert particular properties in Object to array. This function mainly used to response
      * Client in JSON object
      * 
      * 
      * @param locale
      * @param objects
      * @param propertiesNames
      * @param isSetNullToEmptyString
      * @return
      */
     public static Object[][] to2DArrayForStruts(Locale locale, List<?> objects, String[] propertiesNames, boolean isSetNullToEmptyString) {
         String dateTimeFormat = "";
 
         if (isEmpty(objects)) {
             return new Object[0][0];
         }
 
         List<Object[]> result = new ArrayList<Object[]>();
         for (Object object : objects) {
             List<Object> record = new ArrayList<Object>();
 
             for (String propertyName : propertiesNames) {
                 boolean hasResult = false;
                 Object propValue = null;
 
                 try {
                     propValue = PropertyUtils.getProperty(object, propertyName);
                     processPropertiesForStruts(object, record, propertyName, propValue);
                    hasResult = true;
                 } catch (NestedNullException ex) {
                     // if nested bean referenced is null
                     record.add("");
                     hasResult = true;
                 } catch (Exception ex) {
                     if (locale == null)
                         throw new DataException(ex);
                 }
 
                 if (hasResult == false && propValue == null && locale != null) {
                     try {
                         String methodName = "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
                         propValue = MethodUtils.invokeExactMethod(object, methodName, locale);
                         processPropertiesForStruts(object, record, propertyName, propValue);
                     } catch (Exception ex) {
                         throw new DataException(ex);
                     }
                 }
             }
 
             result.add(record.toArray(new Object[0]));
         }
 
         return result.toArray(new Object[1][1]);
     }
 
     private static void processPropertiesForStruts(Object object, List<Object> record, String propertyName, Object propValue) {
         String dateTimeFormat;
         if (propValue instanceof Date) {
             SimpleDateFormat sdf = new SimpleDateFormat(FrameworkConst.DEFAULT_JSON_DATETIME_24_FORMAT);
             dateTimeFormat = sdf.format(propValue);
 
             record.add(dateTimeFormat);
         } else if (propValue instanceof Number) {
             record.add(processForNumber(object, propertyName, propValue));
         } else {
             record.add(propValue == null ? "" : propValue);
         }
     }
 
     private static Object processForNumber(Object object, String propertyName, Object propValue) {
         BeanWrapper wrapper = new BeanWrapperImpl(object);
         PropertyDescriptor descriptor = wrapper.getPropertyDescriptor(propertyName);
         Method method = descriptor.getReadMethod();
         TypeConversion typeConversion = method.getAnnotation(TypeConversion.class);
         if (typeConversion != null) {
             String convertor = typeConversion.converter();
             if (convertor.equalsIgnoreCase(FrameworkConst.STRUTS_DECIMAL_CONVERTER)) {
                 DecimalFormat df = new DecimalFormat(FrameworkConst.DEFAULT_DECIMAL_FORMAT);
                 return df.format(propValue);
             } else {
                 return propValue;
             }
         } else {
             if (propValue instanceof Double || propValue instanceof Float) {
                 DecimalFormat df = new DecimalFormat(FrameworkConst.DEFAULT_DECIMAL_FORMAT);
                 return df.format(propValue);
             } else {
                 return propValue;
             }
         }
     }
 
     /**
      * Make the log more meanings
      * 
      * @param <K>
      * @param <V>
      * @param map
      * @return
      */
     public static <K, V> String toLog(Map<K, V> map) {
         // using StringBuffer instead of String because expect there are many append operation
         StringBuffer sb = new StringBuffer();
 
         if (map == null) {
             return null;
         }
         if (map.isEmpty()) {
             return map.toString();
         }
 
         sb.append("{");
 
         for (Iterator<K> iterator = map.keySet().iterator(); iterator.hasNext();) {
             K key = iterator.next();
             Object value = map.get(key);
             sb.append(key).append("=");
             sb.append(toString4Log(value));
             if (iterator.hasNext()) {
                 sb.append(", ");
             }
         }
 
         sb.append("}");
 
         return sb.toString();
     }
 
     public static <E> String toLog(Collection<E> collec) {
         return toLog("", collec);
     }
 
     /**
      * Make the log more meanings
      * 
      * @param <E>
      * @param collec
      * @return
      */
     public static <E> String toLog(String separator, Collection<E> collec) {
         // using StringBuffer instead of String because expect there are many append operation
         StringBuffer sb = new StringBuffer();
 
         if (collec == null) {
             return null;
         }
         if (collec.isEmpty()) {
             return collec.toString();
         }
         sb.append("[");
         for (Iterator<E> iterator = collec.iterator(); iterator.hasNext();) {
             E value = iterator.next();
             sb.append(separator).append(toString4Log(value)).append(separator);
             if (iterator.hasNext()) {
                 sb.append(", ");
             }
         }
 
         sb.append("]");
         return sb.toString();
     }
 
     public static <E> String toLog(E... array) {
         return toLog("", array);
     }
 
     public static <E> String toLog(String separator, E... array) {
         if (array != null)
             return toLog(separator, Arrays.asList(array));
         else
             return null;
     }
 
     private static String toString4Log(Object value) {
         if (value == null) {
             return null;
         } else if (value instanceof String) {
             return (String) value;
         } else if (value instanceof Short) {
             return "" + ((Short) value).shortValue();
         } else if (value instanceof Integer) {
             return "" + ((Integer) value).intValue();
         } else if (value instanceof Long) {
             return "" + ((Long) value).longValue();
         } else if (value instanceof Float) {
             return "" + ((Float) value).floatValue();
         } else if (value instanceof Double) {
             return "" + ((Double) value).doubleValue();
         } else if (value instanceof Object[]) {
             Object[] objs = (Object[]) value;
             StringBuffer sb = new StringBuffer();
             sb.append("[");
             for (int i = 0; i < objs.length; i++) {
                 sb.append(toString4Log(objs[i]));
 
                 if (i != objs.length - 1) {
                     sb.append(", ");
                 }
             }
             sb.append("]");
             return sb.toString();
         } else if (value instanceof Iterator) {
             StringBuffer sb = new StringBuffer();
             sb.append("[");
             for (Iterator<?> i = (Iterator<?>) value; i.hasNext();) {
                 sb.append(toString4Log(i.next()));
                 if (i.hasNext()) {
                     sb.append(", ");
                 }
             }
             sb.append("]");
             return sb.toString();
         } else
             return value.toString();
     }
 
     public static <T> T getFirst(Collection<T> collection) {
         if (isNotEmpty(collection))
             return collection.iterator().next();
         else
             return null;
     }
 }
