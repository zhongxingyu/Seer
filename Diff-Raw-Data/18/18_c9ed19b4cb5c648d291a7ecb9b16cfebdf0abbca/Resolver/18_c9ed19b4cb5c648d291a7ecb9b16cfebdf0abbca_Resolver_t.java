 package org.codehaus.xfire.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 import org.codehaus.xfire.XFireRuntimeException;
 
 /**
  * Resolves a File, classpath resource, or URL according to the follow rules:
  * <ul>
  * <li>Check to see if a file exists, relative to the base URI.</li>
  * <li>If the file doesn't exist, check the classpath</li>
  * <li>If the classpath doesn't exist, try to create URL from the URI.</li>
  * </ul>
  * @author Dan Diephouse
  */
 public class Resolver
 {
     private File file;
     private URL url;
     private InputStream is;
     
     public Resolver(String path) throws IOException
     {
         this("", path);
     }
     
     public Resolver(String baseUriStr, String uriStr) 
         throws IOException
     {
         if (baseUriStr != null) 
             file = new File(baseUriStr, uriStr);
 
         // fall back on a non relative path, if it doesn't exist we'll catch it later
         if (file == null || !file.exists())
         {
             file = new File(uriStr);
         }
 
         if (!file.exists())
         {
             file = null;
             try 
             {
                 URI relative = new URI(uriStr);
                 if (relative.isAbsolute())
                 {
                     is = relative.toURL().openStream();
                    url = relative.toURL();
                 }
                 else if (baseUriStr != null)
                 {
                     URI base = new URI(baseUriStr);
                     base = base.resolve(relative);
                     if (base.isAbsolute())
                     {
                         is = base.toURL().openStream();
                        url = base.toURL();
                     }
                    
                 }
             } catch (URISyntaxException e) {
             }
         }
         
         if (is == null && file != null && file.exists()) 
         {
             url = file.toURL();
             try
             {
                 is = new FileInputStream(file);
             }
             catch (FileNotFoundException e)
             {
                 throw new XFireRuntimeException("File was deleted! " + uriStr, e);
             }
         }
         else if (is == null)
         {
             url = ClassLoaderUtils.getResource(uriStr, getClass());
             
             if (url == null)
             {
                 try 
                 {
                     url = new URL(uriStr);
                     is = url.openStream();
                 }
                 catch (MalformedURLException e)
                 {
                 }
             }
             else
             {
                 is = url.openStream();
             }
         }
         
         if (is == null) 
             throw new IOException("Could not find resource '" + uriStr + 
                                   "' relative to '" + baseUriStr + "'");
     }
     
     public URL getURL()
     {
         return url;
     }
     
     public InputStream getInputStream()
     {
         return is;
     }
     
     public boolean isFile()
     {
         return file.exists();
     }
     
     public File getFile()
     {
         return file;
     }
 }
