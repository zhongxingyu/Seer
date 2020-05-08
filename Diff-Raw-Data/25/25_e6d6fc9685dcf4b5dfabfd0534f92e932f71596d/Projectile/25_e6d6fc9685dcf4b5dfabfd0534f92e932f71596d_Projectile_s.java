 package vooga.rts.gamedesign.sprite.rtsprite;
 
 import java.awt.Dimension;
 
 
 import vooga.rts.gamedesign.sprite.GameEntity;
 import vooga.rts.gamedesign.sprite.InteractiveEntity;
 import vooga.rts.util.Location;
 import vooga.rts.util.Location3D;
 import vooga.rts.util.Pixmap;
 import vooga.rts.util.Vector;
 
 
 /**
  * This class is the generic abstract class for all types of projectiles that
  * can be fired from a weapon by an InteractiveEntity. The Projectiles health
  * is the time the projectile can exist on the map before it vanishes. It also
  * vanishes after it collides with a GameEntity. 
  * 
  * @author Ryan Fishel
  * @author Kevin Oh
  * @author Francesco Agosti
  * @author Wenshun Liu 
  *
  */
 public class Projectile extends GameEntity{
 	//Default speed
 	public static int DEFAULT_PROJECTILE_SPEED = 1600;
 	
     private int myDamage;
     private InteractiveEntity myTarget;
 
    public Projectile(Projectile other, Location3D shootFrom) {
        this(new Pixmap(other.getImage()), new Location3D(shootFrom), new Dimension(other.getSize()), other.getPlayerID(), other.getDamage(), other.getHealth());
    }
     public Projectile(Pixmap pixmap, Location3D loc, Dimension size, int playerID, int damage, int health){
         super(pixmap, loc, size, playerID, health);
         myDamage = damage;
     }
     public void setEnemy(InteractiveEntity enemy){
         myTarget = enemy;
     }
     public int getDamage() {
         return myDamage;
     }
     @Override
     public void update(double elapsedTime){
         super.update(elapsedTime);
         this.move(myTarget.getWorldLocation());
         if(this.intersects(myTarget.getWorldLocation())){
             attack(myTarget);
             this.die();
         }
     }
     public void attack(InteractiveEntity interactiveEntity) {
         interactiveEntity.changeHealth(myDamage);
     }
     @Override
     public int getSpeed() {
     	return DEFAULT_PROJECTILE_SPEED;
     }
 }
