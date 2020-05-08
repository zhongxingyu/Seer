 package com.madthrax.ridiculousRPG.animations;
 
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Rectangle;
 import com.madthrax.ridiculousRPG.GameBase;
 import com.madthrax.ridiculousRPG.TextureRegionLoader.TextureRegionRef;
 
 public class BoundedImage {
 	private TextureRegionRef image;
 	private Rectangle bounds;
 	private boolean scroll;
 	private Rectangle scrollReference;
 
 	/**
 	 * Scales the image to fit into the bounds
 	 * 
 	 * @param image
 	 *            the image to draw
 	 * @param bounds
 	 *            the bounds for drawing the image
 	 */
 	public BoundedImage(TextureRegionRef image, Rectangle bounds) {
 		this(image, bounds, false, null);
 	}
 
 	/**
 	 * The image will not be scaled, but scrolls inside the bounds.<br>
 	 * Scrolling is performed when scrollReference.x or scrollReference.y
 	 * changes. The scroll speed/amount is computed relatively from
 	 * bounds.width/scrollReference.width and
 	 * bounds.height/scrollReference.height.
 	 * 
 	 * @param image
 	 *            the image to draw
 	 * @param bounds
 	 *            the bounds for drawing the image
 	 * @param scrollReference
 	 *            the bounds which are used to compute/perform scrolling.<br>
 	 *            If null GameBase.$().getPlane() will be used.
 	 */
 	public BoundedImage(TextureRegionRef image, Rectangle bounds,
 			Rectangle scrollReference) {
 		this(image, bounds, true, scrollReference);
 	}
 
 	private BoundedImage(TextureRegionRef image, Rectangle bounds,
 			boolean scroll, Rectangle scrollReference) {
 		this.image = image;
 		this.bounds = bounds;
 		this.scroll = scroll;
 		this.scrollReference = scrollReference;
 		if (scroll && scrollReference == null) {
 			// TODO: change Gamebase.screenWidth, Gamebase.planeWidth,
 			// Gamebase.originalWidth
 			// to Rectangles for easier usage.
			scrollReference = GameBase.$().getPlane();
 		}
 	}
 
 	public void draw(SpriteBatch spriteBatch) {
 		if (scroll) {
 			// TODO: scroll in bounds relative depending on scrollReference
 		} else {
 			spriteBatch.draw(image, bounds.x, bounds.y, bounds.width,
 					bounds.height);
 		}
 	}
 }
