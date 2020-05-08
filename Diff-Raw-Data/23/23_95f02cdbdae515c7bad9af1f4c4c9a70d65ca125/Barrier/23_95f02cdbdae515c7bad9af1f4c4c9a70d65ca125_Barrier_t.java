 /*
  * A re-implementation of the classic C=64 game 'Thrust'.
  *
  * @author "Joe Kiniry (kiniry@acm.org)"
  * @module "COMP 20050, COMP 30050"
  * @creation_date "March 2007"
  * @last_updated_date "April 2008"
  * @keywords "C=64", "Thrust", "game"
  */
 
 package thrust.entities.in_game;
 
 import java.util.logging.Logger;
 
 import thrust.animation.Animatable;
 import thrust.animation.Animation;
 import thrust.entities.NeutralEntity;
 import thrust.entities.StaticEntity;
 
 /**
  * A barrier and trigger to block the spaceship's way.
  * @author Colin Casey (colin.casey@org.com)
  * @version 27 April 2008
  */
 public class Barrier extends StaticEntity
   implements NeutralEntity, Animatable {
 
   /*@ public invariant (* Barriers are always in one of the three states
   @                     of open, closed, or moving. *);
   @*/
 //@ public invariant closed() ==> !opened() & !moving();
 //@ public invariant opened() ==> !closed() & !moving();
 //@ public invariant moving() ==> !closed() & !opened();
 
   /** Logger for animation. */
   private static final Logger THE_LOGGER =
    Logger.getLogger(Barrier.class.getName());
   /** Describes whether the barrier is open. */
   private transient boolean my_open_indicator = true;
   /** Describes whether the barrier is closed. */
   private transient boolean my_closed_indicator;
   /** Describes whether the barrier is moving. */
   private transient boolean my_moving_indicator;
   /** The frames in the barrier animation. */
   private transient Animation my_animation;
   /** Animation frame counter. */
   private transient int my_animation_counter;
 
   /** Barrier Constructor. */
   public Barrier() {
 
   }
 
   /**
    * @return Are you closed?
    */
   public /*@ pure @*/ boolean closed() {
     return my_closed_indicator;
   }
 
   /**
    * @return Are you open?
    */
   public /*@ pure @*/ boolean opened() {
     return my_open_indicator;
   }
 
   /**
    * @return Are you moving?
    */
   public /*@ pure @*/ boolean moving() {
     return my_moving_indicator;
   }
 
   /**
    * Begin closing.
    */
   //@ requires opened();
   public void close() {
     my_open_indicator = false;
     my_closed_indicator = false;
     my_moving_indicator = true;
   }
 
   /**
    * Begin opening.
    */
   //@ requires closed();
   public void open() {
     my_open_indicator = false;
     my_closed_indicator = false;
     my_moving_indicator = true;
   }
 
   public Animation animation() {
     return my_animation;
   }
 
   public void animation(final Animation the_animation) {
     my_animation = the_animation;
   }
 
   public void animate() {
     /* When animate is called a frame of animation is played
      * If that is half way through the animation the barrier is closed
      * If it is complete then the barrier is open again
      */
     my_animation_counter++;
     THE_LOGGER.fine("Barrier animation step " +
                     my_animation_counter + " has been rendered.");
     if (my_animation_counter % (10 * 2) == 0) {
       my_open_indicator = true;
       my_closed_indicator = false;
       my_moving_indicator = false;
       my_animation_counter = 0;
     } else if (my_animation_counter % 10 == 0) {
       my_open_indicator = false;
       my_closed_indicator = true;
       my_moving_indicator = false;
     }
   }
 }
