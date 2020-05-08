 package com.detourgames.raw;
 
 import com.detourgames.raw.Event;
 
 public class FireProjectileEvent extends Event {
 
 	float x;
 	float y;
 
 	public FireProjectileEvent(float x, float y) {
 
 		this.x = x;
 		this.y = y;
 
 	}
 
 	@Override
 	public void executeEvent() {
 		System.out.println("Shot at "+x+"' "+y);
 		//change from screen coords to world coords
 		// "world is locked = "+GameManager.getGameManager().getLevel().getWorld().isLocked());
 		/*
 		 * Projectile p = ProjectilePool.getProjectilePool().getProjectile();
 		 * p.prepareForSpawn(Projectile.TYPE_RAW,
 		 * GameManager.getGameManager().getLevel().getHero(), new Vec2(15,10));
 		 * p.spawn(GameManager.getGameManager().getLevel().getWorld());
 		 */// TODO make projectiles work!
 	}
 
 }
