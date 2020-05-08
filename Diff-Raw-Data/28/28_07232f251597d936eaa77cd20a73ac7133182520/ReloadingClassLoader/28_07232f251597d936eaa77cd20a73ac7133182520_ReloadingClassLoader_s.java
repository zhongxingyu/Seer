 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.myfaces.scripting.sandbox.loader;
 
 import org.apache.myfaces.scripting.sandbox.loader.support.ClassFileLoader;
 import org.apache.myfaces.scripting.sandbox.loader.support.OverridingClassLoader;
 import org.apache.myfaces.scripting.sandbox.loader.support.ThrowAwayClassLoader;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * <p>A class loader implementation that enables you to reload certain classes. It automatically
  * reloads classes if there's a newer version of a .class file available in a specified compilation
  * target path. However, it's also possible to explicitly reload other classes.</p>
  * <p/>
  * <p>This enables you to do both modify and reload various classes that you've used for Spring
  * bean definitions, but it also enables you to reload for example classes depending on those
  * dynamically compiled classes, like factory bean classes. By explicitly reloading a factory
  * bean class the newly loaded factory bean will return updated bean instances as well!</p>
  * <p/>
  * <p>Note that even though this class extends the class URLClassLoader it doesn't use any
  * of its functionalities. This class loader just works similar and provides a similar interface
  * so it's useful to extend the class URLClassLoader as you can treat it like one (especially
  * when it comes to resolving the classpath of a class loader).</p>
  */
 public class ReloadingClassLoader extends URLClassLoader {
 
     /**
      * The system-dependent default name-separator character. Note that it's safe to
      * use this version of the file separator in regex methods, like replaceAll().
      */
    private static String FILE_SEPARATOR = File.separator;

    static {
        if ("\\".equals(FILE_SEPARATOR)) {
            FILE_SEPARATOR = "\\\\";
        }
    }
 
     /**
      * The logger instance for this class.
      */
     private static final Logger logger = Logger.getLogger(ReloadingClassLoader.class.getName());
 
     /**
      * A table of class names and the according class loaders. It's basically like
      * a list of classes that this class loader has already loaded. However, the
      * thing is that this class loader isn't actually going to load them as we
      * would loose the possibility to override them then, which is the reason why
      * each class has got its own class loader.
      */
     private Map<String, ThrowAwayClassLoader> classLoaders =
             new HashMap<String, ThrowAwayClassLoader>();
 
     /**
      * The target directory for the compiler, i.e. the directory that contains the
      * dynamically compiled .class files.
      */
     private File compilationDirectory;
 
     // ------------------------------------------ Constructors
 
     /**
      * <p>Constructs a new reloading classloader for the specified compilation
      * directory using the default delegation parent classloader. Note that this
      * classloader will only delegate to the parent classloader if there's no
      * dynamically compiled class available.</p>
      *
      * @param compilationDirectory the compilation directory
      */
     public ReloadingClassLoader(File compilationDirectory) {
         super(new URL[0]);
         this.compilationDirectory = compilationDirectory;
     }
 
     /**
      * <p>Constructs a new reloading classloader for the specified compilation
      * directory using the given delegation parent classloader. Note that this
      * classloader will only delegate to the parent classloader if there's no
      * dynamically compiled class available.</p>
      *
      * @param parentClassLoader    the parent classloader
      * @param compilationDirectory the compilation directory
      */
     public ReloadingClassLoader(ClassLoader parentClassLoader, File compilationDirectory) {
         super(new URL[0], parentClassLoader);
         this.compilationDirectory = compilationDirectory;
     }
 
     // ------------------------------------------ URLClassLoader methods
 
     /**
      * <p>Loads the class with the specified binary name. This method searches for classes in the
      * compilation directory that you've specified previously. Note that this class loader recognizes
      * whether the class files have changed, that means, if you recompile and reload a class, you'll
      * get a Class object that represents the recompiled class.</p>
      *
      * @param className the binary name of the class you want to load
      * @param resolve   <tt>true</tt>, if the class is to be resolved
      * @return The resulting <tt>Class</tt> object
      * @throws ClassNotFoundException if the class could not be found
      */
     protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
         // First of all, check if there's a class file available in the compilation target path.
         // It doesn't matter which class we're dealing with at the moment as there's always the
         // possibility that the user is either trying to override a statically compiled class
         // (i.e. a class that has been compiled before deploying the application) or he/she is
         // trying to modify a dynamically compiled class, in which case we should compare
         // timestamps, etc.
         File classFile = resolveClassFile(className);
         if (classFile != null && classFile.exists()) {
             if (classLoaders.containsKey(className)) {
                 // Check if the class loader is already outdated, i.e. there is a newer class file available
                 // for the class we want to load than the class file we've already loaded. If that's the case
                 // we're going to throw away this ClassLoader and create a new one for linkage reasons.
                 ThrowAwayClassLoader classLoader = classLoaders.get(className);
                 if (classLoader.isOutdated(classFile.lastModified())) {
                     // If the class loader is outdated, create a new one. Otherwise the same class loader
                     // would have to load the same class twice or more often which would cause severe
                     // linkage errors. Actually the JVM wouldn't permit that anyway and throw some
                     // linkage errors / exceptions.
                     reloadClass(className);
                 }
             } else {
                 if (logger.isLoggable(Level.FINEST)) {
                     logger.log(Level.FINEST, "A new dynamic class '"
                             + className + "' has been found by this class loader '" + this + "'.");
                 }
 
                 // We haven't loaded this class so far, but there is a .class file available,
                 // so we have to reload the given class.
                 reloadClass(className);
             }
 
             ThrowAwayClassLoader classLoader = classLoaders.get(className);
             return classLoader.loadClass(className, resolve);
         } else {
             // Even though there is no class file available, there's still a chance that this
             // class loader has forcefully reloaded a statically compiled class.
             if (classLoaders.containsKey(className)) {
                 ThrowAwayClassLoader classLoader = classLoaders.get(className);
                 return classLoader.loadClass(className, resolve);
             } else {
                 // However, if there's neither a .class file nor a reloadable class loader
                 // available, just delegate to the parent class loader.
                 return super.loadClass(className, resolve);
             }
         }
     }
 
     /**
      * <p>Returns the search path of URLs for loading classes, i.e. the
      * given compilation target directory, the directory that contains the
      * dynamically compiled .class files.</p>
      *
      * @return the search path of URLs for loading classes
      */
     public URL[] getURLs() {
         try {
             return new URL[]{compilationDirectory.toURI().toURL()};
         } catch (IOException ex) {
             logger.log(Level.SEVERE, "Couldn't resolve the URL to the compilation directory {0} {1} {2} ", new String[]{compilationDirectory.getAbsolutePath(), ex.getMessage(), ex.toString()});
             return new URL[0];
         }
     }
 
     // ------------------------------------------ Public methods
 
     /**
      * <p>Determines whether the given class has been loaded by a class
      * loader that is already outdated.</p>
      *
      * @param classObj the class you want to check
      * @return <code>true</code, if there is a newer class file available for the given object
      */
     public boolean isOutdated(Class classObj) {
         // Is there even a dynamically compiled class file available for the given class?
         File classFile = resolveClassFile(classObj.getName());
         if (classFile.exists()) {
             // If so, check if we the Class reference has been loaded by a ThrowAwayClassLoader.
             // Otherwise it's definitely outdated and we don't have to compare timestamps.
             if (classObj.getClassLoader() instanceof ThrowAwayClassLoader) {
                 // Compare the timestamps in order to determine whether the given Class
                 // reference is already outdated.
                 ThrowAwayClassLoader classLoader = (ThrowAwayClassLoader) classObj.getClassLoader();
                 return classLoader.isOutdated(classFile.lastModified());
             } else {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * <p>Reloads the given class internally explicitly. Note that this classloader usually
      * reloads classes automatically, i.e. this classloader detects if there is a newer
      * version of a class file available in the compilation directory. However, by using
      * this method you tell this classloader to forcefully reload the given class. For
      * example, if you've got a newer version of a dynamically recompiled class and a
      * statically compiled class depending on this one, you can tell this classloader to
      * reload the statically compiled class as well so that it references the correct
      * version of the Class object.</p>
      *
      * @param className the class you want to reload
      */
     public void reloadClass(String className) {
         ThrowAwayClassLoader classLoader;
 
         File classFile = resolveClassFile(className);
         if (classFile != null && classFile.exists()) {
             classLoader = new ClassFileLoader(className, classFile, this);
         } else {
             classLoader = new OverridingClassLoader(className, this);
         }
 
         ThrowAwayClassLoader oldClassLoader = classLoaders.put(className, classLoader);
         if (logger.isLoggable(Level.INFO)) {
             if (oldClassLoader != null) {
                 logger.info("Replaced the class loader '" + oldClassLoader + "' with the class loader '"
                         + classLoader + "' as this class loader is supposed to reload the class '" + className + "'.");
             } else {
                 logger.info("Installed a new class loader '" + classLoader + "' for the class '"
                         + className + "' as this class loader is supposed to reload it.");
             }
         }
     }
 
     /**
      * <p>Returns a copy of the current reloading class loader with the only difference
      * being the parent class loader to use. Use this method if you just want to replace
      * the parent class loader (obviously you can't do that after a ClassLoader has been
      * created, hence a copy is created).</p>
      *
      * @param parentClassLoader the parent ClassLoader to use
      * @return a copy of the current reloading class loader
      */
     @SuppressWarnings("unused")
     public ReloadingClassLoader cloneWithParentClassLoader(ClassLoader parentClassLoader) {
         ReloadingClassLoader classLoader =
                 new ReloadingClassLoader(parentClassLoader, compilationDirectory);
 
         // Note that we don't have to create "deep copies" as the class loaders in the map
         // are immutable anyway (they are only supposed to load a single class) and additionally
         // this map doesn't contain any classes that have been loaded using the current parent
         // class loader!
         classLoader.classLoaders = new HashMap<String, ThrowAwayClassLoader>(classLoaders);
 
         return classLoader;
     }
 
     // ------------------------------------------ Utility methods
 
     /**
      * <p>Resolves and returns a File handle that represents the class file of
      * the given class on the file system. However, note that this method only
      * returns <code>null</code> if an error occured while resolving the class
      * file. A non-null valuee doesn't necessarily mean that the class file
      * actually exists. In oder to check the existence call the according
      * method on the returned object.</p>
      *
      * @param className the name of the class that you want to resolve
      * @return a File handle that represents the class file of the given class
      *         on the file system
      * @see java.io.File#exists()
      */
     protected File resolveClassFile(String className) {
         // This method just has to look in the specified compilation directory. The
         // relative class file path can be computed from the class name.
         return new File(compilationDirectory,
                 className.replaceAll("\\.", FILE_SEPARATOR).concat(".class"));
     }
 
 }
