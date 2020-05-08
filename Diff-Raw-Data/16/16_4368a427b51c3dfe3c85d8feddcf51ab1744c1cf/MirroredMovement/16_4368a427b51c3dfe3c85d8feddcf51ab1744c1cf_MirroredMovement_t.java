 package com.secondhand.model.powerup;
 
 import com.badlogic.gdx.math.Vector2;
 import com.secondhand.debug.MyDebug;
 import com.secondhand.model.GameWorld;
 import com.secondhand.model.Player;
 import com.secondhand.model.resource.PowerUpType;
 
 public class MirroredMovement extends PowerUp {
 
 	private final static float DURATION = 10;
 	
 	public MirroredMovement(final Vector2 position,
 			final GameWorld level) {
 		super(position, PowerUpType.MIRRORED_MOVEMENT, level, DURATION);
 
 	}
 
 	@Override
 	public void activateEffect(final Player player) {
 		MyDebug.d("applying mirrored movement");
 		player.getCircle().setColor(1f, 0, 1f);
 		player.setMirroredMovement(true);
 	}
 	
 	@Override
 	public void deactivateEffect(final Player player) {
		final boolean hasAnother = super.hasAnother(player);
 		
 		if(!hasAnother)
 			super.deactivateEffect(player);
 		
 		player.setMirroredMovement(!hasAnother);
 	}
 }
