 /*******************************************************************************
  * This file is part of the Virgo Web Server.
  *
  * Copyright (c) 2010 Eclipse Foundation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.osgi.region;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 
 /**
  * {@link RegionServiceHookBase} is a base service hook for controlling service isolation between the kernel and user
  * regions.
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * This class is thread safe.
  * 
  */
 abstract class RegionServiceHookBase {
 
     private static final long SYSTEM_BUNDLE_ID = 0L;
 
     private static final String SERVICE_CLASS_NAME_PROPERTY = "objectClass";
 
    private static final String SERVICE_CLASS_SEPARATOR = "'";
 
     private final RegionMembership regionMembership;
 
     private final Set<String> serviceImports;
 
     private final Set<String> serviceExports;
 
     RegionServiceHookBase(RegionMembership regionMembership, String regionServiceImports, String regionServiceExports) {
         this.regionMembership = regionMembership;
         this.serviceImports = serviceListStringToSet(regionServiceImports);
         this.serviceExports = serviceListStringToSet(regionServiceExports);
     }
 
     private HashSet<String> serviceListStringToSet(String serviceListString) {
         return new HashSet<String>(Arrays.asList(serviceListString.split(SERVICE_CLASS_SEPARATOR)));
     }
 
     protected final boolean isUserRegionBundle(BundleContext bundleContext) {
         return isUserRegionBundle(bundleContext.getBundle());
     }
 
     protected final boolean isUserRegionBundle(Bundle bundle) {
         return this.regionMembership.contains(bundle);
     }
     
     protected static boolean isSystemBundle(BundleContext bundleContext) {
         return isSystemBundle(bundleContext.getBundle());
     }
 
     protected static boolean isSystemBundle(Bundle bundle) {
         return bundle.getBundleId() == SYSTEM_BUNDLE_ID;
     }
 
     protected final boolean isUserRegionService(ServiceReference<?> serviceReference) {
         Bundle serviceSource = serviceReference.getBundle();
         return isUserRegionBundle(serviceSource);
     }
     
     protected static boolean isSystemBundleService(ServiceReference<?> serviceReference) {
         return isSystemBundle(serviceReference.getBundle());
     }
 
     protected final boolean serviceExported(ServiceReference<?> serviceReference) {
         return servicePresent(this.serviceExports, serviceReference);
     }
 
     private static boolean servicePresent(Set<String> serviceClasses, ServiceReference<?> serviceReference) {
         String[] serviceClassNames = (String[]) serviceReference.getProperty(SERVICE_CLASS_NAME_PROPERTY);
         for (String serviceClassName : serviceClassNames) {
             if ("org.eclipse.virgo.kernel.osgi.framework.PackageAdminUtil".equals(serviceClassName)) {
                 System.out.println("DEBUG 1");
             } else if ("org.springframework.osgi.context.event.OsgiBundleApplicationContextListener".equals(serviceClassName)) {
                 System.out.println("DEBUG 2");
             }
             if (serviceClasses.contains(serviceClassName)) {
                 return true;
             }
         }
         return false;
     }
 
     protected final boolean serviceImported(ServiceReference<?> serviceReference) {
         return servicePresent(this.serviceImports, serviceReference);
     }
 
 }
