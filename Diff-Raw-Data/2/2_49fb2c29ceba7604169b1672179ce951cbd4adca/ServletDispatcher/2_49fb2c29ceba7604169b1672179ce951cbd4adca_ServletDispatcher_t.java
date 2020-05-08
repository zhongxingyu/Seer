 /*
  *  Copyright 2010 mathieuancelin.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 package cx.ath.mancel01.webframework.integration.servlet;
 
 import cx.ath.mancel01.dependencyshot.graph.Binder;
 import cx.ath.mancel01.webframework.FrameworkHandler;
 import cx.ath.mancel01.webframework.WebFramework;
 import cx.ath.mancel01.webframework.http.Request;
 import cx.ath.mancel01.webframework.http.Response;
 import cx.ath.mancel01.webframework.util.FileUtils.FileGrabber;
 import java.io.File;
 import java.io.IOException;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author mathieuancelin
  */
 public class ServletDispatcher extends HttpServlet {
 
     private FrameworkHandler dispatcher;
 
     @Override
     public void init() throws ServletException {
         super.init();
         String configClassName = getServletContext().getInitParameter("config");
         if (configClassName == null) {
             throw new RuntimeException("No binder registered ...");
         }
         final ServletContext context = this.getServletContext();
         dispatcher = new FrameworkHandler(configClassName,
             getServletContext().getContextPath()
             , new File(getServletContext().getRealPath("")), new FileGrabber() {
 
                 @Override
                 public File getFile(String file) {
                    return new File(getServletContext().getRealPath("src/main/webapp/views/" + file));
                 }
 
             });
         dispatcher.start();
     }
 
     @Override
     public void destroy() {
         super.destroy();
         dispatcher.stop();
     }
     
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         WebFramework.logger.trace("start processing request ...");
         long start = System.currentTimeMillis();
         try {
             // process in a thread ?
             Request req = ServletBinder.extractRequest(request);
             Request.current.set(req);
             Response res = dispatcher.process(req);
             Response.current.set(res);
             ServletBinder.flushResponse(req, res, request, response);
         } catch (Exception e) {
             e.printStackTrace(); // TODO : print something useless here
         } finally {
             Request.current.remove();
             Response.current.remove();
             WebFramework.logger.trace("request processed in {} ms.\n", (System.currentTimeMillis() - start));
             WebFramework.logger.trace("=======================================\n");
         }
     }
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     @Override
     protected void doDelete(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     @Override
     protected void doPut(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     @Override
     public String getServletInfo() {
         return "web-framework servlet";
     }   
 }
