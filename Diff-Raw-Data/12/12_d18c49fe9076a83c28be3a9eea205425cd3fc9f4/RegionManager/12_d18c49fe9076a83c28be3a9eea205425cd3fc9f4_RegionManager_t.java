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
 
 package org.eclipse.virgo.kernel.osgi.region;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.virgo.kernel.core.Shutdown;
 import org.eclipse.virgo.kernel.osgi.framework.OsgiFrameworkLogEvents;
 import org.eclipse.virgo.kernel.serviceability.NonNull;
 import org.eclipse.virgo.medic.eventlog.EventLogger;
 import org.eclipse.virgo.osgi.launcher.parser.ArgumentParser;
 import org.eclipse.virgo.osgi.launcher.parser.BundleEntry;
 import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.hooks.bundle.EventHook;
 import org.osgi.framework.hooks.bundle.FindHook;
 import org.osgi.framework.hooks.resolver.ResolverHookFactory;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventAdmin;
 
 /**
  * Creates and manages the user {@link Region regions}.
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * Threadsafe.
  * 
  */
 final class RegionManager {
 
     private static final String USER_REGION_CONFIGURATION_PID = "org.eclipse.virgo.kernel.userregion";
 
     private static final String USER_REGION_BASE_BUNDLES_PROPERTY = "baseBundles";
 
     private static final String USER_REGION_PACKAGE_IMPORTS_PROPERTY = "packageImports";
 
     private static final String USER_REGION_SERVICE_IMPORTS_PROPERTY = "serviceImports";
 
     private static final String USER_REGION_SERVICE_EXPORTS_PROPERTY = "serviceExports";
 
     private static final String USER_REGION_PROPERTIES_PROPERTY = "inheritedFrameworkProperties";
 
     private static final String REGION_KERNEL = "org.eclipse.virgo.region.kernel";
 
     private static final String REGION_USER = "org.eclipse.virgo.region.user";
 
     private static final String EVENT_REGION_STARTING = "org/eclipse/virgo/kernel/region/STARTING";
 
     private static final String EVENT_PROPERTY_REGION_BUNDLECONTEXT = "region.bundleContext";
 
     private final ServiceRegistrationTracker tracker = new ServiceRegistrationTracker();
 
     private final BundleContext bundleContext;
 
     private final ArgumentParser parser = new ArgumentParser();
 
     private final EventAdmin eventAdmin;
 
     private String regionBundles;
 
     private String regionImports;
 
     private String regionServiceImports;
 
     private String regionServiceExports;
 
     public RegionManager(BundleContext bundleContext, EventAdmin eventAdmin, ConfigurationAdmin configAdmin, EventLogger eventLogger,
         Shutdown shutdown) {
         this.bundleContext = bundleContext;
         this.eventAdmin = eventAdmin;
         getRegionConfiguration(configAdmin, eventLogger, shutdown);
     }
 
     private void getRegionConfiguration(ConfigurationAdmin configAdmin, EventLogger eventLogger, Shutdown shutdown) {
         try {
             Configuration config = configAdmin.getConfiguration(USER_REGION_CONFIGURATION_PID);
 
             @SuppressWarnings("unchecked")
             Dictionary<String, String> properties = (Dictionary<String, String>) config.getProperties();
 
             if (properties != null) {
                 this.regionBundles = properties.get(USER_REGION_BASE_BUNDLES_PROPERTY);
                 this.regionImports = properties.get(USER_REGION_PACKAGE_IMPORTS_PROPERTY);
                 this.regionServiceImports = properties.get(USER_REGION_SERVICE_IMPORTS_PROPERTY);
                 this.regionServiceExports = properties.get(USER_REGION_SERVICE_EXPORTS_PROPERTY);
             } else {
                 eventLogger.log(OsgiFrameworkLogEvents.USER_REGION_CONFIGURATION_UNAVAILABLE);
                 shutdown.immediateShutdown();
             }
         } catch (Exception e) {
             eventLogger.log(OsgiFrameworkLogEvents.USER_REGION_CONFIGURATION_UNAVAILABLE, e);
             shutdown.immediateShutdown();
         }
     }
 
     public void start() throws BundleException {
         createAndPublishUserRegion();
     }
 
     private void createAndPublishUserRegion() throws BundleException {
 
         registerRegionService(new ImmutableRegion(REGION_KERNEL, this.bundleContext));
 
         String userRegionImportsProperty = this.regionImports != null ? this.regionImports
             : this.bundleContext.getProperty(USER_REGION_PACKAGE_IMPORTS_PROPERTY);
         String expandedUserRegionImportsProperty = null;
         if (userRegionImportsProperty != null) {
             expandedUserRegionImportsProperty = PackageImportWildcardExpander.expandPackageImportsWildcards(userRegionImportsProperty,
                 this.bundleContext);
         }
 
         RegionMembership regionMembership = new RegionMembership() {
 
             @Override
             public boolean contains(Bundle bundle) {
                 // TODO implement a more robust membership scheme
                 long bundleId = bundle.getBundleId();
                 return bundleId > bundleContext.getBundle().getBundleId() || bundleId == 0L;
             }
         };
         
         registerResolverHookFactory(new RegionResolverHookFactory(regionMembership, expandedUserRegionImportsProperty));
         
         registerBundleEventHook(new RegionBundleEventHook(regionMembership));
         
         registerBundleFindHook(new RegionBundleFindHook(regionMembership));
         
         BundleContext userRegionBundleContext = initialiseUserRegionBundles();
         
         registerRegionService(new ImmutableRegion(REGION_USER, userRegionBundleContext));
         
         publishUserRegionBundleContext(userRegionBundleContext);
     }
 
     private void registerBundleFindHook(FindHook findHook) {
         this.tracker.track(this.bundleContext.registerService(FindHook.class, findHook, null));
         
     }
 
     private void registerBundleEventHook(EventHook eventHook) {
         this.tracker.track(this.bundleContext.registerService(EventHook.class, eventHook, null));
         
     }
 
     private void publishUserRegionBundleContext(BundleContext userRegionBundleContext) {
         Dictionary<String, String> properties = new Hashtable<String, String>();
         properties.put("org.eclipse.virgo.kernel.regionContext", "true");
         this.bundleContext.registerService(BundleContext.class, userRegionBundleContext, properties);
     }
 
     private void registerResolverHookFactory(ResolverHookFactory resolverHookFactory) {
         this.tracker.track(this.bundleContext.registerService(ResolverHookFactory.class, resolverHookFactory, null));
     }
 
     private BundleContext initialiseUserRegionBundles() throws BundleException {
 
         BundleContext userRegionBundleContext = null;
 
         String userRegionBundlesProperty = this.regionBundles != null ? this.regionBundles
             : this.bundleContext.getProperty(USER_REGION_BASE_BUNDLES_PROPERTY);
 
         if (userRegionBundlesProperty != null) {
             List<Bundle> bundlesToStart = new ArrayList<Bundle>();
 
             for (BundleEntry entry : this.parser.parseBundleEntries(userRegionBundlesProperty)) {
                 Bundle bundle = null;
                 String bundleUriString = entry.getURI().toString();
                 
                 InputStream is;
                 try {
                     String filePath = null;
                     if (bundleUriString.startsWith("file:")) {
                         filePath = bundleUriString.substring("file:".length());
                     }
                     is = new FileInputStream(filePath);
                     bundle = this.bundleContext.installBundle("userregion:" + filePath, is);
                } catch (FileNotFoundException _) {
                    // Attempt to install the bundle directly in case the file is a directory.
                     bundle = this.bundleContext.installBundle(bundleUriString);
                 }
 
                 if (entry.isAutoStart()) {
                     bundlesToStart.add(bundle);
                 }
             }
 
             for (Bundle bundle : bundlesToStart) {
                 try {
                     bundle.start();
                 } catch (BundleException e) {
                     throw new BundleException("Failed to start bundle " + bundle.getSymbolicName() + " " + bundle.getVersion(), e);
                 }
                 if (userRegionBundleContext == null) {
                     userRegionBundleContext = bundle.getBundleContext();
                     notifyUserRegionStarting(userRegionBundleContext);
                 }
             }
         }
         return userRegionBundleContext;
     }
 
     private void notifyUserRegionStarting(BundleContext userRegionBundleContext) {
         Map<String, Object> properties = new HashMap<String, Object>();
         properties.put(EVENT_PROPERTY_REGION_BUNDLECONTEXT, userRegionBundleContext);
         this.eventAdmin.sendEvent(new Event(EVENT_REGION_STARTING, properties));
     }
 
     private void registerRegionService(Region region) {
         Dictionary<String, String> props = new Hashtable<String, String>();
         props.put("org.eclipse.virgo.kernel.region.name", region.getName());
         this.tracker.track(this.bundleContext.registerService(Region.class, region, props));
     }
 
     public void stop() {
         this.tracker.unregisterAll();
     }
 
     private static class ImmutableRegion implements Region {
 
         private final String name;
 
         private final BundleContext bundleContext;
 
         public ImmutableRegion(String name, @NonNull BundleContext bundleContext) {
             this.name = name;
             this.bundleContext = bundleContext;
         }
 
         public String getName() {
             return name;
         }
 
         public BundleContext getBundleContext() {
             return this.bundleContext;
         }
 
     }
 }
