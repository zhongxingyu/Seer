 package com.agileapes.couteau.reflection.beans.impl;
 
 import com.agileapes.couteau.reflection.beans.BeanAccessor;
 import com.agileapes.couteau.reflection.beans.ReadAccessorAware;
 import com.agileapes.couteau.reflection.error.NoSuchPropertyException;
 import com.agileapes.couteau.reflection.error.PropertyAccessException;
 import com.agileapes.couteau.reflection.property.ReadPropertyAccessor;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
  * @since 1.0 (7/9/13, 12:51 PM)
  */
 public abstract class AbstractBeanAccessor<E> implements BeanAccessor<E>, ReadAccessorAware {
 
    protected final static Map<Class<?>, Class<?>> primitives = new HashMap<Class<?>, Class<?>>();
    static {
        primitives.put(int.class, Integer.class);
        primitives.put(float.class, Float.class);
        primitives.put(double.class, Double.class);
        primitives.put(long.class, Long.class);
        primitives.put(short.class, Short.class);
        primitives.put(char.class, Character.class);
        primitives.put(boolean.class, Boolean.class);
    }

     private final Map<String, ReadPropertyAccessor<?>> accessors = new HashMap<String, ReadPropertyAccessor<?>>();
     private final E bean;
     private final Class<E> beanType;
 
     public AbstractBeanAccessor(final E bean) {
         this.bean = bean;
         //noinspection unchecked
         this.beanType = (Class<E>) bean.getClass();
         try {
             this.accessors.putAll(getReadAccessors());
         } catch (Exception ignored) {
         }
     }
     
     protected abstract Map<String, ReadPropertyAccessor<?>> getReadAccessors() throws Exception;
 
     @Override
     public Object getPropertyValue(String propertyName) throws NoSuchPropertyException, PropertyAccessException {
         return getPropertyValue(propertyName, Object.class);
     }
 
     @Override
     public <P> P getPropertyValue(String propertyName, Class<P> propertyType) throws NoSuchPropertyException, PropertyAccessException {
         return getPropertyReader(propertyName, propertyType).getPropertyValue();
     }
 
     @Override
     public E getBean() {
         return bean;
     }
 
     @Override
     public Class<E> getBeanType() {
         return beanType;
     }
 
     @Override
     public Set<String> getPropertyNames() {
         return Collections.unmodifiableSet(accessors.keySet());
     }
 
     @Override
     public Class<?> getPropertyType(String propertyName) throws NoSuchPropertyException {
         return getPropertyReader(propertyName).getPropertyType();
     }
 
     @Override
     public boolean hasProperty(String propertyName) {
         return accessors.containsKey(propertyName);
     }
 
     @Override
     public ReadPropertyAccessor<?> getPropertyReader(String propertyName) throws NoSuchPropertyException {
         return getPropertyReader(propertyName, Object.class);
     }
 
     @Override
     public <T> ReadPropertyAccessor<T> getPropertyReader(String propertyName, Class<T> propertyType) throws NoSuchPropertyException {
         if (!hasProperty(propertyName)) {
             throw new NoSuchPropertyException(getBeanType(), propertyName);
         }
         final ReadPropertyAccessor<?> accessor = accessors.get(propertyName);
        final Class<?> actualType = accessor.getPropertyType().isPrimitive() ? primitives.get(accessor.getPropertyType()) : accessor.getPropertyType();
        if  (propertyType.isAssignableFrom(actualType)) {
             //noinspection unchecked
             return (ReadPropertyAccessor<T>) accessor;
         }
         throw new NoSuchPropertyException(getBeanType(), propertyName, propertyType);
     }
 
 }
