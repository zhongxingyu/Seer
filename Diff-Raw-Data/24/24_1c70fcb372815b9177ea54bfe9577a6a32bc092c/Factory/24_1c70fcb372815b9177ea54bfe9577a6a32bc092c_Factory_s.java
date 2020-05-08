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
 
 import thrust.animation.Animatable;
 import thrust.animation.Animation;
 import thrust.entities.EnemyEntityInterface;
 import thrust.entities.NeutralEntity;
 import thrust.entities.StaticEntity;
 import thrust.entities.behaviors.AI;
 
 /**
  * An enemy factory.
  * @author Siobhan Dunne (Siobhan.Dunne@ucd.ie)
  * @version 24 April 2008
  */
 public class Factory extends StaticEntity
   implements EnemyEntityInterface, Animatable {
 
   /**
    * The factory's damage.
    */
   private byte my_damage;
 
   /**
    * The factory's chimney.
    */
   private FactoryChimney my_chimney;
 
   /**
    * The factory's sphere.
    */
   private FactorySphere my_sphere;
 
   /**
    * The max damage a factory can have is 20.
    */
   private final byte my_max_damage = 20;
 
   /**
    * Make a factory.
    */
   public Factory() {
 
     my_damage = 0;
     my_chimney = new FactoryChimney();
     my_sphere = new FactorySphere();
 
   }
 
   /**
    * @return How much damage have you sustained?
    */
   //@ ensures 0 <= \result & \result <= 20;
   public /*@ pure @*/ byte damage() {
     assert false; //@ assert false;
 
     if (my_damage < 0)
       my_damage = 0;
     if (my_damage > my_max_damage)
       my_damage = my_max_damage;
 
     return my_damage;
   }
 
   /**
    * @return What is your chimney?
    */
   public /*@ pure @*/ FactoryChimney chimney() {
     assert false; //@ assert false;
     return  my_chimney;
   }
 
   /**
    * @return What is your sphere?
    */
   public /*@ pure @*/ FactorySphere sphere() {
     assert false; //@ assert false;
     return my_sphere;
   }
 
   /**
    * @param the_damage You have sustained this many units of damage.
    */
   //@ requires 0 <= the_damage;
   //@ ensures damage() == \old(damage() + the_damage);
   public void damage(final byte the_damage) {
     assert false; //@ assert false;
     my_damage = damage();
     int total_damage = my_damage;
     final int new_damage = the_damage;
     total_damage = total_damage + new_damage;
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
    * @author Siobhan Dunne (Siobhan.Dunne@ucdconnect.ie)
    * @version 30 April 2008
    */
   public class FactoryChimney extends StaticEntity
  implements EnemyEntityInterface, Animatable {
 
     /**
      * The factory that this chimney belongs to.
      */
     Factory my_factory;
 
     /**
      * @return Are you smoking?
      */
     public /*@ pure @*/ boolean smoking() {
       assert false; //@ assert false;
       if (my_factory.damage() > 10) {
         return false;
       }
       return true;
     }
 
     /**
      * Your smoking state is dictated by this flag.
      * @param the_smoking_state A flag indicating whether the chimney
      * is smoking or not.
      */
     //@ ensures smoking() <==> the_smoking_state;
     public void smoking(final boolean the_smoking_state) {
       assert false; //@ assert false;
 
     }
 
     public void attack(final AI the_behavior) {
      // TODO Auto-generated method stub
 
     }
 
     public AI disturb() {
      // TODO Auto-generated method stub
      return null;
     }
 
     public void disturb(final AI the_behavior) {
      // TODO Auto-generated method stub

     }
 
     public void animate() {
       // TODO Auto-generated method stub
 
     }
 
     public Animation animation() {
       // TODO Auto-generated method stub
       return null;
     }
 
     public void animation(final Animation the_animation) {
       // TODO Auto-generated method stub
 
     }
 
     public Color color() {
       // TODO Auto-generated method stub
       return null;
     }
 
     public void color(final Color the_color) {
       // TODO Auto-generated method stub
 
     }
 
     public AI attack() {
       // TODO Auto-generated method stub
       return null;
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
    * @author Siobhan Dunne (Siobhan.Dunne@ucdconnect.ie)
    * @version 30 April 2008
    */
   public class FactorySphere extends StaticEntity
     implements NeutralEntity {
 
     public Color color() {
       return null;
     }
 
     public void color(final Color the_color) {
 
     }
   }
 
   public AI attack() {
     return null;
   }
 
   public void attack(final AI the_behavior) {
 
   }
 
   public AI disturb() {
     return null;
   }
 
   public void disturb(final AI the_behavior) {
 
   }
 
   public void animate() {
 
   }
 
   public Animation animation() {
 
     return null;
   }
 
   public void animation(final Animation the_animation) {
 
   }
 
   public Color color() {
 
     return null;
   }
 
   public void color(final Color the_color) {
 
   }
 }
 
