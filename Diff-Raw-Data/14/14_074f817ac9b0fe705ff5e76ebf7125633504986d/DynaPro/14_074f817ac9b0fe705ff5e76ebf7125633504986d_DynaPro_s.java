 package org.cc.props;
 
 import org.cc.util.LogUtil;
 import org.slf4j.Logger;
 import org.springframework.beans.factory.BeanNameAware;
 import org.springframework.core.convert.ConversionService;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.Resource;
 import java.lang.reflect.ParameterizedType;
 
 /**
  * Dynamic place holder that can be updated at runtime.
  * Daneel Yaitskov
  */
public class DynaPro<T> implements BeanNameAware {
 
     private static final Logger logger = LogUtil.get();
 
     /**
      * Cached value.
      */
     private T value;
     private T defaultValue;
     private String propertyName;
     private String sourceValue;
 
     @Resource
     private PropertyExposer properties;
 
     @Resource
    private ConversionService conversionService;
 
     @Resource
     private DynaProSet propertySet;
 
     public synchronized String getSourceValue() {
         return sourceValue;
     }
 
     public synchronized T getDefaultValue() {
         return defaultValue;
     }
 
     public String getPropertyName() {
         return propertyName;
     }
 
     public synchronized void setDefaultValue(T defaultValue) {
         this.defaultValue = defaultValue;
     }
 
     /**
      * Returns current cached value.
      *
      * @return current cached value
      */
     public T get() {
         if (value == null) {
             try {
                 sourceValue = properties.get(propertyName);
                 logger.debug("source value '{}' for '{}'", sourceValue, propertyName);
                 value = conversionService.convert(sourceValue, getParameterClass());
             } catch (IllegalArgumentException e) {
                 if (defaultValue != null) {
                     value = defaultValue;
                     logger.info("use default value '{}' for property '{}'",
                             value, propertyName);
                 } else {
                     throw new IllegalArgumentException("there is no default value " +
                             "for property '" + propertyName + "'", e);
                 }
             }
         }
         return value;
     }
 
     @Override
     public void setBeanName(String name) {
         logger.debug("property name '{}'", name);
         propertyName = name;
     }
 
     @PostConstruct
     public void init() {
         logger.debug("getting initial converted value '{}' for '{}'", get(), propertyName);
         propertySet.add(this);
     }
 
     public Class<T> getParameterClass() {
         Object obj = ((ParameterizedType) getClass().getGenericSuperclass())
                 .getActualTypeArguments()[0];
         return (Class<T>) obj;
     }
 }
