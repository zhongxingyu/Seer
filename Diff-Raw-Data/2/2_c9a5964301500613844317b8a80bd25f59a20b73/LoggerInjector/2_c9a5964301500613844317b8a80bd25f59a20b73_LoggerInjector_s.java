 package com.crowdstore.common.logging;
 
 import com.crowdstore.common.annotations.InjectLogger;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.config.BeanPostProcessor;
 import org.springframework.stereotype.Component;
 import org.springframework.util.ReflectionUtils;
 
 import java.lang.reflect.Field;
 
 /**
  * @author fcamblor
  *         Utility class which will inject SLF4J into @Log annotated fields
  */
 @Component
 public class LoggerInjector implements BeanPostProcessor {
     public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
         return bean;
     }
 
     public Object postProcessBeforeInitialization(final Object bean, String beanName) throws BeansException {
         ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
             public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                 // make the field accessible if defined private
                 ReflectionUtils.makeAccessible(field);
                if (field.getAnnotation(InjectLogger.class) != null && field.getDeclaringClass().isAssignableFrom(Logger.class)) {
                     Logger log = LoggerFactory.getLogger(bean.getClass());
                     field.set(bean, log);
                 }
             }
         });
         return bean;
     }
 }
