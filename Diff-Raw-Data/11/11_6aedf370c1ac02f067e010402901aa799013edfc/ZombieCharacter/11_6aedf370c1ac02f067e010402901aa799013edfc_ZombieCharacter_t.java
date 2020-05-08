 package gt.general.character;
 
 import org.bukkit.Location;
 import org.bukkit.entity.PigZombie;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 /**
  * wraps a bukkit zombie and modifies its speed
  */
 public class ZombieCharacter extends Character {
 	private final PigZombie zombie;
 	private PotionEffect potionEffect;
 
 	private final static int POTION_DURATION = 20 * 60 * 20;
 	private final static int MAX_POTION_AMPLIFIER = 5;
 	
 	/**
 	 * create a new zombie
 	 * 
 	 * @param zombie
 	 *            bukkit zombie
 	 */
 	public ZombieCharacter(final PigZombie zombie) {
 		super();
 		this.zombie = zombie;
 	}
 
 	/**
 	 * @return the wrapped zombie
 	 */
 	public PigZombie getZombie() {
 		return zombie;
 	}
 
 	/**
 	 * @param speed
 	 *            speed of the zombie
 	 * @return the amplifier that is necessary to get a specific speed
 	 */
 //	private int speedToAmplifier(final double speed) {
 //		int result = 0;
 //
 //		// ~25% Faster
 //		if (speed > 1.6) {
 //			return (int) (speed - 1.4) * 4;
 //		}
 //
 //		if (speed < 1.4) {
 //			result = (int) Math.round(((1.8 - speed) / 0.15));
 //			result = result - 1;
 //			return result;
 //		}
 //
 //		return result;
 //	}
 
 	@Override
 	public void applyAttributes() {
 		if (potionEffect != null) {
 			zombie.removePotionEffect(potionEffect.getType());
 		}
 		
 		if(getCurrentSpeed() > getDefaultSpeed()) {
 			potionEffect = getSpeedPotion();
 			
 			zombie.addPotionEffect(potionEffect);
 			System.out.println(getCurrentSpeed() + " @ " + getEffects().size() + " => " + potionEffect.getType() + "_" + potionEffect.getAmplifier());
 			
 		} else if (getCurrentSpeed() < getDefaultSpeed()) {
 			potionEffect = getSlowPotion();
 			
 			zombie.addPotionEffect(potionEffect);
 			System.out.println(getCurrentSpeed() + " @ " + getEffects().size() + " => " + potionEffect.getType() + "_" + potionEffect.getAmplifier());
 		}
 	}
 
 	private PotionEffect getSpeedPotion() {
 		return new PotionEffect(PotionEffectType.SPEED,
 								POTION_DURATION,
 								speedToPotionAmplifier());
 	}
 
 	private PotionEffect getSlowPotion() {
 		return new PotionEffect(PotionEffectType.SLOW,
 								POTION_DURATION,
								-speedToPotionAmplifier());
 	}
 	
 	private int speedToPotionAmplifier() {
 		Double speed = getCurrentSpeed();
 		Double defaultSpeed = getDefaultSpeed();
 		
 		return (int) ((speed - defaultSpeed) / (defaultSpeed / MAX_POTION_AMPLIFIER));
 		
 		
 	}
 
 	@Override
 	public Location getLocation() {
 		return zombie.getLocation();
 	}
 
 }
