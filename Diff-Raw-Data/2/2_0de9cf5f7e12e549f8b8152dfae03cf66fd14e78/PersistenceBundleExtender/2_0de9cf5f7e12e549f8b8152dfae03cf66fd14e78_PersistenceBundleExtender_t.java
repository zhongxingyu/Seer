 /*******************************************************************************
  * Copyright (c) 2010 Oracle.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * and Apache License v2.0 which accompanies this distribution. 
  * The Eclipse Public License is available at
  *     http://www.eclipse.org/legal/epl-v10.html
  * and the Apache License v2.0 is available at 
  *     http://www.opensource.org/licenses/apache2.0.php.
  * You may elect to redistribute this code under either of these licenses.
  *
  * Contributors:
  *     mkeith - Gemini JPA work 
  ******************************************************************************/
 package org.eclipse.gemini.jpa;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.jar.Manifest;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleEvent;
 import org.osgi.framework.Constants;
 import org.osgi.framework.SynchronousBundleListener;
 import org.osgi.service.packageadmin.PackageAdmin;
 
 import org.eclipse.gemini.jpa.provider.OSGiJpaProvider;
 
 import static org.eclipse.gemini.jpa.GeminiUtil.*;
 
 
 /**
  * This extender can be used by a provider to listen for persistence unit 
  * bundles and assign them to this provider if the unit is able to be assigned.
  */
 public class PersistenceBundleExtender implements SynchronousBundleListener  {
 
     /*================*/
     /* Instance state */
     /*================*/
     
     // The provider associated with this extender
     OSGiJpaProvider osgiJpaProvider;
     
     // Utility class
     PersistenceUnitBundleUtil util;
     
     // Persistence units by bundle 
     Map<Bundle, List<PUnitInfo>> unitsByBundle = 
         Collections.synchronizedMap(new HashMap<Bundle, List<PUnitInfo>>());
     
     // Just keep the bundle ids to prevent hard references to the bundles
     Set<Long> lazyBundles = new HashSet<Long>();
     Set<Long> refreshingBundles = new HashSet<Long>();
     
     /*==============*/
     /* Constructors */
     /*==============*/
     
     public PersistenceBundleExtender() {}
     public PersistenceBundleExtender(OSGiJpaProvider provider) { 
         this.osgiJpaProvider = provider;
         this.util = new PersistenceUnitBundleUtil();
     }
 
     /*================================*/
     /* API methods called by provider */
     /*================================*/
 
     /**
      * Start listening for bundle events to indicate the presence of 
      * persistence unit bundles. 
      */
     public void startListening() {
         debug("GeminiExtender listening");
         osgiJpaProvider.getBundleContext().addBundleListener(this);
     }
 
     /**
      * Stop listening to bundle events. 
      */
     public void stopListening() {
         debug("GeminiExtender no longer listening");
         osgiJpaProvider.getBundleContext().removeBundleListener(this);
     }
 
     /**
      * Look for persistence unit bundles that are already installed. 
      */
     public void lookForExistingBundles() {
         
         // Look at the bundles that are already installed
         Bundle[] activeBundles = osgiJpaProvider.getBundleContext().getBundles();
         debug("GeminiExtender looking at existing bundles: ", activeBundles);
         
         // Check if any are p-unit bundles
         for (Bundle b : activeBundles) {
             if (isPersistenceUnitBundle(b)) {
                 // We found a persistence unit bundle.
                 // Refresh it so it will go through the resolving state again and client 
                 // bundles that are waiting for the EMF service will get one
                 if ((b.getState() != Bundle.INSTALLED) && (b.getState() != Bundle.UNINSTALLED)) {
                     addToRefreshingBundles(b);
                     PackageAdmin admin = getPackageAdmin(osgiJpaProvider.getBundleContext());
                     debug("GeminiExtender refreshing packages of bundle ", b);
                     admin.refreshPackages(new Bundle[] { b }); 
                 }
             }
         }
     }
 
     /**
      * Generate a set of anchor interfaces for the packages in the p-units. 
      * Create a fragment for them and install the fragment, attaching it to the 
      * persistence unit bundle.
      * 
      * @param b the persistence unit bundle
      * @param infos the collection of metadata for all of the persistence units
      * 
      * @return The bundle object of the installed fragment
      */
     public Bundle generateAndInstallFragment(Bundle b, Collection<PUnitInfo> infos) {
 
         debug("GeminiExtender generating fragment");
         List<String> packageNames = util.uniquePackages(infos);
         List<byte[]> generatedClasses = util.generateAnchorInterfaces(packageNames, osgiJpaProvider.getAnchorClassName());
         Manifest manifest = util.generateFragmentManifest(b);
         byte[] fragment = util.createFragment(manifest, packageNames, osgiJpaProvider.getAnchorClassName(), generatedClasses);
         debug("GeminiExtender finished generating fragment");
         Bundle installedFragment = util.installFragment(b, osgiJpaProvider.getBundle(), fragment);
         debug("GeminiExtender installed fragment bundle: ", installedFragment);
         return installedFragment;
     }
     
     public Map<Bundle, List<PUnitInfo>> clearAllPUnitInfos() {
         Map<Bundle, List<PUnitInfo>> pUnitInfos = unitsByBundle;
         unitsByBundle = null;
         lazyBundles = null;
         refreshingBundles = null;
         return pUnitInfos;
     }
 
     /*============================*/
     /* Additional Support Methods */
     /*============================*/
     
     /**
      * Go through the p-units in a given bundle and assign the ones that do 
      * not have a provider, or have a provider specified as this one.
      * 
      * @param b the bundle to look for p-units in
      */
     public void tryAssigningPersistenceUnitsInBundle(Bundle b) {
         
         debug("GeminiExtender tryAssigningPersistenceUnitsInBundle: ", b);
         // If we have already assigned it then bail
         if (isAssigned(b)) {
             warning("Attempted to assign a bundle that was already assigned: ", b.toString());
             return;
         }
 
         // Look for all of the persistence descriptor files in the bundle
         List<PersistenceDescriptorInfo> descriptorInfos = util.persistenceDescriptorInfos(b);
 
         // Do a partial parse of the descriptors
         Set<PUnitInfo> pUnitInfos = util.persistenceUnitInfoFromXmlFiles(descriptorInfos);
 
         // Cycle through each p-unit info and see if a provider was specified
         for (PUnitInfo info : pUnitInfos) {
             if ((info.getProvider() == null) || (osgiJpaProvider.getProviderClassName().equals(info.getProvider()))) {
                 // We can be the provider; claim the p-unit and add it to our list
                 info.setBundle(b);
                 info.setAssignedProvider(osgiJpaProvider);
                 addToBundleUnits(unitsByBundle, b, info);
             }
         }
         // If we found any that were for us then let the provider know
         List<PUnitInfo> unitsFound = unitsByBundle.get(b);
         if ((unitsFound != null) && (unitsFound.size() != 0)) {
             osgiJpaProvider.assignPersistenceUnitsInBundle(b, unitsByBundle.get(b));
         }
     }
     
     /**
      * Unassign all of the p-units in a given bundle.
      * 
      * @param b the bundle the p-units are in
      */
     public void unassignPersistenceUnitsInBundle(Bundle b) { 
         
         debug("GeminiExtender unassignPersistenceUnitsInBundle: ", b);
         List<PUnitInfo> infos = unitsByBundle.get(b);
         unitsByBundle.remove(b);
         removeFromLazyBundles(b);   
         osgiJpaProvider.unassignPersistenceUnitsInBundle(b, infos);
         // Uninitialize the state of the p-unit
         for (PUnitInfo info : infos) {
             info.setAssignedProvider(null);
             info.setBundle(null);
         }
     }
 
     /**
      * Register the p-units of a given bundle.
      * 
      * @param b the bundle the p-units are in
      */
     public void registerPersistenceUnitsInBundle(Bundle b) {
         
         debug("GeminiExtender registerPersistenceUnitsInBundle: ", b);
         if (!isAssigned(b)) {
             warning("Register called on bundle " + b.getSymbolicName(), " but bundle was not assigned");
             return;
         }
         if (areCompatibleBundles(b, osgiJpaProvider.getBundle())) { 
             debug("GeminiExtender provider compatible with bundle: ", b);            
             osgiJpaProvider.registerPersistenceUnits(unitsByBundle.get(b));
         } else {
             warning("Cannot support bundle " + b.getSymbolicName() +  
                     " because it is not JPA-compatible with the assigned provider " + 
                     osgiJpaProvider.getProviderClassName() + ". This is because the " +
                     "persistence unit bundle has resolved to a different javax.persistence " +
                     "than the provider. \nTo fix this, uninstall one of the javax.persistence " +
                     "bundles so that both the persistence unit bundle and the provider resolve " +
                     "to the same javax.persistence package.");
             unassignPersistenceUnitsInBundle(b);
             // No point in updating/refreshing. 
             // (It would likely just re-resolve to the same JPA interface package.)
         }
     }
     
     /**
      * Unregister the p-units of a given bundle.
      * 
      * @param b the bundle the p-units are in
      */
     public void unregisterPersistenceUnitsInBundle(Bundle b) {
         
         debug("GeminiExtender unregisterPersistenceUnitsInBundle: ", b);
         if (!isAssigned(b)) {
             warning("Unregister called on bundle " + b.getSymbolicName(), " but bundle was not assigned");
             return;
         }
         osgiJpaProvider.unregisterPersistenceUnits(unitsByBundle.get(b));
     }    
     
     /*========================*/
     /* BundleListener methods */
     /*========================*/
 
     public void bundleChanged(BundleEvent event) {
 
         // Only continue if it is a persistence unit bundle
         Bundle b = event.getBundle();
         debug("Extender - bundle event, ", event);
         if (!isPersistenceUnitBundle(b)) return;
 
         // Process each event
         int eventType = event.getType();
 
         if (eventType == BundleEvent.INSTALLED) {
             tryAssigningPersistenceUnitsInBundle(b);
 
         } else if (eventType == BundleEvent.LAZY_ACTIVATION) {
             if (isAssigned(b)) {
                 lazyBundles.add(b.getBundleId()); 
                 registerPersistenceUnitsInBundle(b);
             }
         } else if (eventType == BundleEvent.STARTING) {
             if (isAssigned(b)) {
                 if (!isLazy(b)) {
                     registerPersistenceUnitsInBundle(b);
                 }
             }
         } else if (eventType == BundleEvent.STOPPING) {
             if (isAssigned(b)) {
                 unregisterPersistenceUnitsInBundle(b);
             }
         } else if (eventType == BundleEvent.UNINSTALLED) {
             if (isAssigned(b)) {
                 unassignPersistenceUnitsInBundle(b);
             }
         } else if (eventType == BundleEvent.UPDATED) {
             if (isAssigned(b)) {
                 unassignPersistenceUnitsInBundle(b);
             }
             tryAssigningPersistenceUnitsInBundle(b);
 
         } else if (eventType == BundleEvent.UNRESOLVED) {
             if (isRefreshing(b)) {  // assign refreshing bundles
                 tryAssigningPersistenceUnitsInBundle(b);
                 removeFromRefreshingBundles(b);
             }
         } else {  // RESOLVED, STARTED, STOPPED
             // Do nothing.
         }
     }
 
     /*================*/
     /* Helper methods */
     /*================*/
     
     protected boolean isAssigned(Bundle b) {
         return unitsByBundle.containsKey(b);
     }
 
     protected boolean isLazy(Bundle b) {
         return lazyBundles.contains(b.getBundleId());
     }
     protected boolean addToLazyBundles(Bundle b) {
         return lazyBundles.add(b.getBundleId());
     }
     protected boolean removeFromLazyBundles(Bundle b) {
        return lazyBundles.remove(b.getBundleId());
     }
 
     protected boolean isRefreshing(Bundle b) {
         return refreshingBundles.contains(b.getBundleId());
     }
     protected void addToRefreshingBundles(Bundle b) {
         refreshingBundles.add(b.getBundleId());
     }
     protected void removeFromRefreshingBundles(Bundle b) {
         refreshingBundles.remove(b.getBundleId());
     }
         
     protected void addToBundleUnits(Map<Bundle,List<PUnitInfo>> map, 
                                     Bundle b, 
                                     PUnitInfo info) {
         if (!map.containsKey(b))
             map.put(b, new ArrayList<PUnitInfo>());
         map.get(b).add(info);
     }
 
     public boolean isPersistenceUnitBundle(Bundle b) {
         return b.getHeaders().get("Meta-Persistence") != null;
     }
 
     public boolean isLazyActivatedBundle(Bundle b) {
         String policy = (String) b.getHeaders().get(Constants.BUNDLE_ACTIVATIONPOLICY);
         return (policy != null) && (policy.equals(Constants.ACTIVATION_LAZY));        
     }
         
     /**
      * Return whether or not the persistence unit bundle
      * has a consistent JPA interface class space with the provider bundle. 
      * This method must be called after both bundles have been resolved.
      */
     public boolean areCompatibleBundles(Bundle pUnitBundle, Bundle providerBundle) {
         try {
             debug("GeminiExtender checking bundle compatibility of: ", pUnitBundle);
             Class<?> pUnitClass = pUnitBundle.loadClass("javax.persistence.Entity");
             Class<?> providerClass = providerBundle.loadClass("javax.persistence.Entity");
             return pUnitClass.getClassLoader() == providerClass.getClassLoader();
         } catch (ClassNotFoundException cnfEx) {
             // If one of the bundles does not have the class in its class space 
             // then by definition the two are consistent w.r.t. that package
             return true;
         } 
     }
     public void stop(BundleContext context) throws Exception {
     }
     public void start(BundleContext context) throws Exception {
     }
 }
