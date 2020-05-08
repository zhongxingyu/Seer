 package com.bytedistillers.payment.common.form;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 public class FormDataMergeUtil<T extends FormData> {
 
  public T merge(T dynamicData, T staticData, Class<T> clazz) {
    T result = createNewInstance(clazz);
 
     Field[] fields = clazz.getDeclaredFields();
     for (Field field : fields) {
 
       if (field.getType().isAssignableFrom(Map.class)) {
         Map dynamicSource = getFieldValue(field, dynamicData, Map.class);
         Map staticSource = getFieldValue(field, staticData, Map.class);
 
         Map mergedSource = new HashMap(staticSource);
         mergedSource.putAll(dynamicSource);
         setFieldValue(result, field, mergedSource);
       } else if (field.getType().isAssignableFrom(Collection.class)) {
         Collection<?> dynamicSource = getFieldValue(field, dynamicData, Collection.class);
         Collection<?> staticSource = getFieldValue(field, staticData, Collection.class);
 
         Collection mergedSource = new ArrayList(staticSource);
         mergedSource.addAll(staticSource);
         setFieldValue(result, field, mergedSource);
       } else {
         Object value = getFieldValue(field, dynamicData, staticData);
         if (value != null) {
           setFieldValue(result, field, value);
         }
       }
     }
 
     return result;
   }
 
   @SuppressWarnings("unchecked")
   private <FT> FT getFieldValue(Field field, T data, Class<FT> dataClass) {
     boolean accessible = field.isAccessible();
     if (!accessible) {
       field.setAccessible(true);
     }
 
     try {
       FT value = (FT) field.get(data);
       return value;
     } catch (Exception e) {
       e.printStackTrace();
       return null;
     }
   }
 
  private T createNewInstance(Class<T> clazz) {
     try {
      T result = clazz.newInstance();
       return result;
     } catch (Exception e) {
       throw new RuntimeException(e.getMessage(), e);
     }
   }
 
   private Object getFieldValue(Field field, T dynamicData, T staticData) {
     boolean accessible = field.isAccessible();
     if (!accessible) {
       field.setAccessible(true);
     }
     try {
       Object valueObject = field.get(dynamicData);
       if (valueObject == null) {
         valueObject = field.get(staticData);
       }
 
       field.setAccessible(accessible);
 
       return valueObject;
     } catch (Exception e) {
       e.printStackTrace();
       return null;
     }
   }
 
   private void setFieldValue(T target, Field field, Object value) {
     try {
       boolean accessible = field.isAccessible();
       if (!accessible) {
         field.setAccessible(true);
       }
 
       field.set(target, value);
 
       if (!accessible) {
         field.setAccessible(accessible);
       }
     } catch (IllegalArgumentException e) {
       e.printStackTrace();
     } catch (IllegalAccessException e) {
       e.printStackTrace();
     }
   }
 
 }
