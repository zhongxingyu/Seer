 package net.sf.clirr.core.internal;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 
 /**
  * Helper class for dealing with ClassLoaders. 
  * @author lk
  */
 public final class ClassLoaderUtil
 {
     
    /** prevent instantiation. */
     private ClassLoaderUtil()
     {
     }
 
     /**
      * @param cpEntries
      * @return
      */
     public static ClassLoader createClassLoader(final String[] cpEntries)
     {
         final URL[] cpUrls = new URL[cpEntries.length];
         for (int i = 0; i < cpEntries.length; i++)
         {
             String cpEntry = cpEntries[i];
             File entry = new File(cpEntry);
             try
             {
                 URL url = entry.toURI().toURL();
                 cpUrls[i] = url;
             }
             catch (MalformedURLException ex)
             {
                 final IllegalArgumentException illegalArgEx =
                     new IllegalArgumentException(
                         "Cannot create classLoader from classpath entry " + entry);
                 ExceptionUtil.initCause(illegalArgEx, ex);
                 throw illegalArgEx;
             }
         }
         final URLClassLoader classPathLoader = new URLClassLoader(cpUrls);
         return classPathLoader;
     }
 
 }
