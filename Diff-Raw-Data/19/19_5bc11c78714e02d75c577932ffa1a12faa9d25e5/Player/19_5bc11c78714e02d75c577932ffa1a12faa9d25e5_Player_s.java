 package gameconcept;
 
 import org.lwjgl.input.Mouse;
 
 public class Player extends Entity {
     //Need to implement updating of screenx and screeny
     protected int screenx = worldx;
     protected int screeny = worldy;
     private static ProjectileEngine projectileEngine;
     private static SoundEngine soundEngine;
     
     public Player(int x, int y, ProjectileEngine projectileEngine, SoundEngine soundEngine) {
         super(x, y);
         this.projectileEngine = projectileEngine;
         this.soundEngine = soundEngine;
     }
     
     public void update(int delta) {
         worldx += (delta * dx) / 1000;
         worldy += (delta * dy) / 1000;
     }
     
     public int getScreenX() {
         return screenx;
     }
     
     public int getScreenY() {
         return screeny;
     }
     
     public void setScreenX(int x) {
         screenx = x;
     }
     
     public void setScreenY(int y) {
         screeny = y;
     }
     
     public void shoot() {
         Projectile projectile = new Projectile(worldx, worldy, ProjectileType.BULLET);
         projectile.setVelocity(1000);
         projectile.setAngle(Math.atan2(Mouse.getY() - worldy, Mouse.getX() - worldx));
         projectile.setSlope(((double) Mouse.getY() - worldy) / ((double) Mouse.getX() - worldx));
         
        soundEngine.loadSound("shot");
         soundEngine.playSound();
         projectileEngine.add(projectile);
     }
 }
