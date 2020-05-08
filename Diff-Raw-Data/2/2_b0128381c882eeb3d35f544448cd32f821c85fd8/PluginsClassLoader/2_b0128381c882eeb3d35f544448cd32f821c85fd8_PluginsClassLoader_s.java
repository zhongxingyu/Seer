 package com.atlassian.plugin.loaders.classloading;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.security.SecureClassLoader;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Hani Suleiman (hani@formicary.net)
  *         (derived from WebWork 1's WebworkClassLoader)
  */
 public abstract class PluginsClassLoader extends SecureClassLoader implements Cloneable
 {
     protected static Log log = LogFactory.getLog(PluginsClassLoader.class);
 
     protected String[] packages = null;
     private Map cache = new HashMap();
 
     protected PluginsClassLoader(ClassLoader parent)
     {
         super(parent);
     }
 
     protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException
     {
         Class c = (Class) cache.get(name);
         if (c != null) return c;
         /* boolean handles = false;
         if (packages != null)
         {
             for (int i = 0; i < packages.length; i++)
             {
                 if (name.startsWith(packages[i]))
                 {
                     handles = true;
                     break;
                 }
             }
         }
         if (!handles)
         {
             return super.loadClass(name, resolve);
         }
         */
         try
         {
             c = findClass(name);
         }
         catch (ClassNotFoundException ex)
         {
             return super.loadClass(name, resolve);
         }
         cache.put(name, c);
         return c;
     }
 
     protected Class findClass(String name) throws ClassNotFoundException
     {
         String path = name.replace('.', '/').concat(".class");
         byte[] data = getFile(path);
         if (data == null)
             throw new ClassNotFoundException();
 
         return defineClass(name, data, 0, data.length);
     }
 
     protected URL findResource(String name)
     {
         byte[] data = this.getFile(name);
 
         if (data == null)
             return null;
         try
         {
             return getDataURL(name, data);
         }
         catch (MalformedURLException e)
         {
             return null;
         }
     }
 
     protected abstract URL getDataURL(String name, byte[] data) throws MalformedURLException;
 
     public Enumeration findResources(String name)
     {
         URL url = this.findResource(name);
 
         if (url == null)
             return null;
 
         return Collections.enumeration(Collections.singleton(url));
     }
 
     protected abstract byte[] getFile(String path);
 
     public static ClassLoader getInstance(URL url)
     {
         return getInstance(url, ClassLoader.getSystemClassLoader());
     }
 
     public static ClassLoader getInstance(URL url, ClassLoader parent)
     {
         ClassLoader loader;
         File file = new File(url.getFile());
         if (file.isDirectory())
         {
             log.warn("Making a DirectoryClassLoader for: " + file);
             loader = new URLClassLoader(new URL[]{url}, parent);
         }
         else
         {
             log.warn("Making a JarClassLoader for: " + file);
             loader = new JarClassLoader(file, parent);
         }
         return loader;
     }
 
     public abstract Object clone();
 
     /**
      * Method from WebWork1 webwork.util.ClassLoaderUtils
      */
     public static byte[] readStream(InputStream in, int size) throws IOException
     {
         if (in == null) return null;
         if (size == 0) return new byte[0];
         int currentTotal = 0;
         int bytesRead;
         byte[] data = new byte[size];
         while (currentTotal < data.length && (bytesRead = in.read(data, currentTotal, data.length - currentTotal)) >= 0)
             currentTotal += bytesRead;
         in.close();
         return data;
     }
 
     public URL getResource(String name)
     {
         URL url = findResource(name);
 
        if (name != null)
             return url;
 
         return super.getResource(name);
     }
 
     /**
      * Clean any resources held by the Classloader. ie, close files etc etc.
      */
     public void close()
     {
     }
 }
