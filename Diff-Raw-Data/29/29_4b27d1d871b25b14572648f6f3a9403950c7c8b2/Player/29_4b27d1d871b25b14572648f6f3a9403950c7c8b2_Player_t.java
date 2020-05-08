 
 package com.jumpandrun;
 
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 
 public class Player {
 	public Body body;
 	
 	public boolean alive = true;
 	
	public int live = 15;
 	
 	public boolean jump = false;	
 	public boolean jumping = false;
 	public int xdir;
 	
 	public float invincible = 0;
 	
 	public Vector2 position = new Vector2();
 	
 	public Weapon weapon;
 	public int currentWeapon = 0; 
 	
 	public float angle = 0;
 
 	public Player(float x, float y) {
 		position.x = x;
 		position.y = y;
 		xdir = 1;
 		weapon = new MachineGun();
 	}
 	
 	public void update(float delta) {
 		position.x = body.getPosition().x;
 		position.y = body.getPosition().y;
 		angle = -position.x*360/(2*MathUtils.PI);
 		
 		invincible = Math.max(0, invincible-(delta*50.f));
 		if(invincible > 0) {
 			body.getFixtureList().get(0).setFilterData(GameInstance.getInstance().enemyCollideFilter);
 			body.getFixtureList().get(1).setFilterData(GameInstance.getInstance().enemyCollideFilter);
 		} else {
 			body.getFixtureList().get(0).setFilterData(GameInstance.getInstance().playerCollideFilter);
 			body.getFixtureList().get(1).setFilterData(GameInstance.getInstance().playerCollideFilter);
 		}
 		
 		weapon.update(delta);
 	}
 	
 	public void hit() {
 		if(invincible>0) return;
 		live-=1;
 		if(live<0) {
 			Resources.getInstance().die.play();
 			alive = false;
 		} else {
 			Resources.getInstance().gethit.play();
 			invincible = 40;
 		}
 		
 	}
 	
 	public void shoot() {		
 
 		weapon.shoot(position, body.getLinearVelocity(), xdir);
 	}
 
 }
