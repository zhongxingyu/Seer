 package gt.general.aura;
 
 import gt.general.Character;
 import gt.general.Hero;
 import gt.plugin.helloworld.HelloWorld;
 
 import org.bukkit.Bukkit;
 
 /**
  * Class for auras, which distribute effects
  */
 public class Aura implements Runnable {
 	/**Owner from which the Aura originates*/
 	private Character owner = null;
 	/**Factory which produces the Effect*/
 	private final EffectFactory effectFactory;
 
 	/**radius of area of effect*/
 	private final int distance;
 	/**every rate ticks the Aura spreads its Effect*/
 	private final int rate;
 	/**Duration of ticks, which the Aura lasts*/
 	private int duration;
 
 	private int taskId = -1;
 
 	/**Constant for infinite duration*/
 	public static final int INFINITE_DURATION = Integer.MIN_VALUE;
 	/**Constant for a rate of 1/Tick*/
 	public static final int EACH_TICK = 1;
 	/**Constant for distance, to only affect owner*/
 	public static final int OWNER_ONLY = 0;
 
 	/**
 	 * generates a new Aura
 	 *
 	 * @param effectFactory
 	 *            a factory for the Effect to be spread
 	 * @param distance
 	 *            the distance in which the Effect is applied to Entities
 	 * @param duration
 	 *            after duration Ticks the Aura is discarded
 	 * @param rate
 	 *            n-th tick which should spread the effect
 	 */
 	public Aura(final EffectFactory effectFactory, final int distance,
 			final int duration, final int rate) {
 		this.owner = null;
 		this.effectFactory = effectFactory;
 		this.distance = distance;
 		this.duration = duration;
 		this.rate = rate;
 	}
 
 	/**
 	 * sets the newowner and starts scheduling of the aura
 	 *
 	 * @param owner
 	 *            the owner of this Aura
 	 */
 	public void setOwner(final Character owner) {
 		ensureUnlinked();
 
 		this.owner = owner;
 
 		taskId = Bukkit.getScheduler().scheduleAsyncRepeatingTask(
				HelloWorld.getPlugin(), this, 0, rate);
 	}
 
 	/**
 	 * ensures that this is not simulated currently and there is no owner
 	 */
 	private void ensureUnlinked() {
 		if (taskId != -1) {
 			Bukkit.getScheduler().cancelTask(taskId);
 		}
 
 		if (owner != null) {
 			owner.getAuras().remove(this);
 		}
 	}
 
 	@Override
 	public void run() {
 		if (dead()) {
 			ensureUnlinked();
 		}
 
 		if (owner != null) {
 			spreadEffect();
 		}
 	}
 
 	/**
 	 * @return true if this aura should be dismissed
 	 */
 	private boolean dead() {
 		if (duration == INFINITE_DURATION) {
 			return false;
 		} else {
 			duration = -rate;
 			return duration < 0;
 		}
 	}
 
 	/**
 	 * spreads the effect of this aura to all Characters in distance
 	 */
 	private void spreadEffect() {
 
 		if (distance == OWNER_ONLY) {
 			
 			if(owner instanceof Hero) {
 				((Hero)owner).getPlayer().sendMessage("adding aura stack");
 			}
 			
 			owner.addEffect(effectFactory.getEffect());
 		} else {
 			// TODO spread auras
 			throw new RuntimeException("not implemented yet");
 		}
 	}
 }
