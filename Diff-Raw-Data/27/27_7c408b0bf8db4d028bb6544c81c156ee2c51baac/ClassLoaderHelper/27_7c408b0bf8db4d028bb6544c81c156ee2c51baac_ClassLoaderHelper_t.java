 package gakesson.util.misc;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.lang.reflect.Field;
 import java.nio.charset.StandardCharsets;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Vector;
 
 /**
  * This class is a helper for pre-loading classes in order to reduce time for
  * JVM to reach steady state.
  * 
  * @author Gustav Åkesson - gustav.r.akesson@gmail.com
  *
  */
 public final class ClassLoaderHelper {
 
 	private ClassLoaderHelper() {
 		// Nothing
 	}
 
 	/**
 	 * Writes all currently loaded classes by the provided {@link ClassLoader}
 	 * into the file in the specified file path.
 	 * 
	 * This method does not raise any kind of {@link Throwable}.
	 * 
 	 * @param absoluteFilePath
 	 * @param classLoader
 	 */
 	public static void writeLoadedClassesToFile(String absoluteFilePath,
 			ClassLoader classLoader) {
 
 		try {
 			List<Class<?>> loadedClasses = ClassLoaderHelper
 					.getLoadedClassesOrDefault(classLoader);
 			File file = new File(absoluteFilePath);
 
 			try (BufferedOutputStream outputStream = new BufferedOutputStream(
 					new FileOutputStream(file), 1024 * 1024)) {
 				byte[] lineSeparator = System.lineSeparator().getBytes(
 						StandardCharsets.UTF_8);
 
 				outputStream
 						.write("# This file contains classes to load for a particular JVM process"
 								.getBytes(StandardCharsets.UTF_8));
 				outputStream.write(lineSeparator);
 
 				for (Class<?> loadedClass : loadedClasses) {
 					outputStream.write(loadedClass.getName().trim()
 							.getBytes(StandardCharsets.UTF_8));
 					outputStream.write(lineSeparator);
 				}
 			}
 		} catch (Throwable t) {
 			// Silent ignore
 		}
 	}
 
 	/**
	 * Loads all classes (fully qualified names) into the provided
	 * {@link ClassLoader} from the file with the specified file path.
	 * 
	 * This method does not raise any kind of {@link Throwable}.
 	 * 
 	 * @param absoluteFilePath
 	 * @param classLoader
 	 */
 	public static void loadClassesFromFile(String absoluteFilePath,
 			ClassLoader classLoader) {
 
 		try {
 			File file = new File(absoluteFilePath);
 
 			if (file.exists()) {
 				try (BufferedReader inputStream = new BufferedReader(
 						new FileReader(file))) {
 
 					String classToLoad = inputStream.readLine();
 
 					while (classToLoad != null) {
 						if (!classToLoad.startsWith("#")) {
 							loadClass(classToLoad.trim(), classLoader);
 						}
 						classToLoad = inputStream.readLine();
 					}
 				}
 			}
 		} catch (Throwable t) {
 			// Silent ignore
 		}
 	}
 
 	private static void loadClass(String classToLoad, ClassLoader classLoader) {
 
 		try {
 			try {
 				Class.forName(classToLoad, true, classLoader);
 			} catch (LinkageError | ClassNotFoundException e) {
 				// Silent ignore
 			}
 
 		} catch (Throwable t) {
 			// Silent ignore
 		}
 	}
 
 	private static List<Class<?>> getLoadedClassesOrDefault(
 			ClassLoader classLoader) {
 		List<Class<?>> loadedClasses = Collections.emptyList();
 
 		try {
			loadedClasses = getLoadedClassesOrDefault(classLoader,
 					classLoader.getClass());
 		} catch (Throwable t) {
 			// Silent ignore
 		}
 
		return Collections.unmodifiableList(new ArrayList<>(loadedClasses));
 	}
 
 	@SuppressWarnings("unchecked") // Whatever
	private static List<Class<?>> getLoadedClassesOrDefault(
 			ClassLoader classLoader, Class<?> classToSearchIn) {
		List<Class<?>> loadedClasses = Collections.emptyList();
 
 		if (classToSearchIn != null) {
 
 			try {
 				Field loadedClassesField = classToSearchIn
 						.getDeclaredField("classes");
 				loadedClassesField.setAccessible(true);
 				loadedClasses = (Vector<Class<?>>) loadedClassesField
 						.get(classLoader);
 			} catch (NoSuchFieldException | IllegalAccessException e) {
				loadedClasses = getLoadedClassesOrDefault(classLoader,
 						classToSearchIn.getSuperclass());
 			}
 		}
 
 		return loadedClasses;
 	}
 }
