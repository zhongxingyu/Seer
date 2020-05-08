 package org.n3r.core.lang;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 
 public class RField {
 
     public static boolean isNotNormal(Field field) {
         return field.getName().startsWith("$") || field.getName().equals("serialVersionUID");
     }
 
    //Returns generic type of any field
    public static Class<?> getFieldGenericType(Type genericType) {
         Class<? extends Type> genericTypeClass = genericType.getClass();
         if (!ParameterizedType.class.isAssignableFrom(genericTypeClass)) return Void.class;
 
         ParameterizedType parameterizedType = (ParameterizedType) genericType;
        return (Class<?>) parameterizedType.getActualTypeArguments()[0];
     }
 }
