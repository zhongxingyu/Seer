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
 import com.google.inject.Binding;
 import com.google.inject.Injector;
 import com.google.inject.Key;
 import com.google.inject.TypeLiteral;
 import org.javabits.yar.Id;
 import org.javabits.yar.Registration;
 import org.javabits.yar.Registry;
 
 import javax.annotation.Nullable;
 import javax.inject.Inject;
 import java.util.List;
 
 import static com.google.common.collect.Lists.newArrayList;
 import static com.google.common.collect.Lists.transform;
 
 /**
  * TODO comment
  * This class handles the register the providing supplier into the registry.
  * Date: 2/10/13
  *
  * @author Romain Gilles
  */
 public class RegistrationBindingHandler implements RegistrationHandler {
     private final Injector injector;
     private final Registry registry;
     volatile private List<Registration<?>> registrations;
 
     @Inject
     public RegistrationBindingHandler(Injector injector, Registry registry) {
         this.injector = injector;
         this.registry = registry;
     }
 
     @Override
     public void init() {
         registrations = registerBindings();
     }
 
     private List<Registration<?>> registerBindings() {
         List<Registration<?>> registrationsBuilder = newArrayList();
         for (Pair<Id, GuiceSupplier> idGuiceSupplierPair : getSuppliers()) {
             registrationsBuilder.add(putRegistrationToRegistry(idGuiceSupplierPair));
         }
         return registrationsBuilder;
     }
 
     //enforce load all providers before register them
     private List<Pair<Id, GuiceSupplier>> getSuppliers() {
         ImmutableList.Builder<Pair<Id, GuiceSupplier>> suppliersBuilder = ImmutableList.builder();
         for (Binding<GuiceRegistration> registrationBinding : injector.findBindingsByType(TypeLiteral.get(GuiceRegistration.class))) {
             Key<?> key = registrationBinding.getProvider().get().key();
             suppliersBuilder.add(newPair(key));
         }
         return suppliersBuilder.build();
     }
 
     @SuppressWarnings("unchecked")
     private StrongPair<Id, GuiceSupplier> newPair(Key<?> key) {
         return new StrongPair<Id, GuiceSupplier>(GuiceId.of(key), new GuiceSupplier(injector.getProvider(key)));
     }
 
     @SuppressWarnings("unchecked")
     private Registration<?> putRegistrationToRegistry(Pair<Id, GuiceSupplier> idGuiceSupplierPair) {
         return registry.put(idGuiceSupplierPair.left(), idGuiceSupplierPair.right());
     }
 
     @Override
     public List<Id<?>> registrations() {
         List<Registration<?>> registrations = this.registrations;
         return transform(registrations, new Function<Registration<?>, Id<?>>() {
             @Nullable
             @Override
             public Id<?> apply(@Nullable Registration<?> registration) {
                 if (registration == null) {
                     throw new NullPointerException("registration");
                 }
                 return registration.id();
             }
         });
     }
 
     @Override
     public List<Id<?>> ids() {
         return registrations();
     }
 
     @Override
     public void clear() {
         List<Registration<?>> registrations = this.registrations;
         for (Registration<?> registration : registrations) {
             registry.remove(registration);
         }
         registrations.clear();
     }
 }
