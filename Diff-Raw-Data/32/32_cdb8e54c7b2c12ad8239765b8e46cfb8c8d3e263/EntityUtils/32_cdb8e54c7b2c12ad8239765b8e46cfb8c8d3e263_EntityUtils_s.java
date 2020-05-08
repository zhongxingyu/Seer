 package com.feedly.cassandra.entity;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.BitSet;
 
 import me.prettyprint.cassandra.serializers.CharSerializer;
 import me.prettyprint.cassandra.serializers.SerializerTypeInferer;
 import me.prettyprint.hector.api.Serializer;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.feedly.cassandra.entity.enhance.IEnhancedEntity;
 
 public class EntityUtils
 {
     private static final Logger _logger = LoggerFactory.getLogger(EntityUtils.class.getName());
     
     public static final boolean modifiersOk(Method method)
     {
         int m = method.getModifiers();
         return Modifier.isPublic(m) && !Modifier.isStatic(m)
                 && !Modifier.isAbstract(m);
     }
     
     public static final void initBitSet(Object o)
     {
         if(o instanceof IEnhancedEntity)
         {
             ((IEnhancedEntity) o).setModifiedFields(new BitSet());
         }
     }
     
     static boolean isBeanMethod(Method method)
     {
         if(!modifiersOk(method))
         {
             _logger.trace("invalid modifiers ", new Object[] { method.getModifiers(), ". excluding ", method.getName()});
             return false;
         }
         
         if(method.getName().length() <= 3)
         {
             _logger.trace("method name == get. cannot infer property name. excluding ", method.getName());
             return false;                
         }
             
         if(method.getDeclaringClass().getPackage().getName().startsWith("java."))
         {
             _logger.trace("method declared by java class. excluding ", method.getName());
             return false;
         }
         
         return true;
     }
     
     public static final boolean isValidSetter(Method setter)
     {
         if(!setter.getName().startsWith("set"))
         {
             _logger.trace("not a setter. excluding ", setter.getName());
             return false;                
         }
 
         if(!setter.getReturnType().equals(void.class))
         {
             _logger.trace("return value present. excluding ", setter.getName());
             return false;                
         }
 
         if(setter.getParameterTypes().length != 1)
         {
             _logger.trace("#parameters != 1. excluding ", setter.getName());
             return false;                
         }
 
         if(!isBeanMethod(setter))
             return false;        
 
         
         return true;
     }
     
     static boolean isValidGetter(Method getter)
     {            
         if(!getter.getName().startsWith("get"))
         {
             _logger.trace("not a getter. excluding ", getter.getName());
             return false;                
         }
 
         if(getter.getParameterTypes().length > 0)
         {
             _logger.trace("#parameters != 0. excluding ", getter.getName());
             return false;                
         }
         
         if(!isBeanMethod(getter))
             return false;
 
         return true;
     }
     
     public static Serializer<?> getSerializer(Class<?> type)
     {
         Serializer<?> s = SerializerTypeInferer.getSerializer(type);
         if(s == null && type.equals(char.class))
             s = CharSerializer.get();
         
         return s;
     }
 
     public static boolean nullSafeEquals(Object o1, Object o2)
     {
         if(o1 == o2)
             return true;
         
         if(o1 == null || o2 == null)
             return false;
         
         return o1.equals(o2);
     }
     
     //not efficient, should be used only in exceptional cases
     public static boolean beanFieldsEqual(Object o1, Object o2)
     {
         if(!o1.getClass().equals(o2.getClass()))
             return false;
         
         for(Field f : o1.getClass().getDeclaredFields())
         {
             try
             {
                 f.setAccessible(true);
                 if(!f.getName().equals("__modifiedFields") &&
                         !f.getName().equals("__unmappedModified") &&
                         !nullSafeEquals(f.get(o1), f.get(o2)))
                 {
                     return false;
                 }
             }
             catch(Exception e)
             {
                 return false;
             }
         }
             
         return true;
     }
 }
