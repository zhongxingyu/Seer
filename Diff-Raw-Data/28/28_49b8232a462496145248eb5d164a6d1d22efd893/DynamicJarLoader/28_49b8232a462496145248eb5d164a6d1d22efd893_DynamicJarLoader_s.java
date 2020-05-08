 package pimp.classloader;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 /**
  * This class loads all the classes from the jars in a specified folder.
  * The list of these classes can be retrieved using getClassList.
  * 
  * @author Daniel Schramm
  */
 public class DynamicJarLoader {
 	
 	private DynamicJarLoader() {} // Make it impossible to instantiate.
 	
 	public static List<Class<?>> load(String dirName) {
 		return load(dirName, Object.class);
 	}
 	
 	/**
 	 * Searches the specified directory for jar files, and loads all classes
 	 * within these jar files.
 	 * 
 	 * @return the list of classes found.
 	 */
 	public static List<Class<?>> load(String dirName, Class<?> superClass) {
 		List<Class<?>> classList = new ArrayList<Class<?>>();
 		
 		// Create a string pointing to the product folder.
 		final ClassLoader loader = DynamicJarLoader.class.getClassLoader();
 		URL directoryUrl = loader.getResource(dirName);
 		if (directoryUrl == null) {
 			System.out.println("Could not find the '" + dirName + 
 					"' folder in " + System.getProperty("user.dir"));
 			return classList; // Could not find the product directory.
 		}
 		
 		// Get a list of files in the directory.
 		List<File> jarFiles = new ArrayList<File>();
 		String decodedUrl = "";
 		try {
 			decodedUrl = URLDecoder.decode(directoryUrl.getFile(), "UTF-8");
 			File jarFolder = new File(decodedUrl);
 			jarFiles = Arrays.asList(jarFolder.listFiles());
 		} catch (UnsupportedEncodingException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		// Loop through each in the .
 		System.out.println("Searching in: '" + decodedUrl + "'");
 	    for(File file: jarFiles){
 		    // Check to make sure it is a .class file.
 		    if (file.getName().endsWith(".jar")) {
			    System.out.println("Checking jar file: " + file.getName());
 		      	// Load all the classes in the jarFile.
 		        classList.addAll(loadClassesFromJar(file, superClass));
 		    }
 	    }
 	    
 	    return classList;
 	}
 	
 	private static boolean hasSuperclass(Class<?> c, Class<?> superClass) {
 		if (superClass == Object.class) return true;
 		
 		Class<?> s = c.getSuperclass();
 		while(s != null && s != Object.class){
 			if (s == superClass) return true;
 			s = s.getSuperclass();
 		}
 		return false;
 	}
 	
 	private static List<Class<?>> loadClassesFromJar(File f, Class<?> sc) {
 		List<Class<?>> classes = new ArrayList<Class<?>>();
 		ClassLoaderWithClose classLoader = null;
 		try {
 			// Create the ClassLoader for this Jar.
			classLoader = new ClassLoaderWithClose( 
		    		new URL[] {f.toURI().toURL()} );
 			
 			JarFile jar = new JarFile(f);
 			// Check each entry in the Jar to see if it is a class.
 			for (JarEntry entry: Collections.list(jar.entries())) {
 				if (entry.getName().endsWith(".class")) {
 					String className = entry.getName();
 					className = className.replace("/", ".").replace(".class","");
 					// Load the class.
 					Class<?> c = classLoader.loadClass(className);
 					if(hasSuperclass(c, sc)) {
 						classes.add(c);
 						System.out.println("Class found: " + className);
 					}
 				}
 	    	}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
			classLoader.close();
 		}
 		return classes;
 	}
 }
