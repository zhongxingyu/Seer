 package com.buzybeans.core.beans;
 
 import com.buzybeans.core.api.Archive;
 import com.buzybeans.core.conf.BuzybeansConfig;
 import com.buzybeans.core.util.ClassUtils;
 import com.buzybeans.core.util.ClassUtils.FromFileReader;
 import com.buzybeans.core.util.ClassUtils.FromJarFileReader;
 import com.buzybeans.core.util.ClassUtils.FromReader;
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  * @author mathieuancelin
  */
 public class ApplicationArchive implements Archive {
 
     private File deployBase;
     private List<File> deployedFiles = new ArrayList<File>();
     private Map<String, FromReader> classPath = new HashMap<String, FromReader>();
     private Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
     private Map<String, List<URL>> resourceFiles = new HashMap<String, List<URL>>();
 
     @Override
     public ApplicationArchive addClass(Class<?> clazz) {
         classes.put(clazz.getName(), clazz);
         return this;
     }
 
     @Override
     public ApplicationArchive addClasses(Class<?>... addclasses) {
         for (Class<?> clazz : addclasses) {
             classes.put(clazz.getName(), clazz);
         }
         return this;
     }
 
     @Override
     public ApplicationArchive addFile(File file) {
         try {
             addURL(file.getName(), file.toURI().toURL());
         } catch (MalformedURLException ex) {
             BuzybeansConfig.logger.error(null, ex);
         }
         return this;
     }
 
     @Override
     public ApplicationArchive addFile(File... files) {
         for (File file : files) {
             try {
                 addURL(file.getName(), file.toURI().toURL());
             } catch (MalformedURLException ex) {
                 BuzybeansConfig.logger.error(null, ex);
             }
         }
         return this;
     }
 
     @Override
     public ApplicationArchive addFile(File file, String name) {
         try {
             addURL(name, file.toURI().toURL());
         } catch (MalformedURLException ex) {
             BuzybeansConfig.logger.error(null, ex);
         }
         return this;
     }
 
     public Collection<Class<?>> getClasses() {
         return classes.values();
     }
 
     public URL getResource(String name) {
         Collection<URL> urls = getResources(name);
         if(urls.isEmpty()) {
             return null;
         } else {
             return urls.toArray(new URL[] {})[0];
         }
     }
 
     public Collection<URL> getResources(String name) {
         if (!resourceFiles.containsKey(name)) {
             return null;
         }
         return resourceFiles.get(name);
     }
 
     public Class<?> getClass(String name) {
         return classes.get(name);
     }
 
     void init(ClassLoader loader) {
         listDeployedClasses();
         populateClasspathFiles();
         loadDeployedClasses(loader);
         for (Class<?> clazz : classes.values()) {
             System.out.println("class : " + clazz.getName());
         }
         System.out.println("");
         for (Collection<URL> urls : resourceFiles.values()) {
             for (URL url : urls) {
                 System.out.println("file : " + url.toString());
             }
         }
         System.out.println("");
     }
 
     public void setDeployedFiles(List<File> files) {
         this.deployedFiles = files;
     }
 
     public void setDeployBase(File base) {
         this.deployBase = base;
     }
 
     public Map<String, FromReader> getClassPath() {
         return classPath;
     }
 
     private void addURL(String key, URL url) {
         if (!resourceFiles.containsKey(key)) {
             resourceFiles.put(key, new ArrayList<URL>());
         }
         resourceFiles.get(key).add(url);
     }
 
     private void listDeployedClasses() {
         for (File file : deployedFiles) {
             if (file.getName().endsWith(".class")) {
                 String name = ClassUtils.filenameToClassname(
                         file.getAbsolutePath().replace(deployBase.getAbsolutePath(), "").replace("/WEB-INF/classes/", ""));
                 classPath.put(name, new FromFileReader(file));
             } else if (file.getName().endsWith(".jar")) {
                 try {
                     classPath.putAll(ClassUtils.loadFromJar(file));
                 } catch (Exception ex) {
                     ex.printStackTrace();
                 }
             } else {
                 // TODO : list files
                 addFile(file);
             }
         }
     }
 
     private void loadDeployedClasses(ClassLoader loader) {
         for (String name : classPath.keySet()) {
            if (!(classPath.get(name) instanceof FromJarFileReader)) {
                try {
                    Class<?> clazz = loader.loadClass(name);
                    addClass(clazz);
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
             }
         }
     }
 
     private void populateClasspathFiles() {
         for (FromReader reader : classPath.values()) {
             if (reader instanceof FromJarFileReader) {
                 FromJarFileReader fileReader = (FromJarFileReader) reader;
                 addURL(fileReader.getName(), fileReader.getUrl());
             }
         }
     }
 }
