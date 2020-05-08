 package gt.general;
 
 import gt.general.aura.Aura;
 import gt.general.aura.Effect;
 
 import java.util.HashMap;
import java.util.Iterator;
 import java.util.Map;
 import java.util.Vector;
 
 import org.bukkit.Location;
 
 /**
  * our representation of Minecraft's LivingEntity
  */
 public abstract class Character {
 
 	/**Auras originating from the player*/
 	private final Vector<Aura> auras = new Vector<Aura>();
 	/**Effects affecting the player*/
 	private final Vector<Effect> effects = new Vector<Effect>();
 
 	/**unmodified Attributes*/
 	private final Map<CharacterAttributes, Double> baseAttributes = new HashMap<CharacterAttributes, Double>();
 	/**modified Attributes*/
 	private final Map<CharacterAttributes, Double> computedAttributes = new HashMap<CharacterAttributes, Double>();
 
 	private boolean computedAttributesTainted = true;
 
 	/**
 	 * creates a new Character
 	 *
 	 * @param defaultHeroSpeed
 	 */
 	public Character() {
 
 		setAttribute(CharacterAttributes.HEALTH, 1.0);
 		setAttribute(CharacterAttributes.SPEED, 100.0);
 		setAttribute(CharacterAttributes.JUMPMULTIPLIER, 1.0);
 	}
 
 	/**
 	 * @return character's current speed
 	 */
 	public double getCurrentSpeed() {
 		return Math.max(computedAttributes.get(CharacterAttributes.SPEED), 0);
 	}
 
 	/**
 	 * @return character's default speed
 	 */
 	public double getDefaultSpeed() {
 		return baseAttributes.get(CharacterAttributes.SPEED);
 	}
 
 	/**
 	 * @return the current Location of this Character
 	 */
 	public Location getLocation() {
 		return null;
 	}
 
 	/**
 	 * @return the auras
 	 */
 	public Vector<Aura> getAuras() {
 		return auras;
 	}
 
 	/**
 	 * @return the effects
 	 */
 	public Vector<Effect> getEffects() {
 		return effects;
 	}
 
 	/**
 	 * @param aura
 	 *            the aura to be added to this character
 	 */
 	public void addAura(final Aura aura) {
 		aura.setOwner(this);
 		auras.add(aura);
 	}
 
 	/**
 	 * @param effect
 	 *            The effect to be added to this character
 	 */
 	public void addEffect(final Effect effect) {
 		computedAttributesTainted = true;
 		effects.add(effect);
 	}
 
 	
 	public void removeEffect(final Effect effect) {
 		computedAttributesTainted = true;
 		effects.remove(effect);
 	}
 	
 	/**
 	 * adds an attribute to the collection of attributes of this character
 	 *
 	 * @param attribute
 	 *            Type of the Attribute
 	 * @param base
 	 *            Immutable base value of the attribute
 	 */
 	protected void setAttribute(final CharacterAttributes attribute,
 			final double base) {
 		baseAttributes.put(attribute, base);
 		computedAttributes.put(attribute, base);
 	}
 
 	/**
 	 * @param attribute
 	 *            Type of the attribute
 	 * @return Immutable base value
 	 */
 	public double getBaseAttribute(final CharacterAttributes attribute) {
 		return baseAttributes.get(attribute);
 	}
 
 	/**
 	 * the value must be calculated on base of the baseAttribute - not on the
 	 * current computed value
 	 *
 	 * @param attribute
 	 *            the attribute to be modified
 	 * @param value
 	 *            the value to be added to the attribute
 	 */
 	public void addToAttribute(final CharacterAttributes attribute,
 			final double value) {
 		computedAttributes.put(attribute, computedAttributes.get(attribute)
 				+ value);
 	}
 
 	/**
 	 * @param attribute
 	 *            the attribute to be modified
 	 * @param value
 	 *            the value which is multiplied to the current value
 	 */
 	public void scaleAttribute(final CharacterAttributes attribute,
 			final double value) {
 		computedAttributes.put(attribute, computedAttributes.get(attribute)
 				* value);
 	}
 
 	/**
 	 * performs the attribute updates on a tick
 	 */
 	public void simulateEffects() {
 
 		for (Effect effect : effects) {
 			if (effect.remainingTicks() == 0) {
 				effects.remove(effect);
 				computedAttributesTainted = true;
 			} else {
 				effect.performTick();
 			}
 		}
 
 		if (computedAttributesTainted) {
 			calculateAttributes();
 			applyEffects();
 			computedAttributesTainted = false;
 		}
 	}
 
 	/**
 	 * calculates the attribute values on base of the current effects
 	 */
 	private void calculateAttributes() {
 		for (CharacterAttributes attribute : baseAttributes.keySet()) {
 			computedAttributes.put(attribute, baseAttributes.get(attribute));
 		}
 
 		for (Effect effect : effects) {
 			effect.takeEffect(this);
 		}
 		
 		
 	}
 	
 	public abstract void applyEffects();
 
 	/**
 	 * returns the computed value of the attribute
 	 * 
 	 * @param attr the attribute to be retrieved
 	 * @return the value of the attribute
 	 */
 	public double getAttribute(final CharacterAttributes attr) {
 		return computedAttributes.get(attr);
 	}
 
 	
 }
