 package org.testory;
 
 import java.lang.reflect.AccessibleObject;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 public class Samples {
   public static Object sample(Class<?> type, String name) {
     return type.isPrimitive() || wrappers.contains(type)
         ? samplePrimitive(type, name)
         : type.isEnum()
             ? type.getEnumConstants()[0]
             : type == String.class
                 ? name
                 : type == Class.class || AccessibleObject.class.isAssignableFrom(type)
                     ? sampleReflected(type, name)
                     : fail(type, name);
   }
 
  private static final HashSet<Class<?>> wrappers = new HashSet<Class<?>>(Arrays.asList(Void.class,
      Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class,
      Float.class, Double.class));
 
   private static Object samplePrimitive(Class<?> type, String name) {
     Map<Class<?>, Object> map = new HashMap<Class<?>, Object>();
 
     map.put(boolean.class, Boolean.FALSE);
     map.put(char.class, Character.valueOf((char) 0));
     map.put(byte.class, Byte.valueOf((byte) 0));
     map.put(short.class, Short.valueOf((short) 0));
     map.put(int.class, Integer.valueOf(0));
     map.put(long.class, Long.valueOf(0));
     map.put(float.class, Float.valueOf(0));
     map.put(double.class, Double.valueOf(0));
 
     map.put(Void.class, null);
     map.put(Boolean.class, Boolean.FALSE);
     map.put(Character.class, Character.valueOf((char) 0));
     map.put(Byte.class, Byte.valueOf((byte) 0));
     map.put(Short.class, Short.valueOf((short) 0));
     map.put(Integer.class, Integer.valueOf(0));
     map.put(Long.class, Long.valueOf(0));
     map.put(Float.class, Float.valueOf(0));
     map.put(Double.class, Double.valueOf(0));
 
     return map.get(type);
   }
 
   private static Object sampleReflected(Class<?> type, String name) {
     @SuppressWarnings("unused")
     class SampleClass {
       public Object sampleField;
 
       public void sampleMethod() {}
     }
     try {
       return type == Class.class
           ? SampleClass.class
           : type == Method.class
               ? SampleClass.class.getDeclaredMethod("sampleMethod")
               : type == Constructor.class
                   ? SampleClass.class.getDeclaredConstructor()
                   : type == Field.class
                       ? SampleClass.class.getDeclaredField("sampleField")
                       : fail(type, name);
     } catch (NoSuchMethodException e) {
       throw new Error(e);
     } catch (NoSuchFieldException e) {
       throw new Error(e);
     }
   }
 
   private static Object fail(Class<?> type, String name) {
     throw new IllegalArgumentException("failed creating sample: " + type.getSimpleName() + " "
         + name);
   }
 }
