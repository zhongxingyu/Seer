 package net.contextfw.web.application.internal.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import net.contextfw.web.application.WebApplicationException;
 
 public class ClassScanner extends AbstractScanner {
 
     public static List<Class<?>> getClasses(String... packageNames) {
         ArrayList<String> list = new ArrayList<String>(packageNames.length);
         Collections.addAll(list, packageNames);
         return getClasses(list);
     }
     
     /**
      * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
      *
      * @param packageName The base package
      * @return The classes
      * @throws ClassNotFoundException
      * @throws IOException
      */
     public static List<Class<?>> getClasses(Iterable<String> packageNames) {
     
         ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
         
         try {
             for (String packageName : packageNames) {
                 List<String> resourcePaths = new ArrayList<String>();
                 resourcePaths.add(packageName);
                 List<ResourceEntry> entries = findResourceEntries(resourcePaths);
 
                 for (ResourceEntry entry: entries) {
                     
                     String fileName = entry.getPath();
                     if (fileName.endsWith(".class") && !fileName.contains("$")) {
                         Class<?> _class;
                         String className = toClassName(fileName);
                         try {
                             _class = Class.forName(className);
                         } catch (ExceptionInInitializerError e) {
                             // happen, for example, in classes, which depend on 
                             // Spring to inject some beans, and which fail, 
                             // if dependency is not fulfilled
                             _class = Class.forName(className,
                                     false, Thread.currentThread().getContextClassLoader());
                         }
                         classes.add(_class);
                     }
                 }
             }
         } catch (ClassNotFoundException e) {
             throw new WebApplicationException(e);
         }
         
         return classes;
     }
 
     private static String toClassName(String fileName) {
         return fileName.substring(0, fileName.length() - 6).replaceAll("/", "\\.");
     }
     
     /**
      * Recursive method used to find all classes in a given directory and subdirs.
      *
      * @param directory   The base directory
      * @param packageName The package name for classes found inside the base directory
      * @return The classes
      * @throws ClassNotFoundException
      */
     private static List<Class<?>> findClasses(String parent, File directory, String packageName) throws ClassNotFoundException 
     {
         List<Class<?>> classes = new ArrayList<Class<?>>();
         if (!directory.exists()) {
             return classes;
         }
         File[] files = directory.listFiles();
         for (File file : files) {
             
             String fileName = file.getName();
             if (file.isDirectory()) {
                 assert !fileName.contains(".");
                 classes.addAll(findClasses(parent+"/"+fileName, file, packageName + "." + fileName));
             } else if (fileName.endsWith(".class") && !fileName.contains("$")) {
                 Class<?> _class;
                 try {
                     _class = Class.forName(packageName + '.' + fileName.substring(0, fileName.length() - 6));
                 } catch (ExceptionInInitializerError e) {
                     // happen, for example, in classes, which depend on 
                     // Spring to inject some beans, and which fail, 
                     // if dependency is not fulfilled
                     _class = Class.forName(packageName + '.' + fileName.substring(0, fileName.length() - 6),
                             false, Thread.currentThread().getContextClassLoader());
                 }
                 classes.add(_class);
             }
         }
         return classes;
     }
 }
