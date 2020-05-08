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
 
import static org.eclipse.gemini.jpa.GeminiUtil.bundleVersion;
import static org.eclipse.gemini.jpa.GeminiUtil.debug;
import static org.eclipse.gemini.jpa.GeminiUtil.fatalError;
import static org.eclipse.gemini.jpa.GeminiUtil.formattedPackageString;
import static org.eclipse.gemini.jpa.GeminiUtil.warning;

 import java.lang.reflect.Proxy;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map;
 
 import javax.persistence.EntityManagerFactory;
 
import org.eclipse.gemini.jpa.classloader.BundleProxyClassLoader;
import org.eclipse.gemini.jpa.classloader.CompositeClassLoader;
import org.eclipse.gemini.jpa.provider.OSGiJpaProvider;
import org.eclipse.gemini.jpa.proxy.EMFBuilderServiceProxyHandler;
import org.eclipse.gemini.jpa.proxy.EMFServiceProxyHandler;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;
 
 /**
  * This class provides functionality to handle service registration of 
  * persistence units and providers, etc. One instance per provider.
  */
 public class GeminiServicesUtil {
 
     // The provider using this instance
     OSGiJpaProvider osgiJpaProvider;
     
     // Keep this for logging convenience
     String providerClassName;
    
     // PersistenceProvider service
     ServiceRegistration providerService;
     
     
     public GeminiServicesUtil(OSGiJpaProvider provider) {
         this.osgiJpaProvider = provider;
         this.providerClassName = provider.getProviderClassName();
     }
     
     /*==================*/
     /* Services methods */
     /*==================*/
     
     /**
      * Register the provider as a persistence provider service.
      * The service registration will be stored locally.
      */
     public void registerProviderService() {
         
         debug("GeminiServicesUtil registering provider service for ", providerClassName);
 
         // Service strings
         String[] serviceNames = { javax.persistence.spi.PersistenceProvider.class.getName() };
         // Get a provider JPA SPI instance 
         javax.persistence.spi.PersistenceProvider persistenceProvider = osgiJpaProvider.getProviderInstance();
 
         // Store the version of the provider as a service property
         String version = bundleVersion(osgiJpaProvider.getBundle());
         Dictionary<String,String> props = new Hashtable<String,String>();
         props.put("osgi.jpa.provider.version", version);
         props.put("javax.persistence.provider", providerClassName);
         
         // Register the provider service
         providerService = osgiJpaProvider.getBundleContext().registerService(
                 serviceNames, persistenceProvider, props);
         debug("GeminiServicesUtil successfully registered provider service for ", providerClassName);
     }    
 
     /**
      * Unregister the provider service. 
      */
     public void unregisterProviderService() {
 
         debug("GeminiServicesUtil unregistering provider service for ", providerClassName);
         providerService.unregister();
         providerService = null;
         debug("GeminiServicesUtil successfully unregistered provider service for ", providerClassName);
     }
 
     /**
      * Register the EMF and EMFBuilder services.
      */
     public void registerEMFServices(PUnitInfo pUnitInfo) {
 
         // Map of generated anchor classes keyed by class name
         Map<String, Class<?>> anchorClasses; 
         
         debug("GeminiServicesUtil registerEMFServices for ", pUnitInfo.getUnitName());
         Bundle pUnitBundle = pUnitInfo.getBundle();
 
         // Will be empty if anchor classes not generated
         anchorClasses = loadAnchorClasses(pUnitInfo);
 
         // Create the properties used for both services
         Dictionary<String,String> props = buildServiceProperties(pUnitInfo);
         
         // Try to register the EMF service (it will only occur if data source is available)
         tryToRegisterEMFService(pUnitInfo, anchorClasses, props);                
 
         // Create a builder service in any case
         registerEMFBuilderService(pUnitInfo, anchorClasses, props);
     }
 
     /**
      * Unregister the EMF service if there was an EMF service registered.
      * 
      * @param pUnitInfo
      * 
      * @return EntityManagerFactory the factory backing the service, or null if one didn't exist
      */
     public EntityManagerFactory unregisterEMFService(PUnitInfo pUnitInfo) {
 
         debug("GeminiServicesUtil unregisterEMFService for ", pUnitInfo.getUnitName());
 
         // If the tracking service is going, stop it
         // TODO Take this out when we handle the DSF coming back?
         stopTrackingDataSourceFactory(pUnitInfo);
 
         // If an EMF service is registered then unregister it
         ServiceRegistration emfService = pUnitInfo.getEmfService();
         if (emfService != null) {
             debug("GeminiServicesUtil unregistering EMF service for ", pUnitInfo.getUnitName());
             try { 
                 emfService.unregister(); 
             } catch (Exception e) {
                 warning("Error unregistering EMF service: ", e);
             }
             debug("GeminiServicesUtil unregistered EMF service for ", pUnitInfo.getUnitName());
             pUnitInfo.setEmfService(null);
         }
 
         EMFServiceProxyHandler emfHandler = pUnitInfo.getEmfHandler();
         EntityManagerFactory emf = null;
         if (emfHandler != null) {
             emf = emfHandler.getEMF();
             debug("GeminiServicesUtil EMF service had emf: ", emf);
             emfHandler.setEMF(null);
             pUnitInfo.setEmfHandler(null);
         }
         return emf;
     }
 
     /**
      * Unregister the EMFBuilder service.
      * 
      * @param pUnitInfo
      * 
      * @return EntityManagerFactory the factory backing the service, or null if one didn't exist yet
      */
     public EntityManagerFactory unregisterEMFBuilderService(PUnitInfo pUnitInfo) {
 
         debug("GeminiServicesUtil unregisterEMFBuilderService for ", pUnitInfo.getUnitName());
 
         // Unregister the service
         ServiceRegistration emfBuilderService = pUnitInfo.getEmfBuilderService();
         if (emfBuilderService != null) {
             debug("GeminiServicesUtil unregistering EMFBuilder service for ", pUnitInfo.getUnitName());
             try {
                 emfBuilderService.unregister();
             } catch (Exception e) {
                 warning("Error unregistering EMFBuilder service: ", e);
             }
             debug("GeminiServicesUtil unregistered EMFBuilder service for ", pUnitInfo.getUnitName());
             pUnitInfo.setEmfBuilderService(null);
         }
 
         // Save the EMF if it exists and clear out the handler
         EMFBuilderServiceProxyHandler emfBuilderHandler = pUnitInfo.getEmfBuilderHandler();
         EntityManagerFactory emf = null;
         if (emfBuilderHandler != null) {
             emf = emfBuilderHandler.getEMF();
             debug("GeminiServicesUtil EMFBuilder service had emf: ", emf);
             emfBuilderHandler.setEMF(null);
             pUnitInfo.setEmfBuilderHandler(null);
         }
         return emf;
     }    
     
     /*================*/
     /* Helper methods */
     /*================*/
     
     /*
      * Some environments (PDE!) do not support dynamically installing bundles, hence
      * anchor classes cannot be generated. 
      * Return whether anchor classes are currently being generated. 
      */
     boolean canGenerateAnchorClasses() { 
         // TODO Make this properly configurable
         return false; 
     }
     
     /*
      * Load and return the anchor classes that were generated.
      * Return an empty collection if anchor classes were not generated.
      */
     Map<String, Class<?>> loadAnchorClasses(PUnitInfo pUnitInfo) {
 
         Map<String, Class<?>> result = new HashMap<String, Class<?>>();
 
         if (!canGenerateAnchorClasses()) 
             return result;
 
         for (String packageName : pUnitInfo.getUniquePackageNames()) {
             String className = formattedPackageString(packageName, '/', '.') + 
                                     osgiJpaProvider.getAnchorClassName();
             try { 
                 debug("GeminiServicesUtil loading anchor class ", className);
                 Class<?> cls = pUnitInfo.getBundle().loadClass(className);
                 debug("GeminiServicesUtil loaded anchor class ", cls);
                 result.put(className, cls);
             } catch (ClassNotFoundException cnfEx) {
                 fatalError("Could not load anchor class ", cnfEx);
             }
         }
         return result;
     }
     
     /**
      * Get or create a loader to load classes from the punit.
      * A sequence of conditions provides a pattern for obtaining it.
      */
     ClassLoader extractPUnitLoader(PUnitInfo pUnitInfo, 
                                    Map<String, Class<?>> anchorClasses) {
     
         ClassLoader pUnitLoader = null;
         
         // 1. If there are any anchor classes then load one and get its loader
         if (!anchorClasses.isEmpty()) {
             pUnitLoader = anchorClasses.values().iterator().next().getClassLoader();
 
         // 2. Otherwise, if there are managed JPA classes listed, use one to get the loader
         } else if (!pUnitInfo.getClasses().isEmpty()) {
             try { 
                 pUnitLoader = pUnitInfo.getBundle().loadClass((String)(pUnitInfo.getClasses().toArray()[0])).getClassLoader();
             } catch (ClassNotFoundException cnfEx) {
                 fatalError("Could not load domain class in p-unit", cnfEx);
             }
             
         // 3. If all else fails just use a proxy loader
         } else {
             pUnitLoader = new BundleProxyClassLoader(pUnitInfo.getBundle());
         }
         debug("GeminiServicesUtil pUnit loader ", pUnitLoader);
         return pUnitLoader;
     }
 
     /**
      * Get or create a loader to use to create a proxy class.
      */
     ClassLoader proxyLoader(PUnitInfo pUnitInfo,
                             Map<String, Class<?>> anchorClasses, 
                             Class<?> jpaClass) {
         
         ClassLoader cl = null;
 
         // If there are no managed JPA classes listed, return loader used to load the class passed in
         if (pUnitInfo.getClasses().isEmpty()) {
             cl = jpaClass.getClassLoader();
         } else if (!anchorClasses.isEmpty()) {
             // If anchor classes exist then get a loader from one of them
             cl = anchorClasses.values().iterator().next().getClassLoader();
         } else {
             try {
                 // We have domain classes, but no anchor classes were generated.
                 // Load a domain class and get a loader from it. Combine it with the provider loader.
                 ClassLoader pUnitLoader = 
                     pUnitInfo.getBundle().loadClass((String)(pUnitInfo.getClasses().toArray()[0])).getClassLoader();
                 ClassLoader jpaClassLoader = jpaClass.getClassLoader();
                 cl = (pUnitLoader == jpaClassLoader) 
                     ? jpaClassLoader 
                     : new CompositeClassLoader(pUnitLoader, jpaClassLoader);
             } catch (ClassNotFoundException cnfEx) {
                 fatalError("Could not load domain class in p-unit", cnfEx);
             }
         }
         debug("GeminiServicesUtil proxy loader ", cl);
         return cl;
     }
 
 
     /** 
      * Load the EMF class from the specified bundle.
      * Throw a fatal exception if not found. 
      */
     Class<?> loadEMFClass(Bundle b) {
 
         debug("GeminiServicesUtil loading EMF class");
         try {
             return b.loadClass("javax.persistence.EntityManagerFactory");
         } catch (ClassNotFoundException cnfEx) {
             fatalError("Could not load EntityManagerFactory from bundle " + b, cnfEx);
         }
         return null;
     }
     
     /** 
      * Load the EMFBuilder class from the specified bundle.
      * Throw a fatal exception if not found. 
      */
     Class<?> loadEMFBuilderClass(Bundle b) {
 
         debug("GeminiServicesUtil loading EMFBuilder class");
         try {
             return b.loadClass("org.osgi.service.jpa.EntityManagerFactoryBuilder");
         } catch (ClassNotFoundException cnfEx) {
             fatalError("Could not load EntityManagerFactoryBuilder from bundle " + b, cnfEx);
         }
         return null;
     }
 
     /** 
      * Create and return a proxy for the EMF (and specified list of classes
      * which must include the EMF class).
      */
     Object createEMFProxy(PUnitInfo pUnitInfo, ClassLoader loader, Class<?>[] clsArray) {
 
         EMFServiceProxyHandler emfProxyHandler = new EMFServiceProxyHandler(pUnitInfo);
         Object result = null;
         try {
             result = Proxy.newProxyInstance(loader, clsArray, emfProxyHandler);
             debug("GeminiServicesUtil created EMF proxy ");
         } catch (Exception e) { 
             fatalError("GeminiServicesUtil - Failed to create proxy for EMF service: ", e); 
         }
         pUnitInfo.setEmfHandler(emfProxyHandler);
         return result;
     }
     
     /** 
      * Create and return a proxy for the EMFBuilder (and specified list of classes
      * which must include the EMFBuilder class).
      */
     Object createEMFBuilderProxy(PUnitInfo pUnitInfo, 
                                         ClassLoader loader, 
                                         Class<?>[] clsArray) {
         
         // Assume that EMF proxy handler has been created and is stored in pUnitInfo
         EMFBuilderServiceProxyHandler emfBuilderProxyHandler = 
             new EMFBuilderServiceProxyHandler(pUnitInfo, pUnitInfo.getEmfHandler());
         Object result = null;
         try {
             result = Proxy.newProxyInstance(loader, clsArray, emfBuilderProxyHandler);
             debug("GeminiServicesUtil created EMFBuilder proxy ");
         } catch (Exception e) { 
             fatalError("GeminiServicesUtil - Failed to create proxy for EMFBuilder service: ", e); 
         }
         pUnitInfo.setEmfBuilderHandler(emfBuilderProxyHandler);
         return result;
     }
     
     /** 
      * build the list of service properties for the service.
      */
     Dictionary<String,String> buildServiceProperties(PUnitInfo pUnitInfo) {
 
         Bundle pUnitBundle = pUnitInfo.getBundle();
         // Assemble the properties
         Dictionary<String,String> props = new Hashtable<String,String>();
         props.put("osgi.unit.name", pUnitInfo.getUnitName());
         props.put("osgi.unit.version", bundleVersion(pUnitInfo.getBundle()));
         props.put("osgi.unit.provider", providerClassName);
         // For now, only support punits composed of one bundle
         String bundleId = pUnitBundle.getSymbolicName() + "_" + bundleVersion(pUnitBundle);
         props.put("osgi.managed.bundles", bundleId);
         debug("GeminiServicesUtil EMF[Builder] services props: ", props);
         return props;
     }
 
     /** 
      * Register the EMF service.
      */
     void tryToRegisterEMFService(PUnitInfo pUnitInfo,
                                         Map<String,Class<?>> anchorClasses,
                                         Dictionary<String,String> props) {
 
         debug("GeminiServicesUtil register EMF service");
         // Array of classes being proxied by EMF proxy
         Collection<Class<?>> proxiedClasses = new ArrayList<Class<?>>();
 
         // Load the EMF class. TODO Make this the pUnit loader when fragment in place
         Class<?> emfClass = loadEMFClass(osgiJpaProvider.getBundle());
         
         // Add EMF class and anchor classes to the proxied class collection for EMF proxy
         proxiedClasses.addAll(anchorClasses.values());
         proxiedClasses.add(emfClass);
         Class<?>[] classArray = proxiedClasses.toArray(new Class[0]);
         debug("GeminiServicesUtil EMF proxy class array: ", classArray);
         
         // Get a loader to load the proxy classes
         ClassLoader loader = proxyLoader(pUnitInfo, anchorClasses, emfClass);
 
         // Create proxy impl object for EMF service
         Object emfServiceProxy = createEMFProxy(pUnitInfo, loader, classArray);
 
         // Do we create an EMF service?
         if (pUnitInfo.getDriverClassName() == null) {
             debug("GeminiServicesUtil No driver class specified so no factory service created");            
         } else {
             if (!trackDataSourceFactory(pUnitInfo)) {
                 warning("DataSourceFactory for " + pUnitInfo.getDriverClassName(), " not registered.");
             } else {
                 // Convert array of classes to class name strings
                 String[] classNameArray = new String[classArray.length];
                 for (int i=0; i<classArray.length; i++)
                     classNameArray[i] = classArray[i].getName();
 
                 // Register the EMF service (using p-unit context) and set registration in PUnitInfo
                 ServiceRegistration emfService = null;
                 try {
                     emfService = pUnitInfo.getBundle().getBundleContext()
                                    .registerService(classNameArray, emfServiceProxy, props);
                     debug("GeminiServicesUtil EMF service: ", emfService);
                 } catch (Exception e) {
                     fatalError("GeminiServicesUtil could not register EMF service for " + pUnitInfo.getUnitName(), e);
                 }
                 pUnitInfo.setEmfService(emfService);
                 debug("GeminiServicesUtil registered EMF service for ", pUnitInfo.getUnitName());
             }
         }
     }
     
     
     /** 
      * Register the EMFBuilder service.
      */
     void registerEMFBuilderService(PUnitInfo pUnitInfo,
                                           Map<String,Class<?>> anchorClasses,
                                           Dictionary<String,String> props) {
     
         debug("GeminiServicesUtil register EMFBuilder service");
         // Array of classes being proxied by EMFBuilder proxy
         Collection<Class<?>> proxiedClasses = new ArrayList<Class<?>>();
 
         // Load the EMFB class. TODO Make this the pUnit loader when fragment in place
         Class<?> emfBuilderClass = loadEMFBuilderClass(osgiJpaProvider.getBundle());
 
         // Add EMF class and anchor classes to the proxied class collection for EMF proxy
         proxiedClasses.addAll(anchorClasses.values());
         proxiedClasses.add(emfBuilderClass);
         Class<?>[] classArray = proxiedClasses.toArray(new Class[0]);
         debug("GeminiServicesUtil EMFBuilder proxy class array: ", classArray);
         
         // Get a loader to load the proxy classes
         ClassLoader loader = proxyLoader(pUnitInfo, anchorClasses, emfBuilderClass);
 
         // Create proxy impl object for EMF service
         Object emfBuilderServiceProxy = createEMFBuilderProxy(pUnitInfo, loader, classArray);
 
         // Convert array of classes to class name strings
         String[] classNameArray = new String[classArray.length];
         for (int i=0; i<classArray.length; i++)
             classNameArray[i] = classArray[i].getName();
     
         //Register the EMFBuilder service and set it in the PUnitInfo
         ServiceRegistration emfBuilderService = null;
         try {
             // TODO Should be registered by p-unit context, not provider context
             emfBuilderService = osgiJpaProvider.getBundleContext()
                     .registerService(classNameArray, emfBuilderServiceProxy, props);
         } catch (Exception e) {
             fatalError("GeminiServicesUtil could not register EMFBuilder service for " + pUnitInfo.getUnitName(), e);
         }
         pUnitInfo.setEmfBuilderService(emfBuilderService);
         debug("GeminiServicesUtil registered EMFBuilder service for ", pUnitInfo.getUnitName());
 
     }    
 
     /*================================*/
     /* Data source management methods */
     /*================================*/
 
     /** 
      * Look up the data source factory service for the specified
      * persistence unit and cause it to be tracked, so that if it gets stopped 
      * then we can be told and remove the dependent EMF service.
      * Return true if the data source factory was registered, false if it wasn't
      */
     public boolean trackDataSourceFactory(PUnitInfo pUnitInfo) {
 
         debug("GeminiServicesUtil trackDataSourceFactory for p-unit ", pUnitInfo.getUnitName());
         ServiceReference[] dsfRefs = null;
 
         // See if the data source factory service for the specified driver is registered
         String filter = "(" + DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + "=" + 
                         pUnitInfo.getDriverClassName() + ")";
         try {
             dsfRefs = pUnitInfo.getBundle().getBundleContext().getServiceReferences(
                 DataSourceFactory.class.getName(), filter);
         } catch (InvalidSyntaxException isEx) {
             fatalError("Bad filter syntax (likely because of missing driver class name)", isEx);
         } 
         if (dsfRefs == null)
             return false;
         
         // We found at least one -- track the first ranked one (assuming it will be used)
         // TODO Race condition where service could disappear before being tracked
         debug("GeminiServicesUtil starting tracker for DataSourceFactory ", 
                 pUnitInfo.getDriverClassName());
         ServiceTracker tracker = new ServiceTracker(osgiJpaProvider.getBundleContext(), 
                                                     dsfRefs[0],
                                                     new DataSourceTracker(pUnitInfo, this));
         pUnitInfo.setTracker(tracker);
         tracker.open();
         return true;
     }
 
     /** 
      * Stop tracking the data source factory for the given p-unit
      */
     public void stopTrackingDataSourceFactory(PUnitInfo pUnitInfo) {
         // Clean up the tracker
         debug("GeminiServicesUtil stopTrackingDataSourceFactory ", 
                 pUnitInfo.getDriverClassName(), " for p-unit: ", pUnitInfo.getUnitName());
         if (pUnitInfo.getTracker() != null) {
             debug("GeminiServicesUtil stopping tracker for DataSourceFactory ", 
                     pUnitInfo.getDriverClassName());
             pUnitInfo.getTracker().close();
             pUnitInfo.setTracker(null);
         }
     }
 
     /** 
      * This method will be invoked by the data source factory tracker
      * when the data source factory service comes online.
      */
     public void dataSourceFactoryAdded(PUnitInfo pUnitInfo) {
         debug("GeminiServicesUtil dataSourceFactoryAdded, ", pUnitInfo.getDriverClassName());
         if ((pUnitInfo.getEmfBuilderHandler() != null) && 
                 (pUnitInfo.getEmfBuilderHandler().getEMF() != null)) {
             // A factory already exists in the builder so we can't register an EMF service
         } else {
             // The builder has not been used. Just unregister it and go through the entire 
             // registration process again assuming the data source service is present
             debug("GeminiServicesUtil dataSourceFactoryAdded, unregistering builder and reregistering ", pUnitInfo.getDriverClassName());
             unregisterEMFBuilderService(pUnitInfo);
             registerEMFServices(pUnitInfo);
         }
     }
 
     /** 
      * This method will be invoked by the data source factory tracker
      * when the data source factory service that we rely on disappears.
      */
     public void dataSourceFactoryRemoved(PUnitInfo pUnitInfo) {
         // TODO async handling of data source removal
 
         debug("GeminiServicesUtil dataSourceFactoryRemoved, ", pUnitInfo.getDriverClassName());
         if ((pUnitInfo.getEmfBuilderHandler() != null) && 
                 (pUnitInfo.getEmfBuilderHandler().getEMF() != null)) {
             debug("GeminiServicesUtil builder had an EMF, ", 
                     "unregistering both services for p-unit ", 
                     pUnitInfo.getDriverClassName());
             // Call into the provider to do the unregistration
             Collection<PUnitInfo> pUnits = new ArrayList<PUnitInfo>();
             pUnits.add(pUnitInfo);
             osgiJpaProvider.unregisterPersistenceUnits(pUnits);
         } else {
             // Only unregister the EMF service, and leave the Builder
             debug("GeminiServicesUtil only unregistering EMF service, ", 
                     "leaving Builder service");
             EntityManagerFactory emf = unregisterEMFService(pUnitInfo);           
             if (emf != null) 
                 emf.close();
         }
     }
 }
