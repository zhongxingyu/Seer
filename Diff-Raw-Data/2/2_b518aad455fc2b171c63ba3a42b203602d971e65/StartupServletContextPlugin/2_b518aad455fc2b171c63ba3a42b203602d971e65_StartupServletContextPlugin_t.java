 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.myfaces.scripting.jsf;
 
 import org.apache.myfaces.scripting.core.util.WeavingContext;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import java.util.logging.Logger;
 
 /**
  * Servlet context plugin which provides a cleaner initialisation
  * than the standard servlet context
  *
  * @author Werner Punz
 * @deprecated the plugin chain loader takes care
 * of it
  */
 public class StartupServletContextPlugin implements ServletContextListener {
     public void contextInitialized(ServletContextEvent servletContextEvent) {
         Logger log = Logger.getLogger(this.getClass().getName());
 
         log.info("[EXT-SCRIPTING] Instantiating StartupServletContextPluginChainLoader");
 
         ServletContext servletContext = servletContextEvent.getServletContext();
         if (servletContext == null) return;
 
     }
 
     public void contextDestroyed(ServletContextEvent servletContextEvent) {
 
     }
 
 }
