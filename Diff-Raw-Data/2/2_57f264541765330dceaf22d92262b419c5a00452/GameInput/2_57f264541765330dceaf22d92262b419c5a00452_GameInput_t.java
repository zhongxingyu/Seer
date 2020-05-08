 package com.flipptor.orbgame;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Application.ApplicationType;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.math.Vector2;
 
 public class GameInput implements IGameInput {
 
 	private final float screenWidth= Gdx.graphics.getWidth();
 	private final float screenHeight = Gdx.graphics.getHeight();
 	private static final float MAX_DOUBLE_TAP_DELAY = 0.5f;
 	private final Input input = Gdx.input;
 	
 	private boolean gyroControl = false;
 	private long prevPressTime = 0;
 	private long currentPressTime = 0;
 	
 	/** Vector of press with origo in the center of the screen */
 	private Vector2 pressVector;
 	
 	private boolean dashing = false;
 	private boolean shooting = false;
 	
 	public GameInput() {
 		if(Gdx.app.getType() == ApplicationType.Android) {
 			gyroControl = true;
 		}
 		pressVector = new Vector2(0, 0);
 		
 	}
 	
 	@Override
 	public void update() {
 		if(input.justTouched()) {
 			prevPressTime = currentPressTime;
 			currentPressTime = Gdx.input.getCurrentEventTime();
 			pressVector.x = input.getX() - (screenWidth/2);
 			pressVector.y = (screenHeight/2) - input.getY();
 			if(currentPressTime - prevPressTime <= MAX_DOUBLE_TAP_DELAY) {
 				dashing = true;
 				shooting = false;
 			} else {
 				dashing = false;
 				shooting = true;
 			}
 		} else {
 			dashing = false;
 			shooting = false;
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
 			return new Vector2(pressVector);
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
 			return new Vector2(pressVector);
 		} else {
 			return new Vector2(0, 0);
 		}
 	}
 	
 	@Override
 	public boolean touchIsDown() {
 		return input.isTouched();
 	}
 
 }
