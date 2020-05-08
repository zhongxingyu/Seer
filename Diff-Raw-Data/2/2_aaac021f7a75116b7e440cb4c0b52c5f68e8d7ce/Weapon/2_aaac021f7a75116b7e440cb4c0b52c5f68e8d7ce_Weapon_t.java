 package vooga.rts.gamedesign;
 
 import vooga.rts.gamedesign.sprite.rtsprite.IAttackable;
 import vooga.rts.gamedesign.sprite.rtsprite.Projectile;
 import vooga.rts.gamedesign.sprite.rtsprite.RTSprite;
 import vooga.rts.gamedesign.sprite.rtsprite.interactive.Interactive;
 import vooga.rts.gamedesign.upgrades.Upgrade;
 import vooga.rts.gamedesign.upgrades.UpgradeTree;
 import vooga.rts.util.Location;
 import java.awt.geom.Ellipse2D.Double;
 import java.awt.geom.Ellipse2D;
 
 
 /**
  * This class represents a weapon. The CanAttack class the implements
  * AttackStrategy contains a list of weapons so that every interactive that can attack
  * will have an instance of a weapon. A weapon can do damage and also contains
  * a projectile that does damage. A weapon is used to attack by using the fire
  * method. When an upgrade of a pojectile is selected, the weapon will just
  * change the type of projectile that it contains to the specified projectile.
  * 
  * @author Ryan Fishel
  * @author Kevin Oh
  * @author Francesco Agosti
  * @author Wenshun Liu
  * 
  */
 public abstract class Weapon {
 
     private int myDamage;
     private Projectile myProjectile;
     private UpgradeTree myUpgradeTree;
     private int myRange;
     private int maxCooldown;
     private int cooldown; 
     private Ellipse2D myRangeCircle;
 
     /**
      * Creates a new weapon with default damage and projectile.
      * 
      * @param damage
      * @param projectile
      */
     public Weapon (int damage, Projectile projectile, int range, Location center, int cooldownTime) {
         myDamage = damage;
         myProjectile = projectile;
         myRange = range;
         maxCooldown = cooldownTime;
         myRangeCircle = new Ellipse2D.Double(center.getX(), center.getY(), range, range);
     }
 
     /**
      * This method is used by the weapon to attack an RTSprite.
      * @throws CloneNotSupportedException 
      */
     public void fire (RTSprite toBeShot) throws CloneNotSupportedException {
     	if(cooldown == 0) {
 
         	myProjectile.attack(toBeShot);
        	//setCooldown(maxCooldown);
     	}
     }
 
     /**
      * This method is used to upgrade a weapon either
      * 
      * @param upgrade
      */
     public void upgrade (Upgrade upgrade) {
     }
 
     /**
      * This method is used to change the projectile for the weapon
      * 
      * @param projectile is the projectile that will be used by the weapon
      */
     public void setProjectile (Projectile projectile) {
         myProjectile = projectile;
     }
 
     /**
      * Checks to see if the interactive passed in is in the range of the
      * weapon.
      * 
      * @param interactive is checked to see if it is in the range of the weapon
      * @return true if the interactive is in the range of the weapon and false
      *         if the interactive is out of the range of the weapon
      */
     public boolean inRange (Interactive interactive, Location center) {
         // add z axis
         myRangeCircle = new Ellipse2D.Double(center.getX(), center.getY(), myRange, myRange);
         return myRangeCircle.contains(interactive.getCenter());
     }
     
     /**
      * subtracts 1 from the cooldown counter
      */
     public void decrementCooldown() {
     	cooldown--;
     }
     /**
      * Returns the cooldown time on the weapon
      * @return the cooldown time on the weapon
      */
     public int getCooldown() {
     	return cooldown;
     }
     /**
      * After the weapon fires, the cooldown is set to the max cooldown for the
      * weapon. 
      * @param time is the time that the cooldown is set to
      */
     private void setCooldown(int time) {
     	cooldown = time;
     }
 }
