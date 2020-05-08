 package actor;
 
 import math.*;
 
 public class Bullet extends Actor {
     private static final long serialVersionUID = -3860927022451699968L;
     private static final int MAX_AGE = 60 * 5; /* 60 fps * 5 seconds = 300 frames */
     private static final float BULLET_SPEED = 1.0f;
     
     public Bullet(Actor actor){
         super();
         angularVelocity = new Quaternion(Vector3.UNIT_Y, 1);
         this.velocity = actor.velocity.plus(actor.getDirection().times(BULLET_SPEED));
         position = new math.Vector3(actor.getPosition());
         setSize(.1f);
     }
     
     /**
      * 
      * @param actor
      * @param positionOffset the offset relative to the actor
      * @param direction
      */
     public Bullet(Actor actor, Vector3 positionOffset){
         this(actor);
        position.plusEquals(positionOffset);
     }
     
     @Override
     public void handleCollision(Actor other) {
         System.err.println("DEBUG: Collision Detected");
     }
     
     public void update(){
         super.update();
         if(age > MAX_AGE){
             // FIXME this throws a concurrent access exception
             //delete();
         }
     }
 }
