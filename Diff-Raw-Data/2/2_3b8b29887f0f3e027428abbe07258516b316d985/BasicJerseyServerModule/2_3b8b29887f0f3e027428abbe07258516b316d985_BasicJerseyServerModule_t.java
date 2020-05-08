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
 package com.nesscomputing.jersey;
 
 
 import com.google.inject.AbstractModule;
 import com.nesscomputing.config.Config;
 import com.nesscomputing.httpserver.selftest.SelftestModule;
 import com.nesscomputing.jdbi.argument.ArgumentFactoryModule;
 import com.nesscomputing.jdbi.metrics.DatabaseMetricsModule;
 import com.nesscomputing.jersey.exceptions.NessJerseyExceptionMapperModule;
 import com.nesscomputing.jersey.filter.BodySizeLimitResourceFilterFactory;
 import com.nesscomputing.jersey.json.NessJacksonJsonProvider;
 import com.nesscomputing.jmx.jolokia.JolokiaModule;
 import com.nesscomputing.jmx.starter.guice.JmxStarterModule;
 import com.sun.jersey.guice.JerseyServletModule;
 import com.yammer.metrics.guice.InstrumentationModule;
 
 /**
  * Setup a basic HTTP server with Jersey and database modules.
  *
 * @deprecated Use the ness-server and ness-server-templates components and {@link  com.nesscomputing.server.templates.BasicGalaxyServerModule}.
  */
 @Deprecated
 public class BasicJerseyServerModule extends AbstractModule {
     private final Config config;
     private final String[] paths;
 
     public BasicJerseyServerModule(Config config) {
         this(config, "/*");
     }
 
     public BasicJerseyServerModule(Config config, String... paths) {
         this.config = config;
         this.paths = paths;
     }
 
     @Override
     protected void configure() {
         binder().requireExplicitBindings();
         binder().disableCircularProxies();
 
         install(new JolokiaModule());
 
         install(new JerseyServletModule());
         install(new NessJerseyServletModule(config, paths));
         install(new JmxStarterModule(config));
         install(new InstrumentationModule());
         install(new DatabaseMetricsModule());
         install(new ArgumentFactoryModule());
         install (new NessJerseyExceptionMapperModule());
 
         install(new SelftestModule());
 
         NessJerseyBinder.bindResourceFilterFactory(binder()).to(BodySizeLimitResourceFilterFactory.class);
 
         bind (NessJacksonJsonProvider.class);
     }
 }
