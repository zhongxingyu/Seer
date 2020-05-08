 package ch.kanti_wohlen.asteroidminer.powerups;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.World;
 
 /**
  * Launcher for a random PowerUp at a given location in the world. <br>
  * To spawn the {@link PowerUp}, <code>run()</code> must be called.
  */
 public class PowerUpLauncher implements Runnable {
 
 	private static final float DROP_PROBABILITIES_SUM;
 	static {
 		float sum = 0f;
 		for (PowerUpType type : PowerUpType.values()) {
 			sum += type.getDropChance();
 		}
 		DROP_PROBABILITIES_SUM = sum;
 	}
 
 	private final World world;
 	private final Vector2 position;
 
 	/**
 	 * Creates a <code>PowerUpLauncher</code> for a given location in the world.
 	 * 
 	 * @param theWorld
 	 *            the {@link World} currently in use. May not be null.
 	 * @param powerUpPosition
 	 *            the position to spawn the <code>PowerUp</code> at.
 	 */
 	public PowerUpLauncher(World theWorld, Vector2 powerUpPosition) {
 		world = theWorld;
		position = powerUpPosition;
 	}
 
 	@Override
 	public void run() {
 		PowerUpType type = randomPowerUp();
 		switch (type) {
 		case BOMB:
 			new BombPowerUp(world, position);
 			break;
 		case FIRING_DAMAGE:
 			new WeaponFiringForcePowerUp(world, position);
 			break;
 		case FIRING_SPEED:
 			new FiringSpeedPowerUp(world, position);
 			break;
 		case HEALTH:
 			new HealthPowerUp(world, position);
 			break;
 		case MOVEMENT_SPEED:
 			new SpeedPowerUp(world, position);
 			break;
 		case SHIELD:
 			new ShieldPowerUp(world, position);
 			break;
 		default:
 			Gdx.app.log("PowerUpLauncher", "Could not add PowerUp - unknown PowerUpType " + String.valueOf(type));
 			break;
 		}
 	}
 
 	private PowerUpType randomPowerUp() {
 		float randomProbability = MathUtils.random(DROP_PROBABILITIES_SUM);
 		float prob = 0f;
 		for (PowerUpType type : PowerUpType.values()) {
 			prob += type.getDropChance();
 			if (prob >= randomProbability) {
 				return type;
 			}
 		}
 		// Should never be reached
 		return null;
 	}
 }
