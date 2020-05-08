 /*
  * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
  *
  * This program is licensed to you under the Apache License Version 2.0,
  * and you may not use this file except in compliance with the Apache License Version 2.0.
  * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the Apache License Version 2.0 is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
  */
 package org.sonatype.sisu.siesta.server.internal.jersey;
 
 import com.google.common.collect.Lists;
 import org.sonatype.sisu.siesta.server.ApplicationContainer;
 import com.sun.jersey.api.core.DefaultResourceConfig;
 import com.sun.jersey.api.core.ResourceConfig;
 import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
 import com.sun.jersey.spi.container.ContainerListener;
 import com.sun.jersey.spi.container.ContainerNotifier;
 import com.sun.jersey.spi.container.WebApplication;
 import com.sun.jersey.spi.container.servlet.ServletContainer;
 import com.sun.jersey.spi.container.servlet.WebConfig;
 import com.sun.jersey.spi.container.servlet.WebServletConfig;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.servlet.ServletException;
 import javax.ws.rs.Path;
 import javax.ws.rs.core.Application;
 import java.util.List;
 import java.util.Map;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.base.Preconditions.checkState;
 
 /**
  * Jersey JAX-RS {@link ApplicationContainer}.
  *
  * @since 1.0
  */
 @Named
 public class JerseyContainer
     extends ServletContainer
     implements ApplicationContainer, ContainerNotifier
 {
     private static final Logger log = LoggerFactory.getLogger(JerseyContainer.class);
 
     private final List<ContainerListener> listeners = Lists.newArrayList();
 
     private final IoCComponentProviderFactory componentProviderFactory;
 
     private ResourceConfig resourceConfig;
 
     @Inject
     public JerseyContainer(final IoCComponentProviderFactory componentProviderFactory) throws Exception {
         this.componentProviderFactory = checkNotNull(componentProviderFactory);
     }
 
     @Override
     protected ResourceConfig getDefaultResourceConfig(final Map<String, Object> props, final WebConfig webConfig) throws ServletException {
         DefaultResourceConfig config = new DefaultResourceConfig();
         // Need to attach a dummy resource, as Jersey will fail to initialize if there are no root resources configured
         config.getSingletons().add(new DummyResource());
         return config;
     }
 
     /**
      * Dummy root resource to configure Jersey with so that it can initialize, it needs at least one root resource or it will puke.
      */
     @Path("internal-dummy-root-resource-for-jersey")
     private static class DummyResource
     {
         // empty
     }
 
     protected void initiate(final ResourceConfig config, final WebApplication webApp) {
         this.resourceConfig = checkNotNull(config);
         webApp.initiate(config, componentProviderFactory);
     }
 
     @Override
     public void init() throws ServletException {
         init(new WebServletConfig(this)
         {
             @Override
             public ResourceConfig getDefaultResourceConfig(final Map<String, Object> props) throws ServletException {
                 checkNotNull(props);
                 props.put(ResourceConfig.PROPERTY_CONTAINER_NOTIFIER, JerseyContainer.this);
                props.put(ResourceConfig.FEATURE_XMLROOTELEMENT_PROCESSING, Boolean.TRUE.toString());
                 return super.getDefaultResourceConfig(props);
             }
         });
     }
 
     public void addListener(final ContainerListener listener) {
         checkNotNull(listener);
         log.trace("Adding listener: {}", listener);
         listeners.add(listener);
     }
 
     public void add(final Application application) {
         checkNotNull(application);
         checkState(resourceConfig != null);
         log.trace("Adding application: {}", application);
         assert resourceConfig != null; // keep IDEA happy
         resourceConfig.add(application);
         for (ContainerListener listener : listeners) {
             listener.onReload();
         }
     }
 
     //public void remove(final Application application) {
     //    checkNotNull(application);
     //    throw new UnsupportedOperationException();
     //}
 }
