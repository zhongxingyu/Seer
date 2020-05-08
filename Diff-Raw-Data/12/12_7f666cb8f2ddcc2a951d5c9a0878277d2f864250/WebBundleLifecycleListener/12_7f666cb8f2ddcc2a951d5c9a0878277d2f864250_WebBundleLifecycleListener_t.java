 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.web.core.internal;
 
 import java.io.IOException;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
 import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
 import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
 import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListenerSupport;
 import org.eclipse.virgo.osgi.extensions.equinox.hooks.PluggableDelegatingClassLoaderDelegateHook;
 import org.eclipse.gemini.web.core.WebApplication;
 import org.eclipse.gemini.web.core.WebApplicationStartFailedException;
 import org.eclipse.gemini.web.core.WebContainer;
 import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
 import org.eclipse.virgo.web.core.WebApplicationRegistry;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventConstants;
 import org.osgi.service.event.EventHandler;
 
 
 /**
  * <strong>Concurrent Semantics</strong><br />
  *
  * Thread-safe.
  *
  */
 final class WebBundleLifecycleListener extends InstallArtifactLifecycleListenerSupport {
     
     private static final String EMPTY_CONTEXT_PATH = "";
 
     private static final String ROOT_CONTEXT_PATH = "/";
 
     private final Map<InstallArtifact, WebApplication> webApplications = new ConcurrentHashMap<InstallArtifact, WebApplication>();
     
     private final Map<Bundle, BundleInstallArtifact> webBundleInstallArtifacts = new ConcurrentHashMap<Bundle, BundleInstallArtifact>();
     
     private final WebAppClassLoaderDelegateHook classLoaderDelegateHook = new WebAppClassLoaderDelegateHook();
     
     private final EventHandler webBundleDeployedEventHandler = new WebBundleDeployedEventHandler();
     
     private final WebDeploymentEnvironment environment;
     
     private final BundleContext bundleContext;
     
     private volatile ServiceRegistration<EventHandler> eventHandlerRegistration;
     
     WebBundleLifecycleListener(WebDeploymentEnvironment environment, BundleContext bundleContext) {
         this.environment = environment;
         this.bundleContext = bundleContext;
         
     }
     
     public void init() {
         PluggableDelegatingClassLoaderDelegateHook.getInstance().addDelegate(this.classLoaderDelegateHook);
         
         registerEventHandler();
     }
 
     private void registerEventHandler() {
         Dictionary<String, String> properties = new Hashtable<String, String>();
         properties.put(EventConstants.EVENT_TOPIC, WebContainer.EVENT_DEPLOYED);
         this.eventHandlerRegistration = this.bundleContext.registerService(EventHandler.class, this.webBundleDeployedEventHandler, properties);
     }
     
     public void destroy() {
         PluggableDelegatingClassLoaderDelegateHook.getInstance().removeDelegate(this.classLoaderDelegateHook);
         
         unregisterEventHandler();
     }
 
     private void unregisterEventHandler() {
         ServiceRegistration<EventHandler> localRegistration = this.eventHandlerRegistration;
         if (localRegistration != null) {
             this.eventHandlerRegistration = null;
             localRegistration.unregister();
         }
     }
     
     /** 
      * {@inheritDoc}
      */
     @Override
     public void onStarting(InstallArtifact installArtifact) throws DeploymentException {
         if (isWebBundle(installArtifact)) {
             try {
                 this.webApplications.put(installArtifact, getWebContainer().createWebApplication(((BundleInstallArtifact)installArtifact).getBundle()));
             } catch (BundleException be) {
                 throw new DeploymentException("Failed to create new web application for web bundle '" + installArtifact + "'.", be);
             }
         }
     }
 
     /** 
      * {@inheritDoc}
      */
     @Override
     public void onStarted(InstallArtifact installArtifact) throws DeploymentException {
         WebApplication webApplication = this.webApplications.get(installArtifact);
         if (webApplication != null) {
             BundleInstallArtifact bundleInstallArtifact = (BundleInstallArtifact)installArtifact;
             Bundle bundle = bundleInstallArtifact.getBundle();
             this.webBundleInstallArtifacts.put(bundle, bundleInstallArtifact);
             try {
                 webApplication.start();
                 
                 this.classLoaderDelegateHook.addWebApplication(webApplication, bundle);
                
                 String contextPath = getContextPath(webApplication);
                 getApplicationRegistry().registerWebApplication(contextPath, getApplicationName(installArtifact));
                 installArtifact.setProperty("org.eclipse.virgo.web.contextPath", contextPath);
                 installArtifact.setProperty("artifact-type", "Web Bundle");
                 
             } catch (WebApplicationStartFailedException wasfe) {
                 throw new DeploymentException("Web application failed to start", wasfe);
             }
         }
     }
     
     protected void webBundleDeployed(Bundle webBundle) {
         BundleInstallArtifact installArtifact = this.webBundleInstallArtifacts.get(webBundle);
         
         if (installArtifact != null) {
             WebApplication webApplication = this.webApplications.get(installArtifact);
             if (webApplication != null) {                   
                 
             }
         }
     }
     
     /** 
      * {@inheritDoc}
      */
     @Override
     public void onStopping(InstallArtifact installArtifact) {
         WebApplication webApplication = this.webApplications.remove(installArtifact);
         if (webApplication != null) {
            BundleInstallArtifact bundleInstallArtifact = (BundleInstallArtifact)installArtifact;
            Bundle bundle = bundleInstallArtifact.getBundle();
            
             getApplicationRegistry().unregisterWebApplication(getContextPath(webApplication));
            
            this.classLoaderDelegateHook.removeWebApplication(bundle);
            
            webApplication.stop();
            
             this.webBundleInstallArtifacts.remove(((BundleInstallArtifact)installArtifact).getBundle());
         }
     }
     
     private static boolean isWebBundle(InstallArtifact installArtifact) throws DeploymentException {
         if (installArtifact instanceof BundleInstallArtifact) {
             try {
                 BundleManifest bundleManifest = ((BundleInstallArtifact)installArtifact).getBundleManifest();
                 return (WebBundleTransformer.WEB_BUNDLE_MODULE_TYPE.equals(bundleManifest.getModuleType()));
             } catch (IOException ioe) {
                 throw new DeploymentException("Failed to get bundle manifest from '" + installArtifact + "'", ioe);
             }
         }
         return false;
     }
     
     private String getContextPath(WebApplication webApplication) {
         String contextPath = webApplication.getServletContext().getContextPath();
         if(EMPTY_CONTEXT_PATH.equals(contextPath)) {
             return ROOT_CONTEXT_PATH;
         }
         return contextPath;
     }
     
     private WebContainer getWebContainer() {
         return this.environment.getWebContainer();
     }
 
     private WebApplicationRegistry getApplicationRegistry() {
         return this.environment.getApplicationRegistry();
     }
     
     private String getApplicationName(InstallArtifact installArtifact) {
         return installArtifact.getName() + "-" + installArtifact.getVersion();
     }
     
     private final class WebBundleDeployedEventHandler implements EventHandler {
 
         public void handleEvent(Event event) {
             if (WebContainer.EVENT_DEPLOYED.equals(event.getTopic())) {
                 webBundleDeployed((Bundle)event.getProperty(EventConstants.BUNDLE));
             }
         }        
     }
 }
