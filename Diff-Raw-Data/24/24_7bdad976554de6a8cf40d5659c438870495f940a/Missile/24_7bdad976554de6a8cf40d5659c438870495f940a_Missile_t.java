 package actor;
 
 import math.Quaternion;
 import math.Vector3;
 
 public class Missile extends Actor {
    private static final long serialVersionUID = -8381240274687476481L;
     private static final float MISSILE_SPEED = 0.5f;
 
     @Override
     public void handleCollision(Actor other) {
         // TODO Auto-generated method stub
     }
     
     public Missile(Actor actor){
         super();
         angularVelocity = new Quaternion(Vector3.UNIT_Y, 1);
         this.velocity = actor.velocity.plus(actor.getDirection().times(MISSILE_SPEED));
         position = new math.Vector3(actor.getPosition());
         setSize(.1f);
     }
 }
