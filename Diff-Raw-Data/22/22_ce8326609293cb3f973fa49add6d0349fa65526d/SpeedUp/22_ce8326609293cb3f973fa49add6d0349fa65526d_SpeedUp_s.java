 package com.secondhand.model.powerup;
 
 import com.secondhand.model.entity.GameWorld;
 import com.secondhand.model.entity.Player;
 import com.secondhand.model.physics.Vector2;
 import com.secondhand.model.resource.PowerUpType;
 
 public class SpeedUp extends PowerUp {
 
 	private final static float DURATION = 10;
	private int factor = 3;
 	
 	public SpeedUp(final Vector2 position, 
 			final GameWorld level) {
 		super(position, PowerUpType.SPEED_UP, level, DURATION);
 		
 	}

	public int getFactor() {
		return factor;
	}
	
	public void setFactor(final int newFactor) {
		factor = newFactor;
	}
 	
 	@Override
 	public void activateEffect(final Player player) {
		// TODO: fix this one. 
		// max speed is no longer used in move, so we need to fix this.
		/*
		player.setMaxSpeed(player.getMaxSpeed()*factor);*/
 	}
 	
 	@Override
 	public void deactivateEffect(final Player player) {
 		
 		final boolean hasAnother = super.hasAnother(player);
 		
		/*if(!hasAnother)
			player.setMaxSpeed(player.getMaxSpeed()/factor);	*/
 	}
 	
 
 	public float getR() {return 0f;}
 	public float getG() {return 0f;}
 	public float getB() {return 1f;}
 }
