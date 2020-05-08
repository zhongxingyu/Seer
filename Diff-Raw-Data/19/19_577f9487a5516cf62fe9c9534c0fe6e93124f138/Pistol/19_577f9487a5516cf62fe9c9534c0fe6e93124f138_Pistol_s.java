 package rsmg.model.weapon;
 
 import java.util.Collection;
 
 import rsmg.model.ObjectName;
 import rsmg.model.object.bullet.Bullet;
 import rsmg.util.Vector2d;
 
 public class Pistol implements IWeapon{
 	private Collection<Bullet> bulletList;
 	private boolean shot;
 
 	private static int bulletWidth = 5;
 	private static int bulletHeight = 3;
 	private static int bulletDamage = 5;
 	private static int bulletSpeed = 500;
 	private int offsetX;
 	private int offsetY;
 	
 	public Pistol(Collection<Bullet> bulletList) {
 		this.bulletList = bulletList;
 		this.shot = false;
 	}
 	
 	@Override
 	public void shoot(double x, double y, boolean isFacingRight) {
 
 		Vector2d bulletVelocity = new Vector2d();
 		
 		if (isFacingRight){
 			bulletVelocity.setX(bulletSpeed);
 			offsetX = 25;
 			offsetY = 5;
 		}else{
 			bulletVelocity.setX(-bulletSpeed);
 			offsetX = -5;
 			offsetY = 5;
 		}
 		
 		bulletList.add(new Bullet(x+offsetX, y+offsetY, bulletWidth, bulletHeight, ObjectName.PISTOL_BULLET, bulletDamage, bulletVelocity));
 		
 		shot = true;
 	}
 
 	@Override
 	public long getCooldown(boolean rapidFire) {
 		if(rapidFire) {
 			return 250;
 		}
 		return 300;
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
		return "pistol";
 	}
 }
