 package io.metacake.core.process.state;
 
 import io.metacake.core.common.CustomizableMap;
 import io.metacake.core.input.ActionTrigger;
 import io.metacake.core.output.RenderingInstructionBundle;
 import io.metacake.core.process.ActionRecognizer;
 import io.metacake.core.process.ActionRecognizerName;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Optional;
 
 /**
  * This class is meant to handle state transitions that require special instructions
  * to be passed to the engine
  * @author florence
  * @author rpless
  */
 public class TransitionState implements GameState {
     private GameState next;
     private Collection<ActionTrigger> triggers;
     private Collection<ActionRecognizer> recognizers;
 
     /**
      * Create a transition state that replaces all triggers with the given ones
      * @param g the next state
      * @param l the triggers to replace
      * @return the transition state
      */
     public static TransitionState transitionWithTriggers(GameState g, Collection<ActionTrigger> l, Collection<ActionRecognizer> r) {
         return new TransitionState(g, l, r);
     }
 
     private TransitionState(GameState next, Collection<ActionTrigger> triggers, Collection<ActionRecognizer> recognizers) {
         this.next = next;
         this.triggers = triggers;
         this.recognizers = recognizers;
     }
 
     @Override
     public GameState tick(long delta, CustomizableMap<ActionRecognizerName, ActionRecognizer> recognizers) {
         return next;
     }
 
     @Override
     public Optional<Collection<ActionTrigger>> replaceActionTriggers() {
         return Optional.of(triggers);
     }
 
     @Override
     public Optional<Collection<ActionRecognizer>> replaceActionRecognizers() {
         return Optional.of(recognizers);
     }
 
     @Override
     public RenderingInstructionBundle renderingInstructions() {
         return RenderingInstructionBundle.EMPTY_BUNDLE;
     }
 
     @Override
     public final boolean isGameOver(){
         return false;
     }
 }
