 /**
  * Creature.java - a drawable creature which has velocity and goals
  */
 package edu.bu.cs.cs480.model.creatures;
 
 import java.util.List;
 
 import javax.media.opengl.GL;
 
 import edu.bu.cs.cs480.drawing.Displayable;
 import edu.bu.cs.cs480.model.Point3D;
 import edu.bu.cs.cs480.model.SizedComponent;
 
 /**
  * A creature which has a velocity, can move, has a bounding sphere, exhibits
  * flock behavior, and is attracted to food.
  * 
  * Flocking behavior code adapted from pseudocode at:
  * http://www.vergenet.net/~conrad/boids/pseudocode.html
  * 
  * @author Jeffrey Finkelstein <jeffrey.finkelstein@gmail.com>
  * @since Spring 2011
  */
 public abstract class Creature extends SizedComponent {
   /**
    * The relative importance of the attraction of this creature to the perceived
    * center of its flock.
    */
   public static final double CENTER_WEIGHT = 0.01;
   /**
    * The relative importance of the attraction of this creature to the nearest
    * piece of food.
    */
  public static final double FOOD_WEIGHT = 0.1;
   /** The initial velocity of the creature. */
   public static final Point3D INITIAL_VELOCITY = new Point3D(0.01, 0, 0);
   /**
    * The maximum corner of the rectangle which bounds the possible positions of
    * this creature.
    */
   public static final Point3D MAX_POSITION = new Point3D(2.9, 2.9, 2.9);
   /** The maximum speed of the creature. */
   public static final double MAX_SPEED = 0.04;
   /**
    * The minimum corner of the rectangle which bounds the possible positions of
    * this creature.
    */
   public static final Point3D MIN_POSITION = new Point3D(-2.9, -2.9, -2.9);
   /**
    * The distance at which other creatures exert a repelling force on this
    * creature.
    */
  // public static final double REPULSION_DISTANCE = 0.5;
   /**
    * The relative importance of the attraction of this creature to the perceived
    * average velocity of its flock.
    */
  public static final double VELOCITY_WEIGHT = 0.125;
   /** The flock of which this creature is a part. */
   private final List<Creature> flock;
   /** The food to which this creature is attracted. */
   private final List<Food> food;
 
   /** The current velocity of this creature. */
   private Point3D velocity = INITIAL_VELOCITY;
 
   /**
    * Instantiates this class by stashing all the provided parameters.
    * 
    * @param position
    *          The position of this component.
    * @param displayable
    *          The object which this component represents.
    * @param name
    *          The human-readable name of this component.
    * @param flock
    *          The flock to which this creature belongs.
    * @param food
    *          The food to which this creature is attracted.
    */
   public Creature(Point3D position, Displayable displayable, String name,
       final List<Creature> flock, final List<Food> food) {
     super(position, displayable, name);
     this.flock = flock;
     this.food = food;
   }
 
   /**
    * Checks that the current position of this creature is within the bounds
    * specified by the bounding rectangle given by {@value #MAX_POSITION} and
    * {@value #MIN_POSITION}.
    * 
    * If the position is beyond those bounds, it is reset to be inside the
    * bounding rectangle.
    */
   protected void checkBounds() {
     double x = this.position().x();
     double y = this.position().y();
     double z = this.position().z();
     x = Math.max(Math.min(x, MAX_POSITION.x()), MIN_POSITION.x());
     y = Math.max(Math.min(y, MAX_POSITION.y()), MIN_POSITION.y());
     z = Math.max(Math.min(z, MAX_POSITION.z()), MIN_POSITION.z());
     this.setPosition(new Point3D(x, y, z));
   }
 
   /**
    * Updates the velocity of this creature by adding the attraction and
    * repulsion due to the positions and velocities of the rest of the flock.
    */
   protected void flockVelocityUpdate() {

     // first compute the velocity towards the center of the flock
     final Point3D velocityTowardsCenter = this.velocityTowardsCenter();
 
     // second compute the repulsion from other creatures
     final Point3D repulsionVelocity = this.repulsionVelocity();
 
     // third compute the perceived velocity of the flock
     final Point3D perceivedFlockVelocity = this.perceivedFlockVelocity();
 
     // add the computed flock velocity offsets to the current velocity
    Point3D newVelocity = this.velocity.sumWith(velocityTowardsCenter);
    newVelocity = this.velocity.sumWith(repulsionVelocity);
    newVelocity = this.velocity.sumWith(perceivedFlockVelocity);
 
     // update the new velocity of the creature
     this.setVelocity(newVelocity);
 
   }
 
   /**
    * Updates the velocity of this creature by adding the attraction to the
    * nearest piece of food in {@link #food}.
    */
   protected void foodVelocityUpdate() {
     if (this.food != null && !this.food.isEmpty()) {
       Food nearestFood = null;
       double nearestFoodDistance = Double.MAX_VALUE;
 
       for (final Food food : this.food) {
         double distance = this.position().distanceTo(food.position());
         if (distance < nearestFoodDistance) {
           nearestFood = food;
           nearestFoodDistance = distance;
         }
       }
 
       final Point3D result = nearestFood.position().difference(this.position())
           .scaledBy(FOOD_WEIGHT);
 
       this.setVelocity(this.velocity.sumWith(result));
     }
   }
 
   /**
    * Checks that the velocity is not beyond the maximum velocity for a creature,
    * and scales it back if it is.
    */
   protected void limitVelocity() {
     if (this.velocity.norm() > MAX_SPEED) {
       this.velocity = this.velocity.normalized().scaledBy(MAX_SPEED);
     }
   }
 
   /** Updates the position of this component using the velocity. */
   protected void move() {
     this.setPosition(this.position().sumWith(this.velocity));
     this.checkBounds();
   }
 
   /**
    * Returns the force due to perceived average velocity of the other creatures
    * in the flock.
    * 
    * Pre-condition: {@link #flock} is not null, and {@code #flock} contains more
    * than just this creature.
    * 
    * @return The force due to the perceived average velocity of the other
    *         creatures in the flock.
    */
   protected Point3D perceivedFlockVelocity() {
     double xSum = 0;
     double ySum = 0;
     double zSum = 0;
 
     for (final Creature creature : this.flock) {
       if (!creature.equals(this)) {
         xSum += creature.velocity.x();
         ySum += creature.velocity.y();
         zSum += creature.velocity.z();
       }
     }
 
     final int numCreatures = this.flock.size() - 1;
     final Point3D perceivedVelocity = new Point3D(xSum / numCreatures, ySum
         / numCreatures, zSum / numCreatures);
     return perceivedVelocity.difference(this.velocity)
         .scaledBy(VELOCITY_WEIGHT);
 
   }
 
   /**
    * Returns the repulsion of this creature due to other creatures in the flock
    * at too close a distance.
    * 
    * @return The repulsion of this creature due to other creatures in the flock
    *         at too close a distance.
    */
   protected Point3D repulsionVelocity() {
     Point3D result = Point3D.ORIGIN;
 
     for (final Creature creature : this.flock) {
       if (!creature.equals(this)) {
        if (this.position().distanceTo(creature.position()) < this
            .boundingRadius() * 2) {
           result = result.difference(creature.position().difference(
               this.position()));
         }
       }
     }
 
     return result;
   }
 
   /**
    * Sets the velocity of this creature and rotates it so that it is facing the
    * direction of its velocity vector.
    * 
    * @param velocity
    *          The new velocity vector for this creature.
    */
   public void setVelocity(final Point3D newVelocity) {
     Point3D axisOfRotation = this.velocity;
     double angleOfRotation = 0;
     // if they are parallel and in opposite directions, rotate by 180
     if (this.velocity.parallelTo(newVelocity)
         && this.velocity.oppositeDirectionFrom(newVelocity)) {
       axisOfRotation = this.velocity.orthogonal();
       angleOfRotation = 180;
     }
     // otherwise compute the angle and axis if they are not parallel
     else if (!this.velocity.parallelTo(newVelocity)) {
       axisOfRotation = this.velocity.crossProduct(newVelocity);
       angleOfRotation = this.velocity.angleBetween(newVelocity);
     }
     this.velocity = newVelocity;
     this.rotate(axisOfRotation, angleOfRotation);
     this.limitVelocity();
   }
 
   /**
    * Updates the velocity of this creature due to flocking behavior and food
    * proximity, moves this creature based on the updated velocity, and updates
    * the OpenGL call list which draws the model of this creature.
    * 
    * @param gl
    *          {@inheritDoc}
    */
   @Override
   public void update(final GL gl) {
     // update the velocity based on the known flock
     this.flockVelocityUpdate();
 
     // update the velocity based on the known food locations
     this.foodVelocityUpdate();
 
     // move the creature
     this.move();
 
     // update the OpenGL call list
     super.update(gl);
   }
 
   /**
    * Returns the velocity due to attraction of this creature towards the
    * perceived center of the other creatures in the flock.
    * 
    * Pre-condition: flock is not null, and flock contains more than just this
    * creature.
    * 
    * @return The velocity due to attraction of this creature towards the
    *         perceived center of the other creatures in the flock.
    */
   protected Point3D velocityTowardsCenter() {
     double xSum = 0;
     double ySum = 0;
     double zSum = 0;
 
     for (final Creature creature : this.flock) {
       if (!creature.equals(this)) {
         xSum += creature.position().x();
         ySum += creature.position().y();
         zSum += creature.position().z();
       }
     }
 
     final int numCreatures = this.flock.size() - 1;
     final Point3D center = new Point3D(xSum / numCreatures,
         ySum / numCreatures, zSum / numCreatures);
 
     return center.difference(this.position()).scaledBy(CENTER_WEIGHT);
   }
 
   /**
    * Returns the velocity of this creature.
    * 
    * Used only for testing purposes.
    * 
    * @return The velocity of this creature.
    */
   protected Point3D velocity() {
     return this.velocity;
   }
 }
