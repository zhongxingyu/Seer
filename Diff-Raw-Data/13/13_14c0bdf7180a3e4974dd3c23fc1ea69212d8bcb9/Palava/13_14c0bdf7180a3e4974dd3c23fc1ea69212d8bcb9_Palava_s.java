 /**
  * Copyright 2010 CosmoCode GmbH
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
 
 package de.cosmocode.palava.core;
 
 import java.util.Properties;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Preconditions;
 import com.google.inject.ConfigurationException;
 import com.google.inject.Module;
 import com.google.inject.ProvisionException;
 import com.google.inject.Stage;
 
 import de.cosmocode.palava.core.inject.Settings;
 
 /**
  * Static factory class for framework instances.
  *
  * @author Willi Schoenborn
  */
 public final class Palava {
 
     private static final Logger LOG = LoggerFactory.getLogger(Palava.class);
     
     private Palava() {
         
     }
 
     /**
     * Constructs a new {@link Framework} using the specified properties.
      * <p>
      *   This method assumes the specified properties contain the fully
      *   qualified class name of the main module under the default configuration
     *   key {@link CoreConfig#APPLICATION}.
      * </p>
      * <p>
      *   The specified properties will be bound using the {@link Settings} annotation.
      * </p>
      * 
      * @param properties the settings
      * @return a new configured {@link Framework} instance
     * @throws NullPointerException if properties is null
     * @throws IllegalArgumentException if the specified class does not exist
      * @throws ClassCastException if the addressed main module class is no subclass of {@link Module}
      * @throws ConfigurationException if guice configuration failed
      * @throws ProvisionException if providing an instance during creation failed
      */
     public static Framework newFramework(Properties properties) {
         Preconditions.checkNotNull(properties, "Properties");
 
         final String className = properties.getProperty(CoreConfig.APPLICATION);
         Preconditions.checkNotNull(className, CoreConfig.APPLICATION);
         final Class<? extends Module> mainModuleClass;
 
         try {
             mainModuleClass = Class.forName(className).asSubclass(Module.class);
         } catch (ClassNotFoundException e) {
             throw new IllegalArgumentException(e);
         }
         
         LOG.debug("Creating new framework using {}", properties);
         return newFramework(mainModuleClass, properties);
     }
     
     /**
     * Constructs a new {@link Framework} using the specified properties.
      * <p>
      *   This method creates a new instance of the given module class using
      *   {@link Class#newInstance()}. As a consequence this class must provide
      *   a public no argument constructor.
      * </p>
      * <p>
      *   The specified properties will be bound using the {@link Settings} annotation.
      * </p>
      * 
      * @param mainModuleClass the class literal of the main module
      * @param properties the application properties
      * @return a new configured {@link Framework} instance
      * @throws NullPointerException if mainModuleClass or properties is null
      * @throws IllegalArgumentException if creating instance of mainModuleClass failed
      * @throws ConfigurationException if guice configuration failed
      * @throws ProvisionException if providing an instance during creation failed
      */
     public static Framework newFramework(Class<? extends Module> mainModuleClass, Properties properties) {
         Preconditions.checkNotNull(mainModuleClass, "MainModuleClass");
         Preconditions.checkNotNull(properties, "Properties");
         
         final Module mainModule;
 
         try {
             mainModule = mainModuleClass.newInstance();
         } catch (InstantiationException e) {
             throw new IllegalArgumentException(e);
         } catch (IllegalAccessException e) {
             throw new IllegalArgumentException(e);
         }
         
         return newFramework(mainModule, properties);
     }
     
     /**
      * Creates a {@link Framework} which loads the given {@link Module}.
      * <p>
      *   The specified properties will be bound using the {@link Settings} annotation.
      * </p>
      * 
      * @param mainModule the application main module
      * @param properties the application properties
      * @return a new configured {@link Framework} instance
      * @throws NullPointerException if mainModule or properties is null
      * @throws ConfigurationException if guice configuration failed
      * @throws ProvisionException if providing an instance during creation failed
      */
     public static Framework newFramework(Module mainModule, Properties properties) {
         Preconditions.checkNotNull(mainModule, "MainModule");
         Preconditions.checkNotNull(properties, "Properties");
         final String stageName = properties.getProperty(CoreConfig.STAGE);
         final Stage stage = StringUtils.isBlank(stageName) ? Stage.PRODUCTION : Stage.valueOf(stageName);
         return newFramework(mainModule, stage, properties); 
     }
     
     /**
      * Creates a {@link Framework} which loads the given {@link Module} using the specified
      * guice {@link Stage}.
      * <p>
      *   The specified properties will be bound using the {@link Settings} annotation.
      * </p>
      * 
      * @param mainModule the application main module
      * @param stage the desired injector stage
      * @param properties the application properties
      * @return a new configured {@link Framework} instance
      * @throws NullPointerException if mainModule, stage or properties is null
      * @throws ConfigurationException if guice configuration failed
      * @throws ProvisionException if providing an instance during creation failed
      */
     public static Framework newFramework(Module mainModule, Stage stage, Properties properties) {
         Preconditions.checkNotNull(mainModule, "MainModule");
         Preconditions.checkNotNull(stage, "Stage");
         Preconditions.checkNotNull(properties, "Properties");
         return new BootstrapFramework(mainModule, stage, properties);
     }
     
 }
