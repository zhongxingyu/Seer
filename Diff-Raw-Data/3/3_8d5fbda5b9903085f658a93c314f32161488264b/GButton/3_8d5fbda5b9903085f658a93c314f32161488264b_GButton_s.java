 package jgame;
 
 import java.awt.Graphics2D;
 import java.awt.Shape;
 import java.awt.geom.Rectangle2D;
 import java.util.HashMap;
 import java.util.Map;
 
 import jgame.listener.ButtonListener;
 
 /**
  * A simple button that uses {@link GSprite}s, animated or static, for each
  * button state defined in {@link jgame.ButtonState ButtonListener.State}.
  * 
  * @author William Chargin
  * 
  */
 public class GButton extends GObject implements GAnimatable {
 
 	/**
 	 * The map of sprites for the various button states.
 	 */
 	private Map<ButtonState, GSprite> sprites;
 
 	/**
 	 * The current state of the button.
 	 */
 	private ButtonState currentState = ButtonState.NONE;
 
 	/**
 	 * The {@link ButtonListener} used by this button.
 	 */
 	private ButtonListener listener;
 
 	/**
 	 * Creates the button with the default settings and no images.
 	 */
 	public GButton() {
 		super();
 
 		// Initialize the map.
 		sprites = new HashMap<ButtonState, GSprite>();
 
 		// Create and add the listener.
 		addListener(listener = new ButtonListener() {
 
 			@Override
 			public void mouseClicked(Context context) {
 				currentState = ButtonState.NONE;
 			}
 
 			@Override
 			public void mouseDown(Context context) {
 				currentState = ButtonState.PRESSED;
 			}
 
 			@Override
 			public void mouseOut(Context context) {
 				currentState = ButtonState.NONE;
 			}
 
 			@Override
 			public void mouseOver(Context context) {
 				currentState = ButtonState.HOVERED;
 			}
 
 		});
 	}
 
 	@Override
 	public Rectangle2D getBoundingBox() {
 		// Delegate to the current sprite.
 		GSprite sprite = getSpriteForState(currentState);
 
 		// If it's non-null, ask it.
 		if (sprite != null) {
 			// Return the answer.
 			return sprite.getBoundingBox();
 		}
 
 		// Otherwise we'll take super.
 		return super.getBoundingBox();
 	}
 
 	@Override
 	public Shape getBoundingShape() {
 		// Delegate to the current sprite.
 		GSprite sprite = getSpriteForState(currentState);
 
 		// If it's non-null, ask it.
 		if (sprite != null) {
 			// Return the answer.
			return new Rectangle2D.Double(0, 0, sprite.getWidth(),
					sprite.getHeight());
 		}
 
 		// Otherwise we'll take super.
 		return super.getBoundingShape();
 	}
 
 	/**
 	 * Gets the button sprite for the specified state. If no sprite for the
 	 * state is set, the next most general state will be tried. For example, if
 	 * the sprite for {@link ButtonState#HOVERED} is requested but none has been
 	 * set, the sprite for {@link ButtonState#NONE} will be returned.
 	 * 
 	 * @param buttonState
 	 *            the state to check
 	 * @return the closest matching sprite
 	 * @throws IllegalArgumentException
 	 *             if {@code state} is {@code null}
 	 */
 	private GSprite getSpriteForState(ButtonState buttonState)
 			throws IllegalArgumentException {
 		// Make sure the state is non-null.
 		if (buttonState == null) {
 			// The state is null.
 			throw new IllegalArgumentException("state == null");
 		}
 
 		// First, see what we've got.
 		GSprite i = sprites.get(buttonState);
 
 		// If it's non-null, use that.
 		if (i != null) {
 			// Good.
 			return i;
 		}
 
 		// Otherwise, can we extrapolate?
 		switch (buttonState) {
 		case HOVERED:
 			// The button is hovered. If there's no GSprite set, we'll use the
 			// default GSprite.
 			return getSpriteForState(ButtonState.NONE);
 
 		case PRESSED:
 			// The button is pressed. If there's no GSprite set, we'll use the
 			// hovered GSprite.
 			return getSpriteForState(ButtonState.HOVERED);
 
 		case NONE:
 			// Well this is the end of the line. If there's no GSprite here,
 			// there's nothing.
 			return null;
 
 		default:
 			// That shouldn't happen, but if it does, we don't know what to do.
 			return null;
 		}
 
 	}
 
 	/**
 	 * Delegate method for {@link ButtonListener#getValidButtonMask()}.
 	 * 
 	 * @return the valid button mask
 	 */
 	public int getValidButtonMask() {
 		return listener.getValidButtonMask();
 	}
 
 	@Override
 	public boolean isPlaying() {
 		// Delegate to the current sprite.
 		GSprite sprite = getSpriteForState(currentState);
 
 		// If it's non-null, ask it.
 		if (sprite != null) {
 			// Return the answer.
 			return sprite.isPlaying();
 		}
 
 		// Otherwise we'll assume not playing.
 		return false;
 	}
 
 	@Override
 	public void nextFrame() {
 		// Tell all sprites.
 		for (GSprite sprite : sprites.values()) {
 			// Is it non-null?
 			if (sprite != null) {
 				// Go!
 				sprite.nextFrame();
 			}
 		}
 	}
 
 	@Override
 	public void paint(Graphics2D g) {
 		// Get the current sprite.
 		GSprite sprite = getSpriteForState(currentState);
 
 		// If it's non-null, paint it.
 		if (sprite != null) {
 			// We can paint.
 			sprite.paint(g);
 		}
 
 		// Paint children.
 		super.paint(g);
 	}
 
 	@Override
 	public void preparePaint(Graphics2D g) {
 		super.preparePaint(g);
 		goodImageTransforms(g);
 	}
 
 	@Override
 	public void previousFrame() {
 		// Tell all sprites.
 		for (GSprite sprite : sprites.values()) {
 			// Is it non-null?
 			if (sprite != null) {
 				// Go!
 				sprite.previousFrame();
 			}
 		}
 	}
 
 	@Override
 	public void setFrameNumber(int frameNumber)
 			throws IndexOutOfBoundsException {
 		// Tell all sprites.
 		for (GSprite sprite : sprites.values()) {
 			// Is it non-null?
 			if (sprite != null) {
 				// Go!
 				sprite.setFrameNumber(frameNumber);
 			}
 		}
 	}
 
 	@Override
 	public void setHeight(double h) throws IllegalArgumentException {
 		// Set the height.
 		super.setHeight(h);
 
 		// Also set the height for all sprites.
 		for (GSprite sprite : sprites.values()) {
 			sprite.setHeight(h);
 		}
 	}
 
 	@Override
 	public void setPlaying(boolean playing) {
 		// Tell all sprites.
 		for (GSprite sprite : sprites.values()) {
 			// Is it non-null?
 			if (sprite != null) {
 				// Go!
 				sprite.setPlaying(playing);
 			}
 		}
 	}
 
 	@Override
 	public void setSize(double w, double h) throws IllegalArgumentException {
 		// Set the size.
 		super.setSize(w, h);
 
 		// Also set the size for all sprites.
 		for (GSprite sprite : sprites.values()) {
 			sprite.setSize(w, h);
 		}
 	}
 
 	/**
 	 * Sets the sprite for the specified state of the button.
 	 * 
 	 * @param buttonState
 	 *            the state for which to change the sprite; may not be
 	 *            {@code null}
 	 * @param sprite
 	 *            the new sprite; may be {@code null}
 	 * @throws IllegalArgumentException
 	 *             if {@code state} is {@code null}
 	 */
 	public void setStateSprite(ButtonState buttonState, GSprite sprite)
 			throws IllegalArgumentException {
 		// Make sure the state is non-null.
 		if (buttonState == null) {
 			// The state is null.
 			throw new IllegalArgumentException("state == null");
 		}
 
 		// Put the sprite to the map.
 		sprites.put(buttonState, sprite);
 	}
 
 	/**
 	 * Delegate method for {@link ButtonListener#setValidButtonMask(int)}.
 	 * 
 	 * @param validButtonMask
 	 *            the new valid button mask
 	 */
 	public void setValidButtonMask(int validButtonMask) {
 		listener.setValidButtonMask(validButtonMask);
 	}
 
 	@Override
 	public void setWidth(double w) throws IllegalArgumentException {
 		// Set the width.
 		super.setWidth(w);
 
 		// Also set the width for all sprites.
 		for (GSprite sprite : sprites.values()) {
 			sprite.setWidth(w);
 		}
 	}
 
 }
