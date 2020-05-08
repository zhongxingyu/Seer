 package ship.weapon;
 
 import actor.Actor;
 
 public class Machinegun extends ship.weapon.Weapon {
     private static int multiplier=0;
     @Override
     public long getShotCoolDown() {
         return 100;
     }
 
     @Override
     protected actor.Projectile newProjectile(actor.Actor ship) {
         return new actor.Bullet(ship,1.0f,multiplier);
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
         return "Machine Gun";
     }
     public static int getMulitplier() {
         return multiplier;
     }
    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
     }
 }
