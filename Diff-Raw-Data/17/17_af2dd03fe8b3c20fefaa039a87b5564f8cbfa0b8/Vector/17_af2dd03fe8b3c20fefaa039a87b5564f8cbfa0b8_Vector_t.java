 package fr.iutvalence.java.mp.RollingBall;
 
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

    // TODO (FIXED) consider overriding toString to return an ASCII representation of the vector
     // it can rely on the toString method overridden in Segment
 
    /**
     * super method to get the parameters of the Vector to String
     * 
     * @return String
     *              parameters of the Vector
     */
    public String toString()
    {
        return "" + super.toString();
    }

 }
