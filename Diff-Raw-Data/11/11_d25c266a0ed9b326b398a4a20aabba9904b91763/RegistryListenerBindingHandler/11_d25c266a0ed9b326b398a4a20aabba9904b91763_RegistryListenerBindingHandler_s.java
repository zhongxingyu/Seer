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
 
 package org.javabits.yar.guice;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.inject.Binding;
 import com.google.inject.Injector;
 import com.google.inject.TypeLiteral;
 import org.javabits.yar.*;
 
 import javax.annotation.Nullable;
 import javax.inject.Inject;
 import java.util.List;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.collect.Lists.newArrayList;
 
 /**
  * TODO comment
  * TODO add support for Supplier and BlockingSupplier
  * Date: 3/14/13
  *
  * @author Romain Gilles
  */
 public class RegistryListenerBindingHandler implements RegistryListenerHandler {
     private final Injector injector;
     private final Registry registry;
     //we keep a strong reference on the watcher to avoid it to be garbage collected
     //therefore the lifecycle to this watcher is at least associated to the lifecycle of owning injector
     volatile private List<Pair<Registration, Watcher>> listenerRegistrations;
 
 
     @Inject
     public RegistryListenerBindingHandler(Injector injector, Registry registry) {
         this.injector = injector;
         this.registry = registry;
 
     }
 
     @Override
     public void init() {
         this.listenerRegistrations = addListenerToRegistry();
     }
 
     //enforce creation of all watcher before register it
     @SuppressWarnings("unchecked")
     private List<Pair<Registration, Watcher>> addListenerToRegistry() {
         List<Pair<Registration, Watcher>> registrationsBuilder = newArrayList();
         for (Pair<IdMatcher, Watcher> guiceWatcherRegistration : getRegisteredWatchers()) {
             Watcher watcher = guiceWatcherRegistration.right();
             Registration registration = registry.addWatcher(guiceWatcherRegistration.left(), watcher);
             registrationsBuilder.add(new StrongPair<>(registration, watcher));
         }
         return registrationsBuilder;
     }
 
     private List<Pair<IdMatcher, Watcher>> getRegisteredWatchers() {
         ImmutableList.Builder<Pair<IdMatcher, Watcher>> registrationsBuilder = ImmutableList.builder();
         List<Binding<GuiceWatcherRegistration>> guiceWatcherRegistrationBindings = injector.findBindingsByType(TypeLiteral.get(GuiceWatcherRegistration.class));
         for (Binding<GuiceWatcherRegistration> watcherRegistrationBinding : guiceWatcherRegistrationBindings) {
             GuiceWatcherRegistration guiceWatcherRegistration = watcherRegistrationBinding.getProvider().get();
             registrationsBuilder.add(new StrongPair<>(guiceWatcherRegistration.matcher(), guiceWatcherRegistration.watcher()));
         }
         return registrationsBuilder.build();
     }
 
     @Override
     public List<Id<?>> listenerIds() {
         List<Pair<Registration, Watcher>> listenerRegistrations = this.listenerRegistrations;
         return Lists.transform(listenerRegistrations, new Function<Pair<Registration, Watcher>, Id<?>>() {
             @Nullable
             @Override
             public Id<?> apply(@Nullable Pair<Registration, Watcher> pair) {
                 checkNotNull(pair, "pair");
                 Registration registration = checkNotNull(pair.left(), "pair.left");
                 return registration.id();
             }
         });
     }
 
     @Override
     public List<Id<?>> ids() {
         return listenerIds();
     }
 
     @Override
     public void clear() {
         List<Pair<Registration, Watcher>> listenerRegistrations = this.listenerRegistrations;
         for (Pair<Registration, Watcher> listenerRegistration : listenerRegistrations) {
             registry.removeWatcher(listenerRegistration.left());
         }
         listenerRegistrations.clear();
     }
 }
