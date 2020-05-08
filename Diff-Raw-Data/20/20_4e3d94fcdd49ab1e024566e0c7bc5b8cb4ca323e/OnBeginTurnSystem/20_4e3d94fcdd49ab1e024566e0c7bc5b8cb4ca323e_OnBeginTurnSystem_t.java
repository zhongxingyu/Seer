 
 package com.wartricks.systems;
 
 import com.artemis.Aspect;
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.annotations.Mapper;
 import com.artemis.systems.EntityProcessingSystem;
 import com.wartricks.components.Action;
 import com.wartricks.components.ActionSequenceOnBeginTurn;
 import com.wartricks.logic.VersusGame;
 
 public class OnBeginTurnSystem extends EntityProcessingSystem {
     private VersusGame game;
 
     @Mapper
     ComponentMapper<ActionSequenceOnBeginTurn> asm;
 
     @SuppressWarnings("unchecked")
     public OnBeginTurnSystem(VersusGame game) {
         super(Aspect.getAspectForAll(ActionSequenceOnBeginTurn.class));
         this.game = game;
     }
 
     @Override
     protected void process(Entity e) {
         final ActionSequenceOnBeginTurn sequence = asm.get(e);
         if (sequence.onBeginTurnActions.size() > 0) {
             for (final Action action : sequence.onBeginTurnActions) {
                 game.executor.execute(action.creatureId, action.skillId, action.origin,
                        action.target, "beginTurn");
             }
         }
     }
 }
