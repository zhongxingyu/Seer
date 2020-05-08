 package dudes;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 
 import weapons.*;
 
 public class Knight extends Monster {
 	float homeToleranceX;
 	float homeToleranceY;
 	GameContainer container;
 	boolean movingUp;
 	boolean movingDown;
 	boolean movingLeft;
 	boolean movingRight;
 
 	public Knight(float xPos, float yPos, int k, GameContainer container) {
 		super();
 		this.container = container;
 		maxHealth = 37;
 		health = maxHealth;
 		pos[0] = xPos;
 		pos[1] = yPos;
 		isRight = false;
 		moveSpeed = 1;
 		healthFill = new Color(Color.red);
		attackTime = 0;
 		//hitbox = new Rectangle(pos[0], pos[1], 64, 64);
 		kind = k;
 		value = 100;
 		
 		this.weapon = new KnightKnife(this);
 		try {
 			this.init();
 		} catch (SlickException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void init() throws SlickException {
 		//create spritesheets for the weapon:
 		homeToleranceX = container.getWidth()/4;
 		this.weapon.init();
 		aiDelay = 1000;
 	}
 
 	// return leftmost point of weapon
 	public float[] weaponLoc() {
 		if(this.isRight){
 			return new float[] {pos[0] + 64 + 15, pos[1]+ 40};
 		}
 		else {
 			return new float[] {pos[0] - 15, pos[1]+40};
 		}
 	}
 
 	@Override
 	public void aiLoop(Player[] players, ArrayList<Monster> monsters, int delta)
 			throws SlickException {
 		if (health <= 0){
 			currentAnimation = handleAnimation("die");
 		}
 		if (doingNothing) {
 			if (locked == null) {
 				doingNothing = this.doNothing(700, delta);
 				if(this.movingLeft){
 					this.moveLeft(1, players, monsters);
 				}
 				if(this.movingRight){
 					this.moveRight(1, players, monsters);
 				}
 				if(this.movingUp){
 					this.moveUp(1, players, monsters);
 				}
 				if(this.movingDown){
 					this.moveDown(1, players, monsters);
 				}
 				if(!doingNothing){
 					this.movingLeft = false;
 					this.movingRight = false;
 					this.movingDown = false;
 					this.movingUp = false;
 				}
 
 			} else {
 				doingNothing = this.doNothing(300, delta);
 				if (locked.getHitBox().getCenterX() > this.pos[0]) {
 					this.moveRight(0, players, monsters);
 				} else {
 					this.moveLeft(0, players, monsters);
 				}
 				if (!doingNothing && Math.random() < .3) {
 					currentAnimation = handleAnimation("attack");
 					this.attack();
 				} else {
					currentAnimation = handleAnimation("walk");
 				}
 				currentAnimation.start();
 			}
 			return;
 		}
 		if (locked == null) {
 			if (Math.abs(players[0].getHitBox().getCenterX() - this.pos[0]) < homeToleranceX) {
 				locked = players[0];
 			} else if (Math.abs(players[1].getHitBox().getCenterX() - this.pos[0]) < homeToleranceX) {
 				locked = players[1];
 			}
 			else {
 				if (Math.random() < .5) {
 					this.movingLeft = true;
 				}
 				else if (Math.random() < .5) {
 					this.movingRight = true;
 				}
 				if (Math.random() < .5) {
 					this.movingUp = true;
 				}
 				else if (Math.random() < .5) {
 					this.movingDown = true;
 				}
 				currentAnimation = handleAnimation("walk");
 				currentAnimation.start();
 				doingNothing = true;
 			}
 
 		} else {
 			if (Math.abs(locked.getHitBox().getCenterX() - this.pos[0]) > 1.5 * homeToleranceX) {
 				locked = null;
 				homing = false;
 			} else {
 				if (!homing) {
 					homing = true;
 				} else {
 					homing = home(locked.getHitBox().getCenter(), players, monsters);
 					if (!homing) {
 						this.doNothing(300, delta);
 						doingNothing = true;
 					}
 					currentAnimation = handleAnimation("walk");
 					currentAnimation.start();
 					return;
 				}
 			}
 			currentAnimation = handleAnimation("walk");
 			currentAnimation.start();
 			return;
 		}
 	}
 
 	@Override
 	public Animation handleAnimation(String whichAnim) {
         if (isRight) {
             if (whichAnim.equals("flinch")) {
                 return weapon.anims[1];
             } else if (whichAnim.equals("punch")) {
                 return weapon.anims[3];
             } else if (whichAnim.equals("die")){
             	return weapon.anims[7];
             } else {
                 // else, the walk animation for now
                 return weapon.anims[5];
             }
         } else {
             if (whichAnim.equals("flinch")) {
                 return weapon.anims[0];
             } else if (whichAnim.equals("punch")) {
                 return weapon.anims[2];
             } else if (whichAnim.equals("die")){
             	return weapon.anims[6];
             } else {
                 // else, the walk animation for now
                 return weapon.anims[4];
             }
         }
 	}
 	
 	@Override
 	public Coin getDropCoin() throws SlickException {
 		double rand2 = Math.random();
         if(rand2<0.5){
         	return new Coin("yellow",pos);
         } else if(rand2<0.7){
         	return new Coin("red",pos);
         } else if(rand2<0.85){
         	return new Coin("blue",pos);
         } else if(rand2<0.95){
         	return new Coin("green",pos);
         } else if (rand2 < 1){
         	return new Coin("purple",pos);
         } 
         
         return null;
 	}
 }
