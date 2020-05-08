 package fr.iutvalence.java.mp.RollingBall;
 
// TODO (fix) detail comment
 /**
 * the Vector is used to set up the physic in the game
  * 
  * @author andrejul
  *
  */
 public class Vector extends Segment
 {
    /**
     * Vector created with the component of the vector
     * @param componentOfTheVector 
     */
     public Vector(Point componentOfTheVector)
     {
        super(new Point(0,0), componentOfTheVector, false);
     }
 
 }
