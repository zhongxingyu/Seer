 package org.ivan.simple.game.hero;
 
 import org.ivan.simple.ImageProvider;
 import org.ivan.simple.R;
 import org.ivan.simple.game.MotionType;
 
 import android.graphics.Canvas;
 
 public class Hero {
 	/**
 	 * Save prev motion after set changed. Prev motion used to get proper animation.
 	 * For example, if prev motion was STEP_LEFT and next motion will be STAY,
 	 * Panda schould turn 90 degrees right in air while jumping on place.
 	 */
 	private MotionType currentMotion = MotionType.NONE;
 	private MotionType finishingMotion = MotionType.NONE;
 	private Sprite sprite8 = new Sprite(ImageProvider.getBitmap(R.drawable.panda_sprite8), 28, 8);
 	private Sprite sprite16 = new Sprite(ImageProvider.getBitmap(R.drawable.panda_sprite16), 8, 16);
 	private Sprite activeSprite;
 	private Sprite shadeSprite = new Sprite(ImageProvider.getBitmap(R.drawable.panda_sprite8), 28, 8);
 	public int heroX;
 	public int heroY;
 	private int prevX;
 	private int prevY;
 	
 	public Hero() {
 		sprite16.setAnimating(true);
 		sprite8.setAnimating(true);
 		activeSprite = sprite16;
 		shadeSprite.setAnimating(true);
 	}
 	
 	public Sprite getSprite() {
 		return activeSprite;
 	}
 	
 	public boolean isFinishing() {
 		return finishingMotion.isFinishing();
 	}
 	
 	public boolean isStarting() {
 		return currentMotion.isStarting();
 	}
 	
 	public boolean tryToEndFinishMotion() {
 		if(activeSprite.currentFrame == 0 ||
 				(finishingMotion == MotionType.FLY_LEFT && activeSprite.currentFrame == 4) ||
 				(finishingMotion == MotionType.FLY_RIGHT && activeSprite.currentFrame == 4)) {
 			// turn motion to initial stage (stage == 0)
 			finishingMotion.startMotion();
 			switchToCurrentMotion();
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public boolean tryToEndStartMotion() {
 		if(activeSprite.currentFrame == 0) {
 			currentMotion.continueMotion();
 			switchToCurrentMotion();
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Check if hero is in control state: ready for begin new motion type.
 	 * Used each game loop iteration to know is it time to process user controls
 	 * and achieve new motion type.
 	 * More often there is control state when next frame is first frame of animation.   
 	 * @return
 	 */
 	public boolean isInControlState() {
 		if((finishingMotion == MotionType.FLY_LEFT  ||
 				finishingMotion == MotionType.FLY_RIGHT) &&
 				finishingMotion.isFinishing()) {
 			return activeSprite.currentFrame == 4;
 		}
 //		if(!finishingMotion.isFinishing()) {
 //			switch(currentMotion) {
 //			case NONE:
 //			case STAY:
 //			case FALL_BLANSH:
 //			case STEP_LEFT_WALL:
 //			case STEP_RIGHT_WALL:
 				return activeSprite.currentFrame == 0;
 //			default:
 //				return activeSprite.currentFrame % 8 == 0;
 //			}
 //		} else if(activeSprite.currentFrame == 0) {
 //			finishingMotion = MotionType.NONE;
 //			switchToCurrentMotion();
 //			return false;
 //		}
 //		return false;
 	}
 	
 	/**
 	 * Change hero behavior (animation) depending on motion type.
 	 * Used after new motion type is obtained. 
 	 * Goal is to play start/end animations of motions.
 	 * @param newMotion
 	 */
 	public void changeMotion(MotionType newMotion) {
 		prevX = heroX;
 		prevY = heroY;
 		startFinishMotions(newMotion);
 		if (finishingMotion.isFinishing()) {
 			switch(finishingMotion) {
 			case MAGNET:
 				activeSprite.changeSet(17);
 				break;
 			case FLY_LEFT:
 				// skip finishing fall down after FLY if finish because wall
 				if(newMotion == MotionType.JUMP_LEFT_WALL|| 
 				newMotion == MotionType.FLY_RIGHT) {
 					finishingMotion.startMotion();
 					switchToCurrentMotion();
 				} else {
 					activeSprite.changeSet(26);
 				}
 				break;
 			case FLY_RIGHT:
 				// skip finishing fall down after FLY if finish because wall
 				if(newMotion == MotionType.JUMP_RIGHT_WALL || 
 				newMotion == MotionType.FLY_LEFT) {
 					finishingMotion.startMotion();
 					switchToCurrentMotion();
 				} else {
 					activeSprite.changeSet(26);
 				}
 				break;
 			}
 		} else if(currentMotion.isStarting()) {
 			pickActiveSprite(currentMotion);
 			switch(currentMotion) {
 			case JUMP:
 				activeSprite.changeSet(9);
 				break;
 			}
 		} else {
 			switchToCurrentMotion();
 		}
 	}
 	
 	/**
 	 * Begins main animation, after finish/start animations became complete
 	 */
 	private void switchToCurrentMotion() {
 		pickActiveSprite(currentMotion);
 		switch (currentMotion) {
 		case STAY:
 			if(finishingMotion == MotionType.THROW_LEFT || 
 			finishingMotion == MotionType.JUMP_LEFT ||
 			finishingMotion == MotionType.TP_LEFT) {
 				activeSprite.changeSet(1);
 			} else if(finishingMotion == MotionType.THROW_RIGHT || 
 					finishingMotion == MotionType.JUMP_RIGHT ||
 					finishingMotion == MotionType.TP_RIGHT) {
 				activeSprite.changeSet(2);
 			} else {
 				activeSprite.changeSet(0);
 			}
 			break;
 		case FALL:
 			if(Math.random() > 0.5) {
 				activeSprite.changeSet(5);
 			} else {
 				activeSprite.changeSet(6);
 			}
 			break;
 		case FALL_BLANSH:
 			activeSprite.changeSet(3);
 			break;
 //		case STEP_LEFT:
 //			if(finishingMotion == currentMotion || finishingMotion == MotionType.JUMP_LEFT) {
 //				activeSprite.changeSet(2);
 //			} else {
 //				activeSprite.changeSet(3);
 //			}
 //			break;
 		case JUMP_LEFT:
 			if(finishingMotion == MotionType.JUMP) {
 				activeSprite.changeSet(8);
 			} else if(finishingMotion == currentMotion || 
 					finishingMotion == MotionType.THROW_LEFT ||
 					finishingMotion == MotionType.TP_LEFT) {
 				activeSprite.changeSet(2);
 			} else {
 				activeSprite.changeSet(3);
 			}
 			break;
 //		case STEP_RIGHT:
 //			if(finishingMotion == currentMotion || finishingMotion == MotionType.JUMP_RIGHT) {
 //				activeSprite.changeSet(0);
 //			} else {
 //				activeSprite.changeSet(1);
 //			}
 //			break;
 		case JUMP_RIGHT:
 			if(finishingMotion == MotionType.JUMP) {
 				activeSprite.changeSet(7);
 			} else if(finishingMotion == currentMotion || 
 					finishingMotion == MotionType.THROW_RIGHT ||
 					finishingMotion == MotionType.TP_RIGHT) {
 				activeSprite.changeSet(0);
 			} else {
 				activeSprite.changeSet(1);
 			}
 			break;
 //		case PRE_JUMP:
 //			activeSprite.changeSet(9);
 //			break;
 		case JUMP:
 			if(finishingMotion == currentMotion) {
 				activeSprite.changeSet(4);
 			} else {
 				activeSprite.changeSet(9);
 			}
 			break;
 		case THROW_LEFT:
 			if(finishingMotion == currentMotion && finishingMotion.getStage() == 1) {
 				activeSprite.changeSet(24);
 			} else if(finishingMotion == MotionType.JUMP_LEFT || finishingMotion == MotionType.THROW_LEFT) {
 				activeSprite.changeSet(21);
 			} else {
 				activeSprite.changeSet(20);
 			}
 			break;
 		case THROW_RIGHT:
 			if(finishingMotion == currentMotion  && finishingMotion.getStage() == 1) {
 				activeSprite.changeSet(25);
 			} else if(finishingMotion == MotionType.JUMP_RIGHT || finishingMotion == MotionType.THROW_RIGHT) {
 				activeSprite.changeSet(19);
 			} else {
 				activeSprite.changeSet(18);
 			}
 			break;
 //		case STEP_LEFT_WALL:
 //			activeSprite.changeSet(5);
 //			break;
 //		case STEP_RIGHT_WALL:
 //			activeSprite.changeSet(4);
 //			break;
 		case JUMP_LEFT_WALL:
 			switch(finishingMotion) {
 			case JUMP:
 			case THROW_LEFT:
 			case FLY_LEFT:
 			case JUMP_RIGHT_WALL: 
 				activeSprite.changeSet(12);
 				break;
 			default:
 				activeSprite = sprite16;
 				activeSprite.changeSet(5);
 				break;
 			}
 			break;
 		case JUMP_RIGHT_WALL:
 			switch(finishingMotion) {
 			case JUMP:
 			case THROW_RIGHT:
 			case FLY_RIGHT:
 			case JUMP_LEFT_WALL:
 				activeSprite.changeSet(11);
 				break;
 			default:
 				activeSprite = sprite16;
 				activeSprite.changeSet(4);
 				break;
 			}
 			break;	
 		case BEAT_ROOF:
 			activeSprite.changeSet(10);
 			break;
 		case MAGNET:
 			if(currentMotion.getStage() == 0) {
 				activeSprite.changeSet(13);
 			} else {
 				activeSprite.changeSet(14);
 			}
 			break;
 		case PRE_MAGNET:
 			activeSprite.changeSet(13);
 			break;	
 		case FLY_LEFT:
 			if(finishingMotion == currentMotion || finishingMotion == MotionType.TP_LEFT) {
 				activeSprite.changeSet(22);
 			} else if(finishingMotion == MotionType.JUMP || finishingMotion == MotionType.FLY_RIGHT) {
 				activeSprite.changeSet(11);
 			} else {
 				activeSprite = sprite16;
 				activeSprite.changeSet(7);
 			}
 			break;
 		case FLY_RIGHT:
 			if(finishingMotion == currentMotion || finishingMotion == MotionType.TP_RIGHT) {
 				activeSprite.changeSet(23);
 			} else if(finishingMotion == MotionType.JUMP || finishingMotion == MotionType.FLY_LEFT) {
 				activeSprite.changeSet(12);
 			} else {
 				activeSprite = sprite16;
 				activeSprite.changeSet(6);
 			}
 			break;
 		case TP_LEFT:
 			shadeSprite.changeSet(8);
 			activeSprite.changeSet(8);
 			break;
 		case TP_RIGHT:
 			shadeSprite.changeSet(7);
 			activeSprite.changeSet(7);
 			break;
 		default:
 			activeSprite.changeSet(4);
 			break;
 		}
 	}
 	
 	
 	/**
 	 * Used to properly switch motion type and play start/finish animations
 	 * @param newMotion
 	 */
 	private void startFinishMotions(MotionType newMotion) {
 		finishingMotion = currentMotion;
 		currentMotion = newMotion;
 		if(finishingMotion != currentMotion) {
			if(!(finishingMotion == MotionType.FLY_LEFT && currentMotion == MotionType.TP_LEFT) &&
					!(finishingMotion == MotionType.FLY_RIGHT && currentMotion == MotionType.TP_RIGHT)) {
				finishingMotion.finishMotion();
			}
			if(!(finishingMotion == MotionType.TP_LEFT && currentMotion == MotionType.FLY_LEFT) &&
					!(finishingMotion == MotionType.TP_RIGHT && currentMotion == MotionType.FLY_RIGHT)) {
				currentMotion.startMotion();
			}
 		}
 	}
 
 	
 	/**
 	 * Used to get proper bitmap for motion
 	 * @param mt
 	 */
 	private void pickActiveSprite(MotionType mt) {
 		switch(mt) {
 		case NONE:
 		case STAY:
 		case FALL_BLANSH:
 //		case STEP_LEFT_WALL:
 //		case STEP_RIGHT_WALL:
 			activeSprite = sprite16;
 			break;
 		default:
 			activeSprite = sprite8;
 			break;
 		}
 	}
 	
 	/**
 	 * Draw proper hero animation frame by center coordinates
 	 * @param canvas
 	 */
 	public void onDraw(Canvas canvas) {
 		activeSprite.onDraw(canvas, heroX - activeSprite.getWidth() / 2, heroY - activeSprite.getHeight() / 2);
 		if(currentMotion == MotionType.TP_LEFT || currentMotion == MotionType.TP_RIGHT) {
 			shadeSprite.onDraw(canvas, prevX - shadeSprite.getWidth() / 2, prevY - shadeSprite.getHeight() / 2);
 		}
 	}
 	
 	public void onDrawShade() {
 		if(currentMotion == MotionType.TP_LEFT) {
 			
 		}
 		if(currentMotion == MotionType.TP_RIGHT) {
 			
 		}
 	}
 	
 	/**
 	 * Real motion type used to get proper hero speed on start/finish/main animations
 	 * @return
 	 */
 	public MotionType getRealMotion() {
 		if(currentMotion.isStarting()) {
 			return MotionType.NONE;
 		} else if(finishingMotion.isFinishing()) {
 			return MotionType.NONE;
 		} else {
 			return currentMotion;
 		}
 	}
 	
 	/**
 	 * Play loose animation
 	 */
 	public void playLoseAnimation() {
 		activeSprite = sprite8;
 		activeSprite.currentSet = 5;
 	}
 	
 	/**
 	 * Play win animation
 	 * @return is sprite animating?
 	 */
 	public boolean playWinAnimation() {
 		activeSprite = sprite8;
 		activeSprite.currentSet = 16;
 		activeSprite.playOnce = true;
 		if(!activeSprite.animating) {
 			activeSprite.gotoAndStop(7);
 			return false;
 		}
 		return true;
 	}
 }
