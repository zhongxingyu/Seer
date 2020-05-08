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
 
 import thrust.animation.Animatable;
 import thrust.entities.EnemyEntity;
 import thrust.entities.NeutralEntity;
 import thrust.entities.StaticEntity;
 
 /**
  * An enemy factory.
  * @author Ciaran Hale (Ciaran.hale@ucdconnect.ie)
  * @author Joe Kiniry (kiniry@acm.org)s
  * @version 24 April 2008
  */
 public class Factory extends StaticEntity
   implements EnemyEntity, Animatable {
 
  /** Amount of damage inflicted upon the factory. */
 
   private byte my_damage_staus;
 
   /** This factories chimney. */
 
   private FactoryChimney my_factory_chimney;
 
   /** This factories sphere. */
 
  private FactorySphere my_sphere;
 
   /**
    * @return How much damage have you sustained?
    */
 
   public /*@ pure @*/ byte damage() {
 
     return my_damage_status;
   }
 
   /**
    * return your chimney.
    */
   public /*@ pure @*/ FactoryChimney chimney() {
 
     return my_factory_chimney;
   }
 
   /**
    * @return What is your sphere?
    */
  public /*@ pure @*/ FactorySphere sphere() {

    return my_sphere;
  }

   /**
    * @param the_damage You have sustained this many units of damage.
    */
   //@ requires 0 <= the_damage;
   //@ ensures damage() == \old(damage() - the_damage);
 
   public void damage(byte the_damage) {
     my_damage_staus += the_damage;
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
     @ public invariant 10 < damage() ==> !chimney().smoking();
     @ public invariant (* A factory with at most 10 units of damage has
     @                     a smoking chimney. *);
     @ public invariant damage() <= 10 ==> chimney().smoking();
     @*/
 
   //@ public invariant (* See constraint on color in FactoryChimney. *);
   //@ public invariant color() == chimney().color();
 
   /**
    * A chimney of a factory.
    * @author Joe Kiniry (kiniry@acm.org)
    * @version 18 April 2008
    */
 public class FactoryChimney extends StaticEntity
     implements EnemyEntity, Animatable {
 
    /**
     *  Whats my smoking status?.
     */
 
 
  private boolean my_smoking_boolean;
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
 
   public void smoking(final the_smoking_state) {
 
 	  my_smoking_state = the_smoking_state;
   }
 
     
     
 
     /*@ public invariant (* A factories chimney is the same color as
       @                     its factory. *);
       @ public invariant (* The goal sphere is destroyed by a
       @                     factory's chimney. *);
       @ public invariant (* The spaceship is destroyed by a factory's
       @                     chimney. *);
       @*/
   
 
   /**
    * A sphere of a factory.
    * @author Joe Kiniry (kiniry@acm.org)
    * @version 18 April 2008
    */
   public class FactorySphere extends StaticEntity
     implements NeutralEntity {
     
	  
 	  /*@ public invariant (* A factory sphere's color is always green. *);
       @ public invariant color() == java.awt.Color.GREEN;
       @ public invariant (* The goal sphere is not destroyed by a
       @                     factory's sphere. *);
       @*/
   }
 
