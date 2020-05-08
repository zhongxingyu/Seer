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
 
 import static org.eclipse.gemini.jpa.GeminiUtil.debug;
 import static org.eclipse.gemini.jpa.GeminiUtil.getPackageAdmin;
 import static org.eclipse.gemini.jpa.GeminiUtil.warning;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleEvent;
 import org.osgi.framework.Constants;
 import org.osgi.framework.SynchronousBundleListener;
 import org.osgi.service.packageadmin.PackageAdmin;
 
 /**
  * This extender listens for persistence unit bundles and assigns
  * them to our provider if the unit is able to be assigned.
  */
 @SuppressWarnings({"deprecation"})
 public class PersistenceBundleExtender implements SynchronousBundleListener  {
 
     /*================*/
     /* Instance state */
     /*================*/
     
     // Pointer back to the mgr
     GeminiManager mgr;
     
     // Stateless utility class
     PersistenceUnitBundleUtil bundleUtil;
     
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
     public PersistenceBundleExtender(GeminiManager mgr) { 
         this.mgr = mgr;
         this.bundleUtil = new PersistenceUnitBundleUtil();
     }
 
     /*===============================*/
     /* API methods called by the mgr */
     /*===============================*/
 
     /**
      * Start listening for bundle events to indicate the presence of 
      * persistence unit bundles. 
      */
     public void startListening() {
         debug("GeminiExtender listening");
         mgr.getBundleContext().addBundleListener(this);
     }
 
     /**
      * Stop listening to bundle events. 
      */
     public void stopListening() {
         debug("GeminiExtender no longer listening");
         mgr.getBundleContext().removeBundleListener(this);
     }
 
     /**
      * Look for persistence unit bundles that are already installed. 
      */
     public void lookForExistingBundles() {
         
         // Look at the bundles that are already installed
         Bundle[] installedBundles = mgr.getBundleContext().getBundles();
         debug("GeminiExtender looking at existing bundles: ", installedBundles);
         
         // Check if any are p-unit bundles
         for (Bundle b : installedBundles) {
             if (isPersistenceUnitBundle(b)) {
                 // We found a persistence unit bundle.
                 if (GeminiSystemProperties.refreshPersistenceBundles()) {
                     // If bundle is active then refresh it and push it through the life cycle again
                     // so it will go through resolving and we can assign it a provider, etc.
                     //  if ((b.getState() != Bundle.INSTALLED) && (b.getState() != Bundle.UNINSTALLED)) {
                     if (b.getState() == Bundle.ACTIVE) {
                         refreshBundle(b);
                     }
                 } else {
                     // Refreshing is disabled - go through assigning and registering process w/o events
                     if (b.getState() != Bundle.UNINSTALLED) {
                         // Assign the p-unit
                         // NOTE: With no refresh, assigning may be happening after the bundle has been resolved
                         tryAssigningPersistenceUnitsInBundle(b);
                         // Now if bundle is starting or active then register the p-units in it
                         if ((b.getState() == Bundle.STARTING) || (b.getState() == Bundle.ACTIVE)) {
                             registerPersistenceUnitsInBundle(b);
                         } // Otherwise just let future events take their course 
                     }
                 }
             }
         }
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
         List<PersistenceDescriptorInfo> descriptorInfos = bundleUtil.persistenceDescriptorInfos(b);
 
         // Do a partial parse of the descriptors
         Set<PUnitInfo> pUnitInfos = bundleUtil.persistenceUnitInfoFromXmlFiles(descriptorInfos);
 
         // Cycle through each p-unit info and see if a provider was specified
         for (PUnitInfo info : pUnitInfos) {
             if ((info.getProvider() == null) || (mgr.getProvider().getProviderClassName().equals(info.getProvider()))) {
                 // We can be the provider; claim the p-unit and add it to our list
                 info.setBundle(b);
                 info.setAssignedProvider(mgr.getProvider());
                 addToBundleUnits(unitsByBundle, b, info);
             }
         }
         // If we found any that were for us then let the provider know
         List<PUnitInfo> unitsFound = unitsByBundle.get(b);
         if ((unitsFound != null) && (unitsFound.size() != 0)) {
             mgr.assignPersistenceUnitsInBundle(b, unitsByBundle.get(b));
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
         mgr.unassignPersistenceUnitsInBundle(b, infos);
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
         if (areCompatibleBundles(b, mgr.getBundle())) { 
             debug("GeminiExtender provider compatible with bundle: ", b);            
             mgr.registerPersistenceUnits(unitsByBundle.get(b));
         } else {
             warning("Cannot support bundle " + b.getSymbolicName() +  
                     " because it is not JPA-compatible with the assigned provider " + 
                     mgr.getProvider().getProviderClassName() + ". This is because the " +
                     "persistence unit bundle has resolved to a different javax.persistence " +
                     "than the provider. \nTo fix this, uninstall one of the javax.persistence " +
                     "bundles so that both the persistence unit bundle and the provider resolve " +
                     "to the same javax.persistence package.");
             unassignPersistenceUnitsInBundle(b);
             // No point in updating or refreshing. 
             // It would likely just re-resolve to the same JPA interface package.
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
         mgr.unregisterPersistenceUnits(unitsByBundle.get(b));
     }    
     
     /**
      * Refresh the persistence bundle.
      * 
      * @param b the bundle the p-units are in
      */    
     public void refreshBundle(Bundle b) {
         // Add the list of currently refreshing bundles. 
         // (It will be removed when the UNRESOLVED event is fired on it)
         addToRefreshingBundles(b);
         // Call refresh on all of the packages
         PackageAdmin admin = getPackageAdmin(mgr.getBundleContext());
         debug("GeminiExtender refreshing packages of bundle ", b);
         admin.refreshPackages(new Bundle[] { b }); 
 
         /* New 4.3 code to use to refresh bundle */
         /*
         Bundle systemBundle = osgiJpaProvider.getBundleContext().getBundle(0);
         FrameworkWiring fw = systemBundle.adapt(FrameworkWiring.class);
         fw.refreshBundles(Arrays.asList(b));
         */
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
         } else if (eventType == BundleEvent.STARTED) {
            // If not assigned then it must have been here already and we need to refresh it
            if (!isAssigned(b)) {
                 refreshBundle(b);
             }
         } else if (eventType == BundleEvent.STOPPING) {
             if (isAssigned(b)) {
                 // Fix for bug #342996
                 if (isLazy(b)) {
                     removeFromLazyBundles(b);
                 }
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
         synchronized (map) {
             if (!map.containsKey(b))
                 map.put(b, new ArrayList<PUnitInfo>());
             List<PUnitInfo> infos = map.get(b);
             if (!infos.contains(info)) 
                 infos.add(info);
         }
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
