 package yuuki.action;
 
 import yuuki.entity.Character;
 
 /**
  * Increases hit points.
  */
 public class Heal extends Skill implements Cloneable {
 	
 	/**
 	 * Creates a new Heal skill that heals the specified amount and uses the
 	 * specified amount of mana.
 	 * 
 	 * @param amount The amount of health that is increased.
 	 * @param cost The amount of mana that the heal costs.
 	 */
 	public Heal(double amount, double cost) {
 		super("Heal", amount, cost, null, null);
 	}
 	
 	/**
 	 * Creates a clone of this Heal.
 	 * 
 	 * @return The clone.
 	 */
 	@Override
 	public Heal clone() {
 		return (Heal) super.clone();
 	}
 	
 	/**
 	 * Creates a Heal from an existing one.
 	 * 
 	 * @param args Must contain two doubles; first is amount of healing, second
 	 * is mana cost.
 	 */
 	@Override
 	public Heal createInstance(String[] args) {
 		double amount = Double.parseDouble(args[0]);
 		double cost = Double.parseDouble(args[1]);
 		return new Heal(amount, cost);
 	}
 	
 	/**
 	 * Has no effect.
 	 */
 	@Override
 	public void applyBuffs() {}
 	
 	/**
 	 * Heals the first target.
 	 */
 	@Override
 	public void applyEffect() {
 		Character target = targets.get(0);
 		int amount = (int) this.effect;
 		target.gainHP(amount);
 	}
 	
 	@Override
 	public void setEffectStat(Character c) {
 		effectStat = c.getHPStat().clone();
 	}
 	
 }
