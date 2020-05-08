 package actor;
 
 public class Player extends Actor {
     private static final String DEFAULT_NAME = new String("Pilot");
     private static final long serialVersionUID = 260627862699350716L;
     protected boolean alive;
     protected graphics.Camera camera;
     protected String name;
 
     
     public Player(){
         super();
         alive = true;
         camera = new graphics.Camera(this);
         name = DEFAULT_NAME;
     }
     @Override
     public void handleCollision(Actor other) {
         // TODO Auto-generated method stub
         
     }
 
     public boolean isAlive() {
         return alive;
     }
 
     public void respawn() {
         // Don't respawn the player if they are alive
         if(alive == true)
             return;
         
         // TODO Auto-generated method stub
         
     }
 
     public void shoot() {
         // TODO Auto-generated method stub
         
     }
 
     public void forwardThrust() {
         // TODO Auto-generated method stub
        position.z += 0.01f;
         
     }
 
     public void reverseThrust() {
         // TODO Auto-generated method stub
        position.z -= 0.01f;
         
     }
 
     public void turnLeft() {
         // TODO Auto-generated method stub
         
     }
 
     public void turnRight() {
         // TODO Auto-generated method stub
         
     }
     public graphics.Camera getCamera() {
         return camera;
     }
 
 }
