 /*
  * Copyright 2005 John R. Fallows
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.java.dev.weblets.packaged;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 
 import java.net.URL;
 import java.net.URLConnection;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.java.dev.weblets.Weblet;
 import net.java.dev.weblets.WebletConfig;
 import net.java.dev.weblets.WebletContainer;
 import net.java.dev.weblets.WebletException;
 import net.java.dev.weblets.WebletRequest;
 import net.java.dev.weblets.WebletResponse;
 
 public class PackagedWeblet extends Weblet
 {
   public void init(
     WebletConfig config)
   {
     super.init(config);
     String packageName = config.getInitParameter("package");
     if (packageName == null)
       throw new WebletException("Missing init parameter \"package\" for " +
                                 " Weblet \"" + config.getWebletName() + "\"");
     _resourceRoot  = packageName.replace('.', '/');
   }
 
   public void service(
     WebletRequest  request,
     WebletResponse response) throws IOException
   {
     String resourcePath = _resourceRoot + request.getPathInfo();
 
     ClassLoader loader = Thread.currentThread().getContextClassLoader();
     URL url = loader.getResource(resourcePath);
 
     if (url != null)
     {
       URLConnection conn = url.openConnection();
       response.setLastModified(conn.getLastModified());
       response.setContentType(conn.getContentType());
      response.setContentLength(conn.getContentLength());
       response.setContentVersion(getWebletConfig().getWebletVersion());
 
       if (request.getIfModifiedSince() < conn.getLastModified())
       {
        InputStream in = conn.getInputStream();
        OutputStream out = response.getOutputStream();

         String contentType = response.getDefaultContentType();
         if (contentType.startsWith("text/") ||
             contentType.endsWith("xml"))
         {
           BufferedReader reader = new BufferedReader(new InputStreamReader(in));
          BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
 
           try
           {
             while (reader.ready())
             {
               String line = reader.readLine();
 
               // TODO: tidy up pattern matching, use leading single/double quote
               // TODO: conditional filtering
               int startAt = line.indexOf("weblet:/");
               if (startAt != -1)
               {
                 String protocol = line.substring(startAt);
                 Matcher matcher = _WEBLET_PROTOCOL.matcher(protocol);
                 if (matcher.matches())
                 {
                   String preamble   = line.substring(0, startAt);
                   String webletName = matcher.group(1);
                   // TODO: fixup pattern definition
                   String pathInfo   = "/" + matcher.group(2);
                   String postamble  = matcher.group(3);
 
                   // default relative weblet:/resource.ext to this weblet
                   if (webletName == null)
                     webletName = getWebletConfig().getWebletName();
 
                   WebletContainer container = WebletContainer.getInstance();
                   String webletURL = container.getWebletURL(webletName, pathInfo);
                   writer.write(preamble);
                   writer.write(request.getContextPath());
                   writer.write(webletURL);
                   writer.write(postamble);
                  writer.write('\n');
                 }
                 else
                 {
                   writer.write(line);
                  writer.write('\n');
                 }
               }
               else
               {
                 writer.write(line);
                writer.write('\n');
               }
             }
           }
           finally
           {
             reader.close();
             writer.close();
           }
         }
         else
         {
           // binary passthrough
           try
           {
             byte[] buffer = new byte[1024];
             while (in.available() > 0)
             {
               int len = in.read(buffer);
               out.write(buffer, 0, len);
             }
           }
           finally
           {
             in.close();
             out.close();
           }
         }
       }
     }
     else
     {
       response.setStatus(WebletResponse.SC_NOT_FOUND);
     }
   }
 
   public void destroy()
   {
     _resourceRoot = null;
     super.destroy();
   }
 
   private String _resourceRoot;
 
   private static final Pattern _WEBLET_PROTOCOL =
                           Pattern.compile("weblet:/(?:/([^/]+)/)?([^\"\\']+)(.*)");
 }
