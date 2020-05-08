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
     private URI uri;
     private InputStream is;
     
     public Resolver(String path) throws IOException
     {
         this("", path);
     }
     
     public Resolver(String baseUriStr, String uriStr) 
         throws IOException
     {
         try 
         {
             URI relative;
             File uriFile = new File(uriStr);
             uriFile = new File(uriFile.getAbsolutePath());
             if (uriFile.exists())
             {
                 relative = uriFile.toURI();
             }
             else
                 relative = new URI(uriStr);
 
             if (relative.isAbsolute())
             {
                 uri = relative;
                 is = relative.toURL().openStream();
             }
             else if (baseUriStr != null)
             {
                 URI base;
                 File baseFile = new File(baseUriStr);
                 if (baseFile.exists())
                     base = baseFile.toURI();
                 else
                     base = new URI(baseUriStr);
                 
                 base = base.resolve(relative);
                 if (base.isAbsolute())
                 {
                     is = base.toURL().openStream();
                     uri = base;
                 }
             }
         } catch (URISyntaxException e) {
         }
         
         if (uri != null && "file".equals(uri.getScheme()))
         {
             file = new File(uri);
         }
         
         if (is == null && file != null && file.exists()) 
         {
             uri = file.toURI();
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
             URL url = ClassLoaderUtils.getResource(uriStr, getClass());
             
             if (url == null)
             {
                 try 
                 {
                     url = new URL(uriStr);
                    uri = url.toURI();
                     is = url.openStream();
                 }
                 catch (MalformedURLException e)
                 {
                 }
                 catch (URISyntaxException e)
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
     
     public URI getURI()
     {
         return uri;
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
