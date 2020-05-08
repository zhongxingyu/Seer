 package de.skuzzle.polly.sdk.eventlistener;
 
 import de.skuzzle.polly.sdk.Configuration;
 
 /**
  * This event is raised when the configuration changes.
  * 
  * @author Simon
  */
 public class ConfigurationEvent {
 
     private Configuration source;
     
     
     /**
      * Creates a new ConfigurationEvent with the given source.
      * 
      * @param source The source of this event.
      */
     public ConfigurationEvent(Configuration source) {
         this.source = source;
     }
     
     
     
     /**
      * Gets the source of this event.
      * 
      * @return The configuration.
      */
     public Configuration getSource() {
         return this.source;
     }
 }
