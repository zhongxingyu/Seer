 package vooga.rts.gamedesign.sprite.rtsprite.interactive.units;
 
 import java.util.List;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import vooga.rts.gamedesign.Weapon;
 import vooga.rts.gamedesign.sprite.Unit;
 import vooga.rts.gamedesign.sprite.rtsprite.IAttackable;
 import vooga.rts.gamedesign.sprite.rtsprite.Projectile;
 import vooga.rts.gamedesign.sprite.rtsprite.RTSprite;
import vooga.rts.gamedesign.sprite.rtsprite.EntityVisitor;
 import vooga.rts.gamedesign.sprite.rtsprite.interactive.IOccupiable;
 import vooga.rts.gamedesign.strategy.attackstrategy.AttackStrategy;
 import vooga.rts.gamedesign.strategy.attackstrategy.CanAttack;
 import vooga.rts.util.Location;
 import vooga.rts.util.Pixmap;
 import vooga.rts.util.Sound;
 
 
 /**
  * 
  * @author Ryan Fishel
  * @author Kevin Oh
  * @author Francesco Agosti
  * @author Wenshun Liu
  * 
  */
 public class Soldier extends Unit {
     /**
      * for this implementation of visit where the soldier visits a IOccupiable,
      * the soldier occupies the IOccupiable RTSprite.
      * Code: would call myOccupyStrategy.occupy(RTSprite);
      */
 
     // private int myHealth; //TESTING PURPOSE
 
     public Soldier(Pixmap image, Location center, Dimension size, Sound sound, int teamID, int health) {
         super(image, center, size, sound, teamID, health);
     }
 
     @Override
     public void update (double elapsedTime) {
         super.update(elapsedTime);
         getAttackStrategy().update(elapsedTime);
     }
     @Override
     public void paint(Graphics2D pen) {
         super.paint(pen);
         AttackStrategy as = getAttackStrategy();
         if (as instanceof CanAttack) {
             for(Projectile p : ((CanAttack)as).getWeapon().getProjectiles()) {
                 p.paint(pen);
             }
         }
     }
     public void upgradeHealth (int armor) { // TESTING PURPOSE
         setHealth(getHealth() + armor);
     }
 
 }
