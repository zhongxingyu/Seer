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
			if(xDiff > (0.3 * target.getBitmapWidth())) {
 				setMoveRight(false);
 				setMoveLeft(true);
				if(xDiff < 0.02 * target.getBitmapWidth()) {
 					setMoveLeft(false);
 					setShooting(true);
					Log.d(TAG, "shooting");
 				}
			} else if(xDiff < (0.3 * target.getBitmapWidth())) {
 				setMoveLeft(false);
 				setMoveRight(true);
				if(xDiff > 0.02 * target.getBitmapWidth()) {
 					setMoveRight(false);
					Log.d(TAG, "shooting");
 					setShooting(true);
 				}
			} else {
 				setMoveRight(false);
 				setMoveLeft(false);
 			}
 		}
//		if((int)yDiff < GameView.height)
//			
//			
//		if((int)yDiff < GameView.height)
 		
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
