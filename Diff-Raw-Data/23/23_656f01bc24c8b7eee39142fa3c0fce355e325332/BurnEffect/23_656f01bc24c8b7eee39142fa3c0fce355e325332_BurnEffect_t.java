 package vooga.fighter.model.effects;
 
 import vooga.fighter.model.ModelConstants;
 import vooga.fighter.model.objects.CharacterObject;
 import vooga.fighter.model.utils.Effect;
 
 
 /**
  * Example of an implementation of effect. Burn effects do damage over time to
  * their target after they are applied.
  * 
  * @author James Wei
  * 
  */
 public class BurnEffect extends Effect {
 
     /**
     * Calls the superclass effect constructor.
     */
    public BurnEffect() {
        super();
    }
    
    /**
      * Constructs a burn effect with given duration and damage over time, and null owner.
      * @param duration is length of burn
      * @param damage is damage over time
      */
     public BurnEffect (int duration, int damage) {
         super(duration);
         addProperty(ModelConstants.EFFECT_PROPERTY_BURN_DAMAGE, damage);
     }
 
     /**
      * Constructs a burn effect with given owner, duration, and damage over time.
      * @param owner is target of burn
      * @param duration is length of burn
      * @param damage is damage over time
      */
     public BurnEffect (CharacterObject owner, int duration, int damage) {
         super(owner, duration);
         addProperty(ModelConstants.EFFECT_PROPERTY_BURN_DAMAGE, damage);
     }
 
     /**
      * Applies a burn effect of some damage over time.
      */
     @Override
     public void applyEffect () {
         int damage = getProperty(ModelConstants.EFFECT_PROPERTY_BURN_DAMAGE);
         getOwner().changeHealth(-damage);
     }
 
     /**
      * Returns a deep copy of this burn effect.
      */
     @Override
     public Effect getCloneOfEffect () {
         int duration = getProperty(ModelConstants.EFFECT_PROPERTY_DURATION);
         int damage = getProperty(ModelConstants.EFFECT_PROPERTY_BURN_DAMAGE);
         BurnEffect result = new BurnEffect(getOwner(), duration, damage);
         return result;
     }
 
 }
