 package com.deepmock;
 
 import com.deepmock.reflect.PrivateAccessor;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.BeanUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.util.ReflectionUtils;
 
 import javax.annotation.Resource;
 import java.beans.PropertyDescriptor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 public class InjectionHelper {
     private static Log LOG = LogFactory.getLog(InjectionHelper.class);
 
     public static List<Field> getInjectableFields(Object target) {
         List<Field> injectableFields = new ArrayList<Field>();
         injectableFields.addAll(getAutowiredFields(target));
         injectableFields.addAll(getFieldsForSetters(target));
         return injectableFields;
     }
 
     public static void injectFieldByType(Object object, Object fieldValue) {
         List<Field> injectableFields = InjectionHelper.getInjectableFields(object);
         for (Field injectableField : injectableFields) {
            if (((Class)injectableField.getGenericType()).isAssignableFrom(fieldValue.getClass())) {
                 PrivateAccessor.setField(object, injectableField, fieldValue);
                 return;
             }
         }
     }
 
     private static List<Field> getAutowiredFields(Object target) {
         final List<Field> fields = new ArrayList<Field>();
         ReflectionUtils.doWithFields(target.getClass(), new ReflectionUtils.FieldCallback() {
             @Override
             public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                 if (field.isAnnotationPresent(Resource.class) || field.isAnnotationPresent(Autowired.class)) {
                     fields.add(field);
                 }
             }
         });
         return fields;
     }
 
     private static List<Field> getFieldsForSetters(Object target) {
         List<Field> fields = new ArrayList<Field>();
         PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(target.getClass());
         for (PropertyDescriptor descriptor : descriptors) {
             addDescriptorField(target, descriptor, fields);
         }
         return fields;
     }
 
     private static void addDescriptorField(Object target, PropertyDescriptor descriptor, List<Field> fields) {
         Method writeMethod = descriptor.getWriteMethod();
         if (writeMethod != null) {
             Field field = getField(target, descriptor);
             if (field != null) {
                 fields.add(field);
             } else {
                 LOG.debug("Could not find field to inject mock into for write method: " + writeMethod);
             }
         }
     }
 
     private static Field getField(Object target, PropertyDescriptor descriptor) {
         Class<?> fieldType = descriptor.getPropertyType();
         // If you have strange field naming conventions, you can add your field name to the possibleFieldNames
         String[] possibleFieldNames = new String[] {descriptor.getName()};
         Field field = getFieldByNameAndType(target, fieldType, possibleFieldNames);
         if (field == null) {
             field = ReflectionUtils.findField(target.getClass(), null, fieldType);
         }
         return field;
     }
 
     private static Field getFieldByNameAndType(Object target, Class<?> fieldType, String... possibleFieldNames) {
         for (String fieldName : possibleFieldNames) {
             Field field = ReflectionUtils.findField(target.getClass(), fieldName, fieldType);
             if (field != null) {
                 return field;
             }
         }
         return null;
     }
 }
