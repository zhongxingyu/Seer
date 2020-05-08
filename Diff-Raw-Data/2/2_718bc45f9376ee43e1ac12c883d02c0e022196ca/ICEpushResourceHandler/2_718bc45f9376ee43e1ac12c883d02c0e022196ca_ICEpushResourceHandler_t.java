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
 
 package org.icefaces.impl.push.servlet;
 
 import org.icefaces.impl.event.DebugTagListener;
 import org.icefaces.util.EnvUtils;
 import org.icepush.PushContext;
 import org.icepush.servlet.MainServlet;
 import org.icepush.util.ExtensionRegistry;
 
 import javax.faces.FactoryFinder;
 import javax.faces.application.Resource;
 import javax.faces.application.ResourceHandler;
 import javax.faces.application.ResourceHandlerWrapper;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.event.PhaseEvent;
 import javax.faces.event.PhaseId;
 import javax.faces.event.PhaseListener;
 import javax.faces.event.PostAddToViewEvent;
 import javax.faces.lifecycle.Lifecycle;
 import javax.faces.lifecycle.LifecycleFactory;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 public class ICEpushResourceHandler extends ResourceHandlerWrapper implements PhaseListener {
     private static final Logger log = Logger.getLogger(ICEpushResourceHandler.class.getName());
     public static final String BLOCKING_CONNECTION_RESOURCE_NAME = "listen.icepush.xml";
     public static final String CREATE_PUSH_ID_RESOURCE_NAME = "create-push-id.icepush.txt";
     public static final String NOTIFY_RESOURCE_NAME = "notify.icepush.txt";
     public static final String ADD_GROUP_MEMBER_RESOURCE_NAME = "add-group-member.icepush.txt";
     public static final String REMOVE_GROUP_MEMBER_RESOURCE_NAME = "remove-group-member.icepush.txt";
     private static final Collection RESOURCES = Arrays.asList(
             BLOCKING_CONNECTION_RESOURCE_NAME,
             NOTIFY_RESOURCE_NAME,
             CREATE_PUSH_ID_RESOURCE_NAME,
             ADD_GROUP_MEMBER_RESOURCE_NAME,
             REMOVE_GROUP_MEMBER_RESOURCE_NAME
     );
 
     private static final ReentrantLock lock = new ReentrantLock();
     private static final Condition condition = lock.newCondition();
 
     private AbstractICEpushResourceHandler resourceHandler;
 
     public ICEpushResourceHandler(final ResourceHandler resourceHandler) {
         FacesContext facesContext = FacesContext.getCurrentInstance();
         final ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
         String projectStage = servletContext.getInitParameter("javax.faces.PROJECT_STAGE");
         if (projectStage != null && !projectStage.equals("Production")) {
             facesContext.getApplication().subscribeToEvent(PostAddToViewEvent.class, new DebugTagListener());
         }
         if (EnvUtils.isICEpushPresent()) {
             String serverInfo = servletContext.getServerInfo();
            if (!serverInfo.startsWith("JBoss Web/3.0.0-CR1")) {
                 this.resourceHandler = new ICEpushResourceHandlerImpl();
                 this.resourceHandler.initialize(resourceHandler, servletContext, this);
             } else {
                 final ICEpushResourceHandlerImpl impl = new ICEpushResourceHandlerImpl();
                 this.resourceHandler = new BlockingICEpushResourceHandlerWrapper(impl);
                 new Thread(
                         new Runnable() {
                             public void run() {
                                 ICEpushResourceHandler.this.resourceHandler.initialize(resourceHandler, servletContext, ICEpushResourceHandler.this);
                                 ICEpushResourceHandler.this.resourceHandler = impl;
                                 lock.lock();
                                 try {
                                     condition.signalAll();
                                 } finally {
                                     lock.unlock();
                                 }
                             }
                         }).start();
             }
         } else {
             this.resourceHandler = new ProxyICEpushResourceHandler(resourceHandler);
             log.log(Level.INFO, "Ajax Push Resource Handling not available.");
         }
     }
 
     public void afterPhase(final PhaseEvent event) {
         resourceHandler.afterPhase(event);
     }
 
     public void beforePhase(final PhaseEvent event) {
         resourceHandler.beforePhase(event);
     }
 
     public Resource createResource(final String resourceName, String libraryName, final String contentType) {
         return resourceHandler.createResource(resourceName, libraryName, contentType);
     }
 
     public PhaseId getPhaseId() {
         return resourceHandler.getPhaseId();
     }
 
     public ResourceHandler getWrapped() {
         return resourceHandler.getWrapped();
     }
 
     @Override
     public void handleResourceRequest(final FacesContext facesContext) throws IOException {
         resourceHandler.handleResourceRequest(facesContext);
     }
 
     @Override
     public boolean isResourceRequest(final FacesContext facesContext) {
         return resourceHandler.isResourceRequest(facesContext);
     }
 
     public static void notifyContextShutdown(ServletContext context) {
         try {
             ((ICEpushResourceHandler) context.getAttribute(ICEpushResourceHandler.class.getName())).shutdownMainServlet();
         } catch (Throwable t) {
             //no need to log this exception for optional Ajax Push, but may be
             //useful for diagnosing other failures
             log.log(Level.FINE, "MainServlet not found in application scope: " + t);
         }
     }
 
     private void shutdownMainServlet() {
         resourceHandler.shutdownMainServlet();
     }
 
     private static abstract class AbstractICEpushResourceHandler extends ResourceHandlerWrapper implements PhaseListener {
         abstract void initialize(ResourceHandler resourceHandler, ServletContext servletContext, ICEpushResourceHandler icePushResourceHandler);
 
         abstract void shutdownMainServlet();
     }
 
     private static class ICEpushResourceHandlerImpl extends AbstractICEpushResourceHandler {
         private static final Pattern ICEpushRequestPattern = Pattern.compile(".*\\.icepush");
         private static final String RESOURCE_KEY = "javax.faces.resource";
         private static final String BROWSERID_COOKIE = "ice.push.browser";
 
         private ResourceHandler resourceHandler;
         private MainServlet mainServlet;
         private ServletContext servletContext;
 
         public void afterPhase(final PhaseEvent event) {
             // Do nothing.
         }
 
         public void beforePhase(final PhaseEvent event) {
             if (mainServlet == null) {
                 Class mainServletClass = (Class) ExtensionRegistry.getBestExtension(servletContext, "org.icepush.MainServlet");
                 try {
                     Constructor mainServletConstructor = mainServletClass.getConstructor(new Class[]{ServletContext.class});
                     mainServlet = (MainServlet) mainServletConstructor.newInstance(servletContext);
                 } catch (Exception e) {
                     log.log(Level.SEVERE, "Cannot instantiate extension org.icepush.MainServlet.", e);
                     throw new RuntimeException(e);
                 }
             }
 
             FacesContext facesContext = FacesContext.getCurrentInstance();
             ExternalContext externalContext = facesContext.getExternalContext();
             Object BrowserID = externalContext.getRequestCookieMap().get(BROWSERID_COOKIE);
             HttpServletRequest request = EnvUtils.getSafeRequest(facesContext);
             HttpServletResponse response = EnvUtils.getSafeResponse(facesContext);
             if (null == BrowserID) {
                 //Need better integration with ICEpush to assign ice.push.browser
                 //without createPushId()
                 ((PushContext) externalContext.getApplicationMap()
                         .get(PushContext.class.getName()))
                         .createPushId(request, response);
             }
         }
 
         public PhaseId getPhaseId() {
             return PhaseId.RESTORE_VIEW;
         }
 
         public ResourceHandler getWrapped() {
             return resourceHandler;
         }
 
         @Override
         public void handleResourceRequest(final FacesContext facesContext) throws IOException {
             if (null == mainServlet) {
                 resourceHandler.handleResourceRequest(facesContext);
                 return;
             }
             String resourceName = facesContext.getExternalContext()
                     .getRequestParameterMap().get(RESOURCE_KEY);
 
             //Return safe, proxied versions of the request and response if we are
             //running in a portlet environment.
             HttpServletRequest request = EnvUtils.getSafeRequest(facesContext);
             HttpServletResponse response = EnvUtils.getSafeResponse(facesContext);
 
             if (RESOURCES.contains(resourceName)) {
                 try {
                     mainServlet.service(request, response);
                 } catch (Exception e) {
                     throw new RuntimeException(e);
                 }
                 return;
             }
             if (request instanceof ProxyHttpServletRequest) {
                 resourceHandler.handleResourceRequest(facesContext);
                 return;
             }
             String requestURI = request.getRequestURI();
             if (ICEpushRequestPattern.matcher(requestURI).find()) {
                 try {
                     mainServlet.service(request, response);
                 } catch (Exception e) {
                     throw new RuntimeException(e);
                 }
             } else {
                 try {
                     resourceHandler.handleResourceRequest(facesContext);
                 } catch (IOException e) {
                     //capture & log Tomcat specific exception
                     if (e.getClass().getName().endsWith("ClientAbortException")) {
                         log.fine("Browser closed the connection prematurely for " + requestURI);
                     } else {
                         throw e;
                     }
                 }
             }
         }
 
         @Override
         public boolean isResourceRequest(final FacesContext facesContext) {
             String resourceName = facesContext.getExternalContext()
                     .getRequestParameterMap().get(RESOURCE_KEY);
             if (RESOURCES.contains(resourceName)) {
                 return true;
             }
             ExternalContext externalContext = facesContext.getExternalContext();
             if (EnvUtils.instanceofPortletRequest(externalContext.getRequest())) {
                 return resourceHandler.isResourceRequest(facesContext);
             }
             HttpServletRequest servletRequest = (HttpServletRequest) externalContext.getRequest();
             String requestURI = servletRequest.getRequestURI();
             return resourceHandler.isResourceRequest(facesContext) || ICEpushRequestPattern.matcher(requestURI).find();
         }
 
         void initialize(final ResourceHandler resourceHandler, final ServletContext servletContext, final ICEpushResourceHandler icePushResourceHandler) {
             this.resourceHandler = resourceHandler;
             this.servletContext = servletContext;
             servletContext.setAttribute(ICEpushResourceHandler.class.getName(), icePushResourceHandler);
             LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
             Iterator<String> lifecycleIds = lifecycleFactory.getLifecycleIds();
             while (lifecycleIds.hasNext()) {
                 String lifecycleId = lifecycleIds.next();
                 Lifecycle lifecycle = lifecycleFactory.getLifecycle(lifecycleId);
                 lifecycle.addPhaseListener(icePushResourceHandler);
             }
         }
 
         void shutdownMainServlet() {
             mainServlet.shutdown();
         }
     }
 
     private static class BlockingICEpushResourceHandlerWrapper extends AbstractICEpushResourceHandler {
         private final ICEpushResourceHandlerImpl resourceHandler;
 
         private BlockingICEpushResourceHandlerWrapper(final ICEpushResourceHandlerImpl resourceHandler) {
             this.resourceHandler = resourceHandler;
         }
 
         public void afterPhase(final PhaseEvent event) {
             lock.lock();
             try {
                 if (resourceHandler.mainServlet == null) {
                     condition.awaitUninterruptibly();
                 }
                 resourceHandler.afterPhase(event);
             } finally {
                 lock.unlock();
             }
         }
 
         public void beforePhase(final PhaseEvent event) {
             lock.lock();
             try {
                 if (resourceHandler.mainServlet == null) {
                     condition.awaitUninterruptibly();
                 }
                 resourceHandler.beforePhase(event);
             } finally {
                 lock.unlock();
             }
         }
 
         @Override
         public Resource createResource(final String resourceName) {
             lock.lock();
             try {
                 if (resourceHandler.mainServlet == null) {
                     condition.awaitUninterruptibly();
                 }
                 return resourceHandler.createResource(resourceName);
             } finally {
                 lock.unlock();
             }
         }
 
         @Override
         public Resource createResource(final String resourceName, final String libraryName) {
             lock.lock();
             try {
                 if (resourceHandler.mainServlet == null) {
                     condition.awaitUninterruptibly();
                 }
                 return resourceHandler.createResource(resourceName, libraryName);
             } finally {
                 lock.unlock();
             }
         }
 
         @Override
         public Resource createResource(final String resourceName, final String libraryName, final String contentType) {
             lock.lock();
             try {
                 if (resourceHandler.mainServlet == null) {
                     condition.awaitUninterruptibly();
                 }
                 return resourceHandler.createResource(resourceName, libraryName, contentType);
             } finally {
                 lock.unlock();
             }
         }
 
         public PhaseId getPhaseId() {
             lock.lock();
             try {
                 if (resourceHandler.mainServlet == null) {
                     condition.awaitUninterruptibly();
                 }
                 return resourceHandler.getPhaseId();
             } finally {
                 lock.unlock();
             }
         }
 
         @Override
         public String getRendererTypeForResourceName(final String resourceName) {
             lock.lock();
             try {
                 if (resourceHandler.mainServlet == null) {
                     condition.awaitUninterruptibly();
                 }
                 return resourceHandler.getRendererTypeForResourceName(resourceName);
             } finally {
                 lock.unlock();
             }
         }
 
         public ResourceHandler getWrapped() {
             lock.lock();
             try {
                 if (resourceHandler.mainServlet == null) {
                     condition.awaitUninterruptibly();
                 }
                 return resourceHandler.getWrapped();
             } finally {
                 lock.unlock();
             }
         }
 
         @Override
         public void handleResourceRequest(final FacesContext facesContext) throws IOException {
             lock.lock();
             try {
                 if (resourceHandler.mainServlet == null) {
                     condition.awaitUninterruptibly();
                 }
                 resourceHandler.handleResourceRequest(facesContext);
             } finally {
                 lock.unlock();
             }
         }
 
         @Override
         public boolean isResourceRequest(final FacesContext facesContext) {
             lock.lock();
             try {
                 if (resourceHandler.mainServlet == null) {
                     condition.awaitUninterruptibly();
                 }
                 return resourceHandler.isResourceRequest(facesContext);
             } finally {
                 lock.unlock();
             }
         }
 
         @Override
         public boolean libraryExists(final String libraryName) {
             lock.lock();
             try {
                 if (resourceHandler.mainServlet == null) {
                     condition.awaitUninterruptibly();
                 }
                 return resourceHandler.libraryExists(libraryName);
             } finally {
                 lock.unlock();
             }
         }
 
         void initialize(final ResourceHandler resourceHandler, final ServletContext servletContext, final ICEpushResourceHandler icePushResourceHandler) {
             this.resourceHandler.initialize(resourceHandler, servletContext, icePushResourceHandler);
         }
 
         void shutdownMainServlet() {
             lock.lock();
             try {
                 if (resourceHandler.mainServlet == null) {
                     condition.awaitUninterruptibly();
                 }
                 resourceHandler.shutdownMainServlet();
             } finally {
                 lock.unlock();
             }
         }
     }
 
     private static class ProxyICEpushResourceHandler extends AbstractICEpushResourceHandler {
         private final ResourceHandler resourceHandler;
 
         public ProxyICEpushResourceHandler(ResourceHandler resourceHandler) {
             this.resourceHandler = resourceHandler;
         }
 
         void initialize(ResourceHandler resourceHandler, ServletContext servletContext, ICEpushResourceHandler icePushResourceHandler) {
         }
 
         void shutdownMainServlet() {
         }
 
         public void afterPhase(PhaseEvent event) {
         }
 
         public void beforePhase(PhaseEvent event) {
         }
 
         public PhaseId getPhaseId() {
             return null;
         }
 
         public ResourceHandler getWrapped() {
             return resourceHandler;
         }
     }
 
 }
