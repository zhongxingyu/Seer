 /*
  * Copyright 2013 Dolf Dijkstra. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.fatwire.gst.web.status.servlet;
 
 import java.io.IOException;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.fatwire.gst.web.status.ConcurrencyCounterLocator;
 import com.fatwire.gst.web.status.RequestCounter;
 import com.fatwire.gst.web.status.StatusRequestListener;
 
 public class StatusServlet extends HttpServlet {
 
     /**
      * 
      */
     private static final long serialVersionUID = -1050326942624090518L;
     private StatusController controller;
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         try {
             controller.handleRequest(req, resp).render(req, resp);
         } catch (IOException e) {
             throw e;
         } catch (ServletException e) {
             throw e;
         } catch (Throwable e) {
             throw new ServletException(e);
 
         }
     }
 
     @Override
     protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
     }
 
     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
     }
 
     @Override
     protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
     }
 
     @Override
     protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
     }
 
     @Override
     protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
     }
 
     @Override
     protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
     }
 
     @Override
     public void destroy() {
         ConcurrencyCounterLocator.getInstance().deregister(controller.getRequestCounter());
         controller = null;
         super.destroy();
     }
 
     @Override
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
 
        RequestCounter requestCounter = (RequestCounter) ConcurrencyCounterLocator.getInstance().locate(
                config.getServletContext().getServletContextName());
 
         if (requestCounter == null)
             throw new ServletException("requestCounter is not registered. Is the "
                     + StatusRequestListener.class.getName() + " registered in web.xml");
         controller = new StatusController();
 
         controller.setRequestCounter(requestCounter);
         controller.setServletContext(getServletContext());
         boolean extendedInfo = config.getInitParameter("extended-info") == null ? true : Boolean.parseBoolean(config
                 .getInitParameter("extended-info"));
         controller.setExtendedInfo(extendedInfo);
 
     }
 
 }
