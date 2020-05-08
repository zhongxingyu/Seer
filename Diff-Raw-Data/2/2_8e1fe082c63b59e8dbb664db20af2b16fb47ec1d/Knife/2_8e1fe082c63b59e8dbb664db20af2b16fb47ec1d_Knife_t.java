 package com.hackaton.firstTeamGame.models;
 
 import java.util.Collection;
 
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 
 public class Knife extends Weapon {
 
 	public Knife() {
 		super();
 	}
 
 	@Override
 	public boolean canShoot(Collection<Soldier> enemies) {
 		float x1;
 		for(Soldier soldier: enemies) {
 			if(owner.faceLeft)
 				x1 = owner.shape.x - owner.shape.width;
 			else
 				x1 = owner.shape.x + owner.shape.width;
 			Rectangle aux = new Rectangle(x1, owner.speed.y, Soldier.WIDTH, Soldier.HEIGHT);
 			if(aux.contains(soldier.shape))
 				return true;
 		}
 		return false;
 	}
 
 	@Override
 	public Bullet fireWeapon() {
 		float x1;
 		if(owner.faceLeft)
 			x1 = owner.shape.x - owner.shape.width;
 		else
 			x1 = owner.shape.x + owner.shape.width;
 		Bullet bullet = new Bullet(x1, owner.speed.y, Soldier.WIDTH, Soldier.HEIGHT, damage);
 		bullet.setSpeed(new Vector2(0, 0));
		return bullet;
 	}
 
 }
