 package io.metacake.core.process.state;
 
 import io.metacake.core.common.CustomizableMap;
 import io.metacake.core.input.ActionTrigger;
 import io.metacake.core.output.Renderable;
 import io.metacake.core.process.ActionRecognizer;
 import io.metacake.core.process.ActionRecognizerName;
 
 import java.util.Collection;
 import java.util.Optional;
 
 /**
  * A GameState represents a state that a game can be in.
  * @author florence
  * @author rpless
  */
 public interface GameState extends Renderable {
 
     /**
      * Perform one cycle of the game loop
      * @param delta The time in milliseconds since tick was last called.
     * @param recognizers The bundle of {@link io.metacake.core.process.ActionRecognizer}s that are in use for this state.
      * @return Returns the next state, which could be the same state.
      */
     public GameState tick(long delta, CustomizableMap<ActionRecognizerName, ActionRecognizer> recognizers);
 
     // TODO: mechanism for adding and removing individual triggers (do we want bundles?)
 
     /**
      * @return Returns an {@code Optional} that may contain a new collection of
      * {@link io.metacake.core.input.ActionTrigger}s that are now in use.
      */
     public Optional<Collection<ActionTrigger>> replaceActionTriggers();
 
     /**
      * @return Returns an {@code Optional} that may contain a new collection of
      * {@link io.metacake.core.input.ActionTrigger}s that are now in use.
      */
     public Optional<Collection<ActionRecognizer>> replaceActionRecognizers();
 
     /**
      * @return true if the game is over
      */
     public boolean isGameOver();
 }
