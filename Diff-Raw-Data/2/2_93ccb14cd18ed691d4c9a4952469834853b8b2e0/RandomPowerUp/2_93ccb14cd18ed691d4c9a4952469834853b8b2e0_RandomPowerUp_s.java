 package com.secondhand.model.powerup;
 
 import java.util.Random;
 
 import com.secondhand.model.entity.IGameWorld;
 import com.secondhand.model.entity.Player;
 import com.secondhand.model.physics.Vector2;
 import com.secondhand.model.resource.PowerUpType;
 
 public class RandomPowerUp extends PowerUp {
 
 	private final PowerUp randomPowerUp;
 
 	private static final int NUM_POWER_UPS = 10;
 
 	
 	public RandomPowerUp(final Vector2 position,
 			final  IGameWorld gameWorld) {
 		super(position, PowerUpType.RANDOM_POWER_UP, 0);
 
 		final Random rng = new Random();
 		final int rand = rng.nextInt(NUM_POWER_UPS-1);
 		
 		
 		if(rand == 0) {
 			randomPowerUp =  new EatObstacle(position);
 		} else if(rand == 1) {
 			randomPowerUp =  new ExtraLife(position);
 		}else if(rand == 2) {
 			randomPowerUp =  new RandomTeleport(position, gameWorld);
 		}else if(rand == 3) {
 			randomPowerUp =  new ScoreUp(position);
 		}else if(rand == 4) {
 			randomPowerUp =  new Shield(position);
 		}else if(rand == 5) {
 			randomPowerUp =  new SpeedUp(position);
 		} else if(rand == 6) {
 			randomPowerUp =  new DoubleScore(position);
 		} else if(rand == 7) {
 			randomPowerUp =  new MirroredMovement(position);
 		} else if(rand == 8) {
 			randomPowerUp =  new SpeedDown(position);
 		}else
 			randomPowerUp = null;
 		
 		this.duration = randomPowerUp.getDuration();
 	}
 
 	@Override
 	public void activateEffect(final Player player) {
 		this.randomPowerUp.activateEffect(player);
 	}
 	
 	@Override
 	public void deactivateEffect(final Player player, final boolean hasAnother) {
 		this.randomPowerUp.deactivateEffect(player, hasAnother);
 	}
 
 	@Override
 	public String getText(){	
 		return this.randomPowerUp.getText();
 	}
 	
 	public float getR() {return this.randomPowerUp.getR();}
 	public float getG() {return this.randomPowerUp.getG();}
 	public float getB() {return this.randomPowerUp.getB();}
 
 
	public static int getFrequency() { return 60000000; }
 }
