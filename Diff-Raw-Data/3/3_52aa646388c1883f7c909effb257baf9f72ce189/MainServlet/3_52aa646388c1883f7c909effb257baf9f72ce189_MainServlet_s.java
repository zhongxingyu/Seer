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
  *
  */
 package org.icepush.servlet;
 
 import org.icepush.*;
 import org.icepush.http.standard.CacheControlledServer;
 import org.icepush.http.standard.CompressingServer;
 import org.icepush.util.ExtensionRegistry;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.net.SocketException;
 import java.net.URI;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Timer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class MainServlet implements PseudoServlet {
     private static final Logger log = Logger.getLogger(MainServlet.class.getName());
     static HashSet<TraceListener> traceListeners = new HashSet();
     protected PushGroupManager pushGroupManager;
     protected PathDispatcher dispatcher;
     protected Timer monitoringScheduler;
     protected PushContext pushContext;
     protected ServletContext context;
     protected Configuration configuration;
     protected boolean terminateConnectionOnShutdown;
 
     public MainServlet(final ServletContext context) {
         this(context, true);
     }
 
     public MainServlet(final ServletContext servletContext,
                        final boolean terminateBlockingConnectionOnShutdown) {
         this(servletContext,terminateBlockingConnectionOnShutdown,true);
     }
 
     public MainServlet(final ServletContext servletContext,
                        final boolean terminateBlockingConnectionOnShutdown,
                        final boolean printProductInfo) {
 
         //We print the product info unless we are part of EE which will print out it's
         //own version.
         if(printProductInfo){
             log.info(new ProductInfo().toString());
         }
 
         context = servletContext;
         terminateConnectionOnShutdown = terminateBlockingConnectionOnShutdown;
         monitoringScheduler = new Timer("Monitoring scheduler", true);
         configuration = new ServletContextConfiguration("org.icepush", context);
         pushContext = new PushContext(context);
         pushGroupManager = PushGroupManagerFactory.newPushGroupManager(context, configuration);
         pushContext.setPushGroupManager(pushGroupManager);
         dispatcher = new PathDispatcher();
         new DefaultOutOfBandNotifier(servletContext);
 
         addDispatches();
     }
 
     protected void addDispatches() {
        dispatchOn(".*code\\.icepush", new BasicAdaptingServlet(new CacheControlledServer(new CompressingServer(new CodeServer("icepush.js"))), configuration));
         dispatchOn(".*", new BrowserDispatcher(configuration) {
             protected PseudoServlet newServer(String browserID) {
                 return createBrowserBoundServlet(browserID);
             }
         });
     }
 
     protected PseudoServlet createBrowserBoundServlet(String browserID) {
         return new BrowserBoundServlet(pushContext, context, pushGroupManager, monitoringScheduler, configuration, terminateConnectionOnShutdown);
     }
 
     public void dispatchOn(String pattern, PseudoServlet servlet) {
         dispatcher.dispatchOn(pattern, servlet);
     }
 
     public PushGroupManager getPushGroupManager() {
         return pushGroupManager;
     }
 
     public void service(HttpServletRequest request,
                         HttpServletResponse response) throws Exception {
         try {
             dispatcher.service(request, response);
         } catch (SocketException e) {
             if ("Broken pipe".equals(e.getMessage())) {
                 // client left the page
                 if (log.isLoggable(Level.FINEST)) {
                     log.log(Level.FINEST, "Connection broken by client.", e);
                 } else if (log.isLoggable(Level.FINE)) {
                     log.log(Level.FINE, "Connection broken by client: " + e.getMessage());
                 }
             } else {
                 throw new ServletException(e);
             }
         } catch (RuntimeException e) {
             //Tomcat won't properly redirect to the configured error-page.
             //So we need a new RuntimeException that actually includes a message.
             if (e.getMessage() == null) {
                 throw new RuntimeException("wrapped Exception: " + e, e);
             } else {
                 throw e;
             }
         } catch (Exception e) {
             throw new ServletException(e);
         }
     }
 
     public void shutdown() {
         dispatcher.shutdown();
         pushGroupManager.shutdown();
         monitoringScheduler.cancel();
     }
 
     public static void trace(String message)  {
         for (TraceListener listener : traceListeners)  {
             listener.handleTrace(message);
         }
     }
 
     public static void addTraceListener(TraceListener listener)  {
         traceListeners.add(listener);
     }
 
     //Application can add itself as a TraceListener to receive
     //diagnostic message callbacks when cloud push occurs
     public interface TraceListener  {
         public void handleTrace(String message);
     }
 
     public static class ExtensionRegistration implements
             ServletContextListener {
         public void contextInitialized(ServletContextEvent servletContextEvent) {
             ExtensionRegistry.addExtension(servletContextEvent.getServletContext(), 1, "org.icepush.MainServlet", MainServlet.class);
         }
 
         public void contextDestroyed(ServletContextEvent servletContextEvent) {
         }
     }
 
     private static class DefaultOutOfBandNotifier implements OutOfBandNotifier {
         private final Logger log = Logger.getLogger(OutOfBandNotifier.class.getName());
         private HashMap providers = new HashMap();
 
         private DefaultOutOfBandNotifier(ServletContext context) {
             context.setAttribute(OutOfBandNotifier.class.getName(), this);
             Object[] extensions = ExtensionRegistry.getExtensions(context, NotificationProvider.class.getName());
             if (extensions == null) {
                 MainServlet.log.fine("Could not find any out of band notification providers.");
             } else {
                 for (int i = 0; i < extensions.length; i++) {
                     NotificationProvider provider = (NotificationProvider) extensions[i];
                     provider.registerWith(this);
                 }
             }
         }
 
         public void broadcast(PushNotification notification, String[] uris) {
             for (int i = 0; i < uris.length; i++) {
                 String notifyURI = uris[i];
                 URI uri = URI.create(notifyURI);
                 String protocol = uri.getScheme();
                 NotificationProvider provider = (NotificationProvider) providers.get(protocol);
                 if (provider == null) {
                     log.warning("No notification providers for '" + uri + "' URI registered");
                 } else {
                     try {
                         provider.send(notifyURI, notification);
                     } catch (Throwable t) {
                         log.log(Level.WARNING, "Exception sending message to " + notifyURI + ", " + t);
                     }
                 }
             }
 
         }
 
         public void registerProvider(String protocol,
                                      NotificationProvider provider) {
             providers.put(protocol, provider);
         }
         
         public void trace(String message)  {
             MainServlet.trace(message);
         }
     }
 }
