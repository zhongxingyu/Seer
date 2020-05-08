 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package projetx;
 
 import java.awt.geom.Point2D;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 /**
  *
  * @author jonas
  */
 public class Player extends Sprite {
 
     protected Point2D speed = new Point2D.Double(0,0);
     protected Point2D AcutalPosition;
     protected boolean isOnAPlatform;
     protected boolean wantsToGoRight = false;
     protected boolean wantsToGoLeft = false;
     protected boolean wantsToJump = false;
    protected double lateralSpeed = 7.5;
     protected double verticalSpeed = 1;
     int numberOfKills = 0;
     int numberOfDeaths = 0;
     int lastPower=0;
     boolean rightOrientation = true;
 
     public Player(String img) throws SlickException {
         super(img);
         this.image = new Image(img);
     }
 
     public void iWouldLikeToJump() {
         if (isOnAPlatform) {
             wantsToJump = true;
         }
     }
 
     public void iWouldLikeToGoLeft() {
         wantsToGoLeft = true;
         if (rightOrientation) {
             this.image = this.image.getFlippedCopy(true, false);
             rightOrientation = false;
         }
     }
 
     public void iWouldLikeToGoRight() {
         wantsToGoRight = true;
         if (!rightOrientation) {
             this.image = this.image.getFlippedCopy(true, false);
             rightOrientation = true;
         }
     }
 
     public void Die() {
         numberOfDeaths++;
         lastPower=0;
     }
 
     public void Kill() {
         numberOfKills++;
 
     }
 
     public void setWantsToGoLeft(boolean wantsToGoLeft) {
         this.wantsToGoLeft = wantsToGoLeft;
     }
 
     public void setWantsToGoRight(boolean wantsToGoRight) {
         this.wantsToGoRight = wantsToGoRight;
     }
 
     public void setWantsToJump(boolean wantsToJump) {
         this.wantsToJump = wantsToJump;
     }
 
     public double getVerticalSpeed() {
         return verticalSpeed;
     }
 
     public void setVerticalSpeed(double verticalSpeed) {
         this.verticalSpeed = verticalSpeed;
     }
 
     public Point2D getSpeed() {
         return speed;
     }
 
     public void setSpeed(Point2D speed) {
         this.speed = speed;
     }
 
     public void setSpeed(double x, double y) {
         this.speed.setLocation(x, y);
     }
 
     public Point2D getActualPosition() {
         return this.AcutalPosition;
     }
 
     public void setActualPosition(Point2D pos) {
         this.AcutalPosition = pos;
     }
 
     public boolean isOnAPlatform() {
         return isOnAPlatform;
     }
 
     public boolean isWantsToGoLeft() {
         return wantsToGoLeft;
     }
 
     public boolean isWantsToGoRight() {
         return wantsToGoRight;
     }
 
     public boolean isWantsToJump() {
         return wantsToJump;
     }
 
     public void setIsOnAPlatform(boolean isOnAPlatform) {
         this.isOnAPlatform = isOnAPlatform;
     }
 
     public double getLateralSpeed() {
         return lateralSpeed;
     }
 
     public void setLateralSpeed(double lateralSpeed) {
         this.lateralSpeed = lateralSpeed;
     }
     
     public int GetTimeSinceLastPower() {
         return lastPower;
     }
     
     public void SetTimeSinceLastPower(int time) {
         lastPower=time;
     }
     
     public float GetTailleBarre() {
         if(lastPower>5000) {
             return 80.f;
         }
         else {
             return (float)((80.f*(float)lastPower)/5000.f);
         }
     }
 }
