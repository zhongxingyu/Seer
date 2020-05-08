 package com.glowman.spaceunit.game.mapObject.enemy.behaviour.core;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Circle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
 import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
 
 /** An on-screen joystick. The movement area of the joystick is circular, centered on the touchpad, and its size determined by the
  * smaller touchpad dimension.
  * <p>
  * The preferred size of the touchpad is determined by the background.
  * <p>
  * {@link ChangeEvent} is fired when the touchpad knob is moved. Cancelling the event will move the knob to where it was
  * previously.
  * @author Josh Street */
 public class TouchPad {
 	private TouchpadStyle style;
 	boolean touched;
 	private float deadzoneRadius;
 	private final Circle knobBounds = new Circle(0, 0, 0);
 	private final Circle touchBounds = new Circle(0, 0, 0);
 	private final Circle deadzoneBounds = new Circle(0, 0, 0);
 	private final Vector2 knobPosition = new Vector2();
 	private final Vector2 knobPercent = new Vector2();
 
 	private Vector2 position = new Vector2(0, 0);
 
 	/** @param deadzoneRadius The distance in pixels from the center of the touchpad required for the knob to be moved. */
 	public TouchPad (float deadzoneRadius, Skin skin) {
 		this(deadzoneRadius, skin.get(TouchpadStyle.class));
 	}
 
 	/** @param deadzoneRadius The distance in pixels from the center of the touchpad required for the knob to be moved. */
 	public TouchPad (float deadzoneRadius, Skin skin, String styleName) {
 		this(deadzoneRadius, skin.get(styleName, TouchpadStyle.class));
 	}
 
 	/** @param deadzoneRadius The distance in pixels from the center of the touchpad required for the knob to be moved. */
 	public TouchPad (float deadzoneRadius, TouchpadStyle style) {
 		if (deadzoneRadius < 0) throw new IllegalArgumentException("deadzoneRadius must be > 0");
 		this.deadzoneRadius = deadzoneRadius;
 
 
 		setStyle(style);
 
 		knobBounds.set(getWidth()/2, getHeight()/2, getWidth()/2);
 		touchBounds.set(getWidth()/2, getHeight()/2, getWidth());
		knobPosition.set(style.background.getMinWidth() / 2f, style.background.getMinHeight() / 2f);
 
		this.updatePosition();
 
 		//setWidth(getPrefWidth());
 		//setHeight(getPrefHeight());
 	}
 
 	public void setPosition(float x, float y) {
 		this.position.x = x;
 		this.position.y = y;
		this.updatePosition();
 	}
 
 	public boolean hit(float x, float y) {
 		return touchBounds.contains(x, y);
 	}
 
 	public void touchDown(float x, float y) {
 		if (touched) return;
 		touched = true;
 		x -= position.x;
 		y -= position.y;
 		calculatePositionAndValue(x, y, false);
 	}
 
 	public void touchUp(float x, float y) {
 		touched = false;
 		x -= position.x;
 		y -= position.y;
 		calculatePositionAndValue(x, y, true);
 	}
 
 	public void touchMove(float x, float y) {
 		x -= position.x;
 		y -= position.y;
 		calculatePositionAndValue(x, y, false);
 	}
 
 	private void calculatePositionAndValue (float x, float y, boolean isTouchUp) {
 		float centerX = knobBounds.x;
 		float centerY = knobBounds.y;
 		knobPosition.set(centerX, centerY);
 		knobPercent.set(0f, 0f);
 		if (!isTouchUp) {
 			if (!deadzoneBounds.contains(x, y)) {
 				knobPercent.set((x - centerX) / knobBounds.radius, (y - centerY) / knobBounds.radius);
 				float length = knobPercent.len();
 				if (length > 1) knobPercent.scl(1 / length);
 				if (knobBounds.contains(x, y)) {
 					knobPosition.set(x, y);
 				} else {
 					knobPosition.set(knobPercent).nor().scl(knobBounds.radius).add(knobBounds.x, knobBounds.y);
 				}
 			}
 		}
 //		if (oldPercentX != knobPercent.x || oldPercentY != knobPercent.y) {
 //			knobPercent.set(oldPercentX, oldPercentY);
 //			knobPosition.set(oldPositionX, oldPositionY);
 //		}
 	}
 
 	public void setStyle (TouchpadStyle style) {
 		if (style == null) throw new IllegalArgumentException("style cannot be null");
 		this.style = style;
 	}
 
 	/** Returns the touchpad's style. Modifying the returned style may not have an effect until {@link #setStyle(TouchpadStyle)} is
 	 * called. */
 	public TouchpadStyle getStyle () {
 		return style;
 	}
 
 //	public void layout () {
 //		// Recalc pad and deadzone bounds
 //		float halfWidth = getWidth() / 2;
 //		float halfHeight = getHeight() / 2;
 //		float radius = Math.min(halfWidth, halfHeight);
 //		touchBounds.set(halfWidth, halfHeight, radius);
 //		if (style.knob != null) radius -= Math.max(style.knob.getMinWidth(), style.knob.getMinHeight()) / 2;
 //		knobBounds.set(halfWidth, halfHeight, radius);
 //		deadzoneBounds.set(halfWidth, halfHeight, deadzoneRadius);
 //		// Recalc pad values and knob position
 //		knobPosition.set(halfWidth, halfHeight);
 //		knobPercent.set(0, 0);
 //	}
 //
 //
 	public void draw (SpriteBatch batch, float parentAlpha) {
 
 		Color color = batch.getColor();
 		float oldAlpha = color.a;
 		color.a = parentAlpha;
 		batch.setColor(color);
 
 		float x = position.x;
 		float y = position.y;
 		float w = style.background.getMinWidth();
 		float h = style.background.getMinHeight();
 
 		final Drawable bg = style.background;
 
 		bg.draw(batch, x, y, w, h);
 
 		final Drawable knob = style.knob;
 		x += knobPosition.x - knob.getMinWidth() / 2f;
 		y += knobPosition.y - knob.getMinHeight() / 2f;
 		knob.draw(batch, x, y, knob.getMinWidth(), knob.getMinHeight());
 
 		color.a = oldAlpha;
 		batch.setColor(color);
 	}
 
 	public boolean isTouched () {
 		return touched;
 	}
 
 	/** @param deadzoneRadius The distance in pixels from the center of the touchpad required for the knob to be moved. */
 	public void setDeadzone (float deadzoneRadius) {
 		if (deadzoneRadius < 0) throw new IllegalArgumentException("deadzoneRadius must be > 0");
 		this.deadzoneRadius = deadzoneRadius;
 	}
 
 	/** Returns the x-position of the knob relative to the center of the widget. The positive direction is right. */
 	public float getKnobX () {
 		return knobPosition.x;
 	}
 
 	/** Returns the y-position of the knob relative to the center of the widget. The positive direction is up. */
 	public float getKnobY () {
 		return knobPosition.y;
 	}
 
 	/** Returns the x-position of the knob as a percentage from the center of the touchpad to the edge of the circular movement
 	 * area. The positive direction is right. */
 	public float getKnobPercentX () {
 		return knobPercent.x;
 	}
 
 	/** Returns the y-position of the knob as a percentage from the center of the touchpad to the edge of the circular movement
 	 * area. The positive direction is up. */
 	public float getKnobPercentY () {
 		return knobPercent.y;
 	}
 
 	private float getWidth() { return style.background.getMinWidth(); }
 	private float getHeight() { return style.background.getMinHeight(); }
 
	private void updatePosition() {
	}

 	/** The style for a {@link TouchPad}.
 	 * @author Josh Street */
 	public static class TouchpadStyle {
 		/** Stretched in both directions. Optional. */
 		public Drawable background;
 
 		/** Optional. */
 		public Drawable knob;
 
 		public TouchpadStyle () {
 		}
 
 		public TouchpadStyle (Drawable background, Drawable knob) {
 			this.background = background;
 			this.knob = knob;
 		}
 
 		public TouchpadStyle (TouchpadStyle style) {
 			this.background = style.background;
 			this.knob = style.knob;
 		}
 	}
 }
