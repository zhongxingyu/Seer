 /**
  * Copyright (c) 2012 scireum GmbH - Andreas Haufler - aha@scireum.de
  *
  * Permission is hereby granted, free of charge, to any person obtaining a 
  * copy of this software and associated documentation files (the "Software"),
  * to deal in the Software without restriction, including without limitation
  * the rights to use, copy, modify, merge, publish, distribute, sublicense,
  * and/or sell copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included
  * in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
  * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  * DEALINGS IN THE SOFTWARE.
  */
 package com.scireum.open.nucleus;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.JarURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.logging.Logger;
 
 /**
  * Responsible for loading and all available modules (JAR files or file based
  * class loaders which have a component.properties in their root directory).
  * 
  * Each class implementing {@link ClassLoadAction} will be instantiated using
  * the no-args constructor and notified about each loaded class.
  * 
  * Additionally this class provides an extension registry where arbitrary
  * objects can be registered for given classes.
  */
 public class Nucleus {
 
 	/**
 	 * Classes implementing this interface will be invoked for each loaded
 	 * class.
 	 */
 	public interface ClassLoadAction {
 
 		/**
 		 * Invoked for each loaded class of a known module.
 		 */
 		void handle(Class<?> clazz) throws Exception;
 
 		/**
 		 * Invoked if all classes were processed.
 		 */
 		void loadingCompleted() throws Exception;
 
 	}
 
 	public static Logger LOG = Logger.getLogger(Nucleus.class.getName());
 	private static boolean initialized = false;
 	private static Map<Class<?>, List<Object>> model = Collections
 			.synchronizedMap(new HashMap<Class<?>, List<Object>>());
 
 	/**
 	 * Takes a given url and creates a list which contains all children of the
 	 * given url. (Works with Files and JARs).
 	 */
 	public static List<String> getChildren(URL url) {
 		List<String> result = new ArrayList<String>();
 		if ("file".equals(url.getProtocol())) {
			File file = new File(url.getPath());
 			if (!file.isDirectory()) {
 				file = file.getParentFile();
 			}
 			addFiles(file, result, file);
 		} else if ("jar".equals(url.getProtocol())) {
 			try {
 				JarFile jar = ((JarURLConnection) url.openConnection())
 						.getJarFile();
 				Enumeration<JarEntry> e = jar.entries();
 				while (e.hasMoreElements()) {
 					JarEntry entry = e.nextElement();
 					result.add(entry.getName());
 				}
 			} catch (IOException e) {
 				LOG.warning(e.getMessage());
 			}
 		}
 		return result;
 	}
 
 	private static void addFiles(File file, List<String> result, File reference) {
 		if (!file.exists() || !file.isDirectory()) {
 			return;
 		}
 		for (File child : file.listFiles()) {
 			if (child.isDirectory()) {
 				addFiles(child, result, reference);
 			} else {
 				String path = null;
 				while (child != null && !child.equals(reference)) {
 					if (path != null) {
 						path = child.getName() + "/" + path;
 					} else {
 						path = child.getName();
 					}
 					child = child.getParentFile();
 				}
 				result.add(path);
 			}
 		}
 	}
 
 	public static synchronized void init() {
 		if (initialized) {
 			return;
 		}
 		initialized = true;
 		try {
 			Nucleus.LOG.info("Nucleus Microkernel is starting up...");
 			Nucleus.LOG.info("-------------------------------------");
 			List<URL> urls = Collections.list(Nucleus.class.getClassLoader()
 					.getResources("component.properties"));
 			List<Class<?>> classes = new ArrayList<Class<?>>();
 			List<ClassLoadAction> loaders = new ArrayList<ClassLoadAction>();
 			for (URL url : urls) {
 				LOG.info("Loading component: " + getName(url) + " form "
 						+ url.toString());
 				for (String relativePath : getChildren(url)) {
 					if (relativePath.endsWith(".class")) {
 						String className = relativePath.substring(0,
 								relativePath.length() - 6).replace("/", ".");
 						try {
 							LOG.fine("Found class: " + className);
 							Class<?> clazz = Class.forName(className);
 							classes.add(clazz);
 							if (ClassLoadAction.class.isAssignableFrom(clazz)
 									&& !clazz.isInterface()) {
 								try {
 									ClassLoadAction loader = (ClassLoadAction) clazz
 											.newInstance();
 									loaders.add(loader);
 								} catch (Exception e) {
 									LOG.warning("Error creating ClassLoadAction: "
 											+ className + ": " + e.getMessage());
 								}
 							}
 						} catch (ClassNotFoundException e) {
 							LOG.warning("Failed to load class: " + className
 									+ ": " + e.getMessage());
 						} catch (NoClassDefFoundError e) {
 							LOG.warning("Failed to load dependend class: "
 									+ className + ": " + e.getMessage());
 						}
 					}
 				}
 			}
 
 			Nucleus.LOG.info("Processing Classes...");
 			// Handle all loaded classes.
 			for (Class<?> clazz : classes) {
 				for (ClassLoadAction loader : loaders) {
 					try {
 						loader.handle(clazz);
 					} catch (Exception e) {
 						LOG.warning("Failed to call the class load action: "
 								+ loader.getClass() + " for: " + clazz + ": "
 								+ e.getMessage());
 					}
 				}
 			}
 
 			Nucleus.LOG.info("Finalizing setup...");
 			// Notify loaders we're complete
 			for (ClassLoadAction loader : loaders) {
 				try {
 					loader.loadingCompleted();
 				} catch (Exception e) {
 					LOG.warning("Failed to complete: " + loader.getClass()
 							+ ": " + e.getMessage());
 				}
 			}
 			Nucleus.LOG.info("Nucleus is ready for operation...");
 			Nucleus.LOG.info("-------------------------------------");
 		} catch (IOException e) {
 			LOG.warning("Failed to discover components: " + e.getMessage());
 		}
 	}
 
 	private static String getName(URL url) {
 		try {
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					url.openStream()));
 			try {
 				return reader.readLine();
 			} finally {
 				reader.close();
 			}
 		} catch (IOException e) {
 			return "unknown";
 		}
 	}
 
 	/**
 	 * Finds an instance for the given class.
 	 */
 	@SuppressWarnings("unchecked")
 	public static <P> P findPart(Class<P> clazz) {
 		if (!initialized) {
 			init();
 		}
 		List<?> objects = model.get(clazz);
 		if (objects == null || objects.isEmpty()) {
 			return null;
 		}
 		Object object = objects.get(0);
 		if (!clazz.isAssignableFrom(object.getClass())) {
 			throw new IllegalArgumentException(
 					"The found part did not implement the requested class: "
 							+ clazz + " resolved to: " + object);
 		}
 		return (P) object;
 	}
 
 	/**
 	 * Finds all instances registered for the given class.
 	 */
 	@SuppressWarnings("unchecked")
 	public static <P> List<P> findParts(Class<P> clazz) {
 		if (!initialized) {
 			init();
 		}
 		List<Object> objects = findAll(clazz);
 		List<P> result = new ArrayList<P>(objects.size());
 		for (Object object : objects) {
 			if (clazz.isAssignableFrom(object.getClass())) {
 				result.add((P) object);
 			}
 
 		}
 		return result;
 	}
 
 	/**
 	 * Finds all objects registered for the given class. There is no required
 	 * relationship between the given class and the returned objects.
 	 */
 	public static List<Object> findAll(Class<?> clazz) {
 		if (!initialized) {
 			init();
 		}
 		List<Object> objects = model.get(clazz);
 		if (objects == null || objects.isEmpty()) {
 			return Collections.emptyList();
 		}
 		return objects;
 	}
 
 	/**
 	 * Registers a new object for the given class.
 	 */
 	public static void register(Class<?> clazz, Object object) {
 		Nucleus.LOG.fine("Registering: " + object + " [" + object.getClass()
 				+ "] for: " + clazz);
 		List<Object> objects = model.get(clazz);
 		if (objects == null) {
 			objects = new ArrayList<Object>();
 			model.put(clazz, objects);
 		}
 		objects.add(object);
 	}
 
 }
