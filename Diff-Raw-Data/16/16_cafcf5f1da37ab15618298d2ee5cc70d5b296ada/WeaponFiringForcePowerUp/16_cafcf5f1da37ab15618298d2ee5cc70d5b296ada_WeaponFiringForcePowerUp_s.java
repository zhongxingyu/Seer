 package ch.kanti_wohlen.asteroidminer.powerups;
 
 import ch.kanti_wohlen.asteroidminer.Player;
 import ch.kanti_wohlen.asteroidminer.TaskScheduler;
 import ch.kanti_wohlen.asteroidminer.Textures;
 import ch.kanti_wohlen.asteroidminer.entities.Laser;
 
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.World;
 
 public class WeaponFiringForcePowerUp extends PowerUp {
 	
 	private static final float DROP_FREQUENCY = 1f;
 	private static final int DAMAGE_INCREASE = 25;
	private static final double POWER_UP_DURATION = 10000.0;
 
 	public WeaponFiringForcePowerUp(World world, Vector2 position) {
 		super(world, position);
 	}
 
 	@Override
 	public void onPickUp(Player player) {
 		player.getSpaceShip().setLaserDamage(Laser.DEFAULT_DAMAGE + DAMAGE_INCREASE);
 		TaskScheduler.INSTANCE.runTaskLater(new PowerUpRemover(player), POWER_UP_DURATION);
 	}
 
 	@Override
 	public float getDropFrequency() {
 		return DROP_FREQUENCY;
 	}
 
 	@Override
 	public void render(SpriteBatch batch) {
 		Sprite s = Textures.FIRINGFORCEPOWERUPBOX;
 		positionSprite(s);
 		s.draw(batch);
 	}
 
 	private class PowerUpRemover implements Runnable {
 
 		private final Player player;
 
 		private PowerUpRemover(Player player) {
 			this.player = player;
 		}
 
 		@Override
 		public void run() {
 			if (player == null || player.getSpaceShip() == null) return;
 			player.getSpaceShip().setLaserDamage(Laser.DEFAULT_DAMAGE);
 		}
 	}
 }
