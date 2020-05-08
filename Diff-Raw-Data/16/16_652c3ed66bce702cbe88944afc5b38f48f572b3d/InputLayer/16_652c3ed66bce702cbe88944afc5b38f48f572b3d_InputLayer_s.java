 package io.metacake.core.input.system;
 
 import io.metacake.core.input.Action;
 import io.metacake.core.input.ActionTrigger;
 import io.metacake.core.input.CakeEvent;
 import io.metacake.core.input.InputSystem;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * This class is the core implementation of the input system. It handles taking events from devices
  * and translating them to actions, that are given to the processing layer
  * @author florence
  * @author rpless
  */
 public class InputLayer implements InputSystem, CakeEventHandler{
     List<Action> queuedActions = new LinkedList<>();
     List<ActionTrigger> currentBindingTriggers = new LinkedList<>();
     List<InputDevice> devices;
 
     public InputLayer(List<InputDevice> devices) {
         this.devices = devices;
     }
 
     @Override
    public List<Action> getAndCleanActions() {
         List<Action> tmp = queuedActions;
         clearActions();
         return tmp;
     }
 
     @Override
     public void clearActions() {
         this.queuedActions = new LinkedList<>();
     }
 
     @Override
     public void setActionTriggers(List<ActionTrigger> ts) {
         this.currentBindingTriggers = ts;
     }
 
     @Override
     public void handle(CakeEvent e) {
         for(ActionTrigger t : currentBindingTriggers){
             if(t.shouldTriggerAction(e)) {
                 queuedActions.add(t.triggerAction(e));
             }
         }
     }
 
     /**
      * Safely shutdown the whole input system
      */
     public void shutdown() {
         for(InputDevice i : devices) {
             i.shutdown();
         }
     }
 }
