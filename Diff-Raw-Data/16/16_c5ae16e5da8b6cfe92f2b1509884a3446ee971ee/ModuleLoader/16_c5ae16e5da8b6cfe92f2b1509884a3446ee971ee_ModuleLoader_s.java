 package edu.mines.acmX.exhibit.module_management.loaders;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import edu.mines.acmX.exhibit.module_management.metas.ModuleMetaData;
 import edu.mines.acmX.exhibit.module_management.modules.ModuleInterface;
 
 /**
  * This class loads an instance of a module interface given an absolute path to
  * the top level directory containing the jar(s) for the module(s).
  *
  * @author  Andrew DeMaria
  * @author  Austin Diviness
  */
 
 public class ModuleLoader {
 	
 	private static Logger log = LogManager.getLogger(ModuleLoader.class.getName());
     /**
 	 * Takes a path to a module jar and returns a Module object as specified in the
      * module's manifest.
      * 
      * @param   jarPath		path to the top level directory for the module jars
      * @param   data		An instance of ModuleMetaData containing the metadata
      *						relevant to the desired Module to be loaded.
 	 * @param	classLoader	class loader //TODO needs better description
      */
 	public static ModuleInterface loadModule(String jarPath,
 			ModuleMetaData data, ClassLoader classLoader)
 			throws ModuleLoadException {
 		Class<? extends ModuleInterface> moduleClassToLoad;
 		try {
 			log.debug("Trying to set class loader for jar file: " + jarPath);
 			URLClassLoader loader = getClassLoader(jarPath, classLoader);
 
 			// We now will load the class by searching the jar for the package
 			// and
 			// class as dictated in the module manifest file.
 			// We set the second argument to true to instantiate the class
 			// TODO change later?
 			// Finally, cast it into the usable ModuleInterface class
 			moduleClassToLoad = Class.forName(
 					data.getPackageName() + "." + data.getClassName(), true,
 					loader).asSubclass(ModuleInterface.class);
 			return moduleClassToLoad.newInstance();
 		} catch (IOException e) {
 			String msg = "Jar file not found\n" + e.toString();
 			log.error(msg);
 			throw new ModuleLoadException(msg);
 		} catch (ClassNotFoundException e) {
 			String msg = "Class not found\n" + e.toString();
 			log.error(msg);
 			throw new ModuleLoadException(msg);
 		} catch (InstantiationException e) {
 			String msg = "Class does not properly implement an abstract module\n" + e.toString();
 			log.error(msg);
 			throw new ModuleLoadException(msg);
 		} catch (IllegalAccessException e) {
 			String msg = "There was a security issue with the module loader\n" + e.toString();
 			log.error(msg);
 			throw new ModuleLoadException(msg);
 		} catch (ClassCastException e) {
 			String msg = "Class does not properly implement an abstract module\n" + e.toString();
 			log.error(msg);
 			throw new ModuleLoadException(msg);
 		}
 
     }
 
 	public static InputStream loadResource(String jarPath, ModuleMetaData
 			data, ClassLoader classLoader, String resourcePath) throws
 		ModuleLoadException, MalformedURLException { 
 
 			URLClassLoader loader = getClassLoader( jarPath, classLoader );
 			return loader.getResourceAsStream( resourcePath );
 	}
 
 	private static URLClassLoader getClassLoader( String jarPath, ClassLoader classLoader  ) throws MalformedURLException {
 
 		// Generate a url list of places to look for the jar.  currently we 
 		// just have one location
 		URL[] urlList = { new File(jarPath).toURI().toURL() };
 		log.debug("module Url looks like: " + urlList[0].toString());
 		// Get the class loader that we currently have and transform it into a 
 		// class loader for urls
 		URLClassLoader loader = new URLClassLoader( urlList, classLoader);
 
 		return loader;
 
 	}
 }
 
 
