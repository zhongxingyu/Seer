 package radon.guns;
 
 import radon.Player;
 
 
 public class Shotgun extends Gun {
     
     public static final int autoFireRate = 0;
     public static final int manualFireRate = 45;
    public static final float bulletForce = 20;
     
     public Shotgun(Player p) {
         super(p, autoFireRate, manualFireRate);
     }
     
     @Override
     public void fireAuto(float angle) {
         return;
     }
     
     @Override
     public void fireManual(float angle, int fireDelay) {
         for (int i = 0; i < 7; i++) {
            float angleSpread = angle + (rand.nextFloat() * 3 - 1) * ((2) * ((p.velocity.length())) + 5);
             
             fireBullet(angleSpread, bulletForce, BulletType.NORMAL);
         }
     }
 }
