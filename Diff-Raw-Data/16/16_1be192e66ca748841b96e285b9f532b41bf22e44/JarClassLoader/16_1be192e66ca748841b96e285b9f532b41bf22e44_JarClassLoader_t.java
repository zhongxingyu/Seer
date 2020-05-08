 package com.atlassian.plugin.loaders.classloading;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.jar.JarFile;
 import java.util.zip.ZipEntry;
 
 /**
 * User: Hani Suleiman & Mike Cannon-Brookes
 * (originally sort-of copied from WebWork1 source)
  */
 public class JarClassLoader extends PluginsClassLoader
 {
     private JarFile jar;
     private File file;
 
     public JarClassLoader(File file, ClassLoader parent)
     {
         super(parent);
         this.file = file;
     }
 
     protected URL getDataURL(String name, byte[] data) throws MalformedURLException
     {
         return new URL(null, file.toURL().toExternalForm() + '!' + name, new BytesURLStreamHandler(data));
     }
 
     private void openJar() throws IOException
     {
         if (jar == null)
         {
             jar = new JarFile(file);
         }
     }
 
     public synchronized byte[] getFile(String path)
     {
         try
         {
             openJar();
             ZipEntry entry = jar.getEntry(path);
 
             if (entry == null) return null;
             InputStream in = jar.getInputStream(entry);
             int size = (int) entry.getSize();
             byte[] data = readStream(in, size);
             return data;
         }
         catch (IOException e)
         {
             return null;
         }
     }
 
     public Object clone()
     {
         JarClassLoader loader = new JarClassLoader(file, getParent());
         loader.packages = packages;
         return loader;
     }
 }
