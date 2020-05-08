 /**
  * Copyright (C) 2012 Ness Computing, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.nesscomputing.httpclient.guice;
 
 
 import java.lang.annotation.Annotation;
 import java.util.Collections;
 import java.util.Map;
 import java.util.Set;
 
 import javax.annotation.Nonnull;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Sets;
 import com.google.inject.AbstractModule;
 import com.google.inject.Binder;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.google.inject.Key;
 import com.google.inject.Provider;
 import com.google.inject.Scopes;
 import com.google.inject.TypeLiteral;
 import com.google.inject.binder.LinkedBindingBuilder;
 import com.google.inject.multibindings.Multibinder;
 import com.google.inject.name.Named;
 import com.google.inject.name.Names;
 import com.nesscomputing.config.ConfigProvider;
 import com.nesscomputing.httpclient.HttpClient;
 import com.nesscomputing.httpclient.HttpClientDefaults;
 import com.nesscomputing.httpclient.HttpClientObserver;
 import com.nesscomputing.httpclient.factory.httpclient4.ApacheHttpClient4Factory;
 import com.nesscomputing.httpclient.internal.HttpClientFactory;
 import com.nesscomputing.lifecycle.LifecycleStage;
 import com.nesscomputing.lifecycle.guice.AbstractLifecycleProvider;
 import com.nesscomputing.lifecycle.guice.LifecycleAction;
 
 /**
  * Guice module to bind an instance of a HttpClient. Each HttpClient should be annotated or named so that
  * other pieces of code can request a specific instance of the HttpClient.
  */
 public class HttpClientModule extends AbstractModule
 {
     public static final String DEFAULT_NAME = "default";
     public static final Named DEFAULT_NAMED = Names.named(DEFAULT_NAME);
 
     private final String clientName;
 
     /**
      * Bind an anonymous, unannotated HttpClient.  Guice 3.0 actually seems to get it right that this is not
      * exposed for arbitrary requests with annotations.
      */
     public HttpClientModule()
     {
         this.clientName = DEFAULT_NAME;
     }
 
     /**
      * Bind an named HttpClient instance.
      * @param clientName The name to use. This can be used with the Named annotation in other parts of the code.
      */
     public HttpClientModule(@Nonnull final String clientName)
     {
         Preconditions.checkArgument(clientName != null, "client name can not be null");
         Preconditions.checkArgument(!DEFAULT_NAME.equals(clientName), "client name can not be '%s'", DEFAULT_NAME);
 
         this.clientName = clientName;
     }
 
     @Override
     public void configure()
     {
         final Map<String, String> optionMap = Collections.singletonMap("httpclient_name", clientName);
         final Annotation annotation = Names.named(clientName);
 
         bind(HttpClientDefaults.class).annotatedWith(annotation).toProvider(ConfigProvider.of(null, HttpClientDefaults.class, optionMap)).in(Scopes.SINGLETON);
         bind(HttpClientFactory.class).annotatedWith(annotation).toProvider(new ApacheHttpClient4FactoryProvider(annotation)).in(Scopes.SINGLETON);
         bind(HttpClient.class).annotatedWith(annotation).toProvider(new HttpClientProvider(annotation)).asEagerSingleton();
 
         if (clientName.equals(DEFAULT_NAME)) {
             bind(HttpClient.class).toProvider(new HttpClientProvider(annotation)).asEagerSingleton();
         }
     }
 
     /**
      * Register a HttpClient observer which observes *every* Guice-bound HttpClient.
      * @return the binding builder you should register with
      */
     public static LinkedBindingBuilder<HttpClientObserver> bindNewObserver(final Binder binder)
     {
         return Multibinder.newSetBinder(binder, HttpClientObserver.class).addBinding();
     }
 
     /**
      * Register a HttpClient observer which observes only requests from a HttpClient with the given Guice binding annotation.
      * @return the binding builder you should register with
      */
     public static LinkedBindingBuilder<HttpClientObserver> bindNewObserver(final Binder binder, final Annotation annotation)
     {
         return Multibinder.newSetBinder(binder, HttpClientObserver.class, annotation).addBinding();
     }
 
     /**
      * Provides an instance of a HttpClient. Retrieves all its dependencies based off an annotation.
      */
     static final class HttpClientProvider extends AbstractLifecycleProvider<HttpClient> implements Provider<HttpClient>
     {
         private HttpClientDefaults  httpClientDefaults = null;
         private HttpClientFactory httpClientFactory = null;
 
         private final Annotation annotation;
 
         private HttpClientProvider(@Nonnull final Annotation annotation)
         {
             this.annotation = annotation;
 
             addAction(LifecycleStage.START_STAGE, new LifecycleAction<HttpClient>() {
                 @Override
                 public void performAction(final HttpClient httpClient) {
                     httpClient.start();
                 }
             });
 
             addAction(LifecycleStage.STOP_STAGE, new LifecycleAction<HttpClient>() {
                 @Override
                 public void performAction(final HttpClient httpClient) {
                     httpClient.stop();
                 }
             });
         }
 
         @Inject
         public void setInjector(final Injector injector)
         {
             this.httpClientDefaults = injector.getInstance(Key.get(HttpClientDefaults.class, annotation));
             this.httpClientFactory = injector.getInstance(Key.get(HttpClientFactory.class, annotation));
         }
 
         @Override
         public HttpClient internalGet()
         {
             return new HttpClient(httpClientFactory, httpClientDefaults);
         }
     }
 
     static final class ApacheHttpClient4FactoryProvider implements Provider<HttpClientFactory>
     {
         private static final TypeLiteral<Set<HttpClientObserver>> OBSERVER_TYPE_LITERAL = new TypeLiteral<Set<HttpClientObserver>>() {};
         private final Annotation annotation;
 
        private Injector injector;
 
         private ApacheHttpClient4FactoryProvider(@Nonnull final Annotation annotation)
         {
             this.annotation = annotation;
         }
 
         @Inject
         void setInjector(final Injector injector)
         {
            this.injector = injector;
         }
 
         private Set<HttpClientObserver> findObservers(final Injector injector, final Key<Set<HttpClientObserver>> key)
         {
             if (injector.getExistingBinding(key) != null) {
                 return injector.getInstance(key);
             }
             else {
                 return Collections.emptySet();
             }
         }
 
         @Override
         public HttpClientFactory get()
         {
            final Set<HttpClientObserver> httpClientObservers = Sets.union(
                    findObservers(injector, Key.get(OBSERVER_TYPE_LITERAL)),
                    findObservers(injector, Key.get(OBSERVER_TYPE_LITERAL, annotation)));

            final HttpClientDefaults httpClientDefaults = injector.getInstance(Key.get(HttpClientDefaults.class, annotation));
             return new ApacheHttpClient4Factory(httpClientDefaults, httpClientObservers);
         }
     }
 }
