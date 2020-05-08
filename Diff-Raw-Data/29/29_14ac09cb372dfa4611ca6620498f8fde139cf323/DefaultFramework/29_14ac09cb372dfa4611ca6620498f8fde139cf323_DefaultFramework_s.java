 /**
  * palava - a java-php-bridge
  * Copyright (C) 2007-2010  CosmoCode GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package de.cosmocode.palava.core;
 
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.inject.Binder;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Key;
 import com.google.inject.Module;
 import com.google.inject.TypeLiteral;
 import com.google.inject.matcher.Matchers;
 import com.google.inject.multibindings.Multibinder;
 import com.google.inject.name.Names;
 import com.google.inject.spi.InjectionListener;
 import com.google.inject.spi.TypeEncounter;
 import com.google.inject.spi.TypeListener;
 
 import de.cosmocode.commons.State;
 import de.cosmocode.palava.core.event.PostFrameworkStart;
 import de.cosmocode.palava.core.event.PreFrameworkStop;
 import de.cosmocode.palava.core.lifecycle.Disposable;
 import de.cosmocode.palava.core.lifecycle.Startable;
 
 /**
  * Default implementation of the {@link Framework} interface.
  *
  * @author Willi Schoenborn
  */
 final class DefaultFramework implements Framework {
 
     private static final Logger LOG = LoggerFactory.getLogger(DefaultFramework.class);
     
     private static final Key<Set<Service>> SERVICE_KEY = Key.get(new TypeLiteral<Set<Service>>() { });
     
     private State state = State.NEW;
     
     private final List<Service> services = Lists.newArrayList();
     
     private final Injector injector;
     private final Registry registry;
     
     DefaultFramework(Properties properties) {
         Preconditions.checkNotNull(properties, "Properties");
 
         final Module mainModule;
         
         final String className = properties.getProperty(CoreConfig.APPLICATION);
         Preconditions.checkNotNull(className, CoreConfig.APPLICATION);
         final Class<? extends Module> mainModuleClass;
 
         try {
             mainModuleClass = Class.forName(className).asSubclass(Module.class);
             mainModule = mainModuleClass.newInstance();
         } catch (ClassNotFoundException e) {
             throw new IllegalArgumentException(e);
         } catch (InstantiationException e) {
             throw new IllegalArgumentException(e);
         } catch (IllegalAccessException e) {
             throw new IllegalArgumentException(e);
         }
         
         injector = Guice.createInjector(
             mainModule,
             new PropertiesModule(properties),
             new ListenerModule(),
             new EmptyServiceModule()
         );
 
         registry = injector.getInstance(Registry.class);
     }
     
     /**
      * Module which binds properties to constants and the properties
      * map itself to a map annotated with {@link Settings}.
      *
      * @author Willi Schoenborn
      */
    private final class PropertiesModule implements Module {
         
         private final Properties properties;
         
         public PropertiesModule(Properties properties) {
             this.properties = Preconditions.checkNotNull(properties, "Properties");
         }
         
         @Override
         public void configure(Binder binder) {
             Names.bindProperties(binder, properties);
             binder.bind(Properties.class).annotatedWith(Settings.class).toInstance(properties);
         }
         
     }
     
     /**
      * Module which listens for {@link Service}s and registers them locally.
      *
      * @author Willi Schoenborn
      */
     private final class ListenerModule implements Module {
 
         @Override
         public void configure(Binder binder) {
             binder.bindListener(Matchers.any(), new TypeListener() {
                 
                 @Override
                 public <I> void hear(final TypeLiteral<I> literal, TypeEncounter<I> encounter) {
                     if (Service.class.isAssignableFrom(literal.getRawType())) {
                         encounter.register(new InjectionListener<I>() {
                             
                             @Override
                             public void afterInjection(I injectee) {
                                 LOG.info("Adding {} to services", injectee);
                                 services.add(Service.class.cast(injectee));
                             };
                             
                         });
                         
                         encounter.register(new InitializableListener<I>());
                         encounter.register(new StartableListener<I>());
                     }
                 }
                 
             });
         }
         
     }
     
     /**
      * Binds an empty set of services.
      *
      * @author Willi Schoenborn
      */
     private static final class EmptyServiceModule implements Module {
         
         @Override
         public void configure(Binder binder) {
             Multibinder.newSetBinder(binder, Service.class);
         }
         
     }
     
     @Override
     public void start() {
         state = State.STARTING;
         final Set<Service> bootstrapped;
         try {
             bootstrapped = injector.getInstance(SERVICE_KEY);
         /* CHECKSTYLE:OFF */
         } catch (RuntimeException e) {
         /* CHECKSTYLE:ON */
             state = State.FAILED;
             throw e;
         }
         for (Service service : bootstrapped) {
             LOG.debug("Bootstrapped service {}", service);
         }
         state = State.RUNNING;
 
         // trigger post framework start event
         registry.notifySilent(PostFrameworkStart.class, new Procedure<PostFrameworkStart>() {
 
             @Override
             public void apply(PostFrameworkStart input) {
                 input.eventPostFrameworkStart();
             }
 
         });
     }
     
     @Override
     public State currentState() {
         return state;
     }
     
     @Override
     public boolean isRunning() {
         return currentState() == State.RUNNING;
     }
     
     @Override
     public void stop() {
         registry.notifySilent(PreFrameworkStop.class, new Procedure<PreFrameworkStop>() {
             
             @Override
             public void apply(PreFrameworkStop input) {
                 input.eventPreFrameworkStop();
             }
             
         });
 
         final State oldState = state;
         
         state = State.STOPPING;
         LOG.info("Stopping framework");
         stopServices();
         disposeServices();
         LOG.info("Framework stopped");
         
         if (oldState == State.FAILED) {
             state = State.FAILED;
         } else {
             state = State.TERMINATED;
         }
     }
     
     private <T> Iterable<T> filterAndReverse(Class<T> type) {
         return Iterables.reverse(Lists.newArrayList(Iterables.filter(services, type)));
     }
     
     private void stopServices() {
         LOG.info("Stopping services");
         for (Startable startable : filterAndReverse(Startable.class)) {
             LOG.info("Stopping {}", startable);
             try {
                 startable.stop();
             /* CHECKSTYLE:OFF */
             } catch (RuntimeException e) {
             /* CHECKSTYLE:ON */
                 final String message = String.format("Unable to stop service %s", startable);
                 LOG.warn(message, e);
             }
         }
     }
     
     private void disposeServices() {
         LOG.info("Disposing services");
         for (Disposable disposable : filterAndReverse(Disposable.class)) {
             LOG.info("Disposing {}", disposable);
             try {
                 disposable.dispose();
             /* CHECKSTYLE:OFF */
             } catch (RuntimeException e) {
             /* CHECKSTYLE:ON */
                 final String message = String.format("Unable to dispose service %s", disposable);
                 LOG.warn(message, e);
             }
         }
     }
     
 }
