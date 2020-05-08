 /*
  * Copyright (c) 2010 Sharegrove Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package org.msjs.servlet;
 
 import com.google.inject.Injector;
 import org.apache.commons.io.IOUtils;
 import org.msjs.config.MsjsConfiguration;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 
 public class FileServlet extends ResourceServlet {
 
     private ServletContext context;
     private String fileRoot;
 
     @Override
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
         context = config.getServletContext();
         Injector injector = (Injector) context.getAttribute(ServletListener.INJECTOR);
         fileRoot = injector.getInstance(MsjsConfiguration.class).getScriptRoot();
     }
 
     @Override
     protected void handleRequest(final HttpServletRequest request,
                                  final HttpServletResponse response) throws IOException {
         final String contentType = context.getMimeType(getFilePath(request));
         if (contentType != null) response.setHeader("Content-Type", contentType);
         IOUtils.copy(getInputStream(request), response.getOutputStream());
     }
 
     private InputStream getInputStream(final HttpServletRequest request)
             throws IOException {
         final File file = getFile(request);
         if (file.exists()) return new FileInputStream(file);
 
        final String path = getFilePath(request);
         try{
            final URL url = FileServlet.class.getResource(path);
            if (url == null) throw new IOException(path);
             URLConnection connection = url.openConnection();
             return connection.getInputStream();
         } catch (IOException e){
            throw new FileNotFoundException(path);
         }
 
     }
 
 
     @Override
     public long getLastModified(final HttpServletRequest request) {
         return getFile(request).lastModified();
     }
 
     private File getFile(HttpServletRequest request) {
         return new File(getFilePath(request));
     }
 
     private String getFilePath(final HttpServletRequest request) {
         return fileRoot + request.getPathInfo();
     }
 
 }
