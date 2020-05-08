 package org.gethydrated.hydra.core.config;
 
 import org.gethydrated.hydra.api.configuration.ConfigItemNotFoundException;
 
 /**
  * 
 * @author 
  * @since 0.1.0
  *
  */
 public class ConfigurationInitializer {
 
     /**
      * @var configuration.
      */
     private Configuration cfg;
 
     /**
      * 
      * @param config .
      */
     public ConfigurationInitializer(final Configuration config) {
         this.cfg = config;
     }
 
     /**
      * 
      * @throws ConfigItemNotFoundException .
      */
     public final void configure() throws ConfigItemNotFoundException {
         //TO-DO: xml configurator.
         BasicConfigurator.configure(cfg);
     }
 }
 
