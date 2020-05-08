 
 package com.wartricks.systems;
 
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.annotations.Mapper;
 import com.artemis.managers.GroupManager;
 import com.artemis.systems.VoidEntitySystem;
 import com.artemis.utils.ImmutableBag;
 import com.badlogic.gdx.utils.Array;
 import com.wartricks.components.Action;
 import com.wartricks.components.ActionSequence;
 import com.wartricks.custom.ActionComparator;
 import com.wartricks.logic.VersusGame;
 import com.wartricks.utils.Constants.Groups;
 
 public class OnExecuteTurnSystem extends VoidEntitySystem {
     private VersusGame game;
 
     @Mapper
     ComponentMapper<ActionSequence> asm;
 
     @SuppressWarnings("unchecked")
     public OnExecuteTurnSystem(VersusGame game) {
         super();
         this.game = game;
     }
 
     @Override
     protected void processSystem() {
         final ImmutableBag<Entity> characters = game.gameWorld.getManager(GroupManager.class)
                .getEntities(Groups.CREATURE);
         final Array<Action> turn = new Array<Action>();
         do {
             for (int i = 0; i < characters.size(); i++) {
                 final ActionSequence sequence = asm.get(characters.get(i));
                 if (sequence.onCastActions.size() > 0) {
                     turn.add(sequence.onCastActions.remove(0));
                 }
             }
             turn.sort(new ActionComparator(game.gameWorld));
             for (final Action action : turn) {
                 game.gameExecutor.executeCast(action);
             }
             turn.clear();
         } while (turn.size > 0);
     }
 }
