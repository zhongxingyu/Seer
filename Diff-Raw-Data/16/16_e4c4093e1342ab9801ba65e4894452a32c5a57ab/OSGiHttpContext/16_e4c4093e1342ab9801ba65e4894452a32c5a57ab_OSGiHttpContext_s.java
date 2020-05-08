 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2011, Red Hat Middleware LLC, and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package devoxx.core.fwk;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.osgi.service.http.HttpContext;
 
 public class OSGiHttpContext implements HttpContext {
 
     private final ClassLoader loader;
     
     private final File devPath;
     
     private final boolean devMode;
 
     public OSGiHttpContext(ClassLoader loader) {
         this.loader = loader;
         String path = System.getProperty("devpath", "none");
         if (path.equals("none")) {
             devMode = false;
             devPath = new File("/dev/null");
         } else if (path.trim().equals("")) {
             devMode = false;
             devPath = new File("/dev/null");
         } else {
             devMode = true;
             devPath = new File(path);
             if (!devPath.exists()) {
                 throw new RuntimeException(devPath.getAbsolutePath() + " doesn't exists.");
             } 
         }
         if (devMode) {
            SimpleLogger.info("Application started in dev mode");
             SimpleLogger.info("Static resources will be dynamically loaded from {}", devPath.getAbsolutePath());
         }
     }
 
     @Override
     public boolean handleSecurity(HttpServletRequest request,
             HttpServletResponse response) throws IOException {
         return true; // TODO support pluggable security
     }
 
     @Override
     public URL getResource(String name) {
         String actualName = name.replace("tmp/", "");
         if (devMode) {
             try {
                 URL url = new File(devPath, actualName).toURI().toURL();
                 SimpleLogger.trace("Loading resource dynamically {}", url.toString());
                 return url;
             } catch (MalformedURLException ex) {
                 throw new RuntimeException(ex);
             }
         } else {
             return loader.getResource(actualName);
         }
     }
 
     @Override
     public String getMimeType(String name) {
         if (name.endsWith(".css")) {
             return "text/css";
         }
         if (name.endsWith(".js")) {
             return "text/javascript";
         }
         return "*"; // TODO map with real types
     }
 }
