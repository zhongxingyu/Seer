 package ca.hullabaloo.properties;
 
 import java.lang.reflect.ParameterizedType;
 
 /* Base class for simple type converstion */
 
 public abstract class BaseConverter<T> implements Converter {
     private final Class<T> targetType;
 
     @SuppressWarnings({"unchecked"})
     public BaseConverter() {
         ParameterizedType parameterizedType =
                 (ParameterizedType) getClass().getGenericSuperclass();
         targetType = (Class<T>) parameterizedType.getActualTypeArguments()[0];
     }
 
     public final boolean supportsType(Class<?> type) {
         return type == targetType;
     }
 
     /**
      * X must be a subtype of T
      */
     public final <X> X convert(Object object, Class<X> targetType) {
        return targetType.cast(convert(object));
     }
 
     protected abstract T convert(Object object);
 }
