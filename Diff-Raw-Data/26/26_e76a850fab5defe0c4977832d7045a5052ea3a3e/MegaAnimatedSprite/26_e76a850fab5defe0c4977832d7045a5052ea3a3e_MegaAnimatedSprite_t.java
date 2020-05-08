 package com.codefest2013.game;
 
 import org.andengine.entity.Entity;
 import org.andengine.entity.sprite.Sprite;
 
 import android.util.Log;
 import android.util.SparseArray;
 
 /**
  * Represents a sprite that supports displaying several large textures for animation
  * @author Sebazzz
  *
  */
 public class MegaAnimatedSprite extends Entity {
 	private final SparseArray<Sprite> textures;
 	
 	private float[] timings;
 	private float time;
 	private int textureIndex;
 	private int index;
 	
 	private boolean isAnimating;
 	private boolean runToAnimationEnd;
 	private Runnable onAnimationEnded;
 	
 	private Sprite currentVisibleSprite;
 	
 	/**
 	 * Ctor
 	 */
 	public MegaAnimatedSprite() {
 		this(5);
 	}
 	
 	/**
 	 * Initializes the MegaAnimatedSprite using the specified expected frame count
 	 * 
 	 * @param expectedFrameCount
 	 */
 	public MegaAnimatedSprite(int expectedFrameCount) {
 		this.textures = new SparseArray<Sprite>(expectedFrameCount);
 	}
 	
 	public void attachTexture(Sprite texture) {
 		this.textures.put(this.textures.size(), texture);
 		
 		if (this.textures.size() != 1) {
 			texture.setVisible(false);
 		} else {
 			currentVisibleSprite = texture;
 		}
 		
 		this.attachChild(texture);
 	}
 	
 	/**
 	 * Gets if the sprite is currently in animating mode
 	 * 
 	 * @return
 	 */
 	public boolean isAnimating() {
 		return this.isAnimating;
 	}
 	
 	/**
 	 * Starts the animation. Each frame will have the specified delay.
 	 * 
 	 * @param frameTime
 	 */
 	public void animate(float frameTime) {
 		this.animate(new float[] { frameTime });
 	}
 	
 	/**
 	 * Starts the animation using the specified frame times
 	 * @param frameTimes
 	 */
 	public void animate(float[] frameTimes) {
 		if (this.textures.size() == 0) {
 			Log.w("engine", "No sprites - not animating");
 			return;
 		}
 		
 		this.timings = frameTimes;
 		this.index = 0;
 		this.textureIndex = 0;
 		this.isAnimating = true;
 		this.runToAnimationEnd = false;
 		this.time = 0;
 		
 		if (this.textures.size() > 1) {
 			this.index = this.textures.size() - 1;
 		}
 	}
 	
 	/**
 	 * Starts the animation
 	 * 
 	 * Note: the animation should be preconfigured by preconfigureAnimation
 	 */
 	public void animate() {
 		if (this.timings != null) {
 			this.animate(this.timings);
 		}
 	}
 	
 	/**
 	 * Preconfigures the frame times for the animation
 	 * 
 	 * @param frameTimes
 	 */
 	public void preconfigureAnimation(float[] frameTimes) {
 		this.timings = frameTimes;
 	}
 	
 	/**
 	 * Stops the animation directly
 	 */
 	public void stopAnimation() {
 		this.isAnimating = false;
 	}
 	
 	/**
 	 * Stops the animation directly
 	 */
 	public void stopAnimation(int index) {
 		setFrameIndex(index);
 		stopAnimation();
 	}
 	
 	/**
 	 * Stops the animation at the end and then invokes the specified optional callback
 	 * 
 	 * @param callback
 	 */
 	public void stopAnimationAtEnd(Runnable callback) {
 		if (!this.isAnimating) {
             if (callback != null)
 			    callback.run();
 			return;
 		}
 		
 		this.runToAnimationEnd = true;
 		this.onAnimationEnded = callback;
 	}
 
 	@Override
 	protected void onManagedUpdate(float pSecondsElapsed) {
 		// update animation
 		if (this.isAnimating) {
 			int tmpIndex = (this.index + 1) % this.timings.length;
 			this.time += pSecondsElapsed;
 			float targetTime = timings[tmpIndex];
 			
 			// check if we need to update the frames
 			if (this.time < targetTime) {
 				return;
 			}
 
 			this.time = 0;
 			this.index = tmpIndex;
 			int tmpTextureIndex = (this.textureIndex + 1) % this.textures.size();
 			
 			// check if we're at end and should respond
 			if (this.runToAnimationEnd && tmpTextureIndex == 0 && this.textureIndex > tmpTextureIndex) {
 				if (this.onAnimationEnded != null) {
 					this.onAnimationEnded.run();
 					this.onAnimationEnded = null;
 				}
 				
 				this.isAnimating = false;
 				return;
 			}
 			
 			setFrameIndex(tmpTextureIndex);
 			
 			this.textureIndex = tmpTextureIndex;
 		}
 		
 		super.onManagedUpdate(pSecondsElapsed);
 	}
 	
 	public boolean contains(float pX, float pY) {
 		if (this.currentVisibleSprite != null) {
 			return this.currentVisibleSprite.contains(pX, pY);
 		}
 
 		return false;
 	}
 	
 	
 	public float getHeight() {
 		if (this.currentVisibleSprite == null) {
 			return 0;
 		}
 		
 		return this.currentVisibleSprite.getHeight();
 	}
 	
 	public float getWidth() {
 		if (this.currentVisibleSprite == null) {
 			return 0;
 		}
 		
 		return this.currentVisibleSprite.getWidth();
 	}
 	
 	/**
 	 * Cleans up all the resources used by this mega texture. Make sure to call this method as soon as possible.
 	 */
 	public void unload() {
 		if (this.textures.size() == 0) {
 			return;
 		}
 		
 		this.isAnimating = false;
 		
 		for (int idx = 0;idx<this.textures.size();idx++) {
 			Sprite s = this.textures.get(idx);
 			s.getTextureRegion().getTexture().unload();
 			s.detachChildren();
 		}
 		
 		this.detachChildren();
 		this.currentVisibleSprite = null;
 		this.textures.clear();
 	}
 	
 	/**
 	 * Same as calling unload
 	 */
 	@Override
 	public void dispose() {
 		this.unload();
 
 		super.dispose();
 	}
 
 	public int getFrameCount() {
 		return this.textures.size();
 	}
 	
 	private void setFrameIndex(int index)
 	{
 		this.textures.get(this.textureIndex).setVisible(false);
 		this.textures.get(index).setVisible(true);
		currentVisibleSprite = this.textures.get(index);
 	}
 }
