 package com.secondhand.model.powerup;
 
 import com.badlogic.gdx.math.Vector2;
 import com.secondhand.model.Level;
 import com.secondhand.model.Player;
 import com.secondhand.resource.PowerUpType;
 
 public class Shield extends PowerUp {
 	
 	private final static float DURATION = 4;
 	
 	public Shield(final Vector2 position,
 			final Level level) {
 		super(position, PowerUpType.SHIELD, level, DURATION);
 	}
 
 	@Override
 	public void activateEffect(final Player player) {
 		player.getCircle().setColor(0, 1f, 0);
 		player.setIsEdible(false);
 	}
 	
 	@Override
 	public void deactivateEffect(final Player player) {
 		super.deactivateEffect(player);
 		boolean hasAnotherShield = false;
 		for (final PowerUp powerUp : player.getPowerUps()) {
 			if (powerUp.getClass() == Shield.class)
 				hasAnotherShield = true;
 		}
		player.setIsEdible(hasAnotherShield);
 	}
 }
