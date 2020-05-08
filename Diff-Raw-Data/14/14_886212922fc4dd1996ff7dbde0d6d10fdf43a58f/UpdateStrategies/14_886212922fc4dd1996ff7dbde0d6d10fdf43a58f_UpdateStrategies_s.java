 package org.zeroturnaround.liverebel.plugins;
 
 /**
  * Interface for getting selected update strategies from plugin UI.
  */
 public interface UpdateStrategies {
 
   /**
    * @return the user selected primary update strategy
    */
   UpdateMode getPrimaryUpdateStrategy();
 
   /**
    * @return the user selected fallback update strategy
    */
   UpdateMode getFallbackUpdateStrategy();
 
   /**
    * @return <code>true</code> if the user has allowed to use Hotpatching even if the archives are compatible with warning
    */
   boolean updateWithWarnings();
 
   /**
    * @return the session drain timeout in seconds when using rolling restarts
    */
   int getSessionDrainTimeout();
 
   /**
    * @return the request pause timeout in seconds when using Hotpatching
    */
   int getRequestPauseTimeout();
 
 }
