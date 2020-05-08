 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Unit;
 
 import Path.Path;
 import Weapon.Weapon;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Point;
 
 /**
  *
  * @author michal
  */
 public class Enemy extends Unit {
 
     Path movePath;
 
     public Enemy(int health, int speed,  Weapon weaponType, Path movePath, int pointValue, int xCoord, int yCoord, Color color) {
         super(health, speed, weaponType, pointValue, xCoord, yCoord, color);
        setTextureFromFile(".//src//Unit//enemy.png");
         this.movePath = movePath;
     }
 
     public void draw(Graphics2D g2) {
        // Graphics2D g_ = (Graphics2D) g;
      /*   g2.setColor(color);
         Polygon p = new Polygon();
         Path2D triangle = new Path2D.Double();
         triangle.moveTo(xCoord, yCoord + 20);
         triangle.lineTo(xCoord - 10, yCoord - 10);
         triangle.lineTo(xCoord + 10, yCoord - 10);
         Area a = new Area(triangle); */
      //   g2.fill(a);
 
       //  g2.draw(new Rectangle2D.Double(xCoord + 2, yCoord + 2, 27, 27));
         g2.drawImage(getTexture(),xCoord,yCoord,null);
     }
 
     /**
      * Moves the enemy to its next location along its path.
      */
     public void doMove(){
         Point nextLoc = movePath.getNextLocation(super.getX(), super.getY());
         super.setLocation(nextLoc);
     }
 
 }
