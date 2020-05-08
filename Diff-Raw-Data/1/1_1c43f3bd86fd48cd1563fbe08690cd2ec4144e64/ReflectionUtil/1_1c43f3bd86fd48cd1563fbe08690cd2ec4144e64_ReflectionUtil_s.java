 package core.util;
 
 import java.beans.PropertyDescriptor;
 import java.util.Map;
 
 /**
  * Helper reflection methods.
  * 
  * @author jani
  * 
  */
 public class ReflectionUtil {
 
 	/**
 	 * Gets the value of a property.
 	 * 
 	 * @param <T>
 	 * @param object
 	 * @param propName
 	 * @return Value of a property.
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T> T getPropertyValue(Object object, String propName) {
 		try {
 			return (T) new PropertyDescriptor(propName, object.getClass())
 					.getReadMethod().invoke(object);
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	/**
 	 * Gets the class of a property.
 	 * 
 	 * @param object
 	 * @param propName
 	 * @return {@link Class}
 	 */
 	public static Class<?> getPropertyClass(Object object, String propName) {
 		try {
 			return new PropertyDescriptor(propName, object.getClass())
 					.getPropertyType();
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	/**
 	 * Sets the value of a property.
 	 * 
 	 * @param object
 	 * @param propName
 	 * @param value
 	 */
 	public static void setPropertyValue(Object object, String propName,
 			Object value) {
 		try {
 			PropertyDescriptor pd = new PropertyDescriptor(propName,
 					object.getClass());
 			pd.getWriteMethod().invoke(object, value);
 		} catch (Exception e) {
 			// nothing
 		}
 	}
 
 	/**
 	 * Sets a list of properties with a certain values. Only updates property if a values
 	 * has been changed, by comparing against the corresponding value in "oldValues".
 	 * 
 	 * @param object
 	 * @param oldValues
 	 *            A map, containing the original values.
 	 * @param newValues
 	 *            A map, containing the new values.
 	 * @return {@link Validator}
 	 */
 	public static Validator updateObjectProps(Object object,
 			Map<String, Object> oldValues, Map<String, Object> newValues) {
 		Validator validator = new Validator();
 
 		Object oldValue = null;
 		Object newValue = null;
 		Class<?> clazz = null;
 		for (String key : newValues.keySet()) {
 			boolean shouldUpdate = true;
 			newValue = newValues.get(key);
 			if (oldValues.containsKey(key)) {
 				clazz = getPropertyClass(object, key);
 
 				oldValue = Parser.readObject(oldValues.get(key), clazz);
 				if (oldValue != null
 						&& oldValue.equals(newValue)) {
 					shouldUpdate = false;
 				}
 			}
 			if (shouldUpdate) {
				System.out.println(newValue);
 				setPropertyValue(object, key, newValue);
 			}
 		}
 
 		return validator;
 	}
 }
