 /**
  * Copyright (C) 2011 Justin Lee <jlee@antwerkz.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.miloframework;
 
 import java.io.IOException;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public abstract class ServletContainer {
     private final Map<String, MiloServletContext> contexts = new ConcurrentHashMap<>();
     protected String host;
     protected int port;
 
     public ServletContainer(String host, int port) {
         this.host = host;
         this.port = port;
     }
 
     public abstract void start() throws IOException;
 
     public abstract void stop();
 
     public MiloServletContext createContext(String name, String path, String root) throws ServletException {
        final MiloServletContext context = new MiloServletContext(this, name, path, root);
         final MiloServletContext put = contexts.put(path, context);
         return put == null ? context : put;
     }
 
     protected void service(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException {
         final MiloServletContext context = getContext(request.getRequestURI());
         context.service(request, response);
     }
 
     public MiloServletContext getContext(String uriPath) {
         MiloServletContext context = contexts.get(uriPath);
         final String[] pathElements = uriPath.split("/");
         int index = pathElements.length - 1;
         while (context == null && index > 0) {
             context = contexts.get(join(pathElements, index--));
         }
         return context;
     }
 
     private String join(String[] pathElements, int length) {
         StringBuilder builder = new StringBuilder();
         for (int index = 0; index < length; index++) {
             builder
                 .append(pathElements[index])
                 .append("/");
         }
         return builder.toString();
     }
 
     public abstract String getMimeType(String file);
 
     @Override
     public String toString() {
         return String.format("%s[%s:%s]", getClass().getSimpleName(), host, port);
     }
 }
