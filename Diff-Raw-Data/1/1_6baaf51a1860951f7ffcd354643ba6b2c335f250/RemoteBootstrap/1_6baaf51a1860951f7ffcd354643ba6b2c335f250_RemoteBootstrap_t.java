 /**
  *  Copyright (C) 2009 Progress Software, Inc. All rights reserved.
  *  http://fusesource.com
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.fusesource.meshkeeper.launcher;
 
 import org.fusesource.meshkeeper.MeshKeeper;
 import org.fusesource.meshkeeper.MeshKeeperFactory;
 import org.fusesource.meshkeeper.classloader.ClassLoaderFactory;
 import org.fusesource.meshkeeper.classloader.Marshalled;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.LinkedList;
 
 /**
  * Java main class that be be used to bootstrap the classpath via remote
  * classpath downloading of another main class.
  * 
  * @author chirino
  */
 public class RemoteBootstrap {
 
     public static final String BOOTSTRAP_PROPERTY = "meshkeeper.bootstrap";
     
     private static MeshKeeper mesh;
     private static ClassLoader classLoader;
 
     private File cache;
     private String classLoaderFactory;
     private String mainClass;
     private String[] args;
     private String meshKeeperUri;
     private String runnable;
 
     static class SyntaxException extends Exception {
         private static final long serialVersionUID = 4997524790367555614L;
 
         SyntaxException(String message) {
             super(message);
         }
     }
 
     public static MeshKeeper getMeshKeeper() {
         return mesh;
     }
     
     public static ClassLoader getClassLoader() {
         return classLoader;
     }
 
     public static void main(String args[]) throws Throwable {
 
         if (System.getProperty("meshkeeper.application") == null) {
             System.setProperty("meshkeeper.application", RemoteBootstrap.class.getName());
         }
 
         RemoteBootstrap main = new RemoteBootstrap();
         LinkedList<String> alist = new LinkedList<String>(Arrays.asList(args));
 
         try {
             // Process the options.
             while (!alist.isEmpty()) {
                 String arg = alist.removeFirst();
                 if (arg.equals("--help")) {
                     showUsage();
                     return;
                 } else if (arg.equals("--cache")) {
                     try {
                         main.setCache(new File(alist.removeFirst()).getCanonicalFile());
                     } catch (Exception e) {
                         throw new SyntaxException("Expected a directoy after the --cache option.");
                     }
                 } else if (arg.equals("--distributor")) {
                     try {
                         main.setDistributor(alist.removeFirst());
                     } catch (Exception e) {
                         throw new SyntaxException("Expected a url after the --distributor option.");
                     }
                 } else if (arg.equals("--runnable")) {
                     try {
                         main.setRunnable(alist.removeFirst());
                     } catch (Throwable e) {
                         throw new SyntaxException("Expected a url after the --runnable option.");
                     }
                 } else if (arg.equals("--classloader")) {
                     try {
                         main.setClassLoader(alist.removeFirst());
                     } catch (Throwable e) {
                         throw new SyntaxException("Expected a url after the --classloader option.");
                     }
                 } else {
                     // Not an option.. then it must be the main class name and args..
                     main.setMainClass(arg);
                     String a[] = new String[alist.size()];
                     alist.toArray(a);
                     main.setArgs(a);
                     break;
                 }
             }
 
             // Validate required arguments/options.
             if (main.getRunnable() == null) {
                 if (main.getMainClass() == null) {
                     throw new SyntaxException("Main class not specified.");
                 }
                 if (main.getClassLoaderFactory() == null) {
                     throw new SyntaxException("--classloader not specified.");
                 }
             }
             if (main.getCache() == null) {
                 throw new SyntaxException("--cache not specified.");
             }
             if (main.getDistributor() == null) {
                 throw new SyntaxException("--distributor not specified.");
             }
 
         } catch (SyntaxException e) {
             System.out.println("Invalid Syntax: " + e.getMessage());
             System.out.println();
             showUsage();
            Thread.currentThread().sleep(1000);
             System.exit(2);
         }
         main.execute();
     }
 
     private static void showUsage() {
         System.out.println();
     }
 
     private void execute() throws Throwable {
 
         // Store our options in the System properties.. they might be usefull
         // to the booted application.
         System.setProperty(MeshKeeperFactory.MESHKEEPER_REGISTRY_PROPERTY, this.meshKeeperUri);
         System.setProperty(BOOTSTRAP_PROPERTY, "true");
         System.setProperty("meshkeeper.bootstrap.cache", cache.getPath());
         if (runnable != null) {
             System.setProperty("meshkeeper.bootstrap.runnable", runnable);
         } else {
             System.setProperty("meshkeeper.bootstrap.classloader", classLoaderFactory);
             System.setProperty("meshkeeper.bootstrap.mainclass", mainClass);
         }
 
         mesh = MeshKeeperFactory.createMeshKeeper(meshKeeperUri);
 
         System.out.println("bootstrap started...");
         if (runnable != null) {
             Runnable r = null;
             try {
                 Marshalled<Runnable> marshalled = mesh.registry().getRegistryObject(runnable);
                 if (marshalled == null) {
                     throw new Exception("The runnable not found at: " + runnable);
                 }
                 //              distributor.getRegistry().remove(runnable, false);
                 ClassLoaderFactory clf = marshalled.getClassLoaderFactory();
 
                 System.out.println("Setting up classloader...");
                 classLoader = clf.createClassLoader(getClass().getClassLoader(), cache);
 
                 System.out.println("Executing runnable.");
                 r = marshalled.get(classLoader);
             } catch (Throwable e) {
                 e.printStackTrace();
                 System.exit(100);
             }
 
             r.run();
 
             try {
                 // The runnable can set the exit code via a system prop.
                 System.exit(Integer.parseInt(System.getProperty("meshkeeper.bootstrap.exit", "0")));
             } catch (NumberFormatException e) {
                 e.printStackTrace();
                 System.exit(101);
             }
 
         } else {
             Method mainMethod = null;
             try {
                 ClassLoaderFactory clf = mesh.registry().getRegistryObject(this.classLoaderFactory);
 
                 System.out.println("Setting up classloader...");
                 classLoader = clf.createClassLoader(getClass().getClassLoader(), cache);
                 
                 System.out.println("Executing main.");
                 Class<?> clazz = classLoader.loadClass(mainClass);
                 // Invoke the main.
                 mainMethod = clazz.getMethod("main", new Class[] { String[].class });
             } catch (Throwable e) {
                 e.printStackTrace();
                 System.exit(100);
             }
             try {
                 mainMethod.invoke(null, new Object[] { args });
             } catch (InvocationTargetException e) {
                 throw e.getTargetException();
             }
         }
     }
 
     ///////////////////////////////////////////////////////////////////
     // Property Accessors
     ///////////////////////////////////////////////////////////////////
 
     public void setDistributor(String uri) {
         meshKeeperUri = uri;
     }
 
     public String getDistributor() {
         return meshKeeperUri;
     }
 
     public void setClassLoader(String classLoader) {
         this.classLoaderFactory = classLoader;
     }
 
     public void setCache(File cache) {
         this.cache = cache;
     }
 
     public File getCache() {
         return cache;
     }
 
     public String getClassLoaderFactory() {
         return classLoaderFactory;
     }
 
     public String[] getArgs() {
         return args;
     }
 
     public void setArgs(String[] args) {
         this.args = args;
     }
 
     public String getMainClass() {
         return mainClass;
     }
 
     public void setMainClass(String mainClass) {
         this.mainClass = mainClass;
     }
 
     public String getRunnable() {
         return runnable;
     }
 
     public void setRunnable(String runnable) {
         this.runnable = runnable;
     }
 }
