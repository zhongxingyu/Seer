 package com.ijg.darklight.core.loader;
 
 import java.lang.reflect.Constructor;
 
 public class DarklightLoader {
 	/**
 	 * 
 	 * @param classToLoad The desired class to load
 	 * @return The loaded class returned by classLoader.loadClass
 	 */
 	public static Class<?> loadClass(String classToLoad) {
 		ClassLoader classLoader = DarklightLoader.class.getClassLoader();
 		try {
 			Class<?> loadedClass = classLoader.loadClass(classToLoad);
 			return loadedClass;
 		} catch (ClassNotFoundException e) {
 			System.out.println("[DarklightLoader] Error, class \"" + classToLoad + "\" not found");
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * This method is to load and instantiate classes with constructors
	 * @param classToLoad The desired class to load
	 * @param initargs Constructor arguments
	 * @param args Argument classes for constructor
	 * @return New class instance
 	 */
	public static Object loadAndInstantiateClass(String classToLoad, Object[] initargs, Class<?>... args) {
 		Class<?> loadedClass = loadClass(classToLoad);
 		try {
			Constructor<?> classConstructor = loadedClass.getConstructor(args);
			return classConstructor.newInstance(initargs);
 		} catch (Exception e) {
 			System.out
 					.println("[DarklightLoader] There was an error loading class \""
 							+ classToLoad + "\":");
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	public static Object loadAndInstantiateClass(String classToLoad) {
 		Class<?> loadedClass = loadClass(classToLoad);
 		try {
 			return loadedClass.newInstance();
 		} catch (Exception e) {
 			System.out
 			.println("[DarklightLoader] There was an error loading class \""
 					+ classToLoad + "\":");
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
