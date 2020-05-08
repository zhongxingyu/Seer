 package rsmg.model.weapon;
 
 import java.util.Collection;
 import java.util.Random;
 
 import rsmg.model.ObjectName;
 import rsmg.model.Variables;
 import rsmg.model.object.bullet.Bullet;
 import rsmg.util.Vector2d;
 
 
 	
 public class Shotgun implements IWeapon{
 	private Collection<Bullet> bulletList;
 	private boolean shot;
 	private int bulletWidth = 2;
 	private int bulletHeight = 2;
 	private int bulletDamage = 2;
 	private int bulletSpeed = 600;
 	private int offsetX;
 	private int offsetY = 5;
 	private static int amountOfBulletsPerShot = 6;
 	//private static int spread = 200;
 	
 	public Shotgun(Collection<Bullet> bulletList) {
 		this.bulletList = bulletList;
 		shot = false;
 	}
 	
 	@Override
 	public void shoot(double x, double y, boolean isFacingRight) {
 
 		int xBulletSpeed;
 		if (isFacingRight){
 			xBulletSpeed = bulletSpeed;
 			offsetX = 25;
 		}else{
 			xBulletSpeed = -bulletSpeed;
 			offsetX = -5;
 		}
 		
 		Random randomGen = new Random();
 		
 		for(int i = 0; i < amountOfBulletsPerShot; i++){
 			Vector2d bulletVelocity = new Vector2d();
 			bulletVelocity.setX(xBulletSpeed);
 			bulletVelocity.setY((randomGen.nextDouble()*2-1)*Variables.getShotgunSpread());
 			bulletList.add(new Bullet(x+offsetX, y+offsetY, bulletWidth, bulletHeight, ObjectName.SHOTGUN_BULLET, bulletDamage, bulletVelocity));
 		}
 		
 		shot = true;
 	}
 
 	@Override
 	public long getCooldown(boolean rapidFire) {
 		if(rapidFire) {
 			return 450;
 		}
 		return 500;
 	}
 	
 	@Override
 	public boolean shot() {
 		if (shot) {
 			shot = !shot;
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	public String getName() {
		return ObjectName.SHOTGUN;
 	}
 }
