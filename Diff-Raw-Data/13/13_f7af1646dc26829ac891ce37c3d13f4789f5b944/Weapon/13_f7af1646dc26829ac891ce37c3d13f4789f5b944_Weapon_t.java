 package vooga.rts.gamedesign.weapon;
 
 import vooga.rts.gamedesign.sprite.gamesprites.Projectile;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.InteractiveEntity;
 import vooga.rts.gamedesign.state.AttackingState;
 import vooga.rts.gamedesign.upgrades.*;
 import vooga.rts.util.DelayedTask;
 import vooga.rts.util.Location3D;
 import java.awt.geom.Ellipse2D;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 
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
 public class Weapon {
 
     public static int DEFAULT_RANGE = 500;
     public static int DEFAULT_COOLDOWN_TIME = 1;
 
     private Projectile myProjectile;
     private int myRange;
     private List<Projectile> myProjectiles;
     private double myCooldownTime;
     // private Interval interval;
     private Ellipse2D myRangeCircle;
     private Location3D myCenter;
     private AttackingState attackingState;
 
     private DelayedTask cooldownTime;
 
     /**
      * Creates a new weapon with default damage and projectile.
      * 
      * @param damage
      * @param projectile
      */
     public Weapon (Projectile projectile, int range, Location3D center, double cooldownTime) {
         myProjectile = projectile;
         myRange = range;
         // interval = new Interval(cooldownTime);
         myCooldownTime = cooldownTime;
         myCenter = center;
         myProjectiles = new ArrayList<Projectile>();
         attackingState = AttackingState.NOT_ATTACKING;
     }
 
     /**
      * This method is used by the weapon to attack an InteractiveEntity.
      * 
      */
     public void fire (InteractiveEntity interactiveEntity) {
         final InteractiveEntity toBeShot = interactiveEntity;
         if (!toBeShot.isDead() && attackingState == AttackingState.NOT_ATTACKING) {
             attackingState = AttackingState.WAITING;
             cooldownTime = new DelayedTask(myCooldownTime, new Runnable() {
 
                 @Override
                 public void run () {
                     attackingState = AttackingState.ATTACKING;
 
                 }
             });
         }
         if (attackingState == AttackingState.ATTACKING) {
             Projectile firingProjectile = myProjectile.copy(myProjectile, myCenter);
             firingProjectile.setEnemy(toBeShot);
             firingProjectile.move(toBeShot.getWorldLocation());
             myProjectiles.add(firingProjectile);
             attackingState = AttackingState.NOT_ATTACKING;
         }
     }
 
     /**
      * 
      * NOTE: moving this method is gonna break DamageUpgradeNode.
      * 
      * @param damage
      */
     public void addDamage (int damage) {
         myProjectile.addDamage(damage);
     }
 
     /**
      * Returns the list of projectiles.
      * 
      * @return the list of projectiles that this weapon has
      */
     public List<Projectile> getProjectiles () {
         return myProjectiles;
     }
 
     /**
      * This method is used to change the projectile for the weapon.
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
     public boolean inRange (InteractiveEntity enemy, double distance) {
 //        myRangeCircle = new Ellipse2D.Double(myCenter.getX(), myCenter.getY(), myRange, myRange);
 //        return myRangeCircle.contains(enemy.getWorldLocation().to2D());
//        ellipse thing doesnt seem to be working very well.
         return (distance < this.myRange);
     }
 
     /**
      * Returns the range of the weapon.
      * 
      * @return the range of the weapon
      */
     public int getRange () {
         return myRange;
     }
 
     /**
      * Increases the range of the weapon
      * 
      * @param range the amount of range to be increased
      */
     public void addRange (int range) {
         myRange += range;
     }
 
     /**
      * Updates the weapon so that the cooldown between attacks is decremented
      * and the projectiles are updated.
      * 
      * @param elapsedTime is the time that has elapsed.
      */
     public void update (double elapsedTime) {
         if (cooldownTime != null) {
             cooldownTime.update(elapsedTime);
         }
         Iterator<Projectile> it = myProjectiles.iterator();
         while (it.hasNext()) {
             Projectile p = it.next();
             if (!p.isDead()) {
                 p.update(elapsedTime);
             }
             else {
                 it.remove();
             }
         }
     }
 
     /**
      * @param center the center to set
      */
     public void setCenter (Location3D center) {
         myCenter = center;
     }
 }
