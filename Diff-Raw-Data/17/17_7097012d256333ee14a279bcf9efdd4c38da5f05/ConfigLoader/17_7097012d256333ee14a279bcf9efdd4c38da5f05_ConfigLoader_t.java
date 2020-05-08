 /**
  * Copyright (C) 2012 - 101loops.com <dev@101loops.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.crashnote.core.config;
 
 import com.crashnote.external.config.Config;
 import com.crashnote.external.config.ConfigFactory;
 import com.crashnote.external.config.ConfigParseOptions;
 
 import java.util.Properties;
 
 /**
 * Create Config instance from various input sources.
  */
 public class ConfigLoader {
 
     // SETUP =====================================================================================
 
     public ConfigLoader() {
         // nothing to do
     }
 
 
     // INTERFACE ==================================================================================
 
     /**
     * create Config from environment props
      */
     public Config fromEnvProps() {
         return ConfigFactory.systemEnvironment();
     }
 
     /**
     * create Config from system props
      */
     public Config fromSystemProps() {
         return ConfigFactory.systemProperties();
     }
 
     /**
     * create Config from loading file
      */
     public Config fromFile(final String name) {
         return ConfigFactory.load(name + ".conf");
     }
 
     /**
     * create Config from String
      */
     public Config fromString(final String str) {
         return ConfigFactory.parseString(str);
     }
 
     /**
     * create Config from properties
      */
     public Config fromProps(final Properties props, final String descr) {
         final ConfigParseOptions opts =  ConfigParseOptions.defaults().setOriginDescription(descr);
         return ConfigFactory.parseProperties(props, opts);
     }
 }
