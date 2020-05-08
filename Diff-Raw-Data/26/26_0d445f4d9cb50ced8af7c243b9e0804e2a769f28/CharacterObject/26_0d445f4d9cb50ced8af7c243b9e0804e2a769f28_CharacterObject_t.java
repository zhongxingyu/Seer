 package vooga.fighter.objects;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import vooga.fighter.objects.utils.Effect;
 import vooga.fighter.objects.utils.Health;
 import util.Vector;
 import vooga.fighter.objects.utils.UpdatableLocation;
 
 
 /**
  * Represents a character in the game, as well as the character's current state
  * in the given session.
  * 
  * @author alanni, james, David Le
  * 
  */
 public class CharacterObject extends GameObject {
 
     private Map<String, AttackObject> myAttacks;
     private List<Effect> myActiveEffects;
     private Health myHealth;
 
     /**
      * Constructs a new CharacterObject.
      * 
      * Note: Dayvid once the object loader is functional we will replace this
      * constructor to take in just an ID, then we will load parameters from XML.
      */
     public CharacterObject(int objectId, UpdatableLocation center) {
         super();
         myAttacks = new HashMap<String, AttackObject>();
         myActiveEffects = new ArrayList<Effect>();
         myHealth = new Health();
         setLoader(new CharacterLoader(objectId, this));
         setLocation(center);
         setCurrentState("stand");
         setImageData();
     }
 
     /**
      * Updates the character for one game loop cycle. Applies movement from acceleration
      * forces acting on the character.
      */
 
     public void update() {
         super.update();
         if (getCurrentState().hasCompleted()) {
             setCurrentState("stand");
         }
         for (Effect effect : myActiveEffects) {
             effect.update();
         }
     }
 
     /**
      * Adds an effect to this character's list of active effects.
      */
     public void addActiveEffect(Effect effect) {
         effect.setOwner(this);
         myActiveEffects.add(effect);
     }
 
     /**
      * Removes an effect from this character's list of active effects.
      */
     public void removeActiveEffect(Effect effect) {
         myActiveEffects.remove(effect);
     }
 
     /**
      * Returns list of currently active effects on this character.
      */
     public List<Effect> getActiveEffects() {
         return myActiveEffects;
     }
 
     /**
      * Adds an AttackObject to the list of attacks available for this character.
      * Note that this attack will not be added to the list of game objects in a
      * level, thus it will not update and should be used solely for generating
      * other attack objects as needed. Overwrites any existing attack.
      */
     public void addAttack(String key, AttackObject object) {
         myAttacks.put(key, object);
     }
 
     /**
      * Creates and returns an attack object based on a given identifier. Returns null
      * if the specified attack does not exist.
      * 
      * Note: For now just using String to represent attack types, but this is obviously
      * subject to change.
      */
    public AttackObject createAttack(String key) {
         if (myAttacks.containsKey(key)) {
             return myAttacks.get(key);
         }
         else {
             return null;
         }
     }
 
     /**
      * Returns the health of the character.
      */
     public Health getHealth() {
         return myHealth;
     }
 
     /**
      * Returns whether or not the character has remaining health.
      */
     public boolean hasHealthRemaining() {
         return myHealth.hasHealthRemaining();
     }
 
     public void setHealth(int amount) {
         myHealth.setHealth(amount);
     }
 
     /**
      * Changes the player's health by a given amount. Positive input raises it, and
      * negative input decreases it. Returns health remaining.
      */
     public int changeHealth(int amount) {
         return myHealth.changeHealth(amount);
     }
 
     /**
      * Creates a new AttackObject by cloning the object identified by the given key
      * in the attacks map.
      */
     public void attack(String attack) {
         setCurrentState("weakPunch");
         if (myAttacks.containsKey(attack)) {
             new AttackObject(myAttacks.get(attack), getLocation());
         }
     }
 
     /**
      * Moves in given direction at speed of character
      */
     public void move(int direction) {
         setCurrentState("moveRight");
         getLocation().translate(new Vector(direction, getProperty("speed")));
     }
 
     /**
      * Will add jump method
      */
     public void jump() {        
 
     }
     
     public boolean shouldBeRemoved() {
         return false;
     }
 
 }
