 package vooga.rts.gamedesign.strategy.attackstrategy;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import vooga.rts.gamedesign.Weapon;
 import vooga.rts.gamedesign.sprite.Building;
 import vooga.rts.gamedesign.sprite.InteractiveEntity;
 import vooga.rts.gamedesign.sprite.rtsprite.IAttackable;
 import vooga.rts.gamedesign.sprite.rtsprite.RTSprite;
 import vooga.rts.gamedesign.sprite.rtsprite.interactive.units.Soldier;
 
 /**
  * This class implements AttackStrategy and is used as an instance in 
  * interactives for objects that are able to attack.  The attack method in this
  * class will specify how the interactive will attack.
  * 
  * @author Ryan Fishel
  * @author Kevin Oh
  * @author Francesco Agosti
  * @author Wenshun Liu 
  *
  */
 public class CanAttack implements AttackStrategy{
 
     private List<Weapon> myWeapons;
     private int myWeaponIndex;
 
     public CanAttack(){
         myWeapons = new ArrayList<Weapon>();
         myWeaponIndex = 0;
 
     }
 
     public void attack(IAttackable a) {
 
         System.out.println("Soldier a health " + ((InteractiveEntity) a).getHealth());
        try {
            myWeapons.get(myWeaponIndex).fire((InteractiveEntity) a);
        }
        catch (CloneNotSupportedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
         System.out.println("a died");
     }
     
     
     public boolean hasWeapon(){
         return !myWeapons.isEmpty();
     }
     public Weapon getWeapon(){
         return myWeapons.get(0);
     }
     public void addWeapons(Weapon weapon) {
         myWeapons.add(weapon);
     }
 
     @Override
     public void update (double elapsedTime) {
         myWeapons.get(myWeaponIndex).update(elapsedTime);
     }
 }
