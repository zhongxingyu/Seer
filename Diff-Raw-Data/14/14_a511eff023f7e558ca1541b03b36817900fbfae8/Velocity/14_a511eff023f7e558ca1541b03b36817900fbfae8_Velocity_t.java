 package kaban.iklan;
 
 public class Velocity {
 
     private int angle;
     private int speed;
 
     private int velX;
     private int velY;
 
     public int getVelX(){
        return velX;
     }
 
     public int getAngle() {
         return angle;
     }
 
     public int getVelY(){
        return velY;
     }
 
     public void setDir(int degrees) {
         this.angle = degrees;
         update();
     }
 
     public void reverse(){
         setDir(360 - angle);
     }
 
     public void setSpeed(int unitsPerUpdate) {
         this.speed = unitsPerUpdate;
         update();
     }
 
     private void update() {
        velX = (int) (speed * Math.sin(Math.toRadians(angle)));
        velY = (int) (speed * Math.cos(Math.toRadians(angle)));
     }
 }
