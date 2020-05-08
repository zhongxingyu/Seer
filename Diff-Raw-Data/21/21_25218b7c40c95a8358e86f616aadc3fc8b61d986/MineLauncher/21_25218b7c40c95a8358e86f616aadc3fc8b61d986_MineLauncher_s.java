 package entity.weapon;
 
 import entity.Mine;
 import level.BasicLevel;
 
 public class MineLauncher implements Weapon {
 
	private int ammo = 10;
 	private double currentCooldown = 0;
	private int cooldownTime = 30;
 
 	@Override
 	public void addAmmo() {
		ammo += 2;
 	}
 
 	@Override
 	public void setAmmo() {
		ammo = 2;
 	}
 
 	@Override
 	public int getAmmo() {
 		return ammo;
 	}
 
 	@Override
 	public void fire(double x, double y, BasicLevel level, double speedPercent, double angle) {
 		if (currentCooldown <= 0 && ammo > 0) {
 			level.addEntity(new Mine(x, y, level, angle));
 			currentCooldown = cooldownTime;
 		}
		ammo--;
 	}
 
 	@Override
 	public void tick(double dt) {
 		if (currentCooldown > 0)
 			currentCooldown -= dt;
 	}
 }
