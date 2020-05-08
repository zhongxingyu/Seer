 
 package com.wartricks.systems;
 
 import com.artemis.Aspect;
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.annotations.Mapper;
 import com.artemis.systems.EntityProcessingSystem;
 import com.wartricks.components.Action;
 import com.wartricks.components.ActionSequenceOnEndTurn;
 import com.wartricks.logic.VersusGame;
 
 public class OnEndTurnSystem extends EntityProcessingSystem {
     private VersusGame game;
 
     @Mapper
     ComponentMapper<ActionSequenceOnEndTurn> asm;
 
     @SuppressWarnings("unchecked")
     public OnEndTurnSystem(VersusGame game) {
         super(Aspect.getAspectForAll(ActionSequenceOnEndTurn.class));
         this.game = game;
     }
 
     @Override
     protected void process(Entity e) {
         final ActionSequenceOnEndTurn sequence = asm.get(e);
         if (sequence.onEndTurnActions.size() > 0) {
             for (final Action action : sequence.onEndTurnActions) {
                 game.executor.execute(action.creatureId, action.skillId, action.origin,
                        action.target, "onEndTurn");
             }
         }
     }
 }
