 package io.metacake.core.process;
 
 import io.metacake.core.common.MilliTimer;
 import io.metacake.core.input.Action;
 import io.metacake.core.input.InputSystem;
 import io.metacake.core.output.OutputSystem;
 
 /**
  * This class run is the main execution loop of the game
  *
  * @author florence
  * @author rpless
  */
 public class GameRunner {
     InputSystem inputSystem;
     OutputSystem outputSystem;
     private boolean isRunning = false;
 
     public GameRunner(InputSystem inputSystem, OutputSystem outputSystem) {
         this.inputSystem = inputSystem;
         this.outputSystem = outputSystem;
     }
 
     /**
      * Execute the main game loop in the current thread. This method returns after #stop has been called.
      * <p>The game loop will attempt to put {@code interval} milliseconds between the start of each game loop.
      * This WILL fail if the GameState#tick takes more than {@code interval} milliseconds to run</p>
      * <p>When #stop is called the function will terminate after the current state finishes its tick cycle</p>
      * @param state the initial state of the game
      * @param interval the number of milliseconds requested to be between the start of each loop.
      */
     public void mainLoop(GameState state, long interval) {
         isRunning = true;
         MilliTimer timer = new MilliTimer(interval);
         // TODO: excpetion handling
         while (isRunning) {
             outputSystem.addToRenderQueue(state);
             updateTriggers(state);
             updateRecognizers(state);
             state = state.tick();
             timer.block();
         }
     }
 
     /**
      * Tell the main game loop to stop. If the main game loop is not running, this has no effect.
      */
     public void stop(){
         isRunning = false;
     }
 
     /**
      * If the state requests the the input system gets new action triggers, give them.
      * @param s The current state
      */
     private void updateTriggers(GameState s){
         if(s.shouldReplaceActionTriggers()) {
             inputSystem.setActionTriggers(s.getNewActionTriggers());
         }
     }
 
     /**
      * handle the clearing actions and passing those actions to the state recognizers.
      * @param s the current state
      */
     private void updateRecognizers(GameState s){
         if(s.shouldClearActions()){
             inputSystem.clearActions();
         } else {
             // CONCERN: This might be two slow. maybe we need to redesign action->recognizer bindings.
            for (Action a : inputSystem.getAndCleanActions()) {
                 for(ActionRecognizer r : s.getRecognizers()) {
                     r.actionOccurred(a);
                 }
             }
         }
     }
 }
