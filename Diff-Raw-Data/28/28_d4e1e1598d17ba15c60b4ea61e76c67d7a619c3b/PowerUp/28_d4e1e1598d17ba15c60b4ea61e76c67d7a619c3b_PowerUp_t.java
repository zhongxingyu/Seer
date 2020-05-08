 package com.secondhand.model.powerup;
 
 import com.secondhand.model.entity.IPowerUp;
 import com.secondhand.model.entity.Player;
 import com.secondhand.model.entity.RectangleEntity;
 import com.secondhand.model.physics.Vector2;
 import com.secondhand.model.resource.PowerUpType;
 import com.secondhand.model.resource.SoundType;
 
 public abstract class PowerUp extends RectangleEntity implements IPowerUp {
 
 	public final static int WIDTH = 64;
 	public final static int HEIGHT = 64;
	
	private final static float SCORE_WORTH = 0;
	
 	protected float duration;
 	private final PowerUpType powerUpType;
 
 	public PowerUp(final Vector2 position, final PowerUpType powerUpType,
 			final float duration) {
 		super(position, WIDTH, HEIGHT, true);
 		this.duration = duration;
 		this.powerUpType = powerUpType;
 	}
 
 	@Override
 	public PowerUpType getPowerUpType() {
 		return this.powerUpType;
 	}
 
 	@Override
 	public float getDuration() {
 		return duration;
 	}
 
 	@Override
 	public void deactivateEffect(final Player player, final boolean hasAnother) {
 	}
 
 	@Override
 	protected float getScoreWorth() {
		return SCORE_WORTH;
 	}
 
 	@Override
 	public boolean hasText() {
 		return getText() != null;
 	}
 
 	@Override
 	public String getText() {
 		return null;
 	}
 
 	// how to color the player when the powerup is applied.
 	// red, green, blue values, from 0 to 1
 	// you can ignore these values for powerups with a duration of 0.
 	// I know that this is technically part of the view, but then we'd have to
 	// create a
 	// separate view class for every powerup.
 	@Override
 	public float getR() {
 		return Player.DEFAULT_COLOR_VALUE;
 	}
 
 	@Override
 	public float getG() {
 		return Player.DEFAULT_COLOR_VALUE;
 	}
 
 	@Override
 	public float getB() {
 		return Player.DEFAULT_COLOR_VALUE;
 	}
 
 	@Override
 	public void activatePowerUp(final Player player, final boolean hasBlackColor) {
 		this.activateEffect(player);
 
 		if (!hasBlackColor)
 			changePlayerColor(player);
 
 		player.playSound(SoundType.POWERUP_SOUND);
 	}
 
 	@Override
 	public void resetPlayerColor(final Player player) {
		player.setRGB(new float[] {
				Player.DEFAULT_COLOR_VALUE,
				Player.DEFAULT_COLOR_VALUE,
				Player.DEFAULT_COLOR_VALUE });
 	}
 
 	private void changePlayerColor(final Player player) {
		player.setRGB(new float[] {
				this.getR(),
				this.getG(),
				this.getB() });
 	}
 
 }
