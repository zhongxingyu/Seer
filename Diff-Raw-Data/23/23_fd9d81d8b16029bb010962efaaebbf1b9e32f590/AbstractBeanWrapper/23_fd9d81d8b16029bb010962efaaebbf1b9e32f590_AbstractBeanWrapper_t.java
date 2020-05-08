 package com.agileapes.couteau.reflection.beans.impl;
 
 import com.agileapes.couteau.reflection.beans.BeanWrapper;
 import com.agileapes.couteau.reflection.beans.WriteAccessorAware;
 import com.agileapes.couteau.reflection.error.NoSuchPropertyException;
 import com.agileapes.couteau.reflection.error.PropertyAccessException;
 import com.agileapes.couteau.reflection.error.PropertyTypeMismatchException;
 import com.agileapes.couteau.reflection.property.WritePropertyAccessor;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
  * @since 1.0 (7/9/13, 1:47 PM)
  */
 public abstract class AbstractBeanWrapper<E> extends AbstractBeanAccessor<E> implements BeanWrapper<E>, WriteAccessorAware {
     
     private final Map<String, WritePropertyAccessor<?>> writeAccessors = new HashMap<String, WritePropertyAccessor<?>>();
     
     public AbstractBeanWrapper(E bean) {
         super(bean);
         try {
             writeAccessors.putAll(getWriteAccessors());
         } catch (Exception ignored) {
         }
     }
 
     protected abstract Map<String, WritePropertyAccessor<?>> getWriteAccessors() throws Exception;
 
     @Override
     public boolean isWritable(String propertyName) throws NoSuchPropertyException {
         return writeAccessors.containsKey(propertyName);
     }
 
     @Override
     public boolean isReadable(String propertyName) throws NoSuchPropertyException {
         if (!hasProperty(propertyName)) {
             throw new NoSuchPropertyException(getBeanType(), propertyName);
         }
         return super.hasProperty(propertyName);
     }
 
     @Override
     public void setPropertyValue(String propertyName, Object propertyValue) throws NoSuchPropertyException, PropertyTypeMismatchException, PropertyAccessException {
         final WritePropertyAccessor accessor = propertyValue == null ? getPropertyWriter(propertyName) : getPropertyWriter(propertyName, propertyValue.getClass());
         //noinspection unchecked
         accessor.setPropertyValue(propertyValue);
     }
 
     @Override
     public WritePropertyAccessor<?> getPropertyWriter(String propertyName) throws NoSuchPropertyException {
         return getPropertyWriter(propertyName, Object.class);
     }
 
     @Override
     public <T> WritePropertyAccessor<T> getPropertyWriter(String propertyName, Class<T> propertyType) throws NoSuchPropertyException {
         if (!hasProperty(propertyName)) {
             throw new NoSuchPropertyException(getBeanType(), propertyName);
         }
         final WritePropertyAccessor<?> accessor = writeAccessors.get(propertyName);
        final Class<?> actualType = accessor.getPropertyType().isPrimitive() ? primitives.get(accessor.getPropertyType()) : accessor.getPropertyType();
        if  (propertyType.isAssignableFrom(actualType)) {
             //noinspection unchecked
             return (WritePropertyAccessor<T>) accessor;
         }
         throw new NoSuchPropertyException(getBeanType(), propertyName, propertyType);
     }
 
     @Override
     public boolean hasProperty(String propertyName) {
         return super.hasProperty(propertyName) || writeAccessors.containsKey(propertyName);
     }
 
     @Override
     public Set<String> getPropertyNames() {
         final HashSet<String> propertyNames = new HashSet<String>(super.getPropertyNames());
         propertyNames.addAll(writeAccessors.keySet());
         return propertyNames;
     }
 
     @Override
     public Class<?> getPropertyType(String propertyName) throws NoSuchPropertyException {
         if (writeAccessors.containsKey(propertyName)) {
             return getPropertyWriter(propertyName).getPropertyType();
         }
         return super.getPropertyType(propertyName);
     }
 }
