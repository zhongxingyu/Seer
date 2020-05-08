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
  * A blinking star in space.
  * @author Colin Casey (colin.casey@org.com)
  * @author Joe Kiniry (kiniry@acm.org)
  * @version 23 April 2008
  */
 public class Star extends StaticEntity
   implements NeutralEntity, Animatable {
 
   /*@ public invariant (* A star's location is in space. *);
     @ public invariant (* A star interacts with no other entities. *);
     @ public invariant (* Each star blinks irregularly. *);
     @ public invariant (* A star's shape is always a small square. *);
     @*/
 
   /** Logger for animation. */
   private static final Logger THE_LOGGER =
    Logger.getLogger(Explosion.class.getName());
   /** The frames in the Star animation. */
   private transient Animation my_animation;
   /** Animation frame counter. */
   private transient int my_animation_counter;
 
   /** Star Constructor. */
   public Star() {
     //shape(small square);
   }
 
   public Animation animation() {
     return my_animation;
   }
 
   public void animation(final Animation the_animation) {
     my_animation = the_animation;
   }
 
   public void animate() {
     /* When animate is called a frame of animation is played
      * Resets after star animation completes
      */
     my_animation_counter++;
     THE_LOGGER.fine("Star animation step " +
                      my_animation_counter + " has been rendered.");
     if (my_animation_counter % 10 == 0) {
       my_animation_counter = 0;
     }
   }
 }
