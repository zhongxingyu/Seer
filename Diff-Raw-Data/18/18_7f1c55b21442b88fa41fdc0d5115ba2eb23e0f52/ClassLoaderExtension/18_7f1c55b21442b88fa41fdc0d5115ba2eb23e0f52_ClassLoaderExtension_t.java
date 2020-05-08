 /**
  * For copyright information see the LICENSE document.
  */
 
 package com.realityshard.host;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 
 /**
  * This class is used to load shared libraries (used by the game apps and protocols)
  * 
  * (Taken from stackoverflow)
  * 
  * @author _rusty
  */
 public class ClassLoaderExtension 
 {
    private final static Logger LOGGER = LoggerFactory.getLogger(ClassLoaderExtension.class.getName());
     private static final Class[] parameters = new Class[]{URL.class};
     
     public static void addLibs(File libsFolder)
     {
         try 
         {
             // add the path itself 
             addFolder(libsFolder.toURI().toURL());
         
             // also add any jars from that path
             if (libsFolder.exists()) 
             {        
                 // quick and dirty: get all jars from there (not including sub directories)
                 File[] libs = libsFolder.listFiles(new FileFilter() 
                 {
                     @Override
                     public boolean accept(File file) 
                     {
                         return file.getName().toLowerCase().endsWith(".jar");
                     }
                 });
 
                 // add the found jars to the classloader
                 for (File file : libs) 
                 {
                     ClassLoaderExtension.addFolder(file.toURI().toURL());
                 }
             }
         } 
         catch (MalformedURLException ex) 
         {
            LOGGER.error(String.format("Could not find a library or add it to the classloader. [name {} ]", libsFolder), ex);
         }
     }
     
     /**
      * Adds a specific folder to the default class loader.
      * Be careful with that, as we will access the classloaders protected methods
      * 
      * @param       folder                  The folder that will be added to the classpath 
      */
     public static void addFolder(URL url)
     {
         // get the system classloader
         URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
         Class<?> sysclass = URLClassLoader.class;
         
         try 
         {
             // and try to access it's protected addURL method
             Method method = sysclass.getDeclaredMethod("addURL", parameters);
             method.setAccessible(true);
             method.invoke(sysloader, new Object[]{url});
         } 
         catch ( IllegalAccessException | 
                 IllegalArgumentException | 
                 InvocationTargetException | 
                 NoSuchMethodException | 
                 SecurityException ex) 
         {
            LOGGER.error(String.format("Could not find a given classpath folder or add it to the classloader. [name {} ]", url), ex);
         }
 
     }
 }
