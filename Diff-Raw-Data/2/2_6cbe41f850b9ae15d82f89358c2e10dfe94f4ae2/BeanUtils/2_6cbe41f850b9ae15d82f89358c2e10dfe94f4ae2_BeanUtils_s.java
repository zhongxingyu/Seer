 package com.arthur.auth.utils;
 
 import java.lang.reflect.Field;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class BeanUtils {
 	
 	private static final Logger LOGGER = LoggerFactory.getLogger(BeanUtils.class);
 	
 	public static Object safeGetFieldValue(Object bean, String fieldName){
 		try {
 			Class<?> clazz = bean.getClass();
 			Field field = clazz.getField(fieldName);
 			Class<?> fieldClass = field.getClass();
 			field.setAccessible(true);
 			Object value = field.get(bean);
 			
			if(null == value){
 				return value;
 			}else{
 				return fieldClass.newInstance();
 			}
 		} catch (Exception e) {
 			LOGGER.error("Get {} value from {} error", fieldName, bean, e);
 			throw new RuntimeException(e);
 		} 
 	}
 
 }
