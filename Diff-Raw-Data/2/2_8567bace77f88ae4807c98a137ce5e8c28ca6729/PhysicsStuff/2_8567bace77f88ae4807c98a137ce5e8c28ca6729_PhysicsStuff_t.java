 package thrust.physics.src;
 //class written by Daire O'Doherty 06535691 3/4/08
 /**
 * @author "Daire O'Doherty 06535691 (daireod@gmail.com)"
  * @version 16 Apr 2008
  */
 public class PhysicsStuff implements Physics {
 /** current speed.*/
   private final transient double my_current_speed;
 /** mass.*/
   private final transient double my_mass;
 /** orientation.*/
   private final transient double my_orien;
 /**x coordinate.*/
   private final transient double my_xCoord;
 /** y coordinate.*/
   private final transient double my_yCoord;
   //@ constraint (* The gravitational constant never changes. *);
   //@ constraint gravitational_constant() == \old(gravitational_constant());
   /**
   * @return What is your acceleration in meters per second squared?
   */
   //@ ensures \result.length == 2;
   public/*@ pure @*/ double[] acceleration() {
   //acceleration = difference in V/difference in time, metre/second squared
     final double[] accel = {my_current_speed, my_orien};
     return accel;
   }
 
   /**
    * @return What is the gravitational constant?
    */
   /*@ pure @*/public double gravitationalConstant() {
   /**earth gravity is 9.81 metres per second squared*/
     final double vGravity = 9.81;
     return vGravity;
   }
 
 
   /**
    * @return What is your mass in kilograms?
    */
   //@ ensures 0 <= \result;
   public/*@ pure @*/ double mass() {
     return my_mass;
   }
   /**
    * @return What is your momentum in kilograms*meters per second?
    */
   public/*@ pure @*/ double momentum() {
 
     return my_mass * my_current_speed;
   }
 
   /**
    * @return What is your orientation in radians?
    */
   public/*@ pure @*/ double orientation() {
     return my_orien;
   }
 
   /**
    * @return What is your position in meters from the origin?
    */
  //@ ensures \result.length == 2;
   public/*@ pure @*/ double[] position() {
   //x+y coordinate
     final int arrayLength = 2;
     final double[] xyPos = new double[arrayLength];
     xyPos[0] = my_xCoord;
     xyPos[1] = my_yCoord;
     return xyPos;
   }
 
   /**
    * @return What is your velocity in meters per second?
    */
   public/*@ pure @*/ double[] velocity () {
   //change in position versus time
   //position 2 - position1%time
     final double []velo = {my_current_speed, my_orien};
     return velo;
   }
 
 
 }
