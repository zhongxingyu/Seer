 package entity.weapon;
 
 import java.util.Random;
 
 import sound.SoundEffect;
 import entity.Bullet;
 import entity.Shell;
 import entity.Tank;
 import level.BasicLevel;
 
 public class Minigun implements Weapon{
 	
 	public boolean firing = false;
 	int bulletsToFire = 100;
 	int bulletsFired = 0;
 	double currentCooldown = 0;
 	double cooldownTime = 120;
	int ammo = 0;
 	Tank owner;
 	BasicLevel level;
 	Random rand = new Random();
 	public Minigun(Tank tank){
 		this.owner = tank;
 	}
 	
 	@Override
 	public void fire(double x, double y, BasicLevel level, double speedPercent, double angle) {
 		this.level = level; 
 		if (currentCooldown <= 0 && ammo > 0) {
 			ammo--;
 			firing = true;
 			bulletsFired = 0;
 			currentCooldown = cooldownTime;
 		}
 	}
 	
 	@Override
 	public void addAmmo() {
 		ammo += 2;
 	}
 	
 	@Override
 	public int getAmmo() {
 		return ammo;
 	}
 	
 	@Override
 	public void setAmmo() {
 		ammo = 0;
 	}
 	
 	public void fireEvent(){
 		level.addEntity(new Bullet(owner.getCrosshairLocation().getX(), owner.getCrosshairLocation().getY(), level, owner.getMuzzleAngle() + 3 * rand.nextDouble() - 3*rand.nextDouble()));
 //		SoundEffect.EXPLOSION1.play();
 	}
 
 	@Override
 	public void tick(double dt) {
 		if (currentCooldown > 0)
 			currentCooldown -= dt;
 		if(firing){
 			fireEvent();
 			bulletsFired++;
 			if(bulletsFired >= bulletsToFire)
 				firing = false;
 		}
 	}
 }
