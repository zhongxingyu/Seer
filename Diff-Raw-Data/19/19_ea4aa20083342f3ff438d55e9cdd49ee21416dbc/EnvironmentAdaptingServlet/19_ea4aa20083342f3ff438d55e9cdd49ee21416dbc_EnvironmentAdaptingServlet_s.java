 /*
  * Version: MPL 1.1
  *
  * "The contents of this file are subject to the Mozilla Public License
  * Version 1.1 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations under
  * the License.
  *
  * The Original Code is ICEfaces 1.5 open source software code, released
  * November 5, 2006. The Initial Developer of the Original Code is ICEsoft
  * Technologies Canada, Corp. Portions created by ICEsoft are Copyright (C)
  * 2004-2010 ICEsoft Technologies Canada, Corp. All Rights Reserved.
  *
  * Contributor(s): _____________________.
  *
  */
 
 package org.icepush.servlet;
 
 import org.icepush.Configuration;
 import org.icepush.http.Server;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class EnvironmentAdaptingServlet implements PseudoServlet {
     private static Logger log = Logger.getLogger(EnvironmentAdaptingServlet.class.getName());
     private PseudoServlet servlet;
     private Server server;
 
     public EnvironmentAdaptingServlet(final Server server, final Configuration configuration) {
         this.server = server;
         if (configuration.getAttributeAsBoolean("useAsyncContext", isAsyncARPAvailable())) {
             log.log(Level.INFO, "Adapting to Servlet 3.0 AsyncContext environment");
            servlet = new AsyncAdaptingServlet(server);
         } else {
             log.log(Level.INFO, "Adapting to Thread Blocking environment");
             servlet = new ThreadBlockingAdaptingServlet(server);
         }
     }
 
     public void service(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
         try {
             servlet.service(request, response);
         } catch (EnvironmentAdaptingException exception) {
             log.log(Level.INFO, "Falling back to Thread Blocking environment");
             servlet = new ThreadBlockingAdaptingServlet(server);
             servlet.service(request, response);
         }
     }
 
     public void shutdown() {
         servlet.shutdown();
     }
 
     private boolean isAsyncARPAvailable() {
         try {
             this.getClass().getClassLoader().loadClass("javax.servlet.AsyncContext");
             return true;
         } catch (ClassNotFoundException exception) {
             return false;
         }
     }
 }
