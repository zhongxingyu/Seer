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
  *     ssmith - EclipseLink integration
  ******************************************************************************/
 package org.eclipse.gemini.jpa.provider;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.spi.ClassTransformer;
 import javax.persistence.spi.PersistenceUnitInfo;
 
 import org.eclipse.gemini.jpa.PUnitInfo;
 import org.eclipse.gemini.jpa.weaving.IWeaver;
 import org.eclipse.persistence.internal.jpa.deployment.JPAInitializer;
 import org.eclipse.persistence.internal.jpa.deployment.JavaSECMPInitializer;
 import org.eclipse.persistence.internal.jpa.deployment.PersistenceInitializationHelper;
 import org.eclipse.persistence.internal.jpa.deployment.PersistenceUnitProcessor;
 import org.eclipse.persistence.jpa.Archive;
 import org.eclipse.persistence.logging.AbstractSessionLog;
 import org.eclipse.persistence.logging.SessionLog;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 
 public class GeminiOSGiInitializer extends JavaSECMPInitializer {
     public static final String OSGI_BUNDLE = "org.eclipse.gemini.jpa.bundle";
     private static final String OSGI_CONTEXT = "org.eclipse.gemini.jpa.context";
 
     private boolean weavingSupported = true; // TODO: need to determine if Equinox 
     protected ClassLoader bundleLoader;
    
     /**
      * Constructor used when registering bundles.
      */
     public GeminiOSGiInitializer() {
     }
 
     /** 
      * Constructor used by PersistenceProvider$PersistenceInitializationHelper
      * @param loader
      */
     GeminiOSGiInitializer(ClassLoader loader) {
         super(loader);
         this.bundleLoader = loader;
     }
 
     /**
      * Indicates whether puName uniquely defines the persistence unit.
      */
     @Override
     public boolean isPersistenceUnitUniquelyDefinedByName() {
         // TODO isPersistenceUnitUniquelyDefinedByName should be false
         // but PU creation fails for embedded PUs
         return true;
     }
     
     /***
      * A bundle is being stopped or becoming in some way unavailable.
      * Undeploy the bundle's persistence units and remove all references
      * to it.
      * @param bundle
      */
     public void unregisterBundle(final Bundle bundle, Collection<PUnitInfo> pUnits) {
         //TODO: unregisterBundle(final Bundle bundle, Collection<PUnitInfo> pUnits)
     }
     
     /***
      * registerBundle will 
      * @param context
      * @param bundle
      * @param pUnits
      */
     public void registerBundle(final BundleContext context, final Bundle bundle, ClassLoader bundleLoader, Collection<PUnitInfo> pUnits) {
         usesAgent = true; // implies weaving=true
         this.bundleLoader = bundleLoader;
         
         List<Archive> pars = new ArrayList<Archive>();
         Map<String, String> storedArchives = new HashMap<String, String>();
         for (PUnitInfo pUnitInfo : pUnits) {
             if (!storedArchives.containsKey(pUnitInfo.getDescriptorInfo().fullDescriptorPath())){
                 pars.addAll(PersistenceUnitProcessor.findPersistenceArchives(bundleLoader, pUnitInfo.getDescriptorInfo().fullDescriptorPath()));
                 storedArchives.put(pUnitInfo.getDescriptorInfo().fullDescriptorPath(), null);
             }
         }
         // Create a properties map with the bundle and context so they
         // are available when defining a transformer.
         Map<String, Object> properties = new HashMap<String, Object>();
         properties.put(OSGI_BUNDLE, bundle);
         properties.put(OSGI_CONTEXT, context);
         for (Archive archive: pars) {
             AbstractSessionLog.getLog().log(SessionLog.FINER, "cmp_init_initialize", archive);
             initPersistenceUnits(archive, properties, createHelper(this.bundleLoader));
         }
     }
 
     protected PersistenceInitializationHelper createHelper(final ClassLoader loader) {
         return new PersistenceInitializationHelper() {
             @SuppressWarnings("rawtypes")
             @Override
             public ClassLoader getClassLoader(String emName, Map properties) {
                 return loader;
             }
             
             @SuppressWarnings("rawtypes")
             @Override
             public JPAInitializer getInitializer(ClassLoader classLoader, Map m) {
                 return GeminiOSGiInitializer.this;
             }
         };
     }
 
     
     /***
 	 * In OSGi we don't need a temp loader so use the loader built
 	 * for the bundle.
 	 */
 	@SuppressWarnings("rawtypes")
     @Override
 	protected ClassLoader createTempLoader(Collection col) {
 	    return this.bundleLoader;
 	}
 	
 	/***
      * This version of getClassLoader is called after the classloader has been
      * built for a bundle.
      * 
      * @param persistenceUnitName
      * @param properties
      * @return
      */
     @SuppressWarnings("rawtypes")
     public ClassLoader getClassLoader(String persistenceUnitName, Map properties) {
         return this.bundleLoader;
     }
      
     @SuppressWarnings("rawtypes")
     @Override
     public void registerTransformer(ClassTransformer transformer, PersistenceUnitInfo persistenceUnitInfo, Map properties) {
         if (weavingSupported) {
             Bundle bundle = (Bundle) properties.get(OSGI_BUNDLE);
             if (bundle == null){
                 AbstractSessionLog.getLog().log(SessionLog.FINER, "Bundle null, not registering Weaving Service");
                 return;
             }
             BundleContext context = (BundleContext) properties.get(OSGI_CONTEXT);
             if (context == null){
                 AbstractSessionLog.getLog().log(SessionLog.FINER, "Bundle Context null, not registering Weaving Service");
                 return;
             }
             if (transformer != null) {
                 AbstractSessionLog.getLog().log(SessionLog.FINER, "cmp_init_register_transformer", persistenceUnitInfo.getPersistenceUnitName());
                 IWeaver weavingService = new OSGiWeaver(transformer, bundle.getSymbolicName(), bundle.getVersion());
                 context.registerService(IWeaver.class.getName(), weavingService, new Hashtable());
                 AbstractSessionLog.getLog().log(SessionLog.FINER, "Registering Weaving Service");
             } else if (transformer == null) {
                 AbstractSessionLog.getLog().log(SessionLog.FINER, "cmp_init_transformer_is_null");
             }
         } else {
             throw new RuntimeException("Attempt to create a transformer when weaving not supported!");
         }
     }
     
 }
