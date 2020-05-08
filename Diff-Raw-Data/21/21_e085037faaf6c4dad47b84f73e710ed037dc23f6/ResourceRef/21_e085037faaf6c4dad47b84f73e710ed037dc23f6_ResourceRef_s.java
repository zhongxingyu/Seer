 package org.jboss.test.qunit;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 
 public class ResourceRef extends URLRefBase implements ScriptRef {
 
     private final String src;
 
     public ResourceRef(String src) {
         this.src = src;
     }
 
     public URL getScript(Object base) {
        return base.getClass().getResource(src);
     }
 
     public String getContent(Object base) {
         try {
             URLConnection connection = getScript(base).openConnection();
             connection.setUseCaches(false);
             InputStream inputStream = connection.getInputStream();
             return readInputStream(inputStream);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
 }
