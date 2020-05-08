 package fr.iutvalence.java.mp.RollingBall;
 
// TODO (fix) detail comment
 /**
  * the Vector extends the Segment class and it is used to set up the physic
 * rules in the game
  * 
  * @author andrejul
  * 
  */
 public class Vector extends Segment
 {
     /**
      * Vector created with the component of the vector
      * 
      * @param componentOfTheVector
      *            second point of the vector which will set up the power of the
      *            vector
      */
     public Vector(Point componentOfTheVector)
     {
         super(new Point(0, 0), componentOfTheVector);
     }
 
    // TODO (fix) finish writing comment
     /**
     * Returns an ASCII representation of the vector as ...
     * @see java.lang.Object#toString()
      */
     public String toString()
     {
         return "" + super.toString();
     }
 
 }
