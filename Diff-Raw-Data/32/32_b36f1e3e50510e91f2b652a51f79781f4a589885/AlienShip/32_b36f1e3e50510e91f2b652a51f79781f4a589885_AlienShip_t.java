 package com.gradiuss.game.models;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.util.Log;
 
 public class AlienShip extends Enemy {
 	private static final String TAG = AlienShip.class.getSimpleName();
 //	private boolean shooting;
 	private boolean hit;
 	private float previousLife;
 	private static final int INITIAL_LIFE = 100;
 	private static final int INITIAL_DAMAGE = 80;
 	private SpaceShip target;
 	
 	
 	public AlienShip(Bitmap bitmap, float x, float y, SpaceShip target) {
 		super(bitmap, x, y);
 		setLife(INITIAL_LIFE);
 		setDamage(INITIAL_DAMAGE);
 		this.target = target;
 		initialize();
 	}
 
 	@Override
 	public void initialize() {
 		SCORE = 50;		
 	}
 	
 	public void setHit(boolean hit) {
 		this.hit = hit;
 	}
 	
 	public boolean isHit() {
 		return hit;
 	}
 	
 	@Override
 	public void setLife(float life) {
 		previousLife = getLife();
 		super.setLife(life);
 	}
 
 	@Override
 	public void updateState() {
 		setShooting(false);
 		float xDiff = getX() - target.getX();
 		float yDiff = getY() - target.getY();
 		if(yDiff < 0) {
			//positive xDiff => coming from right
			if(xDiff >= (0.3 * (float)target.getBitmapWidth())) {
 				setMoveRight(false);
 				setMoveLeft(true);
				if(xDiff <= 0) {//0.02 * target.getBitmapWidth()) {
 					setMoveLeft(false);
 					setShooting(true);
					float x = (float)(0.3 * (float) target.getBitmapWidth());
					Log.d(TAG, "the float = " + x + ", xDiff = " + xDiff);
 				}
			} 
			//negative xDiff => coming from left
			else if(xDiff <= (0.3 * (float)target.getBitmapWidth())) {
 				setMoveLeft(false);
 				setMoveRight(true);
				if(xDiff >= 0) {// 0.02 * target.getBitmapWidth()) {
 					setMoveRight(false);
 					setShooting(true);
					float x = (float)(0.3 * (float) target.getBitmapWidth());
					Log.d(TAG, "the float = " + x + ", xDiff = " + xDiff);
 				}
			} 
			else {
 				setMoveRight(false);
 				setMoveLeft(false);
 			}
 		}
 		
 		if (moveLeft) {	
 			setX((getX() + (getVx() * MovingObject.DIRECTION_LEFT)));
 		}
 		if (moveRight) {	
 			setX((getX() + (getVx() * MovingObject.DIRECTION_RIGHT)));
 		}
 		if (moveUp) {	
 			setY((getY() + (getVy() * MovingObject.DIRECTION_UP)));
 		}
 		if (moveDown) {	
 			setY((getY() + (getVy() * MovingObject.DIRECTION_DOWN)));
 		}
 		if (isHit()) {
 			float damage = previousLife - getLife();
 			setDamage((int)(damage));
 			setHit(false);
 		}
 		super.updateState();
 	}
 	
 
 	@Override
 	public void draw(Canvas canvas) {
 		super.draw(canvas);
 	}
 
 	@Override
 	public String toString() {
 		return "ALIEN";
 	}
 
 }
