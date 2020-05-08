 package vooga.fighter.objects;
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.List;
 
 import vooga.fighter.objects.utils.Counter;
 import vooga.fighter.objects.utils.Effect;
 import vooga.fighter.objects.utils.UpdatableLocation;
 
 /**
  * Object that can inflict damage on other moveable game objects
  * 
  * @author james, alanni, David Le
  *  
  */
 public class AttackObject extends GameObject{
 
     private Counter myCounter;
     private GameObject myOwner;
     private List<Effect> myEffects;
     private int myPower;   
     private int mySpeed;
     private int myDirection;   
     
     /**
      * Constructs an AttackObject with the given owner.
      * 
      * Will update to use ObjectLoader.
      */
     public AttackObject() {
     	super();
         myEffects = new ArrayList<Effect>();
         myCounter = new Counter();
     }
     
    public AttackObject (AttackObject other, UpdatableLocation center){
     	super();
        this.mySpeed= other.mySpeed;
        this.myDirection= other.myDirection; 
        this.myPower= other.myPower; 
        this.myEffects= other.myEffects;
        this.myOwner= other.myOwner;
        this.myCounter= other.myCounter;   
     	setLocation(center);
     }
     
     /**
      * Updates the attack object
      */
     public void update(){
     	super.update();
     	myCounter.decrementCounter();
     }
     /**
      * Adds an effect to myEffects
      */
     public void addEffect(Effect toAdd){
     	myEffects.add(toAdd);
     }
     
     /**
      * 
      * @return list of effects
      */
     public List<Effect> getEffects(){
     	return myEffects;
     }
     
     /**
      * Gets the character who created the attack
      */
     public GameObject getOwner(){
     	return myOwner; 
     }
     
     /**
      * Sets the damage done by the attack
      */
     public void setPower(int power) {
     	myPower = power;
     }
     
     /**
      * Inflicts damage upon a target player.
      */
     public int inflictDamage(CharacterObject target){
     	return target.changeHealth(-myPower);
     }
     
     /**
      * Designates a character as a target of this attack's effects.
      */
     public void addTargetForEffects(CharacterObject target){
         for (Effect effect : myEffects) {
             Effect copyOfEffect = effect.getCloneOfEffect();
             target.addActiveEffect(copyOfEffect);
         }
     }    
     
     /**
      * Sets the amount of time left in attack to zero
      */
     public void endCounter(){
     	myCounter.setCounter(0); 
     }
     
     /**
      * Returns true if this attack object has expired.
      */
     public boolean shouldBeRemoved() {
         return !myCounter.hasCountRemaining();
     }
 
 }
