 package com.agileapes.couteau.reflection.convert.impl;
 
 import com.agileapes.couteau.reflection.beans.BeanAccessor;
 import com.agileapes.couteau.reflection.beans.BeanAccessorFactory;
 import com.agileapes.couteau.reflection.beans.BeanWrapper;
 import com.agileapes.couteau.reflection.beans.BeanWrapperFactory;
 import com.agileapes.couteau.reflection.convert.ConversionDecision;
 import com.agileapes.couteau.reflection.convert.ConversionStrategy;
 import com.agileapes.couteau.reflection.error.*;
 import com.agileapes.couteau.reflection.property.impl.SimplePropertyDescriptor;
 
 /**
  * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
  * @since 1.0 (7/13/13, 7:02 AM)
  */
 public class DefaultBeanConverter extends AbstractCachingBeanConverter {
 
     public DefaultBeanConverter() {
         super();
     }
 
     public DefaultBeanConverter(ConversionStrategy conversionStrategy) {
         super(conversionStrategy);
     }
 
     public DefaultBeanConverter(BeanWrapperFactory wrapperFactory, ConversionStrategy conversionStrategy) {
         super(wrapperFactory, conversionStrategy);
     }
 
     public DefaultBeanConverter(BeanAccessorFactory accessorFactory, BeanWrapperFactory wrapperFactory, ConversionStrategy conversionStrategy) {
         super(accessorFactory, wrapperFactory, conversionStrategy);
     }
 
     @Override
     protected void doConvert(BeanAccessor<?> source, BeanWrapper<?> target) throws BeanConversionException {
         for (String property : source.getPropertyNames()) {
             final Object propertyValue;
             try {
                 propertyValue = source.getPropertyValue(property);
             } catch (Exception e) {
                 throw new FatalBeanConversionException("Failed to read property value: " + source.getBeanType().getCanonicalName().concat(".").concat(property), e);
             }
             if (!target.hasProperty(property)) {
                 continue;
             }
             try {
                 if (!target.isWritable(property)) {
                     continue;
                 }
             } catch (NoSuchPropertyException ignored) {
             }
             Class<?> propertyType = null;
             try {
                 propertyType = target.getPropertyType(property);
             } catch (NoSuchPropertyException ignored) {
             }
             if (propertyType == null) {
                 try {
                     propertyType = source.getPropertyType(property);
                 } catch (NoSuchPropertyException ignored) {
                 }
             }
             Object convertedValue;
            SimplePropertyDescriptor propertyDescriptor = null;
             try {
                propertyDescriptor = new SimplePropertyDescriptor(property, propertyType, target.getGenericPropertyType(property), propertyValue);
             } catch (NoSuchPropertyException ignored) {
             }
            final ConversionDecision decision = getConversionStrategy().decide(propertyDescriptor);
             if (decision.equals(ConversionDecision.IGNORE)) {
                 continue;
             } else {
                convertedValue = convertProperty(propertyDescriptor, decision);
             }
             try {
                 target.setPropertyValue(property, convertedValue);
             } catch (NoSuchPropertyException ignored) {
             } catch (PropertyAccessException e) {
                 throw new FatalBeanConversionException("Failed to access property " + target.getBeanType().getCanonicalName().concat(".").concat(property));
             } catch (PropertyTypeMismatchException ignored) {
             }
         }
 
     }
 
 }
