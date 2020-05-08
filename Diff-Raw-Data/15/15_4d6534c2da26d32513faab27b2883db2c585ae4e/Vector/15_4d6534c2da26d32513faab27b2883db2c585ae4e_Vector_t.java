 package fr.iutvalence.java.mp.RollingBall;
 
// TODO (FIXED) detail comment
 /**
  * the Vector extends the Segment class and it is used to set up the physic
 * rules in the game. 
 * The Vector is the thing we will use to move an object on our map.
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
 
    // TODO (FIXED) finish writing comment
     /**
     * Returns an ASCII representation of the vector as the ASCII representation of the segment
     * @return String
     *              the ASCII representation of the vector
      */
     public String toString()
     {
         return "" + super.toString();
     }
 
 }
