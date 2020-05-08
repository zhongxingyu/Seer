 package com.github.desmaster.Devio.realm.entity;
 
import com.github.desmaster.Devio.cons.Console;
 import com.github.desmaster.Devio.realm.Realm;
 import com.github.desmaster.Devio.util.Position;
 import com.github.desmaster.Devio.util.gamemath.Collision;
 import com.github.desmaster.Devio.util.gamemath.Distance;
 
 public class Mob extends Entity {
 
 	private int walkspeed = 1;
 	private int walkblocktick = 24;
 	double lives;
 
 	private int walkblocktimeout = 0;
 	private boolean walkblockactive = false;
 	protected boolean alive;
 
	public Mob(com.github.desmaster.Devio.util.Position spawnPosition,double lives) {
 		super(spawnPosition);
 		this.lives = lives;
 	}
 
 	public void tick() {
 		walkblock();
 	}
 
 	public void walkblock() {
 		if (walkblocktimeout <= 0) {
 			walkblockactive = false;
 		} else {
 			walkblocktimeout -= 1;
 		}
 	}
 
 	public void hurt(double amount, Entity entity) {
 		if (entity instanceof Player) {
 			Player player = (Player) entity;
 			player.lives -= amount;
			if (player.lives <= 0){
				player.die();
			}
				
		} else if (entity instanceof Mob) {
			Mob mob = (Mob) entity;
			mob.lives -= amount;
			if (mob.lives - amount <= 0)
				die(mob);
 		}
 	}
 
 	private void die(Entity e) {
 		Realm.mobHandler.destroyMob(e);
 	}
 
 	public double getLives() {
 		return lives;
 	}
 
 	public void setLives(double lives) {
 		this.lives = lives;
 	}
 
 	public void walkUp() {
 		face = 0;
 		if (!(y == 0) && !walkblockactive)
 			if (!Collision.check(new Position(x, y), face)) {
 				y -= walkspeed;
 				setWalkBlock(walkblocktick);
 			}
 	}
 
 	public void setWalkSpeed(int ticks) {
 		walkblocktick = ticks;
 	}
 
 	public void walkDown() {
 		face = 2;
 		if (!(y == Realm.WORLD_HEIGHT - 1) && !walkblockactive)
 			if (!Collision.check(new Position(x, y), face)) {
 				y += walkspeed;
 				setWalkBlock(walkblocktick);
 			}
 	}
 
 	public void walkLeft() {
 		face = 3;
 		if (!(x == 0) && !walkblockactive)
 			if (!Collision.check(new Position(x, y), face)) {
 				x -= walkspeed;
 				setWalkBlock(walkblocktick);
 			}
 	}
 
 	public void walkRight() {
 		face = 1;
 		if (!(x == Realm.WORLD_WIDTH - 1) && !walkblockactive)
 			if (!Collision.check(new Position(x, y), face)) {
 				x += walkspeed;
 				setWalkBlock(walkblocktick);
 			}
 	}
 
 	public void setWalkBlock(int timeout) {
 		walkblockactive = true;
 		walkblocktimeout = timeout;
 	}
 
 	public boolean getWalkBlock() {
 		return walkblockactive;
 	}
 
 	public boolean isNearPlayer() {
		return Distance.calulateTotalDistance(x, y, Realm.player.x,
				Realm.player.y) < 50;
 	}
 }
