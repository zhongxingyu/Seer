 /* 
  * 
  * PROJECT
  *     Name
  *         APS OpenJPA Provider
  *     
  *     Code Version
  *         0.9.1
  *     
  *     Description
  *         Provides an implementation of APSJPAService using OpenJPA.
  *         
  * COPYRIGHTS
  *     Copyright (C) 2012 by Natusoft AB All rights reserved.
  *     
  * LICENSE
  *     Apache 2.0 (Open Source)
  *     
  *     Licensed under the Apache License, Version 2.0 (the "License");
  *     you may not use this file except in compliance with the License.
  *     You may obtain a copy of the License at
  *     
  *       http://www.apache.org/licenses/LICENSE-2.0
  *     
  *     Unless required by applicable law or agreed to in writing, software
  *     distributed under the License is distributed on an "AS IS" BASIS,
  *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *     See the License for the specific language governing permissions and
  *     limitations under the License.
  *     
  * AUTHORS
  *     Tommy Svensson (tommy@natusoft.se)
  *         Changes:
  *         2012-08-19: Created!
  *         
  */
 package se.natusoft.osgi.aps.jpa.service;
 
 import org.apache.openjpa.persistence.PersistenceProviderImpl;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleEvent;
 import org.osgi.framework.BundleListener;
 import org.osgi.service.jpa.EntityManagerFactoryBuilder;
 import se.natusoft.osgi.aps.api.data.jpa.service.APSJPAService;
 import se.natusoft.osgi.aps.exceptions.APSResourceNotFoundException;
 import se.natusoft.osgi.aps.jpa.xml.Persistence;
 import se.natusoft.osgi.aps.jpa.xml.PersistenceUnit;
 import se.natusoft.osgi.aps.tools.APSLogger;
 import xob.Factory;
 import xob.XMLObjectBinder;
 import xob.XMLUnmarshaller;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.spi.PersistenceProvider;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  * This provides an implementation of APSJPAService using OpenJPA.
  */
 public class APSOpenJPAServiceProvider implements BundleListener, APSJPAService, EntityManagerFactoryBuilder {
 
     //
     // Private Members
     //
 
     /** The logger to log to. */
     private APSLogger logger = null;
 
     /** The currently open entity manager factories (well, they are wrapped by an APSJPAEntityManagerProviderImp!). */
     private Map<String, APSJPAEntityManagerProviderImpl> openFactories = new HashMap<String, APSJPAEntityManagerProviderImpl>();
 
     /** The current bundles that have a persistence provider and a map of defined persistence units. */
     private Map<Long, Map<String, PersistenceReg>> persistentBundles = new HashMap<Long, Map<String, PersistenceReg>>();
 
     /** Our own bundle. We need it for class loading later. */
     private Bundle thisBundle = null;
 
     //
     // Constructors
     //
 
     /**
      * Creates a new APSOpenJPAServiceProvider instance.
      *
      * @param logger Our logger.
      */
     public APSOpenJPAServiceProvider(APSLogger logger, BundleContext context) {
         this.logger = logger;
         this.thisBundle = context.getBundle();
     }
 
     //
     // Methods
     //
 
     /**
      * Returns true if the specified bundle has a META-INF/persistence.xml.
      *
      * @param bundle The bundle to check.
      */
     private boolean isPersistenceUsingBundle(Bundle bundle) {
         return bundle.getEntry("META-INF/persistence.xml") != null;
     }
 
     /**
      * Creates a combined key for this.openFactories. This allows for different bundles
      * to have persistent units named the same.
      *
      * @param persistenceUnit
      * @param bundleId
      * @return
      */
     private String factoryKey(String persistenceUnit, Long bundleId) {
         return persistenceUnit + bundleId.toString();
     }
 
     /**
      * Receives notification that a bundle has had a lifecycle change.
      *
      * @param event The <code>BundleEvent</code>.
      */
     @Override
     public void bundleChanged(BundleEvent event) {
         Bundle bundle = event.getBundle();
 
         if (isPersistenceUsingBundle(bundle)) {
 
             switch (event.getType()) {
                 case BundleEvent.STARTED:
                     addPersistenceProviderForBundle(bundle);
                     break;
 
                 case BundleEvent.STOPPED:
                     removePersistenceProviderForBundle(bundle);
                     break;
             }
         }
     }
 
     /**
      * Adds a persistence provider for a bundle.
      *
      * @param bundle The bundle to provide a persistence provider for.
      */
     private void addPersistenceProviderForBundle(Bundle bundle) {
         try {
             ClassLoader currentContextClassLoader = Thread.currentThread().getContextClassLoader();
             try {
                 // First read the META-INF/persistence.xml file.
                 URL persistenceXML = bundle.getEntry("META-INF/persistence.xml");
                 XMLObjectBinder binder = Factory.createXMLObjectBinder(Persistence.class);
                 binder.getXMLParser().setValidating(false);
                 XMLUnmarshaller unmarshaller = binder.createUnmarshaller();
                 InputStream persistenceXMLStream = persistenceXML.openStream();
                 Persistence persistence = (Persistence)unmarshaller.unmarshal(persistenceXMLStream);
 
                 // We don't really care if this fails! We at least tried to close it!
                 try {persistenceXMLStream.close();} catch (IOException ioe){}
 
                 // Then setup a persistence provider for each persistence unit.
                 MultiBundleClassLoader multiBundleClassLoader =
                         new MultiBundleClassLoader(this.thisBundle, bundle);
                 Thread.currentThread().setContextClassLoader(multiBundleClassLoader);
 
                 PersistenceProvider persistenceProvider = new PersistenceProviderImpl();
                 PersistenceReg persistenceReg =
                         new PersistenceReg(persistenceProvider, multiBundleClassLoader);
 
                 Map<String, PersistenceReg> persistenceUnits = new HashMap<String, PersistenceReg>();
 
                 Iterator<PersistenceUnit> puit = persistence.getPersistenceUnits();
                 while(puit.hasNext()) {
                     PersistenceUnit persistenceUnit = puit.next();
 
                     persistenceUnits.put(persistenceUnit.getName(), persistenceReg);
 
                     this.logger.info("aps-openjpa-provider: Configured JPA provider for bundle '" +
                             bundle.getSymbolicName() + "' with persistence unit '" + persistenceUnit.getName() + "'.");
                 }
 
                 this.persistentBundles.put(bundle.getBundleId(), persistenceUnits);
             }
             finally {
                 Thread.currentThread().setContextClassLoader(currentContextClassLoader);
             }
         }
         catch (Exception e) {
             this.logger.error("Failed to setup persistence provider for bundle '" + bundle.getSymbolicName() + "'!", e);
         }
     }
 
     /**
      * Removes the persistence provider for a bundle.
      *
      * @param bundle The bundle to remove the persistence provider for.
      */
     private void removePersistenceProviderForBundle(Bundle bundle) {
         try {
             Map<String, PersistenceReg> persistenceUnits = this.persistentBundles.remove(bundle.getBundleId());
             if (persistenceUnits != null) {
                 for (String puName : persistenceUnits.keySet()) {
                     persistenceUnits.remove(puName);
 
                     if (this.openFactories != null) {
                         APSJPAEntityManagerProviderImpl emp = this.openFactories.remove(factoryKey(puName, bundle.getBundleId()));
                         EntityManagerFactory emf = emp.removeEntityManagerFactory();
                         if (emf instanceof ContextEntityManagerFactory) {
                             emf = ((ContextEntityManagerFactory)emf).getEntityManagerFactory();
                         }
                         if (emf != null && emf.isOpen()) {
                             emf.close();
                         }
                     }
                     this.logger.info("aps-openjpa-provider: Closed JPA provider for bundle '" +
                             bundle.getSymbolicName() + "' with persistence unit '" + puName + "'.");
                 }
             }
         }
         catch (Exception e) {
             this.logger.error("Failed to remove persistence provider for bundle '" + bundle.getSymbolicName() + "'!", e);
         }
     }
 
     /**
      * Initializes and returns a provider from the specified properties.
      *
      * @param bundleContext The context of the client bundle. It is used to locate its persistence provider.
      * @param persistenceUnitName The name of the persistent unit defined in persistence.xml.
      * @param props Custom properties to configure database, etc.
      *
      * @return A configured EntityManager.
      */
     public APSJPAEntityManagerProvider initialize(BundleContext bundleContext, String persistenceUnitName, Map<String, String> props) throws APSResourceNotFoundException {
         Map<String, PersistenceReg> persistenceUnits = this.persistentBundles.get(bundleContext.getBundle().getBundleId());
         PersistenceReg persistenceReg = persistenceUnits.get(persistenceUnitName);
         if (persistenceReg == null) {
             throw new APSResourceNotFoundException("Persistence unit '" + persistenceUnitName + "' was not found!");
         }
 
         String factoryKey = factoryKey(persistenceUnitName, bundleContext.getBundle().getBundleId());
 
         APSJPAEntityManagerProviderImpl emp = this.openFactories.remove(factoryKey);
         if (emp != null) {
             EntityManagerFactory emf = emp.removeEntityManagerFactory();
             if (emf.isOpen()) emf.close();
         }
 
         ClassLoader origContextClassLoader = Thread.currentThread().getContextClassLoader();
         try {
             Thread.currentThread().setContextClassLoader(persistenceReg.getContextClassloader());
 
             EntityManagerFactory emf = persistenceReg.getPersistenceProvider().createEntityManagerFactory(persistenceUnitName, props);
             EntityManagerFactory cemf = new ContextEntityManagerFactory(persistenceReg.contextClassloader, emf);
             emp = new APSJPAEntityManagerProviderImpl(cemf);
             this.openFactories.put(factoryKey, emp);
             return emp;
         }
         finally {
             Thread.currentThread().setContextClassLoader(origContextClassLoader);
         }
     }
 
     /**
      * Closes all open objects. This gets called on bundle stop.
      */
     public void closeAll() {
         for (Long bundleId : this.persistentBundles.keySet()) {
             Map<String, PersistenceReg> persistenceUnits = this.persistentBundles.get(bundleId);
             for (String persistenceUnit : persistenceUnits.keySet()) {
                 String factoryKey = factoryKey(persistenceUnit, bundleId);
                 APSJPAEntityManagerProviderImpl emp = this.openFactories.remove(factoryKey);
 
                 // A client might still sit on this, but we make it invalid which should
                 // make the client release it and create a new one instead when and if the
                 // service becomes available again.
                 EntityManagerFactory emf = emp.removeEntityManagerFactory();
                 if (emf instanceof ContextEntityManagerFactory) {
                     emf = ((ContextEntityManagerFactory)emf).getEntityManagerFactory();
                 }
                 if (emf.isOpen()) {
                     emf.close();
                 }
             }
         }
 
         this.openFactories = null;
     }
 
     /**
      * Please note that this provides the org.osgi.service.jpa.EntityManagerFactoryBuilder implementation. For
     * this API persistence unit names must be unique within the whole server. With the APS API the persistence
      * unit names only have to be unique within a bundle. This does not handle service restart/redeploy either.
      * If that happens the use of the returned EntityManagerFactory will throw an unknown exception!
      * <p/>
      * Also note that this implementation will ignore the "osgi.unit.provider" and "osgi.unit.version" properties!
      * This bundle provides OpenJPA and nothing else.
      * _________________________________________________________________________________________________________
      * Return an EntityManagerFactory instance configured according to the properties
      * defined in the corresponding persistence descriptor, as well as the properties
      * passed into the method.
      *
      * @param props Properties to be used, in addition to those in the persistence descriptor,
      *              for configuring the EntityManagerFactory for the persistence unit.
      * @return An EntityManagerFactory for the persistence unit associated with this service. Must not be null.
      */
     @Override
     public EntityManagerFactory createEntityManagerFactory(Map<String, Object> props) {
         String persistenceUnitName = (String) props.get(EntityManagerFactoryBuilder.JPA_UNIT_NAME);
         if (persistenceUnitName == null) {
             throw new APSResourceNotFoundException("Property 'osgi.unit.name' was not specified in properties!");
         }
 
         PersistenceReg persistenceReg = null;
         long persistenceBundleId = -1;
         for (long bundleId : this.persistentBundles.keySet()) {
             Map<String, PersistenceReg> persistenceUnits = this.persistentBundles.get(bundleId);
             persistenceReg = persistenceUnits.get(persistenceUnitName);
             if (persistenceReg != null) {
                 persistenceBundleId = bundleId;
                 break;
             }
         }
         if (persistenceReg == null) {
             throw new APSResourceNotFoundException("Persistence unit '" + persistenceUnitName + "' was not found!");
         }
 
         String factoryKey = factoryKey(persistenceUnitName, persistenceBundleId);
 
         APSJPAEntityManagerProviderImpl emp = this.openFactories.remove(factoryKey);
         if (emp != null) {
             EntityManagerFactory emf = emp.removeEntityManagerFactory();
             if (emf.isOpen()) emf.close();
         }
 
         ClassLoader origContextClassLoader = Thread.currentThread().getContextClassLoader();
         EntityManagerFactory cemf = null;
         try {
             Thread.currentThread().setContextClassLoader(persistenceReg.getContextClassloader());
 
             EntityManagerFactory emf = persistenceReg.getPersistenceProvider().createEntityManagerFactory(persistenceUnitName, props);
             cemf = new ContextEntityManagerFactory(persistenceReg.contextClassloader, emf);
             emp = new APSJPAEntityManagerProviderImpl(cemf);
             this.openFactories.put(factoryKey, emp);
         }
         finally {
             Thread.currentThread().setContextClassLoader(origContextClassLoader);
         }
 
         return cemf;
     }
 
     //
     // Inner Classes
     //
 
     /**
      * Provides an implementation of the APSJPAEntityManagerProvider API.
      */
     private static class APSJPAEntityManagerProviderImpl implements APSJPAEntityManagerProvider {
         //
         // Private Members
         //
 
         /** The managed entity manager factory. */
         private EntityManagerFactory emf = null;
 
         //
         // Constructors
         //
 
         /**
          * Creates a new APSJPAEntityManagerProviderImpl.
          *
          * @param emf The entity manager factory for the persistence unit we represent.
          */
         public APSJPAEntityManagerProviderImpl(EntityManagerFactory emf) {
             this.emf = emf;
         }
 
         //
         // Methods
         //
 
         /**
          * Returns true if this instance is valid. If not call APSJPAService.initialize(...) again to get a new instance.
          * It will be invalid if the APSJPAService provider have been restarted.
          */
         @Override
         public boolean isValid() {
             return this.emf != null;
         }
 
         /**
          * Creates a new EntityManager. You are responsible for closing it!
          * <p/>
          * Please note that the EntityManager caches all referenced entities. If you keep and reuse it for a longer
          * time it can use more memory. For example at
          * <a href='http://docs.jboss.org/ejb3/app-server/tutorial/extended_pc/extended.html'>http://docs.jboss.org/ejb3/app-server/tutorial/extended_pc/extended.html</a>
          * it says that "Usually, an EntityManager in JBoss EJB 3.0 lives and dies within a JTA transaction". This
          * indicates how long-lived the EntityManager should preferably be.
          * <p/>
          * Also note that if isValid() returns false this method will also return null!
          *
          * @return A configured EntityManager.
          */
         @Override
         public EntityManager createEntityManager() {
             if (isValid()) {
                 return this.emf.createEntityManager();
             }
             return null;
         }
 
         /**
          * Returns the underlaying entity manager factory.
          */
         public EntityManagerFactory getEntityManagerFactory() {
             return this.emf;
         }
 
         /**
          * Removes and returns the entity manager factory of this object. After this call isValid() will return false.
          */
         public EntityManagerFactory removeEntityManagerFactory() {
             EntityManagerFactory remf = this.emf;
             this.emf = null;
             return remf;
         }
     }
 
     /**
      * Holds a persistence provider per persistence unit.
      */
     private static class PersistenceReg {
         //
         // Private Members
         //
 
         /** The persistence provider instance for a PU. */
         private PersistenceProvider persistenceProvider = null;
 
         /** The context classloader for the PU.  */
         private ClassLoader contextClassloader = null;
 
         //
         // Constructors
         //
 
         /**
          * Creates a new PersistentReg.
          *
          * @param persistenceProvider The persistence provider instance for a PU.
          * @param contextClassloader The context class loader for a PU.
          */
         public PersistenceReg(PersistenceProvider persistenceProvider, ClassLoader contextClassloader) {
             this.persistenceProvider = persistenceProvider;
             this.contextClassloader = contextClassloader;
         }
 
         //
         // Methods
         //
 
         /**
          * @return The persistence provider for a PU.
          */
         public PersistenceProvider getPersistenceProvider() {
             return this.persistenceProvider;
         }
 
         /**
          * @return The context class loader for a PU.
          */
         public ClassLoader getContextClassloader() {
             return this.contextClassloader;
         }
     }
 }
