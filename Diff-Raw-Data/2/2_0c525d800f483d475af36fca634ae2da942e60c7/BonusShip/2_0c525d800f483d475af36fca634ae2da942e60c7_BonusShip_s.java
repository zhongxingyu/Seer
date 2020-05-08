 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package spaceinvaders3d;
 import spaceinvaders3d.Damageable;
 import spaceinvaders3d.Point3D;
 import spaceinvaders3d.Bullet;
 import spaceinvaders3d.Utility;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 
 /**
  *
  * @author Stephen
  */
 public class BonusShip implements Damageable{
     //when you create a bonus ship, it takes in its size and location
     public BonusShip(Point3D uc, Point3D lc){
         upperCorner = uc;
         lowerCorner = lc;
     }
     
     //moves this ship
     public void move(){
         if(movingRight){
             //moves right
             upperCorner.x+=10;
             lowerCorner.x+=10;
         }
         else{
             //moves left
             upperCorner.x-=10;
             lowerCorner.x-=10;
         }
         if(goneTime<=0){
             //brings the ship back to reasonable depth
             upperCorner.z=250;
             lowerCorner.z=250;
         }
     }
     //switches the direction the ship is moving
     public void changeDirection(){
         movingRight = !movingRight;
     }
     @Override
     public void takeDamage(int n) {
         //doesn't lose health because it is a bonus and can't die
 
     }
 
     //allows hp to be seen in other classes
     @Override
     public int getHP() {
         return hp;
     }
     
     //allows upperCorner to be seen in other classes
     @Override
     public Point3D getUpperCorner() {
         return upperCorner;
     }
     
     //allows lowerCorner to be seen in other classes
     @Override
     public Point3D getLowerCorner() {
         return lowerCorner;
     }
 
     //paints this ship
     @Override
     public void paintSelf(Graphics2D g) {
         //draws a red rectangle at its position
         Point uc = upperCorner.convertTo2D();
         Point lc = lowerCorner.convertTo2D();
         g.setColor(Color.RED);
         g.fillRect(uc.x, uc.y, lc.x-uc.x, lc.y-uc.y);
     }
     //what the ship will do every timer tick
     @Override
     public void cycle(int cycleNumber) {
         move();
        if(upperCorner.x>=100||lowerCorner.x<=100){
             changeDirection();
         }
         goneTime--;
     }
     //tells the ship what to do if it gets hit
     @Override
     public void onCollision(Damageable d) {
         Main.player.resetHP();
         upperCorner.z=9000;
         lowerCorner.z=9000;
         goneTime = 2500;
     }
     //top right and bottom left corners
     private Point3D upperCorner, lowerCorner;
     //amount of health this object has
     private int hp = 1;
     //timer to bring the ship back to original depth
     private int goneTime = 0;
     //which direction the ship is moving
     private boolean movingRight = true;
 }
