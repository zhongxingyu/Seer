 package com.gentics.cr.util.generics;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 
 import org.apache.log4j.Logger;
 
 /**
  * Helper class to instanciate generic classes.
  * @author perhab
  *
  */
 
 public final class Instanciator {
 
 	/**
 	 * Log4j logger for debug output.
 	 */
 	private static Logger logger = Logger.getLogger(Instanciator.class);
 
 	/**
 	 * private constructor so the utility class couldn't be instantiated.
 	 */
 	private Instanciator() {
 	}
 
 	/**
 	 * try to get an instance of a class with multiple parameter variations.
 	 * <br />
 	 * e.g. (myCollection.class, [ [1,2], ["1","2"] ])
 	 * @param className Name of the class to instantiate with the parameters.
 	 * @param prioritizedParameters array with arrays with parameters
 	 * @return instantiated object of given class, null in case of an error.
 	 */
 	public static Object getInstance(final String className, final Object[][] prioritizedParameters) {
 		if (className != null) {
 			try {
 				Class<?> clazz = Class.forName(className);
 				return getInstance(clazz, prioritizedParameters);
 			} catch (ClassNotFoundException e) {
 				logger.debug("Cannot find class " + className + ".", e);
 				return null;
 			}
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * try to get an instance of a class with multiple parameter variations.
 	 * <br />
 	 * e.g. (myCollection.class, [ [1,2], ["1","2"] ])
 	 * @param clazz {@link Class} to instantiate with the parameters
 	 * @param prioritizedParameters array with arrays with parameters
 	 * @return instantiated object of given class, null in case of an error.
 	 */
 	public static Object getInstance(final Class<?> clazz, final Object[][] prioritizedParameters) {
 		Object object = null;
 		for (Object[] parameters : prioritizedParameters) {
 			Constructor<?> constructor;
 			Class<?>[] parameterClasses = getParameterClasses(parameters);
 			try {
 				if (object == null) {
 					constructor = getMatchingConstructor(clazz, parameterClasses);
 					if (constructor != null) {
 						object = constructor.newInstance(parameters);
						break;
 					}
 				}
 			} catch (SecurityException e) {
 				logger.debug("Cannot instanciate object for class " + clazz + " with parameters ("
 						+ getReadableStringFromClassArray(parameterClasses) + ").", e);
 			} catch (IllegalArgumentException e) {
 				logger.debug("Cannot instanciate object for class " + clazz + " with parameters ("
 						+ getReadableStringFromClassArray(parameterClasses) + ").", e);
 			} catch (InstantiationException e) {
 				logger.debug("Cannot instanciate object for class " + clazz + " with parameters ("
 						+ getReadableStringFromClassArray(parameterClasses) + ").", e);
 			} catch (IllegalAccessException e) {
 				logger.debug("Cannot instanciate object for class " + clazz + " with parameters ("
 						+ getReadableStringFromClassArray(parameterClasses) + ").", e);
 			} catch (InvocationTargetException e) {
 				logger.debug("Cannot instanciate object for class " + clazz + " with parameters ("
 						+ getReadableStringFromClassArray(parameterClasses) + ").", e);
 			}
 		}
 		return object;
 	}
 
 	/**
 	 * Tries to find a {@link Constructor} of the given {@link Class} that
 	 * matches the given parameters types.
 	 * @param clazz {@link Class} to find the {@link Constructor} for
 	 * @param parameterTypes array with {@link Class}es of the parameters. if a
 	 * position in the array is null it will match everything except primitives.
 	 * @return the {@link Constructor} for the parameter types. null in case no
 	 * matching constructor is found.
 	 */
 	public static Constructor<?> getMatchingConstructor(final Class<?> clazz, final Class<?>[] parameterTypes) {
 		for (Constructor<?> constructor : clazz.getConstructors()) {
 			Class<?>[] constructorParameterTypes = constructor.getParameterTypes();
 			if (parameterTypesMatch(constructorParameterTypes, parameterTypes)) {
 				return constructor;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * tests if the given source parameter types would match the target
 	 * parameter types.<br /> this can be used to test if a {@link Constructor}
 	 * with the target parameters can be called with the source parameters.
 	 * @param targetParameterTypes parameters of the {@link Constructor}
 	 * @param sourceParameterTypes parameters to call the {@link Constructor}
 	 * with
 	 * @return true if you can call the constructor with the target parameters
 	 * with the source parameters.
 	 */
 	public static boolean parameterTypesMatch(final Class<?>[] targetParameterTypes,
 			final Class<?>[] sourceParameterTypes) {
 		if (targetParameterTypes.length == sourceParameterTypes.length) {
 			int matches = 0;
 			for (int i = 0; i < targetParameterTypes.length; i++) {
 				if (sourceParameterTypes[i] == null) {
 					matches++;
 				} else if (targetParameterTypes[i].isAssignableFrom(sourceParameterTypes[i])) {
 					matches++;
 				} else if (targetParameterTypes[i].isPrimitive()) {
 					Class<?> primitiveParameterType = getPrimitiveType(sourceParameterTypes[i]);
 					if (primitiveParameterType != null && primitiveParameterType.equals(targetParameterTypes[i])) {
 						matches++;
 					}
 				} else if (sourceParameterTypes[i].isPrimitive()) {
 					Class<?> primitiveParameterType = getPrimitiveType(targetParameterTypes[i]);
 					if (primitiveParameterType != null && primitiveParameterType.equals(sourceParameterTypes[i])) {
 						matches++;
 					}
 				}
 			}
 			if (matches == sourceParameterTypes.length) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Tries to get a primitive Type of the {@link Class}.
 	 * @param clazz {@link Class} to get the primitive type from.
 	 * @return primitive type of the class if the class has one.
 	 */
 	private static Class<?> getPrimitiveType(final Class<?> clazz) {
 		try {
 			Field primitiveType = clazz.getField("TYPE");
 			if (primitiveType != null) {
 				Object primitiveClassObject = primitiveType.get(null);
 				if (primitiveClassObject instanceof Class) {
 					return (Class<?>) primitiveClassObject;
 				}
 			}
 		} catch (Exception e) {
 			logger.debug("cannot get primitive type from class " + clazz);
 		}
 		return null;
 	}
 
 	/**
 	 * Get an array with classes of the objects in the given array.
 	 * @param parameters array with objects
 	 * @return array with classes of the given objects
 	 */
 	public static Class<?>[] getParameterClasses(final Object[] parameters) {
 		Class<?>[] parameterClasses = new Class[parameters.length];
 		for (int i = 0; i < parameters.length; i++) {
 			if (parameters[i] != null) {
 				parameterClasses[i] = parameters[i].getClass();
 			}
 		}
 		return parameterClasses;
 	}
 
 	/**
 	 * Get a readable String for all {@link Class}es in the given array.
 	 * @param classes array with classes
 	 * @return comma separated {@link String} with class names
 	 */
 	public static String getReadableStringFromClassArray(final Class<?>[] classes) {
 		StringBuffer returnString = new StringBuffer();
 		for (Class<?> clazz : classes) {
 			if (returnString.length() != 0) {
 				returnString.append(", ");
 			}
 			if (clazz != null) {
 				returnString.append(clazz.getName());
 			} else {
 				returnString.append("null");
 			}
 		}
 		return returnString.toString();
 	}
 }
