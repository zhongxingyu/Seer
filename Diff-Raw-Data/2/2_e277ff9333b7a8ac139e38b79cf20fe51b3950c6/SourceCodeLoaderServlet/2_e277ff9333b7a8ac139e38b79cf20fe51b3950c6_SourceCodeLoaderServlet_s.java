 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icemobile.samples.mobileshowcase.util;
 
 
 import com.uwyn.jhighlight.renderer.XhtmlRendererFactory;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * <p>The SourceCodeLoaderServlet class is responsible for displaying the JSF
  * source code for a particular example. </p>
  *
  * @since 0.0.1
  */
 public class SourceCodeLoaderServlet extends HttpServlet {
 
     private static final Pattern JSPX_PATTERN =
             Pattern.compile("<!--.*?-->", Pattern.DOTALL);
     private static final Pattern JAVA_PATTERN =
             Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
 
 
     public void doGet(HttpServletRequest request,
                       HttpServletResponse response) {
 
         // contains the relative path to where the source code for the example
         // is on the server
         String sourcePath = request.getParameter("path");
 
         if (sourcePath != null) {
             InputStream sourceStream =
                     getServletContext().getResourceAsStream(sourcePath);
 
             if (sourceStream == null) {
                 try {
                     // Work around for websphere
                     sourceStream = new FileInputStream(new File(
                             getServletContext().getRealPath(sourcePath)));
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
 
             if (sourceStream != null) {
                 PrintWriter responseStream;
                 try {
                     // Setting the context type to text/xml provides style
                     // attributes for most browsers which should make reading
                     // the code easier.
                     response.setContentType("text/html");
                     responseStream = response.getWriter();
                     StringBuffer stringBuffer = new StringBuffer();
                     int ch;
                     while ((ch = sourceStream.read()) != -1) {
                         stringBuffer.append((char) ch);
                     }
                     // Remove the license from the source code
                     Matcher m = JSPX_PATTERN.matcher(stringBuffer);
 
                     String toReturn;
                     if (m.find(0)) {
                         toReturn = m.replaceFirst(
                                 "// Apache License, Version 2.0 (see http://www.apache.org/licenses/LICENSE-2.0)\n\n" +
                                 "// Click or touch the screen to navigate back to ICEmobile Suite\n" );
                     } else {
                         m = JAVA_PATTERN.matcher(stringBuffer);
                         toReturn = m.replaceFirst(
                                 "/*  Apache License, Version 2.0 (see http://www.apache.org/licenses/LICENSE-2.0) */\n\n" +
                                 "/* Click or touch the screen to navigate back to ICEmobile Suite */\n" );
                     }
                     String name = sourcePath
                             .substring(sourcePath.lastIndexOf("/") + 1);
                     String type = "";
                     if (sourcePath.endsWith(".java")) {
                         type = XhtmlRendererFactory.JAVA;
                     } else if (sourcePath.endsWith(".jspx")) {
                         type = XhtmlRendererFactory.XHTML;
                     } else if (sourcePath.endsWith(".xhtml")) {
                         type = XhtmlRendererFactory.XHTML;
                     }
                     String toReturnHigh =
                             XhtmlRendererFactory.getRenderer(type)
                                     .highlight(name, toReturn, "utf8", false);
                     String contextPath = getServletContext().getContextPath();
 
                     toReturnHigh =  toReturnHigh.replaceFirst(
                             "<body>", "<body> <div onclick='window.location.href=\"" +
                            contextPath + "\";'>" );
                     toReturnHigh =  toReturnHigh.replaceFirst(
                             "</body>", "</div></body>" );
                     responseStream.print(toReturnHigh);
                     responseStream.close();
                     sourceStream.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
 
             }
         }
     }
 
 }
