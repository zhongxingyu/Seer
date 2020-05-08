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
 	
 	protected float duration;
 	private final PowerUpType powerUpType;
 	protected final Player player;
 	
 	private final float[] RGB = new float[3];	
 
 	public PowerUp (final Vector2 position, final PowerUpType powerUpType, final float duration, final Player player) {
 		super(position, WIDTH, HEIGHT,true);
 		this.duration = duration;
 		this.powerUpType = powerUpType;
 		this.player = player;
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
 	public void deactivateEffect(boolean hasAnother) {
 	}
 	
 	@Override
 	public float getScoreWorth() {
 		throw new IllegalStateException("cannot score points for eating powerup");
 	}
 	
 	@Override
 	public boolean hasText(){
 		return getText() != null;
 	}
 	
 	@Override
 	public String getText(){
 		return null;
 	}
 	
 	// how to color the player when the powerup is applied.
 	// red, green, blue values, from 0 to 1
 	// you can ignore these values for powerups with a duration of 0. 
 	// I know that this is technically part of the view, but then we'd have to create a 
 	// separate view class for every fucking powerup, and I don't really have the energy to do that. 
 	@Override
 	public float getR() {return Player.DEFAULT_COLOR_VALUE;}
 	
 	@Override
 	public float getG() {return Player.DEFAULT_COLOR_VALUE;}
 	
 	@Override
 	public float getB() {return Player.DEFAULT_COLOR_VALUE;}
 	
 	@Override
 	public void eaten(){
 		super.wasEaten();
 	}
 
 	@Override
 	public void activatePowerUp() {
 		this.activateEffect(player);
 		changePlayerColor();
 		player.setSoundType(SoundType.POWERUP_SOUND);
 		eaten();
 	}
 
	@Override
 	public void resetPlayerColor() {
 		RGB[0] = Player.DEFAULT_COLOR_VALUE;
 		RGB[1] = Player.DEFAULT_COLOR_VALUE;
 		RGB[2] = Player.DEFAULT_COLOR_VALUE;
 		player.setRGB(RGB);
 	}
 
 	
 	public void changePlayerColor(){
 		RGB[0] = this.getR();
 		RGB[1] = this.getG();
 		RGB[2] = this.getB();
 		player.setRGB(RGB);		
 	}
 	
 }
