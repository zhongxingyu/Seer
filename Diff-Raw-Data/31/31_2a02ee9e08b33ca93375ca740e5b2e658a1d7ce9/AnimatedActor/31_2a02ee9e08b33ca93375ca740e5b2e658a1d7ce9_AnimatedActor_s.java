 package uk.co.jwlawson.aniguffins;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.badlogic.gdx.graphics.g2d.Animation;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 
 public class AnimatedActor extends DrawableActor {
 
 	private TextureRegion defaultTexture;
 	private Animation mAnimation;
 	private float elapsedTime;
 	private boolean looping;
 	private boolean running;
 
 	private List<AnimationDoneListener> aniDoneListeners;
 
 	public AnimatedActor(TextureRegion tex) {
 		super(tex);
 		defaultTexture = tex;
 		aniDoneListeners = new ArrayList<AnimatedActor.AnimationDoneListener>(1);
 	}
 
 	public void setDefaultTexture(TextureRegion tex) {
 		defaultTexture = tex;
 	}
 
 	/** Cancels any previous animation and activate the given animation */
 	public void activateAnimation(Animation ani, boolean looping) {
 		mAnimation = ani;
 		elapsedTime = 0;
 		this.looping = looping;
 		this.running = true;
 	}
 
 	/**
 	 * Stops the animation as it is. The texture region is not reset or changed
 	 * at all.
 	 */
 	public void forceStopAnimation() {
 		this.running = false;
 		mAnimation = null;
 	}
 
 	/** Will stop a looping animation when it reaches the end of its cycle. */
 	public void stopAnimation() {
 		looping = false;
 	}
 
 	public void addAnimationDoneListener(AnimationDoneListener listener) {
 		aniDoneListeners.add(listener);
 	}
 
 	@Override
 	public void act(float delta) {
 		super.act(delta);
 		if (running) {
 			elapsedTime += delta;
 			setTextureRegion(mAnimation.getKeyFrame(elapsedTime, looping));
			if (mAnimation.isAnimationFinished(elapsedTime)) {
 				animationDone();
 			}
 		}
 	}
 
 	private void animationDone() {
 		setTextureRegion(defaultTexture);
 		for (AnimationDoneListener listener : aniDoneListeners) {
 			listener.animationFinished();
 		}
 	}
 
 	/**
 	 * Helper method to create an animation from a texture region. The region is split according to the tile size and an animation created with the
 	 * specified frame duration.
 	 */
 	protected static Animation getAnimation(TextureRegion tex, float frameDuration, int tileSize) {
 		TextureRegion[][] arr = tex.split(tileSize, tileSize);
 		TextureRegion[] frames = squash2dArray(arr);
 		Animation ani = new Animation(frameDuration, frames);
 		return ani;
 	}
 
 	/**
 	 * Helper method to transfer a 2D array of textures from {@link TextureRegion#split} to a one dimensional array for the animation.
 	 */
 	protected static TextureRegion[] squash2dArray(TextureRegion[][] arr) {
 		TextureRegion[] frames = new TextureRegion[arr.length * arr[0].length];
 		int index = 0;
 		for (int i = 0; i < arr.length; i++) {
 			for (int j = 0; j < arr[0].length; j++) {
 				frames[index++] = arr[i][j];
 			}
 		}
 		return frames;
 	}
 
 	public interface AnimationDoneListener {
 
 		public void animationFinished();
 	}
 }
