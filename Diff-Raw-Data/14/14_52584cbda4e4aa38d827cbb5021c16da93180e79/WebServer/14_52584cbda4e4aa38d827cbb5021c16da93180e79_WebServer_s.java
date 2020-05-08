 package com.yoursway.autoupdater.tests.internal.server;
 
 import static com.google.common.collect.Lists.newArrayList;
 import static com.google.common.collect.Maps.newHashMap;
 import static com.yoursway.utils.YsFileUtils.saveToFile;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.Map;
 
 import com.yoursway.utils.StringInputStream;
 
 public class WebServer {
     
     private final static int PORT = 8744;
     private final SimpleHttpServer server;
     
     private final Map<String, String> mountedStrings = newHashMap();
     
     private final Map<String, byte[]> mountedBytes = newHashMap();
     
     private final Collection<String> notFoundPaths = newArrayList();
     
     public WebServer() {
         SimpleServlet servlet = new SimpleServlet() {
             
             public void log(String s2) {
                 //System.out.println(s2);
             }
             
             public InputStream openFile(String path) throws IOException {
                 String value = mountedStrings.get(path);
                 if (value != null)
                     return new StringInputStream(value);
                 byte[] bytes = mountedBytes.get(path);
                 if (bytes != null)
                     return new ByteArrayInputStream(bytes);
                 notFoundPaths.add(path);
                 throw new IOException("404, blya: " + path);
             }
             
         };
         server = new SimpleHttpServer(PORT, servlet);
     }
     
     @SuppressWarnings("deprecation")
     public void dispose() {
         server.stop();
     }
     
     public int getPort() {
         return PORT;
     }
     
     public void mount(String path, String value) {
         saveForDebugging(path, new StringInputStream(value));
         mountedStrings.put(path, value);
     }
     
     private void saveForDebugging(String path, InputStream inputStream) {
         File file = new File(new File("/tmp/foo"), path);
         file.getParentFile().mkdirs();
         try {
             saveToFile(inputStream, file);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
     
     public void mount(String path, byte[] value) {
         saveForDebugging(path, new ByteArrayInputStream(value));
         mountedBytes.put(path, value);
     }
     
     public boolean requestDetected(String subpath) {
         for (String path : notFoundPaths)
             if (path.contains(subpath))
                 return true;
         return false;
     }
     
 }
