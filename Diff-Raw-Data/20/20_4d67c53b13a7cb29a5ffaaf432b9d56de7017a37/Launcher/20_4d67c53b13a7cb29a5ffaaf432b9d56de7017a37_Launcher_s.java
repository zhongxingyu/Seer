 package com.cloudbees.sdk.boot;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 
 /**
  * @Author: Fabian Donze
  */
 public class Launcher {
     public final static String BEES_CLASS = "com.cloudbees.sdk.Bees";
     public final static File DEFAULT_REPO_DIR = new File(System.getProperty("user.home"), ".bees");
 
     public static void main(String[] args) {
 //        System.out.println(System.getProperties());
 
         try {
             URL[] urls = getJarUrls();
             ClassLoader parentLoader = Thread.currentThread().getContextClassLoader();
             URLClassLoader loader;
             if (parentLoader != null)
                 loader = new URLClassLoader(urls, parentLoader);
             else
                 loader = new URLClassLoader(urls);
 
             Class beesClass = loader.loadClass(BEES_CLASS);
             Thread.currentThread().setContextClassLoader(loader);
 
             Method mainMethod = beesClass.getDeclaredMethod("main", new Class[]{ String[].class });
             Object obj = mainMethod.invoke(null, new Object[]{args});
             int returnValue = 0;
             if (obj instanceof Integer) {
                 returnValue = ((Integer)obj).intValue();
             }
             System.exit(returnValue);
 
         } catch (Throwable e) {
             e.printStackTrace();
         }
     }
 
     private static File getLocalRepository() {
         File repo_dir;
         if(System.getenv("BEES_REPO") != null)
             repo_dir = new File(System.getenv("BEES_REPO"));
         else if(System.getProperty("bees.repo") != null)
             repo_dir = new File(System.getProperty("bees.repo"));
         else
             repo_dir = DEFAULT_REPO_DIR;
         return repo_dir;
     }
 
     private static URL[] getJarUrls() throws MalformedURLException
     {
         String resourcePath = Launcher.class.getName().replace(".", "/") + ".class";
         URL baseUrl = Launcher.class.getClassLoader().getResource(resourcePath);
         if (baseUrl == null)
             throw new IllegalStateException(Launcher.class.getName());
 
         // trim off the jar protocol
         String filePart = baseUrl.getFile();
 
         // trim off the ! part
         String[] parts = filePart.split("!");
         String fileUrl = parts[0];
 
         // trim off the file protocol
         String filePath = new URL(fileUrl).getFile();
         File parentFile = new File(URLDecoder.decode(filePath)).getParentFile();
 
         ArrayList<URL> list = new ArrayList<URL>();
 
        // Add jars from $BEES.REPO/lib default: [${user.home}/.bees/lib]
        addJars(new File(getLocalRepository(), "lib"), list);
 
         // Add jars from where the Launcher jar came from
         if (parentFile.isDirectory()) {
             addJars(parentFile, list);
         }
 
         // Add the tools.jar
         String javaHome = System.getenv("JAVA_HOME");
         // Try to define it
         if (javaHome == null) {
             String[] paths = System.getProperty("sun.boot.library.path").split(",");
             if (paths != null && paths.length > 0) {
                 javaHome = paths[0].trim();
             }
         }
         if (javaHome != null) {
             File javaHomeDir = new File(javaHome);
             File tools = findToolsJar(javaHomeDir);
             if (tools != null && tools.exists()) {
 //                System.out.println("Add tools.jar");
                 list.add(tools.toURI().toURL());
 /*
             } else {
                 System.err.println("WARNING: Cannot find JAVA_HOME. Some commands might not work properly");
 */
             }
         }
 
         return list.toArray(new URL[list.size()]);
 
     }
 
     private static void addJars(File dir, ArrayList<URL> list) throws MalformedURLException {
         String [] files = dir.list(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return (name.endsWith(".jar"));
             }
         });
         if (files != null) {
             for (int i = 0, n = files.length; i < n; i++) {
                 URL u = new File(dir, files[i]).toURI().toURL();
                 list.add(u);
             }
         }
     }
 
     private static File findToolsJar(File dir) {
         if (dir == null) return null;
         File tools = new File(dir, "/lib/tools.jar");
         if (tools.exists()) {
             return tools;
         } else {
             return findToolsJar(dir.getParentFile());
         }
     }
 }
