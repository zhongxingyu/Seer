 /*
  * Copyright 2013 Romain Gilles
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package org.javabits.yar.guice.osgi;
 
 import com.google.common.collect.ImmutableList;
 import com.google.inject.*;
import com.google.inject.name.Names;
 import org.javabits.yar.BlockingSupplierRegistry;
 import org.javabits.yar.guice.RegistrationHandler;
 import org.javabits.yar.guice.RegistryListenerHandler;
 import org.osgi.framework.*;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static java.util.Arrays.asList;
 import static org.javabits.yar.guice.YarGuices.newRegistryDeclarationModule;
 
 /**
  * This class provides utility methods to help you to handle Guice Injector creation.
  * <h2>Injector creation</h2>
  * <p>
  * You have several {@code YarGuices.newInjector(...)} methods that try to mimic the {@code Guice.createInjector(...)}
  * methods.
  * <pre>
  * YarGuices.newInjector(bundleContext, PRODUCTION
  *                       , new AbstractModule() {...}
  *                       , new AbstractModule() {...}
  *                       , ...
  *                       , new AbstractRegistryModule() {...}
  *                       , new AbstractRegistryModule() {...}
  *                       , ...
  *                       , new RegistryModule() {...});
  * </pre>
  * This methods will chain the creation of the injector and the
  * {@link #start(org.osgi.framework.BundleContext, com.google.inject.Injector)} method to initialized Yar properly.
  * </p>
  * <p>
  * <b>Warning:</b> You must add one and only one instance of {@link org.javabits.yar.guice.RegistryModule}
  * but as many as you want {@link org.javabits.yar.guice.AbstractRegistryModule}.
  * </p>
  * <p>
  * To create your injector by your self you must add to the list of modules provided to the {@code Guice#createInjector()} method the
  * injector provided by the {@link YarOSGis#newYarOSGiModule(org.osgi.framework.BundleContext)}. This module binds:
  * <ul>
  * <li>the {@code Bundle}</li>
  * <li>the {@code BundleContext}</li>
  * <li>the {@code Registry}</li>
  * <li>the {@code BlockingSupplierRegistry}</li>
  * </ul>
  * Then you have the start the registry by calling the {@code start(...)} method.
  * </p>
  *
  * @author Romain Gilles
  */
 public final class YarOSGis {
 
     private static final String SERVICE_REGISTRY_ERROR_MESSAGE = "no BlockingSupplierRegistry service reference found in OSGi service registry";
    public static final Key<BundleListener> CLEANUP_BUNDLE_LISTENER = Key.get(BundleListener.class, Names.named("cleanup"));
 
     private YarOSGis() {
         throw new AssertionError("not for you!");
     }
 
     public static Injector newInjector(BundleContext bundleContext, Module... modules) {
         return start(bundleContext, Guice.createInjector(getModules(bundleContext, asList(modules))));
     }
 
     public static Injector newInjector(BundleContext bundleContext, Stage stage, Module... modules) {
         return start(bundleContext, Guice.createInjector(stage, getModules(bundleContext, asList(modules))));
     }
 
     public static Injector newInjector(BundleContext bundleContext, Iterable<Module> modules) {
         return start(bundleContext, Guice.createInjector(getModules(bundleContext, modules)));
     }
 
     public static Injector newInjector(BundleContext bundleContext, Stage stage, Iterable<Module> modules) {
         return start(bundleContext, Guice.createInjector(stage, getModules(bundleContext, modules)));
     }
 
     /**
      * The start method is responsible to 'start' the given injector.
      * <p>More formally, it:
      * <ul>
      * <li>registers the injector has an OSGi</li>
      * <li>Gets the inject supplier registration handler and registry listener handle</li>
      * <li>saves the Handlers into the OSGi registry.</li>
      * <li>Init the Handlers.</li>
      * <li>Add cleaner to remove all the registered suppliers and listeners on bundle shutdown.</li>
      * </ul>
      * </p>
      * <p><b>Warning:</b>This injector must have been created with
      * one and only one {@link org.javabits.yar.guice.RegistryModule}
      * to ensure bind registry handlers for Guice. But you can add/use as many as you want
      * {@link org.javabits.yar.guice.AbstractRegistryModule}.</p>
      *
      * @param bundleContext the bundle context from where the injector is created.
      * @param injector      the injector that must be started.
      * @return the given injector.
      */
     public static Injector start(BundleContext bundleContext, Injector injector) {
         registerInjector(bundleContext, injector);
         RegistrationHandler registrationHandler = getRegistrationHandler(injector);
         registerRegistrationHandler(bundleContext, registrationHandler);
         RegistryListenerHandler registryListenerHandler = getRegistryListenerHandler(injector);
         registerListenerHandler(bundleContext, registryListenerHandler);
         attachStoppingListener(bundleContext, injector);
         initHandlers(registrationHandler, registryListenerHandler);
         return injector;
     }
 
     private static BundleRegistryWrapper getBundleRegistryWrapper(Injector injector) {
         return injector.getInstance(BundleRegistryWrapper.class);
     }
 
     private static void initHandlers(RegistrationHandler registrationHandler, RegistryListenerHandler registryListenerHandler) {
         registrationHandler.init();
         registryListenerHandler.init();
     }
 
     private static void registerInjector(BundleContext bundleContext, Injector injector) {
         bundleContext.registerService(Injector.class, injector, null);
     }
 
     private static ServiceRegistration<RegistrationHandler> registerRegistrationHandler(BundleContext bundleContext, RegistrationHandler registrationHandler) {
         return bundleContext.registerService(RegistrationHandler.class, registrationHandler, null);
     }
 
     private static RegistrationHandler getRegistrationHandler(Injector injector) {
         return injector.getInstance(RegistrationHandler.class);
     }
 
     private static ServiceRegistration<RegistryListenerHandler> registerListenerHandler(BundleContext bundleContext, RegistryListenerHandler registryListenerHandler) {
         return bundleContext.registerService(RegistryListenerHandler.class, registryListenerHandler, null);
     }
 
     private static RegistryListenerHandler getRegistryListenerHandler(Injector injector) {
         return injector.getInstance(RegistryListenerHandler.class);
     }
 
     private static void attachStoppingListener(BundleContext bundleContext, Injector injector) {
        BundleListener listener = injector.getInstance(CLEANUP_BUNDLE_LISTENER);
        bundleContext.addBundleListener(listener);
     }
 
     private static Iterable<Module> getModules(BundleContext bundleContext, Iterable<Module> modules) {
         ImmutableList.Builder<Module> modulesBuilder = ImmutableList.builder();
         modulesBuilder.add(newYarOSGiModule(bundleContext));
         modulesBuilder.addAll(modules);
         return modulesBuilder.build();
     }
 
     /**
      * Create a new module that bind the OSGi element: {@code Bundle} and {@code BundleContext}, and the bind the Yar's
      * elements: {@code Registry} and {@code BlockingSupplierRegistry}.
      * This module is required to make Yar works properly in the registry.
      * This method return an instance of {@code AbstractModule} and not an instance of {@link org.javabits.yar.guice.RegistryModule}.
      *
      * @param bundleContext the bundle context associated to the injector.
      * @return A module that binds all the {@code Registry} interfaces + the {@code Bundle} and the {@code BundleContext}
      */
     public static Module newYarOSGiModule(final BundleContext bundleContext) {
         final ForwardingRegistryWrapper blockingSupplierRegistry = getBlockingSupplierRegistry(bundleContext);
         return new AbstractModule() {
             @Override
             protected void configure() {
                 Key<ForwardingRegistryWrapper> registryKey = Key.get(ForwardingRegistryWrapper.class);
                 bind(registryKey).toInstance(blockingSupplierRegistry);
                 bind(BundleRegistryWrapper.class).to(registryKey);
                bind(CLEANUP_BUNDLE_LISTENER).to(BundleStoppingListener.class);
                 install(newRegistryDeclarationModule(registryKey));
                 install(newOSGiModule(bundleContext));
             }
         };
     }
 
     private static Module newOSGiModule(final BundleContext bundleContext) {
         return new AbstractModule() {
             @Override
             protected void configure() {
                 bind(BundleContext.class).toInstance(bundleContext);
                 bind(Bundle.class).toInstance(bundleContext.getBundle());
             }
         };
     }
 
     private static ForwardingRegistryWrapper getBlockingSupplierRegistry(BundleContext bundleContext) {
         ServiceReference<BlockingSupplierRegistry> serviceReference = checkNotNull(bundleContext.getServiceReference(BlockingSupplierRegistry.class)
                 , SERVICE_REGISTRY_ERROR_MESSAGE);
         return new ForwardingRegistryWrapper(checkNotNull(bundleContext.getService(serviceReference), "BlockingSupplierRegistry service not available"));
     }
 
    @Singleton
    static class BundleStoppingListener implements SynchronousBundleListener {
         private final RegistrationHandler registrationHandler;
         private final RegistryListenerHandler registryListenerHandler;
         private final long bundleId;
         private final BundleRegistryWrapper forwardingRegistryWrapper;
 
        @Inject
        BundleStoppingListener(RegistrationHandler registrationHandler, RegistryListenerHandler registryListenerHandler, BundleRegistryWrapper forwardingRegistryWrapper, Bundle bundle) {
             this.registrationHandler = registrationHandler;
             this.registryListenerHandler = registryListenerHandler;
             this.forwardingRegistryWrapper = forwardingRegistryWrapper;
            this.bundleId = bundle.getBundleId();
         }
 
         @Override
         public void bundleChanged(BundleEvent bundleEvent) {
             if (!isStopping(bundleEvent)) {
                 return;
             }
             clearSupplierRegistration();
             clearListenerRegistration();
             clearMissingRegistrations();
         }
 
         private boolean isStopping(BundleEvent bundleEvent) {
             return BundleEvent.STOPPING == bundleEvent.getType() && bundleEvent.getBundle().getBundleId() == bundleId;
         }
 
         private void clearSupplierRegistration() {
             registrationHandler.clear();
         }
 
         private void clearListenerRegistration() {
             registryListenerHandler.clear();
         }
 
         private void clearMissingRegistrations() {
             forwardingRegistryWrapper.clear();
         }
     }
 }
