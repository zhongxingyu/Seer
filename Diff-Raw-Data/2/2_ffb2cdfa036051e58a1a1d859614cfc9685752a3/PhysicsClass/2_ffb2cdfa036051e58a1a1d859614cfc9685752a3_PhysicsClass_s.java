 /**
  * Computing the behaviour of entities according to physical
  * simulation in two dimensions.
  * @author Patrick Nevin: 06754155:
  *         Robert Plunkett: 06038883:
  * @version 8 April 2008
  * @revised 20 April 2008
  */
 package thrust.physics;
 /**
  * @author Patrick Nevin (patrick.nevin@ucdconnect.ie)
  * @version 20 April 2008
 * @revised 25 April 2008
  */
 public class  PhysicsClass implements PhysicsInterface {
 
   /**constant force,
    * On a screen top left corner has position (0,0), hence g reversed.
    **/
   private static final double GRAVITATIONAL_CONSTANT = 9.8;
   /**The size of the array's.*/
   private static final int ARRAYSIZE = 2;
   /**the entities acceleration.*/
   private double[] my_acceleration = new double[ARRAYSIZE];
   /**the entities position.*/
   private double[] my_position = new double[ARRAYSIZE];
   /**the entities velocity.*/
   private double[] my_velocity = new double[ARRAYSIZE];
   /**the mass of an entity in kg's.*/
   private double my_mass;
   /**the angle of the direction of an entity giving in rad's.*/
   private double my_orientation;
     /**the speed of an entity in m/s.*/
   private double my_speed;
   /**The time in seconds that the entity should simulate.*/
   private double my_simulation;
   /**
    * Create an instance of Physics an set the following attributes.
    * @param mass, set the Entities mass
    * @param x initial location of this entity
    * @param y location of this entity
    */
   public PhysicsClass(final double a_mass) {
     this.my_mass = a_mass;
   }
   /**
    * @return What is your acceleration in meters per second squared?
    */
   //@ ensures \result.length == 2;
   /*@ pure @*/
   public double[] acceleration() {
     return this.my_acceleration;
   }
   /**
    * @param the_acceleration This is your acceleration.
    */
   //@ requires the_acceleration.length == 2;
   //@ ensures acceleration()[0] == the_acceleration[0];
   //@ ensures acceleration()[1] == the_acceleration[1];
   public void acceleration(final double[] the_acceleration) {
     this.my_acceleration[0] = the_acceleration[0];
     this.my_acceleration[1] = the_acceleration[1];
   }
   /**
    * @return What is the gravitational constant?
    */
   public /*@ pure @*/ double gravitational_constant() {
     return GRAVITATIONAL_CONSTANT;
   }
   /**
    * @param the_mass This is your mass.
    */
   //@ requires 0 <= the_mass;
   //@ ensures mass() == the_mass;
   public void mass(final double the_mass) {
     this.my_mass = the_mass;
   }
   /**
    * @return What is your mass in kilograms?
    */
   //@ ensures 0 <= \result;
   public /*@ pure @*/ double mass() {
     return this.my_mass;
   }
   /**
    * @return What is your momentum in kg's*meters per second?
    */
   public /*@ pure @*/ double momentum() {
     return (this.my_speed * this.my_mass);
   }
   /**
    * @return What is your orientation in rad's?
    */
   public /*@ pure @*/ double orientation() {
     return this.my_orientation;
   }
   /**
    * @param the_orientation This is your orientation.
    */
   //@ ensures orientation() == the_orientation;
   public void orientation(final double the_orientation) {
     this.my_orientation = Math.toRadians(the_orientation);
   }
   /**
    * @return What is your position in meters from the origin?
    */
   //@ ensures \result.length == 2;
   public /*@ pure @*/ double[] position() {
     return this.my_position;
   }
   /**
    * @param the_position This is your position.
    */
   //@ requires the_position.length == 2;
   //@ ensures position()[0] == the_position[0];
   //@ ensures position()[1] == the_position[1];
   public void position(final double[] the_position) {
     this.my_position[0] = the_position[0];
     this.my_position[1] = the_position[1];
   }
 
   /**
    * @return What is your velocity in meters per second?
    */
   public /*@ pure @*/ double[] velocity() {
     return my_velocity;
   }
   /**
    * @param the_velocity This is your velocity.
    */
   //@ requires the_velocity.length == 2;
   //@ ensures velocity()[0] == the_velocity[0];
   //@ ensures velocity()[1] == the_velocity[1];
   public void velocity(final double[] the_velocity) {
     this.my_velocity[0] = the_velocity[0];
     this.my_velocity[1] = the_velocity[1];
   }
   /**
    * Simulate yourself for this many seconds.
    * @param some_seconds the number of seconds to simulate.
    */
   public void simulate(final double some_seconds) {
     this.my_simulation = some_seconds;
   }
 }
