 /*
  * Copyright 2009 Google Inc.
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
 package com.google.jstestdriver;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
  */
 public class HeartbeatServlet extends HttpServlet {
 
   private static final long serialVersionUID = 5484417807218095115L;
 
   private static final long TIMEOUT = 15000L; // 15 seconds
   private final CapturedBrowsers capturedBrowsers;
   private final Time time;
 
   public HeartbeatServlet(CapturedBrowsers capturedBrowsers, Time time) {
     this.capturedBrowsers = capturedBrowsers;
     this.time = time;
   }
 
  /**
   * Used by the client to know if the browser is alive.
   */
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     PrintWriter writer = resp.getWriter();
     String id = req.getParameter("id");
     if (id != null) { 
       SlaveBrowser browser = capturedBrowsers.getBrowser(id);
       if (browser != null) {
         if ((time.now().getMillis() - browser.getLastHeartBeat().getMillis()) > TIMEOUT) {
           capturedBrowsers.removeSlave(id);
           writer.write("DEAD");
         } else {
           writer.write("OK");
         }
       } else {
        writer.write("DEAD");
       }
     } else {
      writer.write("DEAD");
     }
     writer.flush();
   }
 
  /**
   * Used by the browser to report if it is still alive.
   */
   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     String id = req.getParameter("id");
     SlaveBrowser browser = capturedBrowsers.getBrowser(id);
 
     if (browser != null) {
       browser.heartBeat();
     }
     resp.getWriter().flush();
   }
 }
