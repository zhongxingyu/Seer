 /**
  * Copyright (C) 2012 Ness Computing, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.nesscomputing.httpserver.jetty;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.eclipse.jetty.http.HttpHeaders;
 import org.eclipse.jetty.http.HttpMethods;
 import org.eclipse.jetty.http.MimeTypes;
 import org.eclipse.jetty.io.Buffer;
 import org.eclipse.jetty.io.WriterOutputStream;
 import org.eclipse.jetty.server.AbstractHttpConnection;
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.server.handler.AbstractHandler;
 
 import com.google.common.io.ByteStreams;
 import com.google.inject.Inject;
 
 /**
  * Serves files from a given folder on the classpath through jetty.
  * Intended to serve a couple of static files e.g. for javascript or HTML.
  *
  * Needs to be configured programatically, can not be used in a jetty.xml file.
  */
 public class ClasspathResourceHandler extends AbstractHandler
 {
     private final String basePath;
     private final String resourceLocation;
 
     /** Record the startup time. The classpath resources will not change after this time, so the handler can return 304 if requested. */
     private final long startupTime = System.currentTimeMillis()/1000L;
 
     private static final MimeTypes MIME_TYPES;
 
     static {
         final MimeTypes mimeTypes = new MimeTypes();
         // Now here is an oversight... =:-O
         mimeTypes.addMimeMapping("json", "application/json");
         MIME_TYPES = mimeTypes;
     }
 
     @Inject
     public ClasspathResourceHandler(final String basePath, final String resourceLocation)
     {
         this.basePath = basePath;
         this.resourceLocation = resourceLocation;
     }
 
     @Override
     public void handle(final String target,
                        final Request baseRequest,
                        final HttpServletRequest request,
                        final HttpServletResponse response) throws IOException, ServletException
     {
         if (baseRequest.isHandled()) {
             return;
         }
 
         String pathInfo = request.getPathInfo();
 
         // Only serve the content if the request matches the base path.
         if (pathInfo == null || !pathInfo.startsWith(basePath)) {
             return;
         }
 
         pathInfo = pathInfo.substring(basePath.length());
 
         if (!pathInfo.startsWith("/") && !pathInfo.isEmpty()) {
             // basepath is /foo and request went to /foobar --> pathInfo starts with bar
             // basepath is /foo and request went to /foo --> pathInfo should be /index.html
             return;
         }
 
         // Allow index.html as welcome file
         if ("/".equals(pathInfo) || "".equals(pathInfo)) {
             pathInfo = "/index.html";
         }
 
         boolean skipContent = false;
 
         // When a request hits this handler, it will serve something. Either data or an error.
         baseRequest.setHandled(true);
 
         final String method = request.getMethod();
         if (!StringUtils.equals(HttpMethods.GET, method)) {
             if (StringUtils.equals(HttpMethods.HEAD, method)) {
                 skipContent = true;
             }
             else {
                 response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                 return;
             }
         }
 
         // Does the request contain an IF_MODIFIED_SINCE header?
         final long ifModifiedSince = request.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE);
        if (ifModifiedSince > 0 && startupTime/1000 <= ifModifiedSince/1000) {
             response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
             return;
         }
 
         InputStream resourceStream = null;
 
         try {
             if (pathInfo.startsWith("/")) {
                 final String resourcePath = resourceLocation + pathInfo;
                 resourceStream = getClass().getResourceAsStream(resourcePath);
             }
 
             if (resourceStream == null) {
                 response.sendError(HttpServletResponse.SC_NOT_FOUND);
                 return;
             }
 
             final Buffer mime = MIME_TYPES.getMimeByExtension(request.getPathInfo());
             if (mime != null) {
                 response.setContentType(mime.toString("ISO8859-1"));
             }
 
             response.setDateHeader(HttpHeaders.LAST_MODIFIED, startupTime*1000L);
 
             if (skipContent) {
                 return;
             }
 
             // Send the content out. Lifted straight out of ResourceHandler.java
 
             OutputStream out = null;
             try {
                 out = response.getOutputStream();
             }
             catch(IllegalStateException e) {
                 out = new WriterOutputStream(response.getWriter());
             }
 
             if (out instanceof AbstractHttpConnection.Output)
             {
                 ((AbstractHttpConnection.Output)out).sendContent(resourceStream);
             }
             else
             {
                 ByteStreams.copy(resourceStream, out);
             }
         } finally {
             IOUtils.closeQuietly(resourceStream);
         }
     }
 }
