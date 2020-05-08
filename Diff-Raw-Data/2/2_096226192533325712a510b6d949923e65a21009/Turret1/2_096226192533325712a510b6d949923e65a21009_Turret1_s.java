 package Turrets;
 
 import jgame.ImageCache;
 import bullets.Bullet;
 import bullets.Bullet1;
 import dtb.Defend;
 
 public class Turret1 extends Turret {
 	
 	private boolean placed = false;
 	public Turret1() {
		super(ImageCache.forClass(Defend.class).get("Swords/swordWood.png"));
 		// TODO Auto-generated constructor stub
 	}
 	public double getFireRange(){
 		return 7.5;
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
     	return new Bullet1();
     }
     public boolean isPlaced() {
         return placed;
 }
 
     public void setPlaced(boolean placed) {
         this.placed = placed;
 }
 
 }
