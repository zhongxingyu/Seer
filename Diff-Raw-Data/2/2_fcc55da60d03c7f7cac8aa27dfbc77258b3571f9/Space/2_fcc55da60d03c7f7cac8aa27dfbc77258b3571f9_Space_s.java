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
 
 import java.awt.Color;
 import java.awt.Shape;
 import java.util.Collection;
 
 import thrust.animation.Animatable;
 import thrust.entities.NeutralEntity;
 import thrust.entities.StaticEntity;
 
 /**
  * The vacuum in which entities exist.
  * @author jdouglas (jd2088@gmail.com)
  * @version 18 April 2008
  */
 public class Space extends StaticEntity
   implements NeutralEntity, Animatable {
 
   public Space(final double[] the_position,
                final double the_orientation, final Color the_color,
                final String the_initial_shape_name,
                final Shape the_initial_shape,
               final byte the_inital_state) {
 
     super();
     super.set_state(the_position, the_orientation, the_color,
                   the_initial_shape_name,
                   the_initial_shape,
                   the_initial_state);
   }
 
   /**
    * @return What are your stars?"
    */
   public /*@ pure @*/ Collection stars() {
     assert false; //@ assert false;
     return null;
   }
 
 
 
     /*public void animate() {
     my_animation.animate();
   }
 
   public Animation animation() {
     return my_animation.animation();
   }*/
 
   /**
    * Add this star to space.
    * @param the_star the star to add.
    */
   public void add_star(final Star the_star) {
     assert false; //@ assert false;
   }
 
   //@ public invariant (* Terrain and space are disjoint. *);
 }
