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
 
 import com.google.common.annotations.Beta;
 import com.google.inject.AbstractModule;
 import com.google.inject.Key;
 import com.google.inject.Provides;
 import com.google.inject.TypeLiteral;
 import com.google.inject.matcher.Matcher;
 import org.javabits.yar.BlockingSupplier;
 import org.javabits.yar.Supplier;
 
 import java.lang.reflect.Method;
 import java.util.Collection;
 import java.util.List;
 
 import static org.javabits.yar.guice.RegistrationBindingBuilderImpl.bindRegistration;
 
 /**
  * Extension of the support class {@code AbstractModule} that adds to the Guice EDSL
  * features to work with Yar {@code Registry}.
  * In Yar {@code Registry} three concepts works together:
  * <ul>
  * <li>Registering {@code Supplier} under an {@code Id}</li>
  * <li>Getting a or a list of {@code Supplier}(s) for a given {@code Id}</li>
  * <li>And then listening mutating operation on the {@code Registry}</li>
  * </ul>
  * This class will help you to work with those concepts. You should subclass this module
  * to work with the registry in the {@link #configureRegistry()} method.
  * <p>
  * Date: 2/28/13
  * </p>
  *
  * @author Romain Gilles
  */
 public abstract class AbstractRegistryModule extends AbstractModule {
 
     @Override
     protected final void configure() {
         doBeforeConfiguration();
         configureRegistry();
         bindProviderMethods();
     }
 
     void doBeforeConfiguration() {
     }
 
     private void bindProviderMethods() {
         for (Method method : this.getClass().getDeclaredMethods()) {
             if (method.isAnnotationPresent(Register.class) && method.isAnnotationPresent(Provides.class)) {
                 bindRegistration(binder(), Key.get(method.getGenericReturnType()));
             }
         }
     }
 
     /**
      * <h1>Registry Management EDSL</h1>
      * Guice injector:
      * <pre>
      * Guice.createInjector(..., new AbstractRegistryModule() {
      *     protected void configureRegistry() {
      *         register(MyProvidedInterface.class).to(MyImplementation.class);
      *         bind(MyRequiredInterface.class).toRegistry();
      *         ...
      *     }
      * })
      * </pre>
      * <p><b>Note:</b> You must have one and only one {@link RegistryModule} in your
      * {@code Injector} construction process. You may need to have a look to this class documentation
      * to follow the initialization process.
      * </p>
      * <p><h2>Getting Supplier(s)</h2>
      * Your component may need services that are located in the registry.
      * <h3>Bind service from registry</h3>
      * Imagine that you required a {@code DataSource} provided by a other component.
      * <h4>Simplest</h4>
      * {@code bind(DataSource.class).toRegistry();}
      * <h4>Named binding</h4>
      * {@code bind(DataSource.class).annotatedWith(Names.named("data-source-xyz")).toRegistry();}
      * <h4>Annotated binding</h4>
      * {@code bind(DataSource.class).annotatedWith(DataSourceXYZAnnotation.class).toRegistry();}
      * <h4>TypeLiteral or Key binding</h4>
      * <pre>{@literal
      * // literal
      * bind(new TypeLiteral<DataSource>(){}).toRegistry();
      * // key
      * bind(Key.get(DataSource.class, Names.named("data-source-xyz"))).toRegistry();
      * }</pre>
      * <h4>Bind a Iterable / Collection / List</h4>
      * The {@code Registry} can return List of suppliers for a given Id.
      * You have a translation of this feature in this EDSL.
      * You can bind {@code Iterable} / {@code Collection} / {@code List} of both direct target
      * type {@code T} or {@code Supplier<T>} as follow:
      * <pre>{@literal
      * //direct
      * bind(new TypeLiteral<Iterable<DataSource>>(){}).toRegistry();
      * bind(new TypeLiteral<Collection<DataSource>>(){}).toRegistry();
      * bind(new TypeLiteral<List<DataSource>>(){}).toRegistry();
      * //through supplier
      * bind(new TypeLiteral<Iterable<Supplier<DataSource>>>(){}).toRegistry();
      * bind(new TypeLiteral<Collection<Supplier<DataSource>>>(){}).toRegistry();
      * bind(new TypeLiteral<List<Supplier<DataSource>>>(){}).toRegistry();
      * }</pre>
      * </p>
      * <p><h2>Registering service</h2>
      * <h3>Simplest</h3>
      * {@code register(DataSource.class).to(MyDataSourcePool.class);}
      * <h3>Named registration</h3>
      * {@code register(DataSource.class).annotatedWith(Names.named("data-source-xyz")).to(MyDataSourcePool.class);}
      * <h3>Annotated registration</h3>
      * {@code register(DataSource.class).annotatedWith(DataSourceXYZAnnotation.class).to(MyDataSourcePool.class);}
      * <h3>TypeLiteral or Key registration</h3>
      * <pre>{@literal
      * // literal
      * register(new TypeLiteral<DataSource>(){}).to(MyDataSourcePool.class);
      * // key
      * register(Key.get(DataSource.class, Names.named("data-source-xyz"))).to(MyDataSourcePool.class);
      * }</pre>
      * <h3>Providers registration</h3>
      * The EDSL extansion also fully support {@code Provider} and method {@code Provides} annotation.
      * <pre>
      * Guice.createInjector(..., new AbstractRegistryModule() {
      *     protected void configureRegistry() {
      *         register(MyProvidedInterface.class).to(MyImplementation.class);
      *         ...
      *     }
      *    {@literal @Provides}
      *    {@literal @Register}
      *     public DataSource myDataSource() {
      *         return new MyDataSourcePool();
      *     }
      * })
      * </pre>
      * <h3>Collections warning</h3>
      * There is no corresponding feature with the opposite bind functionality.
      * You cannot register more than one supplier at a time.
      * </p>
      * <p><h2>Listening registry events</h2>
      * </p>
      *
      * @see RegistryModule
      */
     protected abstract void configureRegistry();
 
     @Beta
     protected <T> void bindRegistryListener(Matcher<Key<T>> matcher, Key<? extends RegistryListener<? super T>> key) {
         if (isBlockingSupplier(matcher)) {
             throw new IllegalArgumentException("Only simple Supplier are supported. BlockingSupplier are only available as injectable element (constructor/field/method param).");
         } else if (isSupplier(matcher)) {
             //TODO
         } else {
             bind(Key.get(GuiceWatcherRegistration.class, UniqueAnnotations.create())).toInstance(GuiceWatcherRegistration.get(matcher, key));
         }
     }
 
     private <T> boolean isBlockingSupplier(Matcher<Key<T>> matcher) {
         return isBlockingSupplier(Matchers.getTargetTypeLiteral(matcher));  //To change body of created methods use File | Settings | File Templates.
     }
 
     protected <T> boolean isSupplier(Matcher<Key<T>> matcher) {
         return isSupplier(Matchers.getTargetTypeLiteral(matcher));
     }
 
     @Beta
     protected <T> void bindRegistryListener(Matcher<Key<T>> matcher, RegistryListener<? super T> listener) {
         if (isBlockingSupplier(matcher)) {
             throw new IllegalArgumentException("Only simple Supplier are supported. BlockingSupplier are only available as injectable element (constructor/field/method param).");
         } else if (isSupplier(matcher)) {
             //TODO
         } else {
             bind(Key.get(GuiceWatcherRegistration.class, UniqueAnnotations.create())).toInstance(GuiceWatcherRegistration.get(matcher, listener));
         }
         requestInjection(listener);
     }
 
 
 //    protected <T> void bindListenerBounded(Matcher<Key<? extends T>> typeMatcher, RegistryListener<? super T> listener) {
 //        if (isBlockingSupplier(typeLiteral)) {
 //
 //        } else if (isSupplier(typeLiteral)) {
 //
 //        } else {
 //
 //        }
 //    }
 
     protected <T> RegistrationLinkedBindingBuilder<T> register(Key<T> key) {
         return new RegistrationBindingBuilderImpl<>(binder(), key);
     }
 
     protected <T> RegistrationAnnotatedBindingBuilder<T> register(TypeLiteral<T> typeLiteral) {
         return new RegistrationBindingBuilderImpl<>(binder(), typeLiteral);
     }
 
     // TODO cannot be scoped
     protected <T> RegistrationAnnotatedBindingBuilder<T> register(Class<T> type) {
         return new RegistrationBindingBuilderImpl<>(binder(), type);
     }
 
     @Override
     protected <T> RegistryLinkedBindingBuilder<T> bind(Key<T> key) {
         return getRegistryBindingBuilderFactory(key).newFrom(key);
     }
 
     @Override
     protected <T> RegistryAnnotatedBindingBuilder<T> bind(TypeLiteral<T> typeLiteral) {
         return getRegistryBindingBuilderFactory(typeLiteral).newFrom(typeLiteral);
     }
 
     @Override
     protected <T> RegistryAnnotatedBindingBuilder<T> bind(Class<T> clazz) {
         return new RegistryBindingBuilder<>(binder(), clazz);
     }
 
     private <T> RegistryBindingBuilderFactory getRegistryBindingBuilderFactory(Key<T> key) {
         return getRegistryBindingBuilderFactory(key.getTypeLiteral());
     }
 
     private <T> RegistryBindingBuilderFactory getRegistryBindingBuilderFactory(TypeLiteral<T> typeLiteral) {
         if (isBlockingSupplier(typeLiteral)) {
             return new BlockingSupplierRegistryBindingBuilderFactory();
         }
         if (isSupplier(typeLiteral)) {
             return new SupplierRegistryBindingBuilderFactory();
         }
         if (isSupportedCollectionsInterface(typeLiteral)) {
             return new CollectionsRegistryBindingBuilderFactory();
         }
         return new SimpleRegistryBindingBuilderFactory();
     }
 
     private <T> boolean isBlockingSupplier(TypeLiteral<T> typeLiteral) {
         return BlockingSupplier.class.isAssignableFrom(typeLiteral.getRawType());
     }
 
     private <T> boolean isSupplier(TypeLiteral<T> typeLiteral) {
        return Supplier.class.isAssignableFrom(typeLiteral.getRawType())
                || com.google.common.base.Supplier.class.isAssignableFrom(typeLiteral.getRawType());
     }
 
     private <T> boolean isSupportedCollectionsInterface(TypeLiteral<T> typeLiteral) {
         return isClassEqualsToLiteralRowType(List.class, typeLiteral)
                 || isClassEqualsToLiteralRowType(Collection.class, typeLiteral)
                 || isClassEqualsToLiteralRowType(Iterable.class, typeLiteral);
     }
 
     private <T> boolean isClassEqualsToLiteralRowType(Class<?> type, TypeLiteral<T> typeLiteral) {
         return type.equals(typeLiteral.getRawType());
     }
 
     private interface RegistryBindingBuilderFactory {
         <T> RegistryAnnotatedBindingBuilder<T> newFrom(TypeLiteral<T> typeLiteral);
 
         <T> RegistryLinkedBindingBuilder<T> newFrom(Key<T> key);
     }
 
     private class SimpleRegistryBindingBuilderFactory implements RegistryBindingBuilderFactory {
         @Override
         public <T> RegistryAnnotatedBindingBuilder<T> newFrom(TypeLiteral<T> typeLiteral) {
             return new RegistryBindingBuilder<>(binder(), typeLiteral);
         }
 
         @Override
         public <T> RegistryLinkedBindingBuilder<T> newFrom(Key<T> key) {
             return new RegistryBindingBuilder<>(binder(), key);
         }
     }
 
     private class SupplierRegistryBindingBuilderFactory implements RegistryBindingBuilderFactory {
         @Override
         public <T> RegistryAnnotatedBindingBuilder<T> newFrom(TypeLiteral<T> typeLiteral) {
             return new SupplierRegistryBindingBuilder<>(binder(), typeLiteral);
         }
 
         @Override
         public <T> RegistryLinkedBindingBuilder<T> newFrom(Key<T> key) {
             return new SupplierRegistryBindingBuilder<>(binder(), key);
         }
     }
 
     private class BlockingSupplierRegistryBindingBuilderFactory implements RegistryBindingBuilderFactory {
         @Override
         public <T> RegistryAnnotatedBindingBuilder<T> newFrom(TypeLiteral<T> typeLiteral) {
             return new BlockingSupplierRegistryBindingBuilder<>(binder(), typeLiteral);
         }
 
         @Override
         public <T> RegistryLinkedBindingBuilder<T> newFrom(Key<T> key) {
             return new BlockingSupplierRegistryBindingBuilder<>(binder(), key);
         }
     }
 
     private class CollectionsRegistryBindingBuilderFactory implements RegistryBindingBuilderFactory {
         @Override
         public <T> RegistryAnnotatedBindingBuilder<T> newFrom(TypeLiteral<T> typeLiteral) {
             return new CollectionsRegistryBindingBuilder<>(binder(), typeLiteral);
         }
 
         @Override
         public <T> RegistryLinkedBindingBuilder<T> newFrom(Key<T> key) {
             return new CollectionsRegistryBindingBuilder<>(binder(), key);
         }
     }
 }
