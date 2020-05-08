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
 package com.nesscomputing.config;
 
 
 import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.util.Providers;
 
 /**
  * Binding configuration to Guice. This is a pretty simple module right now but
  * it might grow over time.
  *
  * To get access to the System configuration, import this module.
  *
  */
 public class ConfigModule extends AbstractModule
 {
    private final Provider<Config> configProvider;
 
     public static ConfigModule forTesting(final String ... keyValuePairs)
     {
         return new ConfigModule(Config.getFixedConfig(keyValuePairs));
     }
 
     public static ConfigModule forTesting()
     {
         return new ConfigModule(Config.getEmptyConfig());
     }
 
     /**
      * Create a new config module that uses the system properties 'ness.config.location' and 'ness.config' to determine what configurations to load.
      */
     public ConfigModule()
     {
        // Use a provider to load during the Injector creating stage, rather than the module instantiaton itself.
        // In the case where the Config binding is later overridden by Modules.override, this will ensure we do not
        // go out and build the Config object anyway -- that may fail due to e.g. missing System properties
        this.configProvider = new Provider<Config>()
        {
            @Override
            public Config get()
            {
                return Config.getConfig();
            }
        };
     }
 
     /**
      * Bind the config module as system configuration.
      *
      */
     public ConfigModule(final Config config)
     {
        this.configProvider = Providers.of(config);
     }
 
     /**
      * @deprecated Use {@link ConfigModule#forTesting(String ... keyValuePairs)}
      */
     @Deprecated
     public ConfigModule(final String ... keyValuePairs)
     {
         this(Config.getFixedConfig(keyValuePairs));
     }
 
     @Override
     public void configure()
     {
        bind(Config.class).toProvider(configProvider).in(Scopes.SINGLETON);
     }
 
     protected Config getConfig()
     {
        return configProvider.get();
     }
 }
