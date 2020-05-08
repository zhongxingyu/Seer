 package cs2114.speedrider1;
 
 import android.graphics.PointF;
 import sofia.graphics.LineShape;
 import sofia.util.Timer;
 import sofia.util.Random;
 import sofia.graphics.ShapeMotion;
 import sofia.graphics.Color;
 import sofia.graphics.OvalShape;
 import sofia.graphics.RectangleShape;
 
 // -------------------------------------------------------------------------
 /**
  *  shoots out objects in a certain direction
  *
  *  @author dmoore09
  *  @version Apr 29, 2013
  */
 public class Shooter
     extends RectangleShape
 {
 
     // -------------------------------------------------------------------------
     /**
      *  Objects that the shooter creates
      *
      *  @author Daniel
      *  @version Apr 29, 2013
      */
     public class Bullet extends OvalShape
     {
 
         // ----------------------------------------------------------
         /**
          * Create a new Bullet object.
          * @param x coordinate of center
          * @param y coordinate of center
          */
         public Bullet(float x, float y)
         {
             super(x, y, 8);
             this.setShapeMotion(ShapeMotion.DYNAMIC);
             this.setDensity(1);
             this.setRestitution(1f);
             this.setImage("spikeballg");
         }
 
 
         /**
          * bullets are removed when they hit an object
          * @param shape any object
          */
         public void onCollisionWith(LineShape shape)
         {
             remove();
         }
 
         /**
          * bullets are removed when they hit an object
          * @param player object on screen
          */
         public void onCollisionWith(Rider player)
         {
             PointF velocity = this.getLinearVelocity();
             player.applyLinearImpulse(velocity.x * 10000, velocity.y * 10000);
             remove();
         }
     }
 
     // ----------------------------------------------------------
     //field for direction
     private boolean rightOrLeft;
 
     /**
      * Create a new Shooter object.
      * @param left corner of rectangle
      * @param top corner of rectangle
      * @param right corner of rectangle
      * @param bottom corner of rectangle
      * @param direction direction it will shoot, true is left, false is right
      */
     public Shooter(float left, float top, float right, float bottom,
         boolean direction)
     {
         super(left, top, right, bottom);
         rightOrLeft = direction;
         Timer.callRepeatedly(this, "shoot", 0, 3000);
     }
 
     /**
      * creates a bullet object at random velocity
      */
     public void shoot()
     {
         //create bullet
         Bullet bullet = new Bullet(getX(), getY());
         this.getShapeField().add(bullet);
 
         //random generator for impulses
         float xImpulse = Random.generator().nextFloat(1000, 100000);
         float yImpulse = Random.generator().nextFloat(1000, 100000);
 
         //if rigthOrLeft is true shoot to the left
         if (rightOrLeft)
         {
             bullet.applyLinearImpulse(-xImpulse, yImpulse);
             setImage("cannon");
         }
         //shoot to the right
         else
         {
             bullet.applyLinearImpulse(xImpulse, yImpulse);
             setImage("cannonl");
         }
 
     }
 
 
 }
