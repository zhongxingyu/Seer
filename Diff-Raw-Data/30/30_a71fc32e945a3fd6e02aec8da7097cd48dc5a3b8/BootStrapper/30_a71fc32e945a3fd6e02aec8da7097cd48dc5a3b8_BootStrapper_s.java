 package org.gethydrated.hydra.launcher;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Bootstraps Hydra.
  * 
  * @author Christian Kulpa
  * @since 0.1.0
 *
  */
 public final class BootStrapper {
    
     /**
      * Hide constructor to prevent instanciation.
      */
     private BootStrapper() {
     }
 
     /**
      * Gathers all Hydra related jars and attemps to lauch afterwards.
      * 
     * @param args arguments.
     * @param hydraHome hydra home directory.
     * @throws Exception on failure while loading or starting Hydra.
      */
     public static void bootstrap(final String[] args, final File hydraHome)
             throws Exception {
         List<File> systemJars = new ArrayList<File>();
 
         addFileSet(systemJars, new File(hydraHome, "lib"), ".jar");
 
         systemJars.add(new File(hydraHome, "conf"));
 
         int i = 0;
         URL[] urls = new URL[systemJars.size()];
         for (File file : systemJars) {
             urls[i++] = file.toURI().toURL();
         }
 
        URLClassLoader rootLoader = new URLClassLoader(urls, null);
         Class<?> mainClass = rootLoader
                 .loadClass("org.gethydrated.hydra.launcher.HydraStarter");
         Method mainMethod = mainClass.getMethod("start", String[].class);
         Thread.currentThread().setContextClassLoader(rootLoader);
         mainMethod.invoke(null, new Object[] {args });
     }
 
     /**
      * Adds files from an directory to a file list based on given file endings.
      * 
     * @param fileSet file list.
     * @param target target directory.
     * @param endings file endings.
      */
     private static void addFileSet(final List<File> fileSet, final File target,
             final String... endings) {
         File[] files = target.listFiles(new FileFilter() {
             public boolean accept(final File file) {
                 for (String s : endings) {
                     if (file.getAbsolutePath().endsWith(s)) {
                         return true;
                     }
                 }
                 return false;
             }
         });
 
         for (File f : files) {
             fileSet.add(f);
         }
     }
 }
