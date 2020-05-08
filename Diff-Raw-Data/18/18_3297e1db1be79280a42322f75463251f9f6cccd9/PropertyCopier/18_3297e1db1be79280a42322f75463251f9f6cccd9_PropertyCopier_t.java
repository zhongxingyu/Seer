 package com.gramercysoftware.utils;
 
 import java.beans.BeanInfo;
 import java.beans.Introspector;
 import java.beans.PropertyDescriptor;
 import java.text.MessageFormat;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.commons.beanutils.ConvertUtils;
 import org.apache.commons.beanutils.NestedNullException;
 import org.apache.commons.beanutils.PropertyUtils;
 import org.apache.commons.beanutils.PropertyUtilsBean;
 import org.apache.commons.lang.ClassUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 /**
  * Utility to copy properties from one object to another, in a null safe
  * fashion.
  * 
  * 'Dot-format' properties are also permitted.
  * 
  * @author David Harcombe <david.harcombe@gmail.com>
  */
 public class PropertyCopier {
	private Logger logger = Logger.getLogger(PropertyCopier.class);
 
 	/**
 	 * Copy all matching named properties in the source into the matching properties in the destination
 	 *  
 	 * @param source
 	 * @param destination
 	 */
 	public void copyProperties(Object source, Object destination) {
 		try {
 			PropertyDescriptor[] propertyDescriptors;
 			BeanInfo info = Introspector.getBeanInfo(source.getClass(), Object.class);
 			propertyDescriptors = info.getPropertyDescriptors();
 			for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
 				if(PropertyUtils.isWriteable(destination, propertyDescriptor.getName())) {
 					copyProperty(source, destination, propertyDescriptor.getName());
 				}
 			}
 		} catch (Exception e) {
 			logger.warn(MessageFormat.format(
 					"Property conversion error: {0} -> {1} throws {2}",
 					new Object[] { ClassUtils.getShortClassName(source, "<unknown>"), ClassUtils.getShortClassName(destination, "<unknown>"), 
 							e.getClass() }));
 			logger.debug("Stack trace:", e);
 		}
 	}
 
 	/**
 	 * Copy the list of properties in 'propertyPaths' from the source object
 	 * into the same named properties in the destination object.
 	 * 
 	 * Missing properties on either side will simply be skipped, and an error
 	 * logged.
 	 * 
 	 * Nested properties should be written in dot-format.
 	 * 
 	 * For example:
 	 * <pre>
 	 * copyProperties(from, to, new String[] { &quot;fooBar&quot;, &quot;foo.bar&quot; })
 	 * </pre>
 	 * is equivalent to:
 	 * <pre>
 	 * to.setFooBar(from.getFooBar());
 	 * to.getFoo().setBar(from.getFoo().getBar());
 	 * </pre>
 	 *
 	 * @param source
 	 * @param destination
 	 * @param propertyPaths
 	 */
 	public void copyProperties(Object source, Object destination, String[] propertyPaths) {
 		for (String property : propertyPaths) {
 			copyProperty(source, destination, property, property);
 		}
 	}
 
 	/**
 	 * Copy the single property 'propertyPath' from the source object into the
 	 * same named property in the destination object.
 	 * 
 	 * @see com.enstream.utils.classutils.PropertyCopier#copyProperties(Object,
 	 *      Object, String[])
 	 *
 	 * @param source
 	 * @param destination
 	 * @param propertyPaths
 	 */
 	public void copyProperty(Object source, Object destination, String propertyPath) {
 		copyProperty(source, destination, propertyPath, propertyPath);
 	}
 
 	/**
 	 * Copy the list of properties in 'propertyPaths' out of the source object
 	 * into the named property in the destination object.
 	 * 
 	 * In the array of properties, element [0] is the source property name,
 	 * element [1] the destination.
 	 * 
 	 * For example:
 	 * <pre>
 	 * copyProperties(from, to, new String[] { {&quot;fooBar&quot;, &quot;barFoo&quot;} })
 	 * </pre>
 	 * is equivalent to:
 	 * <pre>
 	 * to.setBarFoo(from.getFooBar());
 	 * </pre>
 
 	 * @see com.enstream.utils.classutils.PropertyCopier#copyProperties(Object,
 	 *      Object, String[])
 	 *
 	 * @param source
 	 * @param destination
 	 * @param propertyPaths
 	 */
 	public void copyProperties(Object source, Object destination, String[][] propertyPaths) {
 		for (String[] property : propertyPaths) {
 			copyProperty(source, destination, property[0], property[1]);
 		}
 	}
 
 	/**
 	 * sets fromValue directly on destinationPropertyPath of destination, inflating any objects along the way
 	 * @param destination
 	 * @param destinationPropertyPath
 	 * @param fromValue
 	 */
 	public void setProperty(Object destination, String destinationPropertyPath, Object fromValue) {
 		try {
 			if (destination == null) {
 				return;
 			}
 			PropertyUtilsBean instance = new PropertyUtilsBean();
 			Class<?> toType = null;
 			String currentPropertyPath = null;
 			do {
 				try {
 					toType = PropertyUtils.getPropertyType(destination, destinationPropertyPath);
 				} catch (NestedNullException e) {
 					currentPropertyPath = updateCurrentPropertyPath(currentPropertyPath, destinationPropertyPath);
 					if(BeanUtils.getProperty(destination, currentPropertyPath) == null) {
 						Class<?> type = instance.getPropertyType(destination, currentPropertyPath);
 						BeanUtils.setProperty(destination, currentPropertyPath, type.newInstance());
 					}
 				}
 			} while (toType == null);
 			
 			if (ICustomConverter.class.isAssignableFrom(toType) && ConvertUtils.lookup(toType) == null) {
 				registerCustomConverter(toType);
 			}
 			
 			if(Enum.class.isAssignableFrom(toType) && ConvertUtils.lookup(toType) == null) {
 				ConvertUtils.register(new EnumConverter(), toType);
 			}
 			Object convert = ConvertUtils.convert(fromValue, toType);
 			PropertyUtils.setProperty(destination, destinationPropertyPath, convert);
 		} catch (Exception e) {
 			logger.warn(MessageFormat.format(
 					"Property set error: {0}.{1} -> throws {2}",
 					new Object[] { ClassUtils.getShortClassName(destination, "<unknown>"), destinationPropertyPath, e.getClass() }));
 			logger.debug("Stack trace:", e);
 		}
 	}
 	
 	/**
 	 * Copy the single property 'fromPropertyPath' from the source object into
 	 * the property 'toPropertyPath' in the destination object.
 	 * 
 	 * @see com.enstream.utils.classutils.PropertyCopier#copyProperties(Object,
 	 *      Object, String[])
 	 *
 	 * @param source
 	 * @param destination
 	 * @param sourcePropertyPath
 	 * @param destinationPropertyPath
 	 */
 	public void copyProperty(Object source, Object destination, String sourcePropertyPath, String destinationPropertyPath) {
 		try {
 			if (source == null || destination == null) {
 				return;
 			}
 			Object fromValue = PropertyUtils.getProperty(source, sourcePropertyPath);
 			setProperty(destination, destinationPropertyPath, fromValue);
 		} catch (Exception e) {
 			logger.warn(MessageFormat.format(
 					"Property conversion error: {0}.{1} -> {2}.{3} throws {4}",
 					new Object[] { ClassUtils.getShortClassName(source, "<unknown>"), sourcePropertyPath,
 							ClassUtils.getShortClassName(destination, "<unknown>"), destinationPropertyPath, e.getClass() }));
 			logger.debug("Stack trace:", e);
 		}
 	}
 
 	/**
 	 * Copy a single property from indexed object 'fromPropertyPath' from the source object into
 	 * the property 'toPropertyPath' in the destination object.
 	 * 
 	 * @see com.enstream.utils.classutils.PropertyCopier#getIndexedProperty(Object,
 	 *      String, int)
 	 * @see com.enstream.utils.classutils.PropertyCopier#copyProperties(Object,
 	 *      Object, String[])
 	 *
 	 * 
 	 * @param source
 	 * @param destination
 	 * @param sourcePropertyPath
 	 * @param destinationPropertyPath
 	 * @param index
 	 */
 	public void copyIndexedProperty(Object source, Object destination, String sourcePropertyPath, String destinationPropertyPath, int index) {
 		try {
 			Object fromValue = PropertyUtils.getIndexedProperty(source, sourcePropertyPath, index);
 			setProperty(destination, destinationPropertyPath, fromValue);
 		} catch (Exception e) {
 			logger.warn(MessageFormat.format(
 					"Get Indexed Property error: {0}.{1} throws {2}",
 					new Object[] { ClassUtils.getShortClassName(source, "<unknown>"), 
 							sourcePropertyPath,
 							e.getClass() }));
 			logger.debug("Stack trace:", e);
 		}
 	}
 
 	private String updateCurrentPropertyPath(String currentPropertyPath, String destinationPropertyPath) {
 		StringBuffer result = new StringBuffer();
 		int curPosLoc = -1;
 		int nextDot = -1;
 		if(currentPropertyPath == null) {
 			curPosLoc = 0;
			nextDot = destinationPropertyPath.indexOf('.', 0);
 		} else {
 			curPosLoc = currentPropertyPath.length()+1;
 			nextDot = destinationPropertyPath.indexOf(".", curPosLoc+1);
 		}
 		if(nextDot == -1) {
 			nextDot = destinationPropertyPath.length();
 		}
 		
 		if(StringUtils.isNotBlank(currentPropertyPath)) {
 			result.append(currentPropertyPath).append("."); 
 		}
 		result.append(destinationPropertyPath.substring(curPosLoc, nextDot));
 		return result.toString();
 	}
 
 	/**
 	 * Register a custom converter in the ConvertUtils
 	 *
 	 * @param toType
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
 	 * @throws Exception
 	 */
	protected void registerCustomConverter(Class<?> toType) throws InstantiationException, IllegalAccessException {
 		ICustomConverter convertible = (ICustomConverter) toType.newInstance();
 		ConvertUtils.register(convertible.getConverter(), toType);
 	}
 }
