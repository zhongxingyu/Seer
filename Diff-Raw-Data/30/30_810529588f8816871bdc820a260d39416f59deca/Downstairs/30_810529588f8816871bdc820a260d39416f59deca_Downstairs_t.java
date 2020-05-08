 package aigilas.entities;
 
 import aigilas.Aigilas;
 import aigilas.creatures.BaseCreature;
 import aigilas.dungeons.Dungeon;
 import sps.bridge.*;
 import sps.core.Point2;
 import sps.entities.Entity;
 import sps.entities.EntityManager;
 
 public class Downstairs extends Entity {
     public Downstairs(Point2 location) {
         initialize(location, SpriteTypes.get(Aigilas.Entities.Downstairs), EntityTypes.get(Aigilas.Entities.Downstairs), DrawDepths.get(Aigilas.DrawDepths.Stairs));
     }
 
     boolean disabled = false;
     private BaseCreature player;
 
     @Override
     public void update() {
         int nonPlayerCount = EntityManager.get().getActors(ActorTypes.get(Sps.ActorGroups.Non_Player)).size();
         int dummyCount = EntityManager.get().getActors(ActorTypes.get(Aigilas.Actors.Dummy)).size();
         int minionCount = EntityManager.get().getActors(ActorTypes.get(Aigilas.Actors.Minion)).size();
 
        int liveCount = nonPlayerCount - dummyCount - minionCount;
        if (liveCount > 0) {
             disabled = true;
             _graphic.setAlpha(0);
        }
        else {
             _graphic.setAlpha(1);
             disabled = false;
         }
 
         if (!disabled) {
             player = (BaseCreature) EntityManager.get().getTouchingCreature(this);
             if (player != null) {
                 if (player.isInteracting()) {
                     player.performInteraction();
                     Dungeon.getNextFloor();
                 }
             }
         }
     }
 }
