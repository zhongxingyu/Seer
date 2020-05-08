 package com.dafttech.classloader;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 public class ClassLoader {
    private static final String jarPre = "file:";
    private static final String jarPost = ".jar!";
    private static final String fakeSpaces = "%20";

     private File sourceDir = null;
     private String sourcePackage = "";
     private boolean isJarfile = false;
     private boolean canLoadItself = false;
     private List<Class<?>> loaded = new ArrayList<Class<?>>();
 
     public ClassLoader(File dir) {
        String path = dir.toString().replace("\\", "/");
        if (path.contains(jarPre) && path.toLowerCase().contains(jarPost)) {
             isJarfile = true;
            int jarPreIndex = path.indexOf(jarPre);
            int jarPostIndex = path.toLowerCase().indexOf(jarPost);
            sourceDir = new File(path.substring(jarPreIndex + jarPre.length(), jarPostIndex + 4).replace(fakeSpaces, " "));
            sourcePackage = path.substring(jarPostIndex + jarPost.length() + 1).replace("/", ".");
         } else {
            sourceDir = new File(path);
         }
     }
 
     public ClassLoader(Class<?> sourceClass, String packageName) {
         this(getPackagePath(sourceClass, packageName));
     }
 
     public ClassLoader setSourcePackage(String packageName) {
         if (packageName != null) sourcePackage = packageName.replace("\\", ".").replace("/", ".");
         return this;
     }
 
     public ClassLoader load() {
         if (isJarfile) {
             loadJar(sourceDir);
         } else {
             loadDir(sourceDir);
         }
         return this;
     }
 
     public List<Class<?>> getLoaded() {
         return loaded;
     }
 
     private void loadDir(File dir) {
         File[] files = dir.listFiles();
         if (files == null || files.length == 0) return;
         for (File file : files) {
             if (file.isDirectory()) {
                 loadDir(file);
             } else if (file.getName().endsWith(".class")) {
                 loadClass(file);
             }
         }
     }
 
     private void loadJar(File dir) {
         try {
             JarFile jarfile = new JarFile(dir);
             Enumeration<JarEntry> entries = jarfile.entries();
             while (entries.hasMoreElements()) {
                 JarEntry entry = entries.nextElement();
                 if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                     loadClass(new File(dir.getAbsolutePath() + "/" + entry.getName()));
                 }
             }
             jarfile.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private void loadClass(File dir) {
        String currPackage = getCurrentPackage(dir.getName().endsWith(".class") ? new File(dir.getAbsolutePath().substring(0,
                 dir.getAbsolutePath().length() - 6)) : dir);
        if (currPackage.startsWith(sourcePackage)) {
             try {
                 URL[] urls = { sourceDir.toURI().toURL() };
                 URLClassLoader classloader = URLClassLoader.newInstance(urls, getClass().getClassLoader());
                Class<?> tmpClass = classloader.loadClass(currPackage);
                 if (tmpClass != null && (canLoadItself || tmpClass != getClass())) loaded.add(tmpClass);
             } catch (ClassNotFoundException e) {
                 e.printStackTrace();
             } catch (MalformedURLException e) {
                 e.printStackTrace();
             } catch (NoClassDefFoundError e) {
                 e.printStackTrace();
             }
         }
     }
 
     private String getCurrentPackage(File dir) {
         if (!dir.getAbsolutePath().startsWith(sourceDir.getAbsolutePath())
                 || dir.getAbsolutePath().length() <= sourceDir.getAbsolutePath().length()) return "";
         return dir.getAbsolutePath().substring(sourceDir.getAbsolutePath().length() + 1).replace("\\", ".").replace("/", ".");
     }
 
     public static File getClassPath(Class<?> context) {
         URL url = context.getResource("/" + context.getName().replace(".", "/") + ".class");
         if (url == null) return null;
         String path = url.toString();
         if (path.startsWith("rsrc:")) {
             path = "jar:file:/" + new File(System.getProperty("java.class.path")).getAbsolutePath().replace("\\", "/") + "!/"
                     + path.substring(5);
         }
         if (!path.startsWith("jar:")) {
             try {
                 return new File(new URL(path).getPath());
             } catch (MalformedURLException e) {
             }
         }
         return new File(path);
     }
 
     public static File getPackagePath(Class<?> context, String packageName) {
         File path = getClassPath(context);
         if (path != null) {
             String packagePath = path.toString();
             packagePath = packagePath.substring(0, packagePath.length() - (context.getName() + ".class").length());
             return new File(packagePath, packageName.replace(".", "/"));
         }
         return null;
     }
 
     @Deprecated
     public static File getCurrentPath() {
         try {
             Enumeration<URL> resources;
             String parentPackage = "";
             for (Package pck : Package.getPackages()) {
                 parentPackage = pck.getName().contains(".") ? pck.getName().substring(0, pck.getName().indexOf(".")) : pck.getName();
                 resources = java.lang.ClassLoader.getSystemResources(parentPackage);
                 if (resources.hasMoreElements()) {
                     String currPath = resources.nextElement().getFile();
                     if (currPath.endsWith(parentPackage))
                         return new File(currPath.substring(0, currPath.length() - parentPackage.length()));
                 }
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
         return null;
     }
 }
