 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package Objected2D.objects.abstractClasses;
 import Objected2D.*;
import Objected2D.objects.*;
import Objected2D.Objected2DUtils;
import Objected2D.Objected2DWindow;
 import Objected2D.objects.interfaces.Objected2DCollisionObject;
 import Objected2D.util.CollisionPointDetector;
import java.awt.Color;
 
 /**
  *
  * @author Till Ehrengruber <darthmatch@googlemail.com>
  * @copyright  2011 Till Ehrengruber <darthmatch@googlemail.com>
  */
 public abstract class Objected2DCollisionObject_abstract
         extends Objected2DObject_Abstract implements Objected2DCollisionObject {
 
     //possible collision points
     protected boolean[][] collisionPoints;
 
     protected boolean collisionPointsSet = false;
 
     private boolean collision;
     
     /**
      * create possible collision points
      * @return collision points
      */
     public boolean[][] createCollisionPoints () {
         this.collisionPoints = new CollisionPointDetector(this)
                 .getCollisionPoints();
         
         //Objected2DUtils.print2DArrayMatrix(this.collisionPoints);
         //System.exit(0);
         
         this.collisionPointsSet = true;
         return this.collisionPoints;
     }
 
     public boolean[][] getCollisionPoints () {
         if (this.collisionPointsSet)
             return this.collisionPoints;
         else
             return this.createCollisionPoints();
     }
 
     /**
      * trigger that this object is colliding
      */
     public void triggerCollision () {
         this.collision = true;
     }
 
     /**
      * trigger that this object is colliding or not
      * (true => colliding | false => not colliding)
      */
     public void triggerCollision (boolean value) {
         this.collision = value;
     }
 
     /**
      * return if object is colliding
      * @return (true => colliding | false => not colliding)
      */
     public boolean isColliding () {
         return this.collision;
     }
 
     public boolean isCollidingWithOuterBox (Objected2DCollisionObject object) {
         return !((this.getYPos() >= object.getYPos(object.getHeight()))
                 || (this.getYPos(this.getHeight()) <= object.getYPos())
                 || (this.getXPos() >= object.getXPos(object.getWidth()))
                 || (this.getXPos(this.getWidth()) <= object.getXPos())
                 );
     }
 
     public boolean isCollidingWith (Objected2DCollisionObject object) {
         /*
          * Check if the Outer Box of the Object Collides
          */
         if (!this.isCollidingWithOuterBox(object))
             return false;
 
         /*
          * Check the array
          */
         this.getCollisionPoints();
         boolean[][] objectCollisionPoints = object.getCollisionPoints();
 
         /*
          * Debug
          */
         //Fenster erstellen
         //Objected2DWindow fenster = new Objected2DWindow(600, 600);
 
         //Zeichenfläche auslesen
         //Objected2D zeichenflaeche = fenster.getObjected2D();
 
         //Objected2DUtils.print2DArrayMatrix(objectCollisionPoints);
         //Objected2DUtils.print2DArrayMatrix(this.getCollisionPoints());
 
 
 
 
        int heightMax = this.collisionPoints[0].length/*+this.getYPos()*/;
        int widthMax = this.collisionPoints.length/*+this.getXPos()*/;
         int xOffset = object.getXPos() - this.getXPos();
         int yOffset = object.getYPos() - this.getYPos();
         boolean collision = false;
 
        Objected2DUtils.print2DArrayMatrixCombined(this.getCollisionPoints(), objectCollisionPoints, xOffset, yOffset);
 
         //this.debug("x:"+xOffset);
         //this.debug("y:"+yOffset);
         
         for (int y = 0; y < heightMax; y++)
         {
             for (int x = 0; x < widthMax; x++) {
                 /*if (this.collisionPoints[y][x]) {
                     Point point = new Point(x, y);
 
                     try {
                         if (objectCollisionPoints[x-xOffset][y-yOffset]) {
                             point.setColor(Color.red);
                         } else {
                             point.setColor(Color.green);
                         }
 
                     } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                         point.setColor(Color.green);
                     }
 
                     zeichenflaeche.addObject(point);
                 }*/
 
                 
 
                 if (this.collisionPoints[y][x]) {
                     if (x-xOffset >= 0 
                             && y-yOffset >= 0
                             && x-xOffset < objectCollisionPoints.length
                             && y-yOffset < objectCollisionPoints[0].length ) {
                         if (objectCollisionPoints[x-xOffset][y-yOffset]) {
                             collision=true;
                             }
                          else {
                             //this.debug("x:"+(x-xOffset)+" y: "+(y-yOffset));
                          }
                         //this.debug(objectCollisionPoints[x-xOffset][y-yOffset]);
                     }
                 }
             }
         }
 
         //fenster.repaint();
         
         //this.debug("Collision: "+collision);
 
         //if (!collision)
         //    this.debug("x:"+(xOffset)+" y: "+(yOffset));
 
         return collision;
     }
 
     public boolean setParent (Objected2D object) {
         return super.setParent(object);
     }
 }
