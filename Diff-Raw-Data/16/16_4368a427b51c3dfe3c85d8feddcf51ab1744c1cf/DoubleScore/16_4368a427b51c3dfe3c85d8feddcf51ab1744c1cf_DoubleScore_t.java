 package com.secondhand.model.powerup;
 
 import com.badlogic.gdx.math.Vector2;
 import com.secondhand.model.GameWorld;
 import com.secondhand.model.Player;
 import com.secondhand.model.resource.PowerUpType;
 
 // TOOD: this does not yet work for the ScoreUp powerup, fix this.
 public class DoubleScore extends PowerUp {
 	
 	private final static float DURATION = 4;
 	
 	private final static float MULT = 2;
 	
 	public DoubleScore(final Vector2 position,
 			final GameWorld level) {
 		super(position, PowerUpType.DOUBLE_SCORE, level, DURATION);
 	}
 
 	@Override
 	public void activateEffect(final Player player) {
 		player.getCircle().setColor(1f, 1f, 0);
 		player.setScoreMultiplier(2);
 	}
 	
 	@Override
 	public void deactivateEffect(final Player player) {
		final boolean hasAnother = super.hasAnother(player);
 		
 		if(!hasAnother)
 			super.deactivateEffect(player);
 		
 		player.setScoreMultiplier(hasAnother ? MULT : 1);
 		
 		player.setIsEdible(!hasAnother);
 	}
 }
