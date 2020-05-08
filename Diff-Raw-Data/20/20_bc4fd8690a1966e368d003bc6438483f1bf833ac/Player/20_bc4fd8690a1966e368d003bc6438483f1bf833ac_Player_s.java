 package com.rustbyte;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.awt.event.KeyEvent;
 
 import com.rustbyte.Game;
 import com.rustbyte.vector.Vector2;
 import com.rustbyte.level.*;
 
 public class Player extends Mob  {
 	private int ANIM_WALK_RIGHT;
 	private int ANIM_WALK_LEFT;
 	private int ANIM_IDLE_LEFT;
 	private int ANIM_IDLE_RIGHT;	
 	private int ANIM_JUMP_LEFT;
 	private int ANIM_JUMP_RIGHT;
 	private InputHandler input = null;
 	
 	public boolean weaponFired = false;
 	public int weaponTimer = 0;
 	private final int weaponDamage = 25;
 	
 	public Player(int x, int y, int w, int h, Entity p, Game g) {
 		super(x, y, w, h, p, g);
 		ANIM_WALK_RIGHT = this.animator.addAnimation(5, 121, 0, w, h, false,0);
 		ANIM_WALK_LEFT = this.animator.addAnimation(5, 121, 0, w, h, true,0);
 		ANIM_IDLE_RIGHT = this.animator.addAnimation(1, 101, 0, w, h, false,0);
 		ANIM_IDLE_LEFT = this.animator.addAnimation(1, 101, 0, w, h, true,0);
 		ANIM_JUMP_RIGHT = this.animator.addAnimation(1, 101 + w * 3, 0, w, h, false,0);
 		ANIM_JUMP_LEFT = this.animator.addAnimation(1, 101  + w * 3, 0, w, h, true,0);
 		this.animator.setFrameRate(15.0);
 		
 		animator.setCurrentAnimation(ANIM_IDLE_RIGHT);
 		input = g.input;
 		speed = 1.50;
 		xr = 6;
 		flashEffect.setColor(0xFFFF00);
 		hitpoints = 100;
 	}
 	@Override
 	public void tick() {
 		super.tick();								
 		
 		speed = 1.50;
 
 		if(hitpoints <= 0) {
 			this.explode(16, Art.getColor(255,255,0), 100);
 			//this.breakApart(16, Art.getColor(255,255,0), 100);
 			this.alive = false;
 		}
 
 		
 		if(hurtTimer < 40)
 			knockedBack = false;
 		
 
 		if(!knockedBack) {
 			dirY = dirX = 0;		
 
 			if(input.keys[KeyEvent.VK_SPACE].pressed)
 				jump();		
 			if(input.keys[KeyEvent.VK_LEFT].pressed)
 				dirX = -1;
 			if(input.keys[KeyEvent.VK_RIGHT].pressed)
 				dirX = 1;
 			if(input.keys[KeyEvent.VK_D].pressed)
 				fireWeapon();
 			
 			velX = dirX * speed;		
 			
 			if(weaponFired && --weaponTimer <= 0)
 				weaponFired = false;			
 		}		
 
 		
 		move();
 		
 		if(!knockedBack) {
 			if(velX < 0 ) this.animator.setCurrentAnimation(ANIM_WALK_LEFT);
 			else if(velX > 0) this.animator.setCurrentAnimation(ANIM_WALK_RIGHT);
 			else {
 				if(facing == -1) this.animator.setCurrentAnimation(ANIM_IDLE_LEFT);
 				if(facing == 1) this.animator.setCurrentAnimation(ANIM_IDLE_RIGHT);
 			}				
 			if(jumping && facing == 1) this.animator.setCurrentAnimation(ANIM_JUMP_RIGHT);
 			if(jumping && facing == -1) this.animator.setCurrentAnimation(ANIM_JUMP_LEFT);
 		}
 		
 		animator.tick();				
 	}
 	private void fireWeapon() {						
 		if(!weaponFired) {
 			weaponFired = true;
 			weaponTimer = 5;
 			
 			boolean bulletHit = false;
			double cx = 0; // bullet collision point
 			Tile t = game.level.getTileFromPoint(xx,yy);			
 			while(t != null && !bulletHit) {												
 				if( t != null) {
 					if(t.blocking) {
 						t.takeDamage(this, weaponDamage);
 						
 						if((t.tx * t.width) > xx)
 							cx = t.tx * t.width;
 						else
 							cx = (t.tx * t.width) + t.width - 1; 
 						bulletHit = true;
 					} else {
 						ArrayList<Entity> entities = t.getEntities();
 						if(entities.size() > 0) {
 							Comparator<Entity> comp = new Comparator<Entity>() {
 								public int compare(Entity e1, Entity e2) {
 									if((e1.xx - xx) > (e2.xx - xx)) return -1;
 									if((e1.xx - xx) < (e2.xx - xx)) return +1;
 									return 0;
 								}
 							};
 							
 							Collections.sort(entities, comp);
 							for(int i=0; i < entities.size();i++) {
 								Entity ent = entities.get(i);
 								if( ent!= this && ent instanceof Mob) {
 									Mob m = (Mob)ent;
 									m.takeDamage(this, weaponDamage);
 									cx = ent.xx;
 									bulletHit = true;
 									break;
 								}
 							}
 						}
 					}
 				}
 				// get next tile
 				t = game.level.getTile(t.tx + facing, t.ty);
			}				
 			BulletTrace bt = new BulletTrace(xx + (10 * facing),yy+1, cx, yy+1,0,0,null,game);
 			game.addEntity(bt);
 		}
 	}
 	@Override
 	public void render() {		
 		if(weaponFired && !isHurt()) {
 			if(facing > 0)
 				Art.sprites.draw(game.screen, (((int)xx) + wid/2) - game.level.viewX, 
 										      (((int)yy)) - game.level.viewY, 82, 82,5,3,false);
 			else
 				Art.sprites.draw(game.screen, (((int)xx) - 5 - wid/2)  - game.level.viewX, 
 						 (((int)yy)) - game.level.viewY, 82, 82,5,3,true);
 		}				
 		
 		if(isHurt()) {
 			flashEffect.clear();
 			animator.render(flashEffect.renderFrame, 0, 0);
 			flashEffect.render(game.tickcount, game.screen, (((int)xx) - (wid / 2)) - game.level.viewX,
 															(((int)yy) - (hgt / 2)) - game.level.viewY);
 		} else {
 			animator.render(game.screen, (((int)xx) - (wid / 2)) - game.level.viewX, 
 				   	 					 (((int)yy) - (hgt / 2)) - game.level.viewY);
 		}
 	}
 	@Override
 	public void takeDamage(Entity source, int amount) {		
 		if(!isHurt()) {
 			hitpoints -= amount;
 			hurt(50);		
 			game.addEntity(new FloatingText("-" + amount,Art.getColor(255,0,0),xx,yy,new Vector2(0,-1), null, game));
 			this.knockBack((source.xx - xx), 1.0);
 		}
 	}
 }
