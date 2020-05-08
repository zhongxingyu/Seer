 package thrust.physics;
 
 /**
  * Abstract class implemented by the Physics interface.
  * @author David Haughton (Dave.haughton1@gmail.com)
  * @version 7 April 2008
  */
 public abstract class PhysicsClass implements PhysicsInterface {
 
   /**
    * The angle of an object, in radians.
    */
   double my_angleRadians;
 
   /**
    * The mass of an object.
    */
   double my_mass;
 
   /**
    * The position of an object.
    */
   double[] my_xyPosition;
 
   /**
    * The speed of an object.
    */
   double my_speed;
 
   /**
    * some seconds.
    */
   double my_seconds;
 
   /**
    * The acceleration of an object.
    */
   public abstract double[] acceleration();
 
   /**
    * @return the downward acceleration due to gravity.
    */
   public double gravitational_constant()
   {
     final double gravity = -9.81;
     return gravity;
   }
 
   /**
    * @param takes the mass of an object.
    */
   public void setMass(final double some_mass)
   {
     my_mass = some_mass;
   }
 
   /**
    * @return the mass of an object.
    */
   public double mass()
   {
     return my_mass;
   }
 
   /**
    * @return the momentum of an object
    */
   public double momentum()
   {
     final int numberOfElements = 2;
     double[] speed = new double[numberOfElements];
     speed = velocity();
     return mass() * speed[0];
   }
 
   /**
    * @param takes the angle of an object.
    */
   public void getOrientation(final double an_angle)
   {
     my_angleRadians = an_angle;
   }
 
   /**
    * @return the angle of an object.
    */
   public double orientation()
   {
     return my_angleRadians;
   }
 
   /**
    * @param takes an x coordinate and a y coordinate.
    */
   public void  getPosition(final double a_position_x, final double a_position_y)
   {
     final int xyCoordinate = 2;
     my_xyPosition = new double[xyCoordinate];
     my_xyPosition[0] = a_position_x;
     my_xyPosition[1] = a_position_y;
   }
 
   /**
    * @return the x and y coordinates of an object.
    */
   public double[] position()
   {
     return my_xyPosition;
   }
 
   /**
    * @param takes a speed.
    */
   public void setSpeed(final double a_speed)
   {
     my_speed = a_speed;
   }
 
   /**
    * @return the velocity of an object (i.e. the speed and direction).
    */
   public double[] velocity()
   {
     final int numberOfElements = 2;
     final double[] my_velocity = new double[numberOfElements];
     my_velocity[0] = my_speed;
     my_velocity[1] = orientation();
     return my_velocity;
   }
 
   /**
    * @param some_seconds the number of seconds to simulate.
    */
   public void simulate(final double some_seconds)
   {
     this.my_seconds = some_seconds;
   }
 }
