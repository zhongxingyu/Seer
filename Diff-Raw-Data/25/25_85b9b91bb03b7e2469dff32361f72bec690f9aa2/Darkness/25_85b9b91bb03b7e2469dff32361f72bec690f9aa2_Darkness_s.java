 package aigilas.entities;
 
 import aigilas.Common;
 import sps.bridge.DrawDepths;
 import sps.bridge.EntityTypes;
 import sps.bridge.SpriteTypes;
 import sps.core.Point2;
 import sps.core.Settings;
 import sps.entities.Entity;
 import sps.entities.EntityManager;
 import sps.entities.HitTest;
 
 public class Darkness extends Entity {
     public Darkness(Point2 location) {
         initialize(location, SpriteTypes.get(Common.Entities.Darkness), EntityTypes.get(Common.Entities.Darkness), DrawDepths.get(Common.Entities.Darkness));
     }
 
     boolean playerNear;
 
     @Override
     public void update() {
         Entity player = (Entity) EntityManager.get().getNearestPlayer(this);
        float distance = HitTest.getDistanceSquare(player, this);
        if (distance < Settings.get().spriteWidth * Settings.get().spriteWidth * 6) {
            _graphic.setAlpha(0);
            playerNear = true;
         }
         else {
             _graphic.setAlpha(1);
             playerNear = false;
         }
     }
 
     public boolean beingLit() {
         return playerNear;
     }
 
     @Override
    public void draw(){
        if(!playerNear){
             super.draw();
         }
     }
 }
