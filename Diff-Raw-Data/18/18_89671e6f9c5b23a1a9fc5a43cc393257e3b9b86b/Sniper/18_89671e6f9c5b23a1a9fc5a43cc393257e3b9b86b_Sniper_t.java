 package ship.weapon;
 
 import actor.Actor;
 import actor.Projectile;
 
 public class Sniper extends ship.weapon.Weapon {
     private static int multiplier=3;
     @Override
     public long getShotCoolDown() {
         return 1000;
     }
     @Override
     protected actor.Projectile newProjectile(actor.Actor ship) {
         Projectile bullet = new actor.Bullet(ship,50.0f,multiplier);
         System.out.println("velocity: "+bullet.getVelocity());
         return bullet;
     }
     @Override
     public void shoot(Actor ship) {
         //calculates time passed in milliseconds
         if((System.currentTimeMillis() - getLastShotTime()) > getShotCoolDown()) {
             game.Game.getActors().add(newProjectile(ship));
             setLastShotTime(System.currentTimeMillis());
         }
     }
     @Override
     public String getWeaponName() {
         return "Sniper";
     }
     public static int getMulitplier() {
         return multiplier;
     }
     public void setMultiplier(int multiplier) {
        Sniper.multiplier = multiplier;
     }
    
 }
