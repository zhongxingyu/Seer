 /**
  * SAHARA Scheduling Server
  *
  * Schedules and assigns local laboratory rigs.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2010, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 8th February 2010
  */
 
 package au.edu.uts.eng.remotelabs.schedserver.server;
 
 import java.util.Properties;
 
 import org.apache.velocity.app.Velocity;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceEvent;
 import org.osgi.framework.ServiceReference;
 import org.osgi.util.tracker.ServiceTracker;
 
 import au.edu.uts.eng.remotelabs.schedserver.config.Config;
 import au.edu.uts.eng.remotelabs.schedserver.logger.Logger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.LoggerActivator;
 import au.edu.uts.eng.remotelabs.schedserver.server.impl.PageHostingServiceListener;
 import au.edu.uts.eng.remotelabs.schedserver.server.impl.ServerImpl;
 import au.edu.uts.eng.remotelabs.schedserver.server.impl.ServerServiceListener;
 
 /**
  * Activator for the Server bundle which is a Jetty server to host the Axis2
  * SOAP interface.
  */
 public class ServerActivator implements BundleActivator 
 {
     /** The server implementation. */
     private ServerImpl server;
     
     /** Configuration tracker. */
     private static ServiceTracker<Config, Config> configTracker;
     
     /** Logger. */
     private Logger logger;
 
     @Override
     public void start(final BundleContext context) throws Exception
     {
         this.logger = LoggerActivator.getLogger();
         
         ServerActivator.configTracker = new ServiceTracker<Config, Config>(context, Config.class, null);
         ServerActivator.configTracker.open();
         
         this.server = new ServerImpl(context);
         this.server.init();
         
         /* Register a service listener for Servlet services. */
         final ServerServiceListener servListener = new ServerServiceListener(this.server);
         context.addServiceListener(servListener, 
                 '(' + Constants.OBJECTCLASS + '=' + ServletContainerService.class.getName() + ')');
         for (ServiceReference<ServletContainerService> ref :  
                 context.getServiceReferences(ServletContainerService.class, null))
         {
             this.logger.debug("Firing registered event for servlet service from bundle " + 
                     ref.getBundle().getSymbolicName() + '.');
             servListener.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, ref));
         }
         
         final PageHostingServiceListener pageListener = new PageHostingServiceListener(context);
         context.addServiceListener(pageListener,
                 '(' + Constants.OBJECTCLASS + '=' + HostedPage.class.getName() + ')');
         for (ServiceReference<HostedPage> ref : context.getServiceReferences(HostedPage.class, null))
         {
             this.logger.debug("Firing registered event for hosted page from bundle " + 
                     ref.getBundle().getSymbolicName() + '.');
             pageListener.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, ref));
         }
         
         /* Setup Veloctiy to use a custom resource loader. */
 	    Properties velProps = new Properties();
 	    velProps.setProperty("resource.loader", "class");
 	    velProps.setProperty("class.resource.loader.class", "au.edu.uts.eng.remotelabs.schedserver.server.impl.BundleResourceLoader");
	    velProps.setProperty("velocimacro.library=", ""); // Reset default macro library to stop ERROR log 
 	    Velocity.init(velProps);
         
         /* Start the server. */
         this.server.start();
     }
 
     @Override
     public void stop(final BundleContext context) throws Exception
     {
         /* The framework will cleanup any services in use and remove the service listener. */
         this.logger.debug("Stopping the server bundle.");
         this.server.stop();
         
         ServerActivator.configTracker.close();
         ServerActivator.configTracker = null;
     } 
     
     /**
      * Gets a configuration service instance. If this bundle is not running or 
      * the service could not be found <tt>null</tt> is returned.
      * 
      * @return configuration service instance or null
      */
     public static Config getConfig()
     {
         if (ServerActivator.configTracker == null) return null;
         
         return ServerActivator.configTracker.getService();
     }
 }
