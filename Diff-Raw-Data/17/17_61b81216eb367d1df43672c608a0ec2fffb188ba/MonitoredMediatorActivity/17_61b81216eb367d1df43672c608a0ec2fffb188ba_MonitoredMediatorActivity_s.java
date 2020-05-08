 package org.sciflex.plugins.synapse.esper.mediators.monitors;
 
 import org.sciflex.plugins.synapse.esper.core.util.ResetEnabled;
 import org.sciflex.plugins.synapse.esper.core.util.UniquelyIdentifiableChild;
 
 /**
  * Provides necessary capability to monitor a Mediator Activity.
  */
 public interface MonitoredMediatorActivity extends ResetEnabled, UniquelyIdentifiableChild {
 
     /**
      * Starts the {@link SynapseEsperMediator}, and allows mediation activity. 
      * Unless the mediator is active, mediation won't take place.
      */
     public void startMediator();
 
     /**
      * Stops the {@link SynapseEsperMediator}, and disllows mediation activity. 
      * Unless the mediator is active, mediation won't take place.
      */
     public void stopMediator();
 
     /**
      * Refreshes the {@link SynapseEsperMediator} by starting and stopping it.
      * This operation roughly takes the same time as a start operation, or a 
      * stop operation.
      */
     public void refreshMediator();
 
     /**
      * Indicates whether the {@link SynapseEsperMediator} is active.
      *
      * @return true if active, false otherwise.
      */
     public boolean isMediatorActive();
 
     /**
      * Returns how long the {@link SynapseEsperMediator} has been active.
      *
      * @return active time in milliseconds.
      */
     public long getActiveTime();
 }
