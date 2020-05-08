 package Turrets;
 
 import jgame.ImageCache;
 import bullets.Bullet;
 import bullets.Bullet3;
 import dtb.Defend;
 
 public class Turret3 extends Turret{
 
 	public Turret3() {
 		super(ImageCache.forClass(Defend.class).get("Wands/wand3turret.png"), 500);
 		// TODO Auto-generated constructor stub
 	}
 	public double getFireRange(){
		return 1000;
 	}
 
     public int getFireDelay(){
     	return 10;
     }
 
     public int getFireCoolDown(){
     	return 10;
     }
 
     public double getBulletSpeed(){
     	return 10;
     }
 
     public Bullet createBullet(){
     	return new Bullet3();
     }
 
 }
