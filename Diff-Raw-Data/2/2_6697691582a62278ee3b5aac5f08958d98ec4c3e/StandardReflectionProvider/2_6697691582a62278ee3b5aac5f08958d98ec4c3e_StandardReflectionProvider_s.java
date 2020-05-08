 package info.freelibrary.sodbox.impl;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 
 public class StandardReflectionProvider implements ReflectionProvider {
 
 	private final static Logger LOGGER = LoggerFactory.getLogger(StandardReflectionProvider.class);
 	
     @SuppressWarnings("unchecked")
 	static final Class[] defaultConstructorProfile = new Class[0];
 
     @SuppressWarnings("unchecked")
 	public Constructor getDefaultConstructor(Class cls) throws Exception {
     	Constructor constructor = cls.getDeclaredConstructor(defaultConstructorProfile);
     	
     	if (LOGGER.isDebugEnabled()) {
    		LOGGER.debug(constructor.getClass().getName());
     	}
 
         return constructor;
     }
 
     public void setInt(Field field, Object object, int value) throws Exception { 
         field.setInt(object, value);
     }
 
     public void setLong(Field field, Object object, long value) throws Exception { 
         field.setLong(object, value);
     }
 
     public void setShort(Field field, Object object, short value) throws Exception { 
         field.setShort(object, value);
     }
 
     public void setChar(Field field, Object object, char value) throws Exception { 
         field.setChar(object, value);
     }
 
     public void setByte(Field field, Object object, byte value) throws Exception { 
         field.setByte(object, value);
     }
 
     public void setFloat(Field field, Object object, float value) throws Exception { 
         field.setFloat(object, value);
     }
 
     public void setDouble(Field field, Object object, double value) throws Exception { 
         field.setDouble(object, value);
     }
 
     public void setBoolean(Field field, Object object, boolean value) throws Exception { 
         field.setBoolean(object, value);
     }
 
     public void set(Field field, Object object, Object value) throws Exception { 
         field.set(object, value);
     }
 }
