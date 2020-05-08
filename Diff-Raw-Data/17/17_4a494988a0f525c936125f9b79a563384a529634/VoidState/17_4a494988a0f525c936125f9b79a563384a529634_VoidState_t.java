 package io.metacake.core.process.state;
 
 import io.metacake.core.common.CustomizableMap;
 import io.metacake.core.input.ActionTrigger;
 import io.metacake.core.process.ActionRecognizer;
 import io.metacake.core.process.ActionRecognizerName;
 
 import java.util.Collection;
 
 /**
  * A VoidState is a {@link UserState} that allows for an imperative style
  * of programming in {@link GameState}s.
  * <p>
  *      Instead of overriding the {@link io.metacake.core.process.state.GameState#tick(long, io.metacake.core.common.CustomizableMap)} method
  *      a user overrides the {@link io.metacake.core.process.state.VoidState#update(long, io.metacake.core.common.CustomizableMap)} method.
  *      To transition to another state,
  */
 public abstract class VoidState extends UserState {
 
     private GameState currentState = this;
 
     @Override
     public final GameState tick(long delta, CustomizableMap<ActionRecognizerName, ActionRecognizer> recognizers) {
         update(delta, recognizers);
         return currentState;
     }
 
     /**
      * Transition to the next state.
      * <p>
      *     This method should only be called from within the an overridden {@link io.metacake.core.process.state.VoidState#update(long, io.metacake.core.common.CustomizableMap)}
      *     method. This method only sets what the state will be on the next tick cycle. It does not immediately
      *     transition the state.
      * @param gameState The state to transition to
     * @param triggers The {@link io.metacake.core.input.ActionTrigger}s to use on transition
     * @param recognizers The {@link io.metacake.core.process.ActionRecognizer}s to be use on a transition
      */
     public void setTransition(GameState gameState, Collection<ActionTrigger> triggers, Collection<ActionRecognizer> recognizers) {
         currentState = TransitionState.transitionWithTriggers(gameState, triggers, recognizers);
     }
 
     /**
      * In a {@code VoidState}, this method will be overridden instead of {@link io.metacake.core.process.state.GameState#tick(long, io.metacake.core.common.CustomizableMap)}
      * in order to update aspects of the state.
      * @param delta The number of milliseconds since update was last called.
     * @param recognizers The bundle of {@link io.metacake.core.process.ActionRecognizer}s 
      */
     public abstract void update(long delta, CustomizableMap<ActionRecognizerName, ActionRecognizer> recognizers);
 }
