 package com.efzgames.ninjaacademy.elements;
 
 import com.efzgames.framework.gl.Animation;
 import com.efzgames.framework.gl.SpriteBatcher;
 import com.efzgames.framework.gl.Texture;
 import com.efzgames.framework.gl.TextureRegion;
 import com.efzgames.framework.impl.GLGame;
 import com.efzgames.framework.math.Vector2;
 import com.efzgames.ninjaacademy.screens.GameScreen;
 
 public class StraightLineScalingComponent extends StraightLineMovementComponent {
 
 	private float remainingScaleAmount;
 	private float scaleVelocity;
 
 	public float scale;
 
 	public StraightLineScalingComponent(GLGame glGame, GameScreen gameScreen,
 			Animation animation) {
 		super(glGame, gameScreen, animation);
 	}
 
 	public StraightLineScalingComponent(GLGame glGame, GameScreen gameScreen,
 			Texture texture, TextureRegion textureRegion) {
 		super(glGame, gameScreen, texture, textureRegion);
 	}
 	
 	@Override
 	public void update(float deltaTime) {		
 		 super.update(deltaTime);
 		
 		  float elapsedSeconds = (float)deltaTime;
 
           float scaleChange = scaleVelocity * elapsedSeconds;
           scale += scaleChange;
           remainingScaleAmount -= Math.abs(scaleChange);
 
           // Check whether scaling is complete
           if (remainingScaleAmount <= 0)
           {
               // The scale may have changed more than we intended, so change it back appropriately
              scale += remainingScaleAmount * (scaleChange< 0 ? -1 : (scaleChange > 0 ? 1 : 0));                
               scaleVelocity = 0;
           }
 	}
 
 	@Override
 	public void present(float deltaTime, SpriteBatcher batcher) {
 		batcher.beginBatch(texture);
 		animation.present(deltaTime, batcher, position, 0 , visualCenter, scale);	
 		batcher.endBatch();
 	}
 
 	public void moveAndScale(float time, Vector2 initialPosition,
 			Vector2 destinationPosition, float initialScale,
 			float destinationScale) {
 	
 		scale = initialScale;
 		float scaleDelta = destinationScale - initialScale;
 		remainingScaleAmount = Math.abs(scaleDelta);
 		scaleVelocity = scaleDelta / (float) time;
 
 		moveWithTime(time, initialPosition, destinationPosition);
 	}
 
 }
