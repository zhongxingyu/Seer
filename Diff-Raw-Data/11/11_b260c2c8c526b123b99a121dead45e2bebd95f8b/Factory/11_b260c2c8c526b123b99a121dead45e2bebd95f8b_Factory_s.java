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
 
 import thrust.animation.Animatable;
 import thrust.animation.Animation;
 import thrust.entities.EnemyEntity;
 import thrust.entities.EnemyAI;
 import thrust.entities.NeutralEntity;
 import thrust.entities.StaticEntity;
 import thrust.entities.behaviors.AI;
 
 /**
  * An enemy factory.
  * @author Eoin Healy (eoin.healy@gmail.com)
  * @version 18 April 2008
  */
 public class Factory extends StaticEntity
   implements EnemyEntity, Animatable {
 
   /**
    * How much damage has been taken?
    */
   private byte my_damage_taken;
 /**
  * The sphere of the factory.
  */
   private FactorySphere my_factory_sphere;
 /**
  * The Chimney of the factory.
  */
   private FactoryChimney my_factory_chimney;
 /**
  * The color of a factory.
  */
   private Color my_color = Color.yellow;
 /**
  * The AI.
  */
   private EnemyAI my_enemy_ai;
 /**
  * Animation variable.
  */
   private Animation my_animation;
 /**
  * The steps in the animation,text based.
  */
   private final Object[] my_animation_steps = {"F"};
 
 /**
  * Current animation step.
  */
   private int my_step;
   /**
    * @param double[] the_position
    * @param double the_orientation
    * @param String the_intial_shape_name
    * @param Shape the_intial_shape
    * @param byte the_intial_state
    */
   public Factory(final double[] the_position,
                  final double the_orientation,
                  final String the_initial_shape_name,
                  final Shape the_initial_shape,
                  final byte the_inital_state) {
     super.set_state(the_position, the_orientation,
                     my_color, the_initial_shape_name,
                     the_initial_shape, the_inital_state);
   }
   /**
    * @return How much damage have you sustained?
    */
   //@ ensures 0 <= \result & \result <= 20;
   public /*@ pure @*/ byte damage() {
     return my_damage_taken;
   }
 
   /**
    * @return What is your chimney?
    */
   public /*@ pure @*/ FactoryChimney chimney() {
     return my_factory_chimney;
   }
 
   /**
    * @return What is your sphere?
    */
   public /*@ pure @*/ FactorySphere sphere() {
     return my_factory_sphere;
   }
 
   /**
    * @param the_damage You have sustained this many units of damage.
    */
   //@ requires 0 <= the_damage;
   //@ ensures damage() == \old(damage() - the_damage);
   public void damage(final byte the_damage) {
     my_damage_taken += the_damage;
   }
 
   /*@ public invariant (* All factories have exactly one sphere and
     @                     one chimney. *);
     @ public invariant (* A bullet causes 1 unit of damage. *);
     @ public invariant (* Each second 1 unit of damage is eliminated. *);
     @ public initially (* A factory initially has zero units of damage. *);
     @ public initially damage() == 0;
     @ public invariant (* A factory can sustain 20 units of damage before
     @                     it is destroyed. *);
     @ public invariant (* A factory with more than 10 units of damage
     @                     has a chimney that does not smoke. *);
     @ public invariant 10 < damage() ==> !chimney.smoking();
     @ public invariant (* A factory with at most 10 units of damage has
     @                     a smoking chimney. *);
     @ public invariant damage() <= 10 ==> chimney.smoking();
     @*/
 
   //@ public invariant (* See constraint on color in FactoryChimney. *);
   //@ public invariant color() == chimney().color();
 
   /**
    * A chimney of a factory.
    * @author Eoin Healy (eoin.healy@gmail.com)
    * @version 18 April 2008
    */
   public class FactoryChimney extends StaticEntity
     implements EnemyEntity, Animatable {
 /**
  *  Is the chimney smoking?
  */
     private boolean my_smoking_state;
 /**
 * The color of a factory.
 */
     private Color my_color = Color.yellow;
/**
 * Animation variable.
 */
    private Animation my_animation;
 /**
  * The steps in the animation,text based.
  */
     private final Object[] my_animation_steps = {"|", " "};
     /**
      * @param double[] the_position
      * @param double the_orientation
      * @param String the_intial_shape_name
      * @param Shape the_intial_shape
      * @param byte the_intial_state
      */
     public FactoryChimney(final double[] the_position,
                    final double the_orientation,
                    final String the_initial_shape_name,
                    final Shape the_initial_shape,
                    final byte the_inital_state) {
       super.set_state(the_position, the_orientation,
                       my_color, the_initial_shape_name,
                       the_initial_shape, the_inital_state);
 
     }
     /**
      * @return Are you smoking?
      */
     public /*@ pure @*/ boolean smoking() {
       return my_smoking_state;
     }
 
     /**
      * Your smoking state is dictated by this flag.
      * @param the_smoking_state A flag indicating whether the chimney
      * is smoking or not.
      */
     //@ ensures smoking() <==> the_smoking_state;
     public void smoking(final boolean the_smoking_state) {
       my_smoking_state = the_smoking_state;
     }
     public AI attack() {
       return my_enemy_ai.attack();
     }
     public void attack(final AI the_behavior) {
       my_enemy_ai.attack(the_behavior);
     }
     public AI disturb() {
       return my_enemy_ai.disturb();
     }
     public void disturb(final AI the_behavior) {
       my_enemy_ai.disturb(the_behavior);
     }
     public void animate() {
       if (my_step == my_animation_steps.length - 1) {
         my_step = 0;
       } else
         my_step++;
       animation((Animation) my_animation_steps[my_step]);
 
     }
     public Animation animation() {
       return my_animation;
     }
     public void animation(final Animation the_animation) {
       my_animation = the_animation;
 
     }
 
     /*@ public invariant (* A factories chimney is the same color as
       @                     its factory. *);
       @ public invariant (* The goal sphere is destroyed by a
       @                     factory's chimney. *);
       @ public invariant (* The spaceship is destroyed by a factory's
       @                     chimney. *);
       @*/
   }
 
   /**
    * A sphere of a factory.
    * @author Eoin Healy (eoin.healy@gmail.com)
    * @version 18 April 2008
    */
   public class FactorySphere extends StaticEntity
     implements NeutralEntity {
 /**
 * The color of the sphere.
 */
     private Color my_color = Color.green;
 /**
 * @param double[] the_position
 * @param double the_orientation
 * @param String the_intial_shape_name
 * @param Shape the_intial_shape
 * @param byte the_intial_state
 */
     public FactorySphere(final double[] the_position,
                        final double the_orientation,
                        final String the_initial_shape_name,
                        final Shape the_initial_shape,
                        final byte the_inital_state) {
       super.set_state(the_position, the_orientation,
                           my_color, the_initial_shape_name,
                           the_initial_shape, the_inital_state);
 
     }
     /*@ public invariant (* A factory sphere's color is always green. *);
       @ public invariant color() == thrust.entities.properties.GameColor.GREEN;
       @ public invariant (* The goal sphere is not destroyed by a
       @                     factory's sphere. *);
       @*/
   }
 
   public AI attack() {
     return my_enemy_ai.attack();
   }
   public void attack(final AI the_behavior) {
     my_enemy_ai.attack(the_behavior);
   }
   public AI disturb() {
     return my_enemy_ai.disturb();
   }
   public void disturb(final AI the_behavior) {
     my_enemy_ai.disturb(the_behavior);
   }
   public void animate() {
     // Dunno if this needs anything ATM.
   }
   public Animation animation() {
     return my_animation;
   }
   public void animation(final Animation the_animation) {
     my_animation = the_animation;
   }
 }
