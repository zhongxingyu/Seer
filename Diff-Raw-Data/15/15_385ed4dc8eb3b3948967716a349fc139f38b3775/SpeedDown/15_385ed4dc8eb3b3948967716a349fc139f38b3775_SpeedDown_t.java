 package com.secondhand.model.powerup;
 
 import com.secondhand.model.entity.Player;
 import com.secondhand.model.physics.Vector2;
 import com.secondhand.model.resource.PowerUpType;
 
 public class SpeedDown extends PowerUp {
 
 	private final static float DURATION = 10;
 	private final static float FACTOR = 0.5f;
 	
 	public SpeedDown(final Vector2 position, Player player) {
		super(position, PowerUpType.SPEED_DOWN, DURATION, player);
 		
 	}
 	
 	@Override
 	public void activateEffect(final Player player) {
 		player.setSpeedMultiplier(FACTOR);
 	}
 	
 	@Override
 	public void deactivateEffect(final Player player) {
 		
 		final boolean hasAnother = super.hasAnother(player);
 		
 		if(!hasAnother)
 			player.setSpeedMultiplier(player.getSpeedMultiplier()/FACTOR);
 	}
 	
 
 	public float getR() {return 0f;}
 	public float getG() {return 1f;}
 	public float getB() {return 1f;}
 
 	// it's annoying, so it should be common so that it's even more annoying :)
 	public static int getFrequency() { return 7; }
 
 }
