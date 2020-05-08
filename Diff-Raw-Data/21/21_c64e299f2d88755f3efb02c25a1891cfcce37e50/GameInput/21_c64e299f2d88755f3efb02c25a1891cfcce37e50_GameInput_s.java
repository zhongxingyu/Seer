 package com.flipptor.orbgame;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Application.ApplicationType;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.math.Vector2;
 
 public class GameInput implements IGameInput {
 
 	private static final float SCREEN_HEIGHT = Gdx.graphics.getHeight();
 	private static final float MAX_FIRE_HOLD_DELAY = 0.5f;
 	private static final int MIN_SWIPE_LENGTH = 
 			Gdx.graphics.getHeight()*Gdx.graphics.getWidth()/8000;
 	private final Input input = Gdx.input;
 	
 	private boolean gyroControl = false;
 	
 	private long firstPressTime = 0;
 	private long lastPressTime = 0;
 	
	/** Vector of press with origo in the center of the screen */
	private Vector2 pressVector = new Vector2();
 	
 	private boolean previouslyPressed = false;
 	private Vector2 firstPressPosition = new Vector2();
 	private Vector2 lastPressPosition = new Vector2();
 	
 	private boolean dashing = false;
 	private boolean shooting = false;
 	
 	public GameInput() {
 		if(Gdx.app.getType() == ApplicationType.Android) {
 			gyroControl = true;
 		}		
 	}
 	
 	@Override
 	public void update() {
 		dashing = false;
 		shooting = false;
 		if(input.justTouched()) {
 			previouslyPressed = true;
 			firstPressTime = input.getCurrentEventTime();
 			firstPressPosition.set(input.getX(), SCREEN_HEIGHT-input.getY());
 			lastPressPosition.set(input.getX(), SCREEN_HEIGHT-input.getY());
 			lastPressTime = input.getCurrentEventTime();
 		} else if(input.isTouched() && previouslyPressed) {
 			// is held down
 			lastPressPosition.set(input.getX(), SCREEN_HEIGHT-input.getY());
 			lastPressTime = input.getCurrentEventTime();
 		} else if(!input.isTouched() && previouslyPressed) {
 			// just released
 			previouslyPressed = false;
			pressVector.set(lastPressPosition.x - firstPressPosition.x, 
 					lastPressPosition.y - firstPressPosition.y);
			if(Math.sqrt(Math.pow(pressVector.x, 2) + 
					Math.pow(pressVector.y, 2)) >= MIN_SWIPE_LENGTH) {
 				// large enough swipe.
 				dashing = true;
 			} else if(lastPressTime - firstPressTime <= MAX_FIRE_HOLD_DELAY) {
 				// short enough since first press
 				shooting = true;
 			}
 		} 
 	}
 	
 	@Override
 	public Vector2 getMovementVector() {
 		if(gyroControl) {
 			return new Vector2(input.getAccelerometerY(), -input.getAccelerometerX());
 		} else {
 			Vector2 vec = new Vector2(0, 0);
 			if(input.isKeyPressed(Keys.UP)) {
 				vec.y += 5;
 			}
 			if(input.isKeyPressed(Keys.DOWN)) {
 				vec.y -= 5;
 			}
 			if(input.isKeyPressed(Keys.LEFT)) {
 				vec.x -= 5;
 			}
 			if(input.isKeyPressed(Keys.RIGHT)) {
 				vec.x += 5;
 			}
 			return vec;
 		}
 	}
 
 	@Override
 	public boolean isDashing() {
 		return dashing;
 	}
 
 	@Override
 	public Vector2 getDashVector() {
 		if(dashing) {
			return new Vector2(pressVector).nor();
 		} else {
 			return new Vector2(0, 0);
 		}
 	}
 
 	@Override
 	public boolean isShooting() {
 		return shooting;
 	}
 
 	@Override
 	public Vector2 getShotVector() {
 		if(shooting) {
			return new Vector2(pressVector).nor();
 		} else {
 			return new Vector2(0, 0);
 		}
 	}
 	
 	@Override
 	public boolean touchIsDown() {
 		return input.isTouched();
 	}
 
 }
