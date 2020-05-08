 /*
  * Copyright 2012 htfv (Aliaksei Lahachou)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.github.htfv.maven.plugins.buildconfigurator.core;
 
 import java.util.ServiceLoader;
 
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.plugin.LegacySupport;
 import org.codehaus.plexus.component.annotations.Component;
 import org.codehaus.plexus.component.annotations.Requirement;
 
 import com.github.htfv.maven.plugins.buildconfigurator.core.configuration.BuildConfiguratorPluginConfiguration;
 import com.github.htfv.maven.plugins.buildconfigurator.core.configurators.ConfigurationContext;
 import com.github.htfv.maven.plugins.buildconfigurator.core.configurators.Configurator;
 
 /**
  * The core logic of the Build Configurator.
  *
  * @author htfv (Aliaksei Lahachou)
  */
 @Component(role = BuildConfigurator.class)
public final class BuildConfigurator
 {
     /**
      * Object for accessing the current {@link MavenSession}.
      */
     @Requirement
     private LegacySupport legacySupport;
 
     /**
      * Configures a project.
      *
      * @param request
      *            parameters for configuring a project.
      *
      * @return {@link ConfigureResult} object with results.
      *
      * @throws Exception
      *             if an error occurred during project configuration.
      */
     public ConfigureResult configure(final ConfigureRequest request) throws Exception
     {
         final ConfigurationContext ctx =
                 new ConfigurationContext(legacySupport.getSession(), request);
 
         final Boolean skip = ctx.getBooleanValue(
                 request.getPluginConfiguration().getSkip(),
                 BuildConfiguratorPluginConfiguration.DEFAULT_SKIP);
 
         if (!Boolean.TRUE.equals(skip))
         {
             final ServiceLoader<Configurator> configurators =
                     ServiceLoader.load(Configurator.class);
 
             for (final Configurator configurator : configurators)
             {
                 configurator.configure(ctx);
             }
         }
 
         final ConfigureResult result = new ConfigureResult.Builder()
                 .results(ctx.getResults())
                 .build();
 
         return result;
     }
 }
