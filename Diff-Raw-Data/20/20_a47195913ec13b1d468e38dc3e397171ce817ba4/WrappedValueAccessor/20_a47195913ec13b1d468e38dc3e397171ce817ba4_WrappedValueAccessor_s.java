 package com.anasoft.os.daofusion.bitemporal;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Modifier;
 
 import org.joda.time.Interval;
 
 /**
  * {@link ValueAccessor} adapter for use with {@link BitemporalWrapper}.
  * 
  * @param <V> Value wrapped by the {@link Bitemporal} object.
  * @param <T> {@link BitemporalWrapper} implementation that wraps the given value type.
  * 
  * @see ValueAccessor
  * @see BitemporalWrapper
  * 
  * @author igor.mihalik
  */
 public abstract class WrappedValueAccessor<V, T extends BitemporalWrapper<V>> implements ValueAccessor<V, T> {
 
     /**
      * @see com.anasoft.os.daofusion.bitemporal.ValueAccessor#extractValue(com.anasoft.os.daofusion.bitemporal.Bitemporal)
      */
     public V extractValue(T bitemporalObject) {
         if (bitemporalObject == null)
             return null;
         return bitemporalObject.getValue();
     }
 
     /**
      * Creates an instance of {@link WrappedValueAccessor} for the given
      * {@link BitemporalWrapper} type.
      * 
      * <p>
      * 
      * Resulting instance implements the {@link ValueAccessor#wrapValue(Object, Interval)
      * wrapValue} method using the following algorithm:
      * 
      * <ol>
      *  <li>invoke the default no-argument constructor for the given
             {@link BitemporalWrapper} class (the constructor doesn't
             need to be public)
      *  <li>set the wrapped value and validity interval on newly created
      *      {@link BitemporalWrapper} instance
      *  <li>return the {@link BitemporalWrapper} instance as the result
      * </ol>
      */
     public static <V, T extends BitemporalWrapper<V>> WrappedValueAccessor<V, T> create(
             final Class<T> clazz) {
         return new WrappedValueAccessor<V, T>() {
 
             public T wrapValue(V value, Interval validityInterval) {
                 try {
                     Constructor<T> ctor = clazz.getDeclaredConstructor();
                     makeAccessible(ctor);
                     T newInstance = ctor.newInstance();
                     newInstance.setValue(value);
                     newInstance.setValidityInterval(validityInterval);
                     return newInstance;
                 } catch (Exception e) {
                     throw new RuntimeException(e);
                 }
             }
 
         };
     }
 
     private static <T> void makeAccessible(Constructor<T> ctor) {
         if ((!Modifier.isPublic(ctor.getModifiers()) || !Modifier.isPublic(ctor.getDeclaringClass().getModifiers()))
                 && !ctor.isAccessible()) {
             ctor.setAccessible(true);
         }
     }
 
 }
