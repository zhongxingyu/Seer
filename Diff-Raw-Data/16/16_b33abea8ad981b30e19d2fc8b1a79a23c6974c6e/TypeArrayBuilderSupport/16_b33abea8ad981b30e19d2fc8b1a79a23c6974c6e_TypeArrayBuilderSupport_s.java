 package net.sf.clirr.core.spi;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 
 import net.sf.clirr.core.internal.ExceptionUtil;
 
 public abstract class TypeArrayBuilderSupport implements TypeArrayBuilder
 {
 
     protected ClassLoader createClassLoader(File[] jarFiles, ClassLoader thirdPartyClasses)
     {
         final URL[] jarUrls = new URL[jarFiles.length];
         for (int i = 0; i < jarFiles.length; i++)
         {
             File jarFile = jarFiles[i];
             try
             {
                URL url = jarFile.toURL();
                 jarUrls[i] = url;
             }
             catch (MalformedURLException ex)
             {
                 // this should never happen
                 final IllegalArgumentException illegalArgumentException =
                     new IllegalArgumentException(
                             "Cannot create classloader with jar file " + jarFile);
                 ExceptionUtil.initCause(illegalArgumentException, ex);
                 throw illegalArgumentException;
             }
         }
         final URLClassLoader jarsLoader = new URLClassLoader(jarUrls, thirdPartyClasses);
         
         return jarsLoader;
     }
 
 }
