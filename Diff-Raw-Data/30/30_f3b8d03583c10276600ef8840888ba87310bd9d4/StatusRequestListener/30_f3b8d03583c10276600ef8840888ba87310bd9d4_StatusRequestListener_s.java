 /*
  * Copyright 2006 Dolf Dijkstra. All Rights Reserved.
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
 package com.fatwire.gst.web.status;
 
 import java.lang.management.ManagementFactory;
 
 import javax.management.ObjectName;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import javax.servlet.ServletRequestEvent;
 import javax.servlet.ServletRequestListener;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.fatwire.gst.web.status.jmx.StatusCounter;
 
 /**
 * ServletRequestListener that signals the {@link RequestCounter} when requests start and end. Registers the {@link StatusCounter} as a JMX MBean.
  * 
  * @author Dolf.Dijkstra
  * @since Jun 10, 2010
  */
 
 public class StatusRequestListener implements ServletRequestListener, ServletContextListener {
 
     private Log log = LogFactory.getLog(StatusRequestListener.class);
 
     private RequestCounter requestCounter;
 
     private ObjectName name;
 
     public void requestDestroyed(ServletRequestEvent event) {
         if (event.getServletRequest() instanceof HttpServletRequest)
             try {
                 requestCounter.end((HttpServletRequest) event.getServletRequest());
             } catch (Throwable e) {
                 log.warn(e.getMessage(), e);
             }
 
     }
 
     public void requestInitialized(ServletRequestEvent event) {
         if (event.getServletRequest() instanceof HttpServletRequest)
             try {
                 requestCounter.start((HttpServletRequest) event.getServletRequest());
             } catch (Throwable e) {
                 log.warn(e.getMessage(), e);
             }
 
     }
 
     public void contextDestroyed(ServletContextEvent sce) {
         requestCounter = null;
         try {
             ManagementFactory.getPlatformMBeanServer().unregisterMBean(name);
         } catch (Throwable e) {
             log.info(e.getMessage(), e);
         }
 
     }
 
     public void contextInitialized(ServletContextEvent sce) {
        requestCounter = new RequestCounter(sce.getServletContext().getServletContextName());
         ConcurrencyCounterLocator.getInstance().register(requestCounter);
 
         try {
            name = new ObjectName("com.fatwire.gst.web:type=RequestCounter,name="
                    + sce.getServletContext().getServletContextName());
             ManagementFactory.getPlatformMBeanServer().registerMBean(new StatusCounter(requestCounter), name);
         } catch (Throwable e) {
             log.warn(e.getMessage(), e);
         }
 
     }

 }
