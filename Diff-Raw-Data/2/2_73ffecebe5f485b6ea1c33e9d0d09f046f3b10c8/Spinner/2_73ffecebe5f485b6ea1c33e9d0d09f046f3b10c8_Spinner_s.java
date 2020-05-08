 package cs2114.speedrider1;
 
 import android.graphics.PointF;
 import sofia.graphics.ShapeMotion;
 import sofia.graphics.Color;
 import sofia.graphics.RectangleShape;
 
 /**
  * // -------------------------------------------------------------------------
  * /** obstacle that spins around perpetually
  *
  * @author Daniel
  * @version Apr 29, 2013
  */
 public class Spinner
     extends RectangleShape
 {
     /**
      * Create a new spinner object.
      *
      * @param left
      *            corner of rectangle
      * @param top
      *            corner of rectangle
      * @param right
      *            corner of rectangle
      * @param bottom
      *            corner of rectangle
      */
     public Spinner(float left, float top, float right, float bottom)
     {
         super(left, top, right, bottom);
        setFillColor(Color.green);
        setFilled(true);
         this.setGravityScale(0);
         this.setDensity(10);
         setShapeMotion(ShapeMotion.DYNAMIC);
         this.setImage("spinner");
         this.animate(500).repeat().rotation(360).play();
 
     }
 
 
     // ----------------------------------------------------------
     /**
      * when a rider collides it will push him back or forward depending on
      * position
      *
      * @param rider
      *            in the game
      */
     public void onCollisionWith(Rider rider)
     {
 
         if (getX() >= rider.getX())
         {
             PointF velocity = rider.getLinearVelocity();
             rider.applyLinearImpulse(-(velocity.x + 10000), velocity.y + 10000);
         }
         else
         {
             PointF velocity = rider.getLinearVelocity();
             rider.applyLinearImpulse(velocity.x + 10000, -(velocity.y + 10000));
         }
     }
 
 }
