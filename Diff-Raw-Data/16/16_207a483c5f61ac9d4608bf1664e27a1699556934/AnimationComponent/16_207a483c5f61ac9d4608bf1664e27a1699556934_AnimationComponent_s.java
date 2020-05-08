 package com.detourgames.raw;
 
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 
 public class AnimationComponent {
 
 	private boolean animating = true;
 	private Animation mAnimation;
 	private int mCurrentFrameNumber = 0;
 	private float nextFrame = 0;
 	private float FRAME_DURATION = (float) GameLoop.TICKS_PER_SECOND
 			/ (float) Animation.FPS;
 	private int tick = 0;
 
	private int index = 0;
	private int duration = 0;

 	public AnimationComponent() {
 		// mFrame = Animation.RUNNING_INDEX;
 		nextFrame = FRAME_DURATION;
 	}
 
 	public void update(float deltaTime) {
 
 		if (animating) {
 			if (tick > GameLoop.TICKS_PER_SECOND - 1) {
 				tick = 0;
 				nextFrame = FRAME_DURATION;
 			}
 
 			if (tick > nextFrame) {
 				nextFrame += FRAME_DURATION;
				if (mCurrentFrameNumber < index + duration - 1) {
 					mCurrentFrameNumber++;
 				} else {
					mCurrentFrameNumber = index;
 				}
 			}
 
 			tick++;
 		}
 		
 	}
 
 	public TextureRegion getCurrentFrame() {
 		return mAnimation.getFrames()[mCurrentFrameNumber];
 	}
 
 	public void setFrame(int frame) {
 		mCurrentFrameNumber = frame;
 	}
 	
 	public void setAnimation(Animation animation){
 		mAnimation = animation;
 	}
 
 	public void pause() {
 		animating = false;
 	}
 
 	public void resume() {
 		animating = true;
 	}
 
 }
